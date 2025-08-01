package com.jd.genie.agent.orchestrator;

import com.jd.genie.agent.agent.stock.*;
import com.jd.genie.agent.orchestrator.model.*;
import com.jd.genie.agent.orchestrator.config.OrchestrationConfig;
import com.jd.genie.agent.orchestrator.exception.OrchestrationException;
import com.jd.genie.agent.orchestrator.monitor.AgentMonitor;
import com.jd.genie.agent.orchestrator.registry.AgentRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 智能体协调器
 * 统一管理和调度TradingAgents框架中的所有智能体
 * 
 * 核心功能：
 * - 异步并行执行多个智能体分析
 * - 结果聚合和冲突解决
 * - 异常处理和监控
 * - 动态配置和扩展支持
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Service
public class AgentOrchestrator {
    
    @Autowired
    private FundamentalAnalyst fundamentalAnalyst;
    
    @Autowired
    private TechnicalAnalyst technicalAnalyst;
    
    @Autowired
    private SentimentAnalyst sentimentAnalyst;
    
    @Autowired
    private RiskManager riskManager;
    
    @Autowired
    private QuantitativeAnalyst quantitativeAnalyst;
    
    @Autowired
    private MarketAnalyst marketAnalyst;
    
    @Autowired
    private InvestmentAdvisor investmentAdvisor;
    
    @Autowired
    private OrchestrationConfig config;
    
    @Autowired
    private AgentMonitor monitor;
    
    @Autowired
    private AgentRegistry agentRegistry;
    
    @Autowired
    private ThreadPoolTaskExecutor agentExecutor;
    
