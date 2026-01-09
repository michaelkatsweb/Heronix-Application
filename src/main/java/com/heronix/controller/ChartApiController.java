package com.heronix.controller;

import com.heronix.dto.ChartConfig;
import com.heronix.service.ChartDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Chart API Controller
 *
 * REST API endpoints for generating chart configurations and data.
 *
 * Provides Endpoints For:
 * - Pre-built chart types (trend, distribution, comparison)
 * - Custom chart generation
 * - Chart data retrieval
 * - Chart configuration management
 *
 * Chart Types Available:
 * - Daily attendance trend (line chart)
 * - Status distribution (pie chart)
 * - Grade-level comparison (bar chart)
 * - Weekly patterns (area chart)
 * - Custom configurations (any type)
 *
 * Response Format:
 * Returns ChartConfig JSON that can be consumed by:
 * - Chart.js (JavaScript)
 * - Server-side rendering
 * - Export to image/PDF
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 66 - Report Data Visualization & Charts
 */
@RestController
@RequestMapping("/api/charts")
@RequiredArgsConstructor
@Slf4j
public class ChartApiController {

    private final ChartDataService chartDataService;

    /**
     * Generate daily attendance trend chart
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Chart configuration
     */
    @GetMapping("/daily-trend")
    public ResponseEntity<ChartConfig> getDailyTrendChart(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("GET /api/charts/daily-trend - startDate: {}, endDate: {}", startDate, endDate);

        try {
            ChartConfig chart = chartDataService.generateDailyTrendChart(startDate, endDate);
            return ResponseEntity.ok(chart);

        } catch (Exception e) {
            log.error("Error generating daily trend chart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate status distribution pie chart
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Chart configuration
     */
    @GetMapping("/status-distribution")
    public ResponseEntity<ChartConfig> getStatusDistributionChart(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("GET /api/charts/status-distribution - startDate: {}, endDate: {}", startDate, endDate);

        try {
            ChartConfig chart = chartDataService.generateStatusDistributionChart(startDate, endDate);
            return ResponseEntity.ok(chart);

        } catch (Exception e) {
            log.error("Error generating status distribution chart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate grade-level comparison bar chart
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Chart configuration
     */
    @GetMapping("/grade-comparison")
    public ResponseEntity<ChartConfig> getGradeLevelComparisonChart(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("GET /api/charts/grade-comparison - startDate: {}, endDate: {}", startDate, endDate);

        try {
            ChartConfig chart = chartDataService.generateGradeLevelComparisonChart(startDate, endDate);
            return ResponseEntity.ok(chart);

        } catch (Exception e) {
            log.error("Error generating grade comparison chart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate weekly attendance pattern chart
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Chart configuration
     */
    @GetMapping("/weekly-pattern")
    public ResponseEntity<ChartConfig> getWeeklyPatternChart(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("GET /api/charts/weekly-pattern - startDate: {}, endDate: {}", startDate, endDate);

        try {
            ChartConfig chart = chartDataService.generateWeeklyPatternChart(startDate, endDate);
            return ResponseEntity.ok(chart);

        } catch (Exception e) {
            log.error("Error generating weekly pattern chart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate custom chart from configuration
     *
     * @param config Chart configuration
     * @return Generated chart configuration
     */
    @PostMapping("/custom")
    public ResponseEntity<ChartConfig> generateCustomChart(@RequestBody ChartConfig config) {
        log.info("POST /api/charts/custom - type: {}", config.getChartType());

        try {
            ChartConfig chart = chartDataService.generateCustomChart(config);
            return ResponseEntity.status(HttpStatus.CREATED).body(chart);

        } catch (IllegalArgumentException e) {
            log.error("Invalid chart configuration: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error generating custom chart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get available chart themes
     *
     * @return List of theme names
     */
    @GetMapping("/themes")
    public ResponseEntity<String[]> getAvailableThemes() {
        log.info("GET /api/charts/themes");

        try {
            ChartConfig.ChartTheme[] themes = ChartConfig.ChartTheme.values();
            String[] themeNames = new String[themes.length];
            for (int i = 0; i < themes.length; i++) {
                themeNames[i] = themes[i].name();
            }

            return ResponseEntity.ok(themeNames);

        } catch (Exception e) {
            log.error("Error fetching chart themes", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get color palette for theme
     *
     * @param theme Theme name
     * @return Color palette (hex codes)
     */
    @GetMapping("/themes/{theme}/colors")
    public ResponseEntity<java.util.List<String>> getThemeColors(@PathVariable String theme) {
        log.info("GET /api/charts/themes/{}/colors", theme);

        try {
            ChartConfig.ChartTheme chartTheme = ChartConfig.ChartTheme.valueOf(theme.toUpperCase());

            ChartConfig dummyConfig = ChartConfig.builder()
                    .theme(chartTheme)
                    .build();

            java.util.List<String> colors = dummyConfig.getColorPalette();
            return ResponseEntity.ok(colors);

        } catch (IllegalArgumentException e) {
            log.error("Invalid theme name: {}", theme);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error fetching theme colors", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get available chart types
     *
     * @return List of chart type names
     */
    @GetMapping("/types")
    public ResponseEntity<String[]> getAvailableChartTypes() {
        log.info("GET /api/charts/types");

        try {
            ChartConfig.ChartType[] types = ChartConfig.ChartType.values();
            String[] typeNames = new String[types.length];
            for (int i = 0; i < types.length; i++) {
                typeNames[i] = types[i].name();
            }

            return ResponseEntity.ok(typeNames);

        } catch (Exception e) {
            log.error("Error fetching chart types", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
