@echo off
chcp 65001 >nul
title 医院综合信息管理系统

echo =============================================
echo   医院综合信息管理系统 - 启动脚本
echo   技术栈: Spring Boot 3.2 + MyBatis-Plus + Redis + Kafka + MySQL
echo =============================================
echo.

:: 检查 Java 环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Java 运行环境，请安装 JDK 21+
    pause
    exit /b 1
)

:: 检查 Node.js 环境
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [警告] 未检测到 Node.js，前端将无法启动
)

echo.
echo [步骤 1/4] 初始化数据库...
echo 请确保 MySQL 已启动，然后执行: sql\init.sql
echo.

echo [步骤 2/4] 编译后端...
cd /d "%~dp0"
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 (
    echo [错误] 后端编译失败
    pause
    exit /b 1
)
echo [OK] 后端编译成功

echo.
echo [步骤 3/4] 启动后端服务...
start "Hospital-Backend" java -jar target/hospital-system-1.0.0.jar
echo [OK] 后端服务启动中... (端口 8080)
timeout /t 5 /nobreak >nul

echo.
echo [步骤 4/4] 启动前端...
cd /d "%~dp0frontend"
if exist node_modules (
    echo 前端依赖已安装
) else (
    echo 正在安装前端依赖...
    call npm install --silent
)
start "Hospital-Frontend" cmd /c "npm run dev"
echo [OK] 前端服务启动中... (端口 3000)

echo.
echo =============================================
echo   启动完成!
echo   后端地址: http://localhost:8080/api
echo   Swagger:  http://localhost:8080/api/doc.html
echo   前端地址: http://localhost:3000
echo   演示账号: admin / 123456
echo =============================================
pause
