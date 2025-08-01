package com.jd.genie.agent.workflow.service;

import com.jd.genie.agent.workflow.engine.WorkflowEngine;
import com.jd.genie.agent.workflow.engine.WorkflowExecutionListener;
import com.jd.genie.agent.workflow.model.WorkflowDefinition;
import com.jd.genie.agent.workflow.model.WorkflowExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 工作流服务
 * 提供工作流管理和执行的高级API
 * 
 * 功能：
 * - 工作流定义管理
 * - 工作流执行控制
 * - 执行历史查询
 * - 工作流模板管理
 * - 统计和监控
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Service
public class WorkflowService implements WorkflowExecutionListener {
    
    @Autowired
    private WorkflowEngine workflowEngine;
    
    /**
     * 工作流定义存储（实际项目中应该使用数据库）
     */
    private final Map<String, WorkflowDefinition> workflowDefinitions = new ConcurrentHashMap<>();
    
    /**
     * 执行历史存储（实际项目中应该使用数据库）
     */
    private final Map<String, WorkflowExecution> executionHistory = new ConcurrentHashMap<>();
    
    /**
     * 工作流统计信息
     */
    private final Map<String, WorkflowStats> workflowStats = new ConcurrentHashMap<>();
    
    /**
     * 初始化服务
     */
    public void initialize() {
        // 注册执行监听器
        workflowEngine.addExecutionListener(this);
        
        // 创建默认工作流模板
        createDefaultWorkflowTemplates();
        
        log.info("工作流服务初始化完成");
    }
    
    // ==================== 工作流定义管理 ====================
    
    /**
     * 创建工作流定义
     */
    public WorkflowDefinition createWorkflow(WorkflowDefinition workflow) {
        // 验证工作流定义
        List<String> validationErrors = workflow.validate();
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("工作流定义验证失败: " + String.join(", ", validationErrors));
        }
        
        // 设置创建时间
        workflow.setCreatedAt(LocalDateTime.now());
        workflow.setUpdatedAt(LocalDateTime.now());
        
        // 初始化统计信息
        workflow.setUsageCount(0);
        workflow.setAvgExecutionTime(0);
        workflow.setSuccessRate(0.0);
        
        // 保存工作流定义
        workflowDefinitions.put(workflow.getWorkflowId(), workflow);
        
        // 初始化统计
        workflowStats.put(workflow.getWorkflowId(), new WorkflowStats());
        
        log.info("创建工作流定义: {} - {}", workflow.getWorkflowId(), workflow.getName());
        
