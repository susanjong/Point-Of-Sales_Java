package Admin_View;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;

import com.example.uts_pbo.DatabaseConnection;
import com.example.uts_pbo.LoginController;
import com.example.uts_pbo.User;

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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CashierController implements Initializable, Payable {

    @FXML private Button profileBtn;
    @FXML private Button cashierBtn;
    @FXML private Button productsBtn;
    @FXML private Button usersBtn;
    @FXML private Button adminLogBtn;
    @FXML private FlowPane productsContainer;
    @FXML private TextField searchField;
    @FXML private Button addToCartBtn;
    @FXML private Label productNameLabel;
    @FXML private Label priceLabel;
    
    @FXML private TextField paidField;
    @FXML private Label balanceLabel;
    
    @FXML private VBox cartItemsContainer;
    @FXML private Label totalAmountLabel;
    
    private double totalAmount = 0.0;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy");
    private Product currentProduct = null;
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private final ArrayList<Product> products = new ArrayList<>();
    private int transactionId;
    private boolean transactionSaved = false;
    private boolean processingComplete = false;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing CashierController...");
        
        // Set up currency format
        currencyFormat.setMaximumFractionDigits(0);
        
        // Set up listeners
        paidField.textProperty().addListener((observable, oldValue, newValue) -> {
            calculateBalance();
        });
        
        // Load initial data
        try {
            loadProductsFromDatabase();
            System.out.println("Loaded " + products.size() + " products from database");
            displayProducts();
            System.out.println("Displayed products in UI");
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Initialization Error", "Error loading products: " + e.getMessage());
        }
        
        // Initialize UI components
        refreshCartView();
        updateTotalAmount();
        updateTotalDisplay();
        setupSearchFieldListener();

        if (productNameLabel == null || priceLabel == null) {
            // Get the containers where your labels should be displayed
            VBox productNameVBox = null;
            VBox priceVBox = null;
            
            // Find the containers in the FXML hierarchy
            for (javafx.scene.Node node : searchField.getParent().getParent().getChildrenUnmodifiable()) {
                if (node instanceof HBox) {
                    HBox hbox = (HBox) node;
                    for (javafx.scene.Node child : hbox.getChildren()) {
                        if (child instanceof VBox) {
                            VBox vbox = (VBox) child;
                            for (javafx.scene.Node vboxChild : vbox.getChildren()) {
                                if (vboxChild instanceof Label) {
                                    Label label = (Label) vboxChild;
                                    if (label.getText().equals("PRODUCT NAME")) {
                                        productNameVBox = vbox;
                                    } else if (label.getText().equals("PRICE")) {
                                        priceVBox = vbox;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (productNameVBox != null && priceVBox != null) {
                // Create and add productNameLabel
                productNameLabel = new Label("");
                productNameLabel.setPrefHeight(30.0);
                productNameLabel.setPrefWidth(300.0);
                productNameLabel.setStyle("-fx-background-color: #E0E0E0; -fx-background-radius: 5; -fx-padding: 5px;");
                
                // Find the Region and replace it with our label
                for (int i = 0; i < productNameVBox.getChildren().size(); i++) {
                    if (productNameVBox.getChildren().get(i) instanceof javafx.scene.layout.Region) {
                        productNameVBox.getChildren().set(i, productNameLabel);
                        break;
                    }
                }
                
                // Create and add priceLabel
                priceLabel = new Label("");
                priceLabel.setPrefHeight(30.0);
                priceLabel.setPrefWidth(200.0);
                priceLabel.setStyle("-fx-background-color: #E0E0E0; -fx-background-radius: 5; -fx-padding: 5px;");
                
                // Find the Region and replace it with our label
                for (int i = 0; i < priceVBox.getChildren().size(); i++) {
                    if (priceVBox.getChildren().get(i) instanceof javafx.scene.layout.Region) {
                        priceVBox.getChildren().set(i, priceLabel);
                        break;
                    }
                }
            }
        }
        addToCartBtn.setDisable(true);
    }
    
    private void loadProductsFromDatabase() {
        products.clear();
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection is null");
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to database");
                return;
            }
            
            String query = "SELECT code, product_name, price, qty, exp_date, category, image_path FROM product";
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();
            
            while (rs.next()) {
                String code = rs.getString("code");
                String name = rs.getString("product_name");
                double price = rs.getDouble("price");
                int qty = rs.getInt("qty");
                
                // Handle exp_date which can be null
                String expDate = null;
                Date sqlDate = rs.getDate("exp_date");
                if (sqlDate != null) {
                    expDate = dateFormat.format(sqlDate);
                }
                
                String category = rs.getString("category");
                String imagePath = rs.getString("image_path");
                
                System.out.println("Loaded product: " + name + " with image: " + imagePath);
                
                // Create product object with all fields from database
                Product product = new Product(code, name, price, qty, expDate, category, imagePath);
                products.add(product);
            }
            
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Could not load products from database: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    private void displayProducts() {
        if (productsContainer != null) {
            productsContainer.getChildren().clear();
            
            if (products.isEmpty()) {
                Label noProductsLabel = new Label("No products available");
                noProductsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
                productsContainer.getChildren().add(noProductsLabel);
            } else {
                for (Product product : products) {
                    try {
                        VBox productBox = createProductBox(product);
                        productsContainer.getChildren().add(productBox);
                    } catch (Exception e) {
                        System.err.println("Error creating product box for " + product.getName() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.err.println("productsContainer is null");
        }
    }

    private VBox createProductBox(Product product) {
        VBox productBox = new VBox(5);
        productBox.setStyle("-fx-border-color: #CCCCCC; -fx-border-radius: 8; -fx-padding: 10.0;");
        
        ImageView imageView = new ImageView();
        imageView.setFitHeight(120.0);
        imageView.setFitWidth(120.0);
        imageView.setPreserveRatio(true);
        
        try {
            // Try loading the image from the path
            String imagePath = product.getImagePath();
            Image image;
            
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    // First try as a direct file path
                    image = new Image("file:" + imagePath);
                    imageView.setImage(image);
                } catch (Exception e) {
                    // If that fails, try as a resource path
                    try {
                        image = new Image(getClass().getResourceAsStream(imagePath));
                        imageView.setImage(image);
                    } catch (Exception ex) {
                        System.err.println("Could not load image as resource: " + ex.getMessage());
                        // Use placeholder
                        imageView.setImage(new Image(getClass().getResourceAsStream("/resources/images/placeholder.png")));
                    }
                }
            } else {
                // Use placeholder for null or empty path
                imageView.setImage(new Image(getClass().getResourceAsStream("/resources/images/placeholder.png")));
            }
        } catch (Exception e) {
            System.err.println("Error loading image for " + product.getName() + ": " + e.getMessage());
            try {
                // Try to use placeholder image
                imageView.setImage(new Image(getClass().getResourceAsStream("/resources/images/placeholder.png")));
            } catch (Exception ex) {
                System.err.println("Error loading placeholder image: " + ex.getMessage());
            }
        }
        
        Label nameLabel = new Label(product.getName());
        nameLabel.setMaxWidth(150.0);
        nameLabel.setMinWidth(150.0);
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-size: 14px;");
        
        Label priceLabel = new Label(formatPrice(product.getPrice()));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Button addButton = new Button("Add to Cart");
        addButton.setStyle("-fx-background-color: #5B8336; -fx-text-fill: white; -fx-font-size: 12px;");
        addButton.setOnAction(e -> handleAddToCartFromDisplay(product));
        
        productBox.getChildren().addAll(imageView, nameLabel, priceLabel, addButton);
        
        return productBox;
    }
    
    private void handleAddToCartFromDisplay(Product product) {
        boolean productFound = false;
        for (CartItem item : cartItems) {
            if (item.getName().equals(product.getName())) {
                item.setQuantity(item.getQuantity() + 1);
                productFound = true;
                break;
            }
        }
        
        if (!productFound) {
            CartItem productItem = new CartItem(product.getName(), product.getPrice(), 1);
            cartItems.add(productItem);
        }
        
        refreshCartView();
        updateTotalAmount();
        
        showAlert(Alert.AlertType.INFORMATION, "Success", "Product added to cart!");
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
    private void searchProductByCode() {
        String code = searchField.getText().trim();
        if (!code.isEmpty()) {
            Connection conn = null;
            PreparedStatement pst = null;
            ResultSet rs = null;
            
            try {
                conn = DatabaseConnection.getConnection();
                if (conn == null) {
                    System.err.println("Database connection is null");
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to database");
                    return;
                }
                
                String query = "SELECT code, product_name, price, qty, exp_date, category, image_path FROM product WHERE code = ?";
                pst = conn.prepareStatement(query);
                pst.setString(1, code);
                rs = pst.executeQuery();
                
                if (rs.next()) {
                    String productCode = rs.getString("code");
                    String name = rs.getString("product_name");
                    double price = rs.getDouble("price");
                    int qty = rs.getInt("qty");
                    
                    // Handle exp_date which can be null
                    String expDate = null;
                    Date sqlDate = rs.getDate("exp_date");
                    if (sqlDate != null) {
                        expDate = dateFormat.format(sqlDate);
                    }
                    
                    String category = rs.getString("category");
                    String imagePath = rs.getString("image_path");
                    
                    // Create product object with all fields from database
                    currentProduct = new Product(productCode, name, price, qty, expDate, category, imagePath);
                    
                    // Display product info
                    productNameLabel.setText(name);
                    priceLabel.setText(formatPrice(price));
                    addToCartBtn.setDisable(qty <= 0);
                    
                    System.out.println("Found product: " + name + " with price: " + price);
                } else {
                    // Reset if product not found
                    currentProduct = null;
                    productNameLabel.setText("Product not found");
                    priceLabel.setText("");
                    addToCartBtn.setDisable(true);
                    
                    showAlert(Alert.AlertType.WARNING, "Not Found", "Product with code " + code + " not found.");
                }
                
            } catch (SQLException e) {
                System.err.println("SQL Error: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", 
                        "Could not search product: " + e.getMessage());
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (pst != null) pst.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing resources: " + e.getMessage());
                }
            }
        } else {
            currentProduct = null;
            productNameLabel.setText("");
            priceLabel.setText("");
            addToCartBtn.setDisable(true);
        }
    }

    @FXML
    void handleAddToCart(ActionEvent event) {
        if (currentProduct != null) {
            String productName = currentProduct.getName();
            double productPrice = currentProduct.getPrice();
            
            // Check if product already exists in cart
            for (CartItem item : cartItems) {
                if (item.getName().equals(productName)) {
                    item.setQuantity(item.getQuantity() + 1);
                    refreshCartView();
                    updateTotalAmount();
                    searchField.clear();
                    
                    // Reset product info display
                    currentProduct = null;
                    productNameLabel.setText("");
                    priceLabel.setText("");
                    addToCartBtn.setDisable(true);
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product quantity increased!");
                    return;
                }
            }
            
            // Add new product to cart
            CartItem newItem = new CartItem(productName, productPrice, 1);
            cartItems.add(newItem);
            refreshCartView();
            updateTotalAmount();
            searchField.clear();
            
            // Reset product info display
            currentProduct = null;
            productNameLabel.setText("");
            priceLabel.setText("");
            addToCartBtn.setDisable(true);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product added to cart!");
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please search for a valid product first.");
        }
    }

    /**
     * Add event listener for search field
     * Add this to your initialize method
     */
    private void setupSearchFieldListener() {
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                searchProductByCode();
            }
        });
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
        
        // Check if cart is empty
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Cart is empty. Please add products first.");
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
            
            // Process the transaction using both interface methods
             // Just prints a message to console
            serializeTransaction(); // This handles the actual database insertion
            processTransaction(); 

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Payment Successful");
            alert.setHeaderText(null);
            alert.setContentText("Payment received!\nChange: " + formatPrice(change));
            alert.showAndWait();
            
            // Reset UI after successful transaction
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
    
    @Override
    public double calculateTotal() {
        double total = 0.0;
        for (CartItem item : cartItems) {
            double itemSubtotal = item.getPrice() * item.getQuantity();
            total += itemSubtotal;
        }
        return total;
    }

    @Override
    public void processTransaction() {
        // Jika transaksi belum disimpan, simpan dulu
        if (!transactionSaved) {
            serializeTransaction();
        }

        processingComplete = true;
        
        // Gunakan transaction ID yang sudah dihasilkan
        System.out.println("Transaksi dengan ID " + transactionId + " telah diproses");

        transactionId = 0;
        transactionSaved = false;
        processingComplete = false;
    }

    @Override
    public String serializeTransaction() {
        if (transactionSaved) {
            StringBuilder transactionData = new StringBuilder();
            transactionData.append("Transaction ID: ").append(transactionId).append("\n");
            return transactionData.toString();
        }

        transactionId = 0;
        processingComplete = false;
        transactionSaved = false;

        StringBuilder transactionData = new StringBuilder();
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection is null");
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to database");
                return "Error: Database connection failed";
            }
            
            // Get current date and time
            java.util.Date currentDate = new java.util.Date();
            java.sql.Timestamp timestamp = new java.sql.Timestamp(currentDate.getTime());
            
            // Count total items
            int totalItems = 0;
            for (CartItem item : cartItems) {
                totalItems += item.getQuantity();
            }
            
            // Create products string (list of products in cart)
            StringBuilder productsStr = new StringBuilder();
            for (CartItem item : cartItems) {
                productsStr.append(item.getName())
                        .append(" (")
                        .append(item.getQuantity())
                        .append("), ");
            }
            // Remove trailing comma and space
            if (productsStr.length() > 2) {
                productsStr.setLength(productsStr.length() - 2);
            }

            String username = "guest"; // Default value
        
            // Get the current user from LoginController
            User currentUser = LoginController.getCurrentUser();
            if (currentUser != null) {
                username = currentUser.getUsername();
            }
            
            // Insert into transaksi table dengan RETURNING clause untuk mendapatkan ID
            String query = "INSERT INTO transaction (date, username, products, total_item, total_price) VALUES (?, ?, ?, ?, ?) RETURNING transaction_id";
            pst = conn.prepareStatement(query);
            pst.setTimestamp(1, timestamp);
            pst.setString(2, username);
            pst.setString(3, productsStr.toString());
            pst.setInt(4, totalItems);
            pst.setDouble(5, totalAmount);
            
            // Execute dan ambil ID yang dibuat Supabase
            rs = pst.executeQuery();
            
            if (rs.next()) {
                transactionId = rs.getInt(1);
            } else {
                // Fallback jika tidak bisa mendapatkan ID
                transactionId = new Random().nextInt(100000000);
            }
            
            System.out.println("Transaction saved to database with ID: " + transactionId);
            
            // Build transaction summary for return value
            transactionData.append("Transaction ID: ").append(transactionId).append("\n");
            transactionData.append("Date: ").append(new java.util.Date()).append("\n");
            transactionData.append("Total Amount: ").append(formatPrice(totalAmount));
            transactionData.append("User: ").append(username).append("\n");
            transactionSaved = true;
            
        } catch (SQLException e) {
            System.err.println("SQL Error while serializing transaction: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Could not save transaction data: " + e.getMessage());
            
            // Fallback jika error
            if (transactionId == 0) {
                transactionId = new Random().nextInt(100000000);
            }
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        // Return the string representation of the transaction summary
        return transactionData.toString();
    }

    private void updateTotalAmount() {
        totalAmount = calculateTotal();
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
                fxmlFile = "AuthenticationLog.fxml";
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

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
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
