import React, { useState, useEffect } from 'react'
import { Card, Row, Col, Button, InputNumber, Select, Table, Tabs, Modal, Form, message } from 'antd'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, Area } from 'recharts'
import { useAppSelector, useAppDispatch } from '../store/hooks'
import { fetchOrders, createOrder } from '../store/slices/marketSlice'
import { webSocketService } from '../services/websocket'
import './Market.css'

const { Option } = Select
const { TabPane } = Tabs

interface OrderBookItem {
  price: number
  amount: number
  total: number
  type: 'buy' | 'sell'
}

interface PriceData {
  timestamp: string
  open: number
  high: number
  low: number
  close: number
  volume: number
}

const Market: React.FC = () => {
  const dispatch = useAppDispatch()
  const { orders, loading, error } = useAppSelector((state) => state.market)
  
  const [orderBook, setOrderBook] = useState<OrderBookItem[]>([])
  const [priceData, setPriceData] = useState<PriceData[]>([])
  const [selectedTab, setSelectedTab] = useState('orderbook')
  const [isOrderModalVisible, setIsOrderModalVisible] = useState(false)
  const [orderForm] = Form.useForm()

  useEffect(() => {
    // 加载市场数据
    dispatch(fetchOrders())

    // 连接WebSocket获取实时数据
    webSocketService.connect()
    webSocketService.subscribe('market')
    
    webSocketService.onMarketUpdate((data) => {
      // 更新订单簿
      updateOrderBook(data)
      // 更新价格数据
      updatePriceData(data)
    })

    // 初始化模拟数据
    initializeMockData()

    return () => {
      webSocketService.offMarketUpdate(() => {})
      webSocketService.disconnect()
    }
  }, [dispatch])

  const initializeMockData = () => {
    // 模拟订单簿数据
    const mockOrderBook: OrderBookItem[] = [
      { price: 0.82, amount: 500, total: 410, type: 'sell' },
      { price: 0.81, amount: 300, total: 243, type: 'sell' },
      { price: 0.80, amount: 200, total: 160, type: 'sell' },
      { price: 0.79, amount: 400, total: 316, type: 'buy' },
      { price: 0.78, amount: 600, total: 468, type: 'buy' },
      { price: 0.77, amount: 300, total: 231, type: 'buy' },
    ]
    setOrderBook(mockOrderBook)

    // 模拟价格数据
    const mockPriceData: PriceData[] = []
    const now = new Date()
    for (let i = 24; i >= 0; i--) {
      const time = new Date(now.getTime() - i * 60 * 60 * 1000)
      const basePrice = 0.8 + Math.random() * 0.1
      mockPriceData.push({
        timestamp: time.toISOString(),
        open: basePrice,
        high: basePrice + Math.random() * 0.05,
        low: basePrice - Math.random() * 0.05,
        close: basePrice + (Math.random() - 0.5) * 0.03,
        volume: Math.random() * 1000 + 500
      })
    }
    setPriceData(mockPriceData)
  }

  const updateOrderBook = (data: any) => {
    // 实际应用中会根据WebSocket数据更新订单簿
    setOrderBook(prev => {
      const newOrderBook = [...prev]
      // 简单的模拟更新逻辑
      if (Math.random() > 0.5) {
        newOrderBook.push({
          price: 0.8 + Math.random() * 0.1,
          amount: Math.random() * 500 + 100,
          total: 0,
          type: Math.random() > 0.5 ? 'buy' : 'sell'
        })
      }
      return newOrderBook.slice(-20) // 保持订单簿大小
    })
  }

  const updatePriceData = (data: any) => {
    // 实际应用中会根据WebSocket数据更新价格
  }

  const handleCreateOrder = async (values: any) => {
    try {
      await dispatch(createOrder(values)).unwrap()
      message.success('订单创建成功')
      setIsOrderModalVisible(false)
      orderForm.resetFields()
    } catch (error) {
      message.error('订单创建失败')
    }
  }

  const buyOrders = orderBook.filter(order => order.type === 'buy').sort((a, b) => b.price - a.price)
  const sellOrders = orderBook.filter(order => order.type === 'sell').sort((a, b) => a.price - b.price)

  const orderBookColumns = [
    {
      title: '价格 (¥)',
      dataIndex: 'price',
      key: 'price',
      render: (price: number) => <span className="price-cell">{price.toFixed(2)}</span>
    },
    {
      title: '数量 (kWh)',
      dataIndex: 'amount',
      key: 'amount',
      render: (amount: number) => amount.toLocaleString()
    },
    {
      title: '总额 (¥)',
      dataIndex: 'total',
      key: 'total',
      render: (total: number, record: OrderBookItem) => (record.price * record.amount).toFixed(2)
    }
  ]

  return (
    <div className="market-container">
      <div className="market-header">
        <h1>能源交易市场</h1>
        <p>实时P2P热力能量交易平台</p>
      </div>

      <Row gutter={[16, 16]} className="market-overview">
        <Col xs={24} lg={8}>
          <Card title="当前价格" className="price-card">
            <div className="current-price">
              <span className="price-value">¥0.82</span>
              <span className="price-change positive">+2.5%</span>
            </div>
            <div className="price-stats">
              <div className="stat">
                <span className="label">24h最高:</span>
                <span className="value">¥0.85</span>
              </div>
              <div className="stat">
                <span className="label">24h最低:</span>
                <span className="value">¥0.78</span>
              </div>
              <div className="stat">
                <span className="label">24h交易量:</span>
                <span className="value">12,500 kWh</span>
              </div>
            </div>
          </Card>
        </Col>

        <Col xs={24} lg={16}>
          <Card title="价格趋势" className="chart-card">
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={priceData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis 
                  dataKey="timestamp" 
                  tickFormatter={(value) => new Date(value).toLocaleTimeString()}
                />
                <YAxis domain={['dataMin - 0.05', 'dataMax + 0.05']} />
                <Tooltip 
                  formatter={(value) => [`¥${value}`, '价格']}
                  labelFormatter={(label) => `时间: ${new Date(label).toLocaleString()}`}
                />
                <Legend />
                <Line 
                  type="monotone" 
                  dataKey="close" 
                  stroke="#1890ff" 
                  strokeWidth={2}
                  name="收盘价"
                  dot={false}
                />
                <Area 
                  type="monotone" 
                  dataKey="volume" 
                  fill="#e6f7ff" 
                  stroke="#91d5ff"
                  name="交易量"
                  yAxisId="right"
                />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      <Tabs 
        activeKey={selectedTab} 
        onChange={setSelectedTab}
        className="market-tabs"
      >
        <TabPane tab="订单簿" key="orderbook">
          <Row gutter={[16, 16]}>
            <Col xs={24} lg={12}>
              <Card title="买单" className="order-book-card">
                <Table
                  dataSource={buyOrders}
                  columns={orderBookColumns}
                  pagination={false}
                  size="small"
                  rowClassName={(record) => `order-row ${record.type}`}
                />
              </Card>
            </Col>
            <Col xs={24} lg={12}>
              <Card title="卖单" className="order-book-card">
                <Table
                  dataSource={sellOrders}
                  columns={orderBookColumns}
                  pagination={false}
                  size="small"
                  rowClassName={(record) => `order-row ${record.type}`}
                />
              </Card>
            </Col>
          </Row>
        </TabPane>

        <TabPane tab="快速交易" key="quicktrade">
          <Card title="一键交易" className="quick-trade-card">
            <Row gutter={[16, 16]}>
              <Col xs={24} md={12}>
                <div className="trade-action buy">
                  <h3>立即购买</h3>
                  <div className="best-price">最佳价格: ¥0.82/kWh</div>
                  <Form layout="vertical">
                    <Form.Item label="购买数量 (kWh)">
                      <InputNumber 
                        min={1} 
                        max={10000} 
                        defaultValue={100}
                        style={{ width: '100%' }}
                      />
                    </Form.Item>
                    <Button type="primary" size="large" block>
                      立即购买
                    </Button>
                  </Form>
                </div>
              </Col>
              <Col xs={24} md={12}>
                <div className="trade-action sell">
                  <h3>立即出售</h3>
                  <div className="best-price">最佳价格: ¥0.80/kWh</div>
                  <Form layout="vertical">
                    <Form.Item label="出售数量 (kWh)">
                      <InputNumber 
                        min={1} 
                        max={10000} 
                        defaultValue={100}
                        style={{ width: '100%' }}
                      />
                    </Form.Item>
                    <Button type="primary" size="large" block>
                      立即出售
                    </Button>
                  </Form>
                </div>
              </Col>
            </Row>
          </Card>
        </TabPane>
      </Tabs>

      <div className="market-actions">
        <Button 
          type="primary" 
          size="large" 
          icon={<span>➕</span>}
          onClick={() => setIsOrderModalVisible(true)}
        >
          发布高级订单
        </Button>
      </div>

      <Modal
        title="发布交易订单"
        visible={isOrderModalVisible}
        onCancel={() => setIsOrderModalVisible(false)}
        footer={null}
        width={600}
      >
        <Form
          form={orderForm}
          layout="vertical"
          onFinish={handleCreateOrder}
        >
          <Form.Item
            name="type"
            label="订单类型"
            rules={[{ required: true, message: '请选择订单类型' }]}
          >
            <Select placeholder="选择订单类型">
              <Option value="BUY">购买</Option>
              <Option value="SELL">出售</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="energyAmount"
            label="能量数量 (kWh)"
            rules={[{ required: true, message: '请输入能量数量' }]}
          >
            <InputNumber 
              min={1} 
              max={10000} 
              placeholder="输入能量数量"
              style={{ width: '100%' }}
            />
          </Form.Item>

          <Form.Item
            name="price"
            label="价格 (¥/kWh)"
            rules={[{ required: true, message: '请输入价格' }]}
          >
            <InputNumber 
              min={0.01} 
              max={10} 
              step={0.01}
              placeholder="输入价格"
              style={{ width: '100%' }}
            />
          </Form.Item>

          <Form.Item
            name="orderType"
            label="订单类型"
          >
            <Select placeholder="选择订单类型">
              <Option value="LIMIT">限价单</Option>
              <Option value="MARKET">市价单</Option>
            </Select>
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block size="large">
              发布订单
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default Market