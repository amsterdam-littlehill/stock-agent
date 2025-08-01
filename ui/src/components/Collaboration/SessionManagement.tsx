/**
 * 协作会话管理组件
 * @author Stock-Agent Team
 * @version 1.0
 */

import React, { useState, useCallback } from 'react';
import {
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Input,
  Select,
  message,
  Popconfirm,
  Card,
  Row,
  Col,
  Progress,
  Badge,
  Tooltip,
  Drawer,
  Descriptions,
  Timeline,
  Alert,
  Typography,
  Divider,
  Steps,
  List,
  Avatar,
  Statistic
} from 'antd';
import {
  PlusOutlined,
  PlayCircleOutlined,

  StopOutlined,
  EyeOutlined,
  ReloadOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  TeamOutlined,
  MessageOutlined,
  BarChartOutlined,

  BarChartOutlined
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';

import {
  CollaborationSession,
  CollaborationStatus,
  CollaborationMode,
  AgentInfo,
  CollaborationRequest,
  CollaborationInteraction,
  CollaborationInteraction
} from '../../types/collaboration';
import { CollaborationService } from '../../services/collaboration';

const { Option } = Select;
const { TextArea } = Input;
const { Text, Title } = Typography;
const { Step } = Steps;

interface SessionManagementProps {
  sessions: CollaborationSession[];
  agents: AgentInfo[];
  onSessionsChange: (sessions: CollaborationSession[]) => void;
  onRefresh: () => void;
}

/**
 * 协作会话管理组件
 */
const SessionManagement: React.FC<SessionManagementProps> = ({
  sessions,
  agents,
  onRefresh
}) => {
  // 状态管理
  const [loading, setLoading] = useState(false);
  const [selectedSession, setSelectedSession] = useState<CollaborationSession | null>(null);
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [detailDrawerVisible, setDetailDrawerVisible] = useState(false);
  const [monitorDrawerVisible, setMonitorDrawerVisible] = useState(false);

  // 表单实例
  const [createForm] = Form.useForm();

  // 过滤和搜索状态
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<CollaborationStatus | 'ALL'>('ALL');
  const [modeFilter, setModeFilter] = useState<CollaborationMode | 'ALL'>('ALL');

  // 实时监控数据
  const [sessionDetails, setSessionDetails] = useState<any>(null);
  const [sessionInteractions, setSessionInteractions] = useState<CollaborationInteraction[]>([]);

  /**
   * 创建协作会话
   */
  const handleCreateSession = useCallback(async (values: CollaborationRequest) => {
    try {
      setLoading(true);
      const response = await CollaborationService.createCollaborationSession(values);

      if (response.success) {
        message.success('协作会话创建成功');
        setCreateModalVisible(false);
        createForm.resetFields();
        onRefresh();
      } else {
        message.error(response.message || '协作会话创建失败');
      }
    } catch (error) {
      console.error('Create session failed:', error);
      message.error('协作会话创建失败');
    } finally {
      setLoading(false);
    }
  }, [createForm, onRefresh]);

  /**
   * 执行协作会话
   */
  const handleExecuteSession = useCallback(async (sessionId: string) => {
    try {
      setLoading(true);
      const response = await CollaborationService.executeCollaboration(sessionId);

      if (response.success) {
        message.success('协作会话已启动');
        onRefresh();
      } else {
        message.error(response.message || '协作会话启动失败');
      }
    } catch (error) {
      console.error('Execute session failed:', error);
      message.error('协作会话启动失败');
    } finally {
      setLoading(false);
    }
  }, [onRefresh]);

  /**
   * 取消协作会话
   */
  const handleCancelSession = useCallback(async (sessionId: string) => {
    try {
      setLoading(true);
      const response = await CollaborationService.cancelCollaboration(sessionId);

      if (response.success) {
        message.success('协作会话已取消');
        onRefresh();
      } else {
        message.error(response.message || '协作会话取消失败');
      }
    } catch (error) {
      console.error('Cancel session failed:', error);
      message.error('协作会话取消失败');
    } finally {
      setLoading(false);
    }
  }, [onRefresh]);

  /**
   * 查看会话详情
   */
  const handleViewDetails = useCallback(async (session: CollaborationSession) => {
    try {
      setSelectedSession(session);

      // 获取会话详细信息
      const response = await CollaborationService.getCollaborationStatus(session.id);
      if (response.success && response.data) {
        setSessionDetails(response.data);
      }

      setDetailDrawerVisible(true);
    } catch (error) {
      console.error('Get session details failed:', error);
      setSelectedSession(session);
      setDetailDrawerVisible(true);
    }
  }, []);

  /**
   * 实时监控会话
   */
  const handleMonitorSession = useCallback(async (session: CollaborationSession) => {
    try {
      setSelectedSession(session);

      // 获取会话交互记录
      const response = await CollaborationService.getCollaborationStatus(session.id);
      if (response.success && response.data) {
        setSessionDetails(response.data);
        setSessionInteractions(response.data.interactions || []);
      }

      setMonitorDrawerVisible(true);
    } catch (error) {
      console.error('Monitor session failed:', error);
      message.error('获取会话监控数据失败');
    }
  }, []);

  // 过滤会话列表
  const filteredSessions = sessions.filter(session => {
    const matchesSearch = !searchText ||
      session.id.toLowerCase().includes(searchText.toLowerCase()) ||
      session.topic.toLowerCase().includes(searchText.toLowerCase());

    const matchesStatus = statusFilter === 'ALL' || session.status === statusFilter;
    const matchesMode = modeFilter === 'ALL' || session.mode === modeFilter;

    return matchesSearch && matchesStatus && matchesMode;
  });

  // 统计数据
  const runningCount = sessions.filter(s => s.status === CollaborationStatus.RUNNING).length;
  const completedCount = sessions.filter(s => s.status === CollaborationStatus.COMPLETED).length;
  // const failedCount = sessions.filter(s => s.status === CollaborationStatus.FAILED).length;
  const pendingCount = sessions.filter(s => s.status === CollaborationStatus.PENDING).length;

  // 获取协作模式配置
  const getModeConfig = (mode: CollaborationMode) => {
    const configs = {
      [CollaborationMode.STRUCTURED_DEBATE]: {
        color: 'purple',
        text: '结构化辩论',
        icon: '🗣️'
      },
      [CollaborationMode.PARALLEL_ANALYSIS]: {
        color: 'blue',
        text: '并行分析',
        icon: '📊'
      },
      [CollaborationMode.SEQUENTIAL_PIPELINE]: {
        color: 'green',
        text: '顺序流水线',
        icon: '🔄'
      },
      [CollaborationMode.CONSENSUS_BUILDING]: {
        color: 'orange',
        text: '共识构建',
        icon: '🤝'
      }
    };
    return configs[mode] || {
      color: 'default',
      text: mode,
      icon: '❓'
    };
  };

  // 获取状态配置
  const getStatusConfig = (status: CollaborationStatus) => {
    const configs = {
      [CollaborationStatus.PENDING]: {
        color: 'default',
        text: '等待中',
        icon: <ClockCircleOutlined />
      },
      [CollaborationStatus.RUNNING]: {
        color: 'processing',
        text: '运行中',
        icon: <PlayCircleOutlined />
      },
      [CollaborationStatus.COMPLETED]: {
        color: 'success',
        text: '已完成',
        icon: <CheckCircleOutlined />
      },
      [CollaborationStatus.FAILED]: {
        color: 'error',
        text: '失败',
        icon: <ExclamationCircleOutlined />
      },
      [CollaborationStatus.CANCELLED]: {
        color: 'warning',
        text: '已取消',
        icon: <StopOutlined />
      }
    };
    return configs[status] || {
      color: 'default',
      text: status,
      icon: <ClockCircleOutlined />
    };
  };

  // 表格列定义
  const columns: ColumnsType<CollaborationSession> = [
    {
      title: '会话信息',
      key: 'info',
      render: (_, record) => (
        <div>
          <div style={{
            fontWeight: 600,
            fontSize: 14
          }}>{record.topic}</div>
          <div style={{
            color: '#666',
            fontSize: 12
          }}>ID: {record.id}</div>
        </div>
      ),
    },
    {
      title: '协作模式',
      dataIndex: 'mode',
      key: 'mode',
      render: (mode: CollaborationMode) => {
        const config = getModeConfig(mode);
        return (
          <Tag color={config.color}>
            <span style={{ marginRight: 4 }}>{config.icon}</span>
            {config.text}
          </Tag>
        );
      },
      filters: [
        {
          text: '结构化辩论',
          value: CollaborationMode.STRUCTURED_DEBATE
        },
        {
          text: '并行分析',
          value: CollaborationMode.PARALLEL_ANALYSIS
        },
        {
          text: '顺序流水线',
          value: CollaborationMode.SEQUENTIAL_PIPELINE
        },
        {
          text: '共识构建',
          value: CollaborationMode.CONSENSUS_BUILDING
        }
      ],
      onFilter: (value, record) => record.mode === value,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: CollaborationStatus) => {
        const config = getStatusConfig(status);
        return (
          <Badge
            status={config.color as any}
            text={
              <Space>
                {config.icon}
                {config.text}
              </Space>
            }
          />
        );
      },
      filters: [
        {
          text: '等待中',
          value: CollaborationStatus.PENDING
        },
        {
          text: '运行中',
          value: CollaborationStatus.RUNNING
        },
        {
          text: '已完成',
          value: CollaborationStatus.COMPLETED
        },
        {
          text: '失败',
          value: CollaborationStatus.FAILED
        },
        {
          text: '已取消',
          value: CollaborationStatus.CANCELLED
        }
      ],
      onFilter: (value, record) => record.status === value,
    },
    {
      title: '参与智能体',
      dataIndex: 'participantIds',
      key: 'participants',
      render: (participantIds: string[]) => (
        <Space>
          <TeamOutlined />
          <span>{participantIds.length}</span>
          <Tooltip title={participantIds.join(', ')}>
            <Button type="link" size="small">查看</Button>
          </Tooltip>
        </Space>
      ),
    },
    {
      title: '进度',
      key: 'progress',
      render: (_, record) => {
        let percent = 0;
        switch (record.status) {
          case CollaborationStatus.PENDING:
            percent = 0;
            break;
          case CollaborationStatus.RUNNING:
            percent = 50;
            break;
          case CollaborationStatus.COMPLETED:
            percent = 100;
            break;
          case CollaborationStatus.FAILED:
          case CollaborationStatus.CANCELLED:
            percent = 100;
            break;
        }

        return (
          <Progress
            percent={percent}
            size="small"
            status={
              record.status === CollaborationStatus.FAILED ? 'exception' :
                record.status === CollaborationStatus.COMPLETED ? 'success' : 'active'
            }
          />
        );
      },
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => new Date(date).toLocaleString(),
      sorter: (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
    },
    {
      title: '操作',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Tooltip title="查看详情">
            <Button
              type="text"
              icon={<EyeOutlined />}
              onClick={() => handleViewDetails(record)}
            />
          </Tooltip>

          {record.status === CollaborationStatus.RUNNING && (
            <Tooltip title="实时监控">
              <Button
                type="text"
                icon={<BarChartOutlined />}
                onClick={() => handleMonitorSession(record)}
              />
            </Tooltip>
          )}

          {record.status === CollaborationStatus.PENDING && (
            <Tooltip title="启动协作">
              <Button
                type="text"
                icon={<PlayCircleOutlined />}
                onClick={() => handleExecuteSession(record.id)}
              />
            </Tooltip>
          )}

          {(record.status === CollaborationStatus.PENDING ||
            record.status === CollaborationStatus.RUNNING) && (
            <Popconfirm
              title="确定要取消这个协作会话吗？"
              onConfirm={() => handleCancelSession(record.id)}
              okText="确定"
              cancelText="取消"
            >
              <Tooltip title="取消协作">
                <Button
                  type="text"
                  danger
                  icon={<StopOutlined />}
                />
              </Tooltip>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div className="session-management">
      {/* 统计概览 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} md={6}>
          <Card size="small">
            <Statistic
              title="总会话数"
              value={sessions.length}
              prefix={<MessageOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card size="small">
            <Statistic
              title="运行中"
              value={runningCount}
              prefix={<PlayCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card size="small">
            <Statistic
              title="已完成"
              value={completedCount}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card size="small">
            <Statistic
              title="等待中"
              value={pendingCount}
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 操作栏 */}
      <Card size="small" style={{ marginBottom: 16 }}>
        <Row justify="space-between" align="middle">
          <Col>
            <Space>
              <Input.Search
                placeholder="搜索会话ID或主题"
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
                style={{ width: 250 }}
                allowClear
              />
              <Select
                value={statusFilter}
                onChange={setStatusFilter}
                style={{ width: 120 }}
              >
                <Option value="ALL">全部状态</Option>
                <Option value={CollaborationStatus.PENDING}>等待中</Option>
                <Option value={CollaborationStatus.RUNNING}>运行中</Option>
                <Option value={CollaborationStatus.COMPLETED}>已完成</Option>
                <Option value={CollaborationStatus.FAILED}>失败</Option>
                <Option value={CollaborationStatus.CANCELLED}>已取消</Option>
              </Select>
              <Select
                value={modeFilter}
                onChange={setModeFilter}
                style={{ width: 140 }}
              >
                <Option value="ALL">全部模式</Option>
                <Option value={CollaborationMode.STRUCTURED_DEBATE}>结构化辩论</Option>
                <Option value={CollaborationMode.PARALLEL_ANALYSIS}>并行分析</Option>
                <Option value={CollaborationMode.SEQUENTIAL_PIPELINE}>顺序流水线</Option>
                <Option value={CollaborationMode.CONSENSUS_BUILDING}>共识构建</Option>
              </Select>
            </Space>
          </Col>
          <Col>
            <Space>
              <Button
                icon={<ReloadOutlined />}
                onClick={onRefresh}
                loading={loading}
              >
                刷新
              </Button>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => setCreateModalVisible(true)}
              >
                创建协作会话
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 会话列表 */}
      <Card>
        <Table
          columns={columns}
          dataSource={filteredSessions}
          rowKey="id"
          loading={loading}
          pagination={{
            total: filteredSessions.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) =>
              `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
          }}
          className="collaboration-table"
        />
      </Card>

      {/* 创建协作会话模态框 */}
      <Modal
        title="创建协作会话"
        open={createModalVisible}
        onCancel={() => {
          setCreateModalVisible(false);
          createForm.resetFields();
        }}
        footer={null}
        width={700}
      >
        <Form
          form={createForm}
          layout="vertical"
          onFinish={handleCreateSession}
        >
          <Form.Item
            name="topic"
            label="协作主题"
            rules={[{
              required: true,
              message: '请输入协作主题'
            }]}
          >
            <Input placeholder="请输入协作主题，如：分析AAPL股票投资价值" />
          </Form.Item>

          <Form.Item
            name="mode"
            label="协作模式"
            rules={[{
              required: true,
              message: '请选择协作模式'
            }]}
          >
            <Select placeholder="请选择协作模式">
              <Option value={CollaborationMode.STRUCTURED_DEBATE}>
                🗣️ 结构化辩论 - 智能体进行结构化的观点辩论
              </Option>
              <Option value={CollaborationMode.PARALLEL_ANALYSIS}>
                📊 并行分析 - 智能体并行分析不同方面
              </Option>
              <Option value={CollaborationMode.SEQUENTIAL_PIPELINE}>
                🔄 顺序流水线 - 智能体按顺序处理任务
              </Option>
              <Option value={CollaborationMode.CONSENSUS_BUILDING}>
                🤝 共识构建 - 智能体协商达成共识
              </Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="participantIds"
            label="参与智能体"
            rules={[{
              required: true,
              message: '请选择参与智能体'
            }]}
          >
            <Select
              mode="multiple"
              placeholder="请选择参与智能体"
              optionFilterProp="children"
            >
              {agents.filter(agent => agent.status === 'ACTIVE').map(agent => (
                <Option key={agent.id} value={agent.id}>
                  <Space>
                    <div className="agent-type-icon analysis">
                      <TeamOutlined />
                    </div>
                    {agent.name} ({agent.type})
                  </Space>
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            name="description"
            label="协作描述"
            rules={[{
              required: true,
              message: '请输入协作描述'
            }]}
          >
            <TextArea
              rows={3}
              placeholder="请详细描述协作的目标、要求和期望结果"
            />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="priority"
                label="优先级"
                initialValue="MEDIUM"
              >
                <Select>
                  <Option value="LOW">低</Option>
                  <Option value="MEDIUM">中</Option>
                  <Option value="HIGH">高</Option>
                  <Option value="URGENT">紧急</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="timeout"
                label="超时时间(分钟)"
                initialValue={30}
              >
                <Input type="number" min={1} max={1440} />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="parameters"
            label="协作参数 (JSON格式)"
            tooltip="可选的协作参数，如股票代码、分析时间范围等"
          >
            <TextArea
              rows={4}
              placeholder={`{
  "stockSymbol": "AAPL",
  "timeRange": "1M",
  "analysisDepth": "detailed"
}`}
            />
          </Form.Item>

          <Form.Item style={{
            marginBottom: 0,
            textAlign: 'right'
          }}>
            <Space>
              <Button onClick={() => {
                setCreateModalVisible(false);
                createForm.resetFields();
              }}>
                取消
              </Button>
              <Button type="primary" htmlType="submit" loading={loading}>
                创建会话
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* 会话详情抽屉 */}
      <Drawer
        title="协作会话详情"
        placement="right"
        onClose={() => setDetailDrawerVisible(false)}
        open={detailDrawerVisible}
        width={700}
      >
        {selectedSession && (
          <div>
            <Descriptions title="基本信息" bordered column={1}>
              <Descriptions.Item label="会话ID">{selectedSession.id}</Descriptions.Item>
              <Descriptions.Item label="主题">{selectedSession.topic}</Descriptions.Item>
              <Descriptions.Item label="协作模式">
                {(() => {
                  const config = getModeConfig(selectedSession.mode);
                  return (
                    <Tag color={config.color}>
                      <span style={{ marginRight: 4 }}>{config.icon}</span>
                      {config.text}
                    </Tag>
                  );
                })()}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                {(() => {
                  const config = getStatusConfig(selectedSession.status);
                  return (
                    <Badge
                      status={config.color as any}
                      text={
                        <Space>
                          {config.icon}
                          {config.text}
                        </Space>
                      }
                    />
                  );
                })()}
              </Descriptions.Item>
              <Descriptions.Item label="描述">{selectedSession.description}</Descriptions.Item>
              <Descriptions.Item label="创建时间">
                {new Date(selectedSession.createdAt).toLocaleString()}
              </Descriptions.Item>
              <Descriptions.Item label="开始时间">
                {selectedSession.startedAt ?
                  new Date(selectedSession.startedAt).toLocaleString() :
                  '未开始'
                }
              </Descriptions.Item>
              <Descriptions.Item label="结束时间">
                {selectedSession.completedAt ?
                  new Date(selectedSession.completedAt).toLocaleString() :
                  '未结束'
                }
              </Descriptions.Item>
            </Descriptions>

            <Divider />

            <Title level={4}>参与智能体</Title>
            <List
              dataSource={selectedSession.participantIds}
              renderItem={(agentId) => {
                const agent = agents.find(a => a.id === agentId);
                return (
                  <List.Item>
                    <List.Item.Meta
                      avatar={<Avatar icon={<TeamOutlined />} />}
                      title={agent?.name || agentId}
                      description={agent?.description || '智能体信息不可用'}
                    />
                    {agent && (
                      <Tag color="blue">{agent.type}</Tag>
                    )}
                  </List.Item>
                );
              }}
            />

            {sessionDetails && sessionDetails.result && (
              <>
                <Divider />
                <Title level={4}>协作结果</Title>
                <Card size="small">
                  <Descriptions column={1}>
                    <Descriptions.Item label="置信度">
                      <Progress
                        percent={Math.round(sessionDetails.result.confidence * 100)}
                        size="small"
                      />
                    </Descriptions.Item>
                    <Descriptions.Item label="结论">
                      {sessionDetails.result.conclusion}
                    </Descriptions.Item>
                    <Descriptions.Item label="建议">
                      <ul>
                        {sessionDetails.result.recommendations?.map((rec: string, index: number) => (
                          <li key={index}>{rec}</li>
                        ))}
                      </ul>
                    </Descriptions.Item>
                  </Descriptions>
                </Card>
              </>
            )}

            {selectedSession.parameters && (
              <>
                <Divider />
                <Title level={4}>协作参数</Title>
                <pre style={{
                  background: '#f5f5f5',
                  padding: 16,
                  borderRadius: 4,
                  fontSize: 12
                }}>
                  {JSON.stringify(selectedSession.parameters, null, 2)}
                </pre>
              </>
            )}
          </div>
        )}
      </Drawer>

      {/* 实时监控抽屉 */}
      <Drawer
        title="实时监控"
        placement="right"
        onClose={() => setMonitorDrawerVisible(false)}
        open={monitorDrawerVisible}
        width={800}
      >
        {selectedSession && (
          <div>
            <Alert
              message="实时监控"
              description={`正在监控协作会话: ${selectedSession.topic}`}
              type="info"
              showIcon
              style={{ marginBottom: 16 }}
            />

            {/* 协作进度 */}
            <Card title="协作进度" size="small" style={{ marginBottom: 16 }}>
              <Steps
                current={selectedSession.status === CollaborationStatus.COMPLETED ? 3 :
                  selectedSession.status === CollaborationStatus.RUNNING ? 2 :
                    selectedSession.status === CollaborationStatus.PENDING ? 1 : 0}
                status={selectedSession.status === CollaborationStatus.FAILED ? 'error' : 'process'}
              >
                <Step title="创建" description="会话已创建" />
                <Step title="启动" description="协作已启动" />
                <Step title="执行" description="智能体协作中" />
                <Step title="完成" description="协作已完成" />
              </Steps>
            </Card>

            {/* 交互时间线 */}
            <Card title="交互记录" size="small">
              <Timeline>
                {sessionInteractions.map((interaction, index) => (
                  <Timeline.Item
                    key={index}
                    color={interaction.type === 'OPINION' ? 'blue' :
                      interaction.type === 'QUESTION' ? 'orange' : 'green'}
                  >
                    <div>
                      <Text strong>{interaction.agentId}</Text>
                      <Text type="secondary" style={{ marginLeft: 8 }}>
                        {new Date(interaction.timestamp).toLocaleTimeString()}
                      </Text>
                    </div>
                    <div style={{ marginTop: 4 }}>
                      {interaction.content}
                    </div>
                    {interaction.confidence && (
                      <div style={{ marginTop: 4 }}>
                        <Text type="secondary">置信度: </Text>
                        <Progress
                          percent={Math.round(interaction.confidence * 100)}
                          size="small"
                          style={{
                            width: 100,
                            display: 'inline-block'
                          }}
                        />
                      </div>
                    )}
                  </Timeline.Item>
                ))}
              </Timeline>
            </Card>
          </div>
        )}
      </Drawer>
    </div>
  );
};

export default SessionManagement;