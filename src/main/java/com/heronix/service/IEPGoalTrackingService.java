package com.heronix.service;

import com.heronix.model.domain.IEP;
import com.heronix.repository.IEPRepository;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * IEP Goal Tracking Service
 *
 * Manages individual IEP goals, progress monitoring, and mastery tracking.
 * Provides structured goal management beyond the free-text goals field in IEP entity.
 *
 * Key Responsibilities:
 * - Parse and manage individual IEP goals
 * - Track goal progress and mastery levels
 * - Monitor goal completion rates
 * - Generate progress reports
 * - Provide goal templates by eligibility category
 * - Track baseline and current performance levels
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - IEP Management Enhancement
 */
@Slf4j
@Service
public class IEPGoalTrackingService {

    @Autowired
    private IEPRepository iepRepository;

    // ========================================================================
    // GOAL PARSING AND MANAGEMENT
    // ========================================================================

    /**
     * Parse goals from IEP text into structured list
     */
    public List<IEPGoal> parseGoals(Long iepId) {
        log.info("Parsing goals for IEP ID: {}", iepId);

        IEP iep = iepRepository.findById(iepId)
                .orElseThrow(() -> new IllegalArgumentException("IEP not found: " + iepId));

        String goalsText = iep.getGoals();
        if (goalsText == null || goalsText.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<IEPGoal> goals = new ArrayList<>();
        String[] lines = goalsText.split("\\n");
        int sequence = 1;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            line = line.replaceAll("^[•\\-\\*\\d\\.\\)]+\\s*", "");
            line = line.replaceAll("^Goal\\s*\\d*:?\\s*", "");

            if (!line.isEmpty()) {
                GoalArea area = determineGoalArea(line);
                MeasurementMethod method = determineMeasurementMethod(line);

                goals.add(IEPGoal.builder()
                        .iepId(iepId)
                        .goalNumber(sequence)
                        .goalStatement(line)
                        .goalArea(area)
                        .measurementMethod(method)
                        .masteryLevel(MasteryLevel.NOT_STARTED)
                        .progressPercentage(0.0)
                        .targetDate(iep.getEndDate())
                        .build());
                sequence++;
            }
        }

        log.info("Parsed {} goals from IEP {}", goals.size(), iepId);
        return goals;
    }

    private GoalArea determineGoalArea(String goalText) {
        String lower = goalText.toLowerCase();

        if (lower.contains("read") || lower.contains("comprehension") || lower.contains("fluency")) {
            return GoalArea.READING;
        } else if (lower.contains("math") || lower.contains("calculation") || lower.contains("number")) {
            return GoalArea.MATHEMATICS;
        } else if (lower.contains("writ") || lower.contains("composition") || lower.contains("essay")) {
            return GoalArea.WRITING;
        } else if (lower.contains("speak") || lower.contains("articulation") || lower.contains("language") || lower.contains("communication")) {
            return GoalArea.COMMUNICATION;
        } else if (lower.contains("social") || lower.contains("peer") || lower.contains("interact")) {
            return GoalArea.SOCIAL_EMOTIONAL;
        } else if (lower.contains("behavior") || lower.contains("self-control") || lower.contains("attention")) {
            return GoalArea.BEHAVIORAL;
        } else if (lower.contains("motor") || lower.contains("coordination") || lower.contains("movement")) {
            return GoalArea.MOTOR_SKILLS;
        } else if (lower.contains("adaptive") || lower.contains("daily living") || lower.contains("self-care")) {
            return GoalArea.ADAPTIVE_LIVING;
        }

        return GoalArea.OTHER;
    }

    private MeasurementMethod determineMeasurementMethod(String goalText) {
        String lower = goalText.toLowerCase();

        if (lower.contains("curriculum-based") || lower.contains("cbm")) {
            return MeasurementMethod.CURRICULUM_BASED_MEASUREMENT;
        } else if (lower.contains("observation") || lower.contains("observed")) {
            return MeasurementMethod.TEACHER_OBSERVATION;
        } else if (lower.contains("assessment") || lower.contains("test")) {
            return MeasurementMethod.STANDARDIZED_ASSESSMENT;
        } else if (lower.contains("work sample") || lower.contains("portfolio")) {
            return MeasurementMethod.WORK_SAMPLES;
        } else if (lower.contains("data") || lower.contains("frequency") || lower.contains("duration")) {
            return MeasurementMethod.DATA_COLLECTION;
        } else if (lower.contains("checklist")) {
            return MeasurementMethod.CHECKLIST;
        }

        return MeasurementMethod.TEACHER_OBSERVATION;
    }

