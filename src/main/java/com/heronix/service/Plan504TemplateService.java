package com.heronix.service;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 504 Plan Template Service
 *
 * Provides pre-configured templates for common disabilities and conditions.
 * Helps staff quickly create 504 plans with evidence-based accommodations.
 *
 * Key Responsibilities:
 * - Provide disability-specific plan templates
 * - Generate accommodation suggestions
 * - Offer monitoring plan templates
 * - Provide implementation guidance
 * - Customizable template creation
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - 504 Management Enhancement
 */
@Slf4j
@Service
public class Plan504TemplateService {

    // ========================================================================
    // TEMPLATE RETRIEVAL
    // ========================================================================

    /**
     * Get template for a specific disability
     */
    public Plan504Template getTemplateForDisability(String disability) {
        log.info("Getting 504 plan template for disability: {}", disability);

        switch (disability.toUpperCase()) {
            case "ADHD":
                return createADHDTemplate();
            case "DYSLEXIA":
                return createDyslexiaTemplate();
            case "DIABETES":
                return createDiabetesTemplate();
            case "ANXIETY DISORDER":
                return createAnxietyTemplate();
            case "ASTHMA":
                return createAsthmaTemplate();
            case "VISUAL IMPAIRMENT":
                return createVisualImpairmentTemplate();
            case "HEARING IMPAIRMENT":
                return createHearingImpairmentTemplate();
            case "FOOD ALLERGIES":
                return createFoodAllergiesTemplate();
            default:
                return createGenericTemplate();
        }
    }

    /**
     * Get all available templates
     */
    public List<Plan504Template> getAllTemplates() {
        return Arrays.asList(
                createADHDTemplate(),
                createDyslexiaTemplate(),
                createDiabetesTemplate(),
                createAnxietyTemplate(),
                createAsthmaTemplate(),
                createVisualImpairmentTemplate(),
                createHearingImpairmentTemplate(),
                createFoodAllergiesTemplate()
        );
    }

    // ========================================================================
    // SPECIFIC TEMPLATES
    // ========================================================================

    private Plan504Template createADHDTemplate() {
        return Plan504Template.builder()
                .disability("ADHD")
                .description("Template for students with Attention-Deficit/Hyperactivity Disorder")
                .accommodations(Arrays.asList(
                        "Preferential seating near teacher, away from distractions",
                        "Extended time on tests and assignments (1.5x)",
                        "Frequent breaks during long tasks (every 20-30 minutes)",
                        "Written and verbal instructions provided",
                        "Use of organizational aids (checklists, planners)",
                        "Reduced homework assignments when appropriate",
                        "Testing in quiet, separate location",
                        "Permission to move/fidget as needed",
                        "Positive reinforcement system",
                        "Copy of class notes if needed"
                ))
                .monitoringPlan(Arrays.asList(
                        "Weekly check-ins with student",
                        "Monthly progress reports from teachers",
                        "Quarterly 504 team meetings",
                        "Monitor assignment completion rates",
                        "Track test scores with and without accommodations"
                ))
                .coordinatorGuidance("Ensure all teachers understand ADHD-related needs. Monitor for medication effectiveness.")
                .build();
    }

    private Plan504Template createDyslexiaTemplate() {
        return Plan504Template.builder()
                .disability("Dyslexia")
                .description("Template for students with reading disabilities")
                .accommodations(Arrays.asList(
                        "Extended time on reading assignments and tests (2x)",
                        "Audiobooks for all reading materials",
                        "Text-to-speech software access",
                        "Speech-to-text for written assignments",
                        "Reduced penalty for spelling errors",
                        "Alternative assessment formats",
                        "Access to graphic organizers",
                        "Highlighted texts or study guides",
                        "Preferential seating for board viewing",
                        "Oral testing when appropriate"
                ))
                .monitoringPlan(Arrays.asList(
                        "Track reading fluency progress",
                        "Monitor comprehension assessments",
                        "Review use of assistive technology",
                        "Quarterly meetings with reading specialist",
                        "Parent feedback on home reading"
                ))
                .coordinatorGuidance("Coordinate with reading specialist. Ensure assistive technology is properly configured.")
                .build();
    }

