package com.jd.genie.agent.agent.stock;

import com.jd.genie.agent.agent.BaseAgent;
import com.jd.genie.agent.enums.AgentType;
import com.jd.genie.service.StockDataMigrationService;
import com.jd.genie.service.StockDataMigrationService.KLineData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 技术分析师智能体
 * 基于TradingAgents框架的专业技术分析师角色
 * 
 * 职责：
 * - K线形态分析
 * - 技术指标计算
 * - 趋势判断
 * - 买卖点识别
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component("technical_analyst")
public class TechnicalAnalyst extends BaseAgent {
    
    @Autowired
    private StockDataMigrationService stockDataService;
    
    public TechnicalAnalyst() {
        super();
        this.agentType = AgentType.ANALYSIS;
        this.agentName = "技术分析师";
        this.description = "专业的技术分析师，擅长K线形态、技术指标和趋势分析";
    }
    
    /**
     * 执行技术分析
     * 
     * @param stockCode 股票代码
     * @param context 分析上下文
     * @return 分析结果
     */
    public CompletableFuture<TechnicalAnalysisResult> analyze(String stockCode, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("技术分析师开始分析股票: {}", stockCode);
                
                // 1. 获取K线数据（最近120个交易日）
                List<KLineData> klineData = stockDataService.getKLineData(stockCode, "daily", 120).get();
                if (klineData.isEmpty()) {
                    return createErrorResult("无法获取K线数据");
                }
                
                // 按时间排序
                klineData.sort(Comparator.comparing(KLineData::getDate));
                
                // 2. 计算技术指标
                TechnicalIndicators indicators = calculateTechnicalIndicators(klineData);
                
                // 3. K线形态分析
                PatternAnalysis patternAnalysis = analyzePatterns(klineData);
                
                // 4. 趋势分析
                TrendAnalysis trendAnalysis = analyzeTrend(klineData, indicators);
                
                // 5. 支撑阻力位分析
                SupportResistanceAnalysis srAnalysis = analyzeSupportResistance(klineData);
                
                // 6. 买卖信号分析
                TradingSignals tradingSignals = analyzeTradingSignals(indicators, patternAnalysis, trendAnalysis);
                
                // 7. 生成LLM分析
                String llmAnalysis = generateLLMAnalysis(stockCode, klineData, indicators, 
                                                       patternAnalysis, trendAnalysis, tradingSignals);
                
                // 8. 计算技术评分
                double technicalScore = calculateTechnicalScore(indicators, trendAnalysis, tradingSignals);
                
                // 9. 生成操作建议
                String recommendation = generateRecommendation(technicalScore, tradingSignals);
                
                return TechnicalAnalysisResult.builder()
                        .stockCode(stockCode)
                        .agentId("technical_analyst")
                        .agentName("技术分析师")
                        .analysis(llmAnalysis)
                        .indicators(indicators)
                        .patternAnalysis(patternAnalysis)
                        .trendAnalysis(trendAnalysis)
                        .supportResistance(srAnalysis)
                        .tradingSignals(tradingSignals)
                        .technicalScore(technicalScore)
                        .recommendation(recommendation)
                        .confidence(calculateConfidence(indicators, trendAnalysis))
                        .timestamp(System.currentTimeMillis())
                        .build();
                
            } catch (Exception e) {
                log.error("技术分析失败: {}", stockCode, e);
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
            
            // 获取技术分析结果
            TechnicalAnalysisResult analysisResult = (TechnicalAnalysisResult) context.getAgentResult("technical_analyst");
            
            String prompt = buildDebatePrompt(currentRound, analysisResult, previousArguments);
            
            // 调用LLM生成辩论论据
            String argument = callLLM(prompt);
            
            return FundamentalAnalyst.DebateArgument.builder()
                    .agentId("technical_analyst")
                    .agentName("技术分析师")
                    .round(currentRound)
                    .argument(argument)
                    .confidence(analysisResult.getConfidence())
                    .evidenceType("TECHNICAL")
                    .supportingData(Map.of(
                        "indicators", analysisResult.getIndicators(),
                        "trendAnalysis", analysisResult.getTrendAnalysis(),
                        "tradingSignals", analysisResult.getTradingSignals(),
                        "technicalScore", analysisResult.getTechnicalScore()
                    ))
                    .timestamp(System.currentTimeMillis())
                    .build();
                    
        } catch (Exception e) {
            log.error("技术分析师辩论失败", e);
            return createErrorDebateArgument("辩论过程中发生错误");
        }
    }
    
    /**
     * 计算技术指标
     */
    private TechnicalIndicators calculateTechnicalIndicators(List<KLineData> klineData) {
        List<Double> closePrices = klineData.stream()
                .map(KLineData::getClosePrice)
                .collect(Collectors.toList());
        
        List<Double> highPrices = klineData.stream()
                .map(KLineData::getHighPrice)
                .collect(Collectors.toList());
        
        List<Double> lowPrices = klineData.stream()
                .map(KLineData::getLowPrice)
                .collect(Collectors.toList());
        
        List<Long> volumes = klineData.stream()
                .map(KLineData::getVolume)
                .collect(Collectors.toList());
        
        return TechnicalIndicators.builder()
                .ma5(calculateMA(closePrices, 5))
                .ma10(calculateMA(closePrices, 10))
                .ma20(calculateMA(closePrices, 20))
                .ma60(calculateMA(closePrices, 60))
                .ema12(calculateEMA(closePrices, 12))
                .ema26(calculateEMA(closePrices, 26))
                .macd(calculateMACD(closePrices))
                .rsi6(calculateRSI(closePrices, 6))
                .rsi12(calculateRSI(closePrices, 12))
                .rsi24(calculateRSI(closePrices, 24))
                .kdj(calculateKDJ(highPrices, lowPrices, closePrices))
                .boll(calculateBOLL(closePrices, 20))
                .cci(calculateCCI(highPrices, lowPrices, closePrices, 14))
                .williamR(calculateWilliamR(highPrices, lowPrices, closePrices, 14))
                .volumeMA5(calculateMA(volumes.stream().map(Long::doubleValue).collect(Collectors.toList()), 5))
                .volumeMA10(calculateMA(volumes.stream().map(Long::doubleValue).collect(Collectors.toList()), 10))
                .build();
    }
    
    /**
     * K线形态分析
     */
    private PatternAnalysis analyzePatterns(List<KLineData> klineData) {
        List<String> patterns = new ArrayList<>();
        
        // 分析最近的K线形态
        int size = klineData.size();
        if (size >= 3) {
            KLineData current = klineData.get(size - 1);
            KLineData prev1 = klineData.get(size - 2);
            KLineData prev2 = klineData.get(size - 3);
            
            // 十字星
            if (isDoji(current)) {
                patterns.add("十字星");
            }
            
            // 锤子线
            if (isHammer(current)) {
                patterns.add("锤子线");
            }
            
            // 上吊线
            if (isHangingMan(current)) {
                patterns.add("上吊线");
            }
            
            // 三连阳/三连阴
            if (isThreeWhiteSoldiers(current, prev1, prev2)) {
                patterns.add("三连阳");
            } else if (isThreeBlackCrows(current, prev1, prev2)) {
                patterns.add("三连阴");
            }
            
            // 早晨之星/黄昏之星
            if (isMorningStar(current, prev1, prev2)) {
                patterns.add("早晨之星");
            } else if (isEveningStar(current, prev1, prev2)) {
                patterns.add("黄昏之星");
            }
        }
        
        String patternSignal = determinePatternSignal(patterns);
        
        return PatternAnalysis.builder()
                .patterns(patterns)
                .patternSignal(patternSignal)
                .patternStrength(calculatePatternStrength(patterns))
                .build();
    }
    
    /**
     * 趋势分析
     */
    private TrendAnalysis analyzeTrend(List<KLineData> klineData, TechnicalIndicators indicators) {
        // 短期趋势（5日均线）
        String shortTrend = determineTrend(indicators.getMa5());
        
        // 中期趋势（20日均线）
        String mediumTrend = determineTrend(indicators.getMa20());
        
        // 长期趋势（60日均线）
        String longTrend = determineTrend(indicators.getMa60());
        
        // 均线排列
        String maAlignment = analyzeMAAlignment(indicators);
        
        // 趋势强度
        double trendStrength = calculateTrendStrength(klineData, indicators);
        
        // 综合趋势判断
        String overallTrend = determineOverallTrend(shortTrend, mediumTrend, longTrend, maAlignment);
        
        return TrendAnalysis.builder()
                .shortTrend(shortTrend)
                .mediumTrend(mediumTrend)
                .longTrend(longTrend)
                .overallTrend(overallTrend)
                .maAlignment(maAlignment)
                .trendStrength(trendStrength)
                .build();
    }
    
    /**
     * 支撑阻力位分析
     */
    private SupportResistanceAnalysis analyzeSupportResistance(List<KLineData> klineData) {
        List<Double> highs = klineData.stream().map(KLineData::getHighPrice).collect(Collectors.toList());
        List<Double> lows = klineData.stream().map(KLineData::getLowPrice).collect(Collectors.toList());
        
        // 计算支撑位（最近的低点）
        List<Double> supportLevels = findSupportLevels(lows);
        
        // 计算阻力位（最近的高点）
        List<Double> resistanceLevels = findResistanceLevels(highs);
        
        double currentPrice = klineData.get(klineData.size() - 1).getClosePrice();
        
        return SupportResistanceAnalysis.builder()
                .supportLevels(supportLevels)
                .resistanceLevels(resistanceLevels)
                .nearestSupport(findNearestSupport(supportLevels, currentPrice))
                .nearestResistance(findNearestResistance(resistanceLevels, currentPrice))
                .currentPrice(currentPrice)
                .build();
    }
    
    /**
     * 交易信号分析
     */
    private TradingSignals analyzeTradingSignals(TechnicalIndicators indicators, 
                                                PatternAnalysis patternAnalysis, 
                                                TrendAnalysis trendAnalysis) {
        List<String> buySignals = new ArrayList<>();
        List<String> sellSignals = new ArrayList<>();
        
        // MACD信号
        if (indicators.getMacd() != null && !indicators.getMacd().isEmpty()) {
            MACDData latestMACD = indicators.getMacd().get(indicators.getMacd().size() - 1);
            if (latestMACD.getDif() > latestMACD.getDea() && latestMACD.getHistogram() > 0) {
                buySignals.add("MACD金叉");
            } else if (latestMACD.getDif() < latestMACD.getDea() && latestMACD.getHistogram() < 0) {
                sellSignals.add("MACD死叉");
            }
        }
        
        // RSI信号
        if (indicators.getRsi12() != null && !indicators.getRsi12().isEmpty()) {
            double latestRSI = indicators.getRsi12().get(indicators.getRsi12().size() - 1);
            if (latestRSI < 30) {
                buySignals.add("RSI超卖");
            } else if (latestRSI > 70) {
                sellSignals.add("RSI超买");
            }
        }
        
        // KDJ信号
        if (indicators.getKdj() != null && !indicators.getKdj().isEmpty()) {
            KDJData latestKDJ = indicators.getKdj().get(indicators.getKdj().size() - 1);
            if (latestKDJ.getK() < 20 && latestKDJ.getD() < 20) {
                buySignals.add("KDJ超卖");
            } else if (latestKDJ.getK() > 80 && latestKDJ.getD() > 80) {
                sellSignals.add("KDJ超买");
            }
        }
        
        // 均线信号
        if ("多头排列".equals(trendAnalysis.getMaAlignment())) {
            buySignals.add("均线多头排列");
        } else if ("空头排列".equals(trendAnalysis.getMaAlignment())) {
            sellSignals.add("均线空头排列");
        }
        
        // 形态信号
        if ("看涨".equals(patternAnalysis.getPatternSignal())) {
            buySignals.add("看涨形态");
        } else if ("看跌".equals(patternAnalysis.getPatternSignal())) {
            sellSignals.add("看跌形态");
        }
        
        // 综合信号强度
        double signalStrength = calculateSignalStrength(buySignals, sellSignals);
        
        // 操作建议
        String action = determineAction(buySignals, sellSignals, signalStrength);
        
        return TradingSignals.builder()
                .buySignals(buySignals)
                .sellSignals(sellSignals)
                .signalStrength(signalStrength)
                .action(action)
                .build();
    }
    
    /**
     * 生成LLM分析
     */
    private String generateLLMAnalysis(String stockCode, List<KLineData> klineData, 
                                     TechnicalIndicators indicators, PatternAnalysis patternAnalysis,
                                     TrendAnalysis trendAnalysis, TradingSignals tradingSignals) {
        
        KLineData latestData = klineData.get(klineData.size() - 1);
        
        String prompt = String.format("""
            作为专业的技术分析师，请基于以下技术数据对股票 %s 进行深度分析：
            
            最新价格信息：
            - 收盘价：%.2f 元
            - 涨跌幅：%.2f%%
            - 成交量：%d 手
            
            技术指标：
            - MA5：%.2f，MA20：%.2f，MA60：%.2f
            - RSI(12)：%.2f
            - MACD：DIF=%.2f，DEA=%.2f
            
            趋势分析：
            - 短期趋势：%s
            - 中期趋势：%s
            - 长期趋势：%s
            - 均线排列：%s
            - 趋势强度：%.2f
            
            K线形态：
            - 识别形态：%s
            - 形态信号：%s
            
            交易信号：
            - 买入信号：%s
            - 卖出信号：%s
            - 操作建议：%s
            
            请从以下角度进行分析：
            1. 当前技术面强弱判断
            2. 关键技术指标解读
            3. 趋势延续性分析
            4. 重要支撑阻力位
            5. 短期操作策略建议
            
            请提供专业、客观的技术分析意见，字数控制在500字以内。
            """, 
            stockCode,
            latestData.getClosePrice(),
            ((latestData.getClosePrice() - latestData.getOpenPrice()) / latestData.getOpenPrice()) * 100,
            latestData.getVolume(),
            indicators.getMa5() != null && !indicators.getMa5().isEmpty() ? 
                indicators.getMa5().get(indicators.getMa5().size() - 1) : 0.0,
            indicators.getMa20() != null && !indicators.getMa20().isEmpty() ? 
                indicators.getMa20().get(indicators.getMa20().size() - 1) : 0.0,
            indicators.getMa60() != null && !indicators.getMa60().isEmpty() ? 
                indicators.getMa60().get(indicators.getMa60().size() - 1) : 0.0,
            indicators.getRsi12() != null && !indicators.getRsi12().isEmpty() ? 
                indicators.getRsi12().get(indicators.getRsi12().size() - 1) : 0.0,
            indicators.getMacd() != null && !indicators.getMacd().isEmpty() ? 
                indicators.getMacd().get(indicators.getMacd().size() - 1).getDif() : 0.0,
            indicators.getMacd() != null && !indicators.getMacd().isEmpty() ? 
                indicators.getMacd().get(indicators.getMacd().size() - 1).getDea() : 0.0,
            trendAnalysis.getShortTrend(),
            trendAnalysis.getMediumTrend(),
            trendAnalysis.getLongTrend(),
            trendAnalysis.getMaAlignment(),
            trendAnalysis.getTrendStrength(),
            String.join(", ", patternAnalysis.getPatterns()),
            patternAnalysis.getPatternSignal(),
            String.join(", ", tradingSignals.getBuySignals()),
            String.join(", ", tradingSignals.getSellSignals()),
            tradingSignals.getAction()
        );
        
        return callLLM(prompt);
    }
    
    /**
     * 构建辩论提示词
     */
    private String buildDebatePrompt(int round, TechnicalAnalysisResult analysisResult, 
                                   List<FundamentalAnalyst.DebateArgument> previousArguments) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append(String.format("""
            作为技术分析师，你正在参与第%d轮投资决策辩论。
            
            你的分析结果：
            - 技术评分：%.2f/10
            - 操作建议：%s
            - 置信度：%.2f
            - 趋势判断：%s
            
            """, round, analysisResult.getTechnicalScore(), 
            analysisResult.getRecommendation(), analysisResult.getConfidence(),
            analysisResult.getTrendAnalysis().getOverallTrend()));
        
        if (round == 1) {
            prompt.append("""
                第1轮：请阐述你的核心技术观点
                要求：
                1. 基于技术指标和图表形态提出明确观点
                2. 列举3-5个关键的技术信号
                3. 说明你的技术分析逻辑
                4. 控制在200字以内
                """);
        } else if (round == 2) {
            prompt.append("\n其他分析师的观点：\n");
            for (FundamentalAnalyst.DebateArgument arg : previousArguments) {
                if (!"technical_analyst".equals(arg.getAgentId())) {
                    prompt.append(String.format("- %s: %s\n", arg.getAgentName(), arg.getArgument()));
                }
            }
            prompt.append("""
                \n第2轮：请从技术面角度质疑其他观点
                要求：
                1. 指出其他观点在技术面的不足
                2. 用技术指标和图表支撑你的反驳
                3. 强化你的技术观点
                4. 控制在200字以内
                """);
        } else if (round == 3) {
            prompt.append("""
                第3轮：技术面与基本面的平衡建议
                要求：
                1. 综合技术面和基本面观点
                2. 提出平衡的操作策略
                3. 明确技术风险点
                4. 控制在200字以内
                """);
        }
        
        return prompt.toString();
    }
    
    // 技术指标计算方法
    
    private List<Double> calculateMA(List<Double> prices, int period) {
        List<Double> ma = new ArrayList<>();
        for (int i = period - 1; i < prices.size(); i++) {
            double sum = 0;
            for (int j = i - period + 1; j <= i; j++) {
                sum += prices.get(j);
            }
            ma.add(sum / period);
        }
        return ma;
    }
    
    private List<Double> calculateEMA(List<Double> prices, int period) {
        List<Double> ema = new ArrayList<>();
        if (prices.isEmpty()) return ema;
        
        double multiplier = 2.0 / (period + 1);
        ema.add(prices.get(0)); // 第一个值
        
        for (int i = 1; i < prices.size(); i++) {
            double value = (prices.get(i) * multiplier) + (ema.get(i - 1) * (1 - multiplier));
            ema.add(value);
        }
        return ema;
    }
    
    private List<MACDData> calculateMACD(List<Double> prices) {
        List<Double> ema12 = calculateEMA(prices, 12);
        List<Double> ema26 = calculateEMA(prices, 26);
        List<MACDData> macd = new ArrayList<>();
        
        // 计算DIF
        List<Double> dif = new ArrayList<>();
        int startIndex = Math.max(0, ema26.size() - ema12.size());
        for (int i = startIndex; i < ema12.size(); i++) {
            dif.add(ema12.get(i) - ema26.get(i - startIndex));
        }
        
        // 计算DEA (DIF的9日EMA)
        List<Double> dea = calculateEMA(dif, 9);
        
        // 计算MACD柱状图
        for (int i = 0; i < dea.size(); i++) {
            double difValue = i < dif.size() ? dif.get(i) : 0;
            double deaValue = dea.get(i);
            double histogram = (difValue - deaValue) * 2;
            
            macd.add(MACDData.builder()
                    .dif(difValue)
                    .dea(deaValue)
                    .histogram(histogram)
                    .build());
        }
        
        return macd;
    }
    
    private List<Double> calculateRSI(List<Double> prices, int period) {
        List<Double> rsi = new ArrayList<>();
        if (prices.size() < period + 1) return rsi;
        
        for (int i = period; i < prices.size(); i++) {
            double gain = 0, loss = 0;
            
            for (int j = i - period + 1; j <= i; j++) {
                double change = prices.get(j) - prices.get(j - 1);
                if (change > 0) {
                    gain += change;
                } else {
                    loss -= change;
                }
            }
            
            double avgGain = gain / period;
            double avgLoss = loss / period;
            
            double rs = avgLoss == 0 ? 100 : avgGain / avgLoss;
            double rsiValue = 100 - (100 / (1 + rs));
            rsi.add(rsiValue);
        }
        
        return rsi;
    }
    
    private List<KDJData> calculateKDJ(List<Double> highs, List<Double> lows, List<Double> closes) {
        List<KDJData> kdj = new ArrayList<>();
        int period = 9;
        
        if (closes.size() < period) return kdj;
        
        double k = 50, d = 50; // 初始值
        
        for (int i = period - 1; i < closes.size(); i++) {
            // 计算RSV
            double highest = highs.subList(i - period + 1, i + 1).stream().mapToDouble(Double::doubleValue).max().orElse(0);
            double lowest = lows.subList(i - period + 1, i + 1).stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double close = closes.get(i);
            
            double rsv = highest == lowest ? 0 : (close - lowest) / (highest - lowest) * 100;
            
            // 计算K、D、J
            k = (2.0 / 3.0) * k + (1.0 / 3.0) * rsv;
            d = (2.0 / 3.0) * d + (1.0 / 3.0) * k;
            double j = 3 * k - 2 * d;
            
            kdj.add(KDJData.builder()
                    .k(k)
                    .d(d)
                    .j(j)
                    .build());
        }
        
        return kdj;
    }
    
    private BOLLData calculateBOLL(List<Double> prices, int period) {
        if (prices.size() < period) return null;
        
        List<Double> ma = calculateMA(prices, period);
        if (ma.isEmpty()) return null;
        
        double latestMA = ma.get(ma.size() - 1);
        
        // 计算标准差
        double sum = 0;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum += Math.pow(prices.get(i) - latestMA, 2);
        }
        double std = Math.sqrt(sum / period);
        
        return BOLLData.builder()
                .upper(latestMA + 2 * std)
                .middle(latestMA)
                .lower(latestMA - 2 * std)
                .build();
    }
    
    private List<Double> calculateCCI(List<Double> highs, List<Double> lows, List<Double> closes, int period) {
        List<Double> cci = new ArrayList<>();
        
        for (int i = period - 1; i < closes.size(); i++) {
            // 计算典型价格
            List<Double> typicalPrices = new ArrayList<>();
            for (int j = i - period + 1; j <= i; j++) {
                double tp = (highs.get(j) + lows.get(j) + closes.get(j)) / 3;
                typicalPrices.add(tp);
            }
            
            // 计算移动平均
            double ma = typicalPrices.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            
            // 计算平均绝对偏差
            double mad = typicalPrices.stream().mapToDouble(tp -> Math.abs(tp - ma)).average().orElse(0);
            
            // 计算CCI
            double currentTP = typicalPrices.get(typicalPrices.size() - 1);
            double cciValue = mad == 0 ? 0 : (currentTP - ma) / (0.015 * mad);
            cci.add(cciValue);
        }
        
        return cci;
    }
    
    private List<Double> calculateWilliamR(List<Double> highs, List<Double> lows, List<Double> closes, int period) {
        List<Double> williamR = new ArrayList<>();
        
        for (int i = period - 1; i < closes.size(); i++) {
            double highest = highs.subList(i - period + 1, i + 1).stream().mapToDouble(Double::doubleValue).max().orElse(0);
            double lowest = lows.subList(i - period + 1, i + 1).stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double close = closes.get(i);
            
            double wr = highest == lowest ? 0 : (highest - close) / (highest - lowest) * (-100);
            williamR.add(wr);
        }
        
        return williamR;
    }
    
    // K线形态识别方法
    
    private boolean isDoji(KLineData kline) {
        double body = Math.abs(kline.getClosePrice() - kline.getOpenPrice());
        double range = kline.getHighPrice() - kline.getLowPrice();
        return range > 0 && body / range < 0.1;
    }
    
    private boolean isHammer(KLineData kline) {
        double body = Math.abs(kline.getClosePrice() - kline.getOpenPrice());
        double lowerShadow = Math.min(kline.getOpenPrice(), kline.getClosePrice()) - kline.getLowPrice();
        double upperShadow = kline.getHighPrice() - Math.max(kline.getOpenPrice(), kline.getClosePrice());
        
        return lowerShadow > 2 * body && upperShadow < body;
    }
    
    private boolean isHangingMan(KLineData kline) {
        return isHammer(kline); // 形态相同，位置不同
    }
    
    private boolean isThreeWhiteSoldiers(KLineData k1, KLineData k2, KLineData k3) {
        return k1.getClosePrice() > k1.getOpenPrice() &&
               k2.getClosePrice() > k2.getOpenPrice() &&
               k3.getClosePrice() > k3.getOpenPrice() &&
               k2.getClosePrice() > k1.getClosePrice() &&
               k3.getClosePrice() > k2.getClosePrice();
    }
    
    private boolean isThreeBlackCrows(KLineData k1, KLineData k2, KLineData k3) {
        return k1.getClosePrice() < k1.getOpenPrice() &&
               k2.getClosePrice() < k2.getOpenPrice() &&
               k3.getClosePrice() < k3.getOpenPrice() &&
               k2.getClosePrice() < k1.getClosePrice() &&
               k3.getClosePrice() < k2.getClosePrice();
    }
    
    private boolean isMorningStar(KLineData k1, KLineData k2, KLineData k3) {
        return k1.getClosePrice() < k1.getOpenPrice() && // 第一根阴线
               Math.abs(k2.getClosePrice() - k2.getOpenPrice()) < 
               Math.abs(k1.getClosePrice() - k1.getOpenPrice()) * 0.3 && // 第二根十字星或小实体
               k3.getClosePrice() > k3.getOpenPrice() && // 第三根阳线
               k3.getClosePrice() > (k1.getOpenPrice() + k1.getClosePrice()) / 2; // 阳线收盘价超过第一根阴线实体中点
    }
    
    private boolean isEveningStar(KLineData k1, KLineData k2, KLineData k3) {
        return k1.getClosePrice() > k1.getOpenPrice() && // 第一根阳线
               Math.abs(k2.getClosePrice() - k2.getOpenPrice()) < 
               Math.abs(k1.getClosePrice() - k1.getOpenPrice()) * 0.3 && // 第二根十字星或小实体
               k3.getClosePrice() < k3.getOpenPrice() && // 第三根阴线
               k3.getClosePrice() < (k1.getOpenPrice() + k1.getClosePrice()) / 2; // 阴线收盘价低于第一根阳线实体中点
    }
    
    // 工具方法
    
    private String determinePatternSignal(List<String> patterns) {
        long bullishCount = patterns.stream().filter(p -> 
            p.contains("锤子") || p.contains("三连阳") || p.contains("早晨之星")
        ).count();
        
        long bearishCount = patterns.stream().filter(p -> 
            p.contains("上吊") || p.contains("三连阴") || p.contains("黄昏之星")
        ).count();
        
        if (bullishCount > bearishCount) return "看涨";
        if (bearishCount > bullishCount) return "看跌";
        return "中性";
    }
    
    private double calculatePatternStrength(List<String> patterns) {
        return Math.min(patterns.size() * 0.2, 1.0);
    }
    
    private String determineTrend(List<Double> ma) {
        if (ma == null || ma.size() < 2) return "不明";
        
        double current = ma.get(ma.size() - 1);
        double previous = ma.get(ma.size() - 2);
        
        if (current > previous * 1.01) return "上涨";
        if (current < previous * 0.99) return "下跌";
        return "震荡";
    }
    
    private String analyzeMAAlignment(TechnicalIndicators indicators) {
        if (indicators.getMa5() == null || indicators.getMa20() == null || indicators.getMa60() == null ||
            indicators.getMa5().isEmpty() || indicators.getMa20().isEmpty() || indicators.getMa60().isEmpty()) {
            return "数据不足";
        }
        
        double ma5 = indicators.getMa5().get(indicators.getMa5().size() - 1);
        double ma20 = indicators.getMa20().get(indicators.getMa20().size() - 1);
        double ma60 = indicators.getMa60().get(indicators.getMa60().size() - 1);
        
        if (ma5 > ma20 && ma20 > ma60) return "多头排列";
        if (ma5 < ma20 && ma20 < ma60) return "空头排列";
        return "交叉状态";
    }
    
    private double calculateTrendStrength(List<KLineData> klineData, TechnicalIndicators indicators) {
        // 简化的趋势强度计算
        if (klineData.size() < 10) return 0.5;
        
        double priceChange = (klineData.get(klineData.size() - 1).getClosePrice() - 
                            klineData.get(klineData.size() - 10).getClosePrice()) / 
                            klineData.get(klineData.size() - 10).getClosePrice();
        
        return Math.min(Math.abs(priceChange) * 10, 1.0);
    }
    
    private String determineOverallTrend(String shortTrend, String mediumTrend, String longTrend, String maAlignment) {
        if ("多头排列".equals(maAlignment) && "上涨".equals(shortTrend)) return "强势上涨";
        if ("空头排列".equals(maAlignment) && "下跌".equals(shortTrend)) return "强势下跌";
        if ("上涨".equals(shortTrend) && "上涨".equals(mediumTrend)) return "上涨";
        if ("下跌".equals(shortTrend) && "下跌".equals(mediumTrend)) return "下跌";
        return "震荡";
    }
    
    private List<Double> findSupportLevels(List<Double> lows) {
        // 简化实现：找最近的几个低点
        return lows.stream()
                .sorted()
                .limit(3)
                .collect(Collectors.toList());
    }
    
    private List<Double> findResistanceLevels(List<Double> highs) {
        // 简化实现：找最近的几个高点
        return highs.stream()
                .sorted(Comparator.reverseOrder())
                .limit(3)
                .collect(Collectors.toList());
    }
    
    private Double findNearestSupport(List<Double> supportLevels, double currentPrice) {
        return supportLevels.stream()
                .filter(level -> level < currentPrice)
                .max(Double::compareTo)
                .orElse(null);
    }
    
    private Double findNearestResistance(List<Double> resistanceLevels, double currentPrice) {
        return resistanceLevels.stream()
                .filter(level -> level > currentPrice)
                .min(Double::compareTo)
                .orElse(null);
    }
    
    private double calculateSignalStrength(List<String> buySignals, List<String> sellSignals) {
        int netSignals = buySignals.size() - sellSignals.size();
        return Math.max(-1.0, Math.min(1.0, netSignals * 0.2));
    }
    
    private String determineAction(List<String> buySignals, List<String> sellSignals, double signalStrength) {
        if (signalStrength > 0.6) return "强烈买入";
        if (signalStrength > 0.2) return "买入";
        if (signalStrength > -0.2) return "观望";
        if (signalStrength > -0.6) return "卖出";
        return "强烈卖出";
    }
    
    private double calculateTechnicalScore(TechnicalIndicators indicators, TrendAnalysis trendAnalysis, TradingSignals tradingSignals) {
        double score = 5.0; // 基础分
        
        // 趋势评分
        switch (trendAnalysis.getOverallTrend()) {
            case "强势上涨" -> score += 2.0;
            case "上涨" -> score += 1.0;
            case "强势下跌" -> score -= 2.0;
            case "下跌" -> score -= 1.0;
        }
        
        // 信号评分
        score += tradingSignals.getSignalStrength() * 2;
        
        // 趋势强度评分
        score += trendAnalysis.getTrendStrength();
        
        return Math.max(0, Math.min(10, score));
    }
    
    private String generateRecommendation(double technicalScore, TradingSignals tradingSignals) {
        if (technicalScore >= 8.0 && "强烈买入".equals(tradingSignals.getAction())) {
            return "强烈买入";
        } else if (technicalScore >= 7.0) {
            return "买入";
        } else if (technicalScore >= 5.0) {
            return "持有";
        } else if (technicalScore >= 3.0) {
            return "卖出";
        } else {
            return "强烈卖出";
        }
    }
    
    private double calculateConfidence(TechnicalIndicators indicators, TrendAnalysis trendAnalysis) {
        double confidence = 0.5; // 基础置信度
        
        // 数据完整性
        if (indicators.getMa5() != null && !indicators.getMa5().isEmpty()) confidence += 0.1;
        if (indicators.getMacd() != null && !indicators.getMacd().isEmpty()) confidence += 0.1;
        if (indicators.getRsi12() != null && !indicators.getRsi12().isEmpty()) confidence += 0.1;
        
        // 趋势一致性
        if ("多头排列".equals(trendAnalysis.getMaAlignment()) || "空头排列".equals(trendAnalysis.getMaAlignment())) {
            confidence += 0.2;
        }
        
        return Math.min(1.0, confidence);
    }
    
    private TechnicalAnalysisResult createErrorResult(String errorMessage) {
        return TechnicalAnalysisResult.builder()
                .agentId("technical_analyst")
                .agentName("技术分析师")
                .analysis("分析失败: " + errorMessage)
                .technicalScore(0.0)
                .recommendation("无法分析")
                .confidence(0.0)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    private FundamentalAnalyst.DebateArgument createErrorDebateArgument(String errorMessage) {
        return FundamentalAnalyst.DebateArgument.builder()
                .agentId("technical_analyst")
                .agentName("技术分析师")
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
    public static class TechnicalAnalysisResult {
        private String stockCode;
        private String agentId;
        private String agentName;
        private String analysis;
        private TechnicalIndicators indicators;
        private PatternAnalysis patternAnalysis;
        private TrendAnalysis trendAnalysis;
        private SupportResistanceAnalysis supportResistance;
        private TradingSignals tradingSignals;
        private Double technicalScore;
        private String recommendation;
        private Double confidence;
        private Long timestamp;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TechnicalIndicators {
        private List<Double> ma5;
        private List<Double> ma10;
        private List<Double> ma20;
        private List<Double> ma60;
        private List<Double> ema12;
        private List<Double> ema26;
        private List<MACDData> macd;
        private List<Double> rsi6;
        private List<Double> rsi12;
        private List<Double> rsi24;
        private List<KDJData> kdj;
        private BOLLData boll;
        private List<Double> cci;
        private List<Double> williamR;
        private List<Double> volumeMA5;
        private List<Double> volumeMA10;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MACDData {
        private Double dif;
        private Double dea;
        private Double histogram;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class KDJData {
        private Double k;
        private Double d;
        private Double j;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BOLLData {
        private Double upper;
        private Double middle;
        private Double lower;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PatternAnalysis {
        private List<String> patterns;
        private String patternSignal;
        private Double patternStrength;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TrendAnalysis {
        private String shortTrend;
        private String mediumTrend;
        private String longTrend;
        private String overallTrend;
        private String maAlignment;
        private Double trendStrength;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SupportResistanceAnalysis {
        private List<Double> supportLevels;
        private List<Double> resistanceLevels;
        private Double nearestSupport;
        private Double nearestResistance;
        private Double currentPrice;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TradingSignals {
        private List<String> buySignals;
        private List<String> sellSignals;
        private Double signalStrength;
        private String action;
    }
}