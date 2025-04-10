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
import java.util.Optional;
import java.util.ResourceBundle;

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
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        qtyColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        expDateColumn.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        
        productTable.setItems(Product.getAllProducts());
        
        categoryComboBox.setItems(categories);
        
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });
        
        resetProductImage();
    }
    
    private void populateForm(Product product) {
        codeField.setText(product.getCode());
        nameField.setText(product.getName());
        priceField.setText(String.valueOf(product.getPrice()));
        qtyField.setText(String.valueOf(product.getQuantity()));
        expDateField.setText(product.getExpirationDate());
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
    private void handleNavigation(ActionEvent event) {
        Button source = (Button) event.getSource();
        System.out.println("Navigated to: " + source.getText());
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
            int qty = Integer.parseInt(qtyField.getText());
            String expDate = expDateField.getText();
            String category = categoryComboBox.getValue();
            
            Optional<Product> existingProduct = Product.getAllProducts().stream()
                    .filter(p -> p.getCode().equals(code))
                    .findFirst();
            
            if (existingProduct.isPresent()) {
                Product product = existingProduct.get();
                product.setName(name);
                product.setPrice(price);
                product.setQuantity(qty);
                product.setExpirationDate(expDate);
                product.setCategory(category);
                product.setImagePath(currentImagePath);
                
                productTable.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product updated successfully");
            } else {
                Product newProduct = new Product(code, name, price, qty, expDate, category, currentImagePath);
                Product.addProduct(newProduct);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product added successfully");
            }
            
            clearForm();
            
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
                Product.removeProduct(selectedProduct);
                clearForm();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully");
            }
        });
    }
    
    @FXML
    private void handleUpdateProduct() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to update");
            return;
        }
        
        populateForm(selectedProduct);
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