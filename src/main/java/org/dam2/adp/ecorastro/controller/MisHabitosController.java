package org.dam2.adp.ecorastro.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.dam2.adp.ecorastro.model.Actividad;
import org.dam2.adp.ecorastro.model.Habito;
import org.dam2.adp.ecorastro.model.Usuario;
import org.dam2.adp.ecorastro.service.HabitoService;
import org.dam2.adp.ecorastro.service.RecomendacionService;
import org.dam2.adp.ecorastro.util.AlertUtils;
import org.dam2.adp.ecorastro.util.SessionManager;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

/**
 * Controlador para la gestión de Hábitos del usuario.
 * <p>
 * Permite añadir, visualizar y eliminar hábitos recurrentes.
 * <p>
 * Funcionalidades principales:
 * <ul>
 * <li>Visualización de hábitos en tarjetas interactivas.</li>
 * <li>Formulario para añadir nuevos hábitos (Actividad, Frecuencia, Tipo).</li>
 * <li>Filtrado de hábitos por categoría.</li>
 * <li>Eliminación de hábitos existentes mediante menú contextual.</li>
 * <li>Visualización de recomendaciones contextuales al seleccionar un hábito.</li>
 * </ul>
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class MisHabitosController {

    /** Contenedor fluido para mostrar las tarjetas de hábitos. */
    @FXML private FlowPane contenedorHabitos;

    // --- FORMULARIO ---
    /** Desplegable para seleccionar la actividad del hábito. */
    @FXML private ComboBox<Actividad> cmbActividad;
    /** Campo de texto para la frecuencia numérica (ej: 3 veces). */
    @FXML private TextField txtCantidad;
    /** RadioButton para frecuencia diaria. */
    @FXML private RadioButton rbDiario;
    /** RadioButton para frecuencia semanal. */
    @FXML private RadioButton rbSemanal;
    /** RadioButton para frecuencia mensual. */
    @FXML private RadioButton rbMensual;
    /** Grupo de botones para la periodicidad. */
    @FXML private ToggleGroup grupoPeriodicidad;

    // --- FILTROS ---
    /** Filtro para mostrar hábitos de transporte. */
    @FXML private CheckBox chkTransporte;
    /** Filtro para mostrar hábitos de alimentación. */
    @FXML private CheckBox chkAlimentacion;
    /** Filtro para mostrar hábitos de energía. */
    @FXML private CheckBox chkEnergia;
    /** Filtro para mostrar hábitos de agua. */
    @FXML private CheckBox chkAgua;
    /** Filtro para mostrar otros hábitos. */
    @FXML private CheckBox chkOtros;

    // --- INFO ---
    /** Etiqueta lateral para mostrar recomendaciones. */
    @FXML private Label lblRecomendacionSidebar;

    // --- SERVICIOS ---
    /** Servicio para gestionar hábitos. */
    private final HabitoService habitoService = new HabitoService();

    /** Servicio para gestionar huellas (usado para obtener actividades). */
    private final org.dam2.adp.ecorastro.service.HuellaService huellaService = new org.dam2.adp.ecorastro.service.HuellaService();
    /** Servicio para generar recomendaciones. */
    private final RecomendacionService recomendacionService = new RecomendacionService();

    /**
     * Inicializa el controlador de hábitos.
     * <p>
     * Carga las actividades en el combo y muestra los hábitos existentes.
     */
    @FXML
    public void initialize() {
        cargarComboActividades();
        cargarHabitos();
    }

    /**
     * Carga las actividades disponibles en el ComboBox.
     */
    private void cargarComboActividades() {
        List<Actividad> actividades = huellaService.getAllActividades();
        cmbActividad.getItems().addAll(actividades);

        cmbActividad.setConverter(new StringConverter<Actividad>() {
            @Override
            public String toString(Actividad a) { return (a != null) ? a.getNombre() : ""; }
            @Override
            public Actividad fromString(String string) { return null; }
        });
    }

    /**
     * Guarda un nuevo hábito desde el formulario rápido.
     * <p>
     * Valida los datos y llama al servicio para persistir el hábito.
     */
    @FXML
    public void guardarHabitoRapido() {
        Actividad actividad = cmbActividad.getValue();
        String cantidadStr = txtCantidad.getText();

        if (actividad == null || cantidadStr == null || cantidadStr.trim().isEmpty()) {
            AlertUtils.error("Selecciona una actividad e indica la cantidad.");
            return;
        }

        int frecuenciaNum;
        try {
            frecuenciaNum = Integer.parseInt(cantidadStr);
        } catch (NumberFormatException e) {
            AlertUtils.error("La cantidad debe ser un número entero.");
            return;
        }

        String tipoPeriodo = "Diario";
        if (rbSemanal.isSelected()) tipoPeriodo = "Semanal";
        if (rbMensual.isSelected()) tipoPeriodo = "Mensual";

        Usuario usuarioActual = SessionManager.getInstance().getUsuarioActual();
        boolean exito = habitoService.addHabito(usuarioActual, actividad, frecuenciaNum, tipoPeriodo);

        if (exito) {
            cmbActividad.getSelectionModel().clearSelection();
            txtCantidad.clear();
            rbDiario.setSelected(true);
            cargarHabitos();
            AlertUtils.info("¡Hábito añadido correctamente!");
        } else {
            AlertUtils.error("No se pudo guardar. Verifica los datos.");
        }
    }

    /**
     * Carga y muestra los hábitos del usuario en el contenedor.
     * <p>
     * Aplica los filtros de categoría seleccionados.
     */
    @FXML
    public void cargarHabitos() {
        if (contenedorHabitos != null) contenedorHabitos.getChildren().clear();

        int idUsuario = SessionManager.getInstance().getUsuarioActual().getId();
        List<Habito> lista = habitoService.getHabitosByUsuario(idUsuario);

        if (lista.isEmpty()) {
            mostrarMensajeVacio();
            return;
        }

        for (Habito h : lista) {
            String cat = h.getIdActividad().getIdCategoria().getNombre();
            if (!isCategoriaSeleccionada(cat)) continue;

            contenedorHabitos.getChildren().add(crearTarjetaHabito(h));
        }
    }

    /**
     * Verifica si una categoría está seleccionada en los filtros.
     *
     * @param categoria Nombre de la categoría.
     * @return true si debe mostrarse, false en caso contrario.
     */
    private boolean isCategoriaSeleccionada(String categoria) {
        switch (categoria) {
            case "Transporte": return chkTransporte.isSelected();
            case "Alimentación": return chkAlimentacion.isSelected();
            case "Energía": return chkEnergia.isSelected();
            case "Agua": return chkAgua.isSelected();
            default: return chkOtros.isSelected();
        }
    }

    /**
     * Crea una tarjeta visual (VBox) para representar un hábito.
     *
     * @param h El hábito a representar.
     * @return El nodo gráfico de la tarjeta.
     */
    private VBox crearTarjetaHabito(Habito h) {
        VBox card = new VBox(5);
        card.getStyleClass().add("item-card");
        card.setAlignment(Pos.CENTER);

        // A. Icono (FontAwesome)
        String catNombre = h.getIdActividad().getIdCategoria().getNombre();
        FontIcon icon = new FontIcon(getCodigoIcono(catNombre));
        icon.setIconSize(30);
        icon.getStyleClass().add("item-card-icono");

        // B. Título
        Label titulo = new Label(h.getIdActividad().getNombre());
        titulo.getStyleClass().add("item-card-titulo");
        titulo.setWrapText(true);
        titulo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // C. Frecuencia
        String infoFrecuencia = h.getFrecuencia() + " veces / " + h.getTipo();
        Label lblFreq = new Label(infoFrecuencia);
        lblFreq.getStyleClass().add("item-card-valor");
        lblFreq.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-primario; -fx-font-weight: bold;");

        // Añadimos elementos
        card.getChildren().addAll(icon, titulo, lblFreq);

        // --- Interacción: Click para ver consejo ---
        card.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                String consejo = recomendacionService.generarConsejo(catNombre);
                lblRecomendacionSidebar.setText("Tip para " + catNombre + ":\n" + consejo);
            }
        });

        // Menú Contextual
        ContextMenu menu = new ContextMenu();
        MenuItem itemBorrar = new MenuItem("Dejar este hábito");
        itemBorrar.setGraphic(new FontIcon("fas-trash"));
        itemBorrar.setOnAction(e -> eliminarHabito(h));
        menu.getItems().add(itemBorrar);

        card.setOnContextMenuRequested(e -> menu.show(card, e.getScreenX(), e.getScreenY()));

        return card;
    }

    /**
     * Elimina un hábito tras confirmación del usuario.
     *
     * @param h El hábito a eliminar.
     */
    private void eliminarHabito(Habito h) {
        if (AlertUtils.confirmacion("Eliminar Hábito", "Confirmar acción",
                "¿Deseas dejar de seguir el hábito '" + h.getIdActividad().getNombre() + "'?")) {

            if (habitoService.deleteHabito(h)) {
                AlertUtils.info("Hábito eliminado.");
                cargarHabitos();
            } else {
                AlertUtils.error("No se pudo eliminar.");
            }
        }
    }

    /**
     * Muestra un mensaje visual cuando no hay hábitos registrados.
     */
    private void mostrarMensajeVacio() {
        VBox vacio = new VBox(10);
        vacio.setAlignment(Pos.CENTER);
        vacio.setPrefWidth(400);

        FontIcon icono = new FontIcon("fas-seedling");
        icono.setIconSize(40);
        icono.setStyle("-fx-fill: -color-texto-secundario;");

        Label msg = new Label("No tienes hábitos registrados.");
        msg.setStyle("-fx-text-fill: -color-texto-secundario; -fx-font-size: 16px;");

        vacio.getChildren().addAll(icono, msg);
        contenedorHabitos.getChildren().add(vacio);
    }

    /**
     * Obtiene el código del icono FontAwesome según la categoría.
     *
     * @param categoria Nombre de la categoría.
     * @return Código del icono (ej: "fas-car").
     */
    private String getCodigoIcono(String categoria) {
        if (categoria == null) return "fas-leaf";
        return switch (categoria) {
            case "Transporte" -> "fas-car";
            case "Alimentación" -> "fas-apple-alt";
            case "Energía" -> "fas-bolt";
            case "Agua" -> "fas-tint";
            case "Residuos" -> "fas-recycle";
            default -> "fas-box-open";
        };
    }
}