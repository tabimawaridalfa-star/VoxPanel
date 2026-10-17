param(
    [string]$ToolsRoot = (Join-Path $env:USERPROFILE '.codex\tools\android'),
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$AndroidSdkRoot = $env:ANDROID_HOME
)
$ErrorActionPreference = 'Stop'
if ($JavaHome) { $java = Join-Path $JavaHome 'bin\java.exe' }
else { $java = (Get-ChildItem -LiteralPath (Join-Path $ToolsRoot 'jdk17') -Filter java.exe -Recurse | Select-Object -First 1).FullName }
$keytool = Join-Path (Split-Path $java) 'keytool.exe'
$sdk = Join-Path $ToolsRoot 'sdk'
if ($AndroidSdkRoot) {
    $platform = Join-Path $AndroidSdkRoot 'platforms\android-33\android.jar'
    $buildTools = Join-Path $AndroidSdkRoot 'build-tools\34.0.0'
    if (-not(Test-Path -LiteralPath $buildTools)) { $buildTools = Join-Path $AndroidSdkRoot 'build-tools\33.0.2' }
} else {
    $platform = (Get-ChildItem -LiteralPath (Join-Path $sdk 'platform33') -Filter android.jar -Recurse | Select-Object -First 1).FullName
    $buildTools = Split-Path (Get-ChildItem -LiteralPath (Join-Path $sdk 'minimal-buildtools') -Filter aapt.exe -Recurse | Select-Object -First 1).FullName
}
$output = Join-Path $PSScriptRoot 'build'
$classes = Join-Path $output 'classes'
$dex = Join-Path $output 'dex'
New-Item -ItemType Directory -Path $classes,$dex -Force | Out-Null
# Remove only generated files inside this project's build folders, so renamed
# Java classes cannot silently remain in the next APK.
foreach ($folder in @($classes,$dex)) {
    $resolved = (Resolve-Path -LiteralPath $folder).Path
    if (-not $resolved.StartsWith(((Resolve-Path -LiteralPath $output).Path + [IO.Path]::DirectorySeparatorChar), [StringComparison]::OrdinalIgnoreCase)) { throw 'Unexpected build output location' }
    Get-ChildItem -LiteralPath $resolved -File -Recurse | Remove-Item -Force
}
function Check-Result([string]$step) { if ($LASTEXITCODE -ne 0) { throw "$step gagal, exit code $LASTEXITCODE" } }
$sources = @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'src') -Filter '*.java' -Recurse | ForEach-Object FullName)
& (Join-Path (Split-Path $java) 'javac.exe') --release 8 -encoding UTF-8 -proc:none -classpath $platform -d $classes @sources
Check-Result 'Kompilasi Java'
$classFiles = @(Get-ChildItem -LiteralPath $classes -Filter '*.class' -Recurse | ForEach-Object FullName)
& $java -cp (Join-Path $buildTools 'lib\d8.jar') com.android.tools.r8.D8 --min-api 26 --lib $platform --output $dex @classFiles
Check-Result 'Kompilasi DEX'
$unsigned = Join-Path $output 'unsigned.apk'
[xml]$manifest = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'AndroidManifest.xml') -Raw
$manifest.manifest.SetAttribute('package', 'id.local.tecnovoiceprobe')
$manifestPath = Join-Path $output 'AndroidManifest.xml'
$manifest.Save($manifestPath)
& (Join-Path $buildTools 'aapt.exe') package -f -M $manifestPath -S (Join-Path $PSScriptRoot 'res') -I $platform -F $unsigned
Check-Result 'Paket manifest'
Push-Location $dex
try {
    & (Join-Path $buildTools 'aapt.exe') add $unsigned 'classes.dex'
    Check-Result 'Paket DEX'
} finally { Pop-Location }
$aligned = Join-Path $output 'aligned.apk'
& (Join-Path $buildTools 'zipalign.exe') -f 4 $unsigned $aligned
Check-Result 'Alignment'
$keystore = Join-Path $output 'debug.keystore'
if (-not (Test-Path -LiteralPath $keystore)) {
    & $keytool -genkeypair -keystore $keystore -storepass android -keypass android -alias androiddebugkey -dname 'CN=Local Android Debug' -keyalg RSA -keysize 2048 -validity 3650
    Check-Result 'Debug key'
}
$apk = Join-Path $output 'tecno-voice-probe.apk'
& $java -jar (Join-Path $buildTools 'lib\apksigner.jar') sign --ks $keystore --ks-pass pass:android --key-pass pass:android --out $apk $aligned
Check-Result 'Tanda tangan APK'
& $java -jar (Join-Path $buildTools 'lib\apksigner.jar') verify --verbose $apk
Check-Result 'Verifikasi tanda tangan'
Write-Output "APK: $apk"
