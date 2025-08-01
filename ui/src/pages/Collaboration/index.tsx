/**
 * 智能体协作主页面
 * @author Stock-Agent Team
 * @version 1.0
 */

import React, { useState, useEffect, useCallback } from 'react';
import {
  Card,
  Row,
  Col,
  Button,
  Tabs,
  Badge,
  Statistic,
  Space,
  message,
  Modal,

  Alert,
  Typography,
  Tooltip,
  Tooltip
} from 'antd';
import {

  ReloadOutlined,
  SettingOutlined,
  ExportOutlined,
  PlayCircleOutlined,

  TeamOutlined,
  RobotOutlined,
  ThunderboltOutlined,
  BarChartOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined
} from '@ant-design/icons';

import {
  AgentInfo,
  CollaborationSession,
  CollaborationMetrics,
  CollaborationHistory as CollaborationHistoryType,
  CollaborationStatus,
  // AgentType,
  CollaborationStatus
} from '../../types/collaboration';
import { CollaborationService } from '../../services/collaboration';
import AgentManagement from '../../components/Collaboration/AgentManagement';
import SessionManagement from '../../components/Collaboration/SessionManagement';
import CollaborationDashboard from '../../components/Collaboration/CollaborationDashboard';
import QuickCollaboration from '../../components/Collaboration/QuickCollaboration';
import CollaborationHistory from '../../components/Collaboration/CollaborationHistory';
import CollaborationSettings from '../../components/Collaboration/CollaborationSettings';
import RealtimeMonitor from '../../components/Collaboration/RealtimeMonitor';
import './index.css';

const { Title, Text } = Typography;
const { TabPane } = Tabs;

/**
 * 智能体协作主页面组件
 */
