FROM gradle:8.10.2-jdk23 AS builder
WORKDIR /opt/app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle build -x test --parallel --build-cache

FROM openjdk:23
WORKDIR /opt/app
COPY --from=builder /opt/app/build/libs/*.jar /opt/app/app.jar
ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]