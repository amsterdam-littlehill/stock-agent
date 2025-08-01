import React from 'react';
import { Card, List, Typography, Space } from 'antd';
import {
  DatabaseOutlined,
  ApiOutlined,
  BranchesOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  MailOutlined,
  BellOutlined,
  CodeOutlined
} from '@ant-design/icons';
import type { WorkflowNodeType } from '@/types/workflow';

const { Text } = Typography;

interface NodePaletteProps {
  onNodeDrag?: (nodeType: WorkflowNodeType) => void;
  onNodeClick?: (nodeType: WorkflowNodeType) => void;
}

// 节点类型配置
const nodeTypes = [
  {
    type: WorkflowNodeType.START,
    label: '开始节点',
    icon: <PlayCircleOutlined />,
    description: '工作流开始节点',
    color: '#52c41a'
  },
  {
    type: WorkflowNodeType.END,
    label: '结束节点',
    icon: <PauseCircleOutlined />,
    description: '工作流结束节点',
    color: '#ff4d4f'
  },
  {
    type: WorkflowNodeType.AGENT_CALL,
    label: '智能体调用',
    icon: <CodeOutlined />,
    description: '调用智能体执行任务',
    color: '#1890ff'
  },
  {
    type: WorkflowNodeType.CONDITION,
    label: '条件节点',
    icon: <BranchesOutlined />,
    description: '条件判断分支',
    color: '#fa8c16'
  },
  {
    type: WorkflowNodeType.HTTP_REQUEST,
    label: 'HTTP请求',
    icon: <ApiOutlined />,
    description: '发送HTTP请求',
    color: '#722ed1'
  },
  {
    type: WorkflowNodeType.DATABASE_QUERY,
    label: '数据库查询',
    icon: <DatabaseOutlined />,
    description: '执行数据库查询',
    color: '#13c2c2'
  },
  {
    type: WorkflowNodeType.NOTIFICATION,
    label: '通知节点',
    icon: <BellOutlined />,
    description: '发送通知消息',
    color: '#eb2f96'
  },
  {
    type: WorkflowNodeType.EMAIL,
    label: '邮件发送',
    icon: <MailOutlined />,
    description: '发送邮件',
    color: '#f5222d'
  }
];

export const NodePalette: React.FC<NodePaletteProps> = ({
  onNodeDrag,
  onNodeClick
}) => {
  const handleNodeDragStart = (e: React.DragEvent, nodeType: WorkflowNodeType) => {
    e.dataTransfer.setData('application/reactflow', nodeType);
    e.dataTransfer.effectAllowed = 'move';
    onNodeDrag?.(nodeType);
  };

  const handleNodeClick = (nodeType: WorkflowNodeType) => {
    onNodeClick?.(nodeType);
  };

  return (
    <Card
      title="节点面板"
      size="small"
      style={{
        height: '100%',
        overflow: 'auto'
      }}
      bodyStyle={{ padding: '8px' }}
    >
      <List
        size="small"
        dataSource={nodeTypes}
        renderItem={(nodeType) => (
          <List.Item
            style={{
              padding: '8px 0',
              cursor: 'grab'
            }}
            draggable
            onDragStart={(e) => handleNodeDragStart(e, nodeType.type)}
            onClick={() => handleNodeClick(nodeType.type)}
          >
            <Card
              size="small"
              hoverable
              style={{
                width: '100%',
                borderLeft: `4px solid ${nodeType.color}`,
                transition: 'all 0.3s ease'
              }}
              bodyStyle={{ padding: '8px 12px' }}
            >
              <Space direction="vertical" size={4} style={{ width: '100%' }}>
                <Space>
                  <span style={{
                    color: nodeType.color,
                    fontSize: '16px'
                  }}>
                    {nodeType.icon}
                  </span>
                  <Text strong style={{ fontSize: '12px' }}>
                    {nodeType.label}
                  </Text>
                </Space>
                <Text type="secondary" style={{ fontSize: '11px' }}>
                  {nodeType.description}
                </Text>
              </Space>
            </Card>
          </List.Item>
        )}
      />
    </Card>
  );
};

export default NodePalette;