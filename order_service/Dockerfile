FROM gradle:8.13-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean :order_service:bootJar --no-daemon --no-build-cache

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/order_service/build/libs/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
