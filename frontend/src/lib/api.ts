import axios from 'axios'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

// APIクライアントの作成
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// リクエストインターセプター（認証トークンの自動付与）
apiClient.interceptors.request.use(
  (config) => {
    // ローカルストレージまたはセッションからトークンを取得
    const token = localStorage.getItem('authToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// レスポンスインターセプター（認証エラーの処理）
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // 認証エラーの場合、トークンをクリアしてログインページにリダイレクト
      localStorage.removeItem('authToken')
      window.location.href = '/auth/signin'
    }
    return Promise.reject(error)
  }
)

// 認証関連API
export const authAPI = {
  // 新規ユーザー登録
  register: (data: { email: string; password: string; name: string }) =>
    apiClient.post('/api/auth/register', data),
  
  // ユーザーログイン
  login: (data: { email: string; password: string }) =>
    apiClient.post('/api/auth/login', data),
  
  // 現在のユーザー情報を取得
  getCurrentUser: () => apiClient.get('/api/auth/me'),
  
  // トークンの有効性を確認
  validateToken: (token: string) => 
    apiClient.post('/api/auth/validate', { token }),
  
  // JWTトークン更新
  refreshToken: () => apiClient.post('/api/auth/refresh'),
  
  // ログアウト
  logout: () => apiClient.post('/api/auth/logout'),
}

// ユーザー関連API
export const userAPI = {
  // ユーザー情報を取得
  getUser: (id: string) => apiClient.get(`/api/users/${id}`),
  
  // ユーザー情報を更新
  updateUser: (id: string, data: any) => 
    apiClient.put(`/api/users/${id}`, data),
  
  // ユーザー検索
  searchUsers: (query: string) => 
    apiClient.get(`/api/users/search?q=${encodeURIComponent(query)}`),
  
  // ルームのユーザー一覧
  getRoomUsers: (roomId: string) => 
    apiClient.get(`/api/users/room/${roomId}`),
}

// チャットルーム関連API
export const roomAPI = {
  // パブリックルーム一覧
  getPublicRooms: (page = 0, size = 20) => 
    apiClient.get(`/api/rooms/public?page=${page}&size=${size}`),
  
  // 参加中ルーム一覧
  getMyRooms: () => apiClient.get('/api/rooms/my'),
  
  // ルーム詳細
  getRoom: (id: string) => apiClient.get(`/api/rooms/${id}`),
  
  // ルーム作成
  createRoom: (data: { name: string; description?: string; isPrivate?: boolean }) =>
    apiClient.post('/api/rooms', data),
  
  // ルーム更新
  updateRoom: (id: string, data: any) => 
    apiClient.put(`/api/rooms/${id}`, data),
  
  // ルーム参加
  joinRoom: (id: string) => apiClient.post(`/api/rooms/${id}/join`),
  
  // ルーム退出
  leaveRoom: (id: string) => apiClient.post(`/api/rooms/${id}/leave`),
  
  // ルームメンバー一覧
  getRoomMembers: (id: string) => apiClient.get(`/api/rooms/${id}/members`),
  
  // ルーム検索
  searchRooms: (query: string, page = 0, size = 20) =>
    apiClient.get(`/api/rooms/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`),
  
  // ルーム削除
  deleteRoom: (id: string) => apiClient.delete(`/api/rooms/${id}`),
}

// メッセージ関連API
export const messageAPI = {
  // ルームのメッセージ履歴
  getRoomMessages: (roomId: string, page = 0, size = 50) =>
    apiClient.get(`/api/messages/room/${roomId}?page=${page}&size=${size}&sort=createdAt,desc`),
  
  // 最新メッセージ取得
  getRecentMessages: (roomId: string, since: string) =>
    apiClient.get(`/api/messages/room/${roomId}/recent?since=${since}`),
  
  // メッセージ検索
  searchMessages: (roomId: string, query: string, page = 0, size = 20) =>
    apiClient.get(`/api/messages/room/${roomId}/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`),
  
  // メッセージ削除
  deleteMessage: (id: string) => apiClient.delete(`/api/messages/${id}`),
  
  // メッセージ統計
  getMessageStats: (roomId: string) => 
    apiClient.get(`/api/messages/room/${roomId}/stats`),
}

// ファイルアップロード関連API
export const fileAPI = {
  // 画像アップロード
  uploadImage: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    
    return apiClient.post('/api/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },
  
  // ファイル削除
  deleteFile: (filename: string) => 
    apiClient.delete(`/api/upload/files/${filename}`),
}

export default apiClient