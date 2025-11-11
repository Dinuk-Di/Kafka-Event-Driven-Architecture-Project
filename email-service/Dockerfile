# STAGE 1: Build with Maven & Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# --- Process base-domains ---
# 1. Copy its pom and download dependencies
COPY base-domains/pom.xml ./base-domains/pom.xml
RUN mvn -f base-domains/pom.xml dependency:go-offline -B

# 2. Copy its source code
COPY base-domains ./base-domains

# 3. Build and install it to the container's local .m2 repo
RUN mvn -f base-domains/pom.xml clean install

# --- Process order-service ---
# 4. Copy its pom and download dependencies
#    (This will find base-domains in the local .m2 repo)
COPY email-service/pom.xml ./email-service/pom.xml
RUN mvn -f email-service/pom.xml dependency:go-offline -B

# 5. Copy its source code
COPY email-service ./email-service

# 6. Build and package the final application
RUN mvn -f email-service/pom.xml clean package

# STAGE 2: Create final lightweight Java 21 runtime image
FROM eclipse-temurin:21-jre-alpine

# Install curl, which is needed for the HEALTHCHECK
RUN apk add --no-cache curl

WORKDIR /app

EXPOSE 8083

# Copy the built JAR from the 'builder' stage
# !! Make sure this JAR name matches what is in your target folder !!
COPY --from=builder /app/email-service/target/email-service-0.0.1-SNAPSHOT.jar app.jar

# Healthcheck to see if the Spring Boot app is ready
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]