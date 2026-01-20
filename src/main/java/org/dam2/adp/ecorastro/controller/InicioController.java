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

/**
 * Controlador principal para la pantalla de "Inicio" (Dashboard) de la aplicación.
 * <p>
 * Esta clase se encarga de gestionar la vista resumen que ve el usuario al loguearse.
 * Sus responsabilidades incluyen:
 * <ul>
 * <li>Mostrar el KPI principal: Huella de carbono total acumulada.</li>
 * <li>Visualizar una comparativa gráfica entre el usuario y la media global.</li>
 * <li>Gestionar la lógica de gamificación (Nivel del usuario basado en emisiones).</li>
 * <li>Mostrar consejos aleatorios para concienciación ambiental.</li>
 * <li>Gestionar la navegación rápida a otras secciones de la app.</li>
 * </ul>
 *
 * @author TuNombre
 * @version 1.0
 */
public class InicioController {
    public Label lblIconoNivel;
    public Label lblSiguienteNivel;
    public Label lblAhorroRestante;

    // --- ELEMENTOS FXML ---
    /** Etiqueta para mostrar el valor numérico total de emisiones de CO2. */
    @FXML private Label lblHuellaTotal;

    /** Etiqueta para mostrar un consejo o "insight" aleatorio. */
    @FXML private Label lblConsejo;

    /** Gráfico de barras para comparar el impacto del usuario con la comunidad. */
    @FXML private BarChart<String, Number> barChart;

    /** Barra de progreso visual que representa el nivel ecológico del usuario. */
    @FXML private ProgressBar pbNivel;

    /** Etiqueta de texto que indica el rango o título del nivel actual (ej: "Guardián"). */
    @FXML private Label lblNivel;

    // --- SERVICIOS ---
    /** Servicio para la gestión y recuperación de datos de huellas de carbono. */
    private final HuellaService huellaService = new HuellaService();

    /** Servicio para generar recomendaciones y consejos basados en categorías. */
    private final RecomendacionService recomendacionService = new RecomendacionService();

    /** * Variable de estado para almacenar el total de emisiones calculado.
     * Se calcula en {@link #cargarDatosReales()} y se reutiliza en gráficas y gamificación.
     */
    private double totalEmisionesUsuario = 0.0;

    /**
     * Método de inicialización del controlador.
     * <p>
     * Se ejecuta automáticamente tras cargar el archivo FXML. Orquesta la llamada
     * a los distintos métodos de carga de datos y configuración visual.
     */
    public void initialize() {
        cargarDatosReales();
        configurarGraficoResumen();
        calcularNivelGamificacion();
        mostrarConsejoDelDia();
    }

    /**
     * Recupera las huellas del usuario actual desde la base de datos y calcula el impacto total.
     * <p>
     * Lógica:
     * <ol>
     * <li>Obtiene el ID del usuario logueado mediante {@link SessionManager}.</li>
     * <li>Recupera la lista completa de huellas a través de {@link HuellaService}.</li>
     * <li>Itera sobre las huellas sumando el producto de (Valor * Factor de Emisión).</li>
     * <li>Actualiza la etiqueta {@code lblHuellaTotal} con el resultado formateado.</li>
     * </ol>
     */
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

