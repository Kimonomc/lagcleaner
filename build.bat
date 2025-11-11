@echo off
echo LagCleaner 构建脚本
echo ===================

:: 检查Java是否安装
java -version >nul 2>&1
if %errorLevel% neq 0 (
    echo 错误: 未找到Java。请确保已安装Java 17或更高版本。
    pause
    exit /b 1
)

:: 检查Maven是否安装
mvn -version >nul 2>&1
if %errorLevel% neq 0 (
    echo 错误: 未找到Maven。请确保已安装Maven并配置环境变量。
    pause
    exit /b 1
)

:: 清理并构建项目
echo 正在清理并构建项目...
mvn clean package

:: 检查构建是否成功
if %errorLevel% equ 0 (
    echo 构建成功！
    echo JAR文件位于: target/lagcleaner-1.0.0.jar
) else (
    echo 构建失败！
    pause
    exit /b 1
)

pause
