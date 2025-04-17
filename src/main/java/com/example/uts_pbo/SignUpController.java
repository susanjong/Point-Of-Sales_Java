package com.example.uts_pbo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class SignUpController {
    @FXML
    private TextField emailField;
    
    @FXML
    private Button continueButton;
    
    @FXML
    private Hyperlink loginLink;
    
    @FXML
    private Label messageLabel;
    
    @FXML
    void continueButtonOnAction(ActionEvent event) {
        String email = emailField.getText().trim();
        
        if (email.isEmpty()) {
            messageLabel.setText("Email cannot be empty!");
            messageLabel.setTextFill(Color.RED);
            return;
        }
        
        if (!isValidEmail(email)) {
            messageLabel.setText("Please enter a valid email address!");
            messageLabel.setTextFill(Color.RED);
            return;
        }
        
        // Check if email already exists in database
        if (emailExists(email)) {
            messageLabel.setText("Email already registered. Please use a different email.");
            messageLabel.setTextFill(Color.RED);
            return;
        }
        
        // Email is valid, proceed to the next registration step
        try {
            Main.showDataDiriSignUp(email);
        } catch (Exception e) {
            messageLabel.setText("Error loading registration form: " + e.getMessage());
            messageLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }
    
    @FXML
    void loginLinkOnAction(ActionEvent event) {
        try {
            Main.showLoginScreen();
        } catch (Exception e) {
            messageLabel.setText("Error: Could not load login page");
            messageLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }
    
    private boolean isValidEmail(String email) {
        // Simple email validation using regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
    
    private boolean emailExists(String email) {
        try (java.sql.Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Database error: " + e.getMessage());
            messageLabel.setTextFill(Color.RED);
        }
        
        return false;
    }
}