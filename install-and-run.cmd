@echo off
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -Command "(Get-CimInstance Win32_ComputerSystem).HypervisorPresent" | findstr /i "True" >nul
if errorlevel 1 (
  sc query aehd 2>nul | findstr /i "RUNNING" >nul
  if errorlevel 1 (
    echo Emulator acceleration is broken. Run repair-emulator.cmd first.
    exit /b 1
  )
)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0install-and-run.ps1"
exit /b %ERRORLEVEL%
