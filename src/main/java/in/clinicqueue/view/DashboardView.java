package in.clinicqueue.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * DashboardView renders high-level operational statistics:
 * - 4 Status Counters (Waiting, In Consultation, Completed, Cancelled)
 * - Doctor Queue Summaries
 * - Approximate Wait Time overview
 */
public class DashboardView extends VBox {

    private final Label waitingCountLabel = new Label("0");
    private final Label consultationCountLabel = new Label("0");
    private final Label completedCountLabel = new Label("0");
    private final Label cancelledCountLabel = new Label("0");

    public DashboardView() {
        setSpacing(20);
        setPadding(new Insets(20, 24, 20, 24));
        getStyleClass().add("content-area");

        // Top bar with Date and Subtitle
        HBox titleBox = new HBox(12);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        VBox titleText = new VBox(4);
        Label heading = new Label("Clinic Operational Overview");
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label subtitle = new Label("Live patient queue tracking for today • " 
                + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        subtitle.getStyleClass().add("card-subtitle");
        titleText.getChildren().addAll(heading, subtitle);

        titleBox.getChildren().add(titleText);

        // 4 KPI Stat Cards
        HBox statsRow = new HBox(16);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        VBox waitingCard = createStatCard("WAITING PATIENTS", waitingCountLabel, "status-waiting", "#b45309");
        VBox consultCard = createStatCard("IN CONSULTATION", consultationCountLabel, "status-in-consultation", "#1d4ed8");
        VBox completedCard = createStatCard("COMPLETED VISITS", completedCountLabel, "status-completed", "#15803d");
        VBox cancelledCard = createStatCard("CANCELLED", cancelledCountLabel, "status-cancelled", "#475569");

        HBox.setHgrow(waitingCard, Priority.ALWAYS);
        HBox.setHgrow(consultCard, Priority.ALWAYS);
        HBox.setHgrow(completedCard, Priority.ALWAYS);
        HBox.setHgrow(cancelledCard, Priority.ALWAYS);

        statsRow.getChildren().addAll(waitingCard, consultCard, completedCard, cancelledCard);

        // Doctor Queue Summary Container Card
        VBox doctorSummaryCard = new VBox(12);
        doctorSummaryCard.getStyleClass().add("card");
        VBox.setVgrow(doctorSummaryCard, Priority.ALWAYS);

        Label docSummaryTitle = new Label("Doctor Queue Summaries");
        docSummaryTitle.getStyleClass().add("card-title");
        Label docSummaryDesc = new Label("Active queue load by doctor and department.");
        docSummaryDesc.getStyleClass().add("card-subtitle");

        Separator sep = new Separator();

        VBox emptyState = new VBox(8);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(30));
        Label emptyText = new Label("Doctor queue metrics will initialize with seed/active data.");
        emptyText.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");
        emptyState.getChildren().add(emptyText);

        doctorSummaryCard.getChildren().addAll(docSummaryTitle, docSummaryDesc, sep, emptyState);

        getChildren().addAll(titleBox, statsRow, doctorSummaryCard);
    }

    private VBox createStatCard(String labelText, Label valueLabel, String styleClass, String accentColor) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("stat-card", "card");

        Label label = new Label(labelText);
        label.getStyleClass().add("stat-label");

        valueLabel.getStyleClass().add("stat-value");
        valueLabel.setStyle("-fx-text-fill: " + accentColor + ";");

        card.getChildren().addAll(label, valueLabel);
        return card;
    }
}
