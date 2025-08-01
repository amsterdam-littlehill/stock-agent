// Stock Agent Genie Backend 性能测试脚本
// 使用 K6 进行负载测试

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
import { htmlReport } from 'https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';

// 自定义指标
const errorRate = new Rate('error_rate');
const responseTime = new Trend('response_time');
const requestCount = new Counter('request_count');

// 测试配置
export const options = {
  stages: [
    { duration: '2m', target: 10 },   // 预热阶段：2分钟内逐渐增加到10个用户
    { duration: '5m', target: 10 },   // 稳定阶段：保持10个用户5分钟
    { duration: '2m', target: 20 },   // 增压阶段：2分钟内增加到20个用户
    { duration: '5m', target: 20 },   // 高负载阶段：保持20个用户5分钟
    { duration: '2m', target: 50 },   // 峰值阶段：2分钟内增加到50个用户
    { duration: '3m', target: 50 },   // 峰值保持：保持50个用户3分钟
    { duration: '2m', target: 0 },    // 降压阶段：2分钟内降到0个用户
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95%的请求响应时间小于2秒
    http_req_failed: ['rate<0.05'],    // 错误率小于5%
    error_rate: ['rate<0.05'],         // 自定义错误率小于5%
    response_time: ['p(95)<2000'],     // 95%的响应时间小于2秒
  },
};

// 测试环境配置
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api';
const WS_URL = __ENV.WS_URL || 'ws://localhost:8080/ws';

// 测试用户凭据
const TEST_USERS = [
  { username: 'admin', password: 'admin123' },
  { username: 'testuser', password: 'test123' },
  { username: 'analyst', password: 'test123' },
];

// 测试数据
const TEST_STOCKS = ['AAPL', 'GOOGL', 'MSFT', 'AMZN', 'TSLA', 'META', 'NVDA', 'NFLX'];
const WORKFLOW_IDS = ['test-workflow-001', 'test-workflow-002'];

// 全局变量
let authTokens = {};
let workflowIds = [];

// 设置阶段 - 获取认证令牌
export function setup() {
  console.log('开始性能测试设置...');
  
  const tokens = {};
  
  // 为每个测试用户获取认证令牌
  TEST_USERS.forEach((user, index) => {
    const loginResponse = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
      username: user.username,
      password: user.password
    }), {
      headers: { 'Content-Type': 'application/json' },
    });
    
    if (loginResponse.status === 200) {
      const loginData = JSON.parse(loginResponse.body);
      if (loginData.success) {
        tokens[user.username] = loginData.data.accessToken;
        console.log(`用户 ${user.username} 登录成功`);
      }
    } else {
      console.error(`用户 ${user.username} 登录失败: ${loginResponse.status}`);
    }
  });
  
  // 获取可用的工作流ID
  const workflowResponse = http.get(`${BASE_URL}/workflows?page=0&size=10`, {
    headers: {
      'Authorization': `Bearer ${tokens.admin}`,
      'Content-Type': 'application/json'
    }
  });
  
  if (workflowResponse.status === 200) {
    const workflowData = JSON.parse(workflowResponse.body);
    if (workflowData.success && workflowData.data.content.length > 0) {
      workflowIds = workflowData.data.content.map(w => w.id);
      console.log(`找到 ${workflowIds.length} 个可用工作流`);
    }
  }
  
  console.log('性能测试设置完成');
  return { tokens, workflowIds };
}

// 主测试函数
export default function(data) {
  authTokens = data.tokens;
  workflowIds = data.workflowIds;
  
  // 随机选择用户和测试场景
  const user = TEST_USERS[Math.floor(Math.random() * TEST_USERS.length)];
  const token = authTokens[user.username];
  
  if (!token) {
    console.error(`用户 ${user.username} 没有有效的认证令牌`);
    return;
  }
  
  const headers = {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  };
  
  // 随机选择测试场景
  const scenarios = [
    testHealthCheck,
    testGetWorkflows,
    testGetAgents,
    testExecuteWorkflow,
    testGetMCPTools,
    testGetSystemStats,
    testSearchWorkflows,
    testGetExecutionHistory
  ];
  
  const scenario = scenarios[Math.floor(Math.random() * scenarios.length)];
  scenario(headers);
  
  // 随机等待时间，模拟真实用户行为
  sleep(Math.random() * 3 + 1); // 1-4秒随机等待
}

