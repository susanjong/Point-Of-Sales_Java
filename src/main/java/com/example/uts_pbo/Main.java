package com.example.uts_pbo;

import Admin_View.Product;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import java.io.IOException;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
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
            
           
            
            primaryStage.setTitle("SimpleMart - Login");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(320);
            primaryStage.setMinHeight(400);
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
            
            primaryStage.setTitle("SimpleMart - Sign Up");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading SignUp.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Loading Error", "Could not load the sign up screen: " + e.getMessage());
        }
    }
    
    // Method to show data diri sign up screen
    public static void showDataDiriSignUp(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("DataDiriSignUp.fxml"));
            Parent root = loader.load();
            
            // Pass the username to the controller
            DataDiriSignUpController controller = loader.getController();
            controller.setUsername(username);
            
            Scene scene = new Scene(root);
            primaryStage.setTitle("SimpleMart - Complete Registration");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading DataDiriSignUp.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Loading Error", "Could not load the data diri sign up screen: " + e.getMessage());
        }
    }
    
    // Method to show product management screen
    public static void showProductManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Admin_View/ProductManagement.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            primaryStage.setTitle("SimpleMart - Product Management");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading ProductManagement.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Loading Error", "Could not load the product management screen: " + e.getMessage());
        }
    }
    
    // Method to show cashier screen
    public static void showCashier() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Admin_View/Cashier.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            primaryStage.setTitle("SimpleMart - Cashier");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading Cashier.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Loading Error", "Could not load the cashier screen: " + e.getMessage());
        }
    }
    
    // General navigation method
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
            showAlert("Navigation Error", "Could not navigate to the requested screen: " + e.getMessage());
        }
    }
    
    // Helper method to show alerts
    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        // Initialize database connection test
        try {
            DatabaseConnection.getConnection();
            System.out.println("Database connection successful!");
            
            // Initialize admin user
            AdminInitializer.initialize();
            
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Initialize sample data
        Product.addProduct(new Product("P001", "Chitato snack cheese flavour", 5.99, 100, "2025-05-15", "Groceries", "images/chitato.png"));
        Product.addProduct(new Product("P002", "Bango kecap manis", 3.49, 50, "2025-04-20", "Groceries", null));
        Product.addProduct(new Product("P003", "Wardah Lightening Whip Facial Foam", 1.29, 200, "2026-01-10", "Beverages", null));
        
        launch(args);
    }
}