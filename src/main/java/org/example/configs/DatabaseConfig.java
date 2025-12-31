package org.example.configs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database configuration class for managing PostgreSQL database connections.
 * Provides methods to establish and manage database connections for the e-commerce system.
 */
public class DatabaseConfig {
    
    // Database connection parameters
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/ecomdb";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";
    
    // JDBC Driver class name
    private static final String JDBC_DRIVER = "org.postgresql.Driver";
    
    // Static block to load the JDBC driver
    static {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }
    
    /**
     * Establishes a connection to the PostgreSQL database.
     * 
     * @return Connection object if successful, null otherwise
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Establishes a connection to the database with custom credentials.
     * 
     * @param url Database URL
     * @param user Database username
     * @param password Database password
     * @return Connection object if successful
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection(String url, String user, String password) throws SQLException {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Closes a database connection safely.
     * 
     * @param connection The connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Tests the database connection.
     * 
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the database URL.
     * 
     * @return Database URL string
     */
    public static String getDbUrl() {
        return DB_URL;
    }
    
    /**
     * Gets the database username.
     * 
     * @return Database username string
     */
    public static String getDbUser() {
        return DB_USER;
    }
}

