# PyTools部署指南

本文档详细介绍如何在生产环境中部署PyTools图表生成系统，以供Java后端调用。

## 📋 部署方案

### 方案1: 命令行调用 (推荐)
- ✅ 简单直接，无需额外服务
- ✅ 资源占用少
- ✅ 容错性好
- ❌ 每次调用都需要启动Python进程

### 方案2: HTTP API服务
- ✅ 性能更好，复用进程
- ✅ 支持并发处理
- ✅ 可独立扩展
- ❌ 需要维护额外服务

## 🚀 快速部署

### 步骤1: 环境准备

```bash
# 1. 确保Python 3.8+已安装
python --version

# 2. 创建虚拟环境
cd project/pytools
python -m venv venv

# 3. 激活虚拟环境
# Linux/Mac:
source venv/bin/activate
# Windows:
venv\Scripts\activate

# 4. 安装依赖
pip install -r requirements.txt

# 5. 如果使用API模式，安装额外依赖
pip install -r requirements-api.txt
```

### 步骤2: 测试Python环境

```bash
# 测试命令行接口
cd project/pytools/src
python cli.py help

# 测试图表生成
python cli.py create --engine matplotlib --type line --config ../examples/sample_configs/line_config.json
```

### 步骤3: Java配置

在`application.yml`中添加配置:

```yaml
# 引入图表配置
spring:
  profiles:
    include: chart

# 或者直接配置
chart:
  python:
    executable: /path/to/your/python  # Python路径
  output:
    dir: /path/to/output              # 输出目录
```

### 步骤4: 验证集成

```bash
# 启动Java后端
cd project/backend
mvn spring-boot:run

# 测试接口
curl -X GET http://localhost:38081/api/v1/charts/status
```

## 🔧 详细配置

### Python环境配置

#### 1. 虚拟环境管理

```bash
# 创建生产环境
python -m venv /opt/pytools/venv

# 激活并安装依赖
source /opt/pytools/venv/bin/activate
pip install -r requirements.txt

# 验证安装
python -c "import matplotlib, plotly, seaborn; print('OK')"
```

#### 2. 系统级安装

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install python3 python3-pip python3-venv
sudo pip3 install -r requirements.txt

# CentOS/RHEL
sudo yum install python3 python3-pip
sudo pip3 install -r requirements.txt

# 安装字体支持 (中文显示)
sudo apt install fonts-wqy-zenhei fonts-wqy-microhei
```

### Java后端配置

#### application-chart.yml配置详解

```yaml
chart:
  python:
    # Python可执行文件路径
    executable: /opt/pytools/venv/bin/python
    script:
      # 脚本路径 (绝对路径)
      path: /opt/myeden/project/pytools/src/cli.py
  
  # 调用模式
  call:
    mode: cli  # cli 或 api
  
  # 输出配置
  output:
    dir: /var/lib/myeden/charts  # 输出目录
    cleanup:
      enabled: true              # 启用清理
      retention-hours: 24        # 保留24小时
      interval-hours: 6          # 每6小时清理一次
  
  # 超时配置
  timeout:
    seconds: 120  # 2分钟超时
```

#### 权限配置

```bash
# 创建输出目录
sudo mkdir -p /var/lib/myeden/charts
sudo chown myeden:myeden /var/lib/myeden/charts
sudo chmod 755 /var/lib/myeden/charts

# 设置Python脚本权限
sudo chown myeden:myeden /opt/myeden/project/pytools/src/cli.py
sudo chmod 755 /opt/myeden/project/pytools/src/cli.py
```

## 🌐 HTTP API部署

### 使用内置Flask服务器

```bash
# 开发环境
cd project/pytools/src
python api_server.py --host 0.0.0.0 --port 5000

# 生产环境
python api_server.py --host 127.0.0.1 --port 5000 --output-dir /var/lib/charts
```

### 使用Gunicorn (推荐)

```bash
# 安装gunicorn
pip install gunicorn

# 启动服务
gunicorn -w 4 -b 127.0.0.1:5000 api_server:app

# 使用配置文件
gunicorn -c gunicorn.conf.py api_server:app
```

#### gunicorn.conf.py配置

```python
# gunicorn.conf.py
bind = "127.0.0.1:5000"
workers = 4
timeout = 120
keepalive = 2
max_requests = 1000
max_requests_jitter = 100

# 日志配置
accesslog = "/var/log/myeden/pytools-access.log"
errorlog = "/var/log/myeden/pytools-error.log"
loglevel = "info"

