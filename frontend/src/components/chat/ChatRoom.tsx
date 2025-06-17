import React, { useState, useEffect, useCallback } from 'react'
import { Users, Settings, Search } from 'lucide-react'
import MessageList from './MessageList'
import MessageInput from './MessageInput'
import { ChatRoom as ChatRoomType, Message, User, SendMessageData, TypingNotificationData } from '@/types'
import { getWebSocketClient } from '@/lib/websocket'
import { messageAPI } from '@/lib/api'

interface ChatRoomProps {
  room: ChatRoomType
  currentUser: User
  onShowRoomInfo: () => void
}

const ChatRoom: React.FC<ChatRoomProps> = ({ room, currentUser, onShowRoomInfo }) => {
  const [messages, setMessages] = useState<Message[]>([])
  const [typingUsers, setTypingUsers] = useState<TypingNotificationData[]>([])
  const [loading, setLoading] = useState(false)
  const [hasMore, setHasMore] = useState(true)
  const [page, setPage] = useState(0)
  const [connected, setConnected] = useState(false)

  const wsClient = getWebSocketClient()

  // メッセージ履歴の読み込み
  const loadMessages = useCallback(async (pageNum = 0, append = false) => {
    if (loading) return

    setLoading(true)
    try {
      const response = await messageAPI.getRoomMessages(room.id, pageNum, 50)
      const newMessages = response.data.content
      
      if (append) {
        setMessages(prev => [...newMessages.reverse(), ...prev])
      } else {
        setMessages(newMessages.reverse())
      }
      
      setHasMore(!response.data.last)
      setPage(pageNum)
    } catch (error) {
      console.error('Failed to load messages:', error)
    } finally {
      setLoading(false)
    }
  }, [room.id, loading])

  // 過去のメッセージを読み込み
  const loadMoreMessages = useCallback(() => {
    if (hasMore && !loading) {
      loadMessages(page + 1, true)
    }
  }, [hasMore, loading, page, loadMessages])

  // WebSocket接続とイベントリスナーの設定
  useEffect(() => {
    const setupWebSocket = async () => {
      try {
        await wsClient.connect()
        setConnected(true)
        
        // ルームに参加
        wsClient.joinRoom(room.id)

        // イベントリスナーの設定
        wsClient.on('new_message', (data) => {
          const newMessage: Message = {
            id: data.id,
            room: room,
            user: {
              id: data.userId,
              name: data.userName,
              email: '',
              picture: data.userPicture,
              createdAt: '',
              updatedAt: '',
            },
            content: data.content,
            messageType: data.type,
            imageUrl: data.imageUrl,
            createdAt: data.timestamp,
          }
          
          setMessages(prev => [...prev, newMessage])
        })

        wsClient.on('user_typing', (data) => {
          if (data.userId === currentUser.id) return
          
          setTypingUsers(prev => {
            const filtered = prev.filter(user => user.userId !== data.userId)
            if (data.typing) {
              return [...filtered, data]
            }
            return filtered
          })
        })

        wsClient.on('user_joined', (data) => {
          console.log('User joined:', data)
        })

        wsClient.on('user_left', (data) => {
          console.log('User left:', data)
          // タイピング状態からも削除
          setTypingUsers(prev => prev.filter(user => user.userId !== data.userId))
        })

        wsClient.on('error', (data) => {
          console.error('WebSocket error:', data.message)
        })

      } catch (error) {
        console.error('Failed to connect to WebSocket:', error)
        setConnected(false)
      }
    }

    setupWebSocket()

    // 初回メッセージ読み込み
    loadMessages(0, false)

    return () => {
      // ルームから退出
      if (connected) {
        wsClient.leaveRoom(room.id)
      }
    }
  }, [room.id, currentUser.id, wsClient, loadMessages, connected])

  // メッセージ送信
  const handleSendMessage = useCallback((data: SendMessageData) => {
    if (!connected) {
      console.error('WebSocket not connected')
      return
    }
    
    wsClient.sendMessage(data)
  }, [connected, wsClient])

  // タイピング状態の送信
  const handleTypingChange = useCallback((isTyping: boolean) => {
    if (!connected) return
    
    wsClient.sendTyping(isTyping)
  }, [connected, wsClient])

  return (
    <div className="flex flex-col h-full bg-white">
      {/* ルームヘッダー */}
      <div className="border-b border-gray-200 px-4 py-3 flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div>
            <h2 className="text-lg font-semibold text-gray-900">{room.name}</h2>
            {room.description && (
              <p className="text-sm text-gray-500">{room.description}</p>
            )}
          </div>
          {!connected && (
            <span className="text-xs text-red-500 bg-red-50 px-2 py-1 rounded-full">
              接続中...
            </span>
          )}
        </div>

        <div className="flex items-center space-x-2">
          <button
            className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-md"
            title="検索"
          >
            <Search size={18} />
          </button>
          
          <button
            onClick={onShowRoomInfo}
            className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-md"
            title="ルーム情報"
          >
            <Users size={18} />
          </button>
          
          <button
            className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-md"
            title="設定"
          >
            <Settings size={18} />
          </button>
        </div>
      </div>

      {/* メッセージエリア */}
      <MessageList
        messages={messages}
        currentUser={currentUser}
        typingUsers={typingUsers}
        loading={loading}
        hasMore={hasMore}
        onLoadMore={loadMoreMessages}
      />

      {/* メッセージ入力 */}
      <MessageInput
        onSendMessage={handleSendMessage}
        onTypingChange={handleTypingChange}
        disabled={!connected}
        placeholder={connected ? 'メッセージを入力...' : '接続中...'}
      />
    </div>
  )
}

export default ChatRoom