@echo off
setlocal
set "EMU=%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe"
set "AVD=Pixel_6_Pro"
if not exist "%EMU%" ( echo Emulator not found & exit /b 1 )
echo Cold boot + software GPU (use if screen stays black)...
start "" "%EMU%" -avd %AVD% -no-snapshot-load -gpu swiftshader_indirect
echo Emulator window started. When HOME appears, run: install-and-run.cmd
