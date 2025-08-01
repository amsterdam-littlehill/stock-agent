package com.jd.genie.repository;

import com.jd.genie.entity.AnalysisTask;
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
 * 分析任务Repository
 */
@Repository
public interface AnalysisTaskRepository extends JpaRepository<AnalysisTask, Long> {
    
    /**
     * 根据请求ID查找任务
     */
    Optional<AnalysisTask> findByRequestId(String requestId);
    
    /**
     * 根据股票代码查找任务
     */
    List<AnalysisTask> findByStockCode(String stockCode);
    
    /**
     * 根据用户ID查找任务
     */
    List<AnalysisTask> findByUserId(String userId);
    
    /**
     * 根据状态查找任务
     */
    List<AnalysisTask> findByStatus(String status);
    
    /**
     * 根据分析类型查找任务
     */
    List<AnalysisTask> findByAnalysisType(String analysisType);
    
    /**
     * 根据股票代码和状态查找任务
     */
    List<AnalysisTask> findByStockCodeAndStatus(String stockCode, String status);
    
    /**
     * 根据用户ID和状态查找任务
     */
    List<AnalysisTask> findByUserIdAndStatus(String userId, String status);
    
    /**
     * 分页查询用户的分析历史
     */
    Page<AnalysisTask> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    /**
     * 分页查询股票的分析历史
     */
    Page<AnalysisTask> findByStockCodeOrderByCreatedAtDesc(String stockCode, Pageable pageable);
    
    /**
     * 查找待处理的任务（按优先级排序）
     */
    @Query("SELECT t FROM AnalysisTask t WHERE t.status = 'PENDING' ORDER BY t.priority ASC, t.createdAt ASC")
    List<AnalysisTask> findPendingTasksOrderByPriority();
    
    /**
     * 查找运行中的任务
     */
    @Query("SELECT t FROM AnalysisTask t WHERE t.status = 'RUNNING' ORDER BY t.startTime ASC")
    List<AnalysisTask> findRunningTasks();
    
    /**
     * 查找超时的任务
     */
    @Query("SELECT t FROM AnalysisTask t WHERE t.status = 'RUNNING' AND t.startTime < :timeoutTime")
    List<AnalysisTask> findTimeoutTasks(@Param("timeoutTime") LocalDateTime timeoutTime);
    
    /**
     * 查找可重试的失败任务
     */
    @Query("SELECT t FROM AnalysisTask t WHERE t.status = 'FAILED' AND t.retryCount < t.maxRetries")
    List<AnalysisTask> findRetryableTasks();
    
