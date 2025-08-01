/**
 * 股票分析系统API服务
 */

import axios, { AxiosResponse } from 'axios';
import {
  StockInfo,
  StockPrice,
  KLineData,
  AnalysisTask,
  AnalysisResult,
  Agent,
  MarketOverview,
  IndustryAnalysis,
  StockSearchParams,
  TaskSearchParams,
  ResultSearchParams,
  Statistics,
  ApiResponse,
  PageResponse
} from '../types/stock';

// 创建axios实例
const api = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 30000,
  headers: {'Content-Type': 'application/json',},
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 添加认证token
    const token = localStorage.getItem('token');
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
api.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // 处理认证失败
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// 股票数据API
export const stockApi = {
  // 获取股票信息
  getStockInfo: (stockCode: string): Promise<ApiResponse<StockInfo>> =>
    api.get(`/stocks/${stockCode}`).then(res => res.data),

  // 获取实时价格
  getRealTimePrice: (stockCode: string): Promise<ApiResponse<StockPrice>> =>
    api.get(`/stocks/${stockCode}/price`).then(res => res.data),

  // 获取K线数据
  getKLineData: (
    stockCode: string,
    period: string,
    startDate?: string,
    endDate?: string
  ): Promise<ApiResponse<KLineData[]>> =>
    api.get(`/stocks/${stockCode}/kline`, {
      params: {
        period,
        startDate,
        endDate
      }
    }).then(res => res.data),

  // 搜索股票
  searchStocks: (params: StockSearchParams): Promise<ApiResponse<PageResponse<StockInfo>>> =>
    api.get('/stocks/search', { params }).then(res => res.data),

  // 获取支持的股票列表
  getSupportedStocks: (): Promise<ApiResponse<string[]>> =>
    api.get('/stocks/supported').then(res => res.data),

  // 获取行业股票列表
  getIndustryStocks: (industry: string): Promise<ApiResponse<StockInfo[]>> =>
    api.get(`/stocks/industry/${industry}`).then(res => res.data),

  // 获取所有行业列表
  getAllIndustries: (): Promise<ApiResponse<string[]>> =>
    api.get('/stocks/industries').then(res => res.data),

  // 获取所有市场列表
  getAllMarkets: (): Promise<ApiResponse<string[]>> =>
    api.get('/stocks/markets').then(res => res.data),

  // 验证股票代码
  validateStockCode: (stockCode: string): Promise<ApiResponse<boolean>> =>
    api.get(`/stocks/validate/${stockCode}`).then(res => res.data),

  // 获取价格统计
  getPriceStatistics: (stockCode: string): Promise<ApiResponse<any>> =>
    api.get(`/stocks/${stockCode}/price-stats`).then(res => res.data),

  // 获取历史价格范围
  getHistoricalPriceRange: (
    stockCode: string,
    startDate: string,
    endDate: string
  ): Promise<ApiResponse<StockPrice[]>> =>
    api.get(`/stocks/${stockCode}/price-range`, {
      params: {
        startDate,
        endDate
      }
    }).then(res => res.data),

  // 获取涨跌幅榜
  getChangeRanking: (type: 'gainers' | 'losers', limit: number = 20): Promise<ApiResponse<StockPrice[]>> =>
    api.get(`/stocks/ranking/${type}`, { params: { limit } }).then(res => res.data),

  // 获取成交量榜
  getVolumeRanking: (limit: number = 20): Promise<ApiResponse<StockPrice[]>> =>
    api.get('/stocks/ranking/volume', { params: { limit } }).then(res => res.data),

  // 获取涨跌停股票
  getLimitStocks: (type: 'up' | 'down'): Promise<ApiResponse<StockPrice[]>> =>
    api.get(`/stocks/limit/${type}`).then(res => res.data),

  // 获取市场概况
  getMarketOverview: (): Promise<ApiResponse<MarketOverview>> =>
    api.get('/stocks/market-overview').then(res => res.data),

  // 获取行业分析
  getIndustryAnalysis: (industry?: string): Promise<ApiResponse<IndustryAnalysis[]>> =>
    api.get('/stocks/industry-analysis', { params: { industry } }).then(res => res.data),

  // 获取数据统计
  getDataStatistics: (): Promise<ApiResponse<any>> =>
    api.get('/stocks/statistics').then(res => res.data),

  // 批量获取股票信息
  getBatchStockInfo: (stockCodes: string[]): Promise<ApiResponse<StockInfo[]>> =>
    api.post('/stocks/batch/info', { stockCodes }).then(res => res.data),

  // 获取批量股票价格
  getBatchStockPrices: (stockCodes: string[]): Promise<ApiResponse<StockPrice[]>> =>
    api.post('/stocks/batch/prices', { stockCodes }).then(res => res.data),

  // 获取涨幅榜
  getTopGainers: (limit: number = 10): Promise<ApiResponse<StockPrice[]>> =>
    api.get('/stocks/ranking/gainers', { params: { limit } }).then(res => res.data),

  // 获取跌幅榜
  getTopLosers: (limit: number = 10): Promise<ApiResponse<StockPrice[]>> =>
    api.get('/stocks/ranking/losers', { params: { limit } }).then(res => res.data),

  // 获取最活跃股票
  getMostActive: (limit: number = 10): Promise<ApiResponse<StockPrice[]>> =>
    api.get('/stocks/ranking/active', { params: { limit } }).then(res => res.data),

  // 获取板块表现
  getSectorPerformance: (): Promise<ApiResponse<any[]>> =>
    api.get('/stocks/sector-performance').then(res => res.data),
};

