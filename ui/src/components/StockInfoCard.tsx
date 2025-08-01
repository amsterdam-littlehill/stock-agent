import React from 'react';
import { Card, Row, Col, Statistic, Tag, Space, Typography, Divider } from 'antd';
import { ArrowUpOutlined, ArrowDownOutlined } from '@ant-design/icons';

const { Text, Title } = Typography;

export interface StockInfo {
  symbol: string;
  name: string;
  currentPrice: number;
  change: number;
  changePercent: number;
  volume: number;
  marketCap?: number;
  pe?: number;
  pb?: number;
  eps?: number;
  dividend?: number;
  high52w?: number;
  low52w?: number;
  avgVolume?: number;
  sector?: string;
  industry?: string;
  description?: string;
}

export interface StockInfoCardProps {
  stockInfo: StockInfo;
  loading?: boolean;
}

export const StockInfoCard: React.FC<StockInfoCardProps> = ({
  stockInfo,
  loading = false
}) => {
  const isPositive = stockInfo.change >= 0;
  const changeColor = isPositive ? '#52c41a' : '#ff4d4f';
  const changeIcon = isPositive ? <ArrowUpOutlined /> : <ArrowDownOutlined />;

  const formatNumber = (num: number | undefined, precision = 2) => {
    if (num === undefined || num === null) return 'N/A';
    return num.toLocaleString('zh-CN', {
      minimumFractionDigits: precision,
      maximumFractionDigits: precision
    });
  };

  const formatLargeNumber = (num: number | undefined) => {
    if (num === undefined || num === null) return 'N/A';
    if (num >= 1e12) return `${(num / 1e12).toFixed(2)}万亿`;
    if (num >= 1e8) return `${(num / 1e8).toFixed(2)}亿`;
    if (num >= 1e4) return `${(num / 1e4).toFixed(2)}万`;
    return num.toLocaleString('zh-CN');
  };

  return (
    <Card
      title={
        <Space>
          <Title level={4} style={{ margin: 0 }}>
            {stockInfo.name}
          </Title>
          <Text type="secondary">({stockInfo.symbol})</Text>
          {stockInfo.sector && (
            <Tag color="blue">{stockInfo.sector}</Tag>
          )}
        </Space>
      }
      loading={loading}
      size="small"
    >
      <Row gutter={16}>
        <Col span={12}>
          <Statistic
            title="当前价格"
            value={stockInfo.currentPrice}
            precision={2}
            prefix="¥"
            valueStyle={{
              fontSize: '24px',
              fontWeight: 'bold'
            }}
          />
        </Col>
        <Col span={12}>
          <Statistic
            title="涨跌幅"
            value={stockInfo.changePercent}
            precision={2}
            suffix="%"
            prefix={changeIcon}
            valueStyle={{
              color: changeColor,
              fontSize: '18px'
            }}
          />
          <Text style={{
            color: changeColor,
            fontSize: '14px'
          }}>
            {isPositive ? '+' : ''}{formatNumber(stockInfo.change)}
          </Text>
        </Col>
      </Row>

      <Divider />

      <Row gutter={[16, 8]}>
        <Col span={8}>
          <Statistic
            title="成交量"
            value={formatLargeNumber(stockInfo.volume)}
            valueStyle={{ fontSize: '14px' }}
          />
        </Col>
        {stockInfo.marketCap && (
          <Col span={8}>
            <Statistic
              title="市值"
              value={formatLargeNumber(stockInfo.marketCap)}
              valueStyle={{ fontSize: '14px' }}
            />
          </Col>
        )}
        {stockInfo.pe && (
          <Col span={8}>
            <Statistic
              title="市盈率"
              value={formatNumber(stockInfo.pe)}
              valueStyle={{ fontSize: '14px' }}
            />
          </Col>
        )}
      </Row>

      <Row gutter={[16, 8]} style={{ marginTop: 8 }}>
        {stockInfo.pb && (
          <Col span={8}>
            <Statistic
              title="市净率"
              value={formatNumber(stockInfo.pb)}
              valueStyle={{ fontSize: '14px' }}
            />
          </Col>
        )}
        {stockInfo.eps && (
          <Col span={8}>
            <Statistic
              title="每股收益"
              value={formatNumber(stockInfo.eps)}
              valueStyle={{ fontSize: '14px' }}
            />
          </Col>
        )}
        {stockInfo.dividend && (
          <Col span={8}>
            <Statistic
              title="股息率"
              value={formatNumber(stockInfo.dividend)}
              suffix="%"
              valueStyle={{ fontSize: '14px' }}
            />
          </Col>
        )}
      </Row>

      {(stockInfo.high52w || stockInfo.low52w) && (
        <>
          <Divider />
          <Row gutter={16}>
            {stockInfo.high52w && (
              <Col span={12}>
                <Text type="secondary">52周最高: </Text>
                <Text strong>¥{formatNumber(stockInfo.high52w)}</Text>
              </Col>
            )}
            {stockInfo.low52w && (
              <Col span={12}>
                <Text type="secondary">52周最低: </Text>
                <Text strong>¥{formatNumber(stockInfo.low52w)}</Text>
              </Col>
            )}
          </Row>
        </>
      )}

      {stockInfo.industry && (
        <>
          <Divider />
          <Text type="secondary">行业: </Text>
          <Tag>{stockInfo.industry}</Tag>
        </>
      )}

      {stockInfo.description && (
        <>
          <Divider />
          <Text type="secondary" style={{ fontSize: '12px' }}>
            {stockInfo.description}
          </Text>
        </>
      )}
    </Card>
  );
};

export default StockInfoCard;
