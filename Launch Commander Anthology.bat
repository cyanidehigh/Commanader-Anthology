@echo off
setlocal

title Commander Anthology
cd /d "%~dp0"

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0Launch Commander Anthology.ps1"

if errorlevel 1 (
    echo.
    echo Commander Anthology failed to launch.
    pause
)

endlocal
