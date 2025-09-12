# 🎵 Android音频播放器

一个功能完整的Android音频播放器应用，支持文件夹管理和多种播放模式。

## ✨ 功能特性

### 🎼 核心播放功能
- **文件夹播放**: 以文件夹形式添加音频文件到播放列表
- **多种播放模式**: 顺序播放、列表循环、单曲循环
- **后台播放**: 支持后台播放和通知控制
- **播放控制**: 播放/暂停、上一首/下一首、进度调节
- **音量控制**: 集成音量调节功能

### 📱 用户界面
- **现代化UI**: 采用Jetpack Compose构建
- **流畅动画**: 平滑的过渡效果和状态变化动画
- **深色主题**: 支持深色和浅色主题
- **响应式设计**: 适配不同屏幕尺寸

### 🛠 技术特性
- **Media3集成**: 使用最新的Media3库进行音频播放
- **Room数据库**: 本地数据持久化存储
- **协程支持**: 异步操作和后台任务管理
- **权限管理**: 完整的存储访问权限处理
- **错误处理**: 用户友好的错误提示和反馈

## 🚀 快速开始

### 环境要求
- Android Studio Arctic Fox 或更高版本
- JDK 17
- Android SDK (最低API 24, 目标API 34)
- Kotlin 1.9+

### 本地构建

1. **克隆仓库**
```bash
git clone https://github.com/your-username/AndroidAudioPlayer.git
cd AndroidAudioPlayer
```

2. **打开项目**
在Android Studio中打开项目文件夹

3. **同步依赖**
等待Gradle同步完成

4. **构建和运行**
点击运行按钮或按 `Shift+F10`

### 签名配置 (发布版)

1. **创建签名配置**
```bash
cp app/signing.properties.template app/signing.properties
```

2. **编辑签名信息**
编辑 `app/signing.properties` 文件，填入您的签名信息

3. **生成签名密钥** (如果还没有)
```bash
keytool -genkey -v -keystore release.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias your_key_alias
```

## 📋 使用说明

### 添加音乐文件夹
1. 点击主界面右上角的 "+" 按钮
2. 浏览并选择包含音频文件的文件夹
3. 应用会自动扫描文件夹中的音频文件
4. 新的播放列表会出现在主界面

### 播放控制
- **播放/暂停**: 点击中央的大播放按钮
- **上一首/下一首**: 使用左右箭头按钮
- **进度调节**: 拖动进度条到指定位置
- **音量控制**: 点击音量图标调节音量大小
- **播放模式**: 点击播放模式图标切换顺序/循环/单曲模式

### 后台播放
- 应用支持后台播放，可以在通知栏控制播放
- 锁屏界面也会显示播放控制
- 支持从通知栏快速返回应用

## 🏗️ 项目架构

```
app/
├── src/main/java/com/audioplayer/
│   ├── data/                 # 数据层
│   │   ├── AudioFile.kt      # 音频文件实体
│   │   ├── Playlist.kt       # 播放列表实体
│   │   ├── PlayMode.kt       # 播放模式枚举
│   │   └── AudioDatabase.kt  # Room数据库配置
│   ├── repository/           # 数据仓库层
│   │   └── AudioRepository.kt # 数据操作封装
│   ├── service/              # 服务层
│   │   ├── AudioService.kt   # 后台音频服务
│   │   ├── MediaController.kt # 媒体控制器
│   │   └── AudioNotificationAdapter.kt # 通知适配器
│   ├── ui/                   # UI层
│   │   ├── AudioPlayerApp.kt # 主应用UI
│   │   ├── FolderPicker.kt   # 文件夹选择器
│   │   ├── PlaybackControls.kt # 播放控制组件
│   │   └── theme/            # 主题配置
│   ├── utils/                # 工具类
│   │   └── FileManager.kt    # 文件管理器
│   ├── viewmodel/            # 视图模型层
│   │   └── AudioPlayerViewModel.kt # 业务逻辑
│   └── MainActivity.kt       # 主活动
├── src/test/                 # 单元测试
└── src/androidTest/          # UI测试
```

## 🔧 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose + Material Design 3
- **音频播放**: Media3 ExoPlayer
- **数据存储**: Room Database
- **依赖注入**: 手动依赖注入
- **异步处理**: Kotlin Coroutines
- **权限管理**: Accompanist Permissions
- **构建工具**: Gradle + Android Gradle Plugin

## 🔄 CI/CD

项目包含GitHub Actions工作流，支持自动化构建和测试：

- **自动构建**: 每次推送和PR都会触发构建
- **多环境支持**: 支持Debug和Release构建
- **自动测试**: 运行单元测试并生成报告
- **工件上传**: 自动上传APK文件和测试报告
- **签名支持**: 支持发布版APK签名

### 环境变量配置

在GitHub仓库设置中添加以下Secrets：

- `KEYSTORE_PASSWORD`: 密钥库密码
- `KEY_ALIAS`: 密钥别名
- `KEY_PASSWORD`: 密钥密码

## 📱 兼容性

- **最低Android版本**: Android 7.0 (API 24)
- **目标Android版本**: Android 14 (API 34)
- **支持的音频格式**: MP3, FLAC, AAC, OGG, WAV, M4A, WMA

## 🤝 贡献

欢迎提交Issue和Pull Request！

### 开发规范
1. 遵循Kotlin编码规范
2. 添加适当的注释和文档
3. 编写单元测试
4. 确保CI/CD通过

## 📄 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🆘 支持

如遇到问题，请：
1. 查看 [TEST_GUIDE.md](TEST_GUIDE.md) 中的测试指南
2. 检查 [GitHub Issues](https://github.com/your-username/AndroidAudioPlayer/issues)
3. 创建新的Issue描述问题

---

**⭐ 如果这个项目对你有帮助，请给个Star！**