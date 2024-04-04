package dev.integration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Loads dummy data in resources for testing.
 * */
public class CustomRunInitScripts {

    /**
     * Establishes connection with database.
     *
     * @return a {@link Connection} object.
     * @throws SQLException if not connection with specified database can be established.
     * @see <a href="https://docs.oracle.com/javase/tutorial/jdbc/basics/connecting.html">documentation</a>
     */
    private static Connection connection(String username, String password) throws SQLException {
        final String url = "jdbc:mysql://localhost:3306/integration_db";

        final Properties connectionProps = new Properties();
        connectionProps.put("user", username);
        connectionProps.put("password", password);

        return DriverManager.getConnection(url, connectionProps);
    }

    /**
     * Validate if database is empty.
     * */
    private static boolean scriptsBeenRan(String username, String password) throws SQLException {
        String query = "SELECT count(*) FROM product_category";
        try (var conn = connection(username, password); var statement = conn.createStatement()) {
            ResultSet rs = statement.executeQuery(query);
            int i = 0;
            while (rs.next()) {
                i = rs.getInt("count(*)");
                if (i > 0)
                    break;
            }
            return i > 0;
        }
    }

    /**
     * @see <a href="https://docs.oracle.com/javase/tutorial/jdbc/basics/processingsqlstatements.html">documentation</a>
     * */
    private static void dbInteraction(String username, String password, Path path) throws SQLException {
        assertTrue(Files.exists(path));

        try (var conn = connection(username, password); var statement = conn.createStatement()) {
            final StringBuilder sb = new StringBuilder();

            try (var reader = new BufferedReader(new FileReader(new File(path.toUri())))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (line.isEmpty() || line.startsWith("--"))
                        continue;

                    sb.append(line);

                    if (line.endsWith(";")) {
                        statement.execute(sb.toString());
                        sb.setLength(0);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Write dummy data to database.
     */
    public static void processScript(String username, String password) throws SQLException {
        if (scriptsBeenRan(username, password))
            return;

        var path = Paths.get("src/test/resources/db/init.sql");

        dbInteraction(username, password, path);
    }

    /**
     * Called in {@link dev.integration.worker.CronControllerTest} class.
     * */
    public static void insertDummyOrderReservation(String username, String password) throws SQLException {
        var path = Paths.get("src/test/resources/db/reservation.sql");

        processScript(username, password);
        dbInteraction(username, password, path);
    }

}
