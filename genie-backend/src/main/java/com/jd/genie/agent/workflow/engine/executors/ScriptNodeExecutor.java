package com.jd.genie.agent.workflow.engine.executors;

import com.jd.genie.agent.workflow.engine.NodeExecutor;
import com.jd.genie.agent.workflow.model.WorkflowDefinition;
import com.jd.genie.agent.workflow.model.WorkflowExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;

/**
 * 脚本节点执行器
 * 执行自定义脚本代码
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component
public class ScriptNodeExecutor implements NodeExecutor {
    
    private final ScriptEngineManager scriptEngineManager;
    
    public ScriptNodeExecutor() {
        this.scriptEngineManager = new ScriptEngineManager();
    }
    
    @Override
    public Map<String, Object> execute(WorkflowDefinition workflow,
                                      WorkflowExecution execution,
                                      WorkflowDefinition.WorkflowNode node,
                                      Map<String, Object> inputData) throws Exception {
        
        log.info("执行脚本节点: {} - {}", node.getNodeId(), node.getName());
        
        // 获取节点配置
        WorkflowDefinition.NodeConfig config = node.getConfig();
        if (config == null) {
            throw new IllegalArgumentException("脚本节点缺少配置信息");
        }
        
        String script = config.getScript();
        if (script == null || script.trim().isEmpty()) {
            throw new IllegalArgumentException("脚本节点缺少脚本内容");
        }
        
        String scriptLanguage = config.getScriptLanguage();
        if (scriptLanguage == null || scriptLanguage.trim().isEmpty()) {
            scriptLanguage = "javascript"; // 默认使用JavaScript
        }
        
        // 执行脚本
        Object scriptResult = executeScript(script, scriptLanguage, execution, inputData);
        
        // 处理脚本结果
        Map<String, Object> outputData = processScriptResult(scriptResult, inputData, config);
        
        log.info("脚本节点执行完成: {} - {}, 语言: {}", 
                node.getNodeId(), node.getName(), scriptLanguage);
        
        return outputData;
    }
    
    /**
     * 执行脚本
     */
    private Object executeScript(String script, String scriptLanguage, 
                               WorkflowExecution execution, Map<String, Object> inputData) 
            throws ScriptException {
        
        // 获取脚本引擎
        ScriptEngine engine = getScriptEngine(scriptLanguage);
        if (engine == null) {
            throw new IllegalArgumentException("不支持的脚本语言: " + scriptLanguage);
        }
        
        try {
            // 设置脚本上下文
            setupScriptContext(engine, execution, inputData);
            
            // 执行脚本
            log.debug("执行脚本: {}", script);
            Object result = engine.eval(script);
            
            log.debug("脚本执行结果: {}", result);
            return result;
            
        } catch (ScriptException e) {
            log.error("脚本执行失败: {}", script, e);
            throw new ScriptException("脚本执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取脚本引擎
     */
    private ScriptEngine getScriptEngine(String scriptLanguage) {
        String engineName;
        
        switch (scriptLanguage.toLowerCase()) {
            case "javascript":
            case "js":
                engineName = "javascript";
                break;
            case "groovy":
                engineName = "groovy";
                break;
            case "python":
                engineName = "python";
                break;
            case "ruby":
                engineName = "ruby";
                break;
            default:
                engineName = scriptLanguage;
        }
        
        ScriptEngine engine = scriptEngineManager.getEngineByName(engineName);
        if (engine == null) {
            // 尝试其他可能的引擎名称
            if ("javascript".equals(engineName)) {
                engine = scriptEngineManager.getEngineByName("nashorn");
                if (engine == null) {
                    engine = scriptEngineManager.getEngineByName("rhino");
                }
            }
        }
        
        return engine;
    }
    
    /**
     * 设置脚本上下文
     */
    private void setupScriptContext(ScriptEngine engine, WorkflowExecution execution, 
                                  Map<String, Object> inputData) {
        
        // 添加输入数据到脚本上下文
        for (Map.Entry<String, Object> entry : inputData.entrySet()) {
            engine.put(entry.getKey(), entry.getValue());
        }
        
        // 添加执行上下文
        for (Map.Entry<String, Object> entry : execution.getExecutionContext().entrySet()) {
            engine.put("ctx_" + entry.getKey(), entry.getValue());
        }
        
        // 添加执行实例信息
        engine.put("executionId", execution.getExecutionId());
        engine.put("workflowId", execution.getWorkflowId());
        engine.put("progress", execution.getProgress());
        engine.put("retryCount", execution.getRetryCount());
        
        // 添加工具函数
        engine.put("log", new ScriptLogger());
        engine.put("utils", new ScriptUtils());
        
        // 添加常用的Java类
        engine.put("System", System.class);
        engine.put("Math", Math.class);
        engine.put("Date", new Date());
        engine.put("HashMap", HashMap.class);
        engine.put("ArrayList", ArrayList.class);
    }
    
    /**
     * 处理脚本结果
     */
    private Map<String, Object> processScriptResult(Object scriptResult, 
                                                   Map<String, Object> inputData,
                                                   WorkflowDefinition.NodeConfig config) {
        
        Map<String, Object> outputData = new HashMap<>(inputData);
        
        // 添加脚本执行结果
        outputData.put("scriptResult", scriptResult);
        
        // 如果脚本返回Map，将其内容合并到输出数据中
        if (scriptResult instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> scriptResultMap = (Map<String, Object>) scriptResult;
            
            for (Map.Entry<String, Object> entry : scriptResultMap.entrySet()) {
                outputData.put("script_" + entry.getKey(), entry.getValue());
            }
        }
        
        // 应用输出映射
        if (config.getOutputMapping() != null) {
            Map<String, Object> mappedOutput = new HashMap<>();
            
            for (Map.Entry<String, String> mapping : config.getOutputMapping().entrySet()) {
                String targetKey = mapping.getKey();
                String sourceKey = mapping.getValue();
                
                if (outputData.containsKey(sourceKey)) {
                    mappedOutput.put(targetKey, outputData.get(sourceKey));
                }
            }
            
            // 合并映射结果
            outputData.putAll(mappedOutput);
        }
        
        // 添加节点执行元数据
        outputData.put("nodeType", "SCRIPT");
        outputData.put("scriptLanguage", config.getScriptLanguage());
        outputData.put("timestamp", System.currentTimeMillis());
        
        return outputData;
    }
    
    @Override
    public List<String> validateNode(WorkflowDefinition.WorkflowNode node) {
        List<String> errors = new ArrayList<>();
        
        if (node.getConfig() == null) {
            errors.add("脚本节点缺少配置信息");
            return errors;
        }
        
        WorkflowDefinition.NodeConfig config = node.getConfig();
        
        if (config.getScript() == null || config.getScript().trim().isEmpty()) {
            errors.add("脚本节点缺少脚本内容");
        }
        
        String scriptLanguage = config.getScriptLanguage();
        if (scriptLanguage != null && !scriptLanguage.trim().isEmpty()) {
            ScriptEngine engine = getScriptEngine(scriptLanguage);
            if (engine == null) {
                errors.add("不支持的脚本语言: " + scriptLanguage);
            }
        }
        
        // 简单的脚本语法检查（可以根据需要扩展）
        if (config.getScript() != null) {
            String script = config.getScript().trim();
            if (script.contains("System.exit") || script.contains("Runtime.getRuntime")) {
                errors.add("脚本包含不安全的操作");
            }
        }
        
        return errors;
    }
    
    @Override
    public WorkflowDefinition.NodeType getSupportedNodeType() {
        return WorkflowDefinition.NodeType.SCRIPT;
    }
    
    // ==================== 脚本工具类 ====================
    
    /**
     * 脚本日志工具
     */
    public static class ScriptLogger {
        
        public void info(String message) {
            log.info("[Script] {}", message);
        }
        
        public void warn(String message) {
            log.warn("[Script] {}", message);
        }
        
        public void error(String message) {
            log.error("[Script] {}", message);
        }
        
        public void debug(String message) {
            log.debug("[Script] {}", message);
        }
    }
    
    /**
     * 脚本工具类
     */
    public static class ScriptUtils {
        
        /**
         * 格式化数字
         */
        public String formatNumber(double number, int decimals) {
            return String.format("%%.%df", decimals).formatted(number);
        }
        
        /**
         * 解析JSON字符串
         */
        public Map<String, Object> parseJson(String json) {
            // 简单的JSON解析实现
            // 实际项目中应该使用专业的JSON库
            try {
                Map<String, Object> result = new HashMap<>();
                // 这里可以集成Jackson或其他JSON库
                return result;
            } catch (Exception e) {
                log.error("JSON解析失败: {}", json, e);
                return new HashMap<>();
            }
        }
        
        /**
         * 生成UUID
         */
        public String generateUuid() {
            return UUID.randomUUID().toString();
        }
        
        /**
         * 获取当前时间戳
         */
        public long getCurrentTimestamp() {
            return System.currentTimeMillis();
        }
        
        /**
         * 休眠指定毫秒数
         */
        public void sleep(long milliseconds) {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("脚本休眠被中断");
            }
        }
        
        /**
         * 计算两个数的最大值
         */
        public double max(double a, double b) {
            return Math.max(a, b);
        }
        
        /**
         * 计算两个数的最小值
         */
        public double min(double a, double b) {
            return Math.min(a, b);
        }
        
        /**
         * 四舍五入
         */
        public long round(double value) {
            return Math.round(value);
        }
        
        /**
         * 检查字符串是否为空
         */
        public boolean isEmpty(String str) {
            return str == null || str.trim().isEmpty();
        }
        
        /**
         * 检查字符串是否不为空
         */
        public boolean isNotEmpty(String str) {
            return !isEmpty(str);
        }
    }
}