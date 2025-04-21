package Admin_View;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TransactionLogEntry {
    private int id;
    private LocalDateTime timestamp;
    private int transactionId;
    private int userId;
    private String username;
    private String role;
    private double amount;
    private int itemCount;
    private String paymentMethod;
    private String transactionType;
    private String status;
    
    // For new log entries
    public TransactionLogEntry(int transactionId, int userId, String username, 
                              String role, double amount, int itemCount, 
                              String paymentMethod, String transactionType, String status) {
        this.timestamp = LocalDateTime.now();
        this.transactionId = transactionId;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.amount = amount;
        this.itemCount = itemCount;
        this.paymentMethod = paymentMethod;
        this.transactionType = transactionType;
        this.status = status;
    }
    
    // For loading from database
    public TransactionLogEntry(int id, LocalDateTime timestamp, int transactionId, 
                              int userId, String username, String role, 
                              double amount, int itemCount, String paymentMethod, 
                              String transactionType, String status) {
        this.id = id;
        this.timestamp = timestamp;
        this.transactionId = transactionId;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.amount = amount;
        this.itemCount = itemCount;
        this.paymentMethod = paymentMethod;
        this.transactionType = transactionType;
        this.status = status;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }
    
    public int getTransactionId() {
        return transactionId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getRole() {
        return role;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public String getFormattedAmount() {
        return String.format("Rp %.0f", amount);
    }
    
    public int getItemCount() {
        return itemCount;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public String getStatus() {
        return status;
    }
}