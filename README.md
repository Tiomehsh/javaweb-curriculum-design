# Java Web 课程设计项目

## 项目简介

这是一个基于Jakarta EE的Java Web应用程序，实现了校园接待预约系统。

### 技术栈
- **后端**: Java 23, Jakarta EE, JAX-RS
- **前端**: JSP, JavaScript, CSS
- **数据库**: PostgreSQL (兼容OpenGauss)
- **构建工具**: Maven
- **应用服务器**: Tomcat 11.0+
- **反向代理**: Caddy

## 功能特性

### 公共功能
- 在线预约接待
- 预约查询
- 二维码生成

### 管理功能
- 管理员登录/登出
- 预约管理
- 部门管理
- 系统日志
- 用户权限管理

## 项目结构

```
javaweb-curriculum-design/
├── src/
│   ├── main/
│   │   ├── java/com/example/javawebcurriculumdesign/
│   │   │   ├── controller/     # REST API控制器
│   │   │   ├── dao/           # 数据访问层
│   │   │   ├── model/         # 数据模型
│   │   │   ├── service/       # 业务逻辑层
│   │   │   ├── filter/        # 过滤器
│   │   │   └── util/          # 工具类
│   │   ├── resources/         # 配置文件
│   │   └── webapp/           # Web资源
│   │       ├── admin/        # 管理后台页面
│   │       ├── public/       # 公共页面
│   │       ├── css/          # 样式文件
│   │       └── js/           # JavaScript文件
├── docker-compose.yml         # Docker编排配置
├── Caddyfile                 # Caddy配置文件
└── init.sql                  # 数据库初始化脚本
```

---

# 服务器部署指南

## 环境要求
- Java 23
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 或 OpenGauss 数据库

## 快速部署

### 1. 安装 Java 23
```bash
# 下载并安装 OpenJDK 23
sudo mkdir -p /opt/java
cd /tmp
wget https://download.java.net/java/GA/jdk23/3c5b90190c68498b986a97f276efd28a/37/GPL/openjdk-23_linux-x64_bin.tar.gz
sudo tar -xzf openjdk-23_linux-x64_bin.tar.gz -C /opt/java
sudo ln -sf /opt/java/jdk-23 /opt/java/current

# 配置环境变量
export JAVA_HOME=/opt/java/current
export PATH=$JAVA_HOME/bin:$PATH
sudo update-alternatives --install /usr/bin/java java /opt/java/current/bin/java 1
sudo update-alternatives --install /usr/bin/javac javac /opt/java/current/bin/javac 1

# 验证安装
java -version
```

### 2. 安装 Maven 和 Docker
```bash
# 安装 Maven
sudo apt update
sudo apt install maven -y

# 安装 Docker 和 Docker Compose
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker $USER

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 3. 部署应用
```bash
# 克隆项目
git clone https://github.com/Tiomehsh/javaweb-curriculum-design.git
cd javaweb-curriculum-design

# 配置数据库连接
cp .env.example .env
nano .env  # 填入您的数据库配置

# 编译项目
mvn clean package -DskipTests

# 构建并启动服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps
docker-compose logs -f
```

### 4. 访问应用
- **本地访问**: http://服务器IP:8080/
- **域名访问**: https://your-domain.com/ (配置域名后)
- **管理后台**: /admin/login.jsp

## 常用命令

### 服务管理
```bash
# 停止服务
docker-compose down

# 重启服务
docker-compose restart app

# 查看日志
docker-compose logs -f app

# 更新应用
git pull
mvn clean package -DskipTests
docker-compose up -d
```

### 故障排除
```bash
# 检查应用状态
curl -I http://localhost:8080/public/

# 查看容器资源使用
docker stats

# 清理无用镜像
docker system prune -f
```

## 数据库配置

在 `.env` 文件中配置数据库连接：
```env
# 数据库配置
DB_HOST=your-database-server-ip
DB_PORT=5432
DB_NAME=JAVA_WEB
DB_USER=your-username
DB_PASSWORD=your-password

# Java JVM配置
JAVA_OPTS=-Xms512m -Xmx2048m

# 可选：域名配置
DOMAIN=your-domain.com
```

## 安全建议

```bash
# 配置防火墙
sudo ufw enable
sudo ufw allow ssh
sudo ufw allow 80
sudo ufw allow 443
sudo ufw allow 8080

# 定期维护
sudo apt update && sudo apt upgrade -y
docker system prune -f
```

部署完成后，您的 Java Web 应用就可以在服务器上正常运行了！
