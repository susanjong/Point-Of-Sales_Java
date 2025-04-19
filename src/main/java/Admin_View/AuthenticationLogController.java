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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

public class AuthenticationLogController implements Initializable {

    @FXML private Button profileBtn;
    @FXML private Button cashierBtn;
    @FXML private Button productsBtn;
    @FXML private Button usersBtn;
    @FXML private Button adminLogBtn;
    
    @FXML private TableView<AuthenticationLogEntry> logTableView;
    @FXML private TableColumn<AuthenticationLogEntry, Integer> noColumn;
    @FXML private TableColumn<AuthenticationLogEntry, String> timestampColumn;
    @FXML private TableColumn<AuthenticationLogEntry, Integer> userIdColumn;
    @FXML private TableColumn<AuthenticationLogEntry, String> usernameColumn;
    @FXML private TableColumn<AuthenticationLogEntry, String> roleColumn;
    @FXML private TableColumn<AuthenticationLogEntry, String> emailColumn;
    @FXML private TableColumn<AuthenticationLogEntry, String> activityColumn;
    
    @FXML private Button authButton;
    @FXML private Button transactionButton;
    @FXML private Button productModButton;
    @FXML private Button sellingModButton;
    @FXML private Button transDetailButton;
    
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> activityFilterComboBox;
    @FXML private TextField userSearchField;
    @FXML private Button filterButton;
    @FXML private Button resetFilterButton;
    
    private ObservableList<AuthenticationLogEntry> logEntries;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize columns
        noColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("formattedTimestamp"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        activityColumn.setCellValueFactory(new PropertyValueFactory<>("activity"));
        
        // Update the activity filter combo box initialization
        activityFilterComboBox.setItems(FXCollections.observableArrayList(
            "All", "Login", "Logout", "Failed Login", "Failed Password Attempt", "Password Reset", "Account Creation"
        ));
        activityFilterComboBox.setValue("All");
        
        // Set up date pickers with default values
        startDatePicker.setValue(LocalDate.now().minusWeeks(1));
        endDatePicker.setValue(LocalDate.now());
        
        // Set up the category buttons with annotated lambdas to suppress unused param warnings
        authButton.setOnAction((@SuppressWarnings("unused") ActionEvent e) -> showAuthenticationLogs());
        transactionButton.setOnAction((@SuppressWarnings("unused") ActionEvent e) -> showTransactionLogs());
        productModButton.setOnAction((@SuppressWarnings("unused") ActionEvent e) -> showProductModificationLogs());
        sellingModButton.setOnAction((@SuppressWarnings("unused") ActionEvent e) -> showSellingModificationLogs());
        transDetailButton.setOnAction((@SuppressWarnings("unused") ActionEvent e) -> showTransactionDetailLogs());
        
        // Set up filter buttons
        if (filterButton != null) {
            filterButton.setOnAction((@SuppressWarnings("unused") ActionEvent e) -> applyFilters());
        }
        if (resetFilterButton != null) {
            resetFilterButton.setOnAction((@SuppressWarnings("unused") ActionEvent e) -> resetFilters());
        }
        
        // Load authentication logs by default
        showAuthenticationLogs();
    }
    
    private void loadAllLogs() {
        List<AuthenticationLogEntry> logs = AuthenticationLogDAO.getAllLogs();
        logEntries = FXCollections.observableArrayList(logs);
        logTableView.setItems(logEntries);
    }
    
    private void showAuthenticationLogs() {
        setButtonSelected(authButton);
        List<AuthenticationLogEntry> logs = AuthenticationLogDAO.getLogsByActivity("Login");
        logs.addAll(AuthenticationLogDAO.getLogsByActivity("Logout"));
        logs.addAll(AuthenticationLogDAO.getLogsByActivity("Failed Login"));
        logs.addAll(AuthenticationLogDAO.getLogsByActivity("Failed Password Attempt"));
        logs.addAll(AuthenticationLogDAO.getLogsByActivity("Account Creation")); // Add this line
        logEntries = FXCollections.observableArrayList(logs);
        logTableView.setItems(logEntries);
    }
    
