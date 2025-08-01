package com.jd.genie.agent.workflow.repository;

import com.jd.genie.agent.workflow.entity.WorkflowExecutionEntity;
import com.jd.genie.agent.workflow.model.WorkflowExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 工作流执行记录数据访问层
 * 提供工作流执行记录的数据库操作接口
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecutionEntity, Long> {
    
    // ==================== 基本查询 ====================
    
    /**
     * 根据执行ID查找
     */
    Optional<WorkflowExecutionEntity> findByExecutionId(String executionId);
    
    /**
     * 检查执行ID是否存在
     */
    boolean existsByExecutionId(String executionId);
    
    // ==================== 工作流相关查询 ====================
    
    /**
     * 根据工作流ID查找所有执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.workflowId = :workflowId ORDER BY e.startTime DESC")
    List<WorkflowExecutionEntity> findByWorkflowId(@Param("workflowId") String workflowId);
    
    /**
     * 分页查询工作流的执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.workflowId = :workflowId ORDER BY e.startTime DESC")
    Page<WorkflowExecutionEntity> findByWorkflowId(@Param("workflowId") String workflowId, Pageable pageable);
    
    /**
     * 查找工作流的最近N次执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.workflowId = :workflowId ORDER BY e.startTime DESC")
    List<WorkflowExecutionEntity> findRecentExecutions(@Param("workflowId") String workflowId, Pageable pageable);
    
    /**
     * 查找工作流的最后一次执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.workflowId = :workflowId ORDER BY e.startTime DESC")
    Optional<WorkflowExecutionEntity> findLastExecution(@Param("workflowId") String workflowId, Pageable pageable);
    
    // ==================== 状态查询 ====================
    
    /**
     * 根据状态查找执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.status = :status ORDER BY e.startTime DESC")
    List<WorkflowExecutionEntity> findByStatus(@Param("status") WorkflowExecution.ExecutionStatus status);
    
    /**
     * 查找正在运行的执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.status IN ('PENDING', 'RUNNING', 'PAUSED') ORDER BY e.startTime DESC")
    List<WorkflowExecutionEntity> findRunningExecutions();
    
    /**
     * 查找已完成的执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.status IN ('COMPLETED', 'FAILED', 'CANCELLED', 'TIMEOUT') ORDER BY e.startTime DESC")
    List<WorkflowExecutionEntity> findCompletedExecutions();
    
    /**
     * 查找成功的执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.status = 'COMPLETED' ORDER BY e.startTime DESC")
    List<WorkflowExecutionEntity> findSuccessfulExecutions();
    
    /**
     * 查找失败的执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.status IN ('FAILED', 'TIMEOUT') ORDER BY e.startTime DESC")
    List<WorkflowExecutionEntity> findFailedExecutions();
    
    // ==================== 用户相关查询 ====================
    
    /**
     * 根据执行者查找执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.executedBy = :executedBy ORDER BY e.startTime DESC")
    List<WorkflowExecutionEntity> findByExecutedBy(@Param("executedBy") String executedBy);
    
    /**
     * 分页查询用户的执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.executedBy = :executedBy ORDER BY e.startTime DESC")
    Page<WorkflowExecutionEntity> findByExecutedBy(@Param("executedBy") String executedBy, Pageable pageable);
    
    /**
     * 查找用户的最近N次执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.executedBy = :executedBy ORDER BY e.startTime DESC")
    List<WorkflowExecutionEntity> findUserRecentExecutions(@Param("executedBy") String executedBy, Pageable pageable);
    
    // ==================== 复合条件查询 ====================
    
    /**
     * 多条件搜索执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE " +
           "(:workflowId IS NULL OR :workflowId = '' OR e.workflowId = :workflowId) AND " +
           "(:executedBy IS NULL OR :executedBy = '' OR e.executedBy = :executedBy) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:triggerMode IS NULL OR e.triggerMode = :triggerMode) AND " +
           "(:startTime IS NULL OR e.startTime >= :startTime) AND " +
           "(:endTime IS NULL OR e.startTime <= :endTime) " +
           "ORDER BY e.startTime DESC")
    List<WorkflowExecutionEntity> searchExecutions(
        @Param("workflowId") String workflowId,
        @Param("executedBy") String executedBy,
        @Param("status") WorkflowExecution.ExecutionStatus status,
        @Param("triggerMode") WorkflowExecution.TriggerMode triggerMode,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * 分页搜索执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE " +
           "(:workflowId IS NULL OR :workflowId = '' OR e.workflowId = :workflowId) AND " +
           "(:executedBy IS NULL OR :executedBy = '' OR e.executedBy = :executedBy) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:triggerMode IS NULL OR e.triggerMode = :triggerMode) AND " +
           "(:startTime IS NULL OR e.startTime >= :startTime) AND " +
           "(:endTime IS NULL OR e.startTime <= :endTime)")
    Page<WorkflowExecutionEntity> searchExecutions(
        @Param("workflowId") String workflowId,
        @Param("executedBy") String executedBy,
        @Param("status") WorkflowExecution.ExecutionStatus status,
        @Param("triggerMode") WorkflowExecution.TriggerMode triggerMode,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        Pageable pageable
    );
    
    // ==================== 时间范围查询 ====================
    
    /**
     * 查找指定时间范围内开始的执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.startTime BETWEEN :startTime AND :endTime ORDER BY e.startTime DESC")
    List<WorkflowExecutionEntity> findByStartTimeBetween(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * 查找指定时间范围内结束的执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.endTime BETWEEN :startTime AND :endTime ORDER BY e.endTime DESC")
    List<WorkflowExecutionEntity> findByEndTimeBetween(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * 查找今天的执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE DATE(e.startTime) = CURRENT_DATE ORDER BY e.startTime DESC")
    List<WorkflowExecutionEntity> findTodayExecutions();
    
    /**
     * 查找本周的执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.startTime >= :weekStart ORDER BY e.startTime DESC")
    List<WorkflowExecutionEntity> findThisWeekExecutions(@Param("weekStart") LocalDateTime weekStart);
    
    /**
     * 查找本月的执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.startTime >= :monthStart ORDER BY e.startTime DESC")
    List<WorkflowExecutionEntity> findThisMonthExecutions(@Param("monthStart") LocalDateTime monthStart);
    
    // ==================== 统计查询 ====================
    
    /**
     * 统计总执行次数
     */
    @Query("SELECT COUNT(e) FROM WorkflowExecutionEntity e")
    long countTotalExecutions();
    
    /**
     * 统计工作流的执行次数
     */
    @Query("SELECT COUNT(e) FROM WorkflowExecutionEntity e WHERE e.workflowId = :workflowId")
    long countByWorkflowId(@Param("workflowId") String workflowId);
    
    /**
     * 统计用户的执行次数
     */
    @Query("SELECT COUNT(e) FROM WorkflowExecutionEntity e WHERE e.executedBy = :executedBy")
    long countByExecutedBy(@Param("executedBy") String executedBy);
    
    /**
     * 统计各状态的执行次数
     */
    @Query("SELECT e.status, COUNT(e) FROM WorkflowExecutionEntity e GROUP BY e.status")
    List<Object[]> countByStatus();
    
    /**
     * 统计工作流各状态的执行次数
     */
    @Query("SELECT e.status, COUNT(e) FROM WorkflowExecutionEntity e WHERE e.workflowId = :workflowId GROUP BY e.status")
    List<Object[]> countByWorkflowIdAndStatus(@Param("workflowId") String workflowId);
    
    /**
     * 统计用户各状态的执行次数
     */
    @Query("SELECT e.status, COUNT(e) FROM WorkflowExecutionEntity e WHERE e.executedBy = :executedBy GROUP BY e.status")
    List<Object[]> countByExecutedByAndStatus(@Param("executedBy") String executedBy);
    
    /**
     * 统计成功率
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN e.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(e) as successRate " +
           "FROM WorkflowExecutionEntity e WHERE e.workflowId = :workflowId")
    Double calculateSuccessRate(@Param("workflowId") String workflowId);
    
    /**
     * 统计平均执行时间
     */
    @Query("SELECT AVG(e.executionDuration) FROM WorkflowExecutionEntity e WHERE e.workflowId = :workflowId AND e.executionDuration IS NOT NULL")
    Double calculateAverageExecutionTime(@Param("workflowId") String workflowId);
    
    /**
     * 统计每日执行次数
     */
    @Query("SELECT DATE(e.startTime) as date, COUNT(e) as count " +
           "FROM WorkflowExecutionEntity e " +
           "WHERE e.startTime BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(e.startTime) " +
           "ORDER BY date")
    List<Object[]> countDailyExecutions(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 统计每小时执行次数
     */
    @Query("SELECT HOUR(e.startTime) as hour, COUNT(e) as count " +
           "FROM WorkflowExecutionEntity e " +
           "WHERE DATE(e.startTime) = CURRENT_DATE " +
           "GROUP BY HOUR(e.startTime) " +
           "ORDER BY hour")
    List<Object[]> countHourlyExecutions();
    
    // ==================== 性能查询 ====================
    
    /**
     * 查找执行时间最长的记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.executionDuration IS NOT NULL ORDER BY e.executionDuration DESC")
    List<WorkflowExecutionEntity> findLongestExecutions(Pageable pageable);
    
    /**
     * 查找执行时间最短的记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.executionDuration IS NOT NULL AND e.executionDuration > 0 ORDER BY e.executionDuration ASC")
    List<WorkflowExecutionEntity> findShortestExecutions(Pageable pageable);
    
    /**
     * 查找超时的执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.status = 'TIMEOUT' ORDER BY e.startTime DESC")
    List<WorkflowExecutionEntity> findTimeoutExecutions();
    
    /**
     * 查找长时间运行的执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE " +
           "e.status IN ('RUNNING', 'PAUSED') AND " +
           "e.startTime < :threshold " +
           "ORDER BY e.startTime ASC")
    List<WorkflowExecutionEntity> findLongRunningExecutions(@Param("threshold") LocalDateTime threshold);
    
    // ==================== 层次结构查询 ====================
    
    /**
     * 查找子执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.parentExecutionId = :parentExecutionId ORDER BY e.startTime ASC")
    List<WorkflowExecutionEntity> findChildExecutions(@Param("parentExecutionId") String parentExecutionId);
    
    /**
     * 查找根执行记录下的所有执行
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.rootExecutionId = :rootExecutionId ORDER BY e.startTime ASC")
    List<WorkflowExecutionEntity> findExecutionsByRoot(@Param("rootExecutionId") String rootExecutionId);
    
    /**
     * 查找顶级执行记录（没有父执行）
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.parentExecutionId IS NULL ORDER BY e.startTime DESC")
    List<WorkflowExecutionEntity> findTopLevelExecutions();
    
    // ==================== 更新操作 ====================
    
    /**
     * 更新执行状态
     */
    @Modifying
    @Query("UPDATE WorkflowExecutionEntity e SET e.status = :status, e.updatedAt = CURRENT_TIMESTAMP WHERE e.executionId = :executionId")
    int updateStatus(@Param("executionId") String executionId, @Param("status") WorkflowExecution.ExecutionStatus status);
    
    /**
     * 更新执行进度
     */
    @Modifying
    @Query("UPDATE WorkflowExecutionEntity e SET e.progress = :progress, e.currentNodeId = :currentNodeId, e.updatedAt = CURRENT_TIMESTAMP WHERE e.executionId = :executionId")
    int updateProgress(@Param("executionId") String executionId, @Param("progress") int progress, @Param("currentNodeId") String currentNodeId);
    
    /**
     * 更新结束时间和执行时长
     */
    @Modifying
    @Query("UPDATE WorkflowExecutionEntity e SET " +
           "e.endTime = :endTime, " +
           "e.executionDuration = :executionDuration, " +
           "e.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE e.executionId = :executionId")
    int updateEndTime(
        @Param("executionId") String executionId,
        @Param("endTime") LocalDateTime endTime,
        @Param("executionDuration") long executionDuration
    );
    
    /**
     * 更新错误信息
     */
    @Modifying
    @Query("UPDATE WorkflowExecutionEntity e SET " +
           "e.errorMessage = :errorMessage, " +
           "e.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE e.executionId = :executionId")
    int updateErrorMessage(@Param("executionId") String executionId, @Param("errorMessage") String errorMessage);
    
    /**
     * 增加重试次数
     */
    @Modifying
    @Query("UPDATE WorkflowExecutionEntity e SET e.retryCount = e.retryCount + 1, e.updatedAt = CURRENT_TIMESTAMP WHERE e.executionId = :executionId")
    int incrementRetryCount(@Param("executionId") String executionId);
    
    // ==================== 删除操作 ====================
    
    /**
     * 删除指定工作流的所有执行记录
     */
    @Modifying
    @Query("DELETE FROM WorkflowExecutionEntity e WHERE e.workflowId = :workflowId")
    int deleteByWorkflowId(@Param("workflowId") String workflowId);
    
    /**
     * 删除指定时间之前的执行记录
     */
    @Modifying
    @Query("DELETE FROM WorkflowExecutionEntity e WHERE e.startTime < :beforeTime")
    int deleteOldExecutions(@Param("beforeTime") LocalDateTime beforeTime);
    
    /**
     * 删除已完成的执行记录（保留指定天数）
     */
    @Modifying
    @Query("DELETE FROM WorkflowExecutionEntity e WHERE " +
           "e.status IN ('COMPLETED', 'FAILED', 'CANCELLED', 'TIMEOUT') AND " +
           "e.endTime < :beforeTime")
    int deleteCompletedExecutions(@Param("beforeTime") LocalDateTime beforeTime);
    
    // ==================== 自定义查询 ====================
    
    /**
     * 查找需要清理的执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE " +
           "e.status IN ('COMPLETED', 'FAILED', 'CANCELLED', 'TIMEOUT') AND " +
           "e.endTime < :cleanupThreshold " +
           "ORDER BY e.endTime ASC")
    List<WorkflowExecutionEntity> findExecutionsForCleanup(@Param("cleanupThreshold") LocalDateTime cleanupThreshold);
    
    /**
     * 查找异常的执行记录（运行时间过长但状态仍为运行中）
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE " +
           "e.status = 'RUNNING' AND " +
           "e.startTime < :staleThreshold " +
           "ORDER BY e.startTime ASC")
    List<WorkflowExecutionEntity> findStaleRunningExecutions(@Param("staleThreshold") LocalDateTime staleThreshold);
    
    /**
     * 查找重试次数过多的执行记录
     */
    @Query("SELECT e FROM WorkflowExecutionEntity e WHERE e.retryCount >= :maxRetries ORDER BY e.retryCount DESC, e.startTime DESC")
    List<WorkflowExecutionEntity> findHighRetryExecutions(@Param("maxRetries") int maxRetries);
    
    /**
     * 查找用户最常执行的工作流
     */
    @Query("SELECT e.workflowId, COUNT(e) as count " +
           "FROM WorkflowExecutionEntity e " +
           "WHERE e.executedBy = :executedBy " +
           "GROUP BY e.workflowId " +
           "ORDER BY count DESC")
    List<Object[]> findUserMostExecutedWorkflows(@Param("executedBy") String executedBy, Pageable pageable);
    
    /**
     * 查找最活跃的用户
     */
    @Query("SELECT e.executedBy, COUNT(e) as count " +
           "FROM WorkflowExecutionEntity e " +
           "WHERE e.startTime >= :since " +
           "GROUP BY e.executedBy " +
           "ORDER BY count DESC")
    List<Object[]> findMostActiveUsers(@Param("since") LocalDateTime since, Pageable pageable);
}