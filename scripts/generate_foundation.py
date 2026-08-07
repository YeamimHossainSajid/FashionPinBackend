#!/usr/bin/env python3
"""Generate Fashion Pin microservices foundation (structure + placeholders only)."""

from __future__ import annotations

import os
from pathlib import Path
from textwrap import dedent

ROOT = Path(__file__).resolve().parents[1]

GROUP_ID = "com.fashionpin"
PARENT_ARTIFACT = "fashion-pin"
VERSION = "1.0.0-SNAPSHOT"
JAVA_VERSION = "21"
SPRING_BOOT_VERSION = "3.3.5"
SPRING_CLOUD_VERSION = "2023.0.3"
MAPSTRUCT_VERSION = "1.5.5.Final"
LOMBOK_VERSION = "1.18.34"
GRPC_SPRING_VERSION = "3.1.0.RELEASE"
SPRINGDOC_VERSION = "2.6.0"

INFRA_SERVICES = [
    ("discovery-service", 8761, False, False),
    ("config-server", 8888, False, False),
    ("api-gateway", 8080, False, False),
]

# name, port, db_name (or None), needs_jpa
BUSINESS_SERVICES = [
    ("auth-service", 8081, "auth_db", True),
    ("user-service", 8082, "user_db", True),
    ("profile-service", 8083, "profile_db", True),
    ("fashion-discovery-service", 8084, "fashion_discovery_db", True),
    ("product-service", 8085, "product_db", True),
    ("search-service", 8086, "search_db", True),
    ("recommendation-service", 8087, "recommendation_db", True),
    ("outfit-detection-service", 8088, "outfit_detection_db", True),
    ("image-processing-service", 8089, "image_processing_db", True),
    ("visual-search-service", 8090, "visual_search_db", True),
    ("ai-stylist-service", 8091, "ai_stylist_db", True),
    ("virtual-tryon-service", 8092, "virtual_tryon_db", True),
    ("moodboard-service", 8093, "moodboard_db", True),
    ("shopping-service", 8094, "shopping_db", True),
    ("order-service", 8095, "order_db", True),
    ("payment-service", 8096, "payment_db", True),
    ("inventory-service", 8097, "inventory_db", True),
    ("brand-integration-service", 8098, "brand_integration_db", True),
    ("notification-service", 8099, "notification_db", True),
    ("analytics-service", 8100, "analytics_db", True),
    ("media-service", 8101, "media_db", True),
]

STANDARD_PACKAGES = [
    "config",
    "controller",
    "service",
    "repository",
    "entity",
    "dto",
    "mapper",
    "security",
    "exception",
    "client",
    "event",
    "kafka",
    "grpc",
    "util",
    "validation",
    "health",
]


