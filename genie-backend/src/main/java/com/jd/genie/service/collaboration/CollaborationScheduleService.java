package com.jd.genie.service.collaboration;

import com.jd.genie.service.collaboration.CollaborationModels.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 智能体协作定时任务服务
 * 负责自动化协作流程、系统维护和性能优化
 * 
 * @author Stock-Agent Team
 * @version 1.0
 */
@Service
public class CollaborationScheduleService {
    
    private static final Logger logger = LoggerFactory.getLogger(CollaborationScheduleService.class);
    
    @Autowired
    private CollaborationEngineService collaborationService;
    
    // 自动协作任务状态
    private boolean autoCollaborationEnabled = true;
    private final Map<String, LocalDateTime> lastCollaborationTime = new HashMap<>();
    
    /**
     * 定期健康检查智能体状态
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void performAgentHealthCheck() {
        if (!autoCollaborationEnabled) {
            return;
        }
        
        try {
            logger.debug("开始智能体健康检查");
            
            List<AgentInfo> agents = collaborationService.getRegisteredAgents();
            int activeCount = 0;
            int inactiveCount = 0;
            
            for (AgentInfo agent : agents) {
                if (agent.getStatus() == AgentStatus.ACTIVE) {
                    activeCount++;
                    // 检查智能体响应性（模拟）
                    if (Math.random() < 0.05) { // 5%概率检测到问题
                        logger.warn("智能体 {} 响应异常，建议检查", agent.getAgentId());
                    }
                } else {
                    inactiveCount++;
                }
            }
            
            logger.info("智能体健康检查完成 - 活跃: {}, 非活跃: {}, 总计: {}", 
                    activeCount, inactiveCount, agents.size());
            
            // 如果活跃智能体数量过少，发出警告
            if (activeCount < 3) {
                logger.warn("活跃智能体数量不足 ({}), 建议增加智能体或检查系统状态", activeCount);
            }
            
        } catch (Exception e) {
            logger.error("智能体健康检查失败", e);
        }
    }
    
    /**
     * 自动市场分析协作
     * 每30分钟执行一次，在交易时间内
     */
    @Scheduled(cron = "0 */30 9-15 * * MON-FRI") // 工作日9:00-15:59，每30分钟
    public void performAutoMarketAnalysis() {
        if (!autoCollaborationEnabled) {
            return;
        }
        
        try {
            logger.info("开始自动市场分析协作");
            
            // 获取分析型和预测型智能体
            List<AgentInfo> analysisAgents = collaborationService.getAgentsByType(AgentType.ANALYSIS);
            List<AgentInfo> predictionAgents = collaborationService.getAgentsByType(AgentType.PREDICTION);
            
            if (analysisAgents.isEmpty() || predictionAgents.isEmpty()) {
                logger.warn("缺少必要的智能体类型进行市场分析");
                return;
            }
            
            // 选择参与协作的智能体
            List<String> participantIds = new ArrayList<>();
            participantIds.addAll(analysisAgents.stream()
                    .limit(2)
                    .map(AgentInfo::getAgentId)
                    .collect(Collectors.toList()));
            participantIds.addAll(predictionAgents.stream()
                    .limit(2)
                    .map(AgentInfo::getAgentId)
                    .collect(Collectors.toList()));
            
            // 创建协作请求
            CollaborationRequest request = CollaborationRequest.builder()
                    .topic("当前市场趋势分析和投资机会识别")
                    .collaborationMode(CollaborationMode.PARALLEL_ANALYSIS)
                    .participantIds(participantIds)
                    .context(Map.of(
                            "analysisType", "market_trend",
                            "timeframe", "intraday",
                            "timestamp", LocalDateTime.now().toString()
                    ))
                    .build();
            
            // 执行协作
            String sessionId = collaborationService.createCollaborationSession(request);
            if (sessionId != null) {
                CompletableFuture<CollaborationResult> future = 
                        collaborationService.executeCollaboration(sessionId);
                
                // 异步处理结果
                future.thenAccept(result -> {
                    logger.info("自动市场分析完成，置信度: {}, 共识度: {}", 
                            result.getConfidenceScore(), result.getConsensusLevel());
                    
                    // 记录协作时间
                    lastCollaborationTime.put("market_analysis", LocalDateTime.now());
                });
            }
            
        } catch (Exception e) {
            logger.error("自动市场分析协作失败", e);
        }
    }
    
