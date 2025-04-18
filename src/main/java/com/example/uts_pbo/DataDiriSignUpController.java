package com.example.uts_pbo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.control.ComboBox;

public class DataDiriSignUpController {
    private String email;

    @FXML private TextField usernameField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Label messageLabel;
    @FXML private ComboBox<String> roleComboBox;

    public void setEmail(String email) {
        this.email = email;
    }
    
    // This method is needed for compatibility with Main.java
    public void setUsername(String username) {
        this.email = username; // Treating username as email for now
    }

    @FXML
    private void initialize() {
        // Initialize the role combo box
        if (roleComboBox != null) {
            roleComboBox.getItems().addAll("user", "admin");
            roleComboBox.setValue("user"); // Set default value
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
    // Get user input
        String username = usernameField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox != null ? roleComboBox.getValue() : "user";
        
        // Validate input
        if (username.isEmpty() || firstName.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username, first name, and password are required");
            messageLabel.setTextFill(Color.RED);
            return;
        }
        
        // Validate password format
        if (!User.isValidPassword(password)) {
            messageLabel.setText("Password must be 6-20 characters with at least 1 uppercase letter, 1 lowercase letter, and 1 number");
            messageLabel.setTextFill(Color.RED);
            return;
        }
        
        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match");
            messageLabel.setTextFill(Color.RED);
            return;
        }
        
        // Check if username already exists
        if (UserDAO.usernameExists(username)) {
            messageLabel.setText("Username already exists. Please choose another.");
            messageLabel.setTextFill(Color.RED);
            return;
        }
        
        // Create user object with role - the constructor will handle password hashing
        User newUser = new User(firstName, lastName, email, username, password, role);
        
        // Save user to database
        if (UserDAO.createUser(newUser)) {
            messageLabel.setText("Account created successfully!");
            messageLabel.setTextFill(Color.GREEN);
            
            // Redirect to login page after 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        try {
                            Main.showLoginScreen();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            messageLabel.setText("Failed to create account. Please try again.");
            messageLabel.setTextFill(Color.RED);
        }
    }
}