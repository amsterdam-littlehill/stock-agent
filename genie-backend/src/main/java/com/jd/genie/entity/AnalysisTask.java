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
import java.util.Map;

/**
 * 分析任务实体类
 */
@Entity
@Table(name = "analysis_task", indexes = {
    @Index(name = "idx_request_id", columnList = "request_id", unique = true),
    @Index(name = "idx_stock_code", columnList = "stock_code"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_priority", columnList = "priority")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 请求ID(唯一标识)
     */
    @Column(name = "request_id", nullable = false, unique = true, length = 64)
    private String requestId;
    
    /**
     * 股票代码
     */
    @Column(name = "stock_code", nullable = false, length = 20)
    private String stockCode;
    
    /**
     * 股票名称
     */
    @Column(name = "stock_name", length = 100)
    private String stockName;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", length = 64)
    private String userId;
    
    /**
     * 分析类型 (TECHNICAL-技术分析, FUNDAMENTAL-基本面分析, SENTIMENT-情绪分析, COMPREHENSIVE-综合分析)
     */
    @Column(name = "analysis_type", nullable = false, length = 50)
    private String analysisType;
    
    /**
     * 分析深度 (BASIC-基础, STANDARD-标准, DEEP-深度)
     */
    @Column(name = "analysis_depth", nullable = false, length = 20)
    @Builder.Default
    private String analysisDepth = "STANDARD";
    
    /**
     * 时间周期 (1D, 1W, 1M, 3M, 6M, 1Y)
     */
    @Column(name = "time_period", length = 10)
    @Builder.Default
    private String timePeriod = "1M";
    
    /**
     * 任务状态 (PENDING-待处理, RUNNING-执行中, COMPLETED-已完成, FAILED-失败, CANCELLED-已取消)
     */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";
    
    /**
     * 任务优先级 (1-最高, 5-最低)
     */
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 3;
    
    /**
     * 进度百分比 (0-100)
     */
    @Column(name = "progress")
    @Builder.Default
    private Integer progress = 0;
    
    /**
     * 当前执行步骤
     */
    @Column(name = "current_step", length = 200)
    private String currentStep;
    
    /**
     * 参与的智能体列表 (JSON格式)
     */
    @Column(name = "agents", columnDefinition = "TEXT")
    private String agents;
    
    /**
     * 分析参数 (JSON格式)
     */
    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;
    
    /**
     * 分析结果
     */
    @Column(name = "result", columnDefinition = "LONGTEXT")
    private String result;
    
    /**
     * 综合投资建议 (BUY-买入, HOLD-持有, SELL-卖出)
     */
    @Column(name = "recommendation", length = 20)
    private String recommendation;
    
    /**
     * 风险等级 (LOW-低风险, MEDIUM-中风险, HIGH-高风险)
     */
    @Column(name = "risk_level", length = 20)
    private String riskLevel;
    
    /**
     * 置信度 (0-100)
     */
    @Column(name = "confidence", precision = 5, scale = 2)
    private BigDecimal confidence;
    
    /**
     * 目标价格
     */
    @Column(name = "target_price", precision = 12, scale = 4)
    private BigDecimal targetPrice;
    
    /**
     * 当前价格
     */
    @Column(name = "current_price", precision = 12, scale = 4)
    private BigDecimal currentPrice;
    
    /**
     * 上涨空间(%)
     */
    @Column(name = "upside_potential", precision = 8, scale = 4)
    private BigDecimal upsidePotential;
    
    /**
     * 关键要点 (JSON格式)
     */
    @Column(name = "key_points", columnDefinition = "TEXT")
    private String keyPoints;
    
    /**
     * 风险提示 (JSON格式)
     */
    @Column(name = "risk_warnings", columnDefinition = "TEXT")
    private String riskWarnings;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 错误堆栈
     */
    @Column(name = "error_stack", columnDefinition = "TEXT")
    private String errorStack;
    
    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    /**
     * 处理耗时(毫秒)
     */
    @Column(name = "duration_ms")
    private Long durationMs;
    
    /**
     * 回调URL
     */
    @Column(name = "callback_url", length = 500)
    private String callbackUrl;
    
    /**
     * 分析标签 (JSON格式)
     */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;
    
    /**
     * 备注信息
     */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    /**
     * 数据来源
     */
    @Column(name = "data_source", length = 100)
    private String dataSource;
    
    /**
     * 重试次数
     */
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;
    
    /**
     * 最大重试次数
     */
    @Column(name = "max_retries")
    @Builder.Default
    private Integer maxRetries = 3;
    
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
     * 开始任务
     */
    public void startTask() {
        this.status = "RUNNING";
        this.startTime = LocalDateTime.now();
        this.progress = 0;
    }
    
    /**
     * 完成任务
     */
    public void completeTask() {
        this.status = "COMPLETED";
        this.endTime = LocalDateTime.now();
        this.progress = 100;
        if (this.startTime != null) {
            this.durationMs = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }
    
    /**
     * 任务失败
     */
    public void failTask(String errorMessage, String errorStack) {
        this.status = "FAILED";
        this.endTime = LocalDateTime.now();
        this.errorMessage = errorMessage;
        this.errorStack = errorStack;
        if (this.startTime != null) {
            this.durationMs = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }
    
    /**
     * 取消任务
     */
    public void cancelTask() {
        this.status = "CANCELLED";
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.durationMs = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }
    
    /**
     * 更新进度
     */
    public void updateProgress(int progress, String currentStep) {
        this.progress = Math.max(0, Math.min(100, progress));
        this.currentStep = currentStep;
    }
    
    /**
     * 增加重试次数
     */
    public boolean incrementRetryCount() {
        this.retryCount++;
        return this.retryCount <= this.maxRetries;
    }
    
    /**
     * 判断是否可以重试
     */
    public boolean canRetry() {
        return this.retryCount < this.maxRetries;
    }
    
    /**
     * 判断任务是否完成
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(this.status);
    }
    
    /**
     * 判断任务是否失败
     */
    public boolean isFailed() {
        return "FAILED".equals(this.status);
    }
    
    /**
     * 判断任务是否运行中
     */
    public boolean isRunning() {
        return "RUNNING".equals(this.status);
    }
    
    /**
     * 判断任务是否待处理
     */
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }
    
    /**
     * 判断任务是否已取消
     */
    public boolean isCancelled() {
        return "CANCELLED".equals(this.status);
    }
    
    /**
     * 获取状态显示名称
     */
    public String getStatusDisplayName() {
        switch (status) {
            case "PENDING": return "待处理";
            case "RUNNING": return "执行中";
            case "COMPLETED": return "已完成";
            case "FAILED": return "失败";
            case "CANCELLED": return "已取消";
            default: return status;
        }
    }
    
    /**
     * 获取分析类型显示名称
     */
    public String getAnalysisTypeDisplayName() {
        switch (analysisType) {
            case "TECHNICAL": return "技术分析";
            case "FUNDAMENTAL": return "基本面分析";
            case "SENTIMENT": return "情绪分析";
            case "COMPREHENSIVE": return "综合分析";
            default: return analysisType;
        }
    }
    
    /**
     * 获取分析深度显示名称
     */
    public String getAnalysisDepthDisplayName() {
        switch (analysisDepth) {
            case "BASIC": return "基础";
            case "STANDARD": return "标准";
            case "DEEP": return "深度";
            default: return analysisDepth;
        }
    }
    
    /**
     * 获取推荐操作显示名称
     */
    public String getRecommendationDisplayName() {
        if (recommendation == null) return "未知";
        switch (recommendation) {
            case "BUY": return "买入";
            case "HOLD": return "持有";
            case "SELL": return "卖出";
            default: return recommendation;
        }
    }
    
    /**
     * 获取风险等级显示名称
     */
    public String getRiskLevelDisplayName() {
        if (riskLevel == null) return "未知";
        switch (riskLevel) {
            case "LOW": return "低风险";
            case "MEDIUM": return "中风险";
            case "HIGH": return "高风险";
            default: return riskLevel;
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
     * 计算上涨空间
     */
    public void calculateUpsidePotential() {
        if (targetPrice != null && currentPrice != null && 
            currentPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal upside = targetPrice.subtract(currentPrice)
                    .divide(currentPrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            this.upsidePotential = upside;
        }
    }
    
    /**
     * 获取处理时长描述
     */
    public String getDurationDescription() {
        if (durationMs == null) return "未知";
        
        long seconds = durationMs / 1000;
        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            return (seconds / 60) + "分" + (seconds % 60) + "秒";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "小时" + minutes + "分";
        }
    }
}