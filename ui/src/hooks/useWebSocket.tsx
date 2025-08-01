import { useState, useEffect, useRef, useCallback } from 'react';

export interface WebSocketOptions {
  reconnectInterval?: number;
  maxReconnectAttempts?: number;
  onOpen?: (event: Event) => void;
  onMessage?: (data: any) => void;
  onError?: (error: Event) => void;
  onClose?: (event: CloseEvent) => void;
}

export interface WebSocketState {
  socket: WebSocket | null;
  lastMessage: any;
  readyState: number;
  isConnected: boolean;
  reconnectCount: number;
}

export const useWebSocket = (
  url: string | null,
  options: WebSocketOptions = {}
) => {
  const {
    reconnectInterval = 3000,
    maxReconnectAttempts = 5,
    onOpen,
    onMessage,
    onError,
    onClose
  } = options;

  const [state, setState] = useState<WebSocketState>({
    socket: null,
    lastMessage: null,
    readyState: WebSocket.CLOSED,
    isConnected: false,
    reconnectCount: 0
  });

  const reconnectTimeoutRef = useRef<NodeJS.Timeout>();
  const socketRef = useRef<WebSocket | null>(null);

  const connect = useCallback(() => {
    if (!url) return;

    try {
      const socket = new WebSocket(url);
      socketRef.current = socket;

      socket.onopen = (event) => {
        setState(prev => ({
          ...prev,
          socket,
          readyState: socket.readyState,
          isConnected: true,
          reconnectCount: 0
        }));

        if (onOpen) {
          onOpen(event);
        }
      };

      socket.onmessage = (event) => {
        let data;
        try {
          data = JSON.parse(event.data);
        } catch {
          data = event.data;
        }

        setState(prev => ({
          ...prev,
          lastMessage: data
        }));

        if (onMessage) {
          onMessage(data);
        }
      };

      socket.onerror = (error) => {
        console.error('WebSocket error:', error);
        if (onError) {
          onError(error);
        }
      };

      socket.onclose = (event) => {
        setState(prev => ({
          ...prev,
          socket: null,
          readyState: WebSocket.CLOSED,
          isConnected: false
        }));

        if (onClose) {
          onClose(event);
        }

        // 自动重连
        if (state.reconnectCount < maxReconnectAttempts && !event.wasClean) {
          setState(prev => ({
            ...prev,
            reconnectCount: prev.reconnectCount + 1
          }));

          reconnectTimeoutRef.current = setTimeout(() => {
            connect();
          }, reconnectInterval);
        }
      };

      setState(prev => ({
        ...prev,
        socket,
        readyState: socket.readyState
      }));

    } catch (error) {
      console.error('Failed to create WebSocket connection:', error);
    }
  }, [url, onOpen, onMessage, onError, onClose, reconnectInterval, maxReconnectAttempts, state.reconnectCount]);

  const disconnect = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
    }

    if (socketRef.current) {
      socketRef.current.close();
      socketRef.current = null;
    }

    setState(prev => ({
      ...prev,
      socket: null,
      readyState: WebSocket.CLOSED,
      isConnected: false,
      reconnectCount: 0
    }));
  }, []);

  const sendMessage = useCallback((message: any) => {
    if (state.socket && state.socket.readyState === WebSocket.OPEN) {
      const data = typeof message === 'string' ? message : JSON.stringify(message);
      state.socket.send(data);
      return true;
    }
    return false;
  }, [state.socket]);

  useEffect(() => {
    if (url) {
      connect();
    }

    return () => {
      disconnect();
    };
  }, [url, connect, disconnect]);

  return {
    ...state,
    sendMessage,
    connect,
    disconnect
  };
};
