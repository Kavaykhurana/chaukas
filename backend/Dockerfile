# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 chaukas
COPY --from=build /app/target/chaukas-api-1.0.0.jar app.jar
USER chaukas
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75" SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
