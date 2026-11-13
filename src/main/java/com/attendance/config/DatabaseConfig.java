package com.attendance.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    private static DatabaseConfig instance;

    private static String dbUrl = "jdbc:h2:./data/attendance;AUTO_SERVER=TRUE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private DatabaseConfig() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 JDBC driver not found", e);
        }
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    /** Override DB URL (useful for tests with in-memory DBs). */
    public static void setUrl(String url) {
        dbUrl = url;
        instance = null; // Force re-creation
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, DB_USER, DB_PASSWORD);
    }

    /** Runs sql/schema.sql to create all tables. */
    public void initializeDatabase() {
        executeSqlFile("sql/schema.sql");
    }

    /** Runs sql/seed.sql to insert sample data. */
    public void seedDatabase() {
        executeSqlFile("sql/seed.sql");
    }

    private void executeSqlFile(String relativePath) {
        Path path = Paths.get(relativePath);
        if (!Files.exists(path)) {
            // Try from project root
            path = Paths.get(System.getProperty("user.dir"), relativePath);
        }
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = Files.readString(path);
            // Split by semicolons and execute each statement
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to execute SQL file: " + relativePath, e);
        }
    }

    /** Execute raw SQL (for testing). */
    public void executeSql(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute SQL", e);
        }
    }
}
