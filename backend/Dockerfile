FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /work
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN apk upgrade --no-cache \
    && addgroup -S scenechain \
    && adduser -S -D -H -u 10001 -G scenechain scenechain
WORKDIR /app
COPY --from=build /work/target/scenechain-api-0.1.0.jar app.jar
USER scenechain
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
