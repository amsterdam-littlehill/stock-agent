import React, { useState, useEffect, useCallback } from 'react';
import { Card, Row, Col, Button, Tag, Progress, Spin, Empty, message, Tabs, Space, Typography } from 'antd';
import { PlayCircleOutlined, ReloadOutlined, ShareAltOutlined } from '@ant-design/icons';
import { useStockStore } from '@/store';
import { analysisApi, agentApi } from '@/services/stockApi';
import { formatDateTime, formatPercent, formatRecommendation, formatRiskLevel } from '@/utils/format';
import type { AnalysisTask, AnalysisResult, Agent } from '@/types/stock';
import StockChart from '@/components/StockChart';
import './index.css';

const { TabPane } = Tabs;
const { Title, Text, Paragraph } = Typography;

const StockAnalysis: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [analysisResults, setAnalysisResults] = useState<AnalysisResult[]>([]);
  const [currentTask, setCurrentTask] = useState<AnalysisTask | null>(null);
  const [availableAgents, setAvailableAgents] = useState<Agent[]>([]);
  const [activeTab, setActiveTab] = useState('technical');

  const { selectedStock } = useStockStore();

  // 获取可用智能体
  useEffect(() => {
    const fetchAgents = async () => {
      try {
        const agents = await agentApi.getAvailableAgents();
        setAvailableAgents(agents);
      } catch (error) {
        console.error('Failed to fetch agents:', error);
      }
    };
    fetchAgents();
  }, []);

  // 获取分析结果
  const fetchAnalysisResults = useCallback(async () => {
    if (!selectedStock) return;

    try {
      const results = await analysisApi.getStockAnalysisResults(selectedStock.stockCode);
      setAnalysisResults(results);
    } catch (error) {
      console.error('Failed to fetch analysis results:', error);
    }
  }, [selectedStock]);

  useEffect(() => {
    fetchAnalysisResults();
  }, [fetchAnalysisResults]);

  // 开始分析
  const handleStartAnalysis = useCallback(async (analysisType: string) => {
    if (!selectedStock) {
      message.warning('请先选择股票');
      return;
    }

    setLoading(true);
    try {
      const task = await analysisApi.createAnalysisTask({
        stockCode: selectedStock.stockCode,
        analysisType,
        priority: 'NORMAL'
      });
      setCurrentTask(task);
      message.success('分析任务已创建，正在执行...');

      // 轮询任务状态
      const pollTask = async () => {
        try {
          const updatedTask = await analysisApi.getAnalysisTask(task.id);
          setCurrentTask(updatedTask);

          if (updatedTask.status === 'COMPLETED') {
            message.success('分析完成');
            fetchAnalysisResults();
            setLoading(false);
          } else if (updatedTask.status === 'FAILED') {
            message.error('分析失败');
            setLoading(false);
          } else {
            setTimeout(pollTask, 2000);
          }
        } catch (error) {
          console.error('Failed to poll task:', error);
          setLoading(false);
        }
      };

      setTimeout(pollTask, 2000);
    } catch {
      message.error('创建分析任务失败');
      setLoading(false);
    }
  }, [selectedStock, fetchAnalysisResults]);

  // 渲染分析结果卡片
  const renderAnalysisResult = (result: AnalysisResult) => {
    const getRecommendationColor = (recommendation: string) => {
      switch (recommendation) {
        case 'BUY':
        case 'STRONG_BUY':
          return 'success';
        case 'SELL':
        case 'STRONG_SELL':
          return 'error';
        case 'HOLD':
          return 'warning';
        default:
          return 'default';
      }
    };

    const getRiskLevelColor = (riskLevel: string) => {
      switch (riskLevel) {
        case 'LOW':
          return 'success';
        case 'MEDIUM':
          return 'warning';
        case 'HIGH':
          return 'error';
        default:
          return 'default';
      }
    };

    return (
      <Card
        key={result.id}
        className="analysis-result-card"
        title={
          <Space>
            <Text strong>{result.agentType}</Text>
            <Tag color={getRecommendationColor(result.recommendation)}>
              {formatRecommendation(result.recommendation)}
            </Tag>
          </Space>
        }
        extra={
          <Space>
            <Text type="secondary">{formatDateTime(result.createdAt)}</Text>
            <Button type="text" icon={<ShareAltOutlined />} />
          </Space>
        }
      >
        <Row gutter={[16, 16]}>
          <Col span={24}>
            <Space direction="vertical" style={{ width: '100%' }}>
              <div>
                <Text strong>置信度：</Text>
                <Progress
                  percent={Math.round((result.confidence || 0) * 100)}
                  size="small"
                  status={result.confidence && result.confidence > 0.8 ? 'success' : 'normal'}
                  format={(percent) => `${percent}%`}
                />
              </div>

              <Row gutter={16}>
                <Col span={8}>
                  <Text type="secondary">风险等级：</Text>
                  <Tag color={getRiskLevelColor(result.riskLevel)}>
                    {formatRiskLevel(result.riskLevel)}
                  </Tag>
                </Col>
                {result.targetPrice && (
                  <Col span={8}>
                    <Text type="secondary">目标价：</Text>
                    <Text strong>¥{result.targetPrice}</Text>
                  </Col>
                )}
                {result.upside && (
                  <Col span={8}>
                    <Text type="secondary">上涨空间：</Text>
                    <Text strong className={result.upside > 0 ? 'text-red' : 'text-green'}>
                      {formatPercent(result.upside / 100)}
                    </Text>
                  </Col>
                )}
              </Row>

              {result.summary && (
                <div>
                  <Text strong>分析摘要：</Text>
                  <Paragraph className="analysis-summary">
                    {result.summary}
                  </Paragraph>
                </div>
              )}

              {result.reasoning && (
                <div>
                  <Text strong>分析依据：</Text>
                  <Paragraph className="analysis-reasoning">
                    {result.reasoning}
                  </Paragraph>
                </div>
              )}
            </Space>
          </Col>
        </Row>
      </Card>
    );
  };

  // 按分析类型分组结果
  const groupedResults = analysisResults.reduce((acc, result) => {
    const type = result.analysisType || 'OTHER';
    if (!acc[type]) {
      acc[type] = [];
    }
    acc[type].push(result);
    return acc;
  }, {} as Record<string, AnalysisResult[]>);

  if (!selectedStock) {
    return (
      <div className="stock-analysis-page">
        <Card>
          <Empty
            description="请先选择要分析的股票"
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          />
        </Card>
      </div>
    );
  }

  return (
    <div className="stock-analysis-page">
      {/* 股票信息头部 */}
      <Card className="stock-header-card">
        <Row justify="space-between" align="middle">
          <Col>
            <Space direction="vertical" size={4}>
              <Title level={3} style={{ margin: 0 }}>
                {selectedStock.stockName} ({selectedStock.stockCode})
              </Title>
              <Space>
                <Tag color="blue">{selectedStock.market}</Tag>
                <Text type="secondary">{selectedStock.industry}</Text>
              </Space>
            </Space>
          </Col>
          <Col>
            <Space>
              <Button
                type="primary"
                icon={<PlayCircleOutlined />}
                loading={loading}
                onClick={() => handleStartAnalysis('COMPREHENSIVE')}
              >
                开始综合分析
              </Button>
              <Button
                icon={<ReloadOutlined />}
                onClick={fetchAnalysisResults}
              >
                刷新结果
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 股票图表 */}
      <StockChart stockCode={selectedStock.stockCode} />

      {/* 当前任务状态 */}
      {currentTask && currentTask.status !== 'COMPLETED' && (
        <Card className="task-status-card">
          <Row align="middle" gutter={16}>
            <Col>
              <Spin size="small" />
            </Col>
            <Col flex="auto">
              <Text>正在执行{currentTask.analysisType}分析...</Text>
            </Col>
            <Col>
              <Progress
                percent={currentTask.progress || 0}
                size="small"
                style={{ width: 200 }}
              />
            </Col>
          </Row>
        </Card>
      )}

      {/* 快速分析按钮 */}
      <Card className="quick-analysis-card" title="快速分析">
        <Space wrap>
          <Button
            onClick={() => handleStartAnalysis('TECHNICAL')}
            loading={loading}
          >
            技术分析
          </Button>
          <Button
            onClick={() => handleStartAnalysis('FUNDAMENTAL')}
            loading={loading}
          >
            基本面分析
          </Button>
          <Button
            onClick={() => handleStartAnalysis('SENTIMENT')}
            loading={loading}
          >
            情绪分析
          </Button>
          <Button
            onClick={() => handleStartAnalysis('RISK')}
            loading={loading}
          >
            风险分析
          </Button>
        </Space>
      </Card>

      {/* 分析结果 */}
      <Card className="analysis-results-card" title="分析结果">
        {Object.keys(groupedResults).length > 0 ? (
          <Tabs activeKey={activeTab} onChange={setActiveTab}>
            {Object.entries(groupedResults).map(([type, results]) => (
              <TabPane
                tab={`${type} (${results.length})`}
                key={type}
              >
                <div className="analysis-results-list">
                  {results.map(renderAnalysisResult)}
                </div>
              </TabPane>
            ))}
          </Tabs>
        ) : (
          <Empty
            description="暂无分析结果"
            image={Empty.PRESENTED_IMAGE_SIMPLE}
          />
        )}
      </Card>

      {/* 可用智能体 */}
      <Card className="agents-card" title="可用智能体">
        <Row gutter={[16, 16]}>
          {availableAgents.map(agent => (
            <Col key={agent.id} xs={24} sm={12} md={8} lg={6}>
              <Card
                size="small"
                className="agent-card"
                title={agent.name}
                extra={
                  <Tag color={agent.status === 'ACTIVE' ? 'success' : 'default'}>
                    {agent.status}
                  </Tag>
                }
              >
                <Space direction="vertical" size={4} style={{ width: '100%' }}>
                  <Text type="secondary">{agent.type}</Text>
                  <Text>性能评分: {agent.performanceScore || 0}/100</Text>
                  <Text>成功率: {formatPercent((agent.successRate || 0) / 100)}</Text>
                </Space>
              </Card>
            </Col>
          ))}
        </Row>
      </Card>
    </div>
  );
};

export default StockAnalysis;