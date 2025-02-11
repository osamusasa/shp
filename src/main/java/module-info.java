module SHP {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires com.fasterxml.jackson.databind;

    exports xyz.osamusasa.shp;

    opens xyz.osamusasa.shp;
}