import React, { useState, useEffect } from 'react'
import { X, Users, Settings, Calendar, Crown, UserMinus, UserPlus } from 'lucide-react'
import { format } from 'date-fns'
import { ja } from 'date-fns/locale'
import { ChatRoom, RoomMember, User } from '@/types'
import { userAPI, roomAPI } from '@/lib/api'

interface RoomInfoPanelProps {
  room: ChatRoom
  currentUser: User
  isOpen: boolean
  onClose: () => void
  onLeaveRoom?: () => void
  onDeleteRoom?: () => void
}

const RoomInfoPanel: React.FC<RoomInfoPanelProps> = ({
  room,
  currentUser,
  isOpen,
  onClose,
  onLeaveRoom,
  onDeleteRoom,
}) => {
  const [members, setMembers] = useState<RoomMember[]>([])
  const [loading, setLoading] = useState(false)
  const [showConfirmLeave, setShowConfirmLeave] = useState(false)
  const [showConfirmDelete, setShowConfirmDelete] = useState(false)

  const isOwner = room.owner.id === currentUser.id

  // メンバー一覧を取得
  useEffect(() => {
    if (isOpen) {
      loadMembers()
    }
  }, [isOpen, room.id])

  const loadMembers = async () => {
    setLoading(true)
    try {
      const response = await roomAPI.getRoomMembers(room.id)
      setMembers(response.data)
    } catch (error) {
      console.error('Failed to load members:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleLeaveRoom = () => {
    if (onLeaveRoom) {
      onLeaveRoom()
      setShowConfirmLeave(false)
      onClose()
    }
  }

  const handleDeleteRoom = () => {
    if (onDeleteRoom) {
      onDeleteRoom()
      setShowConfirmDelete(false)
      onClose()
    }
  }

  const formatDate = (dateString: string) => {
    return format(new Date(dateString), 'yyyy年M月d日 HH:mm', { locale: ja })
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto lg:relative lg:inset-auto">
      <div className="flex items-start justify-center min-h-full px-4 py-6 lg:px-0 lg:py-0">
        {/* オーバーレイ（モバイルのみ） */}
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 transition-opacity lg:hidden"
          onClick={onClose}
        />

        {/* パネル */}
        <div className="relative bg-white rounded-lg shadow-xl w-full max-w-md lg:max-w-xs lg:rounded-none lg:shadow-none lg:border-l lg:border-gray-200 lg:h-full">
          {/* ヘッダー */}
          <div className="flex items-center justify-between p-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">ルーム情報</h2>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600"
            >
              <X size={24} />
            </button>
          </div>

          {/* コンテンツ */}
          <div className="p-4 space-y-6">
            {/* ルーム基本情報 */}
            <div>
              <h3 className="text-base font-medium text-gray-900 mb-3">基本情報</h3>
              <div className="space-y-3">
                <div>
                  <label className="text-sm text-gray-500">ルーム名</label>
                  <p className="text-sm font-medium text-gray-900">{room.name}</p>
                </div>
                
                {room.description && (
                  <div>
                    <label className="text-sm text-gray-500">説明</label>
                    <p className="text-sm text-gray-700">{room.description}</p>
                  </div>
                )}

                <div>
                  <label className="text-sm text-gray-500">作成日時</label>
                  <p className="text-sm text-gray-700 flex items-center">
                    <Calendar size={14} className="mr-1" />
                    {formatDate(room.createdAt)}
                  </p>
                </div>

                <div>
                  <label className="text-sm text-gray-500">プライバシー</label>
                  <p className="text-sm text-gray-700">
                    {room.isPrivate ? 'プライベート' : 'パブリック'}
                  </p>
                </div>
              </div>
            </div>

            {/* オーナー情報 */}
            <div>
              <h3 className="text-base font-medium text-gray-900 mb-3">オーナー</h3>
              <div className="flex items-center space-x-3">
                {room.owner.picture ? (
                  <img
                    src={room.owner.picture}
                    alt={room.owner.name}
                    className="w-10 h-10 rounded-full object-cover"
                  />
                ) : (
                  <div className="w-10 h-10 bg-gray-300 rounded-full flex items-center justify-center">
                    <span className="text-sm font-medium text-gray-600">
                      {room.owner.name.charAt(0)}
                    </span>
                  </div>
                )}
                <div>
                  <p className="text-sm font-medium text-gray-900 flex items-center">
                    {room.owner.name}
                    <Crown size={14} className="ml-1 text-yellow-500" />
                  </p>
                  <p className="text-xs text-gray-500">{room.owner.email}</p>
                </div>
              </div>
            </div>

            {/* メンバー一覧 */}
            <div>
              <h3 className="text-base font-medium text-gray-900 mb-3 flex items-center">
                <Users size={16} className="mr-1" />
                メンバー ({members.length})
              </h3>
              
              {loading ? (
                <div className="text-center py-4">
                  <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary-500 mx-auto"></div>
                </div>
              ) : (
                <div className="space-y-2 max-h-48 overflow-y-auto">
                  {members.map((member) => (
                    <div
                      key={member.user.id}
                      className="flex items-center justify-between p-2 hover:bg-gray-50 rounded-md"
                    >
                      <div className="flex items-center space-x-3">
                        {member.user.picture ? (
                          <img
                            src={member.user.picture}
                            alt={member.user.name}
                            className="w-8 h-8 rounded-full object-cover"
                          />
                        ) : (
                          <div className="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center">
                            <span className="text-xs font-medium text-gray-600">
                              {member.user.name.charAt(0)}
                            </span>
                          </div>
                        )}
                        <div>
                          <p className="text-sm font-medium text-gray-900">
                            {member.user.name}
                            {member.user.id === room.owner.id && (
                              <Crown size={12} className="ml-1 inline text-yellow-500" />
                            )}
                          </p>
                          <p className="text-xs text-gray-500">
                            {formatDate(member.joinedAt)}に参加
                          </p>
                        </div>
                      </div>

                      {/* メンバーアクション（オーナーのみ） */}
                      {isOwner && member.user.id !== currentUser.id && (
                        <button
                          className="text-gray-400 hover:text-red-600"
                          title="メンバーを削除"
                        >
                          <UserMinus size={16} />
                        </button>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* アクション */}
            <div className="space-y-3 pt-4 border-t border-gray-200">
              {!isOwner && (
                <button
                  onClick={() => setShowConfirmLeave(true)}
                  className="w-full flex items-center justify-center px-4 py-2 text-sm font-medium text-red-600 bg-red-50 border border-red-200 rounded-md hover:bg-red-100"
                >
                  <UserMinus size={16} className="mr-2" />
                  ルームから退出
                </button>
              )}

              {isOwner && (
                <>
                  <button
                    className="w-full flex items-center justify-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                  >
                    <Settings size={16} className="mr-2" />
                    ルーム設定
                  </button>
                  
                  <button
                    onClick={() => setShowConfirmDelete(true)}
                    className="w-full flex items-center justify-center px-4 py-2 text-sm font-medium text-red-600 bg-red-50 border border-red-200 rounded-md hover:bg-red-100"
                  >
                    <X size={16} className="mr-2" />
                    ルームを削除
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* 退出確認モーダル */}
      {showConfirmLeave && (
        <div className="fixed inset-0 z-60 flex items-center justify-center px-4">
          <div className="fixed inset-0 bg-black bg-opacity-50" onClick={() => setShowConfirmLeave(false)} />
          <div className="relative bg-white rounded-lg p-6 max-w-sm w-full">
            <h3 className="text-lg font-medium text-gray-900 mb-2">ルームから退出</h3>
            <p className="text-sm text-gray-600 mb-4">
              本当に「{room.name}」から退出しますか？
            </p>
            <div className="flex space-x-3">
              <button
                onClick={() => setShowConfirmLeave(false)}
                className="flex-1 px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
              >
                キャンセル
              </button>
              <button
                onClick={handleLeaveRoom}
                className="flex-1 px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-md hover:bg-red-700"
              >
                退出
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 削除確認モーダル */}
      {showConfirmDelete && (
        <div className="fixed inset-0 z-60 flex items-center justify-center px-4">
          <div className="fixed inset-0 bg-black bg-opacity-50" onClick={() => setShowConfirmDelete(false)} />
          <div className="relative bg-white rounded-lg p-6 max-w-sm w-full">
            <h3 className="text-lg font-medium text-gray-900 mb-2">ルームを削除</h3>
            <p className="text-sm text-gray-600 mb-4">
              本当に「{room.name}」を削除しますか？この操作は取り消せません。
            </p>
            <div className="flex space-x-3">
              <button
                onClick={() => setShowConfirmDelete(false)}
                className="flex-1 px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
              >
                キャンセル
              </button>
              <button
                onClick={handleDeleteRoom}
                className="flex-1 px-4 py-2 text-sm font-medium text-white bg-red-600 rounded-md hover:bg-red-700"
              >
                削除
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default RoomInfoPanel