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
FROM eclipse-temurin:21-jre

# Instala todas as dependências necessárias para Playwright/Chromium
RUN apt-get update && apt-get install -y --no-install-recommends \
    fonts-liberation \
    libnss3 \
    libnspr4 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libcups2 \
    libdrm2 \
    libxkbcommon0 \
    libxcomposite1 \
    libxrandr2 \
    libgbm1 \
    libpango-1.0-0 \
    libcairo2 \
    libx11-6 \
    libx11-xcb1 \
    libxcb1 \
    libxext6 \
    libxfixes3 \
    libxi6 \
    libxrender1 \
    libxss1 \
    libxtst6 \
    libxdamage1 \
    libasound2t64 \
    ca-certificates \
 && rm -rf /var/lib/apt/lists/*

# Deixa o Playwright gerenciar o Chromium automaticamente
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=false

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]