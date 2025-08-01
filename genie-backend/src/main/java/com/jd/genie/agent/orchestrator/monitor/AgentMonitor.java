package com.jd.genie.agent.orchestrator.monitor;

import com.jd.genie.agent.orchestrator.registry.AgentRegistry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 智能体监控器
 * 负责监控智能体的执行状态、性能指标和健康状况
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component
public class AgentMonitor {
    
    @Autowired
    private AgentRegistry agentRegistry;
    
    /**
     * 监控数据存储
     */
    private final Map<String, AgentMonitorData> monitorData = new ConcurrentHashMap<>();
    
    /**
     * 系统监控数据
     */
    private final SystemMonitorData systemMonitorData = new SystemMonitorData();
    
    /**
     * 告警规则
     */
    private final Map<String, AlertRule> alertRules = new ConcurrentHashMap<>();
    
    /**
     * 告警历史
     */
    private final List<AlertRecord> alertHistory = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * 监控开始时间
     */
    private long monitorStartTime;
    
    @PostConstruct
    public void initialize() {
        monitorStartTime = System.currentTimeMillis();
        initializeAlertRules();
        log.info("智能体监控器初始化完成");
    }
    
    /**
     * 初始化告警规则
     */
    private void initializeAlertRules() {
        // 成功率告警
        alertRules.put("success_rate", AlertRule.builder()
            .name("成功率告警")
            .description("智能体成功率低于阈值")
            .threshold(0.8) // 80%
            .operator(AlertOperator.LESS_THAN)
            .severity(AlertSeverity.WARNING)
            .enabled(true)
            .build());
        
        // 响应时间告警
        alertRules.put("response_time", AlertRule.builder()
            .name("响应时间告警")
            .description("智能体平均响应时间过长")
            .threshold(30000.0) // 30秒
            .operator(AlertOperator.GREATER_THAN)
            .severity(AlertSeverity.WARNING)
            .enabled(true)
            .build());
        
        // 错误率告警
        alertRules.put("error_rate", AlertRule.builder()
            .name("错误率告警")
            .description("智能体错误率过高")
            .threshold(0.1) // 10%
            .operator(AlertOperator.GREATER_THAN)
            .severity(AlertSeverity.CRITICAL)
            .enabled(true)
            .build());
        
        // 连续失败告警
        alertRules.put("consecutive_failures", AlertRule.builder()
            .name("连续失败告警")
            .description("智能体连续失败次数过多")
            .threshold(5.0)
            .operator(AlertOperator.GREATER_THAN)
            .severity(AlertSeverity.CRITICAL)
            .enabled(true)
            .build());
        
        log.info("初始化 {} 个告警规则", alertRules.size());
    }
    
    /**
     * 记录智能体执行开始
     */
    public void recordExecutionStart(String agentType, String taskId) {
        AgentMonitorData data = getOrCreateMonitorData(agentType);
        data.recordExecutionStart(taskId);
        
        systemMonitorData.incrementActiveExecutions();
        
        log.debug("记录智能体 {} 开始执行任务 {}", agentType, taskId);
    }
    
    /**
     * 记录智能体执行完成
     */
    public void recordExecutionComplete(String agentType, String taskId, long executionTime, boolean success) {
        AgentMonitorData data = getOrCreateMonitorData(agentType);
        data.recordExecutionComplete(taskId, executionTime, success);
        
        systemMonitorData.decrementActiveExecutions();
        systemMonitorData.recordExecution(executionTime, success);
        
        // 更新注册表统计
        agentRegistry.recordAgentCall(agentType, executionTime, success);
        
        // 检查告警
        checkAlerts(agentType, data);
        
        log.debug("记录智能体 {} 完成任务 {}，执行时间: {}ms，成功: {}", 
            agentType, taskId, executionTime, success);
    }
    
    /**
     * 记录智能体执行异常
     */
    public void recordExecutionError(String agentType, String taskId, String errorMessage, Throwable exception) {
        AgentMonitorData data = getOrCreateMonitorData(agentType);
        data.recordError(taskId, errorMessage, exception);
        
        systemMonitorData.decrementActiveExecutions();
        systemMonitorData.recordExecution(0, false);
        
        // 更新健康状态
        agentRegistry.updateAgentHealth(agentType, 
            AgentRegistry.HealthStatus.DEGRADED, 
            "执行异常: " + errorMessage);
        
        log.warn("智能体 {} 执行任务 {} 异常: {}", agentType, taskId, errorMessage, exception);
    }
    
