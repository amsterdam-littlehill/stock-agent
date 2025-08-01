/**
 * 智能体协作概览组件
 * @author Stock-Agent Team
 * @version 1.0
 */

import React, { useState } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Progress,
  Table,

  Avatar,
  Tag,
  Space,
  Alert,
  Timeline,
  Badge,
  Tooltip,
  Button,
  Select,
  DatePicker,
  Empty
} from 'antd';
import {
  TeamOutlined,
  RobotOutlined,
  PlayCircleOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  ClockCircleOutlined,
  BarChartOutlined,
  RiseOutlined,

  ThunderboltOutlined,
  MessageOutlined,
  StarOutlined,
  ReloadOutlined
} from '@ant-design/icons';
import { Line, Pie } from '@ant-design/plots';
import dayjs from 'dayjs';

import {
  AgentInfo,
  CollaborationSession,
  CollaborationMetrics,
  CollaborationHistory,
  CollaborationStatus,
  AgentStatus,
  CollaborationMode
} from '../../types/collaboration';

// const { Title } = Typography;
const { RangePicker } = DatePicker;
const { Option } = Select;

interface CollaborationDashboardProps {
  metrics: CollaborationMetrics | null;
  agents: AgentInfo[];
  activeSessions: CollaborationSession[];
  history: CollaborationHistory[];
  realTimeData: any;
}

/**
 * 智能体协作概览组件
 */
