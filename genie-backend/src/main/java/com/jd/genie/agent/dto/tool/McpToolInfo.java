package com.jd.genie.agent.dto.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpToolInfo {
    String mcpServerUrl;
    String name;
    String desc;
    String parameters;
}
