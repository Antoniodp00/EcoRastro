package org.dam2.adp.ecorastro.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.dam2.adp.ecorastro.model.Usuario;
import org.dam2.adp.ecorastro.service.UsuarioService;
import org.dam2.adp.ecorastro.util.AlertUtils;
import org.dam2.adp.ecorastro.util.Navigation;

/**
 * Controlador para la pantalla de registro de nuevos usuarios.
 * <p>
 * Gestiona la validación de datos y la creación de cuentas.
 * <p>
 * Funcionalidades principales:
 * <ul>
 * <li>Validación de campos obligatorios.</li>
 * <li>Verificación de coincidencia de contraseñas.</li>
 * <li>Registro de nuevos usuarios en la base de datos.</li>
 * <li>Navegación de vuelta al login.</li>
 * </ul>
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class RegisterController {


    /** Campo de texto para el nombre del usuario. */
    public TextField txtNombre;

    /** Campo de texto para el correo electrónico. */
    public TextField txtEmail;

    /** Campo de contraseña. */
    public PasswordField txtPassword;

    /** Campo para confirmar la contraseña. */
    public PasswordField txtConfirmPassword;

    /** Etiqueta para mostrar mensajes de error o validación. */
    public Label lblMensaje;


    /** Servicio para la gestión de usuarios. */
    UsuarioService usuarioService = new UsuarioService();

    /**
     * Inicializa el controlador de registro.
     * <p>
     * Oculta el mensaje de error inicialmente.
     */
    public void initialize() {
        lblMensaje.setVisible(false);
    }

    /**
     * Maneja el evento de clic en el botón Registrar.
     * <p>
     * Valida que los campos no estén vacíos y que las contraseñas coincidan.
     * Si todo es correcto, crea el usuario y muestra un mensaje de éxito.
     *
     * @param actionEvent Evento de acción.
     */
    public void onRegistrarClick(ActionEvent actionEvent) {
        if (txtNombre.getText().isEmpty() || txtEmail.getText().isEmpty() || txtPassword.getText().isEmpty()) {
            lblMensaje.setVisible(true);
            lblMensaje.setTextFill(javafx.scene.paint.Color.RED);
            lblMensaje.setText("Por favor, completa todos los campos");

        } else {
            lblMensaje.setVisible(false);
            String nombre = txtNombre.getText();
            String email = txtEmail.getText();
            String password = txtPassword.getText();
            if (comprobarPassword()) {
                Usuario usuario = new Usuario(nombre, email, password);
                if (usuarioService.registrarUsuario(usuario)) {
                    AlertUtils.info("Registro Exitoso!");
                    // Opcional: Navegar al login tras registro exitoso
                    // Navigation.switchScene("login.fxml");
                } else {
                    AlertUtils.error("Error en el registro.");
                }
            }else {
                lblMensaje.setVisible(true);
                lblMensaje.setTextFill(javafx.scene.paint.Color.RED);
                lblMensaje.setText("Las contraseñas no coinciden");
            }

        }
    }

    /**
     * Navega de vuelta a la pantalla de Login.
     *
     * @param actionEvent Evento de acción.
     */
    public void onVolverClick(ActionEvent actionEvent) {
        Navigation.switchScene("login.fxml");
    }

    /**
     * Verifica si la contraseña y su confirmación coinciden.
     *
     * @return true si coinciden, false en caso contrario.
     */
    public boolean comprobarPassword() {
        return txtPassword.getText().equals(txtConfirmPassword.getText());
    }
}