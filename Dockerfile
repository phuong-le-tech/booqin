# Image unique pour la demo (Railway) : Spring Boot sert l'API et le front React.

FROM node:22-slim AS frontend
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm install
COPY frontend/ ./
# Shows the demo notice and demo logins in the UI
ENV VITE_DEMO=true
RUN npm run build

FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /build
COPY pom.xml ./
RUN mvn -q dependency:go-offline
COPY src ./src
COPY --from=frontend /frontend/dist ./src/main/resources/static
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /build/target/booqIn-1.0.0-SNAPSHOT.jar app.jar
# Chemin relatif lu par l'appli (novelists.file.path)
COPY src/main/resources/novelists.txt src/main/resources/novelists.txt
# Donnees de demo (base H2 + images), copiees dans le volume au premier demarrage
COPY docker/data /seed

ENV SPRING_PROFILES_ACTIVE=demo
EXPOSE 8080
CMD ["sh", "-c", "mkdir -p data && [ -f data/database.mv.db ] || cp -r /seed/. data/; exec java -XX:MaxRAMPercentage=75 -jar app.jar"]
