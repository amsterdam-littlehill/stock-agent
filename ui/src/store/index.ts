/**
 * 应用状态管理
 * 使用Zustand进行状态管理
 */

import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';
import {
  StockInfo,
  StockPrice,
  AnalysisTask,
  AnalysisResult,
  Agent,
  MarketOverview,
  Statistics
} from '../types/stock';

// 应用主状态
interface AppState {
  // 用户信息
  user: {
    id?: string;
    name?: string;
    email?: string;
    token?: string;
    isAuthenticated: boolean;
  };

  // 主题设置
  theme: 'light' | 'dark';

  // 语言设置
  language: 'zh-CN' | 'en-US';

  // 侧边栏状态
  sidebarCollapsed: boolean;

  // 全局加载状态
  globalLoading: boolean;

  // 错误信息
  error: string | null;

  // WebSocket连接状态
  wsConnected: boolean;

  // 操作方法
  setUser: (user: Partial<AppState['user']>) => void;
  setTheme: (theme: AppState['theme']) => void;
  setLanguage: (language: AppState['language']) => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  setGlobalLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  setWsConnected: (connected: boolean) => void;
  logout: () => void;
}

// 股票数据状态
interface StockState {
  // 当前选中的股票
  selectedStock: StockInfo | null;

  // 股票列表
  stockList: StockInfo[];

  // 实时价格数据
  realTimePrices: Map<string, StockPrice>;

  // 市场概况
  marketOverview: MarketOverview | null;

  // 行业列表
  industries: string[];

  // 市场列表
  markets: string[];

  // 搜索历史
  searchHistory: string[];

  // 关注列表
  watchList: string[];

  // 操作方法
  setSelectedStock: (stock: StockInfo | null) => void;
  setStockList: (stocks: StockInfo[]) => void;
  updateRealTimePrice: (stockCode: string, price: StockPrice) => void;
  setMarketOverview: (overview: MarketOverview) => void;
  setIndustries: (industries: string[]) => void;
  setMarkets: (markets: string[]) => void;
  addSearchHistory: (keyword: string) => void;
  clearSearchHistory: () => void;
  addToWatchList: (stockCode: string) => void;
  removeFromWatchList: (stockCode: string) => void;
  clearWatchList: () => void;
}

// 分析任务状态
interface AnalysisState {
  // 任务列表
  tasks: AnalysisTask[];

  // 当前任务
  currentTask: AnalysisTask | null;

  // 分析结果
  results: AnalysisResult[];

  // 任务统计
  statistics: Statistics | null;

  // 分析类型列表
  analysisTypes: string[];

  // 操作方法
  setTasks: (tasks: AnalysisTask[]) => void;
  addTask: (task: AnalysisTask) => void;
  updateTask: (taskId: number, updates: Partial<AnalysisTask>) => void;
  removeTask: (taskId: number) => void;
  setCurrentTask: (task: AnalysisTask | null) => void;
  setResults: (results: AnalysisResult[]) => void;
  addResult: (result: AnalysisResult) => void;
  setStatistics: (statistics: Statistics) => void;
  setAnalysisTypes: (types: string[]) => void;
  clearTasks: () => void;
  clearResults: () => void;
}

// 智能体状态
interface AgentState {
  // 智能体列表
  agents: Agent[];

  // 可用智能体
  availableAgents: Agent[];

  // 智能体类型
  agentTypes: string[];

  // 智能体性能统计
  agentPerformance: any;

  // 操作方法
  setAgents: (agents: Agent[]) => void;
  setAvailableAgents: (agents: Agent[]) => void;
  updateAgent: (agentName: string, updates: Partial<Agent>) => void;
  setAgentTypes: (types: string[]) => void;
  setAgentPerformance: (performance: any) => void;
}

