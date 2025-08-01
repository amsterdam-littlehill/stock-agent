package com.jd.genie.agent.workflow.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jd.genie.agent.workflow.engine.WorkflowExecutionListener;
import com.jd.genie.agent.workflow.model.WorkflowExecution;
import com.jd.genie.agent.workflow.service.WorkflowService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 工作流WebSocket处理器
 * 提供实时的工作流执行状态推送和进度更新
 * 
 * 功能：
 * - 客户端连接管理
 * - 工作流执行状态实时推送
 * - 执行进度更新
 * - 错误和警告通知
 * - 订阅管理
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component
public class WorkflowWebSocketHandler implements WebSocketHandler, WorkflowExecutionListener {
    
    @Autowired
    private WorkflowService workflowService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 所有连接的客户端
     */
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    
    /**
     * 客户端订阅信息
     * Key: sessionId, Value: 订阅的执行ID列表
     */
    private final Map<String, Set<String>> subscriptions = new ConcurrentHashMap<>();
    
    /**
     * 执行ID到订阅客户端的映射
     * Key: executionId, Value: 订阅的客户端session列表
     */
    private final Map<String, Set<WebSocketSession>> executionSubscribers = new ConcurrentHashMap<>();
    
    /**
     * 客户端信息
     */
    private final Map<String, ClientInfo> clientInfos = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        subscriptions.put(session.getId(), new CopyOnWriteArraySet<>());
        
        // 记录客户端信息
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setSessionId(session.getId());
        clientInfo.setConnectedAt(LocalDateTime.now());
        clientInfo.setRemoteAddress(session.getRemoteAddress() != null ? 
            session.getRemoteAddress().toString() : "unknown");
        clientInfos.put(session.getId(), clientInfo);
        
        log.info("工作流WebSocket客户端连接: {}, 总连接数: {}", session.getId(), sessions.size());
        
        // 发送连接成功消息
        sendMessage(session, WebSocketMessage.builder()
            .type(MessageType.CONNECTION_ESTABLISHED)
            .data(Map.of(
                "sessionId", session.getId(),
                "connectedAt", LocalDateTime.now(),
                "message", "工作流WebSocket连接成功"
            ))
            .build());
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            String payload = message.getPayload().toString();
            WebSocketMessage receivedMessage = objectMapper.readValue(payload, WebSocketMessage.class);
            
            log.debug("收到WebSocket消息: {} - {}", session.getId(), receivedMessage.getType());
            
            switch (receivedMessage.getType()) {
                case SUBSCRIBE_EXECUTION:
                    handleSubscribeExecution(session, receivedMessage);
                    break;
                    
                case UNSUBSCRIBE_EXECUTION:
                    handleUnsubscribeExecution(session, receivedMessage);
                    break;
                    
                case GET_EXECUTION_STATUS:
                    handleGetExecutionStatus(session, receivedMessage);
                    break;
                    
                case GET_RUNNING_EXECUTIONS:
                    handleGetRunningExecutions(session);
                    break;
                    
                case EXECUTE_WORKFLOW:
                    handleExecuteWorkflow(session, receivedMessage);
                    break;
                    
                case CANCEL_EXECUTION:
                    handleCancelExecution(session, receivedMessage);
                    break;
                    
                case PAUSE_EXECUTION:
                    handlePauseExecution(session, receivedMessage);
                    break;
                    
                case RESUME_EXECUTION:
                    handleResumeExecution(session, receivedMessage);
                    break;
                    
                case PING:
                    handlePing(session);
                    break;
                    
                default:
                    sendErrorMessage(session, "不支持的消息类型: " + receivedMessage.getType());
                    break;
            }
            
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: {}", session.getId(), e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("工作流WebSocket传输错误: {}", session.getId(), exception);
        
