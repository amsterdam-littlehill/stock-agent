package com.jd.genie.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executor;

/**
 * 智能体协作配置类
 * 管理协作引擎的各项配置参数
 * 
 * @author Stock-Agent Team
 * @version 1.0
 */
@Configuration
@EnableAsync
@EnableScheduling
@ConfigurationProperties(prefix = "collaboration")
public class CollaborationConfig {
    
    // 协作引擎配置
    private Engine engine = new Engine();
    
    // 智能体配置
    private Agent agent = new Agent();
    
    // 会话配置
    private Session session = new Session();
    
    // 调度配置
    private Schedule schedule = new Schedule();
    
    // 性能配置
    private Performance performance = new Performance();
    
    /**
     * 协作任务执行器
     */
    @Bean(name = "collaborationTaskExecutor")
    public Executor collaborationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(performance.getCorePoolSize());
        executor.setMaxPoolSize(performance.getMaxPoolSize());
        executor.setQueueCapacity(performance.getQueueCapacity());
        executor.setThreadNamePrefix("Collaboration-");
        executor.setKeepAliveSeconds(performance.getKeepAliveSeconds());
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    /**
     * 协作分析执行器
     */
    @Bean(name = "collaborationAnalysisExecutor")
    public Executor collaborationAnalysisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(performance.getAnalysisCorePoolSize());
        executor.setMaxPoolSize(performance.getAnalysisMaxPoolSize());
        executor.setQueueCapacity(performance.getAnalysisQueueCapacity());
        executor.setThreadNamePrefix("CollabAnalysis-");
        executor.setKeepAliveSeconds(60);
        executor.initialize();
        return executor;
    }
    
    // ==================== 配置类定义 ====================
    
    /**
     * 协作引擎配置
     */
    public static class Engine {
        private boolean enabled = true;
        private String version = "1.0";
        private int maxConcurrentSessions = 50;
        private int maxAgentsPerSession = 10;
        private long sessionTimeoutMs = 300000; // 5分钟
        private boolean enableMetrics = true;
        private boolean enableCache = true;
        private int cacheMaxSize = 1000;
        private long cacheTtlMs = 3600000; // 1小时
        
        // Getters and Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public int getMaxConcurrentSessions() { return maxConcurrentSessions; }
        public void setMaxConcurrentSessions(int maxConcurrentSessions) { this.maxConcurrentSessions = maxConcurrentSessions; }
        
        public int getMaxAgentsPerSession() { return maxAgentsPerSession; }
        public void setMaxAgentsPerSession(int maxAgentsPerSession) { this.maxAgentsPerSession = maxAgentsPerSession; }
        
        public long getSessionTimeoutMs() { return sessionTimeoutMs; }
        public void setSessionTimeoutMs(long sessionTimeoutMs) { this.sessionTimeoutMs = sessionTimeoutMs; }
        
        public boolean isEnableMetrics() { return enableMetrics; }
        public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }
        
        public boolean isEnableCache() { return enableCache; }
        public void setEnableCache(boolean enableCache) { this.enableCache = enableCache; }
        
        public int getCacheMaxSize() { return cacheMaxSize; }
        public void setCacheMaxSize(int cacheMaxSize) { this.cacheMaxSize = cacheMaxSize; }
        
        public long getCacheTtlMs() { return cacheTtlMs; }
        public void setCacheTtlMs(long cacheTtlMs) { this.cacheTtlMs = cacheTtlMs; }
    }
    
    /**
     * 智能体配置
     */
    public static class Agent {
        private int maxRegisteredAgents = 100;
        private long healthCheckIntervalMs = 300000; // 5分钟
        private double minConfidenceThreshold = 0.5;
        private int maxExperienceLevel = 10;
        private long responseTimeoutMs = 30000; // 30秒
        private boolean enableAutoRecovery = true;
        private int maxRetryAttempts = 3;
        
        // Getters and Setters
        public int getMaxRegisteredAgents() { return maxRegisteredAgents; }
        public void setMaxRegisteredAgents(int maxRegisteredAgents) { this.maxRegisteredAgents = maxRegisteredAgents; }
        
        public long getHealthCheckIntervalMs() { return healthCheckIntervalMs; }
        public void setHealthCheckIntervalMs(long healthCheckIntervalMs) { this.healthCheckIntervalMs = healthCheckIntervalMs; }
        
        public double getMinConfidenceThreshold() { return minConfidenceThreshold; }
        public void setMinConfidenceThreshold(double minConfidenceThreshold) { this.minConfidenceThreshold = minConfidenceThreshold; }
        
        public int getMaxExperienceLevel() { return maxExperienceLevel; }
        public void setMaxExperienceLevel(int maxExperienceLevel) { this.maxExperienceLevel = maxExperienceLevel; }
        
        public long getResponseTimeoutMs() { return responseTimeoutMs; }
        public void setResponseTimeoutMs(long responseTimeoutMs) { this.responseTimeoutMs = responseTimeoutMs; }
        
        public boolean isEnableAutoRecovery() { return enableAutoRecovery; }
        public void setEnableAutoRecovery(boolean enableAutoRecovery) { this.enableAutoRecovery = enableAutoRecovery; }
        
        public int getMaxRetryAttempts() { return maxRetryAttempts; }
        public void setMaxRetryAttempts(int maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }
    }
    
    /**
     * 会话配置
     */
    public static class Session {
        private int maxHistorySize = 1000;
        private long cleanupIntervalMs = 3600000; // 1小时
        private long maxSessionDurationMs = 1800000; // 30分钟
        private boolean enableSessionPersistence = true;
        private boolean enableInteractionLogging = true;
        private int maxInteractionsPerSession = 1000;
        
        // 协作模式配置
        private DebateConfig debate = new DebateConfig();
        private AnalysisConfig analysis = new AnalysisConfig();
        private PipelineConfig pipeline = new PipelineConfig();
        private ConsensusConfig consensus = new ConsensusConfig();
        
        // Getters and Setters
        public int getMaxHistorySize() { return maxHistorySize; }
        public void setMaxHistorySize(int maxHistorySize) { this.maxHistorySize = maxHistorySize; }
        
        public long getCleanupIntervalMs() { return cleanupIntervalMs; }
        public void setCleanupIntervalMs(long cleanupIntervalMs) { this.cleanupIntervalMs = cleanupIntervalMs; }
        
        public long getMaxSessionDurationMs() { return maxSessionDurationMs; }
        public void setMaxSessionDurationMs(long maxSessionDurationMs) { this.maxSessionDurationMs = maxSessionDurationMs; }
        
        public boolean isEnableSessionPersistence() { return enableSessionPersistence; }
        public void setEnableSessionPersistence(boolean enableSessionPersistence) { this.enableSessionPersistence = enableSessionPersistence; }
        
        public boolean isEnableInteractionLogging() { return enableInteractionLogging; }
        public void setEnableInteractionLogging(boolean enableInteractionLogging) { this.enableInteractionLogging = enableInteractionLogging; }
        
        public int getMaxInteractionsPerSession() { return maxInteractionsPerSession; }
        public void setMaxInteractionsPerSession(int maxInteractionsPerSession) { this.maxInteractionsPerSession = maxInteractionsPerSession; }
        
        public DebateConfig getDebate() { return debate; }
        public void setDebate(DebateConfig debate) { this.debate = debate; }
        
        public AnalysisConfig getAnalysis() { return analysis; }
        public void setAnalysis(AnalysisConfig analysis) { this.analysis = analysis; }
        
        public PipelineConfig getPipeline() { return pipeline; }
        public void setPipeline(PipelineConfig pipeline) { this.pipeline = pipeline; }
        
        public ConsensusConfig getConsensus() { return consensus; }
        public void setConsensus(ConsensusConfig consensus) { this.consensus = consensus; }
    }
    
    /**
     * 结构化辩论配置
     */
    public static class DebateConfig {
        private int maxRounds = 5;
        private int minParticipants = 2;
        private int maxParticipants = 8;
        private double consensusThreshold = 0.8;
        private long roundTimeoutMs = 60000; // 1分钟
        
        // Getters and Setters
        public int getMaxRounds() { return maxRounds; }
        public void setMaxRounds(int maxRounds) { this.maxRounds = maxRounds; }
        
        public int getMinParticipants() { return minParticipants; }
        public void setMinParticipants(int minParticipants) { this.minParticipants = minParticipants; }
        
        public int getMaxParticipants() { return maxParticipants; }
        public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }
        
        public double getConsensusThreshold() { return consensusThreshold; }
        public void setConsensusThreshold(double consensusThreshold) { this.consensusThreshold = consensusThreshold; }
        
        public long getRoundTimeoutMs() { return roundTimeoutMs; }
        public void setRoundTimeoutMs(long roundTimeoutMs) { this.roundTimeoutMs = roundTimeoutMs; }
    }
    
    /**
     * 并行分析配置
     */
    public static class AnalysisConfig {
        private int maxParallelTasks = 10;
        private long analysisTimeoutMs = 120000; // 2分钟
        private boolean enableResultAggregation = true;
        private double minValidResultRatio = 0.6;
        
        // Getters and Setters
        public int getMaxParallelTasks() { return maxParallelTasks; }
        public void setMaxParallelTasks(int maxParallelTasks) { this.maxParallelTasks = maxParallelTasks; }
        
        public long getAnalysisTimeoutMs() { return analysisTimeoutMs; }
        public void setAnalysisTimeoutMs(long analysisTimeoutMs) { this.analysisTimeoutMs = analysisTimeoutMs; }
        
        public boolean isEnableResultAggregation() { return enableResultAggregation; }
        public void setEnableResultAggregation(boolean enableResultAggregation) { this.enableResultAggregation = enableResultAggregation; }
        
        public double getMinValidResultRatio() { return minValidResultRatio; }
        public void setMinValidResultRatio(double minValidResultRatio) { this.minValidResultRatio = minValidResultRatio; }
    }
    
    /**
     * 顺序流水线配置
     */
    public static class PipelineConfig {
        private int maxSteps = 10;
        private long stepTimeoutMs = 90000; // 1.5分钟
        private boolean enableStepValidation = true;
        private boolean allowStepSkipping = false;
        
        // Getters and Setters
        public int getMaxSteps() { return maxSteps; }
        public void setMaxSteps(int maxSteps) { this.maxSteps = maxSteps; }
        
        public long getStepTimeoutMs() { return stepTimeoutMs; }
        public void setStepTimeoutMs(long stepTimeoutMs) { this.stepTimeoutMs = stepTimeoutMs; }
        
        public boolean isEnableStepValidation() { return enableStepValidation; }
        public void setEnableStepValidation(boolean enableStepValidation) { this.enableStepValidation = enableStepValidation; }
        
        public boolean isAllowStepSkipping() { return allowStepSkipping; }
        public void setAllowStepSkipping(boolean allowStepSkipping) { this.allowStepSkipping = allowStepSkipping; }
    }
    
    /**
     * 共识构建配置
     */
    public static class ConsensusConfig {
        private int maxIterations = 5;
        private double targetConsensus = 0.8;
        private double minConsensusImprovement = 0.1;
        private long iterationTimeoutMs = 120000; // 2分钟
        
        // Getters and Setters
        public int getMaxIterations() { return maxIterations; }
        public void setMaxIterations(int maxIterations) { this.maxIterations = maxIterations; }
        
        public double getTargetConsensus() { return targetConsensus; }
        public void setTargetConsensus(double targetConsensus) { this.targetConsensus = targetConsensus; }
        
        public double getMinConsensusImprovement() { return minConsensusImprovement; }
        public void setMinConsensusImprovement(double minConsensusImprovement) { this.minConsensusImprovement = minConsensusImprovement; }
        
        public long getIterationTimeoutMs() { return iterationTimeoutMs; }
        public void setIterationTimeoutMs(long iterationTimeoutMs) { this.iterationTimeoutMs = iterationTimeoutMs; }
    }
    
    /**
     * 调度配置
     */
    public static class Schedule {
        private boolean enableAutoCollaboration = true;
        private boolean enableMarketAnalysis = true;
        private boolean enableRiskAssessment = true;
        private boolean enableStrategyOptimization = true;
        private boolean enableSystemMaintenance = true;
        
        // 调度间隔配置
        private long healthCheckIntervalMs = 300000; // 5分钟
        private long marketAnalysisIntervalMs = 1800000; // 30分钟
        private long riskAssessmentIntervalMs = 3600000; // 1小时
        private String strategyOptimizationCron = "0 0 9 * * MON-FRI";
        private String systemMaintenanceCron = "0 0 2 * * *";
        
        // Getters and Setters
        public boolean isEnableAutoCollaboration() { return enableAutoCollaboration; }
        public void setEnableAutoCollaboration(boolean enableAutoCollaboration) { this.enableAutoCollaboration = enableAutoCollaboration; }
        
        public boolean isEnableMarketAnalysis() { return enableMarketAnalysis; }
        public void setEnableMarketAnalysis(boolean enableMarketAnalysis) { this.enableMarketAnalysis = enableMarketAnalysis; }
        
        public boolean isEnableRiskAssessment() { return enableRiskAssessment; }
        public void setEnableRiskAssessment(boolean enableRiskAssessment) { this.enableRiskAssessment = enableRiskAssessment; }
        
        public boolean isEnableStrategyOptimization() { return enableStrategyOptimization; }
        public void setEnableStrategyOptimization(boolean enableStrategyOptimization) { this.enableStrategyOptimization = enableStrategyOptimization; }
        
        public boolean isEnableSystemMaintenance() { return enableSystemMaintenance; }
        public void setEnableSystemMaintenance(boolean enableSystemMaintenance) { this.enableSystemMaintenance = enableSystemMaintenance; }
        
        public long getHealthCheckIntervalMs() { return healthCheckIntervalMs; }
        public void setHealthCheckIntervalMs(long healthCheckIntervalMs) { this.healthCheckIntervalMs = healthCheckIntervalMs; }
        
        public long getMarketAnalysisIntervalMs() { return marketAnalysisIntervalMs; }
        public void setMarketAnalysisIntervalMs(long marketAnalysisIntervalMs) { this.marketAnalysisIntervalMs = marketAnalysisIntervalMs; }
        
        public long getRiskAssessmentIntervalMs() { return riskAssessmentIntervalMs; }
        public void setRiskAssessmentIntervalMs(long riskAssessmentIntervalMs) { this.riskAssessmentIntervalMs = riskAssessmentIntervalMs; }
        
        public String getStrategyOptimizationCron() { return strategyOptimizationCron; }
        public void setStrategyOptimizationCron(String strategyOptimizationCron) { this.strategyOptimizationCron = strategyOptimizationCron; }
        
        public String getSystemMaintenanceCron() { return systemMaintenanceCron; }
        public void setSystemMaintenanceCron(String systemMaintenanceCron) { this.systemMaintenanceCron = systemMaintenanceCron; }
    }
    
    /**
     * 性能配置
     */
    public static class Performance {
        // 主线程池配置
        private int corePoolSize = 10;
        private int maxPoolSize = 50;
        private int queueCapacity = 200;
        private int keepAliveSeconds = 60;
        
        // 分析线程池配置
        private int analysisCorePoolSize = 5;
        private int analysisMaxPoolSize = 20;
        private int analysisQueueCapacity = 100;
        
        // 内存配置
        private int maxMemoryUsageMB = 512;
        private boolean enableMemoryMonitoring = true;
        
        // 性能监控
        private boolean enablePerformanceMetrics = true;
        private long metricsCollectionIntervalMs = 60000; // 1分钟
        
        // Getters and Setters
        public int getCorePoolSize() { return corePoolSize; }
        public void setCorePoolSize(int corePoolSize) { this.corePoolSize = corePoolSize; }
        
        public int getMaxPoolSize() { return maxPoolSize; }
        public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }
        
        public int getQueueCapacity() { return queueCapacity; }
        public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }
        
        public int getKeepAliveSeconds() { return keepAliveSeconds; }
        public void setKeepAliveSeconds(int keepAliveSeconds) { this.keepAliveSeconds = keepAliveSeconds; }
        
        public int getAnalysisCorePoolSize() { return analysisCorePoolSize; }
        public void setAnalysisCorePoolSize(int analysisCorePoolSize) { this.analysisCorePoolSize = analysisCorePoolSize; }
        
        public int getAnalysisMaxPoolSize() { return analysisMaxPoolSize; }
        public void setAnalysisMaxPoolSize(int analysisMaxPoolSize) { this.analysisMaxPoolSize = analysisMaxPoolSize; }
        
        public int getAnalysisQueueCapacity() { return analysisQueueCapacity; }
        public void setAnalysisQueueCapacity(int analysisQueueCapacity) { this.analysisQueueCapacity = analysisQueueCapacity; }
        
        public int getMaxMemoryUsageMB() { return maxMemoryUsageMB; }
        public void setMaxMemoryUsageMB(int maxMemoryUsageMB) { this.maxMemoryUsageMB = maxMemoryUsageMB; }
        
        public boolean isEnableMemoryMonitoring() { return enableMemoryMonitoring; }
        public void setEnableMemoryMonitoring(boolean enableMemoryMonitoring) { this.enableMemoryMonitoring = enableMemoryMonitoring; }
        
        public boolean isEnablePerformanceMetrics() { return enablePerformanceMetrics; }
        public void setEnablePerformanceMetrics(boolean enablePerformanceMetrics) { this.enablePerformanceMetrics = enablePerformanceMetrics; }
        
        public long getMetricsCollectionIntervalMs() { return metricsCollectionIntervalMs; }
        public void setMetricsCollectionIntervalMs(long metricsCollectionIntervalMs) { this.metricsCollectionIntervalMs = metricsCollectionIntervalMs; }
    }
    
    // ==================== 主配置类的 Getters and Setters ====================
    
    public Engine getEngine() { return engine; }
    public void setEngine(Engine engine) { this.engine = engine; }
    
    public Agent getAgent() { return agent; }
    public void setAgent(Agent agent) { this.agent = agent; }
    
    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }
    
    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }
    
    public Performance getPerformance() { return performance; }
    public void setPerformance(Performance performance) { this.performance = performance; }
}