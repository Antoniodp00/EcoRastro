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

public class RegisterHuellaController {
    public ComboBox<Actividad> cmbActividad;
    public TextField txtValor;
    public Label lblUnidad;
    public DatePicker dpFecha;

    private final HuellaService huellaService = new HuellaService();

    public void initialize() {
        dpFecha.setValue(LocalDate.now());

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
                return null;
            }
        });
    }

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

           boolean insertado = huellaService.addHuella(
                   SessionManager.getInstance().getUsuarioActual(),
                   actividad,
                   valorConsumo,
                   fecha
           );

           if (insertado){
               AlertUtils.info("Huella registrada correctamente.");
               cerrarVentana();
           }else {
               AlertUtils.error("Error al registrar huella.");}
       }catch (Exception e){
           e.printStackTrace();
           AlertUtils.error("Error al registrar huella.");
       }
    }

    public void onCancelarClick(ActionEvent actionEvent) {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtValor.getScene().getWindow();
        stage.close();
    }
}
