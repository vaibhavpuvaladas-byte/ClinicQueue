package in.clinicqueue.view;

import in.clinicqueue.model.Department;
import in.clinicqueue.model.Doctor;
import in.clinicqueue.model.Visit;
import in.clinicqueue.model.VisitType;
import in.clinicqueue.repository.DepartmentRepository;
import in.clinicqueue.repository.DoctorRepository;
import in.clinicqueue.service.VisitService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Register Visit View allowing receptionists to enter patient information,
 * select department/doctor, choose visit type, and obtain an atomic daily token.
 */
public class RegisterVisitView extends VBox {

    private final VisitService visitService;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final Runnable navigateToQueue;

    // Form inputs
    private final TextField txtName = new TextField();
    private final TextField txtPhone = new TextField();
    private final ComboBox<Department> cbDepartment = new ComboBox<>();
    private final ComboBox<Doctor> cbDoctor = new ComboBox<>();
    private final RadioButton rbWalkIn = new RadioButton("Walk-In");
    private final RadioButton rbAppointment = new RadioButton("Appointment");

    // Success Card Elements
    private final VBox formCard = new VBox(16);
    private final VBox successCard = new VBox(16);
    private final Label lblSuccessToken = new Label();
    private final Label lblSuccessDetails = new Label();

    public RegisterVisitView(VisitService visitService,
                             DepartmentRepository departmentRepository,
                             DoctorRepository doctorRepository,
                             Runnable navigateToQueue) {
        this.visitService = visitService;
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.navigateToQueue = navigateToQueue;

        setSpacing(20);
        setPadding(new Insets(24));
        setMaxWidth(700);
        setAlignment(Pos.TOP_LEFT);

        buildHeader();
        buildForm();
        buildSuccessCard();
        loadDropdownData();
    }

    private void buildHeader() {
        VBox titleBox = new VBox(4);
        Label heading = new Label("Register Patient Visit");
        heading.getStyleClass().add("section-heading");
        Label subheading = new Label("Enter patient details and assign a consulting doctor to generate a daily queue token.");
        subheading.getStyleClass().add("section-subheading");
        titleBox.getChildren().addAll(heading, subheading);
        getChildren().add(titleBox);
    }

    private void buildForm() {
        formCard.getStyleClass().add("card-panel");

        // 1. Patient Name (Required)
        VBox nameBox = new VBox(6);
        Label lblName = new Label("Patient Full Name *");
        lblName.getStyleClass().add("form-label");
        txtName.setPromptText("e.g. Aarav Sharma");
        nameBox.getChildren().addAll(lblName, txtName);

        // 2. Phone Number (Optional)
        VBox phoneBox = new VBox(6);
        Label lblPhone = new Label("Phone Number (Optional)");
        lblPhone.getStyleClass().add("form-label");
        txtPhone.setPromptText("e.g. 9876543210");
        phoneBox.getChildren().addAll(lblPhone, txtPhone);

        // 3. Department & Doctor Selection
        HBox docSelectionBox = new HBox(16);

        VBox deptBox = new VBox(6);
        Label lblDept = new Label("Department *");
        lblDept.getStyleClass().add("form-label");
        cbDepartment.setPromptText("Select Department");
        cbDepartment.setPrefWidth(300);
        cbDepartment.setOnAction(e -> onDepartmentChanged());
        deptBox.getChildren().addAll(lblDept, cbDepartment);

        VBox doctorBox = new VBox(6);
        Label lblDoctor = new Label("Consulting Doctor *");
        lblDoctor.getStyleClass().add("form-label");
        cbDoctor.setPromptText("Select Doctor");
        cbDoctor.setPrefWidth(300);
        doctorBox.getChildren().addAll(lblDoctor, cbDoctor);

        docSelectionBox.getChildren().addAll(deptBox, doctorBox);

        // 4. Visit Type
        VBox typeBox = new VBox(8);
        Label lblType = new Label("Visit Type *");
        lblType.getStyleClass().add("form-label");

        ToggleGroup typeGroup = new ToggleGroup();
        rbWalkIn.setToggleGroup(typeGroup);
        rbAppointment.setToggleGroup(typeGroup);
        rbWalkIn.setSelected(true); // Default to Walk-In

        HBox radioBox = new HBox(20);
        radioBox.getChildren().addAll(rbWalkIn, rbAppointment);
        typeBox.getChildren().addAll(lblType, radioBox);

        // 5. Submit Button
        Button btnSubmit = new Button("Register & Generate Token");
        btnSubmit.getStyleClass().add("btn-primary");
        btnSubmit.setMaxWidth(Double.MAX_VALUE);
        btnSubmit.setOnAction(e -> handleRegister());

        formCard.getChildren().addAll(nameBox, phoneBox, docSelectionBox, typeBox, btnSubmit);
        getChildren().add(formCard);
    }

