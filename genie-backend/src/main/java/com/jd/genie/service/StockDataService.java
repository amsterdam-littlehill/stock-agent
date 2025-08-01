package com.jd.genie.service;

import com.jd.genie.entity.StockInfo;
import com.jd.genie.entity.StockPrice;
import com.jd.genie.repository.StockInfoRepository;
import com.jd.genie.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 股票数据服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockDataService {
    
    private final StockInfoRepository stockInfoRepository;
    private final StockPriceRepository stockPriceRepository;
    
    /**
     * 获取股票基础信息
     */
    @Cacheable(value = "stock-info", key = "#stockCode")
    public Optional<StockInfo> getStockInfo(String stockCode) {
        log.debug("获取股票基础信息: {}", stockCode);
        return stockInfoRepository.findByStockCode(stockCode);
    }
    
    /**
     * 获取股票实时价格
     */
    @Cacheable(value = "stock-price", key = "#stockCode", unless = "#result == null")
    public Optional<StockPrice> getRealTimePrice(String stockCode) {
        log.debug("获取股票实时价格: {}", stockCode);
        return stockPriceRepository.findLatestRealTimePrice(stockCode);
    }
    
    /**
     * 获取股票K线数据
     */
    @Cacheable(value = "stock-kline", key = "#stockCode + '_' + #priceType + '_' + #days")
    public List<StockPrice> getKLineData(String stockCode, String priceType, int days) {
        log.debug("获取股票K线数据: {} {} {}天", stockCode, priceType, days);
        
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        
        switch (priceType.toUpperCase()) {
            case "DAILY":
                return stockPriceRepository.findDailyKLineData(stockCode, fromDate);
            case "WEEKLY":
                return stockPriceRepository.findWeeklyKLineData(stockCode, fromDate);
            case "MONTHLY":
                return stockPriceRepository.findMonthlyKLineData(stockCode, fromDate);
            default:
                return stockPriceRepository.findRecentDailyPrices(stockCode, fromDate);
        }
    }
    
    /**
     * 搜索股票
     */
    @Cacheable(value = "stock-search", key = "#keyword")
    public List<StockInfo> searchStocks(String keyword) {
        log.debug("搜索股票: {}", keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        return stockInfoRepository.searchByKeyword(keyword.trim());
    }
    
    /**
     * 分页搜索股票
     */
    public Page<StockInfo> searchStocks(String keyword, Pageable pageable) {
        log.debug("分页搜索股票: {} {}", keyword, pageable);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return stockInfoRepository.findAll(pageable);
        }
        
        return stockInfoRepository.searchByKeyword(keyword.trim(), pageable);
    }
    
    /**
     * 获取支持的股票列表
     */
    @Cacheable(value = "supported-stocks", key = "#market")
    public List<StockInfo> getSupportedStocks(String market) {
        log.debug("获取支持的股票列表: {}", market);
        
        if (market == null || market.trim().isEmpty()) {
            return stockInfoRepository.findByStatus("ACTIVE");
        }
        
        return stockInfoRepository.findByMarketAndStatus(market, "ACTIVE");
    }
    
    /**
     * 获取行业股票列表
     */
    @Cacheable(value = "industry-stocks", key = "#industry")
    public List<StockInfo> getStocksByIndustry(String industry) {
        log.debug("获取行业股票列表: {}", industry);
        return stockInfoRepository.findByIndustryAndStatus(industry, "ACTIVE");
    }
    
    /**
     * 获取所有行业列表
     */
    @Cacheable(value = "all-industries")
    public List<String> getAllIndustries() {
        log.debug("获取所有行业列表");
        return stockInfoRepository.findAllIndustries();
    }
    
    /**
     * 获取所有市场列表
     */
    @Cacheable(value = "all-markets")
    public List<String> getAllMarkets() {
        log.debug("获取所有市场列表");
        return stockInfoRepository.findAllMarkets();
    }
    
    /**
     * 验证股票代码
     */
    public boolean isValidStockCode(String stockCode) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            return false;
        }
        return stockInfoRepository.existsByStockCode(stockCode.trim());
    }
    
    /**
     * 获取股票价格统计信息
     */
    public Map<String, Object> getPriceStatistics(String stockCode, int days) {
        log.debug("获取股票价格统计: {} {}天", stockCode, days);
        
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);
        
        Object[] stats = stockPriceRepository.getPriceStatistics(stockCode, startTime, endTime);
        
        Map<String, Object> result = new HashMap<>();
        if (stats != null && stats.length >= 4) {
            result.put("count", stats[0]);
            result.put("avgPrice", stats[1]);
            result.put("maxPrice", stats[2]);
            result.put("minPrice", stats[3]);
        }
        
        // 计算涨跌幅
        BigDecimal changePercent = stockPriceRepository.calculatePriceChangePercent(stockCode, startTime, endTime);
        result.put("changePercent", changePercent);
        
        // 计算总成交量
        BigDecimal totalVolume = stockPriceRepository.findTotalVolume(stockCode, startTime, endTime);
        result.put("totalVolume", totalVolume);
        
        return result;
    }
    
    /**
     * 获取股票历史价格范围
     */
    public Map<String, Object> getHistoricalPriceRange(String stockCode, int days) {
        log.debug("获取股票历史价格范围: {} {}天", stockCode, days);
        
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);
        
        BigDecimal maxPrice = stockPriceRepository.findMaxHighPrice(stockCode, startTime, endTime);
        BigDecimal minPrice = stockPriceRepository.findMinLowPrice(stockCode, startTime, endTime);
        BigDecimal avgPrice = stockPriceRepository.findAvgClosePrice(stockCode, startTime, endTime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("maxPrice", maxPrice);
        result.put("minPrice", minPrice);
        result.put("avgPrice", avgPrice);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        
        return result;
    }
    
    /**
     * 获取涨幅榜
     */
    @Cacheable(value = "top-gainers", key = "#limit")
    public List<StockPrice> getTopGainers(int limit) {
        log.debug("获取涨幅榜: top {}", limit);
        
        LocalDateTime fromTime = LocalDateTime.now().minusHours(1);
        Pageable pageable = PageRequest.of(0, limit);
        
        return stockPriceRepository.findTopGainers(fromTime, pageable);
    }
    
    /**
     * 获取跌幅榜
     */
    @Cacheable(value = "top-losers", key = "#limit")
    public List<StockPrice> getTopLosers(int limit) {
        log.debug("获取跌幅榜: top {}", limit);
        
        LocalDateTime fromTime = LocalDateTime.now().minusHours(1);
        Pageable pageable = PageRequest.of(0, limit);
        
        return stockPriceRepository.findTopLosers(fromTime, pageable);
    }
    
    /**
     * 获取成交量榜
     */
    @Cacheable(value = "top-volume", key = "#limit")
    public List<StockPrice> getTopVolumeStocks(int limit) {
        log.debug("获取成交量榜: top {}", limit);
        
        LocalDateTime fromTime = LocalDateTime.now().minusHours(1);
        Pageable pageable = PageRequest.of(0, limit);
        
        return stockPriceRepository.findTopVolumeStocks(fromTime, pageable);
    }
    
    /**
     * 获取涨停股票
     */
    @Cacheable(value = "limit-up-stocks")
    public List<StockPrice> getLimitUpStocks() {
        log.debug("获取涨停股票");
        
        LocalDateTime fromTime = LocalDateTime.now().minusHours(1);
        return stockPriceRepository.findLimitUpStocks(fromTime);
    }
    
    /**
     * 获取跌停股票
     */
    @Cacheable(value = "limit-down-stocks")
    public List<StockPrice> getLimitDownStocks() {
        log.debug("获取跌停股票");
        
        LocalDateTime fromTime = LocalDateTime.now().minusHours(1);
        return stockPriceRepository.findLimitDownStocks(fromTime);
    }
    
    /**
     * 获取市场概况
     */
    @Cacheable(value = "market-overview")
    public Map<String, Object> getMarketOverview() {
        log.debug("获取市场概况");
        
        Map<String, Object> overview = new HashMap<>();
        
        // 统计各市场股票数量
        List<Object[]> marketCounts = stockInfoRepository.countByMarket();
        Map<String, Long> marketStats = marketCounts.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
        overview.put("marketStats", marketStats);
        
        // 统计各行业股票数量
        List<Object[]> industryCounts = stockInfoRepository.countByIndustry();
        Map<String, Long> industryStats = industryCounts.stream()
                .limit(10) // 只取前10个行业
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
        overview.put("industryStats", industryStats);
        
        // 活跃股票总数
        long activeStocks = stockInfoRepository.countActiveStocks();
        overview.put("activeStocks", activeStocks);
        
        // ST股票数量
        long stStocks = stockInfoRepository.countStStocks();
        overview.put("stStocks", stStocks);
        
        return overview;
    }
    
    /**
     * 获取行业分析数据
     */
    @Cacheable(value = "industry-analysis", key = "#industry")
    public Map<String, Object> getIndustryAnalysis(String industry) {
        log.debug("获取行业分析数据: {}", industry);
        
        Map<String, Object> analysis = new HashMap<>();
        
        // 行业股票列表
        List<StockInfo> stocks = stockInfoRepository.findByIndustryAndStatus(industry, "ACTIVE");
        analysis.put("stockCount", stocks.size());
        
        if (!stocks.isEmpty()) {
            // 计算行业平均PE、PB、ROE
            OptionalDouble avgPe = stocks.stream()
                    .filter(s -> s.getPeRatio() != null)
                    .mapToDouble(s -> s.getPeRatio().doubleValue())
                    .average();
            
            OptionalDouble avgPb = stocks.stream()
                    .filter(s -> s.getPbRatio() != null)
                    .mapToDouble(s -> s.getPbRatio().doubleValue())
                    .average();
            
            OptionalDouble avgRoe = stocks.stream()
                    .filter(s -> s.getRoe() != null)
                    .mapToDouble(s -> s.getRoe().doubleValue())
                    .average();
            
            analysis.put("avgPe", avgPe.isPresent() ? BigDecimal.valueOf(avgPe.getAsDouble()).setScale(2, RoundingMode.HALF_UP) : null);
            analysis.put("avgPb", avgPb.isPresent() ? BigDecimal.valueOf(avgPb.getAsDouble()).setScale(2, RoundingMode.HALF_UP) : null);
            analysis.put("avgRoe", avgRoe.isPresent() ? BigDecimal.valueOf(avgRoe.getAsDouble()).setScale(2, RoundingMode.HALF_UP) : null);
            
            // 总市值
            BigDecimal totalMarketCap = stocks.stream()
                    .filter(s -> s.getMarketCap() != null)
                    .map(StockInfo::getMarketCap)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            analysis.put("totalMarketCap", totalMarketCap);
            
            // 市值前5的股票
            List<StockInfo> topStocks = stocks.stream()
                    .filter(s -> s.getMarketCap() != null)
                    .sorted((s1, s2) -> s2.getMarketCap().compareTo(s1.getMarketCap()))
                    .limit(5)
                    .collect(Collectors.toList());
            analysis.put("topStocks", topStocks);
        }
        
        return analysis;
    }
    
    /**
     * 保存或更新股票信息
     */
    @Transactional
    public StockInfo saveOrUpdateStockInfo(StockInfo stockInfo) {
        log.debug("保存或更新股票信息: {}", stockInfo.getStockCode());
        
        Optional<StockInfo> existing = stockInfoRepository.findByStockCode(stockInfo.getStockCode());
        if (existing.isPresent()) {
            StockInfo existingStock = existing.get();
            // 更新字段
            existingStock.setStockName(stockInfo.getStockName());
            existingStock.setIndustry(stockInfo.getIndustry());
            existingStock.setSector(stockInfo.getSector());
            existingStock.setCompanyName(stockInfo.getCompanyName());
            existingStock.setLastUpdated(LocalDateTime.now());
            
            // 更新财务数据
            if (stockInfo.getTotalShares() != null) {
                existingStock.updateFinancialData(
                        stockInfo.getTotalShares(),
                        stockInfo.getFloatShares(),
                        stockInfo.getMarketCap(),
                        stockInfo.getFloatMarketCap(),
                        stockInfo.getBookValuePerShare(),
                        stockInfo.getEarningsPerShare(),
                        stockInfo.getPeRatio(),
                        stockInfo.getPbRatio()
                );
            }
            
            return stockInfoRepository.save(existingStock);
        } else {
            stockInfo.setLastUpdated(LocalDateTime.now());
            return stockInfoRepository.save(stockInfo);
        }
    }
    
    /**
     * 保存股票价格数据
     */
    @Transactional
    public StockPrice saveStockPrice(StockPrice stockPrice) {
        log.debug("保存股票价格数据: {} {}", stockPrice.getStockCode(), stockPrice.getPriceType());
        
        // 计算涨跌额和涨跌幅
        stockPrice.calculateChange();
        stockPrice.calculateAmplitude();
        
        return stockPriceRepository.save(stockPrice);
    }
    
    /**
     * 批量保存股票价格数据
     */
    @Transactional
    public List<StockPrice> batchSaveStockPrices(List<StockPrice> stockPrices) {
        log.debug("批量保存股票价格数据: {} 条", stockPrices.size());
        
        // 计算涨跌额和涨跌幅
        stockPrices.forEach(price -> {
            price.calculateChange();
            price.calculateAmplitude();
        });
        
        return stockPriceRepository.saveAll(stockPrices);
    }
    
    /**
     * 清理过期价格数据
     */
    @Transactional
    public int cleanupExpiredPriceData(int daysToKeep) {
        log.info("清理{}天前的价格数据", daysToKeep);
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
        return stockPriceRepository.deleteOldPriceData(cutoffTime);
    }
    
    /**
     * 获取数据统计信息
     */
    public Map<String, Object> getDataStatistics() {
        log.debug("获取数据统计信息");
        
        Map<String, Object> stats = new HashMap<>();
        
        // 股票信息统计
        long totalStocks = stockInfoRepository.count();
        long activeStocks = stockInfoRepository.countActiveStocks();
        long stStocks = stockInfoRepository.countStStocks();
        
        stats.put("totalStocks", totalStocks);
        stats.put("activeStocks", activeStocks);
        stats.put("stStocks", stStocks);
        
        // 价格数据统计
        long totalPriceData = stockPriceRepository.countAllPriceData();
        long dailyData = stockPriceRepository.countByPriceType("DAILY");
        long realTimeData = stockPriceRepository.countByPriceType("REAL_TIME");
        
        stats.put("totalPriceData", totalPriceData);
        stats.put("dailyData", dailyData);
        stats.put("realTimeData", realTimeData);
        
        return stats;
    }
}