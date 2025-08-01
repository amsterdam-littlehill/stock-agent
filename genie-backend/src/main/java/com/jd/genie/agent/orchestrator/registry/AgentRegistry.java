package com.jd.genie.agent.orchestrator.registry;

import com.jd.genie.agent.agent.stock.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 智能体注册管理器
 * 负责管理和发现所有可用的TradingAgents智能体
 * 
 * @author Stock-Agent Team
 * @since 2024-12-01
 */
@Slf4j
@Component
public class AgentRegistry {
    
    /**
     * 注册的智能体映射
     */
    private final Map<String, Object> registeredAgents = new ConcurrentHashMap<>();
    
    /**
     * 智能体元数据
     */
    private final Map<String, AgentMetadata> agentMetadata = new ConcurrentHashMap<>();
    
    /**
     * 智能体调用统计
     */
    private final Map<String, AgentStats> agentStats = new ConcurrentHashMap<>();
    
    /**
     * 智能体健康状态
     */
    private final Map<String, AgentHealth> agentHealth = new ConcurrentHashMap<>();
    
    // 注入各个智能体
    @Autowired
    private FundamentalAnalyst fundamentalAnalyst;
    
    @Autowired
    private TechnicalAnalyst technicalAnalyst;
    
    @Autowired
    private SentimentAnalyst sentimentAnalyst;
    
    @Autowired
    private RiskManager riskManager;
    
    @Autowired
    private QuantitativeAnalyst quantitativeAnalyst;
    
    @Autowired
    private MarketAnalyst marketAnalyst;
    
    @Autowired
    private NewsAnalyst newsAnalyst;
    
    @Autowired
    private ResearchAnalyst researchAnalyst;
    
    @Autowired
    private TradingExecutor tradingExecutor;
    
    @Autowired
    private InvestmentAdvisor investmentAdvisor;
    
