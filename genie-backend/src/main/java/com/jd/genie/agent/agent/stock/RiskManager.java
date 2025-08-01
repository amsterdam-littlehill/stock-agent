package com.jd.genie.agent.agent.stock;

import com.jd.genie.agent.agent.BaseAgent;
import com.jd.genie.agent.enums.AgentType;
import com.jd.genie.service.StockDataMigrationService;
import com.jd.genie.service.StockDataMigrationService.KLineData;
import com.jd.genie.service.StockDataMigrationService.StockQuoteData;
import com.jd.genie.service.StockDataMigrationService.FinancialData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 风险管理师智能体
 * 基于TradingAgents框架的专业风险管理师角色
 * 
 * 职责：
 * - 风险评估与量化
 * - 风险控制策略
 * - 投资组合风险分析
 * - 风险预警机制
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component("risk_manager")
public class RiskManager extends BaseAgent {
    
    @Autowired
    private StockDataMigrationService stockDataService;
    
    public RiskManager() {
        super();
        this.agentType = AgentType.ANALYSIS;
        this.agentName = "风险管理师";
        this.description = "专业的风险管理师，擅长风险评估、风险控制和投资组合风险分析";
    }
    
    /**
     * 执行风险分析
     * 
     * @param stockCode 股票代码
     * @param context 分析上下文
     * @return 分析结果
     */
    public CompletableFuture<RiskAnalysisResult> analyze(String stockCode, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("风险管理师开始分析股票: {}", stockCode);
                
                // 1. 获取历史数据
                List<KLineData> klineData = stockDataService.getKLineData(stockCode, "daily", 252).get(); // 一年数据
                List<StockQuoteData> quoteData = stockDataService.getRealTimeStockData(List.of(stockCode)).get();
                FinancialData financialData = stockDataService.getFinancialData(stockCode).get();
                
                if (klineData.isEmpty() || quoteData.isEmpty()) {
                    return createErrorResult("无法获取股票数据");
                }
                
                // 按时间排序
                klineData.sort(Comparator.comparing(KLineData::getDate));
                StockQuoteData currentQuote = quoteData.get(0);
                
                // 2. 市场风险分析
                MarketRiskAnalysis marketRisk = analyzeMarketRisk(klineData, currentQuote);
                
                // 3. 信用风险分析
                CreditRiskAnalysis creditRisk = analyzeCreditRisk(financialData, stockCode);
                
                // 4. 流动性风险分析
                LiquidityRiskAnalysis liquidityRisk = analyzeLiquidityRisk(klineData, currentQuote);
                
                // 5. 操作风险分析
                OperationalRiskAnalysis operationalRisk = analyzeOperationalRisk(stockCode, context);
                
                // 6. 风险指标计算
                RiskMetrics riskMetrics = calculateRiskMetrics(klineData, marketRisk);
                
                // 7. 风险评级
                RiskRating riskRating = calculateRiskRating(marketRisk, creditRisk, liquidityRisk, operationalRisk);
                
                // 8. 风险控制建议
                List<RiskControlMeasure> controlMeasures = generateRiskControlMeasures(riskRating, riskMetrics);
                
                // 9. 生成LLM分析
                String llmAnalysis = generateLLMAnalysis(stockCode, marketRisk, creditRisk, 
                                                       liquidityRisk, riskMetrics, riskRating);
                
                // 10. 计算综合风险评分
                double riskScore = calculateOverallRiskScore(riskRating, riskMetrics);
                
                // 11. 生成投资建议
                String recommendation = generateRecommendation(riskScore, riskRating);
                
                return RiskAnalysisResult.builder()
                        .stockCode(stockCode)
                        .agentId("risk_manager")
                        .agentName("风险管理师")
                        .analysis(llmAnalysis)
                        .marketRisk(marketRisk)
                        .creditRisk(creditRisk)
                        .liquidityRisk(liquidityRisk)
                        .operationalRisk(operationalRisk)
                        .riskMetrics(riskMetrics)
                        .riskRating(riskRating)
                        .controlMeasures(controlMeasures)
                        .riskScore(riskScore)
                        .recommendation(recommendation)
                        .confidence(calculateConfidence(riskMetrics, riskRating))
                        .timestamp(System.currentTimeMillis())
                        .build();
                
            } catch (Exception e) {
                log.error("风险分析失败: {}", stockCode, e);
                return createErrorResult("分析过程中发生错误: " + e.getMessage());
            }
        });
    }
    
    /**
     * 参与结构化辩论
     */
    public FundamentalAnalyst.DebateArgument debate(FundamentalAnalyst.DebateContext context, 
                                                   List<FundamentalAnalyst.DebateArgument> previousArguments) {
        try {
            String stockCode = context.getStockCode();
            int currentRound = context.getCurrentRound();
            
            // 获取风险分析结果
            RiskAnalysisResult analysisResult = (RiskAnalysisResult) context.getAgentResult("risk_manager");
            
            String prompt = buildDebatePrompt(currentRound, analysisResult, previousArguments);
            
            // 调用LLM生成辩论论据
            String argument = callLLM(prompt);
            
            return FundamentalAnalyst.DebateArgument.builder()
                    .agentId("risk_manager")
                    .agentName("风险管理师")
                    .round(currentRound)
                    .argument(argument)
                    .confidence(analysisResult.getConfidence())
                    .evidenceType("RISK")
                    .supportingData(Map.of(
                        "riskRating", analysisResult.getRiskRating(),
                        "riskMetrics", analysisResult.getRiskMetrics(),
                        "riskScore", analysisResult.getRiskScore()
                    ))
                    .timestamp(System.currentTimeMillis())
                    .build();
                    
        } catch (Exception e) {
            log.error("风险管理师辩论失败", e);
            return createErrorDebateArgument("辩论过程中发生错误");
        }
    }
    
    /**
     * 市场风险分析
     */
    private MarketRiskAnalysis analyzeMarketRisk(List<KLineData> klineData, StockQuoteData currentQuote) {
        // 计算收益率序列
        List<Double> returns = calculateReturns(klineData);
        
        // 波动率分析
        double volatility = calculateVolatility(returns);
        double annualizedVolatility = volatility * Math.sqrt(252); // 年化波动率
        
        // VaR计算（95%置信度）
        double var95 = calculateVaR(returns, 0.95);
        double var99 = calculateVaR(returns, 0.99);
        
        // 最大回撤
        double maxDrawdown = calculateMaxDrawdown(klineData);
        
        // Beta系数（简化计算，假设市场收益率）
        double beta = calculateBeta(returns);
        
        // 价格风险评估
        PriceRiskAssessment priceRisk = assessPriceRisk(klineData, currentQuote);
        
        // 市场风险等级
        String riskLevel = determineMarketRiskLevel(volatility, var95, maxDrawdown);
        
        return MarketRiskAnalysis.builder()
                .volatility(annualizedVolatility)
                .var95(var95)
                .var99(var99)
                .maxDrawdown(maxDrawdown)
                .beta(beta)
                .priceRisk(priceRisk)
                .riskLevel(riskLevel)
                .build();
    }
    
    /**
     * 信用风险分析
     */
    private CreditRiskAnalysis analyzeCreditRisk(FinancialData financialData, String stockCode) {
        if (financialData == null) {
            return CreditRiskAnalysis.builder()
                    .creditRating("数据不足")
                    .defaultProbability(0.0)
                    .creditScore(0.0)
                    .riskFactors(Arrays.asList("财务数据缺失"))
                    .build();
        }
        
        // 财务健康度评分
        double financialHealthScore = calculateFinancialHealthScore(financialData);
        
        // 违约概率估算（简化模型）
        double defaultProbability = calculateDefaultProbability(financialData);
        
        // 信用评级
        String creditRating = determineCreditRating(financialHealthScore, defaultProbability);
        
        // 信用风险因子识别
        List<String> riskFactors = identifyCreditRiskFactors(financialData);
        
        // 债务分析
        DebtAnalysis debtAnalysis = analyzeDebtStructure(financialData);
        
        return CreditRiskAnalysis.builder()
                .creditRating(creditRating)
                .defaultProbability(defaultProbability)
                .creditScore(financialHealthScore)
                .riskFactors(riskFactors)
                .debtAnalysis(debtAnalysis)
                .build();
    }
    
    /**
     * 流动性风险分析
     */
    private LiquidityRiskAnalysis analyzeLiquidityRisk(List<KLineData> klineData, StockQuoteData currentQuote) {
        // 成交量分析
        List<Long> volumes = klineData.stream().map(KLineData::getVolume).collect(Collectors.toList());
        double avgVolume = volumes.stream().mapToLong(Long::longValue).average().orElse(0);
        double volumeVolatility = calculateVolumeVolatility(volumes);
        
        // 流动性指标
        double turnoverRate = calculateTurnoverRate(currentQuote, avgVolume);
        double bidAskSpread = calculateBidAskSpread(currentQuote); // 简化实现
        
        // 流动性风险等级
        String liquidityLevel = determineLiquidityLevel(turnoverRate, volumeVolatility, bidAskSpread);
        
        // 市场冲击成本
        double marketImpactCost = calculateMarketImpactCost(turnoverRate, volumeVolatility);
        
        return LiquidityRiskAnalysis.builder()
                .avgVolume(avgVolume)
                .volumeVolatility(volumeVolatility)
                .turnoverRate(turnoverRate)
                .bidAskSpread(bidAskSpread)
                .liquidityLevel(liquidityLevel)
                .marketImpactCost(marketImpactCost)
                .build();
    }
    
    /**
     * 操作风险分析
     */
    private OperationalRiskAnalysis analyzeOperationalRisk(String stockCode, Map<String, Object> context) {
        List<String> riskFactors = new ArrayList<>();
        double riskScore = 0.0;
        
        // 监管风险
        if (isHighRegulatoryRisk(stockCode)) {
            riskFactors.add("监管政策风险");
            riskScore += 0.2;
        }
        
        // 行业风险
        String industryRisk = assessIndustryRisk(stockCode);
        if (!"低风险".equals(industryRisk)) {
            riskFactors.add("行业风险: " + industryRisk);
            riskScore += 0.15;
        }
        
        // 公司治理风险
        String governanceRisk = assessGovernanceRisk(stockCode);
        if (!"良好".equals(governanceRisk)) {
            riskFactors.add("公司治理风险: " + governanceRisk);
            riskScore += 0.1;
        }
        
        // 技术风险
        if (hasTechnicalRisk(context)) {
            riskFactors.add("技术系统风险");
            riskScore += 0.05;
        }
        
        String overallLevel = determineOperationalRiskLevel(riskScore);
        
        return OperationalRiskAnalysis.builder()
                .riskFactors(riskFactors)
                .riskScore(riskScore)
                .overallLevel(overallLevel)
                .regulatoryRisk(isHighRegulatoryRisk(stockCode))
                .industryRisk(industryRisk)
                .governanceRisk(governanceRisk)
                .build();
    }
    
    /**
     * 风险指标计算
     */
    private RiskMetrics calculateRiskMetrics(List<KLineData> klineData, MarketRiskAnalysis marketRisk) {
        List<Double> returns = calculateReturns(klineData);
        
        // 夏普比率
        double riskFreeRate = 0.03; // 假设无风险利率3%
        double avgReturn = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0) * 252; // 年化收益率
        double sharpeRatio = marketRisk.getVolatility() != 0 ? 
                (avgReturn - riskFreeRate) / marketRisk.getVolatility() : 0;
        
        // 索提诺比率
        double sortinoRatio = calculateSortinoRatio(returns, riskFreeRate);
        
        // 信息比率
        double informationRatio = calculateInformationRatio(returns);
        
        // 卡尔马比率
        double calmarRatio = marketRisk.getMaxDrawdown() != 0 ? 
                avgReturn / Math.abs(marketRisk.getMaxDrawdown()) : 0;
        
        // 风险调整收益
        double riskAdjustedReturn = avgReturn - (marketRisk.getVolatility() * 0.5);
        
        return RiskMetrics.builder()
                .sharpeRatio(sharpeRatio)
                .sortinoRatio(sortinoRatio)
                .informationRatio(informationRatio)
                .calmarRatio(calmarRatio)
                .riskAdjustedReturn(riskAdjustedReturn)
                .build();
    }
    
    /**
     * 风险评级计算
     */
    private RiskRating calculateRiskRating(MarketRiskAnalysis marketRisk, CreditRiskAnalysis creditRisk,
                                         LiquidityRiskAnalysis liquidityRisk, OperationalRiskAnalysis operationalRisk) {
        
        // 各维度风险评分（1-10，10为最高风险）
        double marketRiskScore = convertRiskLevelToScore(marketRisk.getRiskLevel());
        double creditRiskScore = convertCreditRatingToScore(creditRisk.getCreditRating());
        double liquidityRiskScore = convertLiquidityLevelToScore(liquidityRisk.getLiquidityLevel());
        double operationalRiskScore = operationalRisk.getRiskScore() * 10;
        
        // 权重分配
        double marketWeight = 0.4;
        double creditWeight = 0.3;
        double liquidityWeight = 0.2;
        double operationalWeight = 0.1;
        
        // 综合评分
        double overallScore = marketRiskScore * marketWeight + 
                            creditRiskScore * creditWeight +
                            liquidityRiskScore * liquidityWeight +
                            operationalRiskScore * operationalWeight;
        
        // 综合评级
        String overallRating = convertScoreToRating(overallScore);
        
        return RiskRating.builder()
                .marketRiskScore(marketRiskScore)
                .creditRiskScore(creditRiskScore)
                .liquidityRiskScore(liquidityRiskScore)
                .operationalRiskScore(operationalRiskScore)
                .overallScore(overallScore)
                .overallRating(overallRating)
                .build();
    }
    
    /**
     * 生成风险控制措施
     */
    private List<RiskControlMeasure> generateRiskControlMeasures(RiskRating riskRating, RiskMetrics riskMetrics) {
        List<RiskControlMeasure> measures = new ArrayList<>();
        
        // 基于综合风险评级的控制措施
        if (riskRating.getOverallScore() >= 8.0) {
            measures.add(RiskControlMeasure.builder()
                    .measureType("仓位控制")
                    .description("建议仓位不超过总资产的5%")
                    .priority("高")
                    .implementation("设置严格的仓位上限，分批建仓")
                    .build());
        } else if (riskRating.getOverallScore() >= 6.0) {
            measures.add(RiskControlMeasure.builder()
                    .measureType("仓位控制")
                    .description("建议仓位不超过总资产的10%")
                    .priority("中")
                    .implementation("适度控制仓位，关注风险变化")
                    .build());
        }
        
        // 基于市场风险的控制措施
        if (riskRating.getMarketRiskScore() >= 7.0) {
            measures.add(RiskControlMeasure.builder()
                    .measureType("止损设置")
                    .description("设置5-8%的止损线")
                    .priority("高")
                    .implementation("严格执行止损策略，避免情绪化决策")
                    .build());
        }
        
        // 基于流动性风险的控制措施
        if (riskRating.getLiquidityRiskScore() >= 6.0) {
            measures.add(RiskControlMeasure.builder()
                    .measureType("交易策略")
                    .description("避免大额集中交易，分批进出")
                    .priority("中")
                    .implementation("采用TWAP或VWAP策略，降低市场冲击")
                    .build());
        }
        
        // 基于夏普比率的控制措施
        if (riskMetrics.getSharpeRatio() < 0.5) {
            measures.add(RiskControlMeasure.builder()
                    .measureType("收益优化")
                    .description("风险调整收益较低，建议重新评估投资价值")
                    .priority("中")
                    .implementation("考虑其他投资机会或调整投资策略")
                    .build());
        }
        
        return measures;
    }
    
    /**
     * 生成LLM分析
     */
    private String generateLLMAnalysis(String stockCode, MarketRiskAnalysis marketRisk, 
                                     CreditRiskAnalysis creditRisk, LiquidityRiskAnalysis liquidityRisk,
                                     RiskMetrics riskMetrics, RiskRating riskRating) {
        
        String prompt = String.format("""
            作为专业的风险管理师，请基于以下风险数据对股票 %s 进行深度风险分析：
            
            市场风险分析：
            - 年化波动率：%.2f%%
            - VaR(95%%)：%.2f%%
            - 最大回撤：%.2f%%
            - Beta系数：%.2f
            - 风险等级：%s
            
            信用风险分析：
            - 信用评级：%s
            - 违约概率：%.2f%%
            - 财务健康评分：%.2f
            - 主要风险因子：%s
            
            流动性风险分析：
            - 换手率：%.2f%%
            - 成交量波动率：%.2f
            - 流动性等级：%s
            - 市场冲击成本：%.2f%%
            
            风险指标：
            - 夏普比率：%.2f
            - 索提诺比率：%.2f
            - 卡尔马比率：%.2f
            - 风险调整收益：%.2f%%
            
            综合风险评级：
            - 市场风险评分：%.1f/10
            - 信用风险评分：%.1f/10
            - 流动性风险评分：%.1f/10
            - 综合评分：%.1f/10
            - 综合评级：%s
            
            请从以下角度进行分析：
            1. 当前主要风险点识别
            2. 各类风险的相互影响
            3. 风险可控性评估
            4. 投资者适用性分析
            5. 风险管控建议
            
            请提供专业、客观的风险分析意见，字数控制在500字以内。
            """, 
            stockCode,
            marketRisk.getVolatility() * 100,
            marketRisk.getVar95() * 100,
            marketRisk.getMaxDrawdown() * 100,
            marketRisk.getBeta(),
            marketRisk.getRiskLevel(),
            creditRisk.getCreditRating(),
            creditRisk.getDefaultProbability() * 100,
            creditRisk.getCreditScore(),
            String.join(", ", creditRisk.getRiskFactors()),
            liquidityRisk.getTurnoverRate() * 100,
            liquidityRisk.getVolumeVolatility(),
            liquidityRisk.getLiquidityLevel(),
            liquidityRisk.getMarketImpactCost() * 100,
            riskMetrics.getSharpeRatio(),
            riskMetrics.getSortinoRatio(),
            riskMetrics.getCalmarRatio(),
            riskMetrics.getRiskAdjustedReturn() * 100,
            riskRating.getMarketRiskScore(),
            riskRating.getCreditRiskScore(),
            riskRating.getLiquidityRiskScore(),
            riskRating.getOverallScore(),
            riskRating.getOverallRating()
        );
        
        return callLLM(prompt);
    }
    
    /**
     * 构建辩论提示词
     */
    private String buildDebatePrompt(int round, RiskAnalysisResult analysisResult, 
                                   List<FundamentalAnalyst.DebateArgument> previousArguments) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append(String.format("""
            作为风险管理师，你正在参与第%d轮投资决策辩论。
            
            你的分析结果：
            - 综合风险评分：%.1f/10
            - 风险评级：%s
            - 投资建议：%s
            - 置信度：%.2f
            
            """, round, analysisResult.getRiskScore(), 
            analysisResult.getRiskRating().getOverallRating(),
            analysisResult.getRecommendation(), analysisResult.getConfidence()));
        
        if (round == 1) {
            prompt.append("""
                第1轮：请阐述你的风险管控观点
                要求：
                1. 基于风险分析提出明确的风险警示
                2. 列举3-5个关键风险点
                3. 说明风险对投资决策的影响
                4. 控制在200字以内
                """);
        } else if (round == 2) {
            prompt.append("\n其他分析师的观点：\n");
            for (FundamentalAnalyst.DebateArgument arg : previousArguments) {
                if (!"risk_manager".equals(arg.getAgentId())) {
                    prompt.append(String.format("- %s: %s\n", arg.getAgentName(), arg.getArgument()));
                }
            }
            prompt.append("""
                \n第2轮：请从风险角度质疑其他观点
                要求：
                1. 指出其他观点忽略的风险因素
                2. 用风险数据支撑你的质疑
                3. 强化风险管控的重要性
                4. 控制在200字以内
                """);
        } else if (round == 3) {
            prompt.append("""
                第3轮：风险与收益的平衡建议
                要求：
                1. 综合风险和收益因素
                2. 提出风险可控的投资策略
                3. 明确风险管控的底线
                4. 控制在200字以内
                """);
        }
        
        return prompt.toString();
    }
    
    // 工具方法
    
    private List<Double> calculateReturns(List<KLineData> klineData) {
        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < klineData.size(); i++) {
            double prevClose = klineData.get(i - 1).getClosePrice();
            double currentClose = klineData.get(i).getClosePrice();
            double returnRate = (currentClose - prevClose) / prevClose;
            returns.add(returnRate);
        }
        return returns;
    }
    
    private double calculateVolatility(List<Double> returns) {
        if (returns.isEmpty()) return 0.0;
        
        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = returns.stream()
                .mapToDouble(r -> Math.pow(r - mean, 2))
                .average()
                .orElse(0);
        
        return Math.sqrt(variance);
    }
    
    private double calculateVaR(List<Double> returns, double confidence) {
        if (returns.isEmpty()) return 0.0;
        
        List<Double> sortedReturns = returns.stream().sorted().collect(Collectors.toList());
        int index = (int) ((1 - confidence) * sortedReturns.size());
        
        return index < sortedReturns.size() ? sortedReturns.get(index) : sortedReturns.get(sortedReturns.size() - 1);
    }
    
    private double calculateMaxDrawdown(List<KLineData> klineData) {
        if (klineData.isEmpty()) return 0.0;
        
        double maxPrice = klineData.get(0).getClosePrice();
        double maxDrawdown = 0.0;
        
        for (KLineData data : klineData) {
            double currentPrice = data.getClosePrice();
            if (currentPrice > maxPrice) {
                maxPrice = currentPrice;
            } else {
                double drawdown = (maxPrice - currentPrice) / maxPrice;
                maxDrawdown = Math.max(maxDrawdown, drawdown);
            }
        }
        
        return maxDrawdown;
    }
    
    private double calculateBeta(List<Double> returns) {
        // 简化实现，假设市场收益率
        double marketReturn = 0.08 / 252; // 日均市场收益率
        double stockReturn = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double stockVolatility = calculateVolatility(returns);
        double marketVolatility = 0.15 / Math.sqrt(252); // 假设市场波动率
        
        // 简化的Beta计算
        return stockVolatility / marketVolatility;
    }
    
    private PriceRiskAssessment assessPriceRisk(List<KLineData> klineData, StockQuoteData currentQuote) {
        if (klineData.isEmpty()) {
            return PriceRiskAssessment.builder()
                    .priceVolatility(0.0)
                    .supportLevel(currentQuote.getCurrentPrice())
                    .resistanceLevel(currentQuote.getCurrentPrice())
                    .riskLevel("数据不足")
                    .build();
        }
        
        // 价格波动性
        List<Double> prices = klineData.stream().map(KLineData::getClosePrice).collect(Collectors.toList());
        double priceVolatility = calculateVolatility(calculateReturns(klineData));
        
        // 支撑位和阻力位（简化计算）
        double supportLevel = prices.stream().mapToDouble(Double::doubleValue).min().orElse(0) * 1.05;
        double resistanceLevel = prices.stream().mapToDouble(Double::doubleValue).max().orElse(0) * 0.95;
        
        // 价格风险等级
        String riskLevel = priceVolatility > 0.03 ? "高风险" : priceVolatility > 0.02 ? "中等风险" : "低风险";
        
        return PriceRiskAssessment.builder()
                .priceVolatility(priceVolatility)
                .supportLevel(supportLevel)
                .resistanceLevel(resistanceLevel)
                .riskLevel(riskLevel)
                .build();
    }
    
    private String determineMarketRiskLevel(double volatility, double var95, double maxDrawdown) {
        int riskScore = 0;
        
        if (volatility > 0.3) riskScore += 3;
        else if (volatility > 0.2) riskScore += 2;
        else if (volatility > 0.15) riskScore += 1;
        
        if (Math.abs(var95) > 0.05) riskScore += 2;
        else if (Math.abs(var95) > 0.03) riskScore += 1;
        
        if (maxDrawdown > 0.3) riskScore += 2;
        else if (maxDrawdown > 0.2) riskScore += 1;
        
        if (riskScore >= 5) return "高风险";
        if (riskScore >= 3) return "中等风险";
        return "低风险";
    }
    
    private double calculateFinancialHealthScore(FinancialData data) {
        double score = 5.0; // 基础分
        
        // ROE评分
        if (data.getRoe() > 15) score += 1.5;
        else if (data.getRoe() > 10) score += 1.0;
        else if (data.getRoe() < 5) score -= 1.0;
        
        // 资产负债率评分
        if (data.getDebtToAssetRatio() < 0.3) score += 1.0;
        else if (data.getDebtToAssetRatio() > 0.7) score -= 1.5;
        
        // 流动比率评分
        if (data.getCurrentRatio() > 2.0) score += 0.5;
        else if (data.getCurrentRatio() < 1.0) score -= 1.0;
        
        // 净利润增长率（简化）
        if (data.getNetProfit() > 0) score += 0.5;
        else score -= 1.0;
        
        return Math.max(0, Math.min(10, score));
    }
    
    private double calculateDefaultProbability(FinancialData data) {
        // 简化的违约概率模型
        double probability = 0.05; // 基础概率5%
        
        if (data.getDebtToAssetRatio() > 0.8) probability += 0.1;
        if (data.getCurrentRatio() < 1.0) probability += 0.05;
        if (data.getRoe() < 0) probability += 0.08;
        if (data.getNetProfit() < 0) probability += 0.05;
        
        return Math.min(probability, 0.5); // 最高50%
    }
    
    private String determineCreditRating(double healthScore, double defaultProbability) {
        if (healthScore >= 8.0 && defaultProbability < 0.02) return "AAA";
        if (healthScore >= 7.0 && defaultProbability < 0.05) return "AA";
        if (healthScore >= 6.0 && defaultProbability < 0.1) return "A";
        if (healthScore >= 5.0 && defaultProbability < 0.15) return "BBB";
        if (healthScore >= 4.0 && defaultProbability < 0.25) return "BB";
        if (healthScore >= 3.0 && defaultProbability < 0.35) return "B";
        return "C";
    }
    
    private List<String> identifyCreditRiskFactors(FinancialData data) {
        List<String> factors = new ArrayList<>();
        
        if (data.getDebtToAssetRatio() > 0.6) factors.add("高负债率");
        if (data.getCurrentRatio() < 1.5) factors.add("流动性不足");
        if (data.getRoe() < 5) factors.add("盈利能力弱");
        if (data.getNetProfit() < 0) factors.add("亏损状态");
        if (data.getGrossProfitMargin() < 20) factors.add("毛利率偏低");
        
        return factors.isEmpty() ? Arrays.asList("无明显风险因子") : factors;
    }
    
    private DebtAnalysis analyzeDebtStructure(FinancialData data) {
        double debtRatio = data.getDebtToAssetRatio();
        double currentRatio = data.getCurrentRatio();
        
        String debtLevel = debtRatio > 0.7 ? "高负债" : debtRatio > 0.4 ? "中等负债" : "低负债";
        String liquidityStatus = currentRatio > 2.0 ? "流动性充足" : currentRatio > 1.0 ? "流动性一般" : "流动性紧张";
        
        return DebtAnalysis.builder()
                .debtToAssetRatio(debtRatio)
                .currentRatio(currentRatio)
                .debtLevel(debtLevel)
                .liquidityStatus(liquidityStatus)
                .build();
    }
    
    private double calculateVolumeVolatility(List<Long> volumes) {
        if (volumes.size() < 2) return 0.0;
        
        List<Double> volumeReturns = new ArrayList<>();
        for (int i = 1; i < volumes.size(); i++) {
            double prevVolume = volumes.get(i - 1).doubleValue();
            double currentVolume = volumes.get(i).doubleValue();
            if (prevVolume > 0) {
                volumeReturns.add((currentVolume - prevVolume) / prevVolume);
            }
        }
        
        return calculateVolatility(volumeReturns);
    }
    
    private double calculateTurnoverRate(StockQuoteData quote, double avgVolume) {
        // 简化的换手率计算
        double totalShares = 1000000000; // 假设总股本10亿股
        return avgVolume / totalShares;
    }
    
    private double calculateBidAskSpread(StockQuoteData quote) {
        // 简化实现，假设买卖价差
        return 0.01; // 假设1%的买卖价差
    }
    
    private String determineLiquidityLevel(double turnoverRate, double volumeVolatility, double bidAskSpread) {
        int liquidityScore = 0;
        
        if (turnoverRate > 0.05) liquidityScore += 2;
        else if (turnoverRate > 0.02) liquidityScore += 1;
        
        if (volumeVolatility < 0.3) liquidityScore += 1;
        
        if (bidAskSpread < 0.02) liquidityScore += 1;
        
        if (liquidityScore >= 3) return "高流动性";
        if (liquidityScore >= 2) return "中等流动性";
        return "低流动性";
    }
    
    private double calculateMarketImpactCost(double turnoverRate, double volumeVolatility) {
        // 简化的市场冲击成本模型
        double baseCost = 0.005; // 基础成本0.5%
        
        if (turnoverRate < 0.01) baseCost += 0.01; // 低流动性增加成本
        if (volumeVolatility > 0.5) baseCost += 0.005; // 高波动性增加成本
        
        return baseCost;
    }
    
    private boolean isHighRegulatoryRisk(String stockCode) {
        // 简化实现，基于股票代码判断
        // 实际应该查询监管数据库
        return stockCode.startsWith("ST") || stockCode.startsWith("*ST");
    }
    
    private String assessIndustryRisk(String stockCode) {
        // 简化的行业风险评估
        // 实际应该基于行业分类和风险数据库
        return "中等风险";
    }
    
    private String assessGovernanceRisk(String stockCode) {
        // 简化的公司治理风险评估
        return "良好";
    }
    
    private boolean hasTechnicalRisk(Map<String, Object> context) {
        // 检查技术系统风险
        return false;
    }
    
    private String determineOperationalRiskLevel(double riskScore) {
        if (riskScore >= 0.4) return "高风险";
        if (riskScore >= 0.2) return "中等风险";
        return "低风险";
    }
    
    private double calculateSortinoRatio(List<Double> returns, double riskFreeRate) {
        double avgReturn = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0) * 252;
        
        // 计算下行偏差
        double downside = returns.stream()
                .filter(r -> r < 0)
                .mapToDouble(r -> r * r)
                .average()
                .orElse(0);
        
        double downsideDeviation = Math.sqrt(downside) * Math.sqrt(252);
        
        return downsideDeviation != 0 ? (avgReturn - riskFreeRate) / downsideDeviation : 0;
    }
    
    private double calculateInformationRatio(List<Double> returns) {
        // 简化的信息比率计算
        double avgReturn = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double trackingError = calculateVolatility(returns);
        
        return trackingError != 0 ? avgReturn / trackingError : 0;
    }
    
    private double convertRiskLevelToScore(String riskLevel) {
        switch (riskLevel) {
            case "低风险": return 3.0;
            case "中等风险": return 6.0;
            case "高风险": return 9.0;
            default: return 5.0;
        }
    }
    
    private double convertCreditRatingToScore(String rating) {
        switch (rating) {
            case "AAA": return 1.0;
            case "AA": return 2.0;
            case "A": return 3.0;
            case "BBB": return 5.0;
            case "BB": return 7.0;
            case "B": return 8.5;
            case "C": return 10.0;
            default: return 5.0;
        }
    }
    
    private double convertLiquidityLevelToScore(String liquidityLevel) {
        switch (liquidityLevel) {
            case "高流动性": return 2.0;
            case "中等流动性": return 5.0;
            case "低流动性": return 8.0;
            default: return 5.0;
        }
    }
    
    private String convertScoreToRating(double score) {
        if (score >= 8.5) return "高风险";
        if (score >= 6.5) return "中高风险";
        if (score >= 4.5) return "中等风险";
        if (score >= 2.5) return "中低风险";
        return "低风险";
    }
    
    private double calculateOverallRiskScore(RiskRating riskRating, RiskMetrics riskMetrics) {
        double score = riskRating.getOverallScore();
        
        // 基于风险指标调整
        if (riskMetrics.getSharpeRatio() < 0) score += 1.0;
        if (riskMetrics.getCalmarRatio() < 0.5) score += 0.5;
        
        return Math.max(0, Math.min(10, score));
    }
    
    private String generateRecommendation(double riskScore, RiskRating riskRating) {
        if (riskScore >= 8.0) {
            return "高风险，不建议投资";
        } else if (riskScore >= 6.0) {
            return "风险较高，谨慎投资";
        } else if (riskScore >= 4.0) {
            return "风险适中，可适量投资";
        } else {
            return "风险较低，可正常投资";
        }
    }
    
    private double calculateConfidence(RiskMetrics riskMetrics, RiskRating riskRating) {
        double confidence = 0.5; // 基础置信度
        
        // 基于数据完整性调整
        if (riskMetrics.getSharpeRatio() != 0) confidence += 0.2;
        if (riskRating.getOverallScore() > 0) confidence += 0.2;
        
        // 基于风险一致性调整
        if (Math.abs(riskRating.getMarketRiskScore() - riskRating.getCreditRiskScore()) < 2.0) {
            confidence += 0.1;
        }
        
        return Math.min(1.0, confidence);
    }
    
    private RiskAnalysisResult createErrorResult(String errorMessage) {
        return RiskAnalysisResult.builder()
                .agentId("risk_manager")
                .agentName("风险管理师")
                .analysis("分析失败: " + errorMessage)
                .riskScore(10.0) // 最高风险
                .recommendation("无法分析")
                .confidence(0.0)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    private FundamentalAnalyst.DebateArgument createErrorDebateArgument(String errorMessage) {
        return FundamentalAnalyst.DebateArgument.builder()
                .agentId("risk_manager")
                .agentName("风险管理师")
                .argument("无法参与辩论: " + errorMessage)
                .confidence(0.0)
                .evidenceType("ERROR")
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    // 数据模型类
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RiskAnalysisResult {
        private String stockCode;
        private String agentId;
        private String agentName;
        private String analysis;
        private MarketRiskAnalysis marketRisk;
        private CreditRiskAnalysis creditRisk;
        private LiquidityRiskAnalysis liquidityRisk;
        private OperationalRiskAnalysis operationalRisk;
        private RiskMetrics riskMetrics;
        private RiskRating riskRating;
        private List<RiskControlMeasure> controlMeasures;
        private Double riskScore;
        private String recommendation;
        private Double confidence;
        private Long timestamp;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MarketRiskAnalysis {
        private Double volatility;
        private Double var95;
        private Double var99;
        private Double maxDrawdown;
        private Double beta;
        private PriceRiskAssessment priceRisk;
        private String riskLevel;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PriceRiskAssessment {
        private Double priceVolatility;
        private Double supportLevel;
        private Double resistanceLevel;
        private String riskLevel;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreditRiskAnalysis {
        private String creditRating;
        private Double defaultProbability;
        private Double creditScore;
        private List<String> riskFactors;
        private DebtAnalysis debtAnalysis;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DebtAnalysis {
        private Double debtToAssetRatio;
        private Double currentRatio;
        private String debtLevel;
        private String liquidityStatus;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LiquidityRiskAnalysis {
        private Double avgVolume;
        private Double volumeVolatility;
        private Double turnoverRate;
        private Double bidAskSpread;
        private String liquidityLevel;
        private Double marketImpactCost;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OperationalRiskAnalysis {
        private List<String> riskFactors;
        private Double riskScore;
        private String overallLevel;
        private Boolean regulatoryRisk;
        private String industryRisk;
        private String governanceRisk;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RiskMetrics {
        private Double sharpeRatio;
        private Double sortinoRatio;
        private Double informationRatio;
        private Double calmarRatio;
        private Double riskAdjustedReturn;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RiskRating {
        private Double marketRiskScore;
        private Double creditRiskScore;
        private Double liquidityRiskScore;
        private Double operationalRiskScore;
        private Double overallScore;
        private String overallRating;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RiskControlMeasure {
        private String measureType;
        private String description;
        private String priority;
        private String implementation;
    }
}