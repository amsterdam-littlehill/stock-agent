package com.jd.genie.repository;

import com.jd.genie.entity.PortfolioTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 投资组合交易记录数据访问接口
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Repository
public interface PortfolioTransactionRepository extends JpaRepository<PortfolioTransaction, Long> {
    
    /**
     * 根据投资组合ID查找交易记录
     */
    List<PortfolioTransaction> findByPortfolioIdOrderByCreatedAtDesc(Long portfolioId);
    
    /**
     * 根据投资组合ID和股票代码查找交易记录
     */
    List<PortfolioTransaction> findByPortfolioIdAndStockCodeOrderByCreatedAtDesc(Long portfolioId, String stockCode);
    
    /**
     * 根据投资组合ID和交易类型查找交易记录
     */
    List<PortfolioTransaction> findByPortfolioIdAndTransactionTypeOrderByCreatedAtDesc(Long portfolioId, String transactionType);
    
    /**
     * 根据交易流水号查找交易记录
     */
    Optional<PortfolioTransaction> findByTransactionId(String transactionId);
    
    /**
     * 根据订单ID查找交易记录
     */
    List<PortfolioTransaction> findByOrderId(String orderId);
    
    /**
     * 根据券商交易ID查找交易记录
     */
    Optional<PortfolioTransaction> findByBrokerTransactionId(String brokerTransactionId);
    
    /**
     * 根据股票代码查找所有交易记录
     */
    List<PortfolioTransaction> findByStockCodeOrderByCreatedAtDesc(String stockCode);
    
    /**
     * 根据交易状态查找交易记录
     */
    List<PortfolioTransaction> findByPortfolioIdAndStatusOrderByCreatedAtDesc(Long portfolioId, String status);
    
    /**
     * 根据交易来源查找交易记录
     */
    List<PortfolioTransaction> findByPortfolioIdAndSourceOrderByCreatedAtDesc(Long portfolioId, String source);
    
    /**
     * 根据市场查找交易记录
     */
    List<PortfolioTransaction> findByPortfolioIdAndMarketOrderByCreatedAtDesc(Long portfolioId, String market);
    
    /**
     * 查找指定时间范围内的交易记录
     */
    List<PortfolioTransaction> findByPortfolioIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long portfolioId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 查找指定交易日期的交易记录
     */
    List<PortfolioTransaction> findByPortfolioIdAndTradeDateOrderByCreatedAtDesc(Long portfolioId, String tradeDate);
    
    /**
     * 查找T+0交易记录
     */
    List<PortfolioTransaction> findByPortfolioIdAndIsDayTradeTrueOrderByCreatedAtDesc(Long portfolioId);
    
    /**
     * 查找融资融券交易记录
     */
    List<PortfolioTransaction> findByPortfolioIdAndIsMarginTradeTrueOrderByCreatedAtDesc(Long portfolioId);
    
    /**
     * 查找未确认的交易记录
     */
    List<PortfolioTransaction> findByPortfolioIdAndConfirmedFalseOrderByCreatedAtDesc(Long portfolioId);
    
    /**
     * 查找盈利的交易记录
     */
    List<PortfolioTransaction> findByPortfolioIdAndRealizedPnlGreaterThanOrderByCreatedAtDesc(
            Long portfolioId, BigDecimal threshold);
    
    /**
     * 查找亏损的交易记录
     */
    List<PortfolioTransaction> findByPortfolioIdAndRealizedPnlLessThanOrderByCreatedAtDesc(
            Long portfolioId, BigDecimal threshold);
    
    /**
     * 查找大额交易记录
     */
    List<PortfolioTransaction> findByPortfolioIdAndTotalAmountGreaterThanOrderByTotalAmountDesc(
            Long portfolioId, BigDecimal minAmount);
    
    /**
     * 查找最近的交易记录
     */
    List<PortfolioTransaction> findTop10ByPortfolioIdOrderByCreatedAtDesc(Long portfolioId);
    
    /**
     * 查找指定策略的交易记录
     */
    List<PortfolioTransaction> findByPortfolioIdAndStrategyIdOrderByCreatedAtDesc(Long portfolioId, String strategyId);
    
    /**
     * 查找指定智能体执行的交易记录
     */
    List<PortfolioTransaction> findByPortfolioIdAndExecutingAgentOrderByCreatedAtDesc(Long portfolioId, String executingAgent);
    
    /**
     * 查找关联分析任务的交易记录
     */
    List<PortfolioTransaction> findByAnalysisTaskIdOrderByCreatedAtDesc(Long analysisTaskId);
    
