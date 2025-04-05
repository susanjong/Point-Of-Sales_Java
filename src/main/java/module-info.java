module com.example.uts_pbo {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.jfr;


    opens com.example.uts_pbo to javafx.fxml;
    exports com.example.uts_pbo;
    exports Admin_View;
    opens Admin_View to javafx.fxml;
}