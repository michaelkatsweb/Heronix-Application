@echo off
title Heronix SIS - Quick Start
echo ============================================
echo    Heronix SIS - Quick Start (Skip Tests)
echo ============================================
echo.

:: Navigate to project directory
cd /d "%~dp0"

echo Starting Heronix SIS (skipping tests for faster startup)...
echo.
echo JavaFX Desktop Application with Embedded Server
echo REST API will be available at: http://localhost:9580
echo.
echo Press Ctrl+C to stop the application
echo ============================================
echo.

:: Run with Maven JavaFX plugin, skip tests
mvn javafx:run -DskipTests

pause
