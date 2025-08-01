/**
 * 股票相关类型定义
 */

// 股票基础信息
export interface StockInfo {
  stockCode: string;
  stockName: string;
  market: string;
  industry: string;
  fullName?: string;
  listDate?: string;
  totalShares?: number;
  marketCap?: number;
  netAssetPerShare?: number;
  earningsPerShare?: number;
  peRatio?: number;
  pbRatio?: number;
  dividendYield?: number;
  roe?: number;
  roa?: number;
  grossMargin?: number;
  netMargin?: number;
  debtToAssetRatio?: number;
  currentRatio?: number;
  quickRatio?: number;
  status: 'NORMAL' | 'SUSPENDED' | 'DELISTED';
  isStStock: boolean;
  description?: string;
  dataSource?: string;
  createdAt?: string;
  updatedAt?: string;
}

// 股票价格信息
export interface StockPrice {
  stockCode: string;
  priceTime: string;
  priceType: 'REAL_TIME' | 'DAILY' | 'WEEKLY' | 'MONTHLY';
  openPrice: number;
  highPrice: number;
  lowPrice: number;
  closePrice: number;
  volume: number;
  turnover: number;
  turnoverRate?: number;
  change?: number;
  changePercent?: number;
  amplitude?: number;
  peRatio?: number;
  pbRatio?: number;
  totalMarketCap?: number;
  circulatingMarketCap?: number;
  buyOnePrice?: number;
  buyOneVolume?: number;
  sellOnePrice?: number;
  sellOneVolume?: number;
  high52Week?: number;
  low52Week?: number;
  tradingStatus?: string;
  dataSource?: string;
  createdAt?: string;
}

// K线数据
export interface KLineData {
  time: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
  turnover?: number;
  change?: number;
  changePercent?: number;
}

// 分析任务
export interface AnalysisTask {
  id: number;
  requestId: string;
  stockCode: string;
  stockName: string;
  userId?: string;
  analysisType: string;
  analysisDepth: 'BASIC' | 'STANDARD' | 'DEEP';
  timePeriod: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  priority: number;
  progress: number;
  currentStep?: string;
  participatingAgents?: string[];
  analysisParameters?: Record<string, any>;
  analysisResult?: string;
  investmentRecommendation?: 'BUY' | 'SELL' | 'HOLD';
  riskLevel?: 'LOW' | 'MEDIUM' | 'HIGH';
  confidenceScore?: number;
  targetPrice?: number;
  currentPrice?: number;
  upwardPotential?: number;
  keyPoints?: string[];
  riskWarnings?: string[];
  errorMessage?: string;
  startTime?: string;
  endTime?: string;
  processingDuration?: number;
  callbackUrl?: string;
  tags?: string[];
  remarks?: string;
  dataSource?: string;
  retryCount: number;
  createdAt: string;
  updatedAt: string;
}

// 分析结果
export interface AnalysisResult {
  id: number;
  taskId: number;
  stockCode: string;
  agentType: string;
  analysisType: string;
  analysisResult: string;
  investmentRecommendation?: 'BUY' | 'SELL' | 'HOLD';
  riskLevel?: 'LOW' | 'MEDIUM' | 'HIGH';
  confidenceScore?: number;
  targetPrice?: number;
  currentPrice?: number;
  upwardPotential?: number;
  technicalData?: Record<string, any>;
  fundamentalData?: Record<string, any>;
  sentimentData?: Record<string, any>;
  keyPoints?: string[];
  riskWarnings?: string[];
  supportLevel?: number;
  resistanceLevel?: number;
  trendAnalysis?: string;
  volumeAnalysis?: string;
  financialRatios?: Record<string, number>;
  valuationAnalysis?: string;
  industryComparison?: string;
  newsAnalysis?: string;
  socialSentiment?: string;
  marketSentiment?: string;
  analysisWeight?: number;
  analysisScore?: number;
  processingTime?: number;
  dataSource?: string;
  version?: string;
  rawData?: Record<string, any>;
  remarks?: string;
  createdAt: string;
}

