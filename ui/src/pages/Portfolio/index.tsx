import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Button,
  Table,
  Tag,
  Space,
  Statistic,

  message,
  Tabs,

  Popconfirm,
  Empty,
  Spin,
  Spin
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  BarChartOutlined,

  RiseOutlined,

  SettingOutlined,
  ExportOutlined,
  ImportOutlined
} from '@ant-design/icons';
import { portfolioApi } from '../../services/portfolioApi';
import PortfolioChart from '../../components/Portfolio/PortfolioChart';
import PortfolioHoldings from '../../components/Portfolio/PortfolioHoldings';
import PortfolioPerformance from '../../components/Portfolio/PortfolioPerformance';
import CreatePortfolioModal from '../../components/Portfolio/CreatePortfolioModal';
import './index.css';

const { TabPane } = Tabs;
// const { Option } = Select;

interface Portfolio {
  id: number;
  name: string;
  description: string;
  investmentObjective: string;
  riskTolerance: string;
  initialCapital: number;
  currentValue: number;
  totalReturn: number;
  totalReturnPercent: number;
  todayReturn: number;
  todayReturnPercent: number;
  sharpeRatio: number;
  maxDrawdown: number;
  volatility: number;
  status: string;
  createdAt: string;
  updatedAt: string;
}

interface PortfolioSummary {
  portfolio: Portfolio;
  holdingsCount: number;
  totalMarketValue: number;
  totalCost: number;
  totalPnl: number;
  todayPnl: number;
  cashBalance: number;
}

const PortfolioPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [portfolios, setPortfolios] = useState<PortfolioSummary[]>([]);
  const [selectedPortfolio, setSelectedPortfolio] = useState<PortfolioSummary | null>(null);
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [activeTab, setActiveTab] = useState('overview');
  const [refreshing, setRefreshing] = useState(false);

  // 加载投资组合列表
  const loadPortfolios = async () => {
    try {
      setLoading(true);
      const response = await portfolioApi.getPortfolioList('current_user'); // 实际应用中从用户上下文获取
      setPortfolios(response.data || []);

      // 如果有投资组合且没有选中的，默认选中第一个
      if (response.data && response.data.length > 0 && !selectedPortfolio) {
        setSelectedPortfolio(response.data[0]);
      }
    } catch (error) {
      console.error('加载投资组合失败:', error);
      message.error('加载投资组合失败');
    } finally {
      setLoading(false);
    }
  };

  // 刷新数据
  const handleRefresh = async () => {
    setRefreshing(true);
    await loadPortfolios();
    setRefreshing(false);
    message.success('数据已刷新');
  };

  // 删除投资组合
  const handleDeletePortfolio = async (portfolioId: number) => {
    try {
      await portfolioApi.deletePortfolio(portfolioId);
      message.success('删除成功');
      await loadPortfolios();

      // 如果删除的是当前选中的投资组合，清空选中状态
      if (selectedPortfolio?.portfolio.id === portfolioId) {
        setSelectedPortfolio(null);
      }
    } catch (error) {
      console.error('删除投资组合失败:', error);
      message.error('删除失败');
    }
  };

  // 创建投资组合成功回调
  const handleCreateSuccess = () => {
    setCreateModalVisible(false);
    loadPortfolios();
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
  const formatPercent = (value: number) => {
    const color = value >= 0 ? '#52c41a' : '#ff4d4f';
    const prefix = value >= 0 ? '+' : '';
    return (
      <span style={{ color }}>
        {prefix}{(value * 100).toFixed(2)}%
      </span>
    );
  };

  // 投资组合列表表格列定义
  const portfolioColumns = [
    {
      title: '组合名称',
      dataIndex: ['portfolio', 'name'],
      key: 'name',
      render: (text: string, record: PortfolioSummary) => (
        <div>
          <div style={{ fontWeight: 'bold' }}>{text}</div>
          <div style={{
            fontSize: '12px',
            color: '#666'
          }}>
            {record.portfolio.description}
          </div>
        </div>
      )
    },
    {
      title: '投资目标',
      dataIndex: ['portfolio', 'investmentObjective'],
      key: 'investmentObjective',
      render: (text: string) => {
        const colorMap: Record<string, string> = {
          'GROWTH': 'blue',
          'INCOME': 'green',
          'BALANCED': 'orange',
          'CONSERVATIVE': 'purple'
        };
        const labelMap: Record<string, string> = {
          'GROWTH': '成长型',
          'INCOME': '收益型',
          'BALANCED': '平衡型',
          'CONSERVATIVE': '保守型'
        };
        return <Tag color={colorMap[text]}>{labelMap[text] || text}</Tag>;
      }
    },
    {
      title: '当前价值',
      dataIndex: 'totalMarketValue',
      key: 'currentValue',
      render: (value: number) => formatCurrency(value),
      sorter: (a: PortfolioSummary, b: PortfolioSummary) => a.totalMarketValue - b.totalMarketValue
    },
    {
      title: '总收益',
      dataIndex: 'totalPnl',
      key: 'totalReturn',
      render: (value: number, record: PortfolioSummary) => (
        <div>
          <div>{formatCurrency(value)}</div>
          <div>{formatPercent(record.portfolio.totalReturnPercent)}</div>
        </div>
      ),
      sorter: (a: PortfolioSummary, b: PortfolioSummary) => a.totalPnl - b.totalPnl
    },
    {
      title: '今日收益',
      dataIndex: 'todayPnl',
      key: 'todayReturn',
      render: (value: number, record: PortfolioSummary) => (
        <div>
          <div>{formatCurrency(value)}</div>
          <div>{formatPercent(record.portfolio.todayReturnPercent)}</div>
        </div>
      )
    },
    {
      title: '持仓数量',
      dataIndex: 'holdingsCount',
      key: 'holdingsCount',
      render: (value: number) => `${value} 只`
    },
    {
      title: '状态',
      dataIndex: ['portfolio', 'status'],
      key: 'status',
      render: (status: string) => {
        const colorMap: Record<string, string> = {
          'ACTIVE': 'green',
          'INACTIVE': 'red',
          'SUSPENDED': 'orange'
        };
        const labelMap: Record<string, string> = {
          'ACTIVE': '活跃',
          'INACTIVE': '非活跃',
          'SUSPENDED': '暂停'
        };
        return <Tag color={colorMap[status]}>{labelMap[status] || status}</Tag>;
      }
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record: PortfolioSummary) => (
        <Space size="middle">
          <Button
            type="link"
            size="small"
            onClick={() => setSelectedPortfolio(record)}
          >
            查看详情
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个投资组合吗？"
            onConfirm={() => handleDeletePortfolio(record.portfolio.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ];

  useEffect(() => {
    loadPortfolios();
  }, []);

  return (
    <div className="portfolio-page">
      {/* 页面头部 */}
      <div className="portfolio-header">
        <div className="header-left">
          <h2>投资组合管理</h2>
          <p>管理您的投资组合，跟踪投资表现</p>
        </div>
        <div className="header-right">
          <Space>
            <Button
              icon={<ReloadOutlined />}
              onClick={handleRefresh}
              loading={refreshing}
            >
              刷新
            </Button>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setCreateModalVisible(true)}
            >
              创建投资组合
            </Button>
          </Space>
        </div>
      </div>

      {/* 主要内容 */}
      <Row gutter={[16, 16]}>
        {/* 左侧：投资组合列表 */}
        <Col xs={24} lg={selectedPortfolio ? 8 : 24}>
          <Card
            title="我的投资组合"
            extra={
              <Space>
                <Button size="small" icon={<ImportOutlined />}>
                  导入
                </Button>
                <Button size="small" icon={<ExportOutlined />}>
                  导出
                </Button>
              </Space>
            }
          >
            {loading ? (
              <div style={{
                textAlign: 'center',
                padding: '50px 0'
              }}>
                <Spin size="large" />
              </div>
            ) : portfolios.length === 0 ? (
              <Empty
                description="暂无投资组合"
                image={Empty.PRESENTED_IMAGE_SIMPLE}
              >
                <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={() => setCreateModalVisible(true)}
                >
                  创建第一个投资组合
                </Button>
              </Empty>
            ) : (
              <Table
                dataSource={portfolios}
                columns={portfolioColumns}
                rowKey={(record) => record.portfolio.id}
                pagination={{
                  pageSize: 10,
                  showSizeChanger: true,
                  showQuickJumper: true,
                  showTotal: (total) => `共 ${total} 个投资组合`
                }}
                rowSelection={{
                  type: 'radio',
                  selectedRowKeys: selectedPortfolio ? [selectedPortfolio.portfolio.id] : [],
                  onSelect: (record) => setSelectedPortfolio(record)
                }}
                size="small"
              />
            )}
          </Card>
        </Col>

        {/* 右侧：投资组合详情 */}
        {selectedPortfolio && (
          <Col xs={24} lg={16}>
            <Card
              title={`${selectedPortfolio.portfolio.name} - 详细信息`}
              extra={
                <Space>
                  <Button size="small" icon={<SettingOutlined />}>
                    设置
                  </Button>
                  <Button size="small" icon={<BarChartOutlined />}>
                    分析
                  </Button>
                </Space>
              }
            >
              <Tabs activeKey={activeTab} onChange={setActiveTab}>
                <TabPane tab="概览" key="overview">
                  {/* 关键指标 */}
                  <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
                    <Col span={6}>
                      <Statistic
                        title="当前价值"
                        value={selectedPortfolio.totalMarketValue}
                        formatter={(value) => formatCurrency(Number(value))}
                        prefix={<RiseOutlined />}
                      />
                    </Col>
                    <Col span={6}>
                      <Statistic
                        title="总收益"
                        value={selectedPortfolio.totalPnl}
                        formatter={(value) => formatCurrency(Number(value))}
                        valueStyle={{color: selectedPortfolio.totalPnl >= 0 ? '#3f8600' : '#cf1322'}}
                      />
                    </Col>
                    <Col span={6}>
                      <Statistic
                        title="今日收益"
                        value={selectedPortfolio.todayPnl}
                        formatter={(value) => formatCurrency(Number(value))}
                        valueStyle={{color: selectedPortfolio.todayPnl >= 0 ? '#3f8600' : '#cf1322'}}
                      />
                    </Col>
                    <Col span={6}>
                      <Statistic
                        title="现金余额"
                        value={selectedPortfolio.cashBalance}
                        formatter={(value) => formatCurrency(Number(value))}
                      />
                    </Col>
                  </Row>

                  {/* 风险指标 */}
                  <Row gutter={[16, 16]}>
                    <Col span={8}>
                      <Card size="small">
                        <Statistic
                          title="夏普比率"
                          value={selectedPortfolio.portfolio.sharpeRatio}
                          precision={2}
                        />
                      </Card>
                    </Col>
                    <Col span={8}>
                      <Card size="small">
                        <Statistic
                          title="最大回撤"
                          value={selectedPortfolio.portfolio.maxDrawdown * 100}
                          suffix="%"
                          precision={2}
                          valueStyle={{ color: '#cf1322' }}
                        />
                      </Card>
                    </Col>
                    <Col span={8}>
                      <Card size="small">
                        <Statistic
                          title="波动率"
                          value={selectedPortfolio.portfolio.volatility * 100}
                          suffix="%"
                          precision={2}
                        />
                      </Card>
                    </Col>
                  </Row>
                </TabPane>

                <TabPane tab="持仓明细" key="holdings">
                  <PortfolioHoldings portfolioId={selectedPortfolio.portfolio.id} />
                </TabPane>

                <TabPane tab="业绩分析" key="performance">
                  <PortfolioPerformance portfolioId={selectedPortfolio.portfolio.id} />
                </TabPane>

                <TabPane tab="图表分析" key="charts">
                  <PortfolioChart portfolioId={selectedPortfolio.portfolio.id} />
                </TabPane>
              </Tabs>
            </Card>
          </Col>
        )}
      </Row>

      {/* 创建投资组合模态框 */}
      <CreatePortfolioModal
        visible={createModalVisible}
        onCancel={() => setCreateModalVisible(false)}
        onSuccess={handleCreateSuccess}
      />
    </div>
  );
};

export default PortfolioPage;