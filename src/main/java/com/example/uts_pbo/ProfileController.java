package com.example.uts_pbo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import com.example.uts_pbo.DatabaseConnection;

public class ProfileController implements Initializable {

    @FXML private Button profileBtn;
    @FXML private Button cashierBtn;
    @FXML private Button productsBtn;
    @FXML private Button usersBtn;
    @FXML private Button adminLogBtn;
    
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField roleField;
    @FXML private PasswordField passwordField;
    @FXML private Button editPasswordBtn;
    @FXML private Button confirmChangesBtn;
    
    @FXML private CheckBox deleteAccountCheckbox;
    @FXML private Button deleteAccountBtn;
    
    // Current user information - would normally be set during login
    private int currentUserId = 1; // Default to user ID 1 for testing
    private String currentUsername = "";
    
    // Password validation pattern
    private final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$"
    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing ProfileController...");
        
        // Load user data
        loadUserData();
        
        // Set up password field to be read-only by default
        passwordField.setEditable(false);
        
        // Disable delete account button until checkbox is selected
        deleteAccountBtn.setDisable(true);
        
        // Add listener to checkbox to enable/disable delete button
        deleteAccountCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            deleteAccountBtn.setDisable(!newValue);
        });
    }
    
    private void loadUserData() {
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
            
            // For testing purposes, we'll load user with ID 1
            // In a real app, you would use the logged-in user's ID
            String query = "SELECT user_id, username, name, email, role, password FROM users WHERE user_id = ?";
            pst = conn.prepareStatement(query);
            pst.setInt(1, currentUserId);
            rs = pst.executeQuery();
            
            if (rs.next()) {
                // Store username for later use
                currentUsername = rs.getString("username");
                
                // Populate fields with user data
                nameField.setText(rs.getString("name"));
                usernameField.setText(currentUsername);
                emailField.setText(rs.getString("email"));
                roleField.setText(rs.getString("role"));
                
                // Set password field to masked value
                passwordField.setText("************");
            } else {
                System.err.println("No user found with ID: " + currentUserId);
                showAlert(Alert.AlertType.WARNING, "User Not Found", 
                        "Could not find user information. Please log in again.");
            }
            
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Could not load user information: " + e.getMessage());
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
    
    @FXML
    void handleEditPassword(ActionEvent event) {
        // Make password field editable and clear it
        passwordField.setEditable(true);
        passwordField.setText("");
        passwordField.requestFocus();
    }
    
    @FXML
    void handleConfirmChanges(ActionEvent event) {
        // Validate inputs
        if (nameField.getText().trim().isEmpty() || 
            usernameField.getText().trim().isEmpty() || 
            emailField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "All fields must be filled out.");
            return;
        }
        
        // Validate email format
        if (!isValidEmail(emailField.getText().trim())) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a valid email address.");
            return;
        }
        
        // Check if password was changed and validate if it was
        boolean passwordChanged = passwordField.isEditable();
        if (passwordChanged && !isValidPassword(passwordField.getText())) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", 
                    "Password must be 8-20 characters long and include at least one uppercase letter, " +
                    "one lowercase letter, and one number.");
            return;
        }
        
        // Save changes to database
        if (saveUserChanges(passwordChanged)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
            
            // Reset password field state
            if (passwordChanged) {
                passwordField.setEditable(false);
                passwordField.setText("************");
            }
        }
    }
    
    private boolean saveUserChanges(boolean passwordChanged) {
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to database");
                return false;
            }
            
            // Check if username already exists (if changed)
            if (!usernameField.getText().equals(currentUsername)) {
                if (isUsernameExists(conn, usernameField.getText())) {
                    showAlert(Alert.AlertType.WARNING, "Username Taken", 
                            "This username is already in use. Please choose another one.");
                    return false;
                }
            }
            
            // Prepare SQL statement based on whether password was changed
            String sql;
            if (passwordChanged) {
                sql = "UPDATE users SET name = ?, username = ?, email = ?, password = ? WHERE user_id = ?";
            } else {
                sql = "UPDATE users SET name = ?, username = ?, email = ? WHERE user_id = ?";
            }
            
            pst = conn.prepareStatement(sql);
            pst.setString(1, nameField.getText().trim());
            pst.setString(2, usernameField.getText().trim());
            pst.setString(3, emailField.getText().trim());
            
            if (passwordChanged) {
                // In a real app, you would hash the password before storing it
                pst.setString(4, passwordField.getText());
                pst.setInt(5, currentUserId);
            } else {
                pst.setInt(4, currentUserId);
            }
            
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Could not update profile: " + e.getMessage());
            return false;
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    
    private boolean isUsernameExists(Connection conn, String username) throws SQLException {
        PreparedStatement pst = null;
        ResultSet rs = null;
        
        try {
            String query = "SELECT COUNT(*) FROM users WHERE username = ? AND user_id != ?";
            pst = conn.prepareStatement(query);
            pst.setString(1, username);
            pst.setInt(2, currentUserId);
            rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
            return false;
        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }
    }
    
    @FXML
    void handleDeleteAccount(ActionEvent event) {
        if (!deleteAccountCheckbox.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "Confirmation Required", 
                    "Please check the confirmation box to delete your account.");
            return;
        }
        
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Account Deletion");
        confirmDialog.setHeaderText("Are you sure you want to delete your account?");
        confirmDialog.setContentText("This action cannot be undone.");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Proceed with account deletion
            if (deleteUserAccount()) {
                showAlert(Alert.AlertType.INFORMATION, "Account Deleted", 
                        "Your account has been successfully deleted.");
                
                // Navigate to login screen or exit
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
                    Stage stage = (Stage) deleteAccountBtn.getScene().getWindow();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException e) {
                    System.err.println("Error navigating to login screen: " + e.getMessage());
                }
            }
        }
    }
    
    private boolean deleteUserAccount() {
        Connection conn = null;
        PreparedStatement pst = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to database");
                return false;
            }
            
            // Delete user from database
            String sql = "DELETE FROM users WHERE user_id = ?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, currentUserId);
            
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Could not delete account: " + e.getMessage());
            return false;
        } finally {
            try {
                if (pst != null) pst.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    
    @FXML
    void handleNavigation(ActionEvent event) {
        Object source = event.getSource();
        
        try {
            String fxmlFile = "";
            
            if (source == productsBtn) {
                fxmlFile = "ProductManagement.fxml";
            } else if (source == cashierBtn) {
                fxmlFile = "Cashier.fxml";
            } else if (source == usersBtn) {
                fxmlFile = "UserManagement.fxml";
            } else if (source == adminLogBtn) {
                fxmlFile = "AuthenticationLog.fxml";
            } else if (source == profileBtn) {
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
    
    // Helper methods
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
    
    private boolean isValidPassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
