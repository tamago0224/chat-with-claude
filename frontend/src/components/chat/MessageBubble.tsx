import React from 'react'
import { format } from 'date-fns'
import { ja } from 'date-fns/locale'
import { User } from 'lucide-react'
import { Message } from '@/types'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

interface MessageBubbleProps {
  message: Message
  isOwn: boolean
  showAvatar?: boolean
  showTime?: boolean
}

const MessageBubble: React.FC<MessageBubbleProps> = ({
  message,
  isOwn,
  showAvatar = true,
  showTime = true,
}) => {
  const formatTime = (dateString: string) => {
    const date = new Date(dateString)
    const now = new Date()
    const diffInHours = (now.getTime() - date.getTime()) / (1000 * 60 * 60)

    if (diffInHours < 24) {
      return format(date, 'HH:mm', { locale: ja })
    } else if (diffInHours < 168) { // 1週間以内
      return format(date, 'M/d HH:mm', { locale: ja })
    } else {
      return format(date, 'yyyy/M/d HH:mm', { locale: ja })
    }
  }

  const renderMessageContent = () => {
    switch (message.messageType) {
      case 'IMAGE':
        const imageUrl = message.imageUrl?.startsWith('http') 
          ? message.imageUrl 
          : `${API_BASE_URL}${message.imageUrl}`
        
        return (
          <div className="max-w-xs sm:max-w-sm">
            <img
              src={imageUrl}
              alt="共有画像"
              className="rounded-lg max-w-full h-auto cursor-pointer hover:opacity-90 transition-opacity"
              onClick={() => window.open(imageUrl, '_blank')}
              onError={(e) => {
                console.error('画像の読み込みに失敗しました:', imageUrl)
                e.currentTarget.src = '/placeholder-image.png'
              }}
            />
            {message.content && (
              <p className="mt-2 text-sm">{message.content}</p>
            )}
          </div>
        )
      case 'EMOJI':
        return (
          <span className="text-4xl">{message.content}</span>
        )
      default:
        return (
          <p className="whitespace-pre-wrap break-words">{message.content}</p>
        )
    }
  }

  return (
    <div className={`flex items-end space-x-2 mb-4 ${isOwn ? 'flex-row-reverse space-x-reverse' : ''}`}>
      {/* アバター */}
      {showAvatar && !isOwn && (
        <div className="flex-shrink-0">
          {message.user.picture ? (
            <img
              src={message.user.picture}
              alt={message.user.name}
              className="w-8 h-8 rounded-full object-cover"
            />
          ) : (
            <div className="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center">
              <User size={16} className="text-gray-600" />
            </div>
          )}
        </div>
      )}

      {/* メッセージコンテンツ */}
      <div className={`flex flex-col max-w-xs sm:max-w-sm lg:max-w-md ${isOwn ? 'items-end' : 'items-start'}`}>
        {/* ユーザー名（自分以外） */}
        {!isOwn && showAvatar && (
          <span className="text-xs text-gray-500 mb-1 px-3">
            {message.user.name}
          </span>
        )}

        {/* メッセージバブル */}
        <div
          className={`px-4 py-2 rounded-2xl ${
            isOwn
              ? 'bg-own-message text-white rounded-br-sm'
              : 'bg-other-message text-gray-900 rounded-bl-sm'
          } ${message.messageType === 'EMOJI' ? 'bg-transparent px-0 py-0' : ''}`}
        >
          {renderMessageContent()}
        </div>

        {/* タイムスタンプ */}
        {showTime && (
          <span className={`text-xs text-gray-400 mt-1 px-3 ${isOwn ? 'text-right' : 'text-left'}`}>
            {formatTime(message.createdAt)}
          </span>
        )}
      </div>

      {/* 自分のメッセージの場合の空白 */}
      {isOwn && showAvatar && <div className="w-8" />}
    </div>
  )
}

export default MessageBubble