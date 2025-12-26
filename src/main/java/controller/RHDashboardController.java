package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class RHDashboardController {

    @FXML
    private Pane pieChartContainer;

    @FXML
    private Pane barChartContainer;

    @FXML
    public void initialize() {
        setupPieChart();
        setupBarChart();
    }

    // ================= PIE CHART =================
    private void setupPieChart() {
        PieChart pieChart = new PieChart();
        pieChart.getData().addAll(
                new PieChart.Data("IT", 40),
                new PieChart.Data("RH", 25),
                new PieChart.Data("Marketing", 20),
                new PieChart.Data("Finance", 15)
        );

        pieChart.prefWidthProperty().bind(pieChartContainer.widthProperty());
        pieChart.prefHeightProperty().bind(pieChartContainer.heightProperty());

        pieChartContainer.getChildren().clear();
        pieChartContainer.getChildren().add(pieChart);
    }

    // ================= BAR CHART =================
    private void setupBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        xAxis.setLabel("Mois");
        yAxis.setLabel("Congés");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Congés");

        series.getData().addAll(
                new XYChart.Data<>("Jan", 3),
                new XYChart.Data<>("Feb", 5),
                new XYChart.Data<>("Mar", 8),
                new XYChart.Data<>("Apr", 6),
                new XYChart.Data<>("May", 10)
        );

        barChart.getData().add(series);

        barChart.prefWidthProperty().bind(barChartContainer.widthProperty());
        barChart.prefHeightProperty().bind(barChartContainer.heightProperty());

        barChartContainer.getChildren().clear();
        barChartContainer.getChildren().add(barChart);
    }

    // ================= NAVIGATION =================
    @FXML
    private void ouvrirEmployes() throws Exception {
    	 System.out.println("CLICK EMPLOYES OK"); 
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/RH_Employe.fxml")
        );
        Parent root = loader.load();

        Stage stage = (Stage) pieChartContainer.getScene().getWindow();
        stage.setScene(new Scene(root, 1200, 800));
    }
}
