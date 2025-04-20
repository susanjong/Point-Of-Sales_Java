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
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

     @FXML private Button profileBtn;
     @FXML private Button cashierBtn;
     @FXML private Button productsBtn;
     @FXML private Button bundleproductsBtn;
     @FXML private Button usersBtn;
     @FXML private Button adminLogBtn;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[DEBUG] initialize() called");
        if (!UserSession.isLoggedIn()) {
            System.out.println("[DEBUG] No user session, redirecting to login");
            redirectToLogin();
            return;
        }
        currentUser = UserSession.getCurrentUser();
        System.out.println("[DEBUG] Loaded user: id=" + currentUser.getId() + ", username=" + currentUser.getUsername());
        firstNameField.setText(currentUser.getFirstName());
        lastNameField.setText(currentUser.getLastName());
        emailField.setText(currentUser.getEmail());
        usernameField.setText(currentUser.getUsername());
    }

    @FXML
    private void handleUpdateProfile(ActionEvent event) {
        System.out.println("[DEBUG] handleUpdateProfile() invoked");
        System.out.println("[DEBUG] New values - firstName: " + firstNameField.getText() + ", lastName: " + lastNameField.getText());
        currentUser.setFirstName(firstNameField.getText());
        currentUser.setLastName(lastNameField.getText());
        currentUser.setEmail(emailField.getText());
        currentUser.setUsername(usernameField.getText());

        boolean success = UserDAO.updateUser(currentUser);
        System.out.println("[DEBUG] updateUser returned: " + success);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Profile updated successfully.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed to update profile.");
        }
    }

    @FXML
    private void handleUpdatePassword(ActionEvent event) {
        System.out.println("[DEBUG] handleUpdatePassword() invoked");
        String newPwd = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        System.out.println("[DEBUG] Provided newPwd length=" + newPwd.length());
        if (!newPwd.equals(confirm)) {
            System.out.println("[DEBUG] Passwords do not match");
            showAlert(Alert.AlertType.WARNING, "Passwords do not match.");
            return;
        }
        if (!User.isValidPassword(newPwd)) {
            System.out.println("[DEBUG] Password validation failed");
            showAlert(Alert.AlertType.WARNING, "Password must be 6-20 chars, include upper, lower & digit.");
            return;
        }
        User updated = new User(
            currentUser.getFirstName(),
            currentUser.getLastName(),
            currentUser.getEmail(),
            currentUser.getUsername(),
            newPwd,
            currentUser.getRole()
        );
        updated.setId(currentUser.getId());

        boolean success = UserDAO.updateUser(updated);
        System.out.println("[DEBUG] change password updateUser returned: " + success);
        if (success) {
            UserSession.setCurrentUser(updated);
            showAlert(Alert.AlertType.INFORMATION, "Password changed successfully.");
            newPasswordField.clear();
            confirmPasswordField.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed to change password.");
        }
    }

    @FXML
    private void handleDeleteAccount(ActionEvent event) {
        System.out.println("[DEBUG] handleDeleteAccount() invoked");
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete your account? This cannot be undone.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("[DEBUG] Deletion confirmed by user");
                boolean success = UserDAO.deleteUser(currentUser.getId());
                System.out.println("[DEBUG] deleteUser returned: " + success);
                if (success) {
                    UserSession.logout();
                    showAlert(Alert.AlertType.INFORMATION, "Account deleted.");
                    redirectToLogin();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failed to delete account.");
                }
            }
        });
    }

    @FXML
    void handleNavigation(ActionEvent event) {
        Object source = event.getSource();
        
        try {
            String fxmlFile = "";
            
            if (source == productsBtn) {
                fxmlFile = "/Admin_View/ProductManagement.fxml";
            } else if (source == bundleproductsBtn) {
                fxmlFile = "/Admin_View/BundleProducts.fxml";
            } else if (source == cashierBtn) {
                fxmlFile = "/Admin_View/Cashier.fxml";
            } else if (source == usersBtn) {
                fxmlFile = "/Admin_View/UserManagement.fxml";
            } else if (source == adminLogBtn) {
                fxmlFile = "/Admin_View/AuthenticationLog.fxml";
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
    private void redirectToLogin() {
        System.out.println("[DEBUG] redirectToLogin() invoked");
        try {
            Parent root = FXMLLoader.load(getClass().getResource("signin.fxml"));
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        System.out.println("[DEBUG] showAlert: " + message);
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
}