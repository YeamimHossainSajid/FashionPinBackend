# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY . .
RUN ./mvnw -pl inventory-service -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S fashionpin && adduser -S fashionpin -G fashionpin
USER fashionpin
COPY --from=build /workspace/inventory-service/target/inventory-service-*.jar app.jar
EXPOSE 8097
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
