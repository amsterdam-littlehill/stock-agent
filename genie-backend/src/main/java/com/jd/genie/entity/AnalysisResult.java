package com.jd.genie.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分析结果实体类
 */
@Entity
@Table(name = "analysis_result", indexes = {
    @Index(name = "idx_task_id", columnList = "task_id"),
    @Index(name = "idx_stock_code", columnList = "stock_code"),
    @Index(name = "idx_agent_type", columnList = "agent_type"),
    @Index(name = "idx_analysis_type", columnList = "analysis_type"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 关联的任务ID
     */
    @Column(name = "task_id", nullable = false)
    private Long taskId;
    
    /**
     * 股票代码
     */
    @Column(name = "stock_code", nullable = false, length = 20)
    private String stockCode;
    
    /**
     * 智能体类型 (TECHNICAL-技术分析师, FUNDAMENTAL-基本面分析师, SENTIMENT-情绪分析师, COORDINATOR-协调器)
     */
    @Column(name = "agent_type", nullable = false, length = 50)
    private String agentType;
    
    /**
     * 分析类型 (TECHNICAL-技术分析, FUNDAMENTAL-基本面分析, SENTIMENT-情绪分析)
     */
    @Column(name = "analysis_type", nullable = false, length = 50)
    private String analysisType;
    
    /**
     * 分析结果文本
     */
    @Column(name = "analysis_text", columnDefinition = "LONGTEXT")
    private String analysisText;
    
    /**
     * 投资建议 (BUY-买入, HOLD-持有, SELL-卖出)
     */
    @Column(name = "recommendation", length = 20)
    private String recommendation;
    
    /**
     * 风险等级 (LOW-低风险, MEDIUM-中风险, HIGH-高风险)
     */
    @Column(name = "risk_level", length = 20)
    private String riskLevel;
    
    /**
     * 置信度 (0-100)
     */
    @Column(name = "confidence", precision = 5, scale = 2)
    private BigDecimal confidence;
    
    /**
     * 目标价格
     */
    @Column(name = "target_price", precision = 12, scale = 4)
    private BigDecimal targetPrice;
    
    /**
     * 分析时的当前价格
     */
    @Column(name = "current_price", precision = 12, scale = 4)
    private BigDecimal currentPrice;
    
    /**
     * 上涨空间(%)
     */
    @Column(name = "upside_potential", precision = 8, scale = 4)
    private BigDecimal upsidePotential;
    
    /**
     * 技术指标数据 (JSON格式)
     */
    @Column(name = "technical_indicators", columnDefinition = "TEXT")
    private String technicalIndicators;
    
    /**
     * 基本面数据 (JSON格式)
     */
    @Column(name = "fundamental_data", columnDefinition = "TEXT")
    private String fundamentalData;
    
    /**
     * 情绪分析数据 (JSON格式)
     */
    @Column(name = "sentiment_data", columnDefinition = "TEXT")
    private String sentimentData;
    
    /**
     * 关键要点 (JSON格式)
     */
    @Column(name = "key_points", columnDefinition = "TEXT")
    private String keyPoints;
    
    /**
     * 风险提示 (JSON格式)
     */
    @Column(name = "risk_warnings", columnDefinition = "TEXT")
    private String riskWarnings;
    
    /**
     * 支撑位 (JSON格式)
     */
    @Column(name = "support_levels", columnDefinition = "TEXT")
    private String supportLevels;
    
    /**
     * 阻力位 (JSON格式)
     */
    @Column(name = "resistance_levels", columnDefinition = "TEXT")
    private String resistanceLevels;
    
    /**
     * 趋势分析
     */
    @Column(name = "trend_analysis", columnDefinition = "TEXT")
    private String trendAnalysis;
    
    /**
     * 成交量分析
     */
    @Column(name = "volume_analysis", columnDefinition = "TEXT")
    private String volumeAnalysis;
    
    /**
     * 财务比率 (JSON格式)
     */
    @Column(name = "financial_ratios", columnDefinition = "TEXT")
    private String financialRatios;
    
    /**
     * 估值分析
     */
    @Column(name = "valuation_analysis", columnDefinition = "TEXT")
    private String valuationAnalysis;
    
    /**
     * 行业对比数据 (JSON格式)
     */
    @Column(name = "industry_comparison", columnDefinition = "TEXT")
    private String industryComparison;
    
    /**
     * 新闻情绪分析 (JSON格式)
     */
    @Column(name = "news_sentiment", columnDefinition = "TEXT")
    private String newsSentiment;
    
    /**
     * 社交媒体情绪 (JSON格式)
     */
    @Column(name = "social_sentiment", columnDefinition = "TEXT")
    private String socialSentiment;
    
    /**
     * 市场情绪指标 (JSON格式)
     */
    @Column(name = "market_sentiment", columnDefinition = "TEXT")
    private String marketSentiment;
    
    /**
     * 分析权重 (用于综合分析时的权重计算)
     */
    @Column(name = "weight", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal weight = new BigDecimal("1.0000");
    
    /**
     * 分析得分 (0-100)
     */
    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;
    
    /**
     * 分析耗时(毫秒)
     */
    @Column(name = "duration_ms")
    private Long durationMs;
    
    /**
     * 数据来源
     */
    @Column(name = "data_source", length = 100)
    private String dataSource;
    
    /**
     * 分析版本
     */
    @Column(name = "version", length = 20)
    @Builder.Default
    private String version = "1.0";
    
    /**
     * 原始数据 (JSON格式，用于调试)
     */
    @Column(name = "raw_data", columnDefinition = "LONGTEXT")
    private String rawData;
    
    /**
     * 备注信息
     */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 获取智能体类型显示名称
     */
    public String getAgentTypeDisplayName() {
        switch (agentType) {
            case "TECHNICAL": return "技术分析师";
            case "FUNDAMENTAL": return "基本面分析师";
            case "SENTIMENT": return "情绪分析师";
            case "COORDINATOR": return "协调器";
            default: return agentType;
        }
    }
    
    /**
     * 获取分析类型显示名称
     */
    public String getAnalysisTypeDisplayName() {
        switch (analysisType) {
            case "TECHNICAL": return "技术分析";
            case "FUNDAMENTAL": return "基本面分析";
            case "SENTIMENT": return "情绪分析";
            default: return analysisType;
        }
    }
    
    /**
     * 获取推荐操作显示名称
     */
    public String getRecommendationDisplayName() {
        if (recommendation == null) return "未知";
        switch (recommendation) {
            case "BUY": return "买入";
            case "HOLD": return "持有";
            case "SELL": return "卖出";
            default: return recommendation;
        }
    }
    
    /**
     * 获取风险等级显示名称
     */
    public String getRiskLevelDisplayName() {
        if (riskLevel == null) return "未知";
        switch (riskLevel) {
            case "LOW": return "低风险";
            case "MEDIUM": return "中风险";
            case "HIGH": return "高风险";
            default: return riskLevel;
        }
    }
    
    /**
     * 计算上涨空间
     */
    public void calculateUpsidePotential() {
        if (targetPrice != null && currentPrice != null && 
            currentPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal upside = targetPrice.subtract(currentPrice)
                    .divide(currentPrice, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
            this.upsidePotential = upside;
        }
    }
    
    /**
     * 获取推荐操作数值 (用于计算)
     */
    public int getRecommendationValue() {
        if (recommendation == null) return 0;
        switch (recommendation) {
            case "BUY": return 1;
            case "HOLD": return 0;
            case "SELL": return -1;
            default: return 0;
        }
    }
    
    /**
     * 获取风险等级数值 (用于计算)
     */
    public int getRiskLevelValue() {
        if (riskLevel == null) return 2;
        switch (riskLevel) {
            case "LOW": return 1;
            case "MEDIUM": return 2;
            case "HIGH": return 3;
            default: return 2;
        }
    }
    
    /**
     * 判断是否为买入建议
     */
    public boolean isBuyRecommendation() {
        return "BUY".equals(recommendation);
    }
    
    /**
     * 判断是否为卖出建议
     */
    public boolean isSellRecommendation() {
        return "SELL".equals(recommendation);
    }
    
    /**
     * 判断是否为持有建议
     */
    public boolean isHoldRecommendation() {
        return "HOLD".equals(recommendation);
    }
    
    /**
     * 判断是否为高置信度分析
     */
    public boolean isHighConfidence() {
        return confidence != null && confidence.compareTo(new BigDecimal("80")) >= 0;
    }
    
    /**
     * 判断是否为低置信度分析
     */
    public boolean isLowConfidence() {
        return confidence != null && confidence.compareTo(new BigDecimal("50")) < 0;
    }
    
    /**
     * 获取置信度等级
     */
    public String getConfidenceLevel() {
        if (confidence == null) return "未知";
        
        if (confidence.compareTo(new BigDecimal("80")) >= 0) {
            return "高";
        } else if (confidence.compareTo(new BigDecimal("60")) >= 0) {
            return "中";
        } else {
            return "低";
        }
    }
    
    /**
     * 获取分析质量等级
     */
    public String getQualityLevel() {
        if (score == null) return "未知";
        
        if (score.compareTo(new BigDecimal("85")) >= 0) {
            return "优秀";
        } else if (score.compareTo(new BigDecimal("70")) >= 0) {
            return "良好";
        } else if (score.compareTo(new BigDecimal("60")) >= 0) {
            return "一般";
        } else {
            return "较差";
        }
    }
    
    /**
     * 获取处理时长描述
     */
    public String getDurationDescription() {
        if (durationMs == null) return "未知";
        
        if (durationMs < 1000) {
            return durationMs + "毫秒";
        } else if (durationMs < 60000) {
            return (durationMs / 1000) + "秒";
        } else {
            long seconds = durationMs / 1000;
            return (seconds / 60) + "分" + (seconds % 60) + "秒";
        }
    }
    
    /**
     * 设置技术分析结果
     */
    public void setTechnicalAnalysisResult(String analysisText, String recommendation, 
                                          BigDecimal confidence, String technicalIndicators,
                                          String supportLevels, String resistanceLevels) {
        this.analysisText = analysisText;
        this.recommendation = recommendation;
        this.confidence = confidence;
        this.technicalIndicators = technicalIndicators;
        this.supportLevels = supportLevels;
        this.resistanceLevels = resistanceLevels;
    }
    
    /**
     * 设置基本面分析结果
     */
    public void setFundamentalAnalysisResult(String analysisText, String recommendation,
                                            BigDecimal confidence, String fundamentalData,
                                            String financialRatios, String valuationAnalysis) {
        this.analysisText = analysisText;
        this.recommendation = recommendation;
        this.confidence = confidence;
        this.fundamentalData = fundamentalData;
        this.financialRatios = financialRatios;
        this.valuationAnalysis = valuationAnalysis;
    }
    
    /**
     * 设置情绪分析结果
     */
    public void setSentimentAnalysisResult(String analysisText, String recommendation,
                                         BigDecimal confidence, String sentimentData,
                                         String newsSentiment, String socialSentiment) {
        this.analysisText = analysisText;
        this.recommendation = recommendation;
        this.confidence = confidence;
        this.sentimentData = sentimentData;
        this.newsSentiment = newsSentiment;
        this.socialSentiment = socialSentiment;
    }
}