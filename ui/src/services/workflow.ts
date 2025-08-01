import axios from 'axios';
import type {
  WorkflowDefinition,
  WorkflowExecution,
  WorkflowExecutionStep,
  WorkflowExecutionLog,
  WorkflowStatistics,
  WorkflowTemplate,
  WorkflowSearchParams,
  ExecutionSearchParams,
  ApiResponse,
  PageResponse,
  WorkflowValidationResult,
  WorkflowExportData,
  WorkflowPermission,
  WorkflowShare
} from '@/types/workflow';

// 创建 axios 实例
const api = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api/v1',
  timeout: 30000,
  headers: {'Content-Type': 'application/json',},
});

// 请求拦截器 - 添加认证token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理错误
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // Token过期，跳转到登录页
      localStorage.removeItem('access_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// 工作流服务类
export class WorkflowService {
  // 获取工作流列表
  async getWorkflows(params?: WorkflowSearchParams): Promise<ApiResponse<PageResponse<WorkflowDefinition>>> {
    const response = await api.get('/workflows', { params });
    return response.data;
  }

  // 获取单个工作流
  async getWorkflow(id: string): Promise<ApiResponse<WorkflowDefinition>> {
    const response = await api.get(`/workflows/${id}`);
    return response.data;
  }

  // 创建工作流
  async createWorkflow(workflow: Partial<WorkflowDefinition>): Promise<ApiResponse<WorkflowDefinition>> {
    const response = await api.post('/workflows', workflow);
    return response.data;
  }

  // 更新工作流
  async updateWorkflow(id: string, workflow: Partial<WorkflowDefinition>): Promise<ApiResponse<WorkflowDefinition>> {
    const response = await api.put(`/workflows/${id}`, workflow);
    return response.data;
  }

  // 删除工作流
  async deleteWorkflow(id: string): Promise<ApiResponse<void>> {
    const response = await api.delete(`/workflows/${id}`);
    return response.data;
  }

  // 克隆工作流
  async cloneWorkflow(id: string, name?: string): Promise<ApiResponse<WorkflowDefinition>> {
    const response = await api.post(`/workflows/${id}/clone`, { name });
    return response.data;
  }

  // 验证工作流
  async validateWorkflow(workflow: Partial<WorkflowDefinition>): Promise<ApiResponse<WorkflowValidationResult>> {
    const response = await api.post('/workflows/validate', workflow);
    return response.data;
  }

  // 导出工作流
  async exportWorkflow(id: string): Promise<ApiResponse<WorkflowExportData>> {
    const response = await api.get(`/workflows/${id}/export`);
    return response.data;
  }

  // 导入工作流
  async importWorkflow(data: WorkflowExportData): Promise<ApiResponse<WorkflowDefinition>> {
    const response = await api.post('/workflows/import', data);
    return response.data;
  }

  // 执行工作流
  async executeWorkflow(id: string, input: Record<string, any>): Promise<ApiResponse<{ executionId: string }>> {
    const response = await api.post(`/workflows/${id}/execute`, { input });
    return response.data;
  }

  // 获取工作流执行历史
  async getWorkflowExecutions(workflowId: string, params?: ExecutionSearchParams): Promise<ApiResponse<PageResponse<WorkflowExecution>>> {
    const response = await api.get(`/workflows/${workflowId}/executions`, { params });
    return response.data;
  }

  // 获取用户执行历史
  async getUserExecutions(params?: ExecutionSearchParams): Promise<ApiResponse<PageResponse<WorkflowExecution>>> {
    const response = await api.get('/executions', { params });
    return response.data;
  }

  // 获取执行详情
  async getExecution(executionId: string): Promise<ApiResponse<WorkflowExecution>> {
    const response = await api.get(`/executions/${executionId}`);
    return response.data;
  }

  // 获取执行步骤
  async getExecutionSteps(executionId: string): Promise<ApiResponse<WorkflowExecutionStep[]>> {
    const response = await api.get(`/executions/${executionId}/steps`);
    return response.data;
  }

  // 获取执行日志
  async getExecutionLogs(executionId: string): Promise<ApiResponse<WorkflowExecutionLog[]>> {
    const response = await api.get(`/executions/${executionId}/logs`);
    return response.data;
  }

  // 控制执行（暂停/恢复/停止）
  async controlExecution(executionId: string, action: 'pause' | 'resume' | 'stop'): Promise<ApiResponse<void>> {
    const response = await api.post(`/executions/${executionId}/${action}`);
    return response.data;
  }

  // 下载执行报告
  async downloadExecutionReport(executionId: string): Promise<any> {
    const response = await api.get(`/executions/${executionId}/report`, {responseType: 'blob'});
    return response;
  }

  // 获取工作流统计
  async getWorkflowStatistics(): Promise<ApiResponse<WorkflowStatistics>> {
    const response = await api.get('/workflows/statistics');
    return response.data;
  }

  // 获取用户统计
  async getUserStatistics(): Promise<ApiResponse<WorkflowStatistics>> {
    const response = await api.get('/workflows/statistics/user');
    return response.data;
  }

  // 获取工作流模板
  async getWorkflowTemplates(): Promise<ApiResponse<WorkflowTemplate[]>> {
    const response = await api.get('/workflows/templates');
    return response.data;
  }

  // 从模板创建工作流
  async createFromTemplate(templateId: string, name: string): Promise<ApiResponse<WorkflowDefinition>> {
    const response = await api.post(`/workflows/templates/${templateId}/create`, { name });
    return response.data;
  }

  // 获取节点模板
  async getNodeTemplates(): Promise<ApiResponse<any[]>> {
    const response = await api.get('/workflows/node-templates');
    return response.data;
  }

  // 工作流权限管理
  async getWorkflowPermissions(workflowId: string): Promise<ApiResponse<WorkflowPermission[]>> {
    const response = await api.get(`/workflows/${workflowId}/permissions`);
    return response.data;
  }

  async grantWorkflowPermission(workflowId: string, userId: string, permission: string): Promise<ApiResponse<WorkflowPermission>> {
    const response = await api.post(`/workflows/${workflowId}/permissions`, {
      userId,
      permission
    });
    return response.data;
  }

  async revokeWorkflowPermission(workflowId: string, permissionId: string): Promise<ApiResponse<void>> {
    const response = await api.delete(`/workflows/${workflowId}/permissions/${permissionId}`);
    return response.data;
  }

  // 工作流分享
  async shareWorkflow(workflowId: string, permissions: string[], expiresAt?: string): Promise<ApiResponse<WorkflowShare>> {
    const response = await api.post(`/workflows/${workflowId}/share`, {
      permissions,
      expiresAt
    });
    return response.data;
  }

  async getWorkflowShares(workflowId: string): Promise<ApiResponse<WorkflowShare[]>> {
    const response = await api.get(`/workflows/${workflowId}/shares`);
    return response.data;
  }

  async revokeWorkflowShare(workflowId: string, shareId: string): Promise<ApiResponse<void>> {
    const response = await api.delete(`/workflows/${workflowId}/shares/${shareId}`);
    return response.data;
  }

  async accessSharedWorkflow(shareToken: string): Promise<ApiResponse<WorkflowDefinition>> {
    const response = await api.get(`/workflows/shared/${shareToken}`);
    return response.data;
  }

  // 工作流版本管理
  async getWorkflowVersions(workflowId: string): Promise<ApiResponse<WorkflowDefinition[]>> {
    const response = await api.get(`/workflows/${workflowId}/versions`);
    return response.data;
  }

  async createWorkflowVersion(workflowId: string, version: string): Promise<ApiResponse<WorkflowDefinition>> {
    const response = await api.post(`/workflows/${workflowId}/versions`, { version });
    return response.data;
  }

  async restoreWorkflowVersion(workflowId: string, version: string): Promise<ApiResponse<WorkflowDefinition>> {
    const response = await api.post(`/workflows/${workflowId}/versions/${version}/restore`);
    return response.data;
  }

  // 工作流标签管理
  async getWorkflowTags(): Promise<ApiResponse<string[]>> {
    const response = await api.get('/workflows/tags');
    return response.data;
  }

  async addWorkflowTag(workflowId: string, tag: string): Promise<ApiResponse<void>> {
    const response = await api.post(`/workflows/${workflowId}/tags`, { tag });
    return response.data;
  }

  async removeWorkflowTag(workflowId: string, tag: string): Promise<ApiResponse<void>> {
    const response = await api.delete(`/workflows/${workflowId}/tags/${tag}`);
    return response.data;
  }

  // 工作流收藏
  async favoriteWorkflow(workflowId: string): Promise<ApiResponse<void>> {
    const response = await api.post(`/workflows/${workflowId}/favorite`);
    return response.data;
  }

  async unfavoriteWorkflow(workflowId: string): Promise<ApiResponse<void>> {
    const response = await api.delete(`/workflows/${workflowId}/favorite`);
    return response.data;
  }

  async getFavoriteWorkflows(): Promise<ApiResponse<WorkflowDefinition[]>> {
    const response = await api.get('/workflows/favorites');
    return response.data;
  }

  // 工作流搜索
  async searchWorkflows(query: string, filters?: any): Promise<ApiResponse<PageResponse<WorkflowDefinition>>> {
    const response = await api.post('/workflows/search', {
      query,
      filters
    });
    return response.data;
  }

  // 工作流推荐
  async getRecommendedWorkflows(): Promise<ApiResponse<WorkflowDefinition[]>> {
    const response = await api.get('/workflows/recommendations');
    return response.data;
  }

  // 工作流评分
  async rateWorkflow(workflowId: string, rating: number, comment?: string): Promise<ApiResponse<void>> {
    const response = await api.post(`/workflows/${workflowId}/rating`, {
      rating,
      comment
    });
    return response.data;
  }

  async getWorkflowRatings(workflowId: string): Promise<ApiResponse<any[]>> {
    const response = await api.get(`/workflows/${workflowId}/ratings`);
    return response.data;
  }

  // 工作流使用情况分析
  async getWorkflowUsageAnalytics(workflowId: string, period: string): Promise<ApiResponse<any>> {
    const response = await api.get(`/workflows/${workflowId}/analytics`, {params: { period }});
    return response.data;
  }

  // 系统健康检查
  async healthCheck(): Promise<ApiResponse<any>> {
    const response = await api.get('/health');
    return response.data;
  }
}

// 导出单例实例
export const workflowService = new WorkflowService();

// 导出默认实例
export default workflowService;