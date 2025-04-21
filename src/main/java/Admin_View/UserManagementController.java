package Admin_View;

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

import com.example.uts_pbo.NavigationAuthorizer;
import com.example.uts_pbo.UserSession;

public class UserManagementController implements Initializable {

    // Navigation buttons
    @FXML private Button profileBtn;
    @FXML private Button cashierBtn;
    @FXML private Button productsBtn;
    @FXML private Button bundleproductsBtn;
    @FXML private Button usersBtn;
    @FXML private Button adminLogBtn;
    @FXML private Button refundproductsBtn;
    
    // Table view and columns
    @FXML private TableView<User> UserTable;
    @FXML private TableColumn<User, Integer> userIdColumn;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> lastNameColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> passwordColumn;
    @FXML private TableColumn<User, String> roleColumn;
    
    // Form fields
    @FXML private TextField idField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    
    // Action buttons
    @FXML private Button saveUserBtn;
    @FXML private Button deleteUserBtn;
    
    // Observable list to hold user data
    private ObservableList<User> userList = FXCollections.observableArrayList();
    
    // SQL queries for the updated table structure
    private static final String SQL_SELECT_ALL_USERS = "SELECT * FROM users";
    private static final String SQL_INSERT_USER = "INSERT INTO users (email, first_name, last_name, username, password, role, salt) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_USER = "UPDATE users SET email = ?, first_name = ?, last_name = ?, username = ?, password = ?, role = ?, salt = ? WHERE id = ?";
    private static final String SQL_DELETE_USER = "DELETE FROM users WHERE id = ?";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        // Load data from database
        loadUsersFromDatabase();
        
        // Set table data
        UserTable.setItems(userList);
        
