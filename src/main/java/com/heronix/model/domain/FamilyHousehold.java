package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Family Household Entity
 *
 * Represents a family unit with multiple children enrolled in the school.
 * Tracks sibling relationships, shared parent/guardian information,
 * and family-wide discounts and services.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Entity
@Table(name = "family_households")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyHousehold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // FAMILY IDENTIFICATION
    // ========================================================================

    @Column(nullable = false, unique = true, length = 50)
    private String familyId; // e.g., "FAM-2025-001234"

    @Column(nullable = false, length = 200)
    private String familyName; // e.g., "Smith Family"

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private HouseholdType householdType;

    @Column(length = 500)
    private String primaryAddress;

    @Column(length = 100)
    private String primaryCity;

    @Column(length = 20)
    private String primaryState;

    @Column(length = 20)
    private String primaryZipCode;

    @Column(length = 100)
    private String primaryCounty;

    // ========================================================================
    // PRIMARY CONTACTS
    // ========================================================================

    @Column(length = 200)
    private String primaryParentName;

    @Column(length = 20)
    private String primaryPhone;

    @Column(length = 100)
    private String primaryEmail;

    @Column(length = 50)
    private String secondaryPhone;

    @Column(length = 100)
    private String secondaryEmail;

    // ========================================================================
    // PARENT/GUARDIAN 1
    // ========================================================================

    @Column(length = 200)
    private String parent1Name;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private RelationshipType parent1Relationship;

    @Column(length = 20)
    private String parent1Phone;

    @Column(length = 100)
    private String parent1Email;

    @Column(length = 20)
    private String parent1WorkPhone;

    @Column(length = 200)
    private String parent1Employer;

    @Column
    private Boolean parent1LivesInHousehold;

    @Column
    private Boolean parent1IsCustodial;

    // ========================================================================
    // PARENT/GUARDIAN 2
    // ========================================================================

    @Column(length = 200)
    private String parent2Name;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private RelationshipType parent2Relationship;

    @Column(length = 20)
    private String parent2Phone;

    @Column(length = 100)
    private String parent2Email;

    @Column(length = 20)
    private String parent2WorkPhone;

    @Column(length = 200)
    private String parent2Employer;

    @Column
    private Boolean parent2LivesInHousehold;

    @Column
    private Boolean parent2IsCustodial;

    // ========================================================================
    // EMERGENCY CONTACTS
    // ========================================================================

    @Column(length = 200)
    private String emergency1Name;

    @Column(length = 20)
    private String emergency1Phone;

    @Column(length = 100)
    private String emergency1Relation;

    @Column(length = 200)
    private String emergency2Name;

    @Column(length = 20)
    private String emergency2Phone;

    @Column(length = 100)
    private String emergency2Relation;

    @Column(length = 200)
    private String emergency3Name;

    @Column(length = 20)
    private String emergency3Phone;

    @Column(length = 100)
    private String emergency3Relation;

    // ========================================================================
    // CHILDREN/STUDENTS
    // ========================================================================

    @OneToMany(mappedBy = "familyHousehold", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Student> children = new ArrayList<>();

    @Column
    private Integer totalChildren;

    @Column
    private Integer enrolledChildren;

    @Column
    private Integer pendingChildren;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_student_id")
    private Student primaryStudent; // Main contact student

    // ========================================================================
    // FAMILY DISCOUNTS
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private DiscountType discountType;

    @Column
    private Double baseDiscountPercent; // Base family discount percentage

    @Column
    private Double siblingDiscountAmount; // Total sibling discount in dollars

    @Column
    private Double totalFamilyDiscount; // Total discount applied to family

    @Column
    private Boolean discount2ndChild;

    @Column
    private Boolean discount3rdPlusChildren;

    @Column
    private Boolean earlyBirdDiscountApplied;

    @Column
    private Boolean waiveTechFees3rdPlus;

    @Column(length = 1000)
    private String discountNotes;

    // ========================================================================
    // CUSTODY ARRANGEMENT
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private CustodyArrangement custodyArrangement;

    @Column
    private Boolean custodyPapersOnFile;

    @Column(length = 1000)
    private String pickupRestrictions;

    @Column(length = 2000)
    private String specialFamilyNotes;

    // ========================================================================
    // FAMILY STATUS
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FamilyStatus status;

    @Column
    private Boolean isActive;

    @Column(length = 2000)
    private String familyNotes;

    // ========================================================================
    // AUDIT FIELDS
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_staff_id", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_staff_id")
    private User updatedBy;

    @Column
    private LocalDateTime updatedAt;

    // ========================================================================
    // ENUMS
    // ========================================================================

    public enum HouseholdType {
        TWO_PARENT("Two-Parent Household"),
        SINGLE_PARENT("Single-Parent Household"),
        BLENDED_FAMILY("Blended Family"),
        GRANDPARENT_GUARDIAN("Grandparent Guardian"),
        FOSTER_FAMILY("Foster Family"),
        OTHER("Other");

        private final String displayName;

        HouseholdType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum RelationshipType {
        MOTHER("Mother"),
        FATHER("Father"),
        STEPMOTHER("Stepmother"),
        STEPFATHER("Stepfather"),
        GRANDMOTHER("Grandmother"),
        GRANDFATHER("Grandfather"),
        LEGAL_GUARDIAN("Legal Guardian"),
        FOSTER_PARENT("Foster Parent"),
        OTHER("Other");

        private final String displayName;

        RelationshipType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DiscountType {
        STANDARD_SIBLING("Standard Sibling Discount"),
        MILITARY_FAMILY("Military Family Discount"),
        STAFF_FAMILY("Staff Family Discount"),
        NEED_BASED("Need-Based Discount"),
        CUSTOM("Custom Discount");

        private final String displayName;

        DiscountType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum CustodyArrangement {
        JOINT_CUSTODY("Joint Custody"),
        SOLE_CUSTODY_PARENT1("Sole Custody - Parent 1"),
        SOLE_CUSTODY_PARENT2("Sole Custody - Parent 2"),
        SHARED_CUSTODY_50_50("Shared Custody (50/50)"),
        PRIMARY_WITH_VISITATION("Primary Custody with Visitation"),
        OTHER("Other");

        private final String displayName;

        CustodyArrangement(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum FamilyStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        ARCHIVED("Archived");

        private final String displayName;

        FamilyStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Add a student to the family household
     */
    public void addStudent(Student student) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(student);
        student.setFamilyHousehold(this);
        updateChildrenCounts();
    }

    /**
     * Remove a student from the family household
     */
    public void removeStudent(Student student) {
        if (children != null) {
            children.remove(student);
            student.setFamilyHousehold(null);
            updateChildrenCounts();
        }
    }

    /**
     * Update children counts
     */
    public void updateChildrenCounts() {
        if (children != null) {
            totalChildren = children.size();
            enrolledChildren = (int) children.stream()
                    .filter(s -> "ACTIVE".equals(s.getGradeLevel())) // TODO: Check actual enrollment status
                    .count();
            pendingChildren = totalChildren - enrolledChildren;
        }
    }

    /**
     * Calculate total family discount based on number of children
     */
    public void calculateDiscounts() {
        double totalDiscount = 0.0;

        if (enrolledChildren != null && enrolledChildren > 0) {
            // Apply 2nd child discount
            if (Boolean.TRUE.equals(discount2ndChild) && enrolledChildren >= 2) {
                totalDiscount += 50.0; // $50 for 2nd child
            }

            // Apply 3rd+ children discount
            if (Boolean.TRUE.equals(discount3rdPlusChildren) && enrolledChildren >= 3) {
                totalDiscount += (enrolledChildren - 2) * 75.0; // $75 per additional child
            }

            // Apply early bird discount if applicable
            if (Boolean.TRUE.equals(earlyBirdDiscountApplied)) {
                totalDiscount += 25.0 * enrolledChildren; // $25 per child
            }
        }

        this.siblingDiscountAmount = totalDiscount;
        this.totalFamilyDiscount = totalDiscount;
    }

    /**
     * Check if family is complete (all required fields filled)
     */
    public boolean isComplete() {
        return familyName != null &&
               primaryParentName != null &&
               primaryPhone != null &&
               primaryEmail != null &&
               primaryAddress != null;
    }
}
