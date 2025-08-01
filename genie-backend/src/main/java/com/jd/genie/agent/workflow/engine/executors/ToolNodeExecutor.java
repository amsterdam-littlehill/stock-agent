package com.jd.genie.agent.workflow.engine.executors;

import com.jd.genie.agent.dto.tool.ToolResult;
import com.jd.genie.agent.mcp.McpIntegrationService;
import com.jd.genie.agent.workflow.engine.NodeExecutor;
import com.jd.genie.agent.workflow.model.WorkflowDefinition;
import com.jd.genie.agent.workflow.model.WorkflowExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 工具节点执行器
 * 在工作流中调用MCP工具
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component
public class ToolNodeExecutor implements NodeExecutor {
    
    @Autowired
    private McpIntegrationService mcpIntegrationService;
    
    @Override
    public Map<String, Object> execute(WorkflowDefinition workflow,
                                      WorkflowExecution execution,
                                      WorkflowDefinition.WorkflowNode node,
                                      Map<String, Object> inputData) throws Exception {
        
        log.info("执行工具节点: {} - {}", node.getNodeId(), node.getName());
        
        // 获取节点配置
        WorkflowDefinition.NodeConfig config = node.getConfig();
        if (config == null) {
            throw new IllegalArgumentException("工具节点缺少配置信息");
        }
        
        String toolName = config.getToolName();
        if (toolName == null || toolName.trim().isEmpty()) {
            throw new IllegalArgumentException("工具节点缺少toolName配置");
        }
        
        // 准备工具调用参数
        Map<String, Object> toolParameters = prepareToolParameters(inputData, config);
        
        // 执行工具调用
        ToolResult toolResult = executeToolCall(toolName, toolParameters, node.getTimeoutSeconds());
        
        // 处理工具结果
        Map<String, Object> outputData = processToolResult(toolResult, inputData, config);
        
        log.info("工具节点执行完成: {} - {}, 工具: {}, 结果状态: {}", 
                node.getNodeId(), node.getName(), toolName, toolResult.isSuccess());
        
        return outputData;
    }
    
    /**
     * 准备工具调用参数
     */
    private Map<String, Object> prepareToolParameters(Map<String, Object> inputData, 
                                                     WorkflowDefinition.NodeConfig config) {
        
        Map<String, Object> parameters = new HashMap<>();
        
        // 首先添加节点配置中的输入参数
        if (config.getInputParameters() != null) {
            parameters.putAll(config.getInputParameters());
        }
        
        // 然后添加输入数据（可能会覆盖配置参数）
        if (inputData != null) {
            // 过滤掉一些内部字段
            for (Map.Entry<String, Object> entry : inputData.entrySet()) {
                String key = entry.getKey();
                if (!isInternalField(key)) {
                    parameters.put(key, entry.getValue());
                }
            }
        }
        
        // 处理参数值中的变量替换
        parameters = resolveParameterVariables(parameters, inputData);
        
        log.debug("工具调用参数: {}", parameters);
        
        return parameters;
    }
    
    /**
     * 判断是否为内部字段
     */
    private boolean isInternalField(String fieldName) {
        Set<String> internalFields = Set.of(
            "nodeType", "timestamp", "executionId", "workflowCompleted",
            "analysisResult", "analysisStatus", "analysisTimestamp"
        );
        return internalFields.contains(fieldName);
    }
    
