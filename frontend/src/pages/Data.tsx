import React from 'react';
import { Card, Row, Col, Statistic, Table, DatePicker } from 'antd';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import './Data.css';

const { RangePicker } = DatePicker;

const Data: React.FC = () => {
  // 模拟数据
  const energyData = [
    { time: '00:00', temperature: 65, flow: 120, energy: 45 },
    { time: '06:00', temperature: 68, flow: 125, energy: 48 },
    { time: '12:00', temperature: 72, flow: 130, energy: 52 },
    { time: '18:00', temperature: 70, flow: 128, energy: 50 },
  ];

  const dataColumns = [
    { title: '时间', dataIndex: 'time', key: 'time' },
    { title: '温度(°C)', dataIndex: 'temperature', key: 'temperature' },
    { title: '流量(m³/h)', dataIndex: 'flow', key: 'flow' },
    { title: '能量(kWh)', dataIndex: 'energy', key: 'energy' },
  ];

  return (
    <div className="data-page">
      <h1>数据监控</h1>
      
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic title="当前温度" value={70} suffix="°C" />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="当前流量" value={128} suffix="m³/h" />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="能量产出" value={50} suffix="kWh" />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="系统状态" value="正常" />
          </Card>
        </Col>
      </Row>

      <Card 
        title="实时数据图表" 
        extra={<RangePicker />}
        style={{ marginBottom: 24 }}
      >
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={energyData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="time" />
            <YAxis />
            <Tooltip />
            <Legend />
            <Line type="monotone" dataKey="temperature" stroke="#ff4d4f" />
            <Line type="monotone" dataKey="flow" stroke="#1890ff" />
            <Line type="monotone" dataKey="energy" stroke="#52c41a" />
          </LineChart>
        </ResponsiveContainer>
      </Card>

      <Card title="历史数据记录">
        <Table 
          columns={dataColumns} 
          dataSource={energyData} 
          pagination={{ pageSize: 5 }}
          rowKey="time"
        />
      </Card>
    </div>
  );
};

export default Data;