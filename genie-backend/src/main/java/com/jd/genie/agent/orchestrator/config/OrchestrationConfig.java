package com.jd.genie.agent.orchestrator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 智能体协调器配置
 * 管理协调器的线程池、超时、重试等配置参数
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Configuration
@EnableAsync
@EnableScheduling
@ConfigurationProperties(prefix = "stock-agent.orchestrator")
@Data
public class OrchestrationConfig {
    
    /**
     * 线程池配置
     */
    private ThreadPoolConfig threadPool = new ThreadPoolConfig();
    
    /**
     * 超时配置
     */
    private TimeoutConfig timeout = new TimeoutConfig();
    
    /**
     * 重试配置
     */
    private RetryConfig retry = new RetryConfig();
    
    /**
     * 监控配置
     */
    private MonitorConfig monitor = new MonitorConfig();
    
    /**
     * 缓存配置
     */
    private CacheConfig cache = new CacheConfig();
    
    /**
     * 限流配置
     */
    private RateLimitConfig rateLimit = new RateLimitConfig();
    
    /**
     * 熔断器配置
     */
    private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();
    
    /**
     * 辩论配置
     */
    private DebateConfig debate = new DebateConfig();
    
    /**
     * 创建智能体执行线程池
     */
    @Bean(name = "agentExecutor")
    public Executor agentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPool.getCorePoolSize());
        executor.setMaxPoolSize(threadPool.getMaxPoolSize());
        executor.setQueueCapacity(threadPool.getQueueCapacity());
        executor.setKeepAliveSeconds(threadPool.getKeepAliveSeconds());
        executor.setThreadNamePrefix("agent-exec-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
    
    /**
     * 创建监控线程池
     */
    @Bean(name = "monitorExecutor")
    public Executor monitorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("monitor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.initialize();
        return executor;
    }
    
    /**
     * 线程池配置
     */
    @Data
    public static class ThreadPoolConfig {
        /**
         * 核心线程数
         */
        private int corePoolSize = 10;
        
        /**
         * 最大线程数
         */
        private int maxPoolSize = 50;
        
        /**
         * 队列容量
         */
        private int queueCapacity = 200;
        
        /**
         * 线程空闲时间（秒）
         */
        private int keepAliveSeconds = 60;
        
        /**
         * 是否允许核心线程超时
         */
        private boolean allowCoreThreadTimeOut = false;
    }
    
    /**
     * 超时配置
     */
    @Data
    public static class TimeoutConfig {
        /**
         * 默认智能体执行超时时间（毫秒）
         */
        private long defaultAgentTimeout = 30000;
        
        /**
         * 投资顾问执行超时时间（毫秒）
         */
        private long advisorTimeout = 60000;
        
        /**
         * 整体任务超时时间（毫秒）
         */
        private long overallTaskTimeout = 120000;
        
        /**
         * 辩论超时时间（毫秒）
         */
        private long debateTimeout = 45000;
        
        /**
         * 网络请求超时时间（毫秒）
         */
        private long networkTimeout = 10000;
    }
    
    /**
     * 重试配置
     */
    @Data
    public static class RetryConfig {
        /**
         * 是否启用重试
         */
        private boolean enabled = true;
        
        /**
         * 最大重试次数
         */
        private int maxAttempts = 3;
        
        /**
         * 重试间隔（毫秒）
         */
        private long retryInterval = 1000;
        
        /**
         * 重试间隔倍数
         */
        private double retryMultiplier = 2.0;
        
        /**
         * 最大重试间隔（毫秒）
         */
        private long maxRetryInterval = 10000;
        
        /**
         * 可重试的异常类型
         */
        private String[] retryableExceptions = {
            "java.net.SocketTimeoutException",
            "java.net.ConnectException",
            "org.springframework.web.client.ResourceAccessException"
        };
    }
    
    /**
     * 监控配置
     */
    @Data
    public static class MonitorConfig {
        /**
         * 是否启用监控
         */
        private boolean enabled = true;
        
        /**
         * 健康检查间隔（毫秒）
         */
        private long healthCheckInterval = 60000;
        
        /**
         * 指标收集间隔（毫秒）
         */
        private long metricsCollectionInterval = 30000;
        
        /**
         * 告警检查间隔（毫秒）
         */
        private long alertCheckInterval = 10000;
        
        /**
         * 历史数据保留时间（毫秒）
         */
        private long historyRetentionTime = 86400000; // 24小时
        
        /**
         * 是否启用性能分析
         */
        private boolean enableProfiling = false;
    }
    
    /**
     * 缓存配置
     */
    @Data
    public static class CacheConfig {
        /**
         * 是否启用缓存
         */
        private boolean enabled = true;
        
        /**
         * 缓存大小
         */
        private int maxSize = 1000;
        
        /**
         * 缓存过期时间（毫秒）
         */
        private long expireAfterWrite = 300000; // 5分钟
        
        /**
         * 缓存访问过期时间（毫秒）
         */
        private long expireAfterAccess = 600000; // 10分钟
        
        /**
         * 是否启用缓存统计
         */
        private boolean enableStats = true;
    }
    
    /**
     * 限流配置
     */
    @Data
    public static class RateLimitConfig {
        /**
         * 是否启用限流
         */
        private boolean enabled = true;
        
        /**
         * 每秒最大请求数
         */
        private int maxRequestsPerSecond = 100;
        
        /**
         * 每分钟最大请求数
         */
        private int maxRequestsPerMinute = 1000;
        
        /**
         * 每小时最大请求数
         */
        private int maxRequestsPerHour = 10000;
        
        /**
         * 限流算法类型
         */
        private RateLimitAlgorithm algorithm = RateLimitAlgorithm.TOKEN_BUCKET;
        
        /**
         * 令牌桶容量
         */
        private int bucketCapacity = 200;
        
        /**
         * 令牌补充速率（每秒）
         */
        private int refillRate = 100;
    }
    
    /**
     * 熔断器配置
     */
    @Data
    public static class CircuitBreakerConfig {
        /**
         * 是否启用熔断器
         */
        private boolean enabled = true;
        
        /**
         * 失败率阈值（百分比）
         */
        private double failureRateThreshold = 50.0;
        
        /**
         * 慢调用率阈值（百分比）
         */
        private double slowCallRateThreshold = 50.0;
        
        /**
         * 慢调用时间阈值（毫秒）
         */
        private long slowCallDurationThreshold = 10000;
        
        /**
         * 最小调用数
         */
        private int minimumNumberOfCalls = 10;
        
        /**
         * 滑动窗口大小
         */
        private int slidingWindowSize = 100;
        
        /**
         * 等待时间（毫秒）
         */
        private long waitDurationInOpenState = 60000;
        
        /**
         * 半开状态允许的调用数
         */
        private int permittedNumberOfCallsInHalfOpenState = 10;
    }
    
    /**
     * 辩论配置
     */
    @Data
    public static class DebateConfig {
        /**
         * 是否启用辩论
         */
        private boolean enabled = true;
        
        /**
         * 默认辩论轮数
         */
        private int defaultRounds = 3;
        
        /**
         * 最大辩论轮数
         */
        private int maxRounds = 5;
        
        /**
         * 每轮辩论超时时间（毫秒）
         */
        private long roundTimeout = 15000;
        
        /**
         * 辩论参与者最少数量
         */
        private int minParticipants = 3;
        
        /**
         * 辩论参与者最多数量
         */
        private int maxParticipants = 6;
        
        /**
         * 共识阈值（百分比）
         */
        private double consensusThreshold = 70.0;
        
        /**
         * 是否启用辩论记录
         */
        private boolean enableLogging = true;
    }
    
    /**
     * 限流算法枚举
     */
    public enum RateLimitAlgorithm {
        /**
         * 令牌桶算法
         */
        TOKEN_BUCKET,
        
        /**
         * 漏桶算法
         */
        LEAKY_BUCKET,
        
        /**
         * 固定窗口算法
         */
        FIXED_WINDOW,
        
        /**
         * 滑动窗口算法
         */
        SLIDING_WINDOW
    }
    
    /**
     * 获取智能体特定的超时时间
     */
    public long getAgentTimeout(String agentType) {
        return switch (agentType) {
            case "advisor" -> timeout.getAdvisorTimeout();
            case "quantitative" -> timeout.getDefaultAgentTimeout() + 10000; // 量化分析需要更多时间
            case "risk" -> timeout.getDefaultAgentTimeout() + 5000; // 风险分析需要稍多时间
            default -> timeout.getDefaultAgentTimeout();
        };
    }
    
    /**
     * 获取智能体特定的重试次数
     */
    public int getAgentRetryAttempts(String agentType) {
        return switch (agentType) {
            case "advisor" -> retry.getMaxAttempts() - 1; // 投资顾问减少重试次数
            case "sentiment" -> retry.getMaxAttempts() + 1; // 情绪分析增加重试次数
            default -> retry.getMaxAttempts();
        };
    }
    
    /**
     * 检查配置有效性
     */
    public void validateConfig() {
        // 线程池配置验证
        if (threadPool.getCorePoolSize() <= 0) {
            throw new IllegalArgumentException("核心线程数必须大于0");
        }
        if (threadPool.getMaxPoolSize() < threadPool.getCorePoolSize()) {
            throw new IllegalArgumentException("最大线程数不能小于核心线程数");
        }
        
        // 超时配置验证
        if (timeout.getDefaultAgentTimeout() <= 0) {
            throw new IllegalArgumentException("智能体超时时间必须大于0");
        }
        if (timeout.getOverallTaskTimeout() < timeout.getDefaultAgentTimeout()) {
            throw new IllegalArgumentException("整体任务超时时间不能小于智能体超时时间");
        }
        
        // 重试配置验证
        if (retry.getMaxAttempts() < 1) {
            throw new IllegalArgumentException("最大重试次数必须至少为1");
        }
        if (retry.getRetryInterval() < 0) {
            throw new IllegalArgumentException("重试间隔不能为负数");
        }
        
        // 限流配置验证
        if (rateLimit.getMaxRequestsPerSecond() <= 0) {
            throw new IllegalArgumentException("每秒最大请求数必须大于0");
        }
        
        // 熔断器配置验证
        if (circuitBreaker.getFailureRateThreshold() < 0 || circuitBreaker.getFailureRateThreshold() > 100) {
            throw new IllegalArgumentException("失败率阈值必须在0-100之间");
        }
        
        // 辩论配置验证
        if (debate.getDefaultRounds() < 1 || debate.getDefaultRounds() > debate.getMaxRounds()) {
            throw new IllegalArgumentException("默认辩论轮数必须在1到最大轮数之间");
        }
    }
    
    /**
     * 获取配置摘要
     */
    public String getConfigSummary() {
        return String.format(
            "OrchestrationConfig{" +
            "threadPool: core=%d, max=%d, queue=%d; " +
            "timeout: agent=%dms, overall=%dms; " +
            "retry: enabled=%s, maxAttempts=%d; " +
            "monitor: enabled=%s; " +
            "rateLimit: enabled=%s, maxRps=%d; " +
            "circuitBreaker: enabled=%s; " +
            "debate: enabled=%s, rounds=%d" +
            "}",
            threadPool.getCorePoolSize(), threadPool.getMaxPoolSize(), threadPool.getQueueCapacity(),
            timeout.getDefaultAgentTimeout(), timeout.getOverallTaskTimeout(),
            retry.isEnabled(), retry.getMaxAttempts(),
            monitor.isEnabled(),
            rateLimit.isEnabled(), rateLimit.getMaxRequestsPerSecond(),
            circuitBreaker.isEnabled(),
            debate.isEnabled(), debate.getDefaultRounds()
        );
    }
}