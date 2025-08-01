-- Stock Agent Genie Backend 测试数据初始化脚本
-- 用于单元测试和集成测试的基础数据

-- 插入测试用户
INSERT INTO users (id, username, password, email, full_name, roles, status, created_at, updated_at) VALUES
('test-admin-001', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9tYjKUznkXkWvQu', 'admin@test.com', '测试管理员', 'ADMIN,USER', 'ACTIVE', NOW(), NOW()),
('test-user-001', 'testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9tYjKUznkXkWvQu', 'testuser@test.com', '测试用户', 'USER', 'ACTIVE', NOW(), NOW()),
('test-user-002', 'analyst', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9tYjKUznkXkWvQu', 'analyst@test.com', '测试分析师', 'USER', 'ACTIVE', NOW(), NOW());

-- 插入测试工作流定义
INSERT INTO workflow_definitions (
    id, name, description, version, created_by, type, status, is_public, category, tags,
    nodes, connections, triggers, execution_config, input_parameters, output_parameters,
    usage_count, avg_execution_time, success_rate, last_executed_at, created_at, updated_at
) VALUES
(
    'test-workflow-001',
    '测试股票分析工作流',
    '用于测试的简单股票分析工作流',
    '1.0.0',
    'test-admin-001',
    'CUSTOM',
    'ACTIVE',
    true,
    'STOCK_ANALYSIS',
    'test,stock,analysis',
    '[
        {
            "id": "start-001",
            "type": "START",
            "name": "开始",
            "position": {"x": 100, "y": 100},
            "config": {}
        },
        {
            "id": "agent-001",
            "type": "AGENT",
            "name": "股票分析",
            "position": {"x": 300, "y": 100},
            "config": {
                "agentType": "STOCK_ANALYST",
                "prompt": "分析指定股票的基本面和技术面"
            }
        },
        {
            "id": "end-001",
            "type": "END",
            "name": "结束",
            "position": {"x": 500, "y": 100},
            "config": {}
        }
    ]',
    '[
        {
            "id": "conn-001",
            "sourceNodeId": "start-001",
            "targetNodeId": "agent-001"
        },
        {
            "id": "conn-002",
            "sourceNodeId": "agent-001",
            "targetNodeId": "end-001"
        }
    ]',
    '[]',
    '{
        "timeout": 300000,
        "maxRetries": 3,
        "retryDelay": 5000
    }',
    '[
        {
            "name": "symbol",
            "type": "STRING",
            "required": true,
            "description": "股票代码"
        }
    ]',
    '[
        {
            "name": "analysis",
            "type": "OBJECT",
            "description": "分析结果"
        }
    ]',
    0,
    0,
    0.0,
    NULL,
    NOW(),
    NOW()
),
(
    'test-workflow-002',
    '测试市场监控工作流',
    '用于测试的市场监控工作流',
    '1.0.0',
    'test-user-001',
    'CUSTOM',
    'ACTIVE',
    false,
    'MARKET_MONITORING',
    'test,market,monitoring',
    '[
        {
            "id": "start-002",
            "type": "START",
            "name": "开始",
            "position": {"x": 100, "y": 100},
            "config": {}
        },
        {
            "id": "http-001",
            "type": "HTTP",
            "name": "获取市场数据",
            "position": {"x": 300, "y": 100},
            "config": {
                "url": "https://api.example.com/market-data",
                "method": "GET"
            }
        },
        {
            "id": "condition-001",
            "type": "CONDITION",
            "name": "检查市场状态",
            "position": {"x": 500, "y": 100},
            "config": {
                "condition": "data.marketStatus === 'OPEN'"
            }
        },
        {
            "id": "notification-001",
            "type": "NOTIFICATION",
            "name": "发送通知",
            "position": {"x": 700, "y": 100},
            "config": {
                "type": "EMAIL",
                "template": "市场已开盘"
            }
        },
        {
            "id": "end-002",
            "type": "END",
            "name": "结束",
            "position": {"x": 900, "y": 100},
            "config": {}
        }
    ]',
    '[
        {
            "id": "conn-003",
            "sourceNodeId": "start-002",
            "targetNodeId": "http-001"
        },
        {
            "id": "conn-004",
            "sourceNodeId": "http-001",
            "targetNodeId": "condition-001"
        },
        {
            "id": "conn-005",
            "sourceNodeId": "condition-001",
            "targetNodeId": "notification-001",
            "condition": "true"
        },
        {
            "id": "conn-006",
            "sourceNodeId": "condition-001",
            "targetNodeId": "end-002",
            "condition": "false"
        },
        {
            "id": "conn-007",
            "sourceNodeId": "notification-001",
            "targetNodeId": "end-002"
        }
    ]',
    '[
        {
            "type": "SCHEDULE",
            "config": {
                "cron": "0 9 * * MON-FRI"
            }
        }
    ]',
    '{
        "timeout": 180000,
        "maxRetries": 2,
        "retryDelay": 3000
    }',
    '[]',
    '[
        {
            "name": "marketStatus",
            "type": "STRING",
            "description": "市场状态"
        },
        {
            "name": "notificationSent",
            "type": "BOOLEAN",
            "description": "是否发送通知"
        }
    ]',
    0,
    0,
    0.0,
    NULL,
    NOW(),
    NOW()
);

