package com.jd.genie.repository;

import com.jd.genie.entity.StockInfo;
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
 * 股票信息Repository
 */
@Repository
public interface StockInfoRepository extends JpaRepository<StockInfo, Long> {
    
    /**
     * 根据股票代码查找
     */
    Optional<StockInfo> findByStockCode(String stockCode);
    
    /**
     * 根据股票代码和市场查找
     */
    Optional<StockInfo> findByStockCodeAndMarket(String stockCode, String market);
    
    /**
     * 根据股票名称查找
     */
    List<StockInfo> findByStockNameContaining(String stockName);
    
    /**
     * 根据市场查找
     */
    List<StockInfo> findByMarket(String market);
    
    /**
     * 根据行业查找
     */
    List<StockInfo> findByIndustry(String industry);
    
    /**
     * 根据板块查找
     */
    List<StockInfo> findBySector(String sector);
    
    /**
     * 根据状态查找
     */
    List<StockInfo> findByStatus(String status);
    
    /**
     * 查找ST股票
     */
    List<StockInfo> findByIsStTrue();
    
    /**
     * 根据市场和状态查找
     */
    List<StockInfo> findByMarketAndStatus(String market, String status);
    
    /**
     * 根据行业和状态查找
     */
    List<StockInfo> findByIndustryAndStatus(String industry, String status);
    
    /**
     * 模糊搜索股票（代码或名称）
     */
    @Query("SELECT s FROM StockInfo s WHERE s.stockCode LIKE %:keyword% OR s.stockName LIKE %:keyword%")
    List<StockInfo> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * 分页模糊搜索股票
     */
    @Query("SELECT s FROM StockInfo s WHERE s.stockCode LIKE %:keyword% OR s.stockName LIKE %:keyword%")
    Page<StockInfo> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 根据市值范围查找
     */
    @Query("SELECT s FROM StockInfo s WHERE s.marketCap BETWEEN :minCap AND :maxCap AND s.status = 'ACTIVE'")
    List<StockInfo> findByMarketCapRange(@Param("minCap") BigDecimal minCap, @Param("maxCap") BigDecimal maxCap);
    
    /**
     * 根据PE范围查找
     */
    @Query("SELECT s FROM StockInfo s WHERE s.peRatio BETWEEN :minPe AND :maxPe AND s.status = 'ACTIVE'")
    List<StockInfo> findByPeRatioRange(@Param("minPe") BigDecimal minPe, @Param("maxPe") BigDecimal maxPe);
    
    /**
     * 根据PB范围查找
     */
    @Query("SELECT s FROM StockInfo s WHERE s.pbRatio BETWEEN :minPb AND :maxPb AND s.status = 'ACTIVE'")
    List<StockInfo> findByPbRatioRange(@Param("minPb") BigDecimal minPb, @Param("maxPb") BigDecimal maxPb);
    
    /**
     * 根据ROE范围查找
     */
    @Query("SELECT s FROM StockInfo s WHERE s.roe BETWEEN :minRoe AND :maxRoe AND s.status = 'ACTIVE'")
    List<StockInfo> findByRoeRange(@Param("minRoe") BigDecimal minRoe, @Param("maxRoe") BigDecimal maxRoe);
    
    /**
     * 查找高股息股票
     */
    @Query("SELECT s FROM StockInfo s WHERE s.dividendYield >= :minYield AND s.status = 'ACTIVE' ORDER BY s.dividendYield DESC")
    List<StockInfo> findHighDividendStocks(@Param("minYield") BigDecimal minYield);
    
    /**
     * 查找低估值股票（PE < 15 且 PB < 2）
     */
    @Query("SELECT s FROM StockInfo s WHERE s.peRatio < 15 AND s.pbRatio < 2 AND s.status = 'ACTIVE' ORDER BY s.peRatio ASC")
    List<StockInfo> findUndervaluedStocks();
    
    /**
     * 查找成长股（ROE > 15% 且净利率 > 10%）
     */
    @Query("SELECT s FROM StockInfo s WHERE s.roe > 15 AND s.netMargin > 10 AND s.status = 'ACTIVE' ORDER BY s.roe DESC")
    List<StockInfo> findGrowthStocks();
    
    /**
     * 查找蓝筹股（市值 > 1000亿且ROE > 10%）
     */
    @Query("SELECT s FROM StockInfo s WHERE s.marketCap > 10000000 AND s.roe > 10 AND s.status = 'ACTIVE' ORDER BY s.marketCap DESC")
    List<StockInfo> findBlueChipStocks();
    
    /**
     * 根据行业统计股票数量
     */
    @Query("SELECT s.industry, COUNT(s) FROM StockInfo s WHERE s.status = 'ACTIVE' GROUP BY s.industry ORDER BY COUNT(s) DESC")
    List<Object[]> countByIndustry();
    
    /**
     * 根据市场统计股票数量
     */
    @Query("SELECT s.market, COUNT(s) FROM StockInfo s WHERE s.status = 'ACTIVE' GROUP BY s.market")
    List<Object[]> countByMarket();
    
    /**
     * 获取行业平均PE
     */
    @Query("SELECT s.industry, AVG(s.peRatio) FROM StockInfo s WHERE s.peRatio IS NOT NULL AND s.status = 'ACTIVE' GROUP BY s.industry")
    List<Object[]> getAvgPeByIndustry();
    
    /**
     * 获取行业平均PB
     */
    @Query("SELECT s.industry, AVG(s.pbRatio) FROM StockInfo s WHERE s.pbRatio IS NOT NULL AND s.status = 'ACTIVE' GROUP BY s.industry")
    List<Object[]> getAvgPbByIndustry();
    
