# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Forzamos a Maven a descargar dependencias Y plugins/procesadores con tolerancia a fallos de red
RUN mvn dependency:go-offline dependency:resolve-plugins -Dmaven.wagons.http.retryHandler.count=3
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Exponemos el puerto real donde escucha Spring Boot
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]