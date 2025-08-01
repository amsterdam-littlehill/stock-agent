# Stock Agent Genie Backend Dockerfile
# 多阶段构建，优化镜像大小和构建效率

# 构建阶段
FROM maven:3.9.5-openjdk-17-slim AS builder

# 设置工作目录
WORKDIR /app

# 复制Maven配置文件
COPY pom.xml .

# 下载依赖（利用Docker缓存层）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM openjdk:17-jdk-slim

# 设置维护者信息
LABEL maintainer="Stock Agent Team <team@stockagent.com>"
LABEL description="Stock Agent Genie Backend - 智能股票分析平台后端服务"
LABEL version="1.0.0"

# 安装必要的系统包
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# 创建应用用户
RUN groupadd -r stockagent && useradd -r -g stockagent stockagent

# 设置工作目录
WORKDIR /app

# 创建必要的目录
RUN mkdir -p /app/logs /app/data /app/config \
    && chown -R stockagent:stockagent /app

# 从构建阶段复制JAR文件
COPY --from=builder /app/target/genie-backend-*.jar app.jar

# 复制启动脚本
COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:+UseStringDeduplication -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

# 设置Spring Boot参数
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# 切换到应用用户
USER stockagent

# 设置卷
VOLUME ["/app/logs", "/app/data"]

# 启动应用
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
CMD ["java", "-jar", "app.jar"]