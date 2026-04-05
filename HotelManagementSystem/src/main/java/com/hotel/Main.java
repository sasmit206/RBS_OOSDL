package com.hotel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Application entry point.
 * Loads the FXML layout, attaches the CSS stylesheet, and displays the primary stage.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        URL fxmlURL = getClass().getResource("/fxml/MainView.fxml");
        if (fxmlURL == null) {
            throw new IOException("Cannot find MainView.fxml – check resources folder.");
        }

        FXMLLoader loader = new FXMLLoader(fxmlURL);
        Scene scene = new Scene(loader.load(), 1100, 700);

        // Attach the application stylesheet
        URL cssURL = getClass().getResource("/css/styles.css");
        if (cssURL != null) {
            scene.getStylesheets().add(cssURL.toExternalForm());
        }

        primaryStage.setTitle("Hotel Management System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
