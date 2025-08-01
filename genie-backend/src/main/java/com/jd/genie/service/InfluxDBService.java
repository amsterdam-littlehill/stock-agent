package com.jd.genie.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.jd.genie.config.InfluxDBConfig;
import com.jd.genie.entity.StockData;
import com.jd.genie.entity.StockKLine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * InfluxDB时序数据服务
 * 负责股票数据的时序存储、查询和分析
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "influxdb", name = "enabled", havingValue = "true", matchIfMissing = true)
public class InfluxDBService {

    private final InfluxDBClient influxDBClient;
    private final InfluxDBConfig influxDBConfig;

    /**
     * 写入实时股票数据
     */
    public void writeStockRealTimeData(StockData stockData) {
        if (influxDBClient == null) {
            log.warn("InfluxDB客户端未初始化，跳过数据写入");
            return;
        }

        try {
            Point point = Point.measurement(InfluxDBConfig.Measurements.STOCK_REALTIME)
                    .addTag(InfluxDBConfig.Tags.STOCK_CODE, stockData.getStockCode())
                    .addTag(InfluxDBConfig.Tags.MARKET, getMarketFromCode(stockData.getStockCode()))
                    .addTag(InfluxDBConfig.Tags.SOURCE, stockData.getDataSource())
                    .addField(InfluxDBConfig.Fields.PRICE, stockData.getCurrentPrice())
                    .addField(InfluxDBConfig.Fields.VOLUME, stockData.getVolume())
                    .addField(InfluxDBConfig.Fields.AMOUNT, stockData.getAmount())
                    .addField(InfluxDBConfig.Fields.CHANGE, stockData.getChange())
                    .addField(InfluxDBConfig.Fields.CHANGE_PERCENT, stockData.getChangePercent())
                    .addField(InfluxDBConfig.Fields.TURNOVER_RATE, stockData.getTurnoverRate())
                    .addField(InfluxDBConfig.Fields.MARKET_CAP, stockData.getMarketCap())
                    .addField(InfluxDBConfig.Fields.PE_RATIO, stockData.getPeRatio())
                    .addField(InfluxDBConfig.Fields.PB_RATIO, stockData.getPbRatio())
                    .time(stockData.getUpdateTime().toInstant(ZoneOffset.UTC), WritePrecision.MS);

            WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
            writeApi.writePoint(influxDBConfig.getBucket(), influxDBConfig.getOrg(), point);
            
            log.debug("写入实时数据成功: {}", stockData.getStockCode());
        } catch (Exception e) {
            log.error("写入实时数据失败: {}", stockData.getStockCode(), e);
        }
    }

