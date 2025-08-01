package com.jd.genie.service;

import com.jd.genie.entity.FinancialNews;
import com.jd.genie.repository.FinancialNewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 财经新闻服务
 * 负责新闻数据的获取、处理、分析和管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialNewsService {

    private final FinancialNewsRepository newsRepository;
    private final RestTemplate restTemplate;

    // 股票代码正则表达式
    private static final Pattern STOCK_CODE_PATTERN = Pattern.compile("(\\d{6})\\.(SH|SZ)");
    
    // 情感分析关键词
    private static final Map<String, Double> SENTIMENT_KEYWORDS = Map.of(
        "上涨", 0.8, "涨停", 1.0, "利好", 0.7, "盈利", 0.6, "增长", 0.5,
        "下跌", -0.8, "跌停", -1.0, "利空", -0.7, "亏损", -0.6, "下滑", -0.5,
        "稳定", 0.1, "持平", 0.0, "震荡", 0.0
    );

    /**
     * 获取最新新闻列表
     */
    @Cacheable(value = "news", key = "'latest_' + #page + '_' + #size")
    public Page<FinancialNews> getLatestNews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepository.findAll(pageable);
    }

    /**
     * 获取热点新闻
     */
    @Cacheable(value = "news", key = "'hot_' + #page + '_' + #size")
    public Page<FinancialNews> getHotNews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepository.findHotNews(pageable);
    }

    /**
     * 根据股票代码获取相关新闻
     */
    @Cacheable(value = "news", key = "'stock_' + #stockCode + '_' + #page + '_' + #size")
    public Page<FinancialNews> getNewsByStockCode(String stockCode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepository.findByRelatedStockCode(stockCode, pageable);
    }

    /**
     * 根据分类获取新闻
     */
    @Cacheable(value = "news", key = "'category_' + #category + '_' + #page + '_' + #size")
    public Page<FinancialNews> getNewsByCategory(FinancialNews.NewsCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepository.findByCategoryOrderByPublishTimeDesc(category, pageable);
    }

    /**
     * 搜索新闻
     */
    public Page<FinancialNews> searchNews(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * 获取正面新闻
     */
    @Cacheable(value = "news", key = "'positive_' + #page + '_' + #size")
    public Page<FinancialNews> getPositiveNews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepository.findPositiveNews(pageable);
    }

    /**
     * 获取负面新闻
     */
    @Cacheable(value = "news", key = "'negative_' + #page + '_' + #size")
    public Page<FinancialNews> getNegativeNews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return newsRepository.findNegativeNews(pageable);
    }

    /**
     * 获取今日热点新闻
     */
    @Cacheable(value = "news", key = "'today_hot_' + #limit")
    public List<FinancialNews> getTodayHotNews(int limit) {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime tomorrowStart = todayStart.plusDays(1);
        Pageable pageable = PageRequest.of(0, limit);
        return newsRepository.findTodayHotNews(todayStart, tomorrowStart, pageable);
    }

    /**
     * 获取趋势新闻
     */
    @Cacheable(value = "news", key = "'trending_' + #limit")
    public List<FinancialNews> getTrendingNews(int limit) {
        LocalDateTime recentTime = LocalDateTime.now().minusHours(24); // 最近24小时
        Pageable pageable = PageRequest.of(0, limit);
        return newsRepository.findTrendingNews(recentTime, 10.0, pageable);
    }

    /**
     * 保存新闻
     */
    @Transactional
    public FinancialNews saveNews(FinancialNews news) {
        // 检查是否已存在相同新闻
        if (news.getTitle() != null && newsRepository.findByTitle(news.getTitle()).isPresent()) {
            log.debug("新闻已存在，跳过保存: {}", news.getTitle());
            return null;
        }
        
        if (news.getOriginalUrl() != null && newsRepository.findByOriginalUrl(news.getOriginalUrl()).isPresent()) {
            log.debug("新闻URL已存在，跳过保存: {}", news.getOriginalUrl());
            return null;
        }

        // 自动分析和处理新闻
        processNews(news);
        
        // 保存新闻
        FinancialNews savedNews = newsRepository.save(news);
        
        // 清除相关缓存
        clearNewsCache();
        
        log.info("保存新闻成功: {}", savedNews.getTitle());
        return savedNews;
    }

    /**
     * 批量保存新闻
     */
    @Transactional
    public List<FinancialNews> saveNewsBatch(List<FinancialNews> newsList) {
        List<FinancialNews> savedNews = new ArrayList<>();
        
        for (FinancialNews news : newsList) {
            FinancialNews saved = saveNews(news);
            if (saved != null) {
                savedNews.add(saved);
            }
        }
        
        log.info("批量保存新闻完成，成功保存 {} 条，跳过 {} 条", 
                savedNews.size(), newsList.size() - savedNews.size());
        
        return savedNews;
    }

    /**
     * 处理新闻（分析、分类、情感分析等）
     */
    private void processNews(FinancialNews news) {
        // 1. 提取相关股票代码
        extractStockCodes(news);
        
        // 2. 自动分类
        autoClassifyNews(news);
        
        // 3. 情感分析
        analyzeSentiment(news);
        
        // 4. 确定重要性等级
        determineImportanceLevel(news);
        
        // 5. 计算热度分数
        news.calculateHotScore();
        
        // 6. 提取关键词
        extractKeywords(news);
        
        // 7. 生成摘要（如果没有）
        if (news.getSummary() == null || news.getSummary().trim().isEmpty()) {
            generateSummary(news);
        }
    }

    /**
     * 从新闻内容中提取相关股票代码
     */
    private void extractStockCodes(FinancialNews news) {
        Set<String> stockCodes = new HashSet<>();
        String content = (news.getTitle() + " " + (news.getContent() != null ? news.getContent() : "")).toUpperCase();
        
        Matcher matcher = STOCK_CODE_PATTERN.matcher(content);
        while (matcher.find()) {
            stockCodes.add(matcher.group());
        }
        
        if (!stockCodes.isEmpty()) {
            news.setRelatedStockCodeList(new ArrayList<>(stockCodes));
        }
    }

    /**
     * 自动分类新闻
     */
    private void autoClassifyNews(FinancialNews news) {
        String content = (news.getTitle() + " " + (news.getContent() != null ? news.getContent() : "")).toLowerCase();
        
        if (content.contains("政策") || content.contains("监管") || content.contains("央行")) {
            news.setCategory(FinancialNews.NewsCategory.POLICY_NEWS);
        } else if (content.contains("财报") || content.contains("业绩") || content.contains("年报")) {
            news.setCategory(FinancialNews.NewsCategory.COMPANY_NEWS);
        } else if (content.contains("GDP") || content.contains("CPI") || content.contains("经济数据")) {
            news.setCategory(FinancialNews.NewsCategory.ECONOMIC_DATA);
        } else if (content.contains("行业") || content.contains("板块")) {
            news.setCategory(FinancialNews.NewsCategory.INDUSTRY_NEWS);
        } else if (content.contains("研报") || content.contains("分析师")) {
            news.setCategory(FinancialNews.NewsCategory.RESEARCH_REPORT);
        } else if (content.contains("公告") || content.contains("披露")) {
            news.setCategory(FinancialNews.NewsCategory.ANNOUNCEMENT);
        } else {
            news.setCategory(FinancialNews.NewsCategory.MARKET_NEWS);
        }
    }

    /**
     * 情感分析
     */
    private void analyzeSentiment(FinancialNews news) {
        String content = (news.getTitle() + " " + (news.getContent() != null ? news.getContent() : "")).toLowerCase();
        
        double sentimentScore = 0.0;
        int matchCount = 0;
        
        for (Map.Entry<String, Double> entry : SENTIMENT_KEYWORDS.entrySet()) {
            String keyword = entry.getKey();
            Double score = entry.getValue();
            
            int count = countOccurrences(content, keyword);
            if (count > 0) {
                sentimentScore += score * count;
                matchCount += count;
            }
        }
        
        if (matchCount > 0) {
            sentimentScore = sentimentScore / matchCount;
            // 限制在-1到1之间
            sentimentScore = Math.max(-1.0, Math.min(1.0, sentimentScore));
        }
        
        news.setSentimentScore(sentimentScore);
    }

    /**
     * 确定重要性等级
     */
    private void determineImportanceLevel(FinancialNews news) {
        String content = (news.getTitle() + " " + (news.getContent() != null ? news.getContent() : "")).toLowerCase();
        
        // 重大关键词
        String[] criticalKeywords = {"重大", "紧急", "突发", "暴跌", "暴涨", "停牌", "退市"};
        // 重要关键词
        String[] highKeywords = {"涨停", "跌停", "业绩", "财报", "并购", "重组"};
        // 一般关键词
        String[] mediumKeywords = {"上涨", "下跌", "公告", "分析"};
        
        if (containsAny(content, criticalKeywords)) {
            news.setImportanceLevel(FinancialNews.ImportanceLevel.CRITICAL);
        } else if (containsAny(content, highKeywords)) {
            news.setImportanceLevel(FinancialNews.ImportanceLevel.HIGH);
        } else if (containsAny(content, mediumKeywords)) {
            news.setImportanceLevel(FinancialNews.ImportanceLevel.MEDIUM);
        } else {
            news.setImportanceLevel(FinancialNews.ImportanceLevel.LOW);
        }
    }

    /**
     * 提取关键词
     */
    private void extractKeywords(FinancialNews news) {
        // 简单的关键词提取逻辑
        String content = news.getTitle();
        if (news.getContent() != null) {
            content += " " + news.getContent();
        }
        
        // 这里可以实现更复杂的关键词提取算法
        // 目前使用简单的方式
        List<String> keywords = Arrays.stream(content.split("[\\s\\p{Punct}]+"))
                .filter(word -> word.length() > 1)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
        
        news.setTagList(keywords);
    }

    /**
     * 生成摘要
     */
    private void generateSummary(FinancialNews news) {
        if (news.getContent() != null && news.getContent().length() > 100) {
            // 简单的摘要生成：取前100个字符
            String summary = news.getContent().substring(0, Math.min(100, news.getContent().length()));
            if (news.getContent().length() > 100) {
                summary += "...";
            }
            news.setSummary(summary);
        } else {
            news.setSummary(news.getTitle());
        }
    }

    /**
     * 增加新闻阅读量
     */
    @Transactional
    public void incrementViewCount(Long newsId) {
        newsRepository.incrementViewCount(newsId);
        // 重新计算热度分数
        updateHotScore(newsId);
    }

    /**
     * 增加新闻点赞数
     */
    @Transactional
    public void incrementLikeCount(Long newsId) {
        newsRepository.incrementLikeCount(newsId);
        // 重新计算热度分数
        updateHotScore(newsId);
    }

    /**
     * 更新新闻热度分数
     */
    @Transactional
    public void updateHotScore(Long newsId) {
        Optional<FinancialNews> newsOpt = newsRepository.findById(newsId);
        if (newsOpt.isPresent()) {
            FinancialNews news = newsOpt.get();
            news.calculateHotScore();
            newsRepository.updateHotScore(newsId, news.getHotScore(), news.getIsHot());
        }
    }

    /**
     * 获取新闻统计信息
     */
    @Cacheable(value = "news", key = "'statistics'")
    public NewsStatistics getNewsStatistics() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7); // 最近7天
        Object[] stats = newsRepository.getNewsStatistics(startTime);
        
        return NewsStatistics.builder()
                .totalCount(((Number) stats[0]).longValue())
                .hotCount(((Number) stats[1]).longValue())
                .processedCount(((Number) stats[2]).longValue())
                .avgSentiment(stats[3] != null ? ((Number) stats[3]).doubleValue() : 0.0)
                .maxHotScore(stats[4] != null ? ((Number) stats[4]).doubleValue() : 0.0)
                .build();
    }

    /**
     * 清除新闻缓存
     */
    @CacheEvict(value = "news", allEntries = true)
    public void clearNewsCache() {
        log.debug("清除新闻缓存");
    }

    /**
     * 删除过期新闻
     */
    @Transactional
    public void deleteOldNews(int daysToKeep) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
        newsRepository.deleteOldNews(cutoffTime);
        clearNewsCache();
        log.info("删除 {} 天前的新闻数据", daysToKeep);
    }

    /**
     * 异步处理未处理的新闻
     */
    public CompletableFuture<Void> processUnprocessedNews() {
        return CompletableFuture.runAsync(() -> {
            Pageable pageable = PageRequest.of(0, 100);
            Page<FinancialNews> unprocessedNews = newsRepository.findUnprocessedNews(pageable);
            
            for (FinancialNews news : unprocessedNews.getContent()) {
                try {
                    processNews(news);
                    newsRepository.updateProcessedStatus(news.getId(), true);
                } catch (Exception e) {
                    log.error("处理新闻失败: {}", news.getId(), e);
                }
            }
            
            log.info("处理未处理新闻完成，共处理 {} 条", unprocessedNews.getContent().size());
        });
    }

    // 辅助方法
    private int countOccurrences(String text, String keyword) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(keyword, index)) != -1) {
            count++;
            index += keyword.length();
        }
        return count;
    }

    private boolean containsAny(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 新闻统计信息
     */
    public static class NewsStatistics {
        private Long totalCount;
        private Long hotCount;
        private Long processedCount;
        private Double avgSentiment;
        private Double maxHotScore;

        public static NewsStatisticsBuilder builder() {
            return new NewsStatisticsBuilder();
        }

        // Builder pattern implementation
        public static class NewsStatisticsBuilder {
            private Long totalCount;
            private Long hotCount;
            private Long processedCount;
            private Double avgSentiment;
            private Double maxHotScore;

            public NewsStatisticsBuilder totalCount(Long totalCount) {
                this.totalCount = totalCount;
                return this;
            }

            public NewsStatisticsBuilder hotCount(Long hotCount) {
                this.hotCount = hotCount;
                return this;
            }

            public NewsStatisticsBuilder processedCount(Long processedCount) {
                this.processedCount = processedCount;
                return this;
            }

            public NewsStatisticsBuilder avgSentiment(Double avgSentiment) {
                this.avgSentiment = avgSentiment;
                return this;
            }

            public NewsStatisticsBuilder maxHotScore(Double maxHotScore) {
                this.maxHotScore = maxHotScore;
                return this;
            }

            public NewsStatistics build() {
                NewsStatistics statistics = new NewsStatistics();
                statistics.totalCount = this.totalCount;
                statistics.hotCount = this.hotCount;
                statistics.processedCount = this.processedCount;
                statistics.avgSentiment = this.avgSentiment;
                statistics.maxHotScore = this.maxHotScore;
                return statistics;
            }
        }

        // Getters
        public Long getTotalCount() { return totalCount; }
        public Long getHotCount() { return hotCount; }
        public Long getProcessedCount() { return processedCount; }
        public Double getAvgSentiment() { return avgSentiment; }
        public Double getMaxHotScore() { return maxHotScore; }
    }
}