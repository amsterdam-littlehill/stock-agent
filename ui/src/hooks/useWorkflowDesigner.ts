import { useState, useCallback } from 'react';
import { message } from 'antd';
import type {
  WorkflowNode,
  WorkflowEdge,
  WorkflowGraph,
  Position,
  WorkflowValidationResult,
  NodeValidationResult,
  WorkflowNodeType
} from '@/types/workflow';
import { generateId } from '@/utils/common';

interface SelectedElement {
  id: string;
  type: 'node' | 'edge';
}

interface HistoryState {
  nodes: WorkflowNode[];
  edges: WorkflowEdge[];
}

// 工作流设计器 Hook
export const useWorkflowDesigner = () => {
  const [nodes, setNodes] = useState<WorkflowNode[]>([]);
  const [edges, setEdges] = useState<WorkflowEdge[]>([]);
  const [selectedElements, setSelectedElements] = useState<SelectedElement[]>([]);
  const [zoom, setZoom] = useState(1);
  const [history, setHistory] = useState<HistoryState[]>([]);
  const [historyIndex, setHistoryIndex] = useState(-1);
  const maxHistorySize = 50;

  // 保存历史状态
  const saveHistory = useCallback(() => {
    const newState = {
      nodes: [...nodes],
      edges: [...edges]
    };
    const newHistory = history.slice(0, historyIndex + 1);
    newHistory.push(newState);

    if (newHistory.length > maxHistorySize) {
      newHistory.shift();
    } else {
      setHistoryIndex(prev => prev + 1);
    }

    setHistory(newHistory);
  }, [nodes, edges, history, historyIndex]);

  // 添加节点
  const addNode = useCallback((nodeType: WorkflowNodeType, position: Position) => {
    saveHistory();

    const newNode: WorkflowNode = {
      id: generateId(),
      type: nodeType,
      name: getDefaultNodeName(nodeType),
      position,
      properties: getDefaultNodeProperties(nodeType),
      inputs: getDefaultNodeInputs(nodeType),
      outputs: getDefaultNodeOutputs(nodeType),
    };

    setNodes(prev => [...prev, newNode]);
    message.success('节点添加成功');
  }, [saveHistory]);

  // 更新节点
  const updateNode = useCallback((nodeId: string, updates: Partial<WorkflowNode>) => {
    setNodes(prev => prev.map(node =>
      node.id === nodeId ? {
        ...node,
        ...updates
      } : node
    ));
  }, []);

  // 删除节点
  const deleteNode = useCallback((nodeId: string) => {
    saveHistory();

    setNodes(prev => prev.filter(node => node.id !== nodeId));
    setEdges(prev => prev.filter(edge =>
      edge.sourceNodeId !== nodeId && edge.targetNodeId !== nodeId
    ));
    setSelectedElements(prev => prev.filter(el => el.id !== nodeId));

    message.success('节点删除成功');
  }, [saveHistory]);

  // 添加边
  const addEdge = useCallback((edgeData: Omit<WorkflowEdge, 'id'>) => {
    // 检查是否已存在相同的连接
    const existingEdge = edges.find(edge =>
      edge.sourceNodeId === edgeData.sourceNodeId &&
      edge.sourcePortId === edgeData.sourcePortId &&
      edge.targetNodeId === edgeData.targetNodeId &&
      edge.targetPortId === edgeData.targetPortId
    );

    if (existingEdge) {
      message.warning('连接已存在');
      return;
    }

    // 检查是否会形成循环
    if (wouldCreateCycle(edgeData.sourceNodeId, edgeData.targetNodeId)) {
      message.error('不能创建循环连接');
      return;
    }

    saveHistory();

    const newEdge: WorkflowEdge = {
      id: generateId(),
      ...edgeData,
    };

    setEdges(prev => [...prev, newEdge]);
    message.success('连接创建成功');
  }, [edges, saveHistory]);

  // 删除边
  const deleteEdge = useCallback((edgeId: string) => {
    saveHistory();

    setEdges(prev => prev.filter(edge => edge.id !== edgeId));
    setSelectedElements(prev => prev.filter(el => el.id !== edgeId));

    message.success('连接删除成功');
  }, [saveHistory]);

  // 选择元素
  const selectElement = useCallback((elementId: string, type: 'node' | 'edge', multiSelect = false) => {
    if (multiSelect) {
      setSelectedElements(prev => {
        const exists = prev.find(el => el.id === elementId);
        if (exists) {
          return prev.filter(el => el.id !== elementId);
        } else {
          return [...prev, {
            id: elementId,
            type
          }];
        }
      });
    } else {
      setSelectedElements([{
        id: elementId,
        type
      }]);
    }
  }, []);

  // 清除选择
  const clearSelection = useCallback(() => {
    setSelectedElements([]);
  }, []);

  // 撤销
  const undo = useCallback(() => {
    if (historyIndex > 0) {
      const prevState = history[historyIndex - 1];
      setNodes(prevState.nodes);
      setEdges(prevState.edges);
      setHistoryIndex(prev => prev - 1);
      setSelectedElements([]);
    }
  }, [history, historyIndex]);

  // 重做
  const redo = useCallback(() => {
    if (historyIndex < history.length - 1) {
      const nextState = history[historyIndex + 1];
      setNodes(nextState.nodes);
      setEdges(nextState.edges);
      setHistoryIndex(prev => prev + 1);
      setSelectedElements([]);
    }
  }, [history, historyIndex]);

  // 缩放
  const zoomIn = useCallback(() => {
    setZoom(prev => Math.min(3, prev * 1.2));
  }, []);

  const zoomOut = useCallback(() => {
    setZoom(prev => Math.max(0.1, prev / 1.2));
  }, []);

  const resetZoom = useCallback(() => {
    setZoom(1);
  }, []);

  // 适应屏幕
  const fitToScreen = useCallback(() => {
    if (nodes.length === 0) return;

    const padding = 50;
    const minX = Math.min(...nodes.map(n => n.position.x)) - padding;
    const maxX = Math.max(...nodes.map(n => n.position.x + 200)) + padding;
    const minY = Math.min(...nodes.map(n => n.position.y)) - padding;
    const maxY = Math.max(...nodes.map(n => n.position.y + 100)) + padding;

    const width = maxX - minX;
    const height = maxY - minY;

    // 计算合适的缩放比例
    const containerWidth = window.innerWidth - 600; // 减去侧边栏宽度
    const containerHeight = window.innerHeight - 200; // 减去头部高度

    const scaleX = containerWidth / width;
    const scaleY = containerHeight / height;
    const scale = Math.min(scaleX, scaleY, 1); // 不超过100%

    setZoom(scale);
  }, [nodes]);

  // 验证工作流
  const validateWorkflow = useCallback((): WorkflowValidationResult => {
    const errors: string[] = [];
    const warnings: string[] = [];

    // 检查是否有节点
    if (nodes.length === 0) {
      errors.push('工作流至少需要一个节点');
      return {
        isValid: false,
        errors,
        warnings
      };
    }

    // 检查开始节点
    const startNodes = nodes.filter(node => node.type === 'START');
    if (startNodes.length === 0) {
      errors.push('工作流必须有一个开始节点');
    } else if (startNodes.length > 1) {
      errors.push('工作流只能有一个开始节点');
    }

    // 检查结束节点
    const endNodes = nodes.filter(node => node.type === 'END');
    if (endNodes.length === 0) {
      warnings.push('建议添加结束节点');
    }

    // 检查孤立节点
    const isolatedNodes = nodes.filter(node => {
      const hasIncoming = edges.some(edge => edge.targetNodeId === node.id);
      const hasOutgoing = edges.some(edge => edge.sourceNodeId === node.id);
      return !hasIncoming && !hasOutgoing && node.type !== 'START';
    });

    if (isolatedNodes.length > 0) {
      warnings.push(`发现 ${isolatedNodes.length} 个孤立节点`);
    }

    // 检查节点配置
    nodes.forEach(node => {
      const nodeValidation = validateNode(node);
      if (!nodeValidation.isValid) {
        errors.push(...nodeValidation.errors.map(err => `节点 ${node.name}: ${err}`));
      }
      warnings.push(...nodeValidation.warnings.map(warn => `节点 ${node.name}: ${warn}`));
    });

    return {
      isValid: errors.length === 0,
      errors,
      warnings
    };
  }, [nodes, edges]);

  // 导出工作流
  const exportWorkflow = useCallback((): WorkflowGraph => {
    return {
      nodes,
      edges,
      variables: [],
      settings: {
        timeout: 300000, // 5分钟
        retryCount: 3,
        parallelism: 1,
        errorHandling: 'STOP',
        notifications: {
          onSuccess: false,
          onFailure: true,
          onStart: false,
        },
      },
    };
  }, [nodes, edges]);

  // 导入工作流
  const importWorkflow = useCallback((workflowGraph: WorkflowGraph) => {
    saveHistory();
    setNodes(workflowGraph.nodes || []);
    setEdges(workflowGraph.edges || []);
    setSelectedElements([]);
    message.success('工作流导入成功');
  }, [saveHistory]);

  // 清空工作流
  const clearWorkflow = useCallback(() => {
    saveHistory();
    setNodes([]);
    setEdges([]);
    setSelectedElements([]);
    message.success('工作流已清空');
  }, [saveHistory]);

  // 复制选中元素
  const copySelectedElements = useCallback(() => {
    const selectedNodes = nodes.filter(node =>
      selectedElements.some(el => el.id === node.id && el.type === 'node')
    );

    if (selectedNodes.length > 0) {
      const copiedData = JSON.stringify(selectedNodes);
      navigator.clipboard.writeText(copiedData);
      message.success(`已复制 ${selectedNodes.length} 个节点`);
    }
  }, [nodes, selectedElements]);

  // 粘贴元素
  const pasteElements = useCallback(async () => {
    try {
      const clipboardText = await navigator.clipboard.readText();
      const copiedNodes = JSON.parse(clipboardText) as WorkflowNode[];

      if (Array.isArray(copiedNodes) && copiedNodes.length > 0) {
        saveHistory();

        const newNodes = copiedNodes.map(node => ({
          ...node,
          id: generateId(),
          position: {
            x: node.position.x + 50,
            y: node.position.y + 50,
          },
        }));

        setNodes(prev => [...prev, ...newNodes]);
        message.success(`已粘贴 ${newNodes.length} 个节点`);
      }
    } catch {
      message.error('粘贴失败，剪贴板内容无效');
    }
  }, [saveHistory]);

  return {
    // 状态
    nodes,
    edges,
    selectedElements,
    zoom,
    canUndo: historyIndex > 0,
    canRedo: historyIndex < history.length - 1,

    // 节点操作
    addNode,
    updateNode,
    deleteNode,

    // 边操作
    addEdge,
    deleteEdge,

    // 选择操作
    selectElement,
    clearSelection,

    // 历史操作
    undo,
    redo,

    // 缩放操作
    zoomIn,
    zoomOut,
    resetZoom,
    fitToScreen,

    // 工作流操作
    validateWorkflow,
    exportWorkflow,
    importWorkflow,
    clearWorkflow,

    // 剪贴板操作
    copySelectedElements,
    pasteElements,
  };
};

