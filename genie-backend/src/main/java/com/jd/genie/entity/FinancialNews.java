package com.jd.genie.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 财经新闻实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "financial_news", indexes = {
    @Index(name = "idx_news_publish_time", columnList = "publishTime"),
    @Index(name = "idx_news_source", columnList = "source"),
    @Index(name = "idx_news_category", columnList = "category"),
    @Index(name = "idx_news_stock_codes", columnList = "relatedStockCodes"),
    @Index(name = "idx_news_sentiment", columnList = "sentimentScore"),
    @Index(name = "idx_news_importance", columnList = "importanceLevel")
})
public class FinancialNews extends BaseEntity {

    /**
     * 新闻标题
     */
    @Column(nullable = false, length = 500)
    private String title;

    /**
     * 新闻内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 新闻摘要
     */
    @Column(length = 1000)
    private String summary;

    /**
     * 新闻来源
     */
    @Column(nullable = false, length = 100)
    private String source;

    /**
     * 新闻原始URL
     */
    @Column(length = 500)
    private String originalUrl;

    /**
     * 发布时间
     */
    @Column(nullable = false)
    private LocalDateTime publishTime;

    /**
     * 新闻分类
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NewsCategory category;

    /**
     * 相关股票代码（逗号分隔）
     */
    @Column(length = 1000)
    private String relatedStockCodes;

    /**
     * 新闻标签（逗号分隔）
     */
    @Column(length = 500)
    private String tags;

    /**
     * 情感分析得分（-1到1，负数表示负面，正数表示正面）
     */
    @Column(precision = 3, scale = 2)
    private Double sentimentScore;

    /**
     * 重要性等级
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportanceLevel importanceLevel;

    /**
     * 阅读量
     */
    @Column
    private Long viewCount = 0L;

    /**
     * 点赞数
     */
    @Column
    private Long likeCount = 0L;

    /**
     * 评论数
     */
    @Column
    private Long commentCount = 0L;

    /**
     * 转发数
     */
    @Column
    private Long shareCount = 0L;

    /**
     * 新闻热度分数
     */
    @Column(precision = 10, scale = 2)
    private Double hotScore = 0.0;

    /**
     * 是否为热点新闻
     */
    @Column(nullable = false)
    private Boolean isHot = false;

    /**
     * 是否已处理（用于AI分析）
     */
    @Column(nullable = false)
    private Boolean processed = false;

    /**
     * AI分析结果
     */
    @Column(columnDefinition = "TEXT")
    private String aiAnalysis;

    /**
     * 关键词（JSON格式存储）
     */
    @Column(columnDefinition = "TEXT")
    private String keywords;

    /**
     * 新闻分类枚举
     */
    public enum NewsCategory {
        MARKET_NEWS("市场资讯"),
        COMPANY_NEWS("公司新闻"),
        POLICY_NEWS("政策新闻"),
        ECONOMIC_DATA("经济数据"),
        INDUSTRY_NEWS("行业新闻"),
        INTERNATIONAL_NEWS("国际新闻"),
        RESEARCH_REPORT("研究报告"),
        ANNOUNCEMENT("公告"),
        OTHER("其他");

        private final String description;

        NewsCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 重要性等级枚举
     */
    public enum ImportanceLevel {
        CRITICAL("重大", 5),
        HIGH("重要", 4),
        MEDIUM("一般", 3),
        LOW("较低", 2),
        MINIMAL("最低", 1);

        private final String description;
        private final int level;

        ImportanceLevel(String description, int level) {
            this.description = description;
            this.level = level;
        }

        public String getDescription() {
            return description;
        }

        public int getLevel() {
            return level;
        }
    }

    /**
     * 获取相关股票代码列表
     */
    public List<String> getRelatedStockCodeList() {
        if (relatedStockCodes == null || relatedStockCodes.trim().isEmpty()) {
            return List.of();
        }
        return List.of(relatedStockCodes.split(","));
    }

    /**
     * 设置相关股票代码列表
     */
    public void setRelatedStockCodeList(List<String> stockCodes) {
        if (stockCodes == null || stockCodes.isEmpty()) {
            this.relatedStockCodes = null;
        } else {
            this.relatedStockCodes = String.join(",", stockCodes);
        }
    }

    /**
     * 获取标签列表
     */
    public List<String> getTagList() {
        if (tags == null || tags.trim().isEmpty()) {
            return List.of();
        }
        return List.of(tags.split(","));
    }

    /**
     * 设置标签列表
     */
    public void setTagList(List<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            this.tags = null;
        } else {
            this.tags = String.join(",", tagList);
        }
    }

    /**
     * 计算新闻热度分数
     * 基于阅读量、点赞数、评论数、转发数、发布时间等因素
     */
    public void calculateHotScore() {
        double score = 0.0;
        
        // 基础分数：基于互动数据
        score += (viewCount != null ? viewCount : 0) * 0.1;
        score += (likeCount != null ? likeCount : 0) * 0.5;
        score += (commentCount != null ? commentCount : 0) * 1.0;
        score += (shareCount != null ? shareCount : 0) * 2.0;
        
        // 时间衰减：新闻越新分数越高
        if (publishTime != null) {
            long hoursAgo = java.time.Duration.between(publishTime, LocalDateTime.now()).toHours();
            double timeDecay = Math.max(0.1, 1.0 - (hoursAgo * 0.01)); // 每小时衰减1%
            score *= timeDecay;
        }
        
        // 重要性加权
        if (importanceLevel != null) {
            score *= (importanceLevel.getLevel() * 0.2 + 0.6); // 0.8-1.6倍加权
        }
        
        // 情感分析加权（绝对值越大影响越大）
        if (sentimentScore != null) {
            score *= (1.0 + Math.abs(sentimentScore) * 0.2);
        }
        
        this.hotScore = score;
        this.isHot = score > 100.0; // 热度分数超过100认为是热点新闻
    }

    /**
     * 判断是否为正面新闻
     */
    public boolean isPositiveNews() {
        return sentimentScore != null && sentimentScore > 0.1;
    }

    /**
     * 判断是否为负面新闻
     */
    public boolean isNegativeNews() {
        return sentimentScore != null && sentimentScore < -0.1;
    }

    /**
     * 判断是否为中性新闻
     */
    public boolean isNeutralNews() {
        return sentimentScore != null && Math.abs(sentimentScore) <= 0.1;
    }
}