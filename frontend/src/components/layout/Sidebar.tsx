import React, { useState } from 'react'
import { Plus, Search, Hash, Lock, Users, X } from 'lucide-react'
import { ChatRoom } from '@/types'

interface SidebarProps {
  rooms: ChatRoom[]
  activeRoomId?: string
  onRoomSelect: (roomId: string) => void
  onCreateRoom: () => void
  onJoinRoom: (roomId: string) => void
  isOpen: boolean
  onClose: () => void
  isMobile: boolean
}

const Sidebar: React.FC<SidebarProps> = ({
  rooms,
  activeRoomId,
  onRoomSelect,
  onCreateRoom,
  onJoinRoom,
  isOpen,
  onClose,
  isMobile,
}) => {
  const [searchTerm, setSearchTerm] = useState('')
  const [activeTab, setActiveTab] = useState<'my' | 'public'>('my')

  const filteredRooms = rooms.filter(room =>
    room.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    room.description?.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const myRooms = filteredRooms.filter(room => 
    // 参加中のルーム（実際にはAPIから取得したデータに基づく）
    activeTab === 'my'
  )

  const publicRooms = filteredRooms.filter(room => 
    !room.isPrivate && activeTab === 'public'
  )

  const displayRooms = activeTab === 'my' ? myRooms : publicRooms

  const handleRoomClick = (room: ChatRoom) => {
    // 参加中でない場合は参加処理を実行
    if (activeTab === 'public') {
      onJoinRoom(room.id)
    } else {
      onRoomSelect(room.id)
    }
    
    if (isMobile) {
      onClose()
    }
  }

  const sidebarClasses = `
    fixed inset-y-0 left-0 z-50 w-80 bg-white border-r border-gray-200 transform transition-transform duration-300 ease-in-out
    ${isMobile ? (isOpen ? 'translate-x-0' : '-translate-x-full') : 'relative translate-x-0'}
    ${!isMobile ? 'flex' : ''}
  `

  return (
    <>
      {/* モバイル用オーバーレイ */}
      {isMobile && isOpen && (
        <div 
          className="fixed inset-0 z-40 bg-black bg-opacity-50"
          onClick={onClose}
        />
      )}

      <div className={sidebarClasses}>
        <div className="flex flex-col h-full">
          {/* ヘッダー */}
          <div className="p-4 border-b border-gray-200">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-gray-900">ルーム</h2>
              {isMobile && (
                <button
                  onClick={onClose}
                  className="p-1 text-gray-500 hover:text-gray-700"
                >
                  <X size={20} />
                </button>
              )}
            </div>

            {/* タブ切り替え */}
            <div className="flex space-x-1 mb-4">
              <button
                onClick={() => setActiveTab('my')}
                className={`flex-1 px-3 py-2 text-sm font-medium rounded-md ${
                  activeTab === 'my'
                    ? 'bg-primary-500 text-white'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                }`}
              >
                参加中
              </button>
              <button
                onClick={() => setActiveTab('public')}
                className={`flex-1 px-3 py-2 text-sm font-medium rounded-md ${
                  activeTab === 'public'
                    ? 'bg-primary-500 text-white'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                }`}
              >
                パブリック
              </button>
            </div>

            {/* 検索 */}
            <div className="relative">
              <Search size={18} className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
              <input
                type="text"
                placeholder="ルームを検索..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              />
            </div>
          </div>

          {/* ルーム一覧 */}
          <div className="flex-1 overflow-y-auto">
            <div className="p-2">
              {/* 新規作成ボタン（参加中タブのみ） */}
              {activeTab === 'my' && (
                <button
                  onClick={onCreateRoom}
                  className="w-full flex items-center space-x-3 p-3 text-left text-gray-700 hover:bg-gray-100 rounded-md mb-2"
                >
                  <div className="w-8 h-8 bg-primary-500 rounded-md flex items-center justify-center">
                    <Plus size={16} className="text-white" />
                  </div>
                  <span className="font-medium">新しいルームを作成</span>
                </button>
              )}

              {/* ルームリスト */}
              {displayRooms.map((room) => (
                <button
                  key={room.id}
                  onClick={() => handleRoomClick(room)}
                  className={`w-full flex items-center space-x-3 p-3 text-left rounded-md mb-1 ${
                    activeRoomId === room.id
                      ? 'bg-primary-50 border border-primary-200'
                      : 'hover:bg-gray-100'
                  }`}
                >
                  <div className="w-8 h-8 bg-gray-200 rounded-md flex items-center justify-center">
                    {room.isPrivate ? (
                      <Lock size={16} className="text-gray-600" />
                    ) : (
                      <Hash size={16} className="text-gray-600" />
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between">
                      <span className="font-medium text-gray-900 truncate">
                        {room.name}
                      </span>
                      {room.memberCount && (
                        <span className="text-xs text-gray-500 flex items-center">
                          <Users size={12} className="mr-1" />
                          {room.memberCount}
                        </span>
                      )}
                    </div>
                    {room.description && (
                      <p className="text-sm text-gray-500 truncate">
                        {room.description}
                      </p>
                    )}
                    {room.lastMessage && (
                      <p className="text-xs text-gray-400 truncate">
                        {room.lastMessage.content}
                      </p>
                    )}
                  </div>
                </button>
              ))}

              {displayRooms.length === 0 && (
                <div className="text-center py-8 text-gray-500">
                  {searchTerm ? 'ルームが見つかりません' : 
                   activeTab === 'my' ? 'まだ参加しているルームがありません' : 
                   'パブリックルームがありません'}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default Sidebar