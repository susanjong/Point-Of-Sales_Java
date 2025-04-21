package Admin_View;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

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

public class TransactionLogController implements Initializable {

    @FXML private Button profileBtn;
    @FXML private Button cashierBtn;
    @FXML private Button productsBtn;
    @FXML private Button bundleproductsBtn;
    @FXML private Button usersBtn;
    @FXML private Button adminLogBtn;
    @FXML private Button transLogBtn;
    @FXML private Button authButton;
    @FXML private Button transactionButton;
    @FXML private Button productModButton;
    @FXML private Button sellingModButton;
    @FXML private Button transDetailButton;
    
    @FXML private TableView<TransactionLogEntry> logTableView;
    @FXML private TableColumn<TransactionLogEntry, Integer> noColumn;
    @FXML private TableColumn<TransactionLogEntry, String> timestampColumn;
    @FXML private TableColumn<TransactionLogEntry, Integer> transactionIdColumn;
    @FXML private TableColumn<TransactionLogEntry, Integer> userIdColumn;
    @FXML private TableColumn<TransactionLogEntry, String> usernameColumn;
    @FXML private TableColumn<TransactionLogEntry, String> roleColumn;
    @FXML private TableColumn<TransactionLogEntry, String> amountColumn;
    @FXML private TableColumn<TransactionLogEntry, Integer> itemCountColumn;
    @FXML private TableColumn<TransactionLogEntry, String> paymentMethodColumn;
    @FXML private TableColumn<TransactionLogEntry, String> transactionTypeColumn;
    @FXML private TableColumn<TransactionLogEntry, String> statusColumn;
    
    @FXML private Button allTransButton;
    @FXML private Button salesButton;
    @FXML private Button returnsButton;
    @FXML private Button completedButton;
    @FXML private Button canceledButton;
    
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> paymentMethodComboBox;
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
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("formattedAmount"));
        itemCountColumn.setCellValueFactory(new PropertyValueFactory<>("itemCount"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        transactionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Initialize payment method combo box
        paymentMethodComboBox.setItems(FXCollections.observableArrayList(
            "All", "Cash", "Credit Card", "Debit Card", "E-wallet", "Transfer"
        ));
        paymentMethodComboBox.setValue("All");
        
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
        
        sellingModButton.setOnAction((@SuppressWarnings("unused") ActionEvent e) -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Admin_View/SellingModificationLog.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) sellingModButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        
        transDetailButton.setOnAction((@SuppressWarnings("unused") ActionEvent e) -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Admin_View/TransactionDetailLog.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) transDetailButton.getScene().getWindow();
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
        List<TransactionLogEntry> logs = TransactionLogDAO.getLogsByTransactionType("Return");
        logEntries = FXCollections.observableArrayList(logs);
        logTableView.setItems(logEntries);
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
        
        // Payment method filter
        String selectedPaymentMethod = paymentMethodComboBox.getValue();
        if (!"All".equals(selectedPaymentMethod)) {
            filteredLogs.removeIf(log -> !log.getPaymentMethod().equals(selectedPaymentMethod));
        }
        
        // Amount range filter
        String minAmountText = minAmountField.getText().trim();
        String maxAmountText = maxAmountField.getText().trim();
        
        if (!minAmountText.isEmpty() && !maxAmountText.isEmpty()) {
            try {
                double minAmount = Double.parseDouble(minAmountText);
                double maxAmount = Double.parseDouble(maxAmountText);
                filteredLogs.removeIf(log -> log.getAmount() < minAmount || log.getAmount() > maxAmount);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please enter valid numbers for amount range.");
            }
        } else if (!minAmountText.isEmpty()) {
            try {
                double minAmount = Double.parseDouble(minAmountText);
                filteredLogs.removeIf(log -> log.getAmount() < minAmount);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please enter a valid number for minimum amount.");
            }
        } else if (!maxAmountText.isEmpty()) {
            try {
                double maxAmount = Double.parseDouble(maxAmountText);
                filteredLogs.removeIf(log -> log.getAmount() > maxAmount);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please enter a valid number for maximum amount.");
            }
        }
        
        // User search filter
        String userSearch = userSearchField.getText().trim();
        if (!userSearch.isEmpty()) {
            filteredLogs.removeIf(log -> 
                !log.getUsername().toLowerCase().contains(userSearch.toLowerCase()) && 
                !String.valueOf(log.getUserId()).equals(userSearch));
        }
        
        logEntries = FXCollections.observableArrayList(filteredLogs);
        logTableView.setItems(logEntries);
    }
    
    @FXML
    private void resetFilters() {
        startDatePicker.setValue(LocalDate.now().minusWeeks(1));
        endDatePicker.setValue(LocalDate.now());
        paymentMethodComboBox.setValue("All");
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