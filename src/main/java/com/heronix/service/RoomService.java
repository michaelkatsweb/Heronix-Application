// Location: src/main/java/com/heronix/service/RoomService.java
package com.heronix.service;

import com.heronix.model.domain.Room;
import java.util.List;
import java.util.Optional;

/**
 * Room Service Interface
 * Defines operations for managing rooms
 *
 * @author Heronix Scheduling System Team
 * @version 1.2.0
 * @since 2025-10-25
 */
public interface RoomService {

    /**
     * Get all active rooms
     *
     * @return List of active rooms
     */
    List<Room> getAllActiveRooms();

    /**
     * Get room by ID
     *
     * @param id Room ID
     * @return Room object
     */
    Room getRoomById(Long id);

    /**
     * Get all rooms (including inactive)
     *
     * @return List of all rooms
     */
    List<Room> findAll();

    /**
     * Save a new room or update an existing room
     *
     * @param room Room to save
     * @return Saved room with generated ID
     */
    Room save(Room room);

    /**
     * Delete a room by ID
     *
     * @param id Room ID to delete
     */
    void deleteById(Long id);

    /**
     * Delete a room
     *
     * @param room Room to delete
     */
    void delete(Room room);

    /**
     * Find room by room number
     *
     * @param roomNumber Room number
     * @return Optional containing room if found
     */
    Optional<Room> findByRoomNumber(String roomNumber);

    /**
     * Check if a room exists by ID
     *
     * @param id Room ID
     * @return true if exists, false otherwise
     */
    boolean existsById(Long id);
}