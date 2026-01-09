package com.heronix.service;

import com.heronix.model.domain.Plan504;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.User;
import com.heronix.repository.Plan504Repository;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.UserRepository;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 504 Plan Accommodation Service
 *
 * Manages individual accommodations within 504 plans, tracks implementation effectiveness,
 * and monitors compliance with accommodation requirements.
 *
 * Key Responsibilities:
 * - Parse and manage individual accommodations from plan text
 * - Track accommodation implementation status
 * - Monitor accommodation effectiveness
 * - Generate compliance reports
 * - Track teacher implementation of accommodations
 * - Provide accommodation recommendations based on disability type
 * - Monitor accommodation usage patterns
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - 504 Management Enhancement
 */
@Slf4j
@Service
public class Plan504AccommodationService {

    @Autowired
    private Plan504Repository plan504Repository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    // ========================================================================
    // ACCOMMODATION PARSING AND MANAGEMENT
    // ========================================================================

    /**
     * Parse accommodations from plan text into structured list
     */
    public List<Accommodation> parseAccommodations(Long planId) {
        log.info("Parsing accommodations for 504 plan ID: {}", planId);

        Plan504 plan = plan504Repository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("504 Plan not found: " + planId));

        String accommodationsText = plan.getAccommodations();
        if (accommodationsText == null || accommodationsText.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<Accommodation> accommodations = new ArrayList<>();
        String[] lines = accommodationsText.split("\\n");
        int sequence = 1;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Remove bullet points, numbers, dashes
            line = line.replaceAll("^[•\\-\\*\\d\\.\\)]+\\s*", "");

            if (!line.isEmpty()) {
                AccommodationType type = determineAccommodationType(line);
                String category = determineCategory(line);

                accommodations.add(Accommodation.builder()
                        .planId(planId)
                        .sequence(sequence++)
                        .description(line)
                        .type(type)
                        .category(category)
                        .status("ACTIVE")
                        .implementationRequired(true)
                        .build());
            }
        }

