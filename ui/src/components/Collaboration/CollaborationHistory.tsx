/**
 * 协作历史记录组件
 * @author Stock-Agent Team
 * @version 1.0
 */

import React, { useState, useCallback } from 'react';
import {
  Table,
  Card,
  Space,
  Tag,
  Button,
  Input,
  Select,
  DatePicker,
  Drawer,
  Descriptions,
  Timeline,
  Progress,
  Badge,
  Tooltip,
  Modal,
  List,
  Avatar,
  Typography,
  Divider,
  Alert,
  Row,
  Col,
  Statistic,
  Rate,
  message,
  message
} from 'antd';
import {
  EyeOutlined,
  DownloadOutlined,

  ReloadOutlined,
  BarChartOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  StopOutlined,
  TeamOutlined,
  MessageOutlined,
  StarOutlined,
  TrophyOutlined,
  FileTextOutlined,
  ShareAltOutlined
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

import {
  CollaborationHistory as CollaborationHistoryType,
  CollaborationStatus,
  CollaborationMode,
  AgentOpinion,
  CollaborationInteraction,
  CollaborationInteraction
} from '../../types/collaboration';
import { CollaborationService } from '../../services/collaboration';

const { RangePicker } = DatePicker;
const { Option } = Select;
const { Text, Title, Paragraph } = Typography;
const { Search } = Input;

interface CollaborationHistoryProps {
  history: CollaborationHistoryType[];
  onHistoryChange: (history: CollaborationHistoryType[]) => void;
}

/**
 * 协作历史记录组件
 */
const CollaborationHistory: React.FC<CollaborationHistoryProps> = ({
  history,
  onHistoryChange
}) => {
  // 状态管理
  const [loading, setLoading] = useState(false);
  const [selectedHistory, setSelectedHistory] = useState<CollaborationHistoryType | null>(null);
  const [detailDrawerVisible, setDetailDrawerVisible] = useState(false);
  const [analysisModalVisible, setAnalysisModalVisible] = useState(false);
  const [exportModalVisible, setExportModalVisible] = useState(false);

  // 过滤状态
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<CollaborationStatus | 'ALL'>('ALL');
  const [modeFilter, setModeFilter] = useState<CollaborationMode | 'ALL'>('ALL');
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs] | null>(null);
  const [ratingFilter] = useState<number | 'ALL'>('ALL');

  // 分页状态
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);

  // 详细数据
  const [, setHistoryDetails] = useState<any>(null);
  const [historyInteractions, setHistoryInteractions] = useState<CollaborationInteraction[]>([]);
  const [historyOpinions, setHistoryOpinions] = useState<AgentOpinion[]>([]);

  /**
   * 获取历史详情
   */
  const fetchHistoryDetails = useCallback(async (historyId: string) => {
    try {
      setLoading(true);
      const response = await CollaborationService.getCollaborationHistory({
        sessionId: historyId,
        includeDetails: true
      });

      if (response.success && response.data) {
        const details = response.data[0]; // 假设返回单个详情
        setHistoryDetails(details);
        setHistoryInteractions(details.interactions || []);
        setHistoryOpinions(details.opinions || []);
      }
    } catch (error) {
      console.error('Failed to fetch history details:', error);
      message.error('获取历史详情失败');
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * 查看历史详情
   */
  const handleViewDetails = useCallback(async (record: CollaborationHistoryType) => {
    setSelectedHistory(record);
    await fetchHistoryDetails(record.id);
    setDetailDrawerVisible(true);
  }, [fetchHistoryDetails]);

  /**
   * 导出历史数据
   */
  const handleExportHistory = useCallback(async (format: 'excel' | 'csv' | 'json') => {
    try {
      setLoading(true);
      const blob = await CollaborationService.exportCollaborationData({
        type: 'history',
        format,
        filters: {
          status: statusFilter !== 'ALL' ? statusFilter : undefined,
          mode: modeFilter !== 'ALL' ? modeFilter : undefined,
          dateRange: dateRange ? {
            start: dateRange[0].toISOString(),
            end: dateRange[1].toISOString()
          } : undefined
        }
      });

      // 创建下载链接
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `collaboration_history_${dayjs().format('YYYY-MM-DD')}.${format}`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);

      message.success('历史数据导出成功');
      setExportModalVisible(false);
    } catch (error) {
      console.error('Export history failed:', error);
      message.error('历史数据导出失败');
    } finally {
      setLoading(false);
    }
  }, [statusFilter, modeFilter, dateRange]);

  /**
   * 刷新历史数据
   */
  const handleRefresh = useCallback(async () => {
    try {
      setLoading(true);
      const response = await CollaborationService.getCollaborationHistory({
        limit: 100,
        includeDetails: false
      });

      if (response.success && response.data) {
        onHistoryChange(response.data);
        message.success('历史数据已刷新');
      }
    } catch (error) {
      console.error('Refresh history failed:', error);
      message.error('刷新历史数据失败');
    } finally {
      setLoading(false);
    }
  }, [onHistoryChange]);

  // 过滤历史数据
  const filteredHistory = history.filter(item => {
    const matchesSearch = !searchText ||
      item.topic.toLowerCase().includes(searchText.toLowerCase()) ||
      item.id.toLowerCase().includes(searchText.toLowerCase());

    const matchesStatus = statusFilter === 'ALL' || item.status === statusFilter;
    const matchesMode = modeFilter === 'ALL' || item.mode === modeFilter;

    const matchesDate = !dateRange || (
      dayjs(item.createdAt).isAfter(dateRange[0]) &&
      dayjs(item.createdAt).isBefore(dateRange[1])
    );

    const matchesRating = ratingFilter === 'ALL' ||
      (item.result?.rating && item.result.rating >= ratingFilter);

    return matchesSearch && matchesStatus && matchesMode && matchesDate && matchesRating;
  });

  // 统计数据
  const totalSessions = filteredHistory.length;
  const completedSessions = filteredHistory.filter(h => h.status === CollaborationStatus.COMPLETED).length;
  // const failedSessions = filteredHistory.filter(h => h.status === CollaborationStatus.FAILED).length;
  const avgDuration = filteredHistory.reduce((sum, h) => {
    if (h.completedAt && h.startedAt) {
      return sum + (new Date(h.completedAt).getTime() - new Date(h.startedAt).getTime());
    }
    return sum;
  }, 0) / Math.max(completedSessions, 1);

  const avgConfidence = filteredHistory.reduce((sum, h) => {
    return sum + (h.result?.confidence || 0);
  }, 0) / Math.max(totalSessions, 1);

  // 获取状态配置
  const getStatusConfig = (status: CollaborationStatus) => {
    const configs = {
      [CollaborationStatus.COMPLETED]: {
        color: 'success',
        text: '已完成',
        icon: <CheckCircleOutlined />
      },
      [CollaborationStatus.FAILED]: {
        color: 'error',
        text: '失败',
        icon: <ExclamationCircleOutlined />
      },
      [CollaborationStatus.CANCELLED]: {
        color: 'warning',
        text: '已取消',
        icon: <StopOutlined />
      },
      [CollaborationStatus.RUNNING]: {
        color: 'processing',
        text: '运行中',
        icon: <ClockCircleOutlined />
      },
      [CollaborationStatus.PENDING]: {
        color: 'default',
        text: '等待中',
        icon: <ClockCircleOutlined />
      }
    };
    return configs[status] || configs[CollaborationStatus.COMPLETED];
  };

  // 获取协作模式配置
  const getModeConfig = (mode: CollaborationMode) => {
    const configs = {
      [CollaborationMode.STRUCTURED_DEBATE]: {
        color: 'purple',
        text: '结构化辩论',
        icon: '🗣️'
      },
      [CollaborationMode.PARALLEL_ANALYSIS]: {
        color: 'blue',
        text: '并行分析',
        icon: '📊'
      },
      [CollaborationMode.SEQUENTIAL_PIPELINE]: {
        color: 'green',
        text: '顺序流水线',
        icon: '🔄'
      },
      [CollaborationMode.CONSENSUS_BUILDING]: {
        color: 'orange',
        text: '共识构建',
        icon: '🤝'
      }
    };
    return configs[mode] || {
      color: 'default',
      text: mode,
      icon: '❓'
    };
  };

  // 表格列定义
  const columns: ColumnsType<CollaborationHistoryType> = [
    {
      title: '协作信息',
      key: 'info',
      render: (_, record) => (
        <div>
          <div style={{
            fontWeight: 600,
            fontSize: 14
          }}>{record.topic}</div>
          <div style={{
            color: '#666',
            fontSize: 12
          }}>ID: {record.id}</div>
        </div>
      ),
      width: 200,
    },
    {
      title: '模式',
      dataIndex: 'mode',
      key: 'mode',
      render: (mode: CollaborationMode) => {
        const config = getModeConfig(mode);
        return (
          <Tag color={config.color}>
            <span style={{ marginRight: 4 }}>{config.icon}</span>
            {config.text}
          </Tag>
        );
      },
      filters: [
        {
          text: '结构化辩论',
          value: CollaborationMode.STRUCTURED_DEBATE
        },
        {
          text: '并行分析',
          value: CollaborationMode.PARALLEL_ANALYSIS
        },
        {
          text: '顺序流水线',
          value: CollaborationMode.SEQUENTIAL_PIPELINE
        },
        {
          text: '共识构建',
          value: CollaborationMode.CONSENSUS_BUILDING
        }
      ],
      onFilter: (value, record) => record.mode === value,
      width: 120,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: CollaborationStatus) => {
        const config = getStatusConfig(status);
        return (
          <Badge
            status={config.color as any}
            text={
              <Space>
                {config.icon}
                {config.text}
              </Space>
            }
          />
        );
      },
      filters: [
        {
          text: '已完成',
          value: CollaborationStatus.COMPLETED
        },
        {
          text: '失败',
          value: CollaborationStatus.FAILED
        },
        {
          text: '已取消',
          value: CollaborationStatus.CANCELLED
        }
      ],
      onFilter: (value, record) => record.status === value,
      width: 100,
    },
    {
      title: '参与者',
      dataIndex: 'participantIds',
      key: 'participants',
      render: (participantIds: string[]) => (
        <Space>
          <TeamOutlined />
          <span>{participantIds.length}</span>
        </Space>
      ),
      width: 80,
    },
    {
      title: '结果评分',
      key: 'rating',
      render: (_, record) => {
        if (record.result?.rating) {
          return (
            <Space>
              <Rate disabled value={record.result.rating} style={{ fontSize: 12 }} />
              <Text style={{ fontSize: 12 }}>({record.result.rating}/5)</Text>
            </Space>
          );
        }
        return <Text type="secondary">未评分</Text>;
      },
      sorter: (a, b) => (a.result?.rating || 0) - (b.result?.rating || 0),
      width: 120,
    },
    {
      title: '置信度',
      key: 'confidence',
      render: (_, record) => {
        if (record.result?.confidence) {
          const confidence = record.result.confidence * 100;
          return (
            <div>
              <Progress
                percent={confidence}
                size="small"
                status={confidence >= 80 ? 'success' : confidence >= 60 ? 'normal' : 'exception'}
              />
              <Text style={{ fontSize: 12 }}>{confidence.toFixed(1)}%</Text>
            </div>
          );
        }
        return <Text type="secondary">无数据</Text>;
      },
      sorter: (a, b) => (a.result?.confidence || 0) - (b.result?.confidence || 0),
      width: 100,
    },
    {
      title: '持续时间',
      key: 'duration',
      render: (_, record) => {
        if (record.startedAt && record.completedAt) {
          const duration = new Date(record.completedAt).getTime() - new Date(record.startedAt).getTime();
          const minutes = Math.floor(duration / 60000);
          const seconds = Math.floor((duration % 60000) / 1000);
          return `${minutes}分${seconds}秒`;
        }
        return <Text type="secondary">-</Text>;
      },
      sorter: (a, b) => {
        const getDuration = (record: CollaborationHistoryType) => {
          if (record.startedAt && record.completedAt) {
            return new Date(record.completedAt).getTime() - new Date(record.startedAt).getTime();
          }
          return 0;
        };
        return getDuration(a) - getDuration(b);
      },
      width: 100,
    },
    {
      title: '完成时间',
      dataIndex: 'completedAt',
      key: 'completedAt',
      render: (date: string) => date ? new Date(date).toLocaleString() : '-',
      sorter: (a, b) => {
        const dateA = a.completedAt ? new Date(a.completedAt).getTime() : 0;
        const dateB = b.completedAt ? new Date(b.completedAt).getTime() : 0;
        return dateB - dateA;
      },
      width: 150,
    },
    {
      title: '操作',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Tooltip title="查看详情">
            <Button
              type="text"
              icon={<EyeOutlined />}
              onClick={() => handleViewDetails(record)}
            />
          </Tooltip>
          <Tooltip title="分析报告">
            <Button
              type="text"
              icon={<BarChartOutlined />}
              onClick={() => {
                setSelectedHistory(record);
                setAnalysisModalVisible(true);
              }}
            />
          </Tooltip>
          <Tooltip title="分享">
            <Button
              type="text"
              icon={<ShareAltOutlined />}
              onClick={() => {
                // 复制分享链接到剪贴板
                navigator.clipboard.writeText(`${window.location.origin}/collaboration/history/${record.id}`);
                message.success('分享链接已复制到剪贴板');
              }}
            />
          </Tooltip>
        </Space>
      ),
      width: 120,
      fixed: 'right',
    },
  ];

  return (
    <div className="collaboration-history">
      {/* 统计概览 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={6}>
          <Card size="small">
            <Statistic
              title="总会话数"
              value={totalSessions}
              prefix={<MessageOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={6}>
          <Card size="small">
            <Statistic
              title="成功率"
              value={totalSessions > 0 ? ((completedSessions / totalSessions) * 100).toFixed(1) : 0}
              suffix="%"
              prefix={<TrophyOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={6}>
          <Card size="small">
            <Statistic
              title="平均时长"
              value={Math.round(avgDuration / 60000)}
              suffix="分钟"
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={6}>
          <Card size="small">
            <Statistic
              title="平均置信度"
              value={(avgConfidence * 100).toFixed(1)}
              suffix="%"
              prefix={<StarOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 过滤和操作栏 */}
      <Card size="small" style={{ marginBottom: 16 }}>
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} sm={8} md={6}>
            <Search
              placeholder="搜索主题或ID"
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              allowClear
            />
          </Col>
          <Col xs={12} sm={6} md={4}>
            <Select
              value={statusFilter}
              onChange={setStatusFilter}
              style={{ width: '100%' }}
              placeholder="状态"
            >
              <Option value="ALL">全部状态</Option>
              <Option value={CollaborationStatus.COMPLETED}>已完成</Option>
              <Option value={CollaborationStatus.FAILED}>失败</Option>
              <Option value={CollaborationStatus.CANCELLED}>已取消</Option>
            </Select>
          </Col>
          <Col xs={12} sm={6} md={4}>
            <Select
              value={modeFilter}
              onChange={setModeFilter}
              style={{ width: '100%' }}
              placeholder="模式"
            >
              <Option value="ALL">全部模式</Option>
              <Option value={CollaborationMode.STRUCTURED_DEBATE}>结构化辩论</Option>
              <Option value={CollaborationMode.PARALLEL_ANALYSIS}>并行分析</Option>
              <Option value={CollaborationMode.SEQUENTIAL_PIPELINE}>顺序流水线</Option>
              <Option value={CollaborationMode.CONSENSUS_BUILDING}>共识构建</Option>
            </Select>
          </Col>
          <Col xs={24} sm={8} md={6}>
            <RangePicker
              value={dateRange}
              onChange={setDateRange}
              style={{ width: '100%' }}
              placeholder={['开始日期', '结束日期']}
            />
          </Col>
          <Col xs={24} sm={12} md={4}>
            <Space>
              <Button
                icon={<ReloadOutlined />}
                onClick={handleRefresh}
                loading={loading}
              >
                刷新
              </Button>
              <Button
                icon={<DownloadOutlined />}
                onClick={() => setExportModalVisible(true)}
              >
                导出
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 历史记录表格 */}
      <Card>
        <Table
          columns={columns}
          dataSource={filteredHistory}
          rowKey="id"
          loading={loading}
          pagination={{
            current: currentPage,
            pageSize: pageSize,
            total: filteredHistory.length,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) =>
              `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
            onChange: (page, size) => {
              setCurrentPage(page);
              setPageSize(size || 10);
            }
          }}
          scroll={{ x: 1200 }}
          className="collaboration-table"
        />
      </Card>

      {/* 详情抽屉 */}
      <Drawer
        title="协作历史详情"
        placement="right"
        onClose={() => setDetailDrawerVisible(false)}
        open={detailDrawerVisible}
        width={800}
      >
        {selectedHistory && (
          <div>
            <Descriptions title="基本信息" bordered column={1}>
              <Descriptions.Item label="会话ID">{selectedHistory.id}</Descriptions.Item>
              <Descriptions.Item label="主题">{selectedHistory.topic}</Descriptions.Item>
              <Descriptions.Item label="协作模式">
                {(() => {
                  const config = getModeConfig(selectedHistory.mode);
                  return (
                    <Tag color={config.color}>
                      <span style={{ marginRight: 4 }}>{config.icon}</span>
                      {config.text}
                    </Tag>
                  );
                })()}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                {(() => {
                  const config = getStatusConfig(selectedHistory.status);
                  return (
                    <Badge
                      status={config.color as any}
                      text={
                        <Space>
                          {config.icon}
                          {config.text}
                        </Space>
                      }
                    />
                  );
                })()}
              </Descriptions.Item>
              <Descriptions.Item label="描述">{selectedHistory.description}</Descriptions.Item>
              <Descriptions.Item label="创建时间">
                {new Date(selectedHistory.createdAt).toLocaleString()}
              </Descriptions.Item>
              <Descriptions.Item label="开始时间">
                {selectedHistory.startedAt ?
                  new Date(selectedHistory.startedAt).toLocaleString() :
                  '未开始'
                }
              </Descriptions.Item>
              <Descriptions.Item label="完成时间">
                {selectedHistory.completedAt ?
                  new Date(selectedHistory.completedAt).toLocaleString() :
                  '未完成'
                }
              </Descriptions.Item>
            </Descriptions>

            {selectedHistory.result && (
              <>
                <Divider />
                <Title level={4}>协作结果</Title>
                <Card size="small">
                  <Descriptions column={1}>
                    <Descriptions.Item label="置信度">
                      <Progress
                        percent={Math.round(selectedHistory.result.confidence * 100)}
                        size="small"
                      />
                    </Descriptions.Item>
                    <Descriptions.Item label="评分">
                      {selectedHistory.result.rating ? (
                        <Rate disabled value={selectedHistory.result.rating} />
                      ) : (
                        <Text type="secondary">未评分</Text>
                      )}
                    </Descriptions.Item>
                    <Descriptions.Item label="结论">
                      <Paragraph>{selectedHistory.result.conclusion}</Paragraph>
                    </Descriptions.Item>
                    <Descriptions.Item label="建议">
                      <ul>
                        {selectedHistory.result.recommendations?.map((rec: string, index: number) => (
                          <li key={index}>{rec}</li>
                        ))}
                      </ul>
                    </Descriptions.Item>
                  </Descriptions>
                </Card>
              </>
            )}

            {historyInteractions.length > 0 && (
              <>
                <Divider />
                <Title level={4}>交互记录</Title>
                <Timeline>
                  {historyInteractions.map((interaction, index) => (
                    <Timeline.Item
                      key={index}
                      color={interaction.type === 'OPINION' ? 'blue' :
                        interaction.type === 'QUESTION' ? 'orange' : 'green'}
                    >
                      <div>
                        <Text strong>{interaction.agentId}</Text>
                        <Text type="secondary" style={{ marginLeft: 8 }}>
                          {new Date(interaction.timestamp).toLocaleString()}
                        </Text>
                      </div>
                      <div style={{ marginTop: 4 }}>
                        {interaction.content}
                      </div>
                      {interaction.confidence && (
                        <div style={{ marginTop: 4 }}>
                          <Text type="secondary">置信度: </Text>
                          <Progress
                            percent={Math.round(interaction.confidence * 100)}
                            size="small"
                            style={{
                              width: 100,
                              display: 'inline-block'
                            }}
                          />
                        </div>
                      )}
                    </Timeline.Item>
                  ))}
                </Timeline>
              </>
            )}

            {historyOpinions.length > 0 && (
              <>
                <Divider />
                <Title level={4}>智能体观点</Title>
                <List
                  dataSource={historyOpinions}
                  renderItem={(opinion) => (
                    <List.Item>
                      <List.Item.Meta
                        avatar={<Avatar icon={<TeamOutlined />} />}
                        title={opinion.agentId}
                        description={
                          <div>
                            <Paragraph>{opinion.content}</Paragraph>
                            <Space>
                              <Text type="secondary">置信度:</Text>
                              <Progress
                                percent={Math.round(opinion.confidence * 100)}
                                size="small"
                                style={{ width: 100 }}
                              />
                              <Text type="secondary">
                                {new Date(opinion.timestamp).toLocaleString()}
                              </Text>
                            </Space>
                          </div>
                        }
                      />
                    </List.Item>
                  )}
                />
              </>
            )}
          </div>
        )}
      </Drawer>

      {/* 分析报告模态框 */}
      <Modal
        title="协作分析报告"
        open={analysisModalVisible}
        onCancel={() => setAnalysisModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setAnalysisModalVisible(false)}>
            关闭
          </Button>,
          <Button key="export" type="primary" icon={<FileTextOutlined />}>
            导出报告
          </Button>
        ]}
        width={800}
      >
        {selectedHistory && (
          <div>
            <Alert
              message="协作分析报告"
              description={`基于协作会话 "${selectedHistory.topic}" 的详细分析`}
              type="info"
              showIcon
              style={{ marginBottom: 16 }}
            />

            <Row gutter={[16, 16]}>
              <Col span={12}>
                <Card size="small" title="性能指标">
                  <Statistic
                    title="协作效率"
                    value={85}
                    suffix="%"
                    valueStyle={{ color: '#52c41a' }}
                  />
                  <Progress percent={85} size="small" style={{ marginTop: 8 }} />
                </Card>
              </Col>
              <Col span={12}>
                <Card size="small" title="质量评估">
                  <Statistic
                    title="结果质量"
                    value={selectedHistory.result?.confidence ?
                      Math.round(selectedHistory.result.confidence * 100) : 0}
                    suffix="%"
                    valueStyle={{ color: '#1890ff' }}
                  />
                  <Progress
                    percent={selectedHistory.result?.confidence ?
                      Math.round(selectedHistory.result.confidence * 100) : 0}
                    size="small"
                    style={{ marginTop: 8 }}
                  />
                </Card>
              </Col>
            </Row>

            <Divider />

            <Title level={5}>关键发现</Title>
            <ul>
              <li>智能体协作效率较高，达到预期目标</li>
              <li>结果置信度符合质量标准</li>
              <li>协作过程中无重大异常或冲突</li>
              <li>建议在类似场景中复用此协作模式</li>
            </ul>

            <Title level={5}>改进建议</Title>
            <ul>
              <li>可以考虑增加更多专业领域的智能体参与</li>
              <li>优化协作流程以提高效率</li>
              <li>加强结果验证机制</li>
            </ul>
          </div>
        )}
      </Modal>

      {/* 导出模态框 */}
      <Modal
        title="导出历史数据"
        open={exportModalVisible}
        onCancel={() => setExportModalVisible(false)}
        footer={null}
        width={500}
      >
        <div>
          <Text>选择导出格式：</Text>
          <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
            <Col span={8}>
              <Button
                block
                onClick={() => handleExportHistory('excel')}
                loading={loading}
                icon={<FileTextOutlined />}
              >
                Excel
              </Button>
            </Col>
            <Col span={8}>
              <Button
                block
                onClick={() => handleExportHistory('csv')}
                loading={loading}
                icon={<FileTextOutlined />}
              >
                CSV
              </Button>
            </Col>
            <Col span={8}>
              <Button
                block
                onClick={() => handleExportHistory('json')}
                loading={loading}
                icon={<FileTextOutlined />}
              >
                JSON
              </Button>
            </Col>
          </Row>

          <Divider />

          <Text type="secondary">
            导出将包含当前过滤条件下的所有历史记录，包括详细的交互数据和结果分析。
          </Text>
        </div>
      </Modal>
    </div>
  );
};

export default CollaborationHistory;