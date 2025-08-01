package com.jd.genie.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket消息服务
 * 用于实时推送股票分析进度和结果
 */
@Slf4j
@Service
public class WebSocketMessageService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 分析进度消息
     */
    public static class AnalysisProgressMessage {
        private String requestId;
        private String stockCode;
        private String stage;
        private String agentName;
        private String status;
        private Integer progress;
        private String message;
        private String timestamp;
        private Map<String, Object> data;

        // Getters and Setters
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        public String getStockCode() { return stockCode; }
        public void setStockCode(String stockCode) { this.stockCode = stockCode; }
        public String getStage() { return stage; }
        public void setStage(String stage) { this.stage = stage; }
        public String getAgentName() { return agentName; }
        public void setAgentName(String agentName) { this.agentName = agentName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getProgress() { return progress; }
        public void setProgress(Integer progress) { this.progress = progress; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
    }

    /**
     * 分析结果消息
     */
    public static class AnalysisResultMessage {
        private String requestId;
        private String stockCode;
        private String status;
        private Object result;
        private String error;
        private String timestamp;
        private Long processingTimeMs;

        // Getters and Setters
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        public String getStockCode() { return stockCode; }
        public void setStockCode(String stockCode) { this.stockCode = stockCode; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Object getResult() { return result; }
        public void setResult(Object result) { this.result = result; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public Long getProcessingTimeMs() { return processingTimeMs; }
        public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
    }

    /**
     * 实时价格更新消息
     */
    public static class PriceUpdateMessage {
        private String stockCode;
        private String stockName;
        private Double currentPrice;
        private Double change;
        private Double changePercent;
        private String timestamp;
        private String status;

        // Getters and Setters
        public String getStockCode() { return stockCode; }
        public void setStockCode(String stockCode) { this.stockCode = stockCode; }
        public String getStockName() { return stockName; }
        public void setStockName(String stockName) { this.stockName = stockName; }
        public Double getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }
        public Double getChange() { return change; }
        public void setChange(Double change) { this.change = change; }
        public Double getChangePercent() { return changePercent; }
        public void setChangePercent(Double changePercent) { this.changePercent = changePercent; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    /**
     * 发送分析进度更新
     */
    public void sendAnalysisProgress(String requestId, String stockCode, String stage, 
                                   String agentName, String status, Integer progress, String message) {
        try {
            AnalysisProgressMessage progressMessage = new AnalysisProgressMessage();
            progressMessage.setRequestId(requestId);
            progressMessage.setStockCode(stockCode);
            progressMessage.setStage(stage);
            progressMessage.setAgentName(agentName);
            progressMessage.setStatus(status);
            progressMessage.setProgress(progress);
            progressMessage.setMessage(message);
            progressMessage.setTimestamp(getCurrentTimestamp());

            // 发送到特定请求的订阅者
            messagingTemplate.convertAndSend("/topic/analysis/progress/" + requestId, progressMessage);
            
            // 发送到股票的所有订阅者
            messagingTemplate.convertAndSend("/topic/stock/" + stockCode + "/progress", progressMessage);
            
            log.debug("发送分析进度: {} - {} - {} - {}%", requestId, stockCode, stage, progress);
            
        } catch (Exception e) {
            log.error("发送分析进度失败: {} - {}", requestId, stockCode, e);
        }
    }

    /**
     * 发送分析结果
     */
    public void sendAnalysisResult(String requestId, String stockCode, String status, 
                                 Object result, String error, Long processingTimeMs) {
        try {
            AnalysisResultMessage resultMessage = new AnalysisResultMessage();
            resultMessage.setRequestId(requestId);
            resultMessage.setStockCode(stockCode);
            resultMessage.setStatus(status);
            resultMessage.setResult(result);
            resultMessage.setError(error);
            resultMessage.setTimestamp(getCurrentTimestamp());
            resultMessage.setProcessingTimeMs(processingTimeMs);

            // 发送到特定请求的订阅者
            messagingTemplate.convertAndSend("/topic/analysis/result/" + requestId, resultMessage);
            
            // 发送到股票的所有订阅者
            messagingTemplate.convertAndSend("/topic/stock/" + stockCode + "/result", resultMessage);
            
            log.info("发送分析结果: {} - {} - {} - {}ms", requestId, stockCode, status, processingTimeMs);
            
        } catch (Exception e) {
            log.error("发送分析结果失败: {} - {}", requestId, stockCode, e);
        }
    }

    /**
     * 发送实时价格更新
     */
    public void sendPriceUpdate(String stockCode, String stockName, Double currentPrice, 
                              Double change, Double changePercent, String status) {
        try {
            PriceUpdateMessage priceMessage = new PriceUpdateMessage();
            priceMessage.setStockCode(stockCode);
            priceMessage.setStockName(stockName);
            priceMessage.setCurrentPrice(currentPrice);
            priceMessage.setChange(change);
            priceMessage.setChangePercent(changePercent);
            priceMessage.setTimestamp(getCurrentTimestamp());
            priceMessage.setStatus(status);

            // 发送到股票价格订阅者
            messagingTemplate.convertAndSend("/topic/price/" + stockCode, priceMessage);
            
            // 发送到全局价格更新
            messagingTemplate.convertAndSend("/topic/price/updates", priceMessage);
            
            log.debug("发送价格更新: {} - {} - {}", stockCode, currentPrice, changePercent);
            
        } catch (Exception e) {
            log.error("发送价格更新失败: {}", stockCode, e);
        }
    }

    /**
     * 发送智能体状态更新
     */
    public void sendAgentStatus(String agentId, String agentName, String status, 
                              String currentTask, Map<String, Object> metrics) {
        try {
            Map<String, Object> statusMessage = new HashMap<>();
            statusMessage.put("agentId", agentId);
            statusMessage.put("agentName", agentName);
            statusMessage.put("status", status);
            statusMessage.put("currentTask", currentTask);
            statusMessage.put("metrics", metrics);
            statusMessage.put("timestamp", getCurrentTimestamp());

            // 发送到智能体状态订阅者
            messagingTemplate.convertAndSend("/topic/agent/status/" + agentId, statusMessage);
            
            // 发送到全局智能体状态
            messagingTemplate.convertAndSend("/topic/agent/status", statusMessage);
            
            log.debug("发送智能体状态: {} - {} - {}", agentId, agentName, status);
            
        } catch (Exception e) {
            log.error("发送智能体状态失败: {} - {}", agentId, agentName, e);
        }
    }

    /**
     * 发送系统通知
     */
    public void sendSystemNotification(String type, String title, String message, 
                                     String level, Map<String, Object> data) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", type);
            notification.put("title", title);
            notification.put("message", message);
            notification.put("level", level); // info, warning, error, success
            notification.put("data", data);
            notification.put("timestamp", getCurrentTimestamp());

            // 发送到系统通知订阅者
            messagingTemplate.convertAndSend("/topic/system/notifications", notification);
            
            log.info("发送系统通知: {} - {} - {}", type, title, level);
            
        } catch (Exception e) {
            log.error("发送系统通知失败: {} - {}", type, title, e);
        }
    }

    /**
     * 发送用户特定消息
     */
    public void sendUserMessage(String userId, String type, Object message) {
        try {
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("type", type);
            userMessage.put("message", message);
            userMessage.put("timestamp", getCurrentTimestamp());

            // 发送到特定用户
            messagingTemplate.convertAndSendToUser(userId, "/queue/messages", userMessage);
            
            log.debug("发送用户消息: {} - {}", userId, type);
            
        } catch (Exception e) {
            log.error("发送用户消息失败: {} - {}", userId, type, e);
        }
    }

    /**
     * 广播市场状态更新
     */
    public void broadcastMarketStatus(String market, String status, Map<String, Object> data) {
        try {
            Map<String, Object> marketMessage = new HashMap<>();
            marketMessage.put("market", market);
            marketMessage.put("status", status);
            marketMessage.put("data", data);
            marketMessage.put("timestamp", getCurrentTimestamp());

            // 广播到所有市场状态订阅者
            messagingTemplate.convertAndSend("/topic/market/status", marketMessage);
            
            log.info("广播市场状态: {} - {}", market, status);
            
        } catch (Exception e) {
            log.error("广播市场状态失败: {} - {}", market, status, e);
        }
    }

    /**
     * 发送分析队列状态
     */
    public void sendQueueStatus(int queueSize, int processingCount, int completedCount, 
                              double averageProcessingTime) {
        try {
            Map<String, Object> queueStatus = new HashMap<>();
            queueStatus.put("queueSize", queueSize);
            queueStatus.put("processingCount", processingCount);
            queueStatus.put("completedCount", completedCount);
            queueStatus.put("averageProcessingTime", averageProcessingTime);
            queueStatus.put("timestamp", getCurrentTimestamp());

            // 发送到队列状态订阅者
            messagingTemplate.convertAndSend("/topic/queue/status", queueStatus);
            
            log.debug("发送队列状态: 队列={}, 处理中={}, 已完成={}", queueSize, processingCount, completedCount);
            
        } catch (Exception e) {
            log.error("发送队列状态失败", e);
        }
    }

    /**
     * 发送错误通知
     */
    public void sendErrorNotification(String requestId, String stockCode, String errorType, 
                                    String errorMessage, Map<String, Object> context) {
        try {
            Map<String, Object> errorNotification = new HashMap<>();
            errorNotification.put("requestId", requestId);
            errorNotification.put("stockCode", stockCode);
            errorNotification.put("errorType", errorType);
            errorNotification.put("errorMessage", errorMessage);
            errorNotification.put("context", context);
            errorNotification.put("timestamp", getCurrentTimestamp());

            // 发送到错误通知订阅者
            messagingTemplate.convertAndSend("/topic/errors", errorNotification);
            
            // 如果有请求ID，也发送到特定请求
            if (requestId != null) {
                messagingTemplate.convertAndSend("/topic/analysis/error/" + requestId, errorNotification);
            }
            
            log.warn("发送错误通知: {} - {} - {}", requestId, stockCode, errorType);
            
        } catch (Exception e) {
            log.error("发送错误通知失败: {} - {}", requestId, stockCode, e);
        }
    }

    /**
     * 测试WebSocket连接
     */
    public void sendTestMessage(String destination, String message) {
        try {
            Map<String, Object> testMessage = new HashMap<>();
            testMessage.put("type", "test");
            testMessage.put("message", message);
            testMessage.put("timestamp", getCurrentTimestamp());

            messagingTemplate.convertAndSend(destination, testMessage);
            
            log.info("发送测试消息: {} - {}", destination, message);
            
        } catch (Exception e) {
            log.error("发送测试消息失败: {} - {}", destination, message, e);
        }
    }

    // ==================== 辅助方法 ====================

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }

    /**
     * 创建分析进度消息构建器
     */
    public AnalysisProgressBuilder createProgressBuilder(String requestId, String stockCode) {
        return new AnalysisProgressBuilder(this, requestId, stockCode);
    }

    /**
     * 分析进度消息构建器
     */
    public static class AnalysisProgressBuilder {
        private final WebSocketMessageService service;
        private final String requestId;
        private final String stockCode;
        private String stage;
        private String agentName;
        private String status;
        private Integer progress;
        private String message;
        private Map<String, Object> data;

        public AnalysisProgressBuilder(WebSocketMessageService service, String requestId, String stockCode) {
            this.service = service;
            this.requestId = requestId;
            this.stockCode = stockCode;
        }

        public AnalysisProgressBuilder stage(String stage) {
            this.stage = stage;
            return this;
        }

        public AnalysisProgressBuilder agent(String agentName) {
            this.agentName = agentName;
            return this;
        }

        public AnalysisProgressBuilder status(String status) {
            this.status = status;
            return this;
        }

        public AnalysisProgressBuilder progress(Integer progress) {
            this.progress = progress;
            return this;
        }

        public AnalysisProgressBuilder message(String message) {
            this.message = message;
            return this;
        }

        public AnalysisProgressBuilder data(Map<String, Object> data) {
            this.data = data;
            return this;
        }

        public void send() {
            service.sendAnalysisProgress(requestId, stockCode, stage, agentName, status, progress, message);
        }
    }
}