// IoT模拟器配置
const CONFIG = {
  // WebSocket服务器配置
  websocket: {
    url: 'ws://localhost:8080/ws/iot',
    reconnectInterval: 5000, // 重连间隔(ms)
    maxReconnectAttempts: 10 // 最大重连次数
  },
  
  // REST API配置
  api: {
    baseUrl: 'http://localhost:8080/api',
    endpoints: {
      iotData: '/iot/data',
      health: '/health'
    },
    timeout: 10000 // 请求超时时间(ms)
  },
  
  // 模拟参数
  simulation: {
    dataInterval: {
      min: 2000, // 最小数据间隔(ms)
      max: 5000  // 最大数据间隔(ms)
    },
    restApiFrequency: 0.2, // REST API发送频率(0-1)
    enableRealisticPatterns: true, // 启用真实模式
    
    // 设备故障率配置
    failureRates: {
      solar_panel: 0.05,    // 5%故障率
      heat_pump: 0.03,      // 3%故障率
      battery: 0.02,        // 2%故障率
      wind_turbine: 0.04,   // 4%故障率
      thermal_storage: 0.03 // 3%故障率
    }
  },
  
  // 设备配置
  devices: [
    {
      id: 'solar-001',
      type: 'solar_panel',
      name: '太阳能板 A栋',
      location: 'Building A',
      capacity: 50, // kW
      coordinates: { lat: 39.9042, lng: 116.4074 },
      description: '屋顶太阳能发电系统'
    },
    {
      id: 'heatpump-001',
      type: 'heat_pump',
      name: '热泵系统 B栋',
      location: 'Building B',
      capacity: 30, // kW
      coordinates: { lat: 39.9043, lng: 116.4075 },
      description: '地源热泵供暖系统'
    },
    {
      id: 'battery-001',
      type: 'battery',
      name: '储能电池 C栋',
      location: 'Building C',
      capacity: 100, // kWh
      coordinates: { lat: 39.9044, lng: 116.4076 },
      description: '锂离子储能电池系统'
    },
    {
      id: 'wind-001',
      type: 'wind_turbine',
      name: '风力发电机 D栋',
      location: 'Building D',
      capacity: 20, // kW
      coordinates: { lat: 39.9045, lng: 116.4077 },
      description: '小型风力发电机组'
    },
    {
      id: 'thermal-001',
      type: 'thermal_storage',
      name: '热能存储 E栋',
      location: 'Building E',
      capacity: 200, // kWh
      coordinates: { lat: 39.9046, lng: 116.4078 },
      description: '相变材料热能存储系统'
    },
    {
      id: 'solar-002',
      type: 'solar_panel',
      name: '太阳能板 F栋',
      location: 'Building F',
      capacity: 40, // kW
      coordinates: { lat: 39.9047, lng: 116.4079 },
      description: '停车场太阳能顶棚'
    },
    {
      id: 'heatpump-002',
      type: 'heat_pump',
      name: '热泵系统 G栋',
      location: 'Building G',
      capacity: 25, // kW
      coordinates: { lat: 39.9048, lng: 116.4080 },
      description: '空气源热泵系统'
    }
  ],
  
  // 数据验证配置
  validation: {
    // 数值范围限制
    ranges: {
      solar_panel: {
        powerOutput: { min: 0, max: 100 },
        temperature: { min: -10, max: 80 },
        efficiency: { min: 0.7, max: 0.95 }
      },
      heat_pump: {
        heatOutput: { min: 0, max: 50 },
        powerConsumption: { min: 0, max: 20 },
        cop: { min: 2.5, max: 4.0 }
      },
      battery: {
        chargeLevel: { min: 0, max: 100 },
        voltage: { min: 40, max: 60 },
        current: { min: 0, max: 100 },
        temperature: { min: -10, max: 50 }
      },
      wind_turbine: {
        windSpeed: { min: 0, max: 25 },
        powerOutput: { min: 0, max: 30 },
        rotorSpeed: { min: 0, max: 40 }
      },
      thermal_storage: {
        storedEnergy: { min: 0, max: 250 },
        temperature: { min: 20, max: 120 },
        pressure: { min: 0.5, max: 2.0 }
      }
    },
    
    // 必填字段
    requiredFields: [
      'deviceId', 'type', 'location', 'timestamp', 'status'
    ]
  },
  
  // 日志配置
  logging: {
    level: 'info', // error, warn, info, debug
    format: 'json', // json, simple
    file: {
      enabled: false,
      path: './logs/iot-simulator.log',
      maxSize: '10m',
      maxFiles: 5
    }
  },
  
  // 安全配置
  security: {
    signature: {
      enabled: true,
      secret: 'thermal-ark-secret-key',
      algorithm: 'sha256'
    },
    encryption: {
      enabled: false,
      key: ''
    }
  }
};

// 环境变量覆盖配置
function loadConfigFromEnv() {
  const envConfig = {
    websocket: {
      url: process.env.WS_URL || CONFIG.websocket.url
    },
    api: {
      baseUrl: process.env.API_BASE_URL || CONFIG.api.baseUrl
    },
    simulation: {
      dataInterval: {
        min: parseInt(process.env.DATA_INTERVAL_MIN) || CONFIG.simulation.dataInterval.min,
        max: parseInt(process.env.DATA_INTERVAL_MAX) || CONFIG.simulation.dataInterval.max
      }
    }
  };
  
  return { ...CONFIG, ...envConfig };
}

export default loadConfigFromEnv();
export { CONFIG as defaultConfig };