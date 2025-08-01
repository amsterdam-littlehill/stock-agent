import React, { useRef, useEffect, useState, useCallback, forwardRef, useImperativeHandle } from 'react';
import { Spin } from 'antd';
import type { WorkflowNode, WorkflowEdge, Position } from '@/types/workflow';
import { WorkflowNodeComponent } from './WorkflowNodeComponent';
import { WorkflowEdgeComponent } from './WorkflowEdgeComponent';
import { ConnectionLine } from './ConnectionLine';
import './WorkflowCanvas.css';

interface WorkflowCanvasProps {
  nodes: WorkflowNode[];
  edges: WorkflowEdge[];
  selectedElements: Array<{ id: string; type: 'node' | 'edge' }>;
  zoom: number;
  onNodeSelect: (node: WorkflowNode) => void;
  onNodeUpdate: (nodeId: string, properties: any) => void;
  onEdgeAdd: (edge: Omit<WorkflowEdge, 'id'>) => void;
  onEdgeDelete: (edgeId: string) => void;
  onCanvasClick: (event: React.MouseEvent) => void;
  loading?: boolean;
}

export interface WorkflowCanvasRef {
  fitToScreen: () => void;
  centerView: () => void;
  getCanvasSize: () => { width: number; height: number };
}

// 工作流画布组件
export const WorkflowCanvas = forwardRef<WorkflowCanvasRef, WorkflowCanvasProps>((
  {
    nodes,
    edges,
    selectedElements,
    zoom,
    onNodeSelect,
    onNodeUpdate,
    onEdgeAdd,
    onEdgeDelete,
    onCanvasClick,
    loading = false,
  },
  ref
) => {
  const canvasRef = useRef<HTMLDivElement>(null);
  const svgRef = useRef<SVGSVGElement>(null);
  const [canvasSize, setCanvasSize] = useState({
    width: 0,
    height: 0
  });
  const [viewBox, setViewBox] = useState({
    x: 0,
    y: 0,
    width: 1000,
    height: 600
  });
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({
    x: 0,
    y: 0
  });
  const [dragNode, setDragNode] = useState<string | null>(null);
  const [connectionStart, setConnectionStart] = useState<{
    nodeId: string;
    portId: string;
    position: Position;
  } | null>(null);
  const [connectionEnd, setConnectionEnd] = useState<Position | null>(null);
  const [mousePosition, setMousePosition] = useState({
    x: 0,
    y: 0
  });

  // 暴露给父组件的方法
  useImperativeHandle(ref, () => ({
    fitToScreen: () => {
      if (nodes.length === 0) return;

      const padding = 50;
      const minX = Math.min(...nodes.map(n => n.position.x)) - padding;
      const maxX = Math.max(...nodes.map(n => n.position.x + 200)) + padding; // 假设节点宽度200
      const minY = Math.min(...nodes.map(n => n.position.y)) - padding;
      const maxY = Math.max(...nodes.map(n => n.position.y + 100)) + padding; // 假设节点高度100

      const width = maxX - minX;
      const height = maxY - minY;

      setViewBox({
        x: minX,
        y: minY,
        width,
        height
      });
    },
    centerView: () => {
      const centerX = viewBox.width / 2;
      const centerY = viewBox.height / 2;
      setViewBox(prev => ({
        ...prev,
        x: centerX - canvasSize.width / 2,
        y: centerY - canvasSize.height / 2,
      }));
    },
    getCanvasSize: () => canvasSize,
  }));

  // 更新画布尺寸
  const updateCanvasSize = useCallback(() => {
    if (canvasRef.current) {
      const rect = canvasRef.current.getBoundingClientRect();
      setCanvasSize({
        width: rect.width,
        height: rect.height
      });
    }
  }, []);

  // 监听窗口大小变化
  useEffect(() => {
    updateCanvasSize();
    window.addEventListener('resize', updateCanvasSize);
    return () => window.removeEventListener('resize', updateCanvasSize);
  }, [updateCanvasSize]);

  // 屏幕坐标转换为画布坐标
  const screenToCanvas = useCallback((screenX: number, screenY: number): Position => {
    if (!canvasRef.current) return {
      x: 0,
      y: 0
    };

    const rect = canvasRef.current.getBoundingClientRect();
    const x = (screenX - rect.left) / zoom + viewBox.x;
    const y = (screenY - rect.top) / zoom + viewBox.y;

    return {
      x,
      y
    };
  }, [zoom, viewBox]);

  // 画布坐标转换为屏幕坐标
  // const canvasToScreen = useCallback((canvasX: number, canvasY: number): Position => {
  //   if (!canvasRef.current) return {
  //     x: 0,
  //     y: 0
  //   };

  //   const rect = canvasRef.current.getBoundingClientRect();
  //   const x = (canvasX - viewBox.x) * zoom + rect.left;
  //   const y = (canvasY - viewBox.y) * zoom + rect.top;

  //   return {
  //     x,
  //     y
  //   };
  // }, [zoom, viewBox]);

  // 处理鼠标按下事件
  const handleMouseDown = useCallback((event: React.MouseEvent) => {
    if (event.button !== 0) return; // 只处理左键

    const canvasPos = screenToCanvas(event.clientX, event.clientY);
    setIsDragging(true);
    setDragStart({
      x: event.clientX,
      y: event.clientY
    });

    // 检查是否点击在节点上
    const clickedNode = nodes.find(node => {
      const nodeRect = {
        x: node.position.x,
        y: node.position.y,
        width: 200, // 节点宽度
        height: 100, // 节点高度
      };

      return canvasPos.x >= nodeRect.x &&
             canvasPos.x <= nodeRect.x + nodeRect.width &&
             canvasPos.y >= nodeRect.y &&
             canvasPos.y <= nodeRect.y + nodeRect.height;
    });

    if (clickedNode) {
      setDragNode(clickedNode.id);
      onNodeSelect(clickedNode);
    } else {
      onCanvasClick(event);
    }
  }, [nodes, screenToCanvas, onNodeSelect, onCanvasClick]);

  // 处理鼠标移动事件
  const handleMouseMove = useCallback((event: React.MouseEvent) => {
    const canvasPos = screenToCanvas(event.clientX, event.clientY);
    setMousePosition(canvasPos);

    if (isDragging) {
      if (dragNode) {
        // 拖拽节点
        const deltaX = (event.clientX - dragStart.x) / zoom;
        const deltaY = (event.clientY - dragStart.y) / zoom;

        const node = nodes.find(n => n.id === dragNode);
        if (node) {
          onNodeUpdate(dragNode, {
            position: {
              x: node.position.x + deltaX,
              y: node.position.y + deltaY,
            },
          });
        }

        setDragStart({
          x: event.clientX,
          y: event.clientY
        });
      } else {
        // 拖拽画布
        const deltaX = (event.clientX - dragStart.x) / zoom;
        const deltaY = (event.clientY - dragStart.y) / zoom;

        setViewBox(prev => ({
          ...prev,
          x: prev.x - deltaX,
          y: prev.y - deltaY,
        }));

        setDragStart({
          x: event.clientX,
          y: event.clientY
        });
      }
    }

    // 更新连接线终点
    if (connectionStart) {
      setConnectionEnd(canvasPos);
    }
  }, [isDragging, dragNode, dragStart, zoom, nodes, onNodeUpdate, screenToCanvas, connectionStart]);

  // 处理鼠标抬起事件
  const handleMouseUp = useCallback((event: React.MouseEvent) => {
    setIsDragging(false);
    setDragNode(null);

    // 处理连接完成
    if (connectionStart && connectionEnd) {
      const canvasPos = screenToCanvas(event.clientX, event.clientY);

      // 查找目标节点和端口
      const targetNode = nodes.find(node => {
        const nodeRect = {
          x: node.position.x,
          y: node.position.y,
          width: 200,
          height: 100,
        };

        return canvasPos.x >= nodeRect.x &&
               canvasPos.x <= nodeRect.x + nodeRect.width &&
               canvasPos.y >= nodeRect.y &&
               canvasPos.y <= nodeRect.y + nodeRect.height;
      });

      if (targetNode && targetNode.id !== connectionStart.nodeId) {
        // 创建连接
        const targetPort = targetNode.inputs[0]; // 简化：使用第一个输入端口
        if (targetPort) {
          onEdgeAdd({
            sourceNodeId: connectionStart.nodeId,
            sourcePortId: connectionStart.portId,
            targetNodeId: targetNode.id,
            targetPortId: targetPort.id,
          });
        }
      }
    }

    setConnectionStart(null);
    setConnectionEnd(null);
  }, [connectionStart, connectionEnd, screenToCanvas, nodes, onEdgeAdd]);

  // 处理滚轮缩放
  const handleWheel = useCallback((event: React.WheelEvent) => {
    event.preventDefault();

    const delta = event.deltaY > 0 ? 0.9 : 1.1;
    const newZoom = Math.max(0.1, Math.min(3, zoom * delta));

    // 以鼠标位置为中心缩放
    const canvasPos = screenToCanvas(event.clientX, event.clientY);
    const zoomRatio = newZoom / zoom;

    setViewBox(prev => ({
      x: canvasPos.x - (canvasPos.x - prev.x) * zoomRatio,
      y: canvasPos.y - (canvasPos.y - prev.y) * zoomRatio,
      width: prev.width / zoomRatio,
      height: prev.height / zoomRatio,
    }));
  }, [zoom, screenToCanvas]);

  // 开始连接
  const handleConnectionStart = useCallback((nodeId: string, portId: string, position: Position) => {
    setConnectionStart({
      nodeId,
      portId,
      position
    });
  }, []);

  // 处理边点击
  const handleEdgeClick = useCallback(() => {
    // 可以添加边选择逻辑
  }, []);

  // 处理边删除
  const handleEdgeDoubleClick = useCallback((edgeId: string) => {
    onEdgeDelete(edgeId);
  }, [onEdgeDelete]);

  // 计算网格线
  const renderGrid = () => {
    const gridSize = 20;
    const startX = Math.floor(viewBox.x / gridSize) * gridSize;
    const startY = Math.floor(viewBox.y / gridSize) * gridSize;
    const endX = viewBox.x + viewBox.width;
    const endY = viewBox.y + viewBox.height;

    const lines = [];

    // 垂直线
    for (let x = startX; x <= endX; x += gridSize) {
      lines.push(
        <line
          key={`v-${x}`}
          x1={x}
          y1={viewBox.y}
          x2={x}
          y2={viewBox.y + viewBox.height}
          stroke="#e8e8e8"
          strokeWidth={0.5}
        />
      );
    }

    // 水平线
    for (let y = startY; y <= endY; y += gridSize) {
      lines.push(
        <line
          key={`h-${y}`}
          x1={viewBox.x}
          y1={y}
          x2={viewBox.x + viewBox.width}
          y2={y}
          stroke="#e8e8e8"
          strokeWidth={0.5}
        />
      );
    }

    return lines;
  };

  return (
    <div
      ref={canvasRef}
      className="workflow-canvas"
      onMouseDown={handleMouseDown}
      onMouseMove={handleMouseMove}
      onMouseUp={handleMouseUp}
      onWheel={handleWheel}
    >
      {loading && (
        <div className="canvas-loading">
          <Spin size="large" tip="加载中..." />
        </div>
      )}

      <svg
        ref={svgRef}
        className="canvas-svg"
        viewBox={`${viewBox.x} ${viewBox.y} ${viewBox.width} ${viewBox.height}`}
        style={{ transform: `scale(${zoom})` }}
      >
        {/* 网格 */}
        <g className="grid">
          {renderGrid()}
        </g>

        {/* 边 */}
        <g className="edges">
          {edges.map(edge => {
            const sourceNode = nodes.find(n => n.id === edge.sourceNodeId);
            const targetNode = nodes.find(n => n.id === edge.targetNodeId);

            if (!sourceNode || !targetNode) return null;

            const isSelected = selectedElements.some(el => el.id === edge.id && el.type === 'edge');

            return (
              <WorkflowEdgeComponent
                key={edge.id}
                edge={edge}
                sourcePosition={{
                  x: sourceNode.position.x + 200, // 节点右侧
                  y: sourceNode.position.y + 50,  // 节点中心
                }}
                targetPosition={{
                  x: targetNode.position.x,       // 节点左侧
                  y: targetNode.position.y + 50,  // 节点中心
                }}
                selected={isSelected}
                onClick={() => handleEdgeClick(edge.id)}
                onDoubleClick={() => handleEdgeDoubleClick(edge.id)}
              />
            );
          })}
        </g>

        {/* 连接线 */}
        {connectionStart && connectionEnd && (
          <ConnectionLine
            start={connectionStart.position}
            end={connectionEnd}
          />
        )}
      </svg>

      {/* 节点 */}
      <div className="nodes-container">
        {nodes.map(node => {
          const isSelected = selectedElements.some(el => el.id === node.id && el.type === 'node');
          const isDraggingThis = dragNode === node.id;

          return (
            <WorkflowNodeComponent
              key={node.id}
              node={node}
              position={{
                x: (node.position.x - viewBox.x) * zoom,
                y: (node.position.y - viewBox.y) * zoom,
              }}
              zoom={zoom}
              selected={isSelected}
              dragging={isDraggingThis}
              onSelect={() => onNodeSelect(node)}
              onUpdate={(properties) => onNodeUpdate(node.id, properties)}
              onConnectionStart={(portId, position) => handleConnectionStart(node.id, portId, position)}
            />
          );
        })}
      </div>

      {/* 画布信息 */}
      <div className="canvas-info">
        <div className="zoom-info">缩放: {Math.round(zoom * 100)}%</div>
        <div className="position-info">
          位置: ({Math.round(mousePosition.x)}, {Math.round(mousePosition.y)})
        </div>
        <div className="nodes-count">节点: {nodes.length}</div>
        <div className="edges-count">连接: {edges.length}</div>
      </div>
    </div>
  );
});

WorkflowCanvas.displayName = 'WorkflowCanvas';