@echo off
REM Must start by right-click -> Run as administrator (NOT normal PowerShell)
net session >nul 2>&1
if errorlevel 1 (
  echo ERROR: Run this file as Administrator ^(right-click - Run as administrator^)
  pause
  exit /b 1
)

echo NOTE: AEHD and WHPX conflict. Use setup-emulator-whpx.cmd OR setup-emulator-aehd-only.cmd
echo This script tries AEHD only ^(disable WHPX first if you use WSL2^).
echo.
echo === Android Emulator Hypervisor Driver ^(aehd^) ===
cd /d "%LOCALAPPDATA%\Android\Sdk\extras\google\Android_Emulator_Hypervisor_Driver"
call silent_install.bat
sc config aehd start= demand
sc start aehd

echo.
echo === 3. Service status ===
sc query aehd

echo.
echo === 4. Acceleration check ===
"%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe" -accel-check

echo.
echo REBOOT is strongly recommended now, then run:
echo   cd %USERPROFILE%\OneDrive\Documents\thesis\QuantumRealm
echo   powershell -ExecutionPolicy Bypass -File .\start-emulator.ps1
echo.
echo If aehd STATE is STOPPED after reboot, try WHPX-only:
echo   In Admin cmd here: silent_install.bat -u
echo   Reboot again, then -accel-check
pause
