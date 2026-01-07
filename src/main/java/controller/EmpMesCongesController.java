package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import model.Conge;
import model.Employe;
import service.CongeService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EmpMesCongesController {

    @FXML
    private Label lblCPRestants;
    @FXML
    private Label lblEnAttente;
    @FXML
    private TableView<Conge> tableHistory;
    @FXML
    private TableColumn<Conge, String> colType;
    @FXML
    private TableColumn<Conge, String> colPeriode;
    @FXML
    private TableColumn<Conge, Integer> colJours;
    @FXML
    private TableColumn<Conge, String> colMotif;
    @FXML
    private TableColumn<Conge, String> colStatut;
    @FXML
    private TableColumn<Conge, String> colDateDemande;
    @FXML
    private TableColumn<Conge, Void> colAction;

    private void handleEditConge(Conge conge) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeConge.fxml"));
            Parent root = loader.load();

            DemandeCongeController controller = loader.getController();
            controller.setCongeForEdit(conge);

            Stage stage = new Stage();
            stage.setTitle("Modifier ma demande de congé");
            stage.setScene(new Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh logic after edit
            Employe emp = EmployeeDashboardController.getCurrentEmploye();
            if (emp != null) {
                loadStats(emp.getId());
                loadHistory(emp.getId());
            }

        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'ouverture");
            alert.setContentText("Impossible d'ouvrir la fenêtre de modification : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void handleDeleteConge(Conge conge) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Annuler la demande");
        alert.setHeaderText("Supprimer cette demande de congé ?");
        alert.setContentText("Voulez-vous vraiment annuler votre demande ?");

        if (alert.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            try {
                congeService.deleteConge(conge.getId());
                // Refresh
                Employe emp = EmployeeDashboardController.getCurrentEmploye();
                if (emp != null) {
                    loadStats(emp.getId());
                    loadHistory(emp.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final CongeService congeService = new CongeService();

    @FXML
    public void initialize() {
        Employe emp = EmployeeDashboardController.getCurrentEmploye();
        if (emp != null) {
            loadStats(emp.getId());
            loadHistory(emp.getId());
        }
    }

    private void loadStats(int empId) {
        int remainingDocs = congeService.getRemainingVacationDays(empId);
        lblCPRestants.setText(String.valueOf(remainingDocs));

        List<Conge> all = congeService.findCongesByEmployeeId(empId);
        long pending = all.stream().filter(c -> c.getStatut() == Conge.StatutConge.EN_ATTENTE).count();
        lblEnAttente.setText(String.valueOf(pending));
    }

    private void loadHistory(int empId) {
        List<Conge> history = congeService.findCongesByEmployeeId(empId);
        ObservableList<Conge> data = FXCollections.observableArrayList(history);

        colType.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.setPadding(new javafx.geometry.Insets(4, 12, 4, 12));
                    lbl.setStyle(
                            "-fx-background-color: #E8F0FE; -fx-text-fill: #1967D2; -fx-background-radius: 15; -fx-font-weight: bold; -fx-font-size: 11px;");
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });
        colType.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType().getLibelle()));

        colPeriode.setCellValueFactory(cellData -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String start = sdf.format(cellData.getValue().getDateDebut());
            String end = sdf.format(cellData.getValue().getDateFin());
            return new javafx.beans.property.SimpleStringProperty("📅  " + start + "\n      au " + end);
        });

        colJours.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().calculerDuree()));
        colJours.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item + " jour(s)");
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });

        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colMotif.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setWrapText(true);
                }
            }
        });

        colStatut.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatut().getLibelle()));
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
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
                    setText(null);
                }
            }
        });

        colDateDemande.setCellValueFactory(cellData -> {
            Date dateDemande = cellData.getValue().getDateDemande();
            if (dateDemande != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                return new javafx.beans.property.SimpleStringProperty("🕒 " + sdf.format(dateDemande));
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        // Configure Actions Column
        colAction.setCellFactory(param -> new TableCell<>() {
            private final javafx.scene.control.Button btnEdit = new javafx.scene.control.Button("✎");
            private final javafx.scene.control.Button btnDelete = new javafx.scene.control.Button("🗑");

            {
                String iconStyle = "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0; -fx-font-size: 18px;";
                btnEdit.setStyle(iconStyle + "-fx-text-fill: #10B981;");
                btnDelete.setStyle(iconStyle + "-fx-text-fill: #EF4444;");

                btnEdit.setTooltip(new javafx.scene.control.Tooltip("Modifier la demande"));
                btnDelete.setTooltip(new javafx.scene.control.Tooltip("Annuler la demande"));

                btnEdit.setOnAction(e -> handleEditConge(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleDeleteConge(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Conge c = getTableView().getItems().get(getIndex());
                    boolean isPending = c.getStatut() == Conge.StatutConge.EN_ATTENTE;

                    btnEdit.setDisable(!isPending);
                    btnDelete.setDisable(!isPending);

                    if (!isPending) {
                        btnEdit.setStyle(btnEdit.getStyle() + "-fx-opacity: 0.5;");
                        btnDelete.setStyle(btnDelete.getStyle() + "-fx-opacity: 0.5;");
                    }

                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(12, btnEdit, btnDelete);
                    buttons.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        tableHistory.setItems(data);
        tableHistory.setStyle("-fx-background-color: white; -fx-padding: 10;");
    }

    @FXML
    public void handleNewRequest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemandeConge.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nouvelle demande de congé");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();

            // Refresh view after closing the modal
            initialize();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleExportCsv() {
        if (tableHistory.getItems().isEmpty()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Export CSV");
            alert.setHeaderText("Aucune donnée à exporter");
            alert.showAndWait();
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Exporter historique congés");
        fileChooser.setInitialFileName("mes_conges.csv");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));

        String userHome = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new java.io.File(userHome));

        java.io.File destFile = fileChooser.showSaveDialog(lblCPRestants.getScene().getWindow());

        if (destFile != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(destFile, "UTF-8")) {
                writer.println("Type;Période;Jours;Motif;Statut;Date demande");

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                for (Conge c : tableHistory.getItems()) {
                    String type = escapeCsv(c.getType().toString());
                    String periode = (c.getDateDebut() != null ? dateFormat.format(c.getDateDebut()) : "") + " au " +
                            (c.getDateFin() != null ? dateFormat.format(c.getDateFin()) : "");
                    String jours = c.calculerDuree() + " jours";
                    String motif = escapeCsv(c.getMotif());
                    String statut = c.getStatut().getLibelle();
                    String dateDemande = c.getDateDemande() != null ? dateTimeFormat.format(c.getDateDemande()) : "";

                    writer.println(
                            String.format("%s;%s;%s;%s;%s;%s", type, periode, jours, motif, statut, dateDemande));
                }

                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Export réussi");
                alert.setHeaderText(null);
                alert.setContentText("Historique exporté vers :\n" + destFile.getAbsolutePath());
                alert.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
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
