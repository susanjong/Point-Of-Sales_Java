package Admin_View;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import User_dashboard.NavigationAuthorizer;
import User_dashboard.UserSession;

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
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class TransactionLogController implements Initializable {

    @FXML private Button profileBtn;
    @FXML private Button cashierBtn;
    @FXML private Button productsBtn;
    @FXML private Button bundleproductsBtn;
    @FXML private Button refundproductsBtn;
    @FXML private Button usersBtn;
    @FXML private Button adminLogBtn;
    @FXML private Button transLogBtn;
    @FXML private Button authButton;
    @FXML private Button transactionButton;
    @FXML private Button productModButton;
    @FXML private Button exportButton;
    
    @FXML private TableView<TransactionLogEntry> logTableView;
    @FXML private TableColumn<TransactionLogEntry, Integer> noColumn;
    @FXML private TableColumn<TransactionLogEntry, String> timestampColumn;
    @FXML private TableColumn<TransactionLogEntry, Integer> transactionIdColumn;
    @FXML private TableColumn<TransactionLogEntry, String> usernameColumn;
    @FXML private TableColumn<TransactionLogEntry, String> productsColumn;
    @FXML private TableColumn<TransactionLogEntry, String> amountColumn;
    @FXML private TableColumn<TransactionLogEntry, Integer> itemCountColumn;
    @FXML private TableColumn<TransactionLogEntry, String> transactionTypeColumn;
    @FXML private TableColumn<TransactionLogEntry, String> statusColumn;
    
    @FXML private Button allTransButton;
    @FXML private Button salesButton;
    @FXML private Button returnsButton;
    @FXML private Button completedButton;
    @FXML private Button canceledButton;
    
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField minAmountField;
    @FXML private TextField maxAmountField;
    @FXML private TextField userSearchField;
    @FXML private Button filterButton;
    @FXML private Button resetFilterButton;
    
    private ObservableList<TransactionLogEntry> logEntries;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize columns
        noColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("formattedTimestamp"));
        transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        productsColumn.setCellValueFactory(new PropertyValueFactory<>("products"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("formattedAmount"));
        itemCountColumn.setCellValueFactory(new PropertyValueFactory<>("itemCount"));
        transactionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        
        // Set up date pickers with default values
        startDatePicker.setValue(LocalDate.now().minusWeeks(1));
        endDatePicker.setValue(LocalDate.now());
        
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
        
        // Load all transactions by default
        showAllTransactions();
    }
    
    private void loadAllLogs() {
        List<TransactionLogEntry> logs = TransactionLogDAO.getAllLogs();
        logEntries = FXCollections.observableArrayList(logs);
        logTableView.setItems(logEntries);
    }
    
    @FXML
    private void showAllTransactions() {
        setButtonSelected(allTransButton);
        loadAllLogs();
    }
    
    @FXML
    private void showSalesTransactions() {
        setButtonSelected(salesButton);
        List<TransactionLogEntry> logs = TransactionLogDAO.getLogsByTransactionType("Sale");
        logEntries = FXCollections.observableArrayList(logs);
        logTableView.setItems(logEntries);
    }
    
    @FXML
    private void showReturnTransactions() {
        setButtonSelected(returnsButton);
        
        List<TransactionLogEntry> logEntries = convertRefundsToLogEntries();
        this.logEntries = FXCollections.observableArrayList(logEntries);
        logTableView.setItems(this.logEntries);
    }

    private List<TransactionLogEntry> convertRefundsToLogEntries() {
        List<TransactionLogEntry> entries = new ArrayList<>();
        List<RefundEntry> refunds = RefundDAO.getAllRefunds();
        
        int id = 1;
        for (RefundEntry refund : refunds) {
            // Create a description of the refunded product
            String productDescription = refund.getProductCode() + " (Qty: " + refund.getQty() + ")";
            
            // Use the existing constructor
            TransactionLogEntry entry = new TransactionLogEntry(
                id++,
                refund.getTimestamp(),
                refund.getTransactionId(),
                refund.getUsername(),
                productDescription,
                refund.getTotalRefund(),
                refund.getQty(),
                "Return",  // transaction type
                "Completed"  // status
            );
            entries.add(entry);
        }
        
        return entries;
    }
    
    @FXML
    private void showCompletedTransactions() {
        setButtonSelected(completedButton);
        List<TransactionLogEntry> logs = TransactionLogDAO.getLogsByStatus("Completed");
        logEntries = FXCollections.observableArrayList(logs);
        logTableView.setItems(logEntries);
    }
    
    @FXML
    private void showCanceledTransactions() {
        setButtonSelected(canceledButton);
        List<TransactionLogEntry> logs = TransactionLogDAO.getLogsByStatus("Canceled");
        logEntries = FXCollections.observableArrayList(logs);
        logTableView.setItems(logEntries);
    }
    
    private void setButtonSelected(Button selectedButton) {
        // Reset styles
        allTransButton.setStyle("-fx-background-radius: 50px;");
        salesButton.setStyle("-fx-background-radius: 50px;");
        returnsButton.setStyle("-fx-background-radius: 50px;");
        completedButton.setStyle("-fx-background-radius: 50px;");
        canceledButton.setStyle("-fx-background-radius: 50px;");
        // Highlight
        selectedButton.setStyle("-fx-background-radius: 50px; -fx-background-color: #5b8336; -fx-text-fill: white;");
    }
    
    @FXML
    private void applyFilters() {
        LocalDateTime startDateTime = startDatePicker.getValue().atStartOfDay();
        LocalDateTime endDateTime = endDatePicker.getValue().atTime(LocalTime.MAX);
        List<TransactionLogEntry> filteredLogs = TransactionLogDAO.getLogsByDateRange(startDateTime, endDateTime);
    
        
        // User search filter
        String userSearch = userSearchField.getText().trim();
        if (!userSearch.isEmpty()) {
            filteredLogs.removeIf(log -> !log.getUsername().toLowerCase().contains(userSearch.toLowerCase()));
        }
        
        logEntries = FXCollections.observableArrayList(filteredLogs);
        logTableView.setItems(logEntries);
    }
    
    @FXML
    private void resetFilters() {
        startDatePicker.setValue(LocalDate.now().minusWeeks(1));
        endDatePicker.setValue(LocalDate.now());
        minAmountField.clear();
        maxAmountField.clear();
        userSearchField.clear();
        
        if (allTransButton.getStyle().contains("#5b8336")) showAllTransactions();
        else if (salesButton.getStyle().contains("#5b8336")) showSalesTransactions();
        else if (returnsButton.getStyle().contains("#5b8336")) showReturnTransactions();
        else if (completedButton.getStyle().contains("#5b8336")) showCompletedTransactions();
        else if (canceledButton.getStyle().contains("#5b8336")) showCanceledTransactions();
        else loadAllLogs();
    }
    
    @FXML
    private void handleNavigation(ActionEvent event) {
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
            } else if (source == transLogBtn) {
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