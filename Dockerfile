# Multi-stage Dockerfile for Repository Maintainability Index
# Optimized for enterprise production deployment

# ================================
# Stage 1: Build Stage
# ================================
FROM maven:3.8-openjdk-17-slim AS build

# Set working directory
WORKDIR /build

# Copy pom.xml first for dependency caching
COPY pom.xml ./

# Download dependencies (cached if unchanged)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src/ ./src/

# Build the application
RUN mvn clean package -DskipTests -Dmaven.test.skip=true

# ================================
# Stage 2: Runtime Stage (Production)
# ================================
FROM eclipse-temurin:17-jdk-alpine AS runtime

# Add metadata labels for production
LABEL maintainer="JungleKIA Team" \
      version="1.0.0" \
      description="Repository Maintainability Index - Enterprise GitHub repository analysis tool"

    # Create non-root user for security (Alpine Linux)
RUN addgroup -S rmiuser && adduser -S rmiuser -G rmiuser

# Create directories for app and logs
WORKDIR /app
RUN mkdir -p /app/logs && \
    chown -R rmiuser:rmiuser /app

# Copy JAR from build stage
COPY --from=build /build/target/repo-maintainability-index-*.jar /app/rmi-app.jar

# Change ownership
RUN chown rmiuser:rmiuser /app/rmi-app.jar

# Switch to non-root user
USER rmiuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD java -jar /app/rmi-app.jar --help > /dev/null 2>&1 || exit 1

# Default entrypoint
ENTRYPOINT ["java", "-jar", "/app/rmi-app.jar"]

# Default command (can be overridden)
CMD ["--help"]
