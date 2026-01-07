package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import model.Employe;
import service.EmployeService;

import java.io.IOException;

public class EmployeeDashboardController {

    @FXML
    private VBox mainContent;
    @FXML
    private Button btnMonProfil;
    @FXML
    private Button btnMesConges;
    @FXML
    private Button btnJustificatifs;
    @FXML
    private Label lblUserName;
    @FXML
    private Label lblUserEmail;
    @FXML
    private Label lblPageTitle;
    @FXML
    private Label lblPageSubtitle;

    private static Employe currentEmploye; // User logged in
    private final EmployeService employeService = new EmployeService();

    @FXML
    public void initialize() {
        // Mock user if null (For testing without functionality of login passing)
        if (currentEmploye == null) {
            // Try to find a default employee or create one for demo
            currentEmploye = employeService.listerEmployes().stream().findFirst().orElse(null);
            if (currentEmploye == null) {
                // Fallback mock
                currentEmploye = new Employe(0, "Dupont", "Jean", "jean.dupont@smarthr.com", "password",
                        "Développement", "Développeur Full Stack", "0612345678", "Paris");
            }
        }

        updatUserInfo();
        showMonProfil(); // Default view
    }

    public static void setCurrentEmploye(Employe employe) {
        currentEmploye = employe;
    }

    public static Employe getCurrentEmploye() {
        return currentEmploye;
    }

    private void updatUserInfo() {
        if (currentEmploye != null) {
            lblUserName.setText(currentEmploye.getPrenom() + " " + currentEmploye.getNom());
            lblUserEmail.setText(currentEmploye.getEmail());
        }
    }

    @FXML
    public void showMonProfil() {
        if (lblPageTitle != null)
            lblPageTitle.setText("Mon Profil");
        if (lblPageSubtitle != null)
            lblPageSubtitle.setText("Gérez vos informations personnelles et professionnelles");
        loadView("/fxml/Emp_MonProfil.fxml", btnMonProfil);
    }

    @FXML
    public void showMesConges() {
        if (lblPageTitle != null)
            lblPageTitle.setText("Mes Congés");
        if (lblPageSubtitle != null)
            lblPageSubtitle.setText("Gérer mes demandes et consulter mon historique de congés");
        loadView("/fxml/Emp_MesConges.fxml", btnMesConges);
    }

    @FXML
    public void showJustificatifs() {
        if (lblPageTitle != null)
            lblPageTitle.setText("Justificatifs");
        if (lblPageSubtitle != null)
            lblPageSubtitle.setText("Télécharger et gérer vos justificatifs d'absence");
        loadView("/fxml/Emp_Justificatifs.fxml", btnJustificatifs);
    }

    @FXML
    public void handleLogout() {
        try {
            // 1. Clear session
            currentEmploye = null;
            System.out.println("🔓 Déconnexion effectuée - Session nettoyée");

            // 2. Prepare to open Login Screen
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
                Parent root = loader.load();

                javafx.stage.Stage loginStage = new javafx.stage.Stage();
                loginStage.setTitle("SmartRH - Connexion");
                loginStage.setScene(new javafx.scene.Scene(root));

                // Show Login Screen FIRST
                loginStage.show();
                System.out.println("🔄 Retour à l'écran de connexion");

                // 3. Close current window (Dashboard) AFTER showing login
                if (mainContent.getScene() != null && mainContent.getScene().getWindow() != null) {
                    javafx.stage.Stage stage = (javafx.stage.Stage) mainContent.getScene().getWindow();
                    stage.close();
                }

            } catch (IOException e) {
                System.err.println("❌ Erreur lors de la réouverture du login: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de la déconnexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath, Button activeButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            mainContent.getChildren().clear();
            mainContent.getChildren().add(view);

            updateActiveButton(activeButton);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateActiveButton(Button active) {
        btnMonProfil.getStyleClass().remove("active");
        btnMesConges.getStyleClass().remove("active");
        btnJustificatifs.getStyleClass().remove("active");

        if (active != null) {
            active.getStyleClass().add("active");
        }
    }
}
