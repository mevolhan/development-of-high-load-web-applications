# Используем официальный образ OpenJDK 17
FROM eclipse-temurin:17-jdk-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем jar-файл (собранный через Maven)
# Предполагается, что после сборки он лежит в target/1-lab-*.jar
COPY target/*.jar app.jar

# Открываем порт (по умолчанию 8080, но можно переопределить через PORT)
EXPOSE 8080

# Запускаем приложение, передавая переменную окружения PORT
ENTRYPOINT ["java", "-jar", "app.jar"]