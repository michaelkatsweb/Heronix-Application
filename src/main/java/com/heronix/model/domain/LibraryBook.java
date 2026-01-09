package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a book in the library catalog
 * Location: src/main/java/com/heronix/model/domain/LibraryBook.java
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "library_books", indexes = {
    @Index(name = "idx_isbn", columnList = "isbn"),
    @Index(name = "idx_title", columnList = "title"),
    @Index(name = "idx_author", columnList = "author")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20, unique = true)
    private String isbn;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 300)
    private String author;

    @Column(length = 200)
    private String publisher;

    @Column
    private Integer publicationYear;

    @Column(length = 100)
    private String genre;

    @Column(length = 100)
    private String subject;

    @Column
    private Integer totalCopies;

    @Column
    private Integer availableCopies;

    @Column
    private Integer checkedOutCopies;

    @Column
    private Integer lostCopies;

    @Column
    private Integer damagedCopies;

    @Column(length = 50)
    private String deweyDecimal;

    @Column(length = 50)
    private String callNumber;

    @Column(length = 100)
    private String location; // Shelf location

    @Column(length = 2000)
    private String description;

    @Column
    private Integer pages;

    @Column(length = 50)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private BookType bookType;

    @Column(length = 50)
    private String gradeLevel; // Recommended grade level

    @Column
    private Boolean reference; // Reference books cannot be checked out

    @Column
    private Boolean active;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) active = true;
        if (reference == null) reference = false;
        if (availableCopies == null) availableCopies = totalCopies;
        if (checkedOutCopies == null) checkedOutCopies = 0;
        if (lostCopies == null) lostCopies = 0;
        if (damagedCopies == null) damagedCopies = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum BookType {
        FICTION,
        NON_FICTION,
        BIOGRAPHY,
        REFERENCE,
        TEXTBOOK,
        PERIODICAL,
        GRAPHIC_NOVEL,
        AUDIO_BOOK,
        E_BOOK,
        DVD,
        OTHER
    }
}
