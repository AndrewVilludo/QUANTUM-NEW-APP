# Right-click PowerShell -> Run as administrator, then:
#   cd C:\Users\andre\OneDrive\Documents\thesis\QuantumRealm
#   .\enable-whpx-admin.ps1

$admin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole(
    [Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $admin) {
    Write-Error "Run this script in an Administrator PowerShell window."
    exit 1
}

Write-Host "=== Current state ===" -ForegroundColor Cyan
Write-Host "HypervisorPresent: $((Get-CimInstance Win32_ComputerSystem).HypervisorPresent)"
cmd /c "bcdedit /enum {current}" | Select-String -Pattern "hypervisorlaunchtype"

Write-Host "`n=== Enabling WHPX features (DISM) ===" -ForegroundColor Cyan
dism /online /enable-feature /featurename:HypervisorPlatform /all /norestart
dism /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart

Write-Host "`n=== Ensure hypervisor is not disabled in boot config ===" -ForegroundColor Cyan
cmd /c "bcdedit /set hypervisorlaunchtype auto"

Write-Host "`n=== Optional features state ===" -ForegroundColor Cyan
Get-WindowsOptionalFeature -Online -FeatureName HypervisorPlatform, VirtualMachinePlatform |
    Select-Object FeatureName, State

Write-Host "`n*** REBOOT REQUIRED ***" -ForegroundColor Yellow
Write-Host "After reboot, run:"
Write-Host "  (Get-CimInstance Win32_ComputerSystem).HypervisorPresent"
Write-Host "Expected: True"
Write-Host ""
$ans = Read-Host "Reboot now? (y/n)"
if ($ans -eq 'y') { shutdown /r /t 30 /c "Enable Android emulator WHPX" }
