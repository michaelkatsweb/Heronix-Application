@echo off
setlocal EnableDelayedExpansion
title Heronix SIS - Student Information System

echo ============================================
echo    Heronix SIS - Student Information System
echo ============================================
echo.

:: Navigate to the directory where this batch file is located
cd /d "%~dp0"
echo Working directory: %CD%
echo.

:: Check if Maven is available
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven [mvn] is not found in PATH!
    echo Please ensure Maven is installed and added to your PATH.
    echo.
    pause
    exit /b 1
)

:: Check if Java is available
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not found in PATH!
    echo Please ensure Java 21 is installed and added to your PATH.
    echo.
    pause
    exit /b 1
)

echo Java version:
java -version 2>&1 | findstr /i "version"
echo.
echo Maven version:
mvn -version 2>&1 | findstr /i "Apache Maven"
echo.

echo Starting Heronix SIS...
echo.
echo JavaFX Desktop Application with Embedded Server
echo REST API will be available at: http://localhost:9580
echo.
echo NOTE: First startup may take 1-2 minutes to initialize.
echo The application window will appear when ready.
echo.
echo Press Ctrl+C to stop the application
echo ============================================
echo.

:: Run with Maven JavaFX plugin (compiles and runs the JavaFX app)
call mvn javafx:run

:: Check if the command failed
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ============================================
    echo    APPLICATION EXITED WITH ERROR
    echo    Exit code: %ERRORLEVEL%
    echo ============================================
)

echo.
pause
