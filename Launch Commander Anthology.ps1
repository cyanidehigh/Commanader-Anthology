param(
    [string[]] $GradleArgs = @(":anthology-desktop:run", "--console=plain", "--no-daemon")
)

$ErrorActionPreference = "Stop"

$requiredJavaMajor = 17
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location -LiteralPath $projectRoot

function Get-JavaMajorVersion {
    param(
        [Parameter(Mandatory = $true)]
        [string] $JavaExe
    )

    $escapedJavaExe = $JavaExe.Replace('"', '\"')
    $versionText = cmd /c """$escapedJavaExe"" -version 2>&1" | Select-Object -First 1
    if ($versionText -notmatch 'version "([^"]+)"') {
        return $null
    }

    $version = $Matches[1]
    $majorText = ($version -split '\.')[0]
    if ($majorText -eq "1") {
        $majorText = ($version -split '\.')[1]
    }

    [pscustomobject]@{
        Major = [int] $majorText
        Version = $version
    }
}

function Find-SuitableJava {
    $candidates = New-Object System.Collections.Generic.List[string]

    if ($env:JAVA_HOME) {
        $candidates.Add((Join-Path $env:JAVA_HOME "bin\java.exe"))
    }

    $roots = @(
        "C:\Program Files\Java",
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files\Microsoft",
        "C:\Program Files\Zulu",
        "C:\Program Files\Amazon Corretto"
    )

    foreach ($root in $roots) {
        if (Test-Path -LiteralPath $root) {
            Get-ChildItem -Directory -LiteralPath $root -ErrorAction SilentlyContinue |
                Sort-Object Name -Descending |
                ForEach-Object {
                    $candidates.Add((Join-Path $_.FullName "bin\java.exe"))
                }
        }
    }

    $pathJava = (Get-Command java.exe -ErrorAction SilentlyContinue).Source
    if ($pathJava) {
        $candidates.Add($pathJava)
    }

    foreach ($candidate in $candidates | Select-Object -Unique) {
        if (-not (Test-Path -LiteralPath $candidate)) {
            continue
        }

        $version = Get-JavaMajorVersion -JavaExe $candidate
        if ($version -and $version.Major -ge $requiredJavaMajor) {
            [pscustomobject]@{
                JavaExe = $candidate
                JavaHome = (Resolve-Path -LiteralPath (Join-Path (Split-Path -Parent $candidate) "..")).Path
                Version = $version.Version
                Major = $version.Major
            }
            return
        }
    }
}

$java = Find-SuitableJava
if (-not $java) {
    Write-Host "Commander Anthology could not find Java $requiredJavaMajor or newer."
    Write-Host ""
    Write-Host "Install a current JDK, or set JAVA_HOME to your Java install folder."
    Write-Host "Example:"
    Write-Host "  C:\Program Files\Java\jdk-21.0.11"
    exit 1
}

$env:JAVA_HOME = $java.JavaHome

Write-Host "Using Java $($java.Version):"
Write-Host "  $($java.JavaExe)"
Write-Host ""
Write-Host "Starting Commander Anthology..."

& "$projectRoot\gradlew.bat" @GradleArgs
exit $LASTEXITCODE