-- 插入测试工作流执行记录
INSERT INTO workflow_executions (
    id, workflow_id, workflow_version, executed_by, trigger_type, status,
    start_time, end_time, duration, input, output, context,
    node_executions, progress, error_message, warning_messages, logs,
    performance_stats, retry_count, max_retries, parent_execution_id,
    priority, timeout, is_cancellable, is_pausable, created_at, updated_at
) VALUES
(
    'test-execution-001',
    'test-workflow-001',
    '1.0.0',
    'test-user-001',
    'MANUAL',
    'COMPLETED',
    DATE_SUB(NOW(), INTERVAL 1 HOUR),
    DATE_SUB(NOW(), INTERVAL 59 MINUTE),
    60000,
    '{"symbol": "AAPL"}',
    '{"analysis": {"recommendation": "BUY", "confidence": 0.85}}',
    '{"sessionId": "test-session-001"}',
    '[
        {
            "nodeId": "start-001",
            "status": "COMPLETED",
            "startTime": "2024-01-01T10:00:00Z",
            "endTime": "2024-01-01T10:00:01Z",
            "output": {"started": true}
        },
        {
            "nodeId": "agent-001",
            "status": "COMPLETED",
            "startTime": "2024-01-01T10:00:01Z",
            "endTime": "2024-01-01T10:00:59Z",
            "output": {"analysis": {"recommendation": "BUY", "confidence": 0.85}}
        },
        {
            "nodeId": "end-001",
            "status": "COMPLETED",
            "startTime": "2024-01-01T10:00:59Z",
            "endTime": "2024-01-01T10:01:00Z",
            "output": {"completed": true}
        }
    ]',
    100,
    NULL,
    '[]',
    '[
        {
            "timestamp": "2024-01-01T10:00:00Z",
            "level": "INFO",
            "message": "工作流执行开始"
        },
        {
            "timestamp": "2024-01-01T10:01:00Z",
            "level": "INFO",
            "message": "工作流执行完成"
        }
    ]',
    '{
        "totalNodes": 3,
        "executedNodes": 3,
        "avgNodeExecutionTime": 20000,
        "memoryUsage": 128,
        "cpuUsage": 15
    }',
    0,
    3,
    NULL,
    'NORMAL',
    300000,
    true,
    true,
    DATE_SUB(NOW(), INTERVAL 1 HOUR),
    DATE_SUB(NOW(), INTERVAL 59 MINUTE)
),
(
    'test-execution-002',
    'test-workflow-001',
    '1.0.0',
    'test-user-002',
    'MANUAL',
    'FAILED',
    DATE_SUB(NOW(), INTERVAL 30 MINUTE),
    DATE_SUB(NOW(), INTERVAL 29 MINUTE),
    30000,
    '{"symbol": "INVALID"}',
    NULL,
    '{"sessionId": "test-session-002"}',
    '[
        {
            "nodeId": "start-001",
            "status": "COMPLETED",
            "startTime": "2024-01-01T10:30:00Z",
            "endTime": "2024-01-01T10:30:01Z",
            "output": {"started": true}
        },
        {
            "nodeId": "agent-001",
            "status": "FAILED",
            "startTime": "2024-01-01T10:30:01Z",
            "endTime": "2024-01-01T10:30:30Z",
            "error": "Invalid stock symbol: INVALID"
        }
    ]',
    66,
    'Invalid stock symbol: INVALID',
    '[]',
    '[
        {
            "timestamp": "2024-01-01T10:30:00Z",
            "level": "INFO",
            "message": "工作流执行开始"
        },
        {
            "timestamp": "2024-01-01T10:30:30Z",
            "level": "ERROR",
            "message": "工作流执行失败: Invalid stock symbol"
        }
    ]',
    '{
        "totalNodes": 3,
        "executedNodes": 2,
        "avgNodeExecutionTime": 15000,
        "memoryUsage": 96,
        "cpuUsage": 12
    }',
    1,
    3,
    NULL,
    'NORMAL',
    300000,
    true,
    true,
    DATE_SUB(NOW(), INTERVAL 30 MINUTE),
    DATE_SUB(NOW(), INTERVAL 29 MINUTE)
);

