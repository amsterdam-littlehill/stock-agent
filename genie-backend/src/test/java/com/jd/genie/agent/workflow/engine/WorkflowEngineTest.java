package com.jd.genie.agent.workflow.engine;

import com.jd.genie.agent.workflow.model.*;
import com.jd.genie.agent.workflow.engine.executors.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 工作流引擎单元测试
 * 
 * @author Stock Agent Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class WorkflowEngineTest {

    private WorkflowEngine workflowEngine;
    
    @Mock
    private WorkflowExecutionListener mockListener;
    
    @Mock
    private StartNodeExecutor mockStartExecutor;
    
    @Mock
    private EndNodeExecutor mockEndExecutor;
    
    @Mock
    private AgentNodeExecutor mockAgentExecutor;
    
    @BeforeEach
    void setUp() {
        workflowEngine = new WorkflowEngine();
        
        // 注册模拟执行器
        workflowEngine.registerExecutor(mockStartExecutor);
        workflowEngine.registerExecutor(mockEndExecutor);
        workflowEngine.registerExecutor(mockAgentExecutor);
        
        // 添加监听器
        workflowEngine.addListener(mockListener);
        
        // 配置模拟执行器
        when(mockStartExecutor.getSupportedNodeType()).thenReturn(NodeType.START);
        when(mockEndExecutor.getSupportedNodeType()).thenReturn(NodeType.END);
        when(mockAgentExecutor.getSupportedNodeType()).thenReturn(NodeType.AGENT);
        
        when(mockStartExecutor.validateNode(any())).thenReturn(true);
        when(mockEndExecutor.validateNode(any())).thenReturn(true);
        when(mockAgentExecutor.validateNode(any())).thenReturn(true);
    }
    
    @Test
    @DisplayName("测试简单工作流执行")
    void testSimpleWorkflowExecution() throws Exception {
        // 准备测试数据
        WorkflowDefinition definition = createSimpleWorkflowDefinition();
        Map<String, Object> input = Map.of("symbol", "AAPL");
        
        // 配置执行器返回值
        when(mockStartExecutor.execute(any(), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(
                NodeExecutionResult.success(Map.of("started", true))
            ));
            
        when(mockAgentExecutor.execute(any(), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(
                NodeExecutionResult.success(Map.of("analysis", "Stock analysis result"))
            ));
            
        when(mockEndExecutor.execute(any(), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(
                NodeExecutionResult.success(Map.of("completed", true))
            ));
        
        // 执行工作流
        CompletableFuture<WorkflowExecutionResult> future = workflowEngine.executeWorkflow(
            definition, input, ExecutionPriority.NORMAL, 60000L
        );
        
        WorkflowExecutionResult result = future.get(10, TimeUnit.SECONDS);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertTrue(result.isSuccess());
        assertNotNull(result.getOutput());
        
        // 验证监听器调用
        verify(mockListener).onExecutionStarted(any());
        verify(mockListener).onExecutionCompleted(any());
        verify(mockListener, never()).onExecutionFailed(any(), any());
    }
    
    @Test
    @DisplayName("测试工作流验证失败")
    void testWorkflowValidationFailure() {
        // 准备无效的工作流定义
        WorkflowDefinition invalidDefinition = WorkflowDefinition.builder()
            .id("invalid-workflow")
            .name("Invalid Workflow")
            .nodes(Collections.emptyList()) // 空节点列表
            .connections(Collections.emptyList())
            .build();
        
        Map<String, Object> input = Map.of("symbol", "AAPL");
        
        // 执行工作流应该抛出异常
        assertThrows(WorkflowValidationException.class, () -> {
            workflowEngine.executeWorkflow(
                invalidDefinition, input, ExecutionPriority.NORMAL, 60000L
            );
        });
    }
    
    @Test
    @DisplayName("测试节点执行失败")
    void testNodeExecutionFailure() throws Exception {
        // 准备测试数据
        WorkflowDefinition definition = createSimpleWorkflowDefinition();
        Map<String, Object> input = Map.of("symbol", "AAPL");
        
        // 配置执行器返回值
        when(mockStartExecutor.execute(any(), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(
                NodeExecutionResult.success(Map.of("started", true))
            ));
            
        // 智能体节点执行失败
        when(mockAgentExecutor.execute(any(), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(
                NodeExecutionResult.failure("Agent execution failed", 
                    new RuntimeException("Mock failure"))
            ));
        
        // 执行工作流
        CompletableFuture<WorkflowExecutionResult> future = workflowEngine.executeWorkflow(
            definition, input, ExecutionPriority.NORMAL, 60000L
        );
        
        WorkflowExecutionResult result = future.get(10, TimeUnit.SECONDS);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(ExecutionStatus.FAILED, result.getStatus());
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
        
        // 验证监听器调用
        verify(mockListener).onExecutionStarted(any());
        verify(mockListener).onExecutionFailed(any(), any());
        verify(mockListener, never()).onExecutionCompleted(any());
    }
    
    @Test
    @DisplayName("测试工作流取消")
    void testWorkflowCancellation() throws Exception {
        // 准备测试数据
        WorkflowDefinition definition = createSimpleWorkflowDefinition();
        Map<String, Object> input = Map.of("symbol", "AAPL");
        
        // 配置执行器返回值（模拟长时间运行）
        CompletableFuture<NodeExecutionResult> longRunningTask = new CompletableFuture<>();
        
        when(mockStartExecutor.execute(any(), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(
                NodeExecutionResult.success(Map.of("started", true))
            ));
            
        when(mockAgentExecutor.execute(any(), any(), any()))
            .thenReturn(longRunningTask);
        
        // 执行工作流
        CompletableFuture<WorkflowExecutionResult> future = workflowEngine.executeWorkflow(
            definition, input, ExecutionPriority.NORMAL, 60000L
        );
        
        // 等待一段时间后取消
        Thread.sleep(100);
        String executionId = workflowEngine.getRunningExecutions().keySet().iterator().next();
        boolean cancelled = workflowEngine.cancelExecution(executionId);
        
        assertTrue(cancelled);
        
        // 验证执行被取消
        WorkflowExecutionResult result = future.get(5, TimeUnit.SECONDS);
        assertEquals(ExecutionStatus.CANCELLED, result.getStatus());
        
        // 验证监听器调用
        verify(mockListener).onExecutionStarted(any());
        verify(mockListener).onExecutionCancelled(any());
    }
    
    @Test
    @DisplayName("测试工作流暂停和恢复")
    void testWorkflowPauseAndResume() throws Exception {
        // 准备测试数据
        WorkflowDefinition definition = createSimpleWorkflowDefinition();
        Map<String, Object> input = Map.of("symbol", "AAPL");
        
        // 配置执行器返回值
        CompletableFuture<NodeExecutionResult> pausableTask = new CompletableFuture<>();
        
        when(mockStartExecutor.execute(any(), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(
                NodeExecutionResult.success(Map.of("started", true))
            ));
            
        when(mockAgentExecutor.execute(any(), any(), any()))
            .thenReturn(pausableTask);
        
        // 执行工作流
        CompletableFuture<WorkflowExecutionResult> future = workflowEngine.executeWorkflow(
            definition, input, ExecutionPriority.NORMAL, 60000L
        );
        
        // 等待一段时间后暂停
        Thread.sleep(100);
        String executionId = workflowEngine.getRunningExecutions().keySet().iterator().next();
        boolean paused = workflowEngine.pauseExecution(executionId);
        
        assertTrue(paused);
        
        // 验证执行状态
        WorkflowExecution execution = workflowEngine.getRunningExecutions().get(executionId);
        assertEquals(ExecutionStatus.PAUSED, execution.getStatus());
        
        // 恢复执行
        boolean resumed = workflowEngine.resumeExecution(executionId);
        assertTrue(resumed);
        
        // 完成任务
        pausableTask.complete(NodeExecutionResult.success(Map.of("analysis", "result")));
        
        when(mockEndExecutor.execute(any(), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(
                NodeExecutionResult.success(Map.of("completed", true))
            ));
        
        WorkflowExecutionResult result = future.get(5, TimeUnit.SECONDS);
        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        
        // 验证监听器调用
        verify(mockListener).onExecutionStarted(any());
        verify(mockListener).onExecutionPaused(any());
        verify(mockListener).onExecutionResumed(any());
        verify(mockListener).onExecutionCompleted(any());
    }
    
    @Test
    @DisplayName("测试并发执行限制")
    void testConcurrentExecutionLimit() {
        // 设置最大并发数为2
        workflowEngine.setMaxConcurrentExecutions(2);
        
        WorkflowDefinition definition = createSimpleWorkflowDefinition();
        Map<String, Object> input = Map.of("symbol", "AAPL");
        
        // 配置长时间运行的任务
        CompletableFuture<NodeExecutionResult> longRunningTask = new CompletableFuture<>();
        
        when(mockStartExecutor.execute(any(), any(), any()))
            .thenReturn(longRunningTask);
        
        // 启动3个并发执行
        CompletableFuture<WorkflowExecutionResult> future1 = workflowEngine.executeWorkflow(
            definition, input, ExecutionPriority.NORMAL, 60000L
        );
        
        CompletableFuture<WorkflowExecutionResult> future2 = workflowEngine.executeWorkflow(
            definition, input, ExecutionPriority.NORMAL, 60000L
        );
        
        // 第三个应该被拒绝
        assertThrows(WorkflowExecutionException.class, () -> {
            workflowEngine.executeWorkflow(
                definition, input, ExecutionPriority.NORMAL, 60000L
            );
        });
        
        // 验证只有2个执行在运行
        assertEquals(2, workflowEngine.getRunningExecutions().size());
    }
    
    @Test
    @DisplayName("测试执行超时")
    void testExecutionTimeout() throws Exception {
        // 准备测试数据
        WorkflowDefinition definition = createSimpleWorkflowDefinition();
        Map<String, Object> input = Map.of("symbol", "AAPL");
        
        // 配置永不完成的任务
        CompletableFuture<NodeExecutionResult> neverCompleteTask = new CompletableFuture<>();
        
        when(mockStartExecutor.execute(any(), any(), any()))
            .thenReturn(neverCompleteTask);
        
        // 执行工作流，设置很短的超时时间
        CompletableFuture<WorkflowExecutionResult> future = workflowEngine.executeWorkflow(
            definition, input, ExecutionPriority.NORMAL, 100L // 100ms超时
        );
        
        WorkflowExecutionResult result = future.get(5, TimeUnit.SECONDS);
        
        // 验证超时
        assertEquals(ExecutionStatus.TIMEOUT, result.getStatus());
        assertFalse(result.isSuccess());
        
        // 验证监听器调用
        verify(mockListener).onExecutionStarted(any());
        verify(mockListener).onExecutionTimeout(any());
    }
    
    /**
     * 创建简单的工作流定义
     */
    private WorkflowDefinition createSimpleWorkflowDefinition() {
        // 创建节点
        WorkflowNode startNode = WorkflowNode.builder()
            .id("start")
            .type(NodeType.START)
            .name("开始")
            .position(new NodePosition(100, 100))
            .build();
            
        WorkflowNode agentNode = WorkflowNode.builder()
            .id("agent")
            .type(NodeType.AGENT)
            .name("股票分析")
            .position(new NodePosition(300, 100))
            .config(Map.of(
                "agentType", "STOCK_ANALYST",
                "prompt", "分析指定股票"
            ))
            .build();
            
        WorkflowNode endNode = WorkflowNode.builder()
            .id("end")
            .type(NodeType.END)
            .name("结束")
            .position(new NodePosition(500, 100))
            .build();
        
        // 创建连接
        NodeConnection conn1 = NodeConnection.builder()
            .id("conn1")
            .sourceNodeId("start")
            .targetNodeId("agent")
            .build();
            
        NodeConnection conn2 = NodeConnection.builder()
            .id("conn2")
            .sourceNodeId("agent")
            .targetNodeId("end")
            .build();
        
        // 创建工作流定义
        return WorkflowDefinition.builder()
            .id("test-workflow")
            .name("测试工作流")
            .description("用于单元测试的简单工作流")
            .nodes(Arrays.asList(startNode, agentNode, endNode))
            .connections(Arrays.asList(conn1, conn2))
            .inputParameters(Arrays.asList(
                ParameterDefinition.builder()
                    .name("symbol")
                    .type(ParameterType.STRING)
                    .required(true)
                    .description("股票代码")
                    .build()
            ))
            .outputParameters(Arrays.asList(
                ParameterDefinition.builder()
                    .name("analysis")
                    .type(ParameterType.OBJECT)
                    .description("分析结果")
                    .build()
            ))
            .build();
    }
}