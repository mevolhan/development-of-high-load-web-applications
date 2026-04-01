# Используем образ Maven для сборки (многоступенчатая сборка)
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Второй этап: запуск приложения
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/lab3-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]