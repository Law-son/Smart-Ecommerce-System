package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.utils.NavigationHelper;

/**
 * Main application class for Smart E-Commerce System.
 * Initializes JavaFX application and loads the Login screen.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load Login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 600, 400);
            primaryStage.setTitle("Smart E-Commerce System - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error loading application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
