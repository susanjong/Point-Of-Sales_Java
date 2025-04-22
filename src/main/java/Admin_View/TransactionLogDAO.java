package Admin_View;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.uts_pbo.DatabaseConnection;

public class TransactionLogDAO {
    
    public static List<TransactionLogEntry> getAllLogs() {
        List<TransactionEntry> transactions = TransactionDAO.getAllTransactions();
        List<TransactionLogEntry> logs = new ArrayList<>();
        
        int id = 1;
        for (TransactionEntry transaction : transactions) {
            // Default value for payment method (you can update this based on your data)
            String paymentMethod = determinePaymentMethod(transaction.getTransactionId());
            String status = "Completed"; // Default status
            
            logs.add(TransactionLogEntry.fromTransactionEntry(id++, transaction, paymentMethod, status));
        }
        
        return logs;
    }
    
    public static List<TransactionLogEntry> getLogsByTransactionType(String transactionType) {
        return getAllLogs().stream()
                .filter(log -> log.getTransactionType().equals(transactionType))
                .collect(Collectors.toList());
    }
    
    public static List<TransactionLogEntry> getLogsByStatus(String status) {
        return getAllLogs().stream()
                .filter(log -> log.getStatus().equals(status))
                .collect(Collectors.toList());
    }
    
    public static List<TransactionLogEntry> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<TransactionEntry> transactions = TransactionDAO.getTransactionsByDateRange(startDate, endDate);
        List<TransactionLogEntry> logs = new ArrayList<>();
        
        int id = 1;
        for (TransactionEntry transaction : transactions) {
            String paymentMethod = determinePaymentMethod(transaction.getTransactionId());
            String status = "Completed"; // Default status
            
            logs.add(TransactionLogEntry.fromTransactionEntry(id++, transaction, paymentMethod, status));
        }
        
        return logs;
    }
    
    private static String determinePaymentMethod(int transactionId) {
        // Try to get payment method from database if you have a payment_method column
        String paymentMethod = "Cash"; // Default
        
        String sql = "SELECT payment_method FROM transaction WHERE transaction_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, transactionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String dbPaymentMethod = rs.getString("payment_method");
                    if (dbPaymentMethod != null && !dbPaymentMethod.isEmpty()) {
                        paymentMethod = dbPaymentMethod;
                    }
                }
            }
        } catch (SQLException e) {
            // If the column doesn't exist or there's another error, use the default
            System.err.println("Could not retrieve payment method: " + e.getMessage());
        }
        
        return paymentMethod;
    }
}