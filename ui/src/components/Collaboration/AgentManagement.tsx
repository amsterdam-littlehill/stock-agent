/**
 * 智能体管理组件
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
  Statistic,
  Badge,
  Tooltip,
  Drawer,
  Descriptions,
  Alert,
  Typography,
  Divider
} from 'antd';
import {
  PlusOutlined,
  DeleteOutlined,
  EditOutlined,
  EyeOutlined,
  ReloadOutlined,
  RobotOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
  TeamOutlined
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';

import {
  AgentInfo,
  AgentType,
  AgentStatus
} from '../../types/collaboration';
import { CollaborationService } from '../../services/collaboration';

const { Option } = Select;
const { TextArea } = Input;
const { Title } = Typography;

interface AgentManagementProps {
  agents: AgentInfo[];
  onAgentsChange: (agents: AgentInfo[]) => void;
  onRefresh: () => void;
}

/**
 * 智能体管理组件
 */
const AgentManagement: React.FC<AgentManagementProps> = ({
  agents,
  onRefresh
}) => {
  // 状态管理
  const [loading, setLoading] = useState(false);
  const [selectedAgent, setSelectedAgent] = useState<AgentInfo | null>(null);
  const [registerModalVisible, setRegisterModalVisible] = useState(false);
  const [detailDrawerVisible, setDetailDrawerVisible] = useState(false);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [batchRegisterVisible, setBatchRegisterVisible] = useState(false);

  // 表单实例
  const [registerForm] = Form.useForm();
  const [editForm] = Form.useForm();
  const [batchForm] = Form.useForm();

  // 过滤和搜索状态
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<AgentStatus | 'ALL'>('ALL');
  const [typeFilter, setTypeFilter] = useState<AgentType | 'ALL'>('ALL');

  /**
   * 注册智能体
   */
  const handleRegisterAgent = useCallback(async (values: Omit<AgentInfo, 'lastActiveTime' | 'totalCollaborations' | 'successRate' | 'averageConfidence'>) => {
    try {
      setLoading(true);
      const response = await CollaborationService.registerAgent(values);

      if (response.success) {
        message.success('智能体注册成功');
        setRegisterModalVisible(false);
        registerForm.resetFields();
        onRefresh();
      } else {
        message.error(response.message || '智能体注册失败');
      }
    } catch (error) {
      console.error('Register agent failed:', error);
      message.error('智能体注册失败');
    } finally {
      setLoading(false);
    }
  }, [registerForm, onRefresh]);

  /**
   * 注销智能体
   */
  const handleUnregisterAgent = useCallback(async (agentId: string) => {
    try {
      setLoading(true);
      const response = await CollaborationService.unregisterAgent(agentId);

      if (response.success) {
        message.success('智能体注销成功');
        onRefresh();
      } else {
        message.error(response.message || '智能体注销失败');
      }
    } catch (error) {
      console.error('Unregister agent failed:', error);
      message.error('智能体注销失败');
    } finally {
      setLoading(false);
    }
  }, [onRefresh]);

  /**
   * 更新智能体信息
   */
  const handleUpdateAgent = useCallback(async (agentId: string, values: Partial<AgentInfo>) => {
    try {
      setLoading(true);
      const response = await CollaborationService.updateAgent(agentId, values);

      if (response.success) {
        message.success('智能体信息更新成功');
        setEditModalVisible(false);
        editForm.resetFields();
        onRefresh();
      } else {
        message.error(response.message || '智能体信息更新失败');
      }
    } catch (error) {
      console.error('Update agent failed:', error);
      message.error('智能体信息更新失败');
    } finally {
      setLoading(false);
    }
  }, [editForm, onRefresh]);

  /**
   * 批量注册智能体
   */
  const handleBatchRegister = useCallback(async (values: { agents: Omit<AgentInfo, 'lastActiveTime' | 'totalCollaborations' | 'successRate' | 'averageConfidence'>[] }) => {
    try {
      setLoading(true);
      const response = await CollaborationService.batchRegisterAgents(values.agents);

      if (response.success) {
        message.success(`成功注册 ${response.data?.successCount || 0} 个智能体`);
        if (response.data?.failedCount && response.data.failedCount > 0) {
          message.warning(`${response.data.failedCount} 个智能体注册失败`);
        }
        setBatchRegisterVisible(false);
        batchForm.resetFields();
        onRefresh();
      } else {
        message.error(response.message || '批量注册失败');
      }
    } catch (error) {
      console.error('Batch register failed:', error);
      message.error('批量注册失败');
    } finally {
      setLoading(false);
    }
  }, [batchForm, onRefresh]);

  /**
   * 查看智能体详情
   */
  const handleViewDetails = useCallback(async (agent: AgentInfo) => {
    try {
      const response = await CollaborationService.getAgentDetails(agent.id);
      if (response.success && response.data) {
        setSelectedAgent(response.data);
      } else {
        setSelectedAgent(agent);
      }
      setDetailDrawerVisible(true);
    } catch (error) {
      console.error('Get agent details failed:', error);
      setSelectedAgent(agent);
      setDetailDrawerVisible(true);
    }
  }, []);

  /**
   * 编辑智能体
   */
  const handleEditAgent = useCallback((agent: AgentInfo) => {
    setSelectedAgent(agent);
    editForm.setFieldsValue({
      name: agent.name,
      description: agent.description,
      endpoint: agent.endpoint,
      capabilities: agent.capabilities
    });
    setEditModalVisible(true);
  }, [editForm]);

  // 过滤智能体列表
  const filteredAgents = agents.filter(agent => {
    const matchesSearch = !searchText ||
      agent.name.toLowerCase().includes(searchText.toLowerCase()) ||
      agent.description.toLowerCase().includes(searchText.toLowerCase());

    const matchesStatus = statusFilter === 'ALL' || agent.status === statusFilter;
    const matchesType = typeFilter === 'ALL' || agent.type === typeFilter;

    return matchesSearch && matchesStatus && matchesType;
  });

  // 统计数据
  const activeCount = agents.filter(agent => agent.status === AgentStatus.ACTIVE).length;
  const inactiveCount = agents.filter(agent => agent.status === AgentStatus.INACTIVE).length;
  const errorCount = agents.filter(agent => agent.status === AgentStatus.ERROR).length;

  // 表格列定义
  const columns: ColumnsType<AgentInfo> = [
    {
      title: '智能体信息',
      key: 'info',
      render: (_, record) => (
        <Space>
          <div className="agent-type-icon analysis">
            <RobotOutlined />
          </div>
          <div>
            <div style={{
              fontWeight: 600,
              fontSize: 14
            }}>{record.name}</div>
            <div style={{
              color: '#666',
              fontSize: 12
            }}>{record.id}</div>
          </div>
        </Space>
      ),
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: AgentType) => {
        const typeConfig = {
          [AgentType.ANALYSIS]: {
            color: 'blue',
            text: '分析型'
          },
          [AgentType.PREDICTION]: {
            color: 'green',
            text: '预测型'
          },
          [AgentType.DECISION]: {
            color: 'orange',
            text: '决策型'
          },
          [AgentType.MONITORING]: {
            color: 'purple',
            text: '监控型'
          }
        };
        const config = typeConfig[type];
        return <Tag color={config.color}>{config.text}</Tag>;
      },
      filters: [
        {
          text: '分析型',
          value: AgentType.ANALYSIS
        },
        {
          text: '预测型',
          value: AgentType.PREDICTION
        },
        {
          text: '决策型',
          value: AgentType.DECISION
        },
        {
          text: '监控型',
          value: AgentType.MONITORING
        }
      ],
      onFilter: (value, record) => record.type === value,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: AgentStatus) => {
        const statusConfig = {
          [AgentStatus.ACTIVE]: {
            color: 'success',
            text: '活跃',
            icon: <CheckCircleOutlined />
          },
          [AgentStatus.INACTIVE]: {
            color: 'default',
            text: '非活跃',
            icon: <CloseCircleOutlined />
          },
          [AgentStatus.ERROR]: {
            color: 'error',
            text: '错误',
            icon: <ExclamationCircleOutlined />
          }
        };
        const config = statusConfig[status];
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
          text: '活跃',
          value: AgentStatus.ACTIVE
        },
        {
          text: '非活跃',
          value: AgentStatus.INACTIVE
        },
        {
          text: '错误',
          value: AgentStatus.ERROR
        }
      ],
      onFilter: (value, record) => record.status === value,
    },
    {
      title: '能力',
      dataIndex: 'capabilities',
      key: 'capabilities',
      render: (capabilities: string[]) => (
        <Space wrap>
          {capabilities.slice(0, 3).map((cap, index) => (
            <Tag key={index} size="small">{cap.name}</Tag>
          ))}
          {capabilities.length > 3 && (
            <Tag size="small">+{capabilities.length - 3}</Tag>
          )}
        </Space>
      ),
    },
    {
      title: '注册时间',
      dataIndex: 'registeredAt',
      key: 'registeredAt',
      render: (date: string) => new Date(date).toLocaleString(),
      sorter: (a, b) => new Date(a.registeredAt).getTime() - new Date(b.registeredAt).getTime(),
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
          <Tooltip title="编辑">
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => handleEditAgent(record)}
            />
          </Tooltip>
          <Popconfirm
            title="确定要注销这个智能体吗？"
            description="注销后智能体将无法参与协作"
            onConfirm={() => handleUnregisterAgent(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Tooltip title="注销">
              <Button
                type="text"
                danger
                icon={<DeleteOutlined />}
              />
            </Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="agent-management">
      {/* 统计概览 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={8} md={6}>
          <Card size="small">
            <Statistic
              title="总数"
              value={agents.length}
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8} md={6}>
          <Card size="small">
            <Statistic
              title="活跃"
              value={activeCount}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8} md={6}>
          <Card size="small">
            <Statistic
              title="非活跃"
              value={inactiveCount}
              prefix={<CloseCircleOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8} md={6}>
          <Card size="small">
            <Statistic
              title="错误"
              value={errorCount}
              prefix={<ExclamationCircleOutlined />}
              valueStyle={{ color: '#ff4d4f' }}
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
                placeholder="搜索智能体名称或描述"
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
                <Option value={AgentStatus.ACTIVE}>活跃</Option>
                <Option value={AgentStatus.INACTIVE}>非活跃</Option>
                <Option value={AgentStatus.ERROR}>错误</Option>
              </Select>
              <Select
                value={typeFilter}
                onChange={setTypeFilter}
                style={{ width: 120 }}
              >
                <Option value="ALL">全部类型</Option>
                <Option value={AgentType.ANALYSIS}>分析型</Option>
                <Option value={AgentType.PREDICTION}>预测型</Option>
                <Option value={AgentType.DECISION}>决策型</Option>
                <Option value={AgentType.MONITORING}>监控型</Option>
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
                icon={<TeamOutlined />}
                onClick={() => setBatchRegisterVisible(true)}
              >
                批量注册
              </Button>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => setRegisterModalVisible(true)}
              >
                注册智能体
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 智能体列表 */}
      <Card>
        <Table
          columns={columns}
          dataSource={filteredAgents}
          rowKey="id"
          loading={loading}
          pagination={{
            total: filteredAgents.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) =>
              `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
          }}
          className="collaboration-table"
        />
      </Card>

      {/* 注册智能体模态框 */}
      <Modal
        title="注册智能体"
        open={registerModalVisible}
        onCancel={() => {
          setRegisterModalVisible(false);
          registerForm.resetFields();
        }}
        footer={null}
        width={600}
      >
        <Form
          form={registerForm}
          layout="vertical"
          onFinish={handleRegisterAgent}
        >
          <Form.Item
            name="name"
            label="智能体名称"
            rules={[{
              required: true,
              message: '请输入智能体名称'
            }]}
          >
            <Input placeholder="请输入智能体名称" />
          </Form.Item>

          <Form.Item
            name="type"
            label="智能体类型"
            rules={[{
              required: true,
              message: '请选择智能体类型'
            }]}
          >
            <Select placeholder="请选择智能体类型">
              <Option value={AgentType.FUNDAMENTAL_ANALYST}>基本面分析师</Option>
              <Option value={AgentType.TECHNICAL_ANALYST}>技术分析师</Option>
              <Option value={AgentType.MARKET_ANALYST}>市场分析师</Option>
              <Option value={AgentType.RISK_MANAGER}>风险管理师</Option>
              <Option value={AgentType.SENTIMENT_ANALYST}>情绪分析师</Option>
              <Option value={AgentType.NEWS_ANALYST}>新闻分析师</Option>
              <Option value={AgentType.QUANTITATIVE_ANALYST}>量化分析师</Option>
              <Option value={AgentType.PORTFOLIO_MANAGER}>投资组合管理师</Option>
              <Option value={AgentType.MACRO_ECONOMIST}>宏观经济学家</Option>
              <Option value={AgentType.INDUSTRY_EXPERT}>行业专家</Option>
              <Option value={AgentType.RESEARCH_ANALYST}>研究分析师</Option>
              <Option value={AgentType.TRADING_EXECUTOR}>交易执行师</Option>
              <Option value={AgentType.ADVISOR}>投资顾问</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="endpoint"
            label="服务端点"
            rules={[
              {
                required: true,
                message: '请输入服务端点'
              },
              {
                type: 'url',
                message: '请输入有效的URL'
              }
            ]}
          >
            <Input placeholder="http://localhost:8080/api/agent" />
          </Form.Item>

          <Form.Item
            name="description"
            label="描述"
            rules={[{
              required: true,
              message: '请输入描述'
            }]}
          >
            <TextArea
              rows={3}
              placeholder="请输入智能体的功能描述"
            />
          </Form.Item>

          <Form.Item
            name="capabilities"
            label="能力标签"
            tooltip="用逗号分隔多个能力标签"
          >
            <Input placeholder="数据分析,趋势预测,风险评估" />
          </Form.Item>

          <Form.Item style={{
            marginBottom: 0,
            textAlign: 'right'
          }}>
            <Space>
              <Button onClick={() => {
                setRegisterModalVisible(false);
                registerForm.resetFields();
              }}>
                取消
              </Button>
              <Button type="primary" htmlType="submit" loading={loading}>
                注册
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* 编辑智能体模态框 */}
      <Modal
        title="编辑智能体"
        open={editModalVisible}
        onCancel={() => {
          setEditModalVisible(false);
          editForm.resetFields();
        }}
        footer={null}
        width={600}
      >
        <Form
          form={editForm}
          layout="vertical"
          onFinish={(values) => {
            if (selectedAgent) {
              handleUpdateAgent(selectedAgent.id, values);
            }
          }}
        >
          <Form.Item
            name="name"
            label="智能体名称"
            rules={[{
              required: true,
              message: '请输入智能体名称'
            }]}
          >
            <Input placeholder="请输入智能体名称" />
          </Form.Item>

          <Form.Item
            name="endpoint"
            label="服务端点"
            rules={[
              {
                required: true,
                message: '请输入服务端点'
              },
              {
                type: 'url',
                message: '请输入有效的URL'
              }
            ]}
          >
            <Input placeholder="http://localhost:8080/api/agent" />
          </Form.Item>

          <Form.Item
            name="description"
            label="描述"
            rules={[{
              required: true,
              message: '请输入描述'
            }]}
          >
            <TextArea
              rows={3}
              placeholder="请输入智能体的功能描述"
            />
          </Form.Item>

          <Form.Item style={{
            marginBottom: 0,
            textAlign: 'right'
          }}>
            <Space>
              <Button onClick={() => {
                setEditModalVisible(false);
                editForm.resetFields();
              }}>
                取消
              </Button>
              <Button type="primary" htmlType="submit" loading={loading}>
                更新
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* 智能体详情抽屉 */}
      <Drawer
        title="智能体详情"
        placement="right"
        onClose={() => setDetailDrawerVisible(false)}
        open={detailDrawerVisible}
        width={600}
      >
        {selectedAgent && (
          <div>
            <Descriptions title="基本信息" bordered column={1}>
              <Descriptions.Item label="名称">{selectedAgent.name}</Descriptions.Item>
              <Descriptions.Item label="ID">{selectedAgent.id}</Descriptions.Item>
              <Descriptions.Item label="类型">
                <Tag color="blue">{selectedAgent.type}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                <Badge
                  status={selectedAgent.status === AgentStatus.ACTIVE ? 'success' : 'error'}
                  text={selectedAgent.status}
                />
              </Descriptions.Item>
              <Descriptions.Item label="服务端点">{selectedAgent.endpoint}</Descriptions.Item>
              <Descriptions.Item label="描述">{selectedAgent.description}</Descriptions.Item>
              <Descriptions.Item label="注册时间">
                {new Date(selectedAgent.registeredAt).toLocaleString()}
              </Descriptions.Item>
              <Descriptions.Item label="最后活跃">
                {selectedAgent.lastActiveAt ?
                  new Date(selectedAgent.lastActiveAt).toLocaleString() :
                  '从未活跃'
                }
              </Descriptions.Item>
            </Descriptions>

            <Divider />

            <Title level={4}>能力列表</Title>
            <Space wrap>
              {selectedAgent.capabilities.map((cap, index) => (
                <Tag key={index} color="blue">
                  {cap.name}
                  {cap.confidence && (
                    <span style={{ marginLeft: 4 }}>({(cap.confidence * 100).toFixed(0)}%)</span>
                  )}
                </Tag>
              ))}
            </Space>

            {selectedAgent.metadata && (
              <>
                <Divider />
                <Title level={4}>元数据</Title>
                <pre style={{
                  background: '#f5f5f5',
                  padding: 16,
                  borderRadius: 4,
                  fontSize: 12
                }}>
                  {JSON.stringify(selectedAgent.metadata, null, 2)}
                </pre>
              </>
            )}
          </div>
        )}
      </Drawer>

      {/* 批量注册模态框 */}
      <Modal
        title="批量注册智能体"
        open={batchRegisterVisible}
        onCancel={() => {
          setBatchRegisterVisible(false);
          batchForm.resetFields();
        }}
        footer={null}
        width={800}
      >
        <Alert
          message="批量注册说明"
          description="请按照JSON格式输入智能体信息，每个智能体包含name、type、endpoint、description等字段。"
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
        />

        <Form
          form={batchForm}
          layout="vertical"
          onFinish={handleBatchRegister}
        >
          <Form.Item
            name="agentsJson"
            label="智能体配置 (JSON格式)"
            rules={[{
              required: true,
              message: '请输入智能体配置'
            }]}
          >
            <TextArea
              rows={12}
              placeholder={`[
  {
    "name": "分析智能体1",
    "type": "ANALYSIS",
    "endpoint": "http://localhost:8081/api/agent",
    "description": "专门进行股票技术分析的智能体",
    "capabilities": "技术分析,K线分析,指标计算"
  },
  {
    "name": "预测智能体1",
    "type": "PREDICTION",
    "endpoint": "http://localhost:8082/api/agent",
    "description": "基于机器学习的股价预测智能体",
    "capabilities": "价格预测,趋势分析,风险评估"
  }
]`}
            />
          </Form.Item>

          <Form.Item style={{
            marginBottom: 0,
            textAlign: 'right'
          }}>
            <Space>
              <Button onClick={() => {
                setBatchRegisterVisible(false);
                batchForm.resetFields();
              }}>
                取消
              </Button>
              <Button type="primary" htmlType="submit" loading={loading}>
                批量注册
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AgentManagement;