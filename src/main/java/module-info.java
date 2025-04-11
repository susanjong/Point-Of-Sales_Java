module com.example.uts_pbo {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.jfr;
    requires transitive java.sql;
    requires transitive javafx.graphics;

    opens com.example.uts_pbo to javafx.fxml;
    exports com.example.uts_pbo;

    opens Admin_View to javafx.fxml;
    exports Admin_View;
}

