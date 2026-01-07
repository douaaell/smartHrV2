package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Conge;
import model.Conge.TypeConge;
import model.Employe;
import service.CongeService;

import java.time.ZoneId;
import java.util.Date;

public class DemandeCongeController {

    @FXML
    private ComboBox<TypeCongeOption> comboTypeConge;
    @FXML
    private DatePicker dateDebut;
    @FXML
    private DatePicker dateFin;
    @FXML
    private TextArea txtMotif;

    private final CongeService congeService = new CongeService();

    @FXML
    public void initialize() {
        Employe emp = EmployeeDashboardController.getCurrentEmploye();
        int cp = 0;
        if (emp != null) {
            cp = congeService.getRemainingVacationDays(emp.getId());
        }
        // Populate ComboBox with synchronized days
        comboTypeConge.getItems()
                .add(new TypeCongeOption(TypeConge.ANNUEL, "Congés payés (" + cp + " jours restants)"));
        comboTypeConge.getItems().add(new TypeCongeOption(TypeConge.MALADIE, "Congé maladie"));
        comboTypeConge.getItems().add(new TypeCongeOption(TypeConge.SANS_SOLDE, "Congé sans solde"));
        comboTypeConge.getItems().add(new TypeCongeOption(TypeConge.MATERNITE, "Congé parental"));
        comboTypeConge.getItems().add(new TypeCongeOption(TypeConge.AUTRE, "Autre"));

        // Default selection
        comboTypeConge.getSelectionModel().selectFirst();
    }

    private Conge congeToEdit;

    public void setCongeForEdit(Conge conge) {
        this.congeToEdit = conge;
        if (conge != null) {
            // Pre-fill fields
            if (conge.getDateDebut() != null) {
                dateDebut.setValue(new java.sql.Date(conge.getDateDebut().getTime()).toLocalDate());
            }
            if (conge.getDateFin() != null) {
                dateFin.setValue(new java.sql.Date(conge.getDateFin().getTime()).toLocalDate());
            }
            txtMotif.setText(conge.getMotif());

            // Select correct type in ComboBox
            for (TypeCongeOption opt : comboTypeConge.getItems()) {
                if (opt.type == conge.getType()) {
                    comboTypeConge.getSelectionModel().select(opt);
                    break;
                }
            }
        }
    }

    @FXML
    public void handleSubmit() {
        Employe emp = EmployeeDashboardController.getCurrentEmploye();
        if (emp == null)
            return;

        // Validation
        TypeCongeOption selectedOption = comboTypeConge.getSelectionModel().getSelectedItem();
        if (selectedOption == null) {
            showAlert("Erreur", "Veuillez sélectionner un type de congé.");
            return;
        }

        if (dateDebut.getValue() == null || dateFin.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner les dates de début et de fin.");
            return;
        }

        if (dateFin.getValue().isBefore(dateDebut.getValue())) {
            showAlert("Erreur", "La date de fin doit être postérieure à la date de début.");
            return;
        }

        if (txtMotif.getText() == null || txtMotif.getText().trim().isEmpty()) {
            showAlert("Erreur", "Veuillez saisir un motif.");
            return;
        }

        // Convert Dates
        Date start = Date.from(dateDebut.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(dateFin.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Create or Update Request
        Conge conge = (congeToEdit != null) ? congeToEdit : new Conge();
        conge.setEmployeeId(emp.getId());
        conge.setType(selectedOption.type);
        conge.setDateDebut(start);
        conge.setDateFin(end);
        conge.setMotif(txtMotif.getText().trim());

        // Reset status if editing? Usually yes, but keep it PENDING if it was already
        // PENDING.
        // If it was REFUSED, maybe set back to PENDING for re-evaluation?
        // For now, force Pending on new/edit to be safe.
        conge.setStatut(Conge.StatutConge.EN_ATTENTE);
        // Note: Resetting dateDemande to now might be appropriate for an edit too
        conge.setDateDemande(new Date());

        try {
            congeService.saveConge(conge);
            System.out
                    .println("✅ Demande de congé " + (congeToEdit != null ? "modifiée" : "soumise") + " avec succès !");
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de soumettre la demande : " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtMotif.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Helper class for ComboBox items
    public static class TypeCongeOption {
        TypeConge type;
        String label;

        public TypeCongeOption(TypeConge type, String label) {
            this.type = type;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
