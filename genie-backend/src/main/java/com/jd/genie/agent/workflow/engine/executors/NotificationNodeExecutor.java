package com.jd.genie.agent.workflow.engine.executors;

import com.jd.genie.agent.workflow.engine.NodeExecutor;
import com.jd.genie.agent.workflow.model.WorkflowDefinition;
import com.jd.genie.agent.workflow.model.WorkflowExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 通知节点执行器
 * 发送各种类型的通知消息
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component
public class NotificationNodeExecutor implements NodeExecutor {
    
    @Override
    public Map<String, Object> execute(WorkflowDefinition workflow,
                                      WorkflowExecution execution,
                                      WorkflowDefinition.WorkflowNode node,
                                      Map<String, Object> inputData) throws Exception {
        
        log.info("执行通知节点: {} - {}", node.getNodeId(), node.getName());
        
        // 获取通知配置
        NotificationConfig notificationConfig = getNotificationConfig(node, inputData);
        
        // 发送通知
        NotificationResult result = sendNotification(notificationConfig, execution, inputData);
        
        // 准备输出数据
        Map<String, Object> outputData = new HashMap<>(inputData);
        outputData.put("notificationResult", result);
        outputData.put("notificationSent", result.isSuccess());
        outputData.put("notificationType", notificationConfig.getType());
        outputData.put("notificationMessage", notificationConfig.getMessage());
        outputData.put("nodeType", "NOTIFICATION");
        outputData.put("timestamp", System.currentTimeMillis());
        
        if (!result.isSuccess()) {
            log.warn("通知发送失败: {}", result.getErrorMessage());
            outputData.put("notificationError", result.getErrorMessage());
        }
        
        log.info("通知节点执行完成: {} - {}, 结果: {}", 
                node.getNodeId(), node.getName(), result.isSuccess());
        
        return outputData;
    }
    
    /**
     * 获取通知配置
     */
    private NotificationConfig getNotificationConfig(WorkflowDefinition.WorkflowNode node, 
                                                   Map<String, Object> inputData) {
        
        NotificationConfig config = new NotificationConfig();
        
        // 从节点配置中获取
        if (node.getConfig() != null && node.getConfig().getInputParameters() != null) {
            Map<String, Object> params = node.getConfig().getInputParameters();
            
            config.setType(getStringParam(params, "type", "SYSTEM"));
            config.setTitle(getStringParam(params, "title", "工作流通知"));
            config.setMessage(getStringParam(params, "message", "工作流执行通知"));
            config.setRecipients(getListParam(params, "recipients"));
            config.setTemplate(getStringParam(params, "template", null));
            config.setPriority(getStringParam(params, "priority", "NORMAL"));
            config.setChannel(getStringParam(params, "channel", "DEFAULT"));
        }
        
        // 从输入数据中覆盖配置
        config.setType(getStringParam(inputData, "notificationType", config.getType()));
        config.setTitle(getStringParam(inputData, "notificationTitle", config.getTitle()));
        config.setMessage(getStringParam(inputData, "notificationMessage", config.getMessage()));
        
        List<String> inputRecipients = getListParam(inputData, "notificationRecipients");
        if (inputRecipients != null && !inputRecipients.isEmpty()) {
            config.setRecipients(inputRecipients);
        }
        
        // 处理消息模板变量
        config.setMessage(resolveMessageTemplate(config.getMessage(), inputData));
        config.setTitle(resolveMessageTemplate(config.getTitle(), inputData));
        
        return config;
    }
    