// 创建应用主状态store
export const useAppStore = create<AppState>()(devtools(
  persist(
    (set) => ({
      user: {isAuthenticated: false,},
      theme: 'light',
      language: 'zh-CN',
      sidebarCollapsed: false,
      globalLoading: false,
      error: null,
      wsConnected: false,

      setUser: (user) => set((state) => ({
        user: {
          ...state.user,
          ...user
        }
      }), false, 'setUser'),

      setTheme: (theme) => set({ theme }, false, 'setTheme'),

      setLanguage: (language) => set({ language }, false, 'setLanguage'),

      setSidebarCollapsed: (collapsed) => set({ sidebarCollapsed: collapsed }, false, 'setSidebarCollapsed'),

      setGlobalLoading: (loading) => set({ globalLoading: loading }, false, 'setGlobalLoading'),

      setError: (error) => set({ error }, false, 'setError'),

      setWsConnected: (connected) => set({ wsConnected: connected }, false, 'setWsConnected'),

      logout: () => set({
        user: { isAuthenticated: false },
        error: null,
      }, false, 'logout'),
    }),
    {
      name: 'app-store',
      partialize: (state) => ({
        user: state.user,
        theme: state.theme,
        language: state.language,
        sidebarCollapsed: state.sidebarCollapsed,
      }),
    }
  ),
  { name: 'AppStore' }
));

// 创建股票数据状态store
export const useStockStore = create<StockState>()(devtools(
  persist(
    (set) => ({
      selectedStock: null,
      stockList: [],
      realTimePrices: new Map(),
      marketOverview: null,
      industries: [],
      markets: [],
      searchHistory: [],
      watchList: [],

      setSelectedStock: (stock) => set({ selectedStock: stock }, false, 'setSelectedStock'),

      setStockList: (stocks) => set({ stockList: stocks }, false, 'setStockList'),

      updateRealTimePrice: (stockCode, price) => set((state) => {
        const newPrices = new Map(state.realTimePrices);
        newPrices.set(stockCode, price);
        return { realTimePrices: newPrices };
      }, false, 'updateRealTimePrice'),

      setMarketOverview: (overview) => set({ marketOverview: overview }, false, 'setMarketOverview'),

      setIndustries: (industries) => set({ industries }, false, 'setIndustries'),

      setMarkets: (markets) => set({ markets }, false, 'setMarkets'),

      addSearchHistory: (keyword) => set((state) => {
        const history = [...state.searchHistory];
        const index = history.indexOf(keyword);
        if (index > -1) {
          history.splice(index, 1);
        }
        history.unshift(keyword);
        return { searchHistory: history.slice(0, 10) }; // 只保留最近10条
      }, false, 'addSearchHistory'),

      clearSearchHistory: () => set({ searchHistory: [] }, false, 'clearSearchHistory'),

      addToWatchList: (stockCode) => set((state) => {
        if (!state.watchList.includes(stockCode)) {
          return { watchList: [...state.watchList, stockCode] };
        }
        return state;
      }, false, 'addToWatchList'),

      removeFromWatchList: (stockCode) => set((state) => ({watchList: state.watchList.filter(code => code !== stockCode)}), false, 'removeFromWatchList'),

      clearWatchList: () => set({ watchList: [] }, false, 'clearWatchList'),
    }),
    {
      name: 'stock-store',
      partialize: (state) => ({
        searchHistory: state.searchHistory,
        watchList: state.watchList,
      }),
    }
  ),
  { name: 'StockStore' }
));

// 创建分析任务状态store
export const useAnalysisStore = create<AnalysisState>()(devtools(
  (set) => ({
    tasks: [],
    currentTask: null,
    results: [],
    statistics: null,
    analysisTypes: [],

    setTasks: (tasks) => set({ tasks }, false, 'setTasks'),

    addTask: (task) => set((state) => ({tasks: [task, ...state.tasks]}), false, 'addTask'),

    updateTask: (taskId, updates) => set((state) => ({
      tasks: state.tasks.map(task =>
        task.id === taskId ? {
          ...task,
          ...updates
        } : task
      ),
      currentTask: state.currentTask?.id === taskId
        ? {
          ...state.currentTask,
          ...updates
        }
        : state.currentTask
    }), false, 'updateTask'),

    removeTask: (taskId) => set((state) => ({
      tasks: state.tasks.filter(task => task.id !== taskId),
      currentTask: state.currentTask?.id === taskId ? null : state.currentTask
    }), false, 'removeTask'),

    setCurrentTask: (task) => set({ currentTask: task }, false, 'setCurrentTask'),

    setResults: (results) => set({ results }, false, 'setResults'),

    addResult: (result) => set((state) => ({results: [result, ...state.results]}), false, 'addResult'),

    setStatistics: (statistics) => set({ statistics }, false, 'setStatistics'),

    setAnalysisTypes: (types) => set({ analysisTypes: types }, false, 'setAnalysisTypes'),

    clearTasks: () => set({
      tasks: [],
      currentTask: null
    }, false, 'clearTasks'),

    clearResults: () => set({ results: [] }, false, 'clearResults'),
  }),
  { name: 'AnalysisStore' }
));

