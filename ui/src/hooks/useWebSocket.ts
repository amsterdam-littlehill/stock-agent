import { useEffect, useRef, useState, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

interface WebSocketOptions {
  onMessage?: (data: any) => void;
  onConnect?: () => void;
  onDisconnect?: () => void;
  onError?: (error: any) => void;
  reconnectDelay?: number;
  maxReconnectAttempts?: number;
}

interface WebSocketHookReturn {
  isConnected: boolean;
  sendMessage: (destination: string, body: any) => void;
  subscribe: (destination: string, callback: (data: any) => void) => () => void;
  connect: () => void;
  disconnect: () => void;
}

// WebSocket Hook
export const useWebSocket = (
  topic?: string,
  options: WebSocketOptions = {}
): WebSocketHookReturn => {
  const {
    onMessage,
    onConnect,
    onDisconnect,
    onError,
    reconnectDelay = 3000,
    maxReconnectAttempts = 5,
  } = options;

  const [isConnected, setIsConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);
  const reconnectAttemptsRef = useRef(0);
  const subscriptionsRef = useRef<Map<string, any>>(new Map());
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // 获取WebSocket URL
  const getWebSocketUrl = useCallback(() => {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = process.env.REACT_APP_WS_HOST || window.location.host;
    return `${protocol}//${host}/ws`;
  }, []);

  // 获取认证token
  const getAuthToken = useCallback(() => {
    return localStorage.getItem('access_token');
  }, []);

  // 连接WebSocket
  const connect = useCallback(() => {
    if (clientRef.current?.connected) {
      return;
    }

    try {
      const client = new Client({
        webSocketFactory: () => new SockJS(getWebSocketUrl()),
        connectHeaders: {Authorization: `Bearer ${getAuthToken()}`,},
        debug: (str) => {
          if (process.env.NODE_ENV === 'development') {
            console.log('STOMP Debug:', str);
          }
        },
        reconnectDelay,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          console.log('WebSocket connected');
          setIsConnected(true);
          reconnectAttemptsRef.current = 0;

          // 订阅指定的topic
          if (topic) {
            subscribe(topic, (message) => {
              try {
                const data = JSON.parse(message.body);
                onMessage?.(data);
              } catch (error) {
                console.error('Failed to parse WebSocket message:', error);
                onMessage?.(message.body);
              }
            });
          }

          onConnect?.();
        },
        onDisconnect: () => {
          console.log('WebSocket disconnected');
          setIsConnected(false);
          subscriptionsRef.current.clear();
          onDisconnect?.();
        },
        onStompError: (frame) => {
          console.error('STOMP Error:', frame);
          setIsConnected(false);
          onError?.(frame);

          // 尝试重连
          if (reconnectAttemptsRef.current < maxReconnectAttempts) {
            reconnectAttemptsRef.current++;
            console.log(`Attempting to reconnect (${reconnectAttemptsRef.current}/${maxReconnectAttempts})...`);

            reconnectTimeoutRef.current = setTimeout(() => {
              connect();
            }, reconnectDelay * reconnectAttemptsRef.current);
          } else {
            console.error('Max reconnection attempts reached');
          }
        },
        onWebSocketError: (error) => {
          console.error('WebSocket Error:', error);
          onError?.(error);
        },
      });

      clientRef.current = client;
      client.activate();
    } catch (error) {
      console.error('Failed to create WebSocket connection:', error);
      onError?.(error);
    }
  }, [topic, onMessage, onConnect, onDisconnect, onError, reconnectDelay, maxReconnectAttempts, getWebSocketUrl, getAuthToken]);

  // 断开连接
  const disconnect = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }

    if (clientRef.current) {
      clientRef.current.deactivate();
      clientRef.current = null;
    }

    setIsConnected(false);
    subscriptionsRef.current.clear();
  }, []);

  // 发送消息
  const sendMessage = useCallback((destination: string, body: any) => {
    if (clientRef.current?.connected) {
      try {
        const messageBody = typeof body === 'string' ? body : JSON.stringify(body);
        clientRef.current.publish({
          destination,
          body: messageBody,
        });
      } catch (error) {
        console.error('Failed to send WebSocket message:', error);
        onError?.(error);
      }
    } else {
      console.warn('WebSocket is not connected. Message not sent.');
    }
  }, [onError]);

  // 订阅topic
  const subscribe = useCallback((destination: string, callback: (data: any) => void) => {
    if (!clientRef.current?.connected) {
      console.warn('WebSocket is not connected. Cannot subscribe.');
      return () => {};
    }

    try {
      const subscription = clientRef.current.subscribe(destination, (message) => {
        callback(message);
      });

      subscriptionsRef.current.set(destination, subscription);

      // 返回取消订阅函数
      return () => {
        subscription.unsubscribe();
        subscriptionsRef.current.delete(destination);
      };
    } catch (error) {
      console.error('Failed to subscribe to topic:', error);
      onError?.(error);
      return () => {};
    }
  }, [onError]);

  // 组件挂载时连接
  useEffect(() => {
    connect();

    // 监听页面可见性变化
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible' && !isConnected) {
        connect();
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);

    // 监听网络状态变化
    const handleOnline = () => {
      if (!isConnected) {
        connect();
      }
    };

    const handleOffline = () => {
      disconnect();
    };

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
      disconnect();
    };
  }, []);

  // 监听认证状态变化
  useEffect(() => {
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === 'access_token') {
        if (e.newValue) {
          // Token更新，重新连接
          disconnect();
          setTimeout(connect, 100);
        } else {
          // Token被移除，断开连接
          disconnect();
        }
      }
    };

    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, [connect, disconnect]);

  return {
    isConnected,
    sendMessage,
    subscribe,
    connect,
    disconnect,
  };
};

