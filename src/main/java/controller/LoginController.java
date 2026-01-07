package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Employe;
import service.AuthService;
import service.EmployeService;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ToggleButton adminToggle;
    @FXML
    private ToggleButton employeToggle;
    @FXML
    private Button loginButton;
    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService();
    private final EmployeService employeService = new EmployeService();

    @FXML
    public void initialize() {
        // Setup toggle group for account type
        ToggleGroup accountGroup = new ToggleGroup();
        adminToggle.setToggleGroup(accountGroup);
        employeToggle.setToggleGroup(accountGroup);

        // Set default selection
        adminToggle.setSelected(true);

        // Clear error message when user types
        emailField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> clearError());

        // Clear error when toggle changes
        accountGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> clearError());
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = getSelectedRole();

        System.out.println("🔍 LoginController - Tentative de connexion:");
        System.out.println("  - Email: '" + email + "'");
        System.out.println("  - Mot de passe: '" + password + "'");
        System.out.println("  - Rôle: '" + role + "'");

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("⚠️ Validation échouée - champs vides");
            showError("Veuillez remplir tous les champs");
            return;
        }

        if (!email.contains("@")) {
            System.out.println("⚠️ Validation échouée - email invalide");
            showError("Email invalide");
            return;
        }

        // Authentication
        try {
            System.out.println("🔄 Appel de authService.authenticate()...");
            boolean isAuthenticated = authService.authenticate(email, password, role);

            System.out.println("📊 Résultat de l'authentification: " + isAuthenticated);

            if (isAuthenticated) {
                System.out.println("✅ Connexion réussie - Rôle: " + role);

                // Redirect based on role
                if ("RH".equals(role)) {
                    // Fetch and set the current RH session
                    Employe user = employeService.findEmployeeByEmail(email);
                    if (user != null) {
                        System.out.println("👤 Session RH définie pour: " + user.getEmail());
                        RHDashboardController.setCurrentRH(user);
                        openRHInterface();
                    } else {
                        System.err
                                .println("❌ Erreur: Impossible de récupérer les détails du RH après authentification.");
                        // Fallback: still open but with default info
                        openRHInterface();
                    }
                } else if ("EMPLOYE".equals(role)) {
                    // Fetch and set the current employee session
                    Employe user = employeService.findEmployeeByEmail(email);
                    if (user != null) {
                        System.out.println("👤 Session utilisateur définie pour: " + user.getEmail());
                        EmployeeDashboardController.setCurrentEmploye(user);
                        openEmployeeInterface();
                    } else {
                        System.err.println(
                                "❌ Erreur: Impossible de récupérer les détails de l'employé après authentification.");
                        showError("Erreur lors du chargement du profil.");
                    }
                }

                // Close login window
                Stage loginStage = (Stage) loginButton.getScene().getWindow();
                loginStage.close();

            } else {
                System.out.println("❌ Authentification échouée");
                showError("Email ou mot de passe incorrect");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la connexion: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur de connexion. Veuillez réessayer.");
        }
    }

    private String getSelectedRole() {
        if (adminToggle.isSelected()) {
            return "RH";
        } else if (employeToggle.isSelected()) {
            return "EMPLOYE";
        }
        return null;
    }

    private void openRHInterface() throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RH_Dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("SmartRH - Interface RH");
            stage.setScene(new Scene(root, 1400, 900));
            stage.setMaximized(true);
            stage.show();

            System.out.println("🔄 Interface RH ouverte");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'ouverture de l'interface RH: " + e.getMessage());
            e.printStackTrace();

            // Fallback: show simple alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir l'interface RH");
            alert.setContentText("Veuillez contacter l'administrateur système.");
            alert.showAndWait();
        }
    }

    private void openEmployeeInterface() throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Employee_Dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("SmartRH - Interface Employé");
            stage.setScene(new Scene(root, 1400, 900));
            stage.setMaximized(true);
            stage.show();

            System.out.println("🔄 Interface Employé ouverte");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'ouverture de l'interface Employé: " + e.getMessage());
            e.printStackTrace();

            // Fallback: show simple alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir l'interface Employé");
            alert.setContentText("Veuillez contacter l'administrateur système.");
            alert.showAndWait();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 14px;");
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setText("");
    }
}
