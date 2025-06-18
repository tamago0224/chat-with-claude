import React, { useState, useRef, useEffect } from 'react'
import { Send, Image, Smile, Loader2 } from 'lucide-react'
import { SendMessageData } from '@/types'
import { fileAPI } from '@/lib/api'
import EmojiPicker from './EmojiPicker'
import { useToast } from '@/components/common/ToastContainer'

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
  const [isUploading, setIsUploading] = useState(false)
  const [isEmojiPickerOpen, setIsEmojiPickerOpen] = useState(false)
  const textareaRef = useRef<HTMLTextAreaElement>(null)
  const typingTimeoutRef = useRef<NodeJS.Timeout>()
  const fileInputRef = useRef<HTMLInputElement>(null)
  const { showError, showSuccess } = useToast()

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

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    // ファイルサイズチェック（10MB）
    if (file.size > 10 * 1024 * 1024) {
      showError('ファイルサイズエラー', 'ファイルサイズは10MB以下にしてください')
      return
    }

    // ファイルタイプチェック
    if (!file.type.startsWith('image/')) {
      showError('ファイル形式エラー', '画像ファイルのみアップロード可能です')
      return
    }

    try {
      setIsUploading(true)
      
      // 画像アップロード
      const response = await fileAPI.uploadImage(file)
      const { url } = response.data

      // 画像メッセージを送信
      onSendMessage({
        content: file.name,
        type: 'IMAGE',
        imageUrl: url
      })

      showSuccess('画像アップロード完了', '画像が正常にアップロードされました')

    } catch (error) {
      console.error('画像アップロードエラー:', error)
      showError('アップロードエラー', '画像のアップロードに失敗しました')
    } finally {
      setIsUploading(false)
      
      // 入力をクリア
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
    }
  }

  const openEmojiPicker = () => {
    setIsEmojiPickerOpen(!isEmojiPickerOpen)
  }

  const handleEmojiSelect = (emoji: string) => {
    // カーソル位置に絵文字を挿入
    if (textareaRef.current) {
      const start = textareaRef.current.selectionStart
      const end = textareaRef.current.selectionEnd
      const newMessage = message.slice(0, start) + emoji + message.slice(end)
      setMessage(newMessage)
      
      // カーソル位置を絵文字の後に移動
      setTimeout(() => {
        if (textareaRef.current) {
          textareaRef.current.selectionStart = start + emoji.length
          textareaRef.current.selectionEnd = start + emoji.length
          textareaRef.current.focus()
        }
      }, 0)
    } else {
      setMessage(message + emoji)
    }
  }

  const canSend = message.trim().length > 0 && !disabled && !isUploading

  return (
    <div className="border-t border-gray-200 bg-white px-4 py-3 relative">
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
            disabled={disabled || isUploading}
            className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-full disabled:opacity-50 disabled:cursor-not-allowed"
            title="画像を添付"
          >
            {isUploading ? (
              <Loader2 size={20} className="animate-spin" />
            ) : (
              <Image size={20} />
            )}
          </button>
          
          <button
            type="button"
            onClick={openEmojiPicker}
            disabled={disabled}
            className={`p-2 rounded-full disabled:opacity-50 disabled:cursor-not-allowed transition-colors ${
              isEmojiPickerOpen 
                ? 'text-primary-600 bg-primary-100' 
                : 'text-gray-500 hover:text-gray-700 hover:bg-gray-100'
            }`}
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

      {/* 絵文字ピッカー */}
      <EmojiPicker
        isOpen={isEmojiPickerOpen}
        onEmojiSelect={handleEmojiSelect}
        onClose={() => setIsEmojiPickerOpen(false)}
      />
    </div>
  )
}

export default MessageInput