    /**
     * 根据时间范围查找任务
     */
    @Query("SELECT t FROM AnalysisTask t WHERE t.createdAt BETWEEN :startTime AND :endTime ORDER BY t.createdAt DESC")
    List<AnalysisTask> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据完成时间范围查找已完成任务
     */
    @Query("SELECT t FROM AnalysisTask t WHERE t.status = 'COMPLETED' AND t.endTime BETWEEN :startTime AND :endTime ORDER BY t.endTime DESC")
    List<AnalysisTask> findCompletedTasksByTimeRange(@Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找高置信度的分析结果
     */
    @Query("SELECT t FROM AnalysisTask t WHERE t.status = 'COMPLETED' AND t.confidence >= :minConfidence ORDER BY t.confidence DESC")
    List<AnalysisTask> findHighConfidenceTasks(@Param("minConfidence") BigDecimal minConfidence);
    
    /**
     * 根据推荐操作查找任务
     */
    @Query("SELECT t FROM AnalysisTask t WHERE t.status = 'COMPLETED' AND t.recommendation = :recommendation ORDER BY t.confidence DESC")
    List<AnalysisTask> findByRecommendation(@Param("recommendation") String recommendation);
    
    /**
     * 根据风险等级查找任务
     */
    @Query("SELECT t FROM AnalysisTask t WHERE t.status = 'COMPLETED' AND t.riskLevel = :riskLevel ORDER BY t.confidence DESC")
    List<AnalysisTask> findByRiskLevel(@Param("riskLevel") String riskLevel);
    
    /**
     * 查找买入建议的任务
     */
    @Query("SELECT t FROM AnalysisTask t WHERE t.status = 'COMPLETED' AND t.recommendation = 'BUY' AND t.confidence >= :minConfidence ORDER BY t.confidence DESC")
    List<AnalysisTask> findBuyRecommendations(@Param("minConfidence") BigDecimal minConfidence);
    
    /**
     * 统计各状态的任务数量
     */
    @Query("SELECT t.status, COUNT(t) FROM AnalysisTask t GROUP BY t.status")
    List<Object[]> countByStatus();
    
    /**
     * 统计各分析类型的任务数量
     */
    @Query("SELECT t.analysisType, COUNT(t) FROM AnalysisTask t GROUP BY t.analysisType")
    List<Object[]> countByAnalysisType();
    
    /**
     * 统计各推荐操作的数量
     */
    @Query("SELECT t.recommendation, COUNT(t) FROM AnalysisTask t WHERE t.status = 'COMPLETED' GROUP BY t.recommendation")
    List<Object[]> countByRecommendation();
    
    /**
     * 统计各风险等级的数量
     */
    @Query("SELECT t.riskLevel, COUNT(t) FROM AnalysisTask t WHERE t.status = 'COMPLETED' GROUP BY t.riskLevel")
    List<Object[]> countByRiskLevel();
    
    /**
     * 获取平均处理时间
     */
    @Query("SELECT AVG(t.durationMs) FROM AnalysisTask t WHERE t.status = 'COMPLETED' AND t.durationMs IS NOT NULL")
    Double getAverageProcessingTime();
    
    /**
     * 获取成功率
     */
    @Query("SELECT (COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(t)) FROM AnalysisTask t")
    Double getSuccessRate();
    
    /**
     * 获取平均置信度
     */
    @Query("SELECT AVG(t.confidence) FROM AnalysisTask t WHERE t.status = 'COMPLETED' AND t.confidence IS NOT NULL")
    BigDecimal getAverageConfidence();
    
    /**
     * 获取指定时间段的任务统计
     */
    @Query("SELECT COUNT(t), " +
           "COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END), " +
           "COUNT(CASE WHEN t.status = 'FAILED' THEN 1 END), " +
           "AVG(CASE WHEN t.status = 'COMPLETED' THEN t.durationMs END), " +
           "AVG(CASE WHEN t.status = 'COMPLETED' THEN t.confidence END) " +
           "FROM AnalysisTask t WHERE t.createdAt BETWEEN :startTime AND :endTime")
    Object[] getTaskStatistics(@Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime);
    
    /**
     * 获取用户的任务统计
     */
    @Query("SELECT COUNT(t), " +
           "COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END), " +
           "COUNT(CASE WHEN t.status = 'FAILED' THEN 1 END), " +
           "AVG(CASE WHEN t.status = 'COMPLETED' THEN t.confidence END) " +
           "FROM AnalysisTask t WHERE t.userId = :userId")
    Object[] getUserTaskStatistics(@Param("userId") String userId);
    
    /**
     * 获取股票的分析统计
     */
    @Query("SELECT COUNT(t), " +
           "COUNT(CASE WHEN t.recommendation = 'BUY' THEN 1 END), " +
           "COUNT(CASE WHEN t.recommendation = 'HOLD' THEN 1 END), " +
           "COUNT(CASE WHEN t.recommendation = 'SELL' THEN 1 END), " +
           "AVG(t.confidence), " +
           "AVG(t.targetPrice) " +
           "FROM AnalysisTask t WHERE t.stockCode = :stockCode AND t.status = 'COMPLETED'")
    Object[] getStockAnalysisStatistics(@Param("stockCode") String stockCode);
    
    /**
     * 查找需要清理的过期任务
     */
    @Query("SELECT t FROM AnalysisTask t WHERE t.status IN ('COMPLETED', 'FAILED', 'CANCELLED') AND t.endTime < :cutoffTime")
    List<AnalysisTask> findExpiredTasks(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * 查找最近的分析任务
     */
    @Query("SELECT t FROM AnalysisTask t WHERE t.stockCode = :stockCode AND t.status = 'COMPLETED' ORDER BY t.endTime DESC")
    List<AnalysisTask> findRecentAnalysisByStock(@Param("stockCode") String stockCode, Pageable pageable);
    
    /**
     * 查找相似的分析任务
     */
    @Query("SELECT t FROM AnalysisTask t WHERE t.stockCode = :stockCode AND t.analysisType = :analysisType " +
           "AND t.analysisDepth = :analysisDepth AND t.status = 'COMPLETED' " +
           "AND t.createdAt >= :fromTime ORDER BY t.createdAt DESC")
    List<AnalysisTask> findSimilarTasks(@Param("stockCode") String stockCode,
                                       @Param("analysisType") String analysisType,
                                       @Param("analysisDepth") String analysisDepth,
                                       @Param("fromTime") LocalDateTime fromTime);
    
    /**
     * 检查是否存在重复任务
     */
    @Query("SELECT COUNT(t) > 0 FROM AnalysisTask t WHERE t.stockCode = :stockCode " +
           "AND t.analysisType = :analysisType AND t.userId = :userId " +
           "AND t.status IN ('PENDING', 'RUNNING') ")
    boolean existsDuplicateTask(@Param("stockCode") String stockCode,
                               @Param("analysisType") String analysisType,
                               @Param("userId") String userId);
    
    /**
     * 统计用户今日任务数量
     */
    @Query("SELECT COUNT(t) FROM AnalysisTask t WHERE t.userId = :userId AND t.createdAt >= :todayStart")
    long countUserTodayTasks(@Param("userId") String userId, @Param("todayStart") LocalDateTime todayStart);
    
    /**
     * 统计系统今日任务数量
     */
    @Query("SELECT COUNT(t) FROM AnalysisTask t WHERE t.createdAt >= :todayStart")
    long countTodayTasks(@Param("todayStart") LocalDateTime todayStart);
    
    /**
     * 查找热门分析股票
     */
    @Query("SELECT t.stockCode, t.stockName, COUNT(t) as analysisCount " +
           "FROM AnalysisTask t WHERE t.createdAt >= :fromTime " +
           "GROUP BY t.stockCode, t.stockName ORDER BY COUNT(t) DESC")
    List<Object[]> findPopularStocks(@Param("fromTime") LocalDateTime fromTime, Pageable pageable);
    
    /**
     * 删除过期任务
     */
    @Query("DELETE FROM AnalysisTask t WHERE t.status IN ('COMPLETED', 'FAILED', 'CANCELLED') AND t.endTime < :cutoffTime")
    int deleteExpiredTasks(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * 批量更新任务状态
     */
    @Query("UPDATE AnalysisTask t SET t.status = :newStatus WHERE t.status = :oldStatus AND t.createdAt < :cutoffTime")
    int batchUpdateTaskStatus(@Param("oldStatus") String oldStatus,
                             @Param("newStatus") String newStatus,
                             @Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * 检查请求ID是否存在
     */
    boolean existsByRequestId(String requestId);
    
    /**
     * 统计总任务数
     */
    @Query("SELECT COUNT(t) FROM AnalysisTask t")
    long countAllTasks();
    
    /**
     * 统计已完成任务数
     */
    @Query("SELECT COUNT(t) FROM AnalysisTask t WHERE t.status = 'COMPLETED'")
    long countCompletedTasks();
    
    /**
     * 统计失败任务数
     */
    @Query("SELECT COUNT(t) FROM AnalysisTask t WHERE t.status = 'FAILED'")
    long countFailedTasks();
    
    /**
     * 统计运行中任务数
     */
    @Query("SELECT COUNT(t) FROM AnalysisTask t WHERE t.status = 'RUNNING'")
    long countRunningTasks();
    
    /**
     * 统计待处理任务数
     */
    @Query("SELECT COUNT(t) FROM AnalysisTask t WHERE t.status = 'PENDING'")
    long countPendingTasks();
}