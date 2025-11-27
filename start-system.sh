#!/bin/bash

# Thermal Ark 系统启动脚本
# 启动所有服务：数据库、消息队列、区块链、后端、前端、IoT模拟器

echo "🌡️  Thermal Ark 系统启动脚本"
echo "========================================"

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ Docker未安装，请先安装Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose未安装，请先安装Docker Compose"
    exit 1
fi

echo "✅ Docker环境检查通过"

# 创建必要的目录
echo "📁 创建数据目录..."
mkdir -p data/mysql data/redis data/rabbitmq data/blockchain

# 停止现有服务（如果有）
echo "🛑 停止现有服务..."
docker-compose down

# 构建并启动所有服务
echo "🚀 启动所有服务..."
docker-compose up -d --build

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

# 检查服务状态
echo "🔍 检查服务状态..."

services=("mysql" "redis" "rabbitmq" "fisco-bcos" "backend" "frontend" "iot-simulator" "iot-python")

for service in "${services[@]}"; do
    status=$(docker-compose ps $service | grep -o "Up")
    if [ "$status" = "Up" ]; then
        echo "✅ $service: 运行正常"
    else
        echo "❌ $service: 启动失败"
    fi
done

# 等待后端服务完全启动
echo "⏳ 等待后端服务完全启动..."
for i in {1..30}; do
    if curl -f http://localhost:8080/actuator/health &> /dev/null; then
        echo "✅ 后端服务已就绪"
        break
    fi
    echo "⏱️  等待后端服务... ($i/30)"
    sleep 5
done

# 运行集成测试
echo "🧪 运行集成测试..."
if command -v python3 &> /dev/null; then
    python3 integration-test.py
else
    echo "⚠️  Python3未安装，跳过集成测试"
fi

echo ""
echo "🎊 Thermal Ark 系统启动完成！"
echo ""
echo "📊 服务访问地址:"
echo "  • 前端界面: http://localhost"
echo "  • 后端API: http://localhost:8080"
echo "  • API文档: http://localhost:8080/swagger-ui.html"
echo "  • RabbitMQ管理: http://localhost:15672 (用户名: admin, 密码: admin123)"
echo "  • 区块链节点: http://localhost:8545"
echo "  • IoT模拟器: http://localhost:3001"
echo ""
echo "🔧 常用命令:"
echo "  • 查看日志: docker-compose logs -f [服务名]"
echo "  • 停止服务: docker-compose down"
echo "  • 重启服务: docker-compose restart"
echo "  • 查看状态: docker-compose ps"
echo ""
echo "💡 系统功能:"
echo "  • 智能热量表数据模拟和采集"
echo "  • 热能交易和支付集成"
echo "  • 区块链交易记录和验证"
echo "  • 消息队列异步处理"
echo "  • 完整的Web界面管理"