-- 插入测试MCP工具
INSERT INTO mcp_tools (
    id, name, description, category, version, status, config,
    input_schema, output_schema, timeout, max_retries, health_check_url,
    usage_count, avg_execution_time, success_rate, last_used_at,
    created_at, updated_at
) VALUES
(
    'test-tool-001',
    'stock-data-fetcher',
    '股票数据获取工具',
    'DATA_FETCHING',
    '1.0.0',
    'ACTIVE',
    '{
        "endpoint": "http://localhost:8082/stock-data",
        "apiKey": "test-api-key",
        "timeout": 10000
    }',
    '{
        "type": "object",
        "properties": {
            "symbol": {"type": "string", "description": "股票代码"},
            "period": {"type": "string", "description": "时间周期"}
        },
        "required": ["symbol"]
    }',
    '{
        "type": "object",
        "properties": {
            "data": {"type": "object", "description": "股票数据"},
            "timestamp": {"type": "string", "description": "数据时间戳"}
        }
    }',
    10000,
    3,
    'http://localhost:8082/health',
    0,
    0,
    0.0,
    NULL,
    NOW(),
    NOW()
),
(
    'test-tool-002',
    'news-fetcher',
    '新闻数据获取工具',
    'DATA_FETCHING',
    '1.0.0',
    'ACTIVE',
    '{
        "endpoint": "http://localhost:8083/news",
        "apiKey": "test-news-api-key",
        "timeout": 15000
    }',
    '{
        "type": "object",
        "properties": {
            "query": {"type": "string", "description": "搜索关键词"},
            "limit": {"type": "integer", "description": "结果数量限制"}
        },
        "required": ["query"]
    }',
    '{
        "type": "object",
        "properties": {
            "articles": {"type": "array", "description": "新闻文章列表"},
            "total": {"type": "integer", "description": "总数量"}
        }
    }',
    15000,
    3,
    'http://localhost:8083/health',
    0,
    0,
    0.0,
    NULL,
    NOW(),
    NOW()
);

