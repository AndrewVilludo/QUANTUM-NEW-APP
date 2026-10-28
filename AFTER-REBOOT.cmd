@echo off
cd /d "%~dp0"
echo HypervisorPresent check:
powershell -NoProfile -Command "(Get-CimInstance Win32_ComputerSystem).HypervisorPresent"
echo.
echo Starting emulator in new window...
start "Android Emulator" "%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe" -avd Pixel_6_Pro -gpu swiftshader_indirect
echo Wait for HOME screen, then press any key to install Quantum Realm...
pause >nul
call install-and-run.cmd
