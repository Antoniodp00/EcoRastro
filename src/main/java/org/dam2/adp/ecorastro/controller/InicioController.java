package org.dam2.adp.ecorastro.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import org.dam2.adp.ecorastro.service.HuellaService;
import org.dam2.adp.ecorastro.service.RecomendacionService;
import org.dam2.adp.ecorastro.service.UsuarioService;
import org.dam2.adp.ecorastro.util.Navigation;
import org.dam2.adp.ecorastro.util.SessionManager;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * Controlador principal para la pantalla de "Inicio" (Dashboard).
 * <p>
 * Gestiona el KPI principal, la gráfica comparativa y la gamificación
 * con ranking contextual (Posición vs Total Usuarios).
 *
 * @author Antonio Delgado Portero
 * @version 2.1 (Corrección Gráfico Comparativo)
 */
public class InicioController {

    @FXML private Label lblHuellaTotal;
    @FXML private Label lblConsejo;
    @FXML private BarChart<String, Number> barChart;
    @FXML private ProgressBar pbNivel;
    @FXML private Label lblNivel;
    @FXML private Label lblSiguienteNivel;
    @FXML private Label lblAhorroRestante;
    @FXML private FontIcon iconNivel;

    private final HuellaService huellaService = new HuellaService();
    private final RecomendacionService recomendacionService = new RecomendacionService();
    private final UsuarioService usuarioService = new UsuarioService();

    private double totalEmisionesMes = 0.0;

    /**
     * Inicializa el controlador de inicio.
     * <p>
     * Carga los datos reales, configura los gráficos y calcula el nivel de gamificación.
     */
    public void initialize() {
        cargarDatosReales();
        configurarGraficoResumen();
        calcularNivelGamificacion();
        mostrarConsejoDelDia();
    }

    /**
     * Carga el KPI principal: Huella del mes actual.
     * <p>
     * Obtiene el total de emisiones del usuario para el mes en curso y actualiza la etiqueta correspondiente.
     */
    private void cargarDatosReales() {
        int idUsuario = SessionManager.getInstance().getUsuarioActual().getId();
        LocalDate inicioMes = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate finMes = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());


