package com.jd.genie.agent.agent.stock;

import com.jd.genie.agent.agent.BaseAgent;
import com.jd.genie.agent.enums.AgentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 交易执行师智能体
 * 基于TradingAgents框架的专业交易执行师角色
 * 
 * 职责：
 * - 综合决策融合
 * - 投资建议生成
 * - 仓位管理策略
 * - 风险控制措施
 * - 执行时机判断
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component("trading_executor")
public class TradingExecutor extends BaseAgent {
    
    public TradingExecutor() {
        super();
        this.agentType = AgentType.ANALYSIS;
        this.agentName = "交易执行师";
        this.description = "专业的交易执行师，擅长综合决策、投资建议和仓位管理";
    }
    
    /**
     * 执行综合决策分析
     * 
     * @param stockCode 股票代码
     * @param agentResults 各智能体分析结果
     * @param context 分析上下文
     * @return 综合决策结果
     */
    public CompletableFuture<TradingDecisionResult> synthesizeDecision(String stockCode, 
                                                                      Map<String, Object> agentResults,
                                                                      Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("交易执行师开始综合决策分析: {}", stockCode);
                
                // 1. 解析各智能体分析结果
                AgentResultsSummary resultsSummary = parseAgentResults(agentResults);
                
                // 2. 权重分配和评分融合
                WeightedScoreAnalysis weightedAnalysis = calculateWeightedScores(resultsSummary);
                
                // 3. 一致性分析
                ConsensusAnalysis consensusAnalysis = analyzeConsensus(resultsSummary);
                
                // 4. 风险评估整合
                IntegratedRiskAssessment riskAssessment = integrateRiskAssessment(resultsSummary);
                
                // 5. 投资建议生成
                InvestmentRecommendation recommendation = generateInvestmentRecommendation(
                    weightedAnalysis, consensusAnalysis, riskAssessment);
                
                // 6. 仓位管理策略
                PositionManagementStrategy positionStrategy = generatePositionStrategy(
                    recommendation, riskAssessment, context);
                
                // 7. 执行时机分析
                ExecutionTimingAnalysis timingAnalysis = analyzeExecutionTiming(
                    resultsSummary, recommendation, context);
                
                // 8. 风险控制措施
                RiskControlMeasures riskControls = generateRiskControls(
                    riskAssessment, positionStrategy, recommendation);
                
                // 9. LLM综合分析
                String llmAnalysis = generateLLMSynthesis(stockCode, resultsSummary, 
                    weightedAnalysis, consensusAnalysis, recommendation, positionStrategy);
                
                // 10. 计算最终置信度
                double finalConfidence = calculateFinalConfidence(consensusAnalysis, 
                    weightedAnalysis, riskAssessment);
                
                return TradingDecisionResult.builder()
                    .stockCode(stockCode)
                    .agentId("trading_executor")
                    .agentName("交易执行师")
                    .analysis(llmAnalysis)
                    .resultsSummary(resultsSummary)
                    .weightedAnalysis(weightedAnalysis)
                    .consensusAnalysis(consensusAnalysis)
                    .riskAssessment(riskAssessment)
                    .recommendation(recommendation)
                    .positionStrategy(positionStrategy)
                    .timingAnalysis(timingAnalysis)
                    .riskControls(riskControls)
                    .finalConfidence(finalConfidence)
                    .timestamp(System.currentTimeMillis())
                    .build();
                    
            } catch (Exception e) {
                log.error("交易执行师综合决策失败: {}", e.getMessage(), e);
                return createErrorResult("综合决策失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 参与结构化辩论（作为最终决策者）
     */
    public FundamentalAnalyst.DebateArgument debate(FundamentalAnalyst.DebateContext context, 
                                                   List<FundamentalAnalyst.DebateArgument> previousArguments) {
        try {
            // 分析所有智能体的辩论观点
            Map<String, Object> agentResults = extractResultsFromDebate(previousArguments);
            TradingDecisionResult decisionResult = synthesizeDecision(
                context.getStockCode(), agentResults, context.getContext()).get();
            
            String debatePrompt = buildFinalDebatePrompt(context.getCurrentRound(), 
                decisionResult, previousArguments);
            String argument = llmService.chat(debatePrompt);
            
            return FundamentalAnalyst.DebateArgument.builder()
                .agentId("trading_executor")
                .agentName("交易执行师")
                .round(context.getCurrentRound())
                .argument(argument)
                .confidence(decisionResult.getFinalConfidence())
                .evidenceType("COMPREHENSIVE_DECISION")
                .timestamp(System.currentTimeMillis())
                .build();
                
        } catch (Exception e) {
            log.error("交易执行师辩论失败", e);
            return createErrorDebateArgument("交易执行师辩论失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析各智能体分析结果
     */
    private AgentResultsSummary parseAgentResults(Map<String, Object> agentResults) {
        Map<String, AgentAnalysisResult> parsedResults = new HashMap<>();
        
        // 解析基本面分析结果
        if (agentResults.containsKey("fundamental_analyst")) {
            parsedResults.put("fundamental_analyst", 
                parseAnalysisResult("fundamental_analyst", agentResults.get("fundamental_analyst")));
        }
        
        // 解析技术分析结果
        if (agentResults.containsKey("technical_analyst")) {
            parsedResults.put("technical_analyst", 
                parseAnalysisResult("technical_analyst", agentResults.get("technical_analyst")));
        }
        
        // 解析情绪分析结果
        if (agentResults.containsKey("sentiment_analyst")) {
            parsedResults.put("sentiment_analyst", 
                parseAnalysisResult("sentiment_analyst", agentResults.get("sentiment_analyst")));
        }
        
        // 解析新闻分析结果
        if (agentResults.containsKey("news_analyst")) {
            parsedResults.put("news_analyst", 
                parseAnalysisResult("news_analyst", agentResults.get("news_analyst")));
        }
        
        // 解析研究分析结果
        if (agentResults.containsKey("research_analyst")) {
            parsedResults.put("research_analyst", 
                parseAnalysisResult("research_analyst", agentResults.get("research_analyst")));
        }
        
        // 解析风险管理结果
        if (agentResults.containsKey("risk_manager")) {
            parsedResults.put("risk_manager", 
                parseAnalysisResult("risk_manager", agentResults.get("risk_manager")));
        }
        
        return AgentResultsSummary.builder()
            .agentResults(parsedResults)
            .totalAgents(parsedResults.size())
            .analysisCompleteness(calculateCompleteness(parsedResults))
            .build();
    }
    
    /**
     * 解析单个智能体分析结果
     */
    private AgentAnalysisResult parseAnalysisResult(String agentId, Object result) {
        // 简化的解析逻辑，实际应根据具体结果类型进行解析
        return AgentAnalysisResult.builder()
            .agentId(agentId)
            .score(extractScore(result))
            .confidence(extractConfidence(result))
            .recommendation(extractRecommendation(result))
            .keyFindings(extractKeyFindings(result))
            .riskFactors(extractRiskFactors(result))
            .build();
    }
    
    /**
     * 计算权重评分
     */
    private WeightedScoreAnalysis calculateWeightedScores(AgentResultsSummary resultsSummary) {
        // 定义各智能体权重
        Map<String, Double> agentWeights = Map.of(
            "fundamental_analyst", 0.25,
            "technical_analyst", 0.20,
            "sentiment_analyst", 0.15,
            "news_analyst", 0.15,
            "research_analyst", 0.15,
            "risk_manager", 0.10
        );
        
        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;
        Map<String, Double> individualWeightedScores = new HashMap<>();
        
        for (Map.Entry<String, AgentAnalysisResult> entry : resultsSummary.getAgentResults().entrySet()) {
            String agentId = entry.getKey();
            AgentAnalysisResult result = entry.getValue();
            Double weight = agentWeights.getOrDefault(agentId, 0.1);
            
            double weightedScore = result.getScore() * weight;
            individualWeightedScores.put(agentId, weightedScore);
            totalWeightedScore += weightedScore;
            totalWeight += weight;
        }
        
        double finalScore = totalWeight > 0 ? totalWeightedScore / totalWeight : 0.0;
        
        return WeightedScoreAnalysis.builder()
            .agentWeights(agentWeights)
            .individualWeightedScores(individualWeightedScores)
            .totalWeightedScore(totalWeightedScore)
            .finalScore(finalScore)
            .scoreDistribution(calculateScoreDistribution(individualWeightedScores))
            .build();
    }
    
    /**
     * 分析一致性
     */
    private ConsensusAnalysis analyzeConsensus(AgentResultsSummary resultsSummary) {
        List<String> recommendations = resultsSummary.getAgentResults().values().stream()
            .map(AgentAnalysisResult::getRecommendation)
            .collect(Collectors.toList());
        
        // 统计推荐分布
        Map<String, Integer> recommendationCounts = recommendations.stream()
            .collect(Collectors.groupingBy(
                rec -> normalizeRecommendation(rec),
                Collectors.summingInt(r -> 1)
            ));
        
        // 计算一致性程度
        double consensusLevel = calculateConsensusLevel(recommendationCounts, recommendations.size());
        
        // 确定主导观点
        String dominantView = recommendationCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("NEUTRAL");
        
        // 识别分歧点
        List<String> disagreementPoints = identifyDisagreementPoints(resultsSummary);
        
        // 计算置信度一致性
        double confidenceConsistency = calculateConfidenceConsistency(resultsSummary);
        
        return ConsensusAnalysis.builder()
            .recommendationDistribution(recommendationCounts)
            .consensusLevel(consensusLevel)
            .dominantView(dominantView)
            .disagreementPoints(disagreementPoints)
            .confidenceConsistency(confidenceConsistency)
            .overallConsensus(determineOverallConsensus(consensusLevel, confidenceConsistency))
            .build();
    }
    
    /**
     * 整合风险评估
     */
    private IntegratedRiskAssessment integrateRiskAssessment(AgentResultsSummary resultsSummary) {
        List<String> allRiskFactors = new ArrayList<>();
        Map<String, Integer> riskFrequency = new HashMap<>();
        
        // 收集所有风险因素
        for (AgentAnalysisResult result : resultsSummary.getAgentResults().values()) {
            List<String> risks = result.getRiskFactors();
            allRiskFactors.addAll(risks);
            
            for (String risk : risks) {
                riskFrequency.merge(risk, 1, Integer::sum);
            }
        }
        
        // 识别主要风险
        List<String> majorRisks = riskFrequency.entrySet().stream()
            .filter(entry -> entry.getValue() >= 2) // 至少2个智能体提到
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        // 计算综合风险等级
        String overallRiskLevel = calculateOverallRiskLevel(majorRisks, riskFrequency);
        
        // 风险缓解建议
        List<String> mitigationStrategies = generateMitigationStrategies(majorRisks);
        
        return IntegratedRiskAssessment.builder()
            .allRiskFactors(allRiskFactors)
            .riskFrequency(riskFrequency)
            .majorRisks(majorRisks)
            .overallRiskLevel(overallRiskLevel)
            .mitigationStrategies(mitigationStrategies)
            .riskScore(calculateRiskScore(majorRisks, overallRiskLevel))
            .build();
    }
    
    /**
     * 生成投资建议
     */
    private InvestmentRecommendation generateInvestmentRecommendation(
            WeightedScoreAnalysis weightedAnalysis,
            ConsensusAnalysis consensusAnalysis,
            IntegratedRiskAssessment riskAssessment) {
        
        // 基于综合评分确定基础建议
        String baseRecommendation = determineBaseRecommendation(weightedAnalysis.getFinalScore());
        
        // 基于一致性调整建议
        String adjustedRecommendation = adjustRecommendationByConsensus(
            baseRecommendation, consensusAnalysis);
        
        // 基于风险进一步调整
        String finalRecommendation = adjustRecommendationByRisk(
            adjustedRecommendation, riskAssessment);
        
        // 确定投资评级
        String investmentRating = determineInvestmentRating(finalRecommendation, 
            weightedAnalysis.getFinalScore());
        
        // 计算目标价格区间
        PriceTarget priceTarget = calculatePriceTarget(weightedAnalysis, consensusAnalysis);
        
        // 投资时间框架
        String timeHorizon = determineTimeHorizon(finalRecommendation, riskAssessment);
        
        // 关键支撑论据
        List<String> supportingArguments = generateSupportingArguments(
            weightedAnalysis, consensusAnalysis);
        
        return InvestmentRecommendation.builder()
            .finalRecommendation(finalRecommendation)
            .investmentRating(investmentRating)
            .priceTarget(priceTarget)
            .timeHorizon(timeHorizon)
            .supportingArguments(supportingArguments)
            .confidenceLevel(calculateRecommendationConfidence(consensusAnalysis, weightedAnalysis))
            .build();
    }
    
    /**
     * 生成仓位管理策略
     */
    private PositionManagementStrategy generatePositionStrategy(
            InvestmentRecommendation recommendation,
            IntegratedRiskAssessment riskAssessment,
            Map<String, Object> context) {
        
        // 基于建议确定仓位大小
        double positionSize = determinePositionSize(recommendation, riskAssessment);
        
        // 分批建仓策略
        BatchingStrategy batchingStrategy = generateBatchingStrategy(
            recommendation, riskAssessment, positionSize);
        
        // 止损止盈设置
        StopLossProfit stopLossProfit = generateStopLossProfit(
            recommendation, riskAssessment);
        
        // 仓位调整规则
        List<String> adjustmentRules = generateAdjustmentRules(
            recommendation, riskAssessment);
        
        return PositionManagementStrategy.builder()
            .recommendedPositionSize(positionSize)
            .batchingStrategy(batchingStrategy)
            .stopLossProfit(stopLossProfit)
            .adjustmentRules(adjustmentRules)
            .riskBudget(calculateRiskBudget(positionSize, riskAssessment))
            .build();
    }
    
    /**
     * 分析执行时机
     */
    private ExecutionTimingAnalysis analyzeExecutionTiming(
            AgentResultsSummary resultsSummary,
            InvestmentRecommendation recommendation,
            Map<String, Object> context) {
        
        // 技术面时机分析
        String technicalTiming = analyzeTechnicalTiming(resultsSummary);
        
        // 基本面时机分析
        String fundamentalTiming = analyzeFundamentalTiming(resultsSummary);
        
        // 市场环境分析
        String marketEnvironment = analyzeMarketEnvironment(context);
        
        // 综合时机判断
        String overallTiming = determineOverallTiming(
            technicalTiming, fundamentalTiming, marketEnvironment);
        
        // 最佳执行窗口
        String executionWindow = determineExecutionWindow(overallTiming, recommendation);
        
        return ExecutionTimingAnalysis.builder()
            .technicalTiming(technicalTiming)
            .fundamentalTiming(fundamentalTiming)
            .marketEnvironment(marketEnvironment)
            .overallTiming(overallTiming)
            .executionWindow(executionWindow)
            .urgencyLevel(determineUrgencyLevel(overallTiming, recommendation))
            .build();
    }
    
    /**
     * 生成风险控制措施
     */
    private RiskControlMeasures generateRiskControls(
            IntegratedRiskAssessment riskAssessment,
            PositionManagementStrategy positionStrategy,
            InvestmentRecommendation recommendation) {
        
        // 预警机制
        List<String> alertMechanisms = generateAlertMechanisms(riskAssessment);
        
        // 监控指标
        List<String> monitoringMetrics = generateMonitoringMetrics(riskAssessment);
        
        // 应急预案
        List<String> contingencyPlans = generateContingencyPlans(riskAssessment, recommendation);
        
        // 风险限额
        Map<String, Double> riskLimits = generateRiskLimits(positionStrategy, riskAssessment);
        
        return RiskControlMeasures.builder()
            .alertMechanisms(alertMechanisms)
            .monitoringMetrics(monitoringMetrics)
            .contingencyPlans(contingencyPlans)
            .riskLimits(riskLimits)
            .reviewFrequency(determineReviewFrequency(riskAssessment))
            .build();
    }
    
    /**
     * 生成LLM综合分析
     */
    private String generateLLMSynthesis(String stockCode,
                                      AgentResultsSummary resultsSummary,
                                      WeightedScoreAnalysis weightedAnalysis,
                                      ConsensusAnalysis consensusAnalysis,
                                      InvestmentRecommendation recommendation,
                                      PositionManagementStrategy positionStrategy) {
        
        String prompt = String.format("""
            作为专业的交易执行师，请基于以下多智能体分析结果对股票 %s 进行综合决策：
            
            智能体分析汇总：
            - 参与智能体数量：%d
            - 分析完整度：%.2f%%
            - 综合评分：%.2f
            
            权重分析结果：
            - 最终加权评分：%.2f
            - 评分分布：%s
            
            一致性分析：
            - 主导观点：%s
            - 一致性水平：%.2f
            - 置信度一致性：%.2f
            - 分歧点：%s
            
            最终投资建议：
            - 投资建议：%s
            - 投资评级：%s
            - 时间框架：%s
            - 置信度：%.2f
            
            仓位管理策略：
            - 建议仓位：%.2f%%
            - 风险预算：%.2f%%
            
            请提供：
            1. 综合决策分析和逻辑
            2. 各智能体观点的权衡考虑
            3. 投资建议的核心依据
            4. 风险控制的关键要点
            5. 执行建议和注意事项
            """,
            stockCode,
            resultsSummary.getTotalAgents(),
            resultsSummary.getAnalysisCompleteness(),
            weightedAnalysis.getTotalWeightedScore(),
            weightedAnalysis.getFinalScore(),
            weightedAnalysis.getScoreDistribution(),
            consensusAnalysis.getDominantView(),
            consensusAnalysis.getConsensusLevel(),
            consensusAnalysis.getConfidenceConsistency(),
            consensusAnalysis.getDisagreementPoints(),
            recommendation.getFinalRecommendation(),
            recommendation.getInvestmentRating(),
            recommendation.getTimeHorizon(),
            recommendation.getConfidenceLevel(),
            positionStrategy.getRecommendedPositionSize(),
            positionStrategy.getRiskBudget()
        );
        
        return llmService.chat(prompt);
    }
    
    // ==================== 辅助方法 ====================
    
    private double extractScore(Object result) {
        // 简化实现，实际应根据具体结果类型提取
        return 75.0; // 默认评分
    }
    
    private double extractConfidence(Object result) {
        return 0.8; // 默认置信度
    }
    
    private String extractRecommendation(Object result) {
        return "买入"; // 默认建议
    }
    
    private List<String> extractKeyFindings(Object result) {
        return Arrays.asList("关键发现1", "关键发现2");
    }
    
    private List<String> extractRiskFactors(Object result) {
        return Arrays.asList("风险因素1", "风险因素2");
    }
    
    private double calculateCompleteness(Map<String, AgentAnalysisResult> results) {
        int expectedAgents = 6; // 期望的智能体数量
        return (double) results.size() / expectedAgents * 100;
    }
    
    private Map<String, Double> calculateScoreDistribution(Map<String, Double> scores) {
        double total = scores.values().stream().mapToDouble(Double::doubleValue).sum();
        return scores.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue() / total * 100
            ));
    }
    
    private String normalizeRecommendation(String recommendation) {
        if (recommendation.contains("买入") || recommendation.contains("推荐")) {
            return "BUY";
        } else if (recommendation.contains("卖出") || recommendation.contains("不推荐")) {
            return "SELL";
        } else {
            return "HOLD";
        }
    }
    
    private double calculateConsensusLevel(Map<String, Integer> counts, int total) {
        int maxCount = counts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        return (double) maxCount / total;
    }
    
    private List<String> identifyDisagreementPoints(AgentResultsSummary summary) {
        // 简化实现
        return Arrays.asList("估值分歧", "时机分歧");
    }
    
    private double calculateConfidenceConsistency(AgentResultsSummary summary) {
        List<Double> confidences = summary.getAgentResults().values().stream()
            .map(AgentAnalysisResult::getConfidence)
            .collect(Collectors.toList());
        
        double mean = confidences.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = confidences.stream()
            .mapToDouble(c -> Math.pow(c - mean, 2))
            .average().orElse(0.0);
        
        return 1.0 - Math.sqrt(variance); // 方差越小，一致性越高
    }
    
    private String determineOverallConsensus(double consensusLevel, double confidenceConsistency) {
        if (consensusLevel > 0.7 && confidenceConsistency > 0.8) {
            return "强一致";
        } else if (consensusLevel > 0.5 && confidenceConsistency > 0.6) {
            return "基本一致";
        } else {
            return "存在分歧";
        }
    }
    
    private String calculateOverallRiskLevel(List<String> majorRisks, Map<String, Integer> frequency) {
        int totalRiskMentions = frequency.values().stream().mapToInt(Integer::intValue).sum();
        
        if (totalRiskMentions >= 10 || majorRisks.size() >= 5) {
            return "高风险";
        } else if (totalRiskMentions >= 5 || majorRisks.size() >= 3) {
            return "中等风险";
        } else {
            return "低风险";
        }
    }
    
    private List<String> generateMitigationStrategies(List<String> majorRisks) {
        return Arrays.asList("分散投资", "设置止损", "定期评估", "动态调整");
    }
    
    private double calculateRiskScore(List<String> majorRisks, String riskLevel) {
        switch (riskLevel) {
            case "高风险": return 80.0;
            case "中等风险": return 50.0;
            case "低风险": return 20.0;
            default: return 50.0;
        }
    }
    
    private String determineBaseRecommendation(double score) {
        if (score >= 80) return "强烈买入";
        else if (score >= 65) return "买入";
        else if (score >= 45) return "持有";
        else if (score >= 30) return "卖出";
        else return "强烈卖出";
    }
    
    private String adjustRecommendationByConsensus(String baseRec, ConsensusAnalysis consensus) {
        if ("存在分歧".equals(consensus.getOverallConsensus())) {
            // 如果存在分歧，降低建议强度
            if ("强烈买入".equals(baseRec)) return "买入";
            if ("强烈卖出".equals(baseRec)) return "卖出";
        }
        return baseRec;
    }
    
    private String adjustRecommendationByRisk(String recommendation, IntegratedRiskAssessment risk) {
        if ("高风险".equals(risk.getOverallRiskLevel())) {
            // 高风险情况下降低建议强度
            if ("强烈买入".equals(recommendation)) return "谨慎买入";
            if ("买入".equals(recommendation)) return "谨慎买入";
        }
        return recommendation;
    }
    
    private String determineInvestmentRating(String recommendation, double score) {
        if (recommendation.contains("强烈买入")) return "A+";
        else if (recommendation.contains("买入")) return "A";
        else if (recommendation.contains("持有")) return "B";
        else if (recommendation.contains("卖出")) return "C";
        else return "D";
    }
    
    private PriceTarget calculatePriceTarget(WeightedScoreAnalysis weighted, ConsensusAnalysis consensus) {
        // 简化的目标价计算
        double currentPrice = 20.0; // 假设当前价格
        double upside = weighted.getFinalScore() / 100.0 * 0.3; // 最大30%涨幅
        
        return PriceTarget.builder()
            .currentPrice(currentPrice)
            .targetPrice(currentPrice * (1 + upside))
            .lowTarget(currentPrice * (1 + upside * 0.7))
            .highTarget(currentPrice * (1 + upside * 1.3))
            .priceUpside(upside * 100)
            .build();
    }
    
    private String determineTimeHorizon(String recommendation, IntegratedRiskAssessment risk) {
        if (recommendation.contains("强烈")) {
            return "3-6个月";
        } else if ("高风险".equals(risk.getOverallRiskLevel())) {
            return "1-3个月";
        } else {
            return "6-12个月";
        }
    }
    
    private List<String> generateSupportingArguments(WeightedScoreAnalysis weighted, ConsensusAnalysis consensus) {
        return Arrays.asList(
            "多智能体分析一致性较高",
            "综合评分达到投资标准",
            "风险因素可控",
            "市场时机相对合适"
        );
    }
    
    private double calculateRecommendationConfidence(ConsensusAnalysis consensus, WeightedScoreAnalysis weighted) {
        return (consensus.getConsensusLevel() + consensus.getConfidenceConsistency()) / 2.0;
    }
    
    private double determinePositionSize(InvestmentRecommendation rec, IntegratedRiskAssessment risk) {
        double baseSize = 10.0; // 基础仓位10%
        
        if (rec.getFinalRecommendation().contains("强烈")) {
            baseSize *= 1.5;
        }
        
        if ("高风险".equals(risk.getOverallRiskLevel())) {
            baseSize *= 0.5;
        } else if ("低风险".equals(risk.getOverallRiskLevel())) {
            baseSize *= 1.2;
        }
        
        return Math.min(20.0, Math.max(2.0, baseSize)); // 限制在2%-20%之间
    }
    
    private BatchingStrategy generateBatchingStrategy(InvestmentRecommendation rec, 
                                                     IntegratedRiskAssessment risk, double positionSize) {
        int batches = "高风险".equals(risk.getOverallRiskLevel()) ? 4 : 3;
        
        return BatchingStrategy.builder()
            .totalBatches(batches)
            .batchSize(positionSize / batches)
            .intervalDays(7) // 每周一批
            .strategy("等额分批")
            .build();
    }
    
    private StopLossProfit generateStopLossProfit(InvestmentRecommendation rec, IntegratedRiskAssessment risk) {
        double stopLoss = "高风险".equals(risk.getOverallRiskLevel()) ? 0.08 : 0.12; // 8%-12%止损
        double takeProfit = rec.getPriceTarget().getPriceUpside() / 100.0 * 0.8; // 80%目标价止盈
        
        return StopLossProfit.builder()
            .stopLossPercentage(stopLoss)
            .takeProfitPercentage(takeProfit)
            .trailingStop(true)
            .dynamicAdjustment(true)
            .build();
    }
    
    private List<String> generateAdjustmentRules(InvestmentRecommendation rec, IntegratedRiskAssessment risk) {
        return Arrays.asList(
            "基本面发生重大变化时减仓",
            "技术面破位时止损",
            "达到目标价位时分批获利",
            "市场系统性风险时降低仓位"
        );
    }
    
    private double calculateRiskBudget(double positionSize, IntegratedRiskAssessment risk) {
        return positionSize * 0.1; // 风险预算为仓位的10%
    }
    
    // 其他辅助方法的简化实现...
    private String analyzeTechnicalTiming(AgentResultsSummary summary) { return "技术面时机良好"; }
    private String analyzeFundamentalTiming(AgentResultsSummary summary) { return "基本面支撑"; }
    private String analyzeMarketEnvironment(Map<String, Object> context) { return "市场环境中性"; }
    private String determineOverallTiming(String tech, String fund, String market) { return "时机合适"; }
    private String determineExecutionWindow(String timing, InvestmentRecommendation rec) { return "1-2周内"; }
    private String determineUrgencyLevel(String timing, InvestmentRecommendation rec) { return "中等"; }
    
    private List<String> generateAlertMechanisms(IntegratedRiskAssessment risk) {
        return Arrays.asList("价格预警", "成交量预警", "新闻预警");
    }
    
    private List<String> generateMonitoringMetrics(IntegratedRiskAssessment risk) {
        return Arrays.asList("股价变动", "成交量", "基本面指标", "技术指标");
    }
    
    private List<String> generateContingencyPlans(IntegratedRiskAssessment risk, InvestmentRecommendation rec) {
        return Arrays.asList("快速止损预案", "分批减仓预案", "对冲保护预案");
    }
    
    private Map<String, Double> generateRiskLimits(PositionManagementStrategy pos, IntegratedRiskAssessment risk) {
        return Map.of(
            "单日最大亏损", 0.05,
            "总仓位上限", pos.getRecommendedPositionSize(),
            "止损线", pos.getStopLossProfit().getStopLossPercentage()
        );
    }
    
    private String determineReviewFrequency(IntegratedRiskAssessment risk) {
        return "高风险".equals(risk.getOverallRiskLevel()) ? "每日" : "每周";
    }
    
    private Map<String, Object> extractResultsFromDebate(List<FundamentalAnalyst.DebateArgument> arguments) {
        // 从辩论参数中提取结果
        return new HashMap<>();
    }
    
    private String buildFinalDebatePrompt(int round, TradingDecisionResult result, 
                                        List<FundamentalAnalyst.DebateArgument> arguments) {
        return String.format("""
            作为交易执行师，基于所有智能体的分析和辩论，我的最终决策是：
            
            综合评分：%.2f
            投资建议：%s
            投资评级：%s
            建议仓位：%.2f%%
            风险等级：%s
            
            决策依据：%s
            """,
            result.getWeightedAnalysis().getFinalScore(),
            result.getRecommendation().getFinalRecommendation(),
            result.getRecommendation().getInvestmentRating(),
            result.getPositionStrategy().getRecommendedPositionSize(),
            result.getRiskAssessment().getOverallRiskLevel(),
            String.join(", ", result.getRecommendation().getSupportingArguments())
        );
    }
    
    private double calculateFinalConfidence(ConsensusAnalysis consensus, 
                                          WeightedScoreAnalysis weighted,
                                          IntegratedRiskAssessment risk) {
        
        double consensusWeight = 0.4;
        double scoreWeight = 0.3;
        double riskWeight = 0.3;
        
        double consensusScore = consensus.getConsensusLevel();
        double scoreConfidence = Math.min(1.0, weighted.getFinalScore() / 100.0);
        double riskConfidence = "低风险".equals(risk.getOverallRiskLevel()) ? 0.9 : 
                               "中等风险".equals(risk.getOverallRiskLevel()) ? 0.7 : 0.5;
        
        return consensusScore * consensusWeight + 
               scoreConfidence * scoreWeight + 
               riskConfidence * riskWeight;
    }
    
    private TradingDecisionResult createErrorResult(String errorMessage) {
        return TradingDecisionResult.builder()
            .stockCode("ERROR")
            .agentId("trading_executor")
            .agentName("交易执行师")
            .analysis("综合决策失败: " + errorMessage)
            .finalConfidence(0.0)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    private FundamentalAnalyst.DebateArgument createErrorDebateArgument(String errorMessage) {
        return FundamentalAnalyst.DebateArgument.builder()
            .agentId("trading_executor")
            .agentName("交易执行师")
            .round(0)
            .argument("无法参与辩论: " + errorMessage)
            .confidence(0.0)
            .evidenceType("ERROR")
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    // ==================== 数据模型类 ====================
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TradingDecisionResult {
        private String stockCode;
        private String agentId;
        private String agentName;
        private String analysis;
        private AgentResultsSummary resultsSummary;
        private WeightedScoreAnalysis weightedAnalysis;
        private ConsensusAnalysis consensusAnalysis;
        private IntegratedRiskAssessment riskAssessment;
        private InvestmentRecommendation recommendation;
        private PositionManagementStrategy positionStrategy;
        private ExecutionTimingAnalysis timingAnalysis;
        private RiskControlMeasures riskControls;
        private Double finalConfidence;
        private Long timestamp;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AgentResultsSummary {
        private Map<String, AgentAnalysisResult> agentResults;
        private Integer totalAgents;
        private Double analysisCompleteness;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AgentAnalysisResult {
        private String agentId;
        private Double score;
        private Double confidence;
        private String recommendation;
        private List<String> keyFindings;
        private List<String> riskFactors;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WeightedScoreAnalysis {
        private Map<String, Double> agentWeights;
        private Map<String, Double> individualWeightedScores;
        private Double totalWeightedScore;
        private Double finalScore;
        private Map<String, Double> scoreDistribution;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ConsensusAnalysis {
        private Map<String, Integer> recommendationDistribution;
        private Double consensusLevel;
        private String dominantView;
        private List<String> disagreementPoints;
        private Double confidenceConsistency;
        private String overallConsensus;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IntegratedRiskAssessment {
        private List<String> allRiskFactors;
        private Map<String, Integer> riskFrequency;
        private List<String> majorRisks;
        private String overallRiskLevel;
        private List<String> mitigationStrategies;
        private Double riskScore;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class InvestmentRecommendation {
        private String finalRecommendation;
        private String investmentRating;
        private PriceTarget priceTarget;
        private String timeHorizon;
        private List<String> supportingArguments;
        private Double confidenceLevel;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PriceTarget {
        private Double currentPrice;
        private Double targetPrice;
        private Double lowTarget;
        private Double highTarget;
        private Double priceUpside;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PositionManagementStrategy {
        private Double recommendedPositionSize;
        private BatchingStrategy batchingStrategy;
        private StopLossProfit stopLossProfit;
        private List<String> adjustmentRules;
        private Double riskBudget;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BatchingStrategy {
        private Integer totalBatches;
        private Double batchSize;
        private Integer intervalDays;
        private String strategy;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StopLossProfit {
        private Double stopLossPercentage;
        private Double takeProfitPercentage;
        private Boolean trailingStop;
        private Boolean dynamicAdjustment;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ExecutionTimingAnalysis {
        private String technicalTiming;
        private String fundamentalTiming;
        private String marketEnvironment;
        private String overallTiming;
        private String executionWindow;
        private String urgencyLevel;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RiskControlMeasures {
        private List<String> alertMechanisms;
        private List<String> monitoringMetrics;
        private List<String> contingencyPlans;
        private Map<String, Double> riskLimits;
        private String reviewFrequency;
    }
}