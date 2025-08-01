import React, { useState } from 'react';
import {
  Modal,
  Form,
  Input,
  Select,
  InputNumber,
  Switch,
  Button,
  Space,
  Row,
  Col,
  Divider,
  Tag,
  message,
  Tooltip
} from 'antd';
import {
  InfoCircleOutlined,
  DollarOutlined,
  AimOutlined,
  SafetyOutlined
} from '@ant-design/icons';
import { portfolioApi, CreatePortfolioRequest } from '../../services/portfolioApi';

const { Option } = Select;
const { TextArea } = Input;

interface CreatePortfolioModalProps {
  visible: boolean;
  onCancel: () => void;
  onSuccess: () => void;
}

const CreatePortfolioModal: React.FC<CreatePortfolioModalProps> = ({
  visible,
  onCancel,
  onSuccess
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [selectedTags, setSelectedTags] = useState<string[]>([]);

  // 预设标签
  const predefinedTags = [
    '价值投资', '成长投资', '指数投资', '量化投资',
    '长期持有', '短期交易', '分红股', '科技股',
    '蓝筹股', '小盘股', '新兴市场', '防御性'
  ];

  // 投资目标选项
  const investmentObjectives = [
    {
      value: 'GROWTH',
      label: '成长型',
      description: '追求资本增值，适合风险承受能力较强的投资者'
    },
    {
      value: 'INCOME',
      label: '收益型',
      description: '追求稳定收益，注重分红和利息收入'
    },
    {
      value: 'BALANCED',
      label: '平衡型',
      description: '平衡风险与收益，适合大多数投资者'
    },
    {
      value: 'CONSERVATIVE',
      label: '保守型',
      description: '保本为主，风险较低，收益相对稳定'
    }
  ];

  // 风险承受能力选项
  const riskTolerances = [
    {
      value: 'LOW',
      label: '低风险',
      description: '保守投资，优先考虑资金安全'
    },
    {
      value: 'MEDIUM',
      label: '中等风险',
      description: '平衡风险与收益，可承受一定波动'
    },
    {
      value: 'HIGH',
      label: '高风险',
      description: '追求高收益，可承受较大波动'
    }
  ];

  // 基准指数选项
  const benchmarkOptions = [
    {
      value: '000001.SH',
      label: '上证指数'
    },
    {
      value: '399001.SZ',
      label: '深证成指'
    },
    {
      value: '399006.SZ',
      label: '创业板指'
    },
    {
      value: '000300.SH',
      label: '沪深300'
    },
    {
      value: '000905.SH',
      label: '中证500'
    },
    {
      value: '000852.SH',
      label: '中证1000'
    }
  ];

  // 再平衡频率选项
  const rebalanceFrequencies = [
    {
      value: 'WEEKLY',
      label: '每周'
    },
    {
      value: 'MONTHLY',
      label: '每月'
    },
    {
      value: 'QUARTERLY',
      label: '每季度'
    },
    {
      value: 'SEMI_ANNUALLY',
      label: '每半年'
    },
    {
      value: 'ANNUALLY',
      label: '每年'
    }
  ];

  // 处理表单提交
  const handleSubmit = async (values: any) => {
    try {
      setLoading(true);

      const request: CreatePortfolioRequest = {
        name: values.name,
        description: values.description,
        investmentObjective: values.investmentObjective,
        riskTolerance: values.riskTolerance,
        initialCapital: values.initialCapital,
        benchmarkCode: values.benchmarkCode,
        autoRebalanceEnabled: values.autoRebalanceEnabled || false,
        rebalanceThreshold: values.rebalanceThreshold,
        rebalanceFrequency: values.rebalanceFrequency,
        tags: selectedTags,
        notes: values.notes
      };

      await portfolioApi.createPortfolio(request);
      message.success('投资组合创建成功！');
      form.resetFields();
      setSelectedTags([]);
      onSuccess();
    } catch (error) {
      console.error('创建投资组合失败:', error);
      message.error('创建失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  // 处理取消
  const handleCancel = () => {
    form.resetFields();
    setSelectedTags([]);
    onCancel();
  };

  // 添加标签
  const handleAddTag = (tag: string) => {
    if (!selectedTags.includes(tag)) {
      setSelectedTags([...selectedTags, tag]);
    }
  };

  // 移除标签
  const handleRemoveTag = (tag: string) => {
    setSelectedTags(selectedTags.filter(t => t !== tag));
  };

  return (
    <Modal
      title={
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: 8
        }}>
          <AimOutlined style={{ color: '#1890ff' }} />
          创建投资组合
        </div>
      }
      open={visible}
      onCancel={handleCancel}
      footer={null}
      width={800}
      destroyOnClose
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        initialValues={{
          autoRebalanceEnabled: false,
          rebalanceThreshold: 5,
          rebalanceFrequency: 'QUARTERLY'
        }}
      >
        {/* 基本信息 */}
        <div style={{ marginBottom: 24 }}>
          <h4 style={{
            marginBottom: 16,
            color: '#1890ff'
          }}>
            <InfoCircleOutlined /> 基本信息
          </h4>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="name"
                label="组合名称"
                rules={[
                  {
                    required: true,
                    message: '请输入组合名称'
                  },
                  {
                    max: 50,
                    message: '名称不能超过50个字符'
                  }
                ]}
              >
                <Input placeholder="请输入投资组合名称" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="initialCapital"
                label={
                  <span>
                    <DollarOutlined /> 初始资金
                  </span>
                }
                rules={[
                  {
                    required: true,
                    message: '请输入初始资金'
                  },
                  {
                    type: 'number',
                    min: 1000,
                    message: '初始资金不能少于1000元'
                  }
                ]}
              >
                <InputNumber
                  style={{ width: '100%' }}
                  placeholder="请输入初始资金"
                  formatter={(value) => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                  parser={(value) => value!.replace(/¥\s?|(,*)/g, '')}
                  min={1000}
                  max={100000000}
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="description"
            label="组合描述"
            rules={[
              {
                max: 200,
                message: '描述不能超过200个字符'
              }
            ]}
          >
            <TextArea
              rows={3}
              placeholder="请描述您的投资组合策略和目标"
              showCount
              maxLength={200}
            />
          </Form.Item>
        </div>

        <Divider />

        {/* 投资策略 */}
        <div style={{ marginBottom: 24 }}>
          <h4 style={{
            marginBottom: 16,
            color: '#1890ff'
          }}>
            <AimOutlined /> 投资策略
          </h4>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="investmentObjective"
                label="投资目标"
                rules={[{
                  required: true,
                  message: '请选择投资目标'
                }]}
              >
                <Select placeholder="请选择投资目标">
                  {investmentObjectives.map(option => (
                    <Option key={option.value} value={option.value}>
                      <div>
                        <div>{option.label}</div>
                        <div style={{
                          fontSize: '12px',
                          color: '#666'
                        }}>
                          {option.description}
                        </div>
                      </div>
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="riskTolerance"
                label={
                  <span>
                    <SafetyOutlined /> 风险承受能力
                  </span>
                }
                rules={[{
                  required: true,
                  message: '请选择风险承受能力'
                }]}
              >
                <Select placeholder="请选择风险承受能力">
                  {riskTolerances.map(option => (
                    <Option key={option.value} value={option.value}>
                      <div>
                        <div>{option.label}</div>
                        <div style={{
                          fontSize: '12px',
                          color: '#666'
                        }}>
                          {option.description}
                        </div>
                      </div>
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="benchmarkCode"
            label="基准指数"
          >
            <Select placeholder="请选择基准指数（可选）" allowClear>
              {benchmarkOptions.map(option => (
                <Option key={option.value} value={option.value}>
                  {option.label}
                </Option>
              ))}
            </Select>
          </Form.Item>
        </div>

        <Divider />

        {/* 自动再平衡设置 */}
        <div style={{ marginBottom: 24 }}>
          <h4 style={{
            marginBottom: 16,
            color: '#1890ff'
          }}>
            <SettingOutlined /> 自动再平衡设置
          </h4>

          <Form.Item
            name="autoRebalanceEnabled"
            label="启用自动再平衡"
            valuePropName="checked"
          >
            <Switch />
          </Form.Item>

          <Form.Item
            noStyle
            shouldUpdate={(prevValues, currentValues) =>
              prevValues.autoRebalanceEnabled !== currentValues.autoRebalanceEnabled
            }
          >
            {({ getFieldValue }) =>
              getFieldValue('autoRebalanceEnabled') ? (
                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item
                      name="rebalanceThreshold"
                      label={
                        <span>
                          再平衡阈值
                          <Tooltip title="当资产权重偏离目标权重超过此阈值时触发再平衡">
                            <InfoCircleOutlined style={{
                              marginLeft: 4,
                              color: '#1890ff'
                            }} />
                          </Tooltip>
                        </span>
                      }
                    >
                      <InputNumber
                        style={{ width: '100%' }}
                        min={1}
                        max={20}
                        formatter={(value) => `${value}%`}
                        parser={(value) => value!.replace('%', '')}
                        placeholder="5"
                      />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item
                      name="rebalanceFrequency"
                      label="再平衡频率"
                    >
                      <Select placeholder="请选择再平衡频率">
                        {rebalanceFrequencies.map(option => (
                          <Option key={option.value} value={option.value}>
                            {option.label}
                          </Option>
                        ))}
                      </Select>
                    </Form.Item>
                  </Col>
                </Row>
              ) : null
            }
          </Form.Item>
        </div>

        <Divider />

        {/* 标签和备注 */}
        <div style={{ marginBottom: 24 }}>
          <h4 style={{
            marginBottom: 16,
            color: '#1890ff'
          }}>
            其他设置
          </h4>

          <Form.Item label="投资标签">
            <div style={{ marginBottom: 8 }}>
              <span style={{
                fontSize: '12px',
                color: '#666'
              }}>选择标签：</span>
              {predefinedTags.map(tag => (
                <Tag
                  key={tag}
                  style={{
                    cursor: 'pointer',
                    margin: '2px',
                    backgroundColor: selectedTags.includes(tag) ? '#1890ff' : undefined,
                    color: selectedTags.includes(tag) ? 'white' : undefined
                  }}
                  onClick={() =>
                    selectedTags.includes(tag) ? handleRemoveTag(tag) : handleAddTag(tag)
                  }
                >
                  {tag}
                </Tag>
              ))}
            </div>
            <div>
              <span style={{
                fontSize: '12px',
                color: '#666'
              }}>已选择：</span>
              {selectedTags.map(tag => (
                <Tag
                  key={tag}
                  closable
                  onClose={() => handleRemoveTag(tag)}
                  color="blue"
                  style={{ margin: '2px' }}
                >
                  {tag}
                </Tag>
              ))}
            </div>
          </Form.Item>

          <Form.Item
            name="notes"
            label="备注"
          >
            <TextArea
              rows={2}
              placeholder="其他说明或备注信息"
              showCount
              maxLength={500}
            />
          </Form.Item>
        </div>

        {/* 提交按钮 */}
        <Form.Item style={{
          marginBottom: 0,
          textAlign: 'right'
        }}>
          <Space>
            <Button onClick={handleCancel}>
              取消
            </Button>
            <Button type="primary" htmlType="submit" loading={loading}>
              创建投资组合
            </Button>
          </Space>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default CreatePortfolioModal;