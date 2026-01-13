package org.dam2.adp.ecorastro;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
      FXMLLoader loader = new FXMLLoader(Main.class.getResource("view/login.fxml"));
      Scene scene = new Scene(loader.load(),400,500);

      String css = Objects.requireNonNull(Main.class.getResource("style.css")).toExternalForm();
      scene.getStylesheets().add(css);

      stage.setTitle("EcoRastro");
      stage.setScene(scene);
      stage.show();

    }
}
