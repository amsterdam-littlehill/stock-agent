package com.jd.genie.agent.dto;


import com.jd.genie.agent.dto.tool.ToolCall;
import com.jd.genie.agent.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 消息类 - 表示代理系统中的各种消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private RoleType role;           // 消息角色
    private String content;          // 消息内容
    private String base64Image;      // 图片数据（base64编码）
    private String toolCallId;       // 工具调用ID
    private List<ToolCall> toolCalls; // 工具调用列表

    /**
     * 创建用户消息
     */
    public static Message userMessage(String content, String base64Image) {
        return Message.builder()
                .role(RoleType.USER)
                .content(content)
                .base64Image(base64Image)
                .build();
    }

    /**
     * 创建系统消息
     */
    public static Message systemMessage(String content, String base64Image) {
        return Message.builder()
                .role(RoleType.SYSTEM)
                .content(content)
                .base64Image(base64Image)
                .build();
    }

    /**
     * 创建助手消息
     */
    public static Message assistantMessage(String content, String base64Image) {
        return Message.builder()
                .role(RoleType.ASSISTANT)
                .content(content)
                .base64Image(base64Image)
                .build();
    }

    /**
     * 创建工具消息
     */
    public static Message toolMessage(String content, String toolCallId, String base64Image) {
        return Message.builder()
                .role(RoleType.TOOL)
                .content(content)
                .toolCallId(toolCallId)
                .base64Image(base64Image)
                .build();
    }

    /**
     * 从工具调用创建消息
     */
    public static Message fromToolCalls(String content, List<ToolCall> toolCalls) {
        return Message.builder()
                .role(RoleType.ASSISTANT)
                .content(content)
                .toolCalls(toolCalls)
                .build();
    }
}