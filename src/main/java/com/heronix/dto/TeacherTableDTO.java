package com.heronix.dto;

import com.heronix.model.enums.TeacherRole;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Teacher table display in TeacherManagementController.
 * Pre-resolves all lazy-loaded collections within @Transactional boundary
 * to avoid LazyInitializationException in JavaFX UI thread.
 */
@Data
public class TeacherTableDTO {
    private Long id;
    private String name;
    private String employeeId;
    private TeacherRole role;
    private String roleDisplay;
    private String department;
    private String email;
    private String phoneNumber;
    private String certificationsDisplay;
    private List<String> certifiedSubjects = new ArrayList<>();
    private int courseCount;
    private String courseCountDisplay;
    private Integer maxHoursPerWeek;
    private Boolean active;
    private String workloadIndicator;
}
