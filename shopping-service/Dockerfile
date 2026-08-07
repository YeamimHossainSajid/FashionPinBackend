# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY . .
RUN ./mvnw -pl shopping-service -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S fashionpin && adduser -S fashionpin -G fashionpin
USER fashionpin
COPY --from=build /workspace/shopping-service/target/shopping-service-*.jar app.jar
EXPOSE 8094
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
