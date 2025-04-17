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
        // Create a new User object with data from the form
        User newUser = new User(
                FieldFirstName.getText(),
                LastNameField.getText(),
                EmailFieldText.getText(),
                UsernameDataDiri.getText(),
                PasswordDatDiri.getText()
        );
    
        // Validate the user data
        if (!newUser.isValid()) {
            showAlert("All fields must be filled in!", "Please fill in all the required fields.", AlertType.ERROR);
            return;
        }
    
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