// 通用工具函数

/**
 * 生成唯一ID
 */
export const generateId = (): string => {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
};

/**
 * 生成UUID
 */
export const generateUUID = (): string => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
};

/**
 * 防抖函数
 */
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  wait: number,
  immediate?: boolean
): ((...args: Parameters<T>) => void) => {
  let timeout: NodeJS.Timeout | null = null;

  return function executedFunction(...args: Parameters<T>) {
    const later = () => {
      timeout = null;
      if (!immediate) func(...args);
    };

    const callNow = immediate && !timeout;

    if (timeout) clearTimeout(timeout);
    timeout = setTimeout(later, wait);

    if (callNow) func(...args);
  };
};

/**
 * 节流函数
 */
export const throttle = <T extends (...args: any[]) => any>(
  func: T,
  limit: number
): ((...args: Parameters<T>) => void) => {
  let inThrottle: boolean;

  return function executedFunction(...args: Parameters<T>) {
    if (!inThrottle) {
      func.apply(this, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  };
};

/**
 * 深拷贝
 */
export const deepClone = <T>(obj: T): T => {
  if (obj === null || typeof obj !== 'object') {
    return obj;
  }

  if (obj instanceof Date) {
    return new Date(obj.getTime()) as unknown as T;
  }

  if (obj instanceof Array) {
    return obj.map(item => deepClone(item)) as unknown as T;
  }

  if (typeof obj === 'object') {
    const clonedObj = {} as { [key: string]: any };
    for (const key in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, key)) {
        clonedObj[key] = deepClone(obj[key]);
      }
    }
    return clonedObj as T;
  }

  return obj;
};

/**
 * 格式化文件大小
 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';

  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

/**
 * 格式化数字
 */
export const formatNumber = (num: number, precision = 2): string => {
  if (num >= 1e9) {
    return (num / 1e9).toFixed(precision) + 'B';
  }
  if (num >= 1e6) {
    return (num / 1e6).toFixed(precision) + 'M';
  }
  if (num >= 1e3) {
    return (num / 1e3).toFixed(precision) + 'K';
  }
  return num.toFixed(precision);
};

/**
 * 格式化时间
 */
export const formatTime = (timestamp: string | number | Date): string => {
  const date = new Date(timestamp);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;
  const week = 7 * day;
  const month = 30 * day;
  const year = 365 * day;

  if (diff < minute) {
    return '刚刚';
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`;
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`;
  } else if (diff < week) {
    return `${Math.floor(diff / day)}天前`;
  } else if (diff < month) {
    return `${Math.floor(diff / week)}周前`;
  } else if (diff < year) {
    return `${Math.floor(diff / month)}个月前`;
  } else {
    return `${Math.floor(diff / year)}年前`;
  }
};

/**
 * 格式化持续时间
 */
