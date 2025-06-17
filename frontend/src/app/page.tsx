'use client'

import React, { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { MessageCircle, Users, Zap, Shield, Smartphone, Globe } from 'lucide-react'

export default function Home() {
  const router = useRouter()

  useEffect(() => {
    // 既にログインしている場合はチャット画面にリダイレクト
    const token = localStorage.getItem('authToken')
    if (token) {
      router.push('/chat')
    }
  }, [router])

  const features = [
    {
      icon: <Zap className="h-6 w-6" />,
      title: 'リアルタイム通信',
      description: 'WebSocketによる即座のメッセージ配信とタイピング表示'
    },
    {
      icon: <Users className="h-6 w-6" />,
      title: 'ルーム管理',
      description: 'パブリック・プライベートルームの作成と管理'
    },
    {
      icon: <Shield className="h-6 w-6" />,
      title: 'セキュア認証',
      description: 'Google OAuth2による安全なログイン'
    },
    {
      icon: <Smartphone className="h-6 w-6" />,
      title: 'レスポンシブ',
      description: 'モバイル・タブレット・デスクトップ対応'
    },
    {
      icon: <Globe className="h-6 w-6" />,
      title: '画像共有',
      description: '画像・絵文字の送受信をサポート'
    },
    {
      icon: <MessageCircle className="h-6 w-6" />,
      title: 'メッセージ履歴',
      description: '過去のメッセージ検索と無限スクロール'
    }
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-blue-100">
      {/* ヘッダー */}
      <header className="relative bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div className="flex items-center">
              <MessageCircle className="h-8 w-8 text-primary-500 mr-3" />
              <h1 className="text-2xl font-bold text-gray-900">チャットアプリ</h1>
            </div>
            <Link
              href="/auth/signin"
              className="bg-primary-500 text-white px-6 py-2 rounded-md hover:bg-primary-600 transition-colors"
            >
              ログイン
            </Link>
          </div>
        </div>
      </header>

      {/* メインコンテンツ */}
      <main>
        {/* ヒーローセクション */}
        <section className="relative py-20 sm:py-24 lg:py-32">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center">
              <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold text-gray-900 mb-6">
                リアルタイム
                <span className="text-primary-500 block">チャットアプリ</span>
              </h1>
              <p className="text-xl text-gray-600 mb-8 max-w-3xl mx-auto">
                WebSocketによるリアルタイムメッセージング、画像共有、ルーム管理など
                現代的なチャット機能を備えたWebアプリケーション
              </p>
              <div className="flex flex-col sm:flex-row gap-4 justify-center">
                <Link
                  href="/auth/signin"
                  className="bg-primary-500 text-white px-8 py-3 rounded-lg text-lg font-medium hover:bg-primary-600 transition-colors"
                >
                  今すぐ始める
                </Link>
                <a
                  href="#features"
                  className="bg-white text-primary-500 px-8 py-3 rounded-lg text-lg font-medium border-2 border-primary-500 hover:bg-primary-50 transition-colors"
                >
                  機能を見る
                </a>
              </div>
            </div>
          </div>
        </section>

        {/* 機能セクション */}
        <section id="features" className="py-20 bg-white">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center mb-16">
              <h2 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-4">
                主な機能
              </h2>
              <p className="text-xl text-gray-600 max-w-2xl mx-auto">
                現代的なチャットアプリケーションに必要な機能を全て搭載
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              {features.map((feature, index) => (
                <div
                  key={index}
                  className="bg-gray-50 rounded-lg p-6 hover:shadow-md transition-shadow"
                >
                  <div className="bg-primary-500 text-white w-12 h-12 rounded-lg flex items-center justify-center mb-4">
                    {feature.icon}
                  </div>
                  <h3 className="text-xl font-semibold text-gray-900 mb-2">
                    {feature.title}
                  </h3>
                  <p className="text-gray-600">
                    {feature.description}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* 技術スタックセクション */}
        <section className="py-20 bg-gray-50">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center mb-16">
              <h2 className="text-3xl sm:text-4xl font-bold text-gray-900 mb-4">
                技術スタック
              </h2>
              <p className="text-xl text-gray-600 max-w-2xl mx-auto">
                モダンで実績のある技術を組み合わせて構築
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              <div className="bg-white rounded-lg p-6 shadow-sm">
                <h3 className="text-xl font-semibold text-gray-900 mb-4">フロントエンド</h3>
                <ul className="space-y-2 text-gray-600">
                  <li>• Next.js 14 + TypeScript</li>
                  <li>• TailwindCSS</li>
                  <li>• Socket.io クライアント</li>
                  <li>• NextAuth.js</li>
                </ul>
              </div>

              <div className="bg-white rounded-lg p-6 shadow-sm">
                <h3 className="text-xl font-semibold text-gray-900 mb-4">バックエンド</h3>
                <ul className="space-y-2 text-gray-600">
                  <li>• Spring Boot 3.x + Java 17</li>
                  <li>• PostgreSQL + Valkey (Redis)</li>
                  <li>• Socket.io + gRPC</li>
                  <li>• Google OAuth2</li>
                </ul>
              </div>
            </div>
          </div>
        </section>

        {/* CTAセクション */}
        <section className="py-20 bg-primary-500">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
            <h2 className="text-3xl sm:text-4xl font-bold text-white mb-4">
              今すぐチャットを始めよう
            </h2>
            <p className="text-xl text-primary-100 mb-8 max-w-2xl mx-auto">
              Googleアカウントで簡単ログイン。すぐにリアルタイムチャットを体験できます。
            </p>
            <Link
              href="/auth/signin"
              className="bg-white text-primary-500 px-8 py-3 rounded-lg text-lg font-medium hover:bg-gray-100 transition-colors inline-block"
            >
              無料で始める
            </Link>
          </div>
        </section>
      </main>

      {/* フッター */}
      <footer className="bg-gray-900 text-white py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div>
              <div className="flex items-center mb-4">
                <MessageCircle className="h-6 w-6 text-primary-500 mr-2" />
                <span className="text-lg font-semibold">チャットアプリ</span>
              </div>
              <p className="text-gray-400">
                現代的なリアルタイムチャットアプリケーション
              </p>
            </div>
            
            <div>
              <h3 className="text-lg font-semibold mb-4">機能</h3>
              <ul className="space-y-2 text-gray-400">
                <li>リアルタイムメッセージング</li>
                <li>画像・絵文字共有</li>
                <li>ルーム管理</li>
                <li>セキュア認証</li>
              </ul>
            </div>
            
            <div>
              <h3 className="text-lg font-semibold mb-4">サポート</h3>
              <ul className="space-y-2 text-gray-400">
                <li><a href="/help" className="hover:text-white">ヘルプ</a></li>
                <li><a href="/contact" className="hover:text-white">お問い合わせ</a></li>
                <li><a href="/privacy" className="hover:text-white">プライバシー</a></li>
                <li><a href="/terms" className="hover:text-white">利用規約</a></li>
              </ul>
            </div>
          </div>
          
          <div className="border-t border-gray-800 pt-8 mt-8 text-center text-gray-400">
            <p>&copy; 2024 チャットアプリ. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  )
}