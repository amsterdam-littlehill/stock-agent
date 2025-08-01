package com.jd.genie.agent.workflow.repository;

import com.jd.genie.agent.workflow.entity.WorkflowDefinitionEntity;
import com.jd.genie.agent.workflow.model.WorkflowDefinition;
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
 * 工作流定义数据访问层
 * 提供工作流定义的数据库操作接口
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinitionEntity, Long> {
    
    // ==================== 基本查询 ====================
    
    /**
     * 根据工作流ID查找（未删除）
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE w.workflowId = :workflowId AND w.deleted = false")
    Optional<WorkflowDefinitionEntity> findByWorkflowId(@Param("workflowId") String workflowId);
    
    /**
     * 根据工作流ID查找（包括已删除）
     */
    Optional<WorkflowDefinitionEntity> findByWorkflowIdAndDeletedFalse(String workflowId);
    
    /**
     * 检查工作流ID是否存在
     */
    boolean existsByWorkflowIdAndDeletedFalse(String workflowId);
    
    /**
     * 获取所有未删除的工作流
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE w.deleted = false ORDER BY w.updatedAt DESC")
    List<WorkflowDefinitionEntity> findAllActive();
    
    /**
     * 分页获取所有未删除的工作流
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE w.deleted = false")
    Page<WorkflowDefinitionEntity> findAllActive(Pageable pageable);
    
    // ==================== 条件查询 ====================
    
    /**
     * 根据创建者查找
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE w.createdBy = :createdBy AND w.deleted = false ORDER BY w.updatedAt DESC")
    List<WorkflowDefinitionEntity> findByCreatedBy(@Param("createdBy") String createdBy);
    
    /**
     * 根据类型查找
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE w.type = :type AND w.deleted = false ORDER BY w.updatedAt DESC")
    List<WorkflowDefinitionEntity> findByType(@Param("type") WorkflowDefinition.WorkflowType type);
    
    /**
     * 根据状态查找
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE w.status = :status AND w.deleted = false ORDER BY w.updatedAt DESC")
    List<WorkflowDefinitionEntity> findByStatus(@Param("status") WorkflowDefinition.WorkflowStatus status);
    
    /**
     * 根据分类查找
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE w.category = :category AND w.deleted = false ORDER BY w.updatedAt DESC")
    List<WorkflowDefinitionEntity> findByCategory(@Param("category") String category);
    
    /**
     * 查找公开的工作流
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE w.isPublic = true AND w.status = 'ACTIVE' AND w.deleted = false ORDER BY w.usageCount DESC, w.updatedAt DESC")
    List<WorkflowDefinitionEntity> findPublicWorkflows();
    
    /**
     * 查找用户可访问的工作流（公开的或自己创建的）
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE (w.isPublic = true OR w.createdBy = :userId) AND w.deleted = false ORDER BY w.updatedAt DESC")
    List<WorkflowDefinitionEntity> findAccessibleWorkflows(@Param("userId") String userId);
    
    // ==================== 复合条件查询 ====================
    
    /**
     * 多条件搜索工作流
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:type IS NULL OR w.type = :type) AND " +
           "(:status IS NULL OR w.status = :status) AND " +
           "(:createdBy IS NULL OR :createdBy = '' OR w.createdBy = :createdBy) AND " +
           "(:category IS NULL OR :category = '' OR w.category = :category) AND " +
           "w.deleted = false " +
           "ORDER BY w.updatedAt DESC")
    List<WorkflowDefinitionEntity> searchWorkflows(
        @Param("keyword") String keyword,
        @Param("type") WorkflowDefinition.WorkflowType type,
        @Param("status") WorkflowDefinition.WorkflowStatus status,
        @Param("createdBy") String createdBy,
        @Param("category") String category
    );
    
    /**
     * 分页搜索工作流
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:type IS NULL OR w.type = :type) AND " +
           "(:status IS NULL OR w.status = :status) AND " +
           "(:createdBy IS NULL OR :createdBy = '' OR w.createdBy = :createdBy) AND " +
           "(:category IS NULL OR :category = '' OR w.category = :category) AND " +
           "w.deleted = false")
    Page<WorkflowDefinitionEntity> searchWorkflows(
        @Param("keyword") String keyword,
        @Param("type") WorkflowDefinition.WorkflowType type,
        @Param("status") WorkflowDefinition.WorkflowStatus status,
        @Param("createdBy") String createdBy,
        @Param("category") String category,
        Pageable pageable
    );
    
    // ==================== 统计查询 ====================
    
    /**
     * 统计总工作流数
     */
    @Query("SELECT COUNT(w) FROM WorkflowDefinitionEntity w WHERE w.deleted = false")
    long countActiveWorkflows();
    
    /**
     * 统计各状态的工作流数量
     */
    @Query("SELECT w.status, COUNT(w) FROM WorkflowDefinitionEntity w WHERE w.deleted = false GROUP BY w.status")
    List<Object[]> countByStatus();
    
    /**
     * 统计各类型的工作流数量
     */
    @Query("SELECT w.type, COUNT(w) FROM WorkflowDefinitionEntity w WHERE w.deleted = false GROUP BY w.type")
    List<Object[]> countByType();
    
    /**
     * 统计各分类的工作流数量
     */
    @Query("SELECT w.category, COUNT(w) FROM WorkflowDefinitionEntity w WHERE w.deleted = false AND w.category IS NOT NULL GROUP BY w.category")
    List<Object[]> countByCategory();
    
    /**
     * 统计用户创建的工作流数量
     */
    @Query("SELECT COUNT(w) FROM WorkflowDefinitionEntity w WHERE w.createdBy = :createdBy AND w.deleted = false")
    long countByCreatedBy(@Param("createdBy") String createdBy);
    
    // ==================== 排序查询 ====================
    
    /**
     * 按使用次数排序（热门工作流）
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE w.deleted = false AND w.status = 'ACTIVE' ORDER BY w.usageCount DESC, w.updatedAt DESC")
    List<WorkflowDefinitionEntity> findPopularWorkflows(Pageable pageable);
    
    /**
     * 按成功率排序（高质量工作流）
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE w.deleted = false AND w.status = 'ACTIVE' AND w.usageCount > 0 ORDER BY w.successRate DESC, w.usageCount DESC")
    List<WorkflowDefinitionEntity> findHighQualityWorkflows(Pageable pageable);
    
    /**
     * 按创建时间排序（最新工作流）
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE w.deleted = false ORDER BY w.createdAt DESC")
    List<WorkflowDefinitionEntity> findLatestWorkflows(Pageable pageable);
    
    /**
     * 按更新时间排序（最近更新）
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE w.deleted = false ORDER BY w.updatedAt DESC")
    List<WorkflowDefinitionEntity> findRecentlyUpdatedWorkflows(Pageable pageable);
    
    // ==================== 时间范围查询 ====================
    
    /**
     * 查找指定时间范围内创建的工作流
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE w.createdAt BETWEEN :startTime AND :endTime AND w.deleted = false ORDER BY w.createdAt DESC")
    List<WorkflowDefinitionEntity> findByCreatedAtBetween(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * 查找指定时间范围内更新的工作流
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE w.updatedAt BETWEEN :startTime AND :endTime AND w.deleted = false ORDER BY w.updatedAt DESC")
    List<WorkflowDefinitionEntity> findByUpdatedAtBetween(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * 查找指定时间范围内执行过的工作流
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE w.lastExecutedAt BETWEEN :startTime AND :endTime AND w.deleted = false ORDER BY w.lastExecutedAt DESC")
    List<WorkflowDefinitionEntity> findByLastExecutedAtBetween(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    // ==================== 更新操作 ====================
    
    /**
     * 更新工作流统计信息
     */
    @Modifying
    @Query("UPDATE WorkflowDefinitionEntity w SET " +
           "w.usageCount = w.usageCount + :executionCount, " +
           "w.avgExecutionTime = :avgExecutionTime, " +
           "w.successRate = :successRate, " +
           "w.lastExecutedAt = :lastExecutedAt, " +
           "w.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE w.workflowId = :workflowId")
    int updateStatistics(
        @Param("workflowId") String workflowId,
        @Param("executionCount") int executionCount,
        @Param("avgExecutionTime") long avgExecutionTime,
        @Param("successRate") double successRate,
        @Param("lastExecutedAt") LocalDateTime lastExecutedAt
    );
    
    /**
     * 更新工作流状态
     */
    @Modifying
    @Query("UPDATE WorkflowDefinitionEntity w SET w.status = :status, w.updatedAt = CURRENT_TIMESTAMP WHERE w.workflowId = :workflowId")
    int updateStatus(@Param("workflowId") String workflowId, @Param("status") WorkflowDefinition.WorkflowStatus status);
    
    /**
     * 软删除工作流
     */
    @Modifying
    @Query("UPDATE WorkflowDefinitionEntity w SET w.deleted = true, w.deletedAt = CURRENT_TIMESTAMP, w.updatedAt = CURRENT_TIMESTAMP WHERE w.workflowId = :workflowId")
    int softDelete(@Param("workflowId") String workflowId);
    
    /**
     * 恢复已删除的工作流
     */
    @Modifying
    @Query("UPDATE WorkflowDefinitionEntity w SET w.deleted = false, w.deletedAt = NULL, w.updatedAt = CURRENT_TIMESTAMP WHERE w.workflowId = :workflowId")
    int restore(@Param("workflowId") String workflowId);
    
    /**
     * 批量更新状态
     */
    @Modifying
    @Query("UPDATE WorkflowDefinitionEntity w SET w.status = :status, w.updatedAt = CURRENT_TIMESTAMP WHERE w.workflowId IN :workflowIds")
    int batchUpdateStatus(@Param("workflowIds") List<String> workflowIds, @Param("status") WorkflowDefinition.WorkflowStatus status);
    
    /**
     * 批量软删除
     */
    @Modifying
    @Query("UPDATE WorkflowDefinitionEntity w SET w.deleted = true, w.deletedAt = CURRENT_TIMESTAMP, w.updatedAt = CURRENT_TIMESTAMP WHERE w.workflowId IN :workflowIds")
    int batchSoftDelete(@Param("workflowIds") List<String> workflowIds);
    
    // ==================== 删除操作 ====================
    
    /**
     * 物理删除工作流（谨慎使用）
     */
    void deleteByWorkflowId(String workflowId);
    
    /**
     * 清理指定时间之前的已删除工作流
     */
    @Modifying
    @Query("DELETE FROM WorkflowDefinitionEntity w WHERE w.deleted = true AND w.deletedAt < :beforeTime")
    int cleanupDeletedWorkflows(@Param("beforeTime") LocalDateTime beforeTime);
    
    // ==================== 自定义查询 ====================
    
    /**
     * 查找相似的工作流（基于名称和描述）
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE " +
           "w.workflowId != :excludeWorkflowId AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "w.deleted = false " +
           "ORDER BY w.usageCount DESC")
    List<WorkflowDefinitionEntity> findSimilarWorkflows(
        @Param("excludeWorkflowId") String excludeWorkflowId,
        @Param("keyword") String keyword,
        Pageable pageable
    );
    
    /**
     * 查找推荐的工作流（基于用户历史）
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE " +
           "w.type IN :preferredTypes AND " +
           "w.status = 'ACTIVE' AND " +
           "w.deleted = false " +
           "ORDER BY w.successRate DESC, w.usageCount DESC")
    List<WorkflowDefinitionEntity> findRecommendedWorkflows(
        @Param("preferredTypes") List<WorkflowDefinition.WorkflowType> preferredTypes,
        Pageable pageable
    );
    
    /**
     * 查找需要维护的工作流（长时间未更新或成功率低）
     */
    @Query("SELECT w FROM WorkflowDefinitionEntity w WHERE " +
           "w.status = 'ACTIVE' AND " +
           "w.deleted = false AND " +
           "(w.updatedAt < :staleThreshold OR " +
           " (w.usageCount > 10 AND w.successRate < :lowSuccessRate)) " +
           "ORDER BY w.successRate ASC, w.updatedAt ASC")
    List<WorkflowDefinitionEntity> findWorkflowsNeedingMaintenance(
        @Param("staleThreshold") LocalDateTime staleThreshold,
        @Param("lowSuccessRate") double lowSuccessRate
    );
    
    /**
     * 获取所有分类列表
     */
    @Query("SELECT DISTINCT w.category FROM WorkflowDefinitionEntity w WHERE w.category IS NOT NULL AND w.deleted = false ORDER BY w.category")
    List<String> findAllCategories();
    
    /**
     * 获取用户的工作流类型偏好
     */
    @Query("SELECT w.type, COUNT(w) as count FROM WorkflowDefinitionEntity w WHERE w.createdBy = :userId AND w.deleted = false GROUP BY w.type ORDER BY count DESC")
    List<Object[]> findUserTypePreferences(@Param("userId") String userId);
}