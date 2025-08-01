/**
 * 实时监控组件
 * @author Stock-Agent Team
 * @version 1.0
 */

import React, { useState, useEffect, useCallback, useRef } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,

  Badge,
  Tag,

  Space,
  Typography,
  Alert,
  Timeline,
  Button,
  Switch,
  Select,
  Tooltip,

  Empty,
  Spin,
  notification
} from 'antd';
import {
  WifiOutlined,
  DisconnectOutlined,
  ReloadOutlined,
  PauseCircleOutlined,
  PlayCircleOutlined,
  SettingOutlined,
  BellOutlined,
  EyeOutlined,
  ThunderboltOutlined,

  WarningOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  TeamOutlined,
  MessageOutlined,
  MessageOutlined
} from '@ant-design/icons';
import { Line, Gauge, Liquid } from '@ant-design/plots';
import dayjs from 'dayjs';

interface RealtimeMonitorProps {
  realTimeData: any;
  systemStatus: 'online' | 'offline' | 'maintenance';
  wsConnection: WebSocket | null;
}

const { Text } = Typography;
const { Option } = Select;

/**
 * 实时监控组件
 */
const RealtimeMonitor: React.FC<RealtimeMonitorProps> = ({
  realTimeData,
  systemStatus,
  wsConnection
}) => {
  // 状态管理
  const [isMonitoring, setIsMonitoring] = useState(true);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [refreshInterval, setRefreshInterval] = useState(5); // 秒
  const [notifications, setNotifications] = useState(true);
  const [selectedMetrics, setSelectedMetrics] = useState(['cpu', 'memory', 'network']);

  // 实时数据
  const [systemMetrics, setSystemMetrics] = useState({
    cpu: 0,
    memory: 0,
    network: 0,
    activeConnections: 0,
    messageRate: 0,
    errorRate: 0
  });

  const [recentEvents, setRecentEvents] = useState<any[]>([]);
  const [performanceData, setPerformanceData] = useState<any[]>([]);
  const [connectionStats, setConnectionStats] = useState({
    totalConnections: 0,
    activeAgents: 0,
    activeSessions: 0,
    messagesSent: 0,
    messagesReceived: 0,
    errors: 0
  });

  // 引用
  const intervalRef = useRef<NodeJS.Timeout | null>(null);
  const eventLogRef = useRef<HTMLDivElement>(null);

  /**
   * 生成模拟系统指标
   */
  const generateSystemMetrics = useCallback(() => {
    return {
      cpu: Math.random() * 100,
      memory: Math.random() * 100,
      network: Math.random() * 100,
      activeConnections: Math.floor(Math.random() * 50) + 10,
      messageRate: Math.floor(Math.random() * 1000) + 100,
      errorRate: Math.random() * 5
    };
  }, []);

  /**
   * 生成性能趋势数据
   */
  const generatePerformanceData = useCallback(() => {
    const now = dayjs();
    const data = [];

    for (let i = 29; i >= 0; i--) {
      const time = now.subtract(i, 'minute').format('HH:mm');
      data.push({
        time,
        cpu: Math.random() * 100,
        memory: Math.random() * 100,
        network: Math.random() * 100
      });
    }

    return data;
  }, []);

  /**
   * 添加事件到日志
   */
  const addEvent = useCallback((event: any) => {
    setRecentEvents(prev => {
      const newEvents = [{
        id: Date.now(),
        timestamp: new Date().toISOString(),
        ...event
      }, ...prev.slice(0, 49)]; // 保持最新50条

      return newEvents;
    });

    // 滚动到最新事件
    if (eventLogRef.current) {
      eventLogRef.current.scrollTop = 0;
    }

    // 显示通知
    if (notifications && event.level === 'error') {
      notification.error({
        message: '系统错误',
        description: event.message,
        placement: 'topRight'
      });
    } else if (notifications && event.level === 'warning') {
      notification.warning({
        message: '系统警告',
        description: event.message,
        placement: 'topRight'
      });
    }
  }, [notifications]);

  /**
   * 更新系统指标
   */
  const updateMetrics = useCallback(() => {
    if (!isMonitoring) return;

    const newMetrics = generateSystemMetrics();
    setSystemMetrics(newMetrics);

    // 更新性能数据
    setPerformanceData(prev => {
      const newData = [...prev];
      const now = dayjs().format('HH:mm');

      newData.push({
        time: now,
        cpu: newMetrics.cpu,
        memory: newMetrics.memory,
        network: newMetrics.network
      });

      // 保持最新30个数据点
      return newData.slice(-30);
    });

    // 更新连接统计
    setConnectionStats(prev => ({
      ...prev,
      totalConnections: prev.totalConnections + Math.floor(Math.random() * 3),
      messagesSent: prev.messagesSent + Math.floor(Math.random() * 10),
      messagesReceived: prev.messagesReceived + Math.floor(Math.random() * 8),
      errors: newMetrics.errorRate > 3 ? prev.errors + 1 : prev.errors
    }));

    // 检查异常情况
    if (newMetrics.cpu > 90) {
      addEvent({
        type: 'SYSTEM_ALERT',
        level: 'warning',
        message: `CPU使用率过高: ${newMetrics.cpu.toFixed(1)}%`,
        source: 'System Monitor'
      });
    }

    if (newMetrics.memory > 85) {
      addEvent({
        type: 'SYSTEM_ALERT',
        level: 'warning',
        message: `内存使用率过高: ${newMetrics.memory.toFixed(1)}%`,
        source: 'System Monitor'
      });
    }

    if (newMetrics.errorRate > 4) {
      addEvent({
        type: 'ERROR_RATE_HIGH',
        level: 'error',
        message: `错误率异常: ${newMetrics.errorRate.toFixed(2)}%`,
        source: 'Error Monitor'
      });
    }
  }, [isMonitoring, generateSystemMetrics, addEvent]);

  /**
   * 处理实时数据更新
   */
  useEffect(() => {
    if (realTimeData) {
      addEvent({
        type: realTimeData.type,
        level: realTimeData.level || 'info',
        message: realTimeData.message || '收到实时数据更新',
        source: realTimeData.source || 'WebSocket',
        data: realTimeData
      });
    }
  }, [realTimeData, addEvent]);

  /**
   * 设置自动刷新
   */
  useEffect(() => {
    if (autoRefresh && isMonitoring) {
      intervalRef.current = setInterval(updateMetrics, refreshInterval * 1000);
    } else {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    }

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, [autoRefresh, isMonitoring, refreshInterval, updateMetrics]);

  /**
   * 初始化数据
   */
  useEffect(() => {
    setPerformanceData(generatePerformanceData());
    updateMetrics();

    // 添加初始事件
    addEvent({
      type: 'MONITOR_STARTED',
      level: 'info',
      message: '实时监控已启动',
      source: 'Monitor'
    });
  }, []);

  // 获取系统状态配置
  const getStatusConfig = (status: string) => {
    const configs = {
      online: {
        color: 'success',
        text: '在线',
        icon: <WifiOutlined />
      },
      offline: {
        color: 'error',
        text: '离线',
        icon: <DisconnectOutlined />
      },
      maintenance: {
        color: 'warning',
        text: '维护中',
        icon: <SettingOutlined />
      }
    };
    return configs[status] || configs.offline;
  };

  const statusConfig = getStatusConfig(systemStatus);

  // 图表配置
  const lineConfig = {
    data: performanceData,
    xField: 'time',
    yField: 'value',
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

  const gaugeConfig = {
    percent: systemMetrics.cpu / 100,
    range: {color: systemMetrics.cpu > 80 ? '#ff4d4f' : systemMetrics.cpu > 60 ? '#faad14' : '#52c41a',},
    indicator: {
      pointer: {style: {stroke: '#D0D0D0',},},
      pin: {style: {stroke: '#D0D0D0',},},
    },
    statistic: {
      content: {
        style: {
          fontSize: '36px',
          lineHeight: '36px',
        },
        formatter: () => `${systemMetrics.cpu.toFixed(1)}%`,
      },
    },
  };

  const liquidConfig = {
    percent: systemMetrics.memory / 100,
    outline: {
      border: 4,
      distance: 8,
    },
    wave: {length: 128,},
    statistic: {
      content: {
        style: {
          fontSize: '24px',
          fill: '#fff',
        },
        formatter: () => `${systemMetrics.memory.toFixed(1)}%`,
      },
    },
    color: systemMetrics.memory > 80 ? '#ff4d4f' : systemMetrics.memory > 60 ? '#faad14' : '#1890ff',
  };

  return (
    <div className="realtime-monitor">
      {/* 监控控制栏 */}
      <Card size="small" style={{ marginBottom: 16 }}>
        <Row justify="space-between" align="middle">
          <Col>
            <Space>
              <Badge
                status={statusConfig.color as any}
                text={
                  <Space>
                    {statusConfig.icon}
                    <Text strong>系统状态: {statusConfig.text}</Text>
                  </Space>
                }
              />
              {wsConnection && (
                <Tag color="green">
                  <WifiOutlined /> WebSocket 已连接
                </Tag>
              )}
            </Space>
          </Col>
          <Col>
            <Space>
              <Text>监控:</Text>
              <Switch
                checked={isMonitoring}
                onChange={setIsMonitoring}
                checkedChildren={<EyeOutlined />}
                unCheckedChildren={<PauseCircleOutlined />}
              />

              <Text>自动刷新:</Text>
              <Switch
                checked={autoRefresh}
                onChange={setAutoRefresh}
                disabled={!isMonitoring}
              />

              <Select
                value={refreshInterval}
                onChange={setRefreshInterval}
                size="small"
                style={{ width: 80 }}
                disabled={!autoRefresh || !isMonitoring}
              >
                <Option value={1}>1s</Option>
                <Option value={5}>5s</Option>
                <Option value={10}>10s</Option>
                <Option value={30}>30s</Option>
              </Select>

              <Tooltip title="通知">
                <Button
                  size="small"
                  type={notifications ? 'primary' : 'default'}
                  icon={<BellOutlined />}
                  onClick={() => setNotifications(!notifications)}
                />
              </Tooltip>

              <Button
                size="small"
                icon={<ReloadOutlined />}
                onClick={updateMetrics}
                disabled={!isMonitoring}
              >
                刷新
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 系统指标概览 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={8} md={6}>
          <Card size="small">
            <Statistic
              title="活跃连接"
              value={systemMetrics.activeConnections}
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8} md={6}>
          <Card size="small">
            <Statistic
              title="消息速率"
              value={systemMetrics.messageRate}
              suffix="/min"
              prefix={<MessageOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8} md={6}>
          <Card size="small">
            <Statistic
              title="网络吞吐"
              value={systemMetrics.network.toFixed(1)}
              suffix="MB/s"
              prefix={<ThunderboltOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8} md={6}>
          <Card size="small">
            <Statistic
              title="错误率"
              value={systemMetrics.errorRate.toFixed(2)}
              suffix="%"
              prefix={<WarningOutlined />}
              valueStyle={{color: systemMetrics.errorRate > 3 ? '#ff4d4f' : '#52c41a'}}
            />
          </Card>
        </Col>
      </Row>

      {/* 性能监控图表 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} lg={12}>
          <Card title="CPU 使用率" size="small">
            <div style={{ height: 200 }}>
              <Gauge {...gaugeConfig} height={200} />
            </div>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="内存使用率" size="small">
            <div style={{ height: 200 }}>
              <Liquid {...liquidConfig} height={200} />
            </div>
          </Card>
        </Col>
      </Row>

      {/* 性能趋势和事件日志 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={14}>
          <Card
            title="性能趋势"
            size="small"
            extra={
              <Select
                mode="multiple"
                value={selectedMetrics}
                onChange={setSelectedMetrics}
                size="small"
                style={{ width: 200 }}
                placeholder="选择指标"
              >
                <Option value="cpu">CPU</Option>
                <Option value="memory">内存</Option>
                <Option value="network">网络</Option>
              </Select>
            }
          >
            {performanceData.length > 0 ? (
              <div style={{ height: 300 }}>
                <Line
                  data={performanceData.flatMap(item =>
                    selectedMetrics.map(metric => ({
                      time: item.time,
                      value: item[metric],
                      type: metric.toUpperCase()
                    }))
                  )}
                  {...lineConfig}
                  height={300}
                />
              </div>
            ) : (
              <div style={{
                height: 300,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}>
                <Spin tip="加载性能数据..." />
              </div>
            )}
          </Card>
        </Col>

        <Col xs={24} lg={10}>
          <Card
            title="实时事件日志"
            size="small"
            extra={
              <Space>
                <Badge count={recentEvents.length} size="small" />
                <Button
                  size="small"
                  onClick={() => setRecentEvents([])}
                >
                  清空
                </Button>
              </Space>
            }
          >
            <div
              ref={eventLogRef}
              style={{
                height: 300,
                overflowY: 'auto',
                border: '1px solid #f0f0f0',
                borderRadius: 4,
                padding: 8
              }}
            >
              {recentEvents.length > 0 ? (
                <Timeline size="small">
                  {recentEvents.map((event) => {
                    const getEventIcon = (level: string) => {
                      switch (level) {
                        case 'error':
                          return <WarningOutlined style={{ color: '#ff4d4f' }} />;
                        case 'warning':
                          return <ExclamationCircleOutlined style={{ color: '#faad14' }} />;
                        case 'success':
                          return <CheckCircleOutlined style={{ color: '#52c41a' }} />;
                        default:
                          return <ClockCircleOutlined style={{ color: '#1890ff' }} />;
                      }
                    };

                    return (
                      <Timeline.Item
                        key={event.id}
                        dot={getEventIcon(event.level)}
                      >
                        <div>
                          <Text strong style={{ fontSize: 12 }}>
                            {event.type}
                          </Text>
                          <Text type="secondary" style={{
                            marginLeft: 8,
                            fontSize: 11
                          }}>
                            {dayjs(event.timestamp).format('HH:mm:ss')}
                          </Text>
                        </div>
                        <div style={{
                          fontSize: 12,
                          marginTop: 2
                        }}>
                          {event.message}
                        </div>
                        <div style={{
                          fontSize: 11,
                          color: '#999',
                          marginTop: 2
                        }}>
                          来源: {event.source}
                        </div>
                      </Timeline.Item>
                    );
                  })}
                </Timeline>
              ) : (
                <Empty
                  description="暂无事件"
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                  style={{ padding: '40px 0' }}
                />
              )}
            </div>
          </Card>
        </Col>
      </Row>

      {/* 连接统计 */}
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card title="连接统计" size="small">
            <Row gutter={[16, 16]}>
              <Col xs={12} sm={8} md={4}>
                <Statistic
                  title="总连接数"
                  value={connectionStats.totalConnections}
                  prefix={<WifiOutlined />}
                />
              </Col>
              <Col xs={12} sm={8} md={4}>
                <Statistic
                  title="活跃智能体"
                  value={connectionStats.activeAgents}
                  prefix={<TeamOutlined />}
                />
              </Col>
              <Col xs={12} sm={8} md={4}>
                <Statistic
                  title="活跃会话"
                  value={connectionStats.activeSessions}
                  prefix={<PlayCircleOutlined />}
                />
              </Col>
              <Col xs={12} sm={8} md={4}>
                <Statistic
                  title="发送消息"
                  value={connectionStats.messagesSent}
                  prefix={<MessageOutlined />}
                />
              </Col>
              <Col xs={12} sm={8} md={4}>
                <Statistic
                  title="接收消息"
                  value={connectionStats.messagesReceived}
                  prefix={<MessageOutlined />}
                />
              </Col>
              <Col xs={12} sm={8} md={4}>
                <Statistic
                  title="错误次数"
                  value={connectionStats.errors}
                  prefix={<WarningOutlined />}
                  valueStyle={{color: connectionStats.errors > 10 ? '#ff4d4f' : '#52c41a'}}
                />
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>

      {/* 系统健康检查 */}
      {systemStatus === 'offline' && (
        <Alert
          message="系统离线"
          description="检测到系统连接异常，请检查网络连接或联系系统管理员。"
          type="error"
          showIcon
          style={{ marginTop: 16 }}
          action={
            <Button size="small" danger onClick={() => window.location.reload()}>
              重新连接
            </Button>
          }
        />
      )}

      {systemMetrics.cpu > 90 && (
        <Alert
          message="CPU使用率过高"
          description={`当前CPU使用率为 ${systemMetrics.cpu.toFixed(1)}%，建议检查系统负载。`}
          type="warning"
          showIcon
          style={{ marginTop: 16 }}
        />
      )}

      {systemMetrics.memory > 85 && (
        <Alert
          message="内存使用率过高"
          description={`当前内存使用率为 ${systemMetrics.memory.toFixed(1)}%，建议释放内存资源。`}
          type="warning"
          showIcon
          style={{ marginTop: 16 }}
        />
      )}
    </div>
  );
};

export default RealtimeMonitor;