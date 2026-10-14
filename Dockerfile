# ==============================================================================
# MULTI-STAGE DOCKERFILE: COUPON MANAGEMENT SYSTEM (CMS)
# Student: Rohit Thatikonda | Roll No: 23102A0050 | Serial No: 47
# Subject: DevOps Lab (Task 11: Docker Image Creation & Container Lifecycle)
# ==============================================================================

# ------------------------------------------------------------------------------
# STAGE 1: Compilation & Packaging (Maven Build Environment)
# ------------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

# Copy POM descriptor and source directory
COPY app/pom.xml .
COPY app/src ./src

# Build production executable Fat JAR (unit/acceptance tests verified in Jenkins Quality Gate)
RUN mvn clean package -DskipTests -B

# ------------------------------------------------------------------------------
# STAGE 2: Lightweight Production Runtime (Minimal Eclipse Temurin JRE)
# ------------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runner
WORKDIR /app

# Academic & Project Metadata Labels
LABEL maintainer="Rohit Thatikonda (23102A0050)" \
      project="Coupon Management System (CMS)" \
      course="B.E. Computer Engineering - DevOps Lab" \
      version="1.0" \
      description="Production container image for Coupon Management System"

# Security Hardening: Execute under non-root unprivileged service account
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

# Copy pre-packaged executable fat JAR from builder stage with proper ownership
COPY --from=builder --chown=appuser:appgroup /workspace/target/coupon-management-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose internal application HTTP port
EXPOSE 8080

# Production JVM and Spring Boot Configuration
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

# Native Docker Healthcheck against Spring Boot Actuator endpoint
HEALTHCHECK --interval=20s --timeout=3s --retries=3 --start-period=30s \
  CMD wget -qO- http://localhost:8080/actuator/health | grep '"status":"UP"' || exit 1

# Launch production Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
