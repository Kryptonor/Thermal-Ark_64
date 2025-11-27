# IoT数据模拟器

Thermal Ark平台的IoT数据模拟器，用于生成模拟的热力传感器数据。

## 功能特性

- 🔥 模拟多种类型的热力传感器（工业、商业、住宅）
- 📊 生成温度、流量、能量输出等实时数据
- 🌐 支持HTTP API和WebSocket两种数据推送方式
- ⚙️ 可配置的传感器数量和数据生成间隔
- 📈 自动生成每日统计报告
- 🐳 Docker容器化部署

## 快速开始

### 环境要求

- Node.js 18+
- npm 或 yarn

### 安装依赖

```bash
npm install
```

### 配置环境变量

复制环境变量模板：

```bash
cp .env.example .env
```

编辑`.env`文件：

```env
# 后端服务地址
BACKEND_URL=http://localhost:8080

# WebSocket地址
WS_URL=ws://localhost:8080/ws

# 模拟器ID
SIMULATOR_ID=iot-simulator-001

# 数据生成间隔（毫秒）
INTERVAL=5000

# 传感器数量
NUM_SENSORS=10
```

### 运行模拟器

```bash
# 开发模式（自动重启）
npm run dev

# 生产模式
npm start

# 运行测试
npm test
```

## 数据格式

### 传感器数据格式

```json
{
  "sensorId": "sensor-1",
  "location": "北京市朝阳区",
  "type": "industrial",
  "temperature": 85.5,
  "flowRate": 120.3,
  "energyOutput": 2500,
  "status": "normal",
  "timestamp": "2024-01-01T12:00:00.000Z"
}
```

### 每日报告格式

```json
{
  "type": "daily_report",
  "simulatorId": "iot-simulator-001",
  "date": "2024-01-01",
  "totalSensors": 10,
  "estimatedDailyEnergy": 12500,
  "timestamp": "2024-01-01T00:00:00.000Z"
}
```

## API接口

### 数据接收接口

模拟器会向以下接口发送数据：

- **POST** `/api/thermal-data` - 接收传感器数据
- **WebSocket** `/ws` - 实时数据推送

### 请求头

```http
Content-Type: application/json
X-Simulator-ID: iot-simulator-001
```

## WebSocket消息

### 发送消息

模拟器会发送以下类型的消息：

1. **注册消息**
```json
{
  "type": "register",
  "simulatorId": "iot-simulator-001",
  "timestamp": "2024-01-01T12:00:00.000Z"
}
```

2. **传感器数据**
```json
{
  "type": "sensor_data",
  "data": {
    "sensorId": "sensor-1",
    "temperature": 85.5,
    "flowRate": 120.3,
    "energyOutput": 2500,
    "status": "normal"
  },
  "timestamp": "2024-01-01T12:00:00.000Z"
}
```

### 接收消息

模拟器可以接收以下类型的消息：

1. **配置更新**
```json
{
  "type": "config_update",
  "config": {
    "interval": 10000,
    "numSensors": 15
  }
}
```

2. **传感器控制**
```json
{
  "type": "sensor_control",
  "command": "restart",
  "sensorId": "sensor-1"
}
```

## Docker部署

### 构建镜像

```bash
docker build -t thermal-ark-iot .
```

### 运行容器

```bash
docker run -d \
  --name iot-simulator \
  -p 3001:3001 \
  -e BACKEND_URL=http://backend:8080 \
  -e WS_URL=ws://backend:8080/ws \
  thermal-ark-iot
```

### 使用Docker Compose

```yaml
version: '3.8'
services:
  iot:
    build: ./iot
    environment:
      - BACKEND_URL=http://backend:8080
      - WS_URL=ws://backend:8080/ws
      - SIMULATOR_ID=iot-simulator-001
      - INTERVAL=5000
      - NUM_SENSORS=10
    depends_on:
      - backend
```

## 传感器类型

### 工业传感器
- 温度范围：85-105°C
- 流量范围：200-300 m³/h
- 适用于大型工厂、热电厂

### 商业传感器
- 温度范围：75-90°C
- 流量范围：100-150 m³/h
- 适用于商场、写字楼

### 住宅传感器
- 温度范围：65-75°C
- 流量范围：50-80 m³/h
- 适用于居民小区

## 故障排除

### 常见问题

1. **WebSocket连接失败**
   - 检查后端服务是否运行
   - 验证WebSocket URL配置

2. **API调用失败**
   - 检查网络连接
   - 验证后端服务地址

3. **数据生成异常**
   - 检查环境变量配置
   - 查看日志输出

### 日志查看

```bash
# 查看容器日志
docker logs iot-simulator

# 实时查看日志
docker logs -f iot-simulator
```

## 开发指南

### 添加新的传感器类型

1. 在`SensorSimulator`类中添加新的类型配置
2. 更新`getBaseTemperature`和`getBaseFlowRate`方法
3. 测试新的数据范围

### 扩展数据格式

1. 修改`generateData`方法
2. 更新数据发送逻辑
3. 确保后端API兼容

## 许可证

MIT License