    /**
     * 主要分析流程 - 协调所有智能体进行综合分析
     * 
     * @param task 分析任务
     * @return 综合分析结果
     */
    public CompletableFuture<OrchestrationResult> orchestrateAnalysis(AnalysisTask task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("开始协调分析任务: {}", task.getStockCode());
                monitor.recordTaskStart(task.getTaskId(), task.getStockCode());
                
                // 1. 验证任务参数
                validateTask(task);
                
                // 2. 并行执行专业分析师
                Map<String, CompletableFuture<Object>> analystFutures = executeAnalysts(task);
                
                // 3. 等待所有分析师完成
                Map<String, Object> analystResults = waitForAnalystResults(analystFutures, task);
                
                // 4. 执行投资顾问综合分析
                Object finalAdvice = executeInvestmentAdvisor(task, analystResults);
                
                // 5. 聚合最终结果
                OrchestrationResult result = aggregateResults(task, analystResults, finalAdvice);
                
                // 6. 记录监控数据
                monitor.recordTaskComplete(task.getTaskId(), result.getExecutionTime());
                
                log.info("协调分析任务完成: {}, 耗时: {}ms", task.getStockCode(), result.getExecutionTime());
                return result;
                
            } catch (Exception e) {
                return handleOrchestrationError(task, e);
            }
        }, agentExecutor.getThreadPoolExecutor());
    }
    
    /**
     * 验证任务参数
     */
    private void validateTask(AnalysisTask task) {
        if (task == null) {
            throw new IllegalArgumentException("分析任务不能为空");
        }
        if (task.getStockCode() == null || task.getStockCode().trim().isEmpty()) {
            throw new IllegalArgumentException("股票代码不能为空");
        }
        if (task.getEnabledAnalysts().isEmpty()) {
            throw new IllegalArgumentException("至少需要启用一个分析师");
        }
    }
    
    /**
     * 并行执行专业分析师
     */
    private Map<String, CompletableFuture<Object>> executeAnalysts(AnalysisTask task) {
        Map<String, CompletableFuture<Object>> futures = new HashMap<>();
        
        for (String analystType : task.getEnabledAnalysts()) {
            futures.put(analystType, CompletableFuture.supplyAsync(() -> {
                try {
                    monitor.recordAgentStart(task.getTaskId(), analystType);
                    
                    // 根据分析师类型执行相应的分析
                    Object result = executeAnalystByType(analystType, task);
                    
                    monitor.recordAgentComplete(task.getTaskId(), analystType, System.currentTimeMillis());
                    return result;
                    
                } catch (Exception e) {
                    monitor.recordAgentError(task.getTaskId(), analystType, e.getMessage());
                    return createErrorResult(analystType, e);
                }
            }, agentExecutor.getThreadPoolExecutor()).orTimeout(
                config.getAgentTimeout(analystType), TimeUnit.MILLISECONDS
            ));
        }
        
        return futures;
    }
    
    /**
     * 根据分析师类型执行分析
     */
    private Object executeAnalystByType(String analystType, AnalysisTask task) {
        switch (analystType.toLowerCase()) {
            case "fundamental":
                return fundamentalAnalyst.analyze(task.getStockCode(), task.getContext());
            case "technical":
                return technicalAnalyst.analyze(task.getStockCode(), task.getContext());
            case "sentiment":
                return sentimentAnalyst.analyze(task.getStockCode(), task.getContext());
            case "risk":
                return riskManager.analyze(task.getStockCode(), task.getContext());
            case "quantitative":
                return quantitativeAnalyst.analyze(task.getStockCode(), task.getContext());
            case "market":
                return marketAnalyst.analyze(task.getStockCode(), task.getContext());
            default:
                throw new OrchestrationException("未知的分析师类型: " + analystType);
        }
    }
    
    /**
     * 等待所有分析师结果并处理超时
     */
    private Map<String, Object> waitForAnalystResults(
            Map<String, CompletableFuture<Object>> futures, AnalysisTask task) {
        
        Map<String, Object> results = new HashMap<>();
        
        try {
            // 等待所有任务完成或超时
            CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]))
                .get(config.getTimeout().getOverallTaskTimeout(), TimeUnit.MILLISECONDS);
            
            // 收集结果
            for (Map.Entry<String, CompletableFuture<Object>> entry : futures.entrySet()) {
                try {
                    Object result = entry.getValue().get();
                    if (!isErrorResult(result)) {
                        results.put(entry.getKey(), result);
                    }
                } catch (Exception e) {
                    log.warn("分析师 {} 执行失败: {}", entry.getKey(), e.getMessage());
                }
            }
            
        } catch (TimeoutException e) {
            log.warn("分析任务超时，部分分析师未完成");
            // 取消未完成的任务
            futures.values().forEach(future -> future.cancel(true));
        } catch (Exception e) {
            log.error("等待分析师结果时发生错误", e);
        }
        
        return results;
    }
    
    /**
     * 执行投资顾问分析
     */
    private Object executeInvestmentAdvisor(AnalysisTask task, Map<String, Object> analystResults) {
        try {
            monitor.recordAgentStart(task.getTaskId(), "advisor");
            
            // 过滤有效结果
            Map<String, Object> validResults = analystResults.entrySet().stream()
                .filter(entry -> !isErrorResult(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            
            if (validResults.isEmpty()) {
                throw new OrchestrationException("没有有效的分析师结果可供投资顾问分析");
            }
            
            Object result = investmentAdvisor.analyze(task.getStockCode(), validResults);
            
            monitor.recordAgentComplete(task.getTaskId(), "advisor", System.currentTimeMillis());
            return result;
            
        } catch (Exception e) {
            monitor.recordAgentError(task.getTaskId(), "advisor", e.getMessage());
            throw new OrchestrationException("投资顾问分析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 聚合最终结果
     */
    private OrchestrationResult aggregateResults(
            AnalysisTask task, Map<String, Object> analystResults, Object finalAdvice) {
        
        long executionTime = task.getExecutionTime();
        
        return OrchestrationResult.builder()
            .taskId(task.getTaskId())
            .stockCode(task.getStockCode())
            .analystResults(analystResults)
            .finalAdvice(finalAdvice)
            .keyMetrics(extractKeyMetrics(analystResults, finalAdvice))
            .executionSummary(generateExecutionSummary(task, analystResults, finalAdvice))
            .successRate(calculateSuccessRate(task.getEnabledAnalysts(), analystResults))
            .executionTime(executionTime)
            .timestamp(System.currentTimeMillis())
            .status(OrchestrationResult.ResultStatus.SUCCESS)
            .build();
    }
    
    /**
     * 处理协调错误
     */
    private OrchestrationResult handleOrchestrationError(AnalysisTask task, Exception e) {
        log.error("协调分析任务失败: {}", task.getStockCode(), e);
        
        return OrchestrationResult.builder()
            .taskId(task.getTaskId())
            .stockCode(task.getStockCode())
            .analystResults(new HashMap<>())
            .finalAdvice(null)
            .keyMetrics(new HashMap<>())
            .executionSummary(null)
            .successRate(0.0)
            .executionTime(task.getExecutionTime())
            .timestamp(System.currentTimeMillis())
            .status(OrchestrationResult.ResultStatus.FAILURE)
            .errorMessage(e.getMessage())
            .build();
    }
    
    /**
     * 创建错误结果
     */
    private Object createErrorResult(String analystType, Exception e) {
        return Map.of(
            "error", true,
            "analystType", analystType,
            "errorMessage", e.getMessage(),
            "timestamp", System.currentTimeMillis()
        );
    }
    
    /**
     * 检查是否为错误结果
     */
    private boolean isErrorResult(Object result) {
        if (result instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) result;
            return Boolean.TRUE.equals(map.get("error"));
        }
        return false;
    }
    
    /**
     * 提取关键指标
     */
    private Map<String, Object> extractKeyMetrics(Map<String, Object> analystResults, Object finalAdvice) {
        Map<String, Object> metrics = new HashMap<>();
        
        // 从各分析师结果中提取关键指标
        analystResults.forEach((analyst, result) -> {
            Map<String, Object> analystMetrics = extractAnalystMetrics(analyst, result);
            metrics.putAll(analystMetrics);
        });
        
        // 从最终建议中提取指标
        if (finalAdvice != null) {
            // 这里需要根据InvestmentAdvisor的实际返回结构来实现
            metrics.put("finalRecommendation", finalAdvice);
        }
        
        return metrics;
    }
    
    /**
     * 从分析师结果中提取指标
     */
    private Map<String, Object> extractAnalystMetrics(String analyst, Object result) {
        Map<String, Object> metrics = new HashMap<>();
        
        // 这里需要根据各分析师的实际返回结构来实现
        // 暂时使用通用的提取逻辑
        if (result instanceof Map) {
            Map<?, ?> resultMap = (Map<?, ?>) result;
            metrics.put(analyst + "_result", resultMap);
        } else {
            metrics.put(analyst + "_result", result);
        }
        
        return metrics;
    }
    
    /**
     * 生成执行摘要
     */
    private OrchestrationResult.ExecutionSummary generateExecutionSummary(
            AnalysisTask task, Map<String, Object> analystResults, Object finalAdvice) {
        
        List<String> successfulAnalysts = analystResults.keySet().stream()
            .filter(analyst -> !isErrorResult(analystResults.get(analyst)))
            .collect(Collectors.toList());
        
        List<String> failedAnalysts = task.getEnabledAnalysts().stream()
            .filter(analyst -> !successfulAnalysts.contains(analyst))
            .collect(Collectors.toList());
        
        return OrchestrationResult.ExecutionSummary.builder()
            .totalAnalysts(task.getEnabledAnalysts().size())
            .successfulAnalysts(successfulAnalysts.size())
            .failedAnalysts(failedAnalysts.size())
            .executionTime(task.getExecutionTime())
            .hasInvestmentAdvice(finalAdvice != null)
            .successfulAnalystsList(successfulAnalysts)
            .failedAnalystsList(failedAnalysts)
            .build();
    }
    
    /**
     * 计算成功率
     */
    private double calculateSuccessRate(List<String> enabledAnalysts, Map<String, Object> results) {
        if (enabledAnalysts.isEmpty()) {
            return 0.0;
        }
        
        long successCount = results.values().stream()
            .filter(result -> !isErrorResult(result))
            .count();
        
        return (double) successCount / enabledAnalysts.size();
    }
    
    // 公共方法 - 提供不同的分析模式
    
    /**
     * 快速分析 - 仅使用核心分析师
     */
    public CompletableFuture<OrchestrationResult> quickAnalysis(String stockCode) {
        AnalysisTask task = AnalysisTask.createQuickAnalysis(stockCode);
        return orchestrateAnalysis(task);
    }
    
    /**
     * 深度分析 - 使用所有分析师
     */
    public CompletableFuture<OrchestrationResult> deepAnalysis(String stockCode) {
        AnalysisTask task = AnalysisTask.createDeepAnalysis(stockCode);
        return orchestrateAnalysis(task);
    }
    
    /**
     * 实时分析 - 快速响应模式
     */
    public CompletableFuture<OrchestrationResult> realTimeAnalysis(String stockCode) {
        AnalysisTask task = AnalysisTask.createRealTimeAnalysis(stockCode);
        return orchestrateAnalysis(task);
    }
    
    /**
     * 自定义分析 - 用户指定分析师
     */
    public CompletableFuture<OrchestrationResult> customAnalysis(
            String stockCode, List<String> analysts, Map<String, Object> context) {
        AnalysisTask task = AnalysisTask.createCustomAnalysis(stockCode, analysts, context);
        return orchestrateAnalysis(task);
    }
    
    /**
     * 批量分析
     */
    public CompletableFuture<Map<String, OrchestrationResult>> batchAnalysis(List<String> stockCodes) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, OrchestrationResult> results = new ConcurrentHashMap<>();
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            for (String stockCode : stockCodes) {
                CompletableFuture<Void> future = quickAnalysis(stockCode)
                    .thenAccept(result -> results.put(stockCode, result));
                futures.add(future);
            }
            
            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            return results;
        }, agentExecutor.getThreadPoolExecutor());
    }
    
    /**
     * 获取协调器状态
     */
    public OrchestrationResult.OrchestratorStatus getStatus() {
        ThreadPoolExecutor executor = agentExecutor.getThreadPoolExecutor();
        
        return OrchestrationResult.OrchestratorStatus.builder()
            .activeThreads(executor.getActiveCount())
            .queueSize(executor.getQueue().size())
            .completedTasks(executor.getCompletedTaskCount())
            .registeredAgents(agentRegistry.getAllAgentTypes().size())
            .healthStatus("HEALTHY")
            .lastUpdateTime(System.currentTimeMillis())
            .build();
    }
}