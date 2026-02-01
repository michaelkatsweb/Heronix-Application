package com.heronix.ui.controller;

import com.heronix.service.PollService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class PollResultsDialogController {

    private final PollService pollService;

    @FXML private Label pollTitle;
    @FXML private Label totalResponsesLabel;
    @FXML private Label statusLabel;
    @FXML private VBox resultsContainer;

    @SuppressWarnings("unchecked")
    public void loadResults(Long pollId) {
        try {
            Map<String, Object> results = pollService.getPollResults(pollId);
            pollTitle.setText(String.valueOf(results.getOrDefault("title", "Poll Results")));
            totalResponsesLabel.setText("Total responses: " + results.getOrDefault("totalResponses", 0));
            statusLabel.setText("Status: " + results.getOrDefault("status", ""));

            List<Map<String, Object>> questions = (List<Map<String, Object>>) results.get("questions");
            if (questions == null) return;

            for (int i = 0; i < questions.size(); i++) {
                Map<String, Object> q = questions.get(i);
                VBox qCard = new VBox(5);
                qCard.setPadding(new Insets(10));
                qCard.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #fafafa; -fx-background-radius: 5;");

                Label qLabel = new Label("Q" + (i + 1) + ": " + q.getOrDefault("questionText", ""));
                qLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
                qLabel.setWrapText(true);
                qCard.getChildren().add(qLabel);

                String type = String.valueOf(q.getOrDefault("questionType", ""));
                Label countLabel = new Label("Answers: " + q.getOrDefault("answerCount", 0));
                countLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");
                qCard.getChildren().add(countLabel);

                if ("SHORT_TEXT".equals(type)) {
                    List<String> texts = (List<String>) q.get("textAnswers");
                    if (texts != null) {
                        for (String t : texts) {
                            Label textLabel = new Label("• " + t);
                            textLabel.setWrapText(true);
                            textLabel.setStyle("-fx-font-size: 12;");
                            qCard.getChildren().add(textLabel);
                        }
                    }
                } else {
                    List<Map<String, Object>> options = (List<Map<String, Object>>) q.get("options");
                    if (options != null) {
                        for (Map<String, Object> opt : options) {
                            String optName = String.valueOf(opt.getOrDefault("option", ""));
                            int count = opt.get("count") instanceof Number ? ((Number) opt.get("count")).intValue() : 0;
                            long pct = opt.get("percentage") instanceof Number ? ((Number) opt.get("percentage")).longValue() : 0;

                            HBox row = new HBox(8);
                            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                            Label optLabel = new Label(optName);
                            optLabel.setPrefWidth(120);
                            optLabel.setStyle("-fx-font-size: 12;");
                            ProgressBar bar = new ProgressBar(pct / 100.0);
                            bar.setPrefWidth(200);
                            bar.setPrefHeight(16);
                            Label pctLabel = new Label(count + " (" + pct + "%)");
                            pctLabel.setStyle("-fx-font-size: 11;");
                            row.getChildren().addAll(optLabel, bar, pctLabel);
                            qCard.getChildren().add(row);
                        }
                    }
                }

                resultsContainer.getChildren().add(qCard);
            }
        } catch (Exception e) {
            log.error("Error loading poll results", e);
            resultsContainer.getChildren().add(new Label("Error loading results: " + e.getMessage()));
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) resultsContainer.getScene().getWindow();
        stage.close();
    }
}
