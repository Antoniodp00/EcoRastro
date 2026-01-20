package org.dam2.adp.ecorastro.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.dam2.adp.ecorastro.model.Habito;
import org.dam2.adp.ecorastro.model.Huella;
import org.dam2.adp.ecorastro.service.HabitoService;
import org.dam2.adp.ecorastro.service.HuellaService;
import org.dam2.adp.ecorastro.service.RecomendacionService;
import org.dam2.adp.ecorastro.util.AlertUtils;
import org.dam2.adp.ecorastro.util.SessionManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;

public class AnalisisController {

    public CheckBox chkDesglose;
    // --- ELEMENTOS FXML ---
    @FXML private ComboBox<String> cmbRango;
    @FXML private DatePicker dpFecha;

    @FXML private Label lblTotalPeriodo;
    @FXML private Label lblActividadFrecuente; // Se usará para mostrar el Hábito más frecuente
    @FXML private Label lblInsight;

    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;

    // --- SERVICIOS ---
    private final HuellaService huellaService = new HuellaService();
    private final HabitoService habitoService = new HabitoService();
    private final RecomendacionService recomendacionService = new RecomendacionService();

    // --- DATOS ---
    private List<Huella> huellasFiltradas;

    @FXML
    public void initialize() {
        // 1. Configuración de filtros
        cmbRango.getItems().addAll("Todo el historial", "Mes Seleccionado", "Semana Seleccionada", "Día Concreto");
        cmbRango.setValue("Todo el historial");
        dpFecha.setValue(LocalDate.now());

        // 2. Listeners: Recargar datos al cambiar cualquier filtro
        cmbRango.valueProperty().addListener((obs, oldV, newV) -> cargarDatos());
        dpFecha.valueProperty().addListener((obs, oldV, newV) -> cargarDatos());

        chkDesglose.selectedProperty().addListener((obs, oldV, newV) -> actualizarGraficoComparativo());
        // 3. Carga inicial
        cargarDatos();
    }

    /**
     * Orquestador principal de carga de datos.
     */
    private void cargarDatos() {
        int idUsuario = SessionManager.getInstance().getUsuarioActual().getId();

        // 1. Cargar Historial de Huellas (para gráficos y totales)
        cargarHuellasDesdeService(idUsuario);

        // 2. Actualizar la interfaz gráfica
        actualizarDashboard();

        // 3. Cargar Recomendación basada en HÁBITOS (Requisito PDF)
        cargarInsightPorHabito(idUsuario);
    }

    /**
     * Calcula las fechas y pide al Service las huellas de ese rango.
     */
    private void cargarHuellasDesdeService(int idUsuario) {
        String rango = cmbRango.getValue();

        // --- CORRECCIÓN CLAVE ---
        // Si dice "Todo", usamos EL MISMO MÉTODO que Inicio para que cuadre al céntimo.
        if ("Todo el historial".equals(rango)) {
            huellasFiltradas = huellaService.getHuellasPorUsuario(idUsuario);
            return;
        }
        // ------------------------

        LocalDate ref = dpFecha.getValue() != null ? dpFecha.getValue() : LocalDate.now();
        LocalDate inicio = ref;
        LocalDate fin = ref;

        switch (rango) {
            case "Día Concreto":
                inicio = ref;
                fin = ref;
                break;
            case "Semana Seleccionada":
                inicio = ref.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
                fin = ref.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 7);
                break;
            case "Mes Seleccionado":
                inicio = ref.with(TemporalAdjusters.firstDayOfMonth());
                fin = ref.with(TemporalAdjusters.lastDayOfMonth());
                break;
        }

