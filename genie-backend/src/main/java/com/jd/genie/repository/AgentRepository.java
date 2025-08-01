package com.jd.genie.repository;

import com.jd.genie.entity.Agent;
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
 * 智能体Repository
 */
@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    
    /**
     * 根据名称查找智能体
     */
    Optional<Agent> findByName(String name);
    
    /**
     * 根据智能体类型查找
     */
    List<Agent> findByAgentType(String agentType);
    
    /**
     * 根据状态查找智能体
     */
    List<Agent> findByStatus(String status);
    
    /**
     * 查找启用的智能体
     */
    List<Agent> findByEnabledTrue();
    
    /**
     * 查找禁用的智能体
     */
    List<Agent> findByEnabledFalse();
    
    /**
     * 根据智能体类型和状态查找
     */
    List<Agent> findByAgentTypeAndStatus(String agentType, String status);
    
    /**
     * 根据智能体类型查找启用的智能体
     */
    List<Agent> findByAgentTypeAndEnabledTrue(String agentType);
    
    /**
     * 查找可用的智能体（启用且活跃且未满负载）
     */
    @Query("SELECT a FROM Agent a WHERE a.enabled = true AND a.status = 'ACTIVE' AND a.currentConcurrent < a.maxConcurrent")
    List<Agent> findAvailableAgents();
    
    /**
     * 根据类型查找可用的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.agentType = :agentType AND a.enabled = true AND a.status = 'ACTIVE' AND a.currentConcurrent < a.maxConcurrent")
    List<Agent> findAvailableAgentsByType(@Param("agentType") String agentType);
    
    /**
     * 查找繁忙的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.currentConcurrent >= a.maxConcurrent")
    List<Agent> findBusyAgents();
    
    /**
     * 查找不健康的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.status != 'ACTIVE' OR a.lastActiveTime < :healthCheckTime")
    List<Agent> findUnhealthyAgents(@Param("healthCheckTime") LocalDateTime healthCheckTime);
    
    /**
     * 根据优先级排序查找智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.agentType = :agentType AND a.enabled = true ORDER BY a.priority ASC, a.currentConcurrent ASC")
    List<Agent> findByAgentTypeOrderByPriority(@Param("agentType") String agentType);
    
    /**
     * 根据性能评分排序查找智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.agentType = :agentType AND a.enabled = true AND a.status = 'ACTIVE' ORDER BY a.successRate DESC, a.avgResponseTime ASC")
    List<Agent> findByAgentTypeOrderByPerformance(@Param("agentType") String agentType);
    
    /**
     * 查找高成功率的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.successRate >= :minSuccessRate ORDER BY a.successRate DESC")
    List<Agent> findHighPerformanceAgents(@Param("minSuccessRate") BigDecimal minSuccessRate);
    
    /**
     * 查找低成功率的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.successRate < :maxSuccessRate ORDER BY a.successRate ASC")
    List<Agent> findLowPerformanceAgents(@Param("maxSuccessRate") BigDecimal maxSuccessRate);
    
    /**
     * 查找响应时间快的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.avgResponseTime <= :maxResponseTime AND a.avgResponseTime > 0 ORDER BY a.avgResponseTime ASC")
    List<Agent> findFastResponseAgents(@Param("maxResponseTime") Long maxResponseTime);
    
    /**
     * 查找响应时间慢的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.avgResponseTime > :minResponseTime ORDER BY a.avgResponseTime DESC")
    List<Agent> findSlowResponseAgents(@Param("minResponseTime") Long minResponseTime);
    
    /**
     * 统计各智能体类型的数量
     */
    @Query("SELECT a.agentType, COUNT(a) FROM Agent a GROUP BY a.agentType")
    List<Object[]> countByAgentType();
    
    /**
     * 统计各状态的智能体数量
     */
    @Query("SELECT a.status, COUNT(a) FROM Agent a GROUP BY a.status")
    List<Object[]> countByStatus();
    
    /**
     * 获取智能体类型的平均性能指标
     */
    @Query("SELECT a.agentType, AVG(a.successRate), AVG(a.avgResponseTime), AVG(a.avgConfidence) " +
           "FROM Agent a WHERE a.totalTasks > 0 GROUP BY a.agentType")
    List<Object[]> getPerformanceStatsByType();
    
    /**
     * 获取系统整体性能统计
     */
    @Query("SELECT COUNT(a), " +
           "COUNT(CASE WHEN a.enabled = true THEN 1 END), " +
           "COUNT(CASE WHEN a.status = 'ACTIVE' THEN 1 END), " +
           "AVG(a.successRate), " +
           "AVG(a.avgResponseTime), " +
           "SUM(a.totalTasks), " +
           "SUM(a.successfulTasks) " +
           "FROM Agent a")
    Object[] getSystemPerformanceStats();
    
    /**
     * 查找最近活跃的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.lastActiveTime >= :fromTime ORDER BY a.lastActiveTime DESC")
    List<Agent> findRecentlyActiveAgents(@Param("fromTime") LocalDateTime fromTime);
    
    /**
     * 查找长时间未活跃的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.lastActiveTime < :cutoffTime OR a.lastActiveTime IS NULL")
    List<Agent> findInactiveAgents(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * 查找任务量最多的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.totalTasks > 0 ORDER BY a.totalTasks DESC")
    List<Agent> findTopTaskAgents(Pageable pageable);
    
    /**
     * 查找成功率最高的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.totalTasks > 0 ORDER BY a.successRate DESC")
    List<Agent> findTopSuccessRateAgents(Pageable pageable);
    
    /**
     * 查找平均置信度最高的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.totalTasks > 0 ORDER BY a.avgConfidence DESC")
    List<Agent> findTopConfidenceAgents(Pageable pageable);
    
    /**
     * 查找负载率最高的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.maxConcurrent > 0 ORDER BY (a.currentConcurrent * 100.0 / a.maxConcurrent) DESC")
    List<Agent> findTopLoadAgents(Pageable pageable);
    
    /**
     * 根据权重查找智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.agentType = :agentType AND a.enabled = true ORDER BY a.weight DESC")
    List<Agent> findByAgentTypeOrderByWeight(@Param("agentType") String agentType);
    
    /**
     * 查找需要维护的智能体
     */
    @Query("SELECT a FROM Agent a WHERE a.successRate < :minSuccessRate OR a.avgResponseTime > :maxResponseTime")
    List<Agent> findAgentsNeedingMaintenance(@Param("minSuccessRate") BigDecimal minSuccessRate,
                                            @Param("maxResponseTime") Long maxResponseTime);
    
    /**
     * 查找超时的智能体任务
     */
    @Query("SELECT a FROM Agent a WHERE a.status = 'ACTIVE' AND a.currentConcurrent > 0 AND a.lastTaskTime < :timeoutTime")
    List<Agent> findTimeoutAgents(@Param("timeoutTime") LocalDateTime timeoutTime);
    
    /**
     * 更新智能体活跃时间
     */
    @Query("UPDATE Agent a SET a.lastActiveTime = :activeTime WHERE a.id = :agentId")
    int updateLastActiveTime(@Param("agentId") Long agentId, @Param("activeTime") LocalDateTime activeTime);
    
    /**
     * 更新智能体并发数
     */
    @Query("UPDATE Agent a SET a.currentConcurrent = :concurrent WHERE a.id = :agentId")
    int updateCurrentConcurrent(@Param("agentId") Long agentId, @Param("concurrent") Integer concurrent);
    
    /**
     * 批量更新智能体状态
     */
    @Query("UPDATE Agent a SET a.status = :status WHERE a.id IN :agentIds")
    int batchUpdateStatus(@Param("agentIds") List<Long> agentIds, @Param("status") String status);
    
    /**
     * 重置智能体统计数据
     */
    @Query("UPDATE Agent a SET a.totalTasks = 0, a.successfulTasks = 0, a.failedTasks = 0, " +
           "a.successRate = 100.0, a.avgResponseTime = 0, a.avgConfidence = 0.0, a.avgAccuracy = 0.0 " +
           "WHERE a.id = :agentId")
    int resetAgentStats(@Param("agentId") Long agentId);
    
    /**
     * 批量重置智能体统计数据
     */
    @Query("UPDATE Agent a SET a.totalTasks = 0, a.successfulTasks = 0, a.failedTasks = 0, " +
           "a.successRate = 100.0, a.avgResponseTime = 0, a.avgConfidence = 0.0, a.avgAccuracy = 0.0")
    int batchResetStats();
    
    /**
     * 检查智能体名称是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 检查智能体类型是否有可用实例
     */
    @Query("SELECT COUNT(a) > 0 FROM Agent a WHERE a.agentType = :agentType AND a.enabled = true AND a.status = 'ACTIVE'")
    boolean hasAvailableAgentsByType(@Param("agentType") String agentType);
    
    /**
     * 统计启用的智能体数量
     */
    @Query("SELECT COUNT(a) FROM Agent a WHERE a.enabled = true")
    long countEnabledAgents();
    
    /**
     * 统计活跃的智能体数量
     */
    @Query("SELECT COUNT(a) FROM Agent a WHERE a.status = 'ACTIVE'")
    long countActiveAgents();
    
    /**
     * 统计可用的智能体数量
     */
    @Query("SELECT COUNT(a) FROM Agent a WHERE a.enabled = true AND a.status = 'ACTIVE' AND a.currentConcurrent < a.maxConcurrent")
    long countAvailableAgents();
    
    /**
     * 统计繁忙的智能体数量
     */
    @Query("SELECT COUNT(a) FROM Agent a WHERE a.currentConcurrent >= a.maxConcurrent")
    long countBusyAgents();
    
    /**
     * 统计指定类型的智能体数量
     */
    long countByAgentType(String agentType);
    
    /**
     * 统计指定类型的可用智能体数量
     */
    @Query("SELECT COUNT(a) FROM Agent a WHERE a.agentType = :agentType AND a.enabled = true AND a.status = 'ACTIVE' AND a.currentConcurrent < a.maxConcurrent")
    long countAvailableAgentsByType(@Param("agentType") String agentType);
    
    /**
     * 分页查询智能体
     */
    Page<Agent> findByAgentTypeOrderByPriorityAsc(String agentType, Pageable pageable);
    
    /**
     * 分页查询启用的智能体
     */
    Page<Agent> findByEnabledTrueOrderByAgentTypeAsc(Pageable pageable);
    
    /**
     * 分页查询活跃的智能体
     */
    Page<Agent> findByStatusOrderByLastActiveTimeDesc(String status, Pageable pageable);
}