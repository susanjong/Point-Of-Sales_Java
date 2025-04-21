package Admin_View;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.example.uts_pbo.NavigationAuthorizer;
import com.example.uts_pbo.UserSession;

public class TransactionDetailLogController implements Initializable {

    @FXML private Button profileBtn;
    @FXML private Button cashierBtn;
    @FXML private Button productsBtn;
    @FXML private Button bundleproductsBtn;
    @FXML private Button usersBtn;
    @FXML private Button adminLogBtn;

    @FXML private Button authButton;
    @FXML private Button transactionButton;
    @FXML private Button productModButton;
    @FXML private Button sellingModButton;
    @FXML private Button transDetailButton;

    @FXML private TableView<TransactionEntry> transactionTable;
    @FXML private TableColumn<TransactionEntry, Integer> transactionIdCol;
    @FXML private TableColumn<TransactionEntry, String> dateCol;
    @FXML private TableColumn<TransactionEntry, String> usernameCol;
    @FXML private TableColumn<TransactionEntry, String> productsCol;
    @FXML private TableColumn<TransactionEntry, String> totalPriceCol;
    @FXML private TableColumn<TransactionEntry, Integer> totalItemCol;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField searchField;
    @FXML private Button filterButton;
    @FXML private Button resetButton;

    private List<TransactionEntry> allTransactions;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        transactionIdCol.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().getTransactionId()));
        dateCol.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().getFormattedDate()));
        usernameCol.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().getUsername()));
        productsCol.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().getProducts()));
        totalPriceCol.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().getFormattedTotalPrice()));
        totalItemCol.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().getTotalItem()));

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
        
                // Jika ingin langsung tampilkan log "Product Modified", bisa:
                // AuthenticationLogController controller = loader.getController();
                // controller.showProductModificationLogs();
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

         // Redirect non‑admins away immediately
        Platform.runLater(() -> {
            if (!UserSession.isAdmin()) {
                NavigationAuthorizer.navigateTo(
                  profileBtn,
                  "/Admin_View/Profile.fxml",
                  NavigationAuthorizer.USER_VIEW
                );
                showAlert(Alert.AlertType.WARNING,
                    "Access Denied",
                    "You don't have permission to access Authentication Logs. Admin access required."
                );
            }
        });

        loadTransactions();
    }

    private void loadTransactions() {
        allTransactions = TransactionDAO.getAllTransactions();
        transactionTable.setItems(FXCollections.observableArrayList(allTransactions));
    }

    @FXML
    private void applyFilters() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        String keyword = searchField.getText().trim().toLowerCase();

        LocalDateTime startDT = start.atStartOfDay();
        LocalDateTime endDT = end.atTime(LocalTime.MAX);

        List<TransactionEntry> filtered = allTransactions.stream()
                .filter(tx -> {
                    LocalDateTime txDate = tx.getDateTime();
                    return !txDate.isBefore(startDT) && !txDate.isAfter(endDT);
                })
                .filter(tx -> keyword.isEmpty()
                        || tx.getUsername().toLowerCase().contains(keyword)
                        || tx.getProducts().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

        transactionTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void resetFilters() {
        startDatePicker.setValue(LocalDate.now().minusWeeks(1));
        endDatePicker.setValue(LocalDate.now());
        searchField.clear();
        transactionTable.setItems(FXCollections.observableArrayList(allTransactions));
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
            } else if (source == cashierBtn) {
                fxmlFile = "Cashier.fxml";
            } else if (source == bundleproductsBtn) {
                fxmlFile = "BundleProducts.fxml";
            } else if (source == usersBtn) {
                fxmlFile = "UserManagement.fxml";
            } else if (source == productsBtn) {
                fxmlFile = "ProductManagement.fxml";
            } else if (source == adminLogBtn) {
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
}