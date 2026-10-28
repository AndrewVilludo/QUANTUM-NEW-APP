@echo off
:: Double-click -> Run as administrator (required)

net session >nul 2>&1
if errorlevel 1 (
  echo Requesting Administrator...
  powershell -NoProfile -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
  exit /b
)

echo ========================================
echo  Fix Android Emulator - WHPX ^(Win11^)
echo ========================================

echo [1/4] Remove broken AEHD if present...
set "DRV=%LOCALAPPDATA%\Android\Sdk\extras\google\Android_Emulator_Hypervisor_Driver"
if exist "%DRV%\silent_install.bat" (
  cd /d "%DRV%"
  call silent_install.bat -u >nul 2>&1
)

echo [2/4] Enable Windows Hypervisor Platform...
dism /online /enable-feature /featurename:HypervisorPlatform /all /norestart
dism /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart

echo [3/4] Set hypervisorlaunchtype=auto...
bcdedit /set hypervisorlaunchtype auto

echo [4/4] Feature status:
dism /online /get-featureinfo /featurename:HypervisorPlatform | findstr /i "State"
dism /online /get-featureinfo /featurename:VirtualMachinePlatform | findstr /i "State"

echo.
echo *** YOU MUST REBOOT ***
echo After reboot, PowerShell:
echo   ^(Get-CimInstance Win32_ComputerSystem^).HypervisorPresent
echo Must show: True
echo.
echo Then:
echo   emulator -avd Pixel_6_Pro -gpu swiftshader_indirect
echo   cd %~dp0
echo   install-and-run.cmd
echo.
choice /C YN /M "Reboot in 30 seconds now"
if errorlevel 2 goto skip
shutdown /r /t 30 /c "Enable emulator hypervisor"
:skip
pause
