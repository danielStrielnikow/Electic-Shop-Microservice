FROM gradle:8.13-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean :config_server:bootJar --no-daemon --no-build-cache

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/config_server/build/libs/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