    /**
     * 风险评估协作
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void performRiskAssessment() {
        if (!autoCollaborationEnabled) {
            return;
        }
        
        try {
            logger.info("开始风险评估协作");
            
            // 获取决策型和监控型智能体
            List<AgentInfo> decisionAgents = collaborationService.getAgentsByType(AgentType.DECISION);
            List<AgentInfo> monitoringAgents = collaborationService.getAgentsByType(AgentType.MONITORING);
            
            List<String> participantIds = new ArrayList<>();
            participantIds.addAll(decisionAgents.stream()
                    .limit(2)
                    .map(AgentInfo::getAgentId)
                    .collect(Collectors.toList()));
            participantIds.addAll(monitoringAgents.stream()
                    .limit(2)
                    .map(AgentInfo::getAgentId)
                    .collect(Collectors.toList()));
            
            if (participantIds.size() < 2) {
                logger.warn("参与风险评估的智能体数量不足");
                return;
            }
            
            // 创建风险评估协作
            CollaborationRequest request = CollaborationRequest.builder()
                    .topic("系统风险评估和预警机制优化")
                    .collaborationMode(CollaborationMode.CONSENSUS_BUILDING)
                    .participantIds(participantIds)
                    .context(Map.of(
                            "assessmentType", "system_risk",
                            "scope", "portfolio_wide",
                            "timestamp", LocalDateTime.now().toString()
                    ))
                    .build();
            
            String sessionId = collaborationService.createCollaborationSession(request);
            if (sessionId != null) {
                CompletableFuture<CollaborationResult> future = 
                        collaborationService.executeCollaboration(sessionId);
                
                future.thenAccept(result -> {
                    logger.info("风险评估协作完成，置信度: {}", result.getConfidenceScore());
                    
                    // 如果风险评估结果置信度较低，发出警告
                    if (result.getConfidenceScore() < 0.6) {
                        logger.warn("风险评估置信度较低 ({}), 建议人工介入", result.getConfidenceScore());
                    }
                    
                    lastCollaborationTime.put("risk_assessment", LocalDateTime.now());
                });
            }
            
        } catch (Exception e) {
            logger.error("风险评估协作失败", e);
        }
    }
    
    /**
     * 策略优化协作
     * 每天上午9点执行
     */
    @Scheduled(cron = "0 0 9 * * MON-FRI") // 工作日上午9点
    public void performStrategyOptimization() {
        if (!autoCollaborationEnabled) {
            return;
        }
        
        try {
            logger.info("开始策略优化协作");
            
            // 获取所有类型的智能体进行综合协作
            List<AgentInfo> allAgents = collaborationService.getRegisteredAgents();
            
            if (allAgents.size() < 4) {
                logger.warn("智能体数量不足，无法进行策略优化协作");
                return;
            }
            
            // 每种类型选择一个智能体
            Map<AgentType, List<AgentInfo>> agentsByType = allAgents.stream()
                    .filter(agent -> agent.getStatus() == AgentStatus.ACTIVE)
                    .collect(Collectors.groupingBy(AgentInfo::getAgentType));
            
            List<String> participantIds = new ArrayList<>();
            agentsByType.forEach((type, agents) -> {
                if (!agents.isEmpty()) {
                    participantIds.add(agents.get(0).getAgentId());
                }
            });
            
            if (participantIds.size() < 3) {
                logger.warn("活跃智能体类型不足，无法进行策略优化");
                return;
            }
            
            // 创建策略优化协作
            CollaborationRequest request = CollaborationRequest.builder()
                    .topic("交易策略优化和系统性能提升建议")
                    .collaborationMode(CollaborationMode.STRUCTURED_DEBATE)
                    .participantIds(participantIds)
                    .context(Map.of(
                            "optimizationType", "strategy_enhancement",
                            "timeframe", "daily",
                            "focus", "performance_improvement",
                            "timestamp", LocalDateTime.now().toString()
                    ))
                    .build();
            
            String sessionId = collaborationService.createCollaborationSession(request);
            if (sessionId != null) {
                CompletableFuture<CollaborationResult> future = 
                        collaborationService.executeCollaboration(sessionId);
                
                future.thenAccept(result -> {
                    logger.info("策略优化协作完成，共识度: {}, 关键洞察数: {}", 
                            result.getConsensusLevel(), result.getKeyInsights().size());
                    
                    // 记录优化建议
                    if (!result.getKeyInsights().isEmpty()) {
                        logger.info("策略优化关键建议: {}", 
                                String.join("; ", result.getKeyInsights()));
                    }
                    
                    lastCollaborationTime.put("strategy_optimization", LocalDateTime.now());
                });
            }
            
        } catch (Exception e) {
            logger.error("策略优化协作失败", e);
        }
    }
    
