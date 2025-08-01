import React, { useEffect } from 'react';
import { Card, Form, Input, Select, Switch, InputNumber, Space, Button, Divider } from 'antd';
import { DeleteOutlined, CopyOutlined } from '@ant-design/icons';
import type { WorkflowNode, WorkflowNodeType } from '@/types/workflow';

const { TextArea } = Input;
const { Option } = Select;

interface PropertyPanelProps {
  selectedNode: WorkflowNode | null;
  onNodeUpdate?: (nodeId: string, updates: Partial<WorkflowNode>) => void;
  onNodeDelete?: (nodeId: string) => void;
  onNodeCopy?: (nodeId: string) => void;
}

// 节点类型配置
const nodeTypeOptions = [
  {
    value: WorkflowNodeType.START,
    label: '开始节点'
  },
  {
    value: WorkflowNodeType.END,
    label: '结束节点'
  },
  {
    value: WorkflowNodeType.AGENT_CALL,
    label: '智能体调用'
  },
  {
    value: WorkflowNodeType.CONDITION,
    label: '条件节点'
  },
  {
    value: WorkflowNodeType.HTTP_REQUEST,
    label: 'HTTP请求'
  },
  {
    value: WorkflowNodeType.DATABASE_QUERY,
    label: '数据库查询'
  },
  {
    value: WorkflowNodeType.NOTIFICATION,
    label: '通知节点'
  },
  {
    value: WorkflowNodeType.EMAIL,
    label: '邮件发送'
  }
];

