import React, { useEffect, useRef, useState } from 'react'
import { Loader2 } from 'lucide-react'
import MessageBubble from './MessageBubble'
import TypingIndicator from './TypingIndicator'
import { Message, TypingNotificationData, User } from '@/types'

interface MessageListProps {
  messages: Message[]
  currentUser?: User
  typingUsers: TypingNotificationData[]
  loading?: boolean
  hasMore?: boolean
  onLoadMore?: () => void
}

const MessageList: React.FC<MessageListProps> = ({
  messages,
  currentUser,
  typingUsers,
  loading = false,
  hasMore = false,
  onLoadMore,
}) => {
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const messagesContainerRef = useRef<HTMLDivElement>(null)
  const [shouldAutoScroll, setShouldAutoScroll] = useState(true)

  // 新しいメッセージが追加された時の自動スクロール
  useEffect(() => {
    if (shouldAutoScroll && messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth' })
    }
  }, [messages, shouldAutoScroll])

  // スクロール位置の監視
  const handleScroll = () => {
    if (!messagesContainerRef.current) return

    const { scrollTop, scrollHeight, clientHeight } = messagesContainerRef.current
    const isNearBottom = scrollHeight - scrollTop - clientHeight < 100

    setShouldAutoScroll(isNearBottom)

    // 上端近くでのページング読み込み
    if (scrollTop < 100 && hasMore && onLoadMore && !loading) {
      onLoadMore()
    }
  }

  // メッセージのグループ化（同じユーザーの連続メッセージ）
  const groupedMessages = messages.reduce((groups: Message[][], message, index) => {
    const prevMessage = messages[index - 1]
    const isNewGroup = !prevMessage || 
      prevMessage.user.id !== message.user.id ||
      new Date(message.createdAt).getTime() - new Date(prevMessage.createdAt).getTime() > 5 * 60 * 1000 // 5分以上間隔

    if (isNewGroup) {
      groups.push([message])
    } else {
      groups[groups.length - 1].push(message)
    }

    return groups
  }, [])

  const scrollToBottom = () => {
    setShouldAutoScroll(true)
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  return (
    <div className="flex-1 flex flex-col overflow-hidden">
      {/* メッセージリスト */}
      <div
        ref={messagesContainerRef}
        onScroll={handleScroll}
        className="flex-1 overflow-y-auto px-4 py-4 space-y-4"
      >
        {/* 読み込み中インジケータ（上部） */}
        {loading && (
          <div className="flex justify-center py-4">
            <Loader2 size={24} className="animate-spin text-gray-400" />
          </div>
        )}

        {/* メッセージがない場合 */}
        {messages.length === 0 && !loading && (
          <div className="flex items-center justify-center h-full">
            <div className="text-center text-gray-500">
              <p className="text-lg font-medium">メッセージがありません</p>
              <p className="text-sm">最初のメッセージを送信しましょう！</p>
            </div>
          </div>
        )}

        {/* グループ化されたメッセージ */}
        {groupedMessages.map((group, groupIndex) => (
          <div key={`group-${groupIndex}`} className="space-y-1">
            {group.map((message, messageIndex) => (
              <MessageBubble
                key={message.id}
                message={message}
                isOwn={message.user.id === currentUser?.id}
                showAvatar={messageIndex === group.length - 1} // グループの最後のメッセージのみアバター表示
                showTime={messageIndex === group.length - 1}    // グループの最後のメッセージのみ時刻表示
              />
            ))}
          </div>
        ))}

        {/* タイピングインジケータ */}
        {typingUsers.length > 0 && (
          <div className="space-y-2">
            {typingUsers.map((typingUser) => (
              <TypingIndicator
                key={typingUser.userId}
                userName={typingUser.userName}
              />
            ))}
          </div>
        )}

        {/* スクロール用の参照点 */}
        <div ref={messagesEndRef} />
      </div>

      {/* 下部へのスクロールボタン */}
      {!shouldAutoScroll && (
        <div className="absolute bottom-20 right-6">
          <button
            onClick={scrollToBottom}
            className="bg-primary-500 text-white p-3 rounded-full shadow-lg hover:bg-primary-600 transition-colors"
            title="最新メッセージへ"
          >
            ↓
          </button>
        </div>
      )}
    </div>
  )
}

export default MessageList