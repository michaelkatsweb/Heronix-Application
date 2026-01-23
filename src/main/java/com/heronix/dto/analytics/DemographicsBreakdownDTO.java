package com.heronix.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Demographics breakdown for student population analysis
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Analytics Module
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemographicsBreakdownDTO {

    private Long totalStudents;
    private String campusName;
    private Long campusId;

    // Gender Distribution
    private Map<String, Long> genderDistribution;
    private Map<String, Double> genderPercentages;

    // Ethnicity Distribution
    private Map<String, Long> ethnicityDistribution;
    private Map<String, Double> ethnicityPercentages;

    // Race Distribution
    private Map<String, Long> raceDistribution;
    private Map<String, Double> racePercentages;

    // Language Distribution
    private Map<String, Long> languageDistribution;
    private Map<String, Double> languagePercentages;

    // Special Programs
    private Long iepCount;
    private Double iepPercentage;
    private Long plan504Count;
    private Double plan504Percentage;
    private Long giftedCount;
    private Double giftedPercentage;
    private Long ellCount;
    private Double ellPercentage;

    // Economic Indicators (if available)
    private Long freeReducedLunchCount;
    private Double freeReducedLunchPercentage;

    // Living Situation
    private Map<String, Long> livingSituationDistribution;
    private Long fosterCareCount;
    private Long homelessCount;
    private Long militaryFamilyCount;

    /**
     * Category breakdown for pie/bar charts
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private String category;
        private Long count;
        private Double percentage;
        private String colorHex;
    }

    /**
     * Comparison across campuses
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampusDemographics {
        private Long campusId;
        private String campusName;
        private Long totalStudents;
        private List<CategoryBreakdown> genderBreakdown;
        private List<CategoryBreakdown> ethnicityBreakdown;
        private Double iepPercentage;
        private Double ellPercentage;
    }

    /**
     * Special needs breakdown summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecialNeedsBreakdown {
        private Long iepCount;
        private Long plan504Count;
        private Long giftedCount;
        private Long ellCount;
        private Long totalSpecialNeeds;
        private Double percentOfTotal;
    }

    /**
     * Build category breakdown list from map
     */
    public List<CategoryBreakdown> getGenderBreakdownList() {
        return buildBreakdownList(genderDistribution, genderPercentages);
    }

    public List<CategoryBreakdown> getEthnicityBreakdownList() {
        return buildBreakdownList(ethnicityDistribution, ethnicityPercentages);
    }

    public List<CategoryBreakdown> getRaceBreakdownList() {
        return buildBreakdownList(raceDistribution, racePercentages);
    }

    public List<CategoryBreakdown> getLanguageBreakdownList() {
        return buildBreakdownList(languageDistribution, languagePercentages);
    }

    private List<CategoryBreakdown> buildBreakdownList(Map<String, Long> counts, Map<String, Double> percentages) {
        if (counts == null) return List.of();

        return counts.entrySet().stream()
                .map(e -> CategoryBreakdown.builder()
                        .category(e.getKey())
                        .count(e.getValue())
                        .percentage(percentages != null ? percentages.getOrDefault(e.getKey(), 0.0) : 0.0)
                        .build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .toList();
    }
}
