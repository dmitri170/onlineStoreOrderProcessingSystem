FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR файл из target/ (Maven) вместо build/libs/ (Gradle)
COPY target/*.jar app.jar

# Указываем точку входа
ENTRYPOINT ["java", "-jar", "app.jar"]