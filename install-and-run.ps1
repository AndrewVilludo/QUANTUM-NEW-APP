. "$PSScriptRoot\emulator.config.ps1"

$env:JAVA_HOME = $JavaHome
if (-not $env:GRADLE_USER_HOME) {
    $env:GRADLE_USER_HOME = Join-Path $env:USERPROFILE ".gradle"
}
Set-Location $PSScriptRoot

$adb = Join-Path $AndroidSdkRoot "platform-tools\adb.exe"
$online = & $adb devices | Select-String -Pattern "^\S+\s+device$"
if (-not $online) {
    Write-Host "No emulator or phone detected via adb."
    Write-Host "  Emulator: .\start-emulator.cmd   (use .cmd — not .ps1 — if PowerShell blocks unsigned scripts)"
    Write-Host "  Physical phone: USB debugging on, then .\run-on-phone.cmd"
    exit 1
}

Write-Host "Building and installing debug APK..."
.\gradlew.bat installDebug
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Enable-EmulatorDisplay -AdbPath $adb

Write-Host "Launching app..."
& $adb shell am force-stop com.example.quantumrealm
& $adb shell am start -n com.example.quantumrealm/.MainActivity
