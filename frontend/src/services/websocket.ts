export interface MarketUpdate {
  type: 'ORDER_CREATED' | 'ORDER_CANCELLED' | 'ORDER_FILLED'
  order: any
  price: number
  volume: number
}

export interface EnergyUpdate {
  deviceId: string
  temperature: number
  energyOutput: number
  energyConsumption: number
  efficiency: number
  timestamp: string
}

export interface WebSocketConfig {
  url: string
  reconnectInterval?: number
  maxReconnectAttempts?: number
}

export class WebSocketService {
  private ws: WebSocket | null = null
  private reconnectAttempts = 0
  private config: WebSocketConfig
  private marketCallbacks: ((data: MarketUpdate) => void)[] = []
  private energyCallbacks: ((data: EnergyUpdate) => void)[] = []
  private isConnected = false

  constructor(config: WebSocketConfig) {
    this.config = {
      reconnectInterval: 3000,
      maxReconnectAttempts: 5,
      ...config,
    }
  }

  connect(): void {
    try {
      this.ws = new WebSocket(this.config.url)
      
      this.ws.onopen = () => {
        console.log('WebSocket connected')
        this.isConnected = true
        this.reconnectAttempts = 0
      }

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          this.handleMessage(data)
        } catch (error) {
          console.error('WebSocket message parsing error:', error)
        }
      }

      this.ws.onclose = () => {
        console.log('WebSocket disconnected')
        this.isConnected = false
        this.handleReconnect()
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error)
      }
    } catch (error) {
      console.error('WebSocket connection error:', error)
    }
  }

  private handleMessage(data: any): void {
    if (data.type === 'MARKET_UPDATE') {
      this.marketCallbacks.forEach(callback => callback(data.payload))
    } else if (data.type === 'ENERGY_UPDATE') {
      this.energyCallbacks.forEach(callback => callback(data.payload))
    }
  }

  private handleReconnect(): void {
    if (this.reconnectAttempts < this.config.maxReconnectAttempts!) {
      this.reconnectAttempts++
      console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.config.maxReconnectAttempts})`)
      
      setTimeout(() => {
        this.connect()
      }, this.config.reconnectInterval)
    } else {
      console.error('Max reconnection attempts reached')
    }
  }

  subscribe(channel: string): void {
    if (this.ws && this.isConnected) {
      this.ws.send(JSON.stringify({
        type: 'SUBSCRIBE',
        channel,
      }))
    }
  }

  unsubscribe(channel: string): void {
    if (this.ws && this.isConnected) {
      this.ws.send(JSON.stringify({
        type: 'UNSUBSCRIBE',
        channel,
      }))
    }
  }

  onMarketUpdate(callback: (data: MarketUpdate) => void): void {
    this.marketCallbacks.push(callback)
  }

  offMarketUpdate(callback: (data: MarketUpdate) => void): void {
    this.marketCallbacks = this.marketCallbacks.filter(cb => cb !== callback)
  }

  onEnergyUpdate(callback: (data: EnergyUpdate) => void): void {
    this.energyCallbacks.push(callback)
  }

  offEnergyUpdate(callback: (data: EnergyUpdate) => void): void {
    this.energyCallbacks = this.energyCallbacks.filter(cb => cb !== callback)
  }

  disconnect(): void {
    if (this.ws) {
      this.ws.close()
      this.ws = null
      this.isConnected = false
    }
  }

  getConnectionStatus(): boolean {
    return this.isConnected
  }
}

// 创建WebSocket服务实例
export const webSocketService = new WebSocketService({
  url: 'ws://localhost:8080/ws',
  reconnectInterval: 3000,
  maxReconnectAttempts: 5,
})