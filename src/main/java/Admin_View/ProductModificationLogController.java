package Admin_View;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import com.example.uts_pbo.DatabaseConnection;
import com.example.uts_pbo.NavigationAuthorizer;
import com.example.uts_pbo.UserSession;

import javafx.application.Platform;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ProductModificationLogController implements Initializable {

    // Navigation buttons
    @FXML private Button profileBtn;
    @FXML private Button cashierBtn;
    @FXML private Button productsBtn;
    @FXML private Button bundleproductsBtn;
    @FXML private Button usersBtn;
    @FXML private Button adminLogBtn;
    @FXML private Button refundproductsBtn;

    // Category buttons
    @FXML private Button authButton;
    @FXML private Button transactionButton;
    @FXML private Button productModButton;

    // Filter controls
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> actionTypeFilterComboBox;
    @FXML private TextField productSearchField;
    @FXML private TextField userSearchField;
    @FXML private Button filterButton;
    @FXML private Button resetFilterButton;

    // Table view
    @FXML private TableView<ProductModificationLog> logTableView;
    @FXML private TableColumn<ProductModificationLog, Integer> idColumn;
    @FXML private TableColumn<ProductModificationLog, LocalDateTime> timestampColumn;
    @FXML private TableColumn<ProductModificationLog, Integer> userIdColumn;
    @FXML private TableColumn<ProductModificationLog, String> usernameColumn;
    @FXML private TableColumn<ProductModificationLog, String> productCodeColumn;
    @FXML private TableColumn<ProductModificationLog, String> productNameColumn;
    @FXML private TableColumn<ProductModificationLog, String> actionTypeColumn;

    private ObservableList<ProductModificationLog> logsList = FXCollections.observableArrayList();
    private ObservableList<String> actionTypes = FXCollections.observableArrayList(
            "All", "ADD", "UPDATE", "DELETE"
    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        productCodeColumn.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        actionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("actionType"));

        // Set up action type filter combobox
        actionTypeFilterComboBox.setItems(actionTypes);
        actionTypeFilterComboBox.setValue("All");

        // Set up date pickers with defaults (e.g., last 30 days)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        startDatePicker.setValue(startDate);
        endDatePicker.setValue(endDate);

        // Set up button handlers
        filterButton.setOnAction(event -> applyFilters());
        resetFilterButton.setOnAction(event -> resetFilters());

        // Set up category button handlers
        authButton.setOnAction((@SuppressWarnings("unused") ActionEvent e) -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Admin_View/AuthenticationLog.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) authButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        
        transactionButton.setOnAction((@SuppressWarnings("unused") ActionEvent e) -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Admin_View/TransactionLog.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) transactionButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        
        productModButton.setOnAction((@SuppressWarnings("unused") ActionEvent e) -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Admin_View/ProductModificationLog.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) productModButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        
        // Redirect non-admins away immediately
        Platform.runLater(() -> {
            if (!UserSession.isAdmin()) {
                NavigationAuthorizer.navigateTo(
                  profileBtn,
                  "/Admin_View/Profile.fxml",
                  NavigationAuthorizer.USER_VIEW
                );
                showAlert(Alert.AlertType.WARNING,
                    "Access Denied",
                    "You don't have permission to access Transaction Logs. Admin access required."
                );
            }
        });

        // Set up styles for the current active button
        productModButton.setStyle("-fx-background-radius: 50px; -fx-background-color: #5b8336; -fx-text-fill: white;");
        
        // Load initial data
        loadLogs();
    }

    public void loadLogs() {
        logsList.clear();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT l.id, l.user_id, l.username, l.product_code, l.product_name, " +
                          "l.action_type, l.timestamp " +
                          "FROM product_modification_log l " +
                          "WHERE l.timestamp BETWEEN ? AND ? " +
                          "ORDER BY l.timestamp DESC";
            
            PreparedStatement pst = conn.prepareStatement(query);
            
            // Set date range parameters
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            
            if (startDate != null && endDate != null) {
                pst.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
                pst.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));
            } else {
                // Default to last 30 days if no dates selected
                pst.setTimestamp(1, Timestamp.valueOf(LocalDate.now().minusDays(30).atStartOfDay()));
                pst.setTimestamp(2, Timestamp.valueOf(LocalDate.now().plusDays(1).atStartOfDay()));
            }
            
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                ProductModificationLog log = new ProductModificationLog(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("product_code"),
                    rs.getString("product_name"),
                    rs.getString("action_type"),
                    rs.getTimestamp("timestamp").toLocalDateTime()
                );
                logsList.add(log);
            }
            
            logTableView.setItems(logsList);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Could not load log data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void applyFilters() {
        logsList.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT l.id, l.user_id, l.username, l.product_code, l.product_name, " +
                           "l.action_type, l.timestamp " +
                           "FROM product_modification_log l " +
                           "WHERE l.timestamp BETWEEN ? AND ? " +
                           "ORDER BY l.timestamp DESC";
    
            PreparedStatement pst = conn.prepareStatement(query);
    
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
    
            if (startDate != null && endDate != null) {
                pst.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
                pst.setTimestamp(2, Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()));
            } else {
                // Default to last 30 days if not selected
                pst.setTimestamp(1, Timestamp.valueOf(LocalDate.now().minusDays(30).atStartOfDay()));
                pst.setTimestamp(2, Timestamp.valueOf(LocalDate.now().plusDays(1).atStartOfDay()));
            }
    
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                ProductModificationLog log = new ProductModificationLog(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("product_code"),
                    rs.getString("product_name"),
                    rs.getString("action_type"),
                    rs.getTimestamp("timestamp").toLocalDateTime()
                );
                logsList.add(log);
            }
    
            ObservableList<ProductModificationLog> filteredList = FXCollections.observableArrayList(logsList);
    
            // Action type
            String selectedActionType = actionTypeFilterComboBox.getValue();
            if (selectedActionType != null && !selectedActionType.equals("All")) {
                filteredList = filteredList.filtered(log ->
                    log.getActionType().equals(selectedActionType));
            }
    
            // Product search
            String productSearch = productSearchField.getText().trim().toLowerCase();
            if (!productSearch.isEmpty()) {
                filteredList = filteredList.filtered(log ->
                    log.getProductCode().toLowerCase().contains(productSearch) ||
                    log.getProductName().toLowerCase().contains(productSearch));
            }
    
            // User search
            String userSearch = userSearchField.getText().trim().toLowerCase();
            if (!userSearch.isEmpty()) {
                filteredList = filteredList.filtered(log ->
                    log.getUsername().toLowerCase().contains(userSearch) ||
                    String.valueOf(log.getUserId()).contains(userSearch));
            }
    
            logTableView.setItems(filteredList);
    
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not apply filters: " + e.getMessage());
            e.printStackTrace();
        }
    }    
    
    private void resetFilters() {
        // Reset all filter fields
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());
        actionTypeFilterComboBox.setValue("All");
        productSearchField.clear();
        userSearchField.clear();
        
        // Reload data
        loadLogs();
    }
    
    // Helper method to create a log entry - simplified without action_details
    public static void logProductAction(int userId, String username, String productCode, 
                                    String productName, String actionType) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String insertQuery = "INSERT INTO product_modification_log (user_id, username, " +
                                "product_code, product_name, action_type, timestamp) " +
                                "VALUES (?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pst = conn.prepareStatement(insertQuery);
            pst.setInt(1, userId);
            pst.setString(2, username);
            pst.setString(3, productCode);
            pst.setString(4, productName);
            pst.setString(5, actionType);
            
            pst.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            // Consider logging this error to a file
        }
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
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}