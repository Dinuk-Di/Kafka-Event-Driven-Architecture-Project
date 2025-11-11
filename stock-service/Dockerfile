# STAGE 1: Build with Maven & Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# --- Process base-domains (This part is identical) ---
# 1. Copy its pom and download dependencies
COPY base-domains/pom.xml ./base-domains/pom.xml
RUN mvn -f base-domains/pom.xml dependency:go-offline -B

# 2. Copy its source code
COPY base-domains ./base-domains

# 3. Build and install it
RUN mvn -f base-domains/pom.xml clean install

# --- Process stock-service (This part is CHANGED) ---
# 4. Copy its pom and download dependencies
COPY stock-service/pom.xml ./stock-service/pom.xml
RUN mvn -f stock-service/pom.xml dependency:go-offline -B

# 5. Copy its source code
COPY stock-service ./stock-service

# 6. Build and package the final application
RUN mvn -f stock-service/pom.xml clean package

# STAGE 2: Create final lightweight Java 21 runtime image
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl
WORKDIR /app

# --- This port might be different for stock-service ---
EXPOSE 8081 

# --- This line is the most important change ---
# Copy the built JAR from the 'stock-service' target
COPY --from=builder /app/stock-service/target/stock-service-0.0.1-SNAPSHOT.jar app.jar

# --- Update the healthcheck port ---
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8086/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]