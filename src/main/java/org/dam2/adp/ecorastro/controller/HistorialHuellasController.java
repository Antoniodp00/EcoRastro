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

public class HistorialHuellasController {

    @FXML private TableView<Huella> tablaHuellas;

    @FXML private TableColumn<Huella, String> colFecha;
    @FXML private TableColumn<Huella, String> colActividad;
    
    // Cambiado a String para simplificar el formateo y visualización
    @FXML private TableColumn<Huella, String> colValor;
    
    @FXML private TableColumn<Huella, String> colUnidad;

    private final HuellaService huellaService = new HuellaService();
    private ObservableList<Huella> listaHuellas = FXCollections.observableArrayList();

    public void initialize() {
        configurarColumnas();
        cargarDatos();
    }

    private void configurarColumnas() {
        // 1. FECHA
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.systemDefault());
        colFecha.setCellValueFactory(cell -> {
            if (cell.getValue().getFecha() != null) {
                return new SimpleStringProperty(formatter.format(cell.getValue().getFecha()));
            }
            return new SimpleStringProperty("");
        });

        // 2. ACTIVIDAD
        colActividad.setCellValueFactory(cell -> {
            if (cell.getValue().getIdActividad() != null) {
                return new SimpleStringProperty(cell.getValue().getIdActividad().getNombre());
            }
            return new SimpleStringProperty("Desconocida");
        });

        // 3. VALOR (CANTIDAD)
        // Convertimos directamente a String formateado aquí. Esto es más robusto que usar CellFactory.
        colValor.setCellValueFactory(cell -> {
            BigDecimal val = cell.getValue().getValor();
            if (val != null) {
                return new SimpleStringProperty(String.format("%.2f", val));
            }
            return new SimpleStringProperty("0.00");
        });

        // 4. UNIDAD
        colUnidad.setCellValueFactory(cell -> {
            String u = cell.getValue().getUnidad();
            return new SimpleStringProperty(u != null ? u : "");
        });




        colFecha.setStyle("-fx-alignment: CENTER-LEFT;");
        colActividad.setStyle("-fx-alignment: CENTER-LEFT;");
        colValor.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: -color-primario; -fx-font-weight: bold;");
        colUnidad.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: -color-texto-secundario;");

    }

    @FXML
    public void cargarDatos() {
        listaHuellas.clear();
        List<Huella> datos = huellaService.getHuellasPorUsuario(
                SessionManager.getInstance().getUsuarioActual().getId()
        );
        listaHuellas.addAll(datos);
        tablaHuellas.setItems(listaHuellas);
        tablaHuellas.refresh();
    }

    @FXML
    public void irARegistrar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam2/adp/ecorastro/view/register_huella.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Registrar Nueva Huella");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            cargarDatos(); // Recargar al volver

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void eliminarHuella() {
        Huella seleccionada = tablaHuellas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            AlertUtils.error("Por favor, selecciona una fila de la tabla para borrar.");
            return;
        }

        boolean confirm = AlertUtils.confirmacion("Eliminar Registro","¡Atención!" ,"¿Estás seguro de eliminar este registro permanentemente?");
        if (confirm) {
            if (huellaService.deleteHuella(seleccionada)) {
                listaHuellas.remove(seleccionada);
                AlertUtils.info("Registro eliminado correctamente.");
            } else {
                AlertUtils.error("Error al intentar eliminar el registro.");
            }
        }
    }
}