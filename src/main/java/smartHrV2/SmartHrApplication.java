package smartHrV2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.AuthService;

public class SmartHrApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SmartRH - Connexion");

        try {
            // Créer les utilisateurs par défaut si nécessaire
            AuthService authService = new AuthService();
            authService.createDefaultUsers();
            
            // Charger le FXML de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(loader.load(), 500, 600);

            // Ajouter le CSS
            scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la page de login: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
/*
package smartHrV2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SmartHrApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Smart HR - Tableau de Bord Employé");

        try {
            // Charger le FXML du dashboard Employé
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Employee_Dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);

            // Ajouter le CSS
            scene.getStylesheets().add(getClass().getResource("/css/employee_dashboard.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du Tableau de Bord Employé: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}*/