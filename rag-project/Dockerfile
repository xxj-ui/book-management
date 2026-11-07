# ============================================
# AI 智能助手 Dockerfile
# ============================================
FROM python:3.11-slim

WORKDIR /app

# 安装系统依赖
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    default-libmysqlclient-dev \
    && rm -rf /var/lib/apt/lists/*

# 复制依赖文件
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

# 复制源码
COPY . .

# 创建聊天历史数据目录（持久化用）
RUN mkdir -p /app/data

EXPOSE 8501

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=15s \
    CMD curl -s http://localhost:8501/_stcore/health || exit 1

ENTRYPOINT ["streamlit", "run", "app.py", "--server.port=8501", "--server.address=0.0.0.0"]
