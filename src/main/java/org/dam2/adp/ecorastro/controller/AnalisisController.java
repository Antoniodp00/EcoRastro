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

    // --- ELEMENTOS FXML ---
    public CheckBox chkDesglose;
    @FXML private ComboBox<String> cmbRango;
    @FXML private DatePicker dpFecha;
    @FXML private Label lblTotalPeriodo;
    @FXML private Label lblActividadFrecuente;
    @FXML private Label lblInsight;
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;

    // --- SERVICIOS ---
    private final HuellaService huellaService = new HuellaService();
    private final HabitoService habitoService = new HabitoService();
    private final RecomendacionService recomendacionService = new RecomendacionService();

    // --- DATOS ---
    private List<Huella> huellasFiltradas;

    private LocalDate fechaInicioFiltro;
    private LocalDate fechaFinFiltro;

    @FXML
    public void initialize() {
        cmbRango.getItems().addAll("Todo el historial", "Mes Seleccionado", "Semana Seleccionada", "Día Concreto");
        cmbRango.setValue("Todo el historial");
        dpFecha.setValue(LocalDate.now());

        cmbRango.valueProperty().addListener((obs, oldV, newV) -> cargarDatos());
        dpFecha.valueProperty().addListener((obs, oldV, newV) -> cargarDatos());
        chkDesglose.selectedProperty().addListener((obs, oldV, newV) -> actualizarGraficoComparativo());

        cargarDatos();
    }

    private void cargarDatos() {
        int idUsuario = SessionManager.getInstance().getUsuarioActual().getId();
        cargarHuellasDesdeService(idUsuario);
        actualizarDashboard();
        cargarInsightPorHabito(idUsuario);
    }

    private void cargarHuellasDesdeService(int idUsuario) {
        String rango = cmbRango.getValue();

        if ("Todo el historial".equals(rango)) {
            huellasFiltradas = huellaService.getHuellasPorUsuario(idUsuario);
            // Reseteamos las fechas de filtro para indicar "sin filtro"
            this.fechaInicioFiltro = null;
            this.fechaFinFiltro = null;
            return;
        }

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

        // Guardamos las fechas calculadas en las variables de clase
        this.fechaInicioFiltro = inicio;
        this.fechaFinFiltro = fin;

        huellasFiltradas = huellaService.getHuellasPorFecha(idUsuario, inicio, fin);
    }

    private void actualizarDashboard() {
        if (huellasFiltradas == null) return;

        double total = 0.0;
        for (Huella h : huellasFiltradas) {
            total += h.getValor().doubleValue() * h.getIdActividad().getIdCategoria().getFactorEmision().doubleValue();
        }
        lblTotalPeriodo.setText(String.format("%.2f kg", total));

        actualizarGraficoDistribucion();
        actualizarGraficoComparativo();
    }

    private void cargarInsightPorHabito(int idUsuario) {
        Habito habitoFrecuente = habitoService.getHabitoMasFrecuente(idUsuario);

        if (habitoFrecuente != null) {
            String nombreActividad = habitoFrecuente.getIdActividad().getNombre();
            String nombreCategoria = habitoFrecuente.getIdActividad().getIdCategoria().getNombre();
            lblActividadFrecuente.setText(nombreActividad + " (" + habitoFrecuente.getFrecuencia() + " veces)");
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

        for (PieChart.Data data : pieChart.getData()) {
            String color = getColorPorCategoria(data.getName());
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-pie-color: " + color + ";");
            }
        }
        javafx.application.Platform.runLater(this::colorearLeyenda);
    }

    private void colorearLeyenda() {
        javafx.scene.Node legend = pieChart.lookup(".chart-legend");
        if (legend != null && legend instanceof javafx.scene.Parent) {
            ObservableList<javafx.scene.Node> legendItems = ((javafx.scene.Parent) legend).getChildrenUnmodifiable();
            for (int i = 0; i < legendItems.size() && i < pieChart.getData().size(); i++) {
                javafx.scene.Node item = legendItems.get(i);
                PieChart.Data data = pieChart.getData().get(i);
                String color = getColorPorCategoria(data.getName());
                if (item instanceof Label) {
                    Label label = (Label) item;
                    if (label.getGraphic() != null) {
                        label.getGraphic().setStyle("-fx-background-color: " + color + ";");
                    }
                }
            }
        }
    }

    private String getColorPorCategoria(String categoria) {
        switch (categoria) {
            case "Transporte":    return "#936639";
            case "Alimentación":  return "#656D4A";
            case "Energía":       return "#A68A64";
            case "Agua":          return "#414833";
            case "Residuos":      return "#B6AD90";
            default:              return "#333D29";
        }
    }

    private void actualizarGraficoComparativo() {
        // A. Mis Datos (siempre filtrados por huellasFiltradas)
        Map<String, Double> misTotales = new HashMap<>();
        for (Huella h : huellasFiltradas) {
            String cat = h.getIdActividad().getIdCategoria().getNombre();
            double co2 = h.getValor().doubleValue() * h.getIdActividad().getIdCategoria().getFactorEmision().doubleValue();
            misTotales.put(cat, misTotales.getOrDefault(cat, 0.0) + co2);
        }

        // B. Datos Comunidad (Ahora RESPETA el filtro de fechas)
        Map<String, Double> mediaComunidad;
        if (fechaInicioFiltro == null || fechaFinFiltro == null) {
            // Si es "Todo el historial", pedimos la media histórica total
            mediaComunidad = huellaService.getMediaImpactoPorCategoria();
        } else {
            // Si hay un rango seleccionado, pedimos la media de ese rango
            mediaComunidad = huellaService.getMediaImpactoPorCategoria(fechaInicioFiltro, fechaFinFiltro);
        }

        XYChart.Series<String, Number> serieYo = new XYChart.Series<>();
        serieYo.setName("Tú");

        XYChart.Series<String, Number> serieMedia = new XYChart.Series<>();
        serieMedia.setName("Media Comunidad");

        if (chkDesglose.isSelected()) {
            Set<String> categorias = new HashSet<>();
            categorias.addAll(misTotales.keySet());
            categorias.addAll(mediaComunidad.keySet());

            for (String cat : categorias) {
                serieYo.getData().add(new XYChart.Data<>(cat, misTotales.getOrDefault(cat, 0.0)));
                serieMedia.getData().add(new XYChart.Data<>(cat, mediaComunidad.getOrDefault(cat, 0.0)));
            }
        } else {
            double miTotalGlobal = misTotales.values().stream().mapToDouble(Double::doubleValue).sum();
            double mediaTotalGlobal = mediaComunidad.values().stream().mapToDouble(Double::doubleValue).sum();

            serieYo.getData().add(new XYChart.Data<>("Global", miTotalGlobal));
            serieMedia.getData().add(new XYChart.Data<>("Global", mediaTotalGlobal));
        }

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
                writer.write("FECHA;ACTIVIDAD;CATEGORIA;VALOR;UNIDAD;CO2_KG;RECOMENDACION_ASOCIADA\n");
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                for (Huella h : huellasFiltradas) {
                    double co2 = h.getValor().doubleValue() * h.getIdActividad().getIdCategoria().getFactorEmision().doubleValue();
                    String cat = h.getIdActividad().getIdCategoria().getNombre();
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