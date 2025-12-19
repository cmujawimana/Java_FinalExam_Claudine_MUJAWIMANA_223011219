package com.security.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/securityassistantsystem?serverTimezone=UTC";
    private static final String USER = "root"; // change if needed
    private static final String PASSWORD = ""; // your MySQL password if any

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Load MySQL JDBC driver explicitly
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ JDBC Driver not found! Add MySQL connector to classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            e.printStackTrace();
        }
        return connection;
    }
}
