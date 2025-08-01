package com.jd.genie.agent.collaboration.model;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 智能体协作相关的数据模型
 */
public class CollaborationModels {

    /**
     * 智能体信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentInfo {
        private String agentId;
        private String agentName;
        private AgentType agentType;
        private Set<String> capabilities;
        private AgentStatus status;
        private String endpoint;
        private Map<String, Object> configuration;
        private LocalDateTime lastActiveTime;
        private Double performanceScore;
        private String description;
    }

    /**
     * 智能体类型枚举
     */
    public enum AgentType {
        FUNDAMENTAL_ANALYST("基本面分析师"),
        TECHNICAL_ANALYST("技术分析师"),
        MARKET_ANALYST("市场分析师"),
        RISK_MANAGER("风险管理师"),
        SENTIMENT_ANALYST("情感分析师"),
        NEWS_ANALYST("新闻分析师"),
        RESEARCH_ANALYST("研究分析师"),
        TRADING_EXECUTOR("交易执行师"),
        BULLISH_RESEARCHER("多头研究员"),
        BEARISH_RESEARCHER("空头研究员"),
        QUANTITATIVE_ANALYST("量化分析师"),
        PORTFOLIO_MANAGER("投资组合管理师"),
        MACRO_ECONOMIST("宏观经济学家"),
        INDUSTRY_EXPERT("行业专家");

        private final String description;

        AgentType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 智能体状态枚举
     */
    public enum AgentStatus {
        ACTIVE("活跃"),
        INACTIVE("非活跃"),
        BUSY("忙碌"),
        ERROR("错误"),
        MAINTENANCE("维护中");

        private final String description;

        AgentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 协作请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollaborationRequest {
        private String requestId;
        private String taskType;
        private String taskDescription;
        private Map<String, Object> parameters;
        private List<String> requiredAgentTypes;
        private CollaborationMode mode;
        private Integer maxParticipants;
        private Integer timeoutMinutes;
        private String priority;
        private String requesterId;
        private LocalDateTime requestTime;
    }

    /**
     * 协作模式枚举
     */
    public enum CollaborationMode {
        STRUCTURED_DEBATE("结构化辩论"),
        PARALLEL_ANALYSIS("并行分析"),
        SEQUENTIAL_PIPELINE("顺序流水线"),
        CONSENSUS_BUILDING("共识构建"),
        EXPERT_PANEL("专家小组"),
        PEER_REVIEW("同行评议");

        private final String description;

        CollaborationMode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 协作会话
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollaborationSession {
        private String sessionId;
        private CollaborationRequest request;
        private List<AgentInfo> participants;
        private CollaborationStrategy strategy;
        private CollaborationStatus status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<CollaborationInteraction> interactions;
        private CollaborationResult result;
        private String errorMessage;
        private Map<String, Object> metadata;

        public void addInteraction(CollaborationInteraction interaction) {
            if (interactions == null) {
                interactions = new CopyOnWriteArrayList<>();
            }
            interactions.add(interaction);
        }
    }

    /**
     * 协作状态枚举
     */
    public enum CollaborationStatus {
        PENDING("等待中"),
        ACTIVE("进行中"),
        COMPLETED("已完成"),
        FAILED("失败"),
        STOPPED("已停止"),
        TIMEOUT("超时");

        private final String description;

        CollaborationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 协作策略
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollaborationStrategy {
        private CollaborationMode type;
        private Integer maxRounds;
        private Integer timeoutPerRound;
        private Double consensusThreshold;
        private String votingMethod;
        private Map<String, Object> parameters;
        private List<String> rules;
    }

