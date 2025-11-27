import axios from 'axios'
import WebSocket from 'ws'
import cron from 'cron'
import dotenv from 'dotenv'

// 加载环境变量
dotenv.config()

// 配置
const CONFIG = {
  BACKEND_URL: process.env.BACKEND_URL || 'http://localhost:8080',
  WS_URL: process.env.WS_URL || 'ws://localhost:8080/ws',
  SIMULATOR_ID: process.env.SIMULATOR_ID || 'iot-simulator-001',
  INTERVAL: parseInt(process.env.INTERVAL) || 5000, // 5秒间隔
  NUM_SENSORS: parseInt(process.env.NUM_SENSORS) || 10
}

// 模拟传感器数据
class SensorSimulator {
  constructor(id, location, type) {
    this.id = id
    this.location = location
    this.type = type
    this.baseTemperature = this.getBaseTemperature(type)
    this.baseFlowRate = this.getBaseFlowRate(type)
    this.status = 'normal'
  }

  getBaseTemperature(type) {
    switch (type) {
      case 'industrial': return 85 + Math.random() * 20 // 85-105°C
      case 'commercial': return 75 + Math.random() * 15 // 75-90°C
      case 'residential': return 65 + Math.random() * 10 // 65-75°C
      default: return 70 + Math.random() * 15
    }
  }

  getBaseFlowRate(type) {
    switch (type) {
      case 'industrial': return 200 + Math.random() * 100 // 200-300 m³/h
      case 'commercial': return 100 + Math.random() * 50 // 100-150 m³/h
      case 'residential': return 50 + Math.random() * 30 // 50-80 m³/h
      default: return 100 + Math.random() * 50
    }
  }

  generateData() {
    // 模拟温度波动
    const temperatureVariation = (Math.random() - 0.5) * 10
    const temperature = Math.max(40, this.baseTemperature + temperatureVariation)

    // 模拟流量波动
    const flowRateVariation = (Math.random() - 0.5) * 20
    const flowRate = Math.max(10, this.baseFlowRate + flowRateVariation)

    // 计算能量输出 (简化公式)
    const energyOutput = Math.round(temperature * flowRate * 0.00116 * 100) / 100

    // 模拟设备状态
    const statusRandom = Math.random()
    if (statusRandom < 0.95) {
      this.status = 'normal'
    } else if (statusRandom < 0.98) {
      this.status = 'warning'
    } else {
      this.status = 'critical'
    }

    return {
      sensorId: this.id,
      location: this.location,
      type: this.type,
      temperature: Math.round(temperature * 10) / 10,
      flowRate: Math.round(flowRate * 10) / 10,
      energyOutput,
      status: this.status,
      timestamp: new Date().toISOString()
    }
  }
}

// IoT模拟器主类
class IoTSimulator {
  constructor() {
    this.sensors = []
    this.ws = null
    this.isConnected = false
    this.initializeSensors()
  }

  initializeSensors() {
    const locations = [
      '北京市朝阳区', '上海市浦东新区', '广州市天河区', '深圳市南山区',
      '杭州市西湖区', '南京市鼓楼区', '成都市武侯区', '武汉市江汉区',
      '西安市雁塔区', '重庆市渝中区'
    ]

    const types = ['industrial', 'commercial', 'residential']

    for (let i = 0; i < CONFIG.NUM_SENSORS; i++) {
      const location = locations[i % locations.length]
      const type = types[i % types.length]
      const sensor = new SensorSimulator(
        `sensor-${i + 1}`,
        location,
        type
      )
      this.sensors.push(sensor)
    }

    console.log(`初始化了 ${this.sensors.length} 个传感器`)
  }

  async connectWebSocket() {
    try {
      this.ws = new WebSocket(CONFIG.WS_URL)

      this.ws.on('open', () => {
        console.log('WebSocket连接已建立')
        this.isConnected = true
        
        // 发送模拟器注册信息
        this.ws.send(JSON.stringify({
          type: 'register',
          simulatorId: CONFIG.SIMULATOR_ID,
          timestamp: new Date().toISOString()
        }))
      })

      this.ws.on('message', (data) => {
        try {
          const message = JSON.parse(data)
          this.handleMessage(message)
        } catch (error) {
          console.error('WebSocket消息解析错误:', error)
        }
      })

      this.ws.on('close', () => {
        console.log('WebSocket连接已关闭')
        this.isConnected = false
        // 尝试重新连接
        setTimeout(() => this.connectWebSocket(), 5000)
      })

      this.ws.on('error', (error) => {
        console.error('WebSocket错误:', error)
        this.isConnected = false
      })

    } catch (error) {
      console.error('WebSocket连接失败:', error)
    }
  }

