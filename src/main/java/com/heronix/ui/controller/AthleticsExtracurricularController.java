package com.heronix.ui.controller;

import com.heronix.model.domain.*;
import com.heronix.service.AthleticsService;
import com.heronix.service.ClubActivitiesService;
import com.heronix.repository.ClubRepository;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AthleticsExtracurricularController {

    private final AthleticsService athleticsService;
    private final ClubActivitiesService clubActivitiesService;
    private final ClubRepository clubRepository;

    private static final String CURRENT_ACADEMIC_YEAR = "2024-2025";

    // FXML fields for statistics
    @FXML private Label teamsLabel;
    @FXML private Label participantsLabel;
    @FXML private Label clubsLabel;
    @FXML private Label clubMembersLabel;
    @FXML private Label statusLabel;

    // FXML fields for teams tab
    @FXML private TableView<AthleticTeam> teamsTableView;
    @FXML private TableColumn<AthleticTeam, String> teamNameColumn;
    @FXML private TableColumn<AthleticTeam, String> sportColumn;
    @FXML private TableColumn<AthleticTeam, String> seasonColumn;
    @FXML private TableColumn<AthleticTeam, String> coachColumn;
    @FXML private TableColumn<AthleticTeam, Integer> rosterSizeColumn;
    @FXML private TableColumn<AthleticTeam, String> recordColumn;
    @FXML private TableColumn<AthleticTeam, Void> teamActionsColumn;

    // FXML fields for schedule/events tab
    @FXML private TableView<AthleticEvent> scheduleTableView;
    @FXML private TableColumn<AthleticEvent, String> eventNameColumn;
    @FXML private TableColumn<AthleticEvent, String> eventTeamColumn;
    @FXML private TableColumn<AthleticEvent, String> eventDateColumn;
    @FXML private TableColumn<AthleticEvent, String> eventTypeColumn;
    @FXML private TableColumn<AthleticEvent, String> opponentColumn;
    @FXML private TableColumn<AthleticEvent, String> resultColumn;
    @FXML private TableColumn<AthleticEvent, Void> scheduleActionsColumn;

    // FXML fields for clubs tab
    @FXML private TableView<Club> clubsTableView;
    @FXML private TableColumn<Club, String> clubNameColumn;
    @FXML private TableColumn<Club, String> categoryColumn;
    @FXML private TableColumn<Club, String> advisorColumn;
    @FXML private TableColumn<Club, Integer> membersColumn;
    @FXML private TableColumn<Club, Integer> maxMembersColumn;

    // FXML fields for rosters tab
    @FXML private TableView<TeamMembership> rostersTableView;
    @FXML private TableColumn<TeamMembership, String> playerNameColumn;
    @FXML private TableColumn<TeamMembership, String> playerTeamColumn;
    @FXML private TableColumn<TeamMembership, String> positionColumn;
    @FXML private TableColumn<TeamMembership, String> jerseyColumn;
    @FXML private TableColumn<TeamMembership, String> eligibilityColumn;
    @FXML private TableColumn<TeamMembership, String> documentsColumn;

    // FXML fields for team management
    @FXML private TextField teamNameField;
    @FXML private ComboBox<String> sportComboBox;
    @FXML private ComboBox<String> seasonComboBox;
    @FXML private TextField coachIdField;
    @FXML private TextField maxRosterField;

    // FXML fields for event management
    @FXML private TextField eventNameField;
    @FXML private TextField eventTeamIdField;
    @FXML private TextField opponentField;
    @FXML private ComboBox<String> eventTypeComboBox;

    // FXML fields for club management
    @FXML private TextField clubNameField;
    @FXML private ComboBox<String> clubCategoryComboBox;
    @FXML private TextField clubAdvisorIdField;

    // Observable lists
    private final ObservableList<AthleticTeam> teamsList = FXCollections.observableArrayList();
    private final ObservableList<AthleticEvent> eventsList = FXCollections.observableArrayList();
    private final ObservableList<Club> clubsList = FXCollections.observableArrayList();
    private final ObservableList<TeamMembership> rostersList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTables();
        loadData();
        updateStatistics();
        setupComboBoxes();
    }

    private void setupTables() {
        setupTeamActionsColumn();
        setupScheduleActionsColumn();

        // Teams table setup
        if (teamsTableView != null) {
            teamNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTeamName()));
            sportColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSport() != null ?
                    cellData.getValue().getSport().toString() : "N/A"));
            seasonColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSeason().toString()));
            coachColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getHeadCoach() != null ?
                    cellData.getValue().getHeadCoach().getFirstName() + " " +
                    cellData.getValue().getHeadCoach().getLastName() : "N/A"));
            rosterSizeColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getCurrentRosterSize()));
            recordColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRecord()));

            teamsTableView.setItems(teamsList);
        }

        // Schedule/Events table setup
        if (scheduleTableView != null) {
            eventNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEventName()));
            eventTeamColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTeam().getTeamName()));
            eventDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEventDate() != null ?
                    cellData.getValue().getEventDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")) : ""));
            eventTypeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEventType().toString()));
            opponentColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getOpponent() != null ?
                    cellData.getValue().getOpponent() : "N/A"));
            resultColumn.setCellValueFactory(cellData -> {
                AthleticEvent event = cellData.getValue();
                if (event.getResult() != null) {
                    return new SimpleStringProperty(event.getResult().toString() +
                        (event.getTeamScore() != null && event.getOpponentScore() != null ?
                            " (" + event.getTeamScore() + "-" + event.getOpponentScore() + ")" : ""));
                }
                return new SimpleStringProperty("Pending");
            });

            scheduleTableView.setItems(eventsList);
        }

        // Clubs table setup
        if (clubsTableView != null) {
            clubNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
            categoryColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCategory()));
            advisorColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAdvisor() != null ?
                    cellData.getValue().getAdvisor().getFirstName() + " " + cellData.getValue().getAdvisor().getLastName() : "N/A"));
            membersColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getCurrentEnrollment()));
            maxMembersColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getMaxCapacity()));

            clubsTableView.setItems(clubsList);
        }

        // Rosters table setup
        if (rostersTableView != null) {
            playerNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStudent().getFirstName() + " " +
                    cellData.getValue().getStudent().getLastName()));
            playerTeamColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTeam().getTeamName()));
            positionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPosition() != null ?
                    cellData.getValue().getPosition() : "N/A"));
            jerseyColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getJerseyNumber() != null ?
                    cellData.getValue().getJerseyNumber() : "N/A"));
            eligibilityColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEligible() != null &&
                    cellData.getValue().getEligible() ? "Eligible" : "Ineligible"));
            documentsColumn.setCellValueFactory(cellData -> {
                TeamMembership membership = cellData.getValue();
                int docsCount = 0;
                if (Boolean.TRUE.equals(membership.getPhysicalOnFile())) docsCount++;
                if (Boolean.TRUE.equals(membership.getConsentFormSigned())) docsCount++;
                if (Boolean.TRUE.equals(membership.getEmergencyContactOnFile())) docsCount++;
                return new SimpleStringProperty(docsCount + "/3");
            });

            rostersTableView.setItems(rostersList);
        }
    }

    private void setupTeamActionsColumn() {
        if (teamActionsColumn == null) return;

        teamActionsColumn.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(null));
        teamActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final String BTN_STYLE = "-fx-text-fill: white; -fx-padding: 2 6; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;";
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button rosterBtn = new Button("View Roster");
            private final HBox pane = new HBox(4, editBtn, deleteBtn, rosterBtn);

            {
                pane.setAlignment(Pos.CENTER);
                editBtn.setStyle("-fx-background-color: #3b82f6;" + BTN_STYLE);
                deleteBtn.setStyle("-fx-background-color: #ef4444;" + BTN_STYLE);
                rosterBtn.setStyle("-fx-background-color: #8b5cf6;" + BTN_STYLE);

                editBtn.setOnAction(e -> {
                    AthleticTeam team = getTableRow().getItem();
                    if (team != null) {
                        showAlert("Edit Team", "Edit functionality for: " + team.getTeamName());
                    }
                });

                deleteBtn.setOnAction(e -> {
                    AthleticTeam team = getTableRow().getItem();
                    if (team != null) {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Delete Team");
                        confirm.setHeaderText("Delete " + team.getTeamName() + "?");
                        confirm.setContentText("This action cannot be undone.");
                        confirm.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                new Thread(() -> {
                                    try {
                                        athleticsService.deleteTeam(team.getId());
                                        Platform.runLater(() -> {
                                            showAlert("Success", "Team deleted");
                                            loadData();
                                            updateStatistics();
                                        });
                                    } catch (Exception ex) {
                                        Platform.runLater(() -> showAlert("Error", "Failed to delete team: " + ex.getMessage()));
                                    }
                                }).start();
                            }
                        });
                    }
                });

                rosterBtn.setOnAction(e -> {
                    AthleticTeam team = getTableRow().getItem();
                    if (team != null) {
                        new Thread(() -> {
                            try {
                                List<TeamMembership> roster = athleticsService.getActiveRoster(team.getId());
                                Platform.runLater(() -> rostersList.setAll(roster));
                            } catch (Exception ex) {
                                Platform.runLater(() -> showAlert("Error", "Failed to load roster: " + ex.getMessage()));
                            }
                        }).start();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupScheduleActionsColumn() {
        if (scheduleActionsColumn == null) return;

        scheduleActionsColumn.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(null));
        scheduleActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final String BTN_STYLE = "-fx-text-fill: white; -fx-padding: 2 6; -fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;";
            private final Button resultBtn = new Button("Record Result");
            private final Button cancelBtn = new Button("Cancel");
            private final HBox pane = new HBox(4, resultBtn, cancelBtn);

            {
                pane.setAlignment(Pos.CENTER);
                resultBtn.setStyle("-fx-background-color: #f59e0b;" + BTN_STYLE);
                cancelBtn.setStyle("-fx-background-color: #ef4444;" + BTN_STYLE);

                resultBtn.setOnAction(e -> {
                    AthleticEvent event = getTableRow().getItem();
                    if (event != null) {
                        // Select this event in the table, then call the existing handler
                        scheduleTableView.getSelectionModel().select(event);
                        handleRecordResult();
                    }
                });

                cancelBtn.setOnAction(e -> {
                    AthleticEvent event = getTableRow().getItem();
                    if (event != null) {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Cancel Event");
                        confirm.setHeaderText("Cancel " + event.getEventName() + "?");
                        confirm.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                new Thread(() -> {
                                    try {
                                        athleticsService.cancelEvent(event.getId(), "Cancelled by user");
                                        Platform.runLater(() -> {
                                            showAlert("Success", "Event cancelled");
                                            loadData();
                                        });
                                    } catch (Exception ex) {
                                        Platform.runLater(() -> showAlert("Error", "Failed to cancel event: " + ex.getMessage()));
                                    }
                                }).start();
                            }
                        });
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupComboBoxes() {
        if (sportComboBox != null) {
            sportComboBox.getItems().addAll("Basketball", "Football", "Soccer", "Baseball",
                "Softball", "Volleyball", "Track & Field", "Swimming", "Tennis", "Golf");
        }

        if (seasonComboBox != null) {
            seasonComboBox.getItems().addAll("FALL", "WINTER", "SPRING", "SUMMER");
        }

        if (eventTypeComboBox != null) {
            eventTypeComboBox.getItems().addAll("GAME", "PRACTICE", "TOURNAMENT", "SCRIMMAGE",
                "TRYOUT", "TEAM_MEETING");
        }

        if (clubCategoryComboBox != null) {
            clubCategoryComboBox.getItems().addAll("Academic", "Arts", "Service", "Sports",
                "Technology", "Leadership", "Cultural", "Special Interest");
        }
    }

    private void loadData() {
        new Thread(() -> {
            try {
                // Load teams
                List<AthleticTeam> teams = athleticsService.getTeamsByYear(CURRENT_ACADEMIC_YEAR);
                Platform.runLater(() -> teamsList.setAll(teams));

                // Load events
                List<AthleticEvent> events = athleticsService.getAllUpcomingEvents();
                Platform.runLater(() -> eventsList.setAll(events));

                // Load clubs
                List<Club> clubs = clubRepository.findByActiveTrueOrderByNameAsc();
                Platform.runLater(() -> clubsList.setAll(clubs));

                // Load all rosters from all teams
                List<TeamMembership> rosters = teams.stream()
                    .flatMap(team -> athleticsService.getActiveRoster(team.getId()).stream())
                    .toList();
                Platform.runLater(() -> rostersList.setAll(rosters));

                Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setText("Data loaded successfully");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setText("Error loading data: " + e.getMessage());
                    }
                });
            }
        }).start();
    }

    private void updateStatistics() {
        new Thread(() -> {
            try {
                // Get athletics statistics
                Map<String, Object> athleticsStats = athleticsService.getAthleticsOverview(CURRENT_ACADEMIC_YEAR);
                Long totalTeams = (Long) athleticsStats.getOrDefault("totalTeams", 0L);
                Long totalAthletes = (Long) athleticsStats.getOrDefault("totalAthletes", 0L);

                // Get clubs statistics
                Map<String, Object> clubStats = clubActivitiesService.getClubsOverview(CURRENT_ACADEMIC_YEAR);
                Long totalClubs = (Long) clubStats.getOrDefault("totalActiveClubs", 0L);
                Long totalClubMembers = (Long) clubStats.getOrDefault("totalMembers", 0L);

                Platform.runLater(() -> {
                    if (teamsLabel != null) {
                        teamsLabel.setText(String.valueOf(totalTeams));
                    }
                    if (participantsLabel != null) {
                        participantsLabel.setText(String.valueOf(totalAthletes));
                    }
                    if (clubsLabel != null) {
                        clubsLabel.setText(String.valueOf(totalClubs));
                    }
                    if (clubMembersLabel != null) {
                        clubMembersLabel.setText(String.valueOf(totalClubMembers));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setText("Error updating statistics: " + e.getMessage());
                    }
                });
            }
        }).start();
    }

    // Team Management Handlers

    @FXML
    private void handleCreateTeam() {
        String teamName = teamNameField.getText();
        String sport = sportComboBox.getValue();
        String season = seasonComboBox.getValue();
        String coachIdStr = coachIdField.getText();
        String maxRosterStr = maxRosterField.getText();

        if (teamName == null || teamName.isEmpty() || sport == null || season == null) {
            showAlert("Validation Error", "Team name, sport, and season are required");
            return;
        }

        new Thread(() -> {
            try {
                AthleticTeam team = new AthleticTeam();
                team.setTeamName(teamName);
                // Convert sport string to enum - replace spaces/& with underscore
                String sportEnum = sport.toUpperCase().replace(" ", "_").replace("&", "AND");
                team.setSport(AthleticTeam.Sport.valueOf(sportEnum));
                team.setSeason(AthleticTeam.Season.valueOf(season));
                team.setAcademicYear(CURRENT_ACADEMIC_YEAR);
                team.setActive(true);
                team.setStatus(AthleticTeam.TeamStatus.RECRUITING);
                team.setLevel(AthleticTeam.TeamLevel.VARSITY); // Default

                if (maxRosterStr != null && !maxRosterStr.isEmpty()) {
                    team.setMaxRosterSize(Integer.parseInt(maxRosterStr));
                }

                AthleticTeam created = athleticsService.createTeam(team);

                // Assign coach if provided
                if (coachIdStr != null && !coachIdStr.isEmpty()) {
                    Long coachId = Long.parseLong(coachIdStr.trim());
                    // Update the team with the coach - requires TeacherRepository
                    // For now, skip coach assignment in UI
                }

                final String createdTeamName = created.getTeamName();
                Platform.runLater(() -> {
                    showAlert("Success", "Team created: " + createdTeamName);
                    loadData();
                    updateStatistics();
                    clearTeamFields();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to create team: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleAddPlayerToTeam() {
        AthleticTeam selectedTeam = teamsTableView.getSelectionModel().getSelectedItem();
        if (selectedTeam == null) {
            showAlert("Selection Error", "Please select a team first");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Player");
        dialog.setHeaderText("Add player to " + selectedTeam.getTeamName());
        dialog.setContentText("Student ID:");

        dialog.showAndWait().ifPresent(studentIdStr -> {
            new Thread(() -> {
                try {
                    Long studentId = Long.parseLong(studentIdStr.trim());
                    TeamMembership membership = athleticsService.addPlayerToTeam(
                        selectedTeam.getId(), studentId, CURRENT_ACADEMIC_YEAR, null);

                    Platform.runLater(() -> {
                        showAlert("Success", "Player added to team");
                        loadData();
                        updateStatistics();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Error", "Failed to add player: " + e.getMessage()));
                }
            }).start();
        });
    }

    @FXML
    private void handleRemovePlayer() {
        TeamMembership selected = rostersTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a player to remove");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Removal");
        confirmation.setHeaderText("Remove " + selected.getStudent().getFirstName() + " " +
            selected.getStudent().getLastName() + " from team?");
        confirmation.setContentText("This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        athleticsService.removePlayerFromTeam(selected.getId());
                        Platform.runLater(() -> {
                            showAlert("Success", "Player removed from team");
                            loadData();
                            updateStatistics();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Error", "Failed to remove player: " + e.getMessage()));
                    }
                }).start();
            }
        });
    }

    // Event Management Handlers

    @FXML
    private void handleScheduleEvent() {
        String eventName = eventNameField.getText();
        String teamIdStr = eventTeamIdField.getText();
        String eventType = eventTypeComboBox.getValue();
        String opponent = opponentField.getText();

        if (eventName == null || eventName.isEmpty() || teamIdStr == null ||
            teamIdStr.isEmpty() || eventType == null) {
            showAlert("Validation Error", "Event name, team, and type are required");
            return;
        }

        new Thread(() -> {
            try {
                Long teamId = Long.parseLong(teamIdStr.trim());
                AthleticTeam team = athleticsService.getTeamById(teamId);

                AthleticEvent event = new AthleticEvent();
                event.setEventName(eventName);
                event.setTeam(team);
                event.setEventType(AthleticEvent.EventType.valueOf(eventType));
                event.setEventDate(LocalDateTime.now().plusDays(7)); // Default to 1 week from now
                event.setOpponent(opponent != null && !opponent.isEmpty() ? opponent : null);
                event.setStatus(AthleticEvent.EventStatus.SCHEDULED);

                AthleticEvent created = athleticsService.scheduleEvent(event);

                Platform.runLater(() -> {
                    showAlert("Success", "Event scheduled: " + created.getEventName());
                    loadData();
                    clearEventFields();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to schedule event: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleRecordResult() {
        AthleticEvent selected = scheduleTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select an event to record result");
            return;
        }

        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Record Result");
        dialog.setHeaderText("Record result for " + selected.getEventName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<String> resultComboBox = new ComboBox<>();
        resultComboBox.getItems().addAll("WIN", "LOSS", "TIE", "FORFEIT_WIN", "FORFEIT_LOSS", "NO_CONTEST");
        TextField teamScoreField = new TextField();
        TextField opponentScoreField = new TextField();

        grid.add(new Label("Result:"), 0, 0);
        grid.add(resultComboBox, 1, 0);
        grid.add(new Label("Team Score:"), 0, 1);
        grid.add(teamScoreField, 1, 1);
        grid.add(new Label("Opponent Score:"), 0, 2);
        grid.add(opponentScoreField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new String[]{
                    resultComboBox.getValue(),
                    teamScoreField.getText(),
                    opponentScoreField.getText()
                };
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result[0] != null) {
                new Thread(() -> {
                    try {
                        Integer teamScore = result[1] != null && !result[1].isEmpty() ?
                            Integer.parseInt(result[1]) : null;
                        Integer opponentScore = result[2] != null && !result[2].isEmpty() ?
                            Integer.parseInt(result[2]) : null;

                        athleticsService.recordGameResult(selected.getId(), teamScore, opponentScore);

                        Platform.runLater(() -> {
                            showAlert("Success", "Result recorded");
                            loadData();
                            updateStatistics();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Error", "Failed to record result: " + e.getMessage()));
                    }
                }).start();
            }
        });
    }

    // Club Management Handlers

    @FXML
    private void handleCreateClub() {
        String clubName = clubNameField.getText();
        String category = clubCategoryComboBox.getValue();
        String advisorIdStr = clubAdvisorIdField.getText();

        if (clubName == null || clubName.isEmpty() || category == null) {
            showAlert("Validation Error", "Club name and category are required");
            return;
        }

        new Thread(() -> {
            try {
                Club club = new Club();
                club.setName(clubName);
                club.setCategory(category);
                club.setActive(true);

                Club created = clubRepository.save(club);

                // Assign advisor if provided - would need to update the club
                if (advisorIdStr != null && !advisorIdStr.isEmpty()) {
                    Long advisorId = Long.parseLong(advisorIdStr.trim());
                    // This would require loading teacher and updating club
                    showAlert("Info", "Club created. Please use club management to assign advisor.");
                }

                Platform.runLater(() -> {
                    showAlert("Success", "Club created: " + created.getName());
                    loadData();
                    updateStatistics();
                    clearClubFields();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to create club: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleEnrollStudent() {
        Club selectedClub = clubsTableView.getSelectionModel().getSelectedItem();
        if (selectedClub == null) {
            showAlert("Selection Error", "Please select a club first");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enroll Student");
        dialog.setHeaderText("Enroll student in " + selectedClub.getName());
        dialog.setContentText("Student ID:");

        dialog.showAndWait().ifPresent(studentIdStr -> {
            new Thread(() -> {
                try {
                    Long studentId = Long.parseLong(studentIdStr.trim());
                    ClubMembership membership = clubActivitiesService.joinClub(
                        selectedClub.getId(), studentId, CURRENT_ACADEMIC_YEAR);

                    Platform.runLater(() -> {
                        showAlert("Success", "Student enrolled in club");
                        loadData();
                        updateStatistics();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Error", "Failed to enroll student: " + e.getMessage()));
                }
            }).start();
        });
    }

    @FXML
    private void handleRefresh() {
        loadData();
        updateStatistics();
    }

    @FXML
    private void handleViewEligibility() {
        new Thread(() -> {
            try {
                List<TeamMembership> ineligible = athleticsService.getIneligiblePlayers();
                Platform.runLater(() -> {
                    if (ineligible.isEmpty()) {
                        showAlert("Eligibility Check", "All players are currently eligible");
                    } else {
                        StringBuilder message = new StringBuilder("Ineligible Players:\n\n");
                        for (TeamMembership m : ineligible) {
                            message.append(m.getStudent().getFirstName()).append(" ")
                                   .append(m.getStudent().getLastName())
                                   .append(" - ").append(m.getTeam().getTeamName())
                                   .append(" (").append(m.getStatus()).append(")\n");
                        }
                        showAlert("Eligibility Check", message.toString());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to check eligibility: " + e.getMessage()));
            }
        }).start();
    }

    // Helper methods

    private void clearTeamFields() {
        if (teamNameField != null) teamNameField.clear();
        if (sportComboBox != null) sportComboBox.setValue(null);
        if (seasonComboBox != null) seasonComboBox.setValue(null);
        if (coachIdField != null) coachIdField.clear();
        if (maxRosterField != null) maxRosterField.clear();
    }

    private void clearEventFields() {
        if (eventNameField != null) eventNameField.clear();
        if (eventTeamIdField != null) eventTeamIdField.clear();
        if (eventTypeComboBox != null) eventTypeComboBox.setValue(null);
        if (opponentField != null) opponentField.clear();
    }

    private void clearClubFields() {
        if (clubNameField != null) clubNameField.clear();
        if (clubCategoryComboBox != null) clubCategoryComboBox.setValue(null);
        if (clubAdvisorIdField != null) clubAdvisorIdField.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
