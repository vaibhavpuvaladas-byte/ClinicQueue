package in.clinicqueue.view;

import in.clinicqueue.model.*;
import in.clinicqueue.repository.*;
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

public class QueueBoardView extends VBox {
    private final QueueService queueService;
    private final VisitRepository visitRepo;
    private final DoctorRepository docRepo;
    private final ComboBox<Doctor> cbDoctor = new ComboBox<>();
    private final ComboBox<String> cbStatus = new ComboBox<>();
    private final TextField txtSearch = new TextField();
    private final TableView<Visit> tableView = new TableView<>();
    private final ObservableList<Visit> tableData = FXCollections.observableArrayList();

    public QueueBoardView(QueueService qSvc, VisitRepository vRepo, DoctorRepository dRepo) {
        this.queueService = qSvc; this.visitRepo = vRepo; this.docRepo = dRepo;
        setSpacing(14); setPadding(new Insets(24)); VBox.setVgrow(tableView, Priority.ALWAYS);

        Label h = new Label("Live Clinic Queue Board"); h.getStyleClass().add("section-heading");
        Label sub = new Label("Call waiting patients, track queue order, and complete visits."); sub.getStyleClass().add("section-subheading");

        HBox filter = new HBox(12); filter.setAlignment(Pos.CENTER_LEFT); filter.getStyleClass().add("card-panel");
        cbDoctor.setPrefWidth(220); cbDoctor.setOnAction(e -> refreshQueue());
        cbStatus.getItems().addAll("ALL", "WAITING", "IN_CONSULTATION", "COMPLETED", "CANCELLED");
        cbStatus.setValue("ALL"); cbStatus.setOnAction(e -> refreshQueue());
        txtSearch.setPromptText("Search name/token..."); txtSearch.setPrefWidth(180);
        txtSearch.textProperty().addListener((o, ov, nv) -> refreshQueue());
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button btnRef = new Button("🔄 Refresh"); btnRef.getStyleClass().add("btn-secondary"); btnRef.setOnAction(e -> refreshQueue());
        filter.getChildren().addAll(new Label("Doctor:"), cbDoctor, new Label("Status:"), cbStatus, new Label("Search:"), txtSearch, sp, btnRef);

        Button btnCall = new Button("📞 Call Next Patient"), btnComp = new Button("✅ Complete Consultation"), btnCanc = new Button("❌ Cancel Visit");
        btnCall.getStyleClass().add("btn-primary"); btnCall.setOnAction(e -> handleCallNext());
        btnComp.getStyleClass().add("btn-success"); btnComp.setOnAction(e -> handleComplete());
        btnCanc.getStyleClass().add("btn-danger"); btnCanc.setOnAction(e -> handleCancel());
        HBox actions = new HBox(12, btnCall, btnComp, btnCanc);

        buildTable();

        Label footer = new Label("ℹ️ Note: Estimated wait time = (Waiting Position Ahead × 10 min average). Approximate estimate.");
        footer.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-style: italic;");

        getChildren().addAll(new VBox(4, h, sub), filter, actions, tableView, footer);
        loadDoctors(); refreshQueue();
    }

