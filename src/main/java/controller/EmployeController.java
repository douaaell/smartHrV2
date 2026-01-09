package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.scene.control.TableCell;

import model.Contrat;
import model.Employe;
import service.ContratService;
import service.EmployeService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

public class EmployeController {

    // =================================================================================
    // SECTION 1 : LISTE DES EMPLOYÉS (RH_Employe_Content.fxml)
    // =================================================================================
    @FXML
    private TableView<Employe> tableEmployes;
    @FXML
    private TableColumn<Employe, String> colNom; // Combiné (Nom + Tel)
    @FXML
    private TableColumn<Employe, String> colEmail;
    @FXML
    private TableColumn<Employe, String> colDepartement;
    @FXML
    private TableColumn<Employe, String> colPosition;
    @FXML
    private TableColumn<Employe, String> colType;
    @FXML
    private TableColumn<Employe, String> colStatut;
    @FXML
    private TableColumn<Employe, Void> colActions;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> departementFilter;
    @FXML
    private ComboBox<String> contractFilter;
    @FXML
    private Label lblSubtitle;

    private final EmployeService employeService = new EmployeService();
    private final ContratService contratService = new ContratService();
    private Map<String, String> employeContractTypeMap = new HashMap<>(); // Email -> Type Contrat
    private Map<String, String> employeContractStatusMap = new HashMap<>(); // Email -> Statut

    // =================================================================================
    // SECTION 2 : FORMULAIRE AJOUT / MODIFICATION (AddEmployee.fxml /
    // EditEmployee.fxml)
    // =================================================================================
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextField adresseField;
    @FXML
    private ComboBox<String> departementCombo;
    @FXML
    private TextField positionField;
    @FXML
    private ComboBox<String> roleCombo;
    @FXML
    private PasswordField passwordField;

    // Champs Contrat (Formulaire)
    @FXML
    private ComboBox<String> typeContratCombo;
    @FXML
    private DatePicker dateDebutPicker;
    @FXML
    private DatePicker dateFinPicker;
    @FXML
    private TextField salaireField;

    private Employe currentEmploye; // L'employé en cours d'édition (si null = Ajout)
    private Contrat currentContrat; // Le contrat en cours d'édition

    // =================================================================================
    // SECTION 3 : VUE DÉTAILS (ViewEmployee.fxml)
    // =================================================================================
    @FXML
    private Label lblNom;
    @FXML
    private Label lblEmail;
    @FXML
    private Label lblTelephone;
    @FXML
    private Label lblAdresse;
    @FXML
    private Label lblDepartement;
    @FXML
    private Label lblPoste; // position
    @FXML
    private Label lblRole;
    @FXML
    private Label lblTypeContrat; // Label badge
    @FXML
    private Label lblDateEmbauche;
    @FXML
    private Label lblSalaire;

    // =================================================================================
    // INITIALIZATION (Gère tous les cas)
    // =================================================================================
    @FXML
    public void initialize() {
        // Cas 1 : Nous sommes sur la vue LISTE (employeTable existe)
        if (tableEmployes != null) {
            setupTable();
            setupFilters();
            chargerEmployes();
        }

        // Cas 2 : Nous sommes sur un FORMULAIRE (departementCombo existe)
        if (departementCombo != null) {
            setupComboBoxes();
        }
    }

