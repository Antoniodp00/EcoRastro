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

public class LoginController {
    public TextField txtEmail;
    public PasswordField txtPassword;
    public Label lblError;
    public Button btnLogin;
    public Button btnRegister;

    UsuarioService usuarioService = new UsuarioService();

    public void initialize() {
        lblError.setVisible(false);
        lblError.setTextFill(javafx.scene.paint.Color.RED);
        lblError.setWrapText(true);

        btnLogin.setOnAction(this::onLoginClick);
        btnRegister.setOnAction(this::onRegisterClick);
    }

    public void onLoginClick(ActionEvent actionEvent) {
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if (usuarioService.login(email, password) != null) {
            realizarLoginExitoso(usuarioService.login(email, password));
        }

    }

    public void onRegisterClick(ActionEvent actionEvent) {
        Navigation.switchScene("register.fxml");

    }

    private void realizarLoginExitoso(Usuario usuario) {
        SessionManager.getInstance().setUsuarioActual(usuario);
        Navigation.switchScene("main.fxml");
    }

}
