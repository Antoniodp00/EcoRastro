package org.dam2.adp.ecorastro.controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.dam2.adp.ecorastro.model.Usuario;
import org.dam2.adp.ecorastro.service.UsuarioService;
import org.dam2.adp.ecorastro.util.AlertUtils;
import org.dam2.adp.ecorastro.util.Navigation;
import org.dam2.adp.ecorastro.util.SessionManager;

/**
 * Controlador para la gestión del perfil de usuario.
 * <p>
 * Permite visualizar y editar la información personal del usuario logueado.
 * <p>
 * Funcionalidades principales:
 * <ul>
 * <li>Visualización de nombre y email actuales.</li>
 * <li>Edición de nombre y correo electrónico.</li>
 * <li>Cambio de contraseña con confirmación.</li>
 * <li>Validación de campos obligatorios y coincidencia de claves.</li>
 * </ul>
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class PerfilController {

    // --- ELEMENTOS FXML ---
    /** Campo de texto para editar el nombre. */
    @FXML private TextField txtNombre;
    /** Campo de texto para editar el email. */
    @FXML private TextField txtEmail;
    /** Campo para la nueva contraseña (opcional). */
    @FXML private PasswordField txtPassNueva;
    /** Campo para confirmar la nueva contraseña. */
    @FXML private PasswordField txtPassConfirm;

    // --- SERVICIOS Y DATOS ---
    /** Servicio para operaciones con usuarios. */
    private final UsuarioService usuarioService = new UsuarioService();
    /** Referencia al usuario actualmente logueado. */
    private Usuario usuarioActual;

    /**
     * Inicializa el controlador del perfil.
     * <p>
     * Carga los datos del usuario actual en los campos del formulario.
     */
    @FXML
    public void initialize() {
        // 1. Cargar datos del usuario logueado
        usuarioActual = SessionManager.getInstance().getUsuarioActual();
        if (usuarioActual != null) {
            txtNombre.setText(usuarioActual.getNombre());
            txtEmail.setText(usuarioActual.getEmail());
        }
    }

    /**
     * Guarda los cambios realizados en el perfil.
     * <p>
     * Valida los datos, actualiza el objeto usuario y persiste los cambios.
     * Si se proporciona una nueva contraseña, también se actualiza.
     */
    @FXML
    public void guardarCambios() {
        String nuevoNombre = txtNombre.getText();
        String nuevoEmail = txtEmail.getText();
        String pass1 = txtPassNueva.getText();
        String pass2 = txtPassConfirm.getText();

        // VALIDACIONES
        if (nuevoNombre.isEmpty() || nuevoEmail.isEmpty()) {
            AlertUtils.error("El nombre y el email no pueden estar vacíos.");
            return;
        }

        if (!pass1.isEmpty() && !pass1.equals(pass2)) {
            AlertUtils.error("Las contraseñas no coinciden.");
            return;
        }

        // ACTUALIZAR OBJETO EN MEMORIA
        usuarioActual.setNombre(nuevoNombre);
        usuarioActual.setEmail(nuevoEmail);

        // LLAMAR AL SERVICIO
        // Pasamos la nueva contraseña (o cadena vacía si no la cambia)
        if (usuarioService.actualizarUsuario(usuarioActual, pass1)) {
            AlertUtils.info("Perfil actualizado correctamente.");

            // Actualizar la sesión global por si acaso
            SessionManager.getInstance().setUsuarioActual(usuarioActual);


            Navigation.navigate("inicio.fxml");
        } else {
            AlertUtils.error("Error al actualizar. Puede que el email ya exista.");
        }
    }

    /**
     * Cancela la edición y vuelve a la pantalla de inicio.
     */
    @FXML
    public void cancelar() {
        Navigation.navigate("inicio.fxml");
    }

    /**
     * Navega de vuelta a la pantalla de inicio.
     */
    @FXML
    public void volverInicio() {
        Navigation.navigate("inicio.fxml");
    }
}