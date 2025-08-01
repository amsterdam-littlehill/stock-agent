package com.jd.genie.agent.workflow.engine;

import com.jd.genie.agent.workflow.model.WorkflowDefinition;
import com.jd.genie.agent.workflow.model.WorkflowExecution;

import java.util.Map;

/**
 * 工作流执行监听器接口
 * 监听工作流执行过程中的各种事件
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
public interface WorkflowExecutionListener {
    
    /**
     * 工作流开始执行
     * 
     * @param execution 执行实例
     */
    default void onExecutionStarted(WorkflowExecution execution) {
        // 默认空实现
    }
    
    /**
     * 工作流执行完成
     * 
     * @param execution 执行实例
     */
    default void onExecutionCompleted(WorkflowExecution execution) {
        // 默认空实现
    }
    
    /**
     * 工作流执行失败
     * 
     * @param execution 执行实例
     * @param error 错误信息
     */
    default void onExecutionFailed(WorkflowExecution execution, Exception error) {
        // 默认空实现
    }
    
    /**
     * 工作流执行被取消
     * 
     * @param execution 执行实例
     */
    default void onExecutionCancelled(WorkflowExecution execution) {
        // 默认空实现
    }
    
    /**
     * 工作流执行暂停
     * 
     * @param execution 执行实例
     */
    default void onExecutionPaused(WorkflowExecution execution) {
        // 默认空实现
    }
    
    /**
     * 工作流执行恢复
     * 
     * @param execution 执行实例
     */
    default void onExecutionResumed(WorkflowExecution execution) {
        // 默认空实现
    }
    
    /**
     * 工作流执行超时
     * 
     * @param execution 执行实例
     */
    default void onExecutionTimeout(WorkflowExecution execution) {
        // 默认空实现
    }
    
    /**
     * 节点开始执行
     * 
     * @param execution 执行实例
     * @param node 节点定义
     */
    default void onNodeStarted(WorkflowExecution execution, WorkflowDefinition.WorkflowNode node) {
        // 默认空实现
    }
    
    /**
     * 节点执行完成
     * 
     * @param execution 执行实例
     * @param node 节点定义
     * @param outputData 输出数据
     */
    default void onNodeCompleted(WorkflowExecution execution, 
                                WorkflowDefinition.WorkflowNode node, 
                                Map<String, Object> outputData) {
        // 默认空实现
    }
    
    /**
     * 节点执行失败
     * 
     * @param execution 执行实例
     * @param node 节点定义
     * @param error 错误信息
     */
    default void onNodeFailed(WorkflowExecution execution, 
                              WorkflowDefinition.WorkflowNode node, 
                              Exception error) {
        // 默认空实现
    }
    
    /**
     * 节点执行跳过
     * 
     * @param execution 执行实例
     * @param node 节点定义
     * @param reason 跳过原因
     */
    default void onNodeSkipped(WorkflowExecution execution, 
                              WorkflowDefinition.WorkflowNode node, 
                              String reason) {
        // 默认空实现
    }
    
    /**
     * 节点执行重试
     * 
     * @param execution 执行实例
     * @param node 节点定义
     * @param retryCount 重试次数
     * @param lastError 上次错误
     */
    default void onNodeRetry(WorkflowExecution execution, 
                            WorkflowDefinition.WorkflowNode node, 
                            int retryCount, 
                            Exception lastError) {
        // 默认空实现
    }
}