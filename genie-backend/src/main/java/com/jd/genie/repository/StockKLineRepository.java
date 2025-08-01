package com.jd.genie.repository;

import com.jd.genie.entity.StockKLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * K线数据Repository
 */
@Repository
public interface StockKLineRepository extends JpaRepository<StockKLine, Long> {

    /**
     * 根据股票代码、周期和日期范围查询K线数据
     */
    List<StockKLine> findBySymbolAndPeriodAndTradeDateBetweenOrderByTradeDate(
            String symbol, String period, LocalDate startDate, LocalDate endDate);

    /**
     * 根据股票代码和周期查询最新的K线数据
     */
    Optional<StockKLine> findTopBySymbolAndPeriodOrderByTradeDateDesc(String symbol, String period);

    /**
     * 根据股票代码和周期查询指定数量的最新K线数据
     */
    @Query("SELECT k FROM StockKLine k WHERE k.symbol = :symbol AND k.period = :period ORDER BY k.tradeDate DESC")
    List<StockKLine> findLatestKLines(@Param("symbol") String symbol, 
                                      @Param("period") String period);

    /**
     * 根据股票代码、周期和具体日期查询K线数据
     */
    Optional<StockKLine> findBySymbolAndPeriodAndTradeDate(String symbol, String period, LocalDate tradeDate);

    /**
     * 查询指定日期的所有股票K线数据
     */
    List<StockKLine> findByPeriodAndTradeDateOrderBySymbol(String period, LocalDate tradeDate);

    /**
     * 查询指定股票的所有周期数据
     */
    List<StockKLine> findBySymbolAndTradeDateOrderByPeriod(String symbol, LocalDate tradeDate);

    /**
     * 查询涨幅最大的K线数据
     */
    @Query("SELECT k FROM StockKLine k WHERE k.period = :period AND k.tradeDate = :tradeDate AND k.changePercent IS NOT NULL ORDER BY k.changePercent DESC")
    List<StockKLine> findTopGainersByDate(@Param("period") String period, @Param("tradeDate") LocalDate tradeDate);

    /**
     * 查询跌幅最大的K线数据
     */
    @Query("SELECT k FROM StockKLine k WHERE k.period = :period AND k.tradeDate = :tradeDate AND k.changePercent IS NOT NULL ORDER BY k.changePercent ASC")
    List<StockKLine> findTopLosersByDate(@Param("period") String period, @Param("tradeDate") LocalDate tradeDate);

    /**
     * 查询成交量最大的K线数据
     */
    @Query("SELECT k FROM StockKLine k WHERE k.period = :period AND k.tradeDate = :tradeDate AND k.volume IS NOT NULL ORDER BY k.volume DESC")
    List<StockKLine> findTopVolumeByDate(@Param("period") String period, @Param("tradeDate") LocalDate tradeDate);

    /**
     * 查询指定股票的价格范围
     */
    @Query("SELECT MIN(k.lowPrice), MAX(k.highPrice) FROM StockKLine k WHERE k.symbol = :symbol AND k.period = :period AND k.tradeDate BETWEEN :startDate AND :endDate")
    Object[] findPriceRangeBySymbolAndPeriod(@Param("symbol") String symbol, 
                                             @Param("period") String period,
                                             @Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);

    /**
     * 查询指定股票的成交量统计
     */
    @Query("SELECT AVG(k.volume), MAX(k.volume), MIN(k.volume) FROM StockKLine k WHERE k.symbol = :symbol AND k.period = :period AND k.tradeDate BETWEEN :startDate AND :endDate")
    Object[] findVolumeStatsBySymbolAndPeriod(@Param("symbol") String symbol, 
                                              @Param("period") String period,
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);

    /**
     * 查询指定时间范围内的交易日
     */
    @Query("SELECT DISTINCT k.tradeDate FROM StockKLine k WHERE k.period = :period AND k.tradeDate BETWEEN :startDate AND :endDate ORDER BY k.tradeDate")
    List<LocalDate> findTradingDaysBetween(@Param("period") String period,
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);

    /**
     * 查询最新交易日
     */
    @Query("SELECT MAX(k.tradeDate) FROM StockKLine k WHERE k.period = :period")
    LocalDate findLatestTradingDate(@Param("period") String period);

    /**
     * 查询指定股票的数据完整性
     */
    @Query("SELECT COUNT(k) FROM StockKLine k WHERE k.symbol = :symbol AND k.period = :period AND k.tradeDate BETWEEN :startDate AND :endDate")
    long countBySymbolAndPeriodAndDateRange(@Param("symbol") String symbol, 
                                            @Param("period") String period,
                                            @Param("startDate") LocalDate startDate, 
                                            @Param("endDate") LocalDate endDate);

    /**
     * 查询停牌股票
     */
    List<StockKLine> findByPeriodAndTradeDateAndIsSuspendedTrue(String period, LocalDate tradeDate);

