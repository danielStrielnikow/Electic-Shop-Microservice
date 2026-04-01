FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY product_service/build/libs/*.jar app.jar
EXPOSE 8083 9090
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
