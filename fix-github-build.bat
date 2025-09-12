@echo off
echo === AudioPlayer GitHub Actions 构建修复脚本 ===

echo 1. 验证Gradle wrapper配置...
if exist "gradle\wrapper\gradle-wrapper.properties" (
    echo ✅ gradle-wrapper.properties 存在
    type gradle\wrapper\gradle-wrapper.properties
) else (
    echo ❌ gradle-wrapper.properties 不存在
    exit /b 1
)

echo.
echo 2. 检查Gradle wrapper jar...
if exist "gradle\wrapper\gradle-wrapper.jar" (
    echo ✅ gradle-wrapper.jar 存在
) else (
    echo ❌ gradle-wrapper.jar 不存在，正在重新生成...
    gradlew.bat wrapper --gradle-version 8.4
)

echo.
echo 3. 运行本地构建测试...
gradlew.bat clean
gradlew.bat assembleDebug

echo.
echo === 修复完成 ===
echo 现在你可以提交修改到GitHub进行在线构建测试
pause