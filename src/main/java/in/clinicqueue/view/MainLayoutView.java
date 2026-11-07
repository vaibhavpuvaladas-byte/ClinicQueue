package in.clinicqueue.view;

import in.clinicqueue.database.SchemaInitializer;
import in.clinicqueue.repository.DepartmentRepository;
import in.clinicqueue.repository.DoctorRepository;
import in.clinicqueue.repository.VisitRepository;
import in.clinicqueue.service.DashboardService;
import in.clinicqueue.service.QueueService;
import in.clinicqueue.service.VisitService;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Main Layout containing the persistent top header, navigation bar,
 * central content switcher, and bottom status bar.
 */
public class MainLayoutView extends BorderPane {

    private final Button btnNavDashboard = new Button("📊 Dashboard");
    private final Button btnNavRegister = new Button("➕ Register Patient");
    private final Button btnNavQueue = new Button("📋 Doctor Queue Board");
    private final Button btnReset = new Button("🔄 Reset Demo Data");

    private final StackPane contentArea = new StackPane();

    private final DashboardView dashboardView;
    private final RegisterVisitView registerVisitView;
    private final QueueBoardView queueBoardView;

    public MainLayoutView() {
        // Instantiate repositories and services
        DepartmentRepository departmentRepository = new DepartmentRepository();
        DoctorRepository doctorRepository = new DoctorRepository();
        VisitRepository visitRepository = new VisitRepository();

        VisitService visitService = new VisitService();
        QueueService queueService = new QueueService(visitRepository);
        DashboardService dashboardService = new DashboardService(visitRepository, doctorRepository);

        // Views
        this.dashboardView = new DashboardView(dashboardService, this::showRegisterView, this::showQueueView);
        this.registerVisitView = new RegisterVisitView(visitService, departmentRepository, doctorRepository, this::showQueueView);
        this.queueBoardView = new QueueBoardView(queueService, visitRepository, doctorRepository);

        buildTop();
        setCenter(contentArea);
        buildBottom();

        // Default to Dashboard
        showDashboardView();
    }

    private void buildTop() {
        VBox topContainer = new VBox();

        // 1. Header Bar
        HBox headerBar = new HBox(16);
        headerBar.getStyleClass().add("header-bar");
        headerBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label appTitle = new Label("🏥 CLINICQUEUE");
        appTitle.getStyleClass().add("app-title");
        Label appSubtitle = new Label("Outpatient Queue Management System (MVP)");
        appSubtitle.getStyleClass().add("app-subtitle");
        titleBox.getChildren().addAll(appTitle, appSubtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLabel = new Label("📅 Today: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        dateLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: 600; -fx-font-size: 13px;");

        headerBar.getChildren().addAll(titleBox, spacer, dateLabel);

        // 2. Navigation Bar
        HBox navBar = new HBox(12);
        navBar.getStyleClass().add("nav-bar");
        navBar.setAlignment(Pos.CENTER_LEFT);

        btnNavDashboard.getStyleClass().add("nav-button");
        btnNavDashboard.setOnAction(e -> showDashboardView());

        btnNavRegister.getStyleClass().add("nav-button");
        btnNavRegister.setOnAction(e -> showRegisterView());

        btnNavQueue.getStyleClass().add("nav-button");
        btnNavQueue.setOnAction(e -> showQueueView());

        Region navSpacer = new Region();
        HBox.setHgrow(navSpacer, Priority.ALWAYS);

        btnReset.getStyleClass().add("reset-button");
        btnReset.setOnAction(e -> handleResetDemoData());

        navBar.getChildren().addAll(btnNavDashboard, btnNavRegister, btnNavQueue, navSpacer, btnReset);

        topContainer.getChildren().addAll(headerBar, navBar);
        setTop(topContainer);
    }

    private void buildBottom() {
        HBox statusBar = new HBox(20);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setAlignment(Pos.CENTER_LEFT);

        Label dbStatus = new Label("🟢 Database: SQLite (clinicqueue.db) | Foreign Keys: ON");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label modeStatus = new Label("Single-Workstation Presentation MVP • Fictional Demo Data");

        statusBar.getChildren().addAll(dbStatus, spacer, modeStatus);
        setBottom(statusBar);
    }

    private void setNavActive(Button activeBtn) {
        btnNavDashboard.getStyleClass().remove("nav-button-active");
        btnNavRegister.getStyleClass().remove("nav-button-active");
        btnNavQueue.getStyleClass().remove("nav-button-active");
        if (!activeBtn.getStyleClass().contains("nav-button-active")) {
            activeBtn.getStyleClass().add("nav-button-active");
        }
    }

    public void showDashboardView() {
        setNavActive(btnNavDashboard);
        dashboardView.refreshData();
        contentArea.getChildren().setAll(dashboardView);
    }

    public void showRegisterView() {
        setNavActive(btnNavRegister);
        registerVisitView.resetForm();
        contentArea.getChildren().setAll(registerVisitView);
    }

    public void showQueueView() {
        setNavActive(btnNavQueue);
        queueBoardView.refreshQueue();
        contentArea.getChildren().setAll(queueBoardView);
    }

    private void handleResetDemoData() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Demo Data");
        confirm.setHeaderText("Reset to Clean Sample Dataset?");
        confirm.setContentText("Warning: This will clear existing test visits and reseed initial sample patients and queue tokens. Proceed?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                SchemaInitializer.resetAndReseedDemoData();
                showDashboardView();
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Reset Completed");
                info.setHeaderText(null);
                info.setContentText("Database has been reset and seeded with clean demo records.");
                info.showAndWait();
            } catch (SQLException ex) {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Reset Failed");
                err.setContentText("Failed to reset database: " + ex.getMessage());
                err.showAndWait();
            }
        }
    }
}
