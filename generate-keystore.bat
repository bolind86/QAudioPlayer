@echo off
REM 密钥生成脚本 (Windows版本)
REM 用于生成发布版APK签名所需的keystore文件

echo 生成Android发布版密钥库...
echo ==================================

REM 检查是否已存在keystore文件
if exist "release.keystore" (
    echo 警告: release.keystore 文件已存在！
    set /p overwrite=是否覆盖现有文件? (y/N): 
    if /i "%overwrite%"=="y" (
        del release.keystore
    ) else (
        echo 取消操作
        exit /b 1
    )
)

REM 提示用户输入信息
echo 请输入密钥库信息:

set /p storepass=密钥库密码: 
set /p alias=密钥别名: 
set /p keypass=密钥密码: 
set /p name=姓名: 
set /p ou=组织单位: 
set /p o=组织: 
set /p l=城市: 
set /p st=州/省: 
set /p c=国家代码 (如CN): 
set /p validity=有效期 (年, 默认25年): 

REM 设置默认值
if "%validity%"=="" set validity=25

REM 生成keystore
keytool -genkey -v ^
    -keystore release.keystore ^
    -storepass "%storepass%" ^
    -keypass "%keypass%" ^
    -alias "%alias%" ^
    -keyalg RSA ^
    -keysize 2048 ^
    -validity %validity% ^
    -dname "CN=%name%, OU=%ou%, O=%o%, L=%l%, ST=%st%, C=%c%"

if %errorlevel% equ 0 (
    echo.
    echo ==================================
    echo 密钥库生成成功！
    echo 文件: release.keystore
    echo 别名: %alias%
    echo 有效期: %validity% 年
    echo.
    echo 请妥善保管此文件和密码，不要提交到版本控制！
    echo.
    echo 更新 app/signing.properties 文件:
    echo RELEASE_STORE_FILE=../release.keystore
    echo RELEASE_STORE_PASSWORD=%storepass%
    echo RELEASE_KEY_ALIAS=%alias%
    echo RELEASE_KEY_PASSWORD=%keypass%
) else (
    echo.
    echo 错误: 密钥库生成失败！
    exit /b 1
)

pause