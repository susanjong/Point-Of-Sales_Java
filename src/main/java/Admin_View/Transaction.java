package Admin_View;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Transaction {
    private final String transactionId;
    private final LocalDateTime date;
    
    public Transaction() {
        this.transactionId = generateUniqueId();
        this.date = LocalDateTime.now();
    }
    
    private String generateUniqueId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
}