// 辅助函数

// 检查是否会形成循环
function wouldCreateCycle(sourceNodeId: string, targetNodeId: string): boolean {
  // 简化的循环检测，实际应该使用深度优先搜索
  return sourceNodeId === targetNodeId;
}

// 获取默认节点名称
function getDefaultNodeName(nodeType: WorkflowNodeType): string {
  const nameMap = {
    START: '开始',
    END: '结束',
    CONDITION: '条件判断',
    LOOP: '循环',
    PARALLEL: '并行',
    MERGE: '合并',
    DELAY: '延迟',
    INPUT: '输入',
    OUTPUT: '输出',
    VARIABLE: '变量',
    TRANSFORM: '数据转换',
    FILTER: '数据过滤',
    AGGREGATE: '数据聚合',
    AGENT_CALL: '智能体调用',
    AGENT_CHAIN: '智能体链',
    MCP_TOOL: 'MCP工具',
    HTTP_REQUEST: 'HTTP请求',
    DATABASE_QUERY: '数据库查询',
    FILE_OPERATION: '文件操作',
    EMAIL: '邮件发送',
    STOCK_DATA: '股票数据',
    STOCK_ANALYSIS: '股票分析',
    MARKET_DATA: '市场数据',
    NEWS_FETCH: '新闻获取',
    JAVASCRIPT: 'JavaScript脚本',
    PYTHON: 'Python脚本',
    GROOVY: 'Groovy脚本',
    NOTIFICATION: '通知',
    WEBHOOK: 'Webhook',
    CUSTOM: '自定义节点',
  };

  return nameMap[nodeType] || nodeType;
}

