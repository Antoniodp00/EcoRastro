package org.dam2.adp.ecorastro;

import javafx.application.Application;

/**
 * Clase lanzadora de la aplicación.
 * <p>
 * Se utiliza como punto de entrada para evitar problemas con la carga de módulos
 * de JavaFX en archivos JAR ejecutables.
 *
 * @author TuNombre
 * @version 1.0
 */
public class Launcher {
    /**
     * Método principal que inicia la aplicación.
     *
     * @param args Argumentos de línea de comandos.
     */
    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }
}