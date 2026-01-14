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

public class RegisterController {
    public TextField txtNombre;
    public TextField txtEmail;
    public PasswordField txtPassword;
    public PasswordField txtConfirmPassword;
    public Label lblMensaje;

    UsuarioService usuarioService = new UsuarioService();

    public void initialize() {
        lblMensaje.setVisible(false);
    }

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

    public void onVolverClick(ActionEvent actionEvent) {
        Navigation.switchScene("login.fxml");
    }

    public boolean comprobarPassword() {
        return txtPassword.getText().equals(txtConfirmPassword.getText());
    }
}
