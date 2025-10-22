package User_dashboard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load FXML
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));

        // Ambil ukuran layar utama
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        // Buat scene dengan ukuran penuh layar
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

        // Atur ukuran dan posisi stage sesuai layar
        stage.setScene(scene);
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        stage.setTitle("Ini Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
