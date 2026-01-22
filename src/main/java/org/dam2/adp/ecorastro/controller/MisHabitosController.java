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
import org.dam2.adp.ecorastro.service.HuellaService;
import org.dam2.adp.ecorastro.service.RecomendacionService; // Necesario para los consejos
import org.dam2.adp.ecorastro.util.AlertUtils;
import org.dam2.adp.ecorastro.util.SessionManager;

import java.util.List;

public class MisHabitosController {

    @FXML private FlowPane contenedorHabitos;

    // Formulario
    @FXML private ComboBox<Actividad> cmbActividad;
    @FXML private TextField txtCantidad;
    @FXML private RadioButton rbDiario, rbSemanal, rbMensual;
    @FXML private ToggleGroup grupoPeriodicidad;

    // Filtros
    @FXML private CheckBox chkTransporte, chkAlimentacion, chkEnergia, chkAgua, chkOtros;

    // Info Dinámica
    @FXML private Label lblRecomendacionSidebar;

    // Servicios
    private final HabitoService habitoService = new HabitoService();
    private final HuellaService huellaService = new HuellaService();
    private final RecomendacionService recomendacionService = new RecomendacionService();

    @FXML
    public void initialize() {
        cargarComboActividades();
        cargarHabitos();
    }

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

    @FXML
    public void guardarHabitoRapido() {
        Actividad actividad = cmbActividad.getValue();
        String cantidadStr = txtCantidad.getText();

        // 1. Validar selección
        if (actividad == null || cantidadStr.isEmpty()) {
            AlertUtils.error("Selecciona actividad y cantidad.");
            return;
        }

        // 2. Parsear cantidad
        int frecuenciaNum;
        try {
            frecuenciaNum = Integer.parseInt(cantidadStr);
        } catch (NumberFormatException e) {
            AlertUtils.error("La cantidad debe ser un número entero.");
            return;
        }

        // 3. Obtener periodicidad del RadioButton
        String tipoPeriodo = "Diario";
        if (rbSemanal.isSelected()) tipoPeriodo = "Semanal";
        if (rbMensual.isSelected()) tipoPeriodo = "Mensual";

        // 4. Guardar
        Usuario usuarioActual = SessionManager.getInstance().getUsuarioActual();
        boolean exito = habitoService.addHabito(usuarioActual, actividad, frecuenciaNum, tipoPeriodo);

        if (exito) {
            cmbActividad.getSelectionModel().clearSelection();
            txtCantidad.clear();
            rbDiario.setSelected(true); // Resetear a diario
            cargarHabitos();
            AlertUtils.info("¡Hábito añadido!");
        } else {
            AlertUtils.error("Error al guardar.");
        }
    }

    @FXML
    public void cargarHabitos() {
        if (contenedorHabitos != null) contenedorHabitos.getChildren().clear();

        int idUsuario = SessionManager.getInstance().getUsuarioActual().getId();
        List<Habito> listaCompleta = habitoService.getHabitosByUsuario(idUsuario);

        if (listaCompleta.isEmpty()) {
            mostrarMensajeVacio();
            return;
        }

        for (Habito h : listaCompleta) {
            // --- FILTRADO ---
            String cat = h.getIdActividad().getIdCategoria().getNombre();
            if (!isCategoriaSeleccionada(cat)) continue;

            contenedorHabitos.getChildren().add(crearTarjetaHabito(h));
        }
    }

    private boolean isCategoriaSeleccionada(String categoria) {
        switch (categoria) {
            case "Transporte": return chkTransporte.isSelected();
            case "Alimentación": return chkAlimentacion.isSelected();
            case "Energía": return chkEnergia.isSelected();
            case "Agua": return chkAgua.isSelected();
            default: return chkOtros.isSelected();
        }
    }

    private VBox crearTarjetaHabito(Habito h) {
        VBox card = new VBox(5);
        card.getStyleClass().add("item-card");
        card.setAlignment(Pos.CENTER);

        String catNombre = h.getIdActividad().getIdCategoria().getNombre();

        // A. Icono
        Label icon = new Label(getIconoPorCategoria(catNombre));
        icon.getStyleClass().add("item-card-icono");

        // B. Título
        Label titulo = new Label(h.getIdActividad().getNombre());
        titulo.getStyleClass().add("item-card-titulo");
        titulo.setWrapText(true);
        titulo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // C. Frecuencia (Ej: "3 veces/Semanal")
        String infoFrecuencia = h.getFrecuencia() + " veces / " + h.getTipo();
        Label lblFreq = new Label(infoFrecuencia);
        lblFreq.getStyleClass().add("item-card-valor");
        lblFreq.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-primario; -fx-font-weight: bold;");

        // D. (QUITADO) Etiqueta "En curso" eliminada como pediste.

        card.getChildren().addAll(icon, titulo, lblFreq);

        // --- INTERACCIÓN: Click para ver recomendación ---
        card.setOnMouseClicked(e -> {
            // 1. Obtener consejo según la categoría del hábito seleccionado
            String consejo = recomendacionService.generarConsejo(catNombre);
            // 2. Actualizar el Label del Sidebar
            lblRecomendacionSidebar.setText("Tip para " + catNombre + ":\n" + consejo);
        });

        // Menú Contextual (Borrar)
        ContextMenu menu = new ContextMenu();
        MenuItem itemBorrar = new MenuItem("🗑 Dejar este hábito");
        itemBorrar.setOnAction(e -> eliminarHabito(h));
        menu.getItems().add(itemBorrar);
        card.setOnContextMenuRequested(e -> menu.show(card, e.getScreenX(), e.getScreenY()));

        return card;
    }

    // ... (Métodos auxiliares eliminarHabito, mostrarMensajeVacio, getIconoPorCategoria igual que antes) ...
    // Asegúrate de tenerlos copiados o mantenerlos si ya los tenías.

    private void eliminarHabito(Habito h) {
        if (AlertUtils.confirmacion("Eliminar", "Confirmar", "¿Borrar hábito?")) {
            if (habitoService.deleteHabito(h)) {
                cargarHabitos();
            } else {
                AlertUtils.error("Error al borrar.");
            }
        }
    }

    private void mostrarMensajeVacio() {
        VBox vacio = new VBox(10);
        vacio.setAlignment(Pos.CENTER);
        Label msg = new Label("No hay hábitos con este filtro.");
        msg.setStyle("-fx-text-fill: -color-texto-secundario;");
        vacio.getChildren().add(msg);
        contenedorHabitos.getChildren().add(vacio);
    }

    private String getIconoPorCategoria(String cat) {
        if (cat == null) return "✨";
        switch (cat) {
            case "Transporte": return "🚲";
            case "Alimentación": return "🥦";
            case "Energía": return "🔌";
            case "Agua": return "🚿";
            default: return "♻️";
        }
    }
}