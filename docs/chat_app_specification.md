# チャットアプリケーション - 完全仕様書

## 目次
1. [概要](#1-概要)
2. [コア機能](#2-コア機能)
3. [技術アーキテクチャ](#3-技術アーキテクチャ)
4. [データベース設計](#4-データベース設計)
5. [API仕様](#5-api仕様)
6. [画面・UI仕様](#6-画面ui仕様)
7. [セキュリティ・認証](#7-セキュリティ認証)
8. [非機能要件](#8-非機能要件)
9. [開発・デプロイ](#9-開発デプロイ)
10. [プロジェクト工程](#10-プロジェクト工程)

---

## 1. 概要

### 1.1 プロジェクト概要
リアルタイムチャット機能を持つWebアプリケーション。テキスト、絵文字、画像の送受信に対応し、メール・パスワード認証とルームベースの会話をサポートする。

### 1.2 主要機能
- WebSocketによるリアルタイムメッセージング
- メール・パスワード認証
- チャットルームの作成・管理
- メッセージ履歴とページネーション
- 画像共有機能
- オンライン状態・タイピング表示
- レスポンシブデザイン（モバイルファースト）

### 1.3 技術スタック
- **フロントエンド**: Next.js 14+ + TypeScript + TailwindCSS
- **バックエンド**: Spring Boot 3.x + Java 17
- **データベース**: PostgreSQL 15+ + Valkey (Redis) キャッシュ
- **通信**: WebSocket (Socket.io) + gRPC + REST API
- **認証**: JWT + メール・パスワード
- **デプロイ**: Docker & Docker Compose

---

## 2. コア機能

### 2.1 リアルタイムメッセージング
- **WebSocket通信**: Socket.ioによる即座のメッセージ配信
- **メッセージ種別**: テキスト、絵文字、画像メッセージ
- **タイピング表示**: リアルタイム「入力中」通知
- **オンライン状態**: ライブユーザープレゼンス追跡
- **メッセージ履歴**: 永続化保存と無限スクロール読み込み

### 2.2 ユーザー認証・管理
- **メール・パスワード認証**: セキュアなアカウント作成とログイン
- **JWTトークン**: セキュアなセッション管理
- **アカウント管理**: 新規登録、ログイン、パスワードリセット
- **ユーザープロフィール**: 表示名とプロフィール画像のカスタマイズ
- **プライバシー設定**: オンライン状態の表示制御

### 2.3 チャットルーム機能
- **ルーム作成**: パブリック・プライベートルーム対応
- **ルーム管理**: オーナーベースの権限と設定
- **メンバー管理**: 参加・離脱機能と通知
- **ルーム検索**: パブリックルームの閲覧・検索

### 2.4 メディアサポート
- **画像共有**: アップロードとサムネイル表示
- **絵文字対応**: Unicode絵文字レンダリングとピッカー
- **ファイル検証**: セキュリティのためのサイズ・形式制限

---

## 3. 技術アーキテクチャ

### 3.1 システム構成
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Next.js       │    │  Spring Boot    │    │  PostgreSQL     │
│   フロントエンド  │◄──►│   バックエンド   │◄──►│   データベース   │
│                 │    │                 │    │                 │
│ • TypeScript    │    │ • Java 17       │    │ • Users         │
│ • TailwindCSS   │    │ • Spring Boot   │    │ • Chat Rooms    │
│ • Socket.io     │    │ • Socket.io     │    │ • Messages      │
│ • JWT認証       │    │ • gRPC          │    │ • Room Members  │
└─────────────────┘    │ • Spring Data   │    └─────────────────┘
                       │ • Spring Security│    ┌─────────────────┐
                       └─────────────────┘    │     Valkey      │
                                              │    (Redis)      │
                                              │                 │
                                              │ • セッション     │
                                              │ • オンライン状態 │
                                              │ • メッセージ     │
                                              │   キャッシュ     │
                                              └─────────────────┘
```

### 3.2 通信プロトコル
- **WebSocket**: リアルタイムメッセージングと通知
- **gRPC**: ユーザー管理、ルーム操作
- **REST API**: メッセージ履歴、ファイルアップロード
- **HTTPS**: 全ての外部通信

### 3.3 フロントエンド構成
```typescript
// プロジェクト構造
src/
├── components/
│   ├── chat/           // チャット関連コンポーネント
│   ├── rooms/          // ルーム管理コンポーネント
│   ├── auth/           // 認証関連コンポーネント (サインイン・サインアップ)
│   └── common/         // 共通コンポーネント
├── pages/              // Next.jsページ
├── hooks/              // カスタムフック
├── lib/
│   ├── auth.ts         // JWT認証設定
│   ├── websocket.ts    // WebSocket管理
│   └── grpc-client.ts  // gRPCクライアント
├── types/              // TypeScript型定義
└── utils/              // ユーティリティ関数
```

### 3.4 バックエンド構成
```java
// パッケージ構造
com.chatapp/
├── config/             // 設定クラス
├── controller/         // RESTコントローラー
├── entity/            // JPA エンティティ
├── repository/        // データアクセス層
├── service/           // ビジネスロジック
├── grpc/              // gRPC サービス
├── security/          // セキュリティ設定
└── socket/            // WebSocket ハンドラー
```

---

## 4. データベース設計

### 4.1 ER図
```
Users                    ChatRooms
┌─────────────────┐     ┌─────────────────┐
│ id (PK)         │     │ id (PK)         │
│ email (UNIQUE)  │     │ name            │
│ name            │     │ description     │
│ picture         │     │ owner_id (FK)   │
│ password_hash   │     │ is_private      │
│ created_at      │     │ created_at      │
│ updated_at      │     │ updated_at      │
└─────────────────┘     └─────────────────┘
         │                        │
         │                        │
         └──────┬─────────────────┘
                │
         ┌─────────────────┐
         │  RoomMembers    │
         │ ┌─────────────┐ │
         │ │ room_id(FK) │ │
         │ │ user_id(FK) │ │
         │ │ joined_at   │ │
         │ └─────────────┘ │
         └─────────────────┘
                │
         ┌─────────────────┐
         │    Messages     │
         │ ┌─────────────┐ │
         │ │ id (PK)     │ │
         │ │ room_id(FK) │ │
         │ │ user_id(FK) │ │
         │ │ content     │ │
         │ │ type        │ │
         │ │ image_url   │ │
         │ │ created_at  │ │
         │ └─────────────┘ │
         └─────────────────┘
```

### 4.2 テーブル定義

#### Usersテーブル
```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    picture VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### Chat_roomsテーブル
```sql
CREATE TABLE chat_rooms (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id VARCHAR(36) NOT NULL,
    is_private BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);
```

#### Messagesテーブル
```sql
CREATE TABLE messages (
    id VARCHAR(36) PRIMARY KEY,
    room_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    content TEXT,
    message_type ENUM('TEXT', 'IMAGE', 'EMOJI') DEFAULT 'TEXT',
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES chat_rooms(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### Room_membersテーブル
```sql
CREATE TABLE room_members (
    room_id VARCHAR(36),
    user_id VARCHAR(36),
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (room_id, user_id),
    FOREIGN KEY (room_id) REFERENCES chat_rooms(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 4.3 インデックス
```sql
-- パフォーマンス向上のためのインデックス
CREATE INDEX idx_messages_room_created ON messages(room_id, created_at DESC);
CREATE INDEX idx_messages_user ON messages(user_id);
CREATE INDEX idx_room_members_user ON room_members(user_id);
CREATE INDEX idx_chat_rooms_owner ON chat_rooms(owner_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);
```

---

## 5. API仕様

### 5.1 gRPCサービス

#### UserService
```protobuf
service UserService {
  rpc GetUserProfile(GetUserProfileRequest) returns (UserProfileResponse);
  rpc UpdateUserProfile(UpdateUserProfileRequest) returns (UserProfileResponse);
  rpc GetUsersByRoom(GetUsersByRoomRequest) returns (UsersResponse);
}

message User {
  string id = 1;
  string email = 2;
  string name = 3;
  string picture = 4;
  int64 created_at = 5;
  bool is_online = 6;
}
```

#### ChatRoomService
```protobuf
service ChatRoomService {
  rpc CreateRoom(CreateRoomRequest) returns (ChatRoomResponse);
  rpc GetRoom(GetRoomRequest) returns (ChatRoomResponse);
  rpc GetUserRooms(GetUserRoomsRequest) returns (ChatRoomsResponse);
  rpc JoinRoom(JoinRoomRequest) returns (JoinRoomResponse);
  rpc LeaveRoom(LeaveRoomRequest) returns (LeaveRoomResponse);
  rpc GetRoomMembers(GetRoomMembersRequest) returns (UsersResponse);
}
```

#### MessageService
```protobuf
service MessageService {
  rpc GetMessageHistory(GetMessageHistoryRequest) returns (MessageHistoryResponse);
  rpc SearchMessages(SearchMessagesRequest) returns (MessageHistoryResponse);
}

message Message {
  string id = 1;
  string room_id = 2;
  string user_id = 3;
  string user_name = 4;
  string user_picture = 5;
  string content = 6;
  MessageType type = 7;
  int64 timestamp = 8;
  string image_url = 9;
}
```

### 5.2 WebSocketイベント

#### クライアント → サーバー
```typescript
interface ClientToServerEvents {
  join_room: (data: { roomId: string }) => void;
  send_message: (data: SendMessageData) => void;
  typing: (data: { typing: boolean }) => void;
}

interface SendMessageData {
  content: string;
  type: 'TEXT' | 'IMAGE' | 'EMOJI';
  imageUrl?: string;
}
```

#### サーバー → クライアント
```typescript
interface ServerToClientEvents {
  new_message: (data: MessageData) => void;
  user_joined: (data: UserJoinedData) => void;
  user_left: (userId: string) => void;
  user_typing: (data: TypingNotificationData) => void;
  joined_room: (roomId: string) => void;
  error: (message: string) => void;
}
```

### 5.3 REST APIエンドポイント

#### 認証
- `POST /api/auth/register` - 新規ユーザー登録
- `POST /api/auth/login` - ユーザーログイン
- `POST /api/auth/logout` - ログアウト
- `POST /api/auth/refresh` - JWTトークン更新
- `POST /api/auth/forgot-password` - パスワードリセット要求
- `POST /api/auth/reset-password` - パスワードリセット実行

#### ファイルアップロード
- `POST /api/upload/image` - 画像ファイルアップロード
- `GET /api/files/:fileId` - アップロードファイル取得

#### ヘルスチェック
- `GET /actuator/health` - アプリケーション健全性確認

---

## 6. 画面・UI仕様

### 6.1 画面レイアウト設計

#### デスクトップレイアウト (1280px以上)
```
┌─────────────────────────────────────────────────────────────────┐
│                        ヘッダーバー                              │
├─────────────┬───────────────────────────────────┬───────────────┤
│             │                                   │               │
│   左        │           メインチャット           │    右         │
│  サイドバー  │           エリア                  │   サイドバー   │
│             │                                   │               │
│ • ユーザー情報│ ┌─────────────────────────────┐   │ • ルーム情報   │
│ • ルーム一覧 │ │       メッセージエリア        │   │ • メンバー     │
│ • オンライン │ │                             │   │ • 設定        │
│   ユーザー   │ └─────────────────────────────┘   │               │
│             │ ┌─────────────────────────────┐   │               │
│             │ │    メッセージ入力            │   │               │
│             │ └─────────────────────────────┘   │               │
│   300px     │              ~800px               │    250px      │
└─────────────┴───────────────────────────────────┴───────────────┘
```

#### モバイルレイアウト (767px以下)
```
┌─────────────────────────────────────┐
│           ヘッダーバー               │
│  [☰]  ルーム名        [⚙] [ℹ]     │
├─────────────────────────────────────┤
│                                     │
│          メッセージエリア            │
│         (フルスクリーン)             │
│                                     │
│                                     │
│                                     │
├─────────────────────────────────────┤
│         メッセージ入力               │
│  [📷] [😊] [テキスト入力...] [送信]  │
└─────────────────────────────────────┘

ドロワーメニュー:
左: ユーザー情報、ルーム一覧、オンラインユーザー
右: ルームメンバー、ルーム設定
```

### 6.2 コンポーネント仕様

#### メッセージコンポーネント
```typescript
interface MessageProps {
  id: string;
  content: string;
  type: 'TEXT' | 'IMAGE' | 'EMOJI';
  timestamp: Date;
  user: {
    id: string;
    name: string;
    picture: string;
  };
  isOwn: boolean;
  imageUrl?: string;
}
```

**ビジュアルデザイン:**
- **自分のメッセージ**: 右寄せ、青背景 (#3B82F6)
- **他人のメッセージ**: 左寄せ、灰背景 (#F3F4F6)
- **メッセージグループ化**: 同一ユーザーの連続メッセージはまとめて表示
- **タイムスタンプ**: 相対時間表示 ("2分前", "昨日 14:30")
- **プロフィール画像**: 32px円形アバター
- **画像メッセージ**: サムネイル表示、クリックで拡大

#### メッセージ入力コンポーネント
```typescript
interface MessageInputProps {
  onSendMessage: (content: string, type: MessageType, image?: File) => void;
  onTypingChange: (isTyping: boolean) => void;
  disabled?: boolean;
}
```

**機能:**
- **自動リサイズ**: テキストエリアが最大5行まで拡張
- **絵文字ピッカー**: カテゴリ別絵文字選択
- **画像アップロード**: ドラッグ&ドロップまたはクリックでアップロード
- **キーボードショートカット**: Enter送信、Shift+Enter改行
- **タイピング検出**: デバウンスしたタイピング状態更新

#### ルーム一覧コンポーネント
```typescript
interface RoomListProps {
  rooms: ChatRoom[];
  activeRoomId?: string;
  onRoomSelect: (roomId: string) => void;
  onCreateRoom: () => void;
}
```

**ビジュアルデザイン:**
- **アクティブルーム**: ハイライト背景 (#E5E7EB)
- **未読バッジ**: 赤色円形バッジ (#EF4444)
- **最新メッセージプレビュー**: 50文字で切り詰め
- **ルームアイコン**: デフォルトまたはカスタムアバター
- **オンライン表示**: アクティブルーム用緑色ドット

### 6.3 モーダル・ポップアップ仕様

#### ルーム作成モーダル
```
┌─────────────────────────────────────┐
│  新しいルーム作成                 ✕  │
├─────────────────────────────────────┤
│                                     │
│  ルーム名 *                         │
│  ┌─────────────────────────────────┐ │
│  │ ルーム名を入力...               │ │
│  └─────────────────────────────────┘ │
│  残り50文字                         │
│                                     │
│  説明                               │
│  ┌─────────────────────────────────┐ │
│  │ 任意の説明...                   │ │
│  │                                 │ │
│  └─────────────────────────────────┘ │
│  残り200文字                        │
│                                     │
│  プライバシー設定                   │
│  ○ パブリック - 誰でも参加可能      │
│  ○ プライベート - 招待制のみ        │
│                                     │
├─────────────────────────────────────┤
│                    [キャンセル] [作成]│
└─────────────────────────────────────┘
```

#### 画像アップロードモーダル
```
┌─────────────────────────────────────┐
│  画像アップロード                 ✕  │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────────┐ │
│  │       ドラッグ&ドロップエリア     │ │
│  │              または              │ │
│  │        [ファイル選択]            │ │
│  │                                 │ │
│  │   対応形式: JPG, PNG, GIF       │ │
│  │      最大サイズ: 10MB           │ │
│  └─────────────────────────────────┘ │
│                                     │
│  プレビュー:                        │
│  ┌─────────────────────────────────┐ │
│  │        [画像プレビュー]          │ │
│  └─────────────────────────────────┘ │
│                                     │
│  [プログレスバー] 45%               │
│                                     │
├─────────────────────────────────────┤
│                [キャンセル] [アップロード]│
└─────────────────────────────────────┘
```

### 6.4 レスポンシブブレークポイント
- **モバイル**: 0px - 767px (1カラム + ドロワー)
- **タブレット**: 768px - 1023px (2カラム)
- **デスクトップ**: 1024px - 1279px (3カラム、コンパクト)
- **大型デスクトップ**: 1280px以上 (3カラム、フル)

### 6.5 カラースキーム
```css
:root {
  /* プライマリーカラー */
  --primary-50: #eff6ff;
  --primary-500: #3b82f6;
  --primary-600: #2563eb;
  
  /* グレースケール */
  --gray-50: #f9fafb;
  --gray-100: #f3f4f6;
  --gray-500: #6b7280;
  --gray-900: #111827;
  
  /* ステータスカラー */
  --success: #10b981;
  --error: #ef4444;
  --warning: #f59e0b;
  --info: #3b82f6;
  
  /* チャットカラー */
  --own-message: #3b82f6;
  --other-message: #f3f4f6;
  --online-status: #10b981;
  --offline-status: #6b7280;
}
```

### 6.6 タイポグラフィ
```css
/* フォントファミリー */
font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Hiragino Sans', 'Hiragino Kaku Gothic ProN', 'Noto Sans JP', sans-serif;

/* フォントサイズ */
--text-xs: 0.75rem;    /* 12px */
--text-sm: 0.875rem;   /* 14px */
--text-base: 1rem;     /* 16px */
--text-lg: 1.125rem;   /* 18px */
--text-xl: 1.25rem;    /* 20px */
--text-2xl: 1.5rem;    /* 24px */

/* 行の高さ */
--leading-tight: 1.25;
--leading-normal: 1.5;
--leading-relaxed: 1.625;
```

---

## 7. セキュリティ・認証

### 7.1 認証フロー

#### 新規登録フロー
```
1. ユーザーが「アカウント作成」をクリック
   ↓
2. 登録フォームでメール・パスワード・名前を入力
   ↓
3. バックエンドでメール重複チェック
   ↓
4. パスワードハッシュ化してデータベースに保存
   ↓
5. JWTトークンを生成
   ↓
6. トークンをフロントエンドに返却
   ↓
7. フロントエンドがトークンを保存してWebSocket接続を確立
```

#### ログインフロー
```
1. ユーザーが「ログイン」をクリック
   ↓
2. ログインフォームでメール・パスワードを入力
   ↓
3. バックエンドでメールの存在確認
   ↓
4. パスワードハッシュの照合
   ↓
5. JWTトークンを生成
   ↓
6. トークンをフロントエンドに返却
   ↓
7. フロントエンドがトークンを保存してWebSocket接続を確立
```

### 7.2 JWTトークン構造
```typescript
interface JWTPayload {
  sub: string;        // ユーザーID
  email: string;      // ユーザーメール
  name: string;       // ユーザー名
  picture: string;    // プロフィール画像URL
  iat: number;        // 発行時刻
  exp: number;        // 有効期限 (24時間)
}
```

### 7.3 セキュリティ対策

#### バックエンドセキュリティ
- **HTTPS強制**: 全通信の暗号化
- **CORS設定**: オリジン制限
- **JWT検証**: 保護されたルートでのトークン検証
- **入力検証**: 全入力のサニタイゼーションと検証
- **SQLインジェクション防止**: JPA経由のパラメータ化クエリ
- **ファイルアップロードセキュリティ**: タイプ・サイズ検証
- **レート制限**: API呼び出しスロットリング（将来拡張）

#### フロントエンドセキュリティ
- **XSS防止**: レンダリング前のコンテンツサニタイゼーション
- **CSRF保護**: 状態変更リクエストでのCSRFトークン
- **セキュアストレージ**: JWTトークンのhttpOnlyクッキー（推奨）
- **入力検証**: サーバー検証と併用のクライアント側検証
- **コンテンツセキュリティポリシー**: XSS防止のCSPヘッダー

#### データベースセキュリティ
- **接続暗号化**: TLS暗号化データベース接続
- **最小権限**: 必要最小限のデータベースユーザー権限
- **準備済みステートメント**: 全クエリでパラメータ化ステートメント使用
- **定期バックアップ**: 自動バックアップ戦略

### 7.4 プライバシー配慮
- **データ最小化**: 必要なユーザーデータのみ収集
- **ユーザー同意**: 明確なプライバシーポリシーとデータ使用同意
- **データ保持**: 設定可能なメッセージ保持ポリシー
- **削除権**: ユーザーデータ削除機能
- **GDPR準拠**: ヨーロッパプライバシー規則への準拠

---

## 8. 非機能要件

### 8.1 パフォーマンス要件

#### レスポンス時間目標
- **メッセージ配信**: エンドツーエンド500ms未満
- **ページ読み込み時間**: 初期読み込み3秒未満
- **API応答時間**: リクエストの95%で200ms未満
- **画像アップロード**: 10MBファイルで10秒未満
- **メッセージ履歴読み込み**: 50メッセージで1秒未満

#### スループット要件
- **同時ユーザー**: サーバーあたり1,000同時接続をサポート
- **1秒あたりメッセージ数**: ピーク時100メッセージ/秒を処理
- **データベース接続**: 最適化された接続プーリング
- **メモリ使用量**: サーバーインスタンスあたり2GB未満
- **CPU使用率**: 通常負荷で70%未満

### 8.2 スケーラビリティ要件

#### 水平スケーリング
- **ステートレスバックエンド**: Redis/Valkeyでのセッションデータ
- **ロードバランシング**: nginxリバースプロキシでラウンドロビン
- **データベーススケーリング**: クエリ配信用リードレプリカ
- **CDN統合**: 静的アセット配信最適化
- **マイクロサービス対応**: サービス分解をサポートするアーキテクチャ

#### 容量計画
- **ユーザー**: 初期登録ユーザー10,000人
- **同時セッション**: 同時ユーザー1,000人
- **データストレージ**: 初期データベース容量100GB
- **メッセージ量**: 月間100万メッセージ
- **ファイルストレージ**: アップロード画像用500GB

### 8.3 信頼性要件

#### 可用性目標
- **稼働時間**: 99.5%可用性（月間3.6時間未満のダウンタイム）
- **計画メンテナンス**: 月次2時間未満のメンテナンスウィンドウ
- **グレースフルデグラデーション**: WebSocket失敗時のポーリングフォールバック
- **エラー回復**: 切断時の自動再接続

#### データ整合性
- **メッセージ配信**: 最低1回配信保証
- **データバックアップ**: 30日保持の日次自動バックアップ
- **トランザクション整合性**: 重要な操作でのACID準拠
- **監査ログ**: 包括的な操作ログ記録

### 8.4 監視・可観測性

#### アプリケーションメトリクス
- **レスポンス時間**: P50、P95、P99レイテンシ追跡
- **エラー率**: 4xxと5xxエラー監視
- **WebSocket接続**: アクティブ接続数
- **データベースパフォーマンス**: クエリ実行時間監視
- **メモリ/CPU使用量**: リソース使用率追跡

#### ビジネスメトリクス
- **アクティブユーザー**: 日次/月次アクティブユーザー数
- **メッセージ量**: 日次/時間ごと送信メッセージ数
- **ルーム活動**: ルーム作成・参加率
- **ユーザーエンゲージメント**: セッション継続時間と頻度

#### アラート戦略
- **クリティカルアラート**: 5%超エラー率、サービスダウン
- **警告アラート**: 70%超リソース使用率
- **情報アラート**: デプロイ通知、スケジュールメンテナンス

---

## 9. 開発・デプロイ

### 9.1 開発環境セットアップ

#### 前提条件
```bash
# 必要ソフトウェア
- Docker & Docker Compose
- Node.js 18+
- Java 17+
- Maven 3.8+
- Git

# オプショナルツール
- IntelliJ IDEA / VS Code
- Postman / Insomnia
- pgAdmin / DataGrip
```

#### ローカル開発セットアップ
```bash
# 1. リポジトリクローン
git clone <repository-url>
cd chat-application

# 2. 環境設定
cp .env.example .env
# JWT認証設定で.envを編集

# 3. 開発環境開始
docker-compose up -d postgres valkey

# 4. バックエンド開始
cd backend
mvn spring-boot:run

# 5. フロントエンド開始
cd frontend
npm install
npm run dev
```

#### 環境変数
```bash
# バックエンド (.env)
JWT_SECRET=your_jwt_secret_key_256_bits
JWT_EXPIRATION=86400
JWT_REFRESH_EXPIRATION=604800
PASSWORD_SALT_ROUNDS=12
DB_HOST=localhost
DB_PORT=5432
DB_NAME=chatapp
DB_USER=chatuser
DB_PASSWORD=chatpass
REDIS_HOST=localhost
REDIS_PORT=6379

# フロントエンド (.env.local)
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=http://localhost:8080
NEXT_PUBLIC_GRPC_URL=http://localhost:9090
```

### 9.2 ビルド・デプロイ

#### Docker設定
```dockerfile
# バックエンド Dockerfile
FROM openjdk:17-jdk-alpine
COPY target/chat-backend-*.jar app.jar
EXPOSE 8080 9090
ENTRYPOINT ["java", "-jar", "/app.jar"]

# フロントエンド Dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build
EXPOSE 3000
CMD ["npm", "start"]
```

#### プロダクションデプロイ
```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/prod.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
  
  frontend:
    image: chat-frontend:latest
    environment:
      - NODE_ENV=production
  
  backend:
    image: chat-backend:latest
    environment:
      - SPRING_PROFILES_ACTIVE=production
  
  postgres:
    image: postgres:15-alpine
    volumes:
      - postgres_data:/var/lib/postgresql/data
    environment:
      - POSTGRES_DB=chatapp_prod
  
  valkey:
    image: valkey/valkey:7.2-alpine
    volumes:
      - valkey_data:/data
```

### 9.3 CI/CDパイプライン

#### GitHub Actionsワークフロー
```yaml
name: CI/CDパイプライン

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Java設定
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          
      - name: Node.js設定
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          
      - name: バックエンドテスト実行
        run: |
          cd backend
          mvn test
          
      - name: フロントエンドテスト実行
        run: |
          cd frontend
          npm ci
          npm run test
          
  build:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Dockerイメージビルド
        run: |
          docker build -t chat-backend:latest ./backend
          docker build -t chat-frontend:latest ./frontend
          
      - name: プロダクションデプロイ
        run: |
          # デプロイスクリプト
```

### 9.4 テスト戦略

#### バックエンドテスト
```java
// 単体テスト
@SpringBootTest
@AutoConfigureTestDatabase
class UserServiceTest {
    @Test
    void shouldCreateUser() {
        // テスト実装
    }
}

// 統合テスト
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ChatIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
}
```

#### フロントエンドテスト
```typescript
// コンポーネントテスト
import { render, screen } from '@testing-library/react'
import { MessageComponent } from '@/components/chat/MessageComponent'

test('メッセージ内容をレンダリング', () => {
  render(<MessageComponent {...mockProps} />)
  expect(screen.getByText('Hello World')).toBeInTheDocument()
})

// 統合テスト
test('フォーム送信でメッセージ送信', async () => {
  // テスト実装
})
```

---

## 10. プロジェクト工程

### 10.1 開発フェーズ

#### フェーズ1: コアインフラ (第1-3週)
**バックエンド基盤**
- [ ] プロジェクトセットアップと設定
- [ ] データベーススキーマ作成
- [ ] Spring Bootアプリケーション構造
- [ ] Google OAuth統合
- [ ] JWT認証実装
- [ ] 基本gRPCサービス (User, Room)

**フロントエンド基盤**
- [ ] Next.jsプロジェクトセットアップ
- [ ] 認証ページ
- [ ] 基本ルーティング構造
- [ ] TailwindCSS設定
- [ ] コンポーネントライブラリセットアップ

**インフラ**
- [ ] Docker設定
- [ ] データベースマイグレーション
- [ ] 環境設定
- [ ] ローカル開発環境セットアップ

#### フェーズ2: リアルタイム通信 (第4-6週)
**WebSocket実装**
- [ ] Socket.ioサーバー設定
- [ ] WebSocketイベントハンドラー
- [ ] 接続管理
- [ ] リアルタイムメッセージ配信
- [ ] タイピング表示
- [ ] オンライン状態追跡

**チャットインターフェース**
- [ ] メッセージ表示コンポーネント
- [ ] メッセージ入力インターフェース
- [ ] ルーム選択機能
- [ ] 基本チャット機能
- [ ] メッセージ履歴表示

#### フェーズ3: ルーム管理 (第7-9週)
**ルーム機能**
- [ ] ルーム作成・編集
- [ ] ルーム一覧表示
- [ ] メンバー管理
- [ ] 権限管理（オーナー/メンバー）
- [ ] パブリック/プライベートルーム

**ユーザー管理**
- [ ] プロフィール設定
- [ ] ユーザー検索
- [ ] オンライン状態管理
- [ ] ユーザー設定画面

#### フェーズ4: メディア・拡張機能 (第10-12週)
**画像機能**
- [ ] 画像アップロード
- [ ] 画像表示・サムネイル
- [ ] ファイル検証
- [ ] 画像圧縮・最適化

**UI/UX改善**
- [ ] 絵文字ピッカー
- [ ] レスポンシブデザイン最適化
- [ ] アニメーション・トランジション
- [ ] 通知システム
- [ ] キーボードショートカット

#### フェーズ5: 最適化・テスト (第13-15週)
**パフォーマンス最適化**
- [ ] データベースクエリ最適化
- [ ] キャッシング戦略実装
- [ ] 無限スクロール最適化
- [ ] メモリ使用量最適化

**テスト・品質保証**
- [ ] 単体テスト拡充
- [ ] 統合テスト実装
- [ ] E2Eテスト
- [ ] パフォーマンステスト
- [ ] セキュリティテスト

**デプロイ準備**
- [ ] プロダクション環境設定
- [ ] CI/CDパイプライン構築
- [ ] 監視・ログ設定
- [ ] ドキュメント整備

### 10.2 マイルストーン

#### マイルストーン1 (第3週末)
- 基本認証機能動作
- データベース構築完了
- 開発環境構築完了

#### マイルストーン2 (第6週末)
- リアルタイムチャット基本機能
- WebSocket通信確立
- 基本UI実装完了

#### マイルストーン3 (第9週末)
- ルーム管理機能完成
- ユーザー管理機能完成
- レスポンシブデザイン実装

#### マイルストーン4 (第12週末)
- 画像共有機能完成
- 全UI機能実装完了
- ベータ版リリース準備

#### マイルストーン5 (第15週末)
- 本番環境デプロイ
- パフォーマンステスト完了
- プロダクション準備完了

### 10.3 リソース配分

#### 開発チーム構成（推奨）
- **フルスタック開発者**: 2-3名
- **UI/UXデザイナー**: 1名
- **QAエンジニア**: 1名
- **DevOpsエンジニア**: 1名（兼任可）

#### 技術タスク分担
- **バックエンド**: Spring Boot、データベース、gRPC、WebSocket
- **フロントエンド**: Next.js、React、UI実装、WebSocket統合
- **インフラ**: Docker、デプロイ、監視、セキュリティ
- **QA**: テスト設計、自動テスト、品質保証

### 10.4 リスク管理

#### 技術リスク
- **WebSocket接続の安定性**: フォールバック機能実装
- **スケーラビリティ**: 早期負荷テスト実施
- **セキュリティ脆弱性**: 定期的セキュリティ監査

#### スケジュールリスク
- **機能スコープクリープ**: 明確な機能要件定義
- **技術的負債**: コードレビューと継続的リファクタリング
- **外部依存**: Google API変更への対応計画

#### 緩和策
- **MVP優先**: 最小限実用機能を最優先
- **段階的リリース**: フェーズごとのデプロイ
- **継続的テスト**: 各フェーズでの品質確保

---

## 付録

### A. 用語集
- **WebSocket**: リアルタイム双方向通信プロトコル
- **gRPC**: 高性能RPC（Remote Procedure Call）フレームワーク
- **JWT**: JSON Web Token、認証情報を安全に伝送
- **OAuth**: 第三者認証プロトコル
- **SSR**: Server-Side Rendering、サーバーサイドレンダリング

### B. 参考資料
- [Spring Boot公式ドキュメント](https://spring.io/projects/spring-boot)
- [Next.js公式ドキュメント](https://nextjs.org/docs)
- [Socket.io公式ドキュメント](https://socket.io/docs/)
- [PostgreSQL公式ドキュメント](https://www.postgresql.org/docs/)
- [TailwindCSS公式ドキュメント](https://tailwindcss.com/docs)

### C. 変更履歴
| バージョン | 日付 | 変更内容 | 作成者 |
|-----------|------|----------|--------|
| 1.0 | 2024-XX-XX | 初版作成 | 開発チーム |

---

*この仕様書は、チャットアプリケーション開発の完全ガイドとして作成されました。プロジェクトの進行に応じて、詳細の更新や追加が必要になる場合があります。*
