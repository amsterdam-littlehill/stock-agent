package com.jd.genie.agent.workflow.entity;

import com.jd.genie.agent.workflow.model.WorkflowDefinition;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 工作流定义实体类
 * 用于数据库持久化工作流定义信息
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "workflow_definitions", indexes = {
    @Index(name = "idx_workflow_id", columnList = "workflowId", unique = true),
    @Index(name = "idx_created_by", columnList = "createdBy"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
public class WorkflowDefinitionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 工作流ID（业务主键）
     */
    @Column(name = "workflow_id", nullable = false, unique = true, length = 100)
    private String workflowId;
    
    /**
     * 工作流名称
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    /**
     * 工作流描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * 版本号
     */
    @Column(name = "version", nullable = false, length = 50)
    private String version;
    
    /**
     * 创建者
     */
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
    
    /**
     * 工作流类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private WorkflowDefinition.WorkflowType type;
    
    /**
     * 工作流状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private WorkflowDefinition.WorkflowStatus status = WorkflowDefinition.WorkflowStatus.DRAFT;
    
    /**
     * 是否公开
     */
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;
    
    /**
     * 分类
     */
    @Column(name = "category", length = 100)
    private String category;
    
    /**
     * 标签（JSON格式存储）
     */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;
    
    /**
     * 节点定义（JSON格式存储）
     */
    @Column(name = "nodes", nullable = false, columnDefinition = "LONGTEXT")
    private String nodes;
    
    /**
     * 连接定义（JSON格式存储）
     */
    @Column(name = "connections", nullable = false, columnDefinition = "LONGTEXT")
    private String connections;
    
    /**
     * 触发器配置（JSON格式存储）
     */
    @Column(name = "triggers", columnDefinition = "TEXT")
    private String triggers;
    
    /**
     * 执行配置（JSON格式存储）
     */
    @Column(name = "execution_config", columnDefinition = "TEXT")
    private String executionConfig;
    
    /**
     * 输入参数定义（JSON格式存储）
     */
    @Column(name = "input_parameters", columnDefinition = "TEXT")
    private String inputParameters;
    
    /**
     * 输出参数定义（JSON格式存储）
     */
    @Column(name = "output_parameters", columnDefinition = "TEXT")
    private String outputParameters;
    
    /**
     * 使用次数
     */
    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;
    
    /**
     * 平均执行时间（毫秒）
     */
    @Column(name = "avg_execution_time", nullable = false)
    private Long avgExecutionTime = 0L;
    
    /**
     * 成功率
     */
    @Column(name = "success_rate", nullable = false)
    private Double successRate = 0.0;
    
    /**
     * 最后执行时间
     */
    @Column(name = "last_executed_at")
    private LocalDateTime lastExecutedAt;
    
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
     * 软删除标记
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
    /**
     * 删除时间
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
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
    public WorkflowDefinition toDomainModel() {
        // 这里需要实现JSON反序列化逻辑
        // 简化实现，实际项目中应该使用ObjectMapper
        WorkflowDefinition workflow = WorkflowDefinition.builder()
            .workflowId(this.workflowId)
            .name(this.name)
            .description(this.description)
            .version(this.version)
            .createdBy(this.createdBy)
            .type(this.type)
            .status(this.status)
            .isPublic(this.isPublic)
            .category(this.category)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
        
        workflow.setUsageCount(this.usageCount);
        workflow.setAvgExecutionTime(this.avgExecutionTime);
        workflow.setSuccessRate(this.successRate);
        workflow.setLastExecutedAt(this.lastExecutedAt);
        
        return workflow;
    }
    
    /**
     * 从领域模型创建实体
     */
    public static WorkflowDefinitionEntity fromDomainModel(WorkflowDefinition workflow) {
        WorkflowDefinitionEntity entity = new WorkflowDefinitionEntity();
        
        entity.setWorkflowId(workflow.getWorkflowId());
        entity.setName(workflow.getName());
        entity.setDescription(workflow.getDescription());
        entity.setVersion(workflow.getVersion());
        entity.setCreatedBy(workflow.getCreatedBy());
        entity.setType(workflow.getType());
        entity.setStatus(workflow.getStatus());
        entity.setIsPublic(workflow.isPublic());
        entity.setCategory(workflow.getCategory());
        entity.setUsageCount(workflow.getUsageCount());
        entity.setAvgExecutionTime(workflow.getAvgExecutionTime());
        entity.setSuccessRate(workflow.getSuccessRate());
        entity.setLastExecutedAt(workflow.getLastExecutedAt());
        entity.setCreatedAt(workflow.getCreatedAt());
        entity.setUpdatedAt(workflow.getUpdatedAt());
        
        // 这里需要实现JSON序列化逻辑
        // 简化实现，实际项目中应该使用ObjectMapper
        
        return entity;
    }
    
    /**
     * 更新统计信息
     */
    public void updateStats(int executionCount, long totalExecutionTime, int successCount) {
        this.usageCount += executionCount;
        
        if (this.usageCount > 0) {
            // 更新平均执行时间
            long totalTime = this.avgExecutionTime * (this.usageCount - executionCount) + totalExecutionTime;
            this.avgExecutionTime = totalTime / this.usageCount;
            
            // 更新成功率
            this.successRate = (double) successCount / this.usageCount;
        }
        
        this.lastExecutedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 软删除
     */
    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 恢复删除
     */
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 是否已删除
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }
    
    /**
     * 激活工作流
     */
    public void activate() {
        this.status = WorkflowDefinition.WorkflowStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 停用工作流
     */
    public void deactivate() {
        this.status = WorkflowDefinition.WorkflowStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 归档工作流
     */
    public void archive() {
        this.status = WorkflowDefinition.WorkflowStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 是否可执行
     */
    public boolean isExecutable() {
        return this.status == WorkflowDefinition.WorkflowStatus.ACTIVE && !isDeleted();
    }
    
    /**
     * 是否可编辑
     */
    public boolean isEditable() {
        return this.status != WorkflowDefinition.WorkflowStatus.ARCHIVED && !isDeleted();
    }
}