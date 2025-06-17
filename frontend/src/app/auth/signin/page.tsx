'use client'

import React from 'react'
import { signIn, getSession } from 'next-auth/react'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { MessageCircle, Chrome } from 'lucide-react'

export default function SignIn() {
  const router = useRouter()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    // 既にログインしている場合はダッシュボードにリダイレクト
    const checkSession = async () => {
      const session = await getSession()
      if (session) {
        router.push('/chat')
      }
    }
    checkSession()
  }, [router])

  const handleGoogleSignIn = async () => {
    try {
      setLoading(true)
      setError('')
      
      const result = await signIn('google', {
        callbackUrl: '/chat',
        redirect: false,
      })

      if (result?.error) {
        setError('ログインに失敗しました。もう一度お試しください。')
      } else if (result?.url) {
        router.push(result.url)
      }
    } catch (error) {
      console.error('Sign in error:', error)
      setError('ログインに失敗しました。もう一度お試しください。')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-blue-100 px-4">
      <div className="max-w-md w-full space-y-8">
        {/* ロゴとタイトル */}
        <div className="text-center">
          <div className="mx-auto h-12 w-12 bg-primary-500 rounded-full flex items-center justify-center">
            <MessageCircle size={24} className="text-white" />
          </div>
          <h2 className="mt-6 text-3xl font-bold text-gray-900">
            チャットアプリへようこそ
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            Googleアカウントでログインして始めましょう
          </p>
        </div>

        {/* サインインフォーム */}
        <div className="bg-white py-8 px-6 shadow-lg rounded-lg">
          <div className="space-y-6">
            {/* エラーメッセージ */}
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-md p-3">
                <p className="text-sm text-red-600">{error}</p>
              </div>
            )}

            {/* Googleサインインボタン */}
            <button
              onClick={handleGoogleSignIn}
              disabled={loading}
              className="w-full flex items-center justify-center px-4 py-3 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {loading ? (
                <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-gray-600"></div>
              ) : (
                <>
                  <Chrome size={20} className="mr-3" />
                  Googleでログイン
                </>
              )}
            </button>

            {/* 機能説明 */}
            <div className="mt-8 space-y-4">
              <h3 className="text-sm font-medium text-gray-900">主な機能</h3>
              <ul className="space-y-2 text-sm text-gray-600">
                <li className="flex items-start">
                  <span className="flex-shrink-0 h-1.5 w-1.5 bg-primary-500 rounded-full mt-2 mr-3"></span>
                  リアルタイムメッセージング
                </li>
                <li className="flex items-start">
                  <span className="flex-shrink-0 h-1.5 w-1.5 bg-primary-500 rounded-full mt-2 mr-3"></span>
                  画像・絵文字の共有
                </li>
                <li className="flex items-start">
                  <span className="flex-shrink-0 h-1.5 w-1.5 bg-primary-500 rounded-full mt-2 mr-3"></span>
                  パブリック・プライベートルーム
                </li>
                <li className="flex items-start">
                  <span className="flex-shrink-0 h-1.5 w-1.5 bg-primary-500 rounded-full mt-2 mr-3"></span>
                  タイピング状態・オンライン表示
                </li>
              </ul>
            </div>

            {/* セキュリティ情報 */}
            <div className="mt-6 p-3 bg-gray-50 rounded-md">
              <p className="text-xs text-gray-500">
                ログインすることで、
                <a href="/privacy" className="text-primary-600 hover:text-primary-500">プライバシーポリシー</a>
                と
                <a href="/terms" className="text-primary-600 hover:text-primary-500">利用規約</a>
                に同意したものとみなされます。
              </p>
            </div>
          </div>
        </div>

        {/* フッター */}
        <div className="text-center">
          <p className="text-xs text-gray-500">
            問題が発生した場合は、
            <a href="/support" className="text-primary-600 hover:text-primary-500">
              サポート
            </a>
            までお問い合わせください。
          </p>
        </div>
      </div>
    </div>
  )
}