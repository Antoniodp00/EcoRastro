package org.dam2.adp.ecorastro.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import org.dam2.adp.ecorastro.model.Huella;
import org.dam2.adp.ecorastro.service.HuellaService;
import org.dam2.adp.ecorastro.service.RecomendacionService;
import org.dam2.adp.ecorastro.util.Navigation;
import org.dam2.adp.ecorastro.util.SessionManager;

import java.util.List;
import java.util.Map;

public class InicioController {

    // --- ELEMENTOS FXML ---
    @FXML private Label lblHuellaTotal;
    @FXML private Label lblConsejo;
    @FXML private BarChart<String, Number> barChart;
    @FXML private ProgressBar pbNivel;
    @FXML private Label lblNivel;

    // --- SERVICIOS ---
    private final HuellaService huellaService = new HuellaService();
    private final RecomendacionService recomendacionService = new RecomendacionService();

    // Variable para guardar el total calculado y usarlo en varias partes
    private double totalEmisionesUsuario = 0.0;

    public void initialize() {
        cargarDatosReales();
        configurarGraficoResumen();
        calcularNivelGamificacion();
        mostrarConsejoDelDia();
    }

    private void cargarDatosReales() {
        int idUsuario = SessionManager.getInstance().getUsuarioActual().getId();

        // 1. Obtenemos todas las huellas del usuario
        List<Huella> huellas = huellaService.getHuellasPorUsuario(idUsuario);

        // 2. Sumamos el CO2 total (Valor * Factor)
        totalEmisionesUsuario = 0.0;
        for (Huella h : huellas) {
            double valor = h.getValor().doubleValue();
            double factor = h.getIdActividad().getIdCategoria().getFactorEmision().doubleValue();
            totalEmisionesUsuario += (valor * factor);
        }

        // 3. Actualizamos la etiqueta gigante del Dashboard
        lblHuellaTotal.setText(String.format("%.2f kg CO₂", totalEmisionesUsuario));
    }

    private void configurarGraficoResumen() {
        barChart.getData().clear();
        barChart.setAnimated(false);

        // SERIE 1: TÚ (Tu total calculado arriba)
        XYChart.Series<String, Number> serieYo = new XYChart.Series<>();
        serieYo.setName("Tú");
        serieYo.getData().add(new XYChart.Data<>("", totalEmisionesUsuario));

        // SERIE 2: MEDIA GLOBAL (Calculada desde la BBDD)
        // Pedimos las medias por categoría y las sumamos para tener la "Huella Total de un Usuario Promedio"
        Map<String, Double> mediasPorCategoria = huellaService.getMediaImpactoPorCategoria();
        double mediaGlobalTotal = mediasPorCategoria.values().stream().mapToDouble(Double::doubleValue).sum();

        // Si la BBDD está vacía y da 0, ponemos una referencia visual mínima (ej: 100) para que el gráfico no se rompa
        if (mediaGlobalTotal == 0) mediaGlobalTotal = 100.0;

        XYChart.Series<String, Number> serieMedia = new XYChart.Series<>();
        serieMedia.setName("Media Global");
        serieMedia.getData().add(new XYChart.Data<>("", mediaGlobalTotal));

        barChart.getData().addAll(serieYo, serieMedia);

        // Ajustes estéticos para barras más gruesas
        barChart.setCategoryGap(20);
        barChart.setBarGap(10);
    }

    /**
     * Define tu nivel ecológico basado en tus emisiones totales.
     * Menos es mejor.
     */
    private void calcularNivelGamificacion() {
        String nivel;
        double progreso;

        // Baremos de ejemplo (puedes ajustarlos):
        // < 50 kg: Nivel Experto (Guardián)
        // 50 - 200 kg: Nivel Intermedio (Brote)
        // > 200 kg: Nivel Principiante (Semilla)

        if (totalEmisionesUsuario < 50) {
            nivel = "Guardián del Bosque 🌲";
            progreso = 1.0;
        } else if (totalEmisionesUsuario < 200) {
            nivel = "Árbol Joven 🌱";
            // Fórmula para que la barra baje a medida que te acercas al límite (200)
            progreso = 1.0 - (totalEmisionesUsuario / 200.0);
        } else {
            nivel = "Semilla 🌰";
            progreso = 0.1; // Mínimo visible
        }

        lblNivel.setText(nivel);
        pbNivel.setProgress(progreso);
    }

    private void mostrarConsejoDelDia() {
        // Elegimos una categoría aleatoria para variar el consejo cada vez que entras
        String[] categorias = {"Energía", "Transporte", "Agua", "Residuos", "Alimentación"};
        String categoriaRandom = categorias[(int) (Math.random() * categorias.length)];

        String consejo = recomendacionService.generarConsejo(categoriaRandom);
        lblConsejo.setText(consejo);
    }

    // --- NAVEGACIÓN ---

    @FXML
    public void irARegistrarHuella(MouseEvent event) {
        // Asumiendo que quieres ir al formulario de alta rápida

        Navigation.navigate("historial_huellas.fxml");
    }

    @FXML
    public void irAHabitos(MouseEvent e) {
        Navigation.navigate("mis_habitos.fxml");
    }

    @FXML
    public void irAAnalisis(MouseEvent e) {
        Navigation.navigate("analisis.fxml");
    }
}