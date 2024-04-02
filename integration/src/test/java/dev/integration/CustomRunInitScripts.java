package dev.integration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Loads dummy data in resources for testing.
 * */
class CustomRunInitScripts {

    /**
     * Establishes connection with database.
     *
     * @return a Connection object.
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
     * Write dummy data to database.
     *
     * @see <a href="https://docs.oracle.com/javase/tutorial/jdbc/basics/processingsqlstatements.html">documentation</a>
     */
    public static void processScript(String username, String password) throws SQLException {
        var path = Paths.get("src/test/resources/db/init.sql");

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

}
