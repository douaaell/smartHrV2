package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import java.util.Optional;
import java.util.List;
import model.Employe;
import service.EmployeService;
import service.ContratService;
import service.CongeService;
import model.Conge;
import model.Contrat;

public class RHDashboardController {

    @FXML
    private Pane pieChartContainer;

    @FXML
    private BorderPane mainContentArea;

    @FXML
    private VBox recentLeavesList;

    @FXML
    private VBox recentContractsList;

    @FXML
    private Label lblAdminEmail;

    @FXML
    private Label lblPageTitle;

    @FXML
    private Label lblPageSubtitle;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Button employesBtn;

    @FXML
    private Button contratsBtn;

    @FXML
    private Button congesBtn;

    @FXML
    private Button justificatifsBtn;

    @FXML
    private Button importExportBtn;

    // Stats labels
    @FXML
    private Label lblTotalEmployes;

    @FXML
    private Label lblContratsActifs;

    @FXML
    private Label lblCongesAttente;

    @FXML
    private Label lblContratsExpirant;

    @FXML
    private Label lblAdminName;

    private List<Button> navButtons;

    // Prevent double-clicks
    private boolean isProcessing = false;

    // Store original dashboard content
    private javafx.scene.Node originalDashboardContent;

    private static Employe currentRH; // Current logged-in RH account

    @FXML
    public void initialize() {
        try {
            // Setup navigation list
            navButtons = java.util.Arrays.asList(
                    dashboardBtn, employesBtn, contratsBtn, congesBtn, justificatifsBtn, importExportBtn);

            // Set dashboard as active initially
            setActiveButton(dashboardBtn);

            // Save original dashboard view (center content)
            if (mainContentArea != null) {
                originalDashboardContent = mainContentArea.getCenter();
            }

            // Update admin info labels from session
            updatUserInfo();

            // Update statistics from database
            updateStatistics();

            // Update recent activities
            updateRecentActivities();

            // Setup chart
            setupPieChart();

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'initialisation du dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setCurrentRH(Employe rh) {
        currentRH = rh;
    }

    public static Employe getCurrentRH() {
        return currentRH;
    }

    private void updatUserInfo() {
        if (currentRH != null) {
            if (lblAdminName != null)
                lblAdminName.setText(currentRH.getPrenom() + " " + currentRH.getNom());
            if (lblAdminEmail != null)
                lblAdminEmail.setText(currentRH.getEmail());
        } else {
            // Default mock if no session (for direct FXML preview/testing)
            if (lblAdminName != null)
                lblAdminName.setText("Administrateur RH");
            if (lblAdminEmail != null)
                lblAdminEmail.setText("rh@smarthr.com");
        }
    }

    private void setActiveButton(Button activeBtn) {
        if (navButtons == null)
            return;
        for (Button btn : navButtons) {
            if (btn != null) {
                btn.getStyleClass().remove("active");
            }
        }
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("active");
        }
    }

    // ================= STATISTICS =================
    private void updateStatistics() {
        try {
            EmployeService employeService = new EmployeService();
            ContratService contratService = new ContratService();
            CongeService congeService = new CongeService();

            // Get total employees from database
            List<Employe> employees = employeService.listerEmployes();
            int totalEmployes = employees.size();

            // Get active contracts count
            List<Contrat> allContrats = contratService.listerContrats();
            long contratsActifs = allContrats.stream()
                    .filter(c -> {
                        java.time.LocalDate now = java.time.LocalDate.now();
                        boolean started = !c.getStartDate().isAfter(now);
                        boolean notEnded = c.getEndDate() == null || !c.getEndDate().isBefore(now);
                        return started && notEnded;
                    })
                    .count();

            // Get pending leaves count
            List<Conge> allConges = congeService.findAllConges();
            long congesEnAttente = allConges.stream()
                    .filter(c -> c.getStatut() == Conge.StatutConge.EN_ATTENTE)
                    .count();

            // For "Candidatures nouvelles", we'll use 0 for now (no candidature table yet)
            int candidaturesNouvelles = 0;

            // Update the labels with null checks
            if (lblTotalEmployes != null) {
                lblTotalEmployes.setText(String.valueOf(totalEmployes));
            }
            if (lblContratsActifs != null) {
                lblContratsActifs.setText(String.valueOf(contratsActifs));
            }
            if (lblCongesAttente != null) {
                lblCongesAttente.setText(String.valueOf(congesEnAttente));
            }
            if (lblContratsExpirant != null) {
                lblContratsExpirant.setText(String.valueOf(candidaturesNouvelles));
            }

            System.out.println("📊 Statistiques mises à jour - " + totalEmployes + " employés, " +
                    contratsActifs + " contrats actifs, " + congesEnAttente + " congés en attente");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour des statistiques: " + e.getMessage());
            e.printStackTrace();

            // Set default values on error
            if (lblTotalEmployes != null) {
                lblTotalEmployes.setText("0");
            }
            if (lblContratsActifs != null) {
                lblContratsActifs.setText("0");
            }
            if (lblCongesAttente != null) {
                lblCongesAttente.setText("0");
            }
            if (lblContratsExpirant != null) {
                lblContratsExpirant.setText("0");
            }
        }
    }

