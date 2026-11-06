package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages JDBC Database Connectivity for MySQL.
 * Provides thread-safe, reconnecting connections and diagnostics.
 */
public class DatabaseConnection {

    private static Connection connection = null;

    static {
        try {
            Class.forName(DatabaseConfig.getDriver());
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL: MySQL JDBC Driver not found in classpath: " + e.getMessage());
        }
    }

    private DatabaseConnection() {
        // Utility class
    }

    /**
     * Obtains an active, valid connection to the MySQL database.
     * Reconnects automatically if the previous connection was closed or timed out.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            String url = DatabaseConfig.getDbUrl();
            String user = DatabaseConfig.getDbUser();
            String pass = DatabaseConfig.getDbPassword();
            connection = DriverManager.getConnection(url, user, pass);
        }
        return connection;
    }

    /**
     * Tests if the database is reachable and credentials are valid.
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Safely closes the active connection.
     */
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}