// 获取默认节点属性
function getDefaultNodeProperties(nodeType: WorkflowNodeType): Record<string, any> {
  const defaultProps = {
    START: {},
    END: {},
    CONDITION: { expression: '' },
    LOOP: { maxIterations: 10 },
    PARALLEL: { maxConcurrency: 3 },
    MERGE: {},
    DELAY: { duration: 1000 },
    INPUT: { dataType: 'STRING' },
    OUTPUT: { dataType: 'STRING' },
    VARIABLE: {
      name: '',
      value: ''
    },
    TRANSFORM: { script: '' },
    FILTER: { condition: '' },
    AGGREGATE: { operation: 'SUM' },
    AGENT_CALL: {
      agentType: '',
      parameters: {}
    },
    AGENT_CHAIN: { agents: [] },
    MCP_TOOL: {
      toolName: '',
      parameters: {}
    },
    HTTP_REQUEST: {
      url: '',
      method: 'GET'
    },
    DATABASE_QUERY: { query: '' },
    FILE_OPERATION: {
      operation: 'READ',
      path: ''
    },
    EMAIL: {
      to: '',
      subject: '',
      body: ''
    },
    STOCK_DATA: {
      symbol: '',
      dataType: 'PRICE'
    },
    STOCK_ANALYSIS: {
      symbol: '',
      analysisType: 'TECHNICAL'
    },
    MARKET_DATA: {
      market: 'US',
      dataType: 'INDEX'
    },
    NEWS_FETCH: {
      keywords: '',
      source: ''
    },
    JAVASCRIPT: { script: '' },
    PYTHON: { script: '' },
    GROOVY: { script: '' },
    NOTIFICATION: {
      message: '',
      channel: 'EMAIL'
    },
    WEBHOOK: {
      url: '',
      payload: {}
    },
    CUSTOM: {},
  };

  return defaultProps[nodeType] || {};
}

