FROM gradle:8.13-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :notification_service:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/notification_service/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
