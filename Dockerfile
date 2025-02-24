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
COPY --from=build /app/build/libs/*.jar ./app.jar

# Default Java options
ENV JAVA_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

# Simple CMD with environment variable
CMD ["sh", "-c", "java ${JAVA_OPTIONS} -jar app.jar"]