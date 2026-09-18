# Build stage
FROM maven:3.9.9-eclipse-temurin-17 as build
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src ./src
RUN chmod +x mvnw && ./mvnw -B clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/lab1-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8081} -jar /app/app.jar"]