export const formatDuration = (milliseconds: number): string => {
  const seconds = Math.floor(milliseconds / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (days > 0) {
    return `${days}天 ${hours % 24}小时 ${minutes % 60}分钟`;
  } else if (hours > 0) {
    return `${hours}小时 ${minutes % 60}分钟 ${seconds % 60}秒`;
  } else if (minutes > 0) {
    return `${minutes}分钟 ${seconds % 60}秒`;
  } else {
    return `${seconds}秒`;
  }
};

/**
 * 验证邮箱
 */
export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

/**
 * 验证URL
 */
export const isValidUrl = (url: string): boolean => {
  try {
    new URL(url);
    return true;
  } catch {
    return false;
  }
};

/**
 * 验证股票代码
 */
export const isValidStockSymbol = (symbol: string): boolean => {
  // 简单的股票代码验证，支持美股、A股等格式
  const stockRegex = /^[A-Z]{1,5}(\.[A-Z]{1,3})?$|^[0-9]{6}$/;
  return stockRegex.test(symbol.toUpperCase());
};

/**
 * 获取文件扩展名
 */
export const getFileExtension = (filename: string): string => {
  return filename.slice((filename.lastIndexOf('.') - 1 >>> 0) + 2);
};

/**
 * 获取文件名（不含扩展名）
 */
export const getFileNameWithoutExtension = (filename: string): string => {
  return filename.replace(/\.[^/.]+$/, '');
};

/**
 * 下载文件
 */
export const downloadFile = (data: Blob | string, filename: string, mimeType?: string): void => {
  const blob = data instanceof Blob ? data : new Blob([data], { type: mimeType || 'text/plain' });
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  window.URL.revokeObjectURL(url);
  document.body.removeChild(a);
};

/**
 * 复制到剪贴板
 */
export const copyToClipboard = async (text: string): Promise<boolean> => {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch {
    // 降级方案
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.left = '-999999px';
    textArea.style.top = '-999999px';
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();

    try {
      document.execCommand('copy');
      document.body.removeChild(textArea);
      return true;
    } catch {
      document.body.removeChild(textArea);
      return false;
    }
  }
};

/**
 * 获取随机颜色
 */
export const getRandomColor = (): string => {
  const colors = [
    '#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1',
    '#13c2c2', '#eb2f96', '#fa541c', '#a0d911', '#2f54eb',
    '#fa8c16', '#eb2f96', '#52c41a', '#1890ff', '#722ed1'
  ];
  return colors[Math.floor(Math.random() * colors.length)];
};

/**
 * 获取对比色（黑色或白色）
 */
export const getContrastColor = (hexColor: string): string => {
  // 移除 # 号
  const color = hexColor.replace('#', '');

  // 转换为 RGB
  const r = parseInt(color.substr(0, 2), 16);
  const g = parseInt(color.substr(2, 2), 16);
  const b = parseInt(color.substr(4, 2), 16);

  // 计算亮度
  const brightness = (r * 299 + g * 587 + b * 114) / 1000;

  return brightness > 128 ? '#000000' : '#ffffff';
};

/**
 * 计算两点之间的距离
 */
export const calculateDistance = (
  point1: { x: number; y: number },
  point2: { x: number; y: number }
): number => {
  const dx = point2.x - point1.x;
  const dy = point2.y - point1.y;
  return Math.sqrt(dx * dx + dy * dy);
};

/**
 * 计算贝塞尔曲线控制点
 */
export const calculateBezierControlPoints = (
  start: { x: number; y: number },
  end: { x: number; y: number }
): { cp1: { x: number; y: number }; cp2: { x: number; y: number } } => {
  const dx = end.x - start.x;
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const dy = end.y - start.y;

  const cp1 = {
    x: start.x + dx * 0.5,
    y: start.y
  };

  const cp2 = {
    x: end.x - dx * 0.5,
    y: end.y
  };

  return {
    cp1,
    cp2
  };
};

/**
 * 生成SVG路径
 */
export const generateSVGPath = (
  start: { x: number; y: number },
  end: { x: number; y: number },
  type: 'straight' | 'curved' | 'step' = 'curved'
): string => {
  switch (type) {
    case 'straight':
      return `M ${start.x} ${start.y} L ${end.x} ${end.y}`;

    case 'step': {
      const midX = start.x + (end.x - start.x) / 2;
      return `M ${start.x} ${start.y} L ${midX} ${start.y} L ${midX} ${end.y} L ${end.x} ${end.y}`;
    }

    case 'curved':
    default: {
      const { cp1, cp2 } = calculateBezierControlPoints(start, end);
      return `M ${start.x} ${start.y} C ${cp1.x} ${cp1.y} ${cp2.x} ${cp2.y} ${end.x} ${end.y}`;
    }
  }
};

/**
 * 检查点是否在矩形内
 */
export const isPointInRect = (
  point: { x: number; y: number },
  rect: { x: number; y: number; width: number; height: number }
): boolean => {
  return point.x >= rect.x &&
         point.x <= rect.x + rect.width &&
         point.y >= rect.y &&
         point.y <= rect.y + rect.height;
};

/**
 * 获取矩形的中心点
 */
export const getRectCenter = (
  rect: { x: number; y: number; width: number; height: number }
): { x: number; y: number } => {
  return {
    x: rect.x + rect.width / 2,
    y: rect.y + rect.height / 2
  };
};

/**
 * 限制数值在指定范围内
 */
export const clamp = (value: number, min: number, max: number): number => {
  return Math.min(Math.max(value, min), max);
};

/**
 * 线性插值
 */
export const lerp = (start: number, end: number, factor: number): number => {
  return start + (end - start) * factor;
};

/**
 * 将数值映射到新的范围
 */
export const mapRange = (
  value: number,
  inMin: number,
  inMax: number,
  outMin: number,
  outMax: number
): number => {
  return (value - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
};

/**
 * 检查对象是否为空
 */
export const isEmpty = (obj: any): boolean => {
  if (obj == null) return true;
  if (Array.isArray(obj) || typeof obj === 'string') return obj.length === 0;
  if (obj instanceof Map || obj instanceof Set) return obj.size === 0;
  return Object.keys(obj).length === 0;
};

/**
 * 安全的JSON解析
 */
export const safeJsonParse = <T = any>(str: string, defaultValue: T): T => {
  try {
    return JSON.parse(str);
  } catch {
    return defaultValue;
  }
};

/**
 * 安全的JSON字符串化
 */
export const safeJsonStringify = (obj: any, defaultValue = '{}'): string => {
  try {
    return JSON.stringify(obj);
  } catch {
    return defaultValue;
  }
};

/**
 * 获取嵌套对象属性
 */
export const getNestedProperty = (obj: any, path: string, defaultValue?: any): any => {
  const keys = path.split('.');
  let result = obj;

  for (const key of keys) {
    if (result == null || typeof result !== 'object') {
      return defaultValue;
    }
    result = result[key];
  }

  return result !== undefined ? result : defaultValue;
};

/**
 * 设置嵌套对象属性
 */
export const setNestedProperty = (obj: any, path: string, value: any): void => {
  const keys = path.split('.');
  const lastKey = keys.pop();

  if (!lastKey) return;

  let current = obj;
  for (const key of keys) {
    if (!(key in current) || typeof current[key] !== 'object') {
      current[key] = {};
    }
    current = current[key];
  }

  current[lastKey] = value;
};

/**
 * 数组去重
 */
export const uniqueArray = <T>(array: T[], key?: keyof T): T[] => {
  if (!key) {
    return [...new Set(array)];
  }

  const seen = new Set();
  return array.filter(item => {
    const value = item[key];
    if (seen.has(value)) {
      return false;
    }
    seen.add(value);
    return true;
  });
};

/**
 * 数组分组
 */
export const groupBy = <T, K extends keyof T>(array: T[], key: K): Record<string, T[]> => {
  return array.reduce((groups, item) => {
    const group = String(item[key]);
    if (!groups[group]) {
      groups[group] = [];
    }
    groups[group].push(item);
    return groups;
  }, {} as Record<string, T[]>);
};

/**
 * 数组排序
 */
export const sortBy = <T>(array: T[], key: keyof T, order: 'asc' | 'desc' = 'asc'): T[] => {
  return [...array].sort((a, b) => {
    const aVal = a[key];
    const bVal = b[key];

    if (aVal < bVal) return order === 'asc' ? -1 : 1;
    if (aVal > bVal) return order === 'asc' ? 1 : -1;
    return 0;
  });
};

/**
 * 等待指定时间
 */
export const sleep = (ms: number): Promise<void> => {
  return new Promise(resolve => setTimeout(resolve, ms));
};

/**
 * 重试函数
 */
export const retry = async <T>(
  fn: () => Promise<T>,
  maxAttempts = 3,
  delay = 1000
): Promise<T> => {
  let lastError: Error;

  for (let attempt = 1; attempt <= maxAttempts; attempt++) {
    try {
      return await fn();
    } catch (error) {
      lastError = error as Error;
      if (attempt < maxAttempts) {
        await sleep(delay * attempt);
      }
    }
  }

  throw lastError!;
};