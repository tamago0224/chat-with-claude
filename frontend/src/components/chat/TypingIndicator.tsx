import React from 'react'

interface TypingIndicatorProps {
  userName: string
}

const TypingIndicator: React.FC<TypingIndicatorProps> = ({ userName }) => {
  return (
    <div className="flex items-center space-x-2 px-4 py-2">
      <div className="flex-shrink-0">
        <div className="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center">
          <span className="text-xs text-gray-600">{userName.charAt(0)}</span>
        </div>
      </div>
      
      <div className="flex flex-col">
        <span className="text-xs text-gray-500 mb-1">{userName}</span>
        <div className="bg-gray-200 rounded-2xl rounded-bl-sm px-4 py-2 flex items-center space-x-1">
          <span className="text-sm text-gray-600">入力中</span>
          <div className="flex space-x-1">
            <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }}></div>
            <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }}></div>
            <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }}></div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default TypingIndicator