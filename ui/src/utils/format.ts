/**
 * 格式化工具函数
 */

import numeral from 'numeral';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/zh-cn';

// 配置dayjs
dayjs.extend(relativeTime);
dayjs.locale('zh-cn');

/**
 * 格式化数字
 * @param value 数值
 * @param format 格式化模式
 * @returns 格式化后的字符串
 */
export const formatNumber = (value: number | null | undefined, format: string = '0,0.00'): string => {
  if (value === null || value === undefined || isNaN(value)) {
    return '--';
  }
  return numeral(value).format(format);
};

/**
 * 格式化百分比
 * @param value 数值
 * @param decimals 小数位数
 * @returns 格式化后的百分比字符串
 */
export const formatPercent = (value: number | null | undefined, decimals: number = 2): string => {
  if (value === null || value === undefined || isNaN(value)) {
    return '--';
  }
  return `${(value * 100).toFixed(decimals)}%`;
};

/**
 * 格式化货币
 * @param value 数值
 * @param currency 货币符号
 * @param decimals 小数位数
 * @returns 格式化后的货币字符串
 */
export const formatCurrency = (value: number | null | undefined, currency: string = '¥', decimals: number = 2): string => {
  if (value === null || value === undefined || isNaN(value)) {
    return '--';
  }
  return `${currency}${numeral(value).format(`0,0.${'0'.repeat(decimals)}`)}`;
};

/**
 * 格式化大数字（万、亿）
 * @param value 数值
 * @param decimals 小数位数
 * @returns 格式化后的字符串
 */
export const formatLargeNumber = (value: number | null | undefined, decimals: number = 2): string => {
  if (value === null || value === undefined || isNaN(value)) {
    return '--';
  }

  const absValue = Math.abs(value);
  const sign = value < 0 ? '-' : '';

  if (absValue >= 100000000) {
    // 亿
    return `${sign}${(absValue / 100000000).toFixed(decimals)}亿`;
  } else if (absValue >= 10000) {
    // 万
    return `${sign}${(absValue / 10000).toFixed(decimals)}万`;
  } else {
    return `${sign}${absValue.toFixed(decimals)}`;
  }
};

/**
 * 格式化市值
 * @param value 市值（元）
 * @param decimals 小数位数
 * @returns 格式化后的市值字符串
 */
export const formatMarketCap = (value: number | null | undefined, decimals: number = 2): string => {
  if (value === null || value === undefined || isNaN(value)) {
    return '--';
  }

  if (value >= 100000000) {
    return `${(value / 100000000).toFixed(decimals)}亿`;
  } else if (value >= 10000) {
    return `${(value / 10000).toFixed(decimals)}万`;
  } else {
    return value.toFixed(decimals);
  }
};

/**
 * 格式化成交量
 * @param value 成交量
 * @param decimals 小数位数
 * @returns 格式化后的成交量字符串
 */
export const formatVolume = (value: number | null | undefined, decimals: number = 0): string => {
  if (value === null || value === undefined || isNaN(value)) {
    return '--';
  }

  if (value >= 100000000) {
    return `${(value / 100000000).toFixed(decimals)}亿`;
  } else if (value >= 10000) {
    return `${(value / 10000).toFixed(decimals)}万`;
  } else {
    return value.toFixed(decimals);
  }
};

/**
 * 格式化成交额
 * @param value 成交额（元）
 * @param decimals 小数位数
 * @returns 格式化后的成交额字符串
 */
export const formatTurnover = (value: number | null | undefined, decimals: number = 2): string => {
  if (value === null || value === undefined || isNaN(value)) {
    return '--';
  }

  if (value >= 100000000) {
    return `${(value / 100000000).toFixed(decimals)}亿元`;
  } else if (value >= 10000) {
    return `${(value / 10000).toFixed(decimals)}万元`;
  } else {
    return `${value.toFixed(decimals)}元`;
  }
};

/**
 * 格式化日期时间
 * @param date 日期
 * @param format 格式化模式
 * @returns 格式化后的日期字符串
 */
export const formatDateTime = (date: string | Date | null | undefined, format: string = 'YYYY-MM-DD HH:mm:ss'): string => {
  if (!date) {
    return '--';
  }
  return dayjs(date).format(format);
};

/**
 * 格式化日期
 * @param date 日期
 * @param format 格式化模式
 * @returns 格式化后的日期字符串
 */
