package com.jd.genie.agent.agent.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 股票分析结果数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAnalysisResult {

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 股票名称
     */
    private String stockName;

    /**
     * 分析类型（技术分析、基本面分析、情绪分析等）
     */
    private String analysisType;

    /**
     * 分析时间
     */
    private LocalDateTime analysisTime;

    /**
     * 分析结论
     */
    private String conclusion;

    /**
     * 投资建议（买入、卖出、持有）
     */
    private String recommendation;

    /**
     * 风险等级（低、中、高）
     */
    private String riskLevel;

    /**
     * 置信度分数（0-1之间）
     */
    private Double confidenceScore;

    /**
     * 目标价格
     */
    private Double targetPrice;

    /**
     * 止损价格
     */
    private Double stopLossPrice;

    /**
     * 关键分析要点
     */
    private List<String> keyPoints;

    /**
     * 支撑位
     */
    private List<Double> supportLevels;

    /**
     * 阻力位
     */
    private List<Double> resistanceLevels;

    /**
     * 技术指标数据
     */
    private Map<String, Object> technicalIndicators;

    /**
     * 基本面数据
     */
    private Map<String, Object> fundamentalData;

    /**
     * 情绪分析数据
     */
    private Map<String, Object> sentimentData;

    /**
     * 风险指标
     */
    private Map<String, Object> riskMetrics;

    /**
     * 原始分析数据
     */
    private Map<String, Object> rawData;

    /**
     * 分析师ID或名称
     */
    private String analystId;

    /**
     * 分析耗时（毫秒）
     */
    private Long analysisTimeMs;

    /**
     * 数据来源
     */
    private List<String> dataSources;

    /**
     * 警告信息
     */
    private List<String> warnings;

    /**
     * 相关新闻或事件
     */
    private List<String> relatedNews;

    /**
     * 行业对比数据
     */
    private Map<String, Object> industryComparison;

    /**
     * 历史表现数据
     */
    private Map<String, Object> historicalPerformance;

    /**
     * 获取简化的分析摘要
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("股票: %s(%s)\n", stockName, stockCode));
        summary.append(String.format("建议: %s\n", recommendation));
        summary.append(String.format("风险: %s\n", riskLevel));
        summary.append(String.format("置信度: %.1f%%\n", confidenceScore * 100));
        
        if (targetPrice != null) {
            summary.append(String.format("目标价: %.2f\n", targetPrice));
        }
        
        return summary.toString();
    }

    /**
     * 检查分析结果是否有效
     */
    public boolean isValid() {
        return stockCode != null && 
               analysisType != null && 
               conclusion != null && 
               confidenceScore != null && 
               confidenceScore >= 0 && 
               confidenceScore <= 1;
    }

    /**
     * 获取风险等级的数值表示
     */
    public int getRiskLevelNumeric() {
        if (riskLevel == null) return 0;
        
        switch (riskLevel.toLowerCase()) {
            case "低":
            case "low":
                return 1;
            case "中":
            case "medium":
                return 2;
            case "高":
            case "high":
                return 3;
            default:
                return 0;
        }
    }

    /**
     * 获取推荐操作的数值表示
     */
    public int getRecommendationNumeric() {
        if (recommendation == null) return 0;
        
        String rec = recommendation.toLowerCase();
        if (rec.contains("强烈买入") || rec.contains("strong buy")) {
            return 5;
        } else if (rec.contains("买入") || rec.contains("buy")) {
            return 4;
        } else if (rec.contains("持有") || rec.contains("hold")) {
            return 3;
        } else if (rec.contains("卖出") || rec.contains("sell")) {
            return 2;
        } else if (rec.contains("强烈卖出") || rec.contains("strong sell")) {
            return 1;
        }
        
        return 0;
    }

    /**
     * 添加关键要点
     */
    public void addKeyPoint(String point) {
        if (keyPoints == null) {
            keyPoints = new java.util.ArrayList<>();
        }
        keyPoints.add(point);
    }

    /**
     * 添加警告信息
     */
    public void addWarning(String warning) {
        if (warnings == null) {
            warnings = new java.util.ArrayList<>();
        }
        warnings.add(warning);
    }

    /**
     * 添加数据来源
     */
    public void addDataSource(String source) {
        if (dataSources == null) {
            dataSources = new java.util.ArrayList<>();
        }
        dataSources.add(source);
    }

    /**
     * 设置技术指标数据
     */
    public void setTechnicalIndicator(String indicator, Object value) {
        if (technicalIndicators == null) {
            technicalIndicators = new java.util.HashMap<>();
        }
        technicalIndicators.put(indicator, value);
    }

    /**
     * 设置基本面数据
     */
    public void setFundamentalData(String key, Object value) {
        if (fundamentalData == null) {
            fundamentalData = new java.util.HashMap<>();
        }
        fundamentalData.put(key, value);
    }

    /**
     * 设置情绪数据
     */
    public void setSentimentData(String key, Object value) {
        if (sentimentData == null) {
            sentimentData = new java.util.HashMap<>();
        }
        sentimentData.put(key, value);
    }

    /**
     * 设置风险指标
     */
    public void setRiskMetric(String metric, Object value) {
        if (riskMetrics == null) {
            riskMetrics = new java.util.HashMap<>();
        }
        riskMetrics.put(metric, value);
    }
}