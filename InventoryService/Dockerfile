# Используем базовый образ с JDK 17
FROM eclipse-temurin:17-jdk-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем скомпилированный .jar-файл внутрь контейнера
COPY target/InventoryService-0.0.1-SNAPSHOT.jar app.jar

# Открываем порты HTTP и gRPC
EXPOSE 8081
EXPOSE 9090

# Запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