        huellasFiltradas = huellaService.getHuellasPorFecha(idUsuario, inicio, fin);
    }

    private void actualizarDashboard() {
        if (huellasFiltradas == null) return;

        // KPI: Total CO2
        double total = 0.0;
        for (Huella h : huellasFiltradas) {
            total += h.getValor().doubleValue() * h.getIdActividad().getIdCategoria().getFactorEmision().doubleValue();
        }
        lblTotalPeriodo.setText(String.format("%.2f kg", total));

        // Gráficos
        actualizarGraficoDistribucion();
        actualizarGraficoComparativo();
    }

    /**
     * Genera recomendación basada en el Hábito más frecuente (según PDF).
     */
    private void cargarInsightPorHabito(int idUsuario) {
        Habito habitoFrecuente = habitoService.getHabitoMasFrecuente(idUsuario);

        if (habitoFrecuente != null) {
            String nombreActividad = habitoFrecuente.getIdActividad().getNombre();
            String nombreCategoria = habitoFrecuente.getIdActividad().getIdCategoria().getNombre();

            // KPI: Actividad frecuente
            lblActividadFrecuente.setText(nombreActividad + " (" + habitoFrecuente.getFrecuencia() + " veces)");

            // Insight: Consejo basado en la categoría del hábito
            String consejo = recomendacionService.generarConsejo(nombreCategoria);
            lblInsight.setText("Tu hábito más frecuente es '" + nombreActividad + "'. Consejo: " + consejo);
        } else {
            lblActividadFrecuente.setText("(Sin hábitos)");
            lblInsight.setText("Registra hábitos frecuentes para recibir consejos personalizados.");
        }
    }

    private void actualizarGraficoDistribucion() {
        Map<String, Double> porCategoria = new HashMap<>();

        for (Huella h : huellasFiltradas) {
            String cat = h.getIdActividad().getIdCategoria().getNombre();
            double co2 = h.getValor().doubleValue() * h.getIdActividad().getIdCategoria().getFactorEmision().doubleValue();
            porCategoria.put(cat, porCategoria.getOrDefault(cat, 0.0) + co2);
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        porCategoria.forEach((k, v) -> pieData.add(new PieChart.Data(k, v)));
        pieChart.setData(pieData);
    }

    private void actualizarGraficoComparativo() {
        // A. Preparar Mis Datos (siempre los calculamos)
        Map<String, Double> misTotales = new HashMap<>();
        for (Huella h : huellasFiltradas) {
            String cat = h.getIdActividad().getIdCategoria().getNombre();
            double co2 = h.getValor().doubleValue() * h.getIdActividad().getIdCategoria().getFactorEmision().doubleValue();
            misTotales.put(cat, misTotales.getOrDefault(cat, 0.0) + co2);
        }

        // B. Preparar Datos Comunidad
        Map<String, Double> mediaComunidad = huellaService.getMediaImpactoPorCategoria();

        // C. Configurar Series
        XYChart.Series<String, Number> serieYo = new XYChart.Series<>();
        serieYo.setName("Tú");

        XYChart.Series<String, Number> serieMedia = new XYChart.Series<>();
        serieMedia.setName("Media Comunidad");

        // D. Lógica de Visualización (AQUÍ ESTÁ EL CAMBIO)
        if (chkDesglose.isSelected()) {
            // --- OPCIÓN 1: DESGLOSE POR CATEGORÍA (Como antes) ---
            Set<String> categorias = new HashSet<>();
            categorias.addAll(misTotales.keySet());
            categorias.addAll(mediaComunidad.keySet());

            for (String cat : categorias) {
                serieYo.getData().add(new XYChart.Data<>(cat, misTotales.getOrDefault(cat, 0.0)));
                serieMedia.getData().add(new XYChart.Data<>(cat, mediaComunidad.getOrDefault(cat, 0.0)));
            }
        } else {
            // --- OPCIÓN 2: TOTALES GLOBALES (Sin Categoría) ---
            // Sumamos todos los valores del mapa
            double miTotalGlobal = misTotales.values().stream().mapToDouble(Double::doubleValue).sum();
            double mediaTotalGlobal = mediaComunidad.values().stream().mapToDouble(Double::doubleValue).sum();

            // Creamos una única barra llamada "Global"
            serieYo.getData().add(new XYChart.Data<>("Global", miTotalGlobal));
            serieMedia.getData().add(new XYChart.Data<>("Global", mediaTotalGlobal));
        }

        // E. Pintar
        barChart.getData().clear();
        barChart.getData().addAll(serieYo, serieMedia);
    }

    @FXML
    public void exportarReporte(ActionEvent event) {
        if (huellasFiltradas == null || huellasFiltradas.isEmpty()) {
            AlertUtils.error("No hay datos visibles para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Reporte CSV");
        fileChooser.setInitialFileName("reporte_huella_carbono.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));
        File file = fileChooser.showSaveDialog(pieChart.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Cabecera CSV
                writer.write("FECHA;ACTIVIDAD;CATEGORIA;VALOR;UNIDAD;CO2_KG;RECOMENDACION_ASOCIADA\n");
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                for (Huella h : huellasFiltradas) {
                    double co2 = h.getValor().doubleValue() * h.getIdActividad().getIdCategoria().getFactorEmision().doubleValue();
                    String cat = h.getIdActividad().getIdCategoria().getNombre();
                    // Obtenemos una recomendación contextual para esa fila
                    String tip = recomendacionService.generarConsejo(cat);

                    writer.write(String.format("%s;%s;%s;%.2f;%s;%.2f;%s\n",
                            fmt.format(h.getFecha().atZone(ZoneId.systemDefault())),
                            h.getIdActividad().getNombre(),
                            cat,
                            h.getValor(),
                            h.getUnidad(),
                            co2,
                            tip
                    ));
                }
                AlertUtils.info("Reporte exportado correctamente.");
            } catch (IOException e) {
                AlertUtils.error("Error al exportar: " + e.getMessage());
            }
        }
    }
}