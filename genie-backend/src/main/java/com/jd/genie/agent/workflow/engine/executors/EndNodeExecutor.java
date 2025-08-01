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
 * 结束节点执行器
 * 处理工作流的结束节点逻辑
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component
public class EndNodeExecutor implements NodeExecutor {
    
    @Override
    public Map<String, Object> execute(WorkflowDefinition workflow,
                                      WorkflowExecution execution,
                                      WorkflowDefinition.WorkflowNode node,
                                      Map<String, Object> inputData) throws Exception {
        
        log.info("工作流执行结束: {} - {}", workflow.getWorkflowId(), workflow.getName());
        
        // 设置最终输出结果
        execution.getOutputResults().putAll(inputData);
        
        // 记录结束时间
        LocalDateTime endTime = LocalDateTime.now();
        execution.setContextVariable("workflowEndTime", endTime);
        
        // 计算总执行时间
        LocalDateTime startTime = execution.getContextVariable("workflowStartTime", LocalDateTime.class);
        if (startTime != null) {
            long duration = java.time.Duration.between(startTime, endTime).toMillis();
            execution.setContextVariable("totalExecutionTime", duration);
        }
        
        // 设置完成进度
        execution.updateProgress(100);
        
        // 准备输出数据
        Map<String, Object> outputData = new HashMap<>(inputData);
        outputData.put("nodeType", "END");
        outputData.put("timestamp", endTime);
        outputData.put("executionId", execution.getExecutionId());
        outputData.put("workflowCompleted", true);
        
        // 添加执行摘要
        Map<String, Object> executionSummary = new HashMap<>();
        executionSummary.put("workflowId", workflow.getWorkflowId());
        executionSummary.put("workflowName", workflow.getName());
        executionSummary.put("executionId", execution.getExecutionId());
        executionSummary.put("startTime", startTime);
        executionSummary.put("endTime", endTime);
        executionSummary.put("totalNodes", execution.getNodeExecutions().size());
        executionSummary.put("successfulNodes", execution.getNodeExecutions().values().stream()
            .filter(ne -> ne.getStatus() == WorkflowExecution.NodeExecutionStatus.COMPLETED)
            .count());
        executionSummary.put("failedNodes", execution.getNodeExecutions().values().stream()
            .filter(ne -> ne.getStatus() == WorkflowExecution.NodeExecutionStatus.FAILED)
            .count());
        
        outputData.put("executionSummary", executionSummary);
        
        log.info("工作流执行完成，执行摘要: {}", executionSummary);
        
        return outputData;
    }
    
    @Override
    public WorkflowDefinition.NodeType getSupportedNodeType() {
        return WorkflowDefinition.NodeType.END;
    }
}