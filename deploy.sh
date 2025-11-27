#!/bin/bash

# Thermal Ark Web Application Deployment Script
# This script automates the deployment of the entire Thermal Ark system

echo "🚀 Starting Thermal Ark Web Application Deployment..."

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

echo "✅ Docker and Docker Compose are available"

# Build and start all services
echo "📦 Building and starting all services..."
docker-compose down  # Stop any existing services
docker-compose up --build -d

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 30

# Check service status
echo "🔍 Checking service status..."

# Check MySQL
if docker-compose exec mysql mysqladmin ping -h localhost --silent; then
    echo "✅ MySQL is running"
else
    echo "❌ MySQL is not responding"
fi

# Check Redis
if docker-compose exec redis redis-cli ping | grep -q PONG; then
    echo "✅ Redis is running"
else
    echo "❌ Redis is not responding"
fi

# Check Backend
if curl -f http://localhost:8080/actuator/health &> /dev/null; then
    echo "✅ Backend service is running"
else
    echo "❌ Backend service is not responding"
fi

# Check Frontend
if curl -f http://localhost:80 &> /dev/null; then
    echo "✅ Frontend service is running"
else
    echo "❌ Frontend service is not responding"
fi

# Check IoT Simulator
if curl -f http://localhost:3001 &> /dev/null; then
    echo "✅ IoT Simulator is running"
else
    echo "⚠️ IoT Simulator may not be accessible via HTTP (this is normal)"
fi

echo ""
echo "🎉 Deployment completed!"
echo ""
echo "📊 Application URLs:"
echo "   Frontend: http://localhost:80"
echo "   Backend API: http://localhost:8080"
echo "   IoT Simulator: http://localhost:3001"
echo "   MySQL: localhost:3306"
echo "   Redis: localhost:6379"
echo ""
echo "🔧 Management Commands:"
echo "   View logs: docker-compose logs -f"
echo "   Stop services: docker-compose down"
echo "   Restart services: docker-compose restart"
echo "   Scale services: docker-compose up --scale backend=2"
echo ""
echo "📝 Next Steps:"
echo "   1. Access the frontend at http://localhost:80"
echo "   2. The IoT simulator will automatically start sending data"
echo "   3. Monitor the system using: docker-compose logs -f"

# Display current running containers
echo ""
echo "🐳 Running Containers:"
docker-compose ps