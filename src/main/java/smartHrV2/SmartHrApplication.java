package smartHrV2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SmartHrApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Smart HR Application");

        try {
            // Charger le FXML du dashboard RH
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RH_Dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);

            // Ajouter le CSS
            scene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du Dashboard RH: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
