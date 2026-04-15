# 使用多阶段构建

# 第一阶段：构建阶段
FROM maven:3.8-openjdk-8-slim AS builder

# 设置工作目录
WORKDIR /build

# 复制 pom.xml 和源代码
COPY pom.xml .
COPY src ./src

# 构建项目
RUN mvn clean package -DskipTests

# 第二阶段：运行阶段
FROM openjdk:8-jdk-alpine

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=builder /build/target/qrcode-generator-1.0.0.jar app.jar

# 暴露应用端口
EXPOSE 8080

# 设置 JVM 参数（可根据需要调整）
# 包含 JVM 版本信息输出和常用优化参数
ENV JAVA_OPTS="-Xms512m -Xmx512m -XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCApplicationStoppedTime -XX:+PrintFlagsFinal -XshowSettings:vm -version"

# 设置时区
ENV TZ=Asia/Shanghai

# 启动应用
ENTRYPOINT ["sh", "-c", "echo 'JVM Version:' && java -version && echo '' && echo 'Starting application...' && java $JAVA_OPTS -jar app.jar"]
