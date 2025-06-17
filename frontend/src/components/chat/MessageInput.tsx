import React, { useState, useRef, useEffect } from 'react'
import { Send, Image, Smile } from 'lucide-react'
import { SendMessageData } from '@/types'

interface MessageInputProps {
  onSendMessage: (data: SendMessageData) => void
  onTypingChange: (isTyping: boolean) => void
  disabled?: boolean
  placeholder?: string
}

const MessageInput: React.FC<MessageInputProps> = ({
  onSendMessage,
  onTypingChange,
  disabled = false,
  placeholder = 'メッセージを入力...',
}) => {
  const [message, setMessage] = useState('')
  const [isTyping, setIsTyping] = useState(false)
  const textareaRef = useRef<HTMLTextAreaElement>(null)
  const typingTimeoutRef = useRef<NodeJS.Timeout>()
  const fileInputRef = useRef<HTMLInputElement>(null)

  // テキストエリアの高さを自動調整
  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto'
      textareaRef.current.style.height = `${Math.min(textareaRef.current.scrollHeight, 120)}px`
    }
  }, [message])

  // タイピング状態の管理
  useEffect(() => {
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current)
    }

    if (message.trim() && !isTyping) {
      setIsTyping(true)
      onTypingChange(true)
    }

    typingTimeoutRef.current = setTimeout(() => {
      if (isTyping) {
        setIsTyping(false)
        onTypingChange(false)
      }
    }, 1000)

    return () => {
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current)
      }
    }
  }, [message, isTyping, onTypingChange])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    
    const trimmedMessage = message.trim()
    if (!trimmedMessage || disabled) return

    // メッセージ送信
    onSendMessage({
      content: trimmedMessage,
      type: 'TEXT',
    })

    // 入力をクリア
    setMessage('')
    
    // タイピング状態をリセット
    if (isTyping) {
      setIsTyping(false)
      onTypingChange(false)
    }
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSubmit(e)
    }
  }

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    // ファイルサイズチェック（10MB）
    if (file.size > 10 * 1024 * 1024) {
      alert('ファイルサイズは10MB以下にしてください')
      return
    }

    // ファイルタイプチェック
    if (!file.type.startsWith('image/')) {
      alert('画像ファイルのみアップロード可能です')
      return
    }

    // 画像アップロード処理（実装が必要）
    // uploadImage(file)
    console.log('Image upload:', file)
    
    // 入力をクリア
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }

  const openEmojiPicker = () => {
    // 絵文字ピッカーを開く（実装が必要）
    console.log('Open emoji picker')
  }

  const canSend = message.trim().length > 0 && !disabled

  return (
    <div className="border-t border-gray-200 bg-white px-4 py-3">
      <form onSubmit={handleSubmit} className="flex items-end space-x-2">
        {/* ファイル入力（非表示） */}
        <input
          type="file"
          ref={fileInputRef}
          onChange={handleImageUpload}
          accept="image/*"
          className="hidden"
        />

        {/* アクションボタン */}
        <div className="flex space-x-1">
          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            disabled={disabled}
            className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-full disabled:opacity-50 disabled:cursor-not-allowed"
            title="画像を添付"
          >
            <Image size={20} />
          </button>
          
          <button
            type="button"
            onClick={openEmojiPicker}
            disabled={disabled}
            className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-full disabled:opacity-50 disabled:cursor-not-allowed"
            title="絵文字"
          >
            <Smile size={20} />
          </button>
        </div>

        {/* メッセージ入力 */}
        <div className="flex-1 relative">
          <textarea
            ref={textareaRef}
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={placeholder}
            disabled={disabled}
            rows={1}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg resize-none focus:ring-2 focus:ring-primary-500 focus:border-transparent disabled:opacity-50 disabled:cursor-not-allowed"
            style={{
              minHeight: '40px',
              maxHeight: '120px',
            }}
          />
        </div>

        {/* 送信ボタン */}
        <button
          type="submit"
          disabled={!canSend}
          className={`p-2 rounded-full transition-colors ${
            canSend
              ? 'bg-primary-500 text-white hover:bg-primary-600'
              : 'bg-gray-200 text-gray-400 cursor-not-allowed'
          }`}
          title="送信 (Enter)"
        >
          <Send size={20} />
        </button>
      </form>
    </div>
  )
}

export default MessageInput