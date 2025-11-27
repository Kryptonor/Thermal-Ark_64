import WebSocket from 'ws';
import axios from 'axios';
import crypto from 'crypto';

// IoT设备配置
const DEVICES = [
  { id: 'device-001', type: 'solar_panel', location: 'Building A', capacity: 50 }, // 50kW太阳能板
  { id: 'device-002', type: 'heat_pump', location: 'Building B', capacity: 30 },   // 30kW热泵
  { id: 'device-003', type: 'battery', location: 'Building C', capacity: 100 },    // 100kWh电池
  { id: 'device-004', type: 'wind_turbine', location: 'Building D', capacity: 20 }, // 20kW风力发电机
  { id: 'device-005', type: 'thermal_storage', location: 'Building E', capacity: 200 } // 200kWh热能存储
];

// 模拟数据生成器
class IoTDataSimulator {
  constructor() {
    this.devices = DEVICES;
    this.wsConnections = new Map();
    this.isRunning = false;
  }

  // 生成设备数据
  generateDeviceData(device) {
    const timestamp = new Date().toISOString();
    const baseValue = Math.random() * device.capacity;
    
    // 根据设备类型生成不同的数据
    switch (device.type) {
      case 'solar_panel':
        return {
          deviceId: device.id,
          type: device.type,
          location: device.location,
          timestamp,
          powerOutput: Math.max(0, baseValue * (0.8 + Math.random() * 0.4)), // 发电量
          temperature: 25 + Math.random() * 10, // 温度
          efficiency: 0.85 + Math.random() * 0.1, // 效率
          status: Math.random() > 0.05 ? 'active' : 'fault'
        };
      
      case 'heat_pump':
        return {
          deviceId: device.id,
          type: device.type,
          location: device.location,
          timestamp,
          heatOutput: Math.max(0, baseValue * (0.7 + Math.random() * 0.3)), // 热量输出
          powerConsumption: baseValue * 0.3 + Math.random() * 5, // 能耗
          cop: 3.0 + Math.random() * 0.5, // 性能系数
          status: Math.random() > 0.03 ? 'active' : 'maintenance'
        };
      
      case 'battery':
        return {
          deviceId: device.id,
          type: device.type,
          location: device.location,
          timestamp,
          chargeLevel: Math.random() * 100, // 充电水平
          voltage: 48 + Math.random() * 12, // 电压
          current: Math.random() * 50, // 电流
          temperature: 20 + Math.random() * 15,
          status: Math.random() > 0.02 ? 'charging' : 'discharging'
        };
      
      case 'wind_turbine':
        return {
          deviceId: device.id,
          type: device.type,
          location: device.location,
          timestamp,
          windSpeed: 3 + Math.random() * 15, // 风速
          powerOutput: Math.max(0, baseValue * (0.6 + Math.random() * 0.4)),
          rotorSpeed: 10 + Math.random() * 20, // 转子速度
          status: Math.random() > 0.04 ? 'active' : 'idle'
        };
      
      case 'thermal_storage':
        return {
          deviceId: device.id,
          type: device.type,
          location: device.location,
          timestamp,
          storedEnergy: Math.random() * device.capacity, // 存储能量
          temperature: 60 + Math.random() * 40, // 温度
          pressure: 1.0 + Math.random() * 0.5, // 压力
          status: Math.random() > 0.03 ? 'stable' : 'charging'
        };
      
      default:
        return {
          deviceId: device.id,
          type: device.type,
          location: device.location,
          timestamp,
          value: baseValue,
          status: 'active'
        };
    }
  }

  // 连接到WebSocket服务器
  async connectToWebSocket(device, data) {
    try {
      if (!this.wsConnections.has(device.id)) {
        const ws = new WebSocket('ws://localhost:8080/ws/iot');
        
        ws.on('open', () => {
          console.log(`WebSocket connected for device ${device.id}`);
          this.wsConnections.set(device.id, ws);
        });

        ws.on('error', (error) => {
          console.error(`WebSocket error for device ${device.id}:`, error.message);
          this.wsConnections.delete(device.id);
        });

        ws.on('close', () => {
          console.log(`WebSocket closed for device ${device.id}`);
          this.wsConnections.delete(device.id);
        });

        // 等待连接建立
        await new Promise(resolve => {
          if (ws.readyState === WebSocket.OPEN) {
            resolve();
          } else {
            ws.on('open', resolve);
          }
        });
      }

      const ws = this.wsConnections.get(device.id);
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({
          type: 'iot_data',
          deviceId: device.id,
          data: data
        }));
      }
    } catch (error) {
      console.error(`Failed to send data for device ${device.id}:`, error.message);
    }
  }

  // 发送数据到REST API
  async sendToRestAPI(device, data) {
    try {
      await axios.post('http://localhost:8080/api/iot/data', {
        deviceId: device.id,
        data: data,
        signature: this.generateSignature(data)
      });
    } catch (error) {
      console.error(`Failed to send REST data for device ${device.id}:`, error.message);
    }
  }

  // 生成数据签名
  generateSignature(data) {
    const secret = 'thermal-ark-secret-key';
    return crypto.createHmac('sha256', secret)
      .update(JSON.stringify(data))
      .digest('hex');
  }

  // 启动模拟器
  async start() {
    if (this.isRunning) {
      console.log('Simulator is already running');
      return;
    }

    this.isRunning = true;
    console.log('Starting IoT Data Simulator...');
    console.log(`Simulating ${this.devices.length} devices`);

    // 为每个设备启动数据生成循环
    this.devices.forEach(device => {
      const interval = setInterval(async () => {
        if (!this.isRunning) {
          clearInterval(interval);
          return;
        }

        try {
          // 生成设备数据
          const data = this.generateDeviceData(device);
          
          // 发送到WebSocket
          await this.connectToWebSocket(device, data);
          
          // 发送到REST API（每5次发送一次，避免过于频繁）
          if (Math.random() < 0.2) {
            await this.sendToRestAPI(device, data);
          }

          console.log(`Generated data for ${device.id}:`, {
            type: device.type,
            timestamp: data.timestamp,
            status: data.status
          });

        } catch (error) {
          console.error(`Error generating data for ${device.id}:`, error.message);
        }
      }, 2000 + Math.random() * 3000); // 2-5秒间隔
    });

    console.log('IoT Data Simulator started successfully');
  }

  // 停止模拟器
  stop() {
    this.isRunning = false;
    
    // 关闭所有WebSocket连接
    this.wsConnections.forEach((ws, deviceId) => {
      ws.close();
    });
    this.wsConnections.clear();
    
    console.log('IoT Data Simulator stopped');
  }
}

// 主程序
const simulator = new IoTDataSimulator();

// 处理程序退出
process.on('SIGINT', () => {
  console.log('\nReceived SIGINT. Stopping simulator...');
  simulator.stop();
  process.exit(0);
});

process.on('SIGTERM', () => {
  console.log('\nReceived SIGTERM. Stopping simulator...');
  simulator.stop();
  process.exit(0);
});

// 启动模拟器
simulator.start().catch(error => {
  console.error('Failed to start simulator:', error);
  process.exit(1);
});