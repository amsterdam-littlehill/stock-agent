package com.jd.genie.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * 用于实时推送股票分析进度和结果
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单的消息代理，用于向客户端发送消息
        config.enableSimpleBroker("/topic", "/queue", "/user");
        
        // 设置应用程序目的地前缀
        config.setApplicationDestinationPrefixes("/app");
        
        // 设置用户目的地前缀
        config.setUserDestinationPrefix("/user");
    }

    /**
     * 注册STOMP端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 允许所有来源（生产环境应该限制）
                .withSockJS(); // 启用SockJS支持
        
        // 注册股票分析专用端点
        registry.addEndpoint("/ws/stock-analysis")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}