    @SuppressWarnings("unchecked")
    private void buildTable() {
        TableColumn<Visit, String> cToken = col("Token #", 150, v -> new SimpleStringProperty(v.getTokenNumber()));
        cToken.setStyle("-fx-font-weight: bold; -fx-text-fill: #1d4ed8;");
        TableColumn<Visit, String> cName = col("Patient Name", 170, v -> new SimpleStringProperty(v.getPatientName()));
        TableColumn<Visit, String> cPhone = col("Phone", 110, v -> new SimpleStringProperty(v.getPatientPhone() != null ? v.getPatientPhone() : "—"));
        TableColumn<Visit, String> cDoc = col("Doctor", 150, v -> new SimpleStringProperty(v.getDoctorName()));
        TableColumn<Visit, String> cType = col("Type", 100, v -> new SimpleStringProperty(v.getVisitType().getLabel()));
        TableColumn<Visit, String> cPos = col("Pos.", 70, v -> new SimpleStringProperty(v.getStatus() == VisitStatus.WAITING ? "#" + v.getWaitingPosition() : "—"));
        TableColumn<Visit, String> cWait = col("Approx. Wait", 110, v -> new SimpleStringProperty(v.getStatus() == VisitStatus.WAITING ? v.getEstimatedWaitMinutes() + " mins" : "—"));

        TableColumn<Visit, VisitStatus> cStatus = new TableColumn<>("Status");
        cStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        cStatus.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(VisitStatus s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label chip = new Label(s.getLabel()); chip.getStyleClass().addAll("chip", switch (s) {
                    case WAITING -> "chip-waiting"; case IN_CONSULTATION -> "chip-in-consultation";
                    case COMPLETED -> "chip-completed"; case CANCELLED -> "chip-cancelled";
                });
                setGraphic(chip); setText(null);
            }
        });
        cStatus.setPrefWidth(130);

        TableColumn<Visit, String> cTime = col("Registered At", 150, v -> new SimpleStringProperty(v.getCreatedAt()));

        tableView.getColumns().addAll(cToken, cName, cPhone, cDoc, cType, cPos, cWait, cStatus, cTime);
        tableView.setItems(tableData);
        tableView.setPlaceholder(new Label("No visits matching current filter."));
    }

    private <T> TableColumn<Visit, T> col(String title, double width, java.util.function.Function<Visit, javafx.beans.value.ObservableValue<T>> mapper) {
        TableColumn<Visit, T> c = new TableColumn<>(title);
        c.setCellValueFactory(cell -> mapper.apply(cell.getValue()));
        c.setPrefWidth(width);
        return c;
    }

    private void loadDoctors() {
        try {
            cbDoctor.getItems().add(new Doctor(0, 0, "All Doctors", true, "All"));
            cbDoctor.getItems().addAll(docRepo.getAllActive());
            cbDoctor.getSelectionModel().selectFirst();
        } catch (SQLException e) { alert(Alert.AlertType.ERROR, "Error", e.getMessage()); }
    }

    public void refreshQueue() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Doctor d = cbDoctor.getSelectionModel().getSelectedItem();
        Integer docId = (d != null && d.getId() > 0) ? d.getId() : null;
        String s = cbStatus.getValue();
        VisitStatus status = (s != null && !s.equals("ALL")) ? VisitStatus.valueOf(s) : null;
        try {
            List<Visit> list = visitRepo.searchVisits(today, docId, status, txtSearch.getText());
            queueService.calculateWaitTimes(list);
            tableData.setAll(list);
        } catch (SQLException e) { alert(Alert.AlertType.ERROR, "Query Error", e.getMessage()); }
    }

    private void handleCallNext() {
        Doctor d = cbDoctor.getSelectionModel().getSelectedItem();
        if (d == null || d.getId() <= 0) { alert(Alert.AlertType.WARNING, "Select Doctor", "Please select a specific doctor to call the next patient."); return; }
        try {
            Visit v = queueService.callNext(d.getId(), LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            refreshQueue();
            alert(Alert.AlertType.INFORMATION, "Patient Called", "Called token " + v.getTokenNumber() + " (" + v.getPatientName() + ")");
        } catch (IllegalStateException ex) { alert(Alert.AlertType.WARNING, "Cannot Call Next", ex.getMessage()); }
        catch (SQLException ex) { alert(Alert.AlertType.ERROR, "Error", ex.getMessage()); }
    }

    private void handleComplete() {
        Visit sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { alert(Alert.AlertType.WARNING, "Select Visit", "Please select an 'In Consultation' visit to complete."); return; }
        try {
            queueService.completeConsultation(sel.getId());
            refreshQueue();
            alert(Alert.AlertType.INFORMATION, "Completed", "Consultation completed for " + sel.getTokenNumber());
        } catch (IllegalStateException ex) { alert(Alert.AlertType.WARNING, "Invalid Action", ex.getMessage()); }
        catch (SQLException ex) { alert(Alert.AlertType.ERROR, "Error", ex.getMessage()); }
    }

    private void handleCancel() {
        Visit sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null || sel.getStatus() != VisitStatus.WAITING) {
            alert(Alert.AlertType.WARNING, "Select Waiting", "Please select a 'Waiting' visit to cancel."); return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Cancel token " + sel.getTokenNumber() + "?", ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                queueService.cancelVisit(sel.getId());
                refreshQueue();
                alert(Alert.AlertType.INFORMATION, "Cancelled", "Token " + sel.getTokenNumber() + " cancelled.");
            } catch (IllegalStateException | SQLException ex) { alert(Alert.AlertType.ERROR, "Error", ex.getMessage()); }
        }
    }

    private void alert(Alert.AlertType type, String title, String msg) {
        new Alert(type, msg).showAndWait();
    }
}
