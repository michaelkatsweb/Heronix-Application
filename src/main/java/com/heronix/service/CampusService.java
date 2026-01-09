package com.heronix.service;

import com.heronix.model.domain.Campus;
import com.heronix.repository.CampusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Campus Management Service
 *
 * Manages campus entities including CRUD operations, search, and multi-campus
 * federation support.
 *
 * @author Heronix Development Team
 * @version 2.0
 * @since Phase 12 - Multi-Campus Federation
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CampusService {

    private final CampusRepository campusRepository;

    @Transactional
    public Campus save(Campus campus) {
        log.info("Saving campus: {}", campus.getName());
        return campusRepository.save(campus);
    }

    public List<Campus> findAll() {
        log.debug("Finding all campuses");
        return campusRepository.findAll();
    }

    public List<Campus> getAllCampuses() {
        return findAll();
    }

    public Campus findById(Long id) {
        log.debug("Finding campus by ID: {}", id);
        return campusRepository.findById(id).orElse(null);
    }

    public Optional<Campus> findCampusById(Long id) {
        return campusRepository.findById(id);
    }

    public Optional<Campus> findByCampusCode(String campusCode) {
        return campusRepository.findByCampusCode(campusCode);
    }

    public List<Campus> findActiveCampuses() {
        return campusRepository.findByActiveTrue();
    }

    public List<Campus> findByDistrict(Long districtId) {
        return campusRepository.findByDistrictId(districtId);
    }

    public List<Campus> findByDistrictCode(String districtCode) {
        return campusRepository.findByDistrictCode(districtCode);
    }

    public List<Campus> searchByName(String name) {
        return campusRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public void delete(Campus campus) {
        log.info("Deleting campus: {}", campus.getName());
        campusRepository.delete(campus);
    }

    @Transactional
    public void deleteById(Long id) {
        campusRepository.deleteById(id);
    }
}
