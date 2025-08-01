package com.jd.genie.agent.workflow.engine;

import com.jd.genie.agent.orchestrator.AgentOrchestrator;
import com.jd.genie.agent.mcp.McpIntegrationService;
import com.jd.genie.agent.workflow.model.WorkflowDefinition;
import com.jd.genie.agent.workflow.model.WorkflowExecution;
import com.jd.genie.agent.workflow.engine.executors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 工作流引擎核心服务
 * 负责工作流的执行、调度和管理
 * 
 * 功能：
 * - 工作流执行引擎
 * - 节点调度和执行
 * - 并行和串行执行控制
 * - 错误处理和重试机制
 * - 执行状态监控
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Service
public class WorkflowEngine {
    
    @Autowired
    private AgentOrchestrator agentOrchestrator;
    
    @Autowired
    private McpIntegrationService mcpIntegrationService;
    
    /**
     * 工作流执行器线程池
     */
    private ExecutorService workflowExecutor;
    
    /**
     * 节点执行器线程池
     */
    private ExecutorService nodeExecutor;
    
    /**
     * 定时任务调度器
     */
    private ScheduledExecutorService scheduler;
    
    /**
     * 正在执行的工作流实例
     */
    private final Map<String, WorkflowExecution> runningExecutions = new ConcurrentHashMap<>();
    
    /**
     * 工作流执行监听器
     */
    private final List<WorkflowExecutionListener> listeners = new ArrayList<>();
    
    /**
     * 节点执行器映射
     */
    private final Map<WorkflowDefinition.NodeType, NodeExecutor> nodeExecutors = new HashMap<>();
    
