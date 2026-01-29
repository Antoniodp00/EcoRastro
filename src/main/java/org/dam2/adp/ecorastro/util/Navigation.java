package org.dam2.adp.ecorastro.util;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.dam2.adp.ecorastro.controller.MainController;

import java.io.IOException;
import java.net.URL;

/**
 * Utilidades para la navegación entre escenas en la aplicación.
 * <p>
 * Gestiona el cambio de vistas principales y la carga de contenido dinámico.
 *
 * @author Antonio Delgado Portero
 * @version 1.0
 */
public class Navigation {

    private static Stage primaryStage;
    private static MainController mainController;

    private static final String CUSTOM_CSS_PATH = "/css/style.css";

    /**
     * Establece el escenario principal de la aplicación.
     *
     * @param stage El escenario principal.
     */
    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Establece el controlador principal de la aplicación.
     *
     * @param controller El controlador principal.
     */
    public static void setMainController(MainController controller) {
        mainController = controller;
    }

    /**
     * Navega a una vista dentro del área de contenido principal.
     * <p>
     * Si el controlador principal está activo, carga la vista en su panel de contenido.
     * Si no, intenta cambiar la escena completa.
     *
     * @param fxml El archivo FXML de la vista a cargar.
     */
    public static void navigate(String fxml) {
        if (mainController != null) {
            mainController.loadView(fxml);
        } else {
            switchScene(fxml);
        }
    }

    /**
     * Cambia la escena actual por una nueva.
     * <p>
     * Carga un nuevo archivo FXML y reemplaza la escena del escenario principal.
     * Ajusta el tamaño y propiedades de la ventana según la vista (Login vs Main).
     *
     * @param fxml El archivo FXML de la escena a cargar.
     */
    public static void switchScene(String fxml) {
        if (primaryStage == null) {
            return;
        }
        try {

            String ruta = "/org/dam2/adp/ecorastro/view/" + fxml;

            URL resource = Navigation.class.getResource(ruta);

            if (resource == null) {
                System.err.println(" ERROR: No se encuentra: " + ruta);
                System.err.println("   Verifica que el archivo " + fxml + " está en src/main/resources/org/dam2/adp/ecorastro/view/");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Scene scene = new Scene(root);

            primaryStage.setScene(scene);

            if (fxml.equals("login.fxml") || fxml.equals("register.fxml")) {
                primaryStage.setResizable(false);
                primaryStage.sizeToScene();
                primaryStage.centerOnScreen();
            } else {
                primaryStage.setResizable(true);
                primaryStage.setMaximized(true);
            }

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}