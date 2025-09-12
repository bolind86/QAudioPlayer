# 安卓音频播放器项目

## 项目概述
这是一个支持文件夹管理和多种播放模式的安卓音频播放器应用。

## 核心功能
- 以文件夹形式添加音频文件到播放列表
- 支持播放列表循环播放
- 支持单曲循环播放
- 音频文件管理和播放控制

## 项目架构

### 主要组件
- **MainActivity**: 主界面，包含播放控制和文件夹浏览
- **AudioService**: 后台音频播放服务
- **PlaylistManager**: 播放列表管理器
- **FileManager**: 文件夹和音频文件管理
- **PlaybackController**: 播放控制逻辑

### 数据结构
- **AudioFile**: 音频文件实体类
- **Playlist**: 播放列表实体类
- **PlayMode**: 播放模式枚举（顺序播放、列表循环、单曲循环）

## 开发命令

### 构建项目
```bash
./gradlew build
```

### 运行应用
```bash
./gradlew installDebug
```

### 运行测试
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### 代码检查
```bash
./gradlew lint
./gradlew ktlintCheck
```

## 关键技术栈
- **开发语言**: Kotlin
- **UI框架**: Jetpack Compose / XML布局
- **音频播放**: MediaPlayer / ExoPlayer 
- **数据存储**: Room Database
- **权限管理**: 存储访问权限
- **后台服务**: Foreground Service

## 核心实现要点

### 文件夹音频扫描
- 使用 MediaStore API 扫描指定文件夹
- 支持常见音频格式（MP3, FLAC, AAC, OGG）
- 递归扫描子文件夹

### 播放模式实现
- **顺序播放**: 按添加顺序播放
- **列表循环**: 播放完最后一首后回到第一首
- **单曲循环**: 重复播放当前歌曲

### 后台播放服务
- 使用 Foreground Service 保持播放状态
- MediaSession 支持锁屏控制
- 通知栏播放控制

### 数据持久化
- Room 数据库存储播放列表
- SharedPreferences 存储播放设置
- 文件路径和播放进度保存

## 权限要求
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

## 项目结构
```
app/
├── src/main/java/com/audioplayer/
│   ├── ui/                 # UI组件
│   ├── service/           # 后台服务
│   ├── data/              # 数据层
│   ├── utils/             # 工具类
│   └── MainActivity.kt    # 主活动
├── src/test/              # 单元测试
└── src/androidTest/       # UI测试
```
