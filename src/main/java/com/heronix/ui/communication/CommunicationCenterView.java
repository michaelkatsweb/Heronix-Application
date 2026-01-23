package com.heronix.ui.communication;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
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
 * Communication Center View
 * Unified messaging hub for school communication.
 *
 * Features:
 * - Email-style inbox with folders
 * - Compose new messages with recipients
 * - Reply/Forward/Archive actions
 * - Announcements board
 * - Contact directory
 * - Message templates
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
public class CommunicationCenterView extends BorderPane {

    // ========================================================================
    // DATA
    // ========================================================================

    private final ObservableList<Message> messages = FXCollections.observableArrayList();
    private final ObservableList<Message> filteredMessages = FXCollections.observableArrayList();
    private final ObservableList<Contact> contacts = FXCollections.observableArrayList();
    private final ObservableList<Announcement> announcements = FXCollections.observableArrayList();

    private final ObjectProperty<Folder> selectedFolder = new SimpleObjectProperty<>(Folder.INBOX);
    private final ObjectProperty<Message> selectedMessage = new SimpleObjectProperty<>();

    // ========================================================================
    // COMPONENTS
    // ========================================================================

    private VBox folderList;
    private ListView<Message> messageList;
    private VBox messageDetail;
    private VBox composePane;
    private TabPane mainTabs;

    // Compose fields
    private TextField toField;
    private TextField ccField;
    private TextField subjectField;
    private TextArea bodyArea;
    private ComboBox<String> templateCombo;

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    private Consumer<Message> onMessageSent;
    private Consumer<Message> onMessageDeleted;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public CommunicationCenterView() {
        getStyleClass().add("communication-center");
        setStyle("-fx-background-color: #F8FAFC;");

        // Header
        setTop(createHeader());

        // Main content with tabs
        mainTabs = new TabPane();
        mainTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        mainTabs.getStyleClass().add("communication-tabs");

        Tab messagesTab = new Tab("Messages", createMessagesContent());
        messagesTab.setGraphic(createTabIcon("✉"));

        Tab announcementsTab = new Tab("Announcements", createAnnouncementsContent());
        announcementsTab.setGraphic(createTabIcon("📢"));

        Tab contactsTab = new Tab("Contacts", createContactsContent());
        contactsTab.setGraphic(createTabIcon("👥"));

        mainTabs.getTabs().addAll(messagesTab, announcementsTab, contactsTab);

        setCenter(mainTabs);

        // Load demo data
        loadDemoData();
        filterMessages();

        // Listeners
        selectedFolder.addListener((obs, oldVal, newVal) -> filterMessages());
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
        Label title = new Label("Communication Center");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");
        Label subtitle = new Label("Messages, announcements, and contacts");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Compose button
        Button composeBtn = new Button("✏ Compose");
        composeBtn.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-padding: 10 20;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);
        composeBtn.setOnAction(e -> showComposePane(null));

