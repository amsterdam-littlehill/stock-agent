package com.jd.genie.agent.mcp;

import com.jd.genie.agent.dto.tool.McpToolInfo;
import com.jd.genie.agent.dto.tool.ToolResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * MCP工具集成服务
 * 统一管理和调用各种MCP (Model Context Protocol) 工具
 * 
 * 功能：
 * - MCP工具注册和发现
 * - 工具调用和结果处理
 * - 异步执行和超时控制
 * - 工具状态监控和健康检查
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Service
public class McpIntegrationService {
    
    // 已注册的MCP工具
    private final Map<String, McpToolInfo> registeredTools = new ConcurrentHashMap<>();
    
    // 工具调用统计
    private final Map<String, ToolStats> toolStats = new ConcurrentHashMap<>();
    
    // 工具健康状态
    private final Map<String, ToolHealth> toolHealth = new ConcurrentHashMap<>();
    
    /**
     * 注册MCP工具
     * 
     * @param toolInfo MCP工具信息
     */
    public void registerTool(McpToolInfo toolInfo) {
        if (toolInfo == null || toolInfo.getName() == null) {
            throw new IllegalArgumentException("工具信息不能为空");
        }
        
        String toolName = toolInfo.getName();
        registeredTools.put(toolName, toolInfo);
        toolStats.put(toolName, new ToolStats());
        toolHealth.put(toolName, new ToolHealth(toolName, HealthStatus.UNKNOWN));
        
        log.info("MCP工具注册成功: {}", toolName);
    }
    
    /**
     * 批量注册MCP工具
     * 
     * @param tools MCP工具列表
     */
    public void registerTools(List<McpToolInfo> tools) {
        if (tools == null || tools.isEmpty()) {
            log.warn("工具列表为空，跳过注册");
            return;
        }
        
        for (McpToolInfo tool : tools) {
            try {
                registerTool(tool);
            } catch (Exception e) {
                log.error("注册工具失败: {}", tool != null ? tool.getName() : "unknown", e);
            }
        }
        
        log.info("批量注册MCP工具完成，成功: {}, 总数: {}", registeredTools.size(), tools.size());
    }
    
    /**
     * 调用MCP工具
     * 
     * @param toolName 工具名称
     * @param parameters 调用参数
     * @return 工具执行结果
     */
    public CompletableFuture<ToolResult> callTool(String toolName, Map<String, Object> parameters) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 验证工具是否存在
                McpToolInfo toolInfo = registeredTools.get(toolName);
                if (toolInfo == null) {
                    throw new McpToolException("工具不存在: " + toolName);
                }
                
                // 检查工具健康状态
                ToolHealth health = toolHealth.get(toolName);
                if (health != null && health.getStatus() == HealthStatus.UNHEALTHY) {
                    throw new McpToolException("工具不健康，无法调用: " + toolName);
                }
                
                // 记录调用开始
                ToolStats stats = toolStats.get(toolName);
                stats.recordCallStart();
                
                long startTime = System.currentTimeMillis();
                
                // 执行工具调用
                ToolResult result = executeToolCall(toolInfo, parameters);
                
                long executionTime = System.currentTimeMillis() - startTime;
                
                // 记录调用完成
                stats.recordCallComplete(executionTime);
                
                // 更新健康状态
                updateToolHealth(toolName, HealthStatus.HEALTHY, null);
                
