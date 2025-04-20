package Admin_View;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

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
    @FXML private TableColumn<BundleProduct, String> bundlecodeColumn;
    @FXML private TableColumn<BundleProduct, String> nameColumn;
    @FXML private TableColumn<BundleProduct, String> codeColumn;
    @FXML private TableColumn<BundleProduct, Double> priceColumn;
    @FXML private TableColumn<BundleProduct, Integer> qtyColumn;

    // Form fields
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField productcodeField;
    @FXML private TextField priceField;
    @FXML private TextField qtyField;

    // Buttons
    @FXML private Button saveProductBtn;
    @FXML private Button deleteProductBtn;

    // Observable list to hold the bundle products
    private ObservableList<BundleProduct> bundleProductList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns
        bundlecodeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        qtyColumn.setCellValueFactory(new PropertyValueFactory<>("qty"));

        // Set up table selection listener
        bundleproductTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Fill form fields with selected product data
                codeField.setText(newSelection.getCode());
                nameField.setText(newSelection.getName());
                priceField.setText(String.valueOf(newSelection.getPrice()));
                qtyField.setText(String.valueOf(newSelection.getQty()));
                productcodeField.setText(newSelection.getProductCode());
            }
        });
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
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveProduct() {
        try {
            // Validate input fields
            if (validateFields()) {
                String code = codeField.getText();
                String name = nameField.getText();
                String productCode = productcodeField.getText();
                double price = Double.parseDouble(priceField.getText());
                int qty = Integer.parseInt(qtyField.getText());
                
                // Check if editing existing product or adding new one
                boolean isNewProduct = true;
                for (BundleProduct product : bundleProductList) {
                    if (product.getCode().equals(code)) {
                        // Update existing product
                        product.setName(name);
                        product.setProductCode(productCode);
                        product.setPrice(price);
                        product.setQty(qty);
                        isNewProduct = false;
                        break;
                    }
                }
                
                if (isNewProduct) {
                    // Create new product and add to list
                    BundleProduct newProduct = new BundleProduct(
                        code, name, productCode, price, qty, 
                        LocalDate.now().plusMonths(3), // Default expiry date
                        "Bundle" // Default category
                    );
                    bundleProductList.add(newProduct);
                }
                
                // Refresh table
                bundleproductTable.refresh();
                
                // Clear form fields
                clearFields();
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Bundle product saved successfully!");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid numeric values for price and quantity.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Save Error", "An error occurred while saving the bundle product.");
            e.printStackTrace();
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
                bundleProductList.remove(selectedProduct);
                clearFields();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Bundle product deleted successfully!");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a bundle product to delete.");
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
        
        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errorMessage.toString());
            return false;
        }
        
        return true;
    }

    private void clearFields() {
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

    // Model class for Bundle Product
    public static class BundleProduct {
        private String code;
        private String name;
        private String productCode;
        private double price;
        private int qty;
        private LocalDate expDate;
        private String category;

        public BundleProduct(String code, String name, String productCode, double price, int qty, LocalDate expDate, String category) {
            this.code = code;
            this.name = name;
            this.productCode = productCode;
            this.price = price;
            this.qty = qty;
            this.expDate = expDate;
            this.category = category;
        }

        // Getters and setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getProductCode() { return productCode; }
        public void setProductCode(String productCode) { this.productCode = productCode; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public int getQty() { return qty; }
        public void setQty(int qty) { this.qty = qty; }

        public LocalDate getExpDate() { return expDate; }
        public void setExpDate(LocalDate expDate) { this.expDate = expDate; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
}