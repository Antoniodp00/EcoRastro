package org.dam2.adp.ecorastro.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.dam2.adp.ecorastro.model.Huella;
import org.dam2.adp.ecorastro.service.HuellaService;
import org.dam2.adp.ecorastro.util.AlertUtils;
import org.dam2.adp.ecorastro.util.SessionManager;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Controlador para la gestión visual del Historial de Huellas.
 * <p>
 * REFACTORIZADO: Ahora usa un diseño de Tarjetas (Cards) en un FlowPane
 * en lugar de una tabla tradicional, adaptándose al tema visual.
 */
public class HistorialHuellasController {
    public CheckBox chkTransporte;
    public CheckBox chkAlimentacion;
    public CheckBox chkEnergia;
    public CheckBox chkAgua;

    // --- ELEMENTOS FXML ---

    /** Contenedor fluido donde se añadirán las tarjetas dinámicamente. */
    @FXML private FlowPane contenedorHuellas;


    // --- SERVICIOS ---
    private final HuellaService huellaService = new HuellaService();


    /**
     * Inicializa el controlador. Carga las tarjetas al abrir la vista.
     */
    public void initialize() {
        cargarHuellas();
    }

    /**
     * Obtiene los datos de la BBDD y genera una tarjeta visual por cada registro.
     */
    @FXML
    public void cargarHuellas() {
        if (contenedorHuellas != null) {
            contenedorHuellas.getChildren().clear();
        }

        int idUsuario = SessionManager.getInstance().getUsuarioActual().getId();
        List<Huella> listaCompleta = huellaService.getHuellasPorUsuario(idUsuario);

        if (listaCompleta.isEmpty()) {
            mostrarMensajeVacio();
            return;
        }

        // --- FILTRADO EN MEMORIA ---
        for (Huella h : listaCompleta) {
            String cat = h.getIdActividad().getIdCategoria().getNombre();

            // Si la categoría NO está marcada, saltamos este registro
            if (!isCategoriaSeleccionada(cat)) {
                continue;
            }

            VBox tarjeta = crearTarjetaHuella(h);
            contenedorHuellas.getChildren().add(tarjeta);
        }
    }

    private boolean isCategoriaSeleccionada(String categoria) {
        switch (categoria) {
            case "Transporte":   return chkTransporte.isSelected();
            case "Alimentación": return chkAlimentacion.isSelected();
            case "Energía":      return chkEnergia.isSelected();
            case "Agua":         return chkAgua.isSelected();
        }
        return false;
    }

    /**
     * Crea un componente visual (VBox) que representa una tarjeta de huella individual.
     * Incluye estilos CSS y menú contextual para borrar.
     */
    private VBox crearTarjetaHuella(Huella h) {
        VBox card = new VBox(5);
        card.getStyleClass().add("item-card"); // Estilo definido en style.css
        card.setAlignment(Pos.CENTER);

        // A. Icono según categoría
        String cat = h.getIdActividad().getIdCategoria().getNombre();
        Label icon = new Label(getIconoPorCategoria(cat));
        icon.getStyleClass().add("item-card-icono");

        // B. Título (Actividad)
        Label titulo = new Label(h.getIdActividad().getNombre());
        titulo.getStyleClass().add("item-card-titulo");
        titulo.setWrapText(true);
        titulo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // C. Valor Principal (Cantidad)
        Label valor = new Label(h.getValor() + " " + h.getUnidad());
        valor.getStyleClass().add("item-card-valor");

        // D. Impacto Calculado (Pequeño)
        double impacto = h.getValor().doubleValue() * h.getIdActividad().getIdCategoria().getFactorEmision().doubleValue();

        String colorImpacto;
        String pesoFuente = "normal"; // Para poner negrita si es grave

        if (impacto < 5.0) {
            colorImpacto = "#656D4A"; // Verde Musgo (Bajo/Bueno)
        } else if (impacto < 20.0) {
            colorImpacto = "#DDA15E"; // Ocre Dorado (Medio/Atención)
        } else {
            colorImpacto = "#bc4749"; // Rojo Arcilla (Alto/Peligro)
            pesoFuente = "bold";      // Lo ponemos en negrita para destacar
        }

        Label lblImpacto = new Label(String.format("Impacto: %.2f kg CO₂", impacto));

        // Aplicamos el estilo dinámico
        lblImpacto.setStyle(String.format(
                "-fx-font-size: 11px; -fx-text-fill: %s; -fx-font-weight: %s;",
                colorImpacto, pesoFuente
        ));

        // E. Fecha (Corregido para Instant)
        String fechaStr = "";
        if (h.getFecha() != null) {
            // 1. Convertimos el Instant a la Zona Horaria del sistema
            // 2. Formateamos
            fechaStr = h.getFecha()
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        }
        Label fecha = new Label(fechaStr);
        fecha.getStyleClass().add("item-card-fecha");

        // Añadir elementos a la tarjeta
        card.getChildren().addAll(icon, titulo, valor, lblImpacto, fecha);

        // --- MENÚ CONTEXTUAL (Click Derecho) ---
        ContextMenu menu = new ContextMenu();
        MenuItem itemBorrar = new MenuItem("🗑 Eliminar Registro");
        itemBorrar.setOnAction(e -> eliminarHuella(h));
        menu.getItems().add(itemBorrar);

        // Asignar menú y evento de click
        card.setOnContextMenuRequested(e -> menu.show(card, e.getScreenX(), e.getScreenY()));

        card.setOnMouseClicked(e -> {
            // Evitamos que salte si hacemos click derecho (para el menú borrar)
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                abrirDetalleHuella(h);
            }
        });

