package in.clinicqueue;

import in.clinicqueue.database.SchemaInitializer;
import in.clinicqueue.view.MainLayoutView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Main Application Entry Point for ClinicQueue.
 */
public class ClinicQueueApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Build main layout
        MainLayoutView root = new MainLayoutView();

        // 2. Setup Scene (Optimized for 1366x768 screens, resizable)
        Scene scene = new Scene(root, 1200, 750);

        // 3. Load CSS styles
        try {
            String cssPath = Objects.requireNonNull(
                    getClass().getResource("/in/clinicqueue/css/styles.css")
            ).toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Warning: Could not load styles.css: " + e.getMessage());
        }

        // 4. Configure Stage
        primaryStage.setTitle("ClinicQueue — Hospital Outpatient Queue Management System");
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // 1. Initialize SQLite Database Schema
        SchemaInitializer.initDatabase();

        // 2. Seed initial demo dataset if today's visits are empty
        SchemaInitializer.seedDemoVisitsIfEmpty();

        // 3. Launch JavaFX Desktop Interface
        launch(args);
    }
}
