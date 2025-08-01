package com.jd.genie.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Go-Stock新闻数据获取能力迁移服务
 * 将Go项目中的新闻爬取和情绪分析能力迁移到Java Spring Boot
 * 
 * 支持的新闻源：
 * - 财联社 (实时财经新闻)
 * - 新浪财经 (股票新闻)
 * - 腾讯财经 (市场资讯)
 * - 雪球 (社交媒体情绪)
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Service
public class NewsDataMigrationService {
    
    private final OkHttpClient httpClient;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 新闻源URL常量
    private static final String CLS_NEWS_URL = "https://www.cls.cn/api/sw?app=CailianpressWeb&os=web&sv=7.7.5";
    private static final String SINA_NEWS_URL = "https://finance.sina.com.cn/stock/";
    private static final String TENCENT_NEWS_URL = "https://stock.qq.com/";
    private static final String XUEQIU_API_URL = "https://xueqiu.com/statuses/stock_timeline.json";
    
    // 缓存键前缀
    private static final String CACHE_PREFIX_NEWS = "news:";
    private static final String CACHE_PREFIX_SENTIMENT = "sentiment:";
    
    // 情绪分析关键词
    private static final Map<String, Integer> POSITIVE_KEYWORDS = Map.of(
        "利好", 3, "上涨", 2, "增长", 2, "盈利", 2, "突破", 2,
        "买入", 3, "推荐", 2, "看好", 2, "强势", 2, "创新高", 3
    );
    
    private static final Map<String, Integer> NEGATIVE_KEYWORDS = Map.of(
        "利空", -3, "下跌", -2, "亏损", -2, "风险", -2, "跌破", -2,
        "卖出", -3, "看空", -2, "弱势", -2, "创新低", -3, "暴跌", -3
    );
    
