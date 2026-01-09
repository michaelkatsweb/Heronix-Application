package com.heronix.ui.controller;

import com.heronix.model.domain.LibraryBook;
import com.heronix.model.domain.LibraryCheckout;
import com.heronix.model.domain.LibraryFine;
import com.heronix.service.LibraryManagementService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LibraryManagementController {

    private final LibraryManagementService libraryService;

    @FXML private Label totalBooksLabel;
    @FXML private Label checkedOutLabel;
    @FXML private Label overdueLabel;
    @FXML private Label availableBooksLabel;
    @FXML private Label totalFinesLabel;

    @FXML private TextField searchField;
    @FXML private TextField studentIdField;
    @FXML private TextField bookIsbnField;
    @FXML private ComboBox<String> genreFilterComboBox;

    @FXML private TableView<LibraryBook> catalogTableView;
    @FXML private TableColumn<LibraryBook, String> isbnColumn;
    @FXML private TableColumn<LibraryBook, String> titleColumn;
    @FXML private TableColumn<LibraryBook, String> authorColumn;
    @FXML private TableColumn<LibraryBook, String> genreColumn;
    @FXML private TableColumn<LibraryBook, Integer> availableColumn;
    @FXML private TableColumn<LibraryBook, Integer> totalCopiesColumn;

    @FXML private TableView<LibraryCheckout> checkoutsTableView;
    @FXML private TableColumn<LibraryCheckout, String> studentNameColumn;
    @FXML private TableColumn<LibraryCheckout, String> bookTitleColumn;
    @FXML private TableColumn<LibraryCheckout, LocalDate> checkoutDateColumn;
    @FXML private TableColumn<LibraryCheckout, LocalDate> dueDateColumn;
    @FXML private TableColumn<LibraryCheckout, String> statusColumn;

    @FXML private TableView<LibraryFine> finesTableView;
    @FXML private TableColumn<LibraryFine, String> fineStudentColumn;
    @FXML private TableColumn<LibraryFine, String> fineTypeColumn;
    @FXML private TableColumn<LibraryFine, String> fineAmountColumn;
    @FXML private TableColumn<LibraryFine, String> fineStatusColumn;

    private ObservableList<LibraryBook> books = FXCollections.observableArrayList();
    private ObservableList<LibraryCheckout> checkouts = FXCollections.observableArrayList();
    private ObservableList<LibraryFine> fines = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupCatalogTable();
        setupCheckoutsTable();
        setupFinesTable();
        loadLibraryData();
        updateStatistics();
    }

    private void setupCatalogTable() {
        if (isbnColumn != null) isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        if (authorColumn != null) authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        if (genreColumn != null) genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        if (availableColumn != null) availableColumn.setCellValueFactory(new PropertyValueFactory<>("availableCopies"));
        if (totalCopiesColumn != null) totalCopiesColumn.setCellValueFactory(new PropertyValueFactory<>("totalCopies"));

        if (catalogTableView != null) catalogTableView.setItems(books);
    }

    private void setupCheckoutsTable() {
        if (studentNameColumn != null) {
            studentNameColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getStudent().getFullName()));
        }
        if (bookTitleColumn != null) {
            bookTitleColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getBook().getTitle()));
        }
        if (checkoutDateColumn != null) checkoutDateColumn.setCellValueFactory(new PropertyValueFactory<>("checkoutDate"));
        if (dueDateColumn != null) dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        if (statusColumn != null) {
            statusColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getStatus().toString()));
        }

        if (checkoutsTableView != null) checkoutsTableView.setItems(checkouts);
    }

    private void setupFinesTable() {
        if (fineStudentColumn != null) {
            fineStudentColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getStudent().getFullName()));
        }
        if (fineTypeColumn != null) {
            fineTypeColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getFineType().toString()));
        }
        if (fineAmountColumn != null) {
            fineAmountColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            "$" + cellData.getValue().getAmount().toString()));
        }
        if (fineStatusColumn != null) {
            fineStatusColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getStatus().toString()));
        }

        if (finesTableView != null) finesTableView.setItems(fines);
    }

    private void loadLibraryData() {
        new Thread(() -> {
            try {
                // Load all books
                List<LibraryBook> allBooks = libraryService.getAllBooks();
                Platform.runLater(() -> books.setAll(allBooks));

                // Load active checkouts (show all overdue checkouts by default)
                List<LibraryCheckout> overdueCheckouts = libraryService.getAllOverdueCheckouts();
                Platform.runLater(() -> checkouts.setAll(overdueCheckouts));

                // Load outstanding fines
                List<LibraryCheckout> allCheckouts = overdueCheckouts.stream()
                        .limit(100)
                        .toList();
                List<LibraryFine> outstandingFines = allCheckouts.stream()
                        .flatMap(c -> libraryService.getStudentFines(c.getStudent().getId()).stream())
                        .filter(f -> f.getStatus() == LibraryFine.FineStatus.UNPAID ||
                                f.getStatus() == LibraryFine.FineStatus.PARTIAL)
                        .distinct()
                        .limit(50)
                        .toList();
                Platform.runLater(() -> fines.setAll(outstandingFines));

            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load library data: " + e.getMessage()));
            }
        }).start();
    }

    private void updateStatistics() {
        new Thread(() -> {
            try {
                Map<String, Object> stats = libraryService.getLibraryStatistics();

                Platform.runLater(() -> {
                    if (totalBooksLabel != null) {
                        totalBooksLabel.setText(String.format("%,d", stats.get("totalBooks")));
                    }
                    if (checkedOutLabel != null) {
                        checkedOutLabel.setText(String.format("%,d", stats.get("totalCheckedOut")));
                    }
                    if (overdueLabel != null) {
                        overdueLabel.setText(String.format("%,d", stats.get("totalOverdue")));
                    }
                    if (availableBooksLabel != null) {
                        availableBooksLabel.setText(String.format("%,d", stats.get("totalAvailable")));
                    }
                    if (totalFinesLabel != null) {
                        totalFinesLabel.setText("$" + String.format("%,.2f", stats.get("totalOutstandingFines")));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load statistics: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadLibraryData();
            return;
        }

        new Thread(() -> {
            try {
                List<LibraryBook> results = libraryService.searchBooks(searchTerm.trim());
                Platform.runLater(() -> books.setAll(results));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Search failed: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleCheckOut() {
        String studentIdStr = studentIdField.getText();
        String isbn = bookIsbnField.getText();

        if (studentIdStr == null || studentIdStr.trim().isEmpty() ||
                isbn == null || isbn.trim().isEmpty()) {
            showAlert("Validation Error", "Please enter both Student ID and Book ISBN");
            return;
        }

        new Thread(() -> {
            try {
                Long studentId = Long.parseLong(studentIdStr.trim());
                LibraryBook book = libraryService.getBookByIsbn(isbn.trim());

                if (book == null) {
                    Platform.runLater(() -> showAlert("Error", "Book not found with ISBN: " + isbn));
                    return;
                }

                // Check if student can checkout
                if (!libraryService.canCheckout(studentId)) {
                    Platform.runLater(() -> showAlert("Cannot Checkout",
                            "Student has reached maximum checkouts or has outstanding fines"));
                    return;
                }

                LibraryCheckout checkout = libraryService.checkoutBook(
                        studentId, book.getId(), "System");

                Platform.runLater(() -> {
                    showAlert("Success", "Book checked out successfully\nDue date: " +
                            checkout.getDueDate());
                    studentIdField.clear();
                    bookIsbnField.clear();
                    loadLibraryData();
                    updateStatistics();
                });
            } catch (NumberFormatException e) {
                Platform.runLater(() -> showAlert("Error", "Invalid Student ID format"));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Checkout failed: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleCheckIn() {
        LibraryCheckout selectedCheckout = checkoutsTableView != null ?
                checkoutsTableView.getSelectionModel().getSelectedItem() : null;

        if (selectedCheckout == null) {
            showAlert("Selection Error", "Please select a checkout to return");
            return;
        }

        new Thread(() -> {
            try {
                LibraryCheckout returned = libraryService.returnBook(
                        selectedCheckout.getId(), "System");

                Platform.runLater(() -> {
                    String message = "Book returned successfully";
                    if (returned.getFineAmount() != null &&
                            returned.getFineAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                        message += "\nOverdue fine assessed: $" + returned.getFineAmount();
                    }
                    showAlert("Success", message);
                    loadLibraryData();
                    updateStatistics();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Check-in failed: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleRenewBook() {
        LibraryCheckout selectedCheckout = checkoutsTableView != null ?
                checkoutsTableView.getSelectionModel().getSelectedItem() : null;

        if (selectedCheckout == null) {
            showAlert("Selection Error", "Please select a checkout to renew");
            return;
        }

        new Thread(() -> {
            try {
                LibraryCheckout renewed = libraryService.renewBook(selectedCheckout.getId());
                Platform.runLater(() -> {
                    showAlert("Success", "Book renewed successfully\nNew due date: " +
                            renewed.getDueDate());
                    loadLibraryData();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Renewal failed: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleCalculateFines() {
        new Thread(() -> {
            try {
                libraryService.calculateOverdueFines();
                Platform.runLater(() -> {
                    showAlert("Success", "Overdue fines calculated successfully");
                    loadLibraryData();
                    updateStatistics();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Fine calculation failed: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleFilterByGenre() {
        if (genreFilterComboBox == null) return;

        String selectedGenre = genreFilterComboBox.getValue();
        if (selectedGenre == null || selectedGenre.equals("All Genres")) {
            loadLibraryData();
            return;
        }

        new Thread(() -> {
            try {
                List<LibraryBook> filtered = libraryService.getBooksByGenre(selectedGenre);
                Platform.runLater(() -> books.setAll(filtered));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Filter failed: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleRefresh() {
        loadLibraryData();
        updateStatistics();
    }

    @FXML
    private void handleViewAvailableBooks() {
        new Thread(() -> {
            try {
                List<LibraryBook> available = libraryService.getAvailableBooks();
                Platform.runLater(() -> books.setAll(available));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error", "Failed to load available books: " + e.getMessage()));
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
