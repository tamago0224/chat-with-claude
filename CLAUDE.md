# CLAUDE.md

このファイルは、Claude Code (claude.ai/code) がこのリポジトリのコードを扱う際のガイダンスを提供します。

## プロジェクトについて

このプロジェクトは、リアルタイムチャット機能を持つWebアプリケーションです。詳細な仕様は `docs/chat_app_specification.md` を参照してください。

## 技術スタック

技術スタックと構成の詳細は `docs/chat_app_specification.md` に記載されています。

### フロントエンド
- Next.js 14+ + TypeScript + TailwindCSS
- Socket.io (WebSocket)
- NextAuth.js (Google OAuth)

### バックエンド  
- Spring Boot 3.x + Java 17
- Spring Data JPA
- Socket.io サーバー
- gRPC

### データベース・キャッシュ
- PostgreSQL 15+
- Valkey (Redis) キャッシュ

### デプロイ
- Docker & Docker Compose

## 開発セットアップ

プロジェクトのセットアップ手順は `docs/chat_app_specification.md` の「開発・デプロイ」セクションを参照してください。

## プロジェクト構造

```
frontend/               # Next.jsフロントエンド
├── src/
│   ├── components/     # UIコンポーネント
│   ├── pages/         # Next.jsページ
│   ├── hooks/         # カスタムフック
│   ├── lib/           # 認証・WebSocket・gRPCクライアント
│   ├── types/         # TypeScript型定義
│   └── utils/         # ユーティリティ関数

backend/               # Spring Bootバックエンド
├── src/main/java/com/chatapp/
│   ├── config/        # 設定クラス
│   ├── controller/    # RESTコントローラー
│   ├── entity/        # JPAエンティティ
│   ├── repository/    # データアクセス層
│   ├── service/       # ビジネスロジック
│   ├── grpc/          # gRPCサービス
│   ├── security/      # セキュリティ設定
│   └── socket/        # WebSocketハンドラー

docs/                  # プロジェクトドキュメント
└── chat_app_specification.md  # 完全仕様書
```

## 開発優先順位

現在フェーズ1（コアインフラ）の実装段階です。開発フェーズの詳細は仕様書を参照してください。

### フェーズ1: コアインフラ（第1-3週） 
- プロジェクトセットアップと設定
- データベーススキーマ作成
- Google OAuth統合
- 基本認証機能
- 開発環境構築

## 重要な指示

- 仕様の詳細が必要な場合は、必ず `docs/chat_app_specification.md` を参照すること
- 技術的な決定は仕様書に記載された技術スタックに従うこと
- データベース設計、API仕様、UI仕様も仕様書に詳細に記載されている