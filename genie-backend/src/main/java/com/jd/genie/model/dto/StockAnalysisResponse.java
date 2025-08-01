package com.jd.genie.model.dto;

import com.jd.genie.agent.agent.stock.StockAnalysisResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 股票分析响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAnalysisResponse {

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 股票名称
     */
    private String stockName;

    /**
     * 分析状态
     * PENDING: 等待中
     * PROCESSING: 处理中
     * COMPLETED: 已完成
     * FAILED: 失败
     */
    @Builder.Default
    private String status = "COMPLETED";

    /**
     * 是否成功
     */
    @Builder.Default
    private Boolean success = true;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;

    /**
     * 分析开始时间
     */
    private LocalDateTime startTime;

    /**
     * 分析完成时间
     */
    private LocalDateTime endTime;

    /**
     * 处理耗时（毫秒）
     */
    private Long processingTimeMs;

    /**
     * 分析结果文本
     */
    private String analysisResult;

    /**
     * 综合投资建议
     */
    private String recommendation;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 综合置信度
     */
    private Double confidenceScore;

    /**
     * 目标价格
     */
    private Double targetPrice;

    /**
     * 当前价格
     */
    private Double currentPrice;

    /**
     * 上涨空间百分比
     */
    private Double upsidePotential;

    /**
     * 关键要点
     */
    private List<String> keyPoints;

    /**
     * 风险提示
     */
    private List<String> warnings;

    /**
     * 参与分析的智能体列表
     */
    private List<AgentAnalysisInfo> agentAnalyses;

    /**
     * 技术分析结果
     */
    private TechnicalAnalysisInfo technicalAnalysis;

    /**
     * 基本面分析结果
     */
    private FundamentalAnalysisInfo fundamentalAnalysis;

    /**
     * 情绪分析结果
     */
    private SentimentAnalysisInfo sentimentAnalysis;

    /**
     * 行业对比数据
     */
    private Map<String, Object> industryComparison;

    /**
     * 历史表现数据
     */
    private Map<String, Object> historicalPerformance;

    /**
     * 相关新闻
     */
    private List<String> relatedNews;

    /**
     * 数据来源
     */
    private List<String> dataSources;

    /**
     * 分析配置
     */
    private Map<String, Object> analysisConfig;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 分析标签
     */
    private List<String> tags;

    /**
     * 智能体分析信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentAnalysisInfo {
        private String agentType;
        private String agentName;
        private String recommendation;
        private Double confidence;
        private String riskLevel;
        private Double targetPrice;
        private String conclusion;
        private List<String> keyPoints;
        private Map<String, Object> detailedData;
        private Long processingTimeMs;
    }

    /**
     * 技术分析信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnicalAnalysisInfo {
        private String trendDirection;
        private Map<String, Double> technicalIndicators;
        private List<Double> supportLevels;
        private List<Double> resistanceLevels;
        private String tradingSignal;
        private Double signalStrength;
        private Map<String, String> patternAnalysis;
    }

    /**
     * 基本面分析信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FundamentalAnalysisInfo {
        private Double financialHealthScore;
        private Map<String, Double> financialRatios;
        private Map<String, Double> valuationMetrics;
        private Double fairValuePerShare;
        private String valuationConclusion;
        private Map<String, Object> industryRanking;
    }

    /**
     * 情绪分析信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SentimentAnalysisInfo {
        private Double sentimentScore;
        private String sentimentTrend;
        private Map<String, Double> newsSentiment;
        private Map<String, Double> socialMediaSentiment;
        private List<String> sentimentDrivers;
        private String marketMood;
    }

    /**
     * 从StockAnalysisResult转换
     */
    public static StockAnalysisResponse fromStockAnalysisResult(StockAnalysisResult result, String requestId) {
        if (result == null) {
            return StockAnalysisResponse.builder()
                    .requestId(requestId)
                    .success(false)
                    .status("FAILED")
                    .errorMessage("分析结果为空")
                    .build();
        }

        StockAnalysisResponseBuilder builder = StockAnalysisResponse.builder()
                .requestId(requestId)
                .stockCode(result.getStockCode())
                .stockName(result.getStockName())
                .status("COMPLETED")
                .success(true)
                .endTime(result.getAnalysisTime())
                .processingTimeMs(result.getAnalysisTimeMs())
                .analysisResult(result.getConclusion())
                .recommendation(result.getRecommendation())
                .riskLevel(result.getRiskLevel())
                .confidenceScore(result.getConfidenceScore())
                .targetPrice(result.getTargetPrice())
                .keyPoints(result.getKeyPoints())
                .warnings(result.getWarnings())
                .dataSources(result.getDataSources());

        // 设置技术分析信息
        if (result.getTechnicalIndicators() != null) {
            TechnicalAnalysisInfo techInfo = TechnicalAnalysisInfo.builder()
                    .technicalIndicators(convertToDoubleMap(result.getTechnicalIndicators()))
                    .supportLevels(result.getSupportLevels())
                    .resistanceLevels(result.getResistanceLevels())
                    .build();
            builder.technicalAnalysis(techInfo);
        }

        // 设置基本面分析信息
        if (result.getFundamentalData() != null) {
            FundamentalAnalysisInfo fundInfo = FundamentalAnalysisInfo.builder()
                    .financialRatios(convertToDoubleMap(result.getFundamentalData()))
                    .fairValuePerShare(result.getTargetPrice())
                    .industryRanking(result.getIndustryComparison())
                    .build();
            builder.fundamentalAnalysis(fundInfo);
        }

        // 设置情绪分析信息
        if (result.getSentimentData() != null) {
            SentimentAnalysisInfo sentInfo = SentimentAnalysisInfo.builder()
                    .newsSentiment(convertToDoubleMap(result.getSentimentData()))
                    .build();
            builder.sentimentAnalysis(sentInfo);
        }

        // 设置行业对比和历史表现
        builder.industryComparison(result.getIndustryComparison())
                .historicalPerformance(result.getHistoricalPerformance())
                .relatedNews(result.getRelatedNews());

        return builder.build();
    }

    /**
     * 转换Map<String, Object>为Map<String, Double>
     */
    private static Map<String, Double> convertToDoubleMap(Map<String, Object> source) {
        if (source == null) return null;
        
        Map<String, Double> result = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Number) {
                result.put(entry.getKey(), ((Number) value).doubleValue());
            }
        }
        return result;
    }

    /**
     * 获取分析摘要
     */
    public String getSummary() {
        if (!success) {
            return String.format("股票%s分析失败: %s", stockCode, errorMessage);
        }

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("股票: %s", stockCode));
        if (stockName != null) {
            summary.append(String.format("(%s)", stockName));
        }
        summary.append(String.format(", 建议: %s", recommendation));
        summary.append(String.format(", 风险: %s", riskLevel));
        if (confidenceScore != null) {
            summary.append(String.format(", 置信度: %.1f%%", confidenceScore * 100));
        }
        if (targetPrice != null) {
            summary.append(String.format(", 目标价: %.2f", targetPrice));
        }

        return summary.toString();
    }

    /**
     * 获取处理时长描述
     */
    public String getProcessingTimeDescription() {
        if (processingTimeMs == null) {
            return "未知";
        }

        if (processingTimeMs < 1000) {
            return processingTimeMs + "毫秒";
        } else if (processingTimeMs < 60000) {
            return String.format("%.1f秒", processingTimeMs / 1000.0);
        } else {
            return String.format("%.1f分钟", processingTimeMs / 60000.0);
        }
    }

    /**
     * 获取风险等级数值
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
     * 计算上涨空间
     */
    public void calculateUpsidePotential() {
        if (targetPrice != null && currentPrice != null && currentPrice > 0) {
            upsidePotential = (targetPrice - currentPrice) / currentPrice;
        }
    }

    /**
     * 添加智能体分析信息
     */
    public void addAgentAnalysis(AgentAnalysisInfo agentInfo) {
        if (agentAnalyses == null) {
            agentAnalyses = new java.util.ArrayList<>();
        }
        agentAnalyses.add(agentInfo);
    }

    /**
     * 获取特定类型的智能体分析
     */
    public AgentAnalysisInfo getAgentAnalysis(String agentType) {
        if (agentAnalyses == null) return null;
        
        return agentAnalyses.stream()
                .filter(agent -> agentType.equals(agent.getAgentType()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查是否包含特定类型的分析
     */
    public boolean hasAnalysisType(String analysisType) {
        if (agentAnalyses == null) return false;
        
        return agentAnalyses.stream()
                .anyMatch(agent -> analysisType.equals(agent.getAgentType()));
    }

    /**
     * 获取分析完整性评分
     */
    public double getCompletenessScore() {
        double score = 0.0;
        int totalChecks = 0;
        
        // 基础信息检查
        if (recommendation != null) { score += 1; } totalChecks++;
        if (riskLevel != null) { score += 1; } totalChecks++;
        if (confidenceScore != null) { score += 1; } totalChecks++;
        if (targetPrice != null) { score += 1; } totalChecks++;
        
        // 详细分析检查
        if (keyPoints != null && !keyPoints.isEmpty()) { score += 1; } totalChecks++;
        if (technicalAnalysis != null) { score += 1; } totalChecks++;
        if (fundamentalAnalysis != null) { score += 1; } totalChecks++;
        if (industryComparison != null && !industryComparison.isEmpty()) { score += 1; } totalChecks++;
        
        return totalChecks > 0 ? score / totalChecks : 0.0;
    }

    /**
     * 是否为高质量分析
     */
    public boolean isHighQualityAnalysis() {
        return success &&
               confidenceScore != null && confidenceScore >= 0.7 &&
               getCompletenessScore() >= 0.8 &&
               (agentAnalyses != null && agentAnalyses.size() >= 2);
    }

    /**
     * 获取分析质量等级
     */
    public String getQualityGrade() {
        if (!success) return "F";
        
        double completeness = getCompletenessScore();
        double confidence = confidenceScore != null ? confidenceScore : 0.0;
        double qualityScore = (completeness + confidence) / 2;
        
        if (qualityScore >= 0.9) return "A+";
        if (qualityScore >= 0.8) return "A";
        if (qualityScore >= 0.7) return "B+";
        if (qualityScore >= 0.6) return "B";
        if (qualityScore >= 0.5) return "C";
        return "D";
    }
}