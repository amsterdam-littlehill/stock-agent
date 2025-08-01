import React, { useEffect, useRef } from 'react';
import { Card, Select, Space, Button } from 'antd';
import { FullscreenOutlined, DownloadOutlined } from '@ant-design/icons';

const { Option } = Select;

export interface StockData {
  timestamp: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

export interface StockChartProps {
  symbol: string;
  data: StockData[];
  title?: string;
  height?: number;
  showVolume?: boolean;
  onTimeframeChange?: (timeframe: string) => void;
}

export const StockChart: React.FC<StockChartProps> = ({
  symbol,
  data,
  title,
  height = 400,
  showVolume = true,
  onTimeframeChange
}) => {
  const chartRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    // 这里应该集成真实的图表库，如 ECharts 或 TradingView
    // 目前只是一个占位符实现
    if (chartRef.current && data.length > 0) {
      // 模拟图表渲染
      chartRef.current.innerHTML = `
        <div style="
          display: flex;
          align-items: center;
          justify-content: center;
          height: ${height}px;
          background: #f5f5f5;
          border: 1px dashed #d9d9d9;
          color: #999;
        ">
          <div style="text-align: center;">
            <div style="font-size: 16px; margin-bottom: 8px;">
              ${symbol} 股价走势图
            </div>
            <div style="font-size: 12px;">
              数据点数: ${data.length}
            </div>
            <div style="font-size: 12px;">
              最新价格: ¥${data[data.length - 1]?.close.toFixed(2) || 'N/A'}
            </div>
          </div>
        </div>
      `;
    }
  }, [data, symbol, height]);

  const handleTimeframeChange = (value: string) => {
    if (onTimeframeChange) {
      onTimeframeChange(value);
    }
  };

  const handleFullscreen = () => {
    // 实现全屏功能
    console.log('Fullscreen chart');
  };

  const handleDownload = () => {
    // 实现图表下载功能
    console.log('Download chart');
  };

  return (
    <Card
      title={title || `${symbol} 股价走势`}
      size="small"
      extra={
        <Space>
          <Select
            defaultValue="1D"
            size="small"
            onChange={handleTimeframeChange}
            style={{ width: 80 }}
          >
            <Option value="1m">1分钟</Option>
            <Option value="5m">5分钟</Option>
            <Option value="15m">15分钟</Option>
            <Option value="1h">1小时</Option>
            <Option value="1D">日线</Option>
            <Option value="1W">周线</Option>
            <Option value="1M">月线</Option>
          </Select>
          <Button
            type="text"
            size="small"
            icon={<FullscreenOutlined />}
            onClick={handleFullscreen}
          />
          <Button
            type="text"
            size="small"
            icon={<DownloadOutlined />}
            onClick={handleDownload}
          />
        </Space>
      }
    >
      <div ref={chartRef} />

      {showVolume && (
        <div style={{ marginTop: 16 }}>
          <div
            style={{
              height: 100,
              background: '#f5f5f5',
              border: '1px dashed #d9d9d9',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#999',
              fontSize: '12px'
            }}
          >
            成交量图表区域
          </div>
        </div>
      )}
    </Card>
  );
};

export default StockChart;
