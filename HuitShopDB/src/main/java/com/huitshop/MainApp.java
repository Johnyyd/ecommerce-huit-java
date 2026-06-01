package com.huitshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.huitshop.config.DbConnection;

@SpringBootApplication
public class MainApp {
    public static void main(String[] args) {
        try {
            DbConnection.testConnection();
        } catch (Exception e) {
            System.err.println("Database connection failed on startup: " + e.getMessage());
        }
        SpringApplication.run(MainApp.class, args);
    }
}
