FROM openjdk:23-jdk-slim
COPY ../target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
