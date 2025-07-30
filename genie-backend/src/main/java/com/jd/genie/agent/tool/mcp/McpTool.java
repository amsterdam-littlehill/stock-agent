package com.jd.genie.agent.tool.mcp;

import com.alibaba.fastjson.JSON;
import com.jd.genie.agent.agent.AgentContext;
import com.jd.genie.agent.tool.BaseTool;
import com.jd.genie.agent.util.OkHttpUtil;
import com.jd.genie.agent.util.SpringContextHolder;
import com.jd.genie.config.GenieConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Data
public class McpTool implements BaseTool {
    private AgentContext agentContext;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpToolRequest {
        private String server_url;
        private String name;
        private Map<String, Object> arguments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpToolResponse {
        private String code;
        private String message;
        private String data;
    }

    @Override
    public String getName() {
        return "mcp_tool";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Map<String, Object> toParams() {
        return null;
    }

    @Override
    public Object execute(Object input) {
        return null;
    }

    public String listTool(String mcpServerUrl) {
        try {
            GenieConfig genieConfig = SpringContextHolder.getApplicationContext().getBean(GenieConfig.class);
            String mcpClientUrl = genieConfig.getMcpClientUrl() + "/v1/tool/list";
            McpToolRequest mcpToolRequest = McpToolRequest.builder()
                    .server_url(mcpServerUrl)
                    .build();
            String response = OkHttpUtil.postJson(mcpClientUrl, JSON.toJSONString(mcpToolRequest), null, 30L);
            log.info("list tool request: {} response: {}", JSON.toJSONString(mcpToolRequest), response);
            return response;
        } catch (Exception e) {
            log.error("{} list tool error", agentContext.getRequestId(), e);
        }
        return "";
    }

    public String callTool(String mcpServerUrl, String toolName, Object input) {
        try {
            GenieConfig genieConfig = SpringContextHolder.getApplicationContext().getBean(GenieConfig.class);
            String mcpClientUrl = genieConfig.getMcpClientUrl() + "/v1/tool/call";
            Map<String, Object> params = (Map<String, Object>) input;
            McpToolRequest mcpToolRequest = McpToolRequest.builder()
                    .name(toolName)
                    .server_url(mcpServerUrl)
                    .arguments(params)
                    .build();
            String response = OkHttpUtil.postJson(mcpClientUrl, JSON.toJSONString(mcpToolRequest), null, 30L);
            log.info("call tool request: {} response: {}", JSON.toJSONString(mcpToolRequest), response);
            return response;
        } catch (Exception e) {
            log.error("{} call tool error ", agentContext.getRequestId(), e);
        }
        return "";
    }
}