package com.heronix.ui.reports;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/**
 * Report Template Manager
 * Manage saved report templates with categories and sharing.
 *
 * Features:
 * - Template library with categories
 * - Quick access to recent/favorites
 * - Template sharing and permissions
 * - Clone and customize templates
 * - Usage statistics
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class ReportTemplateManager extends BorderPane {

    // ========================================================================
    // DATA
    // ========================================================================

    private final ObservableList<ReportTemplate> templates = FXCollections.observableArrayList();
    private final ObservableList<ReportTemplate> filteredTemplates = FXCollections.observableArrayList();
    private final ObservableList<String> categories = FXCollections.observableArrayList();

    private final StringProperty searchQuery = new SimpleStringProperty("");
    private final StringProperty selectedCategory = new SimpleStringProperty("All");
    private final ObjectProperty<ReportTemplate> selectedTemplate = new SimpleObjectProperty<>();

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private ListView<ReportTemplate> templateList;
    private VBox detailPane;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<ReportTemplate> onOpenTemplate;
    private Consumer<ReportTemplate> onRunTemplate;
    private Consumer<ReportTemplate> onEditTemplate;
    private Consumer<ReportTemplate> onDeleteTemplate;
    private Runnable onCreateNew;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public ReportTemplateManager() {
        getStyleClass().add("report-template-manager");
        setStyle("-fx-background-color: #F8FAFC;");

        // Header
        setTop(createHeader());

        // Main content
        HBox content = new HBox(0);

        // Left sidebar with categories
        VBox sidebar = createSidebar();

        // Center template list
        VBox listPane = createTemplateListPane();
        HBox.setHgrow(listPane, Priority.ALWAYS);

        // Right detail pane
        detailPane = createDetailPane();

        content.getChildren().addAll(sidebar, listPane, detailPane);
        setCenter(content);

        // Initialize
        loadDemoTemplates();
        initializeCategories();
        filterTemplates();

        // Listeners
        searchQuery.addListener((obs, oldVal, newVal) -> filterTemplates());
        selectedCategory.addListener((obs, oldVal, newVal) -> filterTemplates());
    }

    // ========================================================================
    // HEADER
    // ========================================================================

    private HBox createHeader() {
        HBox header = new HBox(16);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        // Title
        VBox titleBox = new VBox(2);
        Label title = new Label("Report Templates");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");
        Label subtitle = new Label("Create, manage, and run saved report templates");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search
        TextField searchField = new TextField();
        searchField.setPromptText("Search templates...");
        searchField.setPrefWidth(250);
        searchField.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-background-radius: 8;
            -fx-padding: 10 16;
            -fx-font-size: 13px;
            """);
        searchField.textProperty().bindBidirectional(searchQuery);

        // Create new button
        Button createBtn = new Button("+ New Template");
        createBtn.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-padding: 10 20;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);
        createBtn.setOnAction(e -> {
            if (onCreateNew != null) onCreateNew.run();
        });

        header.getChildren().addAll(titleBox, spacer, searchField, createBtn);
        return header;
    }

    // ========================================================================
    // SIDEBAR
    // ========================================================================

    private VBox createSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.setPadding(new Insets(16));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 1 0 0;");

        // Quick access section
        Label quickLabel = new Label("QUICK ACCESS");
        quickLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #64748B; -fx-padding: 0 0 8 8;");

        VBox quickItems = new VBox(2);
        quickItems.getChildren().addAll(
            createSidebarItem("⭐", "Favorites", () -> selectedCategory.set("Favorites")),
            createSidebarItem("🕐", "Recent", () -> selectedCategory.set("Recent")),
            createSidebarItem("👤", "My Templates", () -> selectedCategory.set("My Templates")),
            createSidebarItem("🔗", "Shared with Me", () -> selectedCategory.set("Shared"))
        );

        // Categories section
        Label catLabel = new Label("CATEGORIES");
        catLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #64748B; -fx-padding: 16 0 8 8;");

        VBox catItems = new VBox(2);
        catItems.getChildren().add(createSidebarItem("📋", "All Templates", () -> selectedCategory.set("All")));

        for (String category : categories) {
            String icon = getCategoryIcon(category);
            catItems.getChildren().add(createSidebarItem(icon, category, () -> selectedCategory.set(category)));
        }

        // Stats section
        VBox statsBox = new VBox(8);
        statsBox.setPadding(new Insets(16));
        statsBox.setStyle("""
            -fx-background-color: #F8FAFC;
            -fx-background-radius: 8;
            """);
        VBox.setMargin(statsBox, new Insets(16, 0, 0, 0));

        Label statsTitle = new Label("Usage This Month");
        statsTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #374151;");

        HBox stat1 = createStatRow("Reports Run", "47");
        HBox stat2 = createStatRow("Exports", "23");
        HBox stat3 = createStatRow("Schedules Active", "5");

        statsBox.getChildren().addAll(statsTitle, stat1, stat2, stat3);

        sidebar.getChildren().addAll(quickLabel, quickItems, catLabel, catItems, statsBox);
        return sidebar;
    }

    private HBox createSidebarItem(String icon, String label, Runnable action) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 12, 10, 12));
        item.setCursor(javafx.scene.Cursor.HAND);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 14px;");

        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        item.getChildren().addAll(iconLabel, nameLabel);

        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 8;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-color: transparent;"));
        item.setOnMouseClicked(e -> action.run());

        return item;
    }

    private HBox createStatRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label valueText = new Label(value);
        valueText.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        row.getChildren().addAll(labelText, spacer, valueText);
        return row;
    }

    private String getCategoryIcon(String category) {
        return switch (category) {
            case "Student Reports" -> "👤";
            case "Academic Reports" -> "📚";
            case "Attendance Reports" -> "📅";
            case "Financial Reports" -> "💰";
            case "Staff Reports" -> "👨‍🏫";
            case "Compliance Reports" -> "✅";
            default -> "📊";
        };
    }

    // ========================================================================
    // TEMPLATE LIST
    // ========================================================================

    private VBox createTemplateListPane() {
        VBox pane = new VBox(0);
        pane.setStyle("-fx-background-color: #F8FAFC;");

        // Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setPadding(new Insets(12, 16, 12, 16));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: #F8FAFC;");

        Label countLabel = new Label();
        countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Name A-Z", "Name Z-A", "Recently Modified", "Most Used");
        sortCombo.setValue("Name A-Z");
        sortCombo.setStyle("-fx-font-size: 12px;");

        ToggleGroup viewGroup = new ToggleGroup();
        ToggleButton gridBtn = new ToggleButton("⊞");
        gridBtn.setToggleGroup(viewGroup);
        gridBtn.setSelected(true);
        gridBtn.setStyle("-fx-background-color: #E2E8F0; -fx-padding: 6 10; -fx-background-radius: 4 0 0 4;");

        ToggleButton listBtn = new ToggleButton("☰");
        listBtn.setToggleGroup(viewGroup);
        listBtn.setStyle("-fx-background-color: transparent; -fx-padding: 6 10; -fx-background-radius: 0 4 4 0;");

        HBox viewToggle = new HBox(0);
        viewToggle.getChildren().addAll(gridBtn, listBtn);

        toolbar.getChildren().addAll(countLabel, spacer, sortCombo, viewToggle);

        // Template grid/list
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        FlowPane templateGrid = new FlowPane(16, 16);
        templateGrid.setPadding(new Insets(16));

        filteredTemplates.addListener((ListChangeListener<ReportTemplate>) c -> {
            templateGrid.getChildren().clear();
            for (ReportTemplate template : filteredTemplates) {
                templateGrid.getChildren().add(createTemplateCard(template));
            }
            countLabel.setText(filteredTemplates.size() + " templates");
        });

        scroll.setContent(templateGrid);

        pane.getChildren().addAll(toolbar, scroll);
        return pane;
    }

    private VBox createTemplateCard(ReportTemplate template) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setPrefWidth(280);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-border-color: #E2E8F0;
            -fx-border-radius: 12;
            -fx-cursor: hand;
            """);

        // Header with icon and favorite
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(getCategoryIcon(template.getCategory()));
        icon.setStyle("""
            -fx-font-size: 20px;
            -fx-background-color: #F1F5F9;
            -fx-padding: 8;
            -fx-background-radius: 8;
            """);

        VBox titleBox = new VBox(2);
        Label name = new Label(template.getName());
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        Label category = new Label(template.getCategory());
        category.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        titleBox.getChildren().addAll(name, category);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button favBtn = new Button(template.isFavorite() ? "★" : "☆");
        favBtn.setStyle(String.format("""
            -fx-background-color: transparent;
            -fx-font-size: 16px;
            -fx-text-fill: %s;
            -fx-cursor: hand;
            """, template.isFavorite() ? "#F59E0B" : "#CBD5E1"));
        favBtn.setOnAction(e -> {
            template.setFavorite(!template.isFavorite());
            favBtn.setText(template.isFavorite() ? "★" : "☆");
            favBtn.setStyle(String.format("""
                -fx-background-color: transparent;
                -fx-font-size: 16px;
                -fx-text-fill: %s;
                -fx-cursor: hand;
                """, template.isFavorite() ? "#F59E0B" : "#CBD5E1"));
        });

        header.getChildren().addAll(icon, titleBox, favBtn);

        // Description
        Label description = new Label(template.getDescription());
        description.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B; -fx-wrap-text: true;");
        description.setWrapText(true);
        description.setMaxHeight(40);

        // Tags
        HBox tags = new HBox(6);
        for (String tag : template.getTags()) {
            Label tagLabel = new Label(tag);
            tagLabel.setStyle("""
                -fx-background-color: #EFF6FF;
                -fx-text-fill: #2563EB;
                -fx-font-size: 10px;
                -fx-padding: 3 8;
                -fx-background-radius: 10;
                """);
            tags.getChildren().add(tagLabel);
        }

        // Footer
        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label lastRun = new Label("Last run: " + formatDate(template.getLastRunDate()));
        lastRun.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        Label runCount = new Label("▶ " + template.getRunCount());
        runCount.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        footer.getChildren().addAll(lastRun, footerSpacer, runCount);

        // Action buttons (visible on hover)
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);
        actions.setVisible(false);
        actions.setManaged(false);

        Button runBtn = new Button("▶ Run");
        runBtn.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 11px;
            -fx-font-weight: 600;
            -fx-padding: 6 12;
            -fx-background-radius: 4;
            -fx-cursor: hand;
            """);
        runBtn.setOnAction(e -> {
            if (onRunTemplate != null) onRunTemplate.accept(template);
        });

        Button editBtn = new Button("✏ Edit");
        editBtn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-text-fill: #374151;
            -fx-font-size: 11px;
            -fx-padding: 6 12;
            -fx-background-radius: 4;
            -fx-cursor: hand;
            """);
        editBtn.setOnAction(e -> {
            if (onEditTemplate != null) onEditTemplate.accept(template);
        });

        MenuButton moreBtn = new MenuButton("⋯");
        moreBtn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-font-size: 11px;
            -fx-padding: 6 10;
            -fx-background-radius: 4;
            """);
        moreBtn.getItems().addAll(
            createMenuItem("📋 Clone", () -> cloneTemplate(template)),
            createMenuItem("📤 Share", () -> shareTemplate(template)),
            createMenuItem("📅 Schedule", () -> scheduleTemplate(template)),
            new SeparatorMenuItem(),
            createMenuItem("🗑 Delete", () -> {
                if (onDeleteTemplate != null) onDeleteTemplate.accept(template);
            })
        );

        actions.getChildren().addAll(runBtn, editBtn, moreBtn);

        card.getChildren().addAll(header, description, tags, footer, actions);

        // Hover effects
        card.setOnMouseEntered(e -> {
            card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 12;
                -fx-border-color: #3B82F6;
                -fx-border-radius: 12;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.15), 12, 0, 0, 4);
                """);
            actions.setVisible(true);
            actions.setManaged(true);
            footer.setVisible(false);
            footer.setManaged(false);
        });

        card.setOnMouseExited(e -> {
            card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 12;
                -fx-border-color: #E2E8F0;
                -fx-border-radius: 12;
                -fx-cursor: hand;
                """);
            actions.setVisible(false);
            actions.setManaged(false);
            footer.setVisible(true);
            footer.setManaged(true);
        });

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                selectedTemplate.set(template);
                updateDetailPane();
            } else if (e.getClickCount() == 2) {
                if (onOpenTemplate != null) onOpenTemplate.accept(template);
            }
        });

        return card;
    }

    private MenuItem createMenuItem(String text, Runnable action) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(e -> action.run());
        return item;
    }

    // ========================================================================
    // DETAIL PANE
    // ========================================================================

    private VBox createDetailPane() {
        VBox pane = new VBox(0);
        pane.setPrefWidth(320);
        pane.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 0 1;");

        // Placeholder when no template selected
        VBox placeholder = new VBox(12);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPadding(new Insets(40));
        VBox.setVgrow(placeholder, Priority.ALWAYS);

        Label icon = new Label("📊");
        icon.setStyle("-fx-font-size: 48px; -fx-text-fill: #CBD5E1;");

        Label text = new Label("Select a template to view details");
        text.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");

        placeholder.getChildren().addAll(icon, text);
        pane.getChildren().add(placeholder);

        return pane;
    }

    private void updateDetailPane() {
        detailPane.getChildren().clear();
        ReportTemplate template = selectedTemplate.get();

        if (template == null) {
            VBox placeholder = new VBox(12);
            placeholder.setAlignment(Pos.CENTER);
            placeholder.setPadding(new Insets(40));
            VBox.setVgrow(placeholder, Priority.ALWAYS);

            Label icon = new Label("📊");
            icon.setStyle("-fx-font-size: 48px; -fx-text-fill: #CBD5E1;");

            Label text = new Label("Select a template to view details");
            text.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");

            placeholder.getChildren().addAll(icon, text);
            detailPane.getChildren().add(placeholder);
            return;
        }

        // Header
        VBox header = new VBox(8);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(getCategoryIcon(template.getCategory()));
        icon.setStyle("""
            -fx-font-size: 24px;
            -fx-background-color: #F1F5F9;
            -fx-padding: 12;
            -fx-background-radius: 8;
            """);

        VBox titleBox = new VBox(2);
        Label name = new Label(template.getName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");
        name.setWrapText(true);

        Label cat = new Label(template.getCategory());
        cat.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        titleBox.getChildren().addAll(name, cat);

        titleRow.getChildren().addAll(icon, titleBox);
        header.getChildren().add(titleRow);

        // Action buttons
        HBox actions = new HBox(8);
        actions.setPadding(new Insets(0, 20, 16, 20));

        Button runBtn = new Button("▶ Run Report");
        runBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(runBtn, Priority.ALWAYS);
        runBtn.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-padding: 10 16;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);
        runBtn.setOnAction(e -> {
            if (onRunTemplate != null) onRunTemplate.accept(template);
        });

        Button editBtn = new Button("✏");
        editBtn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-text-fill: #374151;
            -fx-font-size: 14px;
            -fx-padding: 10 14;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);
        editBtn.setTooltip(new Tooltip("Edit Template"));
        editBtn.setOnAction(e -> {
            if (onEditTemplate != null) onEditTemplate.accept(template);
        });

        actions.getChildren().addAll(runBtn, editBtn);

        // Details scroll
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox details = new VBox(16);
        details.setPadding(new Insets(16, 20, 20, 20));

        // Description
        VBox descSection = createDetailSection("Description", template.getDescription());

        // Fields
        VBox fieldsSection = new VBox(8);
        Label fieldsLabel = new Label("Included Fields");
        fieldsLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #64748B;");

        VBox fieldsList = new VBox(4);
        for (String field : template.getFields()) {
            Label fieldItem = new Label("• " + field);
            fieldItem.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");
            fieldsList.getChildren().add(fieldItem);
        }
        fieldsSection.getChildren().addAll(fieldsLabel, fieldsList);

        // Info cards
        VBox infoSection = new VBox(8);
        infoSection.getChildren().addAll(
            createInfoRow("Created by", template.getCreatedBy()),
            createInfoRow("Created", formatDate(template.getCreatedDate())),
            createInfoRow("Last modified", formatDate(template.getModifiedDate())),
            createInfoRow("Run count", String.valueOf(template.getRunCount())),
            createInfoRow("Last run", formatDate(template.getLastRunDate()))
        );

        // Schedule info
        if (template.isScheduled()) {
            VBox scheduleSection = new VBox(8);
            Label scheduleLabel = new Label("Schedule");
            scheduleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #64748B;");

            HBox scheduleInfo = new HBox(8);
            scheduleInfo.setAlignment(Pos.CENTER_LEFT);
            scheduleInfo.setPadding(new Insets(8, 12, 8, 12));
            scheduleInfo.setStyle("-fx-background-color: #ECFDF5; -fx-background-radius: 6;");

            Label schedIcon = new Label("📅");
            Label schedText = new Label(template.getScheduleDescription());
            schedText.setStyle("-fx-font-size: 12px; -fx-text-fill: #059669;");

            scheduleInfo.getChildren().addAll(schedIcon, schedText);
            scheduleSection.getChildren().addAll(scheduleLabel, scheduleInfo);
            details.getChildren().add(scheduleSection);
        }

        details.getChildren().addAll(descSection, fieldsSection, infoSection);
        scroll.setContent(details);

        detailPane.getChildren().addAll(header, actions, scroll);
    }

    private VBox createDetailSection(String title, String content) {
        VBox section = new VBox(6);

        Label label = new Label(title);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #64748B;");

        Label text = new Label(content);
        text.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        text.setWrapText(true);

        section.getChildren().addAll(label, text);
        return section;
    }

    private HBox createInfoRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");
        labelText.setMinWidth(100);

        Label valueText = new Label(value);
        valueText.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

        row.getChildren().addAll(labelText, valueText);
        return row;
    }

    // ========================================================================
    // FILTERING
    // ========================================================================

    private void filterTemplates() {
        String query = searchQuery.get().toLowerCase();
        String category = selectedCategory.get();

        filteredTemplates.clear();
        filteredTemplates.addAll(
            templates.stream()
                .filter(t -> {
                    // Search filter
                    if (!query.isEmpty()) {
                        if (!t.getName().toLowerCase().contains(query) &&
                            !t.getDescription().toLowerCase().contains(query)) {
                            return false;
                        }
                    }

                    // Category filter
                    return switch (category) {
                        case "All" -> true;
                        case "Favorites" -> t.isFavorite();
                        case "Recent" -> t.getLastRunDate() != null &&
                            t.getLastRunDate().isAfter(LocalDateTime.now().minusDays(7));
                        case "My Templates" -> t.getCreatedBy().equals("Current User");
                        case "Shared" -> t.isShared();
                        default -> t.getCategory().equals(category);
                    };
                })
                .sorted(Comparator.comparing(ReportTemplate::getName))
                .toList()
        );
    }

    // ========================================================================
    // ACTIONS
    // ========================================================================

    private void cloneTemplate(ReportTemplate template) {
        ReportTemplate clone = new ReportTemplate();
        clone.setId(UUID.randomUUID().toString());
        clone.setName(template.getName() + " (Copy)");
        clone.setDescription(template.getDescription());
        clone.setCategory(template.getCategory());
        clone.setFields(new ArrayList<>(template.getFields()));
        clone.setTags(new ArrayList<>(template.getTags()));
        clone.setCreatedBy("Current User");
        clone.setCreatedDate(LocalDateTime.now());
        clone.setModifiedDate(LocalDateTime.now());

        templates.add(clone);
        filterTemplates();
        log.info("Cloned template: {}", template.getName());
    }

    private void shareTemplate(ReportTemplate template) {
        template.setShared(true);
        log.info("Shared template: {}", template.getName());
    }

    private void scheduleTemplate(ReportTemplate template) {
        log.info("Schedule template: {}", template.getName());
    }

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    public void setOnOpenTemplate(Consumer<ReportTemplate> callback) {
        this.onOpenTemplate = callback;
    }

    public void setOnRunTemplate(Consumer<ReportTemplate> callback) {
        this.onRunTemplate = callback;
    }

    public void setOnEditTemplate(Consumer<ReportTemplate> callback) {
        this.onEditTemplate = callback;
    }

    public void setOnDeleteTemplate(Consumer<ReportTemplate> callback) {
        this.onDeleteTemplate = callback;
    }

    public void setOnCreateNew(Runnable callback) {
        this.onCreateNew = callback;
    }

    // ========================================================================
    // UTILITIES
    // ========================================================================

    private String formatDate(LocalDateTime date) {
        if (date == null) return "Never";
        return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    private void initializeCategories() {
        categories.addAll(
            "Student Reports",
            "Academic Reports",
            "Attendance Reports",
            "Financial Reports",
            "Staff Reports",
            "Compliance Reports"
        );
    }

    // ========================================================================
    // DEMO DATA
    // ========================================================================

    private void loadDemoTemplates() {
        templates.addAll(
            createTemplate("Student Roster", "Student Reports",
                "Complete list of all enrolled students with contact information",
                List.of("Student ID", "Name", "Grade", "Email", "Phone", "Status"),
                List.of("roster", "students"),
                true, false, 45),

            createTemplate("Grade Distribution", "Academic Reports",
                "Analysis of grade distribution across all courses",
                List.of("Course", "A Count", "B Count", "C Count", "D Count", "F Count", "Average"),
                List.of("grades", "analysis"),
                true, true, 32),

            createTemplate("Daily Attendance", "Attendance Reports",
                "Daily attendance summary by period and class",
                List.of("Date", "Period", "Course", "Present", "Absent", "Tardy", "Rate"),
                List.of("attendance", "daily"),
                false, true, 89),

            createTemplate("GPA Report", "Academic Reports",
                "Student GPA rankings with class rank calculations",
                List.of("Rank", "Student Name", "Grade Level", "GPA", "Credits"),
                List.of("gpa", "rankings"),
                true, false, 28),

            createTemplate("Fee Balance Report", "Financial Reports",
                "Outstanding fee balances by student",
                List.of("Student", "Fee Type", "Amount Due", "Amount Paid", "Balance"),
                List.of("fees", "balance"),
                false, false, 15),

            createTemplate("Teacher Schedule", "Staff Reports",
                "Complete teaching schedule for all staff members",
                List.of("Teacher", "Period", "Course", "Room", "Students"),
                List.of("schedule", "staff"),
                false, false, 22),

            createTemplate("Immunization Compliance", "Compliance Reports",
                "Student immunization status and compliance tracking",
                List.of("Student", "Vaccine", "Date Given", "Status", "Due Date"),
                List.of("health", "compliance"),
                true, true, 8),

            createTemplate("Transcript Summary", "Academic Reports",
                "Academic transcript summary for all students",
                List.of("Student", "Credits Attempted", "Credits Earned", "GPA", "Status"),
                List.of("transcript", "summary"),
                false, false, 18)
        );
    }

    private ReportTemplate createTemplate(String name, String category, String description,
                                          List<String> fields, List<String> tags,
                                          boolean favorite, boolean scheduled, int runCount) {
        ReportTemplate template = new ReportTemplate();
        template.setId(UUID.randomUUID().toString());
        template.setName(name);
        template.setCategory(category);
        template.setDescription(description);
        template.setFields(fields);
        template.setTags(tags);
        template.setFavorite(favorite);
        template.setScheduled(scheduled);
        template.setScheduleDescription(scheduled ? "Weekly on Monday at 8:00 AM" : null);
        template.setRunCount(runCount);
        template.setCreatedBy("Current User");
        template.setCreatedDate(LocalDateTime.now().minusDays(30 + new Random().nextInt(60)));
        template.setModifiedDate(LocalDateTime.now().minusDays(new Random().nextInt(14)));
        template.setLastRunDate(LocalDateTime.now().minusDays(new Random().nextInt(7)));
        return template;
    }

    // ========================================================================
    // DATA CLASSES
    // ========================================================================

    @Getter @Setter
    public static class ReportTemplate {
        private String id;
        private String name;
        private String category;
        private String description;
        private List<String> fields = new ArrayList<>();
        private List<String> tags = new ArrayList<>();
        private boolean favorite;
        private boolean shared;
        private boolean scheduled;
        private String scheduleDescription;
        private int runCount;
        private String createdBy;
        private LocalDateTime createdDate;
        private LocalDateTime modifiedDate;
        private LocalDateTime lastRunDate;
    }
}
