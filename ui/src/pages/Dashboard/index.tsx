import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Table,
  List,
  Progress,
  Tag,
  Button,
  Select,

  Space,
  Typography,

  Spin,
  Empty
} from 'antd';
import {
  ArrowUpOutlined,
  ArrowDownOutlined,
  RiseOutlined,
  FallOutlined,
  DollarOutlined,

  ReloadOutlined,
  DownloadOutlined
} from '@ant-design/icons';
import { RiseOutlined as StockOutlined } from '@ant-design/icons';
import { Column, Pie } from '@ant-design/charts';
// import { useStore } from '../../store';
import { stockApi, analysisApi } from '../../services/stockApi';
import { formatNumber, formatPercent, formatCurrency, getChangeColor } from '../../utils/format';
import './index.css';

const { Title, Text } = Typography;
// const { RangePicker } = DatePicker;
const { Option } = Select;

interface DashboardData {
  marketOverview: {
    totalStocks: number;
    risingStocks: number;
    fallingStocks: number;
    flatStocks: number;
    totalMarketCap: number;
    totalVolume: number;
    avgChange: number;
  };
  topGainers: any[];
  topLosers: any[];
  activeStocks: any[];
  sectorPerformance: any[];
  recentAnalysis: any[];
  portfolioSummary: {
    totalValue: number;
    todayChange: number;
    todayChangePercent: number;
    totalReturn: number;
    totalReturnPercent: number;
  };
}

