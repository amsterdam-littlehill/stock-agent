package com.jd.genie.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 智能体实体类
 */
@Entity
@Table(name = "agent", indexes = {
    @Index(name = "idx_agent_type", columnList = "agent_type"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_name", columnList = "name", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 智能体名称
     */
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;
    
    /**
     * 智能体类型 (TECHNICAL-技术分析师, FUNDAMENTAL-基本面分析师, SENTIMENT-情绪分析师, RISK-风险分析师, COORDINATOR-协调器)
     */
    @Column(name = "agent_type", nullable = false, length = 50)
    private String agentType;
    
    /**
     * 智能体描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * 智能体版本
     */
    @Column(name = "version", length = 20)
    @Builder.Default
    private String version = "1.0";
    
    /**
     * 智能体状态 (ACTIVE-活跃, INACTIVE-非活跃, MAINTENANCE-维护中)
     */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";
    
    /**
     * 专业领域 (JSON格式)
     */
    @Column(name = "specialties", columnDefinition = "TEXT")
    private String specialties;
    
    /**
     * 支持的分析类型 (JSON格式)
     */
    @Column(name = "supported_analysis_types", columnDefinition = "TEXT")
    private String supportedAnalysisTypes;
    
    /**
     * 配置参数 (JSON格式)
     */
    @Column(name = "configuration", columnDefinition = "TEXT")
    private String configuration;
    
    /**
     * 系统提示词
     */
    @Column(name = "system_prompt", columnDefinition = "LONGTEXT")
    private String systemPrompt;
    
    /**
     * 最大并发数
     */
    @Column(name = "max_concurrent")
    @Builder.Default
    private Integer maxConcurrent = 5;
    
    /**
     * 当前并发数
     */
    @Column(name = "current_concurrent")
    @Builder.Default
    private Integer currentConcurrent = 0;
    
    /**
     * 超时时间(毫秒)
     */
    @Column(name = "timeout_ms")
    @Builder.Default
    private Long timeoutMs = 30000L;
    
    /**
     * 优先级 (1-最高, 5-最低)
     */
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 3;
    
    /**
     * 权重 (用于综合分析时的权重计算)
     */
    @Column(name = "weight", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal weight = new BigDecimal("1.0000");
    
    /**
     * 成功率 (0-100)
     */
    @Column(name = "success_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal successRate = new BigDecimal("100.00");
    
    /**
     * 平均响应时间(毫秒)
     */
    @Column(name = "avg_response_time")
    @Builder.Default
    private Long avgResponseTime = 0L;
    
    /**
     * 总任务数
     */
    @Column(name = "total_tasks")
    @Builder.Default
    private Long totalTasks = 0L;
    
    /**
     * 成功任务数
     */
    @Column(name = "successful_tasks")
    @Builder.Default
    private Long successfulTasks = 0L;
    
    /**
     * 失败任务数
     */
    @Column(name = "failed_tasks")
    @Builder.Default
    private Long failedTasks = 0L;
    
    /**
     * 平均置信度
     */
    @Column(name = "avg_confidence", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal avgConfidence = new BigDecimal("0.00");
    
    /**
     * 平均准确率
     */
    @Column(name = "avg_accuracy", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal avgAccuracy = new BigDecimal("0.00");
    
    /**
     * 最后活跃时间
     */
    @Column(name = "last_active_time")
    private LocalDateTime lastActiveTime;
    
    /**
     * 最后任务时间
     */
    @Column(name = "last_task_time")
    private LocalDateTime lastTaskTime;
    
    /**
     * 健康检查URL
     */
    @Column(name = "health_check_url", length = 500)
    private String healthCheckUrl;
    
    /**
     * API端点
     */
    @Column(name = "api_endpoint", length = 500)
    private String apiEndpoint;
    
    /**
     * API密钥
     */
    @Column(name = "api_key", length = 200)
    private String apiKey;
    
    /**
     * 模型名称
     */
    @Column(name = "model_name", length = 100)
    private String modelName;
    
    /**
     * 温度参数
     */
    @Column(name = "temperature", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal temperature = new BigDecimal("0.70");
    
    /**
     * 最大令牌数
     */
    @Column(name = "max_tokens")
    @Builder.Default
    private Integer maxTokens = 2000;
    
    /**
     * 是否启用
     */
    @Column(name = "enabled")
    @Builder.Default
    private Boolean enabled = true;
    
    /**
     * 备注信息
     */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 获取智能体类型显示名称
     */
    public String getAgentTypeDisplayName() {
        switch (agentType) {
            case "TECHNICAL": return "技术分析师";
            case "FUNDAMENTAL": return "基本面分析师";
            case "SENTIMENT": return "情绪分析师";
            case "RISK": return "风险分析师";
            case "COORDINATOR": return "协调器";
            default: return agentType;
        }
    }
    
    /**
     * 获取状态显示名称
     */
    public String getStatusDisplayName() {
        switch (status) {
            case "ACTIVE": return "活跃";
            case "INACTIVE": return "非活跃";
            case "MAINTENANCE": return "维护中";
            default: return status;
        }
    }
    
    /**
     * 获取优先级显示名称
     */
    public String getPriorityDisplayName() {
        switch (priority) {
            case 1: return "最高";
            case 2: return "高";
            case 3: return "中";
            case 4: return "低";
            case 5: return "最低";
            default: return "中";
        }
    }
    
    /**
     * 判断是否可用
     */
    public boolean isAvailable() {
        return enabled && "ACTIVE".equals(status) && currentConcurrent < maxConcurrent;
    }
    
    /**
     * 判断是否繁忙
     */
    public boolean isBusy() {
        return currentConcurrent >= maxConcurrent;
    }
    
    /**
     * 判断是否健康
     */
    public boolean isHealthy() {
        return "ACTIVE".equals(status) && 
               (lastActiveTime == null || 
                lastActiveTime.isAfter(LocalDateTime.now().minusMinutes(5)));
    }
    
    /**
     * 增加并发数
     */
    public synchronized boolean incrementConcurrent() {
        if (currentConcurrent < maxConcurrent) {
            currentConcurrent++;
            return true;
        }
        return false;
    }
    
    /**
     * 减少并发数
     */
    public synchronized void decrementConcurrent() {
        if (currentConcurrent > 0) {
            currentConcurrent--;
        }
    }
    
    /**
     * 更新活跃时间
     */
    public void updateActiveTime() {
        this.lastActiveTime = LocalDateTime.now();
    }
    
    /**
     * 更新任务统计
     */
    public void updateTaskStats(boolean success, long responseTime, BigDecimal confidence) {
        this.totalTasks++;
        this.lastTaskTime = LocalDateTime.now();
        
        if (success) {
            this.successfulTasks++;
        } else {
            this.failedTasks++;
        }
        
        // 更新成功率
        this.successRate = new BigDecimal(successfulTasks)
                .divide(new BigDecimal(totalTasks), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
        
        // 更新平均响应时间
        this.avgResponseTime = (this.avgResponseTime * (totalTasks - 1) + responseTime) / totalTasks;
        
        // 更新平均置信度
        if (confidence != null) {
            BigDecimal totalConfidence = this.avgConfidence.multiply(new BigDecimal(totalTasks - 1))
                    .add(confidence);
            this.avgConfidence = totalConfidence.divide(new BigDecimal(totalTasks), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
    
    /**
     * 重置统计数据
     */
    public void resetStats() {
        this.totalTasks = 0L;
        this.successfulTasks = 0L;
        this.failedTasks = 0L;
        this.successRate = new BigDecimal("100.00");
        this.avgResponseTime = 0L;
        this.avgConfidence = new BigDecimal("0.00");
        this.avgAccuracy = new BigDecimal("0.00");
    }
    
    /**
     * 获取负载率
     */
    public BigDecimal getLoadRate() {
        if (maxConcurrent == 0) return BigDecimal.ZERO;
        return new BigDecimal(currentConcurrent)
                .divide(new BigDecimal(maxConcurrent), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }
    
    /**
     * 获取性能评分
     */
    public BigDecimal getPerformanceScore() {
        // 综合成功率、响应时间、置信度计算性能评分
        BigDecimal successWeight = new BigDecimal("0.4");
        BigDecimal responseWeight = new BigDecimal("0.3");
        BigDecimal confidenceWeight = new BigDecimal("0.3");
        
        BigDecimal successScore = successRate;
        
        // 响应时间评分 (越快越好，以5秒为基准)
        BigDecimal responseScore = BigDecimal.ZERO;
        if (avgResponseTime > 0) {
            BigDecimal baseTime = new BigDecimal("5000"); // 5秒
            if (avgResponseTime <= 5000) {
                responseScore = new BigDecimal("100");
            } else {
                responseScore = baseTime.divide(new BigDecimal(avgResponseTime), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal("100"));
                responseScore = responseScore.min(new BigDecimal("100"));
            }
        }
        
        BigDecimal confidenceScore = avgConfidence;
        
        return successScore.multiply(successWeight)
                .add(responseScore.multiply(responseWeight))
                .add(confidenceScore.multiply(confidenceWeight));
    }
    
    /**
     * 获取健康状态描述
     */
    public String getHealthStatus() {
        if (!enabled) {
            return "已禁用";
        }
        
        if ("MAINTENANCE".equals(status)) {
            return "维护中";
        }
        
        if ("INACTIVE".equals(status)) {
            return "非活跃";
        }
        
        if (isBusy()) {
            return "繁忙";
        }
        
        if (isHealthy()) {
            return "健康";
        }
        
        return "异常";
    }
    
    /**
     * 获取响应时间描述
     */
    public String getResponseTimeDescription() {
        if (avgResponseTime == null || avgResponseTime == 0) {
            return "未知";
        }
        
        if (avgResponseTime < 1000) {
            return avgResponseTime + "毫秒";
        } else if (avgResponseTime < 60000) {
            return (avgResponseTime / 1000) + "秒";
        } else {
            long seconds = avgResponseTime / 1000;
            return (seconds / 60) + "分" + (seconds % 60) + "秒";
        }
    }
}