package com.heronix.dto;

import com.heronix.model.enums.EnrollmentRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollmentRequestDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private String courseCode;
    private Integer preferenceRank;
    private Integer priorityScore;
    private EnrollmentRequestStatus requestStatus;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String statusReason;
    private Boolean isWaitlist;
    private Integer waitlistPosition;
    private String notes;
}
