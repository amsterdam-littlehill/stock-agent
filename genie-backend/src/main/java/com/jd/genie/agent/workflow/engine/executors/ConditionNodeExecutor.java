package com.jd.genie.agent.workflow.engine.executors;

import com.jd.genie.agent.workflow.engine.NodeExecutor;
import com.jd.genie.agent.workflow.model.WorkflowDefinition;
import com.jd.genie.agent.workflow.model.WorkflowExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 条件节点执行器
 * 根据条件表达式决定工作流的执行路径
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component
public class ConditionNodeExecutor implements NodeExecutor {
    
    @Override
    public Map<String, Object> execute(WorkflowDefinition workflow,
                                      WorkflowExecution execution,
                                      WorkflowDefinition.WorkflowNode node,
                                      Map<String, Object> inputData) throws Exception {
        
        log.info("执行条件节点: {} - {}", node.getNodeId(), node.getName());
        
        String condition = node.getCondition();
        if (condition == null || condition.trim().isEmpty()) {
            log.warn("条件节点缺少条件表达式，默认返回true");
            condition = "true";
        }
        
        // 评估条件表达式
        boolean conditionResult = evaluateCondition(condition, execution, inputData);
        
        // 准备输出数据
        Map<String, Object> outputData = new HashMap<>(inputData);
        outputData.put("conditionResult", conditionResult);
        outputData.put("conditionExpression", condition);
        outputData.put("nodeType", "CONDITION");
        outputData.put("timestamp", System.currentTimeMillis());
        
        log.info("条件节点执行完成: {} - {}, 条件: {}, 结果: {}", 
                node.getNodeId(), node.getName(), condition, conditionResult);
        
        return outputData;
    }
    
    /**
     * 评估条件表达式
     */
    private boolean evaluateCondition(String condition, WorkflowExecution execution, Map<String, Object> inputData) {
        try {
            // 替换变量
            String evaluatedCondition = resolveVariables(condition, execution, inputData);
            
            log.debug("原始条件: {}, 解析后条件: {}", condition, evaluatedCondition);
            
            // 评估条件表达式
            return evaluateExpression(evaluatedCondition);
            
        } catch (Exception e) {
            log.error("条件表达式评估失败: {}, 默认返回false", condition, e);
            return false;
        }
    }
    
    /**
     * 解析变量
     */
    private String resolveVariables(String template, WorkflowExecution execution, Map<String, Object> inputData) {
        if (template == null || !template.contains("${")) {
            return template;
        }
        
        String result = template;
        
        // 替换执行上下文变量
        for (Map.Entry<String, Object> entry : execution.getExecutionContext().entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            if (result.contains(placeholder)) {
                String replacement = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace(placeholder, replacement);
            }
        }
        
        // 替换输入数据变量
        for (Map.Entry<String, Object> entry : inputData.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            if (result.contains(placeholder)) {
                String replacement = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace(placeholder, replacement);
            }
        }
        
        // 替换特殊变量
        result = result.replace("${executionId}", execution.getExecutionId());
        result = result.replace("${workflowId}", execution.getWorkflowId());
        result = result.replace("${progress}", String.valueOf(execution.getProgress()));
        result = result.replace("${retryCount}", String.valueOf(execution.getRetryCount()));
        
        return result;
    }
    
    /**
     * 评估表达式
     */
    private boolean evaluateExpression(String expression) {
        // 简单的表达式评估实现
        // 实际项目中可以使用更强大的表达式引擎，如SpEL、MVEL等
        
        expression = expression.trim();
        
        // 处理布尔值
        if ("true".equalsIgnoreCase(expression)) {
            return true;
        }
        if ("false".equalsIgnoreCase(expression)) {
            return false;
        }
        
        // 处理数字比较
        if (expression.contains(">") || expression.contains("<") || 
            expression.contains("=") || expression.contains("!=")) {
            return evaluateComparison(expression);
        }
        
        // 处理字符串比较
        if (expression.contains("contains") || expression.contains("equals") || 
            expression.contains("startsWith") || expression.contains("endsWith")) {
            return evaluateStringComparison(expression);
        }
        
        // 处理逻辑运算
        if (expression.contains("&&") || expression.contains("||") || expression.contains("!")) {
            return evaluateLogicalExpression(expression);
        }
        
        // 默认尝试解析为布尔值
        try {
            return Boolean.parseBoolean(expression);
        } catch (Exception e) {
            log.warn("无法解析表达式: {}, 默认返回false", expression);
            return false;
        }
    }
    
