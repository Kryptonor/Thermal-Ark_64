# 热力方舟 - 热能交易平台

基于区块链的P2P热能交易平台，实现去中心化的热能交易市场。

## 项目结构

```
thermal-ark-web/
├── backend/          # Spring Boot后端服务
├── frontend/         # React前端应用
├── blockchain/       # 智能合约和区块链集成
├── iot/             # IoT数据模拟器
└── docs/            # 项目文档
```

## 环境要求

### 必需环境
- **Docker & Docker Compose** (推荐使用Docker Desktop)
- **Java 17+** (OpenJDK或Oracle JDK)
- **Node.js 18+** (LTS版本)
- **MySQL 8.0+** (或使用Docker镜像)
- **Python 3.8+** (用于IoT数据模拟器)

### 可选环境
- **Ganache** (本地以太坊测试网络)
- **Truffle** (智能合约开发框架)
- **Redis** (缓存和会话管理)

## 快速启动

### 方式一：Docker Compose (推荐)

1. **克隆项目**
```bash
git clone https://github.com/thermalark/thermal-ark-web.git
cd thermal-ark-web
```

2. **环境配置**
```bash
# 复制环境配置文件
cp .env.example .env
# 编辑配置文件，设置数据库密码等参数
```

3. **启动所有服务**
```bash
docker-compose up -d
```

4. **访问应用**
- 前端应用: http://localhost:3000
- 后端API: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui.html
- 数据库管理: http://localhost:8081 (phpMyAdmin)

### 方式二：手动部署

#### 1. 数据库配置
```bash
# 启动MySQL数据库
docker run -d --name mysql-thermalark \
  -e MYSQL_ROOT_PASSWORD=thermalark123 \
  -e MYSQL_DATABASE=thermalark \
  -p 3306:3306 mysql:8.0

# 或使用本地MySQL
mysql -u root -p
CREATE DATABASE thermalark;
```

#### 2. 后端服务
```bash
cd backend

# 配置数据库连接
cp src/main/resources/application-docker.yml src/main/resources/application.yml
# 编辑application.yml，设置数据库连接信息

# 构建和运行
./mvnw clean package
java -jar target/thermal-ark-backend-1.0.0.jar
```

#### 3. 前端应用
```bash
cd frontend

# 安装依赖
npm install

# 开发模式运行
npm start

# 生产构建
npm run build
```

#### 4. 区块链部署
```bash
cd blockchain

# 安装依赖
npm install

# 启动Ganache测试网络
npm run ganache

# 编译和部署合约
npm run compile
npm run migrate
```

#### 5. IoT数据模拟器
```bash
cd iot

# 安装Python依赖
pip install -r requirements.txt

# 运行模拟器
python thermal_simulator.py
```

## 访问地址

部署成功后，可以通过以下地址访问系统：

- **前端应用**: http://localhost:80
- **后端API**: http://localhost:8080
- **IoT模拟器**: http://localhost:3001
- **MySQL数据库**: localhost:3306
- **Redis缓存**: localhost:6379

## 功能特性

### 🔥 热能交易
- 实时热能价格展示
- 点对点热能交易
- 交易历史记录
- 智能合约自动执行

### 📊 数据监控
- 实时传感器数据展示
- 热能使用统计
- 交易数据分析
- 可视化图表

### 🔐 安全认证
- JWT身份验证
- 用户权限管理
- 数据加密传输
- 智能合约安全验证

### 🌐 多设备支持
- 响应式Web设计
- 移动端适配
- 实时数据推送
- WebSocket连接

## 开发指南

### 项目结构
```
thermal-ark-web/
├── frontend/          # React前端应用
├── backend/           # Spring Boot后端服务
├── blockchain/        # 智能合约和区块链相关
├── iot/              # IoT数据模拟器
├── docker-compose.yml # Docker编排文件
├── deploy.sh         # 部署脚本(Linux/macOS)
├── deploy.bat        # 部署脚本(Windows)
└── README.md         # 项目文档
```

### 开发环境设置

1. **克隆项目**
```bash
git clone <repository-url>
cd thermal-ark-web
```

2. **安装依赖**
```bash
# 前端依赖
cd frontend && npm install

# 后端依赖
cd backend && mvn clean install

# IoT模拟器依赖
cd iot && npm install
```

3. **启动开发服务器**
```bash
# 启动后端 (终端1)
cd backend && mvn spring-boot:run

# 启动前端 (终端2)
cd frontend && npm run dev

# 启动IoT模拟器 (终端3)
cd iot && npm start
```

### API文档

后端API文档可在启动后访问：
- Swagger UI: http://localhost:8080/swagger-ui.html
- API文档: http://localhost:8080/v3/api-docs

## 部署说明

### 生产环境部署

1. **环境变量配置**
```bash
cp .env.example .env
# 编辑.env文件，设置生产环境参数
```

2. **构建和部署**
```bash
docker-compose -f docker-compose.prod.yml up --build -d
```

### 监控和日志

- **应用日志**: `docker-compose logs -f [service-name]`
- **性能监控**: 集成Spring Boot Actuator
- **健康检查**: 自动健康检查端点

## 故障排除

### 常见问题

1. **端口冲突**
   - 修改`docker-compose.yml`中的端口映射
   - 检查本地端口占用情况

2. **依赖安装失败**
   - 清理缓存: `npm cache clean --force` 或 `mvn clean`
   - 检查网络连接

3. **数据库连接失败**
   - 检查MySQL服务状态
   - 验证连接参数配置

### 获取帮助

- 查看详细部署指南: [DOCKER_DEPLOYMENT.md](./DOCKER_DEPLOYMENT.md)
- 检查服务状态: `docker-compose ps`
- 查看服务日志: `docker-compose logs`

## 贡献指南

我们欢迎社区贡献！请参考以下指南：

1. Fork项目仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 许可证

本项目采用MIT许可证。详见 [LICENSE](LICENSE) 文件。

## 联系方式

- 项目主页: [GitHub Repository](https://github.com/Kryptonor/Thermal-Ark_64)
- 问题反馈: [Issues](https://github.com/Kryptonor/Thermal-Ark_64/issues)
- 邮箱: PicoDn027@gmail.com
- 博客：[Personal Blog](www.dnhut.top)

---

**Thermal Ark Web** - 让热能交易更智能、更高效！
