@echo off
setlocal EnableDelayedExpansion
title Heronix SIS - Full Stack Launcher

echo ============================================
echo    Heronix SIS - Full Stack Launcher
echo ============================================
echo.
echo This will start:
echo   1. REST API Server (port 9580) - for Talk sync, clients
echo   2. Desktop Application (JavaFX UI) - for administration
echo.

:: Navigate to the directory where this batch file is located
cd /d "%~dp0"
echo Working directory: %CD%
echo.

:: Check if Java is available
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not found in PATH!
    echo Please ensure Java 21 is installed and added to your PATH.
    echo.
    pause
    exit /b 1
)

:: Check if Maven is available
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven [mvn] is not found in PATH!
    echo Please ensure Maven is installed and added to your PATH.
    echo.
    pause
    exit /b 1
)

echo Java version:
java -version 2>&1 | findstr /i "version"
echo.

echo ============================================
echo  Step 1: Starting REST API Server
echo ============================================
echo.
echo Starting API server in background...
echo REST API will be available at: http://localhost:9580
echo.

:: Start API server in a new window
start "Heronix SIS - API Server (9580)" cmd /c "mvn spring-boot:run -Dspring-boot.run.mainClass=com.heronix.HeronixSchedulerApiApplication & pause"

:: Wait for API server to start
echo Waiting for API server to initialize...
timeout /t 15 /nobreak >nul

:: Check if API server is running
echo.
echo Checking API server status...
curl -s -o nul -w "" http://localhost:9580/actuator/health >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo API Server: RUNNING on port 9580
) else (
    echo API Server: Still starting... (this is normal, it may take a moment)
)

echo.
echo ============================================
echo  Step 2: Starting Desktop Application
echo ============================================
echo.
echo Starting JavaFX desktop UI...
echo.

:: Start desktop application in a new window
start "Heronix SIS - Desktop UI" cmd /c "mvn javafx:run & pause"

echo.
echo ============================================
echo  Both services are starting!
echo ============================================
echo.
echo API Server Window: REST API on port 9580
echo Desktop Window:    JavaFX administration UI
echo.
echo Services:
echo   - Teacher Sync API: http://localhost:9580/api/teacher/all
echo   - Health Check:     http://localhost:9580/actuator/health
echo   - H2 Console:       http://localhost:9580/h2-console
echo.
echo You can now start Heronix-Talk to sync teachers.
echo.
echo Press any key to close this launcher (services will keep running)
pause >nul
