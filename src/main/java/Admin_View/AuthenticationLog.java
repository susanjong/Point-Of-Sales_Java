package Admin_View;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthenticationLog extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Ensure the authentication log table exists
        AuthenticationLogDAO.createTableIfNotExists();
        
        FXMLLoader fxmlLoader = new FXMLLoader(AuthenticationLog.class.getResource("AuthenticationLog.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Authentication Log");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}