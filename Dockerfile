FROM gradle:8.8-jdk22 AS builder
WORKDIR /opt/app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle build --no-daemon -x test

FROM eclipse-temurin:22-jre-jammy
WORKDIR /opt/app
EXPOSE 8088
COPY --from=builder /opt/app/build/libs/*.jar /opt/app/app.jar
ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]
