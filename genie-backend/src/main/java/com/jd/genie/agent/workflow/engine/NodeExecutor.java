package com.jd.genie.agent.workflow.engine;

import com.jd.genie.agent.workflow.model.WorkflowDefinition;
import com.jd.genie.agent.workflow.model.WorkflowExecution;

import java.util.Map;

/**
 * 节点执行器接口
 * 定义工作流节点的执行规范
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
public interface NodeExecutor {
    
    /**
     * 执行节点
     * 
     * @param workflow 工作流定义
     * @param execution 执行实例
     * @param node 当前节点
     * @param inputData 输入数据
     * @return 输出数据
     * @throws Exception 执行异常
     */
    Map<String, Object> execute(WorkflowDefinition workflow,
                               WorkflowExecution execution,
                               WorkflowDefinition.WorkflowNode node,
                               Map<String, Object> inputData) throws Exception;
    
    /**
     * 验证节点配置
     * 
     * @param node 节点定义
     * @return 验证结果，空列表表示验证通过
     */
    default java.util.List<String> validateNode(WorkflowDefinition.WorkflowNode node) {
        return java.util.Collections.emptyList();
    }
    
    /**
     * 获取支持的节点类型
     * 
     * @return 节点类型
     */
    WorkflowDefinition.NodeType getSupportedNodeType();
}