// 智能体信息
export interface Agent {
  name: string;
  agentType: string;
  description?: string;
  version?: string;
  status: 'ACTIVE' | 'INACTIVE' | 'BUSY' | 'ERROR';
  specialization?: string;
  supportedAnalysisTypes?: string[];
  configParameters?: Record<string, any>;
  systemPrompt?: string;
  concurrentTasks: number;
  maxConcurrentTasks: number;
  timeoutSeconds: number;
  priority: number;
  weight?: number;
  successRate?: number;
  averageResponseTime?: number;
  totalTasks: number;
  completedTasks: number;
  failedTasks: number;
  confidenceScore?: number;
  accuracyRate?: number;
  lastActiveTime?: string;
  lastTaskTime?: string;
  healthCheckUrl?: string;
  apiEndpoint?: string;
  apiKey?: string;
  modelName?: string;
  temperature?: number;
  maxTokens?: number;
  enabled: boolean;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

// 市场概况
export interface MarketOverview {
  totalStocks: number;
  tradingStocks: number;
  suspendedStocks: number;
  risingStocks: number;
  fallingStocks: number;
  unchangedStocks: number;
  limitUpStocks: number;
  limitDownStocks: number;
  totalVolume: number;
  totalTurnover: number;
  averageChange: number;
  marketSentiment: 'BULLISH' | 'BEARISH' | 'NEUTRAL';
  topGainers: StockPrice[];
  topLosers: StockPrice[];
  mostActive: StockPrice[];
  updateTime: string;
}

// 行业分析
export interface IndustryAnalysis {
  industry: string;
  stockCount: number;
  averageChange: number;
  totalMarketCap: number;
  averagePE: number;
  averagePB: number;
  averageROE: number;
  topStocks: StockInfo[];
  sentiment: 'POSITIVE' | 'NEGATIVE' | 'NEUTRAL';
  analysisTime: string;
}

// 搜索参数
export interface StockSearchParams {
  keyword?: string;
  market?: string;
  industry?: string;
  status?: string;
  minMarketCap?: number;
  maxMarketCap?: number;
  minPE?: number;
  maxPE?: number;
  minPB?: number;
  maxPB?: number;
  minROE?: number;
  maxROE?: number;
  page?: number;
  size?: number;
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
}

// 分析任务搜索参数
export interface TaskSearchParams {
  stockCode?: string;
  userId?: string;
  status?: string;
  analysisType?: string;
  startTime?: string;
  endTime?: string;
  page?: number;
  size?: number;
}

// 分析结果搜索参数
export interface ResultSearchParams {
  stockCode?: string;
  analysisType?: string;
  agentType?: string;
  recommendation?: string;
  riskLevel?: string;
  startTime?: string;
  endTime?: string;
  page?: number;
  size?: number;
}

// 统计数据
export interface Statistics {
  totalTasks: number;
  completedTasks: number;
  failedTasks: number;
  runningTasks: number;
  pendingTasks: number;
  averageProcessingTime: number;
  successRate: number;
  averageConfidence: number;
  analysisTypeStats: Record<string, number>;
  recommendationStats: Record<string, number>;
  riskLevelStats: Record<string, number>;
  agentStats: Record<string, number>;
}

// WebSocket消息类型
export interface WebSocketMessage {
  type: 'TASK_UPDATE' | 'PRICE_UPDATE' | 'ANALYSIS_COMPLETE' | 'SYSTEM_NOTIFICATION';
  data: any;
  timestamp: string;
}

// 任务更新消息
export interface TaskUpdateMessage {
  taskId: number;
  status: string;
  progress: number;
  currentStep?: string;
  message?: string;
}

// 价格更新消息
export interface PriceUpdateMessage {
  stockCode: string;
  price: number;
  change: number;
  changePercent: number;
  volume: number;
  timestamp: string;
}

// 分析完成消息
export interface AnalysisCompleteMessage {
  taskId: number;
  stockCode: string;
  result: AnalysisResult;
}

// API响应类型
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  code?: number;
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

// 图表数据点
export interface ChartDataPoint {
  x: string | number;
  y: number;
  [key: string]: any;
}

// 技术指标
export interface TechnicalIndicator {
  name: string;
  value: number;
  signal: 'BUY' | 'SELL' | 'HOLD';
  description?: string;
}

// 财务指标
export interface FinancialMetric {
  name: string;
  value: number;
  unit?: string;
  trend?: 'UP' | 'DOWN' | 'STABLE';
  industryAverage?: number;
  description?: string;
}

// 新闻情绪
export interface NewsSentiment {
  title: string;
  content: string;
  sentiment: 'POSITIVE' | 'NEGATIVE' | 'NEUTRAL';
  score: number;
  source: string;
  publishTime: string;
  url?: string;
}

// 投资组合
export interface Portfolio {
  id: number;
  name: string;
  description?: string;
  totalValue: number;
  totalCost: number;
  totalReturn: number;
  returnRate: number;
  holdings: PortfolioHolding[];
  createdAt: string;
  updatedAt: string;
}

// 投资组合持仓
export interface PortfolioHolding {
  stockCode: string;
  stockName: string;
  shares: number;
  averageCost: number;
  currentPrice: number;
  marketValue: number;
  totalCost: number;
  unrealizedGain: number;
  unrealizedGainRate: number;
  weight: number;
}

// 风险评估
export interface RiskAssessment {
  overallRisk: 'LOW' | 'MEDIUM' | 'HIGH';
  volatilityRisk: number;
  liquidityRisk: number;
  concentrationRisk: number;
  marketRisk: number;
  industryRisk: number;
  recommendations: string[];
  riskFactors: string[];
}

// 回测结果
export interface BacktestResult {
  strategy: string;
  startDate: string;
  endDate: string;
  initialCapital: number;
  finalValue: number;
  totalReturn: number;
  annualizedReturn: number;
  maxDrawdown: number;
  sharpeRatio: number;
  winRate: number;
  trades: Trade[];
  performanceChart: ChartDataPoint[];
}

// 交易记录
export interface Trade {
  stockCode: string;
  action: 'BUY' | 'SELL';
  shares: number;
  price: number;
  amount: number;
  date: string;
  reason?: string;
}

// 预警规则
export interface AlertRule {
  id: number;
  name: string;
  stockCode?: string;
  condition: string;
  threshold: number;
  operator: 'GT' | 'LT' | 'EQ' | 'GTE' | 'LTE';
  enabled: boolean;
  lastTriggered?: string;
  createdAt: string;
}

// 预警消息
export interface AlertMessage {
  id: number;
  ruleId: number;
  stockCode: string;
  message: string;
  level: 'INFO' | 'WARNING' | 'ERROR';
  read: boolean;
  createdAt: string;
}