        // 发送错误消息
        try {
            sendErrorMessage(session, "连接错误: " + exception.getMessage());
        } catch (Exception e) {
            log.error("发送错误消息失败", e);
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessions.remove(session);
        
        // 清理订阅信息
        Set<String> sessionSubscriptions = subscriptions.remove(session.getId());
        if (sessionSubscriptions != null) {
            for (String executionId : sessionSubscriptions) {
                Set<WebSocketSession> subscribers = executionSubscribers.get(executionId);
                if (subscribers != null) {
                    subscribers.remove(session);
                    if (subscribers.isEmpty()) {
                        executionSubscribers.remove(executionId);
                    }
                }
            }
        }
        
        clientInfos.remove(session.getId());
        
        log.info("工作流WebSocket客户端断开: {}, 状态: {}, 剩余连接数: {}", 
                session.getId(), closeStatus, sessions.size());
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    // ==================== 消息处理方法 ====================
    
    /**
     * 处理订阅执行
     */
    private void handleSubscribeExecution(WebSocketSession session, WebSocketMessage message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData();
            String executionId = (String) data.get("executionId");
            
            if (executionId == null || executionId.trim().isEmpty()) {
                sendErrorMessage(session, "执行ID不能为空");
                return;
            }
            
            // 添加订阅
            subscriptions.get(session.getId()).add(executionId);
            executionSubscribers.computeIfAbsent(executionId, k -> new CopyOnWriteArraySet<>()).add(session);
            
            log.info("客户端 {} 订阅执行: {}", session.getId(), executionId);
            
            // 发送订阅成功消息
            sendMessage(session, WebSocketMessage.builder()
                .type(MessageType.SUBSCRIPTION_CONFIRMED)
                .data(Map.of(
                    "executionId", executionId,
                    "message", "订阅成功"
                ))
                .build());
            
            // 立即发送当前状态
            WorkflowExecution execution = workflowService.getExecutionStatus(executionId);
            if (execution != null) {
                sendMessage(session, WebSocketMessage.builder()
                    .type(MessageType.EXECUTION_STATUS_UPDATE)
                    .data(execution)
                    .build());
            }
            
        } catch (Exception e) {
            log.error("处理订阅执行失败", e);
            sendErrorMessage(session, "订阅失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理取消订阅执行
     */
    private void handleUnsubscribeExecution(WebSocketSession session, WebSocketMessage message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData();
            String executionId = (String) data.get("executionId");
            
            if (executionId == null || executionId.trim().isEmpty()) {
                sendErrorMessage(session, "执行ID不能为空");
                return;
            }
            
            // 移除订阅
            subscriptions.get(session.getId()).remove(executionId);
            Set<WebSocketSession> subscribers = executionSubscribers.get(executionId);
            if (subscribers != null) {
                subscribers.remove(session);
                if (subscribers.isEmpty()) {
                    executionSubscribers.remove(executionId);
                }
            }
            
            log.info("客户端 {} 取消订阅执行: {}", session.getId(), executionId);
            
            // 发送取消订阅成功消息
            sendMessage(session, WebSocketMessage.builder()
                .type(MessageType.UNSUBSCRIPTION_CONFIRMED)
                .data(Map.of(
                    "executionId", executionId,
                    "message", "取消订阅成功"
                ))
                .build());
            
        } catch (Exception e) {
            log.error("处理取消订阅执行失败", e);
            sendErrorMessage(session, "取消订阅失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理获取执行状态
     */
    private void handleGetExecutionStatus(WebSocketSession session, WebSocketMessage message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData();
            String executionId = (String) data.get("executionId");
            
            if (executionId == null || executionId.trim().isEmpty()) {
                sendErrorMessage(session, "执行ID不能为空");
                return;
            }
            
            WorkflowExecution execution = workflowService.getExecutionStatus(executionId);
            if (execution != null) {
                sendMessage(session, WebSocketMessage.builder()
                    .type(MessageType.EXECUTION_STATUS_RESPONSE)
                    .data(execution)
                    .build());
            } else {
                sendErrorMessage(session, "执行不存在: " + executionId);
            }
            
        } catch (Exception e) {
            log.error("处理获取执行状态失败", e);
            sendErrorMessage(session, "获取执行状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理获取正在执行的工作流
     */
    private void handleGetRunningExecutions(WebSocketSession session) {
        try {
            List<WorkflowExecution> runningExecutions = workflowService.getRunningExecutions();
            
            sendMessage(session, WebSocketMessage.builder()
                .type(MessageType.RUNNING_EXECUTIONS_RESPONSE)
                .data(runningExecutions)
                .build());
            
        } catch (Exception e) {
            log.error("处理获取正在执行的工作流失败", e);
            sendErrorMessage(session, "获取正在执行的工作流失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理执行工作流
     */
    private void handleExecuteWorkflow(WebSocketSession session, WebSocketMessage message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData();
            String workflowId = (String) data.get("workflowId");
            String executedBy = (String) data.get("executedBy");
            @SuppressWarnings("unchecked")
            Map<String, Object> inputParameters = (Map<String, Object>) data.get("inputParameters");
            
            if (workflowId == null || workflowId.trim().isEmpty()) {
                sendErrorMessage(session, "工作流ID不能为空");
                return;
            }
            
            if (executedBy == null || executedBy.trim().isEmpty()) {
                sendErrorMessage(session, "执行者不能为空");
                return;
            }
            
            // 执行工作流
            workflowService.executeWorkflow(workflowId, inputParameters, executedBy)
                .whenComplete((execution, throwable) -> {
                    if (throwable == null) {
                        // 自动订阅执行
                        String executionId = execution.getExecutionId();
                        subscriptions.get(session.getId()).add(executionId);
                        executionSubscribers.computeIfAbsent(executionId, k -> new CopyOnWriteArraySet<>()).add(session);
                        
                        // 发送执行开始消息
                        sendMessage(session, WebSocketMessage.builder()
                            .type(MessageType.EXECUTION_STARTED)
                            .data(execution)
                            .build());
                    } else {
                        sendErrorMessage(session, "执行工作流失败: " + throwable.getMessage());
                    }
                });
            
        } catch (Exception e) {
            log.error("处理执行工作流失败", e);
            sendErrorMessage(session, "执行工作流失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理取消执行
     */
    private void handleCancelExecution(WebSocketSession session, WebSocketMessage message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData();
            String executionId = (String) data.get("executionId");
            
            if (executionId == null || executionId.trim().isEmpty()) {
                sendErrorMessage(session, "执行ID不能为空");
                return;
            }
            
            boolean cancelled = workflowService.cancelExecution(executionId);
            
            sendMessage(session, WebSocketMessage.builder()
                .type(MessageType.EXECUTION_CONTROL_RESPONSE)
                .data(Map.of(
                    "executionId", executionId,
                    "action", "cancel",
                    "success", cancelled,
                    "message", cancelled ? "取消成功" : "取消失败"
                ))
                .build());
            
        } catch (Exception e) {
            log.error("处理取消执行失败", e);
            sendErrorMessage(session, "取消执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理暂停执行
     */
    private void handlePauseExecution(WebSocketSession session, WebSocketMessage message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData();
            String executionId = (String) data.get("executionId");
            
            if (executionId == null || executionId.trim().isEmpty()) {
                sendErrorMessage(session, "执行ID不能为空");
                return;
            }
            
            boolean paused = workflowService.pauseExecution(executionId);
            
            sendMessage(session, WebSocketMessage.builder()
                .type(MessageType.EXECUTION_CONTROL_RESPONSE)
                .data(Map.of(
                    "executionId", executionId,
                    "action", "pause",
                    "success", paused,
                    "message", paused ? "暂停成功" : "暂停失败"
                ))
                .build());
            
        } catch (Exception e) {
            log.error("处理暂停执行失败", e);
            sendErrorMessage(session, "暂停执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理恢复执行
     */
    private void handleResumeExecution(WebSocketSession session, WebSocketMessage message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.getData();
            String executionId = (String) data.get("executionId");
            
            if (executionId == null || executionId.trim().isEmpty()) {
                sendErrorMessage(session, "执行ID不能为空");
                return;
            }
            
            boolean resumed = workflowService.resumeExecution(executionId);
            
            sendMessage(session, WebSocketMessage.builder()
                .type(MessageType.EXECUTION_CONTROL_RESPONSE)
                .data(Map.of(
                    "executionId", executionId,
                    "action", "resume",
                    "success", resumed,
                    "message", resumed ? "恢复成功" : "恢复失败"
                ))
                .build());
            
        } catch (Exception e) {
            log.error("处理恢复执行失败", e);
            sendErrorMessage(session, "恢复执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理心跳
     */
    private void handlePing(WebSocketSession session) {
        sendMessage(session, WebSocketMessage.builder()
            .type(MessageType.PONG)
            .data(Map.of(
                "timestamp", LocalDateTime.now(),
                "message", "pong"
            ))
            .build());
    }
    
    // ==================== 工作流执行监听器实现 ====================
    
    @Override
    public void onExecutionStarted(WorkflowExecution execution) {
        broadcastToSubscribers(execution.getExecutionId(), WebSocketMessage.builder()
            .type(MessageType.EXECUTION_STARTED)
            .data(execution)
            .build());
    }
    
    @Override
    public void onExecutionCompleted(WorkflowExecution execution) {
        broadcastToSubscribers(execution.getExecutionId(), WebSocketMessage.builder()
            .type(MessageType.EXECUTION_COMPLETED)
            .data(execution)
            .build());
    }
    
    @Override
    public void onExecutionFailed(WorkflowExecution execution, Exception error) {
        broadcastToSubscribers(execution.getExecutionId(), WebSocketMessage.builder()
            .type(MessageType.EXECUTION_FAILED)
            .data(Map.of(
                "execution", execution,
                "error", error.getMessage()
            ))
            .build());
    }
    
    @Override
    public void onExecutionCancelled(WorkflowExecution execution) {
        broadcastToSubscribers(execution.getExecutionId(), WebSocketMessage.builder()
            .type(MessageType.EXECUTION_CANCELLED)
            .data(execution)
            .build());
    }
    
    @Override
    public void onExecutionPaused(WorkflowExecution execution) {
        broadcastToSubscribers(execution.getExecutionId(), WebSocketMessage.builder()
            .type(MessageType.EXECUTION_PAUSED)
            .data(execution)
            .build());
    }
    
    @Override
    public void onExecutionResumed(WorkflowExecution execution) {
        broadcastToSubscribers(execution.getExecutionId(), WebSocketMessage.builder()
            .type(MessageType.EXECUTION_RESUMED)
            .data(execution)
            .build());
    }
    
    @Override
    public void onNodeStarted(WorkflowExecution execution, WorkflowExecution.NodeExecution nodeExecution) {
        broadcastToSubscribers(execution.getExecutionId(), WebSocketMessage.builder()
            .type(MessageType.NODE_STARTED)
            .data(Map.of(
                "executionId", execution.getExecutionId(),
                "nodeExecution", nodeExecution
            ))
            .build());
    }
    
    @Override
    public void onNodeCompleted(WorkflowExecution execution, WorkflowExecution.NodeExecution nodeExecution) {
        broadcastToSubscribers(execution.getExecutionId(), WebSocketMessage.builder()
            .type(MessageType.NODE_COMPLETED)
            .data(Map.of(
                "executionId", execution.getExecutionId(),
                "nodeExecution", nodeExecution
            ))
            .build());
    }
    
    @Override
    public void onNodeFailed(WorkflowExecution execution, WorkflowExecution.NodeExecution nodeExecution, Exception error) {
        broadcastToSubscribers(execution.getExecutionId(), WebSocketMessage.builder()
            .type(MessageType.NODE_FAILED)
            .data(Map.of(
                "executionId", execution.getExecutionId(),
                "nodeExecution", nodeExecution,
                "error", error.getMessage()
            ))
            .build());
    }
    
    // ==================== 辅助方法 ====================
    
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
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        sendMessage(session, WebSocketMessage.builder()
            .type(MessageType.ERROR)
            .data(Map.of(
                "error", errorMessage,
                "timestamp", LocalDateTime.now()
            ))
            .build());
    }
    
    /**
     * 广播消息给订阅者
     */
    private void broadcastToSubscribers(String executionId, WebSocketMessage message) {
        Set<WebSocketSession> subscribers = executionSubscribers.get(executionId);
        if (subscribers != null && !subscribers.isEmpty()) {
            for (WebSocketSession session : subscribers) {
                sendMessage(session, message);
            }
        }
    }
    
    /**
     * 广播消息给所有客户端
     */
    public void broadcastToAll(WebSocketMessage message) {
        for (WebSocketSession session : sessions) {
            sendMessage(session, message);
        }
    }
    
    /**
     * 获取连接统计信息
     */
    public Map<String, Object> getConnectionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConnections", sessions.size());
        stats.put("totalSubscriptions", subscriptions.values().stream().mapToInt(Set::size).sum());
        stats.put("activeExecutions", executionSubscribers.size());
        return stats;
    }
    
    // ==================== 内部类 ====================
    
    /**
     * 消息类型枚举
     */
    public enum MessageType {
        // 连接相关
        CONNECTION_ESTABLISHED,
        
        // 订阅相关
        SUBSCRIBE_EXECUTION,
        UNSUBSCRIBE_EXECUTION,
        SUBSCRIPTION_CONFIRMED,
        UNSUBSCRIPTION_CONFIRMED,
        
        // 查询相关
        GET_EXECUTION_STATUS,
        EXECUTION_STATUS_RESPONSE,
        GET_RUNNING_EXECUTIONS,
        RUNNING_EXECUTIONS_RESPONSE,
        
        // 执行控制
        EXECUTE_WORKFLOW,
        CANCEL_EXECUTION,
        PAUSE_EXECUTION,
        RESUME_EXECUTION,
        EXECUTION_CONTROL_RESPONSE,
        
        // 执行状态更新
        EXECUTION_STARTED,
        EXECUTION_COMPLETED,
        EXECUTION_FAILED,
        EXECUTION_CANCELLED,
        EXECUTION_PAUSED,
        EXECUTION_RESUMED,
        EXECUTION_STATUS_UPDATE,
        
        // 节点状态更新
        NODE_STARTED,
        NODE_COMPLETED,
        NODE_FAILED,
        
        // 心跳
        PING,
        PONG,
        
        // 错误
        ERROR
    }
    
    /**
     * WebSocket消息模型
     */
    @Data
    public static class WebSocketMessage {
        private MessageType type;
        private Object data;
        private LocalDateTime timestamp;
        
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
                message.setType(type);
                message.setData(data);
                message.setTimestamp(LocalDateTime.now());
                return message;
            }
        }
    }
    
    /**
     * 客户端信息
     */
    @Data
    public static class ClientInfo {
        private String sessionId;
        private LocalDateTime connectedAt;
        private String remoteAddress;
        private Set<String> subscribedExecutions = new HashSet<>();
    }
}