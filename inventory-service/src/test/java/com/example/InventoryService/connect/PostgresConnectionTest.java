package com.example.InventoryService.connect;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;

public class PostgresConnectionTest {

    @Test
    void testDirectConnection() {
        String url = "jdbc:postgresql://localhost:5432/online_store";
        String username = "postgres";
        String password = "password";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("✅ PostgreSQL connection successful!");
            System.out.println("Database: " + connection.getMetaData().getDatabaseProductName());
            System.out.println("Version: " + connection.getMetaData().getDatabaseProductVersion());
        } catch (Exception e) {
            System.out.println("❌ PostgreSQL connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}