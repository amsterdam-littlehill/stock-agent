import React from 'react';
import { Card, Badge, Tooltip } from 'antd';
import {
  PlayCircleOutlined,
  PauseCircleOutlined,
  CodeOutlined,
  BranchesOutlined,
  ApiOutlined,
  DatabaseOutlined,
  BellOutlined,
  MailOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  LoadingOutlined
} from '@ant-design/icons';
import type { WorkflowNode, WorkflowNodeType, NodeExecutionStatus } from '@/types/workflow';

interface WorkflowNodeComponentProps {
  node: WorkflowNode;
  selected?: boolean;
  onSelect?: (node: WorkflowNode) => void;
  style?: React.CSSProperties;
}

// 获取节点图标
const getNodeIcon = (nodeType: WorkflowNodeType) => {
  switch (nodeType) {
    case 'START':
      return <PlayCircleOutlined />;
    case 'END':
      return <PauseCircleOutlined />;
    case 'AGENT_CALL':
      return <CodeOutlined />;
    case 'CONDITION':
      return <BranchesOutlined />;
    case 'HTTP_REQUEST':
      return <ApiOutlined />;
    case 'DATABASE_QUERY':
      return <DatabaseOutlined />;
    case 'NOTIFICATION':
      return <BellOutlined />;
    case 'EMAIL':
      return <MailOutlined />;
    default:
      return <CodeOutlined />;
  }
};

// 获取节点颜色
const getNodeColor = (nodeType: WorkflowNodeType) => {
  switch (nodeType) {
    case 'START':
      return '#52c41a';
    case 'END':
      return '#ff4d4f';
    case 'AGENT_CALL':
      return '#1890ff';
    case 'CONDITION':
      return '#fa8c16';
    case 'HTTP_REQUEST':
      return '#722ed1';
    case 'DATABASE_QUERY':
      return '#13c2c2';
    case 'NOTIFICATION':
      return '#eb2f96';
    case 'EMAIL':
      return '#f5222d';
    default:
      return '#1890ff';
  }
};

// 获取状态图标
const getStatusIcon = (status?: NodeExecutionStatus) => {
  switch (status) {
    case 'COMPLETED':
      return <CheckCircleOutlined style={{ color: '#52c41a' }} />;
    case 'FAILED':
      return <CloseCircleOutlined style={{ color: '#ff4d4f' }} />;
    case 'RUNNING':
      return <LoadingOutlined style={{ color: '#1890ff' }} />;
    default:
      return null;
  }
};

// 获取状态颜色
const getStatusColor = (status?: NodeExecutionStatus) => {
  switch (status) {
    case 'COMPLETED':
      return '#52c41a';
    case 'FAILED':
      return '#ff4d4f';
    case 'RUNNING':
      return '#1890ff';
    case 'PENDING':
      return '#faad14';
    default:
      return '#d9d9d9';
  }
};

export const WorkflowNodeComponent: React.FC<WorkflowNodeComponentProps> = ({
  node,
  selected = false,
  onSelect,
  style
}) => {
  const nodeColor = getNodeColor(node.type);
  const statusIcon = getStatusIcon(node.status);
  const statusColor = getStatusColor(node.status);

  const handleClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    onSelect?.(node);
  };

  const handleDoubleClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    // 双击可以触发编辑模式
  };

  return (
    <div
      style={{
        position: 'absolute',
        left: node.position.x,
        top: node.position.y,
        cursor: 'pointer',
        ...style
      }}
      onClick={handleClick}
      onDoubleClick={handleDoubleClick}
    >
      <Card
        size="small"
        style={{
          width: 180,
          minHeight: 80,
          border: selected ? `2px solid ${nodeColor}` : '1px solid #d9d9d9',
          borderRadius: 8,
          boxShadow: selected
            ? `0 4px 12px ${nodeColor}40`
            : '0 2px 8px rgba(0,0,0,0.1)',
          transition: 'all 0.3s ease',
          backgroundColor: '#fff'
        }}
        bodyStyle={{ padding: '12px' }}
      >
        {/* 节点头部 */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginBottom: 8
        }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: 6
          }}>
            <span style={{
              color: nodeColor,
              fontSize: 16
            }}>
              {getNodeIcon(node.type)}
            </span>
            <span style={{
              fontSize: 12,
              fontWeight: 500,
              color: '#262626',
              maxWidth: 100,
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap'
            }}>
              {node.name}
            </span>
          </div>
          {statusIcon && (
            <Tooltip title={`状态: ${node.status}`}>
              {statusIcon}
            </Tooltip>
          )}
        </div>

        {/* 节点描述 */}
        {node.description && (
          <div style={{
            fontSize: 11,
            color: '#8c8c8c',
            lineHeight: 1.4,
            maxHeight: 28,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical'
          }}>
            {node.description}
          </div>
        )}

        {/* 状态指示器 */}
        <div style={{
          position: 'absolute',
          top: -1,
          left: -1,
          width: 8,
          height: 8,
          borderRadius: '50%',
          backgroundColor: statusColor,
          border: '2px solid #fff'
        }} />

        {/* 连接点 */}
        {/* 输入连接点 */}
        {node.inputs && node.inputs.length > 0 && (
          <div style={{
            position: 'absolute',
            left: -6,
            top: '50%',
            transform: 'translateY(-50%)',
            width: 12,
            height: 12,
            borderRadius: '50%',
            backgroundColor: '#fff',
            border: `2px solid ${nodeColor}`,
            cursor: 'crosshair'
          }} />
        )}

        {/* 输出连接点 */}
        {node.outputs && node.outputs.length > 0 && (
          <div style={{
            position: 'absolute',
            right: -6,
            top: '50%',
            transform: 'translateY(-50%)',
            width: 12,
            height: 12,
            borderRadius: '50%',
            backgroundColor: nodeColor,
            border: '2px solid #fff',
            cursor: 'crosshair'
          }} />
        )}

        {/* 错误提示 */}
        {node.error && (
          <Tooltip title={node.error}>
            <Badge
              count="!"
              style={{
                position: 'absolute',
                top: -8,
                right: -8,
                backgroundColor: '#ff4d4f',
                color: '#fff',
                fontSize: 10,
                minWidth: 16,
                height: 16,
                lineHeight: '16px',
                borderRadius: '50%'
              }}
            />
          </Tooltip>
        )}
      </Card>
    </div>
  );
};

export default WorkflowNodeComponent;