    // =================================================================================
    // LOGIQUE LISTE
    // =================================================================================
    private void setupTable() {
        // Colonne Nom combinée (Nom complet + Téléphone)
        colNom.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Employe emp = (Employe) getTableRow().getItem();
                    javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(2);
                    javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(
                            emp.getPrenom() + " " + emp.getNom());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937;");

                    // Affiche le téléphone en dessous du nom (au lieu de l'email)
                    String phone = (emp.getTelephone() != null && !emp.getTelephone().isEmpty()) ? emp.getTelephone()
                            : "";
                    javafx.scene.control.Label phoneLabel = new javafx.scene.control.Label(phone);
                    phoneLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");

                    vbox.getChildren().addAll(nameLabel, phoneLabel);
                    setGraphic(vbox);
                }
            }
        });

        // Configuration de la colonne Email
        if (colEmail != null) {
            colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        }

        colDepartement.setCellValueFactory(new PropertyValueFactory<>("departement"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));

        // Colonne Type de Contrat
        colType.setCellValueFactory(cellData -> {
            String type = employeContractTypeMap.getOrDefault(cellData.getValue().getEmail(), "-");
            return new javafx.beans.property.SimpleStringProperty(type);
        });

        // Colonne Statut (Badge)
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Employe emp = (Employe) getTableRow().getItem();
                    String status = employeContractStatusMap.getOrDefault(emp.getEmail(), "Inactif");

                    Label badge = new Label(status);
                    badge.setPadding(new javafx.geometry.Insets(4, 8, 4, 8));
                    badge.setStyle("-fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");

                    if ("Actif".equalsIgnoreCase(status)) {
                        badge.setStyle(badge.getStyle()
                                + "-fx-background-color: #DEF7EC; -fx-text-fill: #03543F; -fx-cursor: hand;");
                    } else {
                        badge.setStyle(badge.getStyle()
                                + "-fx-background-color: #FDE2E1; -fx-text-fill: #9B1C1C; -fx-cursor: hand;");
                    }

                    badge.setOnMouseClicked(event -> toggleEmployeeStatus(emp));
                    setGraphic(badge);
                }
            }
        });

        setupActionsColumn();

        // Recherche dynamique
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> filtrerEmployes());
        }
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("👁");
            private final Button editButton = new Button("✎");
            private final Button deleteButton = new Button("🗑");
            private final javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(12, viewButton, editButton,
                    deleteButton);

            {
                buttons.setAlignment(javafx.geometry.Pos.CENTER);
                String iconStyle = "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0; -fx-font-size: 18px;";

                viewButton.setTooltip(new Tooltip("Voir les détails"));
                editButton.setTooltip(new Tooltip("Modifier"));
                deleteButton.setTooltip(new Tooltip("Supprimer"));

                viewButton.setStyle(iconStyle + "-fx-text-fill: #3B82F6;");
                editButton.setStyle(iconStyle + "-fx-text-fill: #10B981;");
                deleteButton.setStyle(iconStyle + "-fx-text-fill: #EF4444;");

                viewButton.setOnAction(event -> viewEmployeeDetails(getTableView().getItems().get(getIndex())));
                editButton.setOnAction(event -> openEditor(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> supprimerEmploye(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void chargerEmployes() {
        List<Employe> employes = employeService.listerEmployes();

        // Charger les données de contrats
        contratService.actualiserStatutsContrats();
        List<Contrat> contrats = contratService.listerContrats();
        employeContractTypeMap.clear();
        employeContractStatusMap.clear();
        for (Contrat c : contrats) {
            employeContractTypeMap.put(c.getEmployeeEmail(), c.getType());
            employeContractStatusMap.put(c.getEmployeeEmail(), c.getStatus());
        }

        if (tableEmployes != null) {
            tableEmployes.setItems(FXCollections.observableArrayList(employes));
            if (lblSubtitle != null) {
                lblSubtitle.setText(employes.size() + " employés actifs");
            }
        }
    }

    // ... Filtres et autres méthodes de liste ...
    private void setupFilters() {
        if (departementFilter != null) {
            departementFilter.setItems(FXCollections.observableArrayList(
                    "Tous les départements", "RH", "Informatique", "Commercial", "Marketing", "Finance", "Direction"));
            departementFilter.setValue("Tous les départements");
            departementFilter.valueProperty().addListener((obs, oldVal, newVal) -> filtrerEmployes());
        }
        if (contractFilter != null) {
            contractFilter.setItems(FXCollections.observableArrayList(
                    "Tous les contrats", "CDI", "CDD", "Stage"));
            contractFilter.setValue("Tous les contrats");
            contractFilter.valueProperty().addListener((obs, oldVal, newVal) -> filtrerEmployes());
        }
    }

    private void filtrerEmployes() {
        String recherche = searchField != null ? searchField.getText().toLowerCase() : "";
        String dept = departementFilter != null ? departementFilter.getValue() : "Tous les départements";
        String contract = contractFilter != null ? contractFilter.getValue() : "Tous les contrats";

        List<Employe> allEmployes = employeService.listerEmployes();
        List<Employe> filtered = allEmployes.stream()
                .filter(e -> recherche.isEmpty() ||
                        e.getNom().toLowerCase().contains(recherche) ||
                        e.getPrenom().toLowerCase().contains(recherche) ||
                        e.getEmail().toLowerCase().contains(recherche))
                .filter(e -> dept.equals("Tous les départements") || e.getDepartement().equals(dept))
                .filter(e -> contract.equals("Tous les contrats") ||
                        contract.equals(employeContractTypeMap.getOrDefault(e.getEmail(), "")))
                .collect(Collectors.toList());

        if (tableEmployes != null) {
            tableEmployes.setItems(FXCollections.observableArrayList(filtered));
            if (lblSubtitle != null)
                lblSubtitle.setText(filtered.size() + " employés filtrés");
        }
    }

    // =================================================================================
    // LOGIQUE FORMULAIRE (ADD / EDIT)
    // =================================================================================
    private void setupComboBoxes() {
        if (departementCombo != null) {
            departementCombo.setItems(FXCollections.observableArrayList("RH", "Informatique", "Commercial", "Marketing",
                    "Finance", "Direction"));
        }
        if (roleCombo != null) {
            roleCombo.setItems(FXCollections.observableArrayList("RH", "EMPLOYE"));
        }
        if (typeContratCombo != null) {
            typeContratCombo.setItems(FXCollections.observableArrayList("CDI", "CDD", "Stage"));
        }
    }

    // Méthode pour charger un employé dans le formulaire (Mode EDIT)
    public void setEmployeForEdit(Employe employe) {
        this.currentEmploye = employe;

        if (nomField != null) {
            nomField.setText(employe.getNom());
            prenomField.setText(employe.getPrenom());
            emailField.setText(employe.getEmail());
            telephoneField.setText(employe.getTelephone());
            adresseField.setText(employe.getAdresse());
            departementCombo.setValue(employe.getDepartement());
            positionField.setText(employe.getPosition());
            roleCombo.setValue(employe.getRole());
            if (passwordField != null)
                passwordField.setText(employe.getPassword());

            // Charger contrat
            List<Contrat> contrats = contratService.filtrerParEmploye(employe.getEmail());
            if (!contrats.isEmpty()) {
                this.currentContrat = contrats.get(0);
                typeContratCombo.setValue(currentContrat.getType());
                if (currentContrat.getStartDate() != null)
                    dateDebutPicker.setValue(currentContrat.getStartDate());
                if (currentContrat.getEndDate() != null)
                    dateFinPicker.setValue(currentContrat.getEndDate());
                if (salaireField != null)
                    salaireField.setText(String.valueOf(currentContrat.getSalary()));
            }
        }
    }

    @FXML
    public void handleSave() {
        if (!validateInput())
            return;

        try {
            boolean isNew = (currentEmploye == null);

            if (isNew) {
                // Création
                // 1. Employé
                Employe newEmploye = new Employe(0,
                        nomField.getText().trim(), prenomField.getText().trim(),
                        emailField.getText().trim(), passwordField.getText(),
                        departementCombo.getValue(), positionField.getText().trim(),
                        telephoneField.getText().trim(), adresseField.getText().trim());
                newEmploye.setRole(roleCombo.getValue());

                boolean success = employeService.ajouterEmploye(newEmploye);

                if (success) {
                    // 2. Contrat
                    saveContract(newEmploye.getEmail());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Employé ajouté avec succès !");
                    closeWindow();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Email déjà utilisé ou erreur système.");
                }

            } else {
                // Modification
                currentEmploye.setNom(nomField.getText().trim());
                currentEmploye.setPrenom(prenomField.getText().trim());
                currentEmploye.setEmail(emailField.getText().trim());
                currentEmploye.setTelephone(telephoneField.getText().trim());
                currentEmploye.setAdresse(adresseField.getText().trim());
                currentEmploye.setDepartement(departementCombo.getValue());
                currentEmploye.setPosition(positionField.getText().trim());
                currentEmploye.setRole(roleCombo.getValue());
                if (passwordField != null && !passwordField.getText().isEmpty()) {
                    currentEmploye.setPassword(passwordField.getText());
                }

                boolean success = employeService.modifierEmploye(currentEmploye);
                if (success) {
                    saveContract(currentEmploye.getEmail()); // Update contrat
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Employé modifié avec succès !");
                    closeWindow();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void saveContract(String email) {
        LocalDate startDate = dateDebutPicker.getValue();
        LocalDate endDate = dateFinPicker.getValue();
        double salary = 0.0;
        try {
            salary = Double.parseDouble(salaireField.getText().trim());
        } catch (Exception e) {
        }

        if (currentContrat != null) {
            // Update existant
            currentContrat.setType(typeContratCombo.getValue());
            currentContrat.setStartDate(startDate);
            currentContrat.setEndDate(endDate);
            currentContrat.setSalary(salary); // Mise à jour du salaire
            currentContrat.setPosition(positionField.getText().trim());
            contratService.modifierContrat(currentContrat);
        } else {
            // Création new
            Contrat c = new Contrat(UUID.randomUUID().toString(), email,
                    typeContratCombo.getValue(), startDate, endDate, salary,
                    positionField.getText().trim(), departementCombo.getValue(),
                    "Standard", "Actif", null);
            contratService.ajouterContrat(c);
        }
    }

    private boolean validateInput() {
        if (isEmpty(nomField) || isEmpty(prenomField) || isEmpty(emailField) || isEmpty(telephoneField) ||
                departementCombo.getValue() == null || isEmpty(positionField) || roleCombo.getValue() == null ||
                typeContratCombo.getValue() == null || dateDebutPicker.getValue() == null || isEmpty(salaireField)) {

            showAlert(Alert.AlertType.WARNING, "Champ vide", "Veuillez remplir tous les champs obligatoires.");
            return false;
        }
        return true;
    }

    private boolean isEmpty(TextField t) {
        return t == null || t.getText().trim().isEmpty();
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    // =================================================================================
    // LOGIQUE DETAILS (VIEW)
    // =================================================================================
    public void setEmployeForView(Employe employe) {
        this.currentEmploye = employe;
        if (lblNom != null) {
            lblNom.setText(employe.getPrenom() + " " + employe.getNom());
            lblEmail.setText(employe.getEmail());
            lblTelephone.setText(employe.getTelephone());
            lblAdresse.setText(employe.getAdresse());
            lblDepartement.setText(employe.getDepartement());
            lblPoste.setText(employe.getPosition());
            lblRole.setText(employe.getRole());

            // Contrat details
            List<Contrat> contrats = contratService.filtrerParEmploye(employe.getEmail());
            if (!contrats.isEmpty()) {
                Contrat c = contrats.get(0);
                lblTypeContrat.setText(c.getType());
                if (c.getStartDate() != null) {
                    lblDateEmbauche.setText(c.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
                lblSalaire.setText(String.format("%.2f DHS", c.getSalary()));
            } else {
                lblTypeContrat.setText("-");
                lblDateEmbauche.setText("-");
                lblSalaire.setText("-");
            }
        }
    }

    @FXML
    public void handleEdit() {
        // Appelé depuis ViewEmployee.fxml
        handleEditFromView();
    }

    @FXML
    public void handleEditFromView() {
        // Ferme la vue détail
        closeWindow();
        // Ouvre l'éditeur
        openEditor(currentEmploye);
    }

    // =================================================================================
    // UTILITAIRES / NAVIGATION
    // =================================================================================

    // Ouvre le formulaire d'ajout (vide)
    @FXML
    public void handleAddEmployee() {
        openModal("/fxml/AddEmployee.fxml", "Ajouter un employé", null);
    }

    @FXML
    public void exportToCsv() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Exporter les employés en CSV");
            fileChooser.getExtensionFilters()
                    .add(new javafx.stage.FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
            java.io.File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                employeService.exportToCsv(file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Exportation réussie vers : " + file.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'exportation : " + e.getMessage());
        }
    }

    @FXML
    public void ajouterEmploye() {
        handleAddEmployee();
    }

    // Ouvre le modale Éditer
    private void openEditor(Employe emp) {
        openModal("/fxml/EditEmployee.fxml", "Modifier Employé", emp);
    }

    // Ouvre le modale Voir
    private void viewEmployeeDetails(Employe emp) {
        openModal("/fxml/ViewEmployee.fxml", "Détails Employé", emp);
    }

    private void openModal(String fxmlPath, String title, Employe data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            // Important: Le fichier FXML doit pointer vers CE controller
            // 'controller.EmployeController'
            loader.setController(null); // On reset pour éviter les conflits si le FXML en a un
            loader.setController(this); // On force CE controller ? Non, ça crée une récursion infinie ou instance
                                        // partagée bizarre.

            // Meilleure approche pour un controller partagé :
            // 1. Le FXML définit 'controller.EmployeController'
            // 2. loader.load() crée une NOUVELLE instance de EmployeController
            // 3. On récupère cette instance pour lui passer les données

            // Note: Comme 'data' doit être passé, on ne peut pas utiliser 'this' (le
            // controller liste).
            // On doit laisser loader créer une NOUVELLE instance propre au modale.

            Parent root = loader.load();
            EmployeController modalController = loader.getController();

            if (data != null) {
                if (fxmlPath.contains("View"))
                    modalController.setEmployeForView(data);
                else
                    modalController.setEmployeForEdit(data);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Si on revient sur la liste, on rafraîchit
            if (this.tableEmployes != null)
                chargerEmployes();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre : " + e.getMessage());
        }
    }

    private void supprimerEmploye(Employe employe) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cet employé ?",
                ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            employeService.supprimerEmploye(employe.getId());
            chargerEmployes();
        }
    }

    private void toggleEmployeeStatus(Employe emp) {
        List<Contrat> contrats = contratService.filtrerParEmploye(emp.getEmail());
        if (!contrats.isEmpty()) {
            Contrat c = contrats.get(0);
            String currentStatus = c.getStatus();
            String newStatus = "Actif".equalsIgnoreCase(currentStatus) ? "Inactif" : "Actif";
            c.setStatus(newStatus);
            contratService.modifierContrat(c);

            // Mise à jour immédiate de l'interface
            employeContractStatusMap.put(emp.getEmail(), newStatus);
            tableEmployes.refresh();

            System.out.println("🔄 Statut de l'employé " + emp.getNom() + " changé en : " + newStatus);
        } else {
            showAlert(Alert.AlertType.WARNING, "Attention", "Cet employé n'a pas de contrat associé.");
        }
    }

    @FXML
    public void handleClose() {
        closeWindow();
    }

    private void closeWindow() {
        try {
            Stage stage = null;
            // Essayer de récupérer le stage via différents composants
            if (nomField != null && nomField.getScene() != null) {
                stage = (Stage) nomField.getScene().getWindow();
            } else if (lblNom != null && lblNom.getScene() != null) {
                stage = (Stage) lblNom.getScene().getWindow();
            } else if (tableEmployes != null && tableEmployes.getScene() != null) {
                stage = (Stage) tableEmployes.getScene().getWindow();
            }

            if (stage != null) {
                stage.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
