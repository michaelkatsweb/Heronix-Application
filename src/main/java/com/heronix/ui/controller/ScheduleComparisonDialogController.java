package com.heronix.ui.controller;

import com.heronix.model.domain.Schedule;
import com.heronix.service.ScheduleService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for Schedule Comparison Dialog
 * Allows administrators to compare two schedules side-by-side
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Component
public class ScheduleComparisonDialogController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RestTemplate restTemplate;

    // FXML Components - Schedule Selection
    @FXML private ComboBox<Schedule> schedule1ComboBox;
    @FXML private ComboBox<Schedule> schedule2ComboBox;
    @FXML private Text schedule1InfoText;
    @FXML private Text schedule2InfoText;

    // FXML Components - Comparison Results
    @FXML private VBox comparisonResultsBox;
    @FXML private Label schedule1NameLabel;
    @FXML private Label schedule2NameLabel;

    // FXML Components - Metrics
    @FXML private Label schedule1HardScoreLabel;
    @FXML private Label schedule2HardScoreLabel;
    @FXML private Label hardScoreWinnerLabel;

    @FXML private Label schedule1SoftScoreLabel;
    @FXML private Label schedule2SoftScoreLabel;
    @FXML private Label softScoreWinnerLabel;

    @FXML private Label schedule1ConflictsLabel;
    @FXML private Label schedule2ConflictsLabel;
    @FXML private Label conflictsWinnerLabel;

    @FXML private Label schedule1MethodLabel;
    @FXML private Label schedule2MethodLabel;

    // FXML Components - Chart & Recommendation
    @FXML private BarChart<String, Number> comparisonChart;
    @FXML private Text recommendationText;

    // FXML Components - Loading
    @FXML private VBox loadingBox;

    private DialogPane dialogPane;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        log.info("Initializing Schedule Comparison Dialog");

        // Set up schedule combo boxes
        setupScheduleComboBox(schedule1ComboBox, schedule1InfoText);
        setupScheduleComboBox(schedule2ComboBox, schedule2InfoText);

        // Load schedules
        loadSchedules();
    }

    /**
     * Set up a schedule combo box with cell factory and listener
     */
    private void setupScheduleComboBox(ComboBox<Schedule> comboBox, Text infoText) {
        comboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Schedule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String year = item.getStartDate() != null ?
                        String.valueOf(item.getStartDate().getYear()) : "Unknown";
                    setText(item.getScheduleName() + " (" + year + ")");
                }
            }
        });

        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Schedule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String year = item.getStartDate() != null ?
                        String.valueOf(item.getStartDate().getYear()) : "Unknown";
                    setText(item.getScheduleName() + " (" + year + ")");
                }
            }
        });

        comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateScheduleInfo(newVal, infoText);
            }
        });
    }

    /**
     * Set the dialog pane reference
     */
    public void setDialogPane(DialogPane pane) {
        this.dialogPane = pane;
    }

    /**
     * Load schedules from database
     */
    private void loadSchedules() {
        try {
            List<Schedule> schedules = scheduleService.getAllSchedules();
            schedule1ComboBox.getItems().setAll(schedules);
            schedule2ComboBox.getItems().setAll(schedules);

            if (schedules.size() >= 2) {
                schedule1ComboBox.getSelectionModel().select(0);
                schedule2ComboBox.getSelectionModel().select(1);
            }
        } catch (Exception e) {
            log.error("Error loading schedules", e);
            showError("Failed to load schedules: " + e.getMessage());
        }
    }

    /**
     * Update schedule info text
     */
    private void updateScheduleInfo(Schedule schedule, Text infoText) {
        if (schedule == null) {
            infoText.setText("");
            return;
        }

        String info = String.format(
            "Type: %s | Slots: %d | Status: %s",
            schedule.getScheduleType() != null ? schedule.getScheduleType().getDisplayName() : "Unknown",
            schedule.getSlots() != null ? schedule.getSlots().size() : 0,
            schedule.getActive() ? "Active" : "Inactive"
        );

        infoText.setText(info);
    }

    /**
     * Compare the selected schedules
     */
    public void compareSchedules() {
        Schedule schedule1 = schedule1ComboBox.getValue();
        Schedule schedule2 = schedule2ComboBox.getValue();

        if (schedule1 == null || schedule2 == null) {
            showError("Please select both schedules to compare");
            return;
        }

        if (schedule1.getId().equals(schedule2.getId())) {
            showError("Please select two different schedules");
            return;
        }

        // Show loading
        loadingBox.setVisible(true);
        loadingBox.setManaged(true);
        comparisonResultsBox.setVisible(false);
        comparisonResultsBox.setManaged(false);

        // Disable compare button
        if (dialogPane != null) {
            Button compareButton = (Button) dialogPane.lookupButton(
                dialogPane.getButtonTypes().stream()
                    .filter(bt -> "Compare Schedules".equals(bt.getText()))
                    .findFirst()
                    .orElse(ButtonType.OK)
            );
            if (compareButton != null) {
                compareButton.setDisable(true);
            }
        }

        // Compare in background
        CompletableFuture.runAsync(() -> {
            try {
                // Call comparison endpoint
                Map<String, Object> comparisonResult = performComparison(schedule1, schedule2);

                Platform.runLater(() -> {
                    displayComparisonResults(schedule1, schedule2, comparisonResult);

                    // Hide loading, show results
                    loadingBox.setVisible(false);
                    loadingBox.setManaged(false);
                    comparisonResultsBox.setVisible(true);
                    comparisonResultsBox.setManaged(true);

                    // Re-enable button
                    if (dialogPane != null) {
                        Button compareButton = (Button) dialogPane.lookupButton(
                            dialogPane.getButtonTypes().stream()
                                .filter(bt -> "Compare Schedules".equals(bt.getText()))
                                .findFirst()
                                .orElse(ButtonType.OK)
                        );
                        if (compareButton != null) {
                            compareButton.setDisable(false);
                        }
                    }
                });

            } catch (Exception e) {
                log.error("Error comparing schedules", e);
                Platform.runLater(() -> {
                    loadingBox.setVisible(false);
                    loadingBox.setManaged(false);
                    showError("Failed to compare schedules: " + e.getMessage());
                });
            }
        });
    }

    /**
     * Perform comparison via API endpoint
     */
    private Map<String, Object> performComparison(Schedule schedule1, Schedule schedule2) {
        try {
            // Prepare request
            Map<String, Long> request = new HashMap<>();
            request.put("schedule1Id", schedule1.getId());
            request.put("schedule2Id", schedule2.getId());

            // Call API endpoint
            String url = "http://localhost:8081/api/schedule-generation/compare";
            HttpEntity<Map<String, Long>> entity = new HttpEntity<>(request);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Empty response from comparison endpoint");
            }

        } catch (Exception e) {
            log.warn("API comparison failed, using local comparison: {}", e.getMessage());
            // Fallback to local comparison
            return performLocalComparison(schedule1, schedule2);
        }
    }

    /**
     * Perform local comparison if API is unavailable
     */
    private Map<String, Object> performLocalComparison(Schedule schedule1, Schedule schedule2) {
        Map<String, Object> result = new HashMap<>();

        // Extract metrics from schedules (using available fields)
        // Note: Schedule doesn't have hardScore/softScore - using totalConflicts and qualityScore instead
        Integer conflicts1 = schedule1.getTotalConflicts() != null ? schedule1.getTotalConflicts() : 0;
        Integer conflicts2 = schedule2.getTotalConflicts() != null ? schedule2.getTotalConflicts() : 0;

        // Use qualityScore as soft score proxy
        Integer quality1 = schedule1.getQualityScore() != null ? schedule1.getQualityScore().intValue() : 0;
        Integer quality2 = schedule2.getQualityScore() != null ? schedule2.getQualityScore().intValue() : 0;

        // Hard score = negative of total conflicts (0 is best)
        Integer hard1 = -conflicts1;
        Integer hard2 = -conflicts2;

        // Count slots with conflicts
        int slotConflicts1 = schedule1.getSlots() != null ?
            (int) schedule1.getSlots().stream()
                .filter(slot -> slot.getHasConflict() != null && slot.getHasConflict())
                .count() : 0;

        int slotConflicts2 = schedule2.getSlots() != null ?
            (int) schedule2.getSlots().stream()
                .filter(slot -> slot.getHasConflict() != null && slot.getHasConflict())
                .count() : 0;

        // Create comparison result
        result.put("schedule1HardScore", hard1);
        result.put("schedule2HardScore", hard2);
        result.put("schedule1SoftScore", quality1);
        result.put("schedule2SoftScore", quality2);
        result.put("schedule1Conflicts", slotConflicts1);
        result.put("schedule2Conflicts", slotConflicts2);
        result.put("schedule1Method", schedule1.getStatus() != null ? schedule1.getStatus().toString() : "Unknown");
        result.put("schedule2Method", schedule2.getStatus() != null ? schedule2.getStatus().toString() : "Unknown");

        // Generate recommendation
        String recommendation = generateRecommendation(hard1, hard2, quality1, quality2, slotConflicts1, slotConflicts2);
        result.put("recommendation", recommendation);
        result.put("winnerScheduleId", determineWinner(hard1, hard2, quality1, quality2));

        return result;
    }

    /**
     * Display comparison results
     */
    private void displayComparisonResults(Schedule schedule1, Schedule schedule2, Map<String, Object> result) {
        // Update schedule names
        schedule1NameLabel.setText(schedule1.getScheduleName());
        schedule2NameLabel.setText(schedule2.getScheduleName());

        // Extract metrics
        Integer hard1 = getIntValue(result, "schedule1HardScore");
        Integer hard2 = getIntValue(result, "schedule2HardScore");
        Integer soft1 = getIntValue(result, "schedule1SoftScore");
        Integer soft2 = getIntValue(result, "schedule2SoftScore");
        Integer conflicts1 = getIntValue(result, "schedule1Conflicts");
        Integer conflicts2 = getIntValue(result, "schedule2Conflicts");
        String method1 = getStringValue(result, "schedule1Method");
        String method2 = getStringValue(result, "schedule2Method");

        // Update metrics labels
        updateMetricLabels(schedule1HardScoreLabel, schedule2HardScoreLabel, hardScoreWinnerLabel,
            hard1, hard2, true); // true = lower is better
        updateMetricLabels(schedule1SoftScoreLabel, schedule2SoftScoreLabel, softScoreWinnerLabel,
            soft1, soft2, false); // false = higher is better
        updateMetricLabels(schedule1ConflictsLabel, schedule2ConflictsLabel, conflictsWinnerLabel,
            conflicts1, conflicts2, true); // true = lower is better

        schedule1MethodLabel.setText(method1);
        schedule2MethodLabel.setText(method2);

        // Update chart
        updateComparisonChart(hard1, hard2, soft1, soft2, conflicts1, conflicts2);

        // Update recommendation
        String recommendation = getStringValue(result, "recommendation");
        if (recommendation == null || recommendation.isEmpty()) {
            recommendation = generateRecommendation(hard1, hard2, soft1, soft2, conflicts1, conflicts2);
        }
        recommendationText.setText(recommendation);
    }

    /**
     * Update metric labels and determine winner
     */
    private void updateMetricLabels(Label label1, Label label2, Label winnerLabel,
                                    Integer value1, Integer value2, boolean lowerIsBetter) {
        label1.setText(String.valueOf(value1));
        label2.setText(String.valueOf(value2));

        String winner;
        if (value1.equals(value2)) {
            winner = "Tie";
            winnerLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #95a5a6;");
        } else if ((lowerIsBetter && value1 < value2) || (!lowerIsBetter && value1 > value2)) {
            winner = "Schedule 1";
            winnerLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9;");
        } else {
            winner = "Schedule 2";
            winnerLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
        }

        winnerLabel.setText(winner);
    }

    /**
     * Update comparison chart
     */
    private void updateComparisonChart(Integer hard1, Integer hard2, Integer soft1, Integer soft2,
                                       Integer conflicts1, Integer conflicts2) {
        comparisonChart.getData().clear();

        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName(schedule1ComboBox.getValue().getScheduleName());
        series1.getData().add(new XYChart.Data<>("Hard Score", Math.abs(hard1)));
        series1.getData().add(new XYChart.Data<>("Soft Score", soft1));
        series1.getData().add(new XYChart.Data<>("Conflicts", conflicts1));

        XYChart.Series<String, Number> series2 = new XYChart.Series<>();
        series2.setName(schedule2ComboBox.getValue().getScheduleName());
        series2.getData().add(new XYChart.Data<>("Hard Score", Math.abs(hard2)));
        series2.getData().add(new XYChart.Data<>("Soft Score", soft2));
        series2.getData().add(new XYChart.Data<>("Conflicts", conflicts2));

        comparisonChart.getData().addAll(series1, series2);
    }

    /**
     * Generate recommendation text
     */
    private String generateRecommendation(Integer hard1, Integer hard2, Integer soft1, Integer soft2,
                                          Integer conflicts1, Integer conflicts2) {
        StringBuilder recommendation = new StringBuilder();

        // Determine overall winner
        Long winnerId = determineWinner(hard1, hard2, soft1, soft2);
        String winnerName = winnerId.equals(schedule1ComboBox.getValue().getId()) ?
            schedule1ComboBox.getValue().getScheduleName() :
            schedule2ComboBox.getValue().getScheduleName();

        recommendation.append("Based on the comparison metrics, we recommend using ")
            .append(winnerName).append(".\n\n");

        // Add reasoning
        if (hard1 == 0 && hard2 > 0) {
            recommendation.append("• Schedule 1 has no hard conflicts, making it feasible, while Schedule 2 has ")
                .append(hard2).append(" conflicts.\n");
        } else if (hard2 == 0 && hard1 > 0) {
            recommendation.append("• Schedule 2 has no hard conflicts, making it feasible, while Schedule 1 has ")
                .append(hard1).append(" conflicts.\n");
        } else if (hard1 == 0 && hard2 == 0) {
            recommendation.append("• Both schedules have no hard conflicts, so we compare soft scores.\n");
            if (soft1 > soft2) {
                recommendation.append("• Schedule 1 has a higher soft score (")
                    .append(soft1).append(" vs ").append(soft2)
                    .append("), indicating better optimization.\n");
            } else if (soft2 > soft1) {
                recommendation.append("• Schedule 2 has a higher soft score (")
                    .append(soft2).append(" vs ").append(soft1)
                    .append("), indicating better optimization.\n");
            }
        }

        if (conflicts1 < conflicts2) {
            recommendation.append("• Schedule 1 has fewer total conflicts (")
                .append(conflicts1).append(" vs ").append(conflicts2).append(").\n");
        } else if (conflicts2 < conflicts1) {
            recommendation.append("• Schedule 2 has fewer total conflicts (")
                .append(conflicts2).append(" vs ").append(conflicts1).append(").\n");
        }

        return recommendation.toString();
    }

    /**
     * Determine winner schedule ID
     */
    private Long determineWinner(Integer hard1, Integer hard2, Integer soft1, Integer soft2) {
        // Hard score is most important (0 conflicts = feasible)
        if (hard1 == 0 && hard2 > 0) {
            return schedule1ComboBox.getValue().getId();
        } else if (hard2 == 0 && hard1 > 0) {
            return schedule2ComboBox.getValue().getId();
        }

        // If both have 0 hard score, or both have conflicts, compare soft score
        if (soft1 > soft2) {
            return schedule1ComboBox.getValue().getId();
        } else if (soft2 > soft1) {
            return schedule2ComboBox.getValue().getId();
        }

        // If tied, return schedule1
        return schedule1ComboBox.getValue().getId();
    }

    /**
     * Validate form
     */
    public boolean validate() {
        if (schedule1ComboBox.getValue() == null) {
            showError("Please select Schedule 1");
            return false;
        }

        if (schedule2ComboBox.getValue() == null) {
            showError("Please select Schedule 2");
            return false;
        }

        if (schedule1ComboBox.getValue().getId().equals(schedule2ComboBox.getValue().getId())) {
            showError("Please select two different schedules");
            return false;
        }

        return true;
    }

    /**
     * Helper to get Integer value from map
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    /**
     * Helper to get String value from map
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "Unknown";
    }

    /**
     * Show error alert
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
