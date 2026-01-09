package com.heronix.service.impl;

import com.heronix.model.domain.GradingCategory;
import com.heronix.repository.GradingCategoryRepository;
import com.heronix.service.GradingCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of GradingCategoryService
 * Handles grading category management with weight validation
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 24, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GradingCategoryServiceImpl implements GradingCategoryService {

    private final GradingCategoryRepository categoryRepository;

    @Override
    @Transactional
    public GradingCategory createCategory(GradingCategory category) {
        log.info("Creating grading category: {} for course ID: {}",
                category.getName(), category.getCourse().getId());

        // Validate category
        if (!category.isValid()) {
            throw new IllegalArgumentException("Invalid grading category: " + category.getName());
        }

        GradingCategory saved = categoryRepository.save(category);
        log.info("Grading category created with ID: {}", saved.getId());

        // Check if weights still sum to 100%
        if (!validateWeights(category.getCourse().getId())) {
            log.warn("WARNING: Category weights do not sum to 100% for course ID: {}",
                    category.getCourse().getId());
        }

        return saved;
    }

    @Override
    @Transactional
    public GradingCategory updateCategory(Long id, GradingCategory category) {
        log.info("Updating grading category ID: {}", id);

        GradingCategory existing = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));

        // Update fields
        existing.setName(category.getName());
        existing.setWeight(category.getWeight());
        existing.setDropLowest(category.getDropLowest());
        existing.setCategoryType(category.getCategoryType());
        existing.setColor(category.getColor());
        existing.setDisplayOrder(category.getDisplayOrder());
        existing.setActive(category.getActive());

        // Validate
        if (!existing.isValid()) {
            throw new IllegalArgumentException("Invalid grading category data");
        }

        GradingCategory saved = categoryRepository.save(existing);
        log.info("Grading category updated: {}", saved.getId());

        // Check if weights still sum to 100%
        if (!validateWeights(existing.getCourse().getId())) {
            log.warn("WARNING: Category weights do not sum to 100% for course ID: {}",
                    existing.getCourse().getId());
        }

        return saved;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting grading category ID: {}", id);

        GradingCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));

        Long courseId = category.getCourse().getId();

        categoryRepository.delete(category);
        log.info("Grading category deleted: {}", id);

        // Check if remaining weights sum to 100%
        if (!validateWeights(courseId)) {
            log.warn("WARNING: After deletion, category weights do not sum to 100% for course ID: {}", courseId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradingCategory> getCategoriesByCourse(Long courseId) {
        log.debug("Getting all grading categories for course ID: {}", courseId);
        return categoryRepository.findByCourseIdOrderByDisplayOrder(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateWeights(Long courseId) {
        log.debug("Validating category weights for course ID: {}", courseId);

        Double totalWeight = categoryRepository.getTotalWeightForCourse(courseId);

        if (totalWeight == null) {
            log.debug("No active categories found for course ID: {}", courseId);
            return true; // No categories is valid
        }

        boolean isValid = Math.abs(totalWeight - 100.0) < 0.01; // Allow for rounding errors

        if (!isValid) {
            log.warn("Category weights validation FAILED for course ID: {}. Total weight: {}",
                    courseId, totalWeight);
        } else {
            log.debug("Category weights validation PASSED for course ID: {}", courseId);
        }

        return isValid;
    }

    @Override
    @Transactional
    public void redistributeWeights(Long courseId) {
        log.info("Redistributing category weights for course ID: {}", courseId);

        List<GradingCategory> categories = categoryRepository.findByCourseIdAndActiveTrueOrderByDisplayOrder(courseId);

        if (categories.isEmpty()) {
            log.warn("No active categories to redistribute for course ID: {}", courseId);
            return;
        }

        // Distribute evenly
        double weightPerCategory = 100.0 / categories.size();
        double remainder = 100.0 - (weightPerCategory * categories.size());

        for (int i = 0; i < categories.size(); i++) {
            GradingCategory category = categories.get(i);
            double weight = weightPerCategory;

            // Add remainder to first category to ensure exact 100%
            if (i == 0) {
                weight += remainder;
            }

            category.setWeight(Math.round(weight * 100.0) / 100.0);
            categoryRepository.save(category);
        }

        log.info("Category weights redistributed for course ID: {}. {} categories, {} weight each",
                courseId, categories.size(), weightPerCategory);
    }
}
