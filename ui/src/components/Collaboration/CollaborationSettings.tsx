/**
 * 协作设置组件
 * @author Stock-Agent Team
 * @version 1.0
 */

import React, { useState, useCallback, useEffect } from 'react';
import {
  Card,
  Form,
  Input,
  InputNumber,
  Switch,
  Select,
  Slider,
  Button,
  Space,

  Row,
  Col,
  Typography,
  Alert,
  Tabs,
  List,

  Modal,
  message,
  Popconfirm,

  Badge,
  Progress,
  Collapse
} from 'antd';
import {
  SettingOutlined,
  SaveOutlined,
  ReloadOutlined,
  DeleteOutlined,
  ExportOutlined,
  ImportOutlined,

  ClockCircleOutlined,
  ThunderboltOutlined,
  TeamOutlined,
  BellOutlined,
  SecurityScanOutlined,
  DatabaseOutlined,
  SecurityScanOutlined
} from '@ant-design/icons';

interface CollaborationSettingsProps {
  onSettingsChange: (settings: any) => void;
  onClearCache: () => void;
}

const { Title, Text } = Typography;
const { TabPane } = Tabs;
const { Option } = Select;
// const { TextArea } = Input;
const { Panel } = Collapse;

/**
 * 协作设置组件
 */
const CollaborationSettings: React.FC<CollaborationSettingsProps> = ({
  onSettingsChange,
  onClearCache
}) => {
  // 表单实例
  const [engineForm] = Form.useForm();
  const [agentForm] = Form.useForm();
  const [sessionForm] = Form.useForm();
  const [notificationForm] = Form.useForm();
  const [securityForm] = Form.useForm();

  // 状态管理
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('engine');
  const [hasChanges, setHasChanges] = useState(false);
  const [exportModalVisible, setExportModalVisible] = useState(false);
  const [importModalVisible, setImportModalVisible] = useState(false);
  // const [resetModalVisible] = useState(false);

  // 设置数据
  const [engineSettings, setEngineSettings] = useState({
    maxConcurrentSessions: 10,
    defaultTimeout: 30,
    enableAutoRetry: true,
    retryAttempts: 3,
    retryDelay: 5,
    enableMetrics: true,
    metricsInterval: 60,
    enableLogging: true,
    logLevel: 'INFO',
    enableDebug: false
  });

  const [agentSettings, setAgentSettings] = useState({
    maxAgentsPerSession: 8,
    agentTimeout: 60,
    enableHealthCheck: true,
    healthCheckInterval: 30,
    enableLoadBalancing: true,
    loadBalancingStrategy: 'ROUND_ROBIN',
    enableAgentPool: true,
    poolSize: 20,
    enableCapabilityMatching: true
  });

  const [sessionSettings, setSessionSettings] = useState({
    defaultMode: 'PARALLEL_ANALYSIS',
    maxIterations: 10,
    confidenceThreshold: 0.8,
    consensusThreshold: 0.7,
    enableSessionPersistence: true,
    sessionTTL: 24,
    enableResultCaching: true,
    cacheExpiry: 60,
    enableSessionRecording: true
  });

  const [notificationSettings, setNotificationSettings] = useState({
    enableEmailNotifications: true,
    enableWebhookNotifications: false,
    enableInAppNotifications: true,
    emailRecipients: ['admin@example.com'],
    webhookUrl: '',
    notificationLevels: ['ERROR', 'WARNING'],
    enableSessionNotifications: true,
    enableAgentNotifications: true,
    enableSystemNotifications: true
  });

  const [securitySettings, setSecuritySettings] = useState({
    enableAuthentication: true,
    enableAuthorization: true,
    enableRateLimit: true,
    rateLimitRequests: 100,
    rateLimitWindow: 60,
    enableIPWhitelist: false,
    ipWhitelist: [],
    enableAuditLog: true,
    auditLogRetention: 90,
    enableEncryption: true
  });

  /**
   * 保存设置
   */
  const handleSaveSettings = useCallback(async () => {
    try {
      setLoading(true);

      // 验证所有表单
      await Promise.all([
        engineForm.validateFields(),
        agentForm.validateFields(),
        sessionForm.validateFields(),
        notificationForm.validateFields(),
        securityForm.validateFields()
      ]);

      const allSettings = {
        engine: engineSettings,
        agent: agentSettings,
        session: sessionSettings,
        notification: notificationSettings,
        security: securitySettings
      };

      // 模拟保存到后端
      await new Promise(resolve => setTimeout(resolve, 1000));

      onSettingsChange(allSettings);
      setHasChanges(false);
      message.success('设置已保存');
    } catch (error) {
      console.error('Save settings failed:', error);
      message.error('保存设置失败');
    } finally {
      setLoading(false);
    }
  }, [engineSettings, agentSettings, sessionSettings, notificationSettings, securitySettings, onSettingsChange, engineForm, agentForm, sessionForm, notificationForm, securityForm]);

  /**
   * 重置设置
   */
  const handleResetSettings = useCallback(() => {
    // 重置为默认值
    setEngineSettings({
      maxConcurrentSessions: 10,
      defaultTimeout: 30,
      enableAutoRetry: true,
      retryAttempts: 3,
      retryDelay: 5,
      enableMetrics: true,
      metricsInterval: 60,
      enableLogging: true,
      logLevel: 'INFO',
      enableDebug: false
    });

    // 重置其他设置...
    setHasChanges(true);
    setResetModalVisible(false);
    message.success('设置已重置为默认值');
  }, []);

  /**
   * 导出设置
   */
  const handleExportSettings = useCallback(() => {
    const allSettings = {
      engine: engineSettings,
      agent: agentSettings,
      session: sessionSettings,
      notification: notificationSettings,
      security: securitySettings,
      exportTime: new Date().toISOString(),
      version: '1.0'
    };

    const blob = new Blob([JSON.stringify(allSettings, null, 2)], {type: 'application/json'});

    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `collaboration-settings-${new Date().toISOString().split('T')[0]}.json`;
    document.body.appendChild(a);
    a.click();
    URL.revokeObjectURL(url);
    document.body.removeChild(a);

    message.success('设置已导出');
    setExportModalVisible(false);
  }, [engineSettings, agentSettings, sessionSettings, notificationSettings, securitySettings]);

  /**
   * 导入设置
   */
  const handleImportSettings = useCallback((file: File) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const settings = JSON.parse(e.target?.result as string);

        if (settings.engine) setEngineSettings(settings.engine);
        if (settings.agent) setAgentSettings(settings.agent);
        if (settings.session) setSessionSettings(settings.session);
        if (settings.notification) setNotificationSettings(settings.notification);
        if (settings.security) setSecuritySettings(settings.security);

        setHasChanges(true);
        message.success('设置已导入');
        setImportModalVisible(false);
      } catch {
        message.error('导入设置失败，请检查文件格式');
      }
    };
    reader.readAsText(file);
  }, []);

  /**
   * 监听设置变化
   */
  useEffect(() => {
    // const handleFormChange = () => {
    //   setHasChanges(true);
    // };

    // 监听表单变化
    engineForm.getFieldsValue();
    agentForm.getFieldsValue();
    sessionForm.getFieldsValue();
    notificationForm.getFieldsValue();
    securityForm.getFieldsValue();
  }, [engineForm, agentForm, sessionForm, notificationForm, securityForm]);

  return (
    <div className="collaboration-settings">
      {/* 设置头部 */}
      <div style={{ marginBottom: 24 }}>
        <Row justify="space-between" align="middle">
          <Col>
            <Space>
              <SettingOutlined style={{
                fontSize: 20,
                color: '#1890ff'
              }} />
              <Title level={4} style={{ margin: 0 }}>协作系统设置</Title>
              {hasChanges && (
                <Badge status="warning" text="有未保存的更改" />
              )}
            </Space>
          </Col>
          <Col>
            <Space>
              <Button
                icon={<ExportOutlined />}
                onClick={() => setExportModalVisible(true)}
              >
                导出设置
              </Button>
              <Button
                icon={<ImportOutlined />}
                onClick={() => setImportModalVisible(true)}
              >
                导入设置
              </Button>
              <Popconfirm
                title="确定要重置所有设置吗？"
                description="这将恢复所有设置为默认值"
                onConfirm={handleResetSettings}
                okText="确定"
                cancelText="取消"
              >
                <Button icon={<ReloadOutlined />}>
                  重置设置
                </Button>
              </Popconfirm>
              <Button
                type="primary"
                icon={<SaveOutlined />}
                onClick={handleSaveSettings}
                loading={loading}
                disabled={!hasChanges}
              >
                保存设置
              </Button>
            </Space>
          </Col>
        </Row>
      </div>

      {/* 设置标签页 */}
      <Tabs activeKey={activeTab} onChange={setActiveTab} type="card">
        {/* 引擎设置 */}
        <TabPane
          tab={
            <span>
              <ThunderboltOutlined />
              引擎设置
            </span>
          }
          key="engine"
        >
          <Form
            form={engineForm}
            layout="vertical"
            initialValues={engineSettings}
            onValuesChange={(_, values) => {
              setEngineSettings(values);
              setHasChanges(true);
            }}
          >
            <Row gutter={[24, 16]}>
              <Col xs={24} md={12}>
                <Card title="基本配置" size="small">
                  <Form.Item
                    name="maxConcurrentSessions"
                    label="最大并发会话数"
                    tooltip="系统同时处理的最大协作会话数量"
                  >
                    <InputNumber min={1} max={100} style={{ width: '100%' }} />
                  </Form.Item>

                  <Form.Item
                    name="defaultTimeout"
                    label="默认超时时间 (分钟)"
                    tooltip="协作会话的默认超时时间"
                  >
                    <InputNumber min={1} max={1440} style={{ width: '100%' }} />
                  </Form.Item>

                  <Form.Item
                    name="enableAutoRetry"
                    label="启用自动重试"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>

                  <Form.Item
                    name="retryAttempts"
                    label="重试次数"
                    dependencies={['enableAutoRetry']}
                  >
                    <InputNumber
                      min={1}
                      max={10}
                      style={{ width: '100%' }}
                      disabled={!engineForm.getFieldValue('enableAutoRetry')}
                    />
                  </Form.Item>

                  <Form.Item
                    name="retryDelay"
                    label="重试延迟 (秒)"
                    dependencies={['enableAutoRetry']}
                  >
                    <InputNumber
                      min={1}
                      max={60}
                      style={{ width: '100%' }}
                      disabled={!engineForm.getFieldValue('enableAutoRetry')}
                    />
                  </Form.Item>
                </Card>
              </Col>

              <Col xs={24} md={12}>
                <Card title="监控和日志" size="small">
                  <Form.Item
                    name="enableMetrics"
                    label="启用性能指标"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>

                  <Form.Item
                    name="metricsInterval"
                    label="指标收集间隔 (秒)"
                    dependencies={['enableMetrics']}
                  >
                    <InputNumber
                      min={10}
                      max={3600}
                      style={{ width: '100%' }}
                      disabled={!engineForm.getFieldValue('enableMetrics')}
                    />
                  </Form.Item>

                  <Form.Item
                    name="enableLogging"
                    label="启用日志记录"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>

                  <Form.Item
                    name="logLevel"
                    label="日志级别"
                    dependencies={['enableLogging']}
                  >
                    <Select
                      style={{ width: '100%' }}
                      disabled={!engineForm.getFieldValue('enableLogging')}
                    >
                      <Option value="DEBUG">DEBUG</Option>
                      <Option value="INFO">INFO</Option>
                      <Option value="WARN">WARN</Option>
                      <Option value="ERROR">ERROR</Option>
                    </Select>
                  </Form.Item>

                  <Form.Item
                    name="enableDebug"
                    label="启用调试模式"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>
                </Card>
              </Col>
            </Row>
          </Form>
        </TabPane>

        {/* 智能体设置 */}
        <TabPane
          tab={
            <span>
              <TeamOutlined />
              智能体设置
            </span>
          }
          key="agent"
        >
          <Form
            form={agentForm}
            layout="vertical"
            initialValues={agentSettings}
            onValuesChange={(_, values) => {
              setAgentSettings(values);
              setHasChanges(true);
            }}
          >
            <Row gutter={[24, 16]}>
              <Col xs={24} md={12}>
                <Card title="智能体管理" size="small">
                  <Form.Item
                    name="maxAgentsPerSession"
                    label="每个会话最大智能体数"
                  >
                    <InputNumber min={1} max={20} style={{ width: '100%' }} />
                  </Form.Item>

                  <Form.Item
                    name="agentTimeout"
                    label="智能体响应超时 (秒)"
                  >
                    <InputNumber min={10} max={300} style={{ width: '100%' }} />
                  </Form.Item>

                  <Form.Item
                    name="enableHealthCheck"
                    label="启用健康检查"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>

                  <Form.Item
                    name="healthCheckInterval"
                    label="健康检查间隔 (秒)"
                    dependencies={['enableHealthCheck']}
                  >
                    <InputNumber
                      min={10}
                      max={300}
                      style={{ width: '100%' }}
                      disabled={!agentForm.getFieldValue('enableHealthCheck')}
                    />
                  </Form.Item>

                  <Form.Item
                    name="enableCapabilityMatching"
                    label="启用能力匹配"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>
                </Card>
              </Col>

              <Col xs={24} md={12}>
                <Card title="负载均衡" size="small">
                  <Form.Item
                    name="enableLoadBalancing"
                    label="启用负载均衡"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>

                  <Form.Item
                    name="loadBalancingStrategy"
                    label="负载均衡策略"
                    dependencies={['enableLoadBalancing']}
                  >
                    <Select
                      style={{ width: '100%' }}
                      disabled={!agentForm.getFieldValue('enableLoadBalancing')}
                    >
                      <Option value="ROUND_ROBIN">轮询</Option>
                      <Option value="LEAST_CONNECTIONS">最少连接</Option>
                      <Option value="WEIGHTED_ROUND_ROBIN">加权轮询</Option>
                      <Option value="RANDOM">随机</Option>
                    </Select>
                  </Form.Item>

                  <Form.Item
                    name="enableAgentPool"
                    label="启用智能体池"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>

                  <Form.Item
                    name="poolSize"
                    label="智能体池大小"
                    dependencies={['enableAgentPool']}
                  >
                    <InputNumber
                      min={5}
                      max={100}
                      style={{ width: '100%' }}
                      disabled={!agentForm.getFieldValue('enableAgentPool')}
                    />
                  </Form.Item>
                </Card>
              </Col>
            </Row>
          </Form>
        </TabPane>

        {/* 会话设置 */}
        <TabPane
          tab={
            <span>
              <ClockCircleOutlined />
              会话设置
            </span>
          }
          key="session"
        >
          <Form
            form={sessionForm}
            layout="vertical"
            initialValues={sessionSettings}
            onValuesChange={(_, values) => {
              setSessionSettings(values);
              setHasChanges(true);
            }}
          >
            <Row gutter={[24, 16]}>
              <Col xs={24} md={12}>
                <Card title="会话配置" size="small">
                  <Form.Item
                    name="defaultMode"
                    label="默认协作模式"
                  >
                    <Select style={{ width: '100%' }}>
                      <Option value="STRUCTURED_DEBATE">结构化辩论</Option>
                      <Option value="PARALLEL_ANALYSIS">并行分析</Option>
                      <Option value="SEQUENTIAL_PIPELINE">顺序流水线</Option>
                      <Option value="CONSENSUS_BUILDING">共识构建</Option>
                    </Select>
                  </Form.Item>

                  <Form.Item
                    name="maxIterations"
                    label="最大迭代次数"
                  >
                    <Slider
                      min={1}
                      max={20}
                      marks={{
                        1: '1',
                        10: '10',
                        20: '20'
                      }}
                    />
                  </Form.Item>

                  <Form.Item
                    name="confidenceThreshold"
                    label="置信度阈值"
                  >
                    <Slider
                      min={0.1}
                      max={1.0}
                      step={0.1}
                      marks={{
                        0.1: '10%',
                        0.5: '50%',
                        1.0: '100%'
                      }}
                    />
                  </Form.Item>

                  <Form.Item
                    name="consensusThreshold"
                    label="共识阈值"
                  >
                    <Slider
                      min={0.1}
                      max={1.0}
                      step={0.1}
                      marks={{
                        0.1: '10%',
                        0.5: '50%',
                        1.0: '100%'
                      }}
                    />
                  </Form.Item>
                </Card>
              </Col>

              <Col xs={24} md={12}>
                <Card title="持久化和缓存" size="small">
                  <Form.Item
                    name="enableSessionPersistence"
                    label="启用会话持久化"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>

                  <Form.Item
                    name="sessionTTL"
                    label="会话生存时间 (小时)"
                    dependencies={['enableSessionPersistence']}
                  >
                    <InputNumber
                      min={1}
                      max={168}
                      style={{ width: '100%' }}
                      disabled={!sessionForm.getFieldValue('enableSessionPersistence')}
                    />
                  </Form.Item>

                  <Form.Item
                    name="enableResultCaching"
                    label="启用结果缓存"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>

                  <Form.Item
                    name="cacheExpiry"
                    label="缓存过期时间 (分钟)"
                    dependencies={['enableResultCaching']}
                  >
                    <InputNumber
                      min={5}
                      max={1440}
                      style={{ width: '100%' }}
                      disabled={!sessionForm.getFieldValue('enableResultCaching')}
                    />
                  </Form.Item>

                  <Form.Item
                    name="enableSessionRecording"
                    label="启用会话录制"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>
                </Card>
              </Col>
            </Row>
          </Form>
        </TabPane>

        {/* 通知设置 */}
        <TabPane
          tab={
            <span>
              <BellOutlined />
              通知设置
            </span>
          }
          key="notification"
        >
          <Form
            form={notificationForm}
            layout="vertical"
            initialValues={notificationSettings}
            onValuesChange={(_, values) => {
              setNotificationSettings(values);
              setHasChanges(true);
            }}
          >
            <Row gutter={[24, 16]}>
              <Col xs={24} md={12}>
                <Card title="通知渠道" size="small">
                  <Form.Item
                    name="enableEmailNotifications"
                    label="启用邮件通知"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>

                  <Form.Item
                    name="emailRecipients"
                    label="邮件接收者"
                    dependencies={['enableEmailNotifications']}
                  >
                    <Select
                      mode="tags"
                      style={{ width: '100%' }}
                      placeholder="输入邮箱地址"
                      disabled={!notificationForm.getFieldValue('enableEmailNotifications')}
                    />
                  </Form.Item>

                  <Form.Item
                    name="enableWebhookNotifications"
                    label="启用Webhook通知"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>

                  <Form.Item
                    name="webhookUrl"
                    label="Webhook URL"
                    dependencies={['enableWebhookNotifications']}
                  >
                    <Input
                      placeholder="https://example.com/webhook"
                      disabled={!notificationForm.getFieldValue('enableWebhookNotifications')}
                    />
                  </Form.Item>

                  <Form.Item
                    name="enableInAppNotifications"
                    label="启用应用内通知"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>
                </Card>
              </Col>

              <Col xs={24} md={12}>
                <Card title="通知级别" size="small">
                  <Form.Item
                    name="notificationLevels"
                    label="通知级别"
                  >
                    <Select mode="multiple" style={{ width: '100%' }}>
                      <Option value="INFO">信息</Option>
                      <Option value="WARNING">警告</Option>
                      <Option value="ERROR">错误</Option>
                      <Option value="CRITICAL">严重</Option>
                    </Select>
                  </Form.Item>

                  <Form.Item
                    name="enableSessionNotifications"
                    label="会话通知"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>

                  <Form.Item
                    name="enableAgentNotifications"
                    label="智能体通知"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>

                  <Form.Item
                    name="enableSystemNotifications"
                    label="系统通知"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>
                </Card>
              </Col>
            </Row>
          </Form>
        </TabPane>

        {/* 安全设置 */}
        <TabPane
          tab={
            <span>
              <SecurityScanOutlined />
              安全设置
            </span>
          }
          key="security"
        >
          <Form
            form={securityForm}
            layout="vertical"
            initialValues={securitySettings}
            onValuesChange={(_, values) => {
              setSecuritySettings(values);
              setHasChanges(true);
            }}
          >
            <Row gutter={[24, 16]}>
              <Col xs={24} md={12}>
                <Card title="访问控制" size="small">
                  <Form.Item
                    name="enableAuthentication"
                    label="启用身份认证"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>

                  <Form.Item
                    name="enableAuthorization"
                    label="启用权限控制"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>

                  <Form.Item
                    name="enableRateLimit"
                    label="启用速率限制"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>

                  <Form.Item
                    name="rateLimitRequests"
                    label="速率限制请求数"
                    dependencies={['enableRateLimit']}
                  >
                    <InputNumber
                      min={10}
                      max={10000}
                      style={{ width: '100%' }}
                      disabled={!securityForm.getFieldValue('enableRateLimit')}
                    />
                  </Form.Item>

                  <Form.Item
                    name="rateLimitWindow"
                    label="速率限制时间窗口 (秒)"
                    dependencies={['enableRateLimit']}
                  >
                    <InputNumber
                      min={1}
                      max={3600}
                      style={{ width: '100%' }}
                      disabled={!securityForm.getFieldValue('enableRateLimit')}
                    />
                  </Form.Item>
                </Card>
              </Col>

              <Col xs={24} md={12}>
                <Card title="审计和加密" size="small">
                  <Form.Item
                    name="enableIPWhitelist"
                    label="启用IP白名单"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>

                  <Form.Item
                    name="ipWhitelist"
                    label="IP白名单"
                    dependencies={['enableIPWhitelist']}
                  >
                    <Select
                      mode="tags"
                      style={{ width: '100%' }}
                      placeholder="输入IP地址"
                      disabled={!securityForm.getFieldValue('enableIPWhitelist')}
                    />
                  </Form.Item>

                  <Form.Item
                    name="enableAuditLog"
                    label="启用审计日志"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>

                  <Form.Item
                    name="auditLogRetention"
                    label="审计日志保留天数"
                    dependencies={['enableAuditLog']}
                  >
                    <InputNumber
                      min={7}
                      max={365}
                      style={{ width: '100%' }}
                      disabled={!securityForm.getFieldValue('enableAuditLog')}
                    />
                  </Form.Item>

                  <Form.Item
                    name="enableEncryption"
                    label="启用数据加密"
                    valuePropName="checked"
                  >
                    <Switch />
                  </Form.Item>
                </Card>
              </Col>
            </Row>
          </Form>
        </TabPane>

        {/* 系统维护 */}
        <TabPane
          tab={
            <span>
              <DatabaseOutlined />
              系统维护
            </span>
          }
          key="maintenance"
        >
          <Row gutter={[24, 16]}>
            <Col xs={24} md={12}>
              <Card title="缓存管理" size="small">
                <Space direction="vertical" style={{ width: '100%' }}>
                  <Alert
                    message="缓存状态"
                    description="当前缓存使用情况和清理选项"
                    type="info"
                    showIcon
                  />

                  <div>
                    <Text>缓存使用率:</Text>
                    <Progress percent={65} size="small" style={{ marginTop: 8 }} />
                  </div>

                  <div>
                    <Text>缓存条目数: 1,234</Text>
                  </div>

                  <div>
                    <Text>缓存大小: 128 MB</Text>
                  </div>

                  <Popconfirm
                    title="确定要清除所有缓存吗？"
                    description="这将清除所有协作结果缓存"
                    onConfirm={onClearCache}
                    okText="确定"
                    cancelText="取消"
                  >
                    <Button danger icon={<DeleteOutlined />}>
                      清除所有缓存
                    </Button>
                  </Popconfirm>
                </Space>
              </Card>
            </Col>

            <Col xs={24} md={12}>
              <Card title="系统信息" size="small">
                <Collapse size="small">
                  <Panel header="版本信息" key="version">
                    <List size="small">
                      <List.Item>
                        <Text>协作引擎版本: 1.0.0</Text>
                      </List.Item>
                      <List.Item>
                        <Text>API版本: v1</Text>
                      </List.Item>
                      <List.Item>
                        <Text>构建时间: 2024-01-15 10:30:00</Text>
                      </List.Item>
                    </List>
                  </Panel>

                  <Panel header="运行状态" key="status">
                    <List size="small">
                      <List.Item>
                        <Text>运行时间: 15天 8小时 32分钟</Text>
                      </List.Item>
                      <List.Item>
                        <Text>处理会话数: 2,456</Text>
                      </List.Item>
                      <List.Item>
                        <Text>活跃连接数: 23</Text>
                      </List.Item>
                    </List>
                  </Panel>

                  <Panel header="资源使用" key="resources">
                    <Space direction="vertical" style={{ width: '100%' }}>
                      <div>
                        <Text>CPU使用率:</Text>
                        <Progress percent={45} size="small" style={{ marginTop: 4 }} />
                      </div>
                      <div>
                        <Text>内存使用率:</Text>
                        <Progress percent={68} size="small" style={{ marginTop: 4 }} />
                      </div>
                      <div>
                        <Text>磁盘使用率:</Text>
                        <Progress percent={32} size="small" style={{ marginTop: 4 }} />
                      </div>
                    </Space>
                  </Panel>
                </Collapse>
              </Card>
            </Col>
          </Row>
        </TabPane>
      </Tabs>

      {/* 导出设置模态框 */}
      <Modal
        title="导出设置"
        open={exportModalVisible}
        onCancel={() => setExportModalVisible(false)}
        footer={[
          <Button key="cancel" onClick={() => setExportModalVisible(false)}>
            取消
          </Button>,
          <Button key="export" type="primary" onClick={handleExportSettings}>
            导出
          </Button>
        ]}
      >
        <Alert
          message="导出说明"
          description="将导出当前所有设置配置为JSON文件，可用于备份或迁移到其他环境。"
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
        />
        <Text>导出内容包括:</Text>
        <ul>
          <li>引擎设置</li>
          <li>智能体设置</li>
          <li>会话设置</li>
          <li>通知设置</li>
          <li>安全设置</li>
        </ul>
      </Modal>

      {/* 导入设置模态框 */}
      <Modal
        title="导入设置"
        open={importModalVisible}
        onCancel={() => setImportModalVisible(false)}
        footer={[
          <Button key="cancel" onClick={() => setImportModalVisible(false)}>
            取消
          </Button>
        ]}
      >
        <Alert
          message="导入说明"
          description="请选择之前导出的设置文件进行导入。导入后需要保存设置才能生效。"
          type="warning"
          showIcon
          style={{ marginBottom: 16 }}
        />
        <input
          type="file"
          accept=".json"
          onChange={(e) => {
            const file = e.target.files?.[0];
            if (file) {
              handleImportSettings(file);
            }
          }}
        />
      </Modal>
    </div>
  );
};

export default CollaborationSettings;