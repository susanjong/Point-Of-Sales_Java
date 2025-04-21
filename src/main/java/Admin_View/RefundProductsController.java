package Admin_View;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.uts_pbo.DatabaseConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RefundProductsController {
    
    @FXML private Button profileBtn;
    @FXML private Button cashierBtn;
    @FXML private Button productsBtn;
    @FXML private Button bundleproductsBtn;
    @FXML private Button refundproductsBtn;
    @FXML private Button usersBtn;
    @FXML private Button adminLogBtn;
    
    @FXML private TableView<Product> refundproductTable;
    @FXML private TableColumn<Product, String> codeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> qtyColumn;
    @FXML private TableColumn<Product, String> expDateColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    
    @FXML private VBox refundItemsContainer;
    @FXML private Label totalAmountLabel;
    @FXML private TextField TransactionIDField;
    @FXML private Label balanceLabel;
    
    private ObservableList<Product> refundList = FXCollections.observableArrayList();
    private List<Product> selectedRefunds = new ArrayList<>();
    
    @FXML
    public void initialize() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        qtyColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        expDateColumn.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
    
        // Load refund data
        loadRefundData();
        
        // Set up table selection listener
        refundproductTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        refundproductTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                addToRefundList(newSelection);
            }
        });
        
        // Set up paid field listener for calculating balance
        TransactionIDField.textProperty().addListener((observable, oldValue, newValue) -> {
        });
    }
    
    private void loadRefundData() {
        // Clear the existing list
        refundList.clear();
        
        try {
            // Create database connection
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT code, product_name, price, qty, exp_date, category FROM product";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            while (rs.next()) {
                String code = rs.getString("code");
                String name = rs.getString("product_name");
                double price = rs.getDouble("price");
                int qty = rs.getInt("qty");
                
                // Handle exp_date properly - can be null
                String expDate = null;
                Date sqlDate = rs.getDate("exp_date");
                if (sqlDate != null) {
                    expDate = dateFormat.format(sqlDate);
                }
                
                String category = rs.getString("category");
                
                // Create a Product object and add it to the list
                Product product = new Product(code, name, price, qty, expDate, category);
                refundList.add(product);
            }
            
            // Close resources
            rs.close();
            pstmt.close();
            conn.close();
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Failed to load product data: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Set the items in the table
        refundproductTable.setItems(refundList);
    }
    
    private void addToRefundList(Product product) {
        // Check if product is already in the selected list
        if (!selectedRefunds.contains(product)) {
            // Create a new product instance with quantity set to 1
            Product refundProduct = new Product(
                product.getCode(),
                product.getName(),
                product.getPrice(),
                1,  // Set default quantity to 1
                product.getExpirationDate(),
                product.getCategory()
            );
            
            selectedRefunds.add(refundProduct);
            updateRefundItemsContainer();
        }
    }
    
    private void updateRefundItemsContainer() {
        refundItemsContainer.getChildren().clear();
        
        for (int i = 0; i < selectedRefunds.size(); i++) {
            Product product = selectedRefunds.get(i);
            HBox row = createRefundItemView(product, i);
            refundItemsContainer.getChildren().add(row);
        }
        
        // Create temporary RefundTransaction to calculate total
        RefundTransaction tempTransaction = new RefundTransaction();
        tempTransaction.setRefundProducts(selectedRefunds);
        double totalAmount = tempTransaction.calculateTotal();
        totalAmountLabel.setText("Rp " + totalAmount);
    }

    private HBox createRefundItemView(Product product, int index) {
        HBox itemRow = new HBox(10);
        itemRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        VBox productInfo = new VBox();
        HBox.setHgrow(productInfo, javafx.scene.layout.Priority.ALWAYS);
        
        Label nameLabel = new Label(product.getName());
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-size: 12px; -fx-pref-width: 250px;");
        
        Label priceLabel = new Label("Rp " + product.getPrice());
        priceLabel.setStyle("-fx-font-weight: bold;");
        
        productInfo.getChildren().addAll(nameLabel, priceLabel);
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #9A030F; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
        deleteBtn.setPrefWidth(70.0);
        deleteBtn.setOnAction(e -> {
            selectedRefunds.remove(index);
            updateRefundItemsContainer();
        });
        
        HBox quantityControls = new HBox(5);
        quantityControls.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button minusBtn = new Button("-");
        minusBtn.setPrefWidth(25.0);
        minusBtn.setOnAction(e -> {
            if (product.getQuantity() > 1) {
                product.setQuantity(product.getQuantity() - 1);
                updateRefundItemsContainer();
            }
        });
        
        Label quantityLabel = new Label(String.valueOf(product.getQuantity()));
        quantityLabel.setStyle("-fx-font-weight: bold;");
        
        Button plusBtn = new Button("+");
        plusBtn.setPrefWidth(25.0);
        plusBtn.setOnAction(e -> {
            product.setQuantity(product.getQuantity() + 1);
            updateRefundItemsContainer();
        });
        
        quantityControls.getChildren().addAll(minusBtn, quantityLabel, plusBtn);
        
        itemRow.getChildren().addAll(productInfo, quantityControls, deleteBtn);
        
        return itemRow;
    }
    
    @FXML
    private void handlerefund() {
        if (selectedRefunds.isEmpty()) {
            showAlert("Error", "No items selected for refund.");
            return;
        }

        // Validate transaction ID is a number
        String transactionIDText = TransactionIDField.getText().trim();
        if (transactionIDText.isEmpty()) {
            showAlert("Error", "Please enter a Transaction ID.");
            return;
        }
        
        int transactionID;
        try {
            transactionID = Integer.parseInt(transactionIDText);
        } catch (NumberFormatException e) {
            showAlert("Error", "Transaction ID must be a number.");
            return;
        }

        // Check if products exist in this transaction and quantities are valid
        if (!validateProductsInTransaction(transactionID, selectedRefunds)) {
            showAlert("Error", "One or more products are not found in the specified transaction or have insufficient quantities.");
            return;
        }
        
        try {
            // Create a RefundTransaction object
            RefundTransaction refundTx = new RefundTransaction(
                new java.util.Date(),
                transactionID,
                new ArrayList<>(selectedRefunds)
            );
            
            // Process the refund
            refundTx.processTransaction();
            
            // Serialize and save the transaction data to database
            String transactionData = refundTx.serializeTransaction();
            System.out.println(transactionData); // For debugging
            
            // Show success message using the calculateTotal method from RefundTransaction
            showAlert("Success", "Refund processed successfully for Transaction ID: " + transactionID +
                     "\nTotal refund amount: Rp " + refundTx.calculateTotal());
            
            // Clear the refund after processing
            selectedRefunds.clear();
            refundItemsContainer.getChildren().clear();
            totalAmountLabel.setText("Rp 0");
            TransactionIDField.setText("");
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to process refund: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateProductsInTransaction(int transactionID, List<Product> products) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Query to get the products string from the transaction
            String query = "SELECT products FROM transaction WHERE transaction_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, transactionID);
            
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                // Transaction not found
                conn.close();
                showAlert("Error", "Transaction ID not found.");
                return false;
            }
            
            String productsStr = rs.getString("products");
            rs.close();
            pstmt.close();
            conn.close();
            
            // Parse the products string to get product names and quantities
            Map<String, Integer> transactionProducts = parseProductsString(productsStr);
            
            // Check if each refund product is in the transaction with sufficient quantity
            for (Product refundProduct : products) {
                boolean productFound = false;
                
                // Look for the product in the transaction products
                for (Map.Entry<String, Integer> entry : transactionProducts.entrySet()) {
                    String productName = entry.getKey();
                    int purchasedQty = entry.getValue();
                    
                    // Check if this product name matches the refund product
                    if (productName.equals(refundProduct.getName())) {
                        productFound = true;
                        
                        // Check if there's sufficient quantity
                        if (refundProduct.getQuantity() > purchasedQty) {
                            showAlert("Error", "Cannot refund more than purchased quantity for " + productName);
                            return false;
                        }
                        break;
                    }
                }
                
                if (!productFound) {
                    showAlert("Error", "Product '" + refundProduct.getName() + "' not found in transaction.");
                    return false;
                }
            }
            
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error validating products in transaction: " + e.getMessage());
            return false;
        }
    }

    // Helper method to parse the products string
    private Map<String, Integer> parseProductsString(String productsStr) {
        Map<String, Integer> result = new HashMap<>();
        
        // Split by comma and space to get individual product entries
        String[] productEntries = productsStr.split(", ");
        
        for (String entry : productEntries) {
            // Extract product name and quantity
            int openParenIndex = entry.lastIndexOf(" (");
            int closeParenIndex = entry.lastIndexOf(")");
            
            if (openParenIndex > 0 && closeParenIndex > openParenIndex) {
                String productName = entry.substring(0, openParenIndex);
                String qtyStr = entry.substring(openParenIndex + 2, closeParenIndex);
                
                try {
                    int quantity = Integer.parseInt(qtyStr);
                    result.put(productName, quantity);
                } catch (NumberFormatException e) {
                    // Skip invalid quantity format
                    System.err.println("Invalid quantity format in: " + entry);
                }
            }
        }
        
        return result;
    }

    // Fixed method with consistent signature
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Additional overloaded method to handle the AlertType parameter
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    void handleNavigation(ActionEvent event) {
        Object source = event.getSource();
        
        try {
            String fxmlFile = "";
            
            if (source == profileBtn) {
                fxmlFile = "Profile.fxml";
            } else if (source == cashierBtn) {
                fxmlFile = "Cashier.fxml";
            } else if (source == bundleproductsBtn) {
                fxmlFile = "BundleProducts.fxml";
            } else if (source == usersBtn) {
                fxmlFile = "UserManagement.fxml";
            } else if (source == productsBtn) {
                fxmlFile = "ProductManagement.fxml";
            } else if (source == adminLogBtn) {
                fxmlFile = "AuthenticationLog.fxml";
            } else if (source == refundproductsBtn) {
                return;
            }
            
            if (!fxmlFile.isEmpty()) {
                URL url = getClass().getResource(fxmlFile);
                
                if (url == null) {
                    // Try alternative path format if the first attempt fails
                    String altPath = fxmlFile.replace("/com/example/uts_pbo/", "/");
                    url = getClass().getResource(altPath);
                    
                    if (url == null) {
                        // Try one more alternative - without leading slash
                        String noSlashPath = fxmlFile.substring(1);
                        url = getClass().getClassLoader().getResource(noSlashPath);
                        
                        if (url == null) {
                            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                                "Could not find FXML file: " + fxmlFile + 
                                "\nPlease check if the file exists in the resources folder.");
                            return;
                        }
                    }
                }
                
                Parent root = FXMLLoader.load(url);
                Stage stage = (Stage) ((Button) source).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            }
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                    "Could not navigate to the requested page: " + e.getMessage());
            e.printStackTrace();
        }
    }
}