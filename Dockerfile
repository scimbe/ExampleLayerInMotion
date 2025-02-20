# Build stage
FROM maven:3.9.4-eclipse-temurin-17-focal AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build the application and create the fat JAR
RUN mvn clean package assembly:single

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy only the fat JAR from build stage
COPY --from=build /app/target/*-jar-with-dependencies.jar ./motion-system.jar

# Create a non-root user for running the application
RUN useradd -m -u 1001 motionapp && \
    chown -R motionapp:motionapp /app

USER motionapp

# Configure JVM options
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75 -XX:MinRAMPercentage=50"

# Define entrypoint
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar motion-system.jar"]