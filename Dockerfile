FROM gradle:8.13-jdk17-alpine AS build
COPY src /app/src/
COPY config /app/config/
COPY build.gradle settings.gradle gradle.properties /app/
RUN cd /app && gradle -Dorg.gradle.welcome=never --no-daemon bootJar

FROM ghcr.io/bell-sw/liberica-openjre-debian:17
COPY --from=build /app/build/libs/nexus-sync-action.jar /opt/action/action.jar
ENTRYPOINT ["java", "-jar", "/opt/action/action.jar"]
