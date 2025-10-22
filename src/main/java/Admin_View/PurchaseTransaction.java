package Admin_View;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Random;

import User_dashboard.DatabaseConnection;
import User_dashboard.LoginController;
import User_dashboard.User;

import Admin_View.CashierController.CartItem;
import javafx.scene.control.Alert;

public class PurchaseTransaction extends Transaction implements Payable {
    private List<CartItem> items;
    private String username;
    private double totalAmount;
    private boolean transactionSaved = false;
    private boolean processingComplete = false;
    
    public PurchaseTransaction() {
        super();
    }
    
    public PurchaseTransaction(Date date, int transactionId, List<CartItem> items, String username) {
        super(date, transactionId);
        this.items = items;
        this.username = username;
        this.totalAmount = calculateTotal();
    }
    
    public List<CartItem> getItems() {
        return items;
    }
    
    public void setItems(List<CartItem> items) {
        this.items = items;
        this.totalAmount = calculateTotal();
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    @Override
    public double calculateTotal() {
        double total = 0.0;
        // Changed from cartItems to items
        for (CartItem item : items) {
            double itemSubtotal = item.getPrice() * item.getQuantity();
            total += itemSubtotal;
        }
        return total;
    }

    @Override
    public void processTransaction() {
        if (!transactionSaved) {
            serializeTransaction();
        }

        processingComplete = true;

        System.out.println("Transaksi dengan ID " + getTransactionId() + " telah diproses");

        setTransactionId(0);
        transactionSaved = false;
        processingComplete = false;
    }

    @Override
    public String serializeTransaction() {
        if (transactionSaved) {
            StringBuilder transactionData = new StringBuilder();
            transactionData.append("Transaction ID: ").append(getTransactionId()).append("\n");
            return transactionData.toString();
        }

        setTransactionId(0);
        processingComplete = false;
        transactionSaved = false;

        StringBuilder transactionData = new StringBuilder();
        Connection conn = null;
        
        try {
            // Dapatkan koneksi dan tambahkan parameter untuk menonaktifkan prepared statements
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection is null");
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to database");
                return "Error: Database connection failed";
            }
            
            // Coba deallocate semua prepared statements yang ada
            try (Statement deallocStmt = conn.createStatement()) {
                deallocStmt.execute("DEALLOCATE ALL");
            } catch (SQLException e) {
                // Abaikan error ini, hanya mencoba membersihkan
                System.out.println("Info: Deallocate all statements: " + e.getMessage());
            }
            
            // Set autocommit false untuk memulai transaksi database
            conn.setAutoCommit(false);
            
            // Get current date and time
            java.util.Date currentDate = new java.util.Date();
            java.sql.Timestamp timestamp = new java.sql.Timestamp(currentDate.getTime());
            
            // Count total items
            int totalItems = 0;
            // Changed from cartItems to items
            for (CartItem item : items) {
                totalItems += item.getQuantity();
            }
            
            // Create products string
            StringBuilder productsStr = new StringBuilder();
            // Changed from cartItems to items
            for (CartItem item : items) {
                productsStr.append(item.getName())
                        .append(" (")
                        .append(item.getQuantity())
                        .append("), ");
            }
            if (productsStr.length() > 2) {
                productsStr.setLength(productsStr.length() - 2);
            }

            String username = "guest"; // Default value
            User currentUser = LoginController.getCurrentUser();
            if (currentUser != null) {
                username = currentUser.getUsername();
            }
            
            // Gunakan try-with-resources untuk setiap statement
            String query = "INSERT INTO transaction (date, username, products, total_item, total_price) VALUES (?, ?, ?, ?, ?) RETURNING transaction_id";
            
            // Gunakan nama unik untuk setiap prepared statement
            String uniqueId = System.currentTimeMillis() + "_" + new Random().nextInt(1000);
            
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setTimestamp(1, timestamp);
                pst.setString(2, username);
                pst.setString(3, productsStr.toString());
                pst.setInt(4, totalItems);
                pst.setDouble(5, totalAmount);
                
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        setTransactionId(rs.getInt(1));
                    } else {
                        setTransactionId(new Random().nextInt(100000000));
                    }
                }
            }

            // Build transaction summary
            transactionData.append("Transaction ID: ").append(getTransactionId()).append("\n");
            transactionData.append("Date: ").append(new java.util.Date()).append("\n");
            transactionData.append("Products: ").append(productsStr.toString()).append("\n");
            transactionData.append("Total Items: ").append(totalItems).append("\n");
            transactionData.append("Total Price: ").append(formatPrice(totalAmount)).append("\n");
            transactionData.append("User: ").append(username);
            
            // Print transaction details
            System.out.println("====================== TRANSACTION DETAIL ======================");
            System.out.println(transactionData.toString());
            System.out.println("===============================================================");
            
            // Update stok produk - gunakan Statement biasa bukan PreparedStatement
            // This section needs products variable that seems undefined. We need to handle this:
            List<Product> products = getProductsFromDatabase(); // Added method to get products
            
            // Changed from cartItems to items
            for (CartItem item : items) {
                String productCode = "";
                for (Product product : products) {
                    if (product.getName().equals(item.getName())) {
                        productCode = product.getCode();
                        break;
                    }
                }
                
                if (!productCode.isEmpty()) {
                    int quantity = item.getQuantity();
                    
                    // Gunakan statement biasa untuk mengurangi risiko prepared statement
                    try (Statement stmt = conn.createStatement()) {
                        String updateSQL = "UPDATE product SET qty = qty - " + quantity + " WHERE code = '" + productCode + "'";
                        int rowsAffected = stmt.executeUpdate(updateSQL);
                        
                        if (rowsAffected == 0) {
                            System.err.println("Warning: No stock updated for product: " + item.getName() + " (Code: " + productCode + ")");
                        } else {
                            System.out.println("- Updated stock for: " + item.getName() + " (Code: " + productCode + "), reduced by: " + quantity);
                        }
                    }
                } else {
                    System.err.println("Warning: Could not find product code for: " + item.getName());
                }
            }
            
            // Commit transaksi jika semua operasi berhasil
            conn.commit();
            transactionSaved = true;
            
        } catch (SQLException e) {
            System.err.println("SQL Error while serializing transaction: " + e.getMessage());
            e.printStackTrace();
            
            // Rollback jika terjadi error
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.rollback();
                    System.out.println("Transaction rolled back due to error");
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Could not save transaction data: " + e.getMessage());
            
            // Fallback jika error
            if (getTransactionId() == 0) {
                setTransactionId(new Random().nextInt(100000000));
            }
        } finally {
            // Tutup koneksi di finally
            try {
                if (conn != null && !conn.isClosed()) {
                    // Coba deallocate lagi sebelum menutup
                    try (Statement deallocStmt = conn.createStatement()) {
                        deallocStmt.execute("DEALLOCATE ALL");
                    } catch (SQLException e) {
                        // Abaikan
                    }
                    
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
        
        return transactionData.toString();
    }
    
    // Helper method to format price
    private String formatPrice(double price) {
        java.text.NumberFormat currencyFormat = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("id", "ID"));
        currencyFormat.setMaximumFractionDigits(0);
        return currencyFormat.format(price).replace("Rp", "Rp ");
    }
    
    // Helper method to show alerts
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Helper method to get products from database
    private List<Product> getProductsFromDatabase() {
        List<Product> products = new java.util.ArrayList<>();
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT code, product_name, price, qty, exp_date, category FROM product";
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();
            
            while (rs.next()) {
                String code = rs.getString("code");
                String name = rs.getString("product_name");
                double price = rs.getDouble("price");
                int qty = rs.getInt("qty");
                
                // Handle exp_date which can be null
                String expDate = null;
                java.sql.Date sqlDate = rs.getDate("exp_date");
                if (sqlDate != null) {
                    expDate = new java.text.SimpleDateFormat("dd-MM-yyyy").format(sqlDate);
                }
                
                String category = rs.getString("category");
              
                Product product = new Product(code, name, price, qty, expDate, category);
                products.add(product);
            }
            
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        return products;
    }
}