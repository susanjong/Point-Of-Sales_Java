package User_dashboard;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

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
    @FXML private Button refundproductsBtn;
    @FXML private Button logoutBtn;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private User currentUser;

    private static final double FIXED_WIDTH = 1280;
    private static final double FIXED_HEIGHT = 800;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[ProfileController] initialize() called");

        if (!UserSession.isLoggedIn()) {
            System.out.println("[ProfileController] No user session, redirecting to login");
            redirectToLogin();
            return;
        }

        currentUser = UserSession.getCurrentUser();
        System.out.println("[ProfileController] User loaded: id=" + currentUser.getId() +
                ", username=" + currentUser.getUsername());

        if (firstNameField != null) firstNameField.setText(currentUser.getFirstName());
        if (lastNameField != null) lastNameField.setText(currentUser.getLastName());
        if (emailField != null) emailField.setText(currentUser.getEmail());
        if (usernameField != null) usernameField.setText(currentUser.getUsername());

        setupFixedWindow();
    }

    private void setupFixedWindow() {
        Platform.runLater(() -> {
            Stage stage = getStage();
            if (stage != null) {
                stage.setMinWidth(FIXED_WIDTH);
                stage.setMinHeight(FIXED_HEIGHT);
                stage.setWidth(FIXED_WIDTH);
                stage.setHeight(FIXED_HEIGHT);
                stage.setResizable(false);
                stage.centerOnScreen();
                System.out.println("[ProfileController] Fixed window size set: " + FIXED_WIDTH + "x" + FIXED_HEIGHT);
            }
        });
    }

    private Stage getStage() {
        if (firstNameField != null && firstNameField.getScene() != null) {
            return (Stage) firstNameField.getScene().getWindow();
        }
        if (profileBtn != null && profileBtn.getScene() != null) {
            return (Stage) profileBtn.getScene().getWindow();
        }
        return null;
    }

    @FXML
    private void handleUpdatePassword(ActionEvent event) {
        System.out.println("[ProfileController] Changing password...");

        String newPwd = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (newPwd.isEmpty() || confirm.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Password fields cannot be empty!");
            return;
        }

        if (!newPwd.equals(confirm)) {
            System.out.println("[ProfileController] Passwords do not match");
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Passwords do not match!");
            return;
        }

        if (!User.isValidPassword(newPwd)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Password must be 6-20 characters long and include:\n" +
                            "• At least one uppercase letter\n" +
                            "• At least one lowercase letter\n" +
                            "• At least one digit");
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
        if (success) {
            currentUser = updated;
            UserSession.setCurrentUser(updated);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully!");
            newPasswordField.clear();
            confirmPasswordField.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to change password. Please try again.");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout Confirmation");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.setContentText("You will need to login again to continue using the application.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (UserSession.isLoggedIn()) {
                    User user = UserSession.getCurrentUser();
                    AuthLogger.logLogout(user);
                }
                UserSession.logout();
                showAlert(Alert.AlertType.INFORMATION, "Logged Out", "You have been logged out successfully.");
                redirectToLogin();
            }
        });
    }

    @FXML
    private void handleDeleteAccount(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.WARNING);
        confirm.setTitle("Delete Account");
        confirm.setHeaderText("⚠️ PERMANENT ACTION");
        confirm.setContentText("Are you absolutely sure you want to delete your account?\n" +
                "This action cannot be undone and all your data will be permanently lost.");

        ButtonType deleteButton = new ButtonType("Delete Account", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(deleteButton, cancelButton);

        confirm.showAndWait().ifPresent(response -> {
            if (response == deleteButton) {
                AuthLogger.logLogout(currentUser);
                boolean success = UserDAO.deleteUser(currentUser.getId());
                if (success) {
                    UserSession.logout();
                    showAlert(Alert.AlertType.INFORMATION, "Account Deleted",
                            "Your account has been permanently deleted.");
                    redirectToLogin();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to delete account. Please try again or contact support.");
                }
            }
        });
    }

    @FXML
    void handleNavigation(ActionEvent event) {
        Object source = event.getSource();
        String fxmlFile = "";

        if (source == productsBtn) {
            fxmlFile = "/Admin_View/ProductManagement.fxml";
        } else if (source == bundleproductsBtn) {
            fxmlFile = "/Admin_View/BundleProducts.fxml";
        } else if (source == refundproductsBtn) {
            fxmlFile = "/Admin_View/RefundProducts.fxml";
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
            try {
                loadScene(fxmlFile, (Stage) ((Button) source).getScene().getWindow());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not navigate: " + e.getMessage());
            }
        }
    }

    private void loadScene(String fxmlFile, Stage stage) throws IOException {
        URL url = getClass().getResource(fxmlFile);
        if (url == null) {
            url = getClass().getClassLoader().getResource(fxmlFile.startsWith("/") ? fxmlFile.substring(1) : fxmlFile);
        }

        if (url == null) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not find FXML file: " + fxmlFile);
            return;
        }

        Parent root = FXMLLoader.load(url);
        stage.setScene(new Scene(root));
        stage.setMinWidth(FIXED_WIDTH);
        stage.setMinHeight(FIXED_HEIGHT);
        stage.setWidth(FIXED_WIDTH);
        stage.setHeight(FIXED_HEIGHT);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    private void redirectToLogin() {
        try {
            URL loginUrl = getClass().getResource("/User_dashboard/Login.fxml");
            if (loginUrl == null) {
                loginUrl = getClass().getClassLoader().getResource("User_dashboard/Login.fxml");
                if (loginUrl == null) {
                    System.err.println("[ProfileController] Could not find Login.fxml file!");
                    return;
                }
            }

            Parent root = FXMLLoader.load(loginUrl);
            Stage stage = getStage();
            if (stage == null) {
                System.err.println("[ProfileController] Could not get stage reference");
                return;
            }

            stage.setScene(new Scene(root));
            stage.setMinWidth(FIXED_WIDTH);
            stage.setMinHeight(FIXED_HEIGHT);
            stage.setWidth(FIXED_WIDTH);
            stage.setHeight(FIXED_HEIGHT);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("[ProfileController] Failed to load Login.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
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