# 进程配置
user = "myeden"
group = "myeden"
tmp_upload_dir = "/tmp"

# 性能配置
worker_class = "sync"
worker_connections = 1000
```

### 使用Nginx反向代理

```nginx
# /etc/nginx/sites-available/pytools-api
server {
    listen 80;
    server_name charts.myeden.com;
    
    location /api/ {
        proxy_pass http://127.0.0.1:5000/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时配置
        proxy_connect_timeout 30s;
        proxy_send_timeout 120s;
        proxy_read_timeout 120s;
        
        # 文件上传大小限制
        client_max_body_size 50M;
    }
    
    location /files/ {
        alias /var/lib/myeden/charts/;
        expires 1h;
        add_header Cache-Control "public, immutable";
    }
}
```

### Systemd服务配置

```ini
# /etc/systemd/system/pytools-api.service
[Unit]
Description=PyTools Chart API Service
After=network.target

[Service]
Type=exec
User=myeden
Group=myeden
WorkingDirectory=/opt/myeden/project/pytools/src
Environment=PATH=/opt/pytools/venv/bin
ExecStart=/opt/pytools/venv/bin/gunicorn -c gunicorn.conf.py api_server:app
ExecReload=/bin/kill -s HUP $MAINPID
Restart=always
RestartSec=5

# 日志配置
StandardOutput=journal
StandardError=journal

# 安全配置
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ReadWritePaths=/var/lib/myeden/charts
ReadWritePaths=/tmp

[Install]
WantedBy=multi-user.target
```

启动服务:
```bash
sudo systemctl daemon-reload
sudo systemctl enable pytools-api
sudo systemctl start pytools-api
sudo systemctl status pytools-api
```

## 🐳 Docker部署

### Dockerfile

```dockerfile
# project/pytools/Dockerfile
FROM python:3.11-slim

# 安装系统依赖
RUN apt-get update && apt-get install -y \
    fonts-wqy-zenhei \
    fonts-wqy-microhei \
    && rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /app

# 复制依赖文件
COPY requirements*.txt ./

# 安装Python依赖
RUN pip install --no-cache-dir -r requirements-api.txt

# 复制源代码
COPY src/ ./src/
COPY examples/ ./examples/

# 创建输出目录
RUN mkdir -p /app/output

# 设置环境变量
ENV PYTHONPATH=/app/src
ENV FLASK_APP=src/api_server.py

# 暴露端口
EXPOSE 5000

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:5000/api/health || exit 1

# 启动命令
CMD ["gunicorn", "-c", "gunicorn.conf.py", "src.api_server:app"]
```

### docker-compose.yml

```yaml
version: '3.8'

services:
  pytools-api:
    build:
      context: ./project/pytools
      dockerfile: Dockerfile
    ports:
      - "5000:5000"
    volumes:
      - charts_data:/app/output
    environment:
      - FLASK_ENV=production
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:5000/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - charts_data:/var/www/charts:ro
    depends_on:
      - pytools-api
    restart: unless-stopped

volumes:
  charts_data:
```

## 📊 监控和日志

### 日志配置

#### Python日志

```python
# src/logging_config.py
import logging
import logging.handlers
import os

def setup_logging():
    log_dir = os.getenv('LOG_DIR', '/var/log/myeden')
    os.makedirs(log_dir, exist_ok=True)
    
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=[
            logging.handlers.RotatingFileHandler(
                os.path.join(log_dir, 'pytools.log'),
                maxBytes=10*1024*1024,  # 10MB
                backupCount=5
            ),
            logging.StreamHandler()
        ]
    )
```

#### Java日志配置

```yaml
# application-chart.yml
logging:
  level:
    com.myeden.service.impl.ChartGenerationServiceImpl: INFO
    com.myeden.controller.ChartController: INFO
  file:
    name: logs/chart-service.log
  pattern:
    file: '%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n'
```

### 监控指标

#### Prometheus配置

```python
# src/metrics.py
from prometheus_client import Counter, Histogram, Gauge, start_http_server

# 指标定义
CHART_REQUESTS_TOTAL = Counter('chart_requests_total', 'Total chart requests', ['engine', 'chart_type', 'status'])
CHART_REQUEST_DURATION = Histogram('chart_request_duration_seconds', 'Chart request duration')
ACTIVE_SESSIONS = Gauge('chart_active_sessions', 'Active chart sessions')

