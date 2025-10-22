package Admin_View;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import User_dashboard.DatabaseConnection;
import User_dashboard.LoginController;
import User_dashboard.User;

public class RefundTransaction extends Transaction implements Payable {
    private List<Product> refundProducts;
    private String username;
    
    public RefundTransaction() {
        super();
        this.username = getCurrentUsername();
    }
    
    public RefundTransaction(Date date, int transactionId, List<Product> refundProducts) {
        super(date, transactionId);
        this.refundProducts = refundProducts;
        this.username = getCurrentUsername();
    }
    
    // Getter and setter methods
    public List<Product> getRefundProducts() {
        return refundProducts;
    }
    
    public void setRefundProducts(List<Product> refundProducts) {
        this.refundProducts = refundProducts;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    // Implementation of Payable interface methods
    @Override
    public double calculateTotal() {
        double totalAmount = 0.0;
        for (Product product : refundProducts) {
            totalAmount += product.getPrice() * product.getQuantity();
        }
        return totalAmount;
    }
    
    @Override
    public void processTransaction() {
        System.out.println("Transaksi dengan ID " + transactionId + " telah diproses");
    }
    
    @Override
    public String serializeTransaction() {
        StringBuilder serializedData = new StringBuilder();
        serializedData.append("Refund Transaction ID: ").append(transactionId)
                     .append(", Date: ").append(date.toString())
                     .append(", User: ").append(username)
                     .append(", Products: ");
        
        for (Product product : refundProducts) {
            serializedData.append(product.getName())
                         .append(" (").append(product.getQuantity()).append("), ");
        }
        
        serializedData.append("Total Amount: Rp ").append(calculateTotal());
        
        // Save transaction to database
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Get current timestamp
            java.sql.Timestamp timestamp = new java.sql.Timestamp(date.getTime());
            
            // Process each product as a separate refund record
            for (Product product : refundProducts) {
                // Calculate total refund amount for this product
                double productRefundAmount = product.getPrice() * product.getQuantity();
                
                // Insert into refund table
                String refundInsertQuery = "INSERT INTO refund (timestamp, transaction_id, username, product_code, qty, total_refund) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement refundStmt = conn.prepareStatement(refundInsertQuery);
                refundStmt.setTimestamp(1, timestamp);
                refundStmt.setInt(2, transactionId);
                refundStmt.setString(3, username);
                refundStmt.setString(4, product.getCode());
                refundStmt.setInt(5, product.getQuantity());
                refundStmt.setDouble(6, productRefundAmount);
                refundStmt.executeUpdate();
                refundStmt.close();
                
                // Update product quantity (adding back the refunded items to inventory)
                String updateStockQuery = "UPDATE product SET qty = qty + ? WHERE code = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateStockQuery);
                updateStmt.setInt(1, product.getQuantity());
                updateStmt.setString(2, product.getCode());
                int updatedRows = updateStmt.executeUpdate();
                updateStmt.close();
                
                // Check if product was updated successfully
                if (updatedRows == 0) {
                    throw new SQLException("Failed to update quantity for product code: " + product.getCode());
                }
            }
            
            conn.commit(); // Commit transaction
            serializedData.append(" [Saved to database]");
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback in case of error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } 
            }
            serializedData.append(" [Database save failed: ").append(e.getMessage()).append("]");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return serializedData.toString();
    }
    
    // Helper method to get current username
    private String getCurrentUsername() {
        User currentUser = LoginController.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUsername();
        }
        return "unknown"; // Fallback value if no user is logged in
    }
}
