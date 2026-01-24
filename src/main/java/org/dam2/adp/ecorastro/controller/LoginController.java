package org.dam2.adp.ecorastro.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.dam2.adp.ecorastro.model.Usuario;
import org.dam2.adp.ecorastro.service.UsuarioService;
import org.dam2.adp.ecorastro.util.Navigation;
import org.dam2.adp.ecorastro.util.SessionManager;

/**
 * Controlador para la pantalla de inicio de sesión.
 * <p>
 * Gestiona la autenticación de usuarios y la navegación al registro.
 * <p>
 * Funcionalidades principales:
 * <ul>
 * <li>Validación de credenciales (email y contraseña).</li>
 * <li>Inicio de sesión y redirección a la pantalla principal.</li>
 * <li>Navegación a la pantalla de registro para nuevos usuarios.</li>
 * <li>Manejo de errores de autenticación.</li>
 * </ul>
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class LoginController {

    // --- ELEMENTOS FXML ---

    /** Campo de texto para introducir el correo electrónico. */
    public TextField txtEmail;

    /** Campo de contraseña para introducir la clave de acceso. */
    public PasswordField txtPassword;

    /** Etiqueta para mostrar mensajes de error en el login. */
    public Label lblError;

    /** Botón para iniciar sesión. */
    public Button btnLogin;

    /** Botón para navegar al registro. */
    public Button btnRegister;

    // --- SERVICIOS ---

    /** Servicio encargado de la gestión de usuarios y autenticación. */
    UsuarioService usuarioService = new UsuarioService();

    /**
     * Inicializa el controlador de login.
     * <p>
     * Configura el estado inicial de la vista:
     * <ol>
     * <li>Oculta la etiqueta de error.</li>
     * <li>Configura el estilo del mensaje de error.</li>
     * <li>Asigna los manejadores de eventos a los botones.</li>
     * </ol>
     */
    public void initialize() {
        lblError.setVisible(false);
        lblError.setTextFill(javafx.scene.paint.Color.RED);
        lblError.setWrapText(true);

        btnLogin.setOnAction(this::onLoginClick);
        btnRegister.setOnAction(this::onRegisterClick);
    }

    /**
     * Maneja el intento de inicio de sesión.
     * <p>
     * Valida las credenciales introducidas contra el servicio de usuarios.
     * Si las credenciales son correctas, inicia la sesión y redirige al usuario.
     *
     * @param actionEvent Evento de acción.
     */
    public void onLoginClick(ActionEvent actionEvent) {
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        Usuario usuario = usuarioService.login(email, password);
        if (usuario != null) {
            realizarLoginExitoso(usuario);
        } else {
            // Mostrar error si el login falla
            lblError.setText("Credenciales incorrectas.");
            lblError.setVisible(true);
        }
    }

    /**
     * Navega a la pantalla de registro de nuevos usuarios.
     *
     * @param actionEvent Evento de acción.
     */
    public void onRegisterClick(ActionEvent actionEvent) {
        Navigation.switchScene("register.fxml");
    }

    /**
     * Configura la sesión del usuario y navega a la pantalla principal.
     * <p>
     * Guarda el usuario autenticado en {@link SessionManager} y cambia la escena a "main.fxml".
     *
     * @param usuario El usuario autenticado.
     */
    private void realizarLoginExitoso(Usuario usuario) {
        SessionManager.getInstance().setUsuarioActual(usuario);
        Navigation.switchScene("main.fxml");
    }
}