import React, { useState, useEffect } from 'react'
import { Card, Row, Col, Statistic, Table, Tag, Progress, Avatar, List, Typography, Divider } from 'antd'
import { 
  TeamOutlined, 
  TrophyOutlined, 
  RiseOutlined, 
  EnvironmentOutlined,
  FireOutlined,
  StarOutlined,
  UserOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined
} from '@ant-design/icons'
import { useAppSelector, useAppDispatch } from '../store/hooks'
import { fetchCommunityStats } from '../store/slices/energySlice'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, BarChart, Bar } from 'recharts'
import './Community.css'

const { Title, Text } = Typography

interface UserRanking {
  rank: number
  username: string
  energySaved: number
  carbonReduction: number
  avatar: string
  change: 'up' | 'down' | 'stable'
}

interface CommunityData {
  totalEnergySaved: number
  totalCarbonReduction: number
  activeUsers: number
  averageEfficiency: number
  topContributors: UserRanking[]
  energyTrend: { date: string; energy: number }[]
  carbonDistribution: { name: string; value: number }[]
  regionalStats: { region: string; energySaved: number; efficiency: number }[]
}

const Community: React.FC = () => {
  const dispatch = useAppDispatch()
  const { communityStats, loading } = useAppSelector((state) => state.energy)
  
  const [timeRange, setTimeRange] = useState<'week' | 'month' | 'year'>('month')

  useEffect(() => {
    // 加载社区统计数据
    dispatch(fetchCommunityStats())
  }, [dispatch])

  // 模拟社区数据
  const mockCommunityData: CommunityData = {
    totalEnergySaved: 125000,
    totalCarbonReduction: 32500,
    activeUsers: 856,
    averageEfficiency: 78.5,
    topContributors: [
      {
        rank: 1,
        username: '热力先锋',
        energySaved: 12500,
        carbonReduction: 3250,
        avatar: '👑',
        change: 'up'
      },
      {
        rank: 2,
        username: '节能达人',
        energySaved: 9800,
        carbonReduction: 2548,
        avatar: '🌟',
        change: 'up'
      },
      {
        rank: 3,
        username: '环保卫士',
        energySaved: 7650,
        carbonReduction: 1989,
        avatar: '🌱',
        change: 'stable'
      },
      {
        rank: 4,
        username: '绿色能源',
        energySaved: 6200,
        carbonReduction: 1612,
        avatar: '⚡',
        change: 'down'
      },
      {
        rank: 5,
        username: '热力专家',
        energySaved: 5400,
        carbonReduction: 1404,
        avatar: '🔥',
        change: 'up'
      }
    ],
    energyTrend: [
      { date: '1月', energy: 12000 },
      { date: '2月', energy: 14500 },
      { date: '3月', energy: 13200 },
      { date: '4月', energy: 15800 },
      { date: '5月', energy: 16500 },
      { date: '6月', energy: 18200 }
    ],
    carbonDistribution: [
      { name: '工业节能', value: 45 },
      { name: '居民节能', value: 30 },
      { name: '商业节能', value: 15 },
      { name: '公共设施', value: 10 }
    ],
    regionalStats: [
      { region: '华北地区', energySaved: 45000, efficiency: 82 },
      { region: '华东地区', energySaved: 38000, efficiency: 76 },
      { region: '华南地区', energySaved: 22000, efficiency: 75 },
      { region: '西部地区', energySaved: 20000, efficiency: 81 }
    ]
  }

  const carbonColors = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042']
  const regionColors = ['#1890ff', '#52c41a', '#faad14', '#f5222d']

  const getRankIcon = (rank: number) => {
    switch (rank) {
      case 1: return '🥇'
      case 2: return '🥈'
      case 3: return '🥉'
      default: return `${rank}`
    }
  }

  const getChangeIcon = (change: string) => {
    switch (change) {
      case 'up': return <ArrowUpOutlined style={{ color: '#52c41a' }} />
      case 'down': return <ArrowDownOutlined style={{ color: '#ff4d4f' }} />
      default: return <span style={{ color: '#faad14' }}>—</span>
    }
  }

  const contributorColumns = [
    {
      title: '排名',
      dataIndex: 'rank',
      key: 'rank',
      render: (rank: number) => (
        <div className="rank-badge">
          <span className="rank-icon">{getRankIcon(rank)}</span>
        </div>
      )
    },
    {
      title: '用户',
      dataIndex: 'username',
      key: 'username',
      render: (username: string, record: UserRanking) => (
        <div className="user-info">
          <Avatar size="small" icon={<UserOutlined />} style={{ backgroundColor: '#1890ff' }} />
          <span style={{ marginLeft: 8 }}>{username}</span>
        </div>
      )
    },
    {
      title: '节能总量',
      dataIndex: 'energySaved',
      key: 'energySaved',
      render: (energy: number) => (
        <Text strong>{energy.toLocaleString()} kWh</Text>
      )
    },
    {
      title: '碳减排',
      dataIndex: 'carbonReduction',
      key: 'carbonReduction',
      render: (carbon: number) => (
        <Tag color="green">{carbon.toLocaleString()} kg</Tag>
      )
    },
    {
      title: '变化',
      dataIndex: 'change',
      key: 'change',
      render: (change: string) => getChangeIcon(change)
    }
  ]

  const regionalColumns = [
    {
      title: '地区',
      dataIndex: 'region',
      key: 'region',
      render: (region: string) => (
        <div className="region-info">
          <EnvironmentOutlined style={{ marginRight: 8, color: '#1890ff' }} />
          {region}
        </div>
      )
    },
    {
      title: '节能总量',
      dataIndex: 'energySaved',
      key: 'energySaved',
      render: (energy: number) => (
        <Text strong>{energy.toLocaleString()} kWh</Text>
      )
    },
    {
      title: '节能效率',
      dataIndex: 'efficiency',
      key: 'efficiency',
      render: (efficiency: number) => (
        <div>
          <Progress 
            percent={efficiency} 
            size="small" 
            strokeColor={{
              '0%': '#108ee9',
              '100%': '#87d068',
            }}
          />
          <Text type="secondary" style={{ fontSize: 12 }}>{efficiency}%</Text>
        </div>
      )
    }
  ]

  return (
    <div className="community-container">
      <div className="community-header">
        <Title level={1}>社区数据</Title>
        <Text type="secondary">查看社区节能数据和用户排名</Text>
      </div>

      {/* 总体统计 */}
      <Row gutter={[16, 16]} className="community-stats">
        <Col xs={24} sm={12} lg={6}>
          <Card className="stat-card">
            <Statistic
              title="累计节能"
              value={mockCommunityData.totalEnergySaved}
              suffix="kWh"
              prefix={<FireOutlined />}
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="stat-card">
            <Statistic
              title="碳减排总量"
              value={mockCommunityData.totalCarbonReduction}
              suffix="kg"
              prefix={<EnvironmentOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="stat-card">
            <Statistic
              title="活跃用户"
              value={mockCommunityData.activeUsers}
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="stat-card">
            <Statistic
              title="平均效率"
              value={mockCommunityData.averageEfficiency}
              suffix="%"
              prefix={<RiseOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        {/* 节能趋势图 */}
        <Col xs={24} lg={12}>
          <Card title="节能趋势" className="chart-card">
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={mockCommunityData.energyTrend}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip 
                  formatter={(value) => [`${value} kWh`, '节能总量']}
                  labelFormatter={(label) => `月份: ${label}`}
                />
                <Line 
                  type="monotone" 
                  dataKey="energy" 
                  stroke="#1890ff" 
                  strokeWidth={3}
                  dot={{ fill: '#1890ff', strokeWidth: 2, r: 4 }}
                  activeDot={{ r: 6, stroke: '#1890ff', strokeWidth: 2 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>

        {/* 碳减排分布 */}
        <Col xs={24} lg={12}>
          <Card title="碳减排分布" className="chart-card">
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={mockCommunityData.carbonDistribution}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {mockCommunityData.carbonDistribution.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={carbonColors[index % carbonColors.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(value) => [`${value}%`, '占比']} />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        {/* 用户排名 */}
        <Col xs={24} lg={12}>
          <Card 
            title={
              <div className="card-title">
                <TrophyOutlined style={{ marginRight: 8, color: '#faad14' }} />
                节能排行榜
              </div>
            }
            className="ranking-card"
          >
            <Table
              dataSource={mockCommunityData.topContributors}
              columns={contributorColumns}
              pagination={false}
              size="small"
              rowKey="rank"
            />
          </Card>
        </Col>

        {/* 地区统计 */}
        <Col xs={24} lg={12}>
          <Card 
            title={
              <div className="card-title">
                <EnvironmentOutlined style={{ marginRight: 8, color: '#52c41a' }} />
                地区节能统计
              </div>
            }
            className="regional-card"
          >
            <Table
              dataSource={mockCommunityData.regionalStats}
              columns={regionalColumns}
              pagination={false}
              size="small"
              rowKey="region"
            />
          </Card>
        </Col>
      </Row>

      {/* 地区节能柱状图 */}
      <Card title="地区节能对比" className="chart-card">
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={mockCommunityData.regionalStats}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="region" />
            <YAxis />
            <Tooltip 
              formatter={(value) => [`${value} kWh`, '节能总量']}
              labelFormatter={(label) => `地区: ${label}`}
            />
            <Bar dataKey="energySaved" fill="#1890ff" radius={[4, 4, 0, 0]}>
              {mockCommunityData.regionalStats.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={regionColors[index % regionColors.length]} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </Card>
    </div>
  )
}

export default Community