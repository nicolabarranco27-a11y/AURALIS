# Corre los tests instrumentados (androidTest) en el celular o emulador conectado.
#
# Requisitos:
#   - Un celular por USB con depuracion USB activada, o un emulador encendido.
#   - Verificar con:  adb devices
#
# Uso:
#   .\tools\run-instrumented-tests.ps1

[CmdletBinding()]
param(
    [string]$SdkDir
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$localProps = Join-Path $repoRoot "local.properties"

if (-not $SdkDir -and (Test-Path $localProps)) {
    $match = Select-String -Path $localProps -Pattern '^sdk\.dir=(.+)$' | Select-Object -First 1
    if ($match) {
        $SdkDir = $match.Matches[0].Groups[1].Value -replace '\\\\', '\' -replace '\\:', ':'
    }
}
if (-not $SdkDir) { $SdkDir = $env:ANDROID_HOME }
if (-not $SdkDir) { $SdkDir = $env:ANDROID_SDK_ROOT }
if (-not $SdkDir) { $SdkDir = Join-Path $env:LOCALAPPDATA "Android\Sdk" }

$adb = Join-Path $SdkDir "platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
    Write-Error "No se encontro adb en '$SdkDir'. Pasa -SdkDir o conecta un dispositivo."
}

$devices = & $adb devices | Select-String -Pattern "device$"
if (-not $devices) {
    Write-Host "No hay dispositivos conectados." -ForegroundColor Yellow
    Write-Output "Opciones:"
    Write-Output "  1) Conectar un celular por USB (depuracion USB) y volver a correr el script."
    Write-Output "  2) Crear/encender un emulador desde Android Studio > Device Manager."
    Write-Output "     Emuladores sugeridos (todas las gamas): API 26 (gama baja), API 29, API 33, API 36."
    exit 1
}

Write-Host "Dispositivos detectados:" -ForegroundColor Cyan
& $SdkDir\platform-tools\adb.exe devices

Push-Location $repoRoot
try {
    & "$repoRoot\gradlew.bat" :app:connectedDebugAndroidTest --console=plain
} finally {
    Pop-Location
}

$reportDir = Join-Path $repoRoot "app\build\reports\androidTests\connected"
if (Test-Path $reportDir) {
    Write-Output ""
    Write-Output "Reporte HTML: $reportDir\index.html"
}
