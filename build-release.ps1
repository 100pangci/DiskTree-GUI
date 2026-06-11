Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$RootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $RootDir

$ReleasesDir = Join-Path $RootDir 'Releases'
if (Test-Path $ReleasesDir) {
    Remove-Item $ReleasesDir -Recurse -Force
}
New-Item -ItemType Directory -Path $ReleasesDir | Out-Null

function Copy-IfExists {
    param(
        [string]$SourcePath,
        [string]$TargetDir
    )

    if (Test-Path $SourcePath) {
        New-Item -ItemType Directory -Path $TargetDir -Force | Out-Null
        Copy-Item -Path $SourcePath -Destination $TargetDir -Recurse -Force
    }
}

Write-Host '==> Generating Gradle wrapper with Gradle 8.10'
gradle -b wrapper.gradle.kts wrapper --no-daemon

Write-Host '==> Building Desktop portable distribution for current OS'
.\gradlew.bat :composeApp:createDistributable --no-daemon
Copy-IfExists -SourcePath (Join-Path $RootDir 'composeApp\build\compose\binaries') -TargetDir (Join-Path $ReleasesDir 'desktop')

$PortableAppDir = Join-Path $RootDir 'composeApp\build\compose\binaries\main\app\DiskTree GUI'
$PortableZipPath = Join-Path $ReleasesDir 'DiskTree-GUI-windows-portable.zip'

if (Test-Path $PortableAppDir) {
    Write-Host "==> Creating portable zip: $PortableZipPath"
    Compress-Archive -Path (Join-Path $PortableAppDir '*') -DestinationPath $PortableZipPath -Force
} else {
    Write-Warning "Portable app directory not found: $PortableAppDir"
}

Write-Host "==> Release artifacts collected in $ReleasesDir"