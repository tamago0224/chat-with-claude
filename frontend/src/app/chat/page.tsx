'use client'

import React, { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Toaster, toast } from 'react-hot-toast'
import Layout from '@/components/layout/Layout'
import ChatRoom from '@/components/chat/ChatRoom'
import CreateRoomModal from '@/components/room/CreateRoomModal'
import RoomInfoPanel from '@/components/room/RoomInfoPanel'
import { ChatRoom as ChatRoomType, User, CreateRoomForm } from '@/types'
import { authAPI, roomAPI } from '@/lib/api'
import { getWebSocketClient } from '@/lib/websocket'

export default function ChatPage() {
  const router = useRouter()
  
  // 状態管理
  const [user, setUser] = useState<User | null>(null)
  const [rooms, setRooms] = useState<ChatRoomType[]>([])
  const [activeRoom, setActiveRoom] = useState<ChatRoomType | null>(null)
  const [loading, setLoading] = useState(true)
  const [showCreateRoomModal, setShowCreateRoomModal] = useState(false)
  const [showRoomInfo, setShowRoomInfo] = useState(false)
  const [createRoomLoading, setCreateRoomLoading] = useState(false)

  // 初期化
  useEffect(() => {
    initializeApp()
  }, [])

  const initializeApp = async () => {
    try {
      // 認証チェック
      const token = localStorage.getItem('authToken')
      if (!token) {
        router.push('/auth/signin')
        return
      }

      // ユーザー情報を取得
      const userResponse = await authAPI.getCurrentUser()
      setUser(userResponse.data)

      // 参加中のルーム一覧を取得
      await loadMyRooms()

    } catch (error) {
      console.error('Initialization error:', error)
      // 認証エラーの場合はログインページにリダイレクト
      localStorage.removeItem('authToken')
      localStorage.removeItem('user')
      router.push('/auth/signin')
    } finally {
      setLoading(false)
    }
  }

  const loadMyRooms = async () => {
    try {
      const response = await roomAPI.getMyRooms()
      setRooms(response.data)
      
      // 最初のルームを自動選択
      if (response.data.length > 0 && !activeRoom) {
        setActiveRoom(response.data[0])
      }
    } catch (error) {
      console.error('Failed to load rooms:', error)
      toast.error('ルーム一覧の取得に失敗しました')
    }
  }

  const loadPublicRooms = async () => {
    try {
      const response = await roomAPI.getPublicRooms()
      return response.data.content
    } catch (error) {
      console.error('Failed to load public rooms:', error)
      toast.error('パブリックルームの取得に失敗しました')
      return []
    }
  }

  const handleRoomSelect = (roomId: string) => {
    const room = rooms.find(r => r.id === roomId)
    if (room) {
      setActiveRoom(room)
      setShowRoomInfo(false)
    }
  }

  const handleCreateRoom = async (data: CreateRoomForm) => {
    setCreateRoomLoading(true)
    try {
      const response = await roomAPI.createRoom(data)
      const newRoom = response.data
      
      // ルーム一覧に追加
      setRooms(prev => [newRoom, ...prev])
      setActiveRoom(newRoom)
      
      toast.success(`ルーム「${newRoom.name}」を作成しました`)
    } catch (error: any) {
      console.error('Failed to create room:', error)
      if (error.response?.status === 409) {
        toast.error('同じ名前のルームが既に存在します')
      } else {
        toast.error('ルームの作成に失敗しました')
      }
      throw error // モーダルでエラーハンドリングするため
    } finally {
      setCreateRoomLoading(false)
    }
  }

  const handleJoinRoom = async (roomId: string) => {
    try {
      await roomAPI.joinRoom(roomId)
      await loadMyRooms() // ルーム一覧を再読み込み
      
      // 参加したルームを選択
      const joinedRoom = rooms.find(r => r.id === roomId)
      if (joinedRoom) {
        setActiveRoom(joinedRoom)
      }
      
      toast.success('ルームに参加しました')
    } catch (error) {
      console.error('Failed to join room:', error)
      toast.error('ルームへの参加に失敗しました')
    }
  }

  const handleLeaveRoom = async () => {
    if (!activeRoom) return

    try {
      await roomAPI.leaveRoom(activeRoom.id)
      
      // ルーム一覧から削除
      setRooms(prev => prev.filter(r => r.id !== activeRoom.id))
      
      // 別のルームを選択または選択解除
      const remainingRooms = rooms.filter(r => r.id !== activeRoom.id)
      setActiveRoom(remainingRooms.length > 0 ? remainingRooms[0] : null)
      
      toast.success(`ルーム「${activeRoom.name}」から退出しました`)
    } catch (error) {
      console.error('Failed to leave room:', error)
      toast.error('ルームからの退出に失敗しました')
    }
  }

  const handleDeleteRoom = async () => {
    if (!activeRoom) return

    try {
      await roomAPI.deleteRoom(activeRoom.id)
      
      // ルーム一覧から削除
      setRooms(prev => prev.filter(r => r.id !== activeRoom.id))
      
      // 別のルームを選択または選択解除
      const remainingRooms = rooms.filter(r => r.id !== activeRoom.id)
      setActiveRoom(remainingRooms.length > 0 ? remainingRooms[0] : null)
      
      toast.success(`ルーム「${activeRoom.name}」を削除しました`)
    } catch (error) {
      console.error('Failed to delete room:', error)
      toast.error('ルームの削除に失敗しました')
    }
  }

  const handleLogout = async () => {
    try {
      // WebSocket接続を切断
      const wsClient = getWebSocketClient()
      wsClient.disconnect()

      // バックエンドにログアウト通知
      await authAPI.logout()
      
      // ローカルストレージをクリア
      localStorage.removeItem('authToken')
      localStorage.removeItem('user')
      
      // ログインページにリダイレクト
      router.push('/auth/signin')
      
      toast.success('ログアウトしました')
    } catch (error) {
      console.error('Logout error:', error)
      // エラーが発生してもログアウト処理は続行
      localStorage.removeItem('authToken')
      localStorage.removeItem('user')
      router.push('/auth/signin')
    }
  }

  // ローディング画面
  if (loading) {
    return (
      <div className="h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500 mx-auto mb-4"></div>
          <p className="text-gray-600">読み込み中...</p>
        </div>
      </div>
    )
  }

  // ユーザー情報がない場合
  if (!user) {
    return (
      <div className="h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <p className="text-gray-600 mb-4">ユーザー情報の取得に失敗しました</p>
          <button
            onClick={() => router.push('/auth/signin')}
            className="bg-primary-500 text-white px-4 py-2 rounded-md hover:bg-primary-600"
          >
            ログインページに戻る
          </button>
        </div>
      </div>
    )
  }

  return (
    <>
      <Layout
        user={user}
        rooms={rooms}
        activeRoomId={activeRoom?.id}
        onRoomSelect={handleRoomSelect}
        onCreateRoom={() => setShowCreateRoomModal(true)}
        onJoinRoom={handleJoinRoom}
        onLogout={handleLogout}
      >
        <div className="flex h-full">
          {/* メインチャットエリア */}
          <div className="flex-1 flex flex-col">
            {activeRoom ? (
              <ChatRoom
                room={activeRoom}
                currentUser={user}
                onShowRoomInfo={() => setShowRoomInfo(true)}
              />
            ) : (
              <div className="flex-1 flex items-center justify-center bg-gray-50">
                <div className="text-center">
                  <h2 className="text-xl font-semibold text-gray-900 mb-2">
                    チャットルームを選択してください
                  </h2>
                  <p className="text-gray-600 mb-4">
                    左のサイドバーからルームを選択するか、新しいルームを作成してください
                  </p>
                  <button
                    onClick={() => setShowCreateRoomModal(true)}
                    className="bg-primary-500 text-white px-4 py-2 rounded-md hover:bg-primary-600"
                  >
                    新しいルームを作成
                  </button>
                </div>
              </div>
            )}
          </div>

          {/* ルーム情報パネル */}
          {activeRoom && (
            <RoomInfoPanel
              room={activeRoom}
              currentUser={user}
              isOpen={showRoomInfo}
              onClose={() => setShowRoomInfo(false)}
              onLeaveRoom={handleLeaveRoom}
              onDeleteRoom={handleDeleteRoom}
            />
          )}
        </div>
      </Layout>

      {/* ルーム作成モーダル */}
      <CreateRoomModal
        isOpen={showCreateRoomModal}
        onClose={() => setShowCreateRoomModal(false)}
        onCreateRoom={handleCreateRoom}
        loading={createRoomLoading}
      />

      {/* トースト通知 */}
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: {
            background: '#363636',
            color: '#fff',
          },
          success: {
            duration: 3000,
            iconTheme: {
              primary: '#10b981',
              secondary: '#fff',
            },
          },
          error: {
            duration: 5000,
            iconTheme: {
              primary: '#ef4444',
              secondary: '#fff',
            },
          },
        }}
      />
    </>
  )
}