package com.jd.genie.agent.agent.stock;

import com.jd.genie.agent.agent.BaseAgent;
import com.jd.genie.agent.enums.AgentType;
import com.jd.genie.service.NewsDataMigrationService;
import com.jd.genie.service.NewsDataMigrationService.NewsItem;
import com.jd.genie.service.StockDataMigrationService;
import com.jd.genie.service.StockDataMigrationService.KLineData;
import com.jd.genie.service.StockDataMigrationService.StockQuoteData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 市场分析师智能体
 * 基于TradingAgents框架的专业市场分析师角色
 * 
 * 职责：
 * - 宏观经济分析
 * - 行业趋势分析
 * - 市场情绪监测
 * - 政策影响评估
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component("market_analyst")
public class MarketAnalyst extends BaseAgent {
    
    @Autowired
    private StockDataMigrationService stockDataService;
    
    @Autowired
    private NewsDataMigrationService newsDataService;
    
    public MarketAnalyst() {
        super();
        this.agentType = AgentType.ANALYSIS;
        this.agentName = "市场分析师";
        this.description = "专业的市场分析师，擅长宏观经济、行业趋势和市场情绪分析";
    }
    
    /**
     * 执行市场分析
     * 
     * @param stockCode 股票代码
     * @param context 分析上下文
     * @return 分析结果
     */
    public CompletableFuture<MarketAnalysisResult> analyze(String stockCode, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("市场分析师开始分析股票: {}", stockCode);
                
                // 1. 获取股票基础信息
                List<StockQuoteData> quoteData = stockDataService.getRealTimeStockData(List.of(stockCode)).get();
                if (quoteData.isEmpty()) {
                    return createErrorResult("无法获取股票数据");
                }
                
                StockQuoteData currentQuote = quoteData.get(0);
                String industry = extractIndustryFromContext(context, stockCode);
                
                // 2. 宏观经济分析
                MacroEconomicAnalysis macroAnalysis = performMacroEconomicAnalysis(stockCode, context);
                
                // 3. 行业分析
                IndustryAnalysis industryAnalysis = performIndustryAnalysis(industry, stockCode, context);
                
                // 4. 市场情绪分析
                MarketSentimentAnalysis sentimentAnalysis = performMarketSentimentAnalysis(stockCode);
                
                // 5. 政策影响分析
                PolicyImpactAnalysis policyAnalysis = performPolicyImpactAnalysis(stockCode, industry);
                
                // 6. 资金流向分析
                CapitalFlowAnalysis capitalFlowAnalysis = performCapitalFlowAnalysis(stockCode);
                
                // 7. 市场周期分析
                MarketCycleAnalysis cycleAnalysis = performMarketCycleAnalysis(stockCode);
                
                // 8. 生成LLM分析
                String llmAnalysis = generateLLMAnalysis(stockCode, macroAnalysis, industryAnalysis, 
                                                       sentimentAnalysis, policyAnalysis, capitalFlowAnalysis, cycleAnalysis);
                
                // 9. 计算市场评分
                double marketScore = calculateMarketScore(macroAnalysis, industryAnalysis, sentimentAnalysis, 
                                                        policyAnalysis, capitalFlowAnalysis, cycleAnalysis);
                
                // 10. 生成投资建议
                String recommendation = generateRecommendation(marketScore, macroAnalysis, industryAnalysis, sentimentAnalysis);
                
                return MarketAnalysisResult.builder()
                        .stockCode(stockCode)
                        .agentId("market_analyst")
                        .agentName("市场分析师")
                        .analysis(llmAnalysis)
                        .macroAnalysis(macroAnalysis)
                        .industryAnalysis(industryAnalysis)
                        .sentimentAnalysis(sentimentAnalysis)
                        .policyAnalysis(policyAnalysis)
                        .capitalFlowAnalysis(capitalFlowAnalysis)
                        .cycleAnalysis(cycleAnalysis)
                        .marketScore(marketScore)
                        .recommendation(recommendation)
                        .confidence(calculateConfidence(marketScore, sentimentAnalysis, industryAnalysis))
                        .timestamp(System.currentTimeMillis())
                        .build();
                
            } catch (Exception e) {
                log.error("市场分析失败: {}", stockCode, e);
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
            
            // 获取市场分析结果
            MarketAnalysisResult analysisResult = (MarketAnalysisResult) context.getAgentResult("market_analyst");
            
            String prompt = buildDebatePrompt(currentRound, analysisResult, previousArguments);
            
            // 调用LLM生成辩论论据
            String argument = callLLM(prompt);
            
            return FundamentalAnalyst.DebateArgument.builder()
                    .agentId("market_analyst")
                    .agentName("市场分析师")
                    .round(currentRound)
                    .argument(argument)
                    .confidence(analysisResult.getConfidence())
                    .evidenceType("MARKET")
                    .supportingData(Map.of(
                        "macroAnalysis", analysisResult.getMacroAnalysis(),
                        "industryAnalysis", analysisResult.getIndustryAnalysis(),
                        "sentimentAnalysis", analysisResult.getSentimentAnalysis(),
                        "marketScore", analysisResult.getMarketScore()
                    ))
                    .timestamp(System.currentTimeMillis())
                    .build();
                    
        } catch (Exception e) {
            log.error("市场分析师辩论失败", e);
            return createErrorDebateArgument("辩论过程中发生错误");
        }
    }
    
    /**
     * 宏观经济分析
     */
    private MacroEconomicAnalysis performMacroEconomicAnalysis(String stockCode, Map<String, Object> context) {
        // 经济指标分析
        EconomicIndicators indicators = analyzeEconomicIndicators();
        
        // 货币政策分析
        MonetaryPolicyAnalysis monetaryPolicy = analyzeMonetaryPolicy();
        
        // 财政政策分析
        FiscalPolicyAnalysis fiscalPolicy = analyzeFiscalPolicy();
        
        // 国际环境分析
        InternationalEnvironmentAnalysis internationalEnv = analyzeInternationalEnvironment();
        
        // 宏观风险评估
        List<MacroRisk> macroRisks = assessMacroRisks(indicators, monetaryPolicy, fiscalPolicy, internationalEnv);
        
        // 宏观影响评分
        double macroImpactScore = calculateMacroImpactScore(indicators, monetaryPolicy, fiscalPolicy, internationalEnv);
        
        return MacroEconomicAnalysis.builder()
                .indicators(indicators)
                .monetaryPolicy(monetaryPolicy)
                .fiscalPolicy(fiscalPolicy)
                .internationalEnvironment(internationalEnv)
                .macroRisks(macroRisks)
                .macroImpactScore(macroImpactScore)
                .build();
    }
    
    /**
     * 行业分析
     */
    private IndustryAnalysis performIndustryAnalysis(String industry, String stockCode, Map<String, Object> context) {
        // 行业生命周期分析
        IndustryLifeCycle lifeCycle = analyzeIndustryLifeCycle(industry);
        
        // 行业竞争格局
        CompetitiveStructure competitiveStructure = analyzeCompetitiveStructure(industry, stockCode);
        
        // 行业政策环境
        IndustryPolicyEnvironment policyEnvironment = analyzeIndustryPolicyEnvironment(industry);
        
        // 行业技术趋势
        TechnologyTrend technologyTrend = analyzeTechnologyTrend(industry);
        
        // 行业估值水平
        IndustryValuation valuation = analyzeIndustryValuation(industry);
        
        // 行业资金流向
        IndustryCapitalFlow capitalFlow = analyzeIndustryCapitalFlow(industry);
        
        // 行业前景评估
        double industryProspectScore = calculateIndustryProspectScore(lifeCycle, competitiveStructure, 
                                                                    policyEnvironment, technologyTrend, valuation);
        
        return IndustryAnalysis.builder()
                .industry(industry)
                .lifeCycle(lifeCycle)
                .competitiveStructure(competitiveStructure)
                .policyEnvironment(policyEnvironment)
                .technologyTrend(technologyTrend)
                .valuation(valuation)
                .capitalFlow(capitalFlow)
                .prospectScore(industryProspectScore)
                .build();
    }
    
    /**
     * 市场情绪分析
     */
    private MarketSentimentAnalysis performMarketSentimentAnalysis(String stockCode) {
        try {
            // 获取相关新闻
            List<NewsItem> stockNews = newsDataService.getStockRelatedNews(stockCode, 50).get();
            List<NewsItem> marketNews = newsDataService.getMarketHotNews(30).get();
            
            // 新闻情绪分析
            NewsSentimentMetrics newsSentiment = analyzeNewsSentiment(stockNews, marketNews);
            
            // 市场技术情绪
            TechnicalSentimentMetrics technicalSentiment = analyzeTechnicalSentiment(stockCode);
            
            // 投资者情绪
            InvestorSentimentMetrics investorSentiment = analyzeInvestorSentiment(stockCode);
            
            // 恐慌贪婪指数
            FearGreedIndex fearGreedIndex = calculateFearGreedIndex(newsSentiment, technicalSentiment, investorSentiment);
            
            // 情绪趋势
            SentimentTrend sentimentTrend = analyzeSentimentTrend(newsSentiment, technicalSentiment, investorSentiment);
            
            // 综合情绪评分
            double overallSentimentScore = calculateOverallSentimentScore(newsSentiment, technicalSentiment, 
                                                                        investorSentiment, fearGreedIndex);
            
            return MarketSentimentAnalysis.builder()
                    .newsSentiment(newsSentiment)
                    .technicalSentiment(technicalSentiment)
                    .investorSentiment(investorSentiment)
                    .fearGreedIndex(fearGreedIndex)
                    .sentimentTrend(sentimentTrend)
                    .overallSentimentScore(overallSentimentScore)
                    .build();
                    
        } catch (Exception e) {
            log.error("市场情绪分析失败", e);
            return createDefaultSentimentAnalysis();
        }
    }
    
    /**
     * 政策影响分析
     */
    private PolicyImpactAnalysis performPolicyImpactAnalysis(String stockCode, String industry) {
        try {
            // 获取政策相关新闻
            List<NewsItem> policyNews = newsDataService.getPolicyNews(industry, 20).get();
            
            // 政策类型分析
            List<PolicyType> policyTypes = analyzePolicyTypes(policyNews, industry);
            
            // 政策影响评估
            List<PolicyImpact> policyImpacts = assessPolicyImpacts(policyTypes, stockCode, industry);
            
            // 政策风险评估
            List<PolicyRisk> policyRisks = assessPolicyRisks(policyTypes, industry);
            
            // 政策机会识别
            List<PolicyOpportunity> policyOpportunities = identifyPolicyOpportunities(policyTypes, industry);
            
            // 政策影响评分
            double policyImpactScore = calculatePolicyImpactScore(policyImpacts, policyRisks, policyOpportunities);
            
            return PolicyImpactAnalysis.builder()
                    .policyTypes(policyTypes)
                    .policyImpacts(policyImpacts)
                    .policyRisks(policyRisks)
                    .policyOpportunities(policyOpportunities)
                    .policyImpactScore(policyImpactScore)
                    .build();
                    
        } catch (Exception e) {
            log.error("政策影响分析失败", e);
            return createDefaultPolicyAnalysis();
        }
    }
    
    /**
     * 资金流向分析
     */
    private CapitalFlowAnalysis performCapitalFlowAnalysis(String stockCode) {
        try {
            // 获取资金流向数据
            List<KLineData> klineData = stockDataService.getKLineData(stockCode, "daily", 60).get();
            
            // 主力资金分析
            MainCapitalFlow mainCapitalFlow = analyzeMainCapitalFlow(klineData);
            
            // 散户资金分析
            RetailCapitalFlow retailCapitalFlow = analyzeRetailCapitalFlow(klineData);
            
            // 机构资金分析
            InstitutionalCapitalFlow institutionalFlow = analyzeInstitutionalCapitalFlow(stockCode);
            
            // 外资流向分析
            ForeignCapitalFlow foreignFlow = analyzeForeignCapitalFlow(stockCode);
            
            // 资金流向趋势
            CapitalFlowTrend flowTrend = analyzeCapitalFlowTrend(mainCapitalFlow, retailCapitalFlow, 
                                                               institutionalFlow, foreignFlow);
            
            // 资金流向评分
            double capitalFlowScore = calculateCapitalFlowScore(mainCapitalFlow, institutionalFlow, foreignFlow);
            
            return CapitalFlowAnalysis.builder()
                    .mainCapitalFlow(mainCapitalFlow)
                    .retailCapitalFlow(retailCapitalFlow)
                    .institutionalFlow(institutionalFlow)
                    .foreignFlow(foreignFlow)
                    .flowTrend(flowTrend)
                    .capitalFlowScore(capitalFlowScore)
                    .build();
                    
        } catch (Exception e) {
            log.error("资金流向分析失败", e);
            return createDefaultCapitalFlowAnalysis();
        }
    }
    
    /**
     * 市场周期分析
     */
    private MarketCycleAnalysis performMarketCycleAnalysis(String stockCode) {
        try {
            // 获取长期历史数据
            List<KLineData> longTermData = stockDataService.getKLineData(stockCode, "daily", 1000).get(); // 4年数据
            
            // 牛熊周期识别
            BullBearCycle bullBearCycle = identifyBullBearCycle(longTermData);
            
            // 行业周期分析
            IndustryCycle industryCycle = analyzeIndustryCycle(stockCode);
            
            // 经济周期位置
            EconomicCyclePosition economicCyclePosition = identifyEconomicCyclePosition();
            
            // 周期性特征
            CyclicalCharacteristics cyclicalCharacteristics = analyzeCyclicalCharacteristics(longTermData);
            
            // 周期预测
            CycleForecast cycleForecast = forecastCycle(bullBearCycle, industryCycle, economicCyclePosition);
            
            // 周期评分
            double cycleScore = calculateCycleScore(bullBearCycle, industryCycle, economicCyclePosition, cyclicalCharacteristics);
            
            return MarketCycleAnalysis.builder()
                    .bullBearCycle(bullBearCycle)
                    .industryCycle(industryCycle)
                    .economicCyclePosition(economicCyclePosition)
                    .cyclicalCharacteristics(cyclicalCharacteristics)
                    .cycleForecast(cycleForecast)
                    .cycleScore(cycleScore)
                    .build();
                    
        } catch (Exception e) {
            log.error("市场周期分析失败", e);
            return createDefaultCycleAnalysis();
        }
    }
    
    /**
     * 生成LLM分析
     */
    private String generateLLMAnalysis(String stockCode, MacroEconomicAnalysis macroAnalysis, 
                                     IndustryAnalysis industryAnalysis, MarketSentimentAnalysis sentimentAnalysis,
                                     PolicyImpactAnalysis policyAnalysis, CapitalFlowAnalysis capitalFlowAnalysis,
                                     MarketCycleAnalysis cycleAnalysis) {
        
        String prompt = String.format("""
            作为专业的市场分析师，请基于以下市场数据对股票 %s 进行深度分析：
            
            宏观经济分析：
            - 宏观影响评分：%.2f/10
            - 经济增长趋势：%s
            - 货币政策环境：%s
            - 财政政策影响：%s
            
            行业分析：
            - 行业：%s
            - 行业前景评分：%.2f/10
            - 行业生命周期：%s
            - 竞争格局：%s
            - 政策环境：%s
            
            市场情绪分析：
            - 综合情绪评分：%.2f/10
            - 新闻情绪：%s
            - 技术情绪：%s
            - 投资者情绪：%s
            - 恐慌贪婪指数：%.2f
            
            政策影响分析：
            - 政策影响评分：%.2f/10
            - 主要政策类型：%s
            - 政策风险等级：%s
            
            资金流向分析：
            - 资金流向评分：%.2f/10
            - 主力资金：%s
            - 机构资金：%s
            - 外资流向：%s
            
            市场周期分析：
            - 周期评分：%.2f/10
            - 牛熊周期：%s
            - 行业周期：%s
            - 经济周期位置：%s
            
            请从以下角度进行分析：
            1. 宏观环境对该股票的影响
            2. 行业趋势和竞争态势
            3. 市场情绪和资金面分析
            4. 政策机遇与风险
            5. 周期性投资机会
            6. 综合市场建议
            
            请提供专业、客观的市场分析意见，字数控制在600字以内。
            """, 
            stockCode,
            macroAnalysis.getMacroImpactScore(),
            macroAnalysis.getIndicators() != null ? macroAnalysis.getIndicators().getGdpGrowthTrend() : "未知",
            macroAnalysis.getMonetaryPolicy() != null ? macroAnalysis.getMonetaryPolicy().getPolicyStance() : "未知",
            macroAnalysis.getFiscalPolicy() != null ? macroAnalysis.getFiscalPolicy().getPolicyDirection() : "未知",
            industryAnalysis.getIndustry(),
            industryAnalysis.getProspectScore(),
            industryAnalysis.getLifeCycle() != null ? industryAnalysis.getLifeCycle().getCurrentStage() : "未知",
            industryAnalysis.getCompetitiveStructure() != null ? industryAnalysis.getCompetitiveStructure().getMarketStructure() : "未知",
            industryAnalysis.getPolicyEnvironment() != null ? industryAnalysis.getPolicyEnvironment().getSupportLevel() : "未知",
            sentimentAnalysis.getOverallSentimentScore(),
            sentimentAnalysis.getNewsSentiment() != null ? sentimentAnalysis.getNewsSentiment().getSentimentLabel() : "中性",
            sentimentAnalysis.getTechnicalSentiment() != null ? sentimentAnalysis.getTechnicalSentiment().getTrendSentiment() : "中性",
            sentimentAnalysis.getInvestorSentiment() != null ? sentimentAnalysis.getInvestorSentiment().getSentimentLevel() : "中性",
            sentimentAnalysis.getFearGreedIndex() != null ? sentimentAnalysis.getFearGreedIndex().getIndexValue() : 50.0,
            policyAnalysis.getPolicyImpactScore(),
            policyAnalysis.getPolicyTypes() != null && !policyAnalysis.getPolicyTypes().isEmpty() ? 
                policyAnalysis.getPolicyTypes().get(0).getPolicyCategory() : "无",
            policyAnalysis.getPolicyRisks() != null && !policyAnalysis.getPolicyRisks().isEmpty() ? 
                policyAnalysis.getPolicyRisks().get(0).getRiskLevel() : "低",
            capitalFlowAnalysis.getCapitalFlowScore(),
            capitalFlowAnalysis.getMainCapitalFlow() != null ? capitalFlowAnalysis.getMainCapitalFlow().getFlowDirection() : "平衡",
            capitalFlowAnalysis.getInstitutionalFlow() != null ? capitalFlowAnalysis.getInstitutionalFlow().getFlowTrend() : "平衡",
            capitalFlowAnalysis.getForeignFlow() != null ? capitalFlowAnalysis.getForeignFlow().getFlowDirection() : "平衡",
            cycleAnalysis.getCycleScore(),
            cycleAnalysis.getBullBearCycle() != null ? cycleAnalysis.getBullBearCycle().getCurrentPhase() : "未知",
            cycleAnalysis.getIndustryCycle() != null ? cycleAnalysis.getIndustryCycle().getCycleStage() : "未知",
            cycleAnalysis.getEconomicCyclePosition() != null ? cycleAnalysis.getEconomicCyclePosition().getCurrentPosition() : "未知"
        );
        
        return callLLM(prompt);
    }
    
    /**
     * 构建辩论提示词
     */
    private String buildDebatePrompt(int round, MarketAnalysisResult analysisResult, 
                                   List<FundamentalAnalyst.DebateArgument> previousArguments) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append(String.format("""
            作为市场分析师，你正在参与第%d轮投资决策辩论。
            
            你的分析结果：
            - 市场评分：%.2f/10
            - 投资建议：%s
            - 置信度：%.2f
            - 宏观影响：%.2f/10
            - 行业前景：%.2f/10
            - 市场情绪：%.2f/10
            
            """, round, analysisResult.getMarketScore(), 
            analysisResult.getRecommendation(), analysisResult.getConfidence(),
            analysisResult.getMacroAnalysis().getMacroImpactScore(),
            analysisResult.getIndustryAnalysis().getProspectScore(),
            analysisResult.getSentimentAnalysis().getOverallSentimentScore()));
        
        if (round == 1) {
            prompt.append("""
                第1轮：请阐述你的市场分析观点
                要求：
                1. 从宏观、行业、情绪三个维度分析
                2. 重点说明市场环境对投资的影响
                3. 提供明确的市场时机判断
                4. 控制在200字以内
                """);
        } else if (round == 2) {
            prompt.append("\n其他分析师的观点：\n");
            for (FundamentalAnalyst.DebateArgument arg : previousArguments) {
                if (!"market_analyst".equals(arg.getAgentId())) {
                    prompt.append(String.format("- %s: %s\n", arg.getAgentName(), arg.getArgument()));
                }
            }
            prompt.append("""
                \n第2轮：请从市场角度质疑或支持其他观点
                要求：
                1. 用市场数据和趋势支撑你的观点
                2. 指出其他分析可能忽略的市场因素
                3. 强调市场时机的重要性
                4. 控制在200字以内
                """);
        } else if (round == 3) {
            prompt.append("""
                第3轮：市场环境下的最终投资建议
                要求：
                1. 综合考虑当前市场环境
                2. 平衡短期市场波动和长期趋势
                3. 提供具体的投资时机建议
                4. 控制在200字以内
                """);
        }
        
        return prompt.toString();
    }
    
    // ==================== 分析方法实现 ====================
    
    private String extractIndustryFromContext(Map<String, Object> context, String stockCode) {
        // 从上下文中提取行业信息，或通过股票代码查询
        if (context.containsKey("industry")) {
            return (String) context.get("industry");
        }
        
        // 简化实现：根据股票代码前缀判断行业
        if (stockCode.startsWith("00")) {
            return "主板";
        } else if (stockCode.startsWith("30")) {
            return "创业板";
        } else if (stockCode.startsWith("60")) {
            return "沪市主板";
        } else {
            return "其他";
        }
    }
    
    private EconomicIndicators analyzeEconomicIndicators() {
        // 简化实现：模拟经济指标分析
        return EconomicIndicators.builder()
                .gdpGrowthRate(5.2) // 模拟GDP增长率
                .gdpGrowthTrend("稳定增长")
                .inflationRate(2.1) // 模拟通胀率
                .inflationTrend("温和通胀")
                .unemploymentRate(5.5) // 模拟失业率
                .unemploymentTrend("稳定")
                .pmiIndex(51.2) // 模拟PMI指数
                .pmiTrend("扩张")
                .consumerConfidenceIndex(108.5) // 模拟消费者信心指数
                .consumerConfidenceTrend("乐观")
                .build();
    }
    
    private MonetaryPolicyAnalysis analyzeMonetaryPolicy() {
        return MonetaryPolicyAnalysis.builder()
                .policyStance("稳健中性")
                .interestRateLevel(3.85)
                .interestRateTrend("稳定")
                .liquidityCondition("合理充裕")
                .currencySupplyGrowth(8.2)
                .policyExpectation("维持稳健")
                .build();
    }
    
    private FiscalPolicyAnalysis analyzeFiscalPolicy() {
        return FiscalPolicyAnalysis.builder()
                .policyDirection("积极财政")
                .fiscalDeficitRatio(3.2)
                .governmentDebtRatio(50.8)
                .taxPolicyDirection("结构性减税")
                .expenditureStructure("基建投资增加")
                .policyEffectiveness("有效")
                .build();
    }
    
    private InternationalEnvironmentAnalysis analyzeInternationalEnvironment() {
        return InternationalEnvironmentAnalysis.builder()
                .globalEconomicGrowth(3.1)
                .globalEconomicTrend("温和复苏")
                .tradeEnvironment("复杂多变")
                .geopoliticalRisk("中等")
                .exchangeRateStability("相对稳定")
                .commodityPriceTrend("震荡")
                .build();
    }
    
    private List<MacroRisk> assessMacroRisks(EconomicIndicators indicators, MonetaryPolicyAnalysis monetaryPolicy,
                                           FiscalPolicyAnalysis fiscalPolicy, InternationalEnvironmentAnalysis internationalEnv) {
        List<MacroRisk> risks = new ArrayList<>();
        
        // 通胀风险
        if (indicators.getInflationRate() > 3.0) {
            risks.add(MacroRisk.builder()
                    .riskType("通胀风险")
                    .riskLevel("中等")
                    .riskDescription("通胀率超过3%，存在通胀压力")
                    .impactLevel("中等")
                    .build());
        }
        
        // 流动性风险
        if ("紧缩".equals(monetaryPolicy.getPolicyStance())) {
            risks.add(MacroRisk.builder()
                    .riskType("流动性风险")
                    .riskLevel("高")
                    .riskDescription("货币政策紧缩，流动性收紧")
                    .impactLevel("高")
                    .build());
        }
        
        // 地缘政治风险
        if ("高".equals(internationalEnv.getGeopoliticalRisk())) {
            risks.add(MacroRisk.builder()
                    .riskType("地缘政治风险")
                    .riskLevel("高")
                    .riskDescription("地缘政治紧张，影响市场稳定")
                    .impactLevel("高")
                    .build());
        }
        
        return risks;
    }
    
    private double calculateMacroImpactScore(EconomicIndicators indicators, MonetaryPolicyAnalysis monetaryPolicy,
                                           FiscalPolicyAnalysis fiscalPolicy, InternationalEnvironmentAnalysis internationalEnv) {
        double score = 5.0; // 基础分
        
        // GDP增长贡献
        if (indicators.getGdpGrowthRate() > 5.0) score += 1.5;
        else if (indicators.getGdpGrowthRate() > 3.0) score += 1.0;
        else if (indicators.getGdpGrowthRate() < 2.0) score -= 1.0;
        
        // PMI贡献
        if (indicators.getPmiIndex() > 52.0) score += 1.0;
        else if (indicators.getPmiIndex() < 48.0) score -= 1.0;
        
        // 货币政策贡献
        if ("宽松".equals(monetaryPolicy.getPolicyStance())) score += 1.0;
        else if ("紧缩".equals(monetaryPolicy.getPolicyStance())) score -= 1.0;
        
        // 财政政策贡献
        if ("积极财政".equals(fiscalPolicy.getPolicyDirection())) score += 0.5;
        
        // 国际环境贡献
        if ("高".equals(internationalEnv.getGeopoliticalRisk())) score -= 1.0;
        else if ("低".equals(internationalEnv.getGeopoliticalRisk())) score += 0.5;
        
        return Math.max(0, Math.min(10, score));
    }
    
    // 简化的行业分析方法
    
    private IndustryLifeCycle analyzeIndustryLifeCycle(String industry) {
        // 简化实现
        return IndustryLifeCycle.builder()
                .currentStage("成长期")
                .stageCharacteristics("快速发展，竞争加剧")
                .expectedDuration("3-5年")
                .nextStagePredict("成熟期")
                .build();
    }
    
    private CompetitiveStructure analyzeCompetitiveStructure(String industry, String stockCode) {
        return CompetitiveStructure.builder()
                .marketStructure("寡头竞争")
                .competitionIntensity("激烈")
                .marketConcentration(0.65)
                .entryBarriers("高")
                .competitiveAdvantage("技术领先")
                .marketPosition("前三")
                .build();
    }
    
    private IndustryPolicyEnvironment analyzeIndustryPolicyEnvironment(String industry) {
        return IndustryPolicyEnvironment.builder()
                .supportLevel("强力支持")
                .policyDirection("鼓励发展")
                .regulatoryEnvironment("适度监管")
                .subsidySupport("有")
                .taxIncentives("有")
                .futurePolicy("持续支持")
                .build();
    }
    
    private TechnologyTrend analyzeTechnologyTrend(String industry) {
        return TechnologyTrend.builder()
                .technologyMaturity("快速发展")
                .innovationSpeed("高")
                .disruptiveRisk("中等")
                .technologyBarriers("高")
                .rdIntensity("高")
                .futureDirection("智能化")
                .build();
    }
    
    private IndustryValuation analyzeIndustryValuation(String industry) {
        return IndustryValuation.builder()
                .averagePE(25.6)
                .averagePB(2.8)
                .valuationLevel("合理")
                .historicalPercentile(45.2)
                .valuationTrend("稳定")
                .valuationRisk("低")
                .build();
    }
    
    private IndustryCapitalFlow analyzeIndustryCapitalFlow(String industry) {
        return IndustryCapitalFlow.builder()
                .netInflow(1250000000L) // 12.5亿
                .flowDirection("净流入")
                .flowIntensity("强")
                .institutionalFlow("净流入")
                .retailFlow("净流出")
                .foreignFlow("净流入")
                .build();
    }
    
    private double calculateIndustryProspectScore(IndustryLifeCycle lifeCycle, CompetitiveStructure competitiveStructure,
                                                 IndustryPolicyEnvironment policyEnvironment, TechnologyTrend technologyTrend,
                                                 IndustryValuation valuation) {
        double score = 5.0;
        
        // 生命周期贡献
        if ("成长期".equals(lifeCycle.getCurrentStage())) score += 2.0;
        else if ("成熟期".equals(lifeCycle.getCurrentStage())) score += 1.0;
        else if ("衰退期".equals(lifeCycle.getCurrentStage())) score -= 2.0;
        
        // 政策环境贡献
        if ("强力支持".equals(policyEnvironment.getSupportLevel())) score += 1.5;
        else if ("一般支持".equals(policyEnvironment.getSupportLevel())) score += 0.5;
        
        // 技术趋势贡献
        if ("高".equals(technologyTrend.getInnovationSpeed())) score += 1.0;
        
        // 估值水平贡献
        if ("低估".equals(valuation.getValuationLevel())) score += 1.0;
        else if ("高估".equals(valuation.getValuationLevel())) score -= 1.0;
        
        return Math.max(0, Math.min(10, score));
    }
    
    // 简化的情绪分析方法
    
    private NewsSentimentMetrics analyzeNewsSentiment(List<NewsItem> stockNews, List<NewsItem> marketNews) {
        // 简化实现
        double positiveRatio = 0.45;
        double negativeRatio = 0.25;
        double neutralRatio = 0.30;
        
        String sentimentLabel;
        if (positiveRatio > 0.5) sentimentLabel = "积极";
        else if (negativeRatio > 0.4) sentimentLabel = "消极";
        else sentimentLabel = "中性";
        
        return NewsSentimentMetrics.builder()
                .positiveRatio(positiveRatio)
                .negativeRatio(negativeRatio)
                .neutralRatio(neutralRatio)
                .sentimentScore(6.2)
                .sentimentLabel(sentimentLabel)
                .newsVolume(stockNews.size() + marketNews.size())
                .build();
    }
    
    private TechnicalSentimentMetrics analyzeTechnicalSentiment(String stockCode) {
        return TechnicalSentimentMetrics.builder()
                .trendSentiment("偏多")
                .momentumSentiment("强势")
                .volumeSentiment("活跃")
                .volatilitySentiment("适中")
                .technicalScore(7.1)
                .build();
    }
    
    private InvestorSentimentMetrics analyzeInvestorSentiment(String stockCode) {
        return InvestorSentimentMetrics.builder()
                .sentimentLevel("乐观")
                .riskAppetite("中等")
                .tradingActivity("活跃")
                .positionLevel("适中")
                .sentimentScore(6.8)
                .build();
    }
    
    private FearGreedIndex calculateFearGreedIndex(NewsSentimentMetrics newsSentiment, 
                                                  TechnicalSentimentMetrics technicalSentiment,
                                                  InvestorSentimentMetrics investorSentiment) {
        double indexValue = (newsSentiment.getSentimentScore() + technicalSentiment.getTechnicalScore() + 
                           investorSentiment.getSentimentScore()) / 3 * 10;
        
        String indexLabel;
        if (indexValue > 75) indexLabel = "极度贪婪";
        else if (indexValue > 55) indexLabel = "贪婪";
        else if (indexValue > 45) indexLabel = "中性";
        else if (indexValue > 25) indexLabel = "恐慌";
        else indexLabel = "极度恐慌";
        
        return FearGreedIndex.builder()
                .indexValue(indexValue)
                .indexLabel(indexLabel)
                .historicalPercentile(52.3)
                .build();
    }
    
    private SentimentTrend analyzeSentimentTrend(NewsSentimentMetrics newsSentiment,
                                               TechnicalSentimentMetrics technicalSentiment,
                                               InvestorSentimentMetrics investorSentiment) {
        return SentimentTrend.builder()
                .shortTermTrend("上升")
                .mediumTermTrend("稳定")
                .longTermTrend("改善")
                .trendStrength("中等")
                .build();
    }
    
    private double calculateOverallSentimentScore(NewsSentimentMetrics newsSentiment,
                                                 TechnicalSentimentMetrics technicalSentiment,
                                                 InvestorSentimentMetrics investorSentiment,
                                                 FearGreedIndex fearGreedIndex) {
        return (newsSentiment.getSentimentScore() + technicalSentiment.getTechnicalScore() + 
               investorSentiment.getSentimentScore()) / 3;
    }
    
    // 工具方法
    
    private double calculateMarketScore(MacroEconomicAnalysis macroAnalysis, IndustryAnalysis industryAnalysis,
                                      MarketSentimentAnalysis sentimentAnalysis, PolicyImpactAnalysis policyAnalysis,
                                      CapitalFlowAnalysis capitalFlowAnalysis, MarketCycleAnalysis cycleAnalysis) {
        double score = 0;
        
        // 各项权重
        score += macroAnalysis.getMacroImpactScore() * 0.25;      // 宏观 25%
        score += industryAnalysis.getProspectScore() * 0.25;       // 行业 25%
        score += sentimentAnalysis.getOverallSentimentScore() * 0.2; // 情绪 20%
        score += policyAnalysis.getPolicyImpactScore() * 0.15;     // 政策 15%
        score += capitalFlowAnalysis.getCapitalFlowScore() * 0.1;  // 资金 10%
        score += cycleAnalysis.getCycleScore() * 0.05;             // 周期 5%
        
        return Math.max(0, Math.min(10, score));
    }
    
    private String generateRecommendation(double marketScore, MacroEconomicAnalysis macroAnalysis,
                                        IndustryAnalysis industryAnalysis, MarketSentimentAnalysis sentimentAnalysis) {
        if (marketScore >= 8.0 && sentimentAnalysis.getOverallSentimentScore() >= 7.0) {
            return "强烈推荐买入";
        } else if (marketScore >= 7.0) {
            return "推荐买入";
        } else if (marketScore >= 6.0) {
            return "谨慎买入";
        } else if (marketScore >= 4.0) {
            return "持有观望";
        } else if (marketScore >= 3.0) {
            return "谨慎减仓";
        } else {
            return "建议卖出";
        }
    }
    
    private double calculateConfidence(double marketScore, MarketSentimentAnalysis sentimentAnalysis, IndustryAnalysis industryAnalysis) {
        double confidence = 0.5; // 基础置信度
        
        // 市场评分贡献
        if (marketScore >= 8.0) confidence += 0.3;
        else if (marketScore >= 7.0) confidence += 0.2;
        else if (marketScore >= 6.0) confidence += 0.1;
        else if (marketScore < 3.0) confidence -= 0.2;
        
        // 情绪一致性贡献
        if (sentimentAnalysis.getOverallSentimentScore() >= 7.0 || sentimentAnalysis.getOverallSentimentScore() <= 3.0) {
            confidence += 0.1; // 极端情绪时置信度高
        }
        
        // 行业前景贡献
        if (industryAnalysis.getProspectScore() >= 8.0) confidence += 0.1;
        
        return Math.max(0.1, Math.min(1.0, confidence));
    }
    
    // 默认值和错误处理方法
    
    private MarketAnalysisResult createErrorResult(String errorMessage) {
        return MarketAnalysisResult.builder()
                .stockCode("")
                .agentId("market_analyst")
                .agentName("市场分析师")
                .analysis("分析失败: " + errorMessage)
                .macroAnalysis(null)
                .industryAnalysis(null)
                .sentimentAnalysis(null)
                .policyAnalysis(null)
                .capitalFlowAnalysis(null)
                .cycleAnalysis(null)
                .marketScore(0.0)
                .recommendation("无法提供建议")
                .confidence(0.0)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    private FundamentalAnalyst.DebateArgument createErrorDebateArgument(String errorMessage) {
        return FundamentalAnalyst.DebateArgument.builder()
                .agentId("market_analyst")
                .agentName("市场分析师")
                .round(0)
                .argument("辩论失败: " + errorMessage)
                .confidence(0.0)
                .evidenceType("ERROR")
                .supportingData(new HashMap<>())
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    private MarketSentimentAnalysis createDefaultSentimentAnalysis() {
        return MarketSentimentAnalysis.builder()
                .newsSentiment(NewsSentimentMetrics.builder()
                        .sentimentScore(5.0)
                        .sentimentLabel("中性")
                        .newsVolume(0)
                        .build())
                .technicalSentiment(TechnicalSentimentMetrics.builder()
                        .trendSentiment("中性")
                        .technicalScore(5.0)
                        .build())
                .investorSentiment(InvestorSentimentMetrics.builder()
                        .sentimentLevel("中性")
                        .sentimentScore(5.0)
                        .build())
                .fearGreedIndex(FearGreedIndex.builder()
                        .indexValue(50.0)
                        .indexLabel("中性")
                        .build())
                .overallSentimentScore(5.0)
                .build();
    }
    
    private PolicyImpactAnalysis createDefaultPolicyAnalysis() {
        return PolicyImpactAnalysis.builder()
                .policyTypes(new ArrayList<>())
                .policyImpacts(new ArrayList<>())
                .policyRisks(new ArrayList<>())
                .policyOpportunities(new ArrayList<>())
                .policyImpactScore(5.0)
                .build();
    }
    
    private CapitalFlowAnalysis createDefaultCapitalFlowAnalysis() {
        return CapitalFlowAnalysis.builder()
                .mainCapitalFlow(MainCapitalFlow.builder()
                        .flowDirection("平衡")
                        .build())
                .retailCapitalFlow(RetailCapitalFlow.builder()
                        .flowDirection("平衡")
                        .build())
                .institutionalFlow(InstitutionalCapitalFlow.builder()
                        .flowTrend("平衡")
                        .build())
                .foreignFlow(ForeignCapitalFlow.builder()
                        .flowDirection("平衡")
                        .build())
                .capitalFlowScore(5.0)
                .build();
    }
    
    private MarketCycleAnalysis createDefaultCycleAnalysis() {
        return MarketCycleAnalysis.builder()
                .bullBearCycle(BullBearCycle.builder()
                        .currentPhase("震荡")
                        .build())
                .industryCycle(IndustryCycle.builder()
                        .cycleStage("稳定")
                        .build())
                .economicCyclePosition(EconomicCyclePosition.builder()
                        .currentPosition("中期")
                        .build())
                .cycleScore(5.0)
                .build();
    }
    
    // 简化实现的其他分析方法
    
    private List<PolicyType> analyzePolicyTypes(List<NewsItem> policyNews, String industry) {
        List<PolicyType> types = new ArrayList<>();
        types.add(PolicyType.builder()
                .policyCategory("产业政策")
                .policyName("行业支持政策")
                .policyDescription("政府支持行业发展")
                .effectiveDate("2024-01-01")
                .build());
        return types;
    }
    
    private List<PolicyImpact> assessPolicyImpacts(List<PolicyType> policyTypes, String stockCode, String industry) {
        List<PolicyImpact> impacts = new ArrayList<>();
        impacts.add(PolicyImpact.builder()
                .impactType("正面影响")
                .impactDescription("政策支持行业发展")
                .impactLevel("中等")
                .timeFrame("中期")
                .build());
        return impacts;
    }
    
    private List<PolicyRisk> assessPolicyRisks(List<PolicyType> policyTypes, String industry) {
        List<PolicyRisk> risks = new ArrayList<>();
        risks.add(PolicyRisk.builder()
                .riskType("监管风险")
                .riskDescription("可能面临更严格监管")
                .riskLevel("低")
                .probability("低")
                .build());
        return risks;
    }
    
    private List<PolicyOpportunity> identifyPolicyOpportunities(List<PolicyType> policyTypes, String industry) {
        List<PolicyOpportunity> opportunities = new ArrayList<>();
        opportunities.add(PolicyOpportunity.builder()
                .opportunityType("政策红利")
                .opportunityDescription("享受政策支持")
                .opportunityLevel("中等")
                .timeWindow("1-2年")
                .build());
        return opportunities;
    }
    
    private double calculatePolicyImpactScore(List<PolicyImpact> impacts, List<PolicyRisk> risks, List<PolicyOpportunity> opportunities) {
        double score = 5.0;
        
        // 正面影响加分
        for (PolicyImpact impact : impacts) {
            if ("正面影响".equals(impact.getImpactType())) {
                if ("高".equals(impact.getImpactLevel())) score += 1.5;
                else if ("中等".equals(impact.getImpactLevel())) score += 1.0;
                else score += 0.5;
            }
        }
        
        // 风险扣分
        for (PolicyRisk risk : risks) {
            if ("高".equals(risk.getRiskLevel())) score -= 1.0;
            else if ("中等".equals(risk.getRiskLevel())) score -= 0.5;
        }
        
        // 机会加分
        for (PolicyOpportunity opportunity : opportunities) {
            if ("高".equals(opportunity.getOpportunityLevel())) score += 1.0;
            else if ("中等".equals(opportunity.getOpportunityLevel())) score += 0.5;
        }
        
        return Math.max(0, Math.min(10, score));
    }
    
    private MainCapitalFlow analyzeMainCapitalFlow(List<KLineData> klineData) {
        // 简化实现
        return MainCapitalFlow.builder()
                .netInflow(50000000L) // 5000万
                .flowDirection("净流入")
                .flowIntensity("中等")
                .flowTrend("上升")
                .build();
    }
    
    private RetailCapitalFlow analyzeRetailCapitalFlow(List<KLineData> klineData) {
        return RetailCapitalFlow.builder()
                .netInflow(-20000000L) // -2000万
                .flowDirection("净流出")
                .flowIntensity("弱")
                .flowTrend("下降")
                .build();
    }
    
    private InstitutionalCapitalFlow analyzeInstitutionalCapitalFlow(String stockCode) {
        return InstitutionalCapitalFlow.builder()
                .netInflow(30000000L) // 3000万
                .flowTrend("净流入")
                .institutionType("基金")
                .flowIntensity("强")
                .build();
    }
    
    private ForeignCapitalFlow analyzeForeignCapitalFlow(String stockCode) {
        return ForeignCapitalFlow.builder()
                .netInflow(15000000L) // 1500万
                .flowDirection("净流入")
                .flowIntensity("中等")
                .flowTrend("稳定")
                .build();
    }
    
    private CapitalFlowTrend analyzeCapitalFlowTrend(MainCapitalFlow mainFlow, RetailCapitalFlow retailFlow,
                                                   InstitutionalCapitalFlow institutionalFlow, ForeignCapitalFlow foreignFlow) {
        return CapitalFlowTrend.builder()
                .overallTrend("资金净流入")
                .trendStrength("中等")
                .trendSustainability("可持续")
                .keyDrivers("机构资金流入")
                .build();
    }
    
    private double calculateCapitalFlowScore(MainCapitalFlow mainFlow, InstitutionalCapitalFlow institutionalFlow, ForeignCapitalFlow foreignFlow) {
        double score = 5.0;
        
        // 主力资金贡献
        if ("净流入".equals(mainFlow.getFlowDirection())) {
            if ("强".equals(mainFlow.getFlowIntensity())) score += 2.0;
            else if ("中等".equals(mainFlow.getFlowIntensity())) score += 1.0;
            else score += 0.5;
        } else if ("净流出".equals(mainFlow.getFlowDirection())) {
            if ("强".equals(mainFlow.getFlowIntensity())) score -= 2.0;
            else if ("中等".equals(mainFlow.getFlowIntensity())) score -= 1.0;
            else score -= 0.5;
        }
        
        // 机构资金贡献
        if ("净流入".equals(institutionalFlow.getFlowTrend())) score += 1.5;
        else if ("净流出".equals(institutionalFlow.getFlowTrend())) score -= 1.5;
        
        // 外资贡献
        if ("净流入".equals(foreignFlow.getFlowDirection())) score += 1.0;
        else if ("净流出".equals(foreignFlow.getFlowDirection())) score -= 1.0;
        
        return Math.max(0, Math.min(10, score));
    }
    
    private BullBearCycle identifyBullBearCycle(List<KLineData> longTermData) {
        // 简化实现
        return BullBearCycle.builder()
                .currentPhase("牛市中期")
                .phaseStartDate("2023-01-01")
                .phaseDuration(365)
                .expectedDuration("1-2年")
                .cycleStrength("中等")
                .build();
    }
    
    private IndustryCycle analyzeIndustryCycle(String stockCode) {
        return IndustryCycle.builder()
                .cycleStage("上升期")
                .cycleLength("3-5年")
                .currentPosition("中期")
                .nextStagePredict("成熟期")
                .cyclicalFactors("技术创新驱动")
                .build();
    }
    
    private EconomicCyclePosition identifyEconomicCyclePosition() {
        return EconomicCyclePosition.builder()
                .currentPosition("复苏期")
                .cycleStage("中期")
                .expectedDuration("12-18个月")
                .nextPhase("繁荣期")
                .economicIndicators("PMI上升，就业改善")
                .build();
    }
    
    private CyclicalCharacteristics analyzeCyclicalCharacteristics(List<KLineData> longTermData) {
        return CyclicalCharacteristics.builder()
                .cyclicalSensitivity("高")
                .seasonalPattern("四季度强势")
                .cyclicalVolatility("中等")
                .correlationWithMarket(0.75)
                .build();
    }
    
    private CycleForecast forecastCycle(BullBearCycle bullBearCycle, IndustryCycle industryCycle, EconomicCyclePosition economicCyclePosition) {
        return CycleForecast.builder()
                .shortTermOutlook("积极")
                .mediumTermOutlook("谨慎乐观")
                .longTermOutlook("稳定")
                .keyRisks("政策变化，外部冲击")
                .keyOpportunities("技术突破，政策支持")
                .build();
    }
    
    private double calculateCycleScore(BullBearCycle bullBearCycle, IndustryCycle industryCycle, 
                                     EconomicCyclePosition economicCyclePosition, CyclicalCharacteristics characteristics) {
        double score = 5.0;
        
        // 牛熊周期贡献
        if ("牛市".contains(bullBearCycle.getCurrentPhase())) score += 2.0;
        else if ("熊市".contains(bullBearCycle.getCurrentPhase())) score -= 2.0;
        
        // 行业周期贡献
        if ("上升期".equals(industryCycle.getCycleStage())) score += 1.5;
        else if ("下降期".equals(industryCycle.getCycleStage())) score -= 1.5;
        
        // 经济周期贡献
        if ("复苏期".equals(economicCyclePosition.getCurrentPosition()) || 
            "繁荣期".equals(economicCyclePosition.getCurrentPosition())) {
            score += 1.0;
        } else if ("衰退期".equals(economicCyclePosition.getCurrentPosition())) {
            score -= 1.0;
        }
        
        return Math.max(0, Math.min(10, score));
    }
    
    // ==================== 数据模型定义 ====================
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MarketAnalysisResult {
        private String stockCode;
        private String agentId;
        private String agentName;
        private String analysis;
        private MacroEconomicAnalysis macroAnalysis;
        private IndustryAnalysis industryAnalysis;
        private MarketSentimentAnalysis sentimentAnalysis;
        private PolicyImpactAnalysis policyAnalysis;
        private CapitalFlowAnalysis capitalFlowAnalysis;
        private MarketCycleAnalysis cycleAnalysis;
        private double marketScore;
        private String recommendation;
        private double confidence;
        private long timestamp;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MacroEconomicAnalysis {
        private EconomicIndicators indicators;
        private MonetaryPolicyAnalysis monetaryPolicy;
        private FiscalPolicyAnalysis fiscalPolicy;
        private InternationalEnvironmentAnalysis internationalEnvironment;
        private List<MacroRisk> macroRisks;
        private double macroImpactScore;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EconomicIndicators {
        private double gdpGrowthRate;
        private String gdpGrowthTrend;
        private double inflationRate;
        private String inflationTrend;
        private double unemploymentRate;
        private String unemploymentTrend;
        private double pmiIndex;
        private String pmiTrend;
        private double consumerConfidenceIndex;
        private String consumerConfidenceTrend;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MonetaryPolicyAnalysis {
        private String policyStance;
        private double interestRateLevel;
        private String interestRateTrend;
        private String liquidityCondition;
        private double currencySupplyGrowth;
        private String policyExpectation;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FiscalPolicyAnalysis {
        private String policyDirection;
        private double fiscalDeficitRatio;
        private double governmentDebtRatio;
        private String taxPolicyDirection;
        private String expenditureStructure;
        private String policyEffectiveness;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class InternationalEnvironmentAnalysis {
        private double globalEconomicGrowth;
        private String globalEconomicTrend;
        private String tradeEnvironment;
        private String geopoliticalRisk;
        private String exchangeRateStability;
        private String commodityPriceTrend;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MacroRisk {
        private String riskType;
        private String riskLevel;
        private String riskDescription;
        private String impactLevel;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IndustryAnalysis {
        private String industry;
        private IndustryLifeCycle lifeCycle;
        private CompetitiveStructure competitiveStructure;
        private IndustryPolicyEnvironment policyEnvironment;
        private TechnologyTrend technologyTrend;
        private IndustryValuation valuation;
        private IndustryCapitalFlow capitalFlow;
        private double prospectScore;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IndustryLifeCycle {
        private String currentStage;
        private String stageCharacteristics;
        private String expectedDuration;
        private String nextStagePredict;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CompetitiveStructure {
        private String marketStructure;
        private String competitionIntensity;
        private double marketConcentration;
        private String entryBarriers;
        private String competitiveAdvantage;
        private String marketPosition;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IndustryPolicyEnvironment {
        private String supportLevel;
        private String policyDirection;
        private String regulatoryEnvironment;
        private String subsidySupport;
        private String taxIncentives;
        private String futurePolicy;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TechnologyTrend {
        private String technologyMaturity;
        private String innovationSpeed;
        private String disruptiveRisk;
        private String technologyBarriers;
        private String rdIntensity;
        private String futureDirection;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IndustryValuation {
        private double averagePE;
        private double averagePB;
        private String valuationLevel;
        private double historicalPercentile;
        private String valuationTrend;
        private String valuationRisk;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IndustryCapitalFlow {
        private long netInflow;
        private String flowDirection;
        private String flowIntensity;
        private String institutionalFlow;
        private String retailFlow;
        private String foreignFlow;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MarketSentimentAnalysis {
        private NewsSentimentMetrics newsSentiment;
        private TechnicalSentimentMetrics technicalSentiment;
        private InvestorSentimentMetrics investorSentiment;
        private FearGreedIndex fearGreedIndex;
        private SentimentTrend sentimentTrend;
        private double overallSentimentScore;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NewsSentimentMetrics {
        private double positiveRatio;
        private double negativeRatio;
        private double neutralRatio;
        private double sentimentScore;
        private String sentimentLabel;
        private int newsVolume;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TechnicalSentimentMetrics {
        private String trendSentiment;
        private String momentumSentiment;
        private String volumeSentiment;
        private String volatilitySentiment;
        private double technicalScore;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class InvestorSentimentMetrics {
        private String sentimentLevel;
        private String riskAppetite;
        private String tradingActivity;
        private String positionLevel;
        private double sentimentScore;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FearGreedIndex {
        private double indexValue;
        private String indexLabel;
        private double historicalPercentile;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SentimentTrend {
        private String shortTermTrend;
        private String mediumTermTrend;
        private String longTermTrend;
        private String trendStrength;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PolicyImpactAnalysis {
        private List<PolicyType> policyTypes;
        private List<PolicyImpact> policyImpacts;
        private List<PolicyRisk> policyRisks;
        private List<PolicyOpportunity> policyOpportunities;
        private double policyImpactScore;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PolicyType {
        private String policyCategory;
        private String policyName;
        private String policyDescription;
        private String effectiveDate;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PolicyImpact {
        private String impactType;
        private String impactDescription;
        private String impactLevel;
        private String timeFrame;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PolicyRisk {
        private String riskType;
        private String riskDescription;
        private String riskLevel;
        private String probability;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PolicyOpportunity {
        private String opportunityType;
        private String opportunityDescription;
        private String opportunityLevel;
        private String timeWindow;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CapitalFlowAnalysis {
        private MainCapitalFlow mainCapitalFlow;
        private RetailCapitalFlow retailCapitalFlow;
        private InstitutionalCapitalFlow institutionalFlow;
        private ForeignCapitalFlow foreignFlow;
        private CapitalFlowTrend flowTrend;
        private double capitalFlowScore;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MainCapitalFlow {
        private long netInflow;
        private String flowDirection;
        private String flowIntensity;
        private String flowTrend;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RetailCapitalFlow {
        private long netInflow;
        private String flowDirection;
        private String flowIntensity;
        private String flowTrend;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class InstitutionalCapitalFlow {
        private long netInflow;
        private String flowTrend;
        private String institutionType;
        private String flowIntensity;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ForeignCapitalFlow {
        private long netInflow;
        private String flowDirection;
        private String flowIntensity;
        private String flowTrend;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CapitalFlowTrend {
        private String overallTrend;
        private String trendStrength;
        private String trendSustainability;
        private String keyDrivers;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MarketCycleAnalysis {
        private BullBearCycle bullBearCycle;
        private IndustryCycle industryCycle;
        private EconomicCyclePosition economicCyclePosition;
        private CyclicalCharacteristics cyclicalCharacteristics;
        private CycleForecast cycleForecast;
        private double cycleScore;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BullBearCycle {
        private String currentPhase;
        private String phaseStartDate;
        private int phaseDuration;
        private String expectedDuration;
        private String cycleStrength;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IndustryCycle {
        private String cycleStage;
        private String cycleLength;
        private String currentPosition;
        private String nextStagePredict;
        private String cyclicalFactors;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EconomicCyclePosition {
        private String currentPosition;
        private String cycleStage;
        private String expectedDuration;
        private String nextPhase;
        private String economicIndicators;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CyclicalCharacteristics {
        private String cyclicalSensitivity;
        private String seasonalPattern;
        private String cyclicalVolatility;
        private double correlationWithMarket;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CycleForecast {
        private String shortTermOutlook;
        private String mediumTermOutlook;
        private String longTermOutlook;
        private String keyRisks;
        private String keyOpportunities;
    }
}