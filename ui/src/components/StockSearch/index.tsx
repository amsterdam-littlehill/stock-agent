/**
 * 股票搜索组件
 */

import React, { useState, useCallback } from 'react';
import {
  Input,
  AutoComplete,
  Tag,

  Button,
  Tooltip,
  Empty,
  Spin,
  message
} from 'antd';
import {
  SearchOutlined,
  StarOutlined,
  StarFilled,
  HistoryOutlined,
  ClearOutlined
} from '@ant-design/icons';
import { debounce } from 'lodash';
import { stockApi } from '../../services/stockApi';
import { useStockStore } from '../../store';
import { StockInfo } from '../../types/stock';
import './index.css';

interface StockSearchProps {
  onSelect?: (stock: StockInfo) => void;
  placeholder?: string;
  size?: 'small' | 'middle' | 'large';
  showHistory?: boolean;
  showWatchList?: boolean;
  className?: string;
}

interface SearchOption {
  value: string;
  label: React.ReactNode;
  stock: StockInfo;
}

const StockSearch: React.FC<StockSearchProps> = ({
  onSelect,
  placeholder = '请输入股票代码或名称',
  size = 'middle',
  showHistory = true,
  showWatchList = true,
  className
}) => {
  const [searchValue, setSearchValue] = useState('');
  const [options, setOptions] = useState<SearchOption[]>([]);
  const [loading, setLoading] = useState(false);
  const [showDropdown, setShowDropdown] = useState(false);

  const {
    searchHistory,
    watchList,
    addSearchHistory,
    clearSearchHistory,
    addToWatchList,
    removeFromWatchList,
    setSelectedStock
  } = useStockStore();

  // 防抖搜索函数
  const debouncedSearch = useCallback(
    debounce(async (keyword: string) => {
      if (!keyword.trim()) {
        setOptions([]);
        return;
      }

      setLoading(true);
      try {
        const response = await stockApi.searchStocks({
          keyword: keyword.trim(),
          page: 0,
          size: 10
        });

        if (response.success && response.data) {
          const searchOptions: SearchOption[] = response.data.content.map(stock => ({
            value: `${stock.stockCode} ${stock.stockName}`,
            label: (
              <div className="stock-search-option">
                <div className="stock-info">
                  <span className="stock-code">{stock.stockCode}</span>
                  <span className="stock-name">{stock.stockName}</span>
                  <Tag color={stock.market === 'SH' ? 'red' : 'blue'}>
                    {stock.market}
                  </Tag>
                </div>
                <div className="stock-meta">
                  <span className="industry">{stock.industry}</span>
                  {stock.marketCap && (
                    <span className="market-cap">
                      市值: {(stock.marketCap / 100000000).toFixed(2)}亿
                    </span>
                  )}
                </div>
              </div>
            ),
            stock
          }));
          setOptions(searchOptions);
        }
      } catch (error) {
        console.error('搜索股票失败:', error);
        message.error('搜索失败，请重试');
      } finally {
        setLoading(false);
      }
    }, 300),
    []
  );

  // 处理搜索输入变化
  const handleSearch = (value: string) => {
    setSearchValue(value);
    debouncedSearch(value);
  };

  // 处理选择股票
  const handleSelect = (value: string, option: any) => {
    const stock = option.stock as StockInfo;
    setSelectedStock(stock);
    addSearchHistory(value);
    onSelect?.(stock);
    setSearchValue('');
    setOptions([]);
    setShowDropdown(false);
  };

  // 处理关注/取消关注
  const handleWatchToggle = (stockCode: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (watchList.includes(stockCode)) {
      removeFromWatchList(stockCode);
      message.success('已取消关注');
    } else {
      addToWatchList(stockCode);
      message.success('已添加关注');
    }
  };

  // 处理历史记录点击
  const handleHistoryClick = (keyword: string) => {
    setSearchValue(keyword);
    debouncedSearch(keyword);
  };

  // 清除搜索历史
  const handleClearHistory = () => {
    clearSearchHistory();
    message.success('已清除搜索历史');
  };

  // 获取下拉面板内容
  const getDropdownContent = () => {
    if (loading) {
      return (
        <div className="search-loading">
          <Spin size="small" />
          <span>搜索中...</span>
        </div>
      );
    }

    if (searchValue.trim() && options.length === 0) {
      return (
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description="未找到相关股票"
          style={{ padding: '20px' }}
        />
      );
    }

    if (!searchValue.trim()) {
      return (
        <div className="search-suggestions">
          {showHistory && searchHistory.length > 0 && (
            <div className="history-section">
              <div className="section-header">
                <HistoryOutlined />
                <span>搜索历史</span>
                <Button
                  type="text"
                  size="small"
                  icon={<ClearOutlined />}
                  onClick={handleClearHistory}
                >
                  清除
                </Button>
              </div>
              <div className="history-tags">
                {searchHistory.slice(0, 8).map((keyword, index) => (
                  <Tag
                    key={index}
                    className="history-tag"
                    onClick={() => handleHistoryClick(keyword)}
                  >
                    {keyword}
                  </Tag>
                ))}
              </div>
            </div>
          )}

          {showWatchList && watchList.length > 0 && (
            <div className="watchlist-section">
              <div className="section-header">
                <StarFilled style={{ color: '#faad14' }} />
                <span>我的关注</span>
              </div>
              <div className="watchlist-items">
                {watchList.slice(0, 6).map((stockCode) => (
                  <div
                    key={stockCode}
                    className="watchlist-item"
                    onClick={() => handleHistoryClick(stockCode)}
                  >
                    <span>{stockCode}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      );
    }

    return null;
  };

  // 渲染选项
  const renderOption = (option: SearchOption) => {
    const isWatched = watchList.includes(option.stock.stockCode);

    return (
      <div className="stock-search-option-wrapper">
        {option.label}
        <div className="option-actions">
          <Tooltip title={isWatched ? '取消关注' : '添加关注'}>
            <Button
              type="text"
              size="small"
              icon={isWatched ? <StarFilled style={{ color: '#faad14' }} /> : <StarOutlined />}
              onClick={(e) => handleWatchToggle(option.stock.stockCode, e)}
            />
          </Tooltip>
        </div>
      </div>
    );
  };

  return (
    <div className={`stock-search ${className || ''}`}>
      <AutoComplete
        value={searchValue}
        options={options.map(option => ({
          ...option,
          label: renderOption(option)
        }))}
        onSearch={handleSearch}
        onSelect={handleSelect}
        onFocus={() => setShowDropdown(true)}
        onBlur={() => setTimeout(() => setShowDropdown(false), 200)}
        open={showDropdown}
        dropdownRender={(menu) => (
          <div className="stock-search-dropdown">
            {options.length > 0 ? menu : getDropdownContent()}
          </div>
        )}
        size={size}
        style={{ width: '100%' }}
      >
        <Input
          prefix={<SearchOutlined />}
          placeholder={placeholder}
          allowClear
          onClear={() => {
            setSearchValue('');
            setOptions([]);
          }}
        />
      </AutoComplete>
    </div>
  );
};

export default StockSearch;