-- 插入测试工具执行记录
INSERT INTO tool_executions (
    id, tool_name, executed_by, status, start_time, end_time, duration,
    input, output, error_message, retry_count, max_retries,
    created_at, updated_at
) VALUES
(
    'test-tool-exec-001',
    'stock-data-fetcher',
    'test-user-001',
    'COMPLETED',
    DATE_SUB(NOW(), INTERVAL 15 MINUTE),
    DATE_SUB(NOW(), INTERVAL 14 MINUTE),
    5000,
    '{"symbol": "AAPL", "period": "1d"}',
    '{"data": {"price": 150.25, "volume": 1000000}, "timestamp": "2024-01-01T10:45:00Z"}',
    NULL,
    0,
    3,
    DATE_SUB(NOW(), INTERVAL 15 MINUTE),
    DATE_SUB(NOW(), INTERVAL 14 MINUTE)
),
(
    'test-tool-exec-002',
    'news-fetcher',
    'test-user-002',
    'FAILED',
    DATE_SUB(NOW(), INTERVAL 5 MINUTE),
    DATE_SUB(NOW(), INTERVAL 4 MINUTE),
    8000,
    '{"query": "AAPL stock", "limit": 10}',
    NULL,
    'API rate limit exceeded',
    2,
    3,
    DATE_SUB(NOW(), INTERVAL 5 MINUTE),
    DATE_SUB(NOW(), INTERVAL 4 MINUTE)
);

-- 插入测试工作流权限
INSERT INTO workflow_permissions (
    id, workflow_id, user_id, permission_type, granted_by, granted_at
) VALUES
('test-perm-001', 'test-workflow-001', 'test-user-001', 'READ', 'test-admin-001', NOW()),
('test-perm-002', 'test-workflow-001', 'test-user-001', 'EXECUTE', 'test-admin-001', NOW()),
('test-perm-003', 'test-workflow-002', 'test-user-002', 'READ', 'test-user-001', NOW());

-- 插入测试系统配置
INSERT INTO system_configs (
    config_key, config_value, description, config_type, is_encrypted, created_at, updated_at
) VALUES
('test.max.concurrent.executions', '5', '测试环境最大并发执行数', 'INTEGER', false, NOW(), NOW()),
('test.default.timeout', '60000', '测试环境默认超时时间', 'LONG', false, NOW(), NOW()),
('test.enable.debug.logging', 'true', '测试环境启用调试日志', 'BOOLEAN', false, NOW(), NOW()),
('test.mock.external.apis', 'true', '测试环境模拟外部API', 'BOOLEAN', false, NOW(), NOW());

-- 更新工作流定义统计信息
UPDATE workflow_definitions SET
    usage_count = (
        SELECT COUNT(*) FROM workflow_executions 
        WHERE workflow_executions.workflow_id = workflow_definitions.id
    ),
    avg_execution_time = (
        SELECT AVG(duration) FROM workflow_executions 
        WHERE workflow_executions.workflow_id = workflow_definitions.id 
        AND workflow_executions.status = 'COMPLETED'
    ),
    success_rate = (
        SELECT 
            CASE 
                WHEN COUNT(*) = 0 THEN 0.0
                ELSE CAST(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS DECIMAL(5,2)) / COUNT(*)
            END
        FROM workflow_executions 
        WHERE workflow_executions.workflow_id = workflow_definitions.id
    ),
    last_executed_at = (
        SELECT MAX(start_time) FROM workflow_executions 
        WHERE workflow_executions.workflow_id = workflow_definitions.id
    ),
    updated_at = NOW();

-- 更新MCP工具统计信息
UPDATE mcp_tools SET
    usage_count = (
        SELECT COUNT(*) FROM tool_executions 
        WHERE tool_executions.tool_name = mcp_tools.name
    ),
    avg_execution_time = (
        SELECT AVG(duration) FROM tool_executions 
        WHERE tool_executions.tool_name = mcp_tools.name 
        AND tool_executions.status = 'COMPLETED'
    ),
    success_rate = (
        SELECT 
            CASE 
                WHEN COUNT(*) = 0 THEN 0.0
                ELSE CAST(SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS DECIMAL(5,2)) / COUNT(*)
            END
        FROM tool_executions 
        WHERE tool_executions.tool_name = mcp_tools.name
    ),
    last_used_at = (
        SELECT MAX(start_time) FROM tool_executions 
        WHERE tool_executions.tool_name = mcp_tools.name
    ),
    updated_at = NOW();