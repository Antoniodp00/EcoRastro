package org.dam2.adp.ecorastro.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import org.dam2.adp.ecorastro.util.Navigation;

public class InicioController {
    public Label lblHuellaTotal;
    public Label lblConsejo;
    public BarChart barChart;

    public void initialize(){
        cargarDatosSimulados();
    }

    private void cargarDatosSimulados() {
        // 1. SIMULAR KPI PRINCIPAL
        // En el futuro esto vendrá de la BBDD
        double totalSimulado = 45.20;
        lblHuellaTotal.setText(String.format("%.2f kg CO₂", totalSimulado));

        // 2. SIMULAR GRÁFICO COMPARATIVO
        // Limpiamos datos previos por si acaso
        barChart.getData().clear();
        barChart.setAnimated(false); // Desactivar animación para evitar bugs visuales al recargar

        // Serie 1: Usuario (Tú)
        XYChart.Series<String, Number> serieYo = new XYChart.Series<>();
        serieYo.setName("Tú");
        serieYo.getData().add(new XYChart.Data<>("", totalSimulado));

        // Serie 2: Media de la Comunidad (Falso)
        XYChart.Series<String, Number> serieMedia = new XYChart.Series<>();
        serieMedia.setName("Media Global");
        serieMedia.getData().add(new XYChart.Data<>("", 60.0)); // Supongamos que la media es 60kg

        barChart.getData().addAll(serieYo, serieMedia);

        // 3. SIMULAR CONSEJO
        lblConsejo.setText("💡 Sabías que... apagar el router por la noche ahorra energía equivalente a cargar 50 móviles.");
    }
    @FXML public void irARegistrarHuella(MouseEvent e) { Navigation.navigate("register_huella.fxml"); }
    @FXML public void irAHabitos(MouseEvent e) { Navigation.navigate("mis_habitos.fxml"); }
    @FXML public void irAAnalisis(MouseEvent e) { Navigation.navigate("analisis.fxml"); }
}
