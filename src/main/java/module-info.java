module com.example.uts_pbo {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.jfr;
    requires transitive java.sql;
    requires transitive javafx.graphics;

    opens User_dashboard to javafx.fxml;
    exports User_dashboard;

    opens Admin_View to javafx.fxml;
    exports Admin_View;
}

