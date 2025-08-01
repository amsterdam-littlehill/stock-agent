package com.jd.genie.agent.workflow.engine.executors;

import com.jd.genie.agent.workflow.engine.NodeExecutor;
import com.jd.genie.agent.workflow.model.WorkflowDefinition;
import com.jd.genie.agent.workflow.model.WorkflowExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 开始节点执行器
 * 处理工作流的开始节点逻辑
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component
public class StartNodeExecutor implements NodeExecutor {
    
    @Override
    public Map<String, Object> execute(WorkflowDefinition workflow,
                                      WorkflowExecution execution,
                                      WorkflowDefinition.WorkflowNode node,
                                      Map<String, Object> inputData) throws Exception {
        
        log.info("开始执行工作流: {} - {}", workflow.getWorkflowId(), workflow.getName());
        
        // 初始化执行上下文
        execution.setContextVariable("workflowStartTime", LocalDateTime.now());
        execution.setContextVariable("workflowName", workflow.getName());
        execution.setContextVariable("workflowVersion", workflow.getVersion());
        
        // 记录开始节点的输入参数
        Map<String, Object> outputData = new HashMap<>(inputData);
        outputData.put("nodeType", "START");
        outputData.put("timestamp", LocalDateTime.now());
        outputData.put("executionId", execution.getExecutionId());
        
        // 设置初始进度
        execution.updateProgress(5);
        
        log.debug("开始节点执行完成，输出数据: {}", outputData);
        
        return outputData;
    }
    
    @Override
    public WorkflowDefinition.NodeType getSupportedNodeType() {
        return WorkflowDefinition.NodeType.START;
    }
}