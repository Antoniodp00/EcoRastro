package org.dam2.adp.ecorastro.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.dam2.adp.ecorastro.model.Huella;
import org.dam2.adp.ecorastro.service.HuellaService;
import org.dam2.adp.ecorastro.util.AlertUtils;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Controlador para la ventana modal de "Detalle de Huella".
 * <p>
 * Permite visualizar la información completa de un registro y editar sus valores.
 * <p>
 * Funcionalidades principales:
 * <ul>
 * <li>Visualización detallada (Fecha, Valor, Impacto, Categoría).</li>
 * <li>Modo Edición: Permite modificar la fecha y el valor del consumo.</li>
 * <li>Cálculo dinámico del impacto al actualizar valores.</li>
 * </ul>
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class DetalleHuellaController {


    /**
     * Etiqueta para el icono de la categoría.
     */
    @FXML
    private Label lblIcono;
    /**
     * Etiqueta para el título (nombre de la actividad).
     */
    @FXML
    private Label lblTitulo;
    /**
     * Etiqueta para el nombre de la categoría.
     */
    @FXML
    private Label lblCategoria;
    /**
     * Etiqueta para mostrar el impacto calculado en CO2.
     */
    @FXML
    private Label lblImpacto;


    /**
     * Etiqueta para mostrar la fecha (solo lectura).
     */
    @FXML
    private Label lblFecha;
    /**
     * Etiqueta para mostrar el valor y unidad (solo lectura).
     */
    @FXML
    private Label lblValor;
    /**
     * Botón para activar el modo edición.
     */
    @FXML
    private Button btnEditar;
    /**
     * Botón para cerrar la ventana.
     */
    @FXML
    private Button btnCerrar;


    /**
     * Selector de fecha (modo edición).
     */
    @FXML
    private DatePicker dpFechaInput;
    /**
     * Campo de texto para editar el valor.
     */
    @FXML
    private TextField txtValorInput;
    /**
     * Etiqueta fija con la unidad de medida.
     */
    @FXML
    private Label lblUnidadInput;
    /**
     * Contenedor para los controles de edición de valor.
     */
    @FXML
    private HBox boxEdicionValor;
    /**
     * Botón para guardar los cambios.
     */
    @FXML
    private Button btnGuardar;


    /**
     * Objeto Huella que se está visualizando/editando.
     */
    private Huella huellaActual;
    /**
     * Servicio para operaciones de persistencia.
     */
    private final HuellaService huellaService = new HuellaService();

    /**
     * Carga los datos iniciales en la vista.
     * <p>
     * Se llama desde el controlador padre al abrir la ventana modal.
     *
     * @param h La huella a mostrar.
     */
    public void setDatos(Huella h) {
        this.huellaActual = h;
        if (h == null) return;

        // 1. Cabecera estática
        lblTitulo.setText(h.getIdActividad().getNombre());
        lblCategoria.setText(h.getIdActividad().getIdCategoria().getNombre().toUpperCase());

        String codigo = getIconoPorCategoria(h.getIdActividad().getIdCategoria().getNombre());
        FontIcon icon = new FontIcon(codigo);
        icon.setIconSize(60);
        icon.getStyleClass().add("texto-primario");
        lblIcono.setGraphic(icon);
        lblIcono.setText("");

        // 2. Rellenar Modo Lectura
        actualizarVistaLectura();

        // 3. Preparar Modo Edición
        if (h.getFecha() != null) {
            dpFechaInput.setValue(h.getFecha().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        txtValorInput.setText(String.valueOf(h.getValor()));
        lblUnidadInput.setText(h.getUnidad());
    }

    /**
     * Actualiza los Labels y Colores según los datos actuales del objeto Huella.
     * <p>
     * Recalcula el impacto y ajusta el color del texto según la gravedad.
     */
    private void actualizarVistaLectura() {
        // Valor y Unidad
        lblValor.setText(huellaActual.getValor() + " " + huellaActual.getUnidad());

        // Fecha
        if (huellaActual.getFecha() != null) {
            lblFecha.setText(huellaActual.getFecha().atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        }

        // Recalcular Impacto
        double factor = huellaActual.getIdActividad().getIdCategoria().getFactorEmision(); // Removed .doubleValue()
        double impacto = huellaActual.getValor() * factor; // Removed .doubleValue()

        lblImpacto.setText(String.format("%.2f kg CO₂", impacto));

        // Color Dinámico
        String color = (impacto < 5) ? "#656D4A" : (impacto < 20) ? "#DDA15E" : "#bc4749";
        lblImpacto.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 16px;");
    }

    /**
     * Cambia la interfaz al modo de edición.
     * <p>
     * Oculta las etiquetas de lectura y muestra los campos de entrada.
     */
    @FXML
    public void activarEdicion() {
        // Ocultar Lectura
        lblFecha.setVisible(false);
        lblFecha.setManaged(false);
        lblValor.setVisible(false);
        lblValor.setManaged(false);
        btnEditar.setVisible(false);
        btnEditar.setManaged(false);

        // Mostrar Edición
        dpFechaInput.setVisible(true);
        dpFechaInput.setManaged(true);
        boxEdicionValor.setVisible(true);
        boxEdicionValor.setManaged(true);
        btnGuardar.setVisible(true);
        btnGuardar.setManaged(true);

        btnCerrar.setText("Cancelar");
    }

    /**
     * Guarda los cambios realizados en el modo edición.
     * <p>
     * Valida la entrada, actualiza el objeto y lo persiste en la base de datos.
     */
    @FXML
    public void guardarCambios() {
        try {
            // 1. Validar y obtener datos
            double nuevoValor = Double.parseDouble(txtValorInput.getText().replace(",", ".")); // Changed to double and Double.parseDouble

            // 2. Actualizar objeto local
            huellaActual.setValor(nuevoValor);
            if (dpFechaInput.getValue() != null) {
                huellaActual.setFecha(dpFechaInput.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            }

            // 3. Guardar en BBDD
            if (huellaService.updateHuella(huellaActual)) {

                // 4. Volver a Modo Lectura
                activarModoLectura();
                actualizarVistaLectura();

                AlertUtils.info("Registro actualizado correctamente.");

            } else {
                AlertUtils.error("Error al guardar en la base de datos.");
            }

        } catch (NumberFormatException e) {
            AlertUtils.error("El valor debe ser un número válido.");
        } catch (Exception e) {
            AlertUtils.error("Error desconocido: " + e.getMessage());
        }
    }

    /**
     * Restaura la interfaz al modo de solo lectura.
     */
    private void activarModoLectura() {
        // Mostrar Lectura
        lblFecha.setVisible(true);
        lblFecha.setManaged(true);
        lblValor.setVisible(true);
        lblValor.setManaged(true);
        btnEditar.setVisible(true);
        btnEditar.setManaged(true);

        // Ocultar Edición
        dpFechaInput.setVisible(false);
        dpFechaInput.setManaged(false);
        boxEdicionValor.setVisible(false);
        boxEdicionValor.setManaged(false);
        btnGuardar.setVisible(false);
        btnGuardar.setManaged(false);

        btnCerrar.setText("Cerrar");
    }

    /**
     * Cierra la ventana modal actual.
     */
    @FXML
    public void cerrarVentana() {
        Stage stage = (Stage) lblTitulo.getScene().getWindow();
        stage.close();
    }

    /**
     * Obtiene un emoji representativo según la categoría.
     *
     * @param categoria Nombre de la categoría.
     * @return Emoji como String.
     */
    private String getIconoPorCategoria(String categoria) {
        if (categoria == null) return "fas-leaf"; // Hoja por defecto

        return switch (categoria) {
            case "Transporte" -> "fas-car";          // Coche
            case "Alimentación" -> "fas-apple-alt";  // Manzana
            case "Energía" -> "fas-bolt";            // Rayo
            case "Agua" -> "fas-tint";               // Gota
            case "Residuos" -> "fas-recycle";        // Reciclaje
            default -> "fas-leaf";                   // Hoja
        };
    }
}