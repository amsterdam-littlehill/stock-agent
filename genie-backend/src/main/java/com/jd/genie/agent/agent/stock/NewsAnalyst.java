package com.jd.genie.agent.agent.stock;

import com.jd.genie.agent.agent.BaseAgent;
import com.jd.genie.agent.enums.AgentType;
import com.jd.genie.service.FinancialNewsService;
import com.jd.genie.entity.FinancialNews;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 新闻分析师智能体
 * 基于TradingAgents框架的专业新闻分析师角色
 * 
 * 职责：
 * - 财经新闻收集和分析
 * - 公告信息解读
 * - 政策影响评估
 * - 市场事件分析
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component("news_analyst")
public class NewsAnalyst extends BaseAgent {
    
    @Autowired
    private FinancialNewsService newsService;
    
    public NewsAnalyst() {
        super();
        this.agentType = AgentType.ANALYSIS;
        this.agentName = "新闻分析师";
        this.description = "专业的新闻分析师，擅长财经新闻解读、事件影响分析和信息提取";
    }
    
    /**
     * 执行新闻分析
     * 
     * @param stockCode 股票代码
     * @param context 分析上下文
     * @return 分析结果
     */
    public CompletableFuture<NewsAnalysisResult> analyze(String stockCode, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("新闻分析师开始分析股票: {}", stockCode);
                
                // 1. 获取相关新闻数据
                List<FinancialNews> recentNews = newsService.getNewsByStockCode(stockCode, 30); // 最近30天
                List<FinancialNews> todayNews = newsService.getTodayNewsByStockCode(stockCode);
                
                // 2. 新闻情绪分析
                NewsSentimentAnalysis sentimentAnalysis = analyzeNewsSentiment(recentNews);
                
                // 3. 事件影响分析
                EventImpactAnalysis eventAnalysis = analyzeEventImpact(recentNews, stockCode);
                
                // 4. 政策影响分析
                PolicyImpactAnalysis policyAnalysis = analyzePolicyImpact(recentNews, stockCode);
                
                // 5. 新闻热度分析
                NewsHeatAnalysis heatAnalysis = analyzeNewsHeat(recentNews, todayNews);
                
                // 6. 关键信息提取
                KeyInformationExtraction keyInfo = extractKeyInformation(recentNews);
                
                // 7. LLM深度分析
                String llmAnalysis = generateLLMAnalysis(stockCode, recentNews, sentimentAnalysis, 
                    eventAnalysis, policyAnalysis, heatAnalysis, keyInfo);
                
                // 8. 计算新闻评分和置信度
                double newsScore = calculateNewsScore(sentimentAnalysis, eventAnalysis, policyAnalysis);
                double confidence = calculateConfidence(recentNews.size(), sentimentAnalysis, eventAnalysis);
                
                // 9. 生成投资建议
                String recommendation = generateRecommendation(newsScore, sentimentAnalysis, eventAnalysis);
                
                return NewsAnalysisResult.builder()
                    .stockCode(stockCode)
                    .agentId("news_analyst")
                    .agentName("新闻分析师")
                    .analysis(llmAnalysis)
                    .sentimentAnalysis(sentimentAnalysis)
                    .eventAnalysis(eventAnalysis)
                    .policyAnalysis(policyAnalysis)
                    .heatAnalysis(heatAnalysis)
                    .keyInformation(keyInfo)
                    .newsScore(newsScore)
                    .recommendation(recommendation)
                    .confidence(confidence)
                    .timestamp(System.currentTimeMillis())
                    .build();
                    
            } catch (Exception e) {
                log.error("新闻分析失败: {}", e.getMessage(), e);
                return createErrorResult("新闻分析失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 参与结构化辩论
     */
    public FundamentalAnalyst.DebateArgument debate(FundamentalAnalyst.DebateContext context, 
                                                   List<FundamentalAnalyst.DebateArgument> previousArguments) {
        try {
            NewsAnalysisResult analysisResult = analyze(context.getStockCode(), context.getContext()).get();
            
            String debatePrompt = buildDebatePrompt(context.getCurrentRound(), analysisResult, previousArguments);
            String argument = llmService.chat(debatePrompt);
            
            return FundamentalAnalyst.DebateArgument.builder()
                .agentId("news_analyst")
                .agentName("新闻分析师")
                .round(context.getCurrentRound())
                .argument(argument)
                .confidence(analysisResult.getConfidence())
                .evidenceType("NEWS_ANALYSIS")
                .timestamp(System.currentTimeMillis())
                .build();
                
        } catch (Exception e) {
            log.error("新闻分析师辩论失败", e);
            return createErrorDebateArgument("新闻分析师辩论失败: " + e.getMessage());
        }
    }
    
    /**
     * 分析新闻情绪
     */
    private NewsSentimentAnalysis analyzeNewsSentiment(List<FinancialNews> newsList) {
        if (newsList.isEmpty()) {
            return NewsSentimentAnalysis.builder()
                .overallSentiment("NEUTRAL")
                .positiveCount(0)
                .negativeCount(0)
                .neutralCount(0)
                .sentimentScore(0.0)
                .build();
        }
        
        int positiveCount = 0;
        int negativeCount = 0;
        int neutralCount = 0;
        double totalScore = 0.0;
        
        List<String> positiveKeywords = Arrays.asList(
            "上涨", "增长", "利好", "突破", "创新高", "盈利", "业绩增长", "分红", "回购", "合作",
            "扩张", "投资", "收购", "重组", "转型", "升级", "优化", "提升", "改善", "机遇"
        );
        
        List<String> negativeKeywords = Arrays.asList(
            "下跌", "下滑", "亏损", "风险", "警告", "调查", "处罚", "违规", "退市", "停牌",
            "减持", "抛售", "债务", "危机", "困难", "压力", "挑战", "问题", "争议", "纠纷"
        );
        
        for (FinancialNews news : newsList) {
            String content = news.getTitle() + " " + (news.getContent() != null ? news.getContent() : "");
            
            long positiveMatches = positiveKeywords.stream()
                .mapToLong(keyword -> content.split(keyword, -1).length - 1)
                .sum();
            
            long negativeMatches = negativeKeywords.stream()
                .mapToLong(keyword -> content.split(keyword, -1).length - 1)
                .sum();
            
            if (positiveMatches > negativeMatches) {
                positiveCount++;
                totalScore += 1.0;
            } else if (negativeMatches > positiveMatches) {
                negativeCount++;
                totalScore -= 1.0;
            } else {
                neutralCount++;
            }
        }
        
        double sentimentScore = totalScore / newsList.size();
        String overallSentiment = sentimentScore > 0.1 ? "POSITIVE" : 
                                 sentimentScore < -0.1 ? "NEGATIVE" : "NEUTRAL";
        
        return NewsSentimentAnalysis.builder()
            .overallSentiment(overallSentiment)
            .positiveCount(positiveCount)
            .negativeCount(negativeCount)
            .neutralCount(neutralCount)
            .sentimentScore(sentimentScore)
            .build();
    }
    
    /**
     * 分析事件影响
     */
    private EventImpactAnalysis analyzeEventImpact(List<FinancialNews> newsList, String stockCode) {
        List<String> majorEvents = new ArrayList<>();
        List<String> impactAssessment = new ArrayList<>();
        double impactScore = 0.0;
        
        // 重大事件关键词
        Map<String, Double> eventKeywords = Map.of(
            "重组", 0.8, "收购", 0.7, "分拆", 0.6, "IPO", 0.5, "退市", -0.9,
            "停牌", -0.3, "复牌", 0.3, "分红", 0.4, "增发", -0.2, "回购", 0.5,
            "业绩预告", 0.3, "年报", 0.2, "季报", 0.1, "中报", 0.2, "违规", -0.6
        );
        
        for (FinancialNews news : newsList) {
            String content = news.getTitle() + " " + (news.getContent() != null ? news.getContent() : "");
            
            for (Map.Entry<String, Double> entry : eventKeywords.entrySet()) {
                if (content.contains(entry.getKey())) {
                    majorEvents.add(news.getTitle());
                    impactScore += entry.getValue();
                    
                    String impact = entry.getValue() > 0 ? "正面影响" : "负面影响";
                    impactAssessment.add(entry.getKey() + ": " + impact);
                    break;
                }
            }
        }
        
        String overallImpact = impactScore > 0.2 ? "POSITIVE" : 
                              impactScore < -0.2 ? "NEGATIVE" : "NEUTRAL";
        
        return EventImpactAnalysis.builder()
            .majorEvents(majorEvents)
            .impactAssessment(impactAssessment)
            .overallImpact(overallImpact)
            .impactScore(impactScore)
            .build();
    }
    
    /**
     * 分析政策影响
     */
    private PolicyImpactAnalysis analyzePolicyImpact(List<FinancialNews> newsList, String stockCode) {
        List<String> policyNews = new ArrayList<>();
        List<String> policyImpacts = new ArrayList<>();
        double policyScore = 0.0;
        
        // 政策关键词
        Map<String, Double> policyKeywords = Map.of(
            "央行", 0.3, "降准", 0.5, "降息", 0.4, "加息", -0.4, "监管", -0.2,
            "政策", 0.1, "支持", 0.3, "鼓励", 0.3, "限制", -0.3, "禁止", -0.5,
            "减税", 0.4, "补贴", 0.3, "扶持", 0.3, "调控", -0.2, "整顿", -0.3
        );
        
        for (FinancialNews news : newsList) {
            String content = news.getTitle() + " " + (news.getContent() != null ? news.getContent() : "");
            
            for (Map.Entry<String, Double> entry : policyKeywords.entrySet()) {
                if (content.contains(entry.getKey())) {
                    policyNews.add(news.getTitle());
                    policyScore += entry.getValue();
                    
                    String impact = entry.getValue() > 0 ? "利好政策" : "利空政策";
                    policyImpacts.add(entry.getKey() + ": " + impact);
                    break;
                }
            }
        }
        
        String overallPolicyImpact = policyScore > 0.1 ? "POSITIVE" : 
                                    policyScore < -0.1 ? "NEGATIVE" : "NEUTRAL";
        
        return PolicyImpactAnalysis.builder()
            .policyNews(policyNews)
            .policyImpacts(policyImpacts)
            .overallPolicyImpact(overallPolicyImpact)
            .policyScore(policyScore)
            .build();
    }
    
    /**
     * 分析新闻热度
     */
    private NewsHeatAnalysis analyzeNewsHeat(List<FinancialNews> recentNews, List<FinancialNews> todayNews) {
        int recentNewsCount = recentNews.size();
        int todayNewsCount = todayNews.size();
        
        // 计算平均每日新闻数量
        double avgDailyNews = recentNewsCount / 30.0;
        
        // 计算热度指数
        double heatIndex = todayNewsCount / Math.max(avgDailyNews, 1.0);
        
        String heatLevel = heatIndex > 2.0 ? "HIGH" : 
                          heatIndex > 1.5 ? "MEDIUM" : "LOW";
        
        return NewsHeatAnalysis.builder()
            .recentNewsCount(recentNewsCount)
            .todayNewsCount(todayNewsCount)
            .avgDailyNews(avgDailyNews)
            .heatIndex(heatIndex)
            .heatLevel(heatLevel)
            .build();
    }
    
    /**
     * 提取关键信息
     */
    private KeyInformationExtraction extractKeyInformation(List<FinancialNews> newsList) {
        List<String> keyEvents = new ArrayList<>();
        List<String> importantAnnouncements = new ArrayList<>();
        List<String> marketRumors = new ArrayList<>();
        
        for (FinancialNews news : newsList) {
            String title = news.getTitle();
            
            // 关键事件
            if (title.contains("重组") || title.contains("收购") || title.contains("分拆") || 
                title.contains("IPO") || title.contains("退市") || title.contains("停牌")) {
                keyEvents.add(title);
            }
            
            // 重要公告
            if (title.contains("公告") || title.contains("披露") || title.contains("发布") || 
                title.contains("公布") || title.contains("通知")) {
                importantAnnouncements.add(title);
            }
            
            // 市场传言
            if (title.contains("传言") || title.contains("消息") || title.contains("据悉") || 
                title.contains("传") || title.contains("爆料")) {
                marketRumors.add(title);
            }
        }
        
        return KeyInformationExtraction.builder()
            .keyEvents(keyEvents)
            .importantAnnouncements(importantAnnouncements)
            .marketRumors(marketRumors)
            .build();
    }
    
    /**
     * 生成LLM分析
     */
    private String generateLLMAnalysis(String stockCode, List<FinancialNews> newsList,
                                     NewsSentimentAnalysis sentimentAnalysis,
                                     EventImpactAnalysis eventAnalysis,
                                     PolicyImpactAnalysis policyAnalysis,
                                     NewsHeatAnalysis heatAnalysis,
                                     KeyInformationExtraction keyInfo) {
        
        String prompt = String.format("""
            作为专业的新闻分析师，请基于以下新闻数据对股票 %s 进行深度分析：
            
            新闻情绪分析：
            - 整体情绪：%s
            - 正面新闻：%d条，负面新闻：%d条，中性新闻：%d条
            - 情绪评分：%.2f
            
            事件影响分析：
            - 重大事件：%s
            - 整体影响：%s
            - 影响评分：%.2f
            
            政策影响分析：
            - 政策新闻：%s
            - 整体政策影响：%s
            - 政策评分：%.2f
            
            新闻热度分析：
            - 最近30天新闻数量：%d条
            - 今日新闻数量：%d条
            - 热度等级：%s
            
            关键信息：
            - 关键事件：%s
            - 重要公告：%s
            - 市场传言：%s
            
            请提供：
            1. 新闻面综合评估
            2. 关键事件影响分析
            3. 市场情绪判断
            4. 投资风险提示
            5. 短期和中期展望
            """,
            stockCode,
            sentimentAnalysis.getOverallSentiment(),
            sentimentAnalysis.getPositiveCount(),
            sentimentAnalysis.getNegativeCount(),
            sentimentAnalysis.getNeutralCount(),
            sentimentAnalysis.getSentimentScore(),
            eventAnalysis.getMajorEvents(),
            eventAnalysis.getOverallImpact(),
            eventAnalysis.getImpactScore(),
            policyAnalysis.getPolicyNews(),
            policyAnalysis.getOverallPolicyImpact(),
            policyAnalysis.getPolicyScore(),
            heatAnalysis.getRecentNewsCount(),
            heatAnalysis.getTodayNewsCount(),
            heatAnalysis.getHeatLevel(),
            keyInfo.getKeyEvents(),
            keyInfo.getImportantAnnouncements(),
            keyInfo.getMarketRumors()
        );
        
        return llmService.chat(prompt);
    }
    
    /**
     * 构建辩论提示词
     */
    private String buildDebatePrompt(int round, NewsAnalysisResult analysisResult, 
                                   List<FundamentalAnalyst.DebateArgument> previousArguments) {
        
        StringBuilder prompt = new StringBuilder();
        prompt.append(String.format("作为新闻分析师，你正在参与第%d轮投资决策辩论。\n\n", round));
        
        prompt.append("你的新闻分析结果：\n");
        prompt.append(String.format("- 新闻情绪：%s (评分: %.2f)\n", 
            analysisResult.getSentimentAnalysis().getOverallSentiment(),
            analysisResult.getSentimentAnalysis().getSentimentScore()));
        prompt.append(String.format("- 事件影响：%s (评分: %.2f)\n", 
            analysisResult.getEventAnalysis().getOverallImpact(),
            analysisResult.getEventAnalysis().getImpactScore()));
        prompt.append(String.format("- 政策影响：%s (评分: %.2f)\n", 
            analysisResult.getPolicyAnalysis().getOverallPolicyImpact(),
            analysisResult.getPolicyAnalysis().getPolicyScore()));
        prompt.append(String.format("- 新闻热度：%s\n", 
            analysisResult.getHeatAnalysis().getHeatLevel()));
        
        if (!previousArguments.isEmpty()) {
            prompt.append("\n其他分析师观点：\n");
            for (FundamentalAnalyst.DebateArgument arg : previousArguments) {
                if (!"news_analyst".equals(arg.getAgentId())) {
                    prompt.append(String.format("- %s: %s\n", arg.getAgentName(), arg.getArgument()));
                }
            }
        }
        
        switch (round) {
            case 1:
                prompt.append("\n请基于新闻面分析，阐述你的核心观点和投资建议。");
                break;
            case 2:
                prompt.append("\n请针对其他分析师的观点，从新闻面角度提出质疑或补充。");
                break;
            case 3:
                prompt.append("\n请寻求共识，提出综合考虑新闻面因素的最终建议。");
                break;
        }
        
        return prompt.toString();
    }
    
    /**
     * 计算新闻评分
     */
    private double calculateNewsScore(NewsSentimentAnalysis sentimentAnalysis, 
                                    EventImpactAnalysis eventAnalysis,
                                    PolicyImpactAnalysis policyAnalysis) {
        
        double sentimentWeight = 0.4;
        double eventWeight = 0.4;
        double policyWeight = 0.2;
        
        double sentimentScore = sentimentAnalysis.getSentimentScore();
        double eventScore = eventAnalysis.getImpactScore();
        double policyScore = policyAnalysis.getPolicyScore();
        
        // 归一化到0-100分
        double totalScore = (sentimentScore * sentimentWeight + 
                           eventScore * eventWeight + 
                           policyScore * policyWeight) * 50 + 50;
        
        return Math.max(0, Math.min(100, totalScore));
    }
    
    /**
     * 计算置信度
     */
    private double calculateConfidence(int newsCount, NewsSentimentAnalysis sentimentAnalysis, 
                                     EventImpactAnalysis eventAnalysis) {
        
        // 基础置信度基于新闻数量
        double baseConfidence = Math.min(0.8, newsCount * 0.02);
        
        // 情绪一致性加分
        double totalNews = sentimentAnalysis.getPositiveCount() + 
                          sentimentAnalysis.getNegativeCount() + 
                          sentimentAnalysis.getNeutralCount();
        
        if (totalNews > 0) {
            double maxSentimentRatio = Math.max(
                sentimentAnalysis.getPositiveCount() / totalNews,
                Math.max(
                    sentimentAnalysis.getNegativeCount() / totalNews,
                    sentimentAnalysis.getNeutralCount() / totalNews
                )
            );
            baseConfidence += maxSentimentRatio * 0.2;
        }
        
        // 重大事件加分
        if (!eventAnalysis.getMajorEvents().isEmpty()) {
            baseConfidence += 0.1;
        }
        
        return Math.min(0.95, baseConfidence);
    }
    
    /**
     * 生成投资建议
     */
    private String generateRecommendation(double newsScore, NewsSentimentAnalysis sentimentAnalysis, 
                                        EventImpactAnalysis eventAnalysis) {
        
        if (newsScore >= 70 && "POSITIVE".equals(sentimentAnalysis.getOverallSentiment())) {
            return "买入 - 新闻面利好明显";
        } else if (newsScore <= 30 && "NEGATIVE".equals(sentimentAnalysis.getOverallSentiment())) {
            return "卖出 - 新闻面利空较多";
        } else if (!eventAnalysis.getMajorEvents().isEmpty()) {
            if ("POSITIVE".equals(eventAnalysis.getOverallImpact())) {
                return "关注 - 重大事件待观察";
            } else {
                return "谨慎 - 重大事件存在风险";
            }
        } else {
            return "持有 - 新闻面相对平稳";
        }
    }
    
    /**
     * 创建错误结果
     */
    private NewsAnalysisResult createErrorResult(String errorMessage) {
        return NewsAnalysisResult.builder()
            .stockCode("ERROR")
            .agentId("news_analyst")
            .agentName("新闻分析师")
            .analysis("分析失败: " + errorMessage)
            .newsScore(0.0)
            .recommendation("无法分析")
            .confidence(0.0)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 创建错误辩论参数
     */
    private FundamentalAnalyst.DebateArgument createErrorDebateArgument(String errorMessage) {
        return FundamentalAnalyst.DebateArgument.builder()
            .agentId("news_analyst")
            .agentName("新闻分析师")
            .round(0)
            .argument("无法参与辩论: " + errorMessage)
            .confidence(0.0)
            .evidenceType("ERROR")
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    // ==================== 数据模型类 ====================
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NewsAnalysisResult {
        private String stockCode;
        private String agentId;
        private String agentName;
        private String analysis;
        private NewsSentimentAnalysis sentimentAnalysis;
        private EventImpactAnalysis eventAnalysis;
        private PolicyImpactAnalysis policyAnalysis;
        private NewsHeatAnalysis heatAnalysis;
        private KeyInformationExtraction keyInformation;
        private Double newsScore;
        private String recommendation;
        private Double confidence;
        private Long timestamp;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NewsSentimentAnalysis {
        private String overallSentiment; // POSITIVE, NEGATIVE, NEUTRAL
        private Integer positiveCount;
        private Integer negativeCount;
        private Integer neutralCount;
        private Double sentimentScore; // -1.0 到 1.0
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EventImpactAnalysis {
        private List<String> majorEvents;
        private List<String> impactAssessment;
        private String overallImpact; // POSITIVE, NEGATIVE, NEUTRAL
        private Double impactScore;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PolicyImpactAnalysis {
        private List<String> policyNews;
        private List<String> policyImpacts;
        private String overallPolicyImpact; // POSITIVE, NEGATIVE, NEUTRAL
        private Double policyScore;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NewsHeatAnalysis {
        private Integer recentNewsCount;
        private Integer todayNewsCount;
        private Double avgDailyNews;
        private Double heatIndex;
        private String heatLevel; // HIGH, MEDIUM, LOW
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class KeyInformationExtraction {
        private List<String> keyEvents;
        private List<String> importantAnnouncements;
        private List<String> marketRumors;
    }
}