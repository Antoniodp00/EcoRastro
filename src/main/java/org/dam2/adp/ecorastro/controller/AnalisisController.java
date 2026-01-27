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

/**
 * Controlador para la vista de Análisis y Reportes.
 * <p>
 * Muestra gráficos detallados, estadísticas y permite exportar datos.
 * <p>
 * Funcionalidades principales:
 * <ul>
 * <li>Filtrado de datos por rango de fechas (Día, Semana, Mes, Todo).</li>
 * <li>Visualización de KPIs (Total CO2, Hábito frecuente).</li>
 * <li>Gráficos interactivos: Distribución por categoría (PieChart) y Comparativa (BarChart).</li>
 * <li>Generación de insights y recomendaciones basadas en hábitos.</li>
 * <li>Exportación de reportes a formato CSV.</li>
 * </ul>
 *
 * @author TuNombre
 * @version 1.0
 */
public class AnalisisController {

    // --- ELEMENTOS FXML ---

    /** Checkbox para alternar entre vista desglosada o global en el gráfico comparativo. */
    public CheckBox chkDesglose;

    /** Desplegable para seleccionar el rango de tiempo a analizar. */
    @FXML private ComboBox<String> cmbRango;

    /** Selector de fecha para definir el punto de referencia del análisis. */
    @FXML private DatePicker dpFecha;

    /** Etiqueta para mostrar el total de emisiones en el periodo seleccionado. */
    @FXML private Label lblTotalPeriodo;

    /** Etiqueta para mostrar la actividad más frecuente (Hábito). */
    @FXML private Label lblActividadFrecuente;

    /** Etiqueta para mostrar un consejo o insight basado en los datos. */
    @FXML private Label lblInsight;

    /** Gráfico circular para mostrar la distribución de emisiones. */
    @FXML private PieChart pieChart;

    /** Gráfico de barras para comparar emisiones con la media o desglosar por categoría. */
    @FXML private BarChart<String, Number> barChart;

    // --- SERVICIOS ---

    /** Servicio para gestionar huellas de carbono. */
    private final HuellaService huellaService = new HuellaService();

    /** Servicio para gestionar hábitos del usuario. */
    private final HabitoService habitoService = new HabitoService();

    /** Servicio para generar recomendaciones. */
    private final RecomendacionService recomendacionService = new RecomendacionService();

    // --- DATOS ---

    /** Lista de huellas filtradas según los criterios seleccionados. */
    private List<Huella> huellasFiltradas;

    /** Fecha de inicio del filtro actual (null si es "Todo"). */
    private LocalDate fechaInicioFiltro;
    /** Fecha de fin del filtro actual (null si es "Todo"). */
    private LocalDate fechaFinFiltro;

    /**
     * Inicializa el controlador de análisis.
     * <p>
     * Configura los filtros de fecha y rango, y carga los datos iniciales.
     * <ol>
     * <li>Configura las opciones del ComboBox de rango.</li>
     * <li>Establece la fecha actual por defecto.</li>
     * <li>Añade listeners para recargar datos al cambiar filtros.</li>
     * <li>Realiza la carga inicial de datos.</li>
     * </ol>
     */
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
     * <p>
     * Recupera huellas, actualiza el dashboard y genera insights.
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
     * Calcula las fechas de inicio y fin según el rango seleccionado
     * y solicita al servicio las huellas correspondientes.
     *
     * @param idUsuario ID del usuario actual.
     */
    private void cargarHuellasDesdeService(int idUsuario) {
        String rango = cmbRango.getValue();

        // Si dice "Todo", usamos EL MISMO MÉTODO que Inicio para que cuadre al céntimo.
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

    /**
     * Actualiza los KPIs y gráficos del dashboard con los datos filtrados.
     */
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
     * Genera una recomendación basada en el hábito más frecuente del usuario.
     *
     * @param idUsuario ID del usuario.
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

    /**
     * Actualiza el gráfico circular (PieChart) con la distribución de emisiones por categoría.
     */
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

        // --- CONFIGURACIÓN DE COLORES ---
        // Asignamos colores específicos a cada categoría para mantener consistencia visual
        for (PieChart.Data data : pieChart.getData()) {
            String color = getColorPorCategoria(data.getName());
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-pie-color: " + color + ";");
            }
        }
        // Coloreamos la leyenda para que coincida
        javafx.application.Platform.runLater(this::colorearLeyenda);
    }

    /**
     * Aplica los colores personalizados a los elementos de la leyenda del gráfico.
     */
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

    /**
     * Devuelve el color hexadecimal asociado a una categoría.
     * @param categoria Nombre de la categoría.
     * @return Código de color hexadecimal.
     */
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

    /**
     * Actualiza el gráfico de barras comparativo (BarChart).
     * <p>
     * Permite alternar entre vista desglosada por categorías o totales globales.
     */
    private void actualizarGraficoComparativo() {
        // A. Preparar Mis Datos (siempre los calculamos)
        Map<String, Double> misTotales = new HashMap<>();
        for (Huella h : huellasFiltradas) {
            String cat = h.getIdActividad().getIdCategoria().getNombre();
            double co2 = h.getValor().doubleValue() * h.getIdActividad().getIdCategoria().getFactorEmision().doubleValue();
            misTotales.put(cat, misTotales.getOrDefault(cat, 0.0) + co2);
        }

        // B. Preparar Datos Comunidad (Ahora RESPETA el filtro de fechas)
        Map<String, Double> mediaComunidad;
        if (fechaInicioFiltro == null || fechaFinFiltro == null) {
            // Si es "Todo el historial", pedimos la media histórica total
            mediaComunidad = huellaService.getMediaImpactoPorCategoria();
        } else {
            // Si hay un rango seleccionado, pedimos la media de ese rango
            mediaComunidad = huellaService.getMediaImpactoPorCategoriaFechas(fechaInicioFiltro, fechaFinFiltro);
        }

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
           double miTotalGlobal = 0.0;
           for (double v : misTotales.values()) miTotalGlobal += v;

           double mediaTotalGlobal = 0.0;
           for (double v : mediaComunidad.values()) mediaTotalGlobal += v;

            // Creamos una única barra llamada "Global"
            serieYo.getData().add(new XYChart.Data<>("Global", miTotalGlobal));
            serieMedia.getData().add(new XYChart.Data<>("Global", mediaTotalGlobal));
        }

        // E. Pintar
        barChart.getData().clear();
        barChart.getData().addAll(serieYo, serieMedia);
    }

    /**
     * Exporta los datos filtrados actuales a un archivo CSV.
     *
     * @param event Evento de acción.
     */
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