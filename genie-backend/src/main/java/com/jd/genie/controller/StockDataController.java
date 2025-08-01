package com.jd.genie.controller;

import com.jd.genie.entity.StockData;
import com.jd.genie.entity.StockKLine;
import com.jd.genie.service.StockDataIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 股票数据控制器
 * 提供股票实时数据、历史数据等API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@Tag(name = "股票数据", description = "股票实时数据和历史数据API")
public class StockDataController {

    private final StockDataIntegrationService stockDataService;

    /**
     * 获取单个股票实时数据
     */
    @GetMapping("/realtime/{symbol}")
    @Operation(summary = "获取股票实时数据", description = "根据股票代码获取实时行情数据")
    public ResponseEntity<StockData> getRealTimeData(
            @Parameter(description = "股票代码，如：000001.SZ", example = "000001.SZ")
            @PathVariable String symbol) {
        
        log.info("获取股票实时数据: {}", symbol);
        StockData stockData = stockDataService.getRealTimeData(symbol);
        
        if (stockData == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(stockData);
    }

    /**
     * 批量获取股票实时数据
     */
    @PostMapping("/realtime/batch")
    @Operation(summary = "批量获取股票实时数据", description = "根据股票代码列表批量获取实时行情数据")
    public ResponseEntity<List<StockData>> getBatchRealTimeData(
            @Parameter(description = "股票代码列表")
            @RequestBody List<String> symbols) {
        
        log.info("批量获取股票实时数据: {} 只", symbols.size());
        List<StockData> stockDataList = stockDataService.getRealTimeData(symbols);
        
        return ResponseEntity.ok(stockDataList);
    }

    /**
     * 获取股票历史K线数据
     */
    @GetMapping("/kline/{symbol}")
    @Operation(summary = "获取股票K线数据", description = "根据股票代码和时间范围获取K线数据")
    public ResponseEntity<List<StockKLine>> getKLineData(
            @Parameter(description = "股票代码", example = "000001.SZ")
            @PathVariable String symbol,
            
            @Parameter(description = "K线周期", example = "1d")
            @RequestParam(defaultValue = "1d") String period,
            
            @Parameter(description = "开始日期", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "结束日期", example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("获取K线数据: {} {} {} - {}", symbol, period, startDate, endDate);
        List<StockKLine> klineData = stockDataService.getHistoricalData(symbol, period, startDate, endDate);
        
        return ResponseEntity.ok(klineData);
    }

    /**
     * 搜索股票
     */
    @GetMapping("/search")
    @Operation(summary = "搜索股票", description = "根据关键词搜索股票（支持代码和名称）")
    public ResponseEntity<List<StockData>> searchStocks(
            @Parameter(description = "搜索关键词", example = "平安")
            @RequestParam String keyword,
            
            @Parameter(description = "返回结果数量限制", example = "20")
            @RequestParam(defaultValue = "20") int limit) {
        
        log.info("搜索股票: {} (限制: {})", keyword, limit);
        List<StockData> results = stockDataService.searchStocks(keyword);
        
        // 限制返回结果数量
        if (results.size() > limit) {
            results = results.subList(0, limit);
        }
        
        return ResponseEntity.ok(results);
    }

    /**
     * 获取涨幅榜
     */
    @GetMapping("/gainers")
    @Operation(summary = "获取涨幅榜", description = "获取当日涨幅最大的股票列表")
    public ResponseEntity<List<StockData>> getTopGainers(
            @Parameter(description = "返回数量", example = "50")
            @RequestParam(defaultValue = "50") int limit) {
        
        log.info("获取涨幅榜: 前{}", limit);
        // 这里需要在StockDataIntegrationService中实现相应方法
        // List<StockData> gainers = stockDataService.getTopGainers(limit);
        // return ResponseEntity.ok(gainers);
        
        return ResponseEntity.ok(List.of()); // 临时返回空列表
    }

    /**
     * 获取跌幅榜
     */
    @GetMapping("/losers")
    @Operation(summary = "获取跌幅榜", description = "获取当日跌幅最大的股票列表")
    public ResponseEntity<List<StockData>> getTopLosers(
            @Parameter(description = "返回数量", example = "50")
            @RequestParam(defaultValue = "50") int limit) {
        
        log.info("获取跌幅榜: 前{}", limit);
        return ResponseEntity.ok(List.of()); // 临时返回空列表
    }

    /**
     * 获取成交量榜
     */
    @GetMapping("/volume")
    @Operation(summary = "获取成交量榜", description = "获取当日成交量最大的股票列表")
    public ResponseEntity<List<StockData>> getTopByVolume(
            @Parameter(description = "返回数量", example = "50")
            @RequestParam(defaultValue = "50") int limit) {
        
        log.info("获取成交量榜: 前{}", limit);
        return ResponseEntity.ok(List.of()); // 临时返回空列表
    }

    /**
     * 获取数据源健康状态
     */
    @GetMapping("/health")
    @Operation(summary = "获取数据源健康状态", description = "检查各个数据源的可用性")
    public ResponseEntity<Map<String, Boolean>> getDataSourceHealth() {
        
        log.info("检查数据源健康状态");
        Map<String, Boolean> healthStatus = stockDataService.getDataSourceHealth();
        
        return ResponseEntity.ok(healthStatus);
    }

    /**
     * 清除股票缓存
     */
    @DeleteMapping("/cache/{symbol}")
    @Operation(summary = "清除股票缓存", description = "清除指定股票的缓存数据")
    public ResponseEntity<Void> clearCache(
            @Parameter(description = "股票代码", example = "000001.SZ")
            @PathVariable String symbol) {
        
        log.info("清除股票缓存: {}", symbol);
        stockDataService.clearCache(symbol);
        
        return ResponseEntity.ok().build();
    }

    /**
     * 清除所有实时数据缓存
     */
    @DeleteMapping("/cache/all")
    @Operation(summary = "清除所有缓存", description = "清除所有股票的实时数据缓存")
    public ResponseEntity<Void> clearAllCache() {
        
        log.info("清除所有实时数据缓存");
        stockDataService.clearAllRealtimeCache();
        
        return ResponseEntity.ok().build();
    }

    /**
     * 获取股票基本信息
     */
    @GetMapping("/info/{symbol}")
    @Operation(summary = "获取股票基本信息", description = "获取股票的基本信息和财务指标")
    public ResponseEntity<StockData> getStockInfo(
            @Parameter(description = "股票代码", example = "000001.SZ")
            @PathVariable String symbol) {
        
        log.info("获取股票基本信息: {}", symbol);
        StockData stockData = stockDataService.getRealTimeData(symbol);
        
        if (stockData == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(stockData);
    }

    /**
     * 获取市场概况
     */
    @GetMapping("/market/overview")
    @Operation(summary = "获取市场概况", description = "获取市场整体概况数据")
    public ResponseEntity<Map<String, Object>> getMarketOverview() {
        
        log.info("获取市场概况");
        
        // 这里需要实现市场概况统计逻辑
        Map<String, Object> overview = Map.of(
            "totalStocks", 0,
            "gainers", 0,
            "losers", 0,
            "unchanged", 0,
            "totalVolume", 0L,
            "totalTurnover", 0L
        );
        
        return ResponseEntity.ok(overview);
    }

    /**
     * 获取热门股票
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门股票", description = "获取当前热门关注的股票列表")
    public ResponseEntity<List<StockData>> getHotStocks(
            @Parameter(description = "返回数量", example = "20")
            @RequestParam(defaultValue = "20") int limit) {
        
        log.info("获取热门股票: 前{}", limit);
        
        // 这里可以基于成交量、涨跌幅、关注度等指标来确定热门股票
        return ResponseEntity.ok(List.of());
    }

    /**
     * 获取股票分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取股票分类", description = "获取股票行业分类或概念分类")
    public ResponseEntity<Map<String, List<String>>> getStockCategories() {
        
        log.info("获取股票分类");
        
        Map<String, List<String>> categories = Map.of(
            "industries", List.of("银行", "证券", "保险", "房地产", "医药", "科技"),
            "concepts", List.of("人工智能", "新能源", "芯片", "5G", "区块链")
        );
        
        return ResponseEntity.ok(categories);
    }
}