        return card;
    }

    private void abrirDetalleHuella(Huella h) {
       try {
           FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam2/adp/ecorastro/view/detalle_huella.fxml"));

           Parent root = loader.load();

           DetalleHuellaController controller = loader.getController();
           controller.setDatos(h);

           Stage stage = new Stage();
           stage.initModality(Modality.APPLICATION_MODAL);
           stage.setTitle("Detalle de Huella");

           Scene scene = new Scene(root);
           scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/org/dam2/adp/ecorastro/style.css")).toExternalForm());

           stage.setScene(scene);
           stage.showAndWait();

           cargarHuellas();
       } catch (Exception e) {
           e.printStackTrace();
           AlertUtils.error("Error al abrir ventana de detalle.");
       }
    }

    /**
     * Lógica para eliminar un registro específico desde su tarjeta.
     */
    private void eliminarHuella(Huella h) {
        if (AlertUtils.confirmacion("Eliminar Registro", "Confirmar borrado",
                "¿Estás seguro de borrar '" + h.getIdActividad().getNombre() + "'?")) {

            if (huellaService.deleteHuella(h)) {
                AlertUtils.info("Registro eliminado.");
                cargarHuellas(); // Recargamos para que desaparezca la tarjeta
            } else {
                AlertUtils.error("No se pudo eliminar el registro.");
            }
        }
    }

    /**
     * Muestra un mensaje visual si no hay datos.
     */
    private void mostrarMensajeVacio() {
        VBox vacio = new VBox(10);
        vacio.setAlignment(Pos.CENTER);
        vacio.setPrefWidth(400); // Para que ocupe espacio

        Label icono = new Label("🍃");
        icono.setStyle("-fx-font-size: 40px;");

        Label msg = new Label("Aún no tienes registros.");
        msg.setStyle("-fx-text-fill: -color-texto-secundario; -fx-font-size: 16px;");

        vacio.getChildren().addAll(icono, msg);
        contenedorHuellas.getChildren().add(vacio);
    }

    /**
     * Devuelve un emoji acorde a la categoría para decorar la tarjeta.
     */
    private String getIconoPorCategoria(String categoria) {
        if (categoria == null) return "📝";
        switch (categoria) {
            case "Transporte": return "🚗";
            case "Alimentación": return "🍎";
            case "Energía": return "💡";
            case "Agua": return "💧";
            case "Residuos": return "♻️";
            default: return "🌿";
        }
    }

    // --- NAVEGACIÓN ---

    @FXML
    public void irARegistrar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam2/adp/ecorastro/view/register_huella.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nueva Huella");

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/org/dam2/adp/ecorastro/style.css").toExternalForm());

            stage.setScene(scene);
            stage.showAndWait();

            // Al volver, recargamos las tarjetas
            cargarHuellas();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.error("Error al abrir ventana de registro.");
        }
    }
}