    /**
     * 解析参数中的变量
     */
    private Map<String, Object> resolveParameterVariables(Map<String, Object> parameters, 
                                                         Map<String, Object> inputData) {
        
        Map<String, Object> resolvedParameters = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof String) {
                String stringValue = (String) value;
                // 处理变量替换，格式: ${variableName}
                String resolvedValue = resolveVariables(stringValue, inputData);
                resolvedParameters.put(key, resolvedValue);
            } else {
                resolvedParameters.put(key, value);
            }
        }
        
        return resolvedParameters;
    }
    
    /**
     * 解析字符串中的变量
     */
    private String resolveVariables(String template, Map<String, Object> variables) {
        if (template == null || !template.contains("${")) {
            return template;
        }
        
        String result = template;
        
        // 简单的变量替换实现
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            if (result.contains(placeholder)) {
                String replacement = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace(placeholder, replacement);
            }
        }
        
        return result;
    }
    
    /**
     * 执行工具调用
     */
    private ToolResult executeToolCall(String toolName, Map<String, Object> parameters, int timeoutSeconds) 
            throws Exception {
        
        try {
            // 检查工具是否存在
            if (!mcpIntegrationService.isToolRegistered(toolName)) {
                throw new RuntimeException("工具未注册: " + toolName);
            }
            
            // 执行工具调用
            CompletableFuture<ToolResult> future = mcpIntegrationService.callToolWithTimeout(
                toolName, parameters, timeoutSeconds);
            
            // 等待结果
            ToolResult result = future.get();
            
            if (result == null) {
                throw new RuntimeException("工具调用返回空结果");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("工具调用失败: {}", toolName, e);
            
            // 创建失败结果
            ToolResult errorResult = new ToolResult();
            errorResult.setSuccess(false);
            errorResult.setErrorMessage("工具调用失败: " + e.getMessage());
            errorResult.setData(new HashMap<>());
            
            return errorResult;
        }
    }
    
    /**
     * 处理工具结果
     */
    private Map<String, Object> processToolResult(ToolResult toolResult, 
                                                 Map<String, Object> inputData,
                                                 WorkflowDefinition.NodeConfig config) {
        
        Map<String, Object> outputData = new HashMap<>(inputData);
        
        // 添加工具执行结果
        outputData.put("toolResult", toolResult);
        outputData.put("toolSuccess", toolResult.isSuccess());
        outputData.put("toolExecutionTime", toolResult.getExecutionTime());
        
        if (toolResult.isSuccess()) {
            // 成功情况：添加工具返回的数据
            if (toolResult.getData() != null) {
                // 直接将工具数据添加到输出中
                outputData.put("toolData", toolResult.getData());
                
                // 如果工具数据是Map，可以将其内容展开到输出数据中
                if (toolResult.getData() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> toolDataMap = (Map<String, Object>) toolResult.getData();
                    
                    // 添加前缀以避免键冲突
                    for (Map.Entry<String, Object> entry : toolDataMap.entrySet()) {
                        outputData.put("tool_" + entry.getKey(), entry.getValue());
                    }
                }
            }
            
            // 添加工具元数据
            if (toolResult.getMetadata() != null) {
                outputData.put("toolMetadata", toolResult.getMetadata());
            }
            
        } else {
            // 失败情况：添加错误信息
            outputData.put("toolError", toolResult.getErrorMessage());
            outputData.put("toolErrorCode", toolResult.getErrorCode());
            
            log.warn("工具执行失败: {}, 错误: {}", config.getToolName(), toolResult.getErrorMessage());
        }
        
        // 应用输出映射
        if (config.getOutputMapping() != null) {
            Map<String, Object> mappedOutput = new HashMap<>();
            
            for (Map.Entry<String, String> mapping : config.getOutputMapping().entrySet()) {
                String targetKey = mapping.getKey();
                String sourceKey = mapping.getValue();
                
                Object sourceValue = getNestedValue(outputData, sourceKey);
                if (sourceValue != null) {
                    mappedOutput.put(targetKey, sourceValue);
                }
            }
            
            // 合并映射结果
            outputData.putAll(mappedOutput);
        }
        
        // 添加节点执行元数据
        outputData.put("nodeType", "TOOL");
        outputData.put("toolName", config.getToolName());
        outputData.put("timestamp", System.currentTimeMillis());
        
        return outputData;
    }
    
    /**
     * 获取嵌套值（支持点号分隔的路径）
     */
    private Object getNestedValue(Map<String, Object> data, String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        
        String[] parts = path.split("\\.");
        Object current = data;
        
        for (String part : parts) {
            if (current instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> currentMap = (Map<String, Object>) current;
                current = currentMap.get(part);
            } else {
                return null;
            }
        }
        
        return current;
    }
    
    @Override
    public List<String> validateNode(WorkflowDefinition.WorkflowNode node) {
        List<String> errors = new ArrayList<>();
        
        if (node.getConfig() == null) {
            errors.add("工具节点缺少配置信息");
            return errors;
        }
        
        WorkflowDefinition.NodeConfig config = node.getConfig();
        
        if (config.getToolName() == null || config.getToolName().trim().isEmpty()) {
            errors.add("工具节点缺少toolName配置");
        } else {
            // 验证工具是否存在（这里可以添加更严格的验证）
            String toolName = config.getToolName();
            if (mcpIntegrationService != null && !mcpIntegrationService.isToolRegistered(toolName)) {
                errors.add("工具未注册: " + toolName);
            }
        }
        
        // 验证输入参数格式
        if (config.getInputParameters() != null) {
            for (Map.Entry<String, Object> entry : config.getInputParameters().entrySet()) {
                if (entry.getKey() == null || entry.getKey().trim().isEmpty()) {
                    errors.add("工具节点输入参数包含空键名");
                }
            }
        }
        
        // 验证输出映射格式
        if (config.getOutputMapping() != null) {
            for (Map.Entry<String, String> entry : config.getOutputMapping().entrySet()) {
                if (entry.getKey() == null || entry.getKey().trim().isEmpty()) {
                    errors.add("工具节点输出映射包含空目标键名");
                }
                if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
                    errors.add("工具节点输出映射包含空源键名");
                }
            }
        }
        
        return errors;
    }
    
    @Override
    public WorkflowDefinition.NodeType getSupportedNodeType() {
        return WorkflowDefinition.NodeType.TOOL;
    }
}