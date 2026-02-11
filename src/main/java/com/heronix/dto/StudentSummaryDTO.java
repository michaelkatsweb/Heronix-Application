package com.heronix.dto;

/**
 * Lightweight DTO for Student, used in ComboBox selections.
 * Contains only the fields needed for display and identification.
 */
public class StudentSummaryDTO {

    private Long id;
    private String studentId;
    private String fullName;

    public StudentSummaryDTO() {}

    public StudentSummaryDTO(Long id, String studentId, String fullName) {
        this.id = id;
        this.studentId = studentId;
        this.fullName = fullName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
