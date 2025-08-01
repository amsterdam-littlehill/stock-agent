package com.jd.genie.agent.orchestrator.config;

import com.jd.genie.agent.orchestrator.websocket.AnalysisWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 * 配置股票分析WebSocket端点和处理器
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Autowired
    private AnalysisWebSocketHandler analysisWebSocketHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册股票分析WebSocket端点
        registry.addHandler(analysisWebSocketHandler, "/ws/analysis")
            .setAllowedOrigins("*") // 允许所有来源，生产环境应该限制具体域名
            .withSockJS(); // 启用SockJS支持，提供WebSocket的降级方案
    }
}