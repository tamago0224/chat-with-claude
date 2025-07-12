'use client'

import React, { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { MessageCircle, Users, Plus, Search, Hash, Lock, UserPlus, LogOut, Settings, Menu } from 'lucide-react'
import Layout from '@/components/layout/Layout'
import { User, ChatRoom } from '@/types'
import { useToast } from '@/components/common/ToastContainer'

export default function Home() {
  const router = useRouter()
  const { showError, showSuccess } = useToast()
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [user, setUser] = useState<User | undefined>()
  const [rooms, setRooms] = useState<ChatRoom[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // 認証状態をチェック
    const token = localStorage.getItem('authToken')
    const userData = localStorage.getItem('userData')
    
    if (token && userData) {
      setIsAuthenticated(true)
      setUser(JSON.parse(userData))
      // TODO: 実際のAPI呼び出しでルーム一覧を取得
      loadRooms()
    } else {
      setIsAuthenticated(false)
    }
    setLoading(false)
  }, [])

  const loadRooms = async () => {
    try {
      const token = localStorage.getItem('authToken')
      if (!token) {
        showError('認証エラー', 'ログインが必要です')
        return
      }

      const response = await fetch('http://localhost:8080/api/rooms/my', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      if (!response.ok) {
        throw new Error(`Failed to load rooms: ${response.status}`)
      }

      const roomsData = await response.json()
      setRooms(roomsData)
    } catch (error) {
      console.error('Failed to load rooms:', error)
      showError('ルーム読み込みエラー', 'ルーム一覧の取得に失敗しました')
    }
  }

  const handleRoomSelect = (roomId: string) => {
    router.push(`/chat?room=${roomId}`)
  }

  const handleCreateRoom = () => {
    // TODO: ルーム作成モーダルを表示
    console.log('Create room')
  }

  const handleJoinRoom = (roomId: string) => {
    // TODO: ルーム参加処理
    console.log('Join room:', roomId)
  }

  const handleLogout = () => {
    localStorage.removeItem('authToken')
    localStorage.removeItem('userData')
    setIsAuthenticated(false)
    setUser(undefined)
    router.push('/auth/signin')
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500 mx-auto mb-4"></div>
          <p className="text-gray-600">読み込み中...</p>
        </div>
      </div>
    )
  }

  // 認証済みユーザー向けダッシュボード
  if (isAuthenticated) {
    return (
      <Layout
        user={user}
        rooms={rooms}
        onRoomSelect={handleRoomSelect}
        onCreateRoom={handleCreateRoom}
        onJoinRoom={handleJoinRoom}
        onLogout={handleLogout}
      >
        <div className="flex-1 flex flex-col bg-gray-50">
          {/* ウェルカムメッセージ */}
          <div className="p-6 bg-white border-b border-gray-200">
            <h1 className="text-2xl font-bold text-gray-900 mb-2">
              おかえりなさい、{user?.name}さん！
            </h1>
            <p className="text-gray-600">
              チャットルームを選択して会話を始めましょう。
            </p>
          </div>

          {/* メインコンテンツ */}
          <div className="flex-1 p-6">
            <div className="max-w-4xl mx-auto">
              {/* 最近のアクティビティ */}
              <div className="mb-8">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">最近のアクティビティ</h2>
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
                  {rooms.slice(0, 3).map((room) => (
                    <div
                      key={room.id}
                      className="flex items-center justify-between py-3 border-b border-gray-100 last:border-b-0"
                    >
                      <div className="flex items-center space-x-3">
                        <div className="w-10 h-10 bg-primary-100 rounded-lg flex items-center justify-center">
                          {room.isPrivate ? (
                            <Lock size={20} className="text-primary-600" />
                          ) : (
                            <Hash size={20} className="text-primary-600" />
                          )}
                        </div>
                        <div>
                          <h3 className="font-medium text-gray-900">{room.name}</h3>
                          <p className="text-sm text-gray-500">
                            {room.lastMessage?.content || '新しいメッセージはありません'}
                          </p>
                        </div>
                      </div>
                      <button
                        onClick={() => handleRoomSelect(room.id)}
                        className="px-4 py-2 text-sm text-primary-600 hover:bg-primary-50 rounded-md transition-colors"
                      >
                        参加
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              {/* クイックアクション */}
              <div className="mb-8">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">クイックアクション</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <button
                    onClick={handleCreateRoom}
                    className="flex items-center space-x-3 p-4 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                  >
                    <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                      <Plus size={20} className="text-green-600" />
                    </div>
                    <div className="text-left">
                      <h3 className="font-medium text-gray-900">新しいルームを作成</h3>
                      <p className="text-sm text-gray-500">プライベートまたはパブリックルーム</p>
                    </div>
                  </button>
                  
                  <button className="flex items-center space-x-3 p-4 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
                    <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                      <Search size={20} className="text-blue-600" />
                    </div>
                    <div className="text-left">
                      <h3 className="font-medium text-gray-900">ルームを検索</h3>
                      <p className="text-sm text-gray-500">パブリックルームを見つける</p>
                    </div>
                  </button>
                </div>
              </div>

              {/* 統計情報 */}
              <div>
                <h2 className="text-lg font-semibold text-gray-900 mb-4">統計情報</h2>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="bg-white p-4 rounded-lg border border-gray-200">
                    <div className="text-2xl font-bold text-primary-600 mb-1">
                      {rooms.filter(r => r.memberCount).length}
                    </div>
                    <div className="text-sm text-gray-500">参加中のルーム</div>
                  </div>
                  
                  <div className="bg-white p-4 rounded-lg border border-gray-200">
                    <div className="text-2xl font-bold text-green-600 mb-1">
                      {rooms.reduce((total, room) => total + (room.memberCount || 0), 0)}
                    </div>
                    <div className="text-sm text-gray-500">総メンバー数</div>
                  </div>
                  
                  <div className="bg-white p-4 rounded-lg border border-gray-200">
                    <div className="text-2xl font-bold text-blue-600 mb-1">
                      {rooms.filter(r => r.lastMessage).length}
                    </div>
                    <div className="text-sm text-gray-500">アクティブなルーム</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Layout>
    )
  }

  // 未認証ユーザー向けランディングページ（簡略版）
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      {/* ヘッダー */}
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center">
              <MessageCircle className="h-8 w-8 text-primary-500 mr-3" />
              <h1 className="text-xl font-bold text-gray-900">チャットアプリ</h1>
            </div>
            <div className="flex items-center space-x-4">
              <Link
                href="/auth/signin"
                className="text-gray-600 hover:text-gray-900 font-medium"
              >
                ログイン
              </Link>
              <Link
                href="/auth/signup"
                className="bg-primary-500 text-white px-4 py-2 rounded-lg hover:bg-primary-600 transition-colors"
              >
                新規登録
              </Link>
            </div>
          </div>
        </div>
      </header>

      {/* メインコンテンツ */}
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="text-center mb-12">
          <h1 className="text-4xl sm:text-5xl font-bold text-gray-900 mb-6">
            リアルタイム
            <span className="text-primary-500 block">チャットアプリ</span>
          </h1>
          <p className="text-xl text-gray-600 mb-8 max-w-2xl mx-auto">
            友達や同僚とリアルタイムでチャットしよう。
            画像共有、絵文字、プライベートルームなど充実の機能。
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              href="/auth/signup"
              className="bg-primary-500 text-white px-8 py-3 rounded-lg text-lg font-medium hover:bg-primary-600 transition-colors"
            >
              今すぐ始める
            </Link>
            <Link
              href="/auth/signin"
              className="bg-white text-primary-500 px-8 py-3 rounded-lg text-lg font-medium border-2 border-primary-500 hover:bg-primary-50 transition-colors"
            >
              ログイン
            </Link>
          </div>
        </div>

        {/* 機能紹介 */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="text-center">
            <div className="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <MessageCircle className="h-8 w-8 text-primary-600" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">リアルタイム</h3>
            <p className="text-gray-600">瞬時にメッセージが届く</p>
          </div>
          
          <div className="text-center">
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <Users className="h-8 w-8 text-green-600" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">ルーム管理</h3>
            <p className="text-gray-600">用途に応じたルーム作成</p>
          </div>
          
          <div className="text-center">
            <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <Lock className="h-8 w-8 text-blue-600" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">セキュア</h3>
            <p className="text-gray-600">安全な認証とプライバシー</p>
          </div>
        </div>
      </main>
    </div>
  )
}