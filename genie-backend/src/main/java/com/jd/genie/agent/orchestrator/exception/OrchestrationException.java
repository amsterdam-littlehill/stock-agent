package com.jd.genie.agent.orchestrator.exception;

/**
 * 协调器异常基类
 * 定义智能体协调过程中可能出现的各种异常
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
public class OrchestrationException extends Exception {
    
    private final String errorCode;
    private final String agentType;
    private final long timestamp;
    
    public OrchestrationException(String message) {
        super(message);
        this.errorCode = "ORCHESTRATION_ERROR";
        this.agentType = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    public OrchestrationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "ORCHESTRATION_ERROR";
        this.agentType = null;
        this.timestamp = System.currentTimeMillis();
    }
    
    public OrchestrationException(String errorCode, String message, String agentType) {
        super(message);
        this.errorCode = errorCode;
        this.agentType = agentType;
        this.timestamp = System.currentTimeMillis();
    }
    
    public OrchestrationException(String errorCode, String message, String agentType, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.agentType = agentType;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getAgentType() {
        return agentType;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return String.format("OrchestrationException{errorCode='%s', agentType='%s', message='%s', timestamp=%d}",
            errorCode, agentType, getMessage(), timestamp);
    }
    
    /**
     * 智能体执行异常
     */
    public static class AgentExecutionException extends OrchestrationException {
        public AgentExecutionException(String agentType, String message) {
            super("AGENT_EXECUTION_ERROR", message, agentType);
        }
        
        public AgentExecutionException(String agentType, String message, Throwable cause) {
            super("AGENT_EXECUTION_ERROR", message, agentType, cause);
        }
    }
    
    /**
     * 智能体超时异常
     */
    public static class AgentTimeoutException extends OrchestrationException {
        private final long timeoutMillis;
        
        public AgentTimeoutException(String agentType, long timeoutMillis) {
            super("AGENT_TIMEOUT", 
                String.format("智能体 %s 执行超时，超时时间: %d毫秒", agentType, timeoutMillis), 
                agentType);
            this.timeoutMillis = timeoutMillis;
        }
        
        public long getTimeoutMillis() {
            return timeoutMillis;
        }
    }
    
    /**
     * 智能体不可用异常
     */
    public static class AgentUnavailableException extends OrchestrationException {
        public AgentUnavailableException(String agentType) {
            super("AGENT_UNAVAILABLE", 
                String.format("智能体 %s 不可用或未注册", agentType), 
                agentType);
        }
        
        public AgentUnavailableException(String agentType, String reason) {
            super("AGENT_UNAVAILABLE", 
                String.format("智能体 %s 不可用: %s", agentType, reason), 
                agentType);
        }
    }
    
    /**
     * 依赖关系异常
     */
    public static class DependencyException extends OrchestrationException {
        private final String missingDependency;
        
        public DependencyException(String agentType, String missingDependency) {
            super("DEPENDENCY_ERROR", 
                String.format("智能体 %s 缺少依赖: %s", agentType, missingDependency), 
                agentType);
            this.missingDependency = missingDependency;
        }
        
        public String getMissingDependency() {
            return missingDependency;
        }
    }
    
    /**
     * 任务配置异常
     */
    public static class TaskConfigurationException extends OrchestrationException {
        public TaskConfigurationException(String message) {
            super("TASK_CONFIG_ERROR", message, null);
        }
        
        public TaskConfigurationException(String message, Throwable cause) {
            super("TASK_CONFIG_ERROR", message, null, cause);
        }
    }
    
    /**
     * 结果聚合异常
     */
    public static class ResultAggregationException extends OrchestrationException {
        public ResultAggregationException(String message) {
            super("RESULT_AGGREGATION_ERROR", message, null);
        }
        
        public ResultAggregationException(String message, Throwable cause) {
            super("RESULT_AGGREGATION_ERROR", message, null, cause);
        }
    }
    
    /**
     * 资源不足异常
     */
    public static class ResourceExhaustedException extends OrchestrationException {
        private final String resourceType;
        
        public ResourceExhaustedException(String resourceType, String message) {
            super("RESOURCE_EXHAUSTED", message, null);
            this.resourceType = resourceType;
        }
        
        public String getResourceType() {
            return resourceType;
        }
    }
    
    /**
     * 并发执行异常
     */
    public static class ConcurrentExecutionException extends OrchestrationException {
        public ConcurrentExecutionException(String message) {
            super("CONCURRENT_EXECUTION_ERROR", message, null);
        }
        
        public ConcurrentExecutionException(String message, Throwable cause) {
            super("CONCURRENT_EXECUTION_ERROR", message, null, cause);
        }
    }
    
    /**
     * 数据验证异常
     */
    public static class DataValidationException extends OrchestrationException {
        private final String fieldName;
        private final Object invalidValue;
        
        public DataValidationException(String fieldName, Object invalidValue, String message) {
            super("DATA_VALIDATION_ERROR", 
                String.format("数据验证失败 - 字段: %s, 值: %s, 原因: %s", fieldName, invalidValue, message), 
                null);
            this.fieldName = fieldName;
            this.invalidValue = invalidValue;
        }
        
        public String getFieldName() {
            return fieldName;
        }
        
        public Object getInvalidValue() {
            return invalidValue;
        }
    }
    
    /**
     * 网络连接异常
     */
    public static class NetworkException extends OrchestrationException {
        private final String endpoint;
        
        public NetworkException(String endpoint, String message) {
            super("NETWORK_ERROR", 
                String.format("网络连接失败 - 端点: %s, 原因: %s", endpoint, message), 
                null);
            this.endpoint = endpoint;
        }
        
        public NetworkException(String endpoint, String message, Throwable cause) {
            super("NETWORK_ERROR", 
                String.format("网络连接失败 - 端点: %s, 原因: %s", endpoint, message), 
                null, cause);
            this.endpoint = endpoint;
        }
        
        public String getEndpoint() {
            return endpoint;
        }
    }
    
    /**
     * 权限异常
     */
    public static class PermissionException extends OrchestrationException {
        private final String operation;
        private final String userId;
        
        public PermissionException(String userId, String operation) {
            super("PERMISSION_DENIED", 
                String.format("用户 %s 没有权限执行操作: %s", userId, operation), 
                null);
            this.operation = operation;
            this.userId = userId;
        }
        
        public String getOperation() {
            return operation;
        }
        
        public String getUserId() {
            return userId;
        }
    }
    
    /**
     * 限流异常
     */
    public static class RateLimitException extends OrchestrationException {
        private final int currentRate;
        private final int maxRate;
        private final long retryAfterMillis;
        
        public RateLimitException(int currentRate, int maxRate, long retryAfterMillis) {
            super("RATE_LIMIT_EXCEEDED", 
                String.format("请求频率超限 - 当前: %d/s, 最大: %d/s, 重试间隔: %d毫秒", 
                    currentRate, maxRate, retryAfterMillis), 
                null);
            this.currentRate = currentRate;
            this.maxRate = maxRate;
            this.retryAfterMillis = retryAfterMillis;
        }
        
        public int getCurrentRate() {
            return currentRate;
        }
        
        public int getMaxRate() {
            return maxRate;
        }
        
        public long getRetryAfterMillis() {
            return retryAfterMillis;
        }
    }
    
    /**
     * 服务降级异常
     */
    public static class ServiceDegradedException extends OrchestrationException {
        private final String serviceName;
        private final String degradationReason;
        
        public ServiceDegradedException(String serviceName, String degradationReason) {
            super("SERVICE_DEGRADED", 
                String.format("服务 %s 已降级: %s", serviceName, degradationReason), 
                null);
            this.serviceName = serviceName;
            this.degradationReason = degradationReason;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public String getDegradationReason() {
            return degradationReason;
        }
    }
    
    /**
     * 熔断器异常
     */
    public static class CircuitBreakerException extends OrchestrationException {
        private final String circuitName;
        private final long retryAfterMillis;
        
        public CircuitBreakerException(String circuitName, long retryAfterMillis) {
            super("CIRCUIT_BREAKER_OPEN", 
                String.format("熔断器 %s 已打开，重试时间: %d毫秒后", circuitName, retryAfterMillis), 
                null);
            this.circuitName = circuitName;
            this.retryAfterMillis = retryAfterMillis;
        }
        
        public String getCircuitName() {
            return circuitName;
        }
        
        public long getRetryAfterMillis() {
            return retryAfterMillis;
        }
    }
    
    /**
     * 创建智能体执行异常
     */
    public static AgentExecutionException agentExecution(String agentType, String message) {
        return new AgentExecutionException(agentType, message);
    }
    
    /**
     * 创建智能体执行异常（带原因）
     */
    public static AgentExecutionException agentExecution(String agentType, String message, Throwable cause) {
        return new AgentExecutionException(agentType, message, cause);
    }
    
    /**
     * 创建智能体超时异常
     */
    public static AgentTimeoutException agentTimeout(String agentType, long timeoutMillis) {
        return new AgentTimeoutException(agentType, timeoutMillis);
    }
    
    /**
     * 创建智能体不可用异常
     */
    public static AgentUnavailableException agentUnavailable(String agentType) {
        return new AgentUnavailableException(agentType);
    }
    
    /**
     * 创建智能体不可用异常（带原因）
     */
    public static AgentUnavailableException agentUnavailable(String agentType, String reason) {
        return new AgentUnavailableException(agentType, reason);
    }
    
    /**
     * 创建依赖关系异常
     */
    public static DependencyException dependency(String agentType, String missingDependency) {
        return new DependencyException(agentType, missingDependency);
    }
    
    /**
     * 创建任务配置异常
     */
    public static TaskConfigurationException taskConfig(String message) {
        return new TaskConfigurationException(message);
    }
    
    /**
     * 创建结果聚合异常
     */
    public static ResultAggregationException resultAggregation(String message) {
        return new ResultAggregationException(message);
    }
    
    /**
     * 创建资源不足异常
     */
    public static ResourceExhaustedException resourceExhausted(String resourceType, String message) {
        return new ResourceExhaustedException(resourceType, message);
    }
    
    /**
     * 创建数据验证异常
     */
    public static DataValidationException dataValidation(String fieldName, Object invalidValue, String message) {
        return new DataValidationException(fieldName, invalidValue, message);
    }
    
    /**
     * 创建网络异常
     */
    public static NetworkException network(String endpoint, String message) {
        return new NetworkException(endpoint, message);
    }
    
    /**
     * 创建权限异常
     */
    public static PermissionException permission(String userId, String operation) {
        return new PermissionException(userId, operation);
    }
    
    /**
     * 创建限流异常
     */
    public static RateLimitException rateLimit(int currentRate, int maxRate, long retryAfterMillis) {
        return new RateLimitException(currentRate, maxRate, retryAfterMillis);
    }
}