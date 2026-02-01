@echo off
title Heronix SIS - Client Mode
echo.
echo =====================================================
echo   Heronix SIS - CLIENT MODE
echo =====================================================
echo.
echo Connects to an external Heronix-SIS-Server.
echo Use this for multi-user enterprise deployments.
echo.

REM Set server URL - change this to your server's IP address
set SIS_SERVER_URL=http://localhost:9590

echo Server URL: %SIS_SERVER_URL%
echo.
echo Make sure Heronix-SIS-Server is running first!
echo   cd H:\Heronix\Heronix-SIS-Server
echo   start-server.bat
echo.

cd /d "%~dp0"
mvn javafx:run -Dsis.server.url=%SIS_SERVER_URL% -Dsis.client.mode=true
