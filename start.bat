@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo   Chlobot 一键启动脚本
echo ========================================
echo.

REM 设置路径
set SECRETS_DIR=D:\WWWWokP\college_students_project\secrets
set ENV_FILE=%SECRETS_DIR%\.env.chlobot

REM 检查密钥文件
echo [1/5] 检查密钥配置文件...
if not exist "%ENV_FILE%" (
    echo.
    echo ❌ 错误：密钥文件不存在
    echo 位置：%ENV_FILE%
    echo.
    echo 请执行以下步骤:
    echo 1. 复制示例文件：copy "%SECRETS_DIR%\.env.chlobot.example" "%ENV_FILE%"
    echo 2. 编辑 %ENV_FILE% 填入真实的密钥信息
    echo.
    pause
    exit /b 1
)
echo ✓ 密钥文件存在

REM 加载环境变量
echo.
echo [2/5] 加载环境变量...
for /f "delims=" %%a in ('findstr /v "^#" "%ENV_FILE%" ^| findstr /v "^$"') do (
    for /f "tokens=1,2 delims==" %%b in ("%%a") do (
        set "%%b=%%c"
    )
)

REM 检查 Docker
echo.
echo [3/5] 检查 Docker 服务...
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：Docker 未运行
    echo 请先启动 Docker Desktop
    pause
    exit /b 1
)
echo ✓ Docker 运行正常

REM 启动数据库
echo.
echo [4/5] 启动数据库服务 (MySQL + PostgreSQL)...
docker compose -f chlobot-compose.yml up -d mysql postgres
if errorlevel 1 (
    echo ❌ 错误：启动数据库失败
    pause
    exit /b 1
)

echo.
echo 等待数据库就绪...
timeout /t 15 /nobreak >nul

REM 启动应用
echo.
echo [5/5] 启动 Spring Boot 应用...
docker compose -f chlobot-compose.yml up -d app
if errorlevel 1 (
    echo ❌ 错误：启动应用失败
    pause
    exit /b 1
)

echo.
echo ========================================
echo   🎉 启动完成！
echo ========================================
echo.
echo 服务访问地址:
echo   - API: http://localhost:8136/api
echo   - Swagger: http://localhost:8136/api/swagger-ui.html
echo   - MySQL: localhost:3362
echo   - PostgreSQL: localhost:5488
echo.
echo 查看日志：docker compose -f chlobot-compose.yml logs -f app
echo 停止服务：docker compose -f chlobot-compose.yml down
echo.
pause
