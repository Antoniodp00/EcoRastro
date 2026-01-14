package org.dam2.adp.ecorastro.util;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.dam2.adp.ecorastro.controller.MainController;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidades para la navegación entre escenas en la aplicación.
 */
public class Navigation {

    private static Stage primaryStage;
    private static MainController mainController;
    private static final Logger logger = Logger.getLogger(Navigation.class.getName());

    private static final String CUSTOM_CSS_PATH = "/css/style.css";

    /**
     * Establece el escenario principal de la aplicación.
     * @param stage El escenario principal.
     */
    public static void setStage(Stage stage) {
        primaryStage = stage;
        logger.log(Level.INFO, "Primary stage establecido.");
    }

    /**
     * Establece el controlador principal de la aplicación.
     * @param controller El controlador principal.
     */
    public static void setMainController(MainController controller) {
        mainController = controller;
        logger.log(Level.INFO, "Main controller establecido.");
    }

    /**
     * Navega a una vista dentro del área de contenido principal.
     * @param fxml El archivo FXML de la vista a cargar.
     */
    public static void navigate(String fxml) {
        logger.log(Level.INFO, "Navegando a sub-vista: " + fxml);
        if (mainController != null) {
            mainController.loadView(fxml);
        } else {
            logger.log(Level.WARNING, "MainController no está establecido. Usando switchScene como fallback.");
            switchScene(fxml);
        }
    }

    /**
     * Cambia la escena actual por una nueva.
     * @param fxml El archivo FXML de la escena a cargar.
     */
    public static void switchScene(String fxml) {
        if (primaryStage == null) {
            logger.log(Level.SEVERE, "¡ERROR! Primary stage es NULL. Llama a Navigation.setStage(stage) en tu Main.");
            return;
        }
        try {
            // CORRECCIÓN CLAVE: Ruta absoluta exacta según tu estructura de carpetas
            // Fíjate que empieza por "/" y sigue toda la ruta de paquetes
            String ruta = "/org/dam2/adp/ecorastro/view/" + fxml;

            URL resource = Navigation.class.getResource(ruta);

            if (resource == null) {
                System.err.println(" ERROR: No se encuentra: " + ruta);
                System.err.println("   Verifica que el archivo " + fxml + " está en src/main/resources/org/dam2/adp/ecorastro/view/");
                return;
            }

            logger.log(Level.INFO, "Cambiando escena a: " + ruta);

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Scene scene = new Scene(root);

            primaryStage.setScene(scene);

            // Ajustes de ventana para Login/Registro
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
            logger.log(Level.SEVERE, "Error CRÍTICO al cargar el FXML: " + fxml, e);
            e.printStackTrace();
        }
    }




}
