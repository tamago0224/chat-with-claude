'use client'

import React from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { AlertCircle, Home, RefreshCw } from 'lucide-react'

export default function AuthError() {
  const router = useRouter()
  const searchParams = useSearchParams()
  
  const error = searchParams.get('error')
  
  const getErrorMessage = (errorCode: string | null) => {
    switch (errorCode) {
      case 'Configuration':
        return 'サーバーの設定に問題があります。管理者にお問い合わせください。'
      case 'AccessDenied':
        return 'アクセスが拒否されました。必要な権限がない可能性があります。'
      case 'Verification':
        return 'メールアドレスの確認に失敗しました。'
      case 'OAuthSignin':
        return 'OAuth認証の開始に失敗しました。'
      case 'OAuthCallback':
        return 'OAuth認証のコールバック処理に失敗しました。'
      case 'OAuthCreateAccount':
        return 'アカウントの作成に失敗しました。'
      case 'EmailCreateAccount':
        return 'メールアドレスでのアカウント作成に失敗しました。'
      case 'Callback':
        return 'コールバック処理中にエラーが発生しました。'
      case 'OAuthAccountNotLinked':
        return 'このメールアドレスは別の認証方法で既に使用されています。'
      case 'EmailSignin':
        return 'メール認証に失敗しました。'
      case 'CredentialsSignin':
        return 'ログイン情報が正しくありません。'
      case 'SessionRequired':
        return 'この操作にはログインが必要です。'
      default:
        return '認証中に予期しないエラーが発生しました。'
    }
  }

  const handleRetry = () => {
    router.push('/auth/signin')
  }

  const handleGoHome = () => {
    router.push('/')
  }

  const handleRefresh = () => {
    window.location.reload()
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-red-50 to-orange-100 px-4">
      <div className="max-w-md w-full">
        <div className="bg-white py-8 px-6 shadow-lg rounded-lg text-center">
          {/* エラーアイコン */}
          <AlertCircle size={48} className="mx-auto text-red-500 mb-4" />
          
          {/* タイトル */}
          <h2 className="text-xl font-semibold text-gray-900 mb-2">
            認証エラー
          </h2>
          
          {/* エラーメッセージ */}
          <p className="text-gray-600 mb-6">
            {getErrorMessage(error)}
          </p>

          {/* エラーコード表示 */}
          {error && (
            <div className="mb-6 p-3 bg-gray-50 rounded-md">
              <p className="text-sm text-gray-500">
                エラーコード: <code className="font-mono">{error}</code>
              </p>
            </div>
          )}

          {/* アクションボタン */}
          <div className="space-y-3">
            <button
              onClick={handleRetry}
              className="w-full flex items-center justify-center bg-primary-500 text-white py-2 px-4 rounded-md hover:bg-primary-600 transition-colors"
            >
              <RefreshCw size={16} className="mr-2" />
              再度ログインする
            </button>
            
            <button
              onClick={handleRefresh}
              className="w-full bg-gray-200 text-gray-800 py-2 px-4 rounded-md hover:bg-gray-300 transition-colors"
            >
              ページを更新
            </button>
            
            <button
              onClick={handleGoHome}
              className="w-full flex items-center justify-center bg-white text-gray-700 py-2 px-4 rounded-md border border-gray-300 hover:bg-gray-50 transition-colors"
            >
              <Home size={16} className="mr-2" />
              ホームに戻る
            </button>
          </div>

          {/* サポート情報 */}
          <div className="mt-6 p-3 bg-blue-50 rounded-md">
            <p className="text-sm text-blue-700">
              問題が解決しない場合は、
              <a 
                href="/support" 
                className="font-medium underline hover:no-underline"
              >
                サポートチーム
              </a>
              までお問い合わせください。
            </p>
          </div>
        </div>

        {/* デバッグ情報（開発環境のみ） */}
        {process.env.NODE_ENV === 'development' && (
          <div className="mt-4 p-3 bg-gray-100 rounded-md text-xs text-gray-600">
            <p><strong>Debug Info:</strong></p>
            <p>Error: {error || 'Unknown'}</p>
            <p>URL: {window.location.href}</p>
            <p>User Agent: {navigator.userAgent}</p>
          </div>
        )}
      </div>
    </div>
  )
}