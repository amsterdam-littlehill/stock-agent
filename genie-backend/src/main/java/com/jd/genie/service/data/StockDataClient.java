package com.jd.genie.service.data;

import com.jd.genie.entity.StockData;
import com.jd.genie.entity.StockKLine;

import java.time.LocalDate;
import java.util.List;

/**
 * 股票数据客户端接口
 * 定义统一的股票数据获取规范
 */
public interface StockDataClient {

    /**
     * 获取多个股票的实时数据
     * 
     * @param symbols 股票代码列表
     * @return 股票实时数据列表
     */
    List<StockData> getRealTimeData(List<String> symbols);

    /**
     * 获取单个股票的实时数据
     * 
     * @param symbol 股票代码
     * @return 股票实时数据
     */
    StockData getRealTimeData(String symbol);

    /**
     * 获取股票历史K线数据
     * 
     * @param symbol 股票代码
     * @param period K线周期（1d, 1w, 1M等）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return K线数据列表
     */
    default List<StockKLine> getHistoricalData(String symbol, String period, 
                                               LocalDate startDate, LocalDate endDate) {
        throw new UnsupportedOperationException("该数据源不支持历史数据获取");
    }

    /**
     * 获取股票分时数据
     * 
     * @param symbol 股票代码
     * @param date 交易日期
     * @return 分时数据列表
     */
    default List<StockKLine> getIntradayData(String symbol, LocalDate date) {
        throw new UnsupportedOperationException("该数据源不支持分时数据获取");
    }

    /**
     * 搜索股票
     * 
     * @param keyword 关键词（股票代码或名称）
     * @return 匹配的股票列表
     */
    default List<StockData> searchStocks(String keyword) {
        throw new UnsupportedOperationException("该数据源不支持股票搜索");
    }

    /**
     * 获取股票列表
     * 
     * @param market 市场代码（SH-上海，SZ-深圳，ALL-全部）
     * @return 股票列表
     */
    default List<StockData> getStockList(String market) {
        throw new UnsupportedOperationException("该数据源不支持股票列表获取");
    }

    /**
     * 检查数据源是否启用
     * 
     * @return 是否启用
     */
    boolean isEnabled();

    /**
     * 获取数据源名称
     * 
     * @return 数据源名称
     */
    String getDataSource();

    /**
     * 获取API调用限制信息
     * 
     * @return 限制信息（每分钟调用次数等）
     */
    default ApiLimitInfo getApiLimitInfo() {
        return new ApiLimitInfo(1000, 60); // 默认每分钟1000次
    }

    /**
     * API限制信息
     */
    record ApiLimitInfo(
        int maxCallsPerPeriod,  // 周期内最大调用次数
        int periodSeconds       // 周期时间（秒）
    ) {}

    /**
     * 数据质量信息
     */
    record DataQualityInfo(
        double accuracy,        // 数据准确性（0-1）
        int delaySeconds,      // 数据延迟（秒）
        double reliability     // 数据可靠性（0-1）
    ) {
        public static DataQualityInfo defaultQuality() {
            return new DataQualityInfo(0.95, 3, 0.9);
        }
    }

    /**
     * 获取数据质量信息
     * 
     * @return 数据质量信息
     */
    default DataQualityInfo getDataQualityInfo() {
        return DataQualityInfo.defaultQuality();
    }

    /**
     * 检查API健康状态
     * 
     * @return 是否健康
     */
    default boolean checkHealth() {
        try {
            // 尝试获取一个测试股票的数据
            StockData testData = getRealTimeData("000001.SZ");
            return testData != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取支持的功能列表
     * 
     * @return 功能列表
     */
    default List<String> getSupportedFeatures() {
        return List.of("REAL_TIME_DATA");
    }
}