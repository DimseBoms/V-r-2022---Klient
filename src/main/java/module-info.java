module com.example.v22klient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.v22klient to javafx.fxml;
    exports com.example.v22klient;
}