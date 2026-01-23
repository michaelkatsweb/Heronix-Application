@echo off
title Heronix SIS - Development Mode
echo ============================================
echo    Heronix SIS - Development Mode
echo ============================================
echo.

:: Navigate to project directory
cd /d "%~dp0"

echo Cleaning and compiling project...
echo.

:: Clean compile first, then run
mvn clean compile -q
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ============================================
    echo    COMPILATION FAILED - Check errors above
    echo ============================================
    pause
    exit /b 1
)

echo.
echo Compilation successful! Starting Heronix SIS...
echo.
echo JavaFX Desktop Application with Embedded Server
echo REST API will be available at: http://localhost:9580
echo.
echo Press Ctrl+C to stop the application
echo ============================================
echo.

:: Run with Maven JavaFX plugin
mvn javafx:run

pause