    // ================= PIE CHART =================
    private void setupPieChart() {
        try {
            if (pieChartContainer == null)
                return;

            System.out.println("📊 Synchronisation du graphique circulaire...");

            EmployeService employeService = new EmployeService();
            List<Employe> allEmployees = employeService.listerEmployes();

            // Compter les employés par département
            java.util.Map<String, Long> deptCounts = allEmployees.stream()
                    .filter(e -> e.getDepartement() != null && !e.getDepartement().isEmpty())
                    .collect(java.util.stream.Collectors.groupingBy(
                            Employe::getDepartement,
                            java.util.stream.Collectors.counting()));

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            deptCounts.forEach((dept, count) -> {
                pieChartData.add(new PieChart.Data(dept, count));
            });

            if (pieChartData.isEmpty()) {
                pieChartData.add(new PieChart.Data("Aucune donnée", 1));
            }

            PieChart pieChart = new PieChart(pieChartData);
            pieChart.setLegendVisible(true);
            pieChart.setLabelsVisible(true);

            pieChart.prefWidthProperty().bind(pieChartContainer.widthProperty());
            pieChart.prefHeightProperty().bind(pieChartContainer.heightProperty());

            pieChartContainer.getChildren().clear();
            pieChartContainer.getChildren().add(pieChart);

            System.out.println("✅ Graphique circulaire synchronisé (" + deptCounts.size() + " départements)");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la synchronisation du graphique: " + e.getMessage());
        }
    }

