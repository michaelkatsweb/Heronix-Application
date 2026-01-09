package com.heronix.service.impl;

import com.heronix.model.domain.*;
import com.heronix.repository.*;
import com.heronix.service.LibraryManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of LibraryManagementService
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryManagementServiceImpl implements LibraryManagementService {

    private final LibraryBookRepository bookRepository;
    private final LibraryCheckoutRepository checkoutRepository;
    private final LibraryFineRepository fineRepository;
    private final StudentRepository studentRepository;

    private static final int MAX_CHECKOUTS_PER_STUDENT = 5;
    private static final int DEFAULT_CHECKOUT_DAYS = 14;
    private static final BigDecimal OVERDUE_FINE_PER_DAY = new BigDecimal("0.25");
    private static final BigDecimal MAX_OVERDUE_FINE = new BigDecimal("10.00");

    // ========================================================================
    // BOOK MANAGEMENT
    // ========================================================================

    @Override
    @Transactional
    public LibraryBook addBook(LibraryBook book) {
        log.info("Adding new book: {}", book.getTitle());
        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public LibraryBook updateBook(Long bookId, LibraryBook book) {
        log.info("Updating book ID: {}", bookId);
        LibraryBook existing = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + bookId));

        existing.setTitle(book.getTitle());
        existing.setAuthor(book.getAuthor());
        existing.setPublisher(book.getPublisher());
        existing.setPublicationYear(book.getPublicationYear());
        existing.setGenre(book.getGenre());
        existing.setSubject(book.getSubject());
        existing.setTotalCopies(book.getTotalCopies());
        existing.setLocation(book.getLocation());
        existing.setDescription(book.getDescription());

        return bookRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId) {
        log.info("Deleting book ID: {}", bookId);
        LibraryBook book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + bookId));
        book.setActive(false);
        bookRepository.save(book);
    }

    @Override
    @Transactional(readOnly = true)
    public LibraryBook getBookById(Long bookId) {
        return bookRepository.findById(bookId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public LibraryBook getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LibraryBook> getAllBooks() {
        return bookRepository.findByActiveTrueOrderByTitleAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LibraryBook> searchBooks(String searchTerm) {
        return bookRepository.searchBooks(searchTerm);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LibraryBook> getBooksByGenre(String genre) {
        return bookRepository.findByGenreAndActiveTrue(genre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LibraryBook> getBooksByAuthor(String author) {
        return bookRepository.findByAuthorAndActiveTrue(author);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LibraryBook> getAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }

    // ========================================================================
    // CHECKOUT MANAGEMENT
    // ========================================================================

    @Override
    @Transactional
    public LibraryCheckout checkoutBook(Long studentId, Long bookId, String checkedOutBy) {
        LocalDate dueDate = LocalDate.now().plusDays(DEFAULT_CHECKOUT_DAYS);
        return checkoutBook(studentId, bookId, dueDate, checkedOutBy);
    }

    @Override
    @Transactional
    public LibraryCheckout checkoutBook(Long studentId, Long bookId, LocalDate dueDate, String checkedOutBy) {
        log.info("Checking out book {} to student {}", bookId, studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        LibraryBook book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + bookId));

        // Validation
        if (!canCheckout(studentId)) {
            throw new IllegalStateException("Student has reached maximum checkout limit or has outstanding fines");
        }

        if (book.getReference()) {
            throw new IllegalStateException("Reference books cannot be checked out");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No copies available for checkout");
        }

        // Create checkout
        LibraryCheckout checkout = LibraryCheckout.builder()
                .book(book)
                .student(student)
                .checkoutDate(LocalDate.now())
                .dueDate(dueDate)
                .status(LibraryCheckout.CheckoutStatus.CHECKED_OUT)
                .checkedOutBy(checkedOutBy)
                .condition("Good")
                .build();

        LibraryCheckout saved = checkoutRepository.save(checkout);

        // Update book inventory
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        book.setCheckedOutCopies(book.getCheckedOutCopies() + 1);
        bookRepository.save(book);

        log.info("Book checked out successfully. Due date: {}", dueDate);
        return saved;
    }

    @Override
    @Transactional
    public LibraryCheckout returnBook(Long checkoutId, String checkedInBy) {
        log.info("Processing return for checkout ID: {}", checkoutId);

        LibraryCheckout checkout = checkoutRepository.findById(checkoutId)
                .orElseThrow(() -> new IllegalArgumentException("Checkout not found with ID: " + checkoutId));

        if (checkout.getStatus() != LibraryCheckout.CheckoutStatus.CHECKED_OUT &&
            checkout.getStatus() != LibraryCheckout.CheckoutStatus.OVERDUE) {
            throw new IllegalStateException("Book has already been returned");
        }

        checkout.setReturnDate(LocalDate.now());
        checkout.setCheckedInBy(checkedInBy);
        checkout.setStatus(LibraryCheckout.CheckoutStatus.RETURNED);
        checkout.setReturnCondition("Good");

        // Calculate overdue fine if applicable
        if (checkout.isOverdue()) {
            long daysOverdue = checkout.getDaysOverdue();
            BigDecimal fineAmount = OVERDUE_FINE_PER_DAY.multiply(new BigDecimal(daysOverdue));
            if (fineAmount.compareTo(MAX_OVERDUE_FINE) > 0) {
                fineAmount = MAX_OVERDUE_FINE;
            }
            createFine(checkout.getStudent().getId(), checkoutId, LibraryFine.FineType.OVERDUE,
                    fineAmount, "Overdue return: " + daysOverdue + " days", checkedInBy);
        }

        LibraryCheckout saved = checkoutRepository.save(checkout);

        // Update book inventory
        LibraryBook book = checkout.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        book.setCheckedOutCopies(book.getCheckedOutCopies() - 1);
        bookRepository.save(book);

        log.info("Book returned successfully");
        return saved;
    }

    @Override
    @Transactional
    public LibraryCheckout renewBook(Long checkoutId) {
        log.info("Renewing checkout ID: {}", checkoutId);

        LibraryCheckout checkout = checkoutRepository.findById(checkoutId)
                .orElseThrow(() -> new IllegalArgumentException("Checkout not found with ID: " + checkoutId));

        if (checkout.getStatus() != LibraryCheckout.CheckoutStatus.CHECKED_OUT) {
            throw new IllegalStateException("Only checked out books can be renewed");
        }

        if (checkout.getRenewalCount() >= checkout.getMaxRenewals()) {
            throw new IllegalStateException("Maximum renewals reached");
        }

        checkout.setDueDate(checkout.getDueDate().plusDays(DEFAULT_CHECKOUT_DAYS));
        checkout.setRenewalCount(checkout.getRenewalCount() + 1);

        LibraryCheckout saved = checkoutRepository.save(checkout);
        log.info("Book renewed. New due date: {}", saved.getDueDate());
        return saved;
    }

    @Override
    @Transactional
    public void markBookLost(Long checkoutId) {
        log.info("Marking book as lost for checkout ID: {}", checkoutId);

        LibraryCheckout checkout = checkoutRepository.findById(checkoutId)
                .orElseThrow(() -> new IllegalArgumentException("Checkout not found with ID: " + checkoutId));

        checkout.setStatus(LibraryCheckout.CheckoutStatus.LOST);
        checkout.setLost(true);
        checkoutRepository.save(checkout);

        // Update book inventory
        LibraryBook book = checkout.getBook();
        book.setCheckedOutCopies(book.getCheckedOutCopies() - 1);
        book.setLostCopies(book.getLostCopies() + 1);
        bookRepository.save(book);

        log.info("Book marked as lost");
    }

    @Override
    @Transactional
    public void markBookDamaged(Long checkoutId, String condition) {
        log.info("Marking book as damaged for checkout ID: {}", checkoutId);

        LibraryCheckout checkout = checkoutRepository.findById(checkoutId)
                .orElseThrow(() -> new IllegalArgumentException("Checkout not found with ID: " + checkoutId));

        checkout.setStatus(LibraryCheckout.CheckoutStatus.DAMAGED);
        checkout.setDamaged(true);
        checkout.setReturnCondition(condition);
        checkoutRepository.save(checkout);

        // Update book inventory
        LibraryBook book = checkout.getBook();
        if (checkout.getReturnDate() == null) {
            book.setCheckedOutCopies(book.getCheckedOutCopies() - 1);
        } else {
            book.setAvailableCopies(book.getAvailableCopies() - 1);
        }
        book.setDamagedCopies(book.getDamagedCopies() + 1);
        bookRepository.save(book);

        log.info("Book marked as damaged");
    }

    // ========================================================================
    // STUDENT CHECKOUT QUERIES
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<LibraryCheckout> getStudentCheckouts(Long studentId) {
        return checkoutRepository.findByStudentIdOrderByCheckoutDateDesc(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LibraryCheckout> getActiveCheckouts(Long studentId) {
        return checkoutRepository.findActiveCheckoutsByStudent(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LibraryCheckout> getOverdueCheckouts(Long studentId) {
        return checkoutRepository.findOverdueCheckoutsByStudent(studentId, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveCheckouts(Long studentId) {
        return checkoutRepository.countActiveCheckoutsByStudent(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCheckout(Long studentId) {
        // Check if student has reached max checkouts
        long activeCheckouts = countActiveCheckouts(studentId);
        if (activeCheckouts >= MAX_CHECKOUTS_PER_STUDENT) {
            return false;
        }

        // Check for outstanding fines
        BigDecimal outstandingFines = getStudentFineBalance(studentId);
        return outstandingFines.compareTo(BigDecimal.ZERO) == 0;
    }

    // ========================================================================
    // FINE MANAGEMENT
    // ========================================================================

    @Override
    @Transactional
    public LibraryFine createFine(Long studentId, Long checkoutId, LibraryFine.FineType fineType,
                                   BigDecimal amount, String reason, String issuedBy) {
        log.info("Creating library fine for student {}: {}", studentId, amount);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        LibraryCheckout checkout = checkoutId != null ? checkoutRepository.findById(checkoutId).orElse(null) : null;

        LibraryFine fine = LibraryFine.builder()
                .student(student)
                .checkout(checkout)
                .fineType(fineType)
                .amount(amount)
                .amountPaid(BigDecimal.ZERO)
                .balance(amount)
                .fineDate(LocalDate.now())
                .status(LibraryFine.FineStatus.UNPAID)
                .reason(reason)
                .issuedBy(issuedBy)
                .build();

        return fineRepository.save(fine);
    }

    @Override
    @Transactional
    public void payFine(Long fineId, BigDecimal amount, String collectedBy) {
        log.info("Processing fine payment {} for fine ID: {}", amount, fineId);

        LibraryFine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new IllegalArgumentException("Fine not found with ID: " + fineId));

        fine.setAmountPaid(fine.getAmountPaid().add(amount));
        fine.setCollectedBy(collectedBy);
        if (fine.getAmountPaid().compareTo(fine.getAmount()) >= 0) {
            fine.setPaidDate(LocalDate.now());
        }
        fineRepository.save(fine);

        log.info("Fine payment processed successfully");
    }

    @Override
    @Transactional
    public void waiveFine(Long fineId, String reason) {
        log.info("Waiving fine ID: {}", fineId);

        LibraryFine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new IllegalArgumentException("Fine not found with ID: " + fineId));

        fine.setWaived(true);
        fine.setWaiverReason(reason);
        fineRepository.save(fine);

        log.info("Fine waived successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<LibraryFine> getStudentFines(Long studentId) {
        return fineRepository.findByStudentIdOrderByFineDateDesc(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LibraryFine> getOutstandingFines(Long studentId) {
        return fineRepository.findOutstandingFinesByStudent(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getStudentFineBalance(Long studentId) {
        BigDecimal balance = fineRepository.calculateOutstandingBalance(studentId);
        return balance != null ? balance : BigDecimal.ZERO;
    }

    // ========================================================================
    // AUTOMATED OPERATIONS
    // ========================================================================

    @Override
    @Transactional
    public void calculateOverdueFines() {
        log.info("Calculating overdue fines");
        List<LibraryCheckout> overdueCheckouts = getAllOverdueCheckouts();

        for (LibraryCheckout checkout : overdueCheckouts) {
            if (checkout.getFineAmount() == null || checkout.getFineAmount().compareTo(BigDecimal.ZERO) == 0) {
                long daysOverdue = checkout.getDaysOverdue();
                BigDecimal fineAmount = OVERDUE_FINE_PER_DAY.multiply(new BigDecimal(daysOverdue));
                if (fineAmount.compareTo(MAX_OVERDUE_FINE) > 0) {
                    fineAmount = MAX_OVERDUE_FINE;
                }
                checkout.setFineAmount(fineAmount);
                checkoutRepository.save(checkout);
            }
        }

        log.info("Overdue fines calculated for {} checkouts", overdueCheckouts.size());
    }

    @Override
    @Transactional
    public void sendOverdueNotices() {
        log.info("Sending overdue notices");
        // This would integrate with notification service
        List<LibraryCheckout> overdueCheckouts = getAllOverdueCheckouts();
        log.info("Would send {} overdue notices", overdueCheckouts.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LibraryCheckout> getAllOverdueCheckouts() {
        return checkoutRepository.findOverdueCheckouts(LocalDate.now());
    }

    // ========================================================================
    // REPORTING
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getLibraryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBooks", bookRepository.countActiveBooks());
        stats.put("totalCopies", bookRepository.countTotalCopies());
        stats.put("checkedOut", bookRepository.countCheckedOutCopies());
        stats.put("available", bookRepository.countTotalCopies() - bookRepository.countCheckedOutCopies());
        stats.put("activeCheckouts", checkoutRepository.countActiveCheckouts());
        stats.put("overdueCheckouts", checkoutRepository.countOverdueCheckouts(LocalDate.now()));
        stats.put("unpaidFines", fineRepository.countUnpaidFines());
        stats.put("totalFinesOutstanding", fineRepository.calculateTotalOutstanding());
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCheckoutReport(LocalDate startDate, LocalDate endDate) {
        List<LibraryCheckout> checkouts = checkoutRepository.findCheckoutsInDateRange(startDate, endDate);

        Map<String, Object> report = new HashMap<>();
        report.put("totalCheckouts", checkouts.size());
        report.put("returned", checkouts.stream().filter(c -> c.getStatus() == LibraryCheckout.CheckoutStatus.RETURNED).count());
        report.put("stillOut", checkouts.stream().filter(c -> c.getStatus() == LibraryCheckout.CheckoutStatus.CHECKED_OUT).count());
        report.put("overdue", checkouts.stream().filter(LibraryCheckout::isOverdue).count());
        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPopularBooks(int limit) {
        List<LibraryBook> allBooks = bookRepository.findByActiveTrue();
        return allBooks.stream()
                .sorted((a, b) -> Integer.compare(b.getCheckedOutCopies(), a.getCheckedOutCopies()))
                .limit(limit)
                .map(book -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("title", book.getTitle());
                    map.put("author", book.getAuthor());
                    map.put("checkoutCount", book.getCheckedOutCopies());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LibraryCheckout> getLostBooks() {
        return checkoutRepository.findLostBooks();
    }
}