export const formatDate = (date: string | Date | null | undefined, format: string = 'YYYY-MM-DD'): string => {
  if (!date) {
    return '--';
  }
  return dayjs(date).format(format);
};

/**
 * 格式化时间
 * @param date 日期
 * @param format 格式化模式
 * @returns 格式化后的时间字符串
 */
export const formatTime = (date: string | Date | null | undefined, format: string = 'HH:mm:ss'): string => {
  if (!date) {
    return '--';
  }
  return dayjs(date).format(format);
};

/**
 * 格式化相对时间
 * @param date 日期
 * @returns 相对时间字符串
 */
export const formatRelativeTime = (date: string | Date | null | undefined): string => {
  if (!date) {
    return '--';
  }
  return dayjs(date).fromNow();
};

/**
 * 格式化持续时间
 * @param seconds 秒数
 * @returns 格式化后的持续时间字符串
 */
export const formatDuration = (seconds: number | null | undefined): string => {
  if (seconds === null || seconds === undefined || isNaN(seconds)) {
    return '--';
  }

  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const remainingSeconds = seconds % 60;

  if (hours > 0) {
    return `${hours}小时${minutes}分钟${remainingSeconds}秒`;
  } else if (minutes > 0) {
    return `${minutes}分钟${remainingSeconds}秒`;
  } else {
    return `${remainingSeconds}秒`;
  }
};

/**
 * 格式化文件大小
 * @param bytes 字节数
 * @param decimals 小数位数
 * @returns 格式化后的文件大小字符串
 */
