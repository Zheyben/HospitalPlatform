FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY apps/backend/pom.xml apps/backend/pom.xml
COPY apps/backend/src apps/backend/src
COPY database/migrations database/migrations

WORKDIR /workspace/apps/backend
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

ENV SPRING_PROFILE=prod
EXPOSE 8080

COPY --from=build /workspace/apps/backend/target/hospital-platform-backend-*.jar app.jar

ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar --spring.profiles.active=${SPRING_PROFILE}"]
