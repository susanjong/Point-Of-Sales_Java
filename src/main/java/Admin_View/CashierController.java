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
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CashierController implements Initializable {

    @FXML private Button profileBtn;
    @FXML private Button cashierBtn;
    @FXML private Button productsBtn;
    @FXML private Button usersBtn;
    @FXML private Button adminLogBtn;
    
    @FXML private TextField searchField;
    @FXML private Button addToCartBtn;
    
    @FXML private TextField paidField;
    @FXML private Label balanceLabel;
    
    @FXML private VBox cartItemsContainer;
    @FXML private Label totalAmountLabel;
    
    private double totalAmount = 0.0;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cartItems.add(new CartItem("Chitato snack cheese flavour", 11000.0, 3));
        cartItems.add(new CartItem("Bango Kecap Manis", 11500.0, 2));
        currencyFormat.setMaximumFractionDigits(0);
        
        paidField.textProperty().addListener((observable, oldValue, newValue) -> {
            calculateBalance();
        });
        refreshCartView();
        updateTotalAmount();
        updateTotalDisplay();
    }
    
    private void refreshCartView() {
        if (cartItemsContainer != null) {
            cartItemsContainer.getChildren().clear();
            
            for (CartItem item : cartItems) {
                HBox itemView = createCartItemView(item);
                cartItemsContainer.getChildren().add(itemView);
            }
        }
    }
    
    private HBox createCartItemView(CartItem item) {
        HBox itemRow = new HBox(10);
        itemRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox productInfo = new VBox();
        HBox.setHgrow(productInfo, javafx.scene.layout.Priority.ALWAYS);
        
        Label nameLabel = new Label(item.getName());
        nameLabel.setWrapText(true);
        
        Label priceLabel = new Label(formatPrice(item.getPrice()));
        priceLabel.setStyle("-fx-font-weight: bold;");
        
        productInfo.getChildren().addAll(nameLabel, priceLabel);
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #9A030F; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
        deleteBtn.setPrefWidth(70.0);
        deleteBtn.setOnAction(e -> {
            handleRemoveFromCart(item);
        });
        
        HBox quantityControls = new HBox(5);
        quantityControls.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button minusBtn = new Button("-");
        minusBtn.setPrefWidth(25.0);
        minusBtn.setOnAction(e -> {
            handleDecreaseQuantity(item);
        });
        
        Label quantityLabel = new Label(String.valueOf(item.getQuantity()));
        quantityLabel.setStyle("-fx-font-weight: bold;");
        
        Button plusBtn = new Button("+");
        plusBtn.setPrefWidth(25.0);
        plusBtn.setOnAction(e -> {
            handleIncreaseQuantity(item);
        });
        
        quantityControls.getChildren().addAll(minusBtn, quantityLabel, plusBtn);
        
        itemRow.getChildren().addAll(productInfo, deleteBtn, quantityControls);
        
        return itemRow;
    }
    
    private void calculateBalance() {
        try {
            String paidText = paidField.getText().trim();
            if (paidText.isEmpty()) {
                balanceLabel.setText("");
                return;
            }
            paidText = paidText.replaceAll("[^\\d]", "");
            
            double paidAmount = Double.parseDouble(paidText);
            double balance = paidAmount - totalAmount;
            
            balanceLabel.setText(formatPrice(balance));
        } catch (NumberFormatException e) {
            balanceLabel.setText("Invalid amount");
        }
    }
    
    @FXML
    void handleAddToCart(ActionEvent event) {
        String code = searchField.getText().trim();
        if (!code.isEmpty()) {
            CartItem newItem = new CartItem("Product from code " + code, 10000.0, 1);
            for (CartItem item : cartItems) {
                if (item.getName().equals(newItem.getName())) {
                    item.setQuantity(item.getQuantity() + 1);
                    refreshCartView();
                    updateTotalAmount();
                    searchField.clear();
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product quantity increased!");
                    return;
                }
            }
            cartItems.add(newItem);
            refreshCartView();
            updateTotalAmount();
            searchField.clear();
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product added to cart!");
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter a product code.");
        }
    }
    
    @FXML
    void handleAddBundleToCart(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        VBox bundleBox = (VBox) clickedButton.getParent();
        String bundleName = "";
        double bundlePrice = 20400.0;
        
        for (javafx.scene.Node node : bundleBox.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if (label.getStyle().contains("-fx-font-weight: bold")) {
                    String priceText = label.getText().replace("Rp ", "").replace(".", "");
                    try {
                        bundlePrice = Double.parseDouble(priceText);
                    } catch (NumberFormatException e) {
                    }
                } else if (!label.getText().isEmpty() && !label.getText().startsWith("Rp")) {
                    bundleName = label.getText();
                }
            }
        }
        
        if (bundleName.isEmpty()) {
            bundleName = "Bundling Chitato snack cheese flavour, Bango Kecap Manis";
        }
        
        boolean bundleFound = false;
        for (CartItem item : cartItems) {
            if (item.getName().equals(bundleName)) {
                item.setQuantity(item.getQuantity() + 1);
                bundleFound = true;
                break;
            }
        }
        
        if (!bundleFound) {
            CartItem bundleItem = new CartItem(bundleName, bundlePrice, 1);
            cartItems.add(bundleItem);
        }
        
        refreshCartView();
        updateTotalAmount();
        
        showAlert(Alert.AlertType.INFORMATION, "Success", "Bundle added to cart!");
    }
    
    @FXML
    void handleAddProductToCart(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        VBox productBox = (VBox) clickedButton.getParent();
        String productName = "";
        double productPrice = 0; 
        
        for (javafx.scene.Node node : productBox.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if (label.getStyle().contains("-fx-font-weight: bold")) {
                    String priceText = label.getText().replace("Rp ", "").replace(".", "");
                    try {
                        productPrice = Double.parseDouble(priceText);
                    } catch (NumberFormatException e) {
                    }
                } else if (!label.getText().isEmpty() && !label.getText().startsWith("Rp")) {
                    productName = label.getText();
                }
            }
        }
        
        if (productName.isEmpty()) {
            productName = "Chitato snack cheese flavour";
        }
        
        boolean productFound = false;
        for (CartItem item : cartItems) {
            if (item.getName().equals(productName)) {
                item.setQuantity(item.getQuantity() + 1);
                productFound = true;
                break;
            }
        }
        
        if (!productFound) {
            CartItem productItem = new CartItem(productName, productPrice, 1);
            cartItems.add(productItem);
        }
        
        refreshCartView();
        updateTotalAmount();
        
        showAlert(Alert.AlertType.INFORMATION, "Success", "Product added to cart!");
    }
    
    private void handleRemoveFromCart(CartItem itemToRemove) {
        cartItems.remove(itemToRemove);
        refreshCartView();
        updateTotalAmount();
    }
    
    @FXML
    void handleRemoveFromCart(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        HBox parentRow = (HBox) clickedButton.getParent();
        
        int index = cartItemsContainer.getChildren().indexOf(parentRow);
        if (index >= 0 && index < cartItems.size()) {
            cartItems.remove(index);
            refreshCartView();
            updateTotalAmount();
        }
    }
    
    private void handleIncreaseQuantity(CartItem item) {
        item.setQuantity(item.getQuantity() + 1);
        refreshCartView();
        updateTotalAmount();
    }
    
    @FXML
    void handleIncreaseQuantity(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        HBox quantityBox = (HBox) clickedButton.getParent();
        HBox parentRow = (HBox) quantityBox.getParent();
        
        int index = cartItemsContainer.getChildren().indexOf(parentRow);
        if (index >= 0 && index < cartItems.size()) {
            CartItem item = cartItems.get(index);
            item.setQuantity(item.getQuantity() + 1);
            refreshCartView();
            updateTotalAmount();
        }
    }
    
    private void handleDecreaseQuantity(CartItem item) {
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            refreshCartView();
            updateTotalAmount();
        }
    }
    
    @FXML
    void handleDecreaseQuantity(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        HBox quantityBox = (HBox) clickedButton.getParent();
        HBox parentRow = (HBox) quantityBox.getParent();
        
        int index = cartItemsContainer.getChildren().indexOf(parentRow);
        if (index >= 0 && index < cartItems.size()) {
            CartItem item = cartItems.get(index);
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                refreshCartView();
                updateTotalAmount();
            }
        }
    }
    
    @FXML
    void handlePayment(ActionEvent event) {
        String paidText = paidField.getText().trim();
        if (paidText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter the paid amount.");
            return;
        }
        
        try {
            paidText = paidText.replaceAll("[^\\d]", "");
            double paidAmount = Double.parseDouble(paidText);
            
            if (paidAmount < totalAmount) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Paid amount is less than the total.");
                return;
            }
            
            double change = paidAmount - totalAmount;
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Payment Successful");
            alert.setHeaderText(null);
            alert.setContentText("Payment received!\nChange: " + formatPrice(change));
            alert.showAndWait();
            cartItems.clear();
            totalAmount = 0.0;
            paidField.clear();
            balanceLabel.setText("");
            refreshCartView();
            updateTotalDisplay();
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid payment amount.");
        }
    }
    
    private void updateTotalAmount() {
        totalAmount = 0;
        for (CartItem item : cartItems) {
            double itemSubtotal = item.getPrice() * item.getQuantity();
            totalAmount += itemSubtotal;
        }
        updateTotalDisplay();
    }
    
    private void updateTotalDisplay() {
        if (totalAmountLabel != null) {
            totalAmountLabel.setText(formatPrice(totalAmount));
        }
    }
    
    private String formatPrice(double price) {
        return currencyFormat.format(price).replace("Rp", "Rp ");
    }
    
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
            } else if (source == productsBtn) {
                fxmlFile = "ProductManagement.fxml";
            } else if (source == usersBtn) {
                fxmlFile = "UserManagement.fxml";
            } else if (source == adminLogBtn) {
                fxmlFile = "AdminLog.fxml";
            } else if (source == cashierBtn) {
                return;
            }
            
            if (!fxmlFile.isEmpty()) {
                Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
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
    
    public static class CartItem {
        private String name;
        private double price;
        private int quantity;
        
        public CartItem(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
        
        public String getName() {
            return name;
        }
        
        public double getPrice() {
            return price;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
        
        public double getTotal() {
            return price * quantity;
        }
    }
}
