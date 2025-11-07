FROM maven:3.9-eclipse-temurin-22 AS build
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]