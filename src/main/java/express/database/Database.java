package express.database;

import express.Express;
import express.database.exceptions.DatabaseNotEnabledException;
import express.database.exceptions.ModelsNotFoundException;
import express.middleware.Middleware;
import org.dizitart.no2.Nitrite;
import org.reflections8.Reflections;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    private static Set<Class<?>> klasses = null;
    private Express express = new Express();

    public Database(Express app) {
        Database.app = app;
        try {
            init("db/embedded.db");
        } catch (ModelsNotFoundException e) {
            e.printStackTrace();
        }
        initBrowser();
    }

    public Database(String dbPath, Express app) {
        Database.app = app;
        try {
            init(dbPath);
        } catch (ModelsNotFoundException e) {
            e.printStackTrace();
        }
        initBrowser();
    }

    private static void init(String dbPath) throws ModelsNotFoundException {
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

        if(klasses.isEmpty()) throw new ModelsNotFoundException("Must have a class with @Model to use embedded database.");

        klasses.forEach(klass -> collections.putIfAbsent(klass.getSimpleName(), new Collection(db.getRepository(klass), klass)));

        sseWatchCollections(klasses);

        enabledDatabase = true;
        Runtime.getRuntime().addShutdownHook(new Thread(db::close));
    }

    /**
     * Embedded server to browse collections locally
     */
    private void initBrowser() {

        express.get("/rest/:coll", (req, res) -> {
            res.json(collection(req.getParam("coll")).find());
        });

        try {
            express.use(Middleware.staticsFromEmbeddedBrowser("/browser"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        express.listen(() -> System.out.println("Browse collections on http://localhost:9595"), 9595);
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
        } catch (DatabaseNotEnabledException e) {
            e.printStackTrace();
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

    private static void sseWatchCollections(Set<Class<?>> klasses) {
        app.get("/watch-collections", (req, res) -> {
            klasses.forEach(klass -> {
               collection(klass).watch(watchData -> {
                   res.sendSSE(watchData.getEvent(), watchData.getData());
               });
            });
        });
    }

}
