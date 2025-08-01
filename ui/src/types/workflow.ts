// 工作流相关类型定义

// 工作流定义
export interface WorkflowDefinition {
  id: string;
  name: string;
  description: string;
  version: string;
  status: 'ACTIVE' | 'INACTIVE' | 'DRAFT';
  definition: WorkflowGraph;
  tags: string[];
  nodeCount: number;
  executionCount: number;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

// 工作流图结构
export interface WorkflowGraph {
  nodes: WorkflowNode[];
  edges: WorkflowEdge[];
  variables: WorkflowVariable[];
  settings: WorkflowSettings;
}

// 工作流节点
export interface WorkflowNode {
  id: string;
  type: WorkflowNodeType;
  name: string;
  description?: string;
  position: Position;
  properties: Record<string, any>;
  inputs: NodePort[];
  outputs: NodePort[];
  status?: NodeExecutionStatus;
  error?: string;
}

// 工作流边（连接线）
export interface WorkflowEdge {
  id: string;
  sourceNodeId: string;
  sourcePortId: string;
  targetNodeId: string;
  targetPortId: string;
  condition?: EdgeCondition;
  label?: string;
}

// 节点端口
export interface NodePort {
  id: string;
  name: string;
  type: PortType;
  dataType: DataType;
  required: boolean;
  description?: string;
}

// 位置信息
export interface Position {
  x: number;
  y: number;
}

// 工作流变量
export interface WorkflowVariable {
  id: string;
  name: string;
  type: DataType;
  value: any;
  description?: string;
  scope: 'GLOBAL' | 'LOCAL';
}

// 工作流设置
export interface WorkflowSettings {
  timeout: number;
  retryCount: number;
  parallelism: number;
  errorHandling: 'STOP' | 'CONTINUE' | 'RETRY';
  notifications: NotificationSettings;
}

// 通知设置
export interface NotificationSettings {
  onSuccess: boolean;
  onFailure: boolean;
  onStart: boolean;
  email?: string[];
  webhook?: string;
}

// 边条件
export interface EdgeCondition {
  type: 'ALWAYS' | 'SUCCESS' | 'FAILURE' | 'EXPRESSION';
  expression?: string;
}

// 工作流执行
export interface WorkflowExecution {
  id: string;
  workflowId: string;
  workflowName: string;
  workflowVersion: string;
  status: ExecutionStatus;
  input: Record<string, any>;
  output?: Record<string, any>;
  error?: string;
  startTime: string;
  endTime?: string;
  duration?: number;
  testMode: boolean;
  triggeredBy: string;
  executionContext: ExecutionContext;
}

// 执行上下文
export interface ExecutionContext {
  userId: string;
  sessionId: string;
  environment: 'DEVELOPMENT' | 'TESTING' | 'PRODUCTION';
  variables: Record<string, any>;
  metadata: Record<string, any>;
}

// 工作流执行步骤
export interface WorkflowExecutionStep {
  id: string;
  executionId: string;
  nodeId: string;
  nodeName: string;
  nodeType: WorkflowNodeType;
  status: NodeExecutionStatus;
  input?: Record<string, any>;
  output?: Record<string, any>;
  error?: string;
  startTime?: string;
  endTime?: string;
  duration?: number;
  retryCount: number;
  stepIndex: number;
}

// 工作流执行日志
export interface WorkflowExecutionLog {
  id: string;
  executionId: string;
  stepId?: string;
  nodeId?: string;
  level: LogLevel;
  message: string;
  timestamp: string;
  metadata?: Record<string, any>;
}

// 工作流统计
export interface WorkflowStatistics {
  totalWorkflows: number;
  activeWorkflows: number;
  totalExecutions: number;
  successfulExecutions: number;
  failedExecutions: number;
  averageExecutionTime: number;
  executionsByStatus: Record<ExecutionStatus, number>;
  executionsByDay: Array<{
    date: string;
    count: number;
    successCount: number;
    failureCount: number;
  }>;
  topWorkflows: Array<{
    workflowId: string;
    workflowName: string;
    executionCount: number;
    successRate: number;
  }>;
}

// 节点模板
export interface NodeTemplate {
  type: WorkflowNodeType;
  name: string;
  description: string;
  category: NodeCategory;
  icon: string;
  color: string;
  defaultProperties: Record<string, any>;
  inputs: NodePortTemplate[];
  outputs: NodePortTemplate[];
  configSchema: any; // JSON Schema
}

// 节点端口模板
export interface NodePortTemplate {
  name: string;
  type: PortType;
  dataType: DataType;
  required: boolean;
  description?: string;
  defaultValue?: any;
}

// 工作流模板
export interface WorkflowTemplate {
  id: string;
  name: string;
  description: string;
  category: string;
  tags: string[];
  thumbnail?: string;
  definition: WorkflowGraph;
  variables: WorkflowVariable[];
  documentation?: string;
  author: string;
  version: string;
  createdAt: string;
  downloads: number;
  rating: number;
}

// 枚举类型定义

// 工作流节点类型
export enum WorkflowNodeType {
  // 控制节点
  START = 'START',
  END = 'END',
  CONDITION = 'CONDITION',
  LOOP = 'LOOP',
  PARALLEL = 'PARALLEL',
  MERGE = 'MERGE',
  DELAY = 'DELAY',

