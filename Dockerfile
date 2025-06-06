FROM tomcat:10.1-jdk21

# 设置环境变量
ENV CATALINA_HOME /usr/local/tomcat
ENV PATH $CATALINA_HOME/bin:$PATH

# 创建工作目录
WORKDIR $CATALINA_HOME

# 删除默认的webapps内容
RUN rm -rf /usr/local/tomcat/webapps/*

# 复制WAR文件
COPY target/javaweb-curriculum-design-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# 复制配置文件
COPY src/main/resources/database.properties /usr/local/tomcat/conf/

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC -XX:+UseStringDeduplication"

# 暴露端口
EXPOSE 8080

# 启动Tomcat
CMD ["catalina.sh", "run"]