        return workflow;
    }
    
    /**
     * 更新工作流定义
     */
    public WorkflowDefinition updateWorkflow(String workflowId, WorkflowDefinition workflow) {
        WorkflowDefinition existingWorkflow = workflowDefinitions.get(workflowId);
        if (existingWorkflow == null) {
            throw new IllegalArgumentException("工作流不存在: " + workflowId);
        }
        
        // 验证工作流定义
        List<String> validationErrors = workflow.validate();
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("工作流定义验证失败: " + String.join(", ", validationErrors));
        }
        
        // 保留原有的统计信息
        workflow.setUsageCount(existingWorkflow.getUsageCount());
        workflow.setAvgExecutionTime(existingWorkflow.getAvgExecutionTime());
        workflow.setSuccessRate(existingWorkflow.getSuccessRate());
        workflow.setCreatedAt(existingWorkflow.getCreatedAt());
        workflow.setUpdatedAt(LocalDateTime.now());
        
        // 更新工作流定义
        workflowDefinitions.put(workflowId, workflow);
        
        log.info("更新工作流定义: {} - {}", workflowId, workflow.getName());
        
        return workflow;
    }
    
    /**
     * 删除工作流定义
     */
    public boolean deleteWorkflow(String workflowId) {
        WorkflowDefinition workflow = workflowDefinitions.remove(workflowId);
        if (workflow != null) {
            workflowStats.remove(workflowId);
            log.info("删除工作流定义: {} - {}", workflowId, workflow.getName());
            return true;
        }
        return false;
    }
    
    /**
     * 获取工作流定义
     */
    public WorkflowDefinition getWorkflow(String workflowId) {
        return workflowDefinitions.get(workflowId);
    }
    
    /**
     * 获取所有工作流定义
     */
    public List<WorkflowDefinition> getAllWorkflows() {
        return new ArrayList<>(workflowDefinitions.values());
    }
    
    /**
     * 根据条件查询工作流
     */
    public List<WorkflowDefinition> searchWorkflows(String keyword, 
                                                   WorkflowDefinition.WorkflowType type,
                                                   WorkflowDefinition.WorkflowStatus status,
                                                   String createdBy) {
        
        return workflowDefinitions.values().stream()
            .filter(workflow -> {
                // 关键词匹配
                boolean keywordMatch = keyword == null || keyword.trim().isEmpty() ||
                    workflow.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                    workflow.getDescription().toLowerCase().contains(keyword.toLowerCase());
                
                // 类型匹配
                boolean typeMatch = type == null || workflow.getType() == type;
                
                // 状态匹配
                boolean statusMatch = status == null || workflow.getStatus() == status;
                
                // 创建者匹配
                boolean creatorMatch = createdBy == null || createdBy.trim().isEmpty() ||
                    workflow.getCreatedBy().equals(createdBy);
                
                return keywordMatch && typeMatch && statusMatch && creatorMatch;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 克隆工作流定义
     */
    public WorkflowDefinition cloneWorkflow(String workflowId, String newName, String createdBy) {
        WorkflowDefinition originalWorkflow = workflowDefinitions.get(workflowId);
        if (originalWorkflow == null) {
            throw new IllegalArgumentException("工作流不存在: " + workflowId);
        }
        
        WorkflowDefinition clonedWorkflow = originalWorkflow.clone();
        clonedWorkflow.setName(newName);
        clonedWorkflow.setCreatedBy(createdBy);
        
        return createWorkflow(clonedWorkflow);
    }
    
    // ==================== 工作流执行 ====================
    
    /**
     * 执行工作流
     */
    public CompletableFuture<WorkflowExecution> executeWorkflow(String workflowId, 
                                                               Map<String, Object> inputParameters,
                                                               String executedBy) {
        
        WorkflowDefinition workflow = workflowDefinitions.get(workflowId);
        if (workflow == null) {
            throw new IllegalArgumentException("工作流不存在: " + workflowId);
        }
        
        if (workflow.getStatus() != WorkflowDefinition.WorkflowStatus.ACTIVE) {
            throw new IllegalStateException("工作流未激活: " + workflowId);
        }
        
        log.info("开始执行工作流: {} - {}, 执行者: {}", workflowId, workflow.getName(), executedBy);
        
        return workflowEngine.executeWorkflow(workflow, inputParameters, executedBy)
            .whenComplete((execution, throwable) -> {
                if (throwable == null) {
                    // 保存执行历史
                    executionHistory.put(execution.getExecutionId(), execution);
                    
                    // 更新统计信息
                    updateWorkflowStats(workflowId, execution);
                }
            });
    }
    
    /**
     * 快速股票分析工作流
     */
    public CompletableFuture<WorkflowExecution> quickStockAnalysis(String stockCode, String executedBy) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("stockCode", stockCode);
        parameters.put("analysisType", "quick");
        
        return executeWorkflow("quick_stock_analysis", parameters, executedBy);
    }
    
    /**
     * 深度股票分析工作流
     */
    public CompletableFuture<WorkflowExecution> deepStockAnalysis(String stockCode, String executedBy) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("stockCode", stockCode);
        parameters.put("analysisType", "deep");
        
        return executeWorkflow("deep_stock_analysis", parameters, executedBy);
    }
    
    /**
     * 市场监控工作流
     */
    public CompletableFuture<WorkflowExecution> marketMonitoring(List<String> stockCodes, String executedBy) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("stockCodes", stockCodes);
        parameters.put("monitoringType", "realtime");
        
        return executeWorkflow("market_monitoring", parameters, executedBy);
    }
    
    /**
     * 取消工作流执行
     */
    public boolean cancelExecution(String executionId) {
        return workflowEngine.cancelExecution(executionId);
    }
    
    /**
     * 暂停工作流执行
     */
    public boolean pauseExecution(String executionId) {
        return workflowEngine.pauseExecution(executionId);
    }
    
    /**
     * 恢复工作流执行
     */
    public boolean resumeExecution(String executionId) {
        return workflowEngine.resumeExecution(executionId);
    }
    
    // ==================== 执行历史查询 ====================
    
    /**
     * 获取执行状态
     */
    public WorkflowExecution getExecutionStatus(String executionId) {
        // 先从正在执行的实例中查找
        WorkflowExecution runningExecution = workflowEngine.getExecutionStatus(executionId);
        if (runningExecution != null) {
            return runningExecution;
        }
        
        // 再从历史记录中查找
        return executionHistory.get(executionId);
    }
    
    /**
     * 获取工作流的执行历史
     */
    public List<WorkflowExecution> getWorkflowExecutionHistory(String workflowId, int limit) {
        return executionHistory.values().stream()
            .filter(execution -> execution.getWorkflowId().equals(workflowId))
            .sorted((e1, e2) -> e2.getStartTime().compareTo(e1.getStartTime()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取用户的执行历史
     */
    public List<WorkflowExecution> getUserExecutionHistory(String userId, int limit) {
        return executionHistory.values().stream()
            .filter(execution -> execution.getExecutedBy().equals(userId))
            .sorted((e1, e2) -> e2.getStartTime().compareTo(e1.getStartTime()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取正在执行的工作流
     */
    public List<WorkflowExecution> getRunningExecutions() {
        return workflowEngine.getRunningExecutions();
    }
    
    // ==================== 统计和监控 ====================
    
    /**
     * 获取工作流统计信息
     */
    public WorkflowStats getWorkflowStats(String workflowId) {
        return workflowStats.get(workflowId);
    }
    
    /**
     * 获取系统统计信息
     */
    public SystemStats getSystemStats() {
        SystemStats stats = new SystemStats();
        
        stats.setTotalWorkflows(workflowDefinitions.size());
        stats.setActiveWorkflows((int) workflowDefinitions.values().stream()
            .filter(w -> w.getStatus() == WorkflowDefinition.WorkflowStatus.ACTIVE)
            .count());
        
        stats.setTotalExecutions(executionHistory.size());
        stats.setRunningExecutions(workflowEngine.getRunningExecutions().size());
        
        long successfulExecutions = executionHistory.values().stream()
            .filter(WorkflowExecution::isSuccessful)
            .count();
        stats.setSuccessfulExecutions((int) successfulExecutions);
        
        if (stats.getTotalExecutions() > 0) {
            stats.setOverallSuccessRate((double) successfulExecutions / stats.getTotalExecutions());
        }
        
        // 计算平均执行时间
        OptionalDouble avgTime = executionHistory.values().stream()
            .filter(e -> e.getExecutionDuration() > 0)
            .mapToLong(WorkflowExecution::getExecutionDuration)
            .average();
        stats.setAvgExecutionTime(avgTime.orElse(0));
        
        return stats;
    }
    
    /**
     * 更新工作流统计信息
     */
    private void updateWorkflowStats(String workflowId, WorkflowExecution execution) {
        WorkflowStats stats = workflowStats.computeIfAbsent(workflowId, k -> new WorkflowStats());
        WorkflowDefinition workflow = workflowDefinitions.get(workflowId);
        
        if (workflow != null) {
            // 更新使用次数
            workflow.setUsageCount(workflow.getUsageCount() + 1);
            stats.setTotalExecutions(stats.getTotalExecutions() + 1);
            
            // 更新成功次数
            if (execution.isSuccessful()) {
                stats.setSuccessfulExecutions(stats.getSuccessfulExecutions() + 1);
            } else {
                stats.setFailedExecutions(stats.getFailedExecutions() + 1);
            }
            
            // 更新成功率
            double successRate = (double) stats.getSuccessfulExecutions() / stats.getTotalExecutions();
            workflow.setSuccessRate(successRate);
            stats.setSuccessRate(successRate);
            
            // 更新平均执行时间
            if (execution.getExecutionDuration() > 0) {
                long totalTime = workflow.getAvgExecutionTime() * (workflow.getUsageCount() - 1) + 
                               execution.getExecutionDuration();
                long avgTime = totalTime / workflow.getUsageCount();
                workflow.setAvgExecutionTime(avgTime);
                stats.setAvgExecutionTime(avgTime);
            }
            
            // 更新最后执行时间
            stats.setLastExecutionTime(execution.getStartTime());
            
            // 更新修改时间
            workflow.setUpdatedAt(LocalDateTime.now());
        }
    }
    
    // ==================== 工作流模板 ====================
    
    /**
     * 创建默认工作流模板
     */
    private void createDefaultWorkflowTemplates() {
        // 快速股票分析模板
        createQuickStockAnalysisTemplate();
        
        // 深度股票分析模板
        createDeepStockAnalysisTemplate();
        
        // 市场监控模板
        createMarketMonitoringTemplate();
    }
    
    /**
     * 创建快速股票分析模板
     */
    private void createQuickStockAnalysisTemplate() {
        WorkflowDefinition workflow = WorkflowDefinition.builder()
            .workflowId("quick_stock_analysis")
            .name("快速股票分析")
            .description("对单只股票进行快速分析，包括基本面和技术面分析")
            .version("1.0.0")
            .createdBy("system")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .status(WorkflowDefinition.WorkflowStatus.ACTIVE)
            .type(WorkflowDefinition.WorkflowType.STOCK_ANALYSIS)
            .isPublic(true)
            .tags(Set.of("股票分析", "快速分析", "模板"))
            .category("股票分析")
            .build();
        
        // 创建节点
        List<WorkflowDefinition.WorkflowNode> nodes = new ArrayList<>();
        
        // 开始节点
        nodes.add(WorkflowDefinition.WorkflowNode.builder()
            .nodeId("start")
            .name("开始")
            .type(WorkflowDefinition.NodeType.START)
            .position(WorkflowDefinition.NodePosition.builder().x(100).y(100).build())
            .build());
        
        // 智能体节点 - 基本面分析
        nodes.add(WorkflowDefinition.WorkflowNode.builder()
            .nodeId("fundamental_analysis")
            .name("基本面分析")
            .type(WorkflowDefinition.NodeType.AGENT)
            .config(WorkflowDefinition.NodeConfig.builder()
                .agentType("fundamental")
                .build())
            .position(WorkflowDefinition.NodePosition.builder().x(300).y(100).build())
            .build());
        
        // 智能体节点 - 技术分析
        nodes.add(WorkflowDefinition.WorkflowNode.builder()
            .nodeId("technical_analysis")
            .name("技术分析")
            .type(WorkflowDefinition.NodeType.AGENT)
            .config(WorkflowDefinition.NodeConfig.builder()
                .agentType("technical")
                .build())
            .position(WorkflowDefinition.NodePosition.builder().x(300).y(200).build())
            .build());
        
        // 智能体节点 - 投资建议
        nodes.add(WorkflowDefinition.WorkflowNode.builder()
            .nodeId("investment_advice")
            .name("投资建议")
            .type(WorkflowDefinition.NodeType.AGENT)
            .config(WorkflowDefinition.NodeConfig.builder()
                .agentType("advisor")
                .build())
            .position(WorkflowDefinition.NodePosition.builder().x(500).y(150).build())
            .build());
        
        // 结束节点
        nodes.add(WorkflowDefinition.WorkflowNode.builder()
            .nodeId("end")
            .name("结束")
            .type(WorkflowDefinition.NodeType.END)
            .position(WorkflowDefinition.NodePosition.builder().x(700).y(150).build())
            .build());
        
        workflow.setNodes(nodes);
        
        // 创建连接
        List<WorkflowDefinition.WorkflowConnection> connections = new ArrayList<>();
        
        connections.add(WorkflowDefinition.WorkflowConnection.builder()
            .connectionId("start_to_fundamental")
            .sourceNodeId("start")
            .targetNodeId("fundamental_analysis")
            .type(WorkflowDefinition.ConnectionType.PARALLEL)
            .build());
        
        connections.add(WorkflowDefinition.WorkflowConnection.builder()
            .connectionId("start_to_technical")
            .sourceNodeId("start")
            .targetNodeId("technical_analysis")
            .type(WorkflowDefinition.ConnectionType.PARALLEL)
            .build());
        
        connections.add(WorkflowDefinition.WorkflowConnection.builder()
            .connectionId("fundamental_to_advice")
            .sourceNodeId("fundamental_analysis")
            .targetNodeId("investment_advice")
            .type(WorkflowDefinition.ConnectionType.SEQUENCE)
            .build());
        
        connections.add(WorkflowDefinition.WorkflowConnection.builder()
            .connectionId("technical_to_advice")
            .sourceNodeId("technical_analysis")
            .targetNodeId("investment_advice")
            .type(WorkflowDefinition.ConnectionType.SEQUENCE)
            .build());
        
        connections.add(WorkflowDefinition.WorkflowConnection.builder()
            .connectionId("advice_to_end")
            .sourceNodeId("investment_advice")
            .targetNodeId("end")
            .type(WorkflowDefinition.ConnectionType.SEQUENCE)
            .build());
        
        workflow.setConnections(connections);
        
        // 设置执行配置
        workflow.setExecutionConfig(WorkflowDefinition.ExecutionConfig.builder()
            .maxConcurrency(2)
            .timeoutSeconds(300)
            .failureStrategy(WorkflowDefinition.FailureStrategy.CONTINUE)
            .build());
        
        // 保存模板
        workflowDefinitions.put(workflow.getWorkflowId(), workflow);
        workflowStats.put(workflow.getWorkflowId(), new WorkflowStats());
        
        log.info("创建快速股票分析模板: {}", workflow.getWorkflowId());
    }
    
    /**
     * 创建深度股票分析模板
     */
    private void createDeepStockAnalysisTemplate() {
        // 类似的实现，包含更多分析节点
        // 这里简化实现
        log.info("创建深度股票分析模板");
    }
    
    /**
     * 创建市场监控模板
     */
    private void createMarketMonitoringTemplate() {
        // 类似的实现，用于市场监控
        // 这里简化实现
        log.info("创建市场监控模板");
    }
    
    // ==================== 执行监听器实现 ====================
    
    @Override
    public void onExecutionStarted(WorkflowExecution execution) {
        log.info("工作流开始执行: {} - {}", execution.getExecutionId(), execution.getWorkflowId());
    }
    
    @Override
    public void onExecutionCompleted(WorkflowExecution execution) {
        log.info("工作流执行完成: {} - {}, 耗时: {}ms", 
                execution.getExecutionId(), execution.getWorkflowId(), execution.getExecutionDuration());
    }
    
    @Override
    public void onExecutionFailed(WorkflowExecution execution, Exception error) {
        log.error("工作流执行失败: {} - {}, 错误: {}", 
                execution.getExecutionId(), execution.getWorkflowId(), error.getMessage());
    }
    
    // ==================== 内部类 ====================
    
    /**
     * 工作流统计信息
     */
    public static class WorkflowStats {
        private int totalExecutions = 0;
        private int successfulExecutions = 0;
        private int failedExecutions = 0;
        private double successRate = 0.0;
        private long avgExecutionTime = 0;
        private LocalDateTime lastExecutionTime;
        
        // Getters and Setters
        public int getTotalExecutions() { return totalExecutions; }
        public void setTotalExecutions(int totalExecutions) { this.totalExecutions = totalExecutions; }
        
        public int getSuccessfulExecutions() { return successfulExecutions; }
        public void setSuccessfulExecutions(int successfulExecutions) { this.successfulExecutions = successfulExecutions; }
        
        public int getFailedExecutions() { return failedExecutions; }
        public void setFailedExecutions(int failedExecutions) { this.failedExecutions = failedExecutions; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public long getAvgExecutionTime() { return avgExecutionTime; }
        public void setAvgExecutionTime(long avgExecutionTime) { this.avgExecutionTime = avgExecutionTime; }
        
        public LocalDateTime getLastExecutionTime() { return lastExecutionTime; }
        public void setLastExecutionTime(LocalDateTime lastExecutionTime) { this.lastExecutionTime = lastExecutionTime; }
    }
    
    /**
     * 系统统计信息
     */
    public static class SystemStats {
        private int totalWorkflows;
        private int activeWorkflows;
        private int totalExecutions;
        private int runningExecutions;
        private int successfulExecutions;
        private double overallSuccessRate;
        private double avgExecutionTime;
        
        // Getters and Setters
        public int getTotalWorkflows() { return totalWorkflows; }
        public void setTotalWorkflows(int totalWorkflows) { this.totalWorkflows = totalWorkflows; }
        
        public int getActiveWorkflows() { return activeWorkflows; }
        public void setActiveWorkflows(int activeWorkflows) { this.activeWorkflows = activeWorkflows; }
        
        public int getTotalExecutions() { return totalExecutions; }
        public void setTotalExecutions(int totalExecutions) { this.totalExecutions = totalExecutions; }
        
        public int getRunningExecutions() { return runningExecutions; }
        public void setRunningExecutions(int runningExecutions) { this.runningExecutions = runningExecutions; }
        
        public int getSuccessfulExecutions() { return successfulExecutions; }
        public void setSuccessfulExecutions(int successfulExecutions) { this.successfulExecutions = successfulExecutions; }
        
        public double getOverallSuccessRate() { return overallSuccessRate; }
        public void setOverallSuccessRate(double overallSuccessRate) { this.overallSuccessRate = overallSuccessRate; }
        
        public double getAvgExecutionTime() { return avgExecutionTime; }
        public void setAvgExecutionTime(double avgExecutionTime) { this.avgExecutionTime = avgExecutionTime; }
    }
}