        // Add listener for table row selection
        UserTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    displayUserDetails(newValue);
                }
            }
        );

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
                    "You don't have permission to access User Management. Admin access required."
                );
            }
        });
        
        // Initialize combo box if not already populated
        if (roleComboBox.getItems().isEmpty()) {
            roleComboBox.setItems(FXCollections.observableArrayList("admin", "user"));
        }
        
        // Set auto-generated ID field as disabled
        idField.setDisable(true);
    }
    
    /**
     * Establishes a connection to the database
     */
    private Connection getConnection() throws SQLException {
        return com.example.uts_pbo.DatabaseConnection.getConnection();
    }
    
    /**
     * Loads users from the database into the observable list
     */
    private void loadUsersFromDatabase() {
        userList.clear();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_SELECT_ALL_USERS)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String email = rs.getString("email");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String role = rs.getString("role");
                String salt = rs.getString("salt");
                
                userList.add(new User(id, email, firstName, lastName, username, password, role, salt));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error loading users from database", e.getMessage());
        }
    }
    
    /**
     * Inserts a new user into the database
     */
    private boolean insertUser(String email, String firstName, String lastName, String username, String password, String role) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            
            // Generate a salt for the new user
            String salt = PasswordUtils.generateSalt();
            // Hash the password with the salt
            String hashedPassword = PasswordUtils.hashPassword(password, salt);
            
            pstmt.setString(1, email);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, username);
            pstmt.setString(5, hashedPassword);
            pstmt.setString(6, role);
            pstmt.setString(7, salt);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                return false;
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    userList.add(new User(id, email, firstName, lastName, username, hashedPassword, role, salt));
                    
                    // Log account creation - FIX: Use proper constructor
                    com.example.uts_pbo.AuthLogger.logAccountCreation(
                        new com.example.uts_pbo.User(id, email, firstName, lastName, username, hashedPassword, salt, role)
                    );
                    
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error inserting user", e.getMessage());
            return false;
        }
    }
    
    /**
     * Updates an existing user in the database
     */
    private boolean updateUser(int id, String email, String firstName, String lastName, String username, String password, String role) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE_USER)) {
            
            // Get the current user to check if password change occurred
            User currentUser = null;
            for (User user : userList) {
                if (user.getId() == id) {
                    currentUser = user;
                    break;
                }
            }
            
            String salt = currentUser != null ? currentUser.getSalt() : PasswordUtils.generateSalt();
            String hashedPassword;
            
            // Check if password was changed
            boolean passwordChanged = false;
            if (currentUser != null && !password.equals(currentUser.getPassword())) {
                // User changed their password - generate new salt and hash
                salt = PasswordUtils.generateSalt();
                hashedPassword = PasswordUtils.hashPassword(password, salt);
                passwordChanged = true;
            } else if (currentUser != null) {
                // Password not changed - use existing hash
                hashedPassword = currentUser.getPassword();
            } else {
                // Fallback if currentUser is null
                hashedPassword = PasswordUtils.hashPassword(password, salt);
            }
            
            pstmt.setString(1, email);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, username);
            pstmt.setString(5, hashedPassword);
            pstmt.setString(6, role);
            pstmt.setString(7, salt);
            pstmt.setInt(8, id);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Update succeeded
                
                // Check if role was changed
                if (currentUser != null && !role.equals(currentUser.getRole())) {
                    // FIX: Use proper constructor for User class
                    com.example.uts_pbo.AuthLogger.logRoleChange(
                        new com.example.uts_pbo.User(id, email, firstName, lastName, username, hashedPassword, salt, role),
                        currentUser.getRole(),
                        role
                    );
                }
                
                // Log password change if applicable
                if (passwordChanged) {
                    // FIX: Use proper constructor for User class
                    com.example.uts_pbo.AuthLogger.logPasswordChange(
                        new com.example.uts_pbo.User(id, email, firstName, lastName, username, hashedPassword, salt, role)
                    );
                }
                
                // Refresh the list
                loadUsersFromDatabase();
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error updating user", e.getMessage());
            return false;
        }
    }
    
    /**
     * Deletes a user from the database
     */
    private boolean deleteUser(int id) {
        try {
            // First get the user information for logging
            User userToDelete = null;
            for (User user : userList) {
                if (user.getId() == id) {
                    userToDelete = user;
                    break;
                }
            }
            
            // Then delete from database
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE_USER)) {
                
                pstmt.setInt(1, id);
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    // Log account deletion
                    if (userToDelete != null) {
                        // FIX: Use proper constructor for User class
                        com.example.uts_pbo.AuthLogger.logAccountDeletion(
                            new com.example.uts_pbo.User(
                                userToDelete.getId(),
                                userToDelete.getEmail(),
                                userToDelete.getFirstName(),
                                userToDelete.getLastName(),
                                userToDelete.getUsername(),
                                userToDelete.getPassword(),
                                userToDelete.getSalt(),
                                userToDelete.getRole()
                            )
                        );
                    }
                    
                    // Update succeeded, refresh the list
                    loadUsersFromDatabase();
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Error deleting user", e.getMessage());
            return false;
        }
    }
    
    /**
     * Handles navigation button clicks
     */
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
            } else if (source == refundproductsBtn) {
                fxmlFile = "RefundProducts.fxml";
            } else if (source == productsBtn) {
                fxmlFile = "ProductManagement.fxml";
            } else if (source == adminLogBtn) {
                fxmlFile = "AuthenticationLog.fxml";
            } else if (source == usersBtn) {
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

    @FXML
    private void handleSaveUser() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();
        
        // Validate inputs
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || 
            email.isEmpty() || password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", 
                    "Please fill in all fields", null);
            return;
        }
        
        boolean success;
        
        // Check if we're updating or creating
        if (!idField.getText().isEmpty()) {
            // Updating existing user
            try {
                int userId = Integer.parseInt(idField.getText());
                success = updateUser(userId, email, firstName, lastName, username, password, role);
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                            "User updated successfully", null);
                    clearForm();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                            "Failed to update user", null);
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Invalid user ID format", null);
            }
        } else {
            // Creating new user
            success = insertUser(email, firstName, lastName, username, password, role);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "User created successfully", null);
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to create user", null);
            }
        }
    }
    
    /**
     * Handles delete user button click
     */
    @FXML
    private void handleDeleteUser() {
        User selectedUser = UserTable.getSelectionModel().getSelectedItem();
        
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                    "Please select a user to delete", null);
            return;
        }
        
        // Confirm deletion
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete User");
        confirmDialog.setContentText("Are you sure you want to delete user: " + 
                selectedUser.getFirstName() + " " + selectedUser.getLastName() + 
                " (" + selectedUser.getUsername() + ")?");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete from database
            boolean success = deleteUser(selectedUser.getId());
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "User deleted successfully", null);
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to delete user", null);
            }
        }
    }
    
    /**
     * Display selected user details in form
     */
    private void displayUserDetails(User user) {
        idField.setText(String.valueOf(user.getId()));
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());
        passwordField.setText(user.getPassword());
        roleComboBox.setValue(user.getRole());
    }
    
    /**
     * Clear the form fields
     */
    private void clearForm() {
        idField.clear();
        firstNameField.clear();
        lastNameField.clear();
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Utility class for password operations
    private static class PasswordUtils {
        public static String generateSalt() {
            // Simple implementation - in production, use a secure random generator
            return java.util.UUID.randomUUID().toString();
        }
        
        public static String hashPassword(String password, String salt) {
            // Simple implementation - in production, use a secure hashing algorithm
            try {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
                String passwordWithSalt = password + salt;
                byte[] hashedBytes = md.digest(passwordWithSalt.getBytes());
                
                StringBuilder sb = new StringBuilder();
                for (byte b : hashedBytes) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            } catch (java.security.NoSuchAlgorithmException e) {
                e.printStackTrace();
                // Fallback to a simple representation if hashing fails
                return password + "-" + salt;
            }
        }
    }
    
    // Updated User class to match the new table structure
    public static class User {
        private final int id;
        private String email;
        private String firstName;
        private String lastName;
        private String username;
        private String password;
        private String role;
        private String salt;
        
        public User(int id, String email, String firstName, String lastName, 
                   String username, String password, String role, String salt) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.username = username;
            this.password = password;
            this.role = role;
            this.salt = salt;
        }
        
        // Getters and setters
        public int getId() { return id; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getSalt() { return salt; }
        public void setSalt(String salt) { this.salt = salt; }
    }
}