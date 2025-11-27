# Thermal Ark Docker 部署指南

## 概述

本文档详细介绍了如何使用 Docker 和 Docker Compose 部署 Thermal Ark 热能管理系统。

## 系统要求

- Docker 20.10+
- Docker Compose 2.0+
- 至少 4GB 可用内存
- 至少 2GB 可用磁盘空间

## 快速开始

### Windows 用户

1. 双击运行 `deploy.bat` 文件
2. 或者使用 PowerShell：
   ```powershell
   .\deploy.bat
   ```

### Linux/macOS 用户

1. 给脚本添加执行权限：
   ```bash
   chmod +x deploy.sh
   ```
2. 运行部署脚本：
   ```bash
   ./deploy.sh
   ```

### 手动部署

如果您希望手动控制部署过程：

```bash
# 停止现有服务
docker-compose down

# 构建并启动所有服务
docker-compose up --build -d

# 查看服务状态
docker-compose ps

# 查看实时日志
docker-compose logs -f
```

## 服务架构

部署完成后，系统将包含以下服务：

| 服务 | 端口 | 描述 | 健康检查 |
|------|------|------|----------|
| MySQL | 3306 | 数据库服务 | `mysqladmin ping` |
| Redis | 6379 | 缓存服务 | `redis-cli ping` |
| Backend | 8080 | Spring Boot 后端 | `/actuator/health` |
| Frontend | 80 | React 前端 | 根路径访问 |
| IoT Simulator | 3001 | IoT 数据模拟器 | 进程状态检查 |

## 环境配置

### 环境变量

复制 `.env.example` 文件为 `.env` 并修改配置：

```bash
cp .env.example .env
```

主要配置项：

- **数据库配置**：MySQL 连接参数
- **Redis 配置**：缓存服务参数
- **后端配置**：Spring Boot 应用配置
- **前端配置**：API 和 WebSocket 端点
- **IoT 模拟器**：数据发送间隔和目标地址
- **安全配置**：JWT 密钥和过期时间

### 自定义配置

修改 `docker-compose.yml` 文件可以调整服务配置：

```yaml
services:
  backend:
    environment:
      SPRING_PROFILES_ACTIVE: prod  # 修改为 prod 环境
    ports:
      - "8080:8080"  # 修改端口映射
```

## 管理命令

### 服务管理

```bash
# 启动服务
docker-compose start

# 停止服务
docker-compose stop

# 重启服务
docker-compose restart

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs [service_name]
```

### 容器管理

```bash
# 进入容器
docker-compose exec backend bash

# 查看容器资源使用
docker stats

# 清理未使用的资源
docker system prune
```

### 数据库管理

```bash
# 进入 MySQL 容器
docker-compose exec mysql mysql -u root -p

# 备份数据库
docker-compose exec mysql mysqldump -u root -p thermal_ark > backup.sql

# 恢复数据库
docker-compose exec -T mysql mysql -u root -p thermal_ark < backup.sql
```

## 故障排除

### 常见问题

1. **端口冲突**
   - 错误：`端口已被占用`
   - 解决：修改 `docker-compose.yml` 中的端口映射

2. **内存不足**
   - 错误：`容器被杀死`
   - 解决：增加 Docker 内存限制或关闭其他应用

3. **构建失败**
   - 错误：`构建镜像失败`
   - 解决：检查网络连接，清理 Docker 缓存

### 日志分析

```bash
# 查看所有服务日志
docker-compose logs

# 查看特定服务日志
docker-compose logs backend

# 实时跟踪日志
docker-compose logs -f frontend

# 查看最近100行日志
docker-compose logs --tail=100
```

### 健康检查

```bash
# 检查所有服务健康状态
docker-compose ps

# 检查单个服务健康状态
docker inspect --format='{{.State.Health.Status}}' thermal-ark-web_backend_1

# 手动健康检查
curl http://localhost:8080/actuator/health
curl http://localhost:80
```

## 扩展部署

### 增加后端实例

```bash
# 扩展到2个后端实例
docker-compose up --scale backend=2 -d
```

### 添加负载均衡器

在 `docker-compose.yml` 中添加 Nginx 负载均衡器：

```yaml
services:
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - backend
```

### 数据持久化

确保重要数据持久化：

```yaml
services:
  mysql:
    volumes:
      - mysql_data:/var/lib/mysql
      - ./backups:/backups

volumes:
  mysql_data:
    driver: local
```

## 生产环境部署

### 安全配置

1. 修改默认密码
2. 启用 HTTPS
3. 配置防火墙规则
4. 设置定期备份

### 监控配置

1. 配置日志收集
2. 设置性能监控
3. 配置告警规则

### 高可用配置

1. 多实例部署
2. 负载均衡
3. 数据库主从复制

## 卸载

```bash
# 停止并删除所有容器
docker-compose down

# 删除所有镜像
docker-compose down --rmi all

# 删除所有数据卷（谨慎操作）
docker-compose down -v
```

## 技术支持

如果遇到问题，请检查：

1. Docker 和 Docker Compose 版本
2. 系统资源使用情况
3. 服务日志输出
4. 网络连接状态