FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY user_service/build/libs/*.jar app.jar
EXPOSE 8081 9091
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
