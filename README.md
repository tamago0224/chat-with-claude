# チャットアプリケーション

リアルタイムチャット機能を持つWebアプリケーションです。WebSocketによるリアルタイムメッセージング、Google OAuth認証、チャットルームの作成・管理などの機能を提供します。

## 技術スタック

- **フロントエンド**: Next.js 14+ + TypeScript + TailwindCSS
- **バックエンド**: Spring Boot 3.x + Java 17
- **データベース**: PostgreSQL 15+ + Valkey (Redis) キャッシュ
- **通信**: WebSocket (Socket.io) + gRPC + REST API
- **認証**: Google OpenID Connect
- **デプロイ**: Docker & Docker Compose

## プロジェクト構造

```
chat-with-claude/
├── frontend/               # Next.jsフロントエンド
│   ├── src/
│   │   ├── app/           # Next.js App Router
│   │   ├── components/    # UIコンポーネント
│   │   ├── hooks/         # カスタムフック
│   │   ├── lib/          # ライブラリ設定
│   │   ├── types/        # TypeScript型定義
│   │   └── utils/        # ユーティリティ関数
│   ├── package.json
│   └── Dockerfile
├── backend/               # Spring Bootバックエンド
│   ├── src/main/java/com/chatapp/
│   │   ├── config/       # 設定クラス
│   │   ├── controller/   # RESTコントローラー
│   │   ├── entity/       # JPAエンティティ
│   │   ├── repository/   # データアクセス層
│   │   ├── service/      # ビジネスロジック
│   │   ├── grpc/         # gRPCサービス
│   │   ├── security/     # セキュリティ設定
│   │   └── socket/       # WebSocketハンドラー
│   ├── pom.xml
│   └── Dockerfile
├── database/              # データベース関連
│   └── init.sql          # 初期スキーマとデータ
├── docs/                  # ドキュメント
│   └── chat_app_specification.md
├── docker-compose.yml     # Docker Compose設定
├── .env.example          # 環境変数テンプレート
└── README.md
```

## 開発環境セットアップ

### 前提条件

以下のソフトウェアがインストールされている必要があります：

- Docker & Docker Compose
- Node.js 18+
- Java 17+
- Maven 3.8+
- Git

### 1. リポジトリのクローン

```bash
git clone <repository-url>
cd chat-with-claude
```

### 2. 環境変数の設定

```bash
cp .env.example .env
```

`.env`ファイルを編集して、Google OAuth認証情報とその他の設定を行ってください：

```env
# Google OAuth設定（必須）
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# JWT設定（必須）
JWT_SECRET=your_jwt_secret_key_minimum_32_characters
NEXTAUTH_SECRET=your_nextauth_secret_key

# その他の設定は開発環境ではデフォルト値を使用可能
```

### 3. Docker Composeで開発環境を起動

```bash
# データベースとキャッシュサーバーのみ起動（開発時推奨）
docker-compose up -d postgres valkey

# または、全サービスを起動
docker-compose up -d
```

### 4. バックエンドの起動（開発時）

```bash
cd backend
mvn spring-boot:run
```

### 5. フロントエンドの起動（開発時）

```bash
cd frontend
npm install
npm run dev
```

## アクセス方法

- **フロントエンド**: http://localhost:3000
- **バックエンドAPI**: http://localhost:8080
- **gRPC**: localhost:9090
- **WebSocket**: http://localhost:8081
- **PostgreSQL**: localhost:5432
- **Valkey (Redis)**: localhost:6379

## 開発ステータス

現在、**フェーズ1（コアインフラ）**の実装が完了しています。

### 完了した項目
- ✅ プロジェクトセットアップと設定
- ✅ データベーススキーマ作成
- ✅ Docker環境構築
- ✅ 基本的な開発環境セットアップ

### 次のステップ（フェーズ2）
- [ ] Google OAuth統合
- [ ] JWT認証実装
- [ ] 基本gRPCサービス実装
- [ ] WebSocketによるリアルタイム通信
- [ ] 基本的なチャット機能

## 開発ガイドライン

詳細な仕様については、`docs/chat_app_specification.md` を参照してください。

- 技術的な決定は仕様書に記載された技術スタックに従うこと
- データベース設計、API仕様、UI仕様も仕様書に詳細に記載されています
- 開発フェーズと優先順位についても仕様書を参照してください

## 利用可能なコマンド

### バックエンド
```bash
cd backend
mvn clean compile        # コンパイル
mvn test                # テスト実行
mvn spring-boot:run     # 開発サーバー起動
mvn clean package       # JAR作成
```

### フロントエンド
```bash
cd frontend
npm install             # 依存関係インストール
npm run dev            # 開発サーバー起動
npm run build          # プロダクションビルド
npm run start          # プロダクションサーバー起動
npm run lint           # リント実行
npm run type-check     # 型チェック
```

### Docker
```bash
docker-compose up -d              # 全サービス起動
docker-compose up -d postgres valkey  # DB・キャッシュのみ起動
docker-compose down               # サービス停止
docker-compose logs -f backend    # バックエンドログ確認
```

## トラブルシューティング

### Google OAuth設定

1. [Google Cloud Console](https://console.cloud.google.com/)でプロジェクトを作成
2. OAuth 2.0 クライアントIDを作成
3. 認証済みリダイレクトURIに `http://localhost:3000/api/auth/callback/google` を追加
4. クライアントIDとシークレットを `.env` ファイルに設定

### データベース関連

- PostgreSQLコンテナが起動しない場合：`docker-compose down -v` でボリュームをリセット
- データベースを初期化する場合：`docker-compose down -v && docker-compose up -d postgres`

### ポート競合

デフォルトポートが使用中の場合は、`docker-compose.yml` のポート設定を変更してください。

## ライセンス

このプロジェクトは開発中です。