$ErrorActionPreference = "Stop"
$BackendDir = Join-Path $PSScriptRoot "backend"

$MvnPath = "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-3.9.6-bin\439sdfsg2nbdob9ciift5h5nse\apache-maven-3.9.6\bin\mvn.cmd"
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    $MvnPath = "mvn"
}

Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host " Starting Moroccan Darija LLM Translator Backend...   " -ForegroundColor Green
Write-Host " Endpoint: http://localhost:8080/api/translator        " -ForegroundColor Yellow
Write-Host " Auth: admin / password123                             " -ForegroundColor Yellow
Write-Host "=======================================================" -ForegroundColor Cyan

Set-Location $BackendDir
& $MvnPath exec:java "-Dexec.mainClass=com.darija.translator.ServerMain"
