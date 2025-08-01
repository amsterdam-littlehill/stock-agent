import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Input,
  Button,
  Select,
  Tag,

  Tabs,
  Space,
  Typography,

  notification,
  Modal,
  Form,
  Switch,

  Badge
} from 'antd';
import {
  SearchOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,

  SettingOutlined,
  DownloadOutlined,
  ShareAltOutlined,
  HeartOutlined,
  HeartFilled,

} from '@ant-design/icons';
import { useWebSocket } from '../hooks/useWebSocket';
import { stockAnalysisApi } from '../services/api';
import { AgentProgressPanel } from './AgentProgressPanel';
import { StockChart } from './StockChart';
import { AnalysisResultPanel } from './AnalysisResultPanel';
import { StockInfoCard } from './StockInfoCard';
import { RealTimePriceCard } from './RealTimePriceCard';

const { Text } = Typography;
const { Option } = Select;
const { TabPane } = Tabs;
const { TextArea } = Input;

interface StockAnalysisRequest {
  stockCode: string;
  depth: 'summary' | 'normal' | 'detailed';
  timeframe: string;
  analysisTypes: string[];
  realTimeData: boolean;
  includeHistoricalComparison: boolean;
  includeIndustryComparison: boolean;
  customParameters?: Record<string, any>;
  remarks?: string;
}

interface AnalysisProgress {
  requestId: string;
  stockCode: string;
  stage: string;
  agentName: string;
  status: string;
  progress: number;
  message: string;
  timestamp: string;
}

interface AnalysisResult {
  requestId: string;
  stockCode: string;
  stockName: string;
  status: string;
  success: boolean;
  analysisResult: string;
  recommendation: string;
  riskLevel: string;
  confidenceScore: number;
  targetPrice: number;
  currentPrice: number;
  keyPoints: string[];
  warnings: string[];
  processingTimeMs: number;
  agentAnalyses: any[];
  technicalAnalysis: any;
  fundamentalAnalysis: any;
}

