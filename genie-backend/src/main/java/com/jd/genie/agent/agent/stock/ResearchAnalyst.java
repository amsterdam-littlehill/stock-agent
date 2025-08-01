package com.jd.genie.agent.agent.stock;

import com.jd.genie.agent.agent.BaseAgent;
import com.jd.genie.agent.enums.AgentType;
import com.jd.genie.service.StockDataService;
import com.jd.genie.entity.StockInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 研究分析师智能体
 * 基于TradingAgents框架的专业研究分析师角色
 * 
 * 职责：
 * - 行业研报收集和分析
 * - 机构观点整理
 * - 深度研究报告
 * - 公司调研分析
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component("research_analyst")
public class ResearchAnalyst extends BaseAgent {
    
    @Autowired
    private StockDataService stockDataService;
    
    public ResearchAnalyst() {
        super();
        this.agentType = AgentType.ANALYSIS;
        this.agentName = "研究分析师";
        this.description = "专业的研究分析师，擅长行业研究、机构观点分析和深度调研";
    }
    
    /**
     * 执行研究分析
     * 
     * @param stockCode 股票代码
     * @param context 分析上下文
     * @return 分析结果
     */
    public CompletableFuture<ResearchAnalysisResult> analyze(String stockCode, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("研究分析师开始分析股票: {}", stockCode);
                
                // 1. 获取股票基本信息
                StockInfo stockInfo = stockDataService.getStockInfo(stockCode);
                
                // 2. 行业研究分析
                IndustryResearchAnalysis industryAnalysis = analyzeIndustryResearch(stockCode, stockInfo);
                
                // 3. 机构观点分析
                InstitutionalViewAnalysis institutionalAnalysis = analyzeInstitutionalViews(stockCode);
                
                // 4. 竞争对手分析
                CompetitorAnalysis competitorAnalysis = analyzeCompetitors(stockCode, stockInfo);
                
                // 5. 公司调研分析
                CompanyResearchAnalysis companyAnalysis = analyzeCompanyResearch(stockCode, stockInfo);
                
                // 6. 投资逻辑分析
                InvestmentLogicAnalysis logicAnalysis = analyzeInvestmentLogic(stockCode, stockInfo, 
                    industryAnalysis, competitorAnalysis);
                
                // 7. 风险因素分析
                RiskFactorAnalysis riskAnalysis = analyzeRiskFactors(stockCode, stockInfo, industryAnalysis);
                
                // 8. LLM深度分析
                String llmAnalysis = generateLLMAnalysis(stockCode, stockInfo, industryAnalysis, 
                    institutionalAnalysis, competitorAnalysis, companyAnalysis, logicAnalysis, riskAnalysis);
                
                // 9. 计算研究评分和置信度
                double researchScore = calculateResearchScore(industryAnalysis, institutionalAnalysis, 
                    logicAnalysis, riskAnalysis);
                double confidence = calculateConfidence(industryAnalysis, institutionalAnalysis, competitorAnalysis);
                
                // 10. 生成投资建议
                String recommendation = generateRecommendation(researchScore, logicAnalysis, riskAnalysis);
                
                return ResearchAnalysisResult.builder()
                    .stockCode(stockCode)
                    .agentId("research_analyst")
                    .agentName("研究分析师")
                    .analysis(llmAnalysis)
                    .industryAnalysis(industryAnalysis)
                    .institutionalAnalysis(institutionalAnalysis)
                    .competitorAnalysis(competitorAnalysis)
                    .companyAnalysis(companyAnalysis)
                    .logicAnalysis(logicAnalysis)
                    .riskAnalysis(riskAnalysis)
                    .researchScore(researchScore)
                    .recommendation(recommendation)
                    .confidence(confidence)
                    .timestamp(System.currentTimeMillis())
                    .build();
                    
            } catch (Exception e) {
                log.error("研究分析失败: {}", e.getMessage(), e);
                return createErrorResult("研究分析失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 参与结构化辩论
     */
    public FundamentalAnalyst.DebateArgument debate(FundamentalAnalyst.DebateContext context, 
                                                   List<FundamentalAnalyst.DebateArgument> previousArguments) {
        try {
            ResearchAnalysisResult analysisResult = analyze(context.getStockCode(), context.getContext()).get();
            
            String debatePrompt = buildDebatePrompt(context.getCurrentRound(), analysisResult, previousArguments);
            String argument = llmService.chat(debatePrompt);
            
            return FundamentalAnalyst.DebateArgument.builder()
                .agentId("research_analyst")
                .agentName("研究分析师")
                .round(context.getCurrentRound())
                .argument(argument)
                .confidence(analysisResult.getConfidence())
                .evidenceType("RESEARCH_ANALYSIS")
                .timestamp(System.currentTimeMillis())
                .build();
                
        } catch (Exception e) {
            log.error("研究分析师辩论失败", e);
            return createErrorDebateArgument("研究分析师辩论失败: " + e.getMessage());
        }
    }
    
    /**
     * 分析行业研究
     */
    private IndustryResearchAnalysis analyzeIndustryResearch(String stockCode, StockInfo stockInfo) {
        String industry = stockInfo != null ? stockInfo.getIndustry() : "未知行业";
        
        // 行业发展阶段分析
        String developmentStage = analyzeDevelopmentStage(industry);
        
        // 行业增长前景
        String growthProspect = analyzeGrowthProspect(industry);
        
        // 行业竞争格局
        String competitivePattern = analyzeCompetitivePattern(industry);
        
        // 行业政策环境
        String policyEnvironment = analyzePolicyEnvironment(industry);
        
        // 行业技术趋势
        String technologyTrend = analyzeTechnologyTrend(industry);
        
        // 行业评分
        double industryScore = calculateIndustryScore(developmentStage, growthProspect, 
            competitivePattern, policyEnvironment);
        
        return IndustryResearchAnalysis.builder()
            .industry(industry)
            .developmentStage(developmentStage)
            .growthProspect(growthProspect)
            .competitivePattern(competitivePattern)
            .policyEnvironment(policyEnvironment)
            .technologyTrend(technologyTrend)
            .industryScore(industryScore)
            .build();
    }
    
    /**
     * 分析机构观点
     */
    private InstitutionalViewAnalysis analyzeInstitutionalViews(String stockCode) {
        // 模拟机构观点数据（实际应从数据库或API获取）
        List<InstitutionalView> views = generateMockInstitutionalViews(stockCode);
        
        // 统计机构评级分布
        Map<String, Integer> ratingDistribution = views.stream()
            .collect(Collectors.groupingBy(
                InstitutionalView::getRating,
                Collectors.summingInt(v -> 1)
            ));
        
        // 计算平均目标价
        double avgTargetPrice = views.stream()
            .mapToDouble(InstitutionalView::getTargetPrice)
            .average()
            .orElse(0.0);
        
        // 计算一致性评级
        String consensusRating = calculateConsensusRating(ratingDistribution);
        
        // 机构观点变化趋势
        String viewTrend = analyzeViewTrend(views);
        
        return InstitutionalViewAnalysis.builder()
            .institutionalViews(views)
            .ratingDistribution(ratingDistribution)
            .avgTargetPrice(avgTargetPrice)
            .consensusRating(consensusRating)
            .viewTrend(viewTrend)
            .build();
    }
    
    /**
     * 分析竞争对手
     */
    private CompetitorAnalysis analyzeCompetitors(String stockCode, StockInfo stockInfo) {
        // 获取同行业竞争对手（模拟数据）
        List<CompetitorInfo> competitors = generateMockCompetitors(stockCode, stockInfo);
        
        // 市场份额分析
        MarketShareAnalysis marketShare = analyzeMarketShare(stockCode, competitors);
        
        // 竞争优势分析
        List<String> competitiveAdvantages = analyzeCompetitiveAdvantages(stockCode, competitors);
        
        // 竞争劣势分析
        List<String> competitiveDisadvantages = analyzeCompetitiveDisadvantages(stockCode, competitors);
        
        // 竞争地位评估
        String competitivePosition = assessCompetitivePosition(marketShare, competitiveAdvantages, 
            competitiveDisadvantages);
        
        return CompetitorAnalysis.builder()
            .competitors(competitors)
            .marketShare(marketShare)
            .competitiveAdvantages(competitiveAdvantages)
            .competitiveDisadvantages(competitiveDisadvantages)
            .competitivePosition(competitivePosition)
            .build();
    }
    
    /**
     * 分析公司调研
     */
    private CompanyResearchAnalysis analyzeCompanyResearch(String stockCode, StockInfo stockInfo) {
        // 管理层质量评估
        ManagementQuality managementQuality = assessManagementQuality(stockCode, stockInfo);
        
        // 商业模式分析
        BusinessModelAnalysis businessModel = analyzeBusinessModel(stockCode, stockInfo);
        
        // 核心竞争力分析
        List<String> coreCompetencies = analyzeCoreCompetencies(stockCode, stockInfo);
        
        // 发展战略分析
        DevelopmentStrategy strategy = analyzeDevelopmentStrategy(stockCode, stockInfo);
        
        // 财务健康度评估
        FinancialHealth financialHealth = assessFinancialHealth(stockCode, stockInfo);
        
        return CompanyResearchAnalysis.builder()
            .managementQuality(managementQuality)
            .businessModel(businessModel)
            .coreCompetencies(coreCompetencies)
            .strategy(strategy)
            .financialHealth(financialHealth)
            .build();
    }
    
    /**
     * 分析投资逻辑
     */
    private InvestmentLogicAnalysis analyzeInvestmentLogic(String stockCode, StockInfo stockInfo,
                                                         IndustryResearchAnalysis industryAnalysis,
                                                         CompetitorAnalysis competitorAnalysis) {
        
        // 投资亮点
        List<String> investmentHighlights = identifyInvestmentHighlights(stockInfo, industryAnalysis, 
            competitorAnalysis);
        
        // 增长驱动因素
        List<String> growthDrivers = identifyGrowthDrivers(stockInfo, industryAnalysis);
        
        // 价值催化剂
        List<String> valueCatalysts = identifyValueCatalysts(stockCode, stockInfo);
        
        // 投资逻辑评分
        double logicScore = calculateLogicScore(investmentHighlights, growthDrivers, valueCatalysts);
        
        // 投资主题
        String investmentTheme = determineInvestmentTheme(industryAnalysis, investmentHighlights);
        
        return InvestmentLogicAnalysis.builder()
            .investmentHighlights(investmentHighlights)
            .growthDrivers(growthDrivers)
            .valueCatalysts(valueCatalysts)
            .logicScore(logicScore)
            .investmentTheme(investmentTheme)
            .build();
    }
    
    /**
     * 分析风险因素
     */
    private RiskFactorAnalysis analyzeRiskFactors(String stockCode, StockInfo stockInfo,
                                                IndustryResearchAnalysis industryAnalysis) {
        
        // 行业风险
        List<String> industryRisks = identifyIndustryRisks(industryAnalysis);
        
        // 公司特定风险
        List<String> companyRisks = identifyCompanyRisks(stockCode, stockInfo);
        
        // 市场风险
        List<String> marketRisks = identifyMarketRisks();
        
        // 政策风险
        List<String> policyRisks = identifyPolicyRisks(industryAnalysis);
        
        // 风险等级评估
        String riskLevel = assessOverallRiskLevel(industryRisks, companyRisks, marketRisks, policyRisks);
        
        return RiskFactorAnalysis.builder()
            .industryRisks(industryRisks)
            .companyRisks(companyRisks)
            .marketRisks(marketRisks)
            .policyRisks(policyRisks)
            .riskLevel(riskLevel)
            .build();
    }
    
    /**
     * 生成LLM分析
     */
    private String generateLLMAnalysis(String stockCode, StockInfo stockInfo,
                                     IndustryResearchAnalysis industryAnalysis,
                                     InstitutionalViewAnalysis institutionalAnalysis,
                                     CompetitorAnalysis competitorAnalysis,
                                     CompanyResearchAnalysis companyAnalysis,
                                     InvestmentLogicAnalysis logicAnalysis,
                                     RiskFactorAnalysis riskAnalysis) {
        
        String prompt = String.format("""
            作为专业的研究分析师，请基于以下深度研究数据对股票 %s (%s) 进行综合分析：
            
            行业研究分析：
            - 所属行业：%s
            - 发展阶段：%s
            - 增长前景：%s
            - 竞争格局：%s
            - 政策环境：%s
            - 行业评分：%.2f
            
            机构观点分析：
            - 一致性评级：%s
            - 平均目标价：%.2f
            - 观点趋势：%s
            
            竞争对手分析：
            - 竞争地位：%s
            - 竞争优势：%s
            - 竞争劣势：%s
            
            投资逻辑分析：
            - 投资主题：%s
            - 投资亮点：%s
            - 增长驱动：%s
            - 价值催化剂：%s
            - 逻辑评分：%.2f
            
            风险因素分析：
            - 风险等级：%s
            - 行业风险：%s
            - 公司风险：%s
            - 市场风险：%s
            
            请提供：
            1. 行业地位和竞争优势评估
            2. 投资逻辑和增长驱动分析
            3. 风险因素和应对策略
            4. 机构观点一致性分析
            5. 长期投资价值判断
            """,
            stockCode,
            stockInfo != null ? stockInfo.getStockName() : "未知",
            industryAnalysis.getIndustry(),
            industryAnalysis.getDevelopmentStage(),
            industryAnalysis.getGrowthProspect(),
            industryAnalysis.getCompetitivePattern(),
            industryAnalysis.getPolicyEnvironment(),
            industryAnalysis.getIndustryScore(),
            institutionalAnalysis.getConsensusRating(),
            institutionalAnalysis.getAvgTargetPrice(),
            institutionalAnalysis.getViewTrend(),
            competitorAnalysis.getCompetitivePosition(),
            competitorAnalysis.getCompetitiveAdvantages(),
            competitorAnalysis.getCompetitiveDisadvantages(),
            logicAnalysis.getInvestmentTheme(),
            logicAnalysis.getInvestmentHighlights(),
            logicAnalysis.getGrowthDrivers(),
            logicAnalysis.getValueCatalysts(),
            logicAnalysis.getLogicScore(),
            riskAnalysis.getRiskLevel(),
            riskAnalysis.getIndustryRisks(),
            riskAnalysis.getCompanyRisks(),
            riskAnalysis.getMarketRisks()
        );
        
        return llmService.chat(prompt);
    }
    
    // ==================== 辅助方法 ====================
    
    private String analyzeDevelopmentStage(String industry) {
        // 基于行业关键词判断发展阶段
        Map<String, String> stageMap = Map.of(
            "新能源", "成长期", "人工智能", "导入期", "房地产", "成熟期",
            "钢铁", "成熟期", "煤炭", "衰退期", "医药", "成长期",
            "科技", "成长期", "金融", "成熟期", "消费", "成长期"
        );
        
        return stageMap.entrySet().stream()
            .filter(entry -> industry.contains(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse("成熟期");
    }
    
    private String analyzeGrowthProspect(String industry) {
        // 基于行业判断增长前景
        if (industry.contains("新能源") || industry.contains("人工智能") || industry.contains("生物医药")) {
            return "高增长";
        } else if (industry.contains("消费") || industry.contains("科技") || industry.contains("医药")) {
            return "稳定增长";
        } else {
            return "低增长";
        }
    }
    
    private String analyzeCompetitivePattern(String industry) {
        if (industry.contains("互联网") || industry.contains("电商")) {
            return "寡头垄断";
        } else if (industry.contains("制造") || industry.contains("化工")) {
            return "充分竞争";
        } else {
            return "垄断竞争";
        }
    }
    
    private String analyzePolicyEnvironment(String industry) {
        if (industry.contains("新能源") || industry.contains("环保") || industry.contains("科技")) {
            return "政策支持";
        } else if (industry.contains("房地产") || industry.contains("煤炭")) {
            return "政策限制";
        } else {
            return "政策中性";
        }
    }
    
    private String analyzeTechnologyTrend(String industry) {
        if (industry.contains("人工智能") || industry.contains("新能源")) {
            return "技术驱动";
        } else if (industry.contains("制造") || industry.contains("医药")) {
            return "技术升级";
        } else {
            return "技术稳定";
        }
    }
    
    private double calculateIndustryScore(String developmentStage, String growthProspect, 
                                        String competitivePattern, String policyEnvironment) {
        double score = 50.0; // 基础分
        
        // 发展阶段加分
        switch (developmentStage) {
            case "导入期": score += 10; break;
            case "成长期": score += 20; break;
            case "成熟期": score += 5; break;
            case "衰退期": score -= 10; break;
        }
        
        // 增长前景加分
        switch (growthProspect) {
            case "高增长": score += 20; break;
            case "稳定增长": score += 10; break;
            case "低增长": score -= 5; break;
        }
        
        // 政策环境加分
        switch (policyEnvironment) {
            case "政策支持": score += 15; break;
            case "政策中性": break;
            case "政策限制": score -= 15; break;
        }
        
        return Math.max(0, Math.min(100, score));
    }
    
    private List<InstitutionalView> generateMockInstitutionalViews(String stockCode) {
        // 模拟机构观点数据
        return Arrays.asList(
            InstitutionalView.builder().institution("中信证券").rating("买入").targetPrice(25.0).build(),
            InstitutionalView.builder().institution("国泰君安").rating("增持").targetPrice(23.5).build(),
            InstitutionalView.builder().institution("华泰证券").rating("买入").targetPrice(26.0).build(),
            InstitutionalView.builder().institution("招商证券").rating("中性").targetPrice(22.0).build(),
            InstitutionalView.builder().institution("海通证券").rating("增持").targetPrice(24.0).build()
        );
    }
    
    private String calculateConsensusRating(Map<String, Integer> ratingDistribution) {
        int buyCount = ratingDistribution.getOrDefault("买入", 0);
        int holdCount = ratingDistribution.getOrDefault("增持", 0) + ratingDistribution.getOrDefault("中性", 0);
        int sellCount = ratingDistribution.getOrDefault("卖出", 0);
        
        if (buyCount > holdCount && buyCount > sellCount) {
            return "买入";
        } else if (sellCount > buyCount && sellCount > holdCount) {
            return "卖出";
        } else {
            return "中性";
        }
    }
    
    private String analyzeViewTrend(List<InstitutionalView> views) {
        // 简化的趋势分析
        long positiveViews = views.stream()
            .filter(v -> "买入".equals(v.getRating()) || "增持".equals(v.getRating()))
            .count();
        
        return positiveViews > views.size() / 2 ? "积极" : "谨慎";
    }
    
    // 其他辅助方法的简化实现...
    private List<CompetitorInfo> generateMockCompetitors(String stockCode, StockInfo stockInfo) {
        return Arrays.asList(
            CompetitorInfo.builder().name("竞争对手A").marketShare(15.0).build(),
            CompetitorInfo.builder().name("竞争对手B").marketShare(12.0).build(),
            CompetitorInfo.builder().name("竞争对手C").marketShare(10.0).build()
        );
    }
    
    private MarketShareAnalysis analyzeMarketShare(String stockCode, List<CompetitorInfo> competitors) {
        return MarketShareAnalysis.builder()
            .currentMarketShare(18.0)
            .marketRank(2)
            .shareGrowthTrend("上升")
            .build();
    }
    
    private List<String> analyzeCompetitiveAdvantages(String stockCode, List<CompetitorInfo> competitors) {
        return Arrays.asList("技术领先", "品牌优势", "渠道优势", "成本控制");
    }
    
    private List<String> analyzeCompetitiveDisadvantages(String stockCode, List<CompetitorInfo> competitors) {
        return Arrays.asList("规模较小", "资金压力");
    }
    
    private String assessCompetitivePosition(MarketShareAnalysis marketShare, 
                                           List<String> advantages, List<String> disadvantages) {
        if (marketShare.getMarketRank() <= 3 && advantages.size() > disadvantages.size()) {
            return "领先地位";
        } else if (marketShare.getMarketRank() <= 5) {
            return "竞争地位";
        } else {
            return "跟随地位";
        }
    }
    
    // 继续实现其他方法...
    private ManagementQuality assessManagementQuality(String stockCode, StockInfo stockInfo) {
        return ManagementQuality.builder()
            .leadershipScore(8.0)
            .experienceScore(7.5)
            .visionScore(8.5)
            .executionScore(7.8)
            .overallScore(7.95)
            .build();
    }
    
    private BusinessModelAnalysis analyzeBusinessModel(String stockCode, StockInfo stockInfo) {
        return BusinessModelAnalysis.builder()
            .modelType("B2B")
            .revenueModel("产品销售")
            .profitModel("规模效应")
            .scalability("高")
            .sustainability("强")
            .build();
    }
    
    private List<String> analyzeCoreCompetencies(String stockCode, StockInfo stockInfo) {
        return Arrays.asList("技术创新能力", "市场营销能力", "供应链管理", "人才团队");
    }
    
    private DevelopmentStrategy analyzeDevelopmentStrategy(String stockCode, StockInfo stockInfo) {
        return DevelopmentStrategy.builder()
            .strategicDirection("数字化转型")
            .expansionPlan("国际化扩张")
            .innovationFocus("产品创新")
            .timeframe("3-5年")
            .feasibility("高")
            .build();
    }
    
    private FinancialHealth assessFinancialHealth(String stockCode, StockInfo stockInfo) {
        return FinancialHealth.builder()
            .liquidityRatio(1.5)
            .debtRatio(0.3)
            .profitabilityScore(7.5)
            .growthScore(8.0)
            .overallHealth("良好")
            .build();
    }
    
    private List<String> identifyInvestmentHighlights(StockInfo stockInfo, 
                                                     IndustryResearchAnalysis industryAnalysis,
                                                     CompetitorAnalysis competitorAnalysis) {
        return Arrays.asList("行业龙头地位", "技术护城河", "管理层优秀", "财务稳健", "增长确定性高");
    }
    
    private List<String> identifyGrowthDrivers(StockInfo stockInfo, IndustryResearchAnalysis industryAnalysis) {
        return Arrays.asList("行业需求增长", "市场份额提升", "产品升级换代", "海外市场拓展");
    }
    
    private List<String> identifyValueCatalysts(String stockCode, StockInfo stockInfo) {
        return Arrays.asList("业绩超预期", "新产品发布", "政策利好", "并购重组");
    }
    
    private double calculateLogicScore(List<String> highlights, List<String> drivers, List<String> catalysts) {
        return (highlights.size() * 20 + drivers.size() * 15 + catalysts.size() * 10);
    }
    
    private String determineInvestmentTheme(IndustryResearchAnalysis industryAnalysis, List<String> highlights) {
        if (industryAnalysis.getIndustry().contains("科技")) {
            return "科技创新";
        } else if (industryAnalysis.getIndustry().contains("消费")) {
            return "消费升级";
        } else {
            return "价值投资";
        }
    }
    
    private List<String> identifyIndustryRisks(IndustryResearchAnalysis industryAnalysis) {
        return Arrays.asList("行业周期性风险", "技术替代风险", "政策变化风险");
    }
    
    private List<String> identifyCompanyRisks(String stockCode, StockInfo stockInfo) {
        return Arrays.asList("经营风险", "财务风险", "管理风险");
    }
    
    private List<String> identifyMarketRisks() {
        return Arrays.asList("市场波动风险", "流动性风险", "系统性风险");
    }
    
    private List<String> identifyPolicyRisks(IndustryResearchAnalysis industryAnalysis) {
        return Arrays.asList("监管政策变化", "税收政策调整", "环保政策影响");
    }
    
    private String assessOverallRiskLevel(List<String> industryRisks, List<String> companyRisks, 
                                         List<String> marketRisks, List<String> policyRisks) {
        int totalRisks = industryRisks.size() + companyRisks.size() + marketRisks.size() + policyRisks.size();
        
        if (totalRisks <= 8) {
            return "低风险";
        } else if (totalRisks <= 12) {
            return "中等风险";
        } else {
            return "高风险";
        }
    }
    
    private String buildDebatePrompt(int round, ResearchAnalysisResult analysisResult, 
                                   List<FundamentalAnalyst.DebateArgument> previousArguments) {
        
        StringBuilder prompt = new StringBuilder();
        prompt.append(String.format("作为研究分析师，你正在参与第%d轮投资决策辩论。\n\n", round));
        
        prompt.append("你的研究分析结果：\n");
        prompt.append(String.format("- 行业评分：%.2f\n", analysisResult.getIndustryAnalysis().getIndustryScore()));
        prompt.append(String.format("- 机构一致评级：%s\n", analysisResult.getInstitutionalAnalysis().getConsensusRating()));
        prompt.append(String.format("- 竞争地位：%s\n", analysisResult.getCompetitorAnalysis().getCompetitivePosition()));
        prompt.append(String.format("- 投资主题：%s\n", analysisResult.getLogicAnalysis().getInvestmentTheme()));
        prompt.append(String.format("- 风险等级：%s\n", analysisResult.getRiskAnalysis().getRiskLevel()));
        
        if (!previousArguments.isEmpty()) {
            prompt.append("\n其他分析师观点：\n");
            for (FundamentalAnalyst.DebateArgument arg : previousArguments) {
                if (!"research_analyst".equals(arg.getAgentId())) {
                    prompt.append(String.format("- %s: %s\n", arg.getAgentName(), arg.getArgument()));
                }
            }
        }
        
        switch (round) {
            case 1:
                prompt.append("\n请基于深度研究分析，阐述你的核心观点和投资建议。");
                break;
            case 2:
                prompt.append("\n请针对其他分析师的观点，从研究角度提出质疑或补充。");
                break;
            case 3:
                prompt.append("\n请寻求共识，提出综合考虑研究因素的最终建议。");
                break;
        }
        
        return prompt.toString();
    }
    
    private double calculateResearchScore(IndustryResearchAnalysis industryAnalysis,
                                        InstitutionalViewAnalysis institutionalAnalysis,
                                        InvestmentLogicAnalysis logicAnalysis,
                                        RiskFactorAnalysis riskAnalysis) {
        
        double industryWeight = 0.3;
        double institutionalWeight = 0.2;
        double logicWeight = 0.3;
        double riskWeight = 0.2;
        
        double industryScore = industryAnalysis.getIndustryScore();
        double institutionalScore = "买入".equals(institutionalAnalysis.getConsensusRating()) ? 80 : 
                                   "中性".equals(institutionalAnalysis.getConsensusRating()) ? 50 : 20;
        double logicScore = logicAnalysis.getLogicScore();
        double riskScore = "低风险".equals(riskAnalysis.getRiskLevel()) ? 80 : 
                          "中等风险".equals(riskAnalysis.getRiskLevel()) ? 50 : 20;
        
        return industryScore * industryWeight + 
               institutionalScore * institutionalWeight + 
               logicScore * logicWeight + 
               riskScore * riskWeight;
    }
    
    private double calculateConfidence(IndustryResearchAnalysis industryAnalysis,
                                     InstitutionalViewAnalysis institutionalAnalysis,
                                     CompetitorAnalysis competitorAnalysis) {
        
        double baseConfidence = 0.6;
        
        // 行业分析质量加分
        if (industryAnalysis.getIndustryScore() > 70) {
            baseConfidence += 0.1;
        }
        
        // 机构观点一致性加分
        if ("买入".equals(institutionalAnalysis.getConsensusRating())) {
            baseConfidence += 0.15;
        }
        
        // 竞争地位加分
        if ("领先地位".equals(competitorAnalysis.getCompetitivePosition())) {
            baseConfidence += 0.1;
        }
        
        return Math.min(0.95, baseConfidence);
    }
    
    private String generateRecommendation(double researchScore, InvestmentLogicAnalysis logicAnalysis, 
                                        RiskFactorAnalysis riskAnalysis) {
        
        if (researchScore >= 75 && "低风险".equals(riskAnalysis.getRiskLevel())) {
            return "强烈推荐 - 研究价值突出";
        } else if (researchScore >= 60 && !"高风险".equals(riskAnalysis.getRiskLevel())) {
            return "推荐 - 投资逻辑清晰";
        } else if (researchScore >= 40) {
            return "中性 - 需要进一步观察";
        } else {
            return "不推荐 - 研究价值有限";
        }
    }
    
    private ResearchAnalysisResult createErrorResult(String errorMessage) {
        return ResearchAnalysisResult.builder()
            .stockCode("ERROR")
            .agentId("research_analyst")
            .agentName("研究分析师")
            .analysis("分析失败: " + errorMessage)
            .researchScore(0.0)
            .recommendation("无法分析")
            .confidence(0.0)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    private FundamentalAnalyst.DebateArgument createErrorDebateArgument(String errorMessage) {
        return FundamentalAnalyst.DebateArgument.builder()
            .agentId("research_analyst")
            .agentName("研究分析师")
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
    public static class ResearchAnalysisResult {
        private String stockCode;
        private String agentId;
        private String agentName;
        private String analysis;
        private IndustryResearchAnalysis industryAnalysis;
        private InstitutionalViewAnalysis institutionalAnalysis;
        private CompetitorAnalysis competitorAnalysis;
        private CompanyResearchAnalysis companyAnalysis;
        private InvestmentLogicAnalysis logicAnalysis;
        private RiskFactorAnalysis riskAnalysis;
        private Double researchScore;
        private String recommendation;
        private Double confidence;
        private Long timestamp;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IndustryResearchAnalysis {
        private String industry;
        private String developmentStage;
        private String growthProspect;
        private String competitivePattern;
        private String policyEnvironment;
        private String technologyTrend;
        private Double industryScore;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class InstitutionalViewAnalysis {
        private List<InstitutionalView> institutionalViews;
        private Map<String, Integer> ratingDistribution;
        private Double avgTargetPrice;
        private String consensusRating;
        private String viewTrend;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class InstitutionalView {
        private String institution;
        private String rating;
        private Double targetPrice;
        private String analyst;
        private LocalDateTime updateTime;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CompetitorAnalysis {
        private List<CompetitorInfo> competitors;
        private MarketShareAnalysis marketShare;
        private List<String> competitiveAdvantages;
        private List<String> competitiveDisadvantages;
        private String competitivePosition;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CompetitorInfo {
        private String name;
        private String stockCode;
        private Double marketShare;
        private Double revenue;
        private Double marketCap;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MarketShareAnalysis {
        private Double currentMarketShare;
        private Integer marketRank;
        private String shareGrowthTrend;
        private Double shareGrowthRate;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CompanyResearchAnalysis {
        private ManagementQuality managementQuality;
        private BusinessModelAnalysis businessModel;
        private List<String> coreCompetencies;
        private DevelopmentStrategy strategy;
        private FinancialHealth financialHealth;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ManagementQuality {
        private Double leadershipScore;
        private Double experienceScore;
        private Double visionScore;
        private Double executionScore;
        private Double overallScore;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BusinessModelAnalysis {
        private String modelType;
        private String revenueModel;
        private String profitModel;
        private String scalability;
        private String sustainability;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DevelopmentStrategy {
        private String strategicDirection;
        private String expansionPlan;
        private String innovationFocus;
        private String timeframe;
        private String feasibility;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FinancialHealth {
        private Double liquidityRatio;
        private Double debtRatio;
        private Double profitabilityScore;
        private Double growthScore;
        private String overallHealth;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class InvestmentLogicAnalysis {
        private List<String> investmentHighlights;
        private List<String> growthDrivers;
        private List<String> valueCatalysts;
        private Double logicScore;
        private String investmentTheme;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RiskFactorAnalysis {
        private List<String> industryRisks;
        private List<String> companyRisks;
        private List<String> marketRisks;
        private List<String> policyRisks;
        private String riskLevel;
    }
}