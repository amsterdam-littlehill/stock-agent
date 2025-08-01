import axios, { AxiosInstance, AxiosResponse } from 'axios';

// 创建axios实例
const api: AxiosInstance = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {'Content-Type': 'application/json',},
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 添加认证token
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
api.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // 处理未授权错误
      localStorage.removeItem('auth_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// 股票分析API
export const stockAnalysisApi = {
  // 获取股票基本信息
  getStockInfo: async (symbol: string) => {
    const response = await api.get(`/stocks/${symbol}/info`);
    return response.data;
  },

  // 获取股票历史数据
  getStockHistory: async (symbol: string, period: string = '1y') => {
    const response = await api.get(`/stocks/${symbol}/history`, {params: { period }});
    return response.data;
  },

  // 获取实时价格
  getRealTimePrice: async (symbol: string) => {
    const response = await api.get(`/stocks/${symbol}/realtime`);
    return response.data;
  },

  // 启动股票分析
  startAnalysis: async (symbol: string, analysisType: string, parameters?: any) => {
    const response = await api.post(`/analysis/start`, {
      symbol,
      analysisType,
      parameters
    });
    return response.data;
  },

  // 获取分析结果
  getAnalysisResult: async (analysisId: string) => {
    const response = await api.get(`/analysis/${analysisId}/result`);
    return response.data;
  },

  // 获取分析历史
  getAnalysisHistory: async (symbol?: string, limit: number = 50) => {
    const response = await api.get('/analysis/history', {
      params: {
        symbol,
        limit
      }
    });
    return response.data;
  },

  // 搜索股票
  searchStocks: async (query: string) => {
    const response = await api.get('/stocks/search', {params: { q: query }});
    return response.data;
  },

  // 获取热门股票
  getPopularStocks: async () => {
    const response = await api.get('/stocks/popular');
    return response.data;
  },

  // 获取市场概览
  getMarketOverview: async () => {
    const response = await api.get('/market/overview');
    return response.data;
  },

  // 获取智能体状态
  getAgentStatus: async () => {
    const response = await api.get('/agents/status');
    return response.data;
  },

  // 启动协作分析
  startCollaboration: async (params: {
    symbol: string;
    mode: string;
    agentTypes: string[];
    parameters?: any;
  }) => {
    const response = await api.post('/collaboration/start', params);
    return response.data;
  },

  // 获取协作状态
  getCollaborationStatus: async (sessionId: string) => {
    const response = await api.get(`/collaboration/${sessionId}/status`);
    return response.data;
  },

  // 停止协作
  stopCollaboration: async (sessionId: string) => {
    const response = await api.post(`/collaboration/${sessionId}/stop`);
    return response.data;
  }
};

export default api;
