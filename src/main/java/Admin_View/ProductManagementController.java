package Admin_View;

import com.example.uts_pbo.DatabaseConnection;
import com.example.uts_pbo.Main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class ProductManagementController implements Initializable {

    // Navigation buttons
    @FXML private Button profileBtn;
    @FXML private Button cashierBtn;
    @FXML private Button productsBtn;
    @FXML private Button usersBtn;
    @FXML private Button adminLogBtn;
    
    // Product table and columns
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> codeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> qtyColumn;
    @FXML private TableColumn<Product, String> expDateColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    
    // Form fields
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField qtyField;
    @FXML private TextField expDateField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ImageView productImage;
    
    // Action buttons
    @FXML private Button saveProductBtn;
    @FXML private Button deleteProductBtn;
    @FXML private Button updateProductBtn;
    @FXML private Button addCategoryBtn;
    @FXML private Button addImageBtn;
    
    private String currentImagePath = "images";
    private ObservableList<String> categories = FXCollections.observableArrayList(
            "Groceries", "Electronics", "Clothing", "Home Goods", "Beauty", "Beverages", "Snacks"
    );

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        qtyColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        expDateColumn.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        
        loadProductsFromDatabase();
        
        categoryComboBox.setItems(categories);
        
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });
        
        resetProductImage();
    }
    
    private void loadProductsFromDatabase() {
        ObservableList<Product> productList = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT code, product_name, price, qty, exp_date, category, image_path FROM product";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                String code = rs.getString("code");
                String name = rs.getString("product_name");
                double price = rs.getDouble("price");
                int qty = rs.getInt("qty");
                //Handle data properly can be null
                String expDate = null;
                    Date sqlDate = rs.getDate("exp_date");
                    if (sqlDate != null) {
                        expDate = dateFormat.format(sqlDate);
                    }
                String category = rs.getString("category");
                String imagePath = rs.getString("image_path");
                
                Product product = new Product(code, name, price, qty, expDate, category, imagePath);
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
        
        // Handle null expiration date
        if (product.getExpirationDate() != null) {
            expDateField.setText(product.getExpirationDate());
        } else {
            expDateField.setText("");
        }

        categoryComboBox.setValue(product.getCategory());
        
        if (product.getImagePath() != null) {
            try {
                Image image;
                String imagePath = product.getImagePath();
                
                if (imagePath.startsWith("/resources/")) {
                    image = new Image(getClass().getResourceAsStream(imagePath));
                } else {
                    image = new Image(new File(imagePath).toURI().toString());
                }
                
                productImage.setImage(image);
                currentImagePath = product.getImagePath();
            } catch (Exception e) {
                resetProductImage();
            }
        } else {
            resetProductImage();
        }
    }
    
    private void resetProductImage() {
        try {
            Image placeholder = new Image(getClass().getResourceAsStream("/resources/no-image.png"));
            productImage.setImage(placeholder);
        } catch (Exception e) {
            productImage.setImage(null);
        }
        currentImagePath = null;
    }

    @FXML
    public void initialize() {
        System.out.println("Controller initialized!");
        System.out.println("profileBtn: " + (profileBtn != null ? "found" : "NOT FOUND"));
        System.out.println("cashierBtn: " + (cashierBtn != null ? "found" : "NOT FOUND"));
        
    }
    
    @FXML
    void handleNavigation(ActionEvent event) {
    Object source = event.getSource();
    
    String fxmlFile = "";
    
    if (source == profileBtn) {
        fxmlFile = "Profile.fxml";
    } else if (source == cashierBtn) {
        fxmlFile = "Cashier.fxml";
    } else if (source == usersBtn) {
        fxmlFile = "UserManagement.fxml";
    } else if (source == adminLogBtn) {
        fxmlFile = "AdminLog.fxml";
    } else if (source == productsBtn) {
        fxmlFile = "ProductManagement.fxml";
    }
    
    if (!fxmlFile.isEmpty()) {
        try {
            Main.navigateTo(fxmlFile);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                    "Could not navigate to the requested page: " + e.getMessage());
        }
    }
}

    
    @FXML
    private void handleSaveProduct() {
        try {
            if (codeField.getText().isEmpty() || nameField.getText().isEmpty() || 
                priceField.getText().isEmpty() || qtyField.getText().isEmpty()) {
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

            String expDateStr = expDateField.getText();
            String category = categoryComboBox.getValue();
            
            // Handle empty exp_date field - set to null if empty
            java.sql.Date sqlDate = null;
            if (expDateStr != null && !expDateStr.trim().isEmpty()) {
                try {
                    java.util.Date parsedDate = dateFormat.parse(expDateStr);
                    sqlDate = new java.sql.Date(parsedDate.getTime());
                } catch (ParseException e) {
                    showAlert(Alert.AlertType.ERROR, "Date Format Error", 
                              "Please enter the expiration date in YYYY-MM-DD format or leave it empty");
                    return;
                }
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Check if product exists
                String checkQuery = "SELECT code FROM product WHERE code = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, code);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    // Update existing product
                    String updateQuery = "UPDATE Product SET product_name = ?, price = ?, qty = ?, exp_date = ?, category = ?, image_path = ? WHERE code = ?";
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
                    updateStmt.setString(6, currentImagePath);
                    updateStmt.setString(7, code);
                    updateStmt.executeUpdate();
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully");
                } else {
                    // Insert new product
                    String insertQuery = "INSERT INTO Product (code, product_name, price, qty, exp_date, category, image_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
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
                    insertStmt.setString(7, currentImagePath);
                    insertStmt.executeUpdate();
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully");
                }
                
                // Reload products from database
                loadProductsFromDatabase();
                clearForm();
                
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not save product: " + e.getMessage());
                e.printStackTrace();
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
    private void handleAddCategory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Category");
        dialog.setHeaderText("Add New Category");
        dialog.setContentText("Please enter the new category name:");
        
        dialog.showAndWait().ifPresent(categoryName -> {
            if (!categoryName.isEmpty() && !categories.contains(categoryName)) {
                categories.add(categoryName);
                categoryComboBox.setValue(categoryName);
            }
        });
    }
    
    private void clearForm() {
        codeField.clear();
        nameField.clear();
        priceField.clear();
        qtyField.clear();
        expDateField.clear();
        categoryComboBox.setValue(null);
        resetProductImage();
        productTable.getSelectionModel().clearSelection();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleAddImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(productImage.getScene().getWindow());
        
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                productImage.setImage(image);
                currentImagePath = selectedFile.getAbsolutePath();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Image Error", "Could not load the selected image");
                resetProductImage();
            }
        }
    }
}
