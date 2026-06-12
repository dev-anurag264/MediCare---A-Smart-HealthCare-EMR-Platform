# DOCKERFILE — Smart Healthcare Platform
# Multi-stage build — two separate stages in one file:
#
# STAGE 1 (builder): Uses a full JDK image to compile and package the app
# STAGE 2 (runtime): Uses a minimal JRE image to run it
#
# WHY multi-stage?
# The JDK image (~600MB) contains compilers, build tools, source files.
# None of that is needed to RUN the app — only to BUILD it.
# The final image only contains the JRE + your JAR (~250MB total).
# Smaller image = faster deployment, less attack surface, lower storage cost.


# ── STAGE 1: Build ───────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Set working directory inside the container
WORKDIR /app

# ── Copy pom.xml first (before source code) ──────────────────────
# WHY? Docker caches each layer. If pom.xml hasn't changed,
# Docker skips the 'mvn dependency:go-offline' step entirely.
# This makes rebuilds after code changes MUCH faster.
# If you copy everything at once, any code change invalidates
# the dependency cache and re-downloads all dependencies.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# ── Now copy source code and build ───────────────────────────────
COPY src ./src
RUN mvn clean package -DskipTests -B
# -DskipTests: don't run tests during Docker build (run them in CI separately)
# -B: batch mode (no interactive prompts, cleaner logs)

# ── STAGE 2: Runtime ─────────────────────────────────────────────
# eclipse-temurin:17-jre-alpine
# eclipse-temurin: OpenJDK distribution from Adoptium (production-grade)
# 17: Java 17 (matches our project)
# jre: runtime only, no compiler (smaller)
# alpine: minimal Linux (~5MB base OS vs ~200MB for Ubuntu)
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Create a non-root user to run the application
# WHY? Running as root inside a container is a security risk.
# If the app is compromised, the attacker gets root inside the container.
# Running as 'appuser' limits the blast radius.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create uploads directory and give ownership to appuser
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

# Copy only the JAR from the builder stage
# The wildcard handles version numbers in the JAR name
COPY --from=builder /app/target/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose the port Spring Boot runs on
EXPOSE 8080

# ── JVM tuning for containers ─────────────────────────────────────
# Without these flags, the JVM reads the HOST machine's RAM
# (e.g., 16GB) and allocates a huge heap — crashing the container.
# -XX:+UseContainerSupport: tells JVM to respect container memory limits
# -XX:MaxRAMPercentage=75.0: use max 75% of container's memory for heap
# -Djava.security.egd: faster random number generation (speeds up startup)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]