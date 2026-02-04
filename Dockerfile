# ======================
# STAGE 1: Build
# ======================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /build

# Copy pom.xml trước để cache dependency
COPY lab1/pom.xml ./pom.xml
RUN mvn dependency:go-offline

# Copy source code
COPY lab1/src ./src

# Build jar
RUN mvn clean package -DskipTests


# ======================
# STAGE 2: Run
# ======================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy jar từ stage build
COPY --from=build /build/target/*.jar app.jar

# Render dùng PORT động
EXPOSE 8080

# Run app
ENTRYPOINT ["java","-jar","app.jar"]
