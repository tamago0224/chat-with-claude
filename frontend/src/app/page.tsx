export default function Home() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-24">
      <div className="text-center">
        <h1 className="text-4xl font-bold mb-4">チャットアプリケーション</h1>
        <p className="text-lg text-gray-600 mb-8">
          リアルタイムチャット機能を持つWebアプリケーション
        </p>
        <div className="space-y-4">
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-xl font-semibold mb-2">技術スタック</h2>
            <ul className="list-disc list-inside text-left space-y-1">
              <li>Next.js 14 + TypeScript</li>
              <li>Spring Boot 3.x + Java 17</li>
              <li>PostgreSQL + Valkey (Redis)</li>
              <li>Socket.io + gRPC</li>
            </ul>
          </div>
          <p className="text-sm text-gray-500">
            フェーズ1: コアインフラ実装中...
          </p>
        </div>
      </div>
    </main>
  )
}