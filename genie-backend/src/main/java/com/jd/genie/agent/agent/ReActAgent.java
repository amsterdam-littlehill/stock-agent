package com.jd.genie.agent.agent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jd.genie.agent.dto.Message;
import com.jd.genie.agent.tool.BaseTool;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ReAct代理 - 基于ReAct模式的智能代理
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public abstract class ReActAgent extends BaseAgent {

    /**
     * 思考过程
     */
    public abstract boolean think();

    /**
     * 执行行动
     */
    public abstract String act();

    /**
     * 执行单个步骤
     */
    @Override
    public String step() {
        boolean shouldAct = think();
        if (!shouldAct) {
            return "Thinking complete - no action needed";
        }
        return act();
    }

    public void generateDigitalEmployee(String task) {
        // 1、参数检查
        if (StringUtils.isEmpty(task)) {
            return;
        }
        try {
            // 2. 构建系统消息（提取为独立方法）
            String formattedPrompt = formatSystemPrompt(task);
            Message userMessage = Message.userMessage(formattedPrompt, null);

            // 3. 调用LLM并处理结果
            CompletableFuture<String> summaryFuture = getLlm().ask(
                    context,
                    Collections.singletonList(userMessage),
                    Collections.emptyList(),
                    false,
                    0.01);

            // 4. 解析响应
            String llmResponse = summaryFuture.get();
            log.info("requestId: {} task:{} generateDigitalEmployee: {}", context.getRequestId(), task, llmResponse);
            JSONObject jsonObject = parseDigitalEmployee(llmResponse);
            if (jsonObject != null) {
                log.info("requestId:{} generateDigitalEmployee: {}", context.getRequestId(), jsonObject);
                context.getToolCollection().updateDigitalEmployee(jsonObject);
                context.getToolCollection().setCurrentTask(task);
                // 更新 availableTools 添加数字员工
                availableTools = context.getToolCollection();
            } else {
                log.error("requestId: {} generateDigitalEmployee failed", context.getRequestId());
            }

        } catch (Exception e) {
            log.error("requestId: {} in generateDigitalEmployee failed,", context.getRequestId(), e);
        }
    }

    // 解析数据员工大模型响应
    private JSONObject parseDigitalEmployee(String response) {
        /**
         * 格式一：
         *      ```json
         *      {
         *          "file_tool": "市场洞察专员"
         *      }
         *      ```
         * 格式二：
         *      {
         *          "file_tool": "市场洞察专员"
         *      }
         */
        if (StringUtils.isBlank(response)) {
            return null;
        }
        String jsonString = response;
        String regex = "```\\s*json([\\d\\D]+?)```";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            String temp = matcher.group(1).trim();
            if (!jsonString.isEmpty()) {
                jsonString = temp;
            }
        }
        try {
            return JSON.parseObject(jsonString);
        } catch (Exception e) {
            log.error("requestId: {} in parseDigitalEmployee error:", context.getRequestId(), e);
            return null;
        }
    }

    // 提取系统提示格式化逻辑
    private String formatSystemPrompt(String task) {
        String digitalEmployeePrompt = getDigitalEmployeePrompt();
        if (digitalEmployeePrompt == null) {
            throw new IllegalStateException("System prompt is not configured");
        }

        StringBuilder toolPrompt = new StringBuilder();
        for (BaseTool tool : context.getToolCollection().getToolMap().values()) {
            toolPrompt.append(String.format("工具名：%s 工具描述：%s\n", tool.getName(), tool.getDescription()));
        }

        // 替换占位符
        return digitalEmployeePrompt
                .replace("{{task}}", task)
                .replace("{{ToolsDesc}}", toolPrompt.toString())
                .replace("{{query}}", context.getQuery());
    }

}