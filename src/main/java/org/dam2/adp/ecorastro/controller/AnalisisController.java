package org.dam2.adp.ecorastro.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;

public class AnalisisController {

    @FXML private ComboBox<String> cmbRango;
    @FXML private DatePicker dpFecha;
    @FXML private Label lblTotalPeriodo;
    @FXML private Label lblActividadFrecuente;
    @FXML private Label lblInsight;
    @FXML private CheckBox chkDesglose;
    @FXML private CheckBox chkEvolucion;
    @FXML private PieChart pieChart;
    @FXML private LineChart<String, Number> lineChart;
    @FXML private BarChart<String, Number> barChart;

    private final HuellaService huellaService = new HuellaService();
    private final HabitoService habitoService = new HabitoService();
    private final RecomendacionService recomendacionService = new RecomendacionService();

    private List<Huella> huellasFiltradas;
    private LocalDate fechaInicioFiltro;
    private LocalDate fechaFinFiltro;

    @FXML
    public void initialize() {
        configurarFiltros();
        configurarListeners();
        lineChart.setVisible(false);
        pieChart.setVisible(true);
        cargarDatos();
    }

    private void configurarFiltros() {
        cmbRango.getItems().addAll("Todo el historial", "Mes Seleccionado", "Semana Seleccionada", "Día Concreto");
        cmbRango.setValue("Mes Seleccionado");
        dpFecha.setValue(LocalDate.now());
    }

    private void configurarListeners() {
        cmbRango.valueProperty().addListener((obs, oldV, newV) -> cargarDatos());
        dpFecha.valueProperty().addListener((obs, oldV, newV) -> cargarDatos());
        chkDesglose.selectedProperty().addListener((obs, oldV, newV) -> actualizarGraficoComparativo());

        chkEvolucion.selectedProperty().addListener((obs, oldV, isSelected) -> {
            pieChart.setVisible(!isSelected);
            lineChart.setVisible(isSelected);
            if (isSelected) actualizarGraficoEvolucion();
            else actualizarGraficoDistribucion();
        });
    }

    private void cargarDatos() {
        int idUsuario = SessionManager.getInstance().getUsuarioActual().getId();
        calcularFechasFiltro();

        // Carga de huellas en memoria para cálculos rápidos
        huellasFiltradas = huellaService.getHuellasPorFecha(idUsuario, fechaInicioFiltro, fechaFinFiltro);

        actualizarKPIs(idUsuario);

        if (chkEvolucion.isSelected()) actualizarGraficoEvolucion();
        else actualizarGraficoDistribucion();

        actualizarGraficoComparativo();
        generarInsight(idUsuario);
    }

