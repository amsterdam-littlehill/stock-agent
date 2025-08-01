/**
 * 快速协作组件
 * @author Stock-Agent Team
 * @version 1.0
 */

import React, { useState, useCallback } from 'react';
import {
  Form,
  Input,
  Select,
  Button,
  Space,
  Card,
  Row,
  Col,
  Tag,
  Avatar,

  Typography,
  Divider,
  Alert,
  Steps,

  Switch,
  Slider,
  InputNumber,
  Radio,
  Checkbox,
  message
} from 'antd';
import {
  ThunderboltOutlined,
  RobotOutlined,
  SettingOutlined,
  PlayCircleOutlined,
  TeamOutlined,

  StarOutlined,
  BulbOutlined,
  BulbOutlined
} from '@ant-design/icons';

import {
  AgentInfo,
  CollaborationMode,

  QuickCollaborationRequest
} from '../../types/collaboration';
// import { CollaborationService } from '../../services/collaboration';

const { Option } = Select;
const { TextArea } = Input;
const { Text, Title } = Typography;
const { Step } = Steps;
const { Group: RadioGroup } = Radio;
const { Group: CheckboxGroup } = Checkbox;

interface QuickCollaborationProps {
  agents: AgentInfo[];
  onSubmit: (request: QuickCollaborationRequest) => void;
  onCancel: () => void;
}

/**
 * 快速协作组件
 */
