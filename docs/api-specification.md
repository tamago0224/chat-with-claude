# Chat Application API仕様書

## 概要

チャットアプリケーションのREST API仕様書です。この仕様書はOpenAPI 3.0に基づいて作成されています。

## サーバー情報

- **開発環境**: http://localhost:8080
- **本番環境**: https://api.chatapp.com

## 認証

このAPIはJWT (JSON Web Token) ベースの認証を使用します。

### 認証フロー

1. Google OAuth2でログイン (`/oauth2/authorization/google`)
2. 認証成功後、JWTトークンを取得
3. APIリクエストのAuthorizationヘッダーに `Bearer {token}` を設定

### 認証が必要なエンドポイント

- `/api/users/**` (一部除く)
- `/api/rooms/**`
- `/api/messages/**`
- `/api/upload/**`

## API エンドポイント

### 認証 (Authentication)

#### GET /auth/me
現在のユーザー情報を取得

**Headers:**
- Authorization: Bearer {token}

**Response (200):**
```json
{
  "id": "user-uuid",
  "email": "user@example.com",
  "name": "ユーザー名",
  "picture": "https://example.com/avatar.jpg"
}
```

#### POST /auth/validate
JWTトークンの有効性を検証

**Request Body:**
```json
{
  "token": "jwt-token"
}
```

**Response (200):**
```json
{
  "valid": true,
  "userId": "user-uuid",
  "email": "user@example.com",
  "name": "ユーザー名",
  "picture": "https://example.com/avatar.jpg"
}
```

#### POST /auth/logout
ログアウト（クライアント側でトークン削除）

**Response (200):**
```json
{
  "message": "Logged out successfully"
}
```

### ユーザー管理 (Users)

#### GET /api/users/{id}
ユーザー情報を取得

#### GET /api/users/room/{roomId}
指定ルームのユーザー一覧を取得

#### GET /api/users/search?q={searchTerm}
ユーザー検索

#### PUT /api/users/{id}
ユーザー情報を更新

**Request Body:**
```json
{
  "name": "新しい名前",
  "picture": "https://example.com/new-avatar.jpg"
}
```

### チャットルーム管理 (Chat Rooms)

#### GET /api/rooms/public
パブリックルーム一覧を取得（ページネーション対応）

#### GET /api/rooms/my
参加中のルーム一覧を取得

#### GET /api/rooms/{id}
ルーム詳細を取得

#### POST /api/rooms
新しいルームを作成

**Request Body:**
```json
{
  "name": "ルーム名",
  "description": "ルームの説明",
  "isPrivate": false
}
```

#### PUT /api/rooms/{id}
ルーム情報を更新（オーナーのみ）

#### POST /api/rooms/{id}/join
ルームに参加

#### POST /api/rooms/{id}/leave
ルームから退出

#### GET /api/rooms/{id}/members
ルームメンバー一覧を取得

#### GET /api/rooms/search?q={searchTerm}
パブリックルーム検索

#### DELETE /api/rooms/{id}
ルームを削除（オーナーのみ）

### メッセージ管理 (Messages)

#### GET /api/messages/room/{roomId}
ルームのメッセージ履歴を取得（ページネーション対応）

#### GET /api/messages/room/{roomId}/recent?since={timestamp}
指定時刻以降の新しいメッセージを取得

#### GET /api/messages/room/{roomId}/search?q={searchTerm}
ルーム内メッセージ検索

#### GET /api/messages/room/{roomId}/date-range?startDate={start}&endDate={end}
日時範囲でメッセージを取得

#### GET /api/messages/user/{userId}
指定ユーザーのメッセージ履歴

#### GET /api/messages/{id}
メッセージ詳細を取得

#### GET /api/messages/room/{roomId}/stats
ルームのメッセージ統計情報

#### DELETE /api/messages/{id}
メッセージを削除（作成者またはルームオーナーのみ）

### ファイルアップロード (File Upload)

#### POST /api/upload/image
画像ファイルをアップロード

**Request:**
- Content-Type: multipart/form-data
- Body: file (画像ファイル、最大10MB)

**Response (200):**
```json
{
  "url": "/api/files/filename.jpg",
  "filename": "uuid-filename.jpg",
  "originalName": "original.jpg",
  "size": "1024000",
  "contentType": "image/jpeg"
}
```

#### GET /api/files/{filename}
アップロードされたファイルを取得

#### DELETE /api/files/{filename}
ファイルを削除

## WebSocket API

### 接続

**URL:** `ws://localhost:8081/socket.io/?token={jwt-token}`

### イベント

#### クライアント → サーバー

**join_room**
```json
{
  "roomId": "room-uuid"
}
```

**leave_room**
```json
{
  "roomId": "room-uuid"
}
```

**send_message**
```json
{
  "content": "メッセージ内容",
  "type": "TEXT", // TEXT, IMAGE, EMOJI
  "imageUrl": "画像URL（オプション）"
}
```

**typing**
```json
{
  "typing": true // true: 入力中, false: 入力停止
}
```

#### サーバー → クライアント

**connected**
```json
{
  "userId": "user-uuid"
}
```

**joined_room**
```json
{
  "roomId": "room-uuid"
}
```

**new_message**
```json
{
  "id": "message-uuid",
  "roomId": "room-uuid",
  "userId": "user-uuid",
  "userName": "送信者名",
  "userPicture": "送信者アバター",
  "content": "メッセージ内容",
  "type": "TEXT",
  "imageUrl": "画像URL",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

**user_joined**
```json
{
  "userId": "user-uuid",
  "userName": "ユーザー名",
  "userPicture": "アバターURL"
}
```

**user_left**
```json
{
  "userId": "user-uuid"
}
```

**user_typing**
```json
{
  "userId": "user-uuid",
  "userName": "ユーザー名",
  "typing": true
}
```

**error**
```json
{
  "message": "エラーメッセージ"
}
```

## エラーレスポンス

### 共通エラーステータス

- **400 Bad Request**: 不正なリクエスト
- **401 Unauthorized**: 認証エラー
- **403 Forbidden**: アクセス拒否
- **404 Not Found**: リソースが見つからない
- **409 Conflict**: リソースの競合
- **500 Internal Server Error**: サーバーエラー

### エラーレスポンス形式

```json
{
  "error": "エラーメッセージ",
  "timestamp": "2024-01-01T12:00:00Z",
  "path": "/api/endpoint"
}
```

## ページネーション

一覧取得APIはページネーションをサポートしています。

### リクエストパラメータ

- `page`: ページ番号（0から開始、デフォルト: 0）
- `size`: ページサイズ（デフォルト: 20、最大: 100）
- `sort`: ソート条件（例: `createdAt,desc`）

### レスポンス形式

```json
{
  "content": [...], // データ配列
  "pageable": {
    "page": 0,
    "size": 20,
    "sort": "createdAt,desc"
  },
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false,
  "numberOfElements": 20
}
```

## レート制限

現在はレート制限は実装されていませんが、将来的に以下の制限を予定しています：

- 認証API: 5リクエスト/分
- メッセージ送信: 60リクエスト/分
- ファイルアップロード: 10リクエスト/分
- その他API: 1000リクエスト/時間

## APIドキュメント

詳細なAPIドキュメントは以下のURLで確認できます：

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## 変更履歴

| バージョン | 日付 | 変更内容 |
|-----------|------|----------|
| 1.0.0 | 2024-01-01 | 初版作成 |