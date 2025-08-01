package com.jd.genie.repository;

import com.jd.genie.entity.StockPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 股票价格Repository
 */
@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {
    
    /**
     * 根据股票代码查找最新价格
     */
    @Query("SELECT s FROM StockPrice s WHERE s.stockCode = :stockCode ORDER BY s.priceTime DESC")
    List<StockPrice> findLatestByStockCode(@Param("stockCode") String stockCode, Pageable pageable);
    
    /**
     * 根据股票代码和价格类型查找最新价格
     */
    @Query("SELECT s FROM StockPrice s WHERE s.stockCode = :stockCode AND s.priceType = :priceType ORDER BY s.priceTime DESC")
    List<StockPrice> findLatestByStockCodeAndPriceType(@Param("stockCode") String stockCode, 
                                                       @Param("priceType") String priceType, 
                                                       Pageable pageable);
    
    /**
     * 获取股票最新实时价格
     */
    @Query("SELECT s FROM StockPrice s WHERE s.stockCode = :stockCode AND s.priceType = 'REAL_TIME' ORDER BY s.priceTime DESC")
    Optional<StockPrice> findLatestRealTimePrice(@Param("stockCode") String stockCode);
    
    /**
     * 根据股票代码和时间范围查找价格
     */
    @Query("SELECT s FROM StockPrice s WHERE s.stockCode = :stockCode AND s.priceTime BETWEEN :startTime AND :endTime ORDER BY s.priceTime ASC")
    List<StockPrice> findByStockCodeAndTimeRange(@Param("stockCode") String stockCode,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据股票代码、价格类型和时间范围查找价格
     */
    @Query("SELECT s FROM StockPrice s WHERE s.stockCode = :stockCode AND s.priceType = :priceType AND s.priceTime BETWEEN :startTime AND :endTime ORDER BY s.priceTime ASC")
    List<StockPrice> findByStockCodeAndPriceTypeAndTimeRange(@Param("stockCode") String stockCode,
                                                             @Param("priceType") String priceType,
                                                             @Param("startTime") LocalDateTime startTime,
                                                             @Param("endTime") LocalDateTime endTime);
    
    /**
     * 获取股票日K线数据
     */
    @Query("SELECT s FROM StockPrice s WHERE s.stockCode = :stockCode AND s.priceType = 'DAILY' AND s.priceTime >= :fromDate ORDER BY s.priceTime ASC")
    List<StockPrice> findDailyKLineData(@Param("stockCode") String stockCode, @Param("fromDate") LocalDateTime fromDate);
    
    /**
     * 获取股票周K线数据
     */
    @Query("SELECT s FROM StockPrice s WHERE s.stockCode = :stockCode AND s.priceType = 'WEEKLY' AND s.priceTime >= :fromDate ORDER BY s.priceTime ASC")
    List<StockPrice> findWeeklyKLineData(@Param("stockCode") String stockCode, @Param("fromDate") LocalDateTime fromDate);
    
    /**
     * 获取股票月K线数据
     */
    @Query("SELECT s FROM StockPrice s WHERE s.stockCode = :stockCode AND s.priceType = 'MONTHLY' AND s.priceTime >= :fromDate ORDER BY s.priceTime ASC")
    List<StockPrice> findMonthlyKLineData(@Param("stockCode") String stockCode, @Param("fromDate") LocalDateTime fromDate);
    
    /**
     * 获取股票指定天数的价格数据
     */
    @Query("SELECT s FROM StockPrice s WHERE s.stockCode = :stockCode AND s.priceType = 'DAILY' AND s.priceTime >= :fromDate ORDER BY s.priceTime DESC")
    List<StockPrice> findRecentDailyPrices(@Param("stockCode") String stockCode, @Param("fromDate") LocalDateTime fromDate);
    
    /**
     * 计算股票在指定时间段的涨跌幅
     */
    @Query("SELECT (MAX(s.closePrice) - MIN(s.closePrice)) / MIN(s.closePrice) * 100 " +
           "FROM StockPrice s WHERE s.stockCode = :stockCode AND s.priceType = 'DAILY' " +
           "AND s.priceTime BETWEEN :startTime AND :endTime")
    BigDecimal calculatePriceChangePercent(@Param("stockCode") String stockCode,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);
    
    /**
     * 获取股票指定时间段的最高价
     */
    @Query("SELECT MAX(s.highPrice) FROM StockPrice s WHERE s.stockCode = :stockCode " +
           "AND s.priceTime BETWEEN :startTime AND :endTime")
    BigDecimal findMaxHighPrice(@Param("stockCode") String stockCode,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);
    
    /**
     * 获取股票指定时间段的最低价
     */
    @Query("SELECT MIN(s.lowPrice) FROM StockPrice s WHERE s.stockCode = :stockCode " +
           "AND s.priceTime BETWEEN :startTime AND :endTime")
    BigDecimal findMinLowPrice(@Param("stockCode") String stockCode,
                              @Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime);
    
    /**
     * 获取股票指定时间段的平均价格
     */
    @Query("SELECT AVG(s.closePrice) FROM StockPrice s WHERE s.stockCode = :stockCode " +
           "AND s.priceTime BETWEEN :startTime AND :endTime")
    BigDecimal findAvgClosePrice(@Param("stockCode") String stockCode,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);
    
    /**
     * 获取股票指定时间段的总成交量
     */
    @Query("SELECT SUM(s.volume) FROM StockPrice s WHERE s.stockCode = :stockCode " +
           "AND s.priceTime BETWEEN :startTime AND :endTime")
    BigDecimal findTotalVolume(@Param("stockCode") String stockCode,
                              @Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime);
    
    /**
     * 获取股票指定时间段的平均成交量
     */
    @Query("SELECT AVG(s.volume) FROM StockPrice s WHERE s.stockCode = :stockCode " +
           "AND s.priceTime BETWEEN :startTime AND :endTime")
    BigDecimal findAvgVolume(@Param("stockCode") String stockCode,
                            @Param("startTime") LocalDateTime startTime,
                            @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找涨停股票
     */
    @Query("SELECT s FROM StockPrice s WHERE s.changePercent >= 9.9 AND s.priceType = 'REAL_TIME' " +
           "AND s.priceTime >= :fromTime ORDER BY s.changePercent DESC")
    List<StockPrice> findLimitUpStocks(@Param("fromTime") LocalDateTime fromTime);
    
    /**
     * 查找跌停股票
     */
    @Query("SELECT s FROM StockPrice s WHERE s.changePercent <= -9.9 AND s.priceType = 'REAL_TIME' " +
           "AND s.priceTime >= :fromTime ORDER BY s.changePercent ASC")
    List<StockPrice> findLimitDownStocks(@Param("fromTime") LocalDateTime fromTime);
    
    /**
     * 查找涨幅榜
     */
    @Query("SELECT s FROM StockPrice s WHERE s.priceType = 'REAL_TIME' AND s.priceTime >= :fromTime " +
           "ORDER BY s.changePercent DESC")
    List<StockPrice> findTopGainers(@Param("fromTime") LocalDateTime fromTime, Pageable pageable);
    
    /**
     * 查找跌幅榜
     */
    @Query("SELECT s FROM StockPrice s WHERE s.priceType = 'REAL_TIME' AND s.priceTime >= :fromTime " +
           "ORDER BY s.changePercent ASC")
    List<StockPrice> findTopLosers(@Param("fromTime") LocalDateTime fromTime, Pageable pageable);
    
    /**
     * 查找成交量榜
     */
    @Query("SELECT s FROM StockPrice s WHERE s.priceType = 'REAL_TIME' AND s.priceTime >= :fromTime " +
           "ORDER BY s.volume DESC")
    List<StockPrice> findTopVolumeStocks(@Param("fromTime") LocalDateTime fromTime, Pageable pageable);
    
    /**
     * 查找成交额榜
     */
    @Query("SELECT s FROM StockPrice s WHERE s.priceType = 'REAL_TIME' AND s.priceTime >= :fromTime " +
           "ORDER BY s.turnover DESC")
    List<StockPrice> findTopTurnoverStocks(@Param("fromTime") LocalDateTime fromTime, Pageable pageable);
    
    /**
     * 查找振幅榜
     */
    @Query("SELECT s FROM StockPrice s WHERE s.priceType = 'REAL_TIME' AND s.priceTime >= :fromTime " +
           "ORDER BY s.amplitude DESC")
    List<StockPrice> findTopAmplitudeStocks(@Param("fromTime") LocalDateTime fromTime, Pageable pageable);
    
    /**
     * 获取股票价格统计信息
     */
    @Query("SELECT COUNT(s), AVG(s.closePrice), MAX(s.closePrice), MIN(s.closePrice) " +
           "FROM StockPrice s WHERE s.stockCode = :stockCode AND s.priceType = 'DAILY' " +
           "AND s.priceTime BETWEEN :startTime AND :endTime")
    Object[] getPriceStatistics(@Param("stockCode") String stockCode,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);
    
    /**
     * 检查股票是否有价格数据
     */
    boolean existsByStockCode(String stockCode);
    
    /**
     * 检查股票在指定时间是否有价格数据
     */
    boolean existsByStockCodeAndPriceTime(String stockCode, LocalDateTime priceTime);
    
    /**
     * 删除指定时间之前的价格数据
     */
    @Query("DELETE FROM StockPrice s WHERE s.priceTime < :cutoffTime")
    int deleteOldPriceData(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * 删除指定股票的价格数据
     */
    int deleteByStockCode(String stockCode);
    
    /**
     * 删除指定股票和价格类型的价格数据
     */
    int deleteByStockCodeAndPriceType(String stockCode, String priceType);
    
    /**
     * 统计价格数据总数
     */
    @Query("SELECT COUNT(s) FROM StockPrice s")
    long countAllPriceData();
    
    /**
     * 统计指定股票的价格数据数量
     */
    long countByStockCode(String stockCode);
    
    /**
     * 统计指定价格类型的数据数量
     */
    long countByPriceType(String priceType);
    
    /**
     * 获取最新更新时间
     */
    @Query("SELECT MAX(s.priceTime) FROM StockPrice s WHERE s.stockCode = :stockCode")
    LocalDateTime findLatestUpdateTime(@Param("stockCode") String stockCode);
    
    /**
     * 获取最早数据时间
     */
    @Query("SELECT MIN(s.priceTime) FROM StockPrice s WHERE s.stockCode = :stockCode")
    LocalDateTime findEarliestDataTime(@Param("stockCode") String stockCode);
    
    /**
     * 分页查询股票价格数据
     */
    Page<StockPrice> findByStockCodeOrderByPriceTimeDesc(String stockCode, Pageable pageable);
    
    /**
     * 分页查询指定价格类型的数据
     */
    Page<StockPrice> findByStockCodeAndPriceTypeOrderByPriceTimeDesc(String stockCode, String priceType, Pageable pageable);
    
    /**
     * 批量插入或更新价格数据（需要在Service层实现）
     */
    @Query("SELECT s FROM StockPrice s WHERE s.stockCode = :stockCode AND s.priceType = :priceType AND s.priceTime = :priceTime")
    Optional<StockPrice> findByStockCodeAndPriceTypeAndPriceTime(@Param("stockCode") String stockCode,
                                                                @Param("priceType") String priceType,
                                                                @Param("priceTime") LocalDateTime priceTime);
}