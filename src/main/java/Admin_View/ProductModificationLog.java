package Admin_View;

import java.time.LocalDateTime;

public class ProductModificationLog {
    private int id;
    private int userId;
    private String username;
    private String productCode;
    private String productName;
    private String actionType;
    private LocalDateTime timestamp;
    
    // Constructor without actionDetails
    public ProductModificationLog(int id, int userId, String username, String productCode,
                                 String productName, String actionType, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.productCode = productCode;
        this.productName = productName;
        this.actionType = actionType;
        this.timestamp = timestamp;
    }
    
    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getProductCode() { return productCode; }
    public String getProductName() { return productName; }
    public String getActionType() { return actionType; }
    public LocalDateTime getTimestamp() { return timestamp; }
}