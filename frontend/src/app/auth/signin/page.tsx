'use client'

import React from 'react'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { MessageCircle, Eye, EyeOff, Loader2 } from 'lucide-react'
import Link from 'next/link'
import { authAPI } from '@/lib/api'

export default function SignIn() {
  const router = useRouter()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  })

  useEffect(() => {
    // 既にログインしている場合はダッシュボードにリダイレクト
    const token = localStorage.getItem('authToken')
    if (token) {
      router.push('/chat')
    }
  }, [router])

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
  }

  const handleSignIn = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!formData.email || !formData.password) {
      setError('メールアドレスとパスワードを入力してください')
      return
    }

    try {
      setLoading(true)
      setError('')
      
      const response = await authAPI.login({
        email: formData.email,
        password: formData.password
      })

      if (response.data.token) {
        // トークンをローカルストレージに保存
        localStorage.setItem('authToken', response.data.token)
        localStorage.setItem('user', JSON.stringify(response.data.user))
        
        // チャット画面にリダイレクト
        router.push('/chat')
      }
    } catch (error: any) {
      console.error('Sign in error:', error)
      const errorMessage = error.response?.data?.error || 'ログインに失敗しました'
      setError(errorMessage)
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
            ログイン
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            アカウントにログインしてチャットを始めましょう
          </p>
        </div>

        {/* サインインフォーム */}
        <div className="bg-white py-8 px-6 shadow-lg rounded-lg">
          <form onSubmit={handleSignIn} className="space-y-6">
            {/* エラーメッセージ */}
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-md p-3">
                <p className="text-sm text-red-600">{error}</p>
              </div>
            )}

            {/* メールアドレス */}
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                メールアドレス
              </label>
              <input
                id="email"
                name="email"
                type="email"
                autoComplete="email"
                required
                value={formData.email}
                onChange={handleInputChange}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                placeholder="example@email.com"
              />
            </div>

            {/* パスワード */}
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                パスワード
              </label>
              <div className="mt-1 relative">
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  required
                  value={formData.password}
                  onChange={handleInputChange}
                  className="block w-full px-3 py-2 pr-10 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                  placeholder="パスワードを入力"
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 pr-3 flex items-center"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? (
                    <EyeOff className="h-4 w-4 text-gray-400" />
                  ) : (
                    <Eye className="h-4 w-4 text-gray-400" />
                  )}
                </button>
              </div>
            </div>

            {/* ログインボタン */}
            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center px-4 py-3 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {loading ? (
                <Loader2 className="animate-spin h-5 w-5" />
              ) : (
                'ログイン'
              )}
            </button>

            {/* アカウント作成リンク */}
            <div className="text-center">
              <p className="text-sm text-gray-600">
                アカウントをお持ちでない方は{' '}
                <Link href="/auth/signup" className="font-medium text-primary-600 hover:text-primary-500">
                  新規登録
                </Link>
              </p>
            </div>
          </form>

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