    private void buildSuccessCard() {
        successCard.getStyleClass().addAll("card-panel", "token-card");
        successCard.setAlignment(Pos.CENTER);
        successCard.setVisible(false);
        successCard.setManaged(false);

        Label lblTitle = new Label("Visit Registered Successfully!");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #15803d;");

        lblSuccessToken.getStyleClass().add("token-highlight");

        lblSuccessDetails.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155;");

        HBox btnBox = new HBox(16);
        btnBox.setAlignment(Pos.CENTER);

        Button btnViewQueue = new Button("Go to Queue Board");
        btnViewQueue.getStyleClass().add("btn-primary");
        btnViewQueue.setOnAction(e -> navigateToQueue.run());

        Button btnRegisterAnother = new Button("+ Register Another Patient");
        btnRegisterAnother.getStyleClass().add("btn-secondary");
        btnRegisterAnother.setOnAction(e -> resetForm());

        btnBox.getChildren().addAll(btnViewQueue, btnRegisterAnother);
        successCard.getChildren().addAll(lblTitle, lblSuccessToken, lblSuccessDetails, btnBox);
        getChildren().add(successCard);
    }

    private void loadDropdownData() {
        try {
            List<Department> departments = departmentRepository.getAllActive();
            cbDepartment.getItems().setAll(departments);
            if (!departments.isEmpty()) {
                cbDepartment.getSelectionModel().selectFirst();
                onDepartmentChanged();
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to load departments: " + e.getMessage());
        }
    }

    private void onDepartmentChanged() {
        Department selected = cbDepartment.getSelectionModel().getSelectedItem();
        if (selected == null) {
            cbDoctor.getItems().clear();
            return;
        }

        try {
            List<Doctor> doctors = doctorRepository.getByDepartmentId(selected.getId());
            cbDoctor.getItems().setAll(doctors);
            if (!doctors.isEmpty()) {
                cbDoctor.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to load doctors: " + e.getMessage());
        }
    }

    private void handleRegister() {
        String name = txtName.getText();
        String phone = txtPhone.getText();
        Doctor doc = cbDoctor.getSelectionModel().getSelectedItem();
        VisitType type = rbAppointment.isSelected() ? VisitType.APPOINTMENT : VisitType.WALK_IN;

        if (name == null || name.trim().isEmpty()) {
            showError("Validation Error", "Patient full name is required.");
            txtName.requestFocus();
            return;
        }

        if (doc == null) {
            showError("Validation Error", "Please select a consulting doctor.");
            return;
        }

        try {
            Visit visit = visitService.registerVisit(name, phone, doc.getId(), type);

            // Display success card
            lblSuccessToken.setText(visit.getTokenNumber());
            lblSuccessDetails.setText(String.format("Patient: %s  |  Doctor: %s  |  Type: %s",
                    name.trim(), doc.getDisplayName(), type.getLabel()));

            formCard.setVisible(false);
            formCard.setManaged(false);
            successCard.setVisible(true);
            successCard.setManaged(true);
        } catch (IllegalArgumentException ex) {
            showError("Validation Error", ex.getMessage());
        } catch (SQLException ex) {
            showError("Registration Error", "Database error occurred while allocating token: " + ex.getMessage());
        }
    }

    public void resetForm() {
        txtName.clear();
        txtPhone.clear();
        rbWalkIn.setSelected(true);
        formCard.setVisible(true);
        formCard.setManaged(true);
        successCard.setVisible(false);
        successCard.setManaged(false);
        txtName.requestFocus();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
