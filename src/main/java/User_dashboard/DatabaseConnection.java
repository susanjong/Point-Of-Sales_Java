package com.example.uts_pbo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // PgAdmin connection detail
    private static final String URL = "jdbc:postgresql://localhost:5432/pos_java"; //only change your port and database name
    /* Just change the port 5432 according to your database in pgAdmin. If it is different, please replace it.
pos_java is the name of the database I created in Supabase; you can change it to any name you prefer.*/
    private static final String USER = "YOUR_USER_PGADMIN";
    private static final String PASSWORD = "YOUR_PASSWORD_PGADMIN";

    // Get a database connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}