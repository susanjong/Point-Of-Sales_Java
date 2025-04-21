package Admin_View;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RefundProductsController {
    
    @FXML private Button profileBtn;
    @FXML private Button cashierBtn;
    @FXML private Button productsBtn;
    @FXML private Button bundleproductsBtn;
    @FXML private Button refundproductsBtn;
    @FXML private Button usersBtn;
    @FXML private Button adminLogBtn;
    
    @FXML private TableView<Refund> refundproductTable;
    @FXML private TableColumn<Refund, String> refundcodeColumn;
    @FXML private TableColumn<Refund, String> nameColumn;
    @FXML private TableColumn<Refund, String> codeColumn;
    @FXML private TableColumn<Refund, Double> priceColumn;
    @FXML private TableColumn<Refund, Integer> qtyColumn;
    
    @FXML private VBox refundItemsContainer;
    @FXML private Label totalAmountLabel;
    @FXML private TextField paidField;
    @FXML private Label balanceLabel;
    
    private ObservableList<Refund> refundList = FXCollections.observableArrayList();
    private List<Refund> selectedRefunds = new ArrayList<>();
    private double totalAmount = 0.0;
    
    @FXML
    public void initialize() {
        // Initialize table columns
        refundcodeColumn.setCellValueFactory(cellData -> cellData.getValue().refundCodeProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().productNameProperty());
        codeColumn.setCellValueFactory(cellData -> cellData.getValue().productCodeProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        qtyColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        
        // Load refund data
        loadRefundData();
        
        // Set up table selection listener
        refundproductTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        refundproductTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                addToRefundList(newSelection);
            }
        });
        
        // Set up paid field listener for calculating balance
        paidField.textProperty().addListener((observable, oldValue, newValue) -> {
            calculateBalance();
        });
    }
    
    private void loadRefundData() {
        // Mock data for demonstration purposes
        // In a real application, this would load from a database or other data source
        refundList.add(new Refund("RF001", "Product A", "P001", 15000.0, 2));
        refundList.add(new Refund("RF002", "Product B", "P002", 25000.0, 1));
        refundList.add(new Refund("RF003", "Product C", "P003", 10000.0, 3));
        refundList.add(new Refund("RF004", "Product D", "P004", 30000.0, 1));
        
        refundproductTable.setItems(refundList);
    }
    
    private void addToRefundList(Refund refund) {
        if (!selectedRefunds.contains(refund)) {
            selectedRefunds.add(refund);
            updateRefundItemsContainer();
            calculateTotal();
        }
    }
    
    private void updateRefundItemsContainer() {
        refundItemsContainer.getChildren().clear();
        
        for (int i = 0; i < selectedRefunds.size(); i++) {
            Refund refund = selectedRefunds.get(i);
            final int index = i;
            
            // Create a display for each selected refund item
            Label itemLabel = new Label(refund.getProductName() + " - " + 
                                      refund.getQuantity() + " x Rp " + 
                                      refund.getPrice() + " = Rp " + 
                                      (refund.getPrice() * refund.getQuantity()));
            
            Button removeButton = new Button("Remove");
            removeButton.setOnAction(e -> {
                selectedRefunds.remove(index);
                updateRefundItemsContainer();
                calculateTotal();
            });
            
            // Add components to a row
            javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10);
            row.getChildren().addAll(itemLabel, removeButton);
            refundItemsContainer.getChildren().add(row);
        }
    }
    
    private void calculateTotal() {
        totalAmount = 0.0;
        for (Refund refund : selectedRefunds) {
            totalAmount += refund.getPrice() * refund.getQuantity();
        }
        totalAmountLabel.setText("Rp " + totalAmount);
        calculateBalance();
    }
    
    private void calculateBalance() {
        try {
            double paidAmount = Double.parseDouble(paidField.getText());
            double balance = paidAmount - totalAmount;
            balanceLabel.setText("Rp " + balance);
        } catch (NumberFormatException e) {
            balanceLabel.setText("Rp 0");
        }
    }
    
    @FXML
    private void handlerefund() {
        if (selectedRefunds.isEmpty()) {
            showAlert("Error", "No items selected for refund.");
            return;
        }
        
        try {
            double paidAmount = Double.parseDouble(paidField.getText());
            if (paidAmount < totalAmount) {
                showAlert("Error", "Paid amount must be at least equal to the total amount.");
                return;
            }
            
            // Process refund
            // In a real application, this would update the database and create records
            showAlert("Success", "Refund processed successfully!");
            
            // Clear the refund after processing
            selectedRefunds.clear();
            refundItemsContainer.getChildren().clear();
            totalAmount = 0.0;
            totalAmountLabel.setText("Rp 0");
            paidField.setText("");
            balanceLabel.setText("Rp 0");
            
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid paid amount.");
        }
    }
    
    // Fixed method with consistent signature
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Additional overloaded method to handle the AlertType parameter
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
                fxmlFile = "AuthenticationLog.fxml";
            } else if (source == refundproductsBtn) {
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