// 分析任务API
export const analysisApi = {
  // 创建分析任务
  createTask: (taskData: Partial<AnalysisTask>): Promise<ApiResponse<AnalysisTask>> =>
    api.post('/analysis/tasks', taskData).then(res => res.data),

  // 获取任务详情
  getTask: (taskId: number): Promise<ApiResponse<AnalysisTask>> =>
    api.get(`/analysis/tasks/${taskId}`).then(res => res.data),

  // 搜索分析任务
  searchTasks: (params: TaskSearchParams): Promise<ApiResponse<PageResponse<AnalysisTask>>> =>
    api.get('/analysis/tasks/search', { params }).then(res => res.data),

  // 取消任务
  cancelTask: (taskId: number): Promise<ApiResponse<void>> =>
    api.post(`/analysis/tasks/${taskId}/cancel`).then(res => res.data),

  // 重试任务
  retryTask: (taskId: number): Promise<ApiResponse<AnalysisTask>> =>
    api.post(`/analysis/tasks/${taskId}/retry`).then(res => res.data),

  // 获取任务统计
  getTaskStatistics: (): Promise<ApiResponse<Statistics>> =>
    api.get('/analysis/tasks/statistics').then(res => res.data),

  // 获取用户任务统计
  getUserTaskStatistics: (userId: string): Promise<ApiResponse<any>> =>
    api.get(`/analysis/tasks/user/${userId}/statistics`).then(res => res.data),

  // 获取热门分析股票
  getHotAnalysisStocks: (limit: number = 10): Promise<ApiResponse<any[]>> =>
    api.get('/analysis/tasks/hot-stocks', { params: { limit } }).then(res => res.data),

  // 获取任务分析结果
  getTaskResults: (taskId: number): Promise<ApiResponse<AnalysisResult[]>> =>
    api.get(`/analysis/results/task/${taskId}`).then(res => res.data),

  // 获取股票最新分析结果
  getLatestStockAnalysis: (stockCode: string, limit: number = 10): Promise<ApiResponse<AnalysisResult[]>> =>
    api.get(`/analysis/results/stock/${stockCode}/latest`, { params: { limit } }).then(res => res.data),

  // 获取股票综合分析
  getComprehensiveAnalysis: (stockCode: string): Promise<ApiResponse<any>> =>
    api.get(`/analysis/results/stock/${stockCode}/comprehensive`).then(res => res.data),

  // 搜索分析结果
  searchResults: (params: ResultSearchParams): Promise<ApiResponse<PageResponse<AnalysisResult>>> =>
    api.get('/analysis/results/search', { params }).then(res => res.data),

  // 获取分析结果统计
  getResultStatistics: (): Promise<ApiResponse<any>> =>
    api.get('/analysis/results/statistics').then(res => res.data),

  // 获取推荐分布
  getRecommendationDistribution: (stockCode?: string, days: number = 30): Promise<ApiResponse<any>> =>
    api.get('/analysis/results/recommendation-distribution', {
      params: {
        stockCode,
        days
      }
    }).then(res => res.data),

  // 获取最近分析结果
  getRecentAnalysis: (limit: number = 5): Promise<ApiResponse<AnalysisResult[]>> =>
    api.get('/analysis/results/recent', { params: { limit } }).then(res => res.data),
};

