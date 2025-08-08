# Use Maven with JDK 21 as builder image
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Use smaller JRE image for runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/target/Product-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 2000

ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=2000"]
