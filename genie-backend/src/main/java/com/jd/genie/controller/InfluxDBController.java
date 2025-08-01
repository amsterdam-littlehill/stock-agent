package com.jd.genie.controller;

import com.jd.genie.service.InfluxDBService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * InfluxDB时序数据控制器
 * 提供时序数据查询和分析的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/timeseries")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@ConditionalOnProperty(prefix = "influxdb", name = "enabled", havingValue = "true", matchIfMissing = true)
public class InfluxDBController {

    private final InfluxDBService influxDBService;

    /**
     * 获取股票实时数据时序
     */
    @GetMapping("/stock/{stockCode}/realtime")
    public ResponseEntity<List<Map<String, Object>>> getStockRealTimeData(
            @PathVariable String stockCode,
            @RequestParam(defaultValue = "60") int limitMinutes) {
        try {
            List<Map<String, Object>> data = influxDBService.queryStockRealTimeData(stockCode, limitMinutes);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("获取股票实时时序数据失败: {}", stockCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取股票K线数据时序
     */
    @GetMapping("/stock/{stockCode}/kline")
    public ResponseEntity<List<Map<String, Object>>> getStockKLineData(
            @PathVariable String stockCode,
            @RequestParam(defaultValue = "1d") String period,
            @RequestParam(defaultValue = "30") int limitDays) {
        try {
            List<Map<String, Object>> data = influxDBService.queryStockKLineData(stockCode, period, limitDays);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("获取股票K线时序数据失败: {} - {}", stockCode, period, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取股票价格趋势
     */
    @GetMapping("/stock/{stockCode}/trend")
    public ResponseEntity<List<Map<String, Object>>> getStockPriceTrend(
            @PathVariable String stockCode,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            List<Map<String, Object>> data = influxDBService.queryStockPriceTrend(stockCode, hours);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("获取股票价格趋势失败: {}", stockCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取市场热点股票
     */
    @GetMapping("/market/hot-stocks")
    public ResponseEntity<List<Map<String, Object>>> getHotStocks(
            @RequestParam(defaultValue = "60") int limitMinutes,
            @RequestParam(defaultValue = "20") int topN) {
        try {
            List<Map<String, Object>> data = influxDBService.queryHotStocks(limitMinutes, topN);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("获取热点股票失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取多只股票的实时对比数据
     */
    @PostMapping("/stocks/compare")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> compareStocks(
            @RequestBody List<String> stockCodes,
            @RequestParam(defaultValue = "60") int limitMinutes) {
        try {
            Map<String, List<Map<String, Object>>> result = new java.util.HashMap<>();
            
            for (String stockCode : stockCodes) {
                List<Map<String, Object>> data = influxDBService.queryStockRealTimeData(stockCode, limitMinutes);
                result.put(stockCode, data);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("股票对比查询失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取股票技术指标数据
     */
    @GetMapping("/stock/{stockCode}/indicators")
    public ResponseEntity<Map<String, Object>> getStockIndicators(
            @PathVariable String stockCode,
            @RequestParam(defaultValue = "1d") String period,
            @RequestParam(defaultValue = "30") int limitDays) {
        try {
            // 这里可以实现技术指标的计算和查询
            // 例如：MA、RSI、MACD等
            Map<String, Object> indicators = new java.util.HashMap<>();
            
            // 获取K线数据用于计算技术指标
            List<Map<String, Object>> klineData = influxDBService.queryStockKLineData(stockCode, period, limitDays);
            
            indicators.put("stockCode", stockCode);
            indicators.put("period", period);
            indicators.put("dataPoints", klineData.size());
            indicators.put("klineData", klineData);
            
            // TODO: 实现具体的技术指标计算
            // indicators.put("ma5", calculateMA(klineData, 5));
            // indicators.put("ma10", calculateMA(klineData, 10));
            // indicators.put("ma20", calculateMA(klineData, 20));
            // indicators.put("rsi", calculateRSI(klineData, 14));
            // indicators.put("macd", calculateMACD(klineData));
            
            return ResponseEntity.ok(indicators);
        } catch (Exception e) {
            log.error("获取股票技术指标失败: {}", stockCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取市场概况数据
     */
    @GetMapping("/market/overview")
    public ResponseEntity<Map<String, Object>> getMarketOverview(
            @RequestParam(defaultValue = "60") int limitMinutes) {
        try {
            Map<String, Object> overview = new java.util.HashMap<>();
            
            // 获取热点股票
            List<Map<String, Object>> hotStocks = influxDBService.queryHotStocks(limitMinutes, 10);
            overview.put("hotStocks", hotStocks);
            
            // TODO: 添加更多市场概况数据
            // - 涨跌股票数量统计
            // - 成交量统计
            // - 市场情绪指标
            // - 板块表现
            
            overview.put("updateTime", System.currentTimeMillis());
            overview.put("dataRange", limitMinutes + " minutes");
            
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            log.error("获取市场概况失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取InfluxDB数据库状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        try {
            Map<String, Object> status = influxDBService.getDatabaseStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("获取数据库状态失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 手动触发数据清理
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> triggerDataCleanup() {
        try {
            influxDBService.cleanupExpiredData();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "数据清理任务已触发",
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("触发数据清理失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "数据清理失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取数据统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getDataStatistics(
            @RequestParam(defaultValue = "24") int hours) {
        try {
            Map<String, Object> statistics = new java.util.HashMap<>();
            
            // TODO: 实现数据统计功能
            // - 数据点数量统计
            // - 存储空间使用情况
            // - 查询性能统计
            // - 数据质量指标
            
            statistics.put("timeRange", hours + " hours");
            statistics.put("timestamp", System.currentTimeMillis());
            statistics.put("message", "统计功能开发中");
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取数据统计失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取支持的查询参数说明
     */
    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getApiHelp() {
        Map<String, Object> help = new java.util.HashMap<>();
        
        help.put("endpoints", Map.of(
            "GET /api/timeseries/stock/{stockCode}/realtime", "获取股票实时数据时序",
            "GET /api/timeseries/stock/{stockCode}/kline", "获取股票K线数据时序",
            "GET /api/timeseries/stock/{stockCode}/trend", "获取股票价格趋势",
            "GET /api/timeseries/market/hot-stocks", "获取市场热点股票",
            "POST /api/timeseries/stocks/compare", "多股票对比分析",
            "GET /api/timeseries/stock/{stockCode}/indicators", "获取技术指标",
            "GET /api/timeseries/market/overview", "获取市场概况",
            "GET /api/timeseries/status", "获取数据库状态"
        ));
        
        help.put("parameters", Map.of(
            "stockCode", "股票代码，如：000001.SZ",
            "limitMinutes", "时间范围（分钟），默认60",
            "limitDays", "时间范围（天），默认30",
            "period", "K线周期：1m,5m,15m,30m,1h,1d,1w,1M",
            "hours", "小时数，默认24",
            "topN", "返回数量，默认20"
        ));
        
        help.put("examples", Map.of(
            "实时数据", "/api/timeseries/stock/000001.SZ/realtime?limitMinutes=120",
            "日K线", "/api/timeseries/stock/000001.SZ/kline?period=1d&limitDays=60",
            "价格趋势", "/api/timeseries/stock/000001.SZ/trend?hours=48",
            "热点股票", "/api/timeseries/market/hot-stocks?topN=50"
        ));
        
        return ResponseEntity.ok(help);
    }
}