# Genie Backend

## 项目简介

Genie Backend 是一个基于 Java 和 Spring Boot 的后端服务，为 Genie 项目提供核心功能支持。该项目集成了多种智能代理（Agent），旨在提供高效的智能对话和任务处理能力。

## 技术栈

- Java
- Spring Boot
- Maven
- SSE (Server-Sent Events)
- 多代理系统

## 项目结构

```
genie-backend/
├── src/
│   ├── main/
│   │   ├── java/com/jd/genie/
│   │   │   ├── agent/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── handler/
│   │   │   ├── model/
│   │   │   ├── service/
│   │   │   └── util/
│   │   └── resources/
│   └── test/
├── pom.xml
├── build.sh
├── start.sh
└── README.md
```

## 安装和运行

1. 确保您的系统已安装 Java 和 Maven。

2. 克隆项目到本地：
   ```
   git clone [项目地址]
   cd genie-backend
   ```

3. 编译项目：
   ```
   ./build.sh
   ```

4. 运行项目：
   ```
   ./start.sh
   ```

## 主要功能

1. 多代理系统：支持多种智能代理，包括规划代理、执行代理、ReAct代理等。
2. 代码解释器：能够解释和执行用户提供的代码片段。
3. 深度搜索：提供高级搜索功能，可在大规模数据中快速定位相关信息。
4. 文件操作：支持文件的读取、写入和管理。
5. SSE 实时通信：使用服务器发送事件（SSE）技术实现实时数据推送。

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
