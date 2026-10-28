@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"

echo === Quantum Realm on physical phone ^(no emulator^) ===
echo.
echo 1. Phone: Settings - Developer options - USB debugging ON
echo 2. Connect USB, accept debugging prompt on phone
echo.

"%ADB%" devices
echo.
"%ADB%" devices | findstr /R "device$" | findstr /V "emulator" >nul
if errorlevel 1 (
  echo No phone detected. Fix USB/driver, or use FIX-EMULATOR-NOW.cmd for emulator.
  pause
  exit /b 1
)

echo Detecting PC Wi-Fi IP for API...
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /i "IPv4"') do (
  set "ip=%%a"
  set "ip=!ip: =!"
  echo   Found: !ip!
  goto gotip
)
:gotip
if not defined ip (
  echo Could not detect IP. Edit local.properties manually: api.host=YOUR_PC_IP
  pause
  exit /b 1
)

echo Updating local.properties api.host=!ip!
powershell -NoProfile -Command ^
  "$p='%~dp0local.properties'; $c=Get-Content $p -Raw; if($c -match '(?m)^api\.host=.*'){$c=$c -replace '(?m)^api\.host=.*','api.host=!ip!'}else{$c+=\"`napi.host=!ip!\"}; Set-Content $p $c.TrimEnd()"

echo.
echo Start Laravel in another window:
echo   cd %~dp0..\quantum-app
echo   php artisan serve --host=0.0.0.0 --port=8000
echo.
pause

call gradlew.bat installDebug
if errorlevel 1 exit /b 1

"%ADB%" shell am start -n com.example.quantumrealm/.MainActivity
echo Done. Phone must be on same Wi-Fi as PC.
pause
