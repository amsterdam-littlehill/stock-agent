import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Tag,
  Button,
  Space,
  DatePicker,
  Select,
  Input,
  Row,
  Col,
  Statistic,
  Tooltip,
  message,
  Modal,

  Descriptions,
  Divider,
  Alert
} from 'antd';
import {
  SearchOutlined,
  ReloadOutlined,
  ExportOutlined,
  FilterOutlined,
  EyeOutlined,
  DeleteOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons';
import { portfolioApi, PortfolioTransaction } from '../../services/portfolioApi';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Option } = Select;
const { RangePicker } = DatePicker;
const { confirm } = Modal;

interface PortfolioTransactionsProps {
  portfolioId: number;
}

interface TransactionFilters {
  stockCode?: string;
  transactionType?: string;
  status?: string;
  dateRange?: [string, string];
  minAmount?: number;
  maxAmount?: number;
}

interface TransactionStatistics {
  totalTransactions: number;
  totalBuyAmount: number;
  totalSellAmount: number;
  totalFees: number;
  totalRealizedPnl: number;
  winRate: number;
  avgHoldingDays: number;
}

const PortfolioTransactions: React.FC<PortfolioTransactionsProps> = ({ portfolioId }) => {
  const [loading, setLoading] = useState(false);
  const [transactions, setTransactions] = useState<PortfolioTransaction[]>([]);
  const [statistics, setStatistics] = useState<TransactionStatistics | null>(null);
  const [filters, setFilters] = useState<TransactionFilters>({});
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0
  });
  const [selectedTransaction, setSelectedTransaction] = useState<PortfolioTransaction | null>(null);
  const [detailModalVisible, setDetailModalVisible] = useState(false);

  // 交易类型选项
  const transactionTypes = [
    {
      value: 'BUY',
      label: '买入',
      color: 'green'
    },
    {
      value: 'SELL',
      label: '卖出',
      color: 'red'
    },
    {
      value: 'DIVIDEND',
      label: '分红',
      color: 'blue'
    },
    {
      value: 'SPLIT',
      label: '拆股',
      color: 'orange'
    },
    {
      value: 'MERGE',
      label: '合股',
      color: 'purple'
    }
  ];

  // 交易状态选项
  const statusOptions = [
    {
      value: 'PENDING',
      label: '待处理',
      color: 'processing'
    },
    {
      value: 'CONFIRMED',
      label: '已确认',
      color: 'success'
    },
    {
      value: 'CANCELLED',
      label: '已取消',
      color: 'error'
    },
    {
      value: 'FAILED',
      label: '失败',
      color: 'error'
    }
  ];

  // 加载交易记录
  const loadTransactions = async (page = 1, pageSize = 20) => {
    try {
      setLoading(true);

      const response = await portfolioApi.getPortfolioTransactions(portfolioId, page - 1, pageSize);
      setTransactions(response.data.transactions);
      setPagination({
        current: page,
        pageSize,
        total: response.data.total
      });
    } catch (error) {
      console.error('加载交易记录失败:', error);
      message.error('加载交易记录失败');
    } finally {
      setLoading(false);
    }
  };

  // 加载统计数据
  const loadStatistics = async () => {
    try {
      const response = await portfolioApi.getTransactionStatistics(portfolioId, filters);
      setStatistics(response.data);
    } catch (error) {
      console.error('加载统计数据失败:', error);
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
  // const formatPercent = (value: number) => {
  //   const color = value >= 0 ? '#52c41a' : '#ff4d4f';
  //   const prefix = value >= 0 ? '+' : '';
  //   return (
  //     <span style={{
  //       color,
  //       fontWeight: 'bold'
  //     }}>
  //       {prefix}{(value * 100).toFixed(2)}%
  //     </span>
  //   );
  // };

  // 获取交易类型配置
  const getTransactionTypeConfig = (type: string) => {
    return transactionTypes.find(t => t.value === type) || {
      value: type,
      label: type,
      color: 'default'
    };
  };

  // 获取状态配置
  const getStatusConfig = (status: string) => {
    return statusOptions.find(s => s.value === status) || {
      value: status,
      label: status,
      color: 'default'
    };
  };

  // 删除交易记录
  const handleDeleteTransaction = (transactionId: number) => {
    confirm({
      title: '确认删除',
      icon: <ExclamationCircleOutlined />,
      content: '确定要删除这条交易记录吗？此操作不可撤销。',
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          await portfolioApi.deleteTransaction(transactionId);
          message.success('删除成功');
          loadTransactions(pagination.current, pagination.pageSize);
          loadStatistics();
        } catch (error) {
          console.error('删除失败:', error);
          message.error('删除失败');
        }
      }
    });
  };

  // 查看交易详情
  const handleViewDetail = (transaction: PortfolioTransaction) => {
    setSelectedTransaction(transaction);
    setDetailModalVisible(true);
  };

  // 应用筛选
  const handleApplyFilters = () => {
    loadTransactions(1, pagination.pageSize);
    loadStatistics();
  };

  // 重置筛选
  const handleResetFilters = () => {
    setFilters({});
    setTimeout(() => {
      loadTransactions(1, pagination.pageSize);
      loadStatistics();
    }, 0);
  };

  // 导出交易记录
  const handleExport = async () => {
    try {
      const response = await portfolioApi.exportTransactions(portfolioId, filters);
      // 创建下载链接
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `portfolio_${portfolioId}_transactions.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      message.success('导出成功');
    } catch (error) {
      console.error('导出失败:', error);
      message.error('导出失败');
    }
  };

  // 表格列定义
  const columns: ColumnsType<PortfolioTransaction> = [
    {
      title: '交易时间',
      dataIndex: 'transactionTime',
      key: 'transactionTime',
      width: 150,
      render: (time: string) => dayjs(time).format('YYYY-MM-DD HH:mm'),
      sorter: true
    },
    {
      title: '股票代码',
      dataIndex: 'stockCode',
      key: 'stockCode',
      width: 100,
      render: (code: string, record: PortfolioTransaction) => (
        <div>
          <div style={{ fontWeight: 'bold' }}>{code}</div>
          <div style={{
            fontSize: '12px',
            color: '#666'
          }}>{record.stockName}</div>
        </div>
      )
    },
    {
      title: '交易类型',
      dataIndex: 'transactionType',
      key: 'transactionType',
      width: 80,
      render: (type: string) => {
        const config = getTransactionTypeConfig(type);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
      filters: transactionTypes.map(type => ({
        text: type.label,
        value: type.value
      })),
      onFilter: (value, record) => record.transactionType === value
    },
    {
      title: '数量',
      dataIndex: 'quantity',
      key: 'quantity',
      width: 100,
      align: 'right',
      render: (quantity: number) => quantity.toLocaleString()
    },
    {
      title: '价格',
      dataIndex: 'price',
      key: 'price',
      width: 100,
      align: 'right',
      render: (price: number) => `¥${price.toFixed(2)}`
    },
    {
      title: '交易金额',
      dataIndex: 'amount',
      key: 'amount',
      width: 120,
      align: 'right',
      render: (amount: number) => formatCurrency(amount),
      sorter: true
    },
    {
      title: '手续费',
      dataIndex: 'fee',
      key: 'fee',
      width: 100,
      align: 'right',
      render: (fee: number) => formatCurrency(fee)
    },
    {
      title: '已实现盈亏',
      dataIndex: 'realizedPnl',
      key: 'realizedPnl',
      width: 120,
      align: 'right',
      render: (pnl: number) => {
        if (pnl === 0) return '-';
        return (
          <span style={{
            color: pnl >= 0 ? '#52c41a' : '#ff4d4f',
            fontWeight: 'bold'
          }}>
            {pnl >= 0 ? '+' : ''}{formatCurrency(pnl)}
          </span>
        );
      }
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: string) => {
        const config = getStatusConfig(status);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
      filters: statusOptions.map(status => ({
        text: status.label,
        value: status.value
      })),
      onFilter: (value, record) => record.status === value
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record: PortfolioTransaction) => (
        <Space size="small">
          <Tooltip title="查看详情">
            <Button
              type="text"
              icon={<EyeOutlined />}
              onClick={() => handleViewDetail(record)}
            />
          </Tooltip>
          <Tooltip title="删除">
            <Button
              type="text"
              danger
              icon={<DeleteOutlined />}
              onClick={() => handleDeleteTransaction(record.id)}
            />
          </Tooltip>
        </Space>
      )
    }
  ];

  useEffect(() => {
    if (portfolioId) {
      loadTransactions();
      loadStatistics();
    }
  }, [portfolioId]);

  return (
    <div>
      {/* 统计信息 */}
      {statistics && (
        <Card style={{ marginBottom: 16 }}>
          <Row gutter={[16, 16]}>
            <Col span={4}>
              <Statistic
                title="交易笔数"
                value={statistics.totalTransactions}
                suffix="笔"
              />
            </Col>
            <Col span={4}>
              <Statistic
                title="买入总额"
                value={statistics.totalBuyAmount}
                formatter={(value) => formatCurrency(Number(value))}
              />
            </Col>
            <Col span={4}>
              <Statistic
                title="卖出总额"
                value={statistics.totalSellAmount}
                formatter={(value) => formatCurrency(Number(value))}
              />
            </Col>
            <Col span={4}>
              <Statistic
                title="总手续费"
                value={statistics.totalFees}
                formatter={(value) => formatCurrency(Number(value))}
              />
            </Col>
            <Col span={4}>
              <Statistic
                title="已实现盈亏"
                value={statistics.totalRealizedPnl}
                formatter={(value) => formatCurrency(Number(value))}
                valueStyle={{color: statistics.totalRealizedPnl >= 0 ? '#3f8600' : '#cf1322'}}
              />
            </Col>
            <Col span={4}>
              <Statistic
                title="胜率"
                value={statistics.winRate * 100}
                precision={1}
                suffix="%"
                valueStyle={{color: statistics.winRate >= 0.6 ? '#3f8600' : statistics.winRate >= 0.4 ? '#faad14' : '#cf1322'}}
              />
            </Col>
          </Row>
        </Card>
      )}

      {/* 筛选面板 */}
      <Card size="small" style={{ marginBottom: 16 }}>
        <Row gutter={[16, 8]} align="middle">
          <Col span={4}>
            <Input
              placeholder="股票代码"
              value={filters.stockCode}
              onChange={(e) => setFilters({
                ...filters,
                stockCode: e.target.value
              })}
              allowClear
            />
          </Col>
          <Col span={4}>
            <Select
              placeholder="交易类型"
              value={filters.transactionType}
              onChange={(value) => setFilters({
                ...filters,
                transactionType: value
              })}
              allowClear
              style={{ width: '100%' }}
            >
              {transactionTypes.map(type => (
                <Option key={type.value} value={type.value}>{type.label}</Option>
              ))}
            </Select>
          </Col>
          <Col span={4}>
            <Select
              placeholder="交易状态"
              value={filters.status}
              onChange={(value) => setFilters({
                ...filters,
                status: value
              })}
              allowClear
              style={{ width: '100%' }}
            >
              {statusOptions.map(status => (
                <Option key={status.value} value={status.value}>{status.label}</Option>
              ))}
            </Select>
          </Col>
          <Col span={6}>
            <RangePicker
              value={filters.dateRange ? [dayjs(filters.dateRange[0]), dayjs(filters.dateRange[1])] : null}
              onChange={(dates) => {
                if (dates) {
                  setFilters({
                    ...filters,
                    dateRange: [dates[0]!.format('YYYY-MM-DD'), dates[1]!.format('YYYY-MM-DD')]
                  });
                } else {
                  setFilters({
                    ...filters,
                    dateRange: undefined
                  });
                }
              }}
              style={{ width: '100%' }}
            />
          </Col>
          <Col span={6}>
            <Space>
              <Button
                type="primary"
                icon={<SearchOutlined />}
                onClick={handleApplyFilters}
              >
                查询
              </Button>
              <Button
                icon={<FilterOutlined />}
                onClick={handleResetFilters}
              >
                重置
              </Button>
              <Button
                icon={<ReloadOutlined />}
                onClick={() => loadTransactions(pagination.current, pagination.pageSize)}
                loading={loading}
              >
                刷新
              </Button>
              <Button
                icon={<ExportOutlined />}
                onClick={handleExport}
              >
                导出
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 交易记录表格 */}
      <Card>
        <Table
          columns={columns}
          dataSource={transactions}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
            onChange: (page, pageSize) => {
              loadTransactions(page, pageSize);
            }
          }}
          scroll={{ x: 1200 }}
          size="small"
        />
      </Card>

      {/* 交易详情模态框 */}
      <Modal
        title="交易详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setDetailModalVisible(false)}>
            关闭
          </Button>
        ]}
        width={800}
      >
        {selectedTransaction && (
          <div>
            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="交易流水号">
                {selectedTransaction.transactionId}
              </Descriptions.Item>
              <Descriptions.Item label="订单ID">
                {selectedTransaction.orderId || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="券商交易ID">
                {selectedTransaction.brokerTransactionId || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="交易时间">
                {dayjs(selectedTransaction.transactionTime).format('YYYY-MM-DD HH:mm:ss')}
              </Descriptions.Item>
              <Descriptions.Item label="股票代码">
                {selectedTransaction.stockCode}
              </Descriptions.Item>
              <Descriptions.Item label="股票名称">
                {selectedTransaction.stockName}
              </Descriptions.Item>
              <Descriptions.Item label="交易类型">
                <Tag color={getTransactionTypeConfig(selectedTransaction.transactionType).color}>
                  {getTransactionTypeConfig(selectedTransaction.transactionType).label}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="交易状态">
                <Tag color={getStatusConfig(selectedTransaction.status).color}>
                  {getStatusConfig(selectedTransaction.status).label}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="数量">
                {selectedTransaction.quantity.toLocaleString()} 股
              </Descriptions.Item>
              <Descriptions.Item label="价格">
                ¥{selectedTransaction.price.toFixed(4)}
              </Descriptions.Item>
              <Descriptions.Item label="交易金额">
                {formatCurrency(selectedTransaction.amount)}
              </Descriptions.Item>
              <Descriptions.Item label="手续费">
                {formatCurrency(selectedTransaction.fee)}
              </Descriptions.Item>
              <Descriptions.Item label="印花税">
                {formatCurrency(selectedTransaction.stampTax || 0)}
              </Descriptions.Item>
              <Descriptions.Item label="过户费">
                {formatCurrency(selectedTransaction.transferFee || 0)}
              </Descriptions.Item>
              <Descriptions.Item label="已实现盈亏">
                {selectedTransaction.realizedPnl !== 0 ? (
                  <span style={{
                    color: selectedTransaction.realizedPnl >= 0 ? '#52c41a' : '#ff4d4f',
                    fontWeight: 'bold'
                  }}>
                    {selectedTransaction.realizedPnl >= 0 ? '+' : ''}
                    {formatCurrency(selectedTransaction.realizedPnl)}
                  </span>
                ) : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="市场">
                {selectedTransaction.market}
              </Descriptions.Item>
              <Descriptions.Item label="货币">
                {selectedTransaction.currency}
              </Descriptions.Item>
              <Descriptions.Item label="交易来源">
                {selectedTransaction.source}
              </Descriptions.Item>
              <Descriptions.Item label="是否T+0">
                {selectedTransaction.isT0 ? '是' : '否'}
              </Descriptions.Item>
              <Descriptions.Item label="是否融资融券">
                {selectedTransaction.isMarginTrading ? '是' : '否'}
              </Descriptions.Item>
              <Descriptions.Item label="备注" span={2}>
                {selectedTransaction.notes || '-'}
              </Descriptions.Item>
            </Descriptions>

            {selectedTransaction.analysisTaskId && (
              <>
                <Divider />
                <Alert
                  message="智能分析"
                  description={`此交易基于分析任务 #${selectedTransaction.analysisTaskId} 的建议执行`}
                  type="info"
                  showIcon
                />
              </>
            )}

            {selectedTransaction.agentRecommendationId && (
              <>
                <Divider />
                <Alert
                  message="智能体建议"
                  description={`此交易基于智能体建议 #${selectedTransaction.agentRecommendationId} 执行`}
                  type="success"
                  showIcon
                />
              </>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default PortfolioTransactions;