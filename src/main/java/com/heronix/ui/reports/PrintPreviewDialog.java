package com.heronix.ui.reports;

import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.print.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.transform.Scale;
import javafx.stage.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Print Preview Dialog
 * Preview and configure document printing.
 *
 * Features:
 * - Visual print preview with zoom
 * - Page orientation selection
 * - Paper size options
 * - Margins configuration
 * - Header/footer customization
 * - Multi-page navigation
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class PrintPreviewDialog extends Stage {

    // ========================================================================
    // PROPERTIES
    // ========================================================================

    private final ObjectProperty<PageOrientation> orientation = new SimpleObjectProperty<>(PageOrientation.PORTRAIT);
    private final ObjectProperty<Paper> paperSize = new SimpleObjectProperty<>(Paper.NA_LETTER);
    private final DoubleProperty zoomLevel = new SimpleDoubleProperty(100);
    private final IntegerProperty currentPage = new SimpleIntegerProperty(1);
    private final IntegerProperty totalPages = new SimpleIntegerProperty(1);

    private final DoubleProperty marginTop = new SimpleDoubleProperty(0.5);
    private final DoubleProperty marginBottom = new SimpleDoubleProperty(0.5);
    private final DoubleProperty marginLeft = new SimpleDoubleProperty(0.5);
    private final DoubleProperty marginRight = new SimpleDoubleProperty(0.5);

    private final BooleanProperty showHeader = new SimpleBooleanProperty(true);
    private final BooleanProperty showFooter = new SimpleBooleanProperty(true);
    private final BooleanProperty showPageNumbers = new SimpleBooleanProperty(true);

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private StackPane previewContainer;
    private VBox previewPage;
    private Node contentToPrint;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<PrintSettings> onPrint;
    private Runnable onCancel;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public PrintPreviewDialog(Node content) {
        this.contentToPrint = content;

        initModality(Modality.APPLICATION_MODAL);
        setTitle("Print Preview");
        setMinWidth(900);
        setMinHeight(700);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #64748B;");

        // Toolbar
        root.setTop(createToolbar());

        // Main content
        HBox mainContent = new HBox(0);

        // Preview area
        ScrollPane previewScroll = createPreviewArea();
        HBox.setHgrow(previewScroll, Priority.ALWAYS);

        // Settings panel
        VBox settingsPanel = createSettingsPanel();

        mainContent.getChildren().addAll(previewScroll, settingsPanel);
        root.setCenter(mainContent);

        // Footer
        root.setBottom(createFooter());

        Scene scene = new Scene(root, 1000, 750);
        setScene(scene);

        // Initialize preview
        updatePreview();

        // Listeners
        orientation.addListener((obs, oldVal, newVal) -> updatePreview());
        paperSize.addListener((obs, oldVal, newVal) -> updatePreview());
        zoomLevel.addListener((obs, oldVal, newVal) -> updatePreviewZoom());
        marginTop.addListener((obs, oldVal, newVal) -> updatePreview());
        marginBottom.addListener((obs, oldVal, newVal) -> updatePreview());
        marginLeft.addListener((obs, oldVal, newVal) -> updatePreview());
        marginRight.addListener((obs, oldVal, newVal) -> updatePreview());
    }

    // ========================================================================
    // TOOLBAR
    // ========================================================================

    private HBox createToolbar() {
        HBox toolbar = new HBox(16);
        toolbar.setPadding(new Insets(12, 20, 12, 20));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        // Title
        Label title = new Label("Print Preview");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // Zoom controls
        HBox zoomControls = new HBox(8);
        zoomControls.setAlignment(Pos.CENTER);

        Button zoomOutBtn = new Button("−");
        zoomOutBtn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-font-size: 14px;
            -fx-font-weight: 700;
            -fx-padding: 6 12;
            -fx-background-radius: 4;
            -fx-cursor: hand;
            """);
        zoomOutBtn.setOnAction(e -> {
            if (zoomLevel.get() > 50) zoomLevel.set(zoomLevel.get() - 25);
        });

        Label zoomLabel = new Label();
        zoomLabel.textProperty().bind(zoomLevel.asString("%.0f%%"));
        zoomLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151; -fx-min-width: 50; -fx-alignment: center;");

        Button zoomInBtn = new Button("+");
        zoomInBtn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-font-size: 14px;
            -fx-font-weight: 700;
            -fx-padding: 6 12;
            -fx-background-radius: 4;
            -fx-cursor: hand;
            """);
        zoomInBtn.setOnAction(e -> {
            if (zoomLevel.get() < 200) zoomLevel.set(zoomLevel.get() + 25);
        });

        ComboBox<String> zoomCombo = new ComboBox<>();
        zoomCombo.getItems().addAll("50%", "75%", "100%", "125%", "150%", "200%");
        zoomCombo.setValue("100%");
        zoomCombo.setStyle("-fx-font-size: 12px;");
        zoomCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                zoomLevel.set(Double.parseDouble(newVal.replace("%", "")));
            }
        });

        zoomControls.getChildren().addAll(zoomOutBtn, zoomLabel, zoomInBtn, zoomCombo);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        // Page navigation
        HBox pageNav = new HBox(8);
        pageNav.setAlignment(Pos.CENTER);

        Button prevBtn = new Button("◀");
        prevBtn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-font-size: 12px;
            -fx-padding: 6 10;
            -fx-background-radius: 4;
            -fx-cursor: hand;
            """);
        prevBtn.setOnAction(e -> {
            if (currentPage.get() > 1) currentPage.set(currentPage.get() - 1);
        });
        prevBtn.disableProperty().bind(currentPage.lessThanOrEqualTo(1));

        Label pageLabel = new Label();
        pageLabel.textProperty().bind(
            currentPage.asString().concat(" of ").concat(totalPages.asString())
        );
        pageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

        Button nextBtn = new Button("▶");
        nextBtn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-font-size: 12px;
            -fx-padding: 6 10;
            -fx-background-radius: 4;
            -fx-cursor: hand;
            """);
        nextBtn.setOnAction(e -> {
            if (currentPage.get() < totalPages.get()) currentPage.set(currentPage.get() + 1);
        });
        nextBtn.disableProperty().bind(currentPage.greaterThanOrEqualTo(totalPages));

        pageNav.getChildren().addAll(prevBtn, pageLabel, nextBtn);

        toolbar.getChildren().addAll(title, spacer1, zoomControls, spacer2, pageNav);
        return toolbar;
    }

    // ========================================================================
    // PREVIEW AREA
    // ========================================================================

    private ScrollPane createPreviewArea() {
        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background-color: #64748B;");
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);

        previewContainer = new StackPane();
        previewContainer.setPadding(new Insets(40));
        previewContainer.setStyle("-fx-background-color: #64748B;");

        previewPage = new VBox();
        previewPage.setStyle("""
            -fx-background-color: white;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);
            """);

        previewContainer.getChildren().add(previewPage);
        scroll.setContent(previewContainer);

        return scroll;
    }

    private void updatePreview() {
        previewPage.getChildren().clear();

        // Calculate page dimensions
        double pageWidth = paperSize.get().getWidth();
        double pageHeight = paperSize.get().getHeight();

        if (orientation.get() == PageOrientation.LANDSCAPE) {
            double temp = pageWidth;
            pageWidth = pageHeight;
            pageHeight = temp;
        }

        // Scale for screen display (72 DPI -> screen)
        double scaleFactor = 1.0;
        previewPage.setPrefWidth(pageWidth * scaleFactor);
        previewPage.setPrefHeight(pageHeight * scaleFactor);
        previewPage.setMinWidth(pageWidth * scaleFactor);
        previewPage.setMinHeight(pageHeight * scaleFactor);

        // Content area with margins
        VBox contentArea = new VBox(0);
        contentArea.setPadding(new Insets(
            marginTop.get() * 72,
            marginRight.get() * 72,
            marginBottom.get() * 72,
            marginLeft.get() * 72
        ));
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        // Header
        if (showHeader.get()) {
            HBox header = createPageHeader();
            contentArea.getChildren().add(header);
        }

        // Main content
        VBox mainContent = new VBox(12);
        mainContent.setPadding(new Insets(16, 0, 16, 0));
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        // Add demo content for preview
        mainContent.getChildren().add(createDemoContent());

        contentArea.getChildren().add(mainContent);

        // Footer
        if (showFooter.get()) {
            HBox footer = createPageFooter();
            contentArea.getChildren().add(footer);
        }

        previewPage.getChildren().add(contentArea);
        updatePreviewZoom();
    }

    private void updatePreviewZoom() {
        double scale = zoomLevel.get() / 100.0;
        previewPage.setScaleX(scale);
        previewPage.setScaleY(scale);
    }

    private HBox createPageHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 12, 0));
        header.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        VBox leftBox = new VBox(2);
        Label schoolName = new Label("Heronix School District");
        schoolName.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        Label reportTitle = new Label("Student Roster Report");
        reportTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        leftBox.getChildren().addAll(schoolName, reportTitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox rightBox = new VBox(2);
        rightBox.setAlignment(Pos.TOP_RIGHT);

        Label dateLabel = new Label("Generated: " + LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")));
        dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8;");

        rightBox.getChildren().add(dateLabel);

        header.getChildren().addAll(leftBox, spacer, rightBox);
        return header;
    }

    private HBox createPageFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(12, 0, 0, 0));
        footer.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 1 0 0 0;");

        Label confidential = new Label("CONFIDENTIAL - For authorized use only");
        confidential.setStyle("-fx-font-size: 9px; -fx-text-fill: #94A3B8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (showPageNumbers.get()) {
            Label pageNum = new Label();
            pageNum.textProperty().bind(
                new SimpleStringProperty("Page ").concat(currentPage.asString())
                    .concat(" of ").concat(totalPages.asString())
            );
            pageNum.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748B;");
            footer.getChildren().addAll(confidential, spacer, pageNum);
        } else {
            footer.getChildren().addAll(confidential, spacer);
        }

        return footer;
    }

    private VBox createDemoContent() {
        VBox content = new VBox(12);

        // Title
        Label title = new Label("Student Roster - Grade 10");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        // Summary stats
        HBox stats = new HBox(24);
        stats.getChildren().addAll(
            createStatItem("Total Students", "318"),
            createStatItem("Active", "312"),
            createStatItem("Inactive", "6")
        );

        // Table header
        HBox tableHeader = new HBox();
        tableHeader.setPadding(new Insets(8, 12, 8, 12));
        tableHeader.setStyle("-fx-background-color: #F1F5F9;");

        String[] headers = {"ID", "Student Name", "Grade", "Email", "Status"};
        double[] widths = {60, 150, 50, 180, 70};

        for (int i = 0; i < headers.length; i++) {
            Label header = new Label(headers[i]);
            header.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #64748B;");
            header.setPrefWidth(widths[i]);
            tableHeader.getChildren().add(header);
        }

        // Table rows
        VBox tableBody = new VBox(0);
        String[][] data = {
            {"10001", "Anderson, James", "10", "james.anderson@school.edu", "Active"},
            {"10002", "Brown, Emily", "10", "emily.brown@school.edu", "Active"},
            {"10003", "Chen, Michael", "10", "michael.chen@school.edu", "Active"},
            {"10004", "Davis, Sarah", "10", "sarah.davis@school.edu", "Inactive"},
            {"10005", "Garcia, Carlos", "10", "carlos.garcia@school.edu", "Active"},
            {"10006", "Johnson, Lisa", "10", "lisa.johnson@school.edu", "Active"},
            {"10007", "Kim, David", "10", "david.kim@school.edu", "Active"},
            {"10008", "Martinez, Ana", "10", "ana.martinez@school.edu", "Active"},
        };

        for (int row = 0; row < data.length; row++) {
            HBox tableRow = new HBox();
            tableRow.setPadding(new Insets(6, 12, 6, 12));
            tableRow.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

            for (int col = 0; col < data[row].length; col++) {
                Label cell = new Label(data[row][col]);
                cell.setStyle("-fx-font-size: 10px; -fx-text-fill: #374151;");
                cell.setPrefWidth(widths[col]);
                tableRow.getChildren().add(cell);
            }

            tableBody.getChildren().add(tableRow);
        }

        content.getChildren().addAll(title, stats, tableHeader, tableBody);
        return content;
    }

    private VBox createStatItem(String label, String value) {
        VBox item = new VBox(0);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748B;");

        item.getChildren().addAll(valueLabel, labelText);
        return item;
    }

    // ========================================================================
    // SETTINGS PANEL
    // ========================================================================

    private VBox createSettingsPanel() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(280);
        panel.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 0 1;");

        Label title = new Label("Print Settings");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        // Orientation
        VBox orientationBox = createSettingSection("Orientation");
        HBox orientationOptions = new HBox(8);

        ToggleGroup orientGroup = new ToggleGroup();
        ToggleButton portraitBtn = createOrientationToggle("Portrait", "📄", orientGroup, true);
        ToggleButton landscapeBtn = createOrientationToggle("Landscape", "📄", orientGroup, false);

        portraitBtn.setOnAction(e -> orientation.set(PageOrientation.PORTRAIT));
        landscapeBtn.setOnAction(e -> orientation.set(PageOrientation.LANDSCAPE));

        orientationOptions.getChildren().addAll(portraitBtn, landscapeBtn);
        orientationBox.getChildren().add(orientationOptions);

        // Paper size
        VBox paperBox = createSettingSection("Paper Size");
        ComboBox<String> paperCombo = new ComboBox<>();
        paperCombo.getItems().addAll("Letter (8.5 x 11)", "Legal (8.5 x 14)", "A4 (210 x 297mm)");
        paperCombo.setValue("Letter (8.5 x 11)");
        paperCombo.setMaxWidth(Double.MAX_VALUE);
        paperCombo.setStyle("-fx-font-size: 12px;");
        paperCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.startsWith("Letter")) paperSize.set(Paper.NA_LETTER);
            else if (newVal.startsWith("Legal")) paperSize.set(Paper.LEGAL);
            else if (newVal.startsWith("A4")) paperSize.set(Paper.A4);
        });
        paperBox.getChildren().add(paperCombo);

        // Margins
        VBox marginsBox = createSettingSection("Margins (inches)");
        GridPane marginsGrid = new GridPane();
        marginsGrid.setHgap(12);
        marginsGrid.setVgap(8);

        marginsGrid.add(createMarginField("Top", marginTop), 0, 0);
        marginsGrid.add(createMarginField("Bottom", marginBottom), 1, 0);
        marginsGrid.add(createMarginField("Left", marginLeft), 0, 1);
        marginsGrid.add(createMarginField("Right", marginRight), 1, 1);

        marginsBox.getChildren().add(marginsGrid);

        // Header/Footer options
        VBox optionsBox = createSettingSection("Options");

        CheckBox headerCheck = new CheckBox("Show header");
        headerCheck.setSelected(true);
        headerCheck.setStyle("-fx-font-size: 12px;");
        headerCheck.selectedProperty().bindBidirectional(showHeader);

        CheckBox footerCheck = new CheckBox("Show footer");
        footerCheck.setSelected(true);
        footerCheck.setStyle("-fx-font-size: 12px;");
        footerCheck.selectedProperty().bindBidirectional(showFooter);

        CheckBox pageNumCheck = new CheckBox("Show page numbers");
        pageNumCheck.setSelected(true);
        pageNumCheck.setStyle("-fx-font-size: 12px;");
        pageNumCheck.selectedProperty().bindBidirectional(showPageNumbers);

        CheckBox colorCheck = new CheckBox("Print in color");
        colorCheck.setSelected(true);
        colorCheck.setStyle("-fx-font-size: 12px;");

        optionsBox.getChildren().addAll(headerCheck, footerCheck, pageNumCheck, colorCheck);

        // Copies
        VBox copiesBox = createSettingSection("Copies");
        HBox copiesRow = new HBox(12);
        copiesRow.setAlignment(Pos.CENTER_LEFT);

        Spinner<Integer> copiesSpinner = new Spinner<>(1, 100, 1);
        copiesSpinner.setPrefWidth(80);
        copiesSpinner.setStyle("-fx-font-size: 12px;");

        CheckBox collateCheck = new CheckBox("Collate");
        collateCheck.setStyle("-fx-font-size: 12px;");

        copiesRow.getChildren().addAll(copiesSpinner, collateCheck);
        copiesBox.getChildren().add(copiesRow);

        panel.getChildren().addAll(title, orientationBox, paperBox, marginsBox, optionsBox, copiesBox);
        return panel;
    }

    private VBox createSettingSection(String label) {
        VBox section = new VBox(8);

        Label sectionLabel = new Label(label);
        sectionLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: #64748B;");

        section.getChildren().add(sectionLabel);
        return section;
    }

    private ToggleButton createOrientationToggle(String text, String icon, ToggleGroup group, boolean selected) {
        ToggleButton btn = new ToggleButton(icon + " " + text);
        btn.setToggleGroup(group);
        btn.setSelected(selected);
        btn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btn, Priority.ALWAYS);

        updateOrientationButtonStyle(btn, selected);

        btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            updateOrientationButtonStyle(btn, isSelected);
        });

        return btn;
    }

    private void updateOrientationButtonStyle(ToggleButton btn, boolean selected) {
        btn.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-font-size: 11px;
            -fx-padding: 10 16;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """,
            selected ? "#3B82F6" : "#F1F5F9",
            selected ? "white" : "#374151"
        ));
    }

    private VBox createMarginField(String label, DoubleProperty value) {
        VBox field = new VBox(4);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8;");

        Spinner<Double> spinner = new Spinner<>(0.0, 2.0, value.get(), 0.25);
        spinner.setPrefWidth(80);
        spinner.setStyle("-fx-font-size: 11px;");
        spinner.valueProperty().addListener((obs, oldVal, newVal) -> value.set(newVal));

        field.getChildren().addAll(lbl, spinner);
        return field;
    }

    // ========================================================================
    // FOOTER
    // ========================================================================

    private HBox createFooter() {
        HBox footer = new HBox(12);
        footer.setPadding(new Insets(16, 24, 16, 24));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 1 0 0 0;");

        // Printer selection
        Label printerLabel = new Label("Printer:");
        printerLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        ComboBox<String> printerCombo = new ComboBox<>();
        printerCombo.getItems().addAll("Default Printer", "HP LaserJet Pro", "Canon PIXMA", "Microsoft Print to PDF");
        printerCombo.setValue("Default Printer");
        printerCombo.setStyle("-fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #64748B;
            -fx-font-size: 13px;
            -fx-padding: 10 20;
            -fx-cursor: hand;
            """);
        cancelBtn.setOnAction(e -> {
            if (onCancel != null) onCancel.run();
            close();
        });

        Button printBtn = new Button("🖨 Print");
        printBtn.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-padding: 10 24;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);
        printBtn.setOnAction(e -> executePrint());

        footer.getChildren().addAll(printerLabel, printerCombo, spacer, cancelBtn, printBtn);
        return footer;
    }

    // ========================================================================
    // PRINT EXECUTION
    // ========================================================================

    private void executePrint() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            // Configure page layout
            PageLayout pageLayout = job.getPrinter().createPageLayout(
                paperSize.get(),
                orientation.get(),
                marginLeft.get() * 72,
                marginRight.get() * 72,
                marginTop.get() * 72,
                marginBottom.get() * 72
            );
            job.getJobSettings().setPageLayout(pageLayout);

            // Show print dialog and print
            if (job.showPrintDialog(this)) {
                boolean success = job.printPage(contentToPrint != null ? contentToPrint : previewPage);
                if (success) {
                    job.endJob();
                    log.info("Print job completed successfully");

                    if (onPrint != null) {
                        PrintSettings settings = new PrintSettings();
                        settings.setOrientation(orientation.get());
                        settings.setPaperSize(paperSize.get());
                        settings.setMarginTop(marginTop.get());
                        settings.setMarginBottom(marginBottom.get());
                        settings.setMarginLeft(marginLeft.get());
                        settings.setMarginRight(marginRight.get());
                        settings.setShowHeader(showHeader.get());
                        settings.setShowFooter(showFooter.get());
                        settings.setShowPageNumbers(showPageNumbers.get());
                        onPrint.accept(settings);
                    }

                    close();
                } else {
                    log.error("Print job failed");
                }
            }
        }
    }

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    public void setOnPrint(Consumer<PrintSettings> callback) {
        this.onPrint = callback;
    }

    public void setOnCancel(Runnable callback) {
        this.onCancel = callback;
    }

    // ========================================================================
    // DATA CLASSES
    // ========================================================================

    @Getter @Setter
    public static class PrintSettings {
        private PageOrientation orientation;
        private Paper paperSize;
        private double marginTop;
        private double marginBottom;
        private double marginLeft;
        private double marginRight;
        private boolean showHeader;
        private boolean showFooter;
        private boolean showPageNumbers;
        private int copies = 1;
        private boolean collate;
        private boolean color = true;
    }
}
