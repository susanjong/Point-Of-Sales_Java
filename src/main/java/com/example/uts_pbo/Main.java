package com.example.uts_pbo;

import Admin_View.Product;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        showProductManagement();
    }
    
    public static void showProductManagement() throws Exception {
        // Sesuaikan path resource sesuai dengan struktur folder project Anda
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/Admin_View/ProductManagement.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        
        primaryStage.setTitle("SimpleMart - Product Management");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        // Initialize sample data
        Product.addProduct(new Product("P001", "Chitato snack cheese flavour", 5.99, 100, "2025-05-15", "Groceries", "images/chitato.png"));
        Product.addProduct(new Product("P002", "Bango kecap manis", 3.49, 50, "2025-04-20", "Groceries", null));
        Product.addProduct(new Product("P003", "Wardah Lightening Whip Facial Foam", 1.29, 200, "2026-01-10", "Beverages", null));
        
        launch(args);
    }
}