#!/usr/bin/env powershell

Write-Host "🚀 Iniciando Backend Spring Boot..." -ForegroundColor Green

# Navegar al directorio del proyecto
Set-Location "c:\Users\JOSUE\OneDrive\Escritorio\proyecto final\proyecto final\proyecto2\proyecto"

# Detener cualquier proceso anterior en el puerto 8081
$existingProcess = Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue
if ($existingProcess) {
    Write-Host "⏹️ Deteniendo proceso anterior en puerto 8081..." -ForegroundColor Yellow
    $processId = (Get-Process -Id $existingProcess.OwningProcess -ErrorAction SilentlyContinue).Id
    if ($processId) {
        Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 3
    }
}

# Compilar el proyecto
Write-Host "🔨 Compilando proyecto..." -ForegroundColor Blue
& cmd /c "mvnw.cmd clean package -DskipTests"

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Compilación exitosa" -ForegroundColor Green
    
    # Ejecutar el JAR
    Write-Host "🎯 Iniciando aplicación..." -ForegroundColor Cyan
    $jarPath = "target\proyecto-0.0.1-SNAPSHOT.jar"
    
    if (Test-Path $jarPath) {
        java -jar $jarPath
    } else {
        Write-Host "❌ JAR no encontrado en: $jarPath" -ForegroundColor Red
        Write-Host "📁 Archivos en target:" -ForegroundColor Yellow
        Get-ChildItem target\ -ErrorAction SilentlyContinue
    }
} else {
    Write-Host "❌ Error en la compilación" -ForegroundColor Red
}
