import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Space, Tag, message, Modal, Tooltip } from 'antd';
import { PlusOutlined, EditOutlined, PlayCircleOutlined, DeleteOutlined, CopyOutlined, EyeOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { workflowService } from '@/services/workflow';
import type { WorkflowDefinition } from '@/types/workflow';
import './index.css';

const { confirm } = Modal;

// 工作流管理页面
const Workflow: React.FC = () => {
  const navigate = useNavigate();
  const [workflows, setWorkflows] = useState<WorkflowDefinition[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

  // 获取工作流列表
  const fetchWorkflows = async () => {
    setLoading(true);
    try {
      const response = await workflowService.getWorkflows();
      setWorkflows(response.data);
    } catch {
      message.error('获取工作流列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWorkflows();
  }, []);

  // 执行工作流
  const handleExecute = async (workflowId: string) => {
    try {
      const response = await workflowService.executeWorkflow(workflowId, {});
      message.success('工作流执行已启动');
      navigate(`/workflow/execution/${response.data.executionId}`);
    } catch {
      message.error('启动工作流失败');
    }
  };

  // 删除工作流
  const handleDelete = (workflowId: string) => {
    confirm({
      title: '确认删除',
      content: '确定要删除这个工作流吗？此操作不可恢复。',
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        try {
          await workflowService.deleteWorkflow(workflowId);
          message.success('删除成功');
          fetchWorkflows();
        } catch {
          message.error('删除失败');
        }
      },
    });
  };

  // 克隆工作流
  const handleClone = async (workflowId: string) => {
    try {
      await workflowService.cloneWorkflow(workflowId);
      message.success('克隆成功');
      fetchWorkflows();
    } catch {
      message.error('克隆失败');
    }
  };

  // 批量删除
  const handleBatchDelete = () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要删除的工作流');
      return;
    }

    confirm({
      title: '批量删除确认',
      content: `确定要删除选中的 ${selectedRowKeys.length} 个工作流吗？此操作不可恢复。`,
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        try {
          await Promise.all(
            selectedRowKeys.map(id => workflowService.deleteWorkflow(id as string))
          );
          message.success('批量删除成功');
          setSelectedRowKeys([]);
          fetchWorkflows();
        } catch {
          message.error('批量删除失败');
        }
      },
    });
  };

  // 表格列定义
  const columns = [
    {
      title: '工作流名称',
      dataIndex: 'name',
      key: 'name',
      render: (text: string, record: WorkflowDefinition) => (
        <div>
          <div className="font-medium">{text}</div>
          <div className="text-gray-500 text-sm">{record.description}</div>
        </div>
      ),
    },
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      width: 80,
      render: (version: string) => <Tag color="blue">v{version}</Tag>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const statusConfig = {
          ACTIVE: {
            color: 'green',
            text: '活跃'
          },
          INACTIVE: {
            color: 'red',
            text: '停用'
          },
          DRAFT: {
            color: 'orange',
            text: '草稿'
          },
        };
        const config = statusConfig[status as keyof typeof statusConfig] || {
          color: 'default',
          text: status
        };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '节点数',
      dataIndex: 'nodeCount',
      key: 'nodeCount',
      width: 80,
      render: (count: number) => <span className="text-blue-600">{count}</span>,
    },
    {
      title: '执行次数',
      dataIndex: 'executionCount',
      key: 'executionCount',
      width: 100,
      render: (count: number) => <span className="text-green-600">{count}</span>,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 150,
      render: (date: string) => new Date(date).toLocaleString(),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: any, record: WorkflowDefinition) => (
        <Space size="small">
          <Tooltip title="查看详情">
            <Button
              type="text"
              icon={<EyeOutlined />}
              onClick={() => navigate(`/workflow/detail/${record.id}`)}
            />
          </Tooltip>
          <Tooltip title="编辑">
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => navigate(`/workflow/designer/${record.id}`)}
            />
          </Tooltip>
          <Tooltip title="执行">
            <Button
              type="text"
              icon={<PlayCircleOutlined />}
              onClick={() => handleExecute(record.id)}
              disabled={record.status !== 'ACTIVE'}
            />
          </Tooltip>
          <Tooltip title="克隆">
            <Button
              type="text"
              icon={<CopyOutlined />}
              onClick={() => handleClone(record.id)}
            />
          </Tooltip>
          <Tooltip title="删除">
            <Button
              type="text"
              danger
              icon={<DeleteOutlined />}
              onClick={() => handleDelete(record.id)}
            />
          </Tooltip>
        </Space>
      ),
    },
  ];

  // 行选择配置
  const rowSelection = {
    selectedRowKeys,
    onChange: (newSelectedRowKeys: React.Key[]) => {
      setSelectedRowKeys(newSelectedRowKeys);
    },
  };

  return (
    <div className="workflow-page">
      <Card>
        <div className="flex justify-between items-center mb-4">
          <div>
            <h2 className="text-xl font-semibold">工作流管理</h2>
            <p className="text-gray-600">管理和执行您的自定义工作流</p>
          </div>
          <Space>
            {selectedRowKeys.length > 0 && (
              <Button danger onClick={handleBatchDelete}>
                批量删除 ({selectedRowKeys.length})
              </Button>
            )}
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => navigate('/workflow/designer/new')}
            >
              创建工作流
            </Button>
          </Space>
        </div>

        <Table
          rowSelection={rowSelection}
          columns={columns}
          dataSource={workflows}
          rowKey="id"
          loading={loading}
          pagination={{
            total: workflows.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
          }}
        />
      </Card>
    </div>
  );
};

export default Workflow;