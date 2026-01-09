package com.heronix.ui.controller;

import com.heronix.integration.SchedulerApiClient;
import com.heronix.service.integration.ScheduleGenerationModeService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Controller for Schedule Generation Progress Dialog
 * Displays real-time progress of AI-powered schedule optimization
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Component
public class ScheduleGenerationProgressController {

    @Autowired
    private SchedulerApiClient schedulerApiClient;

    // FXML Components - Info
    @FXML private Label scheduleNameLabel;
    @FXML private Label generationModeLabel;
    @FXML private Label jobIdLabel;

    // FXML Components - Progress
    @FXML private Label statusIcon;
    @FXML private Label statusLabel;
    @FXML private Label statusMessage;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressPercentLabel;
    @FXML private Label timeElapsedLabel;

    // FXML Components - Metrics
    @FXML private VBox metricsBox;
    @FXML private Label hardScoreLabel;
    @FXML private Label softScoreLabel;
    @FXML private Label conflictsLabel;
    @FXML private Label qualityLabel;

    // FXML Components - Result
    @FXML private VBox resultBox;
    @FXML private Label sectionsCreatedLabel;
    @FXML private Label studentsScheduledLabel;
    @FXML private Label finalHardScoreLabel;
    @FXML private Label finalSoftScoreLabel;

    // FXML Components - Error
    @FXML private VBox errorBox;
    @FXML private javafx.scene.text.Text errorMessageText;

