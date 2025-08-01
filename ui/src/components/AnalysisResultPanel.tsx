import React from 'react';
import { Card, Tabs, Typography, Tag, Space, Divider, Row, Col, Statistic } from 'antd';
import { MinusOutlined } from '@ant-design/icons';

const { Text, Title, Paragraph } = Typography;
const { TabPane } = Tabs;

export interface AnalysisResult {
  id: string;
  agentName: string;
  agentType: string;
  timestamp: string;
  confidence: number;
  recommendation: 'BUY' | 'SELL' | 'HOLD';
  targetPrice?: number;
  currentPrice?: number;
  reasoning: string;
  keyPoints: string[];
  riskFactors?: string[];
  timeHorizon?: string;
  metadata?: Record<string, any>;
}

export interface AnalysisResultPanelProps {
  results: AnalysisResult[];
  title?: string;
}

const getRecommendationIcon = (recommendation: string) => {
  switch (recommendation) {
    case 'BUY':
      return <TrendingUpOutlined style={{ color: '#52c41a' }} />;
    case 'SELL':
      return <TrendingDownOutlined style={{ color: '#ff4d4f' }} />;
    default:
      return <MinusOutlined style={{ color: '#faad14' }} />;
  }
};

const getRecommendationColor = (recommendation: string) => {
  switch (recommendation) {
    case 'BUY':
      return 'success';
    case 'SELL':
      return 'error';
    default:
      return 'warning';
  }
};

export const AnalysisResultPanel: React.FC<AnalysisResultPanelProps> = ({
  results,
  title = '分析结果'
}) => {
  if (!results || results.length === 0) {
    return (
      <Card title={title}>
        <Text type="secondary">暂无分析结果</Text>
      </Card>
    );
  }

  return (
    <Card title={title} size="small">
      <Tabs defaultActiveKey="0" size="small">
        {results.map((result, index) => (
          <TabPane
            tab={
              <Space>
                <Text>{result.agentName}</Text>
                <Tag color={getRecommendationColor(result.recommendation)}>
                  {result.recommendation}
                </Tag>
              </Space>
            }
            key={index.toString()}
          >
            <div>
              <Row gutter={16} style={{ marginBottom: 16 }}>
                <Col span={8}>
                  <Statistic
                    title="置信度"
                    value={result.confidence}
                    precision={1}
                    suffix="%"
                  />
                </Col>
                {result.targetPrice && (
                  <Col span={8}>
                    <Statistic
                      title="目标价格"
                      value={result.targetPrice}
                      precision={2}
                      prefix="¥"
                    />
                  </Col>
                )}
                {result.timeHorizon && (
                  <Col span={8}>
                    <Text type="secondary">
                      时间范围: {result.timeHorizon}
                    </Text>
                  </Col>
                )}
              </Row>

              <Space align="center" style={{ marginBottom: 12 }}>
                {getRecommendationIcon(result.recommendation)}
                <Title level={5} style={{ margin: 0 }}>
                  推荐操作: {result.recommendation}
                </Title>
              </Space>

              <Divider orientation="left" plain>
                分析理由
              </Divider>
              <Paragraph>{result.reasoning}</Paragraph>

              {result.keyPoints && result.keyPoints.length > 0 && (
                <>
                  <Divider orientation="left" plain>
                    关键要点
                  </Divider>
                  <ul>
                    {result.keyPoints.map((point, idx) => (
                      <li key={idx}>
                        <Text>{point}</Text>
                      </li>
                    ))}
                  </ul>
                </>
              )}

              {result.riskFactors && result.riskFactors.length > 0 && (
                <>
                  <Divider orientation="left" plain>
                    风险因素
                  </Divider>
                  <ul>
                    {result.riskFactors.map((risk, idx) => (
                      <li key={idx}>
                        <Text type="warning">{risk}</Text>
                      </li>
                    ))}
                  </ul>
                </>
              )}

              <Divider />
              <Text type="secondary" style={{ fontSize: '12px' }}>
                分析时间: {result.timestamp} | 分析师: {result.agentName} ({result.agentType})
              </Text>
            </div>
          </TabPane>
        ))}
      </Tabs>
    </Card>
  );
};

export default AnalysisResultPanel;
