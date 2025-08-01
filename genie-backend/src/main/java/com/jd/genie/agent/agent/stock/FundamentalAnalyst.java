package com.jd.genie.agent.agent.stock;

import com.jd.genie.agent.agent.BaseAgent;
import com.jd.genie.agent.dto.Message;
import com.jd.genie.agent.enums.AgentType;
import com.jd.genie.service.StockDataMigrationService;
import com.jd.genie.service.StockDataMigrationService.FinancialData;
import com.jd.genie.service.StockDataMigrationService.StockQuoteData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 基本面分析师智能体
 * 基于TradingAgents框架的专业分析师角色
 * 
 * 职责：
 * - 财务数据分析
 * - 估值模型计算
 * - 行业对比分析
 * - 基本面评级
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component("fundamental_analyst")
public class FundamentalAnalyst extends BaseAgent {
    
    @Autowired
    private StockDataMigrationService stockDataService;
    
    public FundamentalAnalyst() {
        super();
        this.agentType = AgentType.ANALYSIS;
        this.agentName = "基本面分析师";
        this.description = "专业的基本面分析师，擅长财务数据分析、估值模型和行业对比";
    }
    
    /**
     * 执行基本面分析
     * 
     * @param stockCode 股票代码
     * @param context 分析上下文
     * @return 分析结果
     */
    public CompletableFuture<FundamentalAnalysisResult> analyze(String stockCode, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("基本面分析师开始分析股票: {}", stockCode);
                
                // 1. 获取财务数据
                FinancialData financialData = stockDataService.getFinancialData(stockCode).get();
                if (financialData == null) {
                    return createErrorResult("无法获取财务数据");
                }
                
                // 2. 获取当前股价
                List<StockQuoteData> quoteData = stockDataService.getRealTimeStockData(List.of(stockCode)).get();
                if (quoteData.isEmpty()) {
                    return createErrorResult("无法获取股价数据");
                }
                
                StockQuoteData currentQuote = quoteData.get(0);
                
                // 3. 计算财务指标
                FinancialRatios ratios = calculateFinancialRatios(financialData);
                
                // 4. 估值分析
                ValuationAnalysis valuation = performValuationAnalysis(financialData, currentQuote, ratios);
                
                // 5. 行业对比（简化实现）
                IndustryComparison industryComparison = performIndustryComparison(stockCode, ratios);
                
                // 6. 生成LLM分析
                String llmAnalysis = generateLLMAnalysis(stockCode, financialData, ratios, valuation, industryComparison);
                
                // 7. 计算综合评分
                double fundamentalScore = calculateFundamentalScore(ratios, valuation, industryComparison);
                
                // 8. 生成投资建议
                String recommendation = generateRecommendation(fundamentalScore, valuation);
                
                return FundamentalAnalysisResult.builder()
                        .stockCode(stockCode)
                        .agentId("fundamental_analyst")
                        .agentName("基本面分析师")
                        .analysis(llmAnalysis)
                        .financialRatios(ratios)
                        .valuation(valuation)
                        .industryComparison(industryComparison)
                        .fundamentalScore(fundamentalScore)
                        .recommendation(recommendation)
                        .confidence(calculateConfidence(ratios, valuation))
                        .timestamp(System.currentTimeMillis())
                        .build();
                
            } catch (Exception e) {
                log.error("基本面分析失败: {}", stockCode, e);
                return createErrorResult("分析过程中发生错误: " + e.getMessage());
            }
        });
    }
    
    /**
     * 参与结构化辩论
     * 
     * @param context 辩论上下文
     * @param previousArguments 之前的辩论观点
     * @return 辩论论据
     */
    public DebateArgument debate(DebateContext context, List<DebateArgument> previousArguments) {
        try {
            String stockCode = context.getStockCode();
            int currentRound = context.getCurrentRound();
            
            // 获取基本面分析结果
            FundamentalAnalysisResult analysisResult = (FundamentalAnalysisResult) context.getAgentResult("fundamental_analyst");
            
            String prompt = buildDebatePrompt(currentRound, analysisResult, previousArguments);
            
            // 调用LLM生成辩论论据
            String argument = callLLM(prompt);
            
            return DebateArgument.builder()
                    .agentId("fundamental_analyst")
                    .agentName("基本面分析师")
                    .round(currentRound)
                    .argument(argument)
                    .confidence(analysisResult.getConfidence())
                    .evidenceType("FUNDAMENTAL")
                    .supportingData(Map.of(
                        "financialRatios", analysisResult.getFinancialRatios(),
                        "valuation", analysisResult.getValuation(),
                        "fundamentalScore", analysisResult.getFundamentalScore()
                    ))
                    .timestamp(System.currentTimeMillis())
                    .build();
                    
        } catch (Exception e) {
            log.error("基本面分析师辩论失败", e);
            return createErrorDebateArgument("辩论过程中发生错误");
        }
    }
    
    /**
     * 计算财务指标
     */
    private FinancialRatios calculateFinancialRatios(FinancialData data) {
        return FinancialRatios.builder()
                .eps(data.getEps())
                .roe(data.getRoe())
                .roa(data.getRoa())
                .debtToAssetRatio(data.getDebtToAssetRatio())
                .currentRatio(data.getCurrentRatio())
                .quickRatio(data.getQuickRatio())
                .grossProfitMargin(data.getGrossProfitMargin())
                .netProfitMargin(data.getNetProfitMargin())
                .assetTurnover(calculateAssetTurnover(data))
                .equityMultiplier(calculateEquityMultiplier(data))
                .priceToBook(0.0) // 需要结合股价计算
                .priceToEarnings(0.0) // 需要结合股价计算
                .build();
    }
    
    /**
     * 估值分析
     */
    private ValuationAnalysis performValuationAnalysis(FinancialData financialData, StockQuoteData quoteData, FinancialRatios ratios) {
        double currentPrice = quoteData.getCurrentPrice();
        
        // 计算PE和PB
        double pe = financialData.getEps() != 0 ? currentPrice / financialData.getEps() : 0;
        double pb = financialData.getShareholderEquity() != 0 ? 
                (currentPrice * 1000000) / financialData.getShareholderEquity() : 0; // 简化计算
        
        // DCF估值（简化版）
        double dcfValue = calculateDCFValue(financialData);
        
        // 相对估值
        double relativeValue = calculateRelativeValue(pe, pb);
        
        // 估值评级
        String valuationRating = determineValuationRating(currentPrice, dcfValue, pe, pb);
        
        return ValuationAnalysis.builder()
                .currentPrice(currentPrice)
                .peRatio(pe)
                .pbRatio(pb)
                .dcfValue(dcfValue)
                .relativeValue(relativeValue)
                .valuationRating(valuationRating)
                .upside(calculateUpside(currentPrice, dcfValue))
                .riskLevel(assessRiskLevel(ratios))
                .build();
    }
    
    /**
     * 行业对比分析
     */
    private IndustryComparison performIndustryComparison(String stockCode, FinancialRatios ratios) {
        // 这里简化实现，实际应该从数据库获取行业平均数据
        return IndustryComparison.builder()
                .industryName("示例行业")
                .industryAvgPE(15.0)
                .industryAvgPB(2.0)
                .industryAvgROE(12.0)
                .industryAvgROA(8.0)
                .industryRanking("前25%") // 简化
                .competitiveAdvantage("财务指标优于行业平均")
                .build();
    }
    
    /**
     * 生成LLM分析
     */
    private String generateLLMAnalysis(String stockCode, FinancialData financialData, 
                                     FinancialRatios ratios, ValuationAnalysis valuation, 
                                     IndustryComparison industryComparison) {
        
        String prompt = String.format("""
            作为专业的基本面分析师，请基于以下财务数据对股票 %s 进行深度分析：
            
            财务数据：
            - 总营收：%.2f 亿元
            - 净利润：%.2f 亿元
            - 总资产：%.2f 亿元
            - 股东权益：%.2f 亿元
            
            关键财务指标：
            - 每股收益(EPS)：%.2f 元
            - 净资产收益率(ROE)：%.2f%%
            - 总资产收益率(ROA)：%.2f%%
            - 资产负债率：%.2f%%
            - 流动比率：%.2f
            - 毛利率：%.2f%%
            - 净利率：%.2f%%
            
            估值分析：
            - 当前股价：%.2f 元
            - 市盈率(PE)：%.2f
            - 市净率(PB)：%.2f
            - DCF估值：%.2f 元
            - 估值评级：%s
            
            请从以下角度进行分析：
            1. 盈利能力分析（ROE、ROA、净利率等）
            2. 偿债能力分析（资产负债率、流动比率等）
            3. 运营效率分析（资产周转率等）
            4. 估值水平分析（PE、PB、DCF等）
            5. 投资价值判断和风险提示
            
            请提供专业、客观的分析意见，字数控制在500字以内。
            """, 
            stockCode,
            financialData.getTotalRevenue() / 100000000, // 转换为亿元
            financialData.getNetProfit() / 100000000,
            financialData.getTotalAssets() / 100000000,
            financialData.getShareholderEquity() / 100000000,
            ratios.getEps(),
            ratios.getRoe(),
            ratios.getRoa(),
            ratios.getDebtToAssetRatio(),
            ratios.getCurrentRatio(),
            ratios.getGrossProfitMargin(),
            ratios.getNetProfitMargin(),
            valuation.getCurrentPrice(),
            valuation.getPeRatio(),
            valuation.getPbRatio(),
            valuation.getDcfValue(),
            valuation.getValuationRating()
        );
        
        return callLLM(prompt);
    }
    
    /**
     * 构建辩论提示词
     */
    private String buildDebatePrompt(int round, FundamentalAnalysisResult analysisResult, List<DebateArgument> previousArguments) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append(String.format("""
            作为基本面分析师，你正在参与第%d轮投资决策辩论。
            
            你的分析结果：
            - 基本面评分：%.2f/10
            - 投资建议：%s
            - 置信度：%.2f
            
            """, round, analysisResult.getFundamentalScore(), 
            analysisResult.getRecommendation(), analysisResult.getConfidence()));
        
        if (round == 1) {
            prompt.append("""
                第1轮：请阐述你的核心观点和论据
                要求：
                1. 基于财务数据提出明确的投资观点
                2. 列举3-5个关键的支撑证据
                3. 说明你的分析逻辑和方法
                4. 控制在200字以内
                """);
        } else if (round == 2) {
            prompt.append("\n其他分析师的观点：\n");
            for (DebateArgument arg : previousArguments) {
                if (!"fundamental_analyst".equals(arg.getAgentId())) {
                    prompt.append(String.format("- %s: %s\n", arg.getAgentName(), arg.getArgument()));
                }
            }
            prompt.append("""
                \n第2轮：请针对其他分析师的观点进行质疑和反驳
                要求：
                1. 指出其他观点的不足或偏差
                2. 用财务数据支撑你的反驳
                3. 强化你的核心观点
                4. 控制在200字以内
                """);
        } else if (round == 3) {
            prompt.append("""
                第3轮：寻求共识和最终建议
                要求：
                1. 综合考虑各方观点
                2. 提出平衡的投资建议
                3. 明确风险和机会
                4. 控制在200字以内
                """);
        }
        
        return prompt.toString();
    }
    
    // 工具方法
    
    private double calculateAssetTurnover(FinancialData data) {
        return data.getTotalAssets() != 0 ? data.getTotalRevenue() / data.getTotalAssets() : 0;
    }
    
    private double calculateEquityMultiplier(FinancialData data) {
        return data.getShareholderEquity() != 0 ? data.getTotalAssets() / data.getShareholderEquity() : 0;
    }
    
    private double calculateDCFValue(FinancialData data) {
        // 简化的DCF计算
        double freeCashFlow = data.getNetProfit() * 0.8; // 简化假设
        double growthRate = 0.05; // 假设5%增长率
        double discountRate = 0.10; // 假设10%折现率
        
        return freeCashFlow * (1 + growthRate) / (discountRate - growthRate);
    }
    
    private double calculateRelativeValue(double pe, double pb) {
        // 简化的相对估值计算
        double industryAvgPE = 15.0;
        double industryAvgPB = 2.0;
        
        double peScore = pe != 0 ? industryAvgPE / pe : 0;
        double pbScore = pb != 0 ? industryAvgPB / pb : 0;
        
        return (peScore + pbScore) / 2;
    }
    
    private String determineValuationRating(double currentPrice, double dcfValue, double pe, double pb) {
        double upside = (dcfValue - currentPrice) / currentPrice;
        
        if (upside > 0.2) return "低估";
        if (upside > 0.1) return "合理偏低";
        if (upside > -0.1) return "合理";
        if (upside > -0.2) return "合理偏高";
        return "高估";
    }
    
    private double calculateUpside(double currentPrice, double dcfValue) {
        return (dcfValue - currentPrice) / currentPrice * 100;
    }
    
    private String assessRiskLevel(FinancialRatios ratios) {
        int riskScore = 0;
        
        if (ratios.getDebtToAssetRatio() > 0.6) riskScore += 2;
        if (ratios.getCurrentRatio() < 1.0) riskScore += 2;
        if (ratios.getRoe() < 5.0) riskScore += 1;
        if (ratios.getNetProfitMargin() < 5.0) riskScore += 1;
        
        if (riskScore >= 4) return "高风险";
        if (riskScore >= 2) return "中等风险";
        return "低风险";
    }
    
    private double calculateFundamentalScore(FinancialRatios ratios, ValuationAnalysis valuation, IndustryComparison industry) {
        double score = 5.0; // 基础分
        
        // ROE评分
        if (ratios.getRoe() > 15) score += 1.5;
        else if (ratios.getRoe() > 10) score += 1.0;
        else if (ratios.getRoe() < 5) score -= 1.0;
        
        // 估值评分
        switch (valuation.getValuationRating()) {
            case "低估" -> score += 2.0;
            case "合理偏低" -> score += 1.0;
            case "合理偏高" -> score -= 1.0;
            case "高估" -> score -= 2.0;
        }
        
        // 风险评分
        switch (valuation.getRiskLevel()) {
            case "低风险" -> score += 1.0;
            case "高风险" -> score -= 1.5;
        }
        
        return Math.max(0, Math.min(10, score));
    }
    
    private String generateRecommendation(double fundamentalScore, ValuationAnalysis valuation) {
        if (fundamentalScore >= 8.0 && "低估".equals(valuation.getValuationRating())) {
            return "强烈买入";
        } else if (fundamentalScore >= 7.0) {
            return "买入";
        } else if (fundamentalScore >= 5.0) {
            return "持有";
        } else if (fundamentalScore >= 3.0) {
            return "卖出";
        } else {
            return "强烈卖出";
        }
    }
    
    private double calculateConfidence(FinancialRatios ratios, ValuationAnalysis valuation) {
        double confidence = 0.5; // 基础置信度
        
        // 数据完整性
        if (ratios.getEps() != 0 && ratios.getRoe() != 0) confidence += 0.2;
        
        // 估值一致性
        if (valuation.getPeRatio() > 0 && valuation.getPbRatio() > 0) confidence += 0.2;
        
        // 风险水平
        if ("低风险".equals(valuation.getRiskLevel())) confidence += 0.1;
        
        return Math.min(1.0, confidence);
    }
    
    private FundamentalAnalysisResult createErrorResult(String errorMessage) {
        return FundamentalAnalysisResult.builder()
                .agentId("fundamental_analyst")
                .agentName("基本面分析师")
                .analysis("分析失败: " + errorMessage)
                .fundamentalScore(0.0)
                .recommendation("无法分析")
                .confidence(0.0)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    private DebateArgument createErrorDebateArgument(String errorMessage) {
        return DebateArgument.builder()
                .agentId("fundamental_analyst")
                .agentName("基本面分析师")
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
    public static class FundamentalAnalysisResult {
        private String stockCode;
        private String agentId;
        private String agentName;
        private String analysis;
        private FinancialRatios financialRatios;
        private ValuationAnalysis valuation;
        private IndustryComparison industryComparison;
        private Double fundamentalScore;
        private String recommendation;
        private Double confidence;
        private Long timestamp;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FinancialRatios {
        private Double eps;                // 每股收益
        private Double roe;                // 净资产收益率
        private Double roa;                // 总资产收益率
        private Double debtToAssetRatio;   // 资产负债率
        private Double currentRatio;       // 流动比率
        private Double quickRatio;         // 速动比率
        private Double grossProfitMargin;  // 毛利率
        private Double netProfitMargin;    // 净利率
        private Double assetTurnover;      // 资产周转率
        private Double equityMultiplier;   // 权益乘数
        private Double priceToBook;        // 市净率
        private Double priceToEarnings;    // 市盈率
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ValuationAnalysis {
        private Double currentPrice;
        private Double peRatio;
        private Double pbRatio;
        private Double dcfValue;
        private Double relativeValue;
        private String valuationRating;
        private Double upside;
        private String riskLevel;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IndustryComparison {
        private String industryName;
        private Double industryAvgPE;
        private Double industryAvgPB;
        private Double industryAvgROE;
        private Double industryAvgROA;
        private String industryRanking;
        private String competitiveAdvantage;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DebateArgument {
        private String agentId;
        private String agentName;
        private Integer round;
        private String argument;
        private Double confidence;
        private String evidenceType;
        private Map<String, Object> supportingData;
        private Long timestamp;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DebateContext {
        private String stockCode;
        private Integer currentRound;
        private Map<String, Object> agentResults;
        
        public Object getAgentResult(String agentId) {
            return agentResults.get(agentId);
        }
    }
}