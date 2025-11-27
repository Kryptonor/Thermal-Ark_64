@echo off
setlocal enabledelayedexpansion

REM Thermal Ark Web Application Deployment Script for Windows
REM This script automates the deployment of the entire Thermal Ark system

echo 🚀 Starting Thermal Ark Web Application Deployment...

REM Check if Docker is installed
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker is not installed. Please install Docker first.
    exit /b 1
)

REM Check if Docker Compose is installed
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker Compose is not installed. Please install Docker Compose first.
    exit /b 1
)

echo ✅ Docker and Docker Compose are available

REM Build and start all services
echo 📦 Building and starting all services...
docker-compose down
if %errorlevel% neq 0 (
    echo ⚠️ Some services may not have been running
)

docker-compose up --build -d
if %errorlevel% neq 0 (
    echo ❌ Failed to start services
    exit /b 1
)

REM Wait for services to be ready
echo ⏳ Waiting for services to be ready...
timeout /t 30 /nobreak >nul

REM Check service status
echo 🔍 Checking service status...

REM Check MySQL (skip if command fails)
docker-compose exec mysql mysqladmin ping -h localhost --silent >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ MySQL is running
) else (
    echo ❌ MySQL is not responding
)

REM Check Redis (skip if command fails)
docker-compose exec redis redis-cli ping | findstr "PONG" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Redis is running
) else (
    echo ❌ Redis is not responding
)

REM Check Backend (skip if command fails)
curl -f http://localhost:8080/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Backend service is running
) else (
    echo ❌ Backend service is not responding
)

REM Check Frontend (skip if command fails)
curl -f http://localhost:80 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Frontend service is running
) else (
    echo ❌ Frontend service is not responding
)

echo.
echo 🎉 Deployment completed!
echo.
echo 📊 Application URLs:
echo    Frontend: http://localhost:80
echo    Backend API: http://localhost:8080
echo    IoT Simulator: http://localhost:3001
echo    MySQL: localhost:3306
echo    Redis: localhost:6379
echo.
echo 🔧 Management Commands:
echo    View logs: docker-compose logs -f
echo    Stop services: docker-compose down
echo    Restart services: docker-compose restart
echo.
echo 📝 Next Steps:
echo    1. Access the frontend at http://localhost:80
echo    2. The IoT simulator will automatically start sending data
echo    3. Monitor the system using: docker-compose logs -f
echo.
echo 🐳 Running Containers:
docker-compose ps