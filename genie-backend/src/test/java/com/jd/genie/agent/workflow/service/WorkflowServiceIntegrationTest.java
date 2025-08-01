package com.jd.genie.agent.workflow.service;

import com.jd.genie.agent.workflow.model.*;
import com.jd.genie.agent.workflow.repository.*;
import com.jd.genie.agent.workflow.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工作流服务集成测试
 * 
 * @author Stock Agent Team
 * @version 1.0.0
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:mysql:8.0:///testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "logging.level.org.hibernate.SQL=DEBUG"
})
@Transactional
class WorkflowServiceIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private WorkflowService workflowService;
    
    @Autowired
    private WorkflowDefinitionRepository definitionRepository;
    
    @Autowired
    private WorkflowExecutionRepository executionRepository;
    
    private WorkflowDefinition testWorkflowDefinition;
    private String testUserId;
    
    @BeforeEach
    void setUp() {
        testUserId = "test-user-" + System.currentTimeMillis();
        testWorkflowDefinition = createTestWorkflowDefinition();
    }
    
    @Test
    @DisplayName("测试创建工作流定义")
    void testCreateWorkflowDefinition() {
        // 执行创建
        WorkflowDefinition created = workflowService.createWorkflowDefinition(
            testWorkflowDefinition, testUserId
        );
        
        // 验证结果
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(testWorkflowDefinition.getName(), created.getName());
        assertEquals(testWorkflowDefinition.getDescription(), created.getDescription());
        assertEquals(testUserId, created.getCreatedBy());
        assertNotNull(created.getCreatedAt());
        
        // 验证数据库中的数据
        Optional<WorkflowDefinitionEntity> entityOpt = definitionRepository.findById(created.getId());
        assertTrue(entityOpt.isPresent());
        
        WorkflowDefinitionEntity entity = entityOpt.get();
        assertEquals(created.getName(), entity.getName());
        assertEquals(created.getDescription(), entity.getDescription());
        assertEquals(testUserId, entity.getCreatedBy());
        assertFalse(entity.isDeleted());
    }
    
    @Test
    @DisplayName("测试获取工作流定义")
    void testGetWorkflowDefinition() {
        // 先创建工作流
        WorkflowDefinition created = workflowService.createWorkflowDefinition(
            testWorkflowDefinition, testUserId
        );
        
        // 获取工作流
        Optional<WorkflowDefinition> retrieved = workflowService.getWorkflowDefinition(created.getId());
        
        // 验证结果
        assertTrue(retrieved.isPresent());
        WorkflowDefinition definition = retrieved.get();
        assertEquals(created.getId(), definition.getId());
        assertEquals(created.getName(), definition.getName());
        assertEquals(created.getDescription(), definition.getDescription());
        assertEquals(created.getNodes().size(), definition.getNodes().size());
        assertEquals(created.getConnections().size(), definition.getConnections().size());
    }
    
    @Test
    @DisplayName("测试更新工作流定义")
    void testUpdateWorkflowDefinition() {
        // 先创建工作流
        WorkflowDefinition created = workflowService.createWorkflowDefinition(
            testWorkflowDefinition, testUserId
        );
        
        // 更新工作流
        WorkflowDefinition updated = created.toBuilder()
            .name("更新后的工作流")
            .description("更新后的描述")
            .build();
            
        WorkflowDefinition result = workflowService.updateWorkflowDefinition(
            created.getId(), updated, testUserId
        );
        
        // 验证结果
        assertNotNull(result);
        assertEquals("更新后的工作流", result.getName());
        assertEquals("更新后的描述", result.getDescription());
        assertNotNull(result.getUpdatedAt());
        
        // 验证数据库中的数据
        Optional<WorkflowDefinitionEntity> entityOpt = definitionRepository.findById(created.getId());
        assertTrue(entityOpt.isPresent());
        
        WorkflowDefinitionEntity entity = entityOpt.get();
        assertEquals("更新后的工作流", entity.getName());
        assertEquals("更新后的描述", entity.getDescription());
    }
    
    @Test
    @DisplayName("测试删除工作流定义")
    void testDeleteWorkflowDefinition() {
        // 先创建工作流
        WorkflowDefinition created = workflowService.createWorkflowDefinition(
            testWorkflowDefinition, testUserId
        );
        
        // 删除工作流
        boolean deleted = workflowService.deleteWorkflowDefinition(created.getId(), testUserId);
        
        // 验证结果
        assertTrue(deleted);
        
        // 验证工作流不再可见
        Optional<WorkflowDefinition> retrieved = workflowService.getWorkflowDefinition(created.getId());
        assertFalse(retrieved.isPresent());
        
        // 验证数据库中的软删除标记
        Optional<WorkflowDefinitionEntity> entityOpt = definitionRepository.findByIdIncludingDeleted(created.getId());
        assertTrue(entityOpt.isPresent());
        assertTrue(entityOpt.get().isDeleted());
        assertNotNull(entityOpt.get().getDeletedAt());
    }
    
    @Test
    @DisplayName("测试搜索工作流定义")
    void testSearchWorkflowDefinitions() {
        // 创建多个工作流
        WorkflowDefinition workflow1 = testWorkflowDefinition.toBuilder()
            .name("股票分析工作流")
            .category("STOCK_ANALYSIS")
            .build();
            
        WorkflowDefinition workflow2 = testWorkflowDefinition.toBuilder()
            .name("市场监控工作流")
            .category("MARKET_MONITORING")
            .build();
            
        workflowService.createWorkflowDefinition(workflow1, testUserId);
        workflowService.createWorkflowDefinition(workflow2, testUserId);
        
        // 搜索工作流
        var searchResult = workflowService.searchWorkflowDefinitions(
            "股票", null, null, null, null, 0, 10
        );
        
        // 验证结果
        assertNotNull(searchResult);
        assertTrue(searchResult.getTotalElements() >= 1);
        
        boolean found = searchResult.getContent().stream()
            .anyMatch(w -> w.getName().contains("股票"));
        assertTrue(found);
    }
    
    @Test
    @DisplayName("测试执行工作流")
    void testExecuteWorkflow() throws Exception {
        // 先创建工作流
        WorkflowDefinition created = workflowService.createWorkflowDefinition(
            testWorkflowDefinition, testUserId
        );
        
        // 准备输入参数
        Map<String, Object> input = Map.of(
            "symbol", "AAPL",
            "analysisType", "BASIC"
        );
        
        // 执行工作流
        CompletableFuture<WorkflowExecutionResult> future = workflowService.executeWorkflow(
            created.getId(), input, testUserId, ExecutionPriority.NORMAL, 60000L
        );
        
        // 等待执行完成（注意：这里可能需要模拟执行器）
        WorkflowExecutionResult result = future.get(30, TimeUnit.SECONDS);
        
        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getExecutionId());
        assertNotNull(result.getStartTime());
        
        // 验证数据库中的执行记录
        Optional<WorkflowExecutionEntity> executionOpt = executionRepository.findById(result.getExecutionId());
        assertTrue(executionOpt.isPresent());
        
        WorkflowExecutionEntity execution = executionOpt.get();
        assertEquals(created.getId(), execution.getWorkflowId());
        assertEquals(testUserId, execution.getExecutedBy());
        assertNotNull(execution.getStartTime());
    }
    
    @Test
    @DisplayName("测试获取执行历史")
    void testGetExecutionHistory() {
        // 先创建工作流
        WorkflowDefinition created = workflowService.createWorkflowDefinition(
            testWorkflowDefinition, testUserId
        );
        
        // 创建执行记录
        WorkflowExecutionEntity execution = new WorkflowExecutionEntity();
        execution.setWorkflowId(created.getId());
        execution.setExecutedBy(testUserId);
        execution.setStatus(ExecutionStatus.COMPLETED);
        execution.setStartTime(new Date());
        execution.setEndTime(new Date());
        execution.setInput(Map.of("symbol", "AAPL"));
        execution.setOutput(Map.of("result", "analysis complete"));
        
        executionRepository.save(execution);
        
        // 获取执行历史
        var historyPage = workflowService.getExecutionHistory(
            created.getId(), null, null, 0, 10
        );
        
        // 验证结果
        assertNotNull(historyPage);
        assertTrue(historyPage.getTotalElements() >= 1);
        
        WorkflowExecution historyExecution = historyPage.getContent().get(0);
        assertEquals(execution.getId(), historyExecution.getId());
        assertEquals(created.getId(), historyExecution.getWorkflowId());
        assertEquals(testUserId, historyExecution.getExecutedBy());
        assertEquals(ExecutionStatus.COMPLETED, historyExecution.getStatus());
    }
    
    @Test
    @DisplayName("测试获取用户执行历史")
    void testGetUserExecutionHistory() {
        // 先创建工作流
        WorkflowDefinition created = workflowService.createWorkflowDefinition(
            testWorkflowDefinition, testUserId
        );
        
        // 创建执行记录
        WorkflowExecutionEntity execution = new WorkflowExecutionEntity();
        execution.setWorkflowId(created.getId());
        execution.setExecutedBy(testUserId);
        execution.setStatus(ExecutionStatus.COMPLETED);
        execution.setStartTime(new Date());
        execution.setEndTime(new Date());
        
        executionRepository.save(execution);
        
        // 获取用户执行历史
        var userHistoryPage = workflowService.getUserExecutionHistory(
            testUserId, null, null, 0, 10
        );
        
        // 验证结果
        assertNotNull(userHistoryPage);
        assertTrue(userHistoryPage.getTotalElements() >= 1);
        
        WorkflowExecution userExecution = userHistoryPage.getContent().get(0);
        assertEquals(testUserId, userExecution.getExecutedBy());
    }
    
    @Test
    @DisplayName("测试获取工作流统计")
    void testGetWorkflowStats() {
        // 先创建工作流
        WorkflowDefinition created = workflowService.createWorkflowDefinition(
            testWorkflowDefinition, testUserId
        );
        
        // 创建一些执行记录
        for (int i = 0; i < 5; i++) {
            WorkflowExecutionEntity execution = new WorkflowExecutionEntity();
            execution.setWorkflowId(created.getId());
            execution.setExecutedBy(testUserId);
            execution.setStatus(i < 4 ? ExecutionStatus.COMPLETED : ExecutionStatus.FAILED);
            execution.setStartTime(new Date(System.currentTimeMillis() - 1000 * i));
            execution.setEndTime(new Date(System.currentTimeMillis() - 500 * i));
            execution.setDuration(500L * i);
            
            executionRepository.save(execution);
        }
        
        // 获取统计信息
        WorkflowService.WorkflowStats stats = workflowService.getWorkflowStats(created.getId());
        
        // 验证结果
        assertNotNull(stats);
        assertEquals(5, stats.getTotalExecutions());
        assertEquals(4, stats.getSuccessfulExecutions());
        assertEquals(1, stats.getFailedExecutions());
        assertEquals(0.8, stats.getSuccessRate(), 0.01);
        assertTrue(stats.getAverageExecutionTime() > 0);
    }
    
    @Test
    @DisplayName("测试克隆工作流")
    void testCloneWorkflow() {
        // 先创建工作流
        WorkflowDefinition original = workflowService.createWorkflowDefinition(
            testWorkflowDefinition, testUserId
        );
        
        // 克隆工作流
        WorkflowDefinition cloned = workflowService.cloneWorkflow(
            original.getId(), "克隆的工作流", testUserId
        );
        
        // 验证结果
        assertNotNull(cloned);
        assertNotEquals(original.getId(), cloned.getId());
        assertEquals("克隆的工作流", cloned.getName());
        assertEquals(original.getDescription(), cloned.getDescription());
        assertEquals(original.getNodes().size(), cloned.getNodes().size());
        assertEquals(original.getConnections().size(), cloned.getConnections().size());
        assertEquals(testUserId, cloned.getCreatedBy());
        
        // 验证节点ID已重新生成
        Set<String> originalNodeIds = new HashSet<>();
        original.getNodes().forEach(node -> originalNodeIds.add(node.getId()));
        
        Set<String> clonedNodeIds = new HashSet<>();
        cloned.getNodes().forEach(node -> clonedNodeIds.add(node.getId()));
        
        // 节点ID应该不同
        assertTrue(Collections.disjoint(originalNodeIds, clonedNodeIds));
    }
    
    /**
     * 创建测试用的工作流定义
     */
    private WorkflowDefinition createTestWorkflowDefinition() {
        // 创建节点
        WorkflowNode startNode = WorkflowNode.builder()
            .id("start-" + System.currentTimeMillis())
            .type(NodeType.START)
            .name("开始")
            .position(new NodePosition(100, 100))
            .build();
            
        WorkflowNode agentNode = WorkflowNode.builder()
            .id("agent-" + System.currentTimeMillis())
            .type(NodeType.AGENT)
            .name("股票分析")
            .position(new NodePosition(300, 100))
            .config(Map.of(
                "agentType", "STOCK_ANALYST",
                "prompt", "分析指定股票的基本面和技术面"
            ))
            .build();
            
        WorkflowNode endNode = WorkflowNode.builder()
            .id("end-" + System.currentTimeMillis())
            .type(NodeType.END)
            .name("结束")
            .position(new NodePosition(500, 100))
            .build();
        
        // 创建连接
        NodeConnection conn1 = NodeConnection.builder()
            .id("conn1-" + System.currentTimeMillis())
            .sourceNodeId(startNode.getId())
            .targetNodeId(agentNode.getId())
            .build();
            
        NodeConnection conn2 = NodeConnection.builder()
            .id("conn2-" + System.currentTimeMillis())
            .sourceNodeId(agentNode.getId())
            .targetNodeId(endNode.getId())
            .build();
        
        // 创建参数定义
        List<ParameterDefinition> inputParams = Arrays.asList(
            ParameterDefinition.builder()
                .name("symbol")
                .type(ParameterType.STRING)
                .required(true)
                .description("股票代码")
                .build(),
            ParameterDefinition.builder()
                .name("analysisType")
                .type(ParameterType.STRING)
                .required(false)
                .description("分析类型")
                .defaultValue("BASIC")
                .build()
        );
        
        List<ParameterDefinition> outputParams = Arrays.asList(
            ParameterDefinition.builder()
                .name("analysis")
                .type(ParameterType.OBJECT)
                .description("分析结果")
                .build()
        );
        
        // 创建工作流定义
        return WorkflowDefinition.builder()
            .name("测试股票分析工作流")
            .description("用于集成测试的股票分析工作流")
            .category("STOCK_ANALYSIS")
            .isPublic(false)
            .nodes(Arrays.asList(startNode, agentNode, endNode))
            .connections(Arrays.asList(conn1, conn2))
            .inputParameters(inputParams)
            .outputParameters(outputParams)
            .build();
    }
}