// 创建智能体状态store
export const useAgentStore = create<AgentState>()(devtools(
  (set) => ({
    agents: [],
    availableAgents: [],
    agentTypes: [],
    agentPerformance: null,

    setAgents: (agents) => set({ agents }, false, 'setAgents'),

    setAvailableAgents: (agents) => set({ availableAgents: agents }, false, 'setAvailableAgents'),

    updateAgent: (agentName, updates) => set((state) => ({
      agents: state.agents.map(agent =>
        agent.name === agentName ? {
          ...agent,
          ...updates
        } : agent
      ),
      availableAgents: state.availableAgents.map(agent =>
        agent.name === agentName ? {
          ...agent,
          ...updates
        } : agent
      )
    }), false, 'updateAgent'),

    setAgentTypes: (types) => set({ agentTypes: types }, false, 'setAgentTypes'),

    setAgentPerformance: (performance) => set({ agentPerformance: performance }, false, 'setAgentPerformance'),
  }),
  { name: 'AgentStore' }
));

// 导出所有store的类型
export type {
  AppState,
  StockState,
  AnalysisState,
  AgentState
};

// 导出便捷的选择器
// 组合所有store的通用hook
export const useStore = () => {
  const appStore = useAppStore();
  const stockStore = useStockStore();
  const analysisStore = useAnalysisStore();
  const agentStore = useAgentStore();

  return {
    // App store
    ...appStore,

    // Stock store
    ...stockStore,

    // Analysis store
    ...analysisStore,

    // Agent store
    ...agentStore,
  };
};

export const selectors = {
  // 应用状态选择器
  app: {
    isAuthenticated: (state: AppState) => state.user.isAuthenticated,
    userInfo: (state: AppState) => state.user,
    theme: (state: AppState) => state.theme,
    language: (state: AppState) => state.language,
    sidebarCollapsed: (state: AppState) => state.sidebarCollapsed,
    globalLoading: (state: AppState) => state.globalLoading,
    error: (state: AppState) => state.error,
    wsConnected: (state: AppState) => state.wsConnected,
  },

  // 股票状态选择器
  stock: {
    selectedStock: (state: StockState) => state.selectedStock,
    stockList: (state: StockState) => state.stockList,
    realTimePrices: (state: StockState) => state.realTimePrices,
    marketOverview: (state: StockState) => state.marketOverview,
    industries: (state: StockState) => state.industries,
    markets: (state: StockState) => state.markets,
    searchHistory: (state: StockState) => state.searchHistory,
    watchList: (state: StockState) => state.watchList,
  },

  // 分析状态选择器
  analysis: {
    tasks: (state: AnalysisState) => state.tasks,
    currentTask: (state: AnalysisState) => state.currentTask,
    results: (state: AnalysisState) => state.results,
    statistics: (state: AnalysisState) => state.statistics,
    analysisTypes: (state: AnalysisState) => state.analysisTypes,
    pendingTasks: (state: AnalysisState) => state.tasks.filter(task => task.status === 'PENDING'),
    runningTasks: (state: AnalysisState) => state.tasks.filter(task => task.status === 'RUNNING'),
    completedTasks: (state: AnalysisState) => state.tasks.filter(task => task.status === 'COMPLETED'),
    failedTasks: (state: AnalysisState) => state.tasks.filter(task => task.status === 'FAILED'),
  },

  // 智能体状态选择器
  agent: {
    agents: (state: AgentState) => state.agents,
    availableAgents: (state: AgentState) => state.availableAgents,
    agentTypes: (state: AgentState) => state.agentTypes,
    agentPerformance: (state: AgentState) => state.agentPerformance,
    activeAgents: (state: AgentState) => state.agents.filter(agent => agent.status === 'ACTIVE'),
    busyAgents: (state: AgentState) => state.agents.filter(agent => agent.status === 'BUSY'),
  },
};