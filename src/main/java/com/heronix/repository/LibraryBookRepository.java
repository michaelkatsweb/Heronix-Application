package com.heronix.repository;

import com.heronix.model.domain.LibraryBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for LibraryBook entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface LibraryBookRepository extends JpaRepository<LibraryBook, Long> {

    Optional<LibraryBook> findByIsbn(String isbn);

    List<LibraryBook> findByActiveTrue();

    List<LibraryBook> findByActiveTrueOrderByTitleAsc();

    @Query("SELECT b FROM LibraryBook b WHERE b.active = true AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<LibraryBook> searchBooks(@Param("searchTerm") String searchTerm);

    List<LibraryBook> findByGenreAndActiveTrue(String genre);

    List<LibraryBook> findBySubjectAndActiveTrue(String subject);

    List<LibraryBook> findByAuthorAndActiveTrue(String author);

    List<LibraryBook> findByBookTypeAndActiveTrue(LibraryBook.BookType bookType);

    List<LibraryBook> findByGradeLevelAndActiveTrue(String gradeLevel);

    @Query("SELECT b FROM LibraryBook b WHERE b.active = true AND b.availableCopies > 0 ORDER BY b.title")
    List<LibraryBook> findAvailableBooks();

    @Query("SELECT b FROM LibraryBook b WHERE b.active = true AND b.reference = true ORDER BY b.title")
    List<LibraryBook> findReferenceBooks();

    @Query("SELECT COUNT(b) FROM LibraryBook b WHERE b.active = true")
    long countActiveBooks();

    @Query("SELECT COALESCE(SUM(b.totalCopies), 0) FROM LibraryBook b WHERE b.active = true")
    int countTotalCopies();

    @Query("SELECT COALESCE(SUM(b.checkedOutCopies), 0) FROM LibraryBook b WHERE b.active = true")
    int countCheckedOutCopies();
}
