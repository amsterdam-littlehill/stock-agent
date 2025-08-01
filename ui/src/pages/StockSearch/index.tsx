import React, { useState, useCallback } from 'react';
import { Input, Card, List, Button, Tag, Space, Typography, Row, Col, Spin, Empty, message } from 'antd';
import { SearchOutlined, StarOutlined, StarFilled, RiseOutlined, FallOutlined } from '@ant-design/icons';
import { useStockStore } from '@/store';
import { stockApi } from '@/services/stockApi';
import { formatNumber, formatPercent, getChangeColorClass } from '@/utils/format';
import type { StockInfo, StockPrice } from '@/types/stock';
import './index.css';

const { Search } = Input;
const { Text, Title } = Typography;

const StockSearch: React.FC = () => {
  const [searchValue, setSearchValue] = useState('');
  const [searchResults, setSearchResults] = useState<StockInfo[]>([]);
  const [loading, setLoading] = useState(false);
  const [realtimePrices, setRealtimePrices] = useState<Record<string, StockPrice>>({});

  const {
    searchHistory,
    watchList,
    addToSearchHistory,
    addToWatchList,
    removeFromWatchList,
    setSelectedStock
  } = useStockStore();

  // 搜索股票
  const handleSearch = useCallback(async (value: string) => {
    if (!value.trim()) {
      setSearchResults([]);
      return;
    }

    setLoading(true);
    try {
      const results = await stockApi.searchStocks({
        keyword: value,
        limit: 20
      });
      setSearchResults(results);
      addToSearchHistory(value);

      // 获取搜索结果的实时价格
      const pricePromises = results.map(stock =>
        stockApi.getStockPrice(stock.stockCode).catch(() => null)
      );
      const prices = await Promise.all(pricePromises);

      const priceMap: Record<string, StockPrice> = {};
      prices.forEach((price, index) => {
        if (price) {
          priceMap[results[index].stockCode] = price;
        }
      });
      setRealtimePrices(priceMap);
    } catch (error) {
      message.error('搜索失败，请重试');
      console.error('Search error:', error);
    } finally {
      setLoading(false);
    }
  }, [addToSearchHistory]);

  // 选择股票
  const handleSelectStock = useCallback((stock: StockInfo) => {
    setSelectedStock(stock);
    // 这里可以导航到股票详情页面
    message.success(`已选择股票：${stock.stockName}`);
  }, [setSelectedStock]);

  // 切换关注状态
  const toggleWatchList = useCallback((stock: StockInfo) => {
    const isWatched = watchList.some(item => item.stockCode === stock.stockCode);
    if (isWatched) {
      removeFromWatchList(stock.stockCode);
      message.success('已取消关注');
    } else {
      addToWatchList(stock);
      message.success('已添加到关注列表');
    }
  }, [watchList, addToWatchList, removeFromWatchList]);

  // 渲染股票项
  const renderStockItem = (stock: StockInfo) => {
    const price = realtimePrices[stock.stockCode];
    const isWatched = watchList.some(item => item.stockCode === stock.stockCode);

    return (
      <List.Item
        key={stock.stockCode}
        className="stock-item"
        onClick={() => handleSelectStock(stock)}
      >
        <div className="stock-item-content">
          <Row justify="space-between" align="middle" style={{ width: '100%' }}>
            <Col flex="auto">
              <Space direction="vertical" size={4}>
                <Space>
                  <Text strong className="stock-code">{stock.stockCode}</Text>
                  <Text className="stock-name">{stock.stockName}</Text>
                  <Tag color="blue">{stock.market}</Tag>
                </Space>
                <Space size={16}>
                  <Text type="secondary" className="stock-industry">{stock.industry}</Text>
                  {stock.marketCap && (
                    <Text type="secondary">
                      市值: {formatNumber(stock.marketCap / 100000000, '0,0.00')}亿
                    </Text>
                  )}
                </Space>
              </Space>
            </Col>

            {price && (
              <Col>
                <Space direction="vertical" size={4} align="end">
                  <Text strong className="stock-price">
                    ¥{formatNumber(price.currentPrice)}
                  </Text>
                  <Space>
                    <Text className={getChangeColorClass(price.change)}>
                      {price.change >= 0 ? '+' : ''}{formatNumber(price.change)}
                    </Text>
                    <Text className={getChangeColorClass(price.changePercent)}>
                      {price.changePercent >= 0 ? '+' : ''}{formatPercent(price.changePercent / 100)}
                    </Text>
                    {price.change >= 0 ? (
                      <RiseOutlined className="trend-up" />
                    ) : (
                      <FallOutlined className="trend-down" />
                    )}
                  </Space>
                </Space>
              </Col>
            )}

            <Col>
              <Button
                type="text"
                icon={isWatched ? <StarFilled /> : <StarOutlined />}
                className={isWatched ? 'watch-btn-active' : 'watch-btn'}
                onClick={(e) => {
                  e.stopPropagation();
                  toggleWatchList(stock);
                }}
              />
            </Col>
          </Row>
        </div>
      </List.Item>
    );
  };

  return (
    <div className="stock-search-page">
      <Card className="search-card">
        <Title level={3}>股票搜索</Title>
        <Search
          placeholder="请输入股票代码或名称"
          allowClear
          enterButton={<SearchOutlined />}
          size="large"
          value={searchValue}
          onChange={(e) => setSearchValue(e.target.value)}
          onSearch={handleSearch}
          className="search-input"
        />

        {/* 搜索历史 */}
        {searchHistory.length > 0 && (
          <div className="search-history">
            <Text type="secondary">搜索历史：</Text>
            <Space wrap>
              {searchHistory.slice(0, 10).map((keyword, index) => (
                <Tag
                  key={index}
                  className="history-tag"
                  onClick={() => {
                    setSearchValue(keyword);
                    handleSearch(keyword);
                  }}
                >
                  {keyword}
                </Tag>
              ))}
            </Space>
          </div>
        )}
      </Card>

      {/* 搜索结果 */}
      <Card className="results-card" title="搜索结果">
        <Spin spinning={loading}>
          {searchResults.length > 0 ? (
            <List
              dataSource={searchResults}
              renderItem={renderStockItem}
              className="stock-list"
            />
          ) : (
            !loading && (
              <Empty
                description="暂无搜索结果"
                image={Empty.PRESENTED_IMAGE_SIMPLE}
              />
            )
          )}
        </Spin>
      </Card>

      {/* 关注列表 */}
      {watchList.length > 0 && (
        <Card className="watchlist-card" title="我的关注">
          <List
            dataSource={watchList}
            renderItem={renderStockItem}
            className="stock-list"
          />
        </Card>
      )}
    </div>
  );
};

export default StockSearch;