package com.example.uts_pbo;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.net.URL;

public class NavigationAuthorizer {
    
    public static final int USER_VIEW  = 0;
    public static final int ADMIN_VIEW = 1;
    
    public static boolean navigateTo(Button button, String fxmlPath, int viewType) {
        // 1) permission check
        if (viewType == ADMIN_VIEW && !UserSession.isAdmin()) {
            showAlert(Alert.AlertType.WARNING,
                      "Access Denied",
                      "Admin access required.");
            return false;
        }
        
        try {
            // 2) load via this class’s classloader, not Button’s
            URL url = NavigationAuthorizer.class.getResource(fxmlPath);
            if (url == null) {
                showAlert(Alert.AlertType.ERROR,
                          "Navigation Error",
                          "Could not find FXML file: " + fxmlPath);
                return false;
            }
            
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) button.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            return true;
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR,
                      "Navigation Error",
                      e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private static void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}