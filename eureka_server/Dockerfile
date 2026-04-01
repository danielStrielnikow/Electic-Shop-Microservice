FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY eureka_server/build/libs/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
