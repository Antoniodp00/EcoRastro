package org.dam2.adp.ecorastro.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.dam2.adp.ecorastro.model.Actividad;
import org.dam2.adp.ecorastro.service.HuellaService;
import org.dam2.adp.ecorastro.util.AlertUtils;
import org.dam2.adp.ecorastro.util.SessionManager;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para la ventana de registro de nuevas huellas de carbono.
 * <p>
 * Esta clase gestiona el formulario donde el usuario introduce los datos de su actividad.
 * Se suele invocar como una ventana modal desde {@link HistorialHuellasController}.
 * <p>
 * Funcionalidades principales:
 * <ul>
 * <li>Selección de actividad mediante un desplegable (ComboBox).</li>
 * <li>Validación de fechas (impide seleccionar fechas futuras).</li>
 * <li>Validación de valores numéricos para el consumo.</li>
 * <li>Guardado de la huella en base de datos mediante {@link HuellaService}.</li>
 * </ul>
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class RegisterHuellaController {


    /** Desplegable para seleccionar la actividad (ej: Conducir coche, Electricidad). */
    public ComboBox<Actividad> cmbActividad;

    /** Campo de texto para introducir el valor del consumo (km, kWh, etc.). */
    public TextField txtValor;

    /** Etiqueta que se actualiza automáticamente para mostrar la unidad de la actividad seleccionada. */
    public Label lblUnidad;

    /** Selector de fecha para indicar cuándo se realizó la actividad. */
    public DatePicker dpFecha;


    /** Servicio encargado de la lógica de negocio y persistencia de huellas. */
    private final HuellaService huellaService = new HuellaService();

    /**
     * Inicializa el controlador.
     * <p>
     * Configura el estado inicial del formulario:
     * <ol>
     * <li>Establece la fecha actual por defecto.</li>
     * <li>Configura el `DateCellFactory` para deshabilitar visualmente días futuros en el calendario.</li>
     * <li>Carga la lista de actividades disponibles en el ComboBox.</li>
     * <li>Añade un listener para actualizar la etiqueta de unidad (km, kg...) según la actividad elegida.</li>
     * </ol>
     */
    public void initialize() {
        dpFecha.setValue(LocalDate.now());

        // Deshabilitar días futuros en el calendario visual
        dpFecha.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                // Deshabilita si la fecha es posterior a hoy
                setDisable(empty || date.isAfter(LocalDate.now()));
            }
        });

        cargarActividades();

        cmbActividad.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Accedemos a la Categoría para saber si son "km", "kg", "kWh", etc.
                lblUnidad.setText(newVal.getIdCategoria().getUnidad());
            } else {
                lblUnidad.setText("unidades");
            }
        });
    }

    /**
     * Carga todas las actividades disponibles desde la base de datos y las añade al ComboBox.
     * <p>
     * Configura un {@link StringConverter} para que el ComboBox muestre el nombre de la actividad
     * en lugar del `toString()` del objeto Java.
     */
    private void cargarActividades() {
        List<Actividad> actividades = huellaService.getAllActividades();
        cmbActividad.getItems().addAll(actividades);

        cmbActividad.setConverter(new StringConverter<Actividad>() {
            @Override
            public String toString(Actividad actividad) {
                return (actividad != null) ? actividad.getNombre() : "";
            }

            @Override
            public Actividad fromString(String s) {
                return null; // No necesitamos convertir de String a Objeto para este caso de uso
            }
        });
    }

    /**
     * Gestiona el evento de clic en el botón "Guardar".
     * <p>
     * Realiza las siguientes validaciones y acciones:
     * <ol>
     * <li>Verifica que se haya seleccionado una actividad.</li>
     * <li>Valida que el valor introducido sea un número positivo válido.</li>
     * <li><b>Valida que la fecha no sea futura</b> (regla de negocio).</li>
     * <li>Si todo es correcto, llama a {@link HuellaService#addHuella} para persistir el dato.</li>
     * <li>Si se guarda con éxito, muestra confirmación y cierra la ventana modal.</li>
     * </ol>
     *
     * @param actionEvent El evento de clic.
     */
    public void onGuardarClick(ActionEvent actionEvent) {
        try{
            Actividad actividad = cmbActividad.getValue();
            if (actividad == null){
                AlertUtils.error("Por favor, selecciona una actividad.");
                return;
            }

            String valorTexto = txtValor.getText();
            if(valorTexto == null || valorTexto.isBlank() || !valorTexto.matches("\\d+(\\.\\d+)?")){
                AlertUtils.error("Introduce una cantidad válida.");
                return;
            }

            double valorConsumo = Double.parseDouble(valorTexto);
            LocalDate fecha = dpFecha.getValue();

            if (fecha.isAfter(LocalDate.now())) {
                AlertUtils.error("No puedes registrar una huella con fecha futura.");
                return;
            }

            boolean insertado = huellaService.addHuella(
                    SessionManager.getInstance().getUsuarioActual(),
                    actividad,
                    valorConsumo,
                    fecha
            );

            if (insertado){
                AlertUtils.info("Huella registrada correctamente.");
                cerrarVentana();
            } else {
                AlertUtils.error("Error al registrar huella.");
            }
        } catch (Exception e){
            e.printStackTrace();
            AlertUtils.error("Error al registrar huella.");
        }
    }

    /**
     * Gestiona el evento de clic en el botón "Cancelar".
     * <p>
     * Cierra la ventana actual sin guardar cambios.
     *
     * @param actionEvent El evento de clic.
     */
    public void onCancelarClick(ActionEvent actionEvent) {
        cerrarVentana();
    }

    /**
     * Cierra la ventana (Stage) actual.
     * <p>
     * Se utiliza {@code stage.close()} porque esta vista está diseñada para abrirse
     * como una ventana modal (pop-up) sobre el historial. Al cerrarse, el control
     * regresa a la ventana padre.
     */
    private void cerrarVentana() {
        Stage stage = (Stage) txtValor.getScene().getWindow();
        stage.close();
    }
}