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

/**
 * Dashboard View displaying today's overall clinic statistics and per-doctor queue summaries.
 */
public class DashboardView extends VBox {

    private final DashboardService dashboardService;
    private final Runnable navigateToRegister;
    private final Runnable navigateToQueue;

    // Metric Labels
    private final Label lblTotal = new Label("0");
    private final Label lblWaiting = new Label("0");
    private final Label lblInConsultation = new Label("0");
    private final Label lblCompleted = new Label("0");

    // Container for dynamic doctor summary cards
    private final FlowPane doctorCardsPane = new FlowPane();

    public DashboardView(DashboardService dashboardService, Runnable navigateToRegister, Runnable navigateToQueue) {
        this.dashboardService = dashboardService;
        this.navigateToRegister = navigateToRegister;
        this.navigateToQueue = navigateToQueue;

        setSpacing(24);
        setPadding(new Insets(24));
        getStyleClass().add("dashboard-view");

        buildHeader();
        buildMetricsRow();
        buildDoctorSection();
        refreshData();
    }

    private void buildHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label heading = new Label("Operational Overview");
        heading.getStyleClass().add("section-heading");
        Label subheading = new Label("Live outpatient queue status for today: " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        subheading.getStyleClass().add("section-subheading");
        titleBox.getChildren().addAll(heading, subheading);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnQuickRegister = new Button("+ Register Patient");
        btnQuickRegister.getStyleClass().add("btn-primary");
        btnQuickRegister.setOnAction(e -> navigateToRegister.run());

        Button btnViewQueue = new Button("View Full Queue");
        btnViewQueue.getStyleClass().add("btn-secondary");
        btnViewQueue.setOnAction(e -> navigateToQueue.run());

        header.getChildren().addAll(titleBox, spacer, btnQuickRegister, btnViewQueue);
        getChildren().add(header);
    }

    private void buildMetricsRow() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        // Configure 4 equal columns
        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            grid.getColumnConstraints().add(col);
        }

        grid.add(createMetricCard("TOTAL REGISTERED", lblTotal, "metric-badge-blue"), 0, 0);
        grid.add(createMetricCard("WAITING IN QUEUE", lblWaiting, "metric-badge-amber"), 1, 0);
        grid.add(createMetricCard("IN CONSULTATION", lblInConsultation, "metric-badge-blue"), 2, 0);
        grid.add(createMetricCard("COMPLETED VISITS", lblCompleted, "metric-badge-green"), 3, 0);

        getChildren().add(grid);
    }

    private VBox createMetricCard(String title, Label valueLabel, String colorClass) {
        VBox card = new VBox(8);
        card.getStyleClass().add("metric-card");

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("metric-title");

        valueLabel.getStyleClass().addAll("metric-value", colorClass);

        card.getChildren().addAll(lblTitle, valueLabel);
        return card;
    }

    private void buildDoctorSection() {
        VBox container = new VBox(16);
        container.getStyleClass().add("card-panel");

        Label sectionTitle = new Label("Doctor Queue Status");
        sectionTitle.getStyleClass().add("section-heading");
        Label sectionSubtitle = new Label("Current waiting patients and active consultation per consulting doctor");
        sectionSubtitle.getStyleClass().add("section-subheading");

        doctorCardsPane.setHgap(16);
        doctorCardsPane.setVgap(16);

        container.getChildren().addAll(sectionTitle, sectionSubtitle, doctorCardsPane);
        getChildren().add(container);
    }

    public void refreshData() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        try {
            DashboardSummary summary = dashboardService.getDashboardSummary(today);
            lblTotal.setText(String.valueOf(summary.getTotalVisits()));
            lblWaiting.setText(String.valueOf(summary.getWaitingCount()));
            lblInConsultation.setText(String.valueOf(summary.getInConsultationCount()));
            lblCompleted.setText(String.valueOf(summary.getCompletedCount()));

            doctorCardsPane.getChildren().clear();
            for (DoctorQueueSummary doc : summary.getDoctorSummaries()) {
                doctorCardsPane.getChildren().add(createDoctorCard(doc));
            }
        } catch (SQLException e) {
            System.err.println("Failed to refresh dashboard: " + e.getMessage());
        }
    }

    private VBox createDoctorCard(DoctorQueueSummary doc) {
        VBox card = new VBox(10);
        card.getStyleClass().add("metric-card");
        card.setPrefWidth(280);

        Label name = new Label(doc.getDoctorName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #0f172a;");

        Label dept = new Label(doc.getDepartmentName());
        dept.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        HBox waitingBox = new HBox(8);
        waitingBox.setAlignment(Pos.CENTER_LEFT);
        Label waitingTag = new Label("Waiting:");
        waitingTag.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");
        Label waitingVal = new Label(doc.getWaitingCount() + " patients");
        waitingVal.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #d97706;");
        waitingBox.getChildren().addAll(waitingTag, waitingVal);

        VBox activeBox = new VBox(4);
        activeBox.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 8px; -fx-background-radius: 6px;");
        Label activeTitle = new Label("Active Consultation:");
        activeTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        Label activeInfo = new Label(doc.getActivePatientToken() + " — " + doc.getActivePatientName());
        activeInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #1d4ed8; -fx-font-weight: 600;");
        activeBox.getChildren().addAll(activeTitle, activeInfo);

        card.getChildren().addAll(name, dept, waitingBox, activeBox);
        return card;
    }
}
