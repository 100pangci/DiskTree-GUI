Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$RootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $RootDir

$TreeRepoUrl = 'https://github.com/100pangci/MyTools.git'
$TreeSourceRelativePath = 'src\Python\4.扫描文件树\Tree.py'
$TempDir = Join-Path $RootDir '.build-temp'
$TreeRepoDir = Join-Path $TempDir 'MyTools'
$TreeBuildDir = Join-Path $TempDir 'tree-build'

$ReleasesDir = Join-Path $RootDir 'Releases'
if (Test-Path $ReleasesDir) {
    Remove-Item $ReleasesDir -Recurse -Force
}
New-Item -ItemType Directory -Path $ReleasesDir | Out-Null

if (Test-Path $TempDir) {
    Remove-Item $TempDir -Recurse -Force
}
New-Item -ItemType Directory -Path $TempDir | Out-Null

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

Write-Host '==> Cloning MyTools repository for Tree.py source'
git clone --depth 1 $TreeRepoUrl $TreeRepoDir

$TreeSourcePath = Join-Path $TreeRepoDir $TreeSourceRelativePath
if (-not (Test-Path $TreeSourcePath)) {
    throw "Tree.py not found at $TreeSourcePath"
}

Write-Host '==> Installing PyInstaller'
python -m pip install --upgrade pip pyinstaller

Write-Host '==> Packaging Tree.py with PyInstaller'
python -m PyInstaller --clean --noconfirm --onefile --name Tree $TreeSourcePath --distpath (Join-Path $TreeBuildDir 'dist') --workpath (Join-Path $TreeBuildDir 'build') --specpath $TreeBuildDir
Copy-IfExists -SourcePath (Join-Path $TreeBuildDir 'dist') -TargetDir (Join-Path $ReleasesDir 'tree')

Write-Host '==> Generating Gradle wrapper with Gradle 8.10'
gradle -b wrapper.gradle.kts wrapper --no-daemon

Write-Host '==> Building Desktop portable distribution for current OS'
.\gradlew.bat :composeApp:createDistributable --no-daemon
Copy-IfExists -SourcePath (Join-Path $RootDir 'composeApp\build\compose\binaries') -TargetDir (Join-Path $ReleasesDir 'desktop')

Write-Host "==> Release artifacts collected in $ReleasesDir"