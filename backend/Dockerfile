# ═══════════════════════════════════════════════
# Vita Clínica — Dockerfile multi-stage
# ═══════════════════════════════════════════════

FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Instala Maven (não vem na imagem temurin)
RUN apk add --no-cache maven

# Cache de dependências (baixa só quando pom.xml mudar)
COPY pom.xml .
RUN mvn dependency:go-offline -q 2>/dev/null || true

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