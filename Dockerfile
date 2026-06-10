# syntax=docker/dockerfile:1

# ---- Build stage: compile the Vaadin production frontend + Spring Boot jar ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

# Copy the whole project and build the production artifact.
# -Pproduction runs the Vaadin build-frontend goal (bundles the optimized frontend).
# Tests are skipped here: they spin up embedded Mongo / browserless and are run in CI, not in the image build.
COPY . .
RUN mvn -B -Pproduction clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /build/target/server-*.jar app.jar

EXPOSE 8080
ENV PORT=8080

# MaxRAMPercentage lets the JVM size its heap from the container memory limit.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
