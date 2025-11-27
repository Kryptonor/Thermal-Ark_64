import React, { useState, useEffect } from 'react'
import { Card, Row, Col, Button, Table, Tabs, Modal, Form, InputNumber, Select, message, Statistic, Tag } from 'antd'
import { 
  WalletOutlined, 
  PlusOutlined, 
  MinusOutlined, 
  SwapOutlined, 
  HistoryOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons'
import { useAppSelector, useAppDispatch } from '../store/hooks'
import { fetchWallet, deposit, withdraw } from '../store/slices/walletSlice'
import './Wallet.css'

const { Option } = Select
const { TabPane } = Tabs

interface Transaction {
  id: number
  type: 'deposit' | 'withdraw' | 'trade' | 'transfer'
  amount: number
  currency: 'energy' | 'CNY'
  counterparty: string
  timestamp: string
  status: 'completed' | 'pending' | 'failed'
  description: string
}

const Wallet: React.FC = () => {
  const dispatch = useAppDispatch()
  const { wallet, loading, error } = useAppSelector((state) => state.wallet)
  
  const [activeTab, setActiveTab] = useState('overview')
  const [isDepositModalVisible, setIsDepositModalVisible] = useState(false)
  const [isWithdrawModalVisible, setIsWithdrawModalVisible] = useState(false)
  const [depositForm] = Form.useForm()
  const [withdrawForm] = Form.useForm()

  useEffect(() => {
    // 加载钱包数据
    dispatch(fetchWallet())
  }, [dispatch])

  const handleDeposit = async (values: any) => {
    try {
      await dispatch(deposit(values.amount)).unwrap()
      message.success(`充值成功: ¥${values.amount}`)
      setIsDepositModalVisible(false)
      depositForm.resetFields()
    } catch (error) {
      message.error('充值失败')
    }
  }

  const handleWithdraw = async (values: any) => {
    try {
      await dispatch(withdraw(values.amount)).unwrap()
      message.success(`提现成功: ¥${values.amount}`)
      setIsWithdrawModalVisible(false)
      withdrawForm.resetFields()
    } catch (error) {
      message.error('提现失败')
    }
  }

  const getTransactionIcon = (type: string) => {
    switch (type) {
      case 'deposit': return <ArrowDownOutlined style={{ color: '#52c41a' }} />
      case 'withdraw': return <ArrowUpOutlined style={{ color: '#ff4d4f' }} />
      case 'trade': return <SwapOutlined style={{ color: '#1890ff' }} />
      case 'transfer': return <SwapOutlined style={{ color: '#faad14' }} />
      default: return <HistoryOutlined />
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'completed': return <CheckCircleOutlined style={{ color: '#52c41a' }} />
      case 'pending': return <ClockCircleOutlined style={{ color: '#faad14' }} />
      case 'failed': return <CloseCircleOutlined style={{ color: '#ff4d4f' }} />
      default: return <ClockCircleOutlined />
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case 'completed': return '已完成'
      case 'pending': return '处理中'
      case 'failed': return '失败'
      default: return '未知'
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'completed': return 'success'
      case 'pending': return 'processing'
      case 'failed': return 'error'
      default: return 'default'
    }
  }

  // 模拟交易数据
  const mockTransactions: Transaction[] = [
    {
      id: 1,
      type: 'trade',
      amount: 300,
      currency: 'energy',
      counterparty: '热力公司A',
      timestamp: new Date().toISOString(),
      status: 'completed',
      description: '购买热力能量'
    },
    {
      id: 2,
      type: 'deposit',
      amount: 1000,
      currency: 'CNY',
      counterparty: '银行转账',
      timestamp: new Date(Date.now() - 86400000).toISOString(),
      status: 'completed',
      description: '账户充值'
    },
    {
      id: 3,
      type: 'trade',
      amount: 150,
      currency: 'energy',
      counterparty: '用户B',
      timestamp: new Date(Date.now() - 172800000).toISOString(),
      status: 'completed',
      description: '出售热力能量'
    },
    {
      id: 4,
      type: 'withdraw',
      amount: 500,
      currency: 'CNY',
      counterparty: '银行账户',
      timestamp: new Date(Date.now() - 259200000).toISOString(),
      status: 'pending',
      description: '提现申请'
    }
  ]

  const transactionColumns = [
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: string, record: Transaction) => (
        <div className="transaction-type">
          {getTransactionIcon(type)}
          <span style={{ marginLeft: 8 }}>
            {type === 'deposit' ? '充值' : 
             type === 'withdraw' ? '提现' :
             type === 'trade' ? '交易' : '转账'}
          </span>
        </div>
      )
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description'
    },
    {
      title: '对方',
      dataIndex: 'counterparty',
      key: 'counterparty'
    },
    {
      title: '金额',
      dataIndex: 'amount',
      key: 'amount',
      render: (amount: number, record: Transaction) => (
        <span className={`amount ${record.type === 'deposit' || record.type === 'trade' ? 'positive' : 'negative'}`}>
          {record.type === 'deposit' || record.type === 'trade' ? '+' : '-'}
          {record.currency === 'energy' ? `${amount} kWh` : `¥${amount}`}
        </span>
      )
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag icon={getStatusIcon(status)} color={getStatusColor(status)}>
          {getStatusText(status)}
        </Tag>
      )
    },
    {
      title: '时间',
      dataIndex: 'timestamp',
      key: 'timestamp',
      render: (timestamp: string) => new Date(timestamp).toLocaleString()
    }
  ]

  return (
    <div className="wallet-container">
      <div className="wallet-header">
        <h1>我的钱包</h1>
        <p>管理您的热力能量和资金</p>
      </div>

      <Row gutter={[16, 16]} className="wallet-overview">
        <Col xs={24} lg={12}>
          <Card title="热力能量" className="balance-card energy">
            <Statistic
              title="可用余额"
              value={wallet?.energy || 1250.5}
              suffix="kWh"
              valueStyle={{ color: '#52c41a' }}
              prefix={<WalletOutlined />}
            />
            <div className="frozen-amount">
              冻结: {wallet?.frozenEnergy || 200} kWh
            </div>
          </Card>
        </Col>
        
        <Col xs={24} lg={12}>
          <Card title="资金余额" className="balance-card currency">
            <Statistic
              title="可用余额"
              value={wallet?.currency || 8560.75}
              precision={2}
              prefix="¥"
              valueStyle={{ color: '#1890ff' }}
            />
            <div className="frozen-amount">
              冻结: ¥{wallet?.frozenCurrency || 500}
            </div>
          </Card>
        </Col>
      </Row>

      <div className="wallet-actions">
        <Button 
          type="primary" 
          size="large" 
          icon={<PlusOutlined />}
          onClick={() => setIsDepositModalVisible(true)}
        >
          充值
        </Button>
        <Button 
          type="default" 
          size="large" 
          icon={<MinusOutlined />}
          onClick={() => setIsWithdrawModalVisible(true)}
        >
          提现
        </Button>
        <Button 
          type="dashed" 
          size="large" 
          icon={<SwapOutlined />}
        >
          转账
        </Button>
        <Button 
          type="dashed" 
          size="large" 
          icon={<HistoryOutlined />}
        >
          交易记录
        </Button>
      </div>

      <Tabs 
        activeKey={activeTab} 
        onChange={setActiveTab}
        className="wallet-tabs"
      >
        <TabPane tab="最近交易" key="overview">
          <Card title="最近交易记录" className="recent-transactions">
            <Table
              dataSource={mockTransactions.slice(0, 5)}
              columns={transactionColumns}
              pagination={false}
              size="small"
              rowKey="id"
            />
          </Card>
        </TabPane>

        <TabPane tab="全部交易" key="transactions">
          <Card title="交易历史" className="all-transactions">
            <Table
              dataSource={mockTransactions}
              columns={transactionColumns}
              pagination={{ pageSize: 10 }}
              size="middle"
              rowKey="id"
            />
          </Card>
        </TabPane>
      </Tabs>

      {/* 充值模态框 */}
      <Modal
        title="账户充值"
        visible={isDepositModalVisible}
        onCancel={() => setIsDepositModalVisible(false)}
        footer={null}
        width={400}
      >
        <Form
          form={depositForm}
          layout="vertical"
          onFinish={handleDeposit}
        >
          <Form.Item
            name="amount"
            label="充值金额 (¥)"
            rules={[{ required: true, message: '请输入充值金额' }]}
          >
            <InputNumber 
              min={1} 
              max={100000} 
              placeholder="输入充值金额"
              style={{ width: '100%' }}
              formatter={value => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              parser={value => {
                const num = parseInt(value!.replace(/¥\s?|(,*)/g, '')) || 1;
                return num as 1 | 100000;
              }}
            />
          </Form.Item>

          <Form.Item
            name="paymentMethod"
            label="支付方式"
            rules={[{ required: true, message: '请选择支付方式' }]}
          >
            <Select placeholder="选择支付方式">
              <Option value="alipay">支付宝</Option>
              <Option value="wechat">微信支付</Option>
              <Option value="bank">银行卡</Option>
            </Select>
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block size="large">
              确认充值
            </Button>
          </Form.Item>
        </Form>
      </Modal>

      {/* 提现模态框 */}
      <Modal
        title="资金提现"
        visible={isWithdrawModalVisible}
        onCancel={() => setIsWithdrawModalVisible(false)}
        footer={null}
        width={400}
      >
        <Form
          form={withdrawForm}
          layout="vertical"
          onFinish={handleWithdraw}
        >
          <Form.Item
            name="amount"
            label="提现金额 (¥)"
            rules={[{ 
              required: true, 
              message: '请输入提现金额' 
            }]}
          >
            <InputNumber 
              min={1} 
              max={wallet?.balance || 0} 
              placeholder="输入提现金额"
              style={{ width: '100%' }}
              formatter={value => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              parser={value => {
                const num = parseInt(value!.replace(/¥\s?|(,*)/g, '')) || 1;
                return num as 1 | 100000;
              }}
            />
          </Form.Item>

          <Form.Item
            name="bankAccount"
            label="收款账户"
            rules={[{ required: true, message: '请输入收款账户' }]}
          >
            <Select placeholder="选择收款账户">
              <Option value="alipay">支付宝账户</Option>
              <Option value="wechat">微信账户</Option>
              <Option value="bank">银行卡账户</Option>
            </Select>
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block size="large">
              确认提现
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default Wallet