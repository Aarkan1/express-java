package express.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Utils {

    private Utils() {}

    /**
     * Write all data from an InputStream in an String
     *
     * @param is The source InputStream
     * @return The data as string
     */
    public static String streamToString(InputStream is) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        } catch (IOException ignored) {
        }

        return null;
    }

    /**
     * Returns the MIME-Type of an file.
     *
     * @param file The file.
     * @return The MIME-Type.
     */
    public static MediaType getContentType(Path file) {
        String ex = getExtension(file);
        MediaType contentType = MediaType.getByExtension(ex);

        if (contentType == null) {
            return MediaType._bin;
        }

        return contentType;
    }

    /**
     * Generates an random token with SecureRandom
     *
     * @param byteLength The token length
     * @param radix      The base
     * @return An token with the base of radix
     */
    public static String randomToken(int byteLength, int radix) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[byteLength];
        secureRandom.nextBytes(token);
        return new BigInteger(1, token).toString(radix); //hex encoding
    }

    /**
     * @return Your ip.
     * @throws UnknownHostException If resolving fails
     */
    public static String getYourIp() throws UnknownHostException {
        return Inet4Address.getLocalHost().getHostAddress();
    }

    /**
     * Extract the extension from the file.
     *
     * @param file The file.
     * @return The extension.
     */
    public static String getExtension(Path file) {
        String path = file.getFileName().toString();
        int index = path.lastIndexOf('.') + 1;

        // No extension present
        if (index == 0) {
            return null;
        }

        return path.substring(index);
    }

    public static <T> T readResultSetToObject(ResultSet rs, Class<T> klass) throws JsonProcessingException {
        String json = readResultSetToJson(rs);
        if(json == null) return null;

        return new ObjectMapper().readValue(json, klass);
    }

    public static String readResultSetToJson(ResultSet rs) throws JsonProcessingException {
        List<Map<String, Object>> rows = null;
        ResultSetMetaData rsmd = null;

        try {
            rows = new ArrayList<>();
            rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            while (rs.next()) {
                // Represent a row in DB. Key: Column name, Value: Column value
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    // Note that the index is 1-based
                    String colName = rsmd.getColumnName(i);
                    Object colVal = rs.getObject(i);
                    row.put(colName, colVal);
                }
                rows.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return new ObjectMapper().writeValueAsString(rows);
    }

}
