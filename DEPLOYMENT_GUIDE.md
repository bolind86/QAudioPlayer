# 部署指南 - Android音频播放器

## 🚀 快速部署清单

### ✅ 项目准备
- [ ] 代码已完成并测试通过
- [ ] 所有配置文件已准备就绪
- [ ] GitHub仓库已创建

### ✅ GitHub配置
- [ ] Secrets已配置 (签名信息)
- [ ] Actions工作流已启用
- [ ] 分支保护规则已设置

### ✅ 本地构建测试
- [ ] 本地debug构建成功
- [ ] 本地release构建成功 (可选)
- [ ] 所有测试通过

## 📦 打包配置文件清单

### 1. GitHub Actions工作流 (`.github/workflows/`)
- ✅ `android-build.yml` - 主要构建工作流
- ✅ `release.yml` - 发布版本工作流

### 2. 构建配置
- ✅ `app/build.gradle` - 包含签名配置和构建类型
- ✅ `app/proguard-rules.pro` - ProGuard混淆规则
- ✅ `app/signing.properties.template` - 签名配置模板

### 3. 项目配置
- ✅ `.gitignore` - Git忽略文件
- ✅ `README.md` - 项目文档
- ✅ `GITHUB_SETUP.md` - GitHub设置指南

### 4. 辅助脚本
- ✅ `generate-keystore.sh` - Unix/Linux密钥生成脚本
- ✅ `generate-keystore.bat` - Windows密钥生成脚本
- ✅ `validate_project.py` - 项目验证脚本

## 🔧 GitHub设置步骤

### 1. 创建GitHub仓库
```bash
# 在本地项目目录中
 git init
 git add .
 git commit -m "Initial commit - Android Audio Player"
 git branch -M main
 git remote add origin https://github.com/your-username/AndroidAudioPlayer.git
 git push -u origin main
```

### 2. 配置GitHub Secrets

进入GitHub仓库 → Settings → Secrets and variables → Actions → New repository secret

#### 必需Secrets:
1. **KEYSTORE_BASE64** (推荐)
   ```bash
   # 生成Base64编码的密钥库
   base64 -w 0 release.keystore > keystore.base64
   # 复制文件内容作为Secret值
   ```

2. **或者分别设置:**
   - `KEYSTORE_PASSWORD`: 密钥库密码
   - `KEY_ALIAS`: 密钥别名  
   - `KEY_PASSWORD`: 密钥密码

#### 生成密钥库:
```bash
# Linux/Mac
./generate-keystore.sh

# Windows
generate-keystore.bat
```

### 3. 启用GitHub Actions
- 进入仓库的Actions页面
- 确认工作流已启用
- 检查是否有任何需要批准的Actions

## 🏗️ 构建流程

### 自动构建触发条件
1. **Push到main分支**: 自动构建debug版本
2. **Pull Request**: 自动构建并运行测试
3. **创建标签**: 自动构建release版本
4. **手动触发**: 可通过Actions页面手动运行

### 构建输出
- **Debug APK**: `app-debug.apk`
- **Release APK**: `app-release.apk` (已签名)
- **Release AAB**: `app-release.aab` (Google Play用)
- **测试报告**: 单元测试和集成测试结果
- **Mapping文件**: 代码混淆映射文件

## 📱 安装和测试

### 从GitHub Actions下载
1. 进入GitHub仓库的Actions页面
2. 选择最新的成功构建
3. 下载Artifacts中的APK文件
4. 在Android设备上安装

### 本地安装测试
```bash
# 安装到连接的设备
adb install path/to/app-debug.apk

# 或者手动安装
# 1. 在设备设置中启用"未知来源"
# 2. 传输APK到设备
# 3. 点击APK文件安装
```

## 🔄 发布流程

### 自动发布 (推荐)
```bash
# 创建版本标签
git tag v1.0.0
git push origin v1.0.0

# GitHub Actions会自动:
# 1. 构建release版本
# 2. 创建GitHub Release
# 3. 上传APK和AAB文件
```

### 手动发布
1. 进入GitHub仓库的Actions页面
2. 选择"Release Build"工作流
3. 点击"Run workflow"
4. 输入版本号
5. 等待构建完成

## 🔍 故障排除

### 常见问题

#### 1. 构建失败
- **检查日志**: 查看GitHub Actions的详细日志
- **本地测试**: 在本地运行相同命令
- **依赖问题**: 确保所有依赖正确配置

#### 2. 签名错误
- **密钥库格式**: 确保Base64编码正确
- **密码错误**: 检查Secrets中的密码
- **密钥别名**: 确认别名正确无误

#### 3. 安装失败
- **权限问题**: 检查设备是否允许未知来源
- **API级别**: 确认设备API级别符合要求
- **存储空间**: 检查设备存储空间

### 调试步骤
1. **查看构建日志**: GitHub Actions页面
2. **本地验证**: 运行验证脚本
3. **设备日志**: 使用`adb logcat`查看设备日志
4. **测试覆盖**: 确保所有功能测试通过

## 📊 监控和优化

### 构建性能
- **缓存利用**: 确保Gradle缓存配置正确
- **并行构建**: 利用GitHub Actions的并行能力
- **增量构建**: 优化构建脚本减少重复工作

### 质量监控
- **测试覆盖率**: 维持高测试覆盖率
- **代码质量**: 使用静态代码分析工具
- **性能监控**: 监控APK大小和构建时间

## 🚀 高级配置

### 多环境部署
```yaml
# 可以配置多个环境
- develop: 开发环境
- staging: 测试环境  
- production: 生产环境
```

### 自动部署到应用商店
可以扩展工作流支持:
- Google Play Console API
- 华为应用市场
- 其他第三方应用商店

### 代码质量集成
- SonarQube集成
- 依赖安全检查
- 性能基准测试

## 📋 部署检查清单

### 代码准备
- [ ] 所有功能实现完成
- [ ] 代码审查通过
- [ ] 测试覆盖率达标
- [ ] 文档完整

### 配置检查
- [ ] Secrets配置正确
- [ ] 签名配置有效
- [ ] ProGuard规则适用
- [ ] 版本号正确

### 构建验证
- [ ] Debug构建成功
- [ ] Release构建成功
- [ ] 所有测试通过
- [ ] APK安装测试通过

### 发布准备
- [ ] 发布说明准备
- [ ] 版本标签创建
- [ ] 构建工件验证
- [ ] 回滚计划准备

---

## 🎯 下一步操作

1. **立即执行**: 按照上述步骤配置GitHub仓库
2. **生成本地密钥**: 运行密钥生成脚本
3. **测试构建**: 推送代码触发自动构建
4. **验证功能**: 下载APK进行设备测试
5. **创建发布**: 打标签创建正式版本

**🎉 项目已完全准备好进行GitHub线上打包和发布！**