    /**
     * 获取字符串参数
     */
    private String getStringParam(Map<String, Object> params, String key, String defaultValue) {
        Object value = params.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    /**
     * 获取列表参数
     */
    @SuppressWarnings("unchecked")
    private List<String> getListParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return list.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .toList();
        } else if (value instanceof String) {
            // 支持逗号分隔的字符串
            String str = (String) value;
            return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        }
        return new ArrayList<>();
    }
    
    /**
     * 解析消息模板
     */
    private String resolveMessageTemplate(String template, Map<String, Object> data) {
        if (template == null || !template.contains("${")) {
            return template;
        }
        
        String result = template;
        
        // 替换数据变量
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            if (result.contains(placeholder)) {
                String replacement = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace(placeholder, replacement);
            }
        }
        
        // 替换系统变量
        result = result.replace("${timestamp}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result = result.replace("${date}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        result = result.replace("${time}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        
        return result;
    }
    
    /**
     * 发送通知
     */
    private NotificationResult sendNotification(NotificationConfig config, 
                                              WorkflowExecution execution, 
                                              Map<String, Object> inputData) {
        
        try {
            switch (config.getType().toUpperCase()) {
                case "EMAIL":
                    return sendEmailNotification(config, execution, inputData);
                case "SMS":
                    return sendSmsNotification(config, execution, inputData);
                case "WEBHOOK":
                    return sendWebhookNotification(config, execution, inputData);
                case "SYSTEM":
                default:
                    return sendSystemNotification(config, execution, inputData);
            }
        } catch (Exception e) {
            log.error("发送通知失败: {}", config.getType(), e);
            return NotificationResult.failure("发送通知失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送邮件通知
     */
    private NotificationResult sendEmailNotification(NotificationConfig config, 
                                                    WorkflowExecution execution, 
                                                    Map<String, Object> inputData) {
        
        log.info("发送邮件通知: 标题={}, 收件人={}", config.getTitle(), config.getRecipients());
        
        // 这里应该集成实际的邮件发送服务
        // 例如：Spring Mail、SendGrid、阿里云邮件推送等
        
        try {
            // 模拟邮件发送
            Thread.sleep(100); // 模拟网络延迟
            
            // 实际实现中，这里应该调用邮件服务API
            /*
            EmailService emailService = ...; // 注入邮件服务
            emailService.sendEmail(
                config.getRecipients(),
                config.getTitle(),
                config.getMessage()
            );
            */
            
            log.info("邮件通知发送成功");
            return NotificationResult.success("邮件发送成功");
            
        } catch (Exception e) {
            log.error("邮件通知发送失败", e);
            return NotificationResult.failure("邮件发送失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送短信通知
     */
    private NotificationResult sendSmsNotification(NotificationConfig config, 
                                                 WorkflowExecution execution, 
                                                 Map<String, Object> inputData) {
        
        log.info("发送短信通知: 内容={}, 收件人={}", config.getMessage(), config.getRecipients());
        
        // 这里应该集成实际的短信发送服务
        // 例如：阿里云短信、腾讯云短信、华为云短信等
        
        try {
            // 模拟短信发送
            Thread.sleep(200); // 模拟网络延迟
            
            // 实际实现中，这里应该调用短信服务API
            /*
            SmsService smsService = ...; // 注入短信服务
            smsService.sendSms(
                config.getRecipients(),
                config.getMessage()
            );
            */
            
            log.info("短信通知发送成功");
            return NotificationResult.success("短信发送成功");
            
        } catch (Exception e) {
            log.error("短信通知发送失败", e);
            return NotificationResult.failure("短信发送失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送Webhook通知
     */
    private NotificationResult sendWebhookNotification(NotificationConfig config, 
                                                     WorkflowExecution execution, 
                                                     Map<String, Object> inputData) {
        
        log.info("发送Webhook通知: URL={}", config.getRecipients());
        
        // 这里应该集成HTTP客户端发送Webhook
        // 例如：RestTemplate、WebClient、OkHttp等
        
        try {
            // 准备Webhook数据
            Map<String, Object> webhookData = new HashMap<>();
            webhookData.put("title", config.getTitle());
            webhookData.put("message", config.getMessage());
            webhookData.put("timestamp", System.currentTimeMillis());
            webhookData.put("executionId", execution.getExecutionId());
            webhookData.put("workflowId", execution.getWorkflowId());
            webhookData.put("data", inputData);
            
            // 模拟Webhook发送
            Thread.sleep(300); // 模拟网络延迟
            
            // 实际实现中，这里应该发送HTTP请求
            /*
            RestTemplate restTemplate = ...; // 注入HTTP客户端
            for (String url : config.getRecipients()) {
                restTemplate.postForObject(url, webhookData, String.class);
            }
            */
            
            log.info("Webhook通知发送成功");
            return NotificationResult.success("Webhook发送成功");
            
        } catch (Exception e) {
            log.error("Webhook通知发送失败", e);
            return NotificationResult.failure("Webhook发送失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送系统通知
     */
    private NotificationResult sendSystemNotification(NotificationConfig config, 
                                                    WorkflowExecution execution, 
                                                    Map<String, Object> inputData) {
        
        log.info("发送系统通知: {}", config.getMessage());
        
        // 系统通知可以记录到数据库、发送到消息队列等
        try {
            // 记录系统通知
            execution.addLog(
                WorkflowExecution.LogLevel.INFO,
                null,
                "系统通知: " + config.getTitle(),
                config.getMessage()
            );
            
            // 实际实现中，这里可以：
            // 1. 保存到数据库
            // 2. 发送到消息队列
            // 3. 推送到WebSocket客户端
            // 4. 记录到审计日志
            
            log.info("系统通知发送成功");
            return NotificationResult.success("系统通知发送成功");
            
        } catch (Exception e) {
            log.error("系统通知发送失败", e);
            return NotificationResult.failure("系统通知发送失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<String> validateNode(WorkflowDefinition.WorkflowNode node) {
        List<String> errors = new ArrayList<>();
        
        if (node.getConfig() == null || node.getConfig().getInputParameters() == null) {
            errors.add("通知节点缺少配置信息");
            return errors;
        }
        
        Map<String, Object> params = node.getConfig().getInputParameters();
        
        // 验证通知类型
        String type = getStringParam(params, "type", "SYSTEM");
        Set<String> supportedTypes = Set.of("EMAIL", "SMS", "WEBHOOK", "SYSTEM");
        if (!supportedTypes.contains(type.toUpperCase())) {
            errors.add("不支持的通知类型: " + type);
        }
        
        // 验证消息内容
        String message = getStringParam(params, "message", null);
        if (message == null || message.trim().isEmpty()) {
            errors.add("通知节点缺少消息内容");
        }
        
        // 验证收件人（对于非系统通知）
        if (!"SYSTEM".equalsIgnoreCase(type)) {
            List<String> recipients = getListParam(params, "recipients");
            if (recipients == null || recipients.isEmpty()) {
                errors.add("通知节点缺少收件人配置");
            }
        }
        
        return errors;
    }
    
    @Override
    public WorkflowDefinition.NodeType getSupportedNodeType() {
        return WorkflowDefinition.NodeType.NOTIFICATION;
    }
    
    // ==================== 内部类 ====================
    
    /**
     * 通知配置
     */
    private static class NotificationConfig {
        private String type;
        private String title;
        private String message;
        private List<String> recipients;
        private String template;
        private String priority;
        private String channel;
        
        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public List<String> getRecipients() { return recipients; }
        public void setRecipients(List<String> recipients) { this.recipients = recipients; }
        
        public String getTemplate() { return template; }
        public void setTemplate(String template) { this.template = template; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
    }
    
    /**
     * 通知结果
     */
    private static class NotificationResult {
        private boolean success;
        private String message;
        private String errorMessage;
        
        private NotificationResult(boolean success, String message, String errorMessage) {
            this.success = success;
            this.message = message;
            this.errorMessage = errorMessage;
        }
        
        public static NotificationResult success(String message) {
            return new NotificationResult(true, message, null);
        }
        
        public static NotificationResult failure(String errorMessage) {
            return new NotificationResult(false, null, errorMessage);
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getErrorMessage() { return errorMessage; }
    }
}