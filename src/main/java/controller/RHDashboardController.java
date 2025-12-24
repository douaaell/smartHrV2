package controller;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

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

    private void setupPieChart() {
        PieChart pieChart = new PieChart();
        pieChart.getData().add(new PieChart.Data("IT", 40));
        pieChart.getData().add(new PieChart.Data("RH", 25));
        pieChart.getData().add(new PieChart.Data("Marketing", 20));
        pieChart.getData().add(new PieChart.Data("Finance", 15));
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);
        pieChart.setMinSize(400, 300);

     // 🔥 CLÉ DE LA CORRECTION
        pieChart.prefWidthProperty().bind(pieChartContainer.widthProperty());
        pieChart.prefHeightProperty().bind(pieChartContainer.heightProperty());

        pieChartContainer.getChildren().clear();
        pieChartContainer.getChildren().add(pieChart);
    }

    private void setupBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        xAxis.setLabel("Mois");
        yAxis.setLabel("Congés");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Congés par mois");
        series.getData().add(new XYChart.Data<>("Jan", 3));
        series.getData().add(new XYChart.Data<>("Feb", 4));
        series.getData().add(new XYChart.Data<>("Mar", 6));
        series.getData().add(new XYChart.Data<>("Apr", 8));
        series.getData().add(new XYChart.Data<>("May", 12));
        series.getData().add(new XYChart.Data<>("Jun", 15));
        series.getData().add(new XYChart.Data<>("Jul", 18));
        series.getData().add(new XYChart.Data<>("Aug", 20));
        series.getData().add(new XYChart.Data<>("Sep", 15));
        series.getData().add(new XYChart.Data<>("Oct", 12));
        series.getData().add(new XYChart.Data<>("Nov", 8));
        series.getData().add(new XYChart.Data<>("Dec", 5));

        barChart.getData().add(series);
        barChart.prefWidthProperty().bind(barChartContainer.widthProperty());
        barChart.prefHeightProperty().bind(barChartContainer.heightProperty());
        barChart.setMinSize(500, 300);

        barChartContainer.getChildren().clear();
        barChartContainer.getChildren().add(barChart);
    }
}
