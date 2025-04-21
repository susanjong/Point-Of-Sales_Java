package Admin_View;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

import com.example.uts_pbo.DatabaseConnection;

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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class BundleProductsController implements Initializable {

    // Navigation buttons
    @FXML private Button profileBtn;
    @FXML private Button cashierBtn;
    @FXML private Button productsBtn;
    @FXML private Button bundleproductsBtn;
    @FXML private Button usersBtn;
    @FXML private Button adminLogBtn;

    // Table and columns
    @FXML private TableView<BundleProduct> bundleproductTable;
    @FXML private TableColumn<BundleProduct, Integer> bundleidColumn;
    @FXML private TableColumn<BundleProduct, String> bundlecodeColumn;
    @FXML private TableColumn<BundleProduct, String> nameColumn;
    @FXML private TableColumn<BundleProduct, String> codeColumn;
    @FXML private TableColumn<BundleProduct, Double> priceColumn;
    @FXML private TableColumn<BundleProduct, Integer> qtyColumn;

    // Form fields
    @FXML private TextField idField;
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField productcodeField;
    @FXML private TextField priceField;
    @FXML private TextField qtyField;

    private ObservableList<BundleProduct> bundleProductList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns
        bundleidColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        bundlecodeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        qtyColumn.setCellValueFactory(new PropertyValueFactory<>("qty"));

        // Load data from database
        loadBundleProducts();

        // Set table items
        bundleproductTable.setItems(bundleProductList);

        // Set up table selection listener
        bundleproductTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                System.out.println("Selected bundle product: " + newSelection.getId() + " - " + newSelection.getName());
                
                // Fill form fields with selected product data
                idField.setText(String.valueOf(newSelection.getId()));
                codeField.setText(newSelection.getCode());
                nameField.setText(newSelection.getName());
                productcodeField.setText(newSelection.getProductCode());
                priceField.setText(String.valueOf(newSelection.getPrice()));
                qtyField.setText(String.valueOf(newSelection.getQty()));
            }
        });
    }

    // Load bundle products from Supabase
    private void loadBundleProducts() {
        String query = "SELECT * FROM bundle_products";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            bundleProductList.clear();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String code = rs.getString("code");
                String bundle_name = rs.getString("bundle_name");
                String productCode = rs.getString("product_code");
                double price = rs.getDouble("price");
                int qty = rs.getInt("qty");
                
                // Create new BundleProduct object and add to list
                BundleProduct product = new BundleProduct(
                    id,
                    code,
                    bundle_name,
                    productCode,
                    price,
                    qty,
                    LocalDate.now().plusMonths(3), // Default expiry date
                    "Bundle" // Default category
                );
                bundleProductList.add(product);
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Failed to load bundle products: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleNavigation(ActionEvent event) {
        Object source = event.getSource();
        
        try {
            String fxmlFile = "";
            
            if (source == profileBtn) {
                fxmlFile = "Profile.fxml";
            } else if (source == productsBtn) {
                fxmlFile = "ProductManagement.fxml";
            } else if (source == cashierBtn) {
                fxmlFile = "cashier.fxml";
            } else if (source == usersBtn) {
                fxmlFile = "UserManagement.fxml";
            } else if (source == adminLogBtn) {
                fxmlFile = "AuthenticationLog.fxml";
            } else if (source == bundleproductsBtn) {
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
        }
    }

    @FXML
    private void handleSaveProduct() {
    try {
        // Validate input fields
        if (validateFields()) {
            String code = codeField.getText().trim();
            String bundle_name = nameField.getText().trim();
            String productCode = productcodeField.getText().trim();
            
            // Parse numeric values with error handling
            double price;
            int qty;
            
            try {
                price = Double.parseDouble(priceField.getText().trim());
                qty = Integer.parseInt(qtyField.getText().trim());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input Error", 
                    "Please enter valid numeric values for price and quantity.");
                return;
            }
            
            // Check if this is an update or a new record
            String idText = idField != null ? idField.getText().trim() : "";

            if (idText.isEmpty()) {
                System.out.println("Inserting new bundle product...");
                // Insert new record
                insertBundleProduct(code, bundle_name, productCode, price, qty);
            } else {
                try {
                    int id = Integer.parseInt(idText);
                    System.out.println("Updating bundle product with ID: " + id);
                    // Update existing record
                    updateBundleProduct(id, code, bundle_name, productCode, price, qty);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid ID format.");
                    return;
                }
            }
            
            // Refresh data from database
            loadBundleProducts();
            
            // Clear form fields
            clearFields();
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Bundle product saved successfully!");
        }
    } catch (Exception e) {
        showAlert(Alert.AlertType.ERROR, "Save Error", 
            "An error occurred while saving the bundle product: " + e.getMessage());
    }
}

    // Insert a new bundle product into the database
    private void insertBundleProduct(String code, String name, String productCode, double price, int qty) {
        String sql = "INSERT INTO bundle_products (code, bundle_name, product_code, price, qty) VALUES (?, ?, ?, ?, ?) RETURNING id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, code);
            pstmt.setString(2, name);
            pstmt.setString(3, productCode);
            pstmt.setDouble(4, price);
            pstmt.setInt(5, qty);
            
            // Execute and get the generated ID
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int generatedId = rs.getInt("id");
                    System.out.println("New bundle product ID: " + generatedId);
                } else {
                    throw new SQLException("Creating bundle product failed, no ID returned.");
                }
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Failed to insert bundle product: " + e.getMessage());
        }
    }

    // Update an existing bundle product in the database
    private void updateBundleProduct(int id, String code, String name, String productCode, double price, int qty) {
        String sql = "UPDATE bundle_products SET code = ?, bundle_name = ?, product_code = ?, price = ?, qty = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, code);
            pstmt.setString(2, name);
            pstmt.setString(3, productCode);
            pstmt.setDouble(4, price);
            pstmt.setInt(5, qty);
            pstmt.setInt(6, id);
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Failed to update bundle product: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteProduct() {
        BundleProduct selectedProduct = bundleproductTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Delete");
            confirmAlert.setHeaderText("Delete Bundle Product");
            confirmAlert.setContentText("Are you sure you want to delete the selected bundle product?");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Delete from database
                deleteBundleProduct(selectedProduct.getId());
                
                // Refresh data
                loadBundleProducts();
                
                // Clear form fields
                clearFields();
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Bundle product deleted successfully!");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a bundle product to delete.");
        }
    }

    // Delete a bundle product from the database
    private void deleteBundleProduct(int id) {
        String sql = "DELETE FROM bundle_products WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Failed to delete bundle product: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        StringBuilder errorMessage = new StringBuilder();
        
        if (codeField.getText().trim().isEmpty()) {
            errorMessage.append("Code cannot be empty.\n");
        }
        if (nameField.getText().trim().isEmpty()) {
            errorMessage.append("Bundle name cannot be empty.\n");
        }
        if (productcodeField.getText().trim().isEmpty()) {
            errorMessage.append("Product code cannot be empty.\n");
        }
        if (priceField.getText().trim().isEmpty()) {
            errorMessage.append("Price cannot be empty.\n");
        } else {
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                if (price < 0) {
                    errorMessage.append("Price cannot be negative.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Price must be a valid number.\n");
            }
        }
        if (qtyField.getText().trim().isEmpty()) {
            errorMessage.append("Quantity cannot be empty.\n");
        } else {
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (qty < 0) {
                    errorMessage.append("Quantity cannot be negative.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Quantity must be a valid integer.\n");
            }
        }
        
        // Only validate ID if it's not empty (for updates)
        if (!idField.getText().trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                if (id < 0) {
                    errorMessage.append("ID cannot be negative.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("ID must be a valid integer.\n");
            }
        }
        
        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errorMessage.toString());
            return false;
        }
        
        return true;
    }

    private void clearFields() {
        idField.clear();
        codeField.clear();
        nameField.clear();
        productcodeField.clear();
        priceField.clear();
        qtyField.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}