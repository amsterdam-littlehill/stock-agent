package com.jd.genie.agent.orchestrator.model;

import com.jd.genie.agent.agent.stock.InvestmentAdvisor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 协调分析结果模型
 * 封装智能体协调器的完整分析结果
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Data
@Builder
public class OrchestrationResult {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 股票代码
     */
    private String stockCode;
    
    /**
     * 各专业分析师的分析结果
     */
    private Map<String, Object> analystResults;
    
    /**
     * 投资顾问的最终建议
     */
    private InvestmentAdvisor.InvestmentAdviceResult finalAdvice;
    
    /**
     * 关键指标汇总
     */
    private Map<String, Double> keyMetrics;
    
    /**
     * 执行摘要
     */
    private ExecutionSummary executionSummary;
    
    /**
     * 分析师执行成功率
     */
    private double successRate;
    
    /**
     * 总执行时间（毫秒）
     */
    private long executionTime;
    
    /**
     * 结果生成时间戳
     */
    private long timestamp;
    
    /**
     * 结果状态
     */
    @Builder.Default
    private ResultStatus status = ResultStatus.SUCCESS;
    
    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
    
    /**
     * 警告信息列表
     */
    private List<String> warnings;
    
    /**
     * 性能统计
     */
    private PerformanceStats performanceStats;
    
    /**
     * 获取指定分析师的结果
     */
    @SuppressWarnings("unchecked")
    public <T> T getAnalystResult(String analystType, Class<T> resultType) {
        Object result = analystResults.get(analystType);
        if (result != null && resultType.isInstance(result)) {
            return (T) result;
        }
        return null;
    }
    
    /**
     * 检查指定分析师是否执行成功
     */
    public boolean isAnalystSuccess(String analystType) {
        Object result = analystResults.get(analystType);
        return result != null && !(result instanceof ErrorResult);
    }
    
    /**
     * 获取成功的分析师列表
     */
    public List<String> getSuccessfulAnalysts() {
        return executionSummary != null ? executionSummary.getSuccessfulAnalysts() : List.of();
    }
    
    /**
     * 获取失败的分析师列表
     */
    public List<String> getFailedAnalysts() {
        return executionSummary != null ? executionSummary.getFailedAnalysts() : List.of();
    }
    
    /**
     * 获取投资建议
     */
    public String getInvestmentRecommendation() {
        if (finalAdvice != null && finalAdvice.getFinalAdvice() != null && 
            finalAdvice.getFinalAdvice().getDecision() != null) {
            return finalAdvice.getFinalAdvice().getDecision().getDecisionType();
        }
        return "无建议";
    }
    
    /**
     * 获取综合评分
     */
    public Double getOverallScore() {
        if (finalAdvice != null && finalAdvice.getAssessment() != null) {
            return finalAdvice.getAssessment().getOverallScore();
        }
        return keyMetrics.get("overall_score");
    }
    
    /**
     * 获取置信度
     */
    public Double getConfidence() {
        if (finalAdvice != null) {
            return finalAdvice.getConfidence();
        }
        return keyMetrics.get("overall_confidence");
    }
    
    /**
     * 获取预期收益
     */
    public Double getExpectedReturn() {
        return keyMetrics.get("expected_return");
    }
    
    /**
     * 获取建议仓位
     */
    public Double getRecommendedPosition() {
        return keyMetrics.get("recommended_position");
    }
    
    /**
     * 检查结果是否成功
     */
    public boolean isSuccess() {
        return status == ResultStatus.SUCCESS;
    }
    
    /**
     * 检查是否有警告
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }
    
    /**
     * 获取执行效率（成功率 * 时间效率）
     */
    public double getExecutionEfficiency() {
        if (performanceStats != null && performanceStats.getExpectedTime() > 0) {
            double timeEfficiency = (double) performanceStats.getExpectedTime() / executionTime;
            return successRate * Math.min(timeEfficiency, 1.0);
        }
        return successRate;
    }
    
    /**
     * 生成结果摘要
     */
    public String generateSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append(String.format("股票 %s 分析完成\n", stockCode));
        summary.append(String.format("执行时间: %d毫秒\n", executionTime));
        summary.append(String.format("成功率: %.1f%%\n", successRate * 100));
        
        if (getOverallScore() != null) {
            summary.append(String.format("综合评分: %.2f/10\n", getOverallScore()));
        }
        
        summary.append(String.format("投资建议: %s\n", getInvestmentRecommendation()));
        
        if (getExpectedReturn() != null) {
            summary.append(String.format("预期收益: %.2f%%\n", getExpectedReturn() * 100));
        }
        
