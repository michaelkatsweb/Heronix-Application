@echo off
REM ============================================================================
REM Heronix SIS - Download Vendor Assets for Offline Operation (Windows)
REM Run this script once to download all required frontend libraries
REM Requires: curl (included in Windows 10+) or PowerShell
REM ============================================================================

echo ===============================================================
echo Downloading vendor assets for offline operation...
echo ===============================================================

set VENDOR_DIR=src\main\resources\static\vendor

REM Create vendor directories
if not exist "%VENDOR_DIR%\bootstrap\css" mkdir "%VENDOR_DIR%\bootstrap\css"
if not exist "%VENDOR_DIR%\bootstrap\js" mkdir "%VENDOR_DIR%\bootstrap\js"
if not exist "%VENDOR_DIR%\fontawesome\css" mkdir "%VENDOR_DIR%\fontawesome\css"
if not exist "%VENDOR_DIR%\fontawesome\webfonts" mkdir "%VENDOR_DIR%\fontawesome\webfonts"
if not exist "%VENDOR_DIR%\chartjs" mkdir "%VENDOR_DIR%\chartjs"

REM Download Bootstrap 5.3.0
echo Downloading Bootstrap 5.3.0...
curl -sL "https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" -o "%VENDOR_DIR%\bootstrap\css\bootstrap.min.css"
curl -sL "https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js" -o "%VENDOR_DIR%\bootstrap\js\bootstrap.bundle.min.js"

REM Download Chart.js 4.4.0
echo Downloading Chart.js 4.4.0...
curl -sL "https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js" -o "%VENDOR_DIR%\chartjs\chart.umd.min.js"

REM Download Font Awesome 6.4.0
echo Downloading Font Awesome 6.4.0...
curl -sL "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" -o "%VENDOR_DIR%\fontawesome\css\all.min.css"

REM Download Font Awesome webfonts
echo Downloading Font Awesome webfonts...
curl -sL "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/webfonts/fa-solid-900.woff2" -o "%VENDOR_DIR%\fontawesome\webfonts\fa-solid-900.woff2"
curl -sL "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/webfonts/fa-regular-400.woff2" -o "%VENDOR_DIR%\fontawesome\webfonts\fa-regular-400.woff2"
curl -sL "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/webfonts/fa-brands-400.woff2" -o "%VENDOR_DIR%\fontawesome\webfonts\fa-brands-400.woff2"

REM Fix Font Awesome CSS paths using PowerShell
echo Fixing Font Awesome CSS paths...
powershell -Command "(Get-Content '%VENDOR_DIR%\fontawesome\css\all.min.css') -replace '../webfonts/', '/vendor/fontawesome/webfonts/' | Set-Content '%VENDOR_DIR%\fontawesome\css\all.min.css'"

echo.
echo ===============================================================
echo Vendor assets downloaded successfully!
echo ===============================================================
echo.
echo Directory structure:
dir /s /b "%VENDOR_DIR%" 2>nul | find /c /v ""
echo files downloaded.

pause
