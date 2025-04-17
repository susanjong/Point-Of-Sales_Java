package com.example.uts_pbo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class LoginController {
    @FXML
    private Button ButtonContinue;

    @FXML
    private PasswordField EnterPassword;

    @FXML
    private Hyperlink ForgotPassword;

    @FXML
    private Hyperlink HyperlinkCreateAcc;

    @FXML
    private Label LoginMessageLabel;

    @FXML
    private TextField UsernameFieldText;

    // Add a static variable to store the logged-in user
    private static User currentUser;

    // Getter for the current user
    public static User getCurrentUser() {
        return currentUser;
    }

    @FXML
    void ButtonContinueOnAction(ActionEvent event) {
        validateLogin();
    }

    @FXML
    void forgotPasswordOnAction(ActionEvent event) {
        try {
            Main.getPrimaryStage().setTitle("Forgot Password");
            Main.showLoginScreen(); // Redirect to OTP email screen when implemented
        } catch (Exception e) {
            LoginMessageLabel.setText("Error: Could not load forgot password page");
            LoginMessageLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    @FXML
    void createAccountOnAction(ActionEvent event) {
        try {
            Main.showSignUpScreen();
        } catch (Exception e) {
            LoginMessageLabel.setText("Error: Could not load signup page");
            LoginMessageLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    private void validateLogin() {
        // Check if username and password are empty
        if (UsernameFieldText.getText().isEmpty() && EnterPassword.getText().isEmpty()) {
            LoginMessageLabel.setText("Username and password cannot be empty!");
            LoginMessageLabel.setTextFill(Color.RED);
        }
        // Check if only username is empty
        else if (UsernameFieldText.getText().isEmpty()) {
            LoginMessageLabel.setText("Username cannot be empty!");
            LoginMessageLabel.setTextFill(Color.RED);
        }
        // Check if only password is empty
        else if (EnterPassword.getText().isEmpty()) {
            LoginMessageLabel.setText("Password cannot be empty!");
            LoginMessageLabel.setTextFill(Color.RED);
        }
        // If all fields are filled, validate against the database
        else {
            authenticateUser();
        }
    }
    
    private void authenticateUser() {
        String usernameOrEmail = UsernameFieldText.getText();
        String password = EnterPassword.getText();
        
        // Try to authenticate user
        User user = UserDAO.getUserByUsernameOrEmail(usernameOrEmail);
        
        if (user != null && user.verifyPassword(password)) {
            // Login successful
            currentUser = user;
            UserSession.setCurrentUser(user);
            
            LoginMessageLabel.setText("Login successful!");
            LoginMessageLabel.setTextFill(Color.GREEN);
            
            // Navigate based on user role
            try {
                if (user.isAdmin()) {
                    // Navigate to admin dashboard
                    Main.showProductManagement();
                } else {
                    // Navigate to user dashboard or shopping interface
                    Main.showCashier();
                }
            } catch (Exception e) {
                e.printStackTrace();
                LoginMessageLabel.setText("Error navigating to dashboard");
                LoginMessageLabel.setTextFill(Color.RED);
            }
        } else {
            // Login failed
            LoginMessageLabel.setText("Invalid username or password");
            LoginMessageLabel.setTextFill(Color.RED);
        }
    }
}