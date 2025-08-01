#!/bin/bash

# Stock Agent Genie Backend Docker 启动脚本
# Author: Stock Agent Team
# Version: 1.0.0

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
log() {
    local level=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    
    case $level in
        "INFO")
            echo -e "${GREEN}[$timestamp] [INFO]${NC} $message"
            ;;
        "WARN")
            echo -e "${YELLOW}[$timestamp] [WARN]${NC} $message"
            ;;
        "ERROR")
            echo -e "${RED}[$timestamp] [ERROR]${NC} $message"
            ;;
        "DEBUG")
            echo -e "${BLUE}[$timestamp] [DEBUG]${NC} $message"
            ;;
        *)
            echo "[$timestamp] $message"
            ;;
    esac
}

# 打印启动横幅
print_banner() {
    echo -e "${BLUE}"
    cat << 'EOF'
    ███████╗████████╗ ██████╗  ██████╗██╗  ██╗     █████╗  ██████╗ ███████╗███╗   ██╗████████╗
    ██╔════╝╚══██╔══╝██╔═══██╗██╔════╝██║ ██╔╝    ██╔══██╗██╔════╝ ██╔════╝████╗  ██║╚══██╔══╝
    ███████╗   ██║   ██║   ██║██║     █████╔╝     ███████║██║  ███╗█████╗  ██╔██╗ ██║   ██║   
    ╚════██║   ██║   ██║   ██║██║     ██╔═██╗     ██╔══██║██║   ██║██╔══╝  ██║╚██╗██║   ██║   
    ███████║   ██║   ╚██████╔╝╚██████╗██║  ██╗    ██║  ██║╚██████╔╝███████╗██║ ╚████║   ██║   
    ╚══════╝   ╚═╝    ╚═════╝  ╚═════╝╚═╝  ╚═╝    ╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚═╝  ╚═══╝   ╚═╝   
                                                                                              
                            ██████╗ ███████╗███╗   ██╗██╗███████╗                           
                           ██╔════╝ ██╔════╝████╗  ██║██║██╔════╝                           
                           ██║  ███╗█████╗  ██╔██╗ ██║██║█████╗                             
                           ██║   ██║██╔══╝  ██║╚██╗██║██║██╔══╝                             
                           ╚██████╔╝███████╗██║ ╚████║██║███████╗                           
                            ╚═════╝ ╚══════╝╚═╝  ╚═══╝╚═╝╚══════╝                           
EOF
    echo -e "${NC}"
    log "INFO" "Stock Agent Genie Backend Docker Container Starting..."
    log "INFO" "Version: 1.0.0"
    log "INFO" "Environment: ${SPRING_PROFILES_ACTIVE:-prod}"
}

# 等待服务可用
wait_for_service() {
    local host=$1
    local port=$2
    local service_name=$3
    local timeout=${4:-60}
    
    log "INFO" "Waiting for $service_name at $host:$port..."
    
    local count=0
    while ! nc -z "$host" "$port" >/dev/null 2>&1; do
        if [ $count -ge $timeout ]; then
            log "ERROR" "Timeout waiting for $service_name at $host:$port"
            return 1
        fi
        log "DEBUG" "$service_name not ready, waiting... ($count/$timeout)"
        sleep 1
        count=$((count + 1))
    done
    
    log "INFO" "$service_name is ready at $host:$port"
    return 0
}

# 检查数据库连接
check_database() {
    if [ -n "$DB_HOST" ] && [ -n "$DB_PORT" ]; then
        wait_for_service "$DB_HOST" "$DB_PORT" "MySQL Database" 120
        if [ $? -ne 0 ]; then
            log "ERROR" "Failed to connect to database"
            exit 1
        fi
    else
        log "WARN" "Database connection parameters not provided, skipping database check"
    fi
}

# 检查Redis连接
check_redis() {
    if [ -n "$REDIS_HOST" ] && [ -n "$REDIS_PORT" ]; then
        wait_for_service "$REDIS_HOST" "$REDIS_PORT" "Redis Cache" 60
        if [ $? -ne 0 ]; then
            log "WARN" "Failed to connect to Redis, application may have limited functionality"
        fi
    else
        log "WARN" "Redis connection parameters not provided, skipping Redis check"
    fi
}

