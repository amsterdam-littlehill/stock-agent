package com.jd.genie.service.collaboration;

import com.jd.genie.service.collaboration.CollaborationModels.*;
import com.jd.genie.service.collaboration.CollaborationStrategyImpl.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 智能体协作引擎核心服务
 * 负责管理智能体协作会话、执行协作策略、融合决策结果
 * 
 * @author Stock-Agent Team
 * @version 1.0
 */
@Service
public class CollaborationEngineService implements CollaborationEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(CollaborationEngineService.class);
    
    // 智能体注册表
    private final Map<String, AgentInfo> registeredAgents = new ConcurrentHashMap<>();
    
    // 活跃协作会话
    private final Map<String, CollaborationSession> activeSessions = new ConcurrentHashMap<>();
    
    // 协作历史记录
    private final List<CollaborationHistory> collaborationHistory = new ArrayList<>();
    
    // 线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);
    
    // 协作策略实例
    private final StructuredDebateStrategy debateStrategy = new StructuredDebateStrategy();
    private final ParallelAnalysisStrategy parallelStrategy = new ParallelAnalysisStrategy(executorService);
    
    /**
     * 注册智能体
     */
    @Override
    public boolean registerAgent(AgentInfo agentInfo) {
        try {
            logger.info("注册智能体: {}", agentInfo.getAgentId());
            
            // 验证智能体信息
            if (!validateAgentInfo(agentInfo)) {
                logger.warn("智能体信息验证失败: {}", agentInfo.getAgentId());
                return false;
            }
            
            // 检查是否已注册
            if (registeredAgents.containsKey(agentInfo.getAgentId())) {
                logger.warn("智能体已存在: {}", agentInfo.getAgentId());
                return false;
            }
            
            // 注册智能体
            registeredAgents.put(agentInfo.getAgentId(), agentInfo);
            
            logger.info("智能体注册成功: {}, 当前注册数量: {}", 
                    agentInfo.getAgentId(), registeredAgents.size());
            
            return true;
            
        } catch (Exception e) {
            logger.error("注册智能体失败: {}", agentInfo.getAgentId(), e);
            return false;
        }
    }
    
    /**
     * 注销智能体
     */
    @Override
    public boolean unregisterAgent(String agentId) {
        try {
            logger.info("注销智能体: {}", agentId);
            
            AgentInfo removed = registeredAgents.remove(agentId);
            if (removed != null) {
                // 清理相关会话
                cleanupAgentSessions(agentId);
                logger.info("智能体注销成功: {}", agentId);
                return true;
            } else {
                logger.warn("智能体不存在: {}", agentId);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("注销智能体失败: {}", agentId, e);
            return false;
        }
    }
    
    /**
     * 获取已注册智能体列表
     */
    @Override
    @Cacheable(value = "agents", key = "'all'")
    public List<AgentInfo> getRegisteredAgents() {
        return new ArrayList<>(registeredAgents.values());
    }
    
    /**
     * 根据类型获取智能体
     */
    @Override
    public List<AgentInfo> getAgentsByType(AgentType agentType) {
        return registeredAgents.values().stream()
                .filter(agent -> agent.getAgentType() == agentType)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据专业领域获取智能体
     */
    @Override
    public List<AgentInfo> getAgentsBySpecialization(String specialization) {
        return registeredAgents.values().stream()
                .filter(agent -> agent.getSpecialization().toLowerCase()
                        .contains(specialization.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * 创建协作会话
     */
    @Override
    public String createCollaborationSession(CollaborationRequest request) {
        try {
            logger.info("创建协作会话，模式: {}, 参与者: {}", 
                    request.getCollaborationMode(), request.getParticipantIds().size());
            
            // 生成会话ID
            String sessionId = UUID.randomUUID().toString();
            
            // 验证参与者
            List<AgentInfo> participants = validateAndGetParticipants(request.getParticipantIds());
            if (participants.isEmpty()) {
                logger.error("没有有效的参与者");
                return null;
            }
            
            // 创建会话
            CollaborationSession session = CollaborationSession.builder()
                    .sessionId(sessionId)
                    .participants(participants)
                    .collaborationMode(request.getCollaborationMode())
                    .topic(request.getTopic())
                    .context(request.getContext())
                    .status(CollaborationStatus.CREATED)
                    .createdAt(LocalDateTime.now())
                    .interactions(new ArrayList<>())
                    .build();
            
            activeSessions.put(sessionId, session);
            
            logger.info("协作会话创建成功: {}", sessionId);
            return sessionId;
            
        } catch (Exception e) {
            logger.error("创建协作会话失败", e);
            return null;
        }
    }
    
    /**
     * 执行协作
     */
    @Override
    @Async
    public CompletableFuture<CollaborationResult> executeCollaboration(String sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("开始执行协作会话: {}", sessionId);
                
                CollaborationSession session = activeSessions.get(sessionId);
                if (session == null) {
                    logger.error("协作会话不存在: {}", sessionId);
                    return createErrorResult("会话不存在");
                }
                
                // 更新会话状态
                session.setStatus(CollaborationStatus.RUNNING);
                session.setStartedAt(LocalDateTime.now());
                
                CollaborationResult result;
                
                // 根据协作模式执行不同策略
                switch (session.getCollaborationMode()) {
                    case STRUCTURED_DEBATE:
                        result = executeStructuredDebate(session);
                        break;
                    case PARALLEL_ANALYSIS:
                        result = executeParallelAnalysis(session);
                        break;
                    case SEQUENTIAL_PIPELINE:
                        result = executeSequentialPipeline(session);
                        break;
                    case CONSENSUS_BUILDING:
                        result = executeConsensusBuilding(session);
                        break;
                    default:
                        result = createErrorResult("不支持的协作模式: " + session.getCollaborationMode());
                }
                
                // 更新会话状态
                session.setStatus(CollaborationStatus.COMPLETED);
                session.setCompletedAt(LocalDateTime.now());
                session.setResult(result);
                
                // 记录协作历史
                recordCollaborationHistory(session, result);
                
                logger.info("协作会话执行完成: {}, 置信度: {}", 
                        sessionId, result.getConfidenceScore());
                
                return result;
                
            } catch (Exception e) {
                logger.error("执行协作会话失败: {}", sessionId, e);
                return createErrorResult("协作执行失败: " + e.getMessage());
            }
        }, executorService);
    }
    
    /**
     * 执行结构化辩论
     */
    private CollaborationResult executeStructuredDebate(CollaborationSession session) {
        logger.info("执行结构化辩论，会话: {}", session.getSessionId());
        
        // 确定辩论轮数（根据参与者数量和复杂度）
        int rounds = Math.max(2, Math.min(5, session.getParticipants().size()));
        
        return debateStrategy.executeDebate(
                session.getParticipants(), 
                session.getTopic(), 
                rounds
        );
    }
    
    /**
     * 执行并行分析
     */
    private CollaborationResult executeParallelAnalysis(CollaborationSession session) {
        logger.info("执行并行分析，会话: {}", session.getSessionId());
        
        try {
            return parallelStrategy.executeParallelAnalysis(
                    session.getParticipants(),
                    session.getTopic(),
                    session.getContext()
            ).get(); // 同步等待结果
        } catch (Exception e) {
            logger.error("并行分析执行失败", e);
            return createErrorResult("并行分析失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行顺序流水线
     */
    private CollaborationResult executeSequentialPipeline(CollaborationSession session) {
        logger.info("执行顺序流水线，会话: {}", session.getSessionId());
        
        List<AgentInfo> participants = session.getParticipants();
        String currentInput = session.getTopic();
        List<AgentOpinion> pipelineResults = new ArrayList<>();
        
        try {
            // 按顺序执行每个智能体
            for (int i = 0; i < participants.size(); i++) {
                AgentInfo agent = participants.get(i);
                logger.debug("流水线步骤 {}: 智能体 {}", i + 1, agent.getAgentId());
                
                // 执行当前步骤
                AgentOpinion stepResult = executeSequentialStep(agent, currentInput, pipelineResults);
                pipelineResults.add(stepResult);
                
                // 下一步的输入是当前步骤的输出
                currentInput = stepResult.getContent();
                
                // 记录交互
                recordInteraction(session, agent, stepResult);
            }
            
            // 生成最终结果
            return synthesizeSequentialResult(pipelineResults, session.getTopic());
            
        } catch (Exception e) {
            logger.error("顺序流水线执行失败", e);
            return createErrorResult("顺序流水线失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行共识构建
     */
    private CollaborationResult executeConsensusBuilding(CollaborationSession session) {
        logger.info("执行共识构建，会话: {}", session.getSessionId());
        
        List<AgentInfo> participants = session.getParticipants();
        String topic = session.getTopic();
        List<AgentOpinion> allOpinions = new ArrayList<>();
        
        try {
            // 第一轮：收集初始观点
            List<AgentOpinion> initialOpinions = collectInitialOpinions(participants, topic);
            allOpinions.addAll(initialOpinions);
            
            // 迭代构建共识
            int maxIterations = 3;
            double targetConsensus = 0.8;
            
            for (int iteration = 1; iteration <= maxIterations; iteration++) {
                logger.debug("共识构建迭代 {}", iteration);
                
                // 计算当前共识度
                double currentConsensus = calculateConsensusScore(allOpinions);
                logger.debug("当前共识度: {}", currentConsensus);
                
                if (currentConsensus >= targetConsensus) {
                    logger.info("达到目标共识度，提前结束");
                    break;
                }
                
                // 收集反馈和调整观点
                List<AgentOpinion> adjustedOpinions = collectAdjustedOpinions(
                        participants, topic, allOpinions);
                allOpinions.addAll(adjustedOpinions);
            }
            
            // 生成共识结果
            return synthesizeConsensusResult(allOpinions, topic);
            
        } catch (Exception e) {
            logger.error("共识构建执行失败", e);
            return createErrorResult("共识构建失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取协作会话状态
     */
    @Override
    public CollaborationSession getSessionStatus(String sessionId) {
        return activeSessions.get(sessionId);
    }
    
    /**
     * 获取协作历史
     */
    @Override
    public List<CollaborationHistory> getCollaborationHistory(int limit) {
        return collaborationHistory.stream()
                .sorted((h1, h2) -> h2.getTimestamp().compareTo(h1.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * 清除缓存
     */
    @Override
    @CacheEvict(value = "agents", allEntries = true)
    public void clearCache() {
        logger.info("清除智能体协作缓存");
    }
    
    /**
     * 获取协作指标
     */
    @Override
    public CollaborationMetrics getCollaborationMetrics() {
        return CollaborationMetrics.builder()
                .totalAgents(registeredAgents.size())
                .activeSessions(activeSessions.size())
                .completedSessions(collaborationHistory.size())
                .averageConfidence(calculateAverageConfidence())
                .successRate(calculateSuccessRate())
                .averageSessionDuration(calculateAverageSessionDuration())
                .build();
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 验证智能体信息
     */
    private boolean validateAgentInfo(AgentInfo agentInfo) {
        return agentInfo != null 
                && agentInfo.getAgentId() != null && !agentInfo.getAgentId().trim().isEmpty()
                && agentInfo.getAgentName() != null && !agentInfo.getAgentName().trim().isEmpty()
                && agentInfo.getAgentType() != null
                && agentInfo.getSpecialization() != null && !agentInfo.getSpecialization().trim().isEmpty();
    }
    
    /**
     * 清理智能体相关会话
     */
    private void cleanupAgentSessions(String agentId) {
        activeSessions.entrySet().removeIf(entry -> {
            CollaborationSession session = entry.getValue();
            return session.getParticipants().stream()
                    .anyMatch(agent -> agent.getAgentId().equals(agentId));
        });
    }
    
    /**
     * 验证并获取参与者
     */
    private List<AgentInfo> validateAndGetParticipants(List<String> participantIds) {
        return participantIds.stream()
                .map(registeredAgents::get)
                .filter(Objects::nonNull)
                .filter(agent -> agent.getStatus() == AgentStatus.ACTIVE)
                .collect(Collectors.toList());
    }
    
    /**
     * 执行顺序步骤
     */
    private AgentOpinion executeSequentialStep(AgentInfo agent, String input, 
                                              List<AgentOpinion> previousResults) {
        // 模拟智能体处理
        String processing = String.format("[%s] 处理输入: %s", agent.getAgentType().name(), input);
        
        if (!previousResults.isEmpty()) {
            processing += "\n基于前序结果进行优化处理...";
        }
        
        return AgentOpinion.builder()
                .agentId(agent.getAgentId())
                .content(processing + "\n处理完成，输出优化结果。")
                .reasoning(String.format("基于 %s 专业能力的顺序处理", agent.getSpecialization()))
                .confidence(0.8 + Math.random() * 0.2)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 收集初始观点
     */
    private List<AgentOpinion> collectInitialOpinions(List<AgentInfo> participants, String topic) {
        return participants.stream()
                .map(agent -> generateInitialOpinion(agent, topic))
                .collect(Collectors.toList());
    }
    
    /**
     * 收集调整观点
     */
    private List<AgentOpinion> collectAdjustedOpinions(List<AgentInfo> participants, 
                                                      String topic, List<AgentOpinion> existingOpinions) {
        return participants.stream()
                .map(agent -> generateAdjustedOpinion(agent, topic, existingOpinions))
                .collect(Collectors.toList());
    }
    
    /**
     * 生成初始观点
     */
    private AgentOpinion generateInitialOpinion(AgentInfo agent, String topic) {
        String content = String.format("[%s] 对 '%s' 的初始观点：基于 %s 专业分析...", 
                agent.getAgentType().name(), topic, agent.getSpecialization());
        
        return AgentOpinion.builder()
                .agentId(agent.getAgentId())
                .content(content)
                .reasoning("初始分析阶段")
                .confidence(0.7 + Math.random() * 0.2)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 生成调整观点
     */
    private AgentOpinion generateAdjustedOpinion(AgentInfo agent, String topic, 
                                               List<AgentOpinion> existingOpinions) {
        String content = String.format("[%s] 对 '%s' 的调整观点：考虑到其他智能体的观点，我调整为...", 
                agent.getAgentType().name(), topic);
        
        return AgentOpinion.builder()
                .agentId(agent.getAgentId())
                .content(content)
                .reasoning("基于群体反馈的观点调整")
                .confidence(0.75 + Math.random() * 0.2)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 计算共识度分数
     */
    private double calculateConsensusScore(List<AgentOpinion> opinions) {
        if (opinions.size() < 2) return 1.0;
        
        // 简化的共识度计算
        double avgConfidence = opinions.stream()
                .mapToDouble(AgentOpinion::getConfidence)
                .average()
                .orElse(0.0);
        
        return Math.min(1.0, avgConfidence + Math.random() * 0.1);
    }
    
    /**
     * 综合顺序结果
     */
    private CollaborationResult synthesizeSequentialResult(List<AgentOpinion> results, String topic) {
        StringBuilder synthesis = new StringBuilder();
        synthesis.append(String.format("'%s' 的顺序流水线处理结果:\n\n", topic));
        
        for (int i = 0; i < results.size(); i++) {
            AgentOpinion result = results.get(i);
            synthesis.append(String.format("步骤 %d (%s): %s\n\n", 
                    i + 1, result.getAgentId(), result.getContent()));
        }
        
        double avgConfidence = results.stream()
                .mapToDouble(AgentOpinion::getConfidence)
                .average()
                .orElse(0.0);
        
        return CollaborationResult.builder()
                .sessionId(UUID.randomUUID().toString())
                .result(synthesis.toString())
                .confidenceScore(avgConfidence)
                .participantCount(results.size())
                .consensusLevel(avgConfidence > 0.8 ? "HIGH" : avgConfidence > 0.6 ? "MEDIUM" : "LOW")
                .keyInsights(results.stream()
                        .map(r -> r.getAgentId() + ": " + r.getContent().substring(0, Math.min(100, r.getContent().length())))
                        .collect(Collectors.toList()))
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 综合共识结果
     */
    private CollaborationResult synthesizeConsensusResult(List<AgentOpinion> opinions, String topic) {
        double consensusScore = calculateConsensusScore(opinions);
        
        StringBuilder synthesis = new StringBuilder();
        synthesis.append(String.format("'%s' 的共识构建结果 (共识度: %.2f):\n\n", topic, consensusScore));
        
        // 按时间分组显示观点演进
        Map<String, List<AgentOpinion>> opinionsByAgent = opinions.stream()
                .collect(Collectors.groupingBy(AgentOpinion::getAgentId));
        
        opinionsByAgent.forEach((agentId, agentOpinions) -> {
            synthesis.append(String.format("智能体 %s 的观点演进:\n", agentId));
            agentOpinions.forEach(op -> 
                    synthesis.append(String.format("- %s\n", op.getContent())));
            synthesis.append("\n");
        });
        
        return CollaborationResult.builder()
                .sessionId(UUID.randomUUID().toString())
                .result(synthesis.toString())
                .confidenceScore(consensusScore)
                .participantCount(opinionsByAgent.size())
                .consensusLevel(consensusScore > 0.8 ? "HIGH" : consensusScore > 0.6 ? "MEDIUM" : "LOW")
                .keyInsights(extractConsensusInsights(opinions))
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 提取共识洞察
     */
    private List<String> extractConsensusInsights(List<AgentOpinion> opinions) {
        List<String> insights = new ArrayList<>();
        
        // 最终共识观点
        opinions.stream()
                .max(Comparator.comparing(AgentOpinion::getTimestamp))
                .ifPresent(latest -> insights.add("最终共识: " + latest.getContent()));
        
        // 观点演进分析
        Map<String, List<AgentOpinion>> byAgent = opinions.stream()
                .collect(Collectors.groupingBy(AgentOpinion::getAgentId));
        
        byAgent.forEach((agentId, agentOpinions) -> {
            if (agentOpinions.size() > 1) {
                insights.add(String.format("%s 观点演进了 %d 次", agentId, agentOpinions.size()));
            }
        });
        
        return insights;
    }
    
    /**
     * 记录交互
     */
    private void recordInteraction(CollaborationSession session, AgentInfo agent, AgentOpinion opinion) {
        AgentInteraction interaction = AgentInteraction.builder()
                .interactionId(UUID.randomUUID().toString())
                .agentId(agent.getAgentId())
                .interactionType("OPINION")
                .content(opinion.getContent())
                .timestamp(LocalDateTime.now())
                .build();
        
        session.getInteractions().add(interaction);
    }
    
    /**
     * 记录协作历史
     */
    private void recordCollaborationHistory(CollaborationSession session, CollaborationResult result) {
        CollaborationHistory history = CollaborationHistory.builder()
                .sessionId(session.getSessionId())
                .collaborationMode(session.getCollaborationMode())
                .participantCount(session.getParticipants().size())
                .topic(session.getTopic())
                .result(result.getResult())
                .confidenceScore(result.getConfidenceScore())
                .consensusLevel(result.getConsensusLevel())
                .duration(calculateSessionDuration(session))
                .timestamp(LocalDateTime.now())
                .build();
        
        collaborationHistory.add(history);
        
        // 限制历史记录数量
        if (collaborationHistory.size() > 1000) {
            collaborationHistory.subList(0, collaborationHistory.size() - 1000).clear();
        }
    }
    
    /**
     * 计算会话持续时间
     */
    private long calculateSessionDuration(CollaborationSession session) {
        if (session.getStartedAt() != null && session.getCompletedAt() != null) {
            return java.time.Duration.between(session.getStartedAt(), session.getCompletedAt()).toMillis();
        }
        return 0;
    }
    
    /**
     * 计算平均置信度
     */
    private double calculateAverageConfidence() {
        return collaborationHistory.stream()
                .mapToDouble(CollaborationHistory::getConfidenceScore)
                .average()
                .orElse(0.0);
    }
    
    /**
     * 计算成功率
     */
    private double calculateSuccessRate() {
        long totalSessions = collaborationHistory.size();
        if (totalSessions == 0) return 0.0;
        
        long successfulSessions = collaborationHistory.stream()
                .mapToLong(h -> h.getConfidenceScore() > 0.6 ? 1 : 0)
                .sum();
        
        return (double) successfulSessions / totalSessions;
    }
    
    /**
     * 计算平均会话持续时间
     */
    private double calculateAverageSessionDuration() {
        return collaborationHistory.stream()
                .mapToLong(CollaborationHistory::getDuration)
                .average()
                .orElse(0.0);
    }
    
    /**
     * 创建错误结果
     */
    private CollaborationResult createErrorResult(String errorMessage) {
        return CollaborationResult.builder()
                .sessionId(UUID.randomUUID().toString())
                .result("协作执行失败")
                .confidenceScore(0.0)
                .participantCount(0)
                .consensusLevel("ERROR")
                .keyInsights(Arrays.asList("错误信息: " + errorMessage))
                .timestamp(LocalDateTime.now())
                .build();
    }
}