    @Autowired
    public NewsDataMigrationService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                            .header("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
                            .header("Accept-Encoding", "gzip, deflate")
                            .header("Connection", "keep-alive")
                            .build();
                    return chain.proceed(request);
                })
                .build();
    }
    
    /**
     * 获取财联社实时新闻
     * 迁移自Go项目的财联社新闻爬取功能
     * 
     * @param limit 新闻数量限制
     * @return 新闻列表
     */
    public CompletableFuture<List<NewsItem>> getCLSNews(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String cacheKey = CACHE_PREFIX_NEWS + "cls:" + limit;
                
                // 检查缓存
                @SuppressWarnings("unchecked")
                List<NewsItem> cachedNews = (List<NewsItem>) redisTemplate.opsForValue().get(cacheKey);
                if (cachedNews != null) {
                    return cachedNews;
                }
                
                List<NewsItem> newsList = fetchCLSNews(limit);
                if (!newsList.isEmpty()) {
                    // 缓存5分钟
                    redisTemplate.opsForValue().set(cacheKey, newsList, Duration.ofMinutes(5));
                }
                
                return newsList;
            } catch (Exception e) {
                log.error("获取财联社新闻失败", e);
                return Collections.emptyList();
            }
        });
    }
    
    /**
     * 获取特定股票相关新闻
     * 迁移自Go项目的股票新闻搜索功能
     * 
     * @param stockCode 股票代码
     * @param stockName 股票名称
     * @param limit 新闻数量限制
     * @return 相关新闻列表
     */
    public CompletableFuture<List<NewsItem>> getStockNews(String stockCode, String stockName, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String cacheKey = CACHE_PREFIX_NEWS + "stock:" + stockCode + ":" + limit;
                
                // 检查缓存
                @SuppressWarnings("unchecked")
                List<NewsItem> cachedNews = (List<NewsItem>) redisTemplate.opsForValue().get(cacheKey);
                if (cachedNews != null) {
                    return cachedNews;
                }
                
                List<NewsItem> newsList = new ArrayList<>();
                
                // 从多个源获取新闻
                newsList.addAll(fetchSinaStockNews(stockCode, stockName, limit / 2));
                newsList.addAll(fetchTencentStockNews(stockCode, stockName, limit / 2));
                
                // 按时间排序
                newsList.sort((a, b) -> b.getPublishTime().compareTo(a.getPublishTime()));
                
                // 限制数量
                if (newsList.size() > limit) {
                    newsList = newsList.subList(0, limit);
                }
                
                if (!newsList.isEmpty()) {
                    // 缓存10分钟
                    redisTemplate.opsForValue().set(cacheKey, newsList, Duration.ofMinutes(10));
                }
                
                return newsList;
            } catch (Exception e) {
                log.error("获取股票新闻失败: {}", stockCode, e);
                return Collections.emptyList();
            }
        });
    }
    
    /**
     * 分析新闻情绪
     * 迁移自Go项目的stock_sentiment_analysis.go
     * 
     * @param newsItems 新闻列表
     * @return 情绪分析结果
     */
    public CompletableFuture<SentimentAnalysisResult> analyzeNewsSentiment(List<NewsItem> newsItems) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (newsItems.isEmpty()) {
                    return SentimentAnalysisResult.builder()
                            .overallSentiment(0.0)
                            .positiveCount(0)
                            .negativeCount(0)
                            .neutralCount(0)
                            .confidence(0.0)
                            .build();
                }
                
                int positiveCount = 0;
                int negativeCount = 0;
                int neutralCount = 0;
                double totalScore = 0.0;
                
                for (NewsItem news : newsItems) {
                    double sentimentScore = calculateSentimentScore(news.getTitle() + " " + news.getContent());
                    totalScore += sentimentScore;
                    
                    if (sentimentScore > 0.5) {
                        positiveCount++;
                    } else if (sentimentScore < -0.5) {
                        negativeCount++;
                    } else {
                        neutralCount++;
                    }
                }
                
                double overallSentiment = totalScore / newsItems.size();
                double confidence = calculateConfidence(positiveCount, negativeCount, neutralCount);
                
                return SentimentAnalysisResult.builder()
                        .overallSentiment(overallSentiment)
                        .positiveCount(positiveCount)
                        .negativeCount(negativeCount)
                        .neutralCount(neutralCount)
                        .confidence(confidence)
                        .totalNews(newsItems.size())
                        .analysisTime(LocalDateTime.now())
                        .build();
                
            } catch (Exception e) {
                log.error("新闻情绪分析失败", e);
                return SentimentAnalysisResult.builder()
                        .overallSentiment(0.0)
                        .confidence(0.0)
                        .build();
            }
        });
    }
    
    /**
     * 获取市场热点新闻
     * 迁移自Go项目的热点新闻功能
     * 
     * @return 热点新闻列表
     */
    public CompletableFuture<List<NewsItem>> getHotNews() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String cacheKey = CACHE_PREFIX_NEWS + "hot";
                
                // 检查缓存
                @SuppressWarnings("unchecked")
                List<NewsItem> cachedNews = (List<NewsItem>) redisTemplate.opsForValue().get(cacheKey);
                if (cachedNews != null) {
                    return cachedNews;
                }
                
                List<NewsItem> hotNews = new ArrayList<>();
                
                // 从财联社获取热点新闻
                hotNews.addAll(fetchCLSNews(20));
                
                // 按热度排序（这里简化为按时间排序）
                hotNews.sort((a, b) -> b.getPublishTime().compareTo(a.getPublishTime()));
                
                // 取前10条
                if (hotNews.size() > 10) {
                    hotNews = hotNews.subList(0, 10);
                }
                
                if (!hotNews.isEmpty()) {
                    // 缓存15分钟
                    redisTemplate.opsForValue().set(cacheKey, hotNews, Duration.ofMinutes(15));
                }
                
                return hotNews;
            } catch (Exception e) {
                log.error("获取热点新闻失败", e);
                return Collections.emptyList();
            }
        });
    }
    
    // 私有方法实现
    
    /**
     * 从财联社获取新闻
     */
    private List<NewsItem> fetchCLSNews(int limit) {
        List<NewsItem> newsList = new ArrayList<>();
        try {
            Request request = new Request.Builder()
                    .url(CLS_NEWS_URL)
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = JSON.parseObject(responseBody);
                    
                    if (jsonResponse.containsKey("data")) {
                        JSONArray newsArray = jsonResponse.getJSONObject("data").getJSONArray("roll_data");
                        
                        for (int i = 0; i < Math.min(newsArray.size(), limit); i++) {
                            JSONObject newsJson = newsArray.getJSONObject(i);
                            NewsItem newsItem = parseNewsFromCLS(newsJson);
                            if (newsItem != null) {
                                newsList.add(newsItem);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取财联社新闻失败", e);
        }
        return newsList;
    }
    
    /**
     * 从新浪财经获取股票新闻
     */
    private List<NewsItem> fetchSinaStockNews(String stockCode, String stockName, int limit) {
        List<NewsItem> newsList = new ArrayList<>();
        try {
            String url = SINA_NEWS_URL + stockCode + "/";
            Request request = new Request.Builder().url(url).build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String html = response.body().string();
                    Document doc = Jsoup.parse(html);
                    
                    Elements newsElements = doc.select(".news-item");
                    for (int i = 0; i < Math.min(newsElements.size(), limit); i++) {
                        Element element = newsElements.get(i);
                        NewsItem newsItem = parseNewsFromSina(element, stockCode);
                        if (newsItem != null) {
                            newsList.add(newsItem);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取新浪股票新闻失败: {}", stockCode, e);
        }
        return newsList;
    }
    
    /**
     * 从腾讯财经获取股票新闻
     */
    private List<NewsItem> fetchTencentStockNews(String stockCode, String stockName, int limit) {
        List<NewsItem> newsList = new ArrayList<>();
        try {
            String url = TENCENT_NEWS_URL + "search?q=" + stockName;
            Request request = new Request.Builder().url(url).build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String html = response.body().string();
                    Document doc = Jsoup.parse(html);
                    
                    Elements newsElements = doc.select(".news-list li");
                    for (int i = 0; i < Math.min(newsElements.size(), limit); i++) {
                        Element element = newsElements.get(i);
                        NewsItem newsItem = parseNewsFromTencent(element, stockCode);
                        if (newsItem != null) {
                            newsList.add(newsItem);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取腾讯股票新闻失败: {}", stockCode, e);
        }
        return newsList;
    }
    
    /**
     * 解析财联社新闻
     */
    private NewsItem parseNewsFromCLS(JSONObject newsJson) {
        try {
            return NewsItem.builder()
                    .id(newsJson.getString("id"))
                    .title(newsJson.getString("title"))
                    .content(newsJson.getString("content"))
                    .source("财联社")
                    .url(newsJson.getString("url"))
                    .publishTime(parseTimestamp(newsJson.getLong("ctime")))
                    .build();
        } catch (Exception e) {
            log.error("解析财联社新闻失败", e);
            return null;
        }
    }
    
    /**
     * 解析新浪新闻
     */
    private NewsItem parseNewsFromSina(Element element, String stockCode) {
        try {
            String title = element.select(".news-title").text();
            String url = element.select("a").attr("href");
            String timeStr = element.select(".news-time").text();
            
            return NewsItem.builder()
                    .id(generateNewsId(url))
                    .title(title)
                    .content("")
                    .source("新浪财经")
                    .url(url)
                    .stockCode(stockCode)
                    .publishTime(parseTimeString(timeStr))
                    .build();
        } catch (Exception e) {
            log.error("解析新浪新闻失败", e);
            return null;
        }
    }
    
    /**
     * 解析腾讯新闻
     */
    private NewsItem parseNewsFromTencent(Element element, String stockCode) {
        try {
            String title = element.select(".news-title").text();
            String url = element.select("a").attr("href");
            String timeStr = element.select(".news-time").text();
            
            return NewsItem.builder()
                    .id(generateNewsId(url))
                    .title(title)
                    .content("")
                    .source("腾讯财经")
                    .url(url)
                    .stockCode(stockCode)
                    .publishTime(parseTimeString(timeStr))
                    .build();
        } catch (Exception e) {
            log.error("解析腾讯新闻失败", e);
            return null;
        }
    }
    
    /**
     * 计算情绪分数
     * 迁移自Go项目的情绪分析算法
     */
    private double calculateSentimentScore(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        
        double score = 0.0;
        int matchCount = 0;
        
        // 正面情绪词汇
        for (Map.Entry<String, Integer> entry : POSITIVE_KEYWORDS.entrySet()) {
            String keyword = entry.getKey();
            int weight = entry.getValue();
            int count = countOccurrences(text, keyword);
            if (count > 0) {
                score += count * weight;
                matchCount += count;
            }
        }
        
        // 负面情绪词汇
        for (Map.Entry<String, Integer> entry : NEGATIVE_KEYWORDS.entrySet()) {
            String keyword = entry.getKey();
            int weight = entry.getValue();
            int count = countOccurrences(text, keyword);
            if (count > 0) {
                score += count * weight;
                matchCount += count;
            }
        }
        
        // 归一化到[-1, 1]区间
        if (matchCount > 0) {
            score = score / (matchCount * 3.0); // 3是最大权重
            return Math.max(-1.0, Math.min(1.0, score));
        }
        
        return 0.0;
    }
    
    /**
     * 计算置信度
     */
    private double calculateConfidence(int positiveCount, int negativeCount, int neutralCount) {
        int totalCount = positiveCount + negativeCount + neutralCount;
        if (totalCount == 0) {
            return 0.0;
        }
        
        int maxCount = Math.max(positiveCount, Math.max(negativeCount, neutralCount));
        return (double) maxCount / totalCount;
    }
    
    // 工具方法
    
    /**
     * 统计关键词出现次数
     */
    private int countOccurrences(String text, String keyword) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(keyword, index)) != -1) {
            count++;
            index += keyword.length();
        }
        return count;
    }
    
    /**
     * 解析时间戳
     */
    private LocalDateTime parseTimestamp(Long timestamp) {
        if (timestamp == null) {
            return LocalDateTime.now();
        }
        return LocalDateTime.ofEpochSecond(timestamp, 0, java.time.ZoneOffset.ofHours(8));
    }
    
    /**
     * 解析时间字符串
     */
    private LocalDateTime parseTimeString(String timeStr) {
        try {
            if (timeStr.contains("今天")) {
                return LocalDateTime.now();
            } else if (timeStr.contains("昨天")) {
                return LocalDateTime.now().minusDays(1);
            } else {
                // 尝试解析具体时间格式
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                return LocalDateTime.parse(timeStr, formatter);
            }
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
    
    /**
     * 生成新闻ID
     */
    private String generateNewsId(String url) {
        return String.valueOf(url.hashCode());
    }
    
    // 数据模型类
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NewsItem {
        private String id;
        private String title;
        private String content;
        private String source;
        private String url;
        private String stockCode;
        private LocalDateTime publishTime;
        private Double sentimentScore;
        private String category;
        private List<String> tags;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SentimentAnalysisResult {
        private Double overallSentiment;  // 总体情绪 [-1, 1]
        private Integer positiveCount;    // 正面新闻数量
        private Integer negativeCount;    // 负面新闻数量
        private Integer neutralCount;     // 中性新闻数量
        private Integer totalNews;        // 总新闻数量
        private Double confidence;        // 置信度 [0, 1]
        private LocalDateTime analysisTime; // 分析时间
        private Map<String, Integer> keywordFrequency; // 关键词频率
    }
}