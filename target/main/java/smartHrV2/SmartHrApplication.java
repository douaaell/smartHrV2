package smartHrV2;

import javafx.application.Application;
import javafx.stage.Stage;

public class SmartHrApplication extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Configuration de la fenêtre principale
        primaryStage.setTitle("Smart HR Application");
        
        try {
            // Initialisation de la vue de connexion
            LoginView login = new LoginView();
            login.show(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de l'application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}