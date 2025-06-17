import React, { useState } from 'react'
import { X, Lock, Globe } from 'lucide-react'
import { CreateRoomForm } from '@/types'

interface CreateRoomModalProps {
  isOpen: boolean
  onClose: () => void
  onCreateRoom: (data: CreateRoomForm) => Promise<void>
  loading?: boolean
}

const CreateRoomModal: React.FC<CreateRoomModalProps> = ({
  isOpen,
  onClose,
  onCreateRoom,
  loading = false,
}) => {
  const [formData, setFormData] = useState<CreateRoomForm>({
    name: '',
    description: '',
    isPrivate: false,
  })
  const [errors, setErrors] = useState<Partial<CreateRoomForm>>({})

  const validateForm = (): boolean => {
    const newErrors: Partial<CreateRoomForm> = {}

    if (!formData.name.trim()) {
      newErrors.name = 'ルーム名は必須です'
    } else if (formData.name.length > 50) {
      newErrors.name = 'ルーム名は50文字以内で入力してください'
    }

    if (formData.description && formData.description.length > 200) {
      newErrors.description = '説明は200文字以内で入力してください'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!validateForm() || loading) return

    try {
      await onCreateRoom(formData)
      handleClose()
    } catch (error) {
      console.error('Failed to create room:', error)
      // エラー処理は親コンポーネントで行う
    }
  }

  const handleClose = () => {
    setFormData({ name: '', description: '', isPrivate: false })
    setErrors({})
    onClose()
  }

  const handleInputChange = (field: keyof CreateRoomForm, value: string | boolean) => {
    setFormData(prev => ({ ...prev, [field]: value }))
    
    // エラーをクリア
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: undefined }))
    }
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-full px-4 py-6">
        {/* オーバーレイ */}
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
          onClick={handleClose}
        />

        {/* モーダル */}
        <div className="relative bg-white rounded-lg shadow-xl max-w-md w-full">
          {/* ヘッダー */}
          <div className="flex items-center justify-between p-6 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">新しいルーム作成</h2>
            <button
              onClick={handleClose}
              className="text-gray-400 hover:text-gray-600"
              disabled={loading}
            >
              <X size={24} />
            </button>
          </div>

          {/* フォーム */}
          <form onSubmit={handleSubmit} className="p-6 space-y-4">
            {/* ルーム名 */}
            <div>
              <label htmlFor="roomName" className="block text-sm font-medium text-gray-700 mb-1">
                ルーム名 <span className="text-red-500">*</span>
              </label>
              <input
                id="roomName"
                type="text"
                value={formData.name}
                onChange={(e) => handleInputChange('name', e.target.value)}
                placeholder="ルーム名を入力..."
                disabled={loading}
                className={`w-full px-3 py-2 border rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent disabled:opacity-50 disabled:cursor-not-allowed ${
                  errors.name ? 'border-red-300' : 'border-gray-300'
                }`}
                maxLength={50}
              />
              {errors.name && (
                <p className="mt-1 text-sm text-red-600">{errors.name}</p>
              )}
              <p className="mt-1 text-xs text-gray-500">
                残り{50 - formData.name.length}文字
              </p>
            </div>

            {/* 説明 */}
            <div>
              <label htmlFor="roomDescription" className="block text-sm font-medium text-gray-700 mb-1">
                説明
              </label>
              <textarea
                id="roomDescription"
                value={formData.description}
                onChange={(e) => handleInputChange('description', e.target.value)}
                placeholder="任意の説明..."
                disabled={loading}
                rows={3}
                className={`w-full px-3 py-2 border rounded-md focus:ring-2 focus:ring-primary-500 focus:border-transparent disabled:opacity-50 disabled:cursor-not-allowed resize-none ${
                  errors.description ? 'border-red-300' : 'border-gray-300'
                }`}
                maxLength={200}
              />
              {errors.description && (
                <p className="mt-1 text-sm text-red-600">{errors.description}</p>
              )}
              <p className="mt-1 text-xs text-gray-500">
                残り{200 - (formData.description?.length || 0)}文字
              </p>
            </div>

            {/* プライバシー設定 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-3">
                プライバシー設定
              </label>
              <div className="space-y-3">
                <label className="flex items-center">
                  <input
                    type="radio"
                    name="privacy"
                    checked={!formData.isPrivate}
                    onChange={() => handleInputChange('isPrivate', false)}
                    disabled={loading}
                    className="text-primary-500 focus:ring-primary-500"
                  />
                  <div className="ml-3 flex items-center">
                    <Globe size={16} className="text-gray-500 mr-2" />
                    <div>
                      <span className="text-sm font-medium text-gray-900">パブリック</span>
                      <p className="text-xs text-gray-500">誰でも参加可能</p>
                    </div>
                  </div>
                </label>

                <label className="flex items-center">
                  <input
                    type="radio"
                    name="privacy"
                    checked={formData.isPrivate}
                    onChange={() => handleInputChange('isPrivate', true)}
                    disabled={loading}
                    className="text-primary-500 focus:ring-primary-500"
                  />
                  <div className="ml-3 flex items-center">
                    <Lock size={16} className="text-gray-500 mr-2" />
                    <div>
                      <span className="text-sm font-medium text-gray-900">プライベート</span>
                      <p className="text-xs text-gray-500">招待制のみ</p>
                    </div>
                  </div>
                </label>
              </div>
            </div>

            {/* ボタン */}
            <div className="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                onClick={handleClose}
                disabled={loading}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                キャンセル
              </button>
              <button
                type="submit"
                disabled={loading || !formData.name.trim()}
                className="px-4 py-2 text-sm font-medium text-white bg-primary-500 border border-transparent rounded-md hover:bg-primary-600 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? '作成中...' : '作成'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

export default CreateRoomModal