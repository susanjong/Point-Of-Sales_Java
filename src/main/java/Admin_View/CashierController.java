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

import User_dashboard.DatabaseConnection;
import User_dashboard.LoginController;
import User_dashboard.User;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
    @FXML private ScrollPane productsScrollPane;
    @FXML private ScrollPane bundleScrollPane;
    @FXML private ScrollPane cartScrollPane;

    private Map<String, BundleProduct> bundleProductMap = new HashMap<>();
    private double totalAmount = 0.0;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private Product currentProduct = null;
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private final ArrayList<Product> products = new ArrayList<>();
    private int transactionId;
    private boolean transactionSaved = false;
    private boolean processingComplete = false;
    private boolean isInitialized = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing CashierController...");

        // Set up currency format
        currencyFormat.setMaximumFractionDigits(0);

        // Configure ScrollPanes for better responsiveness
        configureScrollPanes();

        // Configure FlowPanes for responsive layout
        configureFlowPanes();

        // Set up listeners
        paidField.textProperty().addListener((observable, oldValue, newValue) -> {
            calculateBalance();
        });

        // Maximize stage FIRST, then load content
        Platform.runLater(() -> {
            maximizeStage();

            // Load content AFTER stage is maximized
            Platform.runLater(() -> {
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
            });
        });

        // Initialize UI components
        clearCart();
        refreshCartView();
        updateTotalAmount();
        updateTotalDisplay();
        setupSearchFieldListener();

        // Disable add to cart button initially
        if (addToCartBtn != null) {
            addToCartBtn.setDisable(true);
        }

        System.out.println("Components initialized");
    }

    /**
     * Configure ScrollPanes for better responsiveness
     */
    private void configureScrollPanes() {
        // Configure products scroll pane
        if (productsScrollPane != null) {
            productsScrollPane.setFitToWidth(true);
            productsScrollPane.setFitToHeight(false);
            productsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            productsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        }

        // Configure bundle scroll pane
        if (bundleScrollPane != null) {
            bundleScrollPane.setFitToWidth(true);
            bundleScrollPane.setFitToHeight(false);
            bundleScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            bundleScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        }

        // Configure cart scroll pane
        if (cartScrollPane != null) {
            cartScrollPane.setFitToWidth(true);
            cartScrollPane.setFitToHeight(true);
            cartScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            cartScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        }
    }

    /**
     * Configure FlowPanes for responsive layout
     */
    private void configureFlowPanes() {
        if (productsContainer != null) {
            productsContainer.setHgap(10);
            productsContainer.setVgap(10);
            productsContainer.setPadding(new Insets(10));
            productsContainer.prefWrapLengthProperty().bind(
                    productsContainer.widthProperty()
            );
        }

        if (bundleProductsContainer != null) {
            bundleProductsContainer.setHgap(10);
            bundleProductsContainer.setVgap(10);
            bundleProductsContainer.setPadding(new Insets(10));
            bundleProductsContainer.prefWrapLengthProperty().bind(
                    bundleProductsContainer.widthProperty()
            );
        }
    }

    /**
     * Load all data from database
     */
    private void loadAllData() {
        System.out.println("Loading all data...");

        try {
            // Clear existing data
            products.clear();
            bundleProductMap.clear();

            // Clear containers
            if (productsContainer != null) {
                productsContainer.getChildren().clear();
            }
            if (bundleProductsContainer != null) {
                bundleProductsContainer.getChildren().clear();
            }

            // Load products from database
            loadProductsFromDatabase();
            System.out.println("Loaded " + products.size() + " products");

            // Load bundle products
            loadBundleProducts();
            System.out.println("Loaded " + bundleProductMap.size() + " bundles");

            // Display products
            displayProducts();
            System.out.println("Products displayed");

        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Error", "Error loading data: " + e.getMessage());
        }
    }

    private void maximizeStage() {
        try {
            Stage stage = null;
            if (cashierBtn != null && cashierBtn.getScene() != null) {
                stage = (Stage) cashierBtn.getScene().getWindow();
            } else if (profileBtn != null && profileBtn.getScene() != null) {
                stage = (Stage) profileBtn.getScene().getWindow();
            }

            if (stage != null) {
                stage.setMaximized(true);
                stage.setMinWidth(1024);
                stage.setMinHeight(768);
                System.out.println("Stage maximized successfully");
            } else {
                System.err.println("Could not get stage reference");
            }
        } catch (Exception e) {
            System.err.println("Error maximizing stage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Public method to reload all content when returning to Cashier page
     */
    public void reloadAllContent() {
        System.out.println("=== RELOADING ALL CONTENT ===");

        Platform.runLater(() -> {
            try {
                products.clear();
                bundleProductMap.clear();
                cartItems.clear();

                if (productsContainer != null) {
                    productsContainer.getChildren().clear();
                }
                if (bundleProductsContainer != null) {
                    bundleProductsContainer.getChildren().clear();
                }
                if (cartItemsContainer != null) {
                    cartItemsContainer.getChildren().clear();
                }

                resetAllFields();
                loadAllData();

                if (productsContainer != null) {
                    productsContainer.requestLayout();
                }
                if (bundleProductsContainer != null) {
                    bundleProductsContainer.requestLayout();
                }

                System.out.println("=== RELOAD COMPLETE ===");

            } catch (Exception e) {
                System.err.println("Error reloading content: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Reset all input fields and labels
     */
    private void resetAllFields() {
        try {
            if (searchField != null) {
                searchField.clear();
            }
            if (productNameLabel != null) {
                productNameLabel.setText("");
            }
            if (priceLabel != null) {
                priceLabel.setText("");
            }
            if (paidField != null) {
                paidField.clear();
            }
            if (balanceLabel != null) {
                balanceLabel.setText("");
            }
            if (totalAmountLabel != null) {
                totalAmountLabel.setText(formatPrice(0));
            }
            if (addToCartBtn != null) {
                addToCartBtn.setDisable(true);
            }

            currentProduct = null;
            totalAmount = 0.0;

        } catch (Exception e) {
            System.err.println("Error resetting fields: " + e.getMessage());
        }
    }

    private void clearCart() {
        cartItems.clear();
        totalAmount = 0.0;
        refreshCartView();
        updateTotalDisplay();
    }

    private void loadBundleProducts() {
        System.out.println("Loading bundle products...");
        bundleProductMap.clear();

        if (bundleProductsContainer != null) {
            bundleProductsContainer.getChildren().clear();
        } else {
            System.err.println("WARNING: bundleProductsContainer is null!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String bundleQuery = "SELECT DISTINCT code, price FROM bundle_products";
            try (PreparedStatement stmt = conn.prepareStatement(bundleQuery);
                 ResultSet rs = stmt.executeQuery()) {

                int bundleCount = 0;
                while (rs.next()) {
                    bundleCount++;
                    String bundleCode = rs.getString("code");
                    double bundlePrice = rs.getDouble("price");

                    BundleProduct bundle = new BundleProduct(bundleCode, bundlePrice);
                    bundle.setPreserveOriginalPrice(true);
                    bundleProductMap.put(bundleCode, bundle);

                    loadBundleItems(conn, bundle);

                    VBox bundleBox = createBundleProductBox(bundle);
                    bundleProductsContainer.getChildren().add(bundleBox);
                }

                System.out.println("Loaded " + bundleCount + " bundle products");
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

                    Product product = new Product(
                            productCode, productName, productPrice,
                            productQty, expirationDate, category
                    );

                    bundle.addProduct(product, qty);
                    productNames.add(productName + " (" + qty + ")");
                }

                if (!productNames.isEmpty()) {
                    fullBundleName.append(String.join(", ", productNames));
                    bundle.setName(fullBundleName.toString());
                } else {
                    bundle.setName(bundleName);
                }
            }
        }
    }

    private String getBundleNameFromDatabase(Connection conn, String bundleCode) {
        String defaultName = "Bundle";

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

    private VBox createBundleProductBox(BundleProduct bundle) {
        VBox bundleBox = new VBox(5);
        bundleBox.setStyle("-fx-border-color: #CCCCCC; -fx-border-radius: 8; -fx-padding: 10; " +
                "-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        bundleBox.setMinWidth(200);
        bundleBox.setMaxWidth(220);
        bundleBox.setPrefWidth(200);

        String bundleName = bundle.getName();
        int colonIndex = bundleName.indexOf(':');
        String title = colonIndex > 0 ? bundleName.substring(0, colonIndex) : bundleName;

        Label nameLabel = new Label(title);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        Label includesLabel = new Label("Includes:");
        includesLabel.setStyle("-fx-font-size: 11px; -fx-font-style: italic; -fx-text-fill: #666;");

        VBox productsListBox = new VBox(2);
        productsListBox.setMaxWidth(Double.MAX_VALUE);

        if (colonIndex > 0 && colonIndex < bundleName.length() - 1) {
            String productsText = bundleName.substring(colonIndex + 1).trim();
            String[] products = productsText.split(",");

            for (String product : products) {
                Label productLabel = new Label("• " + product.trim());
                productLabel.setStyle("-fx-font-size: 10px;");
                productLabel.setWrapText(true);
                productLabel.setMaxWidth(Double.MAX_VALUE);
                productsListBox.getChildren().add(productLabel);
            }
        }

        Label priceLabel = new Label(formatPrice(bundle.getDiscountedPrice()));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2E7D32;");

        double savings = bundle.getSavingsAmount();
        double savingsPercentage = bundle.getSavingsPercentage();

        Label savingsLabel = new Label(String.format("Save %s (%.0f%% off)",
                formatPrice(savings), savingsPercentage));
        savingsLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 11px;");
        savingsLabel.setWrapText(true);
        savingsLabel.setMaxWidth(Double.MAX_VALUE);

        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.setMaxWidth(Double.MAX_VALUE);
        addToCartBtn.setStyle("-fx-background-color: #5B8336; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
        addToCartBtn.setOnMouseEntered(e -> addToCartBtn.setStyle("-fx-background-color: #4A6B2A; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;"));
        addToCartBtn.setOnMouseExited(e -> addToCartBtn.setStyle("-fx-background-color: #5B8336; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;"));
        addToCartBtn.setOnAction(e -> addBundleToCart(bundle));

        bundleBox.getChildren().addAll(nameLabel, includesLabel, productsListBox, priceLabel, savingsLabel, addToCartBtn);

        return bundleBox;
    }

    private void addBundleToCart(BundleProduct bundle) {
        boolean bundleFound = false;

        for (CartItem item : cartItems) {
            if (item.getName().equals(bundle.getName())) {
                item.setQuantity(item.getQuantity() + 1);
                bundleFound = true;
                break;
            }
        }

        if (!bundleFound) {
            CartItem bundleItem = new CartItem(bundle.getName(), bundle.getPrice(), 1);
            cartItems.add(bundleItem);
        }

        refreshCartView();
        updateTotalAmount();

        showAlert(Alert.AlertType.INFORMATION, "Success", "Bundle added to cart!");
    }

    private void loadProductsFromDatabase() {
        System.out.println("Loading products from database...");
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

            int count = 0;
            while (rs.next()) {
                count++;
                String code = rs.getString("code");
                String name = rs.getString("product_name");
                double price = rs.getDouble("price");
                int qty = rs.getInt("qty");

                String expDate = null;
                Date sqlDate = rs.getDate("exp_date");
                if (sqlDate != null) {
                    expDate = dateFormat.format(sqlDate);
                }

                String category = rs.getString("category");

                Product product = new Product(code, name, price, qty, expDate, category);
                products.add(product);
            }

            System.out.println("Loaded " + count + " products from database");

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
        System.out.println("Displaying products...");

        if (productsContainer == null) {
            System.err.println("ERROR: productsContainer is null!");
            return;
        }

        productsContainer.getChildren().clear();

        if (products.isEmpty()) {
            Label noProductsLabel = new Label("No products available");
            noProductsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
            productsContainer.getChildren().add(noProductsLabel);
            System.out.println("No products to display");
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
            System.out.println("Displayed " + products.size() + " products");
        }

        productsContainer.requestLayout();
    }

    private VBox createProductBox(Product product) {
        VBox productBox = new VBox(5);
        productBox.setStyle("-fx-border-color: #CCCCCC; -fx-border-radius: 8; -fx-padding: 10; " +
                "-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        productBox.setMinWidth(160);
        productBox.setMaxWidth(180);
        productBox.setPrefWidth(170);

        Label nameLabel = new Label(product.getName());
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-size: 12px;");
        nameLabel.setMinHeight(40);

        Label priceLabel = new Label(formatPrice(product.getPrice()));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2E7D32;");

        Label stockLabel = new Label("Stock: " + product.getQuantity());
        stockLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        Button addButton = new Button("Add to Cart");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setStyle("-fx-background-color: #5B8336; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
        addButton.setOnMouseEntered(e -> addButton.setStyle("-fx-background-color: #4A6B2A; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;"));
        addButton.setOnMouseExited(e -> addButton.setStyle("-fx-background-color: #5B8336; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;"));
        addButton.setOnAction(e -> handleAddToCartFromDisplay(product));

        if (product.getQuantity() <= 0) {
            addButton.setDisable(true);
            addButton.setText("Out of Stock");
            addButton.setStyle("-fx-background-color: #CCCCCC; -fx-text-fill: #666; -fx-font-size: 11px;");
        }

        productBox.getChildren().addAll(nameLabel, priceLabel, stockLabel, addButton);

        return productBox;
    }

    private void handleAddToCartFromDisplay(Product product) {
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
                if (item.getQuantity() + 1 <= availableQty) {
                    item.setQuantity(item.getQuantity() + 1);
                    item.setMaxQuantity(availableQty);
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
        itemRow.setPadding(new Insets(8));
        itemRow.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; " +
                "-fx-border-width: 0 0 1 0; -fx-border-radius: 0;");

        VBox productInfo = new VBox(3);
        HBox.setHgrow(productInfo, Priority.ALWAYS);

        Label nameLabel = new Label(item.getName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setStyle("-fx-font-size: 12px;");

        Label priceLabel = new Label(formatPrice(item.getPrice()));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #2E7D32;");

        productInfo.getChildren().addAll(nameLabel, priceLabel);

        HBox quantityControls = new HBox(8);
        quantityControls.setAlignment(javafx.geometry.Pos.CENTER);

        Button minusBtn = new Button("-");
        minusBtn.setMinWidth(30);
        minusBtn.setPrefWidth(30);
        minusBtn.setStyle("-fx-background-color: #F5F5F5; -fx-font-weight: bold; -fx-cursor: hand;");
        minusBtn.setOnAction(e -> handleDecreaseQuantity(item));

        Label quantityLabel = new Label(String.valueOf(item.getQuantity()));
        quantityLabel.setMinWidth(35);
        quantityLabel.setPrefWidth(35);
        quantityLabel.setAlignment(javafx.geometry.Pos.CENTER);
        quantityLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Button plusBtn = new Button("+");
        plusBtn.setMinWidth(30);
        plusBtn.setPrefWidth(30);
        plusBtn.setStyle("-fx-background-color: #F5F5F5; -fx-font-weight: bold; -fx-cursor: hand;");
        plusBtn.setOnAction(e -> handleIncreaseQuantity(item));

        quantityControls.getChildren().addAll(minusBtn, quantityLabel, plusBtn);

        Button deleteBtn = new Button("×");
        deleteBtn.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; " +
                "-fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand;");
        deleteBtn.setMinWidth(35);
        deleteBtn.setPrefWidth(35);
        deleteBtn.setOnAction(e -> handleRemoveFromCart(item));

        itemRow.getChildren().addAll(productInfo, quantityControls, deleteBtn);

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

                    String expDate = null;
                    Date sqlDate = rs.getDate("exp_date");
                    if (sqlDate != null) {
                        expDate = dateFormat.format(sqlDate);
                    }

                    String category = rs.getString("category");

                    currentProduct = new Product(productCode, name, price, qty, expDate, category);

                    productNameLabel.setText(name);
                    priceLabel.setText(formatPrice(price));
                    addToCartBtn.setDisable(qty <= 0);

                    System.out.println("Found product: " + name + " with price: " + price);
                } else {
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
                availableQty = 0;
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (pst != null) pst.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing resources: " + e.getMessage());
                }
            }

            for (CartItem item : cartItems) {
                if (item.getName().equals(productName)) {
                    if (item.getQuantity() + 1 <= availableQty) {
                        item.setQuantity(item.getQuantity() + 1);
                        refreshCartView();
                        updateTotalAmount();
                        searchField.clear();

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

            if (availableQty > 0) {
                CartItem newItem = new CartItem(productName, productPrice, 1, availableQty);
                cartItems.add(newItem);
                refreshCartView();
                updateTotalAmount();
                searchField.clear();

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

    private void setupSearchFieldListener() {
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                searchProductByCode();
            }
        });
    }

    private void handleRemoveFromCart(CartItem itemToRemove) {
        cartItems.remove(itemToRemove);
        refreshCartView();
        updateTotalAmount();
    }

    private void handleIncreaseQuantity(CartItem item) {
        int availableQty = 0;
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

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

    private void handleDecreaseQuantity(CartItem item) {
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            refreshCartView();
            updateTotalAmount();
        }
    }

    @FXML
    void handlePayment(ActionEvent event) {
        String paidText = paidField.getText().trim();
        if (paidText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter the paid amount.");
            return;
        }

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

            String username = "guest";
            User currentUser = LoginController.getCurrentUser();
            if (currentUser != null) {
                username = currentUser.getUsername();
            }

            PurchaseTransaction transaction = new PurchaseTransaction(
                    new java.sql.Date(System.currentTimeMillis()),
                    0,
                    cartItems,
                    username
            );

            try {
                transaction.processTransaction();

                double change = paidAmount - transaction.getTotalAmount();

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
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Transaction processing failed: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid payment amount.");
        }
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
                reloadAllContent();
                return;
            }

            if (!fxmlFile.isEmpty()) {
                URL url = getClass().getResource(fxmlFile);

                if (url == null) {
                    String altPath = fxmlFile.replace("/com/example/uts_pbo/", "/");
                    url = getClass().getResource(altPath);

                    if (url == null) {
                        String noSlashPath = fxmlFile.substring(1);
                        url = getClass().getClassLoader().getResource(noSlashPath);

                        if (url == null) {
                            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                                    "Could not find FXML file: " + fxmlFile);
                            return;
                        }
                    }
                }

                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                Stage stage = (Stage) ((Button) source).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.show();

                if (fxmlFile.equals("Cashier.fxml")) {
                    Platform.runLater(() -> {
                        Object controller = loader.getController();
                        if (controller instanceof CashierController) {
                            ((CashierController) controller).reloadAllContent();
                        }
                    });
                }
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
            this.maxQuantity = maxQuantity;
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