        // Search
        TextField searchField = new TextField();
        searchField.setPromptText("Search messages...");
        searchField.setPrefWidth(250);
        searchField.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-background-radius: 8;
            -fx-padding: 10 16;
            -fx-font-size: 13px;
            """);

        header.getChildren().addAll(titleBox, spacer, searchField, composeBtn);
        return header;
    }

    // ========================================================================
    // MESSAGES TAB
    // ========================================================================

    private HBox createMessagesContent() {
        HBox content = new HBox(0);

        // Left: Folder list
        folderList = createFolderList();

        // Center: Message list
        VBox messageListPane = createMessageListPane();
        HBox.setHgrow(messageListPane, Priority.SOMETIMES);

        // Right: Message detail / Compose
        StackPane detailStack = new StackPane();
        messageDetail = createMessageDetailPane();
        composePane = createComposePane();
        composePane.setVisible(false);
        composePane.setManaged(false);
        detailStack.getChildren().addAll(messageDetail, composePane);
        HBox.setHgrow(detailStack, Priority.ALWAYS);

        content.getChildren().addAll(folderList, messageListPane, detailStack);
        return content;
    }

    private VBox createFolderList() {
        VBox folders = new VBox(4);
        folders.setPadding(new Insets(16));
        folders.setPrefWidth(200);
        folders.setStyle("-fx-background-color: #F1F5F9; -fx-border-color: #E2E8F0; -fx-border-width: 0 1 0 0;");

        Label header = new Label("FOLDERS");
        header.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #64748B; -fx-padding: 0 0 8 8;");
        folders.getChildren().add(header);

        for (Folder folder : Folder.values()) {
            HBox item = createFolderItem(folder);
            folders.getChildren().add(item);
        }

        // Labels section
        Label labelsHeader = new Label("LABELS");
        labelsHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #64748B; -fx-padding: 16 0 8 8;");
        folders.getChildren().add(labelsHeader);

        folders.getChildren().addAll(
            createLabelItem("Important", "#EF4444"),
            createLabelItem("Parents", "#8B5CF6"),
            createLabelItem("Staff", "#10B981"),
            createLabelItem("Students", "#F59E0B")
        );

        return folders;
    }

    private HBox createFolderItem(Folder folder) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 12, 10, 12));
        item.setCursor(javafx.scene.Cursor.HAND);

        Label icon = new Label(folder.getIcon());
        icon.setStyle("-fx-font-size: 14px;");

        Label name = new Label(folder.getDisplayName());
        name.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Count badge
        long count = messages.stream().filter(m -> matchesFolder(m, folder)).count();
        Label badge = new Label(String.valueOf(count));
        badge.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #64748B;
            -fx-background-color: #E2E8F0;
            -fx-background-radius: 10;
            -fx-padding: 2 8;
            """);
        badge.setVisible(count > 0);

        item.getChildren().addAll(icon, name, spacer, badge);

        // Selection styling
        updateFolderItemStyle(item, folder == selectedFolder.get());

        item.setOnMouseClicked(e -> {
            selectedFolder.set(folder);
            // Update all folder items
            for (var node : folderList.getChildren()) {
                if (node instanceof HBox hbox && hbox.getUserData() instanceof Folder f) {
                    updateFolderItemStyle(hbox, f == folder);
                }
            }
        });

        item.setUserData(folder);
        return item;
    }

    private void updateFolderItemStyle(HBox item, boolean selected) {
        if (selected) {
            item.setStyle("-fx-background-color: #DBEAFE; -fx-background-radius: 8;");
        } else {
            item.setStyle("-fx-background-color: transparent; -fx-background-radius: 8;");
        }
    }

    private HBox createLabelItem(String labelName, String color) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(8, 12, 8, 12));
        item.setCursor(javafx.scene.Cursor.HAND);

        Label dot = new Label("●");
        dot.setStyle("-fx-font-size: 10px; -fx-text-fill: " + color + ";");

        Label name = new Label(labelName);
        name.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        item.getChildren().addAll(dot, name);

        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 8;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-color: transparent;"));

        return item;
    }

    private VBox createMessageListPane() {
        VBox pane = new VBox(0);
        pane.setPrefWidth(350);
        pane.setMinWidth(300);
        pane.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 1 0 0;");

        // Toolbar
        HBox toolbar = new HBox(8);
        toolbar.setPadding(new Insets(12, 16, 12, 16));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        CheckBox selectAll = new CheckBox();
        selectAll.setStyle("-fx-font-size: 12px;");

        Button archiveBtn = createToolbarButton("📥", "Archive");
        Button deleteBtn = createToolbarButton("🗑", "Delete");
        Button markReadBtn = createToolbarButton("✓", "Mark Read");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Newest First", "Oldest First", "Unread First");
        sortCombo.setValue("Newest First");
        sortCombo.setStyle("-fx-font-size: 12px;");

        toolbar.getChildren().addAll(selectAll, archiveBtn, deleteBtn, markReadBtn, spacer, sortCombo);

        // Message list
        messageList = new ListView<>(filteredMessages);
        messageList.setCellFactory(lv -> new MessageListCell());
        messageList.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(messageList, Priority.ALWAYS);

        messageList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedMessage.set(newVal);
            updateMessageDetail();
        });

        pane.getChildren().addAll(toolbar, messageList);
        return pane;
    }

    private Button createToolbarButton(String icon, String tooltip) {
        Button btn = new Button(icon);
        btn.setStyle("""
            -fx-background-color: transparent;
            -fx-font-size: 14px;
            -fx-padding: 6 10;
            -fx-cursor: hand;
            """);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setOnMouseEntered(e -> btn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-background-radius: 6;
            -fx-font-size: 14px;
            -fx-padding: 6 10;
            -fx-cursor: hand;
            """));
        btn.setOnMouseExited(e -> btn.setStyle("""
            -fx-background-color: transparent;
            -fx-font-size: 14px;
            -fx-padding: 6 10;
            -fx-cursor: hand;
            """));
        return btn;
    }

    private VBox createMessageDetailPane() {
        VBox detail = new VBox(0);
        detail.setStyle("-fx-background-color: white;");

        // Placeholder when no message selected
        VBox placeholder = new VBox(12);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPadding(new Insets(60));

        Label icon = new Label("✉");
        icon.setStyle("-fx-font-size: 48px; -fx-text-fill: #CBD5E1;");

        Label text = new Label("Select a message to read");
        text.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");

        placeholder.getChildren().addAll(icon, text);
        detail.getChildren().add(placeholder);
        VBox.setVgrow(placeholder, Priority.ALWAYS);

        return detail;
    }

    private void updateMessageDetail() {
        Message msg = selectedMessage.get();
        messageDetail.getChildren().clear();

        if (msg == null) {
            VBox placeholder = new VBox(12);
            placeholder.setAlignment(Pos.CENTER);
            placeholder.setPadding(new Insets(60));

            Label icon = new Label("✉");
            icon.setStyle("-fx-font-size: 48px; -fx-text-fill: #CBD5E1;");

            Label text = new Label("Select a message to read");
            text.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");

            placeholder.getChildren().addAll(icon, text);
            messageDetail.getChildren().add(placeholder);
            VBox.setVgrow(placeholder, Priority.ALWAYS);
            return;
        }

        // Mark as read
        msg.setRead(true);
        messageList.refresh();

        // Header
        VBox header = new VBox(8);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        Label subject = new Label(msg.getSubject());
        subject.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");
        subject.setWrapText(true);

        HBox senderRow = new HBox(12);
        senderRow.setAlignment(Pos.CENTER_LEFT);

        // Avatar
        Label avatar = new Label(msg.getSenderName().substring(0, 1).toUpperCase());
        avatar.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: 700;
            -fx-pref-width: 40;
            -fx-pref-height: 40;
            -fx-alignment: center;
            -fx-background-radius: 20;
            """);
        avatar.setMinSize(40, 40);
        avatar.setAlignment(Pos.CENTER);

        VBox senderInfo = new VBox(2);
        Label senderName = new Label(msg.getSenderName());
        senderName.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1E293B;");

        Label senderEmail = new Label("<" + msg.getSenderEmail() + ">");
        senderEmail.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        senderInfo.getChildren().addAll(senderName, senderEmail);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timestamp = new Label(formatTimestamp(msg.getTimestamp()));
        timestamp.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");

        senderRow.getChildren().addAll(avatar, senderInfo, spacer, timestamp);

        // To line
        HBox toRow = new HBox(8);
        toRow.setAlignment(Pos.CENTER_LEFT);
        Label toLabel = new Label("To:");
        toLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        Label toValue = new Label(String.join(", ", msg.getRecipients()));
        toValue.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");
        toRow.getChildren().addAll(toLabel, toValue);

        header.getChildren().addAll(subject, senderRow, toRow);

        // Action buttons
        HBox actions = new HBox(8);
        actions.setPadding(new Insets(12, 24, 12, 24));
        actions.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        Button replyBtn = createActionButton("↩ Reply", true);
        replyBtn.setOnAction(e -> showComposePane(msg));

        Button replyAllBtn = createActionButton("↩ Reply All", false);
        Button forwardBtn = createActionButton("→ Forward", false);
        Button archiveBtn = createActionButton("📥 Archive", false);
        Button deleteBtn = createActionButton("🗑 Delete", false);

        actions.getChildren().addAll(replyBtn, replyAllBtn, forwardBtn, archiveBtn, deleteBtn);

        // Body
        ScrollPane bodyScroll = new ScrollPane();
        bodyScroll.setFitToWidth(true);
        bodyScroll.setStyle("-fx-background-color: white;");
        VBox.setVgrow(bodyScroll, Priority.ALWAYS);

        VBox bodyContent = new VBox(16);
        bodyContent.setPadding(new Insets(20, 24, 20, 24));

        Label body = new Label(msg.getBody());
        body.setWrapText(true);
        body.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151; -fx-line-spacing: 4;");

        bodyContent.getChildren().add(body);

        // Attachments if any
        if (msg.getAttachments() != null && !msg.getAttachments().isEmpty()) {
            VBox attachmentBox = new VBox(8);
            attachmentBox.setPadding(new Insets(16, 0, 0, 0));

            Label attachHeader = new Label("Attachments (" + msg.getAttachments().size() + ")");
            attachHeader.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #64748B;");

            javafx.scene.layout.FlowPane attachList = new javafx.scene.layout.FlowPane();
            attachList.setHgap(8);
            attachList.setVgap(8);

            for (String attachment : msg.getAttachments()) {
                HBox attachItem = new HBox(6);
                attachItem.setAlignment(Pos.CENTER_LEFT);
                attachItem.setPadding(new Insets(8, 12, 8, 12));
                attachItem.setStyle("""
                    -fx-background-color: #F1F5F9;
                    -fx-background-radius: 6;
                    -fx-cursor: hand;
                    """);

                Label fileIcon = new Label("📎");
                Label fileName = new Label(attachment);
                fileName.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

                attachItem.getChildren().addAll(fileIcon, fileName);
                attachList.getChildren().add(attachItem);
            }

            attachmentBox.getChildren().addAll(attachHeader, attachList);
            bodyContent.getChildren().add(attachmentBox);
        }

        bodyScroll.setContent(bodyContent);

        messageDetail.getChildren().addAll(header, actions, bodyScroll);
    }

    private Button createActionButton(String text, boolean primary) {
        Button btn = new Button(text);
        if (primary) {
            btn.setStyle("""
                -fx-background-color: #3B82F6;
                -fx-text-fill: white;
                -fx-font-size: 12px;
                -fx-font-weight: 600;
                -fx-padding: 8 16;
                -fx-background-radius: 6;
                -fx-cursor: hand;
                """);
        } else {
            btn.setStyle("""
                -fx-background-color: #F1F5F9;
                -fx-text-fill: #374151;
                -fx-font-size: 12px;
                -fx-font-weight: 500;
                -fx-padding: 8 16;
                -fx-background-radius: 6;
                -fx-cursor: hand;
                """);
        }
        return btn;
    }

    private VBox createComposePane() {
        VBox compose = new VBox(0);
        compose.setStyle("-fx-background-color: white;");

        // Header
        HBox header = new HBox(12);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        Label title = new Label("New Message");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-font-size: 16px;
            -fx-text-fill: #64748B;
            -fx-cursor: hand;
            """);
        closeBtn.setOnAction(e -> hideComposePane());

        header.getChildren().addAll(title, spacer, closeBtn);

        // Form
        VBox form = new VBox(12);
        form.setPadding(new Insets(16, 20, 16, 20));

        // Template selector
        HBox templateRow = new HBox(12);
        templateRow.setAlignment(Pos.CENTER_LEFT);

        Label templateLabel = new Label("Template:");
        templateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
        templateLabel.setPrefWidth(60);

        templateCombo = new ComboBox<>();
        templateCombo.getItems().addAll(
            "None",
            "Parent Meeting Request",
            "Grade Update Notification",
            "Attendance Alert",
            "Event Announcement",
            "Assignment Reminder"
        );
        templateCombo.setValue("None");
        templateCombo.setPrefWidth(250);
        templateCombo.setStyle("-fx-font-size: 13px;");
        templateCombo.setOnAction(e -> applyTemplate());

        templateRow.getChildren().addAll(templateLabel, templateCombo);

        // To field
        HBox toRow = createFormRow("To:", toField = new TextField());
        toField.setPromptText("Enter recipient names or emails...");

        // CC field
        HBox ccRow = createFormRow("Cc:", ccField = new TextField());
        ccField.setPromptText("Add Cc recipients...");

        // Subject field
        HBox subjectRow = createFormRow("Subject:", subjectField = new TextField());
        subjectField.setPromptText("Enter subject...");

        form.getChildren().addAll(templateRow, toRow, ccRow, subjectRow);

        // Body
        VBox bodyBox = new VBox(8);
        bodyBox.setPadding(new Insets(0, 20, 16, 20));
        VBox.setVgrow(bodyBox, Priority.ALWAYS);

        bodyArea = new TextArea();
        bodyArea.setPromptText("Write your message here...");
        bodyArea.setWrapText(true);
        bodyArea.setStyle("""
            -fx-font-size: 14px;
            -fx-background-color: #F8FAFC;
            -fx-background-radius: 8;
            """);
        VBox.setVgrow(bodyArea, Priority.ALWAYS);

        bodyBox.getChildren().add(bodyArea);

        // Attachments
        HBox attachRow = new HBox(12);
        attachRow.setPadding(new Insets(0, 20, 16, 20));
        attachRow.setAlignment(Pos.CENTER_LEFT);

        Button attachBtn = new Button("📎 Attach File");
        attachBtn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-text-fill: #374151;
            -fx-font-size: 12px;
            -fx-padding: 8 16;
            -fx-background-radius: 6;
            -fx-cursor: hand;
            """);

        attachRow.getChildren().add(attachBtn);

        // Footer with send button
        HBox footer = new HBox(12);
        footer.setPadding(new Insets(16, 20, 16, 20));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 1 0 0 0;");

        Button discardBtn = new Button("Discard");
        discardBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #64748B;
            -fx-font-size: 13px;
            -fx-font-weight: 500;
            -fx-padding: 10 20;
            -fx-cursor: hand;
            """);
        discardBtn.setOnAction(e -> hideComposePane());

        Button saveDraftBtn = new Button("Save Draft");
        saveDraftBtn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-text-fill: #374151;
            -fx-font-size: 13px;
            -fx-font-weight: 500;
            -fx-padding: 10 20;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);

        Button sendBtn = new Button("Send Message");
        sendBtn.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-padding: 10 24;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);
        sendBtn.setOnAction(e -> sendMessage());

        footer.getChildren().addAll(discardBtn, saveDraftBtn, sendBtn);

        compose.getChildren().addAll(header, form, bodyBox, attachRow, footer);
        return compose;
    }

    private HBox createFormRow(String label, TextField field) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
        lbl.setPrefWidth(60);

        field.setStyle("""
            -fx-font-size: 13px;
            -fx-background-color: #F8FAFC;
            -fx-background-radius: 6;
            -fx-padding: 10 12;
            """);
        HBox.setHgrow(field, Priority.ALWAYS);

        row.getChildren().addAll(lbl, field);
        return row;
    }

    private void showComposePane(Message replyTo) {
        composePane.setVisible(true);
        composePane.setManaged(true);
        messageDetail.setVisible(false);
        messageDetail.setManaged(false);

        // Clear fields
        toField.clear();
        ccField.clear();
        subjectField.clear();
        bodyArea.clear();
        templateCombo.setValue("None");

        // If replying
        if (replyTo != null) {
            toField.setText(replyTo.getSenderEmail());
            subjectField.setText("Re: " + replyTo.getSubject());
            bodyArea.setText("\n\n--- Original Message ---\n" +
                "From: " + replyTo.getSenderName() + "\n" +
                "Date: " + formatTimestamp(replyTo.getTimestamp()) + "\n\n" +
                replyTo.getBody());
        }
    }

    private void hideComposePane() {
        composePane.setVisible(false);
        composePane.setManaged(false);
        messageDetail.setVisible(true);
        messageDetail.setManaged(true);
    }

    private void applyTemplate() {
        String template = templateCombo.getValue();
        if (template == null || template.equals("None")) return;

        switch (template) {
            case "Parent Meeting Request" -> {
                subjectField.setText("Meeting Request - Student Progress Discussion");
                bodyArea.setText("""
                    Dear Parent/Guardian,

                    I would like to schedule a meeting to discuss your child's academic progress and address any questions or concerns you may have.

                    Please let me know your availability for a meeting in the coming week. I am available during the following times:
                    - [Insert available times]

                    Looking forward to speaking with you.

                    Best regards,
                    [Your Name]
                    """);
            }
            case "Grade Update Notification" -> {
                subjectField.setText("Grade Update Notification");
                bodyArea.setText("""
                    Dear Parent/Guardian,

                    This message is to inform you about a recent grade update for your student.

                    Assignment: [Assignment Name]
                    Grade: [Grade]
                    Comments: [Optional comments]

                    If you have any questions, please don't hesitate to reach out.

                    Best regards,
                    [Your Name]
                    """);
            }
            case "Attendance Alert" -> {
                subjectField.setText("Attendance Notice");
                bodyArea.setText("""
                    Dear Parent/Guardian,

                    We are reaching out regarding your student's attendance record.

                    [Student Name] has been marked as [absent/tardy] on [date(s)].

                    Please contact the school office if you have any questions or need to provide documentation for the absence(s).

                    Thank you for your attention to this matter.

                    Best regards,
                    [Your Name]
                    """);
            }
            case "Event Announcement" -> {
                subjectField.setText("Upcoming Event: [Event Name]");
                bodyArea.setText("""
                    Dear Families,

                    We are excited to announce an upcoming event!

                    Event: [Event Name]
                    Date: [Date]
                    Time: [Time]
                    Location: [Location]

                    [Event description and details]

                    We look forward to seeing you there!

                    Best regards,
                    [Your Name]
                    """);
            }
            case "Assignment Reminder" -> {
                subjectField.setText("Reminder: Upcoming Assignment Due");
                bodyArea.setText("""
                    Dear Students and Families,

                    This is a friendly reminder about an upcoming assignment.

                    Assignment: [Assignment Name]
                    Due Date: [Due Date]
                    Subject: [Subject/Class]

                    Please ensure the assignment is completed and submitted on time.

                    If you have any questions, please reach out.

                    Best regards,
                    [Your Name]
                    """);
            }
        }
    }

    private void sendMessage() {
        String to = toField.getText().trim();
        String subject = subjectField.getText().trim();
        String body = bodyArea.getText().trim();

        if (to.isEmpty() || subject.isEmpty() || body.isEmpty()) {
            // Show validation error
            return;
        }

        Message sent = new Message();
        sent.setId(UUID.randomUUID().toString());
        sent.setSenderName("Current User");
        sent.setSenderEmail("user@school.edu");
        sent.setRecipients(List.of(to));
        sent.setSubject(subject);
        sent.setBody(body);
        sent.setTimestamp(LocalDateTime.now());
        sent.setRead(true);
        sent.setFolder(Folder.SENT);

        messages.add(sent);

        if (onMessageSent != null) {
            onMessageSent.accept(sent);
        }

        hideComposePane();
        log.info("Message sent to: {}", to);
    }

    // ========================================================================
    // ANNOUNCEMENTS TAB
    // ========================================================================

    private VBox createAnnouncementsContent() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(20, 24, 20, 24));

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("School Announcements");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newBtn = new Button("+ New Announcement");
        newBtn.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-padding: 10 20;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);

        header.getChildren().addAll(title, spacer, newBtn);

        // Filters
        HBox filters = new HBox(12);
        filters.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("All Categories", "General", "Academic", "Athletics", "Events", "Emergency");
        categoryFilter.setValue("All Categories");
        categoryFilter.setStyle("-fx-font-size: 13px;");

        ComboBox<String> audienceFilter = new ComboBox<>();
        audienceFilter.getItems().addAll("All Audiences", "Students", "Parents", "Staff", "Everyone");
        audienceFilter.setValue("All Audiences");
        audienceFilter.setStyle("-fx-font-size: 13px;");

        filters.getChildren().addAll(categoryFilter, audienceFilter);

        // Announcements list
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox announcementList = new VBox(12);
        for (Announcement ann : announcements) {
            announcementList.getChildren().add(createAnnouncementCard(ann));
        }

        scroll.setContent(announcementList);

        content.getChildren().addAll(header, filters, scroll);
        return content;
    }

    private VBox createAnnouncementCard(Announcement ann) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-border-color: #E2E8F0;
            -fx-border-radius: 12;
            """);

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Priority badge
        if (ann.isPriority()) {
            Label priorityBadge = new Label("IMPORTANT");
            priorityBadge.setStyle("""
                -fx-background-color: #FEF2F2;
                -fx-text-fill: #DC2626;
                -fx-font-size: 10px;
                -fx-font-weight: 700;
                -fx-padding: 4 8;
                -fx-background-radius: 4;
                """);
            header.getChildren().add(priorityBadge);
        }

        // Category badge
        Label categoryBadge = new Label(ann.getCategory());
        categoryBadge.setStyle("""
            -fx-background-color: #EFF6FF;
            -fx-text-fill: #2563EB;
            -fx-font-size: 10px;
            -fx-font-weight: 600;
            -fx-padding: 4 8;
            -fx-background-radius: 4;
            """);
        header.getChildren().add(categoryBadge);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label date = new Label(formatTimestamp(ann.getPostedDate()));
        date.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");

        header.getChildren().addAll(spacer, date);

        // Title
        Label title = new Label(ann.getTitle());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");
        title.setWrapText(true);

        // Content preview
        Label content = new Label(ann.getContent());
        content.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B; -fx-line-spacing: 2;");
        content.setWrapText(true);

        // Footer
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label author = new Label("Posted by " + ann.getAuthor());
        author.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        Label audience = new Label("👥 " + ann.getAudience());
        audience.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        footer.getChildren().addAll(author, footerSpacer, audience);

        card.getChildren().addAll(header, title, content, footer);
        return card;
    }

    // ========================================================================
    // CONTACTS TAB
    // ========================================================================

    private BorderPane createContactsContent() {
        BorderPane content = new BorderPane();
        content.setStyle("-fx-background-color: #F8FAFC;");

        // Left: Contact list
        VBox leftPane = new VBox(0);
        leftPane.setPrefWidth(350);
        leftPane.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 1 0 0;");

        // Search
        HBox searchBox = new HBox(12);
        searchBox.setPadding(new Insets(16));
        searchBox.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        TextField searchField = new TextField();
        searchField.setPromptText("Search contacts...");
        searchField.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-background-radius: 8;
            -fx-padding: 10 16;
            -fx-font-size: 13px;
            """);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchBox.getChildren().add(searchField);

        // Filter tabs
        HBox filterTabs = new HBox(0);
        filterTabs.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        String[] tabs = {"All", "Staff", "Parents", "Students"};
        for (int i = 0; i < tabs.length; i++) {
            Button tab = new Button(tabs[i]);
            boolean isFirst = i == 0;
            tab.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-font-size: 12px;
                -fx-font-weight: %s;
                -fx-padding: 12 20;
                -fx-background-radius: 0;
                -fx-border-color: transparent;
                -fx-cursor: hand;
                """,
                isFirst ? "#EFF6FF" : "transparent",
                isFirst ? "#2563EB" : "#64748B",
                isFirst ? "600" : "500"
            ));
            HBox.setHgrow(tab, Priority.ALWAYS);
            filterTabs.getChildren().add(tab);
        }

        // Contact list
        ListView<Contact> contactListView = new ListView<>(contacts);
        contactListView.setCellFactory(lv -> new ContactListCell());
        contactListView.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(contactListView, Priority.ALWAYS);

        leftPane.getChildren().addAll(searchBox, filterTabs, contactListView);

        // Right: Contact detail
        VBox detailPane = new VBox(24);
        detailPane.setPadding(new Insets(32));
        detailPane.setAlignment(Pos.TOP_CENTER);
        detailPane.setStyle("-fx-background-color: white;");

        // Placeholder
        Label placeholder = new Label("Select a contact to view details");
        placeholder.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");
        detailPane.getChildren().add(placeholder);

        content.setLeft(leftPane);
        content.setCenter(detailPane);

        // Update detail when contact selected
        contactListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            detailPane.getChildren().clear();
            if (newVal != null) {
                detailPane.getChildren().add(createContactDetail(newVal));
            } else {
                detailPane.getChildren().add(placeholder);
            }
        });

        return content;
    }

    private VBox createContactDetail(Contact contact) {
        VBox detail = new VBox(20);
        detail.setAlignment(Pos.TOP_CENTER);
        detail.setMaxWidth(400);

        // Avatar
        Label avatar = new Label(contact.getName().substring(0, 1).toUpperCase());
        avatar.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 32px;
            -fx-font-weight: 700;
            -fx-pref-width: 80;
            -fx-pref-height: 80;
            -fx-alignment: center;
            -fx-background-radius: 40;
            """);
        avatar.setMinSize(80, 80);
        avatar.setAlignment(Pos.CENTER);

        // Name
        Label name = new Label(contact.getName());
        name.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        // Role
        Label role = new Label(contact.getRole());
        role.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        // Action buttons
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER);

        Button emailBtn = new Button("✉ Send Email");
        emailBtn.setStyle("""
            -fx-background-color: #3B82F6;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-padding: 10 20;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);
        emailBtn.setOnAction(e -> {
            mainTabs.getSelectionModel().select(0);
            showComposePane(null);
            toField.setText(contact.getEmail());
        });

        Button phoneBtn = new Button("📞 Call");
        phoneBtn.setStyle("""
            -fx-background-color: #F1F5F9;
            -fx-text-fill: #374151;
            -fx-font-size: 13px;
            -fx-font-weight: 500;
            -fx-padding: 10 20;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            """);

        actions.getChildren().addAll(emailBtn, phoneBtn);

        // Contact info cards
        VBox infoCards = new VBox(12);
        infoCards.setFillWidth(true);

        infoCards.getChildren().addAll(
            createInfoCard("Email", "✉", contact.getEmail()),
            createInfoCard("Phone", "📞", contact.getPhone()),
            createInfoCard("Department", "🏫", contact.getDepartment())
        );

        detail.getChildren().addAll(avatar, name, role, actions, infoCards);
        return detail;
    }

    private HBox createInfoCard(String label, String icon, String value) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setStyle("""
            -fx-background-color: #F8FAFC;
            -fx-background-radius: 8;
            """);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");

        VBox textBox = new VBox(2);
        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

        Label valueText = new Label(value);
        valueText.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        textBox.getChildren().addAll(labelText, valueText);

        card.getChildren().addAll(iconLabel, textBox);
        return card;
    }

    // ========================================================================
    // FILTERING
    // ========================================================================

    private void filterMessages() {
        Folder folder = selectedFolder.get();
        filteredMessages.clear();
        filteredMessages.addAll(
            messages.stream()
                .filter(m -> matchesFolder(m, folder))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .toList()
        );
    }

    private boolean matchesFolder(Message message, Folder folder) {
        return switch (folder) {
            case INBOX -> message.getFolder() == Folder.INBOX && !message.isArchived() && !message.isDeleted();
            case SENT -> message.getFolder() == Folder.SENT;
            case DRAFTS -> message.getFolder() == Folder.DRAFTS;
            case STARRED -> message.isStarred();
            case ARCHIVED -> message.isArchived();
            case TRASH -> message.isDeleted();
        };
    }

    // ========================================================================
    // UTILITIES
    // ========================================================================

    private Label createTabIcon(String icon) {
        Label label = new Label(icon);
        label.setStyle("-fx-font-size: 14px;");
        return label;
    }

    private String formatTimestamp(LocalDateTime timestamp) {
        LocalDateTime now = LocalDateTime.now();
        if (timestamp.toLocalDate().equals(now.toLocalDate())) {
            return timestamp.format(DateTimeFormatter.ofPattern("h:mm a"));
        } else if (timestamp.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
            return "Yesterday";
        } else if (timestamp.isAfter(now.minusDays(7))) {
            return timestamp.format(DateTimeFormatter.ofPattern("EEEE"));
        } else {
            return timestamp.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        }
    }

    // ========================================================================
    // DEMO DATA
    // ========================================================================

    private void loadDemoData() {
        // Messages
        messages.addAll(
            createMessage("1", "Sarah Johnson", "sjohnson@school.edu",
                List.of("teacher@school.edu"),
                "Question about Math Assignment",
                "Hi,\n\nI wanted to reach out regarding the algebra assignment that was due last week. My son mentioned he had some difficulty with problem #5 and #6. Could we schedule a time to discuss strategies to help him improve?\n\nThank you for your time and dedication to our students.\n\nBest regards,\nSarah Johnson",
                LocalDateTime.now().minusHours(2), false, false),

            createMessage("2", "Principal Williams", "pwilliams@school.edu",
                List.of("all-staff@school.edu"),
                "Staff Meeting - Friday 3PM",
                "Dear Staff,\n\nThis is a reminder about our mandatory staff meeting this Friday at 3:00 PM in the main conference room.\n\nAgenda items include:\n- End of semester updates\n- Professional development opportunities\n- Budget planning for next year\n\nPlease come prepared to discuss any concerns or suggestions.\n\nThank you,\nPrincipal Williams",
                LocalDateTime.now().minusDays(1), true, false),

            createMessage("3", "IT Department", "it@school.edu",
                List.of("teacher@school.edu"),
                "System Maintenance Notice",
                "Dear Staff,\n\nPlease be advised that the student information system will undergo scheduled maintenance this weekend from Saturday 10PM to Sunday 6AM.\n\nDuring this time, the system will be unavailable. Please plan accordingly and ensure any grade submissions are completed before the maintenance window.\n\nWe apologize for any inconvenience.\n\nIT Department",
                LocalDateTime.now().minusDays(2), true, false),

            createMessage("4", "Maria Garcia", "mgarcia@school.edu",
                List.of("teacher@school.edu"),
                "Field Trip Permission Form",
                "Hello,\n\nI'm writing to let you know that we've submitted the permission form for Emma's upcoming field trip to the Science Museum. We're excited for her to have this learning opportunity!\n\nPlease let us know if you need any additional information.\n\nThank you,\nMaria Garcia",
                LocalDateTime.now().minusDays(3), true, false),

            createMessage("5", "Coach Thompson", "cthompson@school.edu",
                List.of("teacher@school.edu"),
                "Basketball Practice Schedule Change",
                "Hi Team,\n\nDue to the upcoming holiday, basketball practice will be moved from Tuesday to Wednesday next week. Same time, same place.\n\nMake sure to let your parents know about the schedule change!\n\nGo Tigers!\nCoach Thompson",
                LocalDateTime.now().minusDays(4), true, false)
        );

        // Add a sent message
        Message sent = createMessage("6", "Current User", "user@school.edu",
            List.of("sjohnson@school.edu"),
            "Re: Question about Math Assignment",
            "Hi Sarah,\n\nThank you for reaching out. I'd be happy to meet and discuss strategies to help your son.\n\nI have availability on Tuesday or Thursday after school. Please let me know which works better for you.\n\nBest regards",
            LocalDateTime.now().minusHours(1), true, false);
        sent.setFolder(Folder.SENT);
        messages.add(sent);

        // Contacts
        contacts.addAll(
            new Contact("Sarah Johnson", "Parent", "sjohnson@school.edu", "(555) 123-4567", "Parent - 8th Grade"),
            new Contact("Principal Williams", "Administrator", "pwilliams@school.edu", "(555) 987-6543", "Administration"),
            new Contact("Coach Thompson", "Teacher", "cthompson@school.edu", "(555) 456-7890", "Physical Education"),
            new Contact("Maria Garcia", "Parent", "mgarcia@school.edu", "(555) 234-5678", "Parent - 6th Grade"),
            new Contact("Dr. Emily Chen", "Counselor", "echen@school.edu", "(555) 345-6789", "Student Services"),
            new Contact("Mark Davis", "Teacher", "mdavis@school.edu", "(555) 567-8901", "Science Department"),
            new Contact("Lisa Brown", "Administrator", "lbrown@school.edu", "(555) 678-9012", "Vice Principal"),
            new Contact("IT Support", "Staff", "it@school.edu", "(555) 789-0123", "Technology")
        );

        // Announcements
        announcements.addAll(
            new Announcement("Winter Break Schedule", "General",
                "School will be closed from December 23rd through January 3rd for Winter Break. Classes will resume on January 4th.",
                "Principal Williams", LocalDateTime.now().minusDays(1), "All Students & Parents", true),

            new Announcement("Science Fair Registration Open", "Academic",
                "Registration for the annual Science Fair is now open! Students interested in participating should sign up with their science teacher by January 15th. This year's theme is 'Innovation for a Better Tomorrow'.",
                "Dr. Davis", LocalDateTime.now().minusDays(3), "Students", false),

            new Announcement("Basketball Game This Friday", "Athletics",
                "Come support our Tigers as they take on Lincoln High this Friday at 7PM in the main gymnasium. Admission is free for students with ID!",
                "Coach Thompson", LocalDateTime.now().minusDays(2), "Everyone", false),

            new Announcement("New Library Hours", "General",
                "Starting next week, the library will have extended hours: Monday-Thursday until 6PM, Friday until 4PM. Take advantage of the extra study time!",
                "Mrs. Anderson", LocalDateTime.now().minusDays(5), "Students & Staff", false)
        );
    }

    private Message createMessage(String id, String senderName, String senderEmail,
                                   List<String> recipients, String subject, String body,
                                   LocalDateTime timestamp, boolean read, boolean starred) {
        Message msg = new Message();
        msg.setId(id);
        msg.setSenderName(senderName);
        msg.setSenderEmail(senderEmail);
        msg.setRecipients(recipients);
        msg.setSubject(subject);
        msg.setBody(body);
        msg.setTimestamp(timestamp);
        msg.setRead(read);
        msg.setStarred(starred);
        msg.setFolder(Folder.INBOX);
        return msg;
    }

    // ========================================================================
    // CALLBACKS
    // ========================================================================

    public void setOnMessageSent(Consumer<Message> callback) {
        this.onMessageSent = callback;
    }

    public void setOnMessageDeleted(Consumer<Message> callback) {
        this.onMessageDeleted = callback;
    }

    // ========================================================================
    // LIST CELLS
    // ========================================================================

    private class MessageListCell extends ListCell<Message> {
        @Override
        protected void updateItem(Message item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            HBox cell = new HBox(12);
            cell.setAlignment(Pos.CENTER_LEFT);
            cell.setPadding(new Insets(12, 16, 12, 16));

            // Unread indicator
            Label unreadDot = new Label("●");
            unreadDot.setStyle("-fx-font-size: 8px; -fx-text-fill: " + (item.isRead() ? "transparent" : "#3B82F6") + ";");
            unreadDot.setMinWidth(12);

            // Star
            Label star = new Label(item.isStarred() ? "★" : "☆");
            star.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (item.isStarred() ? "#F59E0B" : "#CBD5E1") + "; -fx-cursor: hand;");
            star.setOnMouseClicked(e -> {
                item.setStarred(!item.isStarred());
                updateItem(item, false);
                e.consume();
            });

            // Content
            VBox content = new VBox(4);
            HBox.setHgrow(content, Priority.ALWAYS);

            HBox topRow = new HBox(8);
            Label sender = new Label(item.getSenderName());
            sender.setStyle("-fx-font-size: 13px; -fx-font-weight: " + (item.isRead() ? "500" : "700") + "; -fx-text-fill: #1E293B;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label time = new Label(formatTimestamp(item.getTimestamp()));
            time.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

            topRow.getChildren().addAll(sender, spacer, time);

            Label subject = new Label(item.getSubject());
            subject.setStyle("-fx-font-size: 12px; -fx-font-weight: " + (item.isRead() ? "400" : "600") + "; -fx-text-fill: #374151;");

            Label preview = new Label(item.getBody().replace("\n", " ").substring(0, Math.min(60, item.getBody().length())) + "...");
            preview.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");

            content.getChildren().addAll(topRow, subject, preview);

            cell.getChildren().addAll(unreadDot, star, content);

            // Hover effect
            cell.setStyle("-fx-background-color: " + (isSelected() ? "#EFF6FF" : "white") + "; -fx-background-radius: 0;");
            cell.setOnMouseEntered(e -> {
                if (!isSelected()) cell.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 0;");
            });
            cell.setOnMouseExited(e -> {
                if (!isSelected()) cell.setStyle("-fx-background-color: white; -fx-background-radius: 0;");
            });

            setGraphic(cell);
            setStyle("-fx-padding: 0; -fx-background-color: transparent;");
        }
    }

    private class ContactListCell extends ListCell<Contact> {
        @Override
        protected void updateItem(Contact item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            HBox cell = new HBox(12);
            cell.setAlignment(Pos.CENTER_LEFT);
            cell.setPadding(new Insets(12, 16, 12, 16));

            // Avatar
            Label avatar = new Label(item.getName().substring(0, 1).toUpperCase());
            avatar.setStyle("""
                -fx-background-color: #3B82F6;
                -fx-text-fill: white;
                -fx-font-size: 12px;
                -fx-font-weight: 700;
                -fx-pref-width: 36;
                -fx-pref-height: 36;
                -fx-alignment: center;
                -fx-background-radius: 18;
                """);
            avatar.setMinSize(36, 36);
            avatar.setAlignment(Pos.CENTER);

            // Info
            VBox info = new VBox(2);
            Label name = new Label(item.getName());
            name.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #1E293B;");

            Label role = new Label(item.getRole());
            role.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

            info.getChildren().addAll(name, role);

            cell.getChildren().addAll(avatar, info);

            // Hover effect
            cell.setStyle("-fx-background-color: " + (isSelected() ? "#EFF6FF" : "transparent") + ";");
            cell.setOnMouseEntered(e -> {
                if (!isSelected()) cell.setStyle("-fx-background-color: #F8FAFC;");
            });
            cell.setOnMouseExited(e -> {
                if (!isSelected()) cell.setStyle("-fx-background-color: transparent;");
            });

            setGraphic(cell);
            setStyle("-fx-padding: 0; -fx-background-color: transparent;");
        }
    }

    // ========================================================================
    // DATA CLASSES
    // ========================================================================

    @Getter @Setter
    public static class Message {
        private String id;
        private String senderName;
        private String senderEmail;
        private List<String> recipients;
        private String subject;
        private String body;
        private LocalDateTime timestamp;
        private boolean read;
        private boolean starred;
        private boolean archived;
        private boolean deleted;
        private Folder folder = Folder.INBOX;
        private List<String> attachments;
        private List<String> labels;
    }

    @Getter @Setter
    public static class Contact {
        private String name;
        private String role;
        private String email;
        private String phone;
        private String department;

        public Contact(String name, String role, String email, String phone, String department) {
            this.name = name;
            this.role = role;
            this.email = email;
            this.phone = phone;
            this.department = department;
        }
    }

    @Getter @Setter
    public static class Announcement {
        private String title;
        private String category;
        private String content;
        private String author;
        private LocalDateTime postedDate;
        private String audience;
        private boolean priority;

        public Announcement(String title, String category, String content,
                           String author, LocalDateTime postedDate, String audience, boolean priority) {
            this.title = title;
            this.category = category;
            this.content = content;
            this.author = author;
            this.postedDate = postedDate;
            this.audience = audience;
            this.priority = priority;
        }
    }

    @Getter
    public enum Folder {
        INBOX("📥", "Inbox"),
        SENT("📤", "Sent"),
        DRAFTS("📝", "Drafts"),
        STARRED("⭐", "Starred"),
        ARCHIVED("📦", "Archived"),
        TRASH("🗑", "Trash");

        private final String icon;
        private final String displayName;

        Folder(String icon, String displayName) {
            this.icon = icon;
            this.displayName = displayName;
        }
    }
}
