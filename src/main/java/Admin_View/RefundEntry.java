package Admin_View;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RefundEntry {
    private int refundId;
    private LocalDateTime timestamp;
    private int transactionId;
    private String username;
    private String productCode;
    private int qty;
    private double totalRefund;
    
    public RefundEntry(int refundId, LocalDateTime timestamp, int transactionId, 
                      String username, String productCode, int qty, double totalRefund) {
        this.refundId = refundId;
        this.timestamp = timestamp;
        this.transactionId = transactionId;
        this.username = username;
        this.productCode = productCode;
        this.qty = qty;
        this.totalRefund = totalRefund;
    }
    
    // Getters
    public int getRefundId() {
        return refundId;
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
    
    public String getUsername() {
        return username;
    }
    
    public String getProductCode() {
        return productCode;
    }
    
    public int getQty() {
        return qty;
    }
    
    public double getTotalRefund() {
        return totalRefund;
    }
    
    public String getFormattedTotalRefund() {
        return String.format("Rp %.0f", totalRefund);
    }
}