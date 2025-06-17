import React from 'react'
import { User, LogOut, Settings, Menu } from 'lucide-react'
import { User as UserType } from '@/types'

interface HeaderProps {
  user?: UserType
  onLogout: () => void
  onToggleSidebar: () => void
  isMobile: boolean
}

const Header: React.FC<HeaderProps> = ({ user, onLogout, onToggleSidebar, isMobile }) => {
  return (
    <header className="bg-white border-b border-gray-200 px-4 py-3 flex items-center justify-between">
      <div className="flex items-center space-x-4">
        {isMobile && (
          <button
            onClick={onToggleSidebar}
            className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-md"
          >
            <Menu size={20} />
          </button>
        )}
        <h1 className="text-xl font-semibold text-gray-900">チャットアプリ</h1>
      </div>

      <div className="flex items-center space-x-3">
        {user && (
          <>
            <div className="flex items-center space-x-2">
              {user.picture ? (
                <img
                  src={user.picture}
                  alt={user.name}
                  className="w-8 h-8 rounded-full object-cover"
                />
              ) : (
                <div className="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center">
                  <User size={16} className="text-gray-600" />
                </div>
              )}
              <span className="text-sm font-medium text-gray-700 hidden sm:block">
                {user.name}
              </span>
            </div>

            <div className="flex items-center space-x-1">
              <button
                className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-md"
                title="設定"
              >
                <Settings size={18} />
              </button>
              
              <button
                onClick={onLogout}
                className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-md"
                title="ログアウト"
              >
                <LogOut size={18} />
              </button>
            </div>
          </>
        )}
      </div>
    </header>
  )
}

export default Header