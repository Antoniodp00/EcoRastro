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
    @FXML private TableColumn<Huella, String> colValor;
    @FXML private TableColumn<Huella, String> colUnidad;

    // NUEVA COLUMNA: Para cumplir el requisito "Cálculo de una sola huella"
    @FXML private TableColumn<Huella, String> colImpacto;

    private final HuellaService huellaService = new HuellaService();
    private final ObservableList<Huella> listaHuellas = FXCollections.observableArrayList();

    public void initialize() {
        configurarColumnas();
        cargarDatos();
    }

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

        // 5. IMPACTO (La Fórmula del PDF: Valor * Factor)
        colImpacto.setCellValueFactory(cell -> {
            try {
                Huella h = cell.getValue();
                double valor = h.getValor().doubleValue();
                // Obtenemos el factor de la categoría asociada a la actividad
                double factor = h.getIdActividad().getIdCategoria().getFactorEmision().doubleValue();

                // Aplicamos la fórmula
                double impacto = valor * factor;

                return new SimpleStringProperty(String.format("%.2f kg CO₂", impacto));
            } catch (Exception e) {
                return new SimpleStringProperty("Err");
            }
        });

        // ESTILOS
        colFecha.setStyle("-fx-alignment: CENTER-LEFT;");
        colActividad.setStyle("-fx-alignment: CENTER-LEFT;");
        colValor.setStyle("-fx-alignment: CENTER-RIGHT;");
        colUnidad.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: -color-texto-secundario;");

        // Destacamos el impacto en negrita y color primario
        colImpacto.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: -color-primario; -fx-font-weight: bold;");
    }

    @FXML
    public void cargarDatos() {
        listaHuellas.clear();
        // Asegúrate de que tu DAO trae las relaciones (Actividad y Categoría) para poder calcular el factor
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