package in.clinicqueue.view;

import in.clinicqueue.model.*;
import in.clinicqueue.repository.*;
import in.clinicqueue.service.VisitService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;

public class RegisterVisitView extends VBox {
    private final VisitService visitService;
    private final DepartmentRepository deptRepo;
    private final DoctorRepository docRepo;
    private final TextField txtName = new TextField(), txtPhone = new TextField();
    private final ComboBox<Department> cbDept = new ComboBox<>();
    private final ComboBox<Doctor> cbDoc = new ComboBox<>();
    private final RadioButton rbWalkIn = new RadioButton("Walk-In"), rbAppt = new RadioButton("Appointment");
    private final VBox formCard = new VBox(14), successCard = new VBox(14);
    private final Label lblSuccessToken = new Label(), lblSuccessDetails = new Label();

    public RegisterVisitView(VisitService visitService, DepartmentRepository deptRepo, DoctorRepository docRepo, Runnable navigateToQueue) {
        this.visitService = visitService; this.deptRepo = deptRepo; this.docRepo = docRepo;
        setSpacing(16); setPadding(new Insets(24)); setMaxWidth(650);

        Label heading = new Label("Register Patient Visit"); heading.getStyleClass().add("section-heading");
        Label sub = new Label("Enter patient details and assign a doctor to generate a daily queue token."); sub.getStyleClass().add("section-subheading");

        formCard.getStyleClass().add("card-panel");
        txtName.setPromptText("e.g. Aarav Sharma");
        txtPhone.setPromptText("e.g. 9876543210 (Optional)");

        cbDept.setPrefWidth(260); cbDept.setOnAction(e -> onDeptChanged());
        cbDoc.setPrefWidth(260);

        ToggleGroup tg = new ToggleGroup();
        rbWalkIn.setToggleGroup(tg); rbAppt.setToggleGroup(tg); rbWalkIn.setSelected(true);
        HBox typeBox = new HBox(16, rbWalkIn, rbAppt);

        HBox selectBox = new HBox(16, new VBox(4, new Label("Department *"), cbDept), new VBox(4, new Label("Doctor *"), cbDoc));
        Button btnSubmit = new Button("Register & Generate Token"); btnSubmit.getStyleClass().add("btn-primary");
        btnSubmit.setMaxWidth(Double.MAX_VALUE); btnSubmit.setOnAction(e -> handleRegister());

        formCard.getChildren().addAll(
            new VBox(4, new Label("Patient Full Name *"), txtName),
            new VBox(4, new Label("Phone Number"), txtPhone),
            selectBox,
            new VBox(4, new Label("Visit Type *"), typeBox),
            btnSubmit
        );

        successCard.getStyleClass().addAll("card-panel", "token-card"); successCard.setAlignment(Pos.CENTER);
        successCard.setVisible(false); successCard.setManaged(false);
        Label sTitle = new Label("Visit Registered Successfully!"); sTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #15803d;");
        lblSuccessToken.getStyleClass().add("token-highlight");
        Button btnGoQueue = new Button("Go to Queue Board"); btnGoQueue.getStyleClass().add("btn-primary"); btnGoQueue.setOnAction(e -> navigateToQueue.run());
        Button btnAnother = new Button("+ Register Another Patient"); btnAnother.getStyleClass().add("btn-secondary"); btnAnother.setOnAction(e -> resetForm());
        successCard.getChildren().addAll(sTitle, lblSuccessToken, lblSuccessDetails, new HBox(16, btnGoQueue, btnAnother));

        getChildren().addAll(new VBox(4, heading, sub), formCard, successCard);
        loadDropdownData();
    }

    private void loadDropdownData() {
        try {
            List<Department> depts = deptRepo.getAllActive();
            cbDept.getItems().setAll(depts);
            if (!depts.isEmpty()) { cbDept.getSelectionModel().selectFirst(); onDeptChanged(); }
        } catch (SQLException e) { showError("Error", e.getMessage()); }
    }

    private void onDeptChanged() {
        Department d = cbDept.getSelectionModel().getSelectedItem();
        if (d == null) return;
        try {
            List<Doctor> docs = docRepo.getByDepartmentId(d.getId());
            cbDoc.getItems().setAll(docs);
            if (!docs.isEmpty()) cbDoc.getSelectionModel().selectFirst();
        } catch (SQLException e) { showError("Error", e.getMessage()); }
    }

    private void handleRegister() {
        String name = txtName.getText(), phone = txtPhone.getText();
        Doctor doc = cbDoc.getSelectionModel().getSelectedItem();
        VisitType type = rbAppt.isSelected() ? VisitType.APPOINTMENT : VisitType.WALK_IN;
        if (name == null || name.trim().isEmpty()) { showError("Validation Error", "Patient full name is required."); return; }
        if (doc == null) { showError("Validation Error", "Please select a consulting doctor."); return; }

        try {
            Visit v = visitService.registerVisit(name, phone, doc.getId(), type);
            lblSuccessToken.setText(v.getTokenNumber());
            lblSuccessDetails.setText(String.format("Patient: %s  |  Doctor: %s  |  Type: %s", name.trim(), doc.getDisplayName(), type.getLabel()));
            formCard.setVisible(false); formCard.setManaged(false);
            successCard.setVisible(true); successCard.setManaged(true);
        } catch (IllegalArgumentException ex) {
            showError("Validation Error", ex.getMessage());
        } catch (SQLException ex) {
            showError("Registration Error", "Database error: " + ex.getMessage());
        }
    }

    public void resetForm() {
        txtName.clear(); txtPhone.clear(); rbWalkIn.setSelected(true);
        formCard.setVisible(true); formCard.setManaged(true);
        successCard.setVisible(false); successCard.setManaged(false);
        txtName.requestFocus();
    }

    private void showError(String title, String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }
}
