FROM gradle:8.13-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :eureka_server:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/eureka_server/build/libs/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
