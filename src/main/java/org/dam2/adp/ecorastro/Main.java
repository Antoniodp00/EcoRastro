package org.dam2.adp.ecorastro;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.dam2.adp.ecorastro.util.Navigation;

import java.io.IOException;
import java.util.Objects;

/**
 * Clase principal de la aplicación EcoRastro.
 * <p>
 * Inicia la aplicación JavaFX, configura la navegación y carga la pantalla de inicio de sesión.
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class Main extends Application {

    /**
     * Método de inicio de la aplicación JavaFX.
     * <p>
     * Configura el escenario principal (Stage), inicializa el sistema de navegación
     * y carga la vista de login.
     *
     * @param stage El escenario principal proporcionado por JavaFX.
     * @throws IOException Si ocurre un error al cargar el archivo FXML inicial.
     */
    @Override
    public void start(Stage stage) throws IOException {
        Navigation.setStage(stage);

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("view/login.fxml"));
        Scene scene = new Scene(loader.load(),400,500);

      stage.setTitle("EcoRastro");
      stage.setScene(scene);
      stage.show();

    }
}