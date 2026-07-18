FROM gradle:8.7-jdk11 AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon -x test

FROM eclipse-temurin:11-jre
WORKDIR /app
# OpenTelemetry Java Agent — tracing distribuído (Jaeger via OTLP)
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.12.0/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
