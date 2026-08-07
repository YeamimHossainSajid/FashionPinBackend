# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY . .
RUN ./mvnw -pl virtual-tryon-service -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S fashionpin && adduser -S fashionpin -G fashionpin
USER fashionpin
COPY --from=build /workspace/virtual-tryon-service/target/virtual-tryon-service-*.jar app.jar
EXPOSE 8092
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
