# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY . .
RUN ./mvnw -pl image-processing-service -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S fashionpin && adduser -S fashionpin -G fashionpin
USER fashionpin
COPY --from=build /workspace/image-processing-service/target/image-processing-service-*.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
