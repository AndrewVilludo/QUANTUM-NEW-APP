. "$PSScriptRoot\emulator.config.ps1"

$emulator = Join-Path $AndroidSdkRoot "emulator\emulator.exe"
if (-not (Test-Path $emulator)) {
    Write-Error "Emulator not found at $emulator."
    exit 1
}

$hypervisorOn = (Get-CimInstance Win32_ComputerSystem -ErrorAction SilentlyContinue).HypervisorPresent
Write-Host "Windows HypervisorPresent: $hypervisorOn"

Write-Host "Checking CPU acceleration (required for x86_64 AVDs)..."
$accel = & $emulator -accel-check 2>&1 | Out-String
Write-Host $accel.Trim()

if (-not $hypervisorOn) {
    $aehd = sc.exe query aehd 2>&1 | Out-String
    if ($aehd -notmatch "RUNNING") {
        Write-Host ""
        Write-Host "=== No hypervisor: emulator cannot start ===" -ForegroundColor Yellow
        Write-Host "You likely removed AEHD without enabling WHPX + reboot."
        Write-Host "Run as Admin: .\setup-emulator-whpx.cmd  then REBOOT"
        Write-Host "Or: .\repair-emulator.cmd for instructions"
        exit 1
    }
}

if ($accel -match "not installed|HAX is not working|does not support") {
    Write-Host ""
    Write-Host "=== FIX: Emulator cannot start without hypervisor driver ===" -ForegroundColor Yellow
    Write-Host "1. Open PowerShell AS ADMINISTRATOR"
    Write-Host "2. Run:"
    Write-Host "   cd `"$AndroidSdkRoot\extras\google\Android_Emulator_Hypervisor_Driver`""
    Write-Host "   .\silent_install.bat"
    Write-Host "3. Reboot PC if prompted"
    Write-Host "4. Optional Windows Features (Run optionalfeatures.exe): enable"
    Write-Host "   - Windows Hypervisor Platform"
    Write-Host "   - Virtual Machine Platform"
    Write-Host "5. Run this script again: .\start-emulator.ps1"
    Write-Host ""
    Write-Host "Or run: .\install-emulator-driver.cmd (right-click -> Run as administrator)"
    exit 1
}

$avds = & $emulator -list-avds 2>&1
if ($avds -notcontains $EmulatorAvdName) {
    Write-Host "Available AVDs:"
    $avds | ForEach-Object { Write-Host "  $_" }
    Write-Error "Unknown AVD '$EmulatorAvdName'. Edit EmulatorAvdName in emulator.config.ps1"
    exit 1
}

$adb = Join-Path $AndroidSdkRoot "platform-tools\adb.exe"
& $adb kill-server 2>$null | Out-Null
Start-Sleep -Seconds 1
& $adb start-server 2>$null | Out-Null

Write-Host "Starting emulator: $EmulatorAvdName"
$emuArgs = @("-avd", $EmulatorAvdName, "-gpu", "swiftshader_indirect")
Start-Process -FilePath $emulator -ArgumentList $emuArgs -WindowStyle Normal

Write-Host "Waiting for adb (up to 5 min). Watch for the emulator window; if it closes instantly, run emulator -accel-check"
$deviceDeadline = (Get-Date).AddMinutes(5)
$seen = $false
while ((Get-Date) -lt $deviceDeadline) {
    $out = & $adb devices 2>&1 | Out-String
    if ($out -match "emulator-\d+\s+device") { $seen = $true; break }
    if ($out -match "offline") { Write-Host "  ... offline (booting)" } else { Write-Host "  ... waiting" }
    Start-Sleep -Seconds 5
}

if (-not $seen) {
    Write-Warning "adb still sees no device. Run: emulator -accel-check"
    exit 1
}

$bootDeadline = (Get-Date).AddMinutes(3)
do {
    $boot = (& $adb shell getprop sys.boot_completed 2>$null).Trim()
    if ($boot -eq "1") { break }
    Start-Sleep -Seconds 3
} while ((Get-Date) -lt $bootDeadline)

Enable-EmulatorDisplay -AdbPath $adb
Write-Host "Emulator ready. Run: .\install-and-run.cmd"
