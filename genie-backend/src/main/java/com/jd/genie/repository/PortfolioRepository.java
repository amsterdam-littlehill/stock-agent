package com.jd.genie.repository;

import com.jd.genie.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 投资组合数据访问接口
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    
    /**
     * 根据用户ID查找投资组合列表
     */
    List<Portfolio> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * 根据用户ID和状态查找投资组合
     */
    List<Portfolio> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, String status);
    
    /**
     * 根据用户ID和组合名称查找投资组合
     */
    Optional<Portfolio> findByUserIdAndName(String userId, String name);
    
    /**
     * 查找活跃的投资组合
     */
    List<Portfolio> findByStatusOrderByCreatedAtDesc(String status);
    
    /**
     * 查找公开的投资组合
     */
    List<Portfolio> findByIsPublicTrueOrderByTotalReturnPercentDesc();
    
    /**
     * 根据投资目标查找投资组合
     */
    List<Portfolio> findByInvestmentObjectiveOrderByCreatedAtDesc(String investmentObjective);
    
    /**
     * 根据风险承受能力查找投资组合
     */
    List<Portfolio> findByRiskToleranceOrderByCreatedAtDesc(String riskTolerance);
    
    /**
     * 查找需要再平衡的投资组合
     */
    @Query("SELECT p FROM Portfolio p WHERE p.autoRebalanceEnabled = true " +
           "AND (p.lastRebalanced IS NULL OR p.lastRebalanced < :thresholdDate)")
    List<Portfolio> findPortfoliosNeedingRebalance(@Param("thresholdDate") LocalDateTime thresholdDate);
    
    /**
     * 统计用户的投资组合数量
     */
    long countByUserId(String userId);
    
    /**
     * 统计活跃投资组合数量
     */
    long countByStatus(String status);
    
    /**
     * 查找总价值大于指定金额的投资组合
     */
    List<Portfolio> findByCurrentValueGreaterThanOrderByCurrentValueDesc(BigDecimal minValue);
    
    /**
     * 查找收益率大于指定值的投资组合
     */
    List<Portfolio> findByTotalReturnPercentGreaterThanOrderByTotalReturnPercentDesc(BigDecimal minReturnPercent);
    
    /**
     * 查找夏普比率大于指定值的投资组合
     */
    List<Portfolio> findBySharpeRatioGreaterThanOrderBySharpeRatioDesc(BigDecimal minSharpeRatio);
    
    /**
     * 根据基准指数查找投资组合
     */
    List<Portfolio> findByBenchmarkCodeOrderByCreatedAtDesc(String benchmarkCode);
    
    /**
     * 查找指定时间范围内创建的投资组合
     */
    List<Portfolio> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 查找最近更新的投资组合
     */
    List<Portfolio> findTop10ByOrderByUpdatedAtDesc();
    
    /**
     * 查找表现最好的投资组合
     */
    List<Portfolio> findTop10ByStatusOrderByTotalReturnPercentDesc(String status);
    
    /**
     * 查找风险调整收益最好的投资组合
     */
    List<Portfolio> findTop10ByStatusOrderBySharpeRatioDesc(String status);
    
    /**
     * 统计投资组合的总价值
     */
    @Query("SELECT SUM(p.currentValue) FROM Portfolio p WHERE p.userId = :userId AND p.status = :status")
    BigDecimal sumCurrentValueByUserIdAndStatus(@Param("userId") String userId, @Param("status") String status);
    
    /**
     * 统计投资组合的总收益
     */
    @Query("SELECT SUM(p.totalReturn) FROM Portfolio p WHERE p.userId = :userId AND p.status = :status")
    BigDecimal sumTotalReturnByUserIdAndStatus(@Param("userId") String userId, @Param("status") String status);
    
    /**
     * 计算用户投资组合的平均收益率
     */
    @Query("SELECT AVG(p.totalReturnPercent) FROM Portfolio p WHERE p.userId = :userId AND p.status = :status")
    BigDecimal avgTotalReturnPercentByUserIdAndStatus(@Param("userId") String userId, @Param("status") String status);
    
    /**
     * 计算用户投资组合的平均夏普比率
     */
    @Query("SELECT AVG(p.sharpeRatio) FROM Portfolio p WHERE p.userId = :userId AND p.status = :status " +
           "AND p.sharpeRatio IS NOT NULL")
    BigDecimal avgSharpeRatioByUserIdAndStatus(@Param("userId") String userId, @Param("status") String status);
    
    /**
     * 查找包含指定标签的投资组合
     */
    @Query("SELECT p FROM Portfolio p WHERE p.tags LIKE %:tag%")
    List<Portfolio> findByTagsContaining(@Param("tag") String tag);
    
    /**
     * 查找名称包含关键词的投资组合
     */
    List<Portfolio> findByUserIdAndNameContainingIgnoreCaseOrderByCreatedAtDesc(String userId, String keyword);
    
    /**
     * 查找描述包含关键词的投资组合
     */
    List<Portfolio> findByUserIdAndDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(String userId, String keyword);
    
    /**
     * 更新投资组合的当前价值
     */
    @Query("UPDATE Portfolio p SET p.currentValue = :currentValue, p.updatedAt = :updatedAt " +
           "WHERE p.id = :portfolioId")
    void updateCurrentValue(@Param("portfolioId") Long portfolioId, 
                           @Param("currentValue") BigDecimal currentValue,
                           @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 更新投资组合的收益信息
     */
    @Query("UPDATE Portfolio p SET p.totalReturn = :totalReturn, " +
           "p.totalReturnPercent = :totalReturnPercent, " +
           "p.todayReturn = :todayReturn, " +
           "p.todayReturnPercent = :todayReturnPercent, " +
           "p.updatedAt = :updatedAt " +
           "WHERE p.id = :portfolioId")
    void updateReturnInfo(@Param("portfolioId") Long portfolioId,
                         @Param("totalReturn") BigDecimal totalReturn,
                         @Param("totalReturnPercent") BigDecimal totalReturnPercent,
                         @Param("todayReturn") BigDecimal todayReturn,
                         @Param("todayReturnPercent") BigDecimal todayReturnPercent,
                         @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 更新投资组合的风险指标
     */
    @Query("UPDATE Portfolio p SET p.sharpeRatio = :sharpeRatio, " +
           "p.maxDrawdown = :maxDrawdown, " +
           "p.volatility = :volatility, " +
           "p.beta = :beta, " +
           "p.updatedAt = :updatedAt " +
           "WHERE p.id = :portfolioId")
    void updateRiskMetrics(@Param("portfolioId") Long portfolioId,
                          @Param("sharpeRatio") BigDecimal sharpeRatio,
                          @Param("maxDrawdown") BigDecimal maxDrawdown,
                          @Param("volatility") BigDecimal volatility,
                          @Param("beta") BigDecimal beta,
                          @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 更新投资组合的再平衡时间
     */
    @Query("UPDATE Portfolio p SET p.lastRebalanced = :lastRebalanced, p.updatedAt = :updatedAt " +
           "WHERE p.id = :portfolioId")
    void updateLastRebalanced(@Param("portfolioId") Long portfolioId,
                             @Param("lastRebalanced") LocalDateTime lastRebalanced,
                             @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 批量更新投资组合状态
     */
    @Query("UPDATE Portfolio p SET p.status = :newStatus, p.updatedAt = :updatedAt " +
           "WHERE p.id IN :portfolioIds")
    void batchUpdateStatus(@Param("portfolioIds") List<Long> portfolioIds,
                          @Param("newStatus") String newStatus,
                          @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 删除指定用户的所有投资组合
     */
    void deleteByUserId(String userId);
    
    /**
     * 删除指定状态的投资组合
     */
    void deleteByStatus(String status);
    
    /**
     * 删除指定时间之前创建的投资组合
     */
    void deleteByCreatedAtBefore(LocalDateTime date);
}