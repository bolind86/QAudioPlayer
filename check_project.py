#!/usr/bin/env python3
"""
简单的Android项目结构检查脚本
"""
import os
import sys

def check_file_exists(file_path, description):
    """检查文件是否存在"""
    if os.path.exists(file_path):
        print(f"[OK] {description}: {file_path}")
        return True
    else:
        print(f"[MISSING] {description}: {file_path}")
        return False

def check_directory_exists(dir_path, description):
    """检查目录是否存在"""
    if os.path.isdir(dir_path):
        print(f"[OK] {description}: {dir_path}")
        return True
    else:
        print(f"[MISSING] {description}: {dir_path}")
        return False

def main():
    print("=== Android音频播放器项目结构检查 ===\n")
    
    project_root = "d:/GitProjects/AudioPlayer"
    
    # 检查项目根目录文件
    print("1. 项目根目录文件:")
    check_file_exists(f"{project_root}/build.gradle", "根build.gradle")
    check_file_exists(f"{project_root}/settings.gradle", "settings.gradle")
    check_file_exists(f"{project_root}/gradlew.bat", "Gradle Wrapper (Windows)")
    check_file_exists(f"{project_root}/CODEBUDDY.md", "项目文档")
    
    print("\n2. App模块文件:")
    check_file_exists(f"{project_root}/app/build.gradle", "App build.gradle")
    
    print("\n3. 主要源代码文件:")
    src_main = f"{project_root}/app/src/main/java/com/audioplayer"
    
    # 核心类
    check_file_exists(f"{src_main}/MainActivity.kt", "主Activity")
    check_file_exists(f"{src_main}/data/PlayMode.kt", "播放模式枚举")
    check_file_exists(f"{src_main}/data/AudioFile.kt", "音频文件实体")
    check_file_exists(f"{src_main}/data/Playlist.kt", "播放列表实体")
    check_file_exists(f"{src_main}/data/AudioDatabase.kt", "数据库")
    
    # 服务类
    check_file_exists(f"{src_main}/service/AudioService.kt", "音频服务")
    check_file_exists(f"{src_main}/service/MediaController.kt", "媒体控制器")
    check_file_exists(f"{src_main}/service/AudioNotificationAdapter.kt", "通知适配器")
    
    # UI类
    check_file_exists(f"{src_main}/ui/AudioPlayerApp.kt", "主UI应用")
    check_file_exists(f"{src_main}/ui/PlaybackControls.kt", "播放控制UI")
    check_file_exists(f"{src_main}/ui/FolderPicker.kt", "文件夹选择器")
    
    # ViewModel
    check_file_exists(f"{src_main}/viewmodel/AudioPlayerViewModel.kt", "主ViewModel")
    
    # 其他
    check_file_exists(f"{src_main}/repository/AudioRepository.kt", "数据仓库")
    check_file_exists(f"{src_main}/utils/FileManager.kt", "文件管理器")
    
    print("\n4. 测试目录:")
    check_directory_exists(f"{project_root}/app/src/test", "单元测试目录")
    check_directory_exists(f"{project_root}/app/src/androidTest", "UI测试目录")
    check_file_exists(f"{project_root}/app/src/test/java/com/audioplayer/PlayModeTest.kt", "播放模式测试")
    check_file_exists(f"{project_root}/app/src/androidTest/java/com/audioplayer/MainActivityTest.kt", "主Activity测试")
    
    print("\n5. Android资源文件:")
    check_directory_exists(f"{project_root}/app/src/main/res", "资源目录")
    
    print("\n=== 检查完成 ===")

if __name__ == "__main__":
    main()