const StockAnalysisWorkspace: React.FC = () => {
  // 状态管理
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [, setCurrentRequest] = useState<StockAnalysisRequest | null>(null);
  const [analysisProgress, setAnalysisProgress] = useState<AnalysisProgress[]>([]);
  const [analysisResult, setAnalysisResult] = useState<AnalysisResult | null>(null);
  const [stockInfo, setStockInfo] = useState<any>(null);
  const [realTimePrice, setRealTimePrice] = useState<any>(null);
  const [, setSupportedStocks] = useState<any[]>([]);
  const [isFollowing, setIsFollowing] = useState(false);
  const [settingsVisible, setSettingsVisible] = useState(false);
  const [activeTab, setActiveTab] = useState('analysis');

  // WebSocket连接
  const { connected } = useWebSocket('ws://localhost:8080/ws/stock-analysis', {
    onMessage: handleWebSocketMessage,
    onConnect: () => {
      notification.success({
        message: '连接成功',
        description: '实时数据连接已建立'
      });
    },
    onDisconnect: () => {
      notification.warning({
        message: '连接断开',
        description: '实时数据连接已断开，正在尝试重连...'
      });
    }
  });

  // WebSocket消息处理
  function handleWebSocketMessage(message: any) {
    const { type, data } = message;

    switch (type) {
      case 'analysis_progress':
        setAnalysisProgress(prev => {
          const newProgress = [...prev];
          const existingIndex = newProgress.findIndex(
            p => p.requestId === data.requestId && p.agentName === data.agentName
          );

          if (existingIndex >= 0) {
            newProgress[existingIndex] = data;
          } else {
            newProgress.push(data);
          }

          return newProgress;
        });
        break;

      case 'analysis_result':
        setAnalysisResult(data);
        setLoading(false);
        notification.success({
          message: '分析完成',
          description: `股票 ${data.stockCode} 的分析已完成`
        });
        break;

      case 'price_update':
        setRealTimePrice(data);
        break;

      case 'error':
        setLoading(false);
        notification.error({
          message: '分析失败',
          description: data.message || '分析过程中发生错误'
        });
        break;
    }
  }

  // 初始化数据
  useEffect(() => {
    loadSupportedStocks();
  }, []);

  const loadSupportedStocks = async () => {
    try {
      const response = await stockAnalysisApi.getSupportedStocks();
      setSupportedStocks(response.data || []);
    } catch (error) {
      console.error('加载支持股票列表失败:', error);
    }
  };

  // 搜索股票
  const handleStockSearch = async (value: string) => {
    if (!value) return;

    try {
      const [stockInfoResponse, priceResponse] = await Promise.all([
        stockAnalysisApi.getStockInfo(value),
        stockAnalysisApi.getRealTimePrice(value)
      ]);

      setStockInfo(stockInfoResponse.data);
      setRealTimePrice(priceResponse.data);

      // 检查是否已关注
      // TODO: 实现关注状态检查
      setIsFollowing(false);

    } catch {
      notification.error({
        message: '获取股票信息失败',
        description: '请检查股票代码是否正确'
      });
    }
  };

  // 开始分析
  const handleStartAnalysis = async (values: any) => {
    if (!stockInfo) {
      notification.warning({
        message: '请先选择股票',
        description: '请输入股票代码并获取股票信息'
      });
      return;
    }

    const request: StockAnalysisRequest = {
      stockCode: stockInfo.code,
      depth: values.depth || 'normal',
      timeframe: values.timeframe || '1d',
      analysisTypes: values.analysisTypes || ['technical', 'fundamental'],
      realTimeData: values.realTimeData !== false,
      includeHistoricalComparison: values.includeHistoricalComparison || false,
      includeIndustryComparison: values.includeIndustryComparison || false,
      customParameters: values.customParameters,
      remarks: values.remarks
    };

    setCurrentRequest(request);
    setAnalysisProgress([]);
    setAnalysisResult(null);
    setLoading(true);

    try {
      const response = await stockAnalysisApi.analyzeStock(request);

      // 如果是同步响应，直接设置结果
      if (response.data.status === 'COMPLETED') {
        setAnalysisResult(response.data);
        setLoading(false);
      }

    } catch (error) {
      setLoading(false);
      notification.error({
        message: '启动分析失败',
        description: error.message || '请稍后重试'
      });
    }
  };

  // 停止分析
  const handleStopAnalysis = () => {
    setLoading(false);
    setCurrentRequest(null);
    // TODO: 调用停止分析API
  };

  // 关注/取消关注股票
  const handleToggleFollow = async () => {
    if (!stockInfo) return;

    try {
      if (isFollowing) {
        // TODO: 调用取消关注API
        setIsFollowing(false);
        notification.success({ message: '已取消关注' });
      } else {
        // TODO: 调用关注API
        setIsFollowing(true);
        notification.success({ message: '已添加关注' });
      }
    } catch {
      notification.error({ message: '操作失败' });
    }
  };

  // 导出分析结果
  const handleExportResult = () => {
    if (!analysisResult) return;

    const dataStr = JSON.stringify(analysisResult, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `stock_analysis_${analysisResult.stockCode}_${new Date().getTime()}.json`;
    link.click();
    URL.revokeObjectURL(url);
  };

  // 分享分析结果
  const handleShareResult = () => {
    if (!analysisResult) return;

    const shareUrl = `${window.location.origin}/analysis/${analysisResult.requestId}`;
    navigator.clipboard.writeText(shareUrl).then(() => {
      notification.success({
        message: '分享链接已复制',
        description: '链接已复制到剪贴板'
      });
    });
  };

  // 获取推荐标签颜色
  // const getRecommendationColor = (recommendation: string) => {
  //   if (recommendation?.includes('买入')) return 'green';
  //   if (recommendation?.includes('卖出')) return 'red';
  //   if (recommendation?.includes('持有')) return 'blue';
  //   return 'default';
  // };

  // 获取风险等级颜色
  // const getRiskLevelColor = (riskLevel: string) => {
  //   switch (riskLevel) {
  //     case '低': return 'green';
  //     case '中': return 'orange';
  //     case '高': return 'red';
  //     default: return 'default';
  //   }
  // };

  return (
    <div className="stock-analysis-workspace">
      {/* 顶部工具栏 */}
      <Card className="toolbar-card" bodyStyle={{ padding: '16px 24px' }}>
        <Row gutter={16} align="middle">
          <Col flex="auto">
            <Space size="middle">
              <Input.Search
                placeholder="输入股票代码 (如: 000001, AAPL, 00700.HK)"
                style={{ width: 300 }}
                onSearch={handleStockSearch}
                enterButton={<SearchOutlined />}
              />

              {stockInfo && (
                <Space>
                  <Tag color="blue">{stockInfo.code}</Tag>
                  <Text strong>{stockInfo.name}</Text>
                  <Text type="secondary">({stockInfo.exchange})</Text>

                  <Button
                    type="text"
                    icon={isFollowing ? <HeartFilled /> : <HeartOutlined />}
                    onClick={handleToggleFollow}
                    style={{ color: isFollowing ? '#ff4d4f' : undefined }}
                  >
                    {isFollowing ? '已关注' : '关注'}
                  </Button>
                </Space>
              )}
            </Space>
          </Col>

          <Col>
            <Space>
              <Badge status={connected ? 'success' : 'error'} text={connected ? '已连接' : '未连接'} />

              <Button
                icon={<SettingOutlined />}
                onClick={() => setSettingsVisible(true)}
              >
                设置
              </Button>

              {analysisResult && (
                <Space>
                  <Button
                    icon={<DownloadOutlined />}
                    onClick={handleExportResult}
                  >
                    导出
                  </Button>

                  <Button
                    icon={<ShareAltOutlined />}
                    onClick={handleShareResult}
                  >
                    分享
                  </Button>
                </Space>
              )}
            </Space>
          </Col>
        </Row>
      </Card>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        {/* 左侧面板 */}
        <Col xs={24} lg={8}>
          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            {/* 股票信息卡片 */}
            {stockInfo && (
              <StockInfoCard stockInfo={stockInfo} />
            )}

            {/* 实时价格卡片 */}
            {realTimePrice && (
              <RealTimePriceCard priceData={realTimePrice} />
            )}

            {/* 分析配置表单 */}
            <Card title="分析配置" size="small">
              <Form
                form={form}
                layout="vertical"
                onFinish={handleStartAnalysis}
                initialValues={{
                  depth: 'normal',
                  timeframe: '1d',
                  analysisTypes: ['technical', 'fundamental'],
                  realTimeData: true,
                  includeHistoricalComparison: false,
                  includeIndustryComparison: false
                }}
              >
                <Form.Item name="depth" label="分析深度">
                  <Select>
                    <Option value="summary">简要分析</Option>
                    <Option value="normal">常规分析</Option>
                    <Option value="detailed">详细分析</Option>
                  </Select>
                </Form.Item>

                <Form.Item name="timeframe" label="时间周期">
                  <Select>
                    <Option value="1m">1分钟</Option>
                    <Option value="5m">5分钟</Option>
                    <Option value="15m">15分钟</Option>
                    <Option value="30m">30分钟</Option>
                    <Option value="1h">1小时</Option>
                    <Option value="4h">4小时</Option>
                    <Option value="1d">日线</Option>
                    <Option value="1w">周线</Option>
                    <Option value="1M">月线</Option>
                  </Select>
                </Form.Item>

                <Form.Item name="analysisTypes" label="分析类型">
                  <Select mode="multiple" placeholder="选择分析类型">
                    <Option value="technical">技术分析</Option>
                    <Option value="fundamental">基本面分析</Option>
                    <Option value="sentiment">情绪分析</Option>
                    <Option value="risk">风险分析</Option>
                  </Select>
                </Form.Item>

                <Form.Item name="realTimeData" valuePropName="checked">
                  <Switch checkedChildren="实时数据" unCheckedChildren="历史数据" />
                </Form.Item>

                <Form.Item name="includeHistoricalComparison" valuePropName="checked">
                  <Switch checkedChildren="历史对比" unCheckedChildren="不对比" />
                </Form.Item>

                <Form.Item name="includeIndustryComparison" valuePropName="checked">
                  <Switch checkedChildren="行业对比" unCheckedChildren="不对比" />
                </Form.Item>

                <Form.Item name="remarks" label="备注">
                  <TextArea rows={2} placeholder="可选的分析备注..." />
                </Form.Item>

                <Form.Item>
                  <Space style={{ width: '100%' }}>
                    <Button
                      type="primary"
                      htmlType="submit"
                      loading={loading}
                      disabled={!stockInfo}
                      icon={<PlayCircleOutlined />}
                      block
                    >
                      {loading ? '分析中...' : '开始分析'}
                    </Button>

                    {loading && (
                      <Button
                        danger
                        onClick={handleStopAnalysis}
                        icon={<PauseCircleOutlined />}
                      >
                        停止
                      </Button>
                    )}
                  </Space>
                </Form.Item>
              </Form>
            </Card>

            {/* 智能体进度面板 */}
            {(loading || analysisProgress.length > 0) && (
              <AgentProgressPanel
                progress={analysisProgress}
                loading={loading}
              />
            )}
          </Space>
        </Col>

        {/* 右侧主面板 */}
        <Col xs={24} lg={16}>
          <Card className="main-panel">
            <Tabs activeKey={activeTab} onChange={setActiveTab}>
              <TabPane tab="分析结果" key="analysis">
                {analysisResult ? (
                  <AnalysisResultPanel result={analysisResult} />
                ) : (
                  <div style={{
                    textAlign: 'center',
                    padding: '60px 0'
                  }}>
                    <Text type="secondary">请选择股票并开始分析</Text>
                  </div>
                )}
              </TabPane>

              <TabPane tab="K线图表" key="chart">
                {stockInfo ? (
                  <StockChart
                    stockCode={stockInfo.code}
                    timeframe={form.getFieldValue('timeframe') || '1d'}
                  />
                ) : (
                  <div style={{
                    textAlign: 'center',
                    padding: '60px 0'
                  }}>
                    <Text type="secondary">请选择股票查看图表</Text>
                  </div>
                )}
              </TabPane>

              <TabPane tab="历史分析" key="history">
                {/* TODO: 实现历史分析列表 */}
                <div style={{
                  textAlign: 'center',
                  padding: '60px 0'
                }}>
                  <Text type="secondary">历史分析记录</Text>
                </div>
              </TabPane>
            </Tabs>
          </Card>
        </Col>
      </Row>

      {/* 设置弹窗 */}
      <Modal
        title="分析设置"
        visible={settingsVisible}
        onCancel={() => setSettingsVisible(false)}
        footer={null}
        width={600}
      >
        {/* TODO: 实现设置表单 */}
        <div style={{ padding: '20px 0' }}>
          <Text type="secondary">分析参数设置功能开发中...</Text>
        </div>
      </Modal>
    </div>
  );
};

export default StockAnalysisWorkspace;