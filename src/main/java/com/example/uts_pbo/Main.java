package com.example.uts_pbo;

import Admin_View.AuthenticationLogDAO;
import Admin_View.AuthenticationLogEntry;
import Admin_View.Product;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import java.io.IOException;
import javafx.geometry.Rectangle2D;

public class Main extends Application {
    private static Stage primaryStage;

    public static void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            showAlert("Loading Error", "Could not load the requested screen: " + e.getMessage());
        }
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

         // Initialize the default admin account
         AdminInitializer.initialize();
        // Start with the login screen
        showLoginScreen();
    }
    
    // Method to get the primary stage
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    // Add method to show login screen
    public static void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

              Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            primaryStage.setTitle("SimpleMart - Login");
            primaryStage.setScene(scene);
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());
        
            // (Opsional) Atur ukuran minimal jika ingin tetap bisa resize ke kecil
            primaryStage.setMinWidth(800);  
            primaryStage.setMinHeight(600);
            
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading Login.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Loading Error", "Could not load the login screen: " + e.getMessage());
        }
    }
    
    // Method to show sign up screen
    public static void showSignUpScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("SignUp.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            primaryStage.setTitle("SimpleMart - Sign Up");
            primaryStage.setScene(scene);
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());
        
            // (Opsional) Atur ukuran minimal jika ingin tetap bisa resize ke kecil
            primaryStage.setMinWidth(800);  
            primaryStage.setMinHeight(600);
            
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading SignUp.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Loading Error", "Could not load the sign up screen: " + e.getMessage());
        }
    }
    
    public static void showProductManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Admin_View/ProductManagement.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            primaryStage.setTitle("SimpleMart - Product Management");

             primaryStage.setScene(scene);
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());
        
            // (Opsional) Atur ukuran minimal jika ingin tetap bisa resize ke kecil
            primaryStage.setMinWidth(800);  
            primaryStage.setMinHeight(600);
            
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading ProductManagement.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Loading Error", "Could not load the product management screen: " + e.getMessage());
        }
    }

    // Add this method to your Main class
    public static void showDataDiriSignUp(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("DataDiriSignUp.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the username
            DataDiriSignUpController controller = loader.getController();
            if (controller != null) {
                controller.setUsername(username);
            }
            
            Scene scene = new Scene(root);
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            
            primaryStage.setTitle("SimpleMart - Complete Registration");
            primaryStage.setScene(scene);
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());
        
            // (Opsional) Atur ukuran minimal jika ingin tetap bisa resize ke kecil
            primaryStage.setMinWidth(800);  
            primaryStage.setMinHeight(600);
            
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading DataDiriSignUp.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Loading Error", "Could not load the registration data screen: " + e.getMessage());
        }
    }

    // Add this method to your Main class
    public static void showCashier() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Admin_View/Cashier.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            primaryStage.setTitle("SimpleMart - Cashier");
            primaryStage.setScene(scene);
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());
        
            // (Opsional) Atur ukuran minimal jika ingin tetap bisa resize ke kecil
            primaryStage.setMinWidth(800);  
            primaryStage.setMinHeight(600);
            
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Error loading Cashier.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Loading Error", "Could not load the cashier screen: " + e.getMessage());
        }
    }

    public static void showAuthenticationLogs() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Admin_View/AuthenticationLog.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            
            primaryStage.setTitle("SimpleMart - Admin Panel");
            primaryStage.setScene(scene);
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());
        
            // (Opsional) Atur ukuran minimal jika ingin tetap bisa resize ke kecil
            primaryStage.setMinWidth(800);  
            primaryStage.setMinHeight(600);
            
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading AuthenticationLog.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Loading Error", "Could not load the AuthenticationLog screen: " + e.getMessage());
        }
    }

    public static void showProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("Profile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            primaryStage.setTitle("SimpleMart - Profile");
            primaryStage.setScene(scene);
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());
        
            // (Opsional) Atur ukuran minimal jika ingin tetap bisa resize ke kecil
            primaryStage.setMinWidth(800);  
            primaryStage.setMinHeight(600);
            
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading Profile.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Loading Error", "Could not load the profile screen: " + e.getMessage());
        }
    }

    public static void showUserManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Admin_View/UserManagement.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            primaryStage.setTitle("SimpleMart - Profile");
            primaryStage.setScene(scene);
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());
        
            // (Opsional) Atur ukuran minimal jika ingin tetap bisa resize ke kecil
            primaryStage.setMinWidth(800);  
            primaryStage.setMinHeight(600);
            
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading UserManagement.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Loading Error", "Could not load the user management screen: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}