/**
 * 智能体协作API服务
 * @author Stock-Agent Team
 * @version 1.0
 */

import {
  AgentInfo,
  CollaborationRequest,
  CollaborationSession,

  CollaborationHistory,
  CollaborationMetrics,
  QuickCollaborationRequest,
  ApiResponse,

  CollaborationStats,
  CollaborationModeConfig,
  AgentTypeConfig,
  AgentType,
  CollaborationMode
} from '../types/collaboration';
import { request } from '../utils/request';

// API基础路径
const API_BASE = '/api/collaboration';

/**
 * 智能体协作API服务类
 */
export class CollaborationService {

  // ==================== 智能体管理 ====================

  /**
   * 注册智能体
   */
  static async registerAgent(agentInfo: Omit<AgentInfo, 'lastActiveTime' | 'totalCollaborations' | 'successRate' | 'averageConfidence'>): Promise<ApiResponse> {
    return request.post(`${API_BASE}/agents`, agentInfo);
  }

  /**
   * 注销智能体
   */
  static async unregisterAgent(agentId: string): Promise<ApiResponse> {
    return request.delete(`${API_BASE}/agents/${agentId}`);
  }

  /**
   * 获取已注册智能体列表
   */
  static async getRegisteredAgents(params?: {
    type?: string;
    specialization?: string;
    status?: string;
  }): Promise<ApiResponse<AgentInfo[]>> {
    return request.get(`${API_BASE}/agents`, { params });
  }

  /**
   * 批量注册智能体
   */
  static async batchRegisterAgents(agents: Omit<AgentInfo, 'lastActiveTime' | 'totalCollaborations' | 'successRate' | 'averageConfidence'>[]): Promise<ApiResponse> {
    return request.post(`${API_BASE}/agents/batch`, agents);
  }

  /**
   * 获取智能体详情
   */
  static async getAgentDetails(agentId: string): Promise<ApiResponse<AgentInfo>> {
    return request.get(`${API_BASE}/agents/${agentId}`);
  }

  /**
   * 更新智能体信息
   */
  static async updateAgent(agentId: string, updates: Partial<AgentInfo>): Promise<ApiResponse> {
    return request.put(`${API_BASE}/agents/${agentId}`, updates);
  }

  // ==================== 协作会话管理 ====================

  /**
   * 创建协作会话
   */
  static async createCollaborationSession(request: CollaborationRequest): Promise<ApiResponse<{ sessionId: string }>> {
    return request.post(`${API_BASE}/sessions`, request);
  }

  /**
   * 执行协作
   */
  static async executeCollaboration(sessionId: string): Promise<ApiResponse> {
    return request.post(`${API_BASE}/sessions/${sessionId}/execute`);
  }

  /**
   * 获取协作会话状态
   */
  static async getSessionStatus(sessionId: string): Promise<ApiResponse<CollaborationSession>> {
    return request.get(`${API_BASE}/sessions/${sessionId}`);
  }

  /**
   * 取消协作会话
   */
  static async cancelSession(sessionId: string): Promise<ApiResponse> {
    return request.post(`${API_BASE}/sessions/${sessionId}/cancel`);
  }

  /**
   * 获取活跃会话列表
   */
  static async getActiveSessions(): Promise<ApiResponse<CollaborationSession[]>> {
    return request.get(`${API_BASE}/sessions/active`);
  }

  /**
   * 快速协作（一键执行）
   */
  static async quickCollaboration(request: QuickCollaborationRequest): Promise<ApiResponse<{ sessionId: string }>> {
    return request.post(`${API_BASE}/quick`, request);
  }

  // ==================== 协作历史和指标 ====================

  /**
   * 获取协作历史
   */
  static async getCollaborationHistory(params?: {
    limit?: number;
    page?: number;
    mode?: CollaborationMode;
    dateFrom?: string;
    dateTo?: string;
  }): Promise<ApiResponse<CollaborationHistory[]>> {
    return request.get(`${API_BASE}/history`, { params });
  }

  /**
   * 获取协作指标
   */
  static async getCollaborationMetrics(): Promise<ApiResponse<CollaborationMetrics>> {
    return request.get(`${API_BASE}/metrics`);
  }

  /**
   * 获取协作统计
   */
  static async getCollaborationStats(): Promise<ApiResponse<CollaborationStats>> {
    return request.get(`${API_BASE}/stats`);
  }

  /**
   * 获取协作趋势数据
   */
  static async getCollaborationTrends(params?: {
    period?: 'day' | 'week' | 'month';
    metric?: 'sessions' | 'success_rate' | 'confidence' | 'duration';
  }): Promise<ApiResponse<{ labels: string[]; data: number[] }>> {
    return request.get(`${API_BASE}/trends`, { params });
  }

  // ==================== 配置和元数据 ====================

  /**
   * 获取支持的协作模式
   */
  static async getCollaborationModes(): Promise<ApiResponse<Record<string, CollaborationModeConfig>>> {
    return request.get(`${API_BASE}/modes`);
  }

