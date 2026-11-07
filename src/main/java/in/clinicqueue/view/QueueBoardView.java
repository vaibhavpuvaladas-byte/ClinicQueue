package in.clinicqueue.view;

import in.clinicqueue.model.Doctor;
import in.clinicqueue.model.Visit;
import in.clinicqueue.model.VisitStatus;
import in.clinicqueue.repository.DoctorRepository;
import in.clinicqueue.repository.VisitRepository;
import in.clinicqueue.service.QueueService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Queue Board View displaying the live doctor-specific queue with
 * Call Next, Complete Consultation, and Cancel Visit actions.
 */
public class QueueBoardView extends VBox {

    private final QueueService queueService;
    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;

    // Controls
    private final ComboBox<Doctor> cbDoctor = new ComboBox<>();
    private final ComboBox<String> cbStatusFilter = new ComboBox<>();
    private final TextField txtSearch = new TextField();

    private final Button btnCallNext = new Button("📞 Call Next Patient");
    private final Button btnComplete = new Button("✅ Complete Consultation");
    private final Button btnCancel = new Button("❌ Cancel Visit");
    private final Button btnRefresh = new Button("🔄 Refresh");

    private final TableView<Visit> tableView = new TableView<>();
    private final ObservableList<Visit> tableData = FXCollections.observableArrayList();

    public QueueBoardView(QueueService queueService,
                          VisitRepository visitRepository,
                          DoctorRepository doctorRepository) {
        this.queueService = queueService;
        this.visitRepository = visitRepository;
        this.doctorRepository = doctorRepository;

        setSpacing(16);
        setPadding(new Insets(24));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        buildHeader();
        buildFilterBar();
        buildActionBar();
        buildTable();
        buildFooter();
        loadDoctors();
        refreshQueue();
    }

    private void buildHeader() {
        VBox titleBox = new VBox(4);
        Label heading = new Label("Live Clinic Queue Board");
        heading.getStyleClass().add("section-heading");
        Label subheading = new Label("Call waiting patients into consultation, track real-time queue order, and complete visits.");
        subheading.getStyleClass().add("section-subheading");
        titleBox.getChildren().addAll(heading, subheading);
        getChildren().add(titleBox);
    }

    private void buildFilterBar() {
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.getStyleClass().add("card-panel");
        filterBar.setPadding(new Insets(12, 16, 12, 16));

        // Doctor Filter
        Label lblDoc = new Label("Doctor:");
        lblDoc.getStyleClass().add("form-label");
        cbDoctor.setPrefWidth(220);
        cbDoctor.setOnAction(e -> refreshQueue());

        // Status Filter
        Label lblStatus = new Label("Status:");
        lblStatus.getStyleClass().add("form-label");
        cbStatusFilter.getItems().addAll("ALL", "WAITING", "IN_CONSULTATION", "COMPLETED", "CANCELLED");
        cbStatusFilter.setValue("ALL");
        cbStatusFilter.setOnAction(e -> refreshQueue());

        // Search Input
        Label lblSearch = new Label("Search:");
        lblSearch.getStyleClass().add("form-label");
        txtSearch.setPromptText("Patient name or token...");
        txtSearch.setPrefWidth(200);
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> refreshQueue());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnRefresh.getStyleClass().add("btn-secondary");
        btnRefresh.setOnAction(e -> refreshQueue());

