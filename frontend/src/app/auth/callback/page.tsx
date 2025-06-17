'use client'

import React, { useEffect, useState } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { Loader2, CheckCircle, XCircle } from 'lucide-react'

export default function AuthCallback() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading')
  const [message, setMessage] = useState('')

  useEffect(() => {
    const handleCallback = async () => {
      try {
        // URLパラメータからトークンとユーザーIDを取得
        const token = searchParams.get('token')
        const userId = searchParams.get('userId')
        const error = searchParams.get('error')

        if (error) {
          setStatus('error')
          setMessage(`認証エラー: ${error}`)
          return
        }

        if (!token || !userId) {
          setStatus('error')
          setMessage('認証情報が不正です')
          return
        }

        // トークンをローカルストレージに保存
        localStorage.setItem('authToken', token)
        localStorage.setItem('userId', userId)

        // バックエンドでトークンの有効性を確認
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/validate`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ token }),
        })

        if (response.ok) {
          const data = await response.json()
          if (data.valid) {
            setStatus('success')
            setMessage('ログインに成功しました')
            
            // ユーザー情報をローカルストレージに保存
            localStorage.setItem('user', JSON.stringify({
              id: data.userId,
              email: data.email,
              name: data.name,
              picture: data.picture,
            }))

            // 2秒後にチャット画面にリダイレクト
            setTimeout(() => {
              router.push('/chat')
            }, 2000)
          } else {
            setStatus('error')
            setMessage('トークンが無効です')
          }
        } else {
          setStatus('error')
          setMessage('認証の確認に失敗しました')
        }
      } catch (error) {
        console.error('Auth callback error:', error)
        setStatus('error')
        setMessage('認証処理中にエラーが発生しました')
      }
    }

    handleCallback()
  }, [searchParams, router])

  const handleRetry = () => {
    router.push('/auth/signin')
  }

  const handleGoToChat = () => {
    router.push('/chat')
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-blue-100 px-4">
      <div className="max-w-md w-full">
        <div className="bg-white py-8 px-6 shadow-lg rounded-lg text-center">
          {status === 'loading' && (
            <>
              <Loader2 size={48} className="mx-auto text-primary-500 animate-spin mb-4" />
              <h2 className="text-xl font-semibold text-gray-900 mb-2">
                認証中...
              </h2>
              <p className="text-gray-600">
                少々お待ちください
              </p>
            </>
          )}

          {status === 'success' && (
            <>
              <CheckCircle size={48} className="mx-auto text-green-500 mb-4" />
              <h2 className="text-xl font-semibold text-gray-900 mb-2">
                ログイン成功
              </h2>
              <p className="text-gray-600 mb-6">
                {message}
              </p>
              <div className="space-y-3">
                <p className="text-sm text-gray-500">
                  まもなくチャット画面に移動します...
                </p>
                <button
                  onClick={handleGoToChat}
                  className="w-full bg-primary-500 text-white py-2 px-4 rounded-md hover:bg-primary-600 transition-colors"
                >
                  今すぐチャットを開始
                </button>
              </div>
            </>
          )}

          {status === 'error' && (
            <>
              <XCircle size={48} className="mx-auto text-red-500 mb-4" />
              <h2 className="text-xl font-semibold text-gray-900 mb-2">
                認証エラー
              </h2>
              <p className="text-gray-600 mb-6">
                {message}
              </p>
              <div className="space-y-3">
                <button
                  onClick={handleRetry}
                  className="w-full bg-primary-500 text-white py-2 px-4 rounded-md hover:bg-primary-600 transition-colors"
                >
                  再度ログインする
                </button>
                <button
                  onClick={() => router.push('/')}
                  className="w-full bg-gray-200 text-gray-800 py-2 px-4 rounded-md hover:bg-gray-300 transition-colors"
                >
                  ホームに戻る
                </button>
              </div>
            </>
          )}
        </div>

        {/* デバッグ情報（開発環境のみ） */}
        {process.env.NODE_ENV === 'development' && (
          <div className="mt-4 p-3 bg-gray-100 rounded-md text-xs text-gray-600">
            <p><strong>Debug Info:</strong></p>
            <p>Token: {searchParams.get('token') ? 'Present' : 'Missing'}</p>
            <p>User ID: {searchParams.get('userId') || 'Missing'}</p>
            <p>Error: {searchParams.get('error') || 'None'}</p>
            <p>Status: {status}</p>
          </div>
        )}
      </div>
    </div>
  )
}