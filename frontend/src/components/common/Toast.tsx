'use client'

import React, { useEffect, useState } from 'react'
import { CheckCircle, AlertCircle, X, Info } from 'lucide-react'

export type ToastType = 'success' | 'error' | 'info' | 'warning'

export interface ToastProps {
  id: string
  type: ToastType
  title: string
  message?: string
  duration?: number
  onClose: (id: string) => void
}

const Toast: React.FC<ToastProps> = ({
  id,
  type,
  title,
  message,
  duration = 5000,
  onClose,
}) => {
  const [isVisible, setIsVisible] = useState(false)
  const [isExiting, setIsExiting] = useState(false)

  useEffect(() => {
    // アニメーション開始
    const showTimer = setTimeout(() => setIsVisible(true), 100)
    
    // 自動削除
    const autoCloseTimer = setTimeout(() => {
      handleClose()
    }, duration)

    return () => {
      clearTimeout(showTimer)
      clearTimeout(autoCloseTimer)
    }
  }, [duration])

  const handleClose = () => {
    setIsExiting(true)
    setTimeout(() => {
      onClose(id)
    }, 300) // アニメーション時間
  }

  const getToastStyles = () => {
    const baseStyles = "flex items-start space-x-3 p-4 rounded-lg shadow-lg border transition-all duration-300 transform"
    
    if (isExiting) {
      return `${baseStyles} translate-x-full opacity-0`
    }
    
    if (!isVisible) {
      return `${baseStyles} translate-x-full opacity-0`
    }

    const typeStyles = {
      success: "bg-green-50 border-green-200 text-green-800",
      error: "bg-red-50 border-red-200 text-red-800",
      warning: "bg-yellow-50 border-yellow-200 text-yellow-800",
      info: "bg-blue-50 border-blue-200 text-blue-800"
    }

    return `${baseStyles} translate-x-0 opacity-100 ${typeStyles[type]}`
  }

  const getIcon = () => {
    const iconProps = { size: 20, className: "flex-shrink-0 mt-0.5" }
    
    switch (type) {
      case 'success':
        return <CheckCircle {...iconProps} className={`${iconProps.className} text-green-600`} />
      case 'error':
        return <AlertCircle {...iconProps} className={`${iconProps.className} text-red-600`} />
      case 'warning':
        return <AlertCircle {...iconProps} className={`${iconProps.className} text-yellow-600`} />
      case 'info':
        return <Info {...iconProps} className={`${iconProps.className} text-blue-600`} />
    }
  }

  return (
    <div className={getToastStyles()}>
      {getIcon()}
      
      <div className="flex-1 min-w-0">
        <p className="font-semibold">{title}</p>
        {message && (
          <p className="text-sm opacity-90 mt-1">{message}</p>
        )}
      </div>
      
      <button
        onClick={handleClose}
        className="flex-shrink-0 p-1 hover:bg-black hover:bg-opacity-10 rounded"
      >
        <X size={16} />
      </button>
    </div>
  )
}

export default Toast