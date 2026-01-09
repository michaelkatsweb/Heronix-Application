package com.heronix.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Report Dashboard Controller
 *
 * Web UI controller for the attendance analytics dashboard.
 *
 * Provides server-side rendering of the dashboard page with initial
 * parameters and metadata.
 *
 * Features:
 * - Dashboard page rendering
 * - Default date range configuration
 * - User-friendly URL routing
 * - Integration with REST API endpoints
 *
 * Routes:
 * - GET /reports/dashboard - Main analytics dashboard view
 * - GET /reports/analytics - Alias for dashboard
 *
 * The dashboard uses client-side JavaScript to fetch data from the
 * ReportAnalyticsApiController REST endpoints and render interactive
 * charts using Chart.js.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 60 - Report Dashboard UI Integration
 */
@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportDashboardController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Display the analytics dashboard
     *
     * Renders the main analytics dashboard page with interactive charts,
     * statistics, and insights.
     *
     * @param startDate Optional start date for initial data range
     * @param endDate Optional end date for initial data range
     * @param model Spring MVC model
     * @return Dashboard view template name
     */
    @GetMapping("/dashboard")
    public String showDashboard(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Model model) {

        log.info("Loading analytics dashboard - startDate: {}, endDate: {}", startDate, endDate);

        // Set default date range if not provided (last 30 days)
        LocalDate defaultStartDate = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate defaultEndDate = endDate != null ? endDate : LocalDate.now();

        // Add dates to model for initial page render
        model.addAttribute("startDate", defaultStartDate.format(DATE_FORMATTER));
        model.addAttribute("endDate", defaultEndDate.format(DATE_FORMATTER));
        model.addAttribute("pageTitle", "Attendance Analytics Dashboard");
        model.addAttribute("currentYear", LocalDate.now().getYear());

        // Calculate date range description
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(defaultStartDate, defaultEndDate);
        String dateRangeDescription;

        if (daysBetween == 6) {
            dateRangeDescription = "Last 7 days";
        } else if (daysBetween == 29 || daysBetween == 30) {
            dateRangeDescription = "Last 30 days";
        } else if (daysBetween == 89 || daysBetween == 90) {
            dateRangeDescription = "Last 90 days";
        } else {
            dateRangeDescription = defaultStartDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) +
                    " - " + defaultEndDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        }

        model.addAttribute("dateRangeDescription", dateRangeDescription);

        log.debug("Dashboard model prepared with date range: {}", dateRangeDescription);

        return "reports/analytics-dashboard";
    }

    /**
     * Analytics dashboard alias
     *
     * Alternative URL path for accessing the analytics dashboard.
     *
     * @param startDate Optional start date
     * @param endDate Optional end date
     * @param model Spring MVC model
     * @return Dashboard view template name
     */
    @GetMapping("/analytics")
    public String showAnalytics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Model model) {

        log.info("Accessing analytics dashboard via /analytics route");
        return showDashboard(startDate, endDate, model);
    }

    /**
     * Dashboard home - redirects to main dashboard
     *
     * Provides a clean redirect from the reports home to the dashboard.
     *
     * @return Redirect to dashboard
     */
    @GetMapping("")
    public String reportsHome() {
        log.info("Redirecting from /reports to /reports/dashboard");
        return "redirect:/reports/dashboard";
    }
}
