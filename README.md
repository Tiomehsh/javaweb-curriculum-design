# Java Web 课程设计项目

## 项目简介

这是一个基于Jakarta EE的Java Web应用程序，实现了校园接待预约系统。

### 技术栈
- **后端**: Java 23, Jakarta EE, JAX-RS
- **前端**: JSP, JavaScript, CSS
- **数据库**: PostgreSQL (兼容OpenGauss)
- **构建工具**: Maven
- **应用服务器**: Tomcat 10.1+
- **反向代理**: Caddy

## 快速开始

### 1. 环境要求
- Java 21+ (推荐Java 23)
- Maven 3.8+
- PostgreSQL 或 OpenGauss 数据库

### 2. 本地开发
```bash
# 克隆项目
git clone https://github.com/Tiomehsh/javaweb-curriculum-design.git
cd javaweb-curriculum-design

# 配置数据库连接（推荐使用环境变量）
cp .env.example .env
# 编辑.env文件，填入实际数据库配置

# 或者使用传统properties文件配置
cp src/main/resources/database.properties.example src/main/resources/database.properties
cp src/main/resources/crypto.properties.example src/main/resources/crypto.properties
# 然后编辑这两个文件

# 初始化数据库
psql -U your_username -d your_database -f init.sql

# 编译和打包
mvn clean package
```

**注意**: 应用会优先使用环境变量配置，如果环境变量未设置才会使用properties文件。

### 3. 快速部署脚本
项目提供了自动化部署脚本，支持三种模式：

```bash
# 本地部署
./deploy.sh local

# Docker部署
./deploy.sh docker

# 生产环境部署
./deploy.sh production
```

## 部署指南

详细的部署说明请参考 [部署指南.md](部署指南.md)，包含：

- 服务器环境配置
- Tomcat和Caddy配置
- 安全设置
- 监控和日志
- 故障排除

## Docker 部署

### 配置外部数据库
由于项目配置为使用外部数据库，请先配置数据库连接：

```bash
# 复制环境变量配置文件
cp .env.example .env

# 编辑 .env 文件，填入您的数据库配置
nano .env
```

### 使用Docker Compose
```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 服务端口和访问地址
- **应用**: http://localhost:8080
- **Caddy**: http://localhost:80, https://localhost:443

### 访问地址
- **首页**: http://localhost:8080/ (Docker) 或 https://your-domain.com/ (Caddy)
- **公共页面**: http://localhost:8080/public/
- **管理后台**: http://localhost:8080/admin/login.jsp

**注意**:
- 应用被部署为ROOT应用，访问路径中没有应用名称前缀
- 此配置使用外部数据库，请确保您的数据库服务器可以从Docker容器访问

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
├── Dockerfile                 # Docker镜像构建文件
├── Caddyfile                 # Caddy配置文件
├── deploy.sh                 # 自动部署脚本
├── init.sql                  # 数据库初始化脚本
└── 部署指南.md               # 详细部署指南
```

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

## API 接口

### 公共接口
- `GET /api/departments` - 获取部门列表
- `POST /api/appointments/public` - 创建公共预约
- `POST /api/appointments/official` - 创建公务预约
- `GET /api/appointments/query` - 查询预约信息

### 管理接口 (需要认证)
- `GET /api/admin/appointments` - 获取预约列表
- `PUT /api/admin/appointments/{id}` - 更新预约状态
- `GET /api/admin/logs` - 获取系统日志
- `POST /api/admin/departments` - 管理部门信息

## 安全特性

- Session管理和超时控制
- 密码强度验证
- SQL注入防护
- XSS防护
- CSRF防护
- 管理员权限控制

## 监控和日志

- 应用日志记录
- 系统操作日志
- 访问日志
- 错误日志
- 健康检查