    /**
     * 获取行业平均ROE
     */
    @Query("SELECT s.industry, AVG(s.roe) FROM StockInfo s WHERE s.roe IS NOT NULL AND s.status = 'ACTIVE' GROUP BY s.industry")
    List<Object[]> getAvgRoeByIndustry();
    
    /**
     * 查找需要更新的股票（超过指定时间未更新）
     */
    @Query("SELECT s FROM StockInfo s WHERE s.lastUpdated IS NULL OR s.lastUpdated < :cutoffTime")
    List<StockInfo> findStocksNeedingUpdate(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * 查找最近上市的股票
     */
    @Query("SELECT s FROM StockInfo s WHERE s.listDate >= :fromDate AND s.status = 'ACTIVE' ORDER BY s.listDate DESC")
    List<StockInfo> findRecentlyListedStocks(@Param("fromDate") LocalDateTime fromDate);
    
    /**
     * 根据多个条件筛选股票
     */
    @Query("SELECT s FROM StockInfo s WHERE " +
           "(:market IS NULL OR s.market = :market) AND " +
           "(:industry IS NULL OR s.industry = :industry) AND " +
           "(:minMarketCap IS NULL OR s.marketCap >= :minMarketCap) AND " +
           "(:maxMarketCap IS NULL OR s.marketCap <= :maxMarketCap) AND " +
           "(:minPe IS NULL OR s.peRatio >= :minPe) AND " +
           "(:maxPe IS NULL OR s.peRatio <= :maxPe) AND " +
           "(:minPb IS NULL OR s.pbRatio >= :minPb) AND " +
           "(:maxPb IS NULL OR s.pbRatio <= :maxPb) AND " +
           "(:minRoe IS NULL OR s.roe >= :minRoe) AND " +
           "(:maxRoe IS NULL OR s.roe <= :maxRoe) AND " +
           "s.status = 'ACTIVE'")
    Page<StockInfo> findByMultipleConditions(
            @Param("market") String market,
            @Param("industry") String industry,
            @Param("minMarketCap") BigDecimal minMarketCap,
            @Param("maxMarketCap") BigDecimal maxMarketCap,
            @Param("minPe") BigDecimal minPe,
            @Param("maxPe") BigDecimal maxPe,
            @Param("minPb") BigDecimal minPb,
            @Param("maxPb") BigDecimal maxPb,
            @Param("minRoe") BigDecimal minRoe,
            @Param("maxRoe") BigDecimal maxRoe,
            Pageable pageable);
    
    /**
     * 获取所有不重复的行业列表
     */
    @Query("SELECT DISTINCT s.industry FROM StockInfo s WHERE s.industry IS NOT NULL AND s.status = 'ACTIVE' ORDER BY s.industry")
    List<String> findAllIndustries();
    
    /**
     * 获取所有不重复的板块列表
     */
    @Query("SELECT DISTINCT s.sector FROM StockInfo s WHERE s.sector IS NOT NULL AND s.status = 'ACTIVE' ORDER BY s.sector")
    List<String> findAllSectors();
    
    /**
     * 获取所有不重复的市场列表
     */
    @Query("SELECT DISTINCT s.market FROM StockInfo s WHERE s.market IS NOT NULL ORDER BY s.market")
    List<String> findAllMarkets();
    
    /**
     * 检查股票代码是否存在
     */
    boolean existsByStockCode(String stockCode);
    
    /**
     * 检查股票代码和市场组合是否存在
     */
    boolean existsByStockCodeAndMarket(String stockCode, String market);
    
    /**
     * 统计活跃股票总数
     */
    @Query("SELECT COUNT(s) FROM StockInfo s WHERE s.status = 'ACTIVE'")
    long countActiveStocks();
    
    /**
     * 统计ST股票数量
     */
    @Query("SELECT COUNT(s) FROM StockInfo s WHERE s.isSt = true AND s.status = 'ACTIVE'")
    long countStStocks();
    
    /**
     * 获取市值前N的股票
     */
    @Query("SELECT s FROM StockInfo s WHERE s.marketCap IS NOT NULL AND s.status = 'ACTIVE' ORDER BY s.marketCap DESC")
    List<StockInfo> findTopByMarketCap(Pageable pageable);
    
    /**
     * 获取ROE前N的股票
     */
    @Query("SELECT s FROM StockInfo s WHERE s.roe IS NOT NULL AND s.status = 'ACTIVE' ORDER BY s.roe DESC")
    List<StockInfo> findTopByRoe(Pageable pageable);
    
    /**
     * 获取股息率前N的股票
     */
    @Query("SELECT s FROM StockInfo s WHERE s.dividendYield IS NOT NULL AND s.status = 'ACTIVE' ORDER BY s.dividendYield DESC")
    List<StockInfo> findTopByDividendYield(Pageable pageable);
    
    /**
     * 批量更新最后更新时间
     */
    @Query("UPDATE StockInfo s SET s.lastUpdated = :updateTime WHERE s.stockCode IN :stockCodes")
    int batchUpdateLastUpdated(@Param("stockCodes") List<String> stockCodes, @Param("updateTime") LocalDateTime updateTime);
    
    /**
     * 批量更新股票状态
     */
    @Query("UPDATE StockInfo s SET s.status = :status WHERE s.stockCode IN :stockCodes")
    int batchUpdateStatus(@Param("stockCodes") List<String> stockCodes, @Param("status") String status);
}