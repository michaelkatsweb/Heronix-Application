/**
 * Chart Viewer JavaScript
 *
 * Handles interactive chart generation and display.
 *
 * Features:
 * - Dynamic chart loading from API
 * - Chart type switching
 * - Date range filtering
 * - PNG export
 * - Data export
 * - Statistics calculation
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 66 - Report Data Visualization & Charts
 */

let currentChart = null;
let currentChartType = 'daily-trend';
let currentChartData = null;

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    initializeDatePickers();
    setupEventListeners();
    // Don't load chart automatically - wait for user to select dates and click refresh
});

/**
 * Initialize date pickers with default values
 */
function initializeDatePickers() {
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - 30); // Default: last 30 days

    document.getElementById('startDate').value = formatDate(startDate);
    document.getElementById('endDate').value = formatDate(endDate);
}

/**
 * Format date as YYYY-MM-DD
 */
function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

/**
 * Setup event listeners
 */
function setupEventListeners() {
    // Chart type buttons
    document.querySelectorAll('[data-chart]').forEach(button => {
        button.addEventListener('click', function() {
            // Update active state
            document.querySelectorAll('[data-chart]').forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');

            // Update current chart type
            currentChartType = this.dataset.chart;

            // Update chart title and badge
            updateChartTypeDisplay();
        });
    });

    // Refresh button
    document.getElementById('refreshChart').addEventListener('click', loadChart);

    // Download PNG button
    document.getElementById('downloadPNG').addEventListener('click', downloadChartAsPNG);

    // Export data button
    document.getElementById('exportData').addEventListener('click', exportChartData);
}

/**
 * Update chart type display
 */
function updateChartTypeDisplay() {
    const titles = {
        'daily-trend': 'Daily Attendance Trend',
        'status-distribution': 'Attendance Status Distribution',
        'grade-comparison': 'Attendance by Grade Level',
        'weekly-pattern': 'Weekly Attendance Pattern'
    };

    const badges = {
        'daily-trend': 'Line Chart',
        'status-distribution': 'Pie Chart',
        'grade-comparison': 'Bar Chart',
        'weekly-pattern': 'Area Chart'
    };

    document.getElementById('chartTitle').textContent = titles[currentChartType];
    document.getElementById('chartBadge').textContent = badges[currentChartType];
}

/**
 * Load chart from API
 */
async function loadChart() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!startDate || !endDate) {
        alert('Please select both start and end dates');
        return;
    }

    if (new Date(startDate) > new Date(endDate)) {
        alert('Start date must be before end date');
        return;
    }

    showLoading(true);

    try {
        const response = await fetch(
            `/api/charts/${currentChartType}?startDate=${startDate}&endDate=${endDate}`
        );

        if (!response.ok) {
            throw new Error('Failed to load chart data');
        }

        currentChartData = await response.json();
        renderChart(currentChartData);
        updateChartInfo(currentChartData);
        calculateStatistics(currentChartData);

    } catch (error) {
        console.error('Error loading chart:', error);
        alert('Failed to load chart. Please try again.');
    } finally {
        showLoading(false);
    }
}

/**
 * Render chart using Chart.js
 */
function renderChart(chartConfig) {
    // Destroy existing chart
    if (currentChart) {
        currentChart.destroy();
    }

    const ctx = document.getElementById('mainChart').getContext('2d');

    // Map chart type
    const chartType = mapChartType(chartConfig.chartType);

    // Prepare datasets
    const datasets = chartConfig.dataSeries.map(series => ({
        label: series.label,
        data: series.data,
        backgroundColor: series.fill === false ? 'transparent' : (series.color + '33'), // 20% opacity
        borderColor: series.color,
        borderWidth: series.borderWidth || 2,
        pointRadius: series.pointRadius || 3,
        pointBackgroundColor: series.color,
        tension: series.tension || 0,
        fill: series.fill !== false
    }));

    // Chart configuration
    const config = {
        type: chartType,
        data: {
            labels: chartConfig.labels,
            datasets: datasets
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            aspectRatio: 2,
            plugins: {
                legend: {
                    display: chartConfig.showLegend !== false,
                    position: chartConfig.legendPosition || 'top'
                },
                title: {
                    display: false
                },
                tooltip: {
                    enabled: chartConfig.enableTooltips !== false,
                    mode: 'index',
                    intersect: false
                }
            },
            scales: chartType !== 'pie' && chartType !== 'doughnut' ? {
                x: {
                    display: true,
                    title: {
                        display: chartConfig.xAxisLabel != null,
                        text: chartConfig.xAxisLabel
                    },
                    grid: {
                        display: chartConfig.showGrid !== false
                    }
                },
                y: {
                    display: true,
                    title: {
                        display: chartConfig.yAxisLabel != null,
                        text: chartConfig.yAxisLabel
                    },
                    grid: {
                        display: chartConfig.showGrid !== false
                    },
                    beginAtZero: true,
                    min: chartConfig.minYValue,
                    max: chartConfig.maxYValue
                }
            } : {},
            animation: {
                duration: chartConfig.enableAnimations !== false ? 1000 : 0
            }
        }
    };

    // For stacked bar charts
    if (chartConfig.stacked) {
        config.options.scales.x.stacked = true;
        config.options.scales.y.stacked = true;
    }

    currentChart = new Chart(ctx, config);

    // Update subtitle
    document.getElementById('chartSubtitle').textContent = chartConfig.subtitle || '';
}

