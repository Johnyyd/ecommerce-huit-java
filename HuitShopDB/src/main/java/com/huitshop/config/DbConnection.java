package com.huitshop.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbConnection {
    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream input = DbConnection.class.getClassLoader().getResourceAsStream("application.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                System.out.println("Sorry, unable to find application.properties. Using fallback connection details.");
                url = "jdbc:sqlserver://localhost;databaseName=HuitShopDB;encrypt=false;trustServerCertificate=true";
                user = "sa";
                password = "YourPassword";
            } else {
                prop.load(input);
                url = prop.getProperty("db.url");
                user = prop.getProperty("db.user");
                password = prop.getProperty("db.password");
            }
            // Register JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (user == null || user.trim().isEmpty()) {
            return DriverManager.getConnection(url);
        } else {
            return DriverManager.getConnection(url, user, password);
        }
    }
    
    public static void testConnection() throws SQLException {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connection established successfully!");
            }
        }
    }
}