  handleMessage(message) {
    switch (message.type) {
      case 'config_update':
        console.log('收到配置更新:', message.config)
        break
      case 'sensor_control':
        console.log('收到传感器控制命令:', message.command)
        break
      default:
        console.log('收到未知消息类型:', message.type)
    }
  }

  async sendDataToBackend(data) {
    try {
      const response = await axios.post(
        `${CONFIG.BACKEND_URL}/api/thermal-data`,
        data,
        {
          headers: {
            'Content-Type': 'application/json',
            'X-Simulator-ID': CONFIG.SIMULATOR_ID
          },
          timeout: 5000
        }
      )

      if (response.status === 200) {
        console.log(`数据发送成功: ${data.sensorId}`)
      }
    } catch (error) {
      console.error(`数据发送失败 (${data.sensorId}):`, error.message)
    }
  }

  sendDataViaWebSocket(data) {
    if (this.isConnected && this.ws) {
      try {
        this.ws.send(JSON.stringify({
          type: 'sensor_data',
          data: data,
          timestamp: new Date().toISOString()
        }))
      } catch (error) {
        console.error('WebSocket发送失败:', error)
      }
    }
  }

  generateAndSendData() {
    const timestamp = new Date()
    console.log(`\n=== 生成数据 ${timestamp.toLocaleString()} ===`)

    this.sensors.forEach(sensor => {
      const data = sensor.generateData()
      
      // 输出到控制台
      console.log(`传感器 ${sensor.id}: ${data.temperature}°C, ${data.flowRate}m³/h, ${data.energyOutput}kWh [${data.status}]`)

      // 发送到后端API
      this.sendDataToBackend(data)

      // 通过WebSocket发送
      this.sendDataViaWebSocket(data)
    })
  }

  start() {
    console.log('启动IoT数据模拟器...')
    console.log('配置:', CONFIG)

    // 连接WebSocket
    this.connectWebSocket()

    // 启动定时任务
    setInterval(() => {
      this.generateAndSendData()
    }, CONFIG.INTERVAL)

    // 启动每日统计任务
    this.startDailyStats()

    console.log('IoT数据模拟器已启动')
  }

  startDailyStats() {
    // 每天凌晨生成统计报告
    const cronJob = new cron.CronJob('0 0 * * *', () => {
      this.generateDailyReport()
    })

    cronJob.start()
    console.log('每日统计任务已启动')
  }

  generateDailyReport() {
    const totalEnergy = this.sensors.reduce((sum, sensor) => {
      const data = sensor.generateData()
      return sum + data.energyOutput
    }, 0)

    const report = {
      type: 'daily_report',
      simulatorId: CONFIG.SIMULATOR_ID,
      date: new Date().toISOString().split('T')[0],
      totalSensors: this.sensors.length,
      estimatedDailyEnergy: Math.round(totalEnergy * 24), // 估算日产量
      timestamp: new Date().toISOString()
    }

    console.log('生成每日报告:', report)

    // 发送报告到后端
    this.sendDataToBackend(report)
  }
}

// 启动模拟器
const simulator = new IoTSimulator()
simulator.start()

// 导出类供测试使用
export { SensorSimulator, IoTSimulator }

// 优雅关闭
process.on('SIGINT', () => {
  console.log('\n收到关闭信号，正在停止模拟器...')
  if (simulator.ws) {
    simulator.ws.close()
  }
  process.exit(0)
})

process.on('uncaughtException', (error) => {
  console.error('未捕获的异常:', error)
  process.exit(1)
})

process.on('unhandledRejection', (reason, promise) => {
  console.error('未处理的Promise拒绝:', reason)
  process.exit(1)
})