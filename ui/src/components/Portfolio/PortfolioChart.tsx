import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Select,

  Button,
  Space,
  Spin,
  message,
  Tabs,
  Radio
} from 'antd';
import {
  LineChartOutlined,
  PieChartOutlined,

  AreaChartOutlined,
  ReloadOutlined
} from '@ant-design/icons';
import ReactECharts from 'echarts-for-react';
import { portfolioApi, PerformanceAnalysis, AssetAllocationAnalysis } from '../../services/portfolioApi';
import dayjs from 'dayjs';

const { Option } = Select;
// const { RangePicker } = DatePicker;
const { TabPane } = Tabs;

interface PortfolioChartProps {
  portfolioId: number;
}

const PortfolioChart: React.FC<PortfolioChartProps> = ({ portfolioId }) => {
  const [loading, setLoading] = useState(false);
  const [performanceData, setPerformanceData] = useState<PerformanceAnalysis | null>(null);
  const [allocationData, setAllocationData] = useState<AssetAllocationAnalysis | null>(null);
  const [timeRange, setTimeRange] = useState('1M');
  const [chartType, setChartType] = useState('line');
  const [activeTab, setActiveTab] = useState('performance');

  // 时间范围选项
  const timeRangeOptions = [
    {
      value: '1W',
      label: '1周'
    },
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
      value: 'YTD',
      label: '今年以来'
    },
    {
      value: 'ALL',
      label: '全部'
    }
  ];

  // 加载业绩数据
  const loadPerformanceData = async () => {
    try {
      setLoading(true);
      const response = await portfolioApi.getPerformanceAnalysis(portfolioId, timeRange);
      setPerformanceData(response.data);
    } catch (error) {
      console.error('加载业绩数据失败:', error);
      message.error('加载业绩数据失败');
    } finally {
      setLoading(false);
    }
  };

  // 加载资产配置数据
  const loadAllocationData = async () => {
    try {
      const response = await portfolioApi.getAssetAllocation(portfolioId);
      setAllocationData(response.data);
    } catch (error) {
      console.error('加载资产配置数据失败:', error);
      message.error('加载资产配置数据失败');
    }
  };

  // 净值走势图配置
  const getPerformanceChartOption = () => {
    if (!performanceData) return {};

    const dates = performanceData.dailyReturns.map(item => item.date);
    const portfolioReturns = performanceData.dailyReturns.map(item => (item.cumReturn * 100).toFixed(2));
    const benchmarkReturns = performanceData.benchmarkComparison.map(item => (item.benchmarkReturn * 100).toFixed(2));

    return {
      title: {
        text: '投资组合净值走势',
        left: 'center',
        textStyle: {
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {type: 'cross'},
        formatter: (params: any) => {
          let result = `${params[0].axisValue}<br/>`;
          params.forEach((param: any) => {
            result += `${param.seriesName}: ${param.value}%<br/>`;
          });
          return result;
        }
      },
      legend: {
        data: ['投资组合', '基准指数'],
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
      yAxis: {
        type: 'value',
        axisLabel: {formatter: '{value}%'}
      },
      series: [
        {
          name: '投资组合',
          type: chartType,
          data: portfolioReturns,
          smooth: true,
          lineStyle: {
            color: '#1890ff',
            width: 2
          },
          areaStyle: chartType === 'line' ? undefined : {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                {
                  offset: 0,
                  color: 'rgba(24, 144, 255, 0.3)'
                },
                {
                  offset: 1,
                  color: 'rgba(24, 144, 255, 0.1)'
                }
              ]
            }
          }
        },
        {
          name: '基准指数',
          type: 'line',
          data: benchmarkReturns,
          smooth: true,
          lineStyle: {
            color: '#52c41a',
            width: 2,
            type: 'dashed'
          }
        }
      ]
    };
  };

  // 资产配置饼图配置
  const getAllocationPieOption = (data: Array<{ name: string; value: number; weight: number }>, title: string) => {
    return {
      title: {
        text: title,
        left: 'center',
        textStyle: {
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        top: 'middle',
        textStyle: {fontSize: 12}
      },
      series: [
        {
          name: title,
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['60%', '50%'],
          avoidLabelOverlap: false,
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '18',
              fontWeight: 'bold'
            }
          },
          labelLine: {show: false},
          data: data.map(item => ({
            name: item.name,
            value: item.value
          }))
        }
      ]
    };
  };

  // 月度收益柱状图配置
  const getMonthlyReturnsOption = () => {
    if (!performanceData) return {};

    const months = performanceData.monthlyReturns.map(item => item.month);
    const returns = performanceData.monthlyReturns.map(item => (item.return * 100).toFixed(2));

    return {
      title: {
        text: '月度收益率',
        left: 'center',
        textStyle: {
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      tooltip: {
        trigger: 'axis',
        formatter: '{b}: {c}%'
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
        data: months,
        axisLabel: {rotate: 45}
      },
      yAxis: {
        type: 'value',
        axisLabel: {formatter: '{value}%'}
      },
      series: [
        {
          type: 'bar',
          data: returns.map(value => ({
            value,
            itemStyle: {color: parseFloat(value) >= 0 ? '#52c41a' : '#ff4d4f'}
          })),
          barWidth: '60%'
        }
      ]
    };
  };

  // 风险收益散点图配置
  const getRiskReturnOption = () => {
    if (!performanceData || !allocationData) return {};

    const data = allocationData.byStock.map(stock => [
      Math.random() * 20, // 模拟风险数据
      Math.random() * 15, // 模拟收益数据
      stock.weight * 1000, // 权重作为气泡大小
      stock.stockCode
    ]);

    return {
      title: {
        text: '风险收益分析',
        left: 'center',
        textStyle: {
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      tooltip: {
        trigger: 'item',
        formatter: (params: any) => {
          return `${params.data[3]}<br/>风险: ${params.data[0].toFixed(2)}%<br/>收益: ${params.data[1].toFixed(2)}%<br/>权重: ${(params.data[2] / 10).toFixed(2)}%`;
        }
      },
      grid: {
        left: '3%',
        right: '7%',
        bottom: '3%',
        top: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'value',
        name: '风险 (%)',
        nameLocation: 'middle',
        nameGap: 30
      },
      yAxis: {
        type: 'value',
        name: '收益 (%)',
        nameLocation: 'middle',
        nameGap: 40
      },
      series: [
        {
          type: 'scatter',
          symbolSize: (data: number[]) => Math.sqrt(data[2]) * 2,
          data: data,
          itemStyle: {
            color: '#1890ff',
            opacity: 0.7
          },
          emphasis: {itemStyle: {opacity: 1}}
        }
      ]
    };
  };

  // 刷新数据
  const handleRefresh = () => {
    if (activeTab === 'performance') {
      loadPerformanceData();
    } else {
      loadAllocationData();
    }
  };

  useEffect(() => {
    if (portfolioId) {
      loadPerformanceData();
      loadAllocationData();
    }
  }, [portfolioId, timeRange]);

  return (
    <div>
      {/* 控制面板 */}
      <Card size="small" style={{ marginBottom: 16 }}>
        <Row gutter={16} align="middle">
          <Col>
            <Space>
              <span>时间范围:</span>
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
          <Col>
            <Space>
              <span>图表类型:</span>
              <Radio.Group
                value={chartType}
                onChange={(e) => setChartType(e.target.value)}
                size="small"
              >
                <Radio.Button value="line">
                  <LineChartOutlined /> 折线图
                </Radio.Button>
                <Radio.Button value="area">
                  <AreaChartOutlined /> 面积图
                </Radio.Button>
              </Radio.Group>
            </Space>
          </Col>
          <Col flex="auto" />
          <Col>
            <Button
              icon={<ReloadOutlined />}
              onClick={handleRefresh}
              loading={loading}
            >
              刷新
            </Button>
          </Col>
        </Row>
      </Card>

      {/* 图表内容 */}
      <Tabs activeKey={activeTab} onChange={setActiveTab}>
        <TabPane
          tab={
            <span>
              <LineChartOutlined />
              业绩分析
            </span>
          }
          key="performance"
        >
          <Spin spinning={loading}>
            <Row gutter={[16, 16]}>
              {/* 净值走势图 */}
              <Col span={24}>
                <Card>
                  <ReactECharts
                    option={getPerformanceChartOption()}
                    style={{ height: '400px' }}
                    notMerge={true}
                  />
                </Card>
              </Col>

              {/* 月度收益图 */}
              <Col span={12}>
                <Card>
                  <ReactECharts
                    option={getMonthlyReturnsOption()}
                    style={{ height: '300px' }}
                    notMerge={true}
                  />
                </Card>
              </Col>

              {/* 风险收益散点图 */}
              <Col span={12}>
                <Card>
                  <ReactECharts
                    option={getRiskReturnOption()}
                    style={{ height: '300px' }}
                    notMerge={true}
                  />
                </Card>
              </Col>
            </Row>
          </Spin>
        </TabPane>

        <TabPane
          tab={
            <span>
              <PieChartOutlined />
              资产配置
            </span>
          }
          key="allocation"
        >
          <Spin spinning={loading}>
            <Row gutter={[16, 16]}>
              {/* 按股票配置 */}
              <Col span={12}>
                <Card>
                  <ReactECharts
                    option={getAllocationPieOption(
                      allocationData?.byStock.map(item => ({
                        name: item.stockCode,
                        value: item.value,
                        weight: item.weight
                      })) || [],
                      '按股票配置'
                    )}
                    style={{ height: '350px' }}
                    notMerge={true}
                  />
                </Card>
              </Col>

              {/* 按行业配置 */}
              <Col span={12}>
                <Card>
                  <ReactECharts
                    option={getAllocationPieOption(
                      allocationData?.bySector.map(item => ({
                        name: item.sector,
                        value: item.value,
                        weight: item.weight
                      })) || [],
                      '按行业配置'
                    )}
                    style={{ height: '350px' }}
                    notMerge={true}
                  />
                </Card>
              </Col>

              {/* 按市场配置 */}
              <Col span={12}>
                <Card>
                  <ReactECharts
                    option={getAllocationPieOption(
                      allocationData?.byMarket.map(item => ({
                        name: item.market,
                        value: item.value,
                        weight: item.weight
                      })) || [],
                      '按市场配置'
                    )}
                    style={{ height: '350px' }}
                    notMerge={true}
                  />
                </Card>
              </Col>

              {/* 按风险等级配置 */}
              <Col span={12}>
                <Card>
                  <ReactECharts
                    option={getAllocationPieOption(
                      allocationData?.byRiskLevel.map(item => ({
                        name: item.riskLevel,
                        value: item.value,
                        weight: item.weight
                      })) || [],
                      '按风险等级配置'
                    )}
                    style={{ height: '350px' }}
                    notMerge={true}
                  />
                </Card>
              </Col>
            </Row>
          </Spin>
        </TabPane>
      </Tabs>
    </div>
  );
};

export default PortfolioChart;