    /**
     * 协作交互记录
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollaborationInteraction {
        private String interactionId;
        private String agentId;
        private String interactionType;
        private AgentOpinion opinion;
        private LocalDateTime timestamp;
        private Map<String, Object> context;
        private String parentInteractionId;
    }

    /**
     * 智能体观点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentOpinion {
        private String agentId;
        private String opinion;
        private Double confidence;
        private String reasoning;
        private List<String> supportingEvidence;
        private Map<String, Object> data;
        private LocalDateTime timestamp;
        private String opinionType;
        private List<String> tags;
    }

    /**
     * 协作结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollaborationResult {
        private String sessionId;
        private String decision;
        private Double confidence;
        private String reasoning;
        private Map<String, Object> details;
        private List<AgentOpinion> contributingOpinions;
        private Map<String, Double> agentContributions;
        private LocalDateTime timestamp;
        private String resultType;
        private Map<String, Object> metrics;
    }

    /**
     * 决策融合配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FusionConfig {
        private String fusionMethod;
        private Map<String, Double> agentWeights;
        private Double confidenceThreshold;
        private String conflictResolution;
        private Map<String, Object> parameters;
    }

    /**
     * 协作指标
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollaborationMetrics {
        private String sessionId;
        private Integer totalInteractions;
        private Integer participantCount;
        private Long durationMillis;
        private Double consensusLevel;
        private Double averageConfidence;
        private Map<String, Integer> interactionsByAgent;
        private Map<String, Double> agentPerformance;
        private String qualityScore;
    }

    /**
     * 协作历史记录
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollaborationHistory {
        private String historyId;
        private String sessionId;
        private CollaborationRequest request;
        private List<AgentInfo> participants;
        private CollaborationResult result;
        private CollaborationMetrics metrics;
        private LocalDateTime timestamp;
        private String status;
        private Map<String, Object> summary;
    }

    /**
     * 智能体能力描述
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentCapability {
        private String capabilityId;
        private String name;
        private String description;
        private List<String> inputTypes;
        private List<String> outputTypes;
        private Map<String, Object> parameters;
        private Double accuracy;
        private Long averageResponseTime;
    }

    /**
     * 协作模板
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollaborationTemplate {
        private String templateId;
        private String name;
        private String description;
        private CollaborationMode mode;
        private List<AgentType> requiredAgentTypes;
        private CollaborationStrategy defaultStrategy;
        private Map<String, Object> defaultParameters;
        private List<String> useCases;
    }

    /**
     * 协作事件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollaborationEvent {
        private String eventId;
        private String sessionId;
        private String eventType;
        private String agentId;
        private Map<String, Object> eventData;
        private LocalDateTime timestamp;
        private String severity;
        private String description;
    }

    /**
     * 多空辩论结果
     * 参考A_Share_investment_Agent的多空观点对比机制
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BullBearDebateResult {
        private String stockCode;
        private Object bullishResult;  // BullishResearcher.ResearchResult
        private Object bearishResult;  // BearishResearcher.ResearchResult
        private double confidenceDifference;
        private String dominantSide;  // BULLISH, BEARISH, NEUTRAL
        private double debateIntensity;
        private LocalDateTime timestamp;
    }

    /**
     * 综合辩论结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComprehensiveDebateResult {
        private String stockCode;
        private double viewpointConsistency;
        private String synthesizedViewpoint;
        private double combinedConfidence;
        private List<String> keyDisagreements;
        private LocalDateTime timestamp;
    }

    /**
     * LLM第三方评估结果
     * 参考A_Share_investment_Agent的LLM增强机制
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LLMAssessmentResult {
        private String stockCode;
        private String assessmentContent;
        private double objectivityScore;   // 客观性评分
        private double reliabilityScore;   // 可靠性评分
        private double finalScore;         // 最终评分
        private LocalDateTime timestamp;
    }

    /**
     * 增强型辩论结果
     * 整合多空对比、传统辩论、LLM评估的最终结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnhancedDebateResult {
        private String stockCode;
        private BullBearDebateResult bullBearResult;
        private DebateResult traditionalDebateResult;
        private ComprehensiveDebateResult comprehensiveResult;
        private LLMAssessmentResult llmAssessment;
        private double finalConfidence;    // 混合置信度
        private LocalDateTime timestamp;
    }
}