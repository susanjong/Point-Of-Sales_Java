package Admin_View;

import com.example.uts_pbo.AuthLogger;
import com.example.uts_pbo.DatabaseConnection;
import com.example.uts_pbo.NavigationAuthorizer;
import com.example.uts_pbo.User;
import com.example.uts_pbo.UserDAO;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {
    // Navigation Buttons
    @FXML private Button profileBtn;
    @FXML private Button cashierBtn;
    @FXML private Button productsBtn;
    @FXML private Button bundleproductsBtn;
    @FXML private Button refundproductsBtn;
    @FXML private Button usersBtn;
    @FXML private Button adminLogBtn;

    // Table and columns
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User,Integer> userIdCol;
    @FXML private TableColumn<User,String> firstNameCol;
    @FXML private TableColumn<User,String> lastNameCol;
    @FXML private TableColumn<User,String> usernameCol;
    @FXML private TableColumn<User,String> emailCol;
    @FXML private TableColumn<User,String> roleCol;

    // Form fields
    @FXML private TextField idField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;

    // Buttons
    @FXML private Button saveUserBtn;
    @FXML private Button deleteUserBtn;

    private ObservableList<User> userList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Check session
        Platform.runLater(() -> {
            if (!UserSession.isAdmin()) {
                // redirect non‑admins to PROFILE
                NavigationAuthorizer.navigateTo(
                  profileBtn,
                  "/Admin_View/Profile.fxml",
                  NavigationAuthorizer.USER_VIEW
                );
                showAlert(Alert.AlertType.WARNING,
                          "Access Denied",
                          "Admin access required.");
            }
        });

        // Setup table columns
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Load data
        loadUsers();
        userTable.setItems(userList);

        userTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldUser, newUser) -> { if (newUser != null) displayUser(newUser); }
        );

        // Populate role ComboBox
        roleComboBox.setItems(FXCollections.observableArrayList("admin", "user"));
        idField.setDisable(true);
    }

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    private void loadUsers() {
        userList.clear();
        String sql = "SELECT * FROM users";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User u = new User(
                    rs.getInt("id"),
                    rs.getString("email"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("salt"),
                    rs.getString("role")
                );
                userList.add(u);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Error loading users: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveUser(ActionEvent event) {
        String first = firstNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String usern = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String pwd = passwordField.getText();
        String role = roleComboBox.getValue();

        if (first.isEmpty() || last.isEmpty() || usern.isEmpty() || email.isEmpty() || role == null) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please fill in all fields.");
            return;
        }

        boolean success;
        if (!idField.getText().isEmpty()) {
            // Update existing user
            int id = Integer.parseInt(idField.getText());
            User selectedUser = getUserById(id);
            
            if (selectedUser != null) {
                selectedUser.setFirstName(first);
                selectedUser.setLastName(last);
                selectedUser.setUsername(usern);
                selectedUser.setEmail(email);
                selectedUser.setRole(role);
                
                // Only update password if a new one is provided
                if (!pwd.isEmpty()) {
                    // Create a new User object for password update
                    User updatedUser = new User(
                        first, last, email, usern, pwd, role
                    );
                    updatedUser.setId(id);
                    success = UserDAO.updateUser(updatedUser);
                } else {
                    success = UserDAO.updateUser(selectedUser);
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "User not found.");
                return;
            }
        } else {
            // Create new user
            if (pwd.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please provide a password.");
                return;
            }
            
            // Create user with constructor that handles salt internally
            User newUser = new User(first, last, email, usern, pwd, role);
            success = UserDAO.createUser(newUser);
        }

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "User saved successfully.");
            loadUsers();
            clearForm();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save user.");
        }
    }

    // Helper method to get user by ID from the userList
    private User getUserById(int id) {
        for (User user : userList) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        User sel = userTable.getSelectionModel().getSelectedItem();
        if (sel==null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to delete.");
            return;
        }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete user " + sel.getUsername() + "? This cannot be undone.");
        conf.showAndWait().ifPresent(btn -> {
            if (btn==ButtonType.OK) {
                if (UserDAO.deleteUser(sel.getId())) {
                    AuthLogger.logAccountDeletion(sel);
                    showAlert(Alert.AlertType.INFORMATION, "Deleted", "User deleted.");
                    loadUsers(); clearForm();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user.");
                }
            }
        });
    }

    @FXML
    private void handleNavigation(ActionEvent event) {
        Button src = (Button) event.getSource();
        String path = switch(src.getId()) {
            case "profileBtn"       -> "/Admin_View/Profile.fxml";
            case "cashierBtn"       -> "/Admin_View/Cashier.fxml";
            case "productsBtn"      -> "/Admin_View/ProductManagement.fxml";
            case "bundleproductsBtn"-> "/Admin_View/BundleProducts.fxml";
            case "refundproductsBtn"-> "/Admin_View/RefundProducts.fxml";
            case "usersBtn"         -> null;
            case "adminLogBtn"      -> "/Admin_View/AuthenticationLog.fxml";
            default -> null;
        };
        if (path!=null) navigateTo(path);
    }

    private void navigateTo(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) profileBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    private void displayUser(User u) {
        idField.setText(String.valueOf(u.getId()));
        firstNameField.setText(u.getFirstName());
        lastNameField.setText(u.getLastName());
        usernameField.setText(u.getUsername());
        emailField.setText(u.getEmail());
        roleComboBox.setValue(u.getRole());
        passwordField.clear(); // Clear password field for security
    }

    private void clearForm() {
        idField.clear(); firstNameField.clear(); lastNameField.clear();
        usernameField.clear(); emailField.clear(); passwordField.clear();
        roleComboBox.setValue(null);
    }

    private void redirectToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/uts_pbo/Login.fxml"));
            Stage stage = (Stage) profileBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}