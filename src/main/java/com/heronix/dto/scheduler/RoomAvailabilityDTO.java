package com.heronix.dto.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Room availability and capabilities for SchedulerV2
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAvailabilityDTO {

    private Long roomId;

    private String roomNumber;

    private String buildingName;

    private String roomType;

    private Integer capacity;

    /**
     * Equipment available in this room
     */
    private List<String> equipment;

    /**
     * Departments that have priority for this room
     */
    private List<String> assignedDepartments;

    /**
     * Is this room ADA accessible?
     */
    private Boolean isAccessible;

    /**
     * Is this room available for scheduling?
     */
    private Boolean isAvailable;

    /**
     * Time slots when room is unavailable
     */
    private List<UnavailableSlot> unavailableSlots;

    /**
     * Courses that should be scheduled in this room (pinned)
     */
    private List<Long> pinnedCourseIds;

    /**
     * Unavailable time slot
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnavailableSlot {
        private Integer dayOfWeek; // 1=Monday, 5=Friday
        private Integer periodNumber;
        private String reason;
    }
}
