package in.clinicqueue;

import in.clinicqueue.view.DashboardView;
import in.clinicqueue.view.QueueBoardView;
import in.clinicqueue.view.RegisterVisitView;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Main JavaFX Application shell for ClinicQueue MVP.
 * Coordinates primary navigation between Dashboard, Visit Registration, and Queue Board.
 */
public class ClinicQueueApp extends Application {

    private BorderPane rootLayout;
    private StackPane contentArea;

    private Button navDashboardBtn;
    private Button navRegisterBtn;
    private Button navQueueBtn;

    private DashboardView dashboardView;
    private RegisterVisitView registerVisitView;
    private QueueBoardView queueBoardView;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ClinicQueue — Hospital Queue Management MVP");

        rootLayout = new BorderPane();

        // 1. Top Header & Navigation
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(createHeaderBar(), createNavigationBar());
        rootLayout.setTop(topContainer);

        // 2. Central Content Area hosting views
        contentArea = new StackPane();
        dashboardView = new DashboardView();
        registerVisitView = new RegisterVisitView();
        queueBoardView = new QueueBoardView();

        contentArea.getChildren().addAll(dashboardView, registerVisitView, queueBoardView);
        rootLayout.setCenter(contentArea);

        // Show Dashboard by default
        showDashboard();

        // 3. Scene creation and CSS loading
        Scene scene = new Scene(rootLayout, 1280, 750);
        String cssPath = getClass().getResource("/in/clinicqueue/css/app.css") != null
                ? getClass().getResource("/in/clinicqueue/css/app.css").toExternalForm()
                : null;
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }

        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(640);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createHeaderBar() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header-bar");

        VBox titleBox = new VBox(2);
        Label title = new Label("🏥 ClinicQueue Operations");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Outpatient Department & Queue Management System");
        subtitle.getStyleClass().add("header-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLabel = new Label("📅 " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")));
        dateLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: 500;");

        Label badge = new Label("MVP PROTOTYPE");
        badge.getStyleClass().add("badge-demo");

        header.getChildren().addAll(titleBox, spacer, dateLabel, badge);
        return header;
    }

    private HBox createNavigationBar() {
        HBox nav = new HBox(8);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.getStyleClass().add("nav-bar");

        navDashboardBtn = new Button("📊 Dashboard");
        navDashboardBtn.getStyleClass().addAll("nav-button", "nav-button-active");
        navDashboardBtn.setOnAction(e -> showDashboard());

        navRegisterBtn = new Button("➕ Register Patient Visit");
        navRegisterBtn.getStyleClass().add("nav-button");
        navRegisterBtn.setOnAction(e -> showRegisterVisit());

        navQueueBtn = new Button("📋 Doctor Queue Board");
        navQueueBtn.getStyleClass().add("nav-button");
        navQueueBtn.setOnAction(e -> showQueueBoard());

        nav.getChildren().addAll(navDashboardBtn, navRegisterBtn, navQueueBtn);
        return nav;
    }

    private void showDashboard() {
        setActiveNav(navDashboardBtn);
        dashboardView.setVisible(true);
        registerVisitView.setVisible(false);
        queueBoardView.setVisible(false);
    }

    private void showRegisterVisit() {
        setActiveNav(navRegisterBtn);
        dashboardView.setVisible(false);
        registerVisitView.setVisible(true);
        queueBoardView.setVisible(false);
    }

    private void showQueueBoard() {
        setActiveNav(navQueueBtn);
        dashboardView.setVisible(false);
        registerVisitView.setVisible(false);
        queueBoardView.setVisible(true);
    }

    private void setActiveNav(Button activeButton) {
        navDashboardBtn.getStyleClass().remove("nav-button-active");
        navRegisterBtn.getStyleClass().remove("nav-button-active");
        navQueueBtn.getStyleClass().remove("nav-button-active");

        if (!activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