def write(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(dedent(content).lstrip("\n") if content.startswith("\n") else content, encoding="utf-8")
    if not content.endswith("\n"):
        path.write_text(path.read_text(encoding="utf-8") + "\n", encoding="utf-8")


def touch_package(base: Path, packages: list[str]) -> None:
    for pkg in packages:
        pkg_dir = base / pkg
        pkg_dir.mkdir(parents=True, exist_ok=True)
        write(pkg_dir / ".gitkeep", "")


def package_name(service: str) -> str:
    return service.replace("-", "")


def class_prefix(service: str) -> str:
    return "".join(part.capitalize() for part in service.split("-"))


def parent_pom() -> None:
    modules = ["common-lib"] + [s[0] for s in INFRA_SERVICES] + [s[0] for s in BUSINESS_SERVICES]
    modules_xml = "\n".join(f"        <module>{m}</module>" for m in modules)
    write(
        ROOT / "pom.xml",
        f"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>{SPRING_BOOT_VERSION}</version>
        <relativePath/>
    </parent>

    <groupId>{GROUP_ID}</groupId>
    <artifactId>{PARENT_ARTIFACT}</artifactId>
    <version>{VERSION}</version>
    <packaging>pom</packaging>
    <name>Fashion Pin</name>
    <description>Fashion Pin microservices foundation</description>

    <modules>
{modules_xml}
    </modules>

    <properties>
        <java.version>{JAVA_VERSION}</java.version>
        <spring-cloud.version>{SPRING_CLOUD_VERSION}</spring-cloud.version>
        <mapstruct.version>{MAPSTRUCT_VERSION}</mapstruct.version>
        <lombok.version>{LOMBOK_VERSION}</lombok.version>
        <grpc-spring-boot.version>{GRPC_SPRING_VERSION}</grpc-spring-boot.version>
        <springdoc.version>{SPRINGDOC_VERSION}</springdoc.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${{spring-cloud.version}}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>{GROUP_ID}</groupId>
                <artifactId>common-lib</artifactId>
                <version>${{project.version}}</version>
            </dependency>
            <dependency>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct</artifactId>
                <version>${{mapstruct.version}}</version>
            </dependency>
            <dependency>
                <groupId>net.devh</groupId>
                <artifactId>grpc-spring-boot-starter</artifactId>
                <version>${{grpc-spring-boot.version}}</version>
            </dependency>
            <dependency>
                <groupId>net.devh</groupId>
                <artifactId>grpc-server-spring-boot-starter</artifactId>
                <version>${{grpc-spring-boot.version}}</version>
            </dependency>
            <dependency>
                <groupId>net.devh</groupId>
                <artifactId>grpc-client-spring-boot-starter</artifactId>
                <version>${{grpc-spring-boot.version}}</version>
            </dependency>
            <dependency>
                <groupId>org.springdoc</groupId>
                <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
                <version>${{springdoc.version}}</version>
            </dependency>
            <dependency>
                <groupId>org.springdoc</groupId>
                <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
                <version>${{springdoc.version}}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <configuration>
                        <excludes>
                            <exclude>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok</artifactId>
                            </exclude>
                        </excludes>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <configuration>
                        <release>${{java.version}}</release>
                        <annotationProcessorPaths>
                            <path>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok</artifactId>
                                <version>${{lombok.version}}</version>
                            </path>
                            <path>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok-mapstruct-binding</artifactId>
                                <version>0.2.0</version>
                            </path>
                            <path>
                                <groupId>org.mapstruct</groupId>
                                <artifactId>mapstruct-processor</artifactId>
                                <version>${{mapstruct.version}}</version>
                            </path>
                        </annotationProcessorPaths>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
""",
    )


def common_lib() -> None:
    base = ROOT / "common-lib"
    java = base / "src/main/java/com/fashionpin/common"
    write(
        base / "pom.xml",
        f"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>{GROUP_ID}</groupId>
        <artifactId>{PARENT_ARTIFACT}</artifactId>
        <version>{VERSION}</version>
    </parent>
    <artifactId>common-lib</artifactId>
    <name>common-lib</name>
    <description>Shared foundation library for Fashion Pin services</description>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
    </dependencies>
</project>
""",
    )

    write(
        java / "exception/ApiError.java",
        """
        package com.fashionpin.common.exception;

        import com.fasterxml.jackson.annotation.JsonInclude;
        import java.time.Instant;
        import java.util.List;
        import lombok.Builder;
        import lombok.Value;

        @Value
        @Builder
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public class ApiError {
            Instant timestamp;
            int status;
            String error;
            String message;
            String path;
            String correlationId;
            List<String> details;
        }
        """,
    )
    write(
        java / "exception/ErrorResponse.java",
        """
        package com.fashionpin.common.exception;

        import java.time.Instant;
        import java.util.List;
        import lombok.Builder;
        import lombok.Value;

        @Value
        @Builder
        public class ErrorResponse {
            Instant timestamp;
            String code;
            String message;
            List<String> details;
            String path;
            String correlationId;
        }
        """,
    )
    write(
        java / "exception/BusinessException.java",
        """
        package com.fashionpin.common.exception;

        import lombok.Getter;
        import org.springframework.http.HttpStatus;

        @Getter
        public class BusinessException extends RuntimeException {
            private final String code;
            private final HttpStatus status;

            public BusinessException(String code, String message) {
                this(code, message, HttpStatus.BAD_REQUEST);
            }

            public BusinessException(String code, String message, HttpStatus status) {
                super(message);
                this.code = code;
                this.status = status;
            }
        }
        """,
    )
    write(
        java / "exception/ValidationException.java",
        """
        package com.fashionpin.common.exception;

        import java.util.List;
        import lombok.Getter;

        @Getter
        public class ValidationException extends BusinessException {
            private final List<String> details;

            public ValidationException(String message, List<String> details) {
                super("VALIDATION_ERROR", message);
                this.details = details;
            }
        }
        """,
    )
    write(
        java / "exception/ResourceNotFoundException.java",
        """
        package com.fashionpin.common.exception;

        import org.springframework.http.HttpStatus;

        public class ResourceNotFoundException extends BusinessException {
            public ResourceNotFoundException(String message) {
                super("RESOURCE_NOT_FOUND", message, HttpStatus.NOT_FOUND);
            }
        }
        """,
    )
    write(
        java / "dto/ApiResponse.java",
        """
        package com.fashionpin.common.dto;

        import com.fasterxml.jackson.annotation.JsonInclude;
        import java.time.Instant;
        import lombok.Builder;
        import lombok.Value;

        @Value
        @Builder
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public class ApiResponse<T> {
            boolean success;
            String message;
            T data;
            Instant timestamp;

            public static <T> ApiResponse<T> ok(T data) {
                return ApiResponse.<T>builder()
                        .success(true)
                        .message("OK")
                        .data(data)
                        .timestamp(Instant.now())
                        .build();
            }

            public static <T> ApiResponse<T> ok(String message, T data) {
                return ApiResponse.<T>builder()
                        .success(true)
                        .message(message)
                        .data(data)
                        .timestamp(Instant.now())
                        .build();
            }
        }
        """,
    )
    write(
        java / "event/BaseEvent.java",
        """
        package com.fashionpin.common.event;

        import java.time.Instant;
        import java.util.UUID;
        import lombok.AllArgsConstructor;
        import lombok.Data;
        import lombok.NoArgsConstructor;
        import lombok.experimental.SuperBuilder;

        @Data
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        public abstract class BaseEvent {
            private String eventId;
            private String eventType;
            private Instant occurredAt;
            private String correlationId;
            private String source;

            protected BaseEvent(String eventType, String source, String correlationId) {
                this.eventId = UUID.randomUUID().toString();
                this.eventType = eventType;
                this.occurredAt = Instant.now();
                this.source = source;
                this.correlationId = correlationId;
            }
        }
        """,
    )
    write(
        java / "event/DomainEvent.java",
        """
        package com.fashionpin.common.event;

        import lombok.Data;
        import lombok.EqualsAndHashCode;
        import lombok.NoArgsConstructor;
        import lombok.experimental.SuperBuilder;

        @Data
        @SuperBuilder
        @NoArgsConstructor
        @EqualsAndHashCode(callSuper = true)
        public class DomainEvent extends BaseEvent {
            private String aggregateId;
            private String aggregateType;
            private Object payload;

            public DomainEvent(
                    String eventType,
                    String source,
                    String correlationId,
                    String aggregateId,
                    String aggregateType,
                    Object payload) {
                super(eventType, source, correlationId);
                this.aggregateId = aggregateId;
                this.aggregateType = aggregateType;
                this.payload = payload;
            }
        }
        """,
    )
    write(
        java / "security/SecurityConstants.java",
        """
        package com.fashionpin.common.security;

        public final class SecurityConstants {
            public static final String AUTHORIZATION_HEADER = "Authorization";
            public static final String BEARER_PREFIX = "Bearer ";
            public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
            public static final String ROLES_CLAIM = "roles";
            public static final String USER_ID_CLAIM = "userId";

            private SecurityConstants() {
            }
        }
        """,
    )
    write(
        java / "util/CorrelationIdConstants.java",
        """
        package com.fashionpin.common.util;

        public final class CorrelationIdConstants {
            public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
            public static final String MDC_CORRELATION_ID = "correlationId";

            private CorrelationIdConstants() {
            }
        }
        """,
    )
    write(
        java / "kafka/KafkaTopics.java",
        """
        package com.fashionpin.common.kafka;

        public final class KafkaTopics {
            public static final String USER_EVENTS = "fashionpin.user.events";
            public static final String ORDER_EVENTS = "fashionpin.order.events";
            public static final String PRODUCT_EVENTS = "fashionpin.product.events";
            public static final String NOTIFICATION_EVENTS = "fashionpin.notification.events";
            public static final String ANALYTICS_EVENTS = "fashionpin.analytics.events";
            public static final String MEDIA_EVENTS = "fashionpin.media.events";
            public static final String PAYMENT_EVENTS = "fashionpin.payment.events";
            public static final String INVENTORY_EVENTS = "fashionpin.inventory.events";

            private KafkaTopics() {
            }
        }
        """,
    )


def dockerfile(service: str, port: int) -> str:
    return f"""# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY . .
RUN ./mvnw -pl {service} -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S fashionpin && adduser -S fashionpin -G fashionpin
USER fashionpin
COPY --from=build /workspace/{service}/target/{service}-*.jar app.jar
EXPOSE {port}
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
"""


def service_readme(service: str, port: int, description: str) -> str:
    return f"""# {service}

{description}

## Port

`{port}`

## Health

- Actuator: `http://localhost:{port}/actuator/health`
- API health: `http://localhost:{port}/api/v1/health`

## Local run

```bash
mvn -pl {service} spring-boot:run
```

## Docker

```bash
docker compose up {service}
```

This module contains foundation placeholders only. Business features are intentionally not implemented.
"""


def logging_xml() -> str:
    return """<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    <springProperty scope="context" name="appName" source="spring.application.name" defaultValue="fashion-pin"/>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} [%thread] %-5level %logger{36} correlationId=%X{correlationId:-n/a} service=${appName} - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
"""


def generate_discovery() -> None:
    service = "discovery-service"
    port = 8761
    base = ROOT / service
    pkg = f"com.fashionpin.{package_name(service)}"
    java = base / f"src/main/java/{pkg.replace('.', '/')}"
    resources = base / "src/main/resources"
    touch_package(java, STANDARD_PACKAGES)

    write(
        base / "pom.xml",
        f"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>{GROUP_ID}</groupId>
        <artifactId>{PARENT_ARTIFACT}</artifactId>
        <version>{VERSION}</version>
    </parent>
    <artifactId>{service}</artifactId>
    <name>{service}</name>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
""",
    )
    write(
        java / f"{class_prefix(service)}Application.java",
        f"""
        package {pkg};

        import org.springframework.boot.SpringApplication;
        import org.springframework.boot.autoconfigure.SpringBootApplication;
        import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

        @SpringBootApplication
        @EnableEurekaServer
        public class {class_prefix(service)}Application {{

            public static void main(String[] args) {{
                SpringApplication.run({class_prefix(service)}Application.class, args);
            }}
        }}
        """,
    )
    write(
        java / "controller/HealthController.java",
        f"""
        package {pkg}.controller;

        import java.util.Map;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RestController;

        @RestController
        @RequestMapping("/api/v1/health")
        public class HealthController {{

            @GetMapping
            public ResponseEntity<Map<String, String>> health() {{
                return ResponseEntity.ok(Map.of(
                        "status", "UP",
                        "service", "{service}"));
            }}
        }}
        """,
    )
    write(
        java / "config/DiscoveryServerSettings.java",
        f"""
        package {pkg}.config;

        import org.springframework.context.annotation.Configuration;

        @Configuration
        public class DiscoveryServerSettings {{
            // Placeholder for Eureka server customizations.
        }}
        """,
    )
    write(
        resources / "application.yml",
        f"""
        spring:
          application:
            name: {service}
          profiles:
            active: ${{SPRING_PROFILES_ACTIVE:dev}}

        server:
          port: {port}

        eureka:
          instance:
            hostname: localhost
          client:
            register-with-eureka: false
            fetch-registry: false
            service-url:
              defaultZone: http://${{eureka.instance.hostname}}:{port}/eureka/
          server:
            enable-self-preservation: false
            eviction-interval-timer-in-ms: 5000

        management:
          endpoints:
            web:
              exposure:
                include: health,info,metrics,prometheus
          endpoint:
            health:
              show-details: always
          tracing:
            sampling:
              probability: 1.0

        logging:
          config: classpath:logback-spring.xml
        """,
    )
    write(resources / "bootstrap.yml", f"spring:\n  application:\n    name: {service}\n")
    write(resources / "logback-spring.xml", logging_xml())
    write(base / "Dockerfile", dockerfile(service, port))
    write(base / "README.md", service_readme(service, port, "Netflix Eureka service discovery server."))


def generate_config_server() -> None:
    service = "config-server"
    port = 8888
    base = ROOT / service
    pkg = f"com.fashionpin.{package_name(service)}"
    java = base / f"src/main/java/{pkg.replace('.', '/')}"
    resources = base / "src/main/resources"
    touch_package(java, STANDARD_PACKAGES)

    write(
        base / "pom.xml",
        f"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>{GROUP_ID}</groupId>
        <artifactId>{PARENT_ARTIFACT}</artifactId>
        <version>{VERSION}</version>
    </parent>
    <artifactId>{service}</artifactId>
    <name>{service}</name>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
""",
    )
    write(
        java / f"{class_prefix(service)}Application.java",
        f"""
        package {pkg};

        import org.springframework.boot.SpringApplication;
        import org.springframework.boot.autoconfigure.SpringBootApplication;
        import org.springframework.cloud.config.server.EnableConfigServer;
        import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

        @SpringBootApplication
        @EnableConfigServer
        @EnableDiscoveryClient
        public class {class_prefix(service)}Application {{

            public static void main(String[] args) {{
                SpringApplication.run({class_prefix(service)}Application.class, args);
            }}
        }}
        """,
    )
    write(
        java / "controller/HealthController.java",
        f"""
        package {pkg}.controller;

        import java.util.Map;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RestController;

        @RestController
        @RequestMapping("/api/v1/health")
        public class HealthController {{

            @GetMapping
            public ResponseEntity<Map<String, String>> health() {{
                return ResponseEntity.ok(Map.of(
                        "status", "UP",
                        "service", "{service}"));
            }}
        }}
        """,
    )
    write(
        java / "security/SecurityConfig.java",
        f"""
        package {pkg}.security;

        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.security.config.Customizer;
        import org.springframework.security.config.annotation.web.builders.HttpSecurity;
        import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
        import org.springframework.security.web.SecurityFilterChain;

        @Configuration
        public class SecurityConfig {{

            @Bean
            public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {{
                http.csrf(AbstractHttpConfigurer::disable)
                        .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/actuator/**", "/api/v1/health").permitAll()
                                .anyRequest().authenticated())
                        .httpBasic(Customizer.withDefaults());
                return http.build();
            }}
        }}
        """,
    )
    write(
        java / "config/ConfigServerSettings.java",
        f"""
        package {pkg}.config;

        import org.springframework.context.annotation.Configuration;

        @Configuration
        public class ConfigServerSettings {{
            // Placeholder for config-server customizations.
        }}
        """,
    )
    write(
        resources / "application.yml",
        f"""
        spring:
          application:
            name: {service}
          profiles:
            active: native
          security:
            user:
              name: ${{CONFIG_SERVER_USERNAME:config}}
              password: ${{CONFIG_SERVER_PASSWORD:config}}
          cloud:
            config:
              server:
                native:
                  search-locations: file:./config-repo, classpath:/config-repo
                git:
                  uri: ${{CONFIG_GIT_URI:}}
                  default-label: main
                  clone-on-start: false

        server:
          port: {port}

        eureka:
          client:
            service-url:
              defaultZone: ${{EUREKA_SERVER_URL:http://localhost:8761/eureka/}}
          instance:
            prefer-ip-address: true

        management:
          endpoints:
            web:
              exposure:
                include: health,info,metrics,prometheus
          endpoint:
            health:
              show-details: always

        logging:
          config: classpath:logback-spring.xml
        """,
    )
    write(resources / "bootstrap.yml", f"spring:\n  application:\n    name: {service}\n")
    write(resources / "logback-spring.xml", logging_xml())
    write(base / "Dockerfile", dockerfile(service, port))
    write(base / "README.md", service_readme(service, port, "Centralized Spring Cloud Config Server."))


def generate_gateway() -> None:
    service = "api-gateway"
    port = 8080
    base = ROOT / service
    pkg = f"com.fashionpin.{package_name(service)}"
    java = base / f"src/main/java/{pkg.replace('.', '/')}"
    resources = base / "src/main/resources"
    touch_package(java, STANDARD_PACKAGES)

    write(
        base / "pom.xml",
        f"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>{GROUP_ID}</groupId>
        <artifactId>{PARENT_ARTIFACT}</artifactId>
        <version>{VERSION}</version>
    </parent>
    <artifactId>{service}</artifactId>
    <name>{service}</name>
    <dependencies>
        <dependency>
            <groupId>{GROUP_ID}</groupId>
            <artifactId>common-lib</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bootstrap</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-brave</artifactId>
        </dependency>
        <dependency>
            <groupId>io.zipkin.reporter2</groupId>
            <artifactId>zipkin-reporter-brave</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
""",
    )
    write(
        java / f"{class_prefix(service)}Application.java",
        f"""
        package {pkg};

        import org.springframework.boot.SpringApplication;
        import org.springframework.boot.autoconfigure.SpringBootApplication;
        import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

        @SpringBootApplication
        @EnableDiscoveryClient
        public class {class_prefix(service)}Application {{

            public static void main(String[] args) {{
                SpringApplication.run({class_prefix(service)}Application.class, args);
            }}
        }}
        """,
    )

    write(
        java / "config/CorsConfig.java",
        f"""
        package {pkg}.config;

        import java.util.List;
        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.web.cors.CorsConfiguration;
        import org.springframework.web.cors.reactive.CorsWebFilter;
        import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

        @Configuration
        public class CorsConfig {{

            @Bean
            public CorsWebFilter corsWebFilter(
                    @Value("${{gateway.cors.allowed-origins:*}}") List<String> allowedOrigins) {{
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowCredentials(true);
                config.setAllowedOriginPatterns(allowedOrigins);
                config.addAllowedHeader("*");
                config.addAllowedMethod("*");
                config.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return new CorsWebFilter(source);
            }}
        }}
        """,
    )
    write(
        java / "config/OpenApiConfig.java",
        f"""
        package {pkg}.config;

        import io.swagger.v3.oas.models.OpenAPI;
        import io.swagger.v3.oas.models.info.Info;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;

        @Configuration
        public class OpenApiConfig {{

            @Bean
            public OpenAPI openAPI() {{
                return new OpenAPI()
                        .info(new Info()
                                .title("Fashion Pin API Gateway")
                                .description("Gateway entrypoint for Fashion Pin microservices")
                                .version("v1"));
            }}
        }}
        """,
    )
    write(
        java / "config/RateLimiterConfig.java",
        f"""
        package {pkg}.config;

        import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import reactor.core.publisher.Mono;

        @Configuration
        public class RateLimiterConfig {{

            @Bean
            public KeyResolver ipKeyResolver() {{
                // Placeholder rate-limiter key resolver (client IP).
                return exchange -> Mono.justOrEmpty(exchange.getRequest().getRemoteAddress())
                        .map(address -> address.getAddress().getHostAddress())
                        .defaultIfEmpty("unknown");
            }}
        }}
        """,
    )
    write(
        java / "config/GatewayRouteConfig.java",
        f"""
        package {pkg}.config;

        import org.springframework.cloud.gateway.route.RouteLocator;
        import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;

        @Configuration
        public class GatewayRouteConfig {{

            @Bean
            public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {{
                // Routes are primarily defined in application.yml via Eureka lb:// URIs.
                // This bean is a placeholder for programmatic route extensions.
                return builder.routes().build();
            }}
        }}
        """,
    )
    write(
        java / "filter/GlobalLoggingFilter.java",
        f"""
        package {pkg}.filter;

        import java.util.UUID;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.slf4j.MDC;
        import org.springframework.cloud.gateway.filter.GatewayFilterChain;
        import org.springframework.cloud.gateway.filter.GlobalFilter;
        import org.springframework.core.Ordered;
        import org.springframework.http.server.reactive.ServerHttpRequest;
        import org.springframework.stereotype.Component;
        import org.springframework.web.server.ServerWebExchange;
        import reactor.core.publisher.Mono;

        @Component
        public class GlobalLoggingFilter implements GlobalFilter, Ordered {{

            private static final Logger log = LoggerFactory.getLogger(GlobalLoggingFilter.class);
            private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {{
                String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
                if (correlationId == null || correlationId.isBlank()) {{
                    correlationId = UUID.randomUUID().toString();
                }}
                MDC.put("correlationId", correlationId);
                ServerHttpRequest mutated = exchange.getRequest().mutate()
                        .header(CORRELATION_ID_HEADER, correlationId)
                        .build();
                log.info("Incoming request method={{}} path={{}}",
                        mutated.getMethod(), mutated.getURI().getPath());
                String finalCorrelationId = correlationId;
                return chain.filter(exchange.mutate().request(mutated).build())
                        .doFinally(signal -> {{
                            log.info("Completed request path={{}} correlationId={{}}",
                                    mutated.getURI().getPath(), finalCorrelationId);
                            MDC.remove("correlationId");
                        }});
            }}

            @Override
            public int getOrder() {{
                return Ordered.HIGHEST_PRECEDENCE;
            }}
        }}
        """,
    )
    write(
        java / "filter/JwtAuthenticationFilter.java",
        f"""
        package {pkg}.filter;

        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.springframework.cloud.gateway.filter.GatewayFilterChain;
        import org.springframework.cloud.gateway.filter.GlobalFilter;
        import org.springframework.core.Ordered;
        import org.springframework.http.HttpHeaders;
        import org.springframework.stereotype.Component;
        import org.springframework.web.server.ServerWebExchange;
        import reactor.core.publisher.Mono;

        @Component
        public class JwtAuthenticationFilter implements GlobalFilter, Ordered {{

            private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {{
                // Placeholder: validate JWT signature/claims and enrich request headers.
                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {{
                    log.debug("JWT placeholder observed Authorization bearer token");
                }}
                return chain.filter(exchange);
            }}

            @Override
            public int getOrder() {{
                return -100;
            }}
        }}
        """,
    )
    write(
        java / "filter/AuthenticationFilter.java",
        f"""
        package {pkg}.filter;

        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.springframework.cloud.gateway.filter.GatewayFilterChain;
        import org.springframework.cloud.gateway.filter.GlobalFilter;
        import org.springframework.core.Ordered;
        import org.springframework.stereotype.Component;
        import org.springframework.web.server.ServerWebExchange;
        import reactor.core.publisher.Mono;

        @Component
        public class AuthenticationFilter implements GlobalFilter, Ordered {{

            private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {{
                // Placeholder: enforce authentication for protected routes.
                log.trace("Authentication filter placeholder for path={{}}",
                        exchange.getRequest().getURI().getPath());
                return chain.filter(exchange);
            }}

            @Override
            public int getOrder() {{
                return -90;
            }}
        }}
        """,
    )
    write(
        java / "controller/HealthController.java",
        f"""
        package {pkg}.controller;

        import java.util.Map;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RestController;
        import reactor.core.publisher.Mono;

        @RestController
        @RequestMapping("/api/v1/health")
        public class HealthController {{

            @GetMapping
            public Mono<ResponseEntity<Map<String, String>>> health() {{
                return Mono.just(ResponseEntity.ok(Map.of(
                        "status", "UP",
                        "service", "{service}")));
            }}
        }}
        """,
    )

    routes = []
    for name, svc_port, _, _ in BUSINESS_SERVICES:
        route_id = name
        path_prefix = name.removesuffix("-service")
        routes.append(
            f"""
              - id: {route_id}
                uri: lb://{name}
                predicates:
                  - Path=/api/v1/{path_prefix}/**
                filters:
                  - RewritePath=/api/v1/{path_prefix}(?<segment>/?.*), /api/v1${{segment}}
                  # Rate limiting placeholder: enable RequestRateLimiter when Redis is available.
            """.rstrip()
        )
    routes_yaml = "\n".join(routes)

    write(
        resources / "application.yml",
        f"""
        spring:
          application:
            name: {service}
          profiles:
            active: ${{SPRING_PROFILES_ACTIVE:dev}}
          data:
            redis:
              host: ${{REDIS_HOST:localhost}}
              port: ${{REDIS_PORT:6379}}
          cloud:
            gateway:
              discovery:
                locator:
                  enabled: true
                  lower-case-service-id: true
              default-filters:
                - DedupeResponseHeader=Access-Control-Allow-Origin Access-Control-Allow-Credentials, RETAIN_UNIQUE
              routes:
{routes_yaml}

        server:
          port: {port}

        eureka:
          client:
            fetch-registry: true
            register-with-eureka: true
            registry-fetch-interval-seconds: 5
            service-url:
              defaultZone: ${{EUREKA_SERVER_URL:http://localhost:8761/eureka/}}
          instance:
            prefer-ip-address: true
            lease-renewal-interval-in-seconds: 10
            lease-expiration-duration-in-seconds: 30

        management:
          endpoints:
            web:
              exposure:
                include: health,info,metrics,prometheus,gateway
          endpoint:
            health:
              show-details: always
            gateway:
              enabled: true
          health:
            redis:
              enabled: false
          tracing:
            sampling:
              probability: 1.0
          zipkin:
            tracing:
              endpoint: ${{ZIPKIN_URL:http://localhost:9411/api/v2/spans}}

        springdoc:
          api-docs:
            path: /v3/api-docs
          swagger-ui:
            path: /swagger-ui.html

        gateway:
          cors:
            allowed-origins: "*"

        logging:
          config: classpath:logback-spring.xml

        ---
        spring:
          config:
            activate:
              on-profile: dev
          autoconfigure:
            exclude:
              - org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration
              - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
        """,
    )
    write(
        resources / "bootstrap.yml",
        f"""
        spring:
          application:
            name: {service}
          cloud:
            config:
              enabled: false
              uri: ${{CONFIG_SERVER_URL:http://localhost:8888}}
              fail-fast: false
        """,
    )
    write(resources / "logback-spring.xml", logging_xml())
    write(base / "Dockerfile", dockerfile(service, port))
    write(base / "README.md", service_readme(service, port, "Spring Cloud Gateway edge service."))


def business_service_pom(service: str, with_jpa: bool) -> str:
    jpa_deps = ""
    if with_jpa:
        jpa_deps = """
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
"""
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>{GROUP_ID}</groupId>
        <artifactId>{PARENT_ARTIFACT}</artifactId>
        <version>{VERSION}</version>
    </parent>
    <artifactId>{service}</artifactId>
    <name>{service}</name>
    <dependencies>
        <dependency>
            <groupId>{GROUP_ID}</groupId>
            <artifactId>common-lib</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bootstrap</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>
        <dependency>
            <groupId>net.devh</groupId>
            <artifactId>grpc-server-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>net.devh</groupId>
            <artifactId>grpc-client-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-brave</artifactId>
        </dependency>
        <dependency>
            <groupId>io.zipkin.reporter2</groupId>
            <artifactId>zipkin-reporter-brave</artifactId>
        </dependency>
{jpa_deps}
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>kafka</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
"""


def generate_business_service(service: str, port: int, db_name: str, with_jpa: bool) -> None:
    base = ROOT / service
    short = package_name(service)
    pkg = f"com.fashionpin.{short}"
    java = base / f"src/main/java/{pkg.replace('.', '/')}"
    resources = base / "src/main/resources"
    proto = base / "src/main/proto"
    test_java = base / f"src/test/java/{pkg.replace('.', '/')}"
    prefix = class_prefix(service)
    touch_package(java, STANDARD_PACKAGES)
    test_java.mkdir(parents=True, exist_ok=True)

    write(base / "pom.xml", business_service_pom(service, with_jpa))
    write(
        java / f"{prefix}Application.java",
        f"""
        package {pkg};

        import org.springframework.boot.SpringApplication;
        import org.springframework.boot.autoconfigure.SpringBootApplication;
        import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
        import org.springframework.cloud.openfeign.EnableFeignClients;
        import org.springframework.kafka.annotation.EnableKafka;

        @SpringBootApplication(scanBasePackages = {{"com.fashionpin.{short}", "com.fashionpin.common"}})
        @EnableDiscoveryClient
        @EnableFeignClients
        @EnableKafka
        public class {prefix}Application {{

            public static void main(String[] args) {{
                SpringApplication.run({prefix}Application.class, args);
            }}
        }}
        """,
    )
    write(
        java / "controller/HealthController.java",
        f"""
        package {pkg}.controller;

        import com.fashionpin.common.dto.ApiResponse;
        import java.util.Map;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RestController;

        @RestController
        @RequestMapping("/api/v1/health")
        public class HealthController {{

            @GetMapping
            public ResponseEntity<ApiResponse<Map<String, String>>> health() {{
                return ResponseEntity.ok(ApiResponse.ok(Map.of(
                        "status", "UP",
                        "service", "{service}")));
            }}
        }}
        """,
    )
    write(
        java / "health/ServiceHealthIndicator.java",
        f"""
        package {pkg}.health;

        import org.springframework.boot.actuate.health.Health;
        import org.springframework.boot.actuate.health.HealthIndicator;
        import org.springframework.stereotype.Component;

        @Component
        public class ServiceHealthIndicator implements HealthIndicator {{

            @Override
            public Health health() {{
                return Health.up()
                        .withDetail("service", "{service}")
                        .withDetail("foundation", "ready")
                        .build();
            }}
        }}
        """,
    )
    write(
        java / "config/OpenApiConfig.java",
        f"""
        package {pkg}.config;

        import io.swagger.v3.oas.models.OpenAPI;
        import io.swagger.v3.oas.models.info.Info;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;

        @Configuration
        public class OpenApiConfig {{

            @Bean
            public OpenAPI openAPI() {{
                return new OpenAPI()
                        .info(new Info()
                                .title("{prefix} API")
                                .description("Foundation API for {service}")
                                .version("v1"));
            }}
        }}
        """,
    )
    write(
        java / "config/ApplicationConfig.java",
        f"""
        package {pkg}.config;

        import org.springframework.context.annotation.Configuration;

        @Configuration
        public class ApplicationConfig {{
            // Placeholder for service-level beans and infrastructure wiring.
        }}
        """,
    )
    write(
        java / "config/ObservabilityConfig.java",
        f"""
        package {pkg}.config;

        import io.micrometer.core.instrument.MeterRegistry;
        import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;

        @Configuration
        public class ObservabilityConfig {{

            @Bean
            public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {{
                return registry -> registry.config().commonTags("service", "{service}");
            }}
        }}
        """,
    )
    write(
        java / "config/WebMvcConfig.java",
        f"""
        package {pkg}.config;

        import {pkg}.util.CorrelationIdInterceptor;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
        import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

        @Configuration
        public class WebMvcConfig implements WebMvcConfigurer {{

            private final CorrelationIdInterceptor correlationIdInterceptor;

            public WebMvcConfig(CorrelationIdInterceptor correlationIdInterceptor) {{
                this.correlationIdInterceptor = correlationIdInterceptor;
            }}

            @Override
            public void addInterceptors(InterceptorRegistry registry) {{
                registry.addInterceptor(correlationIdInterceptor);
            }}
        }}
        """,
    )
    write(
        java / "util/CorrelationIdInterceptor.java",
        f"""
        package {pkg}.util;

        import com.fashionpin.common.util.CorrelationIdConstants;
        import jakarta.servlet.http.HttpServletRequest;
        import jakarta.servlet.http.HttpServletResponse;
        import java.util.UUID;
        import org.slf4j.MDC;
        import org.springframework.stereotype.Component;
        import org.springframework.web.servlet.HandlerInterceptor;

        @Component
        public class CorrelationIdInterceptor implements HandlerInterceptor {{

            @Override
            public boolean preHandle(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    Object handler) {{
                String correlationId = request.getHeader(CorrelationIdConstants.CORRELATION_ID_HEADER);
                if (correlationId == null || correlationId.isBlank()) {{
                    correlationId = UUID.randomUUID().toString();
                }}
                MDC.put(CorrelationIdConstants.MDC_CORRELATION_ID, correlationId);
                response.setHeader(CorrelationIdConstants.CORRELATION_ID_HEADER, correlationId);
                return true;
            }}

            @Override
            public void afterCompletion(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    Object handler,
                    Exception ex) {{
                MDC.remove(CorrelationIdConstants.MDC_CORRELATION_ID);
            }}
        }}
        """,
    )
    write(
        java / "exception/GlobalExceptionHandler.java",
        f"""
        package {pkg}.exception;

        import com.fashionpin.common.exception.ApiError;
        import com.fashionpin.common.exception.BusinessException;
        import com.fashionpin.common.exception.ValidationException;
        import com.fashionpin.common.util.CorrelationIdConstants;
        import jakarta.servlet.http.HttpServletRequest;
        import java.time.Instant;
        import java.util.List;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.slf4j.MDC;
        import org.springframework.http.HttpStatus;
        import org.springframework.http.ResponseEntity;
        import org.springframework.validation.FieldError;
        import org.springframework.web.bind.MethodArgumentNotValidException;
        import org.springframework.web.bind.annotation.ExceptionHandler;
        import org.springframework.web.bind.annotation.RestControllerAdvice;

        @RestControllerAdvice
        public class GlobalExceptionHandler {{

            private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

            @ExceptionHandler(BusinessException.class)
            public ResponseEntity<ApiError> handleBusiness(
                    BusinessException ex, HttpServletRequest request) {{
                return build(ex.getStatus(), ex.getCode(), ex.getMessage(), request, List.of());
            }}

            @ExceptionHandler(ValidationException.class)
            public ResponseEntity<ApiError> handleValidation(
                    ValidationException ex, HttpServletRequest request) {{
                return build(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage(), request, ex.getDetails());
            }}

            @ExceptionHandler(MethodArgumentNotValidException.class)
            public ResponseEntity<ApiError> handleMethodArgumentNotValid(
                    MethodArgumentNotValidException ex, HttpServletRequest request) {{
                List<String> details = ex.getBindingResult().getFieldErrors().stream()
                        .map(this::formatFieldError)
                        .toList();
                return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", request, details);
            }}

            @ExceptionHandler(Exception.class)
            public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {{
                log.error("Unhandled exception", ex);
                return build(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "INTERNAL_ERROR",
                        "Unexpected error",
                        request,
                        List.of());
            }}

            private String formatFieldError(FieldError error) {{
                return error.getField() + ": " + error.getDefaultMessage();
            }}

            private ResponseEntity<ApiError> build(
                    HttpStatus status,
                    String error,
                    String message,
                    HttpServletRequest request,
                    List<String> details) {{
                ApiError body = ApiError.builder()
                        .timestamp(Instant.now())
                        .status(status.value())
                        .error(error)
                        .message(message)
                        .path(request.getRequestURI())
                        .correlationId(MDC.get(CorrelationIdConstants.MDC_CORRELATION_ID))
                        .details(details)
                        .build();
                return ResponseEntity.status(status).body(body);
            }}
        }}
        """,
    )
    write(
        java / "security/SecurityConfig.java",
        f"""
        package {pkg}.security;

        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.security.config.Customizer;
        import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
        import org.springframework.security.config.annotation.web.builders.HttpSecurity;
        import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
        import org.springframework.security.config.http.SessionCreationPolicy;
        import org.springframework.security.web.SecurityFilterChain;
        import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

        @Configuration
        @EnableMethodSecurity
        public class SecurityConfig {{

            private final JwtAuthenticationFilter jwtAuthenticationFilter;

            public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {{
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
            }}

            @Bean
            public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {{
                http.csrf(AbstractHttpConfigurer::disable)
                        .cors(Customizer.withDefaults())
                        .sessionManagement(session ->
                                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .authorizeHttpRequests(auth -> auth
                                .requestMatchers(
                                        "/actuator/**",
                                        "/api/v1/health",
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html")
                                .permitAll()
                                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated())
                        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
            }}
        }}
        """,
    )
    write(
        java / "security/JwtAuthenticationFilter.java",
        f"""
        package {pkg}.security;

        import com.fashionpin.common.security.SecurityConstants;
        import jakarta.servlet.FilterChain;
        import jakarta.servlet.ServletException;
        import jakarta.servlet.http.HttpServletRequest;
        import jakarta.servlet.http.HttpServletResponse;
        import java.io.IOException;
        import java.util.List;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.springframework.http.HttpHeaders;
        import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
        import org.springframework.security.core.authority.SimpleGrantedAuthority;
        import org.springframework.security.core.context.SecurityContextHolder;
        import org.springframework.stereotype.Component;
        import org.springframework.web.filter.OncePerRequestFilter;

        @Component
        public class JwtAuthenticationFilter extends OncePerRequestFilter {{

            private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain) throws ServletException, IOException {{
                String header = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (header != null && header.startsWith(SecurityConstants.BEARER_PREFIX)) {{
                    // Placeholder: parse/validate JWT and populate SecurityContext.
                    String token = header.substring(SecurityConstants.BEARER_PREFIX.length());
                    log.debug("JWT placeholder token received length={{}}", token.length());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    "placeholder-user",
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_USER")));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }}
                filterChain.doFilter(request, response);
            }}
        }}
        """,
    )
    write(
        java / "security/JwtService.java",
        f"""
        package {pkg}.security;

        import org.springframework.stereotype.Service;

        @Service
        public class JwtService {{

            public boolean validateToken(String token) {{
                // Placeholder for JWT signature and expiry validation.
                return token != null && !token.isBlank();
            }}

            public String extractUserId(String token) {{
                // Placeholder for claim extraction.
                return "placeholder-user";
            }}
        }}
        """,
    )
    write(
        java / "kafka/KafkaConfig.java",
        f"""
        package {pkg}.kafka;

        import java.util.HashMap;
        import java.util.Map;
        import org.apache.kafka.clients.consumer.ConsumerConfig;
        import org.apache.kafka.clients.producer.ProducerConfig;
        import org.apache.kafka.common.serialization.StringDeserializer;
        import org.apache.kafka.common.serialization.StringSerializer;
        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
        import org.springframework.kafka.core.ConsumerFactory;
        import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
        import org.springframework.kafka.core.DefaultKafkaProducerFactory;
        import org.springframework.kafka.core.KafkaTemplate;
        import org.springframework.kafka.core.ProducerFactory;
        import org.springframework.kafka.support.serializer.JsonDeserializer;
        import org.springframework.kafka.support.serializer.JsonSerializer;

        @Configuration
        @org.springframework.context.annotation.Profile({{"!dev", "!test"}})
        public class KafkaConfig {{

            @Value("${{spring.kafka.bootstrap-servers:localhost:9092}}")
            private String bootstrapServers;

            @Value("${{spring.kafka.consumer.group-id:{service}}}")
            private String groupId;

            @Bean
            public ProducerFactory<String, Object> producerFactory() {{
                Map<String, Object> props = new HashMap<>();
                props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
                props.put(ProducerConfig.ACKS_CONFIG, "all");
                return new DefaultKafkaProducerFactory<>(props);
            }}

            @Bean
            public KafkaTemplate<String, Object> kafkaTemplate() {{
                return new KafkaTemplate<>(producerFactory());
            }}

            @Bean
            public ConsumerFactory<String, Object> consumerFactory() {{
                Map<String, Object> props = new HashMap<>();
                props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
                props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
                props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
                props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.fashionpin.*");
                props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                return new DefaultKafkaConsumerFactory<>(props);
            }}

            @Bean
            public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {{
                ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                        new ConcurrentKafkaListenerContainerFactory<>();
                factory.setConsumerFactory(consumerFactory());
                return factory;
            }}
        }}
        """,
    )
    write(
        java / "kafka/KafkaTopicConfig.java",
        f"""
        package {pkg}.kafka;

        import com.fashionpin.common.kafka.KafkaTopics;
        import org.apache.kafka.clients.admin.NewTopic;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.kafka.config.TopicBuilder;

        @Configuration
        @org.springframework.context.annotation.Profile({{"!dev", "!test"}})
        public class KafkaTopicConfig {{

            @Bean
            public NewTopic serviceEventsTopic() {{
                return TopicBuilder.name("fashionpin.{short}.events")
                        .partitions(3)
                        .replicas(1)
                        .build();
            }}

            @Bean
            public NewTopic notificationEventsTopic() {{
                return TopicBuilder.name(KafkaTopics.NOTIFICATION_EVENTS)
                        .partitions(3)
                        .replicas(1)
                        .build();
            }}
        }}
        """,
    )
    write(
        java / "kafka/KafkaProducerService.java",
        f"""
        package {pkg}.kafka;

        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.springframework.beans.factory.ObjectProvider;
        import org.springframework.kafka.core.KafkaTemplate;
        import org.springframework.stereotype.Service;

        @Service
        public class KafkaProducerService {{

            private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

            private final ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider;

            public KafkaProducerService(ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider) {{
                this.kafkaTemplateProvider = kafkaTemplateProvider;
            }}

            public void publish(String topic, String key, Object payload) {{
                KafkaTemplate<String, Object> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
                if (kafkaTemplate == null) {{
                    log.debug("Kafka template unavailable; skipped publish topic={{}} key={{}}", topic, key);
                    return;
                }}
                log.debug("Publishing event topic={{}} key={{}}", topic, key);
                kafkaTemplate.send(topic, key, payload);
            }}
        }}
        """,
    )
    write(
        java / "event/SampleDomainEvent.java",
        f"""
        package {pkg}.event;

        import com.fashionpin.common.event.DomainEvent;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
        import lombok.NoArgsConstructor;
        import lombok.experimental.SuperBuilder;

        @Data
        @SuperBuilder
        @NoArgsConstructor
        @EqualsAndHashCode(callSuper = true)
        public class SampleDomainEvent extends DomainEvent {{

            public static SampleDomainEvent foundationReady(String correlationId) {{
                return SampleDomainEvent.builder()
                        .eventType("FOUNDATION_READY")
                        .source("{service}")
                        .correlationId(correlationId)
                        .aggregateId("{service}")
                        .aggregateType("Service")
                        .payload(java.util.Map.of("status", "ready"))
                        .build();
            }}
        }}
        """,
    )
    write(
        java / "grpc/GrpcServerConfig.java",
        f"""
        package {pkg}.grpc;

        import org.springframework.context.annotation.Configuration;

        @Configuration
        public class GrpcServerConfig {{
            // Placeholder for gRPC server interceptors and server customization.
        }}
        """,
    )
    write(
        java / "grpc/GrpcClientConfig.java",
        f"""
        package {pkg}.grpc;

        import org.springframework.context.annotation.Configuration;

        @Configuration
        public class GrpcClientConfig {{
            // Placeholder for gRPC channel/client customization.
        }}
        """,
    )
    write(
        java / "grpc/HealthGrpcService.java",
        f"""
        package {pkg}.grpc;

        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.springframework.stereotype.Service;

        @Service
        public class HealthGrpcService {{

            private static final Logger log = LoggerFactory.getLogger(HealthGrpcService.class);

            public String check() {{
                // Placeholder until protobuf stubs are generated in CI/local builds.
                log.debug("gRPC health placeholder invoked");
                return "UP";
            }}
        }}
        """,
    )
    write(
        proto / "health.proto",
        f"""
        syntax = "proto3";

        option java_multiple_files = true;
        option java_package = "{pkg}.grpc.generated";
        option java_outer_classname = "HealthProto";

        package fashionpin.{short};

        service HealthService {{
          rpc Check (HealthCheckRequest) returns (HealthCheckResponse);
        }}

        message HealthCheckRequest {{
          string service = 1;
        }}

        message HealthCheckResponse {{
          string status = 1;
          string service = 2;
        }}
        """,
    )
    write(
        java / "client/UserServiceClient.java",
        f"""
        package {pkg}.client;

        import java.util.Map;
        import org.springframework.cloud.openfeign.FeignClient;
        import org.springframework.web.bind.annotation.GetMapping;

        @FeignClient(name = "user-service", path = "/api/v1")
        public interface UserServiceClient {{

            @GetMapping("/health")
            Map<String, Object> health();
        }}
        """,
    )
    write(
        java / "client/ProductServiceClient.java",
        f"""
        package {pkg}.client;

        import java.util.Map;
        import org.springframework.cloud.openfeign.FeignClient;
        import org.springframework.web.bind.annotation.GetMapping;

        @FeignClient(name = "product-service", path = "/api/v1")
        public interface ProductServiceClient {{

            @GetMapping("/health")
            Map<String, Object> health();
        }}
        """,
    )
    write(
        java / "client/FeignClientConfig.java",
        f"""
        package {pkg}.client;

        import feign.Logger;
        import feign.RequestInterceptor;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;

        @Configuration
        public class FeignClientConfig {{

            @Bean
            public Logger.Level feignLoggerLevel() {{
                return Logger.Level.BASIC;
            }}

            @Bean
            public RequestInterceptor feignCorrelationIdInterceptor() {{
                return template -> {{
                    // Placeholder: propagate correlation/auth headers across Feign calls.
                }};
            }}
        }}
        """,
    )
    write(
        java / "service/HealthService.java",
        f"""
        package {pkg}.service;

        import java.util.Map;
        import org.springframework.stereotype.Service;

        @Service
        public class HealthService {{

            public Map<String, String> currentStatus() {{
                return Map.of(
                        "status", "UP",
                        "service", "{service}");
            }}
        }}
        """,
    )
    write(
        java / "dto/HealthResponse.java",
        f"""
        package {pkg}.dto;

        import lombok.Builder;
        import lombok.Value;

        @Value
        @Builder
        public class HealthResponse {{
            String status;
            String service;
        }}
        """,
    )
    write(
        java / "mapper/HealthMapper.java",
        f"""
        package {pkg}.mapper;

        import {pkg}.dto.HealthResponse;
        import java.util.Map;
        import org.mapstruct.Mapper;

        @Mapper(componentModel = "spring")
        public interface HealthMapper {{

            default HealthResponse toResponse(Map<String, String> source) {{
                return HealthResponse.builder()
                        .status(source.get("status"))
                        .service(source.get("service"))
                        .build();
            }}
        }}
        """,
    )
    write(
        java / "validation/NotBlankIfPresent.java",
        f"""
        package {pkg}.validation;

        import jakarta.validation.Constraint;
        import jakarta.validation.Payload;
        import java.lang.annotation.Documented;
        import java.lang.annotation.ElementType;
        import java.lang.annotation.Retention;
        import java.lang.annotation.RetentionPolicy;
        import java.lang.annotation.Target;

        @Documented
        @Constraint(validatedBy = NotBlankIfPresentValidator.class)
        @Target({{ElementType.FIELD, ElementType.PARAMETER}})
        @Retention(RetentionPolicy.RUNTIME)
        public @interface NotBlankIfPresent {{
            String message() default "must not be blank when present";
            Class<?>[] groups() default {{}};
            Class<? extends Payload>[] payload() default {{}};
        }}
        """,
    )
    write(
        java / "validation/NotBlankIfPresentValidator.java",
        f"""
        package {pkg}.validation;

        import jakarta.validation.ConstraintValidator;
        import jakarta.validation.ConstraintValidatorContext;

        public class NotBlankIfPresentValidator implements ConstraintValidator<NotBlankIfPresent, String> {{

            @Override
            public boolean isValid(String value, ConstraintValidatorContext context) {{
                return value == null || !value.isBlank();
            }}
        }}
        """,
    )
    write(
        java / "entity/BaseEntity.java",
        f"""
        package {pkg}.entity;

        import jakarta.persistence.Column;
        import jakarta.persistence.GeneratedValue;
        import jakarta.persistence.GenerationType;
        import jakarta.persistence.Id;
        import jakarta.persistence.MappedSuperclass;
        import jakarta.persistence.PrePersist;
        import jakarta.persistence.PreUpdate;
        import java.time.Instant;
        import java.util.UUID;
        import lombok.Getter;
        import lombok.Setter;

        @Getter
        @Setter
        @MappedSuperclass
        public abstract class BaseEntity {{

            @Id
            @GeneratedValue(strategy = GenerationType.UUID)
            private UUID id;

            @Column(nullable = false, updatable = false)
            private Instant createdAt;

            @Column(nullable = false)
            private Instant updatedAt;

            @PrePersist
            void onCreate() {{
                Instant now = Instant.now();
                createdAt = now;
                updatedAt = now;
            }}

            @PreUpdate
            void onUpdate() {{
                updatedAt = Instant.now();
            }}
        }}
        """,
    )
    write(
        java / "repository/package-info.java",
        f"""
        package {pkg}.repository;
        """,
    )
    write(
        java / "config/JpaConfig.java",
        f"""
        package {pkg}.config;

        import org.springframework.context.annotation.Configuration;
        import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

        @Configuration
        @EnableJpaAuditing
        public class JpaConfig {{
            // Placeholder for JPA auditing and persistence customization.
        }}
        """,
    )

    datasource_block = f"""
          datasource:
            url: ${{DB_URL:jdbc:postgresql://localhost:5432/{db_name}}}
            username: ${{DB_USERNAME:fashionpin}}
            password: ${{DB_PASSWORD:fashionpin}}
            driver-class-name: org.postgresql.Driver
          jpa:
            hibernate:
              ddl-auto: validate
            open-in-view: false
            properties:
              hibernate:
                dialect: org.hibernate.dialect.PostgreSQLDialect
                format_sql: true
          sql:
            init:
              mode: never
"""
    # Use H2 for local foundation boot without requiring Postgres up front
    profile_dev = f"""
        ---
        spring:
          config:
            activate:
              on-profile: dev
          datasource:
            url: jdbc:h2:mem:{db_name};MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE
            username: sa
            password:
            driver-class-name: org.h2.Driver
          jpa:
            hibernate:
              ddl-auto: update
            properties:
              hibernate:
                dialect: org.hibernate.dialect.H2Dialect
          h2:
            console:
              enabled: true
          kafka:
            admin:
              auto-create: true
            listener:
              auto-startup: false
          autoconfigure:
            exclude:
              - org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
              - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
        """

    write(
        resources / "application.yml",
        f"""
        spring:
          application:
            name: {service}
          profiles:
            active: ${{SPRING_PROFILES_ACTIVE:dev}}
{datasource_block}
          data:
            redis:
              host: ${{REDIS_HOST:localhost}}
              port: ${{REDIS_PORT:6379}}
          kafka:
            bootstrap-servers: ${{KAFKA_BOOTSTRAP_SERVERS:localhost:9092}}
            consumer:
              group-id: {service}
              auto-offset-reset: earliest
              key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
              value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
              properties:
                spring.json.trusted.packages: com.fashionpin.*
            producer:
              key-serializer: org.apache.kafka.common.serialization.StringSerializer
              value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
          cloud:
            config:
              enabled: ${{CONFIG_ENABLED:false}}
              fail-fast: false

        server:
          port: {port}

        eureka:
          client:
            service-url:
              defaultZone: ${{EUREKA_SERVER_URL:http://localhost:8761/eureka/}}
            register-with-eureka: true
            fetch-registry: true
            registry-fetch-interval-seconds: 5
          instance:
            prefer-ip-address: true
            lease-renewal-interval-in-seconds: 10
            lease-expiration-duration-in-seconds: 30

        management:
          endpoints:
            web:
              exposure:
                include: health,info,metrics,prometheus,loggers
          endpoint:
            health:
              show-details: always
          tracing:
            sampling:
              probability: 1.0
          zipkin:
            tracing:
              endpoint: ${{ZIPKIN_URL:http://localhost:9411/api/v2/spans}}
          metrics:
            tags:
              application: {service}

        springdoc:
          api-docs:
            path: /v3/api-docs
          swagger-ui:
            path: /swagger-ui.html

        grpc:
          server:
            port: ${{GRPC_PORT:{port + 1000}}}
          client:
            globally:
              negotiation-type: plaintext

        logging:
          config: classpath:logback-spring.xml

{profile_dev}
        ---
        spring:
          config:
            activate:
              on-profile: test
          datasource:
            url: jdbc:h2:mem:{db_name}_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE
            username: sa
            password:
            driver-class-name: org.h2.Driver
          jpa:
            hibernate:
              ddl-auto: create-drop
          autoconfigure:
            exclude:
              - org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
              - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
          kafka:
            listener:
              auto-startup: false

        ---
        spring:
          config:
            activate:
              on-profile: docker
          jpa:
            hibernate:
              ddl-auto: update
          kafka:
            listener:
              auto-startup: true

        ---
        spring:
          config:
            activate:
              on-profile: prod
          jpa:
            hibernate:
              ddl-auto: validate
          kafka:
            listener:
              auto-startup: true
        """,
    )
    write(
        resources / "bootstrap.yml",
        f"""
        spring:
          application:
            name: {service}
          cloud:
            config:
              uri: ${{CONFIG_SERVER_URL:http://localhost:8888}}
              username: ${{CONFIG_SERVER_USERNAME:config}}
              password: ${{CONFIG_SERVER_PASSWORD:config}}
              fail-fast: false
              enabled: ${{CONFIG_ENABLED:false}}
        """,
    )
    write(resources / "logback-spring.xml", logging_xml())
    write(base / "Dockerfile", dockerfile(service, port))
    write(
        base / "README.md",
        service_readme(
            service,
            port,
            f"Foundation module for {service}. Owns database `{db_name}` and exposes health endpoints only.",
        ),
    )
    write(
        test_java / f"{prefix}ApplicationTests.java",
        f"""
        package {pkg};

        import org.junit.jupiter.api.Test;
        import org.springframework.boot.test.context.SpringBootTest;
        import org.springframework.test.context.ActiveProfiles;

        @SpringBootTest
        @ActiveProfiles("test")
        class {prefix}ApplicationTests {{

            @Test
            void contextLoads() {{
                // Foundation smoke test placeholder.
            }}
        }}
        """,
    )


def generate_config_repo() -> None:
    repo = ROOT / "config-repo"
    repo.mkdir(parents=True, exist_ok=True)
    write(
        repo / "application.yml",
        """
        fashionpin:
          cors:
            allowed-origins: "*"
          security:
            jwt:
              issuer: fashion-pin
              # Placeholder secret only for foundation local profiles.
              secret: change-me-in-production

        management:
          endpoints:
            web:
              exposure:
                include: health,info,metrics,prometheus
          tracing:
            sampling:
              probability: 1.0

        logging:
          level:
            root: INFO
            com.fashionpin: INFO
        """,
    )
    for profile in ("dev", "test", "prod"):
        write(
            repo / f"application-{profile}.yml",
            f"""
            fashionpin:
              environment: {profile}
            logging:
              level:
                com.fashionpin: {"DEBUG" if profile == "dev" else "INFO"}
            """,
        )
    for name, port, db_name, _ in BUSINESS_SERVICES:
        write(
            repo / f"{name}.yml",
            f"""
            server:
              port: {port}
            spring:
              datasource:
                url: jdbc:postgresql://postgres:5432/{db_name}
                username: fashionpin
                password: fashionpin
            """,
        )
        for profile in ("dev", "test", "prod"):
            write(
                repo / f"{name}-{profile}.yml",
                f"""
                fashionpin:
                  service: {name}
                  profile: {profile}
                """,
            )
    # Copy into config-server classpath as well
    classpath_repo = ROOT / "config-server" / "src" / "main" / "resources" / "config-repo"
    classpath_repo.mkdir(parents=True, exist_ok=True)
    for file in repo.glob("*.yml"):
        write(classpath_repo / file.name, file.read_text(encoding="utf-8"))


def generate_docker_compose() -> None:
    service_blocks = []
    depends = ["discovery-service", "config-server", "postgres", "redis", "kafka"]

    # discovery
    service_blocks.append(
        """
  discovery-service:
    build:
      context: .
      dockerfile: discovery-service/Dockerfile
    container_name: fashionpin-discovery
    ports:
      - "8761:8761"
    environment:
      SPRING_PROFILES_ACTIVE: docker
    networks:
      - fashionpin-net
"""
    )
    service_blocks.append(
        """
  config-server:
    build:
      context: .
      dockerfile: config-server/Dockerfile
    container_name: fashionpin-config
    ports:
      - "8888:8888"
    environment:
      SPRING_PROFILES_ACTIVE: native
      EUREKA_SERVER_URL: http://discovery-service:8761/eureka/
      CONFIG_SERVER_USERNAME: config
      CONFIG_SERVER_PASSWORD: config
    depends_on:
      - discovery-service
    networks:
      - fashionpin-net
"""
    )
    service_blocks.append(
        """
  api-gateway:
    build:
      context: .
      dockerfile: api-gateway/Dockerfile
    container_name: fashionpin-gateway
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      EUREKA_SERVER_URL: http://discovery-service:8761/eureka/
      REDIS_HOST: redis
      REDIS_PORT: 6379
      ZIPKIN_URL: http://zipkin:9411/api/v2/spans
    depends_on:
      - discovery-service
      - config-server
      - redis
    networks:
      - fashionpin-net
"""
    )

    for name, port, db_name, _ in BUSINESS_SERVICES:
        grpc_port = port + 1000
        service_blocks.append(
            f"""
  {name}:
    build:
      context: .
      dockerfile: {name}/Dockerfile
    container_name: fashionpin-{name}
    ports:
      - "{port}:{port}"
      - "{grpc_port}:{grpc_port}"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      EUREKA_SERVER_URL: http://discovery-service:8761/eureka/
      CONFIG_SERVER_URL: http://config-server:8888
      CONFIG_ENABLED: "true"
      DB_URL: jdbc:postgresql://postgres:5432/{db_name}
      DB_USERNAME: fashionpin
      DB_PASSWORD: fashionpin
      REDIS_HOST: redis
      REDIS_PORT: 6379
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      ZIPKIN_URL: http://zipkin:9411/api/v2/spans
      GRPC_PORT: "{grpc_port}"
    depends_on:
      - discovery-service
      - config-server
      - postgres
      - redis
      - kafka
    networks:
      - fashionpin-net
"""
        )

    init_dbs = "\n".join(
        f'  - "{db_name}"' for _, _, db_name, _ in BUSINESS_SERVICES
    )

    write(
        ROOT / "docker-compose.yml",
        f"""
services:
  postgres:
    image: postgres:16-alpine
    container_name: fashionpin-postgres
    environment:
      POSTGRES_USER: fashionpin
      POSTGRES_PASSWORD: fashionpin
      POSTGRES_DB: fashionpin
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/postgres/init-databases.sh:/docker-entrypoint-initdb.d/init-databases.sh:ro
    networks:
      - fashionpin-net
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U fashionpin"]
      interval: 10s
      timeout: 5s
      retries: 10

  redis:
    image: redis:7-alpine
    container_name: fashionpin-redis
    ports:
      - "6379:6379"
    networks:
      - fashionpin-net

  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.1
    container_name: fashionpin-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"
    networks:
      - fashionpin-net

  kafka:
    image: confluentinc/cp-kafka:7.6.1
    container_name: fashionpin-kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    networks:
      - fashionpin-net

  zipkin:
    image: openzipkin/zipkin:3.4
    container_name: fashionpin-zipkin
    ports:
      - "9411:9411"
    networks:
      - fashionpin-net

  prometheus:
    image: prom/prometheus:v2.54.1
    container_name: fashionpin-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./observability/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
    networks:
      - fashionpin-net

  grafana:
    image: grafana/grafana:11.1.4
    container_name: fashionpin-grafana
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_USER: admin
      GF_SECURITY_ADMIN_PASSWORD: admin
    volumes:
      - ./observability/grafana/provisioning:/etc/grafana/provisioning:ro
      - grafana_data:/var/lib/grafana
    depends_on:
      - prometheus
    networks:
      - fashionpin-net

{"".join(service_blocks)}

volumes:
  postgres_data:
  grafana_data:

networks:
  fashionpin-net:
    driver: bridge
""",
    )

    db_creates = "\n".join(
        f'psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL\n'
        f'  SELECT \'CREATE DATABASE {db_name}\' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = \'{db_name}\')\\gexec\n'
        f"EOSQL"
        for _, _, db_name, _ in BUSINESS_SERVICES
    )
    create_sql = "\n".join(f"CREATE DATABASE {db_name};" for _, _, db_name, _ in BUSINESS_SERVICES)
    write(
        ROOT / "docker/postgres/init-databases.sh",
        f"""#!/bin/bash
set -euo pipefail

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
{create_sql}
EOSQL
""",
    )


def generate_observability() -> None:
    targets = []
    for name, port, *_ in INFRA_SERVICES + [(s[0], s[1], None, None) for s in BUSINESS_SERVICES]:
        targets.append(
            f"""
  - job_name: '{name}'
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['{name}:{port}']
"""
        )
    write(
        ROOT / "observability/prometheus/prometheus.yml",
        f"""
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
{"".join(targets)}
""",
    )
    write(
        ROOT / "observability/grafana/provisioning/datasources/datasource.yml",
        """
apiVersion: 1
datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
""",
    )


def generate_root_docs() -> None:
    service_table = "\n".join(
        f"| `{name}` | {port} | {db or 'n/a'} |"
        for name, port, *rest in (
            [(n, p, None) for n, p, _, _ in INFRA_SERVICES]
            + [(n, p, db) for n, p, db, _ in BUSINESS_SERVICES]
        )
        for db in [rest[0] if rest else None]
    )
    write(
        ROOT / "README.md",
        f"""# Fashion Pin Backend

Production-grade Spring Boot microservices foundation for **Fashion Pin**, a visual fashion discovery platform.

This repository contains infrastructure, configuration, communication, observability, and service skeletons only.
Business APIs, AI models, and domain features are intentionally not implemented.

## Architecture Overview

- Microservices with independent Maven modules
- Domain-oriented service boundaries
- Event-driven communication via Apache Kafka
- Synchronous communication via OpenFeign and gRPC placeholders
- Service discovery with Netflix Eureka
- Edge routing with Spring Cloud Gateway
- Centralized configuration with Spring Cloud Config
- Database-per-service with PostgreSQL
- Observability with Actuator, Micrometer, Prometheus, Grafana, and Zipkin

```text
Clients
   |
   v
API Gateway (JWT/Auth/Rate-limit placeholders, CORS, logging)
   |
   +--> Eureka Discovery
   |
   +--> Config Server
   |
   +--> Domain Services (auth, user, product, search, AI, commerce, ...)
          |                |
          | Kafka events   | Feign / gRPC
          v                v
     Brokers/Topics   Peer services
```

## Tech Stack

Java 21, Spring Boot 3.3.x, Spring Cloud 2023.0.x, Spring Security, Gateway, Eureka, Config Server, Actuator, JPA, PostgreSQL, Redis, Kafka, Docker, gRPC, OpenFeign, MapStruct, Lombok, Validation, OpenAPI, Micrometer, Prometheus, Grafana, Zipkin, JUnit 5, Testcontainers, Maven.

## Modules

| Service | Port | Database |
|---------|------|----------|
{service_table}
| `common-lib` | n/a | shared DTOs/errors/events |

## Folder Structure

Each business service follows:

```text
src/main/java/com/fashionpin/<service>/
  config/
  controller/
  service/
  repository/
  entity/
  dto/
  mapper/
  security/
  exception/
  client/
  event/
  kafka/
  grpc/
  util/
  validation/
  health/
```

## How Services Communicate

1. **Client -> Gateway**: all external traffic enters through `api-gateway:8080`
2. **Gateway -> Services**: Eureka-backed `lb://service-name` routes
3. **Service -> Service (sync)**: OpenFeign clients and gRPC placeholders
4. **Service -> Service (async)**: Kafka topics under `fashionpin.*.events`
5. **Config**: optional Config Server (`CONFIG_ENABLED=true`) with `dev` / `test` / `prod` profiles

## Prerequisites

- **Java 21** (required; use Temurin/OpenJDK 21)
- Maven 3.9+
- Docker & Docker Compose

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```

## Build

```bash
./mvnw clean package -DskipTests
```

## Run locally (foundation profile)

Start infrastructure first (recommended):

```bash
docker compose up -d postgres redis zookeeper kafka zipkin discovery-service config-server
```

Then run a service:

```bash
./mvnw -pl discovery-service spring-boot:run
./mvnw -pl api-gateway spring-boot:run
./mvnw -pl user-service spring-boot:run
```

Dev profile uses in-memory H2 so services can boot without PostgreSQL for foundation smoke checks.
Kafka/Redis autoconfig is disabled in `dev`/`test` to keep local startup reliable.

## Docker instructions

```bash
# Build and start the full stack
docker compose up --build

# Start only platform dependencies
docker compose up -d postgres redis zookeeper kafka zipkin prometheus grafana discovery-service config-server api-gateway
```

Useful URLs:

- Eureka: http://localhost:8761
- Gateway health: http://localhost:8080/api/v1/health
- Config Server: http://localhost:8888
- Zipkin: http://localhost:9411
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

## Health endpoints

Every service exposes:

- `/actuator/health`
- `/api/v1/health`
- `/actuator/prometheus`

Gateway example route:

```text
GET /api/v1/user/health  ->  user-service /api/v1/health
```

Because gateway discovery locator is enabled, services are also reachable as:

```text
GET /user-service/api/v1/health
```

## Security placeholders

- JWT filter placeholders in gateway and services
- Role-based authorization placeholder (`ROLE_ADMIN`, `ROLE_USER`)
- No production auth implementation yet

## Coding standards

- Constructor injection only
- No wildcard imports
- SOLID / clean architecture packaging
- Shared error model in `common-lib`
- Service-owned databases only

## Next implementation phases

1. Auth token issuance and JWT validation
2. Domain APIs per bounded context
3. Real gRPC stub generation in CI
4. Outbox/event schemas for domain events
5. AI service integrations behind hexagonal ports

## License

Proprietary - Fashion Pin startup foundation.
""",
    )
    write(
        ROOT / "docs/ARCHITECTURE.md",
        """
# Fashion Pin Architecture

## Style

- Microservices
- Event-driven integration
- DDD-oriented module boundaries
- Clean / hexagonal packaging inside services (`controller`, `service`, `repository`, `client`, `kafka`, `grpc`)

## Bounded contexts (initial)

- Identity & Access: `auth-service`, `user-service`, `profile-service`
- Discovery & Content: `fashion-discovery-service`, `moodboard-service`, `media-service`
- Catalog & Search: `product-service`, `search-service`, `visual-search-service`
- Intelligence: `recommendation-service`, `ai-stylist-service`, `outfit-detection-service`, `image-processing-service`, `virtual-tryon-service`
- Commerce: `shopping-service`, `order-service`, `payment-service`, `inventory-service`, `brand-integration-service`
- Platform: `notification-service`, `analytics-service`
- Edge/Platform infra: `api-gateway`, `discovery-service`, `config-server`

## Data ownership

Each service owns a dedicated PostgreSQL database. Cross-service data access must happen through APIs or events, never shared tables.

## Observability

- Correlation ID propagation (`X-Correlation-Id`)
- Structured console logging with MDC
- Micrometer metrics + Prometheus scrape endpoints
- Zipkin tracing bridge

## Resilience placeholders

- Gateway rate limiter (Redis RequestRateLimiter)
- Eureka registration/heartbeat
- Feign + load balancer ready for retries/circuit breakers in later phases
""",
    )
    write(
        ROOT / "docs/FOLDER_STRUCTURE.md",
        """
# Folder Explanation

## Root

- `pom.xml` - parent Maven aggregator and dependency management
- `common-lib/` - shared error model, events, security constants
- `config-repo/` - centralized configuration source for Config Server
- `docker-compose.yml` - full local/prod-like runtime topology
- `observability/` - Prometheus and Grafana provisioning
- `docker/postgres/` - per-service database bootstrap
- `docs/` - architecture and operating docs
- `scripts/` - generation/maintenance scripts

## Per service

- `config/` - Spring configuration beans
- `controller/` - inbound HTTP adapters
- `service/` - application services
- `repository/` - persistence ports
- `entity/` - JPA entities
- `dto/` - transport objects
- `mapper/` - MapStruct mappers
- `security/` - security filters/config placeholders
- `exception/` - local exception handling
- `client/` - OpenFeign clients
- `event/` - domain/integration events
- `kafka/` - producer/consumer/topic config
- `grpc/` - gRPC server/client placeholders
- `util/` - utilities (correlation IDs, etc.)
- `validation/` - custom validators
- `health/` - custom health indicators
""",
    )
    write(
        ROOT / ".gitignore",
        """
target/
.idea/
*.iml
.classpath
.project
.settings/
.vscode/
*.log
.DS_Store
.env
.env.*
!.env.example
**/bin/
**/out/
.mvn/wrapper/maven-wrapper.jar
!.mvn/wrapper/maven-wrapper.jar
""",
    )
    write(
        ROOT / ".env.example",
        """
SPRING_PROFILES_ACTIVE=dev
EUREKA_SERVER_URL=http://localhost:8761/eureka/
CONFIG_SERVER_URL=http://localhost:8888
CONFIG_SERVER_USERNAME=config
CONFIG_SERVER_PASSWORD=config
CONFIG_ENABLED=false
REDIS_HOST=localhost
REDIS_PORT=6379
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
ZIPKIN_URL=http://localhost:9411/api/v2/spans
DB_USERNAME=fashionpin
DB_PASSWORD=fashionpin
""",
    )


def generate_mvnw_note() -> None:
    write(
        ROOT / "mvnw",
        """#!/usr/bin/env bash
set -euo pipefail
if command -v mvn >/dev/null 2>&1; then
  exec mvn "$@"
fi
echo "Maven is required. Install Maven 3.9+ or add the Maven Wrapper jar under .mvn/wrapper." >&2
exit 1
""",
    )
    os.chmod(ROOT / "mvnw", 0o755)


def main() -> None:
    parent_pom()
    common_lib()
    generate_discovery()
    generate_config_server()
    generate_gateway()
    for service, port, db_name, with_jpa in BUSINESS_SERVICES:
        generate_business_service(service, port, db_name, with_jpa)
    generate_config_repo()
    generate_docker_compose()
    generate_observability()
    generate_root_docs()
    generate_mvnw_note()
    print(f"Generated Fashion Pin foundation at {ROOT}")


if __name__ == "__main__":
    main()
