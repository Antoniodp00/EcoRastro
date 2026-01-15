package org.dam2.adp.ecorastro.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.dam2.adp.ecorastro.util.Navigation;
import org.dam2.adp.ecorastro.util.SessionManager;

import java.io.IOException;
import java.util.Objects;

public class MainController {
    public Label lblUsuario;

    public Button btnInicio;
    public Button btnHuella;
    public Button btnHabitos;
    public Button btnAnalisis;
    public Button btnCerrarSesion;
    public StackPane contentPane;
    public BorderPane mainPane;

    public void initialize() {
        Navigation.setMainController(this);

        if (SessionManager.getInstance().getUsuarioActual() != null){
            lblUsuario.setText(SessionManager.getInstance().getUsuarioActual().getNombre());
        }


       mostrarInicio(null);

    }

    public void mostrarInicio(ActionEvent actionEvent) {
        loadView("inicio.fxml");
    }



    public void cerrarSesion(ActionEvent actionEvent) {
        SessionManager.getInstance().cerrarSesion();
        Navigation.switchScene("login.fxml");


    }

    public void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam2/adp/ecorastro/view/" + fxml));
            Node view = loader.load();

            // Binding para que ocupe todo el espacio
            if (view instanceof javafx.scene.layout.Region) {
                javafx.scene.layout.Region region = (javafx.scene.layout.Region) view;
                region.prefWidthProperty().bind(contentPane.widthProperty());
                region.prefHeightProperty().bind(contentPane.heightProperty());
            }

            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error cargando vista: " + fxml);
        }
    }
}