/**
 * Map chart type to Chart.js type
 */
function mapChartType(type) {
    const typeMap = {
        'LINE': 'line',
        'BAR': 'bar',
        'HORIZONTAL_BAR': 'bar',
        'PIE': 'pie',
        'DOUGHNUT': 'doughnut',
        'AREA': 'line',
        'SCATTER': 'scatter',
        'RADAR': 'radar',
        'POLAR': 'polarArea'
    };
    return typeMap[type] || 'line';
}

/**
 * Update chart information panel
 */
function updateChartInfo(chartConfig) {
    const generatedAt = new Date(chartConfig.generatedAt);
    document.getElementById('infoGenerated').textContent = generatedAt.toLocaleString();

    const totalDataPoints = chartConfig.dataSeries.reduce((sum, series) =>
        sum + (series.data ? series.data.length : 0), 0);
    document.getElementById('infoDataPoints').textContent = totalDataPoints;

    document.getElementById('infoSource').textContent = chartConfig.sourceReport || 'Unknown';
}

/**
 * Calculate and display statistics
 */
function calculateStatistics(chartConfig) {
    if (!chartConfig.dataSeries || chartConfig.dataSeries.length === 0) {
        return;
    }

    // Find present, absent, and tardy series
    const presentSeries = chartConfig.dataSeries.find(s => s.label === 'Present');
    const absentSeries = chartConfig.dataSeries.find(s => s.label === 'Absent');
    const tardySeries = chartConfig.dataSeries.find(s => s.label === 'Tardy');

    // Calculate totals
    const totalPresent = presentSeries ? presentSeries.data.reduce((a, b) => a + b, 0) : 0;
    const totalAbsent = absentSeries ? absentSeries.data.reduce((a, b) => a + b, 0) : 0;
    const totalTardy = tardySeries ? tardySeries.data.reduce((a, b) => a + b, 0) : 0;
    const grandTotal = totalPresent + totalAbsent + totalTardy;

    // Calculate rates
    const presentRate = grandTotal > 0 ? (totalPresent / grandTotal * 100).toFixed(1) : '0.0';
    const absentRate = grandTotal > 0 ? (totalAbsent / grandTotal * 100).toFixed(1) : '0.0';
    const tardyRate = grandTotal > 0 ? (totalTardy / grandTotal * 100).toFixed(1) : '0.0';

    // Update display
    document.getElementById('statPresentRate').textContent = presentRate + '%';
    document.getElementById('statAbsentRate').textContent = absentRate + '%';
    document.getElementById('statTardyRate').textContent = tardyRate + '%';
}

/**
 * Download chart as PNG
 */
function downloadChartAsPNG() {
    if (!currentChart) {
        alert('No chart to download. Please generate a chart first.');
        return;
    }

    const canvas = document.getElementById('mainChart');
    const url = canvas.toDataURL('image/png');

    const link = document.createElement('a');
    link.download = `${currentChartType}-${new Date().getTime()}.png`;
    link.href = url;
    link.click();
}

/**
 * Export chart data as JSON
 */
function exportChartData() {
    if (!currentChartData) {
        alert('No data to export. Please generate a chart first.');
        return;
    }

    const dataStr = JSON.stringify(currentChartData, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });

    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    link.download = `${currentChartType}-data-${new Date().getTime()}.json`;
    link.href = url;
    link.click();

    URL.revokeObjectURL(url);
}

/**
 * Show/hide loading indicator
 */
function showLoading(show) {
    document.getElementById('loadingIndicator').style.display = show ? 'block' : 'none';
    document.getElementById('chartContainer').style.display = show ? 'none' : 'block';
}
