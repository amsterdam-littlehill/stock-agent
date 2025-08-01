# Stock Agent Genie Backend 部署指南

本文档详细说明了 Stock Agent Genie Backend 的部署流程，包括开发环境、测试环境和生产环境的部署方案。

## 目录

- [系统要求](#系统要求)
- [环境准备](#环境准备)
- [配置说明](#配置说明)
- [部署方式](#部署方式)
  - [本地开发部署](#本地开发部署)
  - [Docker部署](#docker部署)
  - [Kubernetes部署](#kubernetes部署)
  - [云平台部署](#云平台部署)
- [数据库初始化](#数据库初始化)
- [监控配置](#监控配置)
- [安全配置](#安全配置)
- [性能优化](#性能优化)
- [故障排除](#故障排除)

## 系统要求

### 硬件要求

#### 最小配置
- CPU: 2核心
- 内存: 4GB RAM
- 存储: 20GB 可用空间
- 网络: 100Mbps

#### 推荐配置
- CPU: 4核心或更多
- 内存: 8GB RAM 或更多
- 存储: 100GB SSD
- 网络: 1Gbps

#### 生产环境配置
- CPU: 8核心或更多
- 内存: 16GB RAM 或更多
- 存储: 500GB SSD
- 网络: 10Gbps

### 软件要求

- **Java**: OpenJDK 17 或更高版本
- **Maven**: 3.8.0 或更高版本
- **MySQL**: 8.0 或更高版本
- **Redis**: 6.0 或更高版本
- **Docker**: 20.10 或更高版本（可选）
- **Kubernetes**: 1.20 或更高版本（可选）

## 环境准备

### 1. Java 环境

```bash
# 安装 OpenJDK 17
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel

# macOS
brew install openjdk@17

# 验证安装
java -version
```

### 2. Maven 环境

```bash
# Ubuntu/Debian
sudo apt install maven

# CentOS/RHEL
sudo yum install maven

# macOS
brew install maven

# 验证安装
mvn -version
```

### 3. MySQL 数据库

```bash
# Ubuntu/Debian
sudo apt install mysql-server

# CentOS/RHEL
sudo yum install mysql-server

# macOS
brew install mysql

# 启动服务
sudo systemctl start mysql
sudo systemctl enable mysql

# 安全配置
sudo mysql_secure_installation
```

### 4. Redis 缓存

```bash
# Ubuntu/Debian
sudo apt install redis-server

# CentOS/RHEL
sudo yum install redis

# macOS
brew install redis

# 启动服务
sudo systemctl start redis
sudo systemctl enable redis
```

## 配置说明

### 1. 环境变量配置

复制环境变量模板文件：

```bash
cp .env.example .env
```

编辑 `.env` 文件，配置以下关键参数：

```bash
# 基础配置
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=stock_agent_genie
DB_USERNAME=stockagent
DB_PASSWORD=your_secure_password

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# AI 服务配置
OPENAI_API_KEY=your_openai_api_key
OPENAI_BASE_URL=https://api.openai.com

# 安全配置
JWT_SECRET=your_jwt_secret_key_at_least_256_bits

# 监控配置
PROMETHEUS_PORT=9090
GRAFANA_PORT=3000
```

### 2. 应用配置文件

根据部署环境选择相应的配置文件：

- `application-dev.yml`: 开发环境
- `application-test.yml`: 测试环境
- `application-prod.yml`: 生产环境

## 部署方式

### 本地开发部署

#### 1. 克隆代码

```bash
git clone https://github.com/your-org/stock-agent-genie-backend.git
cd stock-agent-genie-backend
```

#### 2. 配置数据库

```sql
-- 创建数据库
CREATE DATABASE stock_agent_genie CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER 'stockagent'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON stock_agent_genie.* TO 'stockagent'@'localhost';
FLUSH PRIVILEGES;
```

#### 3. 构建和运行

```bash
# 使用启动脚本
chmod +x start.sh
./start.sh

# 或者手动构建运行
mvn clean compile
mvn spring-boot:run
```

#### 4. 验证部署

```bash
# 检查健康状态
curl http://localhost:8080/api/actuator/health

# 访问 API 文档
open http://localhost:8080/api/swagger-ui.html
```

### Docker部署

#### 1. 构建镜像

```bash
# 构建应用镜像
docker build -t stock-agent/genie-backend:latest .

# 或使用多阶段构建
docker build -f Dockerfile -t stock-agent/genie-backend:latest .
```

#### 2. 使用 Docker Compose

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend

# 停止服务
docker-compose down
```

#### 3. 单独运行容器

```bash
# 运行 MySQL
docker run -d --name mysql \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=stock_agent_genie \
  -e MYSQL_USER=stockagent \
  -e MYSQL_PASSWORD=password123 \
  -p 3306:3306 \
  mysql:8.0

# 运行 Redis
docker run -d --name redis \
  -p 6379:6379 \
  redis:7.2-alpine

# 运行应用
docker run -d --name stock-agent-backend \
  --link mysql:mysql \
  --link redis:redis \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=mysql \
  -e REDIS_HOST=redis \
  -p 8080:8080 \
  stock-agent/genie-backend:latest
```

### Kubernetes部署

#### 1. 创建命名空间

```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: stock-agent
```

```bash
kubectl apply -f namespace.yaml
```

#### 2. 配置 ConfigMap 和 Secret

```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: stock-agent-config
  namespace: stock-agent
data:
  SPRING_PROFILES_ACTIVE: "prod"
  DB_HOST: "mysql-service"
  REDIS_HOST: "redis-service"
  SERVER_PORT: "8080"
---
apiVersion: v1
kind: Secret
metadata:
  name: stock-agent-secret
  namespace: stock-agent
type: Opaque
data:
  DB_PASSWORD: cGFzc3dvcmQxMjM=  # base64 encoded
  REDIS_PASSWORD: cmVkaXNwYXNz      # base64 encoded
  JWT_SECRET: eW91ci1qd3Qtc2VjcmV0LWtleQ==
  OPENAI_API_KEY: eW91ci1vcGVuYWktYXBpLWtleQ==
```

#### 3. 部署数据库服务

```yaml
# mysql-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
  namespace: stock-agent
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql:8.0
        env:
        - name: MYSQL_ROOT_PASSWORD
          value: "root123"
        - name: MYSQL_DATABASE
          value: "stock_agent_genie"
        - name: MYSQL_USER
          value: "stockagent"
        - name: MYSQL_PASSWORD
          valueFrom:
            secretKeyRef:
              name: stock-agent-secret
              key: DB_PASSWORD
        ports:
        - containerPort: 3306
        volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql
      volumes:
      - name: mysql-storage
        persistentVolumeClaim:
          claimName: mysql-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: mysql-service
  namespace: stock-agent
spec:
  selector:
    app: mysql
  ports:
  - port: 3306
    targetPort: 3306
```

#### 4. 部署应用服务

```yaml
# backend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: stock-agent-backend
  namespace: stock-agent
spec:
  replicas: 3
  selector:
    matchLabels:
      app: stock-agent-backend
  template:
    metadata:
      labels:
        app: stock-agent-backend
    spec:
      containers:
      - name: backend
        image: stock-agent/genie-backend:latest
        envFrom:
        - configMapRef:
            name: stock-agent-config
        env:
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: stock-agent-secret
              key: DB_PASSWORD
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: stock-agent-secret
              key: REDIS_PASSWORD
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: stock-agent-secret
              key: JWT_SECRET
        - name: OPENAI_API_KEY
          valueFrom:
            secretKeyRef:
              name: stock-agent-secret
              key: OPENAI_API_KEY
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /api/actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /api/actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
---
apiVersion: v1
kind: Service
metadata:
  name: stock-agent-backend-service
  namespace: stock-agent
spec:
  selector:
    app: stock-agent-backend
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

#### 5. 部署 Ingress

```yaml
# ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: stock-agent-ingress
  namespace: stock-agent
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - api.stockagent.com
    secretName: stock-agent-tls
  rules:
  - host: api.stockagent.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: stock-agent-backend-service
            port:
              number: 80
```

### 云平台部署

#### AWS 部署

1. **使用 ECS Fargate**

```bash
# 创建 ECS 集群
aws ecs create-cluster --cluster-name stock-agent-cluster

# 创建任务定义
aws ecs register-task-definition --cli-input-json file://task-definition.json

# 创建服务
aws ecs create-service --cluster stock-agent-cluster --service-name stock-agent-backend --task-definition stock-agent-backend:1 --desired-count 2
```

2. **使用 EKS**

```bash
# 创建 EKS 集群
eksctl create cluster --name stock-agent-cluster --region us-west-2

# 部署应用
kubectl apply -f k8s/
```

#### Azure 部署

```bash
# 创建资源组
az group create --name stock-agent-rg --location eastus

# 创建 AKS 集群
az aks create --resource-group stock-agent-rg --name stock-agent-aks --node-count 3

# 获取凭据
az aks get-credentials --resource-group stock-agent-rg --name stock-agent-aks

# 部署应用
kubectl apply -f k8s/
```

#### Google Cloud 部署

```bash
# 创建 GKE 集群
gcloud container clusters create stock-agent-cluster --num-nodes=3

# 获取凭据
gcloud container clusters get-credentials stock-agent-cluster

# 部署应用
kubectl apply -f k8s/
```

## 数据库初始化

### 1. 自动初始化

应用启动时会自动执行 Flyway 数据库迁移脚本：

```bash
# 迁移脚本位置
src/main/resources/db/migration/
├── V1__Create_workflow_tables.sql
├── V2__Add_indexes.sql
└── V3__Insert_default_data.sql
```

### 2. 手动初始化

```bash
# 连接数据库
mysql -h localhost -u stockagent -p stock_agent_genie

# 执行初始化脚本
source sql/V1__Create_workflow_tables.sql
```

### 3. 数据备份和恢复

```bash
# 备份数据库
mysqldump -h localhost -u stockagent -p stock_agent_genie > backup.sql

# 恢复数据库
mysql -h localhost -u stockagent -p stock_agent_genie < backup.sql
```

## 监控配置

### 1. Prometheus 监控

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'stock-agent-backend'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/api/actuator/prometheus'
    scrape_interval: 30s
```

### 2. Grafana 仪表板

导入预配置的仪表板：

```bash
# 导入仪表板配置
cp monitoring/grafana/dashboards/* /var/lib/grafana/dashboards/
```

### 3. 日志监控

```yaml
# logback-spring.xml
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
      <providers>
        <timestamp/>
        <logLevel/>
        <loggerName/>
        <message/>
        <mdc/>
        <stackTrace/>
      </providers>
    </encoder>
  </appender>
  
  <root level="INFO">
    <appender-ref ref="STDOUT"/>
  </root>
</configuration>
```

## 安全配置

### 1. HTTPS 配置

```yaml
# application-prod.yml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: stockagent
```

### 2. 防火墙配置

```bash
# Ubuntu/Debian
sudo ufw allow 8080/tcp
sudo ufw allow 3306/tcp
sudo ufw allow 6379/tcp
sudo ufw enable

# CentOS/RHEL
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=3306/tcp
sudo firewall-cmd --permanent --add-port=6379/tcp
sudo firewall-cmd --reload
```

### 3. 访问控制

```nginx
# nginx.conf
server {
    listen 80;
    server_name api.stockagent.com;
    
    # 限制访问频率
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    
    location /api {
        limit_req zone=api burst=20 nodelay;
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

## 性能优化

### 1. JVM 调优

```bash
# 生产环境 JVM 参数
JAVA_OPTS="-Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UnlockExperimentalVMOptions \
  -XX:+UseJVMCICompiler \
  -XX:+PrintGCDetails \
  -XX:+PrintGCTimeStamps \
  -Xloggc:gc.log"
```

### 2. 数据库优化

```sql
-- MySQL 配置优化
[mysqld]
innodb_buffer_pool_size = 2G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2
max_connections = 200
query_cache_size = 128M
```

### 3. Redis 优化

```bash
# redis.conf
maxmemory 1gb
maxmemory-policy allkeys-lru
save 900 1
save 300 10
save 60 10000
```

## 故障排除

### 1. 常见问题

#### 应用启动失败

```bash
# 检查日志
tail -f logs/application.log

# 检查端口占用
netstat -tulpn | grep 8080

# 检查 Java 进程
jps -l
```

#### 数据库连接失败

```bash
# 测试数据库连接
mysql -h localhost -u stockagent -p

# 检查数据库状态
sudo systemctl status mysql

# 查看数据库日志
sudo tail -f /var/log/mysql/error.log
```

#### Redis 连接失败

```bash
# 测试 Redis 连接
redis-cli ping

# 检查 Redis 状态
sudo systemctl status redis

# 查看 Redis 日志
sudo tail -f /var/log/redis/redis-server.log
```

### 2. 性能问题诊断

```bash
# 查看系统资源使用
top
htop
iostat -x 1

# 查看 JVM 内存使用
jstat -gc <pid>
jmap -histo <pid>

# 生成堆转储
jmap -dump:format=b,file=heap.hprof <pid>
```

### 3. 网络问题诊断

```bash
# 测试网络连通性
ping api.stockagent.com
telnet api.stockagent.com 8080

# 查看网络连接
netstat -an | grep 8080
ss -tulpn | grep 8080

# 抓包分析
tcpdump -i any port 8080
```

## 维护操作

### 1. 日常维护

```bash
# 清理日志文件
find logs/ -name "*.log" -mtime +7 -delete

# 清理临时文件
find /tmp -name "stock-agent-*" -mtime +1 -delete

# 数据库优化
mysql -u root -p -e "OPTIMIZE TABLE workflow_definitions, workflow_executions;"
```

### 2. 版本升级

```bash
# 备份当前版本
cp -r /opt/stock-agent-genie /opt/stock-agent-genie.backup

# 停止服务
sudo systemctl stop stock-agent-genie

# 部署新版本
mvn clean package
cp target/genie-backend-*.jar /opt/stock-agent-genie/

# 启动服务
sudo systemctl start stock-agent-genie

# 验证升级
curl http://localhost:8080/api/actuator/info
```

### 3. 数据迁移

```bash
# 导出数据
mysqldump -u stockagent -p stock_agent_genie > migration.sql

# 导入到新环境
mysql -u stockagent -p new_stock_agent_genie < migration.sql
```

---

## 联系支持

如果在部署过程中遇到问题，请联系技术支持团队：

- **邮箱**: support@stockagent.com
- **文档**: https://docs.stockagent.com
- **GitHub**: https://github.com/your-org/stock-agent-genie-backend

---

*最后更新: 2024年1月*