// 智能体API
export const agentApi = {
  // 获取所有智能体
  getAllAgents: (): Promise<ApiResponse<Agent[]>> =>
    api.get('/agents').then(res => res.data),

  // 获取可用智能体
  getAvailableAgents: (): Promise<ApiResponse<Agent[]>> =>
    api.get('/agents/available').then(res => res.data),

  // 根据类型获取智能体
  getAgentsByType: (agentType: string): Promise<ApiResponse<Agent[]>> =>
    api.get(`/agents/type/${agentType}`).then(res => res.data),

  // 根据名称获取智能体
  getAgentByName: (agentName: string): Promise<ApiResponse<Agent>> =>
    api.get(`/agents/name/${agentName}`).then(res => res.data),

  // 获取最佳智能体
  getBestAgent: (analysisType: string): Promise<ApiResponse<Agent>> =>
    api.get('/agents/best', { params: { analysisType } }).then(res => res.data),

  // 分页查询智能体
  searchAgents: (params: any): Promise<ApiResponse<PageResponse<Agent>>> =>
    api.get('/agents/search', { params }).then(res => res.data),

  // 执行分析
  executeAnalysis: (
    agentName: string,
    stockCode: string,
    analysisType: string,
    parameters?: any
  ): Promise<ApiResponse<AnalysisResult>> =>
    api.post('/agents/analyze', {
      agentName,
      stockCode,
      analysisType,
      parameters
    }).then(res => res.data),

  // 创建智能体
  createAgent: (agent: Partial<Agent>): Promise<ApiResponse<Agent>> =>
    api.post('/agents', agent).then(res => res.data),

  // 更新智能体
  updateAgent: (agentName: string, agent: Partial<Agent>): Promise<ApiResponse<Agent>> =>
    api.put(`/agents/${agentName}`, agent).then(res => res.data),

  // 启用智能体
  enableAgent: (agentName: string): Promise<ApiResponse<void>> =>
    api.post(`/agents/${agentName}/enable`).then(res => res.data),

  // 禁用智能体
  disableAgent: (agentName: string): Promise<ApiResponse<void>> =>
    api.post(`/agents/${agentName}/disable`).then(res => res.data),

  // 重置智能体统计
  resetAgentStats: (agentName: string): Promise<ApiResponse<void>> =>
    api.post(`/agents/${agentName}/reset-stats`).then(res => res.data),

  // 执行健康检查
  performHealthCheck: (agentName: string): Promise<ApiResponse<any>> =>
    api.post(`/agents/${agentName}/health-check`).then(res => res.data),

  // 获取智能体性能统计
  getAgentPerformance: (): Promise<ApiResponse<any>> =>
    api.get('/agents/performance').then(res => res.data),

  // 获取系统统计
  getSystemStats: (): Promise<ApiResponse<any>> =>
    api.get('/agents/system-stats').then(res => res.data),

  // 获取智能体分析历史
  getAgentAnalysisHistory: (agentName: string, page: number = 0, size: number = 20): Promise<ApiResponse<PageResponse<AnalysisResult>>> =>
    api.get(`/agents/${agentName}/analysis-history`, {
      params: {
        page,
        size
      }
    }).then(res => res.data),

  // 获取智能体详细信息
  getAgentDetails: (agentName: string): Promise<ApiResponse<any>> =>
    api.get(`/agents/${agentName}/details`).then(res => res.data),

  // 批量启用智能体
  batchEnableAgents: (agentNames: string[]): Promise<ApiResponse<any>> =>
    api.post('/agents/batch/enable', agentNames).then(res => res.data),

  // 批量禁用智能体
  batchDisableAgents: (agentNames: string[]): Promise<ApiResponse<any>> =>
    api.post('/agents/batch/disable', agentNames).then(res => res.data),

  // 获取智能体类型列表
  getAgentTypes: (): Promise<ApiResponse<string[]>> =>
    api.get('/agents/types').then(res => res.data),

  // 获取分析类型列表
  getAnalysisTypes: (): Promise<ApiResponse<string[]>> =>
    api.get('/agents/analysis-types').then(res => res.data),
};

// 导出默认API实例
export default api;

// 导出所有API
export const stockAnalysisApi = {
  stock: stockApi,
  analysis: analysisApi,
  agent: agentApi,
};

// 工具函数
export const apiUtils = {
  // 处理API错误
  handleError: (error: any) => {
    console.error('API Error:', error);
    if (error.response) {
      return {
        success: false,
        message: error.response.data?.message || '请求失败',
        code: error.response.status,
      };
    } else if (error.request) {
      return {
        success: false,
        message: '网络连接失败',
        code: 0,
      };
    } else {
      return {
        success: false,
        message: error.message || '未知错误',
        code: -1,
      };
    }
  },

  // 格式化查询参数
  formatParams: (params: any) => {
    const formatted: any = {};
    Object.keys(params).forEach(key => {
      if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
        formatted[key] = params[key];
      }
    });
    return formatted;
  },

  // 构建分页参数
  buildPageParams: (page: number, size: number, sortBy?: string, sortOrder?: string) => {
    const params: any = {
      page,
      size
    };
    if (sortBy) params.sortBy = sortBy;
    if (sortOrder) params.sortOrder = sortOrder;
    return params;
  },
};