# JAR jest już zbudowany przez GitHub Actions — tylko kopiujemy i uruchamiamy
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY payment_service/build/libs/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
