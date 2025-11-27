@echo off
chcp 65001 >nul

REM Thermal Ark 系统启动脚本 (Windows版本)
REM 启动所有服务：数据库、消息队列、区块链、后端、前端、IoT模拟器

echo 🌡️  Thermal Ark 系统启动脚本
echo ========================================

REM 检查Docker是否安装
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker未安装，请先安装Docker
    pause
    exit /b 1
)

docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker Compose未安装，请先安装Docker Compose
    pause
    exit /b 1
)

echo ✅ Docker环境检查通过

REM 创建必要的目录
echo 📁 创建数据目录...
if not exist data\mysql mkdir data\mysql
if not exist data\redis mkdir data\redis
if not exist data\rabbitmq mkdir data\rabbitmq
if not exist data\blockchain mkdir data\blockchain

REM 停止现有服务（如果有）
echo 🛑 停止现有服务...
docker-compose down

REM 构建并启动所有服务
echo 🚀 启动所有服务...
docker-compose up -d --build

REM 等待服务启动
echo ⏳ 等待服务启动...
timeout /t 30 /nobreak >nul

REM 检查服务状态
echo 🔍 检查服务状态...

for %%s in (mysql redis rabbitmq fisco-bcos backend frontend iot-simulator iot-python) do (
    docker-compose ps %%s | findstr "Up" >nul
    if !errorlevel! equ 0 (
        echo ✅ %%s: 运行正常
    ) else (
        echo ❌ %%s: 启动失败
    )
)

REM 等待后端服务完全启动
echo ⏳ 等待后端服务完全启动...
for /l %%i in (1,1,30) do (
    curl -f http://localhost:8080/actuator/health >nul 2>&1
    if !errorlevel! equ 0 (
        echo ✅ 后端服务已就绪
        goto :backend_ready
    )
    echo ⏱️  等待后端服务... (%%i/30)
    timeout /t 5 /nobreak >nul
)

:backend_ready

REM 运行集成测试
echo 🧪 运行集成测试...
python --version >nul 2>&1
if !errorlevel! equ 0 (
    python integration-test.py
) else (
    python3 --version >nul 2>&1
    if !errorlevel! equ 0 (
        python3 integration-test.py
    ) else (
        echo ⚠️  Python未安装，跳过集成测试
    )
)

echo.
echo 🎊 Thermal Ark 系统启动完成！
echo.
echo 📊 服务访问地址:
echo   • 前端界面: http://localhost
echo   • 后端API: http://localhost:8080
echo   • API文档: http://localhost:8080/swagger-ui.html
echo   • RabbitMQ管理: http://localhost:15672 (用户名: admin, 密码: admin123)
echo   • 区块链节点: http://localhost:8545
echo   • IoT模拟器: http://localhost:3001
echo.
echo 🔧 常用命令:
echo   • 查看日志: docker-compose logs -f [服务名]
echo   • 停止服务: docker-compose down
echo   • 重启服务: docker-compose restart
echo   • 查看状态: docker-compose ps
echo.
echo 💡 系统功能:
echo   • 智能热量表数据模拟和采集
echo   • 热能交易和支付集成
echo   • 区块链交易记录和验证
echo   • 消息队列异步处理
echo   • 完整的Web界面管理

pause