    private void calcularFechasFiltro() {
        String rango = cmbRango.getValue();
        LocalDate ref = dpFecha.getValue() != null ? dpFecha.getValue() : LocalDate.now();

        if ("Todo el historial".equals(rango)) {
            this.fechaInicioFiltro = LocalDate.of(1970, 1, 1);
            this.fechaFinFiltro = LocalDate.of(2100, 1, 1);
        } else if ("Día Concreto".equals(rango)) {
            this.fechaInicioFiltro = ref;
            this.fechaFinFiltro = ref;
        } else if ("Semana Seleccionada".equals(rango)) {
            this.fechaInicioFiltro = ref.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
            this.fechaFinFiltro = ref.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 7);
        } else {
            this.fechaInicioFiltro = ref.with(TemporalAdjusters.firstDayOfMonth());
            this.fechaFinFiltro = ref.with(TemporalAdjusters.lastDayOfMonth());
        }
    }

    private void actualizarKPIs(int idUsuario) {
        double total = huellaService.getTotalImpactoUsuarioFecha(idUsuario, fechaInicioFiltro, fechaFinFiltro);
        lblTotalPeriodo.setText(String.format("%.2f kg CO₂", total));
    }

    private void generarInsight(int idUsuario) {
        Habito habito = habitoService.getHabitoMasFrecuente(idUsuario);
        if (habito != null) {
            lblActividadFrecuente.setText(habito.getIdActividad().getNombre());
            String consejo = recomendacionService.generarConsejo(habito.getIdActividad().getIdCategoria().getNombre());
            lblInsight.setText("Hábito frecuente (" + habito.getIdActividad().getIdCategoria().getNombre() + "): " + consejo);
        } else {
            lblActividadFrecuente.setText("(Sin datos)");
            lblInsight.setText("Registra actividades para ver consejos.");
        }
    }

    // --- GRÁFICOS ---

    private void actualizarGraficoDistribucion() {
        int idUsuario = SessionManager.getInstance().getUsuarioActual().getId();
        Map<String, Double> datos = huellaService.getImpactoPorCategoriaUsuario(idUsuario, fechaInicioFiltro, fechaFinFiltro);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        datos.forEach((k, v) -> pieData.add(new PieChart.Data(k, v)));
        pieChart.setData(pieData);

        // 1. Colorear los "quesitos" del gráfico
        pieData.forEach(data -> {
            String color = getColorHexPorCategoria(data.getName());
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-pie-color: " + color + ";");
            }
        });

        // 2. Colorear la leyenda (IMPORTANTE: Usar runLater)
        // JavaFX tarda un instante en crear la leyenda internamente, por eso esperamos.
        javafx.application.Platform.runLater(this::colorearLeyenda);
    }

    private void actualizarGraficoEvolucion() {
        int idUsuario = SessionManager.getInstance().getUsuarioActual().getId();

        // Ventana móvil últimos 12 meses
        List<Object[]> datosBBDD = huellaService.getEvolucionUltimos12Meses(idUsuario);
        Map<String, Double> mapaValores = new HashMap<>();

        for (Object[] fila : datosBBDD) {
            mapaValores.put(fila[0] + "-" + fila[1], (Double) fila[2]);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tendencia");

        LocalDate iterador = LocalDate.now().minusMonths(11);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yy", new Locale("es", "ES"));

        for (int i = 0; i < 12; i++) {
            String clave = iterador.getYear() + "-" + iterador.getMonthValue();
            double valor = mapaValores.getOrDefault(clave, 0.0);

            String etiqueta = iterador.format(fmt);
            etiqueta = etiqueta.substring(0, 1).toUpperCase() + etiqueta.substring(1);

            series.getData().add(new XYChart.Data<>(etiqueta, valor));
            iterador = iterador.plusMonths(1);
        }

        lineChart.getData().clear();
        lineChart.getData().add(series);
        // Color naranja para la línea
        series.getNode().setStyle("-fx-stroke: #E67E22; -fx-stroke-width: 2px;");
    }

    private void actualizarGraficoComparativo() {
        int idUsuario = SessionManager.getInstance().getUsuarioActual().getId();
        Map<String, Double> misDatos = huellaService.getImpactoPorCategoriaUsuario(idUsuario, fechaInicioFiltro, fechaFinFiltro);
        Map<String, Double> mediaComunidad = huellaService.getMediaImpactoPorCategoriaFechas(fechaInicioFiltro, fechaFinFiltro);

        XYChart.Series<String, Number> serieYo = new XYChart.Series<>();
        serieYo.setName("Tú");
        XYChart.Series<String, Number> serieMedia = new XYChart.Series<>();
        serieMedia.setName("Media");

        if (chkDesglose.isSelected()) {
            Set<String> categorias = new HashSet<>();
            categorias.addAll(misDatos.keySet());
            categorias.addAll(mediaComunidad.keySet());

            for (String cat : categorias) {
                serieYo.getData().add(new XYChart.Data<>(cat, misDatos.getOrDefault(cat, 0.0)));
                serieMedia.getData().add(new XYChart.Data<>(cat, mediaComunidad.getOrDefault(cat, 0.0)));
            }
        } else {
            double miTotal = misDatos.values().stream().mapToDouble(d -> d).sum();
            double mediaTotal = mediaComunidad.values().stream().mapToDouble(d -> d).sum();
            serieYo.getData().add(new XYChart.Data<>("Global", miTotal));
            serieMedia.getData().add(new XYChart.Data<>("Global", mediaTotal));
        }

        barChart.getData().clear();
        barChart.getData().addAll(serieYo, serieMedia);

        // --- CORRECCIÓN DE COLORES BARCHART ---
        for (XYChart.Data<String, Number> d : serieYo.getData()) {
            if(d.getNode() != null) d.getNode().setStyle("-fx-bar-fill: #936639;"); // Marrón (Tú)
        }
        for (XYChart.Data<String, Number> d : serieMedia.getData()) {
            if(d.getNode() != null) d.getNode().setStyle("-fx-bar-fill: #656D4A;"); // Verde (Media)
        }
    }

    /** Helper para asignar colores fijos según categoría */
    private String getColorHexPorCategoria(String categoria) {
        if (categoria == null) return "#7F8C8D";
        switch (categoria) {
            case "Transporte":    return "#936639"; // Marrón
            case "Alimentación":  return "#656D4A"; // Verde Oliva
            case "Energía":       return "#A68A64"; // Arena
            case "Agua":          return "#414833"; // Verde Oscuro
            case "Residuos":      return "#B6AD90"; // Piedra
            default:              return "#333D29"; // Fondo oscuro
        }
    }

    @FXML
    public void exportarReporte(ActionEvent event) {
        if (huellasFiltradas == null || huellasFiltradas.isEmpty()) {
            AlertUtils.error("No hay datos para exportar.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar CSV");
        fileChooser.setInitialFileName("reporte_eco.csv");
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("FECHA;ACTIVIDAD;CATEGORIA;VALOR;CO2_KG\n");
                for (Huella h : huellasFiltradas) {
                    writer.write(String.format("%s;%s;%s;%.2f;%.2f\n",
                            h.getFecha(), h.getIdActividad().getNombre(),
                            h.getIdActividad().getIdCategoria().getNombre(),
                            h.getValor(), huellaService.calcularImpacto(h)));
                }
                AlertUtils.info("Exportado con éxito.");
            } catch (IOException e) {
                AlertUtils.error("Error al exportar.");
            }
        }
    }

    /**
     * Aplica los colores personalizados a los símbolos de la leyenda.
     * Se debe llamar dentro de Platform.runLater() para asegurar que el gráfico ya se ha renderizado.
     */
    private void colorearLeyenda() {
        // Buscamos el nodo de la leyenda dentro del PieChart (es un hijo oculto)
        javafx.scene.Node legend = pieChart.lookup(".chart-legend");

        if (legend != null && legend instanceof javafx.scene.Parent) {
            ObservableList<javafx.scene.Node> legendItems = ((javafx.scene.Parent) legend).getChildrenUnmodifiable();

            // Iteramos sobre los items de la leyenda
            for (javafx.scene.Node item : legendItems) {
                if (item instanceof Label) {
                    Label label = (Label) item;

                    // El texto de la etiqueta es la categoría (ej: "Energía")
                    String categoria = label.getText();
                    String colorHex = getColorHexPorCategoria(categoria);

                    // El símbolo de color es el "graphic" del Label
                    if (label.getGraphic() != null) {
                        label.getGraphic().setStyle("-fx-background-color: " + colorHex + ";");
                    }
                }
            }
        }
    }
}