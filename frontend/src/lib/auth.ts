import { NextAuthOptions } from 'next-auth'
import GoogleProvider from 'next-auth/providers/google'

export const authOptions: NextAuthOptions = {
  providers: [
    GoogleProvider({
      clientId: process.env.GOOGLE_CLIENT_ID!,
      clientSecret: process.env.GOOGLE_CLIENT_SECRET!,
    }),
  ],
  callbacks: {
    async jwt({ token, account, user }) {
      // 初回ログイン時
      if (account && user) {
        // バックエンドのOAuth2エンドポイントにリダイレクト
        token.backendToken = await getBackendToken(account.access_token!)
      }
      return token
    },
    async session({ session, token }) {
      if (token.backendToken) {
        session.backendToken = token.backendToken as string
      }
      return session
    },
  },
  pages: {
    signIn: '/auth/signin',
    error: '/auth/error',
  },
}

async function getBackendToken(googleAccessToken: string): Promise<string | null> {
  try {
    // Googleの認証をバックエンドに転送してJWTトークンを取得
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/oauth2/authorization/google`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${googleAccessToken}`,
        'Content-Type': 'application/json',
      },
    })

    if (response.ok) {
      const data = await response.json()
      return data.token
    }
  } catch (error) {
    console.error('Failed to get backend token:', error)
  }
  return null
}