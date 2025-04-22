package Admin_View;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.uts_pbo.DatabaseConnection;

public class TransactionDAO {
    public static List<TransactionEntry> getAllTransactions() {
        List<TransactionEntry> list = new ArrayList<>();
        String sql = "SELECT transaction_id, date, username, products, total_price, total_item FROM transaction ORDER BY date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                int transactionId = rs.getInt("transaction_id");
                LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
                String username = rs.getString("username");
                String products = rs.getString("products");
                double totalPrice = rs.getDouble("total_price");
                int totalItem = rs.getInt("total_item");
                
                list.add(new TransactionEntry(transactionId, date, username, products, totalPrice, totalItem));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public static List<TransactionEntry> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<TransactionEntry> list = new ArrayList<>();
        String sql = "SELECT transaction_id, date, username, products, total_price, total_item FROM transaction " +
                     "WHERE date BETWEEN ? AND ? ORDER BY date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, startDate);
            stmt.setObject(2, endDate);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int transactionId = rs.getInt("transaction_id");
                    LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
                    String username = rs.getString("username");
                    String products = rs.getString("products");
                    double totalPrice = rs.getDouble("total_price");
                    int totalItem = rs.getInt("total_item");
                    
                    list.add(new TransactionEntry(transactionId, date, username, products, totalPrice, totalItem));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public static List<TransactionEntry> getTransactionsByUsername(String usernameSearch) {
        List<TransactionEntry> list = new ArrayList<>();
        String sql = "SELECT transaction_id, date, username, products, total_price, total_item FROM transaction " +
                     "WHERE username LIKE ? ORDER BY date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + usernameSearch + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int transactionId = rs.getInt("transaction_id");
                    LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
                    String username = rs.getString("username");
                    String products = rs.getString("products");
                    double totalPrice = rs.getDouble("total_price");
                    int totalItem = rs.getInt("total_item");
                    
                    list.add(new TransactionEntry(transactionId, date, username, products, totalPrice, totalItem));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}