        totalEmisionesMes = huellaService.getTotalImpactoUsuarioFecha(idUsuario, inicioMes, finMes);
        lblHuellaTotal.setText(String.format("%.2f kg CO₂", totalEmisionesMes));
    }

    /**
     * Configura el gráfico de barras: Tu Histórico vs Media Histórica del Resto.
     * <p>
     * Corrección: Ahora usa 'getMediaComunidadSinUsuario' para que la comparación sea justa
     * (Promedio vs Promedio) y no (Promedio vs Suma Total).
     */
    private void configurarGraficoResumen() {
        int idUsuario = SessionManager.getInstance().getUsuarioActual().getId();
        barChart.getData().clear();
        barChart.setAnimated(false);

        double miImpactoHistorico = huellaService.getTotalImpactoUsuarioFecha(
                idUsuario,
                LocalDate.of(1970, 1, 1),
                LocalDate.now().plusDays(1)
        );

        XYChart.Series<String, Number> serieYo = new XYChart.Series<>();
        serieYo.setName("Tú (Total)");
        serieYo.getData().add(new XYChart.Data<>("", miImpactoHistorico));

        double mediaRestoComunidad = huellaService.getMediaComunidadSinUsuario(idUsuario);

        if (mediaRestoComunidad <= 0.1) mediaRestoComunidad = 10.0;

        XYChart.Series<String, Number> serieMedia = new XYChart.Series<>();
        serieMedia.setName("Media Comunidad");
        serieMedia.getData().add(new XYChart.Data<>("", mediaRestoComunidad));

        barChart.getData().addAll(serieYo, serieMedia);

        // Ajustes estéticos
        barChart.setCategoryGap(40);
        barChart.setBarGap(10);
    }

    /**
     * Calcula el nivel del usuario y su posición en el ranking.
     * <p>
     * Determina el nivel (Eco-Héroe, Consumo Consciente, Inicio del Cambio) basado en el ranking
     * y las emisiones del mes. Actualiza la interfaz con el progreso y mensajes motivacionales.
     */
    private void calcularNivelGamificacion() {
        int idUsuario = SessionManager.getInstance().getUsuarioActual().getId();

        Long ranking = huellaService.getRankingUsuario(idUsuario);
        Long totalUsuarios = usuarioService.countUsuariosActivos();

        String nivelTexto;
        String iconCode;
        String objetivoTexto;
        String faltaTexto;
        double progreso;
        String colorTema;

        // NIVEL 1: ECO-HÉROE (Top Rank o emisiones muy bajas este mes)
        if (ranking > 0 && (ranking <= 10 || totalEmisionesMes < 50)) {
            nivelTexto = "Eco-Héroe";
            iconCode = "fas-star";
            colorTema = "#656D4A";
            objetivoTexto = (ranking > 0) ? String.format("Ranking: #%d / %d", ranking, totalUsuarios) : "Ranking: Calculando...";
            faltaTexto = "¡Eres un líder en sostenibilidad!";
            progreso = 1.0;

            // NIVEL 2: CONSUMO CONSCIENTE
        } else if (totalEmisionesMes < 150) {
            nivelTexto = "Consumo Consciente";
            iconCode = "fas-leaf";
            colorTema = "#DDA15E";
            double exceso = totalEmisionesMes - 50;
            objetivoTexto = "Siguiente: Eco-Héroe";
            faltaTexto = String.format("Reduce %.1f kg más", exceso);
            progreso = (150 - totalEmisionesMes) / 100.0;

            // NIVEL 3: INICIO DEL CAMBIO
        } else {
            nivelTexto = "Inicio del Cambio";
            iconCode = "fas-fire";
            colorTema = "#bc4749";
            double exceso = totalEmisionesMes - 150;
            objetivoTexto = "Siguiente: Consciente";
            faltaTexto = String.format("Reduce %.1f kg para subir", exceso);
            progreso = 0.15;
        }

        aplicarEstilosUI(nivelTexto, iconCode, objetivoTexto, faltaTexto, progreso, colorTema);
    }

    /**
     * Aplica los estilos visuales a los elementos de la interfaz de gamificación.
     *
     * @param titulo Título del nivel.
     * @param icono Código del icono FontAwesome.
     * @param objetivo Texto del objetivo siguiente.
     * @param mensaje Mensaje de progreso restante.
     * @param valorBarra Valor de progreso (0.0 a 1.0).
     * @param colorHex Código de color hexadecimal.
     */
    private void aplicarEstilosUI(String titulo, String icono, String objetivo, String mensaje, double valorBarra, String colorHex) {
        if (lblNivel != null) {
            lblNivel.setText(titulo);
            lblNivel.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: bold; -fx-font-size: 20px;");
        }
        if (iconNivel != null) {
            iconNivel.setIconLiteral(icono);
            iconNivel.setIconColor(javafx.scene.paint.Paint.valueOf(colorHex));
        }
        if (lblSiguienteNivel != null) lblSiguienteNivel.setText(objetivo);
        if (lblAhorroRestante != null) lblAhorroRestante.setText(mensaje);
        if (pbNivel != null) {
            pbNivel.setProgress(valorBarra);
            pbNivel.setStyle("-fx-accent: " + colorHex + ";");
        }
    }

    /**
     * Muestra un consejo aleatorio en la pantalla de inicio.
     */
    private void mostrarConsejoDelDia() {
        String[] categorias = {"Energía", "Transporte", "Agua", "Residuos", "Alimentación"};
        String categoriaRandom = categorias[(int) (Math.random() * categorias.length)];
        lblConsejo.setText(recomendacionService.generarConsejo(categoriaRandom));
    }

    // --- NAVEGACIÓN ---
    /**
     * Navega a la vista de registro de huella.
     * @param event Evento del ratón.
     */
    @FXML public void irARegistrarHuella(MouseEvent event) { Navigation.navigate("historial_huellas.fxml"); }
    /**
     * Navega a la vista de mis hábitos.
     * @param e Evento del ratón.
     */
    @FXML public void irAHabitos(MouseEvent e) { Navigation.navigate("mis_habitos.fxml"); }
    /**
     * Navega a la vista de análisis.
     * @param e Evento del ratón.
     */
    @FXML public void irAAnalisis(MouseEvent e) { Navigation.navigate("analisis.fxml"); }
}