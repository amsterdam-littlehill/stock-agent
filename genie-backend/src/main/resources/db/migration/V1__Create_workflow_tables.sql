-- 工作流系统数据库初始化脚本
-- Stock Agent Genie Workflow System Database Schema

-- 创建工作流定义表
CREATE TABLE IF NOT EXISTS workflow_definitions (
    id VARCHAR(36) PRIMARY KEY COMMENT '工作流ID',
    name VARCHAR(255) NOT NULL COMMENT '工作流名称',
    description TEXT COMMENT '工作流描述',
    version VARCHAR(50) NOT NULL DEFAULT '1.0.0' COMMENT '版本号',
    creator_id VARCHAR(36) NOT NULL COMMENT '创建者ID',
    workflow_type VARCHAR(50) NOT NULL DEFAULT 'CUSTOM' COMMENT '工作流类型',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    is_public BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否公开',
    category VARCHAR(100) COMMENT '分类',
    tags JSON COMMENT '标签',
    nodes JSON NOT NULL COMMENT '节点定义',
    connections JSON COMMENT '连接关系',
    triggers JSON COMMENT '触发器配置',
    execution_config JSON COMMENT '执行配置',
    input_parameters JSON COMMENT '输入参数定义',
    output_parameters JSON COMMENT '输出参数定义',
    usage_count BIGINT NOT NULL DEFAULT 0 COMMENT '使用次数',
    avg_execution_time BIGINT COMMENT '平均执行时间(毫秒)',
    success_rate DECIMAL(5,4) COMMENT '成功率',
    last_execution_time DATETIME COMMENT '最后执行时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '软删除标记',
    deleted_time DATETIME COMMENT '删除时间',
    version_number BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    metadata JSON COMMENT '扩展字段',
    
    INDEX idx_creator_id (creator_id),
    INDEX idx_workflow_type (workflow_type),
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_created_time (created_time),
    INDEX idx_updated_time (updated_time),
    INDEX idx_deleted (deleted),
    INDEX idx_usage_count (usage_count),
    INDEX idx_success_rate (success_rate),
    INDEX idx_last_execution_time (last_execution_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流定义表';

-- 创建工作流执行记录表
CREATE TABLE IF NOT EXISTS workflow_executions (
    execution_id VARCHAR(36) PRIMARY KEY COMMENT '执行ID',
    workflow_id VARCHAR(36) NOT NULL COMMENT '工作流ID',
    workflow_version VARCHAR(50) NOT NULL COMMENT '工作流版本',
    executor_id VARCHAR(36) NOT NULL COMMENT '执行者ID',
    trigger_mode VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT '触发方式',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '执行状态',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration BIGINT COMMENT '执行时长(毫秒)',
    input_data JSON COMMENT '输入数据',
    output_data JSON COMMENT '输出数据',
    execution_context JSON COMMENT '执行上下文',
    node_executions JSON COMMENT '节点执行记录',
    progress DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '执行进度',
    error_message TEXT COMMENT '错误信息',
    warning_messages JSON COMMENT '警告信息',
    execution_logs JSON COMMENT '执行日志',
    performance_stats JSON COMMENT '性能统计',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    max_retries INT NOT NULL DEFAULT 0 COMMENT '最大重试次数',
    parent_execution_id VARCHAR(36) COMMENT '父执行ID',
    child_execution_ids JSON COMMENT '子执行ID列表',
    priority INT NOT NULL DEFAULT 5 COMMENT '优先级(1-10)',
    timeout_seconds INT COMMENT '超时时间(秒)',
    is_cancellable BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否可取消',
    is_pausable BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否可暂停',
    paused_time DATETIME COMMENT '暂停时间',
    resumed_time DATETIME COMMENT '恢复时间',
    cancelled_time DATETIME COMMENT '取消时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version_number BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    metadata JSON COMMENT '扩展字段',
    
    INDEX idx_workflow_id (workflow_id),
    INDEX idx_executor_id (executor_id),
    INDEX idx_status (status),
    INDEX idx_trigger_mode (trigger_mode),
    INDEX idx_start_time (start_time),
    INDEX idx_end_time (end_time),
    INDEX idx_duration (duration),
    INDEX idx_created_time (created_time),
    INDEX idx_parent_execution_id (parent_execution_id),
    INDEX idx_priority (priority),
    INDEX idx_progress (progress),
    INDEX idx_retry_count (retry_count),
    
    FOREIGN KEY (workflow_id) REFERENCES workflow_definitions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流执行记录表';

-- 创建MCP工具注册表
CREATE TABLE IF NOT EXISTS mcp_tools (
    tool_id VARCHAR(100) PRIMARY KEY COMMENT '工具ID',
    tool_name VARCHAR(255) NOT NULL COMMENT '工具名称',
    description TEXT COMMENT '工具描述',
    category VARCHAR(100) NOT NULL COMMENT '工具分类',
    tags JSON COMMENT '工具标签',
    version VARCHAR(50) NOT NULL DEFAULT '1.0.0' COMMENT '工具版本',
    class_name VARCHAR(500) NOT NULL COMMENT '实现类名',
    config JSON COMMENT '工具配置',
    input_schema JSON COMMENT '输入参数模式',
    output_schema JSON COMMENT '输出参数模式',
    dependencies JSON COMMENT '依赖关系',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    timeout_seconds INT NOT NULL DEFAULT 30 COMMENT '超时时间(秒)',
    max_retries INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    health_check_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用健康检查',
    health_check_interval INT NOT NULL DEFAULT 60 COMMENT '健康检查间隔(秒)',
    last_health_check DATETIME COMMENT '最后健康检查时间',
    health_status VARCHAR(20) DEFAULT 'UNKNOWN' COMMENT '健康状态',
    usage_count BIGINT NOT NULL DEFAULT 0 COMMENT '使用次数',
    success_count BIGINT NOT NULL DEFAULT 0 COMMENT '成功次数',
    failure_count BIGINT NOT NULL DEFAULT 0 COMMENT '失败次数',
    avg_execution_time BIGINT COMMENT '平均执行时间(毫秒)',
    last_used_time DATETIME COMMENT '最后使用时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    metadata JSON COMMENT '扩展字段',
    
    INDEX idx_category (category),
    INDEX idx_enabled (enabled),
    INDEX idx_health_status (health_status),
    INDEX idx_usage_count (usage_count),
    INDEX idx_success_count (success_count),
    INDEX idx_failure_count (failure_count),
    INDEX idx_last_used_time (last_used_time),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MCP工具注册表';

-- 创建工具执行记录表
CREATE TABLE IF NOT EXISTS tool_executions (
    execution_id VARCHAR(36) PRIMARY KEY COMMENT '执行ID',
    tool_id VARCHAR(100) NOT NULL COMMENT '工具ID',
    workflow_execution_id VARCHAR(36) COMMENT '关联的工作流执行ID',
    node_id VARCHAR(100) COMMENT '关联的节点ID',
    executor_id VARCHAR(36) NOT NULL COMMENT '执行者ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '执行状态',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration BIGINT COMMENT '执行时长(毫秒)',
    input_data JSON COMMENT '输入数据',
    output_data JSON COMMENT '输出数据',
    error_message TEXT COMMENT '错误信息',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    metadata JSON COMMENT '扩展字段',
    
    INDEX idx_tool_id (tool_id),
    INDEX idx_workflow_execution_id (workflow_execution_id),
    INDEX idx_executor_id (executor_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_duration (duration),
    INDEX idx_created_time (created_time),
    
    FOREIGN KEY (tool_id) REFERENCES mcp_tools(tool_id) ON DELETE CASCADE,
    FOREIGN KEY (workflow_execution_id) REFERENCES workflow_executions(execution_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工具执行记录表';

-- 创建用户表（如果不存在）
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(36) PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(100) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(255) NOT NULL UNIQUE COMMENT '邮箱',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    display_name VARCHAR(255) COMMENT '显示名称',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    role VARCHAR(50) NOT NULL DEFAULT 'USER' COMMENT '角色',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    preferences JSON COMMENT '用户偏好设置',
    last_login_time DATETIME COMMENT '最后登录时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建工作流权限表
CREATE TABLE IF NOT EXISTS workflow_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    workflow_id VARCHAR(36) NOT NULL COMMENT '工作流ID',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    permission_type VARCHAR(20) NOT NULL COMMENT '权限类型',
    granted_by VARCHAR(36) NOT NULL COMMENT '授权者ID',
    granted_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    expires_time DATETIME COMMENT '过期时间',
    
    UNIQUE KEY uk_workflow_user_permission (workflow_id, user_id, permission_type),
    INDEX idx_workflow_id (workflow_id),
    INDEX idx_user_id (user_id),
    INDEX idx_permission_type (permission_type),
    INDEX idx_granted_time (granted_time),
    
    FOREIGN KEY (workflow_id) REFERENCES workflow_definitions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (granted_by) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流权限表';

-- 创建系统配置表
CREATE TABLE IF NOT EXISTS system_configs (
    config_key VARCHAR(100) PRIMARY KEY COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    config_type VARCHAR(20) NOT NULL DEFAULT 'STRING' COMMENT '配置类型',
    description TEXT COMMENT '配置描述',
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否加密',
    is_system BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否系统配置',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_config_type (config_type),
    INDEX idx_is_system (is_system),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入默认系统配置
INSERT INTO system_configs (config_key, config_value, config_type, description, is_system) VALUES
('workflow.max_concurrent_executions', '100', 'INTEGER', '最大并发执行数', TRUE),
('workflow.default_timeout', '300', 'INTEGER', '默认超时时间(秒)', TRUE),
('workflow.cleanup_interval', '3600', 'INTEGER', '清理间隔(秒)', TRUE),
('workflow.history_retention_days', '30', 'INTEGER', '历史记录保留天数', TRUE),
('mcp.health_check_interval', '60', 'INTEGER', 'MCP工具健康检查间隔(秒)', TRUE),
('mcp.default_timeout', '30', 'INTEGER', 'MCP工具默认超时时间(秒)', TRUE),
('websocket.max_connections', '1000', 'INTEGER', 'WebSocket最大连接数', TRUE),
('websocket.heartbeat_interval', '30', 'INTEGER', 'WebSocket心跳间隔(秒)', TRUE)
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    updated_time = CURRENT_TIMESTAMP;

-- 创建默认管理员用户（密码: admin123）
INSERT INTO users (user_id, username, email, password_hash, display_name, role, status) VALUES
('admin-user-id-001', 'admin', 'admin@stockagent.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9tYjnZr0Nkn9Zei', '系统管理员', 'ADMIN', 'ACTIVE')
ON DUPLICATE KEY UPDATE 
    display_name = VALUES(display_name),
    updated_time = CURRENT_TIMESTAMP;

-- 创建演示用户（密码: demo123）
INSERT INTO users (user_id, username, email, password_hash, display_name, role, status) VALUES
('demo-user-id-001', 'demo', 'demo@stockagent.com', '$2a$10$8K1p/a0dUrziV4EIEEhzgOxuigfUeONyMasI5a2JZpps2GhKyf4Wa', '演示用户', 'USER', 'ACTIVE')
ON DUPLICATE KEY UPDATE 
    display_name = VALUES(display_name),
    updated_time = CURRENT_TIMESTAMP;

-- 创建索引优化查询性能
CREATE INDEX idx_workflow_executions_composite ON workflow_executions(workflow_id, status, start_time);
CREATE INDEX idx_workflow_executions_executor_status ON workflow_executions(executor_id, status, created_time);
CREATE INDEX idx_tool_executions_composite ON tool_executions(tool_id, status, start_time);
CREATE INDEX idx_workflow_definitions_composite ON workflow_definitions(creator_id, status, category, created_time);

-- 创建视图简化常用查询
CREATE OR REPLACE VIEW v_workflow_stats AS
SELECT 
    wd.id as workflow_id,
    wd.name as workflow_name,
    wd.creator_id,
    wd.category,
    wd.status,
    wd.usage_count,
    wd.avg_execution_time,
    wd.success_rate,
    wd.last_execution_time,
    COUNT(we.execution_id) as total_executions,
    SUM(CASE WHEN we.status = 'COMPLETED' THEN 1 ELSE 0 END) as successful_executions,
    SUM(CASE WHEN we.status = 'FAILED' THEN 1 ELSE 0 END) as failed_executions,
    SUM(CASE WHEN we.status IN ('RUNNING', 'PENDING', 'PAUSED') THEN 1 ELSE 0 END) as running_executions,
    AVG(we.duration) as avg_duration,
    MAX(we.duration) as max_duration,
    MIN(we.duration) as min_duration
FROM workflow_definitions wd
LEFT JOIN workflow_executions we ON wd.id = we.workflow_id
WHERE wd.deleted = FALSE
GROUP BY wd.id, wd.name, wd.creator_id, wd.category, wd.status, wd.usage_count, wd.avg_execution_time, wd.success_rate, wd.last_execution_time;

CREATE OR REPLACE VIEW v_tool_stats AS
SELECT 
    mt.tool_id,
    mt.tool_name,
    mt.category,
    mt.enabled,
    mt.health_status,
    mt.usage_count,
    mt.success_count,
    mt.failure_count,
    mt.avg_execution_time,
    mt.last_used_time,
    CASE 
        WHEN mt.usage_count > 0 THEN ROUND((mt.success_count * 100.0 / mt.usage_count), 2)
        ELSE 0
    END as success_rate,
    COUNT(te.execution_id) as recent_executions,
    SUM(CASE WHEN te.status = 'COMPLETED' THEN 1 ELSE 0 END) as recent_successful,
    SUM(CASE WHEN te.status = 'FAILED' THEN 1 ELSE 0 END) as recent_failed
FROM mcp_tools mt
LEFT JOIN tool_executions te ON mt.tool_id = te.tool_id AND te.created_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY mt.tool_id, mt.tool_name, mt.category, mt.enabled, mt.health_status, mt.usage_count, mt.success_count, mt.failure_count, mt.avg_execution_time, mt.last_used_time;

-- 创建存储过程用于清理历史数据
DELIMITER //
CREATE PROCEDURE CleanupWorkflowHistory(IN retention_days INT)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE cleanup_date DATETIME;
    
    SET cleanup_date = DATE_SUB(NOW(), INTERVAL retention_days DAY);
    
    -- 清理工具执行记录
    DELETE FROM tool_executions 
    WHERE created_time < cleanup_date 
    AND status IN ('COMPLETED', 'FAILED', 'CANCELLED');
    
    -- 清理工作流执行记录
    DELETE FROM workflow_executions 
    WHERE created_time < cleanup_date 
    AND status IN ('COMPLETED', 'FAILED', 'CANCELLED');
    
    SELECT ROW_COUNT() as cleaned_records;
END //
DELIMITER ;

-- 创建存储过程用于更新工作流统计信息
DELIMITER //
CREATE PROCEDURE UpdateWorkflowStats(IN workflow_id_param VARCHAR(36))
BEGIN
    DECLARE total_executions INT DEFAULT 0;
    DECLARE successful_executions INT DEFAULT 0;
    DECLARE avg_duration BIGINT DEFAULT 0;
    DECLARE success_rate_calc DECIMAL(5,4) DEFAULT 0;
    DECLARE last_exec_time DATETIME DEFAULT NULL;
    
    -- 计算统计信息
    SELECT 
        COUNT(*),
        SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END),
        AVG(CASE WHEN status = 'COMPLETED' THEN duration ELSE NULL END),
        MAX(start_time)
    INTO total_executions, successful_executions, avg_duration, last_exec_time
    FROM workflow_executions 
    WHERE workflow_id = workflow_id_param;
    
    -- 计算成功率
    IF total_executions > 0 THEN
        SET success_rate_calc = successful_executions / total_executions;
    END IF;
    
    -- 更新工作流定义表
    UPDATE workflow_definitions 
    SET 
        usage_count = total_executions,
        avg_execution_time = avg_duration,
        success_rate = success_rate_calc,
        last_execution_time = last_exec_time,
        updated_time = CURRENT_TIMESTAMP
    WHERE id = workflow_id_param;
    
END //
DELIMITER ;

-- 创建触发器自动更新统计信息
DELIMITER //
CREATE TRIGGER tr_workflow_execution_stats_update
AFTER UPDATE ON workflow_executions
FOR EACH ROW
BEGIN
    IF NEW.status != OLD.status AND NEW.status IN ('COMPLETED', 'FAILED', 'CANCELLED') THEN
        CALL UpdateWorkflowStats(NEW.workflow_id);
    END IF;
END //
DELIMITER ;

-- 创建事件调度器定期清理历史数据
CREATE EVENT IF NOT EXISTS evt_cleanup_workflow_history
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
  CALL CleanupWorkflowHistory(30);

-- 启用事件调度器
SET GLOBAL event_scheduler = ON;

COMMIT;