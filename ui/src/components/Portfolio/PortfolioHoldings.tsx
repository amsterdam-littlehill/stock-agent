import React, { useState, useEffect } from 'react';
import {
  Table,
  Card,
  Button,
  Space,
  Tag,
  Progress,
  Tooltip,
  Modal,
  Form,
  Input,
  InputNumber,

  message,

  Row,
  Col,
  Statistic,
  Alert
} from 'antd';
import {
  PlusOutlined,
  MinusOutlined,
  EditOutlined,

  RiseOutlined,
  FallOutlined,
  RiseOutlined
} from '@ant-design/icons';
import { portfolioApi, PortfolioHolding, AddHoldingRequest, SellHoldingRequest } from '../../services/portfolioApi';
import type { ColumnsType } from 'antd/es/table';

// const { Option } = Select;

interface PortfolioHoldingsProps {
  portfolioId: number;
}

const PortfolioHoldings: React.FC<PortfolioHoldingsProps> = ({ portfolioId }) => {
  const [loading, setLoading] = useState(false);
  const [holdings, setHoldings] = useState<PortfolioHolding[]>([]);
  const [addModalVisible, setAddModalVisible] = useState(false);
  const [sellModalVisible, setSellModalVisible] = useState(false);
  const [selectedHolding, setSelectedHolding] = useState<PortfolioHolding | null>(null);
  const [addForm] = Form.useForm();
  const [sellForm] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);

  // 加载持仓数据
  const loadHoldings = async () => {
    try {
      setLoading(true);
      const response = await portfolioApi.getPortfolioHoldings(portfolioId);
      setHoldings(response.data || []);
    } catch (error) {
      console.error('加载持仓数据失败:', error);
      message.error('加载持仓数据失败');
    } finally {
      setLoading(false);
    }
  };

  // 添加持仓
  const handleAddHolding = async (values: any) => {
    try {
      setSubmitting(true);
      const request: AddHoldingRequest = {
        stockCode: values.stockCode,
        quantity: values.quantity,
        price: values.price,
        targetWeight: values.targetWeight,
        notes: values.notes
      };

      await portfolioApi.addHolding(portfolioId, request);
      message.success('添加持仓成功');
      setAddModalVisible(false);
      addForm.resetFields();
      loadHoldings();
    } catch (error) {
      console.error('添加持仓失败:', error);
      message.error('添加持仓失败');
    } finally {
      setSubmitting(false);
    }
  };

  // 卖出持仓
  const handleSellHolding = async (values: any) => {
    try {
      setSubmitting(true);
      const request: SellHoldingRequest = {
        stockCode: selectedHolding!.stockCode,
        quantity: values.quantity,
        price: values.price,
        notes: values.notes
      };

      await portfolioApi.sellHolding(portfolioId, request);
      message.success('卖出成功');
      setSellModalVisible(false);
      sellForm.resetFields();
      setSelectedHolding(null);
      loadHoldings();
    } catch (error) {
      console.error('卖出失败:', error);
      message.error('卖出失败');
    } finally {
      setSubmitting(false);
    }
  };

  // 格式化金额
  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('zh-CN', {
      style: 'currency',
      currency: 'CNY',
      minimumFractionDigits: 2
    }).format(value);
  };

  // 格式化百分比
  const formatPercent = (value: number, showSign = true) => {
    const color = value >= 0 ? '#52c41a' : '#ff4d4f';
    const prefix = showSign && value >= 0 ? '+' : '';
    return (
      <span style={{
        color,
        fontWeight: 'bold'
      }}>
        {prefix}{(value * 100).toFixed(2)}%
      </span>
    );
  };

  // 获取风险等级颜色
  const getRiskLevelColor = (riskLevel: string) => {
    const colorMap: Record<string, string> = {
      'LOW': 'green',
      'MEDIUM': 'orange',
      'HIGH': 'red',
      'VERY_HIGH': 'purple'
    };
    return colorMap[riskLevel] || 'default';
  };

  // 获取投资评级颜色
  const getRatingColor = (rating: string) => {
    const colorMap: Record<string, string> = {
      'BUY': 'green',
      'HOLD': 'orange',
      'SELL': 'red',
      'STRONG_BUY': 'blue',
      'STRONG_SELL': 'purple'
    };
    return colorMap[rating] || 'default';
  };

  // 表格列定义
  const columns: ColumnsType<PortfolioHolding> = [
    {
      title: '股票信息',
      key: 'stock',
      width: 150,
      render: (_, record) => (
        <div>
          <div style={{
            fontWeight: 'bold',
            fontSize: '14px'
          }}>
            {record.stockCode}
          </div>
          <div style={{
            fontSize: '12px',
            color: '#666'
          }}>
            {record.stockName}
          </div>
          <div style={{
            fontSize: '11px',
            color: '#999'
          }}>
            {record.market} | {record.sector}
          </div>
        </div>
      )
    },
    {
      title: '持仓数量',
      dataIndex: 'quantity',
      key: 'quantity',
      width: 100,
      render: (value) => value.toLocaleString()
    },
    {
      title: '成本价',
      dataIndex: 'avgCost',
      key: 'avgCost',
      width: 100,
      render: (value) => `¥${value.toFixed(2)}`
    },
    {
      title: '现价',
      dataIndex: 'currentPrice',
      key: 'currentPrice',
      width: 100,
      render: (value) => `¥${value.toFixed(2)}`
    },
    {
      title: '市值',
      dataIndex: 'marketValue',
      key: 'marketValue',
      width: 120,
      render: (value) => formatCurrency(value),
      sorter: (a, b) => a.marketValue - b.marketValue
    },
    {
      title: '权重',
      key: 'weight',
      width: 120,
      render: (_, record) => (
        <div>
          <div>{formatPercent(record.weight, false)}</div>
          {record.targetWeight && (
            <div style={{
              fontSize: '11px',
              color: '#666'
            }}>
              目标: {formatPercent(record.targetWeight, false)}
            </div>
          )}
          <Progress
            percent={record.weight * 100}
            size="small"
            showInfo={false}
            strokeColor={record.weight > (record.targetWeight || 0) ? '#ff4d4f' : '#52c41a'}
          />
        </div>
      )
    },
    {
      title: '浮动盈亏',
      key: 'unrealizedPnl',
      width: 120,
      render: (_, record) => (
        <div>
          <div>{formatCurrency(record.unrealizedPnl)}</div>
          <div>{formatPercent(record.unrealizedPnlPercent)}</div>
        </div>
      ),
      sorter: (a, b) => a.unrealizedPnl - b.unrealizedPnl
    },
    {
      title: '今日盈亏',
      key: 'todayPnl',
      width: 120,
      render: (_, record) => (
        <div>
          <div>{formatCurrency(record.todayPnl)}</div>
          <div>{formatPercent(record.todayPnlPercent)}</div>
        </div>
      )
    },
    {
      title: '风险评级',
      key: 'risk',
      width: 100,
      render: (_, record) => (
        <div>
          <Tag color={getRiskLevelColor(record.riskLevel)}>
            {record.riskLevel}
          </Tag>
          {record.investmentRating && (
            <Tag color={getRatingColor(record.investmentRating)} size="small">
              {record.investmentRating}
            </Tag>
          )}
        </div>
      )
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="买入">
            <Button
              type="link"
              size="small"
              icon={<PlusOutlined />}
              onClick={() => {
                addForm.setFieldsValue({ stockCode: record.stockCode });
                setAddModalVisible(true);
              }}
            />
          </Tooltip>
          <Tooltip title="卖出">
            <Button
              type="link"
              size="small"
              icon={<MinusOutlined />}
              onClick={() => {
                setSelectedHolding(record);
                sellForm.setFieldsValue({
                  quantity: record.quantity,
                  price: record.currentPrice
                });
                setSellModalVisible(true);
              }}
            />
          </Tooltip>
          <Tooltip title="编辑">
            <Button
              type="link"
              size="small"
              icon={<EditOutlined />}
            />
          </Tooltip>
        </Space>
      )
    }
  ];

  // 计算汇总数据
  const summary = holdings.reduce(
    (acc, holding) => {
      acc.totalMarketValue += holding.marketValue;
      acc.totalCost += holding.totalCost;
      acc.totalUnrealizedPnl += holding.unrealizedPnl;
      acc.totalTodayPnl += holding.todayPnl;
      return acc;
    },
    {
      totalMarketValue: 0,
      totalCost: 0,
      totalUnrealizedPnl: 0,
      totalTodayPnl: 0
    }
  );

  useEffect(() => {
    if (portfolioId) {
      loadHoldings();
    }
  }, [portfolioId]);

  return (
    <div>
      {/* 汇总信息 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card size="small">
            <Statistic
              title="总市值"
              value={summary.totalMarketValue}
              formatter={(value) => formatCurrency(Number(value))}
              prefix={<RiseOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <Statistic
              title="总成本"
              value={summary.totalCost}
              formatter={(value) => formatCurrency(Number(value))}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <Statistic
              title="浮动盈亏"
              value={summary.totalUnrealizedPnl}
              formatter={(value) => formatCurrency(Number(value))}
              valueStyle={{color: summary.totalUnrealizedPnl >= 0 ? '#3f8600' : '#cf1322'}}
              prefix={
                summary.totalUnrealizedPnl >= 0 ?
                  <RiseOutlined /> :
                  <FallOutlined />
              }
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card size="small">
            <Statistic
              title="今日盈亏"
              value={summary.totalTodayPnl}
              formatter={(value) => formatCurrency(Number(value))}
              valueStyle={{color: summary.totalTodayPnl >= 0 ? '#3f8600' : '#cf1322'}}
            />
          </Card>
        </Col>
      </Row>

      {/* 持仓表格 */}
      <Card
        title="持仓明细"
        extra={
          <Space>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setAddModalVisible(true)}
            >
              添加持仓
            </Button>
          </Space>
        }
      >
        <Table
          columns={columns}
          dataSource={holdings}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1200 }}
          pagination={{
            pageSize: 20,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 只股票`
          }}
          summary={() => (
            <Table.Summary>
              <Table.Summary.Row>
                <Table.Summary.Cell index={0} colSpan={4}>
                  <strong>合计</strong>
                </Table.Summary.Cell>
                <Table.Summary.Cell index={4}>
                  <strong>{formatCurrency(summary.totalMarketValue)}</strong>
                </Table.Summary.Cell>
                <Table.Summary.Cell index={5}>
                  <strong>100.00%</strong>
                </Table.Summary.Cell>
                <Table.Summary.Cell index={6}>
                  <strong style={{ color: summary.totalUnrealizedPnl >= 0 ? '#52c41a' : '#ff4d4f' }}>
                    {formatCurrency(summary.totalUnrealizedPnl)}
                  </strong>
                </Table.Summary.Cell>
                <Table.Summary.Cell index={7}>
                  <strong style={{ color: summary.totalTodayPnl >= 0 ? '#52c41a' : '#ff4d4f' }}>
                    {formatCurrency(summary.totalTodayPnl)}
                  </strong>
                </Table.Summary.Cell>
                <Table.Summary.Cell index={8} colSpan={2} />
              </Table.Summary.Row>
            </Table.Summary>
          )}
        />
      </Card>

      {/* 添加持仓模态框 */}
      <Modal
        title="添加持仓"
        open={addModalVisible}
        onCancel={() => {
          setAddModalVisible(false);
          addForm.resetFields();
        }}
        footer={null}
        destroyOnClose
      >
        <Form
          form={addForm}
          layout="vertical"
          onFinish={handleAddHolding}
        >
          <Form.Item
            name="stockCode"
            label="股票代码"
            rules={[{
              required: true,
              message: '请输入股票代码'
            }]}
          >
            <Input placeholder="请输入股票代码，如：000001" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="quantity"
                label="买入数量"
                rules={[
                  {
                    required: true,
                    message: '请输入买入数量'
                  },
                  {
                    type: 'number',
                    min: 100,
                    message: '最少买入100股'
                  }
                ]}
              >
                <InputNumber
                  style={{ width: '100%' }}
                  placeholder="100"
                  min={100}
                  step={100}
                  formatter={(value) => `${value} 股`}
                  parser={(value) => value!.replace(' 股', '')}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="price"
                label="买入价格"
                rules={[
                  {
                    required: true,
                    message: '请输入买入价格'
                  },
                  {
                    type: 'number',
                    min: 0.01,
                    message: '价格必须大于0'
                  }
                ]}
              >
                <InputNumber
                  style={{ width: '100%' }}
                  placeholder="0.00"
                  min={0.01}
                  step={0.01}
                  formatter={(value) => `¥ ${value}`}
                  parser={(value) => value!.replace('¥ ', '')}
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="targetWeight"
            label="目标权重（可选）"
          >
            <InputNumber
              style={{ width: '100%' }}
              placeholder="10"
              min={0.1}
              max={50}
              step={0.1}
              formatter={(value) => `${value}%`}
              parser={(value) => value!.replace('%', '')}
            />
          </Form.Item>

          <Form.Item
            name="notes"
            label="备注"
          >
            <Input.TextArea
              rows={2}
              placeholder="买入原因或备注信息"
            />
          </Form.Item>

          <Form.Item style={{
            marginBottom: 0,
            textAlign: 'right'
          }}>
            <Space>
              <Button onClick={() => {
                setAddModalVisible(false);
                addForm.resetFields();
              }}>
                取消
              </Button>
              <Button type="primary" htmlType="submit" loading={submitting}>
                确认买入
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* 卖出持仓模态框 */}
      <Modal
        title={`卖出 ${selectedHolding?.stockName || ''}`}
        open={sellModalVisible}
        onCancel={() => {
          setSellModalVisible(false);
          sellForm.resetFields();
          setSelectedHolding(null);
        }}
        footer={null}
        destroyOnClose
      >
        {selectedHolding && (
          <>
            <Alert
              message="持仓信息"
              description={
                <div>
                  <p>当前持仓：{selectedHolding.quantity.toLocaleString()} 股</p>
                  <p>成本价：¥{selectedHolding.avgCost.toFixed(2)}</p>
                  <p>现价：¥{selectedHolding.currentPrice.toFixed(2)}</p>
                  <p>浮动盈亏：{formatCurrency(selectedHolding.unrealizedPnl)}</p>
                </div>
              }
              type="info"
              style={{ marginBottom: 16 }}
            />

            <Form
              form={sellForm}
              layout="vertical"
              onFinish={handleSellHolding}
            >
              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item
                    name="quantity"
                    label="卖出数量"
                    rules={[
                      {
                        required: true,
                        message: '请输入卖出数量'
                      },
                      {
                        type: 'number',
                        min: 100,
                        message: '最少卖出100股'
                      },
                      {
                        validator: (_, value) => {
                          if (value > selectedHolding.quantity) {
                            return Promise.reject('卖出数量不能超过持仓数量');
                          }
                          return Promise.resolve();
                        }
                      }
                    ]}
                  >
                    <InputNumber
                      style={{ width: '100%' }}
                      placeholder="100"
                      min={100}
                      max={selectedHolding.quantity}
                      step={100}
                      formatter={(value) => `${value} 股`}
                      parser={(value) => value!.replace(' 股', '')}
                    />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item
                    name="price"
                    label="卖出价格"
                    rules={[
                      {
                        required: true,
                        message: '请输入卖出价格'
                      },
                      {
                        type: 'number',
                        min: 0.01,
                        message: '价格必须大于0'
                      }
                    ]}
                  >
                    <InputNumber
                      style={{ width: '100%' }}
                      placeholder="0.00"
                      min={0.01}
                      step={0.01}
                      formatter={(value) => `¥ ${value}`}
                      parser={(value) => value!.replace('¥ ', '')}
                    />
                  </Form.Item>
                </Col>
              </Row>

              <Form.Item
                name="notes"
                label="备注"
              >
                <Input.TextArea
                  rows={2}
                  placeholder="卖出原因或备注信息"
                />
              </Form.Item>

              <Form.Item style={{
                marginBottom: 0,
                textAlign: 'right'
              }}>
                <Space>
                  <Button onClick={() => {
                    setSellModalVisible(false);
                    sellForm.resetFields();
                    setSelectedHolding(null);
                  }}>
                    取消
                  </Button>
                  <Button type="primary" danger htmlType="submit" loading={submitting}>
                    确认卖出
                  </Button>
                </Space>
              </Form.Item>
            </Form>
          </>
        )}
      </Modal>
    </div>
  );
};

export default PortfolioHoldings;