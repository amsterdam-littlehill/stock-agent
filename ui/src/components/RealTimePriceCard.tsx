import React, { useState, useEffect } from 'react';
import { Card, Statistic, Row, Col, Space, Typography, Badge } from 'antd';
import { ArrowUpOutlined, ArrowDownOutlined, SyncOutlined } from '@ant-design/icons';

const { Text } = Typography;

export interface RealTimePriceData {
  symbol: string;
  name: string;
  price: number;
  change: number;
  changePercent: number;
  volume: number;
  timestamp: string;
  bid?: number;
  ask?: number;
  high: number;
  low: number;
  open: number;
  previousClose: number;
}

export interface RealTimePriceCardProps {
  symbol: string;
  onPriceUpdate?: (data: RealTimePriceData) => void;
  refreshInterval?: number;
}

export const RealTimePriceCard: React.FC<RealTimePriceCardProps> = ({
  symbol,
  onPriceUpdate,
  refreshInterval = 5000
}) => {
  const [priceData, setPriceData] = useState<RealTimePriceData | null>(null);
  const [loading, setLoading] = useState(true);
  const [lastUpdate, setLastUpdate] = useState<Date>(new Date());

  useEffect(() => {
    // 模拟实时价格数据获取
    const fetchPriceData = () => {
      setLoading(true);

      // 模拟API调用
      setTimeout(() => {
        const mockData: RealTimePriceData = {
          symbol,
          name: `${symbol} 股票`,
          price: 100 + Math.random() * 50,
          change: (Math.random() - 0.5) * 10,
          changePercent: (Math.random() - 0.5) * 5,
          volume: Math.floor(Math.random() * 1000000),
          timestamp: new Date().toISOString(),
          bid: 100 + Math.random() * 50 - 0.1,
          ask: 100 + Math.random() * 50 + 0.1,
          high: 100 + Math.random() * 50 + 5,
          low: 100 + Math.random() * 50 - 5,
          open: 100 + Math.random() * 50,
          previousClose: 100 + Math.random() * 50
        };

        setPriceData(mockData);
        setLastUpdate(new Date());
        setLoading(false);

        if (onPriceUpdate) {
          onPriceUpdate(mockData);
        }
      }, 500);
    };

    // 初始加载
    fetchPriceData();

    // 设置定时刷新
    const interval = setInterval(fetchPriceData, refreshInterval);

    return () => clearInterval(interval);
  }, [symbol, refreshInterval, onPriceUpdate]);

  if (!priceData) {
    return (
      <Card title="实时价格" loading={loading} size="small">
        <Text type="secondary">加载中...</Text>
      </Card>
    );
  }

  const isPositive = priceData.change >= 0;
  const changeColor = isPositive ? '#52c41a' : '#ff4d4f';
  const changeIcon = isPositive ? <ArrowUpOutlined /> : <ArrowDownOutlined />;

  const formatNumber = (num: number, precision = 2) => {
    return num.toLocaleString('zh-CN', {
      minimumFractionDigits: precision,
      maximumFractionDigits: precision
    });
  };

  return (
    <Card
      title={
        <Space>
          <Text strong>{priceData.name}</Text>
          <Badge status="processing" text="实时" />
        </Space>
      }
      extra={
        <Space>
          <SyncOutlined spin={loading} />
          <Text type="secondary" style={{ fontSize: '12px' }}>
            {lastUpdate.toLocaleTimeString()}
          </Text>
        </Space>
      }
      size="small"
      loading={loading}
    >
      <Row gutter={16}>
        <Col span={12}>
          <Statistic
            title="当前价格"
            value={priceData.price}
            precision={2}
            prefix="¥"
            valueStyle={{
              fontSize: '20px',
              fontWeight: 'bold'
            }}
          />
        </Col>
        <Col span={12}>
          <Statistic
            title="涨跌"
            value={priceData.changePercent}
            precision={2}
            suffix="%"
            prefix={changeIcon}
            valueStyle={{
              color: changeColor,
              fontSize: '16px'
            }}
          />
          <Text style={{
            color: changeColor,
            fontSize: '12px'
          }}>
            {isPositive ? '+' : ''}{formatNumber(priceData.change)}
          </Text>
        </Col>
      </Row>

      <Row gutter={16} style={{ marginTop: 16 }}>
        <Col span={6}>
          <Text type="secondary">开盘</Text>
          <br />
          <Text strong>{formatNumber(priceData.open)}</Text>
        </Col>
        <Col span={6}>
          <Text type="secondary">最高</Text>
          <br />
          <Text strong style={{ color: '#52c41a' }}>
            {formatNumber(priceData.high)}
          </Text>
        </Col>
        <Col span={6}>
          <Text type="secondary">最低</Text>
          <br />
          <Text strong style={{ color: '#ff4d4f' }}>
            {formatNumber(priceData.low)}
          </Text>
        </Col>
        <Col span={6}>
          <Text type="secondary">昨收</Text>
          <br />
          <Text strong>{formatNumber(priceData.previousClose)}</Text>
        </Col>
      </Row>

      {(priceData.bid || priceData.ask) && (
        <Row gutter={16} style={{ marginTop: 12 }}>
          {priceData.bid && (
            <Col span={12}>
              <Text type="secondary">买一价</Text>
              <br />
              <Text strong style={{ color: '#52c41a' }}>
                {formatNumber(priceData.bid)}
              </Text>
            </Col>
          )}
          {priceData.ask && (
            <Col span={12}>
              <Text type="secondary">卖一价</Text>
              <br />
              <Text strong style={{ color: '#ff4d4f' }}>
                {formatNumber(priceData.ask)}
              </Text>
            </Col>
          )}
        </Row>
      )}

      <Row style={{ marginTop: 12 }}>
        <Col span={24}>
          <Text type="secondary">成交量: </Text>
          <Text strong>
            {priceData.volume.toLocaleString('zh-CN')}
          </Text>
        </Col>
      </Row>
    </Card>
  );
};

export default RealTimePriceCard;
