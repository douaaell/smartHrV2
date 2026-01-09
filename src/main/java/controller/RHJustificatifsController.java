package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.scene.layout.HBox;
import model.Document;
import model.Employe;
import service.DocumentService;
import service.EmployeService;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

public class RHJustificatifsController {

    @FXML
    private Label lblEnAttente;
    @FXML
    private Label lblValides;
    @FXML
    private Label lblRefuses;
    @FXML
    private Label lblTableTitle;
    @FXML
    private ComboBox<String> comboStatutFilter;
    @FXML
    private TextField txtSearchEmploye;
    @FXML
    private TableView<DocumentWithEmployee> tableJustificatifs;
    @FXML
    private TableColumn<DocumentWithEmployee, String> colEmploye;
    @FXML
    private TableColumn<DocumentWithEmployee, String> colType;
    @FXML
    private TableColumn<DocumentWithEmployee, String> colDateAbsence;
    @FXML
    private TableColumn<DocumentWithEmployee, String> colDocument;
    @FXML
    private TableColumn<DocumentWithEmployee, String> colDateUpload;
    @FXML
    private TableColumn<DocumentWithEmployee, String> colStatut;
    @FXML
    private TableColumn<DocumentWithEmployee, Void> colActions;

    private final DocumentService documentService = new DocumentService();
    private final EmployeService employeService = new EmployeService();
    private ObservableList<DocumentWithEmployee> allDocuments;

    @FXML
    public void initialize() {
        // Initialize the list early to avoid null pointers in listeners
        allDocuments = FXCollections.observableArrayList();

        setupTableColumns();

        // Initialize filter combo
        comboStatutFilter.getItems().clear();
        comboStatutFilter.getItems().addAll("Tous", "En attente", "Validé", "Refusé");
        comboStatutFilter.getSelectionModel().selectFirst();

        // Add listeners for automatic search
        comboStatutFilter.valueProperty().addListener((obs, oldVal, newVal) -> handleSearch());
        txtSearchEmploye.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());

