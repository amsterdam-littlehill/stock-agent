-- =====================================================
-- Stock-Agent 数据库完整迁移脚本
-- 从 Go-Stock SQLite 迁移到 MySQL/PostgreSQL
-- =====================================================

-- 设置字符集和时区
SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- =====================================================
-- 1. 用户和权限管理表
-- =====================================================

-- 用户表
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100),
    `avatar_url` VARCHAR(500),
    `phone` VARCHAR(20),
    `status` ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    `role` ENUM('USER', 'ADMIN', 'ANALYST') DEFAULT 'USER',
    `preferences` JSON,
    `last_login_at` TIMESTAMP NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户会话表
CREATE TABLE IF NOT EXISTS `user_sessions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `session_token` VARCHAR(255) NOT NULL UNIQUE,
    `device_info` JSON,
    `ip_address` VARCHAR(45),
    `expires_at` TIMESTAMP NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    INDEX `idx_session_token` (`session_token`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 2. 股票基础数据表
-- =====================================================

-- 股票信息表（对应Go-Stock的stock表）
CREATE TABLE IF NOT EXISTS `stocks` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `code` VARCHAR(20) NOT NULL UNIQUE COMMENT '股票代码',
    `name` VARCHAR(100) NOT NULL COMMENT '股票名称',
    `exchange` VARCHAR(20) NOT NULL COMMENT '交易所',
    `market` VARCHAR(20) NOT NULL COMMENT '市场类型',
    `industry` VARCHAR(50) COMMENT '行业',
    `sector` VARCHAR(50) COMMENT '板块',
    `market_cap` DECIMAL(20,2) COMMENT '市值',
    `pe_ratio` DECIMAL(10,2) COMMENT 'PE比率',
    `pb_ratio` DECIMAL(10,2) COMMENT 'PB比率',
    `eps` DECIMAL(10,4) COMMENT '每股收益',
    `roe` DECIMAL(10,4) COMMENT 'ROE',
    `dividend_yield` DECIMAL(10,4) COMMENT '股息率',
    `description` TEXT COMMENT '公司描述',
    `listing_date` DATE COMMENT '上市日期',
    `status` ENUM('ACTIVE', 'SUSPENDED', 'DELISTED') DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_code` (`code`),
    INDEX `idx_exchange` (`exchange`),
    INDEX `idx_industry` (`industry`),
    INDEX `idx_market_cap` (`market_cap`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 股票分组表（对应Go-Stock的group表）
CREATE TABLE IF NOT EXISTS `stock_groups` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `name` VARCHAR(100) NOT NULL COMMENT '分组名称',
    `description` TEXT COMMENT '分组描述',
    `color` VARCHAR(7) DEFAULT '#1890ff' COMMENT '分组颜色',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户关注股票表（对应Go-Stock的follow表）
CREATE TABLE IF NOT EXISTS `user_stock_follows` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `stock_id` BIGINT NOT NULL,
    `group_id` BIGINT NULL,
    `notes` TEXT COMMENT '备注',
    `alert_enabled` BOOLEAN DEFAULT FALSE COMMENT '是否启用提醒',
    `alert_conditions` JSON COMMENT '提醒条件',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`stock_id`) REFERENCES `stocks`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`group_id`) REFERENCES `stock_groups`(`id`) ON DELETE SET NULL,
    UNIQUE KEY `uk_user_stock` (`user_id`, `stock_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_stock_id` (`stock_id`),
    INDEX `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 3. 智能体管理表
-- =====================================================

-- 智能体配置表
CREATE TABLE IF NOT EXISTS `agents` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '智能体名称',
    `type` VARCHAR(50) NOT NULL COMMENT '智能体类型',
    `description` TEXT COMMENT '描述',
    `class_name` VARCHAR(200) NOT NULL COMMENT 'Java类名',
    `config` JSON COMMENT '配置参数',
    `system_prompt` TEXT COMMENT '系统提示词',
    `tools` JSON COMMENT '可用工具列表',
    `max_steps` INT DEFAULT 10 COMMENT '最大执行步数',
    `timeout_seconds` INT DEFAULT 300 COMMENT '超时时间',
    `status` ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE') DEFAULT 'ACTIVE',
    `version` VARCHAR(20) DEFAULT '1.0.0',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_type` (`type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 智能体实例表
CREATE TABLE IF NOT EXISTS `agent_instances` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `agent_id` BIGINT NOT NULL,
    `instance_id` VARCHAR(100) NOT NULL UNIQUE COMMENT '实例ID',
    `status` ENUM('IDLE', 'RUNNING', 'COMPLETED', 'ERROR', 'TIMEOUT') DEFAULT 'IDLE',
    `current_task` VARCHAR(500) COMMENT '当前任务',
    `progress` INT DEFAULT 0 COMMENT '进度百分比',
    `metrics` JSON COMMENT '性能指标',
    `error_message` TEXT COMMENT '错误信息',
    `started_at` TIMESTAMP NULL,
    `completed_at` TIMESTAMP NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`agent_id`) REFERENCES `agents`(`id`) ON DELETE CASCADE,
    INDEX `idx_agent_id` (`agent_id`),
    INDEX `idx_instance_id` (`instance_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. 分析任务和结果表
-- =====================================================

-- 分析任务表
CREATE TABLE IF NOT EXISTS `analysis_tasks` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `request_id` VARCHAR(100) NOT NULL UNIQUE COMMENT '请求ID',
    `user_id` BIGINT NULL,
    `stock_id` BIGINT NOT NULL,
    `analysis_type` VARCHAR(50) NOT NULL COMMENT '分析类型',
    `depth` ENUM('summary', 'normal', 'detailed') DEFAULT 'normal',
    `timeframe` VARCHAR(10) DEFAULT '1d',
    `parameters` JSON COMMENT '分析参数',
    `status` ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED') DEFAULT 'PENDING',
    `priority` ENUM('low', 'normal', 'high') DEFAULT 'normal',
    `assigned_agents` JSON COMMENT '分配的智能体',
    `progress` INT DEFAULT 0 COMMENT '总体进度',
    `estimated_time` INT COMMENT '预估时间(秒)',
    `actual_time` INT COMMENT '实际耗时(秒)',
    `error_message` TEXT COMMENT '错误信息',
    `callback_url` VARCHAR(500) COMMENT '回调URL',
    `tags` JSON COMMENT '标签',
    `started_at` TIMESTAMP NULL,
    `completed_at` TIMESTAMP NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`stock_id`) REFERENCES `stocks`(`id`) ON DELETE CASCADE,
    INDEX `idx_request_id` (`request_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_stock_id` (`stock_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 分析结果表
CREATE TABLE IF NOT EXISTS `analysis_results` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_id` BIGINT NOT NULL,
    `agent_id` BIGINT NULL,
    `result_type` VARCHAR(50) NOT NULL COMMENT '结果类型',
    `conclusion` TEXT COMMENT '分析结论',
    `recommendation` VARCHAR(50) COMMENT '投资建议',
    `confidence_score` DECIMAL(5,4) COMMENT '置信度',
    `risk_level` VARCHAR(20) COMMENT '风险等级',
    `target_price` DECIMAL(10,2) COMMENT '目标价',
    `stop_loss_price` DECIMAL(10,2) COMMENT '止损价',
    `key_points` JSON COMMENT '关键要点',
    `warnings` JSON COMMENT '风险提示',
    `technical_indicators` JSON COMMENT '技术指标',
    `fundamental_data` JSON COMMENT '基本面数据',
    `sentiment_data` JSON COMMENT '情绪数据',
    `support_levels` JSON COMMENT '支撑位',
    `resistance_levels` JSON COMMENT '阻力位',
    `industry_comparison` JSON COMMENT '行业对比',
    `historical_performance` JSON COMMENT '历史表现',
    `related_news` JSON COMMENT '相关新闻',
    `data_sources` JSON COMMENT '数据来源',
    `raw_data` JSON COMMENT '原始数据',
    `quality_score` DECIMAL(5,4) COMMENT '质量评分',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`task_id`) REFERENCES `analysis_tasks`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`agent_id`) REFERENCES `agents`(`id`) ON DELETE SET NULL,
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_agent_id` (`agent_id`),
    INDEX `idx_result_type` (`result_type`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 5. 多智能体协作表
-- =====================================================

-- 智能体会话表
CREATE TABLE IF NOT EXISTS `agent_sessions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `session_id` VARCHAR(100) NOT NULL UNIQUE COMMENT '会话ID',
    `task_id` BIGINT NOT NULL,
    `coordinator_agent_id` BIGINT NULL COMMENT '协调者智能体',
    `participant_agents` JSON COMMENT '参与的智能体',
    `session_type` VARCHAR(50) DEFAULT 'ANALYSIS' COMMENT '会话类型',
    `status` ENUM('ACTIVE', 'COMPLETED', 'FAILED', 'CANCELLED') DEFAULT 'ACTIVE',
    `context` JSON COMMENT '会话上下文',
    `messages` JSON COMMENT '消息历史',
    `results` JSON COMMENT '协作结果',
    `started_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `ended_at` TIMESTAMP NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`task_id`) REFERENCES `analysis_tasks`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`coordinator_agent_id`) REFERENCES `agents`(`id`) ON DELETE SET NULL,
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 智能体消息表
CREATE TABLE IF NOT EXISTS `agent_messages` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `session_id` BIGINT NOT NULL,
    `from_agent_id` BIGINT NULL,
    `to_agent_id` BIGINT NULL,
    `message_type` VARCHAR(50) NOT NULL COMMENT '消息类型',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `metadata` JSON COMMENT '元数据',
    `status` ENUM('SENT', 'DELIVERED', 'PROCESSED', 'FAILED') DEFAULT 'SENT',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`session_id`) REFERENCES `agent_sessions`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`from_agent_id`) REFERENCES `agents`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`to_agent_id`) REFERENCES `agents`(`id`) ON DELETE SET NULL,
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_from_agent` (`from_agent_id`),
    INDEX `idx_to_agent` (`to_agent_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 6. 市场数据表
-- =====================================================

-- 实时价格表
CREATE TABLE IF NOT EXISTS `stock_prices` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `stock_id` BIGINT NOT NULL,
    `price` DECIMAL(10,4) NOT NULL COMMENT '当前价格',
    `open_price` DECIMAL(10,4) COMMENT '开盘价',
    `high_price` DECIMAL(10,4) COMMENT '最高价',
    `low_price` DECIMAL(10,4) COMMENT '最低价',
    `pre_close` DECIMAL(10,4) COMMENT '前收盘价',
    `volume` BIGINT COMMENT '成交量',
    `amount` DECIMAL(20,2) COMMENT '成交额',
    `change_amount` DECIMAL(10,4) COMMENT '涨跌额',
    `change_percent` DECIMAL(8,4) COMMENT '涨跌幅',
    `market_status` VARCHAR(20) COMMENT '市场状态',
    `timestamp` TIMESTAMP NOT NULL COMMENT '数据时间',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`stock_id`) REFERENCES `stocks`(`id`) ON DELETE CASCADE,
    INDEX `idx_stock_id` (`stock_id`),
    INDEX `idx_timestamp` (`timestamp`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- K线数据表
CREATE TABLE IF NOT EXISTS `stock_klines` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `stock_id` BIGINT NOT NULL,
    `timeframe` VARCHAR(10) NOT NULL COMMENT '时间周期',
    `timestamp` TIMESTAMP NOT NULL COMMENT '时间戳',
    `open_price` DECIMAL(10,4) NOT NULL COMMENT '开盘价',
    `high_price` DECIMAL(10,4) NOT NULL COMMENT '最高价',
    `low_price` DECIMAL(10,4) NOT NULL COMMENT '最低价',
    `close_price` DECIMAL(10,4) NOT NULL COMMENT '收盘价',
    `volume` BIGINT NOT NULL COMMENT '成交量',
    `amount` DECIMAL(20,2) COMMENT '成交额',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`stock_id`) REFERENCES `stocks`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_stock_timeframe_timestamp` (`stock_id`, `timeframe`, `timestamp`),
    INDEX `idx_stock_timeframe` (`stock_id`, `timeframe`),
    INDEX `idx_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 7. 系统配置和日志表
-- =====================================================

-- 系统配置表（对应Go-Stock的config表）
CREATE TABLE IF NOT EXISTS `system_configs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` VARCHAR(20) DEFAULT 'STRING' COMMENT '配置类型',
    `description` TEXT COMMENT '描述',
    `is_encrypted` BOOLEAN DEFAULT FALSE COMMENT '是否加密',
    `is_public` BOOLEAN DEFAULT FALSE COMMENT '是否公开',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_config_key` (`config_key`),
    INDEX `idx_is_public` (`is_public`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NULL,
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `resource_type` VARCHAR(50) COMMENT '资源类型',
    `resource_id` VARCHAR(100) COMMENT '资源ID',
    `operation_desc` TEXT COMMENT '操作描述',
    `request_data` JSON COMMENT '请求数据',
    `response_data` JSON COMMENT '响应数据',
    `ip_address` VARCHAR(45) COMMENT 'IP地址',
    `user_agent` TEXT COMMENT '用户代理',
    `status` VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '状态',
    `error_message` TEXT COMMENT '错误信息',
    `execution_time` INT COMMENT '执行时间(毫秒)',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 系统监控表
CREATE TABLE IF NOT EXISTS `system_metrics` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `metric_name` VARCHAR(100) NOT NULL COMMENT '指标名称',
    `metric_value` DECIMAL(20,6) NOT NULL COMMENT '指标值',
    `metric_unit` VARCHAR(20) COMMENT '单位',
    `tags` JSON COMMENT '标签',
    `timestamp` TIMESTAMP NOT NULL COMMENT '时间戳',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_metric_name` (`metric_name`),
    INDEX `idx_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 8. 初始化数据
-- =====================================================

-- 插入默认智能体配置
INSERT INTO `agents` (`name`, `type`, `description`, `class_name`, `config`, `system_prompt`, `tools`) VALUES
('技术分析师', 'TECHNICAL_ANALYST', '专注于股票技术分析的智能体', 'com.jd.genie.agent.agent.stock.TechnicalAnalystAgent', 
 '{"confidence_threshold": 0.7, "max_analysis_days": 252}', 
 '你是一位专业的技术分析师，擅长分析股票的技术指标、图形模式和价格趋势。', 
 '["GetStockKLine", "CalculateTechnicalIndicators", "IdentifyPatterns"]'),

('基本面分析师', 'FUNDAMENTAL_ANALYST', '专注于股票基本面分析的智能体', 'com.jd.genie.agent.agent.stock.FundamentalAnalystAgent', 
 '{"confidence_threshold": 0.7, "valuation_methods": ["DCF", "PEG", "Graham"]}', 
 '你是一位专业的基本面分析师，擅长分析公司财务数据、估值模型和行业对比。', 
 '["GetFinancialData", "CalculateValuation", "IndustryComparison"]'),

('多智能体协调器', 'COORDINATOR', '协调多个分析师智能体的工作', 'com.jd.genie.agent.agent.stock.StockMultiAgentCoordinator', 
 '{"max_agents": 5, "consensus_threshold": 0.6}', 
 '你是一位智能体协调器，负责管理和协调多个分析师的工作，整合分析结果。', 
 '["CoordinateAgents", "AggregateResults", "GenerateReport"]');

-- 插入默认系统配置
INSERT INTO `system_configs` (`config_key`, `config_value`, `config_type`, `description`, `is_public`) VALUES
('system.name', 'Stock-Agent', 'STRING', '系统名称', TRUE),
('system.version', '1.0.0', 'STRING', '系统版本', TRUE),
('analysis.default_depth', 'normal', 'STRING', '默认分析深度', TRUE),
('analysis.default_timeframe', '1d', 'STRING', '默认时间周期', TRUE),
('analysis.max_concurrent_tasks', '10', 'INTEGER', '最大并发分析任务数', FALSE),
('websocket.enabled', 'true', 'BOOLEAN', '是否启用WebSocket', TRUE),
('cache.ttl_seconds', '3600', 'INTEGER', '缓存过期时间(秒)', FALSE);

-- 插入示例股票数据
INSERT INTO `stocks` (`code`, `name`, `exchange`, `market`, `industry`, `sector`, `market_cap`, `pe_ratio`, `pb_ratio`) VALUES
('000001', '平安银行', 'SZSE', 'A股', '银行', '金融', 500000000000, 5.2, 0.8),
('000002', '万科A', 'SZSE', 'A股', '房地产', '房地产', 300000000000, 8.5, 1.2),
('600000', '浦发银行', 'SSE', 'A股', '银行', '金融', 400000000000, 4.8, 0.7),
('600036', '招商银行', 'SSE', 'A股', '银行', '金融', 800000000000, 6.2, 1.1),
('600519', '贵州茅台', 'SSE', 'A股', '白酒', '消费', 2500000000000, 35.6, 12.8),
('000858', '五粮液', 'SZSE', 'A股', '白酒', '消费', 800000000000, 28.9, 8.5),
('00700.HK', '腾讯控股', 'HKEX', '港股', '互联网', '科技', 3500000000000, 18.5, 3.2),
('09988.HK', '阿里巴巴', 'HKEX', '港股', '电商', '科技', 2000000000000, 12.8, 2.1),
('AAPL', '苹果', 'NASDAQ', '美股', '消费电子', '科技', 3000000000000, 28.5, 45.2),
('TSLA', '特斯拉', 'NASDAQ', '美股', '电动汽车', '汽车', 800000000000, 65.2, 12.8);

-- =====================================================
-- 9. 创建视图
-- =====================================================

-- 分析任务概览视图
CREATE VIEW `v_analysis_overview` AS
SELECT 
    t.id,
    t.request_id,
    u.username,
    s.code as stock_code,
    s.name as stock_name,
    t.analysis_type,
    t.status,
    t.progress,
    t.created_at,
    t.completed_at,
    t.actual_time,
    r.recommendation,
    r.confidence_score,
    r.risk_level
FROM analysis_tasks t
LEFT JOIN users u ON t.user_id = u.id
LEFT JOIN stocks s ON t.stock_id = s.id
LEFT JOIN analysis_results r ON t.id = r.task_id AND r.result_type = 'FINAL';

-- 用户关注股票视图
CREATE VIEW `v_user_follows` AS
SELECT 
    f.id,
    u.username,
    s.code as stock_code,
    s.name as stock_name,
    s.exchange,
    g.name as group_name,
    f.notes,
    f.alert_enabled,
    f.created_at
FROM user_stock_follows f
JOIN users u ON f.user_id = u.id
JOIN stocks s ON f.stock_id = s.id
LEFT JOIN stock_groups g ON f.group_id = g.id;

-- 智能体性能统计视图
CREATE VIEW `v_agent_performance` AS
SELECT 
    a.id,
    a.name,
    a.type,
    COUNT(r.id) as total_analyses,
    AVG(r.confidence_score) as avg_confidence,
    AVG(t.actual_time) as avg_processing_time,
    COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) as successful_analyses,
    COUNT(CASE WHEN t.status = 'FAILED' THEN 1 END) as failed_analyses
FROM agents a
LEFT JOIN analysis_results r ON a.id = r.agent_id
LEFT JOIN analysis_tasks t ON r.task_id = t.id
GROUP BY a.id, a.name, a.type;

-- =====================================================
-- 10. 创建存储过程
-- =====================================================

DELIMITER //

-- 清理过期数据的存储过程
CREATE PROCEDURE CleanupExpiredData()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE cleanup_date TIMESTAMP DEFAULT DATE_SUB(NOW(), INTERVAL 30 DAY);
    
    -- 清理过期的分析任务和结果
    DELETE FROM analysis_results WHERE created_at < cleanup_date;
    DELETE FROM analysis_tasks WHERE created_at < cleanup_date AND status IN ('COMPLETED', 'FAILED', 'CANCELLED');
    
    -- 清理过期的智能体会话
    DELETE FROM agent_messages WHERE created_at < cleanup_date;
    DELETE FROM agent_sessions WHERE created_at < cleanup_date AND status IN ('COMPLETED', 'FAILED', 'CANCELLED');
    
    -- 清理过期的操作日志
    DELETE FROM operation_logs WHERE created_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
    
    -- 清理过期的系统监控数据
    DELETE FROM system_metrics WHERE created_at < DATE_SUB(NOW(), INTERVAL 7 DAY);
    
    -- 清理过期的用户会话
    DELETE FROM user_sessions WHERE expires_at < NOW();
    
END //

-- 获取股票分析统计的存储过程
CREATE PROCEDURE GetStockAnalysisStats(IN stock_code_param VARCHAR(20))
BEGIN
    SELECT 
        COUNT(*) as total_analyses,
        COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) as completed_analyses,
        COUNT(CASE WHEN t.status = 'FAILED' THEN 1 END) as failed_analyses,
        AVG(t.actual_time) as avg_processing_time,
        AVG(r.confidence_score) as avg_confidence,
        COUNT(DISTINCT t.user_id) as unique_users,
        MAX(t.created_at) as last_analysis_time
    FROM analysis_tasks t
    JOIN stocks s ON t.stock_id = s.id
    LEFT JOIN analysis_results r ON t.id = r.task_id AND r.result_type = 'FINAL'
    WHERE s.code = stock_code_param;
END //

DELIMITER ;

-- =====================================================
-- 11. 创建触发器
-- =====================================================

DELIMITER //

-- 分析任务状态更新触发器
CREATE TRIGGER tr_analysis_task_status_update
AFTER UPDATE ON analysis_tasks
FOR EACH ROW
BEGIN
    -- 当任务完成时，记录完成时间
    IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN
        UPDATE analysis_tasks 
        SET completed_at = NOW(), 
            actual_time = TIMESTAMPDIFF(SECOND, started_at, NOW())
        WHERE id = NEW.id;
    END IF;
    
    -- 当任务开始时，记录开始时间
    IF NEW.status = 'PROCESSING' AND OLD.status = 'PENDING' THEN
        UPDATE analysis_tasks 
        SET started_at = NOW()
        WHERE id = NEW.id;
    END IF;
END //

-- 用户关注股票触发器
CREATE TRIGGER tr_user_follow_log
AFTER INSERT ON user_stock_follows
FOR EACH ROW
BEGIN
    INSERT INTO operation_logs (user_id, operation_type, resource_type, resource_id, operation_desc)
    VALUES (NEW.user_id, 'FOLLOW_STOCK', 'STOCK', NEW.stock_id, CONCAT('用户关注股票: ', NEW.stock_id));
END //

DELIMITER ;

-- =====================================================
-- 12. 创建索引优化
-- =====================================================

-- 复合索引优化查询性能
CREATE INDEX idx_analysis_tasks_user_status ON analysis_tasks(user_id, status, created_at);
CREATE INDEX idx_analysis_results_task_type ON analysis_results(task_id, result_type);
CREATE INDEX idx_stock_prices_stock_timestamp ON stock_prices(stock_id, timestamp DESC);
CREATE INDEX idx_operation_logs_user_type_time ON operation_logs(user_id, operation_type, created_at);

-- =====================================================
-- 完成
-- =====================================================

-- 显示创建的表
SHOW TABLES;

-- 显示数据库状态
SELECT 
    'Database migration completed successfully!' as status,
    NOW() as completion_time,
    (
        SELECT COUNT(*) 
        FROM information_schema.tables 
        WHERE table_schema = DATABASE()
    ) as total_tables;

COMMIT;