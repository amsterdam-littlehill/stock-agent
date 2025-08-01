import React from 'react';
import { Card, Progress, List, Tag, Typography, Space } from 'antd';
import { CheckCircleOutlined, LoadingOutlined, ClockCircleOutlined } from '@ant-design/icons';

const { Text } = Typography;

export interface AgentProgress {
  id: string;
  name: string;
  status: 'pending' | 'running' | 'completed' | 'failed';
  progress: number;
  currentTask?: string;
  startTime?: string;
  endTime?: string;
  result?: any;
}

export interface AgentProgressPanelProps {
  agents: AgentProgress[];
  title?: string;
}

const getStatusIcon = (status: string) => {
  switch (status) {
    case 'completed':
      return <CheckCircleOutlined style={{ color: '#52c41a' }} />;
    case 'running':
      return <LoadingOutlined style={{ color: '#1890ff' }} />;
    case 'failed':
      return <CheckCircleOutlined style={{ color: '#ff4d4f' }} />;
    default:
      return <ClockCircleOutlined style={{ color: '#d9d9d9' }} />;
  }
};

const getStatusColor = (status: string) => {
  switch (status) {
    case 'completed':
      return 'success';
    case 'running':
      return 'processing';
    case 'failed':
      return 'error';
    default:
      return 'default';
  }
};

export const AgentProgressPanel: React.FC<AgentProgressPanelProps> = ({
  agents,
  title = '智能体执行进度'
}) => {
  return (
    <Card title={title} size="small">
      <List
        dataSource={agents}
        renderItem={(agent) => (
          <List.Item>
            <div style={{ width: '100%' }}>
              <Space align="center" style={{ marginBottom: 8 }}>
                {getStatusIcon(agent.status)}
                <Text strong>{agent.name}</Text>
                <Tag color={getStatusColor(agent.status)}>
                  {agent.status.toUpperCase()}
                </Tag>
              </Space>

              <Progress
                percent={agent.progress}
                size="small"
                status={agent.status === 'failed' ? 'exception' : 'active'}
                showInfo={false}
              />

              {agent.currentTask && (
                <Text type="secondary" style={{ fontSize: '12px' }}>
                  当前任务: {agent.currentTask}
                </Text>
              )}
            </div>
          </List.Item>
        )}
      />
    </Card>
  );
};

export default AgentProgressPanel;