// 健康检查测试
function testHealthCheck(headers) {
  const startTime = Date.now();
  
  const response = http.get(`${BASE_URL}/actuator/health`);
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    '健康检查状态码为200': (r) => r.status === 200,
    '健康检查响应时间<500ms': (r) => r.timings.duration < 500,
  });
  
  errorRate.add(!success);
}

// 获取工作流列表测试
function testGetWorkflows(headers) {
  const startTime = Date.now();
  
  const response = http.get(`${BASE_URL}/workflows?page=0&size=20`, { headers });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    '获取工作流状态码为200': (r) => r.status === 200,
    '获取工作流响应时间<1000ms': (r) => r.timings.duration < 1000,
    '工作流响应包含数据': (r) => {
      try {
        const data = JSON.parse(r.body);
        return data.success && Array.isArray(data.data.content);
      } catch (e) {
        return false;
      }
    },
  });
  
  errorRate.add(!success);
}

// 获取智能体列表测试
function testGetAgents(headers) {
  const startTime = Date.now();
  
  const response = http.get(`${BASE_URL}/agents`, { headers });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    '获取智能体状态码为200': (r) => r.status === 200,
    '获取智能体响应时间<800ms': (r) => r.timings.duration < 800,
  });
  
  errorRate.add(!success);
}

// 执行工作流测试
function testExecuteWorkflow(headers) {
  if (workflowIds.length === 0) {
    console.log('没有可用的工作流进行测试');
    return;
  }
  
  const workflowId = workflowIds[Math.floor(Math.random() * workflowIds.length)];
  const symbol = TEST_STOCKS[Math.floor(Math.random() * TEST_STOCKS.length)];
  
  const startTime = Date.now();
  
  const response = http.post(`${BASE_URL}/workflows/${workflowId}/execute`, JSON.stringify({
    input: {
      symbol: symbol,
      analysisType: 'BASIC'
    },
    priority: 'NORMAL',
    timeout: 60000
  }), { headers });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    '执行工作流状态码为200': (r) => r.status === 200,
    '执行工作流响应时间<3000ms': (r) => r.timings.duration < 3000,
    '执行工作流返回执行ID': (r) => {
      try {
        const data = JSON.parse(r.body);
        return data.success && data.data.executionId;
      } catch (e) {
        return false;
      }
    },
  });
  
  errorRate.add(!success);
}

// 获取MCP工具列表测试
function testGetMCPTools(headers) {
  const startTime = Date.now();
  
  const response = http.get(`${BASE_URL}/mcp/tools`, { headers });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    '获取MCP工具状态码为200': (r) => r.status === 200,
    '获取MCP工具响应时间<600ms': (r) => r.timings.duration < 600,
  });
  
  errorRate.add(!success);
}

// 获取系统统计测试
function testGetSystemStats(headers) {
  const startTime = Date.now();
  
  const response = http.get(`${BASE_URL}/system/stats`, { headers });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    '获取系统统计状态码为200': (r) => r.status === 200,
    '获取系统统计响应时间<1000ms': (r) => r.timings.duration < 1000,
  });
  
  errorRate.add(!success);
}

// 搜索工作流测试
function testSearchWorkflows(headers) {
  const keywords = ['股票', '分析', '监控', '测试'];
  const keyword = keywords[Math.floor(Math.random() * keywords.length)];
  
  const startTime = Date.now();
  
  const response = http.get(`${BASE_URL}/workflows/search?keyword=${keyword}&page=0&size=10`, { headers });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    '搜索工作流状态码为200': (r) => r.status === 200,
    '搜索工作流响应时间<1200ms': (r) => r.timings.duration < 1200,
  });
  
  errorRate.add(!success);
}

// 获取执行历史测试
function testGetExecutionHistory(headers) {
  const startTime = Date.now();
  
  const response = http.get(`${BASE_URL}/workflows/executions?page=0&size=20`, { headers });
  
  const duration = Date.now() - startTime;
  responseTime.add(duration);
  requestCount.add(1);
  
  const success = check(response, {
    '获取执行历史状态码为200': (r) => r.status === 200,
    '获取执行历史响应时间<1000ms': (r) => r.timings.duration < 1000,
  });
  
  errorRate.add(!success);
}

// 生成测试报告
export function handleSummary(data) {
  return {
    'performance-test-report.html': htmlReport(data),
    stdout: textSummary(data, { indent: ' ', enableColors: true }),
    'performance-test-summary.json': JSON.stringify(data),
  };
}

// 清理阶段
export function teardown(data) {
  console.log('性能测试清理完成');
}