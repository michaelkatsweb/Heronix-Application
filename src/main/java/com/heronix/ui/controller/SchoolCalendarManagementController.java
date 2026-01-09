package com.heronix.ui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SchoolCalendarManagementController {

    // Header Components
    @FXML private Label currentMonthLabel;
    @FXML private Label eventsThisMonthLabel;
    @FXML private Label schoolDaysLabel;
    @FXML private Label holidaysLabel;

    // View Controls
    @FXML private ToggleButton monthViewButton;
    @FXML private ToggleButton weekViewButton;
    @FXML private ToggleButton dayViewButton;
    @FXML private ToggleButton agendaViewButton;
    @FXML private ComboBox<String> eventFilterComboBox;

    // Calendar Grid
    @FXML private GridPane calendarHeaderGrid;
    @FXML private GridPane calendarDaysGrid;

    // Event Details
    @FXML private Label selectedDateLabel;
    @FXML private Label selectedDateEventsLabel;
    @FXML private ListView<String> dateEventsListView;

    // Quick Add Event
    @FXML private TextField quickEventTitleField;
    @FXML private DatePicker quickEventDatePicker;
    @FXML private ComboBox<String> quickEventCategoryComboBox;

    // Academic Year
    @FXML private ComboBox<String> academicYearComboBox;
    @FXML private Label totalSchoolDaysLabel;
    @FXML private Label daysElapsedLabel;

    // Footer
    @FXML private Label statusLabel;
    @FXML private Label lastUpdatedLabel;

    // Data
    private YearMonth currentMonth;
    private LocalDate selectedDate;
    private Map<LocalDate, List<CalendarEvent>> events = new HashMap<>();
    private Map<String, String> eventColors = new HashMap<>();

    @FXML
    public void initialize() {
        setupViewToggle();
        setupEventColors();
        setupFilters();
        currentMonth = YearMonth.now();
        loadSampleEvents();
        renderCalendar();
        updateStatistics();
        updateLastUpdated();
    }

    private void setupViewToggle() {
        ToggleGroup viewGroup = new ToggleGroup();
        monthViewButton.setToggleGroup(viewGroup);
        weekViewButton.setToggleGroup(viewGroup);
        dayViewButton.setToggleGroup(viewGroup);
        agendaViewButton.setToggleGroup(viewGroup);
        monthViewButton.setSelected(true);

        viewGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                renderCalendar();
            }
        });
    }

    private void setupEventColors() {
        eventColors.put("Academic", "#2196f3");
        eventColors.put("Holiday", "#f44336");
        eventColors.put("Sports", "#4caf50");
        eventColors.put("Activities", "#ff9800");
        eventColors.put("Testing", "#673ab7");
        eventColors.put("Parent Events", "#00bcd4");
    }

    private void setupFilters() {
        eventFilterComboBox.getSelectionModel().selectFirst();
        eventFilterComboBox.setOnAction(e -> renderCalendar());
        academicYearComboBox.getSelectionModel().selectFirst();
    }

    private void loadSampleEvents() {
        // December 2024 events
        LocalDate dec2024 = LocalDate.of(2024, 12, 1);

        addEvent(dec2024.withDayOfMonth(2), "First Day of December", "Academic");
        addEvent(dec2024.withDayOfMonth(5), "Parent-Teacher Conferences", "Parent Events");
        addEvent(dec2024.withDayOfMonth(6), "Basketball Game vs. Lincoln HS", "Sports");
        addEvent(dec2024.withDayOfMonth(10), "Parent-Teacher Conferences", "Parent Events");
        addEvent(dec2024.withDayOfMonth(12), "Drama Club Performance", "Activities");
        addEvent(dec2024.withDayOfMonth(13), "Final Exams Begin", "Testing");
        addEvent(dec2024.withDayOfMonth(13), "Soccer Practice", "Sports");
        addEvent(dec2024.withDayOfMonth(15), "Last Day Before Break", "Academic");
        addEvent(dec2024.withDayOfMonth(16), "Winter Break Begins", "Holiday");
        addEvent(dec2024.withDayOfMonth(17), "Winter Break", "Holiday");
        addEvent(dec2024.withDayOfMonth(18), "Winter Concert", "Activities");
        addEvent(dec2024.withDayOfMonth(18), "Winter Break", "Holiday");
        addEvent(dec2024.withDayOfMonth(19), "Winter Break", "Holiday");
        addEvent(dec2024.withDayOfMonth(20), "Winter Break", "Holiday");
        addEvent(dec2024.withDayOfMonth(23), "Winter Break", "Holiday");
        addEvent(dec2024.withDayOfMonth(24), "Christmas Eve - School Closed", "Holiday");
        addEvent(dec2024.withDayOfMonth(25), "Christmas Day - School Closed", "Holiday");
        addEvent(dec2024.withDayOfMonth(26), "Winter Break", "Holiday");
        addEvent(dec2024.withDayOfMonth(27), "Winter Break", "Holiday");
        addEvent(dec2024.withDayOfMonth(30), "Winter Break", "Holiday");
        addEvent(dec2024.withDayOfMonth(31), "New Year's Eve - School Closed", "Holiday");

        // January 2025 events
        LocalDate jan2025 = LocalDate.of(2025, 1, 1);
        addEvent(jan2025.withDayOfMonth(1), "New Year's Day - School Closed", "Holiday");
        addEvent(jan2025.withDayOfMonth(6), "School Resumes", "Academic");
        addEvent(jan2025.withDayOfMonth(10), "Science Fair", "Activities");
        addEvent(jan2025.withDayOfMonth(15), "Midterm Exams Begin", "Testing");
        addEvent(jan2025.withDayOfMonth(20), "Martin Luther King Jr. Day - No School", "Holiday");
    }

    private void addEvent(LocalDate date, String title, String category) {
        events.putIfAbsent(date, new ArrayList<>());
        events.get(date).add(new CalendarEvent(title, category, date, "All Day"));
    }

    private void renderCalendar() {
        calendarDaysGrid.getChildren().clear();

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int daysInMonth = currentMonth.lengthOfMonth();
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0

        currentMonthLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        // Get filtered events
        String filter = eventFilterComboBox.getSelectionModel().getSelectedItem();
        Map<LocalDate, List<CalendarEvent>> filteredEvents = filterEvents(filter);

        int row = 0;
        int col = dayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            VBox dayCell = createDayCell(date, filteredEvents.get(date));

            calendarDaysGrid.add(dayCell, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }

        updateStatistics();
    }

    private VBox createDayCell(LocalDate date, List<CalendarEvent> dayEvents) {
        VBox cell = new VBox(3);
        cell.setAlignment(Pos.TOP_LEFT);
        cell.setStyle("-fx-padding: 5; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 1 0; -fx-cursor: hand;");
        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        GridPane.setHgrow(cell, Priority.ALWAYS);
        GridPane.setVgrow(cell, Priority.ALWAYS);

        // Day number
        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        String dayStyle = "-fx-font-weight: bold; -fx-font-size: 14px;";

        // Highlight today
        if (date.equals(LocalDate.now())) {
            dayStyle += " -fx-text-fill: white; -fx-background-color: #2196f3; -fx-padding: 3 6; -fx-background-radius: 15;";
        } else if (date.equals(selectedDate)) {
            dayStyle += " -fx-text-fill: #2196f3;";
            cell.setStyle(cell.getStyle() + " -fx-background-color: #e3f2fd;");
        } else if (date.getMonth() != currentMonth.getMonth()) {
            dayStyle += " -fx-text-fill: #bdbdbd;";
        }

        dayLabel.setStyle(dayStyle);
        cell.getChildren().add(dayLabel);

        // Add event indicators
        if (dayEvents != null && !dayEvents.isEmpty()) {
            int maxEvents = Math.min(dayEvents.size(), 3);
            for (int i = 0; i < maxEvents; i++) {
                CalendarEvent event = dayEvents.get(i);
                Label eventLabel = new Label(event.getTitle());
                String color = eventColors.getOrDefault(event.getCategory(), "#757575");
                eventLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: white; -fx-background-color: " + color +
                        "; -fx-padding: 2 4; -fx-background-radius: 3; -fx-max-width: infinity;");
                eventLabel.setMaxWidth(Double.MAX_VALUE);
                cell.getChildren().add(eventLabel);
            }

            if (dayEvents.size() > 3) {
                Label moreLabel = new Label("+" + (dayEvents.size() - 3) + " more");
                moreLabel.setStyle("-fx-font-size: 8px; -fx-text-fill: #757575; -fx-font-style: italic;");
                cell.getChildren().add(moreLabel);
            }
        }

        // Click handler
        cell.setOnMouseClicked(e -> selectDate(date));

        return cell;
    }

    private Map<LocalDate, List<CalendarEvent>> filterEvents(String filter) {
        if (filter == null || filter.equals("All Events")) {
            return events;
        }

        Map<LocalDate, List<CalendarEvent>> filtered = new HashMap<>();
        for (Map.Entry<LocalDate, List<CalendarEvent>> entry : events.entrySet()) {
            List<CalendarEvent> filteredList = entry.getValue().stream()
                    .filter(e -> e.getCategory().equals(filter))
                    .collect(Collectors.toList());
            if (!filteredList.isEmpty()) {
                filtered.put(entry.getKey(), filteredList);
            }
        }
        return filtered;
    }

    private void selectDate(LocalDate date) {
        selectedDate = date;
        selectedDateLabel.setText(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));

        List<CalendarEvent> dayEvents = events.get(date);
        if (dayEvents != null && !dayEvents.isEmpty()) {
            selectedDateEventsLabel.setText(dayEvents.size() + " event(s) on this date");

            ObservableList<String> eventStrings = FXCollections.observableArrayList();
            for (CalendarEvent event : dayEvents) {
                String color = eventColors.getOrDefault(event.getCategory(), "#757575");
                eventStrings.add("● " + event.getTitle() + "\n" + event.getCategory() + " • " + event.getTime());
            }
            dateEventsListView.setItems(eventStrings);

            dateEventsListView.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle("-fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
                    }
                }
            });
        } else {
            selectedDateEventsLabel.setText("No events on this date");
            dateEventsListView.setItems(FXCollections.observableArrayList());
        }

        renderCalendar();
        statusLabel.setText("Selected: " + date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
    }

    private void updateStatistics() {
        // Count events this month
        long eventsCount = events.entrySet().stream()
                .filter(e -> YearMonth.from(e.getKey()).equals(currentMonth))
                .mapToLong(e -> e.getValue().size())
                .sum();
        eventsThisMonthLabel.setText(String.valueOf(eventsCount));

        // Count school days (weekdays that are not holidays)
        long schoolDays = 0;
        long holidays = 0;
        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            LocalDate date = currentMonth.atDay(day);
            List<CalendarEvent> dayEvents = events.get(date);

            boolean isHoliday = dayEvents != null && dayEvents.stream()
                    .anyMatch(e -> e.getCategory().equals("Holiday"));

            if (isHoliday) {
                holidays++;
            } else if (date.getDayOfWeek().getValue() < 6) { // Monday-Friday
                schoolDays++;
            }
        }

        schoolDaysLabel.setText(String.valueOf(schoolDays));
        holidaysLabel.setText(String.valueOf(holidays));
    }

    private void updateLastUpdated() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd 'at' h:mm a"));
        lastUpdatedLabel.setText("Last updated: " + timestamp);
    }

    // Event Handlers

    @FXML
    private void handlePreviousMonth() {
        currentMonth = currentMonth.minusMonths(1);
        renderCalendar();
        statusLabel.setText("Viewing " + currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
    }

    @FXML
    private void handleNextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        renderCalendar();
        statusLabel.setText("Viewing " + currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
    }

    @FXML
    private void handleToday() {
        currentMonth = YearMonth.now();
        selectDate(LocalDate.now());
        renderCalendar();
        statusLabel.setText("Viewing current month");
    }

    @FXML
    private void handleNewEvent() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Event");
        dialog.setHeaderText("Create a new calendar event");
        dialog.setContentText("Event title:");

        dialog.showAndWait().ifPresent(title -> {
            statusLabel.setText("Event '" + title + "' would be created");
            showAlert("New Event", "Event creation dialog will open:\n" +
                    "• Event title\n" +
                    "• Date and time\n" +
                    "• Category\n" +
                    "• Description\n" +
                    "• Recurrence options");
        });
    }

    @FXML
    private void handleAddEventToDate() {
        if (selectedDate != null) {
            statusLabel.setText("Adding event to " + selectedDate.format(DateTimeFormatter.ofPattern("MMM d")));
            showAlert("Add Event", "Add event to " + selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        } else {
            showAlert("No Date Selected", "Please select a date first");
        }
    }

    @FXML
    private void handleQuickAddEvent() {
        String title = quickEventTitleField.getText();
        LocalDate date = quickEventDatePicker.getValue();
        String category = quickEventCategoryComboBox.getValue();

        if (title == null || title.trim().isEmpty()) {
            showAlert("Missing Title", "Please enter an event title");
            return;
        }

        if (date == null) {
            showAlert("Missing Date", "Please select a date");
            return;
        }

        if (category == null) {
            showAlert("Missing Category", "Please select a category");
            return;
        }

        addEvent(date, title, category);
        renderCalendar();

        quickEventTitleField.clear();
        quickEventDatePicker.setValue(null);
        quickEventCategoryComboBox.getSelectionModel().clearSelection();

        statusLabel.setText("Event '" + title + "' added to " + date.format(DateTimeFormatter.ofPattern("MMM d")));
        showAlert("Event Added", "Event '" + title + "' has been added to the calendar");
    }

    @FXML
    private void handleExportCalendar() {
        statusLabel.setText("Exporting calendar...");
        showAlert("Export Calendar",
                "Export calendar to:\n" +
                        "• iCalendar (.ics)\n" +
                        "• Google Calendar\n" +
                        "• Outlook\n" +
                        "• PDF\n" +
                        "• Excel");
    }

    @FXML
    private void handleImportCalendar() {
        statusLabel.setText("Importing calendar...");
        showAlert("Import Calendar",
                "Import events from:\n" +
                        "• iCalendar (.ics)\n" +
                        "• Google Calendar\n" +
                        "• Outlook\n" +
                        "• CSV file");
    }

    @FXML
    private void handlePrintCalendar() {
        statusLabel.setText("Printing calendar for " + currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        showAlert("Print Calendar", "Print calendar for " + currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
    }

    @FXML
    private void handleManageAcademicCalendar() {
        statusLabel.setText("Opening academic calendar settings...");
        showAlert("Academic Calendar",
                "Manage:\n" +
                        "• School start/end dates\n" +
                        "• Grading periods (quarters/semesters)\n" +
                        "• Holidays and breaks\n" +
                        "• Professional development days\n" +
                        "• Early dismissal days\n" +
                        "• Testing windows");
    }

    @FXML
    private void handleSettings() {
        statusLabel.setText("Opening calendar settings...");
        showAlert("Calendar Settings",
                "Configure:\n" +
                        "• Default view (Month/Week/Day)\n" +
                        "• Event categories and colors\n" +
                        "• Notification preferences\n" +
                        "• Calendar sharing options\n" +
                        "• Display preferences");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Data Classes

    public static class CalendarEvent {
        private String title;
        private String category;
        private LocalDate date;
        private String time;

        public CalendarEvent(String title, String category, LocalDate date, String time) {
            this.title = title;
            this.category = category;
            this.date = date;
            this.time = time;
        }

        public String getTitle() { return title; }
        public String getCategory() { return category; }
        public LocalDate getDate() { return date; }
        public String getTime() { return time; }
    }
}
