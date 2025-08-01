package com.jd.genie.repository;

import com.jd.genie.entity.AnalysisResult;
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
 * 分析结果Repository
 */
@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {
    
    /**
     * 根据任务ID查找分析结果
     */
    List<AnalysisResult> findByTaskId(Long taskId);
    
    /**
     * 根据股票代码查找分析结果
     */
    List<AnalysisResult> findByStockCode(String stockCode);
    
    /**
     * 根据智能体类型查找分析结果
     */
    List<AnalysisResult> findByAgentType(String agentType);
    
    /**
     * 根据分析类型查找分析结果
     */
    List<AnalysisResult> findByAnalysisType(String analysisType);
    
    /**
     * 根据任务ID和智能体类型查找分析结果
     */
    Optional<AnalysisResult> findByTaskIdAndAgentType(Long taskId, String agentType);
    
    /**
     * 根据任务ID和分析类型查找分析结果
     */
    Optional<AnalysisResult> findByTaskIdAndAnalysisType(Long taskId, String analysisType);
    
    /**
     * 根据股票代码和分析类型查找最新分析结果
     */
    @Query("SELECT r FROM AnalysisResult r WHERE r.stockCode = :stockCode AND r.analysisType = :analysisType ORDER BY r.createdAt DESC")
    List<AnalysisResult> findLatestByStockCodeAndAnalysisType(@Param("stockCode") String stockCode,
                                                             @Param("analysisType") String analysisType,
                                                             Pageable pageable);
    
    /**
     * 根据股票代码查找最新分析结果
     */
    @Query("SELECT r FROM AnalysisResult r WHERE r.stockCode = :stockCode ORDER BY r.createdAt DESC")
    List<AnalysisResult> findLatestByStockCode(@Param("stockCode") String stockCode, Pageable pageable);
    
    /**
     * 根据推荐操作查找分析结果
     */
    List<AnalysisResult> findByRecommendation(String recommendation);
    
    /**
     * 根据风险等级查找分析结果
     */
    List<AnalysisResult> findByRiskLevel(String riskLevel);
    
    /**
     * 查找高置信度的分析结果
     */
    @Query("SELECT r FROM AnalysisResult r WHERE r.confidence >= :minConfidence ORDER BY r.confidence DESC")
    List<AnalysisResult> findHighConfidenceResults(@Param("minConfidence") BigDecimal minConfidence);
    
    /**
     * 查找买入建议的分析结果
     */
    @Query("SELECT r FROM AnalysisResult r WHERE r.recommendation = 'BUY' AND r.confidence >= :minConfidence ORDER BY r.confidence DESC")
    List<AnalysisResult> findBuyRecommendations(@Param("minConfidence") BigDecimal minConfidence);
    
    /**
     * 查找卖出建议的分析结果
     */
    @Query("SELECT r FROM AnalysisResult r WHERE r.recommendation = 'SELL' AND r.confidence >= :minConfidence ORDER BY r.confidence DESC")
    List<AnalysisResult> findSellRecommendations(@Param("minConfidence") BigDecimal minConfidence);
    
    /**
     * 根据时间范围查找分析结果
     */
    @Query("SELECT r FROM AnalysisResult r WHERE r.createdAt BETWEEN :startTime AND :endTime ORDER BY r.createdAt DESC")
    List<AnalysisResult> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据股票代码和时间范围查找分析结果
     */
    @Query("SELECT r FROM AnalysisResult r WHERE r.stockCode = :stockCode AND r.createdAt BETWEEN :startTime AND :endTime ORDER BY r.createdAt DESC")
    List<AnalysisResult> findByStockCodeAndTimeRange(@Param("stockCode") String stockCode,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据智能体类型和时间范围查找分析结果
     */
    @Query("SELECT r FROM AnalysisResult r WHERE r.agentType = :agentType AND r.createdAt BETWEEN :startTime AND :endTime ORDER BY r.createdAt DESC")
    List<AnalysisResult> findByAgentTypeAndTimeRange(@Param("agentType") String agentType,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计各智能体类型的分析结果数量
     */
    @Query("SELECT r.agentType, COUNT(r) FROM AnalysisResult r GROUP BY r.agentType")
    List<Object[]> countByAgentType();
    
    /**
     * 统计各分析类型的结果数量
     */
    @Query("SELECT r.analysisType, COUNT(r) FROM AnalysisResult r GROUP BY r.analysisType")
    List<Object[]> countByAnalysisType();
    
    /**
     * 统计各推荐操作的数量
     */
    @Query("SELECT r.recommendation, COUNT(r) FROM AnalysisResult r WHERE r.recommendation IS NOT NULL GROUP BY r.recommendation")
    List<Object[]> countByRecommendation();
    
    /**
     * 统计各风险等级的数量
     */
    @Query("SELECT r.riskLevel, COUNT(r) FROM AnalysisResult r WHERE r.riskLevel IS NOT NULL GROUP BY r.riskLevel")
    List<Object[]> countByRiskLevel();
    
    /**
     * 获取平均置信度
     */
    @Query("SELECT AVG(r.confidence) FROM AnalysisResult r WHERE r.confidence IS NOT NULL")
    BigDecimal getAverageConfidence();
    
    /**
     * 获取各智能体类型的平均置信度
     */
    @Query("SELECT r.agentType, AVG(r.confidence) FROM AnalysisResult r WHERE r.confidence IS NOT NULL GROUP BY r.agentType")
    List<Object[]> getAverageConfidenceByAgentType();
    
    /**
     * 获取各分析类型的平均置信度
     */
    @Query("SELECT r.analysisType, AVG(r.confidence) FROM AnalysisResult r WHERE r.confidence IS NOT NULL GROUP BY r.analysisType")
    List<Object[]> getAverageConfidenceByAnalysisType();
    
    /**
     * 获取平均处理时间
     */
    @Query("SELECT AVG(r.durationMs) FROM AnalysisResult r WHERE r.durationMs IS NOT NULL")
    Double getAverageProcessingTime();
    
    /**
     * 获取各智能体类型的平均处理时间
     */
    @Query("SELECT r.agentType, AVG(r.durationMs) FROM AnalysisResult r WHERE r.durationMs IS NOT NULL GROUP BY r.agentType")
    List<Object[]> getAverageProcessingTimeByAgentType();
    
    /**
     * 获取股票的综合分析统计
     */
    @Query("SELECT COUNT(r), " +
           "COUNT(CASE WHEN r.recommendation = 'BUY' THEN 1 END), " +
           "COUNT(CASE WHEN r.recommendation = 'HOLD' THEN 1 END), " +
           "COUNT(CASE WHEN r.recommendation = 'SELL' THEN 1 END), " +
           "AVG(r.confidence), " +
           "AVG(r.targetPrice) " +
           "FROM AnalysisResult r WHERE r.stockCode = :stockCode")
    Object[] getStockAnalysisStatistics(@Param("stockCode") String stockCode);
    
    /**
     * 获取智能体性能统计
     */
    @Query("SELECT COUNT(r), AVG(r.confidence), AVG(r.durationMs), AVG(r.score) " +
           "FROM AnalysisResult r WHERE r.agentType = :agentType")
    Object[] getAgentPerformanceStatistics(@Param("agentType") String agentType);
    
    /**
     * 查找目标价格最高的分析结果
     */
    @Query("SELECT r FROM AnalysisResult r WHERE r.targetPrice IS NOT NULL ORDER BY r.targetPrice DESC")
    List<AnalysisResult> findTopTargetPrices(Pageable pageable);
    
    /**
     * 查找上涨空间最大的分析结果
     */
    @Query("SELECT r FROM AnalysisResult r WHERE r.upsidePotential IS NOT NULL ORDER BY r.upsidePotential DESC")
    List<AnalysisResult> findTopUpsidePotential(Pageable pageable);
    
    /**
     * 查找评分最高的分析结果
     */
    @Query("SELECT r FROM AnalysisResult r WHERE r.score IS NOT NULL ORDER BY r.score DESC")
    List<AnalysisResult> findTopScoredResults(Pageable pageable);
    
    /**
     * 根据股票代码和推荐操作统计
     */
    @Query("SELECT r.recommendation, COUNT(r), AVG(r.confidence) " +
           "FROM AnalysisResult r WHERE r.stockCode = :stockCode AND r.recommendation IS NOT NULL " +
           "GROUP BY r.recommendation")
    List<Object[]> getRecommendationStatsByStock(@Param("stockCode") String stockCode);
    
    /**
     * 查找一致性分析结果（多个智能体给出相同建议）
     */
    @Query("SELECT r.stockCode, r.recommendation, COUNT(DISTINCT r.agentType) as agentCount " +
           "FROM AnalysisResult r WHERE r.taskId = :taskId AND r.recommendation IS NOT NULL " +
           "GROUP BY r.stockCode, r.recommendation HAVING COUNT(DISTINCT r.agentType) >= :minAgents")
    List<Object[]> findConsistentRecommendations(@Param("taskId") Long taskId, @Param("minAgents") int minAgents);
    
    /**
     * 查找分歧分析结果（智能体给出不同建议）
     */
    @Query("SELECT r.stockCode, COUNT(DISTINCT r.recommendation) as recommendationCount " +
           "FROM AnalysisResult r WHERE r.taskId = :taskId AND r.recommendation IS NOT NULL " +
           "GROUP BY r.stockCode HAVING COUNT(DISTINCT r.recommendation) > 1")
    List<Object[]> findDivergentRecommendations(@Param("taskId") Long taskId);
    
    /**
     * 获取最新的技术分析结果
     */
    @Query("SELECT r FROM AnalysisResult r WHERE r.stockCode = :stockCode AND r.analysisType = 'TECHNICAL' ORDER BY r.createdAt DESC")
    Optional<AnalysisResult> findLatestTechnicalAnalysis(@Param("stockCode") String stockCode);
    
    /**
     * 获取最新的基本面分析结果
     */
    @Query("SELECT r FROM AnalysisResult r WHERE r.stockCode = :stockCode AND r.analysisType = 'FUNDAMENTAL' ORDER BY r.createdAt DESC")
    Optional<AnalysisResult> findLatestFundamentalAnalysis(@Param("stockCode") String stockCode);
    
    /**
     * 获取最新的情绪分析结果
     */
    @Query("SELECT r FROM AnalysisResult r WHERE r.stockCode = :stockCode AND r.analysisType = 'SENTIMENT' ORDER BY r.createdAt DESC")
    Optional<AnalysisResult> findLatestSentimentAnalysis(@Param("stockCode") String stockCode);
    
    /**
     * 分页查询分析结果
     */
    Page<AnalysisResult> findByStockCodeOrderByCreatedAtDesc(String stockCode, Pageable pageable);
    
    /**
     * 分页查询智能体的分析结果
     */
    Page<AnalysisResult> findByAgentTypeOrderByCreatedAtDesc(String agentType, Pageable pageable);
    
    /**
     * 查找需要清理的过期分析结果
     */
    @Query("SELECT r FROM AnalysisResult r WHERE r.createdAt < :cutoffTime")
    List<AnalysisResult> findExpiredResults(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * 删除过期分析结果
     */
    @Query("DELETE FROM AnalysisResult r WHERE r.createdAt < :cutoffTime")
    int deleteExpiredResults(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * 删除指定任务的分析结果
     */
    int deleteByTaskId(Long taskId);
    
    /**
     * 删除指定股票的分析结果
     */
    int deleteByStockCode(String stockCode);
    
    /**
     * 统计总分析结果数
     */
    @Query("SELECT COUNT(r) FROM AnalysisResult r")
    long countAllResults();
    
    /**
     * 统计指定股票的分析结果数
     */
    long countByStockCode(String stockCode);
    
    /**
     * 统计指定智能体的分析结果数
     */
    long countByAgentType(String agentType);
    
    /**
     * 统计指定分析类型的结果数
     */
    long countByAnalysisType(String analysisType);
    
    /**
     * 检查是否存在指定任务的分析结果
     */
    boolean existsByTaskId(Long taskId);
    
    /**
     * 检查是否存在指定任务和智能体类型的分析结果
     */
    boolean existsByTaskIdAndAgentType(Long taskId, String agentType);
}