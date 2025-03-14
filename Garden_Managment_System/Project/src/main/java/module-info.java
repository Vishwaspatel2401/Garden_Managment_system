module com.project {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.project to javafx.fxml;
//    exports com.project;
    exports com.project.controller;
    opens com.project.controller to javafx.fxml;
}