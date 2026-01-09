package com.heronix.service;

import com.heronix.model.domain.GradingCategory;
import java.util.List;

/**
 * Service interface for GradingCategory operations
 * 
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 24, 2025
 */
public interface GradingCategoryService {
    GradingCategory createCategory(GradingCategory category);
    GradingCategory updateCategory(Long id, GradingCategory category);
    void deleteCategory(Long id);
    List<GradingCategory> getCategoriesByCourse(Long courseId);
    boolean validateWeights(Long courseId);
    void redistributeWeights(Long courseId);
}
