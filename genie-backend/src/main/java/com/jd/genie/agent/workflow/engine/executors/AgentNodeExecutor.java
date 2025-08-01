package com.jd.genie.agent.workflow.engine.executors;

import com.jd.genie.agent.orchestrator.AgentOrchestrator;
import com.jd.genie.agent.orchestrator.model.AnalysisTask;
import com.jd.genie.agent.orchestrator.model.OrchestrationResult;
import com.jd.genie.agent.workflow.engine.NodeExecutor;
import com.jd.genie.agent.workflow.model.WorkflowDefinition;
import com.jd.genie.agent.workflow.model.WorkflowExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 智能体节点执行器
 * 在工作流中调用TradingAgents智能体进行股票分析
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component
public class AgentNodeExecutor implements NodeExecutor {
    
    @Autowired
    private AgentOrchestrator agentOrchestrator;
    
    @Override
    public Map<String, Object> execute(WorkflowDefinition workflow,
                                      WorkflowExecution execution,
                                      WorkflowDefinition.WorkflowNode node,
                                      Map<String, Object> inputData) throws Exception {
        
        log.info("执行智能体节点: {} - {}", node.getNodeId(), node.getName());
        
        // 获取节点配置
        WorkflowDefinition.NodeConfig config = node.getConfig();
        if (config == null) {
            throw new IllegalArgumentException("智能体节点缺少配置信息");
        }
        
        String agentType = config.getAgentType();
        if (agentType == null || agentType.trim().isEmpty()) {
            throw new IllegalArgumentException("智能体节点缺少agentType配置");
        }
        
        // 从输入数据中获取股票代码
        String stockCode = extractStockCode(inputData, config);
        if (stockCode == null || stockCode.trim().isEmpty()) {
            throw new IllegalArgumentException("无法获取股票代码");
        }
        
        // 创建分析任务
        AnalysisTask analysisTask = createAnalysisTask(stockCode, agentType, inputData, config);
        
        // 执行智能体分析
        OrchestrationResult result = executeAgent(analysisTask, agentType);
        
        // 处理分析结果
        Map<String, Object> outputData = processAnalysisResult(result, inputData, config);
        
        log.info("智能体节点执行完成: {} - {}, 结果状态: {}", 
                node.getNodeId(), node.getName(), result.getStatus());
        
        return outputData;
    }
    
    /**
     * 提取股票代码
     */
    private String extractStockCode(Map<String, Object> inputData, WorkflowDefinition.NodeConfig config) {
        // 优先从节点配置的输入参数中获取
        if (config.getInputParameters() != null) {
            Object stockCodeParam = config.getInputParameters().get("stockCode");
            if (stockCodeParam != null) {
                return stockCodeParam.toString();
            }
        }
        
        // 从输入数据中获取
        Object stockCode = inputData.get("stockCode");
        if (stockCode != null) {
            return stockCode.toString();
        }
        
        // 从其他可能的字段中获取
        Object symbol = inputData.get("symbol");
        if (symbol != null) {
            return symbol.toString();
        }
        
        Object code = inputData.get("code");
        if (code != null) {
            return code.toString();
        }
        
        return null;
    }
    
    /**
     * 创建分析任务
     */
    private AnalysisTask createAnalysisTask(String stockCode, String agentType, 
                                           Map<String, Object> inputData, 
                                           WorkflowDefinition.NodeConfig config) {
        
        // 根据智能体类型确定分析类型
        AnalysisTask.AnalysisType analysisType = determineAnalysisType(agentType);
        
        // 创建分析任务构建器
        AnalysisTask.AnalysisTaskBuilder taskBuilder = AnalysisTask.builder()
            .stockCode(stockCode)
            .analysisType(analysisType)
            .priority(AnalysisTask.Priority.NORMAL);
        
        // 根据智能体类型启用相应的分析师
        enableAnalysts(taskBuilder, agentType);
        
        // 添加上下文数据
        if (config.getInputParameters() != null) {
            for (Map.Entry<String, Object> entry : config.getInputParameters().entrySet()) {
                taskBuilder.addContextData(entry.getKey(), entry.getValue());
            }
        }
        
        // 添加输入数据作为上下文
        for (Map.Entry<String, Object> entry : inputData.entrySet()) {
            if (!"stockCode".equals(entry.getKey()) && !"symbol".equals(entry.getKey()) && !"code".equals(entry.getKey())) {
                taskBuilder.addContextData(entry.getKey(), entry.getValue());
            }
        }
        
        return taskBuilder.build();
    }
    
    /**
     * 确定分析类型
     */
    private AnalysisTask.AnalysisType determineAnalysisType(String agentType) {
        switch (agentType.toLowerCase()) {
            case "fundamental":
            case "technical":
            case "sentiment":
            case "risk":
            case "quantitative":
            case "market":
                return AnalysisTask.AnalysisType.DEEP;
            case "advisor":
            case "investment":
                return AnalysisTask.AnalysisType.COMPREHENSIVE;
            default:
                return AnalysisTask.AnalysisType.QUICK;
        }
    }
    
