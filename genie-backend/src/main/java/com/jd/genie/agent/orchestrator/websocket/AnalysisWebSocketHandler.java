package com.jd.genie.agent.orchestrator.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jd.genie.agent.orchestrator.model.OrchestrationResult;
import com.jd.genie.agent.orchestrator.service.OrchestrationService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 股票分析WebSocket处理器
 * 提供实时分析结果推送和进度更新
 * 
 * 功能：
 * - 实时推送分析结果
 * - 分析进度更新
 * - 客户端连接管理
 * - 消息广播和点对点通信
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component
public class AnalysisWebSocketHandler implements WebSocketHandler {
    
    @Autowired
    private OrchestrationService orchestrationService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 存储所有活跃的WebSocket会话
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    // 存储订阅特定股票的会话
    private final Map<String, List<WebSocketSession>> stockSubscriptions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        
        log.info("WebSocket连接建立: {}, 当前连接数: {}", sessionId, sessions.size());
        
        // 发送连接成功消息
        WebSocketMessage welcomeMessage = WebSocketMessage.builder()
            .type(MessageType.CONNECTION_ESTABLISHED)
            .data(Map.of(
                "sessionId", sessionId,
                "timestamp", System.currentTimeMillis(),
                "message", "WebSocket连接已建立"
            ))
            .build();
        
        sendMessage(session, welcomeMessage);
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            String payload = message.getPayload().toString();
            WebSocketMessage request = objectMapper.readValue(payload, WebSocketMessage.class);
            
            log.debug("收到WebSocket消息: {}, 类型: {}", session.getId(), request.getType());
            
