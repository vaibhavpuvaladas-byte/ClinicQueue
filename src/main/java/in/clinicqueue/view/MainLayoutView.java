package in.clinicqueue.view;

import in.clinicqueue.database.SchemaInitializer;
import in.clinicqueue.repository.*;
import in.clinicqueue.service.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class MainLayoutView extends BorderPane {
    private final Button btnDash = new Button("📊 Dashboard"), btnReg = new Button("➕ Register Patient"), btnQueue = new Button("📋 Doctor Queue Board"), btnReset = new Button("🔄 Reset Demo Data");
    private final StackPane contentArea = new StackPane();
    private final DashboardView dashboardView;
    private final RegisterVisitView registerVisitView;
    private final QueueBoardView queueBoardView;

    public MainLayoutView() {
        DepartmentRepository deptRepo = new DepartmentRepository();
        DoctorRepository docRepo = new DoctorRepository();
        VisitRepository visitRepo = new VisitRepository();

        this.dashboardView = new DashboardView(new DashboardService(visitRepo, docRepo), this::showRegisterView, this::showQueueView);
        this.registerVisitView = new RegisterVisitView(new VisitService(), deptRepo, docRepo, this::showQueueView);
        this.queueBoardView = new QueueBoardView(new QueueService(visitRepo), visitRepo, docRepo);

        VBox top = new VBox();
        HBox header = new HBox(16); header.getStyleClass().add("header-bar"); header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(2);
        Label title = new Label("🏥 CLINICQUEUE"); title.getStyleClass().add("app-title");
        Label sub = new Label("Outpatient Queue Management System (MVP)"); sub.getStyleClass().add("app-subtitle");
        titleBox.getChildren().addAll(title, sub);
        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);
        Label date = new Label("📅 " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        date.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: 600;");
        header.getChildren().addAll(titleBox, sp1, date);

        HBox nav = new HBox(12); nav.getStyleClass().add("nav-bar"); nav.setAlignment(Pos.CENTER_LEFT);
        btnDash.getStyleClass().add("nav-button"); btnDash.setOnAction(e -> showDashboardView());
        btnReg.getStyleClass().add("nav-button"); btnReg.setOnAction(e -> showRegisterView());
        btnQueue.getStyleClass().add("nav-button"); btnQueue.setOnAction(e -> showQueueView());
        btnReset.getStyleClass().add("reset-button"); btnReset.setOnAction(e -> handleReset());
        Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);
        nav.getChildren().addAll(btnDash, btnReg, btnQueue, sp2, btnReset);

        top.getChildren().addAll(header, nav);
        setTop(top);
        setCenter(contentArea);

        HBox status = new HBox(20); status.getStyleClass().add("status-bar"); status.setAlignment(Pos.CENTER_LEFT);
        Label db = new Label("🟢 Database: SQLite (clinicqueue.db) | Foreign Keys: ON");
        Region sp3 = new Region(); HBox.setHgrow(sp3, Priority.ALWAYS);
        Label mode = new Label("Single-Workstation Presentation MVP • Fictional Demo Data");
        status.getChildren().addAll(db, sp3, mode);
        setBottom(status);

        showDashboardView();
    }

    private void setActive(Button b) {
        btnDash.getStyleClass().remove("nav-button-active");
        btnReg.getStyleClass().remove("nav-button-active");
        btnQueue.getStyleClass().remove("nav-button-active");
        if (!b.getStyleClass().contains("nav-button-active")) b.getStyleClass().add("nav-button-active");
    }

    public void showDashboardView() { setActive(btnDash); dashboardView.refreshData(); contentArea.getChildren().setAll(dashboardView); }
    public void showRegisterView() { setActive(btnReg); registerVisitView.resetForm(); contentArea.getChildren().setAll(registerVisitView); }
    public void showQueueView() { setActive(btnQueue); queueBoardView.refreshQueue(); contentArea.getChildren().setAll(queueBoardView); }

    private void handleReset() {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Clear existing visits and reseed sample patients & tokens?", ButtonType.OK, ButtonType.CANCEL);
        c.setTitle("Reset Demo Data");
        Optional<ButtonType> r = c.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) {
            try {
                SchemaInitializer.resetAndReseedDemoData();
                showDashboardView();
                new Alert(Alert.AlertType.INFORMATION, "Database reset and seeded with clean demo records.").showAndWait();
            } catch (SQLException ex) {
                new Alert(Alert.AlertType.ERROR, "Failed to reset: " + ex.getMessage()).showAndWait();
            }
        }
    }
}
