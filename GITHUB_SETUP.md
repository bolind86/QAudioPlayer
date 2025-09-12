# GitHub Actions 设置指南

## 概述

本项目使用GitHub Actions进行自动化构建和发布。为了正常使用CI/CD功能，需要配置一些必要的Secrets。

## 必需的Secrets配置

### 1. 签名配置 (用于发布版构建)

#### 方法1: 使用Base64编码的密钥库

1. **生成密钥库** (如果还没有)
   ```bash
   # Linux/Mac
   ./generate-keystore.sh
   
   # Windows
   generate-keystore.bat
   ```

2. **将密钥库转换为Base64**
   ```bash
   # Linux/Mac
   base64 -w 0 release.keystore > keystore.base64
   
   # Windows (PowerShell)
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore")) > keystore.base64
   ```

3. **添加Secret到GitHub**
   - 打开GitHub仓库页面
   - 进入 Settings → Secrets and variables → Actions
   - 点击 "New repository secret"
   - 名称: `KEYSTORE_BASE64`
   - 值: 复制keystore.base64文件的内容

#### 方法2: 分别配置密钥信息

1. **添加以下Secrets:**
   - `KEYSTORE_PASSWORD`: 密钥库密码
   - `KEY_ALIAS`: 密钥别名
   - `KEY_PASSWORD`: 密钥密码

### 2. GitHub Token (自动生成)

GitHub会自动提供 `GITHUB_TOKEN` secret，无需手动配置。

## 可选的Secrets配置

### 版本号配置

如果需要自定义版本号，可以在手动触发工作流时输入。

## 工作流说明

### 1. Android Build (`android-build.yml`)

**触发条件:**
- 推送到 main 或 develop 分支
- 创建Pull Request
- 手动触发 (workflow_dispatch)

**功能:**
- 运行单元测试
- 构建Debug APK
- 上传构建工件

**使用方法:**
1. 推送到main分支会自动构建debug版本
2. 手动触发时可选择构建debug或release版本

### 2. Release Build (`release.yml`)

**触发条件:**
- 推送版本标签 (如 `v1.0.0`)
- 手动触发 (workflow_dispatch)

**功能:**
- 构建Release APK和AAB
- 自动创建GitHub Release
- 上传签名后的构建文件

**使用方法:**
1. **自动触发**: 创建并推送标签
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. **手动触发**: 
   - 进入GitHub仓库的Actions页面
   - 选择 "Release Build" 工作流
   - 点击 "Run workflow"
   - 输入版本号 (如 1.0.0)

## 构建输出

### APK文件
- **Debug APK**: `app-debug.apk`
- **Release APK**: `app-release.apk` (已签名)

### AAB文件 (Google Play)
- **Release AAB**: `app-release.aab` (已签名)

### 其他文件
- **Mapping文件**: `mapping.txt` (用于混淆代码调试)
- **测试报告**: 单元测试和UI测试结果

## 故障排除

### 常见问题

#### 1. 构建失败 - 签名错误
**问题**: `Keystore was tampered with, or password was incorrect`
**解决**: 
- 检查Secrets中的密码是否正确
- 确认KEYSTORE_BASE64格式正确
- 重新生成密钥库并更新Secrets

#### 2. 构建超时
**问题**: 构建过程超时
**解决**:
- 检查是否包含大文件
- 优化构建配置
- 考虑使用自托管运行器

#### 3. 测试失败
**问题**: 单元测试或UI测试失败
**解决**:
- 查看测试报告获取详细信息
- 在本地运行测试进行调试
- 检查代码变更是否破坏了现有功能

### 调试技巧

1. **查看构建日志**
   - 进入GitHub仓库的Actions页面
   - 点击失败的构建任务
   - 查看详细的日志输出

2. **本地测试**
   ```bash
   # 运行测试
   ./gradlew test
   
   # 构建debug版本
   ./gradlew assembleDebug
   
   # 构建release版本 (需要签名配置)
   ./gradlew assembleRelease
   ```

3. **验证配置**
   ```bash
   # 运行项目验证脚本
   python validate_project.py
   ```

## 最佳实践

### 1. 分支管理
- `main`: 稳定版本分支
- `develop`: 开发分支
- `feature/*`: 功能开发分支
- `hotfix/*`: 紧急修复分支

### 2. 版本管理
- 使用语义化版本号 (如 1.0.0)
- 创建标签时附带详细的发布说明
- 保持CHANGELOG更新

### 3. 安全建议
- 永远不要提交密钥文件到版本控制
- 定期轮换签名密钥
- 使用强密码保护密钥库
- 限制GitHub Secrets的访问权限

### 4. 性能优化
- 启用Gradle构建缓存
- 使用依赖缓存
- 考虑使用增量构建
- 监控构建时间和资源使用

## 监控和通知

### 构建状态
构建状态会显示在仓库主页的README文件中。

### 通知设置
可以在GitHub设置中配置构建失败的通知方式：
1. 进入 Settings → Notifications
2. 配置Actions通知偏好
3. 可以设置邮件、Slack等通知方式

## 扩展功能

### 可能的扩展
1. **自动部署到Google Play**: 使用Google Play API
2. **代码质量检查**: 集成SonarQube或CodeClimate
3. **安全扫描**: 集成依赖安全检查
4. **性能测试**: 添加性能基准测试
5. **多环境部署**: 支持alpha、beta、production环境

### 相关文档
- [GitHub Actions文档](https://docs.github.com/en/actions)
- [Android构建文档](https://developer.android.com/studio/build)
- [APK签名文档](https://developer.android.com/studio/publish/app-signing)