const CollaborationPage: React.FC = () => {
  // 状态管理
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('dashboard');
  const [agents, setAgents] = useState<AgentInfo[]>([]);
  const [activeSessions, setActiveSessions] = useState<CollaborationSession[]>([]);
  const [metrics, setMetrics] = useState<CollaborationMetrics | null>(null);
  const [history, setHistory] = useState<CollaborationHistoryType[]>([]);
  const [realTimeData, setRealTimeData] = useState<any>(null);
  const [wsConnection, setWsConnection] = useState<WebSocket | null>(null);
  const [systemStatus, setSystemStatus] = useState<'online' | 'offline' | 'maintenance'>('online');

  // 模态框状态
  const [quickCollabVisible, setQuickCollabVisible] = useState(false);
  const [settingsVisible, setSettingsVisible] = useState(false);
  const [exportModalVisible, setExportModalVisible] = useState(false);

  /**
   * 初始化数据加载
   */
  const initializeData = useCallback(async () => {
    setLoading(true);
    try {
      // 并行加载所有数据
      const [agentsRes, sessionsRes, metricsRes, historyRes] = await Promise.all([
        CollaborationService.getRegisteredAgents(),
        CollaborationService.getActiveSessions(),
        CollaborationService.getCollaborationMetrics(),
        CollaborationService.getCollaborationHistory({ limit: 20 })
      ]);

      if (agentsRes.success) {
        setAgents(agentsRes.data || []);
      }

      if (sessionsRes.success) {
        setActiveSessions(sessionsRes.data || []);
      }

      if (metricsRes.success) {
        setMetrics(metricsRes.data);
      }

      if (historyRes.success) {
        setHistory(historyRes.data || []);
      }

    } catch (error) {
      console.error('Failed to initialize collaboration data:', error);
      message.error('加载协作数据失败');
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * 建立WebSocket连接
   */
  const setupWebSocket = useCallback(() => {
    try {
      const ws = CollaborationService.subscribeToCollaborationEvents((event) => {
        setRealTimeData(event);

        // 根据事件类型更新相应数据
        switch (event.type) {
          case 'SESSION_CREATED':
          case 'SESSION_COMPLETED':
          case 'SESSION_FAILED':
            // 重新加载活跃会话
            loadActiveSessions();
            break;
          case 'AGENT_REGISTERED':
          case 'AGENT_UNREGISTERED':
            // 重新加载智能体列表
            loadAgents();
            break;
          case 'METRICS_UPDATED':
            // 重新加载指标
            loadMetrics();
            break;
        }
      });

      ws.onopen = () => {
        setSystemStatus('online');
        console.log('WebSocket connected');
      };

      ws.onclose = () => {
        setSystemStatus('offline');
        console.log('WebSocket disconnected');
        // 尝试重连
        setTimeout(setupWebSocket, 5000);
      };

      ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        setSystemStatus('offline');
      };

      setWsConnection(ws);
    } catch (error) {
      console.error('Failed to setup WebSocket:', error);
      setSystemStatus('offline');
    }
  }, []);

  /**
   * 加载智能体列表
   */
  const loadAgents = useCallback(async () => {
    try {
      const response = await CollaborationService.getRegisteredAgents();
      if (response.success) {
        setAgents(response.data || []);
      }
    } catch (error) {
      console.error('Failed to load agents:', error);
    }
  }, []);

  /**
   * 加载活跃会话
   */
  const loadActiveSessions = useCallback(async () => {
    try {
      const response = await CollaborationService.getActiveSessions();
      if (response.success) {
        setActiveSessions(response.data || []);
      }
    } catch (error) {
      console.error('Failed to load active sessions:', error);
    }
  }, []);

  /**
   * 加载协作指标
   */
  const loadMetrics = useCallback(async () => {
    try {
      const response = await CollaborationService.getCollaborationMetrics();
      if (response.success) {
        setMetrics(response.data);
      }
    } catch (error) {
      console.error('Failed to load metrics:', error);
    }
  }, []);

  /**
   * 刷新所有数据
   */
  const handleRefresh = useCallback(() => {
    initializeData();
    message.success('数据已刷新');
  }, [initializeData]);

  /**
   * 快速协作
   */
  const handleQuickCollaboration = useCallback(async (params: any) => {
    try {
      const response = await CollaborationService.quickCollaboration(params);
      if (response.success) {
        message.success('快速协作已启动');
        setQuickCollabVisible(false);
        // 切换到会话管理标签页
        setActiveTab('sessions');
        // 刷新活跃会话
        loadActiveSessions();
      } else {
        message.error(response.message || '快速协作启动失败');
      }
    } catch (error) {
      console.error('Quick collaboration failed:', error);
      message.error('快速协作启动失败');
    }
  }, [loadActiveSessions]);

  /**
   * 导出数据
   */
  const handleExport = useCallback(async (type: string, format: string) => {
    try {
      setLoading(true);
      const blob = await CollaborationService.exportCollaborationData({
        type: type as any,
        format: format as any
      });

      // 创建下载链接
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `collaboration_${type}_${new Date().toISOString().split('T')[0]}.${format}`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);

      message.success('数据导出成功');
      setExportModalVisible(false);
    } catch (error) {
      console.error('Export failed:', error);
      message.error('数据导出失败');
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * 清除缓存
   */
  const handleClearCache = useCallback(async () => {
    try {
      const response = await CollaborationService.clearCache();
      if (response.success) {
        message.success('缓存已清除');
        // 重新加载数据
        initializeData();
      } else {
        message.error('缓存清除失败');
      }
    } catch (error) {
      console.error('Clear cache failed:', error);
      message.error('缓存清除失败');
    }
  }, [initializeData]);

  // 组件挂载时初始化
  useEffect(() => {
    initializeData();
    setupWebSocket();

    // 清理函数
    return () => {
      if (wsConnection) {
        wsConnection.close();
      }
    };
  }, []);

  // 计算统计数据
  const activeAgentsCount = agents.filter(agent => agent.status === 'ACTIVE').length;
  const runningSessionsCount = activeSessions.filter(session => session.status === CollaborationStatus.RUNNING).length;
  const todaySuccessRate = metrics ? (metrics.successRate * 100).toFixed(1) : '0';
  const avgConfidence = metrics ? (metrics.averageConfidence * 100).toFixed(1) : '0';

  return (
    <div className="collaboration-page">
      {/* 页面头部 */}
      <div className="collaboration-header">
        <Row justify="space-between" align="middle">
          <Col>
            <Space align="center">
              <RobotOutlined className="page-icon" />
              <Title level={2} style={{ margin: 0 }}>智能体协作中心</Title>
              <Badge
                status={systemStatus === 'online' ? 'success' : 'error'}
                text={systemStatus === 'online' ? '在线' : '离线'}
              />
            </Space>
          </Col>
          <Col>
            <Space>
              <Tooltip title="快速协作">
                <Button
                  type="primary"
                  icon={<ThunderboltOutlined />}
                  onClick={() => setQuickCollabVisible(true)}
                >
                  快速协作
                </Button>
              </Tooltip>
              <Tooltip title="刷新数据">
                <Button
                  icon={<ReloadOutlined />}
                  onClick={handleRefresh}
                  loading={loading}
                >
                  刷新
                </Button>
              </Tooltip>
              <Tooltip title="导出数据">
                <Button
                  icon={<ExportOutlined />}
                  onClick={() => setExportModalVisible(true)}
                >
                  导出
                </Button>
              </Tooltip>
              <Tooltip title="系统设置">
                <Button
                  icon={<SettingOutlined />}
                  onClick={() => setSettingsVisible(true)}
                >
                  设置
                </Button>
              </Tooltip>
            </Space>
          </Col>
        </Row>
      </div>

      {/* 概览统计卡片 */}
      <Row gutter={[16, 16]} className="collaboration-stats">
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="活跃智能体"
              value={activeAgentsCount}
              suffix={`/ ${agents.length}`}
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="运行中会话"
              value={runningSessionsCount}
              suffix={`/ ${activeSessions.length}`}
              prefix={<PlayCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="成功率"
              value={todaySuccessRate}
              suffix="%"
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="平均置信度"
              value={avgConfidence}
              suffix="%"
              prefix={<BarChartOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 系统状态提醒 */}
      {systemStatus === 'offline' && (
        <Alert
          message="系统离线"
          description="WebSocket连接已断开，实时数据更新可能受到影响。系统正在尝试重新连接..."
          type="warning"
          showIcon
          closable
          style={{ marginBottom: 16 }}
        />
      )}

      {/* 主要内容标签页 */}
      <Card className="collaboration-content">
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          type="card"
          size="large"
        >
          <TabPane
            tab={
              <span>
                <BarChartOutlined />
                协作概览
              </span>
            }
            key="dashboard"
          >
            <CollaborationDashboard
              metrics={metrics}
              agents={agents}
              activeSessions={activeSessions}
              history={history}
              realTimeData={realTimeData}
            />
          </TabPane>

          <TabPane
            tab={
              <span>
                <TeamOutlined />
                智能体管理
                <Badge count={activeAgentsCount} size="small" style={{ marginLeft: 8 }} />
              </span>
            }
            key="agents"
          >
            <AgentManagement
              agents={agents}
              onAgentsChange={setAgents}
              onRefresh={loadAgents}
            />
          </TabPane>

          <TabPane
            tab={
              <span>
                <PlayCircleOutlined />
                会话管理
                <Badge count={runningSessionsCount} size="small" style={{ marginLeft: 8 }} />
              </span>
            }
            key="sessions"
          >
            <SessionManagement
              sessions={activeSessions}
              agents={agents}
              onSessionsChange={setActiveSessions}
              onRefresh={loadActiveSessions}
            />
          </TabPane>

          <TabPane
            tab={
              <span>
                <ClockCircleOutlined />
                协作历史
              </span>
            }
            key="history"
          >
            <CollaborationHistory
              history={history}
              onHistoryChange={setHistory}
            />
          </TabPane>

          <TabPane
            tab={
              <span>
                <ThunderboltOutlined />
                实时监控
              </span>
            }
            key="monitor"
          >
            <RealtimeMonitor
              realTimeData={realTimeData}
              systemStatus={systemStatus}
              wsConnection={wsConnection}
            />
          </TabPane>
        </Tabs>
      </Card>

      {/* 快速协作模态框 */}
      <Modal
        title="快速协作"
        open={quickCollabVisible}
        onCancel={() => setQuickCollabVisible(false)}
        footer={null}
        width={800}
      >
        <QuickCollaboration
          agents={agents}
          onSubmit={handleQuickCollaboration}
          onCancel={() => setQuickCollabVisible(false)}
        />
      </Modal>

      {/* 设置模态框 */}
      <Modal
        title="协作设置"
        open={settingsVisible}
        onCancel={() => setSettingsVisible(false)}
        footer={null}
        width={1000}
      >
        <CollaborationSettings
          onSettingsChange={() => {
            message.success('设置已保存');
            setSettingsVisible(false);
            initializeData();
          }}
          onClearCache={handleClearCache}
        />
      </Modal>

      {/* 导出模态框 */}
      <Modal
        title="导出协作数据"
        open={exportModalVisible}
        onCancel={() => setExportModalVisible(false)}
        footer={null}
        width={600}
      >
        <div className="export-options">
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <div>
              <Text strong>选择导出类型：</Text>
              <Row gutter={[16, 16]} style={{ marginTop: 8 }}>
                <Col span={8}>
                  <Button
                    block
                    onClick={() => handleExport('sessions', 'excel')}
                    loading={loading}
                  >
                    会话数据 (Excel)
                  </Button>
                </Col>
                <Col span={8}>
                  <Button
                    block
                    onClick={() => handleExport('agents', 'csv')}
                    loading={loading}
                  >
                    智能体数据 (CSV)
                  </Button>
                </Col>
                <Col span={8}>
                  <Button
                    block
                    onClick={() => handleExport('metrics', 'json')}
                    loading={loading}
                  >
                    指标数据 (JSON)
                  </Button>
                </Col>
              </Row>
            </div>
          </Space>
        </div>
      </Modal>
    </div>
  );
};

export default CollaborationPage;