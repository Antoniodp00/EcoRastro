package org.dam2.adp.ecorastro.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidades para mostrar alertas en la interfaz de usuario.
 */
public class AlertUtils {

    private static final Logger logger = Logger.getLogger(AlertUtils.class.getName());

    /**
     * Aplica la hoja de estilos global a la escena de la alerta.
     * @param alert La alerta a la que aplicar el estilo.
     */
    private static void aplicarEstilo(Alert alert) {
        Scene scene = alert.getDialogPane().getScene();
        if (scene != null) {

            String rutaCss = "/org/dam2/adp/ecorastro/style.css";

            var urlResource = AlertUtils.class.getResource(rutaCss);


            if (urlResource != null) {
                String cssPath = urlResource.toExternalForm();
                if (!scene.getStylesheets().contains(cssPath)) {
                    scene.getStylesheets().add(cssPath);
                }
            } else {
                System.err.println("⚠️ ADVERTENCIA: No se pudo cargar el CSS para la alerta en: " + rutaCss);
            }
        }
    }

    /**
     * Muestra una alerta de error.
     * Si el mensaje es largo, usa un área de texto desplazable.
     *
     * @param mensaje El mensaje de error a mostrar.
     */
    public static void error(String mensaje) {
        mostrarAlertaPersonalizada(Alert.AlertType.ERROR, "Error del Sistema", "Ha ocurrido un error", mensaje);
    }

    /**
     * Muestra una alerta de información simple.
     *
     * @param mensaje El mensaje informativo.
     */
    public static void info(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.setResizable(true);

        aplicarEstilo(alert);

        alert.showAndWait();
    }

    /**
     * Método interno para construir y mostrar una alerta personalizada y robusta.
     * @param tipo El tipo de alerta (ERROR, INFORMATION, etc.).
     * @param titulo El título de la ventana de la alerta.
     * @param cabecera El texto de la cabecera de la alerta.
     * @param contenido El mensaje principal, que se mostrará en un TextArea.
     */
    private static void mostrarAlertaPersonalizada(Alert.AlertType tipo, String titulo, String cabecera, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecera);

        alert.setResizable(true);

        TextArea textArea = new TextArea(contenido);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(new Label("Detalle del mensaje:"), 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setContent(expContent);

        aplicarEstilo(alert);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        alert.showAndWait();
    }

    /**
     * Muestra una alerta de confirmación y espera la respuesta del usuario.
     * @param title El título de la ventana de diálogo.
     * @param header El texto de la cabecera.
     * @param content El mensaje principal del diálogo.
     * @return true si el usuario presiona OK, false en caso contrario.
     */
    public static boolean confirmacion(String title, String header, String content) {
        logger.log(Level.INFO, "Mostrando alerta de confirmación: " + header);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        aplicarEstilo(alert);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