    /**
     * 初始化注册所有智能体
     */
    @PostConstruct
    public void initializeAgents() {
        log.info("开始初始化智能体注册表...");
        
        // 注册基本面分析师
        registerAgent("fundamental", fundamentalAnalyst, AgentMetadata.builder()
            .name("基本面分析师")
            .description("负责公司财务分析、估值分析、行业分析和基本面评估")
            .category("分析师")
            .priority(8)
            .timeout(30000)
            .dependencies(Set.of())
            .capabilities(Set.of("财务分析", "估值分析", "行业分析", "基本面评估"))
            .build());
        
        // 注册技术分析师
        registerAgent("technical", technicalAnalyst, AgentMetadata.builder()
            .name("技术分析师")
            .description("负责技术指标分析、图表形态分析、趋势分析和技术面评估")
            .category("分析师")
            .priority(7)
            .timeout(20000)
            .dependencies(Set.of())
            .capabilities(Set.of("技术指标", "图表形态", "趋势分析", "技术面评估"))
            .build());
        
        // 注册情绪分析师
        registerAgent("sentiment", sentimentAnalyst, AgentMetadata.builder()
            .name("情绪分析师")
            .description("负责新闻情绪分析、市场情绪监测、舆情风险评估")
            .category("分析师")
            .priority(6)
            .timeout(25000)
            .dependencies(Set.of())
            .capabilities(Set.of("新闻分析", "情绪监测", "舆情评估", "情绪驱动因子"))
            .build());
        
        // 注册风险管理师
        registerAgent("risk", riskManager, AgentMetadata.builder()
            .name("风险管理师")
            .description("负责风险评估与量化、风险控制策略、投资组合风险分析")
            .category("分析师")
            .priority(9)
            .timeout(35000)
            .dependencies(Set.of())
            .capabilities(Set.of("风险评估", "风险控制", "组合分析", "风险预警"))
            .build());
        
        // 注册量化分析师
        registerAgent("quantitative", quantitativeAnalyst, AgentMetadata.builder()
            .name("量化分析师")
            .description("负责量化模型构建、算法交易策略、统计套利分析")
            .category("分析师")
            .priority(7)
            .timeout(40000)
            .dependencies(Set.of())
            .capabilities(Set.of("量化建模", "算法策略", "统计套利", "因子挖掘"))
            .build());
        
        // 注册市场分析师
        registerAgent("market", marketAnalyst, AgentMetadata.builder()
            .name("市场分析师")
            .description("负责宏观经济分析、行业趋势分析、市场情绪监测")
            .category("分析师")
            .priority(8)
            .timeout(30000)
            .dependencies(Set.of())
            .capabilities(Set.of("宏观分析", "行业趋势", "政策影响", "市场周期"))
            .build());
        
        // 注册新闻分析师
        registerAgent("news", newsAnalyst, AgentMetadata.builder()
            .name("新闻分析师")
            .description("负责财经新闻收集分析、公告解读、政策影响评估和市场事件分析")
            .category("分析师")
            .priority(7)
            .timeout(25000)
            .dependencies(Set.of())
            .capabilities(Set.of("新闻分析", "公告解读", "政策评估", "事件分析", "热度监测"))
            .build());
        
        // 注册研究分析师
        registerAgent("research", researchAnalyst, AgentMetadata.builder()
            .name("研究分析师")
            .description("负责行业研报收集分析、机构观点整理、深度研究报告和公司调研分析")
            .category("分析师")
            .priority(8)
            .timeout(35000)
            .dependencies(Set.of())
            .capabilities(Set.of("研报分析", "机构观点", "深度研究", "公司调研", "投资逻辑"))
            .build());
        
        // 注册交易执行师
        registerAgent("trading", tradingExecutor, AgentMetadata.builder()
            .name("交易执行师")
            .description("负责综合决策融合、投资建议生成、仓位管理策略和风险控制措施")
            .category("执行师")
            .priority(10)
            .timeout(45000)
            .dependencies(Set.of("fundamental", "technical", "sentiment", "risk", "news", "research"))
            .capabilities(Set.of("决策融合", "投资建议", "仓位管理", "风险控制", "执行时机"))
            .build());
        
        // 注册投资顾问
        registerAgent("advisor", investmentAdvisor, AgentMetadata.builder()
            .name("投资顾问")
            .description("综合各专业分析师意见，进行结构化辩论，生成最终投资建议")
            .category("顾问")
            .priority(9)
            .timeout(60000)
            .dependencies(Set.of("fundamental", "technical", "sentiment", "risk", "quantitative", "market", "news", "research", "trading"))
            .capabilities(Set.of("综合评估", "结构化辩论", "投资策略", "组合建议"))
            .build());
        
        log.info("智能体注册完成，共注册 {} 个智能体", registeredAgents.size());
        
        // 初始化健康检查
        initializeHealthChecks();
    }
    
    /**
     * 注册智能体
     */
    public void registerAgent(String agentType, Object agent, AgentMetadata metadata) {
        registeredAgents.put(agentType, agent);
        agentMetadata.put(agentType, metadata);
        agentStats.put(agentType, new AgentStats());
        agentHealth.put(agentType, AgentHealth.builder()
            .agentType(agentType)
            .status(HealthStatus.HEALTHY)
            .lastCheckTime(System.currentTimeMillis())
            .build());
        
        log.info("注册智能体: {} - {}", agentType, metadata.getName());
    }
    
    /**
     * 获取智能体
     */
    @SuppressWarnings("unchecked")
    public <T> T getAgent(String agentType, Class<T> agentClass) {
        Object agent = registeredAgents.get(agentType);
        if (agent != null && agentClass.isInstance(agent)) {
            return (T) agent;
        }
        return null;
    }
    
    /**
     * 获取智能体（不指定类型）
     */
    public Object getAgent(String agentType) {
        return registeredAgents.get(agentType);
    }
    
    /**
     * 检查智能体是否存在
     */
    public boolean hasAgent(String agentType) {
        return registeredAgents.containsKey(agentType);
    }
    
