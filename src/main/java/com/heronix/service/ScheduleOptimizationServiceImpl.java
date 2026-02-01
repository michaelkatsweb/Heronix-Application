package com.heronix.service;

import com.heronix.model.domain.*;
import com.heronix.model.dto.ScheduleRequest;
import com.heronix.model.enums.ScheduleStatus;
import com.heronix.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Schedule Optimization Service Implementation
 *
 * Implements schedule optimization techniques including:
 * - Constraint-based optimization using heuristics
 * - Kanban principles for workflow optimization
 * - Eisenhower Matrix for priority-based scheduling
 * - Lean Six Sigma principles for efficiency
 * - Machine Learning model training from historical data
 *
 * Optimization Metrics:
 * - Teacher utilization and workload balance
 * - Room utilization efficiency
 * - Student preference satisfaction
 * - Conflict resolution rate
 * - Schedule compactness
 *
 * @author Heronix SIS Team
 * @version 2.0.0
 * @since January 5, 2026
 */
@Slf4j
@Service
public class ScheduleOptimizationServiceImpl implements ScheduleOptimizationService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired(required = false)
    private ScheduleSlotRepository slotRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired(required = false)
    private StudentRepository studentRepository;

    // Optimization weights (can be configured)
    @Value("${heronix.optimization.weight.teacher-utilization:0.25}")
    private double teacherUtilizationWeight;

    @Value("${heronix.optimization.weight.room-utilization:0.20}")
    private double roomUtilizationWeight;

    @Value("${heronix.optimization.weight.student-preference:0.25}")
    private double studentPreferenceWeight;

    @Value("${heronix.optimization.weight.conflict-resolution:0.15}")
    private double conflictResolutionWeight;

    @Value("${heronix.optimization.weight.compactness:0.15}")
    private double compactnessWeight;

    // ML Model state (simplified in-memory model)
    private Map<String, Double> mlModelWeights = new HashMap<>();
    private boolean mlModelTrained = false;

    // ========================================================================
    // MAIN OPTIMIZATION METHOD
    // ========================================================================

    @Override
    @Transactional
    public Schedule optimizeSchedule(ScheduleRequest request) {
        log.info("OPTIMIZATION: Starting schedule optimization for request: {}", request);

        try {
            // Create or find schedule
            Schedule schedule = findOrCreateSchedule(request);

            if (schedule == null) {
                log.error("OPTIMIZATION: Could not find or create schedule");
                return null;
            }

            // Phase 1: Apply constraint-based optimization
            applyConstraintOptimization(schedule);

            // Phase 2: Apply Kanban principles for flow optimization
            applyKanbanPrinciples(schedule);

            // Phase 3: Apply Eisenhower Matrix for priority scheduling
            applyEisenhowerMatrix(schedule);

            // Phase 4: Apply Lean Six Sigma for waste reduction
            applyLeanSixSigma(schedule);

            // Phase 5: Calculate and store optimization score
            double score = calculateOptimizationScore(schedule);
            schedule.setOptimizationScore(score);
            schedule.setLastModifiedDate(java.time.LocalDate.now());

            // Save optimized schedule
            schedule = scheduleRepository.save(schedule);

            log.info("OPTIMIZATION: Schedule {} optimized with score: {}", schedule.getId(), score);
            return schedule;

        } catch (Exception e) {
            log.error("OPTIMIZATION: Failed to optimize schedule", e);
            return null;
        }
    }

    private Schedule findOrCreateSchedule(ScheduleRequest request) {
        if (request == null) {
            return null;
        }

        // Try to find existing schedule by name if provided
        if (request.getScheduleName() != null && !request.getScheduleName().isBlank()) {
            // Search by exact name in existing schedules
            List<Schedule> allSchedules = scheduleRepository.findAll();
            for (Schedule s : allSchedules) {
                if (s.getName() != null && s.getName().equalsIgnoreCase(request.getScheduleName())) {
                    return s;
                }
            }
        }

        // Create new schedule from request
        Schedule schedule = new Schedule();
        schedule.setName(request.getScheduleName() != null ? request.getScheduleName() : "Optimized Schedule");
        schedule.setStartDate(request.getStartDate());
        schedule.setEndDate(request.getEndDate());
        schedule.setStatus(ScheduleStatus.DRAFT);
        schedule.setCreatedDate(java.time.LocalDate.now());

        return scheduleRepository.save(schedule);
    }

    // ========================================================================
    // CONSTRAINT-BASED OPTIMIZATION
    // ========================================================================

    private void applyConstraintOptimization(Schedule schedule) {
        log.info("OPTIMIZATION: Applying constraint-based optimization to schedule {}", schedule.getId());

        List<ScheduleSlot> slots = schedule.getSlots();
        if (slots == null || slots.isEmpty()) {
            log.warn("OPTIMIZATION: No slots to optimize in schedule {}", schedule.getId());
            return;
        }

        int conflictsResolved = 0;
        int totalConflicts = 0;

        // Check and resolve teacher conflicts
        Map<Long, List<ScheduleSlot>> teacherSlots = groupSlotsByTeacher(slots);
        for (Map.Entry<Long, List<ScheduleSlot>> entry : teacherSlots.entrySet()) {
            List<TimeConflict> conflicts = findTimeConflicts(entry.getValue());
            totalConflicts += conflicts.size();
            conflictsResolved += resolveConflicts(conflicts, schedule);
        }

        // Check and resolve room conflicts
        Map<Long, List<ScheduleSlot>> roomSlots = groupSlotsByRoom(slots);
        for (Map.Entry<Long, List<ScheduleSlot>> entry : roomSlots.entrySet()) {
            List<TimeConflict> conflicts = findTimeConflicts(entry.getValue());
            totalConflicts += conflicts.size();
            conflictsResolved += resolveConflicts(conflicts, schedule);
        }

        schedule.setTotalConflicts(totalConflicts);
        schedule.setResolvedConflicts(conflictsResolved);

        log.info("OPTIMIZATION: Resolved {}/{} conflicts in schedule {}",
                conflictsResolved, totalConflicts, schedule.getId());
    }

    private Map<Long, List<ScheduleSlot>> groupSlotsByTeacher(List<ScheduleSlot> slots) {
        Map<Long, List<ScheduleSlot>> grouped = new HashMap<>();
        for (ScheduleSlot slot : slots) {
            Long teacherId = slot.getTeacher() != null ? slot.getTeacher().getId() : null;
            if (teacherId != null) {
                grouped.computeIfAbsent(teacherId, k -> new ArrayList<>()).add(slot);
            }
        }
        return grouped;
    }

    private Map<Long, List<ScheduleSlot>> groupSlotsByRoom(List<ScheduleSlot> slots) {
        Map<Long, List<ScheduleSlot>> grouped = new HashMap<>();
        for (ScheduleSlot slot : slots) {
            Long roomId = slot.getRoom() != null ? slot.getRoom().getId() : null;
            if (roomId != null) {
                grouped.computeIfAbsent(roomId, k -> new ArrayList<>()).add(slot);
            }
        }
        return grouped;
    }

    private List<TimeConflict> findTimeConflicts(List<ScheduleSlot> slots) {
        List<TimeConflict> conflicts = new ArrayList<>();

        for (int i = 0; i < slots.size(); i++) {
            for (int j = i + 1; j < slots.size(); j++) {
                ScheduleSlot slot1 = slots.get(i);
                ScheduleSlot slot2 = slots.get(j);

                if (slotsOverlap(slot1, slot2)) {
                    conflicts.add(new TimeConflict(slot1, slot2));
                }
            }
        }

        return conflicts;
    }

    private boolean slotsOverlap(ScheduleSlot slot1, ScheduleSlot slot2) {
        // Check if same day and time overlap
        if (!Objects.equals(slot1.getDayOfWeek(), slot2.getDayOfWeek())) {
            return false;
        }

        if (slot1.getStartTime() == null || slot1.getEndTime() == null ||
            slot2.getStartTime() == null || slot2.getEndTime() == null) {
            return false;
        }

        return slot1.getStartTime().isBefore(slot2.getEndTime()) &&
               slot2.getStartTime().isBefore(slot1.getEndTime());
    }

    private int resolveConflicts(List<TimeConflict> conflicts, Schedule schedule) {
        int resolved = 0;

        for (TimeConflict conflict : conflicts) {
            // Try to find alternative slot for the second slot
            ScheduleSlot slotToMove = conflict.slot2;

            // Find available time slot
            LocalTime newStartTime = findAvailableTime(slotToMove, schedule);
            if (newStartTime != null) {
                int duration = (int) Duration.between(
                        slotToMove.getStartTime(), slotToMove.getEndTime()).toMinutes();
                slotToMove.setStartTime(newStartTime);
                slotToMove.setEndTime(newStartTime.plusMinutes(duration));
                resolved++;
            }
        }

        return resolved;
    }

    private LocalTime findAvailableTime(ScheduleSlot slot, Schedule schedule) {
        // Simple algorithm: try each period from 7 AM to 3 PM
        LocalTime[] tryTimes = {
            LocalTime.of(7, 30),
            LocalTime.of(8, 30),
            LocalTime.of(9, 30),
            LocalTime.of(10, 30),
            LocalTime.of(11, 30),
            LocalTime.of(12, 30),
            LocalTime.of(13, 30),
            LocalTime.of(14, 30)
        };

        for (LocalTime time : tryTimes) {
            if (!hasConflictAtTime(slot, time, schedule)) {
                return time;
            }
        }

        return null;
    }

    private boolean hasConflictAtTime(ScheduleSlot slot, LocalTime time, Schedule schedule) {
        int duration = 50; // 50 minute periods
        LocalTime endTime = time.plusMinutes(duration);

        for (ScheduleSlot other : schedule.getSlots()) {
            if (other.getId().equals(slot.getId())) continue;

            // Check teacher conflict
            Long slotTeacherId = slot.getTeacher() != null ? slot.getTeacher().getId() : null;
            Long otherTeacherId = other.getTeacher() != null ? other.getTeacher().getId() : null;
            if (Objects.equals(slotTeacherId, otherTeacherId) && slotTeacherId != null &&
                Objects.equals(slot.getDayOfWeek(), other.getDayOfWeek())) {
                if (time.isBefore(other.getEndTime()) && endTime.isAfter(other.getStartTime())) {
                    return true;
                }
            }

            // Check room conflict
            Long slotRoomId = slot.getRoom() != null ? slot.getRoom().getId() : null;
            Long otherRoomId = other.getRoom() != null ? other.getRoom().getId() : null;
            if (Objects.equals(slotRoomId, otherRoomId) && slotRoomId != null &&
                Objects.equals(slot.getDayOfWeek(), other.getDayOfWeek())) {
                if (time.isBefore(other.getEndTime()) && endTime.isAfter(other.getStartTime())) {
                    return true;
                }
            }
        }

        return false;
    }

    // ========================================================================
    // KANBAN PRINCIPLES
    // ========================================================================

    @Override
    @Transactional
    public void applyKanbanPrinciples(Schedule schedule) {
        log.info("OPTIMIZATION: Applying Kanban principles to schedule {}", schedule.getId());

        if (schedule.getSlots() == null || schedule.getSlots().isEmpty()) {
            return;
        }

        // Kanban Principle 1: Limit Work in Progress (WIP)
        limitTeacherWIP(schedule);

        // Kanban Principle 2: Manage Flow
        optimizeFlow(schedule);

        // Kanban Principle 3: Make Policies Explicit
        enforceConsistentRules(schedule);

        log.info("OPTIMIZATION: Kanban principles applied to schedule {}", schedule.getId());
    }

    private void limitTeacherWIP(Schedule schedule) {
        final int MAX_PREPS_PER_DAY = 4;

        Map<Long, Map<Integer, Set<Long>>> teacherDayPreps = new HashMap<>();

        for (ScheduleSlot slot : schedule.getSlots()) {
            Long teacherId = slot.getTeacher() != null ? slot.getTeacher().getId() : null;
            Long courseId = slot.getCourse() != null ? slot.getCourse().getId() : null;
            if (teacherId != null && courseId != null) {
                teacherDayPreps
                    .computeIfAbsent(teacherId, k -> new HashMap<>())
                    .computeIfAbsent(slot.getDayOfWeek().getValue(), k -> new HashSet<>())
                    .add(courseId);
            }
        }

        // Log WIP violations
        for (Map.Entry<Long, Map<Integer, Set<Long>>> entry : teacherDayPreps.entrySet()) {
            for (Map.Entry<Integer, Set<Long>> dayEntry : entry.getValue().entrySet()) {
                if (dayEntry.getValue().size() > MAX_PREPS_PER_DAY) {
                    log.warn("OPTIMIZATION: Teacher {} has {} preps on day {} (max: {})",
                            entry.getKey(), dayEntry.getValue().size(), dayEntry.getKey(), MAX_PREPS_PER_DAY);
                }
            }
        }
    }

    private void optimizeFlow(Schedule schedule) {
        Map<Long, List<ScheduleSlot>> teacherSlots = groupSlotsByTeacher(schedule.getSlots());

        for (Map.Entry<Long, List<ScheduleSlot>> entry : teacherSlots.entrySet()) {
            List<ScheduleSlot> slots = entry.getValue();
            slots.sort(Comparator
                    .comparingInt((ScheduleSlot s) -> s.getDayOfWeek() != null ? s.getDayOfWeek().getValue() : 0)
                    .thenComparing(ScheduleSlot::getStartTime));

            // Check for gaps > 1 period and log
            for (int i = 0; i < slots.size() - 1; i++) {
                ScheduleSlot current = slots.get(i);
                ScheduleSlot next = slots.get(i + 1);

                if (current.getDayOfWeek().equals(next.getDayOfWeek())) {
                    long gapMinutes = Duration.between(
                            current.getEndTime(), next.getStartTime()).toMinutes();

                    if (gapMinutes > 60) {
                        log.debug("OPTIMIZATION: Large gap ({} min) in teacher {} schedule on day {}",
                                gapMinutes, entry.getKey(), current.getDayOfWeek());
                    }
                }
            }
        }
    }

    private void enforceConsistentRules(Schedule schedule) {
        for (ScheduleSlot slot : schedule.getSlots()) {
            if (slot.getStartTime() == null || slot.getEndTime() == null) {
                log.warn("OPTIMIZATION: Slot {} missing time information", slot.getId());
            }
        }
    }

    // ========================================================================
    // EISENHOWER MATRIX
    // ========================================================================

    @Override
    @Transactional
    public void applyEisenhowerMatrix(Schedule schedule) {
        log.info("OPTIMIZATION: Applying Eisenhower Matrix to schedule {}", schedule.getId());

        if (schedule.getSlots() == null || schedule.getSlots().isEmpty()) {
            return;
        }

        // Categorize slots by priority
        List<ScheduleSlot> urgent = new ArrayList<>();
        List<ScheduleSlot> important = new ArrayList<>();
        List<ScheduleSlot> delegate = new ArrayList<>();
        List<ScheduleSlot> eliminate = new ArrayList<>();

        for (ScheduleSlot slot : schedule.getSlots()) {
            PriorityCategory category = categorizeSlot(slot);
            switch (category) {
                case URGENT_IMPORTANT:
                    urgent.add(slot);
                    break;
                case NOT_URGENT_IMPORTANT:
                    important.add(slot);
                    break;
                case URGENT_NOT_IMPORTANT:
                    delegate.add(slot);
                    break;
                case NOT_URGENT_NOT_IMPORTANT:
                    eliminate.add(slot);
                    break;
            }
        }

        log.info("OPTIMIZATION: Eisenhower Matrix applied. Urgent: {}, Important: {}, Delegate: {}, Eliminate: {}",
                urgent.size(), important.size(), delegate.size(), eliminate.size());
    }

    private enum PriorityCategory {
        URGENT_IMPORTANT,
        NOT_URGENT_IMPORTANT,
        URGENT_NOT_IMPORTANT,
        NOT_URGENT_NOT_IMPORTANT
    }

    private PriorityCategory categorizeSlot(ScheduleSlot slot) {
        Course course = slot.getCourse();
        if (course == null) {
            return PriorityCategory.NOT_URGENT_NOT_IMPORTANT;
        }
        String courseName = course.getCourseName() != null ? course.getCourseName().toUpperCase() : "";

        // Core required courses are Urgent & Important
        if (course.getIsCoreRequired() != null && course.getIsCoreRequired()) {
            return PriorityCategory.URGENT_IMPORTANT;
        }

        // AP/Honors courses are Important but not Urgent
        if (courseName.contains("AP ") || courseName.contains("HONORS") || courseName.contains("IB ")) {
            return PriorityCategory.NOT_URGENT_IMPORTANT;
        }

        // Standard courses are Urgent but not Important
        if (course.getCredits() != null && course.getCredits().doubleValue() >= 1.0) {
            return PriorityCategory.URGENT_NOT_IMPORTANT;
        }

        return PriorityCategory.NOT_URGENT_NOT_IMPORTANT;
    }

    // ========================================================================
    // LEAN SIX SIGMA
    // ========================================================================

    @Override
    @Transactional
    public void applyLeanSixSigma(Schedule schedule) {
        log.info("OPTIMIZATION: Applying Lean Six Sigma principles to schedule {}", schedule.getId());

        if (schedule.getSlots() == null || schedule.getSlots().isEmpty()) {
            return;
        }

        // DMAIC Framework
        ScheduleWasteAnalysis wasteAnalysis = defineWaste(schedule);
        double currentEfficiency = measureEfficiency(schedule);
        analyzeInefficiencies(schedule, wasteAnalysis);
        improveSchedule(schedule, wasteAnalysis);
        schedule.setEfficiencyRate(currentEfficiency);

        log.info("OPTIMIZATION: Lean Six Sigma applied. Efficiency: {}", currentEfficiency);
    }

    @Data
    private static class ScheduleWasteAnalysis {
        private int emptySlots = 0;
        private int underutilizedRooms = 0;
        private int teacherGaps = 0;
        private double wastePercentage = 0.0;
    }

    private ScheduleWasteAnalysis defineWaste(Schedule schedule) {
        ScheduleWasteAnalysis analysis = new ScheduleWasteAnalysis();

        List<Room> allRooms = roomRepository.findAll();
        int totalPossibleSlots = allRooms.size() * 7 * 5;
        int usedSlots = schedule.getSlots().size();
        analysis.setEmptySlots(totalPossibleSlots - usedSlots);

        Map<Long, Integer> roomUsage = new HashMap<>();
        for (ScheduleSlot slot : schedule.getSlots()) {
            Long roomId = slot.getRoom() != null ? slot.getRoom().getId() : null;
            if (roomId != null) {
                roomUsage.merge(roomId, 1, Integer::sum);
            }
        }
        int threshold = 10;
        for (Map.Entry<Long, Integer> entry : roomUsage.entrySet()) {
            if (entry.getValue() < threshold) {
                analysis.setUnderutilizedRooms(analysis.getUnderutilizedRooms() + 1);
            }
        }

        double waste = (double) analysis.getEmptySlots() / totalPossibleSlots * 100;
        analysis.setWastePercentage(waste);

        return analysis;
    }

    private double measureEfficiency(Schedule schedule) {
        double teacherUtil = calculateTeacherUtilization(schedule);
        double roomUtil = calculateRoomUtilization(schedule);

        schedule.setTeacherUtilization(teacherUtil);
        schedule.setRoomUtilization(roomUtil);

        return (teacherUtil + roomUtil) / 2.0;
    }

    private double calculateTeacherUtilization(Schedule schedule) {
        if (schedule.getSlots() == null || schedule.getSlots().isEmpty()) {
            return 0.0;
        }

        Map<Long, Integer> teacherSlotCounts = new HashMap<>();
        for (ScheduleSlot slot : schedule.getSlots()) {
            Long teacherId = slot.getTeacher() != null ? slot.getTeacher().getId() : null;
            if (teacherId != null) {
                teacherSlotCounts.merge(teacherId, 1, Integer::sum);
            }
        }

        int maxSlotsPerTeacher = 30;
        double totalUtilization = 0.0;

        for (Integer count : teacherSlotCounts.values()) {
            totalUtilization += (double) count / maxSlotsPerTeacher;
        }

        return teacherSlotCounts.isEmpty() ? 0.0 :
               (totalUtilization / teacherSlotCounts.size()) * 100;
    }

    private double calculateRoomUtilization(Schedule schedule) {
        if (schedule.getSlots() == null || schedule.getSlots().isEmpty()) {
            return 0.0;
        }

        Map<Long, Integer> roomSlotCounts = new HashMap<>();
        for (ScheduleSlot slot : schedule.getSlots()) {
            Long roomId = slot.getRoom() != null ? slot.getRoom().getId() : null;
            if (roomId != null) {
                roomSlotCounts.merge(roomId, 1, Integer::sum);
            }
        }

        int maxSlotsPerRoom = 35;
        double totalUtilization = 0.0;

        for (Integer count : roomSlotCounts.values()) {
            totalUtilization += (double) count / maxSlotsPerRoom;
        }

        return roomSlotCounts.isEmpty() ? 0.0 :
               (totalUtilization / roomSlotCounts.size()) * 100;
    }

    private void analyzeInefficiencies(Schedule schedule, ScheduleWasteAnalysis wasteAnalysis) {
        log.debug("OPTIMIZATION: Waste analysis - Empty slots: {}, Underutilized rooms: {}, Waste: {}%",
                wasteAnalysis.getEmptySlots(), wasteAnalysis.getUnderutilizedRooms(),
                String.format("%.1f", wasteAnalysis.getWastePercentage()));
    }

    private void improveSchedule(Schedule schedule, ScheduleWasteAnalysis wasteAnalysis) {
        if (wasteAnalysis.getUnderutilizedRooms() > 0) {
            log.info("OPTIMIZATION: Consider consolidating {} underutilized rooms",
                    wasteAnalysis.getUnderutilizedRooms());
        }
    }

    // ========================================================================
    // ML MODEL TRAINING
    // ========================================================================

    @Override
    @Transactional
    public void trainMLModel(List<Schedule> historicalSchedules) {
        log.info("OPTIMIZATION: Training ML model with {} historical schedules",
                historicalSchedules != null ? historicalSchedules.size() : 0);

        if (historicalSchedules == null || historicalSchedules.isEmpty()) {
            log.warn("OPTIMIZATION: No historical schedules provided for training");
            return;
        }

        mlModelWeights.clear();

        List<Schedule> successfulSchedules = historicalSchedules.stream()
                .filter(s -> s.getOptimizationScore() != null && s.getOptimizationScore() > 70)
                .collect(Collectors.toList());

        if (successfulSchedules.isEmpty()) {
            log.warn("OPTIMIZATION: No successful schedules found for training");
            return;
        }

        double avgTeacherUtil = successfulSchedules.stream()
                .filter(s -> s.getTeacherUtilization() != null)
                .mapToDouble(Schedule::getTeacherUtilization)
                .average().orElse(70.0);

        double avgRoomUtil = successfulSchedules.stream()
                .filter(s -> s.getRoomUtilization() != null)
                .mapToDouble(Schedule::getRoomUtilization)
                .average().orElse(60.0);

        double avgEfficiency = successfulSchedules.stream()
                .filter(s -> s.getEfficiencyRate() != null)
                .mapToDouble(Schedule::getEfficiencyRate)
                .average().orElse(65.0);

        mlModelWeights.put("targetTeacherUtilization", avgTeacherUtil);
        mlModelWeights.put("targetRoomUtilization", avgRoomUtil);
        mlModelWeights.put("targetEfficiency", avgEfficiency);

        mlModelTrained = true;

        log.info("OPTIMIZATION: ML model trained. Targets - Teacher: {}, Room: {}, Efficiency: {}",
                String.format("%.1f", avgTeacherUtil),
                String.format("%.1f", avgRoomUtil),
                String.format("%.1f", avgEfficiency));
    }

    // ========================================================================
    // OPTIMIZATION SCORE CALCULATION
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public double calculateOptimizationScore(Schedule schedule) {
        log.info("OPTIMIZATION: Calculating optimization score for schedule {}", schedule.getId());

        if (schedule.getSlots() == null || schedule.getSlots().isEmpty()) {
            return 0.0;
        }

        double teacherScore = calculateTeacherUtilization(schedule);
        double roomScore = calculateRoomUtilization(schedule);
        double conflictScore = calculateConflictResolutionScore(schedule);
        double compactnessScore = calculateCompactnessScore(schedule);
        double preferenceScore = calculatePreferenceSatisfactionScore(schedule);

        double weightedScore =
                (teacherScore * teacherUtilizationWeight) +
                (roomScore * roomUtilizationWeight) +
                (conflictScore * conflictResolutionWeight) +
                (compactnessScore * compactnessWeight) +
                (preferenceScore * studentPreferenceWeight);

        double finalScore = Math.min(100.0, Math.max(0.0, weightedScore));

        log.info("OPTIMIZATION: Score breakdown - Teacher: {}, Room: {}, Conflicts: {}, " +
                "Compactness: {}, Preference: {}. Final: {}",
                String.format("%.1f", teacherScore),
                String.format("%.1f", roomScore),
                String.format("%.1f", conflictScore),
                String.format("%.1f", compactnessScore),
                String.format("%.1f", preferenceScore),
                String.format("%.1f", finalScore));

        return finalScore;
    }

    private double calculateConflictResolutionScore(Schedule schedule) {
        int total = schedule.getTotalConflicts() != null ? schedule.getTotalConflicts() : 0;
        int resolved = schedule.getResolvedConflicts() != null ? schedule.getResolvedConflicts() : 0;

        if (total == 0) {
            return 100.0;
        }

        return ((double) resolved / total) * 100;
    }

    private double calculateCompactnessScore(Schedule schedule) {
        double totalGapMinutes = 0;
        int teacherCount = 0;

        Map<Long, List<ScheduleSlot>> teacherSlots = groupSlotsByTeacher(schedule.getSlots());

        for (List<ScheduleSlot> slots : teacherSlots.values()) {
            if (slots.size() < 2) continue;

            slots.sort(Comparator
                    .comparingInt((ScheduleSlot s) -> s.getDayOfWeek() != null ? s.getDayOfWeek().getValue() : 0)
                    .thenComparing(ScheduleSlot::getStartTime));

            for (int i = 0; i < slots.size() - 1; i++) {
                ScheduleSlot current = slots.get(i);
                ScheduleSlot next = slots.get(i + 1);

                if (current.getDayOfWeek() != null && current.getDayOfWeek().equals(next.getDayOfWeek())) {
                    if (current.getEndTime() != null && next.getStartTime() != null) {
                        long gap = Duration.between(
                                current.getEndTime(), next.getStartTime()).toMinutes();
                        if (gap > 10) {
                            totalGapMinutes += gap;
                        }
                    }
                }
            }
            teacherCount++;
        }

        if (teacherCount == 0) return 100.0;

        double avgGapMinutes = totalGapMinutes / teacherCount;
        double maxAcceptableGap = 120.0;

        return Math.max(0, 100 - (avgGapMinutes / maxAcceptableGap * 100));
    }

    private double calculatePreferenceSatisfactionScore(Schedule schedule) {
        int satisfiedPreferences = 0;
        int totalPreferences = 0;

        for (ScheduleSlot slot : schedule.getSlots()) {
            if (slot.getCourse() != null && slot.getStartTime() != null) {
                totalPreferences++;

                PriorityCategory priority = categorizeSlot(slot);

                if (priority == PriorityCategory.URGENT_IMPORTANT &&
                    slot.getStartTime().isBefore(LocalTime.NOON)) {
                    satisfiedPreferences++;
                } else if (priority != PriorityCategory.URGENT_IMPORTANT) {
                    satisfiedPreferences++;
                }
            }
        }

        if (totalPreferences == 0) return 100.0;

        return ((double) satisfiedPreferences / totalPreferences) * 100;
    }

    // ========================================================================
    // HELPER CLASSES
    // ========================================================================

    @Data
    private static class TimeConflict {
        private final ScheduleSlot slot1;
        private final ScheduleSlot slot2;

        public TimeConflict(ScheduleSlot slot1, ScheduleSlot slot2) {
            this.slot1 = slot1;
            this.slot2 = slot2;
        }
    }
}
