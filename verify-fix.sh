#!/bin/bash

# 验证修复效果的测试脚本

echo "=== GitHub Actions 构建修复验证 ==="

# 1. 检查重要文件是否存在
echo "检查关键文件..."

files_to_check=(
    ".github/workflows/build.yml"
    ".github/workflows/android-build.yml"
    "gradle/wrapper/gradle-wrapper.properties"
    "gradlew"
    "gradlew.bat"
    "build.gradle"
    "app/build.gradle"
    "settings.gradle"
)

for file in "${files_to_check[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file 存在"
    else
        echo "❌ $file 缺失"
    fi
done

# 2. 检查Gradle配置
echo -e "\n检查Gradle版本配置..."
if grep -q "gradle-8.4" gradle/wrapper/gradle-wrapper.properties; then
    echo "✅ Gradle wrapper 配置为 8.4"
else
    echo "⚠️  Gradle wrapper 版本可能不是 8.4"
fi

# 3. 检查GitHub Actions配置
echo -e "\n检查GitHub Actions配置..."
if grep -q "gradle-version: 8.4" .github/workflows/build.yml; then
    echo "✅ GitHub Actions 配置了正确的Gradle版本"
else
    echo "❌ GitHub Actions Gradle版本配置可能有问题"
fi

if grep -q "./gradlew" .github/workflows/build.yml; then
    echo "✅ GitHub Actions 使用了gradlew wrapper"
else
    echo "❌ GitHub Actions 可能仍在使用gradle命令"
fi

echo -e "\n=== 验证完成 ==="
echo "如果所有检查都通过，可以提交代码并测试GitHub构建"