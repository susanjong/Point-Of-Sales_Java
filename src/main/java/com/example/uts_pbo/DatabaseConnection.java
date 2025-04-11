package com.example.uts_pbo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Supabase connection details
    private static final String URL = "jdbc:postgresql://db.xgkxcfanebxtjidqiqka.supabase.co:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "pbopastisiap";
    
    // Get a database connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}