    /**
     * 批量写入实时股票数据
     */
    public void writeStockRealTimeDataBatch(List<StockData> stockDataList) {
        if (influxDBClient == null || stockDataList.isEmpty()) {
            return;
        }

        try {
            List<Point> points = stockDataList.stream()
                    .map(this::createRealTimePoint)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!points.isEmpty()) {
                WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
                writeApi.writePoints(influxDBConfig.getBucket(), influxDBConfig.getOrg(), points);
                log.info("批量写入实时数据成功，共 {} 条", points.size());
            }
        } catch (Exception e) {
            log.error("批量写入实时数据失败", e);
        }
    }

    /**
     * 写入K线数据
     */
    public void writeStockKLineData(StockKLine klineData) {
        if (influxDBClient == null) {
            log.warn("InfluxDB客户端未初始化，跳过K线数据写入");
            return;
        }

        try {
            Point point = Point.measurement(InfluxDBConfig.Measurements.STOCK_KLINE)
                    .addTag(InfluxDBConfig.Tags.STOCK_CODE, klineData.getStockCode())
                    .addTag(InfluxDBConfig.Tags.MARKET, getMarketFromCode(klineData.getStockCode()))
                    .addTag(InfluxDBConfig.Tags.PERIOD, klineData.getPeriod().name())
                    .addField(InfluxDBConfig.Fields.OPEN, klineData.getOpenPrice())
                    .addField(InfluxDBConfig.Fields.HIGH, klineData.getHighPrice())
                    .addField(InfluxDBConfig.Fields.LOW, klineData.getLowPrice())
                    .addField(InfluxDBConfig.Fields.CLOSE, klineData.getClosePrice())
                    .addField(InfluxDBConfig.Fields.VOLUME, klineData.getVolume())
                    .addField(InfluxDBConfig.Fields.AMOUNT, klineData.getAmount())
                    .addField(InfluxDBConfig.Fields.CHANGE_PERCENT, klineData.getChangePercent())
                    .addField(InfluxDBConfig.Fields.TURNOVER_RATE, klineData.getTurnoverRate())
                    .time(klineData.getTradeDate().atStartOfDay().toInstant(ZoneOffset.UTC), WritePrecision.MS);

            WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
            writeApi.writePoint(influxDBConfig.getBucket(), influxDBConfig.getOrg(), point);
            
            log.debug("写入K线数据成功: {} - {}", klineData.getStockCode(), klineData.getTradeDate());
        } catch (Exception e) {
            log.error("写入K线数据失败: {} - {}", klineData.getStockCode(), klineData.getTradeDate(), e);
        }
    }

    /**
     * 批量写入K线数据
     */
    public void writeStockKLineDataBatch(List<StockKLine> klineDataList) {
        if (influxDBClient == null || klineDataList.isEmpty()) {
            return;
        }

        try {
            List<Point> points = klineDataList.stream()
                    .map(this::createKLinePoint)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!points.isEmpty()) {
                WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
                writeApi.writePoints(influxDBConfig.getBucket(), influxDBConfig.getOrg(), points);
                log.info("批量写入K线数据成功，共 {} 条", points.size());
            }
        } catch (Exception e) {
            log.error("批量写入K线数据失败", e);
        }
    }

    /**
     * 查询股票实时数据
     */
    public List<Map<String, Object>> queryStockRealTimeData(String stockCode, int limitMinutes) {
        if (influxDBClient == null) {
            return Collections.emptyList();
        }

        try {
            String flux = String.format(
                "from(bucket: \"%s\")" +
                "  |> range(start: -%dm)" +
                "  |> filter(fn: (r) => r._measurement == \"%s\")" +
                "  |> filter(fn: (r) => r.stock_code == \"%s\")" +
                "  |> sort(columns: [\"_time\"], desc: true)",
                influxDBConfig.getBucket(),
                limitMinutes,
                InfluxDBConfig.Measurements.STOCK_REALTIME,
                stockCode
            );

            return executeQuery(flux);
        } catch (Exception e) {
            log.error("查询实时数据失败: {}", stockCode, e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询股票K线数据
     */
    public List<Map<String, Object>> queryStockKLineData(String stockCode, String period, int limitDays) {
        if (influxDBClient == null) {
            return Collections.emptyList();
        }

        try {
            String flux = String.format(
                "from(bucket: \"%s\")" +
                "  |> range(start: -%dd)" +
                "  |> filter(fn: (r) => r._measurement == \"%s\")" +
                "  |> filter(fn: (r) => r.stock_code == \"%s\")" +
                "  |> filter(fn: (r) => r.period == \"%s\")" +
                "  |> sort(columns: [\"_time\"], desc: false)",
                influxDBConfig.getBucket(),
                limitDays,
                InfluxDBConfig.Measurements.STOCK_KLINE,
                stockCode,
                period
            );

            return executeQuery(flux);
        } catch (Exception e) {
            log.error("查询K线数据失败: {} - {}", stockCode, period, e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询市场热点股票
     */
    public List<Map<String, Object>> queryHotStocks(int limitMinutes, int topN) {
        if (influxDBClient == null) {
            return Collections.emptyList();
        }

        try {
            String flux = String.format(
                "from(bucket: \"%s\")" +
                "  |> range(start: -%dm)" +
                "  |> filter(fn: (r) => r._measurement == \"%s\")" +
                "  |> filter(fn: (r) => r._field == \"change_percent\")" +
                "  |> group(columns: [\"stock_code\"])" +
                "  |> last()" +
                "  |> group()" +
                "  |> sort(columns: [\"_value\"], desc: true)" +
                "  |> limit(n: %d)",
                influxDBConfig.getBucket(),
                limitMinutes,
                InfluxDBConfig.Measurements.STOCK_REALTIME,
                topN
            );

            return executeQuery(flux);
        } catch (Exception e) {
            log.error("查询热点股票失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询股票价格趋势
     */
    public List<Map<String, Object>> queryStockPriceTrend(String stockCode, int hours) {
        if (influxDBClient == null) {
            return Collections.emptyList();
        }

        try {
            String flux = String.format(
                "from(bucket: \"%s\")" +
                "  |> range(start: -%dh)" +
                "  |> filter(fn: (r) => r._measurement == \"%s\")" +
                "  |> filter(fn: (r) => r.stock_code == \"%s\")" +
                "  |> filter(fn: (r) => r._field == \"price\")" +
                "  |> aggregateWindow(every: 5m, fn: mean, createEmpty: false)" +
                "  |> sort(columns: [\"_time\"], desc: false)",
                influxDBConfig.getBucket(),
                hours,
                InfluxDBConfig.Measurements.STOCK_REALTIME,
                stockCode
            );

            return executeQuery(flux);
        } catch (Exception e) {
            log.error("查询价格趋势失败: {}", stockCode, e);
            return Collections.emptyList();
        }
    }

    /**
     * 异步写入数据
     */
    public CompletableFuture<Void> writeStockDataAsync(StockData stockData) {
        return CompletableFuture.runAsync(() -> writeStockRealTimeData(stockData));
    }

    /**
     * 异步批量写入数据
     */
    public CompletableFuture<Void> writeStockDataBatchAsync(List<StockData> stockDataList) {
        return CompletableFuture.runAsync(() -> writeStockRealTimeDataBatch(stockDataList));
    }

    /**
     * 清理过期数据
     */
    public void cleanupExpiredData() {
        if (influxDBClient == null) {
            return;
        }

        try {
            // 清理实时数据（保留7天）
            String deleteFlux = String.format(
                "from(bucket: \"%s\")" +
                "  |> range(start: -365d, stop: -%dd)" +
                "  |> filter(fn: (r) => r._measurement == \"%s\")" +
                "  |> drop()",
                influxDBConfig.getBucket(),
                influxDBConfig.getRetention().getRealtimeRetentionDays(),
                InfluxDBConfig.Measurements.STOCK_REALTIME
            );

            // 注意：InfluxDB 2.x的删除操作需要特殊权限，这里仅作示例
            log.info("执行数据清理任务");
        } catch (Exception e) {
            log.error("清理过期数据失败", e);
        }
    }

    /**
     * 获取数据库状态
     */
    public Map<String, Object> getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            if (influxDBClient == null) {
                status.put("connected", false);
                status.put("error", "客户端未初始化");
                return status;
            }

            // 执行简单查询测试连接
            String testFlux = String.format(
                "from(bucket: \"%s\") |> range(start: -1m) |> limit(n: 1)",
                influxDBConfig.getBucket()
            );
            
            QueryApi queryApi = influxDBClient.getQueryApi();
            queryApi.query(testFlux, influxDBConfig.getOrg());
            
            status.put("connected", true);
            status.put("bucket", influxDBConfig.getBucket());
            status.put("org", influxDBConfig.getOrg());
            status.put("url", influxDBConfig.getUrl());
        } catch (Exception e) {
            status.put("connected", false);
            status.put("error", e.getMessage());
        }
        
        return status;
    }

    // 私有辅助方法
    
    private Point createRealTimePoint(StockData stockData) {
        try {
            return Point.measurement(InfluxDBConfig.Measurements.STOCK_REALTIME)
                    .addTag(InfluxDBConfig.Tags.STOCK_CODE, stockData.getStockCode())
                    .addTag(InfluxDBConfig.Tags.MARKET, getMarketFromCode(stockData.getStockCode()))
                    .addTag(InfluxDBConfig.Tags.SOURCE, stockData.getDataSource())
                    .addField(InfluxDBConfig.Fields.PRICE, stockData.getCurrentPrice())
                    .addField(InfluxDBConfig.Fields.VOLUME, stockData.getVolume())
                    .addField(InfluxDBConfig.Fields.AMOUNT, stockData.getAmount())
                    .addField(InfluxDBConfig.Fields.CHANGE, stockData.getChange())
                    .addField(InfluxDBConfig.Fields.CHANGE_PERCENT, stockData.getChangePercent())
                    .time(stockData.getUpdateTime().toInstant(ZoneOffset.UTC), WritePrecision.MS);
        } catch (Exception e) {
            log.error("创建实时数据Point失败: {}", stockData.getStockCode(), e);
            return null;
        }
    }

    private Point createKLinePoint(StockKLine klineData) {
        try {
            return Point.measurement(InfluxDBConfig.Measurements.STOCK_KLINE)
                    .addTag(InfluxDBConfig.Tags.STOCK_CODE, klineData.getStockCode())
                    .addTag(InfluxDBConfig.Tags.MARKET, getMarketFromCode(klineData.getStockCode()))
                    .addTag(InfluxDBConfig.Tags.PERIOD, klineData.getPeriod().name())
                    .addField(InfluxDBConfig.Fields.OPEN, klineData.getOpenPrice())
                    .addField(InfluxDBConfig.Fields.HIGH, klineData.getHighPrice())
                    .addField(InfluxDBConfig.Fields.LOW, klineData.getLowPrice())
                    .addField(InfluxDBConfig.Fields.CLOSE, klineData.getClosePrice())
                    .addField(InfluxDBConfig.Fields.VOLUME, klineData.getVolume())
                    .time(klineData.getTradeDate().atStartOfDay().toInstant(ZoneOffset.UTC), WritePrecision.MS);
        } catch (Exception e) {
            log.error("创建K线数据Point失败: {}", klineData.getStockCode(), e);
            return null;
        }
    }

    private String getMarketFromCode(String stockCode) {
        if (stockCode == null) {
            return "UNKNOWN";
        }
        if (stockCode.endsWith(".SH")) {
            return "SH";
        } else if (stockCode.endsWith(".SZ")) {
            return "SZ";
        }
        return "UNKNOWN";
    }

    private List<Map<String, Object>> executeQuery(String flux) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            QueryApi queryApi = influxDBClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(flux, influxDBConfig.getOrg());
            
            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("time", record.getTime());
                    row.put("measurement", record.getMeasurement());
                    row.put("field", record.getField());
                    row.put("value", record.getValue());
                    
                    // 添加所有标签
                    record.getValues().forEach((key, value) -> {
                        if (!key.startsWith("_") && !"result".equals(key) && !"table".equals(key)) {
                            row.put(key, value);
                        }
                    });
                    
                    results.add(row);
                }
            }
        } catch (Exception e) {
            log.error("执行查询失败: {}", flux, e);
        }
        
        return results;
    }
}