        loadAllDocuments();
    }

    private void setupTableColumns() {
        // Employe column
        colEmploye.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getEmploye().getNom() + " " +
                        cellData.getValue().getEmploye().getPrenom()));

        // Type column
        colType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDocument().getType()));

        // Date absence column
        colDateAbsence.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDocument().getDateAbsence() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                return new javafx.beans.property.SimpleStringProperty(
                        sdf.format(cellData.getValue().getDocument().getDateAbsence()));
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        // Document column
        colDocument.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDocument().getNom()));

        // Date upload column
        colDateUpload.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDocument().getDateUpload() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                return new javafx.beans.property.SimpleStringProperty(
                        sdf.format(cellData.getValue().getDocument().getDateUpload()));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        // Statut column with badges
        colStatut.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDocument().getStatut() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDocument().getStatut().getLibelle());
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
                    badge.setPadding(new javafx.geometry.Insets(4, 12, 4, 12));
                    badge.setStyle("-fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;");

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

        // Actions column
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnVoir = new Button("👁");
            private final Button btnTelecharger = new Button("📥");
            private final Button btnValider = new Button("✅");
            private final Button btnRefuser = new Button("❌");

            {
                String iconStyle = "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0; -fx-font-size: 18px;";

                btnVoir.setStyle(iconStyle + "-fx-text-fill: #3B82F6;");
                btnTelecharger.setStyle(iconStyle + "-fx-text-fill: #8B5CF6;");
                btnValider.setStyle(iconStyle + "-fx-text-fill: #10B981;");
                btnRefuser.setStyle(iconStyle + "-fx-text-fill: #EF4444;");

                btnVoir.setTooltip(new Tooltip("Voir détails"));
                btnTelecharger.setTooltip(new Tooltip("Télécharger"));
                btnValider.setTooltip(new Tooltip("Valider"));
                btnRefuser.setTooltip(new Tooltip("Refuser"));

                btnVoir.setOnAction(event -> {
                    DocumentWithEmployee item = getTableView().getItems().get(getIndex());
                    handleVoir(item.getDocument());
                });

                btnTelecharger.setOnAction(event -> {
                    DocumentWithEmployee item = getTableView().getItems().get(getIndex());
                    downloadDocument(item.getDocument());
                });

                btnValider.setOnAction(event -> {
                    DocumentWithEmployee item = getTableView().getItems().get(getIndex());
                    handleValider(item.getDocument());
                });

                btnRefuser.setOnAction(event -> {
                    DocumentWithEmployee item = getTableView().getItems().get(getIndex());
                    handleRefuser(item.getDocument());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(12, btnVoir, btnTelecharger, btnValider, btnRefuser);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadAllDocuments() {
        // Get all employes and their documents
        List<Employe> employes = employeService.listerEmployes();
        allDocuments.clear();

        for (Employe emp : employes) {
            List<Document> docs = documentService.findDocumentsByEmployeeEmail(emp.getEmail());
            for (Document doc : docs) {
                allDocuments.add(new DocumentWithEmployee(doc, emp));
            }
        }

        updateStats();
        handleSearch(); // Keep active filter
    }

    private void updateStats() {
        long pending = allDocuments.stream()
                .filter(d -> d.getDocument().getStatut() == Document.StatutDocument.EN_ATTENTE)
                .count();
        long validated = allDocuments.stream()
                .filter(d -> d.getDocument().getStatut() == Document.StatutDocument.VALIDE)
                .count();
        long refused = allDocuments.stream()
                .filter(d -> d.getDocument().getStatut() == Document.StatutDocument.REFUSE)
                .count();

        lblEnAttente.setText(String.valueOf(pending));
        lblValides.setText(String.valueOf(validated));
        lblRefuses.setText(String.valueOf(refused));
    }

    private void displayDocuments(ObservableList<DocumentWithEmployee> documents) {
        lblTableTitle.setText("Tous les justificatifs (" + documents.size() + ")");
        tableJustificatifs.setItems(documents);
    }

    private void handleValider(Document doc) {
        doc.setStatut(Document.StatutDocument.VALIDE);
        documentService.updateDocument(doc);
        loadAllDocuments();
        showAlert("Succès", "Le justificatif a été validé.");
    }

    private void handleRefuser(Document doc) {
        doc.setStatut(Document.StatutDocument.REFUSE);
        documentService.updateDocument(doc);
        loadAllDocuments();
        showAlert("Succès", "Le justificatif a été refusé.");
    }

    private void handleVoir(Document doc) {
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
                doc.getDateAbsence() != null ? new SimpleDateFormat("dd/MM/yyyy").format(doc.getDateAbsence())
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

    private void downloadDocument(Document doc) {
        try {
            java.io.File file = new java.io.File(doc.getChemin());
            if (file.exists()) {
                // Open file with default application
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(file);
                } else {
                    showAlert("Information", "Fichier: " + doc.getChemin());
                }
            } else {
                showAlert("Erreur", "Fichier introuvable: " + doc.getChemin());
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le document: " + e.getMessage());
        }
    }

    @FXML
    public void handleSearch() {
        String search = txtSearchEmploye.getText().toLowerCase();
        String statutFilter = comboStatutFilter.getSelectionModel().getSelectedItem();

        ObservableList<DocumentWithEmployee> filtered = allDocuments.stream()
                .filter(d -> {
                    String fullName = (d.getEmploye().getNom() + " " + d.getEmploye().getPrenom()).toLowerCase();
                    String nom = d.getEmploye().getNom().toLowerCase();
                    String prenom = d.getEmploye().getPrenom().toLowerCase();

                    boolean matchesSearch = search.isEmpty() ||
                            fullName.contains(search) ||
                            nom.contains(search) ||
                            prenom.contains(search);

                    boolean matchesStatut = statutFilter.equals("Tous") ||
                            (d.getDocument().getStatut() != null &&
                                    d.getDocument().getStatut().getLibelle().equals(statutFilter));

                    return matchesSearch && matchesStatut;
                })
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        displayDocuments(filtered);
    }

    @FXML
    public void handleReset() {
        txtSearchEmploye.clear();
        comboStatutFilter.getSelectionModel().selectFirst();
        displayDocuments(allDocuments);
    }

    @FXML
    public void handleExportCsv() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Enregistrer le fichier CSV");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
            fileChooser.setInitialFileName("justificatifs_rh_export.csv");

            java.io.File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                exportToCsv(file.getAbsolutePath());
                showAlert("Succès", "Export CSV réussi vers : " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'export CSV: " + e.getMessage());
        }
    }

    private void exportToCsv(String filePath) throws java.io.IOException {
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(filePath))) {
            // Header
            writer.write("Employé,Type,Date d'absence,Document,Date d'upload,Statut");
            writer.newLine();

            // Data rows
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            for (DocumentWithEmployee docWithEmp : tableJustificatifs.getItems()) {
                Document doc = docWithEmp.getDocument();
                Employe emp = docWithEmp.getEmploye();

                String employeName = emp.getNom() + " " + emp.getPrenom();
                String type = doc.getType() != null ? doc.getType() : "";
                String dateAbsence = doc.getDateAbsence() != null ? sdf.format(doc.getDateAbsence()) : "";
                String docName = doc.getNom() != null ? doc.getNom() : "";
                String dateUpload = doc.getDateUpload() != null ? sdf.format(doc.getDateUpload()) : "";
                String statut = doc.getStatut() != null ? doc.getStatut().getLibelle() : "";

                writer.write(String.format("%s,%s,%s,%s,%s,%s",
                        escapeCsv(employeName),
                        escapeCsv(type),
                        dateAbsence,
                        escapeCsv(docName),
                        dateUpload,
                        escapeCsv(statut)));
                writer.newLine();
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null)
            return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Helper class to combine Document with Employee
    public static class DocumentWithEmployee {
        private final Document document;
        private final Employe employe;

        public DocumentWithEmployee(Document document, Employe employe) {
            this.document = document;
            this.employe = employe;
        }

        public Document getDocument() {
            return document;
        }

        public Employe getEmploye() {
            return employe;
        }
    }
}
