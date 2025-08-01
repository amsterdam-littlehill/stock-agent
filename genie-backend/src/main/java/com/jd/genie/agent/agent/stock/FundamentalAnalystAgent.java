package com.jd.genie.agent.agent.stock;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 基本面分析师智能体
 * 专注于公司财务数据分析、估值模型计算和基本面评估
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class FundamentalAnalystAgent extends StockAnalysisAgent {

    private static final String AGENT_NAME = "基本面分析师";
    private static final String AGENT_DESCRIPTION = "专业的基本面分析师，擅长财务报表分析、估值模型和公司基本面评估";
    
    // 估值参数
    private double riskFreeRate = 0.03;    // 无风险利率
    private double marketRiskPremium = 0.06; // 市场风险溢价
    private double terminalGrowthRate = 0.02; // 永续增长率
    
    // 财务健康度权重
    private Map<String, Double> financialHealthWeights;

    public FundamentalAnalystAgent() {
        setName(AGENT_NAME);
        setDescription(AGENT_DESCRIPTION);
        setAnalysisType("基本面分析");
        
        // 初始化财务健康度权重
        initializeFinancialHealthWeights();
        
        // 设置系统提示词
        setSystemPrompt(buildSystemPrompt());
    }

    @Override
    public StockAnalysisResult performAnalysis(String stockCode, Map<String, Object> parameters) {
        try {
            log.info("{} 开始对股票 {} 进行基本面分析", getName(), stockCode);
            
            long startTime = System.currentTimeMillis();
            
            // 1. 获取财务数据
            Map<String, Object> financialData = getFinancialData(stockCode, parameters);
            if (financialData == null || financialData.isEmpty()) {
                throw new RuntimeException("无法获取财务数据");
            }
            
            // 2. 计算财务比率
            Map<String, Object> financialRatios = calculateFinancialRatios(financialData);
            
            // 3. 进行估值分析
            Map<String, Object> valuationMetrics = performValuationAnalysis(financialData, financialRatios);
            
            // 4. 评估财务健康度
            double financialHealthScore = assessFinancialHealth(financialRatios);
            
            // 5. 行业对比分析
            Map<String, Object> industryComparison = performIndustryComparison(stockCode, financialRatios);
            
            // 6. 生成投资建议
            String investmentAdvice = generateInvestmentAdvice(financialRatios, valuationMetrics, financialHealthScore);
            
            // 7. 计算置信度
            double confidence = calculateConfidence(financialData, financialRatios, valuationMetrics);
            
            // 8. 构建分析结果
            StockAnalysisResult result = StockAnalysisResult.builder()
                    .stockCode(stockCode)
                    .stockName(getStockName(stockCode))
                    .analysisType("基本面分析")
                    .analysisTime(LocalDateTime.now())
                    .conclusion(buildConclusion(investmentAdvice, financialHealthScore))
                    .recommendation(generateRecommendation(investmentAdvice, confidence))
                    .riskLevel(assessRiskLevel(financialRatios, financialHealthScore))
                    .confidenceScore(confidence)
                    .targetPrice(calculateTargetPrice(valuationMetrics))
                    .fundamentalData(financialRatios)
                    .industryComparison(industryComparison)
                    .analystId(getName())
                    .analysisTimeMs(System.currentTimeMillis() - startTime)
                    .build();
            
            // 9. 添加关键要点
            addKeyPoints(result, financialRatios, valuationMetrics, financialHealthScore);
            
            // 10. 添加数据来源
            result.addDataSource("财务报表数据");
            result.addDataSource("估值模型计算");
            result.addDataSource("行业对比数据");
            
            log.info("{} 完成基本面分析，财务健康度: {:.2f}, 置信度: {:.2f}", 
                    getName(), financialHealthScore, confidence);
            
            return result;
            
        } catch (Exception e) {
            log.error("{} 基本面分析失败", getName(), e);
            throw new RuntimeException("基本面分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取财务数据
     */
    private Map<String, Object> getFinancialData(String stockCode, Map<String, Object> parameters) {
        // 这里应该调用实际的财务数据获取工具
        // 暂时返回模拟数据
        Map<String, Object> data = new HashMap<>();
        
        // 模拟财务数据
        data.put("revenue", 1000000000.0);           // 营业收入
        data.put("net_income", 80000000.0);          // 净利润
        data.put("total_assets", 800000000.0);       // 总资产
        data.put("total_equity", 400000000.0);       // 股东权益
        data.put("total_debt", 200000000.0);         // 总负债
        data.put("operating_cash_flow", 90000000.0); // 经营现金流
        data.put("shares_outstanding", 100000000.0);  // 流通股本
        data.put("current_assets", 300000000.0);     // 流动资产
        data.put("current_liabilities", 150000000.0); // 流动负债
        data.put("market_cap", 2500000000.0);        // 市值
        data.put("book_value", 400000000.0);         // 账面价值
        data.put("dividend_per_share", 0.5);         // 每股股息
        
        // 历史数据（用于增长率计算）
        data.put("revenue_growth_rate", 0.15);       // 营收增长率
        data.put("earnings_growth_rate", 0.20);      // 盈利增长率
        data.put("roe_history", Arrays.asList(0.18, 0.19, 0.20)); // 历史ROE
        
        return data;
    }

    /**
     * 计算财务比率
     */
    private Map<String, Object> calculateFinancialRatios(Map<String, Object> financialData) {
        Map<String, Object> ratios = new HashMap<>();
        
        double revenue = (Double) financialData.get("revenue");
        double netIncome = (Double) financialData.get("net_income");
        double totalAssets = (Double) financialData.get("total_assets");
        double totalEquity = (Double) financialData.get("total_equity");
        double totalDebt = (Double) financialData.get("total_debt");
        double operatingCashFlow = (Double) financialData.get("operating_cash_flow");
        double sharesOutstanding = (Double) financialData.get("shares_outstanding");
        double currentAssets = (Double) financialData.get("current_assets");
        double currentLiabilities = (Double) financialData.get("current_liabilities");
        double marketCap = (Double) financialData.get("market_cap");
        double bookValue = (Double) financialData.get("book_value");
        
        // 盈利能力比率
        ratios.put("net_profit_margin", netIncome / revenue);                    // 净利润率
        ratios.put("roa", netIncome / totalAssets);                             // 资产收益率
        ratios.put("roe", netIncome / totalEquity);                             // 股东权益收益率
        ratios.put("eps", netIncome / sharesOutstanding);                       // 每股收益
        
        // 偿债能力比率
        ratios.put("debt_to_equity", totalDebt / totalEquity);                  // 负债权益比
        ratios.put("debt_to_assets", totalDebt / totalAssets);                  // 负债资产比
        ratios.put("current_ratio", currentAssets / currentLiabilities);        // 流动比率
        ratios.put("interest_coverage", operatingCashFlow / (totalDebt * 0.05)); // 利息保障倍数（假设5%利率）
        
        // 估值比率
        double currentPrice = marketCap / sharesOutstanding;
        ratios.put("pe_ratio", currentPrice / (netIncome / sharesOutstanding)); // 市盈率
        ratios.put("pb_ratio", marketCap / bookValue);                          // 市净率
        ratios.put("ps_ratio", marketCap / revenue);                            // 市销率
        
        // 运营效率比率
        ratios.put("asset_turnover", revenue / totalAssets);                    // 资产周转率
        ratios.put("equity_turnover", revenue / totalEquity);                   // 权益周转率
        
        // 现金流比率
        ratios.put("operating_cash_flow_ratio", operatingCashFlow / currentLiabilities); // 经营现金流比率
        ratios.put("cash_flow_to_debt", operatingCashFlow / totalDebt);         // 现金流负债比
        
        // 增长率
        ratios.put("revenue_growth_rate", financialData.get("revenue_growth_rate"));
        ratios.put("earnings_growth_rate", financialData.get("earnings_growth_rate"));
        
        return ratios;
    }

    /**
     * 进行估值分析
     */
    private Map<String, Object> performValuationAnalysis(Map<String, Object> financialData, Map<String, Object> ratios) {
        Map<String, Object> valuation = new HashMap<>();
        
        double netIncome = (Double) financialData.get("net_income");
        double sharesOutstanding = (Double) financialData.get("shares_outstanding");
        double marketCap = (Double) financialData.get("market_cap");
        double bookValue = (Double) financialData.get("book_value");
        double earningsGrowthRate = (Double) ratios.get("earnings_growth_rate");
        double roe = (Double) ratios.get("roe");
        
        // DCF估值
        double dcfValue = calculateDCFValue(netIncome, earningsGrowthRate);
        valuation.put("dcf_value", dcfValue);
        valuation.put("dcf_value_per_share", dcfValue / sharesOutstanding);
        
        // PEG比率
        double peRatio = (Double) ratios.get("pe_ratio");
        double pegRatio = peRatio / (earningsGrowthRate * 100);
        valuation.put("peg_ratio", pegRatio);
        
        // 格雷厄姆内在价值
        double eps = (Double) ratios.get("eps");
        double grahamValue = calculateGrahamIntrinsicValue(eps, earningsGrowthRate);
        valuation.put("graham_intrinsic_value", grahamValue);
        
        // 账面价值倍数
        valuation.put("book_value_multiple", marketCap / bookValue);
        
        // ROE调整估值
        double roeAdjustedValue = calculateROEAdjustedValue(bookValue, roe, sharesOutstanding);
        valuation.put("roe_adjusted_value_per_share", roeAdjustedValue);
        
        // 综合估值
        double currentPrice = marketCap / sharesOutstanding;
        double fairValue = (dcfValue / sharesOutstanding + grahamValue + roeAdjustedValue) / 3;
        valuation.put("fair_value_per_share", fairValue);
        valuation.put("current_price", currentPrice);
        valuation.put("upside_potential", (fairValue - currentPrice) / currentPrice);
        
        return valuation;
    }

    /**
     * 计算DCF估值
     */
    private double calculateDCFValue(double currentEarnings, double growthRate) {
        double terminalValue = currentEarnings * Math.pow(1 + growthRate, 5) * (1 + terminalGrowthRate) / (riskFreeRate + marketRiskPremium - terminalGrowthRate);
        
        double presentValue = 0;
        for (int year = 1; year <= 5; year++) {
            double futureEarnings = currentEarnings * Math.pow(1 + growthRate, year);
            presentValue += futureEarnings / Math.pow(1 + riskFreeRate + marketRiskPremium, year);
        }
        
        presentValue += terminalValue / Math.pow(1 + riskFreeRate + marketRiskPremium, 5);
        
        return presentValue;
    }

    /**
     * 计算格雷厄姆内在价值
     */
    private double calculateGrahamIntrinsicValue(double eps, double growthRate) {
        // 格雷厄姆公式: V = EPS × (8.5 + 2g) × 4.4 / Y
        // 其中 g 是增长率，Y 是AAA级企业债券收益率
        double aaBondYield = 0.04; // 假设4%
        return eps * (8.5 + 2 * growthRate * 100) * 4.4 / aaBondYield;
    }

    /**
     * 计算ROE调整估值
     */
    private double calculateROEAdjustedValue(double bookValue, double roe, double sharesOutstanding) {
        // 基于ROE的估值：如果ROE高于市场平均水平，给予溢价
        double marketAverageROE = 0.15; // 假设市场平均ROE为15%
        double roePremium = Math.max(0, (roe - marketAverageROE) / marketAverageROE);
        double adjustedBookValue = bookValue * (1 + roePremium);
        return adjustedBookValue / sharesOutstanding;
    }

    /**
     * 评估财务健康度
     */
    private double assessFinancialHealth(Map<String, Object> ratios) {
        double healthScore = 0.0;
        
        // 盈利能力评分
        double roe = (Double) ratios.get("roe");
        double netProfitMargin = (Double) ratios.get("net_profit_margin");
        healthScore += financialHealthWeights.get("profitability") * 
                      (normalizeRatio(roe, 0.15, 0.25) + normalizeRatio(netProfitMargin, 0.05, 0.15)) / 2;
        
        // 偿债能力评分
        double debtToEquity = (Double) ratios.get("debt_to_equity");
        double currentRatio = (Double) ratios.get("current_ratio");
        healthScore += financialHealthWeights.get("solvency") * 
                      (normalizeRatio(1 / debtToEquity, 0.5, 2.0) + normalizeRatio(currentRatio, 1.2, 2.5)) / 2;
        
        // 运营效率评分
        double assetTurnover = (Double) ratios.get("asset_turnover");
        healthScore += financialHealthWeights.get("efficiency") * normalizeRatio(assetTurnover, 0.5, 1.5);
        
        // 增长性评分
        double revenueGrowth = (Double) ratios.get("revenue_growth_rate");
        double earningsGrowth = (Double) ratios.get("earnings_growth_rate");
        healthScore += financialHealthWeights.get("growth") * 
                      (normalizeRatio(revenueGrowth, 0.05, 0.20) + normalizeRatio(earningsGrowth, 0.10, 0.30)) / 2;
        
        return Math.min(1.0, Math.max(0.0, healthScore));
    }

    /**
     * 标准化比率到0-1区间
     */
    private double normalizeRatio(double value, double minGood, double maxExcellent) {
        if (value <= 0) return 0.0;
        if (value >= maxExcellent) return 1.0;
        if (value <= minGood) return value / minGood * 0.6;
        return 0.6 + (value - minGood) / (maxExcellent - minGood) * 0.4;
    }

    /**
     * 行业对比分析
     */
    private Map<String, Object> performIndustryComparison(String stockCode, Map<String, Object> ratios) {
        Map<String, Object> comparison = new HashMap<>();
        
        // 模拟行业平均数据
        Map<String, Double> industryAverages = new HashMap<>();
        industryAverages.put("pe_ratio", 18.0);
        industryAverages.put("pb_ratio", 2.5);
        industryAverages.put("roe", 0.15);
        industryAverages.put("debt_to_equity", 0.6);
        industryAverages.put("net_profit_margin", 0.08);
        
        // 计算相对表现
        for (Map.Entry<String, Double> entry : industryAverages.entrySet()) {
            String metric = entry.getKey();
            double industryAvg = entry.getValue();
            double companyValue = (Double) ratios.get(metric);
            
            double relativePerformance = companyValue / industryAvg;
            comparison.put(metric + "_relative", relativePerformance);
            comparison.put(metric + "_industry_avg", industryAvg);
        }
        
        // 计算综合排名
        double overallRanking = calculateOverallRanking(ratios, industryAverages);
        comparison.put("overall_ranking_percentile", overallRanking);
        
        return comparison;
    }

    /**
     * 计算综合排名
     */
    private double calculateOverallRanking(Map<String, Object> ratios, Map<String, Double> industryAverages) {
        double totalScore = 0.0;
        int count = 0;
        
        // 正向指标（越高越好）
        String[] positiveMetrics = {"roe", "net_profit_margin", "current_ratio"};
        for (String metric : positiveMetrics) {
            if (ratios.containsKey(metric) && industryAverages.containsKey(metric)) {
                double ratio = (Double) ratios.get(metric) / industryAverages.get(metric);
                totalScore += Math.min(2.0, ratio); // 最高2倍
                count++;
            }
        }
        
        // 负向指标（越低越好）
        String[] negativeMetrics = {"debt_to_equity", "pe_ratio"};
        for (String metric : negativeMetrics) {
            if (ratios.containsKey(metric) && industryAverages.containsKey(metric)) {
                double ratio = industryAverages.get(metric) / (Double) ratios.get(metric);
                totalScore += Math.min(2.0, ratio); // 最高2倍
                count++;
            }
        }
        
        double averageScore = totalScore / count;
        // 转换为百分位数（假设正态分布）
        return Math.min(95.0, Math.max(5.0, averageScore * 50));
    }

    /**
     * 生成投资建议
     */
    private String generateInvestmentAdvice(Map<String, Object> ratios, Map<String, Object> valuationMetrics, double healthScore) {
        double upsidePotential = (Double) valuationMetrics.get("upside_potential");
        double pegRatio = (Double) valuationMetrics.get("peg_ratio");
        double roe = (Double) ratios.get("roe");
        
        StringBuilder advice = new StringBuilder();
        
        // 估值判断
        if (upsidePotential > 0.2) {
            advice.append("估值偏低，具有较大上涨空间。");
        } else if (upsidePotential < -0.1) {
            advice.append("估值偏高，存在回调风险。");
        } else {
            advice.append("估值相对合理。");
        }
        
        // PEG判断
        if (pegRatio < 1.0) {
            advice.append("PEG比率较低，成长性估值合理。");
        } else if (pegRatio > 2.0) {
            advice.append("PEG比率较高，成长性估值偏贵。");
        }
        
        // 财务健康度判断
        if (healthScore > 0.8) {
            advice.append("财务状况优秀。");
        } else if (healthScore > 0.6) {
            advice.append("财务状况良好。");
        } else {
            advice.append("财务状况需要关注。");
        }
        
        // ROE判断
        if (roe > 0.2) {
            advice.append("股东回报率优秀。");
        } else if (roe < 0.1) {
            advice.append("股东回报率偏低。");
        }
        
        return advice.toString();
    }

    /**
     * 计算置信度
     */
    private double calculateConfidence(Map<String, Object> financialData, Map<String, Object> ratios, Map<String, Object> valuationMetrics) {
        double baseConfidence = 0.7;
        
        // 数据完整性检查
        if (financialData.size() >= 10) {
            baseConfidence += 0.1;
        }
        
        // 财务指标一致性检查
        double roe = (Double) ratios.get("roe");
        double netProfitMargin = (Double) ratios.get("net_profit_margin");
        double pegRatio = (Double) valuationMetrics.get("peg_ratio");
        
        // 如果多个指标都显示良好，提高置信度
        if (roe > 0.15 && netProfitMargin > 0.08 && pegRatio < 1.5) {
            baseConfidence += 0.15;
        }
        
        return Math.min(0.95, baseConfidence);
    }

    /**
     * 生成投资建议
     */
    private String generateRecommendation(String advice, double confidence) {
        if (confidence < 0.6) {
            return "观望";
        }
        
        if (advice.contains("上涨空间") && advice.contains("优秀")) {
            return confidence > 0.8 ? "强烈买入" : "买入";
        } else if (advice.contains("回调风险") || advice.contains("需要关注")) {
            return confidence > 0.8 ? "强烈卖出" : "卖出";
        } else {
            return "持有";
        }
    }

    /**
     * 评估风险等级
     */
    private String assessRiskLevel(Map<String, Object> ratios, double healthScore) {
        double debtToEquity = (Double) ratios.get("debt_to_equity");
        double currentRatio = (Double) ratios.get("current_ratio");
        
        if (healthScore < 0.5 || debtToEquity > 1.5 || currentRatio < 1.0) {
            return "高";
        } else if (healthScore < 0.7 || debtToEquity > 1.0 || currentRatio < 1.5) {
            return "中";
        } else {
            return "低";
        }
    }

    /**
     * 计算目标价格
     */
    private Double calculateTargetPrice(Map<String, Object> valuationMetrics) {
        return (Double) valuationMetrics.get("fair_value_per_share");
    }

    /**
     * 添加关键要点
     */
    private void addKeyPoints(StockAnalysisResult result, Map<String, Object> ratios, 
                             Map<String, Object> valuationMetrics, double healthScore) {
        
        result.addKeyPoint(String.format("财务健康度评分: %.1f/10", healthScore * 10));
        result.addKeyPoint(String.format("ROE: %.1f%%, 净利润率: %.1f%%", 
                (Double) ratios.get("roe") * 100, 
                (Double) ratios.get("net_profit_margin") * 100));
        result.addKeyPoint(String.format("PE: %.1f, PB: %.1f, PEG: %.2f", 
                (Double) ratios.get("pe_ratio"),
                (Double) ratios.get("pb_ratio"),
                (Double) valuationMetrics.get("peg_ratio")));
        result.addKeyPoint(String.format("估值上涨空间: %.1f%%", 
                (Double) valuationMetrics.get("upside_potential") * 100));
        
        // 添加风险提示
        double debtToEquity = (Double) ratios.get("debt_to_equity");
        if (debtToEquity > 1.0) {
            result.addWarning("负债率较高，关注偿债风险");
        }
        
        double pegRatio = (Double) valuationMetrics.get("peg_ratio");
        if (pegRatio > 2.0) {
            result.addWarning("PEG比率偏高，估值可能过于乐观");
        }
    }

    /**
     * 构建分析结论
     */
    private String buildConclusion(String advice, double healthScore) {
        return String.format("基于基本面分析，%s 财务健康度评分为%.1f分（满分10分）。%s", 
                getStockCode(), healthScore * 10, advice);
    }

    /**
     * 获取股票名称
     */
    private String getStockName(String stockCode) {
        // 这里应该从数据源获取股票名称
        return "股票-" + stockCode;
    }

    /**
     * 初始化财务健康度权重
     */
    private void initializeFinancialHealthWeights() {
        financialHealthWeights = new HashMap<>();
        financialHealthWeights.put("profitability", 0.35);  // 盈利能力35%
        financialHealthWeights.put("solvency", 0.25);       // 偿债能力25%
        financialHealthWeights.put("efficiency", 0.20);     // 运营效率20%
        financialHealthWeights.put("growth", 0.20);         // 增长性20%
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        return "你是一位资深的基本面分析师，拥有20年的财务分析和投资研究经验。" +
               "你擅长分析公司财务报表、计算各种财务比率、进行估值建模和行业对比分析。" +
               "你熟悉DCF估值、格雷厄姆价值投资理论、ROE分析等多种估值方法。" +
               "请始终基于客观的财务数据进行分析，并明确指出分析的假设条件和局限性。";
    }
}