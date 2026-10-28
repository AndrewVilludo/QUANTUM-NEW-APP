@echo off
setlocal
echo ============================================
echo  Quantum Realm - Emulator repair
echo ============================================
echo.
echo Your PC currently has NO working acceleration:
echo   - AEHD driver removed or stopped
echo   - Windows Hypervisor not active ^(needs reboot after enabling^)
echo.
echo Pick ONE path in ADMIN Command Prompt:
echo.
echo [A] WHPX ^(Windows 11, works with WSL2^) — RECOMMENDED
echo     Right-click: setup-emulator-whpx.cmd - Run as administrator
echo     REBOOT when done
echo     Then: emulator -avd Pixel_6_Pro -gpu swiftshader_indirect
echo.
echo [B] AEHD only ^(no WSL2 / disable Virtual Machine Platform^)
echo     Right-click: setup-emulator-aehd-only.cmd - Run as administrator
echo     REBOOT, turn off Memory integrity, then Admin:
echo       cd %%LOCALAPPDATA%%\Android\Sdk\extras\google\Android_Emulator_Hypervisor_Driver
echo       silent_install.bat
echo       sc start aehd
echo.
echo After reboot, verify hypervisor is on ^(PowerShell^):
echo   ^(Get-CimInstance Win32_ComputerSystem^).HypervisorPresent
echo   Should be True for WHPX, or sc query aehd shows RUNNING for AEHD
echo.
echo Then install app:
echo   cd %~dp0
echo   install-and-run.cmd
echo.
pause