    /**
     * 获取所有注册的智能体类型
     */
    public Set<String> getAllAgentTypes() {
        return new HashSet<>(registeredAgents.keySet());
    }
    
    /**
     * 获取指定类别的智能体
     */
    public Set<String> getAgentsByCategory(String category) {
        return agentMetadata.entrySet().stream()
            .filter(entry -> category.equals(entry.getValue().getCategory()))
            .map(Map.Entry::getKey)
            .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }
    
    /**
     * 获取分析师智能体（排除投资顾问）
     */
    public Set<String> getAnalystAgents() {
        return getAgentsByCategory("分析师");
    }
    
    /**
     * 获取智能体元数据
     */
    public AgentMetadata getAgentMetadata(String agentType) {
        return agentMetadata.get(agentType);
    }
    
    /**
     * 获取智能体统计信息
     */
    public AgentStats getAgentStats(String agentType) {
        return agentStats.get(agentType);
    }
    
    /**
     * 获取智能体健康状态
     */
    public AgentHealth getAgentHealth(String agentType) {
        return agentHealth.get(agentType);
    }
    
    /**
     * 记录智能体调用
     */
    public void recordAgentCall(String agentType, long executionTime, boolean success) {
        AgentStats stats = agentStats.get(agentType);
        if (stats != null) {
            stats.recordCall(executionTime, success);
        }
    }
    
    /**
     * 更新智能体健康状态
     */
    public void updateAgentHealth(String agentType, HealthStatus status, String message) {
        AgentHealth health = agentHealth.get(agentType);
        if (health != null) {
            health.setStatus(status);
            health.setMessage(message);
            health.setLastCheckTime(System.currentTimeMillis());
            
            if (status != HealthStatus.HEALTHY) {
                log.warn("智能体 {} 健康状态异常: {} - {}", agentType, status, message);
            }
        }
    }
    
    /**
     * 检查智能体是否健康
     */
    public boolean isAgentHealthy(String agentType) {
        AgentHealth health = agentHealth.get(agentType);
        return health != null && health.getStatus() == HealthStatus.HEALTHY;
    }
    
    /**
     * 获取健康的智能体列表
     */
    public Set<String> getHealthyAgents() {
        return agentHealth.entrySet().stream()
            .filter(entry -> entry.getValue().getStatus() == HealthStatus.HEALTHY)
            .map(Map.Entry::getKey)
            .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }
    
