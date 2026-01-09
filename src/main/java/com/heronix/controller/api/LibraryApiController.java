package com.heronix.controller.api;

import com.heronix.model.domain.LibraryBook;
import com.heronix.model.domain.LibraryCheckout;
import com.heronix.model.domain.LibraryFine;
import com.heronix.service.LibraryManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Library Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryApiController {

    private final LibraryManagementService libraryService;

    // ========== Book Management ==========

    @PostMapping("/books")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<LibraryBook> addBook(@RequestBody LibraryBook book) {
        LibraryBook created = libraryService.addBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/books/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<LibraryBook> updateBook(
            @PathVariable Long bookId,
            @RequestBody LibraryBook book) {
        LibraryBook updated = libraryService.updateBook(bookId, book);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/books/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long bookId) {
        libraryService.deleteBook(bookId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/books/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<LibraryBook> getBookById(@PathVariable Long bookId) {
        LibraryBook book = libraryService.getBookById(bookId);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/books/isbn/{isbn}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<LibraryBook> getBookByIsbn(@PathVariable String isbn) {
        LibraryBook book = libraryService.getBookByIsbn(isbn);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/books")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<LibraryBook>> getAllBooks() {
        List<LibraryBook> books = libraryService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/books/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<LibraryBook>> searchBooks(@RequestParam String searchTerm) {
        List<LibraryBook> books = libraryService.searchBooks(searchTerm);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/books/genre/{genre}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<LibraryBook>> getBooksByGenre(@PathVariable String genre) {
        List<LibraryBook> books = libraryService.getBooksByGenre(genre);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/books/author/{author}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<LibraryBook>> getBooksByAuthor(@PathVariable String author) {
        List<LibraryBook> books = libraryService.getBooksByAuthor(author);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/books/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<LibraryBook>> getAvailableBooks() {
        List<LibraryBook> books = libraryService.getAvailableBooks();
        return ResponseEntity.ok(books);
    }

    // ========== Checkout Management ==========

    @PostMapping("/checkouts")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<LibraryCheckout> checkoutBook(
            @RequestParam Long studentId,
            @RequestParam Long bookId,
            @RequestParam String checkedOutBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        LibraryCheckout checkout;
        if (dueDate != null) {
            checkout = libraryService.checkoutBook(studentId, bookId, dueDate, checkedOutBy);
        } else {
            checkout = libraryService.checkoutBook(studentId, bookId, checkedOutBy);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(checkout);
    }

    @PostMapping("/checkouts/{checkoutId}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<LibraryCheckout> returnBook(
            @PathVariable Long checkoutId,
            @RequestParam String checkedInBy) {
        LibraryCheckout checkout = libraryService.returnBook(checkoutId, checkedInBy);
        return ResponseEntity.ok(checkout);
    }

    @PostMapping("/checkouts/{checkoutId}/renew")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'STUDENT')")
    public ResponseEntity<LibraryCheckout> renewBook(@PathVariable Long checkoutId) {
        LibraryCheckout checkout = libraryService.renewBook(checkoutId);
        return ResponseEntity.ok(checkout);
    }

    @PostMapping("/checkouts/{checkoutId}/lost")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Void> markBookLost(@PathVariable Long checkoutId) {
        libraryService.markBookLost(checkoutId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/checkouts/{checkoutId}/damaged")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Void> markBookDamaged(
            @PathVariable Long checkoutId,
            @RequestParam String condition) {
        libraryService.markBookDamaged(checkoutId, condition);
        return ResponseEntity.ok().build();
    }

    // ========== Student Checkout Queries ==========

    @GetMapping("/checkouts/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<LibraryCheckout>> getStudentCheckouts(@PathVariable Long studentId) {
        List<LibraryCheckout> checkouts = libraryService.getStudentCheckouts(studentId);
        return ResponseEntity.ok(checkouts);
    }

    @GetMapping("/checkouts/student/{studentId}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<LibraryCheckout>> getActiveCheckouts(@PathVariable Long studentId) {
        List<LibraryCheckout> checkouts = libraryService.getActiveCheckouts(studentId);
        return ResponseEntity.ok(checkouts);
    }

    @GetMapping("/checkouts/student/{studentId}/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<LibraryCheckout>> getOverdueCheckouts(@PathVariable Long studentId) {
        List<LibraryCheckout> checkouts = libraryService.getOverdueCheckouts(studentId);
        return ResponseEntity.ok(checkouts);
    }

    @GetMapping("/checkouts/student/{studentId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<Map<String, Long>> countActiveCheckouts(@PathVariable Long studentId) {
        long count = libraryService.countActiveCheckouts(studentId);
        Map<String, Long> response = new HashMap<>();
        response.put("activeCheckouts", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/checkouts/student/{studentId}/can-checkout")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<Map<String, Boolean>> canCheckout(@PathVariable Long studentId) {
        boolean canCheckout = libraryService.canCheckout(studentId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("canCheckout", canCheckout);
        return ResponseEntity.ok(response);
    }

    // ========== Fine Management ==========

    @PostMapping("/fines")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<LibraryFine> createFine(
            @RequestParam Long studentId,
            @RequestParam Long checkoutId,
            @RequestParam LibraryFine.FineType fineType,
            @RequestParam BigDecimal amount,
            @RequestParam String reason,
            @RequestParam String issuedBy) {
        LibraryFine fine = libraryService.createFine(studentId, checkoutId, fineType, amount, reason, issuedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(fine);
    }

    @PostMapping("/fines/{fineId}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'CASHIER')")
    public ResponseEntity<Void> payFine(
            @PathVariable Long fineId,
            @RequestParam BigDecimal amount,
            @RequestParam String collectedBy) {
        libraryService.payFine(fineId, amount, collectedBy);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/fines/{fineId}/waive")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Void> waiveFine(
            @PathVariable Long fineId,
            @RequestParam String reason) {
        libraryService.waiveFine(fineId, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/fines/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<LibraryFine>> getStudentFines(@PathVariable Long studentId) {
        List<LibraryFine> fines = libraryService.getStudentFines(studentId);
        return ResponseEntity.ok(fines);
    }

    @GetMapping("/fines/student/{studentId}/outstanding")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<LibraryFine>> getOutstandingFines(@PathVariable Long studentId) {
        List<LibraryFine> fines = libraryService.getOutstandingFines(studentId);
        return ResponseEntity.ok(fines);
    }

    @GetMapping("/fines/student/{studentId}/balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<Map<String, BigDecimal>> getStudentFineBalance(@PathVariable Long studentId) {
        BigDecimal balance = libraryService.getStudentFineBalance(studentId);
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("balance", balance);
        return ResponseEntity.ok(response);
    }

    // ========== Automated Operations ==========

    @PostMapping("/operations/calculate-overdue-fines")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Void> calculateOverdueFines() {
        libraryService.calculateOverdueFines();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/operations/send-overdue-notices")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Void> sendOverdueNotices() {
        libraryService.sendOverdueNotices();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/operations/overdue-checkouts")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'TEACHER')")
    public ResponseEntity<List<LibraryCheckout>> getAllOverdueCheckouts() {
        List<LibraryCheckout> checkouts = libraryService.getAllOverdueCheckouts();
        return ResponseEntity.ok(checkouts);
    }

    // ========== Reporting ==========

    @GetMapping("/reports/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> getLibraryStatistics() {
        Map<String, Object> statistics = libraryService.getLibraryStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/reports/checkout-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> getCheckoutReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> report = libraryService.getCheckoutReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/popular-books")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<Map<String, Object>>> getPopularBooks(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> popularBooks = libraryService.getPopularBooks(limit);
        return ResponseEntity.ok(popularBooks);
    }

    @GetMapping("/reports/lost-books")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<LibraryCheckout>> getLostBooks() {
        List<LibraryCheckout> lostBooks = libraryService.getLostBooks();
        return ResponseEntity.ok(lostBooks);
    }
}
