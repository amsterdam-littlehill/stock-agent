package com.jd.genie.service;

import com.jd.genie.agent.agent.stock.*;
import com.jd.genie.model.dto.StockAnalysisRequest;
import com.jd.genie.model.dto.StockAnalysisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 股票分析服务
 * 整合多智能体协调器，提供完整的股票分析功能
 */
@Slf4j
@Service
public class StockAnalysisService {

    @Autowired
    private StockDataService stockDataService;

    // 分析任务缓存
    private final Map<String, StockAnalysisResponse> analysisCache = new ConcurrentHashMap<>();
    
    // 异步执行器
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    // 智能体实例缓存
    private final Map<String, BaseAgent> agentCache = new ConcurrentHashMap<>();

    /**
     * 执行多智能体股票分析
     */
    public StockAnalysisResponse analyzeStock(StockAnalysisRequest request) {
        String requestId = generateRequestId();
        log.info("开始股票分析: {} - {}", requestId, request.getStockCode());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 验证请求
            validateRequest(request);
            
            // 获取股票基础信息
            StockDataService.StockInfo stockInfo = stockDataService.getStockInfo(request.getStockCode());
            StockDataService.RealTimePrice realTimePrice = stockDataService.getRealTimePrice(request.getStockCode());
            
            // 创建多智能体协调器
            StockMultiAgentCoordinator coordinator = getOrCreateCoordinator(request.getStockCode());
            
            // 设置分析参数
            setupAnalysisContext(coordinator, request, stockInfo, realTimePrice);
            
            // 执行分析
            StockAnalysisResult result = executeAnalysis(coordinator, request);
            
            // 构建响应
            StockAnalysisResponse response = buildResponse(requestId, request, result, startTime);
            
            // 缓存结果
            analysisCache.put(requestId, response);
            
            log.info("股票分析完成: {} - {} - {}ms", requestId, request.getStockCode(), 
                    System.currentTimeMillis() - startTime);
            
            return response;
            
        } catch (Exception e) {
            log.error("股票分析失败: {} - {}", requestId, request.getStockCode(), e);
            return buildErrorResponse(requestId, request, e, startTime);
        }
    }

    /**
     * 异步执行股票分析
     */
    public CompletableFuture<StockAnalysisResponse> analyzeStockAsync(StockAnalysisRequest request) {
        return CompletableFuture.supplyAsync(() -> analyzeStock(request), executorService);
    }

    /**
     * 批量分析股票
     */
    public List<StockAnalysisResponse> batchAnalyzeStocks(List<StockAnalysisRequest> requests) {
        log.info("开始批量股票分析: {} 只股票", requests.size());
        
        List<CompletableFuture<StockAnalysisResponse>> futures = requests.stream()
                .map(this::analyzeStockAsync)
                .collect(java.util.stream.Collectors.toList());
        
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取分析历史
     */
    public List<StockAnalysisResponse> getAnalysisHistory(String stockCode, int limit) {
        return analysisCache.values().stream()
                .filter(response -> stockCode == null || stockCode.equals(response.getStockCode()))
                .sorted((a, b) -> {
                    if (a.getEndTime() == null) return 1;
                    if (b.getEndTime() == null) return -1;
                    return b.getEndTime().compareTo(a.getEndTime());
                })
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取分析结果
     */
    public StockAnalysisResponse getAnalysisResult(String requestId) {
        return analysisCache.get(requestId);
    }

    /**
     * 执行技术分析
     */
    public StockAnalysisResponse performTechnicalAnalysis(StockAnalysisRequest request) {
        String requestId = generateRequestId();
        log.info("开始技术分析: {} - {}", requestId, request.getStockCode());
        
        long startTime = System.currentTimeMillis();
        
        try {
            validateRequest(request);
            
            TechnicalAnalystAgent technicalAgent = getOrCreateTechnicalAgent();
            
            // 设置分析上下文
            setupTechnicalAnalysisContext(technicalAgent, request);
            
            // 执行技术分析
            StockAnalysisResult result = technicalAgent.performAnalysis(request.getStockCode(), 
                    createAnalysisParameters(request));
            
            // 构建响应
            StockAnalysisResponse response = buildResponse(requestId, request, result, startTime);
            response.setAnalysisResult("技术分析: " + result.getConclusion());
            
            // 添加技术分析特定信息
            addTechnicalAnalysisInfo(response, result);
            
            analysisCache.put(requestId, response);
            
            return response;
            
        } catch (Exception e) {
            log.error("技术分析失败: {} - {}", requestId, request.getStockCode(), e);
            return buildErrorResponse(requestId, request, e, startTime);
        }
    }

    /**
     * 执行基本面分析
     */
    public StockAnalysisResponse performFundamentalAnalysis(StockAnalysisRequest request) {
        String requestId = generateRequestId();
        log.info("开始基本面分析: {} - {}", requestId, request.getStockCode());
        
        long startTime = System.currentTimeMillis();
        
        try {
            validateRequest(request);
            
            FundamentalAnalystAgent fundamentalAgent = getOrCreateFundamentalAgent();
            
            // 设置分析上下文
            setupFundamentalAnalysisContext(fundamentalAgent, request);
            
            // 执行基本面分析
            StockAnalysisResult result = fundamentalAgent.performAnalysis(request.getStockCode(), 
                    createAnalysisParameters(request));
            
            // 构建响应
            StockAnalysisResponse response = buildResponse(requestId, request, result, startTime);
            response.setAnalysisResult("基本面分析: " + result.getConclusion());
            
            // 添加基本面分析特定信息
            addFundamentalAnalysisInfo(response, result);
            
            analysisCache.put(requestId, response);
            
            return response;
            
        } catch (Exception e) {
            log.error("基本面分析失败: {} - {}", requestId, request.getStockCode(), e);
            return buildErrorResponse(requestId, request, e, startTime);
        }
    }

    /**
     * 获取分析统计信息
     */
    public Map<String, Object> getAnalysisStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<StockAnalysisResponse> allAnalyses = new ArrayList<>(analysisCache.values());
        
        stats.put("totalAnalyses", allAnalyses.size());
        stats.put("successfulAnalyses", allAnalyses.stream().mapToLong(r -> r.getSuccess() ? 1 : 0).sum());
        stats.put("failedAnalyses", allAnalyses.stream().mapToLong(r -> !r.getSuccess() ? 1 : 0).sum());
        
        // 平均处理时间
        OptionalDouble avgProcessingTime = allAnalyses.stream()
                .filter(r -> r.getProcessingTimeMs() != null)
                .mapToLong(StockAnalysisResponse::getProcessingTimeMs)
                .average();
        stats.put("averageProcessingTimeMs", avgProcessingTime.orElse(0.0));
        
        // 最受欢迎的股票
        Map<String, Long> stockCounts = allAnalyses.stream()
                .filter(r -> r.getStockCode() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        StockAnalysisResponse::getStockCode,
                        java.util.stream.Collectors.counting()
                ));
        stats.put("popularStocks", stockCounts);
        
        // 分析质量分布
        Map<String, Long> qualityDistribution = allAnalyses.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        StockAnalysisResponse::getQualityGrade,
                        java.util.stream.Collectors.counting()
                ));
        stats.put("qualityDistribution", qualityDistribution);
        
        return stats;
    }

    /**
     * 清理过期的分析缓存
     */
    public void cleanupExpiredAnalyses() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24); // 保留24小时
        
        List<String> expiredKeys = analysisCache.entrySet().stream()
                .filter(entry -> {
                    StockAnalysisResponse response = entry.getValue();
                    return response.getEndTime() != null && response.getEndTime().isBefore(cutoffTime);
                })
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
        
        expiredKeys.forEach(analysisCache::remove);
        
        log.info("清理过期分析缓存: {} 条记录", expiredKeys.size());
    }

    // ==================== 私有辅助方法 ====================

    private void validateRequest(StockAnalysisRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("分析请求不能为空");
        }
        
        if (!request.isValid()) {
            throw new IllegalArgumentException("分析请求参数无效: " + request.getSummary());
        }
        
        if (!stockDataService.isValidStockCode(request.getStockCode())) {
            throw new IllegalArgumentException("无效的股票代码: " + request.getStockCode());
        }
    }

    private StockMultiAgentCoordinator getOrCreateCoordinator(String stockCode) {
        String cacheKey = "coordinator_" + stockCode;
        return (StockMultiAgentCoordinator) agentCache.computeIfAbsent(cacheKey, 
                k -> new StockMultiAgentCoordinator());
    }

    private TechnicalAnalystAgent getOrCreateTechnicalAgent() {
        return (TechnicalAnalystAgent) agentCache.computeIfAbsent("technical_agent", 
                k -> new TechnicalAnalystAgent());
    }

    private FundamentalAnalystAgent getOrCreateFundamentalAgent() {
        return (FundamentalAnalystAgent) agentCache.computeIfAbsent("fundamental_agent", 
                k -> new FundamentalAnalystAgent());
    }

    private void setupAnalysisContext(StockMultiAgentCoordinator coordinator, 
                                    StockAnalysisRequest request,
                                    StockDataService.StockInfo stockInfo,
                                    StockDataService.RealTimePrice realTimePrice) {
        // 设置股票基础信息
        Map<String, Object> context = new HashMap<>();
        context.put("stockInfo", stockInfo);
        context.put("realTimePrice", realTimePrice);
        context.put("analysisDepth", request.getDepth());
        context.put("timeframe", request.getTimeframe());
        context.put("analysisTypes", request.getAnalysisTypes());
        
        // 获取K线数据
        if (Boolean.TRUE.equals(request.getRealTimeData())) {
            List<StockDataService.KLineData> klineData = stockDataService.getKLineData(
                    request.getStockCode(), request.getTimeframe(), 100);
            context.put("klineData", klineData);
        }
        
        // 获取历史对比数据
        if (Boolean.TRUE.equals(request.getIncludeHistoricalComparison())) {
            Map<String, Double> priceRange = stockDataService.getPriceRange(
                    request.getStockCode(), 252); // 一年的交易日
            context.put("historicalData", priceRange);
        }
        
        // 获取行业对比数据
        if (Boolean.TRUE.equals(request.getIncludeIndustryComparison())) {
            List<StockDataService.StockInfo> industryStocks = 
                    stockDataService.getStocksByIndustry(stockInfo.getIndustry());
            context.put("industryStocks", industryStocks);
        }
        
        coordinator.setAnalysisContext(context);
    }

    private void setupTechnicalAnalysisContext(TechnicalAnalystAgent agent, StockAnalysisRequest request) {
        // 获取K线数据
        List<StockDataService.KLineData> klineData = stockDataService.getKLineData(
                request.getStockCode(), request.getTimeframe(), 200);
        
        Map<String, Object> context = new HashMap<>();
        context.put("klineData", klineData);
        context.put("timeframe", request.getTimeframe());
        context.put("depth", request.getDepth());
        
        agent.setAnalysisContext(context);
    }

    private void setupFundamentalAnalysisContext(FundamentalAnalystAgent agent, StockAnalysisRequest request) {
        StockDataService.StockInfo stockInfo = stockDataService.getStockInfo(request.getStockCode());
        
        Map<String, Object> context = new HashMap<>();
        context.put("stockInfo", stockInfo);
        context.put("depth", request.getDepth());
        
        // 获取行业对比数据
        List<StockDataService.StockInfo> industryStocks = 
                stockDataService.getStocksByIndustry(stockInfo.getIndustry());
        context.put("industryStocks", industryStocks);
        
        agent.setAnalysisContext(context);
    }

    private Map<String, Object> createAnalysisParameters(StockAnalysisRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("depth", request.getDepth());
        params.put("timeframe", request.getTimeframe());
        params.put("analysisTypes", request.getAnalysisTypes());
        params.put("customParameters", request.getCustomParameters());
        return params;
    }

    private StockAnalysisResult executeAnalysis(StockMultiAgentCoordinator coordinator, 
                                              StockAnalysisRequest request) {
        // 构建分析查询
        String query = buildAnalysisQuery(request);
        
        // 执行协调器分析
        coordinator.step(query);
        
        // 获取分析结果
        return coordinator.getAnalysisResult();
    }

    private String buildAnalysisQuery(StockAnalysisRequest request) {
        StringBuilder query = new StringBuilder();
        query.append("请分析股票 ").append(request.getStockCode());
        
        if (request.getAnalysisTypes() != null && !request.getAnalysisTypes().isEmpty()) {
            query.append("，分析类型包括: ");
            query.append(String.join(", ", request.getAnalysisTypes()));
        }
        
        query.append("，分析深度: ").append(request.getDepthDisplayName());
        query.append("，时间周期: ").append(request.getTimeframeDisplayName());
        
        if (request.getRemarks() != null && !request.getRemarks().trim().isEmpty()) {
            query.append("。备注: ").append(request.getRemarks());
        }
        
        return query.toString();
    }

    private StockAnalysisResponse buildResponse(String requestId, 
                                              StockAnalysisRequest request, 
                                              StockAnalysisResult result, 
                                              long startTime) {
        StockAnalysisResponse response = StockAnalysisResponse.fromStockAnalysisResult(result, requestId);
        
        // 设置请求相关信息
        response.setStartTime(LocalDateTime.now().minusNanos((System.currentTimeMillis() - startTime) * 1000000));
        response.setEndTime(LocalDateTime.now());
        response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        response.setUserId(request.getUserId());
        response.setTags(request.getTags());
        
        // 设置分析配置
        Map<String, Object> config = new HashMap<>();
        config.put("depth", request.getDepth());
        config.put("timeframe", request.getTimeframe());
        config.put("analysisTypes", request.getAnalysisTypes());
        config.put("realTimeData", request.getRealTimeData());
        config.put("includeHistoricalComparison", request.getIncludeHistoricalComparison());
        config.put("includeIndustryComparison", request.getIncludeIndustryComparison());
        response.setAnalysisConfig(config);
        
        // 设置当前价格
        try {
            StockDataService.RealTimePrice price = stockDataService.getRealTimePrice(request.getStockCode());
            response.setCurrentPrice(price.getCurrentPrice());
            response.calculateUpsidePotential();
        } catch (Exception e) {
            log.warn("获取实时价格失败: {}", request.getStockCode(), e);
        }
        
        return response;
    }

    private StockAnalysisResponse buildErrorResponse(String requestId, 
                                                   StockAnalysisRequest request, 
                                                   Exception error, 
                                                   long startTime) {
        return StockAnalysisResponse.builder()
                .requestId(requestId)
                .stockCode(request.getStockCode())
                .status("FAILED")
                .success(false)
                .errorMessage(error.getMessage())
                .startTime(LocalDateTime.now().minusNanos((System.currentTimeMillis() - startTime) * 1000000))
                .endTime(LocalDateTime.now())
                .processingTimeMs(System.currentTimeMillis() - startTime)
                .userId(request.getUserId())
                .build();
    }

    private void addTechnicalAnalysisInfo(StockAnalysisResponse response, StockAnalysisResult result) {
        if (result.getTechnicalIndicators() != null) {
            StockAnalysisResponse.TechnicalAnalysisInfo techInfo = 
                    StockAnalysisResponse.TechnicalAnalysisInfo.builder()
                    .technicalIndicators(convertToDoubleMap(result.getTechnicalIndicators()))
                    .supportLevels(result.getSupportLevels())
                    .resistanceLevels(result.getResistanceLevels())
                    .tradingSignal(result.getRecommendation())
                    .signalStrength(result.getConfidenceScore())
                    .build();
            response.setTechnicalAnalysis(techInfo);
        }
    }

    private void addFundamentalAnalysisInfo(StockAnalysisResponse response, StockAnalysisResult result) {
        if (result.getFundamentalData() != null) {
            StockAnalysisResponse.FundamentalAnalysisInfo fundInfo = 
                    StockAnalysisResponse.FundamentalAnalysisInfo.builder()
                    .financialRatios(convertToDoubleMap(result.getFundamentalData()))
                    .fairValuePerShare(result.getTargetPrice())
                    .valuationConclusion(result.getConclusion())
                    .industryRanking(result.getIndustryComparison())
                    .build();
            response.setFundamentalAnalysis(fundInfo);
        }
    }

    private Map<String, Double> convertToDoubleMap(Map<String, Object> source) {
        if (source == null) return new HashMap<>();
        
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Number) {
                result.put(entry.getKey(), ((Number) value).doubleValue());
            }
        }
        return result;
    }

    private String generateRequestId() {
        return "REQ_" + System.currentTimeMillis() + "_" + 
               Integer.toHexString(new Random().nextInt());
    }
}