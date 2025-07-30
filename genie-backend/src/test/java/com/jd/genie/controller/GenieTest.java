package com.jd.genie.controller;

import com.jd.genie.agent.tool.mcp.McpTool;
import com.jd.genie.agent.util.SpringContextHolder;
import com.jd.genie.config.GenieConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@SpringBootTest
public class GenieTest {

    @Test
    public void mcpToolTest() {

        GenieConfig genieConfig = SpringContextHolder.getApplicationContext().getBean(GenieConfig.class);
        log.info("{} {}", genieConfig.getMcpClientUrl(), genieConfig.getMcpServerUrlArr());
        if (genieConfig.getMcpServerUrlArr().length > 0) {
            String mcpServerUrl = genieConfig.getMcpServerUrlArr()[0];

            // time mcp tool
            McpTool tool = new McpTool();
            String listResult = tool.listTool(mcpServerUrl);
            log.info("list tool result {}", listResult);

            Map<String, String> input = new HashMap<>();
            input.put("timezone", "America/New_York");
            String callRsult = tool.callTool(mcpServerUrl, "get_current_time", input);
            log.info("call tool result {}", callRsult);
        }
    }
}