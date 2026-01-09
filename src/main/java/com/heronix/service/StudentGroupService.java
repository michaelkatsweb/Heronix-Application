package com.heronix.service;

import com.heronix.model.domain.StudentGroup;
import com.heronix.model.domain.StudentGroup.GroupType;
import com.heronix.model.domain.StudentGroup.GroupStatus;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.User;
import com.heronix.repository.StudentGroupRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for StudentGroup management
 *
 * Handles all business logic for student groupings including:
 * - Homeroom assignments
 * - Advisory groups
 * - Cohorts (graduating classes)
 * - House system
 * - Team assignments
 * - Learning communities
 * - Academic tracks
 * - Extracurricular clubs
 * - Intervention groups
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StudentGroupService {

    private final StudentGroupRepository groupRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================

    /**
     * Create new student group
     */
    public StudentGroup createGroup(String groupName, GroupType groupType, String academicYear, Long createdByStaffId) {
        log.info("Creating {} group: {}", groupType, groupName);

        User createdBy = userRepository.findById(createdByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + createdByStaffId));

        StudentGroup group = StudentGroup.builder()
                .groupName(groupName)
                .groupType(groupType)
                .status(GroupStatus.ACTIVE)
                .academicYear(academicYear)
                .acceptingNewMembers(true)
                .createdBy(createdBy)
                .build();

        return groupRepository.save(group);
    }

    /**
     * Get group by ID
     */
    public Optional<StudentGroup> getGroupById(Long id) {
        return groupRepository.findById(id);
    }

    /**
     * Get all groups
     */
    public List<StudentGroup> getAllGroups() {
        return groupRepository.findAll();
    }

    /**
     * Get active groups
     */
    public List<StudentGroup> getActiveGroups() {
        return groupRepository.findByStatus(GroupStatus.ACTIVE);
    }

    /**
     * Get groups by type
     */
    public List<StudentGroup> getGroupsByType(GroupType groupType) {
        return groupRepository.findActiveByType(groupType);
    }

    /**
     * Create group (overload - accepts group and userId)
     */
    public StudentGroup createGroup(StudentGroup group, Long userId) {
        if (group == null) {
            throw new IllegalArgumentException("Group cannot be null");
        }
        return createGroup(group.getGroupName(), group.getGroupType(), group.getAcademicYear(), userId);
    }

    /**
     * Update group
     */
    public StudentGroup updateGroup(Long id, StudentGroup updatedGroup, Long updatedByStaffId) {
        log.info("Updating group ID: {}", id);

        StudentGroup existing = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + id));

        User updatedBy = userRepository.findById(updatedByStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + updatedByStaffId));

        // Update basic fields
        existing.setGroupName(updatedGroup.getGroupName());
        existing.setGroupCode(updatedGroup.getGroupCode());
        existing.setDescription(updatedGroup.getDescription());
        existing.setStatus(updatedGroup.getStatus());
        existing.setMaxCapacity(updatedGroup.getMaxCapacity());
        existing.setAcceptingNewMembers(updatedGroup.getAcceptingNewMembers());
        existing.setGradeLevel(updatedGroup.getGradeLevel());

        // Update dates
        existing.setStartDate(updatedGroup.getStartDate());
        existing.setEndDate(updatedGroup.getEndDate());

        // Update meeting info
        existing.setMeetingLocation(updatedGroup.getMeetingLocation());
        existing.setMeetingSchedule(updatedGroup.getMeetingSchedule());
        existing.setMeetingNotes(updatedGroup.getMeetingNotes());

        // Update type-specific fields
        existing.setHomeroomNumber(updatedGroup.getHomeroomNumber());
        existing.setAdvisoryFocus(updatedGroup.getAdvisoryFocus());
        existing.setGraduationYear(updatedGroup.getGraduationYear());
        existing.setCohortName(updatedGroup.getCohortName());
        existing.setHouseName(updatedGroup.getHouseName());
        existing.setHouseColor(updatedGroup.getHouseColor());
        existing.setHousePoints(updatedGroup.getHousePoints());
        existing.setTeamName(updatedGroup.getTeamName());
        existing.setSportOrActivity(updatedGroup.getSportOrActivity());

        existing.setUpdatedBy(updatedBy);

        return groupRepository.save(existing);
    }

    /**
     * Update group (overload - accepts group and userId)
     */
    public StudentGroup updateGroup(StudentGroup group, Long userId) {
        if (group == null || group.getId() == null) {
            throw new IllegalArgumentException("Group and Group ID cannot be null for update");
        }
        return updateGroup(group.getId(), group, userId);
    }

    /**
     * Delete group
     */
    public void deleteGroup(Long id) {
        log.info("Deleting group ID: {}", id);

        StudentGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + id));

        if (group.getStudents() != null && !group.getStudents().isEmpty()) {
            throw new IllegalStateException("Cannot delete group with enrolled students. Remove all students first.");
        }

        groupRepository.deleteById(id);
    }

    // ========================================================================
    // STUDENT ENROLLMENT MANAGEMENT
    // ========================================================================

    /**
     * Enroll student in group
     */
    public StudentGroup enrollStudent(Long groupId, Long studentId, Long staffId) {
        log.info("Enrolling student {} in group {}", studentId, groupId);

        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        // Check if group is accepting members
        if (!Boolean.TRUE.equals(group.getAcceptingNewMembers())) {
            throw new IllegalStateException("Group is not accepting new members");
        }

        // Check capacity
        if (group.isFull()) {
            throw new IllegalStateException("Group is at full capacity");
        }

        // Check if student is already enrolled
        if (group.getStudents().contains(student)) {
            throw new IllegalStateException("Student is already enrolled in this group");
        }

        // Add student to group
        boolean added = group.addStudent(student);
        if (!added) {
            throw new IllegalStateException("Failed to add student to group");
        }

        group.setUpdatedBy(staff);
        return groupRepository.save(group);
    }

    /**
     * Remove student from group
     */
    public StudentGroup removeStudent(Long groupId, Long studentId, Long staffId) {
        log.info("Removing student {} from group {}", studentId, groupId);

        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        boolean removed = group.removeStudent(student);
        if (!removed) {
            throw new IllegalStateException("Student is not enrolled in this group");
        }

        group.setUpdatedBy(staff);
        return groupRepository.save(group);
    }

    /**
     * Get groups for a student
     */
    public List<StudentGroup> getGroupsForStudent(Long studentId) {
        return groupRepository.findActiveByStudentId(studentId);
    }

    /**
     * Get groups for student by type
     */
    public List<StudentGroup> getGroupsForStudentByType(Long studentId, GroupType groupType) {
        return groupRepository.findByStudentIdAndType(studentId, groupType);
    }

    /**
     * Check if student is in group
     */
    public boolean isStudentInGroup(Long groupId, Long studentId) {
        return groupRepository.isStudentInGroup(groupId, studentId);
    }

    /**
     * Bulk enroll students
     */
    public StudentGroup bulkEnrollStudents(Long groupId, List<Long> studentIds, Long staffId) {
        log.info("Bulk enrolling {} students in group {}", studentIds.size(), groupId);

        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        int enrolled = 0;
        for (Long studentId : studentIds) {
            try {
                enrollStudent(groupId, studentId, staffId);
                enrolled++;
            } catch (Exception e) {
                log.warn("Failed to enroll student {}: {}", studentId, e.getMessage());
            }
        }

        log.info("Successfully enrolled {}/{} students", enrolled, studentIds.size());

        // Return the updated group
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
    }

    // ========================================================================
    // STAFF ASSIGNMENT
    // ========================================================================

    /**
     * Assign primary advisor to group
     */
    public StudentGroup assignPrimaryAdvisor(Long groupId, Long advisorId, Long staffId) {
        log.info("Assigning primary advisor {} to group {}", advisorId, groupId);

        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        User advisor = userRepository.findById(advisorId)
                .orElseThrow(() -> new IllegalArgumentException("Advisor not found: " + advisorId));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        group.setPrimaryAdvisor(advisor);
        group.setUpdatedBy(staff);

        return groupRepository.save(group);
    }

    /**
     * Assign homeroom teacher
     */
    public StudentGroup assignHomeroomTeacher(Long groupId, Long teacherId, Long staffId) {
        log.info("Assigning homeroom teacher {} to group {}", teacherId, groupId);

        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!Boolean.TRUE.equals(group.getIsHomeroom())) {
            throw new IllegalStateException("Group is not a homeroom");
        }

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        group.setHomeroomTeacher(teacher);
        group.setUpdatedBy(staff);

        return groupRepository.save(group);
    }

    /**
     * Get groups by advisor
     */
    public List<StudentGroup> getGroupsByAdvisor(Long advisorId) {
        return groupRepository.findActiveByPrimaryAdvisor(advisorId);
    }

    // ========================================================================
    // TYPE-SPECIFIC QUERIES
    // ========================================================================

    /**
     * Get all homerooms
     */
    public List<StudentGroup> getAllHomerooms() {
        return groupRepository.findAllHomerooms();
    }

    /**
     * Get homeroom by number
     */
    public Optional<StudentGroup> getHomeroomByNumber(String homeroomNumber) {
        return groupRepository.findByHomeroomNumber(homeroomNumber);
    }

    /**
     * Get all advisory groups
     */
    public List<StudentGroup> getAllAdvisories() {
        return groupRepository.findAllAdvisories();
    }

    /**
     * Get all cohorts
     */
    public List<StudentGroup> getAllCohorts() {
        return groupRepository.findAllCohorts();
    }

    /**
     * Get cohort by graduation year
     */
    public Optional<StudentGroup> getCohortByGraduationYear(Integer graduationYear) {
        return groupRepository.findByGraduationYear(graduationYear);
    }

    /**
     * Get all houses
     */
    public List<StudentGroup> getAllHouses() {
        return groupRepository.findAllHouses();
    }

    /**
     * Get house by name
     */
    public Optional<StudentGroup> getHouseByName(String houseName) {
        return groupRepository.findByHouseName(houseName);
    }

    /**
     * Get all teams
     */
    public List<StudentGroup> getAllTeams() {
        return groupRepository.findAllTeams();
    }

    /**
     * Get all learning communities
     */
    public List<StudentGroup> getAllLearningCommunities() {
        return groupRepository.findAllLearningCommunities();
    }

    /**
     * Get all academic tracks
     */
    public List<StudentGroup> getAllAcademicTracks() {
        return groupRepository.findAllAcademicTracks();
    }

    /**
     * Get all extracurricular clubs
     */
    public List<StudentGroup> getAllExtracurricularClubs() {
        return groupRepository.findAllExtracurricularClubs();
    }

    /**
     * Get all intervention groups
     */
    public List<StudentGroup> getAllInterventionGroups() {
        return groupRepository.findAllInterventionGroups();
    }

    // ========================================================================
    // ACADEMIC YEAR MANAGEMENT
    // ========================================================================

    /**
     * Get groups for academic year
     */
    public List<StudentGroup> getGroupsByAcademicYear(String academicYear) {
        return groupRepository.findActiveByAcademicYear(academicYear);
    }

    /**
     * Get groups for academic year and type
     */
    public List<StudentGroup> getGroupsByAcademicYearAndType(String academicYear, GroupType groupType) {
        return groupRepository.findByAcademicYearAndGroupType(academicYear, groupType);
    }

    // ========================================================================
    // CAPACITY MANAGEMENT
    // ========================================================================

    /**
     * Get groups with available capacity
     */
    public List<StudentGroup> getGroupsWithCapacity() {
        return groupRepository.findGroupsWithCapacity();
    }

    /**
     * Get full groups
     */
    public List<StudentGroup> getFullGroups() {
        return groupRepository.findFullGroups();
    }

    /**
     * Get groups near capacity (90% full)
     */
    public List<StudentGroup> getGroupsNearCapacity() {
        return groupRepository.findGroupsNearCapacity(90.0);
    }

    /**
     * Set group capacity
     */
    public StudentGroup setCapacity(Long groupId, Integer maxCapacity, Long staffId) {
        log.info("Setting capacity for group {} to {}", groupId, maxCapacity);

        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        // Check if reducing capacity would exceed current enrollment
        if (maxCapacity != null && group.getCurrentEnrollment() != null &&
            maxCapacity < group.getCurrentEnrollment()) {
            throw new IllegalArgumentException(
                "Cannot set capacity below current enrollment (" + group.getCurrentEnrollment() + ")");
        }

        group.setMaxCapacity(maxCapacity);
        group.setUpdatedBy(staff);

        return groupRepository.save(group);
    }

    /**
     * Toggle accepting new members
     */
    public StudentGroup toggleAcceptingMembers(Long groupId, boolean accepting, Long staffId) {
        log.info("Setting group {} accepting members: {}", groupId, accepting);

        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        group.setAcceptingNewMembers(accepting);
        group.setUpdatedBy(staff);

        return groupRepository.save(group);
    }

    // ========================================================================
    // STATUS MANAGEMENT
    // ========================================================================

    /**
     * Activate group
     */
    public StudentGroup activateGroup(Long id, Long staffId) {
        log.info("Activating group ID: {}", id);

        StudentGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + id));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        group.setStatus(GroupStatus.ACTIVE);
        if (group.getStartDate() == null) {
            group.setStartDate(LocalDate.now());
        }
        group.setUpdatedBy(staff);

        return groupRepository.save(group);
    }

    /**
     * Deactivate group
     */
    public StudentGroup deactivateGroup(Long id, Long staffId) {
        log.info("Deactivating group ID: {}", id);

        StudentGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + id));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        group.setStatus(GroupStatus.INACTIVE);
        if (group.getEndDate() == null) {
            group.setEndDate(LocalDate.now());
        }
        group.setUpdatedBy(staff);

        return groupRepository.save(group);
    }

    /**
     * Archive group
     */
    public StudentGroup archiveGroup(Long id, Long staffId) {
        log.info("Archiving group ID: {}", id);

        StudentGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + id));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        group.setStatus(GroupStatus.ARCHIVED);
        group.setEndDate(LocalDate.now());
        group.setAcceptingNewMembers(false);
        group.setUpdatedBy(staff);

        return groupRepository.save(group);
    }

    // ========================================================================
    // SEARCH OPERATIONS
    // ========================================================================

    /**
     * Search groups by name
     */
    public List<StudentGroup> searchGroupsByName(String searchTerm) {
        return groupRepository.searchByName(searchTerm);
    }

    /**
     * Search groups by name or code
     */
    public List<StudentGroup> searchGroupsByNameOrCode(String searchTerm) {
        return groupRepository.searchByNameOrCode(searchTerm);
    }

    /**
     * Find group by name
     */
    public Optional<StudentGroup> findByGroupName(String groupName) {
        return groupRepository.findByGroupName(groupName);
    }

    /**
     * Find group by code
     */
    public Optional<StudentGroup> findByGroupCode(String groupCode) {
        return groupRepository.findByGroupCode(groupCode);
    }

    // ========================================================================
    // STATISTICAL QUERIES
    // ========================================================================

    /**
     * Count groups by type
     */
    public long countByType(GroupType groupType) {
        return groupRepository.countByGroupType(groupType);
    }

    /**
     * Count active groups
     */
    public long countActiveGroups() {
        return groupRepository.countActive();
    }

    /**
     * Get total enrollment across all groups
     */
    public Long getTotalEnrollment() {
        Long total = groupRepository.getTotalEnrollment();
        return total != null ? total : 0L;
    }

    /**
     * Get average group size
     */
    public Double getAverageGroupSize() {
        Double avg = groupRepository.getAverageGroupSize();
        return avg != null ? avg : 0.0;
    }


    /**
     * Get capacity utilization
     */
    public List<Object[]> getCapacityUtilization() {
        return groupRepository.getCapacityUtilization();
    }

    // ========================================================================
    // HOUSE SYSTEM MANAGEMENT
    // ========================================================================

    /**
     * Update house points
     */
    public StudentGroup updateHousePoints(Long groupId, Integer points, Long staffId) {
        log.info("Updating house points for group {} to {}", groupId, points);

        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!Boolean.TRUE.equals(group.getIsHouse())) {
            throw new IllegalStateException("Group is not a house");
        }

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        group.setHousePoints(points);
        group.setUpdatedBy(staff);

        return groupRepository.save(group);
    }

    /**
     * Add house points
     */
    public StudentGroup addHousePoints(Long groupId, Integer pointsToAdd, Long staffId) {
        log.info("Adding {} points to house {}", pointsToAdd, groupId);

        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!Boolean.TRUE.equals(group.getIsHouse())) {
            throw new IllegalStateException("Group is not a house");
        }

        Integer currentPoints = group.getHousePoints() != null ? group.getHousePoints() : 0;
        return updateHousePoints(groupId, currentPoints + pointsToAdd, staffId);
    }

    /**
     * Get house rankings
     */
    public List<StudentGroup> getHouseRankings() {
        List<StudentGroup> houses = getAllHouses();
        houses.sort((h1, h2) -> {
            Integer points1 = h1.getHousePoints() != null ? h1.getHousePoints() : 0;
            Integer points2 = h2.getHousePoints() != null ? h2.getHousePoints() : 0;
            return points2.compareTo(points1); // Descending order
        });
        return houses;
    }

    // ========================================================================
    // BULK OPERATIONS
    // ========================================================================

    /**
     * Archive groups for academic year
     */
    public int archiveGroupsForAcademicYear(String academicYear, Long staffId) {
        log.info("Archiving all groups for academic year: {}", academicYear);

        List<StudentGroup> groups = groupRepository.findByAcademicYear(academicYear);
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        for (StudentGroup group : groups) {
            group.setStatus(GroupStatus.ARCHIVED);
            group.setEndDate(LocalDate.now());
            group.setAcceptingNewMembers(false);
            group.setUpdatedBy(staff);
        }

        groupRepository.saveAll(groups);
        log.info("Archived {} groups", groups.size());

        return groups.size();
    }

    /**
     * Clone group for new academic year
     */
    public StudentGroup cloneGroupForNewYear(Long groupId, String newAcademicYear, Long staffId) {
        log.info("Cloning group {} for academic year {}", groupId, newAcademicYear);

        StudentGroup original = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        StudentGroup cloned = StudentGroup.builder()
                .groupName(original.getGroupName())
                .groupCode(original.getGroupCode())
                .groupType(original.getGroupType())
                .description(original.getDescription())
                .status(GroupStatus.ACTIVE)
                .academicYear(newAcademicYear)
                .gradeLevel(original.getGradeLevel())
                .maxCapacity(original.getMaxCapacity())
                .acceptingNewMembers(true)
                .primaryAdvisor(original.getPrimaryAdvisor())
                .primaryRoom(original.getPrimaryRoom())
                .meetingLocation(original.getMeetingLocation())
                .meetingSchedule(original.getMeetingSchedule())
                .createdBy(staff)
                .build();

        // Copy type-specific fields
        cloned.setIsHomeroom(original.getIsHomeroom());
        cloned.setHomeroomNumber(original.getHomeroomNumber());
        cloned.setHomeroomTeacher(original.getHomeroomTeacher());
        cloned.setIsAdvisory(original.getIsAdvisory());
        cloned.setAdvisoryFocus(original.getAdvisoryFocus());
        cloned.setIsHouse(original.getIsHouse());
        cloned.setHouseName(original.getHouseName());
        cloned.setHouseColor(original.getHouseColor());

        return groupRepository.save(cloned);
    }

    // ========================================================================
    // ADDITIONAL METHODS FOR API SUPPORT
    // ========================================================================

    /**
     * Get student IDs in a group
     */
    public List<Long> getStudentIdsInGroup(Long groupId) {
        log.debug("Getting student IDs for group: {}", groupId);
        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        return group.getStudents().stream()
                .map(Student::getId)
                .toList();
    }

    /**
     * Bulk remove students
     */
    public StudentGroup bulkRemoveStudents(Long groupId, List<Long> studentIds, Long staffId) {
        log.info("Bulk removing {} students from group {}", studentIds.size(), groupId);

        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        for (Long studentId : studentIds) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            group.removeStudent(student);
        }

        group.setUpdatedBy(staff);
        return groupRepository.save(group);
    }

    /**
     * Get active groups for a student
     */
    public List<StudentGroup> getActiveGroupsForStudent(Long studentId) {
        log.debug("Getting active groups for student: {}", studentId);
        return groupRepository.findActiveByStudentId(studentId);
    }

    /**
     * Get groups by student and type
     */
    public List<StudentGroup> getGroupsByStudentAndType(Long studentId, GroupType type) {
        log.debug("Getting {} groups for student: {}", type, studentId);
        return groupRepository.findByStudentIdAndType(studentId, type);
    }

    /**
     * Get active groups by type
     */
    public List<StudentGroup> getActiveGroupsByType(GroupType groupType) {
        log.debug("Getting active groups by type: {}", groupType);
        return groupRepository.findActiveByType(groupType);
    }

    /**
     * Get active groups by academic year
     */
    public List<StudentGroup> getActiveGroupsByAcademicYear(String academicYear) {
        log.debug("Getting active groups for academic year: {}", academicYear);
        return groupRepository.findActiveByAcademicYear(academicYear);
    }

    /**
     * Get active groups by primary advisor
     */
    public List<StudentGroup> getActiveGroupsByPrimaryAdvisor(Long advisorId) {
        log.debug("Getting active groups by advisor: {}", advisorId);
        return groupRepository.findActiveByPrimaryAdvisor(advisorId);
    }

    /**
     * Assign secondary advisor
     */
    public StudentGroup assignSecondaryAdvisor(Long groupId, Long advisorId, Long staffId) {
        log.info("Assigning secondary advisor {} to group {}", advisorId, groupId);

        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        User advisor = userRepository.findById(advisorId)
                .orElseThrow(() -> new IllegalArgumentException("Advisor not found: " + advisorId));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        group.setSecondaryAdvisor(advisor);
        group.setUpdatedBy(staff);

        return groupRepository.save(group);
    }

    /**
     * Get groups near capacity
     */
    public List<StudentGroup> getGroupsNearCapacity(double percentage) {
        log.debug("Getting groups near capacity ({}%)", percentage);
        return groupRepository.findGroupsNearCapacity(percentage);
    }

    /**
     * Set accepting new members status
     */
    public StudentGroup setAcceptingNewMembers(Long groupId, Boolean accepting, Long staffId) {
        log.info("Setting group {} accepting members to: {}", groupId, accepting);
        return toggleAcceptingMembers(groupId, accepting, staffId);
    }

    /**
     * Get groups by status
     */
    public List<StudentGroup> getGroupsByStatus(GroupStatus status) {
        log.debug("Getting groups by status: {}", status);
        return groupRepository.findByStatus(status);
    }

    /**
     * Get house leaderboard
     */
    public List<StudentGroup> getHouseLeaderboard() {
        log.debug("Getting house leaderboard");
        return getAllHouses().stream()
                .sorted((h1, h2) -> {
                    Integer points1 = h1.getHousePoints() != null ? h1.getHousePoints() : 0;
                    Integer points2 = h2.getHousePoints() != null ? h2.getHousePoints() : 0;
                    return points2.compareTo(points1);
                })
                .toList();
    }

    /**
     * Subtract house points
     */
    public StudentGroup subtractHousePoints(Long groupId, Integer points, Long staffId) {
        log.info("Subtracting {} points from group {}", points, groupId);

        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!Boolean.TRUE.equals(group.getIsHouse())) {
            throw new IllegalStateException("Group is not a house");
        }

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        Integer currentPoints = group.getHousePoints() != null ? group.getHousePoints() : 0;
        group.setHousePoints(Math.max(0, currentPoints - points));
        group.setUpdatedBy(staff);

        return groupRepository.save(group);
    }

    /**
     * Reset house points
     */
    public StudentGroup resetHousePoints(Long groupId, Long staffId) {
        log.info("Resetting house points for group {}", groupId);

        StudentGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!Boolean.TRUE.equals(group.getIsHouse())) {
            throw new IllegalStateException("Group is not a house");
        }

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        group.setHousePoints(0);
        group.setUpdatedBy(staff);

        return groupRepository.save(group);
    }

    /**
     * Get group statistics
     */
    public Map<String, Object> getGroupStatistics() {
        log.debug("Getting group statistics");
        Map<String, Object> stats = new HashMap<>();

        List<Object[]> rawStats = groupRepository.getGroupStatistics();
        Map<String, Map<String, Long>> statsByType = new HashMap<>();

        for (Object[] stat : rawStats) {
            GroupType type = (GroupType) stat[0];
            Long count = (Long) stat[1];
            Long enrollment = (Long) stat[2];

            Map<String, Long> typeStats = new HashMap<>();
            typeStats.put("count", count);
            typeStats.put("totalEnrollment", enrollment);

            statsByType.put(type.name(), typeStats);
        }

        stats.put("byType", statsByType);
        stats.put("totalGroups", groupRepository.countActive());
        stats.put("totalEnrollment", groupRepository.getTotalEnrollment());
        stats.put("averageGroupSize", groupRepository.getAverageGroupSize());

        return stats;
    }

    /**
     * Count active groups
     */
    public Long countActive() {
        return groupRepository.countActive();
    }

    /**
     * Bulk archive by academic year
     */
    public List<StudentGroup> bulkArchiveByAcademicYear(String academicYear, Long staffId) {
        log.info("Bulk archiving groups for academic year: {}", academicYear);

        List<StudentGroup> groups = groupRepository.findByAcademicYear(academicYear);
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found: " + staffId));

        for (StudentGroup group : groups) {
            group.setStatus(GroupStatus.ARCHIVED);
            group.setUpdatedBy(staff);
        }

        return groupRepository.saveAll(groups);
    }

    /**
     * Get groups by primary advisor
     */
    public List<StudentGroup> getGroupsByPrimaryAdvisor(Long advisorId) {
        return groupRepository.findByPrimaryAdvisorId(advisorId);
    }

    /**
     * Get groups by grade level
     */
    public List<StudentGroup> getGroupsByGradeLevel(String gradeLevel) {
        return groupRepository.findByGradeLevel(gradeLevel);
    }

    /**
     * Get active groups by grade level
     */
    public List<StudentGroup> getActiveGroupsByGradeLevel(String gradeLevel) {
        return groupRepository.findActiveByGradeLevel(gradeLevel);
    }
}
