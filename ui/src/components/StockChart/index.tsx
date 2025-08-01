/**
 * 股票价格图表组件
 * 支持K线图、成交量、技术指标等
 */

import React, { useState, useEffect, useRef } from 'react';
import {
  Card,
  Select,
  Button,
  Space,
  Spin,
  message,
  Tooltip,
  Radio
} from 'antd';
import {
  FullscreenOutlined,
  FullscreenExitOutlined,
  DownloadOutlined,
  ReloadOutlined
} from '@ant-design/icons';
import * as echarts from 'echarts';
import { stockApi } from '../../services/stockApi';
import { useStockStore } from '../../store';
import { KLineData, StockPrice } from '../../types/stock';
import { formatNumber, formatPercent } from '../../utils';
import './index.css';

interface StockChartProps {
  stockCode: string;
  height?: number;
  showToolbar?: boolean;
  showVolume?: boolean;
  showIndicators?: boolean;
  className?: string;
}

type PeriodType = '1d' | '5d' | '1m' | '3m' | '6m' | '1y' | '2y' | '5y';
type ChartType = 'candlestick' | 'line' | 'area';
type IndicatorType = 'MA' | 'EMA' | 'BOLL' | 'MACD' | 'RSI' | 'KDJ';

const StockChart: React.FC<StockChartProps> = ({
  stockCode,
  height = 400,
  showToolbar = true,
  showVolume = true,
  showIndicators = true,
  className
}) => {
  const chartRef = useRef<HTMLDivElement>(null);
  const chartInstance = useRef<echarts.ECharts | null>(null);

  const [loading, setLoading] = useState(false);
  const [fullscreen, setFullscreen] = useState(false);
  const [period, setPeriod] = useState<PeriodType>('1m');
  const [chartType] = useState<ChartType>('candlestick');
  const [selectedIndicators, setSelectedIndicators] = useState<IndicatorType[]>(['MA']);
  const [klineData, setKlineData] = useState<KLineData[]>([]);
  const [realTimePrice, setRealTimePrice] = useState<StockPrice | null>(null);

  const { realTimePrices } = useStockStore();

  // 时间周期选项
  const periodOptions = [
    {
      label: '1天',
      value: '1d'
    },
    {
      label: '5天',
      value: '5d'
    },
    {
      label: '1月',
      value: '1m'
    },
    {
      label: '3月',
      value: '3m'
    },
    {
      label: '6月',
      value: '6m'
    },
    {
      label: '1年',
      value: '1y'
    },
    {
      label: '2年',
      value: '2y'
    },
    {
      label: '5年',
      value: '5y'
    }
  ];

  // 技术指标选项
  const indicatorOptions = [
    {
      label: '移动平均线(MA)',
      value: 'MA'
    },
    {
      label: '指数移动平均(EMA)',
      value: 'EMA'
    },
    {
      label: '布林带(BOLL)',
      value: 'BOLL'
    },
    {
      label: 'MACD',
      value: 'MACD'
    },
    {
      label: 'RSI',
      value: 'RSI'
    },
    {
      label: 'KDJ',
      value: 'KDJ'
    }
  ];

  // 获取K线数据
  const fetchKLineData = async () => {
    if (!stockCode) return;

    setLoading(true);
    try {
      const response = await stockApi.getKLineData(stockCode, period);
      if (response.success && response.data) {
        setKlineData(response.data);
      }
    } catch (error) {
      console.error('获取K线数据失败:', error);
      message.error('获取K线数据失败');
    } finally {
      setLoading(false);
    }
  };

  // 初始化图表
  const initChart = () => {
    if (!chartRef.current) return;

    if (chartInstance.current) {
      chartInstance.current.dispose();
    }

    chartInstance.current = echarts.init(chartRef.current);
    updateChart();
  };

  // 更新图表
  const updateChart = () => {
    if (!chartInstance.current || klineData.length === 0) return;

    const dates = klineData.map(item => item.time);
    const values = klineData.map(item => [item.open, item.close, item.low, item.high]);
    const volumes = klineData.map(item => item.volume);
    const closes = klineData.map(item => item.close);

    // 计算技术指标
    const indicators = calculateIndicators(closes);

    const option: echarts.EChartsOption = {
      animation: false,
      legend: {
        bottom: 10,
        left: 'center',
        data: ['K线', '成交量', ...selectedIndicators]
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {type: 'cross'},
        backgroundColor: 'rgba(245, 245, 245, 0.8)',
        borderWidth: 1,
        borderColor: '#ccc',
        textStyle: {color: '#000'},
        formatter: (params: any) => {
          const dataIndex = params[0].dataIndex;
          const data = klineData[dataIndex];
          if (!data) return '';

          return `
            <div style="margin: 0px 0 0;line-height:1;">
              <div style="font-size:14px;color:#666;font-weight:400;line-height:1;">
                ${data.time}
              </div>
              <div style="margin: 10px 0 0;line-height:1;">
                <div style="margin: 0px 0 0;line-height:1;">
                  <span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:#c23531;"></span>
                  开盘: ${formatNumber(data.open)}
                </div>
                <div style="margin: 0px 0 0;line-height:1;">
                  <span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:#2f4554;"></span>
                  收盘: ${formatNumber(data.close)}
                </div>
                <div style="margin: 0px 0 0;line-height:1;">
                  <span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:#61a0a8;"></span>
                  最高: ${formatNumber(data.high)}
                </div>
                <div style="margin: 0px 0 0;line-height:1;">
                  <span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:#d48265;"></span>
                  最低: ${formatNumber(data.low)}
                </div>
                <div style="margin: 0px 0 0;line-height:1;">
                  <span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:#91c7ae;"></span>
                  成交量: ${formatNumber(data.volume)}
                </div>
                ${data.changePercent ? `
                <div style="margin: 0px 0 0;line-height:1;">
                  <span style="display:inline-block;margin-right:5px;border-radius:10px;width:10px;height:10px;background-color:#749f83;"></span>
                  涨跌幅: ${formatPercent(data.changePercent)}
                </div>
                ` : ''}
              </div>
            </div>
          `;
        }
      },
      axisPointer: {
        link: { xAxisIndex: 'all' },
        label: {backgroundColor: '#777'}
      },
      toolbox: {
        feature: {
          dataZoom: {yAxisIndex: false},
          brush: {type: ['lineX', 'clear']}
        }
      },
      brush: {
        xAxisIndex: 'all',
        brushLink: 'all',
        outOfBrush: {colorAlpha: 0.1}
      },
      visualMap: {
        show: false,
        seriesIndex: 5,
        dimension: 2,
        pieces: [
          {
            value: 1,
            color: '#00da3c'
          },
          {
            value: -1,
            color: '#ec0000'
          }
        ]
      },
      grid: [
        {
          left: '10%',
          right: '8%',
          height: showVolume ? '50%' : '65%'
        },
        {
          left: '10%',
          right: '8%',
          top: '63%',
          height: '16%'
        }
      ],
      xAxis: [
        {
          type: 'category',
          data: dates,
          scale: true,
          boundaryGap: false,
          axisLine: { onZero: false },
          splitLine: { show: false },
          splitNumber: 20,
          min: 'dataMin',
          max: 'dataMax',
          axisPointer: {z: 100}
        },
        {
          type: 'category',
          gridIndex: 1,
          data: dates,
          scale: true,
          boundaryGap: false,
          axisLine: { onZero: false },
          axisTick: { show: false },
          splitLine: { show: false },
          axisLabel: { show: false },
          splitNumber: 20,
          min: 'dataMin',
          max: 'dataMax'
        }
      ],
      yAxis: [
        {
          scale: true,
          splitArea: {show: true}
        },
        {
          scale: true,
          gridIndex: 1,
          splitNumber: 2,
          axisLabel: { show: false },
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: { show: false }
        }
      ],
      dataZoom: [
        {
          type: 'inside',
          xAxisIndex: [0, 1],
          start: 98,
          end: 100
        },
        {
          show: true,
          xAxisIndex: [0, 1],
          type: 'slider',
          top: '85%',
          start: 98,
          end: 100
        }
      ],
      series: [
        {
          name: 'K线',
          type: 'candlestick',
          data: values,
          itemStyle: {
            color: '#00da3c',
            color0: '#ec0000',
            borderColor: undefined,
            borderColor0: undefined
          }
        },
        ...getIndicatorSeries(indicators),
        ...(showVolume ? [
          {
            name: '成交量',
            type: 'bar',
            xAxisIndex: 1,
            yAxisIndex: 1,
            data: volumes.map((volume, index) => {
              const current = klineData[index];
              const color = current.close >= current.open ? '#00da3c' : '#ec0000';
              return {
                value: volume,
                itemStyle: { color }
              };
            })
          }
        ] : [])
      ]
    };

    chartInstance.current.setOption(option, true);
  };

  // 计算技术指标
  const calculateIndicators = (closes: number[]) => {
    const indicators: any = {};

    if (selectedIndicators.includes('MA')) {
      indicators.MA5 = calculateMA(closes, 5);
      indicators.MA10 = calculateMA(closes, 10);
      indicators.MA20 = calculateMA(closes, 20);
      indicators.MA30 = calculateMA(closes, 30);
    }

    if (selectedIndicators.includes('EMA')) {
      indicators.EMA12 = calculateEMA(closes, 12);
      indicators.EMA26 = calculateEMA(closes, 26);
    }

    if (selectedIndicators.includes('BOLL')) {
      const boll = calculateBOLL(closes, 20, 2);
      indicators.BOLL_UPPER = boll.upper;
      indicators.BOLL_MIDDLE = boll.middle;
      indicators.BOLL_LOWER = boll.lower;
    }

    return indicators;
  };

  // 计算移动平均线
  const calculateMA = (data: number[], period: number) => {
    const result = [];
    for (let i = 0; i < data.length; i++) {
      if (i < period - 1) {
        result.push(null);
      } else {
        const sum = data.slice(i - period + 1, i + 1).reduce((a, b) => a + b, 0);
        result.push(sum / period);
      }
    }
    return result;
  };

  // 计算指数移动平均线
  const calculateEMA = (data: number[], period: number) => {
    const result = [];
    const multiplier = 2 / (period + 1);
    let ema = data[0];
    result.push(ema);

    for (let i = 1; i < data.length; i++) {
      ema = (data[i] - ema) * multiplier + ema;
      result.push(ema);
    }
    return result;
  };

  // 计算布林带
  const calculateBOLL = (data: number[], period: number, multiplier: number) => {
    const ma = calculateMA(data, period);
    const upper = [];
    const lower = [];

    for (let i = 0; i < data.length; i++) {
      if (i < period - 1) {
        upper.push(null);
        lower.push(null);
      } else {
        const slice = data.slice(i - period + 1, i + 1);
        const mean = ma[i];
        const variance = slice.reduce((sum, val) => sum + Math.pow(val - mean!, 2), 0) / period;
        const stdDev = Math.sqrt(variance);
        upper.push(mean! + multiplier * stdDev);
        lower.push(mean! - multiplier * stdDev);
      }
    }

    return {
      upper,
      middle: ma,
      lower
    };
  };

  // 获取技术指标系列
  const getIndicatorSeries = (indicators: any) => {
    const series = [];

    if (indicators.MA5) {
      series.push({
        name: 'MA5',
        type: 'line',
        data: indicators.MA5,
        smooth: true,
        lineStyle: { width: 1 },
        showSymbol: false
      });
    }

    if (indicators.MA10) {
      series.push({
        name: 'MA10',
        type: 'line',
        data: indicators.MA10,
        smooth: true,
        lineStyle: { width: 1 },
        showSymbol: false
      });
    }

    if (indicators.MA20) {
      series.push({
        name: 'MA20',
        type: 'line',
        data: indicators.MA20,
        smooth: true,
        lineStyle: { width: 1 },
        showSymbol: false
      });
    }

    if (indicators.MA30) {
      series.push({
        name: 'MA30',
        type: 'line',
        data: indicators.MA30,
        smooth: true,
        lineStyle: { width: 1 },
        showSymbol: false
      });
    }

    if (indicators.BOLL_UPPER) {
      series.push(
        {
          name: 'BOLL上轨',
          type: 'line',
          data: indicators.BOLL_UPPER,
          lineStyle: {
            color: '#fac858',
            width: 1
          },
          showSymbol: false
        },
        {
          name: 'BOLL中轨',
          type: 'line',
          data: indicators.BOLL_MIDDLE,
          lineStyle: {
            color: '#ee6666',
            width: 1
          },
          showSymbol: false
        },
        {
          name: 'BOLL下轨',
          type: 'line',
          data: indicators.BOLL_LOWER,
          lineStyle: {
            color: '#73c0de',
            width: 1
          },
          showSymbol: false
        }
      );
    }

    return series;
  };

  // 切换全屏
  const toggleFullscreen = () => {
    setFullscreen(!fullscreen);
  };

  // 下载图表
  const downloadChart = () => {
    if (chartInstance.current) {
      const url = chartInstance.current.getDataURL({
        type: 'png',
        backgroundColor: '#fff'
      });
      const link = document.createElement('a');
      link.download = `${stockCode}_chart.png`;
      link.href = url;
      link.click();
    }
  };

  // 刷新数据
  const refreshData = () => {
    fetchKLineData();
  };

  // 监听股票代码变化
  useEffect(() => {
    if (stockCode) {
      fetchKLineData();
    }
  }, [stockCode, period]);

  // 监听K线数据变化
  useEffect(() => {
    if (klineData.length > 0) {
      initChart();
    }
  }, [klineData, selectedIndicators, showVolume, chartType]);

  // 监听实时价格更新
  useEffect(() => {
    const price = realTimePrices.get(stockCode);
    if (price) {
      setRealTimePrice(price);
    }
  }, [realTimePrices, stockCode]);

  // 监听窗口大小变化
  useEffect(() => {
    const handleResize = () => {
      if (chartInstance.current) {
        chartInstance.current.resize();
      }
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // 组件卸载时销毁图表
  useEffect(() => {
    return () => {
      if (chartInstance.current) {
        chartInstance.current.dispose();
      }
    };
  }, []);

  return (
    <Card
      className={`stock-chart ${fullscreen ? 'fullscreen' : ''} ${className || ''}`}
      title={
        <div className="chart-header">
          <span>股价走势</span>
          {realTimePrice && (
            <div className="real-time-info">
              <span className="price">{formatNumber(realTimePrice.closePrice)}</span>
              <span className={`change ${realTimePrice.changePercent! >= 0 ? 'positive' : 'negative'}`}>
                {realTimePrice.change! >= 0 ? '+' : ''}{formatNumber(realTimePrice.change!)}
                ({formatPercent(realTimePrice.changePercent!)})
              </span>
            </div>
          )}
        </div>
      }
      extra={
        showToolbar && (
          <Space>
            <Radio.Group
              value={period}
              onChange={(e) => setPeriod(e.target.value)}
              size="small"
            >
              {periodOptions.map(option => (
                <Radio.Button key={option.value} value={option.value}>
                  {option.label}
                </Radio.Button>
              ))}
            </Radio.Group>

            {showIndicators && (
              <Select
                mode="multiple"
                placeholder="技术指标"
                value={selectedIndicators}
                onChange={setSelectedIndicators}
                options={indicatorOptions}
                size="small"
                style={{ minWidth: 120 }}
              />
            )}

            <Tooltip title="刷新">
              <Button
                type="text"
                icon={<ReloadOutlined />}
                onClick={refreshData}
                loading={loading}
                size="small"
              />
            </Tooltip>

            <Tooltip title="下载图表">
              <Button
                type="text"
                icon={<DownloadOutlined />}
                onClick={downloadChart}
                size="small"
              />
            </Tooltip>

            <Tooltip title={fullscreen ? '退出全屏' : '全屏显示'}>
              <Button
                type="text"
                icon={fullscreen ? <FullscreenExitOutlined /> : <FullscreenOutlined />}
                onClick={toggleFullscreen}
                size="small"
              />
            </Tooltip>
          </Space>
        )
      }
      bodyStyle={{ padding: 0 }}
    >
      <Spin spinning={loading}>
        <div
          ref={chartRef}
          className="chart-container"
          style={{ height: fullscreen ? '80vh' : height }}
        />
      </Spin>
    </Card>
  );
};

export default StockChart;