package com.heronix.ui.controller;

import com.heronix.service.TalkApiService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CommunicationCenterController {

    @Autowired(required = false)
    private TalkApiService talkApiService;

    // Header Stats
    @FXML private Label unreadMessagesLabel;
    @FXML private Label draftMessagesLabel;
    @FXML private Label sentTodayLabel;
    @FXML private Label activeAnnouncementsLabel;
    @FXML private Label pendingAlertsLabel;

    // Left Panel Stats
    @FXML private Label totalMessagesLabel;
    @FXML private Label storageUsedLabel;

    // Message List
    @FXML private Label folderTitleLabel;
    @FXML private TextField messageSearchField;
    @FXML private CheckBox selectAllCheckBox;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private ListView<Message> messageListView;
    @FXML private Label paginationLabel;

    // Message Preview
    @FXML private TabPane rightPanelTabPane;
    @FXML private Label previewSubjectLabel;
    @FXML private Label previewFromLabel;
    @FXML private Label previewToLabel;
    @FXML private Label previewDateLabel;
    @FXML private TextArea previewBodyArea;

    // Compose
    @FXML private ComboBox<String> recipientTypeComboBox;
    @FXML private TextField recipientField;
    @FXML private TextField subjectField;
    @FXML private TextArea composeBodyArea;
    @FXML private CheckBox highPriorityCheckBox;
    @FXML private CheckBox requestReadReceiptCheckBox;
    @FXML private CheckBox sendCopyToMeCheckBox;

    // Announcements
    @FXML private ListView<String> announcementsListView;

    // Templates
    @FXML private ListView<String> templatesListView;

    // Footer
    @FXML private Label statusLabel;
    @FXML private Label lastUpdatedLabel;

    // Data
    private ObservableList<Message> allMessages = FXCollections.observableArrayList();
    private ObservableList<Message> filteredMessages = FXCollections.observableArrayList();
    private Message selectedMessage = null;
    private String currentFolder = "Inbox";
    private List<Map<String, Object>> channels = new ArrayList<>();

    @FXML
    public void initialize() {
        setupMessageList();
        setupComposeTab();
        loadTemplates();
        updateLastUpdated();

        // Connect to Talk server and load messages in background
        connectAndLoadMessages();
    }

    private void connectAndLoadMessages() {
        if (talkApiService == null) {
            statusLabel.setText("Talk service not available. Showing offline data.");
            loadOfflineData();
            loadOfflineAnnouncements();
            updateStatistics();
            return;
        }

        statusLabel.setText("Connecting to Talk server...");

        Thread thread = new Thread(() -> {
            boolean connected = talkApiService != null && talkApiService.isConnected() || talkApiService.login("admin", "admin123");

            Platform.runLater(() -> {
                if (connected) {
                    statusLabel.setText("Connected to Talk server. Loading messages...");
                    loadMessagesFromServer();
                    loadAnnouncementsFromServer();
                } else {
                    statusLabel.setText("Could not connect to Talk server. Showing offline data.");
                    loadOfflineData();
                    loadOfflineAnnouncements();
                }
                updateStatistics();
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void loadMessagesFromServer() {
        Thread thread = new Thread(() -> {
            try {
                channels = talkApiService.getChannels();
                List<Map<String, Object>> serverMessages = talkApiService.getAllMessages();

                List<Message> messages = new ArrayList<>();
                for (Map<String, Object> msg : serverMessages) {
                    messages.add(convertServerMessage(msg));
                }

                Platform.runLater(() -> {
                    allMessages.clear();
                    allMessages.addAll(messages);
                    filterMessages("Inbox");
                    updateStatistics();
                    statusLabel.setText("Loaded " + messages.size() + " message(s) from Talk server");
                    updateLastUpdated();
                });
            } catch (Exception e) {
                log.error("Error loading messages from server", e);
                Platform.runLater(() -> {
                    statusLabel.setText("Error loading messages: " + e.getMessage());
                    loadOfflineData();
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private Message convertServerMessage(Map<String, Object> msg) {
        String id = String.valueOf(msg.getOrDefault("id", "0"));
        String senderName = (String) msg.getOrDefault("senderName", "Unknown");
        String channelName = (String) msg.getOrDefault("channelName", "");
        String content = (String) msg.getOrDefault("content", "");
        String timestamp = (String) msg.getOrDefault("timestamp", "");
        boolean pinned = Boolean.TRUE.equals(msg.get("pinned"));
        boolean important = Boolean.TRUE.equals(msg.get("important"));

        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(timestamp);
        } catch (Exception e) {
            dateTime = LocalDateTime.now();
        }

        String subject = channelName;
        String body = content;
        if (content.startsWith("**") && content.contains("**\n")) {
            int end = content.indexOf("**\n");
            subject = content.substring(2, end);
            body = content.substring(end + 3).trim();
        }

        String preview = body.length() > 80 ? body.substring(0, 80) + "..." : body;

        Long senderId = msg.get("senderId") != null ? ((Number) msg.get("senderId")).longValue() : null;
        boolean isSent = senderId != null && senderId.equals(talkApiService.getUserId());

        return new Message(id, senderName, channelName, subject, preview, body,
                dateTime, !isSent, pinned || important, false, isSent, false);
    }

    private void loadAnnouncementsFromServer() {
        Thread thread = new Thread(() -> {
            try {
                List<Map<String, Object>> news = talkApiService.getNewsItems();
                List<Map<String, Object>> alerts = talkApiService.getAlerts();

                Platform.runLater(() -> {
                    ObservableList<String> announcements = FXCollections.observableArrayList();

                    for (Map<String, Object> alert : alerts) {
                        String title = (String) alert.getOrDefault("title", "Alert");
                        String message = (String) alert.getOrDefault("message", "");
                        String level = (String) alert.getOrDefault("alertLevel", "NORMAL");
                        announcements.add("[" + level + "] " + title + "\n" + message);
                    }

                    for (Map<String, Object> item : news) {
                        String headline = (String) item.getOrDefault("headline", "News");
                        String itemContent = (String) item.getOrDefault("content", "");
                        announcements.add(headline + "\n" + itemContent);
                    }

                    if (announcements.isEmpty()) {
                        announcements.add("No announcements at this time");
                    }

                    announcementsListView.setItems(announcements);
                    setupAnnouncementsCellFactory();

                    activeAnnouncementsLabel.setText(String.valueOf(news.size()));
                    pendingAlertsLabel.setText(String.valueOf(alerts.size()));
                });
            } catch (Exception e) {
                log.debug("Could not load announcements from server", e);
                Platform.runLater(this::loadOfflineAnnouncements);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void loadOfflineData() {
        LocalDateTime now = LocalDateTime.now();
        allMessages.addAll(
                new Message("M001", "System", "All", "Welcome to Heronix Communication Center",
                        "The Communication Center is not connected to the Talk server...",
                        "The Communication Center could not connect to the Talk server at this time. " +
                                "Please ensure the Talk server is running on port 9680.",
                        now, true, false, false, false, false)
        );
        filterMessages("Inbox");
    }

    private void loadOfflineAnnouncements() {
        ObservableList<String> announcements = FXCollections.observableArrayList();
        announcements.add("Talk server is offline\nCannot load announcements at this time");
        announcementsListView.setItems(announcements);
        setupAnnouncementsCellFactory();
    }

    private void setupAnnouncementsCellFactory() {
        announcementsListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-padding: 12; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; " +
                            "-fx-background-color: #fff3e0;");
                }
            }
        });
    }

    private void setupMessageList() {
        messageListView.setItems(filteredMessages);
        messageListView.setCellFactory(lv -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    String text = String.format("%s%s\nFrom: %s\n%s\n%s",
                            msg.isUnread() ? "* " : "",
                            msg.getSubject(),
                            msg.getFrom(),
                            msg.getPreview(),
                            msg.getTimestamp().format(DateTimeFormatter.ofPattern("MMM dd, h:mm a")));

                    setText(text);

                    String style = "-fx-padding: 12; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;";
                    if (msg.isUnread()) {
                        style += " -fx-background-color: #e3f2fd; -fx-font-weight: bold;";
                    }
                    if (msg.isPriority()) {
                        style += " -fx-border-left-color: #f44336; -fx-border-left-width: 4;";
                    }
                    setStyle(style);
                }
            }
        });

        messageListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedMessage = newVal;
                displayMessage(newVal);
            }
        });

        sortComboBox.getSelectionModel().selectFirst();
        sortComboBox.setOnAction(e -> sortMessages());
    }

    private void setupComposeTab() {
        recipientTypeComboBox.getSelectionModel().selectFirst();
    }

    private void loadTemplates() {
        ObservableList<String> templates = FXCollections.observableArrayList();
        templates.add("Absence Excuse Request\nTemplate for requesting student absence documentation");
        templates.add("Parent Meeting Request\nStandard template for scheduling parent meetings");
        templates.add("Grade Concern Notice\nNotification for parents about low grades");
        templates.add("Positive Behavior Recognition\nTemplate for praising student achievements");
        templates.add("Homework Reminder\nReminder about missing homework assignments");
        templates.add("Field Trip Permission\nField trip permission slip and details");

        templatesListView.setItems(templates);
        templatesListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-padding: 12; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
                }
            }
        });
    }

    private void filterMessages(String folder) {
        currentFolder = folder;
        folderTitleLabel.setText(folder);

        filteredMessages.clear();

        switch (folder) {
            case "Inbox":
                filteredMessages.addAll(allMessages.stream()
                        .filter(m -> !m.isSent() && !m.isDraft())
                        .collect(Collectors.toList()));
                break;
            case "Sent":
                filteredMessages.addAll(allMessages.stream()
                        .filter(Message::isSent)
                        .collect(Collectors.toList()));
                break;
            case "Drafts":
                filteredMessages.addAll(allMessages.stream()
                        .filter(Message::isDraft)
                        .collect(Collectors.toList()));
                break;
            case "Starred":
                filteredMessages.addAll(allMessages.stream()
                        .filter(Message::isStarred)
                        .collect(Collectors.toList()));
                break;
            default:
                filteredMessages.addAll(allMessages);
                break;
        }

        updatePagination();
        statusLabel.setText("Viewing " + folder + " - " + filteredMessages.size() + " message(s)");
    }

    private void sortMessages() {
        String sort = sortComboBox.getSelectionModel().getSelectedItem();
        if (sort == null) return;

        switch (sort) {
            case "Sort: Newest First":
                filteredMessages.sort((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()));
                break;
            case "Sort: Oldest First":
                filteredMessages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
                break;
            case "Sort: Sender A-Z":
                filteredMessages.sort((m1, m2) -> m1.getFrom().compareTo(m2.getFrom()));
                break;
            case "Sort: Unread First":
                filteredMessages.sort((m1, m2) -> Boolean.compare(m2.isUnread(), m1.isUnread()));
                break;
        }

        messageListView.refresh();
    }

    private void displayMessage(Message msg) {
        previewSubjectLabel.setText(msg.getSubject());
        previewFromLabel.setText(msg.getFrom());
        previewToLabel.setText(msg.getTo());
        previewDateLabel.setText(msg.getTimestamp().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a")));
        previewBodyArea.setText(msg.getBody());

        if (msg.isUnread()) {
            msg.setUnread(false);
            updateStatistics();
            messageListView.refresh();
        }

        rightPanelTabPane.getSelectionModel().select(0);
        statusLabel.setText("Viewing message: " + msg.getSubject());
    }

    private void updateStatistics() {
        long unread = allMessages.stream().filter(Message::isUnread).count();
        long drafts = allMessages.stream().filter(Message::isDraft).count();
        long sentCount = allMessages.stream().filter(Message::isSent).count();

        unreadMessagesLabel.setText(String.valueOf(unread));
        draftMessagesLabel.setText(String.valueOf(drafts));
        sentTodayLabel.setText(String.valueOf(sentCount));

        totalMessagesLabel.setText("Total: " + allMessages.size() + " messages");
        storageUsedLabel.setText(talkApiService != null && talkApiService.isConnected() ? "Connected to Talk" : "Offline");
    }

    private void updatePagination() {
        paginationLabel.setText("1-" + Math.min(20, filteredMessages.size()) + " of " + filteredMessages.size());
    }

    private void updateLastUpdated() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd 'at' h:mm a"));
        lastUpdatedLabel.setText("Last updated: " + timestamp);
    }

    // Event Handlers - Folders

    @FXML
    private void handleInbox() { filterMessages("Inbox"); }

    @FXML
    private void handleSent() { filterMessages("Sent"); }

    @FXML
    private void handleDrafts() { filterMessages("Drafts"); }

    @FXML
    private void handleStarred() { filterMessages("Starred"); }

    @FXML
    private void handleTrash() { filterMessages("Trash"); }

    @FXML
    private void handleFromParents() {
        folderTitleLabel.setText("From Parents");
        filteredMessages.clear();
        filteredMessages.addAll(allMessages.stream()
                .filter(m -> m.getFrom().toLowerCase().contains("parent") ||
                        m.getTo().toLowerCase().contains("parent"))
                .collect(Collectors.toList()));
        updatePagination();
        statusLabel.setText("Viewing messages from parents");
    }

    @FXML
    private void handleFromTeachers() {
        folderTitleLabel.setText("From Teachers");
        filteredMessages.clear();
        filteredMessages.addAll(allMessages.stream()
                .filter(m -> m.getFrom().toLowerCase().contains("teacher") ||
                        m.getFrom().toLowerCase().contains("ms.") ||
                        m.getFrom().toLowerCase().contains("mr."))
                .collect(Collectors.toList()));
        updatePagination();
        statusLabel.setText("Viewing messages from teachers");
    }

    @FXML
    private void handleFromStaff() {
        folderTitleLabel.setText("From Staff");
        filteredMessages.clear();
        filteredMessages.addAll(allMessages.stream()
                .filter(m -> m.getFrom().toLowerCase().contains("admin") ||
                        m.getFrom().toLowerCase().contains("principal") ||
                        m.getFrom().toLowerCase().contains("department"))
                .collect(Collectors.toList()));
        updatePagination();
        statusLabel.setText("Viewing messages from staff");
    }

    @FXML
    private void handleFromStudents() {
        folderTitleLabel.setText("From Students");
        filteredMessages.clear();
        filteredMessages.addAll(allMessages.stream()
                .filter(m -> m.getFrom().toLowerCase().contains("student"))
                .collect(Collectors.toList()));
        updatePagination();
        statusLabel.setText("Viewing messages from students");
    }

    // Event Handlers - Actions

    @FXML
    private void handleSearch() {
        String query = messageSearchField.getText().trim();
        if (query.isEmpty()) {
            filterMessages(currentFolder);
            return;
        }

        if (talkApiService != null && talkApiService.isConnected()) {
            statusLabel.setText("Searching Talk server...");
            Thread thread = new Thread(() -> {
                List<Map<String, Object>> results = talkApiService.searchMessages(query);
                List<Message> messages = results.stream()
                        .map(this::convertServerMessage)
                        .collect(Collectors.toList());

                Platform.runLater(() -> {
                    filteredMessages.clear();
                    filteredMessages.addAll(messages);
                    updatePagination();
                    statusLabel.setText("Search results: " + filteredMessages.size() + " message(s)");
                });
            });
            thread.setDaemon(true);
            thread.start();
        } else {
            String q = query.toLowerCase();
            filteredMessages.clear();
            filteredMessages.addAll(allMessages.stream()
                    .filter(m -> m.getSubject().toLowerCase().contains(q) ||
                            m.getFrom().toLowerCase().contains(q) ||
                            m.getBody().toLowerCase().contains(q))
                    .collect(Collectors.toList()));
            updatePagination();
            statusLabel.setText("Search results: " + filteredMessages.size() + " message(s)");
        }
    }

    @FXML
    private void handleDeleteMessages() {
        statusLabel.setText("Deleting selected messages...");
        showAlert("Delete Messages", "Selected messages will be moved to trash");
    }

    @FXML
    private void handleMarkRead() {
        filteredMessages.forEach(m -> m.setUnread(false));
        updateStatistics();
        messageListView.refresh();
        statusLabel.setText("All messages marked as read");
    }

    @FXML
    private void handleStarMessages() {
        statusLabel.setText("Starring selected messages...");
        showAlert("Star Messages", "Selected messages will be starred");
    }

    @FXML
    private void handlePreviousPage() {
        statusLabel.setText("Loading previous page...");
    }

    @FXML
    private void handleNextPage() {
        statusLabel.setText("Loading next page...");
    }

    @FXML
    private void handleReply() {
        if (selectedMessage != null) {
            rightPanelTabPane.getSelectionModel().select(1);
            recipientField.setText(selectedMessage.getFrom());
            subjectField.setText("Re: " + selectedMessage.getSubject());
            composeBodyArea.requestFocus();
            statusLabel.setText("Replying to: " + selectedMessage.getFrom());
        }
    }

    @FXML
    private void handleReplyAll() {
        if (selectedMessage != null) {
            handleReply();
            statusLabel.setText("Replying to all recipients");
        }
    }

    @FXML
    private void handleForward() {
        if (selectedMessage != null) {
            rightPanelTabPane.getSelectionModel().select(1);
            subjectField.setText("Fwd: " + selectedMessage.getSubject());
            composeBodyArea.setText("\n\n---------- Forwarded message ----------\n" +
                    "From: " + selectedMessage.getFrom() + "\n" +
                    "Subject: " + selectedMessage.getSubject() + "\n\n" +
                    selectedMessage.getBody());
            statusLabel.setText("Forwarding message");
        }
    }

    @FXML
    private void handleDeleteCurrent() {
        if (selectedMessage != null) {
            statusLabel.setText("Deleting message: " + selectedMessage.getSubject());
            showAlert("Delete Message", "Message will be moved to trash");
        }
    }

    @FXML
    private void handleComposeMessage() {
        rightPanelTabPane.getSelectionModel().select(1);
        clearComposeForm();
        statusLabel.setText("Composing new message");
    }

    @FXML
    private void handleNewAnnouncement() {
        rightPanelTabPane.getSelectionModel().select(2);
        statusLabel.setText("Creating new announcement");
    }

    @FXML
    private void handleAttachFile() {
        statusLabel.setText("Opening file picker...");
        showAlert("Attach File", "Select files to attach to this message");
    }

    @FXML
    private void handleSaveDraft() {
        String subject = subjectField.getText();
        if (subject.isEmpty()) {
            showAlert("No Subject", "Please enter a subject before saving draft");
            return;
        }
        statusLabel.setText("Draft saved: " + subject);
        showAlert("Draft Saved", "Your message has been saved to drafts");
    }

    @FXML
    private void handleSendMessage() {
        String recipient = recipientField.getText();
        String subject = subjectField.getText();
        String body = composeBodyArea.getText();

        if (recipient.isEmpty()) {
            showAlert("No Recipient", "Please enter at least one recipient");
            return;
        }
        if (subject.isEmpty()) {
            showAlert("No Subject", "Please enter a subject");
            return;
        }

        if (talkApiService == null || !talkApiService.isConnected()) {
            showAlert("Not Connected", "Cannot send message - Talk server is not connected");
            return;
        }

        statusLabel.setText("Sending message...");

        Thread thread = new Thread(() -> {
            String content = "**" + subject + "**\n\n" + body;
            Long targetChannel = 1L; // General channel default

            // Try to find a matching channel by name
            for (Map<String, Object> ch : channels) {
                String name = (String) ch.getOrDefault("name", "");
                if (name.toLowerCase().contains(recipient.toLowerCase())) {
                    targetChannel = ((Number) ch.get("id")).longValue();
                    break;
                }
            }

            Map<String, Object> result = talkApiService.sendMessage(targetChannel, content);

            Platform.runLater(() -> {
                if (result != null) {
                    clearComposeForm();
                    statusLabel.setText("Message sent successfully to " + recipient);
                    showAlert("Message Sent", "Your message has been sent via Talk server");
                    loadMessagesFromServer();
                } else {
                    statusLabel.setText("Failed to send message");
                    showAlert("Send Failed", "Could not send message. Please try again.");
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleCreateAnnouncement() {
        statusLabel.setText("Opening announcement builder...");
        showAlert("Create Announcement",
                "Create school-wide announcement:\n" +
                        "- Choose audience (All/Students/Parents/Staff)\n" +
                        "- Set priority level\n" +
                        "- Schedule posting time\n" +
                        "- Add attachments");
    }

    @FXML
    private void handleUseTemplate() {
        String selected = templatesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            rightPanelTabPane.getSelectionModel().select(1);
            statusLabel.setText("Loading template: " + selected);
            showAlert("Template Loaded", "Template has been loaded into compose form");
        }
    }

    @FXML
    private void handleRefresh() {
        if (talkApiService != null && talkApiService.isConnected()) {
            loadMessagesFromServer();
            loadAnnouncementsFromServer();
        } else {
            connectAndLoadMessages();
        }
        updateLastUpdated();
        statusLabel.setText("Refreshing messages...");
    }

    @FXML
    private void handleSettings() {
        statusLabel.setText("Opening communication settings...");
        showAlert("Communication Settings",
                "Configure:\n" +
                        "- Email signature\n" +
                        "- Auto-reply messages\n" +
                        "- Notification preferences\n" +
                        "- Message templates\n" +
                        "- Folder organization");
    }

    private void clearComposeForm() {
        recipientField.clear();
        subjectField.clear();
        composeBodyArea.clear();
        highPriorityCheckBox.setSelected(false);
        requestReadReceiptCheckBox.setSelected(false);
        sendCopyToMeCheckBox.setSelected(false);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Data Classes

    public static class Message {
        private String id;
        private String from;
        private String to;
        private String subject;
        private String preview;
        private String body;
        private LocalDateTime timestamp;
        private boolean unread;
        private boolean priority;
        private boolean starred;
        private boolean sent;
        private boolean draft;

        public Message(String id, String from, String to, String subject, String preview, String body,
                       LocalDateTime timestamp, boolean unread, boolean priority, boolean starred) {
            this(id, from, to, subject, preview, body, timestamp, unread, priority, starred, false, false);
        }

        public Message(String id, String from, String to, String subject, String preview, String body,
                       LocalDateTime timestamp, boolean unread, boolean priority, boolean starred,
                       boolean sent, boolean draft) {
            this.id = id;
            this.from = from;
            this.to = to;
            this.subject = subject;
            this.preview = preview;
            this.body = body;
            this.timestamp = timestamp;
            this.unread = unread;
            this.priority = priority;
            this.starred = starred;
            this.sent = sent;
            this.draft = draft;
        }

        public String getId() { return id; }
        public String getFrom() { return from; }
        public String getTo() { return to; }
        public String getSubject() { return subject; }
        public String getPreview() { return preview; }
        public String getBody() { return body; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isUnread() { return unread; }
        public void setUnread(boolean unread) { this.unread = unread; }
        public boolean isPriority() { return priority; }
        public boolean isStarred() { return starred; }
        public boolean isSent() { return sent; }
        public boolean isDraft() { return draft; }
    }
}
