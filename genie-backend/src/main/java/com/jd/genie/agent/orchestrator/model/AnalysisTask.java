package com.jd.genie.agent.orchestrator.model;

import lombok.Builder;
import lombok.Data;

import java.util.*;

/**
 * 分析任务模型
 * 封装股票分析请求的所有参数和配置
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Data
@Builder
public class AnalysisTask {
    
    /**
     * 任务唯一标识
     */
    private String taskId;
    
    /**
     * 股票代码
     */
    private String stockCode;
    
    /**
     * 股票名称
     */
    private String stockName;
    
    /**
     * 市场类型（A股、港股、美股）
     */
    private String marketType;
    
    /**
     * 分析类型（实时分析、深度分析、快速分析）
     */
    private AnalysisType analysisType;
    
    /**
     * 启用的分析师列表
     */
    @Builder.Default
    private Set<String> enabledAnalysts = new HashSet<>(Arrays.asList(
        "fundamental", "technical", "sentiment", "risk", "quantitative", "market"
    ));
    
    /**
     * 分析上下文数据
     */
    @Builder.Default
    private Map<String, Object> context = new HashMap<>();
    
    /**
     * 用户自定义参数
     */
    @Builder.Default
    private Map<String, Object> userParams = new HashMap<>();
    
    /**
     * 任务优先级（1-10，数字越大优先级越高）
     */
    @Builder.Default
    private int priority = 5;
    
    /**
     * 任务创建时间
     */
    @Builder.Default
    private long createTime = System.currentTimeMillis();
    
    /**
     * 任务开始时间
     */
    private long startTime;
    
    /**
     * 超时时间（毫秒）
     */
    @Builder.Default
    private long timeout = 60000; // 默认60秒
    
    /**
     * 是否启用结构化辩论
     */
    @Builder.Default
    private boolean enableDebate = true;
    
    /**
     * 辩论轮数
     */
    @Builder.Default
    private int debateRounds = 3;
    
    /**
     * 是否需要详细分析报告
     */
    @Builder.Default
    private boolean needDetailedReport = true;
    
    /**
     * 回调URL（异步任务完成后通知）
     */
    private String callbackUrl;
    
    /**
     * 请求来源
     */
    private String requestSource;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 检查是否启用指定分析师
     */
    public boolean isEnableAnalyst(String analystType) {
        return enabledAnalysts.contains(analystType);
    }
    
    /**
     * 启用分析师
     */
    public void enableAnalyst(String analystType) {
        enabledAnalysts.add(analystType);
    }
    
    /**
     * 禁用分析师
     */
    public void disableAnalyst(String analystType) {
        enabledAnalysts.remove(analystType);
    }
    
    /**
     * 添加上下文数据
     */
    public void addContext(String key, Object value) {
        context.put(key, value);
    }
    
    /**
     * 获取上下文数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getContext(String key, Class<T> type) {
        Object value = context.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * 添加用户参数
     */
    public void addUserParam(String key, Object value) {
        userParams.put(key, value);
    }
    
    /**
     * 获取用户参数
     */
    @SuppressWarnings("unchecked")
    public <T> T getUserParam(String key, Class<T> type) {
        Object value = userParams.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * 标记任务开始
     */
    public void markStart() {
        this.startTime = System.currentTimeMillis();
    }
    
    /**
     * 获取任务执行时长
     */
    public long getExecutionTime() {
        if (startTime > 0) {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }
    
    /**
     * 检查任务是否超时
     */
    public boolean isTimeout() {
        if (startTime > 0) {
            return getExecutionTime() > timeout;
        }
        return false;
    }
    
    /**
     * 创建快速分析任务
     */
    public static AnalysisTask createQuickTask(String stockCode) {
        return AnalysisTask.builder()
            .taskId(UUID.randomUUID().toString())
            .stockCode(stockCode)
            .analysisType(AnalysisType.QUICK)
            .enabledAnalysts(Set.of("technical", "sentiment"))
            .timeout(30000) // 30秒
            .enableDebate(false)
            .needDetailedReport(false)
            .build();
    }
    
    /**
     * 创建深度分析任务
     */
    public static AnalysisTask createDeepTask(String stockCode) {
        return AnalysisTask.builder()
            .taskId(UUID.randomUUID().toString())
            .stockCode(stockCode)
            .analysisType(AnalysisType.DEEP)
            .enabledAnalysts(Set.of("fundamental", "technical", "sentiment", "risk", "quantitative", "market"))
            .timeout(120000) // 2分钟
            .enableDebate(true)
            .debateRounds(3)
            .needDetailedReport(true)
            .build();
    }
    
    /**
     * 创建实时分析任务
     */
    public static AnalysisTask createRealTimeTask(String stockCode) {
        return AnalysisTask.builder()
            .taskId(UUID.randomUUID().toString())
            .stockCode(stockCode)
            .analysisType(AnalysisType.REALTIME)
            .enabledAnalysts(Set.of("technical", "sentiment", "market"))
            .timeout(15000) // 15秒
            .enableDebate(false)
            .needDetailedReport(false)
            .build();
    }
    
    /**
     * 创建自定义分析任务
     */
    public static AnalysisTask createCustomTask(String stockCode, Set<String> analysts, long timeout) {
        return AnalysisTask.builder()
            .taskId(UUID.randomUUID().toString())
            .stockCode(stockCode)
            .analysisType(AnalysisType.CUSTOM)
            .enabledAnalysts(new HashSet<>(analysts))
            .timeout(timeout)
            .enableDebate(analysts.size() >= 3) // 3个以上分析师才启用辩论
            .needDetailedReport(true)
            .build();
    }
    
    @Override
    public String toString() {
        return String.format("AnalysisTask{taskId='%s', stockCode='%s', type=%s, analysts=%s, timeout=%d}",
            taskId, stockCode, analysisType, enabledAnalysts, timeout);
    }
    
    /**
     * 分析类型枚举
     */
    public enum AnalysisType {
        /**
         * 快速分析 - 基础技术面和情绪面分析
         */
        QUICK("快速分析", 30),
        
        /**
         * 实时分析 - 实时数据分析
         */
        REALTIME("实时分析", 15),
        
        /**
         * 深度分析 - 全面的多维度分析
         */
        DEEP("深度分析", 120),
        
        /**
         * 自定义分析 - 用户自定义配置
         */
        CUSTOM("自定义分析", 60);
        
        private final String description;
        private final int defaultTimeoutSeconds;
        
        AnalysisType(String description, int defaultTimeoutSeconds) {
            this.description = description;
            this.defaultTimeoutSeconds = defaultTimeoutSeconds;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getDefaultTimeoutSeconds() {
            return defaultTimeoutSeconds;
        }
        
        public long getDefaultTimeoutMillis() {
            return defaultTimeoutSeconds * 1000L;
        }
    }
}