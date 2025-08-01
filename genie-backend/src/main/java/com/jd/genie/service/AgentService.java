package com.jd.genie.service;

import com.jd.genie.entity.Agent;
import com.jd.genie.entity.AnalysisTask;
import com.jd.genie.entity.AnalysisResult;
import com.jd.genie.repository.AgentRepository;
import com.jd.genie.repository.AnalysisResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能体服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {
    
    private final AgentRepository agentRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final StockDataService stockDataService;
    
    /**
     * 获取所有可用智能体
     */
    @Cacheable(value = "available-agents")
    public List<Agent> getAvailableAgents() {
        log.debug("获取所有可用智能体");
        return agentRepository.findAvailableAgents();
    }
    
    /**
     * 根据类型获取智能体
     */
    @Cacheable(value = "agents-by-type", key = "#agentType")
    public List<Agent> getAgentsByType(String agentType) {
        log.debug("根据类型获取智能体: {}", agentType);
        return agentRepository.findByAgentTypeAndEnabledTrue(agentType);
    }
    
    /**
     * 获取智能体详情
     */
    @Cacheable(value = "agent-detail", key = "#agentId")
    public Optional<Agent> getAgentDetail(Long agentId) {
        log.debug("获取智能体详情: {}", agentId);
        return agentRepository.findById(agentId);
    }
    
    /**
     * 根据名称获取智能体
     */
    @Cacheable(value = "agent-by-name", key = "#agentName")
    public Optional<Agent> getAgentByName(String agentName) {
        log.debug("根据名称获取智能体: {}", agentName);
        return agentRepository.findByAgentName(agentName);
    }
    
    /**
     * 获取最佳智能体
     */
    @Cacheable(value = "best-agents", key = "#agentType + '_' + #limit")
    public List<Agent> getBestAgents(String agentType, int limit) {
        log.debug("获取最佳智能体: {} limit {}", agentType, limit);
        
        Pageable pageable = PageRequest.of(0, limit);
        
        if (StringUtils.hasText(agentType)) {
            return agentRepository.findTopPerformingAgentsByType(agentType, pageable);
        } else {
            return agentRepository.findTopPerformingAgents(pageable);
        }
    }
    
    /**
     * 获取智能体性能统计
     */
    @Cacheable(value = "agent-performance", key = "#agentId")
    public Map<String, Object> getAgentPerformance(Long agentId) {
        log.debug("获取智能体性能统计: {}", agentId);
        
        Optional<Agent> agentOpt = agentRepository.findById(agentId);
        if (!agentOpt.isPresent()) {
            return Collections.emptyMap();
        }
        
        Agent agent = agentOpt.get();
        Map<String, Object> performance = new HashMap<>();
        
        // 基本信息
        performance.put("agentName", agent.getAgentName());
        performance.put("agentType", agent.getAgentType());
        performance.put("status", agent.getStatus());
        
        // 性能指标
        performance.put("successRate", agent.getSuccessRate());
        performance.put("averageResponseTime", agent.getAverageResponseTime());
        performance.put("totalTasks", agent.getTotalTasks());
        performance.put("completedTasks", agent.getCompletedTasks());
        performance.put("failedTasks", agent.getFailedTasks());
        performance.put("averageConfidence", agent.getAverageConfidence());
        performance.put("averageAccuracy", agent.getAverageAccuracy());
        
        // 负载信息
        performance.put("currentConcurrency", agent.getCurrentConcurrency());
        performance.put("maxConcurrency", agent.getMaxConcurrency());
        performance.put("loadRate", agent.getLoadRate());
        
        // 健康状态
        performance.put("isHealthy", agent.isHealthy());
        performance.put("lastActiveTime", agent.getLastActiveTime());
        performance.put("lastTaskTime", agent.getLastTaskTime());
        
        return performance;
    }
    
    /**
     * 获取系统智能体统计
     */
    @Cacheable(value = "system-agent-stats")
    public Map<String, Object> getSystemAgentStatistics() {
        log.debug("获取系统智能体统计");
        
        Map<String, Object> stats = new HashMap<>();
        
        // 总智能体数
        long totalAgents = agentRepository.count();
        stats.put("totalAgents", totalAgents);
        
        // 各状态智能体数
        long activeAgents = agentRepository.countByStatus("ACTIVE");
        long busyAgents = agentRepository.countByStatus("BUSY");
        long inactiveAgents = agentRepository.countByStatus("INACTIVE");
        long errorAgents = agentRepository.countByStatus("ERROR");
        
        stats.put("activeAgents", activeAgents);
        stats.put("busyAgents", busyAgents);
        stats.put("inactiveAgents", inactiveAgents);
        stats.put("errorAgents", errorAgents);
        
        // 各类型智能体数
        List<Object[]> typeStats = agentRepository.countByAgentType();
        Map<String, Long> agentTypeStats = typeStats.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
        stats.put("agentTypeStats", agentTypeStats);
        
        // 系统整体性能
        Object[] systemPerf = agentRepository.getSystemPerformanceStats();
        if (systemPerf != null && systemPerf.length >= 3) {
            stats.put("systemSuccessRate", systemPerf[0]);
            stats.put("systemAvgResponseTime", systemPerf[1]);
            stats.put("systemAvgConfidence", systemPerf[2]);
        }
        
        return stats;
    }
    
    /**
     * 执行分析
     */
    @Transactional
    public AnalysisResult executeAnalysis(String agentType, AnalysisTask task) {
        log.info("执行分析: {} for task {}", agentType, task.getId());
        
        // 获取可用的智能体
        List<Agent> agents = agentRepository.findAvailableAgentsByType(agentType);
        if (agents.isEmpty()) {
            log.warn("没有可用的{}智能体", agentType);
            return null;
        }
        
        // 选择最佳智能体
        Agent selectedAgent = selectBestAgent(agents);
        
        // 增加并发数
        selectedAgent.incrementConcurrency();
        selectedAgent.updateLastActiveTime();
        agentRepository.save(selectedAgent);
        
        try {
            // 执行分析
            AnalysisResult result = performAnalysis(selectedAgent, task);
            
            // 更新智能体统计
            updateAgentStatistics(selectedAgent, true, result.getProcessingTime(), result.getConfidenceScore());
            
            return result;
            
        } catch (Exception e) {
            log.error("智能体分析失败: {} {}", selectedAgent.getAgentName(), task.getId(), e);
            
            // 更新智能体统计
            updateAgentStatistics(selectedAgent, false, 0L, null);
            
            throw e;
        } finally {
            // 减少并发数
            selectedAgent.decrementConcurrency();
            agentRepository.save(selectedAgent);
        }
    }
    
    /**
     * 创建或更新智能体
     */
    @Transactional
    public Agent saveOrUpdateAgent(Agent agent) {
        log.info("保存或更新智能体: {}", agent.getAgentName());
        
        Optional<Agent> existing = agentRepository.findByAgentName(agent.getAgentName());
        if (existing.isPresent()) {
            Agent existingAgent = existing.get();
            
            // 更新配置
            existingAgent.setDescription(agent.getDescription());
            existingAgent.setVersion(agent.getVersion());
            existingAgent.setSpecialization(agent.getSpecialization());
            existingAgent.setSupportedAnalysisTypes(agent.getSupportedAnalysisTypes());
            existingAgent.setConfigurationParameters(agent.getConfigurationParameters());
            existingAgent.setSystemPrompt(agent.getSystemPrompt());
            existingAgent.setMaxConcurrency(agent.getMaxConcurrency());
            existingAgent.setTimeout(agent.getTimeout());
            existingAgent.setPriority(agent.getPriority());
            existingAgent.setWeight(agent.getWeight());
            existingAgent.setApiEndpoint(agent.getApiEndpoint());
            existingAgent.setApiKey(agent.getApiKey());
            existingAgent.setModelName(agent.getModelName());
            existingAgent.setTemperature(agent.getTemperature());
            existingAgent.setMaxTokens(agent.getMaxTokens());
            existingAgent.setEnabled(agent.getEnabled());
            existingAgent.setRemarks(agent.getRemarks());
            existingAgent.setUpdatedAt(LocalDateTime.now());
            
            return agentRepository.save(existingAgent);
        } else {
            agent.setCreatedAt(LocalDateTime.now());
            agent.setUpdatedAt(LocalDateTime.now());
            return agentRepository.save(agent);
        }
    }
    
    /**
     * 启用/禁用智能体
     */
    @Transactional
    public boolean toggleAgentStatus(Long agentId, boolean enabled) {
        log.info("切换智能体状态: {} {}", agentId, enabled);
        
        Optional<Agent> agentOpt = agentRepository.findById(agentId);
        if (agentOpt.isPresent()) {
            Agent agent = agentOpt.get();
            agent.setEnabled(enabled);
            agent.setStatus(enabled ? "ACTIVE" : "INACTIVE");
            agent.setUpdatedAt(LocalDateTime.now());
            agentRepository.save(agent);
            return true;
        }
        
        return false;
    }
    
    /**
     * 重置智能体统计
     */
    @Transactional
    public boolean resetAgentStatistics(Long agentId) {
        log.info("重置智能体统计: {}", agentId);
        
        Optional<Agent> agentOpt = agentRepository.findById(agentId);
        if (agentOpt.isPresent()) {
            Agent agent = agentOpt.get();
            agent.resetStatistics();
            agentRepository.save(agent);
            return true;
        }
        
        return false;
    }
    
    /**
     * 健康检查
     */
    @Transactional
    public Map<String, Object> performHealthCheck() {
        log.info("执行智能体健康检查");
        
        Map<String, Object> healthReport = new HashMap<>();
        List<Agent> allAgents = agentRepository.findAll();
        
        int healthyCount = 0;
        int unhealthyCount = 0;
        List<Map<String, Object>> unhealthyAgents = new ArrayList<>();
        
        for (Agent agent : allAgents) {
            boolean isHealthy = checkAgentHealth(agent);
            
            if (isHealthy) {
                healthyCount++;
                if (!"ACTIVE".equals(agent.getStatus())) {
                    agent.setStatus("ACTIVE");
                    agentRepository.save(agent);
                }
            } else {
                unhealthyCount++;
                agent.setStatus("ERROR");
                agentRepository.save(agent);
                
                Map<String, Object> unhealthyInfo = new HashMap<>();
                unhealthyInfo.put("agentName", agent.getAgentName());
                unhealthyInfo.put("agentType", agent.getAgentType());
                unhealthyInfo.put("lastActiveTime", agent.getLastActiveTime());
                unhealthyInfo.put("errorReason", "健康检查失败");
                unhealthyAgents.add(unhealthyInfo);
            }
        }
        
        healthReport.put("totalAgents", allAgents.size());
        healthReport.put("healthyAgents", healthyCount);
        healthReport.put("unhealthyAgents", unhealthyCount);
        healthReport.put("healthRate", allAgents.size() > 0 ? (double) healthyCount / allAgents.size() * 100 : 0);
        healthReport.put("unhealthyDetails", unhealthyAgents);
        healthReport.put("checkTime", LocalDateTime.now());
        
        return healthReport;
    }
    
    /**
     * 获取智能体分析历史
     */
    public Page<AnalysisResult> getAgentAnalysisHistory(Long agentId, Pageable pageable) {
        log.debug("获取智能体分析历史: {} {}", agentId, pageable);
        
        Optional<Agent> agentOpt = agentRepository.findById(agentId);
        if (agentOpt.isPresent()) {
            String agentName = agentOpt.get().getAgentName();
            return analysisResultRepository.findByAgentTypeOrderByCreatedAtDesc(agentName, pageable);
        }
        
        return Page.empty();
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 选择最佳智能体
     */
    private Agent selectBestAgent(List<Agent> agents) {
        // 根据性能评分、负载率、响应时间等因素选择最佳智能体
        return agents.stream()
                .filter(Agent::isAvailable)
                .min(Comparator
                        .comparing(Agent::getLoadRate)
                        .thenComparing(agent -> -agent.getPerformanceScore())
                        .thenComparing(Agent::getAverageResponseTime))
                .orElse(agents.get(0));
    }
    
    /**
     * 执行分析
     */
    private AnalysisResult performAnalysis(Agent agent, AnalysisTask task) {
        log.info("智能体{}执行分析: {}", agent.getAgentName(), task.getStockCode());
        
        long startTime = System.currentTimeMillis();
        
        // 创建分析结果
        AnalysisResult result = new AnalysisResult();
        result.setTaskId(task.getId());
        result.setStockCode(task.getStockCode());
        result.setAgentType(agent.getAgentName());
        result.setAnalysisType(task.getAnalysisType());
        result.setDataSource("AGENT_" + agent.getAgentName());
        result.setVersion(agent.getVersion());
        result.setCreatedAt(LocalDateTime.now());
        
        try {
            // 根据智能体类型执行不同的分析逻辑
            switch (agent.getAgentType()) {
                case "TECHNICAL_ANALYST":
                    performTechnicalAnalysis(result, task, agent);
                    break;
                case "FUNDAMENTAL_ANALYST":
                    performFundamentalAnalysis(result, task, agent);
                    break;
                case "SENTIMENT_ANALYST":
                    performSentimentAnalysis(result, task, agent);
                    break;
                case "RISK_ANALYST":
                    performRiskAnalysis(result, task, agent);
                    break;
                case "MARKET_ANALYST":
                    performMarketAnalysis(result, task, agent);
                    break;
                default:
                    performGeneralAnalysis(result, task, agent);
            }
            
            // 计算处理时间
            long processingTime = System.currentTimeMillis() - startTime;
            result.setProcessingTime(processingTime);
            
            return result;
            
        } catch (Exception e) {
            log.error("智能体分析执行失败: {}", agent.getAgentName(), e);
            throw new RuntimeException("分析执行失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 技术分析
     */
    private void performTechnicalAnalysis(AnalysisResult result, AnalysisTask task, Agent agent) {
        log.debug("执行技术分析: {}", task.getStockCode());
        
        // 获取K线数据
        List<var> klineData = stockDataService.getKLineData(task.getStockCode(), "DAILY", 30);
        
        // 模拟技术分析逻辑
        result.setAnalysisResult("技术分析：基于30日K线数据，该股票呈现上升趋势");
        result.setInvestmentRecommendation("BUY");
        result.setConfidenceScore(BigDecimal.valueOf(0.75));
        result.setRiskLevel("MEDIUM");
        
        // 设置技术指标
        Map<String, Object> technicalData = new HashMap<>();
        technicalData.put("ma5", "计算5日均线");
        technicalData.put("ma20", "计算20日均线");
        technicalData.put("rsi", "相对强弱指数");
        technicalData.put("macd", "MACD指标");
        result.setTechnicalData(technicalData);
        
        // 设置支撑阻力位
        if (!klineData.isEmpty()) {
            var latestPrice = klineData.get(klineData.size() - 1);
            result.setSupportLevel(latestPrice.getClosePrice().multiply(BigDecimal.valueOf(0.95)));
            result.setResistanceLevel(latestPrice.getClosePrice().multiply(BigDecimal.valueOf(1.05)));
        }
        
        result.setKeyPoints(Arrays.asList("技术指标向好", "成交量放大", "突破关键阻力位"));
        result.setRiskWarnings(Arrays.asList("注意回调风险", "关注大盘走势"));
    }
    
    /**
     * 基本面分析
     */
    private void performFundamentalAnalysis(AnalysisResult result, AnalysisTask task, Agent agent) {
        log.debug("执行基本面分析: {}", task.getStockCode());
        
        // 获取股票基本信息
        var stockInfo = stockDataService.getStockInfo(task.getStockCode());
        
        if (stockInfo.isPresent()) {
            var stock = stockInfo.get();
            
            result.setAnalysisResult("基本面分析：公司财务状况良好，盈利能力稳定");
            result.setInvestmentRecommendation("HOLD");
            result.setConfidenceScore(BigDecimal.valueOf(0.80));
            result.setRiskLevel("LOW");
            
            // 设置基本面数据
            Map<String, Object> fundamentalData = new HashMap<>();
            fundamentalData.put("pe", stock.getPeRatio());
            fundamentalData.put("pb", stock.getPbRatio());
            fundamentalData.put("roe", stock.getRoe());
            fundamentalData.put("eps", stock.getEarningsPerShare());
            result.setFundamentalData(fundamentalData);
            
            // 设置财务比率
            Map<String, Object> financialRatios = new HashMap<>();
            financialRatios.put("debtToEquity", stock.getDebtToEquityRatio());
            financialRatios.put("currentRatio", stock.getCurrentRatio());
            financialRatios.put("quickRatio", stock.getQuickRatio());
            result.setFinancialRatios(financialRatios);
            
            result.setKeyPoints(Arrays.asList("PE估值合理", "ROE表现优秀", "财务结构稳健"));
            result.setRiskWarnings(Arrays.asList("关注行业政策变化", "注意市场竞争加剧"));
        }
    }
    
    /**
     * 情绪分析
     */
    private void performSentimentAnalysis(AnalysisResult result, AnalysisTask task, Agent agent) {
        log.debug("执行情绪分析: {}", task.getStockCode());
        
        result.setAnalysisResult("情绪分析：市场情绪偏向乐观，投资者信心较强");
        result.setInvestmentRecommendation("BUY");
        result.setConfidenceScore(BigDecimal.valueOf(0.70));
        result.setRiskLevel("MEDIUM");
        
        // 设置情绪数据
        Map<String, Object> sentimentData = new HashMap<>();
        sentimentData.put("newssentiment", "正面");
        sentimentData.put("socialSentiment", "乐观");
        sentimentData.put("analystSentiment", "看好");
        sentimentData.put("marketSentiment", "积极");
        result.setSentimentData(sentimentData);
        
        result.setKeyPoints(Arrays.asList("媒体报道积极", "社交媒体讨论热烈", "分析师普遍看好"));
        result.setRiskWarnings(Arrays.asList("情绪可能过热", "注意反向指标"));
    }
    
    /**
     * 风险分析
     */
    private void performRiskAnalysis(AnalysisResult result, AnalysisTask task, Agent agent) {
        log.debug("执行风险分析: {}", task.getStockCode());
        
        result.setAnalysisResult("风险分析：整体风险可控，建议适度配置");
        result.setInvestmentRecommendation("HOLD");
        result.setConfidenceScore(BigDecimal.valueOf(0.85));
        result.setRiskLevel("MEDIUM");
        
        result.setKeyPoints(Arrays.asList("系统性风险较低", "个股风险可控", "流动性充足"));
        result.setRiskWarnings(Arrays.asList("关注宏观经济变化", "注意行业周期性风险", "控制仓位比例"));
    }
    
    /**
     * 市场分析
     */
    private void performMarketAnalysis(AnalysisResult result, AnalysisTask task, Agent agent) {
        log.debug("执行市场分析: {}", task.getStockCode());
        
        result.setAnalysisResult("市场分析：所属行业前景良好，市场地位稳固");
        result.setInvestmentRecommendation("BUY");
        result.setConfidenceScore(BigDecimal.valueOf(0.78));
        result.setRiskLevel("LOW");
        
        result.setKeyPoints(Arrays.asList("行业增长稳定", "市场份额领先", "竞争优势明显"));
        result.setRiskWarnings(Arrays.asList("关注新进入者", "注意技术变革影响"));
    }
    
    /**
     * 通用分析
     */
    private void performGeneralAnalysis(AnalysisResult result, AnalysisTask task, Agent agent) {
        log.debug("执行通用分析: {}", task.getStockCode());
        
        result.setAnalysisResult("综合分析：该股票具有一定投资价值，建议关注");
        result.setInvestmentRecommendation("HOLD");
        result.setConfidenceScore(BigDecimal.valueOf(0.65));
        result.setRiskLevel("MEDIUM");
        
        result.setKeyPoints(Arrays.asList("基本面稳健", "技术面向好", "估值合理"));
        result.setRiskWarnings(Arrays.asList("注意市场波动", "控制投资风险"));
    }
    
    /**
     * 更新智能体统计
     */
    private void updateAgentStatistics(Agent agent, boolean success, Long processingTime, BigDecimal confidence) {
        agent.updateTaskStatistics(success, processingTime, confidence);
        agent.updateLastTaskTime();
        agentRepository.save(agent);
    }
    
    /**
     * 检查智能体健康状态
     */
    private boolean checkAgentHealth(Agent agent) {
        // 检查是否启用
        if (!agent.getEnabled()) {
            return false;
        }
        
        // 检查最后活跃时间
        if (agent.getLastActiveTime() != null) {
            LocalDateTime threshold = LocalDateTime.now().minusHours(1);
            if (agent.getLastActiveTime().isBefore(threshold)) {
                return false;
            }
        }
        
        // 检查成功率
        if (agent.getSuccessRate() != null && agent.getSuccessRate().compareTo(BigDecimal.valueOf(0.5)) < 0) {
            return false;
        }
        
        // 检查响应时间
        if (agent.getAverageResponseTime() != null && agent.getAverageResponseTime() > 30000) { // 30秒
            return false;
        }
        
        return true;
    }
}