        if (hasWarnings()) {
            summary.append(String.format("警告数量: %d\n", warnings.size()));
        }
        
        return summary.toString();
    }
    
    /**
     * 创建成功结果
     */
    public static OrchestrationResult success(String taskId, String stockCode, 
                                            Map<String, Object> analystResults,
                                            InvestmentAdvisor.InvestmentAdviceResult finalAdvice,
                                            long executionTime) {
        return OrchestrationResult.builder()
            .taskId(taskId)
            .stockCode(stockCode)
            .analystResults(analystResults)
            .finalAdvice(finalAdvice)
            .executionTime(executionTime)
            .timestamp(System.currentTimeMillis())
            .status(ResultStatus.SUCCESS)
            .build();
    }
    
    /**
     * 创建失败结果
     */
    public static OrchestrationResult failure(String taskId, String stockCode, 
                                            String errorMessage, long executionTime) {
        return OrchestrationResult.builder()
            .taskId(taskId)
            .stockCode(stockCode)
            .errorMessage(errorMessage)
            .executionTime(executionTime)
            .timestamp(System.currentTimeMillis())
            .status(ResultStatus.FAILURE)
            .successRate(0.0)
            .build();
    }
    
    /**
     * 创建部分成功结果
     */
    public static OrchestrationResult partialSuccess(String taskId, String stockCode,
                                                   Map<String, Object> analystResults,
                                                   List<String> warnings,
                                                   long executionTime) {
        return OrchestrationResult.builder()
            .taskId(taskId)
            .stockCode(stockCode)
            .analystResults(analystResults)
            .warnings(warnings)
            .executionTime(executionTime)
            .timestamp(System.currentTimeMillis())
            .status(ResultStatus.PARTIAL_SUCCESS)
            .build();
    }
    
    /**
     * 结果状态枚举
     */
    public enum ResultStatus {
        /**
         * 成功 - 所有分析师都执行成功
         */
        SUCCESS("成功"),
        
        /**
         * 部分成功 - 部分分析师执行成功
         */
        PARTIAL_SUCCESS("部分成功"),
        
        /**
         * 失败 - 分析执行失败
         */
        FAILURE("失败"),
        
        /**
         * 超时 - 执行超时
         */
        TIMEOUT("超时"),
        
        /**
         * 取消 - 任务被取消
         */
        CANCELLED("取消");
        
        private final String description;
        
        ResultStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}

/**
 * 执行摘要
 */
@Data
@Builder
class ExecutionSummary {
    /**
     * 整体状态
     */
    private String overallStatus;
    
    /**
     * 成功的分析师列表
     */
    private List<String> successfulAnalysts;
    
    /**
     * 失败的分析师列表
     */
    private List<String> failedAnalysts;
    
    /**
     * 投资建议
     */
    private String recommendation;
    
    /**
     * 关键洞察
     */
    private List<String> keyInsights;
    
    /**
     * 风险警告
     */
    private List<String> riskWarnings;
}

/**
 * 错误结果
 */
@Data
@Builder
class ErrorResult {
    /**
     * 分析师类型
     */
    private String analystType;
    
    /**
     * 错误消息
     */
    private String errorMessage;
    
    /**
     * 错误代码
     */
    private String errorCode;
    
    /**
     * 错误时间戳
     */
    private long timestamp;
    
    /**
     * 是否可重试
     */
    @Builder.Default
    private boolean retryable = true;
}

/**
 * 性能统计
 */
@Data
@Builder
class PerformanceStats {
    /**
     * 预期执行时间（毫秒）
     */
    private long expectedTime;
    
    /**
     * 实际执行时间（毫秒）
     */
    private long actualTime;
    
    /**
     * 各分析师执行时间
     */
    private Map<String, Long> analystExecutionTimes;
    
    /**
     * 内存使用峰值（字节）
     */
    private long peakMemoryUsage;
    
    /**
     * CPU使用率
     */
    private double cpuUsage;
    
    /**
     * 网络请求次数
     */
    private int networkRequests;
    
    /**
     * 缓存命中率
     */
    private double cacheHitRate;
}

/**
 * 协调器状态
 */
@Data
@Builder
class OrchestratorStatus {
    /**
     * 活跃线程数
     */
    private int activeThreads;
    
    /**
     * 队列大小
     */
    private int queueSize;
    
    /**
     * 已完成任务数
     */
    private long completedTasks;
    
    /**
     * 注册的智能体数量
     */
    private int registeredAgents;
    
    /**
     * 健康状态
     */
    private String healthStatus;
    
    /**
     * 最后更新时间
     */
    @Builder.Default
    private long lastUpdateTime = System.currentTimeMillis();
}