package com.heronix.ui.controller;

import com.heronix.model.domain.Poll;
import com.heronix.model.domain.Poll.TargetAudience;
import com.heronix.model.domain.Poll.ResultsVisibility;
import com.heronix.model.domain.PollQuestion;
import com.heronix.model.domain.PollQuestion.QuestionType;
import com.heronix.service.PollService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class PollEditorDialogController {

    private final PollService pollService;

    @FXML private Label dialogTitle;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> audienceCombo;
    @FXML private CheckBox anonymousCheck;
    @FXML private ComboBox<String> resultsVisibilityCombo;
    @FXML private CheckBox multiResponseCheck;
    @FXML private VBox questionsContainer;
    @FXML private Label errorLabel;

    private Poll editingPoll;
    private Runnable onSave;
    private final List<QuestionEntry> questionEntries = new ArrayList<>();

    @FXML
    public void initialize() {
        audienceCombo.setItems(FXCollections.observableArrayList(
                "STUDENTS", "TEACHERS", "PARENTS", "STAFF", "ALL"));
        audienceCombo.getSelectionModel().select("ALL");

        resultsVisibilityCombo.setItems(FXCollections.observableArrayList(
                "AFTER_VOTING", "AFTER_CLOSE", "NEVER"));
        resultsVisibilityCombo.getSelectionModel().select("AFTER_CLOSE");
    }

    public void setPoll(Poll poll) {
        this.editingPoll = poll;
        if (poll != null) {
            dialogTitle.setText("Edit Poll");
            titleField.setText(poll.getTitle());
            descriptionField.setText(poll.getDescription());
            audienceCombo.getSelectionModel().select(poll.getTargetAudience().name());
            anonymousCheck.setSelected(Boolean.TRUE.equals(poll.getIsAnonymous()));
            resultsVisibilityCombo.getSelectionModel().select(poll.getResultsVisibility().name());
            multiResponseCheck.setSelected(Boolean.TRUE.equals(poll.getAllowMultipleResponses()));

            if (poll.getQuestions() != null) {
                for (PollQuestion q : poll.getQuestions()) {
                    addQuestionEntry(q.getQuestionType(), q.getQuestionText(),
                            q.getOptions(), q.getIsRequired());
                }
            }
        }
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    @FXML private void addMultipleChoice() { addQuestionEntry(QuestionType.MULTIPLE_CHOICE, "", new ArrayList<>(), true); }
    @FXML private void addCheckbox() { addQuestionEntry(QuestionType.CHECKBOX, "", new ArrayList<>(), true); }
    @FXML private void addYesNo() { addQuestionEntry(QuestionType.YES_NO, "", new ArrayList<>(), true); }
    @FXML private void addShortText() { addQuestionEntry(QuestionType.SHORT_TEXT, "", new ArrayList<>(), true); }

    private void addQuestionEntry(QuestionType type, String text, List<String> options, boolean required) {
        QuestionEntry entry = new QuestionEntry();
        entry.type = type;

        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-color: #fafafa; -fx-background-radius: 5;");

        int qNum = questionEntries.size() + 1;
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label typeLabel = new Label("Q" + qNum + " [" + type.name().replace("_", " ") + "]");
        typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        CheckBox reqCheck = new CheckBox("Required");
        reqCheck.setSelected(required);
        entry.requiredCheck = reqCheck;
        Button removeBtn = new Button("Remove");
        removeBtn.setStyle("-fx-font-size: 10; -fx-background-color: #f44336; -fx-text-fill: white;");
        removeBtn.setOnAction(e -> {
            questionsContainer.getChildren().remove(card);
            questionEntries.remove(entry);
        });
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        header.getChildren().addAll(typeLabel, reqCheck, spacer, removeBtn);

        TextField questionText = new TextField(text);
        questionText.setPromptText("Enter question text...");
        entry.questionTextField = questionText;

        card.getChildren().addAll(header, questionText);

        if (type == QuestionType.MULTIPLE_CHOICE || type == QuestionType.CHECKBOX) {
            TextArea optionsArea = new TextArea(String.join("\n", options));
            optionsArea.setPromptText("Enter options, one per line");
            optionsArea.setPrefRowCount(3);
            entry.optionsArea = optionsArea;
            card.getChildren().add(new Label("Options (one per line):"));
            card.getChildren().add(optionsArea);
        }

        entry.card = card;
        questionEntries.add(entry);
        questionsContainer.getChildren().add(card);
    }

    @FXML
    private void handleSave() {
        String title = titleField.getText();
        if (title == null || title.trim().isEmpty()) {
            errorLabel.setText("Title is required");
            return;
        }
        if (questionEntries.isEmpty()) {
            errorLabel.setText("Add at least one question");
            return;
        }

        try {
            Poll poll = editingPoll != null ? editingPoll : new Poll();
            poll.setTitle(title.trim());
            poll.setDescription(descriptionField.getText());
            poll.setTargetAudience(TargetAudience.valueOf(audienceCombo.getValue()));
            poll.setIsAnonymous(anonymousCheck.isSelected());
            poll.setAllowMultipleResponses(multiResponseCheck.isSelected());
            poll.setResultsVisibility(ResultsVisibility.valueOf(resultsVisibilityCombo.getValue()));
            poll.setCreatorName("Admin"); // SIS Desktop is admin
            poll.setCreatorType("ADMIN");

            List<PollQuestion> questions = new ArrayList<>();
            for (int i = 0; i < questionEntries.size(); i++) {
                QuestionEntry entry = questionEntries.get(i);
                String qText = entry.questionTextField.getText();
                if (qText == null || qText.trim().isEmpty()) {
                    errorLabel.setText("Question " + (i + 1) + " text is required");
                    return;
                }
                PollQuestion q = new PollQuestion();
                q.setQuestionText(qText.trim());
                q.setQuestionType(entry.type);
                q.setDisplayOrder(i);
                q.setIsRequired(entry.requiredCheck.isSelected());

                if (entry.type == QuestionType.MULTIPLE_CHOICE || entry.type == QuestionType.CHECKBOX) {
                    String optText = entry.optionsArea != null ? entry.optionsArea.getText() : "";
                    List<String> opts = Arrays.stream(optText.split("\n"))
                            .map(String::trim).filter(s -> !s.isEmpty())
                            .collect(java.util.stream.Collectors.toList());
                    if (opts.size() < 2) {
                        errorLabel.setText("Question " + (i + 1) + " needs at least 2 options");
                        return;
                    }
                    q.setOptions(opts);
                } else if (entry.type == QuestionType.YES_NO) {
                    q.setOptions(List.of("Yes", "No"));
                }

                questions.add(q);
            }
            poll.setQuestions(questions);

            if (editingPoll != null && editingPoll.getId() != null) {
                pollService.updatePoll(editingPoll.getId(), poll);
            } else {
                pollService.createPoll(poll);
            }

            if (onSave != null) onSave.run();
            closeDialog();
        } catch (Exception e) {
            log.error("Error saving poll", e);
            errorLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private static class QuestionEntry {
        QuestionType type;
        TextField questionTextField;
        TextArea optionsArea;
        CheckBox requiredCheck;
        VBox card;
    }
}
