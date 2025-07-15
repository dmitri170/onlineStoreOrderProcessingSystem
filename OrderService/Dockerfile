# Используем официальный JDK-образ
FROM openjdk:17-jdk-slim

# Указываем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем собранный jar-файл
COPY build/libs/*.jar app.jar

# Указываем точку входа
ENTRYPOINT ["java", "-jar", "app.jar"]
