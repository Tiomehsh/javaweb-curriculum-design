#!/bin/bash

# Java Web 项目自动部署脚本
# 使用方法: ./deploy.sh [production|docker]

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        log_error "$1 命令未找到，请先安装"
        exit 1
    fi
}

# 项目配置
PROJECT_NAME="javaweb-curriculum-design"
WAR_NAME="${PROJECT_NAME}-1.0-SNAPSHOT.war"
DEPLOY_MODE=${1:-"production"}

log_info "开始部署 ${PROJECT_NAME}，模式: ${DEPLOY_MODE}"

# 检查必要工具
check_command "mvn"
if [ "$DEPLOY_MODE" = "docker" ]; then
    check_command "docker"
    check_command "docker-compose"
fi

# 清理和编译
log_info "清理项目..."
mvn clean

log_info "编译和打包项目..."
mvn package -DskipTests

# 检查WAR文件是否生成
if [ ! -f "target/${WAR_NAME}" ]; then
    log_error "WAR文件未生成: target/${WAR_NAME}"
    exit 1
fi

log_success "项目打包完成: target/${WAR_NAME}"

# 根据部署模式执行不同操作
case $DEPLOY_MODE in
    "production")
        log_info "生产环境部署模式"
        
        # 检查是否有服务器配置
        read -p "请输入服务器地址 (例: user@server-ip): " SERVER_ADDRESS
        read -p "请输入服务器上的部署路径 (默认: /opt/tomcat/webapps/): " DEPLOY_PATH
        DEPLOY_PATH=${DEPLOY_PATH:-"/opt/tomcat/webapps/"}
        
        if [ -z "$SERVER_ADDRESS" ]; then
            log_error "服务器地址不能为空"
            exit 1
        fi
        
        log_info "上传WAR文件到服务器..."
        scp "target/${WAR_NAME}" "${SERVER_ADDRESS}:/tmp/"
        
        log_info "在服务器上执行部署..."
        ssh "$SERVER_ADDRESS" << EOF
            # 停止Tomcat
            sudo systemctl stop tomcat
            
            # 备份旧版本
            if [ -f "${DEPLOY_PATH}${PROJECT_NAME}.war" ]; then
                sudo cp "${DEPLOY_PATH}${PROJECT_NAME}.war" "${DEPLOY_PATH}${PROJECT_NAME}-backup-\$(date +%Y%m%d-%H%M%S).war"
            fi
            
            # 清理旧部署
            sudo rm -rf "${DEPLOY_PATH}${PROJECT_NAME}"*
            
            # 部署新版本
            sudo cp "/tmp/${WAR_NAME}" "${DEPLOY_PATH}${PROJECT_NAME}.war"
            sudo chown tomcat:tomcat "${DEPLOY_PATH}${PROJECT_NAME}.war"
            
            # 启动Tomcat
            sudo systemctl start tomcat
            
            # 等待启动
            sleep 10
            
            # 检查状态
            sudo systemctl status tomcat --no-pager
EOF
        
        log_success "生产环境部署完成！"
        log_info "访问地址: https://your-domain.com/${PROJECT_NAME}/"
        ;;
        
    "docker")
        log_info "Docker部署模式"
        
        # 检查Docker和Docker Compose
        if ! docker info > /dev/null 2>&1; then
            log_error "Docker未运行，请启动Docker"
            exit 1
        fi
        
        log_info "构建Docker镜像..."
        docker-compose build
        
        log_info "启动服务..."
        docker-compose up -d
        
        log_info "等待服务启动..."
        sleep 30
        
        log_info "检查服务状态..."
        docker-compose ps
        
        # 健康检查
        log_info "执行健康检查..."
        if curl -f -s http://localhost:8080/ > /dev/null; then
            log_success "应用启动成功！"
            log_info "访问地址: http://localhost:8080/"
        else
            log_warning "应用可能还在启动中，请稍后检查"
            log_info "查看日志: docker-compose logs -f app"
        fi
        ;;
        
    "local")
        log_info "本地测试模式"
        
        # 检查是否有Tomcat
        if [ -d "/opt/tomcat" ]; then
            TOMCAT_HOME="/opt/tomcat"
        elif [ -d "/usr/local/tomcat" ]; then
            TOMCAT_HOME="/usr/local/tomcat"
        else
            log_error "未找到Tomcat安装目录"
            exit 1
        fi
        
        log_info "部署到本地Tomcat: ${TOMCAT_HOME}"
        
        # 停止Tomcat
        if pgrep -f "catalina" > /dev/null; then
            log_info "停止Tomcat..."
            sudo "${TOMCAT_HOME}/bin/shutdown.sh" || true
            sleep 5
        fi
        
        # 清理旧部署
        sudo rm -rf "${TOMCAT_HOME}/webapps/${PROJECT_NAME}"*
        
        # 部署新版本
        sudo cp "target/${WAR_NAME}" "${TOMCAT_HOME}/webapps/${PROJECT_NAME}.war"
        
        # 启动Tomcat
        log_info "启动Tomcat..."
        sudo "${TOMCAT_HOME}/bin/startup.sh"
        
        log_success "本地部署完成！"
        log_info "访问地址: http://localhost:8080/${PROJECT_NAME}/"
        ;;
        
    *)
        log_error "未知的部署模式: $DEPLOY_MODE"
        log_info "支持的模式: production, docker, local"
        exit 1
        ;;
esac

log_success "部署完成！"

# 显示有用的命令
echo ""
log_info "有用的命令:"
case $DEPLOY_MODE in
    "docker")
        echo "  查看日志: docker-compose logs -f"
        echo "  重启服务: docker-compose restart"
        echo "  停止服务: docker-compose down"
        echo "  查看状态: docker-compose ps"
        ;;
    "production"|"local")
        echo "  查看Tomcat日志: tail -f /opt/tomcat/logs/catalina.out"
        echo "  重启Tomcat: sudo systemctl restart tomcat"
        echo "  查看Tomcat状态: sudo systemctl status tomcat"
        ;;
esac
