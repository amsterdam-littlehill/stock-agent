package com.jd.genie.agent.collaboration;

import com.jd.genie.agent.agent.stock.*;
import com.jd.genie.agent.collaboration.model.CollaborationModels.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

/**
 * 结构化辩论引擎
 * 基于TradingAgents论文实现的多智能体结构化辩论机制
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component
public class StructuredDebateEngine {
    
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
    private BullishResearcher bullishResearcher;
    
    @Autowired
    private BearishResearcher bearishResearcher;
    
    /**
     * 执行增强型结构化辩论（含多空观点对比）
     * 参考A_Share_investment_Agent的辩论室机制
     * 
     * @param stockCode 股票代码
     * @param analysisResults 各智能体的分析结果
     * @param maxRounds 最大辩论轮数
     * @return 增强辩论结果
     */
    public EnhancedDebateResult conductEnhancedStructuredDebate(String stockCode, 
                                                               Map<String, Object> analysisResults, 
                                                               int maxRounds) {
        log.info("开始增强型结构化辩论（含多空对比）: 股票={}, 最大轮数={}", stockCode, maxRounds);
        
        // 第一阶段：多空研究
        BullBearDebateResult bullBearResult = conductBullBearResearch(stockCode, analysisResults);
        
        // 第二阶段：传统分析师辩论
        DebateResult traditionalDebate = conductStructuredDebate(stockCode, analysisResults, maxRounds);
        
        // 第三阶段：多空观点对比与综合
        ComprehensiveDebateResult comprehensiveResult = synthesizeBullBearAndTraditionalDebate(
                bullBearResult, traditionalDebate, stockCode);
        
        // 第四阶段：LLM第三方评估（参考A_Share_investment_Agent）
        LLMAssessmentResult llmAssessment = conductLLMThirdPartyAssessment(comprehensiveResult);
        
        // 构建最终增强辩论结果
        EnhancedDebateResult enhancedResult = EnhancedDebateResult.builder()
                .stockCode(stockCode)
                .bullBearResult(bullBearResult)
                .traditionalDebateResult(traditionalDebate)
                .comprehensiveResult(comprehensiveResult)
                .llmAssessment(llmAssessment)
                .finalConfidence(calculateEnhancedConfidence(bullBearResult, traditionalDebate, llmAssessment))
                .build();
        
        log.info("增强型结构化辩论完成，最终置信度: {}", enhancedResult.getFinalConfidence());
        return enhancedResult;
    }
    
    /**
     * 原有的结构化辩论方法（保持兼容性）
     */
    public DebateResult conductStructuredDebate(String stockCode, 
                                               Map<String, Object> analysisResults, 
                                               int maxRounds) {
        log.info("开始结构化辩论: 股票={}, 参与智能体={}, 最大轮数={}", 
                stockCode, analysisResults.keySet(), maxRounds);
        
        List<FundamentalAnalyst.DebateArgument> allArguments = new ArrayList<>();
        Map<Integer, List<FundamentalAnalyst.DebateArgument>> roundArguments = new HashMap<>();
        
        // 创建辩论上下文
        FundamentalAnalyst.DebateContext context = FundamentalAnalyst.DebateContext.builder()
                .stockCode(stockCode)
                .currentRound(1)
                .analysisResults(analysisResults)
                .build();
        
        // 进行多轮辩论
        for (int round = 1; round <= maxRounds; round++) {
            log.debug("第{}轮辩论开始", round);
            context.setCurrentRound(round);
            
            List<FundamentalAnalyst.DebateArgument> currentRoundArguments = conductDebateRound(
                    context, allArguments, analysisResults);
            
            roundArguments.put(round, currentRoundArguments);
            allArguments.addAll(currentRoundArguments);
            
            // 计算当前轮次的共识度
            double consensusLevel = calculateConsensusLevel(currentRoundArguments);
            log.debug("第{}轮辩论结束，共识度: {:.2f}", round, consensusLevel);
            
            // 如果达到高共识度，可以提前结束
            if (consensusLevel > 0.8 && round >= 2) {
                log.info("达到高共识度({:.2f})，在第{}轮提前结束辩论", consensusLevel, round);
                break;
            }
        }
        
        // 分析辩论结果
        DebateConsensus consensus = analyzeDebateConsensus(allArguments);
        List<String> keyInsights = extractKeyInsights(allArguments);
        Map<String, Double> agentAgreement = calculateAgentAgreement(allArguments);
        double debateQuality = assessDebateQuality(allArguments);
        
        DebateResult result = DebateResult.builder()
                .allArguments(allArguments)
                .roundArguments(roundArguments)
                .consensus(consensus)
                .keyInsights(keyInsights)
                .agentAgreement(agentAgreement)
                .debateQuality(debateQuality)
                .totalRounds(roundArguments.size())
                .participantCount(analysisResults.size())
                .build();
        
        log.info("结构化辩论完成: 总轮数={}, 参与者={}, 最终共识度={:.2f}, 辩论质量={:.2f}", 
                result.getTotalRounds(), result.getParticipantCount(), 
                consensus.getConsensusLevel(), debateQuality);
        
        return result;
    }
    
    /**
     * 执行多空研究对比
     * 参考A_Share_investment_Agent的多空研究员机制
     */
    private BullBearDebateResult conductBullBearResearch(String stockCode, Map<String, Object> analysisResults) {
        log.info("开始多空研究对比: {}", stockCode);
        
        // 并行执行多空研究
        CompletableFuture<BullishResearcher.ResearchResult> bullishFuture = 
                bullishResearcher.conductBullishResearch(stockCode, analysisResults);
        CompletableFuture<BullishResearcher.ResearchResult> bearishFuture = 
                bearishResearcher.conductBearishResearch(stockCode, analysisResults);
        
        try {
            BullishResearcher.ResearchResult bullishResult = bullishFuture.get();
            BullishResearcher.ResearchResult bearishResult = bearishFuture.get();
            
            // 计算多空置信度差异
            double confidenceDifference = bullishResult.getConfidence() - bearishResult.getConfidence();
            
            // 确定占优方
            String dominantSide = confidenceDifference > 0.1 ? "BULLISH" : 
                                 confidenceDifference < -0.1 ? "BEARISH" : "NEUTRAL";
            
            BullBearDebateResult result = BullBearDebateResult.builder()
                    .stockCode(stockCode)
                    .bullishResult(bullishResult)
                    .bearishResult(bearishResult)
                    .confidenceDifference(Math.abs(confidenceDifference))
                    .dominantSide(dominantSide)
                    .debateIntensity(calculateDebateIntensity(bullishResult, bearishResult))
                    .build();
            
            log.info("多空研究完成，占优方: {}, 置信度差异: {:.3f}", dominantSide, confidenceDifference);
            return result;
            
        } catch (Exception e) {
            log.error("多空研究执行失败: {}", e.getMessage(), e);
            throw new RuntimeException("多空研究执行失败", e);
        }
    }
    
    /**
     * 综合多空观点和传统分析师辩论结果
     */
    private ComprehensiveDebateResult synthesizeBullBearAndTraditionalDebate(
            BullBearDebateResult bullBearResult, 
            DebateResult traditionalDebate, 
            String stockCode) {
        
        log.info("开始综合多空观点和传统分析师结果");
        
        // 分析观点一致性
        double viewpointConsistency = analyzeViewpointConsistency(bullBearResult, traditionalDebate);
        
        // 生成综合观点
        String synthesizedViewpoint = generateSynthesizedViewpoint(bullBearResult, traditionalDebate, stockCode);
        
        // 计算综合置信度
        double combinedConfidence = calculateCombinedConfidence(bullBearResult, traditionalDebate);
        
        // 识别关键分歧点
        List<String> keyDisagreements = identifyKeyDisagreements(bullBearResult, traditionalDebate);
        
        return ComprehensiveDebateResult.builder()
                .stockCode(stockCode)
                .viewpointConsistency(viewpointConsistency)
                .synthesizedViewpoint(synthesizedViewpoint)
                .combinedConfidence(combinedConfidence)
                .keyDisagreements(keyDisagreements)
                .build();
    }
    
    /**
     * LLM第三方客观评估
     * 参考A_Share_investment_Agent的LLM增强机制
     */
    private LLMAssessmentResult conductLLMThirdPartyAssessment(ComprehensiveDebateResult comprehensiveResult) {
        log.info("开始LLM第三方客观评估");
        
        // 构建LLM评估提示词
        String assessmentPrompt = buildLLMAssessmentPrompt(comprehensiveResult);
        
        // 调用LLM进行客观评估
        String llmAssessment = "基于客观分析，综合各方观点..."; // 实际应调用LLM API
        
        // 解析LLM评分
        double objectivityScore = 0.8; // 客观性评分
        double reliabilityScore = 0.75; // 可靠性评分
        double finalScore = (objectivityScore + reliabilityScore) / 2;
        
        return LLMAssessmentResult.builder()
                .stockCode(comprehensiveResult.getStockCode())
                .assessmentContent(llmAssessment)
                .objectivityScore(objectivityScore)
                .reliabilityScore(reliabilityScore)
                .finalScore(finalScore)
                .build();
    }
    
    /**
     * 计算增强置信度（混合多空、传统分析师和LLM评估）
     */
    private double calculateEnhancedConfidence(BullBearDebateResult bullBearResult, 
                                             DebateResult traditionalDebate, 
                                             LLMAssessmentResult llmAssessment) {
        
        // 权重分配：多空对比40%，传统分析师30%，LLM评估30%
        double bullBearWeight = 0.4;
        double traditionalWeight = 0.3;
        double llmWeight = 0.3;
        
        // 计算各部分置信度
        double bullBearConfidence = Math.max(bullBearResult.getBullishResult().getConfidence(),
                                           bullBearResult.getBearishResult().getConfidence());
        double traditionalConfidence = traditionalDebate.getConsensus().getConsensusLevel();
        double llmConfidence = llmAssessment.getFinalScore();
        
        // 加权计算最终置信度
        double enhancedConfidence = bullBearConfidence * bullBearWeight + 
                                  traditionalConfidence * traditionalWeight + 
                                  llmConfidence * llmWeight;
        
        log.info("增强置信度计算: 多空={:.3f}(权重{:.1f}) + 传统={:.3f}(权重{:.1f}) + LLM={:.3f}(权重{:.1f}) = {:.3f}",
                bullBearConfidence, bullBearWeight, traditionalConfidence, traditionalWeight, 
                llmConfidence, llmWeight, enhancedConfidence);
        
        return enhancedConfidence;
    }
    
    /**
     * 计算辩论强度
     */
    private double calculateDebateIntensity(BullishResearcher.ResearchResult bullishResult, 
                                          BullishResearcher.ResearchResult bearishResult) {
        // 基于置信度差异和观点对立程度计算辩论强度
        double confidenceDiff = Math.abs(bullishResult.getConfidence() - bearishResult.getConfidence());
        return Math.min(1.0, confidenceDiff * 2); // 标准化到[0,1]
    }
    
    /**
     * 分析观点一致性
     */
    private double analyzeViewpointConsistency(BullBearDebateResult bullBearResult, DebateResult traditionalDebate) {
        // 实现观点一致性分析逻辑
        return 0.65; // 示例值
    }
    
    /**
     * 生成综合观点
     */
    private String generateSynthesizedViewpoint(BullBearDebateResult bullBearResult, 
                                              DebateResult traditionalDebate, 
                                              String stockCode) {
        return String.format("基于多空研究和传统分析师辩论，%s的综合投资观点为...", stockCode);
    }
    
    /**
     * 计算组合置信度
     */
    private double calculateCombinedConfidence(BullBearDebateResult bullBearResult, DebateResult traditionalDebate) {
        double bullBearAvg = (bullBearResult.getBullishResult().getConfidence() + 
                             bullBearResult.getBearishResult().getConfidence()) / 2;
        double traditionalConf = traditionalDebate.getConsensus().getConsensusLevel();
        return (bullBearAvg + traditionalConf) / 2;
    }
    
    /**
     * 识别关键分歧点
     */
    private List<String> identifyKeyDisagreements(BullBearDebateResult bullBearResult, DebateResult traditionalDebate) {
        List<String> disagreements = new ArrayList<>();
        
        if (bullBearResult.getConfidenceDifference() > 0.3) {
            disagreements.add("多空观点存在显著分歧");
        }
        
        if (traditionalDebate.getConsensus().getConsensusLevel() < 0.6) {
            disagreements.add("传统分析师共识度较低");
        }
        
        return disagreements;
    }
    
    /**
     * 构建LLM评估提示词
     */
    private String buildLLMAssessmentPrompt(ComprehensiveDebateResult comprehensiveResult) {
        return String.format("""
            作为独立的第三方分析师，请客观评估以下股票分析结果：
            
            股票代码：%s
            观点一致性：%.2f
            综合观点：%s
            组合置信度：%.3f
            关键分歧：%s
            
            请从以下维度进行客观评估：
            1. 分析逻辑的完整性和严密性
            2. 数据支撑的充分性
            3. 风险因素的考虑是否全面
            4. 投资建议的可操作性
            
            请给出客观性评分(0-1)和可靠性评分(0-1)，并说明理由。
            """,
            comprehensiveResult.getStockCode(),
            comprehensiveResult.getViewpointConsistency(),
            comprehensiveResult.getSynthesizedViewpoint(),
            comprehensiveResult.getCombinedConfidence(),
            String.join(", ", comprehensiveResult.getKeyDisagreements())
        );
    }
    
    /**
     * 执行单轮辩论
     */
    private List<FundamentalAnalyst.DebateArgument> conductDebateRound(
            FundamentalAnalyst.DebateContext context,
            List<FundamentalAnalyst.DebateArgument> previousArguments,
            Map<String, Object> analysisResults) {
        
        List<FundamentalAnalyst.DebateArgument> roundArguments = new ArrayList<>();
        
        try {
            // 各分析师按顺序参与辩论
            if (analysisResults.containsKey("fundamental_analyst")) {
                FundamentalAnalyst.DebateArgument arg = fundamentalAnalyst.debate(context, previousArguments);
                roundArguments.add(arg);
            }
            
            if (analysisResults.containsKey("technical_analyst")) {
                FundamentalAnalyst.DebateArgument arg = technicalAnalyst.debate(context, previousArguments);
                roundArguments.add(arg);
            }
            
            if (analysisResults.containsKey("sentiment_analyst")) {
                FundamentalAnalyst.DebateArgument arg = sentimentAnalyst.debate(context, previousArguments);
                roundArguments.add(arg);
            }
            
            if (analysisResults.containsKey("risk_manager")) {
                FundamentalAnalyst.DebateArgument arg = riskManager.debate(context, previousArguments);
                roundArguments.add(arg);
            }
            
            if (analysisResults.containsKey("quantitative_analyst")) {
                FundamentalAnalyst.DebateArgument arg = quantitativeAnalyst.debate(context, previousArguments);
                roundArguments.add(arg);
            }
            
            if (analysisResults.containsKey("market_analyst")) {
                FundamentalAnalyst.DebateArgument arg = marketAnalyst.debate(context, previousArguments);
                roundArguments.add(arg);
            }
            
            if (analysisResults.containsKey("news_analyst")) {
                FundamentalAnalyst.DebateArgument arg = newsAnalyst.debate(context, previousArguments);
                roundArguments.add(arg);
            }
            
            if (analysisResults.containsKey("research_analyst")) {
                FundamentalAnalyst.DebateArgument arg = researchAnalyst.debate(context, previousArguments);
                roundArguments.add(arg);
            }
            
            if (analysisResults.containsKey("trading_executor")) {
                FundamentalAnalyst.DebateArgument arg = tradingExecutor.debate(context, previousArguments);
                roundArguments.add(arg);
            }
            
        } catch (Exception e) {
            log.error("第{}轮辩论失败", context.getCurrentRound(), e);
        }
        
        return roundArguments;
    }
    
    /**
     * 计算共识度
     */
    private double calculateConsensusLevel(List<FundamentalAnalyst.DebateArgument> arguments) {
        if (arguments.isEmpty()) {
            return 0.0;
        }
        
        // 基于置信度的方差计算共识度
        double[] confidences = arguments.stream()
                .mapToDouble(FundamentalAnalyst.DebateArgument::getConfidence)
                .toArray();
        
        double mean = Arrays.stream(confidences).average().orElse(0.0);
        double variance = Arrays.stream(confidences)
                .map(conf -> Math.pow(conf - mean, 2))
                .average().orElse(0.0);
        
        // 共识度 = 1 - 标准化方差
        return Math.max(0.0, 1.0 - Math.sqrt(variance));
    }
    
    /**
     * 分析辩论共识
     */
    private DebateConsensus analyzeDebateConsensus(List<FundamentalAnalyst.DebateArgument> arguments) {
        if (arguments.isEmpty()) {
            return DebateConsensus.builder()
                    .consensusLevel(0.0)
                    .consensusType("无共识")
                    .majorityView("无观点")
                    .disagreementPoints(Collections.emptyList())
                    .build();
        }
        
        // 计算整体共识度
        double consensusLevel = calculateConsensusLevel(arguments);
        
        // 分析观点分布
        Map<String, Long> viewCounts = arguments.stream()
                .collect(Collectors.groupingBy(
                        this::extractViewFromArgument,
                        Collectors.counting()
                ));
        
        String majorityView = viewCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("无明确观点");
        
        // 确定共识类型
        String consensusType;
        if (consensusLevel > 0.8) {
            consensusType = "强共识";
        } else if (consensusLevel > 0.6) {
            consensusType = "中等共识";
        } else if (consensusLevel > 0.4) {
            consensusType = "弱共识";
        } else {
            consensusType = "分歧较大";
        }
        
        // 识别分歧点
        List<String> disagreementPoints = identifyDisagreementPoints(arguments);
        
        return DebateConsensus.builder()
                .consensusLevel(consensusLevel)
                .consensusType(consensusType)
                .majorityView(majorityView)
                .disagreementPoints(disagreementPoints)
                .build();
    }
    
    /**
     * 从辩论论据中提取观点
     */
    private String extractViewFromArgument(FundamentalAnalyst.DebateArgument argument) {
        String content = argument.getArgument().toLowerCase();
        
        if (content.contains("看多") || content.contains("买入") || content.contains("上涨")) {
            return "看多";
        } else if (content.contains("看空") || content.contains("卖出") || content.contains("下跌")) {
            return "看空";
        } else if (content.contains("持有") || content.contains("观望")) {
            return "中性";
        } else {
            return "不明确";
        }
    }
    
    /**
     * 识别分歧点
     */
    private List<String> identifyDisagreementPoints(List<FundamentalAnalyst.DebateArgument> arguments) {
        List<String> disagreements = new ArrayList<>();
        
        // 简单的分歧识别逻辑
        Map<String, List<FundamentalAnalyst.DebateArgument>> viewGroups = arguments.stream()
                .collect(Collectors.groupingBy(this::extractViewFromArgument));
        
        if (viewGroups.size() > 1) {
            disagreements.add("投资方向存在分歧");
        }
        
        // 检查置信度差异
        double maxConfidence = arguments.stream()
                .mapToDouble(FundamentalAnalyst.DebateArgument::getConfidence)
                .max().orElse(0.0);
        double minConfidence = arguments.stream()
                .mapToDouble(FundamentalAnalyst.DebateArgument::getConfidence)
                .min().orElse(0.0);
        
        if (maxConfidence - minConfidence > 0.3) {
            disagreements.add("置信度差异较大");
        }
        
        return disagreements;
    }
    
    /**
     * 提取关键洞察
     */
    private List<String> extractKeyInsights(List<FundamentalAnalyst.DebateArgument> arguments) {
        List<String> insights = new ArrayList<>();
        
        // 按智能体类型分组统计观点
        Map<String, List<FundamentalAnalyst.DebateArgument>> agentGroups = arguments.stream()
                .collect(Collectors.groupingBy(FundamentalAnalyst.DebateArgument::getAgentId));
        
        for (Map.Entry<String, List<FundamentalAnalyst.DebateArgument>> entry : agentGroups.entrySet()) {
            String agentId = entry.getKey();
            List<FundamentalAnalyst.DebateArgument> agentArgs = entry.getValue();
            
            if (!agentArgs.isEmpty()) {
                FundamentalAnalyst.DebateArgument lastArg = agentArgs.get(agentArgs.size() - 1);
                insights.add(String.format("%s: %s (置信度: %.2f)", 
                        lastArg.getAgentName(), 
                        extractViewFromArgument(lastArg),
                        lastArg.getConfidence()));
            }
        }
        
        return insights;
    }
    
    /**
     * 计算智能体间一致性
     */
    private Map<String, Double> calculateAgentAgreement(List<FundamentalAnalyst.DebateArgument> arguments) {
        Map<String, Double> agreement = new HashMap<>();
        
        Map<String, List<FundamentalAnalyst.DebateArgument>> agentGroups = arguments.stream()
                .collect(Collectors.groupingBy(FundamentalAnalyst.DebateArgument::getAgentId));
        
        for (String agentId : agentGroups.keySet()) {
            List<FundamentalAnalyst.DebateArgument> agentArgs = agentGroups.get(agentId);
            
            // 计算该智能体与其他智能体的一致性
            double totalAgreement = 0.0;
            int comparisons = 0;
            
            for (String otherAgentId : agentGroups.keySet()) {
                if (!agentId.equals(otherAgentId)) {
                    List<FundamentalAnalyst.DebateArgument> otherArgs = agentGroups.get(otherAgentId);
                    double pairAgreement = calculatePairwiseAgreement(agentArgs, otherArgs);
                    totalAgreement += pairAgreement;
                    comparisons++;
                }
            }
            
            agreement.put(agentId, comparisons > 0 ? totalAgreement / comparisons : 0.0);
        }
        
        return agreement;
    }
    
    /**
     * 计算两个智能体间的一致性
     */
    private double calculatePairwiseAgreement(List<FundamentalAnalyst.DebateArgument> args1,
                                            List<FundamentalAnalyst.DebateArgument> args2) {
        if (args1.isEmpty() || args2.isEmpty()) {
            return 0.0;
        }
        
        // 简单的一致性计算：基于最后观点的相似度
        FundamentalAnalyst.DebateArgument lastArg1 = args1.get(args1.size() - 1);
        FundamentalAnalyst.DebateArgument lastArg2 = args2.get(args2.size() - 1);
        
        String view1 = extractViewFromArgument(lastArg1);
        String view2 = extractViewFromArgument(lastArg2);
        
        if (view1.equals(view2)) {
            // 观点一致，再考虑置信度差异
            double confidenceDiff = Math.abs(lastArg1.getConfidence() - lastArg2.getConfidence());
            return Math.max(0.0, 1.0 - confidenceDiff);
        } else {
            return 0.0;
        }
    }
    
    /**
     * 评估辩论质量
     */
    private double assessDebateQuality(List<FundamentalAnalyst.DebateArgument> arguments) {
        if (arguments.isEmpty()) {
            return 0.0;
        }
        
        double qualityScore = 0.0;
        
        // 1. 参与度评分 (30%)
        double participationScore = Math.min(1.0, arguments.size() / 15.0); // 假设15个论据为满分
        qualityScore += participationScore * 0.3;
        
        // 2. 论据质量评分 (40%)
        double avgArgumentLength = arguments.stream()
                .mapToInt(arg -> arg.getArgument().length())
                .average().orElse(0.0);
        double argumentQualityScore = Math.min(1.0, avgArgumentLength / 200.0); // 200字符为满分
        qualityScore += argumentQualityScore * 0.4;
        
        // 3. 置信度分布评分 (30%)
        double avgConfidence = arguments.stream()
                .mapToDouble(FundamentalAnalyst.DebateArgument::getConfidence)
                .average().orElse(0.0);
        qualityScore += avgConfidence * 0.3;
        
        return Math.min(1.0, qualityScore);
    }
    
    /**
     * 辩论结果数据类
     */
    @lombok.Data
    @lombok.Builder
    public static class DebateResult {
        private List<FundamentalAnalyst.DebateArgument> allArguments;
        private Map<Integer, List<FundamentalAnalyst.DebateArgument>> roundArguments;
        private DebateConsensus consensus;
        private List<String> keyInsights;
        private Map<String, Double> agentAgreement;
        private double debateQuality;
        private int totalRounds;
        private int participantCount;
    }
    
    /**
     * 辩论共识数据类
     */
    @lombok.Data
    @lombok.Builder
    public static class DebateConsensus {
        private double consensusLevel;
        private String consensusType;
        private String majorityView;
        private List<String> disagreementPoints;
    }
}