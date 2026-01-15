FROM gradle:8.13-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :user_service:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/user_service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
