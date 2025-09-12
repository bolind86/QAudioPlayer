#!/bin/bash

# 修复GitHub Actions构建问题的脚本

echo "=== AudioPlayer GitHub Actions 构建修复脚本 ==="

# 1. 确保gradlew有执行权限
echo "正在设置gradlew权限..."
chmod +x ./gradlew

# 2. 验证Gradle wrapper配置
echo "验证Gradle wrapper配置..."
if [ -f "gradle/wrapper/gradle-wrapper.properties" ]; then
    echo "✅ gradle-wrapper.properties 存在"
    cat gradle/wrapper/gradle-wrapper.properties
else
    echo "❌ gradle-wrapper.properties 不存在"
    exit 1
fi

# 3. 检查Gradle wrapper jar
if [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "✅ gradle-wrapper.jar 存在"
else
    echo "❌ gradle-wrapper.jar 不存在，正在重新生成..."
    # 重新生成wrapper
    ./gradlew wrapper --gradle-version 8.4
fi

# 4. 运行本地测试
echo "运行本地构建测试..."
./gradlew clean
./gradlew assembleDebug

echo "=== 修复完成 ==="
echo "现在你可以提交修改到GitHub进行在线构建测试"