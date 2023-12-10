module com.MDLab5 {
    requires javafx.controls;
    requires javafx.fxml;
    requires annotations;


    opens com.MDLab5 to javafx.fxml;
    exports com.MDLab5;
}