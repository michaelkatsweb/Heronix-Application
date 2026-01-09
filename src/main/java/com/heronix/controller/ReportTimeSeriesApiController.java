package com.heronix.controller;

import com.heronix.dto.ReportTimeSeries;
import com.heronix.service.ReportTimeSeriesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Report Time-Series API Controller
 *
 * REST API endpoints for time-series database management, temporal data analysis,
 * forecasting, anomaly detection, and trend analysis.
 *
 * Endpoints:
 * - POST /api/timeseries - Create time-series database
 * - GET /api/timeseries/{id} - Get time-series database
 * - POST /api/timeseries/{id}/deploy - Deploy database
 * - POST /api/timeseries/{id}/measurement - Create measurement
 * - POST /api/timeseries/{id}/datapoint - Ingest data point
 * - POST /api/timeseries/{id}/series - Create time series
 * - POST /api/timeseries/{id}/query - Execute query
 * - POST /api/timeseries/{id}/aggregation - Create aggregation
 * - POST /api/timeseries/{id}/forecast - Create forecast
 * - POST /api/timeseries/{id}/anomaly - Detect anomaly
 * - POST /api/timeseries/{id}/trend - Detect trend
 * - POST /api/timeseries/{id}/window - Create time window
 * - POST /api/timeseries/{id}/downsample - Create downsample
 * - DELETE /api/timeseries/{id} - Delete database
 * - GET /api/timeseries/stats - Get statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 126 - Report Time-Series Database & Analytics
 */
@RestController
@RequestMapping("/api/timeseries")
@RequiredArgsConstructor
@Slf4j
public class ReportTimeSeriesApiController {

    private final ReportTimeSeriesService timeSeriesService;

