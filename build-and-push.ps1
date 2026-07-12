# Script para construir y subir todas las imagenes a Docker Hub
# Uso: .\build-and-push.ps1
# Requiere: docker login maxxito (ejecutar antes de correr este script)

$DOCKER_HUB_USER = "maxxito"
$ErrorActionPreference = "Stop"

$BACKEND_SERVICES = @(
    "api_gateway",
    "bff",
    "ms_alertas",
    "ms_usuarios",
    "ms_monitoreo",
    "ms_reportes",
    "ms_integracion"
)

Write-Host "=== Build y Push a Docker Hub ($DOCKER_HUB_USER) ===" -ForegroundColor Magenta
Write-Host ""

foreach ($service in $BACKEND_SERVICES) {
    $imageName = "${DOCKER_HUB_USER}/${service}:latest"
    Write-Host "[BUILD] $imageName" -ForegroundColor Cyan
    docker build -t $imageName "./$service"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Fallo al construir $service" -ForegroundColor Red
        exit 1
    }
    Write-Host "[PUSH]  $imageName" -ForegroundColor Yellow
    docker push $imageName
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Fallo al subir $service" -ForegroundColor Red
        exit 1
    }
    Write-Host "[OK]    $imageName" -ForegroundColor Green
    Write-Host ""
}

# Frontend requiere build arg para URLs relativas (Nginx reverse proxy)
$frontendImage = "${DOCKER_HUB_USER}/frontend:latest"
Write-Host "[BUILD] $frontendImage (con Nginx)" -ForegroundColor Cyan
docker build -t $frontendImage --build-arg VITE_API_URL="" ./frontend
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Fallo al construir frontend" -ForegroundColor Red
    exit 1
}
Write-Host "[PUSH]  $frontendImage" -ForegroundColor Yellow
docker push $frontendImage
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Fallo al subir frontend" -ForegroundColor Red
    exit 1
}
Write-Host "[OK]    $frontendImage" -ForegroundColor Green
Write-Host ""

Write-Host "=== Todas las imagenes subidas exitosamente a Docker Hub ===" -ForegroundColor Green
Write-Host ""
Write-Host "Imagenes disponibles en: https://hub.docker.com/u/$DOCKER_HUB_USER" -ForegroundColor White
