package com.jd.genie.agent.workflow.engine.executors;

import com.jd.genie.agent.workflow.engine.NodeExecutor;
import com.jd.genie.agent.workflow.model.WorkflowDefinition;
import com.jd.genie.agent.workflow.model.WorkflowExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 延迟节点执行器
 * 在工作流中添加延迟等待
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component
public class DelayNodeExecutor implements NodeExecutor {
    
    @Override
    public Map<String, Object> execute(WorkflowDefinition workflow,
                                      WorkflowExecution execution,
                                      WorkflowDefinition.WorkflowNode node,
                                      Map<String, Object> inputData) throws Exception {
        
        log.info("执行延迟节点: {} - {}", node.getNodeId(), node.getName());
        
        // 获取延迟时间
        long delayMillis = getDelayTime(node, inputData);
        
        if (delayMillis <= 0) {
            log.warn("延迟时间无效: {} ms，跳过延迟", delayMillis);
        } else {
            log.info("开始延迟等待: {} ms", delayMillis);
            
            long startTime = System.currentTimeMillis();
            
            try {
                // 执行延迟
                Thread.sleep(delayMillis);
                
                long actualDelay = System.currentTimeMillis() - startTime;
                log.info("延迟等待完成，实际延迟: {} ms", actualDelay);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("延迟等待被中断");
                throw new RuntimeException("延迟等待被中断", e);
            }
        }
        
        // 准备输出数据
        Map<String, Object> outputData = new HashMap<>(inputData);
        outputData.put("delayTime", delayMillis);
        outputData.put("nodeType", "DELAY");
        outputData.put("timestamp", System.currentTimeMillis());
        outputData.put("delayCompleted", true);
        
        log.info("延迟节点执行完成: {} - {}", node.getNodeId(), node.getName());
        
        return outputData;
    }
    
    /**
     * 获取延迟时间
     */
    private long getDelayTime(WorkflowDefinition.WorkflowNode node, Map<String, Object> inputData) {
        
        // 优先从节点配置中获取
        if (node.getConfig() != null && node.getConfig().getInputParameters() != null) {
            Map<String, Object> params = node.getConfig().getInputParameters();
            
            // 检查延迟时间参数
            Object delayParam = params.get("delayTime");
            if (delayParam != null) {
                return parseDelayTime(delayParam);
            }
            
            Object delaySecondsParam = params.get("delaySeconds");
            if (delaySecondsParam != null) {
                return parseDelayTime(delaySecondsParam) * 1000;
            }
            
            Object delayMinutesParam = params.get("delayMinutes");
            if (delayMinutesParam != null) {
                return parseDelayTime(delayMinutesParam) * 60 * 1000;
            }
        }
        
        // 从输入数据中获取
        Object delayParam = inputData.get("delayTime");
        if (delayParam != null) {
            return parseDelayTime(delayParam);
        }
        
        Object delaySecondsParam = inputData.get("delaySeconds");
        if (delaySecondsParam != null) {
            return parseDelayTime(delaySecondsParam) * 1000;
        }
        
        Object delayMinutesParam = inputData.get("delayMinutes");
        if (delayMinutesParam != null) {
            return parseDelayTime(delayMinutesParam) * 60 * 1000;
        }
        
        // 默认延迟时间（1秒）
        return 1000;
    }
    
    /**
     * 解析延迟时间
     */
    private long parseDelayTime(Object delayParam) {
        if (delayParam == null) {
            return 0;
        }
        
        try {
            if (delayParam instanceof Number) {
                return ((Number) delayParam).longValue();
            }
            
            if (delayParam instanceof String) {
                String delayStr = (String) delayParam;
                
                // 支持时间单位格式，如 "5s", "2m", "1h"
                if (delayStr.matches("\\d+[smh]")) {
                    char unit = delayStr.charAt(delayStr.length() - 1);
                    long value = Long.parseLong(delayStr.substring(0, delayStr.length() - 1));
                    
                    switch (unit) {
                        case 's':
                            return value * 1000;
                        case 'm':
                            return value * 60 * 1000;
                        case 'h':
                            return value * 60 * 60 * 1000;
                        default:
                            return value;
                    }
                } else {
                    // 直接解析为数字（毫秒）
                    return Long.parseLong(delayStr);
                }
            }
            
        } catch (NumberFormatException e) {
            log.warn("无法解析延迟时间: {}, 使用默认值", delayParam);
        }
        
        return 0;
    }
    
    @Override
    public List<String> validateNode(WorkflowDefinition.WorkflowNode node) {
        List<String> errors = new ArrayList<>();
        
        // 检查是否有有效的延迟时间配置
        boolean hasValidDelay = false;
        
        if (node.getConfig() != null && node.getConfig().getInputParameters() != null) {
            Map<String, Object> params = node.getConfig().getInputParameters();
            
            if (params.containsKey("delayTime") || 
                params.containsKey("delaySeconds") || 
                params.containsKey("delayMinutes")) {
                hasValidDelay = true;
                
                // 验证延迟时间值
                for (String key : Arrays.asList("delayTime", "delaySeconds", "delayMinutes")) {
                    Object value = params.get(key);
                    if (value != null) {
                        long delayTime = parseDelayTime(value);
                        if (delayTime < 0) {
                            errors.add("延迟时间不能为负数: " + key + " = " + value);
                        }
                        if (delayTime > TimeUnit.HOURS.toMillis(24)) {
                            errors.add("延迟时间过长（超过24小时）: " + key + " = " + value);
                        }
                    }
                }
            }
        }
        
        if (!hasValidDelay) {
            // 这不是错误，只是警告，因为可以从输入数据中获取延迟时间
            log.debug("延迟节点没有配置延迟时间，将从输入数据中获取或使用默认值");
        }
        
        return errors;
    }
    
    @Override
    public WorkflowDefinition.NodeType getSupportedNodeType() {
        return WorkflowDefinition.NodeType.DELAY;
    }
}