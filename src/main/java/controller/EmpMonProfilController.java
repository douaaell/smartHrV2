package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.control.Button;
import model.Employe;
import model.Contrat;
import model.Document;
import service.ContratService;
import service.CongeService;
import service.DocumentService;
import java.util.List;
import java.io.IOException;

public class EmpMonProfilController {

    @FXML
    private Label lblNom;
    @FXML
    private Label lblPoste;
    @FXML
    private Label lblDepartement;
    @FXML
    private Label lblEmail;
    @FXML
    private Label lblTelephone;
    @FXML
    private Label lblAdresse;
    @FXML
    private Label lblContratType;
    @FXML
    private Label lblDateEmbauche;
    @FXML
    private Label lblSalaire;

    @FXML
    private Label lblCP;
    @FXML
    private Label lblCpDetails; // Added
    @FXML
    private ProgressBar progressCP;
    @FXML
    private VBox documentsList; // Added

    private final ContratService contratService = new ContratService();
    private final CongeService congeService = new CongeService();
    private final DocumentService documentService = new DocumentService();

    @FXML
    public void initialize() {
        Employe emp = EmployeeDashboardController.getCurrentEmploye();
        if (emp != null) {
            lblNom.setText(emp.getPrenom() + " " + emp.getNom());
            lblPoste.setText(emp.getPosition());
            lblDepartement.setText(emp.getDepartement());
            lblEmail.setText(emp.getEmail());
            lblTelephone.setText(emp.getTelephone());
            lblAdresse.setText(emp.getAdresse());

            // Contrat
            List<Contrat> contrats = contratService.filtrerParEmploye(emp.getEmail());
            if (!contrats.isEmpty()) {
                Contrat c = contrats.get(0);
                lblContratType.setText(c.getType());
                lblDateEmbauche.setText(c.getStartDate().toString());
                lblSalaire.setText(String.format("%.2f DHS", c.getSalary()));
            } else {
                lblContratType.setText("-");
                lblDateEmbauche.setText("-");
                lblSalaire.setText("-");
            }

            loadLeaveStats(emp.getId());
            loadDocuments(emp.getEmail());
        }
    }

    private void loadLeaveStats(int empId) {
        int joursRestants = congeService.getRemainingVacationDays(empId);
        int totalJours = 25; // TODO: Retrieve from config or contract if dynamic
        int joursUtilises = totalJours - joursRestants;

        lblCP.setText(joursRestants + " jours");
        double progress = (double) joursRestants / totalJours;
        progressCP.setProgress(progress);

        lblCpDetails.setText(joursUtilises + " utilisés sur " + totalJours);
    }

    private void loadDocuments(String email) {
        if (documentsList == null)
            return;

        documentsList.getChildren().clear();
        List<Document> docs = documentService.findDocumentsByEmployeeEmail(email);

        if (docs.isEmpty()) {
            Label placeholder = new Label("Aucun document disponible.");
            placeholder.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            documentsList.getChildren().add(placeholder);
        } else {
            for (Document doc : docs) {
                HBox row = new HBox(10);
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 10; -fx-background-radius: 5;");

                Label icon = new Label(doc.getType().equals("Autre") ? "📄" : "📊");
                icon.setStyle("-fx-font-size: 20px;");

                VBox info = new VBox();
                Label title = new Label(doc.getNom());
                title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1F2937; -fx-font-size: 14px;");
                Label subtitle = new Label(doc.getType() + " • "
                        + (doc.getDateUpload() != null
                                ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(doc.getDateUpload())
                                : ""));
                subtitle.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
                info.getChildren().addAll(title, subtitle);

                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                Button btnDownload = new Button("Télécharger");
                btnDownload.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #1a73e8; -fx-font-weight: bold; -fx-cursor: hand;");
                btnDownload.setOnAction(e -> handleDownload(doc)); // Requires implementation or reuse

                row.getChildren().addAll(icon, info, spacer, btnDownload);
                documentsList.getChildren().add(row);
            }
        }
    }

    private void handleDownload(Document doc) {
        // Reuse download logic or create helper
        if (doc.getChemin() == null)
            return;

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Enregistrer le document");
        fileChooser.setInitialFileName(doc.getNom());

        java.io.File sourceFile = new java.io.File(doc.getChemin());
        if (!sourceFile.exists()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setContentText("Fichier introuvable.");
            alert.show();
            return;
        }

        java.io.File dest = fileChooser.showSaveDialog(lblNom.getScene().getWindow());
        if (dest != null) {
            try {
                java.nio.file.Files.copy(sourceFile.toPath(), dest.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleDemandeConge() {
        openModal("/fxml/DemandeConge.fxml", "Nouvelle demande de congé");
    }

    @FXML
    public void handleJustifierAbsence() {
        openModal("/fxml/AjouterJustificatif.fxml", "Ajouter un justificatif");
    }

    private void openModal(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            showStage(root, title);
            initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showStage(Parent root, String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.setScene(new javafx.scene.Scene(root));
        stage.showAndWait();
    }
}
