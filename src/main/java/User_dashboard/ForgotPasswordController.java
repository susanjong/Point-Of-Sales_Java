package User_dashboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * FIXED: ForgotPasswordController yang terintegrasi dengan AuthLogger
 *
 * PERUBAHAN:
 * 1. Menggunakan UserDAO untuk ambil user
 * 2. Menggunakan PasswordHasher untuk hash password
 * 3. Menggunakan AuthLogger untuk logging
 * 4. Logging ke password_reset_logs tetap menggunakan direct SQL
 */
public class ForgotPasswordController {

    @FXML
    private TextField EmailFieldText;

    @FXML
    private PasswordField NewPassword;

    @FXML
    private PasswordField ConfirmNewPassword;

    @FXML
    private Button ButtonResetPassword;

    @FXML
    private Hyperlink HyperlinkBackToLogin;

    @FXML
    private Label MessageLabel;

    // Konstanta untuk keamanan
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    /**
     * Method untuk mereset password
     */
    @FXML
    public void resetPasswordOnAction(ActionEvent event) {
        // Clear previous message
        MessageLabel.setText("");

        try {
            // Validasi input
            String email = EmailFieldText.getText().trim();
            String newPassword = NewPassword.getText();
            String confirmPassword = ConfirmNewPassword.getText();

            // === VALIDASI LENGKAP ===

            // 1. Cek field kosong
            if (email.isEmpty()) {
                showMessage("Email is required!", "#CD0404");
                EmailFieldText.requestFocus();
                return;
            }

            if (newPassword.isEmpty()) {
                showMessage("New password is required!", "#CD0404");
                NewPassword.requestFocus();
                return;
            }

            if (confirmPassword.isEmpty()) {
                showMessage("Please confirm your password!", "#CD0404");
                ConfirmNewPassword.requestFocus();
                return;
            }

            // 2. Validasi format email
            if (!isValidEmail(email)) {
                showMessage("Invalid email format! Example: user@example.com", "#CD0404");
                EmailFieldText.requestFocus();
                return;
            }

            // 3. Cek password match
            if (!newPassword.equals(confirmPassword)) {
                showMessage("Passwords do not match!", "#CD0404");
                ConfirmNewPassword.clear();
                ConfirmNewPassword.requestFocus();
                return;
            }

            // 4. Validasi kekuatan password menggunakan User.isValidPassword()
            if (!User.isValidPassword(newPassword)) {
                showMessage("Password must be 6-20 characters with uppercase, lowercase & digit!", "#CD0404");
                NewPassword.requestFocus();
                return;
            }

            // 5. Cek password tidak boleh sama dengan email
            if (newPassword.equalsIgnoreCase(email)) {
                showMessage("Password cannot be same as email!", "#CD0404");
                NewPassword.requestFocus();
                return;
            }

            // === PROSES RESET PASSWORD ===

            // Disable button saat proses (prevent double click)
            ButtonResetPassword.setDisable(true);
            showMessage("Processing...", "#FFA500"); // Orange color

            ResetResult result = resetPasswordInDatabase(email, newPassword);

            if (result.isSuccess()) {
                showMessage("✓ Password reset successful!", "#5B8336");

                // Clear fields
                EmailFieldText.clear();
                NewPassword.clear();
                ConfirmNewPassword.clear();

                // Optional: Auto redirect ke login setelah 2 detik
                redirectToLoginAfterDelay(event);

            } else {
                showMessage("✗ " + result.getMessage(), "#CD0404");

                // Log detail error ke console untuk debugging
                System.err.println("Password reset failed: " + result.getMessage());
                if (result.getDetailError() != null) {
                    System.err.println("Detail: " + result.getDetailError());
                }

                // Focus ke email field untuk retry
                EmailFieldText.requestFocus();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("System error! Please try again later.", "#CD0404");
            System.err.println("Unexpected error in resetPasswordOnAction: " + e.getMessage());

        } finally {
            // Re-enable button
            ButtonResetPassword.setDisable(false);
        }
    }

    /**
     * UPDATED: Reset password menggunakan UserDAO dan PasswordHasher
     */
    private ResetResult resetPasswordInDatabase(String email, String newPassword) {
        try {
            // === STEP 1: Cek email dan ambil user menggunakan UserDAO ===
            User user = getUserByEmail(email);

            if (user == null) {
                // Email tidak ditemukan - log failed attempt
                logPasswordResetToDatabase(email, -1, null, "email_not_found",
                        "Email not registered");
                return new ResetResult(false, "Email not found in system!",
                        "Email: " + email + " is not registered");
            }

            // === STEP 2: Hash password dengan PasswordHasher ===
            String salt = user.getSalt();
            if (salt == null || salt.isEmpty()) {
                // Generate salt baru jika tidak ada
                salt = PasswordHasher.generateSalt();
                System.out.println("WARNING: User " + user.getUsername() +
                        " has no salt! Generated new salt.");
            }

            String hashedPassword = PasswordHasher.hashPassword(newPassword, salt);

            if (hashedPassword == null || hashedPassword.isEmpty()) {
                return new ResetResult(false, "Password encryption failed!",
                        "Hash function returned empty");
            }

            // === STEP 3: Update password di database ===
            boolean updateSuccess = updateUserPasswordInDatabase(user.getId(), hashedPassword, salt);

            if (!updateSuccess) {
                logPasswordResetToDatabase(email, user.getId(), user.getUsername(),
                        "failed", "Database update failed");
                return new ResetResult(false, "Failed to update password!",
                        "Database UPDATE statement failed");
            }

            // === STEP 4: Log successful reset menggunakan AuthLogger ===
            logPasswordResetToDatabase(email, user.getId(), user.getUsername(),
                    "success", null);

            // Log ke authentication log menggunakan AuthLogger
            logPasswordChangeActivity(user);

            return new ResetResult(true, "Password reset successful!", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResetResult(false, "Unexpected error!",
                    "Exception: " + e.getClass().getName() +
                            " - " + e.getMessage());
        }
    }

    /**
     * Ambil user dari database berdasarkan email
     */
    private User getUserByEmail(String email) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            String query = "SELECT id, email, first_name, last_name, username, password, salt, role " +
                    "FROM users WHERE email = ?";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("salt"),
                        rs.getString("role")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error getting user by email: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                // Connection managed by pool, no need to close
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Update password user di database
     */
    private boolean updateUserPasswordInDatabase(int userId, String hashedPassword, String salt) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            String updateQuery = "UPDATE users SET password = ?, salt = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE id = ?";

            stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, hashedPassword);
            stmt.setString(2, salt);
            stmt.setInt(3, userId);

            int rowsAffected = stmt.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating user password: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Log password reset activity ke tabel password_reset_logs
     */
    private void logPasswordResetToDatabase(String email, int userId, String username,
                                            String status, String errorMessage) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            String logQuery = "INSERT INTO password_reset_logs " +
                    "(email, reset_date, ip_address, status, user_id, error_message) " +
                    "VALUES (?, CURRENT_TIMESTAMP, ?, ?, ?, ?)";

            stmt = conn.prepareStatement(logQuery);
            stmt.setString(1, email);
            stmt.setString(2, getClientIP());
            stmt.setString(3, status);

            if (userId > 0) {
                stmt.setInt(4, userId);
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }

            if (errorMessage != null) {
                stmt.setString(5, errorMessage);
            } else {
                stmt.setNull(5, java.sql.Types.VARCHAR);
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            // Log error tapi jangan throw exception
            System.err.println("Error logging password reset: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Log password change activity menggunakan AuthLogger
     */
    private void logPasswordChangeActivity(User user) {
        try {
            // Menggunakan AuthLogger yang sudah ada
            AuthLogger.logPasswordChange(user);
        } catch (Exception e) {
            System.err.println("Error logging password change: " + e.getMessage());
        }
    }

    /**
     * Method validasi email
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    /**
     * Ambil IP client
     */
    private String getClientIP() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            return localhost.getHostAddress();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    /**
     * Method untuk tampilkan pesan
     */
    private void showMessage(String message, String color) {
        MessageLabel.setText(message);
        MessageLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    /**
     * Method kembali ke login
     */
    @FXML
    public void backToLoginOnAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) HyperlinkBackToLogin.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Error loading login page!", "#CD0404");
        }
    }

    /**
     * Static method untuk navigate dari login
     */
    public static void navigateToForgotPassword(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    ForgotPasswordController.class.getResource("ForgotPassword.fxml")
            );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Forgot Password page: " + e.getMessage());
        }
    }

    /**
     * Auto redirect ke login setelah sukses
     */
    private void redirectToLoginAfterDelay(ActionEvent event) {
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Wait 2 seconds
                javafx.application.Platform.runLater(() -> backToLoginOnAction(event));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ========== INNER CLASS ==========

    /**
     * Class untuk menyimpan hasil reset password
     */
    private static class ResetResult {
        private boolean success;
        private String message;
        private String detailError;

        public ResetResult(boolean success, String message, String detailError) {
            this.success = success;
            this.message = message;
            this.detailError = detailError;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getDetailError() {
            return detailError;
        }
    }
}
