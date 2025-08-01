package com.jd.genie.agent.workflow.entity;

import com.jd.genie.agent.workflow.model.WorkflowExecution;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 工作流执行实体类
 * 用于数据库持久化工作流执行记录
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "workflow_executions", indexes = {
    @Index(name = "idx_execution_id", columnList = "executionId", unique = true),
    @Index(name = "idx_workflow_id", columnList = "workflowId"),
    @Index(name = "idx_executed_by", columnList = "executedBy"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_trigger_mode", columnList = "triggerMode"),
    @Index(name = "idx_start_time", columnList = "startTime"),
    @Index(name = "idx_end_time", columnList = "endTime"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
public class WorkflowExecutionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 执行ID（业务主键）
     */
    @Column(name = "execution_id", nullable = false, unique = true, length = 100)
    private String executionId;
    
    /**
     * 工作流ID
     */
    @Column(name = "workflow_id", nullable = false, length = 100)
    private String workflowId;
    
    /**
     * 工作流版本
     */
    @Column(name = "workflow_version", nullable = false, length = 50)
    private String workflowVersion;
    
    /**
     * 执行者
     */
    @Column(name = "executed_by", nullable = false, length = 100)
    private String executedBy;
    
    /**
     * 触发方式
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_mode", nullable = false, length = 50)
    private WorkflowExecution.TriggerMode triggerMode;
    
    /**
     * 执行状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private WorkflowExecution.ExecutionStatus status;
    
    /**
     * 开始时间
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    /**
     * 执行时长（毫秒）
     */
    @Column(name = "execution_duration")
    private Long executionDuration;
    
    /**
     * 输入参数（JSON格式存储）
     */
    @Column(name = "input_parameters", columnDefinition = "LONGTEXT")
    private String inputParameters;
    
    /**
     * 输出结果（JSON格式存储）
     */
    @Column(name = "output_result", columnDefinition = "LONGTEXT")
    private String outputResult;
    
    /**
     * 执行上下文（JSON格式存储）
     */
    @Column(name = "execution_context", columnDefinition = "LONGTEXT")
    private String executionContext;
    
    /**
     * 节点执行记录（JSON格式存储）
     */
    @Column(name = "node_executions", columnDefinition = "LONGTEXT")
    private String nodeExecutions;
    
    /**
     * 执行进度（0-100）
     */
    @Column(name = "progress", nullable = false)
    private Integer progress = 0;
    
    /**
     * 当前执行节点
     */
    @Column(name = "current_node_id", length = 100)
    private String currentNodeId;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 错误堆栈（JSON格式存储）
     */
    @Column(name = "error_stack", columnDefinition = "LONGTEXT")
    private String errorStack;
    
    /**
     * 警告信息（JSON格式存储）
     */
    @Column(name = "warnings", columnDefinition = "TEXT")
    private String warnings;
    
    /**
     * 执行日志（JSON格式存储）
     */
    @Column(name = "execution_logs", columnDefinition = "LONGTEXT")
    private String executionLogs;
    
    /**
     * 性能统计（JSON格式存储）
     */
    @Column(name = "performance_stats", columnDefinition = "TEXT")
    private String performanceStats;
    
    /**
     * 资源使用情况（JSON格式存储）
     */
    @Column(name = "resource_usage", columnDefinition = "TEXT")
    private String resourceUsage;
    
    /**
     * 重试次数
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    /**
     * 最大重试次数
     */
    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 0;
    
    /**
     * 父执行ID（用于子工作流）
     */
    @Column(name = "parent_execution_id", length = 100)
    private String parentExecutionId;
    
    /**
     * 根执行ID（用于嵌套工作流）
     */
    @Column(name = "root_execution_id", length = 100)
    private String rootExecutionId;
    
    /**
     * 执行优先级
     */
    @Column(name = "priority", nullable = false)
    private Integer priority = 5;
    
    /**
     * 超时时间（秒）
     */
    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;
    
    /**
     * 是否可取消
     */
    @Column(name = "cancellable", nullable = false)
    private Boolean cancellable = true;
    
    /**
     * 是否可暂停
     */
    @Column(name = "pausable", nullable = false)
    private Boolean pausable = true;
    
    /**
     * 暂停时间
     */
    @Column(name = "paused_at")
    private LocalDateTime pausedAt;
    
    /**
     * 恢复时间
     */
    @Column(name = "resumed_at")
    private LocalDateTime resumedAt;
    
    /**
     * 取消时间
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    /**
     * 取消原因
     */
    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;
    
    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 版本号（乐观锁）
     */
    @Version
    @Column(name = "version_lock")
    private Long versionLock;
    
    /**
     * 扩展字段（JSON格式存储）
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    // ==================== 辅助方法 ====================
    
    /**
     * 转换为领域模型
     */
    public WorkflowExecution toDomainModel() {
        // 这里需要实现JSON反序列化逻辑
        // 简化实现，实际项目中应该使用ObjectMapper
        WorkflowExecution execution = new WorkflowExecution();
        
        execution.setExecutionId(this.executionId);
        execution.setWorkflowId(this.workflowId);
        execution.setWorkflowVersion(this.workflowVersion);
        execution.setExecutedBy(this.executedBy);
        execution.setTriggerMode(this.triggerMode);
        execution.setStatus(this.status);
        execution.setStartTime(this.startTime);
        execution.setEndTime(this.endTime);
        execution.setExecutionDuration(this.executionDuration != null ? this.executionDuration : 0L);
        execution.setProgress(this.progress);
        execution.setCurrentNodeId(this.currentNodeId);
        execution.setErrorMessage(this.errorMessage);
        execution.setRetryCount(this.retryCount);
        execution.setMaxRetries(this.maxRetries);
        execution.setParentExecutionId(this.parentExecutionId);
        execution.setRootExecutionId(this.rootExecutionId);
        execution.setPriority(this.priority);
        execution.setTimeoutSeconds(this.timeoutSeconds);
        execution.setCancellable(this.cancellable);
        execution.setPausable(this.pausable);
        execution.setPausedAt(this.pausedAt);
        execution.setResumedAt(this.resumedAt);
        execution.setCancelledAt(this.cancelledAt);
        execution.setCancelReason(this.cancelReason);
        
        return execution;
    }
    
    /**
     * 从领域模型创建实体
     */
    public static WorkflowExecutionEntity fromDomainModel(WorkflowExecution execution) {
        WorkflowExecutionEntity entity = new WorkflowExecutionEntity();
        
        entity.setExecutionId(execution.getExecutionId());
        entity.setWorkflowId(execution.getWorkflowId());
        entity.setWorkflowVersion(execution.getWorkflowVersion());
        entity.setExecutedBy(execution.getExecutedBy());
        entity.setTriggerMode(execution.getTriggerMode());
        entity.setStatus(execution.getStatus());
        entity.setStartTime(execution.getStartTime());
        entity.setEndTime(execution.getEndTime());
        entity.setExecutionDuration(execution.getExecutionDuration());
        entity.setProgress(execution.getProgress());
        entity.setCurrentNodeId(execution.getCurrentNodeId());
        entity.setErrorMessage(execution.getErrorMessage());
        entity.setRetryCount(execution.getRetryCount());
        entity.setMaxRetries(execution.getMaxRetries());
        entity.setParentExecutionId(execution.getParentExecutionId());
        entity.setRootExecutionId(execution.getRootExecutionId());
        entity.setPriority(execution.getPriority());
        entity.setTimeoutSeconds(execution.getTimeoutSeconds());
        entity.setCancellable(execution.isCancellable());
        entity.setPausable(execution.isPausable());
        entity.setPausedAt(execution.getPausedAt());
        entity.setResumedAt(execution.getResumedAt());
        entity.setCancelledAt(execution.getCancelledAt());
        entity.setCancelReason(execution.getCancelReason());
        
        // 这里需要实现JSON序列化逻辑
        // 简化实现，实际项目中应该使用ObjectMapper
        
        return entity;
    }
    
    /**
     * 开始执行
     */
    public void start() {
        this.status = WorkflowExecution.ExecutionStatus.RUNNING;
        this.startTime = LocalDateTime.now();
        this.progress = 0;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 完成执行
     */
    public void complete() {
        this.status = WorkflowExecution.ExecutionStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.progress = 100;
        
        if (this.startTime != null) {
            this.executionDuration = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 执行失败
     */
    public void fail(String errorMessage) {
        this.status = WorkflowExecution.ExecutionStatus.FAILED;
        this.endTime = LocalDateTime.now();
        this.errorMessage = errorMessage;
        
        if (this.startTime != null) {
            this.executionDuration = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 取消执行
     */
    public void cancel(String reason) {
        this.status = WorkflowExecution.ExecutionStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
        this.cancelledAt = LocalDateTime.now();
        this.cancelReason = reason;
        
        if (this.startTime != null) {
            this.executionDuration = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 暂停执行
     */
    public void pause() {
        this.status = WorkflowExecution.ExecutionStatus.PAUSED;
        this.pausedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 恢复执行
     */
    public void resume() {
        this.status = WorkflowExecution.ExecutionStatus.RUNNING;
        this.resumedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 超时
     */
    public void timeout() {
        this.status = WorkflowExecution.ExecutionStatus.TIMEOUT;
        this.endTime = LocalDateTime.now();
        this.errorMessage = "执行超时";
        
        if (this.startTime != null) {
            this.executionDuration = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新进度
     */
    public void updateProgress(int progress, String currentNodeId) {
        this.progress = Math.max(0, Math.min(100, progress));
        this.currentNodeId = currentNodeId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        this.retryCount++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 是否可以重试
     */
    public boolean canRetry() {
        return this.retryCount < this.maxRetries;
    }
    
    /**
     * 是否正在运行
     */
    public boolean isRunning() {
        return this.status == WorkflowExecution.ExecutionStatus.RUNNING;
    }
    
    /**
     * 是否已完成（成功或失败）
     */
    public boolean isCompleted() {
        return this.status == WorkflowExecution.ExecutionStatus.COMPLETED ||
               this.status == WorkflowExecution.ExecutionStatus.FAILED ||
               this.status == WorkflowExecution.ExecutionStatus.CANCELLED ||
               this.status == WorkflowExecution.ExecutionStatus.TIMEOUT;
    }
    
    /**
     * 是否成功
     */
    public boolean isSuccessful() {
        return this.status == WorkflowExecution.ExecutionStatus.COMPLETED;
    }
    
    /**
     * 是否暂停
     */
    public boolean isPaused() {
        return this.status == WorkflowExecution.ExecutionStatus.PAUSED;
    }
    
    /**
     * 是否可取消
     */
    public boolean isCancellable() {
        return Boolean.TRUE.equals(this.cancellable) && 
               (isRunning() || isPaused());
    }
    
    /**
     * 是否可暂停
     */
    public boolean isPausable() {
        return Boolean.TRUE.equals(this.pausable) && isRunning();
    }
    
    /**
     * 是否可恢复
     */
    public boolean isResumable() {
        return isPaused();
    }
    
    /**
     * 获取执行时长（如果还在执行中，返回到当前时间的时长）
     */
    public long getCurrentExecutionDuration() {
        if (this.executionDuration != null) {
            return this.executionDuration;
        }
        
        if (this.startTime != null) {
            LocalDateTime endTime = this.endTime != null ? this.endTime : LocalDateTime.now();
            return java.time.Duration.between(this.startTime, endTime).toMillis();
        }
        
        return 0L;
    }
}