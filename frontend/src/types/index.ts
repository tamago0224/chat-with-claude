// ユーザー関連の型定義
export interface User {
  id: string
  email: string
  name: string
  picture?: string
  createdAt: string
  updatedAt: string
}

// チャットルーム関連の型定義
export interface ChatRoom {
  id: string
  name: string
  description?: string
  owner: User
  isPrivate: boolean
  createdAt: string
  updatedAt: string
  memberCount?: number
  lastMessage?: Message
}

// メッセージ関連の型定義
export interface Message {
  id: string
  room: ChatRoom
  user: User
  content: string
  messageType: 'TEXT' | 'IMAGE' | 'EMOJI'
  imageUrl?: string
  createdAt: string
}

// ルームメンバー関連の型定義
export interface RoomMember {
  id: {
    roomId: string
    userId: string
  }
  room: ChatRoom
  user: User
  joinedAt: string
}

// WebSocket関連の型定義
export interface SendMessageData {
  content: string
  type: 'TEXT' | 'IMAGE' | 'EMOJI'
  imageUrl?: string
}

export interface MessageData {
  id: string
  roomId: string
  userId: string
  userName: string
  userPicture: string
  content: string
  type: 'TEXT' | 'IMAGE' | 'EMOJI'
  imageUrl?: string
  timestamp: string
}

export interface UserJoinedData {
  userId: string
  userName: string
  userPicture: string
}

export interface TypingNotificationData {
  userId: string
  userName: string
  typing: boolean
}

// API レスポンス関連の型定義
export interface ApiResponse<T> {
  data: T
  message?: string
}

export interface PaginatedResponse<T> {
  content: T[]
  pageable: {
    page: number
    size: number
    sort: string
  }
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
  numberOfElements: number
}

// フォーム関連の型定義
export interface CreateRoomForm {
  name: string
  description?: string
  isPrivate: boolean
}

export interface UpdateUserForm {
  name: string
  picture?: string
}

// UI状態関連の型定義
export interface ChatState {
  currentRoom?: ChatRoom
  messages: Message[]
  onlineUsers: string[]
  typingUsers: TypingNotificationData[]
  loading: boolean
  error?: string
}

export interface AuthState {
  user?: User
  token?: string
  loading: boolean
  authenticated: boolean
}

// WebSocket接続状態
export interface SocketState {
  connected: boolean
  connecting: boolean
  error?: string
}

// ファイルアップロード関連
export interface FileUploadResponse {
  url: string
  filename: string
  originalName: string
  size: string
  contentType: string
}

// エラーレスポンス
export interface ApiError {
  error: string
  timestamp: string
  path: string
}