    /**
     * 获取或创建监控数据
     */
    private AgentMonitorData getOrCreateMonitorData(String agentType) {
        return monitorData.computeIfAbsent(agentType, k -> new AgentMonitorData(agentType));
    }
    
    /**
     * 检查告警
     */
    private void checkAlerts(String agentType, AgentMonitorData data) {
        for (Map.Entry<String, AlertRule> entry : alertRules.entrySet()) {
            AlertRule rule = entry.getValue();
            if (!rule.isEnabled()) {
                continue;
            }
            
            double value = getMetricValue(data, entry.getKey());
            if (shouldTriggerAlert(value, rule)) {
                triggerAlert(agentType, entry.getKey(), value, rule);
            }
        }
    }
    
    /**
     * 获取指标值
     */
    private double getMetricValue(AgentMonitorData data, String metricName) {
        return switch (metricName) {
            case "success_rate" -> data.getSuccessRate();
            case "response_time" -> data.getAverageExecutionTime();
            case "error_rate" -> data.getErrorRate();
            case "consecutive_failures" -> data.getConsecutiveFailures();
            default -> 0.0;
        };
    }
    
    /**
     * 判断是否应该触发告警
     */
    private boolean shouldTriggerAlert(double value, AlertRule rule) {
        return switch (rule.getOperator()) {
            case GREATER_THAN -> value > rule.getThreshold();
            case LESS_THAN -> value < rule.getThreshold();
            case EQUALS -> Math.abs(value - rule.getThreshold()) < 0.001;
            case NOT_EQUALS -> Math.abs(value - rule.getThreshold()) >= 0.001;
        };
    }
    
    /**
     * 触发告警
     */
    private void triggerAlert(String agentType, String metricName, double value, AlertRule rule) {
        AlertRecord alert = AlertRecord.builder()
            .agentType(agentType)
            .metricName(metricName)
            .metricValue(value)
            .rule(rule)
            .timestamp(System.currentTimeMillis())
            .build();
        
        alertHistory.add(alert);
        
        // 限制告警历史大小
        if (alertHistory.size() > 1000) {
            alertHistory.subList(0, alertHistory.size() - 1000).clear();
        }
        
        log.warn("触发告警 - 智能体: {}, 指标: {}, 值: {}, 规则: {}", 
            agentType, metricName, value, rule.getName());
    }
    
    /**
     * 获取智能体监控数据
     */
    public AgentMonitorData getAgentMonitorData(String agentType) {
        return monitorData.get(agentType);
    }
    
    /**
     * 获取所有智能体监控数据
     */
    public Map<String, AgentMonitorData> getAllAgentMonitorData() {
        return new HashMap<>(monitorData);
    }
    
    /**
     * 获取系统监控数据
     */
    public SystemMonitorData getSystemMonitorData() {
        return systemMonitorData;
    }
    
    /**
     * 获取告警历史
     */
    public List<AlertRecord> getAlertHistory(int limit) {
        synchronized (alertHistory) {
            int size = alertHistory.size();
            int fromIndex = Math.max(0, size - limit);
            return new ArrayList<>(alertHistory.subList(fromIndex, size));
        }
    }
    
    /**
     * 获取活跃告警
     */
    public List<AlertRecord> getActiveAlerts() {
        long cutoffTime = System.currentTimeMillis() - 300000; // 5分钟内的告警
        synchronized (alertHistory) {
            return alertHistory.stream()
                .filter(alert -> alert.getTimestamp() > cutoffTime)
                .toList();
        }
    }
    
    /**
     * 生成监控报告
     */
    public MonitorReport generateReport() {
        Map<String, AgentSummary> agentSummaries = new HashMap<>();
        
        for (Map.Entry<String, AgentMonitorData> entry : monitorData.entrySet()) {
            String agentType = entry.getKey();
            AgentMonitorData data = entry.getValue();
            
            agentSummaries.put(agentType, AgentSummary.builder()
                .agentType(agentType)
                .totalExecutions(data.getTotalExecutions())
                .successfulExecutions(data.getSuccessfulExecutions())
                .failedExecutions(data.getFailedExecutions())
                .successRate(data.getSuccessRate())
                .averageExecutionTime(data.getAverageExecutionTime())
                .minExecutionTime(data.getMinExecutionTime())
                .maxExecutionTime(data.getMaxExecutionTime())
                .errorRate(data.getErrorRate())
                .consecutiveFailures(data.getConsecutiveFailures())
                .lastExecutionTime(data.getLastExecutionTime())
                .build());
        }
        
        return MonitorReport.builder()
            .reportTime(System.currentTimeMillis())
            .monitorDuration(System.currentTimeMillis() - monitorStartTime)
            .agentSummaries(agentSummaries)
            .systemSummary(SystemSummary.builder()
                .totalExecutions(systemMonitorData.getTotalExecutions())
                .activeExecutions(systemMonitorData.getActiveExecutions())
                .successfulExecutions(systemMonitorData.getSuccessfulExecutions())
                .failedExecutions(systemMonitorData.getFailedExecutions())
                .overallSuccessRate(systemMonitorData.getOverallSuccessRate())
                .averageExecutionTime(systemMonitorData.getAverageExecutionTime())
                .peakActiveExecutions(systemMonitorData.getPeakActiveExecutions())
                .build())
            .activeAlerts(getActiveAlerts())
            .build();
    }
    
