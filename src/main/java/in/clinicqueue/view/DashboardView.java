package in.clinicqueue.view;

import in.clinicqueue.model.DashboardSummary;
import in.clinicqueue.model.DoctorQueueSummary;
import in.clinicqueue.service.DashboardService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardView extends VBox {
    private final DashboardService dashboardService;
    private final Label lblTotal = new Label("0"), lblWaiting = new Label("0"), lblInConsultation = new Label("0"), lblCompleted = new Label("0");
    private final FlowPane doctorCardsPane = new FlowPane();

    public DashboardView(DashboardService dashboardService, Runnable navigateToRegister, Runnable navigateToQueue) {
        this.dashboardService = dashboardService;
        setSpacing(20); setPadding(new Insets(24));

        HBox header = new HBox(16); header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Operational Overview"); title.getStyleClass().add("section-heading");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnReg = new Button("+ Register Patient"), btnQ = new Button("View Full Queue");
        btnReg.getStyleClass().add("btn-primary"); btnReg.setOnAction(e -> navigateToRegister.run());
        btnQ.getStyleClass().add("btn-secondary"); btnQ.setOnAction(e -> navigateToQueue.run());
        header.getChildren().addAll(title, spacer, btnReg, btnQ);

        GridPane grid = new GridPane(); grid.setHgap(16); grid.setVgap(16);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints c = new ColumnConstraints(); c.setPercentWidth(25); grid.getColumnConstraints().add(c);
        }
        grid.add(metricCard("TOTAL REGISTERED", lblTotal, "metric-badge-blue"), 0, 0);
        grid.add(metricCard("WAITING IN QUEUE", lblWaiting, "metric-badge-amber"), 1, 0);
        grid.add(metricCard("IN CONSULTATION", lblInConsultation, "metric-badge-blue"), 2, 0);
        grid.add(metricCard("COMPLETED VISITS", lblCompleted, "metric-badge-green"), 3, 0);

        VBox docBox = new VBox(12); docBox.getStyleClass().add("card-panel");
        Label docTitle = new Label("Doctor Queue Status"); docTitle.getStyleClass().add("section-heading");
        doctorCardsPane.setHgap(16); doctorCardsPane.setVgap(16);
        docBox.getChildren().addAll(docTitle, doctorCardsPane);

        getChildren().addAll(header, grid, docBox);
        refreshData();
    }

    private VBox metricCard(String title, Label val, String colorClass) {
        VBox card = new VBox(8); card.getStyleClass().add("metric-card");
        Label l = new Label(title); l.getStyleClass().add("metric-title");
        val.getStyleClass().addAll("metric-value", colorClass);
        card.getChildren().addAll(l, val);
        return card;
    }

    public void refreshData() {
        try {
            DashboardSummary s = dashboardService.getDashboardSummary(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            lblTotal.setText(String.valueOf(s.getTotalVisits()));
            lblWaiting.setText(String.valueOf(s.getWaitingCount()));
            lblInConsultation.setText(String.valueOf(s.getInConsultationCount()));
            lblCompleted.setText(String.valueOf(s.getCompletedCount()));

            doctorCardsPane.getChildren().clear();
            for (DoctorQueueSummary doc : s.getDoctorSummaries()) {
                VBox c = new VBox(8); c.getStyleClass().add("metric-card"); c.setPrefWidth(280);
                Label name = new Label(doc.getDoctorName()); name.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
                Label dept = new Label(doc.getDepartmentName()); dept.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
                Label wait = new Label("Waiting: " + doc.getWaitingCount() + " patients"); wait.setStyle("-fx-font-weight: bold; -fx-text-fill: #d97706;");
                Label active = new Label("Active: " + doc.getActivePatientToken() + " — " + doc.getActivePatientName()); active.setStyle("-fx-text-fill: #1d4ed8; -fx-font-weight: 600; -fx-font-size: 12px;");
                c.getChildren().addAll(name, dept, wait, active);
                doctorCardsPane.getChildren().add(c);
            }
        } catch (SQLException e) {
            System.err.println("Dashboard refresh error: " + e.getMessage());
        }
    }
}