    /**
     * 系统维护和清理
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * *") // 每天凌晨2点
    public void performSystemMaintenance() {
        try {
            logger.info("开始系统维护和清理");
            
            // 清理协作缓存
            collaborationService.clearCache();
            
            // 获取系统指标
            CollaborationMetrics metrics = collaborationService.getCollaborationMetrics();
            
            logger.info("系统维护报告 - 总智能体: {}, 活跃会话: {}, 完成会话: {}, 平均置信度: {:.2f}, 成功率: {:.2f}%", 
                    metrics.getTotalAgents(),
                    metrics.getActiveSessions(),
                    metrics.getCompletedSessions(),
                    metrics.getAverageConfidence(),
                    metrics.getSuccessRate() * 100);
            
            // 性能警告
            if (metrics.getSuccessRate() < 0.8) {
                logger.warn("协作成功率较低 ({:.2f}%), 建议检查智能体配置", metrics.getSuccessRate() * 100);
            }
            
            if (metrics.getAverageConfidence() < 0.7) {
                logger.warn("平均置信度较低 ({:.2f}), 建议优化协作策略", metrics.getAverageConfidence());
            }
            
            // 清理过期的协作时间记录
            LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
            lastCollaborationTime.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
            
            logger.info("系统维护完成");
            
        } catch (Exception e) {
            logger.error("系统维护失败", e);
        }
    }
    
    /**
     * 紧急协作响应
     * 当检测到异常情况时触发
     */
    public void triggerEmergencyCollaboration(String emergencyType, Map<String, Object> context) {
        try {
            logger.warn("触发紧急协作响应，类型: {}", emergencyType);
            
            // 获取所有活跃智能体
            List<AgentInfo> activeAgents = collaborationService.getRegisteredAgents().stream()
                    .filter(agent -> agent.getStatus() == AgentStatus.ACTIVE)
                    .collect(Collectors.toList());
            
            if (activeAgents.size() < 2) {
                logger.error("活跃智能体数量不足，无法执行紧急协作");
                return;
            }
            
            // 选择最相关的智能体
            List<String> participantIds = selectEmergencyParticipants(emergencyType, activeAgents);
            
            // 创建紧急协作请求
            CollaborationRequest request = CollaborationRequest.builder()
                    .topic(String.format("紧急情况处理: %s", emergencyType))
                    .collaborationMode(CollaborationMode.PARALLEL_ANALYSIS) // 快速并行分析
                    .participantIds(participantIds)
                    .context(context)
                    .build();
            
            String sessionId = collaborationService.createCollaborationSession(request);
            if (sessionId != null) {
                CompletableFuture<CollaborationResult> future = 
                        collaborationService.executeCollaboration(sessionId);
                
                future.thenAccept(result -> {
                    logger.warn("紧急协作完成，建议: {}", result.getResult());
                    
                    // 如果置信度很高，可以考虑自动执行某些操作
                    if (result.getConfidenceScore() > 0.9) {
                        logger.info("紧急协作建议置信度很高，建议立即采取行动");
                    }
                });
            }
            
        } catch (Exception e) {
            logger.error("紧急协作响应失败", e);
        }
    }
    
