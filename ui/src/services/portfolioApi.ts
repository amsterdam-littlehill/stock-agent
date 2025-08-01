import axios from 'axios';

// API基础配置
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const portfolioApiClient = axios.create({
  baseURL: `${API_BASE_URL}/api/portfolios`,
  timeout: 30000,
  headers: {'Content-Type': 'application/json',},
});

// 请求拦截器
portfolioApiClient.interceptors.request.use(
  (config) => {
    // 添加认证token（如果有）
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
portfolioApiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // 处理认证失败
      localStorage.removeItem('auth_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// 投资组合相关接口类型定义
export interface Portfolio {
  id: number;
  userId: string;
  name: string;
  description: string;
  investmentObjective: string;
  riskTolerance: string;
  initialCapital: number;
  currentValue: number;
  cashBalance: number;
  totalReturn: number;
  totalReturnPercent: number;
  todayReturn: number;
  todayReturnPercent: number;
  sharpeRatio: number;
  maxDrawdown: number;
  volatility: number;
  beta: number;
  status: string;
  benchmarkCode: string;
  relativeReturn: number;
  informationRatio: number;
  trackingError: number;
  holdingsCount: number;
  transactionCount: number;
  winRate: number;
  avgHoldingDays: number;
  turnoverRate: number;
  createdAt: string;
  updatedAt: string;
  lastRebalanced: string;
  isPublic: boolean;
  autoRebalanceEnabled: boolean;
  rebalanceThreshold: number;
  rebalanceFrequency: string;
  tags: string[];
  notes: string;
  configParams: Record<string, any>;
}

export interface PortfolioHolding {
  id: number;
  portfolioId: number;
  stockCode: string;
  stockName: string;
  quantity: number;
  avgCost: number;
  currentPrice: number;
  marketValue: number;
  totalCost: number;
  unrealizedPnl: number;
  unrealizedPnlPercent: number;
  todayPnl: number;
  todayPnlPercent: number;
  weight: number;
  targetWeight: number;
  sector: string;
  market: string;
  currency: string;
  status: string;
  isCoreHolding: boolean;
  firstBuyDate: string;
  lastTradeDate: string;
  dividendReceived: number;
  riskLevel: string;
  investmentRating: string;
  targetPrice: number;
  stopLoss: number;
  takeProfit: number;
  notes: string;
}

export interface PortfolioTransaction {
  id: number;
  portfolioId: number;
  stockCode: string;
  stockName: string;
  transactionType: string;
  quantity: number;
  price: number;
  amount: number;
  fee: number;
  realizedPnl: number;
  transactionTime: string;
  market: string;
  currency: string;
  status: string;
  source: string;
  analysisTaskId: number;
  agentRecommendationId: number;
  orderNumber: string;
  brokerId: string;
  brokerTransactionId: string;
  isT0: boolean;
  isMarginTrading: boolean;
  isConfirmed: boolean;
  notes: string;
}

export interface PortfolioSummary {
  portfolio: Portfolio;
  holdingsCount: number;
  totalMarketValue: number;
  totalCost: number;
  totalPnl: number;
  todayPnl: number;
  cashBalance: number;
}

export interface PortfolioDetail {
  portfolio: Portfolio;
  holdings: PortfolioHolding[];
  recentTransactions: PortfolioTransaction[];
  metrics: PortfolioMetrics;
  allocation: AssetAllocationAnalysis;
  riskAnalysis: PortfolioRiskAnalysis;
  performance: PerformanceAnalysis;
}

export interface PortfolioMetrics {
  totalValue: number;
  totalReturn: number;
  totalReturnPercent: number;
  annualizedReturn: number;
  volatility: number;
  sharpeRatio: number;
  maxDrawdown: number;
  beta: number;
  alpha: number;
  informationRatio: number;
  trackingError: number;
  winRate: number;
  profitLossRatio: number;
  avgHoldingPeriod: number;
  turnoverRate: number;
}

export interface AssetAllocationAnalysis {
  byStock: Array<{ stockCode: string; weight: number; value: number }>;
  bySector: Array<{ sector: string; weight: number; value: number }>;
  byMarket: Array<{ market: string; weight: number; value: number }>;
  byRiskLevel: Array<{ riskLevel: string; weight: number; value: number }>;
  cashWeight: number;
  concentrationRisk: number;
  diversificationScore: number;
}

export interface PortfolioRiskAnalysis {
  overallRisk: string;
  riskScore: number;
  var95: number;
  var99: number;
  expectedShortfall: number;
  concentrationRisk: number;
  sectorConcentration: Array<{ sector: string; weight: number; risk: string }>;
  correlationMatrix: Array<Array<number>>;
  riskContribution: Array<{ stockCode: string; contribution: number }>;
  stressTestResults: Array<{ scenario: string; impact: number }>;
}

export interface PerformanceAnalysis {
  dailyReturns: Array<{ date: string; return: number; cumReturn: number }>;
  monthlyReturns: Array<{ month: string; return: number }>;
  yearlyReturns: Array<{ year: string; return: number }>;
  benchmarkComparison: Array<{ date: string; portfolioReturn: number; benchmarkReturn: number }>;
  rollingMetrics: Array<{ date: string; sharpe: number; volatility: number; maxDrawdown: number }>;
  attribution: {
    stockSelection: number;
    sectorAllocation: number;
    interaction: number;
    total: number;
  };
}

export interface CreatePortfolioRequest {
  name: string;
  description: string;
  investmentObjective: string;
  riskTolerance: string;
  initialCapital: number;
  benchmarkCode?: string;
  autoRebalanceEnabled?: boolean;
  rebalanceThreshold?: number;
  rebalanceFrequency?: string;
  tags?: string[];
  notes?: string;
}

export interface UpdatePortfolioRequest {
  name?: string;
  description?: string;
  investmentObjective?: string;
  riskTolerance?: string;
  benchmarkCode?: string;
  autoRebalanceEnabled?: boolean;
  rebalanceThreshold?: number;
  rebalanceFrequency?: string;
  tags?: string[];
  notes?: string;
}

export interface AddHoldingRequest {
  stockCode: string;
  quantity: number;
  price: number;
  targetWeight?: number;
  notes?: string;
}

export interface SellHoldingRequest {
  stockCode: string;
  quantity: number;
  price: number;
  notes?: string;
}

export interface RebalanceRequest {
  targetAllocations: Record<string, number>;
  execute: boolean;
  notes?: string;
}

export interface RebalanceResult {
  portfolioId: number;
  currentValue: number;
  actions: RebalanceAction[];
  executed: boolean;
  rebalanceDate: string;
}

export interface RebalanceAction {
  stockCode: string;
  action: string; // BUY, SELL, HOLD
  currentWeight: number;
  targetWeight: number;
  currentQuantity: number;
  targetQuantity: number;
  quantityChange: number;
  estimatedAmount: number;
  reason: string;
}

export interface OptimizationRequest {
  objective: string; // MAX_RETURN, MIN_RISK, MAX_SHARPE
  constraints: {
    maxWeight?: number;
    minWeight?: number;
    sectorLimits?: Record<string, number>;
    turnoverLimit?: number;
  };
  timeHorizon: string;
  riskTolerance: string;
}

export interface PortfolioOptimizationResult {
  portfolioId: number;
  objective: string;
  recommendedAllocations: Record<string, number>;
  expectedReturn: number;
  expectedRisk: number;
  expectedSharpe: number;
  improvementMetrics: {
    returnImprovement: number;
    riskReduction: number;
    sharpeImprovement: number;
  };
  rebalanceActions: RebalanceAction[];
  optimizationDate: string;
}

// 投资组合API服务
export const portfolioApi = {
  // 获取投资组合列表
  getPortfolioList: (userId: string) => {
    return portfolioApiClient.get<PortfolioSummary[]>('', {params: { userId }});
  },

  // 获取投资组合详情
  getPortfolioDetail: (portfolioId: number) => {
    return portfolioApiClient.get<PortfolioDetail>(`/${portfolioId}`);
  },

  // 创建投资组合
  createPortfolio: (request: CreatePortfolioRequest) => {
    return portfolioApiClient.post<Portfolio>('', request);
  },

  // 更新投资组合
  updatePortfolio: (portfolioId: number, request: UpdatePortfolioRequest) => {
    return portfolioApiClient.put<string>(`/${portfolioId}`, request);
  },

  // 删除投资组合
  deletePortfolio: (portfolioId: number) => {
    return portfolioApiClient.delete<string>(`/${portfolioId}`);
  },

  // 添加持仓
  addHolding: (portfolioId: number, request: AddHoldingRequest) => {
    return portfolioApiClient.post<string>(`/${portfolioId}/holdings`, request);
  },

  // 卖出持仓
  sellHolding: (portfolioId: number, request: SellHoldingRequest) => {
    return portfolioApiClient.post<string>(`/${portfolioId}/holdings/sell`, request);
  },

  // 投资组合再平衡
  rebalancePortfolio: (portfolioId: number, request: RebalanceRequest) => {
    return portfolioApiClient.post<RebalanceResult>(`/${portfolioId}/rebalance`, request);
  },

  // 投资组合优化
  optimizePortfolio: (portfolioId: number, request: OptimizationRequest) => {
    return portfolioApiClient.post<PortfolioOptimizationResult>(`/${portfolioId}/optimize`, request);
  },

  // 获取投资组合性能指标
  getPortfolioMetrics: (portfolioId: number) => {
    return portfolioApiClient.get<PortfolioMetrics>(`/${portfolioId}/metrics`);
  },

  // 获取资产配置分析
  getAssetAllocation: (portfolioId: number) => {
    return portfolioApiClient.get<AssetAllocationAnalysis>(`/${portfolioId}/allocation`);
  },

  // 获取风险分析
  getRiskAnalysis: (portfolioId: number) => {
    return portfolioApiClient.get<PortfolioRiskAnalysis>(`/${portfolioId}/risk`);
  },

  // 获取业绩分析
  getPerformanceAnalysis: (portfolioId: number, timeRange?: string) => {
    return portfolioApiClient.get<PerformanceAnalysis>(`/${portfolioId}/performance`, {params: timeRange ? { timeRange } : {}});
  },

  // 获取投资组合统计
  getPortfolioStatistics: (userId: string) => {
    return portfolioApiClient.get('/statistics', {params: { userId }});
  },

  // 搜索投资组合
  searchPortfolios: (userId: string, keyword: string) => {
    return portfolioApiClient.get<PortfolioSummary[]>('/search', {
      params: {
        userId,
        keyword
      }
    });
  },

  // 获取投资组合持仓列表
  getPortfolioHoldings: (portfolioId: number) => {
    return portfolioApiClient.get<PortfolioHolding[]>(`/${portfolioId}/holdings`);
  },

  // 获取投资组合交易记录
  getPortfolioTransactions: (portfolioId: number, page?: number, size?: number) => {
    return portfolioApiClient.get<{
      transactions: PortfolioTransaction[];
      total: number;
      page: number;
      size: number;
    }>(`/${portfolioId}/transactions`, {
      params: {
        page,
        size
      }
    });
  },

  // 导出投资组合数据
  exportPortfolio: (portfolioId: number, format: 'excel' | 'pdf' | 'csv') => {
    return portfolioApiClient.get(`/${portfolioId}/export`, {
      params: { format },
      responseType: 'blob'
    });
  },

  // 导入投资组合数据
  importPortfolio: (userId: string, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('userId', userId);

    return portfolioApiClient.post('/import', formData, {headers: {'Content-Type': 'multipart/form-data'}});
  },

  // 删除交易记录
  deleteTransaction: (transactionId: number) => {
    return portfolioApiClient.delete<string>(`/transactions/${transactionId}`);
  }
};

export default portfolioApi;