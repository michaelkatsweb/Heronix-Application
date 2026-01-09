package com.heronix.dto.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * School/District information for SchedulerV2
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolInfoDTO {

    private Long schoolId;

    private String schoolName;

    private String districtName;

    private Long campusId;

    private String campusName;

    private String academicYear;

    private Integer totalStudents;

    private Integer totalTeachers;

    private Integer totalRooms;
}