    /**
     * 评估比较表达式
     */
    private boolean evaluateComparison(String expression) {
        try {
            // 处理 >= 和 <=
            if (expression.contains(">=")) {
                String[] parts = expression.split(">=");
                if (parts.length == 2) {
                    double left = Double.parseDouble(parts[0].trim());
                    double right = Double.parseDouble(parts[1].trim());
                    return left >= right;
                }
            }
            
            if (expression.contains("<=")) {
                String[] parts = expression.split("<=");
                if (parts.length == 2) {
                    double left = Double.parseDouble(parts[0].trim());
                    double right = Double.parseDouble(parts[1].trim());
                    return left <= right;
                }
            }
            
            // 处理 != 
            if (expression.contains("!=")) {
                String[] parts = expression.split("!=");
                if (parts.length == 2) {
                    String left = parts[0].trim();
                    String right = parts[1].trim();
                    
                    // 尝试数字比较
                    try {
                        double leftNum = Double.parseDouble(left);
                        double rightNum = Double.parseDouble(right);
                        return leftNum != rightNum;
                    } catch (NumberFormatException e) {
                        // 字符串比较
                        return !left.equals(right);
                    }
                }
            }
            
            // 处理 ==
            if (expression.contains("==")) {
                String[] parts = expression.split("==");
                if (parts.length == 2) {
                    String left = parts[0].trim();
                    String right = parts[1].trim();
                    
                    // 尝试数字比较
                    try {
                        double leftNum = Double.parseDouble(left);
                        double rightNum = Double.parseDouble(right);
                        return leftNum == rightNum;
                    } catch (NumberFormatException e) {
                        // 字符串比较
                        return left.equals(right);
                    }
                }
            }
            
            // 处理 >
            if (expression.contains(">") && !expression.contains(">=")) {
                String[] parts = expression.split(">");
                if (parts.length == 2) {
                    double left = Double.parseDouble(parts[0].trim());
                    double right = Double.parseDouble(parts[1].trim());
                    return left > right;
                }
            }
            
            // 处理 <
            if (expression.contains("<") && !expression.contains("<=")) {
                String[] parts = expression.split("<");
                if (parts.length == 2) {
                    double left = Double.parseDouble(parts[0].trim());
                    double right = Double.parseDouble(parts[1].trim());
                    return left < right;
                }
            }
            
        } catch (Exception e) {
            log.warn("比较表达式评估失败: {}", expression, e);
        }
        
        return false;
    }
    
