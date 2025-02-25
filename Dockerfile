FROM eclipse-temurin:21-jdk-alpine-3.21 AS build
WORKDIR /app

# Copy Gradle files first (these change less frequently)
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

# Copy dependency definitions first
COPY build.gradle settings.gradle ./

# Download dependencies (this layer will be cached if build.gradle hasn't changed)
RUN ./gradlew dependencies --no-daemon

# Copy source code (this layer will only rebuild if source code changes)
COPY src ./src

# Build the application
RUN ./gradlew -x test build --no-daemon

FROM eclipse-temurin:21-jre-alpine-3.21
WORKDIR /app

# Create non-root user and necessary directories
RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup && \
    mkdir -p /app/logs /app/tmp && \
    chown -R appuser:appgroup /app

# Copy the jar from build stage and set permissions
COPY --from=build --chown=appuser:appgroup /app/build/libs/*.jar ./app.jar

# Switch to non-root user
USER appuser:appgroup

# Default Java options and Spring profile
ENV JAVA_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=n"
ENV SPRING_PROFILES_ACTIVE=dev

# Expose ports
EXPOSE 8080 5005

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/health || exit 1

# Simple CMD with environment variable and Spring profile
CMD ["sh", "-c", "java ${JAVA_OPTIONS} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar"]