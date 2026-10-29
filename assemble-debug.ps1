. "$PSScriptRoot\emulator.config.ps1"

$env:JAVA_HOME = $JavaHome
Set-Location $PSScriptRoot
.\gradlew.bat assembleDebug @args
exit $LASTEXITCODE
