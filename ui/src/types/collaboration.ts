/**
 * 智能体协作相关类型定义
 * @author Stock-Agent Team
 * @version 1.0
 */

// 智能体类型枚举
export enum AgentType {
  FUNDAMENTAL_ANALYST = 'FUNDAMENTAL_ANALYST',
  TECHNICAL_ANALYST = 'TECHNICAL_ANALYST',
  MARKET_ANALYST = 'MARKET_ANALYST',
  RISK_MANAGER = 'RISK_MANAGER',
  SENTIMENT_ANALYST = 'SENTIMENT_ANALYST',
  NEWS_ANALYST = 'NEWS_ANALYST',
  QUANTITATIVE_ANALYST = 'QUANTITATIVE_ANALYST',
  PORTFOLIO_MANAGER = 'PORTFOLIO_MANAGER',
  MACRO_ECONOMIST = 'MACRO_ECONOMIST',
  INDUSTRY_EXPERT = 'INDUSTRY_EXPERT',
  RESEARCH_ANALYST = 'RESEARCH_ANALYST',
  TRADING_EXECUTOR = 'TRADING_EXECUTOR',
  ADVISOR = 'ADVISOR',
  ANALYSIS = 'ANALYSIS',
  PREDICTION = 'PREDICTION',
  DECISION = 'DECISION',
  MONITORING = 'MONITORING'
}

// 智能体状态枚举
export enum AgentStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  BUSY = 'BUSY',
  ERROR = 'ERROR'
}

// 协作模式枚举
export enum CollaborationMode {
  STRUCTURED_DEBATE = 'STRUCTURED_DEBATE',
  PARALLEL_ANALYSIS = 'PARALLEL_ANALYSIS',
  SEQUENTIAL_PIPELINE = 'SEQUENTIAL_PIPELINE',
  CONSENSUS_BUILDING = 'CONSENSUS_BUILDING'
}

// 协作状态枚举
export enum CollaborationStatus {
  CREATED = 'CREATED',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
  PENDING = 'PENDING'
}

// 智能体信息接口
export interface AgentInfo {
  id: string;
  agentId: string;
  name: string;
  agentName: string;
  type: AgentType;
  agentType: AgentType;
  specialization: string;
  experienceLevel: number;
  status: AgentStatus;
  description?: string;
  capabilities?: string[];
  lastActiveTime?: string;
  lastActiveAt?: string;
  registeredAt?: string;
  endpoint?: string;
  metadata?: Record<string, any>;
  totalCollaborations?: number;
  successRate?: number;
  averageConfidence?: number;
}

// 智能体观点接口
export interface AgentOpinion {
  agentId: string;
  content: string;
  reasoning: string;
  confidence: number;
  timestamp: string;
  round?: number;
  tags?: string[];
}

// 协作请求接口
export interface CollaborationRequest {
  topic: string;
  collaborationMode: CollaborationMode;
  participantIds: string[];
  context?: Record<string, any>;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  expectedDuration?: number;
  requirements?: string[];
}

// 协作会话接口
export interface CollaborationSession {
  id: string;
  sessionId: string;
  participants: AgentInfo[];
  participantIds?: string[];
  mode: CollaborationMode;
  collaborationMode: CollaborationMode;
  topic: string;
  description?: string;
  context?: Record<string, any>;
  parameters?: Record<string, any>;
  status: CollaborationStatus;
  createdAt: string;
  startedAt?: string;
  completedAt?: string;
  interactions: AgentInteraction[];
  result?: CollaborationResult;
  progress?: number;
  currentStep?: string;
}

// 智能体交互接口
export interface AgentInteraction {
  interactionId: string;
  agentId: string;
  interactionType: string;
  content: string;
  timestamp: string;
  metadata?: Record<string, any>;
}

// 协作结果接口
export interface CollaborationResult {
  sessionId: string;
  result: string;
  confidenceScore: number;
  participantCount: number;
  consensusLevel: string;
  keyInsights: string[];
  timestamp: string;
  recommendations?: string[];
  riskAssessment?: string;
  nextSteps?: string[];
}

// 协作历史接口
export interface CollaborationHistory {
  id: string;
  sessionId: string;
  mode: CollaborationMode;
  collaborationMode: CollaborationMode;
  status: CollaborationStatus;
  participantCount: number;
  participantIds: string[];
  topic: string;
  description?: string;
  result: string;
  confidenceScore: number;
  consensusLevel: string;
  duration: number;
  timestamp: string;
  createdAt: string;
  startedAt?: string;
  completedAt?: string;
  success: boolean;
  interactions?: any[];
  opinions?: any[];
}