export const PropertyPanel: React.FC<PropertyPanelProps> = ({
  selectedNode,
  onNodeUpdate,
  onNodeDelete,
  onNodeCopy
}) => {
  const [form] = Form.useForm();

  useEffect(() => {
    if (selectedNode) {
      form.setFieldsValue({
        id: selectedNode.id,
        type: selectedNode.type,
        label: selectedNode.data?.label || '',
        description: selectedNode.data?.description || '',
        config: selectedNode.data?.config || {},
        timeout: selectedNode.data?.timeout || 30,
        retryCount: selectedNode.data?.retryCount || 0,
        enabled: selectedNode.data?.enabled !== false
      });
    } else {
      form.resetFields();
    }
  }, [selectedNode, form]);

  const handleFormChange = (changedFields: any, allFields: any) => {
    if (!selectedNode || !onNodeUpdate) return;

    const updates: Partial<WorkflowNode> = {
      type: allFields.type,
      data: {
        ...selectedNode.data,
        label: allFields.label,
        description: allFields.description,
        config: allFields.config || {},
        timeout: allFields.timeout,
        retryCount: allFields.retryCount,
        enabled: allFields.enabled
      }
    };

    onNodeUpdate(selectedNode.id, updates);
  };

  const handleDelete = () => {
    if (selectedNode && onNodeDelete) {
      onNodeDelete(selectedNode.id);
    }
  };

  const handleCopy = () => {
    if (selectedNode && onNodeCopy) {
      onNodeCopy(selectedNode.id);
    }
  };

  if (!selectedNode) {
    return (
      <Card
        title="属性面板"
        size="small"
        style={{ height: '100%' }}
        bodyStyle={{
          padding: '16px',
          textAlign: 'center'
        }}
      >
        <p style={{
          color: '#999',
          marginTop: '50px'
        }}>
          请选择一个节点来编辑属性
        </p>
      </Card>
    );
  }

  return (
    <Card
      title="属性面板"
      size="small"
      style={{
        height: '100%',
        overflow: 'auto'
      }}
      bodyStyle={{ padding: '16px' }}
      extra={
        <Space>
          <Button
            type="text"
            size="small"
            icon={<CopyOutlined />}
            onClick={handleCopy}
            title="复制节点"
          />
          <Button
            type="text"
            size="small"
            danger
            icon={<DeleteOutlined />}
            onClick={handleDelete}
            title="删除节点"
          />
        </Space>
      }
    >
      <Form
        form={form}
        layout="vertical"
        size="small"
        onValuesChange={handleFormChange}
      >
        <Form.Item label="节点ID" name="id">
          <Input disabled />
        </Form.Item>

        <Form.Item label="节点类型" name="type">
          <Select>
            {nodeTypeOptions.map(option => (
              <Option key={option.value} value={option.value}>
                {option.label}
              </Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item label="节点名称" name="label">
          <Input placeholder="请输入节点名称" />
        </Form.Item>

        <Form.Item label="节点描述" name="description">
          <TextArea
            rows={3}
            placeholder="请输入节点描述"
          />
        </Form.Item>

        <Divider orientation="left" style={{ margin: '16px 0 12px 0' }}>
          执行配置
        </Divider>

        <Form.Item label="启用状态" name="enabled" valuePropName="checked">
          <Switch />
        </Form.Item>

        <Form.Item label="超时时间(秒)" name="timeout">
          <InputNumber
            min={1}
            max={3600}
            style={{ width: '100%' }}
            placeholder="30"
          />
        </Form.Item>

        <Form.Item label="重试次数" name="retryCount">
          <InputNumber
            min={0}
            max={10}
            style={{ width: '100%' }}
            placeholder="0"
          />
        </Form.Item>

        {selectedNode.type === WorkflowNodeType.HTTP_REQUEST && (
          <>
            <Divider orientation="left" style={{ margin: '16px 0 12px 0' }}>
              HTTP请求配置
            </Divider>

            <Form.Item label="请求URL" name={['config', 'url']}>
              <Input placeholder="https://api.example.com/endpoint" />
            </Form.Item>

            <Form.Item label="请求方法" name={['config', 'method']}>
              <Select defaultValue="GET">
                <Option value="GET">GET</Option>
                <Option value="POST">POST</Option>
                <Option value="PUT">PUT</Option>
                <Option value="DELETE">DELETE</Option>
              </Select>
            </Form.Item>

            <Form.Item label="请求头" name={['config', 'headers']}>
              <TextArea
                rows={3}
                placeholder='{"Content-Type": "application/json"}'
              />
            </Form.Item>

            <Form.Item label="请求体" name={['config', 'body']}>
              <TextArea
                rows={4}
                placeholder="请求体内容（JSON格式）"
              />
            </Form.Item>
          </>
        )}

        {selectedNode.type === WorkflowNodeType.DATABASE_QUERY && (
          <>
            <Divider orientation="left" style={{ margin: '16px 0 12px 0' }}>
              数据库配置
            </Divider>

            <Form.Item label="数据源" name={['config', 'datasource']}>
              <Select placeholder="选择数据源">
                <Option value="default">默认数据源</Option>
                <Option value="readonly">只读数据源</Option>
                <Option value="analytics">分析数据源</Option>
              </Select>
            </Form.Item>

            <Form.Item label="SQL语句" name={['config', 'sql']}>
              <TextArea
                rows={6}
                placeholder="SELECT * FROM table WHERE condition = ?"
              />
            </Form.Item>

            <Form.Item label="参数" name={['config', 'parameters']}>
              <TextArea
                rows={3}
                placeholder='["param1", "param2"]'
              />
            </Form.Item>
          </>
        )}

        {selectedNode.type === WorkflowNodeType.CONDITION && (
          <>
            <Divider orientation="left" style={{ margin: '16px 0 12px 0' }}>
              条件配置
            </Divider>

            <Form.Item label="条件表达式" name={['config', 'expression']}>
              <TextArea
                rows={4}
                placeholder="例如: data.status === 'success' && data.amount > 100"
              />
            </Form.Item>

            <Form.Item label="条件类型" name={['config', 'conditionType']}>
              <Select defaultValue="javascript">
                <Option value="javascript">JavaScript表达式</Option>
                <Option value="simple">简单比较</Option>
                <Option value="regex">正则表达式</Option>
              </Select>
            </Form.Item>
          </>
        )}

        {(selectedNode.type === WorkflowNodeType.NOTIFICATION || selectedNode.type === WorkflowNodeType.EMAIL) && (
          <>
            <Divider orientation="left" style={{ margin: '16px 0 12px 0' }}>
              通知配置
            </Divider>

            <Form.Item label="接收者" name={['config', 'recipients']}>
              <TextArea
                rows={2}
                placeholder="邮箱地址或用户ID，多个用逗号分隔"
              />
            </Form.Item>

            <Form.Item label="标题" name={['config', 'subject']}>
              <Input placeholder="通知标题" />
            </Form.Item>

            <Form.Item label="内容" name={['config', 'content']}>
              <TextArea
                rows={4}
                placeholder="通知内容，支持模板变量"
              />
            </Form.Item>

            <Form.Item label="模板" name={['config', 'template']}>
              <Select placeholder="选择消息模板">
                <Option value="default">默认模板</Option>
                <Option value="success">成功通知</Option>
                <Option value="error">错误通知</Option>
                <Option value="warning">警告通知</Option>
              </Select>
            </Form.Item>
          </>
        )}
      </Form>
    </Card>
  );
};

export default PropertyPanel;