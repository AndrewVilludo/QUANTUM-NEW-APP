# Local emulator settings (command-line runs). Valid names: run emulator -list-avds
$script:AndroidSdkRoot = "$env:LOCALAPPDATA\Android\Sdk"
$script:EmulatorAvdName = "Pixel_6_Pro"
$script:JavaHome = "C:\Program Files\Android\Android Studio\jbr"

function Enable-EmulatorDisplay {
    param([string]$AdbPath)
    # Prevent black screen while emulator is "sleeping" (common when launched from CLI).
    & $AdbPath shell settings put global stay_on_while_plugged_in 3 2>$null | Out-Null
    & $AdbPath shell input keyevent KEYCODE_WAKEUP 2>$null | Out-Null
    & $AdbPath shell wm dismiss-keyguard 2>$null | Out-Null
}
