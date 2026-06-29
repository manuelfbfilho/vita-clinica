# ═══════════════════════════════════════════════
# Vita Clínica — Dockerfile multi-stage
# Stage 1: Build com Maven
# Stage 2: Runtime mínimo com JRE Alpine
# ═══════════════════════════════════════════════

FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Cache de dependências Maven (camada separada)
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -q 2>/dev/null || true

# Build do projeto
COPY src ./src
RUN mvn clean package -DskipTests -q

# ═══════════════════════════════════════════════
# Stage 2: Runtime mínimo
# ═══════════════════════════════════════════════
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S vitaclinica && adduser -S vitaclinica -G vitaclinica
USER vitaclinica

COPY --from=builder /app/target/agendamento-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
