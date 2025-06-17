FROM openjdk:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-alpine

WORKDIR /app

# Copy built JAR
COPY --from=builder /app/target/chat-backend-*.jar app.jar

# Expose ports
EXPOSE 8080 9090 8081

# Create non-root user
RUN addgroup -g 1001 -S chatapp && \
    adduser -S chatapp -u 1001

USER chatapp

# Run application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]