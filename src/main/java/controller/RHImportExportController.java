package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import model.Employe;
import model.Contrat;
import service.EmployeService;
import service.ContratService;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class RHImportExportController {

    @FXML
    private ComboBox<String> exportTypeCombo;
    @FXML
    private ComboBox<String> exportDeptCombo;
    @FXML
    private ComboBox<String> exportStatutCombo;
    @FXML
    private VBox historyList;

    private final EmployeService employeService = new EmployeService();
    private final ContratService contratService = new ContratService();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupCombos();
    }

    private void setupCombos() {
        exportTypeCombo.setItems(FXCollections.observableArrayList("Tous les contrats", "CDI", "CDD", "Stage"));
        exportTypeCombo.setValue("Tous les contrats");

        exportDeptCombo.setItems(FXCollections.observableArrayList(
                "Tous les départements", "RH", "Informatique", "Finance", "Marketing", "Ventes", "Logistique"));
        exportDeptCombo.setValue("Tous les départements");

        exportStatutCombo.setItems(FXCollections.observableArrayList("Tous les employés", "Employés actifs uniquement",
                "Employés inactifs uniquement"));
        exportStatutCombo.setValue("Tous les employés");
    }

    @FXML
    public void handlePickFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            importCsv(file);
        }
    }

    private void importCsv(File file) {
        executor.execute(() -> {
            int successCount = 0;
            int errorCount = 0;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                boolean isHeader = true;

                while ((line = reader.readLine()) != null) {
                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }

                    try {
                        String[] data = line.split(",");
                        if (data.length < 10)
                            continue;

                        String nom = data[0].trim();
                        String prenom = data[1].trim();
                        String email = data[2].trim();
                        String tel = data[3].trim();
                        String adresse = data[4].trim();
                        String dept = data[5].trim();
                        String poste = data[6].trim();
                        String typeContrat = data[7].trim();
                        LocalDate debut = LocalDate.parse(data[8].trim(), dateFormatter);
                        double salaire = Double.parseDouble(data[9].trim());

                        // 1. Create Employee
                        Employe emp = new Employe(0, nom, prenom, email, "password123", dept, poste, tel, adresse);
                        boolean empAdded = employeService.ajouterEmploye(emp);

                        if (empAdded) {
                            // 2. Create Contract
                            Contrat contrat = new Contrat(
                                    java.util.UUID.randomUUID().toString(),
                                    email, typeContrat, debut, null, salaire, poste, dept, "Standard", "Actif", null);
                            contratService.ajouterContrat(contrat);
                            successCount++;
                        }
                    } catch (Exception e) {
                        errorCount++;
                    }
                }

                final int finalSuccess = successCount;
                final int finalErrors = errorCount;

                javafx.application.Platform.runLater(() -> {
                    showAlert("Importation terminée",
                            finalSuccess + " employés importés avec succès.\n" + finalErrors + " erreurs.",
                            Alert.AlertType.INFORMATION);
                    addHistoryItem(file, "Import", finalSuccess);
                });

            } catch (IOException e) {
                javafx.application.Platform.runLater(() -> showAlert("Erreur",
                        "Impossible de lire le fichier: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        });
    }

    @FXML
    public void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer l'export CSV");
        fileChooser.setInitialFileName("export_employes_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            exportCsv(file);
        }
    }

    private void exportCsv(File file) {
        executor.execute(() -> {
            try {
                List<Employe> employes = employeService.listerEmployes();
                List<Contrat> contrats = contratService.listerContrats();

                // Apply Filters
                String typeFilter = exportTypeCombo.getValue();
                String deptFilter = exportDeptCombo.getValue();

                List<Employe> filtered = employes.stream()
                        .filter(e -> deptFilter.equals("Tous les départements")
                                || e.getDepartement().equals(deptFilter))
                        .collect(Collectors.toList());

                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.println(
                            "Nom,Prenom,Email,Telephone,Adresse,Departement,Poste,Type Contrat,Date Debut,Salaire");

                    for (Employe e : filtered) {
                        Contrat c = contrats.stream()
                                .filter(co -> co.getEmployeeEmail().equals(e.getEmail())
                                        && "Actif".equals(co.getStatus()))
                                .findFirst().orElse(null);

                        if (c != null) {
                            if (!typeFilter.equals("Tous les contrats") && !c.getType().equals(typeFilter))
                                continue;

                            writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%.2f",
                                    e.getNom(), e.getPrenom(), e.getEmail(), e.getTelephone(), e.getAdresse(),
                                    e.getDepartement(), e.getPosition(), c.getType(),
                                    (c.getStartDate() != null ? c.getStartDate().format(dateFormatter) : ""),
                                    c.getSalary()));
                        }
                    }
                }

                javafx.application.Platform.runLater(() -> {
                    showAlert("Export réussi", "Les données ont été exportées vers " + file.getName(),
                            Alert.AlertType.INFORMATION);
                    addHistoryItem(file, "Export", filtered.size());
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> showAlert("Erreur",
                        "Une erreur est survenue lors de l'export: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        });
    }

    private void addHistoryItem(java.io.File file, String type, int count) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.layout.HBox item = new javafx.scene.layout.HBox(15);
            item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            item.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");

            // Icône box colorée
            javafx.scene.layout.StackPane iconBox = new javafx.scene.layout.StackPane();
            String iconColor = type.equalsIgnoreCase("Import") ? "#E1EFFE" : "#DEF7EC";
            String iconEmoji = type.equalsIgnoreCase("Import") ? "📤" : "📥";
            iconBox.setStyle("-fx-background-color: " + iconColor + "; -fx-background-radius: 8; -fx-padding: 10;");
            Label iconLabel = new Label(iconEmoji);
            iconLabel.setStyle("-fx-font-size: 18px;");
            iconBox.getChildren().add(iconLabel);

            // Infos fichier
            javafx.scene.layout.VBox info = new javafx.scene.layout.VBox(2);
            Label lblFile = new Label(file.getName());
            lblFile.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 14px;");
            Label lblDetails = new Label(
                    java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " • "
                            + count
                            + " enregistrements");
            lblDetails.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
            info.getChildren().addAll(lblFile, lblDetails);

            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            // Badge de statut
            Label statusBadge = new Label(type);
            String badgeStyle = type.equalsIgnoreCase("Import")
                    ? "-fx-background-color: #E1EFFE; -fx-text-fill: #1E429F;"
                    : "-fx-background-color: #DEF7EC; -fx-text-fill: #03543F;";
            statusBadge.setStyle(
                    badgeStyle
                            + "-fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");

            item.getChildren().addAll(iconBox, info, spacer, statusBadge);

            // Ajout du bouton Télécharger si c'est un Export
            if (type.equalsIgnoreCase("Export")) {
                Hyperlink downloadLink = new Hyperlink("📥 Télécharger");
                downloadLink.setStyle("-fx-text-fill: #2563EB; -fx-font-size: 12px; -fx-underline: false;");

                downloadLink.setOnAction(e -> {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Enregistrer une copie du CSV");
                    fileChooser.setInitialFileName(file.getName());
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
                    java.io.File target = fileChooser.showSaveDialog(null);
                    if (target != null) {
                        try {
                            java.nio.file.Files.copy(file.toPath(), target.toPath(),
                                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            showAlert("Succès", "Le fichier a été enregistré sous : " + target.getAbsolutePath(),
                                    Alert.AlertType.INFORMATION);
                        } catch (java.io.IOException ex) {
                            showAlert("Erreur", "Impossible de copier le fichier : " + ex.getMessage(),
                                    Alert.AlertType.ERROR);
                        }
                    }
                });

                item.getChildren().add(downloadLink);
            }

            if (historyList.getChildren().size() > 0 && historyList.getChildren().get(0) instanceof Label) {
                historyList.getChildren().clear();
            }
            historyList.getChildren().add(0, item);
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
