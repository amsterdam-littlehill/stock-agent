package com.jd.genie.agent.agent.stock;

import com.jd.genie.agent.enums.RoleType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 技术分析师智能体
 * 专注于股票的技术指标分析、图形模式识别和价格趋势预测
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class TechnicalAnalystAgent extends StockAnalysisAgent {

    private static final String AGENT_NAME = "技术分析师";
    private static final String AGENT_DESCRIPTION = "专业的技术分析师，擅长通过技术指标和图表模式分析股票价格走势";
    
    // 技术分析参数
    private int shortMaPeriod = 5;   // 短期均线周期
    private int longMaPeriod = 20;   // 长期均线周期
    private int rsiPeriod = 14;      // RSI周期
    private int macdFastPeriod = 12; // MACD快线周期
    private int macdSlowPeriod = 26; // MACD慢线周期
    private int macdSignalPeriod = 9; // MACD信号线周期

    public TechnicalAnalystAgent() {
        setName(AGENT_NAME);
        setDescription(AGENT_DESCRIPTION);
        setAnalysisType("技术分析");
        
        // 设置系统提示词
        setSystemPrompt(buildSystemPrompt());
    }

    @Override
    public StockAnalysisResult performAnalysis(String stockCode, Map<String, Object> parameters) {
        try {
            log.info("{} 开始对股票 {} 进行技术分析", getName(), stockCode);
            
            long startTime = System.currentTimeMillis();
            
            // 1. 获取股票数据
            Map<String, Object> stockData = getStockData(stockCode, parameters);
            if (stockData == null || stockData.isEmpty()) {
                throw new RuntimeException("无法获取股票数据");
            }
            
            // 2. 计算技术指标
            Map<String, Object> technicalIndicators = calculateTechnicalIndicators(stockData);
            
            // 3. 分析趋势
            String trendAnalysis = analyzeTrend(technicalIndicators);
            
            // 4. 识别支撑阻力位
            List<Double> supportLevels = identifySupportLevels(stockData);
            List<Double> resistanceLevels = identifyResistanceLevels(stockData);
            
            // 5. 生成交易信号
            String tradingSignal = generateTradingSignal(technicalIndicators);
            
            // 6. 计算置信度
            double confidence = calculateConfidence(technicalIndicators, trendAnalysis);
            
            // 7. 构建分析结果
            StockAnalysisResult result = StockAnalysisResult.builder()
                    .stockCode(stockCode)
                    .stockName(getStockName(stockCode))
                    .analysisType("技术分析")
                    .analysisTime(LocalDateTime.now())
                    .conclusion(buildConclusion(trendAnalysis, tradingSignal))
                    .recommendation(generateRecommendation(tradingSignal, confidence))
                    .riskLevel(assessRiskLevel(technicalIndicators))
                    .confidenceScore(confidence)
                    .targetPrice(calculateTargetPrice(stockData, technicalIndicators))
                    .stopLossPrice(calculateStopLoss(stockData, technicalIndicators))
                    .supportLevels(supportLevels)
                    .resistanceLevels(resistanceLevels)
                    .technicalIndicators(technicalIndicators)
                    .analystId(getName())
                    .analysisTimeMs(System.currentTimeMillis() - startTime)
                    .build();
            
            // 8. 添加关键要点
            addKeyPoints(result, technicalIndicators, trendAnalysis);
            
            // 9. 添加数据来源
            result.addDataSource("技术指标计算");
            result.addDataSource("价格走势分析");
            
            log.info("{} 完成技术分析，置信度: {:.2f}", getName(), confidence);
            
            return result;
            
        } catch (Exception e) {
            log.error("{} 技术分析失败", getName(), e);
            throw new RuntimeException("技术分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取股票数据
     */
    private Map<String, Object> getStockData(String stockCode, Map<String, Object> parameters) {
        // 这里应该调用实际的数据获取工具
        // 暂时返回模拟数据
        Map<String, Object> data = new HashMap<>();
        
        // 模拟K线数据
        List<Map<String, Double>> klineData = generateMockKlineData();
        data.put("kline", klineData);
        data.put("current_price", 25.68);
        data.put("volume", 1250000L);
        data.put("timeframe", parameters.getOrDefault("timeframe", "1d"));
        
        return data;
    }

    /**
     * 计算技术指标
     */
    private Map<String, Object> calculateTechnicalIndicators(Map<String, Object> stockData) {
        Map<String, Object> indicators = new HashMap<>();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Double>> klineData = (List<Map<String, Double>>) stockData.get("kline");
        
        if (klineData == null || klineData.size() < longMaPeriod) {
            throw new RuntimeException("数据不足，无法计算技术指标");
        }
        
        // 计算移动平均线
        double shortMA = calculateMA(klineData, shortMaPeriod);
        double longMA = calculateMA(klineData, longMaPeriod);
        indicators.put("MA5", shortMA);
        indicators.put("MA20", longMA);
        
        // 计算RSI
        double rsi = calculateRSI(klineData, rsiPeriod);
        indicators.put("RSI", rsi);
        
        // 计算MACD
        Map<String, Double> macd = calculateMACD(klineData);
        indicators.putAll(macd);
        
        // 计算布林带
        Map<String, Double> bollinger = calculateBollingerBands(klineData, 20, 2.0);
        indicators.putAll(bollinger);
        
        // 计算KDJ
        Map<String, Double> kdj = calculateKDJ(klineData, 9, 3, 3);
        indicators.putAll(kdj);
        
        return indicators;
    }

    /**
     * 计算移动平均线
     */
    private double calculateMA(List<Map<String, Double>> klineData, int period) {
        if (klineData.size() < period) {
            return 0.0;
        }
        
        double sum = 0.0;
        for (int i = klineData.size() - period; i < klineData.size(); i++) {
            sum += klineData.get(i).get("close");
        }
        
        return sum / period;
    }

    /**
     * 计算RSI
     */
    private double calculateRSI(List<Map<String, Double>> klineData, int period) {
        if (klineData.size() < period + 1) {
            return 50.0; // 默认中性值
        }
        
        double gainSum = 0.0;
        double lossSum = 0.0;
        
        for (int i = klineData.size() - period; i < klineData.size(); i++) {
            double change = klineData.get(i).get("close") - klineData.get(i - 1).get("close");
            if (change > 0) {
                gainSum += change;
            } else {
                lossSum += Math.abs(change);
            }
        }
        
        double avgGain = gainSum / period;
        double avgLoss = lossSum / period;
        
        if (avgLoss == 0) {
            return 100.0;
        }
        
        double rs = avgGain / avgLoss;
        return 100.0 - (100.0 / (1.0 + rs));
    }

    /**
     * 计算MACD
     */
    private Map<String, Double> calculateMACD(List<Map<String, Double>> klineData) {
        Map<String, Double> macd = new HashMap<>();
        
        // 简化的MACD计算
        double ema12 = calculateEMA(klineData, macdFastPeriod);
        double ema26 = calculateEMA(klineData, macdSlowPeriod);
        double dif = ema12 - ema26;
        double dea = dif * 0.8; // 简化计算
        double macdValue = (dif - dea) * 2;
        
        macd.put("MACD_DIF", dif);
        macd.put("MACD_DEA", dea);
        macd.put("MACD", macdValue);
        
        return macd;
    }

    /**
     * 计算EMA
     */
    private double calculateEMA(List<Map<String, Double>> klineData, int period) {
        if (klineData.size() < period) {
            return calculateMA(klineData, klineData.size());
        }
        
        double multiplier = 2.0 / (period + 1);
        double ema = klineData.get(klineData.size() - period).get("close");
        
        for (int i = klineData.size() - period + 1; i < klineData.size(); i++) {
            ema = (klineData.get(i).get("close") * multiplier) + (ema * (1 - multiplier));
        }
        
        return ema;
    }

    /**
     * 计算布林带
     */
    private Map<String, Double> calculateBollingerBands(List<Map<String, Double>> klineData, int period, double stdDev) {
        Map<String, Double> bollinger = new HashMap<>();
        
        double ma = calculateMA(klineData, period);
        double variance = 0.0;
        
        for (int i = klineData.size() - period; i < klineData.size(); i++) {
            double diff = klineData.get(i).get("close") - ma;
            variance += diff * diff;
        }
        
        double standardDeviation = Math.sqrt(variance / period);
        
        bollinger.put("BOLL_MID", ma);
        bollinger.put("BOLL_UPPER", ma + (standardDeviation * stdDev));
        bollinger.put("BOLL_LOWER", ma - (standardDeviation * stdDev));
        
        return bollinger;
    }

    /**
     * 计算KDJ
     */
    private Map<String, Double> calculateKDJ(List<Map<String, Double>> klineData, int period, int k, int d) {
        Map<String, Double> kdj = new HashMap<>();
        
        // 简化的KDJ计算
        double rsv = calculateRSV(klineData, period);
        double kValue = rsv * 0.6 + 50 * 0.4; // 简化计算
        double dValue = kValue * 0.6 + 50 * 0.4;
        double jValue = 3 * kValue - 2 * dValue;
        
        kdj.put("KDJ_K", kValue);
        kdj.put("KDJ_D", dValue);
        kdj.put("KDJ_J", jValue);
        
        return kdj;
    }

    /**
     * 计算RSV
     */
    private double calculateRSV(List<Map<String, Double>> klineData, int period) {
        if (klineData.size() < period) {
            return 50.0;
        }
        
        double highest = Double.MIN_VALUE;
        double lowest = Double.MAX_VALUE;
        
        for (int i = klineData.size() - period; i < klineData.size(); i++) {
            highest = Math.max(highest, klineData.get(i).get("high"));
            lowest = Math.min(lowest, klineData.get(i).get("low"));
        }
        
        double currentClose = klineData.get(klineData.size() - 1).get("close");
        
        if (highest == lowest) {
            return 50.0;
        }
        
        return ((currentClose - lowest) / (highest - lowest)) * 100;
    }

    /**
     * 分析趋势
     */
    private String analyzeTrend(Map<String, Object> indicators) {
        double ma5 = (Double) indicators.get("MA5");
        double ma20 = (Double) indicators.get("MA20");
        double rsi = (Double) indicators.get("RSI");
        double macd = (Double) indicators.get("MACD");
        
        StringBuilder trend = new StringBuilder();
        
        // 均线趋势
        if (ma5 > ma20) {
            trend.append("短期均线上穿长期均线，呈现上升趋势。");
        } else {
            trend.append("短期均线下穿长期均线，呈现下降趋势。");
        }
        
        // RSI分析
        if (rsi > 70) {
            trend.append("RSI超买，可能面临回调压力。");
        } else if (rsi < 30) {
            trend.append("RSI超卖，可能出现反弹机会。");
        } else {
            trend.append("RSI处于正常区间。");
        }
        
        // MACD分析
        if (macd > 0) {
            trend.append("MACD金叉，动能向好。");
        } else {
            trend.append("MACD死叉，动能偏弱。");
        }
        
        return trend.toString();
    }

    /**
     * 生成交易信号
     */
    private String generateTradingSignal(Map<String, Object> indicators) {
        double ma5 = (Double) indicators.get("MA5");
        double ma20 = (Double) indicators.get("MA20");
        double rsi = (Double) indicators.get("RSI");
        double macd = (Double) indicators.get("MACD");
        
        int bullishSignals = 0;
        int bearishSignals = 0;
        
        // 均线信号
        if (ma5 > ma20) bullishSignals++;
        else bearishSignals++;
        
        // RSI信号
        if (rsi > 30 && rsi < 70) bullishSignals++;
        else if (rsi > 70) bearishSignals++;
        else if (rsi < 30) bullishSignals++;
        
        // MACD信号
        if (macd > 0) bullishSignals++;
        else bearishSignals++;
        
        if (bullishSignals > bearishSignals) {
            return "买入信号";
        } else if (bearishSignals > bullishSignals) {
            return "卖出信号";
        } else {
            return "持有信号";
        }
    }

    /**
     * 计算置信度
     */
    private double calculateConfidence(Map<String, Object> indicators, String trendAnalysis) {
        double baseConfidence = 0.6;
        
        // 根据指标一致性调整置信度
        double ma5 = (Double) indicators.get("MA5");
        double ma20 = (Double) indicators.get("MA20");
        double rsi = (Double) indicators.get("RSI");
        double macd = (Double) indicators.get("MACD");
        
        // 指标一致性检查
        boolean macdBullish = macd > 0;
        boolean maBullish = ma5 > ma20;
        boolean rsiNormal = rsi > 30 && rsi < 70;
        
        if (macdBullish == maBullish) {
            baseConfidence += 0.15;
        }
        
        if (rsiNormal) {
            baseConfidence += 0.1;
        }
        
        return Math.min(0.95, baseConfidence);
    }

    /**
     * 生成投资建议
     */
    private String generateRecommendation(String signal, double confidence) {
        if (confidence < 0.6) {
            return "观望";
        }
        
        switch (signal) {
            case "买入信号":
                return confidence > 0.8 ? "强烈买入" : "买入";
            case "卖出信号":
                return confidence > 0.8 ? "强烈卖出" : "卖出";
            default:
                return "持有";
        }
    }

    /**
     * 评估风险等级
     */
    private String assessRiskLevel(Map<String, Object> indicators) {
        double rsi = (Double) indicators.get("RSI");
        
        if (rsi > 80 || rsi < 20) {
            return "高";
        } else if (rsi > 70 || rsi < 30) {
            return "中";
        } else {
            return "低";
        }
    }

    /**
     * 识别支撑位
     */
    private List<Double> identifySupportLevels(Map<String, Object> stockData) {
        List<Double> supports = new ArrayList<>();
        
        // 简化的支撑位计算
        Double currentPrice = (Double) stockData.get("current_price");
        if (currentPrice != null) {
            supports.add(currentPrice * 0.95); // 5%支撑位
            supports.add(currentPrice * 0.90); // 10%支撑位
        }
        
        return supports;
    }

    /**
     * 识别阻力位
     */
    private List<Double> identifyResistanceLevels(Map<String, Object> stockData) {
        List<Double> resistances = new ArrayList<>();
        
        // 简化的阻力位计算
        Double currentPrice = (Double) stockData.get("current_price");
        if (currentPrice != null) {
            resistances.add(currentPrice * 1.05); // 5%阻力位
            resistances.add(currentPrice * 1.10); // 10%阻力位
        }
        
        return resistances;
    }

    /**
     * 计算目标价格
     */
    private Double calculateTargetPrice(Map<String, Object> stockData, Map<String, Object> indicators) {
        Double currentPrice = (Double) stockData.get("current_price");
        if (currentPrice == null) return null;
        
        double ma5 = (Double) indicators.get("MA5");
        double ma20 = (Double) indicators.get("MA20");
        
        // 简化的目标价计算
        if (ma5 > ma20) {
            return currentPrice * 1.08; // 上涨8%
        } else {
            return currentPrice * 0.95; // 下跌5%
        }
    }

    /**
     * 计算止损价格
     */
    private Double calculateStopLoss(Map<String, Object> stockData, Map<String, Object> indicators) {
        Double currentPrice = (Double) stockData.get("current_price");
        if (currentPrice == null) return null;
        
        // 简化的止损价计算
        return currentPrice * 0.92; // 8%止损
    }

    /**
     * 添加关键要点
     */
    private void addKeyPoints(StockAnalysisResult result, Map<String, Object> indicators, String trendAnalysis) {
        result.addKeyPoint("技术指标分析: " + formatIndicators(indicators));
        result.addKeyPoint("趋势判断: " + trendAnalysis);
        result.addKeyPoint("交易建议: 基于多个技术指标的综合判断");
        
        double rsi = (Double) indicators.get("RSI");
        if (rsi > 70) {
            result.addWarning("RSI超买，注意回调风险");
        } else if (rsi < 30) {
            result.addWarning("RSI超卖，关注反弹机会");
        }
    }

    /**
     * 格式化技术指标
     */
    private String formatIndicators(Map<String, Object> indicators) {
        return String.format("MA5: %.2f, MA20: %.2f, RSI: %.2f, MACD: %.2f",
                (Double) indicators.get("MA5"),
                (Double) indicators.get("MA20"),
                (Double) indicators.get("RSI"),
                (Double) indicators.get("MACD"));
    }

    /**
     * 构建分析结论
     */
    private String buildConclusion(String trendAnalysis, String tradingSignal) {
        return String.format("基于技术分析，%s 当前%s。%s", 
                getStockCode(), tradingSignal.toLowerCase(), trendAnalysis);
    }

    /**
     * 获取股票名称
     */
    private String getStockName(String stockCode) {
        // 这里应该从数据源获取股票名称
        return "股票-" + stockCode;
    }

    /**
     * 生成模拟K线数据
     */
    private List<Map<String, Double>> generateMockKlineData() {
        List<Map<String, Double>> klineData = new ArrayList<>();
        Random random = new Random();
        double basePrice = 25.0;
        
        for (int i = 0; i < 30; i++) {
            Map<String, Double> kline = new HashMap<>();
            double open = basePrice + (random.nextGaussian() * 0.5);
            double close = open + (random.nextGaussian() * 0.3);
            double high = Math.max(open, close) + Math.abs(random.nextGaussian() * 0.2);
            double low = Math.min(open, close) - Math.abs(random.nextGaussian() * 0.2);
            
            kline.put("open", open);
            kline.put("high", high);
            kline.put("low", low);
            kline.put("close", close);
            kline.put("volume", 1000000.0 + random.nextDouble() * 500000);
            
            klineData.add(kline);
            basePrice = close;
        }
        
        return klineData;
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        return "你是一位资深的技术分析师，拥有15年的股票技术分析经验。" +
               "你擅长使用各种技术指标（如移动平均线、RSI、MACD、布林带、KDJ等）来分析股票价格走势。" +
               "你能够识别图表模式、支撑阻力位，并基于技术分析给出专业的交易建议。" +
               "请始终保持客观、专业的分析态度，并明确指出分析的局限性和风险。";
    }
}