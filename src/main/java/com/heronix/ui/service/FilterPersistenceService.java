package com.heronix.ui.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filter Persistence Service
 * Persists filter states across sessions for a better user experience.
 *
 * Features:
 * - Save filter state per view/screen
 * - Support for different filter types (text, select, multi-select, date range)
 * - Named filter presets
 * - User-specific persistence
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
@Service
public class FilterPersistenceService {

    // ========================================================================
    // CONFIGURATION
    // ========================================================================

    private static final String FILTER_DIR = ".heronix/filters";
    private static final String FILTER_FILE = "filter-state.json";
    private static final String PRESETS_FILE = "filter-presets.json";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Path filterDir;
    private Path filterFilePath;
    private Path presetsFilePath;

    // ========================================================================
    // STATE
    // ========================================================================

    // Current filter state per view: viewId -> Map<filterId, value>
    private final Map<String, Map<String, Object>> filterState = new ConcurrentHashMap<>();

    // Named filter presets per view: viewId -> Map<presetName, Map<filterId, value>>
    private final Map<String, Map<String, Map<String, Object>>> filterPresets = new ConcurrentHashMap<>();

    // Recent filter values for suggestions: viewId.filterId -> List<recentValues>
    private final Map<String, LinkedList<String>> recentValues = new ConcurrentHashMap<>();

    private static final int MAX_RECENT_VALUES = 10;

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @PostConstruct
    public void initialize() {
        try {
            // Create filter directory
            String userHome = System.getProperty("user.home");
            filterDir = Paths.get(userHome, FILTER_DIR);
            Files.createDirectories(filterDir);

            filterFilePath = filterDir.resolve(FILTER_FILE);
            presetsFilePath = filterDir.resolve(PRESETS_FILE);

            // Load existing state
            loadFilterState();
            loadFilterPresets();

            log.info("✓ FilterPersistenceService initialized at: {}", filterDir);
        } catch (IOException e) {
            log.error("Failed to initialize FilterPersistenceService", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        saveFilterState();
        saveFilterPresets();
        log.info("FilterPersistenceService shutdown complete");
    }

    // ========================================================================
    // FILTER STATE API
    // ========================================================================

    /**
     * Save a filter value for a view
     *
     * @param viewId   The view/screen identifier
     * @param filterId The filter identifier within the view
     * @param value    The filter value (can be String, List, Map, etc.)
     */
    public void saveFilter(String viewId, String filterId, Object value) {
        filterState.computeIfAbsent(viewId, k -> new ConcurrentHashMap<>())
                .put(filterId, value);

        // Track recent values for text filters
        if (value instanceof String && !((String) value).isEmpty()) {
            addRecentValue(viewId, filterId, (String) value);
        }

        log.debug("Saved filter: {}.{} = {}", viewId, filterId, value);
    }

    /**
     * Get a filter value for a view
     *
     * @param viewId   The view/screen identifier
     * @param filterId The filter identifier
     * @return The saved value, or null if not set
     */
    @SuppressWarnings("unchecked")
    public <T> T getFilter(String viewId, String filterId) {
        Map<String, Object> viewFilters = filterState.get(viewId);
        if (viewFilters != null) {
            return (T) viewFilters.get(filterId);
        }
        return null;
    }

    /**
     * Get a filter value with default
     */
    @SuppressWarnings("unchecked")
    public <T> T getFilter(String viewId, String filterId, T defaultValue) {
        T value = getFilter(viewId, filterId);
        return value != null ? value : defaultValue;
    }

    /**
     * Get all filters for a view
     */
    public Map<String, Object> getFilters(String viewId) {
        return filterState.getOrDefault(viewId, Collections.emptyMap());
    }

    /**
     * Clear a specific filter
     */
    public void clearFilter(String viewId, String filterId) {
        Map<String, Object> viewFilters = filterState.get(viewId);
        if (viewFilters != null) {
            viewFilters.remove(filterId);
        }
    }

    /**
     * Clear all filters for a view
     */
    public void clearFilters(String viewId) {
        filterState.remove(viewId);
        log.debug("Cleared all filters for view: {}", viewId);
    }

    /**
     * Clear all filters
     */
    public void clearAllFilters() {
        filterState.clear();
        log.debug("Cleared all filters");
    }

    // ========================================================================
    // FILTER PRESETS API
    // ========================================================================

    /**
     * Save current filters as a named preset
     *
     * @param viewId     The view identifier
     * @param presetName The preset name
     */
    public void savePreset(String viewId, String presetName) {
        Map<String, Object> currentFilters = filterState.get(viewId);
        if (currentFilters != null && !currentFilters.isEmpty()) {
            filterPresets
                    .computeIfAbsent(viewId, k -> new ConcurrentHashMap<>())
                    .put(presetName, new HashMap<>(currentFilters));
            log.debug("Saved preset: {}.{}", viewId, presetName);
        }
    }

    /**
     * Load a preset (applies it to current filters)
     *
     * @param viewId     The view identifier
     * @param presetName The preset name
     * @return true if preset was found and loaded
     */
    public boolean loadPreset(String viewId, String presetName) {
        Map<String, Map<String, Object>> viewPresets = filterPresets.get(viewId);
        if (viewPresets != null) {
            Map<String, Object> preset = viewPresets.get(presetName);
            if (preset != null) {
                filterState.put(viewId, new ConcurrentHashMap<>(preset));
                log.debug("Loaded preset: {}.{}", viewId, presetName);
                return true;
            }
        }
        return false;
    }

    /**
     * Delete a preset
     */
    public void deletePreset(String viewId, String presetName) {
        Map<String, Map<String, Object>> viewPresets = filterPresets.get(viewId);
        if (viewPresets != null) {
            viewPresets.remove(presetName);
            log.debug("Deleted preset: {}.{}", viewId, presetName);
        }
    }

    /**
     * Get all preset names for a view
     */
    public List<String> getPresetNames(String viewId) {
        Map<String, Map<String, Object>> viewPresets = filterPresets.get(viewId);
        if (viewPresets != null) {
            return new ArrayList<>(viewPresets.keySet());
        }
        return Collections.emptyList();
    }

    /**
     * Check if a preset exists
     */
    public boolean hasPreset(String viewId, String presetName) {
        Map<String, Map<String, Object>> viewPresets = filterPresets.get(viewId);
        return viewPresets != null && viewPresets.containsKey(presetName);
    }

    // ========================================================================
    // RECENT VALUES API
    // ========================================================================

    /**
     * Get recent values for a filter (for autocomplete suggestions)
     */
    public List<String> getRecentValues(String viewId, String filterId) {
        String key = viewId + "." + filterId;
        LinkedList<String> recent = recentValues.get(key);
        return recent != null ? new ArrayList<>(recent) : Collections.emptyList();
    }

    /**
     * Add a recent value
     */
    private void addRecentValue(String viewId, String filterId, String value) {
        String key = viewId + "." + filterId;
        LinkedList<String> recent = recentValues.computeIfAbsent(key, k -> new LinkedList<>());

        // Remove if already exists (to move to front)
        recent.remove(value);

        // Add to front
        recent.addFirst(value);

        // Trim to max size
        while (recent.size() > MAX_RECENT_VALUES) {
            recent.removeLast();
        }
    }

    /**
     * Clear recent values for a filter
     */
    public void clearRecentValues(String viewId, String filterId) {
        String key = viewId + "." + filterId;
        recentValues.remove(key);
    }

    // ========================================================================
    // PERSISTENCE
    // ========================================================================

    /**
     * Save filter state to file
     */
    public void saveFilterState() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("filters", filterState);
            data.put("recent", recentValues);

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(filterFilePath.toFile(), data);

            log.debug("Saved filter state to: {}", filterFilePath);
        } catch (IOException e) {
            log.error("Failed to save filter state", e);
        }
    }

