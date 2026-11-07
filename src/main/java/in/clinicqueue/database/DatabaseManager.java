package in.clinicqueue.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages database connections for ClinicQueue.
 * Ensures foreign keys are enabled on every connection.
 */
public class DatabaseManager {
    private static final String DEFAULT_DB_URL = "jdbc:sqlite:clinicqueue.db";
    private static String dbUrl = DEFAULT_DB_URL;

    // Disallow instantiation
    private DatabaseManager() {}

    /**
     * Allows setting a custom DB URL (useful for running tests with in-memory or isolated DB).
     */
    public static void setDbUrl(String newDbUrl) {
        dbUrl = newDbUrl;
    }

    /**
     * Resets DB URL back to default.
     */
    public static void resetToDefaultUrl() {
        dbUrl = DEFAULT_DB_URL;
    }

    /**
     * Opens a new database connection and enables foreign key enforcement.
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        // SQLite disables foreign keys by default, so we explicitly enable them on every connection.
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }
}