const CollaborationDashboard: React.FC<CollaborationDashboardProps> = ({
  metrics,
  agents,
  activeSessions,
  history,
  realTimeData
}) => {
  // 状态管理
  const [timeRange, setTimeRange] = useState<'1D' | '7D' | '30D' | 'custom'>('7D');
  const [customDateRange, setCustomDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs] | null>(null);
  // const [selectedMetric] = useState<'success' | 'performance' | 'efficiency'>('success');

  // 计算统计数据
  const totalAgents = agents.length;
  const activeAgents = agents.filter(agent => agent.status === AgentStatus.ACTIVE).length;
  const totalSessions = activeSessions.length;
  const runningSessions = activeSessions.filter(session => session.status === CollaborationStatus.RUNNING).length;
  const completedSessions = history.filter(h => h.status === CollaborationStatus.COMPLETED).length;
  const failedSessions = history.filter(h => h.status === CollaborationStatus.FAILED).length;

  // 成功率计算
  const successRate = completedSessions + failedSessions > 0 ?
    (completedSessions / (completedSessions + failedSessions) * 100).toFixed(1) : '0';

  // 平均置信度
  const avgConfidence = metrics ? (metrics.averageConfidence * 100).toFixed(1) : '0';

  // 协作模式分布数据
  const modeDistribution = [
    {
      type: '结构化辩论',
      value: history.filter(h => h.mode === CollaborationMode.STRUCTURED_DEBATE).length,
      color: '#667eea'
    },
    {
      type: '并行分析',
      value: history.filter(h => h.mode === CollaborationMode.PARALLEL_ANALYSIS).length,
      color: '#f093fb'
    },
    {
      type: '顺序流水线',
      value: history.filter(h => h.mode === CollaborationMode.SEQUENTIAL_PIPELINE).length,
      color: '#4facfe'
    },
    {
      type: '共识构建',
      value: history.filter(h => h.mode === CollaborationMode.CONSENSUS_BUILDING).length,
      color: '#43e97b'
    }
  ];

  // 时间趋势数据（模拟）
  const generateTrendData = () => {
    const days = timeRange === '1D' ? 1 : timeRange === '7D' ? 7 : 30;
    const data = [];

    for (let i = days - 1; i >= 0; i--) {
      const date = dayjs().subtract(i, 'day').format('MM-DD');
      data.push({
        date,
        sessions: Math.floor(Math.random() * 10) + 5,
        success: Math.floor(Math.random() * 8) + 4,
        agents: Math.floor(Math.random() * 5) + activeAgents - 2
      });
    }

    return data;
  };

  const trendData = generateTrendData();

  // 智能体性能排行
  const agentPerformance = agents.map(agent => {
    const agentSessions = history.filter(h => h.participantIds.includes(agent.id));
    const agentSuccess = agentSessions.filter(h => h.status === CollaborationStatus.COMPLETED).length;
    const successRate = agentSessions.length > 0 ? (agentSuccess / agentSessions.length * 100) : 0;

    return {
      ...agent,
      sessionCount: agentSessions.length,
      successRate,
      avgConfidence: agentSessions.length > 0 ?
        agentSessions.reduce((sum, s) => sum + (s.result?.confidence || 0), 0) / agentSessions.length * 100 : 0
    };
  }).sort((a, b) => b.successRate - a.successRate);

  // 最近活动
  const recentActivities = history
    .slice(0, 10)
    .sort((a, b) => new Date(b.completedAt || b.createdAt).getTime() - new Date(a.completedAt || a.createdAt).getTime());

  // 图表配置
  const lineConfig = {
    data: trendData,
    xField: 'date',
    yField: 'sessions',
    seriesField: 'type',
    smooth: true,
    animation: {
      appear: {
        animation: 'path-in',
        duration: 1000,
      },
    },
    color: ['#1890ff', '#52c41a', '#faad14'],
    legend: {position: 'top' as const,},
  };

  const pieConfig = {
    data: modeDistribution.filter(item => item.value > 0),
    angleField: 'value',
    colorField: 'type',
    radius: 0.8,
    label: {
      type: 'outer',
      content: '{name} {percentage}',
    },
    interactions: [
      {type: 'element-selected',},
      {type: 'element-active',},
    ],
    color: modeDistribution.map(item => item.color),
  };

  // 智能体性能表格列
  const performanceColumns: ColumnsType<any> = [
    {
      title: '排名',
      key: 'rank',
      render: (_, __, index) => (
        <div style={{ textAlign: 'center' }}>
          {index < 3 ? (
            <Badge
              count={index + 1}
              style={{backgroundColor: index === 0 ? '#faad14' : index === 1 ? '#52c41a' : '#1890ff'}}
            />
          ) : (
            <span style={{
              fontSize: 16,
              fontWeight: 600
            }}>{index + 1}</span>
          )}
        </div>
      ),
      width: 80,
    },
    {
      title: '智能体',
      key: 'agent',
      render: (_, record) => (
        <Space>
          <Avatar icon={<RobotOutlined />} size="small" />
          <div>
            <div style={{ fontWeight: 600 }}>{record.name}</div>
            <Text type="secondary" style={{ fontSize: 12 }}>{record.type}</Text>
          </div>
        </Space>
      ),
    },
    {
      title: '参与会话',
      dataIndex: 'sessionCount',
      key: 'sessionCount',
      render: (count: number) => (
        <Statistic value={count} suffix="次" valueStyle={{ fontSize: 14 }} />
      ),
      sorter: (a, b) => a.sessionCount - b.sessionCount,
    },
    {
      title: '成功率',
      dataIndex: 'successRate',
      key: 'successRate',
      render: (rate: number) => (
        <div>
          <Progress
            percent={rate}
            size="small"
            status={rate >= 80 ? 'success' : rate >= 60 ? 'normal' : 'exception'}
          />
          <Text style={{ fontSize: 12 }}>{rate.toFixed(1)}%</Text>
        </div>
      ),
      sorter: (a, b) => a.successRate - b.successRate,
    },
    {
      title: '平均置信度',
      dataIndex: 'avgConfidence',
      key: 'avgConfidence',
      render: (confidence: number) => (
        <div>
          <Progress
            percent={confidence}
            size="small"
            strokeColor={confidence >= 80 ? '#52c41a' : confidence >= 60 ? '#faad14' : '#ff4d4f'}
          />
          <Text style={{ fontSize: 12 }}>{confidence.toFixed(1)}%</Text>
        </div>
      ),
      sorter: (a, b) => a.avgConfidence - b.avgConfidence,
    },
  ];

  return (
    <div className="collaboration-dashboard">
      {/* 实时状态提醒 */}
      {realTimeData && (
        <Alert
          message={`实时更新: ${realTimeData.type}`}
          description={realTimeData.message || '协作系统状态已更新'}
          type="info"
          showIcon
          closable
          style={{ marginBottom: 16 }}
        />
      )}

      {/* 核心指标卡片 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} md={6}>
          <Card className="metric-card">
            <Statistic
              title="智能体总数"
              value={totalAgents}
              suffix={`(${activeAgents} 活跃)`}
              prefix={<TeamOutlined style={{ color: '#1890ff' }} />}
              valueStyle={{ color: '#1890ff' }}
            />
            <div style={{ marginTop: 8 }}>
              <Progress
                percent={totalAgents > 0 ? (activeAgents / totalAgents * 100) : 0}
                size="small"
                showInfo={false}
              />
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} md={6}>
          <Card className="metric-card">
            <Statistic
              title="协作会话"
              value={totalSessions}
              suffix={`(${runningSessions} 运行中)`}
              prefix={<PlayCircleOutlined style={{ color: '#52c41a' }} />}
              valueStyle={{ color: '#52c41a' }}
            />
            <div style={{ marginTop: 8 }}>
              <Progress
                percent={totalSessions > 0 ? (runningSessions / totalSessions * 100) : 0}
                size="small"
                showInfo={false}
                strokeColor="#52c41a"
              />
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} md={6}>
          <Card className="metric-card">
            <Statistic
              title="成功率"
              value={successRate}
              suffix="%"
              prefix={
                <CheckCircleOutlined
                  style={{ color: parseFloat(successRate) >= 80 ? '#52c41a' : '#faad14' }}
                />
              }
              valueStyle={{color: parseFloat(successRate) >= 80 ? '#52c41a' : '#faad14'}}
            />
            <div style={{ marginTop: 8 }}>
              <Space size="small">
                <RiseOutlined style={{ color: '#52c41a' }} />
                <Text type="secondary" style={{ fontSize: 12 }}>较昨日 +2.3%</Text>
              </Space>
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} md={6}>
          <Card className="metric-card">
            <Statistic
              title="平均置信度"
              value={avgConfidence}
              suffix="%"
              prefix={<StarOutlined style={{ color: '#722ed1' }} />}
              valueStyle={{ color: '#722ed1' }}
            />
            <div style={{ marginTop: 8 }}>
              <Space size="small">
                <RiseOutlined style={{ color: '#52c41a' }} />
                <Text type="secondary" style={{ fontSize: 12 }}>较昨日 +1.8%</Text>
              </Space>
            </div>
          </Card>
        </Col>
      </Row>

      {/* 图表区域 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} lg={16}>
          <Card
            title="协作趋势分析"
            extra={
              <Space>
                <Select value={timeRange} onChange={setTimeRange} size="small">
                  <Option value="1D">今日</Option>
                  <Option value="7D">近7天</Option>
                  <Option value="30D">近30天</Option>
                  <Option value="custom">自定义</Option>
                </Select>
                {timeRange === 'custom' && (
                  <RangePicker
                    size="small"
                    value={customDateRange}
                    onChange={setCustomDateRange}
                  />
                )}
              </Space>
            }
          >
            {trendData.length > 0 ? (
              <Line {...lineConfig} height={300} />
            ) : (
              <Empty description="暂无趋势数据" style={{ padding: '60px 0' }} />
            )}
          </Card>
        </Col>

        <Col xs={24} lg={8}>
          <Card title="协作模式分布">
            {modeDistribution.some(item => item.value > 0) ? (
              <Pie {...pieConfig} height={300} />
            ) : (
              <Empty description="暂无分布数据" style={{ padding: '60px 0' }} />
            )}
          </Card>
        </Col>
      </Row>

      {/* 详细信息区域 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={14}>
          <Card
            title="智能体性能排行"
            extra={
              <Tooltip title="刷新数据">
                <Button icon={<ReloadOutlined />} size="small" />
              </Tooltip>
            }
          >
            <Table
              columns={performanceColumns}
              dataSource={agentPerformance.slice(0, 10)}
              rowKey="id"
              pagination={false}
              size="small"
              className="performance-table"
            />
          </Card>
        </Col>

        <Col xs={24} lg={10}>
          <Card title="最近活动">
            {recentActivities.length > 0 ? (
              <Timeline>
                {recentActivities.map((activity, index) => {
                  const statusConfig = {
                    [CollaborationStatus.COMPLETED]: {
                      color: 'green',
                      icon: <CheckCircleOutlined />
                    },
                    [CollaborationStatus.FAILED]: {
                      color: 'red',
                      icon: <ExclamationCircleOutlined />
                    },
                    [CollaborationStatus.RUNNING]: {
                      color: 'blue',
                      icon: <PlayCircleOutlined />
                    },
                    [CollaborationStatus.CANCELLED]: {
                      color: 'orange',
                      icon: <ClockCircleOutlined />
                    }
                  };

                  const config = statusConfig[activity.status] || statusConfig[CollaborationStatus.COMPLETED];

                  return (
                    <Timeline.Item
                      key={index}
                      color={config.color}
                      dot={config.icon}
                    >
                      <div>
                        <Text strong>{activity.topic}</Text>
                        <div style={{ marginTop: 4 }}>
                          <Space size="small">
                            <Tag size="small">{activity.mode}</Tag>
                            <Text type="secondary" style={{ fontSize: 12 }}>
                              {new Date(activity.completedAt || activity.createdAt).toLocaleString()}
                            </Text>
                          </Space>
                        </div>
                        {activity.result && (
                          <div style={{ marginTop: 4 }}>
                            <Text type="secondary" style={{ fontSize: 12 }}>
                              置信度: {(activity.result.confidence * 100).toFixed(0)}%
                            </Text>
                          </div>
                        )}
                      </div>
                    </Timeline.Item>
                  );
                })}
              </Timeline>
            ) : (
              <Empty description="暂无活动记录" style={{ padding: '40px 0' }} />
            )}
          </Card>
        </Col>
      </Row>

      {/* 系统健康状态 */}
      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col span={24}>
          <Card title="系统健康状态">
            <Row gutter={[16, 16]}>
              <Col xs={24} sm={8}>
                <Card size="small" className="health-card">
                  <Statistic
                    title="API响应时间"
                    value={metrics?.averageResponseTime || 0}
                    suffix="ms"
                    prefix={<ThunderboltOutlined />}
                    valueStyle={{color: (metrics?.averageResponseTime || 0) < 1000 ? '#52c41a' : '#faad14'}}
                  />
                </Card>
              </Col>

              <Col xs={24} sm={8}>
                <Card size="small" className="health-card">
                  <Statistic
                    title="消息吞吐量"
                    value={metrics?.totalInteractions || 0}
                    suffix="/小时"
                    prefix={<MessageOutlined />}
                    valueStyle={{ color: '#1890ff' }}
                  />
                </Card>
              </Col>

              <Col xs={24} sm={8}>
                <Card size="small" className="health-card">
                  <Statistic
                    title="系统负载"
                    value={75}
                    suffix="%"
                    prefix={<BarChartOutlined />}
                    valueStyle={{ color: '#722ed1' }}
                  />
                  <Progress percent={75} size="small" showInfo={false} style={{ marginTop: 8 }} />
                </Card>
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default CollaborationDashboard;