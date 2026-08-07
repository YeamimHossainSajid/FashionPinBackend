# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY . .
RUN ./mvnw -pl moodboard-service -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S fashionpin && adduser -S fashionpin -G fashionpin
USER fashionpin
COPY --from=build /workspace/moodboard-service/target/moodboard-service-*.jar app.jar
EXPOSE 8093
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