    @PostConstruct
    public void initialize() {
        // 初始化线程池
        workflowExecutor = Executors.newFixedThreadPool(10, 
            r -> new Thread(r, "workflow-executor-" + System.currentTimeMillis()));
        nodeExecutor = Executors.newFixedThreadPool(20, 
            r -> new Thread(r, "node-executor-" + System.currentTimeMillis()));
        scheduler = Executors.newScheduledThreadPool(5, 
            r -> new Thread(r, "workflow-scheduler-" + System.currentTimeMillis()));
        
        // 注册节点执行器
        registerNodeExecutors();
        
        // 启动监控任务
        startMonitoringTasks();
        
        log.info("工作流引擎初始化完成");
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("开始关闭工作流引擎...");
        
        // 停止接受新任务
        workflowExecutor.shutdown();
        nodeExecutor.shutdown();
        scheduler.shutdown();
        
        try {
            // 等待现有任务完成
            if (!workflowExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                workflowExecutor.shutdownNow();
            }
            if (!nodeExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                nodeExecutor.shutdownNow();
            }
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            workflowExecutor.shutdownNow();
            nodeExecutor.shutdownNow();
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        log.info("工作流引擎已关闭");
    }
    
    /**
     * 执行工作流
     */
    @Async
    public CompletableFuture<WorkflowExecution> executeWorkflow(
            WorkflowDefinition workflow, 
            Map<String, Object> inputParameters,
            String executedBy) {
        
        // 创建执行实例
        WorkflowExecution execution = WorkflowExecution.create(
            workflow.getWorkflowId(),
            workflow.getVersion(),
            executedBy,
            WorkflowExecution.TriggerMode.MANUAL,
            inputParameters
        );
        
        return executeWorkflow(workflow, execution);
    }
    
    /**
     * 执行工作流（使用现有执行实例）
     */
    public CompletableFuture<WorkflowExecution> executeWorkflow(
            WorkflowDefinition workflow, 
            WorkflowExecution execution) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 验证工作流定义
                List<String> validationErrors = workflow.validate();
                if (!validationErrors.isEmpty()) {
                    throw new WorkflowExecutionException(
                        "工作流定义验证失败: " + String.join(", ", validationErrors));
                }
                
                // 注册执行实例
                runningExecutions.put(execution.getExecutionId(), execution);
                
                // 通知监听器
                notifyListeners(listener -> listener.onExecutionStarted(execution));
                
                // 开始执行
                execution.start();
                
                // 执行工作流
                executeWorkflowInternal(workflow, execution);
                
                // 完成执行
                if (execution.getStatus() == WorkflowExecution.ExecutionStatus.RUNNING) {
                    execution.complete();
                    notifyListeners(listener -> listener.onExecutionCompleted(execution));
                }
                
                return execution;
                
            } catch (Exception e) {
                log.error("工作流执行失败: {}", execution.getExecutionId(), e);
                execution.fail(e.getMessage(), getStackTrace(e));
                notifyListeners(listener -> listener.onExecutionFailed(execution, e));
                return execution;
                
            } finally {
                // 清理执行实例
                runningExecutions.remove(execution.getExecutionId());
            }
        }, workflowExecutor);
    }
    
    /**
     * 内部工作流执行逻辑
     */
    private void executeWorkflowInternal(WorkflowDefinition workflow, WorkflowExecution execution) 
            throws WorkflowExecutionException {
        
        // 获取开始节点
        WorkflowDefinition.WorkflowNode startNode = workflow.getStartNode();
        if (startNode == null) {
            throw new WorkflowExecutionException("工作流缺少开始节点");
        }
        
        // 初始化执行上下文
        execution.setContextVariable("workflow", workflow);
        execution.setContextVariable("startTime", LocalDateTime.now());
        
        // 从开始节点执行
        executeNode(workflow, execution, startNode, execution.getInputParameters());
    }
    
    /**
     * 执行节点
     */
    private void executeNode(WorkflowDefinition workflow, 
                           WorkflowExecution execution, 
                           WorkflowDefinition.WorkflowNode node, 
                           Map<String, Object> inputData) throws WorkflowExecutionException {
        
        // 检查执行状态
        if (execution.getStatus() != WorkflowExecution.ExecutionStatus.RUNNING) {
            return;
        }
        
        // 检查节点是否启用
        if (!node.isEnabled()) {
            execution.skipNodeExecution(node.getNodeId(), "节点已禁用");
            executeNextNodes(workflow, execution, node, inputData);
            return;
        }
        
        try {
            // 开始节点执行
            execution.startNodeExecution(node.getNodeId(), node.getName(), node.getType(), inputData);
            
            // 通知监听器
            notifyListeners(listener -> listener.onNodeStarted(execution, node));
            
            // 获取节点执行器
            NodeExecutor nodeExecutor = getNodeExecutor(node.getType());
            if (nodeExecutor == null) {
                throw new WorkflowExecutionException("不支持的节点类型: " + node.getType());
            }
            
            // 执行节点
            Map<String, Object> outputData = executeNodeWithTimeout(nodeExecutor, workflow, execution, node, inputData);
            
            // 完成节点执行
            execution.completeNodeExecution(node.getNodeId(), outputData);
            
            // 通知监听器
            notifyListeners(listener -> listener.onNodeCompleted(execution, node, outputData));
            
            // 执行下一个节点
            executeNextNodes(workflow, execution, node, outputData);
            
        } catch (Exception e) {
            log.error("节点执行失败: {} - {}", node.getNodeId(), node.getName(), e);
            
            // 记录节点执行失败
            execution.failNodeExecution(node.getNodeId(), e.getMessage(), getStackTrace(e));
            
            // 通知监听器
            notifyListeners(listener -> listener.onNodeFailed(execution, node, e));
            
            // 处理失败策略
            handleNodeFailure(workflow, execution, node, e);
        }
    }
    
    /**
     * 带超时的节点执行
     */
    private Map<String, Object> executeNodeWithTimeout(
            NodeExecutor nodeExecutor,
            WorkflowDefinition workflow,
            WorkflowExecution execution,
            WorkflowDefinition.WorkflowNode node,
            Map<String, Object> inputData) throws Exception {
        
        CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
            try {
                return nodeExecutor.execute(workflow, execution, node, inputData);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, this.nodeExecutor);
        
        try {
            return future.get(node.getTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new WorkflowExecutionException("节点执行超时: " + node.getName());
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                RuntimeException re = (RuntimeException) cause;
                if (re.getCause() instanceof Exception) {
                    throw (Exception) re.getCause();
                }
                throw re;
            }
            throw new WorkflowExecutionException("节点执行异常: " + cause.getMessage(), cause);
        }
    }
    
    /**
     * 执行下一个节点
     */
    private void executeNextNodes(WorkflowDefinition workflow, 
                                WorkflowExecution execution, 
                                WorkflowDefinition.WorkflowNode currentNode, 
                                Map<String, Object> outputData) throws WorkflowExecutionException {
        
        // 获取输出连接
        List<WorkflowDefinition.WorkflowConnection> connections = workflow.getNodeOutputConnections(currentNode.getNodeId());
        
        if (connections.isEmpty()) {
            // 没有后续节点，检查是否为结束节点
            if (currentNode.getType() == WorkflowDefinition.NodeType.END) {
                return; // 正常结束
            } else {
                execution.addWarning("节点 " + currentNode.getName() + " 没有后续连接");
                return;
            }
        }
        
        // 处理不同类型的连接
        List<WorkflowDefinition.WorkflowConnection> sequenceConnections = new ArrayList<>();
        List<WorkflowDefinition.WorkflowConnection> parallelConnections = new ArrayList<>();
        List<WorkflowDefinition.WorkflowConnection> conditionConnections = new ArrayList<>();
        
        for (WorkflowDefinition.WorkflowConnection connection : connections) {
            switch (connection.getType()) {
                case SEQUENCE:
                    sequenceConnections.add(connection);
                    break;
                case PARALLEL:
                    parallelConnections.add(connection);
                    break;
                case CONDITION:
                    conditionConnections.add(connection);
                    break;
            }
        }
        
        // 处理条件连接
        if (!conditionConnections.isEmpty()) {
            executeConditionalNodes(workflow, execution, conditionConnections, outputData);
            return;
        }
        
        // 处理并行连接
        if (!parallelConnections.isEmpty()) {
            executeParallelNodes(workflow, execution, parallelConnections, outputData);
            return;
        }
        
        // 处理顺序连接
        if (!sequenceConnections.isEmpty()) {
            executeSequentialNodes(workflow, execution, sequenceConnections, outputData);
        }
    }
    
    /**
     * 执行条件节点
     */
    private void executeConditionalNodes(WorkflowDefinition workflow, 
                                       WorkflowExecution execution,
                                       List<WorkflowDefinition.WorkflowConnection> connections,
                                       Map<String, Object> outputData) throws WorkflowExecutionException {
        
        for (WorkflowDefinition.WorkflowConnection connection : connections) {
            if (evaluateCondition(connection.getCondition(), execution, outputData)) {
                WorkflowDefinition.WorkflowNode nextNode = workflow.getNodeById(connection.getTargetNodeId());
                if (nextNode != null) {
                    Map<String, Object> mappedData = applyDataMapping(connection.getDataMapping(), outputData);
                    executeNode(workflow, execution, nextNode, mappedData);
                }
                return; // 只执行第一个满足条件的分支
            }
        }
        
        execution.addWarning("没有满足条件的分支");
    }
    
    /**
     * 执行并行节点
     */
    private void executeParallelNodes(WorkflowDefinition workflow, 
                                     WorkflowExecution execution,
                                     List<WorkflowDefinition.WorkflowConnection> connections,
                                     Map<String, Object> outputData) throws WorkflowExecutionException {
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (WorkflowDefinition.WorkflowConnection connection : connections) {
            WorkflowDefinition.WorkflowNode nextNode = workflow.getNodeById(connection.getTargetNodeId());
            if (nextNode != null) {
                Map<String, Object> mappedData = applyDataMapping(connection.getDataMapping(), outputData);
                
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        executeNode(workflow, execution, nextNode, mappedData);
                    } catch (WorkflowExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }, this.nodeExecutor);
                
                futures.add(future);
            }
        }
        
        // 等待所有并行任务完成
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        } catch (Exception e) {
            throw new WorkflowExecutionException("并行节点执行失败", e);
        }
    }
    
    /**
     * 执行顺序节点
     */
    private void executeSequentialNodes(WorkflowDefinition workflow, 
                                      WorkflowExecution execution,
                                      List<WorkflowDefinition.WorkflowConnection> connections,
                                      Map<String, Object> outputData) throws WorkflowExecutionException {
        
        for (WorkflowDefinition.WorkflowConnection connection : connections) {
            WorkflowDefinition.WorkflowNode nextNode = workflow.getNodeById(connection.getTargetNodeId());
            if (nextNode != null) {
                Map<String, Object> mappedData = applyDataMapping(connection.getDataMapping(), outputData);
                executeNode(workflow, execution, nextNode, mappedData);
            }
        }
    }
    
    /**
     * 处理节点失败
     */
    private void handleNodeFailure(WorkflowDefinition workflow, 
                                 WorkflowExecution execution, 
                                 WorkflowDefinition.WorkflowNode node, 
                                 Exception error) throws WorkflowExecutionException {
        
        WorkflowDefinition.ExecutionConfig config = workflow.getExecutionConfig();
        if (config == null) {
            throw new WorkflowExecutionException("节点执行失败: " + node.getName(), error);
        }
        
        switch (config.getFailureStrategy()) {
            case STOP:
                throw new WorkflowExecutionException("节点执行失败，停止工作流: " + node.getName(), error);
                
            case CONTINUE:
                execution.addWarning("节点执行失败，继续执行: " + node.getName() + ", 错误: " + error.getMessage());
                executeNextNodes(workflow, execution, node, new HashMap<>());
                break;
                
            case RETRY:
                retryNodeExecution(workflow, execution, node, error);
                break;
                
            case SKIP:
                execution.skipNodeExecution(node.getNodeId(), "节点执行失败，跳过: " + error.getMessage());
                executeNextNodes(workflow, execution, node, new HashMap<>());
                break;
        }
    }
    
    /**
     * 重试节点执行
     */
    private void retryNodeExecution(WorkflowDefinition workflow, 
                                  WorkflowExecution execution, 
                                  WorkflowDefinition.WorkflowNode node, 
                                  Exception lastError) throws WorkflowExecutionException {
        
        WorkflowExecution.NodeExecution nodeExecution = execution.getNodeExecutions().get(node.getNodeId());
        if (nodeExecution == null) {
            throw new WorkflowExecutionException("节点执行记录不存在: " + node.getNodeId());
        }
        
        if (nodeExecution.getRetryCount() >= node.getRetryCount()) {
            throw new WorkflowExecutionException(
                "节点重试次数已达上限: " + node.getName() + ", 最后错误: " + lastError.getMessage(), lastError);
        }
        
        // 增加重试次数
        nodeExecution.setRetryCount(nodeExecution.getRetryCount() + 1);
        
        // 延迟重试
        try {
            Thread.sleep(1000 * nodeExecution.getRetryCount()); // 简单的线性退避
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WorkflowExecutionException("重试被中断", e);
        }
        
        // 重新执行节点
        executeNode(workflow, execution, node, nodeExecution.getInputData());
    }
    
    /**
     * 评估条件表达式
     */
    private boolean evaluateCondition(String condition, WorkflowExecution execution, Map<String, Object> data) {
        if (condition == null || condition.trim().isEmpty()) {
            return true;
        }
        
        // 简单的条件评估实现
        // 实际项目中可以使用更复杂的表达式引擎，如SpEL、MVEL等
        try {
            // 替换变量
            String evaluatedCondition = condition;
            
            // 替换上下文变量
            for (Map.Entry<String, Object> entry : execution.getExecutionContext().entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                if (evaluatedCondition.contains(placeholder)) {
                    evaluatedCondition = evaluatedCondition.replace(placeholder, String.valueOf(entry.getValue()));
                }
            }
            
            // 替换数据变量
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                if (evaluatedCondition.contains(placeholder)) {
                    evaluatedCondition = evaluatedCondition.replace(placeholder, String.valueOf(entry.getValue()));
                }
            }
            
            // 简单的布尔表达式评估
            return Boolean.parseBoolean(evaluatedCondition);
            
        } catch (Exception e) {
            log.warn("条件表达式评估失败: {}, 默认返回false", condition, e);
            return false;
        }
    }
    
    /**
     * 应用数据映射
     */
    private Map<String, Object> applyDataMapping(Map<String, String> dataMapping, Map<String, Object> sourceData) {
        if (dataMapping == null || dataMapping.isEmpty()) {
            return sourceData;
        }
        
        Map<String, Object> mappedData = new HashMap<>();
        
        for (Map.Entry<String, String> mapping : dataMapping.entrySet()) {
            String targetKey = mapping.getKey();
            String sourceKey = mapping.getValue();
            
            if (sourceData.containsKey(sourceKey)) {
                mappedData.put(targetKey, sourceData.get(sourceKey));
            }
        }
        
        return mappedData;
    }
    
    /**
     * 注册节点执行器
     */
    private void registerNodeExecutors() {
        nodeExecutors.put(WorkflowDefinition.NodeType.START, new StartNodeExecutor());
        nodeExecutors.put(WorkflowDefinition.NodeType.END, new EndNodeExecutor());
        nodeExecutors.put(WorkflowDefinition.NodeType.AGENT, new AgentNodeExecutor());
        nodeExecutors.put(WorkflowDefinition.NodeType.TOOL, new ToolNodeExecutor());
        nodeExecutors.put(WorkflowDefinition.NodeType.CONDITION, new ConditionNodeExecutor());
        nodeExecutors.put(WorkflowDefinition.NodeType.SCRIPT, new ScriptNodeExecutor());
        nodeExecutors.put(WorkflowDefinition.NodeType.HTTP, new HttpNodeExecutor());
        nodeExecutors.put(WorkflowDefinition.NodeType.DATABASE, new DatabaseNodeExecutor());
        nodeExecutors.put(WorkflowDefinition.NodeType.DELAY, new DelayNodeExecutor());
        nodeExecutors.put(WorkflowDefinition.NodeType.NOTIFICATION, new NotificationNodeExecutor());
    }
    
    /**
     * 获取节点执行器
     */
    private NodeExecutor getNodeExecutor(WorkflowDefinition.NodeType nodeType) {
        return nodeExecutors.get(nodeType);
    }
    
    /**
     * 启动监控任务
     */
    private void startMonitoringTasks() {
        // 定期清理已完成的执行实例
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupCompletedExecutions();
            } catch (Exception e) {
                log.error("清理已完成执行实例失败", e);
            }
        }, 5, 5, TimeUnit.MINUTES);
        
        // 定期检查超时的执行实例
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkTimeoutExecutions();
            } catch (Exception e) {
                log.error("检查超时执行实例失败", e);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
    
    /**
     * 清理已完成的执行实例
     */
    private void cleanupCompletedExecutions() {
        List<String> completedExecutions = runningExecutions.entrySet().stream()
            .filter(entry -> entry.getValue().isCompleted())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        for (String executionId : completedExecutions) {
            runningExecutions.remove(executionId);
        }
        
        if (!completedExecutions.isEmpty()) {
            log.debug("清理了 {} 个已完成的执行实例", completedExecutions.size());
        }
    }
    
    /**
     * 检查超时的执行实例
     */
    private void checkTimeoutExecutions() {
        LocalDateTime now = LocalDateTime.now();
        
        for (WorkflowExecution execution : runningExecutions.values()) {
            if (execution.getStatus() == WorkflowExecution.ExecutionStatus.RUNNING) {
                long executionTime = java.time.Duration.between(execution.getStartTime(), now).toMillis();
                
                // 检查是否超时（这里需要从工作流定义中获取超时配置）
                // 暂时使用默认超时时间 1 小时
                if (executionTime > 3600000) {
                    execution.fail("执行超时", null);
                    notifyListeners(listener -> listener.onExecutionTimeout(execution));
                }
            }
        }
    }
    
    /**
     * 通知监听器
     */
    private void notifyListeners(java.util.function.Consumer<WorkflowExecutionListener> action) {
        for (WorkflowExecutionListener listener : listeners) {
            try {
                action.accept(listener);
            } catch (Exception e) {
                log.error("通知工作流监听器失败", e);
            }
        }
    }
    
    /**
     * 获取异常堆栈信息
     */
    private String getStackTrace(Throwable throwable) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
    
    // ==================== 公共API ====================
    
    /**
     * 取消工作流执行
     */
    public boolean cancelExecution(String executionId) {
        WorkflowExecution execution = runningExecutions.get(executionId);
        if (execution != null && !execution.isCompleted()) {
            execution.cancel();
            notifyListeners(listener -> listener.onExecutionCancelled(execution));
            return true;
        }
        return false;
    }
    
    /**
     * 暂停工作流执行
     */
    public boolean pauseExecution(String executionId) {
        WorkflowExecution execution = runningExecutions.get(executionId);
        if (execution != null && execution.getStatus() == WorkflowExecution.ExecutionStatus.RUNNING) {
            execution.pause();
            notifyListeners(listener -> listener.onExecutionPaused(execution));
            return true;
        }
        return false;
    }
    
    /**
     * 恢复工作流执行
     */
    public boolean resumeExecution(String executionId) {
        WorkflowExecution execution = runningExecutions.get(executionId);
        if (execution != null && execution.getStatus() == WorkflowExecution.ExecutionStatus.PAUSED) {
            execution.resume();
            notifyListeners(listener -> listener.onExecutionResumed(execution));
            return true;
        }
        return false;
    }
    
    /**
     * 获取执行状态
     */
    public WorkflowExecution getExecutionStatus(String executionId) {
        return runningExecutions.get(executionId);
    }
    
    /**
     * 获取所有正在执行的工作流
     */
    public List<WorkflowExecution> getRunningExecutions() {
        return new ArrayList<>(runningExecutions.values());
    }
    
    /**
     * 添加执行监听器
     */
    public void addExecutionListener(WorkflowExecutionListener listener) {
        listeners.add(listener);
    }
    
    /**
     * 移除执行监听器
     */
    public void removeExecutionListener(WorkflowExecutionListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * 获取引擎状态
     */
    public EngineStatus getEngineStatus() {
        return EngineStatus.builder()
            .runningExecutions(runningExecutions.size())
            .totalExecutions(runningExecutions.size())
            .workflowThreadPoolSize(((ThreadPoolExecutor) workflowExecutor).getPoolSize())
            .nodeThreadPoolSize(((ThreadPoolExecutor) nodeExecutor).getPoolSize())
            .workflowQueueSize(((ThreadPoolExecutor) workflowExecutor).getQueue().size())
            .nodeQueueSize(((ThreadPoolExecutor) nodeExecutor).getQueue().size())
            .build();
    }
    
    // ==================== 内部类 ====================
    
    /**
     * 引擎状态
     */
    @lombok.Data
    @lombok.Builder
    public static class EngineStatus {
        private int runningExecutions;
        private int totalExecutions;
        private int workflowThreadPoolSize;
        private int nodeThreadPoolSize;
        private int workflowQueueSize;
        private int nodeQueueSize;
    }
    
    /**
     * 工作流执行异常
     */
    public static class WorkflowExecutionException extends Exception {
        public WorkflowExecutionException(String message) {
            super(message);
        }
        
        public WorkflowExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}