                log.debug("MCP工具调用成功: {}, 耗时: {}ms", toolName, executionTime);
                return result;
                
            } catch (Exception e) {
                // 记录调用失败
                ToolStats stats = toolStats.get(toolName);
                if (stats != null) {
                    stats.recordCallError();
                }
                
                // 更新健康状态
                updateToolHealth(toolName, HealthStatus.UNHEALTHY, e.getMessage());
                
                log.error("MCP工具调用失败: {}", toolName, e);
                throw new McpToolException("工具调用失败: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 带超时的工具调用
     * 
     * @param toolName 工具名称
     * @param parameters 调用参数
     * @param timeoutSeconds 超时时间（秒）
     * @return 工具执行结果
     */
    public CompletableFuture<ToolResult> callToolWithTimeout(
            String toolName, Map<String, Object> parameters, long timeoutSeconds) {
        
        return callTool(toolName, parameters)
            .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .exceptionally(throwable -> {
                if (throwable instanceof java.util.concurrent.TimeoutException) {
                    log.warn("MCP工具调用超时: {}, 超时时间: {}秒", toolName, timeoutSeconds);
                    updateToolHealth(toolName, HealthStatus.UNHEALTHY, "调用超时");
                    throw new McpToolException("工具调用超时: " + toolName);
                } else {
                    throw new McpToolException("工具调用异常: " + throwable.getMessage(), throwable);
                }
            });
    }
    
    /**
     * 批量调用MCP工具
     * 
     * @param toolCalls 工具调用列表
     * @return 批量调用结果
     */
    public CompletableFuture<Map<String, ToolResult>> batchCallTools(
            List<ToolCallRequest> toolCalls) {
        
        if (toolCalls == null || toolCalls.isEmpty()) {
            return CompletableFuture.completedFuture(new HashMap<>());
        }
        
        Map<String, CompletableFuture<ToolResult>> futures = new HashMap<>();
        
        // 并行执行所有工具调用
        for (ToolCallRequest request : toolCalls) {
            String key = request.getToolName() + "_" + request.getRequestId();
            futures.put(key, callTool(request.getToolName(), request.getParameters()));
        }
        
        // 等待所有调用完成
        return CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                Map<String, ToolResult> results = new HashMap<>();
                futures.forEach((key, future) -> {
                    try {
                        results.put(key, future.get());
                    } catch (Exception e) {
                        log.error("批量调用中的工具执行失败: {}", key, e);
                        results.put(key, ToolResult.error("执行失败: " + e.getMessage()));
                    }
                });
                return results;
            });
    }
    
    /**
     * 获取已注册的工具列表
     * 
     * @return 工具信息列表
     */
    public List<McpToolInfo> getRegisteredTools() {
        return new ArrayList<>(registeredTools.values());
    }
    
    /**
     * 获取工具信息
     * 
     * @param toolName 工具名称
     * @return 工具信息
     */
    public McpToolInfo getToolInfo(String toolName) {
        return registeredTools.get(toolName);
    }
    
    /**
     * 检查工具是否存在
     * 
     * @param toolName 工具名称
     * @return 是否存在
     */
    public boolean isToolRegistered(String toolName) {
        return registeredTools.containsKey(toolName);
    }
    
    /**
     * 获取工具统计信息
     * 
     * @param toolName 工具名称
     * @return 统计信息
     */
    public ToolStats getToolStats(String toolName) {
        return toolStats.get(toolName);
    }
    
    /**
     * 获取所有工具统计信息
     * 
     * @return 所有工具的统计信息
     */
    public Map<String, ToolStats> getAllToolStats() {
        return new HashMap<>(toolStats);
    }
    
    /**
     * 获取工具健康状态
     * 
     * @param toolName 工具名称
     * @return 健康状态
     */
    public ToolHealth getToolHealth(String toolName) {
        return toolHealth.get(toolName);
    }
    
    /**
     * 获取所有工具健康状态
     * 
     * @return 所有工具的健康状态
     */
    public Map<String, ToolHealth> getAllToolHealth() {
        return new HashMap<>(toolHealth);
    }
    
    /**
     * 健康检查
     * 
     * @param toolName 工具名称
     * @return 健康检查结果
     */
    public CompletableFuture<ToolHealth> healthCheck(String toolName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                McpToolInfo toolInfo = registeredTools.get(toolName);
                if (toolInfo == null) {
                    return new ToolHealth(toolName, HealthStatus.NOT_FOUND, "工具未注册");
                }
                
                // 执行简单的健康检查调用
                Map<String, Object> healthCheckParams = Map.of("action", "health_check");
                ToolResult result = executeToolCall(toolInfo, healthCheckParams);
                
                if (result.isSuccess()) {
                    updateToolHealth(toolName, HealthStatus.HEALTHY, "健康检查通过");
                } else {
                    updateToolHealth(toolName, HealthStatus.UNHEALTHY, "健康检查失败: " + result.getError());
                }
                
                return toolHealth.get(toolName);
                
            } catch (Exception e) {
                log.error("工具健康检查失败: {}", toolName, e);
                updateToolHealth(toolName, HealthStatus.UNHEALTHY, "健康检查异常: " + e.getMessage());
                return toolHealth.get(toolName);
            }
        });
    }
    
    /**
     * 批量健康检查
     * 
     * @return 所有工具的健康检查结果
     */
    public CompletableFuture<Map<String, ToolHealth>> batchHealthCheck() {
        List<CompletableFuture<ToolHealth>> futures = registeredTools.keySet().stream()
            .map(this::healthCheck)
            .toList();
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                Map<String, ToolHealth> results = new HashMap<>();
                futures.forEach(future -> {
                    try {
                        ToolHealth health = future.get();
                        results.put(health.getToolName(), health);
                    } catch (Exception e) {
                        log.error("批量健康检查中的工具检查失败", e);
                    }
                });
                return results;
            });
    }
    
    /**
     * 注销工具
     * 
     * @param toolName 工具名称
     */
    public void unregisterTool(String toolName) {
        registeredTools.remove(toolName);
        toolStats.remove(toolName);
        toolHealth.remove(toolName);
        
        log.info("MCP工具注销成功: {}", toolName);
    }
    
    /**
     * 清空所有工具
     */
    public void clearAllTools() {
        registeredTools.clear();
        toolStats.clear();
        toolHealth.clear();
        
        log.info("所有MCP工具已清空");
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 执行工具调用
     */
    private ToolResult executeToolCall(McpToolInfo toolInfo, Map<String, Object> parameters) {
        // 这里应该根据具体的MCP协议实现工具调用
        // 暂时返回模拟结果
        try {
            // 模拟工具执行
            Thread.sleep(100); // 模拟执行时间
            
            return ToolResult.success(Map.of(
                "toolName", toolInfo.getName(),
                "result", "执行成功",
                "parameters", parameters,
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new McpToolException("工具执行被中断", e);
        }
    }
    
    /**
     * 更新工具健康状态
     */
    private void updateToolHealth(String toolName, HealthStatus status, String message) {
        ToolHealth health = toolHealth.get(toolName);
        if (health != null) {
            health.setStatus(status);
            health.setMessage(message);
            health.setLastCheckTime(System.currentTimeMillis());
        }
    }
    
    // ==================== 内部类和枚举 ====================
    
    /**
     * 工具调用请求
     */
    @Data
    public static class ToolCallRequest {
        private String requestId;
        private String toolName;
        private Map<String, Object> parameters;
        private long timeoutSeconds = 30; // 默认30秒超时
        
        public ToolCallRequest(String toolName, Map<String, Object> parameters) {
            this.requestId = UUID.randomUUID().toString();
            this.toolName = toolName;
            this.parameters = parameters;
        }
    }
    
    /**
     * 工具统计信息
     */
    @Data
    public static class ToolStats {
        private long totalCalls = 0;
        private long successCalls = 0;
        private long errorCalls = 0;
        private long totalExecutionTime = 0;
        private long averageExecutionTime = 0;
        private long lastCallTime = 0;
        private long firstCallTime = 0;
        
        public void recordCallStart() {
            totalCalls++;
            long currentTime = System.currentTimeMillis();
            lastCallTime = currentTime;
            if (firstCallTime == 0) {
                firstCallTime = currentTime;
            }
        }
        
        public void recordCallComplete(long executionTime) {
            successCalls++;
            totalExecutionTime += executionTime;
            averageExecutionTime = totalExecutionTime / successCalls;
        }
        
        public void recordCallError() {
            errorCalls++;
        }
        
        public double getSuccessRate() {
            return totalCalls > 0 ? (double) successCalls / totalCalls : 0.0;
        }
        
        public double getErrorRate() {
            return totalCalls > 0 ? (double) errorCalls / totalCalls : 0.0;
        }
    }
    
    /**
     * 工具健康状态
     */
    @Data
    public static class ToolHealth {
        private String toolName;
        private HealthStatus status;
        private String message;
        private long lastCheckTime;
        
        public ToolHealth(String toolName, HealthStatus status) {
            this.toolName = toolName;
            this.status = status;
            this.lastCheckTime = System.currentTimeMillis();
        }
        
        public ToolHealth(String toolName, HealthStatus status, String message) {
            this(toolName, status);
            this.message = message;
        }
    }
    
    /**
     * 健康状态枚举
     */
    public enum HealthStatus {
        HEALTHY("健康"),
        UNHEALTHY("不健康"),
        UNKNOWN("未知"),
        NOT_FOUND("未找到");
        
        private final String description;
        
        HealthStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * MCP工具异常
     */
    public static class McpToolException extends RuntimeException {
        public McpToolException(String message) {
            super(message);
        }
        
        public McpToolException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}