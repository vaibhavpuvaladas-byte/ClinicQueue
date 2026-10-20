package in.clinicqueue.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * QueueBoardView provides the doctor & operator queue management console:
 * - Doctor selector
 * - Status filter (All, Waiting, In Consultation, Completed, Cancelled)
 * - Patient / Token search
 * - Queue Table
 * - "Call Next" action (picks earliest waiting patient)
 * - "Complete Consultation" and "Cancel Visit" transitions
 * - Approximate wait time indicator
 */
public class QueueBoardView extends VBox {

    private final ComboBox<String> doctorFilterCombo = new ComboBox<>();
    private final ComboBox<String> statusFilterCombo = new ComboBox<>();
    private final TextField searchField = new TextField();
    private final Button callNextButton = new Button("▶ Call Next Patient");
    private final Button completeButton = new Button("✓ Complete Consultation");
    private final Button cancelButton = new Button("✕ Cancel Visit");
    private final TableView<String> queueTable = new TableView<>();
    private final Label waitEstimateBanner = new Label();

    public QueueBoardView() {
        setSpacing(16);
        setPadding(new Insets(20, 24, 20, 24));
        getStyleClass().add("content-area");

        // Heading
        VBox titleBox = new VBox(4);
        Label heading = new Label("Doctor Queue Management Board");
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label subtitle = new Label("Call waiting patients in FIFO order and manage consultation lifecycle.");
        subtitle.getStyleClass().add("card-subtitle");
        titleBox.getChildren().addAll(heading, subtitle);

        // Control Toolbar Card
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("card");

        Label docLbl = new Label("Doctor:");
        docLbl.getStyleClass().add("form-label");
        doctorFilterCombo.setPromptText("All Doctors");
        doctorFilterCombo.setPrefWidth(220);

        Label statusLbl = new Label("Status:");
        statusLbl.getStyleClass().add("form-label");
        statusFilterCombo.getItems().addAll("ALL STATUSES", "WAITING", "IN_CONSULTATION", "COMPLETED", "CANCELLED");
        statusFilterCombo.setValue("ALL STATUSES");
        statusFilterCombo.setPrefWidth(160);

        searchField.setPromptText("Search patient name / token...");
        searchField.setPrefWidth(220);

        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(actionsBox, Priority.ALWAYS);

        callNextButton.getStyleClass().add("btn-primary");
        completeButton.getStyleClass().add("btn-success");
        cancelButton.getStyleClass().add("btn-danger");

        actionsBox.getChildren().addAll(callNextButton, completeButton, cancelButton);

        toolbar.getChildren().addAll(docLbl, doctorFilterCombo, statusLbl, statusFilterCombo, searchField, actionsBox);

        // Wait estimate banner
        waitEstimateBanner.setStyle("-fx-background-color: #eff6ff; -fx-border-color: #bfdbfe; "
                + "-fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 8px 14px; "
                + "-fx-text-fill: #1e40af; -fx-font-weight: 500;");
        waitEstimateBanner.setText("⏱ APPROXIMATE WAIT: Estimated at ~10 mins per waiting patient ahead.");

        // Queue Table Card
        VBox tableCard = new VBox(10);
        tableCard.getStyleClass().add("card");
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        queueTable.setPlaceholder(new Label("No patients in queue for selected criteria."));
        VBox.setVgrow(queueTable, Priority.ALWAYS);

        tableCard.getChildren().addAll(waitEstimateBanner, queueTable);

        getChildren().addAll(titleBox, toolbar, tableCard);
    }
}