    // State
    private String jobId;
    private String scheduleName;
    private String mode;
    private boolean cancelled = false;
    private ScheduledExecutorService scheduler;
    private DialogPane dialogPane;
    private ScheduleGenerationModeService.ScheduleGenerationResult finalResult;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        log.info("Initializing Schedule Generation Progress Dialog");
    }

    /**
     * Set the dialog pane reference
     */
    public void setDialogPane(DialogPane pane) {
        this.dialogPane = pane;

        // Initially hide close button, show cancel
        Button closeButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (closeButton != null) {
            closeButton.setVisible(false);
            closeButton.setManaged(false);
        }
    }

    /**
     * Start monitoring a schedule generation job
     */
    public void startMonitoring(String jobId, String scheduleName, String mode) {
        this.jobId = jobId;
        this.scheduleName = scheduleName;
        this.mode = mode;

        // Update UI with job info
        scheduleNameLabel.setText(scheduleName);
        generationModeLabel.setText(mode);
        jobIdLabel.setText(jobId);

        // Show metrics box
        metricsBox.setVisible(true);
        metricsBox.setManaged(true);

        // Start polling
        startPolling();
    }

    /**
     * Start polling the job status
     */
    private void startPolling() {
        scheduler = Executors.newScheduledThreadPool(1);

        // Poll every 2 seconds
        scheduler.scheduleAtFixedRate(() -> {
            if (!cancelled) {
                pollJobStatus();
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    /**
     * Poll the job status from SchedulerV2
     */
    private void pollJobStatus() {
        try {
            SchedulerApiClient.ScheduleJobStatus status = schedulerApiClient.getJobStatus(jobId);

            Platform.runLater(() -> updateUI(status));

            // Check if job is complete
            if ("COMPLETED".equals(status.getStatus()) ||
                "FAILED".equals(status.getStatus()) ||
                "ERROR".equals(status.getStatus())) {

                stopPolling();

                if ("COMPLETED".equals(status.getStatus())) {
                    Platform.runLater(() -> showCompletedState(status));
                } else {
                    Platform.runLater(() -> showErrorState(status));
                }
            }

        } catch (Exception e) {
            log.error("Error polling job status", e);
            Platform.runLater(() -> {
                statusLabel.setText("Error checking status");
                statusMessage.setText("Failed to communicate with SchedulerV2: " + e.getMessage());
                statusIcon.setText("⚠️");
            });
        }
    }

    /**
     * Update UI with current job status
     */
    private void updateUI(SchedulerApiClient.ScheduleJobStatus status) {
        // Update progress
        double progress = status.getProgress() != null ? status.getProgress() / 100.0 : 0.0;
        progressBar.setProgress(progress);
        progressPercentLabel.setText(String.format("%.0f%%", status.getProgress() != null ? status.getProgress() : 0));

        // Update time
        if (status.getElapsedSeconds() != null) {
            timeElapsedLabel.setText(status.getElapsedSeconds() + " seconds");
        }

        // Update status message
        if (status.getMessage() != null) {
            statusMessage.setText(status.getMessage());
        }

        // Update metrics
        if (status.getHardScore() != null) {
            hardScoreLabel.setText(String.valueOf(status.getHardScore()));
            hardScoreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " +
                (status.getHardScore() == 0 ? "#27ae60" : "#e74c3c") + ";");
        }

        if (status.getSoftScore() != null) {
            softScoreLabel.setText(String.valueOf(status.getSoftScore()));
        }

        // Calculate quality indicator
        if (status.getHardScore() != null && status.getSoftScore() != null) {
            String quality = status.getHardScore() == 0 ? "Good" : "Has Conflicts";
            qualityLabel.setText(quality);
            qualityLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " +
                (status.getHardScore() == 0 ? "#27ae60" : "#e67e22") + ";");
        }

        // Update status icon based on progress
        if (progress < 0.33) {
            statusIcon.setText("⏳");
            statusLabel.setText("Initializing optimization...");
        } else if (progress < 0.66) {
            statusIcon.setText("🔄");
            statusLabel.setText("Optimizing schedule...");
        } else if (progress < 1.0) {
            statusIcon.setText("⚙️");
            statusLabel.setText("Finalizing results...");
        }
    }

    /**
     * Show completed state
     */
    private void showCompletedState(SchedulerApiClient.ScheduleJobStatus status) {
        statusIcon.setText("✅");
        statusLabel.setText("Schedule Generation Complete!");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        statusMessage.setText("Your optimized schedule is ready.");

        progressBar.setProgress(1.0);
        progressPercentLabel.setText("100%");

        // Hide metrics, show results
        metricsBox.setVisible(false);
        metricsBox.setManaged(false);

        resultBox.setVisible(true);
        resultBox.setManaged(true);

        // Populate result data (you would get this from import result)
        finalHardScoreLabel.setText(status.getHardScore() != null ? String.valueOf(status.getHardScore()) : "N/A");
        finalSoftScoreLabel.setText(status.getSoftScore() != null ? String.valueOf(status.getSoftScore()) : "N/A");

        // Show close button, hide cancel
        if (dialogPane != null) {
            Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
            Button closeButton = (Button) dialogPane.lookupButton(ButtonType.OK);

            if (cancelButton != null) {
                cancelButton.setVisible(false);
                cancelButton.setManaged(false);
            }
            if (closeButton != null) {
                closeButton.setVisible(true);
                closeButton.setManaged(true);
            }
        }
    }

    /**
     * Show error state
     */
    private void showErrorState(SchedulerApiClient.ScheduleJobStatus status) {
        statusIcon.setText("❌");
        statusLabel.setText("Generation Failed");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        statusMessage.setText("An error occurred during schedule generation.");

        // Hide metrics
        metricsBox.setVisible(false);
        metricsBox.setManaged(false);

        // Show error box
        errorBox.setVisible(true);
        errorBox.setManaged(true);

        String errorMessage = status.getMessage() != null ?
            status.getMessage() :
            "Unknown error occurred. Please check SchedulerV2 logs for details.";
        errorMessageText.setText(errorMessage);

        // Show close button, hide cancel
        if (dialogPane != null) {
            Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
            Button closeButton = (Button) dialogPane.lookupButton(ButtonType.OK);

            if (cancelButton != null) {
                cancelButton.setVisible(false);
                cancelButton.setManaged(false);
            }
            if (closeButton != null) {
                closeButton.setVisible(true);
                closeButton.setManaged(true);
            }
        }
    }

    /**
     * Cancel the job
     */
    public void cancel() {
        cancelled = true;
        stopPolling();

        statusIcon.setText("⏹️");
        statusLabel.setText("Cancelled");
        statusMessage.setText("Schedule generation was cancelled by user.");

        log.info("Schedule generation cancelled for job: {}", jobId);
    }

    /**
     * Stop polling
     */
    private void stopPolling() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Get the final result
     */
    public ScheduleGenerationModeService.ScheduleGenerationResult getFinalResult() {
        return finalResult;
    }

    /**
     * Set result data after import
     */
    public void setResultData(Integer sectionsCreated, Integer studentsScheduled) {
        if (sectionsCreated != null) {
            sectionsCreatedLabel.setText(String.valueOf(sectionsCreated));
        }
        if (studentsScheduled != null) {
            studentsScheduledLabel.setText(String.valueOf(studentsScheduled));
        }
    }

    /**
     * Cleanup when dialog closes
     */
    public void cleanup() {
        stopPolling();
    }
}
