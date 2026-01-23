package com.heronix.ui.component;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Enhanced Table View Component
 * A feature-rich table component with pagination, filtering, bulk operations, and more.
 *
 * Features:
 * - Pagination with configurable page sizes
 * - Client-side filtering
 * - Multi-select with checkbox column
 * - Bulk operations toolbar
 * - Column visibility toggle
 * - Context menu support
 * - Keyboard navigation
 * - Loading states
 * - Empty states
 *
 * @param <T> The type of items in the table
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class EnhancedTableView<T> extends VBox {

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private final TableView<T> tableView;
    private final HBox toolbar;
    private final HBox bulkActionsBar;
    private final HBox paginationBar;
    private final StackPane loadingOverlay;
    private final VBox emptyState;

    // Selection column
    private TableColumn<T, Boolean> selectColumn;
    private CheckBox selectAllCheckbox;

    // Pagination controls
    private final ComboBox<Integer> pageSizeCombo;
    private final Label pageInfoLabel;
    private final Button firstPageBtn;
    private final Button prevPageBtn;
    private final Button nextPageBtn;
    private final Button lastPageBtn;
    private final TextField pageNumberField;

    // ========================================================================
    // DATA
    // ========================================================================

    private final ObservableList<T> allItems = FXCollections.observableArrayList();
    private final FilteredList<T> filteredItems;
    private final ObservableList<T> displayedItems = FXCollections.observableArrayList();
    private final ObservableSet<T> selectedItems = FXCollections.observableSet(new LinkedHashSet<>());

    // ========================================================================
    // PROPERTIES
    // ========================================================================

    private final IntegerProperty currentPage = new SimpleIntegerProperty(1);
    private final IntegerProperty pageSize = new SimpleIntegerProperty(25);
    private final IntegerProperty totalPages = new SimpleIntegerProperty(1);
    private final IntegerProperty totalItems = new SimpleIntegerProperty(0);
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final BooleanProperty showPagination = new SimpleBooleanProperty(true);
    private final BooleanProperty showSelectColumn = new SimpleBooleanProperty(true);
    private final BooleanProperty showToolbar = new SimpleBooleanProperty(true);

    private final ObjectProperty<Predicate<T>> filter = new SimpleObjectProperty<>(item -> true);

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<T> onRowDoubleClick;
    private Consumer<List<T>> onSelectionChanged;
    private Consumer<List<T>> onBulkDelete;
    private Consumer<List<T>> onBulkEdit;
    private Consumer<List<T>> onBulkExport;
    private Function<T, ContextMenu> contextMenuFactory;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public EnhancedTableView() {
        // Initialize filtered list
        filteredItems = new FilteredList<>(allItems);

        // Create components
        tableView = new TableView<>();
        toolbar = createToolbar();
        bulkActionsBar = createBulkActionsBar();
        paginationBar = new HBox(10);
        loadingOverlay = createLoadingOverlay();
        emptyState = createEmptyState();

        // Pagination controls
        pageSizeCombo = new ComboBox<>(FXCollections.observableArrayList(10, 25, 50, 100, 250));
        pageInfoLabel = new Label();
        firstPageBtn = new Button("⏮");
        prevPageBtn = new Button("◀");
        nextPageBtn = new Button("▶");
        lastPageBtn = new Button("⏭");
        pageNumberField = new TextField();

        // Setup
        setupTableView();
        setupPagination();
        setupLayout();
        setupBindings();
        setupKeyboardShortcuts();

        log.debug("EnhancedTableView initialized");
    }

    // ========================================================================
    // SETUP METHODS
    // ========================================================================

    private void setupTableView() {
        tableView.setItems(displayedItems);
        tableView.setEditable(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.setPlaceholder(new Label("No data"));
        tableView.getStyleClass().add("table-modern");
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        // Row factory for context menu and double-click
        tableView.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();

            // Double-click handler
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && onRowDoubleClick != null) {
                    onRowDoubleClick.accept(row.getItem());
                }
            });

            // Context menu
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty() && contextMenuFactory != null) {
                    ContextMenu menu = contextMenuFactory.apply(row.getItem());
                    if (menu != null) {
                        menu.show(row, event.getScreenX(), event.getScreenY());
                    }
                }
            });

            return row;
        });
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(8, 12, 8, 12));
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");
        return toolbar;
    }

    private HBox createBulkActionsBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8, 12, 8, 12));
        bar.setStyle("-fx-background-color: #DBEAFE; -fx-border-color: #2563EB; -fx-border-width: 0 0 1 0;");
        bar.setVisible(false);
        bar.setManaged(false);

        Label selectedLabel = new Label();
        selectedLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #1E40AF;");

        Button editBtn = new Button("✏️ Edit Selected");
        editBtn.getStyleClass().addAll("btn", "btn-ghost");
        editBtn.setOnAction(e -> {
            if (onBulkEdit != null) onBulkEdit.accept(new ArrayList<>(selectedItems));
        });

        Button deleteBtn = new Button("🗑️ Delete Selected");
        deleteBtn.getStyleClass().addAll("btn", "btn-ghost");
        deleteBtn.setStyle("-fx-text-fill: #EF4444;");
        deleteBtn.setOnAction(e -> {
            if (onBulkDelete != null) onBulkDelete.accept(new ArrayList<>(selectedItems));
        });

        Button exportBtn = new Button("📥 Export Selected");
        exportBtn.getStyleClass().addAll("btn", "btn-ghost");
        exportBtn.setOnAction(e -> {
            if (onBulkExport != null) onBulkExport.accept(new ArrayList<>(selectedItems));
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button clearBtn = new Button("✕ Clear Selection");
        clearBtn.getStyleClass().addAll("btn", "btn-ghost");
        clearBtn.setOnAction(e -> clearSelection());

        bar.getChildren().addAll(selectedLabel, editBtn, deleteBtn, exportBtn, spacer, clearBtn);

        // Update selected count
        selectedItems.addListener((SetChangeListener<T>) change -> {
            int count = selectedItems.size();
            selectedLabel.setText(count + " item" + (count != 1 ? "s" : "") + " selected");
        });

        return bar;
    }

    private void setupPagination() {
        paginationBar.setAlignment(Pos.CENTER);
        paginationBar.setPadding(new Insets(8, 12, 8, 12));
        paginationBar.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 1 0 0 0;");

        // Page size selector
        Label pageSizeLabel = new Label("Show:");
        pageSizeCombo.setValue(25);
        pageSizeCombo.setOnAction(e -> {
            pageSize.set(pageSizeCombo.getValue());
            currentPage.set(1);
            updateDisplayedItems();
        });

        // Navigation buttons
        firstPageBtn.getStyleClass().addAll("btn", "btn-ghost", "btn-sm");
        prevPageBtn.getStyleClass().addAll("btn", "btn-ghost", "btn-sm");
        nextPageBtn.getStyleClass().addAll("btn", "btn-ghost", "btn-sm");
        lastPageBtn.getStyleClass().addAll("btn", "btn-ghost", "btn-sm");

        firstPageBtn.setOnAction(e -> goToPage(1));
        prevPageBtn.setOnAction(e -> goToPage(currentPage.get() - 1));
        nextPageBtn.setOnAction(e -> goToPage(currentPage.get() + 1));
        lastPageBtn.setOnAction(e -> goToPage(totalPages.get()));

        // Page number input
        pageNumberField.setPrefWidth(50);
        pageNumberField.setAlignment(Pos.CENTER);
        pageNumberField.setOnAction(e -> {
            try {
                int page = Integer.parseInt(pageNumberField.getText());
                goToPage(page);
            } catch (NumberFormatException ex) {
                pageNumberField.setText(String.valueOf(currentPage.get()));
            }
        });

        Label ofLabel = new Label("of");
        Label totalPagesLabel = new Label();
        totalPagesLabel.textProperty().bind(totalPages.asString());

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        paginationBar.getChildren().addAll(
                pageSizeLabel, pageSizeCombo,
                leftSpacer,
                firstPageBtn, prevPageBtn,
                new Label("Page"), pageNumberField, ofLabel, totalPagesLabel,
                nextPageBtn, lastPageBtn,
                rightSpacer,
                pageInfoLabel
        );
    }

    private StackPane createLoadingOverlay() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8);");
        overlay.setVisible(false);

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(48, 48);

        VBox content = new VBox(10, spinner, new Label("Loading..."));
        content.setAlignment(Pos.CENTER);

        overlay.getChildren().add(content);
        return overlay;
    }

    private VBox createEmptyState() {
        VBox empty = new VBox(16);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(48));
        empty.setVisible(false);
        empty.setManaged(false);

        Label icon = new Label("📋");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("No data found");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #475569;");

        Label description = new Label("Try adjusting your filters or add new records");
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");

        empty.getChildren().addAll(icon, title, description);
        return empty;
    }

    private void setupLayout() {
        setSpacing(0);
        getStyleClass().add("enhanced-table-view");

        StackPane tableStack = new StackPane(tableView, loadingOverlay, emptyState);
        VBox.setVgrow(tableStack, Priority.ALWAYS);

        getChildren().addAll(toolbar, bulkActionsBar, tableStack, paginationBar);

        // Bind visibility
        toolbar.visibleProperty().bind(showToolbar);
        toolbar.managedProperty().bind(showToolbar);
        paginationBar.visibleProperty().bind(showPagination);
        paginationBar.managedProperty().bind(showPagination);
    }

    private void setupBindings() {
        // Filter binding
        filter.addListener((obs, oldVal, newVal) -> {
            filteredItems.setPredicate(newVal);
            currentPage.set(1);
            updateDisplayedItems();
        });

        // Loading overlay
        loading.addListener((obs, oldVal, newVal) -> {
            loadingOverlay.setVisible(newVal);
        });

        // Page info
        currentPage.addListener((obs, oldVal, newVal) -> {
            pageNumberField.setText(String.valueOf(newVal.intValue()));
            updatePageInfo();
            updateNavigationButtons();
        });

        totalPages.addListener((obs, oldVal, newVal) -> {
            updateNavigationButtons();
        });

        // Selection tracking
        tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<T>) change -> {
            // Note: We use checkbox selection, not row selection for bulk ops
        });
    }

    private void setupKeyboardShortcuts() {
        tableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.A && event.isControlDown()) {
                // Ctrl+A: Select all
                selectAll();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                // Escape: Clear selection
                clearSelection();
                event.consume();
            } else if (event.getCode() == KeyCode.DELETE) {
                // Delete: Bulk delete
                if (!selectedItems.isEmpty() && onBulkDelete != null) {
                    onBulkDelete.accept(new ArrayList<>(selectedItems));
                }
                event.consume();
            }
        });
    }

    // ========================================================================
    // PAGINATION METHODS
    // ========================================================================

    private void updateDisplayedItems() {
        int total = filteredItems.size();
        totalItems.set(total);

        int pages = Math.max(1, (int) Math.ceil((double) total / pageSize.get()));
        totalPages.set(pages);

        // Adjust current page if out of bounds
        if (currentPage.get() > pages) {
            currentPage.set(pages);
        }

        // Calculate slice
        int start = (currentPage.get() - 1) * pageSize.get();
        int end = Math.min(start + pageSize.get(), total);

        displayedItems.setAll(filteredItems.subList(start, end));

        // Update empty state
        boolean isEmpty = displayedItems.isEmpty();
        emptyState.setVisible(isEmpty && !loading.get());
        emptyState.setManaged(isEmpty && !loading.get());
        tableView.setVisible(!isEmpty);

        updatePageInfo();
        log.debug("Displaying items {}-{} of {}", start + 1, end, total);
    }

    private void updatePageInfo() {
        int total = totalItems.get();
        if (total == 0) {
            pageInfoLabel.setText("No items");
            return;
        }

        int start = (currentPage.get() - 1) * pageSize.get() + 1;
        int end = Math.min(currentPage.get() * pageSize.get(), total);
        pageInfoLabel.setText(String.format("Showing %d-%d of %d", start, end, total));
    }

    private void updateNavigationButtons() {
        int page = currentPage.get();
        int total = totalPages.get();

        firstPageBtn.setDisable(page <= 1);
        prevPageBtn.setDisable(page <= 1);
        nextPageBtn.setDisable(page >= total);
        lastPageBtn.setDisable(page >= total);
    }

    public void goToPage(int page) {
        int validPage = Math.max(1, Math.min(page, totalPages.get()));
        if (validPage != currentPage.get()) {
            currentPage.set(validPage);
            updateDisplayedItems();
        }
    }

    // ========================================================================
    // SELECTION METHODS
    // ========================================================================

    /**
     * Add checkbox selection column
     */
    public void addSelectColumn() {
        if (selectColumn != null) return;

        selectColumn = new TableColumn<>();
        selectColumn.setPrefWidth(40);
        selectColumn.setMinWidth(40);
        selectColumn.setMaxWidth(40);
        selectColumn.setSortable(false);
        selectColumn.setResizable(false);

        // Header checkbox
        selectAllCheckbox = new CheckBox();
        selectAllCheckbox.setOnAction(e -> {
            if (selectAllCheckbox.isSelected()) {
                selectAll();
            } else {
                clearSelection();
            }
        });
        selectColumn.setGraphic(selectAllCheckbox);

        // Cell checkbox
        selectColumn.setCellFactory(col -> new TableCell<T, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(e -> {
                    T item = getTableRow().getItem();
                    if (item != null) {
                        if (checkBox.isSelected()) {
                            selectedItems.add(item);
                        } else {
                            selectedItems.remove(item);
                        }
                        updateSelectionUI();
                    }
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    T rowItem = getTableRow().getItem();
                    checkBox.setSelected(selectedItems.contains(rowItem));
                    setGraphic(checkBox);
                }
            }
        });

        tableView.getColumns().add(0, selectColumn);
    }

    public void selectAll() {
        selectedItems.addAll(displayedItems);
        updateSelectionUI();
    }

    public void clearSelection() {
        selectedItems.clear();
        updateSelectionUI();
    }

    public void invertSelection() {
        Set<T> newSelection = new LinkedHashSet<>(displayedItems);
        newSelection.removeAll(selectedItems);
        selectedItems.clear();
        selectedItems.addAll(newSelection);
        updateSelectionUI();
    }

    private void updateSelectionUI() {
        // Update bulk actions bar visibility
        boolean hasSelection = !selectedItems.isEmpty();
        bulkActionsBar.setVisible(hasSelection);
        bulkActionsBar.setManaged(hasSelection);

        // Update select all checkbox
        if (selectAllCheckbox != null) {
            if (selectedItems.isEmpty()) {
                selectAllCheckbox.setSelected(false);
                selectAllCheckbox.setIndeterminate(false);
            } else if (selectedItems.containsAll(displayedItems)) {
                selectAllCheckbox.setSelected(true);
                selectAllCheckbox.setIndeterminate(false);
            } else {
                selectAllCheckbox.setIndeterminate(true);
            }
        }

        // Refresh table to update checkboxes
        tableView.refresh();

        // Fire callback
        if (onSelectionChanged != null) {
            onSelectionChanged.accept(new ArrayList<>(selectedItems));
        }
    }

    public Set<T> getSelectedItems() {
        return Collections.unmodifiableSet(selectedItems);
    }

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /**
     * Set all items in the table
     */
    public void setItems(List<T> items) {
        allItems.setAll(items);
        currentPage.set(1);
        updateDisplayedItems();
    }

    /**
     * Add items to the table
     */
    public void addItems(List<T> items) {
        allItems.addAll(items);
        updateDisplayedItems();
    }

    /**
     * Remove items from the table
     */
    public void removeItems(List<T> items) {
        allItems.removeAll(items);
        selectedItems.removeAll(items);
        updateDisplayedItems();
        updateSelectionUI();
    }

    /**
     * Clear all items
     */
    public void clearItems() {
        allItems.clear();
        selectedItems.clear();
        updateDisplayedItems();
        updateSelectionUI();
    }

    /**
     * Refresh the table display
     */
    public void refresh() {
        updateDisplayedItems();
        tableView.refresh();
    }

    /**
     * Get the underlying TableView
     */
    public TableView<T> getTableView() {
        return tableView;
    }

    /**
     * Get columns for customization
     */
    public ObservableList<TableColumn<T, ?>> getColumns() {
        return tableView.getColumns();
    }

    /**
     * Add a column to the table
     */
    public void addColumn(TableColumn<T, ?> column) {
        tableView.getColumns().add(column);
    }

    /**
     * Set filter predicate
     */
    public void setFilter(Predicate<T> predicate) {
        filter.set(predicate != null ? predicate : item -> true);
    }

    /**
     * Clear filter
     */
    public void clearFilter() {
        filter.set(item -> true);
    }

    /**
     * Set loading state
     */
    public void setLoading(boolean isLoading) {
        loading.set(isLoading);
    }

    /**
     * Add node to toolbar
     */
    public void addToToolbar(Node node) {
        toolbar.getChildren().add(node);
    }

    /**
     * Set toolbar content
     */
    public void setToolbarContent(Node... nodes) {
        toolbar.getChildren().setAll(nodes);
    }

    // ========================================================================
    // CALLBACK SETTERS
    // ========================================================================

    public void setOnRowDoubleClick(Consumer<T> callback) {
        this.onRowDoubleClick = callback;
    }

    public void setOnSelectionChanged(Consumer<List<T>> callback) {
        this.onSelectionChanged = callback;
    }

    public void setOnBulkDelete(Consumer<List<T>> callback) {
        this.onBulkDelete = callback;
    }

    public void setOnBulkEdit(Consumer<List<T>> callback) {
        this.onBulkEdit = callback;
    }

    public void setOnBulkExport(Consumer<List<T>> callback) {
        this.onBulkExport = callback;
    }

    public void setContextMenuFactory(Function<T, ContextMenu> factory) {
        this.contextMenuFactory = factory;
    }

    // ========================================================================
    // PROPERTY ACCESSORS
    // ========================================================================

    public IntegerProperty currentPageProperty() { return currentPage; }
    public IntegerProperty pageSizeProperty() { return pageSize; }
    public IntegerProperty totalPagesProperty() { return totalPages; }
    public IntegerProperty totalItemsProperty() { return totalItems; }
    public BooleanProperty loadingProperty() { return loading; }
    public BooleanProperty showPaginationProperty() { return showPagination; }
    public BooleanProperty showSelectColumnProperty() { return showSelectColumn; }
    public BooleanProperty showToolbarProperty() { return showToolbar; }
}
