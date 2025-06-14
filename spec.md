# チャットアプリケーション 仕様書

## 1. コア機能

### 1.1 基本機能
- **リアルタイムメッセージング**: WebSocketを使用したリアルタイム通信
- **ユーザー認証・登録**: Google OpenID Connectによる認証
- **チャットルームの作成・参加**: パブリック・プライベートルーム対応
- **メッセージ履歴の保存・表示**: PostgreSQLでの永続化
- **メッセージ対応形式**: テキスト、絵文字、画像のみ

### 1.2 リアルタイム機能
- メッセージの即座配信
- ユーザーのオンライン状態表示
- タイピングインジケーター
- ルーム参加・離脱通知

## 2. 技術仕様

### 2.1 アーキテクチャ概要
```
[Next.js Frontend] 
    ↕ WebSocket (Socket.io)
    ↕ gRPC
    ↕ REST API
[Spring Boot Backend]
    ↕ 
[PostgreSQL] + [Valkey Cache]
```

### 2.2 フロントエンド
- **フレームワーク**: Next.js 14+ (SSR対応)
- **言語**: TypeScript
- **スタイリング**: TailwindCSS (レスポンシブデザイン)
- **認証**: NextAuth.js (Google OAuth)
- **リアルタイム通信**: Socket.io-client
- **状態管理**: React Hooks
- **フォーム管理**: React Hook Form

### 2.3 バックエンド
- **フレームワーク**: Spring Boot 3.x
- **言語**: Java 17
- **認証**: Spring Security + OpenID Connect
- **リアルタイム通信**: Socket.io Java実装
- **API通信**: 
  - gRPC (ユーザー管理、ルーム管理)
  - REST API (メッセージ履歴、ファイルアップロード)
- **ORM**: Spring Data JPA

### 2.4 データベース・キャッシュ
- **メインDB**: PostgreSQL 15+
- **キャッシュ**: Valkey 7+ (Redisフォーク)
  - WebSocket接続情報管理
  - オンラインユーザー状態
  - チャットルーム参加者リスト
  - 最近のメッセージキャッシュ

## 3. データベース設計

### 3.1 テーブル構成

#### Users テーブル
```sql
- id (VARCHAR, PK)
- email (VARCHAR, UNIQUE, NOT NULL)
- name (VARCHAR, NOT NULL)
- picture (VARCHAR)
- google_id (VARCHAR, UNIQUE)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

#### Chat_rooms テーブル
```sql
- id (VARCHAR, PK)
- name (VARCHAR, NOT NULL)
- description (TEXT)
- owner_id (VARCHAR, FK)
- is_private (BOOLEAN, DEFAULT false)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

#### Messages テーブル
```sql
- id (VARCHAR, PK)
- room_id (VARCHAR, FK)
- user_id (VARCHAR, FK)
- content (TEXT)
- message_type (ENUM: TEXT, IMAGE, EMOJI)
- image_url (VARCHAR)
- created_at (TIMESTAMP)
```

#### Room_members テーブル（中間テーブル）
```sql
- room_id (VARCHAR, FK)
- user_id (VARCHAR, FK)
- joined_at (TIMESTAMP)
```

## 4. API設計

### 4.1 gRPC API
**UserService**
- `GetUserProfile(userId)` → User
- `UpdateUserProfile(userId, name, picture)` → User
- `GetUsersByRoom(roomId)` → User[]

**ChatRoomService**
- `CreateRoom(name, description, ownerId, isPrivate)` → ChatRoom
- `GetRoom(roomId)` → ChatRoom
- `GetUserRooms(userId)` → ChatRoom[]
- `JoinRoom(roomId, userId)` → boolean
- `LeaveRoom(roomId, userId)` → boolean
- `GetRoomMembers(roomId)` → User[]

**MessageService**
- `GetMessageHistory(roomId, page, size, beforeTimestamp)` → Messages[]
- `SearchMessages(roomId, query, page, size)` → Messages[]

### 4.2 WebSocket Events