    /**
     * Load filter state from file
     */
    @SuppressWarnings("unchecked")
    private void loadFilterState() {
        File file = filterFilePath.toFile();
        if (file.exists()) {
            try {
                Map<String, Object> data = objectMapper.readValue(file,
                        new TypeReference<Map<String, Object>>() {});

                // Load filters
                Object filters = data.get("filters");
                if (filters instanceof Map) {
                    filterState.clear();
                    ((Map<String, Map<String, Object>>) filters).forEach((viewId, viewFilters) -> {
                        filterState.put(viewId, new ConcurrentHashMap<>(viewFilters));
                    });
                }

                // Load recent values
                Object recent = data.get("recent");
                if (recent instanceof Map) {
                    recentValues.clear();
                    ((Map<String, List<String>>) recent).forEach((key, values) -> {
                        recentValues.put(key, new LinkedList<>(values));
                    });
                }

                log.debug("Loaded filter state from: {}", filterFilePath);
            } catch (IOException e) {
                log.warn("Failed to load filter state: {}", e.getMessage());
            }
        }
    }

    /**
     * Save filter presets to file
     */
    public void saveFilterPresets() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(presetsFilePath.toFile(), filterPresets);

            log.debug("Saved filter presets to: {}", presetsFilePath);
        } catch (IOException e) {
            log.error("Failed to save filter presets", e);
        }
    }

    /**
     * Load filter presets from file
     */
    @SuppressWarnings("unchecked")
    private void loadFilterPresets() {
        File file = presetsFilePath.toFile();
        if (file.exists()) {
            try {
                Map<String, Map<String, Map<String, Object>>> data = objectMapper.readValue(file,
                        new TypeReference<Map<String, Map<String, Map<String, Object>>>>() {});

                filterPresets.clear();
                data.forEach((viewId, presets) -> {
                    filterPresets.put(viewId, new ConcurrentHashMap<>(presets));
                });

                log.debug("Loaded filter presets from: {}", presetsFilePath);
            } catch (IOException e) {
                log.warn("Failed to load filter presets: {}", e.getMessage());
            }
        }
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Check if any filters are set for a view
     */
    public boolean hasFilters(String viewId) {
        Map<String, Object> viewFilters = filterState.get(viewId);
        return viewFilters != null && !viewFilters.isEmpty();
    }

    /**
     * Get the count of active filters for a view
     */
    public int getFilterCount(String viewId) {
        Map<String, Object> viewFilters = filterState.get(viewId);
        if (viewFilters == null) return 0;

        // Count non-null, non-empty values
        return (int) viewFilters.values().stream()
                .filter(v -> v != null)
                .filter(v -> {
                    if (v instanceof String) return !((String) v).isEmpty();
                    if (v instanceof Collection) return !((Collection<?>) v).isEmpty();
                    return true;
                })
                .count();
    }

    /**
     * Export all filter data (for backup/debugging)
     */
    public Map<String, Object> exportAll() {
        Map<String, Object> export = new HashMap<>();
        export.put("filters", new HashMap<>(filterState));
        export.put("presets", new HashMap<>(filterPresets));
        export.put("recent", new HashMap<>(recentValues));
        return export;
    }
}
