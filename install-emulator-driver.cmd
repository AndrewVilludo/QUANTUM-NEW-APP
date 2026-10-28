@echo off
REM Must run as Administrator (right-click -> Run as administrator)
cd /d "%LOCALAPPDATA%\Android\Sdk\extras\google\Android_Emulator_Hypervisor_Driver"
if not exist silent_install.bat (
  echo Driver folder missing. In Android SDK Manager install:
  echo   Android Emulator hypervisor driver ^(installer^)
  exit /b 1
)
echo Installing Android Emulator Hypervisor Driver...
call silent_install.bat
echo.
echo Reboot if the installer asked you to, then run start-emulator.cmd
pause