#### クライアント → サーバー
- `join_room` - ルーム参加
- `send_message` - メッセージ送信
- `typing` - タイピング状態

#### サーバー → クライアント
- `new_message` - 新しいメッセージ
- `user_joined` - ユーザー参加通知
- `user_left` - ユーザー離脱通知
- `user_typing` - タイピング通知
- `joined_room` - ルーム参加完了
- `error` - エラー通知

## 5. 認証・セキュリティ

### 5.1 認証フロー
1. Google OpenID Connect認証
2. バックエンドでJWTトークン発行
3. WebSocket接続時の認証検証
4. gRPC/REST通信時の認証ヘッダー検証

### 5.2 セキュリティ対策
- **HTTPS通信強制**: nginx設定
- **CORS設定**: Spring Security設定
- **CSRF対策**: Spring Security
- **XSS対策**: フロントエンドでのサニタイズ
- **画像アップロード制限**: ファイル形式・サイズ制限
- **JWT有効期限**: 24時間

## 6. 非機能要件

### 6.1 パフォーマンス
- **同時接続ユーザー数**: 100-10,000人対応
- **メッセージ送信遅延**: 1秒以内
- **メッセージ履歴読み込み**: ページネーション対応
- **キャッシュ戦略**: Valkeyによる高速アクセス

### 6.2 可用性・拡張性
- **水平スケーリング**: Valkeyでセッション共有
- **ロードバランサー**: nginx
- **DB負荷分散**: リードレプリカ活用
- **CDN対応**: 画像配信最適化

## 7. UI/UX設計

### 7.1 画面構成
- **チャット画面**: メッセージ表示・入力エリア
- **ルーム一覧**: 参加中ルーム・パブリックルーム
- **ユーザープロフィール**: プロフィール編集
- **ルーム設定**: ルーム作成・編集・メンバー管理

### 7.2 レスポンシブデザイン
- **モバイルファースト**: スマートフォン最適化
- **タッチ操作対応**: タップ・スワイプ対応
- **ブレークポイント**: sm(640px), md(768px), lg(1024px), xl(1280px)

### 7.3 ユーザビリティ
- **リアルタイムタイピング表示**
- **既読・未読状態の明確表示**
- **プッシュ通知対応**
- **絵文字ピッカー**
- **画像ドラッグ&ドロップアップロード**

## 8. 開発・運用

### 8.1 開発環境
- **コンテナ**: Docker & Docker Compose
- **バージョン管理**: Git
- **CI/CD**: GitHub Actions (推奨)
- **テスト**: JUnit (Backend), Jest (Frontend)

### 8.2 監視・ログ
- **アプリケーションログ**: Logback (Backend), Console (Frontend)
- **メトリクス**: Spring Actuator
- **ヘルスチェック**: `/actuator/health`
- **エラートラッキング**: カスタム実装

## 9. 環境変数

### 9.1 Backend (.env)
```
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
DB_HOST=postgres
DB_PORT=5432
DB_NAME=chatapp
DB_USER=chatuser
DB_PASSWORD=chatpass
REDIS_HOST=valkey
REDIS_PORT=6379
JWT_SECRET=your_jwt_secret
JWT_EXPIRATION=86400
```

### 9.2 Frontend (.env.local)
```
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=http://localhost:8080
NEXT_PUBLIC_GRPC_URL=http://localhost:9090
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=your_nextauth_secret
```

## 10. 開発ロードマップ

### Phase 1: 基本機能 (2-3週間)
- ユーザー認証（Google OAuth）
- 基本チャット機能
- PostgreSQL基本設計
- REST API実装

### Phase 2: リアルタイム機能 (2-3週間)
- WebSocket実装
- Valkey統合
- メッセージ履歴
- gRPC API実装

### Phase 3: 拡張機能 (2-3週間)
- 画像アップロード
- 未読管理
- 通知機能
- UI/UXブラッシュアップ

### Phase 4: 最適化・テスト (1-2週間)
- パフォーマンステスト
- セキュリティテスト
- ドキュメント整備
- デプロイ準備