package org.dam2.adp.ecorastro.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import org.dam2.adp.ecorastro.DAO.ActividadDAO;
import org.dam2.adp.ecorastro.model.Actividad;
import org.dam2.adp.ecorastro.model.Habito;
import org.dam2.adp.ecorastro.model.HabitoId;
import org.dam2.adp.ecorastro.service.HabitoService;
import org.dam2.adp.ecorastro.service.RecomendacionService;
import org.dam2.adp.ecorastro.util.AlertUtils;
import org.dam2.adp.ecorastro.util.SessionManager;

/**
 * Controlador para la gestión de Hábitos del usuario.
 * <p>
 * Permite añadir, visualizar y eliminar hábitos recurrentes.
 * <p>
 * Funcionalidades principales:
 * <ul>
 * <li>Visualización de hábitos en una tabla.</li>
 * <li>Formulario para añadir nuevos hábitos (Actividad, Frecuencia, Tipo).</li>
 * <li>Eliminación de hábitos existentes.</li>
 * <li>Visualización de recomendaciones contextuales al seleccionar un hábito.</li>
 * </ul>
 *
 * @author TuNombre
 * @version 1.0
 */
public class MisHabitosController {

    // --- ELEMENTOS FXML ---

    /** Desplegable para seleccionar la actividad del hábito. */
    public ComboBox<Actividad> cmbActividad;

    /** Campo de texto para la frecuencia numérica (ej: 3 veces). */
    public TextField txtFrecuencia;

    /** Desplegable para el tipo de frecuencia (Diario, Semanal, Mensual). */
    public ComboBox<String> cmbTipo;

    /** Etiqueta para mostrar recomendaciones asociadas al hábito seleccionado. */
    public Label lblRecomendacion;

    /** Tabla para listar los hábitos del usuario. */
    public TableView<Habito> tablaHabitos;

    /** Columna para el nombre de la actividad. */
    public TableColumn<Habito, String> colActividad;

    /** Columna para la categoría de la actividad. */
    public TableColumn<Habito, String> colCategoria;

    /** Columna para la frecuencia del hábito. */
    public TableColumn<Habito, String> colFrecuencia;

    /** Columna para el tipo de periodicidad. */
    public TableColumn<Habito, String> colTipo;

    // --- SERVICIOS ---

    /** Servicio para gestionar hábitos. */
    private final HabitoService habitoService = new HabitoService();

    /** Servicio para generar recomendaciones. */
    private final RecomendacionService recomendacionService = new RecomendacionService();

    /** DAO para acceder a las actividades disponibles. */
    private final ActividadDAO actividadDAO = new ActividadDAO();

    /** Lista observable para mantener los datos de la tabla. */
    private ObservableList<Habito> listaHabitos = FXCollections.observableArrayList();

    /**
     * Inicializa el controlador de hábitos.
     * <p>
     * Configura la tabla, carga los combos y los datos iniciales.
     * <ol>
     * <li>Configura las columnas de la tabla.</li>
     * <li>Carga las opciones en los desplegables.</li>
     * <li>Carga los datos de hábitos del usuario.</li>
     * <li>Añade un listener para mostrar consejos al seleccionar una fila.</li>
     * </ol>
     */
    public void initialize() {
        configurarTabla();
        cargarCombos();
        cargarDatosTabla();

        tablaHabitos.getSelectionModel().selectedItemProperty().addListener(
                (observable, seleccionAnterior, seleccionNueva) -> mostrarConsejo(seleccionNueva)
        );
    }

