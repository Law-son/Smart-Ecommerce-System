package org.example.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Utility class for handling screen navigation in JavaFX.
 */
public class NavigationHelper {
    
    private static final String FXML_PATH = "/fxml/";
    
    /**
     * Navigates to a new screen.
     *
     * @param fxmlFile FXML file name (e.g., "Dashboard.fxml")
     * @param currentStage Current stage
     * @return true if navigation successful, false otherwise
     */
    public static boolean navigateTo(String fxmlFile, Stage currentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationHelper.class.getResource(FXML_PATH + fxmlFile));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle(getTitleFromFxml(fxmlFile));
            currentStage.setResizable(true); // Ensure all screens are resizable
            currentStage.show();
            
            return true;
        } catch (IOException e) {
            System.err.println("Error loading FXML file: " + fxmlFile);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Opens a dialog window.
     *
     * @param fxmlFile FXML file name
     * @param ownerStage Owner stage
     * @return Stage of the dialog, or null if failed
     */
    public static Stage openDialog(String fxmlFile, Stage ownerStage) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationHelper.class.getResource(FXML_PATH + fxmlFile));
            Parent root = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(ownerStage);
            dialogStage.initStyle(StageStyle.UTILITY);
            dialogStage.setScene(new Scene(root));
            dialogStage.setTitle(getTitleFromFxml(fxmlFile));
            dialogStage.setResizable(true); // Enable resizable UI for dialogs
            dialogStage.show();
            
            return dialogStage;
        } catch (IOException e) {
            System.err.println("Error loading dialog FXML file: " + fxmlFile);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Gets a title from FXML file name.
     *
     * @param fxmlFile FXML file name
     * @return Formatted title
     */
    private static String getTitleFromFxml(String fxmlFile) {
        String name = fxmlFile.replace(".fxml", "");
        return name.replaceAll("([A-Z])", " $1").trim();
    }
    
    /**
     * Gets the current stage from a scene.
     *
     * @param scene Scene object
     * @return Stage object
     */
    public static Stage getStage(javafx.scene.Scene scene) {
        return (Stage) scene.getWindow();
    }
}



