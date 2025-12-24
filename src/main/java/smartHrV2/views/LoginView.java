/*package smartHrV2.views;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginView {
    private Stage primaryStage;
    
    public void show(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Création des composants
        Label titleLabel = new Label("Connexion");
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        Label passwordLabel = new Label("Mot de passe:");
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Se connecter");
        
        // Gestion de l'événement de connexion
        loginButton.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();
            
            // Ici, vous pouvez ajouter la logique de vérification des identifiants
            System.out.println("Tentative de connexion avec email: " + email);
        });
        
        // Mise en page
        VBox layout = new VBox(10);
        layout.getChildren().addAll(titleLabel, emailLabel, emailField, passwordLabel, passwordField, loginButton);
        
        // Configuration de la scène
        Scene scene = new Scene(layout, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
*/