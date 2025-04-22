package Admin_View;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import com.example.uts_pbo.DatabaseConnection;
import com.example.uts_pbo.NavigationAuthorizer;
import com.example.uts_pbo.UserSession;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ProductManagementController implements Initializable {

    // Navigation buttons
    @FXML private Button profileBtn;
    @FXML private Button cashierBtn;
    @FXML private Button productsBtn;
    @FXML private Button bundleproductsBtn;
    @FXML private Button usersBtn;
    @FXML private Button adminLogBtn;
    @FXML private Button refundproductsBtn;
    
    // Product table and columns
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> codeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> qtyColumn;
    @FXML private TableColumn<Product, String> expDateColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, String> typeColumn;
    
    // Form fields
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField qtyField;
    @FXML private TextField expDateField;
    @FXML private ComboBox<String> productTypeComboBox;
    @FXML private ComboBox<String> categoryComboBox;
    
    // Digital product fields
    @FXML private VBox perishableProductFields;
    @FXML private VBox digitalProductFields;
    @FXML private TextField urlField;
    @FXML private TextField vendorField;
    
    // Action buttons
    @FXML private Button saveProductBtn;
    @FXML private Button deleteProductBtn;
    @FXML private Button updateProductBtn;

    private ObservableList<String> productTypes = FXCollections.observableArrayList(
            "Perishable Product", "Non-Perishable Product", "Digital Product"
    );
    
    private ObservableList<String> categories = FXCollections.observableArrayList(
        "Groceries", "Electronics", "Clothing", "Home Goods", "Beauty", 
        "Beverages", "Snacks", "Toys", "Office Supplies", "Others"
    );

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        qtyColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        expDateColumn.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        
        // Setup for the type column
        typeColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            String type = "Unknown";
            if (product instanceof PerishableProduct) {
                type = "Perishable";
            } else if (product instanceof NonPerishableProduct) {
                type = "Non-Perishable";
            } else if (product instanceof DigitalProduct) {
                type = "Digital";
            }
            return new SimpleStringProperty(type);
        });
        
        loadProductsFromDatabase();
        
        productTypeComboBox.setItems(productTypes);
        categoryComboBox.setItems(categories);
        
        // Add listener to show/hide fields based on selected product type
        productTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateFieldVisibility(newVal);
        });
        
        // Selection listener for the product table
        productTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    populateForm(newSelection);
                }
            }
        );
        
        // Check admin access on initialization
        Platform.runLater(() -> {
            if (!UserSession.isAdmin()) {
                // redirect non-admins to PROFILE
                NavigationAuthorizer.navigateTo(
                  profileBtn,
                  "/Admin_View/Profile.fxml",
                  NavigationAuthorizer.USER_VIEW
                );
                showAlert(Alert.AlertType.WARNING,
                          "Access Denied",
                          "Admin access required.");
            }
        });
    }
    
    private void updateFieldVisibility(String productType) {
        if (productType == null) return;
        
        boolean isPerishable = "Perishable Product".equals(productType);
        boolean isDigital = "Digital Product".equals(productType);
        
        // Show/hide perishable product fields
        perishableProductFields.setVisible(isPerishable);
        perishableProductFields.setManaged(isPerishable);
        
        // Show/hide digital product fields
        digitalProductFields.setVisible(isDigital);
        digitalProductFields.setManaged(isDigital);
    }
    
    private void loadProductsFromDatabase() {
        ObservableList<Product> productList = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT code, product_name, price, qty, exp_date, category, " +
                           "url, vendor_name, product_type FROM product";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                String code = rs.getString("code");
                String name = rs.getString("product_name");
                double price = rs.getDouble("price");
                int qty = rs.getInt("qty");
                String category = rs.getString("category");
                String productType = rs.getString("product_type");
                
                Product product;
                
                if ("Perishable Product".equals(productType)) {
                    Date sqlDate = rs.getDate("exp_date");
                    LocalDate expiryDate = null;
                    if (sqlDate != null) {
                        expiryDate = sqlDate.toLocalDate();
                    }
                    product = new PerishableProduct(code, name, price, qty, expiryDate, category);
                } 
                else if ("Digital Product".equals(productType)) {
                    String urlStr = rs.getString("url");
                    String vendorName = rs.getString("vendor_name");
                    URL productUrl = null;
                    try {
                        if (urlStr != null && !urlStr.isEmpty()) {
                            productUrl = new URL(urlStr);
                        }
                    } catch (Exception e) {
                        System.err.println("Invalid URL: " + urlStr);
                    }
                    product = new DigitalProduct(code, name, price, qty, category, productUrl, vendorName);
                } 
                else {
                    // Default to NonPerishableProduct
                    product = new NonPerishableProduct(code, name, price, qty, category);
                }
                
                productList.add(product);
            }
            
            productTable.setItems(productList);
            Product.getAllProducts().setAll(productList);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load products from database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void populateForm(Product product) {
        codeField.setText(product.getCode());
        nameField.setText(product.getName());
        priceField.setText(String.valueOf(product.getPrice()));
        qtyField.setText(String.valueOf(product.getQuantity()));
        
        // Set the category in the ComboBox
        if (product.getCategory() != null && !product.getCategory().isEmpty()) {
            // If the category exists in our list, select it
            if (categories.contains(product.getCategory())) {
                categoryComboBox.setValue(product.getCategory());
            } else {
                // If it doesn't exist, add it
                categories.add(product.getCategory());
                categoryComboBox.setValue(product.getCategory());
            }
        } else {
            categoryComboBox.setValue("Others"); // Default if no category is set
        }
        
        // Determine the product type and set the appropriate field values
        if (product instanceof PerishableProduct) {
            productTypeComboBox.setValue("Perishable Product");
            PerishableProduct perishable = (PerishableProduct) product;
            if (perishable.getExpiryDate() != null) {
                expDateField.setText(perishable.getExpiryDate().format(dateFormatter));
            } else {
                expDateField.setText("");
            }
        } 
        else if (product instanceof DigitalProduct) {
            productTypeComboBox.setValue("Digital Product");
            DigitalProduct digital = (DigitalProduct) product;
            if (digital.getUrl() != null) {
                urlField.setText(digital.getUrl().toString());
            } else {
                urlField.setText("");
            }
            vendorField.setText(digital.getVendorName());
        } 
        else {
            productTypeComboBox.setValue("Non-Perishable Product");
            expDateField.setText("");
        }
        
        // Update field visibility based on the selected product type
        updateFieldVisibility(productTypeComboBox.getValue());
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
            } else if (source == refundproductsBtn) {
                fxmlFile = "RefundProducts.fxml";
            } else if (source == usersBtn) {
                fxmlFile = "UserManagement.fxml";
            } else if (source == adminLogBtn) {
                fxmlFile = "AuthenticationLog.fxml";
            } else if (source == productsBtn) {
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

    @FXML
    private void handleSaveProduct() {
        try {
            if (codeField.getText().isEmpty() || nameField.getText().isEmpty() || 
                priceField.getText().isEmpty() || qtyField.getText().isEmpty() ||
                productTypeComboBox.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please fill all required fields");
                return;
            }
            
            String code = codeField.getText();
            String name = nameField.getText();
            double price = Double.parseDouble(priceField.getText());
            int qty;
            try {
                qty = Integer.parseInt(qtyField.getText());
                if (qty < 0) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "Quantity cannot be negative");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Quantity must be a valid number");
                return;
            }

            String productType = productTypeComboBox.getValue();
            String category = categoryComboBox.getValue();
            if (category == null || category.isEmpty()) {
                category = "Others"; // Default if none selected
            }
            
            // Process specific fields based on product type
            java.sql.Date sqlDate = null;
            URL productUrl = null;
            String vendorName = null;
            
            if ("Perishable Product".equals(productType)) {
                String expDateStr = expDateField.getText();
                if (expDateStr != null && !expDateStr.trim().isEmpty()) {
                    try {
                        java.util.Date parsedDate = dateFormat.parse(expDateStr);
                        sqlDate = new java.sql.Date(parsedDate.getTime());
                    } catch (ParseException e) {
                        showAlert(Alert.AlertType.ERROR, "Date Format Error", 
                                  "Please enter the expiration date in dd-MM-yyyy format or leave it empty");
                        return;
                    }
                }
            } else if ("Digital Product".equals(productType)) {
                String urlStr = urlField.getText();
                if (urlStr != null && !urlStr.trim().isEmpty()) {
                    try {
                        productUrl = new URL(urlStr);
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "URL Format Error", 
                                  "Please enter a valid URL");
                        return;
                    }
                }
                vendorName = vendorField.getText();
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Check if product exists
                String checkQuery = "SELECT code FROM product WHERE code = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, code);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    // Update existing product
                    String updateQuery = "UPDATE Product SET product_name = ?, price = ?, qty = ?, exp_date = ?, " +
                                         "category = ?, product_type = ?, url = ?, vendor_name = ? WHERE code = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setString(1, name);
                    updateStmt.setDouble(2, price);
                    updateStmt.setInt(3, qty);
                    
                    if (sqlDate != null) {
                        updateStmt.setDate(4, sqlDate);
                    } else {
                        updateStmt.setNull(4, Types.DATE);
                    }
                    
                    updateStmt.setString(5, category);
                    updateStmt.setString(6, productType);
                    
                    if (productUrl != null) {
                        updateStmt.setString(7, productUrl.toString());
                    } else {
                        updateStmt.setNull(7, Types.VARCHAR);
                    }
                    
                    if (vendorName != null && !vendorName.isEmpty()) {
                        updateStmt.setString(8, vendorName);
                    } else {
                        updateStmt.setNull(8, Types.VARCHAR);
                    }
                    
                    updateStmt.setString(9, code);
                    updateStmt.executeUpdate();
                
                    ProductModificationLogController.logProductAction(
                        UserSession.getUserId(),
                        UserSession.getUsername(),
                        code,
                        name,
                        "UPDATE"
                    );

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully");
                } else {
                    // Insert new product
                    String insertQuery = "INSERT INTO Product (code, product_name, price, qty, exp_date, " +
                                        "category, product_type, url, vendor_name) " +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                    insertStmt.setString(1, code);
                    insertStmt.setString(2, name);
                    insertStmt.setDouble(3, price);
                    insertStmt.setInt(4, qty);
                    
                    // Set date as NULL if empty
                    if (sqlDate != null) {
                        insertStmt.setDate(5, sqlDate);
                    } else {
                        insertStmt.setNull(5, Types.DATE);
                    }
                    
                    insertStmt.setString(6, category);
                    insertStmt.setString(7, productType);
                    
                    if (productUrl != null) {
                        insertStmt.setString(8, productUrl.toString());
                    } else {
                        insertStmt.setNull(8, Types.VARCHAR);
                    }
                    
                    if (vendorName != null && !vendorName.isEmpty()) {
                        insertStmt.setString(9, vendorName);
                    } else {
                        insertStmt.setNull(9, Types.VARCHAR);
                    }
                    
                    insertStmt.executeUpdate();

                    ProductModificationLogController.logProductAction(
                        UserSession.getUserId(),
                        UserSession.getUsername(),
                        code,
                        name,
                        "ADD"
                    );
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully");
                }
                
                // Reload products from database
                loadProductsFromDatabase();
                clearForm();
                
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not save product: " + e.getMessage());
            }
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid number format for price or quantity");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteProduct() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to delete");
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Product");
        confirmDialog.setContentText("Are you sure you want to delete " + selectedProduct.getName() + "?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    
                    ProductModificationLogController.logProductAction(
                        UserSession.getUserId(),
                        UserSession.getUsername(),
                        selectedProduct.getCode(),
                        selectedProduct.getName(),
                        "DELETE"
                    );
                    
                    String deleteQuery = "DELETE FROM product WHERE code = ?";
                    PreparedStatement pst = conn.prepareStatement(deleteQuery);
                    pst.setString(1, selectedProduct.getCode());
                    pst.executeUpdate();
                    
                    loadProductsFromDatabase();
                    clearForm();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully");
                    
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Could not delete product: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    @FXML
    private void addNewCategory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Category");
        dialog.setHeaderText("Add New Category");
        dialog.setContentText("Please enter the new category name:");

        dialog.showAndWait().ifPresent(newCategory -> {
            if (!newCategory.isEmpty() && !categories.contains(newCategory)) {
                categories.add(newCategory);
                categoryComboBox.setValue(newCategory);
            }
        });
    }
    
    private void clearForm() {
        codeField.clear();
        nameField.clear();
        priceField.clear();
        qtyField.clear();
        expDateField.clear();
        productTypeComboBox.setValue(null);
        categoryComboBox.setValue(null);
        
        if (urlField != null) urlField.clear();
        if (vendorField != null) vendorField.clear();
        
        productTable.getSelectionModel().clearSelection();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}