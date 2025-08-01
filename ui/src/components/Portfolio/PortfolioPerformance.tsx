import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Table,
  Progress,

  Select,
  // DatePicker,
  Button,
  Space,
  Alert,
  Descriptions,
  Divider,
  message,
  Spin
} from 'antd';
import {
  RiseOutlined,
  FallOutlined,
  ReloadOutlined,
  ReloadOutlined,
  ExportOutlined,
  WarningOutlined
} from '@ant-design/icons';
import { portfolioApi, PortfolioMetrics, PerformanceAnalysis, PortfolioRiskAnalysis } from '../../services/portfolioApi';
import ReactECharts from 'echarts-for-react';
import dayjs from 'dayjs';

// const { Option } = Select;
// const { RangePicker } = DatePicker;

interface PortfolioPerformanceProps {
  portfolioId: number;
}

const PortfolioPerformance: React.FC<PortfolioPerformanceProps> = ({ portfolioId }) => {
  const [loading, setLoading] = useState(false);
  const [metrics, setMetrics] = useState<PortfolioMetrics | null>(null);
  const [performance, setPerformance] = useState<PerformanceAnalysis | null>(null);
  const [riskAnalysis, setRiskAnalysis] = useState<PortfolioRiskAnalysis | null>(null);
  const [timeRange, setTimeRange] = useState('1Y');

  // 时间范围选项
  const timeRangeOptions = [
    {
      value: '1M',
      label: '1个月'
    },
    {
      value: '3M',
      label: '3个月'
    },
    {
      value: '6M',
      label: '6个月'
    },
    {
      value: '1Y',
      label: '1年'
    },
    {
      value: '3Y',
      label: '3年'
    },
    {
      value: 'ALL',
      label: '全部'
    }
  ];

  // 加载数据
  const loadData = async () => {
    try {
      setLoading(true);
      const [metricsRes, performanceRes, riskRes] = await Promise.all([
        portfolioApi.getPortfolioMetrics(portfolioId),
        portfolioApi.getPerformanceAnalysis(portfolioId, timeRange),
        portfolioApi.getRiskAnalysis(portfolioId)
      ]);

      setMetrics(metricsRes.data);
      setPerformance(performanceRes.data);
      setRiskAnalysis(riskRes.data);
    } catch (error) {
      console.error('加载业绩数据失败:', error);
      message.error('加载业绩数据失败');
    } finally {
      setLoading(false);
    }
  };

  // 格式化百分比
  const formatPercent = (value: number, precision = 2) => {
    const color = value >= 0 ? '#52c41a' : '#ff4d4f';
    const prefix = value >= 0 ? '+' : '';
    return (
      <span style={{
        color,
        fontWeight: 'bold'
      }}>
        {prefix}{(value * 100).toFixed(precision)}%
      </span>
    );
  };

  // 格式化金额
  // const formatCurrency = (value: number) => {
  //   return new Intl.NumberFormat('zh-CN', {
  //     style: 'currency',
  //     currency: 'CNY',
  //     minimumFractionDigits: 2
  //   }).format(value);
  // };

  // 获取风险等级颜色
  // const getRiskColor = (risk: string) => {
  //   const colorMap: Record<string, string> = {
  //     '低': 'green',
  //     '中': 'orange',
  //     '高': 'red',
  //     '极高': 'purple'
  //   };
  //   return colorMap[risk] || 'default';
  // };

  // 滚动指标图表配置
  const getRollingMetricsOption = () => {
    if (!performance) return {};

    const dates = performance.rollingMetrics.map(item => item.date);
    const sharpeRatios = performance.rollingMetrics.map(item => item.sharpe);
    const volatilities = performance.rollingMetrics.map(item => item.volatility * 100);
    const maxDrawdowns = performance.rollingMetrics.map(item => item.maxDrawdown * 100);

    return {
      title: {
        text: '滚动风险指标',
        left: 'center',
        textStyle: {
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {type: 'cross'}
      },
      legend: {
        data: ['夏普比率', '波动率', '最大回撤'],
        top: 30
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates,
        axisLabel: {formatter: (value: string) => dayjs(value).format('MM-DD')}
      },
      yAxis: [
        {
          type: 'value',
          name: '夏普比率',
          position: 'left',
          axisLabel: {formatter: '{value}'}
        },
        {
          type: 'value',
          name: '百分比 (%)',
          position: 'right',
          axisLabel: {formatter: '{value}%'}
        }
      ],
      series: [
        {
          name: '夏普比率',
          type: 'line',
          yAxisIndex: 0,
          data: sharpeRatios,
          smooth: true,
          lineStyle: {
            color: '#1890ff',
            width: 2
          }
        },
        {
          name: '波动率',
          type: 'line',
          yAxisIndex: 1,
          data: volatilities,
          smooth: true,
          lineStyle: {
            color: '#52c41a',
            width: 2
          }
        },
        {
          name: '最大回撤',
          type: 'line',
          yAxisIndex: 1,
          data: maxDrawdowns,
          smooth: true,
          lineStyle: {
            color: '#ff4d4f',
            width: 2
          }
        }
      ]
    };
  };

  // 年度收益表格列
  const yearlyColumns = [
    {
      title: '年份',
      dataIndex: 'year',
      key: 'year',
      width: 100
    },
    {
      title: '年度收益率',
      dataIndex: 'return',
      key: 'return',
      render: (value: number) => formatPercent(value)
    }
  ];

  // 风险贡献表格列
  const riskContributionColumns = [
    {
      title: '股票代码',
      dataIndex: 'stockCode',
      key: 'stockCode',
      width: 120
    },
    {
      title: '风险贡献度',
      dataIndex: 'contribution',
      key: 'contribution',
      render: (value: number) => (
        <div>
          <div>{formatPercent(value)}</div>
          <Progress
            percent={value * 100}
            size="small"
            showInfo={false}
            strokeColor={value > 0.1 ? '#ff4d4f' : '#52c41a'}
          />
        </div>
      )
    }
  ];

  // 压力测试表格列
  const stressTestColumns = [
    {
      title: '压力情景',
      dataIndex: 'scenario',
      key: 'scenario',
      width: 150
    },
    {
      title: '预期影响',
      dataIndex: 'impact',
      key: 'impact',
      render: (value: number) => (
        <span style={{
          color: value < 0 ? '#ff4d4f' : '#52c41a',
          fontWeight: 'bold'
        }}>
          {value > 0 ? '+' : ''}{(value * 100).toFixed(2)}%
        </span>
      )
    }
  ];

  useEffect(() => {
    if (portfolioId) {
      loadData();
    }
  }, [portfolioId, timeRange]);

  return (
    <Spin spinning={loading}>
      <div>
        {/* 控制面板 */}
        <Card size="small" style={{ marginBottom: 16 }}>
          <Row gutter={16} align="middle">
            <Col>
              <Space>
                <span>分析周期:</span>
                <Select
                  value={timeRange}
                  onChange={setTimeRange}
                  style={{ width: 120 }}
                >
                  {timeRangeOptions.map(option => (
                    <Option key={option.value} value={option.value}>
                      {option.label}
                    </Option>
                  ))}
                </Select>
              </Space>
            </Col>
            <Col flex="auto" />
            <Col>
              <Space>
                <Button
                  icon={<ReloadOutlined />}
                  onClick={loadData}
                  loading={loading}
                >
                  刷新
                </Button>
                <Button icon={<ExportOutlined />}>
                  导出报告
                </Button>
              </Space>
            </Col>
          </Row>
        </Card>

        {/* 核心指标 */}
        {metrics && (
          <Card title="核心业绩指标" style={{ marginBottom: 16 }}>
            <Row gutter={[16, 16]}>
              <Col span={6}>
                <Statistic
                  title="总收益率"
                  value={metrics.totalReturnPercent * 100}
                  precision={2}
                  suffix="%"
                  valueStyle={{color: metrics.totalReturnPercent >= 0 ? '#3f8600' : '#cf1322'}}
                  prefix={
                    metrics.totalReturnPercent >= 0 ?
                      <RiseOutlined /> :
                      <FallOutlined />
                  }
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="年化收益率"
                  value={metrics.annualizedReturn * 100}
                  precision={2}
                  suffix="%"
                  valueStyle={{color: metrics.annualizedReturn >= 0 ? '#3f8600' : '#cf1322'}}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="夏普比率"
                  value={metrics.sharpeRatio}
                  precision={2}
                  valueStyle={{color: metrics.sharpeRatio >= 1 ? '#3f8600' : metrics.sharpeRatio >= 0.5 ? '#faad14' : '#cf1322'}}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="最大回撤"
                  value={metrics.maxDrawdown * 100}
                  precision={2}
                  suffix="%"
                  valueStyle={{ color: '#cf1322' }}
                  prefix={<FallOutlined />}
                />
              </Col>
            </Row>

            <Divider />

            <Row gutter={[16, 16]}>
              <Col span={6}>
                <Statistic
                  title="波动率"
                  value={metrics.volatility * 100}
                  precision={2}
                  suffix="%"
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="Beta系数"
                  value={metrics.beta}
                  precision={2}
                  valueStyle={{color: metrics.beta > 1 ? '#cf1322' : '#3f8600'}}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="Alpha系数"
                  value={metrics.alpha * 100}
                  precision={2}
                  suffix="%"
                  valueStyle={{color: metrics.alpha >= 0 ? '#3f8600' : '#cf1322'}}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="信息比率"
                  value={metrics.informationRatio}
                  precision={2}
                  valueStyle={{color: metrics.informationRatio >= 0.5 ? '#3f8600' : '#faad14'}}
                />
              </Col>
            </Row>
          </Card>
        )}

        {/* 风险分析 */}
        {riskAnalysis && (
          <Card
            title={
              <span>
                <WarningOutlined style={{
                  color: '#faad14',
                  marginRight: 8
                }} />
                风险分析
              </span>
            }
            style={{ marginBottom: 16 }}
          >
            <Alert
              message={`整体风险等级: ${riskAnalysis.overallRisk}`}
              description={`风险评分: ${riskAnalysis.riskScore}/100`}
              type={riskAnalysis.overallRisk === '高' ? 'error' : riskAnalysis.overallRisk === '中' ? 'warning' : 'success'}
              showIcon
              style={{ marginBottom: 16 }}
            />

            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="95% VaR">
                <span style={{
                  color: '#cf1322',
                  fontWeight: 'bold'
                }}>
                  {formatPercent(riskAnalysis.var95)}
                </span>
              </Descriptions.Item>
              <Descriptions.Item label="99% VaR">
                <span style={{
                  color: '#cf1322',
                  fontWeight: 'bold'
                }}>
                  {formatPercent(riskAnalysis.var99)}
                </span>
              </Descriptions.Item>
              <Descriptions.Item label="预期损失">
                <span style={{
                  color: '#cf1322',
                  fontWeight: 'bold'
                }}>
                  {formatPercent(riskAnalysis.expectedShortfall)}
                </span>
              </Descriptions.Item>
              <Descriptions.Item label="集中度风险">
                <span style={{
                  color: riskAnalysis.concentrationRisk > 0.3 ? '#cf1322' : '#3f8600',
                  fontWeight: 'bold'
                }}>
                  {formatPercent(riskAnalysis.concentrationRisk)}
                </span>
              </Descriptions.Item>
            </Descriptions>
          </Card>
        )}

        <Row gutter={[16, 16]}>
          {/* 滚动指标图表 */}
          <Col span={24}>
            <Card title="滚动风险指标">
              <ReactECharts
                option={getRollingMetricsOption()}
                style={{ height: '400px' }}
                notMerge={true}
              />
            </Card>
          </Col>

          {/* 年度收益 */}
          <Col span={8}>
            <Card title="年度收益率">
              <Table
                columns={yearlyColumns}
                dataSource={performance?.yearlyReturns || []}
                rowKey="year"
                pagination={false}
                size="small"
              />
            </Card>
          </Col>

          {/* 风险贡献 */}
          <Col span={8}>
            <Card title="风险贡献度">
              <Table
                columns={riskContributionColumns}
                dataSource={riskAnalysis?.riskContribution || []}
                rowKey="stockCode"
                pagination={false}
                size="small"
              />
            </Card>
          </Col>

          {/* 压力测试 */}
          <Col span={8}>
            <Card title="压力测试">
              <Table
                columns={stressTestColumns}
                dataSource={riskAnalysis?.stressTestResults || []}
                rowKey="scenario"
                pagination={false}
                size="small"
              />
            </Card>
          </Col>
        </Row>

        {/* 业绩归因分析 */}
        {performance?.attribution && (
          <Card title="业绩归因分析" style={{ marginTop: 16 }}>
            <Row gutter={[16, 16]}>
              <Col span={6}>
                <Statistic
                  title="股票选择贡献"
                  value={performance.attribution.stockSelection * 100}
                  precision={2}
                  suffix="%"
                  valueStyle={{color: performance.attribution.stockSelection >= 0 ? '#3f8600' : '#cf1322'}}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="行业配置贡献"
                  value={performance.attribution.sectorAllocation * 100}
                  precision={2}
                  suffix="%"
                  valueStyle={{color: performance.attribution.sectorAllocation >= 0 ? '#3f8600' : '#cf1322'}}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="交互效应"
                  value={performance.attribution.interaction * 100}
                  precision={2}
                  suffix="%"
                  valueStyle={{color: performance.attribution.interaction >= 0 ? '#3f8600' : '#cf1322'}}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="总超额收益"
                  value={performance.attribution.total * 100}
                  precision={2}
                  suffix="%"
                  valueStyle={{color: performance.attribution.total >= 0 ? '#3f8600' : '#cf1322'}}
                />
              </Col>
            </Row>
          </Card>
        )}
      </div>
    </Spin>
  );
};

export default PortfolioPerformance;