    /**
     * 统计投资组合的交易次数
     */
    long countByPortfolioId(Long portfolioId);
    
    /**
     * 统计指定类型的交易次数
     */
    long countByPortfolioIdAndTransactionType(Long portfolioId, String transactionType);
    
    /**
     * 统计指定股票的交易次数
     */
    long countByPortfolioIdAndStockCode(Long portfolioId, String stockCode);
    
    /**
     * 统计指定时间范围内的交易次数
     */
    long countByPortfolioIdAndCreatedAtBetween(Long portfolioId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 统计今日交易次数
     */
    long countByPortfolioIdAndTradeDate(Long portfolioId, String tradeDate);
    
    /**
     * 计算投资组合的总交易金额
     */
    @Query("SELECT SUM(t.totalAmount) FROM PortfolioTransaction t WHERE t.portfolioId = :portfolioId")
    BigDecimal sumTotalAmountByPortfolioId(@Param("portfolioId") Long portfolioId);
    
    /**
     * 计算投资组合的买入总金额
     */
    @Query("SELECT SUM(t.totalAmount) FROM PortfolioTransaction t " +
           "WHERE t.portfolioId = :portfolioId AND t.transactionType = 'BUY'")
    BigDecimal sumBuyAmountByPortfolioId(@Param("portfolioId") Long portfolioId);
    
    /**
     * 计算投资组合的卖出总金额
     */
    @Query("SELECT SUM(t.totalAmount) FROM PortfolioTransaction t " +
           "WHERE t.portfolioId = :portfolioId AND t.transactionType = 'SELL'")
    BigDecimal sumSellAmountByPortfolioId(@Param("portfolioId") Long portfolioId);
    
    /**
     * 计算投资组合的总手续费
     */
    @Query("SELECT SUM(t.totalFees) FROM PortfolioTransaction t WHERE t.portfolioId = :portfolioId")
    BigDecimal sumTotalFeesByPortfolioId(@Param("portfolioId") Long portfolioId);
    
    /**
     * 计算投资组合的总已实现盈亏
     */
    @Query("SELECT SUM(t.realizedPnl) FROM PortfolioTransaction t " +
           "WHERE t.portfolioId = :portfolioId AND t.realizedPnl IS NOT NULL")
    BigDecimal sumRealizedPnlByPortfolioId(@Param("portfolioId") Long portfolioId);
    
    /**
     * 计算投资组合的分红总额
     */
    @Query("SELECT SUM(t.totalAmount) FROM PortfolioTransaction t " +
           "WHERE t.portfolioId = :portfolioId AND t.transactionType = 'DIVIDEND'")
    BigDecimal sumDividendAmountByPortfolioId(@Param("portfolioId") Long portfolioId);
    
    /**
     * 按股票代码分组统计交易金额
     */
    @Query("SELECT t.stockCode, SUM(t.totalAmount) FROM PortfolioTransaction t " +
           "WHERE t.portfolioId = :portfolioId GROUP BY t.stockCode")
    List<Object[]> sumAmountByStockCode(@Param("portfolioId") Long portfolioId);
    
    /**
     * 按交易类型分组统计交易金额
     */
    @Query("SELECT t.transactionType, SUM(t.totalAmount) FROM PortfolioTransaction t " +
           "WHERE t.portfolioId = :portfolioId GROUP BY t.transactionType")
    List<Object[]> sumAmountByTransactionType(@Param("portfolioId") Long portfolioId);
    
    /**
     * 按月份分组统计交易金额
     */
    @Query("SELECT YEAR(t.createdAt), MONTH(t.createdAt), SUM(t.totalAmount) " +
           "FROM PortfolioTransaction t WHERE t.portfolioId = :portfolioId " +
           "GROUP BY YEAR(t.createdAt), MONTH(t.createdAt) " +
           "ORDER BY YEAR(t.createdAt), MONTH(t.createdAt)")
    List<Object[]> sumAmountByMonth(@Param("portfolioId") Long portfolioId);
    
    /**
     * 按日期分组统计交易次数
     */
    @Query("SELECT t.tradeDate, COUNT(t) FROM PortfolioTransaction t " +
           "WHERE t.portfolioId = :portfolioId GROUP BY t.tradeDate ORDER BY t.tradeDate")
    List<Object[]> countByTradeDate(@Param("portfolioId") Long portfolioId);
    
    /**
     * 计算指定股票的平均买入价格
     */
    @Query("SELECT AVG(t.price) FROM PortfolioTransaction t " +
           "WHERE t.portfolioId = :portfolioId AND t.stockCode = :stockCode " +
           "AND t.transactionType = 'BUY'")
    BigDecimal avgBuyPriceByStockCode(@Param("portfolioId") Long portfolioId, 
                                     @Param("stockCode") String stockCode);
    
    /**
     * 计算指定股票的平均卖出价格
     */
    @Query("SELECT AVG(t.price) FROM PortfolioTransaction t " +
           "WHERE t.portfolioId = :portfolioId AND t.stockCode = :stockCode " +
           "AND t.transactionType = 'SELL'")
    BigDecimal avgSellPriceByStockCode(@Param("portfolioId") Long portfolioId, 
                                      @Param("stockCode") String stockCode);
    
    /**
     * 查找指定股票的首次买入记录
     */
    @Query("SELECT t FROM PortfolioTransaction t " +
           "WHERE t.portfolioId = :portfolioId AND t.stockCode = :stockCode " +
           "AND t.transactionType = 'BUY' ORDER BY t.createdAt ASC")
    List<PortfolioTransaction> findFirstBuyTransaction(@Param("portfolioId") Long portfolioId, 
                                                     @Param("stockCode") String stockCode);
    
    /**
     * 查找指定股票的最后卖出记录
     */
    @Query("SELECT t FROM PortfolioTransaction t " +
           "WHERE t.portfolioId = :portfolioId AND t.stockCode = :stockCode " +
           "AND t.transactionType = 'SELL' ORDER BY t.createdAt DESC")
    List<PortfolioTransaction> findLastSellTransaction(@Param("portfolioId") Long portfolioId, 
                                                     @Param("stockCode") String stockCode);
    
    /**
     * 查找换手率高的交易记录
     */
    @Query("SELECT t FROM PortfolioTransaction t " +
           "WHERE t.portfolioId = :portfolioId " +
           "AND t.totalAmount > (SELECT AVG(t2.totalAmount) * 2 FROM PortfolioTransaction t2 " +
           "WHERE t2.portfolioId = :portfolioId)")
    List<PortfolioTransaction> findHighTurnoverTransactions(@Param("portfolioId") Long portfolioId);
    
    /**
     * 查找异常交易记录
     */
    @Query("SELECT t FROM PortfolioTransaction t " +
           "WHERE t.portfolioId = :portfolioId " +
           "AND (t.totalFees > t.totalAmount * 0.1 OR t.price <= 0)")
    List<PortfolioTransaction> findAbnormalTransactions(@Param("portfolioId") Long portfolioId);
    
    /**
     * 更新交易状态
     */
    @Query("UPDATE PortfolioTransaction t SET t.status = :status, t.updatedAt = :updatedAt " +
           "WHERE t.id = :transactionId")
    void updateStatus(@Param("transactionId") Long transactionId,
                     @Param("status") String status,
                     @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 更新交易确认状态
     */
    @Query("UPDATE PortfolioTransaction t SET t.confirmed = :confirmed, " +
           "t.confirmedAt = :confirmedAt, t.updatedAt = :updatedAt " +
           "WHERE t.id = :transactionId")
    void updateConfirmation(@Param("transactionId") Long transactionId,
                           @Param("confirmed") Boolean confirmed,
                           @Param("confirmedAt") LocalDateTime confirmedAt,
                           @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 更新已实现盈亏
     */
    @Query("UPDATE PortfolioTransaction t SET t.realizedPnl = :realizedPnl, " +
           "t.realizedPnlPercent = :realizedPnlPercent, t.updatedAt = :updatedAt " +
           "WHERE t.id = :transactionId")
    void updateRealizedPnl(@Param("transactionId") Long transactionId,
                          @Param("realizedPnl") BigDecimal realizedPnl,
                          @Param("realizedPnlPercent") BigDecimal realizedPnlPercent,
                          @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 批量更新交易状态
     */
    @Query("UPDATE PortfolioTransaction t SET t.status = :newStatus, t.updatedAt = :updatedAt " +
           "WHERE t.id IN :transactionIds")
    void batchUpdateStatus(@Param("transactionIds") List<Long> transactionIds,
                          @Param("newStatus") String newStatus,
                          @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 删除投资组合的所有交易记录
     */
    void deleteByPortfolioId(Long portfolioId);
    
    /**
     * 删除指定状态的交易记录
     */
    void deleteByStatus(String status);
    
    /**
     * 删除指定时间之前的交易记录
     */
    void deleteByCreatedAtBefore(LocalDateTime date);
    
    /**
     * 删除未确认的交易记录
     */
    void deleteByConfirmedFalse();
}