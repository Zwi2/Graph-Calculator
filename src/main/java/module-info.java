module calc.graphcalc {
    requires javafx.controls;
    requires javafx.fxml;


    opens calc.graphcalc to javafx.fxml;
    exports calc.graphcalc;
}