    private Plan504Template createDiabetesTemplate() {
        return Plan504Template.builder()
                .disability("Diabetes")
                .description("Template for students with Type 1 or Type 2 Diabetes")
                .accommodations(Arrays.asList(
                        "Unrestricted access to water and bathroom",
                        "Permission to eat snacks as needed for blood sugar management",
                        "Permission to test blood glucose in classroom",
                        "Administer insulin as prescribed",
                        "Extra time on assignments if blood sugar affects concentration",
                        "Excused absences for diabetes-related medical appointments",
                        "No penalties for absences due to blood sugar issues",
                        "Alternative physical education activities if needed",
                        "Communication system for urgent medical needs",
                        "Storage for diabetes supplies in classroom"
                ))
                .monitoringPlan(Arrays.asList(
                        "Daily health check by school nurse",
                        "Weekly blood sugar log review",
                        "Emergency action plan on file and updated",
                        "Staff training on diabetes management",
                        "Parent communication for any concerns"
                ))
                .coordinatorGuidance("CRITICAL: Ensure all staff understand emergency procedures. Maintain current emergency contact information.")
                .build();
    }

    private Plan504Template createAnxietyTemplate() {
        return Plan504Template.builder()
                .disability("Anxiety Disorder")
                .description("Template for students with diagnosed anxiety disorders")
                .accommodations(Arrays.asList(
                        "Testing in quiet, private location",
                        "Breaks during class as needed",
                        "Advance notice of schedule changes",
                        "Option to leave class for counselor visits",
                        "Extended time on tests due to anxiety",
                        "Preferential seating (near door, if requested)",
                        "Option to submit assignments electronically",
                        "Reduced public speaking requirements or alternatives",
                        "Permission to use stress-reduction tools (fidgets, etc.)",
                        "Late arrival pass for anxiety-related tardiness"
                ))
                .monitoringPlan(Arrays.asList(
                        "Bi-weekly counselor check-ins",
                        "Monitor classroom participation",
                        "Track anxiety-related absences",
                        "Parent communication for triggers",
                        "Review effectiveness of coping strategies"
                ))
                .coordinatorGuidance("Work closely with school counselor. Be aware of anxiety triggers and warning signs.")
                .build();
    }

    private Plan504Template createAsthmaTemplate() {
        return Plan504Template.builder()
                .disability("Asthma")
                .description("Template for students with asthma")
                .accommodations(Arrays.asList(
                        "Immediate access to inhaler at all times",
                        "Permission to sit out during physical education if needed",
                        "Modified physical education activities during high pollen/pollution days",
                        "Extra time to complete physical activities",
                        "Pre-medication before physical exertion",
                        "Classroom assignment during recess if air quality is poor",
                        "Seating away from classroom irritants (chalk, markers, etc.)",
                        "Make-up work for asthma-related absences",
                        "Emergency action plan for severe attacks",
                        "Notification to substitute teachers about asthma"
                ))
                .monitoringPlan(Arrays.asList(
                        "Track frequency of inhaler use",
                        "Monitor participation in physical activities",
                        "Review air quality reports",
                        "Monthly check-in with nurse",
                        "Parent notification of any asthma incidents"
                ))
                .coordinatorGuidance("Ensure emergency medication is always accessible. Monitor environmental triggers.")
                .build();
    }

    private Plan504Template createVisualImpairmentTemplate() {
        return Plan504Template.builder()
                .disability("Visual Impairment")
                .description("Template for students with low vision or blindness")
                .accommodations(Arrays.asList(
                        "Large print materials (18pt minimum)",
                        "Preferential seating near board",
                        "Digital copies of all materials",
                        "Extended time on visual tasks",
                        "Magnification devices or software",
                        "Audio textbooks and materials",
                        "Braille materials if needed",
                        "High-contrast materials",
                        "Extra lighting as needed",
                        "Verbal descriptions of visual information"
                ))
                .monitoringPlan(Arrays.asList(
                        "Weekly check of material accessibility",
                        "Monthly vision teacher consultation",
                        "Review assistive technology effectiveness",
                        "Monitor eye fatigue",
                        "Coordinate with orientation and mobility specialist"
                ))
                .coordinatorGuidance("Consult with vision specialist. Ensure all materials are provided in accessible formats.")
                .build();
    }

