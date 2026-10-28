@echo off
REM Right-click -> Run as administrator
REM Only if you do NOT need WSL2 / Virtual Machine Platform. AEHD conflicts with WHPX.

net session >nul 2>&1
if errorlevel 1 (
  echo Run as Administrator.
  pause
  exit /b 1
)

echo Disabling Windows Hypervisor Platform ^(required for AEHD^)...
dism /online /disable-feature /featurename:HypervisorPlatform /norestart
dism /online /disable-feature /featurename:VirtualMachinePlatform /norestart

echo Turn OFF Memory integrity: Settings - Device security - Core isolation - reboot after.
echo.
echo After reboot, Admin PowerShell:
echo   cd %%LOCALAPPDATA%%\Android\Sdk\extras\google\Android_Emulator_Hypervisor_Driver
echo   silent_install.bat
echo   sc start aehd
echo   sc query aehd   ^(expect STATE: RUNNING^)
pause
