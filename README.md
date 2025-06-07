# トレーニング記録アプリ

## 概要

このアプリは、筋トレやフィットネスのトレーニング記録を簡単に管理できるAndroidアプリです。種目・日付・重量・回数・セット数を記録し、履歴やグラフで推移を可視化できます。

---

## 画面イメージ
- トレーニング記録入力フォーム
- トレーニング量推移グラフ
- トレーニング履歴リスト

---

## 使い方
- トレーニング記録入力
トレーニング種目・日付・重量・回数・セット数を記録できます。

- トレーニング量推移グラフ
トレーニング入力をすることで、これまでの記録したトレーニング量のグラフが表示されます。

- トレーニング履歴リスト
トレーニング履歴を表示します。
絞込み検索では、トレーニング種目・日付ごとの絞込み表示ができます。
リセットで、履歴が全件表示されます。

- トレーニング履歴
タップで、記録した履歴内容を修正できます。
長押しで削除も可能です。削除確認ダイヤログが表示されます。

---

## 主な機能
- トレーニング記録の入力・保存・編集・削除
- 履歴のリスト表示（種目・日付・重量・回数・セット数）
- MPAndroidChartによるグラフ表示（日・週・月単位で推移を切替）
- 種目・日付による絞込み検索（モーダルダイアログ）
- Roomデータベースによるデータ管理
- SharedPreferencesからRoomへのデータ移行機能
- UI/UXを意識したモダンなデザイン

---

## セットアップ方法

1. **リポジトリをクローン**
    ```sh
git clone https://github.com/yumion787/TraningRecordApp.git
cd TraningRecordApp
    ```
2. **Android Studioで開く**
3. **必要な依存ライブラリはGradleで自動インストールされます**
4. **エミュレータまたは実機を用意し、Run 'app'で起動**

---

## ビルド・実行方法

- Android Studioの「Run」ボタン（緑の三角）でビルド＆実行
- 実行構成は「app」を選択
- エミュレータまたは実機で動作確認

---

## テスト方法

- ViewModelやRoomのテストは`src/test/java`および`src/androidTest/java`に実装済み
- Android Studioで各テストクラスを右クリック→「Run」で実行

---

## 主な技術・ライブラリ
- Kotlin
- Android Jetpack（ViewModel, LiveData, Room, DataBinding, etc.）
- MPAndroidChart
- Material Components
- Gson
- JUnit, Mockito, Coroutines Test

---

## ディレクトリ構成

```
app/
  src/
    main/
      java/com/example/traningrecordapp/
        adapter/
        data/
        util/
        viewmodel/
      res/
    test/
    androidTest/
```