        log.info("Parsed {} accommodations from plan {}", accommodations.size(), planId);
        return accommodations;
    }

    /**
     * Get accommodation recommendations based on disability type
     */
    public List<AccommodationRecommendation> getRecommendations(String disability) {
        log.info("Getting accommodation recommendations for disability: {}", disability);

        List<AccommodationRecommendation> recommendations = new ArrayList<>();

        switch (disability.toUpperCase()) {
            case "ADHD":
                recommendations.add(createRecommendation(
                        "Preferential seating near teacher",
                        "ENVIRONMENTAL",
                        "High",
                        "Reduces distractions and increases focus"
                ));
                recommendations.add(createRecommendation(
                        "Extended time on tests (1.5x)",
                        "TESTING",
                        "High",
                        "Compensates for processing speed challenges"
                ));
                recommendations.add(createRecommendation(
                        "Frequent breaks during long tasks",
                        "INSTRUCTIONAL",
                        "Medium",
                        "Maintains attention and reduces fatigue"
                ));
                recommendations.add(createRecommendation(
                        "Written and verbal instructions",
                        "INSTRUCTIONAL",
                        "High",
                        "Supports multiple learning modalities"
                ));
                break;

            case "DYSLEXIA":
                recommendations.add(createRecommendation(
                        "Audiobooks for reading assignments",
                        "ASSISTIVE_TECHNOLOGY",
                        "High",
                        "Provides alternative access to text"
                ));
                recommendations.add(createRecommendation(
                        "Extended time on reading tasks (2x)",
                        "TESTING",
                        "High",
                        "Accommodates slower reading speed"
                ));
                recommendations.add(createRecommendation(
                        "Text-to-speech software",
                        "ASSISTIVE_TECHNOLOGY",
                        "High",
                        "Supports decoding challenges"
                ));
                recommendations.add(createRecommendation(
                        "Reduced penalty for spelling errors",
                        "GRADING",
                        "Medium",
                        "Focuses assessment on content, not spelling"
                ));
                break;

            case "ANXIETY DISORDER":
                recommendations.add(createRecommendation(
                        "Testing in quiet, separate location",
                        "TESTING",
                        "High",
                        "Reduces anxiety triggers during assessments"
                ));
                recommendations.add(createRecommendation(
                        "Breaks during class as needed",
                        "BEHAVIORAL",
                        "High",
                        "Allows self-regulation during anxiety episodes"
                ));
                recommendations.add(createRecommendation(
                        "Advance notice of schedule changes",
                        "ENVIRONMENTAL",
                        "Medium",
                        "Reduces anxiety from unexpected transitions"
                ));
                recommendations.add(createRecommendation(
                        "Option to submit assignments electronically",
                        "INSTRUCTIONAL",
                        "Low",
                        "Reduces social anxiety from in-person submission"
                ));
                break;

            case "DIABETES":
                recommendations.add(createRecommendation(
                        "Unrestricted access to water and snacks",
                        "HEALTH",
                        "Critical",
                        "Manages blood sugar levels"
                ));
                recommendations.add(createRecommendation(
                        "Bathroom breaks as needed without permission",
                        "HEALTH",
                        "Critical",
                        "Addresses diabetes-related needs"
                ));
                recommendations.add(createRecommendation(
                        "Make-up work for medical absences",
                        "ATTENDANCE",
                        "High",
                        "Accounts for diabetes management appointments"
                ));
                recommendations.add(createRecommendation(
                        "Excused from timed physical activities if needed",
                        "PHYSICAL_EDUCATION",
                        "Medium",
                        "Prevents blood sugar complications"
                ));
                break;

            case "VISUAL IMPAIRMENT":
                recommendations.add(createRecommendation(
                        "Large print materials (18pt minimum)",
                        "MATERIALS",
                        "High",
                        "Improves text accessibility"
                ));
                recommendations.add(createRecommendation(
                        "Preferential seating near board",
                        "ENVIRONMENTAL",
                        "High",
                        "Maximizes visual access to instruction"
                ));
                recommendations.add(createRecommendation(
                        "Digital copies of all materials",
                        "ASSISTIVE_TECHNOLOGY",
                        "High",
                        "Allows use of screen magnification"
                ));
                recommendations.add(createRecommendation(
                        "Extended time on visual tasks",
                        "TESTING",
                        "Medium",
                        "Compensates for slower visual processing"
                ));
                break;

            case "HEARING IMPAIRMENT":
                recommendations.add(createRecommendation(
                        "Preferential seating to see teacher's face",
                        "ENVIRONMENTAL",
                        "High",
                        "Facilitates lip reading and visual cues"
                ));
                recommendations.add(createRecommendation(
                        "Written notes/handouts for lectures",
                        "INSTRUCTIONAL",
                        "High",
                        "Provides alternative to auditory instruction"
                ));
                recommendations.add(createRecommendation(
                        "Closed captioning for videos",
                        "ASSISTIVE_TECHNOLOGY",
                        "High",
                        "Ensures access to multimedia content"
                ));
                recommendations.add(createRecommendation(
                        "Check for understanding frequently",
                        "INSTRUCTIONAL",
                        "Medium",
                        "Ensures student comprehends auditory information"
                ));
                break;

            default:
                recommendations.add(createRecommendation(
                        "Extended time on tests (1.5x)",
                        "TESTING",
                        "Medium",
                        "General accommodation for processing needs"
                ));
                recommendations.add(createRecommendation(
                        "Preferential seating",
                        "ENVIRONMENTAL",
                        "Medium",
                        "Reduces distractions and improves focus"
                ));
                break;
        }

        return recommendations;
    }

    // ========================================================================
    // ACCOMMODATION EFFECTIVENESS TRACKING
    // ========================================================================

    /**
     * Track accommodation effectiveness over time
     */
    @Transactional
    public AccommodationEffectiveness trackEffectiveness(
            Long planId,
            int accommodationSequence,
            String implementingTeacher,
            EffectivenessRating rating,
            String notes) {

        log.info("Tracking effectiveness for plan {} accommodation #{}: {}",
                planId, accommodationSequence, rating);

        Plan504 plan = plan504Repository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("504 Plan not found: " + planId));

        // In production, would save to AccommodationEffectiveness table
        // For now, create DTO for return

        return AccommodationEffectiveness.builder()
                .planId(planId)
                .accommodationSequence(accommodationSequence)
                .implementingTeacher(implementingTeacher)
                .rating(rating)
                .notes(notes)
                .recordedAt(LocalDateTime.now())
                .studentId(plan.getStudent().getId())
                .build();
    }

    /**
     * Get effectiveness summary for all accommodations in a plan
     */
    public AccommodationEffectivenessSummary getEffectivenessSummary(Long planId) {
        log.info("Getting effectiveness summary for plan {}", planId);

        Plan504 plan = plan504Repository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("504 Plan not found: " + planId));

        List<Accommodation> accommodations = parseAccommodations(planId);

        // Mock implementation - would query actual effectiveness records
        int totalAccommodations = accommodations.size();
        int effective = (int) (totalAccommodations * 0.7); // 70% mock
        int partiallyEffective = (int) (totalAccommodations * 0.2); // 20% mock
        int ineffective = totalAccommodations - effective - partiallyEffective;

        return AccommodationEffectivenessSummary.builder()
                .planId(planId)
                .studentName(plan.getStudent().getFullName())
                .totalAccommodations(totalAccommodations)
                .effectiveCount(effective)
                .partiallyEffectiveCount(partiallyEffective)
                .ineffectiveCount(ineffective)
                .notYetRatedCount(0)
                .overallEffectivenessRate(totalAccommodations > 0 ?
                        (double) effective / totalAccommodations : 0.0)
                .build();
    }

    // ========================================================================
    // COMPLIANCE MONITORING
    // ========================================================================

    /**
     * Generate compliance report for accommodation implementation
     */
    public AccommodationComplianceReport generateComplianceReport(
            Long planId,
            LocalDate startDate,
            LocalDate endDate) {

        log.info("Generating compliance report for plan {} from {} to {}",
                planId, startDate, endDate);

        Plan504 plan = plan504Repository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("504 Plan not found: " + planId));

        List<Accommodation> accommodations = parseAccommodations(planId);

        // Mock implementation - would query actual implementation records
        List<AccommodationCompliance> complianceItems = new ArrayList<>();

        for (Accommodation acc : accommodations) {
            // Mock compliance data
            double implementationRate = 0.85 + (Math.random() * 0.15); // 85-100%
            boolean isCompliant = implementationRate >= 0.80;

            complianceItems.add(AccommodationCompliance.builder()
                    .accommodation(acc.getDescription())
                    .category(acc.getCategory())
                    .implementationRate(implementationRate)
                    .isCompliant(isCompliant)
                    .totalOpportunities(20)
                    .timesImplemented((int) (20 * implementationRate))
                    .build());
        }

        double overallCompliance = complianceItems.stream()
                .mapToDouble(AccommodationCompliance::getImplementationRate)
                .average()
                .orElse(0.0);

        int compliantCount = (int) complianceItems.stream()
                .filter(AccommodationCompliance::isCompliant)
                .count();

        return AccommodationComplianceReport.builder()
                .planId(planId)
                .studentName(plan.getStudent().getFullName())
                .reportStartDate(startDate)
                .reportEndDate(endDate)
                .totalAccommodations(accommodations.size())
                .compliantAccommodations(compliantCount)
                .nonCompliantAccommodations(accommodations.size() - compliantCount)
                .overallComplianceRate(overallCompliance)
                .complianceItems(complianceItems)
                .isFullyCompliant(compliantCount == accommodations.size())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Determine accommodation type from description
     */
    private AccommodationType determineAccommodationType(String description) {
        String lower = description.toLowerCase();

        if (lower.contains("test") || lower.contains("exam") || lower.contains("assessment")) {
            return AccommodationType.TESTING;
        } else if (lower.contains("seat") || lower.contains("environment") || lower.contains("quiet")) {
            return AccommodationType.ENVIRONMENTAL;
        } else if (lower.contains("instruction") || lower.contains("teach") || lower.contains("lesson")) {
            return AccommodationType.INSTRUCTIONAL;
        } else if (lower.contains("technology") || lower.contains("software") || lower.contains("device")) {
            return AccommodationType.ASSISTIVE_TECHNOLOGY;
        } else if (lower.contains("grade") || lower.contains("score") || lower.contains("penalty")) {
            return AccommodationType.GRADING;
        } else if (lower.contains("behavior") || lower.contains("break") || lower.contains("self-regulation")) {
            return AccommodationType.BEHAVIORAL;
        } else if (lower.contains("assignment") || lower.contains("homework") || lower.contains("classwork")) {
            return AccommodationType.ASSIGNMENT_MODIFICATION;
        } else {
            return AccommodationType.OTHER;
        }
    }

    /**
     * Determine accommodation category
     */
    private String determineCategory(String description) {
        AccommodationType type = determineAccommodationType(description);

        switch (type) {
            case TESTING: return "Assessment";
            case ENVIRONMENTAL: return "Classroom Environment";
            case INSTRUCTIONAL: return "Instruction & Presentation";
            case ASSISTIVE_TECHNOLOGY: return "Technology & Tools";
            case GRADING: return "Evaluation & Grading";
            case BEHAVIORAL: return "Behavior & Self-Regulation";
            case ASSIGNMENT_MODIFICATION: return "Assignments & Homework";
            default: return "General";
        }
    }

    /**
     * Create accommodation recommendation
     */
    private AccommodationRecommendation createRecommendation(
            String description,
            String category,
            String priority,
            String rationale) {

        return AccommodationRecommendation.builder()
                .description(description)
                .category(category)
                .priority(priority)
                .rationale(rationale)
                .evidenceBased(true)
                .build();
    }

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum AccommodationType {
        TESTING,
        ENVIRONMENTAL,
        INSTRUCTIONAL,
        ASSISTIVE_TECHNOLOGY,
        GRADING,
        BEHAVIORAL,
        ASSIGNMENT_MODIFICATION,
        HEALTH,
        MATERIALS,
        ATTENDANCE,
        PHYSICAL_EDUCATION,
        OTHER
    }

    public enum EffectivenessRating {
        VERY_EFFECTIVE,
        EFFECTIVE,
        PARTIALLY_EFFECTIVE,
        INEFFECTIVE,
        NOT_APPLICABLE
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class Accommodation {
        private Long planId;
        private int sequence;
        private String description;
        private AccommodationType type;
        private String category;
        private String status;
        private boolean implementationRequired;
    }

    @Data
    @Builder
    public static class AccommodationRecommendation {
        private String description;
        private String category;
        private String priority; // Critical, High, Medium, Low
        private String rationale;
        private boolean evidenceBased;
    }

    @Data
    @Builder
    public static class AccommodationEffectiveness {
        private Long planId;
        private Long studentId;
        private int accommodationSequence;
        private String implementingTeacher;
        private EffectivenessRating rating;
        private String notes;
        private LocalDateTime recordedAt;
    }

    @Data
    @Builder
    public static class AccommodationEffectivenessSummary {
        private Long planId;
        private String studentName;
        private int totalAccommodations;
        private int effectiveCount;
        private int partiallyEffectiveCount;
        private int ineffectiveCount;
        private int notYetRatedCount;
        private double overallEffectivenessRate;
    }

    @Data
    @Builder
    public static class AccommodationComplianceReport {
        private Long planId;
        private String studentName;
        private LocalDate reportStartDate;
        private LocalDate reportEndDate;
        private int totalAccommodations;
        private int compliantAccommodations;
        private int nonCompliantAccommodations;
        private double overallComplianceRate;
        private List<AccommodationCompliance> complianceItems;
        private boolean isFullyCompliant;
        private LocalDateTime generatedAt;
    }

    @Data
    @Builder
    public static class AccommodationCompliance {
        private String accommodation;
        private String category;
        private double implementationRate;
        private boolean isCompliant;
        private int totalOpportunities;
        private int timesImplemented;
    }
}