// 获取默认输入端口
function getDefaultNodeInputs(nodeType: WorkflowNodeType) {
  if (nodeType === 'START') return [];

  return [{
    id: 'input',
    name: '输入',
    type: 'INPUT' as const,
    dataType: 'ANY' as const,
    required: false,
  }];
}

// 获取默认输出端口
function getDefaultNodeOutputs(nodeType: WorkflowNodeType) {
  if (nodeType === 'END') return [];

  return [{
    id: 'output',
    name: '输出',
    type: 'OUTPUT' as const,
    dataType: 'ANY' as const,
    required: false,
  }];
}

// 验证单个节点
function validateNode(node: WorkflowNode): NodeValidationResult {
  const errors: string[] = [];
  const warnings: string[] = [];

  // 检查节点名称
  if (!node.name || node.name.trim() === '') {
    errors.push('节点名称不能为空');
  }

  // 根据节点类型进行特定验证
  switch (node.type) {
    case 'CONDITION':
      if (!node.properties.expression) {
        errors.push('条件表达式不能为空');
      }
      break;
    case 'HTTP_REQUEST':
      if (!node.properties.url) {
        errors.push('请求URL不能为空');
      }
      break;
    case 'DATABASE_QUERY':
      if (!node.properties.query) {
        errors.push('数据库查询语句不能为空');
      }
      break;
    case 'EMAIL':
      if (!node.properties.to || !node.properties.subject) {
        errors.push('邮件收件人和主题不能为空');
      }
      break;
    case 'STOCK_DATA':
    case 'STOCK_ANALYSIS':
      if (!node.properties.symbol) {
        errors.push('股票代码不能为空');
      }
      break;
  }

  return {
    nodeId: node.id,
    isValid: errors.length === 0,
    errors,
    warnings,
  };
}