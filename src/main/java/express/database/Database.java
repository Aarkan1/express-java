package express.database;

import express.Express;
import express.database.exceptions.DatabaseNotEnabledException;
import express.database.exceptions.ModelsNotFoundException;
import express.middleware.FileInJarProvider;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.Id;
import org.reflections8.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Johan Wirén
 *
 * The embedded database based on Nitrite DB
 *
 * Documentation: https://www.dizitart.org/nitrite-database
 */
public class Database {
    private static Map<String, Collection> collections = new HashMap<>();
    private static boolean enabledDatabase = false;
    private static Express app;
    private static Express express = new Express();
    private static Logger logger;
    private static boolean enableSSEwatcher = false;
    private static boolean disableBrowser = false;

    public Database(Express app) {
        Database.app = app;
        try {
            init("db/embedded.db");
        } catch (ModelsNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Database(Express app, CollectionOptions... options) {
        Database.app = app;
        setOptions(options);

        try {
            init("db/embedded.db");
        } catch (ModelsNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Database(String dbPath, Express app) {
        Database.app = app;
        try {
            init(dbPath);
        } catch (ModelsNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Database(String dbPath, Express app, CollectionOptions... options) {
        Database.app = app;
        setOptions(options);

        try {
            init(dbPath);
        } catch (ModelsNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void setOptions(CollectionOptions... options) {
        for (CollectionOptions option : options) {
            if(option == CollectionOptions.ENABLE_SSE_WATCHER) enableSSEwatcher = true;
            else if(option == CollectionOptions.DISABLE_BROWSER) disableBrowser = true;
        }
    }

    private static void init(String dbPath) throws ModelsNotFoundException {
        logger = Logger.getLogger(Database.class.getSimpleName());

        if(dbPath.startsWith("db/")) {
            File directory = new File(Paths.get("db").toString());
            if (! directory.exists()){
                directory.mkdir();
            }
        }

        Nitrite db = Nitrite.builder()
                .compressed()
                .filePath(Paths.get(dbPath).toString())
                .openOrCreate("fwEWfwGhjyuYThtgSD", "dWTRgvVBfeeetgFR");

        Reflections reflections = new Reflections();
        Set<Class<?>> klasses = reflections.getTypesAnnotatedWith(Model.class);
        Set<String> collNames = klasses.stream().map(Class::getSimpleName).collect(Collectors.toSet());
        Map<String, String> idFields = new HashMap<>();

        if(klasses.isEmpty()) throw new ModelsNotFoundException("Must have a class with @Model to use embedded database.");

        klasses.forEach(klass -> {
            String klassName = klass.getSimpleName();
            for(Field field : klass.getDeclaredFields()) {
                if(field.isAnnotationPresent(Id.class)) {
                    idFields.putIfAbsent(klassName, field.getName());
                    break;
                }
            }

            collections.putIfAbsent(klassName, new Collection(db.getRepository(klass), klass, idFields.get(klassName)));
        });

        if (enableSSEwatcher) sseWatchCollections(collNames);
        if(!disableBrowser) initBrowser(collNames, idFields);

        enabledDatabase = true;
        Runtime.getRuntime().addShutdownHook(new Thread(db::close));
    }

    /**
     * Embedded server to browse collections locally
     */
    private static void initBrowser(Set<String> collNames, Map<String, String> idFields) {
        express.get("/rest/collNames", (req, res) -> res.json(collNames));

        express.get("/rest/:coll", (req, res) -> {
            String coll = req.getParam("coll");
            res.json(Map.of(idFields.get(coll), collection(coll).find()));
        });

        express.delete("/rest/:coll/:id", (req, res) -> {
            String coll = req.getParam("coll");
            String id = req.getParam("id");
            collection(coll).deleteById(id);
            res.send("OK");
        });

        try {
            express.use(new FileInJarProvider("/browser"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        express.listen(() -> System.out.println("\nBrowse collections: \thttp://localhost:9595"), 9595);
    }

    public void closeBrowser() {
        express.stop();
    }

    public static Collection collection(Object model) {
        return collection(model.getClass().getSimpleName());
    }

    public static Collection collection(Class klass) { return collection(klass.getSimpleName()); }

    public static Collection collection(String klass) {
        try {
            return getColl(klass);
        } catch (DatabaseNotEnabledException | NullPointerException e) {
            logger.log(Level.WARNING, "Database not enabled.", e);
        }
        return null;
    }

    private static Collection getColl(String klass) throws DatabaseNotEnabledException {
        if(enabledDatabase) {
            return collections.get(klass);
        } else {
            throw new DatabaseNotEnabledException("Database is not enabled");
        }
    }

    private static void sseWatchCollections(Set<String> collNames) {
        app.sse("/watch-collections", (req, res) ->
            collNames.forEach(coll ->
                collection(coll).watch(watchData ->
                    res.send(coll, watchData)
        )));
    }

}