    // ================= RECENT ACTIVITIES =================
    private void updateRecentActivities() {
        try {
            EmployeService employeService = new EmployeService();
            CongeService congeService = new CongeService();
            ContratService contratService = new ContratService();

            // 1. Populate Recent Leaves
            if (recentLeavesList != null) {
                recentLeavesList.getChildren().clear();
                List<model.Conge> allConges = congeService.findAllConges();
                // Get last 3 (most recent first)
                java.util.Collections.reverse(allConges);
                List<model.Conge> recentConges = allConges.stream().limit(3)
                        .collect(java.util.stream.Collectors.toList());

                for (model.Conge conge : recentConges) {
                    model.Employe emp = employeService.findEmployeeById(conge.getEmployeeId());
                    String name = (emp != null) ? emp.getPrenom() + " " + emp.getNom() : "ID: " + conge.getEmployeeId();
                    String details = conge.getType() + " - " +
                            (conge.getDateDebut() != null
                                    ? new java.text.SimpleDateFormat("dd/MM").format(conge.getDateDebut())
                                    : "??")
                            + " au " +
                            (conge.getDateFin() != null
                                    ? new java.text.SimpleDateFormat("dd/MM").format(conge.getDateFin())
                                    : "??");

                    recentLeavesList.getChildren().add(createRecentItem(name, details, () -> handleConges()));
                }

                if (recentConges.isEmpty()) {
                    recentLeavesList.getChildren().add(new Label("Aucune demande récente"));
                }
            }

            // 2. Populate Recent Contracts
            if (recentContractsList != null) {
                recentContractsList.getChildren().clear();
                List<model.Contrat> allContrats = contratService.listerContrats();
                // Get last 3
                java.util.Collections.reverse(allContrats);
                List<model.Contrat> recentContrats = allContrats.stream().limit(3)
                        .collect(java.util.stream.Collectors.toList());

                for (model.Contrat contrat : recentContrats) {
                    model.Employe emp = employeService.findEmployeeByEmail(contrat.getEmployeeEmail());
                    String name = (emp != null) ? emp.getPrenom() + " " + emp.getNom() : contrat.getEmployeeEmail();
                    String details = contrat.getType() + " - " + contrat.getPosition();

                    recentContractsList.getChildren().add(createRecentItem(name, details, () -> handleContrats()));
                }

                if (recentContrats.isEmpty()) {
                    recentContractsList.getChildren().add(new Label("Aucun contrat récent"));
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour des activités récentes: " + e.getMessage());
        }
    }

    private HBox createRecentItem(String name, String details, Runnable onAction) {
        HBox item = new HBox(10);
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        item.getStyleClass().add("recent-item");

        VBox texts = new VBox(3);
        Label lblName = new Label(name);
        lblName.getStyleClass().add("item-name");
        Label lblDetails = new Label(details);
        lblDetails.getStyleClass().add("item-details");
        texts.getChildren().addAll(lblName, lblDetails);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDetails = new Button("Détails");
        btnDetails.getStyleClass().add("detail-btn");
        btnDetails.setOnAction(e -> onAction.run());

        item.getChildren().addAll(texts, spacer, btnDetails);
        return item;
    }

    @FXML
    public void handleDashboard() {
        try {
            System.out.println("📊 Dashboard RH sélectionné");
            setActiveButton(dashboardBtn);

            if (lblPageTitle != null)
                lblPageTitle.setText("Tableau de bord");
            if (lblPageSubtitle != null)
                lblPageSubtitle.setText("Vue d'ensemble de la plateforme SmartHR+");

            // Restore original dashboard view
            if (originalDashboardContent != null) {
                mainContentArea.setCenter(originalDashboardContent);
            }

            // Refresh statistics
            updateStatistics();

            // Refresh recent activities
            updateRecentActivities();

            // Refresh chart
            setupPieChart();

            System.out.println("✅ Dashboard RH affiché");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'affichage du dashboard RH: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleContrats() {
        try {
            System.out.println("📄 Contrats sélectionné");
            setActiveButton(contratsBtn);

            if (lblPageTitle != null)
                lblPageTitle.setText("Gestion des Contrats");
            if (lblPageSubtitle != null)
                lblPageSubtitle.setText("Suivi des contrats, salaires et types d'emploi");

            // Load contracts view
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/RH_Contrat_Content.fxml"));
            Parent contractsView = loader.load();

            // Replace content
            mainContentArea.setCenter(contractsView);

            System.out.println("✅ Vue contrats affichée");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'affichage des contrats: " + e.getMessage());
            e.printStackTrace();

            // Fallback to placeholder
            VBox contractsContent = new VBox(20);
            contractsContent.getStyleClass().add("main-content");
            Label headerLabel = new Label("Gestion des Contrats");
            headerLabel.getStyleClass().add("header-title");
            contractsContent.getChildren().add(headerLabel);
            Label placeholder = new Label("Erreur de chargement du module contrats...");
            placeholder.getStyleClass().add("header-subtitle");
            contractsContent.getChildren().add(placeholder);
            mainContentArea.setCenter(contractsContent);
        }
    }

    @FXML
    public void handleConges() {
        try {
            System.out.println("🏖️ Congés sélectionné");
            setActiveButton(congesBtn);

            if (lblPageTitle != null)
                lblPageTitle.setText("Gestion des Congés");
            if (lblPageSubtitle != null)
                lblPageSubtitle.setText("Validation et suivi des demandes de congés");

            // Load leave view
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/RH_Conge_Content.fxml"));
            Parent leaveView = loader.load();

            // Replace content
            mainContentArea.setCenter(leaveView);

            System.out.println("✅ Vue congés affichée");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'affichage des congés: " + e.getMessage());
            e.printStackTrace();

            // Fallback to placeholder
            VBox leaveContent = new VBox(20);
            leaveContent.getStyleClass().add("main-content");
            Label headerLabel = new Label("Gestion des Congés");
            headerLabel.getStyleClass().add("header-title");
            leaveContent.getChildren().add(headerLabel);
            Label placeholder = new Label("Erreur de chargement du module congés...");
            placeholder.getStyleClass().add("header-subtitle");
            leaveContent.getChildren().add(placeholder);
            mainContentArea.setCenter(leaveContent);
        }
    }

    @FXML
    public void handleJustificatifs() {
        try {
            System.out.println("📄 Justificatifs sélectionné");
            setActiveButton(justificatifsBtn);

            if (lblPageTitle != null)
                lblPageTitle.setText("Justificatifs");
            if (lblPageSubtitle != null)
                lblPageSubtitle.setText("Gestion des documents et justificatifs d'absence");

            // Load justifications view
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/RH_Justificatifs_Content.fxml"));
            Parent justificatifsView = loader.load();

            // Replace content
            mainContentArea.setCenter(justificatifsView);

            System.out.println("✅ Vue justificatifs affichée");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'affichage des justificatifs: " + e.getMessage());
            e.printStackTrace();

            // Fallback to placeholder
            VBox justifContent = new VBox(20);
            justifContent.getStyleClass().add("main-content");
            Label headerLabel = new Label("Gestion des Justificatifs");
            headerLabel.getStyleClass().add("header-title");
            justifContent.getChildren().add(headerLabel);
            Label placeholder = new Label("Erreur de chargement du module justificatifs...");
            placeholder.getStyleClass().add("header-subtitle");
            justifContent.getChildren().add(placeholder);
            mainContentArea.setCenter(justifContent);
        }
    }

    @FXML
    public void handleParametres() {
        if (isProcessing) {
            System.out.println("⚠️ Action déjà en cours, ignorée...");
            return;
        }

        try {
            isProcessing = true;
            System.out.println("⚙️ Paramètres sélectionné");

            // Create settings view
            VBox settingsContent = new VBox(20);
            settingsContent.getStyleClass().add("main-content");

            // Header
            Label headerLabel = new Label("Paramètres de l'Application");
            headerLabel.getStyleClass().add("header-title");
            settingsContent.getChildren().add(headerLabel);

            // Placeholder content
            Label placeholder = new Label("Module Paramètres en cours de développement...");
            placeholder.getStyleClass().add("header-subtitle");
            settingsContent.getChildren().add(placeholder);

            // Replace content
            mainContentArea.setCenter(settingsContent);

            System.out.println("✅ Vue paramètres affichée");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'affichage des paramètres: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isProcessing = false;
        }
    }

    @FXML
    public void ouvrirEmployes() throws Exception {
        try {
            System.out.println("👥 Gestion des employés sélectionnée");
            setActiveButton(employesBtn);

            if (lblPageTitle != null)
                lblPageTitle.setText("Gestion des Employés");
            if (lblPageSubtitle != null)
                lblPageSubtitle.setText("Gérer les informations et le personnel de l'entreprise");

            // Load the employee view (content only, no sidebar)
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/RH_Employe_Content.fxml"));
            Parent employeeView = loader.load();

            // Replace the content
            mainContentArea.setCenter(employeeView);

            System.out.println("✅ Vue employés affichée avec succès");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'affichage des employés: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRecrutement() {
        if (isProcessing) {
            System.out.println("⚠️ Action déjà en cours, ignorée...");
            return;
        }

        try {
            isProcessing = true;
            System.out.println("🎯 Recrutement sélectionné");

            // Load recruitment view
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/RH_Recrutement_Content.fxml"));
            Parent recruitmentView = loader.load();

            // Replace content
            mainContentArea.setCenter(recruitmentView);

            System.out.println("✅ Vue recrutement affichée");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'affichage du recrutement: " + e.getMessage());
            e.printStackTrace();

            // Fallback content
            VBox placeholder = new VBox(20);
            placeholder.getStyleClass().add("main-content");
            placeholder.getChildren().add(new Label("Gestion du Recrutement"));
            placeholder.getChildren().add(new Label("Erreur de chargement de la vue."));
            mainContentArea.setCenter(placeholder);
        } finally {
            isProcessing = false;
        }
    }

    @FXML
    public void handleImportExport() {
        try {
            System.out.println("📂 Import/Export sélectionné");
            setActiveButton(importExportBtn);

            if (lblPageTitle != null)
                lblPageTitle.setText("Import / Export CSV");
            if (lblPageSubtitle != null)
                lblPageSubtitle.setText("Synchronisation et gestion des données massives");

            // Load Import/Export view
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/RH_ImportExport_Content.fxml"));
            Parent importExportView = loader.load();

            // Replace content
            mainContentArea.setCenter(importExportView);

            System.out.println("✅ Vue Import/Export affichée");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'affichage Import/Export: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout() {
        try {
            System.out.println("🔓 Déconnexion en cours...");

            // Show confirmation dialog
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Déconnexion");
            confirmation.setHeaderText("Quitter SmartRH");
            confirmation.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

            Optional<ButtonType> result = confirmation.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Clear session data
                currentRH = null;

                // Close current window
                Stage currentStage = (Stage) mainContentArea.getScene().getWindow();
                currentStage.close();

                // Open login screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
                Parent root = loader.load();

                Stage loginStage = new Stage();
                loginStage.setTitle("SmartRH - Connexion");
                loginStage.setScene(new Scene(root, 400, 500));
                loginStage.show();

                System.out.println("✅ Déconnexion réussie - Retour à la page de connexion");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la déconnexion: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de déconnexion");
            alert.setContentText("Une erreur est survenue lors de la déconnexion: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
