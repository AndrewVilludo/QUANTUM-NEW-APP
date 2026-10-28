@echo off
REM Right-click -> Run as administrator. Then REBOOT (required).

net session >nul 2>&1
if errorlevel 1 (
  echo Run as Administrator.
  pause
  exit /b 1
)

set "DRV=%LOCALAPPDATA%\Android\Sdk\extras\google\Android_Emulator_Hypervisor_Driver"
if exist "%DRV%\silent_install.bat" (
  echo Removing AEHD ^(conflicts with WHPX^)...
  cd /d "%DRV%"
  call silent_install.bat -u
)

echo Enabling Windows Hypervisor Platform...
dism /online /enable-feature /featurename:HypervisorPlatform /all /norestart
dism /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart

echo.
echo *** REBOOT NOW ***
echo After reboot, PowerShell should show:
echo   ^(Get-CimInstance Win32_ComputerSystem^).HypervisorPresent  -^> True
echo.
echo Start emulator:
echo   emulator -avd Pixel_6_Pro -gpu swiftshader_indirect
echo.
echo -accel-check may still show code 6; ignore if emulator window opens.
shutdown /r /t 60 /c "Rebooting to enable Android emulator WHPX. Cancel with: shutdown /a"
echo Reboot in 60 seconds... Cancel with: shutdown /a
pause
