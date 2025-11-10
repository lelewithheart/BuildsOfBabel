param(
    [string]$Version = "3.9.11",
    [string]$TargetDir
)

try {
    if ([string]::IsNullOrEmpty($TargetDir)) {
        Write-Error "TargetDir parameter is required.";
        exit 1
    }

    $names = @(
        "https://downloads.apache.org/maven/maven-3/$Version/binaries/apache-maven-$Version-bin.zip",
        "https://dlcdn.apache.org/maven/maven-3/$Version/binaries/apache-maven-$Version-bin.zip",
        "https://archive.apache.org/dist/maven/maven-3/$Version/binaries/apache-maven-$Version-bin.zip"
    )

    $zip = Join-Path $env:TEMP "apache-maven-$Version-bin.zip"
    if (Test-Path $zip) { Remove-Item $zip -Force -ErrorAction SilentlyContinue }

    $downloaded = $false
    foreach ($url in $names) {
        try {
            Write-Host "Attempting download from: $url"
            Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing -ErrorAction Stop
            $downloaded = $true
            break
        } catch {
            Write-Warning "Download attempt failed from $url : $_"
        }
    }

    if (-not $downloaded) {
        Write-Error "All download attempts failed."
        exit 1
    }

    Write-Host "Extracting to $TargetDir"
    if (-Not (Test-Path -Path $TargetDir)) { New-Item -ItemType Directory -Path $TargetDir | Out-Null }
    Expand-Archive -Path $zip -DestinationPath $TargetDir -Force -ErrorAction Stop

    Write-Host "Maven downloaded and extracted."
    exit 0
} catch {
    Write-Error "Failed to download or extract Maven: $_"
    exit 1
}
