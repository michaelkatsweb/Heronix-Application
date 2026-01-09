package com.heronix.ui.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class Plan504ManagementController {

    @FXML private TableView<Plan504> plan504TableView;
    @FXML private TableColumn<Plan504, String> studentColumn;
    @FXML private TableColumn<Plan504, String> statusColumn;
    @FXML private TableColumn<Plan504, String> accommodationsColumn;
    @FXML private TableColumn<Plan504, String> nextReviewColumn;

    private ObservableList<Plan504> plans = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up table columns
        studentColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        accommodationsColumn.setCellValueFactory(new PropertyValueFactory<>("accommodations"));
        nextReviewColumn.setCellValueFactory(new PropertyValueFactory<>("nextReview"));

        // Style status column with color coding
        statusColumn.setCellFactory(column -> new TableCell<Plan504, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = "-fx-padding: 5; -fx-border-radius: 3; -fx-background-radius: 3; ";
                    switch (item) {
                        case "Active":
                            style += "-fx-background-color: #4caf50; -fx-text-fill: white;";
                            break;
                        case "Under Review":
                            style += "-fx-background-color: #ff9800; -fx-text-fill: white;";
                            break;
                        case "Expired":
                            style += "-fx-background-color: #f44336; -fx-text-fill: white;";
                            break;
                        case "Draft":
                            style += "-fx-background-color: #9e9e9e; -fx-text-fill: white;";
                            break;
                    }
                    setStyle(style);
                }
            }
        });

        // Load sample data
        loadSampleData();
        plan504TableView.setItems(plans);
    }

    private void loadSampleData() {
        plans.addAll(
            new Plan504("Emily Rodriguez", "Active", "Extended time on tests, Preferential seating", "2025-05-15"),
            new Plan504("Michael Chen", "Active", "Breaks as needed, Note-taking assistance", "2025-06-20"),
            new Plan504("Sarah Johnson", "Under Review", "Modified assignments, Audio materials", "2025-03-10"),
            new Plan504("David Williams", "Active", "Extended time, Calculator use", "2025-07-01"),
            new Plan504("Jessica Brown", "Draft", "Flexible scheduling, Reduced workload", "2025-02-28"),
            new Plan504("Christopher Lee", "Active", "Preferential seating, Extended time", "2025-08-12"),
            new Plan504("Amanda Taylor", "Expired", "Assistive technology, Extra breaks", "2024-12-15"),
            new Plan504("Ryan Martinez", "Active", "Modified homework, Alternative assessments", "2025-04-25")
        );
    }

    @FXML
    private void handleCreate504Plan() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create 504 Plan");
        dialog.setHeaderText("New 504 Plan Wizard");

        // Create form layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        TextField studentField = new TextField();
        studentField.setPromptText("Student name");

        ComboBox<String> statusCombo = new ComboBox<>(
            FXCollections.observableArrayList("Draft", "Under Review", "Active")
        );
        statusCombo.setValue("Draft");

        TextArea accommodationsArea = new TextArea();
        accommodationsArea.setPromptText("Enter accommodations (comma separated)");
        accommodationsArea.setPrefRowCount(4);

        DatePicker reviewDatePicker = new DatePicker(LocalDate.now().plusMonths(6));

        grid.add(new Label("Student:"), 0, 0);
        grid.add(studentField, 1, 0);
        grid.add(new Label("Initial Status:"), 0, 1);
        grid.add(statusCombo, 1, 1);
        grid.add(new Label("Accommodations:"), 0, 2);
        grid.add(accommodationsArea, 1, 2);
        grid.add(new Label("Next Review Date:"), 0, 3);
        grid.add(reviewDatePicker, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String studentName = studentField.getText();
                String status = statusCombo.getValue();
                String accommodations = accommodationsArea.getText();
                String reviewDate = reviewDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                if (!studentName.isEmpty() && !accommodations.isEmpty()) {
                    Plan504 newPlan = new Plan504(studentName, status, accommodations, reviewDate);
                    plans.add(newPlan);

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("504 Plan Created");
                    successAlert.setContentText("504 Plan for " + studentName + " has been created successfully.");
                    successAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Validation Error");
                    errorAlert.setContentText("Please fill in all required fields.");
                    errorAlert.showAndWait();
                }
            }
        });
    }

    // Inner class for 504 Plan data model
    public static class Plan504 {
        private final SimpleStringProperty studentName;
        private final SimpleStringProperty status;
        private final SimpleStringProperty accommodations;
        private final SimpleStringProperty nextReview;

        public Plan504(String studentName, String status, String accommodations, String nextReview) {
            this.studentName = new SimpleStringProperty(studentName);
            this.status = new SimpleStringProperty(status);
            this.accommodations = new SimpleStringProperty(accommodations);
            this.nextReview = new SimpleStringProperty(nextReview);
        }

        public String getStudentName() { return studentName.get(); }
        public void setStudentName(String value) { studentName.set(value); }
        public SimpleStringProperty studentNameProperty() { return studentName; }

        public String getStatus() { return status.get(); }
        public void setStatus(String value) { status.set(value); }
        public SimpleStringProperty statusProperty() { return status; }

        public String getAccommodations() { return accommodations.get(); }
        public void setAccommodations(String value) { accommodations.set(value); }
        public SimpleStringProperty accommodationsProperty() { return accommodations; }

        public String getNextReview() { return nextReview.get(); }
        public void setNextReview(String value) { nextReview.set(value); }
        public SimpleStringProperty nextReviewProperty() { return nextReview; }
    }
}
