package com.example.uts_pbo;

import Admin_View.Product;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import java.net.URL;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showProductManagement();
    }
    
    public static void showProductManagement() throws Exception {
        loadFXML("ProductManagement.fxml", "SimpleMart - Product Management");
    }
    
    public static void showProfile() throws Exception {
        loadFXML("Profile.fxml", "SimpleMart - Profile");
    }
    
    public static void showCashier() throws Exception {
        loadFXML("Cashier.fxml", "SimpleMart - Cashier");
    }
    
    public static void showUserManagement() throws Exception {
        loadFXML("UserManagement.fxml", "SimpleMart - User Management");
    }
    
    public static void showAdminLog() throws Exception {
        loadFXML("AdminLog.fxml", "SimpleMart - Admin Log");
    }
    
    private static void loadFXML(String fxmlFile, String title) throws Exception {
        try {
            // Always use the Admin_View package path
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Admin_View/" + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(600);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void navigateTo(String fxmlFile) {
        try {
            System.out.println("navigateTo called with: " + fxmlFile);
            String title = "SimpleMart";
            
            // Set appropriate title based on the page
            if (fxmlFile.contains("Product")) {
                title += " - Product Management";
            } else if (fxmlFile.contains("Profile")) {
                title += " - Profile";
            } else if (fxmlFile.contains("Cashier")) {
                title += " - Cashier";
            } else if (fxmlFile.contains("UserManagement")) {
                title += " - User Management";
            } else if (fxmlFile.contains("AdminLog")) {
                title += " - Admin Log";
            }
            
            // Debug the resource location
            URL resourceUrl = Main.class.getResource("/Admin_View/" + fxmlFile);
            System.out.println("Resource URL: " + resourceUrl);
            
            if (resourceUrl == null) {
                throw new IOException("Resource not found: /Admin_View/" + fxmlFile);
            }
            
            loadFXML(fxmlFile, title);
        } catch (Exception e) {
            System.err.println("Navigation error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {        
        launch(args);
    }
}