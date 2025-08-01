/**
 * WebSocket服务
 * 用于实时接收股票价格更新、分析任务进度等
 */

import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {
  TaskUpdateMessage,
  PriceUpdateMessage,
  AnalysisCompleteMessage
} from '../types/stock';

type MessageHandler = (message: any) => void;
type ConnectionHandler = () => void;
type ErrorHandler = (error: any) => void;

class WebSocketService {
  private client: Client | null = null;
  private connected = false;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 5000;
  private subscriptions = new Map<string, any>();
  private messageHandlers = new Map<string, MessageHandler[]>();
  private connectionHandlers: ConnectionHandler[] = [];
  private disconnectionHandlers: ConnectionHandler[] = [];
  private errorHandlers: ErrorHandler[] = [];

  constructor() {
    this.initializeClient();
  }

  /**
   * 初始化WebSocket客户端
   */
  private initializeClient() {
    const wsUrl = process.env.REACT_APP_WS_URL || 'http://localhost:8080/ws';

    this.client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      connectHeaders: {
        // 添加认证头
        Authorization: `Bearer ${localStorage.getItem('token') || ''}`,
      },
      debug: (str) => {
        if (process.env.NODE_ENV === 'development') {
          console.log('WebSocket Debug:', str);
        }
      },
      reconnectDelay: this.reconnectInterval,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('WebSocket连接成功');
        this.connected = true;
        this.reconnectAttempts = 0;
        this.connectionHandlers.forEach(handler => handler());
        this.resubscribeAll();
      },
      onDisconnect: () => {
        console.log('WebSocket连接断开');
        this.connected = false;
        this.disconnectionHandlers.forEach(handler => handler());
      },
      onStompError: (frame) => {
        console.error('WebSocket STOMP错误:', frame.headers['message']);
        console.error('详细信息:', frame.body);
        this.errorHandlers.forEach(handler => handler(frame));
      },
      onWebSocketError: (error) => {
        console.error('WebSocket错误:', error);
        this.errorHandlers.forEach(handler => handler(error));
      },
      onWebSocketClose: () => {
        console.log('WebSocket连接关闭');
        this.connected = false;
        this.handleReconnect();
      },
    });
  }

  /**
   * 连接WebSocket
   */
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.connected) {
        resolve();
        return;
      }

      if (!this.client) {
        this.initializeClient();
      }

      const onConnect = () => {
        this.removeConnectionHandler(onConnect);
        resolve();
      };

      const onError = (error: any) => {
        this.removeErrorHandler(onError);
        reject(error);
      };

      this.addConnectionHandler(onConnect);
      this.addErrorHandler(onError);

      this.client!.activate();
    });
  }

  /**
   * 断开WebSocket连接
   */
  disconnect(): Promise<void> {
    return new Promise((resolve) => {
      if (!this.connected || !this.client) {
        resolve();
        return;
      }

      const onDisconnect = () => {
        this.removeDisconnectionHandler(onDisconnect);
        resolve();
      };

      this.addDisconnectionHandler(onDisconnect);
      this.client.deactivate();
    });
  }

  /**
   * 处理重连
   */
  private handleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('WebSocket重连次数超过限制，停止重连');
      return;
    }

    this.reconnectAttempts++;
    console.log(`WebSocket重连尝试 ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);

    setTimeout(() => {
      if (!this.connected && this.client) {
        this.client.activate();
      }
    }, this.reconnectInterval * this.reconnectAttempts);
  }

  /**
   * 重新订阅所有主题
   */
  private resubscribeAll() {
    this.subscriptions.forEach((subscription, destination) => {
      this.subscriptions.delete(destination);
      const handlers = this.messageHandlers.get(destination) || [];
      handlers.forEach(handler => {
        this.subscribe(destination, handler);
      });
    });
  }

  /**
   * 订阅主题
   */
  subscribe(destination: string, handler: MessageHandler): () => void {
    if (!this.client) {
      throw new Error('WebSocket客户端未初始化');
    }

    // 保存消息处理器
    if (!this.messageHandlers.has(destination)) {
      this.messageHandlers.set(destination, []);
    }
    this.messageHandlers.get(destination)!.push(handler);

    // 如果已连接，立即订阅
    if (this.connected) {
      const subscription = this.client.subscribe(destination, (message: IMessage) => {
        try {
          const data = JSON.parse(message.body);
          handler(data);
        } catch (error) {
          console.error('解析WebSocket消息失败:', error);
          console.error('原始消息:', message.body);
        }
      });

      this.subscriptions.set(destination, subscription);

      // 返回取消订阅函数
      return () => {
        subscription.unsubscribe();
        this.subscriptions.delete(destination);
        const handlers = this.messageHandlers.get(destination) || [];
        const index = handlers.indexOf(handler);
        if (index > -1) {
          handlers.splice(index, 1);
        }
      };
    }

    // 如果未连接，返回空函数
    return () => {
      const handlers = this.messageHandlers.get(destination) || [];
      const index = handlers.indexOf(handler);
      if (index > -1) {
        handlers.splice(index, 1);
      }
    };
  }

  /**
   * 发送消息
   */
  send(destination: string, body: any, headers: any = {}) {
    if (!this.client || !this.connected) {
      throw new Error('WebSocket未连接');
    }

    this.client.publish({
      destination,
      body: JSON.stringify(body),
      headers,
    });
  }

  /**
   * 订阅股票价格更新
   */
  subscribeStockPrice(stockCode: string, handler: (data: PriceUpdateMessage) => void): () => void {
    return this.subscribe(`/topic/stock/price/${stockCode}`, handler);
  }

  /**
   * 订阅所有股票价格更新
   */
  subscribeAllStockPrices(handler: (data: PriceUpdateMessage) => void): () => void {
    return this.subscribe('/topic/stock/prices', handler);
  }

  /**
   * 订阅分析任务更新
   */
  subscribeTaskUpdate(taskId: number, handler: (data: TaskUpdateMessage) => void): () => void {
    return this.subscribe(`/topic/analysis/task/${taskId}`, handler);
  }

  /**
   * 订阅用户的所有任务更新
   */
  subscribeUserTasks(userId: string, handler: (data: TaskUpdateMessage) => void): () => void {
    return this.subscribe(`/user/${userId}/tasks`, handler);
  }

  /**
   * 订阅分析完成通知
   */
  subscribeAnalysisComplete(handler: (data: AnalysisCompleteMessage) => void): () => void {
    return this.subscribe('/topic/analysis/complete', handler);
  }

  /**
   * 订阅系统通知
   */
  subscribeSystemNotifications(handler: (data: any) => void): () => void {
    return this.subscribe('/topic/system/notifications', handler);
  }

  /**
   * 订阅市场概况更新
   */
  subscribeMarketOverview(handler: (data: any) => void): () => void {
    return this.subscribe('/topic/market/overview', handler);
  }

  /**
   * 订阅行业分析更新
   */
  subscribeIndustryAnalysis(industry: string, handler: (data: any) => void): () => void {
    return this.subscribe(`/topic/industry/${industry}`, handler);
  }

  /**
   * 添加连接处理器
   */
  addConnectionHandler(handler: ConnectionHandler) {
    this.connectionHandlers.push(handler);
  }

  /**
   * 移除连接处理器
   */
  removeConnectionHandler(handler: ConnectionHandler) {
    const index = this.connectionHandlers.indexOf(handler);
    if (index > -1) {
      this.connectionHandlers.splice(index, 1);
    }
  }

  /**
   * 添加断开连接处理器
   */
  addDisconnectionHandler(handler: ConnectionHandler) {
    this.disconnectionHandlers.push(handler);
  }

  /**
   * 移除断开连接处理器
   */
  removeDisconnectionHandler(handler: ConnectionHandler) {
    const index = this.disconnectionHandlers.indexOf(handler);
    if (index > -1) {
      this.disconnectionHandlers.splice(index, 1);
    }
  }

  /**
   * 添加错误处理器
   */
  addErrorHandler(handler: ErrorHandler) {
    this.errorHandlers.push(handler);
  }

  /**
   * 移除错误处理器
   */
  removeErrorHandler(handler: ErrorHandler) {
    const index = this.errorHandlers.indexOf(handler);
    if (index > -1) {
      this.errorHandlers.splice(index, 1);
    }
  }

  /**
   * 获取连接状态
   */
  isConnected(): boolean {
    return this.connected;
  }

  /**
   * 获取重连次数
   */
  getReconnectAttempts(): number {
    return this.reconnectAttempts;
  }

  /**
   * 重置重连次数
   */
  resetReconnectAttempts() {
    this.reconnectAttempts = 0;
  }

  /**
   * 设置最大重连次数
   */
  setMaxReconnectAttempts(attempts: number) {
    this.maxReconnectAttempts = attempts;
  }

  /**
   * 设置重连间隔
   */
  setReconnectInterval(interval: number) {
    this.reconnectInterval = interval;
  }
}

// 创建全局WebSocket服务实例
const websocketService = new WebSocketService();

export default websocketService;

// 导出类型
export type { MessageHandler, ConnectionHandler, ErrorHandler };

// 导出便捷的Hook函数
export const useWebSocket = () => {
  return {
    connect: () => websocketService.connect(),
    disconnect: () => websocketService.disconnect(),
    subscribe: (destination: string, handler: MessageHandler) =>
      websocketService.subscribe(destination, handler),
    send: (destination: string, body: any, headers?: any) =>
      websocketService.send(destination, body, headers),
    isConnected: () => websocketService.isConnected(),
    subscribeStockPrice: (stockCode: string, handler: (data: PriceUpdateMessage) => void) =>
      websocketService.subscribeStockPrice(stockCode, handler),
    subscribeTaskUpdate: (taskId: number, handler: (data: TaskUpdateMessage) => void) =>
      websocketService.subscribeTaskUpdate(taskId, handler),
    subscribeAnalysisComplete: (handler: (data: AnalysisCompleteMessage) => void) =>
      websocketService.subscribeAnalysisComplete(handler),
    subscribeSystemNotifications: (handler: (data: any) => void) =>
      websocketService.subscribeSystemNotifications(handler),
  };
};