def record_chart_request(engine, chart_type, status, duration):
    CHART_REQUESTS_TOTAL.labels(engine=engine, chart_type=chart_type, status=status).inc()
    CHART_REQUEST_DURATION.observe(duration)
```

## ⚡ 性能优化

### Python性能优化

```python
# src/performance_config.py
import matplotlib
matplotlib.use('Agg')  # 使用非GUI后端

# 启用缓存
import matplotlib.pyplot as plt
plt.ioff()  # 关闭交互模式

# 优化字体缓存
import matplotlib.font_manager
matplotlib.font_manager._rebuild()
```

### 系统级优化

```bash
# 增加文件句柄限制
echo "myeden soft nofile 65535" >> /etc/security/limits.conf
echo "myeden hard nofile 65535" >> /etc/security/limits.conf

# 优化Python GC
export PYTHONHASHSEED=0
export PYTHONOPTIMIZE=1

# 设置临时目录
export TMPDIR=/tmp/pytools
mkdir -p $TMPDIR
```

## 🔒 安全配置

### 文件权限

```bash
# 设置适当的文件权限
find /opt/myeden -type f -name "*.py" -exec chmod 644 {} \;
find /opt/myeden -type d -exec chmod 755 {} \;
chmod 755 /opt/myeden/project/pytools/src/cli.py
```

### 网络安全

```bash
# 防火墙配置 (仅内部访问API)
sudo ufw allow from 10.0.0.0/8 to any port 5000
sudo ufw deny 5000
```

### 输入验证

```python
# 在api_server.py中已实现
- 文件大小限制
- 路径安全检查
- 输入参数验证
- 会话管理
```

## 🧪 测试部署

### 自动化测试脚本

```bash
#!/bin/bash
# test_deployment.sh

echo "测试Python环境..."
python --version || exit 1

echo "测试依赖安装..."
python -c "import matplotlib, plotly, seaborn; print('依赖OK')" || exit 1

echo "测试命令行接口..."
cd project/pytools/src
python cli.py help || exit 1

echo "测试图表生成..."
python cli.py create --engine matplotlib --type line --config ../examples/sample_configs/line_config.json || exit 1

echo "测试Java集成..."
curl -s -X GET http://localhost:38081/api/v1/charts/status | grep -q '"status":"ok"' || exit 1

echo "所有测试通过！"
```

## 📈 扩展性考虑

### 集群部署

```yaml
# docker-compose.cluster.yml
version: '3.8'

services:
  pytools-api-1:
    extends:
      file: docker-compose.yml
      service: pytools-api
    container_name: pytools-api-1
    
  pytools-api-2:
    extends:
      file: docker-compose.yml  
      service: pytools-api
    container_name: pytools-api-2
    
  nginx:
    image: nginx:alpine
    volumes:
      - ./nginx-cluster.conf:/etc/nginx/nginx.conf:ro
    depends_on:
      - pytools-api-1
      - pytools-api-2
    ports:
      - "80:80"
```

### 负载均衡配置

```nginx
# nginx-cluster.conf
upstream pytools_backend {
    server pytools-api-1:5000 weight=1 max_fails=3 fail_timeout=30s;
    server pytools-api-2:5000 weight=1 max_fails=3 fail_timeout=30s;
    
    # 健康检查
    keepalive 32;
}

server {
    listen 80;
    
    location /api/ {
        proxy_pass http://pytools_backend/api/;
        # ... 其他配置
    }
}
```

## 🚨 故障排查

### 常见问题

1. **Python依赖问题**
```bash
# 重新安装依赖
pip install --force-reinstall -r requirements.txt
```

2. **字体问题**
```bash
# 清除matplotlib缓存
rm -rf ~/.cache/matplotlib
python -c "import matplotlib.font_manager; matplotlib.font_manager._rebuild()"
```

3. **权限问题**
```bash
# 检查文件权限
ls -la /opt/myeden/project/pytools/src/cli.py
chmod +x /opt/myeden/project/pytools/src/cli.py
```

4. **内存不足**
```bash
# 增加交换空间
sudo dd if=/dev/zero of=/swapfile bs=1024 count=2048000
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

### 日志查看

```bash
# Java后端日志
tail -f logs/myeden.log | grep -i chart

# Python API日志
journalctl -u pytools-api -f

# Nginx日志
tail -f /var/log/nginx/access.log
```

这套完整的部署方案提供了从开发到生产的全流程支持，可以根据实际需求选择合适的部署方式。