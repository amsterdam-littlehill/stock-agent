package com.jd.genie.repository;

import com.jd.genie.entity.StockData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 股票数据Repository
 */
@Repository
public interface StockDataRepository extends JpaRepository<StockData, Long> {

    /**
     * 根据股票代码查找最新数据
     */
    Optional<StockData> findBySymbolAndIsLatestTrue(String symbol);

    /**
     * 根据股票代码查找所有历史数据
     */
    List<StockData> findBySymbolOrderByUpdateTimeDesc(String symbol);

    /**
     * 根据股票代码或名称模糊查询
     */
    @Query("SELECT s FROM StockData s WHERE s.isLatest = true AND (s.symbol LIKE %:keyword% OR s.name LIKE %:keyword%) ORDER BY s.symbol")
    List<StockData> findBySymbolContainingOrNameContaining(@Param("keyword") String symbol, @Param("keyword") String name);

    /**
     * 查找所有最新的股票数据
     */
    List<StockData> findByIsLatestTrueOrderBySymbol();

    /**
     * 根据数据源查找股票数据
     */
    List<StockData> findByDataSourceAndIsLatestTrue(String dataSource);

    /**
     * 根据交易状态查找股票数据
     */
    List<StockData> findByTradingStatusAndIsLatestTrue(String tradingStatus);

    /**
     * 查找指定时间范围内的股票数据
     */
    @Query("SELECT s FROM StockData s WHERE s.symbol = :symbol AND s.updateTime BETWEEN :startTime AND :endTime ORDER BY s.updateTime DESC")
    List<StockData> findBySymbolAndUpdateTimeBetween(@Param("symbol") String symbol, 
                                                     @Param("startTime") LocalDateTime startTime, 
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 查找涨幅榜前N名
     */
    @Query("SELECT s FROM StockData s WHERE s.isLatest = true AND s.changePercent IS NOT NULL ORDER BY s.changePercent DESC")
    List<StockData> findTopGainers();

    /**
     * 查找跌幅榜前N名
     */
    @Query("SELECT s FROM StockData s WHERE s.isLatest = true AND s.changePercent IS NOT NULL ORDER BY s.changePercent ASC")
    List<StockData> findTopLosers();

    /**
     * 查找成交量榜前N名
     */
    @Query("SELECT s FROM StockData s WHERE s.isLatest = true AND s.volume IS NOT NULL ORDER BY s.volume DESC")
    List<StockData> findTopByVolume();

    /**
     * 查找成交额榜前N名
     */
    @Query("SELECT s FROM StockData s WHERE s.isLatest = true AND s.turnover IS NOT NULL ORDER BY s.turnover DESC")
    List<StockData> findTopByTurnover();

    /**
     * 根据市值范围查找股票
     */
    @Query("SELECT s FROM StockData s WHERE s.isLatest = true AND s.marketCap BETWEEN :minMarketCap AND :maxMarketCap ORDER BY s.marketCap DESC")
    List<StockData> findByMarketCapBetween(@Param("minMarketCap") java.math.BigDecimal minMarketCap, 
                                           @Param("maxMarketCap") java.math.BigDecimal maxMarketCap);

    /**
     * 根据PE范围查找股票
     */
    @Query("SELECT s FROM StockData s WHERE s.isLatest = true AND s.peRatio BETWEEN :minPe AND :maxPe ORDER BY s.peRatio ASC")
    List<StockData> findByPeRatioBetween(@Param("minPe") java.math.BigDecimal minPe, 
                                         @Param("maxPe") java.math.BigDecimal maxPe);

    /**
     * 更新股票的isLatest状态
     */
    @Modifying
    @Query("UPDATE StockData s SET s.isLatest = :isLatest WHERE s.symbol = :symbol")
    int updateIsLatestBySymbol(@Param("symbol") String symbol, @Param("isLatest") Boolean isLatest);

    /**
     * 删除指定时间之前的历史数据
     */
    @Modifying
    @Query("DELETE FROM StockData s WHERE s.isLatest = false AND s.updateTime < :cutoffTime")
    int deleteHistoricalDataBefore(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 统计股票总数
     */
    @Query("SELECT COUNT(DISTINCT s.symbol) FROM StockData s WHERE s.isLatest = true")
    long countDistinctSymbols();

    /**
     * 统计各数据源的股票数量
     */
    @Query("SELECT s.dataSource, COUNT(s) FROM StockData s WHERE s.isLatest = true GROUP BY s.dataSource")
    List<Object[]> countByDataSource();

    /**
     * 查找最近更新的股票数据
     */
    @Query("SELECT s FROM StockData s WHERE s.isLatest = true ORDER BY s.updateTime DESC")
    List<StockData> findRecentlyUpdated();

    /**
     * 查找指定时间之后更新的股票数据
     */
    @Query("SELECT s FROM StockData s WHERE s.isLatest = true AND s.updateTime > :afterTime ORDER BY s.updateTime DESC")
    List<StockData> findUpdatedAfter(@Param("afterTime") LocalDateTime afterTime);

    /**
     * 查找异常数据（价格为0或负数）
     */
    @Query("SELECT s FROM StockData s WHERE s.isLatest = true AND (s.currentPrice <= 0 OR s.currentPrice IS NULL)")
    List<StockData> findAbnormalData();

    /**
     * 批量查找股票数据
     */
    @Query("SELECT s FROM StockData s WHERE s.symbol IN :symbols AND s.isLatest = true")
    List<StockData> findBySymbolIn(@Param("symbols") List<String> symbols);

    /**
     * 查找活跃股票（成交量大于平均值）
     */
    @Query("SELECT s FROM StockData s WHERE s.isLatest = true AND s.volume > (SELECT AVG(sd.volume) FROM StockData sd WHERE sd.isLatest = true) ORDER BY s.volume DESC")
    List<StockData> findActiveStocks();

    /**
     * 查找停牌股票
     */
    @Query("SELECT s FROM StockData s WHERE s.isLatest = true AND s.tradingStatus = 'SUSPENDED'")
    List<StockData> findSuspendedStocks();

    /**
     * 根据涨跌幅范围查找股票
     */
    @Query("SELECT s FROM StockData s WHERE s.isLatest = true AND s.changePercent BETWEEN :minChange AND :maxChange ORDER BY s.changePercent DESC")
    List<StockData> findByChangePercentBetween(@Param("minChange") java.math.BigDecimal minChange, 
                                               @Param("maxChange") java.math.BigDecimal maxChange);

    /**
     * 查找新股（上市时间较短）
     */
    @Query("SELECT s FROM StockData s WHERE s.isLatest = true AND s.createdAt > :sinceDate ORDER BY s.createdAt DESC")
    List<StockData> findNewStocks(@Param("sinceDate") LocalDateTime sinceDate);
}