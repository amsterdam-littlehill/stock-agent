import React from 'react';
import { Tooltip } from 'antd';
import type { WorkflowEdge } from '@/types/workflow';

interface WorkflowEdgeComponentProps {
  edge: WorkflowEdge;
  sourcePosition: { x: number; y: number };
  targetPosition: { x: number; y: number };
  selected?: boolean;
  onSelect?: (edge: WorkflowEdge) => void;
  onDelete?: (edgeId: string) => void;
}

// 计算箭头路径
const getArrowPath = (x1: number, y1: number, x2: number, y2: number) => {
  const dx = x2 - x1;
  const dy = y2 - y1;
  // const angle = Math.atan2(dy, dx); // 暂时不使用
  const length = Math.sqrt(dx * dx + dy * dy);

  // 控制点偏移
  const controlOffset = Math.min(length * 0.3, 100);

  const cp1x = x1 + controlOffset;
  const cp1y = y1;
  const cp2x = x2 - controlOffset;
  const cp2y = y2;

  return `M ${x1} ${y1} C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${x2} ${y2}`;
};

// 计算箭头标记
const getArrowMarker = (x1: number, y1: number, x2: number, y2: number) => {
  const dx = x2 - x1;
  const dy = y2 - y1;
  const angle = Math.atan2(dy, dx);

  const arrowLength = 8;
  // const arrowWidth = 6; // 暂时不使用

  const x3 = x2 - arrowLength * Math.cos(angle - Math.PI / 6);
  const y3 = y2 - arrowLength * Math.sin(angle - Math.PI / 6);
  const x4 = x2 - arrowLength * Math.cos(angle + Math.PI / 6);
  const y4 = y2 - arrowLength * Math.sin(angle + Math.PI / 6);

  return `M ${x2} ${y2} L ${x3} ${y3} M ${x2} ${y2} L ${x4} ${y4}`;
};

// 获取边的颜色
const getEdgeColor = (edge: WorkflowEdge, selected: boolean) => {
  if (selected) return '#1890ff';

  switch (edge.condition?.type) {
    case 'SUCCESS':
      return '#52c41a';
    case 'FAILURE':
      return '#ff4d4f';
    case 'EXPRESSION':
      return '#fa8c16';
    default:
      return '#d9d9d9';
  }
};

// 获取边的样式
const getEdgeStyle = (edge: WorkflowEdge) => {
  switch (edge.condition?.type) {
    case 'FAILURE':
      return '5,5'; // 虚线
    case 'EXPRESSION':
      return '10,5'; // 点划线
    default:
      return 'none'; // 实线
  }
};

export const WorkflowEdgeComponent: React.FC<WorkflowEdgeComponentProps> = ({
  edge,
  sourcePosition,
  targetPosition,
  selected = false,
  onSelect,
  onDelete
}) => {
  const color = getEdgeColor(edge, selected);
  const strokeDasharray = getEdgeStyle(edge);

  // 计算连接点位置（从节点边缘开始）
  const x1 = sourcePosition.x + 90; // 节点宽度的一半 + 连接点偏移
  const y1 = sourcePosition.y + 40; // 节点高度的一半
  const x2 = targetPosition.x - 6; // 目标节点左边缘
  const y2 = targetPosition.y + 40;

  const pathData = getArrowPath(x1, y1, x2, y2);
  const arrowData = getArrowMarker(x1, y1, x2, y2);

  // 计算标签位置（路径中点）
  const labelX = (x1 + x2) / 2;
  const labelY = (y1 + y2) / 2;

  const handleClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    onSelect?.(edge);
  };

  const handleDoubleClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    onDelete?.(edge.id);
  };

  return (
    <g onClick={handleClick} onDoubleClick={handleDoubleClick} style={{ cursor: 'pointer' }}>
      {/* 主路径 */}
      <path
        d={pathData}
        stroke={color}
        strokeWidth={selected ? 3 : 2}
        strokeDasharray={strokeDasharray}
        fill="none"
        style={{transition: 'all 0.3s ease'}}
      />

      {/* 箭头 */}
      <path
        d={arrowData}
        stroke={color}
        strokeWidth={selected ? 3 : 2}
        fill="none"
        strokeLinecap="round"
      />

      {/* 可点击区域（透明的粗线） */}
      <path
        d={pathData}
        stroke="transparent"
        strokeWidth={12}
        fill="none"
        style={{ cursor: 'pointer' }}
      />

      {/* 标签 */}
      {edge.label && (
        <g>
          <rect
            x={labelX - 20}
            y={labelY - 8}
            width={40}
            height={16}
            fill="#fff"
            stroke={color}
            strokeWidth={1}
            rx={8}
          />
          <text
            x={labelX}
            y={labelY + 3}
            textAnchor="middle"
            fontSize={10}
            fill={color}
            fontWeight={selected ? 'bold' : 'normal'}
          >
            {edge.label}
          </text>
        </g>
      )}

      {/* 条件标识 */}
      {edge.condition?.type && edge.condition.type !== 'ALWAYS' && (
        <Tooltip title={`条件: ${edge.condition.type}${edge.condition.expression ? ` - ${edge.condition.expression}` : ''}`}>
          <circle
            cx={labelX + 25}
            cy={labelY}
            r={6}
            fill={color}
            stroke="#fff"
            strokeWidth={2}
          />
          <text
            x={labelX + 25}
            y={labelY + 2}
            textAnchor="middle"
            fontSize={8}
            fill="#fff"
            fontWeight="bold"
          >
            {edge.condition.type === 'SUCCESS' ? '✓' :
              edge.condition.type === 'FAILURE' ? '✗' : '?'}
          </text>
        </Tooltip>
      )}

      {/* 选中状态的高亮 */}
      {selected && (
        <>
          <circle
            cx={x1}
            cy={y1}
            r={4}
            fill={color}
            stroke="#fff"
            strokeWidth={2}
          />
          <circle
            cx={x2}
            cy={y2}
            r={4}
            fill={color}
            stroke="#fff"
            strokeWidth={2}
          />
        </>
      )}
    </g>
  );
};

export default WorkflowEdgeComponent;