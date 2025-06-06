FROM openjdk:23-jdk-slim

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 设置 Tomcat 版本
ENV TOMCAT_VERSION=11.0.1
ENV CATALINA_HOME=/usr/local/tomcat
ENV PATH=$CATALINA_HOME/bin:$PATH

# 创建 tomcat 用户
RUN groupadd -r tomcat && useradd -r -g tomcat tomcat

# 下载并安装 Tomcat 11
RUN mkdir -p "$CATALINA_HOME" \
    && wget -O tomcat.tar.gz "https://archive.apache.org/dist/tomcat/tomcat-11/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz" \
    && tar -xzf tomcat.tar.gz --strip-components=1 -C "$CATALINA_HOME" \
    && rm tomcat.tar.gz \
    && chown -R tomcat:tomcat "$CATALINA_HOME" \
    && chmod +x "$CATALINA_HOME"/bin/*.sh

# 设置工作目录
WORKDIR $CATALINA_HOME

# 暴露端口
EXPOSE 8080

# 切换到 tomcat 用户
USER tomcat

# 启动 Tomcat
CMD ["catalina.sh", "run"]