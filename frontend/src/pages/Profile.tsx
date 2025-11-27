import React from 'react';
import { Card, Form, Input, Button, Upload, message, Avatar, Descriptions, Row, Col } from 'antd';
import { UserOutlined, UploadOutlined } from '@ant-design/icons';
import './Profile.css';

const Profile: React.FC = () => {
  const [form] = Form.useForm();

  const onFinish = (values: any) => {
    console.log('提交数据:', values);
    message.success('个人信息更新成功');
  };

  const uploadProps = {
    beforeUpload: (file: File) => {
      const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
      if (!isJpgOrPng) {
        message.error('只能上传 JPG/PNG 格式的图片!');
      }
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isLt2M) {
        message.error('图片大小不能超过 2MB!');
      }
      return isJpgOrPng && isLt2M;
    },
  };

  return (
    <div className="profile-page">
      <h1>个人资料</h1>
      
      <Row gutter={[24, 24]}>
        <Col span={8}>
          <Card title="头像设置" style={{ marginBottom: 24 }}>
            <div style={{ textAlign: 'center' }}>
              <Avatar size={100} icon={<UserOutlined />} />
              <Upload {...uploadProps} style={{ marginTop: 16 }}>
                <Button icon={<UploadOutlined />}>上传头像</Button>
              </Upload>
            </div>
          </Card>

          <Card title="账户信息">
            <Descriptions column={1} bordered>
              <Descriptions.Item label="用户ID">TA001</Descriptions.Item>
              <Descriptions.Item label="注册时间">2024-01-15</Descriptions.Item>
              <Descriptions.Item label="账户类型">企业用户</Descriptions.Item>
              <Descriptions.Item label="认证状态">已认证</Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>

        <Col span={16}>
          <Card title="基本信息">
            <Form
              form={form}
              layout="vertical"
              onFinish={onFinish}
              initialValues={{
                name: '张三',
                email: 'zhangsan@example.com',
                phone: '13800138000',
                company: '热力科技有限公司',
                address: '北京市朝阳区某某街道123号'
              }}
            >
              <Form.Item
                label="姓名"
                name="name"
                rules={[{ required: true, message: '请输入姓名' }]}
              >
                <Input />
              </Form.Item>

              <Form.Item
                label="邮箱"
                name="email"
                rules={[
                  { required: true, message: '请输入邮箱' },
                  { type: 'email', message: '请输入有效的邮箱地址' }
                ]}
              >
                <Input />
              </Form.Item>

              <Form.Item
                label="手机号"
                name="phone"
                rules={[{ required: true, message: '请输入手机号' }]}
              >
                <Input />
              </Form.Item>

              <Form.Item
                label="公司名称"
                name="company"
              >
                <Input />
              </Form.Item>

              <Form.Item
                label="地址"
                name="address"
              >
                <Input.TextArea rows={3} />
              </Form.Item>

              <Form.Item>
                <Button type="primary" htmlType="submit">
                  保存修改
                </Button>
              </Form.Item>
            </Form>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Profile;