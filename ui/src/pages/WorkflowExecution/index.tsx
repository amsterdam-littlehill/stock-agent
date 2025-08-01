import React, { useState, useEffect, useRef } from 'react';
import { Card, Steps, Progress, Button, Space, Tag, Descriptions, Alert, Drawer, Table, message } from 'antd';
import { PlayCircleOutlined, PauseCircleOutlined, StopOutlined, ReloadOutlined, DownloadOutlined, EyeOutlined } from '@ant-design/icons';
import { useParams, useNavigate } from 'react-router-dom';
import { workflowService } from '@/services/workflow';
import { useWebSocket } from '@/hooks/useWebSocket';
import type { WorkflowExecution, WorkflowExecutionStep, WorkflowExecutionLog } from '@/types/workflow';
import './index.css';

const { Step } = Steps;

// 工作流执行监控页面
const WorkflowExecution: React.FC = () => {
  const { executionId } = useParams<{ executionId: string }>();
  const navigate = useNavigate();
  const logsRef = useRef<HTMLDivElement>(null);

  const [execution, setExecution] = useState<WorkflowExecution | null>(null);
  const [_steps, setSteps] = useState<WorkflowExecutionStep[]>([]);
  const [logs, setLogs] = useState<WorkflowExecutionLog[]>([]);
  const [, setLoading] = useState(false);
  const [logsDrawerVisible, setLogsDrawerVisible] = useState(false);
  const [autoScroll, setAutoScroll] = useState(true);

  // WebSocket 连接用于实时更新
  const { isConnected } = useWebSocket(
    `/topic/workflow/execution/${executionId}`,
    {
      onMessage: (data) => {
        if (data.type === 'execution_update') {
          setExecution(data.execution);
        } else if (data.type === 'step_update') {
          setSteps(prev => {
            const index = prev.findIndex(s => s.id === data.step.id);
            if (index >= 0) {
              const newSteps = [...prev];
              newSteps[index] = data.step;
              return newSteps;
            } else {
              return [...prev, data.step];
            }
          });
        } else if (data.type === 'log_update') {
          setLogs(prev => [...prev, data.log]);
          if (autoScroll && logsRef.current) {
            setTimeout(() => {
              logsRef.current?.scrollTo({
                top: logsRef.current.scrollHeight,
                behavior: 'smooth'
              });
            }, 100);
          }
        }
      },
      onError: (error) => {
        console.error('WebSocket error:', error);
        message.error('实时连接断开，请刷新页面');
      }
    }
  );

  // 加载执行数据
  const loadExecution = async () => {
    if (!executionId) return;

    setLoading(true);
    try {
      const [executionRes, stepsRes, logsRes] = await Promise.all([
        workflowService.getExecution(executionId),
        workflowService.getExecutionSteps(executionId),
        workflowService.getExecutionLogs(executionId)
      ]);

      setExecution(executionRes.data);
      setSteps(stepsRes.data);
      setLogs(logsRes.data);
    } catch {
      message.error('加载执行数据失败');
      navigate('/workflow');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadExecution();
  }, [executionId]);

  // 控制执行
  const handleControl = async (action: 'pause' | 'resume' | 'stop') => {
    if (!executionId) return;

    try {
      await workflowService.controlExecution(executionId, action);
      message.success(`执行${action === 'pause' ? '暂停' : action === 'resume' ? '恢复' : '停止'}成功`);
    } catch {
      message.error(`执行${action === 'pause' ? '暂停' : action === 'resume' ? '恢复' : '停止'}失败`);
    }
  };

  // 重新执行
  const handleRerun = async () => {
    if (!execution?.workflowId) return;

    try {
      const response = await workflowService.executeWorkflow(execution.workflowId, execution.input);
      message.success('重新执行已启动');
      navigate(`/workflow/execution/${response.data.executionId}`);
    } catch {
      message.error('重新执行失败');
    }
  };

  // 下载执行报告
  const handleDownloadReport = async () => {
    if (!executionId) return;

    try {
      const response = await workflowService.downloadExecutionReport(executionId);
      const blob = new Blob([response.data], { type: 'application/json' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `workflow-execution-${executionId}.json`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      message.success('报告下载成功');
    } catch {
      message.error('下载报告失败');
    }
  };

  // 获取执行状态配置
  const getStatusConfig = (status: string) => {
    const configs = {
      PENDING: {
        color: 'default',
        text: '等待中'
      },
      RUNNING: {
        color: 'processing',
        text: '执行中'
      },
      PAUSED: {
        color: 'warning',
        text: '已暂停'
      },
      COMPLETED: {
        color: 'success',
        text: '已完成'
      },
      FAILED: {
        color: 'error',
        text: '执行失败'
      },
      CANCELLED: {
        color: 'default',
        text: '已取消'
      },
    };
    return configs[status as keyof typeof configs] || {
      color: 'default',
      text: status
    };
  };

  // 获取步骤状态
  const getStepStatus = (status: string) => {
    const statusMap = {
      PENDING: 'wait',
      RUNNING: 'process',
      COMPLETED: 'finish',
      FAILED: 'error',
      SKIPPED: 'wait',
    };
    return statusMap[status as keyof typeof statusMap] || 'wait';
  };

  // 计算总体进度
  const calculateProgress = () => {
    if (_steps.length === 0) return 0;
    const completedSteps = _steps.filter(s => s.status === 'COMPLETED').length;
    return Math.round((completedSteps / _steps.length) * 100);
  };

  // 日志表格列定义
  const logColumns = [
    {
      title: '时间',
      dataIndex: 'timestamp',
      key: 'timestamp',
      width: 180,
      render: (timestamp: string) => new Date(timestamp).toLocaleString(),
    },
    {
      title: '级别',
      dataIndex: 'level',
      key: 'level',
      width: 80,
      render: (level: string) => {
        const colors = {
          ERROR: 'red',
          WARN: 'orange',
          INFO: 'blue',
          DEBUG: 'default',
        };
        return <Tag color={colors[level as keyof typeof colors]}>{level}</Tag>;
      },
    },
    {
      title: '节点',
      dataIndex: 'nodeId',
      key: 'nodeId',
      width: 120,
    },
    {
      title: '消息',
      dataIndex: 'message',
      key: 'message',
      ellipsis: true,
    },
  ];

  if (!execution) {
    return <div className="execution-loading">加载中...</div>;
  }

  const statusConfig = getStatusConfig(execution.status);
  const progress = calculateProgress();

  return (
    <div className="workflow-execution">
      {/* 执行概览 */}
      <Card className="execution-overview">
        <div className="overview-header">
          <div className="overview-info">
            <h2 className="execution-title">{execution.workflowName}</h2>
            <div className="execution-meta">
              <Tag color={statusConfig.color} className="status-tag">
                {statusConfig.text}
              </Tag>
              <span className="execution-id">执行ID: {execution.id}</span>
              <span className="start-time">
                开始时间: {new Date(execution.startTime).toLocaleString()}
              </span>
              {execution.endTime && (
                <span className="end-time">
                  结束时间: {new Date(execution.endTime).toLocaleString()}
                </span>
              )}
            </div>
          </div>

          <div className="overview-actions">
            <Space>
              <Button
                icon={<EyeOutlined />}
                onClick={() => setLogsDrawerVisible(true)}
              >
                查看日志
              </Button>
              <Button
                icon={<DownloadOutlined />}
                onClick={handleDownloadReport}
              >
                下载报告
              </Button>
              {execution.status === 'RUNNING' && (
                <Button
                  icon={<PauseCircleOutlined />}
                  onClick={() => handleControl('pause')}
                >
                  暂停
                </Button>
              )}
              {execution.status === 'PAUSED' && (
                <Button
                  icon={<PlayCircleOutlined />}
                  onClick={() => handleControl('resume')}
                >
                  恢复
                </Button>
              )}
              {['RUNNING', 'PAUSED'].includes(execution.status) && (
                <Button
                  danger
                  icon={<StopOutlined />}
                  onClick={() => handleControl('stop')}
                >
                  停止
                </Button>
              )}
              {['COMPLETED', 'FAILED', 'CANCELLED'].includes(execution.status) && (
                <Button
                  type="primary"
                  icon={<ReloadOutlined />}
                  onClick={handleRerun}
                >
                  重新执行
                </Button>
              )}
            </Space>
          </div>
        </div>

        {/* 进度条 */}
        <div className="progress-section">
          <Progress
            percent={progress}
            status={execution.status === 'FAILED' ? 'exception' : execution.status === 'COMPLETED' ? 'success' : 'active'}
            strokeColor={{
              '0%': '#108ee9',
              '100%': '#87d068',
            }}
          />
          <div className="progress-info">
            <span>已完成 {_steps.filter(s => s.status === 'COMPLETED').length} / {_steps.length} 个步骤</span>
            {execution.status === 'RUNNING' && (
              <span className="connection-status">
                实时连接: {isConnected ? '已连接' : '已断开'}
              </span>
            )}
          </div>
        </div>

        {/* 执行详情 */}
        <Descriptions column={3} size="small" className="execution-details">
          <Descriptions.Item label="工作流版本">{execution.workflowVersion}</Descriptions.Item>
          <Descriptions.Item label="执行模式">{execution.testMode ? '测试模式' : '正常模式'}</Descriptions.Item>
          <Descriptions.Item label="执行时长">
            {execution.endTime
              ? `${Math.round((new Date(execution.endTime).getTime() - new Date(execution.startTime).getTime()) / 1000)}秒`
              : `${Math.round((Date.now() - new Date(execution.startTime).getTime()) / 1000)}秒`
            }
          </Descriptions.Item>
        </Descriptions>

        {/* 错误信息 */}
        {execution.error && (
          <Alert
            type="error"
            message="执行错误"
            description={execution.error}
            showIcon
            className="error-alert"
          />
        )}
      </Card>

      {/* 执行步骤 */}
      <Card title="执行步骤" className="execution-steps">
        <Steps
          direction="vertical"
          current={_steps.findIndex(s => s.status === 'RUNNING')}
          className="steps-timeline"
        >
          {_steps.map((step) => (
            <Step
              key={step.id}
              title={step.nodeName}
              description={
                <div className="step-description">
                  <div className="step-info">
                    <span className="step-type">{step.nodeType}</span>
                    {step.startTime && (
                      <span className="step-time">
                        {new Date(step.startTime).toLocaleString()}
                      </span>
                    )}
                    {step.duration && (
                      <span className="step-duration">
                        耗时: {step.duration}ms
                      </span>
                    )}
                  </div>
                  {step.output && (
                    <div className="step-output">
                      <pre>{JSON.stringify(step.output, null, 2)}</pre>
                    </div>
                  )}
                  {step.error && (
                    <div className="step-error">
                      <Alert type="error" message={step.error} />
                    </div>
                  )}
                </div>
              }
              status={getStepStatus(step.status) as any}
              icon={step.status === 'RUNNING' ? <PlayCircleOutlined /> : undefined}
            />
          ))}
        </Steps>
      </Card>

      {/* 日志抽屉 */}
      <Drawer
        title="执行日志"
        placement="right"
        width={800}
        open={logsDrawerVisible}
        onClose={() => setLogsDrawerVisible(false)}
        extra={
          <Space>
            <Button
              size="small"
              type={autoScroll ? 'primary' : 'default'}
              onClick={() => setAutoScroll(!autoScroll)}
            >
              自动滚动
            </Button>
            <Button
              size="small"
              onClick={() => setLogs([])}
            >
              清空日志
            </Button>
          </Space>
        }
      >
        <div ref={logsRef} className="logs-container">
          <Table
            columns={logColumns}
            dataSource={logs}
            rowKey="id"
            size="small"
            pagination={false}
            scroll={{ y: 'calc(100vh - 200px)' }}
          />
        </div>
      </Drawer>
    </div>
  );
};

export default WorkflowExecution;