    /**
     * Configura las columnas de la tabla de hábitos.
     * <p>
     * Define cómo se muestran los datos en cada columna.
     */
    private void configurarTabla() {
        colActividad.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getIdActividad().getNombre()));

        colCategoria.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getIdActividad().getIdCategoria().getNombre())
        );
        colFrecuencia.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getFrecuencia() + " veces")));

        colTipo.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getTipo())
        );
        colFrecuencia.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: -color-primario; -fx-font-weight: bold;");
    }

    /**
     * Carga las opciones en los ComboBox de actividad y tipo de frecuencia.
     */
    private void cargarCombos() {
        cmbTipo.getItems().addAll("Diario", "Semanal", "Mensual");

        cmbActividad.setItems(FXCollections.observableArrayList(actividadDAO.getAllActividades()));

        cmbActividad.setConverter(new StringConverter<>() {
            @Override
            public String toString(Actividad actividad) {
                return (actividad != null) ? actividad.getNombre() : "";
            }

            @Override
            public Actividad fromString(String s) {
                return null;
            }
        });
    }

    /**
     * Carga los hábitos del usuario actual en la tabla.
     */
    private void cargarDatosTabla() {
        listaHabitos.clear();
        listaHabitos.addAll(habitoService.getHabitosByUsuario(
                SessionManager.getInstance().getUsuarioActual().getId()
        ));
        tablaHabitos.setItems(listaHabitos);
    }

    /**
     * Guarda un nuevo hábito en la base de datos.
     * <p>
     * Valida los campos antes de proceder. Si es exitoso, actualiza la tabla.
     *
     * @param actionEvent Evento de acción.
     */
    public void guardarHabito(ActionEvent actionEvent) {
        Actividad actividad = cmbActividad.getValue();
        String frecuenciaStr = txtFrecuencia.getText();
        String tipo = cmbTipo.getValue();

        if (actividad == null || frecuenciaStr == null || tipo == null) {
            AlertUtils.error("Por favor, rellena todos los campos.");
            return;
        }

        try {
            int frecuencia = Integer.parseInt(frecuenciaStr);

            boolean exito = habitoService.addHabito(
                    SessionManager.getInstance().getUsuarioActual(),
                    actividad,
                    frecuencia,
                    tipo
            );

            if (exito){
                AlertUtils.info("Nuevo registro guardado.");
                limpiarFormulario();
                cargarDatosTabla();
            } else {
                AlertUtils.error("Error al guardar el registro.");
            }
        }catch (NumberFormatException e){
            AlertUtils.error("La frecuencia debe ser un número entero.");

        }
    }

    /**
     * Elimina el hábito seleccionado de la tabla.
     * <p>
     * Solicita confirmación al usuario antes de borrar.
     *
     * @param mouseEvent Evento del ratón.
     */
    public void eliminarHabito(MouseEvent mouseEvent) {
        Habito habitoSeleccionado = tablaHabitos.getSelectionModel().getSelectedItem();
        if (habitoSeleccionado == null) {
            AlertUtils.error("Por favor, selecciona un hábito para eliminar.");
            return;
        }

        if (AlertUtils.confirmacion("Eliminar Hábito","Confirmacion", "¿Estás seguro de que quieres eliminar este hábito?")){
            if (habitoService.deleteHabito(habitoSeleccionado)){
                listaHabitos.remove(habitoSeleccionado);
                limpiarFormulario();
            }else {
                AlertUtils.error("Error al eliminar el hábito.");
            }
        }
    }

    /**
     * Muestra un consejo relacionado con la categoría del hábito seleccionado.
     *
     * @param habito El hábito seleccionado.
     */
    private void mostrarConsejo(Habito habito){
        if (habito != null){
            String categoria = habito.getIdActividad().getIdCategoria().getNombre();

            String consejo = recomendacionService.generarConsejo(categoria);

            lblRecomendacion.setText(consejo);
        }else {
            lblRecomendacion.setText("Selecciona un habito para ver consejos.");

        }
    }

    /**
     * Limpia los campos del formulario de creación de hábitos.
     */
    private void limpiarFormulario(){
        cmbActividad.getSelectionModel().clearSelection();
        cmbTipo.getSelectionModel().clearSelection();
        txtFrecuencia.clear();
        lblRecomendacion.setText("Selecciona un habito para ver consejos.");
    }
}