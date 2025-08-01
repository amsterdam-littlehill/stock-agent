package com.jd.genie.agent.agent.stock;

import com.jd.genie.agent.agent.BaseAgent;
import com.jd.genie.agent.enums.AgentType;
import com.jd.genie.service.NewsDataMigrationService;
import com.jd.genie.service.NewsDataMigrationService.NewsItem;
import com.jd.genie.service.NewsDataMigrationService.SentimentAnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 情绪分析师智能体
 * 基于TradingAgents框架的专业情绪分析师角色
 * 
 * 职责：
 * - 新闻情绪分析
 * - 市场情绪监测
 * - 舆情风险评估
 * - 情绪驱动因子识别
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component("sentiment_analyst")
public class SentimentAnalyst extends BaseAgent {
    
    @Autowired
    private NewsDataMigrationService newsDataService;
    
    public SentimentAnalyst() {
        super();
        this.agentType = AgentType.ANALYSIS;
        this.agentName = "情绪分析师";
        this.description = "专业的情绪分析师，擅长新闻情绪、市场情绪和舆情风险分析";
    }
    
    /**
     * 执行情绪分析
     * 
     * @param stockCode 股票代码
     * @param context 分析上下文
     * @return 分析结果
     */
    public CompletableFuture<SentimentAnalysisResult> analyze(String stockCode, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("情绪分析师开始分析股票: {}", stockCode);
                
                // 1. 获取相关新闻数据
                List<NewsItem> stockNews = newsDataService.getStockRelatedNews(stockCode, 50).get();
                List<NewsItem> marketNews = newsDataService.getMarketHotNews(20).get();
                
                if (stockNews.isEmpty() && marketNews.isEmpty()) {
                    return createErrorResult("无法获取新闻数据");
                }
                
                // 2. 新闻情绪分析
                NewsSentimentAnalysis newsSentiment = analyzeNewsSentiment(stockNews);
                
                // 3. 市场情绪分析
                MarketSentimentAnalysis marketSentiment = analyzeMarketSentiment(marketNews, stockCode);
                
                // 4. 舆情风险评估
                RiskAssessment riskAssessment = assessSentimentRisk(newsSentiment, marketSentiment);
                
                // 5. 情绪驱动因子识别
                List<SentimentDriver> sentimentDrivers = identifySentimentDrivers(stockNews, marketNews);
                
                // 6. 时间序列情绪分析
                SentimentTrend sentimentTrend = analyzeSentimentTrend(stockNews);
                
                // 7. 生成LLM分析
                String llmAnalysis = generateLLMAnalysis(stockCode, newsSentiment, marketSentiment, 
                                                       riskAssessment, sentimentDrivers, sentimentTrend);
                
                // 8. 计算情绪评分
                double sentimentScore = calculateSentimentScore(newsSentiment, marketSentiment, riskAssessment);
                
                // 9. 生成投资建议
                String recommendation = generateRecommendation(sentimentScore, riskAssessment);
                
                return SentimentAnalysisResult.builder()
                        .stockCode(stockCode)
                        .agentId("sentiment_analyst")
                        .agentName("情绪分析师")
                        .analysis(llmAnalysis)
                        .newsSentiment(newsSentiment)
                        .marketSentiment(marketSentiment)
                        .riskAssessment(riskAssessment)
                        .sentimentDrivers(sentimentDrivers)
                        .sentimentTrend(sentimentTrend)
                        .sentimentScore(sentimentScore)
                        .recommendation(recommendation)
                        .confidence(calculateConfidence(newsSentiment, marketSentiment))
                        .timestamp(System.currentTimeMillis())
                        .build();
                
            } catch (Exception e) {
                log.error("情绪分析失败: {}", stockCode, e);
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
            
            // 获取情绪分析结果
            SentimentAnalysisResult analysisResult = (SentimentAnalysisResult) context.getAgentResult("sentiment_analyst");
            
            String prompt = buildDebatePrompt(currentRound, analysisResult, previousArguments);
            
            // 调用LLM生成辩论论据
            String argument = callLLM(prompt);
            
            return FundamentalAnalyst.DebateArgument.builder()
                    .agentId("sentiment_analyst")
                    .agentName("情绪分析师")
                    .round(currentRound)
                    .argument(argument)
                    .confidence(analysisResult.getConfidence())
                    .evidenceType("SENTIMENT")
                    .supportingData(Map.of(
                        "newsSentiment", analysisResult.getNewsSentiment(),
                        "marketSentiment", analysisResult.getMarketSentiment(),
                        "riskAssessment", analysisResult.getRiskAssessment(),
                        "sentimentScore", analysisResult.getSentimentScore()
                    ))
                    .timestamp(System.currentTimeMillis())
                    .build();
                    
        } catch (Exception e) {
            log.error("情绪分析师辩论失败", e);
            return createErrorDebateArgument("辩论过程中发生错误");
        }
    }
    
    /**
     * 新闻情绪分析
     */
    private NewsSentimentAnalysis analyzeNewsSentiment(List<NewsItem> stockNews) {
        if (stockNews.isEmpty()) {
            return NewsSentimentAnalysis.builder()
                    .overallSentiment("中性")
                    .positiveCount(0)
                    .negativeCount(0)
                    .neutralCount(0)
                    .sentimentScore(0.0)
                    .keyPositiveNews(new ArrayList<>())
                    .keyNegativeNews(new ArrayList<>())
                    .build();
        }
        
        int positiveCount = 0, negativeCount = 0, neutralCount = 0;
        double totalSentimentScore = 0.0;
        List<NewsItem> keyPositiveNews = new ArrayList<>();
        List<NewsItem> keyNegativeNews = new ArrayList<>();
        
        for (NewsItem news : stockNews) {
            try {
                // 调用新闻情绪分析服务
                NewsDataMigrationService.SentimentAnalysisResult sentiment = 
                    newsDataService.analyzeNewsSentiment(news.getTitle() + " " + news.getContent()).get();
                
                double score = sentiment.getSentimentScore();
                totalSentimentScore += score;
                
                if (score > 0.1) {
                    positiveCount++;
                    if (score > 0.5) {
                        keyPositiveNews.add(news);
                    }
                } else if (score < -0.1) {
                    negativeCount++;
                    if (score < -0.5) {
                        keyNegativeNews.add(news);
                    }
                } else {
                    neutralCount++;
                }
                
            } catch (Exception e) {
                log.warn("新闻情绪分析失败: {}", news.getTitle(), e);
                neutralCount++;
            }
        }
        
        double avgSentimentScore = stockNews.size() > 0 ? totalSentimentScore / stockNews.size() : 0.0;
        String overallSentiment = determineOverallSentiment(avgSentimentScore, positiveCount, negativeCount, neutralCount);
        
        // 限制关键新闻数量
        keyPositiveNews = keyPositiveNews.stream().limit(5).collect(Collectors.toList());
        keyNegativeNews = keyNegativeNews.stream().limit(5).collect(Collectors.toList());
        
        return NewsSentimentAnalysis.builder()
                .overallSentiment(overallSentiment)
                .positiveCount(positiveCount)
                .negativeCount(negativeCount)
                .neutralCount(neutralCount)
                .sentimentScore(avgSentimentScore)
                .keyPositiveNews(keyPositiveNews)
                .keyNegativeNews(keyNegativeNews)
                .newsVolume(stockNews.size())
                .build();
    }
    
    /**
     * 市场情绪分析
     */
    private MarketSentimentAnalysis analyzeMarketSentiment(List<NewsItem> marketNews, String stockCode) {
        // 分析市场整体情绪
        Map<String, Integer> sentimentDistribution = new HashMap<>();
        sentimentDistribution.put("乐观", 0);
        sentimentDistribution.put("悲观", 0);
        sentimentDistribution.put("中性", 0);
        
        double totalMarketScore = 0.0;
        List<String> marketThemes = new ArrayList<>();
        
        for (NewsItem news : marketNews) {
            try {
                // 简化的市场情绪分析
                String sentiment = analyzeMarketNewsSentiment(news);
                sentimentDistribution.put(sentiment, sentimentDistribution.get(sentiment) + 1);
                
                // 提取市场主题
                List<String> themes = extractMarketThemes(news);
                marketThemes.addAll(themes);
                
                // 计算市场情绪分数
                double score = calculateMarketSentimentScore(news, sentiment);
                totalMarketScore += score;
                
            } catch (Exception e) {
                log.warn("市场情绪分析失败: {}", news.getTitle(), e);
                sentimentDistribution.put("中性", sentimentDistribution.get("中性") + 1);
            }
        }
        
        double avgMarketScore = marketNews.size() > 0 ? totalMarketScore / marketNews.size() : 0.0;
        String marketMood = determineMarketMood(sentimentDistribution, avgMarketScore);
        
        // 统计主题频次
        Map<String, Long> themeFrequency = marketThemes.stream()
                .collect(Collectors.groupingBy(theme -> theme, Collectors.counting()));
        
        List<String> hotThemes = themeFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        return MarketSentimentAnalysis.builder()
                .marketMood(marketMood)
                .sentimentDistribution(sentimentDistribution)
                .marketScore(avgMarketScore)
                .hotThemes(hotThemes)
                .marketVolatility(calculateMarketVolatility(sentimentDistribution))
                .build();
    }
    
    /**
     * 舆情风险评估
     */
    private RiskAssessment assessSentimentRisk(NewsSentimentAnalysis newsSentiment, MarketSentimentAnalysis marketSentiment) {
        double riskScore = 0.0;
        List<String> riskFactors = new ArrayList<>();
        
        // 新闻情绪风险
        if (newsSentiment.getNegativeCount() > newsSentiment.getPositiveCount() * 2) {
            riskScore += 0.3;
            riskFactors.add("负面新闻过多");
        }
        
        if (newsSentiment.getSentimentScore() < -0.3) {
            riskScore += 0.2;
            riskFactors.add("新闻情绪极度悲观");
        }
        
        // 市场情绪风险
        if ("悲观".equals(marketSentiment.getMarketMood())) {
            riskScore += 0.2;
            riskFactors.add("市场情绪悲观");
        }
        
        if (marketSentiment.getMarketVolatility() > 0.7) {
            riskScore += 0.15;
            riskFactors.add("市场情绪波动剧烈");
        }
        
        // 新闻量风险
        if (newsSentiment.getNewsVolume() > 100) {
            riskScore += 0.1;
            riskFactors.add("新闻关注度异常高");
        }
        
        // 热点主题风险
        List<String> riskThemes = Arrays.asList("监管", "调查", "违规", "亏损", "退市", "诉讼");
        for (String theme : marketSentiment.getHotThemes()) {
            if (riskThemes.stream().anyMatch(theme::contains)) {
                riskScore += 0.05;
                riskFactors.add("涉及风险主题: " + theme);
            }
        }
        
        String riskLevel = determineRiskLevel(riskScore);
        
        return RiskAssessment.builder()
                .riskLevel(riskLevel)
                .riskScore(Math.min(riskScore, 1.0))
                .riskFactors(riskFactors)
                .recommendation(generateRiskRecommendation(riskLevel, riskFactors))
                .build();
    }
    
    /**
     * 识别情绪驱动因子
     */
    private List<SentimentDriver> identifySentimentDrivers(List<NewsItem> stockNews, List<NewsItem> marketNews) {
        List<SentimentDriver> drivers = new ArrayList<>();
        
        // 分析股票相关驱动因子
        Map<String, Integer> stockKeywords = extractKeywords(stockNews);
        for (Map.Entry<String, Integer> entry : stockKeywords.entrySet()) {
            if (entry.getValue() >= 3) { // 出现频次阈值
                drivers.add(SentimentDriver.builder()
                        .driverType("股票相关")
                        .description(entry.getKey())
                        .impact(calculateKeywordImpact(entry.getKey(), stockNews))
                        .frequency(entry.getValue())
                        .build());
            }
        }
        
        // 分析市场驱动因子
        Map<String, Integer> marketKeywords = extractKeywords(marketNews);
        for (Map.Entry<String, Integer> entry : marketKeywords.entrySet()) {
            if (entry.getValue() >= 2) {
                drivers.add(SentimentDriver.builder()
                        .driverType("市场相关")
                        .description(entry.getKey())
                        .impact(calculateKeywordImpact(entry.getKey(), marketNews))
                        .frequency(entry.getValue())
                        .build());
            }
        }
        
        // 按影响力排序并限制数量
        return drivers.stream()
                .sorted(Comparator.comparing(SentimentDriver::getImpact).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
    
    /**
     * 情绪趋势分析
     */
    private SentimentTrend analyzeSentimentTrend(List<NewsItem> stockNews) {
        // 按时间分组分析情绪变化
        Map<String, List<NewsItem>> dailyNews = stockNews.stream()
                .collect(Collectors.groupingBy(news -> news.getPublishTime().substring(0, 10))); // 简化：按日期分组
        
        List<DailySentiment> dailySentiments = new ArrayList<>();
        
        for (Map.Entry<String, List<NewsItem>> entry : dailyNews.entrySet()) {
            double dailyScore = entry.getValue().stream()
                    .mapToDouble(news -> {
                        try {
                            return newsDataService.analyzeNewsSentiment(news.getTitle()).get().getSentimentScore();
                        } catch (Exception e) {
                            return 0.0;
                        }
                    })
                    .average()
                    .orElse(0.0);
            
            dailySentiments.add(DailySentiment.builder()
                    .date(entry.getKey())
                    .sentimentScore(dailyScore)
                    .newsCount(entry.getValue().size())
                    .build());
        }
        
        // 按日期排序
        dailySentiments.sort(Comparator.comparing(DailySentiment::getDate));
        
        // 计算趋势
        String trend = calculateSentimentTrend(dailySentiments);
        double volatility = calculateSentimentVolatility(dailySentiments);
        
        return SentimentTrend.builder()
                .trend(trend)
                .volatility(volatility)
                .dailySentiments(dailySentiments)
                .build();
    }
    
    /**
     * 生成LLM分析
     */
    private String generateLLMAnalysis(String stockCode, NewsSentimentAnalysis newsSentiment, 
                                     MarketSentimentAnalysis marketSentiment, RiskAssessment riskAssessment,
                                     List<SentimentDriver> sentimentDrivers, SentimentTrend sentimentTrend) {
        
        String prompt = String.format("""
            作为专业的情绪分析师，请基于以下情绪数据对股票 %s 进行深度分析：
            
            新闻情绪分析：
            - 整体情绪：%s
            - 情绪评分：%.2f
            - 正面新闻：%d条，负面新闻：%d条，中性新闻：%d条
            - 新闻总量：%d条
            
            市场情绪分析：
            - 市场情绪：%s
            - 市场评分：%.2f
            - 情绪分布：乐观%d条，悲观%d条，中性%d条
            - 热门主题：%s
            - 市场波动性：%.2f
            
            风险评估：
            - 风险等级：%s
            - 风险评分：%.2f
            - 主要风险因子：%s
            
            情绪驱动因子：
            %s
            
            情绪趋势：
            - 趋势方向：%s
            - 波动性：%.2f
            
            请从以下角度进行分析：
            1. 当前情绪面对股价的影响
            2. 关键情绪驱动因子解读
            3. 舆情风险评估和预警
            4. 情绪趋势的延续性判断
            5. 基于情绪面的投资策略建议
            
            请提供专业、客观的情绪分析意见，字数控制在500字以内。
            """, 
            stockCode,
            newsSentiment.getOverallSentiment(),
            newsSentiment.getSentimentScore(),
            newsSentiment.getPositiveCount(),
            newsSentiment.getNegativeCount(),
            newsSentiment.getNeutralCount(),
            newsSentiment.getNewsVolume(),
            marketSentiment.getMarketMood(),
            marketSentiment.getMarketScore(),
            marketSentiment.getSentimentDistribution().get("乐观"),
            marketSentiment.getSentimentDistribution().get("悲观"),
            marketSentiment.getSentimentDistribution().get("中性"),
            String.join(", ", marketSentiment.getHotThemes()),
            marketSentiment.getMarketVolatility(),
            riskAssessment.getRiskLevel(),
            riskAssessment.getRiskScore(),
            String.join(", ", riskAssessment.getRiskFactors()),
            sentimentDrivers.stream()
                    .map(driver -> String.format("- %s: %s (影响力%.2f)", 
                            driver.getDriverType(), driver.getDescription(), driver.getImpact()))
                    .collect(Collectors.joining("\n")),
            sentimentTrend.getTrend(),
            sentimentTrend.getVolatility()
        );
        
        return callLLM(prompt);
    }
    
    /**
     * 构建辩论提示词
     */
    private String buildDebatePrompt(int round, SentimentAnalysisResult analysisResult, 
                                   List<FundamentalAnalyst.DebateArgument> previousArguments) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append(String.format("""
            作为情绪分析师，你正在参与第%d轮投资决策辩论。
            
            你的分析结果：
            - 情绪评分：%.2f/10
            - 投资建议：%s
            - 置信度：%.2f
            - 风险等级：%s
            
            """, round, analysisResult.getSentimentScore(), 
            analysisResult.getRecommendation(), analysisResult.getConfidence(),
            analysisResult.getRiskAssessment().getRiskLevel()));
        
        if (round == 1) {
            prompt.append("""
                第1轮：请阐述你的情绪面观点
                要求：
                1. 基于新闻情绪和市场情绪提出明确观点
                2. 列举3-5个关键的情绪信号
                3. 说明情绪对投资决策的影响
                4. 控制在200字以内
                """);
        } else if (round == 2) {
            prompt.append("\n其他分析师的观点：\n");
            for (FundamentalAnalyst.DebateArgument arg : previousArguments) {
                if (!"sentiment_analyst".equals(arg.getAgentId())) {
                    prompt.append(String.format("- %s: %s\n", arg.getAgentName(), arg.getArgument()));
                }
            }
            prompt.append("""
                \n第2轮：请从情绪面角度质疑其他观点
                要求：
                1. 指出其他观点忽略的情绪风险
                2. 用舆情数据支撑你的反驳
                3. 强化你的情绪面观点
                4. 控制在200字以内
                """);
        } else if (round == 3) {
            prompt.append("""
                第3轮：情绪面与其他因素的综合建议
                要求：
                1. 综合情绪面和其他分析维度
                2. 提出平衡的投资策略
                3. 明确情绪风险的管控措施
                4. 控制在200字以内
                """);
        }
        
        return prompt.toString();
    }
    
    // 工具方法
    
    private String determineOverallSentiment(double avgScore, int positive, int negative, int neutral) {
        if (avgScore > 0.2 && positive > negative) return "积极";
        if (avgScore < -0.2 && negative > positive) return "消极";
        return "中性";
    }
    
    private String analyzeMarketNewsSentiment(NewsItem news) {
        // 简化的市场情绪分析
        String content = (news.getTitle() + " " + news.getContent()).toLowerCase();
        
        List<String> positiveKeywords = Arrays.asList("上涨", "利好", "增长", "突破", "创新高", "买入", "看好");
        List<String> negativeKeywords = Arrays.asList("下跌", "利空", "下滑", "跌破", "创新低", "卖出", "看空");
        
        long positiveCount = positiveKeywords.stream().mapToLong(keyword -> 
            content.split(keyword, -1).length - 1).sum();
        long negativeCount = negativeKeywords.stream().mapToLong(keyword -> 
            content.split(keyword, -1).length - 1).sum();
        
        if (positiveCount > negativeCount) return "乐观";
        if (negativeCount > positiveCount) return "悲观";
        return "中性";
    }
    
    private List<String> extractMarketThemes(NewsItem news) {
        List<String> themes = new ArrayList<>();
        String content = news.getTitle() + " " + news.getContent();
        
        // 预定义主题关键词
        Map<String, List<String>> themeKeywords = Map.of(
            "货币政策", Arrays.asList("央行", "利率", "货币政策", "流动性"),
            "财政政策", Arrays.asList("财政", "税收", "减税", "刺激政策"),
            "监管政策", Arrays.asList("监管", "合规", "政策", "规定"),
            "行业发展", Arrays.asList("行业", "发展", "转型", "升级"),
            "国际贸易", Arrays.asList("贸易", "出口", "进口", "关税"),
            "科技创新", Arrays.asList("科技", "创新", "研发", "技术")
        );
        
        for (Map.Entry<String, List<String>> entry : themeKeywords.entrySet()) {
            if (entry.getValue().stream().anyMatch(content::contains)) {
                themes.add(entry.getKey());
            }
        }
        
        return themes;
    }
    
    private double calculateMarketSentimentScore(NewsItem news, String sentiment) {
        switch (sentiment) {
            case "乐观": return 0.5;
            case "悲观": return -0.5;
            default: return 0.0;
        }
    }
    
    private String determineMarketMood(Map<String, Integer> distribution, double avgScore) {
        int optimistic = distribution.get("乐观");
        int pessimistic = distribution.get("悲观");
        int neutral = distribution.get("中性");
        
        if (optimistic > pessimistic && avgScore > 0.1) return "乐观";
        if (pessimistic > optimistic && avgScore < -0.1) return "悲观";
        return "中性";
    }
    
    private double calculateMarketVolatility(Map<String, Integer> distribution) {
        int total = distribution.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) return 0.0;
        
        int optimistic = distribution.get("乐观");
        int pessimistic = distribution.get("悲观");
        
        // 计算情绪分化程度
        double optimisticRatio = (double) optimistic / total;
        double pessimisticRatio = (double) pessimistic / total;
        
        return Math.abs(optimisticRatio - pessimisticRatio);
    }
    
    private String determineRiskLevel(double riskScore) {
        if (riskScore >= 0.7) return "高风险";
        if (riskScore >= 0.4) return "中等风险";
        if (riskScore >= 0.2) return "低风险";
        return "极低风险";
    }
    
    private String generateRiskRecommendation(String riskLevel, List<String> riskFactors) {
        switch (riskLevel) {
            case "高风险":
                return "建议谨慎投资，密切关注舆情变化，考虑减仓或观望";
            case "中等风险":
                return "建议适度投资，加强风险监控，控制仓位";
            case "低风险":
                return "风险可控，可正常投资，但需关注潜在风险因子";
            default:
                return "情绪面风险较低，对投资决策影响有限";
        }
    }
    
    private Map<String, Integer> extractKeywords(List<NewsItem> newsList) {
        Map<String, Integer> keywords = new HashMap<>();
        
        // 预定义关键词
        List<String> importantKeywords = Arrays.asList(
            "业绩", "财报", "营收", "利润", "增长", "下滑", "亏损",
            "重组", "并购", "分拆", "上市", "退市",
            "监管", "调查", "处罚", "合规", "违规",
            "创新", "研发", "技术", "专利", "产品",
            "市场", "竞争", "份额", "扩张", "收缩"
        );
        
        for (NewsItem news : newsList) {
            String content = news.getTitle() + " " + news.getContent();
            for (String keyword : importantKeywords) {
                if (content.contains(keyword)) {
                    keywords.put(keyword, keywords.getOrDefault(keyword, 0) + 1);
                }
            }
        }
        
        return keywords;
    }
    
    private double calculateKeywordImpact(String keyword, List<NewsItem> newsList) {
        // 简化的影响力计算
        double impact = 0.0;
        
        for (NewsItem news : newsList) {
            if ((news.getTitle() + " " + news.getContent()).contains(keyword)) {
                // 根据新闻来源和时效性计算影响力
                impact += calculateNewsImpact(news);
            }
        }
        
        return Math.min(impact, 1.0);
    }
    
    private double calculateNewsImpact(NewsItem news) {
        double impact = 0.3; // 基础影响力
        
        // 根据来源调整
        if (news.getSource().contains("财联社") || news.getSource().contains("证券时报")) {
            impact += 0.3;
        }
        
        // 根据时效性调整（简化实现）
        impact += 0.2;
        
        return Math.min(impact, 1.0);
    }
    
    private String calculateSentimentTrend(List<DailySentiment> dailySentiments) {
        if (dailySentiments.size() < 2) return "数据不足";
        
        double firstScore = dailySentiments.get(0).getSentimentScore();
        double lastScore = dailySentiments.get(dailySentiments.size() - 1).getSentimentScore();
        
        double change = lastScore - firstScore;
        
        if (change > 0.1) return "改善";
        if (change < -0.1) return "恶化";
        return "稳定";
    }
    
    private double calculateSentimentVolatility(List<DailySentiment> dailySentiments) {
        if (dailySentiments.size() < 2) return 0.0;
        
        double mean = dailySentiments.stream()
                .mapToDouble(DailySentiment::getSentimentScore)
                .average()
                .orElse(0.0);
        
        double variance = dailySentiments.stream()
                .mapToDouble(ds -> Math.pow(ds.getSentimentScore() - mean, 2))
                .average()
                .orElse(0.0);
        
        return Math.sqrt(variance);
    }
    
    private double calculateSentimentScore(NewsSentimentAnalysis newsSentiment, 
                                         MarketSentimentAnalysis marketSentiment, 
                                         RiskAssessment riskAssessment) {
        double score = 5.0; // 基础分
        
        // 新闻情绪评分
        score += newsSentiment.getSentimentScore() * 2;
        
        // 市场情绪评分
        score += marketSentiment.getMarketScore() * 1.5;
        
        // 风险调整
        score -= riskAssessment.getRiskScore() * 2;
        
        return Math.max(0, Math.min(10, score));
    }
    
    private String generateRecommendation(double sentimentScore, RiskAssessment riskAssessment) {
        if (sentimentScore >= 8.0 && "极低风险".equals(riskAssessment.getRiskLevel())) {
            return "强烈买入";
        } else if (sentimentScore >= 7.0 && !"高风险".equals(riskAssessment.getRiskLevel())) {
            return "买入";
        } else if (sentimentScore >= 5.0) {
            return "持有";
        } else if (sentimentScore >= 3.0) {
            return "卖出";
        } else {
            return "强烈卖出";
        }
    }
    
    private double calculateConfidence(NewsSentimentAnalysis newsSentiment, MarketSentimentAnalysis marketSentiment) {
        double confidence = 0.5; // 基础置信度
        
        // 数据量调整
        if (newsSentiment.getNewsVolume() > 20) confidence += 0.2;
        if (newsSentiment.getNewsVolume() > 50) confidence += 0.1;
        
        // 情绪一致性调整
        if (newsSentiment.getOverallSentiment().equals(marketSentiment.getMarketMood())) {
            confidence += 0.2;
        }
        
        return Math.min(1.0, confidence);
    }
    
    private SentimentAnalysisResult createErrorResult(String errorMessage) {
        return SentimentAnalysisResult.builder()
                .agentId("sentiment_analyst")
                .agentName("情绪分析师")
                .analysis("分析失败: " + errorMessage)
                .sentimentScore(0.0)
                .recommendation("无法分析")
                .confidence(0.0)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    private FundamentalAnalyst.DebateArgument createErrorDebateArgument(String errorMessage) {
        return FundamentalAnalyst.DebateArgument.builder()
                .agentId("sentiment_analyst")
                .agentName("情绪分析师")
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
    public static class SentimentAnalysisResult {
        private String stockCode;
        private String agentId;
        private String agentName;
        private String analysis;
        private NewsSentimentAnalysis newsSentiment;
        private MarketSentimentAnalysis marketSentiment;
        private RiskAssessment riskAssessment;
        private List<SentimentDriver> sentimentDrivers;
        private SentimentTrend sentimentTrend;
        private Double sentimentScore;
        private String recommendation;
        private Double confidence;
        private Long timestamp;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NewsSentimentAnalysis {
        private String overallSentiment;
        private Integer positiveCount;
        private Integer negativeCount;
        private Integer neutralCount;
        private Double sentimentScore;
        private List<NewsItem> keyPositiveNews;
        private List<NewsItem> keyNegativeNews;
        private Integer newsVolume;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MarketSentimentAnalysis {
        private String marketMood;
        private Map<String, Integer> sentimentDistribution;
        private Double marketScore;
        private List<String> hotThemes;
        private Double marketVolatility;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RiskAssessment {
        private String riskLevel;
        private Double riskScore;
        private List<String> riskFactors;
        private String recommendation;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SentimentDriver {
        private String driverType;
        private String description;
        private Double impact;
        private Integer frequency;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SentimentTrend {
        private String trend;
        private Double volatility;
        private List<DailySentiment> dailySentiments;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DailySentiment {
        private String date;
        private Double sentimentScore;
        private Integer newsCount;
    }
}