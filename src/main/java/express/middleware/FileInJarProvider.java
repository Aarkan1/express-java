package express.middleware;

import express.Express;
import express.http.HttpRequestHandler;
import express.http.request.Request;
import express.http.response.Response;
import express.utils.MediaType;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Johan Wirén
 * An middleware to provide access to static server-files from within the jar file.
 */
public final class FileInJarProvider implements HttpRequestHandler {
    private final Logger logger;

    {
        this.logger = Logger.getLogger(this.getClass().getSimpleName());
    }

    public FileInJarProvider(String root) throws IOException {
        if (!root.startsWith("/browser")) {
            throw new IOException(root + " does not exists or isn't a directory.");
        }
    }

    @Override
    public void handle(Request req, Response res) {
        try {
            String path = req.getURI().getPath();


            // Check context
            String context = req.getContext();
            if (path.indexOf(context) == 0) {
                path = path.substring(context.length());
            }

            // If the path is empty try index.html
            if (path.length() <= 1) {
                path = "/index.html";
            }

            if(path.startsWith("/www") || path.equals("/index.html")) {
                InputStream resourceStream = Express.class.getResourceAsStream("/browser" + path);

                if (resourceStream != null) {
                    finish(path, resourceStream, req, res);
                } else {
                    path = "/index.html";
                    InputStream fallbackStream = Express.class.getResourceAsStream("/browser" + path);
                    finish(path, fallbackStream, req, res);
                }
            }

        } catch (Exception e) {
            logger.log(Level.INFO, "Could not find file in Jar.", e);
        }
    }

    private void finish(String path, InputStream resourceStream, Request req, Response res) throws IOException {
        String ex = path.substring(path.lastIndexOf(".") + 1);
        res.streamFrom(resourceStream.available(), resourceStream, MediaType.getByExtension(ex));
    }

    /**
     * Returns the logger which is concerned for this FileProvider object.
     * There is no default-handler active, if you want to log it you need to set an handler.
     *
     * @return The logger from this FileProvider object.
     */
    public Logger getLogger() {
        return logger;
    }
}
