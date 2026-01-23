@echo off
setlocal EnableDelayedExpansion
title Heronix SIS - REST API Server (Port 9580)

echo ============================================
echo    Heronix SIS - REST API Server
echo ============================================
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

echo Java version:
java -version 2>&1 | findstr /i "version"
echo.

echo Starting Heronix SIS REST API Server...
echo.
echo This provides REST API endpoints for:
echo   - Heronix-Talk sync (teacher accounts)
echo   - EduPro-Student client
echo   - EduPro-Teacher client
echo.
echo REST API will be available at: http://localhost:9580
echo Health check: http://localhost:9580/actuator/health
echo Teacher sync: http://localhost:9580/api/teacher/all
echo.
echo Press Ctrl+C to stop the server
echo ============================================
echo.

:: Run the API server using Spring Boot
:: Uses HeronixSchedulerApiApplication (SERVLET mode with web server)
mvn spring-boot:run -Dspring-boot.run.mainClass=com.heronix.HeronixSchedulerApiApplication

:: Check if the command failed
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ============================================
    echo    SERVER EXITED WITH ERROR
    echo    Exit code: %ERRORLEVEL%
    echo ============================================
)

echo.
pause
