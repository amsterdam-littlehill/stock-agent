package com.jd.genie.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 全局配置类
 * 统一管理应用的核心配置
 * 
 * @author Stock Agent Team
 * @version 1.0.0
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class GlobalConfiguration {

    /**
     * 配置Jackson ObjectMapper
     * 统一JSON序列化/反序列化配置
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 注册Java 8时间模块
        mapper.registerModule(new JavaTimeModule());
        
        // 禁用将日期写为时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 设置时区
        mapper.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Asia/Shanghai")));
        
        // 忽略未知属性
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 忽略空Bean序列化错误
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        // 不序列化null值
        mapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
        
        return mapper;
    }
    
    /**
     * 配置主要任务执行器
     * 用于异步任务执行
     */
    @Bean(name = "taskExecutor")
    @Primary
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(8);
        
        // 最大线程数
        executor.setMaxPoolSize(20);
        
        // 队列容量
        executor.setQueueCapacity(100);
        
        // 线程名前缀
        executor.setThreadNamePrefix("stock-agent-task-");
        
        // 线程空闲时间
        executor.setKeepAliveSeconds(60);
        
        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待任务完成后关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
    
    /**
     * 配置智能体专用任务执行器
     * 用于智能体协调器的异步任务
     */
    @Bean(name = "agentTaskExecutor")
    public TaskExecutor agentTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("agent-executor-");
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
    
    /**
     * 配置工作流专用任务执行器
     * 用于工作流引擎的异步任务
     */
    @Bean(name = "workflowTaskExecutor")
    public TaskExecutor workflowTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("workflow-engine-");
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
    
    /**
     * 配置任务调度器
     * 用于定时任务
     */
    @Bean(name = "taskScheduler")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("stock-agent-scheduling-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        
        scheduler.initialize();
        return scheduler;
    }
    
    /**
     * 配置CORS
     * 跨域资源共享配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://localhost:*",
            "https://127.0.0.1:*"
        ));
        
        // 允许的方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // 允许的头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 允许凭证
        configuration.setAllowCredentials(true);
        
        // 预检请求缓存时间
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    /**
     * 配置审计提供者
     * 用于JPA审计功能
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of(authentication.getName());
            }
            return Optional.of("system");
        };
    }
    
    /**
     * 应用配置属性
     */
    @ConfigurationProperties(prefix = "stock-agent")
    @Bean
    public StockAgentProperties stockAgentProperties() {
        return new StockAgentProperties();
    }
    
    /**
     * 应用配置属性类
     */
    public static class StockAgentProperties {
        
        private AgentConfig agents = new AgentConfig();
        private OrchestratorConfig orchestrator = new OrchestratorConfig();
        private WorkflowConfig workflow = new WorkflowConfig();
        private McpConfig mcp = new McpConfig();
        private WebSocketConfig websocket = new WebSocketConfig();
        private CacheConfig cache = new CacheConfig();
        private SecurityConfig security = new SecurityConfig();
        private MonitoringConfig monitoring = new MonitoringConfig();
        
        // Getters and Setters
        public AgentConfig getAgents() { return agents; }
        public void setAgents(AgentConfig agents) { this.agents = agents; }
        
        public OrchestratorConfig getOrchestrator() { return orchestrator; }
        public void setOrchestrator(OrchestratorConfig orchestrator) { this.orchestrator = orchestrator; }
        
        public WorkflowConfig getWorkflow() { return workflow; }
        public void setWorkflow(WorkflowConfig workflow) { this.workflow = workflow; }
        
        public McpConfig getMcp() { return mcp; }
        public void setMcp(McpConfig mcp) { this.mcp = mcp; }
        
        public WebSocketConfig getWebsocket() { return websocket; }
        public void setWebsocket(WebSocketConfig websocket) { this.websocket = websocket; }
        
        public CacheConfig getCache() { return cache; }
        public void setCache(CacheConfig cache) { this.cache = cache; }
        
        public SecurityConfig getSecurity() { return security; }
        public void setSecurity(SecurityConfig security) { this.security = security; }
        
        public MonitoringConfig getMonitoring() { return monitoring; }
        public void setMonitoring(MonitoringConfig monitoring) { this.monitoring = monitoring; }
        
        /**
         * 智能体配置
         */
        public static class AgentConfig {
            private java.util.Map<String, AgentSettings> agents = new java.util.HashMap<>();
            
            public java.util.Map<String, AgentSettings> getAgents() { return agents; }
            public void setAgents(java.util.Map<String, AgentSettings> agents) { this.agents = agents; }
            
            public static class AgentSettings {
                private boolean enabled = true;
                private int maxConcurrent = 5;
                private long timeout = 30000;
                
                public boolean isEnabled() { return enabled; }
                public void setEnabled(boolean enabled) { this.enabled = enabled; }
                
                public int getMaxConcurrent() { return maxConcurrent; }
                public void setMaxConcurrent(int maxConcurrent) { this.maxConcurrent = maxConcurrent; }
                
                public long getTimeout() { return timeout; }
                public void setTimeout(long timeout) { this.timeout = timeout; }
            }
        }
        
        /**
         * 协调器配置
         */
        public static class OrchestratorConfig {
            private TimeoutConfig timeout = new TimeoutConfig();
            private RetryConfig retry = new RetryConfig();
            private ExecutorConfig executor = new ExecutorConfig();
            
            public TimeoutConfig getTimeout() { return timeout; }
            public void setTimeout(TimeoutConfig timeout) { this.timeout = timeout; }
            
            public RetryConfig getRetry() { return retry; }
            public void setRetry(RetryConfig retry) { this.retry = retry; }
            
            public ExecutorConfig getExecutor() { return executor; }
            public void setExecutor(ExecutorConfig executor) { this.executor = executor; }
            
            public static class TimeoutConfig {
                private long overallTask = 120000;
                private long individualAgent = 60000;
                
                public long getOverallTask() { return overallTask; }
                public void setOverallTask(long overallTask) { this.overallTask = overallTask; }
                
                public long getIndividualAgent() { return individualAgent; }
                public void setIndividualAgent(long individualAgent) { this.individualAgent = individualAgent; }
            }
            
            public static class RetryConfig {
                private int maxAttempts = 3;
                private long delay = 1000;
                private double multiplier = 2.0;
                
                public int getMaxAttempts() { return maxAttempts; }
                public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
                
                public long getDelay() { return delay; }
                public void setDelay(long delay) { this.delay = delay; }
                
                public double getMultiplier() { return multiplier; }
                public void setMultiplier(double multiplier) { this.multiplier = multiplier; }
            }
            
            public static class ExecutorConfig {
                private int corePoolSize = 10;
                private int maxPoolSize = 50;
                private int queueCapacity = 200;
                private int keepAliveSeconds = 60;
                private String threadNamePrefix = "agent-executor-";
                
                public int getCorePoolSize() { return corePoolSize; }
                public void setCorePoolSize(int corePoolSize) { this.corePoolSize = corePoolSize; }
                
                public int getMaxPoolSize() { return maxPoolSize; }
                public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }
                
                public int getQueueCapacity() { return queueCapacity; }
                public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }
                
                public int getKeepAliveSeconds() { return keepAliveSeconds; }
                public void setKeepAliveSeconds(int keepAliveSeconds) { this.keepAliveSeconds = keepAliveSeconds; }
                
                public String getThreadNamePrefix() { return threadNamePrefix; }
                public void setThreadNamePrefix(String threadNamePrefix) { this.threadNamePrefix = threadNamePrefix; }
            }
        }
        
        /**
         * 工作流配置
         */
        public static class WorkflowConfig {
            private ExecutionConfig execution = new ExecutionConfig();
            private java.util.Map<String, NodeExecutorConfig> nodeExecutors = new java.util.HashMap<>();
            private EngineConfig engine = new EngineConfig();
            
            public ExecutionConfig getExecution() { return execution; }
            public void setExecution(ExecutionConfig execution) { this.execution = execution; }
            
            public java.util.Map<String, NodeExecutorConfig> getNodeExecutors() { return nodeExecutors; }
            public void setNodeExecutors(java.util.Map<String, NodeExecutorConfig> nodeExecutors) { this.nodeExecutors = nodeExecutors; }
            
            public EngineConfig getEngine() { return engine; }
            public void setEngine(EngineConfig engine) { this.engine = engine; }
            
            public static class ExecutionConfig {
                private int defaultTimeout = 300;
                private int maxConcurrentExecutions = 100;
                private int cleanupInterval = 3600;
                private int historyRetentionDays = 30;
                
                public int getDefaultTimeout() { return defaultTimeout; }
                public void setDefaultTimeout(int defaultTimeout) { this.defaultTimeout = defaultTimeout; }
                
                public int getMaxConcurrentExecutions() { return maxConcurrentExecutions; }
                public void setMaxConcurrentExecutions(int maxConcurrentExecutions) { this.maxConcurrentExecutions = maxConcurrentExecutions; }
                
                public int getCleanupInterval() { return cleanupInterval; }
                public void setCleanupInterval(int cleanupInterval) { this.cleanupInterval = cleanupInterval; }
                
                public int getHistoryRetentionDays() { return historyRetentionDays; }
                public void setHistoryRetentionDays(int historyRetentionDays) { this.historyRetentionDays = historyRetentionDays; }
            }
            
            public static class NodeExecutorConfig {
                private int timeout = 30;
                private int maxRetries = 3;
                private String maxMemory = "128MB";
                private java.util.List<String> allowedLanguages = new java.util.ArrayList<>();
                private int maxDelay = 3600;
                
                public int getTimeout() { return timeout; }
                public void setTimeout(int timeout) { this.timeout = timeout; }
                
                public int getMaxRetries() { return maxRetries; }
                public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
                
                public String getMaxMemory() { return maxMemory; }
                public void setMaxMemory(String maxMemory) { this.maxMemory = maxMemory; }
                
                public java.util.List<String> getAllowedLanguages() { return allowedLanguages; }
                public void setAllowedLanguages(java.util.List<String> allowedLanguages) { this.allowedLanguages = allowedLanguages; }
                
                public int getMaxDelay() { return maxDelay; }
                public void setMaxDelay(int maxDelay) { this.maxDelay = maxDelay; }
            }
            
            public static class EngineConfig {
                private int corePoolSize = 5;
                private int maxPoolSize = 20;
                private int queueCapacity = 100;
                private int keepAliveSeconds = 60;
                private String threadNamePrefix = "workflow-engine-";
                
                public int getCorePoolSize() { return corePoolSize; }
                public void setCorePoolSize(int corePoolSize) { this.corePoolSize = corePoolSize; }
                
                public int getMaxPoolSize() { return maxPoolSize; }
                public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }
                
                public int getQueueCapacity() { return queueCapacity; }
                public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }
                
                public int getKeepAliveSeconds() { return keepAliveSeconds; }
                public void setKeepAliveSeconds(int keepAliveSeconds) { this.keepAliveSeconds = keepAliveSeconds; }
                
                public String getThreadNamePrefix() { return threadNamePrefix; }
                public void setThreadNamePrefix(String threadNamePrefix) { this.threadNamePrefix = threadNamePrefix; }
            }
        }
        
        /**
         * MCP配置
         */
        public static class McpConfig {
            private java.util.Map<String, Long> timeout = new java.util.HashMap<>();
            private RetryConfig retry = new RetryConfig();
            private HealthCheckConfig healthCheck = new HealthCheckConfig();
            private java.util.Map<String, CategoryConfig> categories = new java.util.HashMap<>();
            
            public java.util.Map<String, Long> getTimeout() { return timeout; }
            public void setTimeout(java.util.Map<String, Long> timeout) { this.timeout = timeout; }
            
            public RetryConfig getRetry() { return retry; }
            public void setRetry(RetryConfig retry) { this.retry = retry; }
            
            public HealthCheckConfig getHealthCheck() { return healthCheck; }
            public void setHealthCheck(HealthCheckConfig healthCheck) { this.healthCheck = healthCheck; }
            
            public java.util.Map<String, CategoryConfig> getCategories() { return categories; }
            public void setCategories(java.util.Map<String, CategoryConfig> categories) { this.categories = categories; }
            
            public static class RetryConfig {
                private int maxAttempts = 3;
                private long delay = 1000;
                private double multiplier = 2.0;
                
                public int getMaxAttempts() { return maxAttempts; }
                public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
                
                public long getDelay() { return delay; }
                public void setDelay(long delay) { this.delay = delay; }
                
                public double getMultiplier() { return multiplier; }
                public void setMultiplier(double multiplier) { this.multiplier = multiplier; }
            }
            
            public static class HealthCheckConfig {
                private int interval = 60;
                private int timeout = 10;
                private boolean enabled = true;
                
                public int getInterval() { return interval; }
                public void setInterval(int interval) { this.interval = interval; }
                
                public int getTimeout() { return timeout; }
                public void setTimeout(int timeout) { this.timeout = timeout; }
                
                public boolean isEnabled() { return enabled; }
                public void setEnabled(boolean enabled) { this.enabled = enabled; }
            }
            
            public static class CategoryConfig {
                private boolean enabled = true;
                private int maxConcurrent = 10;
                
                public boolean isEnabled() { return enabled; }
                public void setEnabled(boolean enabled) { this.enabled = enabled; }
                
                public int getMaxConcurrent() { return maxConcurrent; }
                public void setMaxConcurrent(int maxConcurrent) { this.maxConcurrent = maxConcurrent; }
            }
        }
        
        /**
         * WebSocket配置
         */
        public static class WebSocketConfig {
            private boolean enabled = true;
            private long heartbeatInterval = 30000;
            private int maxSessions = 1000;
            private EndpointConfig analysis = new EndpointConfig();
            private EndpointConfig workflow = new EndpointConfig();
            
            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }
            
            public long getHeartbeatInterval() { return heartbeatInterval; }
            public void setHeartbeatInterval(long heartbeatInterval) { this.heartbeatInterval = heartbeatInterval; }
            
            public int getMaxSessions() { return maxSessions; }
            public void setMaxSessions(int maxSessions) { this.maxSessions = maxSessions; }
            
            public EndpointConfig getAnalysis() { return analysis; }
            public void setAnalysis(EndpointConfig analysis) { this.analysis = analysis; }
            
            public EndpointConfig getWorkflow() { return workflow; }
            public void setWorkflow(EndpointConfig workflow) { this.workflow = workflow; }
            
            public static class EndpointConfig {
                private String endpoint = "/ws";
                private int maxConnections = 500;
                private long messageSizeLimit = 1048576;
                private long progressUpdateInterval = 1000;
                
                public String getEndpoint() { return endpoint; }
                public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
                
                public int getMaxConnections() { return maxConnections; }
                public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
                
                public long getMessageSizeLimit() { return messageSizeLimit; }
                public void setMessageSizeLimit(long messageSizeLimit) { this.messageSizeLimit = messageSizeLimit; }
                
                public long getProgressUpdateInterval() { return progressUpdateInterval; }
                public void setProgressUpdateInterval(long progressUpdateInterval) { this.progressUpdateInterval = progressUpdateInterval; }
            }
        }
        
        /**
         * 缓存配置
         */
        public static class CacheConfig {
            private RedisConfig redis = new RedisConfig();
            private LocalConfig local = new LocalConfig();
            
            public RedisConfig getRedis() { return redis; }
            public void setRedis(RedisConfig redis) { this.redis = redis; }
            
            public LocalConfig getLocal() { return local; }
            public void setLocal(LocalConfig local) { this.local = local; }
            
            public static class RedisConfig {
                private long defaultTtl = 3600;
                private String keyPrefix = "stock-agent:";
                
                public long getDefaultTtl() { return defaultTtl; }
                public void setDefaultTtl(long defaultTtl) { this.defaultTtl = defaultTtl; }
                
                public String getKeyPrefix() { return keyPrefix; }
                public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
            }
            
            public static class LocalConfig {
                private int maxSize = 1000;
                private long expireAfterWrite = 300;
                private long expireAfterAccess = 600;
                
                public int getMaxSize() { return maxSize; }
                public void setMaxSize(int maxSize) { this.maxSize = maxSize; }
                
                public long getExpireAfterWrite() { return expireAfterWrite; }
                public void setExpireAfterWrite(long expireAfterWrite) { this.expireAfterWrite = expireAfterWrite; }
                
                public long getExpireAfterAccess() { return expireAfterAccess; }
                public void setExpireAfterAccess(long expireAfterAccess) { this.expireAfterAccess = expireAfterAccess; }
            }
        }
        
        /**
         * 安全配置
         */
        public static class SecurityConfig {
            private JwtConfig jwt = new JwtConfig();
            private CorsConfig cors = new CorsConfig();
            
            public JwtConfig getJwt() { return jwt; }
            public void setJwt(JwtConfig jwt) { this.jwt = jwt; }
            
            public CorsConfig getCors() { return cors; }
            public void setCors(CorsConfig cors) { this.cors = cors; }
            
            public static class JwtConfig {
                private String secret = "stock-agent-genie-secret-key-2024";
                private long expiration = 86400000;
                private long refreshExpiration = 604800000;
                
                public String getSecret() { return secret; }
                public void setSecret(String secret) { this.secret = secret; }
                
                public long getExpiration() { return expiration; }
                public void setExpiration(long expiration) { this.expiration = expiration; }
                
                public long getRefreshExpiration() { return refreshExpiration; }
                public void setRefreshExpiration(long refreshExpiration) { this.refreshExpiration = refreshExpiration; }
            }
            
            public static class CorsConfig {
                private java.util.List<String> allowedOrigins = new java.util.ArrayList<>();
                private java.util.List<String> allowedMethods = new java.util.ArrayList<>();
                private java.util.List<String> allowedHeaders = new java.util.ArrayList<>();
                private boolean allowCredentials = true;
                private long maxAge = 3600;
                
                public java.util.List<String> getAllowedOrigins() { return allowedOrigins; }
                public void setAllowedOrigins(java.util.List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
                
                public java.util.List<String> getAllowedMethods() { return allowedMethods; }
                public void setAllowedMethods(java.util.List<String> allowedMethods) { this.allowedMethods = allowedMethods; }
                
                public java.util.List<String> getAllowedHeaders() { return allowedHeaders; }
                public void setAllowedHeaders(java.util.List<String> allowedHeaders) { this.allowedHeaders = allowedHeaders; }
                
                public boolean isAllowCredentials() { return allowCredentials; }
                public void setAllowCredentials(boolean allowCredentials) { this.allowCredentials = allowCredentials; }
                
                public long getMaxAge() { return maxAge; }
                public void setMaxAge(long maxAge) { this.maxAge = maxAge; }
            }
        }
        
        /**
         * 监控配置
         */
        public static class MonitoringConfig {
            private PerformanceConfig performance = new PerformanceConfig();
            private AlertConfig alert = new AlertConfig();
            
            public PerformanceConfig getPerformance() { return performance; }
            public void setPerformance(PerformanceConfig performance) { this.performance = performance; }
            
            public AlertConfig getAlert() { return alert; }
            public void setAlert(AlertConfig alert) { this.alert = alert; }
            
            public static class PerformanceConfig {
                private boolean enabled = true;
                private long slowQueryThreshold = 1000;
                private long slowExecutionThreshold = 5000;
                
                public boolean isEnabled() { return enabled; }
                public void setEnabled(boolean enabled) { this.enabled = enabled; }
                
                public long getSlowQueryThreshold() { return slowQueryThreshold; }
                public void setSlowQueryThreshold(long slowQueryThreshold) { this.slowQueryThreshold = slowQueryThreshold; }
                
                public long getSlowExecutionThreshold() { return slowExecutionThreshold; }
                public void setSlowExecutionThreshold(long slowExecutionThreshold) { this.slowExecutionThreshold = slowExecutionThreshold; }
            }
            
            public static class AlertConfig {
                private boolean enabled = true;
                private double errorRateThreshold = 0.05;
                private long responseTimeThreshold = 3000;
                private double memoryUsageThreshold = 0.8;
                private double cpuUsageThreshold = 0.8;
                private String webhookUrl;
                private String emailRecipients;
                
                public boolean isEnabled() { return enabled; }
                public void setEnabled(boolean enabled) { this.enabled = enabled; }
                
                public double getErrorRateThreshold() { return errorRateThreshold; }
                public void setErrorRateThreshold(double errorRateThreshold) { this.errorRateThreshold = errorRateThreshold; }
                
                public long getResponseTimeThreshold() { return responseTimeThreshold; }
                public void setResponseTimeThreshold(long responseTimeThreshold) { this.responseTimeThreshold = responseTimeThreshold; }
                
                public double getMemoryUsageThreshold() { return memoryUsageThreshold; }
                public void setMemoryUsageThreshold(double memoryUsageThreshold) { this.memoryUsageThreshold = memoryUsageThreshold; }
                
                public double getCpuUsageThreshold() { return cpuUsageThreshold; }
                public void setCpuUsageThreshold(double cpuUsageThreshold) { this.cpuUsageThreshold = cpuUsageThreshold; }
                
                public String getWebhookUrl() { return webhookUrl; }
                public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
                
                public String getEmailRecipients() { return emailRecipients; }
                public void setEmailRecipients(String emailRecipients) { this.emailRecipients = emailRecipients; }
            }
        }
    }
}