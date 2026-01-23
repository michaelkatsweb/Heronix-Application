package com.heronix.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Chart and data export configuration
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Analytics Module
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartExportRequestDTO {

    private String chartTitle;
    private String chartSubtitle;
    private ExportFormat format;
    private ExportType exportType;

    // Image export settings
    private Integer width;
    private Integer height;
    private Integer dpi;
    private Boolean includeTitle;
    private Boolean includeLegend;
    private Boolean includeTimestamp;

    // Data export settings
    private List<String> columns;
    private Map<String, String> columnHeaders;
    private Boolean includeHeaders;
    private String dateFormat;
    private String numberFormat;

    // PDF specific
    private PageOrientation orientation;
    private PageSize pageSize;
    private Boolean includePageNumbers;
    private String headerText;
    private String footerText;

    // Filters applied (for reference in export)
    private AnalyticsFilterDTO appliedFilters;

    public enum ExportFormat {
        PNG,
        PDF,
        CSV,
        EXCEL,
        JSON
    }

    public enum ExportType {
        CHART_IMAGE,
        CHART_WITH_DATA,
        DATA_ONLY,
        SUMMARY_REPORT,
        FULL_REPORT
    }

    public enum PageOrientation {
        PORTRAIT,
        LANDSCAPE
    }

    public enum PageSize {
        LETTER,
        A4,
        LEGAL
    }

    /**
     * Default PNG export settings
     */
    public static ChartExportRequestDTO pngExport(String title) {
        return ChartExportRequestDTO.builder()
                .chartTitle(title)
                .format(ExportFormat.PNG)
                .exportType(ExportType.CHART_IMAGE)
                .width(1920)
                .height(1080)
                .dpi(150)
                .includeTitle(true)
                .includeLegend(true)
                .includeTimestamp(true)
                .build();
    }

    /**
     * Default PDF export settings
     */
    public static ChartExportRequestDTO pdfExport(String title, String subtitle) {
        return ChartExportRequestDTO.builder()
                .chartTitle(title)
                .chartSubtitle(subtitle)
                .format(ExportFormat.PDF)
                .exportType(ExportType.CHART_WITH_DATA)
                .width(1200)
                .height(800)
                .orientation(PageOrientation.LANDSCAPE)
                .pageSize(PageSize.LETTER)
                .includeTitle(true)
                .includeLegend(true)
                .includeTimestamp(true)
                .includePageNumbers(true)
                .build();
    }

    /**
     * Default CSV export settings
     */
    public static ChartExportRequestDTO csvExport(List<String> columns) {
        return ChartExportRequestDTO.builder()
                .format(ExportFormat.CSV)
                .exportType(ExportType.DATA_ONLY)
                .columns(columns)
                .includeHeaders(true)
                .dateFormat("yyyy-MM-dd")
                .numberFormat("#,##0.00")
                .build();
    }

    /**
     * Default Excel export settings
     */
    public static ChartExportRequestDTO excelExport(List<String> columns, Map<String, String> headers) {
        return ChartExportRequestDTO.builder()
                .format(ExportFormat.EXCEL)
                .exportType(ExportType.DATA_ONLY)
                .columns(columns)
                .columnHeaders(headers)
                .includeHeaders(true)
                .dateFormat("yyyy-MM-dd")
                .numberFormat("#,##0.00")
                .build();
    }
}
