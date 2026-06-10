# Stage 1 — build
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY confeitaria/pom.xml .
RUN mvn dependency:go-offline -B
COPY confeitaria/src ./src
RUN mvn clean package -DskipTests

# Stage 2 — runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/confeitaria-1.0.0.jar app.jar
COPY Imagens /app/Imagens
RUN mkdir -p /app/uploads /app/data
EXPOSE 8080
ENV APP_IMAGES_DIR=/app/Imagens
ENV APP_UPLOAD_DIR=/app/uploads
ENTRYPOINT ["java", "-jar", "app.jar"]
