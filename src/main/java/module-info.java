module ide {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    requires com.sun.jna;
    requires com.sun.jna.platform;

    requires com.google.gson;

    opens ide;
}