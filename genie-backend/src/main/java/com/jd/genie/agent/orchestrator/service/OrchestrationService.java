package com.jd.genie.agent.orchestrator.service;

import com.jd.genie.agent.orchestrator.AgentOrchestrator;
import com.jd.genie.agent.orchestrator.model.AnalysisTask;
import com.jd.genie.agent.orchestrator.model.OrchestrationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 智能体协调服务
 * 为前端和其他服务提供统一的智能体协调API
 * 
 * 功能：
 * - 提供多种分析模式的便捷接口
 * - 处理业务逻辑和参数验证
 * - 统一异常处理和日志记录
 * - 支持异步和同步调用模式
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Service
public class OrchestrationService {
    
    @Autowired
    private AgentOrchestrator orchestrator;
    
    /**
     * 快速股票分析
     * 使用核心分析师进行快速分析，适合实时查询
     * 
     * @param stockCode 股票代码
     * @return 分析结果
     */
    public CompletableFuture<OrchestrationResult> quickAnalysis(String stockCode) {
        log.info("开始快速分析: {}", stockCode);
        validateStockCode(stockCode);
        
        return orchestrator.quickAnalysis(stockCode)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("快速分析失败: {}", stockCode, throwable);
                } else {
                    log.info("快速分析完成: {}, 成功率: {:.2f}%", 
                        stockCode, result.getSuccessRate() * 100);
                }
            });
    }
    
    /**
     * 深度股票分析
     * 使用所有分析师进行全面分析，适合投资决策
     * 
     * @param stockCode 股票代码
     * @return 分析结果
     */
    public CompletableFuture<OrchestrationResult> deepAnalysis(String stockCode) {
        log.info("开始深度分析: {}", stockCode);
        validateStockCode(stockCode);
        
        return orchestrator.deepAnalysis(stockCode)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("深度分析失败: {}", stockCode, throwable);
                } else {
                    log.info("深度分析完成: {}, 成功率: {:.2f}%, 耗时: {}ms", 
                        stockCode, result.getSuccessRate() * 100, result.getExecutionTime());
                }
            });
    }
    
    /**
     * 实时股票分析
     * 快速响应模式，适合高频交易场景
     * 
     * @param stockCode 股票代码
     * @return 分析结果
     */
    public CompletableFuture<OrchestrationResult> realTimeAnalysis(String stockCode) {
        log.info("开始实时分析: {}", stockCode);
        validateStockCode(stockCode);
        
        return orchestrator.realTimeAnalysis(stockCode)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("实时分析失败: {}", stockCode, throwable);
                } else {
                    log.info("实时分析完成: {}, 耗时: {}ms", 
                        stockCode, result.getExecutionTime());
                }
            });
    }
    
    /**
     * 自定义股票分析
     * 用户指定分析师和参数进行个性化分析
     * 
     * @param stockCode 股票代码
     * @param analysts 启用的分析师列表
     * @param context 分析上下文
     * @return 分析结果
     */
    public CompletableFuture<OrchestrationResult> customAnalysis(
            String stockCode, List<String> analysts, Map<String, Object> context) {
        
        log.info("开始自定义分析: {}, 分析师: {}", stockCode, analysts);
        validateStockCode(stockCode);
        validateAnalysts(analysts);
        
        return orchestrator.customAnalysis(stockCode, analysts, context)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("自定义分析失败: {}", stockCode, throwable);
                } else {
                    log.info("自定义分析完成: {}, 使用分析师: {}, 成功率: {:.2f}%", 
                        stockCode, analysts, result.getSuccessRate() * 100);
                }
            });
    }
    
    /**
     * 批量股票分析
     * 并行分析多只股票，适合投资组合分析
     * 
     * @param stockCodes 股票代码列表
     * @return 批量分析结果
     */
    public CompletableFuture<Map<String, OrchestrationResult>> batchAnalysis(List<String> stockCodes) {
        log.info("开始批量分析: {} 只股票", stockCodes.size());
        validateStockCodes(stockCodes);
        
        return orchestrator.batchAnalysis(stockCodes)
            .whenComplete((results, throwable) -> {
                if (throwable != null) {
                    log.error("批量分析失败", throwable);
                } else {
                    long successCount = results.values().stream()
                        .mapToLong(result -> result.getStatus() == OrchestrationResult.ResultStatus.SUCCESS ? 1 : 0)
                        .sum();
                    log.info("批量分析完成: {}/{} 成功", successCount, stockCodes.size());
                }
            });
    }
    
    /**
     * 同步快速分析
     * 阻塞等待分析结果，适合简单的同步调用场景
     * 
     * @param stockCode 股票代码
     * @return 分析结果
     */
    public OrchestrationResult quickAnalysisSync(String stockCode) {
        try {
            return quickAnalysis(stockCode).get();
        } catch (Exception e) {
            log.error("同步快速分析失败: {}", stockCode, e);
            throw new RuntimeException("分析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 同步深度分析
     * 阻塞等待分析结果，适合需要完整结果的场景
     * 
     * @param stockCode 股票代码
     * @return 分析结果
     */
    public OrchestrationResult deepAnalysisSync(String stockCode) {
        try {
            return deepAnalysis(stockCode).get();
        } catch (Exception e) {
            log.error("同步深度分析失败: {}", stockCode, e);
            throw new RuntimeException("分析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取协调器状态
     * 用于监控和健康检查
     * 
     * @return 协调器状态
     */
    public OrchestrationResult.OrchestratorStatus getOrchestratorStatus() {
        return orchestrator.getStatus();
    }
    
    /**
     * 检查协调器健康状态
     * 
     * @return 是否健康
     */
    public boolean isHealthy() {
        try {
            OrchestrationResult.OrchestratorStatus status = getOrchestratorStatus();
            return "HEALTHY".equals(status.getHealthStatus());
        } catch (Exception e) {
            log.error("检查协调器健康状态失败", e);
            return false;
        }
    }
    
    /**
     * 创建自定义分析任务
     * 提供更细粒度的任务控制
     * 
     * @param stockCode 股票代码
     * @param analysisType 分析类型
     * @param analysts 分析师列表
     * @param context 上下文数据
     * @param priority 优先级
     * @return 分析结果
     */
    public CompletableFuture<OrchestrationResult> createCustomTask(
            String stockCode, 
            AnalysisTask.AnalysisType analysisType,
            List<String> analysts,
            Map<String, Object> context,
            int priority) {
        
        log.info("创建自定义分析任务: {}, 类型: {}, 优先级: {}", stockCode, analysisType, priority);
        
        AnalysisTask task = AnalysisTask.builder()
            .stockCode(stockCode)
            .analysisType(analysisType)
            .enabledAnalysts(analysts)
            .context(context)
            .priority(priority)
            .build();
        
        return orchestrator.orchestrateAnalysis(task)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("自定义任务执行失败: {}", stockCode, throwable);
                } else {
                    log.info("自定义任务完成: {}, 任务ID: {}", stockCode, result.getTaskId());
                }
            });
    }
    
    // ==================== 私有验证方法 ====================
    
    /**
     * 验证股票代码
     */
    private void validateStockCode(String stockCode) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            throw new IllegalArgumentException("股票代码不能为空");
        }
        
        // 简单的股票代码格式验证
        String code = stockCode.trim().toUpperCase();
        if (!code.matches("^[A-Z0-9]{2,10}$")) {
            throw new IllegalArgumentException("股票代码格式不正确: " + stockCode);
        }
    }
    
    /**
     * 验证股票代码列表
     */
    private void validateStockCodes(List<String> stockCodes) {
        if (stockCodes == null || stockCodes.isEmpty()) {
            throw new IllegalArgumentException("股票代码列表不能为空");
        }
        
        if (stockCodes.size() > 100) {
            throw new IllegalArgumentException("批量分析最多支持100只股票");
        }
        
        for (String stockCode : stockCodes) {
            validateStockCode(stockCode);
        }
    }
    
    /**
     * 验证分析师列表
     */
    private void validateAnalysts(List<String> analysts) {
        if (analysts == null || analysts.isEmpty()) {
            throw new IllegalArgumentException("分析师列表不能为空");
        }
        
        List<String> validAnalysts = List.of(
            "fundamental", "technical", "sentiment", 
            "risk", "quantitative", "market"
        );
        
        for (String analyst : analysts) {
            if (!validAnalysts.contains(analyst.toLowerCase())) {
                throw new IllegalArgumentException("不支持的分析师类型: " + analyst);
            }
        }
    }
}