    /**
     * 获取按优先级排序的智能体列表
     */
    public List<String> getAgentsByPriority() {
        return agentMetadata.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().getPriority(), e1.getValue().getPriority()))
            .map(Map.Entry::getKey)
            .toList();
    }
    
    /**
     * 检查智能体依赖关系
     */
    public boolean checkDependencies(String agentType, Set<String> availableAgents) {
        AgentMetadata metadata = agentMetadata.get(agentType);
        if (metadata == null || metadata.getDependencies().isEmpty()) {
            return true;
        }
        
        return availableAgents.containsAll(metadata.getDependencies());
    }
    
    /**
     * 获取智能体执行顺序（考虑依赖关系）
     */
    public List<String> getExecutionOrder(Set<String> requestedAgents) {
        List<String> executionOrder = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        
        // 拓扑排序处理依赖关系
        while (processed.size() < requestedAgents.size()) {
            boolean progress = false;
            
            for (String agentType : requestedAgents) {
                if (!processed.contains(agentType)) {
                    AgentMetadata metadata = agentMetadata.get(agentType);
                    if (metadata == null || processed.containsAll(metadata.getDependencies())) {
                        executionOrder.add(agentType);
                        processed.add(agentType);
                        progress = true;
                    }
                }
            }
            
            if (!progress) {
                // 存在循环依赖，按优先级添加剩余智能体
                requestedAgents.stream()
                    .filter(agent -> !processed.contains(agent))
                    .sorted((a1, a2) -> {
                        AgentMetadata m1 = agentMetadata.get(a1);
                        AgentMetadata m2 = agentMetadata.get(a2);
                        return Integer.compare(m2.getPriority(), m1.getPriority());
                    })
                    .forEach(agent -> {
                        executionOrder.add(agent);
                        processed.add(agent);
                    });
                break;
            }
        }
        
        return executionOrder;
    }
    
    /**
     * 初始化健康检查
     */
    private void initializeHealthChecks() {
        // 这里可以添加定期健康检查逻辑
        log.info("智能体健康检查初始化完成");
    }
    
    /**
     * 获取注册表状态
     */
    public RegistryStatus getRegistryStatus() {
        int totalAgents = registeredAgents.size();
        long healthyAgents = agentHealth.values().stream()
            .mapToLong(health -> health.getStatus() == HealthStatus.HEALTHY ? 1 : 0)
            .sum();
        
        long totalCalls = agentStats.values().stream()
            .mapToLong(stats -> stats.getTotalCalls().get())
            .sum();
        
        double avgSuccessRate = agentStats.values().stream()
            .mapToDouble(AgentStats::getSuccessRate)
            .average()
            .orElse(0.0);
        
        return RegistryStatus.builder()
            .totalAgents(totalAgents)
            .healthyAgents((int) healthyAgents)
            .totalCalls(totalCalls)
            .averageSuccessRate(avgSuccessRate)
            .lastUpdateTime(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 智能体元数据
     */
    @lombok.Data
    @lombok.Builder
    public static class AgentMetadata {
        private String name;
        private String description;
        private String category;
        private int priority;
        private long timeout;
        private Set<String> dependencies;
        private Set<String> capabilities;
        private String version;
        
        @lombok.Builder.Default
        private long createTime = System.currentTimeMillis();
    }
    
    /**
     * 智能体统计信息
     */
    @lombok.Data
    public static class AgentStats {
        private final AtomicLong totalCalls = new AtomicLong(0);
        private final AtomicLong successfulCalls = new AtomicLong(0);
        private final AtomicLong failedCalls = new AtomicLong(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private volatile long lastCallTime = 0;
        private volatile long minExecutionTime = Long.MAX_VALUE;
        private volatile long maxExecutionTime = 0;
        
        public void recordCall(long executionTime, boolean success) {
            totalCalls.incrementAndGet();
            totalExecutionTime.addAndGet(executionTime);
            lastCallTime = System.currentTimeMillis();
            
            if (executionTime < minExecutionTime) {
                minExecutionTime = executionTime;
            }
            if (executionTime > maxExecutionTime) {
                maxExecutionTime = executionTime;
            }
            
            if (success) {
                successfulCalls.incrementAndGet();
            } else {
                failedCalls.incrementAndGet();
            }
        }
        
        public double getSuccessRate() {
            long total = totalCalls.get();
            return total > 0 ? (double) successfulCalls.get() / total : 0.0;
        }
        
        public double getAverageExecutionTime() {
            long total = totalCalls.get();
            return total > 0 ? (double) totalExecutionTime.get() / total : 0.0;
        }
    }
    
    /**
     * 智能体健康状态
     */
    @lombok.Data
    @lombok.Builder
    public static class AgentHealth {
        private String agentType;
        private HealthStatus status;
        private String message;
        private long lastCheckTime;
        private int consecutiveFailures;
    }
    
    /**
     * 健康状态枚举
     */
    public enum HealthStatus {
        HEALTHY("健康"),
        DEGRADED("降级"),
        UNHEALTHY("不健康"),
        UNKNOWN("未知");
        
        private final String description;
        
        HealthStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 注册表状态
     */
    @lombok.Data
    @lombok.Builder
    public static class RegistryStatus {
        private int totalAgents;
        private int healthyAgents;
        private long totalCalls;
        private double averageSuccessRate;
        private long lastUpdateTime;
    }
}