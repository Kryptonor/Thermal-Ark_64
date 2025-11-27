# IoT Data Simulator - Thermal Ark Energy Trading Platform

物联网数据模拟器，为热能方舟能源交易平台生成模拟的能源设备数据。

## 功能特性

- 🔌 **多设备模拟**: 支持太阳能板、热泵、电池、风力发电机、热能存储等多种设备
- 📊 **实时数据生成**: 模拟真实设备数据，包括功率输出、温度、效率等参数
- 🌐 **双通道传输**: 同时支持WebSocket实时推送和REST API批量上传
- 🔒 **数据安全**: 支持数据签名验证，确保数据完整性
- ⚙️ **灵活配置**: 可配置设备参数、数据间隔、故障率等
- 🧪 **测试工具**: 提供完整的测试套件，验证系统功能

## 设备类型

| 设备类型 | 容量范围 | 模拟参数 | 故障率 |
|---------|---------|---------|--------|
| 太阳能板 | 10-100kW | 发电量、温度、效率 | 5% |
| 热泵 | 10-50kW | 热量输出、能耗、COP | 3% |
| 电池 | 50-200kWh | 充电水平、电压、电流 | 2% |
| 风力发电机 | 5-30kW | 风速、发电量、转子速度 | 4% |
| 热能存储 | 100-300kWh | 存储能量、温度、压力 | 3% |

## 快速开始

### 1. 安装依赖

```bash
cd iot-simulator
npm install
```

### 2. 配置环境

复制环境配置文件：

```bash
cp .env.example .env
```

编辑 `.env` 文件，配置服务器地址和模拟参数：

```env
# WebSocket服务器地址
WS_URL=ws://localhost:8080/ws/iot

# REST API基础地址
API_BASE_URL=http://localhost:8080/api

# 数据生成间隔（毫秒）
DATA_INTERVAL_MIN=2000
DATA_INTERVAL_MAX=5000

# 日志级别
LOG_LEVEL=info
```

### 3. 运行模拟器

**开发模式（自动重启）：**
```bash
npm run dev
```

**生产模式：**
```bash
npm start
```

**测试模式：**
```bash
npm test
```

## 配置说明

### 设备配置

在 `src/config.js` 中配置模拟设备：

```javascript
{
  id: 'solar-001',           // 设备唯一标识
  type: 'solar_panel',       // 设备类型
  name: '太阳能板 A栋',       // 设备名称
  location: 'Building A',     // 安装位置
  capacity: 50,              // 设备容量(kW/kWh)
  coordinates: {             // 地理坐标
    lat: 39.9042, 
    lng: 116.4074 
  },
  description: '屋顶太阳能发电系统' // 设备描述
}
```

### 数据格式

**WebSocket消息格式：**
```json
{
  "type": "iot_data",
  "deviceId": "solar-001",
  "data": {
    "deviceId": "solar-001",
    "type": "solar_panel",
    "location": "Building A",
    "timestamp": "2024-01-15T10:30:00.000Z",
    "powerOutput": 42.5,
    "temperature": 28.3,
    "efficiency": 0.89,
    "status": "active"
  }
}
```

**REST API数据格式：**
```json
{
  "deviceId": "solar-001",
  "data": {
    "deviceId": "solar-001",
    "type": "solar_panel",
    "location": "Building A",
    "timestamp": "2024-01-15T10:30:00.000Z",
    "powerOutput": 42.5,
    "temperature": 28.3,
    "efficiency": 0.89,
    "status": "active"
  },
  "signature": "a1b2c3d4e5f6..."
}
```

## API接口

### WebSocket端点
- **地址**: `ws://localhost:8080/ws/iot`
- **协议**: 实时数据推送

### REST API端点
- **健康检查**: `GET /api/health`
- **数据接收**: `POST /api/iot/data`

## 故障模拟

模拟器支持设备故障模拟，故障率可配置：

- **太阳能板**: 5%概率出现故障状态
- **热泵**: 3%概率需要维护
- **电池**: 2%概率充放电异常
- **风力发电机**: 4%概率空闲状态
- **热能存储**: 3%概率充能状态

## 开发指南

### 添加新设备类型

1. 在 `src/config.js` 的 `devices` 数组中添加新设备
2. 在 `src/simulator.js` 的 `generateDeviceData` 方法中添加数据生成逻辑
3. 在配置的 `validation.ranges` 中定义数据验证规则

### 自定义数据模式

启用真实模式可模拟日夜变化、季节影响等：

```javascript
// 在 config.js 中启用
enableRealisticPatterns: true
```

### 扩展传输协议

支持添加MQTT、Kafka等传输协议：

```javascript
// 在 simulator.js 中添加新的传输方法
async sendToMQTT(device, data) {
  // MQTT客户端实现
}
```

## 故障排除

### 常见问题

1. **WebSocket连接失败**
   - 检查后端服务器是否运行
   - 验证WebSocket地址配置
   - 查看防火墙设置

2. **REST API请求超时**
   - 检查网络连接
   - 验证API地址和端口
   - 调整超时时间配置

3. **数据格式错误**
   - 检查设备配置
   - 验证数据验证规则
   - 查看日志输出

### 日志分析

启用详细日志：
```bash
LOG_LEVEL=debug npm start
```

## 部署说明

### Docker部署

```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install --production
COPY . .
EXPOSE 3001
CMD ["npm", "start"]
```

### 环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| WS_URL | ws://localhost:8080/ws/iot | WebSocket地址 |
| API_BASE_URL | http://localhost:8080/api | API基础地址 |
| DATA_INTERVAL_MIN | 2000 | 最小数据间隔(ms) |
| DATA_INTERVAL_MAX | 5000 | 最大数据间隔(ms) |
| LOG_LEVEL | info | 日志级别 |

## 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

## 贡献指南

欢迎提交Issue和Pull Request来改进这个项目。

## 联系方式

- 项目主页: [Thermal Ark Energy Trading Platform](https://github.com/thermal-ark)
- 问题反馈: [GitHub Issues](https://github.com/thermal-ark/iot-simulator/issues)
- 邮箱: contact@thermal-ark.org