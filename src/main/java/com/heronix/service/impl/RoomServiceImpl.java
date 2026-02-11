// Location: src/main/java/com/heronix/service/impl/RoomServiceImpl.java
package com.heronix.service.impl;

import com.heronix.model.domain.Room;
import com.heronix.repository.RoomRepository;
import com.heronix.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Room Service Implementation
 * Location: src/main/java/com/heronix/service/impl/RoomServiceImpl.java
 *
 * Provides transactional CRUD operations for Room entities.
 * All write operations are wrapped in transactions to ensure data consistency.
 *
 * @author Heronix Scheduling System Team
 * @version 1.2.0
 * @since 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    /**
     * Get all active rooms
     *
     * @return List of active rooms
     */
    @Override
    @Transactional(readOnly = true)
    public List<Room> getAllActiveRooms() {
        log.debug("Fetching all active rooms");
        List<Room> rooms = roomRepository.findByActiveTrue();
        log.info("Found {} active rooms", rooms.size());
        return rooms;
    }

    /**
     * Get room by ID
     *
     * @param id Room ID
     * @return Room object or null if not found
     */
    @Override
    @Transactional(readOnly = true)
    public Room getRoomById(Long id) {
        log.debug("Fetching room with ID: {}", id);
        Room room = roomRepository.findById(id).orElse(null);
        if (room != null) {
            log.debug("Found room: {}", room.getRoomNumber() != null ? room.getRoomNumber() : "Unknown");
        } else {
            log.warn("Room not found with ID: {}", id);
        }
        return room;
    }

    /**
     * Get all rooms (including inactive)
     *
     * @return List of all rooms
     */
    @Override
    @Transactional(readOnly = true)
    public List<Room> findAll() {
        log.debug("Fetching all rooms (including inactive)");
        List<Room> rooms = roomRepository.findAllWithTeacher();
        log.info("Found {} total rooms", rooms.size());
        return rooms;
    }

    /**
     * Save a new room or update an existing room
     * This method is transactional to ensure the save is committed to the database.
     *
     * @param room Room to save
     * @return Saved room with generated ID
     */
    @Override
    @Transactional
    public Room save(Room room) {
        log.debug("Saving room: {}", room.getRoomNumber());
        Room savedRoom = roomRepository.save(room);
        log.info("Room saved successfully with ID: {}", savedRoom.getId());
        return savedRoom;
    }

    /**
     * Delete a room by ID
     *
     * @param id Room ID to delete
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting room with ID: {}", id);
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
            log.info("Room deleted successfully with ID: {}", id);
        } else {
            log.warn("Cannot delete - room not found with ID: {}", id);
        }
    }

    /**
     * Delete a room
     *
     * @param room Room to delete
     */
    @Override
    @Transactional
    public void delete(Room room) {
        log.debug("Deleting room: {}", room.getRoomNumber());
        roomRepository.delete(room);
        log.info("Room deleted successfully: {}", room.getRoomNumber());
    }

    /**
     * Find room by room number
     *
     * @param roomNumber Room number
     * @return Optional containing room if found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Room> findByRoomNumber(String roomNumber) {
        log.debug("Finding room by number: {}", roomNumber);
        return roomRepository.findByRoomNumber(roomNumber);
    }

    /**
     * Check if a room exists by ID
     *
     * @param id Room ID
     * @return true if exists, false otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return roomRepository.existsById(id);
    }
}