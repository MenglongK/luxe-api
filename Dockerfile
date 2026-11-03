# Stage 1: Build the application using the project's Gradle Wrapper
FROM eclipse-temurin:25-jdk-jammy AS builder
WORKDIR /app

# Copy the Gradle wrapper files
COPY gradlew .
COPY gradle ./gradle
RUN chmod +x ./gradlew

# Copy Gradle configuration files
COPY build.gradle settings.gradle gradle.properties ./

# Copy all project modules
COPY application ./application
COPY core-feature ./core-feature
COPY payment ./payment
COPY security ./security

# Build only the application bootJar, forcing IPv4 & TLS protocols to prevent network drops
RUN ./gradlew :application:bootJar -x test --no-daemon \
    -Djava.net.preferIPv4Stack=true \
    -Dhttps.protocols=TLSv1.2,TLSv1.3 \
    && cp $(find /app/application/build/libs -name "*.jar" ! -name "*-plain.jar" | head -n 1) /app/app.jar

# Stage 2: Highly Optimized Production Runtime
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

# Create a non-root user for security
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Copy the single compiled executable jar from Stage 1
COPY --from=builder --chown=spring:spring /app/app.jar app.jar

EXPOSE 8080

# JVM Tuning optimized specifically for shared, low-core environments
ENTRYPOINT ["java", \
  "-XX:+UseSerialGC", \
  "-XX:TieredStopAtLevel=1", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Xss512k", \
  "-jar", "app.jar"]
