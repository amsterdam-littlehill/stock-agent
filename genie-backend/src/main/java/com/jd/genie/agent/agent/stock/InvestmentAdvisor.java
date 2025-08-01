package com.jd.genie.agent.agent.stock;

import com.jd.genie.agent.agent.BaseAgent;
import com.jd.genie.agent.enums.AgentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 投资顾问智能体
 * 基于TradingAgents框架的专业投资顾问角色
 * 
 * 职责：
 * - 综合各专业分析师意见
 * - 制定投资策略
 * - 风险收益平衡
 * - 投资组合建议
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component("investment_advisor")
public class InvestmentAdvisor extends BaseAgent {
    
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
    private NewsAnalyst newsAnalyst;
    
    @Autowired
    private ResearchAnalyst researchAnalyst;
    
    @Autowired
    private TradingExecutor tradingExecutor;
    
    @Autowired
    private com.jd.genie.agent.collaboration.StructuredDebateEngine structuredDebateEngine;
    
    public InvestmentAdvisor() {
        super();
        this.agentType = AgentType.ADVISOR;
        this.agentName = "投资顾问";
        this.description = "专业的投资顾问，综合各专业分析师意见，提供最终投资建议";
    }
    
    /**
     * 执行综合投资分析
     * 
     * @param stockCode 股票代码
     * @param context 分析上下文
     * @return 投资建议结果
     */
    public CompletableFuture<InvestmentAdviceResult> analyze(String stockCode, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("投资顾问开始综合分析股票: {}", stockCode);
                
                // 1. 收集各专业分析师的分析结果
                Map<String, Object> analysisResults = collectAnalysisResults(stockCode, context);
                
                // 2. 执行结构化辩论
                DebateResult debateResult = conductStructuredDebate(stockCode, analysisResults);
                
                // 3. 综合评估
                ComprehensiveAssessment assessment = performComprehensiveAssessment(analysisResults, debateResult);
                
                // 4. 投资策略制定
                InvestmentStrategy strategy = formulateInvestmentStrategy(stockCode, assessment, analysisResults);
                
                // 5. 风险收益分析
                RiskReturnProfile riskReturnProfile = analyzeRiskReturnProfile(assessment, strategy);
                
                // 6. 投资组合建议
                PortfolioRecommendation portfolioRecommendation = generatePortfolioRecommendation(stockCode, strategy, riskReturnProfile);
                
                // 7. 投资时机分析
                TimingAnalysis timingAnalysis = analyzeInvestmentTiming(analysisResults, assessment);
                
                // 8. 生成最终投资建议
                FinalInvestmentAdvice finalAdvice = generateFinalInvestmentAdvice(stockCode, assessment, strategy, 
                                                                                 riskReturnProfile, portfolioRecommendation, timingAnalysis);
                
                // 9. 生成LLM综合分析
                String llmAnalysis = generateLLMAnalysis(stockCode, analysisResults, debateResult, assessment, finalAdvice);
                
                return InvestmentAdviceResult.builder()
                        .stockCode(stockCode)
                        .agentId("investment_advisor")
                        .agentName("投资顾问")
                        .analysis(llmAnalysis)
                        .analysisResults(analysisResults)
                        .debateResult(debateResult)
                        .assessment(assessment)
                        .strategy(strategy)
                        .riskReturnProfile(riskReturnProfile)
                        .portfolioRecommendation(portfolioRecommendation)
                        .timingAnalysis(timingAnalysis)
                        .finalAdvice(finalAdvice)
                        .confidence(calculateConfidence(assessment, debateResult))
                        .timestamp(System.currentTimeMillis())
                        .build();
                
            } catch (Exception e) {
                log.error("投资顾问分析失败: {}", stockCode, e);
                return createErrorResult("分析过程中发生错误: " + e.getMessage());
            }
        });
    }
    
    /**
     * 收集各专业分析师的分析结果
     */
    private Map<String, Object> collectAnalysisResults(String stockCode, Map<String, Object> context) {
        Map<String, Object> results = new HashMap<>();
        
        try {
            // 并行执行各专业分析师的分析
            CompletableFuture<FundamentalAnalyst.FundamentalAnalysisResult> fundamentalFuture = 
                fundamentalAnalyst.analyze(stockCode, context);
            
            CompletableFuture<TechnicalAnalyst.TechnicalAnalysisResult> technicalFuture = 
                technicalAnalyst.analyze(stockCode, context);
            
            CompletableFuture<SentimentAnalyst.SentimentAnalysisResult> sentimentFuture = 
                sentimentAnalyst.analyze(stockCode, context);
            
            CompletableFuture<RiskManager.RiskAnalysisResult> riskFuture = 
                riskManager.analyze(stockCode, context);
            
            CompletableFuture<QuantitativeAnalyst.QuantitativeAnalysisResult> quantFuture = 
                quantitativeAnalyst.analyze(stockCode, context);
            
            CompletableFuture<MarketAnalyst.MarketAnalysisResult> marketFuture = 
                marketAnalyst.analyze(stockCode, context);
            
            CompletableFuture<NewsAnalyst.NewsAnalysisResult> newsFuture = 
                newsAnalyst.analyze(stockCode, context);
            
            CompletableFuture<ResearchAnalyst.ResearchAnalysisResult> researchFuture = 
                researchAnalyst.analyze(stockCode, context);
            
            CompletableFuture<TradingExecutor.TradingDecisionResult> tradingFuture = 
                tradingExecutor.synthesizeDecision(stockCode, context);
            
            // 等待所有分析完成
            CompletableFuture.allOf(fundamentalFuture, technicalFuture, sentimentFuture, 
                                  riskFuture, quantFuture, marketFuture, newsFuture, researchFuture, tradingFuture).join();
            
            // 收集结果
            results.put("fundamental_analyst", fundamentalFuture.get());
            results.put("technical_analyst", technicalFuture.get());
            results.put("sentiment_analyst", sentimentFuture.get());
            results.put("risk_manager", riskFuture.get());
            results.put("quantitative_analyst", quantFuture.get());
            results.put("market_analyst", marketFuture.get());
            results.put("news_analyst", newsFuture.get());
            results.put("research_analyst", researchFuture.get());
            results.put("trading_executor", tradingFuture.get());
            
        } catch (Exception e) {
            log.error("收集分析结果失败", e);
            // 返回空结果，后续处理会处理这种情况
        }
        
        return results;
    }
    
    /**
     * 执行结构化辩论
     */
    private DebateResult conductStructuredDebate(String stockCode, Map<String, Object> analysisResults) {
        try {
            // 使用专门的结构化辩论引擎
            com.jd.genie.agent.collaboration.StructuredDebateEngine.DebateResult engineResult = 
                    structuredDebateEngine.conductStructuredDebate(stockCode, analysisResults, 3);
            
            // 转换为InvestmentAdvisor的DebateResult格式
            return DebateResult.builder()
                    .allArguments(engineResult.getAllArguments())
                    .consensus(DebateConsensus.builder()
                            .consensusLevel(engineResult.getConsensus().getConsensusLevel())
                            .consensusType(engineResult.getConsensus().getConsensusType())
                            .majorityView(engineResult.getConsensus().getMajorityView())
                            .disagreementPoints(engineResult.getConsensus().getDisagreementPoints())
                            .build())
                    .keyInsights(engineResult.getKeyInsights())
                    .agentAgreement(engineResult.getAgentAgreement())
                    .debateQuality(engineResult.getDebateQuality())
                    .build();
                    
        } catch (Exception e) {
            log.error("结构化辩论执行失败: {}", stockCode, e);
            
            // 返回错误结果
            return DebateResult.builder()
                    .allArguments(Collections.emptyList())
                    .consensus(DebateConsensus.builder()
                            .consensusLevel(0.0)
                            .consensusType("辩论失败")
                            .majorityView("无法确定")
                            .disagreementPoints(Arrays.asList("辩论过程中发生错误: " + e.getMessage()))
                            .build())
                    .keyInsights(Arrays.asList("辩论执行失败，请检查系统状态"))
                    .agentAgreement(Collections.emptyMap())
                    .debateQuality(0.0)
                    .build();
        }
    }
    
    /**
     * 综合评估
     */
    private ComprehensiveAssessment performComprehensiveAssessment(Map<String, Object> analysisResults, DebateResult debateResult) {
        // 收集各维度评分
        Map<String, Double> dimensionScores = collectDimensionScores(analysisResults);
        
        // 计算权重
        Map<String, Double> weights = calculateDimensionWeights(analysisResults, debateResult);
        
        // 计算综合评分
        double overallScore = calculateOverallScore(dimensionScores, weights);
        
        // 识别关键优势
        List<String> keyStrengths = identifyKeyStrengths(analysisResults, dimensionScores);
        
        // 识别关键风险
        List<String> keyRisks = identifyKeyRisks(analysisResults, dimensionScores);
        
        // 不确定性分析
        UncertaintyAnalysis uncertaintyAnalysis = analyzeUncertainty(analysisResults, debateResult);
        
        // 敏感性分析
        SensitivityAnalysis sensitivityAnalysis = performSensitivityAnalysis(dimensionScores, weights);
        
        return ComprehensiveAssessment.builder()
                .dimensionScores(dimensionScores)
                .weights(weights)
                .overallScore(overallScore)
                .keyStrengths(keyStrengths)
                .keyRisks(keyRisks)
                .uncertaintyAnalysis(uncertaintyAnalysis)
                .sensitivityAnalysis(sensitivityAnalysis)
                .assessmentConfidence(calculateAssessmentConfidence(analysisResults, debateResult))
                .build();
    }
    
    /**
     * 制定投资策略
     */
    private InvestmentStrategy formulateInvestmentStrategy(String stockCode, ComprehensiveAssessment assessment, Map<String, Object> analysisResults) {
        // 确定投资目标
        InvestmentObjective objective = determineInvestmentObjective(assessment);
        
        // 制定策略类型
        StrategyType strategyType = determineStrategyType(assessment, analysisResults);
        
        // 仓位建议
        PositionSizing positionSizing = calculatePositionSizing(assessment, objective);
        
        // 进入策略
        EntryStrategy entryStrategy = formulateEntryStrategy(assessment, analysisResults);
        
        // 退出策略
        ExitStrategy exitStrategy = formulateExitStrategy(assessment, objective);
        
        // 风险控制
        RiskControl riskControl = designRiskControl(assessment, positionSizing);
        
        // 监控指标
        List<String> monitoringIndicators = defineMonitoringIndicators(analysisResults);
        
        return InvestmentStrategy.builder()
                .objective(objective)
                .strategyType(strategyType)
                .positionSizing(positionSizing)
                .entryStrategy(entryStrategy)
                .exitStrategy(exitStrategy)
                .riskControl(riskControl)
                .monitoringIndicators(monitoringIndicators)
                .strategyRationale(generateStrategyRationale(assessment, objective, strategyType))
                .build();
    }
    
    /**
     * 分析风险收益特征
     */
    private RiskReturnProfile analyzeRiskReturnProfile(ComprehensiveAssessment assessment, InvestmentStrategy strategy) {
        // 预期收益分析
        ExpectedReturn expectedReturn = calculateExpectedReturn(assessment, strategy);
        
        // 风险分析
        RiskProfile riskProfile = analyzeRiskProfile(assessment, strategy);
        
        // 风险调整收益
        double riskAdjustedReturn = calculateRiskAdjustedReturn(expectedReturn, riskProfile);
        
        // 最大回撤预估
        double maxDrawdownEstimate = estimateMaxDrawdown(riskProfile, strategy);
        
        // 胜率分析
        WinRateAnalysis winRateAnalysis = analyzeWinRate(assessment, strategy);
        
        // 收益分布
        ReturnDistribution returnDistribution = analyzeReturnDistribution(expectedReturn, riskProfile);
        
        return RiskReturnProfile.builder()
                .expectedReturn(expectedReturn)
                .riskProfile(riskProfile)
                .riskAdjustedReturn(riskAdjustedReturn)
                .maxDrawdownEstimate(maxDrawdownEstimate)
                .winRateAnalysis(winRateAnalysis)
                .returnDistribution(returnDistribution)
                .sharpeRatio(calculateSharpeRatio(expectedReturn, riskProfile))
                .build();
    }
    
    /**
     * 生成投资组合建议
     */
    private PortfolioRecommendation generatePortfolioRecommendation(String stockCode, InvestmentStrategy strategy, RiskReturnProfile riskReturnProfile) {
        // 资产配置建议
        AssetAllocation assetAllocation = recommendAssetAllocation(strategy, riskReturnProfile);
        
        // 相关性分析
        CorrelationAnalysis correlationAnalysis = analyzeCorrelation(stockCode);
        
        // 分散化建议
        DiversificationAdvice diversificationAdvice = provideDiversificationAdvice(stockCode, correlationAnalysis);
        
        // 再平衡策略
        RebalancingStrategy rebalancingStrategy = designRebalancingStrategy(assetAllocation, strategy);
        
        // 组合优化
        PortfolioOptimization optimization = optimizePortfolio(assetAllocation, riskReturnProfile);
        
        return PortfolioRecommendation.builder()
                .assetAllocation(assetAllocation)
                .correlationAnalysis(correlationAnalysis)
                .diversificationAdvice(diversificationAdvice)
                .rebalancingStrategy(rebalancingStrategy)
                .optimization(optimization)
                .portfolioRisk(calculatePortfolioRisk(assetAllocation, correlationAnalysis))
                .build();
    }
    
    /**
     * 分析投资时机
     */
    private TimingAnalysis analyzeInvestmentTiming(Map<String, Object> analysisResults, ComprehensiveAssessment assessment) {
        // 技术面时机
        TechnicalTiming technicalTiming = analyzeTechnicalTiming(analysisResults);
        
        // 基本面时机
        FundamentalTiming fundamentalTiming = analyzeFundamentalTiming(analysisResults);
        
        // 市场时机
        MarketTiming marketTiming = analyzeMarketTiming(analysisResults);
        
        // 情绪面时机
        SentimentTiming sentimentTiming = analyzeSentimentTiming(analysisResults);
        
        // 综合时机评分
        double overallTimingScore = calculateOverallTimingScore(technicalTiming, fundamentalTiming, marketTiming, sentimentTiming);
        
        // 最佳进入时间窗口
        TimeWindow optimalEntryWindow = determineOptimalEntryWindow(technicalTiming, fundamentalTiming, marketTiming);
        
        return TimingAnalysis.builder()
                .technicalTiming(technicalTiming)
                .fundamentalTiming(fundamentalTiming)
                .marketTiming(marketTiming)
                .sentimentTiming(sentimentTiming)
                .overallTimingScore(overallTimingScore)
                .optimalEntryWindow(optimalEntryWindow)
                .timingConfidence(calculateTimingConfidence(overallTimingScore, assessment))
                .build();
    }
    
    /**
     * 生成最终投资建议
     */
    private FinalInvestmentAdvice generateFinalInvestmentAdvice(String stockCode, ComprehensiveAssessment assessment, 
                                                              InvestmentStrategy strategy, RiskReturnProfile riskReturnProfile,
                                                              PortfolioRecommendation portfolioRecommendation, TimingAnalysis timingAnalysis) {
        
        // 确定投资决策
        InvestmentDecision decision = makeInvestmentDecision(assessment, strategy, timingAnalysis);
        
        // 目标价位
        PriceTarget priceTarget = calculatePriceTarget(assessment, strategy);
        
        // 持有期建议
        HoldingPeriod holdingPeriod = recommendHoldingPeriod(strategy, riskReturnProfile);
        
        // 关键监控点
        List<String> keyMonitoringPoints = identifyKeyMonitoringPoints(assessment, strategy);
        
        // 调整触发条件
        List<String> adjustmentTriggers = defineAdjustmentTriggers(assessment, strategy);
        
        // 替代方案
        List<String> alternatives = suggestAlternatives(stockCode, assessment);
        
        return FinalInvestmentAdvice.builder()
                .decision(decision)
                .priceTarget(priceTarget)
                .holdingPeriod(holdingPeriod)
                .keyMonitoringPoints(keyMonitoringPoints)
                .adjustmentTriggers(adjustmentTriggers)
                .alternatives(alternatives)
                .confidenceLevel(calculateAdviceConfidence(assessment, strategy, timingAnalysis))
                .riskWarning(generateRiskWarning(riskReturnProfile, assessment))
                .actionPlan(generateActionPlan(decision, strategy, timingAnalysis))
                .build();
    }
    
    /**
     * 生成LLM综合分析
     */
    private String generateLLMAnalysis(String stockCode, Map<String, Object> analysisResults, DebateResult debateResult, 
                                     ComprehensiveAssessment assessment, FinalInvestmentAdvice finalAdvice) {
        
        StringBuilder prompt = new StringBuilder();
        
        prompt.append(String.format("""
            作为专业的投资顾问，请基于以下综合分析对股票 %s 提供最终投资建议：
            
            综合评估结果：
            - 综合评分：%.2f/10
            - 评估置信度：%.2f
            
            各维度评分：
            """, stockCode, assessment.getOverallScore(), assessment.getAssessmentConfidence()));
        
        // 添加各维度评分
        for (Map.Entry<String, Double> entry : assessment.getDimensionScores().entrySet()) {
            prompt.append(String.format("- %s：%.2f/10\n", entry.getKey(), entry.getValue()));
        }
        
        prompt.append(String.format("""
            
            关键优势：
            %s
            
            关键风险：
            %s
            
            辩论共识：
            - 共识度：%.2f
            - 辩论质量：%.2f
            - 关键洞察：%s
            
            投资建议：
            - 投资决策：%s
            - 目标价位：%.2f - %.2f
            - 建议持有期：%s
            - 建议仓位：%.1f%%
            - 预期收益：%.2f%%
            - 风险等级：%s
            
            投资时机：
            - 时机评分：%.2f/10
            - 最佳进入窗口：%s
            
            请从以下角度提供专业的投资建议：
            1. 投资决策的核心逻辑
            2. 风险收益权衡分析
            3. 投资时机和策略建议
            4. 关键监控指标和调整条件
            5. 投资组合配置建议
            6. 风险提示和注意事项
            
            请提供专业、客观、实用的投资建议，字数控制在800字以内。
            """,
            String.join(", ", assessment.getKeyStrengths()),
            String.join(", ", assessment.getKeyRisks()),
            debateResult.getConsensus() != null ? debateResult.getConsensus().getConsensusLevel() : 0.0,
            debateResult.getDebateQuality(),
            String.join(", ", debateResult.getKeyInsights()),
            finalAdvice.getDecision().getDecisionType(),
            finalAdvice.getPriceTarget().getTargetLow(),
            finalAdvice.getPriceTarget().getTargetHigh(),
            finalAdvice.getHoldingPeriod().getRecommendedPeriod(),
            finalAdvice.getDecision().getRecommendedPosition() * 100,
            finalAdvice.getDecision().getExpectedReturn() * 100,
            finalAdvice.getDecision().getRiskLevel(),
            assessment.getOverallScore(), // 使用综合评分作为时机评分的简化
            finalAdvice.getPriceTarget().getOptimalEntryRange()
        ));
        
        return callLLM(prompt.toString());
    }
    
    // ==================== 工具方法 ====================
    
    private Map<String, Double> collectDimensionScores(Map<String, Object> analysisResults) {
        Map<String, Double> scores = new HashMap<>();
        
        // 基本面评分
        if (analysisResults.containsKey("fundamental_analyst")) {
            FundamentalAnalyst.FundamentalAnalysisResult fundamental = 
                (FundamentalAnalyst.FundamentalAnalysisResult) analysisResults.get("fundamental_analyst");
            scores.put("基本面", fundamental.getFundamentalScore());
        }
        
        // 技术面评分
        if (analysisResults.containsKey("technical_analyst")) {
            TechnicalAnalyst.TechnicalAnalysisResult technical = 
                (TechnicalAnalyst.TechnicalAnalysisResult) analysisResults.get("technical_analyst");
            scores.put("技术面", technical.getTechnicalScore());
        }
        
        // 情绪面评分
        if (analysisResults.containsKey("sentiment_analyst")) {
            SentimentAnalyst.SentimentAnalysisResult sentiment = 
                (SentimentAnalyst.SentimentAnalysisResult) analysisResults.get("sentiment_analyst");
            scores.put("情绪面", sentiment.getSentimentScore());
        }
        
        // 风险评分
        if (analysisResults.containsKey("risk_manager")) {
            RiskManager.RiskAnalysisResult risk = 
                (RiskManager.RiskAnalysisResult) analysisResults.get("risk_manager");
            scores.put("风险控制", 10.0 - risk.getOverallRiskScore()); // 风险越低评分越高
        }
        
        // 量化评分
        if (analysisResults.containsKey("quantitative_analyst")) {
            QuantitativeAnalyst.QuantitativeAnalysisResult quant = 
                (QuantitativeAnalyst.QuantitativeAnalysisResult) analysisResults.get("quantitative_analyst");
            scores.put("量化分析", quant.getQuantScore());
        }
        
        // 市场评分
        if (analysisResults.containsKey("market_analyst")) {
            MarketAnalyst.MarketAnalysisResult market = 
                (MarketAnalyst.MarketAnalysisResult) analysisResults.get("market_analyst");
            scores.put("市场环境", market.getMarketScore());
        }
        
        return scores;
    }
    
    private Map<String, Double> calculateDimensionWeights(Map<String, Object> analysisResults, DebateResult debateResult) {
        Map<String, Double> weights = new HashMap<>();
        
        // 基础权重
        weights.put("基本面", 0.25);
        weights.put("技术面", 0.20);
        weights.put("市场环境", 0.20);
        weights.put("风险控制", 0.15);
        weights.put("量化分析", 0.10);
        weights.put("情绪面", 0.10);
        
        // 根据辩论结果调整权重
        if (debateResult.getAgentAgreement() != null) {
            for (Map.Entry<String, Double> entry : debateResult.getAgentAgreement().entrySet()) {
                String agentId = entry.getKey();
                Double agreement = entry.getValue();
                
                // 一致性高的分析师权重增加
                if (agreement > 0.8) {
                    String dimension = mapAgentToDimension(agentId);
                    if (weights.containsKey(dimension)) {
                        weights.put(dimension, weights.get(dimension) * 1.2);
                    }
                }
            }
        }
        
        // 归一化权重
        double totalWeight = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        weights.replaceAll((k, v) -> v / totalWeight);
        
        return weights;
    }
    
    private String mapAgentToDimension(String agentId) {
        switch (agentId) {
            case "fundamental_analyst": return "基本面";
            case "technical_analyst": return "技术面";
            case "sentiment_analyst": return "情绪面";
            case "risk_manager": return "风险控制";
            case "quantitative_analyst": return "量化分析";
            case "market_analyst": return "市场环境";
            default: return "其他";
        }
    }
    
    private double calculateOverallScore(Map<String, Double> dimensionScores, Map<String, Double> weights) {
        double score = 0.0;
        
        for (Map.Entry<String, Double> entry : dimensionScores.entrySet()) {
            String dimension = entry.getKey();
            Double dimensionScore = entry.getValue();
            Double weight = weights.getOrDefault(dimension, 0.0);
            
            score += dimensionScore * weight;
        }
        
        return Math.max(0, Math.min(10, score));
    }
    
    private List<String> identifyKeyStrengths(Map<String, Object> analysisResults, Map<String, Double> dimensionScores) {
        List<String> strengths = new ArrayList<>();
        
        for (Map.Entry<String, Double> entry : dimensionScores.entrySet()) {
            if (entry.getValue() >= 7.0) {
                strengths.add(entry.getKey() + "表现优秀(" + String.format("%.1f", entry.getValue()) + "/10)");
            }
        }
        
        // 从具体分析结果中提取优势
        if (analysisResults.containsKey("fundamental_analyst")) {
            FundamentalAnalyst.FundamentalAnalysisResult fundamental = 
                (FundamentalAnalyst.FundamentalAnalysisResult) analysisResults.get("fundamental_analyst");
            if (fundamental.getFundamentalScore() >= 7.0) {
                strengths.add("财务状况良好");
                strengths.add("盈利能力强");
            }
        }
        
        if (analysisResults.containsKey("technical_analyst")) {
            TechnicalAnalyst.TechnicalAnalysisResult technical = 
                (TechnicalAnalyst.TechnicalAnalysisResult) analysisResults.get("technical_analyst");
            if (technical.getTechnicalScore() >= 7.0) {
                strengths.add("技术形态良好");
                strengths.add("趋势向上");
            }
        }
        
        return strengths.isEmpty() ? List.of("暂无明显优势") : strengths;
    }
    
    private List<String> identifyKeyRisks(Map<String, Object> analysisResults, Map<String, Double> dimensionScores) {
        List<String> risks = new ArrayList<>();
        
        for (Map.Entry<String, Double> entry : dimensionScores.entrySet()) {
            if (entry.getValue() <= 4.0) {
                risks.add(entry.getKey() + "存在风险(" + String.format("%.1f", entry.getValue()) + "/10)");
            }
        }
        
        // 从风险分析结果中提取风险
        if (analysisResults.containsKey("risk_manager")) {
            RiskManager.RiskAnalysisResult risk = 
                (RiskManager.RiskAnalysisResult) analysisResults.get("risk_manager");
            if (risk.getOverallRiskScore() >= 7.0) {
                risks.add("整体风险较高");
            }
        }
        
        return risks.isEmpty() ? List.of("风险可控") : risks;
    }
    
    private DebateConsensus analyzeDebateConsensus(List<FundamentalAnalyst.DebateArgument> arguments) {
        if (arguments.isEmpty()) {
            return DebateConsensus.builder()
                    .consensusLevel(0.5)
                    .consensusType("无共识")
                    .majorityView("无明确观点")
                    .disagreementPoints(new ArrayList<>())
                    .build();
        }
        
        // 分析观点一致性
        Map<String, Integer> viewCounts = new HashMap<>();
        for (FundamentalAnalyst.DebateArgument arg : arguments) {
            String view = extractViewFromArgument(arg.getArgument());
            viewCounts.put(view, viewCounts.getOrDefault(view, 0) + 1);
        }
        
        // 找出主流观点
        String majorityView = viewCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("无明确观点");
        
        // 计算共识度
        int totalArguments = arguments.size();
        int majorityCount = viewCounts.getOrDefault(majorityView, 0);
        double consensusLevel = (double) majorityCount / totalArguments;
        
        // 确定共识类型
        String consensusType;
        if (consensusLevel >= 0.8) consensusType = "强共识";
        else if (consensusLevel >= 0.6) consensusType = "一般共识";
        else if (consensusLevel >= 0.4) consensusType = "弱共识";
        else consensusType = "分歧较大";
        
        // 识别分歧点
        List<String> disagreementPoints = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : viewCounts.entrySet()) {
            if (!entry.getKey().equals(majorityView) && entry.getValue() >= 2) {
                disagreementPoints.add(entry.getKey());
            }
        }
        
        return DebateConsensus.builder()
                .consensusLevel(consensusLevel)
                .consensusType(consensusType)
                .majorityView(majorityView)
                .disagreementPoints(disagreementPoints)
                .build();
    }
    
    private String extractViewFromArgument(String argument) {
        // 简化实现：从论据中提取观点
        if (argument.contains("推荐") || argument.contains("买入") || argument.contains("看好")) {
            return "看多";
        } else if (argument.contains("不推荐") || argument.contains("卖出") || argument.contains("看空")) {
            return "看空";
        } else if (argument.contains("观望") || argument.contains("持有") || argument.contains("中性")) {
            return "中性";
        } else {
            return "不明确";
        }
    }
    
    private List<String> extractKeyInsights(List<FundamentalAnalyst.DebateArgument> arguments) {
        List<String> insights = new ArrayList<>();
        
        // 从辩论中提取关键洞察（简化实现）
        Set<String> uniqueInsights = new HashSet<>();
        
        for (FundamentalAnalyst.DebateArgument arg : arguments) {
            String argument = arg.getArgument();
            
            // 提取关键词和短语
            if (argument.contains("估值")) uniqueInsights.add("估值分析是关键因素");
            if (argument.contains("趋势")) uniqueInsights.add("技术趋势值得关注");
            if (argument.contains("风险")) uniqueInsights.add("风险控制至关重要");
            if (argument.contains("政策")) uniqueInsights.add("政策影响不容忽视");
            if (argument.contains("业绩")) uniqueInsights.add("业绩表现是核心");
            if (argument.contains("市场")) uniqueInsights.add("市场环境影响显著");
        }
        
        insights.addAll(uniqueInsights);
        
        return insights.isEmpty() ? List.of("需要综合考虑多个因素") : new ArrayList<>(insights);
    }
    
    private Map<String, Double> calculateAgentAgreement(List<FundamentalAnalyst.DebateArgument> arguments) {
        Map<String, Double> agreement = new HashMap<>();
        
        // 计算各分析师观点的一致性（简化实现）
        Map<String, List<String>> agentViews = new HashMap<>();
        
        for (FundamentalAnalyst.DebateArgument arg : arguments) {
            String agentId = arg.getAgentId();
            String view = extractViewFromArgument(arg.getArgument());
            
            agentViews.computeIfAbsent(agentId, k -> new ArrayList<>()).add(view);
        }
        
        // 计算每个分析师的一致性
        for (Map.Entry<String, List<String>> entry : agentViews.entrySet()) {
            String agentId = entry.getKey();
            List<String> views = entry.getValue();
            
            if (views.isEmpty()) {
                agreement.put(agentId, 0.5);
                continue;
            }
            
            // 计算观点一致性
            Map<String, Long> viewCounts = views.stream()
                    .collect(Collectors.groupingBy(v -> v, Collectors.counting()));
            
            long maxCount = viewCounts.values().stream().mapToLong(Long::longValue).max().orElse(0);
            double consistency = (double) maxCount / views.size();
            
            agreement.put(agentId, consistency);
        }
        
        return agreement;
    }
    
    private double assessDebateQuality(List<FundamentalAnalyst.DebateArgument> arguments) {
        if (arguments.isEmpty()) return 0.0;
        
        double quality = 5.0; // 基础分
        
        // 参与度评分
        Set<String> participatingAgents = arguments.stream()
                .map(FundamentalAnalyst.DebateArgument::getAgentId)
                .collect(Collectors.toSet());
        
        quality += participatingAgents.size() * 0.5; // 参与分析师越多质量越高
        
        // 论据质量评分
        double avgConfidence = arguments.stream()
                .mapToDouble(FundamentalAnalyst.DebateArgument::getConfidence)
                .average()
                .orElse(0.5);
        
        quality += avgConfidence * 2; // 置信度越高质量越高
        
        // 轮次完整性
        long rounds = arguments.stream()
                .mapToInt(FundamentalAnalyst.DebateArgument::getRound)
                .distinct()
                .count();
        
        if (rounds >= 3) quality += 1.0; // 完整的3轮辩论
        
        return Math.max(0, Math.min(10, quality));
    }
    
    // 简化实现的其他方法
    
    private UncertaintyAnalysis analyzeUncertainty(Map<String, Object> analysisResults, DebateResult debateResult) {
        double uncertaintyLevel = 1.0 - debateResult.getConsensus().getConsensusLevel();
        
        return UncertaintyAnalysis.builder()
                .uncertaintyLevel(uncertaintyLevel)
                .uncertaintySources(List.of("市场波动", "政策变化", "业绩不确定性"))
                .confidenceInterval("±" + String.format("%.1f", uncertaintyLevel * 20) + "%")
                .build();
    }
    
    private SensitivityAnalysis performSensitivityAnalysis(Map<String, Double> dimensionScores, Map<String, Double> weights) {
        Map<String, Double> sensitivity = new HashMap<>();
        
        for (String dimension : dimensionScores.keySet()) {
            double weight = weights.getOrDefault(dimension, 0.0);
            sensitivity.put(dimension, weight * 10); // 简化计算
        }
        
        return SensitivityAnalysis.builder()
                .sensitivity(sensitivity)
                .mostSensitiveFactor(sensitivity.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("未知"))
                .build();
    }
    
    private double calculateAssessmentConfidence(Map<String, Object> analysisResults, DebateResult debateResult) {
        double confidence = 0.5; // 基础置信度
        
        // 分析师参与度
        confidence += analysisResults.size() * 0.05;
        
        // 辩论质量
        confidence += debateResult.getDebateQuality() * 0.03;
        
        // 共识度
        confidence += debateResult.getConsensus().getConsensusLevel() * 0.2;
        
        return Math.max(0.1, Math.min(1.0, confidence));
    }
    
    // 投资策略相关方法的简化实现
    
    private InvestmentObjective determineInvestmentObjective(ComprehensiveAssessment assessment) {
        String objectiveType;
        String riskTolerance;
        String timeHorizon;
        
        if (assessment.getOverallScore() >= 8.0) {
            objectiveType = "积极成长";
            riskTolerance = "高";
            timeHorizon = "中长期";
        } else if (assessment.getOverallScore() >= 6.0) {
            objectiveType = "稳健增值";
            riskTolerance = "中等";
            timeHorizon = "中期";
        } else {
            objectiveType = "保守投资";
            riskTolerance = "低";
            timeHorizon = "短期";
        }
        
        return InvestmentObjective.builder()
                .objectiveType(objectiveType)
                .riskTolerance(riskTolerance)
                .timeHorizon(timeHorizon)
                .expectedReturn(assessment.getOverallScore() * 0.02) // 简化计算
                .build();
    }
    
    private StrategyType determineStrategyType(ComprehensiveAssessment assessment, Map<String, Object> analysisResults) {
        String strategyName;
        String strategyDescription;
        
        if (assessment.getOverallScore() >= 8.0) {
            strategyName = "积极买入";
            strategyDescription = "综合评分优秀，建议积极配置";
        } else if (assessment.getOverallScore() >= 7.0) {
            strategyName = "买入";
            strategyDescription = "综合评分良好，建议买入";
        } else if (assessment.getOverallScore() >= 6.0) {
            strategyName = "谨慎买入";
            strategyDescription = "综合评分一般，谨慎买入";
        } else if (assessment.getOverallScore() >= 4.0) {
            strategyName = "持有观望";
            strategyDescription = "综合评分偏低，建议观望";
        } else {
            strategyName = "减仓";
            strategyDescription = "综合评分较低，建议减仓";
        }
        
        return StrategyType.builder()
                .strategyName(strategyName)
                .strategyDescription(strategyDescription)
                .applicableScenarios(List.of("当前市场环境"))
                .build();
    }
    
    private PositionSizing calculatePositionSizing(ComprehensiveAssessment assessment, InvestmentObjective objective) {
        double recommendedPosition;
        double maxPosition;
        double minPosition;
        
        if (assessment.getOverallScore() >= 8.0) {
            recommendedPosition = 0.15; // 15%
            maxPosition = 0.20;
            minPosition = 0.10;
        } else if (assessment.getOverallScore() >= 7.0) {
            recommendedPosition = 0.10; // 10%
            maxPosition = 0.15;
            minPosition = 0.05;
        } else if (assessment.getOverallScore() >= 6.0) {
            recommendedPosition = 0.05; // 5%
            maxPosition = 0.10;
            minPosition = 0.02;
        } else {
            recommendedPosition = 0.02; // 2%
            maxPosition = 0.05;
            minPosition = 0.01;
        }
        
        return PositionSizing.builder()
                .recommendedPosition(recommendedPosition)
                .maxPosition(maxPosition)
                .minPosition(minPosition)
                .sizingRationale("基于综合评分和风险评估")
                .build();
    }
    
    private double calculateConfidence(ComprehensiveAssessment assessment, DebateResult debateResult) {
        double confidence = 0.5; // 基础置信度
        
        // 评估置信度贡献
        confidence += assessment.getAssessmentConfidence() * 0.3;
        
        // 辩论质量贡献
        confidence += debateResult.getDebateQuality() * 0.02;
        
        // 共识度贡献
        confidence += debateResult.getConsensus().getConsensusLevel() * 0.2;
        
        return Math.max(0.1, Math.min(1.0, confidence));
    }
    
    private InvestmentAdviceResult createErrorResult(String errorMessage) {
        return InvestmentAdviceResult.builder()
                .stockCode("")
                .agentId("investment_advisor")
                .agentName("投资顾问")
                .analysis("分析失败: " + errorMessage)
                .analysisResults(new HashMap<>())
                .debateResult(null)
                .assessment(null)
                .strategy(null)
                .riskReturnProfile(null)
                .portfolioRecommendation(null)
                .timingAnalysis(null)
                .finalAdvice(null)
                .confidence(0.0)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    // 其他简化实现的方法...
    
    private EntryStrategy formulateEntryStrategy(ComprehensiveAssessment assessment, Map<String, Object> analysisResults) {
        return EntryStrategy.builder()
                .entryMethod("分批建仓")
                .entryTiming("技术回调时")
                .entryPrice("当前价格±5%")
                .entryConditions(List.of("技术指标确认", "成交量配合"))
                .build();
    }
    
    private ExitStrategy formulateExitStrategy(ComprehensiveAssessment assessment, InvestmentObjective objective) {
        return ExitStrategy.builder()
                .profitTarget(assessment.getOverallScore() * 0.05) // 简化计算
                .stopLoss(0.15) // 15%止损
                .exitConditions(List.of("达到目标价位", "基本面恶化", "技术破位"))
                .exitMethod("分批减仓")
                .build();
    }
    
    private RiskControl designRiskControl(ComprehensiveAssessment assessment, PositionSizing positionSizing) {
        return RiskControl.builder()
                .maxLoss(positionSizing.getRecommendedPosition() * 0.2) // 最大损失
                .stopLossLevel(0.15)
                .riskMonitoring(List.of("价格变动", "成交量异常", "基本面变化"))
                .riskAdjustment("动态调整仓位")
                .build();
    }
    
    private List<String> defineMonitoringIndicators(Map<String, Object> analysisResults) {
        return List.of(
            "股价变动",
            "成交量变化",
            "技术指标",
            "基本面数据",
            "市场情绪",
            "行业动态",
            "政策变化"
        );
    }
    
    private String generateStrategyRationale(ComprehensiveAssessment assessment, InvestmentObjective objective, StrategyType strategyType) {
        return String.format(
            "基于综合评分%.2f/10，%s目标，采用%s策略。主要考虑因素包括：%s。",
            assessment.getOverallScore(),
            objective.getObjectiveType(),
            strategyType.getStrategyName(),
            String.join("、", assessment.getKeyStrengths())
        );
    }
    
    // 继续添加其他简化实现的方法...
    
    private ExpectedReturn calculateExpectedReturn(ComprehensiveAssessment assessment, InvestmentStrategy strategy) {
        double expectedReturn = assessment.getOverallScore() * 0.02; // 简化计算
        
        return ExpectedReturn.builder()
                .expectedReturn(expectedReturn)
                .bestCase(expectedReturn * 1.5)
                .worstCase(expectedReturn * 0.5)
                .probability(0.6)
                .timeFrame(strategy.getObjective().getTimeHorizon())
                .build();
    }
    
    private RiskProfile analyzeRiskProfile(ComprehensiveAssessment assessment, InvestmentStrategy strategy) {
        double riskLevel = (10.0 - assessment.getOverallScore()) / 10.0; // 评分越低风险越高
        
        return RiskProfile.builder()
                .overallRisk(riskLevel)
                .volatility(riskLevel * 0.3)
                .maxDrawdown(riskLevel * 0.25)
                .riskFactors(assessment.getKeyRisks())
                .build();
    }
    
    private double calculateRiskAdjustedReturn(ExpectedReturn expectedReturn, RiskProfile riskProfile) {
        if (riskProfile.getVolatility() == 0) return 0;
        return expectedReturn.getExpectedReturn() / riskProfile.getVolatility();
    }
    
    private double estimateMaxDrawdown(RiskProfile riskProfile, InvestmentStrategy strategy) {
        return riskProfile.getMaxDrawdown();
    }
    
    private WinRateAnalysis analyzeWinRate(ComprehensiveAssessment assessment, InvestmentStrategy strategy) {
        double winRate = 0.5 + (assessment.getOverallScore() - 5.0) * 0.05; // 简化计算
        
        return WinRateAnalysis.builder()
                .expectedWinRate(Math.max(0.3, Math.min(0.8, winRate)))
                .avgWin(0.15)
                .avgLoss(0.08)
                .winLossRatio(0.15 / 0.08)
                .build();
    }
    
    private ReturnDistribution analyzeReturnDistribution(ExpectedReturn expectedReturn, RiskProfile riskProfile) {
        return ReturnDistribution.builder()
                .mean(expectedReturn.getExpectedReturn())
                .standardDeviation(riskProfile.getVolatility())
                .skewness(0.0) // 假设正态分布
                .kurtosis(3.0) // 正态分布的峰度
                .build();
    }
    
    private double calculateSharpeRatio(ExpectedReturn expectedReturn, RiskProfile riskProfile) {
        double riskFreeRate = 0.03; // 假设无风险利率3%
        if (riskProfile.getVolatility() == 0) return 0;
        return (expectedReturn.getExpectedReturn() - riskFreeRate) / riskProfile.getVolatility();
    }
    
    // 投资组合相关方法的简化实现
    
    private AssetAllocation recommendAssetAllocation(InvestmentStrategy strategy, RiskReturnProfile riskReturnProfile) {
        return AssetAllocation.builder()
                .stockAllocation(strategy.getPositionSizing().getRecommendedPosition())
                .bondAllocation(0.3)
                .cashAllocation(0.1)
                .alternativeAllocation(0.05)
                .allocationRationale("基于风险收益特征的资产配置")
                .build();
    }
    
    private CorrelationAnalysis analyzeCorrelation(String stockCode) {
        return CorrelationAnalysis.builder()
                .marketCorrelation(0.7) // 假设与市场相关性
                .sectorCorrelation(0.8) // 与行业相关性
                .correlationStability("稳定")
                .diversificationBenefit("中等")
                .build();
    }
    
    private DiversificationAdvice provideDiversificationAdvice(String stockCode, CorrelationAnalysis correlationAnalysis) {
        return DiversificationAdvice.builder()
                .diversificationLevel("适度分散")
                .recommendedSectors(List.of("科技", "消费", "医疗"))
                .avoidSectors(List.of("高相关行业"))
                .diversificationStrategy("跨行业配置")
                .build();
    }
    
    private RebalancingStrategy designRebalancingStrategy(AssetAllocation assetAllocation, InvestmentStrategy strategy) {
        return RebalancingStrategy.builder()
                .rebalancingFrequency("季度")
                .rebalancingTrigger("偏离目标配置±5%")
                .rebalancingMethod("阈值再平衡")
                .rebalancingCost("0.1%")
                .build();
    }
    
    private PortfolioOptimization optimizePortfolio(AssetAllocation assetAllocation, RiskReturnProfile riskReturnProfile) {
        return PortfolioOptimization.builder()
                .optimizationMethod("均值方差优化")
                .expectedReturn(riskReturnProfile.getExpectedReturn().getExpectedReturn())
                .expectedRisk(riskReturnProfile.getRiskProfile().getOverallRisk())
                .sharpeRatio(riskReturnProfile.getSharpeRatio())
                .optimizationConstraints(List.of("单一资产不超过20%", "现金不低于5%"))
                .build();
    }
    
    private double calculatePortfolioRisk(AssetAllocation assetAllocation, CorrelationAnalysis correlationAnalysis) {
        // 简化的组合风险计算
        return assetAllocation.getStockAllocation() * correlationAnalysis.getMarketCorrelation() * 0.2;
    }
    
    // 时机分析相关方法
    
    private TechnicalTiming analyzeTechnicalTiming(Map<String, Object> analysisResults) {
        if (analysisResults.containsKey("technical_analyst")) {
            TechnicalAnalyst.TechnicalAnalysisResult technical = 
                (TechnicalAnalyst.TechnicalAnalysisResult) analysisResults.get("technical_analyst");
            
            return TechnicalTiming.builder()
                    .timingScore(technical.getTechnicalScore())
                    .trendDirection("上升")
                    .momentumStrength("强")
                    .supportResistance("支撑位附近")
                    .build();
        }
        
        return TechnicalTiming.builder()
                .timingScore(5.0)
                .trendDirection("中性")
                .momentumStrength("中等")
                .supportResistance("无明确")
                .build();
    }
    
    private FundamentalTiming analyzeFundamentalTiming(Map<String, Object> analysisResults) {
        if (analysisResults.containsKey("fundamental_analyst")) {
            FundamentalAnalyst.FundamentalAnalysisResult fundamental = 
                (FundamentalAnalyst.FundamentalAnalysisResult) analysisResults.get("fundamental_analyst");
            
            return FundamentalTiming.builder()
                    .timingScore(fundamental.getFundamentalScore())
                    .valuationLevel("合理")
                    .earningsGrowth("稳定")
                    .businessCycle("成长期")
                    .build();
        }
        
        return FundamentalTiming.builder()
                .timingScore(5.0)
                .valuationLevel("中性")
                .earningsGrowth("未知")
                .businessCycle("未知")
                .build();
    }
    
    private MarketTiming analyzeMarketTiming(Map<String, Object> analysisResults) {
        if (analysisResults.containsKey("market_analyst")) {
            MarketAnalyst.MarketAnalysisResult market = 
                (MarketAnalyst.MarketAnalysisResult) analysisResults.get("market_analyst");
            
            return MarketTiming.builder()
                    .timingScore(market.getMarketScore())
                    .marketPhase("上升期")
                    .liquidityCondition("充裕")
                    .riskAppetite("中等")
                    .build();
        }
        
        return MarketTiming.builder()
                .timingScore(5.0)
                .marketPhase("中性")
                .liquidityCondition("正常")
                .riskAppetite("中等")
                .build();
    }
    
    private SentimentTiming analyzeSentimentTiming(Map<String, Object> analysisResults) {
        if (analysisResults.containsKey("sentiment_analyst")) {
            SentimentAnalyst.SentimentAnalysisResult sentiment = 
                (SentimentAnalyst.SentimentAnalysisResult) analysisResults.get("sentiment_analyst");
            
            return SentimentTiming.builder()
                    .timingScore(sentiment.getSentimentScore())
                    .sentimentTrend("积极")
                    .newsFlow("正面")
                    .investorMood("乐观")
                    .build();
        }
        
        return SentimentTiming.builder()
                .timingScore(5.0)
                .sentimentTrend("中性")
                .newsFlow("平衡")
                .investorMood("中性")
                .build();
    }
    
    private double calculateOverallTimingScore(TechnicalTiming technical, FundamentalTiming fundamental, 
                                             MarketTiming market, SentimentTiming sentiment) {
        return (technical.getTimingScore() * 0.3 + 
                fundamental.getTimingScore() * 0.3 + 
                market.getTimingScore() * 0.25 + 
                sentiment.getTimingScore() * 0.15);
    }
    
    private TimeWindow determineOptimalEntryWindow(TechnicalTiming technical, FundamentalTiming fundamental, MarketTiming market) {
        return TimeWindow.builder()
                .startTime("即时")
                .endTime("1-2周内")
                .optimalTime("技术回调时")
                .timeRationale("综合技术面和基本面分析")
                .build();
    }
    
    private double calculateTimingConfidence(double overallTimingScore, ComprehensiveAssessment assessment) {
        return Math.max(0.3, Math.min(0.9, overallTimingScore / 10.0 * assessment.getAssessmentConfidence()));
    }
    
    // 最终投资建议相关方法
    
    private InvestmentDecision makeInvestmentDecision(ComprehensiveAssessment assessment, InvestmentStrategy strategy, TimingAnalysis timingAnalysis) {
        String decisionType;
        double recommendedPosition = strategy.getPositionSizing().getRecommendedPosition();
        double expectedReturn = assessment.getOverallScore() * 0.02;
        String riskLevel;
        
        if (assessment.getOverallScore() >= 8.0) {
            decisionType = "强烈推荐";
            riskLevel = "中等";
        } else if (assessment.getOverallScore() >= 7.0) {
            decisionType = "推荐";
            riskLevel = "中等";
        } else if (assessment.getOverallScore() >= 6.0) {
            decisionType = "谨慎推荐";
            riskLevel = "中高";
        } else if (assessment.getOverallScore() >= 4.0) {
            decisionType = "观望";
            riskLevel = "高";
        } else {
            decisionType = "不推荐";
            riskLevel = "很高";
        }
        
        return InvestmentDecision.builder()
                .decisionType(decisionType)
                .recommendedPosition(recommendedPosition)
                .expectedReturn(expectedReturn)
                .riskLevel(riskLevel)
                .decisionRationale(strategy.getStrategyRationale())
                .build();
    }
    
    private PriceTarget calculatePriceTarget(ComprehensiveAssessment assessment, InvestmentStrategy strategy) {
        double currentPrice = 100.0; // 假设当前价格，实际应从市场数据获取
        double upside = assessment.getOverallScore() * 0.05; // 简化计算
        
        return PriceTarget.builder()
                .targetHigh(currentPrice * (1 + upside))
                .targetLow(currentPrice * (1 + upside * 0.5))
                .currentPrice(currentPrice)
                .upside(upside)
                .optimalEntryRange(String.format("%.2f - %.2f", currentPrice * 0.95, currentPrice * 1.05))
                .build();
    }
    
    private HoldingPeriod recommendHoldingPeriod(InvestmentStrategy strategy, RiskReturnProfile riskReturnProfile) {
        String recommendedPeriod;
        String minPeriod;
        String maxPeriod;
        
        if (strategy.getObjective().getTimeHorizon().equals("中长期")) {
            recommendedPeriod = "12-18个月";
            minPeriod = "6个月";
            maxPeriod = "24个月";
        } else if (strategy.getObjective().getTimeHorizon().equals("中期")) {
            recommendedPeriod = "6-12个月";
            minPeriod = "3个月";
            maxPeriod = "18个月";
        } else {
            recommendedPeriod = "3-6个月";
            minPeriod = "1个月";
            maxPeriod = "12个月";
        }
        
        return HoldingPeriod.builder()
                .recommendedPeriod(recommendedPeriod)
                .minPeriod(minPeriod)
                .maxPeriod(maxPeriod)
                .periodRationale("基于投资目标和风险收益特征")
                .build();
    }
    
    private List<String> identifyKeyMonitoringPoints(ComprehensiveAssessment assessment, InvestmentStrategy strategy) {
        List<String> points = new ArrayList<>();
        
        points.add("股价突破关键技术位");
        points.add("季度业绩发布");
        points.add("行业政策变化");
        points.add("市场情绪转变");
        
        if (assessment.getKeyRisks().contains("风险控制")) {
            points.add("风险指标异常");
        }
        
        return points;
    }
    
    private List<String> defineAdjustmentTriggers(ComprehensiveAssessment assessment, InvestmentStrategy strategy) {
        return List.of(
            "股价跌破止损位",
            "基本面发生重大变化",
            "技术形态破坏",
            "市场系统性风险",
            "达到目标收益"
        );
    }
    
    private List<String> suggestAlternatives(String stockCode, ComprehensiveAssessment assessment) {
        return List.of(
            "同行业龙头股票",
            "相关ETF产品",
            "行业指数基金",
            "可转债产品"
        );
    }
    
    private double calculateAdviceConfidence(ComprehensiveAssessment assessment, InvestmentStrategy strategy, TimingAnalysis timingAnalysis) {
        return (assessment.getAssessmentConfidence() + timingAnalysis.getTimingConfidence()) / 2.0;
    }
    
    private String generateRiskWarning(RiskReturnProfile riskReturnProfile, ComprehensiveAssessment assessment) {
        StringBuilder warning = new StringBuilder();
        
        warning.append("投资有风险，入市需谨慎。");
        
        if (riskReturnProfile.getRiskProfile().getOverallRisk() > 0.7) {
            warning.append("该投资风险较高，请注意控制仓位。");
        }
        
        if (assessment.getKeyRisks().size() > 2) {
            warning.append("存在多项风险因素，建议密切关注。");
        }
        
        return warning.toString();
    }
    
    private String generateActionPlan(InvestmentDecision decision, InvestmentStrategy strategy, TimingAnalysis timingAnalysis) {
        StringBuilder plan = new StringBuilder();
        
        plan.append(String.format("1. 投资决策：%s\n", decision.getDecisionType()));
        plan.append(String.format("2. 建议仓位：%.1f%%\n", decision.getRecommendedPosition() * 100));
        plan.append(String.format("3. 进入策略：%s\n", strategy.getEntryStrategy().getEntryMethod()));
        plan.append(String.format("4. 风险控制：止损%.1f%%\n", strategy.getRiskControl().getStopLossLevel() * 100));
        plan.append(String.format("5. 最佳时机：%s", timingAnalysis.getOptimalEntryWindow().getOptimalTime()));
        
        return plan.toString();
    }
    
    // ==================== 数据模型定义 ====================
    
    @lombok.Data
    @lombok.Builder
    public static class InvestmentAdviceResult {
        private String stockCode;
        private String agentId;
        private String agentName;
        private String analysis;
        private Map<String, Object> analysisResults;
        private DebateResult debateResult;
        private ComprehensiveAssessment assessment;
        private InvestmentStrategy strategy;
        private RiskReturnProfile riskReturnProfile;
        private PortfolioRecommendation portfolioRecommendation;
        private TimingAnalysis timingAnalysis;
        private FinalInvestmentAdvice finalAdvice;
        private double confidence;
        private long timestamp;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class DebateResult {
        private List<FundamentalAnalyst.DebateArgument> allArguments;
        private DebateConsensus consensus;
        private List<String> keyInsights;
        private Map<String, Double> agentAgreement;
        private double debateQuality;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class DebateConsensus {
        private double consensusLevel;
        private String consensusType;
        private String majorityView;
        private List<String> disagreementPoints;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class ComprehensiveAssessment {
        private Map<String, Double> dimensionScores;
        private Map<String, Double> weights;
        private double overallScore;
        private List<String> keyStrengths;
        private List<String> keyRisks;
        private UncertaintyAnalysis uncertaintyAnalysis;
        private SensitivityAnalysis sensitivityAnalysis;
        private double assessmentConfidence;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class UncertaintyAnalysis {
        private double uncertaintyLevel;
        private List<String> uncertaintySources;
        private String confidenceInterval;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class SensitivityAnalysis {
        private Map<String, Double> sensitivity;
        private String mostSensitiveFactor;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class InvestmentStrategy {
        private InvestmentObjective objective;
        private StrategyType strategyType;
        private PositionSizing positionSizing;
        private EntryStrategy entryStrategy;
        private ExitStrategy exitStrategy;
        private RiskControl riskControl;
        private List<String> monitoringIndicators;
        private String strategyRationale;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class InvestmentObjective {
        private String objectiveType;
        private String riskTolerance;
        private String timeHorizon;
        private double expectedReturn;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class StrategyType {
        private String strategyName;
        private String strategyDescription;
        private List<String> applicableScenarios;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PositionSizing {
        private double recommendedPosition;
        private double maxPosition;
        private double minPosition;
        private String sizingRationale;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class EntryStrategy {
        private String entryMethod;
        private String entryTiming;
        private String entryPrice;
        private List<String> entryConditions;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class ExitStrategy {
        private double profitTarget;
        private double stopLoss;
        private List<String> exitConditions;
        private String exitMethod;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class RiskControl {
        private double maxLoss;
        private double stopLossLevel;
        private List<String> riskMonitoring;
        private String riskAdjustment;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class RiskReturnProfile {
        private ExpectedReturn expectedReturn;
        private RiskProfile riskProfile;
        private double riskAdjustedReturn;
        private double maxDrawdownEstimate;
        private WinRateAnalysis winRateAnalysis;
        private ReturnDistribution returnDistribution;
        private double sharpeRatio;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class ExpectedReturn {
        private double expectedReturn;
        private double bestCase;
        private double worstCase;
        private double probability;
        private String timeFrame;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class RiskProfile {
        private double overallRisk;
        private double volatility;
        private double maxDrawdown;
        private List<String> riskFactors;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class WinRateAnalysis {
        private double expectedWinRate;
        private double avgWin;
        private double avgLoss;
        private double winLossRatio;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class ReturnDistribution {
        private double mean;
        private double standardDeviation;
        private double skewness;
        private double kurtosis;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PortfolioRecommendation {
        private AssetAllocation assetAllocation;
        private CorrelationAnalysis correlationAnalysis;
        private DiversificationAdvice diversificationAdvice;
        private RebalancingStrategy rebalancingStrategy;
        private PortfolioOptimization optimization;
        private double portfolioRisk;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class AssetAllocation {
        private double stockAllocation;
        private double bondAllocation;
        private double cashAllocation;
        private double alternativeAllocation;
        private String allocationRationale;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class CorrelationAnalysis {
        private double marketCorrelation;
        private double sectorCorrelation;
        private String correlationStability;
        private String diversificationBenefit;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class DiversificationAdvice {
        private String diversificationLevel;
        private List<String> recommendedSectors;
        private List<String> avoidSectors;
        private String diversificationStrategy;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class RebalancingStrategy {
        private String rebalancingFrequency;
        private String rebalancingTrigger;
        private String rebalancingMethod;
        private String rebalancingCost;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PortfolioOptimization {
        private String optimizationMethod;
        private double expectedReturn;
        private double expectedRisk;
        private double sharpeRatio;
        private List<String> optimizationConstraints;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class TimingAnalysis {
        private TechnicalTiming technicalTiming;
        private FundamentalTiming fundamentalTiming;
        private MarketTiming marketTiming;
        private SentimentTiming sentimentTiming;
        private double overallTimingScore;
        private TimeWindow optimalEntryWindow;
        private double timingConfidence;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class TechnicalTiming {
        private double timingScore;
        private String trendDirection;
        private String momentumStrength;
        private String supportResistance;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class FundamentalTiming {
        private double timingScore;
        private String valuationLevel;
        private String earningsGrowth;
        private String businessCycle;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class MarketTiming {
        private double timingScore;
        private String marketPhase;
        private String liquidityCondition;
        private String riskAppetite;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class SentimentTiming {
        private double timingScore;
        private String sentimentTrend;
        private String newsFlow;
        private String investorMood;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class TimeWindow {
        private String startTime;
        private String endTime;
        private String optimalTime;
        private String timeRationale;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class FinalInvestmentAdvice {
        private InvestmentDecision decision;
        private PriceTarget priceTarget;
        private HoldingPeriod holdingPeriod;
        private List<String> keyMonitoringPoints;
        private List<String> adjustmentTriggers;
        private List<String> alternatives;
        private double confidenceLevel;
        private String riskWarning;
        private String actionPlan;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class InvestmentDecision {
        private String decisionType;
        private double recommendedPosition;
        private double expectedReturn;
        private String riskLevel;
        private String decisionRationale;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class PriceTarget {
        private double targetHigh;
        private double targetLow;
        private double currentPrice;
        private double upside;
        private String optimalEntryRange;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class HoldingPeriod {
        private String recommendedPeriod;
        private String minPeriod;
        private String maxPeriod;
        private String periodRationale;
    }
}