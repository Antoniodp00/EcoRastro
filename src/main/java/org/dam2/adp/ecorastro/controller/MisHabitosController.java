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
import org.dam2.adp.ecorastro.util.AlertUtils;
import org.dam2.adp.ecorastro.util.SessionManager;

import java.util.List;

public class MisHabitosController {

    @FXML private FlowPane contenedorHabitos;

    // --- CAMPOS DEL FORMULARIO LATERAL ---
    @FXML private ComboBox<Actividad> cmbActividad;
    @FXML private TextField txtFrecuencia; // Aquí el usuario escribirá "Diario", "Semanal", etc.

    // --- SERVICIOS ---
    private final HabitoService habitoService = new HabitoService();
    private final HuellaService huellaService = new HuellaService(); // Necesario para llenar el combo

    @FXML
    public void initialize() {
        cargarComboActividades();
        cargarHabitos();
    }

    /**
     * Carga las actividades disponibles en el ComboBox del sidebar.
     */
    private void cargarComboActividades() {
        // Asegúrate de que ActividadService tenga un método para obtener todas
        List<Actividad> actividades = huellaService.getAllActividades();
        cmbActividad.getItems().addAll(actividades);

        // Convertidor para mostrar solo el nombre en el ComboBox
        cmbActividad.setConverter(new StringConverter<Actividad>() {
            @Override
            public String toString(Actividad a) {
                return (a != null) ? a.getNombre() : "";
            }
            @Override
            public Actividad fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Acción del botón "Añadir Rutina".
     * ADAPTADO A TU SERVICIO: addHabito(Usuario, Actividad, int, String)
     */
    @FXML
    public void guardarHabitoRapido() {
        // 1. Obtener datos de la vista
        Actividad actividad = cmbActividad.getValue();
        String tipoTexto = txtFrecuencia.getText(); // Ej: "Diario"

        // 2. Validaciones básicas
        if (actividad == null || tipoTexto == null || tipoTexto.trim().isEmpty()) {
            AlertUtils.error("Por favor, selecciona una actividad y escribe la frecuencia (ej: Diario).");
            return;
        }

        // 3. Obtener Usuario actual
        Usuario usuarioActual = SessionManager.getInstance().getUsuarioActual();

        // 4. Llamar al Servicio
        // NOTA: Como el formulario rápido solo tiene un campo de texto, asumimos
        // frecuencia numérica = 1 y el texto es el "tipo" (periodicidad).
        boolean exito = habitoService.addHabito(usuarioActual, actividad, 1, tipoTexto);

        if (exito) {
            // 5. Limpiar formulario
            cmbActividad.getSelectionModel().clearSelection();
            txtFrecuencia.clear();

            // 6. Recargar lista
            cargarHabitos();

            AlertUtils.info("¡Hábito añadido correctamente!");
        } else {
            AlertUtils.error("No se pudo guardar el hábito. Verifica los datos.");
        }
    }

    /**
     * Carga y muestra las tarjetas de hábitos.
     * ADAPTADO A TU SERVICIO: getHabitosByUsuario
     */
    @FXML
    public void cargarHabitos() {
        if (contenedorHabitos != null) {
            contenedorHabitos.getChildren().clear();
        }

        int idUsuario = SessionManager.getInstance().getUsuarioActual().getId();

        // CORRECCIÓN: Usamos el nombre exacto de tu servicio 'getHabitosByUsuario'
        List<Habito> lista = habitoService.getHabitosByUsuario(idUsuario);

        if (lista.isEmpty()) {
            mostrarMensajeVacio();
        } else {
            for (Habito h : lista) {
                contenedorHabitos.getChildren().add(crearTarjetaHabito(h));
            }
        }
    }

    /**
     * Crea la tarjeta visual para un hábito.
     */
    private VBox crearTarjetaHabito(Habito h) {
        VBox card = new VBox(5);
        card.getStyleClass().add("item-card");
        card.setAlignment(Pos.CENTER);

        // A. Icono
        String catNombre = h.getIdActividad().getIdCategoria().getNombre();
        Label icon = new Label(getIconoPorCategoria(catNombre));
        icon.getStyleClass().add("item-card-icono");

        // B. Título (Actividad)
        Label titulo = new Label(h.getIdActividad().getNombre());
        titulo.getStyleClass().add("item-card-titulo");
        titulo.setWrapText(true);
        titulo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // C. Frecuencia y Tipo (Ej: "1 vez(ces) Diario")
        // Combinamos el int y el String que guardaste
        String infoFrecuencia = h.getFrecuencia() + " x " + h.getTipo();
        Label lblFreq = new Label(infoFrecuencia);
        lblFreq.getStyleClass().add("item-card-valor");
        lblFreq.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-primario; -fx-font-weight: bold;");

        // D. Estado (Decorativo)
        Label lblEstado = new Label("En curso ⏳");
        lblEstado.setStyle("-fx-font-size: 11px; -fx-text-fill: #656D4A;");

        card.getChildren().addAll(icon, titulo, lblFreq, lblEstado);

        // Menú Contextual (Click derecho para borrar)
        ContextMenu menu = new ContextMenu();
        MenuItem itemBorrar = new MenuItem("🗑 Dejar este hábito");
        itemBorrar.setOnAction(e -> eliminarHabito(h));
        menu.getItems().add(itemBorrar);

        card.setOnContextMenuRequested(e -> menu.show(card, e.getScreenX(), e.getScreenY()));

        return card;
    }

    /**
     * Elimina un hábito usando tu servicio.
     */
    private void eliminarHabito(Habito h) {
        if (AlertUtils.confirmacion("Eliminar Hábito", "Confirmar acción",
                "¿Deseas dejar de seguir el hábito '" + h.getIdActividad().getNombre() + "'?")) {

            // CORRECCIÓN: Usamos deleteHabito(Habito) como está en tu servicio
            if (habitoService.deleteHabito(h)) {
                AlertUtils.info("Hábito eliminado.");
                cargarHabitos();
            } else {
                AlertUtils.error("No se pudo eliminar.");
            }
        }
    }

    private void mostrarMensajeVacio() {
        VBox vacio = new VBox(10);
        vacio.setAlignment(Pos.CENTER);
        vacio.setPrefWidth(400);

        Label icono = new Label("🌱");
        icono.setStyle("-fx-font-size: 40px;");
        Label msg = new Label("No tienes hábitos registrados.");
        msg.setStyle("-fx-text-fill: -color-texto-secundario; -fx-font-size: 16px;");

        vacio.getChildren().addAll(icono, msg);
        contenedorHabitos.getChildren().add(vacio);
    }

    private String getIconoPorCategoria(String categoria) {
        if (categoria == null) return "✨";
        switch (categoria) {
            case "Transporte": return "🚲";
            case "Alimentación": return "🥦";
            case "Energía": return "🔌";
            case "Agua": return "🚿";
            case "Residuos": return "♻️";
            default: return "✨";
        }
    }
}