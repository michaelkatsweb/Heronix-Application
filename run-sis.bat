@echo off
setlocal EnableDelayedExpansion
title Heronix SIS - Clean, Compile, Run

echo ============================================
echo    Heronix SIS - Clean, Compile, and Run
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

echo ============================================
echo    Step 0: Kill previous Heronix SIS instance
echo ============================================
echo.
:: Find and kill any java.exe holding target files (previous SIS run)
for /f "tokens=2" %%p in ('wmic process where "name='java.exe' and commandline like '%%heronix%%'" get processid /value 2^>nul ^| findstr ProcessId') do (
    echo Killing previous Heronix process: PID %%p
    taskkill /F /PID %%p >nul 2>&1
)
echo.

echo ============================================
echo    Step 1: Maven Clean
echo ============================================
echo.
call mvn clean -q
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven clean failed!
    echo.
    pause
    exit /b 1
)
echo Clean completed successfully.
echo.

echo ============================================
echo    Step 2: Maven Compile
echo ============================================
echo.
call mvn compile -q
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven compile failed!
    echo.
    pause
    exit /b 1
)
echo Compile completed successfully.
echo.

echo ============================================
echo    Step 3: Starting Heronix SIS
echo ============================================
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

:: Run with Maven JavaFX plugin
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
