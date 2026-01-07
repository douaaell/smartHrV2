package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Document;
import model.Employe;
import service.DocumentService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.Date;

public class AjouterJustificatifController {

    @FXML
    private DatePicker dateAbsence;
    @FXML
    private ComboBox<String> comboTypeAbsence;
    @FXML
    private VBox dropZone;
    @FXML
    private ImageView imagePreview;
    @FXML
    private Label iconLabel;
    @FXML
    private Label dropLabel;
    @FXML
    private Label dropSubLabel;
    @FXML
    private Label lblFileName;
    @FXML
    private TextArea txtCommentaire;

    private final DocumentService documentService = new DocumentService();
    private File selectedFile;

    @FXML
    public void initialize() {
        // Populate absence types
        comboTypeAbsence.getItems().addAll(
                "Arrêt maladie",
                "Rendez-vous médical",
                "Hospitalisation",
                "Accident de travail",
                "Autre motif médical");

        // Default selection
        comboTypeAbsence.getSelectionModel().selectFirst();

        // Setup file drop zone click handler
        dropZone.setOnMouseClicked(event -> handleFileSelection());
    }

    private void handleFileSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un justificatif");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"));

        Stage stage = (Stage) dropZone.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // Check file size (max 5 MB)
            long fileSizeInMB = selectedFile.length() / (1024 * 1024);
            if (fileSizeInMB > 5) {
                showAlert("Erreur", "Le fichier est trop volumineux. Taille maximale : 5 MB");
                selectedFile = null;
                lblFileName.setText("");
                resetDropZone();
                return;
            }

            // Show file name
            lblFileName.setText("✓ " + selectedFile.getName());
            updatePreview(selectedFile);
        }
    }

    private void updatePreview(java.io.File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
            try {
                Image image = new Image(file.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setVisible(true);
                imagePreview.setManaged(true);

                iconLabel.setVisible(false);
                iconLabel.setManaged(false);
                dropLabel.setVisible(false);
                dropLabel.setManaged(false);
                dropSubLabel.setVisible(false);
                dropSubLabel.setManaged(false);
            } catch (Exception e) {
                e.printStackTrace();
                resetDropZone();
            }
        } else {
            // PDF or other
            imagePreview.setVisible(false);
            imagePreview.setManaged(false);

            iconLabel.setText("✅");
            iconLabel.setVisible(true);
            iconLabel.setManaged(true);
            dropLabel.setText("Fichier sélectionné");
            dropLabel.setVisible(true);
            dropLabel.setManaged(true);
            dropSubLabel.setText(file.getName());
            dropSubLabel.setVisible(true);
            dropSubLabel.setManaged(true);
        }
    }

    private void resetDropZone() {
        if (imagePreview != null) {
            imagePreview.setVisible(false);
            imagePreview.setManaged(false);
            imagePreview.setImage(null);
        }
        if (iconLabel != null) {
            iconLabel.setText("📄");
            iconLabel.setVisible(true);
            iconLabel.setManaged(true);
        }
        if (dropLabel != null) {
            dropLabel.setText("Cliquez pour sélectionner un fichier");
            dropLabel.setVisible(true);
            dropLabel.setManaged(true);
        }
        if (dropSubLabel != null) {
            dropSubLabel.setText("ou glissez-déposez votre document ici");
            dropSubLabel.setVisible(true);
            dropSubLabel.setManaged(true);
        }
    }

    private Document documentToEdit;

    public void setDocumentForEdit(Document doc) {
        this.documentToEdit = doc;
        if (doc != null) {
            // Fill fields
            if (doc.getDateAbsence() != null) {
                dateAbsence.setValue(new java.sql.Date(doc.getDateAbsence().getTime()).toLocalDate());
            }
            comboTypeAbsence.getSelectionModel().select(doc.getType());
            lblFileName.setText("✓ " + doc.getNom() + " (Actuel)");

            // Preview existing
            if (doc.getChemin() != null) {
                java.io.File file = new java.io.File(doc.getChemin());
                if (file.exists()) {
                    updatePreview(file);
                }
            }
        }
    }

    @FXML
    public void handleUpload() {
        Employe emp = EmployeeDashboardController.getCurrentEmploye();
        if (emp == null)
            return;

        // Validation
        if (dateAbsence.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une date d'absence.");
            return;
        }

        if (comboTypeAbsence.getSelectionModel().getSelectedItem() == null) {
            showAlert("Erreur", "Veuillez sélectionner un type d'absence.");
            return;
        }

        if (selectedFile == null && documentToEdit == null) {
            showAlert("Erreur", "Veuillez sélectionner un document justificatif.");
            return;
        }

        try {
            // Convert absence date
            Date absenceDate = null;
            if (dateAbsence.getValue() != null) {
                absenceDate = Date
                        .from(dateAbsence.getValue().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            }

            // Create or Update
            Document doc = (documentToEdit != null) ? documentToEdit : new Document();

            // Handle File Update only if a new file is selected
            if (selectedFile != null) {
                // 1. Create a storage directory in the Project Root
                String storageDir = System.getProperty("user.dir") + File.separator + "uploads";
                File dir = new File(storageDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                // 2. Generate a unique filename to prevent overwrites
                String fileExt = "";
                int i = selectedFile.getName().lastIndexOf('.');
                if (i > 0) {
                    fileExt = selectedFile.getName().substring(i);
                }
                String newFileName = "justif_" + System.currentTimeMillis() + fileExt;
                Path destination = Paths.get(storageDir, newFileName);

                // 3. Copy the file to the secure folder
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                doc.setNom(selectedFile.getName());
                doc.setChemin(destination.toString());
                doc.setDateUpload(new Date()); // Update upload date on new file
            }

            // Update other fields
            doc.setType(comboTypeAbsence.getSelectionModel().getSelectedItem());
            doc.setDateAbsence(absenceDate);

            if (documentToEdit == null) {
                doc.setEmployeeEmail(emp.getEmail());
                doc.setStatut(Document.StatutDocument.EN_ATTENTE); // Default for new
                documentService.saveDocument(doc);
            } else {
                // If editing, usually status might reset to PENDING if it was REFUSED?
                // Or stay as is? Let's assume PENDING for re-validation.
                doc.setStatut(Document.StatutDocument.EN_ATTENTE);
                documentService.updateDocument(doc);
            }

            showInfoAlert("Succès", "Le justificatif a été " + (documentToEdit != null ? "mis à jour" : "téléchargé")
                    + " avec succès !");
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de sauvegarder le fichier : " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) dateAbsence.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