    /**
     * Create time-series database
     */
    @PostMapping
    public ResponseEntity<ReportTimeSeries> createTimeSeriesDatabase(@RequestBody ReportTimeSeries timeSeries) {
        log.info("POST /api/timeseries - Creating time-series database: {}", timeSeries.getDatabaseName());

        try {
            ReportTimeSeries created = timeSeriesService.createTimeSeriesDatabase(timeSeries);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            log.error("Error creating time-series database", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get time-series database
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportTimeSeries> getTimeSeriesDatabase(@PathVariable Long id) {
        log.info("GET /api/timeseries/{}", id);

        try {
            return timeSeriesService.getTimeSeriesDatabase(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error fetching time-series database: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Deploy time-series database
     */
    @PostMapping("/{id}/deploy")
    public ResponseEntity<Map<String, Object>> deployTimeSeriesDatabase(@PathVariable Long id) {
        log.info("POST /api/timeseries/{}/deploy", id);

        try {
            timeSeriesService.deployTimeSeriesDatabase(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Time-series database deployed");
            response.put("timeSeriesId", id);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Time-series database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error deploying time-series database: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create measurement
     */
    @PostMapping("/{id}/measurement")
    public ResponseEntity<ReportTimeSeries.Measurement> createMeasurement(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/timeseries/{}/measurement", id);

        try {
            String measurementName = (String) request.get("measurementName");
            String description = (String) request.get("description");
            String unit = (String) request.get("unit");
            String dataType = (String) request.get("dataType");
            @SuppressWarnings("unchecked")
            Map<String, String> tags = (Map<String, String>) request.get("tags");

            ReportTimeSeries.Measurement measurement = timeSeriesService.createMeasurement(
                    id, measurementName, description, unit, dataType, tags
            );

            return ResponseEntity.ok(measurement);

        } catch (IllegalArgumentException e) {
            log.error("Time-series database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating measurement: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Ingest data point
     */
    @PostMapping("/{id}/datapoint")
    public ResponseEntity<ReportTimeSeries.DataPoint> ingestDataPoint(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/timeseries/{}/datapoint", id);

        try {
            String measurementId = (String) request.get("measurementId");
            String timestampStr = (String) request.get("timestamp");
            LocalDateTime timestamp = timestampStr != null ? LocalDateTime.parse(timestampStr) : LocalDateTime.now();
            @SuppressWarnings("unchecked")
            Map<String, Object> fields = (Map<String, Object>) request.get("fields");
            @SuppressWarnings("unchecked")
            Map<String, String> tags = (Map<String, String>) request.get("tags");
            Double value = request.get("value") != null ?
                    ((Number) request.get("value")).doubleValue() : null;

            ReportTimeSeries.DataPoint dataPoint = timeSeriesService.ingestDataPoint(
                    id, measurementId, timestamp, fields, tags, value
            );

            return ResponseEntity.ok(dataPoint);

        } catch (IllegalArgumentException e) {
            log.error("Time-series database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error ingesting data point: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create time series
     */
    @PostMapping("/{id}/series")
    public ResponseEntity<ReportTimeSeries.TimeSeries> createTimeSeries(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/timeseries/{}/series", id);

        try {
            String seriesName = (String) request.get("seriesName");
            String measurementId = (String) request.get("measurementId");
            @SuppressWarnings("unchecked")
            Map<String, String> tags = (Map<String, String>) request.get("tags");
            String startTimeStr = (String) request.get("startTime");
            String endTimeStr = (String) request.get("endTime");
            LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : LocalDateTime.now().minusDays(7);
            LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : LocalDateTime.now();
            String granularity = (String) request.get("granularity");

            ReportTimeSeries.TimeSeries series = timeSeriesService.createTimeSeries(
                    id, seriesName, measurementId, tags, startTime, endTime, granularity
            );

            return ResponseEntity.ok(series);

        } catch (IllegalArgumentException e) {
            log.error("Time-series database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating time series: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Execute query
     */
    @PostMapping("/{id}/query")
    public ResponseEntity<ReportTimeSeries.TimeSeriesQuery> executeQuery(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/timeseries/{}/query", id);

        try {
            String queryName = (String) request.get("queryName");
            String queryLanguage = (String) request.get("queryLanguage");
            String queryString = (String) request.get("queryString");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) request.get("parameters");
            String startTimeStr = (String) request.get("startTime");
            String endTimeStr = (String) request.get("endTime");
            LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null;
            LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null;
            String executedBy = (String) request.get("executedBy");

            ReportTimeSeries.TimeSeriesQuery query = timeSeriesService.executeQuery(
                    id, queryName, queryLanguage, queryString, parameters, startTime, endTime, executedBy
            );

            return ResponseEntity.ok(query);

        } catch (IllegalArgumentException e) {
            log.error("Time-series database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error executing query: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create aggregation
     */
    @PostMapping("/{id}/aggregation")
    public ResponseEntity<ReportTimeSeries.Aggregation> createAggregation(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/timeseries/{}/aggregation", id);

        try {
            String aggregationName = (String) request.get("aggregationName");
            String measurementId = (String) request.get("measurementId");
            String functionStr = (String) request.get("function");
            String interval = (String) request.get("interval");
            String startTimeStr = (String) request.get("startTime");
            String endTimeStr = (String) request.get("endTime");
            LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : LocalDateTime.now().minusDays(1);
            LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : LocalDateTime.now();
            @SuppressWarnings("unchecked")
            Map<String, String> groupBy = (Map<String, String>) request.get("groupBy");

            ReportTimeSeries.AggregationFunction function =
                    ReportTimeSeries.AggregationFunction.valueOf(functionStr);

            ReportTimeSeries.Aggregation aggregation = timeSeriesService.createAggregation(
                    id, aggregationName, measurementId, function, interval, startTime, endTime, groupBy
            );

            return ResponseEntity.ok(aggregation);

        } catch (IllegalArgumentException e) {
            log.error("Time-series database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating aggregation: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create forecast
     */
    @PostMapping("/{id}/forecast")
    public ResponseEntity<ReportTimeSeries.Forecast> createForecast(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/timeseries/{}/forecast", id);

        try {
            String forecastName = (String) request.get("forecastName");
            String measurementId = (String) request.get("measurementId");
            String modelStr = (String) request.get("model");
            String trainingStartTimeStr = (String) request.get("trainingStartTime");
            String trainingEndTimeStr = (String) request.get("trainingEndTime");
            LocalDateTime trainingStartTime = trainingStartTimeStr != null ?
                    LocalDateTime.parse(trainingStartTimeStr) : LocalDateTime.now().minusDays(30);
            LocalDateTime trainingEndTime = trainingEndTimeStr != null ?
                    LocalDateTime.parse(trainingEndTimeStr) : LocalDateTime.now();
            Integer forecastHorizon = request.get("forecastHorizon") != null ?
                    ((Number) request.get("forecastHorizon")).intValue() : 24;
            String createdBy = (String) request.get("createdBy");

            ReportTimeSeries.ForecastModel model = ReportTimeSeries.ForecastModel.valueOf(modelStr);

            ReportTimeSeries.Forecast forecast = timeSeriesService.createForecast(
                    id, forecastName, measurementId, model, trainingStartTime, trainingEndTime,
                    forecastHorizon, createdBy
            );

            return ResponseEntity.ok(forecast);

        } catch (IllegalArgumentException e) {
            log.error("Time-series database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating forecast: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Detect anomaly
     */
    @PostMapping("/{id}/anomaly")
    public ResponseEntity<ReportTimeSeries.Anomaly> detectAnomaly(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/timeseries/{}/anomaly", id);

        try {
            String measurementId = (String) request.get("measurementId");
            String detectionMethodStr = (String) request.get("detectionMethod");
            String timestampStr = (String) request.get("timestamp");
            LocalDateTime timestamp = timestampStr != null ? LocalDateTime.parse(timestampStr) : LocalDateTime.now();
            Double actualValue = ((Number) request.get("actualValue")).doubleValue();
            Double expectedValue = ((Number) request.get("expectedValue")).doubleValue();

            ReportTimeSeries.AnomalyDetectionMethod detectionMethod =
                    ReportTimeSeries.AnomalyDetectionMethod.valueOf(detectionMethodStr);

            ReportTimeSeries.Anomaly anomaly = timeSeriesService.detectAnomaly(
                    id, measurementId, detectionMethod, timestamp, actualValue, expectedValue
            );

            return ResponseEntity.ok(anomaly);

        } catch (IllegalArgumentException e) {
            log.error("Time-series database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error detecting anomaly: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Detect trend
     */
    @PostMapping("/{id}/trend")
    public ResponseEntity<ReportTimeSeries.Trend> detectTrend(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/timeseries/{}/trend", id);

        try {
            String trendName = (String) request.get("trendName");
            String measurementId = (String) request.get("measurementId");
            String trendType = (String) request.get("trendType");
            String startTimeStr = (String) request.get("startTime");
            String endTimeStr = (String) request.get("endTime");
            LocalDateTime startTime = startTimeStr != null ? LocalDateTime.parse(startTimeStr) : LocalDateTime.now().minusDays(7);
            LocalDateTime endTime = endTimeStr != null ? LocalDateTime.parse(endTimeStr) : LocalDateTime.now();
            Double slope = request.get("slope") != null ? ((Number) request.get("slope")).doubleValue() : 0.0;
            Double changeRate = request.get("changeRate") != null ?
                    ((Number) request.get("changeRate")).doubleValue() : 0.0;

            ReportTimeSeries.Trend trend = timeSeriesService.detectTrend(
                    id, trendName, measurementId, trendType, startTime, endTime, slope, changeRate
            );

            return ResponseEntity.ok(trend);

        } catch (IllegalArgumentException e) {
            log.error("Time-series database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error detecting trend: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create time window
     */
    @PostMapping("/{id}/window")
    public ResponseEntity<ReportTimeSeries.TimeWindow> createTimeWindow(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/timeseries/{}/window", id);

        try {
            String windowName = (String) request.get("windowName");
            String windowType = (String) request.get("windowType");
            String measurementId = (String) request.get("measurementId");
            String duration = (String) request.get("duration");
            String slide = (String) request.get("slide");
            String aggregationFunctionStr = (String) request.get("aggregationFunction");

            ReportTimeSeries.AggregationFunction aggregationFunction =
                    ReportTimeSeries.AggregationFunction.valueOf(aggregationFunctionStr);

            ReportTimeSeries.TimeWindow timeWindow = timeSeriesService.createTimeWindow(
                    id, windowName, windowType, measurementId, duration, slide, aggregationFunction
            );

            return ResponseEntity.ok(timeWindow);

        } catch (IllegalArgumentException e) {
            log.error("Time-series database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating time window: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create downsample
     */
    @PostMapping("/{id}/downsample")
    public ResponseEntity<ReportTimeSeries.Downsample> createDownsample(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/timeseries/{}/downsample", id);

        try {
            String downsampleName = (String) request.get("downsampleName");
            String sourceMeasurementId = (String) request.get("sourceMeasurementId");
            String targetMeasurementId = (String) request.get("targetMeasurementId");
            String aggregationFunctionStr = (String) request.get("aggregationFunction");
            String interval = (String) request.get("interval");

            ReportTimeSeries.AggregationFunction aggregationFunction =
                    ReportTimeSeries.AggregationFunction.valueOf(aggregationFunctionStr);

            ReportTimeSeries.Downsample downsample = timeSeriesService.createDownsample(
                    id, downsampleName, sourceMeasurementId, targetMeasurementId, aggregationFunction, interval
            );

            return ResponseEntity.ok(downsample);

        } catch (IllegalArgumentException e) {
            log.error("Time-series database not found: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error creating downsample: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete time-series database
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTimeSeriesDatabase(@PathVariable Long id) {
        log.info("DELETE /api/timeseries/{}", id);

        try {
            timeSeriesService.deleteTimeSeriesDatabase(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Time-series database deleted");
            response.put("timeSeriesId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting time-series database: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/timeseries/stats");

        try {
            Map<String, Object> stats = timeSeriesService.getStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
