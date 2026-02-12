package com.heronix.ui.controller;

import com.heronix.model.domain.Poll;
import com.heronix.model.domain.Poll.PollStatus;
import com.heronix.model.domain.Poll.TargetAudience;
import com.heronix.service.PollService;
import com.heronix.util.ResponsiveDesignHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PollManagementController {

    private final PollService pollService;
    private final ApplicationContext applicationContext;

    @FXML private Label statTotal;
    @FXML private Label statDraft;
    @FXML private Label statPublished;
    @FXML private Label statClosed;

    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> audienceFilter;
    @FXML private TextField searchField;
    @FXML private Label recordCountLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<Poll> pollsTable;
    @FXML private TableColumn<Poll, String> titleColumn;
    @FXML private TableColumn<Poll, String> creatorColumn;
    @FXML private TableColumn<Poll, String> audienceColumn;
    @FXML private TableColumn<Poll, String> statusColumn;
    @FXML private TableColumn<Poll, String> questionsColumn;
    @FXML private TableColumn<Poll, String> responsesColumn;
    @FXML private TableColumn<Poll, String> createdColumn;
    @FXML private TableColumn<Poll, Void> actionsColumn;

    private final ObservableList<Poll> allPolls = FXCollections.observableArrayList();
    private final ObservableList<Poll> filteredPolls = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @FXML
    public void initialize() {
        setupFilters();
        setupTableColumns();
        setupActionsColumn();
        pollsTable.setItems(filteredPolls);
        loadPolls();
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList(
                "All", "DRAFT", "PUBLISHED", "CLOSED", "ARCHIVED"));
        statusFilter.getSelectionModel().selectFirst();
        statusFilter.setOnAction(e -> applyFilters());

        audienceFilter.setItems(FXCollections.observableArrayList(
                "All", "STUDENTS", "TEACHERS", "PARENTS", "STAFF", "ALL"));
        audienceFilter.getSelectionModel().selectFirst();
        audienceFilter.setOnAction(e -> applyFilters());

        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void setupTableColumns() {
        titleColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTitle()));
        creatorColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getCreatorName() != null ? cd.getValue().getCreatorName() : ""));
        audienceColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getTargetAudience() != null ? cd.getValue().getTargetAudience().name() : ""));
        statusColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getStatus() != null ? cd.getValue().getStatus().name() : ""));
        questionsColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                String.valueOf(cd.getValue().getQuestions() != null ? cd.getValue().getQuestions().size() : 0)));
        responsesColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                String.valueOf(cd.getValue().getTotalResponses() != null ? cd.getValue().getTotalResponses() : 0)));
        createdColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getCreatedAt() != null ? cd.getValue().getCreatedAt().format(DATE_FMT) : ""));
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button publishBtn = new Button("Publish");
            private final Button closeBtn = new Button("Close");
            private final Button resultsBtn = new Button("Results");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.setStyle("-fx-font-size: 10;");
                publishBtn.setStyle("-fx-font-size: 10; -fx-background-color: #4CAF50; -fx-text-fill: white;");
                closeBtn.setStyle("-fx-font-size: 10; -fx-background-color: #FF9800; -fx-text-fill: white;");
                resultsBtn.setStyle("-fx-font-size: 10; -fx-background-color: #2196F3; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-font-size: 10; -fx-background-color: #f44336; -fx-text-fill: white;");

                editBtn.setOnAction(e -> handleEditPoll(getTableView().getItems().get(getIndex())));
                publishBtn.setOnAction(e -> handlePublishPoll(getTableView().getItems().get(getIndex())));
                closeBtn.setOnAction(e -> handleClosePoll(getTableView().getItems().get(getIndex())));
                resultsBtn.setOnAction(e -> handleViewResults(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDeletePoll(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Poll poll = getTableView().getItems().get(getIndex());
                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(3);
                    if (poll.getStatus() == PollStatus.DRAFT) {
                        box.getChildren().addAll(editBtn, publishBtn, deleteBtn);
                    } else if (poll.getStatus() == PollStatus.PUBLISHED) {
                        box.getChildren().addAll(closeBtn, resultsBtn);
                    } else {
                        box.getChildren().addAll(resultsBtn, deleteBtn);
                    }
                    setGraphic(box);
                }
            }
        });
    }

    private void loadPolls() {
        try {
            List<Poll> polls = pollService.getAllPolls();
            allPolls.setAll(polls);
            applyFilters();
            updateStats();
            statusLabel.setText("");
        } catch (Exception e) {
            log.error("Failed to load polls", e);
            statusLabel.setText("Error loading polls: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String statusVal = statusFilter.getValue();
        String audienceVal = audienceFilter.getValue();
        String search = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";

        List<Poll> filtered = allPolls.stream()
                .filter(p -> statusVal == null || "All".equals(statusVal)
                        || p.getStatus().name().equals(statusVal))
                .filter(p -> audienceVal == null || "All".equals(audienceVal)
                        || p.getTargetAudience().name().equals(audienceVal))
                .filter(p -> search.isEmpty()
                        || (p.getTitle() != null && p.getTitle().toLowerCase().contains(search))
                        || (p.getDescription() != null && p.getDescription().toLowerCase().contains(search)))
                .collect(Collectors.toList());

        filteredPolls.setAll(filtered);
        recordCountLabel.setText(filtered.size() + " poll" + (filtered.size() != 1 ? "s" : ""));
    }

    private void updateStats() {
        long total = allPolls.size();
        long draft = allPolls.stream().filter(p -> p.getStatus() == PollStatus.DRAFT).count();
        long published = allPolls.stream().filter(p -> p.getStatus() == PollStatus.PUBLISHED).count();
        long closed = allPolls.stream().filter(p -> p.getStatus() == PollStatus.CLOSED).count();
        statTotal.setText(String.valueOf(total));
        statDraft.setText(String.valueOf(draft));
        statPublished.setText(String.valueOf(published));
        statClosed.setText(String.valueOf(closed));
    }

    @FXML
    private void handleCreatePoll() {
        openPollEditor(null);
    }

    private void handleEditPoll(Poll poll) {
        openPollEditor(poll);
    }

    private void openPollEditor(Poll poll) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PollEditorDialog.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            PollEditorDialogController controller = loader.getController();
            controller.setPoll(poll);
            controller.setOnSave(() -> loadPolls());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(poll == null ? "Create New Poll" : "Edit Poll");
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            ResponsiveDesignHelper.makeDialogResponsive(stage);
            stage.showAndWait();
        } catch (Exception e) {
            log.error("Error opening poll editor", e);
        }
    }

    private void handlePublishPoll(Poll poll) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Publish this poll? It will become visible to the target audience.");
        alert.setHeaderText("Publish: " + poll.getTitle());
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    pollService.publishPoll(poll.getId());
                    loadPolls();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
                }
            }
        });
    }

    private void handleClosePoll(Poll poll) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Close this poll? No more responses will be accepted.");
        alert.setHeaderText("Close: " + poll.getTitle());
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    pollService.closePoll(poll.getId());
                    loadPolls();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
                }
            }
        });
    }

    private void handleDeletePoll(Poll poll) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete this poll? This action cannot be undone.");
        alert.setHeaderText("Delete: " + poll.getTitle());
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    pollService.deletePoll(poll.getId());
                    loadPolls();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
                }
            }
        });
    }

    private void handleViewResults(Poll poll) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PollResultsDialog.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            PollResultsDialogController controller = loader.getController();
            controller.loadResults(poll.getId());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Poll Results: " + poll.getTitle());
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            ResponsiveDesignHelper.makeDialogResponsive(stage);
            stage.showAndWait();
        } catch (Exception e) {
            log.error("Error opening results", e);
        }
    }

    @FXML
    private void handleRefresh() {
        loadPolls();
    }
}
