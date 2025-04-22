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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CashierController implements Initializable{

    @FXML private Button profileBtn;
    @FXML private Button cashierBtn;
    @FXML private Button productsBtn;
    @FXML private Button bundleproductsBtn;
    @FXML private Button usersBtn;
    @FXML private Button adminLogBtn;
    @FXML private Button refundproductsBtn;
    @FXML private FlowPane productsContainer;
    @FXML private FlowPane bundleProductsContainer;
    @FXML private TextField searchField;
    @FXML private Button addToCartBtn;
    @FXML private Label productNameLabel;
    @FXML private Label priceLabel;
    
    @FXML private TextField paidField;
    @FXML private Label balanceLabel;
    
    @FXML private VBox cartItemsContainer;
    @FXML private Label totalAmountLabel;
    
    private Map<String, BundleProduct> bundleProductMap = new HashMap<>(); 
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

        TransactionLogger.initialize();
        
        // Set up currency format
        currencyFormat.setMaximumFractionDigits(0);
        
        // Set up listeners
        paidField.textProperty().addListener((observable, oldValue, newValue) -> {
            calculateBalance();
        });
        
        // Load initial data
        try {
            loadProductsFromDatabase();
            loadBundleProducts();
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

    private void loadBundleProducts() {
        System.out.println("Loading bundle products...");
        // Clear the existing map and container
        bundleProductMap.clear();
        if (bundleProductsContainer != null) {
            bundleProductsContainer.getChildren().clear();
        } else {
            System.err.println("WARNING: bundleProductsContainer is null!");
            try {
                bundleProductsContainer = (FlowPane) cashierBtn.getScene().lookup("#bundleProductsContainer");
            } catch (Exception e) {
                System.err.println("Failed to find bundleProductsContainer: " + e.getMessage());
            }
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // First, get all unique bundle codes and their prices
            String bundleQuery = "SELECT DISTINCT code, price FROM bundle_products";
            try (PreparedStatement stmt = conn.prepareStatement(bundleQuery);
                 ResultSet rs = stmt.executeQuery()) {
                
                int bundleCount = 0;
                while (rs.next()) {
                    bundleCount++;
                    String bundleCode = rs.getString("code");
                    double bundlePrice = rs.getDouble("price");
                    
                    // Create a new bundle object to store info
                    BundleProduct bundle = new BundleProduct(bundleCode, bundlePrice);
                    bundle.setPreserveOriginalPrice(true); // Add this flag to prevent price recalculation
                    bundleProductMap.put(bundleCode, bundle);
                    
                    // Now get all products in this bundle
                    loadBundleItems(conn, bundle);
                    
                    // Create and add the bundle UI element
                    VBox bundleBox = createBundleProductBox(bundle);
                    
                    if (bundleProductsContainer != null) {
                        bundleProductsContainer.getChildren().add(bundleBox);
                    } else {
                        System.err.println("Cannot add bundle to UI, container is null");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL error loading bundles: " + e.getMessage());
        }
    }

    
    private void loadBundleItems(Connection conn, BundleProduct bundle) throws SQLException {
        String itemsQuery = "SELECT bp.product_code, bp.qty, p.product_name, p.price, p.qty, " +
                            "p.exp_date, p.category " +
                            "FROM bundle_products bp " +
                            "JOIN product p ON bp.product_code = p.code " +
                            "WHERE bp.code = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(itemsQuery)) {
            stmt.setString(1, bundle.getCode());
            try (ResultSet rs = stmt.executeQuery()) {
                String bundleName = getBundleNameFromDatabase(conn, bundle.getCode());
                StringBuilder fullBundleName = new StringBuilder(bundleName + ": ");
                List<String> productNames = new ArrayList<>();
                
                while (rs.next()) {
                    String productCode = rs.getString("product_code");
                    int qty = rs.getInt("qty");
                    String productName = rs.getString("product_name");
                    double productPrice = rs.getDouble("price");
                    int productQty = rs.getInt("qty");
                    String expirationDate = rs.getString("exp_date");
                    String category = rs.getString("category");
                    
                    // Create a Product object first
                    Product product = new Product(
                        productCode, productName, productPrice, 
                        productQty, expirationDate, category
                    );
                    
                    // Now add the Product object to the bundle with the quantity
                    bundle.addProduct(product, qty);
                    
                    productNames.add(productName + " (" + qty + ")");
                }
                
                if (!productNames.isEmpty()) {
                    fullBundleName.append(String.join(", ", productNames));
                    bundle.setName(fullBundleName.toString());
                } else {
                    // If no products found, just use the name from database
                    bundle.setName(bundleName);
                }
            }
        }
    }

    private String getBundleNameFromDatabase(Connection conn, String bundleCode) {
        String defaultName = "Bundle";  // Default fallback name
        
        try {
            String query = "SELECT bundle_name FROM bundle_products WHERE code = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, bundleCode);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("bundle_name");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting bundle name: " + e.getMessage());
        }
        
        return defaultName;
    }

    /**
 * Creates a UI box for displaying a bundle product that matches your design
 */
    private VBox createBundleProductBox(BundleProduct bundle) {
        // Create the main VBox container with the specified styling
        VBox bundleBox = new VBox();
        bundleBox.setStyle("-fx-border-color: #CCCCCC; -fx-border-radius: 8; -fx-padding: 10.0;");
        bundleBox.setSpacing(5);
        
        // Create bundle name label with wrapping text
        Label nameLabel = new Label(bundle.getName());
        nameLabel.setMaxWidth(180.0);
        nameLabel.setMinWidth(180.0);
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-size: 14px;");
        
        // Display the bundle price using getDiscountedPrice method
        Label priceLabel = new Label(formatPrice(bundle.getDiscountedPrice()));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Use the getSavingsAmount and getSavingsPercentage methods
        double savings = bundle.getSavingsAmount();
        double savingsPercentage = bundle.getSavingsPercentage();
        
        Label savingsLabel = new Label(String.format("Save %s (%.0f%% off)", 
                              formatPrice(savings), savingsPercentage));
        savingsLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");
        
        // Create add to cart button with green styling
        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.setStyle("-fx-background-color: #5B8336; -fx-text-fill: white;");
        addToCartBtn.setUserData(bundle); // Store BundleProduct object with the button
        addToCartBtn.setOnAction(e -> addBundleToCart(bundle));
        
        // Add all elements to the VBox
        bundleBox.getChildren().addAll(nameLabel, priceLabel, savingsLabel, addToCartBtn);
        
        return bundleBox;
    }

    private void addBundleToCart(BundleProduct bundle) {
        boolean bundleFound = false;
        
        // Check if bundle already exists in cart
        for (CartItem item : cartItems) {
            if (item.getName().equals(bundle.getName())) {
                item.setQuantity(item.getQuantity() + 1);
                bundleFound = true;
                break;
            }
        }
        
        // Add new bundle to cart if not found
        if (!bundleFound) {
            CartItem bundleItem = new CartItem(bundle.getName(), bundle.getPrice(), 1);
            cartItems.add(bundleItem);
        }
        
        refreshCartView();
        updateTotalAmount();
        
        showAlert(Alert.AlertType.INFORMATION, "Success", "Bundle added to cart!");
    }

    /**
     * Original method that extracts bundle info from UI elements
     */
    private void addBundleToCartFromUI(Button clickedButton) {
        VBox bundleBox = (VBox) clickedButton.getParent();
        String bundleName = "";
        double bundlePrice = 0;
        
        // Extract information from the UI elements
        for (javafx.scene.Node node : bundleBox.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if (label.getStyle().contains("-fx-font-weight: bold")) {
                    String priceText = label.getText().replace("Rp ", "").replace(".", "");
                    try {
                        bundlePrice = Double.parseDouble(priceText);
                    } catch (NumberFormatException e) {
                        // Keep default price if parsing fails
                    }
                } else if (!label.getText().isEmpty() && !label.getText().startsWith("Rp")) {
                    bundleName = label.getText();
                }
            }
        }
        
        // Check if bundle already exists in cart
        boolean bundleFound = false;
        for (CartItem item : cartItems) {
            if (item.getName().equals(bundleName)) {
                item.setQuantity(item.getQuantity() + 1);
                bundleFound = true;
                break;
            }
        }
        
        // Add new bundle to cart if not found
        if (!bundleFound) {
            CartItem bundleItem = new CartItem(bundleName, bundlePrice, 1);
            cartItems.add(bundleItem);
        }
        
        refreshCartView();
        updateTotalAmount();
        
        showAlert(Alert.AlertType.INFORMATION, "Success", "Bundle added to cart!");
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
                Date sqlDate = rs.getDate("exp_date");
                if (sqlDate != null) {
                    expDate = dateFormat.format(sqlDate);
                }
                
                String category = rs.getString("category");
                
                // Create product object with all fields from database
                Product product = new Product(code, name, price, qty, expDate, category);
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
        
        // Rest of your method remains the same...
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
        
        productBox.getChildren().addAll(nameLabel, priceLabel, addButton);
        
        return productBox;
    }
    
    // Helper method to extract filename from path
    private String getFileName(String path) {
        if (path == null) return "";
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }
    
    private void handleAddToCartFromDisplay(Product product) {
        // Check available stock in the database
        int availableQty = 0;
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT qty FROM product WHERE code = ?";
            pst = conn.prepareStatement(query);
            pst.setString(1, product.getCode());
            rs = pst.executeQuery();
            
            if (rs.next()) {
                availableQty = rs.getInt("qty");
            }
        } catch (SQLException e) {
            System.err.println("Error checking stock: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        boolean productFound = false;
        for (CartItem item : cartItems) {
            if (item.getName().equals(product.getName())) {
                // Check if current quantity + 1 exceeds available
                if (item.getQuantity() + 1 <= availableQty) {
                    item.setQuantity(item.getQuantity() + 1);
                    item.setMaxQuantity(availableQty); // Update max quantity in case it changed
                    productFound = true;
                    refreshCartView();
                    updateTotalAmount();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product added to cart!");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Stock Limit", 
                        "Cannot add more. Maximum available stock is " + availableQty);
                }
                return;
            }
        }
        
        if (!productFound && availableQty > 0) {
            CartItem productItem = new CartItem(product.getName(), product.getPrice(), 1, availableQty);
            cartItems.add(productItem);
            refreshCartView();
            updateTotalAmount();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Product added to cart!");
        } else if (!productFound) {
            showAlert(Alert.AlertType.WARNING, "Out of Stock", "Sorry, this product is out of stock.");
        }
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
        nameLabel.setStyle("-fx-font-size: 12px; -fx-pref-width: 300px; -fx-pref-height: 37px;");
        
        Label priceLabel = new Label(formatPrice(item.getPrice()));
        priceLabel.setStyle("-fx-font-weight: bold;");
        
        productInfo.getChildren().addAll(nameLabel, priceLabel);
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #9A030F; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
        deleteBtn.setPrefWidth(100.0);
        deleteBtn.setOnAction(e -> {
            handleRemoveFromCart(item);
        });
        
        HBox quantityControls = new HBox(5);
        quantityControls.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button minusBtn = new Button("-");
        minusBtn.setPrefWidth(15.0);
        minusBtn.setOnAction(e -> {
            handleDecreaseQuantity(item);
        });
        
        Label quantityLabel = new Label(String.valueOf(item.getQuantity()));
        quantityLabel.setMinWidth(30.0);
        quantityLabel.setPrefWidth(30.0);
        quantityLabel.setAlignment(javafx.geometry.Pos.CENTER);
        quantityLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0 5;");
        
        Button plusBtn = new Button("+");
        plusBtn.setPrefWidth(15.0);
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
                
                String query = "SELECT code, product_name, price, qty, exp_date, category FROM product WHERE code = ?";
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
                    
                    // Create product object with all fields from database
                    currentProduct = new Product(productCode, name, price, qty, expDate, category);
                    
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
        int availableQty = 0;
        
        // Get available quantity from database again to ensure it's current
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT qty FROM product WHERE code = ?";
            pst = conn.prepareStatement(query);
            pst.setString(1, currentProduct.getCode());
            rs = pst.executeQuery();
            
            if (rs.next()) {
                availableQty = rs.getInt("qty");
            }
        } catch (SQLException e) {
            System.err.println("Error checking stock: " + e.getMessage());
            availableQty = 0;  // Be conservative if we can't check
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        // Check if product already exists in cart
        for (CartItem item : cartItems) {
            if (item.getName().equals(productName)) {
                // Check if current quantity + cart quantity exceeds available
                if (item.getQuantity() + 1 <= availableQty) {
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
                } else {
                    showAlert(Alert.AlertType.WARNING, "Stock Limit", 
                        "Cannot add more. Maximum available stock is " + availableQty);
                }
                return;
            }
        }
        
        // Add new product to cart if stock is available
        if (availableQty > 0) {
            CartItem newItem = new CartItem(productName, productPrice, 1, availableQty);
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
            showAlert(Alert.AlertType.WARNING, "Out of Stock", "Sorry, this product is out of stock.");
        }
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
        // Re-check available stock in database for latest quantity
        int availableQty = 0;
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        // Find the product code first
        String productCode = "";
        for (Product product : products) {
            if (product.getName().equals(item.getName())) {
                productCode = product.getCode();
                break;
            }
        }
    
        if (!productCode.isEmpty()) {
            try {
                conn = DatabaseConnection.getConnection();
                String query = "SELECT qty FROM product WHERE code = ?";
                pst = conn.prepareStatement(query);
                pst.setString(1, productCode);
                rs = pst.executeQuery();
                
                if (rs.next()) {
                    availableQty = rs.getInt("qty");
                }
            } catch (SQLException e) {
                System.err.println("Error checking stock: " + e.getMessage());
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
        
        // Update the maximum quantity and check if we can increase
        item.setMaxQuantity(availableQty);
        if (item.getQuantity() < item.getMaxQuantity()) {
            item.setQuantity(item.getQuantity() + 1);
            refreshCartView();
            updateTotalAmount();
        } else {
            showAlert(Alert.AlertType.WARNING, "Stock Limit", 
                "Cannot add more. Maximum available stock is " + item.getMaxQuantity());
        }
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

    private PurchaseTransaction currentTransaction;
    
    
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
        
        String username = "guest"; // Default value
        User currentUser = LoginController.getCurrentUser();
        if (currentUser != null) {
            username = currentUser.getUsername();
        }
        
        // Create PurchaseTransaction instance
        PurchaseTransaction transaction = new PurchaseTransaction(
            new java.sql.Date(System.currentTimeMillis()),
            0, // transactionId will be set during serialization
            cartItems,
            username
        );
        
        // Process transaction using PurchaseTransaction methods
        String transactionData = transaction.serializeTransaction();
        
        try {
            transaction.processTransaction();
            
            // Log the successful transaction
            int itemCount = getTotalItemCount();
            String paymentMethod = "Cash"; // You may need to add payment method selection to your UI
            
            // Log the transaction
            TransactionLogger.logSale(transaction.getTransactionId(), transaction.getTotalAmount(), 
                                    itemCount, paymentMethod);
            
            double change = paidAmount - transaction.getTotalAmount();
            
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
        } catch (Exception e) {
            // Log failed transaction
            int itemCount = getTotalItemCount();
            String paymentMethod = "Cash";
            TransactionLogger.logCanceledSale(transaction.getTransactionId(), transaction.getTotalAmount(), 
                                            itemCount, paymentMethod);
                                            
            showAlert(Alert.AlertType.ERROR, "Error", "Transaction processing failed: " + e.getMessage());
        }
        
    } catch (NumberFormatException e) {
        showAlert(Alert.AlertType.ERROR, "Error", "Invalid payment amount.");
    }
}

// Helper method to count total items in cart
private int getTotalItemCount() {
    int count = 0;
    for (CartItem item : cartItems) {
        count += item.getQuantity();
    }
    return count;
}

    private void updateTotalAmount() {
        totalAmount = 0.0;
        for (CartItem item : cartItems) {
        totalAmount += item.getPrice() * item.getQuantity();
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
            } else if (source == bundleproductsBtn) {
                fxmlFile = "BundleProducts.fxml";
            } else if (source == refundproductsBtn) {
                fxmlFile = "RefundProducts.fxml";
            } else if (source == usersBtn) {
                fxmlFile = "UserManagement.fxml";
            } else if (source == adminLogBtn) {
                fxmlFile = "AuthenticationLog.fxml";
            } else if (source == cashierBtn) {
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


    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    
    public static class CartItem {
        private String code;
        private String name;
        private double price;
        private int quantity;
        private int maxQuantity;

        public CartItem(String name, double price, int quantity) {
            this.code = "";
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.maxQuantity = Integer.MAX_VALUE; 
        }

        public CartItem(String name, double price, int quantity, int maxQuantity) {
            this.code = "";
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.maxQuantity = Integer.MAX_VALUE; 
        }
        
        public String getCode() {
            return code;
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
            if (quantity <= maxQuantity) {
                this.quantity = quantity;
            }
        }

        public int getMaxQuantity() {
            return maxQuantity;
        }
        
        public void setMaxQuantity(int maxQuantity) {
            this.maxQuantity = maxQuantity;
        }
        
        public double getTotal() {
            return price * quantity;
        }
    }
}