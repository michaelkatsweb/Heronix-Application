package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {
    private Long id;
    private String courseCode;
    private String courseName;
    private String subject;
    private BigDecimal credits;
    private Integer minGradeLevel;
    private Integer maxGradeLevel;
    private String description;
    private Boolean isCoreRequired;
    private Boolean active;
}
