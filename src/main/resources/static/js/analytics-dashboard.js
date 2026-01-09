/**
 * Attendance Analytics Dashboard JavaScript
 *
 * Handles data loading, chart rendering, and user interactions for the
 * attendance analytics dashboard.
 *
 * Features:
 * - Real-time data fetching from REST API
 * - Interactive charts using Chart.js
 * - Date range selection
 * - Auto-refresh capability
 * - Responsive design
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 60 - Report Dashboard UI Integration
 */

// Global variables
let dailyTrendChart = null;
let statusPieChart = null;
let gradeBreakdownChart = null;
let currentStartDate = null;
let currentEndDate = null;

// Initialize dashboard on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('Initializing Analytics Dashboard...');

    // Set default date range (last 30 days)
    setQuickRange(30);

    // Load dashboard data
    loadDashboardData();

    // Set up auto-refresh (every 5 minutes)
    setInterval(loadDashboardData, 5 * 60 * 1000);
});

/**
 * Load all dashboard data
 */
function loadDashboardData() {
    console.log('Loading dashboard data...');

    const startDate = currentStartDate || getDateDaysAgo(30);
    const endDate = currentEndDate || getTodayDate();

    const apiUrl = `/api/reports/analytics/statistics?startDate=${startDate}&endDate=${endDate}`;

    fetch(apiUrl)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Dashboard data loaded:', data);
            updateDashboard(data);
            updateLastUpdatedTime();
        })
        .catch(error => {
            console.error('Error loading dashboard data:', error);
            showError('Failed to load dashboard data. Please try again.');
        });
}

/**
 * Update all dashboard components with new data
 */
function updateDashboard(data) {
    // Update overall statistics cards
    updateStatisticsCards(data.overall);

    // Update trend analysis
    updateTrendAnalysis(data.trends);

    // Update insights
    updateInsights(data.trends.insights);

    // Update top absentees table
    updateTopAbsentees(data.topAbsentees);

    // Update charts
    updateDailyTrendChart(data.dailyStats);
    updateStatusPieChart(data.overall);
    updateGradeBreakdownChart(data.gradeBreakdown);
}

/**
 * Update statistics cards
 */
function updateStatisticsCards(overall) {
    document.getElementById('totalStudents').textContent = overall.totalStudents.toLocaleString();
    document.getElementById('attendanceRate').textContent = overall.attendanceRate.toFixed(1) + '%';
    document.getElementById('absenteeismRate').textContent = overall.absenteeismRate.toFixed(1) + '%';
    document.getElementById('tardyRate').textContent = overall.tardyRate.toFixed(1) + '%';
}

/**
 * Update trend analysis section
 */
function updateTrendAnalysis(trends) {
    const trendDirection = document.getElementById('trendDirection');
    const trendBadge = document.getElementById('trendBadge');

    // Set trend direction with icon
    let trendIcon = '';
    let badgeClass = '';

    switch(trends.trend) {
        case 'IMPROVING':
            trendIcon = '<i class="fas fa-arrow-up text-success"></i>';
            badgeClass = 'bg-success';
            break;
        case 'DECLINING':
            trendIcon = '<i class="fas fa-arrow-down text-danger"></i>';
            badgeClass = 'bg-danger';
            break;
        case 'STABLE':
            trendIcon = '<i class="fas fa-arrows-alt-h text-primary"></i>';
            badgeClass = 'bg-primary';
            break;
        default:
            trendIcon = '<i class="fas fa-question text-muted"></i>';
            badgeClass = 'bg-secondary';
    }

    trendDirection.innerHTML = trendIcon;
    trendBadge.textContent = trends.trend;
    trendBadge.className = 'badge ' + badgeClass;

    // Update trend stats
    document.getElementById('averageRate').textContent = trends.averageAttendanceRate.toFixed(1) + '%';
    document.getElementById('changePercentage').textContent =
        (trends.changePercentage >= 0 ? '+' : '') + trends.changePercentage.toFixed(1) + '%';
    document.getElementById('bestDay').textContent = trends.bestDay + ' (' + trends.bestDayRate.toFixed(1) + '%)';
    document.getElementById('worstDay').textContent = trends.worstDay + ' (' + trends.worstDayRate.toFixed(1) + '%)';
}

/**
 * Update insights list
 */
function updateInsights(insights) {
    const insightsList = document.getElementById('insightsList');

    if (!insights || insights.length === 0) {
        insightsList.innerHTML = '<li class="text-muted">No insights available</li>';
        return;
    }

    insightsList.innerHTML = insights.map(insight =>
        `<li><i class="fas fa-lightbulb text-warning"></i> ${insight}</li>`
    ).join('');
}

/**
 * Update top absentees table
 */
function updateTopAbsentees(absentees) {
    const tbody = document.getElementById('absenteesTableBody');

    if (!absentees || absentees.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No data available</td></tr>';
        return;
    }

    tbody.innerHTML = absentees.map(student => {
        const riskBadge = getRiskBadge(student.riskLevel);
        return `
            <tr>
                <td>${student.studentName}</td>
                <td>${student.grade}</td>
                <td>${student.absentDays}</td>
                <td>${student.absenteeismRate.toFixed(1)}%</td>
                <td>${riskBadge}</td>
            </tr>
        `;
    }).join('');
}

/**
 * Get risk level badge HTML
 */
