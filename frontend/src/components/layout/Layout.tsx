import React, { useState, useEffect } from 'react'
import Header from './Header'
import Sidebar from './Sidebar'
import { User, ChatRoom } from '@/types'

interface LayoutProps {
  user?: User
  rooms: ChatRoom[]
  activeRoomId?: string
  onRoomSelect: (roomId: string) => void
  onCreateRoom: () => void
  onJoinRoom: (roomId: string) => void
  onLogout: () => void
  children: React.ReactNode
}

const Layout: React.FC<LayoutProps> = ({
  user,
  rooms,
  activeRoomId,
  onRoomSelect,
  onCreateRoom,
  onJoinRoom,
  onLogout,
  children,
}) => {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [isMobile, setIsMobile] = useState(false)

  // レスポンシブ対応
  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 1024)
      if (window.innerWidth >= 1024) {
        setSidebarOpen(false) // デスクトップではサイドバーは常に表示
      }
    }

    checkMobile()
    window.addEventListener('resize', checkMobile)
    return () => window.removeEventListener('resize', checkMobile)
  }, [])

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen)
  }

  const closeSidebar = () => {
    setSidebarOpen(false)
  }

  return (
    <div className="h-screen flex flex-col bg-gray-50">
      {/* ヘッダー */}
      <Header
        user={user}
        onLogout={onLogout}
        onToggleSidebar={toggleSidebar}
        isMobile={isMobile}
      />

      {/* メインコンテンツエリア */}
      <div className="flex-1 flex overflow-hidden">
        {/* サイドバー */}
        <Sidebar
          rooms={rooms}
          activeRoomId={activeRoomId}
          onRoomSelect={onRoomSelect}
          onCreateRoom={onCreateRoom}
          onJoinRoom={onJoinRoom}
          isOpen={sidebarOpen}
          onClose={closeSidebar}
          isMobile={isMobile}
        />

        {/* メインコンテンツ */}
        <main className={`flex-1 flex flex-col overflow-hidden ${!isMobile ? 'ml-0' : ''}`}>
          {children}
        </main>
      </div>
    </div>
  )
}

export default Layout