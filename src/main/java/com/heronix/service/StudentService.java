package com.heronix.service;

import com.heronix.model.domain.Student;
import com.heronix.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for Student operations
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;

    /**
     * Get all students (excluding soft-deleted)
     */
    public List<Student> getAllStudents() {
        log.debug("Getting all students (excluding deleted)");
        return studentRepository.findAllNonDeleted();
    }

    /**
     * Alias for getAllStudents
     */
    public List<Student> findAllStudents() {
        return getAllStudents();
    }

    /**
     * Another alias for getAllStudents
     */
    public List<Student> findAll() {
        return getAllStudents();
    }

    /**
     * Get student by ID
     */
    public Optional<Student> getStudentById(Long id) {
        log.debug("Getting student by ID: {}", id);
        return studentRepository.findById(id);
    }

    /**
     * Get student by student ID
     */
    public Optional<Student> getStudentByStudentId(String studentId) {
        log.debug("Getting student by student ID: {}", studentId);
        return studentRepository.findByStudentId(studentId);
    }

    /**
     * Create student
     */
    public Student createStudent(Student student) {
        log.info("Creating student: {}", student.getStudentId());
        return studentRepository.save(student);
    }

    /**
     * Update student
     */
    public Student updateStudent(Student student) {
        log.info("Updating student: {}", student.getId());
        return studentRepository.save(student);
    }

    /**
     * Delete student
     */
    public void deleteStudent(Long id) {
        log.info("Deleting student ID: {}", id);
        studentRepository.deleteById(id);
    }

    /**
     * Search students by name
     */
    public List<Student> searchByName(String searchTerm) {
        log.debug("Searching students by name: {}", searchTerm);
        return studentRepository.searchByName(searchTerm);
    }

    /**
     * Get active students
     */
    public List<Student> getActiveStudents() {
        log.debug("Getting active students");
        return studentRepository.findByStudentStatus(Student.StudentStatus.ACTIVE);
    }

    /**
     * Count all students
     */
    public long countAllStudents() {
        return studentRepository.count();
    }

    /**
     * Count active students
     */
    public long countActiveStudents() {
        return studentRepository.countByStudentStatus(Student.StudentStatus.ACTIVE);
    }

    /**
     * Save student (alias for create/update)
     */
    public Student save(Student student) {
        log.info("Saving student: {}", student.getStudentId());
        return studentRepository.save(student);
    }

    /**
     * Delete student entity
     */
    public void delete(Student student) {
        log.info("Deleting student: {}", student.getStudentId());
        studentRepository.delete(student);
    }

    /**
     * Find student by ID (returns Student or null)
     */
    public Student findById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    /**
     * Find student by student ID
     */
    public Optional<Student> findByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    /**
     * Find all active students
     */
    public List<Student> findByActiveTrue() {
        return studentRepository.findByActiveTrue();
    }

    /**
     * Find all students with enrolled courses (eager loading)
     */
    @Transactional(readOnly = true)
    public List<Student> findAllWithEnrolledCourses() {
        return studentRepository.findAllWithEnrolledCourses();
    }

    /**
     * Find student by ID with enrolled courses
     */
    @Transactional(readOnly = true)
    public Optional<Student> findByIdWithEnrolledCourses(Long id) {
        return studentRepository.findByIdWithEnrolledCourses(id);
    }

    /**
     * Find student by ID with emergency contacts eagerly loaded
     */
    @Transactional(readOnly = true)
    public Optional<Student> findByIdWithEmergencyContacts(Long id) {
        return studentRepository.findByIdWithEmergencyContacts(id);
    }

    /**
     * Check if student exists by student ID
     */
    public boolean existsByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId).isPresent();
    }
}