  // 数据节点
  INPUT = 'INPUT',
  OUTPUT = 'OUTPUT',
  VARIABLE = 'VARIABLE',
  TRANSFORM = 'TRANSFORM',
  FILTER = 'FILTER',
  AGGREGATE = 'AGGREGATE',

  // 智能体节点
  AGENT_CALL = 'AGENT_CALL',
  AGENT_CHAIN = 'AGENT_CHAIN',

  // MCP工具节点
  MCP_TOOL = 'MCP_TOOL',

  // 外部服务节点
  HTTP_REQUEST = 'HTTP_REQUEST',
  DATABASE_QUERY = 'DATABASE_QUERY',
  FILE_OPERATION = 'FILE_OPERATION',
  EMAIL = 'EMAIL',

  // 股票相关节点
  STOCK_DATA = 'STOCK_DATA',
  STOCK_ANALYSIS = 'STOCK_ANALYSIS',
  MARKET_DATA = 'MARKET_DATA',
  NEWS_FETCH = 'NEWS_FETCH',

  // 脚本节点
  JAVASCRIPT = 'JAVASCRIPT',
  PYTHON = 'PYTHON',
  GROOVY = 'GROOVY',

  // 通知节点
  NOTIFICATION = 'NOTIFICATION',
  WEBHOOK = 'WEBHOOK',

  // 自定义节点
  CUSTOM = 'CUSTOM'
}

// 节点分类
export enum NodeCategory {
  CONTROL = 'CONTROL',
  DATA = 'DATA',
  AGENT = 'AGENT',
  TOOL = 'TOOL',
  SERVICE = 'SERVICE',
  STOCK = 'STOCK',
  SCRIPT = 'SCRIPT',
  NOTIFICATION = 'NOTIFICATION',
  CUSTOM = 'CUSTOM'
}

// 端口类型
export enum PortType {
  INPUT = 'INPUT',
  OUTPUT = 'OUTPUT'
}

// 数据类型
export enum DataType {
  STRING = 'STRING',
  NUMBER = 'NUMBER',
  BOOLEAN = 'BOOLEAN',
  OBJECT = 'OBJECT',
  ARRAY = 'ARRAY',
  DATE = 'DATE',
  FILE = 'FILE',
  ANY = 'ANY'
}

// 执行状态
export enum ExecutionStatus {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  PAUSED = 'PAUSED',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}

// 节点执行状态
export enum NodeExecutionStatus {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  SKIPPED = 'SKIPPED'
}

// 日志级别
export enum LogLevel {
  ERROR = 'ERROR',
  WARN = 'WARN',
  INFO = 'INFO',
  DEBUG = 'DEBUG'
}

// API 响应类型
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  code?: string;
  timestamp: string;
}

// 分页响应
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// 工作流搜索参数
export interface WorkflowSearchParams {
  keyword?: string;
  status?: string;
  tags?: string[];
  createdBy?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
  sort?: string;
}

// 执行搜索参数
export interface ExecutionSearchParams {
  workflowId?: string;
  status?: ExecutionStatus;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
  sort?: string;
}

// 工作流验证结果
export interface WorkflowValidationResult {
  isValid: boolean;
  errors: string[];
  warnings: string[];
}

// 节点验证结果
export interface NodeValidationResult {
  nodeId: string;
  isValid: boolean;
  errors: string[];
  warnings: string[];
}

// 工作流导入/导出格式
export interface WorkflowExportData {
  workflow: WorkflowDefinition;
  metadata: {
    exportedAt: string;
    exportedBy: string;
    version: string;
  };
}

// WebSocket 消息类型
export interface WorkflowWebSocketMessage {
  type: 'execution_update' | 'step_update' | 'log_update' | 'error';
  executionId: string;
  execution?: WorkflowExecution;
  step?: WorkflowExecutionStep;
  log?: WorkflowExecutionLog;
  error?: string;
  timestamp: string;
}

// 工作流权限
export interface WorkflowPermission {
  id: string;
  workflowId: string;
  userId: string;
  permission: 'READ' | 'WRITE' | 'EXECUTE' | 'ADMIN';
  grantedBy: string;
  grantedAt: string;
}

// 工作流分享
export interface WorkflowShare {
  id: string;
  workflowId: string;
  shareToken: string;
  permissions: string[];
  expiresAt?: string;
  createdBy: string;
  createdAt: string;
  accessCount: number;
}