    /**
     * 选择紧急情况参与者
     */
    private List<String> selectEmergencyParticipants(String emergencyType, List<AgentInfo> availableAgents) {
        List<String> participants = new ArrayList<>();
        
        switch (emergencyType.toLowerCase()) {
            case "market_crash":
            case "volatility_spike":
                // 优先选择分析和决策智能体
                availableAgents.stream()
                        .filter(agent -> agent.getAgentType() == AgentType.ANALYSIS || 
                                       agent.getAgentType() == AgentType.DECISION)
                        .limit(4)
                        .forEach(agent -> participants.add(agent.getAgentId()));
                break;
                
            case "system_error":
            case "data_anomaly":
                // 优先选择监控和分析智能体
                availableAgents.stream()
                        .filter(agent -> agent.getAgentType() == AgentType.MONITORING || 
                                       agent.getAgentType() == AgentType.ANALYSIS)
                        .limit(4)
                        .forEach(agent -> participants.add(agent.getAgentId()));
                break;
                
            default:
                // 默认选择各类型智能体
                Map<AgentType, List<AgentInfo>> byType = availableAgents.stream()
                        .collect(Collectors.groupingBy(AgentInfo::getAgentType));
                
                byType.forEach((type, agents) -> {
                    if (!agents.isEmpty() && participants.size() < 4) {
                        participants.add(agents.get(0).getAgentId());
                    }
                });
        }
        
        // 确保至少有2个参与者
        if (participants.size() < 2 && availableAgents.size() >= 2) {
            availableAgents.stream()
                    .limit(2)
                    .forEach(agent -> {
                        if (!participants.contains(agent.getAgentId())) {
                            participants.add(agent.getAgentId());
                        }
                    });
        }
        
        return participants;
    }
    
    /**
     * 启用/禁用自动协作
     */
    public void setAutoCollaborationEnabled(boolean enabled) {
        this.autoCollaborationEnabled = enabled;
        logger.info("自动协作已{}", enabled ? "启用" : "禁用");
    }
    
    /**
     * 获取自动协作状态
     */
    public boolean isAutoCollaborationEnabled() {
        return autoCollaborationEnabled;
    }
    
    /**
     * 获取最后协作时间
     */
    public Map<String, LocalDateTime> getLastCollaborationTimes() {
        return new HashMap<>(lastCollaborationTime);
    }
    
    /**
     * 手动触发市场分析
     */
    public void manualTriggerMarketAnalysis() {
        logger.info("手动触发市场分析协作");
        performAutoMarketAnalysis();
    }
    
    /**
     * 手动触发风险评估
     */
    public void manualTriggerRiskAssessment() {
        logger.info("手动触发风险评估协作");
        performRiskAssessment();
    }
    
    /**
     * 手动触发策略优化
     */
    public void manualTriggerStrategyOptimization() {
        logger.info("手动触发策略优化协作");
        performStrategyOptimization();
    }
    
    /**
     * 获取调度服务状态
     */
    public Map<String, Object> getScheduleStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("autoCollaborationEnabled", autoCollaborationEnabled);
        status.put("lastCollaborationTimes", lastCollaborationTime);
        status.put("systemTime", LocalDateTime.now());
        
        // 计算下次执行时间（简化版本）
        LocalDateTime now = LocalDateTime.now();
        status.put("nextMarketAnalysis", calculateNextMarketAnalysis(now));
        status.put("nextRiskAssessment", now.plusHours(1).withMinute(0).withSecond(0));
        status.put("nextStrategyOptimization", now.plusDays(1).withHour(9).withMinute(0).withSecond(0));
        status.put("nextSystemMaintenance", now.plusDays(1).withHour(2).withMinute(0).withSecond(0));
        
        return status;
    }
    
    /**
     * 计算下次市场分析时间
     */
    private LocalDateTime calculateNextMarketAnalysis(LocalDateTime now) {
        // 简化计算：下一个30分钟整点
        int nextMinute = ((now.getMinute() / 30) + 1) * 30;
        if (nextMinute >= 60) {
            return now.plusHours(1).withMinute(0).withSecond(0);
        } else {
            return now.withMinute(nextMinute).withSecond(0);
        }
    }
}