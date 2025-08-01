package com.jd.genie.controller;

import com.jd.genie.agent.agent.AgentContext;
import com.jd.genie.agent.agent.stock.*;
import com.jd.genie.agent.llm.LLM;
import com.jd.genie.model.dto.StockAnalysisRequest;
import com.jd.genie.model.dto.StockAnalysisResponse;
import com.jd.genie.model.response.ApiResponse;
import com.jd.genie.service.StockAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 股票分析控制器
 * 提供股票分析相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockAnalysisController {

    private final StockAnalysisService stockAnalysisService;
    private final LLM llm;

    /**
     * 执行多智能体股票分析
     */
    @PostMapping("/analyze")
    public ApiResponse<StockAnalysisResponse> analyzeStock(@Valid @RequestBody StockAnalysisRequest request) {
        try {
            log.info("收到股票分析请求: {}", request.getStockCode());
            
            // 创建分析上下文
            String requestId = UUID.randomUUID().toString();
            AgentContext context = new AgentContext();
            context.setRequestId(requestId);
            
            // 创建多智能体协调器
            StockMultiAgentCoordinator coordinator = new StockMultiAgentCoordinator();
            coordinator.setContext(context);
            coordinator.setLlm(llm);
            
            // 执行分析
            String analysisQuery = buildAnalysisQuery(request);
            String analysisResult = coordinator.run(analysisQuery);
            
            // 构建响应
            StockAnalysisResponse response = StockAnalysisResponse.builder()
                    .requestId(requestId)
                    .stockCode(request.getStockCode())
                    .analysisResult(analysisResult)
                    .success(true)
                    .build();
            
            log.info("股票分析完成: {}", request.getStockCode());
            
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("股票分析失败", e);
            return ApiResponse.error("分析失败: " + e.getMessage());
        }
    }

    /**
     * 流式股票分析（SSE）
     */
    @GetMapping(value = "/analyze/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter analyzeStockStream(@RequestParam String stockCode,
                                        @RequestParam(defaultValue = "normal") String depth,
                                        @RequestParam(defaultValue = "1d") String timeframe) {
        
        SseEmitter emitter = new SseEmitter(60000L); // 60秒超时
        
        try {
            log.info("开始流式股票分析: {}", stockCode);
            
            // 异步执行分析
            stockAnalysisService.analyzeStockAsync(stockCode, depth, timeframe, emitter);
            
        } catch (Exception e) {
            log.error("流式分析启动失败", e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("分析启动失败: " + e.getMessage()));
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        }
        
        return emitter;
    }

    /**
     * 获取技术分析
     */
    @PostMapping("/technical-analysis")
    public ApiResponse<StockAnalysisResult> getTechnicalAnalysis(@Valid @RequestBody StockAnalysisRequest request) {
        try {
            log.info("收到技术分析请求: {}", request.getStockCode());
            
            // 创建技术分析师
            TechnicalAnalystAgent technicalAgent = new TechnicalAnalystAgent();
            
            // 设置上下文
            AgentContext context = new AgentContext();
            context.setRequestId(UUID.randomUUID().toString());
            technicalAgent.setContext(context);
            technicalAgent.setLlm(llm);
            
            // 执行分析
            Map<String, Object> parameters = buildAnalysisParameters(request);
            StockAnalysisResult result = technicalAgent.performAnalysis(request.getStockCode(), parameters);
            
            log.info("技术分析完成: {}", request.getStockCode());
            
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            log.error("技术分析失败", e);
            return ApiResponse.error("技术分析失败: " + e.getMessage());
        }
    }

    /**
     * 获取基本面分析
     */
    @PostMapping("/fundamental-analysis")
    public ApiResponse<StockAnalysisResult> getFundamentalAnalysis(@Valid @RequestBody StockAnalysisRequest request) {
        try {
            log.info("收到基本面分析请求: {}", request.getStockCode());
            
            // 创建基本面分析师
            FundamentalAnalystAgent fundamentalAgent = new FundamentalAnalystAgent();
            
            // 设置上下文
            AgentContext context = new AgentContext();
            context.setRequestId(UUID.randomUUID().toString());
            fundamentalAgent.setContext(context);
            fundamentalAgent.setLlm(llm);
            
            // 执行分析
            Map<String, Object> parameters = buildAnalysisParameters(request);
            StockAnalysisResult result = fundamentalAgent.performAnalysis(request.getStockCode(), parameters);
            
            log.info("基本面分析完成: {}", request.getStockCode());
            
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            log.error("基本面分析失败", e);
            return ApiResponse.error("基本面分析失败: " + e.getMessage());
        }
    }

    /**
     * 获取分析历史
     */
    @GetMapping("/history")
    public ApiResponse<List<StockAnalysisResponse>> getAnalysisHistory(
            @RequestParam(required = false) String stockCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            List<StockAnalysisResponse> history = stockAnalysisService.getAnalysisHistory(stockCode, page, size);
            return ApiResponse.success(history);
        } catch (Exception e) {
            log.error("获取分析历史失败", e);
            return ApiResponse.error("获取历史记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取支持的股票列表
     */
    @GetMapping("/supported-stocks")
    public ApiResponse<List<Map<String, String>>> getSupportedStocks(
            @RequestParam(required = false) String market,
            @RequestParam(required = false) String keyword) {
        
        try {
            List<Map<String, String>> stocks = stockAnalysisService.getSupportedStocks(market, keyword);
            return ApiResponse.success(stocks);
        } catch (Exception e) {
            log.error("获取支持股票列表失败", e);
            return ApiResponse.error("获取股票列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取股票基本信息
     */
    @GetMapping("/info/{stockCode}")
    public ApiResponse<Map<String, Object>> getStockInfo(@PathVariable String stockCode) {
        try {
            Map<String, Object> stockInfo = stockAnalysisService.getStockInfo(stockCode);
            return ApiResponse.success(stockInfo);
        } catch (Exception e) {
            log.error("获取股票信息失败: {}", stockCode, e);
            return ApiResponse.error("获取股票信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取实时股价
     */
    @GetMapping("/price/{stockCode}")
    public ApiResponse<Map<String, Object>> getStockPrice(@PathVariable String stockCode) {
        try {
            Map<String, Object> priceInfo = stockAnalysisService.getStockPrice(stockCode);
            return ApiResponse.success(priceInfo);
        } catch (Exception e) {
            log.error("获取股价失败: {}", stockCode, e);
            return ApiResponse.error("获取股价失败: " + e.getMessage());
        }
    }

    /**
     * 获取K线数据
     */
    @GetMapping("/kline/{stockCode}")
    public ApiResponse<List<Map<String, Object>>> getKlineData(
            @PathVariable String stockCode,
            @RequestParam(defaultValue = "1d") String period,
            @RequestParam(defaultValue = "100") int limit) {
        
        try {
            List<Map<String, Object>> klineData = stockAnalysisService.getKlineData(stockCode, period, limit);
            return ApiResponse.success(klineData);
        } catch (Exception e) {
            log.error("获取K线数据失败: {}", stockCode, e);
            return ApiResponse.error("获取K线数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取技术指标数据
     */
    @GetMapping("/indicators/{stockCode}")
    public ApiResponse<Map<String, Object>> getTechnicalIndicators(
            @PathVariable String stockCode,
            @RequestParam(required = false) List<String> indicators,
            @RequestParam(defaultValue = "1d") String period) {
        
        try {
            Map<String, Object> indicatorData = stockAnalysisService.getTechnicalIndicators(stockCode, indicators, period);
            return ApiResponse.success(indicatorData);
        } catch (Exception e) {
            log.error("获取技术指标失败: {}", stockCode, e);
            return ApiResponse.error("获取技术指标失败: " + e.getMessage());
        }
    }

    /**
     * 批量分析股票
     */
    @PostMapping("/batch-analyze")
    public ApiResponse<List<StockAnalysisResponse>> batchAnalyzeStocks(
            @RequestBody List<String> stockCodes,
            @RequestParam(defaultValue = "normal") String depth) {
        
        try {
            log.info("收到批量分析请求，股票数量: {}", stockCodes.size());
            
            List<StockAnalysisResponse> results = stockAnalysisService.batchAnalyzeStocks(stockCodes, depth);
            
            log.info("批量分析完成，成功分析: {} 只股票", results.size());
            
            return ApiResponse.success(results);
            
        } catch (Exception e) {
            log.error("批量分析失败", e);
            return ApiResponse.error("批量分析失败: " + e.getMessage());
        }
    }

    /**
     * 获取分析配置
     */
    @GetMapping("/config")
    public ApiResponse<Map<String, Object>> getAnalysisConfig() {
        try {
            Map<String, Object> config = stockAnalysisService.getAnalysisConfig();
            return ApiResponse.success(config);
        } catch (Exception e) {
            log.error("获取分析配置失败", e);
            return ApiResponse.error("获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新分析配置
     */
    @PostMapping("/config")
    public ApiResponse<String> updateAnalysisConfig(@RequestBody Map<String, Object> config) {
        try {
            stockAnalysisService.updateAnalysisConfig(config);
            return ApiResponse.success("配置更新成功");
        } catch (Exception e) {
            log.error("更新分析配置失败", e);
            return ApiResponse.error("配置更新失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> healthCheck() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "timestamp", System.currentTimeMillis(),
                "service", "stock-analysis",
                "version", "1.0.0"
        );
        return ApiResponse.success(health);
    }

    /**
     * 构建分析查询语句
     */
    private String buildAnalysisQuery(StockAnalysisRequest request) {
        StringBuilder query = new StringBuilder();
        
        query.append("请对股票 ").append(request.getStockCode()).append(" 进行");
        
        if ("detailed".equals(request.getDepth())) {
            query.append("详细");
        } else if ("summary".equals(request.getDepth())) {
            query.append("简要");
        }
        
        query.append("分析，时间周期为").append(request.getTimeframe());
        
        if (request.getAnalysisTypes() != null && !request.getAnalysisTypes().isEmpty()) {
            query.append("，重点关注").append(String.join("、", request.getAnalysisTypes()));
        }
        
        return query.toString();
    }

    /**
     * 构建分析参数
     */
    private Map<String, Object> buildAnalysisParameters(StockAnalysisRequest request) {
        Map<String, Object> parameters = new java.util.HashMap<>();
        
        parameters.put("timeframe", request.getTimeframe());
        parameters.put("depth", request.getDepth());
        
        if (request.getAnalysisTypes() != null) {
            parameters.put("analysis_types", request.getAnalysisTypes());
        }
        
        if (request.getCustomParameters() != null) {
            parameters.putAll(request.getCustomParameters());
        }
        
        return parameters;
    }
}