  /**
   * 获取智能体类型
   */
  static async getAgentTypes(): Promise<ApiResponse<Record<string, AgentTypeConfig>>> {
    return request.get(`${API_BASE}/agent-types`);
  }

  /**
   * 获取API帮助文档
   */
  static async getApiHelp(): Promise<ApiResponse> {
    return request.get(`${API_BASE}/help`);
  }

  // ==================== 系统管理 ====================

  /**
   * 清除缓存
   */
  static async clearCache(): Promise<ApiResponse> {
    return request.post(`${API_BASE}/cache/clear`);
  }

  /**
   * 获取系统状态
   */
  static async getSystemStatus(): Promise<ApiResponse> {
    return request.get(`${API_BASE}/system/status`);
  }

  /**
   * 获取性能指标
   */
  static async getPerformanceMetrics(): Promise<ApiResponse> {
    return request.get(`${API_BASE}/system/performance`);
  }

  // ==================== 实时更新 ====================

  /**
   * 创建WebSocket连接用于实时更新
   */
  static createWebSocketConnection(sessionId?: string): WebSocket {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    const path = sessionId
      ? `/ws/collaboration/sessions/${sessionId}`
      : '/ws/collaboration';

    return new WebSocket(`${protocol}//${host}${path}`);
  }

  /**
   * 订阅协作事件
   */
  static subscribeToCollaborationEvents(
    callback: (event: any) => void,
    sessionId?: string
  ): WebSocket {
    const ws = this.createWebSocketConnection(sessionId);

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        callback(data);
      } catch (error) {
        console.error('Failed to parse WebSocket message:', error);
      }
    };

    ws.onerror = (error) => {
      console.error('WebSocket error:', error);
    };

    return ws;
  }

  // ==================== 协作建议和优化 ====================

  /**
   * 获取协作建议
   */
  static async getCollaborationSuggestions(params?: {
    topic?: string;
    agentTypes?: AgentType[];
    context?: Record<string, any>;
  }): Promise<ApiResponse> {
    return request.post(`${API_BASE}/suggestions`, params);
  }

  /**
   * 获取最佳实践
   */
  static async getBestPractices(): Promise<ApiResponse> {
    return request.get(`${API_BASE}/best-practices`);
  }

  /**
   * 分析协作效果
   */
  static async analyzeCollaborationEffectiveness(sessionId: string): Promise<ApiResponse> {
    return request.get(`${API_BASE}/sessions/${sessionId}/analysis`);
  }

  // ==================== 模板管理 ====================

  /**
   * 获取协作模板
   */
  static async getCollaborationTemplates(): Promise<ApiResponse> {
    return request.get(`${API_BASE}/templates`);
  }

  /**
   * 创建协作模板
   */
  static async createCollaborationTemplate(template: any): Promise<ApiResponse> {
    return request.post(`${API_BASE}/templates`, template);
  }

  /**
   * 使用模板创建协作
   */
  static async createFromTemplate(templateId: string, params: any): Promise<ApiResponse> {
    return request.post(`${API_BASE}/templates/${templateId}/create`, params);
  }

  // ==================== 导出和报告 ====================

  /**
   * 导出协作报告
   */
  static async exportCollaborationReport(params: {
    sessionIds?: string[];
    dateFrom?: string;
    dateTo?: string;
    format?: 'pdf' | 'excel' | 'csv';
  }): Promise<Blob> {
    const response = await request.post(`${API_BASE}/export/report`, params, {responseType: 'blob'});
    return response.data;
  }

  /**
   * 导出协作数据
   */
  static async exportCollaborationData(params: {
    type: 'sessions' | 'agents' | 'metrics';
    format?: 'json' | 'csv' | 'excel';
    filters?: Record<string, any>;
  }): Promise<Blob> {
    const response = await request.post(`${API_BASE}/export/data`, params, {responseType: 'blob'});
    return response.data;
  }

  // ==================== 调试和测试 ====================

  /**
   * 测试智能体连接
   */
  static async testAgentConnection(agentId: string): Promise<ApiResponse> {
    return request.post(`${API_BASE}/agents/${agentId}/test`);
  }

  /**
   * 模拟协作会话
   */
  static async simulateCollaboration(params: {
    mode: CollaborationMode;
    participantCount: number;
    topic: string;
    duration?: number;
  }): Promise<ApiResponse> {
    return request.post(`${API_BASE}/simulate`, params);
  }

  /**
   * 获取调试信息
   */
  static async getDebugInfo(sessionId: string): Promise<ApiResponse> {
    return request.get(`${API_BASE}/sessions/${sessionId}/debug`);
  }
}

// 导出默认实例
export default CollaborationService;

// 导出便捷方法
export const {
  registerAgent,
  unregisterAgent,
  getRegisteredAgents,
  createCollaborationSession,
  executeCollaboration,
  getSessionStatus,
  getCollaborationHistory,
  getCollaborationMetrics,
  getCollaborationModes,
  getAgentTypes,
  quickCollaboration,
  clearCache,
  subscribeToCollaborationEvents
} = CollaborationService;