package com.heronix.dto;

import com.heronix.model.domain.ScheduleChangeRequest.RequestStatus;
import com.heronix.model.domain.ScheduleChangeRequest.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Schedule Change Request
 * Used for API requests and responses
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleChangeRequestDTO {

    private Long id;

    private Long studentId;

    private String studentName;

    private String studentNumber;

    private RequestType requestType;

    private Long currentCourseId;

    private String currentCourseName;

    private String currentCourseCode;

    private Long currentSectionId;

    private String currentSectionName;

    private Long requestedCourseId;

    private String requestedCourseName;

    private String requestedCourseCode;

    private Long requestedSectionId;

    private String requestedSectionName;

    private String reason;

    private String studentNotes;

    private String parentContact;

    private Boolean parentContacted;

    private LocalDateTime requestDate;

    private RequestStatus status;

    private Integer priorityLevel;

    private Long reviewedById;

    private String reviewedByName;

    private LocalDateTime reviewedDate;

    private String reviewNotes;

    private String denialReason;

    private LocalDateTime completionDate;

    private Long academicYearId;

    private String academicYearName;

    private Long gradingPeriodId;

    private String gradingPeriodName;

    // Calculated fields for response
    private String requestSummary;

    private Long daysSinceRequest;

    private Boolean isOverdue;

    private Boolean canAutoApprove;
}
