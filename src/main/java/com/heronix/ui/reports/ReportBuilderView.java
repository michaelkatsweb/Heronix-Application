package com.heronix.ui.reports;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/**
 * Report Builder View
 * Drag-and-drop interface for creating custom reports.
 *
 * Features:
 * - Drag fields from data sources
 * - Configure columns, filters, and sorting
 * - Live preview of report data
 * - Save as templates
 * - Export to multiple formats
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class ReportBuilderView extends BorderPane {

    // ========================================================================
    // DATA
    // ========================================================================

    private final ObservableList<DataSource> dataSources = FXCollections.observableArrayList();
    private final ObservableList<ReportField> selectedFields = FXCollections.observableArrayList();
    private final ObservableList<ReportFilter> filters = FXCollections.observableArrayList();
    private final ObservableList<SortOption> sortOptions = FXCollections.observableArrayList();

    private final StringProperty reportName = new SimpleStringProperty("Untitled Report");
    private final ObjectProperty<DataSource> activeDataSource = new SimpleObjectProperty<>();

    private static final DataFormat FIELD_FORMAT = new DataFormat("application/x-report-field");

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private TreeView<FieldTreeItem> fieldTree;
    private ListView<ReportField> selectedFieldsList;
    private VBox filterPane;
    private VBox sortPane;
    private TableView<Map<String, Object>> previewTable;
    private Label recordCountLabel;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<ReportDefinition> onSaveTemplate;
    private Consumer<ReportDefinition> onRunReport;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public ReportBuilderView() {
        getStyleClass().add("report-builder");
        setStyle("-fx-background-color: #F8FAFC;");

        // Header
        setTop(createHeader());

        // Main content
        SplitPane mainSplit = new SplitPane();
        mainSplit.setDividerPositions(0.25, 0.55);

        // Left: Field palette
        VBox fieldPalette = createFieldPalette();

        // Center: Report configuration
        VBox configPane = createConfigPane();

        // Right: Preview
        VBox previewPane = createPreviewPane();

        mainSplit.getItems().addAll(fieldPalette, configPane, previewPane);

        setCenter(mainSplit);

        // Initialize data sources
        initializeDataSources();
    }

    // ========================================================================
    // HEADER
    // ========================================================================

    private HBox createHeader() {
        HBox header = new HBox(16);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        // Back button
        Button backBtn = new Button("← Back");
        backBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #64748B;
            -fx-font-size: 13px;
            -fx-cursor: hand;
            """);

        // Report name (editable)
        TextField nameField = new TextField();
        nameField.textProperty().bindBidirectional(reportName);
        nameField.setStyle("""
            -fx-background-color: transparent;
            -fx-font-size: 18px;
            -fx-font-weight: 700;
            -fx-text-fill: #1E293B;
            -fx-padding: 4 8;
            -fx-border-color: transparent;
            -fx-border-width: 0 0 2 0;
            """);
        nameField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            nameField.setStyle(String.format("""
                -fx-background-color: transparent;
                -fx-font-size: 18px;
                -fx-font-weight: 700;
                -fx-text-fill: #1E293B;
                -fx-padding: 4 8;
                -fx-border-color: %s;
                -fx-border-width: 0 0 2 0;
                """, isFocused ? "#3B82F6" : "transparent"));
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Action buttons
        Button saveTemplateBtn = new Button("💾 Save Template");
        saveTemplateBtn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-text-fill: #374151;
            -fx-font-size: 13px;
            -fx-font-weight: 500;
            -fx-padding: 10 16;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);
        saveTemplateBtn.setOnAction(e -> saveTemplate());

        Button runReportBtn = new Button("▶ Run Report");
        runReportBtn.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-padding: 10 20;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);
        runReportBtn.setOnAction(e -> runReport());

        MenuButton exportBtn = new MenuButton("📥 Export");
        exportBtn.setStyle("""
            -fx-background-color: #10B981;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-padding: 10 16;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);
        exportBtn.getItems().addAll(
            new MenuItem("Export as PDF"),
            new MenuItem("Export as Excel"),
            new MenuItem("Export as CSV"),
            new MenuItem("Export as JSON")
        );

        header.getChildren().addAll(backBtn, nameField, spacer, saveTemplateBtn, runReportBtn, exportBtn);
        return header;
    }

    // ========================================================================
    // FIELD PALETTE (LEFT)
    // ========================================================================

    private VBox createFieldPalette() {
        VBox palette = new VBox(0);
        palette.setStyle("-fx-background-color: white;");
        palette.setMinWidth(250);

        // Header
        HBox header = new HBox(12);
        header.setPadding(new Insets(16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        Label title = new Label("Data Fields");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        header.getChildren().add(title);

        // Data source selector
        VBox sourceBox = new VBox(8);
        sourceBox.setPadding(new Insets(12, 16, 12, 16));
        sourceBox.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        Label sourceLabel = new Label("Data Source");
        sourceLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: #64748B;");

        ComboBox<DataSource> sourceCombo = new ComboBox<>(dataSources);
        sourceCombo.setMaxWidth(Double.MAX_VALUE);
        sourceCombo.setStyle("-fx-font-size: 13px;");
        sourceCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DataSource item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getIcon() + " " + item.getName());
            }
        });
        sourceCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(DataSource item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getIcon() + " " + item.getName());
            }
        });
        sourceCombo.valueProperty().bindBidirectional(activeDataSource);
        sourceCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateFieldTree());

        sourceBox.getChildren().addAll(sourceLabel, sourceCombo);

        // Search
        TextField searchField = new TextField();
        searchField.setPromptText("Search fields...");
        searchField.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-background-radius: 6;
            -fx-padding: 8 12;
            -fx-font-size: 12px;
            """);
        VBox.setMargin(searchField, new Insets(12, 16, 8, 16));

        // Field tree
        fieldTree = new TreeView<>();
        fieldTree.setShowRoot(false);
        fieldTree.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(fieldTree, Priority.ALWAYS);

        fieldTree.setCellFactory(tv -> new FieldTreeCell());

        palette.getChildren().addAll(header, sourceBox, searchField, fieldTree);
        return palette;
    }

    private void updateFieldTree() {
        DataSource source = activeDataSource.get();
        if (source == null) {
            fieldTree.setRoot(null);
            return;
        }

        TreeItem<FieldTreeItem> root = new TreeItem<>(new FieldTreeItem("Root", null, true));

        // Group fields by category
        Map<String, List<DataField>> grouped = new LinkedHashMap<>();
        for (DataField field : source.getFields()) {
            grouped.computeIfAbsent(field.getCategory(), k -> new ArrayList<>()).add(field);
        }

        for (Map.Entry<String, List<DataField>> entry : grouped.entrySet()) {
            TreeItem<FieldTreeItem> categoryItem = new TreeItem<>(
                new FieldTreeItem(entry.getKey(), null, true)
            );
            categoryItem.setExpanded(true);

            for (DataField field : entry.getValue()) {
                TreeItem<FieldTreeItem> fieldItem = new TreeItem<>(
                    new FieldTreeItem(field.getDisplayName(), field, false)
                );
                categoryItem.getChildren().add(fieldItem);
            }

            root.getChildren().add(categoryItem);
        }

        fieldTree.setRoot(root);
    }

    // ========================================================================
    // CONFIG PANE (CENTER)
    // ========================================================================

    private VBox createConfigPane() {
        VBox config = new VBox(0);
        config.setStyle("-fx-background-color: #F8FAFC;");

        // Tabs for different config sections
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getStyleClass().add("config-tabs");
        VBox.setVgrow(tabs, Priority.ALWAYS);

        Tab columnsTab = new Tab("Columns", createColumnsPane());
        Tab filtersTab = new Tab("Filters", createFiltersPane());
        Tab sortingTab = new Tab("Sorting", createSortingPane());
        Tab groupingTab = new Tab("Grouping", createGroupingPane());

        tabs.getTabs().addAll(columnsTab, filtersTab, sortingTab, groupingTab);

        config.getChildren().add(tabs);
        return config;
    }

    private VBox createColumnsPane() {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(16));

        // Instructions
        Label instructions = new Label("Drag fields from the left panel or click + to add columns");
        instructions.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        // Selected fields list
        selectedFieldsList = new ListView<>(selectedFields);
        selectedFieldsList.setCellFactory(lv -> new SelectedFieldCell());
        selectedFieldsList.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        VBox.setVgrow(selectedFieldsList, Priority.ALWAYS);

        // Enable drop
        selectedFieldsList.setOnDragOver(e -> {
            if (e.getGestureSource() != selectedFieldsList && e.getDragboard().hasContent(FIELD_FORMAT)) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            e.consume();
        });

        selectedFieldsList.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasContent(FIELD_FORMAT)) {
                String fieldId = (String) db.getContent(FIELD_FORMAT);
                addFieldById(fieldId);
                e.setDropCompleted(true);
            } else {
                e.setDropCompleted(false);
            }
            e.consume();
        });

        // Drop zone placeholder when empty
        if (selectedFields.isEmpty()) {
            VBox dropZone = createDropZone();
            pane.getChildren().addAll(instructions, dropZone);
        } else {
            pane.getChildren().addAll(instructions, selectedFieldsList);
        }

        selectedFields.addListener((ListChangeListener<ReportField>) c -> {
            pane.getChildren().clear();
            if (selectedFields.isEmpty()) {
                pane.getChildren().addAll(instructions, createDropZone());
            } else {
                pane.getChildren().addAll(instructions, selectedFieldsList);
            }
            updatePreview();
        });

        return pane;
    }

    private VBox createDropZone() {
        VBox zone = new VBox(12);
        zone.setAlignment(Pos.CENTER);
        zone.setPadding(new Insets(40));
        zone.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-border-color: #CBD5E1;
            -fx-border-radius: 12;
            -fx-border-style: dashed;
            -fx-border-width: 2;
            """);
        VBox.setVgrow(zone, Priority.ALWAYS);

        Label icon = new Label("📋");
        icon.setStyle("-fx-font-size: 32px;");

        Label text = new Label("Drag fields here to add columns");
        text.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        Label hint = new Label("Or double-click a field in the list");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");

        zone.getChildren().addAll(icon, text, hint);

        // Enable drop on zone
        zone.setOnDragOver(e -> {
            if (e.getDragboard().hasContent(FIELD_FORMAT)) {
                e.acceptTransferModes(TransferMode.COPY);
                zone.setStyle("""
                    -fx-background-color: #EFF6FF;
                    -fx-background-radius: 12;
                    -fx-border-color: #3B82F6;
                    -fx-border-radius: 12;
                    -fx-border-style: dashed;
                    -fx-border-width: 2;
                    """);
            }
            e.consume();
        });

        zone.setOnDragExited(e -> {
            zone.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 12;
                -fx-border-color: #CBD5E1;
                -fx-border-radius: 12;
                -fx-border-style: dashed;
                -fx-border-width: 2;
                """);
        });

        zone.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasContent(FIELD_FORMAT)) {
                String fieldId = (String) db.getContent(FIELD_FORMAT);
                addFieldById(fieldId);
                e.setDropCompleted(true);
            }
            e.consume();
        });

        return zone;
    }

    private VBox createFiltersPane() {
        filterPane = new VBox(12);
        filterPane.setPadding(new Insets(16));

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Filter Conditions");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1E293B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addFilterBtn = new Button("+ Add Filter");
        addFilterBtn.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 12px;
            -fx-font-weight: 600;
            -fx-padding: 8 16;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """);
        addFilterBtn.setOnAction(e -> addFilter());

        header.getChildren().addAll(title, spacer, addFilterBtn);

        // Filter list
        VBox filterList = new VBox(8);
        filterList.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 16;");
        VBox.setVgrow(filterList, Priority.ALWAYS);

        // Empty state
        if (filters.isEmpty()) {
            Label empty = new Label("No filters applied. Click '+ Add Filter' to narrow your results.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8;");
            filterList.getChildren().add(empty);
        }

        filterPane.getChildren().addAll(header, filterList);
        return filterPane;
    }

    private void addFilter() {
        ReportFilter filter = new ReportFilter();
        filter.setId(UUID.randomUUID().toString());
        filters.add(filter);
        rebuildFilterPane();
    }

    private void rebuildFilterPane() {
        VBox filterList = (VBox) filterPane.getChildren().get(1);
        filterList.getChildren().clear();

        if (filters.isEmpty()) {
            Label empty = new Label("No filters applied. Click '+ Add Filter' to narrow your results.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8;");
            filterList.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < filters.size(); i++) {
            ReportFilter filter = filters.get(i);
            HBox filterRow = createFilterRow(filter, i);
            filterList.getChildren().add(filterRow);
        }
    }

    private HBox createFilterRow(ReportFilter filter, int index) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 6;");

        // AND/OR connector (not for first row)
        if (index > 0) {
            ComboBox<String> connector = new ComboBox<>();
            connector.getItems().addAll("AND", "OR");
            connector.setValue(filter.getConnector() != null ? filter.getConnector() : "AND");
            connector.setPrefWidth(70);
            connector.setStyle("-fx-font-size: 12px;");
            connector.valueProperty().addListener((obs, oldVal, newVal) -> filter.setConnector(newVal));
            row.getChildren().add(connector);
        }

        // Field selector
        ComboBox<ReportField> fieldCombo = new ComboBox<>(selectedFields);
        fieldCombo.setPromptText("Select field");
        fieldCombo.setPrefWidth(150);
        fieldCombo.setStyle("-fx-font-size: 12px;");
        fieldCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ReportField item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });
        fieldCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ReportField item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });

        // Operator selector
        ComboBox<String> operatorCombo = new ComboBox<>();
        operatorCombo.getItems().addAll("equals", "not equals", "contains", "starts with", "ends with",
            "greater than", "less than", "is empty", "is not empty");
        operatorCombo.setValue("equals");
        operatorCombo.setPrefWidth(120);
        operatorCombo.setStyle("-fx-font-size: 12px;");
        operatorCombo.valueProperty().addListener((obs, oldVal, newVal) -> filter.setOperator(newVal));

        // Value field
        TextField valueField = new TextField();
        valueField.setPromptText("Value");
        valueField.setPrefWidth(150);
        valueField.setStyle("-fx-font-size: 12px; -fx-background-radius: 4;");
        valueField.textProperty().addListener((obs, oldVal, newVal) -> filter.setValue(newVal));

        // Remove button
        Button removeBtn = new Button("✕");
        removeBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #94A3B8;
            -fx-font-size: 12px;
            -fx-cursor: hand;
            """);
        removeBtn.setOnAction(e -> {
            filters.remove(filter);
            rebuildFilterPane();
        });

        row.getChildren().addAll(fieldCombo, operatorCombo, valueField, removeBtn);
        return row;
    }

    private VBox createSortingPane() {
        sortPane = new VBox(12);
        sortPane.setPadding(new Insets(16));

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Sort Order");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1E293B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addSortBtn = new Button("+ Add Sort");
        addSortBtn.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 12px;
            -fx-font-weight: 600;
            -fx-padding: 8 16;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """);
        addSortBtn.setOnAction(e -> addSort());

        header.getChildren().addAll(title, spacer, addSortBtn);

        // Sort list
        VBox sortList = new VBox(8);
        sortList.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 16;");
        VBox.setVgrow(sortList, Priority.ALWAYS);

        Label empty = new Label("No sorting applied. Data will appear in default order.");
        empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8;");
        sortList.getChildren().add(empty);

        sortPane.getChildren().addAll(header, sortList);
        return sortPane;
    }

    private void addSort() {
        SortOption sort = new SortOption();
        sort.setId(UUID.randomUUID().toString());
        sort.setDirection("ASC");
        sortOptions.add(sort);
        rebuildSortPane();
    }

    private void rebuildSortPane() {
        VBox sortList = (VBox) sortPane.getChildren().get(1);
        sortList.getChildren().clear();

        if (sortOptions.isEmpty()) {
            Label empty = new Label("No sorting applied. Data will appear in default order.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8;");
            sortList.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < sortOptions.size(); i++) {
            SortOption sort = sortOptions.get(i);
            HBox sortRow = createSortRow(sort, i);
            sortList.getChildren().add(sortRow);
        }
    }

    private HBox createSortRow(SortOption sort, int index) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 6;");

        // Priority label
        Label priority = new Label((index + 1) + ".");
        priority.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #64748B;");
        priority.setMinWidth(20);

        // Field selector
        ComboBox<ReportField> fieldCombo = new ComboBox<>(selectedFields);
        fieldCombo.setPromptText("Select field");
        fieldCombo.setPrefWidth(180);
        fieldCombo.setStyle("-fx-font-size: 12px;");
        fieldCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ReportField item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });
        fieldCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ReportField item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });

        // Direction toggle
        ToggleGroup directionGroup = new ToggleGroup();

        ToggleButton ascBtn = new ToggleButton("↑ A-Z");
        ascBtn.setToggleGroup(directionGroup);
        ascBtn.setSelected(sort.getDirection().equals("ASC"));
        ascBtn.setStyle("""
            -fx-background-color: #E2E8F0;
            -fx-text-fill: #374151;
            -fx-font-size: 11px;
            -fx-padding: 6 12;
            -fx-background-radius: 4 0 0 4;
            """);

        ToggleButton descBtn = new ToggleButton("↓ Z-A");
        descBtn.setToggleGroup(directionGroup);
        descBtn.setSelected(sort.getDirection().equals("DESC"));
        descBtn.setStyle("""
            -fx-background-color: #E2E8F0;
            -fx-text-fill: #374151;
            -fx-font-size: 11px;
            -fx-padding: 6 12;
            -fx-background-radius: 0 4 4 0;
            """);

        directionGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == ascBtn) {
                sort.setDirection("ASC");
                ascBtn.setStyle("""
                    -fx-background-color: #3B82F6;
                    -fx-text-fill: white;
                    -fx-font-size: 11px;
                    -fx-padding: 6 12;
                    -fx-background-radius: 4 0 0 4;
                    """);
                descBtn.setStyle("""
                    -fx-background-color: #E2E8F0;
                    -fx-text-fill: #374151;
                    -fx-font-size: 11px;
                    -fx-padding: 6 12;
                    -fx-background-radius: 0 4 4 0;
                    """);
            } else {
                sort.setDirection("DESC");
                descBtn.setStyle("""
                    -fx-background-color: #3B82F6;
                    -fx-text-fill: white;
                    -fx-font-size: 11px;
                    -fx-padding: 6 12;
                    -fx-background-radius: 0 4 4 0;
                    """);
                ascBtn.setStyle("""
                    -fx-background-color: #E2E8F0;
                    -fx-text-fill: #374151;
                    -fx-font-size: 11px;
                    -fx-padding: 6 12;
                    -fx-background-radius: 4 0 0 4;
                    """);
            }
        });

        HBox directionBox = new HBox(0);
        directionBox.getChildren().addAll(ascBtn, descBtn);

        // Remove button
        Button removeBtn = new Button("✕");
        removeBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #94A3B8;
            -fx-font-size: 12px;
            -fx-cursor: hand;
            """);
        removeBtn.setOnAction(e -> {
            sortOptions.remove(sort);
            rebuildSortPane();
        });

        row.getChildren().addAll(priority, fieldCombo, directionBox, removeBtn);
        return row;
    }

    private VBox createGroupingPane() {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(16));

        Label title = new Label("Group By");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1E293B;");

        // Group field selector
        VBox groupBox = new VBox(8);
        groupBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 16;");

        Label hint = new Label("Select a field to group records by:");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        ComboBox<ReportField> groupCombo = new ComboBox<>(selectedFields);
        groupCombo.setPromptText("No grouping");
        groupCombo.setMaxWidth(Double.MAX_VALUE);
        groupCombo.setStyle("-fx-font-size: 13px;");
        groupCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ReportField item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });
        groupCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ReportField item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "No grouping" : item.getDisplayName());
            }
        });

        // Options
        CheckBox showSubtotals = new CheckBox("Show subtotals for each group");
        showSubtotals.setStyle("-fx-font-size: 12px;");

        CheckBox pageBreaks = new CheckBox("Page break after each group");
        pageBreaks.setStyle("-fx-font-size: 12px;");

        groupBox.getChildren().addAll(hint, groupCombo, showSubtotals, pageBreaks);

        pane.getChildren().addAll(title, groupBox);
        return pane;
    }

    // ========================================================================
    // PREVIEW PANE (RIGHT)
    // ========================================================================

    private VBox createPreviewPane() {
        VBox preview = new VBox(0);
        preview.setStyle("-fx-background-color: white;");
        preview.setMinWidth(400);

        // Header
        HBox header = new HBox(12);
        header.setPadding(new Insets(16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        Label title = new Label("Preview");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        recordCountLabel = new Label("0 records");
        recordCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Button refreshBtn = new Button("⟳");
        refreshBtn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-font-size: 14px;
            -fx-padding: 6 10;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """);
        refreshBtn.setTooltip(new Tooltip("Refresh preview"));
        refreshBtn.setOnAction(e -> updatePreview());

        header.getChildren().addAll(title, spacer, recordCountLabel, refreshBtn);

        // Preview table
        previewTable = new TableView<>();
        previewTable.setPlaceholder(new Label("Add fields to see preview"));
        previewTable.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(previewTable, Priority.ALWAYS);

        preview.getChildren().addAll(header, previewTable);
        return preview;
    }

    private void updatePreview() {
        previewTable.getColumns().clear();
        previewTable.getItems().clear();

        if (selectedFields.isEmpty()) {
            recordCountLabel.setText("0 records");
            return;
        }

        // Create columns
        for (ReportField field : selectedFields) {
            TableColumn<Map<String, Object>, String> col = new TableColumn<>(field.getDisplayName());
            col.setCellValueFactory(data -> {
                Object value = data.getValue().get(field.getFieldId());
                return new SimpleStringProperty(value != null ? value.toString() : "");
            });
            col.setPrefWidth(field.getWidth());
            previewTable.getColumns().add(col);
        }

        // Generate demo data
        List<Map<String, Object>> demoData = generateDemoData();
        previewTable.getItems().addAll(demoData);
        recordCountLabel.setText(demoData.size() + " records");
    }

    private List<Map<String, Object>> generateDemoData() {
        List<Map<String, Object>> data = new ArrayList<>();
        String[] firstNames = {"Emma", "Liam", "Olivia", "Noah", "Ava", "James", "Sophia", "William"};
        String[] lastNames = {"Johnson", "Smith", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"};
        String[] grades = {"9", "10", "11", "12"};

        Random rand = new Random();
        for (int i = 0; i < 20; i++) {
            Map<String, Object> row = new HashMap<>();
            String firstName = firstNames[rand.nextInt(firstNames.length)];
            String lastName = lastNames[rand.nextInt(lastNames.length)];

            row.put("student_id", "STU" + String.format("%05d", 10000 + i));
            row.put("first_name", firstName);
            row.put("last_name", lastName);
            row.put("full_name", firstName + " " + lastName);
            row.put("grade_level", grades[rand.nextInt(grades.length)]);
            row.put("gpa", String.format("%.2f", 2.0 + rand.nextDouble() * 2.0));
            row.put("email", firstName.toLowerCase() + "." + lastName.toLowerCase() + "@school.edu");
            row.put("enrollment_date", "2024-08-" + String.format("%02d", 1 + rand.nextInt(28)));
            row.put("status", rand.nextBoolean() ? "Active" : "Inactive");
            row.put("credits_earned", String.valueOf(10 + rand.nextInt(100)));
            row.put("attendance_rate", String.format("%.1f%%", 85.0 + rand.nextDouble() * 15.0));

            data.add(row);
        }

        return data;
    }

    // ========================================================================
    // FIELD OPERATIONS
    // ========================================================================

    private void addFieldById(String fieldId) {
        DataSource source = activeDataSource.get();
        if (source == null) return;

        for (DataField field : source.getFields()) {
            if (field.getId().equals(fieldId)) {
                // Check if already added
                boolean exists = selectedFields.stream()
                    .anyMatch(f -> f.getFieldId().equals(fieldId));

                if (!exists) {
                    ReportField reportField = new ReportField();
                    reportField.setFieldId(field.getId());
                    reportField.setDisplayName(field.getDisplayName());
                    reportField.setDataType(field.getDataType());
                    reportField.setWidth(120);
                    selectedFields.add(reportField);
                }
                break;
            }
        }
    }

    // ========================================================================
    // ACTIONS
    // ========================================================================

    private void saveTemplate() {
        ReportDefinition def = buildReportDefinition();
        if (onSaveTemplate != null) {
            onSaveTemplate.accept(def);
        }
        log.info("Saving report template: {}", def.getName());
    }

    private void runReport() {
        ReportDefinition def = buildReportDefinition();
        if (onRunReport != null) {
            onRunReport.accept(def);
        }
        log.info("Running report: {}", def.getName());
    }

    private ReportDefinition buildReportDefinition() {
        ReportDefinition def = new ReportDefinition();
        def.setId(UUID.randomUUID().toString());
        def.setName(reportName.get());
        def.setDataSource(activeDataSource.get());
        def.setFields(new ArrayList<>(selectedFields));
        def.setFilters(new ArrayList<>(filters));
        def.setSortOptions(new ArrayList<>(sortOptions));
        def.setCreatedAt(LocalDateTime.now());
        return def;
    }

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    public void setOnSaveTemplate(Consumer<ReportDefinition> callback) {
        this.onSaveTemplate = callback;
    }

    public void setOnRunReport(Consumer<ReportDefinition> callback) {
        this.onRunReport = callback;
    }

    // ========================================================================
    // DATA SOURCES INITIALIZATION
    // ========================================================================

    private void initializeDataSources() {
        // Students data source
        DataSource students = new DataSource("students", "Students", "👤");
        students.addField(new DataField("student_id", "Student ID", "Basic Info", "string"));
        students.addField(new DataField("first_name", "First Name", "Basic Info", "string"));
        students.addField(new DataField("last_name", "Last Name", "Basic Info", "string"));
        students.addField(new DataField("full_name", "Full Name", "Basic Info", "string"));
        students.addField(new DataField("grade_level", "Grade Level", "Academic", "string"));
        students.addField(new DataField("gpa", "GPA", "Academic", "number"));
        students.addField(new DataField("email", "Email", "Contact", "string"));
        students.addField(new DataField("enrollment_date", "Enrollment Date", "Basic Info", "date"));
        students.addField(new DataField("status", "Status", "Basic Info", "string"));
        students.addField(new DataField("credits_earned", "Credits Earned", "Academic", "number"));
        students.addField(new DataField("attendance_rate", "Attendance Rate", "Academic", "string"));
        dataSources.add(students);

        // Grades data source
        DataSource grades = new DataSource("grades", "Grades", "📊");
        grades.addField(new DataField("student_name", "Student Name", "Student", "string"));
        grades.addField(new DataField("course_name", "Course", "Course Info", "string"));
        grades.addField(new DataField("assignment_name", "Assignment", "Assignment", "string"));
        grades.addField(new DataField("score", "Score", "Grade", "number"));
        grades.addField(new DataField("max_score", "Max Score", "Grade", "number"));
        grades.addField(new DataField("percentage", "Percentage", "Grade", "number"));
        grades.addField(new DataField("letter_grade", "Letter Grade", "Grade", "string"));
        grades.addField(new DataField("due_date", "Due Date", "Assignment", "date"));
        grades.addField(new DataField("submitted_date", "Submitted", "Assignment", "date"));
        dataSources.add(grades);

        // Attendance data source
        DataSource attendance = new DataSource("attendance", "Attendance", "📅");
        attendance.addField(new DataField("student_name", "Student Name", "Student", "string"));
        attendance.addField(new DataField("date", "Date", "Attendance", "date"));
        attendance.addField(new DataField("status", "Status", "Attendance", "string"));
        attendance.addField(new DataField("period", "Period", "Attendance", "string"));
        attendance.addField(new DataField("course", "Course", "Attendance", "string"));
        attendance.addField(new DataField("teacher", "Teacher", "Attendance", "string"));
        attendance.addField(new DataField("notes", "Notes", "Attendance", "string"));
        dataSources.add(attendance);

        // Staff data source
        DataSource staff = new DataSource("staff", "Staff", "👨‍🏫");
        staff.addField(new DataField("staff_id", "Staff ID", "Basic Info", "string"));
        staff.addField(new DataField("first_name", "First Name", "Basic Info", "string"));
        staff.addField(new DataField("last_name", "Last Name", "Basic Info", "string"));
        staff.addField(new DataField("role", "Role", "Employment", "string"));
        staff.addField(new DataField("department", "Department", "Employment", "string"));
        staff.addField(new DataField("email", "Email", "Contact", "string"));
        staff.addField(new DataField("phone", "Phone", "Contact", "string"));
        staff.addField(new DataField("hire_date", "Hire Date", "Employment", "date"));
        dataSources.add(staff);

        // Set default
        activeDataSource.set(students);
    }

    // ========================================================================
    // CELL CLASSES
    // ========================================================================

    private class FieldTreeCell extends TreeCell<FieldTreeItem> {
        @Override
        protected void updateItem(FieldTreeItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            HBox cell = new HBox(8);
            cell.setAlignment(Pos.CENTER_LEFT);
            cell.setPadding(new Insets(4, 8, 4, 8));

            if (item.isCategory()) {
                // Category header
                Label label = new Label(item.getName());
                label.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #64748B;");
                cell.getChildren().add(label);
            } else {
                // Field item
                DataField field = item.getField();

                Label typeIcon = new Label(getTypeIcon(field.getDataType()));
                typeIcon.setStyle("-fx-font-size: 12px;");

                Label label = new Label(field.getDisplayName());
                label.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

                cell.getChildren().addAll(typeIcon, label);

                // Enable drag
                cell.setOnDragDetected(e -> {
                    Dragboard db = cell.startDragAndDrop(TransferMode.COPY);
                    ClipboardContent content = new ClipboardContent();
                    content.put(FIELD_FORMAT, field.getId());
                    db.setContent(content);
                    e.consume();
                });

                // Double click to add
                cell.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2) {
                        addFieldById(field.getId());
                    }
                });

                cell.setCursor(javafx.scene.Cursor.HAND);
            }

            setGraphic(cell);
            setText(null);
        }

        private String getTypeIcon(String type) {
            return switch (type) {
                case "string" -> "Aa";
                case "number" -> "#";
                case "date" -> "📅";
                case "boolean" -> "☑";
                default -> "•";
            };
        }
    }

    private class SelectedFieldCell extends ListCell<ReportField> {
        @Override
        protected void updateItem(ReportField item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            HBox cell = new HBox(12);
            cell.setAlignment(Pos.CENTER_LEFT);
            cell.setPadding(new Insets(10, 12, 10, 12));
            cell.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 6;");

            // Drag handle
            Label handle = new Label("⋮⋮");
            handle.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8; -fx-cursor: move;");

            // Field name
            Label name = new Label(item.getDisplayName());
            name.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #1E293B;");
            HBox.setHgrow(name, Priority.ALWAYS);

            // Width control
            Spinner<Integer> widthSpinner = new Spinner<>(60, 300, item.getWidth(), 10);
            widthSpinner.setPrefWidth(80);
            widthSpinner.setStyle("-fx-font-size: 11px;");
            widthSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                item.setWidth(newVal);
                updatePreview();
            });

            // Remove button
            Button removeBtn = new Button("✕");
            removeBtn.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: #94A3B8;
                -fx-font-size: 14px;
                -fx-cursor: hand;
                """);
            removeBtn.setOnAction(e -> {
                selectedFields.remove(item);
            });

            cell.getChildren().addAll(handle, name, widthSpinner, removeBtn);
            setGraphic(cell);
            setStyle("-fx-padding: 2; -fx-background-color: transparent;");
        }
    }

    // ========================================================================
    // DATA CLASSES
    // ========================================================================

    @Getter @Setter
    public static class DataSource {
        private String id;
        private String name;
        private String icon;
        private List<DataField> fields = new ArrayList<>();

        public DataSource(String id, String name, String icon) {
            this.id = id;
            this.name = name;
            this.icon = icon;
        }

        public void addField(DataField field) {
            fields.add(field);
        }
    }

    @Getter @Setter
    public static class DataField {
        private String id;
        private String displayName;
        private String category;
        private String dataType;

        public DataField(String id, String displayName, String category, String dataType) {
            this.id = id;
            this.displayName = displayName;
            this.category = category;
            this.dataType = dataType;
        }
    }

    @Getter @Setter
    public static class ReportField {
        private String fieldId;
        private String displayName;
        private String dataType;
        private int width = 120;
        private String format;
        private String aggregation;
    }

    @Getter @Setter
    public static class ReportFilter {
        private String id;
        private String fieldId;
        private String operator;
        private String value;
        private String connector = "AND";
    }

    @Getter @Setter
    public static class SortOption {
        private String id;
        private String fieldId;
        private String direction = "ASC";
    }

    @Getter @Setter
    public static class ReportDefinition {
        private String id;
        private String name;
        private DataSource dataSource;
        private List<ReportField> fields;
        private List<ReportFilter> filters;
        private List<SortOption> sortOptions;
        private String groupByField;
        private boolean showSubtotals;
        private LocalDateTime createdAt;
    }

    @Getter
    private static class FieldTreeItem {
        private final String name;
        private final DataField field;
        private final boolean category;

        public FieldTreeItem(String name, DataField field, boolean category) {
            this.name = name;
            this.field = field;
            this.category = category;
        }
    }
}
