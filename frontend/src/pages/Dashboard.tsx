import React, { useState, useEffect } from 'react'
import { Card, Row, Col, Statistic, Spin, Alert, Typography } from 'antd'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, AreaChart, Area } from 'recharts'
import { useAuth } from '../contexts/AuthContext'
import { useAppSelector, useAppDispatch } from '../store/hooks'
import { fetchEnergyData } from '../store/slices/energySlice'
import { webSocketService } from '../services/websocket'
import './Dashboard.css'

const { Title } = Typography

interface EnergyChartData {
  timestamp: string
  energyOutput: number
  energyConsumption: number
  efficiency: number
  temperature: number
}

interface MarketStats {
  totalTransactions: number
  totalEnergyTraded: number
  activeUsers: number
  averagePrice: number
}

const Dashboard: React.FC = () => {
  const { user } = useAuth()
  const dispatch = useAppDispatch()
  const { energyData, loading, error } = useAppSelector((state) => state.energy)
  
  const [marketStats, setMarketStats] = useState<MarketStats>({
    totalTransactions: 156,
    totalEnergyTraded: 12500,
    activeUsers: 42,
    averagePrice: 0.85
  })

  const [realTimeData, setRealTimeData] = useState<EnergyChartData[]>([])

  useEffect(() => {
    // 加载能源数据
    dispatch(fetchEnergyData('24h'))

    // 连接WebSocket获取实时数据
    webSocketService.connect()
    webSocketService.subscribe('energy')
    
    webSocketService.onEnergyUpdate((data) => {
      const newData: EnergyChartData = {
        timestamp: new Date().toLocaleTimeString(),
        energyOutput: data.energyOutput,
        energyConsumption: data.energyConsumption,
        efficiency: data.efficiency,
        temperature: data.temperature
      }
      
      setRealTimeData(prev => {
        const updated = [...prev, newData]
        return updated.slice(-20) // 只保留最近20个数据点
      })
    })

    return () => {
      webSocketService.offEnergyUpdate(() => {})
      webSocketService.disconnect()
    }
  }, [dispatch])

  // 生成模拟图表数据
  const generateChartData = (): EnergyChartData[] => {
    const data: EnergyChartData[] = []
    const now = new Date()
    
    for (let i = 12; i >= 0; i--) {
      const time = new Date(now.getTime() - i * 2 * 60 * 60 * 1000)
      data.push({
        timestamp: time.toLocaleTimeString(),
        energyOutput: Math.random() * 3000 + 2000,
        energyConsumption: Math.random() * 2500 + 1800,
        efficiency: Math.random() * 20 + 80,
        temperature: Math.random() * 20 + 75
      })
    }
    
    return data
  }

  const chartData = generateChartData()

  if (loading) {
    return (
      <div className="dashboard-container">
        <div className="loading-container">
          <Spin size="large" />
          <p>加载数据中...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="dashboard-container">
        <Alert message="数据加载失败" description={error} type="error" showIcon />
      </div>
    )
  }

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <Title level={2}>能源交易仪表盘</Title>
        <p>欢迎回来，{user?.username}！</p>
      </div>

      {/* 统计卡片 */}
      <Row gutter={[16, 16]} className="stats-row">
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="总交易量"
              value={marketStats.totalTransactions}
              suffix="笔"
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="总能量交易"
              value={marketStats.totalEnergyTraded}
              suffix="kWh"
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="活跃用户"
              value={marketStats.activeUsers}
              suffix="位"
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="平均价格"
              value={marketStats.averagePrice}
              prefix="¥"
              suffix="/kWh"
              valueStyle={{ color: '#f5222d' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 能源产出与消耗图表 */}
      <Row gutter={[16, 16]} className="charts-row">
        <Col xs={24} lg={12}>
          <Card title="能源产出与消耗趋势" className="chart-card">
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="timestamp" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="energyOutput" stroke="#1890ff" strokeWidth={2} name="能源产出 (kWh)" />
                <Line type="monotone" dataKey="energyConsumption" stroke="#52c41a" strokeWidth={2} name="能源消耗 (kWh)" />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        
        <Col xs={24} lg={12}>
          <Card title="系统效率与温度" className="chart-card">
            <ResponsiveContainer width="100%" height={300}>
              <AreaChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="timestamp" />
                <YAxis yAxisId="left" />
                <YAxis yAxisId="right" orientation="right" />
                <Tooltip />
                <Legend />
                <Area yAxisId="left" type="monotone" dataKey="efficiency" stroke="#faad14" fill="#fff7e6" name="效率 (%)" />
                <Line yAxisId="right" type="monotone" dataKey="temperature" stroke="#f5222d" strokeWidth={2} name="温度 (°C)" />
              </AreaChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      {/* 实时数据展示 */}
      <Row gutter={[16, 16]}>
        <Col xs={24}>
          <Card title="实时能源数据流" className="realtime-card">
            <div className="realtime-data">
              {realTimeData.length > 0 ? (
                <ResponsiveContainer width="100%" height={200}>
                  <LineChart data={realTimeData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="timestamp" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line type="monotone" dataKey="energyOutput" stroke="#1890ff" strokeWidth={2} name="实时产出" />
                    <Line type="monotone" dataKey="energyConsumption" stroke="#52c41a" strokeWidth={2} name="实时消耗" />
                  </LineChart>
                </ResponsiveContainer>
              ) : (
                <div className="no-data">
                  <p>等待实时数据连接...</p>
                </div>
              )}
            </div>
          </Card>
        </Col>
      </Row>

      {/* 关键指标 */}
      <Row gutter={[16, 16]} className="metrics-row">
        <Col xs={24} sm={8}>
          <Card className="metric-card">
            <div className="metric-content">
              <div className="metric-icon" style={{ backgroundColor: '#e6f7ff' }}>⚡</div>
              <div className="metric-info">
                <div className="metric-value">2,450 kWh</div>
                <div className="metric-label">当前产出</div>
              </div>
            </div>
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card className="metric-card">
            <div className="metric-content">
              <div className="metric-icon" style={{ backgroundColor: '#f6ffed' }}>🌡️</div>
              <div className="metric-info">
                <div className="metric-value">85.5°C</div>
                <div className="metric-label">系统温度</div>
              </div>
            </div>
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card className="metric-card">
            <div className="metric-content">
              <div className="metric-icon" style={{ backgroundColor: '#fff7e6' }}>📊</div>
              <div className="metric-info">
                <div className="metric-value">92.3%</div>
                <div className="metric-label">运行效率</div>
              </div>
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default Dashboard