    /**
     * 启用相应的分析师
     */
    private void enableAnalysts(AnalysisTask.AnalysisTaskBuilder taskBuilder, String agentType) {
        switch (agentType.toLowerCase()) {
            case "fundamental":
                taskBuilder.enableAnalyst("fundamental");
                break;
            case "technical":
                taskBuilder.enableAnalyst("technical");
                break;
            case "sentiment":
                taskBuilder.enableAnalyst("sentiment");
                break;
            case "risk":
                taskBuilder.enableAnalyst("risk");
                break;
            case "quantitative":
                taskBuilder.enableAnalyst("quantitative");
                break;
            case "market":
                taskBuilder.enableAnalyst("market");
                break;
            case "advisor":
            case "investment":
                // 投资顾问需要所有分析师的结果
                taskBuilder.enableAnalyst("fundamental")
                          .enableAnalyst("technical")
                          .enableAnalyst("sentiment")
                          .enableAnalyst("risk")
                          .enableAnalyst("quantitative")
                          .enableAnalyst("market");
                break;
            case "all":
                // 启用所有分析师
                taskBuilder.enableAnalyst("fundamental")
                          .enableAnalyst("technical")
                          .enableAnalyst("sentiment")
                          .enableAnalyst("risk")
                          .enableAnalyst("quantitative")
                          .enableAnalyst("market");
                break;
            default:
                log.warn("未知的智能体类型: {}, 使用默认配置", agentType);
                taskBuilder.enableAnalyst("fundamental")
                          .enableAnalyst("technical");
        }
    }
    
    /**
     * 执行智能体分析
     */
    private OrchestrationResult executeAgent(AnalysisTask analysisTask, String agentType) throws Exception {
        try {
            // 根据分析类型选择执行方法
            CompletableFuture<OrchestrationResult> future;
            
            switch (analysisTask.getAnalysisType()) {
                case QUICK:
                    future = agentOrchestrator.quickAnalysis(analysisTask.getStockCode());
                    break;
                case DEEP:
                    future = agentOrchestrator.deepAnalysis(analysisTask.getStockCode());
                    break;
                case COMPREHENSIVE:
                    future = agentOrchestrator.customAnalysis(analysisTask);
                    break;
                default:
                    future = agentOrchestrator.quickAnalysis(analysisTask.getStockCode());
            }
            
            // 等待分析结果
            OrchestrationResult result = future.get();
            
            if (result == null) {
                throw new RuntimeException("智能体分析返回空结果");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("智能体分析执行失败: {}", agentType, e);
            throw new RuntimeException("智能体分析执行失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 处理分析结果
     */
    private Map<String, Object> processAnalysisResult(OrchestrationResult result, 
                                                     Map<String, Object> inputData,
                                                     WorkflowDefinition.NodeConfig config) {
        
        Map<String, Object> outputData = new HashMap<>(inputData);
        
        // 添加分析结果
        outputData.put("analysisResult", result);
        outputData.put("analysisStatus", result.getStatus().toString());
        outputData.put("analysisTimestamp", result.getTimestamp());
        
        // 提取关键指标
        if (result.getKeyMetrics() != null) {
            outputData.put("keyMetrics", result.getKeyMetrics());
        }
        
        // 提取投资建议
        if (result.getInvestmentAdvice() != null) {
            outputData.put("investmentAdvice", result.getInvestmentAdvice());
            outputData.put("recommendation", result.getInvestmentAdvice().getRecommendation());
            outputData.put("confidenceLevel", result.getInvestmentAdvice().getConfidenceLevel());
            outputData.put("targetPrice", result.getInvestmentAdvice().getTargetPrice());
            outputData.put("riskLevel", result.getInvestmentAdvice().getRiskLevel());
        }
        
        // 提取各分析师结果
        if (result.getAnalystResults() != null) {
            Map<String, Object> analystResults = new HashMap<>();
            result.getAnalystResults().forEach((analyst, analysisResult) -> {
                if (analysisResult != null) {
                    Map<String, Object> analystData = new HashMap<>();
                    analystData.put("recommendation", analysisResult.getRecommendation());
                    analystData.put("confidence", analysisResult.getConfidence());
                    analystData.put("reasoning", analysisResult.getReasoning());
                    analystData.put("keyPoints", analysisResult.getKeyPoints());
                    analystResults.put(analyst, analystData);
                }
            });
            outputData.put("analystResults", analystResults);
        }
        
        // 应用输出映射
        if (config.getOutputMapping() != null) {
            Map<String, Object> mappedOutput = new HashMap<>();
            
            for (Map.Entry<String, String> mapping : config.getOutputMapping().entrySet()) {
                String targetKey = mapping.getKey();
                String sourceKey = mapping.getValue();
                
                if (outputData.containsKey(sourceKey)) {
                    mappedOutput.put(targetKey, outputData.get(sourceKey));
                }
            }
            
            // 合并映射结果
            outputData.putAll(mappedOutput);
        }
        
        // 添加执行元数据
        outputData.put("nodeType", "AGENT");
        outputData.put("agentType", config.getAgentType());
        outputData.put("executionTime", result.getExecutionTime());
        outputData.put("successRate", result.getSuccessRate());
        
        return outputData;
    }
    
    @Override
    public List<String> validateNode(WorkflowDefinition.WorkflowNode node) {
        List<String> errors = new ArrayList<>();
        
        if (node.getConfig() == null) {
            errors.add("智能体节点缺少配置信息");
            return errors;
        }
        
        WorkflowDefinition.NodeConfig config = node.getConfig();
        
        if (config.getAgentType() == null || config.getAgentType().trim().isEmpty()) {
            errors.add("智能体节点缺少agentType配置");
        } else {
            // 验证智能体类型是否支持
            String agentType = config.getAgentType().toLowerCase();
            Set<String> supportedTypes = Set.of(
                "fundamental", "technical", "sentiment", "risk", 
                "quantitative", "market", "advisor", "investment", "all"
            );
            
            if (!supportedTypes.contains(agentType)) {
                errors.add("不支持的智能体类型: " + config.getAgentType());
            }
        }
        
        return errors;
    }
    
    @Override
    public WorkflowDefinition.NodeType getSupportedNodeType() {
        return WorkflowDefinition.NodeType.AGENT;
    }
}