    /**
     * 定期健康检查
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void performHealthCheck() {
        log.debug("执行智能体健康检查...");
        
        for (String agentType : agentRegistry.getAllAgentTypes()) {
            AgentMonitorData data = monitorData.get(agentType);
            if (data != null) {
                // 检查最近是否有执行
                long timeSinceLastExecution = System.currentTimeMillis() - data.getLastExecutionTime();
                if (timeSinceLastExecution > 300000) { // 5分钟无执行
                    agentRegistry.updateAgentHealth(agentType, 
                        AgentRegistry.HealthStatus.UNKNOWN, 
                        "长时间无执行记录");
                } else if (data.getConsecutiveFailures() > 3) {
                    agentRegistry.updateAgentHealth(agentType, 
                        AgentRegistry.HealthStatus.UNHEALTHY, 
                        "连续失败次数过多");
                } else if (data.getSuccessRate() < 0.8) {
                    agentRegistry.updateAgentHealth(agentType, 
                        AgentRegistry.HealthStatus.DEGRADED, 
                        "成功率偏低");
                } else {
                    agentRegistry.updateAgentHealth(agentType, 
                        AgentRegistry.HealthStatus.HEALTHY, 
                        "运行正常");
                }
            }
        }
    }
    
    /**
     * 清理历史数据
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanupHistoryData() {
        log.debug("清理监控历史数据...");
        
        // 清理过期的告警记录
        long cutoffTime = System.currentTimeMillis() - 86400000; // 24小时前
        synchronized (alertHistory) {
            alertHistory.removeIf(alert -> alert.getTimestamp() < cutoffTime);
        }
        
        // 清理智能体监控数据中的详细执行记录
        for (AgentMonitorData data : monitorData.values()) {
            data.cleanupOldExecutions(cutoffTime);
        }
        
        log.debug("监控历史数据清理完成");
    }
    
    /**
     * 智能体监控数据
     */
    @Data
    public static class AgentMonitorData {
        private final String agentType;
        private final AtomicLong totalExecutions = new AtomicLong(0);
        private final AtomicLong successfulExecutions = new AtomicLong(0);
        private final AtomicLong failedExecutions = new AtomicLong(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private volatile long minExecutionTime = Long.MAX_VALUE;
        private volatile long maxExecutionTime = 0;
        private volatile long lastExecutionTime = 0;
        private volatile int consecutiveFailures = 0;
        private final Map<String, ExecutionRecord> activeExecutions = new ConcurrentHashMap<>();
        private final List<ErrorRecord> recentErrors = Collections.synchronizedList(new ArrayList<>());
        
        public AgentMonitorData(String agentType) {
            this.agentType = agentType;
        }
        
        public void recordExecutionStart(String taskId) {
            activeExecutions.put(taskId, new ExecutionRecord(taskId, System.currentTimeMillis()));
        }
        
        public void recordExecutionComplete(String taskId, long executionTime, boolean success) {
            activeExecutions.remove(taskId);
            
            totalExecutions.incrementAndGet();
            totalExecutionTime.addAndGet(executionTime);
            lastExecutionTime = System.currentTimeMillis();
            
            if (executionTime < minExecutionTime) {
                minExecutionTime = executionTime;
            }
            if (executionTime > maxExecutionTime) {
                maxExecutionTime = executionTime;
            }
            
            if (success) {
                successfulExecutions.incrementAndGet();
                consecutiveFailures = 0;
            } else {
                failedExecutions.incrementAndGet();
                consecutiveFailures++;
            }
        }
        
        public void recordError(String taskId, String errorMessage, Throwable exception) {
            activeExecutions.remove(taskId);
            
            ErrorRecord error = new ErrorRecord(taskId, errorMessage, exception, System.currentTimeMillis());
            recentErrors.add(error);
            
            // 限制错误记录数量
            if (recentErrors.size() > 100) {
                recentErrors.subList(0, recentErrors.size() - 100).clear();
            }
            
            failedExecutions.incrementAndGet();
            totalExecutions.incrementAndGet();
            consecutiveFailures++;
            lastExecutionTime = System.currentTimeMillis();
        }
        
        public double getSuccessRate() {
            long total = totalExecutions.get();
            return total > 0 ? (double) successfulExecutions.get() / total : 0.0;
        }
        
        public double getErrorRate() {
            long total = totalExecutions.get();
            return total > 0 ? (double) failedExecutions.get() / total : 0.0;
        }
        
        public double getAverageExecutionTime() {
            long total = totalExecutions.get();
            return total > 0 ? (double) totalExecutionTime.get() / total : 0.0;
        }
        
        public void cleanupOldExecutions(long cutoffTime) {
            synchronized (recentErrors) {
                recentErrors.removeIf(error -> error.getTimestamp() < cutoffTime);
            }
        }
    }
    
