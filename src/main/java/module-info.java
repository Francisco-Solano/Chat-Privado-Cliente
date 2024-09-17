module com.example.clienteproyectoservicios2ev {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.clienteproyectoservicios2ev to javafx.fxml;
    exports com.example.clienteproyectoservicios2ev;
}