    /**
     * 查询ST股票
     */
    List<StockKLine> findByPeriodAndTradeDateAndIsStTrue(String period, LocalDate tradeDate);

    /**
     * 查询涨停股票
     */
    @Query("SELECT k FROM StockKLine k WHERE k.period = :period AND k.tradeDate = :tradeDate AND k.changePercent >= 9.8")
    List<StockKLine> findLimitUpStocks(@Param("period") String period, @Param("tradeDate") LocalDate tradeDate);

    /**
     * 查询跌停股票
     */
    @Query("SELECT k FROM StockKLine k WHERE k.period = :period AND k.tradeDate = :tradeDate AND k.changePercent <= -9.8")
    List<StockKLine> findLimitDownStocks(@Param("period") String period, @Param("tradeDate") LocalDate tradeDate);

    /**
     * 查询一字板股票（开盘价=收盘价=最高价=最低价）
     */
    @Query("SELECT k FROM StockKLine k WHERE k.period = :period AND k.tradeDate = :tradeDate AND k.openPrice = k.closePrice AND k.openPrice = k.highPrice AND k.openPrice = k.lowPrice")
    List<StockKLine> findOneBoardStocks(@Param("period") String period, @Param("tradeDate") LocalDate tradeDate);

    /**
     * 查询放量股票（成交量大于前N日平均值的倍数）
     */
    @Query("SELECT k FROM StockKLine k WHERE k.symbol = :symbol AND k.period = :period AND k.tradeDate = :tradeDate AND k.volume > (SELECT AVG(k2.volume) * :multiplier FROM StockKLine k2 WHERE k2.symbol = :symbol AND k2.period = :period AND k2.tradeDate < :tradeDate ORDER BY k2.tradeDate DESC LIMIT :days)")
    List<StockKLine> findHighVolumeStocks(@Param("symbol") String symbol,
                                          @Param("period") String period,
                                          @Param("tradeDate") LocalDate tradeDate,
                                          @Param("multiplier") double multiplier,
                                          @Param("days") int days);

    /**
     * 查询缩量股票
     */
    @Query("SELECT k FROM StockKLine k WHERE k.symbol = :symbol AND k.period = :period AND k.tradeDate = :tradeDate AND k.volume < (SELECT AVG(k2.volume) * :multiplier FROM StockKLine k2 WHERE k2.symbol = :symbol AND k2.period = :period AND k2.tradeDate < :tradeDate ORDER BY k2.tradeDate DESC LIMIT :days)")
    List<StockKLine> findLowVolumeStocks(@Param("symbol") String symbol,
                                         @Param("period") String period,
                                         @Param("tradeDate") LocalDate tradeDate,
                                         @Param("multiplier") double multiplier,
                                         @Param("days") int days);

    /**
     * 查询连续上涨股票
     */
    @Query(value = "SELECT * FROM stock_kline k1 WHERE k1.symbol = :symbol AND k1.period = :period AND k1.trade_date = :tradeDate AND (SELECT COUNT(*) FROM stock_kline k2 WHERE k2.symbol = k1.symbol AND k2.period = k1.period AND k2.trade_date <= k1.trade_date AND k2.trade_date > DATE_SUB(k1.trade_date, INTERVAL :days DAY) AND k2.change_percent > 0) >= :days", nativeQuery = true)
    List<StockKLine> findContinuousRisingStocks(@Param("symbol") String symbol,
                                                @Param("period") String period,
                                                @Param("tradeDate") LocalDate tradeDate,
                                                @Param("days") int days);

    /**
     * 查询连续下跌股票
     */
    @Query(value = "SELECT * FROM stock_kline k1 WHERE k1.symbol = :symbol AND k1.period = :period AND k1.trade_date = :tradeDate AND (SELECT COUNT(*) FROM stock_kline k2 WHERE k2.symbol = k1.symbol AND k2.period = k1.period AND k2.trade_date <= k1.trade_date AND k2.trade_date > DATE_SUB(k1.trade_date, INTERVAL :days DAY) AND k2.change_percent < 0) >= :days", nativeQuery = true)
    List<StockKLine> findContinuousFallingStocks(@Param("symbol") String symbol,
                                                 @Param("period") String period,
                                                 @Param("tradeDate") LocalDate tradeDate,
                                                 @Param("days") int days);

    /**
     * 删除指定日期之前的历史数据
     */
    void deleteByTradeDateBefore(LocalDate cutoffDate);

    /**
     * 统计各周期的数据量
     */
    @Query("SELECT k.period, COUNT(k) FROM StockKLine k GROUP BY k.period")
    List<Object[]> countByPeriod();

    /**
     * 查询数据源统计
     */
    @Query("SELECT k.dataSource, COUNT(k) FROM StockKLine k GROUP BY k.dataSource")
    List<Object[]> countByDataSource();
}