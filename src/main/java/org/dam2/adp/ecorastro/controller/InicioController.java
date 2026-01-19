package org.dam2.adp.ecorastro.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import org.dam2.adp.ecorastro.util.Navigation;

public class InicioController {
    public Label lblHuellaTotal;
    public Label lblConsejo;
    public BarChart barChart;
    public ProgressBar pbNivel;
    public Label lblNivel;

    public void initialize(){
        cargarDatosSimulados();
    }

    private void cargarDatosSimulados() {
        // 1. SIMULAR KPI PRINCIPAL
        // En el futuro esto vendrá de la BBDD
        double totalSimulado = 45.20;
        lblHuellaTotal.setText(String.format("%.2f kg CO₂", totalSimulado));

            // 1. Limpiar datos anteriores
            barChart.getData().clear();
            barChart.setAnimated(false); // Importante: Desactivar animación para evitar bugs de visualización

            // 2. SERIE 1: Usuario (Tú) -> Se pintará con .default-color0 (Cian)
            XYChart.Series<String, Number> serieUsuario = new XYChart.Series<>();
            serieUsuario.setName("Tú");
            serieUsuario.getData().add(new XYChart.Data<>("", 45.20)); // Dejamos la X vacía o ponemos texto

            // 3. SERIE 2: Comunidad -> Se pintará con .default-color1 (Ámbar)
            XYChart.Series<String, Number> serieComunidad = new XYChart.Series<>();
            serieComunidad.setName("Media Global");
            serieComunidad.getData().add(new XYChart.Data<>("", 60.00));

            // 4. Añadir ambas series
            barChart.getData().addAll(serieUsuario, serieComunidad);

            // 5. Ajuste manual del ancho de las barras (Truco para forzar que se vean gordas)
            barChart.setCategoryGap(20);
            barChart.setBarGap(10);

        // 3. SIMULAR CONSEJO
        lblConsejo.setText("💡 Sabías que... apagar el router por la noche ahorra energía equivalente a cargar 50 móviles.");
    }
    @FXML
    public void irARegistrarHuella(MouseEvent event) {
        Navigation.navigate("historial_huellas.fxml");
    }
    @FXML public void irAHabitos(MouseEvent e) { Navigation.navigate("mis_habitos.fxml"); }
    @FXML public void irAAnalisis(MouseEvent e) { Navigation.navigate("analisis.fxml"); }
}
