. "$PSScriptRoot\emulator.config.ps1"

$emulator = Join-Path $AndroidSdkRoot "emulator\emulator.exe"
if (-not (Test-Path $emulator)) {
    Write-Error "Emulator not found at $emulator. Install Android SDK Emulator in SDK Manager."
    exit 1
}

$avds = & $emulator -list-avds 2>&1
if ($avds -notcontains $EmulatorAvdName) {
    Write-Host "Available AVDs:"
    $avds | ForEach-Object { Write-Host "  $_" }
    Write-Error "Unknown AVD '$EmulatorAvdName'. Set EmulatorAvdName in emulator.config.ps1 to one of the names above."
    exit 1
}

Write-Host "Starting emulator: $EmulatorAvdName"
Start-Process -FilePath $emulator -ArgumentList @("-avd", $EmulatorAvdName) -WindowStyle Normal

$adb = Join-Path $AndroidSdkRoot "platform-tools\adb.exe"
Write-Host "Waiting for device..."
& $adb wait-for-device
$deadline = (Get-Date).AddMinutes(3)
do {
    $boot = & $adb shell getprop sys.boot_completed 2>$null
    if ($boot -match "1") { break }
    Start-Sleep -Seconds 2
} while ((Get-Date) -lt $deadline)

if ($boot -notmatch "1") {
    Write-Warning "Emulator started but boot may not be finished yet."
} else {
    Enable-EmulatorDisplay -AdbPath $adb
    Write-Host "Emulator ready (display woken)."
}
