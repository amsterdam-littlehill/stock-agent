package com.jd.genie.agent.agent.stock;

import com.jd.genie.agent.agent.BaseAgent;
import com.jd.genie.agent.enums.AgentState;
import com.jd.genie.agent.enums.RoleType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * 股票多智能体协调器
 * 负责协调多个分析师智能体，整合分析结果，生成最终投资建议
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class StockMultiAgentCoordinator extends BaseAgent {

    private static final String COORDINATOR_NAME = "投资组合经理";
    private static final String COORDINATOR_DESCRIPTION = "协调多个分析师智能体，整合分析结果，制定最终投资策略";
    
    // 注册的分析师智能体
    private Map<String, StockAnalysisAgent> analysisAgents;
    
    // 智能体权重配置
    private Map<String, Double> agentWeights;
    
    // 执行器服务
    private ExecutorService executorService;
    
    // 分析结果缓存
    private Map<String, StockAnalysisResult> analysisResults;
    
    // 协调配置
    private int maxConcurrentAgents = 5;
    private long analysisTimeoutMs = 30000; // 30秒超时
    
    public StockMultiAgentCoordinator() {
        setName(COORDINATOR_NAME);
        setDescription(COORDINATOR_DESCRIPTION);
        
        // 初始化组件
        initializeComponents();
        
        // 设置系统提示词
        setSystemPrompt(buildSystemPrompt());
    }

    /**
     * 初始化组件
     */
    private void initializeComponents() {
        analysisAgents = new ConcurrentHashMap<>();
        agentWeights = new HashMap<>();
        analysisResults = new ConcurrentHashMap<>();
        
        // 创建线程池
        executorService = Executors.newFixedThreadPool(maxConcurrentAgents);
        
        // 注册默认分析师
        registerDefaultAgents();
        
        // 设置默认权重
        setDefaultWeights();
    }

    /**
     * 注册默认分析师智能体
     */
    private void registerDefaultAgents() {
        // 技术分析师
        TechnicalAnalystAgent technicalAgent = new TechnicalAnalystAgent();
        registerAgent("technical", technicalAgent);
        
        // 基本面分析师
        FundamentalAnalystAgent fundamentalAgent = new FundamentalAnalystAgent();
        registerAgent("fundamental", fundamentalAgent);
        
        // 可以继续添加其他分析师
        // SentimentAnalystAgent sentimentAgent = new SentimentAnalystAgent();
        // registerAgent("sentiment", sentimentAgent);
    }

    /**
     * 设置默认权重
     */
    private void setDefaultWeights() {
        agentWeights.put("technical", 0.4);     // 技术分析40%
        agentWeights.put("fundamental", 0.5);   // 基本面分析50%
        agentWeights.put("sentiment", 0.1);     // 情绪分析10%
    }

    /**
     * 注册分析师智能体
     */
    public void registerAgent(String agentType, StockAnalysisAgent agent) {
        if (agent != null) {
            // 设置相同的上下文
            agent.setContext(getContext());
            agent.setLlm(getLlm());
            
            analysisAgents.put(agentType, agent);
            log.info("注册分析师智能体: {} - {}", agentType, agent.getName());
        }
    }

    /**
     * 设置智能体权重
     */
    public void setAgentWeight(String agentType, double weight) {
        if (weight >= 0 && weight <= 1) {
            agentWeights.put(agentType, weight);
        }
    }

    /**
     * 执行多智能体分析
     */
    @Override
    public String step() {
        try {
            setState(AgentState.RUNNING);
            
            // 获取用户查询
            String userQuery = getMemory().getLastUserMessage();
            if (userQuery == null || userQuery.trim().isEmpty()) {
                setState(AgentState.ERROR);
                return "错误：未找到有效的分析请求";
            }
            
            log.info("{} {} 开始多智能体协作分析", getContext().getRequestId(), getName());
            
            // 提取股票代码
            String stockCode = extractStockCode(userQuery);
            if (stockCode == null) {
                setState(AgentState.ERROR);
                return "错误：无法从请求中提取股票代码";
            }
            
            // 解析分析参数
            Map<String, Object> analysisParams = extractAnalysisParameters(userQuery);
            
            // 并发执行多个分析师
            Map<String, StockAnalysisResult> results = executeMultiAgentAnalysis(stockCode, analysisParams);
            
            if (results.isEmpty()) {
                setState(AgentState.ERROR);
                return "分析失败：所有分析师都未能产生有效结果";
            }
            
            // 整合分析结果
            StockAnalysisResult consolidatedResult = consolidateResults(stockCode, results);
            
            // 生成最终报告
            String finalReport = generateFinalReport(consolidatedResult, results);
            
            // 更新记忆
            updateMemory(RoleType.ASSISTANT, finalReport, null);
            
            setState(AgentState.FINISHED);
            
            log.info("{} {} 完成多智能体协作分析，参与智能体数量: {}", 
                    getContext().getRequestId(), getName(), results.size());
            
            return finalReport;
            
        } catch (Exception e) {
            setState(AgentState.ERROR);
            log.error("{} {} 多智能体分析过程中发生错误", getContext().getRequestId(), getName(), e);
            return "分析失败：" + e.getMessage();
        }
    }

    /**
     * 并发执行多智能体分析
     */
    private Map<String, StockAnalysisResult> executeMultiAgentAnalysis(String stockCode, Map<String, Object> parameters) {
        Map<String, StockAnalysisResult> results = new ConcurrentHashMap<>();
        List<Future<Void>> futures = new ArrayList<>();
        
        // 为每个智能体创建分析任务
        for (Map.Entry<String, StockAnalysisAgent> entry : analysisAgents.entrySet()) {
            String agentType = entry.getKey();
            StockAnalysisAgent agent = entry.getValue();
            
            Future<Void> future = executorService.submit(() -> {
                try {
                    log.info("{} 启动{}分析", getContext().getRequestId(), agentType);
                    
                    // 执行分析
                    StockAnalysisResult result = agent.performAnalysis(stockCode, parameters);
                    
                    if (result != null && result.isValid()) {
                        results.put(agentType, result);
                        log.info("{} {}分析完成，置信度: {:.2f}", 
                                getContext().getRequestId(), agentType, result.getConfidenceScore());
                    } else {
                        log.warn("{} {}分析结果无效", getContext().getRequestId(), agentType);
                    }
                    
                } catch (Exception e) {
                    log.error("{} {}分析失败", getContext().getRequestId(), agentType, e);
                }
                return null;
            });
            
            futures.add(future);
        }
        
        // 等待所有任务完成或超时
        try {
            for (Future<Void> future : futures) {
                future.get(analysisTimeoutMs, TimeUnit.MILLISECONDS);
            }
        } catch (TimeoutException e) {
            log.warn("{} 部分分析师执行超时", getContext().getRequestId());
            // 取消未完成的任务
            futures.forEach(future -> future.cancel(true));
        } catch (Exception e) {
            log.error("{} 等待分析结果时发生错误", getContext().getRequestId(), e);
        }
        
        return results;
    }

    /**
     * 整合分析结果
     */
    private StockAnalysisResult consolidateResults(String stockCode, Map<String, StockAnalysisResult> results) {
        if (results.isEmpty()) {
            throw new RuntimeException("没有有效的分析结果可以整合");
        }
        
        // 计算加权平均置信度
        double weightedConfidence = calculateWeightedConfidence(results);
        
        // 生成综合建议
        String consolidatedRecommendation = generateConsolidatedRecommendation(results);
        
        // 评估综合风险等级
        String consolidatedRiskLevel = assessConsolidatedRiskLevel(results);
        
        // 计算综合目标价格
        Double consolidatedTargetPrice = calculateConsolidatedTargetPrice(results);
        
        // 收集所有关键要点
        List<String> allKeyPoints = collectAllKeyPoints(results);
        
        // 收集所有警告
        List<String> allWarnings = collectAllWarnings(results);
        
        // 构建综合结果
        StockAnalysisResult consolidatedResult = StockAnalysisResult.builder()
                .stockCode(stockCode)
                .stockName(getStockName(stockCode))
                .analysisType("多智能体综合分析")
                .analysisTime(LocalDateTime.now())
                .conclusion(generateConsolidatedConclusion(results, consolidatedRecommendation))
                .recommendation(consolidatedRecommendation)
                .riskLevel(consolidatedRiskLevel)
                .confidenceScore(weightedConfidence)
                .targetPrice(consolidatedTargetPrice)
                .keyPoints(allKeyPoints)
                .warnings(allWarnings)
                .analystId(getName())
                .build();
        
        // 添加各智能体的详细数据
        addDetailedAnalysisData(consolidatedResult, results);
        
        return consolidatedResult;
    }

    /**
     * 计算加权平均置信度
     */
    private double calculateWeightedConfidence(Map<String, StockAnalysisResult> results) {
        double totalWeightedConfidence = 0.0;
        double totalWeight = 0.0;
        
        for (Map.Entry<String, StockAnalysisResult> entry : results.entrySet()) {
            String agentType = entry.getKey();
            StockAnalysisResult result = entry.getValue();
            
            double weight = agentWeights.getOrDefault(agentType, 1.0);
            double confidence = result.getConfidenceScore();
            
            totalWeightedConfidence += weight * confidence;
            totalWeight += weight;
        }
        
        return totalWeight > 0 ? totalWeightedConfidence / totalWeight : 0.0;
    }

    /**
     * 生成综合投资建议
     */
    private String generateConsolidatedRecommendation(Map<String, StockAnalysisResult> results) {
        Map<String, Integer> recommendationCounts = new HashMap<>();
        Map<String, Double> recommendationWeights = new HashMap<>();
        
        // 统计各种建议的加权投票
        for (Map.Entry<String, StockAnalysisResult> entry : results.entrySet()) {
            String agentType = entry.getKey();
            StockAnalysisResult result = entry.getValue();
            String recommendation = result.getRecommendation();
            
            if (recommendation != null) {
                double weight = agentWeights.getOrDefault(agentType, 1.0);
                double confidence = result.getConfidenceScore();
                double adjustedWeight = weight * confidence;
                
                recommendationCounts.merge(recommendation, 1, Integer::sum);
                recommendationWeights.merge(recommendation, adjustedWeight, Double::sum);
            }
        }
        
        // 找出权重最高的建议
        return recommendationWeights.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("持有");
    }

    /**
     * 评估综合风险等级
     */
    private String assessConsolidatedRiskLevel(Map<String, StockAnalysisResult> results) {
        Map<String, Integer> riskCounts = new HashMap<>();
        
        for (StockAnalysisResult result : results.values()) {
            String riskLevel = result.getRiskLevel();
            if (riskLevel != null) {
                riskCounts.merge(riskLevel, 1, Integer::sum);
            }
        }
        
        // 采用保守策略：如果有高风险评估，则整体评为高风险
        if (riskCounts.getOrDefault("高", 0) > 0) {
            return "高";
        } else if (riskCounts.getOrDefault("中", 0) > 0) {
            return "中";
        } else {
            return "低";
        }
    }

    /**
     * 计算综合目标价格
     */
    private Double calculateConsolidatedTargetPrice(Map<String, StockAnalysisResult> results) {
        double totalWeightedPrice = 0.0;
        double totalWeight = 0.0;
        
        for (Map.Entry<String, StockAnalysisResult> entry : results.entrySet()) {
            String agentType = entry.getKey();
            StockAnalysisResult result = entry.getValue();
            Double targetPrice = result.getTargetPrice();
            
            if (targetPrice != null && targetPrice > 0) {
                double weight = agentWeights.getOrDefault(agentType, 1.0);
                double confidence = result.getConfidenceScore();
                double adjustedWeight = weight * confidence;
                
                totalWeightedPrice += adjustedWeight * targetPrice;
                totalWeight += adjustedWeight;
            }
        }
        
        return totalWeight > 0 ? totalWeightedPrice / totalWeight : null;
    }

    /**
     * 收集所有关键要点
     */
    private List<String> collectAllKeyPoints(Map<String, StockAnalysisResult> results) {
        List<String> allKeyPoints = new ArrayList<>();
        
        for (Map.Entry<String, StockAnalysisResult> entry : results.entrySet()) {
            String agentType = entry.getKey();
            StockAnalysisResult result = entry.getValue();
            
            if (result.getKeyPoints() != null) {
                for (String point : result.getKeyPoints()) {
                    allKeyPoints.add(String.format("[%s] %s", agentType.toUpperCase(), point));
                }
            }
        }
        
        return allKeyPoints;
    }

    /**
     * 收集所有警告
     */
    private List<String> collectAllWarnings(Map<String, StockAnalysisResult> results) {
        List<String> allWarnings = new ArrayList<>();
        
        for (Map.Entry<String, StockAnalysisResult> entry : results.entrySet()) {
            String agentType = entry.getKey();
            StockAnalysisResult result = entry.getValue();
            
            if (result.getWarnings() != null) {
                for (String warning : result.getWarnings()) {
                    allWarnings.add(String.format("[%s] %s", agentType.toUpperCase(), warning));
                }
            }
        }
        
        return allWarnings;
    }

    /**
     * 生成综合结论
     */
    private String generateConsolidatedConclusion(Map<String, StockAnalysisResult> results, String recommendation) {
        StringBuilder conclusion = new StringBuilder();
        
        conclusion.append(String.format("经过%d个专业分析师的综合评估，", results.size()));
        conclusion.append(String.format("对股票%s的投资建议为：%s。", getStockCode(), recommendation));
        
        // 添加各分析师的简要观点
        for (Map.Entry<String, StockAnalysisResult> entry : results.entrySet()) {
            String agentType = entry.getKey();
            StockAnalysisResult result = entry.getValue();
            
            conclusion.append(String.format("\n%s观点：%s（置信度：%.1f%%）", 
                    getAgentDisplayName(agentType), 
                    result.getRecommendation(),
                    result.getConfidenceScore() * 100));
        }
        
        return conclusion.toString();
    }

    /**
     * 添加详细分析数据
     */
    private void addDetailedAnalysisData(StockAnalysisResult consolidatedResult, Map<String, StockAnalysisResult> results) {
        Map<String, Object> detailedData = new HashMap<>();
        
        for (Map.Entry<String, StockAnalysisResult> entry : results.entrySet()) {
            String agentType = entry.getKey();
            StockAnalysisResult result = entry.getValue();
            
            Map<String, Object> agentData = new HashMap<>();
            agentData.put("recommendation", result.getRecommendation());
            agentData.put("confidence", result.getConfidenceScore());
            agentData.put("risk_level", result.getRiskLevel());
            agentData.put("target_price", result.getTargetPrice());
            agentData.put("conclusion", result.getConclusion());
            
            // 添加特定类型的数据
            if ("technical".equals(agentType) && result.getTechnicalIndicators() != null) {
                agentData.put("technical_indicators", result.getTechnicalIndicators());
            }
            if ("fundamental".equals(agentType) && result.getFundamentalData() != null) {
                agentData.put("fundamental_data", result.getFundamentalData());
            }
            
            detailedData.put(agentType + "_analysis", agentData);
        }
        
        consolidatedResult.setRawData(detailedData);
    }

    /**
     * 生成最终报告
     */
    private String generateFinalReport(StockAnalysisResult consolidatedResult, Map<String, StockAnalysisResult> individualResults) {
        StringBuilder report = new StringBuilder();
        
        // 报告标题
        report.append("=== 多智能体股票分析报告 ===\n");
        report.append(String.format("股票代码: %s\n", consolidatedResult.getStockCode()));
        report.append(String.format("分析时间: %s\n", consolidatedResult.getAnalysisTime()));
        report.append(String.format("参与分析师: %d位\n\n", individualResults.size()));
        
        // 综合结论
        report.append("=== 综合投资建议 ===\n");
        report.append(String.format("投资建议: %s\n", consolidatedResult.getRecommendation()));
        report.append(String.format("风险等级: %s\n", consolidatedResult.getRiskLevel()));
        report.append(String.format("综合置信度: %.1f%%\n", consolidatedResult.getConfidenceScore() * 100));
        
        if (consolidatedResult.getTargetPrice() != null) {
            report.append(String.format("目标价格: %.2f\n", consolidatedResult.getTargetPrice()));
        }
        
        report.append("\n");
        
        // 各分析师详细观点
        report.append("=== 各分析师详细观点 ===\n");
        for (Map.Entry<String, StockAnalysisResult> entry : individualResults.entrySet()) {
            String agentType = entry.getKey();
            StockAnalysisResult result = entry.getValue();
            
            report.append(String.format("\n【%s】\n", getAgentDisplayName(agentType)));
            report.append(String.format("建议: %s\n", result.getRecommendation()));
            report.append(String.format("置信度: %.1f%%\n", result.getConfidenceScore() * 100));
            report.append(String.format("分析结论: %s\n", result.getConclusion()));
            
            if (result.getKeyPoints() != null && !result.getKeyPoints().isEmpty()) {
                report.append("关键要点:\n");
                for (String point : result.getKeyPoints()) {
                    report.append(String.format("- %s\n", point));
                }
            }
        }
        
        // 风险提示
        if (consolidatedResult.getWarnings() != null && !consolidatedResult.getWarnings().isEmpty()) {
            report.append("\n=== 风险提示 ===\n");
            for (String warning : consolidatedResult.getWarnings()) {
                report.append(String.format("⚠️ %s\n", warning));
            }
        }
        
        // 免责声明
        report.append("\n=== 免责声明 ===\n");
        report.append("本分析报告仅供参考，不构成投资建议。投资有风险，入市需谨慎。\n");
        
        return report.toString();
    }

    /**
     * 获取智能体显示名称
     */
    private String getAgentDisplayName(String agentType) {
        switch (agentType) {
            case "technical":
                return "技术分析师";
            case "fundamental":
                return "基本面分析师";
            case "sentiment":
                return "情绪分析师";
            case "risk":
                return "风险管理师";
            default:
                return agentType.toUpperCase() + "分析师";
        }
    }

    /**
     * 从用户查询中提取股票代码
     */
    private String extractStockCode(String query) {
        // 复用StockAnalysisAgent中的方法
        String[] patterns = {
            "\\b\\d{6}\\.(SZ|SH)\\b",  // 000001.SZ, 600000.SH
            "\\b(SZ|SH)\\d{6}\\b",      // SZ000001, SH600000
            "\\b\\d{6}\\b",             // 000001, 600000
            "\\b[A-Z]{1,5}\\b"          // AAPL, MSFT
        };
        
        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(query.toUpperCase());
            if (m.find()) {
                return m.group();
            }
        }
        
        return null;
    }

    /**
     * 从用户查询中提取分析参数
     */
    private Map<String, Object> extractAnalysisParameters(String query) {
        Map<String, Object> params = new HashMap<>();
        
        // 提取时间范围
        if (query.contains("日线") || query.contains("日K")) {
            params.put("timeframe", "1d");
        } else if (query.contains("周线") || query.contains("周K")) {
            params.put("timeframe", "1w");
        } else if (query.contains("月线") || query.contains("月K")) {
            params.put("timeframe", "1M");
        } else {
            params.put("timeframe", "1d"); // 默认日线
        }
        
        // 提取分析深度
        if (query.contains("详细") || query.contains("深度")) {
            params.put("depth", "detailed");
        } else if (query.contains("简单") || query.contains("概要")) {
            params.put("depth", "summary");
        } else {
            params.put("depth", "normal");
        }
        
        return params;
    }

    /**
     * 获取股票名称
     */
    private String getStockName(String stockCode) {
        // 这里应该从数据源获取股票名称
        return "股票-" + stockCode;
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        return "你是一位资深的投资组合经理，负责协调多个专业分析师团队。" +
               "你擅长整合技术分析、基本面分析、情绪分析等多维度信息，" +
               "基于团队的集体智慧做出最终的投资决策。" +
               "你注重风险控制，追求稳健的投资回报，" +
               "并能够清晰地向客户解释投资逻辑和风险因素。";
    }

    /**
     * 清理资源
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}