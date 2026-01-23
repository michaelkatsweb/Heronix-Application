package com.heronix.ui.component;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;

/**
 * Filter Chips Component
 * A modern filter UI with chips/pills for selected filters.
 *
 * Features:
 * - Multi-select filter chips
 * - Search within filters
 * - Clear all button
 * - Collapsible sections
 * - Active filter count badge
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class FilterChips extends VBox {

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private final HBox activeFiltersBar;
    private final FlowPane activeFiltersPane;
    private final Label activeCountLabel;
    private final Button clearAllBtn;
    private final VBox filterSections;

    // ========================================================================
    // DATA
    // ========================================================================

    private final ObservableList<FilterSection> sections = FXCollections.observableArrayList();
    private final Map<String, Set<String>> activeFilters = new LinkedHashMap<>();

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<Map<String, Set<String>>> onFilterChange;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public FilterChips() {
        // Active filters bar
        activeFiltersBar = new HBox(10);
        activeFiltersBar.setAlignment(Pos.CENTER_LEFT);
        activeFiltersBar.setPadding(new Insets(8, 12, 8, 12));
        activeFiltersBar.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");
        activeFiltersBar.setVisible(false);
        activeFiltersBar.setManaged(false);

        Label activeLabel = new Label("Active Filters:");
        activeLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #475569;");

        activeFiltersPane = new FlowPane(8, 8);
        activeFiltersPane.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(activeFiltersPane, Priority.ALWAYS);

        activeCountLabel = new Label();
        activeCountLabel.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 11px; -fx-font-weight: 600;");

        clearAllBtn = new Button("Clear All");
        clearAllBtn.getStyleClass().addAll("btn", "btn-ghost", "btn-sm");
        clearAllBtn.setOnAction(e -> clearAllFilters());

        activeFiltersBar.getChildren().addAll(activeLabel, activeCountLabel, activeFiltersPane, clearAllBtn);

        // Filter sections container
        filterSections = new VBox(8);
        filterSections.setPadding(new Insets(12));

        // Listen for section changes
        sections.addListener((ListChangeListener<FilterSection>) change -> {
            rebuildSections();
        });

        // Layout
        setSpacing(0);
        getChildren().addAll(activeFiltersBar, filterSections);
        getStyleClass().add("filter-chips");
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Add a filter section
     */
    public FilterSection addSection(String id, String label) {
        FilterSection section = new FilterSection(id, label);
        sections.add(section);
        return section;
    }

    /**
     * Add a filter section with options
     */
    public FilterSection addSection(String id, String label, List<String> options) {
        FilterSection section = new FilterSection(id, label);
        section.setOptions(options);
        sections.add(section);
        return section;
    }

    /**
     * Get a section by ID
     */
    public Optional<FilterSection> getSection(String id) {
        return sections.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }

    /**
     * Remove a section
     */
    public void removeSection(String id) {
        sections.removeIf(s -> s.getId().equals(id));
        activeFilters.remove(id);
        updateActiveFiltersBar();
        fireFilterChange();
    }

    /**
     * Clear all filters
     */
    public void clearAllFilters() {
        activeFilters.clear();
        sections.forEach(section -> section.clearSelection());
        updateActiveFiltersBar();
        fireFilterChange();
    }

    /**
     * Get active filters
     */
    public Map<String, Set<String>> getActiveFilters() {
        return Collections.unmodifiableMap(activeFilters);
    }

    /**
     * Check if filter is active
     */
    public boolean isFilterActive(String sectionId, String value) {
        Set<String> values = activeFilters.get(sectionId);
        return values != null && values.contains(value);
    }

    /**
     * Get active filter count
     */
    public int getActiveFilterCount() {
        return activeFilters.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    /**
     * Set filter change callback
     */
    public void setOnFilterChange(Consumer<Map<String, Set<String>>> callback) {
        this.onFilterChange = callback;
    }

    /**
     * Set initial filter values
     */
    public void setFilters(Map<String, Set<String>> filters) {
        activeFilters.clear();
        filters.forEach((key, values) -> {
            if (values != null && !values.isEmpty()) {
                activeFilters.put(key, new LinkedHashSet<>(values));
            }
        });

        // Update section selections
        sections.forEach(section -> {
            Set<String> values = activeFilters.get(section.getId());
            section.setSelection(values != null ? values : Collections.emptySet());
        });

        updateActiveFiltersBar();
    }

    // ========================================================================
    // INTERNAL METHODS
    // ========================================================================

    private void rebuildSections() {
        filterSections.getChildren().clear();

        for (FilterSection section : sections) {
            filterSections.getChildren().add(section.buildUI());

            // Setup callback
            section.setOnSelectionChange(selected -> {
                if (selected.isEmpty()) {
                    activeFilters.remove(section.getId());
                } else {
                    activeFilters.put(section.getId(), new LinkedHashSet<>(selected));
                }
                updateActiveFiltersBar();
                fireFilterChange();
            });
        }
    }

    private void updateActiveFiltersBar() {
        activeFiltersPane.getChildren().clear();

        int totalCount = 0;
        for (Map.Entry<String, Set<String>> entry : activeFilters.entrySet()) {
            String sectionId = entry.getKey();
            Set<String> values = entry.getValue();

            // Find section label
            String sectionLabel = sections.stream()
                    .filter(s -> s.getId().equals(sectionId))
                    .map(FilterSection::getLabel)
                    .findFirst()
                    .orElse(sectionId);

            for (String value : values) {
                HBox chip = createFilterChip(sectionId, sectionLabel, value);
                activeFiltersPane.getChildren().add(chip);
                totalCount++;
            }
        }

        activeCountLabel.setText(String.valueOf(totalCount));
        activeFiltersBar.setVisible(totalCount > 0);
        activeFiltersBar.setManaged(totalCount > 0);
    }

    private HBox createFilterChip(String sectionId, String sectionLabel, String value) {
        HBox chip = new HBox(4);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(4, 8, 4, 10));
        chip.setStyle("-fx-background-color: #E0E7FF; -fx-background-radius: 16; -fx-border-radius: 16;");

        Label label = new Label(sectionLabel + ": " + value);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #3730A3;");

        Button removeBtn = new Button("✕");
        removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6366F1; -fx-padding: 0 2; -fx-font-size: 10px; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> {
            Set<String> values = activeFilters.get(sectionId);
            if (values != null) {
                values.remove(value);
                if (values.isEmpty()) {
                    activeFilters.remove(sectionId);
                }
            }

            // Update section UI
            getSection(sectionId).ifPresent(section -> section.deselectValue(value));

            updateActiveFiltersBar();
            fireFilterChange();
        });

        chip.getChildren().addAll(label, removeBtn);
        return chip;
    }

    private void fireFilterChange() {
        if (onFilterChange != null) {
            onFilterChange.accept(Collections.unmodifiableMap(activeFilters));
        }
    }

    // ========================================================================
    // FILTER SECTION CLASS
    // ========================================================================

    /**
     * A filter section containing multiple filter options
     */
    @Getter
    public static class FilterSection {
        private final String id;
        private final StringProperty label = new SimpleStringProperty();
        private final ObservableList<String> options = FXCollections.observableArrayList();
        private final Set<String> selectedValues = new LinkedHashSet<>();

        private boolean multiSelect = true;
        private boolean searchable = false;
        private boolean collapsed = false;

        private Consumer<Set<String>> onSelectionChange;
        private FlowPane optionsPane;
        private TextField searchField;
        private TitledPane titledPane;

        public FilterSection(String id, String label) {
            this.id = id;
            this.label.set(label);
        }

        public String getLabel() {
            return label.get();
        }

        public void setLabel(String label) {
            this.label.set(label);
        }

        public void setOptions(List<String> options) {
            this.options.setAll(options);
        }

        public void addOption(String option) {
            if (!options.contains(option)) {
                options.add(option);
            }
        }

        public void setMultiSelect(boolean multiSelect) {
            this.multiSelect = multiSelect;
        }

        public void setSearchable(boolean searchable) {
            this.searchable = searchable;
        }

        public void setCollapsed(boolean collapsed) {
            this.collapsed = collapsed;
            if (titledPane != null) {
                titledPane.setExpanded(!collapsed);
            }
        }

        public void setSelection(Set<String> values) {
            selectedValues.clear();
            selectedValues.addAll(values);
            updateOptionsUI();
        }

        public void clearSelection() {
            selectedValues.clear();
            updateOptionsUI();
        }

        public void selectValue(String value) {
            if (!multiSelect) {
                selectedValues.clear();
            }
            selectedValues.add(value);
            updateOptionsUI();
            fireSelectionChange();
        }

        public void deselectValue(String value) {
            selectedValues.remove(value);
            updateOptionsUI();
        }

        void setOnSelectionChange(Consumer<Set<String>> callback) {
            this.onSelectionChange = callback;
        }

        private void fireSelectionChange() {
            if (onSelectionChange != null) {
                onSelectionChange.accept(Collections.unmodifiableSet(selectedValues));
            }
        }

        TitledPane buildUI() {
            VBox content = new VBox(8);
            content.setPadding(new Insets(8));

            // Search field (if searchable)
            if (searchable) {
                searchField = new TextField();
                searchField.setPromptText("Search " + label.get() + "...");
                searchField.getStyleClass().add("input");
                searchField.textProperty().addListener((obs, oldVal, newVal) -> filterOptions(newVal));
                content.getChildren().add(searchField);
            }

            // Options
            optionsPane = new FlowPane(6, 6);
            optionsPane.setPrefWrapLength(400);
            rebuildOptions();
            content.getChildren().add(optionsPane);

            // Listen for options changes
            options.addListener((ListChangeListener<String>) change -> rebuildOptions());

            // Titled pane
            titledPane = new TitledPane(label.get(), content);
            titledPane.setExpanded(!collapsed);
            titledPane.setCollapsible(true);
            titledPane.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-radius: 6;");

            // Bind label
            label.addListener((obs, oldVal, newVal) -> titledPane.setText(newVal));

            return titledPane;
        }

        private void rebuildOptions() {
            if (optionsPane == null) return;
            optionsPane.getChildren().clear();

            for (String option : options) {
                ToggleButton chip = createOptionChip(option);
                optionsPane.getChildren().add(chip);
            }
        }

        private ToggleButton createOptionChip(String option) {
            ToggleButton chip = new ToggleButton(option);
            chip.setSelected(selectedValues.contains(option));

            // Styling
            chip.setStyle(chip.isSelected() ? getSelectedStyle() : getUnselectedStyle());

            chip.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                chip.setStyle(isSelected ? getSelectedStyle() : getUnselectedStyle());

                if (isSelected) {
                    if (!multiSelect) {
                        selectedValues.clear();
                        // Deselect others
                        optionsPane.getChildren().forEach(node -> {
                            if (node instanceof ToggleButton && node != chip) {
                                ((ToggleButton) node).setSelected(false);
                            }
                        });
                    }
                    selectedValues.add(option);
                } else {
                    selectedValues.remove(option);
                }

                fireSelectionChange();
            });

            return chip;
        }

        private void filterOptions(String query) {
            if (optionsPane == null) return;

            String lowerQuery = query != null ? query.toLowerCase() : "";

            optionsPane.getChildren().forEach(node -> {
                if (node instanceof ToggleButton) {
                    ToggleButton chip = (ToggleButton) node;
                    boolean matches = lowerQuery.isEmpty() || chip.getText().toLowerCase().contains(lowerQuery);
                    chip.setVisible(matches);
                    chip.setManaged(matches);
                }
            });
        }

        private void updateOptionsUI() {
            if (optionsPane == null) return;

            optionsPane.getChildren().forEach(node -> {
                if (node instanceof ToggleButton) {
                    ToggleButton chip = (ToggleButton) node;
                    boolean selected = selectedValues.contains(chip.getText());
                    chip.setSelected(selected);
                    chip.setStyle(selected ? getSelectedStyle() : getUnselectedStyle());
                }
            });
        }

        private String getSelectedStyle() {
            return "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-background-radius: 16; " +
                    "-fx-padding: 4 12; -fx-font-size: 12px; -fx-cursor: hand;";
        }

        private String getUnselectedStyle() {
            return "-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-background-radius: 16; " +
                    "-fx-padding: 4 12; -fx-font-size: 12px; -fx-cursor: hand; -fx-border-color: #E2E8F0; -fx-border-radius: 16;";
        }
    }
}
