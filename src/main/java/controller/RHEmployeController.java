package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import model.Employe;
import service.EmployeService;

public class RHEmployeController {

    @FXML private TableView<Employe> tableEmployes;

    @FXML private TableColumn<Employe, Integer> colId;
    @FXML private TableColumn<Employe, String> colNom;
    @FXML private TableColumn<Employe, String> colPrenom;
    @FXML private TableColumn<Employe, String> colEmail;
    @FXML private TableColumn<Employe, String> colDepartement;
    @FXML private TableColumn<Employe, String> colPosition;
    @FXML private TableColumn<Employe, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> departementFilter;

    private final EmployeService service = new EmployeService();
    private final ObservableList<Employe> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()).asObject());
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));
        colPrenom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPrenom()));
        colEmail.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));
        colDepartement.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDepartement()));
        colPosition.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPosition()));

        ajouterBoutonsActions();
        chargerEmployes();
    }

    private void ajouterBoutonsActions() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");

            {
                btnDelete.setOnAction(e -> {
                    Employe emp = getTableView().getItems().get(getIndex());
                    service.supprimerEmploye(emp.getId());
                    chargerEmployes();
                });

                btnEdit.setOnAction(e -> {
                    Employe emp = getTableView().getItems().get(getIndex());
                    System.out.println("Modifier : " + emp);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(5, btnEdit, btnDelete));
                }
            }
        });
    }

    @FXML
    public void chargerEmployes() {
        data.setAll(service.listerEmployes());
        tableEmployes.setItems(data);
    }

    @FXML
    public void rechercher() {
        String keyword = searchField.getText().toLowerCase();
        tableEmployes.setItems(
                data.filtered(e ->
                        e.getNom().toLowerCase().contains(keyword)
                     || e.getPrenom().toLowerCase().contains(keyword))
        );
    }

    @FXML
    public void ajouterEmploye() {
        System.out.println("Ouvrir formulaire d'ajout employé");
    }
}
