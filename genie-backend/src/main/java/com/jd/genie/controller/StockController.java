package com.jd.genie.controller;

import com.jd.genie.entity.StockInfo;
import com.jd.genie.entity.StockPrice;
import com.jd.genie.service.StockDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 股票数据控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
@Tag(name = "股票数据", description = "股票基础信息、实时价格、K线数据等API")
public class StockController {
    
    private final StockDataService stockDataService;
    
    /**
     * 获取股票基础信息
     */
    @GetMapping("/{stockCode}/info")
    @Operation(summary = "获取股票基础信息", description = "根据股票代码获取股票的基础信息")
    public ResponseEntity<StockInfo> getStockInfo(
            @Parameter(description = "股票代码", example = "000001")
            @PathVariable @NotBlank String stockCode) {
        
        log.info("获取股票基础信息: {}", stockCode);
        
        Optional<StockInfo> stockInfo = stockDataService.getStockInfo(stockCode);
        
        return stockInfo.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取股票实时价格
     */
    @GetMapping("/{stockCode}/price")
    @Operation(summary = "获取股票实时价格", description = "获取指定股票的实时价格信息")
    public ResponseEntity<StockPrice> getRealTimePrice(
            @Parameter(description = "股票代码", example = "000001")
            @PathVariable @NotBlank String stockCode) {
        
        log.info("获取股票实时价格: {}", stockCode);
        
        Optional<StockPrice> price = stockDataService.getRealTimePrice(stockCode);
        
        return price.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取股票K线数据
     */
    @GetMapping("/{stockCode}/kline")
    @Operation(summary = "获取股票K线数据", description = "获取指定股票的K线数据")
    public ResponseEntity<List<StockPrice>> getKLineData(
            @Parameter(description = "股票代码", example = "000001")
            @PathVariable @NotBlank String stockCode,
            
            @Parameter(description = "价格类型", example = "DAILY")
            @RequestParam(defaultValue = "DAILY") String priceType,
            
            @Parameter(description = "天数", example = "30")
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        
        log.info("获取股票K线数据: {} {} {}天", stockCode, priceType, days);
        
        List<StockPrice> klineData = stockDataService.getKLineData(stockCode, priceType, days);
        
        return ResponseEntity.ok(klineData);
    }
    
    /**
     * 搜索股票
     */
    @GetMapping("/search")
    @Operation(summary = "搜索股票", description = "根据关键词搜索股票")
    public ResponseEntity<List<StockInfo>> searchStocks(
            @Parameter(description = "搜索关键词", example = "平安")
            @RequestParam @NotBlank String keyword) {
        
        log.info("搜索股票: {}", keyword);
        
        List<StockInfo> stocks = stockDataService.searchStocks(keyword);
        
        return ResponseEntity.ok(stocks);
    }
    
    /**
     * 分页搜索股票
     */
    @GetMapping("/search/page")
    @Operation(summary = "分页搜索股票", description = "分页搜索股票信息")
    public ResponseEntity<Page<StockInfo>> searchStocksWithPagination(
            @Parameter(description = "搜索关键词")
            @RequestParam(required = false) String keyword,
            
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        log.info("分页搜索股票: {} page={} size={}", keyword, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<StockInfo> stocks = stockDataService.searchStocks(keyword, pageable);
        
        return ResponseEntity.ok(stocks);
    }
    
    /**
     * 获取支持的股票列表
     */
    @GetMapping("/supported")
    @Operation(summary = "获取支持的股票列表", description = "获取系统支持的股票列表")
    public ResponseEntity<List<StockInfo>> getSupportedStocks(
            @Parameter(description = "市场类型", example = "A股")
            @RequestParam(required = false) String market) {
        
        log.info("获取支持的股票列表: {}", market);
        
        List<StockInfo> stocks = stockDataService.getSupportedStocks(market);
        
        return ResponseEntity.ok(stocks);
    }
    
    /**
     * 获取行业股票列表
     */
    @GetMapping("/industry/{industry}")
    @Operation(summary = "获取行业股票列表", description = "获取指定行业的股票列表")
    public ResponseEntity<List<StockInfo>> getStocksByIndustry(
            @Parameter(description = "行业名称", example = "银行")
            @PathVariable @NotBlank String industry) {
        
        log.info("获取行业股票列表: {}", industry);
        
        List<StockInfo> stocks = stockDataService.getStocksByIndustry(industry);
        
        return ResponseEntity.ok(stocks);
    }
    
    /**
     * 获取所有行业列表
     */
    @GetMapping("/industries")
    @Operation(summary = "获取所有行业列表", description = "获取系统中所有的行业分类")
    public ResponseEntity<List<String>> getAllIndustries() {
        
        log.info("获取所有行业列表");
        
        List<String> industries = stockDataService.getAllIndustries();
        
        return ResponseEntity.ok(industries);
    }
    
    /**
     * 获取所有市场列表
     */
    @GetMapping("/markets")
    @Operation(summary = "获取所有市场列表", description = "获取系统中所有的市场分类")
    public ResponseEntity<List<String>> getAllMarkets() {
        
        log.info("获取所有市场列表");
        
        List<String> markets = stockDataService.getAllMarkets();
        
        return ResponseEntity.ok(markets);
    }
    
    /**
     * 验证股票代码
     */
    @GetMapping("/validate/{stockCode}")
    @Operation(summary = "验证股票代码", description = "验证股票代码是否有效")
    public ResponseEntity<Map<String, Object>> validateStockCode(
            @Parameter(description = "股票代码", example = "000001")
            @PathVariable @NotBlank String stockCode) {
        
        log.info("验证股票代码: {}", stockCode);
        
        boolean isValid = stockDataService.isValidStockCode(stockCode);
        
        Map<String, Object> result = Map.of(
                "stockCode", stockCode,
                "isValid", isValid
        );
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取股票价格统计
     */
    @GetMapping("/{stockCode}/price/statistics")
    @Operation(summary = "获取股票价格统计", description = "获取指定时间段内的股票价格统计信息")
    public ResponseEntity<Map<String, Object>> getPriceStatistics(
            @Parameter(description = "股票代码", example = "000001")
            @PathVariable @NotBlank String stockCode,
            
            @Parameter(description = "统计天数", example = "30")
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        
        log.info("获取股票价格统计: {} {}天", stockCode, days);
        
        Map<String, Object> statistics = stockDataService.getPriceStatistics(stockCode, days);
        
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 获取股票历史价格范围
     */
    @GetMapping("/{stockCode}/price/range")
    @Operation(summary = "获取股票历史价格范围", description = "获取指定时间段内的股票价格范围")
    public ResponseEntity<Map<String, Object>> getHistoricalPriceRange(
            @Parameter(description = "股票代码", example = "000001")
            @PathVariable @NotBlank String stockCode,
            
            @Parameter(description = "统计天数", example = "30")
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        
        log.info("获取股票历史价格范围: {} {}天", stockCode, days);
        
        Map<String, Object> priceRange = stockDataService.getHistoricalPriceRange(stockCode, days);
        
        return ResponseEntity.ok(priceRange);
    }
    
    /**
     * 获取涨幅榜
     */
    @GetMapping("/ranking/gainers")
    @Operation(summary = "获取涨幅榜", description = "获取当前涨幅最大的股票列表")
    public ResponseEntity<List<StockPrice>> getTopGainers(
            @Parameter(description = "返回数量", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        log.info("获取涨幅榜: top {}", limit);
        
        List<StockPrice> topGainers = stockDataService.getTopGainers(limit);
        
        return ResponseEntity.ok(topGainers);
    }
    
    /**
     * 获取跌幅榜
     */
    @GetMapping("/ranking/losers")
    @Operation(summary = "获取跌幅榜", description = "获取当前跌幅最大的股票列表")
    public ResponseEntity<List<StockPrice>> getTopLosers(
            @Parameter(description = "返回数量", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        log.info("获取跌幅榜: top {}", limit);
        
        List<StockPrice> topLosers = stockDataService.getTopLosers(limit);
        
        return ResponseEntity.ok(topLosers);
    }
    
    /**
     * 获取成交量榜
     */
    @GetMapping("/ranking/volume")
    @Operation(summary = "获取成交量榜", description = "获取当前成交量最大的股票列表")
    public ResponseEntity<List<StockPrice>> getTopVolumeStocks(
            @Parameter(description = "返回数量", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        
        log.info("获取成交量榜: top {}", limit);
        
        List<StockPrice> topVolumeStocks = stockDataService.getTopVolumeStocks(limit);
        
        return ResponseEntity.ok(topVolumeStocks);
    }
    
    /**
     * 获取涨停股票
     */
    @GetMapping("/limit-up")
    @Operation(summary = "获取涨停股票", description = "获取当前涨停的股票列表")
    public ResponseEntity<List<StockPrice>> getLimitUpStocks() {
        
        log.info("获取涨停股票");
        
        List<StockPrice> limitUpStocks = stockDataService.getLimitUpStocks();
        
        return ResponseEntity.ok(limitUpStocks);
    }
    
    /**
     * 获取跌停股票
     */
    @GetMapping("/limit-down")
    @Operation(summary = "获取跌停股票", description = "获取当前跌停的股票列表")
    public ResponseEntity<List<StockPrice>> getLimitDownStocks() {
        
        log.info("获取跌停股票");
        
        List<StockPrice> limitDownStocks = stockDataService.getLimitDownStocks();
        
        return ResponseEntity.ok(limitDownStocks);
    }
    
    /**
     * 获取市场概况
     */
    @GetMapping("/market/overview")
    @Operation(summary = "获取市场概况", description = "获取整体市场的概况信息")
    public ResponseEntity<Map<String, Object>> getMarketOverview() {
        
        log.info("获取市场概况");
        
        Map<String, Object> overview = stockDataService.getMarketOverview();
        
        return ResponseEntity.ok(overview);
    }
    
    /**
     * 获取行业分析
     */
    @GetMapping("/industry/{industry}/analysis")
    @Operation(summary = "获取行业分析", description = "获取指定行业的分析数据")
    public ResponseEntity<Map<String, Object>> getIndustryAnalysis(
            @Parameter(description = "行业名称", example = "银行")
            @PathVariable @NotBlank String industry) {
        
        log.info("获取行业分析: {}", industry);
        
        Map<String, Object> analysis = stockDataService.getIndustryAnalysis(industry);
        
        return ResponseEntity.ok(analysis);
    }
    
    /**
     * 获取数据统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取数据统计", description = "获取股票数据的统计信息")
    public ResponseEntity<Map<String, Object>> getDataStatistics() {
        
        log.info("获取数据统计");
        
        Map<String, Object> statistics = stockDataService.getDataStatistics();
        
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 批量获取股票信息
     */
    @PostMapping("/batch/info")
    @Operation(summary = "批量获取股票信息", description = "批量获取多个股票的基础信息")
    public ResponseEntity<Map<String, StockInfo>> getBatchStockInfo(
            @Parameter(description = "股票代码列表")
            @RequestBody List<String> stockCodes) {
        
        log.info("批量获取股票信息: {} 只股票", stockCodes.size());
        
        Map<String, StockInfo> result = stockCodes.stream()
                .collect(java.util.stream.Collectors.toMap(
                        code -> code,
                        code -> stockDataService.getStockInfo(code).orElse(null),
                        (existing, replacement) -> existing
                ));
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 批量获取股票价格
     */
    @PostMapping("/batch/price")
    @Operation(summary = "批量获取股票价格", description = "批量获取多个股票的实时价格")
    public ResponseEntity<Map<String, StockPrice>> getBatchStockPrice(
            @Parameter(description = "股票代码列表")
            @RequestBody List<String> stockCodes) {
        
        log.info("批量获取股票价格: {} 只股票", stockCodes.size());
        
        Map<String, StockPrice> result = stockCodes.stream()
                .collect(java.util.stream.Collectors.toMap(
                        code -> code,
                        code -> stockDataService.getRealTimePrice(code).orElse(null),
                        (existing, replacement) -> existing
                ));
        
        return ResponseEntity.ok(result);
    }
}