const QuickCollaboration: React.FC<QuickCollaborationProps> = ({
  agents,
  onSubmit,
  onCancel
}) => {
  // 状态管理
  const [currentStep, setCurrentStep] = useState(0);
  const [loading] = useState(false);
  const [form] = Form.useForm();

  // 表单数据
  const [selectedMode, setSelectedMode] = useState<CollaborationMode | null>(null);
  const [selectedAgents, setSelectedAgents] = useState<string[]>([]);
  const [collaborationTopic, setCollaborationTopic] = useState('');
  const [collaborationDescription, setCollaborationDescription] = useState('');
  const [advancedSettings, setAdvancedSettings] = useState({
    priority: 'MEDIUM' as 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT',
    timeout: 30,
    autoStart: true,
    enableNotifications: true,
    maxIterations: 10,
    confidenceThreshold: 0.8,
    consensusThreshold: 0.7
  });

  // 预设模板
  const [templates] = useState([
    {
      id: 'stock_analysis',
      name: '股票分析',
      description: '对指定股票进行全面分析',
      mode: CollaborationMode.PARALLEL_ANALYSIS,
      topic: '股票投资价值分析',
      agentTypes: ['ANALYSIS', 'PREDICTION'],
      parameters: {
        stockSymbol: 'AAPL',
        timeRange: '1M',
        analysisDepth: 'detailed'
      }
    },
    {
      id: 'market_debate',
      name: '市场辩论',
      description: '对市场趋势进行辩论分析',
      mode: CollaborationMode.STRUCTURED_DEBATE,
      topic: '市场趋势辩论',
      agentTypes: ['ANALYSIS', 'PREDICTION', 'DECISION'],
      parameters: {
        debateRounds: 3,
        timePerRound: 5
      }
    },
    {
      id: 'risk_assessment',
      name: '风险评估',
      description: '进行投资风险评估',
      mode: CollaborationMode.SEQUENTIAL_PIPELINE,
      topic: '投资风险评估',
      agentTypes: ['ANALYSIS', 'MONITORING', 'DECISION'],
      parameters: {
        riskLevel: 'moderate',
        timeHorizon: '1Y'
      }
    },
    {
      id: 'consensus_strategy',
      name: '策略共识',
      description: '达成投资策略共识',
      mode: CollaborationMode.CONSENSUS_BUILDING,
      topic: '投资策略共识',
      agentTypes: ['ANALYSIS', 'PREDICTION', 'DECISION'],
      parameters: {
        consensusTarget: 0.8,
        maxRounds: 5
      }
    }
  ]);

  // 获取可用智能体（按类型分组）
  const availableAgents = agents.filter(agent => agent.status === 'ACTIVE');
  const agentsByType = availableAgents.reduce((acc, agent) => {
    if (!acc[agent.type]) {
      acc[agent.type] = [];
    }
    acc[agent.type].push(agent);
    return acc;
  }, {} as Record<string, AgentInfo[]>);

  /**
   * 应用模板
   */
  const applyTemplate = useCallback((template: any) => {
    setSelectedMode(template.mode);
    setCollaborationTopic(template.topic);
    setCollaborationDescription(template.description);

    // 自动选择对应类型的智能体
    const recommendedAgents = template.agentTypes.map((type: string) => {
      const agentsOfType = agentsByType[type] || [];
      return agentsOfType.length > 0 ? agentsOfType[0].id : null;
    }).filter(Boolean);

    setSelectedAgents(recommendedAgents);

    form.setFieldsValue({
      mode: template.mode,
      topic: template.topic,
      description: template.description,
      participantIds: recommendedAgents,
      parameters: JSON.stringify(template.parameters, null, 2)
    });

    message.success(`已应用模板: ${template.name}`);
  }, [agentsByType, form]);

  /**
   * 智能推荐智能体
   */
  const recommendAgents = useCallback((mode: CollaborationMode) => {
    const recommendations = {
      [CollaborationMode.STRUCTURED_DEBATE]: ['ANALYSIS', 'PREDICTION', 'DECISION'],
      [CollaborationMode.PARALLEL_ANALYSIS]: ['ANALYSIS', 'PREDICTION'],
      [CollaborationMode.SEQUENTIAL_PIPELINE]: ['ANALYSIS', 'MONITORING', 'DECISION'],
      [CollaborationMode.CONSENSUS_BUILDING]: ['ANALYSIS', 'PREDICTION', 'DECISION']
    };

    const recommendedTypes = recommendations[mode] || [];
    const recommendedAgents = recommendedTypes.map(type => {
      const agentsOfType = agentsByType[type] || [];
      return agentsOfType.length > 0 ? agentsOfType[0].id : null;
    }).filter(Boolean);

    setSelectedAgents(recommendedAgents);
    form.setFieldValue('participantIds', recommendedAgents);
  }, [agentsByType, form]);

  /**
   * 下一步
   */
  const handleNext = useCallback(() => {
    if (currentStep < 2) {
      setCurrentStep(currentStep + 1);
    }
  }, [currentStep]);

  /**
   * 上一步
   */
  const handlePrev = useCallback(() => {
    if (currentStep > 0) {
      setCurrentStep(currentStep - 1);
    }
  }, [currentStep]);

  /**
   * 提交协作请求
   */
  const handleSubmit = useCallback(async () => {
    try {
      const values = await form.validateFields();

      const request: QuickCollaborationRequest = {
        topic: values.topic,
        mode: values.mode,
        participantIds: values.participantIds,
        description: values.description,
        priority: advancedSettings.priority,
        timeout: advancedSettings.timeout,
        autoStart: advancedSettings.autoStart,
        parameters: values.parameters ? JSON.parse(values.parameters) : undefined,
        settings: {
          maxIterations: advancedSettings.maxIterations,
          confidenceThreshold: advancedSettings.confidenceThreshold,
          consensusThreshold: advancedSettings.consensusThreshold,
          enableNotifications: advancedSettings.enableNotifications
        }
      };

      onSubmit(request);
    } catch (error) {
      console.error('Form validation failed:', error);
      message.error('请检查表单输入');
    }
  }, [form, advancedSettings, onSubmit]);

  // 协作模式配置
  const modeConfigs = {
    [CollaborationMode.STRUCTURED_DEBATE]: {
      title: '结构化辩论',
      description: '智能体通过结构化的辩论形式交换观点，适合需要多角度分析的场景',
      icon: '🗣️',
      color: '#667eea',
      features: ['多轮辩论', '观点对抗', '逻辑推理', '结论综合'],
      bestFor: '市场趋势分析、投资决策辩论'
    },
    [CollaborationMode.PARALLEL_ANALYSIS]: {
      title: '并行分析',
      description: '多个智能体同时分析不同方面，然后汇总结果，适合需要快速全面分析的场景',
      icon: '📊',
      color: '#f093fb',
      features: ['并行处理', '快速响应', '全面覆盖', '结果汇总'],
      bestFor: '股票技术分析、财务数据分析'
    },
    [CollaborationMode.SEQUENTIAL_PIPELINE]: {
      title: '顺序流水线',
      description: '智能体按照预定顺序依次处理任务，适合有明确流程的分析场景',
      icon: '🔄',
      color: '#4facfe',
      features: ['流程化', '步骤清晰', '质量控制', '可追溯'],
      bestFor: '风险评估流程、投资决策流程'
    },
    [CollaborationMode.CONSENSUS_BUILDING]: {
      title: '共识构建',
      description: '智能体通过协商和妥协达成共识，适合需要统一意见的决策场景',
      icon: '🤝',
      color: '#43e97b',
      features: ['协商机制', '共识达成', '冲突解决', '统一决策'],
      bestFor: '投资策略制定、风险控制策略'
    }
  };

  // 步骤内容
  const stepContents = [
    // 第一步：选择模式和模板
    (
      <div>
        <Title level={4}>选择协作模式</Title>
        <Row gutter={[16, 16]}>
          {Object.entries(modeConfigs).map(([mode, config]) => (
            <Col xs={24} sm={12} key={mode}>
              <Card
                hoverable
                className={`mode-card ${selectedMode === mode ? 'selected' : ''}`}
                onClick={() => {
                  setSelectedMode(mode as CollaborationMode);
                  form.setFieldValue('mode', mode);
                  recommendAgents(mode as CollaborationMode);
                }}
                style={{border: selectedMode === mode ? `2px solid ${config.color}` : '1px solid #d9d9d9'}}
              >
                <div style={{ textAlign: 'center' }}>
                  <div style={{
                    fontSize: 32,
                    marginBottom: 8
                  }}>{config.icon}</div>
                  <Title level={5}>{config.title}</Title>
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    {config.description}
                  </Text>
                  <div style={{ marginTop: 12 }}>
                    <Space wrap>
                      {config.features.map((feature, index) => (
                        <Tag key={index} size="small">{feature}</Tag>
                      ))}
                    </Space>
                  </div>
                  <div style={{ marginTop: 8 }}>
                    <Text style={{
                      fontSize: 11,
                      color: config.color
                    }}>
                      适用于: {config.bestFor}
                    </Text>
                  </div>
                </div>
              </Card>
            </Col>
          ))}
        </Row>

        <Divider />

        <Title level={4}>快速模板</Title>
        <Row gutter={[16, 16]}>
          {templates.map((template) => (
            <Col xs={24} sm={12} md={6} key={template.id}>
              <Card
                size="small"
                hoverable
                onClick={() => applyTemplate(template)}
                actions={[
                  <Button
                    type="link"
                    size="small"
                    icon={<ThunderboltOutlined />}
                    onClick={(e) => {
                      e.stopPropagation();
                      applyTemplate(template);
                    }}
                  >
                    应用
                  </Button>
                ]}
              >
                <Card.Meta
                  avatar={<Avatar icon={<BulbOutlined />} />}
                  title={template.name}
                  description={template.description}
                />
              </Card>
            </Col>
          ))}
        </Row>
      </div>
    ),

    // 第二步：配置协作参数
    (
      <div>
        <Form form={form} layout="vertical">
          <Row gutter={[16, 16]}>
            <Col xs={24} md={12}>
              <Form.Item
                name="topic"
                label="协作主题"
                rules={[{
                  required: true,
                  message: '请输入协作主题'
                }]}
              >
                <Input
                  placeholder="请输入协作主题"
                  value={collaborationTopic}
                  onChange={(e) => setCollaborationTopic(e.target.value)}
                />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item
                name="mode"
                label="协作模式"
                rules={[{
                  required: true,
                  message: '请选择协作模式'
                }]}
              >
                <Select
                  placeholder="请选择协作模式"
                  value={selectedMode}
                  onChange={(value) => {
                    setSelectedMode(value);
                    recommendAgents(value);
                  }}
                >
                  {Object.entries(modeConfigs).map(([mode, config]) => (
                    <Option key={mode} value={mode}>
                      <Space>
                        <span>{config.icon}</span>
                        {config.title}
                      </Space>
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
          </Row>

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
              value={collaborationDescription}
              onChange={(e) => setCollaborationDescription(e.target.value)}
            />
          </Form.Item>

          <Form.Item
            name="participantIds"
            label="参与智能体"
            rules={[{
              required: true,
              message: '请选择参与智能体'
            }]}
          >
            <div>
              <div style={{ marginBottom: 16 }}>
                <Space>
                  <Text>智能推荐:</Text>
                  <Button
                    size="small"
                    type="dashed"
                    onClick={() => selectedMode && recommendAgents(selectedMode)}
                    disabled={!selectedMode}
                  >
                    根据模式推荐
                  </Button>
                </Space>
              </div>

              {Object.entries(agentsByType).map(([type, typeAgents]) => (
                <div key={type} style={{ marginBottom: 16 }}>
                  <Text strong>{type} 类型智能体:</Text>
                  <div style={{ marginTop: 8 }}>
                    <CheckboxGroup
                      value={selectedAgents}
                      onChange={setSelectedAgents}
                    >
                      <Row gutter={[8, 8]}>
                        {typeAgents.map((agent) => (
                          <Col key={agent.id}>
                            <Checkbox value={agent.id}>
                              <Space>
                                <Avatar size="small" icon={<RobotOutlined />} />
                                <div>
                                  <div style={{
                                    fontSize: 12,
                                    fontWeight: 600
                                  }}>
                                    {agent.name}
                                  </div>
                                  <div style={{
                                    fontSize: 10,
                                    color: '#666'
                                  }}>
                                    {agent.capabilities.slice(0, 2).map(cap => cap.name).join(', ')}
                                  </div>
                                </div>
                              </Space>
                            </Checkbox>
                          </Col>
                        ))}
                      </Row>
                    </CheckboxGroup>
                  </div>
                </div>
              ))}
            </div>
          </Form.Item>

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
        </Form>
      </div>
    ),

    // 第三步：高级设置
    (
      <div>
        <Title level={4}>高级设置</Title>

        <Row gutter={[24, 24]}>
          <Col xs={24} md={12}>
            <Card title="基本设置" size="small">
              <Space direction="vertical" style={{ width: '100%' }}>
                <div>
                  <Text>优先级:</Text>
                  <RadioGroup
                    value={advancedSettings.priority}
                    onChange={(e) => setAdvancedSettings(prev => ({
                      ...prev,
                      priority: e.target.value
                    }))}
                    style={{ marginLeft: 16 }}
                  >
                    <Radio value="LOW">低</Radio>
                    <Radio value="MEDIUM">中</Radio>
                    <Radio value="HIGH">高</Radio>
                    <Radio value="URGENT">紧急</Radio>
                  </RadioGroup>
                </div>

                <div>
                  <Text>超时时间 (分钟):</Text>
                  <InputNumber
                    value={advancedSettings.timeout}
                    onChange={(value) => setAdvancedSettings(prev => ({
                      ...prev,
                      timeout: value || 30
                    }))}
                    min={1}
                    max={1440}
                    style={{
                      marginLeft: 16,
                      width: 100
                    }}
                  />
                </div>

                <div>
                  <Text>自动启动:</Text>
                  <Switch
                    checked={advancedSettings.autoStart}
                    onChange={(checked) => setAdvancedSettings(prev => ({
                      ...prev,
                      autoStart: checked
                    }))}
                    style={{ marginLeft: 16 }}
                  />
                </div>

                <div>
                  <Text>启用通知:</Text>
                  <Switch
                    checked={advancedSettings.enableNotifications}
                    onChange={(checked) => setAdvancedSettings(prev => ({
                      ...prev,
                      enableNotifications: checked
                    }))}
                    style={{ marginLeft: 16 }}
                  />
                </div>
              </Space>
            </Card>
          </Col>

          <Col xs={24} md={12}>
            <Card title="协作参数" size="small">
              <Space direction="vertical" style={{ width: '100%' }}>
                <div>
                  <Text>最大迭代次数:</Text>
                  <Slider
                    value={advancedSettings.maxIterations}
                    onChange={(value) => setAdvancedSettings(prev => ({
                      ...prev,
                      maxIterations: value
                    }))}
                    min={1}
                    max={20}
                    marks={{
                      1: '1',
                      10: '10',
                      20: '20'
                    }}
                    style={{ marginTop: 16 }}
                  />
                </div>

                <div>
                  <Text>置信度阈值:</Text>
                  <Slider
                    value={advancedSettings.confidenceThreshold}
                    onChange={(value) => setAdvancedSettings(prev => ({
                      ...prev,
                      confidenceThreshold: value
                    }))}
                    min={0.1}
                    max={1.0}
                    step={0.1}
                    marks={{
                      0.1: '10%',
                      0.5: '50%',
                      1.0: '100%'
                    }}
                    style={{ marginTop: 16 }}
                  />
                </div>

                <div>
                  <Text>共识阈值:</Text>
                  <Slider
                    value={advancedSettings.consensusThreshold}
                    onChange={(value) => setAdvancedSettings(prev => ({
                      ...prev,
                      consensusThreshold: value
                    }))}
                    min={0.1}
                    max={1.0}
                    step={0.1}
                    marks={{
                      0.1: '10%',
                      0.5: '50%',
                      1.0: '100%'
                    }}
                    style={{ marginTop: 16 }}
                  />
                </div>
              </Space>
            </Card>
          </Col>
        </Row>

        <Divider />

        <Alert
          message="配置预览"
          description={
            <div>
              <Text>协作主题: {collaborationTopic || '未设置'}</Text><br/>
              <Text>协作模式: {selectedMode ? modeConfigs[selectedMode].title : '未选择'}</Text><br/>
              <Text>参与智能体: {selectedAgents.length} 个</Text><br/>
              <Text>优先级: {advancedSettings.priority}</Text><br/>
              <Text>超时时间: {advancedSettings.timeout} 分钟</Text>
            </div>
          }
          type="info"
          showIcon
        />
      </div>
    )
  ];

  return (
    <div className="quick-collaboration">
      <div style={{ marginBottom: 24 }}>
        <Steps current={currentStep} size="small">
          <Step title="选择模式" icon={<SettingOutlined />} />
          <Step title="配置参数" icon={<TeamOutlined />} />
          <Step title="高级设置" icon={<StarOutlined />} />
        </Steps>
      </div>

      <div style={{ minHeight: 400 }}>
        {stepContents[currentStep]}
      </div>

      <Divider />

      <div style={{ textAlign: 'right' }}>
        <Space>
          <Button onClick={onCancel}>
            取消
          </Button>
          {currentStep > 0 && (
            <Button onClick={handlePrev}>
              上一步
            </Button>
          )}
          {currentStep < 2 ? (
            <Button
              type="primary"
              onClick={handleNext}
              disabled={currentStep === 0 && !selectedMode}
            >
              下一步
            </Button>
          ) : (
            <Button
              type="primary"
              icon={<PlayCircleOutlined />}
              onClick={handleSubmit}
              loading={loading}
              disabled={!selectedMode || selectedAgents.length === 0}
            >
              启动协作
            </Button>
          )}
        </Space>
      </div>
    </div>
  );
};

export default QuickCollaboration;