const Dashboard: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null);
  const [timeRange, setTimeRange] = useState('1D');
  const [selectedMarket, setSelectedMarket] = useState('ALL');
  const [refreshing, setRefreshing] = useState(false);

  // const { } = useStore();

  useEffect(() => {
    loadDashboardData();
  }, [timeRange, selectedMarket]);

  const loadDashboardData = async () => {
    try {
      setLoading(true);

      // 模拟API调用
      const [marketData, topGainersData, topLosersData, activeData, sectorData, analysisData] = await Promise.all([
        stockApi.getMarketOverview(),
        stockApi.getTopGainers(10),
        stockApi.getTopLosers(10),
        stockApi.getMostActive(10),
        stockApi.getSectorPerformance(),
        analysisApi.getRecentAnalysis(5)
      ]);

      setDashboardData({
        marketOverview: marketData || {
          totalStocks: 4532,
          risingStocks: 2156,
          fallingStocks: 1876,
          flatStocks: 500,
          totalMarketCap: 85600000000000,
          totalVolume: 456789000000,
          avgChange: 0.85
        },
        topGainers: topGainersData || generateMockStocks('gainers'),
        topLosers: topLosersData || generateMockStocks('losers'),
        activeStocks: activeData || generateMockStocks('active'),
        sectorPerformance: sectorData || generateMockSectors(),
        recentAnalysis: analysisData || generateMockAnalysis(),
        portfolioSummary: {
          totalValue: 1250000,
          todayChange: 15600,
          todayChangePercent: 1.26,
          totalReturn: 186500,
          totalReturnPercent: 17.5
        }
      });
    } catch (error) {
      console.error('加载仪表板数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = async () => {
    setRefreshing(true);
    await loadDashboardData();
    setRefreshing(false);
  };

  const generateMockStocks = (type: 'gainers' | 'losers' | 'active') => {
    const stocks = [
      {
        code: '000001',
        name: '平安银行',
        price: 12.45,
        change: type === 'gainers' ? 1.23 : type === 'losers' ? -0.87 : 0.45,
        changePercent: type === 'gainers' ? 10.98 : type === 'losers' ? -6.52 : 3.75,
        volume: 125600000
      },
      {
        code: '000002',
        name: '万科A',
        price: 18.76,
        change: type === 'gainers' ? 1.67 : type === 'losers' ? -1.23 : 0.23,
        changePercent: type === 'gainers' ? 9.78 : type === 'losers' ? -6.15 : 1.24,
        volume: 98700000
      },
      {
        code: '600036',
        name: '招商银行',
        price: 42.15,
        change: type === 'gainers' ? 3.85 : type === 'losers' ? -2.45 : 1.25,
        changePercent: type === 'gainers' ? 10.05 : type === 'losers' ? -5.49 : 3.05,
        volume: 87500000
      },
      {
        code: '600519',
        name: '贵州茅台',
        price: 1678.50,
        change: type === 'gainers' ? 152.30 : type === 'losers' ? -89.60 : 45.20,
        changePercent: type === 'gainers' ? 9.98 : type === 'losers' ? -5.07 : 2.77,
        volume: 12300000
      },
      {
        code: '000858',
        name: '五粮液',
        price: 156.78,
        change: type === 'gainers' ? 14.25 : type === 'losers' ? -8.90 : 3.45,
        changePercent: type === 'gainers' ? 9.99 : type === 'losers' ? -5.37 : 2.25,
        volume: 45600000
      }
    ];
    return stocks;
  };

  const generateMockSectors = () => {
    return [
      {
        name: '科技',
        change: 2.45,
        stocks: 156
      },
      {
        name: '金融',
        change: 1.23,
        stocks: 89
      },
      {
        name: '医药',
        change: -0.87,
        stocks: 124
      },
      {
        name: '消费',
        change: 0.56,
        stocks: 98
      },
      {
        name: '地产',
        change: -1.45,
        stocks: 67
      },
      {
        name: '能源',
        change: 3.21,
        stocks: 45
      }
    ];
  };

  const generateMockAnalysis = () => {
    return [
      {
        id: 1,
        stockCode: '000001',
        stockName: '平安银行',
        type: '技术分析',
        result: '买入',
        confidence: 85,
        createdAt: '2024-01-15 14:30:00'
      },
      {
        id: 2,
        stockCode: '600036',
        stockName: '招商银行',
        type: '基本面分析',
        result: '持有',
        confidence: 78,
        createdAt: '2024-01-15 13:45:00'
      },
      {
        id: 3,
        stockCode: '600519',
        stockName: '贵州茅台',
        type: '综合分析',
        result: '买入',
        confidence: 92,
        createdAt: '2024-01-15 12:20:00'
      },
      {
        id: 4,
        stockCode: '000858',
        stockName: '五粮液',
        type: '风险分析',
        result: '观望',
        confidence: 65,
        createdAt: '2024-01-15 11:15:00'
      },
      {
        id: 5,
        stockCode: '000002',
        stockName: '万科A',
        type: '情绪分析',
        result: '卖出',
        confidence: 72,
        createdAt: '2024-01-15 10:30:00'
      }
    ];
  };

  const stockColumns = [
    {
      title: '股票代码',
      dataIndex: 'code',
      key: 'code',
      render: (code: string) => <Text strong>{code}</Text>
    },
    {
      title: '股票名称',
      dataIndex: 'name',
      key: 'name'
    },
    {
      title: '现价',
      dataIndex: 'price',
      key: 'price',
      render: (price: number) => formatCurrency(price)
    },
    {
      title: '涨跌额',
      dataIndex: 'change',
      key: 'change',
      render: (change: number) => (
        <Text style={{ color: getChangeColor(change) }}>
          {change > 0 ? '+' : ''}{formatNumber(change, 2)}
        </Text>
      )
    },
    {
      title: '涨跌幅',
      dataIndex: 'changePercent',
      key: 'changePercent',
      render: (percent: number) => (
        <Text style={{ color: getChangeColor(percent) }}>
          {percent > 0 ? '+' : ''}{formatPercent(percent)}
        </Text>
      )
    },
    {
      title: '成交量',
      dataIndex: 'volume',
      key: 'volume',
      render: (volume: number) => formatNumber(volume)
    }
  ];

  const analysisColumns = [
    {
      title: '股票',
      key: 'stock',
      render: (record: any) => (
        <div>
          <Text strong>{record.stockCode}</Text>
          <br />
          <Text type="secondary">{record.stockName}</Text>
        </div>
      )
    },
    {
      title: '分析类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => <Tag color="blue">{type}</Tag>
    },
    {
      title: '结果',
      dataIndex: 'result',
      key: 'result',
      render: (result: string) => {
        const color = result === '买入' ? 'red' : result === '卖出' ? 'green' : 'orange';
        return <Tag color={color}>{result}</Tag>;
      }
    },
    {
      title: '置信度',
      dataIndex: 'confidence',
      key: 'confidence',
      render: (confidence: number) => (
        <Progress percent={confidence} size="small" />
      )
    },
    {
      title: '时间',
      dataIndex: 'createdAt',
      key: 'createdAt'
    }
  ];

  if (loading) {
    return (
      <div className="dashboard-loading">
        <Spin size="large" />
        <Text>加载仪表板数据中...</Text>
      </div>
    );
  }

  if (!dashboardData) {
    return (
      <div className="dashboard-error">
        <Empty description="暂无数据" />
        <Button type="primary" onClick={loadDashboardData}>重新加载</Button>
      </div>
    );
  }

  const { marketOverview: market, topGainers, topLosers, activeStocks, sectorPerformance, recentAnalysis, portfolioSummary } = dashboardData;

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <Title level={2}>股票分析仪表板</Title>
        <Space>
          <Select value={timeRange} onChange={setTimeRange}>
            <Option value="1D">今日</Option>
            <Option value="1W">本周</Option>
            <Option value="1M">本月</Option>
            <Option value="3M">三个月</Option>
          </Select>
          <Select value={selectedMarket} onChange={setSelectedMarket}>
            <Option value="ALL">全部市场</Option>
            <Option value="SH">上海</Option>
            <Option value="SZ">深圳</Option>
            <Option value="CY">创业板</Option>
          </Select>
          <Button
            icon={<ReloadOutlined />}
            loading={refreshing}
            onClick={handleRefresh}
          >
            刷新
          </Button>
          <Button icon={<DownloadOutlined />}>导出报告</Button>
        </Space>
      </div>

      {/* 市场概览 */}
      <Row gutter={[16, 16]} className="market-overview">
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="总股票数"
              value={market.totalStocks}
              prefix={<StockOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="上涨股票"
              value={market.risingStocks}
              prefix={<ArrowUpOutlined />}
              valueStyle={{ color: '#f5222d' }}
              suffix={`/ ${formatPercent((market.risingStocks / market.totalStocks) * 100)}`}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="下跌股票"
              value={market.fallingStocks}
              prefix={<ArrowDownOutlined />}
              valueStyle={{ color: '#52c41a' }}
              suffix={`/ ${formatPercent((market.fallingStocks / market.totalStocks) * 100)}`}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="平均涨跌幅"
              value={market.avgChange}
              precision={2}
              prefix={market.avgChange >= 0 ? <RiseOutlined /> : <FallOutlined />}
              valueStyle={{ color: getChangeColor(market.avgChange) }}
              suffix="%"
            />
          </Card>
        </Col>
      </Row>

      {/* 投资组合概览 */}
      <Row gutter={[16, 16]} className="portfolio-overview">
        <Col span={24}>
          <Card title="投资组合概览" extra={<Button type="link">查看详情</Button>}>
            <Row gutter={[16, 16]}>
              <Col xs={24} sm={12} md={6}>
                <Statistic
                  title="总市值"
                  value={portfolioSummary.totalValue}
                  prefix={<DollarOutlined />}
                  formatter={(value) => formatCurrency(Number(value))}
                  valueStyle={{ color: '#1890ff' }}
                />
              </Col>
              <Col xs={24} sm={12} md={6}>
                <Statistic
                  title="今日盈亏"
                  value={portfolioSummary.todayChange}
                  precision={2}
                  prefix={portfolioSummary.todayChange >= 0 ? <ArrowUpOutlined /> : <ArrowDownOutlined />}
                  formatter={(value) => formatCurrency(Number(value))}
                  valueStyle={{ color: getChangeColor(portfolioSummary.todayChange) }}
                  suffix={`(${formatPercent(portfolioSummary.todayChangePercent)})`}
                />
              </Col>
              <Col xs={24} sm={12} md={6}>
                <Statistic
                  title="总收益"
                  value={portfolioSummary.totalReturn}
                  precision={2}
                  prefix={portfolioSummary.totalReturn >= 0 ? <ArrowUpOutlined /> : <ArrowDownOutlined />}
                  formatter={(value) => formatCurrency(Number(value))}
                  valueStyle={{ color: getChangeColor(portfolioSummary.totalReturn) }}
                  suffix={`(${formatPercent(portfolioSummary.totalReturnPercent)})`}
                />
              </Col>
              <Col xs={24} sm={12} md={6}>
                <Statistic
                  title="总成交额"
                  value={market.totalVolume}
                  formatter={(value) => formatCurrency(Number(value))}
                  valueStyle={{ color: '#722ed1' }}
                />
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        {/* 涨幅榜 */}
        <Col xs={24} lg={8}>
          <Card title="涨幅榜" extra={<Button type="link">更多</Button>}>
            <Table
              dataSource={topGainers}
              columns={stockColumns.filter(col => !['volume'].includes(col.key as string))}
              pagination={false}
              size="small"
              rowKey="code"
            />
          </Card>
        </Col>

        {/* 跌幅榜 */}
        <Col xs={24} lg={8}>
          <Card title="跌幅榜" extra={<Button type="link">更多</Button>}>
            <Table
              dataSource={topLosers}
              columns={stockColumns.filter(col => !['volume'].includes(col.key as string))}
              pagination={false}
              size="small"
              rowKey="code"
            />
          </Card>
        </Col>

        {/* 成交活跃榜 */}
        <Col xs={24} lg={8}>
          <Card title="成交活跃榜" extra={<Button type="link">更多</Button>}>
            <Table
              dataSource={activeStocks}
              columns={stockColumns.filter(col => !['change', 'changePercent'].includes(col.key as string))}
              pagination={false}
              size="small"
              rowKey="code"
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        {/* 行业表现 */}
        <Col xs={24} lg={12}>
          <Card title="行业表现" extra={<Button type="link">更多</Button>}>
            <List
              dataSource={sectorPerformance}
              renderItem={(item: any) => (
                <List.Item>
                  <List.Item.Meta
                    title={item.name}
                    description={`${item.stocks} 只股票`}
                  />
                  <div>
                    <Text style={{ color: getChangeColor(item.change) }}>
                      {item.change > 0 ? '+' : ''}{formatPercent(item.change)}
                    </Text>
                  </div>
                </List.Item>
              )}
            />
          </Card>
        </Col>

        {/* 最近分析 */}
        <Col xs={24} lg={12}>
          <Card title="最近分析" extra={<Button type="link">查看全部</Button>}>
            <Table
              dataSource={recentAnalysis}
              columns={analysisColumns}
              pagination={false}
              size="small"
              rowKey="id"
            />
          </Card>
        </Col>
      </Row>

      {/* 市场热力图 */}
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card title="市场分布" extra={<Button type="link">切换视图</Button>}>
            <Row gutter={[16, 16]}>
              <Col xs={24} md={12}>
                <div className="chart-container">
                  <Pie
                    data={[
                      {
                        type: '上涨',
                        value: market.risingStocks
                      },
                      {
                        type: '下跌',
                        value: market.fallingStocks
                      },
                      {
                        type: '平盘',
                        value: market.flatStocks
                      }
                    ]}
                    angleField="value"
                    colorField="type"
                    radius={0.8}
                    label={{
                      type: 'outer',
                      content: '{name} {percentage}'
                    }}
                    color={['#f5222d', '#52c41a', '#d9d9d9']}
                  />
                </div>
              </Col>
              <Col xs={24} md={12}>
                <div className="chart-container">
                  <Column
                    data={sectorPerformance}
                    xField="name"
                    yField="change"
                    color={(datum: any) => datum.change >= 0 ? '#f5222d' : '#52c41a'}
                    label={{
                      position: 'top',
                      formatter: (datum: any) => `${datum.change > 0 ? '+' : ''}${datum.change.toFixed(2)}%`
                    }}
                  />
                </div>
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;