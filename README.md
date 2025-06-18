# チャットアプリケーション

リアルタイムチャット機能を持つWebアプリケーションです。WebSocketによるリアルタイムメッセージング、メール・パスワード認証、チャットルームの作成・管理、画像共有などの機能を提供します。

## 技術スタック

- **フロントエンド**: Next.js 14+ + TypeScript + TailwindCSS
- **バックエンド**: Spring Boot 3.x + Java 17
- **データベース**: PostgreSQL 15+ + Valkey (Redis) キャッシュ
- **通信**: WebSocket (Socket.io) + gRPC + REST API
- **認証**: JWT + メール・パスワード認証
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

環境変数を設定します（.envファイルは用意されていますが、必要に応じて調整してください）：

```env
# JWT認証設定
JWT_SECRET=your_jwt_secret_key_minimum_32_characters
JWT_EXPIRATION=86400

# データベース設定（開発環境ではデフォルト値を使用可能）
DB_HOST=localhost
DB_PORT=5432
DB_NAME=chatapp
DB_USER=chatuser
DB_PASSWORD=chatpass

# Redis/Valkey設定
REDIS_HOST=localhost
REDIS_PORT=6379
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

現在、**フェーズ3（フロントエンド実装）**まで完了しています。

### 完了した項目
- ✅ プロジェクトセットアップと設定
- ✅ データベーススキーマ作成
- ✅ Docker環境構築
- ✅ JWT認証実装（メール・パスワード）
- ✅ 基本gRPCサービス実装
- ✅ WebSocketによるリアルタイム通信
- ✅ フロントエンドUI実装
- ✅ チャットルーム機能
- ✅ 画像共有機能
- ✅ レスポンシブデザイン

### 主要機能
- **認証**: メール・パスワード認証（サインイン・サインアップ）
- **リアルタイムチャット**: WebSocketによる即座のメッセージ配信
- **ルーム管理**: チャットルームの作成・参加・管理
- **画像共有**: 画像のアップロードと表示
- **レスポンシブUI**: モバイル・デスクトップ対応

## 開発ガイドライン

詳細な仕様については、`docs/chat_app_specification.md` を参照してください。

- 技術的な決定は仕様書に記載された技術スタックに従うこと
- データベース設計、API仕様、UI仕様も仕様書に詳細に記載されています
- 開発フェーズと優先順位についても仕様書を参照してください

### コード品質管理

バックエンドでは以下のツールを使用してコード品質を維持しています：

- **Google Java Format**: 統一されたコードフォーマット
- **Checkstyle**: コーディング規約チェック（カスタム設定）
- **SpotBugs**: バグパターンと潜在的な問題の検出
- **PMD**: コード品質分析とベストプラクティスチェック
- **EditorConfig**: エディタ設定の統一

開発時は定期的に `mvn validate` を実行してコード品質を確認してください。

## 利用可能なコマンド

### バックエンド
```bash
cd backend
mvn clean compile        # コンパイル
mvn test                # テスト実行
mvn spring-boot:run     # 開発サーバー起動
mvn clean package       # JAR作成

# コード品質・フォーマット
mvn fmt:format          # Google Java Formatでコード整形
mvn checkstyle:check    # Checkstyleでコーディング規約チェック
mvn spotbugs:check      # SpotBugsでバグパターン検出
mvn pmd:check           # PMDでコード品質分析
mvn validate            # 全てのコード品質チェック実行
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

### JWT認証関連

- JWTトークンの有効期限切れ: 自動的に再ログインが必要です
- 認証に失敗する場合: メールアドレスとパスワードを確認してください

### データベース関連

- PostgreSQLコンテナが起動しない場合：`docker-compose down -v` でボリュームをリセット
- データベースを初期化する場合：`docker-compose down -v && docker-compose up -d postgres`

### ポート競合

デフォルトポートが使用中の場合は、`docker-compose.yml` のポート設定を変更してください。

## ライセンス

このプロジェクトは開発中です。