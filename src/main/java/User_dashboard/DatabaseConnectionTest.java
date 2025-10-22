package User_dashboard;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionTest {

    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        
        try {
            // Try to get a connection
            Connection conn = DatabaseConnection.getConnection();
            
            // If we get here, connection was successful
            System.out.println("Connection successful!");
            System.out.println("Connected to: " + conn.getMetaData().getURL());
            System.out.println("Database product: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("Database version: " + conn.getMetaData().getDatabaseProductVersion());
            
            // Don't forget to close the connection
            conn.close();
            System.out.println("Connection closed successfully.");
            
        } catch (SQLException e) {
            // If we get here, connection failed
            System.out.println("Connection failed!");
            System.out.println("Error message: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            e.printStackTrace();
        }
    }
}