    private void showTransactionLogs() {
        setButtonSelected(transactionButton);
        List<AuthenticationLogEntry> logs = AuthenticationLogDAO.getLogsByActivity("Transaction");
        logEntries = FXCollections.observableArrayList(logs);
        logTableView.setItems(logEntries);
    }
    
    private void showProductModificationLogs() {
        setButtonSelected(productModButton);
        List<AuthenticationLogEntry> logs = AuthenticationLogDAO.getLogsByActivity("Product Added");
        logs.addAll(AuthenticationLogDAO.getLogsByActivity("Product Updated"));
        logs.addAll(AuthenticationLogDAO.getLogsByActivity("Product Deleted"));
        logEntries = FXCollections.observableArrayList(logs);
        logTableView.setItems(logEntries);
    }
    
    private void showSellingModificationLogs() {
        setButtonSelected(sellingModButton);
        List<AuthenticationLogEntry> logs = AuthenticationLogDAO.getLogsByActivity("Selling Modified");
        logEntries = FXCollections.observableArrayList(logs);
        logTableView.setItems(logEntries);
    }
    
    private void showTransactionDetailLogs() {
        setButtonSelected(transDetailButton);
        List<AuthenticationLogEntry> logs = AuthenticationLogDAO.getLogsByActivity("Transaction Detail");
        logEntries = FXCollections.observableArrayList(logs);
        logTableView.setItems(logEntries);
    }
    
    private void setButtonSelected(Button selectedButton) {
        // Reset styles
        authButton.setStyle("-fx-background-radius: 50px;");
        transactionButton.setStyle("-fx-background-radius: 50px;");
        productModButton.setStyle("-fx-background-radius: 50px;");
        sellingModButton.setStyle("-fx-background-radius: 50px;");
        transDetailButton.setStyle("-fx-background-radius: 50px;");
        // Highlight
        selectedButton.setStyle("-fx-background-radius: 50px; -fx-background-color: #5b8336; -fx-text-fill: white;");
    }
    
    private void applyFilters() {
        LocalDateTime startDateTime = startDatePicker.getValue().atStartOfDay();
        LocalDateTime endDateTime = endDatePicker.getValue().atTime(LocalTime.MAX);
        List<AuthenticationLogEntry> filteredLogs = AuthenticationLogDAO.getLogsByDateRange(startDateTime, endDateTime);
        String selectedActivity = activityFilterComboBox.getValue();
        if (!"All".equals(selectedActivity)) {
            filteredLogs.removeIf(log -> !log.getActivity().equals(selectedActivity));
        }
        String userSearch = userSearchField.getText().trim();
        if (!userSearch.isEmpty()) {
            filteredLogs.removeIf(log -> 
                !log.getUsername().toLowerCase().contains(userSearch.toLowerCase()) && 
                !String.valueOf(log.getUserId()).equals(userSearch));
        }
        logEntries = FXCollections.observableArrayList(filteredLogs);
        logTableView.setItems(logEntries);
    }
    
    private void resetFilters() {
        startDatePicker.setValue(LocalDate.now().minusWeeks(1));
        endDatePicker.setValue(LocalDate.now());
        activityFilterComboBox.setValue("All");
        userSearchField.clear();
        if (authButton.getStyle().contains("#5b8336")) showAuthenticationLogs();
        else if (transactionButton.getStyle().contains("#5b8336")) showTransactionLogs();
        else if (productModButton.getStyle().contains("#5b8336")) showProductModificationLogs();
        else if (sellingModButton.getStyle().contains("#5b8336")) showSellingModificationLogs();
        else if (transDetailButton.getStyle().contains("#5b8336")) showTransactionDetailLogs();
        else loadAllLogs();
    }
    
    @FXML
    void handleNavigation(ActionEvent event) {
        Object source = event.getSource();
        try {
            String fxmlFile = "";
            if (source == profileBtn)       fxmlFile = "Profile.fxml";
            else if (source == cashierBtn)  fxmlFile = "Cashier.fxml";
            else if (source == productsBtn) fxmlFile = "ProductManagement.fxml";
            else if (source == usersBtn)    fxmlFile = "UserManagement.fxml";
            else if (source == adminLogBtn) return; // Already here
            if (!fxmlFile.isEmpty()) {
                Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
                Stage stage = (Stage) ((Button) source).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not navigate: " + e.getMessage());
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
