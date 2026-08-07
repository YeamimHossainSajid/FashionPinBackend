# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY . .
RUN ./mvnw -pl fashion-discovery-service -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S fashionpin && adduser -S fashionpin -G fashionpin
USER fashionpin
COPY --from=build /workspace/fashion-discovery-service/target/fashion-discovery-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
