@echo off
title Heronix SIS - JDK 21 Development Mode
echo ============================================
echo    Heronix SIS - JDK 21 Development Mode
echo ============================================
echo.

:: Navigate to project directory
cd /d "%~dp0"

:: Set JDK 21
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"

:: Verify JDK
echo Using Java:
java -version 2>&1
echo.

:: Clean compile
echo Cleaning and compiling project...
echo.

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
