FROM gradle:8.13-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean :cart_service:bootJar --no-daemon --no-build-cache

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/cart_service/build/libs/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
