package com.jd.genie.agent.workflow.config;

import com.jd.genie.agent.workflow.websocket.WorkflowWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 工作流WebSocket配置
 * 配置工作流相关的WebSocket端点和处理器
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Configuration
@EnableWebSocket
public class WorkflowWebSocketConfig implements WebSocketConfigurer {
    
    @Autowired
    private WorkflowWebSocketHandler workflowWebSocketHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册工作流WebSocket处理器
        registry.addHandler(workflowWebSocketHandler, "/ws/workflow")
                .setAllowedOrigins("*") // 生产环境中应该配置具体的域名
                .withSockJS(); // 启用SockJS支持
    }
}