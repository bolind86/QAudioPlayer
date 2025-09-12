#!/bin/bash

# 密钥生成脚本
# 用于生成发布版APK签名所需的keystore文件

echo "生成Android发布版密钥库..."
echo "=================================="

# 检查是否已存在keystore文件
if [ -f "release.keystore" ]; then
    echo "警告: release.keystore 文件已存在！"
    read -p "是否覆盖现有文件? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "取消操作"
        exit 1
    fi
    rm release.keystore
fi

# 提示用户输入信息
echo "请输入密钥库信息:"
read -p "密钥库密码: " -s storepass
echo
read -p "密钥别名: " alias
read -p "密钥密码: " -s keypass
echo
read -p "姓名: " name
read -p "组织单位: " ou
read -p "组织: " o
read -p "城市: " l
read -p "州/省: " st
read -p "国家代码 (如CN): " c
read -p "有效期 (年, 默认25年): " validity

# 设置默认值
if [ -z "$validity" ]; then
    validity=25
fi

# 生成keystore
keytool -genkey -v \
    -keystore release.keystore \
    -storepass "$storepass" \
    -keypass "$keypass" \
    -alias "$alias" \
    -keyalg RSA \
    -keysize 2048 \
    -validity $((validity * 365)) \
    -dname "CN=$name, OU=$ou, O=$o, L=$l, ST=$st, C=$c"

if [ $? -eq 0 ]; then
    echo
    echo "=================================="
    echo "密钥库生成成功！"
    echo "文件: release.keystore"
    echo "别名: $alias"
    echo "有效期: $validity 年"
    echo
    echo "请妥善保管此文件和密码，不要提交到版本控制！"
    echo
    echo "更新 app/signing.properties 文件:"
    echo "RELEASE_STORE_FILE=../release.keystore"
    echo "RELEASE_STORE_PASSWORD=$storepass"
    echo "RELEASE_KEY_ALIAS=$alias"
    echo "RELEASE_KEY_PASSWORD=$keypass"
else
    echo
    echo "错误: 密钥库生成失败！"
    exit 1
fi