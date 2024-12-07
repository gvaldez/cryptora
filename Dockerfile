FROM gradle:8.10.2-jdk23 AS builder
WORKDIR /opt/app

COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle build --no-daemon -x test

FROM openjdk:23
WORKDIR /opt/app
EXPOSE 8088

COPY --from=builder /opt/app/build/libs/*.jar /opt/app/app.jar
ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]
