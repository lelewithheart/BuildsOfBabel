@echo off
REM Lightweight Maven wrapper for Windows (uses a PowerShell helper to download Maven on first run)
setlocal
set MVNW_ROOT=%~dp0
set MVNW_DIR=%MVNW_ROOT%.mvn\wrapper
set MAVEN_VER=3.9.11
set MAVEN_DIR=%MVNW_DIR%\apache-maven-%MAVEN_VER%

if not exist "%MAVEN_DIR%\bin\mvn.cmd" (
  echo Maven not found in wrapper directory. Downloading Apache Maven %MAVEN_VER%...
  if not exist "%MVNW_DIR%" mkdir "%MVNW_DIR%"
  powershell -NoProfile -ExecutionPolicy Bypass -File "%MVNW_ROOT%\.mvn\wrapper\download-maven.ps1" -Version "%MAVEN_VER%" -TargetDir "%MVNW_DIR%"
  if errorlevel 1 (
    echo Download failed. Please install Maven manually or use Chocolatey.
    exit /b 1
  )
)

set MVN_CMD=%MAVEN_DIR%\bin\mvn.cmd
if exist "%MVN_CMD%" (
  "%MVN_CMD%" %*
) else (
  echo Failed to find mvn executable after download.
  exit /b 1
)
