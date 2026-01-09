package com.heronix.ui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CommunicationCenterController {

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

    @FXML
    public void initialize() {
        setupMessageList();
        setupComposeTab();
        loadSampleData();
        loadAnnouncements();
        loadTemplates();
        updateStatistics();
        updateLastUpdated();
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
                            msg.isUnread() ? "● " : "",
                            msg.getSubject(),
                            msg.getFrom(),
                            msg.getPreview(),
                            msg.getTimestamp());

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

    private void loadSampleData() {
        LocalDateTime now = LocalDateTime.now();

        allMessages.addAll(
                new Message("M001", "Sarah Anderson", "Me", "Meeting Request: Emma's Progress",
                        "I would like to schedule a meeting to discuss Emma's recent test scores...",
                        "Could we meet next week to discuss Emma's progress in Algebra II? I'm concerned about her recent quiz scores.",
                        now.minusHours(2), true, false, false),

                new Message("M002", "Principal Martinez", "All Teachers", "Reminder: Staff Meeting Tomorrow",
                        "Don't forget our staff meeting tomorrow at 3:00 PM in the conference room...",
                        "This is a reminder about our monthly staff meeting tomorrow. We'll be discussing the upcoming parent-teacher conferences.",
                        now.minusHours(5), true, true, false),

                new Message("M003", "John Smith (Parent)", "Me", "Question about Homework Assignment",
                        "My son mentioned there was confusion about the homework for Chapter 5...",
                        "Hi, my son Liam mentioned there was some confusion in class today about the homework assignment for Chapter 5. Could you clarify?",
                        now.minusHours(8), true, false, false),

                new Message("M004", "Ms. Johnson (Chemistry)", "Science Department", "Lab Safety Reminder",
                        "Please remind students about proper lab safety procedures before tomorrow's experiment...",
                        "All science teachers, please take 5 minutes at the start of class tomorrow to review lab safety procedures.",
                        now.minusDays(1), false, false, false),

                new Message("M005", "Athletic Director", "All Coaches", "Updated Practice Schedule",
                        "Due to facility maintenance, practice schedules have been adjusted for next week...",
                        "The gym will be closed for floor maintenance next Tuesday and Wednesday. Please adjust practice schedules accordingly.",
                        now.minusDays(1), false, false, false),

                new Message("M006", "IT Department", "All Staff", "System Maintenance Saturday",
                        "Scheduled maintenance on Saturday night will affect email and gradebook systems...",
                        "Please be aware that we will be performing system upgrades on Saturday from 10 PM to 6 AM. Email and gradebook will be unavailable.",
                        now.minusDays(2), false, false, false),

                new Message("M007", "Emma Anderson (Student)", "Me", "Absent Tomorrow - Doctor Appointment",
                        "I have a doctor's appointment tomorrow and will miss Period 2...",
                        "Hi Ms. Rodriguez, I wanted to let you know I have a doctor's appointment tomorrow morning and will miss your class. Can I make up the quiz?",
                        now.minusDays(2), false, false, false),

                new Message("M008", "Counseling Office", "Homeroom Teachers", "Progress Report Deadline",
                        "Progress reports are due by Friday. Please complete all grade entries...",
                        "This is a reminder that progress reports are due this Friday. Please ensure all grades are entered in the system by Thursday evening.",
                        now.minusDays(3), true, true, false)
        );

        filterMessages("Inbox");
    }

    private void loadAnnouncements() {
        ObservableList<String> announcements = FXCollections.observableArrayList();
        announcements.add("📢 Winter Break Schedule\nSchool closes Dec 15 • Resumes Jan 6\nPosted: Dec 1, 2024");
        announcements.add("📢 Parent-Teacher Conferences\nDec 5 & 10 • 3:00 PM - 6:00 PM\nPosted: Nov 28, 2024");
        announcements.add("📢 Final Exams Schedule Released\nDec 13-15 • Check student portal\nPosted: Nov 25, 2024");
        announcements.add("📢 Holiday Concert\nDec 18 • 7:00 PM • Main Hall\nPosted: Nov 20, 2024");
        announcements.add("📢 Yearbook Photos - Retakes\nDec 9 • Morning only\nPosted: Nov 18, 2024");

        announcementsListView.setItems(announcements);
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

    private void loadTemplates() {
        ObservableList<String> templates = FXCollections.observableArrayList();
        templates.add("📄 Absence Excuse Request\nTemplate for requesting student absence documentation");
        templates.add("📄 Parent Meeting Request\nStandard template for scheduling parent meetings");
        templates.add("📄 Grade Concern Notice\nNotification for parents about low grades");
        templates.add("📄 Positive Behavior Recognition\nTemplate for praising student achievements");
        templates.add("📄 Homework Reminder\nReminder about missing homework assignments");
        templates.add("📄 Field Trip Permission\nField trip permission slip and details");

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

        // Mark as read
        if (msg.isUnread()) {
            msg.setUnread(false);
            updateStatistics();
            messageListView.refresh();
        }

        // Switch to preview tab
        rightPanelTabPane.getSelectionModel().select(0);

        statusLabel.setText("Viewing message: " + msg.getSubject());
    }

    private void updateStatistics() {
        long unread = allMessages.stream().filter(Message::isUnread).count();
        long drafts = allMessages.stream().filter(Message::isDraft).count();
        long sentToday = allMessages.stream()
                .filter(m -> m.isSent() && m.getTimestamp().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .count();

        unreadMessagesLabel.setText(String.valueOf(unread));
        draftMessagesLabel.setText(String.valueOf(drafts));
        sentTodayLabel.setText(String.valueOf(sentToday));
        activeAnnouncementsLabel.setText("5");
        pendingAlertsLabel.setText("2");

        totalMessagesLabel.setText("Total: " + allMessages.size() + " messages");
        storageUsedLabel.setText("Storage: 125 MB / 5 GB");
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
    private void handleInbox() {
        filterMessages("Inbox");
    }

    @FXML
    private void handleSent() {
        filterMessages("Sent");
    }

    @FXML
    private void handleDrafts() {
        filterMessages("Drafts");
    }

    @FXML
    private void handleStarred() {
        filterMessages("Starred");
    }

    @FXML
    private void handleTrash() {
        filterMessages("Trash");
    }

    @FXML
    private void handleFromParents() {
        folderTitleLabel.setText("From Parents");
        filteredMessages.clear();
        filteredMessages.addAll(allMessages.stream()
                .filter(m -> m.getFrom().contains("Parent") || m.getFrom().contains("Anderson") || m.getFrom().contains("Smith"))
                .collect(Collectors.toList()));
        updatePagination();
        statusLabel.setText("Viewing messages from parents");
    }

    @FXML
    private void handleFromTeachers() {
        folderTitleLabel.setText("From Teachers");
        filteredMessages.clear();
        filteredMessages.addAll(allMessages.stream()
                .filter(m -> m.getFrom().contains("Ms.") || m.getFrom().contains("Mr."))
                .collect(Collectors.toList()));
        updatePagination();
        statusLabel.setText("Viewing messages from teachers");
    }

    @FXML
    private void handleFromStaff() {
        folderTitleLabel.setText("From Staff");
        filteredMessages.clear();
        filteredMessages.addAll(allMessages.stream()
                .filter(m -> m.getFrom().contains("Principal") || m.getFrom().contains("Department") ||
                        m.getFrom().contains("Office") || m.getFrom().contains("Director"))
                .collect(Collectors.toList()));
        updatePagination();
        statusLabel.setText("Viewing messages from staff");
    }

    @FXML
    private void handleFromStudents() {
        folderTitleLabel.setText("From Students");
        filteredMessages.clear();
        filteredMessages.addAll(allMessages.stream()
                .filter(m -> m.getFrom().contains("Student") || m.getFrom().contains("Emma"))
                .collect(Collectors.toList()));
        updatePagination();
        statusLabel.setText("Viewing messages from students");
    }

    // Event Handlers - Actions

    @FXML
    private void handleSearch() {
        String query = messageSearchField.getText().toLowerCase();
        if (query.isEmpty()) {
            filterMessages(currentFolder);
            return;
        }

        filteredMessages.clear();
        filteredMessages.addAll(allMessages.stream()
                .filter(m -> m.getSubject().toLowerCase().contains(query) ||
                        m.getFrom().toLowerCase().contains(query) ||
                        m.getBody().toLowerCase().contains(query))
                .collect(Collectors.toList()));
        updatePagination();
        statusLabel.setText("Search results: " + filteredMessages.size() + " message(s)");
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

        clearComposeForm();
        statusLabel.setText("Message sent to " + recipient);
        showAlert("Message Sent", "Your message has been sent successfully");
    }

    @FXML
    private void handleCreateAnnouncement() {
        statusLabel.setText("Opening announcement builder...");
        showAlert("Create Announcement",
                "Create school-wide announcement:\n" +
                        "• Choose audience (All/Students/Parents/Staff)\n" +
                        "• Set priority level\n" +
                        "• Schedule posting time\n" +
                        "• Add attachments");
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
        updateStatistics();
        updateLastUpdated();
        statusLabel.setText("Messages refreshed");
    }

    @FXML
    private void handleSettings() {
        statusLabel.setText("Opening communication settings...");
        showAlert("Communication Settings",
                "Configure:\n" +
                        "• Email signature\n" +
                        "• Auto-reply messages\n" +
                        "• Notification preferences\n" +
                        "• Message templates\n" +
                        "• Folder organization");
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
            this.sent = false;
            this.draft = false;
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
