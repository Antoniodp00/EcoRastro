package org.dam2.adp.ecorastro.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.dam2.adp.ecorastro.model.Huella;
import org.dam2.adp.ecorastro.service.HuellaService;
import org.dam2.adp.ecorastro.util.AlertUtils;
import org.dam2.adp.ecorastro.util.SessionManager;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador encargado de gestionar la pantalla de "Historial de Huellas".
 * <p>
 * Esta clase permite al usuario:
 * <ul>
 * <li>Visualizar una tabla con todos los registros de huella de carbono creados.</li>
 * <li>Ver el cálculo automático del impacto (Valor * Factor) para cada registro individual.</li>
 * <li>Abrir el formulario para registrar nuevas huellas.</li>
 * <li>Eliminar registros existentes de la base de datos.</li>
 * </ul>
 *
 * @author TuNombre
 * @version 1.0
 */
public class HistorialHuellasController {

    // --- ELEMENTOS FXML ---

    /** Tabla principal que muestra la lista de huellas. */
    @FXML private TableView<Huella> tablaHuellas;

    /** Columna para la fecha del registro. */
    @FXML private TableColumn<Huella, String> colFecha;

    /** Columna para el nombre de la actividad realizada. */
    @FXML private TableColumn<Huella, String> colActividad;

    /** Columna para la cantidad numérica consumida (ej: 100). */
    @FXML private TableColumn<Huella, String> colValor;

    /** Columna para la unidad de medida (ej: km, kWh). */
    @FXML private TableColumn<Huella, String> colUnidad;

    /**
     * Columna calculada para mostrar el impacto final en CO2.
     * <p>
     * Este valor no se almacena directamente en la tabla simple de la BBDD,
     * sino que se calcula dinámicamente multiplicando el valor por el factor de emisión.
     */
    @FXML private TableColumn<Huella, String> colImpacto;


    // --- SERVICIOS Y DATOS ---

    /** Servicio para realizar operaciones CRUD sobre las huellas. */
    private final HuellaService huellaService = new HuellaService();

    /** Lista observable que mantiene los datos sincronizados con la tabla visual. */
    private final ObservableList<Huella> listaHuellas = FXCollections.observableArrayList();

    /**
     * Inicializa el controlador.
     * <p>
     * Se llama automáticamente al cargar la vista. Configura el renderizado de las columnas
     * y carga los datos iniciales desde la base de datos.
     */
    public void initialize() {
        configurarColumnas();
        cargarDatos();
    }

    /**
     * Configura cómo se deben mostrar los datos en cada columna de la tabla.
     * <p>
     * Define formateadores para:
     * <ul>
     * <li><b>Fecha:</b> Formato dd/MM/yyyy.</li>
     * <li><b>Valor:</b> Dos decimales.</li>
     * <li><b>Impacto:</b> Aplica la fórmula {@code Valor * Factor} obteniendo el factor
     * desde la Categoría asociada a la Actividad.</li>
     * </ul>
     */
    private void configurarColumnas() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.systemDefault());

        // 1. FECHA
        colFecha.setCellValueFactory(cell -> {
            if (cell.getValue().getFecha() != null) {
                return new SimpleStringProperty(formatter.format(cell.getValue().getFecha()));
            }
            return new SimpleStringProperty("");
        });

        // 2. ACTIVIDAD
        colActividad.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getIdActividad().getNombre())
        );

        // 3. VALOR
        colValor.setCellValueFactory(cell -> {
            BigDecimal val = cell.getValue().getValor();
            return new SimpleStringProperty(val != null ? String.format("%.2f", val) : "0.00");
        });

        // 4. UNIDAD
        colUnidad.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getUnidad())
        );

        // 5. IMPACTO (Lógica de Negocio en la Vista)
        colImpacto.setCellValueFactory(cell -> {
            try {
                Huella h = cell.getValue();
                double valor = h.getValor().doubleValue();
                // Navegamos por el grafo de objetos: Huella -> Actividad -> Categoria -> Factor
                double factor = h.getIdActividad().getIdCategoria().getFactorEmision().doubleValue();

                // Aplicamos la fórmula requerida
                double impacto = valor * factor;

                return new SimpleStringProperty(String.format("%.2f kg CO₂", impacto));
            } catch (Exception e) {
                return new SimpleStringProperty("Err");
            }
        });

        // ESTILOS VISUALES
        colFecha.setStyle("-fx-alignment: CENTER-LEFT;");
        colActividad.setStyle("-fx-alignment: CENTER-LEFT;");
        colValor.setStyle("-fx-alignment: CENTER-RIGHT;");
        colUnidad.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: -color-texto-secundario;");

        // Destacamos el impacto en negrita y color primario
        colImpacto.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: -color-primario; -fx-font-weight: bold;");
    }

    /**
     * Recarga los datos de la tabla desde la base de datos.
     * <p>
     * 1. Limpia la lista actual.<br>
     * 2. Obtiene las huellas del usuario logueado mediante {@link HuellaService}.<br>
     * 3. Actualiza la tabla visual.
     */
    @FXML
    public void cargarDatos() {
        listaHuellas.clear();
        // Es vital que el DAO traiga las relaciones con 'JOIN FETCH' para que el cálculo del factor no falle
        List<Huella> datos = huellaService.getHuellasPorUsuario(
                SessionManager.getInstance().getUsuarioActual().getId()
        );
        listaHuellas.addAll(datos);
        tablaHuellas.setItems(listaHuellas);
        tablaHuellas.refresh();
    }

    /**
     * Abre una ventana modal para registrar una nueva huella.
     * <p>
     * Carga la vista {@code register_huella.fxml} y espera a que se cierre.
     * Al cerrarse la ventana modal, recarga automáticamente la tabla para mostrar el nuevo registro.
     */
    @FXML
    public void irARegistrar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam2/adp/ecorastro/view/register_huella.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Registrar Nueva Huella");

            Scene scene = new Scene(root);

            // --- AQUÍ AÑADES EL ESTILO ---
            // Esto asegura que el modal herede todos tus colores y diseños del style.css
            scene.getStylesheets().add(getClass().getResource("/org/dam2/adp/ecorastro/style.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            cargarDatos();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Elimina el registro de huella seleccionado en la tabla.
     * <p>
     * Flujo:
     * <ol>
     * <li>Verifica que haya una fila seleccionada.</li>
     * <li>Muestra una alerta de confirmación al usuario.</li>
     * <li>Si confirma, llama al servicio para borrar en BBDD.</li>
     * <li>Si el servicio retorna éxito, elimina el objeto de la lista visual.</li>
     * </ol>
     */
    @FXML
    public void eliminarHuella() {
        Huella seleccionada = tablaHuellas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            AlertUtils.error("Por favor, selecciona una fila para borrar.");
            return;
        }

        if (AlertUtils.confirmacion("Eliminar", "Confirmar", "¿Borrar este registro?")) {
            if (huellaService.deleteHuella(seleccionada)) {
                listaHuellas.remove(seleccionada);
                AlertUtils.info("Eliminado correctamente.");
            } else {
                AlertUtils.error("No se pudo eliminar.");
            }
        }
    }
}