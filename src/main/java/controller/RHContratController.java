package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;

import model.Contrat;
import model.Employe;
import service.ContratService;
import service.EmployeService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RHContratController {

    @FXML
    private TableView<Contrat> contratsTable;

    @FXML
    private TableColumn<Contrat, String> colId;

    @FXML
    private TableColumn<Contrat, String> colEmploye;

    @FXML
    private TableColumn<Contrat, String> colType;

    @FXML
    private TableColumn<Contrat, String> colDateDebut;

    @FXML
    private TableColumn<Contrat, String> colDateFin;

    @FXML
    private TableColumn<Contrat, Double> colSalaire;

    @FXML
    private TableColumn<Contrat, String> colPoste;

    @FXML
    private TableColumn<Contrat, String> colDepartement;

    @FXML
    private TableColumn<Contrat, String> colStatut;

    @FXML
    private TableColumn<Contrat, Void> colActions;

    @FXML
    private ComboBox<String> filterStatut;

    @FXML
    private ComboBox<String> filterType;

    @FXML
    private TextField filterEmploye;

    @FXML
    private CheckBox alerteExpiration;

    @FXML
    private Label lblTotalContrats;

    private final ContratService contratService = new ContratService();
    private final EmployeService employeService = new EmployeService();
    private ObservableList<Contrat> contratsList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // =================================================================================
    // SECTION LOGIN: AddContract (Formulaire nouveau contrat)
    // =================================================================================
    @FXML
    private ComboBox<Employe> employeCombo;
    @FXML
    private ComboBox<String> typeContratCombo;
    @FXML
    private TextField posteField;
    @FXML
    private DatePicker dateDebutPicker;
    @FXML
    private DatePicker dateFinPicker;
    @FXML
    private TextField salaireField;

    @FXML
    public void initialize() {
        // Mode LISTE : Si on a la table
        if (contratsTable != null) {
            setupTableColumns();
            setupFilters();
            contratService.actualiserStatutsContrats(); // Mettre à jour les expirations
            loadContrats();
        }

        // Mode AJOUT : Si on a les ComboBox du formulaire
        if (employeCombo != null) {
            setupAddContractForm();
        }
    }

    private void setupAddContractForm() {
        // Charger les types de contrat
        typeContratCombo.setItems(FXCollections.observableArrayList("CDI", "CDD", "Stage"));
        typeContratCombo.setValue("CDI");

        // Charger les employés
        List<Employe> employes = employeService.listerEmployes();
        employeCombo.setItems(FXCollections.observableArrayList(employes));

        // Personnaliser l'affichage de la ComboBox pour afficher "Nom Prénom"
        employeCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Employe item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() + " " + item.getPrenom() + " (" + item.getEmail() + ")");
                }
            }
        });

        // Affichage du bouton sélectionné
        employeCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Employe item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() + " " + item.getPrenom());
                }
            }
        });
    }

    @FXML
    public void handleCreer() {
        if (!validateInput())
            return;

        try {
            Employe selectedEmploye = employeCombo.getValue();
            String type = typeContratCombo.getValue();
            String poste = posteField.getText().trim();
            LocalDate debut = dateDebutPicker.getValue();
            LocalDate fin = dateFinPicker.getValue();
            double salaire = Double.parseDouble(salaireField.getText().trim());

            // Création du contrat
            // id, email, type, debut, fin, salaire, poste, departement, benefits, status,
            // fileNamePdf
            Contrat contrat = new Contrat(
                    java.util.UUID.randomUUID().toString(),
                    selectedEmploye.getEmail(),
                    type,
                    debut,
                    fin,
                    salaire,
                    poste,
                    selectedEmploye.getDepartement(),
                    "Standard", // Benefits
                    "Actif", // Status
                    null // PDF
            );

            contratService.ajouterContrat(contrat);

            showAlert("Succès", "Contrat créé avec succès !", Alert.AlertType.INFORMATION);
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le salaire doit être un nombre valide (ex: 45000.00).", Alert.AlertType.ERROR);
        } catch (IllegalStateException | IllegalArgumentException e) {
            showAlert("Règle métier", e.getMessage(), Alert.AlertType.WARNING);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de créer le contrat : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateInput() {
        if (employeCombo.getValue() == null) {
            showAlert("Attention", "Veuillez sélectionner un employé.", Alert.AlertType.WARNING);
            return false;
        }
        if (posteField.getText().trim().isEmpty() || salaireField.getText().trim().isEmpty()
                || dateDebutPicker.getValue() == null) {
            showAlert("Attention", "Veuillez remplir tous les champs obligatoires.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    @FXML
    public void handleAnnuler() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = null;
        if (posteField != null && posteField.getScene() != null) {
            stage = (Stage) posteField.getScene().getWindow();
        }
        if (stage != null)
            stage.close();
    }

    // =================================================================================
    // SECTION EXISTING METHODS
    // =================================================================================

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        colEmploye.setCellValueFactory(cellData -> {
            String email = cellData.getValue().getEmployeeEmail();
            try {
                Employe employe = employeService.findEmployeeByEmail(email);
                return new javafx.beans.property.SimpleStringProperty(
                        employe != null ? employe.getNom() + " " + employe.getPrenom() : email);
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty(email);
            }
        });

        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDateDebut.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStartDate() != null ? cellData.getValue().getStartDate().format(dateFormatter)
                        : ""));
        colDateFin.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getEndDate() != null ? cellData.getValue().getEndDate().format(dateFormatter)
                        : ""));

        colSalaire.setCellValueFactory(new PropertyValueFactory<>("salary"));
        colSalaire.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DHS", item));
                }
            }
        });
        colPoste.setCellValueFactory(new PropertyValueFactory<>("position"));
        colDepartement.setCellValueFactory(new PropertyValueFactory<>("department"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add action buttons
        colActions.setCellFactory(createActionCellFactory());
    }

    private Callback<TableColumn<Contrat, Void>, TableCell<Contrat, Void>> createActionCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Contrat, Void> call(final TableColumn<Contrat, Void> param) {
                return new TableCell<>() {
                    private final Button btnModifier = new Button("✎");
                    private final Button btnSupprimer = new Button("🗑");
                    private final Button btnDetails = new Button("👁");
                    private final javafx.scene.layout.HBox actionsBox = new javafx.scene.layout.HBox(12, btnDetails,
                            btnModifier,
                            btnSupprimer);

                    {
                        actionsBox.setAlignment(javafx.geometry.Pos.CENTER);
                        String iconStyle = "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0; -fx-font-size: 18px;";

                        btnDetails.setStyle(iconStyle + "-fx-text-fill: #3B82F6;");
                        btnModifier.setStyle(iconStyle + "-fx-text-fill: #10B981;");
                        btnSupprimer.setStyle(iconStyle + "-fx-text-fill: #EF4444;");

                        btnDetails.setTooltip(new Tooltip("Détails"));
                        btnModifier.setTooltip(new Tooltip("Modifier"));
                        btnSupprimer.setTooltip(new Tooltip("Supprimer"));

                        btnModifier.setOnAction(event -> {
                            Contrat contrat = getTableView().getItems().get(getIndex());
                            handleModifierContrat(contrat);
                        });

                        btnSupprimer.setOnAction(event -> {
                            Contrat contrat = getTableView().getItems().get(getIndex());
                            handleSupprimerContrat(contrat);
                        });

                        btnDetails.setOnAction(event -> {
                            Contrat contrat = getTableView().getItems().get(getIndex());
                            handleDetailsContrat(contrat);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(actionsBox);
                        }
                    }
                };
            }
        };
    }

    private void setupFilters() {
        // Setup status filter (Title Case to match typical data)
        filterStatut.setItems(FXCollections.observableArrayList(
                "Tous", "Actif", "Suspendu", "Terminé"));
        filterStatut.setValue("Tous");

        // Setup type filter
        filterType.setItems(FXCollections.observableArrayList(
                "Tous", "CDI", "CDD", "Stage"));
        filterType.setValue("Tous");

        // Add listeners for auto-refresh
        filterStatut.valueProperty().addListener((obs, oldVal, newVal) -> handleRechercher());
        filterType.valueProperty().addListener((obs, oldVal, newVal) -> handleRechercher());
        filterEmploye.textProperty().addListener((obs, oldVal, newVal) -> handleRechercher());
        alerteExpiration.selectedProperty().addListener((obs, oldVal, newVal) -> handleRechercher());
    }

    private void loadContrats() {
        try {
            List<Contrat> allContrats = contratService.listerContrats();
            contratsList.clear();
            contratsList.addAll(allContrats);
            contratsTable.setItems(contratsList);
            updateTotalLabel();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les contrats: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateTotalLabel() {
        lblTotalContrats.setText("Total: " + contratsTable.getItems().size() + " contrats");
    }

    @FXML
    public void handleNouveauContrat() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddContract.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nouveau Contrat");
            stage.initModality(Modality.APPLICATION_MODAL); // Bloque la fenêtre principale
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Rafraîchir la liste après fermeture
            loadContrats();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleRechercher() {
        String statusFilter = filterStatut.getValue();
        String typeFilter = filterType.getValue();
        String employeFilter = filterEmploye.getText() != null ? filterEmploye.getText().toLowerCase() : "";
        boolean showAlerte = alerteExpiration.isSelected();

        List<Contrat> filteredContrats = contratsList.stream().filter(contrat -> {
            // Status filter (case insensitive)
            if (statusFilter != null && !"Tous".equalsIgnoreCase(statusFilter)) {
                if (contrat.getStatus() == null || !statusFilter.equalsIgnoreCase(contrat.getStatus())) {
                    return false;
                }
            }

            // Type filter (case insensitive)
            if (typeFilter != null && !"Tous".equalsIgnoreCase(typeFilter)) {
                if (contrat.getType() == null || !typeFilter.equalsIgnoreCase(contrat.getType())) {
                    return false;
                }
            }

            // Employee filter
            if (!employeFilter.isEmpty()) {
                try {
                    Employe employe = employeService.findEmployeeByEmail(contrat.getEmployeeEmail());
                    if (employe != null) {
                        String fullName = (employe.getNom() + " " + employe.getPrenom()).toLowerCase();
                        if (!fullName.contains(employeFilter))
                            return false;
                    } else {
                        // Check email directly if employee not found
                        if (!contrat.getEmployeeEmail().toLowerCase().contains(employeFilter)) {
                            return false;
                        }
                    }
                } catch (Exception e) {
                    // If employee not found, check email
                    if (!contrat.getEmployeeEmail().toLowerCase().contains(employeFilter)) {
                        return false;
                    }
                }
            }

            // Expiration alert filter
            if (showAlerte && contrat.getEndDate() != null) {
                LocalDate today = LocalDate.now();
                LocalDate alertDate = today.plusDays(30);
                if (contrat.getEndDate().isAfter(alertDate)) {
                    return false;
                }
            }

            return true;
        }).collect(Collectors.toList());

        contratsTable.setItems(FXCollections.observableArrayList(filteredContrats));
        updateTotalLabel();
    }

    @FXML
    public void handleResetFilters() {
        filterStatut.setValue("Tous");
        filterType.setValue("Tous");
        filterEmploye.clear();
        alerteExpiration.setSelected(false);
        contratsTable.setItems(contratsList);
        updateTotalLabel();
    }

    @FXML
    public void handleExporterCSV() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Enregistrer le fichier CSV");
            fileChooser.getExtensionFilters()
                    .add(new javafx.stage.FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
            fileChooser.setInitialFileName("contrats_export.csv");
            java.io.File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                contratService.exportToCsv(file.getAbsolutePath());
                showAlert("Succès", "Export CSV réussi vers : " + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'exporter en CSV: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleAlertesExpiration() {
        try {
            List<Contrat> contratsExpirant = contratService.contratsFinProche(30);

            if (contratsExpirant.isEmpty()) {
                showAlert("Information", "Aucun contrat n'expire dans les 30 prochains jours",
                        Alert.AlertType.INFORMATION);
            } else {
                StringBuilder message = new StringBuilder("Contrats expirant dans les 30 prochains jours:\n\n");
                for (Contrat contrat : contratsExpirant) {
                    try {
                        Employe employe = employeService.findEmployeeByEmail(contrat.getEmployeeEmail());
                        String employeName = employe != null ? employe.getNom() + " " + employe.getPrenom()
                                : contrat.getEmployeeEmail();

                        message.append("• ").append(employeName)
                                .append(" - Expire le: ").append(contrat.getEndDate().format(dateFormatter))
                                .append("\n");
                    } catch (Exception e) {
                        message.append("• ").append(contrat.getEmployeeEmail())
                                .append(" - Expire le: ").append(contrat.getEndDate().format(dateFormatter))
                                .append("\n");
                    }
                }

                showAlert("Alertes Expiration", message.toString(), Alert.AlertType.WARNING);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de récupérer les alertes: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleModifierContrat(Contrat contrat) {
        try {
            Employe employe = employeService.findEmployeeByEmail(contrat.getEmployeeEmail());
            if (employe == null) {
                showAlert("Erreur", "Employé introuvable pour ce contrat.", Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditEmployee.fxml"));
            EmployeController controller = new EmployeController();
            loader.setController(controller);

            Parent root = loader.load();
            controller.setEmployeForEdit(employe);

            Stage stage = new Stage();
            stage.setTitle("Modifier Contrat");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadContrats();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleSupprimerContrat(Contrat contrat) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer le contrat");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer le contrat " + contrat.getId() + " ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                contratService.supprimerContrat(contrat.getId());
                showAlert("Succès", "Contrat supprimé avec succès", Alert.AlertType.INFORMATION);
                loadContrats();
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de supprimer le contrat: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void handleDetailsContrat(Contrat contrat) {
        try {
            Employe employe = employeService.findEmployeeByEmail(contrat.getEmployeeEmail());
            if (employe == null) {
                showAlert("Erreur", "Employé introuvable pour ce contrat.", Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ViewEmployee.fxml"));
            EmployeController controller = new EmployeController();
            loader.setController(controller);

            Parent root = loader.load();
            controller.setEmployeForView(employe);

            Stage stage = new Stage();
            stage.setTitle("Détails Contrat / Employé");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
