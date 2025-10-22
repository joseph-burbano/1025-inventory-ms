# syntax=docker/dockerfile:1

# Build stage
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Create volume directory for H2 database
RUN mkdir -p /app/data

# Copy jar from build stage
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]


# docker-compose.yml
version: '3.8'
services:
inventory-ms:
build: .
ports:
- "8080:8080"
volumes:
- ./logs:/app/logs
environment:
- SPRING_PROFILES_ACTIVE=default