    /**
     * Configura y rellena el gráfico de barras comparativo.
     * <p>
     * Muestra dos barras:
     * <ul>
     * <li><b>Tú:</b> El total de emisiones del usuario actual.</li>
     * <li><b>Media Global:</b> El promedio de emisiones calculado desde la base de datos.</li>
     * </ul>
     * Si no hay datos globales, se establece un valor base por defecto para mantener la estética.
     */
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
     * Calcula y muestra el nivel de gamificación del usuario.
     * <p>
     * El nivel se determina inversamente a la cantidad de emisiones:
     * <ul>
     * <li><b>Guardián del Bosque:</b> < 50 kg CO2.</li>
     * <li><b>Árbol Joven:</b> Entre 50 y 200 kg CO2.</li>
     * <li><b>Semilla:</b> > 200 kg CO2.</li>
     * </ul>
     * Actualiza tanto el texto del nivel como la barra de progreso visual.
     */
    private void calcularNivelGamificacion() {
        String nivelTexto;
        String icono;
        String objetivoTexto;
        String faltaTexto;
        double progreso;

        // LÍMITES: <50 (Bueno), 50-200 (Normal), >200 (Malo)

        if (totalEmisionesUsuario < 50) {
            // --- NIVEL ÓPTIMO ---
            nivelTexto = "Bajo Consumo";
            icono = "🌿";
            objetivoTexto = "Objetivo: ¡Mantener!";
            faltaTexto = "¡Estás en el nivel máximo!";
            progreso = 1.0; // Barra llena de "energía verde"

            // Estilos opcionales (color verde)
            if (lblNivel != null) lblNivel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 20px;");

        } else if (totalEmisionesUsuario < 200) {
            // --- NIVEL MEDIO ---
            nivelTexto = "Consumo Moderado";
            icono = "⚖️";

            // Calculamos cuánto sobra para bajar al nivel bueno (50)
            double exceso = totalEmisionesUsuario - 50;
            objetivoTexto = "Siguiente: Bajo Consumo";
            faltaTexto = String.format("Reduce %.1f kg para subir nivel", exceso);

            // Progreso relativo: 1.0 si estás en 50, 0.0 si estás en 200
            // Fórmula: (200 - actual) / (200 - 50)
            progreso = (200 - totalEmisionesUsuario) / 150.0;

            if (lblNivel != null) lblNivel.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold; -fx-font-size: 20px;");

        } else {
            // --- NIVEL MALO ---
            nivelTexto = "Alto Consumo";
            icono = "🔥";

            // Calculamos cuánto sobra para bajar al nivel moderado (200)
            double exceso = totalEmisionesUsuario - 200;
            objetivoTexto = "Siguiente: Moderado";
            faltaTexto = String.format("Reduce %.1f kg para mejorar", exceso);

            progreso = 0.1; // Barra en rojo/casi vacía

            if (lblNivel != null) lblNivel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 20px;");
        }

        // --- ACTUALIZAR INTERFAZ ---
        if (lblIconoNivel != null) lblIconoNivel.setText(icono);
        if (lblNivel != null) lblNivel.setText(nivelTexto);
        if (lblSiguienteNivel != null) lblSiguienteNivel.setText(objetivoTexto);
        if (lblAhorroRestante != null) lblAhorroRestante.setText(faltaTexto);
        if (pbNivel != null) pbNivel.setProgress(progreso);
    }

    /**
     * Selecciona una categoría aleatoria y muestra un consejo relacionado.
     * <p>
     * Utiliza el {@link RecomendacionService} para obtener el texto del consejo.
     * Esto proporciona dinamismo a la pantalla de inicio cada vez que se carga.
     */
    private void mostrarConsejoDelDia() {
        // Elegimos una categoría aleatoria para variar el consejo cada vez que entras
        String[] categorias = {"Energía", "Transporte", "Agua", "Residuos", "Alimentación"};
        String categoriaRandom = categorias[(int) (Math.random() * categorias.length)];

        String consejo = recomendacionService.generarConsejo(categoriaRandom);
        lblConsejo.setText(consejo);
    }

    // --- NAVEGACIÓN ---

    /**
     * Maneja el evento de clic para navegar a la pantalla de Historial de Huellas.
     * Se usa normalmente como atajo desde el Dashboard para registrar una nueva huella.
     *
     * @param event El evento del ratón que disparó la acción.
     */
    @FXML
    public void irARegistrarHuella(MouseEvent event) {
        // Asumiendo que quieres ir al formulario de alta rápida o historial
        Navigation.navigate("historial_huellas.fxml");
    }

    /**
     * Maneja el evento de clic para navegar a la pantalla de "Mis Hábitos".
     *
     * @param e El evento del ratón que disparó la acción.
     */
    @FXML
    public void irAHabitos(MouseEvent e) {
        Navigation.navigate("mis_habitos.fxml");
    }

    /**
     * Maneja el evento de clic para navegar a la pantalla de "Análisis".
     *
     * @param e El evento del ratón que disparó la acción.
     */
    @FXML
    public void irAAnalisis(MouseEvent e) {
        Navigation.navigate("analisis.fxml");
    }
}