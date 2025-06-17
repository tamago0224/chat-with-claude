import { io, Socket } from 'socket.io-client'
import { SendMessageData, MessageData, UserJoinedData, TypingNotificationData } from '@/types'

interface ServerToClientEvents {
  connected: (data: { userId: string }) => void
  joined_room: (data: { roomId: string }) => void
  new_message: (data: MessageData) => void
  user_joined: (data: UserJoinedData) => void
  user_left: (data: { userId: string }) => void
  user_typing: (data: TypingNotificationData) => void
  error: (data: { message: string }) => void
}

interface ClientToServerEvents {
  join_room: (data: { roomId: string }) => void
  leave_room: (data: { roomId: string }) => void
  send_message: (data: SendMessageData) => void
  typing: (data: { typing: boolean }) => void
}

export class WebSocketClient {
  private socket: Socket<ServerToClientEvents, ClientToServerEvents> | null = null
  private token: string | null = null
  private currentRoomId: string | null = null
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectDelay = 1000

  // イベントハンドラー
  private eventHandlers: {
    [K in keyof ServerToClientEvents]?: ServerToClientEvents[K][]
  } = {}

  constructor() {
    this.token = localStorage.getItem('authToken')
  }

  // 接続
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (!this.token) {
        reject(new Error('認証トークンが必要です'))
        return
      }

      if (this.socket?.connected) {
        resolve()
        return
      }

      const WS_URL = process.env.NEXT_PUBLIC_WS_URL || 'http://localhost:8081'

      this.socket = io(WS_URL, {
        query: { token: this.token },
        transports: ['websocket'],
        timeout: 10000,
      })

      this.socket.on('connect', () => {
        console.log('WebSocket connected')
        this.reconnectAttempts = 0
        resolve()
      })

      this.socket.on('connect_error', (error) => {
        console.error('WebSocket connection error:', error)
        this.handleReconnect()
        reject(error)
      })

      this.socket.on('disconnect', (reason) => {
        console.log('WebSocket disconnected:', reason)
        if (reason === 'io server disconnect') {
          // サーバーから切断された場合は再接続を試行
          this.handleReconnect()
        }
      })

      // サーバーイベントのリスニング設定
      this.setupEventListeners()
    })
  }

  // 切断
  disconnect(): void {
    if (this.socket) {
      this.socket.disconnect()
      this.socket = null
    }
    this.currentRoomId = null
  }

  // 再接続処理
  private handleReconnect(): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`再接続試行 ${this.reconnectAttempts}/${this.maxReconnectAttempts}`)
      
      setTimeout(() => {
        this.connect().catch((error) => {
          console.error('再接続失敗:', error)
        })
      }, this.reconnectDelay * this.reconnectAttempts)
    } else {
      console.error('最大再接続回数に達しました')
      this.emit('error', { message: '接続が失われました' })
    }
  }

  // イベントリスナーの設定
  private setupEventListeners(): void {
    if (!this.socket) return

    this.socket.on('connected', (data) => this.emit('connected', data))
    this.socket.on('joined_room', (data) => this.emit('joined_room', data))
    this.socket.on('new_message', (data) => this.emit('new_message', data))
    this.socket.on('user_joined', (data) => this.emit('user_joined', data))
    this.socket.on('user_left', (data) => this.emit('user_left', data))
    this.socket.on('user_typing', (data) => this.emit('user_typing', data))
    this.socket.on('error', (data) => this.emit('error', data))
  }

  // ルームに参加
  joinRoom(roomId: string): void {
    if (!this.socket?.connected) {
      console.error('WebSocket not connected')
      return
    }

    if (this.currentRoomId && this.currentRoomId !== roomId) {
      // 現在のルームから退出
      this.leaveRoom(this.currentRoomId)
    }

    this.socket.emit('join_room', { roomId })
    this.currentRoomId = roomId
  }

  // ルームから退出
  leaveRoom(roomId: string): void {
    if (!this.socket?.connected) {
      console.error('WebSocket not connected')
      return
    }

    this.socket.emit('leave_room', { roomId })
    if (this.currentRoomId === roomId) {
      this.currentRoomId = null
    }
  }

  // メッセージ送信
  sendMessage(data: SendMessageData): void {
    if (!this.socket?.connected) {
      console.error('WebSocket not connected')
      return
    }

    if (!this.currentRoomId) {
      console.error('Not in any room')
      return
    }

    this.socket.emit('send_message', data)
  }

  // タイピング状態の送信
  sendTyping(typing: boolean): void {
    if (!this.socket?.connected || !this.currentRoomId) {
      return
    }

    this.socket.emit('typing', { typing })
  }

  // イベントハンドラーの登録
  on<K extends keyof ServerToClientEvents>(
    event: K,
    handler: ServerToClientEvents[K]
  ): void {
    if (!this.eventHandlers[event]) {
      this.eventHandlers[event] = []
    }
    this.eventHandlers[event]!.push(handler)
  }

  // イベントハンドラーの削除
  off<K extends keyof ServerToClientEvents>(
    event: K,
    handler: ServerToClientEvents[K]
  ): void {
    if (!this.eventHandlers[event]) return
    
    const index = this.eventHandlers[event]!.indexOf(handler)
    if (index > -1) {
      this.eventHandlers[event]!.splice(index, 1)
    }
  }

  // イベントの発火
  private emit<K extends keyof ServerToClientEvents>(
    event: K,
    ...args: Parameters<ServerToClientEvents[K]>
  ): void {
    if (!this.eventHandlers[event]) return
    
    this.eventHandlers[event]!.forEach((handler) => {
      try {
        ;(handler as any)(...args)
      } catch (error) {
        console.error(`Error in ${event} handler:`, error)
      }
    })
  }

  // 接続状態の確認
  get connected(): boolean {
    return this.socket?.connected || false
  }

  // 現在のルームID
  get currentRoom(): string | null {
    return this.currentRoomId
  }

  // トークンの更新
  updateToken(token: string): void {
    this.token = token
    localStorage.setItem('authToken', token)
    
    // 既に接続している場合は再接続
    if (this.socket?.connected) {
      this.disconnect()
      this.connect().catch((error) => {
        console.error('Token update reconnection failed:', error)
      })
    }
  }
}

// シングルトンインスタンス
let wsClient: WebSocketClient | null = null

export const getWebSocketClient = (): WebSocketClient => {
  if (!wsClient) {
    wsClient = new WebSocketClient()
  }
  return wsClient
}

export default getWebSocketClient