export const formatFileSize = (bytes: number | null | undefined, decimals: number = 2): string => {
  if (bytes === null || bytes === undefined || isNaN(bytes)) {
    return '--';
  }

  if (bytes === 0) return '0 Bytes';

  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(decimals))} ${sizes[i]}`;
};

/**
 * 格式化股票代码
 * @param stockCode 股票代码
 * @returns 格式化后的股票代码
 */
export const formatStockCode = (stockCode: string | null | undefined): string => {
  if (!stockCode) {
    return '--';
  }

  // 如果已经包含市场前缀，直接返回
  if (stockCode.includes('.')) {
    return stockCode.toUpperCase();
  }

  // 根据股票代码判断市场
  if (stockCode.startsWith('6')) {
    return `${stockCode}.SH`;
  } else if (stockCode.startsWith('0') || stockCode.startsWith('3')) {
    return `${stockCode}.SZ`;
  } else if (stockCode.startsWith('8') || stockCode.startsWith('4')) {
    return `${stockCode}.BJ`;
  }

  return stockCode.toUpperCase();
};

/**
 * 格式化涨跌幅颜色类名
 * @param value 涨跌幅值
 * @returns CSS类名
 */
export const getChangeColorClass = (value: number | null | undefined): string => {
  if (value === null || value === undefined || isNaN(value)) {
    return '';
  }

  if (value > 0) {
    return 'text-red';
  } else if (value < 0) {
    return 'text-green';
  } else {
    return 'text-gray';
  }
};

/**
 * 获取涨跌颜色值
 * @param value 数值
 * @returns 颜色值
 */
export const getChangeColor = (value: number | null | undefined): string => {
  if (value === null || value === undefined || isNaN(value)) {
    return '#666666';
  }

  if (value > 0) {
    return '#ff4d4f'; // 红色
  } else if (value < 0) {
    return '#52c41a'; // 绿色
  } else {
    return '#666666'; // 灰色
  }
};

/**
 * 格式化涨跌幅显示
 * @param change 涨跌额
 * @param changePercent 涨跌幅
 * @returns 格式化后的涨跌幅字符串
 */
export const formatChange = (change: number | null | undefined, changePercent: number | null | undefined): string => {
  if ((change === null || change === undefined || isNaN(change)) &&
      (changePercent === null || changePercent === undefined || isNaN(changePercent))) {
    return '--';
  }

  const changeStr = change !== null && change !== undefined && !isNaN(change)
    ? `${change >= 0 ? '+' : ''}${formatNumber(change)}`
    : '--';

  const percentStr = changePercent !== null && changePercent !== undefined && !isNaN(changePercent)
    ? `${changePercent >= 0 ? '+' : ''}${formatPercent(changePercent)}`
    : '--';

  return `${changeStr} (${percentStr})`;
};

/**
 * 格式化投资建议
 * @param recommendation 投资建议
 * @returns 格式化后的投资建议
 */
export const formatRecommendation = (recommendation: string | null | undefined): string => {
  if (!recommendation) {
    return '--';
  }

  const recommendationMap: Record<string, string> = {
    'BUY': '买入',
    'SELL': '卖出',
    'HOLD': '持有',
    'STRONG_BUY': '强烈买入',
    'STRONG_SELL': '强烈卖出'
  };

  return recommendationMap[recommendation.toUpperCase()] || recommendation;
};

/**
 * 格式化风险等级
 * @param riskLevel 风险等级
 * @returns 格式化后的风险等级
 */
export const formatRiskLevel = (riskLevel: string | null | undefined): string => {
  if (!riskLevel) {
    return '--';
  }

  const riskLevelMap: Record<string, string> = {
    'LOW': '低风险',
    'MEDIUM': '中风险',
    'HIGH': '高风险'
  };

  return riskLevelMap[riskLevel.toUpperCase()] || riskLevel;
};

/**
 * 格式化任务状态
 * @param status 任务状态
 * @returns 格式化后的任务状态
 */
export const formatTaskStatus = (status: string | null | undefined): string => {
  if (!status) {
    return '--';
  }

  const statusMap: Record<string, string> = {
    'PENDING': '待处理',
    'RUNNING': '运行中',
    'COMPLETED': '已完成',
    'FAILED': '失败',
    'CANCELLED': '已取消'
  };

  return statusMap[status.toUpperCase()] || status;
};

/**
 * 格式化智能体状态
 * @param status 智能体状态
 * @returns 格式化后的智能体状态
 */
export const formatAgentStatus = (status: string | null | undefined): string => {
  if (!status) {
    return '--';
  }

  const statusMap: Record<string, string> = {
    'ACTIVE': '活跃',
    'INACTIVE': '非活跃',
    'BUSY': '忙碌',
    'ERROR': '错误'
  };

  return statusMap[status.toUpperCase()] || status;
};

/**
 * 格式化分析类型
 * @param analysisType 分析类型
 * @returns 格式化后的分析类型
 */
export const formatAnalysisType = (analysisType: string | null | undefined): string => {
  if (!analysisType) {
    return '--';
  }

  const typeMap: Record<string, string> = {
    'TECHNICAL': '技术分析',
    'FUNDAMENTAL': '基本面分析',
    'SENTIMENT': '情绪分析',
    'RISK': '风险分析',
    'COMPREHENSIVE': '综合分析'
  };

  return typeMap[analysisType.toUpperCase()] || analysisType;
};

/**
 * 截断文本
 * @param text 文本
 * @param maxLength 最大长度
 * @param suffix 后缀
 * @returns 截断后的文本
 */
export const truncateText = (text: string | null | undefined, maxLength: number = 50, suffix: string = '...'): string => {
  if (!text) {
    return '--';
  }

  if (text.length <= maxLength) {
    return text;
  }

  return text.substring(0, maxLength - suffix.length) + suffix;
};

/**
 * 高亮关键词
 * @param text 文本
 * @param keyword 关键词
 * @param className CSS类名
 * @returns 高亮后的HTML字符串
 */
export const highlightKeyword = (text: string, keyword: string, className: string = 'highlight'): string => {
  if (!text || !keyword) {
    return text || '';
  }

  const regex = new RegExp(`(${keyword})`, 'gi');
  return text.replace(regex, `<span class="${className}">$1</span>`);
};

/**
 * 格式化置信度
 * @param confidence 置信度（0-1）
 * @returns 格式化后的置信度字符串
 */
export const formatConfidence = (confidence: number | null | undefined): string => {
  if (confidence === null || confidence === undefined || isNaN(confidence)) {
    return '--';
  }

  const percent = Math.round(confidence * 100);

  if (percent >= 80) {
    return `${percent}% (高)`;
  } else if (percent >= 60) {
    return `${percent}% (中)`;
  } else {
    return `${percent}% (低)`;
  }
};

/**
 * 格式化评分
 * @param score 评分（0-100）
 * @returns 格式化后的评分字符串
 */
export const formatScore = (score: number | null | undefined): string => {
  if (score === null || score === undefined || isNaN(score)) {
    return '--';
  }

  if (score >= 80) {
    return `${score}分 (优秀)`;
  } else if (score >= 60) {
    return `${score}分 (良好)`;
  } else if (score >= 40) {
    return `${score}分 (一般)`;
  } else {
    return `${score}分 (较差)`;
  }
};