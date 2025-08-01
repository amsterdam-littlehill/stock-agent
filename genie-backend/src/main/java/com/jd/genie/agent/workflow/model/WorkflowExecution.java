package com.jd.genie.agent.workflow.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作流执行实例模型
 * 记录工作流的执行状态、进度和结果
 * 
 * 功能：
 * - 执行状态跟踪
 * - 节点执行记录
 * - 错误和异常处理
 * - 性能监控数据
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecution {
    
    /**
     * 执行实例ID
     */
    @NotBlank(message = "执行实例ID不能为空")
    private String executionId;
    
    /**
     * 工作流ID
     */
    @NotBlank(message = "工作流ID不能为空")
    private String workflowId;
    
    /**
     * 工作流版本
     */
    @NotBlank(message = "工作流版本不能为空")
    private String workflowVersion;
    
    /**
     * 执行者ID
     */
    @NotBlank(message = "执行者ID不能为空")
    private String executedBy;
    
    /**
     * 触发方式
     */
    @NotNull(message = "触发方式不能为空")
    private TriggerMode triggerMode;
    
    /**
     * 执行状态
     */
    @NotNull(message = "执行状态不能为空")
    private ExecutionStatus status;
    
    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    /**
     * 执行时长（毫秒）
     */
    private long executionDuration;
    
    /**
     * 输入参数
     */
    private Map<String, Object> inputParameters;
    
    /**
     * 输出结果
     */
    private Map<String, Object> outputResults;
    
    /**
     * 执行上下文
     */
    private Map<String, Object> executionContext;
    
    /**
     * 节点执行记录
     */
    @Valid
    private Map<String, NodeExecution> nodeExecutions;
    
    /**
     * 当前执行节点ID
     */
    private String currentNodeId;
    
    /**
     * 执行进度（0-100）
     */
    @Min(value = 0, message = "执行进度不能小于0")
    @Max(value = 100, message = "执行进度不能大于100")
    private int progress;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 错误堆栈
     */
    private String errorStackTrace;
    
    /**
     * 警告信息
     */
    private List<String> warnings;
    
    /**
     * 执行日志
     */
    private List<ExecutionLog> logs;
    
    /**
     * 性能统计
     */
    @Valid
    private PerformanceStats performanceStats;
    
    /**
     * 重试次数
     */
    @Min(value = 0, message = "重试次数不能小于0")
    private int retryCount;
    
    /**
     * 最大重试次数
     */
    @Min(value = 0, message = "最大重试次数不能小于0")
    private int maxRetries;
    
    /**
     * 下次重试时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextRetryTime;
    
    /**
     * 执行标签
     */
    private Set<String> tags;
    
    /**
     * 执行元数据
     */
    private Map<String, Object> metadata;
    
    // ==================== 节点执行记录 ====================
    
    /**
     * 节点执行记录
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeExecution {
        
        /**
         * 节点ID
         */
        @NotBlank(message = "节点ID不能为空")
        private String nodeId;
        
        /**
         * 节点名称
         */
        private String nodeName;
        
        /**
         * 节点类型
         */
        private WorkflowDefinition.NodeType nodeType;
        
        /**
         * 执行状态
         */
        @NotNull(message = "节点执行状态不能为空")
        private NodeExecutionStatus status;
        
        /**
         * 开始时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;
        
        /**
         * 结束时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;
        
        /**
         * 执行时长（毫秒）
         */
        private long executionDuration;
        
        /**
         * 输入数据
         */
        private Map<String, Object> inputData;
        
        /**
         * 输出数据
         */
        private Map<String, Object> outputData;
        
        /**
         * 错误信息
         */
        private String errorMessage;
        
        /**
         * 错误堆栈
         */
        private String errorStackTrace;
        
        /**
         * 重试次数
         */
        @Min(value = 0, message = "重试次数不能小于0")
        private int retryCount;
        
        /**
         * 执行器信息
         */
        private String executorInfo;
        
        /**
         * 资源使用情况
         */
        private ResourceUsage resourceUsage;
        
        /**
         * 节点日志
         */
        private List<String> logs;
    }
    
    /**
     * 资源使用情况
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceUsage {
        
        /**
         * CPU使用率（%）
         */
        @DecimalMin(value = "0.0", message = "CPU使用率不能小于0")
        @DecimalMax(value = "100.0", message = "CPU使用率不能大于100")
        private double cpuUsage;
        
        /**
         * 内存使用量（MB）
         */
        @Min(value = 0, message = "内存使用量不能小于0")
        private long memoryUsage;
        
        /**
         * 网络IO（字节）
         */
        @Min(value = 0, message = "网络IO不能小于0")
        private long networkIO;
        
        /**
         * 磁盘IO（字节）
         */
        @Min(value = 0, message = "磁盘IO不能小于0")
        private long diskIO;
    }
    
    // ==================== 执行日志 ====================
    
    /**
     * 执行日志
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionLog {
        
        /**
         * 日志ID
         */
        private String logId;
        
        /**
         * 时间戳
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
        private LocalDateTime timestamp;
        
        /**
         * 日志级别
         */
        @NotNull(message = "日志级别不能为空")
        private LogLevel level;
        
        /**
         * 节点ID
         */
        private String nodeId;
        
        /**
         * 日志消息
         */
        @NotBlank(message = "日志消息不能为空")
        private String message;
        
        /**
         * 详细信息
         */
        private String details;
        
        /**
         * 异常信息
         */
        private String exception;
    }
    
    // ==================== 性能统计 ====================
    
    /**
     * 性能统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceStats {
        
        /**
         * 总节点数
         */
        @Min(value = 0, message = "总节点数不能小于0")
        private int totalNodes;
        
        /**
         * 成功节点数
         */
        @Min(value = 0, message = "成功节点数不能小于0")
        private int successfulNodes;
        
        /**
         * 失败节点数
         */
        @Min(value = 0, message = "失败节点数不能小于0")
        private int failedNodes;
        
        /**
         * 跳过节点数
         */
        @Min(value = 0, message = "跳过节点数不能小于0")
        private int skippedNodes;
        
        /**
         * 平均节点执行时间（毫秒）
         */
        @Min(value = 0, message = "平均节点执行时间不能小于0")
        private long avgNodeExecutionTime;
        
        /**
         * 最长节点执行时间（毫秒）
         */
        @Min(value = 0, message = "最长节点执行时间不能小于0")
        private long maxNodeExecutionTime;
        
        /**
         * 最短节点执行时间（毫秒）
         */
        @Min(value = 0, message = "最短节点执行时间不能小于0")
        private long minNodeExecutionTime;
        
        /**
         * 总等待时间（毫秒）
         */
        @Min(value = 0, message = "总等待时间不能小于0")
        private long totalWaitTime;
        
        /**
         * 并行执行节点数
         */
        @Min(value = 0, message = "并行执行节点数不能小于0")
        private int parallelNodes;
        
        /**
         * 资源使用峰值
         */
        private ResourceUsage peakResourceUsage;
        
        /**
         * 吞吐量（节点/秒）
         */
        @DecimalMin(value = "0.0", message = "吞吐量不能小于0")
        private double throughput;
    }
    
    // ==================== 枚举定义 ====================
    
    /**
     * 触发方式
     */
    public enum TriggerMode {
        MANUAL,         // 手动触发
        SCHEDULE,       // 定时触发
        EVENT,          // 事件触发
        API,            // API触发
        RETRY          // 重试触发
    }
    
    /**
     * 执行状态
     */
    public enum ExecutionStatus {
        PENDING,        // 等待执行
        RUNNING,        // 执行中
        PAUSED,         // 暂停
        COMPLETED,      // 完成
        FAILED,         // 失败
        CANCELLED,      // 取消
        TIMEOUT,        // 超时
        RETRYING       // 重试中
    }
    
    /**
     * 节点执行状态
     */
    public enum NodeExecutionStatus {
        PENDING,        // 等待执行
        RUNNING,        // 执行中
        COMPLETED,      // 完成
        FAILED,         // 失败
        SKIPPED,        // 跳过
        TIMEOUT,        // 超时
        CANCELLED      // 取消
    }
    
    /**
     * 日志级别
     */
    public enum LogLevel {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 初始化执行实例
     */
    public static WorkflowExecution create(String workflowId, String workflowVersion, 
                                         String executedBy, TriggerMode triggerMode,
                                         Map<String, Object> inputParameters) {
        return WorkflowExecution.builder()
            .executionId(UUID.randomUUID().toString())
            .workflowId(workflowId)
            .workflowVersion(workflowVersion)
            .executedBy(executedBy)
            .triggerMode(triggerMode)
            .status(ExecutionStatus.PENDING)
            .startTime(LocalDateTime.now())
            .inputParameters(inputParameters != null ? inputParameters : new HashMap<>())
            .outputResults(new HashMap<>())
            .executionContext(new ConcurrentHashMap<>())
            .nodeExecutions(new ConcurrentHashMap<>())
            .progress(0)
            .warnings(new ArrayList<>())
            .logs(new ArrayList<>())
            .retryCount(0)
            .maxRetries(0)
            .tags(new HashSet<>())
            .metadata(new HashMap<>())
            .build();
    }
    
    /**
     * 开始执行
     */
    public void start() {
        this.status = ExecutionStatus.RUNNING;
        this.startTime = LocalDateTime.now();
        addLog(LogLevel.INFO, null, "工作流开始执行", null);
    }
    
    /**
     * 完成执行
     */
    public void complete() {
        this.status = ExecutionStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.executionDuration = java.time.Duration.between(startTime, endTime).toMillis();
        this.progress = 100;
        calculatePerformanceStats();
        addLog(LogLevel.INFO, null, "工作流执行完成", null);
    }
    
    /**
     * 执行失败
     */
    public void fail(String errorMessage, String stackTrace) {
        this.status = ExecutionStatus.FAILED;
        this.endTime = LocalDateTime.now();
        this.executionDuration = java.time.Duration.between(startTime, endTime).toMillis();
        this.errorMessage = errorMessage;
        this.errorStackTrace = stackTrace;
        calculatePerformanceStats();
        addLog(LogLevel.ERROR, null, "工作流执行失败: " + errorMessage, stackTrace);
    }
    
    /**
     * 取消执行
     */
    public void cancel() {
        this.status = ExecutionStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
        this.executionDuration = java.time.Duration.between(startTime, endTime).toMillis();
        calculatePerformanceStats();
        addLog(LogLevel.WARN, null, "工作流执行被取消", null);
    }
    
    /**
     * 暂停执行
     */
    public void pause() {
        this.status = ExecutionStatus.PAUSED;
        addLog(LogLevel.INFO, null, "工作流执行暂停", null);
    }
    
    /**
     * 恢复执行
     */
    public void resume() {
        this.status = ExecutionStatus.RUNNING;
        addLog(LogLevel.INFO, null, "工作流执行恢复", null);
    }
    
    /**
     * 开始节点执行
     */
    public void startNodeExecution(String nodeId, String nodeName, 
                                 WorkflowDefinition.NodeType nodeType,
                                 Map<String, Object> inputData) {
        this.currentNodeId = nodeId;
        
        NodeExecution nodeExecution = NodeExecution.builder()
            .nodeId(nodeId)
            .nodeName(nodeName)
            .nodeType(nodeType)
            .status(NodeExecutionStatus.RUNNING)
            .startTime(LocalDateTime.now())
            .inputData(inputData != null ? inputData : new HashMap<>())
            .outputData(new HashMap<>())
            .retryCount(0)
            .logs(new ArrayList<>())
            .build();
        
        nodeExecutions.put(nodeId, nodeExecution);
        addLog(LogLevel.INFO, nodeId, "节点开始执行: " + nodeName, null);
    }
    
    /**
     * 完成节点执行
     */
    public void completeNodeExecution(String nodeId, Map<String, Object> outputData) {
        NodeExecution nodeExecution = nodeExecutions.get(nodeId);
        if (nodeExecution != null) {
            nodeExecution.setStatus(NodeExecutionStatus.COMPLETED);
            nodeExecution.setEndTime(LocalDateTime.now());
            nodeExecution.setExecutionDuration(
                java.time.Duration.between(nodeExecution.getStartTime(), nodeExecution.getEndTime()).toMillis());
            nodeExecution.setOutputData(outputData != null ? outputData : new HashMap<>());
            
            addLog(LogLevel.INFO, nodeId, "节点执行完成: " + nodeExecution.getNodeName(), null);
        }
    }
    
    /**
     * 节点执行失败
     */
    public void failNodeExecution(String nodeId, String errorMessage, String stackTrace) {
        NodeExecution nodeExecution = nodeExecutions.get(nodeId);
        if (nodeExecution != null) {
            nodeExecution.setStatus(NodeExecutionStatus.FAILED);
            nodeExecution.setEndTime(LocalDateTime.now());
            nodeExecution.setExecutionDuration(
                java.time.Duration.between(nodeExecution.getStartTime(), nodeExecution.getEndTime()).toMillis());
            nodeExecution.setErrorMessage(errorMessage);
            nodeExecution.setErrorStackTrace(stackTrace);
            
            addLog(LogLevel.ERROR, nodeId, "节点执行失败: " + nodeExecution.getNodeName() + ", 错误: " + errorMessage, stackTrace);
        }
    }
    
    /**
     * 跳过节点执行
     */
    public void skipNodeExecution(String nodeId, String reason) {
        NodeExecution nodeExecution = nodeExecutions.get(nodeId);
        if (nodeExecution != null) {
            nodeExecution.setStatus(NodeExecutionStatus.SKIPPED);
            nodeExecution.setEndTime(LocalDateTime.now());
            nodeExecution.setExecutionDuration(
                java.time.Duration.between(nodeExecution.getStartTime(), nodeExecution.getEndTime()).toMillis());
            
            addLog(LogLevel.WARN, nodeId, "节点被跳过: " + nodeExecution.getNodeName() + ", 原因: " + reason, null);
        }
    }
    
    /**
     * 添加执行日志
     */
    public void addLog(LogLevel level, String nodeId, String message, String details) {
        ExecutionLog log = ExecutionLog.builder()
            .logId(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .level(level)
            .nodeId(nodeId)
            .message(message)
            .details(details)
            .build();
        
        logs.add(log);
    }
    
    /**
     * 添加警告
     */
    public void addWarning(String warning) {
        warnings.add(warning);
        addLog(LogLevel.WARN, currentNodeId, warning, null);
    }
    
    /**
     * 更新进度
     */
    public void updateProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
    }
    
    /**
     * 设置上下文变量
     */
    public void setContextVariable(String key, Object value) {
        executionContext.put(key, value);
    }
    
    /**
     * 获取上下文变量
     */
    public Object getContextVariable(String key) {
        return executionContext.get(key);
    }
    
    /**
     * 获取上下文变量（带默认值）
     */
    @SuppressWarnings("unchecked")
    public <T> T getContextVariable(String key, T defaultValue) {
        Object value = executionContext.get(key);
        return value != null ? (T) value : defaultValue;
    }
    
    /**
     * 是否执行完成
     */
    public boolean isCompleted() {
        return status == ExecutionStatus.COMPLETED || 
               status == ExecutionStatus.FAILED || 
               status == ExecutionStatus.CANCELLED;
    }
    
    /**
     * 是否执行成功
     */
    public boolean isSuccessful() {
        return status == ExecutionStatus.COMPLETED;
    }
    
    /**
     * 是否可以重试
     */
    public boolean canRetry() {
        return status == ExecutionStatus.FAILED && retryCount < maxRetries;
    }
    
    /**
     * 计算性能统计
     */
    private void calculatePerformanceStats() {
        if (performanceStats == null) {
            performanceStats = new PerformanceStats();
        }
        
        performanceStats.setTotalNodes(nodeExecutions.size());
        performanceStats.setSuccessfulNodes((int) nodeExecutions.values().stream()
            .filter(ne -> ne.getStatus() == NodeExecutionStatus.COMPLETED)
            .count());
        performanceStats.setFailedNodes((int) nodeExecutions.values().stream()
            .filter(ne -> ne.getStatus() == NodeExecutionStatus.FAILED)
            .count());
        performanceStats.setSkippedNodes((int) nodeExecutions.values().stream()
            .filter(ne -> ne.getStatus() == NodeExecutionStatus.SKIPPED)
            .count());
        
        List<Long> executionTimes = nodeExecutions.values().stream()
            .filter(ne -> ne.getExecutionDuration() > 0)
            .map(NodeExecution::getExecutionDuration)
            .toList();
        
        if (!executionTimes.isEmpty()) {
            performanceStats.setAvgNodeExecutionTime(
                (long) executionTimes.stream().mapToLong(Long::longValue).average().orElse(0));
            performanceStats.setMaxNodeExecutionTime(
                executionTimes.stream().mapToLong(Long::longValue).max().orElse(0));
            performanceStats.setMinNodeExecutionTime(
                executionTimes.stream().mapToLong(Long::longValue).min().orElse(0));
        }
        
        if (executionDuration > 0 && performanceStats.getTotalNodes() > 0) {
            performanceStats.setThroughput(
                (double) performanceStats.getTotalNodes() / (executionDuration / 1000.0));
        }
    }
    
    /**
     * 获取执行摘要
     */
    public String getExecutionSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("执行ID: ").append(executionId).append("\n");
        summary.append("工作流ID: ").append(workflowId).append("\n");
        summary.append("状态: ").append(status).append("\n");
        summary.append("进度: ").append(progress).append("%\n");
        summary.append("执行时长: ").append(executionDuration).append("ms\n");
        
        if (performanceStats != null) {
            summary.append("总节点数: ").append(performanceStats.getTotalNodes()).append("\n");
            summary.append("成功节点数: ").append(performanceStats.getSuccessfulNodes()).append("\n");
            summary.append("失败节点数: ").append(performanceStats.getFailedNodes()).append("\n");
        }
        
        if (errorMessage != null) {
            summary.append("错误信息: ").append(errorMessage).append("\n");
        }
        
        return summary.toString();
    }
}