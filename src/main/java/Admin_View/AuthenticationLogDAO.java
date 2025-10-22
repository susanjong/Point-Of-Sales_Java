package Admin_View;

import User_dashboard.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuthenticationLogDAO {
    
    // Record a new authentication log entry
    public static boolean recordLog(AuthenticationLogEntry entry) {
        String sql = "INSERT INTO authentication_log (timestamp, user_id, username, role, email, activity) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            // Convert LocalDateTime to Timestamp
            Timestamp timestamp = Timestamp.valueOf(entry.getTimestamp());
            
            stmt.setTimestamp(1, timestamp);
            stmt.setInt(2, entry.getUserId());
            stmt.setString(3, entry.getUsername());
            stmt.setString(4, entry.getRole());
            stmt.setString(5, entry.getEmail());
            stmt.setString(6, entry.getActivity());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get all authentication logs
    public static List<AuthenticationLogEntry> getAllLogs() {
        List<AuthenticationLogEntry> logs = new ArrayList<>();
        String sql = "SELECT * FROM authentication_log ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                LocalDateTime dateTime = timestamp.toLocalDateTime();
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                String role = rs.getString("role");
                String email = rs.getString("email");
                String activity = rs.getString("activity");
                
                AuthenticationLogEntry entry = new AuthenticationLogEntry(
                    id, dateTime, userId, username, role, email, activity
                );
                logs.add(entry);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return logs;
    }
    
    // Get logs by user ID
    public static List<AuthenticationLogEntry> getLogsByUserId(int userId) {
        List<AuthenticationLogEntry> logs = new ArrayList<>();
        String sql = "SELECT * FROM authentication_log WHERE user_id = ? ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                LocalDateTime dateTime = timestamp.toLocalDateTime();
                String username = rs.getString("username");
                String role = rs.getString("role");
                String email = rs.getString("email");
                String activity = rs.getString("activity");
                
                AuthenticationLogEntry entry = new AuthenticationLogEntry(
                    id, dateTime, userId, username, role, email, activity
                );
                logs.add(entry);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return logs;
    }
    
    // Get logs by activity type
    public static List<AuthenticationLogEntry> getLogsByActivity(String activity) {
        List<AuthenticationLogEntry> logs = new ArrayList<>();
        String sql = "SELECT * FROM authentication_log WHERE activity = ? ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, activity);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                LocalDateTime dateTime = timestamp.toLocalDateTime();
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                String role = rs.getString("role");
                String email = rs.getString("email");
                
                AuthenticationLogEntry entry = new AuthenticationLogEntry(
                    id, dateTime, userId, username, role, email, activity
                );
                logs.add(entry);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return logs;
    }
    
    // Get logs within a date range
    public static List<AuthenticationLogEntry> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<AuthenticationLogEntry> logs = new ArrayList<>();
        String sql = "SELECT * FROM authentication_log WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                LocalDateTime dateTime = timestamp.toLocalDateTime();
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                String role = rs.getString("role");
                String email = rs.getString("email");
                String activity = rs.getString("activity");
                
                AuthenticationLogEntry entry = new AuthenticationLogEntry(
                    id, dateTime, userId, username, role, email, activity
                );
                logs.add(entry);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return logs;
    }
    
    // Create the authentication_log table if it doesn't exist
    public static void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS authentication_log (" +
                    "id SERIAL PRIMARY KEY, " +
                    "timestamp TIMESTAMP NOT NULL, " +
                    "user_id INTEGER NOT NULL, " +
                    "username VARCHAR(100) NOT NULL, " +
                    "role VARCHAR(50) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL, " +
                    "activity VARCHAR(100) NOT NULL" +
                    ")";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(sql);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}