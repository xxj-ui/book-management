# ============================================
# Stage 1: Build with Maven
# ============================================
FROM maven:3.9.4-eclipse-temurin-17 AS builder

WORKDIR /build

# 先复制 pom.xml，利用 Docker 缓存层
COPY pom.xml ./
COPY lombok.config ./

# 下载依赖（独立层，后续代码变动不会重新下载）
RUN mvn dependency:go-offline -B -DskipTests || true

# 复制源码并编译打包
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================
# Stage 2: Run with JRE
# ============================================
FROM eclipse-temurin:17-jre

WORKDIR /app

# 从 builder 阶段复制构建产物
COPY --from=builder /build/target/*.jar app.jar

# 复制 docker 环境配置文件
COPY src/main/resources/application.yml /app/config/application.yml
COPY src/main/resources/application-docker.yml /app/config/application-docker.yml

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=60s \
  CMD curl -s http://localhost:8080/api/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/app/config/"]
