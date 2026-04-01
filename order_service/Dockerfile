FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY order_service/build/libs/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
