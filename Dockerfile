# ============================================================================
# Heronix SIS - Docker Configuration
# Multi-stage build for optimized production image
# ============================================================================

# ==========================================================================
# Stage 1: Build Stage
# ==========================================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS build

LABEL stage=builder
LABEL application=heronix-sis

WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds in Docker)
RUN mvn clean package -DskipTests -B

# ==========================================================================
# Stage 2: Runtime Stage
# ==========================================================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Heronix Educational Systems LLC"
LABEL application=heronix-sis
LABEL version=1.0.0

# Install utilities for health checks
RUN apk add --no-cache wget curl

# Create application user (non-root for security)
RUN addgroup -g 1000 heronix && \
    adduser -D -u 1000 -G heronix heronix

# Create application directories
RUN mkdir -p /app/data /app/logs /app/exports && \
    chown -R heronix:heronix /app

WORKDIR /app

# Copy JAR from build stage (artifact name from pom.xml)
COPY --from=build --chown=heronix:heronix \
    /app/target/heronix-scheduler-1.0.0.jar app.jar

# Switch to non-root user
USER heronix

# Expose ports (8080 HTTP, 8443 HTTPS)
EXPOSE 8080 8443

# Health check using Spring Actuator endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9590/actuator/health/readiness || exit 1

# ==========================================================================
# JVM OPTIONS - Optimized for 500+ concurrent connections
# ==========================================================================
# Memory: Use container-aware settings
# GC: G1GC tuned for low latency with high throughput
# Threading: Optimized for high concurrency
# Security: Secure random generation
# ==========================================================================
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:G1HeapRegionSize=16m \
    -XX:+UseStringDeduplication \
    -XX:+ParallelRefProcEnabled \
    -XX:+AlwaysPreTouch \
    -XX:+DisableExplicitGC \
    -XX:+UseNUMA \
    -XX:+OptimizeStringConcat \
    -XX:MetaspaceSize=256m \
    -XX:MaxMetaspaceSize=512m \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/logs/heapdump.hprof \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8 \
    -Djava.awt.headless=true \
    -Dspring.backgroundpreinitializer.ignore=true"

# Default Spring profile for production
ENV SPRING_PROFILES_ACTIVE=production

# Additional environment variables for tuning
ENV HIKARI_MAX_POOL_SIZE=100
ENV HIKARI_MIN_IDLE=20

# Run application with optimized settings
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# OCI Image Metadata
ARG BUILD_DATE
ARG VCS_REF
ARG VERSION=1.0.0

LABEL org.opencontainers.image.created=$BUILD_DATE \
      org.opencontainers.image.title="Heronix SIS API Server" \
      org.opencontainers.image.description="AI-Powered School Information System - REST API" \
      org.opencontainers.image.version=$VERSION \
      org.opencontainers.image.revision=$VCS_REF \
      org.opencontainers.image.vendor="Heronix Educational Systems LLC" \
      org.opencontainers.image.url="https://heronixedu.com" \
      org.opencontainers.image.licenses="Proprietary"
