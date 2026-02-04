# ========================
# STAGE 1: Build JAR
# ========================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /build

# Copy pom.xml trong thư mục lab1
COPY lab1/pom.xml .

# Download dependencies trước (tối ưu cache)
RUN mvn dependency:go-offline

# Copy source code
COPY lab1/src ./src

# Build jar
RUN mvn clean package -DskipTests

# ========================
# STAGE 2: Run app
# ========================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy jar từ stage build
COPY --from=build /build/target/*.jar app.jar

# Render dùng biến PORT → không hardcode
EXPOSE 8080

# Start Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