    private Plan504Template createHearingImpairmentTemplate() {
        return Plan504Template.builder()
                .disability("Hearing Impairment")
                .description("Template for students who are deaf or hard of hearing")
                .accommodations(Arrays.asList(
                        "Preferential seating to see teacher's face",
                        "FM system or other assistive listening device",
                        "Interpreter or note-taker as needed",
                        "Written notes for all verbal instructions",
                        "Closed captioning for videos",
                        "Extended time for assignments due to language processing",
                        "Visual aids and demonstrations",
                        "Peer note-sharing system",
                        "Teacher faces student when speaking",
                        "Reduced background noise in classroom"
                ))
                .monitoringPlan(Arrays.asList(
                        "Weekly equipment check",
                        "Monthly hearing specialist consultation",
                        "Monitor comprehension of verbal instruction",
                        "Review note-taking effectiveness",
                        "Parent feedback on communication"
                ))
                .coordinatorGuidance("Ensure assistive technology is functioning properly. Verify all teachers understand communication needs.")
                .build();
    }

    private Plan504Template createFoodAllergiesTemplate() {
        return Plan504Template.builder()
                .disability("Food Allergies")
                .description("Template for students with severe food allergies")
                .accommodations(Arrays.asList(
                        "Peanut-free or allergen-free table at lunch",
                        "Access to EpiPen at all times",
                        "No food-based rewards or classroom celebrations without parent approval",
                        "Hand washing before and after meals",
                        "Safe storage for emergency medication",
                        "Staff training on allergy recognition and response",
                        "Emergency action plan visible in classroom",
                        "Parent notification of all food-related activities",
                        "Alternative activities during cooking/food projects",
                        "Seating away from food allergens"
                ))
                .monitoringPlan(Arrays.asList(
                        "Weekly EpiPen location verification",
                        "Monthly staff training review",
                        "Immediate parent contact for any exposure",
                        "Review emergency action plan quarterly",
                        "Track any allergic reactions"
                ))
                .coordinatorGuidance("CRITICAL: Life-threatening condition. Ensure all staff are trained on emergency procedures.")
                .build();
    }

    private Plan504Template createGenericTemplate() {
        return Plan504Template.builder()
                .disability("General")
                .description("Generic template for conditions not listed")
                .accommodations(Arrays.asList(
                        "Extended time on tests and assignments (specify time)",
                        "Preferential seating",
                        "Frequent breaks as needed",
                        "Modified assignments as appropriate",
                        "Additional time for transitions",
                        "Access to support services"
                ))
                .monitoringPlan(Arrays.asList(
                        "Monthly progress monitoring",
                        "Quarterly 504 team meetings",
                        "Teacher feedback collection",
                        "Parent communication"
                ))
                .coordinatorGuidance("Customize based on student's specific needs. Consult with medical professionals as needed.")
                .build();
    }

    // ========================================================================
    // TEMPLATE CUSTOMIZATION
    // ========================================================================

    /**
     * Customize template with specific student information
     */
    public Plan504Template customizeTemplate(
            Plan504Template template,
            String studentName,
            String gradeLevel,
            String additionalAccommodations) {

        log.info("Customizing 504 template for student: {}", studentName);

        Plan504Template customized = Plan504Template.builder()
                .disability(template.getDisability())
                .description(template.getDescription())
                .accommodations(new ArrayList<>(template.getAccommodations()))
                .monitoringPlan(new ArrayList<>(template.getMonitoringPlan()))
                .coordinatorGuidance(template.getCoordinatorGuidance())
                .studentName(studentName)
                .gradeLevel(gradeLevel)
                .build();

        // Add custom accommodations if provided
        if (additionalAccommodations != null && !additionalAccommodations.trim().isEmpty()) {
            for (String accommodation : additionalAccommodations.split("\\n")) {
                if (!accommodation.trim().isEmpty()) {
                    customized.getAccommodations().add(accommodation.trim());
                }
            }
        }

        return customized;
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class Plan504Template {
        private String disability;
        private String description;
        private List<String> accommodations;
        private List<String> monitoringPlan;
        private String coordinatorGuidance;
        private String studentName; // For customized templates
        private String gradeLevel; // For customized templates
    }
}
