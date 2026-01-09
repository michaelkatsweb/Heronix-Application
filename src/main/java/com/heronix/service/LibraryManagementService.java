package com.heronix.service;

import com.heronix.model.domain.LibraryBook;
import com.heronix.model.domain.LibraryCheckout;
import com.heronix.model.domain.LibraryFine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Library Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
public interface LibraryManagementService {

    // Book Management
    LibraryBook addBook(LibraryBook book);
    LibraryBook updateBook(Long bookId, LibraryBook book);
    void deleteBook(Long bookId);
    LibraryBook getBookById(Long bookId);
    LibraryBook getBookByIsbn(String isbn);
    List<LibraryBook> getAllBooks();
    List<LibraryBook> searchBooks(String searchTerm);
    List<LibraryBook> getBooksByGenre(String genre);
    List<LibraryBook> getBooksByAuthor(String author);
    List<LibraryBook> getAvailableBooks();

    // Checkout Management
    LibraryCheckout checkoutBook(Long studentId, Long bookId, String checkedOutBy);
    LibraryCheckout checkoutBook(Long studentId, Long bookId, LocalDate dueDate, String checkedOutBy);
    LibraryCheckout returnBook(Long checkoutId, String checkedInBy);
    LibraryCheckout renewBook(Long checkoutId);
    void markBookLost(Long checkoutId);
    void markBookDamaged(Long checkoutId, String condition);

    // Student Checkout Queries
    List<LibraryCheckout> getStudentCheckouts(Long studentId);
    List<LibraryCheckout> getActiveCheckouts(Long studentId);
    List<LibraryCheckout> getOverdueCheckouts(Long studentId);
    long countActiveCheckouts(Long studentId);
    boolean canCheckout(Long studentId);

    // Fine Management
    LibraryFine createFine(Long studentId, Long checkoutId, LibraryFine.FineType fineType,
                          BigDecimal amount, String reason, String issuedBy);
    void payFine(Long fineId, BigDecimal amount, String collectedBy);
    void waiveFine(Long fineId, String reason);
    List<LibraryFine> getStudentFines(Long studentId);
    List<LibraryFine> getOutstandingFines(Long studentId);
    BigDecimal getStudentFineBalance(Long studentId);

    // Automated Operations
    void calculateOverdueFines();
    void sendOverdueNotices();
    List<LibraryCheckout> getAllOverdueCheckouts();

    // Reporting
    Map<String, Object> getLibraryStatistics();
    Map<String, Object> getCheckoutReport(LocalDate startDate, LocalDate endDate);
    List<Map<String, Object>> getPopularBooks(int limit);
    List<LibraryCheckout> getLostBooks();
}