# 设置默认环境变量
set_defaults() {
    export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
    export SERVER_PORT=${SERVER_PORT:-8080}
    export JAVA_OPTS=${JAVA_OPTS:-"-Xms512m -Xmx2g -XX:+UseG1GC -XX:+UseStringDeduplication"}
    
    # 数据库配置
    export DB_HOST=${DB_HOST:-mysql}
    export DB_PORT=${DB_PORT:-3306}
    export DB_NAME=${DB_NAME:-stock_agent_genie}
    export DB_USERNAME=${DB_USERNAME:-root}
    
    # Redis配置
    export REDIS_HOST=${REDIS_HOST:-redis}
    export REDIS_PORT=${REDIS_PORT:-6379}
    
    log "INFO" "Environment configuration:"
    log "INFO" "  Profile: $SPRING_PROFILES_ACTIVE"
    log "INFO" "  Port: $SERVER_PORT"
    log "INFO" "  Database: $DB_HOST:$DB_PORT/$DB_NAME"
    log "INFO" "  Redis: $REDIS_HOST:$REDIS_PORT"
}

# 健康检查
health_check() {
    local max_attempts=30
    local attempt=1
    
    log "INFO" "Starting health check..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "http://localhost:$SERVER_PORT/api/actuator/health" >/dev/null 2>&1; then
            log "INFO" "Application is healthy and ready to serve requests"
            return 0
        fi
        
        log "DEBUG" "Health check attempt $attempt/$max_attempts failed, retrying..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    log "ERROR" "Application failed health check after $max_attempts attempts"
    return 1
}

# 优雅关闭处理
graceful_shutdown() {
    log "INFO" "Received shutdown signal, initiating graceful shutdown..."
    
    # 发送SIGTERM信号给Java进程
    if [ -n "$JAVA_PID" ]; then
        kill -TERM "$JAVA_PID"
        
        # 等待进程优雅关闭
        local count=0
        while kill -0 "$JAVA_PID" 2>/dev/null && [ $count -lt 30 ]; do
            log "DEBUG" "Waiting for application to shutdown... ($count/30)"
            sleep 1
            count=$((count + 1))
        done
        
        # 如果进程仍在运行，强制终止
        if kill -0 "$JAVA_PID" 2>/dev/null; then
            log "WARN" "Application did not shutdown gracefully, forcing termination"
            kill -KILL "$JAVA_PID"
        else
            log "INFO" "Application shutdown gracefully"
        fi
    fi
    
    exit 0
}

# 主函数
main() {
    # 打印启动横幅
    print_banner
    
    # 设置默认值
    set_defaults
    
    # 检查依赖服务
    check_database
    check_redis
    
    # 设置信号处理
    trap graceful_shutdown SIGTERM SIGINT
    
    log "INFO" "Starting Stock Agent Genie Backend..."
    
    # 构建Java命令
    JAVA_CMD="java $JAVA_OPTS"
    JAVA_CMD="$JAVA_CMD -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE"
    JAVA_CMD="$JAVA_CMD -Dserver.port=$SERVER_PORT"
    
    # 添加数据库配置
    if [ -n "$DB_HOST" ]; then
        JAVA_CMD="$JAVA_CMD -Dspring.datasource.url=jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
    fi
    if [ -n "$DB_USERNAME" ]; then
        JAVA_CMD="$JAVA_CMD -Dspring.datasource.username=$DB_USERNAME"
    fi
    if [ -n "$DB_PASSWORD" ]; then
        JAVA_CMD="$JAVA_CMD -Dspring.datasource.password=$DB_PASSWORD"
    fi
    
    # 添加Redis配置
    if [ -n "$REDIS_HOST" ]; then
        JAVA_CMD="$JAVA_CMD -Dspring.data.redis.host=$REDIS_HOST"
    fi
    if [ -n "$REDIS_PORT" ]; then
        JAVA_CMD="$JAVA_CMD -Dspring.data.redis.port=$REDIS_PORT"
    fi
    if [ -n "$REDIS_PASSWORD" ]; then
        JAVA_CMD="$JAVA_CMD -Dspring.data.redis.password=$REDIS_PASSWORD"
    fi
    
    # 添加其他配置
    JAVA_CMD="$JAVA_CMD -jar app.jar"
    
    log "INFO" "Executing: $JAVA_CMD"
    
    # 启动应用（后台运行以便处理信号）
    $JAVA_CMD &
    JAVA_PID=$!
    
    log "INFO" "Application started with PID: $JAVA_PID"
    
    # 等待应用启动完成
    sleep 10
    
    # 执行健康检查
    if [ "$SKIP_HEALTH_CHECK" != "true" ]; then
        health_check
        if [ $? -eq 0 ]; then
            log "INFO" "Application is ready to serve requests at http://localhost:$SERVER_PORT/api"
            log "INFO" "API Documentation: http://localhost:$SERVER_PORT/api/swagger-ui.html"
            log "INFO" "Health Check: http://localhost:$SERVER_PORT/api/actuator/health"
        fi
    fi
    
    # 等待Java进程结束
    wait $JAVA_PID
    
    log "INFO" "Application has stopped"
}

# 如果脚本被直接执行（不是被source）
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    main "$@"
fi