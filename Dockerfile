FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY jeongminkim-core jeongminkim-core
COPY jeongminkim-application jeongminkim-application
COPY jeongminkim-api jeongminkim-api
RUN chmod +x ./gradlew
RUN ./gradlew :jeongminkim-api:bootJar

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/jeongminkim-api/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
