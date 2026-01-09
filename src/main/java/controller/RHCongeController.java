package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;

import model.Conge;
import model.Conge.StatutConge;
import model.Conge.TypeConge;
import model.Employe;
import service.CongeService;
import service.EmployeService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RHCongeController {

    @FXML
    private TableView<Conge> congesTable;

    @FXML
    private TableColumn<Conge, Integer> colId;

    @FXML
    private TableColumn<Conge, String> colEmploye;

    @FXML
    private TableColumn<Conge, TypeConge> colType;

    @FXML
    private TableColumn<Conge, String> colDateDebut;

    @FXML
    private TableColumn<Conge, String> colDateFin;

    @FXML
    private TableColumn<Conge, Integer> colDuree;

    @FXML
    private TableColumn<Conge, StatutConge> colStatut;

    @FXML
    private TableColumn<Conge, String> colMotif;

    @FXML
    private TableColumn<Conge, Void> colActions;

    @FXML
    private ToggleGroup statusGroup;

    // Note: Type filter was hidden in new design, but we keep the logic if needed
    // later
    // or we can remove it. For now, we will default to "Tous".

    @FXML
    private Label lblTotalConges;

    @FXML
    private Label lblPendingCount;

    @FXML
    private Label lblApprovedCount;

    @FXML
    private Label lblRejectedCount;

    private final CongeService congeService = new CongeService();
    private final EmployeService employeService = new EmployeService();
    private ObservableList<Conge> congesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        // Filters are setup in FXML via ToggleGroup
        loadConges();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        colEmploye.setCellValueFactory(cellData -> {
            int employeeId = cellData.getValue().getEmployeeId();
            try {
                Employe employe = employeService.findEmployeeById(employeeId);
                return new javafx.beans.property.SimpleStringProperty(
                        employe != null ? employe.getNom() + " " + employe.getPrenom() : "Inconnu");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Erreur");
            }
        });

        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDateDebut.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDateDebut() != null
                        ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(cellData.getValue().getDateDebut())
                        : ""));
        colDateFin.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDateFin() != null
                        ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(cellData.getValue().getDateFin())
                        : ""));

        colDuree.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().calculerDuree()));

        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));

        // Add action buttons
        colActions.setCellFactory(createActionCellFactory());
    }

    private Callback<TableColumn<Conge, Void>, TableCell<Conge, Void>> createActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Conge, Void> call(final TableColumn<Conge, Void> param) {
                return new TableCell<>() {
                    private final Button btnValider = new Button("✅");
                    private final Button btnRefuser = new Button("❌");
                    private final Button btnDetails = new Button("👁");
                    private final javafx.scene.layout.HBox actionsBox = new javafx.scene.layout.HBox(12, btnValider,
                            btnRefuser, btnDetails);

                    {
                        String iconStyle = "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0; -fx-font-size: 18px;";

                        btnValider.setStyle(iconStyle + "-fx-text-fill: #10B981;");
                        btnRefuser.setStyle(iconStyle + "-fx-text-fill: #EF4444;");
                        btnDetails.setStyle(iconStyle + "-fx-text-fill: #3B82F6;");

                        btnValider.setTooltip(new Tooltip("Valider"));
                        btnRefuser.setTooltip(new Tooltip("Refuser"));
                        btnDetails.setTooltip(new Tooltip("Voir les détails"));

                        btnValider.setOnAction(event -> handleValiderConge(getTableView().getItems().get(getIndex())));
                        btnRefuser.setOnAction(event -> handleRefuserConge(getTableView().getItems().get(getIndex())));
                        btnDetails.setOnAction(event -> handleDetailsConge(getTableView().getItems().get(getIndex())));
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Conge conge = getTableView().getItems().get(getIndex());

                            // Show/hide buttons based on status
                            btnValider.setVisible(conge.getStatut() == StatutConge.EN_ATTENTE);
                            btnRefuser.setVisible(conge.getStatut() == StatutConge.EN_ATTENTE);

                            setGraphic(actionsBox);
                        }
                    }
                };
            }
        };
    }

    private void loadConges() {
        try {
            List<Conge> allConges = congeService.findAllConges();
            congesList.clear();
            congesList.addAll(allConges);
            congesTable.setItems(congesList);
            updateStatistics();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les congés: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateStatistics() {
        // Update total
        if (lblTotalConges != null) {
            lblTotalConges.setText("Total: " + congesTable.getItems().size() + " congés");
        }

        // Count pending, approved, and rejected
        long pendingCount = congesList.stream()
                .filter(c -> c.getStatut() == StatutConge.EN_ATTENTE)
                .count();
        long approvedCount = congesList.stream()
                .filter(c -> c.getStatut() == StatutConge.VALIDE)
                .count();
        long rejectedCount = congesList.stream()
                .filter(c -> c.getStatut() == StatutConge.REFUSE)
                .count();

        // Update cards
        if (lblPendingCount != null) {
            lblPendingCount.setText(String.valueOf(pendingCount));
        }
        if (lblApprovedCount != null) {
            lblApprovedCount.setText(String.valueOf(approvedCount));
        }
        if (lblRejectedCount != null) {
            lblRejectedCount.setText(String.valueOf(rejectedCount));
        }
    }

    @FXML
    public void handleFilterChange() {
        handleRechercher();
    }

    @FXML
    public void handleRechercher() {
        String statusFilter = "Tous";
        if (statusGroup.getSelectedToggle() != null) {
            statusFilter = (String) statusGroup.getSelectedToggle().getUserData();
        }

        // Use default if type filter is missing in UI
        // String typeFilter = "Tous"; // TODO: Implement type filter

        // String employeFilter = ""; // TODO: Implement employee search

        String finalStatusFilter = statusFilter;
        List<Conge> filteredConges = congesList.stream().filter(conge -> {
            // Status filter
            if (!"Tous".equals(finalStatusFilter)) {
                // Approximate matching for "En attente", "Validé", "Refusé"
                StatutConge expectedStatus = getStatutFromLibelle(finalStatusFilter);
                // Adjust comparison because "Validé" vs "VALIDÉ" case sensitivity or exact enum
                // match
                if (conge.getStatut() != expectedStatus) {
                    // Try flexible matching
                    if ("En attente".equals(finalStatusFilter) && conge.getStatut() == StatutConge.EN_ATTENTE)
                        return true;
                    if ("Validé".equals(finalStatusFilter) && conge.getStatut() == StatutConge.VALIDE)
                        return true;
                    if ("Refusé".equals(finalStatusFilter) && conge.getStatut() == StatutConge.REFUSE)
                        return true;
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());

        congesTable.setItems(FXCollections.observableArrayList(filteredConges));
        updateStatistics();
    }

    @FXML
    public void handleResetFilters() {
        statusGroup.selectToggle(statusGroup.getToggles().get(0)); // Select "Tous"
        congesTable.setItems(congesList);
        updateStatistics();
    }

    @FXML
    public void handleValiderSelection() {
        ObservableList<Conge> selectedConges = congesTable.getSelectionModel().getSelectedItems();
        if (selectedConges.isEmpty()) {
            showAlert("Information", "Veuillez sélectionner au moins un congé à valider", Alert.AlertType.INFORMATION);
            return;
        }

        int validatedCount = 0;
        for (Conge conge : selectedConges) {
            if (conge.getStatut() == StatutConge.EN_ATTENTE) {
                try {
                    congeService.updateCongeStatus(conge.getId(), StatutConge.VALIDE, null);
                    validatedCount++;
                } catch (Exception e) {
                    System.err.println("Erreur validation congé " + conge.getId() + ": " + e.getMessage());
                }
            }
        }

        if (validatedCount > 0) {
            showAlert("Succès", validatedCount + " congé(s) validé(s) avec succès", Alert.AlertType.INFORMATION);
            loadConges();
        } else {
            showAlert("Information", "Aucun congé en attente parmi la sélection", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    public void handleRefuserSelection() {
        ObservableList<Conge> selectedConges = congesTable.getSelectionModel().getSelectedItems();
        if (selectedConges.isEmpty()) {
            showAlert("Information", "Veuillez sélectionner au moins un congé à refuser", Alert.AlertType.INFORMATION);
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Motif de refus");
        dialog.setHeaderText("Veuillez saisir le motif du refus");
        dialog.setContentText("Motif:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String motifRefus = result.get();
            int refusedCount = 0;

            for (Conge conge : selectedConges) {
                if (conge.getStatut() == StatutConge.EN_ATTENTE) {
                    try {
                        congeService.updateCongeStatus(conge.getId(), StatutConge.REFUSE, motifRefus);
                        refusedCount++;
                    } catch (Exception e) {
                        System.err.println("Erreur refus congé " + conge.getId() + ": " + e.getMessage());
                    }
                }
            }

            if (refusedCount > 0) {
                showAlert("Succès", refusedCount + " congé(s) refusé(s) avec succès", Alert.AlertType.INFORMATION);
                loadConges();
            } else {
                showAlert("Information", "Aucun congé en attente parmi la sélection", Alert.AlertType.INFORMATION);
            }
        }
    }

    @FXML
    public void handleExporterCSV() {
        if (congesList.isEmpty()) {
            showAlert("Information", "Il n'y a aucun congé à exporter.", Alert.AlertType.INFORMATION);
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Enregistrer l'export CSV");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        fileChooser.setInitialFileName("export_conges.csv");

        java.io.File file = fileChooser.showSaveDialog(congesTable.getScene().getWindow());
        if (file != null) {
            try {
                csv.CongeCSV.exportToCsv(congesList, file.getAbsolutePath()).get();
                showAlert("Succès", "L'export CSV a été généré avec succès.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Une erreur est survenue lors de l'export: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        }
    }

    private void handleValiderConge(Conge conge) {
        try {
            congeService.updateCongeStatus(conge.getId(), StatutConge.VALIDE, null);
            showAlert("Succès", "Congé validé avec succès", Alert.AlertType.INFORMATION);
            loadConges();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de valider le congé: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleRefuserConge(Conge conge) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Motif de refus");
        dialog.setHeaderText("Veuillez saisir le motif du refus pour ce congé");
        dialog.setContentText("Motif:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                congeService.updateCongeStatus(conge.getId(), StatutConge.REFUSE, result.get());
                showAlert("Succès", "Congé refusé avec succès", Alert.AlertType.INFORMATION);
                loadConges();
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de refuser le congé: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void handleDetailsConge(Conge conge) {
        try {
            Employe employe = employeService.findEmployeeById(conge.getEmployeeId());
            String employeName = employe != null ? employe.getNom() + " " + employe.getPrenom() : "Inconnu";

            String details = String.format(
                    "📋 DÉTAILS DU CONGÉ\n\n" +
                            "👤 Employé: %s\n" +
                            "📅 Type: %s\n" +
                            "📆 Date début: %s\n" +
                            "📆 Date fin: %s\n" +
                            "⏱️ Durée: %d jours\n" +
                            "📝 Motif: %s\n" +
                            "✅ Statut: %s\n" +
                            "📊 Date demande: %s",
                    employeName,
                    conge.getType() != null ? conge.getType().getLibelle() : "N/A",
                    conge.getDateDebut() != null
                            ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(conge.getDateDebut())
                            : "N/A",
                    conge.getDateFin() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(conge.getDateFin())
                            : "N/A",
                    conge.calculerDuree(),
                    conge.getMotif() != null && !conge.getMotif().isEmpty() ? conge.getMotif() : "-",
                    conge.getStatut() != null ? conge.getStatut().getLibelle() : "N/A",
                    conge.getDateDemande() != null
                            ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(conge.getDateDemande())
                            : "N/A");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Détails du congé #" + conge.getId());
            alert.setHeaderText(null);
            alert.setContentText(details);
            alert.showAndWait();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les détails: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private StatutConge getStatutFromLibelle(String libelle) {
        for (StatutConge statut : StatutConge.values()) {
            if (statut.getLibelle().equals(libelle)) {
                return statut;
            }
        }
        return StatutConge.EN_ATTENTE;
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
