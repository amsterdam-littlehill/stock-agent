import React from 'react';

interface ConnectionLineProps {
  sourceX: number;
  sourceY: number;
  targetX: number;
  targetY: number;
  isConnecting?: boolean;
}

// 计算贝塞尔曲线路径
const getBezierPath = (x1: number, y1: number, x2: number, y2: number) => {
  const dx = x2 - x1;
  const dy = y2 - y1;
  const length = Math.sqrt(dx * dx + dy * dy);

  // 控制点偏移
  const controlOffset = Math.min(length * 0.3, 100);

  const cp1x = x1 + controlOffset;
  const cp1y = y1;
  const cp2x = x2 - controlOffset;
  const cp2y = y2;

  return `M ${x1} ${y1} C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${x2} ${y2}`;
};

// 连接线组件 - 用于拖拽时显示临时连接线
export const ConnectionLine: React.FC<ConnectionLineProps> = ({
  sourceX,
  sourceY,
  targetX,
  targetY,
  isConnecting = false
}) => {
  if (!isConnecting) {
    return null;
  }

  const pathData = getBezierPath(sourceX, sourceY, targetX, targetY);

  return (
    <g>
      <path
        d={pathData}
        stroke="#1890ff"
        strokeWidth={2}
        strokeDasharray="5,5"
        fill="none"
        style={{
          opacity: 0.6,
          animation: 'dash 1s linear infinite'
        }}
      />

      {/* 目标点指示器 */}
      <circle
        cx={targetX}
        cy={targetY}
        r={4}
        fill="#1890ff"
        stroke="#fff"
        strokeWidth={2}
        style={{opacity: 0.8}}
      />

      <style>
        {`
          @keyframes dash {
            to {
              stroke-dashoffset: -10;
            }
          }
        `}
      </style>
    </g>
  );
};

export default ConnectionLine;