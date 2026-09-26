# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine AS build
RUN apk add --no-cache bash maven
WORKDIR /workspace
COPY . .
RUN mvn -pl media-service -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S fashionpin && adduser -S fashionpin -G fashionpin
USER fashionpin
COPY --from=build /workspace/media-service/target/media-service-*.jar app.jar
EXPOSE 8101
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
