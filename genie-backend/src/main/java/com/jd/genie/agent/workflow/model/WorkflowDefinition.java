package com.jd.genie.agent.workflow.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 工作流定义模型
 * 定义用户自定义的股票分析工作流程
 * 
 * 功能：
 * - 工作流基本信息管理
 * - 节点和连接关系定义
 * - 触发条件和执行策略
 * - 版本控制和状态管理
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDefinition {
    
    /**
     * 工作流唯一标识
     */
    @NotBlank(message = "工作流ID不能为空")
    private String workflowId;
    
    /**
     * 工作流名称
     */
    @NotBlank(message = "工作流名称不能为空")
    @Size(max = 100, message = "工作流名称不能超过100个字符")
    private String name;
    
    /**
     * 工作流描述
     */
    @Size(max = 500, message = "工作流描述不能超过500个字符")
    private String description;
    
    /**
     * 工作流版本
     */
    @NotBlank(message = "版本号不能为空")
    private String version;
    
    /**
     * 创建者ID
     */
    @NotBlank(message = "创建者ID不能为空")
    private String createdBy;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * 工作流状态
     */
    @NotNull(message = "工作流状态不能为空")
    private WorkflowStatus status;
    
    /**
     * 工作流类型
     */
    @NotNull(message = "工作流类型不能为空")
    private WorkflowType type;
    
    /**
     * 工作流节点列表
     */
    @Valid
    @NotEmpty(message = "工作流节点不能为空")
    private List<WorkflowNode> nodes;
    
    /**
     * 节点连接关系
     */
    @Valid
    private List<WorkflowConnection> connections;
    
    /**
     * 触发条件
     */
    @Valid
    private WorkflowTrigger trigger;
    
    /**
     * 执行配置
     */
    @Valid
    private ExecutionConfig executionConfig;
    
    /**
     * 工作流标签
     */
    private Set<String> tags;
    
    /**
     * 工作流分类
     */
    private String category;
    
    /**
     * 是否公开（其他用户可见）
     */
    private boolean isPublic;
    
    /**
     * 使用次数
     */
    private long usageCount;
    
    /**
     * 平均执行时间（毫秒）
     */
    private long avgExecutionTime;
    
    /**
     * 成功率
     */
    @DecimalMin(value = "0.0", message = "成功率不能小于0")
    @DecimalMax(value = "1.0", message = "成功率不能大于1")
    private double successRate;
    
    /**
     * 工作流元数据
     */
    private Map<String, Object> metadata;
    
    // ==================== 工作流节点 ====================
    
    /**
     * 工作流节点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowNode {
        
        /**
         * 节点ID
         */
        @NotBlank(message = "节点ID不能为空")
        private String nodeId;
        
        /**
         * 节点名称
         */
        @NotBlank(message = "节点名称不能为空")
        private String name;
        
        /**
         * 节点类型
         */
        @NotNull(message = "节点类型不能为空")
        private NodeType type;
        
        /**
         * 节点配置
         */
        @Valid
        private NodeConfig config;
        
        /**
         * 节点位置（用于UI显示）
         */
        private NodePosition position;
        
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * 节点描述
         */
        private String description;
        
        /**
         * 超时时间（秒）
         */
        @Positive(message = "超时时间必须大于0")
        private int timeoutSeconds = 300;
        
        /**
         * 重试次数
         */
        @Min(value = 0, message = "重试次数不能小于0")
        private int retryCount = 0;
        
        /**
         * 条件表达式（用于条件节点）
         */
        private String condition;
        
        /**
         * 节点标签
         */
        private Set<String> tags;
    }
    
    /**
     * 节点配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeConfig {
        
        /**
         * 智能体类型（用于智能体节点）
         */
        private String agentType;
        
        /**
         * MCP工具名称（用于工具节点）
         */
        private String toolName;
        
        /**
         * 输入参数
         */
        private Map<String, Object> inputParameters;
        
        /**
         * 输出映射
         */
        private Map<String, String> outputMapping;
        
        /**
         * 自定义脚本（用于脚本节点）
         */
        private String script;
        
        /**
         * 脚本语言
         */
        private String scriptLanguage;
        
        /**
         * HTTP配置（用于HTTP节点）
         */
        private HttpConfig httpConfig;
        
        /**
         * 数据库配置（用于数据库节点）
         */
        private DatabaseConfig databaseConfig;
    }
    
    /**
     * HTTP配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HttpConfig {
        private String url;
        private String method;
        private Map<String, String> headers;
        private String body;
        private int timeoutSeconds = 30;
    }
    
    /**
     * 数据库配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseConfig {
        private String dataSource;
        private String sql;
        private Map<String, Object> parameters;
    }
    
    /**
     * 节点位置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodePosition {
        private double x;
        private double y;
    }
    
    // ==================== 工作流连接 ====================
    
    /**
     * 工作流连接
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowConnection {
        
        /**
         * 连接ID
         */
        @NotBlank(message = "连接ID不能为空")
        private String connectionId;
        
        /**
         * 源节点ID
         */
        @NotBlank(message = "源节点ID不能为空")
        private String sourceNodeId;
        
        /**
         * 目标节点ID
         */
        @NotBlank(message = "目标节点ID不能为空")
        private String targetNodeId;
        
        /**
         * 连接类型
         */
        @NotNull(message = "连接类型不能为空")
        private ConnectionType type;
        
        /**
         * 连接条件
         */
        private String condition;
        
        /**
         * 数据映射
         */
        private Map<String, String> dataMapping;
        
        /**
         * 是否启用
         */
        private boolean enabled = true;
    }
    
    // ==================== 工作流触发器 ====================
    
    /**
     * 工作流触发器
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowTrigger {
        
        /**
         * 触发类型
         */
        @NotNull(message = "触发类型不能为空")
        private TriggerType type;
        
        /**
         * 定时触发配置
         */
        private ScheduleTrigger schedule;
        
        /**
         * 事件触发配置
         */
        private EventTrigger event;
        
        /**
         * 手动触发配置
         */
        private ManualTrigger manual;
    }
    
    /**
     * 定时触发器
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleTrigger {
        
        /**
         * Cron表达式
         */
        @NotBlank(message = "Cron表达式不能为空")
        private String cronExpression;
        
        /**
         * 时区
         */
        private String timezone = "Asia/Shanghai";
        
        /**
         * 开始时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;
        
        /**
         * 结束时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;
    }
    
    /**
     * 事件触发器
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventTrigger {
        
        /**
         * 事件类型
         */
        @NotBlank(message = "事件类型不能为空")
        private String eventType;
        
        /**
         * 事件过滤条件
         */
        private Map<String, Object> filters;
    }
    
    /**
     * 手动触发器
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManualTrigger {
        
        /**
         * 是否需要参数
         */
        private boolean requiresParameters;
        
        /**
         * 参数模板
         */
        private Map<String, Object> parameterTemplate;
    }
    
    // ==================== 执行配置 ====================
    
    /**
     * 执行配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionConfig {
        
        /**
         * 最大并发数
         */
        @Positive(message = "最大并发数必须大于0")
        private int maxConcurrency = 1;
        
        /**
         * 超时时间（秒）
         */
        @Positive(message = "超时时间必须大于0")
        private int timeoutSeconds = 3600;
        
        /**
         * 失败策略
         */
        @NotNull(message = "失败策略不能为空")
        private FailureStrategy failureStrategy;
        
        /**
         * 重试配置
         */
        private RetryConfig retryConfig;
        
        /**
         * 通知配置
         */
        private NotificationConfig notificationConfig;
        
        /**
         * 环境变量
         */
        private Map<String, String> environment;
    }
    
    /**
     * 重试配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetryConfig {
        
        /**
         * 最大重试次数
         */
        @Min(value = 0, message = "重试次数不能小于0")
        private int maxRetries = 0;
        
        /**
         * 重试间隔（秒）
         */
        @Positive(message = "重试间隔必须大于0")
        private int retryIntervalSeconds = 60;
        
        /**
         * 重试策略
         */
        private RetryStrategy strategy = RetryStrategy.FIXED;
    }
    
    /**
     * 通知配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationConfig {
        
        /**
         * 是否启用通知
         */
        private boolean enabled = false;
        
        /**
         * 通知类型
         */
        private Set<NotificationType> types;
        
        /**
         * 通知接收者
         */
        private List<String> recipients;
        
        /**
         * 通知模板
         */
        private Map<String, String> templates;
    }
    
    // ==================== 枚举定义 ====================
    
    /**
     * 工作流状态
     */
    public enum WorkflowStatus {
        DRAFT,      // 草稿
        ACTIVE,     // 激活
        INACTIVE,   // 停用
        ARCHIVED    // 归档
    }
    
    /**
     * 工作流类型
     */
    public enum WorkflowType {
        STOCK_ANALYSIS,     // 股票分析
        MARKET_MONITORING,  // 市场监控
        RISK_MANAGEMENT,    // 风险管理
        PORTFOLIO_OPTIMIZATION, // 投资组合优化
        CUSTOM             // 自定义
    }
    
    /**
     * 节点类型
     */
    public enum NodeType {
        START,          // 开始节点
        END,            // 结束节点
        AGENT,          // 智能体节点
        TOOL,           // MCP工具节点
        CONDITION,      // 条件节点
        PARALLEL,       // 并行节点
        MERGE,          // 合并节点
        SCRIPT,         // 脚本节点
        HTTP,           // HTTP请求节点
        DATABASE,       // 数据库节点
        DELAY,          // 延迟节点
        NOTIFICATION    // 通知节点
    }
    
    /**
     * 连接类型
     */
    public enum ConnectionType {
        SEQUENCE,       // 顺序连接
        CONDITION,      // 条件连接
        PARALLEL,       // 并行连接
        ERROR          // 错误连接
    }
    
    /**
     * 触发类型
     */
    public enum TriggerType {
        MANUAL,         // 手动触发
        SCHEDULE,       // 定时触发
        EVENT          // 事件触发
    }
    
    /**
     * 失败策略
     */
    public enum FailureStrategy {
        STOP,           // 停止执行
        CONTINUE,       // 继续执行
        RETRY,          // 重试
        SKIP           // 跳过
    }
    
    /**
     * 重试策略
     */
    public enum RetryStrategy {
        FIXED,          // 固定间隔
        EXPONENTIAL,    // 指数退避
        LINEAR         // 线性增长
    }
    
    /**
     * 通知类型
     */
    public enum NotificationType {
        EMAIL,          // 邮件
        SMS,            // 短信
        WEBHOOK,        // Webhook
        SYSTEM         // 系统通知
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 获取开始节点
     */
    public WorkflowNode getStartNode() {
        return nodes.stream()
            .filter(node -> node.getType() == NodeType.START)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * 获取结束节点
     */
    public List<WorkflowNode> getEndNodes() {
        return nodes.stream()
            .filter(node -> node.getType() == NodeType.END)
            .toList();
    }
    
    /**
     * 根据ID获取节点
     */
    public WorkflowNode getNodeById(String nodeId) {
        return nodes.stream()
            .filter(node -> node.getNodeId().equals(nodeId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * 获取节点的输出连接
     */
    public List<WorkflowConnection> getNodeOutputConnections(String nodeId) {
        return connections.stream()
            .filter(conn -> conn.getSourceNodeId().equals(nodeId) && conn.isEnabled())
            .toList();
    }
    
    /**
     * 获取节点的输入连接
     */
    public List<WorkflowConnection> getNodeInputConnections(String nodeId) {
        return connections.stream()
            .filter(conn -> conn.getTargetNodeId().equals(nodeId) && conn.isEnabled())
            .toList();
    }
    
    /**
     * 验证工作流定义
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        
        // 检查开始节点
        long startNodeCount = nodes.stream()
            .filter(node -> node.getType() == NodeType.START)
            .count();
        if (startNodeCount == 0) {
            errors.add("工作流必须包含一个开始节点");
        } else if (startNodeCount > 1) {
            errors.add("工作流只能包含一个开始节点");
        }
        
        // 检查结束节点
        long endNodeCount = nodes.stream()
            .filter(node -> node.getType() == NodeType.END)
            .count();
        if (endNodeCount == 0) {
            errors.add("工作流必须包含至少一个结束节点");
        }
        
        // 检查节点连接
        for (WorkflowConnection connection : connections) {
            if (getNodeById(connection.getSourceNodeId()) == null) {
                errors.add("连接的源节点不存在: " + connection.getSourceNodeId());
            }
            if (getNodeById(connection.getTargetNodeId()) == null) {
                errors.add("连接的目标节点不存在: " + connection.getTargetNodeId());
            }
        }
        
        // 检查循环依赖
        if (hasCyclicDependency()) {
            errors.add("工作流存在循环依赖");
        }
        
        return errors;
    }
    
    /**
     * 检查是否存在循环依赖
     */
    private boolean hasCyclicDependency() {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        
        for (WorkflowNode node : nodes) {
            if (hasCyclicDependencyUtil(node.getNodeId(), visited, recursionStack)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean hasCyclicDependencyUtil(String nodeId, Set<String> visited, Set<String> recursionStack) {
        if (recursionStack.contains(nodeId)) {
            return true;
        }
        
        if (visited.contains(nodeId)) {
            return false;
        }
        
        visited.add(nodeId);
        recursionStack.add(nodeId);
        
        List<WorkflowConnection> outputConnections = getNodeOutputConnections(nodeId);
        for (WorkflowConnection connection : outputConnections) {
            if (hasCyclicDependencyUtil(connection.getTargetNodeId(), visited, recursionStack)) {
                return true;
            }
        }
        
        recursionStack.remove(nodeId);
        return false;
    }
    
    /**
     * 克隆工作流定义
     */
    public WorkflowDefinition clone() {
        return WorkflowDefinition.builder()
            .workflowId(UUID.randomUUID().toString())
            .name(this.name + "_copy")
            .description(this.description)
            .version("1.0.0")
            .createdBy(this.createdBy)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .status(WorkflowStatus.DRAFT)
            .type(this.type)
            .nodes(new ArrayList<>(this.nodes))
            .connections(new ArrayList<>(this.connections))
            .trigger(this.trigger)
            .executionConfig(this.executionConfig)
            .tags(new HashSet<>(this.tags != null ? this.tags : Collections.emptySet()))
            .category(this.category)
            .isPublic(false)
            .usageCount(0)
            .avgExecutionTime(0)
            .successRate(0.0)
            .metadata(new HashMap<>(this.metadata != null ? this.metadata : Collections.emptyMap()))
            .build();
    }
}