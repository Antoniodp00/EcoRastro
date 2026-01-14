package org.dam2.adp.ecorastro.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import org.dam2.adp.ecorastro.util.Navigation;
import org.dam2.adp.ecorastro.util.SessionManager;

import java.util.Objects;

public class MainController {
    public Label lblUsuario;

    public Button btnInicio;
    public Button btnHuella;
    public Button btnHabitos;
    public Button btnAnalisis;
    public Button btnCerrarSesion;
    public ScrollPane scrollPane;
    public StackPane contentPane;

    public void initialize() {
        Navigation.setMainController(this);

        btnAnalisis.setOnAction(this::mostrarAnalisis);
        btnHuella.setOnAction(this::mostrarHuella);
        btnHabitos.setOnAction(this::mostrarHabitos);
        btnInicio.setOnAction(this::mostrarInicio);
        btnCerrarSesion.setOnAction(this::cerrarSesion);

        loadView("inicio.fxml");


    }

    public void mostrarInicio(ActionEvent actionEvent) {
        loadView("inicio.fxml");
    }

    public void mostrarHuella(ActionEvent actionEvent) {
    loadView("register_huella.fxml");
    }

    public void mostrarHabitos(ActionEvent actionEvent) {
        loadView("mis_habitos.fxml");
    }

    public void mostrarAnalisis(ActionEvent actionEvent) {
        loadView("analisis.fxml");
    }

    public void cerrarSesion(ActionEvent actionEvent) {
        SessionManager.getInstance().cerrarSesion();
        Navigation.switchScene("login.fxml");


    }

    public void loadView(String fxml) {
        try{
            Node view = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/dam2/adp/ecorastro/view/" + fxml)));
            contentPane.getChildren().setAll(view);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