    /**
     * 系统监控数据
     */
    @Data
    public static class SystemMonitorData {
        private final AtomicLong totalExecutions = new AtomicLong(0);
        private final AtomicLong successfulExecutions = new AtomicLong(0);
        private final AtomicLong failedExecutions = new AtomicLong(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private volatile int activeExecutions = 0;
        private volatile int peakActiveExecutions = 0;
        
        public synchronized void incrementActiveExecutions() {
            activeExecutions++;
            if (activeExecutions > peakActiveExecutions) {
                peakActiveExecutions = activeExecutions;
            }
        }
        
        public synchronized void decrementActiveExecutions() {
            if (activeExecutions > 0) {
                activeExecutions--;
            }
        }
        
        public void recordExecution(long executionTime, boolean success) {
            totalExecutions.incrementAndGet();
            totalExecutionTime.addAndGet(executionTime);
            
            if (success) {
                successfulExecutions.incrementAndGet();
            } else {
                failedExecutions.incrementAndGet();
            }
        }
        
        public double getOverallSuccessRate() {
            long total = totalExecutions.get();
            return total > 0 ? (double) successfulExecutions.get() / total : 0.0;
        }
        
        public double getAverageExecutionTime() {
            long total = totalExecutions.get();
            return total > 0 ? (double) totalExecutionTime.get() / total : 0.0;
        }
    }
    
    // 其他内部类定义...
    @lombok.Data
    @lombok.Builder
    public static class AlertRule {
        private String name;
        private String description;
        private double threshold;
        private AlertOperator operator;
        private AlertSeverity severity;
        private boolean enabled;
    }
    
    public enum AlertOperator {
        GREATER_THAN, LESS_THAN, EQUALS, NOT_EQUALS
    }
    
    public enum AlertSeverity {
        INFO, WARNING, CRITICAL
    }
    
    @lombok.Data
    @lombok.Builder
    public static class AlertRecord {
        private String agentType;
        private String metricName;
        private double metricValue;
        private AlertRule rule;
        private long timestamp;
    }
    
    @lombok.Data
    public static class ExecutionRecord {
        private final String taskId;
        private final long startTime;
        
        public ExecutionRecord(String taskId, long startTime) {
            this.taskId = taskId;
            this.startTime = startTime;
        }
    }
    
    @lombok.Data
    public static class ErrorRecord {
        private final String taskId;
        private final String errorMessage;
        private final Throwable exception;
        private final long timestamp;
        
        public ErrorRecord(String taskId, String errorMessage, Throwable exception, long timestamp) {
            this.taskId = taskId;
            this.errorMessage = errorMessage;
            this.exception = exception;
            this.timestamp = timestamp;
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class MonitorReport {
        private long reportTime;
        private long monitorDuration;
        private Map<String, AgentSummary> agentSummaries;
        private SystemSummary systemSummary;
        private List<AlertRecord> activeAlerts;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class AgentSummary {
        private String agentType;
        private long totalExecutions;
        private long successfulExecutions;
        private long failedExecutions;
        private double successRate;
        private double averageExecutionTime;
        private long minExecutionTime;
        private long maxExecutionTime;
        private double errorRate;
        private int consecutiveFailures;
        private long lastExecutionTime;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class SystemSummary {
        private long totalExecutions;
        private int activeExecutions;
        private long successfulExecutions;
        private long failedExecutions;
        private double overallSuccessRate;
        private double averageExecutionTime;
        private int peakActiveExecutions;
    }
}