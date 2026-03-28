FROM gradle:8.7-jdk11 AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon -x test

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