    // ========================================================================
    // PROGRESS TRACKING
    // ========================================================================

    @Transactional
    public GoalProgress recordProgress(
            Long iepId,
            int goalNumber,
            double progressPercentage,
            MasteryLevel masteryLevel,
            String observation,
            String recordedBy) {

        log.info("Recording progress for IEP {} Goal {}: {}%", iepId, goalNumber, progressPercentage);

        IEP iep = iepRepository.findById(iepId)
                .orElseThrow(() -> new IllegalArgumentException("IEP not found: " + iepId));

        return GoalProgress.builder()
                .id(UUID.randomUUID().toString())
                .iepId(iepId)
                .goalNumber(goalNumber)
                .studentName(iep.getStudent().getFullName())
                .progressDate(LocalDate.now())
                .progressPercentage(progressPercentage)
                .masteryLevel(masteryLevel)
                .observation(observation)
                .recordedBy(recordedBy)
                .recordedAt(LocalDateTime.now())
                .build();
    }

    public GoalProgressSummary getProgressSummary(Long iepId) {
        log.info("Getting progress summary for IEP {}", iepId);

        IEP iep = iepRepository.findById(iepId)
                .orElseThrow(() -> new IllegalArgumentException("IEP not found: " + iepId));

        List<IEPGoal> goals = parseGoals(iepId);

        if (goals.isEmpty()) {
            return GoalProgressSummary.builder()
                    .iepId(iepId)
                    .totalGoals(0)
                    .mastered(0)
                    .progressing(0)
                    .notStarted(0)
                    .overallProgress(0.0)
                    .onTrack(true)
                    .build();
        }

        int mastered = (int) goals.stream().filter(g -> g.getMasteryLevel() == MasteryLevel.MASTERED).count();
        int progressing = (int) goals.stream().filter(g -> g.getMasteryLevel() == MasteryLevel.PROGRESSING ||
                                                            g.getMasteryLevel() == MasteryLevel.EMERGING).count();
        int notStarted = (int) goals.stream().filter(g -> g.getMasteryLevel() == MasteryLevel.NOT_STARTED).count();

        double avgProgress = goals.stream().mapToDouble(IEPGoal::getProgressPercentage).average().orElse(0.0);
        boolean onTrack = (mastered + progressing) > (goals.size() * 0.5);

        return GoalProgressSummary.builder()
                .iepId(iepId)
                .studentName(iep.getStudent().getFullName())
                .totalGoals(goals.size())
                .mastered(mastered)
                .progressing(progressing)
                .notStarted(notStarted)
                .overallProgress(avgProgress)
                .onTrack(onTrack)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    // ========================================================================
    // GOAL TEMPLATES
    // ========================================================================

    public List<GoalTemplate> getGoalTemplates(String eligibilityCategory) {
        log.info("Getting goal templates for: {}", eligibilityCategory);

        List<GoalTemplate> templates = new ArrayList<>();

        switch (eligibilityCategory.toUpperCase()) {
            case "SPECIFIC LEARNING DISABILITY":
            case "SLD":
                templates.add(createTemplate("Reading Fluency", GoalArea.READING,
                        "Student will read grade-level text with fluency at [X] words per minute with 95% accuracy.",
                        MeasurementMethod.CURRICULUM_BASED_MEASUREMENT));
                templates.add(createTemplate("Math Calculation", GoalArea.MATHEMATICS,
                        "Student will solve grade-level math problems with 80% accuracy.",
                        MeasurementMethod.CURRICULUM_BASED_MEASUREMENT));
                templates.add(createTemplate("Written Expression", GoalArea.WRITING,
                        "Student will write a paragraph with topic sentence and supporting details.",
                        MeasurementMethod.WORK_SAMPLES));
                break;

            case "SPEECH/LANGUAGE IMPAIRMENT":
            case "SPEECH":
                templates.add(createTemplate("Articulation", GoalArea.COMMUNICATION,
                        "Student will correctly articulate [sound] in all positions of words with 90% accuracy.",
                        MeasurementMethod.TEACHER_OBSERVATION));
                templates.add(createTemplate("Expressive Language", GoalArea.COMMUNICATION,
                        "Student will use complete sentences when describing pictures or events.",
                        MeasurementMethod.DATA_COLLECTION));
                break;

            case "AUTISM":
            case "ASD":
                templates.add(createTemplate("Social Interaction", GoalArea.SOCIAL_EMOTIONAL,
                        "Student will initiate positive interactions with peers 3 times per session.",
                        MeasurementMethod.DATA_COLLECTION));
                templates.add(createTemplate("Communication", GoalArea.COMMUNICATION,
                        "Student will request items appropriately in 8 out of 10 opportunities.",
                        MeasurementMethod.DATA_COLLECTION));
                break;

            case "EMOTIONAL DISTURBANCE":
            case "ED":
                templates.add(createTemplate("Self-Regulation", GoalArea.BEHAVIORAL,
                        "Student will use coping strategies when frustrated in 4 out of 5 opportunities.",
                        MeasurementMethod.TEACHER_OBSERVATION));
                templates.add(createTemplate("On-Task Behavior", GoalArea.BEHAVIORAL,
                        "Student will remain on-task for 15 minutes with no more than 2 prompts.",
                        MeasurementMethod.DATA_COLLECTION));
                break;

            case "OTHER HEALTH IMPAIRMENT":
            case "OHI":
                templates.add(createTemplate("Attention", GoalArea.BEHAVIORAL,
                        "Student will maintain attention to task for 20 minutes with minimal redirection.",
                        MeasurementMethod.DATA_COLLECTION));
                templates.add(createTemplate("Organization", GoalArea.ADAPTIVE_LIVING,
                        "Student will organize materials and maintain planner with 85% accuracy.",
                        MeasurementMethod.CHECKLIST));
                break;

            default:
                templates.add(createTemplate("Academic Goal", GoalArea.READING,
                        "Student will demonstrate progress in targeted academic area.",
                        MeasurementMethod.TEACHER_OBSERVATION));
        }

        return templates;
    }

    private GoalTemplate createTemplate(String name, GoalArea area, String template, MeasurementMethod method) {
        return GoalTemplate.builder()
                .name(name)
                .goalArea(area)
                .templateText(template)
                .measurementMethod(method)
                .frequency("Weekly")
                .evidenceBase("Research-based best practices")
                .build();
    }

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum GoalArea {
        READING("Reading"),
        MATHEMATICS("Mathematics"),
        WRITING("Written Expression"),
        COMMUNICATION("Communication/Speech"),
        SOCIAL_EMOTIONAL("Social/Emotional"),
        BEHAVIORAL("Behavioral"),
        MOTOR_SKILLS("Motor Skills"),
        ADAPTIVE_LIVING("Adaptive/Daily Living"),
        VOCATIONAL("Vocational"),
        OTHER("Other");

        private final String displayName;
        GoalArea(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    public enum MasteryLevel {
        NOT_STARTED("Not Started"),
        EMERGING("Emerging"),
        PROGRESSING("Progressing"),
        MASTERED("Mastered"),
        MAINTAINED("Maintained");

        private final String displayName;
        MasteryLevel(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    public enum MeasurementMethod {
        CURRICULUM_BASED_MEASUREMENT("Curriculum-Based Measurement"),
        TEACHER_OBSERVATION("Teacher Observation"),
        STANDARDIZED_ASSESSMENT("Standardized Assessment"),
        WORK_SAMPLES("Work Samples"),
        DATA_COLLECTION("Data Collection"),
        CHECKLIST("Checklist/Rubric"),
        PORTFOLIO("Portfolio"),
        RUNNING_RECORD("Running Record");

        private final String displayName;
        MeasurementMethod(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class IEPGoal {
        private Long iepId;
        private Integer goalNumber;
        private String goalStatement;
        private GoalArea goalArea;
        private String baselineData;
        private MeasurementMethod measurementMethod;
        private String frequency;
        private LocalDate targetDate;
        private MasteryLevel masteryLevel;
        private Double progressPercentage;
        private String notes;
    }

    @Data
    @Builder
    public static class GoalProgress {
        private String id;
        private Long iepId;
        private Integer goalNumber;
        private String studentName;
        private LocalDate progressDate;
        private Double progressPercentage;
        private MasteryLevel masteryLevel;
        private String observation;
        private String dataPoints;
        private String recordedBy;
        private LocalDateTime recordedAt;
    }

    @Data
    @Builder
    public static class GoalProgressSummary {
        private Long iepId;
        private String studentName;
        private Integer totalGoals;
        private Integer mastered;
        private Integer progressing;
        private Integer notStarted;
        private Double overallProgress;
        private Boolean onTrack;
        private LocalDateTime lastUpdated;
    }

    @Data
    @Builder
    public static class GoalTemplate {
        private String name;
        private GoalArea goalArea;
        private String templateText;
        private MeasurementMethod measurementMethod;
        private String frequency;
        private String evidenceBase;
    }
}
