# Stock Agent Genie Backend

🤖 **智能股票分析平台 - 智能体协调器后端服务**

## 📋 项目概述

Stock Agent Genie 是一个基于Spring Boot的智能股票分析平台，集成了多个AI智能体和自定义工作流引擎。系统通过协调多个专业分析师智能体，为用户提供全面、准确的股票投资分析和建议。

## ✨ 核心功能

### 🤖 智能体协调器
- **多智能体管理**: 统一管理和调度6种专业分析师
- **并行分析**: 支持多个智能体同时工作，提高分析效率
- **结果整合**: 智能整合各分析师的结果，提供综合投资建议
- **实时监控**: 实时监控智能体状态和执行进度

### 🔄 工作流引擎
- **可视化设计**: 支持用户自定义分析工作流
- **多种节点类型**: 开始、结束、智能体、工具、条件、脚本、延迟、通知节点
- **灵活调度**: 支持串行、并行、条件分支等执行模式
- **版本管理**: 工作流版本控制和历史记录

### 🛠️ MCP工具集成
- **丰富工具库**: 数据分析、市场数据、技术分析、风险管理等工具
- **统一接口**: 标准化的工具调用接口
- **健康监控**: 工具状态监控和自动恢复
- **性能统计**: 工具使用统计和性能分析

### 📡 实时通信
- **WebSocket支持**: 实时推送分析结果和进度更新
- **多端点支持**: 分析和工作流独立的WebSocket端点
- **消息广播**: 支持一对一和一对多消息推送

### 📊 数据持久化
- **完整记录**: 保存所有执行历史和统计数据
- **性能分析**: 提供详细的性能统计和趋势分析
- **数据清理**: 自动清理过期数据，保持系统性能

## 🧠 智能体类型

| 智能体 | 功能描述 | 分析重点 |
|--------|----------|----------|
| 📊 **基本面分析师** | 分析公司财务数据和基本面指标 | 财务报表、估值模型、行业对比 |
| 📈 **技术分析师** | 进行技术指标分析和图表分析 | K线形态、技术指标、支撑阻力 |
| 🌍 **市场分析师** | 分析市场趋势和宏观经济因素 | 市场情绪、宏观经济、行业趋势 |
| ⚠️ **风险分析师** | 评估投资风险和风险管理 | 风险评估、波动率分析、风控建议 |
| 💭 **情感分析师** | 分析市场情绪和新闻舆情 | 新闻情感、社交媒体、市场情绪 |
| 💡 **投资顾问** | 综合各方面分析提供投资建议 | 综合建议、投资策略、风险提示 |

## 🔧 工作流节点类型

| 节点类型 | 图标 | 功能描述 |
|----------|------|----------|
| **开始节点** | 🚀 | 工作流入口，设置初始参数 |
| **结束节点** | 🏁 | 工作流出口，输出最终结果 |
| **智能体节点** | 🤖 | 调用AI智能体进行分析 |
| **工具节点** | 🛠️ | 调用MCP工具处理数据 |
| **条件节点** | ❓ | 根据条件分支执行 |
| **脚本节点** | 📝 | 执行自定义脚本代码 |
| **延迟节点** | ⏰ | 延迟等待指定时间 |
| **通知节点** | 📢 | 发送通知消息 |

## 🚀 快速开始

### 环境要求

- **Java**: 17+
- **Maven**: 3.8+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Node.js**: 16+ (前端开发)

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/stock-agent/genie-backend.git
cd genie-backend
```

2. **配置数据库**
```sql
-- 创建数据库
CREATE DATABASE stock_agent_genie CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER 'stock_agent'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON stock_agent_genie.* TO 'stock_agent'@'localhost';
FLUSH PRIVILEGES;
```

3. **配置应用**
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/stock_agent_genie
    username: stock_agent
    password: your_password
  
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
```

4. **构建和运行**
```bash
# 构建项目
mvn clean compile

# 运行应用
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/genie-backend-1.0.0.jar
```

5. **验证安装**
- 访问 http://localhost:8080/api/actuator/health
- 访问 http://localhost:8080/api/swagger-ui.html

## 📚 API 文档

### 主要端点

| 模块 | 端点 | 描述 |
|------|------|------|
| **智能体管理** | `/api/agent/**` | 智能体相关操作 |
| **协调器** | `/api/orchestrator/**` | 协调器管理和执行 |
| **工作流** | `/api/workflow/**` | 工作流定义和执行 |
| **MCP工具** | `/api/tool/**` | 工具管理和调用 |
| **用户管理** | `/api/user/**` | 用户认证和管理 |
| **系统监控** | `/api/actuator/**` | 系统健康和监控 |

### WebSocket 端点

| 端点 | 用途 |
|------|------|
| `/api/ws/analysis` | 实时分析结果推送 |
| `/api/ws/workflow` | 工作流执行状态推送 |

## 🏗️ 项目结构

```
src/main/java/com/jd/genie/
├── agent/                      # 智能体模块
│   ├── orchestrator/          # 协调器核心
│   ├── workflow/              # 工作流引擎
│   │   ├── model/            # 工作流模型
│   │   ├── engine/           # 执行引擎
│   │   ├── service/          # 服务层
│   │   ├── controller/       # 控制器
│   │   └── repository/       # 数据访问
│   ├── tool/                 # MCP工具集成
│   │   └── mcp/             # MCP工具实现
│   ├── dto/                  # 数据传输对象
│   ├── enums/               # 枚举定义
│   └── exception/           # 异常定义
├── config/                   # 配置类
│   ├── GlobalConfiguration.java
│   ├── SwaggerConfiguration.java
│   └── GlobalExceptionHandler.java
└── StockAgentGenieApplication.java  # 启动类

src/main/resources/
├── application.yml           # 主配置文件
├── db/migration/            # 数据库迁移脚本
│   └── V1__Create_workflow_tables.sql
└── static/                  # 静态资源
```

## 配置说明

主要配置文件位于 `src/main/resources/application.yml`。您可以根据需要修改以下配置：

- 服务器端口
- LLM服务地址及APIKEY

code_interpreter_url: "http://127.0.0.1:1601"
deep_search_url: "http://127.0.0.1:1601"
mcp_client_url: "http://127.0.0.1:8188"

配置完后重新编译


## 贡献指南

我们欢迎所有形式的贡献，包括但不限于：

1. 报告 Bug
2. 提交功能请求
3. 编写文档
4. 提交代码改进

请确保在提交 Pull Request 之前，已经运行了所有测试并且代码符合项目的编码规范。

## 许可证

本项目采用 [MIT] 许可证。详情请参阅 LICENSE 文件。