    /**
     * 评估字符串比较表达式
     */
    private boolean evaluateStringComparison(String expression) {
        try {
            if (expression.contains(".contains(")) {
                // 格式: string.contains(substring)
                int dotIndex = expression.indexOf(".contains(");
                int endIndex = expression.lastIndexOf(")");
                
                if (dotIndex > 0 && endIndex > dotIndex) {
                    String str = expression.substring(0, dotIndex).trim();
                    String substring = expression.substring(dotIndex + 10, endIndex).trim();
                    
                    // 移除引号
                    str = removeQuotes(str);
                    substring = removeQuotes(substring);
                    
                    return str.contains(substring);
                }
            }
            
            if (expression.contains(".equals(")) {
                // 格式: string.equals(other)
                int dotIndex = expression.indexOf(".equals(");
                int endIndex = expression.lastIndexOf(")");
                
                if (dotIndex > 0 && endIndex > dotIndex) {
                    String str = expression.substring(0, dotIndex).trim();
                    String other = expression.substring(dotIndex + 8, endIndex).trim();
                    
                    // 移除引号
                    str = removeQuotes(str);
                    other = removeQuotes(other);
                    
                    return str.equals(other);
                }
            }
            
            if (expression.contains(".startsWith(")) {
                // 格式: string.startsWith(prefix)
                int dotIndex = expression.indexOf(".startsWith(");
                int endIndex = expression.lastIndexOf(")");
                
                if (dotIndex > 0 && endIndex > dotIndex) {
                    String str = expression.substring(0, dotIndex).trim();
                    String prefix = expression.substring(dotIndex + 12, endIndex).trim();
                    
                    // 移除引号
                    str = removeQuotes(str);
                    prefix = removeQuotes(prefix);
                    
                    return str.startsWith(prefix);
                }
            }
            
            if (expression.contains(".endsWith(")) {
                // 格式: string.endsWith(suffix)
                int dotIndex = expression.indexOf(".endsWith(");
                int endIndex = expression.lastIndexOf(")");
                
                if (dotIndex > 0 && endIndex > dotIndex) {
                    String str = expression.substring(0, dotIndex).trim();
                    String suffix = expression.substring(dotIndex + 10, endIndex).trim();
                    
                    // 移除引号
                    str = removeQuotes(str);
                    suffix = removeQuotes(suffix);
                    
                    return str.endsWith(suffix);
                }
            }
            
        } catch (Exception e) {
            log.warn("字符串比较表达式评估失败: {}", expression, e);
        }
        
        return false;
    }
    
    /**
     * 评估逻辑表达式
     */
    private boolean evaluateLogicalExpression(String expression) {
        try {
            // 处理 &&
            if (expression.contains("&&")) {
                String[] parts = expression.split("&&");
                for (String part : parts) {
                    if (!evaluateExpression(part.trim())) {
                        return false;
                    }
                }
                return true;
            }
            
            // 处理 ||
            if (expression.contains("||")) {
                String[] parts = expression.split("\\|\\|");
                for (String part : parts) {
                    if (evaluateExpression(part.trim())) {
                        return true;
                    }
                }
                return false;
            }
            
            // 处理 !
            if (expression.startsWith("!")) {
                String innerExpression = expression.substring(1).trim();
                return !evaluateExpression(innerExpression);
            }
            
        } catch (Exception e) {
            log.warn("逻辑表达式评估失败: {}", expression, e);
        }
        
        return false;
    }
    
    /**
     * 移除字符串两端的引号
     */
    private String removeQuotes(String str) {
        if (str == null) {
            return "";
        }
        
        str = str.trim();
        if ((str.startsWith("\"") && str.endsWith("\"")) || 
            (str.startsWith("'") && str.endsWith("'"))) {
            return str.substring(1, str.length() - 1);
        }
        
        return str;
    }
    
    @Override
    public List<String> validateNode(WorkflowDefinition.WorkflowNode node) {
        List<String> errors = new ArrayList<>();
        
        String condition = node.getCondition();
        if (condition == null || condition.trim().isEmpty()) {
            errors.add("条件节点缺少条件表达式");
        } else {
            // 简单的语法检查
            if (!isValidConditionSyntax(condition)) {
                errors.add("条件表达式语法错误: " + condition);
            }
        }
        
        return errors;
    }
    
    /**
     * 检查条件表达式语法
     */
    private boolean isValidConditionSyntax(String condition) {
        try {
            // 简单的语法检查
            condition = condition.trim();
            
            // 检查括号匹配
            int openParens = 0;
            for (char c : condition.toCharArray()) {
                if (c == '(') {
                    openParens++;
                } else if (c == ')') {
                    openParens--;
                    if (openParens < 0) {
                        return false;
                    }
                }
            }
            
            return openParens == 0;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public WorkflowDefinition.NodeType getSupportedNodeType() {
        return WorkflowDefinition.NodeType.CONDITION;
    }
}