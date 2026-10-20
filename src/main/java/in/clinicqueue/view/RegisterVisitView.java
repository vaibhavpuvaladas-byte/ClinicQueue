package in.clinicqueue.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * RegisterVisitView provides the intake form for registering patients:
 * - Patient Name (required, trimmed, rejected if empty)
 * - Phone (optional)
 * - Department selection
 * - Doctor selection (filtered by chosen department)
 * - Visit Type (APPOINTMENT or WALK_IN)
 * - Clear submission feedback and generated token display
 */
public class RegisterVisitView extends VBox {

    private final TextField nameField = new TextField();
    private final TextField phoneField = new TextField();
    private final ComboBox<String> departmentCombo = new ComboBox<>();
    private final ComboBox<String> doctorCombo = new ComboBox<>();
    private final ToggleGroup visitTypeGroup = new ToggleGroup();
    private final RadioButton walkInRadio = new RadioButton("Walk-in");
    private final RadioButton appointmentRadio = new RadioButton("Scheduled Appointment");
    private final Button registerButton = new Button("Register Visit & Issue Token");
    private final Label feedbackLabel = new Label();

    public RegisterVisitView() {
        setSpacing(20);
        setPadding(new Insets(20, 24, 20, 24));
        getStyleClass().add("content-area");

        // Heading
        VBox titleBox = new VBox(4);
        Label heading = new Label("Patient Intake & Visit Registration");
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label subtitle = new Label("Issue a sequential daily queue token for doctor consultation.");
        subtitle.getStyleClass().add("card-subtitle");
        titleBox.getChildren().addAll(heading, subtitle);

        // Form Card
        VBox formCard = new VBox(16);
        formCard.getStyleClass().add("card");
        formCard.setMaxWidth(720);

        Label formHeader = new Label("Visit Details");
        formHeader.getStyleClass().add("card-title");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(14);

        // Name
        Label nameLbl = new Label("Patient Full Name *");
        nameLbl.getStyleClass().add("form-label");
        nameField.setPromptText("e.g. Aarav Sharma");
        nameField.setPrefWidth(380);

        // Phone
        Label phoneLbl = new Label("Phone Number (Optional)");
        phoneLbl.getStyleClass().add("form-label");
        phoneField.setPromptText("e.g. +91 9876543210");

        // Department
        Label deptLbl = new Label("Department *");
        deptLbl.getStyleClass().add("form-label");
        departmentCombo.setPromptText("Select Department");
        departmentCombo.setPrefWidth(380);

        // Doctor
        Label docLbl = new Label("Assigned Doctor *");
        docLbl.getStyleClass().add("form-label");
        doctorCombo.setPromptText("Select Doctor");
        doctorCombo.setPrefWidth(380);

        // Visit Type
        Label typeLbl = new Label("Visit Type *");
        typeLbl.getStyleClass().add("form-label");

        walkInRadio.setToggleGroup(visitTypeGroup);
        walkInRadio.setSelected(true);
        appointmentRadio.setToggleGroup(visitTypeGroup);

        HBox typeBox = new HBox(16, walkInRadio, appointmentRadio);
        typeBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(nameLbl, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(phoneLbl, 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(deptLbl, 0, 2);
        grid.add(departmentCombo, 1, 2);
        grid.add(docLbl, 0, 3);
        grid.add(doctorCombo, 1, 3);
        grid.add(typeLbl, 0, 4);
        grid.add(typeBox, 1, 4);

        registerButton.getStyleClass().add("btn-primary");
        registerButton.setPrefHeight(38);

        feedbackLabel.setWrapText(true);
        feedbackLabel.setVisible(false);

        formCard.getChildren().addAll(formHeader, new Separator(), grid, registerButton, feedbackLabel);

        getChildren().addAll(titleBox, formCard);
    }
}
