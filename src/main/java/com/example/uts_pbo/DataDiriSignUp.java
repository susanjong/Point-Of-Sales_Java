package com.example.uts_pbo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class DataDiriSignUp {

    @FXML
    private TextField EmailFieldText;
    @FXML
    private TextField FieldFirstName;
    @FXML
    private TextField LastNameField;
    @FXML
    private TextField UsernameDataDiri;
    @FXML
    private PasswordField PasswordDatDiri;

    // Handle sign-up logic using OOP principles
    @FXML
    private void handleSignUp(ActionEvent event) {
    // Get the data from form fields
    String firstName = FieldFirstName.getText();
    String lastName = LastNameField.getText();
    String email = EmailFieldText.getText();
    String username = UsernameDataDiri.getText();
    String password = PasswordDatDiri.getText();
    
    // Check if all fields are filled
    if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || 
        username.isEmpty() || password.isEmpty()) {
        showAlert("All fields must be filled in!", "Please fill in all the required fields.", AlertType.ERROR);
        return;
    }
    
        // Validate password format
        if (!User.isValidPassword(password)) {
            showAlert("Invalid Password", 
                    "Password must be 6-20 characters long and include at least:\n" +
                    "- 1 uppercase letter\n" +
                    "- 1 lowercase letter\n" +
                    "- 1 number", 
                    AlertType.ERROR);
                    return;
                }
    
    // Create a new User object with data from the form
    User newUser = new User(
            firstName,
            lastName,
            email,
            username,
            password
        );

    // Save user to database
    if (UserDAO.createUser(newUser)) {
        showAlert("Sign-Up Successful", "Welcome " + newUser.getFirstName() + "!", AlertType.INFORMATION);
        
        // Clear the fields
        clearFields();
        
        // Redirect to login page
        try {
            Main.showLoginScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    } else {
        showAlert("Registration Failed", "Could not complete registration. Please try again.", AlertType.ERROR);
    }
}

    private void showAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        EmailFieldText.clear();
        FieldFirstName.clear();
        LastNameField.clear();
        UsernameDataDiri.clear();
        PasswordDatDiri.clear();
    }
}