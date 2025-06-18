'use client'

import React from 'react'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { MessageCircle, Eye, EyeOff, Loader2, Check } from 'lucide-react'
import Link from 'next/link'
import { authAPI } from '@/lib/api'

export default function SignUp() {
  const router = useRouter()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: ''
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

  const validateForm = () => {
    if (!formData.name.trim()) {
      setError('名前を入力してください')
      return false
    }
    
    if (!formData.email) {
      setError('メールアドレスを入力してください')
      return false
    }
    
    if (formData.password.length < 8) {
      setError('パスワードは8文字以上で入力してください')
      return false
    }
    
    if (formData.password !== formData.confirmPassword) {
      setError('パスワードが一致しません')
      return false
    }
    
    return true
  }

  const handleSignUp = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!validateForm()) {
      return
    }

    try {
      setLoading(true)
      setError('')
      setSuccess('')
      
      const response = await authAPI.register({
        name: formData.name,
        email: formData.email,
        password: formData.password
      })

      if (response.data.token) {
        setSuccess('アカウントが正常に作成されました！')
        
        // トークンをローカルストレージに保存
        localStorage.setItem('authToken', response.data.token)
        localStorage.setItem('user', JSON.stringify(response.data.user))
        
        // 少し待ってからチャット画面にリダイレクト
        setTimeout(() => {
          router.push('/chat')
        }, 1500)
      }
    } catch (error: any) {
      console.error('Sign up error:', error)
      const errorMessage = error.response?.data?.error || 'アカウント作成に失敗しました'
      setError(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  // パスワード強度チェック
  const getPasswordStrength = () => {
    const password = formData.password
    let strength = 0
    
    if (password.length >= 8) strength++
    if (/[A-Z]/.test(password)) strength++
    if (/[a-z]/.test(password)) strength++
    if (/[0-9]/.test(password)) strength++
    if (/[^A-Za-z0-9]/.test(password)) strength++
    
    return strength
  }

  const passwordStrength = getPasswordStrength()

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-blue-100 px-4 py-8">
      <div className="max-w-md w-full space-y-8">
        {/* ロゴとタイトル */}
        <div className="text-center">
          <div className="mx-auto h-12 w-12 bg-primary-500 rounded-full flex items-center justify-center">
            <MessageCircle size={24} className="text-white" />
          </div>
          <h2 className="mt-6 text-3xl font-bold text-gray-900">
            アカウント作成
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            新しいアカウントを作成してチャットを始めましょう
          </p>
        </div>

        {/* サインアップフォーム */}
        <div className="bg-white py-8 px-6 shadow-lg rounded-lg">
          <form onSubmit={handleSignUp} className="space-y-6">
            {/* エラーメッセージ */}
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-md p-3">
                <p className="text-sm text-red-600">{error}</p>
              </div>
            )}

            {/* 成功メッセージ */}
            {success && (
              <div className="bg-green-50 border border-green-200 rounded-md p-3">
                <div className="flex items-center">
                  <Check className="h-5 w-5 text-green-400 mr-2" />
                  <p className="text-sm text-green-600">{success}</p>
                </div>
              </div>
            )}

            {/* 名前 */}
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-gray-700">
                名前
              </label>
              <input
                id="name"
                name="name"
                type="text"
                autoComplete="name"
                required
                value={formData.name}
                onChange={handleInputChange}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                placeholder="あなたの名前"
              />
            </div>

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
                  autoComplete="new-password"
                  required
                  value={formData.password}
                  onChange={handleInputChange}
                  className="block w-full px-3 py-2 pr-10 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                  placeholder="8文字以上のパスワード"
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
              
              {/* パスワード強度インジケーター */}
              {formData.password && (
                <div className="mt-2">
                  <div className="flex items-center space-x-1">
                    {[1, 2, 3, 4, 5].map((level) => (
                      <div
                        key={level}
                        className={`h-1 w-full rounded ${
                          level <= passwordStrength
                            ? passwordStrength <= 2
                              ? 'bg-red-400'
                              : passwordStrength <= 4
                              ? 'bg-yellow-400'
                              : 'bg-green-400'
                            : 'bg-gray-200'
                        }`}
                      />
                    ))}
                  </div>
                  <p className="text-xs mt-1 text-gray-500">
                    パスワード強度: {
                      passwordStrength <= 2 ? '弱い' :
                      passwordStrength <= 4 ? '普通' : '強い'
                    }
                  </p>
                </div>
              )}
            </div>

            {/* パスワード確認 */}
            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700">
                パスワード確認
              </label>
              <div className="mt-1 relative">
                <input
                  id="confirmPassword"
                  name="confirmPassword"
                  type={showConfirmPassword ? 'text' : 'password'}
                  autoComplete="new-password"
                  required
                  value={formData.confirmPassword}
                  onChange={handleInputChange}
                  className="block w-full px-3 py-2 pr-10 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                  placeholder="パスワードを再入力"
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 pr-3 flex items-center"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                >
                  {showConfirmPassword ? (
                    <EyeOff className="h-4 w-4 text-gray-400" />
                  ) : (
                    <Eye className="h-4 w-4 text-gray-400" />
                  )}
                </button>
              </div>
            </div>

            {/* アカウント作成ボタン */}
            <button
              type="submit"
              disabled={loading || success !== ''}
              className="w-full flex items-center justify-center px-4 py-3 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {loading ? (
                <Loader2 className="animate-spin h-5 w-5" />
              ) : success ? (
                <Check className="h-5 w-5" />
              ) : (
                'アカウント作成'
              )}
            </button>

            {/* ログインリンク */}
            <div className="text-center">
              <p className="text-sm text-gray-600">
                既にアカウントをお持ちの方は{' '}
                <Link href="/auth/signin" className="font-medium text-primary-600 hover:text-primary-500">
                  ログイン
                </Link>
              </p>
            </div>
          </form>

          {/* セキュリティ情報 */}
          <div className="mt-6 p-3 bg-gray-50 rounded-md">
            <p className="text-xs text-gray-500">
              アカウントを作成することで、
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