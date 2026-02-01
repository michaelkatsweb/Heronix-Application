@echo off
title Heronix SIS - Desktop (Standalone)
echo.
echo =====================================================
echo   Heronix SIS - STANDALONE MODE
echo =====================================================
echo.
echo Runs the full desktop application with embedded server.
echo Use this for single-user or development.
echo.
echo Port: 9580
echo.

cd /d "%~dp0"
mvn javafx:run
