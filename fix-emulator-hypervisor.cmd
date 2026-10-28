@echo off
REM Right-click -> Run as administrator
cd /d "%LOCALAPPDATA%\Android\Sdk\extras\google\Android_Emulator_Hypervisor_Driver"

echo === Android Emulator Hypervisor (aehd) ===
sc query aehd
echo.

echo Reinstalling driver...
call silent_install.bat
if errorlevel 1 (
  echo Install failed. See notes below.
  goto tips
)

echo Starting aehd service...
sc start aehd
echo.
sc query aehd
echo.

"%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe" -accel-check
echo.
echo If still code 6: enable Windows Hypervisor Platform ^(optionalfeatures^), reboot, try again.
goto end

:tips
echo.
echo If aehd will not start:
echo   1. Win+R optionalfeatures - enable "Windows Hypervisor Platform" and "Virtual Machine Platform"
echo   2. Reboot
echo   3. Settings - Device Security - Core isolation - turn OFF Memory integrity ^(test^), reboot
echo   4. Disable VirtualBox/Hyper-V conflict if you use other VMs
echo   5. BIOS: Intel VT-x / AMD-V enabled

:end
pause