            switch (request.getType()) {
                case SUBSCRIBE_STOCK:
                    handleStockSubscription(session, request);
                    break;
                case UNSUBSCRIBE_STOCK:
                    handleStockUnsubscription(session, request);
                    break;
                case QUICK_ANALYSIS:
                    handleQuickAnalysis(session, request);
                    break;
                case DEEP_ANALYSIS:
                    handleDeepAnalysis(session, request);
                    break;
                case REALTIME_ANALYSIS:
                    handleRealtimeAnalysis(session, request);
                    break;
                case BATCH_ANALYSIS:
                    handleBatchAnalysis(session, request);
                    break;
                case GET_STATUS:
                    handleGetStatus(session);
                    break;
                default:
                    sendErrorMessage(session, "不支持的消息类型: " + request.getType());
            }
            
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: {}", session.getId(), e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: {}", session.getId(), exception);
        cleanupSession(session);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("WebSocket连接关闭: {}, 状态: {}, 当前连接数: {}", 
            session.getId(), closeStatus, sessions.size() - 1);
        cleanupSession(session);
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    // ==================== 消息处理方法 ====================
    
    /**
     * 处理股票订阅
     */
    private void handleStockSubscription(WebSocketSession session, WebSocketMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String stockCode = (String) data.get("stockCode");
            
            if (stockCode == null || stockCode.trim().isEmpty()) {
                sendErrorMessage(session, "股票代码不能为空");
                return;
            }
            
            stockSubscriptions.computeIfAbsent(stockCode, k -> new CopyOnWriteArrayList<>())
                .add(session);
            
            log.info("会话 {} 订阅股票: {}", session.getId(), stockCode);
            
            WebSocketMessage response = WebSocketMessage.builder()
                .type(MessageType.SUBSCRIPTION_CONFIRMED)
                .data(Map.of(
                    "stockCode", stockCode,
                    "message", "订阅成功",
                    "timestamp", System.currentTimeMillis()
                ))
                .build();
            
            sendMessage(session, response);
            
        } catch (Exception e) {
            log.error("处理股票订阅失败", e);
            sendErrorMessage(session, "订阅失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理股票取消订阅
     */
    private void handleStockUnsubscription(WebSocketSession session, WebSocketMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String stockCode = (String) data.get("stockCode");
            
            if (stockCode != null) {
                List<WebSocketSession> subscribers = stockSubscriptions.get(stockCode);
                if (subscribers != null) {
                    subscribers.remove(session);
                    if (subscribers.isEmpty()) {
                        stockSubscriptions.remove(stockCode);
                    }
                }
            }
            
            log.info("会话 {} 取消订阅股票: {}", session.getId(), stockCode);
            
            WebSocketMessage response = WebSocketMessage.builder()
                .type(MessageType.UNSUBSCRIPTION_CONFIRMED)
                .data(Map.of(
                    "stockCode", stockCode,
                    "message", "取消订阅成功",
                    "timestamp", System.currentTimeMillis()
                ))
                .build();
            
            sendMessage(session, response);
            
        } catch (Exception e) {
            log.error("处理取消订阅失败", e);
            sendErrorMessage(session, "取消订阅失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理快速分析请求
     */
    private void handleQuickAnalysis(WebSocketSession session, WebSocketMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String stockCode = (String) data.get("stockCode");
            
            // 发送分析开始消息
            sendAnalysisStartMessage(session, stockCode, "快速分析");
            
            // 执行分析
            orchestrationService.quickAnalysis(stockCode)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        sendAnalysisErrorMessage(session, stockCode, throwable.getMessage());
                    } else {
                        sendAnalysisResultMessage(session, stockCode, result, "快速分析");
                        // 广播给订阅该股票的所有客户端
                        broadcastToStockSubscribers(stockCode, result, "快速分析");
                    }
                });
                
        } catch (Exception e) {
            log.error("处理快速分析请求失败", e);
            sendErrorMessage(session, "快速分析请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理深度分析请求
     */
    private void handleDeepAnalysis(WebSocketSession session, WebSocketMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String stockCode = (String) data.get("stockCode");
            
            sendAnalysisStartMessage(session, stockCode, "深度分析");
            
            orchestrationService.deepAnalysis(stockCode)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        sendAnalysisErrorMessage(session, stockCode, throwable.getMessage());
                    } else {
                        sendAnalysisResultMessage(session, stockCode, result, "深度分析");
                        broadcastToStockSubscribers(stockCode, result, "深度分析");
                    }
                });
                
        } catch (Exception e) {
            log.error("处理深度分析请求失败", e);
            sendErrorMessage(session, "深度分析请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理实时分析请求
     */
    private void handleRealtimeAnalysis(WebSocketSession session, WebSocketMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            String stockCode = (String) data.get("stockCode");
            
            sendAnalysisStartMessage(session, stockCode, "实时分析");
            
            orchestrationService.realTimeAnalysis(stockCode)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        sendAnalysisErrorMessage(session, stockCode, throwable.getMessage());
                    } else {
                        sendAnalysisResultMessage(session, stockCode, result, "实时分析");
                        broadcastToStockSubscribers(stockCode, result, "实时分析");
                    }
                });
                
        } catch (Exception e) {
            log.error("处理实时分析请求失败", e);
            sendErrorMessage(session, "实时分析请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理批量分析请求
     */
    private void handleBatchAnalysis(WebSocketSession session, WebSocketMessage request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) request.getData();
            @SuppressWarnings("unchecked")
            List<String> stockCodes = (List<String>) data.get("stockCodes");
            
            if (stockCodes == null || stockCodes.isEmpty()) {
                sendErrorMessage(session, "股票代码列表不能为空");
                return;
            }
            
            // 发送批量分析开始消息
            WebSocketMessage startMessage = WebSocketMessage.builder()
                .type(MessageType.BATCH_ANALYSIS_STARTED)
                .data(Map.of(
                    "stockCodes", stockCodes,
                    "totalCount", stockCodes.size(),
                    "timestamp", System.currentTimeMillis()
                ))
                .build();
            sendMessage(session, startMessage);
            
            orchestrationService.batchAnalysis(stockCodes)
                .whenComplete((results, throwable) -> {
                    if (throwable != null) {
                        sendErrorMessage(session, "批量分析失败: " + throwable.getMessage());
                    } else {
                        WebSocketMessage resultMessage = WebSocketMessage.builder()
                            .type(MessageType.BATCH_ANALYSIS_COMPLETED)
                            .data(Map.of(
                                "results", results,
                                "totalCount", stockCodes.size(),
                                "successCount", results.size(),
                                "timestamp", System.currentTimeMillis()
                            ))
                            .build();
                        sendMessage(session, resultMessage);
                    }
                });
                
        } catch (Exception e) {
            log.error("处理批量分析请求失败", e);
            sendErrorMessage(session, "批量分析请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理获取状态请求
     */
    private void handleGetStatus(WebSocketSession session) {
        try {
            OrchestrationResult.OrchestratorStatus status = orchestrationService.getOrchestratorStatus();
            
            WebSocketMessage response = WebSocketMessage.builder()
                .type(MessageType.STATUS_RESPONSE)
                .data(Map.of(
                    "status", status,
                    "timestamp", System.currentTimeMillis()
                ))
                .build();
            
            sendMessage(session, response);
            
        } catch (Exception e) {
            log.error("获取状态失败", e);
            sendErrorMessage(session, "获取状态失败: " + e.getMessage());
        }
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 发送分析开始消息
     */
    private void sendAnalysisStartMessage(WebSocketSession session, String stockCode, String analysisType) {
        WebSocketMessage message = WebSocketMessage.builder()
            .type(MessageType.ANALYSIS_STARTED)
            .data(Map.of(
                "stockCode", stockCode,
                "analysisType", analysisType,
                "timestamp", System.currentTimeMillis()
            ))
            .build();
        sendMessage(session, message);
    }
    
    /**
     * 发送分析结果消息
     */
    private void sendAnalysisResultMessage(WebSocketSession session, String stockCode, 
                                         OrchestrationResult result, String analysisType) {
        WebSocketMessage message = WebSocketMessage.builder()
            .type(MessageType.ANALYSIS_COMPLETED)
            .data(Map.of(
                "stockCode", stockCode,
                "analysisType", analysisType,
                "result", result,
                "timestamp", System.currentTimeMillis()
            ))
            .build();
        sendMessage(session, message);
    }
    
    /**
     * 发送分析错误消息
     */
    private void sendAnalysisErrorMessage(WebSocketSession session, String stockCode, String error) {
        WebSocketMessage message = WebSocketMessage.builder()
            .type(MessageType.ANALYSIS_ERROR)
            .data(Map.of(
                "stockCode", stockCode,
                "error", error,
                "timestamp", System.currentTimeMillis()
            ))
            .build();
        sendMessage(session, message);
    }
    
    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String error) {
        WebSocketMessage message = WebSocketMessage.builder()
            .type(MessageType.ERROR)
            .data(Map.of(
                "error", error,
                "timestamp", System.currentTimeMillis()
            ))
            .build();
        sendMessage(session, message);
    }
    
    /**
     * 发送消息到指定会话
     */
    private void sendMessage(WebSocketSession session, WebSocketMessage message) {
        try {
            if (session.isOpen()) {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            log.error("发送WebSocket消息失败: {}", session.getId(), e);
        }
    }
    
    /**
     * 广播消息给订阅特定股票的所有客户端
     */
    private void broadcastToStockSubscribers(String stockCode, OrchestrationResult result, String analysisType) {
        List<WebSocketSession> subscribers = stockSubscriptions.get(stockCode);
        if (subscribers != null && !subscribers.isEmpty()) {
            WebSocketMessage broadcast = WebSocketMessage.builder()
                .type(MessageType.STOCK_UPDATE)
                .data(Map.of(
                    "stockCode", stockCode,
                    "analysisType", analysisType,
                    "result", result,
                    "timestamp", System.currentTimeMillis()
                ))
                .build();
            
            subscribers.forEach(session -> sendMessage(session, broadcast));
            log.debug("广播股票更新: {}, 订阅者数量: {}", stockCode, subscribers.size());
        }
    }
    
    /**
     * 清理会话
     */
    private void cleanupSession(WebSocketSession session) {
        sessions.remove(session.getId());
        
        // 从所有股票订阅中移除该会话
        stockSubscriptions.values().forEach(subscribers -> subscribers.remove(session));
        
        // 移除空的订阅列表
        stockSubscriptions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    // ==================== 消息类型和数据模型 ====================
    
    /**
     * WebSocket消息类型
     */
    public enum MessageType {
        // 连接管理
        CONNECTION_ESTABLISHED,
        
        // 订阅管理
        SUBSCRIBE_STOCK,
        UNSUBSCRIBE_STOCK,
        SUBSCRIPTION_CONFIRMED,
        UNSUBSCRIPTION_CONFIRMED,
        
        // 分析请求
        QUICK_ANALYSIS,
        DEEP_ANALYSIS,
        REALTIME_ANALYSIS,
        BATCH_ANALYSIS,
        
        // 分析响应
        ANALYSIS_STARTED,
        ANALYSIS_COMPLETED,
        ANALYSIS_ERROR,
        BATCH_ANALYSIS_STARTED,
        BATCH_ANALYSIS_COMPLETED,
        
        // 状态和更新
        GET_STATUS,
        STATUS_RESPONSE,
        STOCK_UPDATE,
        
        // 错误处理
        ERROR
    }
    
    /**
     * WebSocket消息模型
     */
    @Data
    public static class WebSocketMessage {
        private MessageType type;
        private Object data;
        private long timestamp;
        
        public static WebSocketMessageBuilder builder() {
            return new WebSocketMessageBuilder();
        }
        
        public static class WebSocketMessageBuilder {
            private MessageType type;
            private Object data;
            
            public WebSocketMessageBuilder type(MessageType type) {
                this.type = type;
                return this;
            }
            
            public WebSocketMessageBuilder data(Object data) {
                this.data = data;
                return this;
            }
            
            public WebSocketMessage build() {
                WebSocketMessage message = new WebSocketMessage();
                message.type = this.type;
                message.data = this.data;
                message.timestamp = System.currentTimeMillis();
                return message;
            }
        }
    }
}