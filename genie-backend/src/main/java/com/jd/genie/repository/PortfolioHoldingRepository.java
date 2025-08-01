package com.jd.genie.repository;

import com.jd.genie.entity.PortfolioHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 投资组合持仓数据访问接口
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Repository
public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, Long> {
    
    /**
     * 根据投资组合ID查找所有持仓
     */
    List<PortfolioHolding> findByPortfolioId(Long portfolioId);
    
    /**
     * 根据投资组合ID和股票代码查找持仓
     */
    Optional<PortfolioHolding> findByPortfolioIdAndStockCode(Long portfolioId, String stockCode);
    
    /**
     * 根据投资组合ID和状态查找持仓
     */
    List<PortfolioHolding> findByPortfolioIdAndStatus(Long portfolioId, String status);
    
    /**
     * 根据股票代码查找所有持仓
     */
    List<PortfolioHolding> findByStockCode(String stockCode);
    
    /**
     * 根据行业查找持仓
     */
    List<PortfolioHolding> findByPortfolioIdAndSector(Long portfolioId, String sector);
    
    /**
     * 根据市场查找持仓
     */
    List<PortfolioHolding> findByPortfolioIdAndMarket(Long portfolioId, String market);
    
    /**
     * 查找核心持仓
     */
    List<PortfolioHolding> findByPortfolioIdAndIsCoreHoldingTrue(Long portfolioId);
    
    /**
     * 查找盈利的持仓
     */
    List<PortfolioHolding> findByPortfolioIdAndUnrealizedPnlGreaterThan(Long portfolioId, BigDecimal threshold);
    
    /**
     * 查找亏损的持仓
     */
    List<PortfolioHolding> findByPortfolioIdAndUnrealizedPnlLessThan(Long portfolioId, BigDecimal threshold);
    
    /**
     * 查找权重大于指定值的持仓
     */
    List<PortfolioHolding> findByPortfolioIdAndWeightGreaterThanOrderByWeightDesc(Long portfolioId, BigDecimal minWeight);
    
    /**
     * 查找市值最大的持仓
     */
    List<PortfolioHolding> findByPortfolioIdOrderByMarketValueDesc(Long portfolioId);
    
    /**
     * 查找收益率最高的持仓
     */
    List<PortfolioHolding> findByPortfolioIdOrderByUnrealizedPnlPercentDesc(Long portfolioId);
    
    /**
     * 查找持仓时间最长的持仓
     */
    List<PortfolioHolding> findByPortfolioIdOrderByFirstBuyDateAsc(Long portfolioId);
    
    /**
     * 查找最近交易的持仓
     */
    List<PortfolioHolding> findByPortfolioIdOrderByLastTradeDateDesc(Long portfolioId);
    
    /**
     * 查找启用止损的持仓
     */
    List<PortfolioHolding> findByPortfolioIdAndStopLossEnabledTrue(Long portfolioId);
    
    /**
     * 查找启用止盈的持仓
     */
    List<PortfolioHolding> findByPortfolioIdAndTakeProfitEnabledTrue(Long portfolioId);
    
    /**
     * 查找触发止损的持仓
     */
    @Query("SELECT h FROM PortfolioHolding h WHERE h.portfolioId = :portfolioId " +
           "AND h.stopLossEnabled = true AND h.currentPrice <= h.stopLossPrice")
    List<PortfolioHolding> findTriggeredStopLoss(@Param("portfolioId") Long portfolioId);
    
    /**
     * 查找触发止盈的持仓
     */
    @Query("SELECT h FROM PortfolioHolding h WHERE h.portfolioId = :portfolioId " +
           "AND h.takeProfitEnabled = true AND h.currentPrice >= h.takeProfitPrice")
    List<PortfolioHolding> findTriggeredTakeProfit(@Param("portfolioId") Long portfolioId);
    
    /**
     * 统计投资组合的持仓数量
     */
    long countByPortfolioId(Long portfolioId);
    
    /**
     * 统计投资组合中指定状态的持仓数量
     */
    long countByPortfolioIdAndStatus(Long portfolioId, String status);
    
    /**
     * 统计投资组合中指定行业的持仓数量
     */
    long countByPortfolioIdAndSector(Long portfolioId, String sector);
    
    /**
     * 计算投资组合的总市值
     */
    @Query("SELECT SUM(h.marketValue) FROM PortfolioHolding h WHERE h.portfolioId = :portfolioId")
    BigDecimal sumMarketValueByPortfolioId(@Param("portfolioId") Long portfolioId);
    
    /**
     * 计算投资组合的总成本
     */
    @Query("SELECT SUM(h.totalCost) FROM PortfolioHolding h WHERE h.portfolioId = :portfolioId")
    BigDecimal sumTotalCostByPortfolioId(@Param("portfolioId") Long portfolioId);
    
    /**
     * 计算投资组合的总浮动盈亏
     */
    @Query("SELECT SUM(h.unrealizedPnl) FROM PortfolioHolding h WHERE h.portfolioId = :portfolioId")
    BigDecimal sumUnrealizedPnlByPortfolioId(@Param("portfolioId") Long portfolioId);
    
    /**
     * 计算投资组合的总已实现盈亏
     */
    @Query("SELECT SUM(h.realizedPnl) FROM PortfolioHolding h WHERE h.portfolioId = :portfolioId")
    BigDecimal sumRealizedPnlByPortfolioId(@Param("portfolioId") Long portfolioId);
    
    /**
     * 计算投资组合的今日盈亏
     */
    @Query("SELECT SUM(h.todayPnl) FROM PortfolioHolding h WHERE h.portfolioId = :portfolioId")
    BigDecimal sumTodayPnlByPortfolioId(@Param("portfolioId") Long portfolioId);
    
    /**
     * 计算投资组合的分红总额
     */
    @Query("SELECT SUM(h.dividendAmount) FROM PortfolioHolding h WHERE h.portfolioId = :portfolioId")
    BigDecimal sumDividendAmountByPortfolioId(@Param("portfolioId") Long portfolioId);
    
    /**
     * 按行业分组统计市值
     */
    @Query("SELECT h.sector, SUM(h.marketValue) FROM PortfolioHolding h " +
           "WHERE h.portfolioId = :portfolioId GROUP BY h.sector")
    List<Object[]> sumMarketValueBySector(@Param("portfolioId") Long portfolioId);
    
    /**
     * 按市场分组统计市值
     */
    @Query("SELECT h.market, SUM(h.marketValue) FROM PortfolioHolding h " +
           "WHERE h.portfolioId = :portfolioId GROUP BY h.market")
    List<Object[]> sumMarketValueByMarket(@Param("portfolioId") Long portfolioId);
    
    /**
     * 查找权重偏离目标权重的持仓
     */
    @Query("SELECT h FROM PortfolioHolding h WHERE h.portfolioId = :portfolioId " +
           "AND h.targetWeight IS NOT NULL " +
           "AND ABS(h.weight - h.targetWeight) > :threshold")
    List<PortfolioHolding> findWeightDeviations(@Param("portfolioId") Long portfolioId, 
                                               @Param("threshold") BigDecimal threshold);
    
    /**
     * 查找需要再平衡的持仓
     */
    @Query("SELECT h FROM PortfolioHolding h WHERE h.portfolioId = :portfolioId " +
           "AND h.targetWeight IS NOT NULL " +
           "AND (h.weight > h.targetWeight * 1.1 OR h.weight < h.targetWeight * 0.9)")
    List<PortfolioHolding> findHoldingsNeedingRebalance(@Param("portfolioId") Long portfolioId);
    
    /**
     * 查找高风险持仓
     */
    List<PortfolioHolding> findByPortfolioIdAndRiskLevel(Long portfolioId, String riskLevel);
    
    /**
     * 查找指定评级的持仓
     */
    List<PortfolioHolding> findByPortfolioIdAndRating(Long portfolioId, String rating);
    
    /**
     * 查找价格接近目标价的持仓
     */
    @Query("SELECT h FROM PortfolioHolding h WHERE h.portfolioId = :portfolioId " +
           "AND h.targetPrice IS NOT NULL " +
           "AND ABS(h.currentPrice - h.targetPrice) / h.targetPrice < :threshold")
    List<PortfolioHolding> findNearTargetPrice(@Param("portfolioId") Long portfolioId, 
                                              @Param("threshold") BigDecimal threshold);
    
    /**
     * 更新持仓的当前价格和市值
     */
    @Query("UPDATE PortfolioHolding h SET h.currentPrice = :currentPrice, " +
           "h.marketValue = :marketValue, " +
           "h.unrealizedPnl = :unrealizedPnl, " +
           "h.unrealizedPnlPercent = :unrealizedPnlPercent, " +
           "h.priceUpdatedAt = :priceUpdatedAt, " +
           "h.updatedAt = :updatedAt " +
           "WHERE h.id = :holdingId")
    void updatePriceAndValue(@Param("holdingId") Long holdingId,
                            @Param("currentPrice") BigDecimal currentPrice,
                            @Param("marketValue") BigDecimal marketValue,
                            @Param("unrealizedPnl") BigDecimal unrealizedPnl,
                            @Param("unrealizedPnlPercent") BigDecimal unrealizedPnlPercent,
                            @Param("priceUpdatedAt") LocalDateTime priceUpdatedAt,
                            @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 更新持仓的权重
     */
    @Query("UPDATE PortfolioHolding h SET h.weight = :weight, h.updatedAt = :updatedAt " +
           "WHERE h.id = :holdingId")
    void updateWeight(@Param("holdingId") Long holdingId,
                     @Param("weight") BigDecimal weight,
                     @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 批量更新持仓权重
     */
    @Query("UPDATE PortfolioHolding h SET h.weight = :weight, h.updatedAt = :updatedAt " +
           "WHERE h.portfolioId = :portfolioId AND h.stockCode = :stockCode")
    void updateWeightByStockCode(@Param("portfolioId") Long portfolioId,
                                @Param("stockCode") String stockCode,
                                @Param("weight") BigDecimal weight,
                                @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 更新持仓的今日盈亏
     */
    @Query("UPDATE PortfolioHolding h SET h.todayPnl = :todayPnl, " +
           "h.todayPnlPercent = :todayPnlPercent, " +
           "h.updatedAt = :updatedAt " +
           "WHERE h.id = :holdingId")
    void updateTodayPnl(@Param("holdingId") Long holdingId,
                       @Param("todayPnl") BigDecimal todayPnl,
                       @Param("todayPnlPercent") BigDecimal todayPnlPercent,
                       @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 更新持仓的止损止盈价格
     */
    @Query("UPDATE PortfolioHolding h SET h.stopLossPrice = :stopLossPrice, " +
           "h.takeProfitPrice = :takeProfitPrice, " +
           "h.stopLossEnabled = :stopLossEnabled, " +
           "h.takeProfitEnabled = :takeProfitEnabled, " +
           "h.updatedAt = :updatedAt " +
           "WHERE h.id = :holdingId")
    void updateStopLossTakeProfit(@Param("holdingId") Long holdingId,
                                 @Param("stopLossPrice") BigDecimal stopLossPrice,
                                 @Param("takeProfitPrice") BigDecimal takeProfitPrice,
                                 @Param("stopLossEnabled") Boolean stopLossEnabled,
                                 @Param("takeProfitEnabled") Boolean takeProfitEnabled,
                                 @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 删除投资组合的所有持仓
     */
    void deleteByPortfolioId(Long portfolioId);
    
    /**
     * 删除指定股票的持仓
     */
    void deleteByPortfolioIdAndStockCode(Long portfolioId, String stockCode);
    
    /**
     * 删除零持仓记录
     */
    void deleteByQuantity(Integer quantity);
    
    /**
     * 删除指定状态的持仓
     */
    void deleteByStatus(String status);
}