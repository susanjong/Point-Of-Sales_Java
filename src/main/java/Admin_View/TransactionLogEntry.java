package Admin_View;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TransactionLogEntry {
    private int id;
    private LocalDateTime timestamp;
    private int transactionId;
    private String username;
    private String products;
    private double amount;
    private int itemCount;
    private String paymentMethod;
    private String transactionType;
    private String status;
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TransactionLogEntry(int id, LocalDateTime timestamp, int transactionId, 
                               String username, String products, double amount, int itemCount,
                               String paymentMethod, String transactionType, String status) {
        this.id = id;
        this.timestamp = timestamp;
        this.transactionId = transactionId;
        this.username = username;
        this.products = products;
        this.amount = amount;
        this.itemCount = itemCount;
        this.paymentMethod = paymentMethod;
        this.transactionType = transactionType;
        this.status = status;
    }
    
    // Convert from TransactionEntry to TransactionLogEntry
    public static TransactionLogEntry fromTransactionEntry(int id, TransactionEntry entry,
                                                        String paymentMethod, String status) {
        return new TransactionLogEntry(
            id,
            entry.getDate(),
            entry.getTransactionId(),
            entry.getUsername(),
            entry.getProducts(),
            entry.getTotalPrice(),
            entry.getTotalItem(),
            paymentMethod,
            "Sale",
            status
        );
    }

    TransactionLogEntry(int i, LocalDateTime timestamp, int transactionId, int i0, String username, String customer, double totalRefund, int qty, String cash, String aReturn, String completed) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // Getters
    public int getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getFormattedTimestamp() {
        return timestamp.format(formatter);
    }

    public int getTransactionId() {
        return transactionId;
    }

    public String getUsername() {
        return username;
    }
    
    public String getProducts() {
        return products;
    }

    public double getAmount() {
        return amount;
    }
    
    public String getFormattedAmount() {
        return String.format("%,.2f", amount);
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