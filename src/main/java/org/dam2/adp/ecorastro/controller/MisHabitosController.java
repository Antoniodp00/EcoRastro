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

public class MisHabitosController {
    public ComboBox<Actividad> cmbActividad;
    public TextField txtFrecuencia;
    public ComboBox<String> cmbTipo;
    public Label lblRecomendacion;

    public TableView<Habito> tablaHabitos;
    public TableColumn<Habito, String> colActividad;
    public TableColumn<Habito, String> colCategoria;
    public TableColumn<Habito, String> colFrecuencia;
    public TableColumn<Habito, String> colTipo;

    private final HabitoService habitoService = new HabitoService();
    private final RecomendacionService recomendacionService = new RecomendacionService();
    private final ActividadDAO actividadDAO = new ActividadDAO();


    private ObservableList<Habito> listaHabitos = FXCollections.observableArrayList();

    public void initialize() {
        configurarTabla();
        cargarCombos();
        cargarDatosTabla();

        tablaHabitos.getSelectionModel().selectedItemProperty().addListener(
                (observable, seleccionAnterior, seleccionNueva) -> mostrarConsejo(seleccionNueva)
        );
    }

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

    private void cargarDatosTabla() {
        listaHabitos.clear();
        listaHabitos.addAll(habitoService.getHabitosByUsuario(
                SessionManager.getInstance().getUsuarioActual().getId()
        ));
        tablaHabitos.setItems(listaHabitos);
    }

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

    private void mostrarConsejo(Habito habito){
        if (habito != null){
            String categoria = habito.getIdActividad().getIdCategoria().getNombre();

            String consejo = recomendacionService.generarConsejo(categoria);

            lblRecomendacion.setText(consejo);
        }else {
            lblRecomendacion.setText("Selecciona un habito para ver consejos.");

        }
    }

    private void limpiarFormulario(){
        cmbActividad.getSelectionModel().clearSelection();
        cmbTipo.getSelectionModel().clearSelection();
        txtFrecuencia.clear();
        lblRecomendacion.setText("Selecciona un habito para ver consejos.");
    }
}
