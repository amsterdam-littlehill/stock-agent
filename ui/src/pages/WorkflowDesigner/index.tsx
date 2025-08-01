import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Button, Space, message, Modal, Form, Input, Select, Drawer, Tooltip, Divider } from 'antd';
import { SaveOutlined, PlayCircleOutlined, UndoOutlined, RedoOutlined, ZoomInOutlined, ZoomOutOutlined, FullscreenOutlined } from '@ant-design/icons';
import { useParams, useNavigate } from 'react-router-dom';
import { workflowService } from '@/services/workflow';
import { WorkflowCanvas } from '@/components/WorkflowDesigner/WorkflowCanvas';
import { NodePalette } from '@/components/WorkflowDesigner/NodePalette';
import { PropertyPanel } from '@/components/WorkflowDesigner/PropertyPanel';
// import { WorkflowToolbar } from '@/components/WorkflowDesigner/WorkflowToolbar';
import { useWorkflowDesigner } from '@/hooks/useWorkflowDesigner';
import type { WorkflowDefinition, WorkflowNode } from '@/types/workflow';
import './index.css';

const { TextArea } = Input;
const { Option } = Select;

// 工作流设计器页面
const WorkflowDesigner: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const canvasRef = useRef<HTMLDivElement>(null);

  const [workflow, setWorkflow] = useState<WorkflowDefinition | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [saveModalVisible, setSaveModalVisible] = useState(false);
  const [propertyDrawerVisible, setPropertyDrawerVisible] = useState(false);
  const [selectedNode, setSelectedNode] = useState<WorkflowNode | null>(null);
  const [form] = Form.useForm();

  // 使用工作流设计器 Hook
  const {
    nodes,
    edges,
    selectedElements,
    canUndo,
    canRedo,
    zoom,
    addNode,
    updateNode,
    deleteNode,
    addEdge,
    deleteEdge,
    selectElement,
    clearSelection,
    undo,
    redo,
    zoomIn,
    zoomOut,
    fitToScreen,
    validateWorkflow,
    exportWorkflow,
    importWorkflow,
  } = useWorkflowDesigner();

  // 加载工作流数据
  const loadWorkflow = useCallback(async () => {
    if (!id || id === 'new') return;

    setLoading(true);
    try {
      const response = await workflowService.getWorkflow(id);
      const workflowData = response.data;
      setWorkflow(workflowData);

      // 导入工作流到设计器
      if (workflowData.definition) {
        importWorkflow(workflowData.definition);
      }

      // 设置表单初始值
      form.setFieldsValue({
        name: workflowData.name,
        description: workflowData.description,
        version: workflowData.version,
        tags: workflowData.tags,
      });
    } catch {
      message.error('加载工作流失败');
      navigate('/workflow');
    } finally {
      setLoading(false);
    }
  }, [id, navigate, form, importWorkflow]);

  useEffect(() => {
    loadWorkflow();
  }, [loadWorkflow]);

  // 保存工作流
  const handleSave = async (values: any) => {
    // 验证工作流
    const validation = validateWorkflow();
    if (!validation.isValid) {
      message.error(`工作流验证失败: ${validation.errors.join(', ')}`);
      return;
    }

    setSaving(true);
    try {
      const workflowDefinition = exportWorkflow();
      const workflowData = {
        ...values,
        definition: workflowDefinition,
        nodeCount: nodes.length,
        status: 'DRAFT',
      };

      if (id && id !== 'new') {
        // 更新现有工作流
        await workflowService.updateWorkflow(id, workflowData);
        message.success('工作流保存成功');
      } else {
        // 创建新工作流
        const response = await workflowService.createWorkflow(workflowData);
        message.success('工作流创建成功');
        navigate(`/workflow/designer/${response.data.id}`, { replace: true });
      }

      setSaveModalVisible(false);
      loadWorkflow();
    } catch {
      message.error('保存工作流失败');
    } finally {
      setSaving(false);
    }
  };

  // 测试运行工作流
  const handleTestRun = async () => {
    if (!workflow?.id) {
      message.warning('请先保存工作流');
      return;
    }

    // 验证工作流
    const validation = validateWorkflow();
    if (!validation.isValid) {
      message.error(`工作流验证失败: ${validation.errors.join(', ')}`);
      return;
    }

    try {
      const response = await workflowService.executeWorkflow(workflow.id, {testMode: true,});
      message.success('测试运行已启动');
      navigate(`/workflow/execution/${response.data.executionId}`);
    } catch {
      message.error('启动测试运行失败');
    }
  };

  // 处理节点选择
  const handleNodeSelect = (node: WorkflowNode) => {
    setSelectedNode(node);
    setPropertyDrawerVisible(true);
    selectElement(node.id, 'node');
  };

  // 处理节点属性更新
  const handleNodeUpdate = (nodeId: string, properties: any) => {
    updateNode(nodeId, properties);
    if (selectedNode?.id === nodeId) {
      setSelectedNode({
        ...selectedNode,
        ...properties
      });
    }
  };

  // 处理画布点击
  const handleCanvasClick = (event: React.MouseEvent) => {
    if (event.target === event.currentTarget) {
      clearSelection();
      setPropertyDrawerVisible(false);
      setSelectedNode(null);
    }
  };

  // 键盘快捷键处理
  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.ctrlKey || event.metaKey) {
        switch (event.key) {
          case 's':
            event.preventDefault();
            setSaveModalVisible(true);
            break;
          case 'z':
            event.preventDefault();
            if (event.shiftKey) {
              redo();
            } else {
              undo();
            }
            break;
          case '=':
          case '+':
            event.preventDefault();
            zoomIn();
            break;
          case '-':
            event.preventDefault();
            zoomOut();
            break;
          case '0':
            event.preventDefault();
            fitToScreen();
            break;
        }
      } else if (event.key === 'Delete' || event.key === 'Backspace') {
        if (selectedElements.length > 0) {
          selectedElements.forEach(element => {
            if (element.type === 'node') {
              deleteNode(element.id);
            } else if (element.type === 'edge') {
              deleteEdge(element.id);
            }
          });
          clearSelection();
        }
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [selectedElements, undo, redo, zoomIn, zoomOut, fitToScreen, deleteNode, deleteEdge, clearSelection]);

  return (
    <div className="workflow-designer">
      {/* 顶部工具栏 */}
      <div className="designer-header">
        <div className="header-left">
          <h2 className="designer-title">
            {workflow?.name || '新建工作流'}
          </h2>
          <span className="designer-subtitle">
            {workflow?.description || '设计您的自定义工作流'}
          </span>
        </div>

        <div className="header-right">
          <Space>
            <Tooltip title="撤销 (Ctrl+Z)">
              <Button
                icon={<UndoOutlined />}
                disabled={!canUndo}
                onClick={undo}
              />
            </Tooltip>
            <Tooltip title="重做 (Ctrl+Shift+Z)">
              <Button
                icon={<RedoOutlined />}
                disabled={!canRedo}
                onClick={redo}
              />
            </Tooltip>
            <Divider type="vertical" />
            <Tooltip title="缩小 (Ctrl+-)">
              <Button
                icon={<ZoomOutOutlined />}
                onClick={zoomOut}
              />
            </Tooltip>
            <span className="zoom-level">{Math.round(zoom * 100)}%</span>
            <Tooltip title="放大 (Ctrl++)">
              <Button
                icon={<ZoomInOutlined />}
                onClick={zoomIn}
              />
            </Tooltip>
            <Tooltip title="适应屏幕 (Ctrl+0)">
              <Button
                icon={<FullscreenOutlined />}
                onClick={fitToScreen}
              />
            </Tooltip>
            <Divider type="vertical" />
            <Button
              icon={<PlayCircleOutlined />}
              onClick={handleTestRun}
              disabled={nodes.length === 0}
            >
              测试运行
            </Button>
            <Button
              type="primary"
              icon={<SaveOutlined />}
              onClick={() => setSaveModalVisible(true)}
              loading={saving}
            >
              保存
            </Button>
          </Space>
        </div>
      </div>

      {/* 主要内容区域 */}
      <div className="designer-content">
        {/* 左侧节点面板 */}
        <div className="designer-sidebar left">
          <NodePalette onNodeAdd={addNode} />
        </div>

        {/* 中间画布区域 */}
        <div className="designer-main">
          <WorkflowCanvas
            ref={canvasRef}
            nodes={nodes}
            edges={edges}
            selectedElements={selectedElements}
            zoom={zoom}
            onNodeSelect={handleNodeSelect}
            onNodeUpdate={handleNodeUpdate}
            onEdgeAdd={addEdge}
            onEdgeDelete={deleteEdge}
            onCanvasClick={handleCanvasClick}
            loading={loading}
          />
        </div>

        {/* 右侧属性面板 */}
        <Drawer
          title="节点属性"
          placement="right"
          width={400}
          open={propertyDrawerVisible}
          onClose={() => setPropertyDrawerVisible(false)}
          mask={false}
          getContainer={false}
          style={{ position: 'absolute' }}
        >
          {selectedNode && (
            <PropertyPanel
              node={selectedNode}
              onUpdate={(properties) => handleNodeUpdate(selectedNode.id, properties)}
            />
          )}
        </Drawer>
      </div>

      {/* 保存对话框 */}
      <Modal
        title={id === 'new' ? '创建工作流' : '保存工作流'}
        open={saveModalVisible}
        onCancel={() => setSaveModalVisible(false)}
        footer={null}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSave}
          initialValues={{
            version: '1.0.0',
            tags: [],
          }}
        >
          <Form.Item
            name="name"
            label="工作流名称"
            rules={[{
              required: true,
              message: '请输入工作流名称'
            }]}
          >
            <Input placeholder="请输入工作流名称" />
          </Form.Item>

          <Form.Item
            name="description"
            label="描述"
            rules={[{
              required: true,
              message: '请输入工作流描述'
            }]}
          >
            <TextArea
              rows={3}
              placeholder="请输入工作流描述"
            />
          </Form.Item>

          <Form.Item
            name="version"
            label="版本"
            rules={[{
              required: true,
              message: '请输入版本号'
            }]}
          >
            <Input placeholder="例如: 1.0.0" />
          </Form.Item>

          <Form.Item
            name="tags"
            label="标签"
          >
            <Select
              mode="tags"
              placeholder="请输入标签"
              tokenSeparators={[',']}
            >
              <Option value="股票分析">股票分析</Option>
              <Option value="数据处理">数据处理</Option>
              <Option value="自动化">自动化</Option>
              <Option value="监控">监控</Option>
            </Select>
          </Form.Item>

          <Form.Item className="form-actions">
            <Space>
              <Button onClick={() => setSaveModalVisible(false)}>
                取消
              </Button>
              <Button type="primary" htmlType="submit" loading={saving}>
                {id === 'new' ? '创建' : '保存'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default WorkflowDesigner;