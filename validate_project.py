#!/usr/bin/env python3
"""
项目验证脚本 - 检查Android音频播放器项目的基本结构和语法
"""

import os
import re
import sys

def check_file_exists(filepath, description):
    """检查文件是否存在"""
    if os.path.exists(filepath):
        print(f"[OK] {description}: 存在")
        return True
    else:
        print(f"[ERROR] {description}: 缺失")
        return False

def check_kotlin_syntax(filepath):
    """检查Kotlin文件的基本语法"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # 检查常见的语法问题
        issues = []
        
        # 检查未闭合的大括号
        open_braces = content.count('{')
        close_braces = content.count('}')
        if open_braces != close_braces:
            issues.append(f"大括号不匹配: {open_braces} 开启, {close_braces} 闭合")
        
        # 检查未闭合的括号
        open_parens = content.count('(')
        close_parens = content.count(')')
        if open_parens != close_parens:
            issues.append(f"括号不匹配: {open_parens} 开启, {close_parens} 闭合")
        
        # 检查基本的Kotlin语法结构
        if 'package ' not in content:
            issues.append("缺少package声明")
        
        # 检查类定义
        class_matches = re.findall(r'class\s+(\w+)', content)
        if not class_matches and 'object' not in content:
            issues.append("缺少类或对象定义")
        
        return issues
    except Exception as e:
        return [f"读取文件失败: {e}"]

def check_gradle_file():
    """检查Gradle构建文件"""
    gradle_file = "build.gradle"
    if not os.path.exists(gradle_file):
        print("❌ build.gradle 文件缺失")
        return False
    
    try:
        with open(gradle_file, 'r', encoding='utf-8') as f:
            content = f.read()
        
        required_plugins = ['com.android.application', 'org.jetbrains.kotlin.android']
        required_dependencies = ['androidx.media3:media3-exoplayer', 'androidx.room:room-runtime']
        
        missing_plugins = [plugin for plugin in required_plugins if plugin not in content]
        missing_deps = [dep for dep in required_dependencies if dep not in content]
        
        if missing_plugins:
            print(f"[WARNING] 缺少插件: {missing_plugins}")
        if missing_deps:
            print(f"[WARNING] 缺少依赖: {missing_deps}")
        
        if not missing_plugins and not missing_deps:
            print("[OK] Gradle配置完整")
            return True
        return len(missing_plugins) == 0 and len(missing_deps) == 0
    except Exception as e:
        print(f"[ERROR] 检查Gradle文件失败: {e}")
        return False

def main():
    """主验证函数"""
    print("Android音频播放器项目验证")
    print("=" * 50)
    
    # 检查基本文件结构
    required_files = [
        ("build.gradle", "主构建文件"),
        ("app/build.gradle", "应用模块构建文件"),
        ("app/src/main/AndroidManifest.xml", "Android清单文件"),
        ("app/src/main/java/com/audioplayer/MainActivity.kt", "主活动文件"),
        ("app/src/main/java/com/audioplayer/data/AudioFile.kt", "音频文件数据类"),
        ("app/src/main/java/com/audioplayer/service/AudioService.kt", "音频服务"),
        ("app/src/main/java/com/audioplayer/ui/AudioPlayerApp.kt", "主UI组件"),
        ("app/src/main/java/com/audioplayer/viewmodel/AudioPlayerViewModel.kt", "视图模型"),
    ]
    
    structure_ok = True
    for filepath, description in required_files:
        if not check_file_exists(filepath, description):
            structure_ok = False
    
    print("\n" + "=" * 50)
    
    # 检查Kotlin文件语法
    kotlin_files = [
        "app/src/main/java/com/audioplayer/MainActivity.kt",
        "app/src/main/java/com/audioplayer/data/AudioFile.kt",
        "app/src/main/java/com/audioplayer/service/AudioService.kt",
        "app/src/main/java/com/audioplayer/ui/AudioPlayerApp.kt",
        "app/src/main/java/com/audioplayer/viewmodel/AudioPlayerViewModel.kt",
    ]
    
    syntax_ok = True
    for filepath in kotlin_files:
        if os.path.exists(filepath):
            issues = check_kotlin_syntax(filepath)
            if issues:
                print(f"[WARNING] {filepath} 发现问题:")
                for issue in issues:
                    print(f"   - {issue}")
                syntax_ok = False
            else:
                print(f"[OK] {filepath} 语法检查通过")
    
    print("\n" + "=" * 50)
    
    # 检查Gradle配置
    gradle_ok = check_gradle_file()
    
    print("\n" + "=" * 50)
    print("验证结果总结:")
    
    if structure_ok:
        print("[OK] 项目结构完整")
    else:
        print("[ERROR] 项目结构存在问题")
    
    if syntax_ok:
        print("[OK] Kotlin语法检查通过")
    else:
        print("[ERROR] Kotlin语法存在问题")
    
    if gradle_ok:
        print("[OK] Gradle配置正确")
    else:
        print("[ERROR] Gradle配置需要检查")
    
    overall_ok = structure_ok and syntax_ok and gradle_ok
    
    if overall_ok:
        print("\n项目验证通过！可以开始构建和测试。")
        return 0
    else:
        print("\n项目存在一些问题，建议修复后再进行构建。")
        return 1

if __name__ == "__main__":
    sys.exit(main())