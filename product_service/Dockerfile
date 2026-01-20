FROM gradle:8.13-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean :product_service:bootJar --no-daemon --no-build-cache

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/product_service/build/libs/*.jar app.jar
EXPOSE 8083 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