// 协作指标接口
export interface CollaborationMetrics {
  totalAgents: number;
  activeSessions: number;
  completedSessions: number;
  averageConfidence: number;
  successRate: number;
  averageSessionDuration: number;
  averageResponseTime?: number;
  totalInteractions?: number;
  agentTypeDistribution?: Record<AgentType, number>;
  modeUsageStats?: Record<CollaborationMode, number>;
  dailyCollaborations?: number;
  weeklyTrend?: number[];
}

// 协作模式配置接口
export interface CollaborationModeConfig {
  name: string;
  description: string;
  minParticipants: number;
  maxParticipants: number;
  estimatedDuration: string;
  icon?: string;
  color?: string;
  features?: string[];
  bestFor?: string[];
}

// 智能体类型配置接口
export interface AgentTypeConfig {
  name: string;
  description: string;
  capabilities: string[];
  icon?: string;
  color?: string;
  examples?: string[];
}

// 协作事件接口
export interface CollaborationEvent {
  eventId: string;
  sessionId: string;
  eventType: 'SESSION_CREATED' | 'SESSION_STARTED' | 'AGENT_JOINED' | 'OPINION_SUBMITTED' | 'SESSION_COMPLETED' | 'ERROR_OCCURRED';
  agentId?: string;
  message: string;
  timestamp: string;
  data?: Record<string, any>;
}

// 快速协作请求接口
export interface QuickCollaborationRequest {
  topic: string;
  mode: string;
  participantIds: string[];
  context?: Record<string, any>;
}

// API响应接口
export interface ApiResponse<T = any> {
  success: boolean;
  message?: string;
  data?: T;
  error?: string;
  timestamp?: string;
}

// 分页响应接口
export interface PaginatedResponse<T> {
  success: boolean;
  data: T[];
  total: number;
  page: number;
  pageSize: number;
  hasNext: boolean;
  hasPrev: boolean;
}

// 协作统计接口
export interface CollaborationStats {
  today: {
    sessions: number;
    successRate: number;
    averageConfidence: number;
    activeAgents: number;
  };
  thisWeek: {
    sessions: number;
    successRate: number;
    topMode: CollaborationMode;
    topAgent: string;
  };
  thisMonth: {
    sessions: number;
    growth: number;
    efficiency: number;
    satisfaction: number;
  };
}

// 实时协作状态接口
export interface RealtimeCollaborationStatus {
  activeSessions: CollaborationSession[];
  queuedSessions: number;
  activeAgents: number;
  systemLoad: number;
  lastUpdate: string;
}

// 协作建议接口
export interface CollaborationSuggestion {
  suggestionId: string;
  type: 'AGENT_SELECTION' | 'MODE_RECOMMENDATION' | 'TOPIC_OPTIMIZATION' | 'TIMING_ADVICE';
  title: string;
  description: string;
  confidence: number;
  actionable: boolean;
  estimatedImpact: 'LOW' | 'MEDIUM' | 'HIGH';
  relatedData?: Record<string, any>;
}

// 协作模板接口
export interface CollaborationTemplate {
  templateId: string;
  name: string;
  description: string;
  collaborationMode: CollaborationMode;
  recommendedAgentTypes: AgentType[];
  topicTemplate: string;
  contextTemplate?: Record<string, any>;
  estimatedDuration: number;
  successRate: number;
  usageCount: number;
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

// 协作性能指标接口
export interface CollaborationPerformance {
  sessionId: string;
  startTime: string;
  endTime: string;
  duration: number;
  participantCount: number;
  interactionCount: number;
  averageResponseTime: number;
  memoryUsage: number;
  cpuUsage: number;
  networkLatency: number;
  errorCount: number;
  warningCount: number;
}

// 协作配置接口
export interface CollaborationConfig {
  maxConcurrentSessions: number;
  maxAgentsPerSession: number;
  sessionTimeoutMs: number;
  enableAutoCollaboration: boolean;
  enableRealTimeUpdates: boolean;
  enablePerformanceMonitoring: boolean;
  defaultCollaborationMode: CollaborationMode;
  autoSaveInterval: number;
  maxHistorySize: number;
}

// 所有类型已通过单独的export语句导出

// 枚举已在定义时导出，无需重复导出