// 简化的WebSocket Hook，用于单次连接
export const useSimpleWebSocket = (url: string, options: WebSocketOptions = {}) => {
  const [socket, setSocket] = useState<WebSocket | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const reconnectAttemptsRef = useRef(0);
  const { onMessage, onConnect, onDisconnect, onError, maxReconnectAttempts = 5, reconnectDelay = 3000 } = options;

  const connect = useCallback(() => {
    try {
      const ws = new WebSocket(url);

      ws.onopen = () => {
        console.log('Simple WebSocket connected');
        setIsConnected(true);
        reconnectAttemptsRef.current = 0;
        onConnect?.();
      };

      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          onMessage?.(data);
        } catch {
          onMessage?.(event.data);
        }
      };

      ws.onclose = () => {
        console.log('Simple WebSocket disconnected');
        setIsConnected(false);
        onDisconnect?.();

        // 尝试重连
        if (reconnectAttemptsRef.current < maxReconnectAttempts) {
          reconnectAttemptsRef.current++;
          setTimeout(connect, reconnectDelay * reconnectAttemptsRef.current);
        }
      };

      ws.onerror = (error) => {
        console.error('Simple WebSocket error:', error);
        onError?.(error);
      };

      setSocket(ws);
    } catch (error) {
      console.error('Failed to create simple WebSocket:', error);
      onError?.(error);
    }
  }, [url, onMessage, onConnect, onDisconnect, onError, maxReconnectAttempts, reconnectDelay]);

  const disconnect = useCallback(() => {
    if (socket) {
      socket.close();
      setSocket(null);
      setIsConnected(false);
    }
  }, [socket]);

  const sendMessage = useCallback((data: any) => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      const message = typeof data === 'string' ? data : JSON.stringify(data);
      socket.send(message);
    }
  }, [socket]);

  useEffect(() => {
    connect();
    return disconnect;
  }, []);

  return {
    isConnected,
    sendMessage,
    connect,
    disconnect,
  };
};

export default useWebSocket;