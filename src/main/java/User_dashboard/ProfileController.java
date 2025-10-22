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

        // Populate fields dengan data user
        if (firstNameField != null) firstNameField.setText(currentUser.getFirstName());
        if (lastNameField != null) lastNameField.setText(currentUser.getLastName());
        if (emailField != null) emailField.setText(currentUser.getEmail());
        if (usernameField != null) usernameField.setText(currentUser.getUsername());

        // Setup window maximize & centered
        setupWindowMaximized();
    }

    /**
     * ✅ Setup window untuk selalu maximize dan center di semua ukuran layar
     */
    private void setupWindowMaximized() {
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) firstNameField.getScene().getWindow();
                if (stage != null) {
                    // Get screen bounds
                    Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

                    // Set minimum size responsif
                    stage.setMinWidth(1024);
                    stage.setMinHeight(768);

                    // Set posisi ke center screen
                    stage.setX(screenBounds.getMinX());
                    stage.setY(screenBounds.getMinY());
                    stage.setWidth(screenBounds.getWidth());
                    stage.setHeight(screenBounds.getHeight());

                    // Maximize window
                    stage.setMaximized(true);
                    stage.centerOnScreen();

                    // Lock maximized state - prevent user dari resize
                    stage.maximizedProperty().addListener((obs, wasMax, isMax) -> {
                        if (!isMax) {
                            Platform.runLater(() -> {
                                stage.setMaximized(true);
                                stage.centerOnScreen();
                            });
                        }
                    });

                    System.out.println("[ProfileController] Window maximized and centered");
                }
            } catch (Exception e) {
                System.err.println("[ProfileController] Error setting window: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleUpdatePassword(ActionEvent event) {
        System.out.println("[ProfileController] Changing password...");

        String newPwd = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        // Validasi password tidak kosong
        if (newPwd.isEmpty() || confirm.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Password fields cannot be empty!");
            return;
        }

        // Validasi password match
        if (!newPwd.equals(confirm)) {
            System.out.println("[ProfileController] Passwords do not match");
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Passwords do not match!");
            return;
        }

        // Validasi password strength
        if (!User.isValidPassword(newPwd)) {
            System.out.println("[ProfileController] Password validation failed");
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                    "Password must be 6-20 characters long and include:\n" +
                            "• At least one uppercase letter\n" +
                            "• At least one lowercase letter\n" +
                            "• At least one digit");
            return;
        }

        // Create updated user with new password
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
        System.out.println("[ProfileController] Password change result: " + success);

        if (success) {
            currentUser = updated;
            UserSession.setCurrentUser(updated);
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Password changed successfully!");
            newPasswordField.clear();
            confirmPasswordField.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to change password. Please try again.");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("[ProfileController] Logout initiated");

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout Confirmation");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.setContentText("You will need to login again to continue using the application.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("[ProfileController] Logout confirmed by user");

                // Log the logout action
                if (UserSession.isLoggedIn()) {
                    User user = UserSession.getCurrentUser();
                    AuthLogger.logLogout(user);
                    System.out.println("[ProfileController] User logged out: " + user.getUsername());
                }

                // Clear session
                UserSession.logout();

                // Show confirmation
                showAlert(Alert.AlertType.INFORMATION, "Logged Out",
                        "You have been logged out successfully.");

                // Redirect to login
                redirectToLogin();
            } else {
                System.out.println("[ProfileController] Logout cancelled by user");
            }
        });
    }

    @FXML
    private void handleDeleteAccount(ActionEvent event) {
        System.out.println("[ProfileController] Delete account initiated");

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
                System.out.println("[ProfileController] Account deletion confirmed");

                // Log before deleting
                if (UserSession.isLoggedIn()) {
                    AuthLogger.logLogout(currentUser);
                }

                boolean success = UserDAO.deleteUser(currentUser.getId());
                System.out.println("[ProfileController] Delete result: " + success);

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
        System.out.println("[ProfileController] Navigation triggered");

        try {
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
                System.out.println("[ProfileController] Already on Profile page");
                return;
            }

            if (!fxmlFile.isEmpty()) {
                loadScene(fxmlFile, (Stage) ((Button) source).getScene().getWindow());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not navigate to the requested page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ✅ Load scene baru dengan window maximize & centered otomatis
     */
    private void loadScene(String fxmlFile, Stage stage) throws IOException {
        System.out.println("[ProfileController] Loading scene: " + fxmlFile);

        URL url = getClass().getResource(fxmlFile);
        if (url == null) {
            String altPath = fxmlFile.replace("/com/example/uts_pbo/", "/");
            url = getClass().getResource(altPath);

            if (url == null) {
                String noSlashPath = fxmlFile.substring(1);
                url = getClass().getClassLoader().getResource(noSlashPath);

                if (url == null) {
                    showAlert(Alert.AlertType.ERROR, "Navigation Error",
                            "Could not find FXML file: " + fxmlFile);
                    return;
                }
            }
        }

        Parent root = FXMLLoader.load(url);

        // Get screen bounds
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        // Set minimum size
        stage.setMinWidth(1024);
        stage.setMinHeight(768);

        // Set scene
        stage.setScene(new Scene(root));

        // ✅ Maximize dan center otomatis di semua ukuran layar
        Platform.runLater(() -> {
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());
            stage.setMaximized(true);
            stage.centerOnScreen();

            // Lock maximized state
            stage.maximizedProperty().addListener((obs, wasMax, isMax) -> {
                if (!isMax) {
                    Platform.runLater(() -> {
                        stage.setMaximized(true);
                        stage.centerOnScreen();
                    });
                }
            });

            System.out.println("[ProfileController] Scene loaded, maximized and centered");
        });

        stage.show();
    }

    /**
     * ✅ Redirect ke Login dengan maximize dan center otomatis
     */
    private void redirectToLogin() {
        System.out.println("[ProfileController] Redirecting to login...");
        try {
            URL loginUrl = getClass().getResource("/User_dashboard/Login.fxml");

            if (loginUrl == null) {
                loginUrl = getClass().getResource("Login.fxml");

                if (loginUrl == null) {
                    loginUrl = getClass().getClassLoader().getResource("User_dashboard/Login.fxml");

                    if (loginUrl == null) {
                        System.err.println("[ProfileController] Could not find Login.fxml file!");
                        return;
                    }
                }
            }

            Parent root = FXMLLoader.load(loginUrl);
            Stage stage = (Stage) firstNameField.getScene().getWindow();

            // Get screen bounds
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // Set minimum size
            stage.setMinWidth(1024);
            stage.setMinHeight(768);

            // Set scene
            stage.setScene(new Scene(root));

            // ✅ Maximize dan center otomatis
            Platform.runLater(() -> {
                stage.setX(screenBounds.getMinX());
                stage.setY(screenBounds.getMinY());
                stage.setWidth(screenBounds.getWidth());
                stage.setHeight(screenBounds.getHeight());
                stage.setMaximized(true);
                stage.centerOnScreen();

                // Lock maximized state
                stage.maximizedProperty().addListener((obs, wasMax, isMax) -> {
                    if (!isMax) {
                        Platform.runLater(() -> {
                            stage.setMaximized(true);
                            stage.centerOnScreen();
                        });
                    }
                });

                System.out.println("[ProfileController] Login window maximized and centered");
            });

            stage.show();

        } catch (IOException e) {
            System.err.println("[ProfileController] Failed to load Login.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ✅ Alert Helper - Short version
     */
    private void showAlert(Alert.AlertType type, String message) {
        System.out.println("[ProfileController] Alert: " + message);
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * ✅ Alert Helper - Full version
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}