        filterBar.getChildren().addAll(lblDoc, cbDoctor, lblStatus, cbStatusFilter, lblSearch, txtSearch, spacer, btnRefresh);
        getChildren().add(filterBar);
    }

    private void buildActionBar() {
        HBox actionBar = new HBox(12);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        btnCallNext.getStyleClass().add("btn-primary");
        btnCallNext.setOnAction(e -> handleCallNext());

        btnComplete.getStyleClass().add("btn-success");
        btnComplete.setOnAction(e -> handleComplete());

        btnCancel.getStyleClass().add("btn-danger");
        btnCancel.setOnAction(e -> handleCancel());

        actionBar.getChildren().addAll(btnCallNext, btnComplete, btnCancel);
        getChildren().add(actionBar);
    }

    @SuppressWarnings("unchecked")
    private void buildTable() {
        TableColumn<Visit, String> colToken = new TableColumn<>("Token #");
        colToken.setCellValueFactory(new PropertyValueFactory<>("tokenNumber"));
        colToken.setPrefWidth(160);
        colToken.setStyle("-fx-font-weight: bold; -fx-text-fill: #1d4ed8;");

        TableColumn<Visit, String> colPatient = new TableColumn<>("Patient Name");
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colPatient.setPrefWidth(180);

        TableColumn<Visit, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getPatientPhone() != null ? cell.getValue().getPatientPhone() : "—"));
        colPhone.setPrefWidth(120);

        TableColumn<Visit, String> colDoctor = new TableColumn<>("Doctor");
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colDoctor.setPrefWidth(160);

        TableColumn<Visit, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getVisitType().getLabel()));
        colType.setPrefWidth(110);

        TableColumn<Visit, String> colPos = new TableColumn<>("Queue Pos.");
        colPos.setCellValueFactory(cell -> {
            Visit v = cell.getValue();
            if (v.getStatus() == VisitStatus.WAITING) {
                return new SimpleStringProperty("#" + v.getWaitingPosition());
            }
            return new SimpleStringProperty("—");
        });
        colPos.setPrefWidth(90);

        TableColumn<Visit, String> colWait = new TableColumn<>("Approx. Wait");
        colWait.setCellValueFactory(cell -> {
            Visit v = cell.getValue();
            if (v.getStatus() == VisitStatus.WAITING) {
                return new SimpleStringProperty(v.getEstimatedWaitMinutes() + " mins");
            }
            return new SimpleStringProperty("—");
        });
        colWait.setPrefWidth(120);

        TableColumn<Visit, VisitStatus> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(VisitStatus status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label chip = new Label(status.getLabel());
                    chip.getStyleClass().add("chip");
                    switch (status) {
                        case WAITING -> chip.getStyleClass().add("chip-waiting");
                        case IN_CONSULTATION -> chip.getStyleClass().add("chip-in-consultation");
                        case COMPLETED -> chip.getStyleClass().add("chip-completed");
                        case CANCELLED -> chip.getStyleClass().add("chip-cancelled");
                    }
                    setGraphic(chip);
                    setText(null);
                }
            }
        });
        colStatus.setPrefWidth(140);

        TableColumn<Visit, String> colTime = new TableColumn<>("Registered At");
        colTime.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colTime.setPrefWidth(160);

        tableView.getColumns().addAll(colToken, colPatient, colPhone, colDoctor, colType, colPos, colWait, colStatus, colTime);
        tableView.setItems(tableData);
        tableView.setPlaceholder(new Label("No visits matching current filter."));

        getChildren().add(tableView);
    }

    private void buildFooter() {
        Label footer = new Label("ℹ️ Note: Estimated wait time = (Waiting Position Ahead × 10 min average). Approximate estimate for classroom demonstration.");
        footer.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-style: italic;");
        getChildren().add(footer);
    }

    private void loadDoctors() {
        try {
            List<Doctor> doctors = doctorRepository.getAllActive();
            // Create a pseudo "All Doctors" option
            Doctor allOption = new Doctor(0, 0, "All Doctors", true, "All");
            cbDoctor.getItems().add(allOption);
            cbDoctor.getItems().addAll(doctors);
            cbDoctor.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load doctors: " + e.getMessage());
        }
    }

    public void refreshQueue() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Doctor selectedDoc = cbDoctor.getSelectionModel().getSelectedItem();
        Integer docId = (selectedDoc != null && selectedDoc.getId() > 0) ? selectedDoc.getId() : null;

        String statusStr = cbStatusFilter.getValue();
        VisitStatus status = (statusStr != null && !statusStr.equals("ALL")) ? VisitStatus.valueOf(statusStr) : null;

        String query = txtSearch.getText();

        try {
            List<Visit> visits = visitRepository.searchVisits(today, docId, status, query);
            queueService.calculateWaitTimes(visits);
            tableData.setAll(visits);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Query Error", "Failed to load visits: " + e.getMessage());
        }
    }

    private void handleCallNext() {
        Doctor selectedDoc = cbDoctor.getSelectionModel().getSelectedItem();
        if (selectedDoc == null || selectedDoc.getId() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Doctor Required", "Please select a specific doctor to call the next patient.");
            return;
        }

        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        try {
            Visit called = queueService.callNext(selectedDoc.getId(), today);
            refreshQueue();
            showAlert(Alert.AlertType.INFORMATION, "Patient Called",
                    String.format("Called token %s (%s) for %s.",
                            called.getTokenNumber(), called.getPatientName(), selectedDoc.getDisplayName()));
        } catch (IllegalStateException ex) {
            showAlert(Alert.AlertType.WARNING, "Cannot Call Next", ex.getMessage());
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update visit status: " + ex.getMessage());
        }
    }

    private void handleComplete() {
        Visit selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select an 'In Consultation' visit from the table to complete.");
            return;
        }

        try {
            queueService.completeConsultation(selected.getId());
            refreshQueue();
            showAlert(Alert.AlertType.INFORMATION, "Consultation Completed",
                    "Consultation completed for token " + selected.getTokenNumber());
        } catch (IllegalStateException ex) {
            showAlert(Alert.AlertType.WARNING, "Invalid Action", ex.getMessage());
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to complete consultation: " + ex.getMessage());
        }
    }

    private void handleCancel() {
        Visit selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a 'Waiting' visit from the table to cancel.");
            return;
        }

        if (selected.getStatus() != VisitStatus.WAITING) {
            showAlert(Alert.AlertType.WARNING, "Invalid Action", "Only visits in 'Waiting' status can be cancelled.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Cancellation");
        confirm.setHeaderText("Cancel Visit for " + selected.getPatientName() + "?");
        confirm.setContentText("Are you sure you want to cancel token " + selected.getTokenNumber() + "? This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                queueService.cancelVisit(selected.getId());
                refreshQueue();
                showAlert(Alert.AlertType.INFORMATION, "Visit Cancelled", "Token " + selected.getTokenNumber() + " has been cancelled.");
            } catch (IllegalStateException | SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Cancellation Failed", ex.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
