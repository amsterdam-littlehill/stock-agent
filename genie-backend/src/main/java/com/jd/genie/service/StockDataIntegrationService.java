package com.jd.genie.service;

import com.jd.genie.entity.StockData;
import com.jd.genie.entity.StockKLine;
import com.jd.genie.repository.StockDataRepository;
import com.jd.genie.repository.StockKLineRepository;
import com.jd.genie.service.data.StockDataClient;
import com.jd.genie.service.InfluxDBService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 股票数据集成服务
 * 整合多个数据源，提供统一的数据访问接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockDataIntegrationService {

    private final List<StockDataClient> stockDataClients;
    private final StockDataRepository stockDataRepository;
    private final StockKLineRepository stockKLineRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;
     
     @Autowired(required = false)
     private InfluxDBService influxDBService;
     
     private static final String CACHE_PREFIX_REALTIME = "stock:realtime:";
    private static final String CACHE_PREFIX_KLINE = "stock:kline:";
    private static final int CACHE_EXPIRE_SECONDS = 300; // 5分钟缓存

    /**
     * 获取实时股票数据（带缓存）
     */
    public StockData getRealTimeData(String symbol) {
        // 先从缓存获取
        String cacheKey = CACHE_PREFIX_REALTIME + symbol;
        StockData cachedData = (StockData) redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            log.debug("从缓存获取股票数据: {}", symbol);
            return cachedData;
        }

        // 从数据源获取
        StockData stockData = fetchRealTimeDataFromSources(symbol);
        if (stockData != null) {
            // 缓存数据
            redisTemplate.opsForValue().set(cacheKey, stockData, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
            
            // 异步保存到数据库
            CompletableFuture.runAsync(() -> saveStockDataAsync(stockData));
        }

        return stockData;
    }

    /**
     * 批量获取实时股票数据
     */
    public List<StockData> getRealTimeData(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, StockData> result = new HashMap<>();
        List<String> uncachedSymbols = new ArrayList<>();

        // 先从缓存获取
        for (String symbol : symbols) {
            String cacheKey = CACHE_PREFIX_REALTIME + symbol;
            StockData cachedData = (StockData) redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                result.put(symbol, cachedData);
            } else {
                uncachedSymbols.add(symbol);
            }
        }

        // 从数据源获取未缓存的数据
        if (!uncachedSymbols.isEmpty()) {
            List<StockData> freshData = fetchRealTimeDataFromSources(uncachedSymbols);
            for (StockData data : freshData) {
                result.put(data.getSymbol(), data);
                
                // 缓存数据
                String cacheKey = CACHE_PREFIX_REALTIME + data.getSymbol();
                redisTemplate.opsForValue().set(cacheKey, data, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
            }

            // 异步批量保存到数据库
            if (!freshData.isEmpty()) {
                CompletableFuture.runAsync(() -> saveStockDataBatchAsync(freshData));
                
                // 批量保存到时序数据库
                if (influxDBService != null) {
                    influxDBService.writeStockDataBatchAsync(freshData);
                    log.debug("批量保存 {} 条股票数据到时序数据库", freshData.size());
                }
            }
        }

        return symbols.stream()
                .map(result::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取历史K线数据
     */
    public List<StockKLine> getHistoricalData(String symbol, String period, 
                                              LocalDate startDate, LocalDate endDate) {
        // 先从数据库查询
        List<StockKLine> dbData = stockKLineRepository
                .findBySymbolAndPeriodAndTradeDateBetweenOrderByTradeDate(
                        symbol, period, startDate, endDate);

        // 如果数据不完整，从数据源补充
        if (isDataIncomplete(dbData, startDate, endDate)) {
            List<StockKLine> sourceData = fetchHistoricalDataFromSources(symbol, period, startDate, endDate);
            if (!sourceData.isEmpty()) {
                // 异步保存到数据库
                CompletableFuture.runAsync(() -> saveKLineDataBatchAsync(sourceData));
                return sourceData;
            }
        }

        return dbData;
    }

    /**
     * 搜索股票
     */
    public List<StockData> searchStocks(String keyword) {
        // 优先从数据库搜索
        List<StockData> dbResults = stockDataRepository.findBySymbolContainingOrNameContaining(keyword, keyword);
        if (!dbResults.isEmpty()) {
            return dbResults.stream().limit(20).collect(Collectors.toList());
        }

        // 从数据源搜索
        for (StockDataClient client : getEnabledClients()) {
            try {
                List<StockData> results = client.searchStocks(keyword);
                if (!results.isEmpty()) {
                    return results.stream().limit(20).collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("数据源 {} 搜索失败: {}", client.getDataSource(), e.getMessage());
            }
        }

        return Collections.emptyList();
    }

    /**
     * 从多个数据源获取实时数据（单个股票）
     */
    private StockData fetchRealTimeDataFromSources(String symbol) {
        List<StockData> results = fetchRealTimeDataFromSources(Collections.singletonList(symbol));
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 从多个数据源获取实时数据（批量）
     */
    private List<StockData> fetchRealTimeDataFromSources(List<String> symbols) {
        List<StockDataClient> enabledClients = getEnabledClients();
        
        for (StockDataClient client : enabledClients) {
            try {
                List<StockData> data = client.getRealTimeData(symbols);
                if (!data.isEmpty()) {
                    log.debug("从 {} 获取到 {} 只股票数据", client.getDataSource(), data.size());
                    return data;
                }
            } catch (Exception e) {
                log.warn("数据源 {} 获取实时数据失败: {}", client.getDataSource(), e.getMessage());
            }
        }

        log.warn("所有数据源都无法获取股票数据: {}", symbols);
        return Collections.emptyList();
    }

    /**
     * 从数据源获取历史数据
     */
    private List<StockKLine> fetchHistoricalDataFromSources(String symbol, String period,
                                                            LocalDate startDate, LocalDate endDate) {
        for (StockDataClient client : getEnabledClients()) {
            try {
                List<StockKLine> data = client.getHistoricalData(symbol, period, startDate, endDate);
                if (!data.isEmpty()) {
                    log.debug("从 {} 获取到历史数据: {} 条", client.getDataSource(), data.size());
                    return data;
                }
            } catch (UnsupportedOperationException e) {
                // 该数据源不支持历史数据，继续尝试下一个
                continue;
            } catch (Exception e) {
                log.warn("数据源 {} 获取历史数据失败: {}", client.getDataSource(), e.getMessage());
            }
        }

        return Collections.emptyList();
    }

    /**
     * 获取启用的数据客户端
     */
    private List<StockDataClient> getEnabledClients() {
        return stockDataClients.stream()
                .filter(StockDataClient::isEnabled)
                .collect(Collectors.toList());
    }

    /**
     * 检查数据是否不完整
     */
    private boolean isDataIncomplete(List<StockKLine> data, LocalDate startDate, LocalDate endDate) {
        if (data.isEmpty()) {
            return true;
        }

        // 简单检查：如果数据量少于预期的一半，认为不完整
        long expectedDays = startDate.until(endDate).getDays();
        return data.size() < expectedDays / 2;
    }

    /**
     * 异步保存股票数据
     */
    @Transactional
    public void saveStockDataAsync(StockData stockData) {
        try {
            // 先将之前的数据标记为非最新
            stockDataRepository.updateIsLatestBySymbol(stockData.getSymbol(), false);
            
            // 保存新数据到关系型数据库
            stockDataRepository.save(stockData);
            log.debug("保存股票数据到关系型数据库: {}", stockData.getSymbol());
            
            // 保存到时序数据库
            if (influxDBService != null) {
                influxDBService.writeStockDataAsync(stockData);
                log.debug("保存股票数据到时序数据库: {}", stockData.getSymbol());
            }
        } catch (Exception e) {
            log.error("保存股票数据失败: {}", stockData.getSymbol(), e);
        }
    }

    /**
     * 异步批量保存股票数据
     */
    @Transactional
    public void saveStockDataBatchAsync(List<StockData> stockDataList) {
        try {
            // 批量更新旧数据
            Set<String> symbols = stockDataList.stream()
                    .map(StockData::getSymbol)
                    .collect(Collectors.toSet());
            
            for (String symbol : symbols) {
                stockDataRepository.updateIsLatestBySymbol(symbol, false);
            }
            
            // 批量保存新数据
            stockDataRepository.saveAll(stockDataList);
            log.debug("批量保存股票数据: {} 条", stockDataList.size());
        } catch (Exception e) {
            log.error("批量保存股票数据失败", e);
        }
    }

    /**
     * 异步批量保存K线数据
     */
    @Transactional
    public void saveKLineDataBatchAsync(List<StockKLine> klineDataList) {
        try {
            stockKLineRepository.saveAll(klineDataList);
            log.debug("批量保存K线数据: {} 条", klineDataList.size());
        } catch (Exception e) {
            log.error("批量保存K线数据失败", e);
        }
    }

    /**
     * 获取数据源健康状态
     */
    public Map<String, Boolean> getDataSourceHealth() {
        Map<String, Boolean> healthStatus = new HashMap<>();
        
        for (StockDataClient client : stockDataClients) {
            boolean isHealthy = client.isEnabled() && client.checkHealth();
            healthStatus.put(client.getDataSource(), isHealthy);
        }
        
        return healthStatus;
    }

    /**
     * 清除缓存
     */
    public void clearCache(String symbol) {
        String cacheKey = CACHE_PREFIX_REALTIME + symbol;
        redisTemplate.delete(cacheKey);
        log.debug("清除股票缓存: {}", symbol);
    }

    /**
     * 清除所有实时数据缓存
     */
    public void clearAllRealtimeCache() {
        Set<String> keys = redisTemplate.keys(CACHE_PREFIX_REALTIME + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("清除所有实时数据缓存: {} 条", keys.size());
        }
    }
}