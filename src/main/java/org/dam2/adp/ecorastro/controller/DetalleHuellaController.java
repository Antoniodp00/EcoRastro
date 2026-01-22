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

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DetalleHuellaController {

    // --- VIEW ELEMENTS ---
    @FXML private Label lblIcono, lblTitulo, lblCategoria, lblImpacto;

    // Elementos Modo Lectura
    @FXML private Label lblFecha, lblValor;
    @FXML private Button btnEditar, btnCerrar;

    // Elementos Modo Edición
    @FXML private DatePicker dpFechaInput;
    @FXML private TextField txtValorInput;
    @FXML private Label lblUnidadInput;
    @FXML private HBox boxEdicionValor;
    @FXML private Button btnGuardar;

    // --- DATA ---
    private Huella huellaActual;
    private final HuellaService huellaService = new HuellaService();

    /**
     * Carga los datos iniciales en la vista.
     */
    public void setDatos(Huella h) {
        this.huellaActual = h;
        if (h == null) return;

        // 1. Cabecera estática
        lblTitulo.setText(h.getIdActividad().getNombre());
        lblCategoria.setText(h.getIdActividad().getIdCategoria().getNombre().toUpperCase());
        lblIcono.setText(getIconoPorCategoria(h.getIdActividad().getIdCategoria().getNombre()));

        // 2. Rellenar Modo Lectura
        actualizarVistaLectura();

        // 3. Preparar Modo Edición (Pre-cargar inputs)
        if (h.getFecha() != null) {
            dpFechaInput.setValue(h.getFecha().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        txtValorInput.setText(h.getValor().toString());
        lblUnidadInput.setText(h.getUnidad());
    }

    /**
     * Actualiza los Labels y Colores según los datos actuales del objeto Huella.
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
        double factor = huellaActual.getIdActividad().getIdCategoria().getFactorEmision().doubleValue();
        double impacto = huellaActual.getValor().doubleValue() * factor;

        lblImpacto.setText(String.format("%.2f kg CO₂", impacto));

        // Color Dinámico (Ancient Woods Theme)
        String color = (impacto < 5) ? "#656D4A" : (impacto < 20) ? "#DDA15E" : "#bc4749";
        lblImpacto.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 16px;");
    }

    @FXML
    public void activarEdicion() {
        // Ocultar Lectura
        lblFecha.setVisible(false); lblFecha.setManaged(false);
        lblValor.setVisible(false); lblValor.setManaged(false);
        btnEditar.setVisible(false); btnEditar.setManaged(false);

        // Mostrar Edición
        dpFechaInput.setVisible(true); dpFechaInput.setManaged(true);
        boxEdicionValor.setVisible(true); boxEdicionValor.setManaged(true);
        btnGuardar.setVisible(true); btnGuardar.setManaged(true);

        btnCerrar.setText("Cancelar"); // Cambiamos texto del botón cerrar
    }

    @FXML
    public void guardarCambios() {
        try {
            // 1. Validar y obtener datos
            BigDecimal nuevoValor = new BigDecimal(txtValorInput.getText().replace(",", "."));

            // 2. Actualizar objeto local
            huellaActual.setValor(nuevoValor);
            if (dpFechaInput.getValue() != null) {
                huellaActual.setFecha(dpFechaInput.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            }

            // 3. Guardar en BBDD
            if (huellaService.updateHuella(huellaActual)) { // Asumiendo que creaste este método

                // 4. Volver a Modo Lectura
                activarModoLectura();
                actualizarVistaLectura(); // Refrescar textos con lo nuevo

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

    private void activarModoLectura() {
        // Mostrar Lectura
        lblFecha.setVisible(true); lblFecha.setManaged(true);
        lblValor.setVisible(true); lblValor.setManaged(true);
        btnEditar.setVisible(true); btnEditar.setManaged(true);

        // Ocultar Edición
        dpFechaInput.setVisible(false); dpFechaInput.setManaged(false);
        boxEdicionValor.setVisible(false); boxEdicionValor.setManaged(false);
        btnGuardar.setVisible(false); btnGuardar.setManaged(false);

        btnCerrar.setText("Cerrar");
    }

    @FXML
    public void cerrarVentana() {
        Stage stage = (Stage) lblTitulo.getScene().getWindow();
        stage.close();
    }

    private String getIconoPorCategoria(String categoria) {
        if (categoria == null) return "🌿";
        return switch (categoria) {
            case "Transporte" -> "🚗";
            case "Alimentación" -> "🍎";
            case "Energía" -> "💡";
            case "Agua" -> "💧";
            case "Residuos" -> "♻️";
            default -> "🌿";
        };
    }
}