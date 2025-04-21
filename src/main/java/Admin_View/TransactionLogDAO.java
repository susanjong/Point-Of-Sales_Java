package Admin_View;

import com.example.uts_pbo.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionLogDAO {
    
    // Record a new transaction log entry
    public static boolean recordLog(TransactionLogEntry entry) {
        String sql = "INSERT INTO transaction_log (timestamp, transaction_id, user_id, username, role, " +
                     "amount, item_count, payment_method, transaction_type, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            // Convert LocalDateTime to Timestamp
            Timestamp timestamp = Timestamp.valueOf(entry.getTimestamp());
            
            stmt.setTimestamp(1, timestamp);
            stmt.setInt(2, entry.getTransactionId());
            stmt.setInt(3, entry.getUserId());
            stmt.setString(4, entry.getUsername());
            stmt.setString(5, entry.getRole());
            stmt.setDouble(6, entry.getAmount());
            stmt.setInt(7, entry.getItemCount());
            stmt.setString(8, entry.getPaymentMethod());
            stmt.setString(9, entry.getTransactionType());
            stmt.setString(10, entry.getStatus());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get all transaction logs
    public static List<TransactionLogEntry> getAllLogs() {
        List<TransactionLogEntry> logs = new ArrayList<>();
        String sql = "SELECT * FROM transaction_log ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                LocalDateTime dateTime = timestamp.toLocalDateTime();
                int transactionId = rs.getInt("transaction_id");
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                String role = rs.getString("role");
                double amount = rs.getDouble("amount");
                int itemCount = rs.getInt("item_count");
                String paymentMethod = rs.getString("payment_method");
                String transactionType = rs.getString("transaction_type");
                String status = rs.getString("status");
                
                TransactionLogEntry entry = new TransactionLogEntry(
                    id, dateTime, transactionId, userId, username, role, 
                    amount, itemCount, paymentMethod, transactionType, status
                );
                logs.add(entry);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return logs;
    }
    
    // Get logs by user ID
    public static List<TransactionLogEntry> getLogsByUserId(int userId) {
        List<TransactionLogEntry> logs = new ArrayList<>();
        String sql = "SELECT * FROM transaction_log WHERE user_id = ? ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                LocalDateTime dateTime = timestamp.toLocalDateTime();
                int transactionId = rs.getInt("transaction_id");
                String username = rs.getString("username");
                String role = rs.getString("role");
                double amount = rs.getDouble("amount");
                int itemCount = rs.getInt("item_count");
                String paymentMethod = rs.getString("payment_method");
                String transactionType = rs.getString("transaction_type");
                String status = rs.getString("status");
                
                TransactionLogEntry entry = new TransactionLogEntry(
                    id, dateTime, transactionId, userId, username, role, 
                    amount, itemCount, paymentMethod, transactionType, status
                );
                logs.add(entry);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return logs;
    }
    
    // Get logs by transaction type
    public static List<TransactionLogEntry> getLogsByTransactionType(String type) {
        List<TransactionLogEntry> logs = new ArrayList<>();
        String sql = "SELECT * FROM transaction_log WHERE transaction_type = ? ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                LocalDateTime dateTime = timestamp.toLocalDateTime();
                int transactionId = rs.getInt("transaction_id");
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                String role = rs.getString("role");
                double amount = rs.getDouble("amount");
                int itemCount = rs.getInt("item_count");
                String paymentMethod = rs.getString("payment_method");
                String status = rs.getString("status");
                
                TransactionLogEntry entry = new TransactionLogEntry(
                    id, dateTime, transactionId, userId, username, role, 
                    amount, itemCount, paymentMethod, type, status
                );
                logs.add(entry);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return logs;
    }
    
    // Get logs by status
    public static List<TransactionLogEntry> getLogsByStatus(String status) {
        List<TransactionLogEntry> logs = new ArrayList<>();
        String sql = "SELECT * FROM transaction_log WHERE status = ? ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                LocalDateTime dateTime = timestamp.toLocalDateTime();
                int transactionId = rs.getInt("transaction_id");
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                String role = rs.getString("role");
                double amount = rs.getDouble("amount");
                int itemCount = rs.getInt("item_count");
                String paymentMethod = rs.getString("payment_method");
                String transactionType = rs.getString("transaction_type");
                
                TransactionLogEntry entry = new TransactionLogEntry(
                    id, dateTime, transactionId, userId, username, role, 
                    amount, itemCount, paymentMethod, transactionType, status
                );
                logs.add(entry);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return logs;
    }
    
    // Get logs within a date range
    public static List<TransactionLogEntry> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<TransactionLogEntry> logs = new ArrayList<>();
        String sql = "SELECT * FROM transaction_log WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                LocalDateTime dateTime = timestamp.toLocalDateTime();
                int transactionId = rs.getInt("transaction_id");
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                String role = rs.getString("role");
                double amount = rs.getDouble("amount");
                int itemCount = rs.getInt("item_count");
                String paymentMethod = rs.getString("payment_method");
                String transactionType = rs.getString("transaction_type");
                String status = rs.getString("status");
                
                TransactionLogEntry entry = new TransactionLogEntry(
                    id, dateTime, transactionId, userId, username, role, 
                    amount, itemCount, paymentMethod, transactionType, status
                );
                logs.add(entry);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return logs;
    }
    
    // Get logs by transaction amount range
    public static List<TransactionLogEntry> getLogsByAmountRange(double minAmount, double maxAmount) {
        List<TransactionLogEntry> logs = new ArrayList<>();
        String sql = "SELECT * FROM transaction_log WHERE amount BETWEEN ? AND ? ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, minAmount);
            stmt.setDouble(2, maxAmount);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                LocalDateTime dateTime = timestamp.toLocalDateTime();
                int transactionId = rs.getInt("transaction_id");
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                String role = rs.getString("role");
                double amount = rs.getDouble("amount");
                int itemCount = rs.getInt("item_count");
                String paymentMethod = rs.getString("payment_method");
                String transactionType = rs.getString("transaction_type");
                String status = rs.getString("status");
                
                TransactionLogEntry entry = new TransactionLogEntry(
                    id, dateTime, transactionId, userId, username, role, 
                    amount, itemCount, paymentMethod, transactionType, status
                );
                logs.add(entry);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return logs;
    }
    
    // Create the transaction_log table if it doesn't exist
    public static void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS transaction_log (" +
                    "id SERIAL PRIMARY KEY, " +
                    "timestamp TIMESTAMP NOT NULL, " +
                    "transaction_id INTEGER NOT NULL, " +
                    "user_id INTEGER NOT NULL, " +
                    "username VARCHAR(100) NOT NULL, " +
                    "role VARCHAR(50) NOT NULL, " +
                    "amount DOUBLE PRECISION NOT NULL, " +
                    "item_count INTEGER NOT NULL, " +
                    "payment_method VARCHAR(50) NOT NULL, " +
                    "transaction_type VARCHAR(50) NOT NULL, " +
                    "status VARCHAR(50) NOT NULL" +
                    ")";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(sql);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}