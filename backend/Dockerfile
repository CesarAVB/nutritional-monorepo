# ===============================
# STAGE 1 — BUILD
# ===============================
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY . .

RUN chmod +x ./mvnw \
 && ./mvnw -B -DskipTests clean package


# ===============================
# STAGE 2 — RUNTIME
# ===============================
FROM mcr.microsoft.com/playwright/java:v1.49.0-jammy

# Usa os browsers já provisionados pela imagem oficial do Playwright
# e evita downloads adicionais em runtime.
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=true

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]