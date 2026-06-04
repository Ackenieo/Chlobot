#!/bin/bash

set -e

echo "========================================"
echo "  Chlobot 一键启动脚本"
echo "========================================"
echo ""

# 设置路径
SECRETS_DIR="D:/WWWWokP/college_students_project/secrets"
ENV_FILE="${SECRETS_DIR}/.env.chlobot"

# 检查密钥文件
echo "[1/5] 检查密钥配置文件..."
if [ ! -f "$ENV_FILE" ]; then
    echo ""
    echo "❌ 错误：密钥文件不存在"
    echo "位置：$ENV_FILE"
    echo ""
    echo "请执行以下步骤:"
    echo "1. 复制示例文件：cp ${SECRETS_DIR}/.env.chlobot.example ${ENV_FILE}"
    echo "2. 编辑 ${ENV_FILE} 填入真实的密钥信息"
    echo ""
    exit 1
fi
echo "✓ 密钥文件存在"

# 加载环境变量
echo ""
echo "[2/5] 加载环境变量..."
if [ -f "$ENV_FILE" ]; then
    export $(grep -v '^#' "$ENV_FILE" | xargs)
fi

# 检查 Docker
echo ""
echo "[3/5] 检查 Docker 服务..."
if ! docker info > /dev/null 2>&1; then
    echo "❌ 错误：Docker 未运行"
    echo "请先启动 Docker Desktop"
    exit 1
fi
echo "✓ Docker 运行正常"

# 启动数据库
echo ""
echo "[4/5] 启动数据库服务 (MySQL + PostgreSQL)..."
docker compose -f chlobot-compose.yml up -d mysql postgres
if [ $? -ne 0 ]; then
    echo "❌ 错误：启动数据库失败"
    exit 1
fi

echo ""
echo "等待数据库就绪..."
sleep 15

# 启动应用
echo ""
echo "[5/5] 启动 Spring Boot 应用..."
docker compose -f chlobot-compose.yml up -d app
if [ $? -ne 0 ]; then
    echo "❌ 错误：启动应用失败"
    exit 1
fi

echo ""
echo "========================================"
echo "  🎉 启动完成！"
echo "========================================"
echo ""
echo "服务访问地址:"
echo "  - API: http://localhost:8136/api"
echo "  - Swagger: http://localhost:8136/api/swagger-ui.html"
echo "  - MySQL: localhost:3362"
echo "  - PostgreSQL: localhost:5488"
echo ""
echo "查看日志：docker compose -f chlobot-compose.yml logs -f app"
echo "停止服务：docker compose -f chlobot-compose.yml down"
echo ""
