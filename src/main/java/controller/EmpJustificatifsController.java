package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.Button;
import model.Document;
import model.Employe;
import service.DocumentService;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.List;

public class EmpJustificatifsController {

    @FXML
    private Label lblValides;
    @FXML
    private Label lblEnAttente;
    @FXML
    private Label lblRefuses;
    @FXML
    private Label lblTableTitle;
    @FXML
    private TableView<Document> tableJustificatifs;
    @FXML
    private TableColumn<Document, String> colDate; // Absence date (not in model)
    @FXML
    private TableColumn<Document, String> colType;
    @FXML
    private TableColumn<Document, String> colDocument;
    @FXML
    private TableColumn<Document, String> colDateUpload;
    @FXML
    private TableColumn<Document, String> colStatut; // Status (not in model)
    @FXML
    private TableColumn<Document, Void> colActions;

    private final DocumentService documentService = new DocumentService();

    @FXML
    public void initialize() {
        Employe emp = EmployeeDashboardController.getCurrentEmploye();
        if (emp != null) {
            loadDocuments(emp.getEmail());
        }
    }

    private void loadDocuments(String email) {
        List<Document> docs = documentService.findDocumentsByEmployeeEmail(email);

        // Count validated, pending and rejected documents
        long validatedCount = docs.stream().filter(d -> d.getStatut() == Document.StatutDocument.VALIDE).count();
        long pendingCount = docs.stream().filter(d -> d.getStatut() == Document.StatutDocument.EN_ATTENTE).count();
        long rejectedCount = docs.stream().filter(d -> d.getStatut() == Document.StatutDocument.REFUSE).count();

        lblValides.setText(String.valueOf(validatedCount));
        lblEnAttente.setText(String.valueOf(pendingCount));
        if (lblRefuses != null) {
            lblRefuses.setText(String.valueOf(rejectedCount));
        }
        lblTableTitle.setText("Mes justificatifs (" + docs.size() + ")");

        ObservableList<Document> data = FXCollections.observableArrayList(docs);

        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateAbsence() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                return new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getDateAbsence()));
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDocument.setCellValueFactory(new PropertyValueFactory<>("nom"));

        colDateUpload.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateUpload() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                return new javafx.beans.property.SimpleStringProperty(sdf.format(cellData.getValue().getDateUpload()));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        colStatut.setCellValueFactory(cellData -> {
            if (cellData.getValue().getStatut() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatut().getLibelle());
            }
            return new javafx.beans.property.SimpleStringProperty("En attente");
        });
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setPadding(new javafx.geometry.Insets(2, 8, 2, 8));
                    badge.setStyle("-fx-background-radius: 10; -fx-font-weight: bold;");

                    if (item.equalsIgnoreCase("Validé")) {
                        badge.setStyle(badge.getStyle() + "-fx-background-color: #DEF7EC; -fx-text-fill: #03543F;");
                    } else if (item.equalsIgnoreCase("Refusé")) {
                        badge.setStyle(badge.getStyle() + "-fx-background-color: #FDE8E8; -fx-text-fill: #9B1C1C;");
                    } else {
                        badge.setStyle(badge.getStyle() + "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E;");
                    }
                    setGraphic(badge);
                }
            }
        });

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("✎");
            private final Button btnDelete = new Button("🗑");
            private final Button btnDetails = new Button("👁");
            private final Button btnDownload = new Button("📥");

            {
                String iconStyle = "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0; -fx-font-size: 18px;";

                btnDetails.setStyle(iconStyle + "-fx-text-fill: #3B82F6;");
                btnEdit.setStyle(iconStyle + "-fx-text-fill: #10B981;");
                btnDelete.setStyle(iconStyle + "-fx-text-fill: #EF4444;");
                btnDownload.setStyle(iconStyle + "-fx-text-fill: #6B7280;");

                btnEdit.setOnAction(event -> handleEdit(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(event -> handleDelete(getTableView().getItems().get(getIndex())));
                btnDetails.setOnAction(event -> handleDetails(getTableView().getItems().get(getIndex())));
                btnDownload.setOnAction(event -> handleDownload(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Document doc = getTableView().getItems().get(getIndex());
                    // Only allow Edit/Delete if "En attente" or "Refusé" maybe? Restrict to Pending
                    // for now.
                    boolean isPending = doc.getStatut() == Document.StatutDocument.EN_ATTENTE;

                    btnEdit.setDisable(!isPending);
                    btnDelete.setDisable(!isPending);

                    // Style disabled buttons
                    if (!isPending) {
                        btnEdit.setStyle(btnEdit.getStyle() + "-fx-opacity: 0.5;");
                        btnDelete.setStyle(btnDelete.getStyle() + "-fx-opacity: 0.5;");
                    }

                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(12, btnDetails, btnEdit, btnDelete,
                            btnDownload);
                    buttons.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        tableJustificatifs.setItems(data);
    }

    private void handleEdit(Document doc) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/AjouterJustificatif.fxml"));
            javafx.scene.Parent root = loader.load();

            AjouterJustificatifController controller = loader.getController();
            controller.setDocumentForEdit(doc);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Modifier le justificatif");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();

            // Refresh table
            Employe emp = EmployeeDashboardController.getCurrentEmploye();
            if (emp != null)
                loadDocuments(emp.getEmail());

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir la fenêtre de modification : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void handleDelete(Document doc) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer");
        alert.setHeaderText("Supprimer le justificatif ?");
        alert.setContentText("Voulez-vous vraiment supprimer ce document ? (" + doc.getNom() + ")");

        if (alert.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            try {
                // Delete from DB
                documentService.deleteDocument(doc.getId());

                // Try to delete file
                if (doc.getChemin() != null) {
                    java.io.File file = new java.io.File(doc.getChemin());
                    if (file.exists()) {
                        file.delete();
                    }
                }

                // Refresh table
                Employe emp = EmployeeDashboardController.getCurrentEmploye();
                if (emp != null)
                    loadDocuments(emp.getEmail());

            } catch (Exception e) {
                e.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Erreur");
                error.setContentText("Erreur lors de la suppression : " + e.getMessage());
                error.showAndWait();
            }
        }
    }

    private void handleDetails(Document doc) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Aperçu : " + doc.getNom());
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(15);
        layout.setPadding(new javafx.geometry.Insets(20));
        layout.setStyle("-fx-background-color: white;");
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        // Metadata
        String infos = String.format("Type: %s\nDate: %s\nStatut: %s",
                doc.getType(),
                doc.getDateAbsence() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(doc.getDateAbsence())
                        : "-",
                doc.getStatut().getLibelle());

        javafx.scene.control.Label lblInfos = new javafx.scene.control.Label(infos);
        lblInfos.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151; -fx-font-weight: bold;");
        layout.getChildren().add(lblInfos);

        // Preview
        if (doc.getChemin() != null && new java.io.File(doc.getChemin()).exists()) {
            java.io.File file = new java.io.File(doc.getChemin());
            String fileName = file.getName().toLowerCase();

            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                try {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(file.toURI().toString());
                    javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
                    imgView.setFitWidth(550);
                    imgView.setFitHeight(450);
                    imgView.setPreserveRatio(true);
                    layout.getChildren().add(imgView);
                } catch (Exception e) {
                    layout.getChildren().add(new Label("Erreur chargement image"));
                }
            } else {
                // PDF or other
                javafx.scene.control.Label lblNoPreview = new javafx.scene.control.Label("📄 " + fileName);
                lblNoPreview.setStyle("-fx-font-size: 18px;");

                javafx.scene.control.Button btnOpen = new javafx.scene.control.Button("Ouvrir le fichier");
                btnOpen.setStyle(
                        "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-cursor: hand; -fx-background-radius: 5;");
                btnOpen.setOnAction(e -> {
                    try {
                        if (java.awt.Desktop.isDesktopSupported()) {
                            java.awt.Desktop.getDesktop().open(file);
                        }
                    } catch (java.io.IOException ex) {
                        ex.printStackTrace();
                    }
                });
                layout.getChildren().addAll(lblNoPreview, btnOpen);
            }
        } else {
            javafx.scene.control.Label lblErr = new javafx.scene.control.Label(
                    "Fichier introuvable sur le disque :\n" + doc.getChemin());
            lblErr.setStyle("-fx-text-fill: red;");
            layout.getChildren().add(lblErr);
        }

        javafx.scene.control.Button btnClose = new javafx.scene.control.Button("Fermer");
        btnClose.setStyle(
                "-fx-background-color: #E5E7EB; -fx-text-fill: black; -fx-padding: 8 16; -fx-background-radius: 5; -fx-cursor: hand;");
        btnClose.setOnAction(e -> stage.close());
        layout.getChildren().add(btnClose);

        javafx.scene.Scene scene = new javafx.scene.Scene(layout, 600, 650);
        stage.setScene(scene);
        stage.show();
    }

    private void handleDownload(Document doc) {
        if (doc.getChemin() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Aucun chemin de fichier enregistré.");
            alert.showAndWait();
            return;
        }

        java.io.File sourceFile = new java.io.File(doc.getChemin());
        if (!sourceFile.exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Le fichier source est introuvable sur le disque.\nChemin: " + doc.getChemin());
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le document");

        // Suggest original name
        fileChooser.setInitialFileName(doc.getNom());

        // Suggest saving to Downloads
        String userHome = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new java.io.File(userHome + "/Downloads"));

        java.io.File destFile = fileChooser.showSaveDialog(lblTableTitle.getScene().getWindow());

        if (destFile != null) {
            try {
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setContentText("Fichier téléchargé avec succès !");
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Erreur lors du téléchargement : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    public void handleAddJustificatif() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/AjouterJustificatif.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Ajouter un justificatif");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();

            // Refresh view after closing the modal
            Employe emp = EmployeeDashboardController.getCurrentEmploye();
            if (emp != null) {
                loadDocuments(emp.getEmail());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleExportCsv() {
        if (tableJustificatifs.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Export CSV");
            alert.setHeaderText("Aucune donnée à exporter");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter mes justificatifs");
        fileChooser.setInitialFileName("mes_justificatifs.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));

        String userHome = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new java.io.File(userHome));

        java.io.File destFile = fileChooser.showSaveDialog(lblTableTitle.getScene().getWindow());

        if (destFile != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(destFile, "UTF-8")) {
                writer.println("Date d'absence;Type;Nom du document;Date d'upload;Statut");

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                for (Document doc : tableJustificatifs.getItems()) {
                    String dateAbsence = doc.getDateAbsence() != null ? dateFormat.format(doc.getDateAbsence()) : "";
                    String type = escapeCsv(doc.getType());
                    String nom = escapeCsv(doc.getNom());
                    String dateUpload = doc.getDateUpload() != null ? dateTimeFormat.format(doc.getDateUpload()) : "";
                    String statut = doc.getStatut() != null ? doc.getStatut().getLibelle() : "";

                    writer.println(String.format("%s;%s;%s;%s;%s", dateAbsence, type, nom, dateUpload, statut));
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export réussi");
                alert.setHeaderText(null);
                alert.setContentText("Justificatifs exportés vers :\n" + destFile.getAbsolutePath());
                alert.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Erreur lors de l'export : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private String escapeCsv(String text) {
        if (text == null)
            return "";
        return text.replace(";", ",");
    }
}
