package org.dam2.adp.ecorastro.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.dam2.adp.ecorastro.util.Navigation;
import org.dam2.adp.ecorastro.util.SessionManager;

import java.io.IOException;

/**
 * Controlador principal de la aplicación.
 * <p>
 * Gestiona la estructura base de la interfaz, incluyendo el menú lateral de navegación
 * y el área de contenido dinámico donde se cargan las distintas vistas.
 * <p>
 * Funcionalidades principales:
 * <ul>
 * <li>Navegación entre las secciones principales (Inicio, Huella, Hábitos, Análisis).</li>
 * <li>Visualización del usuario conectado.</li>
 * <li>Gestión del cierre de sesión.</li>
 * <li>Carga dinámica de vistas FXML.</li>
 * </ul>
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class MainController {

    // --- ELEMENTOS FXML ---

    /** Etiqueta para mostrar el nombre del usuario conectado. */
    public Label lblUsuario;

    /** Botón para navegar a la vista de Inicio. */
    public Button btnInicio;

    /** Botón para navegar a la vista de Historial de Huellas. */
    public Button btnHuella;

    /** Botón para navegar a la vista de Mis Hábitos. */
    public Button btnHabitos;

    /** Botón para navegar a la vista de Análisis. */
    public Button btnAnalisis;

    /** Botón para cerrar la sesión actual. */
    public Button btnCerrarSesion;

    /** Contenedor principal donde se cargan las vistas dinámicamente. */
    public StackPane contentPane;

    /** Panel principal que estructura el layout (menú lateral + contenido). */
    public BorderPane mainPane;

    /**
     * Inicializa el controlador principal.
     * <p>
     * Configura la navegación y muestra la vista de inicio por defecto.
     * <ol>
     * <li>Establece este controlador como el controlador principal en {@link Navigation}.</li>
     * <li>Muestra el nombre del usuario actual en la interfaz.</li>
     * <li>Carga la vista de Inicio automáticamente.</li>
     * </ol>
     */
    public void initialize() {
        Navigation.setMainController(this);

        if (SessionManager.getInstance().getUsuarioActual() != null){
            lblUsuario.setText(SessionManager.getInstance().getUsuarioActual().getNombre());
        }

        mostrarInicio(null);
    }

    /**
     * Muestra la vista de Inicio (Dashboard).
     *
     * @param actionEvent Evento de acción (clic en botón).
     */
    public void mostrarInicio(ActionEvent actionEvent) {
        loadView("inicio.fxml");
    }

    /**
     * Cierra la sesión del usuario actual y redirige a la pantalla de Login.
     * <p>
     * Limpia la sesión en {@link SessionManager} y navega a la vista de login.
     *
     * @param actionEvent Evento de acción.
     */
    public void cerrarSesion(ActionEvent actionEvent) {
        SessionManager.getInstance().cerrarSesion();
        Navigation.switchScene("login.fxml");
    }

    /**
     * Carga una vista FXML en el área de contenido principal.
     * <p>
     * Ajusta el tamaño de la vista para que ocupe todo el espacio disponible en el {@code contentPane}.
     *
     * @param fxml Nombre del archivo FXML a cargar (sin ruta, solo nombre).
     */
    public void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/dam2/adp/ecorastro/view/" + fxml));
            Node view = loader.load();

            // Binding para que ocupe todo el espacio
            if (view instanceof javafx.scene.layout.Region) {
                javafx.scene.layout.Region region = (javafx.scene.layout.Region) view;
                region.prefWidthProperty().bind(contentPane.widthProperty());
                region.prefHeightProperty().bind(contentPane.heightProperty());
            }

            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error cargando vista: " + fxml);
        }
    }

    /**
     * Navega a la vista de perfil del usuario.
     *
     * @param event Evento del ratón.
     */
    @FXML
    public void irAPerfil(MouseEvent event) {
        Navigation.navigate("perfil.fxml");
    }
}