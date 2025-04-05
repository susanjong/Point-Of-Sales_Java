package com.example.uts_pbo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.IOException;

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

    @FXML
    void ButtonContinueOnAction(ActionEvent event) {
        validateLogin();
    }

    @FXML
    void forgotPasswordOnAction(ActionEvent event) {
        try {
            // Load the forgot password FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("OTPEmail.fxml"));
            Parent root = loader.load();

            // Get the current stage from the event source
            Stage stage = (Stage) ForgotPassword.getScene().getWindow();

            // Create new scene and set it on the current stage
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Forgot Password");
            stage.show();

        } catch (IOException e) {
            LoginMessageLabel.setText("Error: Could not load forgot password page");
            LoginMessageLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    @FXML
    void createAccountOnAction(ActionEvent event) {
        try {
            // Load the signup FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SignUp.fxml"));
            Parent root = loader.load();

            // Get the current stage from the event source
            Stage stage = (Stage) HyperlinkCreateAcc.getScene().getWindow();

            // Create new scene and set it on the current stage
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Sign Up");
            stage.show();

        } catch (IOException e) {
            LoginMessageLabel.setText("Error: Could not load signup page");
            LoginMessageLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    private void validateLogin() {
        // Cek jika username dan password kosong
        if (UsernameFieldText.getText().isEmpty() && EnterPassword.getText().isEmpty()) {
            LoginMessageLabel.setText("Username and password cannot be empty!");
            LoginMessageLabel.setTextFill(Color.RED);
        }
        // Cek jika hanya username yang kosong
        else if (UsernameFieldText.getText().isEmpty()) {
            LoginMessageLabel.setText("Username cannot be empty!");
            LoginMessageLabel.setTextFill(Color.RED);
        }
        // Cek jika hanya password yang kosong
        else if (EnterPassword.getText().isEmpty()) {
            LoginMessageLabel.setText("Password cannot be empty!");
            LoginMessageLabel.setTextFill(Color.RED);
        }
        // Jika semua field telah diisi
        else {
            // Tambahkan logic autentikasi sebenarnya di sini
            LoginMessageLabel.setText("Login succesfull!");
            LoginMessageLabel.setTextFill(Color.GREEN);
        }
    }
}