function getRiskBadge(riskLevel) {
    const badges = {
        'CRITICAL': '<span class="badge bg-danger">Critical</span>',
        'HIGH': '<span class="badge bg-warning">High</span>',
        'MODERATE': '<span class="badge bg-info">Moderate</span>',
        'LOW': '<span class="badge bg-success">Low</span>'
    };
    return badges[riskLevel] || '<span class="badge bg-secondary">Unknown</span>';
}

/**
 * Update daily trend chart
 */
function updateDailyTrendChart(dailyStats) {
    const ctx = document.getElementById('dailyTrendChart').getContext('2d');

    const labels = dailyStats.map(stat => stat.date);
    const presentData = dailyStats.map(stat => stat.presentCount);
    const absentData = dailyStats.map(stat => stat.absentCount);
    const tardyData = dailyStats.map(stat => stat.tardyCount);

    if (dailyTrendChart) {
        dailyTrendChart.destroy();
    }

    dailyTrendChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Present',
                    data: presentData,
                    borderColor: '#28a745',
                    backgroundColor: 'rgba(40, 167, 69, 0.1)',
                    tension: 0.4,
                    fill: true
                },
                {
                    label: 'Absent',
                    data: absentData,
                    borderColor: '#dc3545',
                    backgroundColor: 'rgba(220, 53, 69, 0.1)',
                    tension: 0.4,
                    fill: true
                },
                {
                    label: 'Tardy',
                    data: tardyData,
                    borderColor: '#ffc107',
                    backgroundColor: 'rgba(255, 193, 7, 0.1)',
                    tension: 0.4,
                    fill: true
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            aspectRatio: 2.5,
            plugins: {
                legend: {
                    position: 'bottom'
                },
                tooltip: {
                    mode: 'index',
                    intersect: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        precision: 0
                    }
                }
            }
        }
    });
}

/**
 * Update status pie chart
 */
function updateStatusPieChart(overall) {
    const ctx = document.getElementById('statusPieChart').getContext('2d');

    if (statusPieChart) {
        statusPieChart.destroy();
    }

    statusPieChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Present', 'Absent', 'Tardy'],
            datasets: [{
                data: [overall.presentCount, overall.absentCount, overall.tardyCount],
                backgroundColor: ['#28a745', '#dc3545', '#ffc107'],
                borderWidth: 2,
                borderColor: '#fff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            aspectRatio: 1.5,
            plugins: {
                legend: {
                    position: 'bottom'
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.parsed || 0;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = ((value / total) * 100).toFixed(1);
                            return label + ': ' + value.toLocaleString() + ' (' + percentage + '%)';
                        }
                    }
                }
            }
        }
    });
}

/**
 * Update grade breakdown chart
 */
function updateGradeBreakdownChart(gradeBreakdown) {
    const ctx = document.getElementById('gradeBreakdownChart').getContext('2d');

    const grades = Object.keys(gradeBreakdown).sort();
    const attendanceRates = grades.map(grade => gradeBreakdown[grade].attendanceRate);

    if (gradeBreakdownChart) {
        gradeBreakdownChart.destroy();
    }

    gradeBreakdownChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: grades,
            datasets: [{
                label: 'Attendance Rate (%)',
                data: attendanceRates,
                backgroundColor: '#007bff',
                borderColor: '#0056b3',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            aspectRatio: 2,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return 'Attendance: ' + context.parsed.y.toFixed(1) + '%';
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    max: 100,
                    ticks: {
                        callback: function(value) {
                            return value + '%';
                        }
                    }
                }
            }
        }
    });
}

/**
 * Set quick date range
 */
function setQuickRange(days) {
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - days);

    currentStartDate = formatDate(startDate);
    currentEndDate = formatDate(endDate);

    // Update input fields
    document.getElementById('startDate').value = currentStartDate;
    document.getElementById('endDate').value = currentEndDate;

    // Update display
    document.getElementById('dateRangeDisplay').textContent = `Last ${days} days`;
}

/**
 * Apply date range from modal
 */
function applyDateRange() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!startDate || !endDate) {
        alert('Please select both start and end dates');
        return;
    }

    currentStartDate = startDate;
    currentEndDate = endDate;

    // Update display
    document.getElementById('dateRangeDisplay').textContent = `${startDate} to ${endDate}`;

    // Close modal
    const modal = bootstrap.Modal.getInstance(document.getElementById('dateRangeModal'));
    modal.hide();

    // Reload data
    loadDashboardData();
}

/**
 * Refresh dashboard
 */
function refreshDashboard() {
    console.log('Refreshing dashboard...');
    loadDashboardData();
}

/**
 * Update last updated time
 */
function updateLastUpdatedTime() {
    const now = new Date();
    const timeString = now.toLocaleTimeString();
    document.getElementById('lastUpdated').textContent = `Last updated: ${timeString}`;
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
 * Get date N days ago
 */
function getDateDaysAgo(days) {
    const date = new Date();
    date.setDate(date.getDate() - days);
    return formatDate(date);
}

/**
 * Get today's date
 */
function getTodayDate() {
    return formatDate(new Date());
}

/**
 * Show error message
 */
function showError(message) {
    // Create error alert
    const alert = document.createElement('div');
    alert.className = 'alert alert-danger alert-dismissible fade show position-fixed top-0 start-50 translate-middle-x mt-3';
    alert.style.zIndex = '9999';
    alert.innerHTML = `
        <i class="fas fa-exclamation-triangle"></i> ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    document.body.appendChild(alert);

    // Auto-dismiss after 5 seconds
    setTimeout(() => {
        alert.remove();
    }, 5000);
}
